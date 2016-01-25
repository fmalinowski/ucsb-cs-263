package goplaces.resources;

import static org.junit.Assert.*;
import goplaces.models.Waypoint;

import java.net.URI;
import java.util.ArrayList;

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

public class FakeTestResourceIT {
	ClientConfig config = new ClientConfig();
	Client client = ClientBuilder.newClient(config);
	WebTarget service = client.target(getBaseURI());
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testTestAction() {
		String responseString = service.path("rest")
								 .path("tests")
								 .path("1")
								 .request()
								 .accept(MediaType.APPLICATION_JSON)
								 .get(String.class);
		JSONObject resultJSON = new JSONObject(responseString);
		
		JSONObject expectedJSONResult = new JSONObject();
		JSONObject originPlace = new JSONObject();
		try {
			originPlace.put("name", "start");
			originPlace.put("latitude", 1.1);
			originPlace.put("longitude", 2.2);
			originPlace.put("googlePlaceId", "id1");
			JSONObject destinationPlace = new JSONObject();
			destinationPlace.put("name", "start");
			destinationPlace.put("latitude", 1.1);
			destinationPlace.put("longitude", 2.2);
			destinationPlace.put("googlePlaceId", "id2");
			expectedJSONResult.put("origin", originPlace);
			expectedJSONResult.put("destination", destinationPlace);
			expectedJSONResult.put("mode", "DRIVING");
			expectedJSONResult.put("duration", 0.0);
			expectedJSONResult.put("distance", 0.0);
			expectedJSONResult.put("waypoints", new ArrayList<Waypoint>());
			
			assertEquals(resultJSON.toString(), expectedJSONResult.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}

	private static URI getBaseURI() {
		return UriBuilder.fromUri(
				"http://localhost:8080/").build();
	}
}