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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import org.andicar.persistence.MainDbAdapter;
import org.andicar.persistence.ReportDbAdapter;
import org.andicar.utils.StaticValues;
import org.andicar.activity.ListActivityBase;
import org.andicar.activity.R;
import org.andicar.persistence.FileUtils;
import org.andicar.utils.Utils;

/**
 *
 * @author miki
 */
public class ReportListActivityBase extends ListActivityBase{
    protected ReportDbAdapter mListDbHelper = null;
    protected ReportDbAdapter mReportDbHelper = null;
    protected Bundle whereConditions;
    private View reportDialogView;
//    private CheckBox ckSaveLocally;
    private CheckBox ckSendEmail;
    private Spinner formatSpinner;
    

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
        optionsMenu.add( 0, StaticValues.OPTION_MENU_ADD_ID, 0,
                mRes.getText( R.string.MENU_ADD_NEW_CAPTION ) ).
                setIcon( mRes.getDrawable( R.drawable.ic_menu_add ) );
        optionsMenu.add( 0, StaticValues.OPTION_MENU_SEARCH_ID, 0,
                mRes.getText( R.string.MENU_SEARCH_CAPTION ) ).
                setIcon( mRes.getDrawable( R.drawable.ic_menu_search ) );
        optionsMenu.add( 0, StaticValues.OPTION_MENU_REPORT_ID, 0,
                mRes.getText( R.string.MENU_REPORT_CAPTION ) ).
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
            showDialog(StaticValues.reportOptionsDialog);
//            return createReport(false, "html");
        }
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

    @Override
    protected Dialog onCreateDialog(int id) {
        LayoutInflater factory = LayoutInflater.from(this);
        reportDialogView = factory.inflate(R.layout.report_options_dialog, null);
        formatSpinner = (Spinner)reportDialogView.findViewById(R.id.reportOptionsFormatSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.report_formats, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        formatSpinner.setAdapter(adapter);

//        ckSaveLocally = (CheckBox)reportDialogView.findViewById(R.id.reportOptionsCkSaveLocal);
        ckSendEmail = (CheckBox)reportDialogView.findViewById(R.id.reportOptionsCkSendEmail);

        AlertDialog.Builder reportOptionsDialog = new AlertDialog.Builder(ReportListActivityBase.this);
        reportOptionsDialog.setTitle(R.string.REPORTOPTIONS_DIALOG_TITLE);
        reportOptionsDialog.setView(reportDialogView);
        reportOptionsDialog.setPositiveButton(R.string.GEN_OK, reportDialogButtonlistener);
        reportOptionsDialog.setNegativeButton(R.string.GEN_CANCEL, reportDialogButtonlistener);
        return reportOptionsDialog.create();
    }
    private DialogInterface.OnClickListener reportDialogButtonlistener =
            new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {
            if (whichButton == DialogInterface.BUTTON_POSITIVE) {
                    createReport(true, ckSendEmail.isChecked(),
                            formatSpinner.getSelectedItemId());
            }
        };
    };

    protected boolean createReport(boolean saveLocally, boolean sendToMail, long reportFormatId){
        Cursor reportCursor = null;
        String reportContent = "";
        String reportTitle = "";
        String reportFileName = "";
        int i;
        if( this instanceof MileageListReportActivity){
            reportTitle = "MileageReport_";
            mReportDbHelper = new ReportDbAdapter(this, "reportMileageListReportSelect", whereConditions);
            reportCursor = mReportDbHelper.fetchReport(-1);
        }
        else if(this instanceof RefuelListReportActivity){
            reportTitle = "RefuelReport_";
            mReportDbHelper = new ReportDbAdapter(this, "reportRefuelListReportSelect", whereConditions);
            reportCursor = mReportDbHelper.fetchReport(-1);
        }
        reportTitle = Utils.appendDateTime(reportTitle, true, false, false);
        reportFileName = Utils.appendDateTime(reportTitle, true, true, true);

        if(reportFormatId == 0){
            reportFileName = reportFileName +".csv";
            reportContent = createCSVContent(reportCursor);
        }
        else if(reportFormatId == 1){
            reportFileName = reportFileName +".html";
            reportContent = createHTMLContent(reportCursor, reportTitle);
        }
        reportCursor.close();

        FileUtils fu = new FileUtils();
        i = fu.writeToFile(reportContent, reportFileName);
        if(i != -1){ //error
            if(fu.lastError != null)
                errorAlertBuilder.setMessage(mRes.getString(i) + "\n" + fu.lastError);
            else
                errorAlertBuilder.setMessage(mRes.getString(i));
           errorAlert = errorAlertBuilder.create();
           errorAlert.show();
           return false;
        }
        
        if(sendToMail){
            Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
            emailIntent.setType("text/html");
            emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, " AndiCar report " + reportTitle);
            emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "This email was sent by AndiCar (https://code.google.com/p/andicar/wiki/Welcome)");
            emailIntent.putExtra(android.content.Intent.EXTRA_STREAM, Uri.parse("file://" + StaticValues.reportFolder + reportFileName));
            emailIntent.setType("text/plain");
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
        }
        Toast toast = Toast.makeText( getApplicationContext(),
                    mRes.getString(R.string.REPORTLIST_REPORT_CREATED) + " " +
                        StaticValues.reportFolder, Toast.LENGTH_LONG );
        toast.show();

        return true;
    }

    public String createCSVContent(Cursor reportCursor){
        String reportContent = "";
        int i;
        for(i = 0; i< reportCursor.getColumnCount(); i++){
            if(i > 0)
                reportContent = reportContent + ",";
            reportContent = reportContent + reportCursor.getColumnName(i);
        }
        reportContent = reportContent + "\n";
        while(reportCursor != null && reportCursor.moveToNext()){
            for(i = 0; i< reportCursor.getColumnCount(); i++){
                if(i > 0)
                    reportContent = reportContent + ",";
                reportContent = reportContent  + reportCursor.getString(i).replaceAll(",", " ");
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
        for(i = 0; i< reportCursor.getColumnCount(); i++){
            reportContent = reportContent +
                                "<TH>" + reportCursor.getColumnName(i) + "</TH>\n";
        }
        reportContent = reportContent +
                            "</TR>\n"; //end table header

        while(reportCursor != null && reportCursor.moveToNext()){
            reportContent = reportContent +
                            "<TR VALIGN=TOP>\n";
            for(i = 0; i< reportCursor.getColumnCount(); i++){
                reportContent = reportContent +
                                "<TD>" + reportCursor.getString(i) + "</TD>\n";
            }
            reportContent = reportContent +
                            "</TR\n";
        }
        reportContent = reportContent +
                        "</table>\n" +
                        "<br><br><p align=\"center\"> Created with <a href=\"https://code.google.com/p/andicar/wiki/Welcome\" target=\"new\">AndiCar</a>\n" +
                    "</body>\n" +
                "</html>";

        return reportContent;
    }

}
