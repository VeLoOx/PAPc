package pl.pap.activities;

import pl.pap.activities.map.MapActivity;
import pl.pap.client.R;
import pl.pap.dialogs.MarkerDialog;
import pl.pap.model.Route;
import pl.pap.utils.ConnectionGuardian;
import pl.pap.utils.OfflineModeManager;
import pl.pap.utils.SharedPrefsUtils;
import android.location.Location;
import android.os.Bundle;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start_new_route);

		// ===============MAPS
		initializeMap();
		// Set up map methods
		setUpMap();
		setUpListeners();

		route = new Route();
		// ===================PREFS
		prefs = new SharedPrefsUtils(this);
		// ===============Location
		// First we need to check availability of play services
		if (checkPlayServices()) {

			// Building the GoogleApi client
			buildGoogleApiClient();
			// createLocationRequest();
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
	}

	@Override
	protected void onResume() {
		super.onResume();
		checkPlayServices();

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
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.showRouteEditItem).setEnabled(true);
		menu.findItem(R.id.showRouteSaveItem).setVisible(true);
		menu.findItem(R.id.showRouteDeleteItem).setVisible(false);
		return super.onPrepareOptionsMenu(menu);

	}

	private void centerOnUser() {
		getLocation();
		if (lastLocation != null) {
			CameraPosition cameraPosition = new CameraPosition.Builder()
					.target(new LatLng(lastLocation.getLatitude(), lastLocation
							.getLongitude())).zoom(15).build();
			googleMap.animateCamera(CameraUpdateFactory
					.newCameraPosition(cameraPosition));
		}

	}

	public void markPosition(View view) {
		getLocation();
		if (lastLocation != null) {
			double latitude = lastLocation.getLatitude();
			double longitude = lastLocation.getLongitude();
			currentMarker = googleMap.addMarker(new MarkerOptions().position(
					new LatLng(latitude, longitude)).title("New Marker"));
			mDialog = new MarkerDialog(currentMarker);
			FragmentManager fragMan = getSupportFragmentManager();
			mDialog.show(fragMan, "markerDialog");

		} else {
			Toast.makeText(getApplicationContext(), R.string.checkGPS,
					Toast.LENGTH_LONG).show();
		}
	}

	private void showUserLocation() {
		if (googleMap.isMyLocationEnabled()) {
			googleMap.setMyLocationEnabled(false);
			return;
		}
		googleMap.setMyLocationEnabled(true);
		centerOnUser();
	}

	/**
	 * Method to obtain user location
	 * */
	private void getLocation() {
		lastLocation = LocationServices.FusedLocationApi
				.getLastLocation(googleApiClient);

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
	 * Google api callback methods
	 */
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Toast.makeText(
				getApplicationContext(),
				"Connection failed: ConnectionResult.getErrorCode() = "
						+ result.getErrorCode(), Toast.LENGTH_LONG).show();

	}

	@Override
	public void onConnected(Bundle arg0) {
		// Once connected with google api, get the location
		getLocation();
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		googleApiClient.connect();
	}

	@Override
	public void onLocationChanged(Location location) {
		// Assign the new location
		lastLocation = location;
		// Store the new location
		getLocation();
	}

}
