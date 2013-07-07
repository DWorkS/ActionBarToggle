/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.dworks.libs.actionbartoggle.demo;

import java.util.Locale;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.SlidingPaneLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;

import dev.dworks.libs.actionbartoggle.ActionBarToggle;


public class SlidingPaneLayoutActivity extends SherlockActivity {
    private SlidingPaneLayout mSlidingPaneLayout;
    private ListView mSliderList;
    private ActionBarToggle mActionBarToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] mPlanetTitles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slider);

        mTitle = mDrawerTitle = getTitle();
        mPlanetTitles = getResources().getStringArray(R.array.planets_array);
        mSlidingPaneLayout = (SlidingPaneLayout) findViewById(R.id.sliding_layout);
        mSliderList = (ListView) findViewById(R.id.sliding_list);

        // set a custom shadow that overlays the main content when the slider opens
        mSlidingPaneLayout.setShadowResource(R.drawable.drawer_shadow);
        // set up the slider's list view with items and click listener
        mSliderList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.list_item, mPlanetTitles));
        mSliderList.setOnItemClickListener(new DrawerItemClickListener());

        // enable ActionBar app icon to behave as action to toggle sliding pane
        getSupportActionBar().setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding pane and the action bar app icon
        mActionBarToggle = new ActionBarToggle(
                this,                  /* host Activity */
                mSlidingPaneLayout,         /* SlidingPaneLayout object */
                R.drawable.ic_drawer_light,  /* sliding pane  image to replace 'Up' caret */
                R.string.slider_open,  /* "open drawer" description for accessibility */
                R.string.slider_close  /* "close drawer" description for accessibility */
                ) {
        	@Override
			public void closeView() {
                getSupportActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
				super.closeView();
			}
        	
        	@Override
			public void openView() {
                getSupportActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
				super.openView();
			}
        };
        mSlidingPaneLayout.setPanelSlideListener(mActionBarToggle);

        if (savedInstanceState == null) {
            selectItem(0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
    	getSupportMenuInflater().inflate(R.menu.main, menu);
    	return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(com.actionbarsherlock.view.Menu menu) {
        // If the sliding pane is open, hide action items related to the content view
        boolean drawerOpen = mSlidingPaneLayout.isOpen();
        menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
    	return super.onPrepareOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(
    		com.actionbarsherlock.view.MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
       if (mActionBarToggle.onOptionsItemSelected(item)) {
           return true;
       }
       // Handle action buttons
       switch(item.getItemId()) {
       case R.id.action_websearch:
           // create intent to perform web search for this planet
           Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
           intent.putExtra(SearchManager.QUERY, getSupportActionBar().getTitle());
           // catch event that there's no activity to handle intent
           if (intent.resolveActivity(getPackageManager()) != null) {
               startActivity(intent);
           } else {
               Toast.makeText(this, R.string.app_not_available, Toast.LENGTH_LONG).show();
           }
           return true;
       default:
           return super.onOptionsItemSelected(item);
       }
    }

    /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
        // update the main content by replacing fragments
        Fragment fragment = new PlanetFragment();
        Bundle args = new Bundle();
        args.putInt(PlanetFragment.ARG_PLANET_NUMBER, position);
        fragment.setArguments(args);

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

        // update selected item and title, then close the slider
        mSliderList.setItemChecked(position, true);
        setTitle(mPlanetTitles[position]);
        mSlidingPaneLayout.closePane();
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mActionBarToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(mActionBarToggle.isDrawerIndicatorEnabled());
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the slider toggls
        mActionBarToggle.onConfigurationChanged(newConfig);
        getSupportActionBar().setDisplayHomeAsUpEnabled(mActionBarToggle.isDrawerIndicatorEnabled());
    }
    
    @Override
    public void onBackPressed() {
    	if(mSlidingPaneLayout.isSlideable() && mSlidingPaneLayout.isOpen()){
    		mSlidingPaneLayout.closePane();
    	}
    	else{
    		super.onBackPressed();
    	}
    }

    /**
     * Fragment that appears in the "content_frame", shows a planet
     */
    public static class PlanetFragment extends Fragment {
        public static final String ARG_PLANET_NUMBER = "planet_number";

        public PlanetFragment() {
            // Empty constructor required for fragment subclasses
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_planet, container, false);
            int i = getArguments().getInt(ARG_PLANET_NUMBER);
            String planet = getResources().getStringArray(R.array.planets_array)[i];

            int imageId = getResources().getIdentifier(planet.toLowerCase(Locale.getDefault()),
                            "drawable", getActivity().getPackageName());
            ((ImageView) rootView.findViewById(R.id.image)).setImageResource(imageId);
            getActivity().setTitle(planet);
            return rootView;
        }
    }
}