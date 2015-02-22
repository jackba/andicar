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

import java.math.BigDecimal;

import org.andicar.utils.StaticValues;
import org.andicar.utils.Utils;
import org.andicar2.activity.R;

import android.database.Cursor;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class MileageListDataBinder implements SimpleCursorAdapter.ViewBinder {
    @Override
    public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
		if(columnIndex == 1) {
    		((TextView) view).setText(
    				cursor.getString(1)
    					.replace("[#1]", DateFormat.getDateFormat(view.getContext().getApplicationContext()).format(cursor.getLong(5) * 1000) +
    							(cursor.getLong(14) != 0L ? " (" + Utils.getDaysHoursMinsFromSec(cursor.getLong(14)) + ")" : "")
				 				));
    		return true;
    	}
    	else if(columnIndex == 2){
    		BigDecimal reimbursementRate = BigDecimal.ZERO;
    		try{
    			reimbursementRate = new BigDecimal(cursor.getDouble(12));
    		}
    		catch(Exception e){};
    		((TextView) view).setText(
    				cursor.getString(2)
    					.replace("[#1]", Utils.numberToString(cursor.getDouble(6) , true, StaticValues.DECIMALS_LENGTH, StaticValues.ROUNDING_MODE_LENGTH))
    					.replace("[#2]", Utils.numberToString(cursor.getDouble(7) , true, StaticValues.DECIMALS_LENGTH, StaticValues.ROUNDING_MODE_LENGTH))
    					.replace("[#3]", Utils.numberToString(cursor.getDouble(8) , true, StaticValues.DECIMALS_LENGTH, StaticValues.ROUNDING_MODE_LENGTH))
    					.replace("[#4]", 
    							(reimbursementRate.compareTo(BigDecimal.ZERO) == 0) ? "" :
        						"("+ view.getContext().getResources().getText(R.string.GEN_Reimbursement).toString() + " " +
        						Utils.numberToString(reimbursementRate.multiply(new BigDecimal(cursor.getDouble(8))) , true, StaticValues.DECIMALS_RATES, StaticValues.ROUNDING_MODE_RATES)
        							+ " " + cursor.getString(11) + ")")
			 );
    		return true;
    	}
        return false;
    }
}
