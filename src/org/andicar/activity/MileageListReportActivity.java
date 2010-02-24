/*
 *  Copyright (C) 2010 Miklos Keresztes (miklos.keresztes@gmail.com)
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.andicar.activity;

import android.os.Bundle;
import org.andicar.persistence.MainDbAdapter;
import org.andicar.persistence.ReportDbAdapter;
import org.andicar.utils.Constants;

/**
 *
 * @author miki
 */
public class MileageListReportActivity extends ReportListActivityBase{

    @Override
    public void onCreate( Bundle icicle )
    {
        Long mCarId = getSharedPreferences( Constants.GLOBAL_PREFERENCE_NAME, 0 ).getLong("CurrentCar_ID", 0);

        super.onCreate( icicle, null, MileageEditActivity.class,
                MainDbAdapter.MILEAGE_TABLE_NAME, ReportDbAdapter.reportMileageListViewSelectCols, null,
                null,
                R.layout.twoline_list_activity,
                new String[]{ReportDbAdapter.FIRST_LINE_LIST_NAME, ReportDbAdapter.SECOND_LINE_LIST_NAME},
                new int[]{R.id.twoLineListText1, R.id.twoLineListText2}, 
                "reportMileageListViewSelect",  new String[] {mCarId.toString()});

    }
}
