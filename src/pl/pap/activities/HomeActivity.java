package pl.pap.activities;

import org.json.JSONException;
import org.json.JSONObject;

import pl.pap.activities.base.BaseActivity;
import pl.pap.client.R;
import pl.pap.utils.ConnectionGuardian;
import pl.pap.utils.Consts;
import pl.pap.utils.OfflineModeManager;
import pl.pap.utils.SharedPrefsUtils;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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

public class HomeActivity extends BaseActivity implements Consts {
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

	// Spinner adapter
	// private SpinnerNavigationAdapter adapter;
	// using to synchronize routes
	private int actualRouteNumber = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Utils
		prefs = new SharedPrefsUtils(this);
		connGuard = new ConnectionGuardian(this);
		offManager = new OfflineModeManager(this);

		// Displays Home Screen
		setContentView(R.layout.activity_home);
		// tvUserData = (TextView) findViewById(R.id.tvUserData);
		tvWelcomeBar = (TextView) findViewById(R.id.tvWelcomeBar);
		tvWelcomeBar.setText(tvWelcomeBar.getText() + " " + prefs.getLogin());

		// Progress dialog
		prgDialog = new ProgressDialog(this);
		prgDialog.setMessage("Loading...");
		prgDialog.setCancelable(false);

		// action bar actionBar = getActionBar();
		// Hide the action bar title
		// actionBar.setDisplayShowTitleEnabled(false);

		// sets up slide menu
		setUpSlideMenu();
		drawerList.setSelection(app.slideMenuPosition);
		drawerList.setItemChecked(0, true);

	}

	@Override
	protected void onStop() {
		super.onStop();
		// System.out.println("Home onStop");
		if (prgDialog != null) {
			prgDialog.dismiss();
			prgDialog = null;
		}
		// exit();
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (prgDialog != null) {
			prgDialog.dismiss();
			prgDialog = null;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		drawerList.setItemChecked(0, true);
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
		// toggle nav drawer on selecting action bar app icon/title

		int id = item.getItemId();
		switch (item.getItemId()) {

		case R.id.action_synchronize:
			synchronizeRoutes();
			break;

		case R.id.action_logout:
			decideToLogout();
			break;

		case R.id.about:
			navigateToAboutActivity();
			break;

		case R.id.item2:
			checkSync();
			break;
		default:

		}

		return super.onOptionsItemSelected(item);
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

	private void logout() {
		SharedPreferences sharedpreferences = getSharedPreferences(
				LoginActivity.PREFS, Context.MODE_PRIVATE);
		Editor editor = sharedpreferences.edit();
		editor.clear();
		editor.commit();
		moveTaskToBack(true);
		HomeActivity.this.finish();
	}

	private void decideToLogout() {

		AlertDialog.Builder alertDialog = new AlertDialog.Builder(
				HomeActivity.this);

		// Setting Dialog Title
		alertDialog.setTitle(R.string.wantLogoutTitle);

		// Setting Dialog Message
		alertDialog.setMessage(R.string.wantLogoutDesc);

		// Setting Icon to Dialog
		alertDialog.setIcon(R.drawable.ic_logout);

		// Setting Positive "Yes" Button
		alertDialog.setPositiveButton(R.string.yes,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {

						// saveRoute();
						logout();

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

	public void exit() {
		moveTaskToBack(true);
		HomeActivity.this.finish();
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
		} else {
			// Toast.makeText(this, R.string.nothingToSycnh,
			// Toast.LENGTH_LONG).show();
			System.out.println("Either not connected or nothin to persist");
		}

	}

	private void checkSync() {
		int i = 1;
		while (prefs.getRoute(i) != "") {
			actualRouteNumber = i;
			System.out.println(ROUTE_KEY + i);
			System.out.println(prefs.getRoute(i));
			i++;

		}

		if (prefs.getRoute(i) == "")
			System.out.println("Nothing to persist");

		System.out.println("Route list: " + prefs.getRoutesList());
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

	public void navigateToAboutActivity() {
		Intent aboutIntent = new Intent(getApplicationContext(),
				AboutActivity.class);
		aboutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(aboutIntent);
	}

}
