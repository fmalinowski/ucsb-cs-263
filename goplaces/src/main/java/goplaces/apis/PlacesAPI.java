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
 * Mapped to /places.
 * GET, POST and DELETE are supported to manipulate Place objects in the datastore.
 *
 * @author Aviral Takkar
 *
 * Curl examples:
 *
 * GET - curl "http://go-places-ucsb.appspot.com/rest/places/{place_id}"
 * POST - curl -i -H "Accept: application/json" -H "Content-Type: application/json" -X POST --data "{\"name\":\"UCSB\",\"address\":\"At the Pacific shore,California\",\"latitude\":5.6,\"longitude\":6.6,\"rating\":5.0,\"reviews\":\"its magnificient\",\"googlePlaceId\":\"unknown\"}" http://go-places-ucsb.appspot.com/rest/places
 *
 */

@Path("/places")
public class PlacesAPI {
    @Context
    UriInfo uriInfo;
    @Context
    Request request;
    DatastoreService datastore;
    MemcacheService syncCache;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String addPlace(Place place,
                        @Context HttpServletResponse servletResponse) throws IOException {
        try{

            Entity entity = new Entity("Place", place.getGooglePlaceId());
            entity.setProperty("name", place.getName());
            entity.setProperty("address", place.getAddress());
            entity.setProperty("latitude", place.getLatitude());
            entity.setProperty("longitude", place.getLongitude());
            entity.setProperty("rating", place.getRating());
            entity.setProperty("reviews", new Text(place.getReviews()));

            datastore = DatastoreServiceFactory.getDatastoreService();
            Key key = datastore.put(entity);
            return new JSONObject().append("status","OK").append("key", key.toString()).toString();
        }
        catch(Exception e){
            return new JSONObject().append("status","ERROR").append("message", "Google Place ID is required.")
                    .toString();
        }
    }


    @GET
    @Path("{place_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getPlace(@PathParam("place_id") String googlePlaceId) {
        JSONObject reply = new JSONObject();
        try{
            syncCache = MemcacheServiceFactory.getMemcacheService();
            syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));

            String result = (String)syncCache.get(KeyFactory.createKey("Place", googlePlaceId));
            if(result != null)
                return reply.append("Found in", "memcache").append("Place Object", result).toString();


            datastore = DatastoreServiceFactory.getDatastoreService();
            Entity result2 = datastore.get(KeyFactory.createKey("Place", googlePlaceId));
            Text reviews = (Text)result2.getProperty("reviews");
            reply.append("Found in", "datastore").append("Name", result2.getProperty("name")).append("Address", result2
                    .getProperty("address"))
                    .append("Latitude", result2.getProperty("latitude")).append("longitude", result2.getProperty
                    ("longitude")).append("rating", result2.getProperty("rating")).append("reviews", reviews.getValue());

            return reply.toString();
        }
        catch(Exception e){
            return reply.append("ERROR", "Place object with ID " + googlePlaceId + " not found.").toString();
        }
    }
}
