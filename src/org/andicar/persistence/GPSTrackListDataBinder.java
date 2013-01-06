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

import android.content.res.Resources;
import android.database.Cursor;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import org.andicar2.activity.R;
import org.andicar.utils.Utils;

public class GPSTrackListDataBinder implements SimpleCursorAdapter.ViewBinder {
    @Override
    public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
        if(columnIndex == 1){
            ((TextView) view).setText(cursor.getString(1)
            		.replace("[#1]", DateFormat.getDateFormat(view.getContext().getApplicationContext())
            				.format(cursor.getLong(7) * 1000)));
            return true;
        }
        else if(columnIndex == 2){
            Resources mRes = view.getResources();
            ((TextView) view).setText(cursor.getString(2)
                    .replace("[#1]", mRes.getString(R.string.GPSTrackReport_GPSTrackVar_1))
                    .replace("[#2]", mRes.getString(R.string.GPSTrackReport_GPSTrackVar_2))
                    .replace("[#3]", mRes.getString(R.string.GPSTrackReport_GPSTrackVar_3))
                    .replace("[#4]", mRes.getString(R.string.GPSTrackReport_GPSTrackVar_4))
                    .replace("[#5]", mRes.getString(R.string.GPSTrackReport_GPSTrackVar_5) +
                            Utils.getTimeString(cursor.getLong(4), false))
                    .replace("[#6]", mRes.getString(R.string.GPSTrackReport_GPSTrackVar_6) +
                            Utils.getTimeString(cursor.getLong(5), false))
                    .replace("[#7]", mRes.getString(R.string.GPSTrackReport_GPSTrackVar_7))
                    .replace("[#8]", mRes.getString(R.string.GPSTrackReport_GPSTrackVar_8))
                    .replace("[#9]", mRes.getString(R.string.GPSTrackReport_GPSTrackVar_9))
                    .replace("[#10]", mRes.getString(R.string.GPSTrackReport_GPSTrackVar_10))
                    .replace("[#11]", mRes.getString(R.string.GPSTrackReport_GPSTrackVar_11))
                    );
            return true;
        }
        return false;
    }
}
