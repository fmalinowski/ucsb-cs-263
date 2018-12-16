package goplaces.apis;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import com.google.appengine.api.datastore.*;
import com.google.appengine.api.memcache.*;
import goplaces.models.Route;
import org.json.JSONObject;

/**
 * Mapped to /routesapi.
 * GET, POST and DELETE are supported to manipulate Route objects in the datastore.
 *
 * @author Aviral Takkar
 *
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
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

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
            Key routeKey = datastore.put(routeEntity);
            return new JSONObject().append("status","OK").append("key", routeKey.getId()).toString();
        }
        catch(Exception e){
            return new JSONObject().append("status","fail").append("message", "Could not store object.")
                    .toString();
        }
    }


    @GET
    @Path("{route_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getPlace(@PathParam("route_id") String routeID) {
        JSONObject reply = new JSONObject();
        try{

            Entity result2 = datastore.get(KeyFactory.createKey("Route", Long.parseLong(routeID)));
            reply.append("status", "OK");
            reply.append("message", "Found in datastore.");
            reply.append("Origin ID", result2.getProperty("originPlaceID"));
            reply.append("Destination ID", result2.getProperty("destinationPlaceID"));
            reply.append("Duration", result2.getProperty("duration"));
            reply.append("distance", result2.getProperty("distance"));
            reply.append("origin", result2.getProperty("origin")).append("destination", result2.getProperty
                    ("destination"));
            Text routeJSON = (Text)result2.getProperty("routeJSON");
            if (routeJSON != null)
                reply.append("routeJSON", routeJSON.getValue());

            return reply.toString();
        }
        catch(Exception e){
            e.printStackTrace();
            System.out.println(e.getMessage());
            return new JSONObject().append("status","fail").append("message","Route object with ID " + routeID + " not found.")
                    .toString();
        }
    }

    @DELETE
    @Path("{route_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String deletePlace(@PathParam("route_id") String routeId){
        JSONObject reply = new JSONObject();
        try{
            datastore.delete(KeyFactory.createKey("Route", Long.parseLong(routeId)));
            return reply.append("status", "OK").append("message", "successfully deleted route " + routeId)
                    .toString();
        }
        catch(Exception e){
            return new JSONObject().append("status", "fail").append("message", "Route object with ID " +
                    routeId + " " +
                    "not" +
                    " " +
                    "found.").toString();
        }
    }
}