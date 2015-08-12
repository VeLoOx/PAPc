package pl.pap.activities;

import org.json.JSONException;
import org.json.JSONObject;

import pl.pap.activities.map.MapActivity;
import pl.pap.client.R;
import pl.pap.dialogs.RouteInfoDialog;
import pl.pap.maps.MapsSettings;
import pl.pap.model.MarkerModel;
import pl.pap.utils.ConnectionGuardian;
import pl.pap.utils.SharedPrefsUtils;
import pl.pap.utils.Utility;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;
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
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import static java.lang.Math.*;

public class ShowRouteActivity extends MapActivity implements
		RouteInfoDialog.RouteInfoDialogListener, ConnectionCallbacks,
		OnConnectionFailedListener, LocationListener {

	// Dialogs
	RouteInfoDialog iDialog;

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
		System.out.println("ShowRoute on create");
		setContentView(R.layout.activity_show_route);
		// ===============MAPS
		initializeMap();
		// Set up map methods
		maps = new MapsSettings(googleMap);
		googleMap = maps.setUpMap();
		setUpListeners();
		// ===================
		prefs = new SharedPrefsUtils(this);
		// Connection
		connGuard = new ConnectionGuardian(getApplicationContext());
		// ===============Location
		// First we need to check availability of play services
		if (checkPlayServices()) {

			// Building the GoogleApi client
			buildGoogleApiClient();
			createLocationRequest();
		}
		setUpSlideMenu();

		Bundle extras = getIntent().getExtras();

		// if (extras != null) {
		// String value = extras.getString("routeId");
		// System.out.println("Route extraction: id " + value);
		// requestRoute(value);
		// }

		if (extras != null) {
			String value = extras.getString("route");
			System.out.println("Route extraction: " + value);
			showRoute(value);
		}

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

			showRouteInfo();
			break;
		case R.id.showRouteEditItem:

			isEditable = true;
			setUpListeners();
			break;
		case R.id.showRouteSaveItem:

			toPersist = true;
			fillRouteInfo();
			break;
		case R.id.showRouteDeleteItem:

			deciedeToDelete();
			// removeRoute();
			break;
		case R.id.action_showLocation:

			// togglePeriodicLocationUpdates();
			showUserLocation();

			break;
		default:

		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (isAuthor) {
			// menu.clear();
			menu.findItem(R.id.showRouteEditItem).setEnabled(true);
		}
		if (isEditable) {
			menu.findItem(R.id.showRouteSaveItem).setVisible(true);
			menu.findItem(R.id.showRouteEditItem).setEnabled(false);
		}
		return super.onPrepareOptionsMenu(menu);

	}

	@Override
	public void setUpListeners() {
		if (isEditable) {
			googleMap.setOnMapClickListener(this);
			googleMap.setOnMapLongClickListener(this);
			googleMap.setOnMarkerClickListener(this);
			googleMap.setOnMarkerDragListener(this);
		}

	}

	private void centerOnRoute() {
		double lat = 0.0;
		double lng = 0.0;
		double maxLat = 0.0;
		double maxLng = 0.0;
		float fZoom = 0;
		int size = 0;

		if (route != null) {
			size = route.getMarkerMap().size();
			for (MarkerModel value : route.getMarkerMap().values()) {
				lat += value.getLat();
				lng += value.getLng();
				if (Math.abs(value.getLat()) > maxLat) {
					maxLat = value.getLat();
				}
				if (Math.abs(value.getLng()) > maxLng) {
					maxLng = value.getLng();
				}
			}

		}

		if (size > 0) {

			lat = lat / route.getMarkerMap().size();
			lng = lng / route.getMarkerMap().size();
			System.out.println("middle point " + lat + " " + lng);
			System.out.println("Max Min Values" + (maxLat - lat) + " "
					+ (maxLng - lng));

			if (Math.abs((maxLat - lat)) <= 0.002
					|| Math.abs((maxLng - lng)) <= 0.002) {
				fZoom = 15;
			}

			if (Math.abs((maxLat - lat)) > 0.01
					|| Math.abs((maxLng - lng)) > 0.01) {
				fZoom = 12;
			}

			if (Math.abs((maxLat - lat)) > 0.5
					|| Math.abs((maxLng - lng)) > 0.5) {
				fZoom = 7;
			}

			if (Math.abs((maxLat - lat)) > 1 || Math.abs((maxLng - lng)) > 1) {
				fZoom = 5;
			}

			if (Math.abs((maxLat - lat)) > 10 || Math.abs((maxLng - lng)) > 10) {
				fZoom = 3;
			}

			if (Math.abs((maxLat - lat)) > 20 || Math.abs((maxLng - lng)) > 20) {
				fZoom = 1;
			}

			System.out.println("Setting map zoom to: " + fZoom);
			CameraPosition cameraPosition = new CameraPosition.Builder()
					.target(new LatLng(lat, lng)).zoom(fZoom).build();
			googleMap.animateCamera(CameraUpdateFactory
					.newCameraPosition(cameraPosition));
		}

	}

	private void requestRoute(String routeId) {
		System.out.println("Requet route");
		RequestParams params = new RequestParams();
		params.put("login", prefs.getLogin());
		params.put("sessionId", prefs.getSessionID());
		params.put("autor", prefs.getLogin());
		params.put("id", routeId);
		restInvokeRequest(params);
	}

	private void showRoute(String json) {
		route = Utility.convertRouteFromJson(json);

		if (route.getAuthor().equals(prefs.getLogin())) {
			isAuthor = true;
		}
		fillMap();
		centerOnRoute();
	}

	private void fillMap() {
		System.out.println("Fill map");
		if (route != null) {
			System.out.println("Marker map size " + route.getMarkerMap());
			for (Object value : route.getMarkerMap().values()) {
				googleMap.addMarker(route
						.convertToMarkerOptions((MarkerModel) value));
			}
			// centerOnRoute();
		} else
			System.out.println("Route empty");
	}

	private void updateRoute() {
		// Fill route object
		route.setAuthor(prefs.getLogin());
		// Instantiate Http Request Param Object
		RequestParams params = new RequestParams();
		params.put("login", prefs.getLogin());
		params.put("sessionId", prefs.getSessionID());
		// params.put("route", convertToJson());
		params.put("route", Utility.convertToJson(route));
		restInvokeUpdate(params);
	}

	private void showRouteInfo() {
		iDialog = new RouteInfoDialog(route);
		FragmentManager fragMan = getSupportFragmentManager();
		iDialog.show(fragMan, "routeInfoDialog");
	}

	private void deciedeToDelete() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(
				ShowRouteActivity.this);

		// Setting Dialog Title
		alertDialog.setTitle(R.string.wantDeleteTitle);

		// Setting Dialog Message
		alertDialog.setMessage(R.string.wantDeleteDesc);

		// Setting Icon to Dialog
		alertDialog.setIcon(R.drawable.ic_cancel);

		// Setting Positive "Yes" Button
		alertDialog.setPositiveButton(R.string.yes,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {

						removeRoute();
						Toast.makeText(getApplicationContext(),
								"You clicked on YES", Toast.LENGTH_SHORT)
								.show();
					}
				});

		// Setting Negative "NO" Button
		alertDialog.setNegativeButton(R.string.no,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {

						Toast.makeText(getApplicationContext(),
								"You clicked on NO", Toast.LENGTH_SHORT).show();
						dialog.cancel();
					}
				});

		// Showing Alert Message
		alertDialog.show();
	}

	private void removeRoute() {
		// Instantiate Http Request Param Object
		RequestParams params = new RequestParams();
		params.put("login", prefs.getLogin());
		params.put("sessionId", prefs.getSessionID());
		params.put("id", route.getId().toString());
		System.out.println("removeRoute delete: id " + route.getId());
		restInvokeDelete(params);
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
			if (connGuard.isConnectedToInternet())
				updateRoute();
			// navigateToHomeActivity();
		}

	}

	@Override
	public void onSaveRouteDialogNegativeClick(DialogFragment dialog) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRouteInfoDialogPositiveClick(DialogFragment dialog) {
		// TODO Auto-generated method stub

	}

	private void restInvokeUpdate(RequestParams params) {
		// Show Progress Dialog
		// prgDialog.show();
		// Make RESTful webservice call using AsyncHttpClient object
		AsyncHttpClient client = new AsyncHttpClient();
		client.get(domainAdress + UPDATE_ROUTE, params,
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

	private void restInvokeRequest(RequestParams params) {
		// Show Progress Dialog
		// prgDialog.show();
		// Make RESTful webservice call using AsyncHttpClient object
		System.out.println("RestInvokeRequest");
		AsyncHttpClient client = new AsyncHttpClient();
		client.get(domainAdress + REQUEST_ROUTE, params,
				new AsyncHttpResponseHandler() {
					// When the response returned by REST has Http response code
					// '200'
					@Override
					public void onSuccess(int StatusCode, String answer) {
						System.out.println("Request succes");
						try {
							JSONObject jO = new JSONObject(answer);
							if (jO.getBoolean("status")) {
								// convertFromJson(jO.getString("data"));
								route = Utility.convertRouteFromJson(jO
										.getString("data"));

								if (route.getAuthor().equals(prefs.getLogin())) {
									isAuthor = true;
								}
								fillMap();
								centerOnRoute();
							} else {
								Toast.makeText(getApplicationContext(),
										jO.getString("errorMessage"),
										Toast.LENGTH_LONG).show();
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
						System.out.println("Request failure");
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

	private void restInvokeDelete(RequestParams params) {
		// Show Progress Dialog
		// prgDialog.show();
		// Make RESTful webservice call using AsyncHttpClient object
		AsyncHttpClient client = new AsyncHttpClient();
		client.delete(getApplicationContext(), domainAdress + DELETE_ROUTE,
				null, params, new AsyncHttpResponseHandler() {
					// When the response returned by REST has Http response code
					// '200'
					@Override
					public void onSuccess(int StatusCode, String answer) {
						try {
							JSONObject jO = new JSONObject(answer);
							if (jO.getBoolean("status")) {
								Toast.makeText(getApplicationContext(),
										jO.getString("data"), Toast.LENGTH_LONG)
										.show();
								navigateToHomeActivity();

							} else {
								Toast.makeText(getApplicationContext(),
										jO.getString("errorMessage"),
										Toast.LENGTH_LONG).show();
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
