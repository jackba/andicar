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

package org.andicar.activity;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;
import org.andicar.persistence.MainDbAdapter;
import org.andicar.utils.StaticValues;

/**
 *
 * @author miki
 */
public class DriverListActivity extends ListActivityBase
{
    AlertDialog.Builder inactiveDriverSelectedAlertBuilder;
    AlertDialog inactiveDriverSelectedAlert;

    /** Called when the activity is first created. */
    @Override
    public void onCreate( Bundle icicle )
    {
        super.onCreate( icicle, mItemClickListener, DriverEditActivity.class,
                MainDbAdapter.DRIVER_TABLE_NAME, MainDbAdapter.driverTableColNames, null, MainDbAdapter.GEN_COL_NAME_NAME,
                android.R.layout.simple_list_item_2,
                new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, new int[]{android.R.id.text1});

        inactiveDriverSelectedAlertBuilder = new AlertDialog.Builder( this );
        inactiveDriverSelectedAlertBuilder.setCancelable( false );
        inactiveDriverSelectedAlertBuilder.setPositiveButton( mRes.getString(R.string.GEN_OK), null );
        inactiveDriverSelectedAlertBuilder.setMessage( mRes.getString(R.string.INACTIVE_DRIVER_SELECTED_ERROR_MESSAGE) );
        inactiveDriverSelectedAlert = inactiveDriverSelectedAlertBuilder.create();

    }


    private OnItemClickListener mItemClickListener = new OnItemClickListener()
    {
        public void onItemClick( AdapterView parent, View v, int position, long id )
        {
            Cursor selectedRecord = mMainDbHelper.fetchRecord(MainDbAdapter.DRIVER_TABLE_NAME,
                    MainDbAdapter.driverTableColNames, id);
            //driver is actve?
            if( selectedRecord.getString( MainDbAdapter.GEN_COL_ISACTIVE_POS ).equals( "Y" ) ) {
                SharedPreferences settings = getSharedPreferences( StaticValues.GLOBAL_PREFERENCE_NAME, 0 );
                SharedPreferences.Editor editor = settings.edit();
                editor.putLong( "CurrentDriver_ID", id );
                editor.putString( "CurrentDriver_Name", selectedRecord.getString( MainDbAdapter.GEN_COL_NAME_POS ).trim() );
                editor.commit();
                Toast toast = Toast.makeText( getApplicationContext(),
                        selectedRecord.getString( MainDbAdapter.GEN_COL_NAME_POS ) + mRes.getString( R.string.RECORD_SELECTED_TOAST_MESSAGE), Toast.LENGTH_SHORT );
                toast.show();
                finish();
            }
            else //inactive driver selected
            {
                inactiveDriverSelectedAlert.show();
            }
        }
    };
}
