package pl.pap.activites;

import org.json.JSONException;
import org.json.JSONObject;

import pl.pap.activites.base.BaseActivity;
import pl.pap.client.R;
import pl.pap.dialogs.DescriptionDialog;
import pl.pap.dialogs.MarkerDialog;
import pl.pap.dialogs.RouteInfoDialog;
import pl.pap.dialogs.SaveRouteDialog;
import pl.pap.maps.MapsMethods;
import pl.pap.maps.MapsSettings;
import pl.pap.model.MarkerModel;
import pl.pap.model.Route;
import pl.pap.utils.ConnectionGuardian;
import pl.pap.utils.Consts;
import pl.pap.utils.SharedPrefsUtils;
import pl.pap.utils.Utility;
import android.content.Intent;
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
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class StartNewRouteActivity extends BaseActivity implements
		OnMapLongClickListener, OnMapClickListener, OnMarkerDragListener,
		OnMarkerClickListener, MarkerDialog.MarkerDialogListener,
		RouteInfoDialog.RouteInfoDialogListener,
		SaveRouteDialog.SaveRouteDialogListener, MapsMethods, Consts,
		ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

	// Maps
	private GoogleMap googleMap;
	Marker currentMarker;
	MapsSettings maps;
	Route route;// = new Route();
	// Dialogs
	DescriptionDialog dDialog = new DescriptionDialog();
	MarkerDialog mDialog = new MarkerDialog(currentMarker);
	SaveRouteDialog rDialog;
	RouteInfoDialog iDialog;
	// Utils
	JSONObject jsonMarker;
	SharedPrefsUtils prefs;
	ConnectionGuardian connGuard;

	boolean isAuthor = false;
	boolean isEditable = false;
	boolean toPersist = false;

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
		route = new Route();
		// ===============MAPS
		initializeMap();
		// Set up map methods
		maps = new MapsSettings(googleMap);
		googleMap = maps.setUpMap();
		setUpListeners();
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

		setUpSlideMenu();

	}

	@Override
	protected void onStart() {
		super.onStart();
		if (googleApiClient != null) {
			googleApiClient.connect();
		}
		centerOnUser();
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

			// togglePeriodicLocationUpdates();
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

	private void fillRouteInfo() {
		rDialog = new SaveRouteDialog(route);
		FragmentManager fragMan = getSupportFragmentManager();
		rDialog.show(fragMan, "saveRouteDialog");
	}

	@Override
	public boolean onMarkerClick(Marker mark) {
		if (isEditable) {
			deleteMarker();
			System.out.println(mark.getPosition());
			currentMarker = mark;
			mDialog = new MarkerDialog(currentMarker);
			FragmentManager fragMan = getSupportFragmentManager();
			mDialog.show(fragMan, "markerDialog");
		}
		return false;
	}

	@Override
	public void onMapClick(LatLng arg0) {
		/*
		 * googleMap.animateCamera(CameraUpdateFactory.newLatLng(arg0)); if
		 * (mDialog.isDeletable) { currentMarker.remove(); }
		 */
		deleteMarker();
	}

	@Override
	public void onMapLongClick(LatLng point) {
		if (isEditable)
			addMarker(point);
	}

	@Override
	public void onMarkerDrag(Marker arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMarkerDragStart(Marker arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMarkerDragEnd(Marker mark) {
		if (isEditable) {
			System.out.println("Marker drag end");
			currentMarker = mark;
			updateMarkers(currentMarker);
		}
	}

	@Override
	public void initializeMap() {
		if (googleMap == null) {
			googleMap = ((MapFragment) getFragmentManager().findFragmentById(
					R.id.map)).getMap();

			// check if map is created successfully or not
			if (googleMap == null) {
				Toast.makeText(getApplicationContext(),
						R.string.unnableToCreateMap, Toast.LENGTH_SHORT).show();
			}
		}
		

	}

	@Override
	public void setUpListeners() {
		googleMap.setOnMapClickListener(this);
		googleMap.setOnMapLongClickListener(this);
		googleMap.setOnMarkerClickListener(this);
		googleMap.setOnMarkerDragListener(this);

	}

	@Override
	public void addMarker(LatLng point) {
		MarkerOptions marker = new MarkerOptions().position(
				new LatLng(point.latitude, point.longitude))
				.title("New Marker");
		marker.draggable(true);
		Marker tmp = googleMap.addMarker(marker);
		persistMarker(tmp);
		System.out.println("Marker added " + point.latitude + "---"
				+ point.longitude);
	}

	private void persistMarker(Marker marker) {
		MarkerModel markModel = new MarkerModel();
		markModel.copyValues(marker);
		route.addMarkerToMap(markModel.getMarkerId(), markModel);
	}

	private void centerOnUser() {
		lastLocation = LocationServices.FusedLocationApi
				.getLastLocation(googleApiClient);
		System.out.println("Last location :" + lastLocation);
		showUserLocation();
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

	@Override
	public void deleteMarker() {
		if (mDialog.isDeletable) {
			currentMarker.remove();
			route.getMarkerMap().remove(currentMarker.getId());
		}
	}

	@Override
	public boolean updateMarkers(Marker marker) {
		/*
		 * System.out.println("Marker remove and update status: " +
		 * route.removeMarkerFromMap(marker.getId()));
		 */
		persistMarker(marker);
		System.out.println("Marker updated");
		return true;

	}

	private void saveRoute() {
		if (!connGuard.isConnectedToInternet()) {
			Toast.makeText(getApplicationContext(), R.string.notConnected,
					Toast.LENGTH_LONG).show();
			return;
		}
		// Fill route object
		route.setAuthor(prefs.getLogin());
		// Instantiate Http Request Param Object
		RequestParams params = new RequestParams();
		params.put("login", prefs.getLogin());
		params.put("sessionId", prefs.getSessionID());
		// params.put("route", convertToJson());
		params.put("route", Utility.convertToJson(route));
		restInvoke(params);
	}

	@Override
	public void onMarkerDialogPositiveClick(DialogFragment dialog) {
		currentMarker.setTitle(mDialog.markerTitle);
		currentMarker.setSnippet(mDialog.markerSnippet);
		updateMarkers(currentMarker);

	}

	@Override
	public void onMarkerDialogNegativeClick(DialogFragment dialog) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRouteInfoDialogPositiveClick(DialogFragment dialog) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSaveRouteDialogPositiveClick(DialogFragment dialog) {
		route.setName(rDialog.routeName);
		route.setCity(rDialog.routeCity);
		route.setDescription(rDialog.routeDescription);
		if (toPersist) {
			// saveRoute();
			// navigateToHomeActivity();
			System.out.println("Route is about to be updated");
			saveRoute();

		}

	}

	@Override
	public void onSaveRouteDialogNegativeClick(DialogFragment dialog) {
		// TODO Auto-generated method stub

	}

	private void restInvoke(RequestParams params) {
		// Show Progress Dialog
		// prgDialog.show();
		// Make RESTful webservice call using AsyncHttpClient object
		// System.out.println("PlanRouteActivity: Inside restInvoke");
		AsyncHttpClient client = new AsyncHttpClient();
		client.get(domainAdress + PERSIST_ROUTE, params,
				new AsyncHttpResponseHandler() {
					// When the response returned by REST has Http response code
					// '200'
					@Override
					public void onSuccess(int StatusCode, String answer) {
						// Hide Progress Dialog
						// prgDialog.hide();
						try {
							// JSON Object
							JSONObject jO = new JSONObject(answer);
							// When the JSON response has status boolean value
							// assigned with true
							if (jO.getBoolean("status")) {
								Toast.makeText(getApplicationContext(),
										jO.getString("data") + StatusCode,
										Toast.LENGTH_LONG).show();
								navigateToHomeActivity();
							} else {
								Toast.makeText(
										getApplicationContext(),
										jO.getString("errorMessage")
												+ StatusCode, Toast.LENGTH_LONG)
										.show();
							}
						} catch (JSONException e) {
							Toast.makeText(getApplicationContext(),
									R.string.invalidJSON, Toast.LENGTH_LONG)
									.show();
							e.printStackTrace();

						}
					}

					// When the response returned by REST has Http response code
					// other than '200'
					@Override
					public void onFailure(int statusCode, Throwable error,
							String content) {
						// Hide Progress Dialog
						// prgDialog.hide();
						// When Http response code is '404'
						if (statusCode == 404) {
							Toast.makeText(getApplicationContext(),
									R.string.err404, Toast.LENGTH_LONG).show();
						}
						// When Http response code is '500'
						else if (statusCode == 500) {
							Toast.makeText(getApplicationContext(),
									R.string.err500, Toast.LENGTH_LONG).show();
						}
						// When Http response code other than 404, 500
						else {
							Toast.makeText(getApplicationContext(),
									R.string.otherErr, Toast.LENGTH_LONG)
									.show();
						}
					}
				});
	}

	private void showUserLocation() {
		System.out.println("Location enabled? "
				+ googleMap.isMyLocationEnabled());
		if (googleMap.isMyLocationEnabled()) {
			googleMap.setMyLocationEnabled(false);
			return;
		}
		googleMap.setMyLocationEnabled(true);
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
