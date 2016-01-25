package goplaces.resources;

import goplaces.models.Place;
import goplaces.models.Route;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

//Will map the resource to the URL todos
@Path("/tests")
public class FakeTestResource {

	@Context UriInfo uriInfo;
	@Context Request request;
	
	@GET
	@Path("1")
	@Produces(MediaType.APPLICATION_JSON)
	public Route getTestAction() {
		Route route = new Route(new Place("start", 1.1, 2.2, "id1"), new Place("start", 1.1, 2.2, "id2"));
		route.setMode(Route.TransportationMode.DRIVING);
		return route;
	}
}

