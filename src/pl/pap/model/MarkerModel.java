package pl.pap.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class MarkerModel {
	private String markerId;
	private double lat;
	private double lng;
	private String title;
	private String snippet;

	public MarkerModel() {

	}

	public MarkerModel copyValues(Marker mark) {
		this.markerId=mark.getId();
		this.lat=mark.getPosition().latitude;
		this.lng=mark.getPosition().longitude;
		this.title=mark.getTitle();
		this.snippet=mark.getSnippet();
		
		return this;

	}

	public String getMarkerId() {
		return markerId;
	}

	public void setMarkerId(String markerId) {
		this.markerId = markerId;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLng() {
		return lng;
	}

	public void setLng(double lng) {
		this.lng = lng;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSnippet() {
		return snippet;
	}

	public void setSnippet(String snippet) {
		this.snippet = snippet;
	}
	
	public LatLng getPosition(){
		return (new LatLng(this.lat,this.lng));
	}



}
