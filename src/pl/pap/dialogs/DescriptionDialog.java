package pl.pap.dialogs;

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
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

@SuppressLint("NewApi")
public class DescriptionDialog extends DialogFragment {

/*	public interface DescriptionDialogListener {
		public void onDialogPositiveClick(DialogFragment dialog);

		public void onDialogNegativeClick(DialogFragment dialog);
		
	}

	DescriptionDialogListener dDialogListener;*/
	
	public TextView tvDialogDesription;

	// Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
   /* @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
        	dDialogListener = (DescriptionDialogListener) activity;
        	System.out.println("Dialog: listener added");
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement MarkerDialogListener");
        }
    }*/

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
	
		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		// Get the layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();
		
		View descDialogView =inflater.inflate(R.layout.dialog_description, null);
		tvDialogDesription=(TextView) descDialogView.findViewById(R.id.tvDialogDescription);
		tvDialogDesription.setText("Bardzo fajna trasa wycieszkowa. Mile widoki, ciekawe miejsca. Polecam!");
		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout
		builder.setView(descDialogView)
				// Add action buttons
				.setPositiveButton(R.string.close,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								//dDialogListener.onDialogPositiveClick(DescriptionDialog.this);
								System.out.println("Dialog: positive click");
								DescriptionDialog.this.getDialog().cancel();
							}
						})
				;
		// Create the AlertDialog object and return it
		return builder.create();
	}
}
