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


/**
 * This class runs in a background thread and calculates the potential waypoints along the given route.
 * The current implementation uses the "steps" parameter in the JSONObject returned by the Google
 * Directions API. After fetching the waypoints, a background task is started (WaypointsReview) to get
 * reviews and ratings for these waypoints.
 *
 *
 * Since some routes may include a long travel distance between any two consecutive steps, some potential
 * waypoints may not be captured by this algorithm.
 * A better way to do it would be to use RouteBoxer
 * (Once we figure out how to make it memory friendly)
 *
 * @author Francois Malinowski
 * @author Aviral Takkar
 */


public class BoxRouteWorker extends HttpServlet {
	private final static int RADIUS_TO_LOOK_FOR_PLACES = 50000; // 5000 meters
	private final static int FINAL_PLACES_NUMBER_PER_REQUEST = 100;
	
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
			//System.out.println("Route steps loaded in a JSONArray...size is " + steps.size());

            List<Double> stepLats = new ArrayList<Double>();
            List<Double> stepLngs = new ArrayList<Double>();
            
            for(int i = 0; i < steps.size(); i++){
                JSONObject temp = (JSONObject)((JSONObject)steps.get(i)).get("end_location");
                //System.out.println("Lat of end_location of step " + i + " " + temp.get("lat"));
                stepLats.add(Double.parseDouble(temp.get("lat").toString()));
                stepLngs.add(Double.parseDouble(temp.get("lng").toString()));    
            }   
           // System.out.println("All steps set with size " + stepLngs.size() + " and " + stepLats.size());


            //System.out.println("Skipping route boxer...");
            //RouteBoxer routeBoxer = new RouteBoxer(stepLats, stepLngs, Double.parseDouble(request.getParameter("radius")));
            
            //if(routeBoxer.getFlag())
              //  throw new RuntimeException("Could not create boxes for the route");

            //List<Double> boxLats = routeBoxer.getLats();
            //List<Double> boxLngs = routeBoxer.getLngs();

            //System.out.println("Calculated boxes with number of lats " + boxLats.size() + " and number of lngs " + boxLngs.size());
            
            double r = Double.parseDouble(request.getParameter("radius").toString());
			int radius = r > RADIUS_TO_LOOK_FOR_PLACES ? RADIUS_TO_LOOK_FOR_PLACES : (int)r;
            
            String[] types = request.getParameter("keywords").split(",");
            System.out.println("Size of types is " + types.length);
            
            JSONObject finalPlacesJSONObject = new JSONObject();
            
            for(int j = 0; j < types.length; j++){
            	JSONArray jsonArrayForType = new JSONArray();
            	
                for(int i = 0; i < stepLats.size(); i++){
                	JSONObject placesAroundLocationJSONObject = (JSONObject)parser.parse(GoogleMap.getPlacesAroundLocation(stepLats.get(i), stepLngs.get(i), radius, types[j]));
                	JSONArray placesAroundLocationJSONArray = (JSONArray)placesAroundLocationJSONObject.get("results");
                	
                	if (!placesAroundLocationJSONArray.isEmpty()) {
                		jsonArrayForType.addAll(placesAroundLocationJSONArray);
                    }
                }
                finalPlacesJSONObject.put(types[j], jsonArrayForType);
            }
            List<String> place_ids = new ArrayList<String>();
            
            finalPlacesJSONObject = removeDuplicatePlaces(finalPlacesJSONObject);
            finalPlacesJSONObject = filterPlacesRandomly(finalPlacesJSONObject, FINAL_PLACES_NUMBER_PER_REQUEST, place_ids);
			
            //System.out.println("MAGIC " + place_ids.toString());
            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
            MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
            
            // add places as a property of original route entity
            Entity originalRouteEntity = datastore.get(KeyFactory.createKey("Route", Long.parseLong(routeID)));
            Text placesJsonAsText = new Text(finalPlacesJSONObject.toJSONString());
            originalRouteEntity.setProperty("placesJSON", placesJsonAsText);
            datastore.put(originalRouteEntity);                
            //System.out.println("SUCCESS written places to datastore");

			// add task for fetching place reviews to queue
            QueueFactory.getDefaultQueue().add(TaskOptions.Builder.withUrl("/waypointsreview").param("places", place_ids.toString()));

            System.out.println("Task to get reviews added to queue");
            // We cache the route entity
        	String cacheKey = "route-" + routeID;
        	syncCache.put(cacheKey, originalRouteEntity);
        }	
        catch(Exception e){
        	System.out.println("ERROR " + e.getMessage());
        	e.printStackTrace();
        }

    }
    
    public JSONObject removeDuplicatePlaces(JSONObject placesJSONObject) {
    	JSONObject initialJSON = (JSONObject) placesJSONObject.clone();
    	HashSet<String> placesPresence = new HashSet<String>();
    	JSONObject finalJSONObject = new JSONObject();
    	
    	Set<String> keywords = initialJSON.keySet();
    	
    	for (String keyword : keywords) {
    		JSONArray placesForKeyword = (JSONArray)initialJSON.get(keyword);
    		JSONArray finalPlacesForKeyword = new JSONArray();
    		
    		for (int i = 0; i < placesForKeyword.size(); i++) {
    			JSONObject placeJSON = (JSONObject) placesForKeyword.get(i);
    			
    			if (!placesPresence.contains((String)placeJSON.get("place_id"))) {
    				placesPresence.add((String)placeJSON.get("place_id"));
    				finalPlacesForKeyword.add(placeJSON);
    			}
    		}
    		finalJSONObject.put(keyword, finalPlacesForKeyword);
    	}
    	
    	return finalJSONObject;
    }
    
    public JSONObject filterPlacesRandomly(JSONObject placesJSONObject, int finalPlacesNumberPerRequest, List<String> place_ids) {
    	JSONObject initialJSON = (JSONObject) placesJSONObject.clone();
    	JSONObject finalJSONObject = new JSONObject();
    	int numberFields = placesJSONObject.size();
    	int remainingPlaces = finalPlacesNumberPerRequest;
    	int keywordsProcessed = 0;
    	
    	Set<String> keywords = initialJSON.keySet();
    	
    	for (String keyword : keywords) {
    		JSONArray placesForKeyword = (JSONArray)initialJSON.get(keyword);
    		JSONArray finalPlacesForKeyword = new JSONArray();
    		int placesForKeywordSize = placesForKeyword.size();
    		
    		int maxPlacesToKeepInFinalJSON = remainingPlaces / (numberFields - keywordsProcessed);
    		int placesToKeepInFinalJSON = Math.min(maxPlacesToKeepInFinalJSON, placesForKeywordSize);
    		remainingPlaces -= placesToKeepInFinalJSON;
    		
    		for (int i = 0; i < placesToKeepInFinalJSON; i++) {
    			int remainingPlacesInJSONArray = placesForKeyword.size();
    			int randomElementPosInArray = (new Random()).nextInt(remainingPlacesInJSONArray);
				JSONObject temp = (JSONObject)placesForKeyword.remove(randomElementPosInArray);
				place_ids.add((String)temp.get("place_id"));
    			finalPlacesForKeyword.add(temp);
    		}

    		finalJSONObject.put(keyword, finalPlacesForKeyword);
    		keywordsProcessed++;
    	}

    	return finalJSONObject;
    }
}



