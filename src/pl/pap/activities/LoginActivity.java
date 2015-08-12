package pl.pap.activities;

import org.json.JSONException;
import org.json.JSONObject;

import pl.pap.client.R;
import pl.pap.utils.ConnectionGuardian;
import pl.pap.utils.Consts;
import pl.pap.utils.SharedPrefsUtils;
import pl.pap.utils.Utility;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class LoginActivity extends Activity implements Consts {
	// Progress Dialog Object
	ProgressDialog prgDialog;
	// Error Msg TextView Object
	TextView tvErrorMsg;
	// Email Edit View Object
	EditText etUsername;
	// Password Edit View Object
	EditText etPwd;
	// SharedPreferences sharedpreferences;
	SharedPrefsUtils prefs;
	ConnectionGuardian connGuard;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		// Shared prefs
		prefs = new SharedPrefsUtils(this);
		connGuard = new ConnectionGuardian(this);
		
		// Find Error Msg Text View control by ID
		tvErrorMsg = (TextView) findViewById(R.id.login_error);
		// Find Email Edit View control by ID
		etUsername = (EditText) findViewById(R.id.loginUsername);
		// Find Password Edit View control by ID
		etPwd = (EditText) findViewById(R.id.loginPassword);
		setDefaultValues();
		// Instantiate Progress Dialog object
		prgDialog = new ProgressDialog(this);
		// Set Progress Dialog Text
		prgDialog.setMessage("Please wait...");
		// Set Cancelable as False
		prgDialog.setCancelable(false);
	}

	// When sharedprefs contains data home screen is displayed
	@Override
	protected void onResume() {
		// prgDialog.show();
		setDefaultValues();
		if (prefs.checkLogin() && prefs.checkPassword()) {
			navigateToHomeActivity();

		}
		super.onResume();
	}

	private void setDefaultValues() {
		etUsername.setText("");
		etPwd.setText("");
	}

	private void persistSession(String sessionId) {
		String u = etUsername.getText().toString();
		String p = etPwd.getText().toString();
		prefs.setLogin(u);
		prefs.setPassword(p);
		prefs.setSessionID(sessionId);
		System.out.println("Session id recived from server " + sessionId);
	}

	/**
	 * Method gets triggered when Login button is clicked
	 *
	 * @param view
	 */
	public void loginUser(View view) {
		if (connGuard.isConnectedToInternet()) {
			// Get Email Edit View Value
			String userName = etUsername.getText().toString();
			// Get Password Edit View Value
			String password = etPwd.getText().toString();
			// Instantiate Http Request Param Object
			RequestParams params = new RequestParams();
			if (Utility.isNotNull(userName) && Utility.isNotNull(password)) {
				// Put Http parameter username with value of userName Edit View
				// control
				params.put("login", userName);
				// Put Http parameter password with value of Password Edit Value
				// control
				params.put("password", password);
				// Invoke RESTful Web Service with Http parameters
				restInvoke(params);

			} else {
				Toast.makeText(getApplicationContext(), R.string.emptyField,
						Toast.LENGTH_LONG).show();
			}
		}

	}

	/**
	 * Method that performs RESTful webservice invocations
	 *
	 * @param params
	 */
	public void restInvoke(RequestParams params) {
		// Show Progress Dialog
		prgDialog.show();
		// Make RESTful webservice call using AsyncHttpClient object
		AsyncHttpClient client = new AsyncHttpClient();
		client.get(domainAdress + LOGIN, params,
				new AsyncHttpResponseHandler() {
					// When the response returned by REST has Http response code
					// '200'
					@Override
					public void onSuccess(int StatusCode, String answer) {
						// Hide Progress Dialog
						prgDialog.hide();
						try {
							// JSON Object
							JSONObject jO = new JSONObject(answer);
							// When the JSON response has status boolean value
							// assigned with true
							if (jO.getBoolean("status")) {
								Toast.makeText(getApplicationContext(),
										R.string.loginSucces, Toast.LENGTH_LONG)
										.show();
								persistSession(jO.getString("data"));

								// Navigate to Home screen
								navigateToHomeActivity();
							}
							// Else display error message
							else {
								tvErrorMsg.setText(jO.getString("errorMessage"));
								Toast.makeText(getApplicationContext(),
										jO.getString("errorMessage"),
										Toast.LENGTH_LONG).show();
							}
						} catch (JSONException e) {
							// TODO Auto-generated catch block
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
						prgDialog.hide();
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

	/**
	 * Method which navigates from Login Activity to Home Activity
	 */
	public void navigateToHomeActivity() {
		prgDialog.dismiss();
		Intent homeIntent = new Intent(getApplicationContext(),
				HomeActivity.class);
		homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(homeIntent);
	}

	/**
	 * Method gets triggered when Register button is clicked
	 *
	 * @param view
	 */
	public void navigateToRegisterActivity(View view) {
		prgDialog.dismiss();
		Intent loginIntent = new Intent(getApplicationContext(),
				RegisterActivity.class);
		loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(loginIntent);
	}

	@Override
	protected void onStop() {
		super.onStop();
		/*
		 * if (prgDialog != null) { prgDialog.dismiss(); prgDialog = null; }
		 */
		prgDialog.dismiss();
	}

	@Override
	protected void onPause() {
		super.onPause();
		/*
		 * if (prgDialog != null) { prgDialog.dismiss(); prgDialog = null; }
		 */
		prgDialog.dismiss();
	}
}
