package pl.pap.routeslist.adapter;

import pl.pap.client.R;
import pl.pap.model.Route;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class RouteListAdapter extends ArrayAdapter<Route> {

	private int layoutResourceId;
	public RouteListAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
		layoutResourceId = textViewResourceId;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		try {
			Route item = getItem(position);
			View v = null;
			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) getContext()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

				v = inflater.inflate(layoutResourceId, null);

			} else {
				v = convertView;
			}

			TextView routeAutor = (TextView) v.findViewById(R.id.route_author);
			TextView routeName = (TextView) v.findViewById(R.id.route_name);

			routeAutor.setText(item.getAuthor());
			routeName.setText(item.getName());

			return v;
		} catch (Exception ex) {
			return null;
		}
	}
}
