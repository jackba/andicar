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

package org.andicar.activity.report;

import java.util.Calendar;

import org.andicar.activity.ListActivityBase;
import org.andicar.activity.R;
import org.andicar.persistence.FileUtils;
import org.andicar.persistence.MainDbAdapter;
import org.andicar.persistence.ReportDbAdapter;
import org.andicar.utils.StaticValues;
import org.andicar.utils.Utils;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

/**
 *
 * @author miki
 */
public abstract class ReportListActivityBase extends ListActivityBase implements Runnable{
    protected ReportDbAdapter mListDbHelper = null;
    protected ReportDbAdapter mReportDbHelper = null;
    protected String reportSelectName = null;
    protected Bundle whereConditions;
    private View reportDialogView;
    private CheckBox ckIsSendEmail;
    private Spinner spnReportFormat;
    protected int mYearFrom = 2010;
    protected int mMonthFrom = 11;
    protected int mDayFrom = 1;
    protected int mYearTo = 2010;
    protected int mMonthTo = 11;
    protected int mDayTo = 1;
    
    AlertDialog.Builder reportOptionsDialog;
    ProgressDialog progressDialog;

    /**
     * 
     * @param what 1 = DateFrom; 2 = DateTo
     */
    abstract protected void updateDate(int what);

    @SuppressWarnings("rawtypes")
	protected void onCreate(Bundle icicle, OnItemClickListener mItemClickListener, Class editClass, Class insertClass,
            String editTableName, String[] editTableColumns, String whereCondition, String orderByColumn,
            int pLayoutId, String[] pDbMapFrom, int[] pLayoutIdTo, 
            String reportSqlName, Bundle reportParams, SimpleCursorAdapter.ViewBinder pViewBinder) {

        mListDbHelper = new ReportDbAdapter(this, reportSqlName, reportParams);
        mViewBinder = pViewBinder;

        super.onCreate(icicle, mItemClickListener, editClass, insertClass, editTableName, editTableColumns,
                whereCondition, orderByColumn, pLayoutId, pDbMapFrom, pLayoutIdTo, pViewBinder);

        Calendar cal = Calendar.getInstance();
        mYearFrom = cal.get(Calendar.YEAR);
        mMonthFrom = cal.get(Calendar.MONTH);
        mDayFrom = 1;
        mYearTo = cal.get(Calendar.YEAR);
        mMonthTo = cal.get(Calendar.MONTH);
        mDayTo = cal.get(Calendar.DAY_OF_MONTH);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mListDbHelper != null){
            mListDbHelper.close();
            mListDbHelper = null;
        }
        if(mReportDbHelper != null){
            mReportDbHelper.close();
            mReportDbHelper = null;
        }
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

