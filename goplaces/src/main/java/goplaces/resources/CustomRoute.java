package goplaces.resources;

import goplaces.models.Place;
import goplaces.models.Route;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import lib.GoogleMap;
import goplaces.models.CustomRouteParameters;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.taskqueue.*;

/** This URL is used to get the customized route. The POST API requires the original routeID
 * and a list of strings as parameters. More details in models/CustomRouteParameters
 */


@Path("/get_custom_route")
public class CustomRoute {

	@Context UriInfo uriInfo;
	@Context Request request;
	
	DatastoreService datastore;
	MemcacheService syncCache;
	
	public CustomRoute(){
		datastore = DatastoreServiceFactory.getDatastoreService();
		syncCache = MemcacheServiceFactory.getMemcacheService();	
		
		syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
	}
	
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String getFinalRoute(@QueryParam("routeID") String routeID, @Context HttpServletResponse servletResponse) {
		
		try {
			String cacheKey = "finalroute-" + routeID;
		
			Entity routeEntity = null;
		
			if (syncCache.contains(cacheKey)) {
				routeEntity = (Entity)syncCache.get(cacheKey);
			} else {
				routeEntity = datastore.get(KeyFactory.createKey("Route", Long.parseLong(routeID)));
			}
		
			if (routeEntity.hasProperty("finalrouteJSON")) {
				Text finalrouteJsonText = (Text)routeEntity.getProperty("finalrouteJSON");
				return finalrouteJsonText.getValue();
			}
			
			JSONObject answerJSON = new JSONObject();
			answerJSON.put("status", "ERROR");
			answerJSON.put("message", "Final Route not yet computed!");
			return answerJSON.toString();
		} catch (Exception e) {
			e.printStackTrace();
			
			JSONObject answerJSON = new JSONObject();
			answerJSON.put("status", "ERROR");
			answerJSON.put("message", e.getMessage());
			return answerJSON.toString();
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String getCustomRoute(CustomRouteParameters customRouteParameters, @Context HttpServletResponse servletResponse) {

		try{			
			System.out.println("route ID: " + customRouteParameters.getRouteID());
			Entity routeEntity = datastore.get(KeyFactory.createKey("Route", Long.parseLong(customRouteParameters.getRouteID())));
			
			Place origin = new Place("","",0.0,0.0,(String)routeEntity.getProperty("originPlaceID"));
			Place destination = new Place("","",0.0,0.0,(String)routeEntity.getProperty("destinationPlaceID"));
			
			ArrayList<String> wp = customRouteParameters.getWaypoints();
			Place[] waypoints = new Place[wp.size()];

			for(int i = 0; i < wp.size(); i++)
				waypoints[i] = new Place("","",0.0,0.0,wp.get(i)); // may need to change this - we eventually want to search by google place id, and not name
			System.out.println("CUSTOMROUTE calling Google API...");
			
			String finalRouteJSON = GoogleMap.getCustomRoute(origin, destination, waypoints).toString();
			Text finalRouteJSONasText = new Text(finalRouteJSON);
			routeEntity.setProperty("finalrouteJSON", finalRouteJSONasText);
			
			String cacheKey = "finalroute-" + customRouteParameters.getRouteID();
			syncCache.put(cacheKey, routeEntity);
			datastore.put(routeEntity);
			
			return finalRouteJSON;
		}
		catch(Exception e){

			JSONObject answerJSON = new JSONObject();
			answerJSON.put("status", "ERROR");
			answerJSON.put("message", e.getMessage());
			return answerJSON.toString();
		}
	}
	

}