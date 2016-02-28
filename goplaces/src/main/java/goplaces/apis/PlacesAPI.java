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
 * TODO Document curl commands
 */

@Path("/places")
public class PlacesAPI {
    @Context
    UriInfo uriInfo;
    @Context
    Request request;
    DatastoreService datastore;
    MemcacheService syncCache;

    @GET
    @Produces(MediaType.TEXT_XML)
    public String getTodosBrowser() {
        return "Hi";
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String addPlace(Place place,
                        @Context HttpServletResponse servletResponse) throws IOException {
        try{
            StringBuilder msg = new StringBuilder();
            int flag = 0;
            Entity originEntity = new Entity("Place", place.getGooglePlaceId());
            originEntity.setProperty("name", place.getName());
            originEntity.setProperty("address", place.getAddress());
            originEntity.setProperty("latitude", place.getLatitude());
            originEntity.setProperty("longitude", place.getLongitude());
            originEntity.setProperty("rating", place.getRating());
            originEntity.setProperty("reviews", new Text(place.getReviews()));

            datastore = DatastoreServiceFactory.getDatastoreService();
            Key key = datastore.put(originEntity);
            return new JSONObject().append("status","OK").append("key", key.toString()).append("message",msg
                        .toString()).toString();
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
