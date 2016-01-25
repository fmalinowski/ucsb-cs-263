package goplaces.models;

public class Waypoint {
	private String keyword;
	private Place place;
	
	public Waypoint(String keyword) {
		this.setKeyword(keyword);
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public Place getPlace() {
		return place;
	}

	public void setPlace(Place place) {
		this.place = place;
	}
}
