package goplaces.apis;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import goplaces.models.CustomizeRouteQuery;
import org.json.JSONObject;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;


/**
 * Mapped to /enqueue_boxrouteworker_task.
 * This API supports GET and POST requests to add BoxRouteWorker background task to the default queue.
 *
 * @author Aviral Takkar
 *
 *
 * POST - curl -X POST -H "Content-Type: application/json" -H "Cache-Control: no-cache" -H "Postman-Token: e21b5c97-cddf-1ae6-c2a9-96d7c3eb08de" -d '{
 * "routeID":"5081456606969856",
 * "radius":"10",
 * "keywords":["pet park","museum"]
 * }' "http://localhost:8080/rest/boxrouteworkerapi"
 *
 * GET - curl -X GET -H "Content-Type: application/json" -H "Cache-Control: no-cache" -H "Postman-Token: 888a79a6-8bad-73cd-df51-c90edb0aba6a" "http://go-places-ucsb.appspot.com/rest/boxrouteworkerapi/5081456606969856"
 */

@Path("/boxrouteworkerapi")
public class BoxRouteWorkerAPI{
        @Context
        UriInfo uriInfo;
        @Context
        Request request;
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        @POST
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        public String addNewTask(CustomizeRouteQuery route,
                               @Context HttpServletResponse servletResponse) throws IOException {

            try{
                String route_id = route.getRouteID();
                String radius = String.valueOf(route.getRadius());
                StringBuilder keywords = new StringBuilder();

                if(route.getKeywords().length > 5)
                    return new JSONObject().append("status","fail").append("message","too many keywords").toString();

                for(String keyword : route.getKeywords())
                    keywords.append(keyword + ",");
                System.out.println(1 + " Route ID " + Long.parseLong(route_id));
                Entity originalRouteEntity = datastore.get(KeyFactory.createKey("Route", Long.parseLong(route_id)));
                if(originalRouteEntity == null){
                    return new JSONObject().append("status", "fail").append("message", "route not found in datastore" +
                            ".").toString();
                }
                Text originalRouteJsonText = (Text)originalRouteEntity.getProperty("routeJSON");
                if(originalRouteJsonText == null){
                    return new JSONObject().append("status", "fail").append("message", "route not found in datastore" +
                            ".").toString();
                }
                String originalRouteJSON = originalRouteJsonText.getValue();
                Queue queue = QueueFactory.getDefaultQueue();
                queue.add(TaskOptions.Builder.withUrl("/boxroute").param("originalroutejsontext", originalRouteJSON)
                        .param("routeid", route_id).param("radius", radius)
                        .param("keywords", keywords.toString()));
                return new JSONObject().append("status", "ok").append("message", "task fired off.").toString();
            }
            catch(Exception e){
                e.printStackTrace();
            }
            return new JSONObject().append("status", "fail").append("message","something went wrong").toString();
        }

    @GET
    @Path("{route_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getPlace(@PathParam("route_id") String routeID) {
        JSONObject reply = new JSONObject();
        try{

            Entity result2 = datastore.get(KeyFactory.createKey("Route", Long.parseLong(routeID)));
            if(result2 == null || result2.getProperty("placesJSON") == null)
                return reply.append("status", "fail").append("message", "Could not find places around the given route" +
                        " in datastore.").append("routeID", routeID).toString();

            reply.append("status", "OK");
            reply.append("message", "Found in datastore.");
            reply.append("routeID", routeID);
            reply.append("placesJSON",((Text)(result2.getProperty("placesJSON"))).getValue());
            return reply.toString();
        }
        catch(Exception e){
            e.printStackTrace();
            System.out.println(e.getMessage());
            return new JSONObject().append("status","fail").append("message","no such route")
                    .toString();
        }
    }
}