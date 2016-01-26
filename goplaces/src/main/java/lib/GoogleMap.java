package lib;

import goplaces.models.Place;

import java.io.IOException;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

public class GoogleMap {
	private static final String API_KEY = "REPLACE YOU KEY HERE";
	
	public static JSONObject getDirections(Place origin, Place destination) {
		String url, originString, destinationString;
		
		originString = getPlaceStringForDirectionQuery(origin);
		destinationString = getPlaceStringForDirectionQuery(destination);
		url = "https://maps.googleapis.com/maps/api/directions/json?origin=";
		url += originString;
		url += "&destination=";
		url += destinationString;
		url += "&key=";
		url += API_KEY;
		
		return sendRequest(url);
	}
	
	private static JSONObject sendRequest(String url) {
		String json = "";
		
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		HttpPost request = new HttpPost(url);
//        StringEntity params = new StringEntity(query);
        request.addHeader("content-type", "application/json");
//        request.setEntity(params);
        
        HttpResponse result;
		try {
			result = httpClient.execute(request);
			json = EntityUtils.toString(result.getEntity(), "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
        
		return new JSONObject(json);
	}
	
	private static String getPlaceStringForDirectionQuery(Place place) {
		String placeString;
		
		if (place.getAddress() != null && !place.getAddress().isEmpty()) {
			placeString = StringEscapeUtils.escapeHtml4(place.getAddress());
		} else {
			placeString = String.valueOf(place.getLatitude()) + "," + String.valueOf(place.getLongitude());
		}
		
		return placeString;
	}
}
