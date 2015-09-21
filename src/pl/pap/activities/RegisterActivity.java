package pl.pap.activities;

import org.json.JSONException;
import org.json.JSONObject;

import pl.pap.client.R;
import pl.pap.utils.ConnectionGuardian;
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
	EditText etUsername;
	// Passwprd Edit View Object
	EditText etPwd;

	//
	ConnectionGuardian connGuard;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		connGuard = new ConnectionGuardian(this);
		// Find Error Msg Text View control by ID
		tvErrorMsg = (TextView) findViewById(R.id.registerError);
		// Find Name Edit View control by ID
		etUsername = (EditText) findViewById(R.id.registerUsername);
		// Find Password Edit View control by ID
		etPwd = (EditText) findViewById(R.id.registerPassword);
		// Instantiate Progress Dialog object
		prgDialog = new ProgressDialog(this);
		// Set Progress Dialog Text
		prgDialog.setMessage("Please wait...");
		// Set Cancelable as False
		prgDialog.setCancelable(false);
	}

	public void registerUser(View view) {
		if (connGuard.isConnectedToInternet()) {
			// Get NAme ET control value
			String userName = etUsername.getText().toString();
			// Get Password ET control value
			String password = etPwd.getText().toString();
			// Instantiate Http Request Param Object
			RequestParams params = new RequestParams();
			// When Name Edit View, Email Edit View and Password Edit View have
			// values other than Null
			if (Utility.isNotNull(userName) && Utility.isNotNull(password)) {
				// When Email entered is Valid
				if (Utility.isNotTooShort(userName)
						&& Utility.isNotTooShort(password)) {
					params.put(Consts.PARAM_LOGIN, userName);
					// Put Http parameter password with value of Password Edit
					// View
					// control
					params.put(Consts.PARAM_PASSWORD, password);
					// Invoke RESTful Web Service with Http parameters
					restInvoke(params);
				}
				// When Email is invalid
				else {
					Toast.makeText(getApplicationContext(), R.string.tooShort,
							Toast.LENGTH_LONG).show();
				}

			}
			// When any of the Edit View control left blank
			else {
				Toast.makeText(getApplicationContext(), R.string.emptyField,
						Toast.LENGTH_LONG).show();
			}
		}
	}

	public void restInvoke(RequestParams params) {
		// Show Progress Dialog
		prgDialog.show();
		// Make RESTful webservice call using AsyncHttpClient object
		AsyncHttpClient client = new AsyncHttpClient();
		client.post(domainAdress + REGISTER, params,
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
							if (jO.getBoolean(Consts.MSG_STATUS)) {
								// Set Default Values for Edit View controls
								setDefaultValues();
								// Display successfully registered message using
								// Toast
								Toast.makeText(getApplicationContext(),
										R.string.registerSucces,
										Toast.LENGTH_LONG).show();
								navigateToLoginActivity();
							}
							// Else display error message
							else {
								tvErrorMsg.setText(jO.getString(Consts.MSG_INFO));
								Toast.makeText(getApplicationContext(),
										jO.getString(Consts.MSG_INFO),
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

	private void setDefaultValues() {
		etUsername.setText("");
		etPwd.setText("");
	}
}
