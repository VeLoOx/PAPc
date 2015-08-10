package pl.pap.activites.base;

import java.util.ArrayList;

import pl.pap.activites.ChooseRouteActivity;
import pl.pap.activites.HomeActivity;
import pl.pap.activites.PlanRouteActivity;
import pl.pap.activites.ShowRouteActivity;
import pl.pap.activites.StartNewRouteActivity;
import pl.pap.client.R;
import pl.pap.slidemenu.adapter.SlideMenuListAdapter;
import pl.pap.slidemenu.model.SlideMenuItem;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class BaseActivity extends FragmentActivity {
	// slide menu variables
	private DrawerLayout drawerLayout;
	private ListView drawerList;
	protected ActionBarDrawerToggle drawerToggle;

	// nav drawer title
	private CharSequence drawerTitle;

	// used to store app title
	private CharSequence title;

	// slide menu items
	private String[] slideMenuTitles;
	private TypedArray slideMenuIcons;

	private ArrayList<SlideMenuItem> slideMenuItems;
	private SlideMenuListAdapter slideMenuAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.slider_menu_layout);
		// setUpSlideMenu();
	}

	/**
	 * Method for setting up slide menu
	 */
	protected void setUpSlideMenu() {
		title = drawerTitle = getTitle();

		// load slide menu items
		slideMenuTitles = getResources().getStringArray(
				R.array.slider_menu_items);

		// drawer icons from resources
		slideMenuIcons = getResources().obtainTypedArray(
				R.array.slider_menu_icons);

		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawerList = (ListView) findViewById(R.id.listView_sliderMenu);

		slideMenuItems = new ArrayList<SlideMenuItem>();

		// adding drawer items to array
		// Home
		slideMenuItems.add(new SlideMenuItem(slideMenuTitles[0], slideMenuIcons
				.getResourceId(0, -1)));
		// Plan Route
		slideMenuItems.add(new SlideMenuItem(slideMenuTitles[1], slideMenuIcons
				.getResourceId(1, -1)));
		// Chose Route
		slideMenuItems.add(new SlideMenuItem(slideMenuTitles[2], slideMenuIcons
				.getResourceId(2, -1)));
		// Start New Route
		slideMenuItems.add(new SlideMenuItem(slideMenuTitles[3], slideMenuIcons
				.getResourceId(3, -1)));

		// Recycle the typed array
		slideMenuIcons.recycle();

		drawerList.setOnItemClickListener(new SlideMenuClickListener());

		// setting the drawer list adapter
		slideMenuAdapter = new SlideMenuListAdapter(getApplicationContext(),
				slideMenuItems);
		drawerList.setAdapter(slideMenuAdapter);

		// enabling action bar app icon and behaving it as toggle button
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
		// R.drawable.ic_drawer, // menu toggle icon
				R.string.app_name, // drawer open - description for
									// accessibility
				R.string.app_name // drawer close - description for
									// accessibility
		) {
			public void onDrawerClosed(View view) {
				getActionBar().setTitle(title);
				// calling onPrepareOptionsMenu() to show action bar icons
				invalidateOptionsMenu();
			}

			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle(drawerTitle);
				// calling onPrepareOptionsMenu() to hide action bar icons
				invalidateOptionsMenu();
			}
		};
		drawerLayout.setDrawerListener(drawerToggle);
		// if (savedInstanceState == null) {
		// on first time display view for first nav item
		// displayView(0);

		// }

	}

	/**
	 * Slide menu item click listener
	 * */
	class SlideMenuClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// display selected activity
			selectSlideMenuItem(position);

		}
	}

	private void selectSlideMenuItem(int position) {
		// update the main content by replacing fragments
		switch (position) {
		case 0:
			navigateToHomeActivity();
			break;
		case 1:
			navigateToPlanRouteActivity();
			break;
		case 2:
			navigateToChooseRouteActivity();
			break;
		case 3:
			navigateToStartNewRouteActivity();
			break;
		default:
			break;
		}
		drawerLayout.closeDrawer(drawerList);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// getSupportMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		/*
		 * if (item.getItemId() == android.R.id.home) {
		 * System.out.println("Home button pressed"); if
		 * (drawerLayout.isDrawerOpen(drawerList)) {
		 * System.out.println("Drawer otwarty - zamykam");
		 * drawerLayout.closeDrawer(drawerList); } else {
		 * //drawerLayout.openDrawer(drawerList); } }
		 */

		if (drawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	/***
	 * Called when invalidateOptionsMenu() is triggered
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// if nav drawer is opened, hide the action items
		// boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
		// menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void setTitle(CharSequence title) {
		this.title = title;
		getActionBar().setTitle(this.title);
	}

	/**
	 * When using the ActionBarDrawerToggle, you must call it during
	 * onPostCreate() and onConfigurationChanged()...
	 */

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		drawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		drawerToggle.onConfigurationChanged(newConfig);
	}

	public void navigateToHomeActivity() {
		// prgDialog.dismiss();
		Intent homeIntent = new Intent(getApplicationContext(),
				HomeActivity.class);
		homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(homeIntent);
	}

	public void navigateToPlanRouteActivity(View view) {
		Intent planRouteIntent = new Intent(getApplicationContext(),
				PlanRouteActivity.class);
		planRouteIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(planRouteIntent);
	}

	public void navigateToPlanRouteActivity() {
		Intent planRouteIntent = new Intent(getApplicationContext(),
				PlanRouteActivity.class);
		planRouteIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(planRouteIntent);
	}

	public void navigateToStartNewRouteActivity(View view) {
		Intent startRouteIntent = new Intent(getApplicationContext(),
				StartNewRouteActivity.class);
		startRouteIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(startRouteIntent);
	}

	public void navigateToStartNewRouteActivity() {
		Intent startRouteIntent = new Intent(getApplicationContext(),
				StartNewRouteActivity.class);
		startRouteIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(startRouteIntent);
	}

	public void navigateToShowRouteActivity(View view) {
		Intent showRouteIntent = new Intent(getApplicationContext(),
				ShowRouteActivity.class);
		showRouteIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(showRouteIntent);
	}

	public void navigateToChooseRouteActivity(View view) {
		Intent chooseRouteIntent = new Intent(getApplicationContext(),
				ChooseRouteActivity.class);
		chooseRouteIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(chooseRouteIntent);
	}

	public void navigateToChooseRouteActivity() {
		Intent chooseRouteIntent = new Intent(getApplicationContext(),
				ChooseRouteActivity.class);
		chooseRouteIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(chooseRouteIntent);
	}
}