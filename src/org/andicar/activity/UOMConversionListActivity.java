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

import android.os.Bundle;
import org.andicar.persistence.MainDbAdapter;

/**
 *
 * @author miki
 */
public class UOMConversionListActivity extends ListActivityBase
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate( Bundle icicle )
    {
        super.onCreate( icicle, null, UOMConversionEditActivity.class,
                MainDbAdapter.UOM_CONVERSION_TABLE_NAME, MainDbAdapter.uomConversionTableColNames,
                null, MainDbAdapter.GEN_COL_NAME_NAME,
                R.layout.threeline_list_activity,
                new String[]{MainDbAdapter.GEN_COL_NAME_NAME, MainDbAdapter.GEN_COL_USER_COMMENT_NAME, MainDbAdapter.UOM_CONVERSION_COL_RATE_NAME},
                new int[]{R.id.threeLineListText1, R.id.threeLineListText2, R.id.threeLineListText3});

//        mDbHelper = new UOMConversionDbAdapter( this );

    }

}
