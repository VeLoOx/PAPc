package pl.pap.activities;

import java.io.UnsupportedEncodingException;

import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import pl.pap.activities.map.MapActivity;
import pl.pap.client.R;
import pl.pap.dialogs.RouteInfoDialog;
import pl.pap.model.MarkerModel;
import pl.pap.utils.ConnectionGuardian;
import pl.pap.utils.Consts;
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
		setContentView(R.layout.activity_show_route);
		// ===============MAPS
		initializeMap();
		// Set up map methods
		setUpMap();
		setUpListeners();
		// ===================
		prefs = new SharedPrefsUtils(this);
		// Connection
		connGuard = new ConnectionGuardian(getApplicationContext());
		setUpSlideMenu();
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			String value = extras.getString("route");
			showRoute(value);
		}
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
			showUserLocation();

			break;
		default:

		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (isAuthor) {
			menu.findItem(R.id.showRouteEditItem).setEnabled(true);

		}
		if (isEditable) {
			menu.findItem(R.id.showRouteSaveItem).setVisible(true);
			menu.findItem(R.id.showRouteEditItem).setEnabled(false);
			menu.findItem(R.id.showRouteDeleteItem).setVisible(true);
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
			fZoom = calculateZoom((maxLat - lat), (maxLng - lng));
			CameraPosition cameraPosition = new CameraPosition.Builder()
					.target(new LatLng(lat, lng)).zoom(fZoom).build();
			googleMap.animateCamera(CameraUpdateFactory
					.newCameraPosition(cameraPosition));
		}

	}

	private float calculateZoom(double lat, double lng) {
		if (Math.abs(lat) <= 0.002 || Math.abs((lng)) <= 0.002) {
			return 15;
		}

		if (Math.abs(lat) > 0.01 || Math.abs(lng) > 0.01) {
			return 12;
		}

		if (Math.abs(lat) > 0.5 || Math.abs(lng) > 0.5) {
			return 7;
		}

		if (Math.abs(lat) > 1 || Math.abs(lng) > 1) {
			return 5;
		}

		if (Math.abs(lat) > 10 || Math.abs(lng) > 10) {
			return 3;
		}

		if (Math.abs(lat) > 20 || Math.abs(-lng) > 20) {
			return 1;
		}
		return 0;
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
		if (route != null) {
			for (Object value : route.getMarkerMap().values()) {
				googleMap.addMarker(route
						.convertToMarkerOptions((MarkerModel) value));
			}
		} 

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
		restInvokeDelete(params);
	}

	@Override
	public void onSaveRouteDialogPositiveClick(DialogFragment dialog) {
		route.setName(rDialog.routeName);
		route.setCity(rDialog.routeCity);
		route.setDescription(rDialog.routeDescription);
		if (toPersist) {
			if (connGuard.isConnectedToInternet())
				updateRoute();
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

	private void updateRoute() {
		// Fill route object
		route.setAuthor(prefs.getLogin());
		restInvokeUpdate();
	}

	private void restInvokeUpdate() {
		// Make RESTful webservice call using AsyncHttpClient object
		AsyncHttpClient client = new AsyncHttpClient();
		
		StringEntity entity = null;
		try {
			entity = new StringEntity(Utility.convertToJson(route), "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		client.addHeader(Consts.PARAM_LOGIN, prefs.getLogin());
		client.addHeader(Consts.PARAM_SESSIONID, prefs.getSessionID());
		
		client.put(getApplicationContext(),domainAdress + UPDATE_ROUTE, null, entity, Consts.JSON,
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
							if (jO.getBoolean(Consts.MSG_STATUS)) {
								Toast.makeText(getApplicationContext(),
										jO.getString(Consts.MSG_INFO) + StatusCode,
										Toast.LENGTH_LONG).show();
							} else {
								Toast.makeText(
										getApplicationContext(),
										jO.getString(Consts.MSG_INFO)
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
	private void restInvokeDelete(RequestParams params) {
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
										jO.getString(Consts.MSG_INFO), Toast.LENGTH_LONG)
										.show();
								navigateToHomeActivity();

							} else {
								Toast.makeText(getApplicationContext(),
										jO.getString(Consts.MSG_INFO),
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
		if (googleMap.isMyLocationEnabled()) {
			googleMap.setMyLocationEnabled(false);
			return;
		}
		googleMap.setMyLocationEnabled(true);
	}

	/**
	 * Method to display the location on UI
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
		getLocation();

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
		getLocation();
	}

}
