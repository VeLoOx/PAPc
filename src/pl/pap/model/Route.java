package pl.pap.model;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class Route {
	Long id;
	String autor;
	String description;
	String city;
	private ArrayList<Marker> markerList = new ArrayList<Marker>();
	private HashMap<String, Marker> markerMap = new HashMap<String, Marker>();

	public MarkerOptions convertToMarkerOptions(Marker marker) {

		return new MarkerOptions().position(marker.getPosition()).title(
				marker.getTitle());
	}

	public boolean addMarkerToList(Marker marker) {
		return (markerList.add(marker));
	}

	public boolean removeMarkerFromList(Marker marker) {
		for (Marker mark : markerList) {
			if (mark.getId() == marker.getId()) {
				mark.remove();
				return true;
			}
		}
		return false;

	}

	public ArrayList<Marker> getMarkerList() {
		return markerList;
	}

	public void setMarkerList(ArrayList<Marker> markerList) {
		this.markerList = markerList;
	}

	public HashMap<String, Marker> getMarkerMap() {
		return markerMap;
	}

	public void setMarkerMap(HashMap<String, Marker> markerMap) {
		this.markerMap = markerMap;
	}

	public boolean addMarkerToMap(String id, Marker marker) {
		return (markerMap.put(id, marker)) != null;
	}
	
	public boolean removeMarkerFromMap(String id){
		return (markerMap.remove(id)) != null;
	}

}
