package goplaces.models;

/**
 * This object is used as an exchange message between SelectWaypointsResource the background task boxrouteworker.
 */

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE) // This is to include only the fields we want in the generated JSON
public class CustomizeRouteQuery {
	private String[] keywords;
	private int radius;
	private String routeID;
	
	public CustomizeRouteQuery(String routeID, String[] keywords, int radius) {
		this.setKeywords(keywords);
		this.setRouteID(routeID);
		this.setRadius(radius);
	}

	public CustomizeRouteQuery() {
	}

	@XmlElement
	public int getRadius() {
		return radius;
	}

	@XmlElement
	public void setRadius(int radius) {
		this.radius = radius;
	}

	@XmlElement
	public String[] getKeywords() {
		return keywords;
	}

	public void setKeywords(String[] keywords) {
		this.keywords = new String[keywords.length];
		int i = 0;

		for(String keyword : keywords)
			this.keywords[i++] = keyword;
	}

	@XmlElement
	public String getRouteID() {
		return routeID;
	}

	public void setRouteID(String routeID) {
		this.routeID = routeID;
	}
}
