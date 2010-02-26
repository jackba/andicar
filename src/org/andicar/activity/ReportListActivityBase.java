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

import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import org.andicar.persistence.MainDbAdapter;
import org.andicar.persistence.ReportDbAdapter;
import org.andicar.utils.Constants;

/**
 *
 * @author miki
 */
public class ReportListActivityBase extends ListActivityBase{
    protected ReportDbAdapter mReportDbHelper = null;
    protected Bundle searchConditions;
    

    protected void onCreate(Bundle icicle, OnItemClickListener mItemClickListener, Class editClass,
            String editTableName, String[] editTableColumns, String whereCondition, String orderByColumn,
            int pLayoutId, String[] pDbMapFrom, int[] pLayoutIdTo, 
            String reportSqlName, Bundle reportParams) {

        mReportDbHelper = new ReportDbAdapter(this, reportSqlName, reportParams);

        super.onCreate(icicle, mItemClickListener, editClass, editTableName, editTableColumns,
                whereCondition, orderByColumn, pLayoutId, pDbMapFrom, pLayoutIdTo);
    }

    @Override
    protected void fillData() {
        recordCursor = mReportDbHelper.fetchReport(-1);
        startManagingCursor( recordCursor );

        setListAdapter( null );

        if( recordCursor.getCount() == 0 ) {
            return;
        }

        SimpleCursorAdapter cursorAdapter =
                new SimpleCursorAdapter( this, mLayoutId, recordCursor, mDbMapFrom, mLayoutIdTo );

        setListAdapter( cursorAdapter );

    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu )
    {
        optionsMenu = menu;
        optionsMenu.add( 0, Constants.OPTION_MENU_ADD_ID, 0,
                mRes.getText( R.string.MENU_ADD_NEW_CAPTION ) ).setIcon( mRes.getDrawable( R.drawable.ic_menu_add ) );
        optionsMenu.add( 0, Constants.OPTION_MENU_SEARCH_ID, 0,
                mRes.getText( R.string.MENU_SEARCH_CAPTION ) ).setIcon( mRes.getDrawable( R.drawable.ic_menu_search ) );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        if(item.getItemId() == Constants.OPTION_MENU_ADD_ID)
            return super.onOptionsItemSelected( item );
        return true;
    }

    protected void initSpinner(View pSpinner, String spinnerType){
        try{
            String selectSql = "";
            String tableName = "";
            if(spinnerType.equals("ExpenseType"))
                tableName = MainDbAdapter.EXPENSETYPE_TABLE_NAME;
            else if(spinnerType.equals("Car"))
                tableName = MainDbAdapter.CAR_TABLE_NAME;
            else if(spinnerType.equals("Driver"))
                tableName = MainDbAdapter.DRIVER_TABLE_NAME;

            selectSql = "SELECT '<All>' AS " + MainDbAdapter.GEN_COL_NAME_NAME + ", " +
                                 "-1 AS " + MainDbAdapter.GEN_COL_ROWID_NAME +
                        " UNION " +
                        " SELECT " + MainDbAdapter.GEN_COL_NAME_NAME + ", " +
                                    MainDbAdapter.GEN_COL_ROWID_NAME +
                        " FROM " + tableName +
                        " ORDER BY " + MainDbAdapter.GEN_COL_ROWID_NAME;

            Spinner spinner = (Spinner) pSpinner;
            Cursor mCursor = mReportDbHelper.query(selectSql, null);
            startManagingCursor( mCursor );
            int[] to = new int[]{android.R.id.text1};
            SimpleCursorAdapter mCursorAdapter =
                    new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, mCursor,
            new String[] {MainDbAdapter.GEN_COL_NAME_NAME}, to);
            mCursorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(mCursorAdapter);

        }
        catch(Exception e){}

    }

}
