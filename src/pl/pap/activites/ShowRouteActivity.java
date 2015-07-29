package pl.pap.activites;

import org.json.JSONException;
import org.json.JSONObject;

import pl.pap.client.R;
import pl.pap.dialogs.DescriptionDialog;
import pl.pap.dialogs.MarkerDialog;
import pl.pap.maps.MapsMethods;
import pl.pap.maps.MapsSettings;
import pl.pap.model.MarkerModel;
import pl.pap.model.Route;
import pl.pap.utils.Consts;
import pl.pap.utils.SharedPrefsUtils;
import pl.pap.utils.Utility;

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
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

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

public class ShowRouteActivity extends FragmentActivity implements
		OnMapLongClickListener, OnMapClickListener, OnMarkerDragListener,
		OnMarkerClickListener, MarkerDialog.MarkerDialogListener, MapsMethods,
		Consts {

	private GoogleMap googleMap;
	DescriptionDialog dDialog = new DescriptionDialog();
	MarkerDialog mDialog = new MarkerDialog();
	Marker currentMarker;
	MapsSettings maps;
	Route route;// = new Route();
	JSONObject jsonMarker;
	SharedPrefsUtils prefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_route);
		initilizeMap();
		// Set up map methods
		maps = new MapsSettings(googleMap);
		googleMap = maps.setUpMap();
		setUpListeners();
		prefs = new SharedPrefsUtils(this);
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
		    String value = extras.getString("routeId");
		    requestRoute(value);
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

	public void showDescription(View view) {
		dDialog.show(getSupportFragmentManager(), "DescriptionDialog");
	}

	private void requestRoute(String routeId) {
		RequestParams params = new RequestParams();
		params.put("login", prefs.getLogin());
		params.put("sessionId", prefs.getSessionID());
		params.put("autor", prefs.getLogin());
		params.put("id", routeId);
		restInvokeRequest(params);
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
		route=routeModel;
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
	public boolean onMarkerClick(Marker mark) {
		/*
		 * System.out.println("prev: " + mark.getTitle()); mark.setTitle("asd");
		 * System.out.println("after: " + mark.getTitle());
		 */
		// mark.showInfoWindow();
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
		if (mDialog.isDeletable) {
			currentMarker.remove();
		}
		return false;
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
	public void onMarkerDrag(Marker arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMarkerDragStart(Marker arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMarkerDragEnd(Marker arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMarkerDialogPositiveClick(DialogFragment dialog) {
		currentMarker.setTitle(mDialog.markerTitle);

	}

	@Override
	public void onMarkerDialogNegativeClick(DialogFragment dialog) {
		// TODO Auto-generated method stub

	}

	@Override
	public void initializeMap() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setUpListeners() {
		// TODO Auto-generated method stub

	}

	@Override
	public void addMarker(LatLng point) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteMarker() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean updateMarkers(Marker marker) {
		// TODO Auto-generated method stub
		return false;
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
								//convertFromJson(jO.getString("data"));
								route=Utility.convertRouteFromJson(jO.getString("data"));
								fillMap();
							} else {
								Toast.makeText(getApplicationContext(),
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
}
