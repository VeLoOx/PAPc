package pl.pap.REST;

import org.json.JSONException;
import org.json.JSONObject;

import pl.pap.utils.Consts;
import android.app.ProgressDialog;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class RestClient implements Consts {
	public JSONObject invokeWS(RequestParams params,
			final ProgressDialog prgDialog) {
		// Show Progress Dialog
		prgDialog.show();
		JSONObject object;
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
							JSONObject object = new JSONObject(answer);

						} catch (JSONException e) {
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
						
					}
						
				});
		return null;
	}
}
