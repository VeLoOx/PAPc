package pl.pap.activites;

import org.json.JSONException;
import org.json.JSONObject;

import pl.pap.actionbar.adapter.SpinnerNavigationAdapter;
import pl.pap.client.R;
import pl.pap.utils.ConnectionGuardian;
import pl.pap.utils.Consts;
import pl.pap.utils.OfflineModeManager;
import pl.pap.utils.SharedPrefsUtils;
import pl.pap.utils.Utility;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class HomeActivity extends Activity implements Consts,
		ActionBar.OnNavigationListener {
	TextView tvUserData;
	TextView tvWelcomeBar;
	ProgressDialog prgDialog;
	// Utils
	// SharedPreferences sp;
	ConnectionGuardian connGuard;
	SharedPrefsUtils prefs;
	OfflineModeManager offManager;
	// action bar
	private ActionBar actionBar;

	// adapter
	private SpinnerNavigationAdapter adapter;

	private int actualRouteNumber = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Utils
		prefs = new SharedPrefsUtils(this);
		connGuard = new ConnectionGuardian(this);
		offManager = new OfflineModeManager(this);

		// Displays Home Screen
		setContentView(R.layout.home);
		tvUserData = (TextView) findViewById(R.id.tvUserData);
		tvWelcomeBar = (TextView) findViewById(R.id.tvWelcomeBar);
		tvWelcomeBar.setText(tvWelcomeBar.getText() + " " + prefs.getLogin());

		// Progress dialog
		prgDialog = new ProgressDialog(this);
		prgDialog.setMessage("Loading...");
		prgDialog.setCancelable(false);

		// action bar
		actionBar = getActionBar();
		// Hide the action bar title
		actionBar.setDisplayShowTitleEnabled(false);
		// Enabling Spinner dropdown navigation
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

		// title drop down adapter
		adapter = new SpinnerNavigationAdapter(getApplicationContext());

		// assigning the spinner navigation
		actionBar.setListNavigationCallbacks(adapter, this);

	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {

		switch (itemPosition) {
		case 0:
			System.out.println("case 0");
			break;
		case 1:
			System.out.println("case 1");
			navigateToPlanRouteActivity();
			break;
		case 2:
			System.out.println("case 2");
			navigateToStartNewRouteActivity();
			break;
		case 3:
			System.out.println("case 3");
			navigateToChooseRouteActivity();
		default:
			break;

		}
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.home_menu, menu);

		return super.onCreateOptionsMenu(menu);
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
			ktoryElement = "Sync";
			synchronizeRoutes();
			break;
		case R.id.item2:
			ktoryElement = "checkSync";
			checkSync();
			break;
		default:
			ktoryElement = "none";

		}

		Toast.makeText(getApplicationContext(), "Element: " + ktoryElement,
				Toast.LENGTH_LONG).show();

		return super.onOptionsItemSelected(item);
	}

	public void getUserData() {

	}

	public void logout(View view) {
		SharedPreferences sharedpreferences = getSharedPreferences(
				LoginActivity.PREFS, Context.MODE_PRIVATE);
		Editor editor = sharedpreferences.edit();
		editor.clear();
		editor.commit();
		moveTaskToBack(true);
		HomeActivity.this.finish();
	}

	public void navigateToPlanRouteActivity(View view) {
		Intent planRouteIntent = new Intent(getApplicationContext(),
				PlanRouteActivity.class);
		planRouteIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(planRouteIntent);
	}

	public void navigateToPlanRouteActivity() {
		Intent planRouteIntent = new Intent(getApplicationContext(),
				PlanRouteActivity.class);
		planRouteIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(planRouteIntent);
	}

	public void navigateToStartNewRouteActivity(View view) {
		Intent startRouteIntent = new Intent(getApplicationContext(),
				StartNewRouteActivity.class);
		startRouteIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(startRouteIntent);
	}

	public void navigateToStartNewRouteActivity() {
		Intent startRouteIntent = new Intent(getApplicationContext(),
				StartNewRouteActivity.class);
		startRouteIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(startRouteIntent);
	}

	public void navigateToShowRouteActivity(View view) {
		Intent showRouteIntent = new Intent(getApplicationContext(),
				ShowRouteActivity.class);
		showRouteIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(showRouteIntent);
	}

	public void navigateToChooseRouteActivity(View view) {
		Intent chooseRouteIntent = new Intent(getApplicationContext(),
				ChooseRouteActivity.class);
		chooseRouteIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(chooseRouteIntent);
	}

	public void navigateToChooseRouteActivity() {
		Intent chooseRouteIntent = new Intent(getApplicationContext(),
				ChooseRouteActivity.class);
		chooseRouteIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(chooseRouteIntent);
	}

	public void exit(View view) {
		moveTaskToBack(true);
		HomeActivity.this.finish();
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (prgDialog != null) {
			prgDialog.dismiss();
			prgDialog = null;
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (prgDialog != null) {
			prgDialog.dismiss();
			prgDialog = null;
		}
	}

	private void synchronizeRoutes() {
		if (connGuard.isConnectedToInternet() && offManager.isSmthToPersist()) {
			int i = 1;
			while (prefs.getRoute(i) != "") {
				actualRouteNumber = i;
				System.out.println(ROUTE_KEY + i);
				System.out.println(prefs.getRoute(i));
				persistRoute(prefs.getRoute(i));
				i++;

			}
		} else
			System.out.println("Either not connected or nothin to persist");
	}

	private void checkSync() {
		int i = 1;
		while (prefs.getRoute(i) != "") {
			actualRouteNumber = i;
			System.out.println(ROUTE_KEY + i);
			System.out.println(prefs.getRoute(i));
			i++;

		}
	}

	private void persistRoute(String route) {
		if (connGuard.isConnectedToInternet()) {
			RequestParams params = new RequestParams();
			params.put("login", prefs.getLogin());
			params.put("sessionId", prefs.getSessionID());
			params.put("route", route);
			restInvoke(params);
		} else {
			Toast.makeText(getApplicationContext(), R.string.notConnected,
					Toast.LENGTH_LONG).show();
		}
	}

	public void restInvoke(RequestParams params) {
		// Show Progress Dialog
		// prgDialog.show();
		// Make RESTful webservice call using AsyncHttpClient object
		AsyncHttpClient client = new AsyncHttpClient();
		client.get(domainAdress + PERSIST_ROUTE, params,
				new AsyncHttpResponseHandler() {
					// When the response returned by REST has Http response code
					// '200'
					@Override
					public void onSuccess(int StatusCode, String answer) {
						// Hide Progress Dialog
						if (prgDialog != null)
							prgDialog.hide();
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
								prefs.removeRoute(actualRouteNumber);
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

}
