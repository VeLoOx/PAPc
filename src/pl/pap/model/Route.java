package pl.pap.model;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class Route {
	private long ID;
	private String autor;
	private String description;
	private String city;
	//private ArrayList<Marker> markerList = new ArrayList<Marker>();
	//private HashMap<String, Marker> markerMap1 = new HashMap<String, Marker>();
	private HashMap<String, MarkerModel> markerMap = new HashMap<String, MarkerModel>();

	public MarkerOptions convertToMarkerOptions(MarkerModel markerM) {

		return new MarkerOptions().position(markerM.getPosition()).title(
				markerM.getTitle());
	}

	/*public boolean addMarkerToList(Marker marker) {
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
*/
	public HashMap<String, MarkerModel> getMarkerMap() {
		return markerMap;
	}

	public void setMarkerMap(HashMap<String, MarkerModel> markerMap) {
		this.markerMap = markerMap;
	}

	public boolean addMarkerToMap(String id, MarkerModel marker) {
		return (markerMap.put(id, marker)) != null;
	}
	
	public boolean removeMarkerFromMap(String id){
		return (markerMap.remove(id)) != null;
	}

	public Long getId() {
		return ID;
	}

	public void setId(Long ID) {
		this.ID = ID;
	}

	public String getAutor() {
		return autor;
	}

	public void setAutor(String autor) {
		this.autor = autor;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

}
