package goplaces.models;

/**
 * Object to represent a place in the context of a keyword for it.
 */

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE) // This is to include only the fields we want in the generated JSON
public class Waypoint {
	private String keyword;
	private Place place;
	
	public Waypoint(String keyword) {
		this.setKeyword(keyword);
	}

	@XmlElement
	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	@XmlElement
	public Place getPlace() {
		return place;
	}

	public void setPlace(Place place) {
		this.place = place;
	}
}
