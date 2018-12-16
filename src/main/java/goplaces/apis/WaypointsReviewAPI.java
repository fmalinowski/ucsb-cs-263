package goplaces.apis;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.taskqueue.*;
import goplaces.models.PlaceIDS;
import org.json.JSONObject;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;


/**
 * Mapped to /enqueue_waypointsreview_task.
 * This API supports POST requests to add WaypointsReview background task to the default queue.
 *
 * @author Aviral Takkar
 *
 *
 * POST - curl -X POST -H "Content-Type: application/json" -H "Cache-Control: no-cache" -H "Postman-Token: a053ea25-82ca-9d9b-4cfa-f791f2ada6d6" -d '{
 * "place_ids":"[ChIJ1YMtb8cU6YARSHa612Q60cg]"
 * }' "http://localhost:8080/rest/waypointsreviewapi"
 *
 * GET - curl -X GET -H "Content-Type: application/json" -H "Cache-Control: no-cache" "http://localhost:8080/rest/waypointsreviewapi/ChIJ1YMtb8cU6YARSHa612Q60cg"
 *
 *
 */
@Path("/waypointsreviewapi")
public class WaypointsReviewAPI{
    @Context
    UriInfo uriInfo;
    @Context
    Request request;
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String addPlace(PlaceIDS placeids,
                           @Context HttpServletResponse servletResponse) throws IOException {
        try {
            String place_ids = placeids.getPlace_ids();
            System.out.println("WAYPOINTSREVIEWAPI place_ids " + place_ids);
            QueueFactory.getDefaultQueue().add(TaskOptions.Builder.withUrl("/waypointsreview").param("places", place_ids));
            return new JSONObject().append("status","ok").append("message","New task created to fetch reviews.").toString();
        }
        catch(Exception e) {
            System.out.println("Could not enqueue task for fetching place reviews.");
            e.printStackTrace();
            return new JSONObject().append("status", "fail").append("message", "Could not create background task").toString();
        }
    }

    @GET
    @Path("{place_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getPlace(@PathParam("place_id") String googlePlaceId) {
        JSONObject reply = new JSONObject();
        try{

            Entity result2 = datastore.get(KeyFactory.createKey("Place", googlePlaceId));

            Text reviews = (Text)result2.getProperty("reviews");

            reply.append("status", "OK").append("message", "Found in datastore.");

            if(result2.getProperty("name") != null)
                reply.append("Name", result2.getProperty("name"));

            if(result2.getProperty("address") != null)
                reply.append("Address", result2.getProperty("address"));

            if(result2.getProperty("rating") != null)
                reply.append("rating", result2.getProperty("rating"));

            if(reviews != null)
                reply.append("reviews", reviews.getValue());


            return reply.toString();
        }
        catch(Exception e){
            return new JSONObject().append("status", "fail").append("message", "Place object with ID " + googlePlaceId + " " +
                    "not found").toString();
        }
    }
}