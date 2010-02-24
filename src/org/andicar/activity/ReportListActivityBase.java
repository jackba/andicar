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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleCursorAdapter;
import org.andicar.persistence.MainDbAdapter;
import org.andicar.persistence.ReportDbAdapter;

/**
 *
 * @author miki
 */
public class ReportListActivityBase extends ListActivityBase{
    protected ReportDbAdapter mReportDbHelper = null;

    protected void onCreate(Bundle icicle, OnItemClickListener mItemClickListener, Class editClass,
            String editTableName, String[] editTableColumns, String whereCondition, String orderByColumn,
            int pLayoutId, String[] pDbMapFrom, int[] pLayoutIdTo, 
            String reportSqlName, String[] reportParams) {

        mReportDbHelper = new ReportDbAdapter(this, reportSqlName, reportParams);

        super.onCreate(icicle, mItemClickListener, editClass, editTableName, editTableColumns,
                whereCondition, orderByColumn, pLayoutId, pDbMapFrom, pLayoutIdTo);
    }

    @Override
    protected void fillData() {
        recordCursor = mReportDbHelper.fetchReport();
        startManagingCursor( recordCursor );

        setListAdapter( null );

        if( recordCursor.getCount() == 0 ) {
            return;
        }

        SimpleCursorAdapter cursorAdapter =
                new SimpleCursorAdapter( this, mLayoutId, recordCursor, mDbMapFrom, mLayoutIdTo );

        setListAdapter( cursorAdapter );

    }



}
