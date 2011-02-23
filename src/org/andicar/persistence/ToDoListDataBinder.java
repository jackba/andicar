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

import java.util.Calendar;

import org.andicar.activity.R;
import org.andicar.utils.StaticValues;
import org.andicar.utils.Utils;

import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class ToDoListDataBinder implements SimpleCursorAdapter.ViewBinder {
    @Override
    public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
    	Resources mRes = view.getResources();
    	String dataString;
    	TextView tv = (TextView) view;
    	if(columnIndex == 1) {
    		dataString = cursor.getString(1);
    		if(dataString.contains("[#5]"))
    			tv.setTextColor(Color.RED);
    		else if(dataString.contains("[#15]"))
    			tv.setTextColor(Color.GREEN);
    		else
    			tv.setTextColor(Color.WHITE);

    		tv.setText(
    				dataString
    					.replace("[#1]", mRes.getString(R.string.GEN_TypeLabel))
    					.replace("[#2]", mRes.getString(R.string.GEN_TaskLabel))
    					.replace("[#3]", mRes.getString(R.string.GEN_CarLabel))
    					.replace("[#4]", mRes.getString(R.string.GEN_StatusLabel))
    					.replace("[#5]", mRes.getString(R.string.ToDo_OverdueLabel))
    					.replace("[#6]", mRes.getString(R.string.ToDo_ScheduledLabel))
    					.replace("[#15]", mRes.getString(R.string.ToDo_DoneLabel))
			 );
    		return true;
    	}
    	else if(columnIndex == 2) {
    		if(cursor != null && cursor.getString(2) != null){
        		long time = System.currentTimeMillis();
        		Calendar now = Calendar.getInstance();
        		Calendar cal = Calendar.getInstance();
        		
        		long estMileageDueDays = cursor.getLong(7);
        		String timeStr = "";
    			if(estMileageDueDays >= 0){
    				if(estMileageDueDays == 99999999999L)
    					timeStr = mRes.getString(R.string.ToDo_EstimatedMileageDateNoData);
    				else{
    					if(cursor.getString(1).contains("[#5]"))
    						timeStr = mRes.getString(R.string.ToDo_OverdueLabel);
    					else{
    						cal.setTimeInMillis(time + (estMileageDueDays * StaticValues.ONE_DAY_IN_MILISECONDS));
    						if(cal.get(Calendar.YEAR) - now.get(Calendar.YEAR) > 5)
    							timeStr = mRes.getString(R.string.ToDo_EstimatedMileageDateTooFar);
    						else{
    							if(cal.getTimeInMillis() - now.getTimeInMillis() < 365 * StaticValues.ONE_DAY_IN_MILISECONDS) // 1 year
	    							timeStr = DateFormat.getDateFormat(view.getContext().getApplicationContext())
														.format(time + (estMileageDueDays * StaticValues.ONE_DAY_IN_MILISECONDS));
    							else{
    								timeStr = DateFormat.format("MMM, yyyy", cal).toString();
    							}
									
    						}
    					}
    				}
    			}
    			time = time + cursor.getLong(7);
    			tv.setText(
	    				cursor.getString(2)
	    					.replace("[#7]", mRes.getString(R.string.ToDo_ScheduledDateLabel)) 
	    					.replace("[#8]",  
		    							DateFormat.getDateFormat(view.getContext().getApplicationContext())
										.format(cursor.getLong(4) * 1000) + " " +
				 							DateFormat.getTimeFormat(view.getContext().getApplicationContext())
					 								.format(cursor.getLong(4) * 1000))
							.replace("[#9]", mRes.getString(R.string.GEN_Or2))
	    					.replace("[#10]", mRes.getString(R.string.ToDo_ScheduledMileageLabel))
	    					.replace("[#11]", Utils.numberToString(cursor.getDouble(5) , true, StaticValues.DECIMALS_LENGTH, StaticValues.ROUNDING_MODE_LENGTH))
	    					.replace("[#12]", mRes.getString(R.string.GEN_Mileage))
	    					.replace("[#13]", mRes.getString(R.string.ToDo_EstimatedMileageDate))
	    					.replace("[#14]", timeStr)
				 );
	    		return true;
    		}
    	}
        return false;
    }
}
