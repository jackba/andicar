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

import android.os.Bundle;
import org.andicar.persistence.MainDbAdapter;

/**
 *
 * @author miki
 */
public class UOMListActivity extends ListActivityBase
{
    private String uomType = "";

    /** Called when the activity is first created. */
    @Override
    public void onCreate( Bundle icicle )
    {
        extras = getIntent().getExtras();
        uomType = extras.getString(MainDbAdapter.UOM_COL_UOMTYPE_NAME);

        super.onCreate( icicle, null, UOMEditActivity.class, null,
                MainDbAdapter.UOM_TABLE_NAME, MainDbAdapter.uomTableColNames, 
                MainDbAdapter.UOM_COL_UOMTYPE_NAME + "='" + uomType + "'", MainDbAdapter.UOM_COL_CODE_NAME,
                R.layout.twoline_list_activity,
                new String[]{MainDbAdapter.UOM_COL_CODE_NAME, MainDbAdapter.GEN_COL_NAME_NAME},
                new int[]{R.id.tvTwoLineListText1, R.id.tvTwoLineListText2}, null);

        if(uomType.equals( "L"))
            setTitle( getTitle() + " (" + mRes.getString( R.string.UOMEditActivity_UOMTypeLengthTitle) + ")");
        else
            setTitle( getTitle() + " (" + mRes.getString( R.string.UOMEditActivity_UOMTypeVolumeTitle) + ")");
    }
}
