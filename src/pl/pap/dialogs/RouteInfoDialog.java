package pl.pap.dialogs;

import pl.pap.client.R;
import pl.pap.dialogs.SaveRouteDialog.SaveRouteDialogListener;
import pl.pap.model.Route;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class RouteInfoDialog extends DialogFragment {
	public interface RouteInfoDialogListener {
		public void onRouteInfoDialogPositiveClick(DialogFragment dialog);

		// public void onRouteInfoDialogNegativeClick(DialogFragment dialog);

	}

	public RouteInfoDialog(Route route) {
		this.route = route;
	}

	RouteInfoDialogListener iDialogListener;

	EditText etRouteName;
	EditText etRouteCity;
	EditText etRouteDescription;
	public String routeName;
	public String routeCity;
	public String routeDescription;

	Route route;

	private void fillFields() {
		etRouteName.setText(route.getName());
		etRouteCity.setText(route.getCity());
		etRouteDescription.setText(route.getDescription());
		
		etRouteName.setClickable(false);
		etRouteName.setCursorVisible(false);
		etRouteName.setFocusable(false);
		
		etRouteCity.setClickable(false);
		etRouteCity.setCursorVisible(false);
		etRouteCity.setFocusable(false);
		
		etRouteDescription.setClickable(false);
		etRouteDescription.setCursorVisible(false);
		etRouteDescription.setFocusable(false);
		
	}

	// Override the Fragment.onAttach() method to instantiate the
	// NoticeDialogListener
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// Verify that the host activity implements the callback interface
		try {
			// Instantiate the NoticeDialogListener so we can send events to the
			// host
			iDialogListener = (RouteInfoDialogListener) activity;
			System.out.println("Route Dialog: listener added");
		} catch (ClassCastException e) {
			// The activity doesn't implement the interface, throw exception
			throw new ClassCastException(activity.toString()
					+ " must implement RouteInfoDialogListener");
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		// Get the layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();

		View routeDialogView = inflater.inflate(R.layout.dialog_route_info,
				null);
		etRouteName = (EditText) routeDialogView.findViewById(R.id.etRouteName);
		etRouteCity = (EditText) routeDialogView.findViewById(R.id.etRouteCity);
		etRouteDescription = (EditText) routeDialogView
				.findViewById(R.id.etRouteDescription);

		fillFields();
		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout
		builder.setView(routeDialogView)
		// Add action buttons
				.setPositiveButton(R.string.close,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								routeName = etRouteName.getText().toString();
								routeCity = etRouteCity.getText().toString();
								routeDescription = etRouteDescription.getText()
										.toString();
								iDialogListener
										.onRouteInfoDialogPositiveClick(RouteInfoDialog.this);
								System.out
										.println("RouteInfoDialog: positive click");
							}
						});
		// Create the AlertDialog object and return it
		return builder.create();
	}
}
