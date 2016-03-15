package goplaces;



import java.net.URI;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.client.ClientConfig;


public class ApplicationTester {

    private static final String LOG_TAG = "ApplicationTester";
    private static Response placeResponse, routeResponse, waypointsreviewResponse;
    private static final String EXISTING_ROUTE_KEY = "5076495651307520";
    private static Object waitOnMe;

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("USAGE for devserver: java -cp ... DSTester localhost port");
            System.err.println("USAGE for app_engine: java -cp ... DSTester appid.appspot.com 0000");
            System.exit(1);
        }
        String host;
        if(!args[1].equals("0000"))
            host = "http://"+args[0]+":"+args[1]+"/";
        else
            host = "http://"+args[0]+"/";

        URI this_uri = UriBuilder.fromUri(host).build();

        Client client = ClientBuilder.newClient(new ClientConfig());
        WebTarget service = client.target(this_uri);

        // Get a non existent place and route entity.
        try{
            int status = service.path("rest").path("places").path("placetester").request(MediaType.APPLICATION_JSON).get()
                    .getStatus();
            assert(status != 200);

            status = service.path("rest").path("routesapi").path("routetester").request(MediaType.APPLICATION_JSON).get()
                    .getStatus();
            assert(status != 200);

            System.out.println("Test 1 passed.");
        }
        catch(Exception e){
            System.err.println(LOG_TAG + " operation: get non existent place/route entity. error.");
            e.printStackTrace();
        }

        // Create new place and route entities in the Datastore
        try{
            String place = "{\"name\":\"UCSB\",\"address\":\"At the Pacific shore,California\",\"latitude\":5.6," +
                    "\"longitude\":6.6,\"rating\":5.0,\"reviews\":\"its magnificient\",\"googlePlaceId\":\"unknown\"}";
            String route = "{\"origin\":{\"address\":\"santa barbara\"},\"destination\":{\"address\":\"los " +
                    "angeles\"},\"mapJsonAsText\":\"xyz\"}";

            placeResponse = service.path("rest").path("places").request(MediaType.APPLICATION_JSON)
                    .put(Entity.entity(place,MediaType.APPLICATION_JSON),Response.class);
            assert(placeResponse.getStatus() == 201);

            routeResponse = service.path("rest").path("routesapi").request(MediaType.APPLICATION_JSON)
                    .put(Entity.entity(route,MediaType.APPLICATION_JSON),Response.class);
            assert(routeResponse.getStatus() == 201);
            System.out.println("Test 2 passed.");
        }
        catch(Exception e){
            System.err.println(LOG_TAG + " operation: post place entity. error.");
            e.printStackTrace();
        }


        // Get existing place and route entities
        try{

            placeResponse = service.path("rest").path("places").path("unknown").request(MediaType.APPLICATION_JSON).accept
                    (MediaType.APPLICATION_JSON).get();

            System.out.println(LOG_TAG + " operation: get existing place. status: " + placeResponse.getStatus());

            routeResponse = service.path("rest").path("routesapi").path(EXISTING_ROUTE_KEY).request(MediaType
                    .APPLICATION_JSON).accept
                    (MediaType
                    .APPLICATION_JSON).get();

            System.out.println(LOG_TAG + " operation: get existing route. status: " + routeResponse.getStatus());

            System.out.println("Test 3 passed.");
        }
        catch(Exception e){
            System.err.println(LOG_TAG + " operation: get existing place/route entity. error.");
            e.printStackTrace();
        }

        // Use the waypointsreviewapi
        try{
            String place_ids = "{\"place_ids\":\"[ChIJ1YMtb8cU6YARSHa612Q60cg]\"}";

            waypointsreviewResponse = service.path("rest").path("waypointsreviewapi").request(MediaType
                    .APPLICATION_JSON)
                    .put(Entity.entity(place_ids,MediaType.APPLICATION_JSON),Response.class);
            assert(waypointsreviewResponse.getStatus() == 201);

            // Give it about 2 seconds to get waypoint review.
            waitOnMe = new Object();

            synchronized (waitOnMe){
                waitOnMe.wait(2 * 1000);
            }


            waypointsreviewResponse = service.path("rest").path("waypointsreviewapi").path
                    ("ChIJ1YMtb8cU6YARSHa612Q60cg").request(MediaType.APPLICATION_JSON).accept
                    (MediaType.APPLICATION_JSON).get();
            assert(waypointsreviewResponse.getStatus() == 200);

            System.out.println(LOG_TAG + " operation: get existing place review. status: " + waypointsreviewResponse.getStatus());

            System.out.println("Test 4 passed.");
        }
        catch(Exception e){
            System.err.println(LOG_TAG + " operation: use the waypointsreviewapi. error.");
            e.printStackTrace();
        }
    }
}