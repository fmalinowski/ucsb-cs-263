package goplaces.workers;

import goplaces.models.Place;
import goplaces.models.Route;

import javax.servlet.*;
import javax.servlet.http.*;

import java.io.*;
import java.util.*;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import org.json.simple.JSONArray;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;

import lib.GoogleMap;
import lib.RouteBoxer;
import goplaces.models.CustomizeRouteQuery;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.taskqueue.*;


/* This class runs in a background thread and calculates the potential waypoints along the given route.
 * The current implementation uses the "steps" parameter in the JSONObject returned by the Google
 * Directions API. 
 * Since some routes may include a long travel distance between any two consecutive steps, some potetial 
 * waypoints may not be caputured by this algorithm.
 * A better way to do it would be to use RouteBoxer
 * (Once we figure out how to make it memory friendly)
 *
 * The potential waypoints are stored as a one long string in the datastore.
 * Each JSONObject corresponding to a potential waypoint is simply converted to string and
 * appended.
 */


public class BoxRouteWorker extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try{ 
        	JSONParser parser = new JSONParser();       	
			String originalRouteJsonString = request.getParameter("originalroutejsontext");
			JSONObject originalRouteJsonObject = (JSONObject)parser.parse(originalRouteJsonString);
			JSONObject routes = (JSONObject)((JSONArray)originalRouteJsonObject.get("routes")).get(0);
			JSONArray legs = (JSONArray)routes.get("legs");
			JSONArray steps = (JSONArray)((JSONObject)legs.get(0)).get("steps");
			
			String routeID = request.getParameter("routeid");
			System.out.println("Route steps loaded in a JSONArray...size is " + steps.size());

            List<Double> stepLats = new ArrayList<Double>();
            List<Double> stepLngs = new ArrayList<Double>();
            
            for(int i = 0; i < steps.size(); i++){
                JSONObject temp = (JSONObject)((JSONObject)steps.get(i)).get("end_location");
                //System.out.println("Lat of end_location of step " + i + " " + temp.get("lat"));
                stepLats.add(Double.parseDouble(temp.get("lat").toString()));
                stepLngs.add(Double.parseDouble(temp.get("lng").toString()));    
            }   
            System.out.println("All steps set with size " + stepLngs.size() + " and " + stepLats.size());


            System.out.println("Skipping route boxer...");
            //RouteBoxer routeBoxer = new RouteBoxer(stepLats, stepLngs, Double.parseDouble(request.getParameter("radius")));
            
            //if(routeBoxer.getFlag())
              //  throw new RuntimeException("Could not create boxes for the route");

            //List<Double> boxLats = routeBoxer.getLats();
            //List<Double> boxLngs = routeBoxer.getLngs();

            //System.out.println("Calculated boxes with number of lats " + boxLats.size() + " and number of lngs " + boxLngs.size());
            
            double radius = Double.parseDouble(request.getParameter("radius").toString());
            
            String[] types = request.getParameter("keywords").split(",");
            System.out.println("Size of types is " + types.length);

            //JSONObject[][] places = new JSONObject[types.length][stepLats.size()];
            StringBuilder places = new StringBuilder();
            for(int j = 0; j < types.length; j++){
                for(int i = 0; i < stepLats.size(); i++){
                    //places[j][i] = ((JSONObject)parser.parse(GoogleMap.getPlacesAroundLocation(stepLats.get(i), stepLngs.get(i), radius, types[j]))).toJSONString();
                
                    places.append(((JSONObject)parser.parse(GoogleMap.getPlacesAroundLocation(stepLats.get(i), stepLngs.get(i), radius, types[j]))).toJSONString());
                }
            }
            System.out.println("Places found. These places are on the route at a radius of " + radius + " Kms around the whole length of the route. (Used steps of routes, not boxes around steps.)");
            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
            MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
            
            // add places as a property of original route entity
            try{
                Entity originalRouteEntity = datastore.get(KeyFactory.createKey("Route", Long.parseLong(routeID)));
                Text placesJsonAsText = new Text(places.toString());
                originalRouteEntity.setProperty("placesJSON", placesJsonAsText);
                datastore.put(originalRouteEntity);                
                System.out.println("SUCCESS written places to datastore");
                
                // We cache the route entity
        		String cacheKey = "route-" + routeID;
        		syncCache.put(cacheKey, originalRouteEntity);
            }
            catch(Exception e){
                System.out.println("ERROR " + e.getMessage());    
            }
        }	
        catch(Exception e){
        	System.out.println("ERROR " + e.getMessage());
        }

    }
}



