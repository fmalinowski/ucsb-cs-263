package goplaces.resources;
import static org.junit.Assert.*;
import goplaces.models.Place;
import goplaces.models.Route;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class RoutesResourceTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testPopulateRouteWithInitialRouteFromMap() {
		String googleJSONString = "{" +
	   "\"geocoded_waypoints\" : [" +
	   "   {" +
	   "      \"geocoder_status\" : \"OK\"," +
	   "      \"place_id\" : \"ChIJN9OIQ0I_6YARy4_Q_WdjPC0\"," +
	   "      \"types\" : [ \"street_address\" ]" +
	   "   }," +
	   "   {" +
	   "      \"geocoder_status\" : \"OK\"," +
	   "      \"place_id\" : \"Eiw2NjM4IERlbCBQbGF5YSBEciwgSXNsYSBWaXN0YSwgQ0EgOTMxMTcsIFVTQQ\"," +
	   "      \"types\" : [ \"street_address\" ]" +
	   "   }" +
	   "]," +
	   "\"routes\" : [" +
	   "   {" +
	   "      \"bounds\" : {" +
	   "         \"northeast\" : {" +
	   "            \"lat\" : 34.4097624," +
	   "            \"lng\" : -119.8601637" +
	   "         }," +
	   "         \"southwest\" : {" +
	   "            \"lat\" : 34.4097622," +
	   "            \"lng\" : -119.8602522" +
	   "         }" +
	   "      }," +
	   "      \"copyrights\" : \"Données cartographiques ©2016 Google\"," +
	   "      \"legs\" : [" +
	   "         {" +
	   "            \"distance\" : {" +
	   "               \"text\" : \"26 pieds\"," +
	   "               \"value\" : 8" +
	   "            }," +
	   "            \"duration\" : {" +
	   "               \"text\" : \"1 minute\"," +
	   "               \"value\" : 2" +
	   "            }," +
	   "            \"end_address\" : \"6638 Del Playa Dr, Isla Vista, CA 93117, États-Unis\"," +
	   "            \"end_location\" : {" +
	   "               \"lat\" : 34.4097624," +
	   "               \"lng\" : -119.8602522" +
	   "            }," +
	   "            \"start_address\" : \"6636 Del Playa Dr, Goleta, CA 93117, États-Unis\"," +
	   "            \"start_location\" : {" +
	   "               \"lat\" : 34.4097622," +
	   "               \"lng\" : -119.8601637" +
	   "            }," +
	   "            \"steps\" : [" +
	   "               {" +
	   "                  \"distance\" : {" +
	   "                     \"text\" : \"26 pieds\"," +
	   "                     \"value\" : 8" +
	   "                  }," +
	   "                  \"duration\" : {" +
	   "                     \"text\" : \"1 minute\"," +
	   "                     \"value\" : 2" +
	   "                  }," +
	   "                  \"end_location\" : {" +
	   "                     \"lat\" : 34.4097624," +
	   "                     \"lng\" : -119.8602522" +
	   "                  }," +
	   "                  \"html_instructions\" : \"Prendre la direction \\u003cb\\u003eouest\\u003c/b\\u003e sur \\u003cb\\u003eDel Playa Dr\\u003c/b\\u003e\"," +
	   "                  \"polyline\" : {" +
	   "                     \"points\" : \"_t_qE~dqzU?P\"" +
	   "                  }," +
	   "                  \"start_location\" : {" +
	   "                     \"lat\" : 34.4097622," +
	   "                     \"lng\" : -119.8601637" +
	   "                  }," +
	   "                  \"travel_mode\" : \"DRIVING\"" +
	   "               }" +
	   "            ]," +
	   "            \"via_waypoint\" : []" +
	   "         }" +
	   "      ]," +
	   "      \"overview_polyline\" : {" +
	   "         \"points\" : \"_t_qE~dqzU?P\"" +
	   "      }," +
	   "      \"summary\" : \"Del Playa Dr\"," +
	   "      \"warnings\" : []," +
	   "      \"waypoint_order\" : []" +
	   "   }" +
	   "]," +
	   "\"status\" : \"OK\"" +
	   "}";
		
		Route route = new Route(new Place(), new Place());
		JSONObject googleJSON = new JSONObject(googleJSONString);
		
		RoutesResource routesResource = new RoutesResource();
		routesResource.populateRouteWithInitialRouteFromMap(googleJSON, route);
		
		assertEquals("ChIJN9OIQ0I_6YARy4_Q_WdjPC0", route.getOrigin().getGooglePlaceId());
		assertEquals("Eiw2NjM4IERlbCBQbGF5YSBEciwgSXNsYSBWaXN0YSwgQ0EgOTMxMTcsIFVTQQ", route.getDestination().getGooglePlaceId());
		assertEquals(34.4097622, route.getOrigin().getLatitude(), 0.0000001);
		assertEquals(-119.8601637, route.getOrigin().getLongitude(), 0.0000001);
		assertEquals(34.4097624, route.getDestination().getLatitude(), 0.0000001);
		assertEquals(-119.8602522, route.getDestination().getLongitude(), 0.0000001);
		assertEquals("6636 Del Playa Dr, Goleta, CA 93117, États-Unis", route.getOrigin().getAddress());
		assertEquals("6638 Del Playa Dr, Isla Vista, CA 93117, États-Unis", route.getDestination().getAddress());
		assertEquals(2, route.getDuration());
		assertEquals(8, route.getDistance());
	}

}
