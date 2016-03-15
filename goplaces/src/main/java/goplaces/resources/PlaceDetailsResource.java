/**
 * This URL responds to GET requests. The parameter it expects in a GET request
 * is the Google place_id as "place_id" for which ratings and reviews are required.
 * It responds with a JSON object containing a status, which is set to "OK"
 * when a correct response is generated, and a result, which includes the rating 
 * and reviews for the given place, separated by the string "/&/".
 *
 * @author Aviral Takkar
 */


package goplaces.resources;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import org.json.JSONObject;

import goplaces.models.CustomizeRouteQuery;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.taskqueue.*;

@Path("/place_details")
public class PlaceDetailsResource {

	@Context UriInfo uriInfo;
	@Context Request request;
	
	DatastoreService datastore;
	MemcacheService syncCache;
	
	private static final Logger log = Logger.getLogger(SelectWaypointsResource.class.getName());
	
	public PlaceDetailsResource(){
		datastore = DatastoreServiceFactory.getDatastoreService();
		syncCache = MemcacheServiceFactory.getMemcacheService();
		
		syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
	}

	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String getPlaceDetails(@QueryParam("place_id") String place_id, @Context HttpServletResponse servletResponse) {
		JSONObject answer = new JSONObject();
		//System.out.println("Place id is " + place_id);
		try{
			Entity place_details = (Entity)syncCache.get("place-"+place_id);
			if(place_details != null){
				//System.out.println("Place details in memcache (and datastore) for " + place_id);
				return answer.put("status","OK").put("result",(String)place_details.getProperty("rating") + "/&/" +
						((Text)place_details.getProperty("reviews")).getValue()).toString();
			}
			
			Entity place_details_entity = datastore.get(KeyFactory.createKey("Place", place_id));
			if(place_details_entity != null){
				//System.out.println("Place details in datastore for " + place_id);
				return answer.put("status","OK").put("result",(String)place_details_entity.getProperty("rating") +
						"/&/" + ((Text)place_details_entity.getProperty("reviews")).getValue()).toString();
			}
			return answer.put("status","NOT FOUND").toString();
		}
		catch(Exception e){
			System.out.println("PLACEDETAILSRESOURCE ERROR " + e.getMessage());
			return answer.put("status","NOT FOUND").toString();
		}
	}

}
	