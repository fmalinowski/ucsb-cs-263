package goplaces.resources;

import static org.junit.Assert.*;

import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.client.ClientConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RoutesResourceIT {
	ClientConfig config = new ClientConfig();
	Client client = ClientBuilder.newClient(config);
	WebTarget service = client.target(getBaseURI());
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

//	@Test
//	public void testTestAction() {
//		String responseString = service.path("routes")
//								 .path("test")
//								 .request()
//								 .accept(MediaType.TEXT_PLAIN)
//								 .get(String.class);
//		
//		assertEquals("YOLO", responseString);
//	}

	private static URI getBaseURI() {
		return UriBuilder.fromUri(
				"http://localhost:8080/").build();
	}
}
