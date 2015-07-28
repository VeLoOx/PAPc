package pl.pap.activites;

import org.json.JSONException;
import org.json.JSONObject;

import pl.pap.client.R;
import pl.pap.dialogs.MarkerDialog;
import pl.pap.maps.MapsMethods;
import pl.pap.maps.MapsSettings;
import pl.pap.model.MarkerModel;
import pl.pap.model.Route;
import pl.pap.utils.Consts;
import pl.pap.utils.SharedPrefsUtils;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;
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
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class PlanRouteActivity extends FragmentActivity implements
		OnMapLongClickListener, OnMapClickListener, OnMarkerDragListener,
		OnMarkerClickListener, MarkerDialog.MarkerDialogListener, MapsMethods,
		Consts, ActionBar.OnNavigationListener {

	// Google Map
	private GoogleMap googleMap;
	MarkerDialog mDialog = new MarkerDialog();
	Marker currentMarker;
	MapsSettings maps;
	Route route;// = new Route();
	JSONObject jsonMarker;
	SharedPrefsUtils prefs;

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
			prefs = new SharedPrefsUtils(this);

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
			ktoryElement = "zapis";
			saveRoute();
			break;
		case R.id.item2:
			ktoryElement = "naniesienie na mape";
			fillMap();
			break;
		case R.id.item3:
			/*
			 * ktoryElement = "marshall"; System.out.println("Gson mapper");
			 * System.out.println(convertToJson());
			 */
			requestRoute();
			break;
		default:
			ktoryElement = "¿aden";

		}

		Toast.makeText(getApplicationContext(), "Element: " + ktoryElement,
				Toast.LENGTH_LONG).show();

		return super.onOptionsItemSelected(item);
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

	private void fillMap() {
		if (route != null) {
			System.out.println("Marker map size " + route.getMarkerMap());
			for (Object value : route.getMarkerMap().values()) {
				googleMap.addMarker(route
						.convertToMarkerOptions((MarkerModel) value));
			}
		} else
			System.out.println("Route empty");
	}

	@Override
	public void addMarker(LatLng point) {
		MarkerOptions marker = new MarkerOptions().position(
				new LatLng(point.latitude, point.longitude))
				.title("New Marker");
		marker.draggable(true);
		Marker tmp = googleMap.addMarker(marker);
		// route.addMarkerToList(googleMap.addMarker(marker));
		// MarkerModel markModel = new MarkerModel();
		// markModel.copyValues(tmp);
		// route.addMarkerToMap(markModel.getId(), markModel);
		persistMarker(tmp);
		System.out.println("Marker added " + point.latitude + "---"
				+ point.longitude);
	}

	@Override
	public void deleteMarker() {
		if (mDialog.isDeletable) {
			currentMarker.remove();
		}

	}

	private void persistMarker(Marker marker) {
		MarkerModel markModel = new MarkerModel();
		markModel.copyValues(marker);
		route.addMarkerToMap(markModel.getMarkerId(), markModel);
	}

	@Override
	public boolean updateMarkers(Marker marker) {
		// if(route.removeMarkerFromMap(marker.getId())){
		System.out.println("Marker remove and update status: "
				+ route.removeMarkerFromMap(marker.getId()));
		// route.addMarkerToMap(marker.getId(), marker);
		persistMarker(marker);
		System.out.println("Marker updated");
		return true;
		// }
		// return false;
	}

	private String convertToJson() {
		Gson gson = new Gson();
		String ret = "";
		ret = gson.toJson(route);
		return ret;
	}

	private void convertFromJson(String json) {
		Gson gson = new Gson();
		Route routeModel = gson.fromJson(json, Route.class);
		System.out.println("GSON: String: " + route);
		System.out.println(routeModel);
		System.out.println(routeModel.getAuthor());
		System.out.println("rozmiar mapy markerow "
				+ routeModel.getMarkerMap().size());
		for (MarkerModel value : routeModel.getMarkerMap().values()) {
			// System.out.println("ID "+(MarkerModel)value.toString());
			System.out.println(value.getTitle());
		}
	}

	private JSONObject convertToJson2() {
		JSONObject jObject = new JSONObject();
		try {
			jObject.put("Marker", currentMarker);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// System.out.println(jObject);
		return jObject;

	}

	private void restInvoke(RequestParams params) {
		// Show Progress Dialog
		// prgDialog.show();
		// Make RESTful webservice call using AsyncHttpClient object
		//System.out.println("PlanRouteActivity: Inside restInvoke");
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
								Toast.makeText(
										getApplicationContext(),
										"Route succesfully saved " + StatusCode,
										Toast.LENGTH_LONG).show();
							}else{
								Toast.makeText(
										getApplicationContext(),
										jO.getString("errorMessage") + StatusCode,
										Toast.LENGTH_LONG).show();
							}
						} catch (JSONException e) {
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

	private void restInvokeRequest(RequestParams params) {
		// Show Progress Dialog
		// prgDialog.show();
		// Make RESTful webservice call using AsyncHttpClient object
		System.out.println("PlanRouteActivity: Inside restInvokeRequest");
		AsyncHttpClient client = new AsyncHttpClient();
		System.out.println(domainAdress + REQUEST_ROUTE);
		client.get(domainAdress + REQUEST_ROUTE, params,
				new AsyncHttpResponseHandler() {
					// When the response returned by REST has Http response code
					// '200'
					@Override
					public void onSuccess(int StatusCode, String answer) {
						try {
							JSONObject jO = new JSONObject(answer);
							if (jO.getBoolean("status")) {
								System.out
										.println("Recived string from server: "
												+ answer);
								System.out.println("Data from serwer: "
										+ jO.getString("data"));
								convertFromJson(jO.getString("data"));
							}else{
								Toast.makeText(
										getApplicationContext(),
										jO.getString("errorMessage"),
										Toast.LENGTH_LONG).show();
							}
						} catch (JSONException e) {
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

	private void saveRoute() {
		// Fill route object
		// route.setId((long) 666);
		route.setAuthor(prefs.getLogin());
		route.setCity("London");
		route.setDescription("asd qwe zxc ghj");
		// Instantiate Http Request Param Object
		RequestParams params = new RequestParams();
		// String login=prefs.getString(USER_LOGIN, "");
		params.put("login", prefs.getLogin());
		params.put("sessionId", prefs.getSessionID());
		params.put("route", convertToJson());
		restInvoke(params);
	}

	private void requestRoute() {
		RequestParams params = new RequestParams();
		params.put("login", prefs.getLogin());
		params.put("sessionId", prefs.getSessionID());
		params.put("autor", prefs.getLogin());
		params.put("id", "71");
		restInvokeRequest(params);
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
