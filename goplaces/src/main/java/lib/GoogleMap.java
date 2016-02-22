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
		// maybe url can be string builder here too?
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

	public static JSONObject getPlaceDetails(String place_id){
		StringBuilder url = new StringBuilder("https://maps.googleapis.com/maps/api/place/details/json?placeid=");
		url.append(place_id + "&key=").append(API_KEY);

		return sendRequest(url.toString());
	}

	public static JSONObject getCustomRoute(Place origin, Place destination, Place[] waypoints){
		StringBuilder url;
		
		System.out.println("GOOGLEMAP origin " + origin.getGooglePlaceId() + " destination " + destination.getGooglePlaceId() + " number of waypoints is " + waypoints.length);

		url = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?origin=place_id:");
		url.append(origin.getGooglePlaceId());
		url.append("&destination=place_id:");
		url.append(destination.getGooglePlaceId());
		url.append("&waypoints=");
		for(Place wp : waypoints)
			url.append(wp.getName() + "|");
		url.append("&key=");
		url.append(API_KEY);

		return sendRequest(url.toString());
	}
	
	public static String getPlacesAroundLocation(Double lat, Double lng, int radius, String type){
		StringBuilder url = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
		url.append("location=");
		url.append(lat);
		url.append(",");
		url.append(lng);
		url.append("&radius=");
		url.append(radius);
		url.append("&keyword=");
		String[] type_spaces = type.split(" ");
		StringBuilder type_plus = new StringBuilder();
		for(String t : type_spaces)
			type_plus.append(t + "+");		
		url.append(type_plus.deleteCharAt(type_plus.length() - 1));
		url.append("&key=");
		url.append(API_KEY);
		
		System.out.println("Query sent: " + url.toString());

		return sendRequest(url.toString()).toString();
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
