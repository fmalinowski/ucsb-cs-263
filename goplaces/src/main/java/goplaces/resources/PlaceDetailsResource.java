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
}
	// @POST
	// @Produces(MediaType.APPLICATION_JSON)
	// //public String selectWaypointsForRoute(CustomizeRouteQuery customizeRouteQuery, @Context HttpServletResponse servletResponse) {
