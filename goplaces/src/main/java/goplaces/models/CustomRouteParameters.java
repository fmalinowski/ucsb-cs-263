package goplaces.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE) // This is to include only the fields we want in the generated JSON

public class CustomRouteParameters {
	private String routeID;
	private String[] waypoints;
	
	public CustomRouteParameters(String ID, String[] waypoints) {
		this.setWaypoints(waypoints);
		this.setRouteID(ID);
	}

	public CustomRouteParameters() {
	}

	@XmlElement
	public String[] getWaypoints() {
		return waypoints;
	}

	@XmlElement
	public void setWaypoints(String[] waypoints) {
		for(int i = 0; i < waypoints.length; i++)
			this.waypoints[i] = waypoints[i];
	}

	@XmlElement
	public String getRouteID() {
		return routeID;
	}

	public void setRouteID(String routeID) {
		this.routeID = routeID;
	}
}
