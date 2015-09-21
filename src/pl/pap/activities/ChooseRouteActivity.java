package pl.pap.activities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import pl.pap.activities.base.BaseActivity;
import pl.pap.client.R;
import pl.pap.dialogs.CriteriaDialog;
import pl.pap.model.MarkerModel;
import pl.pap.model.Route;
import pl.pap.routeslist.adapter.RouteListAdapter;
import pl.pap.utils.ConnectionGuardian;
import pl.pap.utils.Consts;
import pl.pap.utils.OfflineModeManager;
import pl.pap.utils.SharedPrefsUtils;
import pl.pap.utils.Utility;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseRouteActivity extends BaseActivity implements Consts,
		CriteriaDialog.CriteriaDialogListener {

	private ListView listView;
	private RouteListAdapter adapter;
	Route route;
	List<Route> routesList;
	List<Route> appliedCriteriaList;

	CriteriaDialog cDialog;

	ConnectionGuardian connGuard;
	SharedPrefsUtils prefs;
	OfflineModeManager offManager;

	//
	TextView tvConn;
	ListView lvRouteList;
	Button btnShowRoutesList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_choose_route);
		//
		prefs = new SharedPrefsUtils(this);
		connGuard = new ConnectionGuardian(this);
		offManager= new OfflineModeManager(this);
		//

		//
		setUpSlideMenu();

		//
		tvConn = (TextView) findViewById(R.id.tvConnection);
		lvRouteList = (ListView) findViewById(R.id.lV1);
		btnShowRoutesList = (Button) findViewById(R.id.btnShowRoutesList);
		btnShowRoutesList.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showStoredRoutesList();

			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();
		checkConnectionAndDealWithList();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onPause() {
		super.onPause();
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
		switch (item.getItemId()) {

		case R.id.action_search:
			showCriteriaDialog();
			break;

		case R.id.action_refresh:
			checkConnectionAndDealWithList();
			break;
		case R.id.showStoredList:
			showStoredRoutesList();
			break;
		default:

		}
		return super.onOptionsItemSelected(item);
	}

	private void showCriteriaDialog() {
		cDialog = new CriteriaDialog();
		FragmentManager fragMan = getSupportFragmentManager();
		cDialog.show(fragMan, "saveRouteDialog");
	}

	private void selectCriteria() {

		if (cDialog.cbAuthor.isChecked() && cDialog.cbCity.isChecked()) {
			applyCriteriaBoth(cDialog.criteriaAuthor, cDialog.criteriaCity);
			adapter.clear();
			adapter.addAll(appliedCriteriaList);
			return;
		} else {
			if (cDialog.cbAuthor.isChecked()) {
				applyCriteriaAuthor(cDialog.criteriaAuthor);
				adapter.clear();
				adapter.addAll(appliedCriteriaList);
				return;
			}

			if (cDialog.cbCity.isChecked()) {
				applyCriteriaCity(cDialog.criteriaCity);
				adapter.clear();
				adapter.addAll(appliedCriteriaList);
				return;
			}
		}

		int selectedRadio;
		selectedRadio = cDialog.rgSelectMode.getCheckedRadioButtonId();

		if (cDialog.rbShowMy.getId() == selectedRadio) {
			applyCriteriaAuthor(prefs.getLogin());
			adapter.clear();
			adapter.addAll(appliedCriteriaList);
			return;
		}

		if (cDialog.rbShowAll.getId() == selectedRadio) {
			adapter.clear();
			adapter.addAll(routesList);
			return;
		}

	}

	private void applyCriteriaBoth(String author, String city) {
		appliedCriteriaList = new ArrayList<Route>();
		if (Utility.isNotNull(author) && Utility.isNotNull(city)) {
			for (Route value : routesList) {
				if (value.getAuthor().equals(author)
						&& value.getCity().equals(city)) {
					appliedCriteriaList.add(value);
				}
			}
		} else
			Toast.makeText(getApplicationContext(), R.string.cantbenull,
					Toast.LENGTH_LONG).show();
	}

	private void applyCriteriaAuthor(String author) {
		appliedCriteriaList = new ArrayList<Route>();
		if (Utility.isNotNull(author)) {
			for (Route value : routesList) {
				if (value.getAuthor().equals(author)) {
					appliedCriteriaList.add(value);
				}
			}
		} else
			Toast.makeText(getApplicationContext(), R.string.cantbenull,
					Toast.LENGTH_LONG).show();
	}

	private void applyCriteriaCity(String city) {
		appliedCriteriaList = new ArrayList<Route>();
		if (Utility.isNotNull(city)) {
			for (Route value : routesList) {
				if (value.getCity() != null) {
					if (value.getCity().equals(city)) {
						appliedCriteriaList.add(value);
					}
				}
			}
		} else
			Toast.makeText(getApplicationContext(), R.string.cantbenull,
					Toast.LENGTH_LONG).show();
	}

	private void convertFromJson(String json) {
		Gson gson = new Gson();
		Route[] routesArray = gson.fromJson(json, Route[].class);
		routesList = Arrays.asList(routesArray);

	}

	private void requestRoutesList() {
		RequestParams params = new RequestParams();
		params.put("login", prefs.getLogin());
		params.put("sessionId", prefs.getSessionID());
		restInvokeRequest(params);
	}

	private void showStoredRoutesList() {
		if (prefs.checkRoutesList()) {
			convertFromJson(prefs.getRoutesList());
			populateList();
			lvRouteList.setVisibility(View.VISIBLE);
			tvConn.setVisibility(View.GONE);
			btnShowRoutesList.setVisibility(View.GONE);
			return;
		}

		Toast.makeText(this, R.string.noOfflineRoutesList, Toast.LENGTH_LONG)
				.show();
	}

	private void populateList() {
		adapter = new RouteListAdapter(this, R.layout.list_chose_route_item);
		listView = (ListView) findViewById(R.id.lV1);
		listView.setAdapter(adapter);
		adapter.addAll(routesList);

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				Route routeToPass = (Route) listView
						.getItemAtPosition(position);
				
				navigateToShowRouteActivity(routeToPass);

			}

		});
	}

	private void checkConnectionAndDealWithList() {
		if (!connGuard.isConnectedToInternet()) {
			lvRouteList.setVisibility(View.GONE);
			tvConn.setVisibility(View.VISIBLE);
			btnShowRoutesList.setVisibility(View.VISIBLE);
			return;
		}
		lvRouteList.setVisibility(View.VISIBLE);
		tvConn.setVisibility(View.GONE);
		btnShowRoutesList.setVisibility(View.GONE);
		requestRoutesList();

	}

	private void restInvokeRequest(RequestParams params) {
		// Make RESTful webservice call using AsyncHttpClient object
		AsyncHttpClient client = new AsyncHttpClient();
		client.get(domainAdress + REQUEST_ROUTES_LIST, params,
				new AsyncHttpResponseHandler() {
					// When the response returned by REST has Http response code
					// '200'
					@Override
					public void onSuccess(int StatusCode, String answer) {
						try {
							JSONObject jO = new JSONObject(answer);
							if (jO.getBoolean(Consts.MSG_STATUS)) {
								
								convertFromJson(jO.getString(Consts.MSG_DATA));
								offManager.storeRoutesList(jO.getString(Consts.MSG_DATA));
							
								populateList();
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


	private void navigateToShowRouteActivity(Route route) {
		Intent showRouteIntent = new Intent(getApplicationContext(),
				ShowRouteActivity.class);
		showRouteIntent.putExtra("route", Utility.convertToJson(route));
		showRouteIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(showRouteIntent);

	}

	@Override
	public void onCriteriaDialogPositiveClick(DialogFragment dialog) {
		selectCriteria();

	}

	@Override
	public void onCriteriaDialogNegativeClick(DialogFragment dialog) {
		// TODO Auto-generated method stub

	}
}
