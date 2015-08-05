package pl.pap.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class OfflineModeManager implements Consts {
	Context context;
	//SharedPreferences prefs;
	Editor editor;
	SharedPrefsUtils prefs;

	public OfflineModeManager(Context context) {
		this.context = context;
		//prefs = this.context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
		prefs=new SharedPrefsUtils(context);
	}

	public boolean saveRoute(String route) {
		int i = 1;
		for (i = 1; i <= ROUTE_MEMORY_SPACE+1; i++) {
			if (!prefs.checkRoute(i)) {
				System.out.println(ROUTE_KEY + i);
				break;
			}

			if (i > ROUTE_MEMORY_SPACE) {

				System.out
						.println("OfflineMode: no space availabale for another ROUTE");
				return false;
			}
		}

		/*editor = prefs.edit();
		editor.putString(ROUTE_KEY + i, route);
		editor.commit();*/
		prefs.setRoute(i, route);
		System.out.println("OfflineMode: ROUTE saved into SharedPrefs");
		System.out.println("Value check: " + "Key: " + ROUTE_KEY + i + " "
				+prefs.getRoute(i));
		return true;
	}

	public boolean isSmthToPersist() {
		int i = 1;
		for (i = 1; i <= ROUTE_MEMORY_SPACE; i++) {
			if (prefs.checkRoute(i)) {
				//System.out.println(ROUTE_KEY + i);
				return true;
			}
		}
		System.out
				.println("OfflineMode: nothin to PERSIST");
		return false;

	}
}
