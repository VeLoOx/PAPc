package pl.pap.activites;

import org.json.JSONException;
import org.json.JSONObject;

import pl.pap.client.R;
import pl.pap.dialogs.MarkerDialog;
import pl.pap.maps.MapsMethods;
import pl.pap.maps.MapsSettings;
import pl.pap.model.Route;
import pl.pap.utils.Consts;

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

import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class PlanRouteActivity extends FragmentActivity implements
		OnMapLongClickListener, OnMapClickListener, OnMarkerDragListener,
		OnMarkerClickListener, MarkerDialog.MarkerDialogListener, MapsMethods,
		Consts, ActionBar.OnNavigationListener {

	// Google Map
	private GoogleMap googleMap;
	MarkerDialog mDialog = new MarkerDialog();
	Marker currentMarker;
	MapsSettings maps;
	Route route;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.plan_route);
		System.out.println("PlanRouteActivity on create");
		try {
			route = new Route();
			// Loading map
			initializeMap();
			// Set up map methods
			maps = new MapsSettings(googleMap);
			googleMap = maps.setUpMap();
			setUpListeners();
			// setUpMap();

		} catch (Exception e) {
			e.printStackTrace();
		}
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
		String ktoryElement = "";

		switch (item.getItemId()) {

		case R.id.item1:
			ktoryElement = "pierwszy";

			break;
		case R.id.item2:
			System.out.println("Marker map size " + route.getMarkerMap());
			for (Object value : route.getMarkerMap().values()) {
				googleMap.addMarker(route
						.convertToMarkerOptions((Marker) value));
			}
			break;
		case R.id.item3:
			ktoryElement = "trzeci";
			break;
		default:
			ktoryElement = "¿aden";

		}

		Toast.makeText(getApplicationContext(), "Element: " + ktoryElement,
				Toast.LENGTH_LONG).show();

		return super.onOptionsItemSelected(item);
	}

	private void saveRoute() {

	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {

		return false;
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

	@Override
	public void addMarker(LatLng point) {
		MarkerOptions marker = new MarkerOptions().position(
				new LatLng(point.latitude, point.longitude))
				.title("New Marker");
		marker.draggable(true);
		Marker tmp = googleMap.addMarker(marker);
		// route.addMarkerToList(googleMap.addMarker(marker));
		route.addMarkerToMap(tmp.getId(), tmp);
		System.out.println("Marker added " + point.latitude + "---"
				+ point.longitude);
	}

	@Override
	public void deleteMarker() {
		if (mDialog.isDeletable) {
			currentMarker.remove();
		}

	}

	@Override
	public boolean updateMarkers(Marker marker) {
		// if(route.removeMarkerFromMap(marker.getId())){
		System.out.println("Marker remove and update status: "
				+ route.removeMarkerFromMap(marker.getId()));
		route.addMarkerToMap(marker.getId(), marker);
		System.out.println("Marker updated");
		return true;
		// }
		// return false;
	}

	public void restInvoke(RequestParams params) {
		// Show Progress Dialog
		// prgDialog.show();
		// Make RESTful webservice call using AsyncHttpClient object
		AsyncHttpClient client = new AsyncHttpClient();
		client.get(domainAdress + LOGIN, params,
				new AsyncHttpResponseHandler() {
					// When the response returned by REST has Http response code
					// '200'
					@Override
					public void onSuccess(int StatusCode, String answer) {
						// Hide Progress Dialog
						//prgDialog.hide();
						try {
							// JSON Object
							JSONObject jO = new JSONObject(answer);
							// When the JSON response has status boolean value
							// assigned with true
							if (jO.getBoolean("status")) {
								Toast.makeText(
										getApplicationContext(),
										"You are successfully logged in! Status code: "
												+ StatusCode, Toast.LENGTH_LONG)
										.show();
							}
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							Toast.makeText(
									getApplicationContext(),
									"Error Occured [Server's JSON response might be invalid]!",
									Toast.LENGTH_LONG).show();
							e.printStackTrace();

						}
					}

					// When the response returned by REST has Http response code
					// other than '200'
					@Override
					public void onFailure(int statusCode, Throwable error,
							String content) {
						// Hide Progress Dialog
						//prgDialog.hide();
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

	// LISTENERS
	@Override
	public boolean onMarkerClick(Marker mark) {

		// System.out.println("prev: " + mark.getTitle()); mark.setTitle("asd");
		// System.out.println("after: " + mark.getTitle());

		// mark.showInfoWindow();
		deleteMarker();
		System.out.println(mark.getPosition());
		mDialog = new MarkerDialog();
		currentMarker = mark;
		FragmentManager fragMan = getSupportFragmentManager();
		mDialog.show(fragMan, "markerDialog");

		/*
		 * mDialog.btnDeleteMarker.setOnClickListener(new View.OnClickListener()
		 * {
		 * 
		 * @Override public void onClick(View v) { currentMarker.remove();
		 * 
		 * } });
		 */

		return false;
	}

	@Override
	public void onMapClick(LatLng arg0) {
		System.out.println("Map clicked");
		// maps.googleMap.animateCamera(CameraUpdateFactory.newLatLng(arg0));
		deleteMarker();
		/*
		 * System.out.println("Marker list size "+
		 * route.getMarkerList().size()); for(Marker
		 * marker:route.getMarkerList()){
		 * googleMap.addMarker(route.convertToMarkerOptions(marker)); }
		 */
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
	public void onDialogPositiveClick(DialogFragment dialog) {
		// System.out.println("Title changed");
		// System.out.println("mDialog marker title "+ mDialog.markerTitle);
		currentMarker.setTitle(mDialog.markerTitle);
		updateMarkers(currentMarker);

	}

	@Override
	public void onDialogNegativeClick(DialogFragment dialog) {
		// System.out.println("Negative Click");
	}

}