        if(mViewBinder != null)
            cursorAdapter.setViewBinder(mViewBinder);
        setListAdapter( cursorAdapter );

    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu )
    {
        optionsMenu = menu;
        optionsMenu.add( 0, StaticValues.OPTION_MENU_ADD_ID, 0,
                mRes.getText( R.string.MENU_AddNewCaption ) ).
                setIcon( mRes.getDrawable( R.drawable.ic_menu_add ) );
        optionsMenu.add( 0, StaticValues.OPTION_MENU_SEARCH_ID, 0,
                mRes.getText( R.string.MENU_SearchCaption ) ).
                setIcon( mRes.getDrawable( R.drawable.ic_menu_search ) );
        optionsMenu.add( 0, StaticValues.OPTION_MENU_REPORT_ID, 0,
                mRes.getText( R.string.MENU_CreateReportCaption ) ).
                setIcon( mRes.getDrawable( R.drawable.ic_menu_report ) );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        if(item.getItemId() == StaticValues.OPTION_MENU_ADD_ID)
            return super.onOptionsItemSelected( item );
        else if(item.getItemId() == StaticValues.OPTION_MENU_REPORT_ID)
        {
            showDialog(StaticValues.DIALOG_REPORT_OPTIONS);
        }
        return true;
    }

    protected void initSpinner(View pSpinner, String tableName, String selection, String[] selectionArgs, long selectedId){
    	try{
            String selectSql = "";

            selectSql = "SELECT 'All' AS " + MainDbAdapter.GEN_COL_NAME_NAME + ", " +
                                 "-1 AS " + MainDbAdapter.GEN_COL_ROWID_NAME +
                        " UNION " +
                        " SELECT " + MainDbAdapter.GEN_COL_NAME_NAME + ", " +
                                    MainDbAdapter.GEN_COL_ROWID_NAME +
                        " FROM " + tableName;
            if(selection != null)
            	selectSql = selectSql + " WHERE " + selection;
            selectSql = selectSql + " ORDER BY " + MainDbAdapter.GEN_COL_ROWID_NAME;

            Spinner spinner = (Spinner) pSpinner;
            Cursor c = mListDbHelper.query(selectSql, selectionArgs);
            startManagingCursor( c );
            int[] to = new int[]{android.R.id.text1};
            SimpleCursorAdapter mCursorAdapter =
                    new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, c,
            new String[] {MainDbAdapter.GEN_COL_NAME_NAME}, to);
            mCursorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(mCursorAdapter);
            if(selectedId >= 0){
                //set the spinner to the selectedId
                    c.moveToFirst();
                    for( int i = 0; i < c.getCount(); i++ ) {
                        if( c.getLong( 1 ) == selectedId) {
                        	spinner.setSelection( i );
                            break;
                        }
                        c.moveToNext();
                    }
                }
        }
        catch(Exception e){}

    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch(id) {
		    case StaticValues.DIALOG_DATE_FROM_PICKER:
		        return new DatePickerDialog(this,
		                onDateFromSetListener,
		                mYearFrom, mMonthFrom, mDayFrom);
		    case StaticValues.DIALOG_DATE_TO_PICKER:
		        return new DatePickerDialog(this,
		                onDateToSetListener,
		                mYearTo, mMonthTo, mDayTo);
		    case StaticValues.DIALOG_REPORT_OPTIONS:
		        LayoutInflater liLayoutFactory = LayoutInflater.from(this);
		        reportDialogView = liLayoutFactory.inflate(R.layout.report_options_dialog, null);
		        spnReportFormat = (Spinner)reportDialogView.findViewById(R.id.spnReportOptionsFormat);
		        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
		                this, R.array.report_formats, android.R.layout.simple_spinner_item);
		        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		        spnReportFormat.setAdapter(adapter);
		
		        ckIsSendEmail = (CheckBox)reportDialogView.findViewById(R.id.ckIsSendEmail);
		
		        reportOptionsDialog = new AlertDialog.Builder(ReportListActivityBase.this);
		        reportOptionsDialog.setTitle(R.string.DIALOGReport_DialogTitle);
		        reportOptionsDialog.setView(reportDialogView);
		        reportOptionsDialog.setPositiveButton(R.string.GEN_OK, reportDialogButtonlistener);
		        reportOptionsDialog.setNegativeButton(R.string.GEN_CANCEL, reportDialogButtonlistener);
		        return reportOptionsDialog.create();
        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener onDateFromSetListener =
        new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                    int dayOfMonth) {
                mYearFrom = year;
                mMonthFrom = monthOfYear;
                mDayFrom = dayOfMonth;
               	updateDate(1);
            }
        };
        
    private DatePickerDialog.OnDateSetListener onDateToSetListener =
        new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                    int dayOfMonth) {
                mYearTo = year;
                mMonthTo = monthOfYear;
                mDayTo = dayOfMonth;
               	updateDate(2);
            }
        };

    private DialogInterface.OnClickListener reportDialogButtonlistener =
            new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {
            if (whichButton == DialogInterface.BUTTON_POSITIVE) {
                progressDialog = ProgressDialog.show(ReportListActivityBase.this, "",
                    mRes.getString(R.string.REPORTActivity_ProgressMessage), true);
//                createReport(true, ckSendEmail.isChecked(), formatSpinner.getSelectedItemId());
                Thread thread = new Thread(ReportListActivityBase.this);
                thread.start();

            }
        };
    };

    public void run() {
        createReport(true, ckIsSendEmail.isChecked(), spnReportFormat.getSelectedItemId());
    }

    private Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                try{
                    progressDialog.dismiss();
                    Toast toast = null;
                    if(msg.peekData() == null)
                        toast = Toast.makeText( getApplicationContext(),
                                mRes.getString(msg.what), Toast.LENGTH_LONG );
                    else
                        toast = Toast.makeText( getApplicationContext(),
                                msg.peekData().getString("ErrorMsg"), Toast.LENGTH_LONG );
                    if(toast != null)
                        toast.show();
                }
                catch(Exception e){}
            }
    };

    protected boolean createReport(boolean saveLocally, boolean sendToMail, long reportFormatId){
        Cursor c = null;
        String reportContent = "";
        String reportTitle = "";
        String reportFileName = "";

        int i;
        if( this instanceof MileageListReportActivity){
            reportTitle = "MileageReport_";
            mReportDbHelper = new ReportDbAdapter(this, "reportMileageListReportSelect", whereConditions);
            c = mReportDbHelper.fetchReport(-1);
        }
        else if(this instanceof RefuelListReportActivity){
            reportTitle = "RefuelReport_";
            mReportDbHelper = new ReportDbAdapter(this, "reportRefuelListReportSelect", whereConditions);
            c = mReportDbHelper.fetchReport(-1);
        }
        else if(this instanceof ExpensesListReportActivity){
            reportTitle = "ExpenseReport_";
            mReportDbHelper = new ReportDbAdapter(this, "reportExpensesListReportSelect", whereConditions);
            c = mReportDbHelper.fetchReport(-1);
        }
        else if(this instanceof GPSTrackListReportActivity){
            reportTitle = "GPSTrackReport_";
            mReportDbHelper = new ReportDbAdapter(this, "gpsTrackListReportSelect", whereConditions);
            c = mReportDbHelper.fetchReport(-1);
        }
        else{
            handler.sendEmptyMessage(R.string.ERR_035);
            return false;
        }
        if(c == null){
            Message msg = new Message();
            Bundle msgBundle = new Bundle();
            msgBundle.putString("ErrorMsg", mReportDbHelper.lastErrorMessage);
            msg.setData(msgBundle);
            handler.sendMessage(msg);
        }

        reportTitle = Utils.appendDateTime(reportTitle, true, false, false);
        reportFileName = Utils.appendDateTime(reportTitle, true, true, true);

        if(reportFormatId == 0){
            reportFileName = reportFileName +".csv";
            reportContent = createCSVContent(c);
        }
        else if(reportFormatId == 1){
            reportFileName = reportFileName +".html";
            reportContent = createHTMLContent(c, reportTitle);
        }
        c.close();

        FileUtils fu = new FileUtils(this);
        i = fu.writeReportFile(reportContent, reportFileName);
        if(i != -1){ //error
            handler.sendEmptyMessage(R.string.ERR_034);
            return false;
        }

        if(sendToMail){
            Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
            emailIntent.setType("text/html");
            emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, " AndiCar report " + reportTitle);
            emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Sent by AndiCar (http://sites.google.com/site/andicarfree/)");
            emailIntent.putExtra(android.content.Intent.EXTRA_STREAM, Uri.parse("file://" + StaticValues.REPORT_FOLDER + reportFileName));
//            emailIntent.setType("text/plain");
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
        }
        handler.sendEmptyMessage(R.string.REPORTActivity_ReportCreatedMessage);
        return true;
    }

    public String createCSVContent(Cursor reportCursor){
        String reportContent = "";
        String colVal;
        int i;

        //create header row
        for(i = 0; i< reportCursor.getColumnCount(); i++){
            if(i > 0)
                reportContent = reportContent + ",";
            reportContent = reportContent + reportCursor.getColumnName(i).replaceAll("_DTypeN", "");
        }
        reportContent = reportContent + "\n";
        while(reportCursor != null && reportCursor.moveToNext()){
            for(i = 0; i< reportCursor.getColumnCount(); i++){
                if(i > 0)
                    reportContent = reportContent + ",";
                if(reportCursor.getColumnName(i).contains("_DTypeN"))
                	colVal = Utils.numberToString(reportCursor.getDouble(i), false, 4, StaticValues.ROUNDING_MODE_LENGTH) ;
                else
                	colVal = reportCursor.getString(i);
                if(colVal == null)
                    colVal = "";
                reportContent = reportContent  + 
                        colVal.replace(",", " ")
                            .replace("[%d0]", mRes.getString(R.string.DayOfWeek_0))
                            .replace("[%d1]", mRes.getString(R.string.DayOfWeek_1))
                            .replace("[%d2]", mRes.getString(R.string.DayOfWeek_2))
                            .replace("[%d3]", mRes.getString(R.string.DayOfWeek_3))
                            .replace("[%d4]", mRes.getString(R.string.DayOfWeek_4))
                            .replace("[%d5]", mRes.getString(R.string.DayOfWeek_5))
                            .replace("[%d6]", mRes.getString(R.string.DayOfWeek_6))
                            ;
            }
            reportContent = reportContent + "\n";
        }
        return reportContent;
    }
    public String createHTMLContent(Cursor reportCursor, String title){
        String reportContent = 
                "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">\n" +
                "<html>\n" +
                    "<head>\n" +
                        "<title>" + title + "</title>\n" +
                        "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n" +
                    "</head>\n" +
                    "<body>\n" +
                        "<table  WIDTH=100% BORDER=1 BORDERCOLOR=\"#000000\" CELLPADDING=4 CELLSPACING=0>\n" +
                            "<TR VALIGN=TOP>\n"; //table header
        int i;
        String colVal;
        //create table header
        for(i = 0; i< reportCursor.getColumnCount(); i++){
            reportContent = reportContent +
                                "<TH>" + reportCursor.getColumnName(i).replaceAll("_DTypeN", "") + "</TH>\n";
        }
        reportContent = reportContent +
                            "</TR>\n"; //end table header

        while(reportCursor != null && reportCursor.moveToNext()){
            reportContent = reportContent +
                            "<TR VALIGN=TOP>\n";
            for(i = 0; i< reportCursor.getColumnCount(); i++){
                if(reportCursor.getColumnName(i).contains("_DTypeN"))
                	colVal = Utils.numberToString(reportCursor.getDouble(i), true, 4, StaticValues.ROUNDING_MODE_LENGTH) ;
                else
                	colVal = reportCursor.getString(i);
                if(colVal == null)
                    colVal = "";
                reportContent = reportContent +
                                "<TD>" + colVal
                                            .replace("[%d0]", mRes.getString(R.string.DayOfWeek_0))
                                            .replace("[%d1]", mRes.getString(R.string.DayOfWeek_1))
                                            .replace("[%d2]", mRes.getString(R.string.DayOfWeek_2))
                                            .replace("[%d3]", mRes.getString(R.string.DayOfWeek_3))
                                            .replace("[%d4]", mRes.getString(R.string.DayOfWeek_4))
                                            .replace("[%d5]", mRes.getString(R.string.DayOfWeek_5))
                                            .replace("[%d6]", mRes.getString(R.string.DayOfWeek_6))
                                + "</TD>\n";
            }
            reportContent = reportContent +
                            "</TR\n";
        }
        reportContent = reportContent +
                        "</table>\n" +
                        "<br><br><p align=\"center\"> Created with <a href=\"http://sites.google.com/site/andicarfree/\" target=\"new\">AndiCar</a>\n" +
                    "</body>\n" +
                "</html>";

        return reportContent;
    }

}
