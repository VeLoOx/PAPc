package pl.pap.activites;

import java.util.ArrayList;
import java.util.Arrays;

import pl.pap.client.R;
import android.support.v7.app.ActionBarActivity;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ChooseRouteActivity extends Activity {

	private ListView list;
	private ArrayAdapter<String> adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.choose_route);
		
		list = (ListView) findViewById(R.id.lV1);
		 
        String routes[] = {"Kraków- Stare Miasto", "Kielce- Pomniki przyrody"};
 
        ArrayList<String> routess = new ArrayList<String>();  
        routess.addAll( Arrays.asList(routes) );  
 
        adapter = new ArrayAdapter<String>(this, R.layout.list_chose_route_item, routess);
 
        list.setAdapter(adapter);
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
}
