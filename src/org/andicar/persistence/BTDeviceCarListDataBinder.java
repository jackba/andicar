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

package org.andicar.persistence;

import org.andicar2.activity.R;

import android.content.res.Resources;
import android.database.Cursor;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class BTDeviceCarListDataBinder implements SimpleCursorAdapter.ViewBinder {
	MainDbAdapter mdb = null;
	String carName = null;
	Resources mResource = null;

	public BTDeviceCarListDataBinder(MainDbAdapter mdb, Resources mResource) {
		super();
		this.mdb = mdb;
		this.mResource = mResource;
	}

	@Override
    public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
    	if(columnIndex == AddOnDBAdapter.ADDON_BTDEVICE_CAR_CAR_ID_POS){
    		carName = mdb.getCarName(cursor.getLong(columnIndex));
    		if(carName != null)
    			((TextView) view).setText(mResource.getString(R.string.AddOn_BTStarter_LinkedTo) + " " + carName);
    		else
    			((TextView) view).setText(mResource.getString(R.string.AddOn_BTStarter_NoLinkedCar));
    		return true;
    	}
        return false;
    }
}
