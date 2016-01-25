package goplaces.models;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Place {
	private String name;
	private double latitude;
	private double longitude;
	private String googlePlaceId;
	
	public Place() {
	}

	public Place(String name, double latitude, double longitude, String googlePlaceId) {
		this.setName(name);
		this.setLatitude(latitude);
		this.setLongitude(longitude);
		this.setGooglePlaceId(googlePlaceId);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public String getGooglePlaceId() {
		return googlePlaceId;
	}

	public void setGooglePlaceId(String googlePlaceId) {
		this.googlePlaceId = googlePlaceId;
	}
}
