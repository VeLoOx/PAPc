package pl.pap.activities.map;

import org.json.JSONException;
import org.json.JSONObject;

import pl.pap.activities.base.BaseActivity;
import pl.pap.client.R;
import pl.pap.dialogs.MarkerDialog;
import pl.pap.dialogs.SaveRouteDialog;
import pl.pap.maps.MapsMethods;
import pl.pap.maps.MapsSettings;
import pl.pap.model.MarkerModel;
import pl.pap.model.Route;
import pl.pap.utils.ConnectionGuardian;
import pl.pap.utils.Consts;
import pl.pap.utils.OfflineModeManager;
import pl.pap.utils.SharedPrefsUtils;
import pl.pap.utils.Utility;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class MapActivity extends BaseActivity implements
		OnMapLongClickListener, OnMapClickListener, OnMarkerDragListener,
		OnMarkerClickListener, MarkerDialog.MarkerDialogListener,
		SaveRouteDialog.SaveRouteDialogListener, MapsMethods, Consts {

	// Google Map
	protected GoogleMap googleMap;
	protected Marker currentMarker;
	protected MapsSettings maps;
	protected Route route;// = new Route();

	// Dialogs
	protected MarkerDialog mDialog = new MarkerDialog(currentMarker);
	protected SaveRouteDialog rDialog;// = new SaveRouteDialog();

	// Utils
	protected JSONObject jsonMarker;
	protected SharedPrefsUtils prefs;
	protected ConnectionGuardian connGuard;
	protected OfflineModeManager offManager;

	protected boolean toPersist = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	/**
	 * function to load map If map is not created it will create it for you
	 * */
	@Override
	@SuppressLint("NewApi")
	public void initializeMap() {
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

	@Override
	public void setUpListeners() {
		googleMap.setOnMapClickListener(this);
		googleMap.setOnMapLongClickListener(this);
		googleMap.setOnMarkerClickListener(this);
		googleMap.setOnMarkerDragListener(this);

	}

	protected void fillRouteInfo() {
		rDialog = new SaveRouteDialog(route);
		FragmentManager fragMan = getSupportFragmentManager();
		rDialog.show(fragMan, "saveRouteDialog");
	}



	protected void cleanMap() {
		googleMap.clear();
		route = new Route();

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

	@Override
	public void deleteMarker() {
		if (mDialog.isDeletable) {
			currentMarker.remove();
			route.getMarkerMap().remove(currentMarker.getId());
		}

	}

	private void persistMarker(Marker marker) {
		MarkerModel markModel = new MarkerModel();
		markModel.copyValues(marker);
		route.addMarkerToMap(markModel.getMarkerId(), markModel);
	}

	@Override
	public boolean updateMarkers(Marker marker) {
		System.out.println("Marker remove and update status: "
				+ route.removeMarkerFromMap(marker.getId()));
		persistMarker(marker);
		System.out.println("Marker updated");
		return true;

	}

	protected void saveRoute() {
		route.setAuthor(prefs.getLogin());
		if (connGuard.isConnectedToInternet()) {
			RequestParams params = new RequestParams();
			params.put("login", prefs.getLogin());
			params.put("sessionId", prefs.getSessionID());
			params.put("route", Utility.convertToJson(route));
			restInvoke(params);
		} else {
			/*
			 * Toast.makeText(getApplicationContext(), R.string.notConnected,
			 * Toast.LENGTH_LONG).show();
			 */
			offManager.saveRoute(Utility.convertToJson(route));
		}
	}

	// LISTENERS
	@Override
	public boolean onMarkerClick(Marker mark) {
		deleteMarker();
		System.out.println(mark.getPosition());
		currentMarker = mark;
		mDialog = new MarkerDialog(currentMarker);
		FragmentManager fragMan = getSupportFragmentManager();
		mDialog.show(fragMan, "markerDialog");
		return false;
	}

	@Override
	public void onMapClick(LatLng arg0) {
		System.out.println("Map clicked");
		// maps.googleMap.animateCamera(CameraUpdateFactory.newLatLng(arg0));
		deleteMarker();
	}

	@Override
	public void onMapLongClick(LatLng point) {
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
		System.out.println("Marker drag end");
		currentMarker = mark;
		updateMarkers(currentMarker);

	}

	@Override
	public void onMarkerDialogPositiveClick(DialogFragment dialog) {
		// System.out.println("Title changed");
		// System.out.println("mDialog marker title "+ mDialog.markerTitle);
		currentMarker.setTitle(mDialog.markerTitle);
		currentMarker.setSnippet(mDialog.markerSnippet);
		updateMarkers(currentMarker);

	}

	@Override
	public void onMarkerDialogNegativeClick(DialogFragment dialog) {
		// System.out.println("Negative Click");
	}

	@Override
	public void onSaveRouteDialogPositiveClick(DialogFragment dialog) {
		route.setName(rDialog.routeName);
		route.setCity(rDialog.routeCity);
		route.setDescription(rDialog.routeDescription);
		if (toPersist) {

			saveRoute();
			// navigateUpTo(getParentActivityIntent());
			navigateToHomeActivity();
		}

	}

	@Override
	public void onSaveRouteDialogNegativeClick(DialogFragment dialog) {
		// TODO Auto-generated method stub

	}

	protected void restInvoke(RequestParams params) {
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
									"Requested resource not found",
									Toast.LENGTH_LONG).show();
						}
						// When Http response code is '500'
						else if (statusCode == 500) {
							Toast.makeText(getApplicationContext(),
									"Something went wrong at server end",
									Toast.LENGTH_LONG).show();
						}
						// When Http response code other than 404, 500
						else {
							Toast.makeText(
									getApplicationContext(),
									"Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]",
									Toast.LENGTH_LONG).show();
						}
					}
				});
	}

}
