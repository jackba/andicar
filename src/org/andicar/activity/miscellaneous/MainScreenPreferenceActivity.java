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

package org.andicar.activity.miscellaneous;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.util.ArrayList;
import org.andicar.activity.EditActivityBase;
import org.andicar.activity.R;


/**
 *
 * @author miki
 */
public class MainScreenPreferenceActivity extends EditActivityBase {

    SharedPreferences.Editor editor;
    ListView zonesList;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        editor = mPreferences.edit();
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
        ArrayAdapter listAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, mainScreenZones);

        zonesList = (ListView) findViewById(android.R.id.list);
        zonesList.setAdapter(listAdapter);
        zonesList.setItemsCanFocus(false);
        zonesList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        zonesList.setOnItemClickListener(zonesSelectedListener);
        zonesList.setItemChecked(0, mPreferences.getBoolean("MainActivityShowMileage", true));
        zonesList.setItemChecked(1, mPreferences.getBoolean("MainActivityShowGPSTrack", true));
        zonesList.setItemChecked(2, mPreferences.getBoolean("MainActivityShowRefuel", true));
        zonesList.setItemChecked(3, mPreferences.getBoolean("MainActivityShowExpense", true));
        zonesList.setItemChecked(4, mPreferences.getBoolean("MainActivityShowCarReport", true));
    }

    protected AdapterView.OnItemClickListener zonesSelectedListener = new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                SparseBooleanArray checkedItems = zonesList.getCheckedItemPositions();
                if(checkedItems.valueAt(0))
                    editor.putBoolean("MainActivityShowMileage", true);
                else
                    editor.putBoolean("MainActivityShowMileage", false);
                if(checkedItems.valueAt(1))
                    editor.putBoolean("MainActivityShowGPSTrack", true);
                else
                    editor.putBoolean("MainActivityShowGPSTrack", false);
                if(checkedItems.valueAt(2))
                    editor.putBoolean("MainActivityShowRefuel", true);
                else
                    editor.putBoolean("MainActivityShowRefuel", false);
                if(checkedItems.valueAt(3))
                    editor.putBoolean("MainActivityShowExpense", true);
                else
                    editor.putBoolean("MainActivityShowExpense", false);
                if(checkedItems.valueAt(4))
                    editor.putBoolean("MainActivityShowCarReport", true);
                else
                    editor.putBoolean("MainActivityShowCarReport", false);
                
                editor.commit();
            }
        };

}
