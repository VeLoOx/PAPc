package pl.pap.activites;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import pl.pap.client.R;
import pl.pap.model.MarkerModel;
import pl.pap.model.Route;
import pl.pap.routeslist.adapter.RouteListAdapter;
import pl.pap.utils.Consts;
import pl.pap.utils.SharedPrefsUtils;
import pl.pap.utils.Utility;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class ChooseRouteActivity extends Activity implements Consts {

	private ListView listView;
	private RouteListAdapter adapter;
	Route route;
	List<Route> routesList;
	SharedPrefsUtils prefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.choose_route);
		//
		prefs = new SharedPrefsUtils(this);
		requestRoutesList();
		//

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.choose_route_menu, menu);
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

	private void convertFromJson(String json) {
		Gson gson = new Gson();
		Route[] routesArray = gson.fromJson(json, Route[].class);
		// routesList = gson.fromJson(json, Route.class);
		routesList = Arrays.asList(routesArray);
		// System.out.println("routesList siez "+routesList.size());
		for (Route value : routesList) {
			// System.out.println("ID "+(MarkerModel)value.toString());
			System.out.println("Author: " + value.getAuthor());
			System.out.println("Name :" + value.getName());
		}
	}

	private void requestRoutesList() {
		RequestParams params = new RequestParams();
		params.put("login", prefs.getLogin());
		params.put("sessionId", prefs.getSessionID());
		restInvokeRequest(params);
	}

	private void populateList() {
		adapter = new RouteListAdapter(this, R.layout.list_chose_route_item);
		listView = (ListView) findViewById(R.id.lV1);
		listView.setAdapter(adapter);
		/*
		 * Route r1 = new Route(); r1.setAuthor("QWE"); r1.setName("QWE"); Route
		 * r2 = new Route(); r2.setAuthor("ASD"); r2.setName("ASD");
		 * 
		 * ArrayList<Route> arrayList = new ArrayList<Route>();
		 * arrayList.add(r1); arrayList.add(r2);
		 */
		adapter.addAll(routesList);

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Toast.makeText(getApplicationContext(),
						"Click ListItem Number " + position, Toast.LENGTH_LONG)
						.show();
				Route routeToPass = (Route) listView
						.getItemAtPosition(position);
				System.out.println(listView.getItemAtPosition(position));
				navigateToShowRouteActivity(routeToPass.getId().toString());

			}

		});
	}

	private void restInvokeRequest(RequestParams params) {
		// Show Progress Dialog
		// prgDialog.show();
		// Make RESTful webservice call using AsyncHttpClient object
		System.out.println("ChoseRouteActivity: Inside restInvokeRequest");
		AsyncHttpClient client = new AsyncHttpClient();
		System.out.println(domainAdress + REQUEST_ROUTES_LIST);
		client.get(domainAdress + REQUEST_ROUTES_LIST, params,
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
								//Utility.convertRouteFromJson(jO.getString("data"));
								populateList();
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

	public void navigateToShowRouteActivity(String routeId) {
		Intent showRouteIntent = new Intent(getApplicationContext(),
				ShowRouteActivity.class);
		showRouteIntent.putExtra("routeId", routeId);
		showRouteIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(showRouteIntent);

	}
}
