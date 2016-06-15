package com.kareem_adel.environmentmaps;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.idunnololz.widgets.AnimatedExpandableListView;
import com.ikimuhendis.ldrawer.ActionBarDrawerToggle;
import com.ikimuhendis.ldrawer.DrawerArrowDrawable;
import com.kareem_adel.environmentmaps.OptionItem.OptionsItem;
import com.kareem_adel.environmentmaps.OptionItem.OptionsItemHolder;

import java.util.ArrayList;

public class Navigator extends Activity {
    static Navigator activity;

    ActionBarDrawerToggle actionBarDrawerToggle;
    DrawerArrowDrawable drawerArrow;
    DrawerLayout drawerLayout;


    IndoorMap indoorMap;
    AnimatedExpandableListView leftDrawerListView;
    OptionsItemsAdapter optionsItemsAdapter;
    FrameLayout navigator_content_frame;
    public UserSettings userSettings;

    public Sensor mSensor;
    public SensorManager mSensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigator);

        activity = this;

        navigator_content_frame = (FrameLayout) findViewById(R.id.navigator_content_frame);

        userSettings = loadPreferences();

        indoorMap = new IndoorMap(this);
        navigator_content_frame.addView(indoorMap);
        indoorMap.init();

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerArrow = new DrawerArrowDrawable(this) {
            @Override
            public boolean isLayoutRtl() {
                return false;
            }
        };
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, drawerArrow, R.string.OpenDrawer, R.string.CloseDrawer) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
            }
        };
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        actionBarDrawerToggle.setAnimateEnabled(true);
        drawerArrow.setColor(R.color.ldrawer_color);


        leftDrawerListView = (AnimatedExpandableListView) findViewById(R.id.leftDrawer);
        leftDrawerListView.setDividerHeight(0);

        optionsItemsAdapter = new OptionsItemsAdapter();
        leftDrawerListView.setAdapter(optionsItemsAdapter);
        optionsItemsAdapter.optionsItems.add(new OptionsItem("Navigation Mode", R.drawable.none, new ArrayList<OptionsItem>() {{
            add(new OptionsItem("Basic", R.drawable.ic_basic, null));
            add(new OptionsItem("Advanced", R.drawable.ic_advanced, null));
        }}));
        optionsItemsAdapter.optionsItems.add(new OptionsItem("Preferences", R.drawable.none, new ArrayList<OptionsItem>()));
        optionsItemsAdapter.notifyDataSetChanged();

        leftDrawerListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                String OptionsItem_Name = ((TextView) v.findViewById(R.id.OptionsItem_Name)).getText().toString();

                switch (OptionsItem_Name) {
                    case "Basic": {
                        userSettings.NavigationMode = "fastMode";
                        indoorMap.SourceNode = null;
                        indoorMap.DestinationNode = null;
                        GraphMap.InitSlowGraph();
                        break;
                    }
                    case "Advanced": {
                        userSettings.NavigationMode = "slowMode";
                        indoorMap.SourceNode = null;
                        indoorMap.DestinationNode = null;
                        GraphMap.InitSlowGraph();
                        break;
                    }
                }
                savePreferences();
                drawerLayout.closeDrawers();
                return true;
            }
        });

        startBTAdapter();
    }

    public void savePreferences() {
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(userSettings, new TypeToken<UserSettings>() {
        }.getType());
        prefsEditor.putString("Preferences", json);
        prefsEditor.commit();
    }

    public static UserSettings loadPreferences() {
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(activity);
        Gson gson = new Gson();
        String json = mPrefs.getString("Preferences", "");
        if (!json.equals("")) {
            return (UserSettings) gson.fromJson(json, new TypeToken<UserSettings>() {
            }.getType());
        } else {
            return new UserSettings();
        }
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        actionBarDrawerToggle.syncState();
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        actionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return actionBarDrawerToggle.onOptionsItemSelected(item);
    }

    public class OptionsItemsAdapter extends AnimatedExpandableListView.AnimatedExpandableListAdapter {

        ArrayList<OptionsItem> optionsItems = new ArrayList<>();

        @Override
        public int getGroupCount() {
            return optionsItems.size();
        }

        @Override
        public int getRealChildrenCount(int groupPosition) {
            return optionsItems.get(groupPosition).optionsItems.size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return optionsItems.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return optionsItems.get(groupPosition).optionsItems.get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) Navigator.this.getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.options_item, parent, false);
                convertView.setTag(new OptionsItemHolder(convertView));
            }
            OptionsItem optionsItem = optionsItems.get(groupPosition);
            OptionsItemHolder optionsItemsHolder = (OptionsItemHolder) convertView.getTag();
            optionsItemsHolder.OptionsItem_Name.setText(optionsItem.OptionName);

            return convertView;
        }

        @Override
        public View getRealChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

            LayoutInflater inflater = (LayoutInflater) Navigator.this.getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v;

            if (optionsItemsAdapter.optionsItems.get(groupPosition).OptionName.equals("Preferences")) {
                v = inflater.inflate(R.layout.options_item_child_checkbox, parent, false);
                OptionsItemHolder optionsItemHolder = new OptionsItemHolder(v);
                final OptionsItem optionsItem = optionsItems.get(groupPosition).optionsItems.get(childPosition);
                optionsItemHolder.OptionsItem_Name.setText(optionsItem.OptionName);
                if (indoorMap.userPrefs.contains(optionsItem.OptionName))
                    optionsItemHolder.OptionsItem_checkbox.setChecked(true);
                else
                    optionsItemHolder.OptionsItem_checkbox.setChecked(false);
                optionsItemHolder.OptionsItem_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            if (!indoorMap.userPrefs.contains(optionsItem.OptionName)) {
                                indoorMap.userPrefs.add(optionsItem.OptionName);
                            }
                        } else {
                            indoorMap.userPrefs.remove(optionsItem.OptionName);
                        }
                        savePreferences();
                    }
                });
            } else {
                v = inflater.inflate(R.layout.options_item_child, parent, false);
                OptionsItemHolder optionsItemHolder = new OptionsItemHolder(v);
                final OptionsItem optionsItem = optionsItems.get(groupPosition).optionsItems.get(childPosition);
                optionsItemHolder.OptionsItem_Name.setText(optionsItem.OptionName);
                optionsItemHolder.OptionsItem_Image.setImageResource(optionsItem.OptionImage);
            }

            return v;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }


    public BluetoothAdapter mBluetoothAdapter = null;
    public final int REQUEST_ENABLE_BT = 2;

    public void startBTAdapter() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            return;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
            Toast.makeText(this, "Bluetooth Connected", Toast.LENGTH_LONG).show();
            //start service
            startService(new Intent(this, BlueCloudService.class));
        }
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    protected void onDestroy() {
        if (isFinishing()) {
            stopBTAdapter();
            stopService(new Intent(this, BlueCloudService.class));
        }

        super.onDestroy();
    }

    public void stopBTAdapter() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            return;
        }
        if (mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.cancelDiscovery();
            mBluetoothAdapter.disable();
        }
        Toast.makeText(this, "Bluetooth Disconnected", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ENABLE_BT: {
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "Bluetooth is enabled successfully !", Toast.LENGTH_SHORT).show();
                    startBTAdapter();
                } else {
                    Toast.makeText(this, "Bluetooth is NOT enabled successfully", Toast.LENGTH_SHORT).show();
                    stopBTAdapter();
                }
                break;
            }
        }
    }


}
