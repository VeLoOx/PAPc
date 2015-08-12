package pl.pap.utils;

import pl.pap.client.R;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class ConnectionGuardian {

	private Context context;

	public ConnectionGuardian(Context context) {
		this.context = context;
	}

	public boolean isConnectedToInternet() {
		ConnectivityManager connManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connManager != null) {
			NetworkInfo[] info = connManager.getAllNetworkInfo();
			if (info != null)
				for (int i = 0; i < info.length; i++)
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}

		}
		Toast.makeText(context, R.string.noConnection, Toast.LENGTH_LONG).show();
		return false;
		
	}

}
