package lib;

import goplaces.models.Place;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONObject;

import java.util.logging.Logger;

public class GoogleMap {
	private static final String API_KEY = "AIzaSyDJBrd2qfr_f-U91N50-0RRBQl0r2kHIUo";
	private static final Logger log = Logger.getLogger(GoogleMap.class.getName());
	
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
	
	public static String getPlacesAroundLocation(Double lat, Double lng, Double radius, String type){
		String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
		url += "location=";
		url += lat;
		url += ",";
		url += lng;
		url += "&radius=";
		url += radius;
		url += "&type=";
		String[] type_spaces = type.split(" ");
		StringBuilder type_plus = new StringBuilder();
		for(String t : type_spaces)
			type_plus.append(t + "+");		
		url += type_plus.deleteCharAt(type_plus.length() - 1);
		url += "&key=";
		url += API_KEY;

		return sendRequest(url).toString();
	}

	private static JSONObject sendRequest(String urlString) {
		String json = "";
		
		URL url;
		try {
			url = new URL(urlString);
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
		    String line;

		    while ((line = reader.readLine()) != null) {
		    	json += line;
		    }
		    reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
//		log.warning("JSON from GoogleMAP Api: " + json);
		return new JSONObject(json);
	}
	
	private static String getPlaceStringForDirectionQuery(Place place) {
		String placeString = "";
		
		if (place.getAddress() != null && !place.getAddress().isEmpty()) {
			try {
				placeString = URLEncoder.encode(place.getAddress(), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		} else {
			placeString = String.valueOf(place.getLatitude()) + "," + String.valueOf(place.getLongitude());
		}
		
		return placeString;
	}
}
