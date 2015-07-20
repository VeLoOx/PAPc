package pl.pap.activites;

import org.json.JSONException;
import org.json.JSONObject;

import pl.pap.client.R;
import pl.pap.utils.Consts;
import pl.pap.utils.Utility;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class RegisterActivity extends Activity implements Consts {
	// Progress Dialog Object
	ProgressDialog prgDialog;
	// Error Msg TextView Object
	TextView tvErrorMsg;
	// Name Edit View Object
	EditText etName;
	// Email Edit View Object
	EditText etEmail;
	// Passwprd Edit View Object
	EditText etPassword;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register);
		// Find Error Msg Text View control by ID
		tvErrorMsg = (TextView) findViewById(R.id.register_error);
		// Find Name Edit View control by ID
		etName = (EditText) findViewById(R.id.registerName);
		// Find Email Edit View control by ID
		etEmail = (EditText) findViewById(R.id.registerEmail);
		// Find Password Edit View control by ID
		etPassword = (EditText) findViewById(R.id.registerPassword);
		// Instantiate Progress Dialog object
		prgDialog = new ProgressDialog(this);
		// Set Progress Dialog Text
		prgDialog.setMessage("Please wait...");
		// Set Cancelable as False
		prgDialog.setCancelable(false);
	}

	/**
	 * Method gets triggered when Register button is clicked
	 *
	 * @param view
	 */
	public void registerUser(View view) {
		// Get NAme ET control value
		String name = etName.getText().toString();
		// Get Email ET control value
		String email = etEmail.getText().toString();
		// Get Password ET control value
		String password = etPassword.getText().toString();
		// Instantiate Http Request Param Object
		RequestParams params = new RequestParams();
		// When Name Edit View, Email Edit View and Password Edit View have
		// values other than Null
		if (Utility.isNotNull(name) && Utility.isNotNull(email)
				&& Utility.isNotNull(password)) {
			// When Email entered is Valid
			// if(Utility.validate(email)){
			if (true) {
				// Put Http parameter name with value of Name Edit View control
				params.put("name", name);
				// Put Http parameter username with value of Email Edit View
				// control
				params.put("login", email);
				// Put Http parameter password with value of Password Edit View
				// control
				params.put("password", password);
				// Invoke RESTful Web Service with Http parameters
				restInvoke(params);
			}
			// When Email is invalid
			else {
				Toast.makeText(getApplicationContext(),
						"Please enter valid email", Toast.LENGTH_LONG).show();
			}
		}
		// When any of the Edit View control left blank
		else {
			Toast.makeText(getApplicationContext(),
					"Please fill the form, don't leave any field blank",
					Toast.LENGTH_LONG).show();
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
		client.get(domainAdress + REGISTER, params,
				new AsyncHttpResponseHandler() {
					// When the response returned by REST has Http response code
					// '200'
					@Override
					public void onSuccess(String response) {
						// Hide Progress Dialog
						prgDialog.hide();
						try {
							// JSON Object
							JSONObject jO = new JSONObject(response);
							// When the JSON response has status boolean value
							// assigned with true
							if (jO.getBoolean("status")) {
								// Set Default Values for Edit View controls
								setDefaultValues();
								// Display successfully registered message using
								// Toast
								Toast.makeText(getApplicationContext(),
										"You are successfully registered!",
										Toast.LENGTH_LONG).show();
								navigateToLoginActivity();
							}
							// Else display error message
							else {
								tvErrorMsg.setText(jO.getString("error_msg"));
								Toast.makeText(getApplicationContext(),
										jO.getString("error_msg"),
										Toast.LENGTH_LONG).show();
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

	/**
	 * Method which navigates from Register Activity to Login Activity
	 */
	public void navigateToLoginActivity(View view) {
		prgDialog.dismiss();
		Intent loginIntent = new Intent(RegisterActivity.this,
				LoginActivity.class);
		// Clears History of Activity
		loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(loginIntent);
	}
	
	public void navigateToLoginActivity() {
		prgDialog.dismiss();
		Intent loginIntent = new Intent(RegisterActivity.this,
				LoginActivity.class);
		// Clears History of Activity
		loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(loginIntent);
	}

	/**
	 * Set default values for Edit View controls
	 */
	public void setDefaultValues() {
		etName.setText("");
		etEmail.setText("");
		etPassword.setText("");
	}

	/*@Override
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
*/
}
