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

//import org.json.simple.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
//import org.json.simple.parser.*;

import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query;

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

/** This class retrieves ratings and reviews for a list of places from Google
 * unless those details are already present in the datastore (or memcache)
 *
 * @post Expected parameter: "places" - A list of place IDs for which reviews are to be fetched. These reviews
 * would be fetched and stored in the datastore. The client polls /place_details (PlaceDetailsResource) to check if the reviews
 * for a particular place are available as yet.
 *
 *
 *
 * One potential problem is that the list of places is not unique many times.
 * This class first checks for existence of the place details locally in the datastore,
 * before making a Google API request.
 * Use a background task to fetch reviews instead of doing them in the
 * main application thread. The assumption here is that the number of place_ids would not be too 
 * large.
 *
 * @author Aviral Takkar
 */

public class WaypointsReview extends HttpServlet {

	/**
	 * This method expects an array of place_ids as parameter in the HttpServletRequest and for each place_id, it fetches the reviews and ratings
	 * from a Google API.
	 * It writes the fetched data to the datastore.
	 */

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		//System.out.println("WAYPOINTSREVIEW Yep we're here");

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();

		StringBuilder place_ids_builder = new StringBuilder(request.getParameter("places")).deleteCharAt(0);
		place_ids_builder.deleteCharAt(place_ids_builder.length() - 1);

		//System.out.println("WaypointsReview place ids received: " + place_ids_builder);
		String[] place_ids = place_ids_builder.toString().split(",");

		//System.out.println("WAYPOINTSREVIEW Number of places: " + place_ids.length);
		int count = 0;

		for (String place : place_ids) {
			//System.out.println("Place ID: " + place);
			try {
				if (syncCache.get("place-" + place) != null) {

		//			System.out.println("Place details in memcache (and datastore) for " + place);
					continue;
				}
				if (datastore.get(KeyFactory.createKey("Place", place)) != null) {
		//			System.out.println("Place details already in datastore for " + place);

					continue;
				}
				throw new Exception();
			} catch (Exception e) {
				count++;
			}
			JSONObject place_result;
			Entity placeEntity;
			try {
				place_result = (JSONObject) ((JSONObject) GoogleMap.getPlaceDetails(place.trim())).get("result");
		//		System.out.println("Place id: " + place_result.get("place_id"));
		//		System.out.println(" rating " + place_result.getDouble("rating"));
		//		System.out.println(" reviews " + place_result.get("reviews").toString());
				placeEntity = new Entity("Place", (String)place_result.get("place_id"));
			} catch (Exception e) {
				System.out.println("WAYPOINTSREVIEW ERROR " + e.getMessage());
				continue;
			}


			//try to set rating, if available
			try {
				placeEntity.setProperty("rating", place_result.get("rating"));
				//System.out.println("Set rating");
			} catch (Exception e) {
				placeEntity.setProperty("rating", "");
			}

			//try to set reviews, if available
			try {
				placeEntity.setProperty("reviews", new Text(place_result.get("reviews").toString()));
				//System.out.println("Set reviews");
			} catch (Exception e) {
				System.out.println(e.getMessage());
				placeEntity.setProperty("reviews", "");
			}

			try {
				datastore.put(placeEntity);
				System.out.println("Saved place entity with key ID " + place_result.getString("place_id"));
			} catch (Exception e) {
				System.out.println("WAYPOINTSREVIEW ERROR unable to store " + e.getMessage());
			}

			// add place details to cache
			try {
				String cacheKey = "place-" + place;
				syncCache.put(cacheKey, placeEntity);
			} catch (Exception e) {
				System.out.println("WAYPOINTSREVIEW ERROR unable to cache " + e.getMessage());
			}
		}
		System.out.println(count + " new places added to datastore.");
	}
}