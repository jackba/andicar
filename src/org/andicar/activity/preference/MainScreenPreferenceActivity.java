/*
 *  AndiCar - a car management software for Android powered devices.
 * 
 *  Copyright (C) 2010 Miklos Keresztes (miklos.keresztes@gmail.com)
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.andicar.activity.preference;

import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.andicar.activity.BaseActivity;
import org.andicar.activity.R;
import org.andicar.utils.AndiCarStatistics;


/**
 *
 * @author miki
 */
public class MainScreenPreferenceActivity extends BaseActivity {

    private ListView lvZones;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mPrefEditor = mPreferences.edit();
        setContentView( R.layout.main_screen_preference_activity );
        fillZonesList();
    }

    private void fillZonesList() {
        ArrayList<String> mainScreenZones = new ArrayList<String>();

        mainScreenZones.add("Show mileage zone");
        mainScreenZones.add("Show GPS track zone");
        mainScreenZones.add("Show refuel zone");
        mainScreenZones.add("Show expense zone");
        mainScreenZones.add("Show statistics");
        ArrayAdapter<String> listAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, mainScreenZones);

        lvZones = (ListView) findViewById(R.id.lvZones);
        lvZones.setAdapter(listAdapter);
        lvZones.setItemsCanFocus(false);
        lvZones.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        lvZones.setOnItemClickListener(zonesSelectedListener);
        lvZones.setItemChecked(0, mPreferences.getBoolean("MainActivityShowMileage", true));
        lvZones.setItemChecked(1, mPreferences.getBoolean("MainActivityShowGPSTrack", true));
        lvZones.setItemChecked(2, mPreferences.getBoolean("MainActivityShowRefuel", true));
        lvZones.setItemChecked(3, mPreferences.getBoolean("MainActivityShowExpense", true));
        lvZones.setItemChecked(4, mPreferences.getBoolean("MainActivityShowStatistics", true));
    }

    protected AdapterView.OnItemClickListener zonesSelectedListener = new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                SparseBooleanArray checkedItems = lvZones.getCheckedItemPositions();
                if(checkedItems.valueAt(0))
                    mPrefEditor.putBoolean("MainActivityShowMileage", true);
                else
                    mPrefEditor.putBoolean("MainActivityShowMileage", false);
                if(checkedItems.valueAt(1))
                    mPrefEditor.putBoolean("MainActivityShowGPSTrack", true);
                else
                    mPrefEditor.putBoolean("MainActivityShowGPSTrack", false);
                if(checkedItems.valueAt(2))
                    mPrefEditor.putBoolean("MainActivityShowRefuel", true);
                else
                    mPrefEditor.putBoolean("MainActivityShowRefuel", false);
                if(checkedItems.valueAt(3))
                    mPrefEditor.putBoolean("MainActivityShowExpense", true);
                else
                    mPrefEditor.putBoolean("MainActivityShowExpense", false);
                if(checkedItems.valueAt(4))
                    mPrefEditor.putBoolean("MainActivityShowStatistics", true);
                else
                    mPrefEditor.putBoolean("MainActivityShowStatistics", false);
                
                mPrefEditor.commit();
                if(isSendStatistics){
                    Map<String, String> parameters = new HashMap<String, String>();
                    parameters.put("MainActivityShowMileage", 
                            (checkedItems.valueAt(0) ? "True" : "False"));
                    parameters.put("MainActivityShowGPSTrack",
                            (checkedItems.valueAt(1) ? "True" : "False"));
                    parameters.put("MainActivityShowRefuel",
                            (checkedItems.valueAt(2) ? "True" : "False"));
                    parameters.put("MainActivityShowExpense",
                            (checkedItems.valueAt(3) ? "True" : "False"));
                    parameters.put("MainActivityShowStatistics",
                            (checkedItems.valueAt(4) ? "True" : "False"));
                    AndiCarStatistics.sendFlurryEvent("BackupRestore", parameters);
                }
            }
        };

}
