package pl.pap.activites;

import org.json.JSONException;
import org.json.JSONObject;

import pl.pap.client.R;
import pl.pap.dialogs.DescriptionDialog;
import pl.pap.dialogs.MarkerDialog;
import pl.pap.dialogs.RouteInfoDialog;
import pl.pap.dialogs.SaveRouteDialog;
import pl.pap.maps.MapsMethods;
import pl.pap.maps.MapsSettings;
import pl.pap.model.MarkerModel;
import pl.pap.model.Route;
import pl.pap.utils.Consts;
import pl.pap.utils.SharedPrefsUtils;
import pl.pap.utils.Utility;
import android.content.Intent;
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
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class ShowRouteActivity extends FragmentActivity implements
		OnMapLongClickListener, OnMapClickListener, OnMarkerDragListener,
		OnMarkerClickListener, MarkerDialog.MarkerDialogListener,
		SaveRouteDialog.SaveRouteDialogListener,
		RouteInfoDialog.RouteInfoDialogListener, MapsMethods, Consts {

	private GoogleMap googleMap;
	DescriptionDialog dDialog = new DescriptionDialog();

	Marker currentMarker;
	MarkerDialog mDialog = new MarkerDialog(currentMarker);
	SaveRouteDialog rDialog;
	RouteInfoDialog iDialog;
	MapsSettings maps;
	Route route;// = new Route();
	JSONObject jsonMarker;
	SharedPrefsUtils prefs;
	boolean isAuthor = false;
	boolean isEditable = false;
	boolean toPersist = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_route);
		initializeMap();
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

		String ktoryElement = "";

		switch (item.getItemId()) {

		case R.id.showRouteInfoItem:
			ktoryElement = "Info";
			showRouteInfo();
			break;
		case R.id.showRouteEditItem:
			ktoryElement = "edit";
			isEditable=true;
			//setUpListeners();
			break;
		case R.id.showRouteSaveItem:
			ktoryElement = "save";
			toPersist = true;
			fillRouteInfo();
			break;
		default:
			ktoryElement = "none";
		}
		
		Toast.makeText(getApplicationContext(), "Element: " + ktoryElement,
				Toast.LENGTH_LONG).show();

		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (isAuthor) {
			// menu.clear();
			menu.findItem(R.id.showRouteEditItem).setEnabled(true);
		}
		if (isEditable){
			menu.findItem(R.id.showRouteSaveItem).setVisible(true);
			menu.findItem(R.id.showRouteEditItem).setEnabled(false);
		}
		return super.onPrepareOptionsMenu(menu);

	}

	private void requestRoute(String routeId) {
		RequestParams params = new RequestParams();
		params.put("login", prefs.getLogin());
		params.put("sessionId", prefs.getSessionID());
		params.put("autor", prefs.getLogin());
		params.put("id", routeId);
		restInvokeRequest(params);
	}

	private void fillMap() {
		if (route != null) {
			// System.out.println("Marker map size " + route.getMarkerMap());
			for (Object value : route.getMarkerMap().values()) {
				googleMap.addMarker(route
						.convertToMarkerOptions((MarkerModel) value));
			}
		} else
			System.out.println("Route empty");
	}

	private void showRouteInfo() {
		iDialog = new RouteInfoDialog(route);
		FragmentManager fragMan = getSupportFragmentManager();
		iDialog.show(fragMan, "routeInfoDialog");
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
		persistMarker(tmp);
		System.out.println("Marker added " + point.latitude + "---"
				+ point.longitude);
	}

	private void persistMarker(Marker marker) {
		MarkerModel markModel = new MarkerModel();
		markModel.copyValues(marker);
		route.addMarkerToMap(markModel.getMarkerId(), markModel);
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
		System.out.println("Marker remove and update status: "
				+ route.removeMarkerFromMap(marker.getId()));
		persistMarker(marker);
		System.out.println("Marker updated");
		return true;

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
			//saveRoute();
			//navigateToHomeActivity();
			System.out.println("Route is about to be updated");
			updateRoute();
			navigateToHomeActivity();
		}

	}
	
	@Override
	public void onSaveRouteDialogNegativeClick(DialogFragment dialog) {
		// TODO Auto-generated method stub

	}

	public void navigateToHomeActivity() {
		// prgDialog.dismiss();
		Intent homeIntent = new Intent(getApplicationContext(),
				HomeActivity.class);
		homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(homeIntent);
	}

	

	private void restInvoke(RequestParams params) {
		// Show Progress Dialog
		// prgDialog.show();
		// Make RESTful webservice call using AsyncHttpClient object
		// System.out.println("PlanRouteActivity: Inside restInvoke");
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
								Toast.makeText(
										getApplicationContext(),
										"Route succesfully updated " + StatusCode,
										Toast.LENGTH_LONG).show();
							} else {
								Toast.makeText(
										getApplicationContext(),
										jO.getString("errorMessage")
												+ StatusCode, Toast.LENGTH_LONG)
										.show();
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
		client.get(domainAdress + REQUEST_ROUTE, params,
				new AsyncHttpResponseHandler() {
					// When the response returned by REST has Http response code
					// '200'
					@Override
					public void onSuccess(int StatusCode, String answer) {
						try {
							JSONObject jO = new JSONObject(answer);
							if (jO.getBoolean("status")) {
								// convertFromJson(jO.getString("data"));
								route = Utility.convertRouteFromJson(jO
										.getString("data"));
								fillMap();
								if (route.getAuthor().equals(prefs.getLogin())) {
									isAuthor = true;
								}
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
