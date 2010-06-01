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

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;
import org.andicar.persistence.MainDbAdapter;

/**
 *
 * @author miki
 */
public class CarListActivity extends ListActivityBase
{

    /** Called when the activity is first created. */
    @Override
    public void onCreate( Bundle icicle )
    {
        super.onCreate( icicle, mItemClickListener, CarEditActivity.class, null,
                MainDbAdapter.CAR_TABLE_NAME, MainDbAdapter.carTableColNames, null, MainDbAdapter.GEN_COL_NAME_NAME,
                android.R.layout.simple_list_item_2, new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, new int[]{android.R.id.text1}, null);
    }

    private OnItemClickListener mItemClickListener = new OnItemClickListener()
    {
        public void onItemClick( AdapterView parent, View v, int position, long id )
        {
            Cursor c = mDbAdapter.fetchRecord(MainDbAdapter.CAR_TABLE_NAME,
                    MainDbAdapter.carTableColNames, id);
            //car is actve?
            if( c.getString( MainDbAdapter.GEN_COL_ISACTIVE_POS ).equals( "Y" ) ) {
                mPrefEditor.putLong( "CurrentCar_ID", id );
                mPrefEditor.putString( "CurrentCar_Name", c.getString( MainDbAdapter.GEN_COL_NAME_POS ).trim() );
                mPrefEditor.putLong("CarUOMLength_ID", c.getLong(MainDbAdapter.CAR_COL_UOMLENGTH_ID_POS));
                mPrefEditor.putLong("CarUOMVolume_ID", c.getLong(MainDbAdapter.CAR_COL_UOMVOLUME_ID_POS));
                mPrefEditor.putLong("CarCurrency_ID", c.getLong(MainDbAdapter.CAR_COL_CURRENCY_ID_POS));
                mPrefEditor.commit();
                Toast toast = Toast.makeText( getApplicationContext(),
                        c.getString( MainDbAdapter.GEN_COL_NAME_POS ) + mRes.getString( R.string.GEN_SelectedMessage), Toast.LENGTH_SHORT );
                toast.show();
                c.close();
                finish();
            }
            else //inactive car selected
            {
                c.close();
                errorAlertBuilder.setMessage(mRes.getString(R.string.CarListActivity_InactiveCarSelectedMessage));
                errorAlert = errorAlertBuilder.create();
                errorAlert.show();
            }
        }
    };
}
