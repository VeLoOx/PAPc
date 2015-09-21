package pl.pap.dialogs;

import pl.pap.client.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.provider.MediaStore.Audio.Radio;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class CriteriaDialog extends DialogFragment {
	
	public interface CriteriaDialogListener{
		
		public void onCriteriaDialogPositiveClick(DialogFragment dialog);

		public void onCriteriaDialogNegativeClick(DialogFragment dialog);
	}
	
	CriteriaDialogListener cDialogListener;
	
	public RadioGroup rgSelectMode;
	public CheckBox cbCity, cbAuthor;
	public EditText etCriteriaCity;
	public EditText etCriteriaAuthor;
	public RadioButton rbShowAll;
	public RadioButton rbShowMy;
	public RadioButton rbSelected;
	public String criteriaCity;
	public String criteriaAuthor;
	
	// Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
        	cDialogListener = (CriteriaDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement CriteriaDialogListener");
        }
    }
    
    
    
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
	
		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		// Get the layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();
		
		final View criteriaDialogView =inflater.inflate(R.layout.dialog_criteria, null);
		rgSelectMode = (RadioGroup) criteriaDialogView.findViewById(R.id.rgSelectMode);
		cbAuthor = (CheckBox) criteriaDialogView.findViewById(R.id.cbAuthor);
		cbCity = (CheckBox) criteriaDialogView.findViewById(R.id.cbCity);
		etCriteriaCity= (EditText) criteriaDialogView.findViewById(R.id.etCriteriaCity);
		etCriteriaAuthor=(EditText) criteriaDialogView.findViewById(R.id.etCriteriaAuthor);
		rbShowMy=(RadioButton) criteriaDialogView.findViewById(R.id.rbShowyMy);
		rbShowAll=(RadioButton) criteriaDialogView.findViewById(R.id.rbShowAll);
		
		
		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout
		builder.setView(criteriaDialogView)
				// Add action buttons
				.setPositiveButton(R.string.select,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {								
								int selectedRadio;
								selectedRadio = rgSelectMode.getCheckedRadioButtonId();
								rbSelected=(RadioButton) criteriaDialogView.findViewById(selectedRadio);
								criteriaCity=etCriteriaCity.getText().toString();
								criteriaAuthor=etCriteriaAuthor.getText().toString();
								cDialogListener.onCriteriaDialogPositiveClick(CriteriaDialog.this);
							}
						})
				.setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								cDialogListener.onCriteriaDialogNegativeClick(CriteriaDialog.this);
								CriteriaDialog.this.getDialog().cancel();
							}
						});
		// Create the AlertDialog object and return it
		return builder.create();
	}
	
}
