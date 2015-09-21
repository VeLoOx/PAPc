package pl.pap.activities.map;

import java.io.UnsupportedEncodingException;

import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import pl.pap.activities.base.BaseActivity;
import pl.pap.client.R;
import pl.pap.dialogs.MarkerDialog;
import pl.pap.dialogs.SaveRouteDialog;
import pl.pap.maps.MapsMethods;
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
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

public class MapActivity extends BaseActivity implements
		OnMapLongClickListener, OnMapClickListener, OnMarkerDragListener,
		OnMarkerClickListener, MarkerDialog.MarkerDialogListener,
		SaveRouteDialog.SaveRouteDialogListener, MapsMethods, Consts {

	// Google Map
	protected GoogleMap googleMap;
	protected Marker currentMarker;
	protected Route route;// = new Route();

	// Dialogs
	protected MarkerDialog mDialog = new MarkerDialog(currentMarker);
	protected SaveRouteDialog rDialog;

	// Utils
	// protected JSONObject jsonMarker;
	protected SharedPrefsUtils prefs;
	protected ConnectionGuardian connGuard;
	protected OfflineModeManager offManager;

	protected boolean toPersist = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	@SuppressLint("NewApi")
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

	public void setUpMap() {
		// Changing map type
		googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

		// Showing / hiding your current location
		googleMap.setMyLocationEnabled(false);

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
		persistMarker(marker);
		return true;

	}

	// LISTENERS
	@Override
	public boolean onMarkerClick(Marker mark) {
		deleteMarker();
		currentMarker = mark;
		mDialog = new MarkerDialog(currentMarker);
		FragmentManager fragMan = getSupportFragmentManager();
		mDialog.show(fragMan, "markerDialog");
		return false;
	}

	@Override
	public void onMapClick(LatLng arg0) {
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
		currentMarker = mark;
		updateMarkers(currentMarker);

	}

	@Override
	public void onMarkerDialogPositiveClick(DialogFragment dialog) {
		currentMarker.setTitle(mDialog.markerTitle);
		currentMarker.setSnippet(mDialog.markerSnippet);
		updateMarkers(currentMarker);

	}

	@Override
	public void onMarkerDialogNegativeClick(DialogFragment dialog) {
	}

	@Override
	public void onSaveRouteDialogPositiveClick(DialogFragment dialog) {
		route.setName(rDialog.routeName);
		route.setCity(rDialog.routeCity);
		route.setDescription(rDialog.routeDescription);
		if (toPersist) {
			saveRoute();
		}

	}

	@Override
	public void onSaveRouteDialogNegativeClick(DialogFragment dialog) {
		// TODO Auto-generated method stub

	}

	protected void saveRoute() {
		route.setAuthor(prefs.getLogin());
		if (connGuard.isConnectedToInternet()) {
			restInvokePost();
		} else {
			offManager.storeRoute(Utility.convertToJson(route));
			navigateToHomeActivity();
		}
	}

	protected void restInvokePost() {
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
		client.post(getApplicationContext(), domainAdress + PERSIST_ROUTE,
				null, entity, Consts.JSON, new AsyncHttpResponseHandler() {
					// When the response returned by REST has Http response code
					// '200'
					@Override
					public void onSuccess(int StatusCode, String answer) {
						try {
							// JSON Object
							JSONObject jO = new JSONObject(answer);
							// When the JSON response has status boolean value
							// assigned with true
							if (jO.getBoolean(Consts.MSG_STATUS)) {
								Toast.makeText(
										getApplicationContext(),
										jO.getString(Consts.MSG_INFO)
												+ StatusCode, Toast.LENGTH_LONG)
										.show();
								navigateToHomeActivity();
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

}
