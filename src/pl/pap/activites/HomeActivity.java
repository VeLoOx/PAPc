package pl.pap.activites;

import org.json.JSONException;
import org.json.JSONObject;

import pl.pap.actionbar.adapter.SpinnerNavigationAdapter;
import pl.pap.client.R;
import pl.pap.utils.Consts;
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
	SharedPreferences sp;
	// action bar
	private ActionBar actionBar;
	
	//adapter
	private SpinnerNavigationAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Displays Home Screen
		setContentView(R.layout.home);
		tvUserData = (TextView) findViewById(R.id.tvUserData);
		tvWelcomeBar = (TextView) findViewById(R.id.tvWelcomeBar);
		sp = getSharedPreferences(LoginActivity.PREFS, Context.MODE_PRIVATE);
		// Progress dialog
		prgDialog = new ProgressDialog(this);
		prgDialog.setMessage("Loading...");
		prgDialog.setCancelable(false);
		//restInvoke(null);

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

		// mDialog.show(getFragmentManager(), "");

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_main_actions, menu);

		return super.onCreateOptionsMenu(menu);
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

	public void getUserData() {

	}

	public void restInvoke(RequestParams params) {
		// Show Progress Dialog
		prgDialog.show();
		// Make RESTful webservice call using AsyncHttpClient object
		AsyncHttpClient client = new AsyncHttpClient();
		client.get(domainAdress + GET_USER_INFO, params,
				new AsyncHttpResponseHandler() {
					// When the response returned by REST has Http response code
					// '200'
					@Override
					public void onSuccess(String response) {
						// Hide Progress Dialog
						if (prgDialog != null)
							prgDialog.hide();
						try {
							// JSON Object
							JSONObject jO = new JSONObject(response);

							// Tutaj wyœwietlanie imienia WSTAWIC POBIERANE Z BAZY
							/*
							 * tvWelcomeBar.setText(tvWelcomeBar.getText() + " "
							 * + sp.getString("loginKey", null));
							 */
							tvWelcomeBar.setText(tvWelcomeBar.getText() + " "
									+ "Piotr");

							// Display successfully registered message using
							// Toast
							Toast.makeText(getApplicationContext(),
									"User data extracted!", Toast.LENGTH_LONG)
									.show();
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
						prgDialog.hide();
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

	

}
