package goplaces.models;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.*;

@XmlRootElement
// JAX-RS supports an automatic mapping from JAXB annotated class to XML and
// JSON
public class Route {
	public enum TransportationMode {
		DRIVING,
		TRANSIT,
		WALKING,
		CYCLING,
		FLIGHTS
	}
	
	private Place origin;
	private Place destination;
	private TransportationMode mode;
	private double duration;
	private double distance;
	private ArrayList<Waypoint> waypoints = new ArrayList<Waypoint>();
	
	public Route() {
	}
	
	public Route (Place origin, Place destination) {
		this.setOrigin(origin);
		this.setDestination(destination);
	}

	public Place getOrigin() {
		return origin;
	}

	public void setOrigin(Place origin) {
		this.origin = origin;
	}

	public Place getDestination() {
		return destination;
	}

	public void setDestination(Place destination) {
		this.destination = destination;
	}

	public TransportationMode getMode() {
		return mode;
	}

	public void setMode(TransportationMode mode) {
		this.mode = mode;
	}

	public double getDuration() {
		return duration;
	}

	public void setDuration(double duration) {
		this.duration = duration;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public ArrayList<Waypoint> getWaypoints() {
		return waypoints;
	}

	public void setWaypoints(ArrayList<Waypoint> waypoints) {
		this.waypoints = waypoints;
	}
}
