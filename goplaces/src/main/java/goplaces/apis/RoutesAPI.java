package goplaces.apis;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.util.logging.*;
import com.google.appengine.api.datastore.*;
import com.google.appengine.api.memcache.*;
import goplaces.models.Place;
import goplaces.models.Route;
import org.json.JSONObject;

/**
 * Mapped to /routes.
 * GET, POST and DELETE are supported to manipulate Route objects in the datastore.
 *
 * @author Aviral Takkar
 *
 * TODO Document curl commands
 * Curl examples:
 *
 * GET - curl "http://go-places-ucsb.appspot.com/rest/routesapi/{route_id}"
 * POST - curl -i -H "Accept: application/json" -H "Content-Type: application/json" -X POST --data "{\"origin\":{\"address\":\"santa barbara\"},\"destination\":{\"address\":\"los angeles\"},\"mapJsonAsText\":\"xyz\"}" http://go-places-ucsb.appspot.com/rest/routesapi
 *
 */

@Path("/routesapi")
public class RoutesAPI {
    @Context
    UriInfo uriInfo;
    @Context
    Request request;
    DatastoreService datastore;
    MemcacheService syncCache;

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

            Text mapJsonAsText = route.getMapJsonAsText();
            routeEntity.setProperty("routeJSON", mapJsonAsText);
            datastore = DatastoreServiceFactory.getDatastoreService();
            Key routeKey = datastore.put(routeEntity);
            return new JSONObject().append("status","OK").append("key", routeKey.toString()).toString();
        }
        catch(Exception e){
            return new JSONObject().append("status","ERROR").append("message", "Could not store object.")
                    .toString();
        }
    }


    @GET
    @Path("{route_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getPlace(@PathParam("route_id") String routeID) {
        JSONObject reply = new JSONObject();
        try{

            syncCache = MemcacheServiceFactory.getMemcacheService();
            syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));

            String result = (String)syncCache.get(KeyFactory.createKey("Route", routeID));
            if(result != null)
                return reply.append("Found in", "memcache").append("Route Object", result).toString();

            datastore = DatastoreServiceFactory.getDatastoreService();
            Entity result2 = datastore.get(KeyFactory.createKey("Route", Long.parseLong(routeID)));
            System.out.println(3);
            reply.append("Found in", "datastore").append("Origin ID", result2.getProperty("originPlaceID")).append
                    ("Destination ID",
                    result2
                    .getProperty("destinationPlaceID"))
                    .append("Duration", result2.getProperty("duration")).append("distance", result2.getProperty
                    ("distance")).append("routeJSON", ((Text)result2.getProperty("routeJSON")).getValue()).append
                    ("status","OK");

            return reply.toString();
        }
        catch(Exception e){
            return reply.append("status","ERROR").append("message","Route object with ID " + routeID + " not found.")
                    .toString();
        }
    }
}