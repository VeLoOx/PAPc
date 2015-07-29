package pl.pap.activites;

import pl.pap.client.R;
import pl.pap.dialogs.MarkerDialog;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class StartNewRouteActivity extends FragmentActivity implements
		OnMapLongClickListener, OnMapClickListener, OnMarkerDragListener,
		OnMarkerClickListener, MarkerDialog.MarkerDialogListener {

	private GoogleMap googleMap;
	MarkerDialog mDialog = new MarkerDialog();
	Marker currentMarker;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.start_new_route);
		initilizeMap();
		setUpMap();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.map_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * function to load map If map is not created it will create it for you
	 * */
	@SuppressLint("NewApi")
	private void initilizeMap() {
		if (googleMap == null) {
			googleMap = ((MapFragment) getFragmentManager().findFragmentById(
					R.id.map)).getMap();

			// check if map is created successfully or not
			if (googleMap == null) {
				Toast.makeText(getApplicationContext(),
						"Sorry! unable to create maps", Toast.LENGTH_SHORT)
						.show();
			}
		}
	}
	
	private void setUpMap() {

		// Map listeners
		googleMap.setOnMapClickListener(this);
		googleMap.setOnMapLongClickListener(this);
		googleMap.setOnMarkerClickListener(this);

		// Changing map type
		googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		// googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		// googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
		// googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
		// googleMap.setMapType(GoogleMap.MAP_TYPE_NONE);

		// Showing / hiding your current location
		googleMap.setMyLocationEnabled(true);

		// Enable / Disable zooming controls
		googleMap.getUiSettings().setZoomControlsEnabled(false);

		// Enable / Disable my location button
		googleMap.getUiSettings().setMyLocationButtonEnabled(true);

		// Enable / Disable Compass icon
		googleMap.getUiSettings().setCompassEnabled(true);

		// Enable / Disable Rotate gesture
		googleMap.getUiSettings().setRotateGesturesEnabled(true);

		// Enable / Disable zooming functionality
		googleMap.getUiSettings().setZoomGesturesEnabled(true);

		double latitude = 50.862357054416364;
		double longitude = 20.618173219263554;

		CameraPosition cameraPosition = new CameraPosition.Builder()
				.target(new LatLng(latitude, longitude)).zoom(15).build();

		googleMap.animateCamera(CameraUpdateFactory
				.newCameraPosition(cameraPosition));
	}
	
	public void addMarker(View view){
		FragmentManager fragMan = getSupportFragmentManager();
		mDialog.show(fragMan, "markerDialog");
	}
	
	private void deleteMarker(){
		if (mDialog.isDeletable) {
			currentMarker.remove();
		}
	}

	@Override
	public boolean onMarkerClick(Marker mark) {
		deleteMarker();
		System.out.println(mark.getPosition());
		mDialog = new MarkerDialog();
		currentMarker = mark;
		FragmentManager fragMan = getSupportFragmentManager();
		mDialog.show(fragMan, "markerDialog");
		return false;
	}

	@Override
	public void onMarkerDrag(Marker arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMarkerDragEnd(Marker arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMarkerDragStart(Marker arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMapClick(LatLng arg0) {
		googleMap.animateCamera(CameraUpdateFactory.newLatLng(arg0));
		if (mDialog.isDeletable) {
			currentMarker.remove();
		}
		
	}

	@Override
	public void onMapLongClick(LatLng point) {
		MarkerOptions marker = new MarkerOptions().position(
				new LatLng(point.latitude, point.longitude))
				.title("New Marker");
		marker.draggable(true);
		googleMap.addMarker(marker);
		System.out.println(point.latitude + "---" + point.longitude);
		
	}

	@Override
	public void onMarkerDialogPositiveClick(DialogFragment dialog) {
		currentMarker.setTitle(mDialog.markerTitle);
		
	}

	@Override
	public void onMarkerDialogNegativeClick(DialogFragment dialog) {
		// TODO Auto-generated method stub
		
	}

}
