package goplaces.models;

/**
 * Object to represent real world places.
 */


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE) // This is to include only the fields we want in the generated JSON
public class Place {
	private String name;
	private String address;
	private double latitude;
	private double longitude;
	private String googlePlaceId;
	private String rating;
	private String reviews;

	public Place(String name, String address, double latitude, double longitude, String googlePlaceId) {
		this.setName(name);
		this.setAddress(address);
		this.setLatitude(latitude);
		this.setLongitude(longitude);
		this.setGooglePlaceId(googlePlaceId);
	}

	public Place(String name, String address, double latitude, double longitude, String googlePlaceId, String
			reviews, String rating) {
		this.setName(name);
		this.setAddress(address);
		this.setLatitude(latitude);
		this.setLongitude(longitude);
		this.setGooglePlaceId(googlePlaceId);
		this.setReviews(reviews);
		this.setRating(rating);
	}

	@XmlElement
	public String getReviews() {
		return reviews;
	}

	public void setReviews(String reviews) {
		this.reviews = reviews;
	}

	@XmlElement
	public String getRating() {
		return rating;
	}

	public void setRating(String rating) {
		this.rating = rating;
	}
	
	public Place() {
	}

	@XmlElement
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@XmlElement
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	@XmlElement
	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	@XmlElement
	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	@XmlElement
	public String getGooglePlaceId() {
		return googlePlaceId;
	}

	public void setGooglePlaceId(String googlePlaceId) {
		this.googlePlaceId = googlePlaceId;
	}
	
	public String getGooglePlaceIDFromAddress() {
		return null;
	}
	
	public String getGooglePlaceIDFromCoordinates() {
		return null;
	}
}
