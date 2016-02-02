package goplaces.resources;

import goplaces.models.Place;
import goplaces.models.Route;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import lib.RouteBoxer;
import goplaces.models.CustomizeRouteQuery;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;

@Path("/select_waypoints")
public class SelectWaypointsResource {

	@Context UriInfo uriInfo;
	@Context Request request;
	
	DatastoreService datastore;
	
	public SelectWaypointsResource(){
		datastore = DatastoreServiceFactory.getDatastoreService();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String selectWaypointsForRoute(CustomizeRouteQuery customizeRouteQuery, @Context HttpServletResponse servletResponse) {

		try{
			System.out.println("route ID: " + customizeRouteQuery.getRouteID());
			Entity originalRouteEntity = datastore.get(KeyFactory.createKey("Route", Long.parseLong(customizeRouteQuery.getRouteID())));
			System.out.println("wattup doodz");
			Text originalRouteJsonText = (Text)originalRouteEntity.getProperty("routeJSON");
			JSONObject answerJSON = new JSONObject(originalRouteJsonText.getValue());
			return answerJSON.toString();
		}
		catch(Exception e){
			return "Oops! " + e.getMessage();
		}

		// we have a default route and a set of keywords representing the interests of the user.
		// now box the default route --> for each keyword, search the boxes for those keywords in 
		// a radius by querying the google places API --> for each keyword, collect and return a 
		// list of potential places
	}
	

}