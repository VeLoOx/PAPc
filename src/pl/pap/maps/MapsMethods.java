package pl.pap.maps;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public interface MapsMethods {
	void initializeMap();
	public void setUpListeners();
	public void addMarker(LatLng point);
	public void deleteMarker();
	public boolean updateMarkers(Marker marker);
}
