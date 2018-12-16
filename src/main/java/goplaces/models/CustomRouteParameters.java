package goplaces.models;

/**
 * Object to which client messages to get custom routes are deserialized to.
 */

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE) 
// This is to include only the fields we want in the generated JSON

public class CustomRouteParameters {
	private String routeID;
	private ArrayList<String> waypoints;
	
	public CustomRouteParameters(String ID, ArrayList<String> waypoints) {
		this.setWaypoints(waypoints);
		this.setRouteID(ID);
	}

	public CustomRouteParameters() {
	}

	@XmlElement
	public ArrayList<String> getWaypoints() {
		return waypoints;
	}

	public void setWaypoints(ArrayList<String> waypoints) {
		this.waypoints = waypoints;
	}

	@XmlElement
	public String getRouteID() {
		return routeID;
	}

	public void setRouteID(String routeID) {
		this.routeID = routeID;
	}
}
