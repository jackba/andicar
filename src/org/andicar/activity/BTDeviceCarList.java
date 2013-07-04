/*
 *  AndiCar - a car management software for Android powered devices.
 *
 *  Copyright (C) 2013 Miklos Keresztes (miklos.keresztes@gmail.com)
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
/**
 * List the linked cars to a Bluetooth device
 */
package org.andicar.activity;

import org.andicar2.activity.R;
import org.andicar.persistence.AddOnDBAdapter;
import org.andicar.persistence.BTDeviceCarListDataBinder;
import org.andicar.persistence.MainDbAdapter;
import org.andicar.utils.StaticValues;

import android.content.Context;
import android.os.Bundle;


/**
 * @author miki
 *
 */
public class BTDeviceCarList extends ListActivityBase {
    @Override
    public void onCreate( Bundle icicle )
    {
    	if(mDbAdapter == null)
    		mDbAdapter = new MainDbAdapter(this);

    	if(mPreferences == null)
        	mPreferences = getSharedPreferences(StaticValues.GLOBAL_PREFERENCE_NAME, Context.MODE_MULTI_PROCESS);

    	int twoLineListLayout = R.layout.twoline_list_activity_s01;
    		twoLineListLayout = R.layout.twoline_list_activity_s01;
    	
        super.onCreate( icicle, null, BTDeviceCarLink.class, null,
                AddOnDBAdapter.ADDON_BTDEVICE_CAR_TABLE_NAME, AddOnDBAdapter.addonBTDeviceCarTableColNames, 
                null, MainDbAdapter.COL_NAME_GEN_NAME,
                twoLineListLayout,
                new String[]{MainDbAdapter.COL_NAME_GEN_NAME, AddOnDBAdapter.ADDON_BTDEVICE_CAR_CAR_ID_NAME},
                new int[]{R.id.tvTwoLineListText1, R.id.tvTwoLineListText2}, new BTDeviceCarListDataBinder(mDbAdapter, getResources()));
    }

}
