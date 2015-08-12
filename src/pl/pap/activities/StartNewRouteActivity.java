package pl.pap.activities;

import pl.pap.activities.map.MapActivity;
import pl.pap.client.R;
import pl.pap.dialogs.MarkerDialog;
import pl.pap.dialogs.RouteInfoDialog;
import pl.pap.maps.MapsSettings;
import pl.pap.model.Route;
import pl.pap.utils.ConnectionGuardian;
import pl.pap.utils.OfflineModeManager;
import pl.pap.utils.SharedPrefsUtils;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class StartNewRouteActivity extends MapActivity implements
		ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

	boolean isAuthor = false;
	boolean isEditable = false;

	// Location
	private Location lastLocation;

	// Google client to interact with Google API
	private GoogleApiClient googleApiClient;

	// boolean flag to toggle periodic location updates
	private boolean requestingLocationUpdates = false;

	private LocationRequest locationRequest;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start_new_route);

		// ===============MAPS
		initializeMap();
		// Set up map methods
		maps = new MapsSettings(googleMap);
		googleMap = maps.setUpMap();
		setUpListeners();

		route = new Route();
		// ===================PREFS
		prefs = new SharedPrefsUtils(this);
		// ===============Location
		// First we need to check availability of play services
		if (checkPlayServices()) {

			// Building the GoogleApi client
			buildGoogleApiClient();

			createLocationRequest();
		}

		// Connection
		connGuard = new ConnectionGuardian(getApplicationContext());
		offManager = new OfflineModeManager(this);

		setUpSlideMenu();
		

	}

	@Override
	protected void onStart() {
		super.onStart();
		if (googleApiClient != null) {
			googleApiClient.connect();
		}
		//showUserLocation();
		// centerOnUser();
	}

	@Override
	protected void onResume() {
		super.onResume();

		checkPlayServices();

		// Resuming the periodic location updates
		if (googleApiClient.isConnected() && requestingLocationUpdates) {
			startLocationUpdates();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (googleApiClient.isConnected()) {
			googleApiClient.disconnect();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		stopLocationUpdates();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.show_route_menu, menu);

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

		switch (item.getItemId()) {

		case R.id.showRouteInfoItem:

			// showRouteInfo();
			fillRouteInfo();
			toPersist = false;
			break;
		case R.id.showRouteEditItem:

			isEditable = true;
			break;
		case R.id.showRouteSaveItem:

			toPersist = true;
			fillRouteInfo();
			break;
		case R.id.action_showLocation:
			showUserLocation();
			// centerOnUser();
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// if (isAuthor) {
		// menu.clear();
		menu.findItem(R.id.showRouteEditItem).setEnabled(true);
		// }
		// if (isEditable) {
		menu.findItem(R.id.showRouteSaveItem).setVisible(true);
		menu.findItem(R.id.showRouteDeleteItem).setVisible(false);
		// menu.findItem(R.id.showRouteEditItem).setEnabled(false);
		// }
		return super.onPrepareOptionsMenu(menu);

	}

	private void centerOnUser() {
		lastLocation = LocationServices.FusedLocationApi
				.getLastLocation(googleApiClient);
		System.out.println("Last location :" + lastLocation);
		if (lastLocation != null) {
			CameraPosition cameraPosition = new CameraPosition.Builder()
					.target(new LatLng(lastLocation.getLatitude(), lastLocation
							.getLongitude())).zoom(15).build();
			googleMap.animateCamera(CameraUpdateFactory
					.newCameraPosition(cameraPosition));
		}

	}

	public void markPosition(View view) {
		if (lastLocation != null) {
			double latitude = lastLocation.getLatitude();
			double longitude = lastLocation.getLongitude();
			// locationMarker.setPosition(new LatLng(latitude, longitude));

			/*
			 * if (locationMarker != null) locationMarker.remove();
			 */
			currentMarker = googleMap.addMarker(new MarkerOptions().position(
					new LatLng(latitude, longitude)).title("New Marker"));
			System.out.println("Current position: " + latitude + " "
					+ longitude);
			// addMarker(new LatLng(latitude, longitude));
			mDialog = new MarkerDialog(currentMarker);
			FragmentManager fragMan = getSupportFragmentManager();
			mDialog.show(fragMan, "markerDialog");

		} else {
			Toast.makeText(getApplicationContext(), R.string.checkGPS,
					Toast.LENGTH_LONG).show();
		}
	}

	private void showUserLocation() {
		System.out.println("Location enabled? "
				+ googleMap.isMyLocationEnabled());
		if (googleMap.isMyLocationEnabled()) {
			googleMap.setMyLocationEnabled(false);
			return;
		}
		googleMap.setMyLocationEnabled(true);
		centerOnUser();
		System.out.println("Location enabled? "
				+ googleMap.isMyLocationEnabled());
	}

	/**
	 * Method to display the location on UI
	 * */
	private void displayLocation() {

		lastLocation = LocationServices.FusedLocationApi
				.getLastLocation(googleApiClient);

		if (lastLocation != null) {
			double latitude = lastLocation.getLatitude();
			double longitude = lastLocation.getLongitude();
			// locationMarker.setPosition(new LatLng(latitude,longitude));
			/*
			 * if (locationMarker != null) locationMarker.remove();
			 * locationMarker = googleMap.addMarker(new MarkerOptions()
			 * .position(new LatLng(latitude, longitude)));
			 */
		} else {

		}

	}

	/**
	 * Method to toggle periodic location updates
	 * */
	private void togglePeriodicLocationUpdates() {
		if (!requestingLocationUpdates) {

			requestingLocationUpdates = true;

			// Starting the location updates
			startLocationUpdates();
			googleMap.setMyLocationEnabled(true);

			// Log.d(TAG, "Periodic location updates started!");
			System.out.println("Location updates started");

		} else {

			requestingLocationUpdates = false;

			// Stopping the location updates
			stopLocationUpdates();
			googleMap.setMyLocationEnabled(true);

			// Log.d(TAG, "Periodic location updates stopped!");
			System.out.println("Location updates stopped");
		}
	}

	/**
	 * Creating google api client object
	 * */
	protected synchronized void buildGoogleApiClient() {
		googleApiClient = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(LocationServices.API).build();
	}

	/**
	 * Creating location request object
	 * */
	protected void createLocationRequest() {
		locationRequest = new LocationRequest();
		locationRequest.setInterval(UPDATE_INTERVAL);
		locationRequest.setFastestInterval(FASTEST_INTERVAL);
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		locationRequest.setSmallestDisplacement(DISPLACEMENT);
	}

	/**
	 * Method to verify google play services on the device
	 * */
	private boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, this,
						PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				Toast.makeText(getApplicationContext(),
						"This device is not supported.", Toast.LENGTH_LONG)
						.show();
				finish();
			}
			return false;
		}
		return true;
	}

	/**
	 * Starting the location updates
	 * */
	protected void startLocationUpdates() {

		LocationServices.FusedLocationApi.requestLocationUpdates(
				googleApiClient, locationRequest, this);

	}

	/**
	 * Stopping location updates
	 */
	protected void stopLocationUpdates() {
		LocationServices.FusedLocationApi.removeLocationUpdates(
				googleApiClient, this);
	}

	/**
	 * Google api callback methods
	 */
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		/*
		 * Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " +
		 * result.getErrorCode());
		 */
		Toast.makeText(
				getApplicationContext(),
				"Connection failed: ConnectionResult.getErrorCode() = "
						+ result.getErrorCode(), Toast.LENGTH_LONG).show();

	}

	@Override
	public void onConnected(Bundle arg0) {

		// Once connected with google api, get the location
		displayLocation();

		if (requestingLocationUpdates) {
			startLocationUpdates();
		}
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		googleApiClient.connect();
	}

	@Override
	public void onLocationChanged(Location location) {
		// Assign the new location
		lastLocation = location;

		Toast.makeText(getApplicationContext(), "Location changed!",
				Toast.LENGTH_SHORT).show();

		// Displaying the new location on UI
		displayLocation();
	}

}
