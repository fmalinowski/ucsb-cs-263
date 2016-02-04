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

@Path("/tests")
public class FakeTestResource {

	@Context UriInfo uriInfo;
	@Context Request request;
	
	@GET
	@Path("1")
	@Produces(MediaType.APPLICATION_JSON)
	public Route getTestAction() {
		Place originPlace = new Place("start", "whatever", 0, 1, "id1");
		Place destinationPlace = new Place("dest", "whatever", 2, 3, "id2");
		Route route = new Route(originPlace, destinationPlace);
		route.setMode(Route.TransportationMode.DRIVING);
		return route;
	}
}

