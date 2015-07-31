package pl.pap.dialogs;

import com.google.android.gms.maps.model.Marker;

import pl.pap.client.R;
import pl.pap.client.R.id;
import pl.pap.client.R.layout;
import pl.pap.client.R.string;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

@SuppressLint("NewApi")
public class MarkerDialog extends DialogFragment {
	
	public MarkerDialog(Marker currMarker){
		this.currMarker=currMarker;
	}

	public interface MarkerDialogListener {
		public void onMarkerDialogPositiveClick(DialogFragment dialog);

		public void onMarkerDialogNegativeClick(DialogFragment dialog);
		
	}

	MarkerDialogListener mDialogListener;
	
	Marker currMarker;
	
	EditText etMarkerTitle;
	EditText etMarkerSnippet;
	public String markerTitle;
	public String markerSnippet;
	
	public Button btnDeleteMarker;
	
	public boolean isDeletable=false;
	
	// Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
        	mDialogListener = (MarkerDialogListener) activity;
        	System.out.println("Dialog: listener added");
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement MarkerDialogListener");
        }
    }

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
	
		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		// Get the layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();
		
		View markDialogView =inflater.inflate(R.layout.dialog_marker, null);
		etMarkerTitle= (EditText) markDialogView.findViewById(R.id.markerTitle);
		etMarkerSnippet= (EditText)markDialogView.findViewById(R.id.markerSnippet);
		
		//etMarkerTitle.setText(currMarker.getTitle());
		//etMarkerSnippet.setText(currMarker.getSnippet());
		
		btnDeleteMarker=(Button) markDialogView.findViewById(R.id.markerDelete);
		
		btnDeleteMarker.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				System.out.println("Dialog: delete");	
				isDeletable=true;
				MarkerDialog.this.getDialog().cancel();
			}
		});
		
		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout
		builder.setView(markDialogView)
				// Add action buttons
				.setPositiveButton(R.string.set,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								markerTitle=etMarkerTitle.getText().toString();
								markerSnippet=etMarkerSnippet.getText().toString();
								mDialogListener.onMarkerDialogPositiveClick(MarkerDialog.this);
								System.out.println("Dialog: positive click");
							}
						})
				.setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								System.out.println("Dialog: negative click");
								mDialogListener.onMarkerDialogNegativeClick(MarkerDialog.this);
								MarkerDialog.this.getDialog().cancel();
							}
						});
		// Create the AlertDialog object and return it
		return builder.create();
	}
}
