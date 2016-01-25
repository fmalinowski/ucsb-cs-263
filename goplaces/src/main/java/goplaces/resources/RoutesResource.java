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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;

// Will map the resource to the URL todos
@Path("/routes")
public class RoutesResource {

	@Context UriInfo uriInfo;
	@Context Request request;
	
	private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public void createRoute(Route route,
							@Context HttpServletResponse servletResponse) {
		
	}
}
