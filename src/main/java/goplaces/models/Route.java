package goplaces.models;

/**
 * Class to represent real world routes.
 */

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import com.google.appengine.api.datastore.Text;

import java.util.*;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE) // This is to include only the fields we want in the generated JSON
public class Route {
	public enum TransportationMode {
		DRIVING,
		TRANSIT,
		WALKING,
		CYCLING,
		FLIGHTS
	}
	
	private long id;
	private Place origin;
	private Place destination;
	private TransportationMode mode;
	private int duration;
	private int distance;

	// should the memory creation not happen inside the set waypoints method?
	
	private ArrayList<Waypoint> waypoints = new ArrayList<Waypoint>(); 
	private Text mapJsonAsText;

	public Route() {
	}
	
	public Route (Place origin, Place destination) {
		this.setOrigin(origin);
		this.setDestination(destination);
	}

	@XmlElement
	public Text getMapJsonAsText(){
		return mapJsonAsText;
	}

	public void setMapJsonAsText(Text mapJsonAsText){
		this.mapJsonAsText = new Text(mapJsonAsText.getValue());
	}

	@XmlElement
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@XmlElement
	public Place getOrigin() {
		return origin;
	}

	public void setOrigin(Place origin) {
		this.origin = origin;
	}

	@XmlElement
	public Place getDestination() {
		return destination;
	}

	public void setDestination(Place destination) {
		this.destination = destination;
	}

	@XmlElement
	public TransportationMode getMode() {
		return mode;
	}

	public void setMode(TransportationMode mode) {
		this.mode = mode;
	}

	@XmlElement
	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	@XmlElement
	public int getDistance() {
		return distance;
	}

	public void setDistance(int distance) {
		this.distance = distance;
	}

	@XmlElement
	public ArrayList<Waypoint> getWaypoints() {
		return waypoints;
	}

	public void setWaypoints(ArrayList<Waypoint> waypoints) {
		this.waypoints = waypoints;
	}
}
