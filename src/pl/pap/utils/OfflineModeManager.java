package pl.pap.utils;

import pl.pap.client.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.widget.Toast;

public class OfflineModeManager implements Consts {
	Context context;
	Editor editor;
	SharedPrefsUtils prefs;

	public OfflineModeManager(Context context) {
		this.context = context;
		prefs=new SharedPrefsUtils(context);
	}

	public boolean storeRoute(String route) {
		int i = 1;
		for (i = 1; i <= ROUTE_MEMORY_SPACE+1; i++) {
			if (!prefs.checkRoute(i)) {
				break;
			}

			if (i > ROUTE_MEMORY_SPACE) {
				return false;
			}
		}
		prefs.setRoute(i, route);
		return true;
	}
	
	public void storeRoutesList(String routesList) {
		if (prefs.checkRoutesList()) {
			prefs.removeRoutesList();
		}
		prefs.setRoutesList(routesList);
	}

	public boolean isSmthToPersist() {
		int i = 1;
		for (i = 1; i <= ROUTE_MEMORY_SPACE; i++) {
			if (prefs.checkRoute(i)) {
				return true;
			}
		}
		Toast.makeText(context, R.string.nothingToPersist, Toast.LENGTH_LONG).show();
		return false;

	}
}
