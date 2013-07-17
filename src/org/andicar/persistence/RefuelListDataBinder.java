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
import java.math.RoundingMode;

import org.andicar.utils.StaticValues;
import org.andicar.utils.Utils;
import org.andicar2.activity.R;

import android.content.Context;
import android.database.Cursor;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class RefuelListDataBinder implements SimpleCursorAdapter.ViewBinder {
//	private String m_fuelEffStr = "";
	String text = "";
	String consStr = "";
	BigDecimal oldFullRefuelIndex = null;
	BigDecimal distance = null;
	BigDecimal fuelQty = null;
	MainDbAdapter mDbAdapter = null;
	Context mCtx;

	
	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		if(mDbAdapter != null){
			try{mDbAdapter.close();}catch(Exception e){};
		}
		super.finalize();
	}

	public void initCtx(Context ctx){
		mCtx = ctx;
	}
	
	@Override
    public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
		if(mDbAdapter == null)
			mDbAdapter = new MainDbAdapter(mCtx);
		
    	if(columnIndex == 1) {
    		((TextView) view).setText(
    				cursor.getString(1)
    					.replace("[#1]", DateFormat.getDateFormat(view.getContext().getApplicationContext())
				 				.format(cursor.getLong(4) * 1000))
			 );
    		return true;
    	}
    	else if(columnIndex == 2){
    		String text = 
    				cursor.getString(2)
						.replace("[#1]", Utils.numberToString(cursor.getDouble(5) , true, StaticValues.DECIMALS_VOLUME, StaticValues.ROUNDING_MODE_VOLUME))
						.replace("[#2]", Utils.numberToString(cursor.getDouble(6) , true, StaticValues.DECIMALS_VOLUME, StaticValues.ROUNDING_MODE_VOLUME))
						.replace("[#3]", Utils.numberToString(cursor.getDouble(7) , true, StaticValues.DECIMALS_PRICE, StaticValues.ROUNDING_MODE_PRICE))
						.replace("[#4]", Utils.numberToString(cursor.getDouble(8) , true, StaticValues.DECIMALS_PRICE, StaticValues.ROUNDING_MODE_PRICE))
						.replace("[#5]", Utils.numberToString(cursor.getDouble(9) , true, StaticValues.DECIMALS_AMOUNT, StaticValues.ROUNDING_MODE_AMOUNT))
						.replace("[#6]", Utils.numberToString(cursor.getDouble(10) , true, StaticValues.DECIMALS_AMOUNT, StaticValues.ROUNDING_MODE_AMOUNT))
						.replace("[#7]", Utils.numberToString(cursor.getDouble(11) , true, StaticValues.DECIMALS_LENGTH, StaticValues.ROUNDING_MODE_LENGTH));
    		((TextView) view).setText(text);
    		return true;
    	}
    	else if(columnIndex == 3){
    		text = cursor.getString(3);
    		try{
    			oldFullRefuelIndex = new BigDecimal(cursor.getDouble(13));
    		}
    		catch(Exception e){
    			text = text.replace("[#1]", "Error#1! Please contact me at andicar.support@gmail.com");
        		((TextView) view).setText(text);
        		return true;
    		}
    		if(oldFullRefuelIndex == null || oldFullRefuelIndex.compareTo(BigDecimal.ZERO) < 0  //no previous full refuel found 
    				|| cursor.getString(12).equals("N")){ //this is not a full refuel
    			text = text.replace("[#1]", "");
        		((TextView) view).setText(text);
        		return true;
    		}
			// calculate the cons and fuel eff.
    		distance =  (new BigDecimal(cursor.getString(11))).subtract(oldFullRefuelIndex);
//			fuelQty = (new BigDecimal(cursor.getString(6))).add(new BigDecimal(
//					(cursor.getString(16) == null ? "0" : cursor.getString(16))));
    		try{
				fuelQty = (new BigDecimal(mDbAdapter.getFuelQtyForCons(
						cursor.getLong(16), oldFullRefuelIndex, cursor.getDouble(11))));
    		}
    		catch(NullPointerException e){
    			text = text.replace("[#1]", "Error#2! Please contact me at andicar.support@gmail.com");
        		((TextView) view).setText(text);
        		return true;
    		}
			try{
				consStr = Utils.numberToString(
							fuelQty.multiply(new BigDecimal("100")).divide(distance, 10, RoundingMode.HALF_UP), true,
							StaticValues.DECIMALS_LENGTH,
							StaticValues.ROUNDING_MODE_LENGTH) + " " + cursor.getString(14) + "/100" + cursor.getString(15) + 
							"; " +
						Utils.numberToString(
								distance.divide(fuelQty, 10, RoundingMode.HALF_UP), true,
								StaticValues.DECIMALS_LENGTH,
								StaticValues.ROUNDING_MODE_LENGTH) + " " + cursor.getString(15) + "/" + cursor.getString(14);
			}
			catch(Exception e){
    			text = text.replace("[#1]", "Error#3! Please contact me at andicar.support@gmail.com");
        		((TextView) view).setText(text);
        		return true;
			}
			
   			text = text.replace("[#1]", (text.equals("[#1]") ? "" : "\n") + 
    					view.getResources().getString(R.string.GEN_FuelEff) + " " + consStr);
    		
    		((TextView) view).setText(text);
    		return true;
    	}
        return false;
    }
    
//    public void setFuelEffStr(String str){
//    	m_fuelEffStr = str;
//    }
}
