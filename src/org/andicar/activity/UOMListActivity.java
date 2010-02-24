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
public class UOMListActivity extends ListActivityBase
{
    private String uomType = "";

    /** Called when the activity is first created. */
    @Override
    public void onCreate( Bundle icicle )
    {
        extras = getIntent().getExtras();
        uomType = extras.getString(MainDbAdapter.UOM_COL_UOMTYPE_NAME);

        super.onCreate( icicle, null, UOMEditActivity.class,
                MainDbAdapter.UOM_TABLE_NAME, MainDbAdapter.uomTableColNames, 
                MainDbAdapter.UOM_COL_UOMTYPE_NAME + "='" + uomType + "'", MainDbAdapter.UOM_COL_CODE_NAME,
                R.layout.twoline_list_activity,
                new String[]{MainDbAdapter.UOM_COL_CODE_NAME, MainDbAdapter.GEN_COL_NAME_NAME},
                new int[]{R.id.twoLineListText1, R.id.twoLineListText2});

        if(uomType.equals( "L"))
            setTitle( getTitle() + " (" + mRes.getString( R.string.UOM_EDIT_ACTIVITY_TITLE_LENGTH) + ")");
        else
            setTitle( getTitle() + " (" + mRes.getString( R.string.UOM_EDIT_ACTIVITY_TITLE_VOLUME) + ")");
    }
}
