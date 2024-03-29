/*
 *  AndiCar - car management software for Android powered devices
 *  Copyright (C) 2010 Miklos Keresztes (miklos.keresztes@gmail.com)
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT AY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.andicar.persistence;

import org.andicar.utils.StaticValues;
import org.andicar.utils.Utils;

import android.database.Cursor;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class ReimbursementListDataBinder implements SimpleCursorAdapter.ViewBinder {
    @Override
    public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
    	if(columnIndex == 2) {
    		((TextView) view).setText(
    				cursor.getString(2)
    					.replace("[#1]", DateFormat.getDateFormat(view.getContext().getApplicationContext())
				 				.format(cursor.getLong(3) * 1000))
    					.replace("[#2]", DateFormat.getDateFormat(view.getContext().getApplicationContext())
				 				.format(cursor.getLong(4) * 1000))
    					.replace("[#3]", Utils.numberToString(cursor.getDouble(5) , true, StaticValues.DECIMALS_RATES, StaticValues.ROUNDING_MODE_RATES)));
    		return true;
    	}
        return false;
    }
}
