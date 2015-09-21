package pl.pap.activities;

import java.io.IOException;
import java.util.List;

import pl.pap.activities.map.MapActivity;
import pl.pap.client.R;
import pl.pap.model.Route;
import pl.pap.utils.ConnectionGuardian;
import pl.pap.utils.OfflineModeManager;
import pl.pap.utils.SharedPrefsUtils;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

public class PlanRouteActivity extends MapActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_plan_route);
		try {
			route = new Route();
			// Loading map
			initializeMap();
			// Set up map methods
			setUpMap();
			setUpListeners();
			prefs = new SharedPrefsUtils(this);
			connGuard = new ConnectionGuardian(this);
			offManager = new OfflineModeManager(this);

		} catch (Exception e) {
			e.printStackTrace();
		}
		setUpSlideMenu();

		handleIntent(getIntent());

	}

	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		handleIntent(intent);
	}

	private void handleIntent(Intent intent) {
		if (connGuard.isConnectedToInternet()) {
			if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
				String query = intent.getStringExtra(SearchManager.QUERY);
				new SearchCityTask().execute(query);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.plan_route_menu, menu);

		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView = (SearchView) menu.findItem(R.id.action_lookUp)
				.getActionView();
		searchView.setSearchableInfo(searchManager
				.getSearchableInfo(getComponentName()));
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

		switch (item.getItemId()) {

		case R.id.item1:
			deciedeToSave();
			break;
		case R.id.item2:

			toPersist = true;
			fillRouteInfo();
			break;
		case R.id.item3:

			toPersist = false;
			fillRouteInfo();
			break;
		default:

		}

		return super.onOptionsItemSelected(item);
	}

	private void deciedeToSave() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(
				PlanRouteActivity.this);

		// Setting Dialog Title
		alertDialog.setTitle(R.string.wantSaveTitle);

		// Setting Dialog Message
		alertDialog.setMessage(R.string.wantSaveTitle);

		// Setting Icon to Dialog
		alertDialog.setIcon(R.drawable.ic_action_help);

		// Setting Positive "Yes" Button
		alertDialog.setPositiveButton(R.string.yes,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {

						// saveRoute();
						cleanMap();

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

	// An AsyncTask class for accessing the GeoCoding Web Service
	private class SearchCityTask extends
			AsyncTask<String, Void, List<Address>> {

		@Override
		protected List<Address> doInBackground(String... locationName) {
			// Creating an instance of Geocoder class
			Geocoder geocoder = new Geocoder(getBaseContext());
			List<Address> addresses = null;

			try {
				// Getting a maximum of 3 Address that matches the input text
				addresses = geocoder.getFromLocationName(locationName[0], 3);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return addresses;
		}

		@Override
		protected void onPostExecute(List<Address> addresses) {

			if (addresses == null || addresses.size() == 0) {
				Toast.makeText(getBaseContext(), "No Location found",
						Toast.LENGTH_SHORT).show();
			}
				Address address = (Address) addresses.get(0);
				// Creating an instance of GeoPoint
				LatLng latLng = new LatLng(address.getLatitude(),
						address.getLongitude());

				// Locate the first location
					CameraPosition cameraPosition = new CameraPosition.Builder()
							.target(latLng).zoom(10).build();
					googleMap.animateCamera(CameraUpdateFactory
							.newCameraPosition(cameraPosition));			
		}
	}

}
