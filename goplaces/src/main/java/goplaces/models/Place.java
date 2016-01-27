package goplaces.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE) // This is to include only the fields we want in the generated JSON
public class Place {
	private String name;
	private String address;
	private float latitude;
	private float longitude;
	private String googlePlaceId;
	
	public Place() {
	}

	public Place(String name, String address, float latitude, float longitude, String googlePlaceId) {
		this.setName(name);
		this.setAddress(address);
		this.setLatitude(latitude);
		this.setLongitude(longitude);
		this.setGooglePlaceId(googlePlaceId);
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
	public float getLatitude() {
		return latitude;
	}

	public void setLatitude(float latitude) {
		this.latitude = latitude;
	}

	@XmlElement
	public float getLongitude() {
		return longitude;
	}

	public void setLongitude(float longitude) {
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
