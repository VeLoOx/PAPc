package pl.pap.actionbar.adapter;

import java.util.ArrayList;

import pl.pap.actiobar.model.SpinnerNavigationItem;
import pl.pap.client.R;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SpinnerNavigationAdapter extends BaseAdapter {
	 
    private ImageView imgIcon;
    private TextView txtTitle;
    private ArrayList<SpinnerNavigationItem> spinnerNavigationItem;
    private Context context;
 
    public SpinnerNavigationAdapter(Context context,
            ArrayList<SpinnerNavigationItem> spinnerNavigationItem) {
        this.spinnerNavigationItem = spinnerNavigationItem;
        this.context = context;
    }
    
     public SpinnerNavigationAdapter(Context context) {
    	 spinnerNavigationItem = new ArrayList<SpinnerNavigationItem>();		//Wstawiæ odpowiednie ikonki
    	 spinnerNavigationItem.add(new SpinnerNavigationItem("Home", R.drawable.ic_launcher));
    	 spinnerNavigationItem.add(new SpinnerNavigationItem("Plan route", R.drawable.ic_launcher));
    	 spinnerNavigationItem.add(new SpinnerNavigationItem("Start new route", R.drawable.ic_launcher));
    	 spinnerNavigationItem.add(new SpinnerNavigationItem("Chose route", R.drawable.ic_launcher));
    	 this.context = context;
	}
 
    @Override
    public int getCount() {
        return spinnerNavigationItem.size();
    }
 
    @Override
    public Object getItem(int index) {
        return spinnerNavigationItem.get(index);
    }
 
    @Override
    public long getItemId(int position) {
        return position;
    }
 
    @Override
    public View getView(int position, View convertView, ViewGroup parent) { 
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.list_spinner_navigation_item, null);
        }
         
        imgIcon = (ImageView) convertView.findViewById(R.id.imgIcon);
        txtTitle = (TextView) convertView.findViewById(R.id.txtTitle);
         
        imgIcon.setImageResource(spinnerNavigationItem.get(position).getIcon());
        imgIcon.setVisibility(View.GONE);
        txtTitle.setText(spinnerNavigationItem.get(position).getTitle());
        return convertView;
    }
     
 
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.list_spinner_navigation_item, null);
        }
         
        imgIcon = (ImageView) convertView.findViewById(R.id.imgIcon);
        txtTitle = (TextView) convertView.findViewById(R.id.txtTitle);
         
        imgIcon.setImageResource(spinnerNavigationItem.get(position).getIcon());        
        txtTitle.setText(spinnerNavigationItem.get(position).getTitle());
        return convertView;
    }
 

}
