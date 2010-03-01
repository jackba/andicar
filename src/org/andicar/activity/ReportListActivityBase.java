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
import java.util.Calendar;
import org.andicar.persistence.MainDbAdapter;
import org.andicar.persistence.ReportDbAdapter;
import org.andicar.utils.Constants;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.andicar.persistence.FileUtils;

/**
 *
 * @author miki
 */
public class ReportListActivityBase extends ListActivityBase{
    protected ReportDbAdapter mListDbHelper = null;
    protected ReportDbAdapter mReportDbHelper = null;
    protected Bundle whereConditions;
    

    protected void onCreate(Bundle icicle, OnItemClickListener mItemClickListener, Class editClass,
            String editTableName, String[] editTableColumns, String whereCondition, String orderByColumn,
            int pLayoutId, String[] pDbMapFrom, int[] pLayoutIdTo, 
            String reportSqlName, Bundle reportParams) {

        mListDbHelper = new ReportDbAdapter(this, reportSqlName, reportParams);

        super.onCreate(icicle, mItemClickListener, editClass, editTableName, editTableColumns,
                whereCondition, orderByColumn, pLayoutId, pDbMapFrom, pLayoutIdTo);
    }

    @Override
    protected void fillData() {
        recordCursor = mListDbHelper.fetchReport(-1);
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
                mRes.getText( R.string.MENU_ADD_NEW_CAPTION ) ).
                setIcon( mRes.getDrawable( R.drawable.ic_menu_add ) );
        optionsMenu.add( 0, Constants.OPTION_MENU_SEARCH_ID, 0,
                mRes.getText( R.string.MENU_SEARCH_CAPTION ) ).
                setIcon( mRes.getDrawable( R.drawable.ic_menu_search ) );
        optionsMenu.add( 0, Constants.OPTION_MENU_REPORT_ID, 0,
                mRes.getText( R.string.MENU_REPORT_CAPTION ) ).
                setIcon( mRes.getDrawable( R.drawable.ic_menu_report ) );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        if(item.getItemId() == Constants.OPTION_MENU_ADD_ID)
            return super.onOptionsItemSelected( item );
        else if(item.getItemId() == Constants.OPTION_MENU_REPORT_ID)
            return createReport(false, "CSV");
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
            Cursor mCursor = mListDbHelper.query(selectSql, null);
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

    protected boolean createReport(boolean sendToMail, String reportFormat){
        Cursor reportCursor = null;
        String reportContent = "";
        String reportName = "";
        Calendar reportCal = Calendar.getInstance();
        int i;
        if( this instanceof MileageListReportActivity){
            reportName = "MileageReport_";
            mReportDbHelper = new ReportDbAdapter(this, "reportMileageListReportSelect", whereConditions);
            reportCursor = mReportDbHelper.fetchReport(-1);
        }
        else if(this instanceof RefuelListReportActivity){
            reportName = "RefuelReport_";
            mReportDbHelper = new ReportDbAdapter(this, "reportRefuelListReportSelect", whereConditions);
            reportCursor = mReportDbHelper.fetchReport(-1);
        }
        reportName = reportName +
                    reportCal.get(Calendar.YEAR) + "" +
                    (reportCal.get(Calendar.MONTH) + 1) + "" +
                    reportCal.get(Calendar.DAY_OF_MONTH) + "" +
                    reportCal.get(Calendar.HOUR_OF_DAY) + "" +
                    reportCal.get(Calendar.MINUTE) + "" +
                    reportCal.get(Calendar.SECOND) +
                    reportCal.get(Calendar.MILLISECOND) +".csv";

        for(i = 0; i< reportCursor.getColumnCount(); i++){
            if(i > 0)
                reportContent = reportContent + ", ";
            reportContent = reportContent + reportCursor.getColumnName(i);
        }
        reportContent = reportContent + "\n";
        while(reportCursor != null && reportCursor.moveToNext()){
            for(i = 0; i< reportCursor.getColumnCount(); i++){
                if(i > 0)
                    reportContent = reportContent + ", ";
                reportContent = reportContent  + reportCursor.getString(i).replaceAll(",", " ");
            }
            reportContent = reportContent + "\n";
        }
        reportCursor.close();
        FileUtils fu = new FileUtils();
        i = fu.writeToFile(reportContent, reportName);
        if(i != -1){ //error
            if(fu.lastError != null)
                errorAlertBuilder.setMessage(mRes.getString(i) + "\n" + fu.lastError);
            else
                errorAlertBuilder.setMessage(mRes.getString(i));
           errorAlert = errorAlertBuilder.create();
           errorAlert.show();
        }
        return true;
    }

}
