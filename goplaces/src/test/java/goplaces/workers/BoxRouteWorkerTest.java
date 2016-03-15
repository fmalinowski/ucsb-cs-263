package goplaces.workers;

import static org.junit.Assert.*;

import java.util.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BoxRouteWorkerTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	// Commenting this out because I want to check the 100 (at most) places which are returned by BoxRouteWorker
	@Test
	public void testFilterPlacesRandomly() {
		JSONObject initialJSONObject = new JSONObject();
		int finalPlacesNumberPerRequest = 100;
		
		for (int key = 0; key < 20; key++) {
			JSONArray jsonArrayForKey = new JSONArray();
			
			for (int valueIndex = 0; valueIndex < finalPlacesNumberPerRequest; valueIndex++) {
				int value = key * finalPlacesNumberPerRequest + valueIndex;
				JSONObject jsonObjectForArray = new JSONObject();
				jsonObjectForArray.put(String.valueOf(valueIndex), value);
				jsonArrayForKey.add(jsonObjectForArray);
			}
			initialJSONObject.put(String.valueOf(key), jsonArrayForKey);
		}
		
		assertEquals(20, initialJSONObject.keySet().size());
		Set<String> keySet = initialJSONObject.keySet();
		
		for (String key : keySet) {
			assertEquals(100, ((JSONArray)initialJSONObject.get(key)).size());
		}
		
		// We get an JSONObject that looks like:
		/*
		 * {
		 * 		"0": [
		 * 				{"0": 0},
		 * 				{"1": 1},
		 * 				{"2": 2},
		 * 				...
		 * 				{"99": 99}
		 * 			],
		 * 		"1": [
		 * 				{"0": 100},
		 * 				{"1": 101},
		 * 				{"2": 102},
		 * 				...
		 * 				{"99": 199}
		 * 			],
		 * 		...
		 * 		"19": [
		 * 				{"0": 1900},
		 * 				{"1": 1901},
		 * 				{"2": 1902},
		 * 				...
		 * 				{"99": 1999}
		 * 			]
		 * }
		 */
		BoxRouteWorker boxRouteWorker = new BoxRouteWorker();
		JSONObject finalJSONObject = boxRouteWorker.filterPlacesRandomly(initialJSONObject, 100, new ArrayList<String>());

		
		assertEquals(20, finalJSONObject.keySet().size());
		Set<String> finalKeySet = finalJSONObject.keySet();
		
		for (String key : finalKeySet) {
			JSONArray finalJSONArrayForKey = (JSONArray)finalJSONObject.get(key);
			assertEquals(5, finalJSONArrayForKey.size());
			
			for (int i = 0; i < finalJSONArrayForKey.size(); i++) {
				JSONObject jsonObject = (JSONObject)finalJSONArrayForKey.get(i);
				int valueInJSON = ((int)jsonObject.values().toArray()[0]);
				assertTrue(valueInJSON >= Integer.parseInt(key) * 100);
				assertTrue(valueInJSON < (Integer.parseInt(key)+1) * 100);
			}
		}
	}
	
	@Test
	public void testFilterPlacesRandomly__when_only_few_places_per_key() {
		JSONObject initialJSONObject = new JSONObject();
		
		JSONArray jsonArrayForKey1 = new JSONArray();
		for (int i = 0; i < 100; i++) {
			JSONObject jsonObjectForArray = new JSONObject();
			jsonObjectForArray.put(String.valueOf(i), i);
			jsonArrayForKey1.add(jsonObjectForArray);
		}
		initialJSONObject.put("0", jsonArrayForKey1);
		
		JSONArray jsonArrayForKey2 = new JSONArray();
		for (int i = 0; i < 4; i++) {
			JSONObject jsonObjectForArray = new JSONObject();
			jsonObjectForArray.put(String.valueOf(i), i);
			jsonArrayForKey2.add(jsonObjectForArray);
		}
		initialJSONObject.put("1", jsonArrayForKey2);
		
		JSONArray jsonArrayForKey3 = new JSONArray();
		for (int i = 0; i < 4; i++) {
			JSONObject jsonObjectForArray = new JSONObject();
			jsonObjectForArray.put(String.valueOf(i), i);
			jsonArrayForKey3.add(jsonObjectForArray);
		}
		initialJSONObject.put("2", jsonArrayForKey3);
		
		// Let's test now
		
		BoxRouteWorker boxRouteWorker = new BoxRouteWorker();
		JSONObject finalJSONObject = boxRouteWorker.filterPlacesRandomly(initialJSONObject, 100, new ArrayList<String>());
		
		assertEquals(3, finalJSONObject.keySet().size());
		Set<String> finalKeySet = finalJSONObject.keySet();
		
		assertEquals(92, ((JSONArray)finalJSONObject.get("0")).size());
		assertEquals(4, ((JSONArray)finalJSONObject.get("1")).size());
		assertEquals(4, ((JSONArray)finalJSONObject.get("2")).size());
	}
}
