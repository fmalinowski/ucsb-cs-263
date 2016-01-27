package goplaces.resources;

import static org.junit.Assert.*;

import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.client.ClientConfig;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class RoutesResourceIT {
	
	// Testing environment for google datastore
	private final LocalServiceTestHelper helper =
		      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
	
	ClientConfig config = new ClientConfig();
	Client client = ClientBuilder.newClient(config);
	WebTarget service = client.target(getBaseURI());
	
	DatastoreService datastore;
	
	@Before
	public void setUp() throws Exception {
		helper.setUp();
		datastore = DatastoreServiceFactory.getDatastoreService();
	}

	@After
	public void tearDown() throws Exception {
		helper.tearDown();
	}
	
	@Test
	public void testCreateRoute() {
		String originGooglePlaceID = "ChIJN9OIQ0I_6YARy4_Q_WdjPC0";
		String destinationGooglePlaceID = "ChIJ25e4v7pA6YARnROZU3zkYQI";
		
		Key originKey = KeyFactory.createKey("Place", originGooglePlaceID);
		Key destinationKey = KeyFactory.createKey("Place", destinationGooglePlaceID);
		
		datastore.delete(originKey);
		datastore.delete(destinationKey);
		
		JSONObject newRoute = new JSONObject();
		JSONObject originPlace = new JSONObject();
		JSONObject destinationPlace = new JSONObject();
		
		originPlace.put("address", "6636 Del Playa Dr, Isla Vista CA");
		destinationPlace.put("address", "6638 Del Playa Dr, Isla Vista");
		newRoute.put("origin", originPlace);
		newRoute.put("destination", destinationPlace);
		
		// Assert the places do not exist in the Datastore
		assertTrue(isPlaceEntityNotFound(originGooglePlaceID));
		assertTrue(isPlaceEntityNotFound(destinationGooglePlaceID));
		
		// Send the POST request
		String responseString = service.path("rest")
									   .path("routes")
									   .request(MediaType.APPLICATION_JSON)
									   .post(javax.ws.rs.client.Entity.entity(newRoute.toString(), MediaType.APPLICATION_JSON))
									   .readEntity(String.class);
		
		JSONObject responseJSON = new JSONObject(responseString);
		
		// Assert the JSON returned from server is correct
		assertEquals("OK", responseJSON.get("status"));
		assertTrue(Long.parseLong(responseJSON.get("routeID").toString()) > 0);
		assertTrue(responseJSON.has("googledirections"));	
		String googleDirectionsStart = "{\"geocoded_waypoints\":[{\"";
		assertTrue(responseJSON.get("googledirections").toString().startsWith(googleDirectionsStart));
		
		// Assert values stored in DB are correct
//		assertFalse(isPlaceEntityNotFound(originGooglePlaceID));
//		assertFalse(isPlaceEntityNotFound(destinationGooglePlaceID));
		
//		Entity routeEntity = null;
//		try {
//			routeEntity = datastore.get(KeyFactory.createKey("Route", Long.parseLong(responseJSON.get("routeID").toString())));
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		
//		assertNotNull(routeEntity);
//		assertEquals(originGooglePlaceID, routeEntity.getProperty("originPlaceID"));
//		assertEquals(destinationGooglePlaceID, routeEntity.getProperty("destinationPlaceID"));
//		assertEquals(2, routeEntity.getProperty("duration"));
//		assertEquals(8, routeEntity.getProperty("distance"));
//		Text resultGoogleDirections = (Text) routeEntity.getProperty("routeJSON");
//		assertEquals(responseJSON.get("googledirections").toString(), resultGoogleDirections.toString());
	}
	
	private boolean isPlaceEntityNotFound(String googlePlaceID) {
		boolean placeFound = true;
		try {
			Key placeKey = KeyFactory.createKey("Place", googlePlaceID);
			datastore.get(placeKey);
		} catch (EntityNotFoundException e) {
			placeFound = false;
		}
		return !placeFound;
	}

	private static URI getBaseURI() {
		return UriBuilder.fromUri(
				"http://localhost:8080/").build();
	}
}
