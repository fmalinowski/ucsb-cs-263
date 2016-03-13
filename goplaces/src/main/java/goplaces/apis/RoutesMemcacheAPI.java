package goplaces.apis;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.util.Random;
import java.util.logging.*;
import com.google.appengine.api.datastore.*;
import com.google.appengine.api.memcache.*;
import goplaces.models.Route;
import org.json.JSONObject;

/**
 * Mapped to /routesmemcache.
 * GET, POST and DELETE are supported to manipulate Route objects in the datastore.
 *
 * @author Aviral Takkar
 *
 * Curl examples:
 *
 * GET - curl "http://go-places-ucsb.appspot.com/rest/routesmemcache/{route_id}"
 *
 * POST - curl -i -H "Accept: application/json" -H "Content-Type: application/json" -X POST --data
 * "{\"origin\":{\"address\":\"santa barbara\"},\"destination\":{\"address\":\"los angeles\"},
 * \"mapJsonAsText\":\"xyz\"}" http://go-places-ucsb.appspot.com/rest/routesmemcache
 *
 */

@Path("/routesmemcache")
public class RoutesMemcacheAPI {
    @Context
    UriInfo uriInfo;
    @Context
    Request request;
    Random key = new java.util.Random();
    MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String addRoute(Route route,
                           @Context HttpServletResponse servletResponse) throws IOException {
        try{
            Entity routeEntity = new Entity("Route");
            routeEntity.setProperty("originPlaceID", route.getOrigin().getGooglePlaceId());
            routeEntity.setProperty("destinationPlaceID", route.getDestination().getGooglePlaceId());
            routeEntity.setProperty("duration", route.getDuration());
            routeEntity.setProperty("distance", route.getDistance());
            routeEntity.setProperty("origin", route.getOrigin().getAddress());
            routeEntity.setProperty("destination", route.getDestination().getAddress());

            Text mapJsonAsText = route.getMapJsonAsText();
            routeEntity.setProperty("routeJSON", mapJsonAsText);

            syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
            String cacheKey = "route-" + key.nextLong();
            syncCache.put(cacheKey, routeEntity);
            return new JSONObject().append("status","OK").append("key", cacheKey).toString();
        }
        catch(Exception e){
            return new JSONObject().append("status","fail").append("message", "Could not cache object.")
                    .toString();
        }
    }


    @GET
    @Path("{route_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getPlace(@PathParam("route_id") String routeID) {
        JSONObject reply = new JSONObject();
        try{

            syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));

            Entity result = (Entity)syncCache.get(routeID);
            if(result != null)
                return reply.append("status", "OK").append("message", "Found in memcache.").append("Route Object", result).toString();

            return reply.append("status", "fail").append("message", "Not found in memcache.").toString();
        }
        catch(Exception e){
            return new JSONObject().append("status","fail").append("message","Route object with ID " + routeID + " not found.")
                    .toString();
        }
    }

    @DELETE
    @Path("{route_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String deletePlace(@PathParam("route_id") String routeID){
        syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
        JSONObject reply = new JSONObject();
        try{
            syncCache.delete(routeID);
            return reply.append("status", "OK").append("message", "successfully deleted route " + routeID + " from " +
                    "memcache")
                    .toString();
        }
        catch(Exception e){
            return new JSONObject().append("status", "fail").append("message", e.getMessage()).toString();
        }
    }

}
