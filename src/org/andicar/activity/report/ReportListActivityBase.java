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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;

import org.andicar.activity.ListActivityBase;
import org.andicar.activity.dialog.AndiCarDialogBuilder;
import org.andicar.persistence.FileUtils;
import org.andicar.persistence.MainDbAdapter;
import org.andicar.persistence.ReportDbAdapter;
import org.andicar.utils.StaticValues;
import org.andicar.utils.Utils;
import org.andicar2.activity.R;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
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
//    private CheckBox ckIsSendEmail;
    private Spinner spnReportFormat;
    protected int mYearFrom = 2010;
    protected int mMonthFrom = 11;
    protected int mDayFrom = 1;
    protected int mYearTo = 2010;
    protected int mMonthTo = 11;
    protected int mDayTo = 1;
    
    AndiCarDialogBuilder reportOptionsDialog;
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
        if(this instanceof ReimbursementRateListReportActivity)
        	mMonthFrom = 0;
        else
        	mMonthFrom = cal.get(Calendar.MONTH);
        mDayFrom = 1;
        mYearTo = cal.get(Calendar.YEAR);
        if(this instanceof ReimbursementRateListReportActivity){
	        mMonthTo = 11;
	        mDayTo = 31;
        }
        else{
	        mMonthTo = cal.get(Calendar.MONTH);
	        mDayTo = cal.get(Calendar.DAY_OF_MONTH);
        }
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
        optionsMenu.add( 0, StaticValues.OPTION_MENU_SHARE_ID, 0,
                mRes.getText( R.string.MENU_ShareReport ) ).
                setIcon( mRes.getDrawable( R.drawable.ic_menu_report ) );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        if(item.getItemId() == StaticValues.OPTION_MENU_ADD_ID)
            return super.onOptionsItemSelected( item );
        else if(item.getItemId() == StaticValues.OPTION_MENU_SHARE_ID)
        {
            showDialog(StaticValues.DIALOG_REPORT_OPTIONS);
        }
        return true;
    }

    protected void initSpinner(View pSpinner, String tableName, String selection, String[] selectionArgs, long selectedId){
    	try{
            String selectSql = "";

            selectSql = "SELECT 'All' AS " + MainDbAdapter.COL_NAME_GEN_NAME + ", " +
                                 "-1 AS " + MainDbAdapter.COL_NAME_GEN_ROWID +
                        " UNION " +
                        " SELECT " + MainDbAdapter.COL_NAME_GEN_NAME + ", " +
                                    MainDbAdapter.COL_NAME_GEN_ROWID +
                        " FROM " + tableName;
            if(selection != null)
            	selectSql = selectSql + " WHERE " + selection;
            selectSql = selectSql + " ORDER BY " + MainDbAdapter.COL_NAME_GEN_ROWID;

            Spinner spinner = (Spinner) pSpinner;
            Cursor c = mListDbHelper.query(selectSql, selectionArgs);
            startManagingCursor( c );
            int[] to = new int[]{android.R.id.text1};
            SimpleCursorAdapter mCursorAdapter =
                    new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, c,
            new String[] {MainDbAdapter.COL_NAME_GEN_NAME}, to);
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
		
//		        ckIsSendEmail = (CheckBox)reportDialogView.findViewById(R.id.ckIsSendEmail);
		
		        reportOptionsDialog = new AndiCarDialogBuilder(ReportListActivityBase.this, 
		        		AndiCarDialogBuilder.DIALOGTYPE_QUESTION, mRes.getString(R.string.DIALOGReport_DialogTitle));
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
            	//recreate the report folder if not exists
            	FileUtils fu = new FileUtils(ReportListActivityBase.this);
            	fu.createFolderIfNotExists(FileUtils.REPORT_FOLDER);
            	fu = null;

                progressDialog = ProgressDialog.show(ReportListActivityBase.this, "",
                    mRes.getString(R.string.REPORTActivity_ProgressMessage), true);
//                createReport(true, ckSendEmail.isChecked(), formatSpinner.getSelectedItemId());
                Thread thread = new Thread(ReportListActivityBase.this);
                thread.start();

            }
        };
    };

    public void run() {
        createReport(true, true /*ckIsSendEmail.isChecked()*/, spnReportFormat.getSelectedItemId());
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

    protected boolean createReport(boolean saveLocally, boolean share, long reportFormatId){
        Cursor c = null;
        String reportContent = "";
        String reportTitle = "";
        String reportFileName = "";

        int i;
        if( this instanceof MileageListReportActivity){
            reportTitle = "MileageReport_";
            mReportDbHelper = new ReportDbAdapter(this, "mileageListReportSelect", whereConditions);
            c = mReportDbHelper.fetchReport(-1);
        }
        else if(this instanceof RefuelListReportActivity){
            reportTitle = "RefuelReport_";
            mReportDbHelper = new ReportDbAdapter(this, "refuelListReportSelect", whereConditions);
            c = mReportDbHelper.fetchReport(-1);
        }
        else if(this instanceof ExpensesListReportActivity){
            reportTitle = "ExpenseReport_";
            mReportDbHelper = new ReportDbAdapter(this, "expensesListReportSelect", whereConditions);
            c = mReportDbHelper.fetchReport(-1);
        }
        else if(this instanceof GPSTrackListReportActivity){
            reportTitle = "GPSTrackReport_";
            mReportDbHelper = new ReportDbAdapter(this, "gpsTrackListReportSelect", whereConditions);
            c = mReportDbHelper.fetchReport(-1);
        }
        else if(this instanceof ToDoListReportActivity){
            reportTitle = "ToDoListReport_";
            mReportDbHelper = new ReportDbAdapter(this, "todoListReportSelect", whereConditions);
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

        if(share){
            Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
            emailIntent.setType("text/html");
            emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, " AndiCar report " + reportTitle + (reportFormatId == 0 ? ".csv" : ".html"));
            emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Sent by AndiCar (http://www.andicar.org)");
            emailIntent.putExtra(android.content.Intent.EXTRA_STREAM, Uri.parse("file://" + StaticValues.REPORT_FOLDER + reportFileName));
//            emailIntent.setType("text/plain");
            try{
            	startActivity(Intent.createChooser(emailIntent, mRes.getString(R.string.GEN_Share)));
            }
            catch(ActivityNotFoundException e){
            	Toast.makeText(this, R.string.ERR_067, Toast.LENGTH_LONG).show();
            }
        }
        handler.sendEmptyMessage(R.string.REPORTActivity_ReportCreatedMessage);
        return true;
    }

    public String createCSVContent(Cursor reportCursor){
        String reportContent = "";
        String colVal;
        int i;
    	BigDecimal oldFullRefuelIndex = null;
    	BigDecimal distance = null;
    	BigDecimal fuelQty = null;

        //create header row
        boolean appendComma = false;
        for(i = 0; i< reportCursor.getColumnCount(); i++){
        	if(reportCursor.getColumnName(i).endsWith("DoNotExport"))
        		continue;
        	
            if(appendComma)
                reportContent = reportContent + ",";
            appendComma = true;
            
            reportContent = reportContent + "\"" + 
            					reportCursor.getColumnName(i)
            						.replaceAll("_DTypeN", "")
            						.replaceAll("_DTypeD", "")
            						.replaceAll("_DTypeL", "")
            						.replaceAll("_DTypeR", "")
            						+ "\"";
        }
        reportContent = reportContent + "\n";
        
		long currentTime = System.currentTimeMillis();
		long days;
		Calendar now = Calendar.getInstance();
		Calendar cal = Calendar.getInstance();
		
		appendComma = false;
        while(reportCursor != null && reportCursor.moveToNext()){
        	appendComma = false;
            for(i = 0; i< reportCursor.getColumnCount(); i++){
            	if(reportCursor.getColumnName(i).endsWith("DoNotExport"))
            		continue;
            	
                if(appendComma)
                    reportContent = reportContent + ",";
                appendComma = true;
                
                if(reportCursor.getColumnName(i).endsWith("_DTypeN"))
                	colVal = Utils.numberToString(reportCursor.getDouble(i), false, 4, StaticValues.ROUNDING_MODE_LENGTH) ;
                else if(reportCursor.getColumnName(i).endsWith("_DTypeL"))
                	colVal = Utils.numberToString(reportCursor.getLong(i), false, 4, StaticValues.ROUNDING_MODE_LENGTH) ;
                else if(reportCursor.getColumnName(i).endsWith("_DTypeR"))
                	colVal = Utils.numberToString(reportCursor.getDouble(i), false, 5, StaticValues.ROUNDING_MODE_RATES) ;
                else if(reportCursor.getColumnName(i).endsWith("_DTypeD"))
                	colVal = DateFormat.getDateFormat(this).format(reportCursor.getLong(i) * 1000);
                else
                	colVal = reportCursor.getString(i);
                
                if(colVal == null)
                    colVal = "";
                colVal = colVal.replace("\"", "''");
                if(this instanceof ToDoListReportActivity){
                	colVal =   
    	                	colVal.replace("[#TDR1]", mRes.getString(R.string.ToDo_DoneLabel))
    	                            .replace("[#TDR2]", mRes.getString(R.string.ToDo_OverdueLabel))
    	                            .replace("[#TDR3]", mRes.getString(R.string.ToDo_ScheduledLabel))
    	                            .replace("[#TDR4]", mRes.getString(R.string.GEN_Time))
    	                            .replace("[#TDR5]", mRes.getString(R.string.TaskEditActivity_MileageDriven))
    	                            .replace("[#TDR6]", 
    	                            		mRes.getString(R.string.GEN_Time) 
    	                            			+ " & " + mRes.getString(R.string.TaskEditActivity_MileageDriven))
    	                            ;
                    	if((i == 6 || i == 7 || i == 8) && colVal.equals("0"))
                    		colVal = "N/A";
                    	if((i == 8 || i == 9) && !colVal.equals("N/A")){
                    		try{
    	                		days = Long.parseLong(colVal);
    	                		if(days == 99999999999L)
    								colVal = mRes.getString(R.string.ToDo_EstimatedMileageDateNoData);
    	                		else{
    								cal.setTimeInMillis(currentTime + (days * StaticValues.ONE_DAY_IN_MILISECONDS));
    								if(cal.get(Calendar.YEAR) - now.get(Calendar.YEAR) > 5)
    									colVal = mRes.getString(R.string.ToDo_EstimatedMileageDateTooFar);
    								else{
    									if(cal.getTimeInMillis() - now.getTimeInMillis() < 365 * StaticValues.ONE_DAY_IN_MILISECONDS) // 1 year
    										colVal = DateFormat.getDateFormat(this)
    															.format(currentTime + (days * StaticValues.ONE_DAY_IN_MILISECONDS));
    									else{
    										colVal = DateFormat.format("MMM, yyyy", cal).toString();
    									}
    								}
    	                		}
                    		}
                    		catch(NumberFormatException e){
                    			colVal = mRes.getString(R.string.ToDo_EstimatedMileageDateNoData);
                    		}
                    	}
                }
                else{ 
                	if(this instanceof RefuelListReportActivity){
	                	if(colVal.contains("[#rv1]") || colVal.contains("[#rv2]")){
		            		try{
		            			oldFullRefuelIndex = new BigDecimal(reportCursor.getDouble(27));
		            		}
		            		catch(Exception e){
		            			colVal = colVal.replace("[#rv1]", "Error #1! Please contact me at andicar.support@gmail.com")
	            								.replace("[#rv2]", "Error #1! Please contact me at andicar.support@gmail.com");
		            			reportContent = reportContent + "\"" + colVal + "\"";
		            			continue;
		            		}
		            		if(oldFullRefuelIndex == null || oldFullRefuelIndex.compareTo(BigDecimal.ZERO) < 0  //no previous full refuel found 
		            				|| reportCursor.getString(6).equals("N")){ //this is not a full refuel
		            			colVal = colVal.replace("[#rv1]", "")
		            							.replace("[#rv2]", "");
		            		}
		        			// calculate the cons and fuel eff.
		            		distance = (new BigDecimal(reportCursor.getString(5))).subtract(oldFullRefuelIndex);
		            		try{
		        				fuelQty = (new BigDecimal(mReportDbHelper.getFuelQtyForCons(
		        						reportCursor.getLong(28), oldFullRefuelIndex, reportCursor.getDouble(5))));
		            		}
		            		catch(NullPointerException e){
		            			colVal = colVal.replace("[#rv1]", "Error#2! Please contact me at andicar.support@gmail.com")
		            							.replace("[#rv2]", "Error#2! Please contact me at andicar.support@gmail.com");
		            			reportContent = reportContent + "\"" + colVal + "\"";
		            			continue;
		            		}
		            		try{
		            			colVal = colVal.replace("[#rv1]", 
		        						Utils.numberToString(fuelQty.multiply(new BigDecimal("100")).divide(distance, 10, RoundingMode.HALF_UP), false,
		        								2, RoundingMode.HALF_UP))
		        								.replace("[#rv2]", 
		        						Utils.numberToString(distance.divide(fuelQty, 10, RoundingMode.HALF_UP), false,
		        								2, RoundingMode.HALF_UP));
		            		}
		            		catch(Exception e){
		            			colVal = colVal.replace("[#rv1]", "Error#3! Please contact me at andicar.support@gmail.com")
		            							.replace("[#rv2]", "Error#3! Please contact me at andicar.support@gmail.com");
		            			reportContent = reportContent + "\"" + colVal + "\"";
		            			continue;
		            		}
	                	}
	                }
                	colVal =   
	                	colVal.replace("[#d0]", mRes.getString(R.string.DayOfWeek_0))
	                            .replace("[#d1]", mRes.getString(R.string.DayOfWeek_1))
	                            .replace("[#d2]", mRes.getString(R.string.DayOfWeek_2))
	                            .replace("[#d3]", mRes.getString(R.string.DayOfWeek_3))
	                            .replace("[#d4]", mRes.getString(R.string.DayOfWeek_4))
	                            .replace("[#d5]", mRes.getString(R.string.DayOfWeek_5))
	                            .replace("[#d6]", mRes.getString(R.string.DayOfWeek_6));
                }
                reportContent = reportContent + "\"" + colVal + "\"";
            }
            reportContent = reportContent + "\n";
        }
        return reportContent;
    }
    
    
    public String createHTMLContent(Cursor reportCursor, String title){
    	BigDecimal oldFullRefuelIndex = null;
    	BigDecimal distance = null;
    	BigDecimal fuelQty = null;
        int i;
        String colVal;
    	
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
        //create table header
        for(i = 0; i< reportCursor.getColumnCount(); i++){
        	if(reportCursor.getColumnName(i).endsWith("DoNotExport"))
        		continue;

            reportContent = reportContent +
                                "<TH>" + reportCursor.getColumnName(i)
                                		.replaceAll("_DTypeN", "") 
	            						.replaceAll("_DTypeD", "")
	            						.replaceAll("_DTypeL", "")
	            						.replaceAll("_DTypeR", "")
                        		+ "</TH>\n";
        }
        reportContent = reportContent +
                            "</TR>\n"; //end table header
		
        long currentTime = System.currentTimeMillis();
		long days;
		Calendar now = Calendar.getInstance();
		Calendar cal = Calendar.getInstance();
		String colValUF = "";
		long date = 0;

        while(reportCursor != null && reportCursor.moveToNext()){
            reportContent = reportContent +
                            "<TR VALIGN=TOP>\n";
            for(i = 0; i< reportCursor.getColumnCount(); i++){

            	if(reportCursor.getColumnName(i).endsWith("DoNotExport"))
            		continue;
            	
                if(reportCursor.getColumnName(i).contains("_DTypeN")){
                	colVal = Utils.numberToString(reportCursor.getDouble(i), true, 4, StaticValues.ROUNDING_MODE_LENGTH);
                	colValUF = Utils.numberToString(reportCursor.getDouble(i), false, 4, StaticValues.ROUNDING_MODE_LENGTH);
                }
                else if(reportCursor.getColumnName(i).endsWith("_DTypeL")){
                	colVal = Utils.numberToString(reportCursor.getLong(i), true, 4, StaticValues.ROUNDING_MODE_LENGTH) ;
                	colValUF = Utils.numberToString(reportCursor.getLong(i), false, 4, StaticValues.ROUNDING_MODE_LENGTH);
                }
                if(reportCursor.getColumnName(i).contains("_DTypeR")){
                	colVal = Utils.numberToString(reportCursor.getDouble(i), true, 5, StaticValues.ROUNDING_MODE_RATES);
                	colValUF = Utils.numberToString(reportCursor.getDouble(i), false, 5, StaticValues.ROUNDING_MODE_RATES);
                }
                else if(reportCursor.getColumnName(i).endsWith("_DTypeD")){
                	date = reportCursor.getLong(i) * 1000;
                	colVal = DateFormat.getDateFormat(this).format(date);
                }
                else
                	colVal = reportCursor.getString(i);
                if(colVal == null)
                    colVal = "";
                
                if(this instanceof ToDoListReportActivity){
                	colVal =   
    	                	colVal.replace("[#TDR1]", mRes.getString(R.string.ToDo_DoneLabel))
    	                            .replace("[#TDR2]", mRes.getString(R.string.ToDo_OverdueLabel))
    	                            .replace("[#TDR3]", mRes.getString(R.string.ToDo_ScheduledLabel))
    	                            .replace("[#TDR4]", mRes.getString(R.string.GEN_Time))
    	                            .replace("[#TDR5]", mRes.getString(R.string.TaskEditActivity_MileageDriven))
    	                            .replace("[#TDR6]", 
    	                            		mRes.getString(R.string.GEN_Time) 
    	                            			+ " & " + mRes.getString(R.string.TaskEditActivity_MileageDriven))
    	                            ;
                    	if((i == 6 && date == 0) || ((i == 7 || i == 8) && colVal.equals("0")))
                    		colVal = "N/A";
                    	if((i == 8 || i == 9) && !colVal.equals("N/A")){
                    		try{
                    			days = Long.parseLong(colValUF);
                    		}
                    		catch (NumberFormatException e) {
                    			days = 0;
    						}
                    		if(days == 99999999999L)
    							colVal = mRes.getString(R.string.ToDo_EstimatedMileageDateNoData);
                    		else{
    							cal.setTimeInMillis(currentTime + (days * StaticValues.ONE_DAY_IN_MILISECONDS));
    							if(cal.get(Calendar.YEAR) - now.get(Calendar.YEAR) > 5)
    								colVal = mRes.getString(R.string.ToDo_EstimatedMileageDateTooFar);
    							else{
    								if(cal.getTimeInMillis() - now.getTimeInMillis() < 365 * StaticValues.ONE_DAY_IN_MILISECONDS) // 1 year
    									colVal = DateFormat.getDateFormat(this)
    														.format(currentTime + (days * StaticValues.ONE_DAY_IN_MILISECONDS));
    								else{
    									colVal = DateFormat.format("MMM, yyyy", cal).toString();
    								}
    							}
                    		}
                    	}
                }
                else{
                	if(this instanceof RefuelListReportActivity){
	                	if(colVal.contains("[#rv1]") || colVal.contains("[#rv2]")){
		            		try{
		            			oldFullRefuelIndex = new BigDecimal(reportCursor.getDouble(27));
		            		}
		            		catch(Exception e){
		            			colVal = colVal.replace("[#rv1]", "Error #1! Please contact me at andicar.support@gmail.com")
	            								.replace("[#rv2]", "Error #1! Please contact me at andicar.support@gmail.com");
		            			reportContent = reportContent + "<TD>" + colVal + "</TD>\n";
		            			continue;
		            		}
		            		if(oldFullRefuelIndex == null || oldFullRefuelIndex.compareTo(BigDecimal.ZERO) < 0  //no previous full refuel found 
		            				|| reportCursor.getString(6).equals("N")){ //this is not a full refuel
		            			colVal = colVal.replace("[#rv1]", "")
		            							.replace("[#rv2]", "");
		            		}
		        			// calculate the cons and fuel eff.
		            		distance = (new BigDecimal(reportCursor.getString(5))).subtract(oldFullRefuelIndex);
		            		try{
		        				fuelQty = (new BigDecimal(mReportDbHelper.getFuelQtyForCons(
		        						reportCursor.getLong(28), oldFullRefuelIndex, reportCursor.getDouble(5))));
		            		}
		            		catch(NullPointerException e){
		            			colVal = colVal.replace("[#rv1]", "Error#2! Please contact me at andicar.support@gmail.com")
		            							.replace("[#rv2]", "Error#2! Please contact me at andicar.support@gmail.com");
		            			reportContent = reportContent + "<TD>" + colVal + "</TD>\n";
		            			continue;
		            		}
		            		try{
		            			colVal = colVal.replace("[#rv1]", 
		        						Utils.numberToString(fuelQty.multiply(new BigDecimal("100")).divide(distance, 10, RoundingMode.HALF_UP), false,
		        								2, RoundingMode.HALF_UP))
		        								.replace("[#rv2]", 
		        						Utils.numberToString(distance.divide(fuelQty, 10, RoundingMode.HALF_UP), false,
		        								2, RoundingMode.HALF_UP));
		            		}
		            		catch(Exception e){
		            			colVal = colVal.replace("[#rv1]", "Error#3! Please contact me at andicar.support@gmail.com")
		            							.replace("[#rv2]", "Error#3! Please contact me at andicar.support@gmail.com");
		            			reportContent = reportContent + "<TD>" + colVal + "</TD>\n";
		            			continue;
		            		}
	                	}
	                }
                	colVal =   
    	                	colVal.replace("[#d0]", mRes.getString(R.string.DayOfWeek_0))
    	                            .replace("[#d1]", mRes.getString(R.string.DayOfWeek_1))
    	                            .replace("[#d2]", mRes.getString(R.string.DayOfWeek_2))
    	                            .replace("[#d3]", mRes.getString(R.string.DayOfWeek_3))
    	                            .replace("[#d4]", mRes.getString(R.string.DayOfWeek_4))
    	                            .replace("[#d5]", mRes.getString(R.string.DayOfWeek_5))
    	                            .replace("[#d6]", mRes.getString(R.string.DayOfWeek_6));
            	}
                reportContent = reportContent + "<TD>" + colVal + "</TD>\n"; 
            }
            reportContent = reportContent +
                            "</TR\n";
        }
        reportContent = reportContent +
                        "</table>\n" +
                        "<br><br><p align=\"center\"> Created with <a href=\"http:/www.andicar.org\" target=\"new\">AndiCar</a>\n" +
                    "</body>\n" +
                "</html>";

        return reportContent;
    }

}
