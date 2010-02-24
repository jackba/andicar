/*
Copyright (C) 2009-2010 Miklos Keresztes - miklos.keresztes@gmail.com

This program is free software; you can redistribute it and/or modify it
under the terms of the GNU General Public License as published by the
Free Software Foundation; either version 2 of the License.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program;
if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

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
import org.andicar.utils.Constants;

/**
 *
 * @author miki
 */
public class CarListActivity extends ListActivityBase
{

    AlertDialog.Builder inactiveCarSelectedAlertBuilder;
    AlertDialog inactiveCarSelectedAlert;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate( Bundle icicle )
    {
        super.onCreate( icicle, mItemClickListener, CarEditActivity.class,
                MainDbAdapter.CAR_TABLE_NAME, MainDbAdapter.carTableColNames, null, MainDbAdapter.GEN_COL_NAME_NAME,
                android.R.layout.simple_list_item_2, new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, new int[]{android.R.id.text1});

        inactiveCarSelectedAlertBuilder = new AlertDialog.Builder( this );
        inactiveCarSelectedAlertBuilder.setCancelable( false );
        inactiveCarSelectedAlertBuilder.setPositiveButton( mRes.getString(R.string.GEN_OK), null );
        inactiveCarSelectedAlertBuilder.setMessage( mRes.getString(R.string.INACTIVE_CAR_SELECTED_ERROR_MESSAGE) );
        inactiveCarSelectedAlert = inactiveCarSelectedAlertBuilder.create();

    }

    private OnItemClickListener mItemClickListener = new OnItemClickListener()
    {
        public void onItemClick( AdapterView parent, View v, int position, long id )
        {
            Cursor selectedRecord = mMainDbHelper.fetchRecord(MainDbAdapter.CAR_TABLE_NAME,
                    MainDbAdapter.carTableColNames, id);
            //car is actve?
            if( selectedRecord.getString( MainDbAdapter.GEN_COL_ISACTIVE_POS ).equals( "Y" ) ) {
                SharedPreferences.Editor editor = mPreferences.edit();
                editor.putLong( "CurrentCar_ID", id );
                editor.putString( "CurrentCar_Name", selectedRecord.getString( MainDbAdapter.GEN_COL_NAME_POS ).trim() );
                editor.putLong("CarUOMLength_ID", selectedRecord.getLong(MainDbAdapter.CAR_COL_UOMLENGTH_ID_POS));
                editor.putLong("CarUOMVolume_ID", selectedRecord.getLong(MainDbAdapter.CAR_COL_UOMVOLUME_ID_POS));
                editor.putLong("CarCurrency_ID", selectedRecord.getLong(MainDbAdapter.CAR_COL_CURRENCY_ID_POS));
                editor.commit();
                Toast toast = Toast.makeText( getApplicationContext(),
                        selectedRecord.getString( MainDbAdapter.GEN_COL_NAME_POS ) + mRes.getString( R.string.RECORD_SELECTED_TOAST_MESSAGE), Toast.LENGTH_SHORT );
                toast.show();
                finish();
            }
            else //inactive car selected
            {
                inactiveCarSelectedAlert.show();
            }
        }
    };
}
