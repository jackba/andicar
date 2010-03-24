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

import android.os.Bundle;
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

    private ArrayList<String> mainScreenZones;
    private ListView zonesList;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView( R.layout.main_screen_preference_activity );
        zonesList = (ListView) findViewById(android.R.id.list);
        fillBkList();
    }

    private void fillBkList() {
        mainScreenZones = getZones();
        ArrayAdapter listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, mainScreenZones);
        zonesList.setAdapter(listAdapter);
        zonesList.setItemsCanFocus(false);
        zonesList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        zonesList.setOnItemClickListener(zonesSelectedListener);
    }

    protected ArrayList<String> getZones() {
        ArrayList<String> myData = new ArrayList<String>();
        myData.add("Mileage zone");
        myData.add("Refuel zone");
        myData.add("Expense zone");
        return myData;
    }

    protected AdapterView.OnItemClickListener zonesSelectedListener = new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            }
        };

}
