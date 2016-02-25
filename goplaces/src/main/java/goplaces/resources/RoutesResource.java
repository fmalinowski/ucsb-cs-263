/** I think we should consider checking if a route between a given source
 * and destination already exists in our datastore before querying
 * the Google API. It's definitely not required for correctness
 * but should help in increasing efficiency. - AT
*/


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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import lib.GoogleMap;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

// Will map the resource to the URL todos
@Path("/routes")
public class RoutesResource {

	@Context UriInfo uriInfo;
	@Context Request request;
	
	DatastoreService datastore;
	MemcacheService syncCache;
	
	public RoutesResource() {
		datastore = DatastoreServiceFactory.getDatastoreService();
		syncCache = MemcacheServiceFactory.getMemcacheService();
		
		syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
	}
	

	// Route boxer expects two parameters: a list of LatLng objects which comprise the route from 
	// the source to the destination, and a range that the boxes should cover around the path. For 
	// our application, a good default choice of this range can be 75 Kms.

	// Alternatively, a user can provide us with this range.

	// @PUT
 //  	@Consumes(MediaType.APPLICATION_JSON)
 //  	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
 //  	public String viewRoute()
  

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String createRoute(Route route,
							@Context HttpServletResponse servletResponse) {
		Place origin, destination;
		origin = route.getOrigin();
		destination = route.getDestination();
		
		if (origin == null || origin.getAddress() == null && origin.getLatitude() == 0 && origin.getLongitude() == 0 || 
				origin.getAddress() != null && origin.getAddress().isEmpty() && origin.getLatitude() == 0 && origin.getLongitude() == 0) {
			throw new RuntimeException("The origin is not specified");
		}
		if (destination == null || destination.getAddress() == null && destination.getLatitude() == 0 && destination.getLongitude() == 0 || 
				destination.getAddress() != null && destination.getAddress().isEmpty() && destination.getLatitude() == 0 && destination.getLongitude() == 0) {
			throw new RuntimeException("The destination is not specified");
		}
		
		JSONObject mapJSONResult = GoogleMap.getDirections(origin, destination);
		populateRouteWithInitialRouteFromMap(mapJSONResult, route);

		Entity originEntity = new Entity("Place", origin.getGooglePlaceId());
		originEntity.setProperty("name", origin.getName());
		originEntity.setProperty("address", origin.getAddress());
		originEntity.setProperty("latitude", origin.getLatitude());
		originEntity.setProperty("longitude", origin.getLongitude());
		Key originKey = datastore.put(originEntity);
		
		Entity destinationEntity = new Entity("Place", destination.getGooglePlaceId());
		destinationEntity.setProperty("name", destination.getName());
		destinationEntity.setProperty("address", destination.getAddress());
		destinationEntity.setProperty("latitude", destination.getLatitude());
		destinationEntity.setProperty("longitude", destination.getLongitude());
		Key destinationKey = datastore.put(destinationEntity);
		
		Entity routeEntity = new Entity("Route");
		routeEntity.setProperty("originPlaceID", origin.getGooglePlaceId());
		routeEntity.setProperty("destinationPlaceID", destination.getGooglePlaceId());
		routeEntity.setProperty("duration", route.getDuration());
		routeEntity.setProperty("distance", route.getDistance());
		
		Text mapJsonAsText = new Text(mapJSONResult.toString());
		routeEntity.setProperty("routeJSON", mapJsonAsText);
		
		Key routeKey = datastore.put(routeEntity);
		
		// We cache the route entity
		String cacheKey = "route-" + route.getId();
		syncCache.put(cacheKey, routeEntity);
		
		JSONObject answerJSON = new JSONObject();
		answerJSON.put("status", "OK");
		answerJSON.put("routeID", routeKey.getId());
		answerJSON.put("googledirections", mapJSONResult);
		
		return answerJSON.toString();
	}
	
	public void populateRouteWithInitialRouteFromMap(JSONObject mapJSON, Route route) {
		Place origin, destination;
		
		origin = route.getOrigin();
		destination = route.getDestination();
		
		try {
			JSONArray geocodedArray = mapJSON.getJSONArray("geocoded_waypoints");
			
			if (geocodedArray.length() > 0) {
				origin.setGooglePlaceId(geocodedArray.getJSONObject(0).getString("place_id"));
			}
			if (geocodedArray.length() > 1) {
				destination.setGooglePlaceId(geocodedArray.getJSONObject(1).getString("place_id"));
			}
			
		} catch(JSONException exception) {
		}
		
		try {
			JSONArray routesArray = mapJSON.getJSONArray("routes");
			JSONArray legsArray = routesArray.getJSONObject(0).getJSONArray("legs");
			JSONObject legObject = legsArray.getJSONObject(0);
			
			JSONObject originJSONObject = legObject.getJSONObject("start_location");
			JSONObject destinationJSONObject = legObject.getJSONObject("end_location");
			
			origin.setLatitude(originJSONObject.getDouble("lat"));
			origin.setLongitude(originJSONObject.getDouble("lng"));
			
			destination.setLatitude(destinationJSONObject.getDouble("lat"));
			destination.setLongitude(destinationJSONObject.getDouble("lng"));
			
			JSONObject distanceJSON = legObject.getJSONObject("distance");
			JSONObject durationJSON = legObject.getJSONObject("duration");
			
			route.setDistance(distanceJSON.getInt("value")); // In meters
			route.setDuration(durationJSON.getInt("value")); // In seconds
			
			origin.setAddress(legObject.getString("start_address"));
			destination.setAddress(legObject.getString("end_address"));
			
		} catch(JSONException exception) {
		}
	}
}
