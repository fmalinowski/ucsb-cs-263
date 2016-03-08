package goplaces.apis;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.util.logging.*;
import com.google.appengine.api.datastore.*;
import com.google.appengine.api.memcache.*;
import goplaces.models.Place;
import org.json.JSONObject;


/**
 * Mapped to /placesmemcache.
 * GET, POST and DELETE are supported to manipulate Place objects in the memcache.
 *
 * @author Aviral Takkar
 *
 * Curl examples:
 *
 * GET - curl "http://go-places-ucsb.appspot.com/rest/placesmemcache/{place_id}"
 *
 * POST - curl -i -H "Accept: application/json" -H "Content-Type: application/json" -X POST --data
 * "{\"name\":\"UCSB\",\"address\":\"At the Pacific shore,California\",\"latitude\":5.6,\"longitude\":6.6,
 * \"rating\":5.0,\"reviews\":\"its magnificient\",\"googlePlaceId\":\"unknown\"}" http://go-places-ucsb.appspot
 * .com/rest/placesmemcache
 *
 */

@Path("/placesmemcache")
public class PlacesMemcacheAPI {

    @Context
    UriInfo uriInfo;
    @Context
    Request request;
    DatastoreService datastore;
    MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String addPlace(Place place,
                           @Context HttpServletResponse servletResponse) throws IOException {
        try{
            syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
            Entity entity = new Entity("Place", place.getGooglePlaceId());
            entity.setProperty("name", place.getName());
            entity.setProperty("address", place.getAddress());
            entity.setProperty("latitude", place.getLatitude());
            entity.setProperty("longitude", place.getLongitude());
            entity.setProperty("rating", place.getRating());
            entity.setProperty("reviews", new Text(place.getReviews()));

            String cacheKey = "place-" + place.getGooglePlaceId();
            syncCache.put(cacheKey, entity);
            return new JSONObject().append("status","OK").append("key", cacheKey).toString();
        }
        catch(Exception e){
            return new JSONObject().append("status","fail").append("message", "Google Place ID is required.")
                    .toString();
        }
    }


    @GET
    @Path("{place_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getPlace(@PathParam("place_id") String googlePlaceId) {
        syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
        JSONObject reply = new JSONObject();
        try{
            Entity result = (Entity)syncCache.get(googlePlaceId);
            if(result != null)
                return reply.append("status", "OK").append("message", "Found in memcache.").append("Place Object", result).toString();

            return reply.append("status", "fail").append("message", "Not found in memcache.").toString();
        }
        catch(Exception e){
            return new JSONObject().append("status", "fail").append("message", e.getMessage()).toString();
        }
    }

    @DELETE
    @Path("{place_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String deletePlace(@PathParam("place_id") String googlePlaceId){
        syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
        JSONObject reply = new JSONObject();
        try{
            syncCache.delete(googlePlaceId);
            return reply.append("status", "OK").append("message", "successfully deleted place " + googlePlaceId)
                    .toString();
        }
        catch(Exception e){
            return new JSONObject().append("status", "fail").append("message", "Place object with ID " + googlePlaceId + " " +
                    "not " +
                    "found in memcache.").toString();
        }
    }

}
