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

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.andicar.activity.report.GPSTrackListReportActivity;
import org.andicar.persistence.MainDbAdapter;
import org.andicar.persistence.ReportDbAdapter;
import org.andicar.utils.AndiCarExceptionHandler;
import org.andicar.utils.AndiCarStatistics;
import org.andicar.utils.StaticValues;
import org.andicar.utils.Utils;


/**
 *
 * @author Miklos Keresztes
 */
public class ListActivityBase extends ListActivity {
    protected Cursor recordCursor = null;
    protected long mLongClickId = -1;
    protected boolean showInactiveRecords = false;
    protected Menu optionsMenu;
    protected Class mEditClass = null;
    protected Class mInsertClass = null;
    protected String mTableName = null;
    protected String[] mColumns = null;
    protected String mWhereCondition = null;
    protected String mOrderByColumn = null;
    protected int mLayoutId;
    protected String[] mDbMapFrom;
    protected int[] mLayoutIdTo;
    protected Resources mRes = null;
    protected SharedPreferences mPreferences;
    protected SharedPreferences.Editor mPrefEditor;
    protected MainDbAdapter mMainDbAdapter = null;
    protected Bundle extras = null;
    protected AlertDialog.Builder errorAlertBuilder;
    protected AlertDialog errorAlert;
    protected boolean isSendStatistics = true;
    protected boolean isSendCrashReport = true;
    protected ListView lvBaseList = null;
    protected SimpleCursorAdapter.ViewBinder mViewBinder;

    /** Use onCreate(Bundle icicle, OnItemClickListener mItemClickListener, Class editClass,
     *                  String tableName, String[] columns, String whereCondition, String orderByColumn,
     *                  int pLayoutId, String[] pDbMapFrom, int[] pLayoutIdTo)
     */
    @Override
    @Deprecated
    public void onCreate(Bundle icicle) {
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        if(isSendStatistics)
            AndiCarStatistics.sendFlurryStartSession(this);
    }
    @Override
    protected void onStop()
    {
        super.onStop();
        if(isSendStatistics)
            AndiCarStatistics.sendFlurryEndSession(this);
    }

    protected void onCreate(Bundle icicle, OnItemClickListener mItemClickListener, Class editClass, Class insertClass,
            String tableName, String[] columns, String whereCondition, String orderByColumn,
            int pLayoutId, String[] pDbMapFrom, int[] pLayoutIdTo, SimpleCursorAdapter.ViewBinder pViewBinder) {

        super.onCreate(icicle);

        mPreferences = getSharedPreferences(StaticValues.GLOBAL_PREFERENCE_NAME, 0);
        isSendStatistics = mPreferences.getBoolean("SendUsageStatistics", true);
        isSendCrashReport = mPreferences.getBoolean("SendCrashReport", true);
        if(isSendCrashReport)
            Thread.setDefaultUncaughtExceptionHandler(
                    new AndiCarExceptionHandler(Thread.getDefaultUncaughtExceptionHandler(), this));
        mRes = getResources();
        mPrefEditor = mPreferences.edit();
        mMainDbAdapter = new MainDbAdapter(this);

        mViewBinder = pViewBinder;

        if(extras == null) {
            extras = getIntent().getExtras();
        }

        lvBaseList = getListView();
        
        errorAlertBuilder = new AlertDialog.Builder(this);
        errorAlertBuilder.setCancelable(false);
        errorAlertBuilder.setPositiveButton(mRes.getString(R.string.GEN_OK), null);

        mEditClass = editClass;
        if(insertClass == null)
            mInsertClass = mEditClass;
        else
            mInsertClass = insertClass;
        
        mTableName = tableName;
        mColumns = columns;
        mWhereCondition = whereCondition;
        mOrderByColumn = orderByColumn;
        mLayoutId = pLayoutId;
        mDbMapFrom = pDbMapFrom;
        mLayoutIdTo = pLayoutIdTo;

        lvBaseList.setTextFilterEnabled(true);
        lvBaseList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lvBaseList.setOnItemClickListener(mItemClickListener);
        lvBaseList.setOnItemLongClickListener(mItemLongClickListener);
        registerForContextMenu(lvBaseList);

        fillData();

        if(/*!(this instanceof GPSTrackListReportActivity)
                &&*/ (getListAdapter() == null || getListAdapter().getCount() == 0)
                && mInsertClass != null) {
            Intent i = new Intent(this, mInsertClass);
            i.putExtra("Operation", "N");
            startActivityForResult(i, StaticValues.ACTIVITY_NEW_REQUEST_CODE);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        isSendStatistics = mPreferences.getBoolean("SendUsageStatistics", true);
        if(mMainDbAdapter == null)
            mMainDbAdapter = new MainDbAdapter(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mMainDbAdapter != null){
            mMainDbAdapter.close();
            mMainDbAdapter = null;
        }
    }

    protected AdapterView.OnItemLongClickListener mItemLongClickListener =
            new AdapterView.OnItemLongClickListener() {
                public boolean onItemLongClick(AdapterView parent, View v, int position, long id) {
                    mLongClickId = id;
                    return false;
                }
            };

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if(mEditClass == null)
            return;

        menu.add(0, StaticValues.CONTEXT_MENU_EDIT_ID, 0, mRes.getString(R.string.MENU_EditCaption));
        menu.add(0, StaticValues.CONTEXT_MENU_INSERT_ID, 0, mRes.getString(R.string.MENU_AddNewCaption));
        menu.add(0, StaticValues.CONTEXT_MENU_DELETE_ID, 0, mRes.getString(R.string.MENU_DeleteCaption));
        if(this instanceof GPSTrackListReportActivity){
            menu.add( 0, StaticValues.CONTEXT_MENU_SENDASEMAIL_ID, 0, mRes.getText( R.string.MENU_SendAsEmailCaption ));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(mEditClass == null)
            return false;
        optionsMenu = menu;
        optionsMenu.add(0, StaticValues.OPTION_MENU_ADD_ID, 0, mRes.getText(R.string.MENU_AddNewCaption)).setIcon(mRes.getDrawable(R.drawable.ic_menu_add));
        if(!showInactiveRecords) {
            optionsMenu.add(0, StaticValues.OPTION_MENU_SHOWINACTIVE_ID, 0, mRes.getText(R.string.MENU_ShowInactiveCaption)).setIcon(mRes.getDrawable(R.drawable.ic_menu_show_inactive));
        }
        else {
            optionsMenu.add(0, StaticValues.OPTION_MENU_HIDEINACTIVE_ID, 0, mRes.getText(R.string.MENU_HideInactiveCaption)).setIcon(mRes.getDrawable(R.drawable.ic_menu_show_active));
        }
        return true;
    }

    protected void startEditActivity(long id){
        Intent i = new Intent(this, mEditClass);
        i.putExtra(MainDbAdapter.GEN_COL_ROWID_NAME, id);
        if(mTableName.equals(MainDbAdapter.CAR_TABLE_NAME)) {
            i.putExtra("CurrentCar_ID", mPreferences.getLong("CurrentCar_ID", -1));
        }
        else if(mTableName.equals(MainDbAdapter.DRIVER_TABLE_NAME)) {
            i.putExtra("CurrentDriver_ID", mPreferences.getLong("CurrentDriver_ID", -1));
        }
        else if(mTableName.equals(MainDbAdapter.UOM_TABLE_NAME)) {
            i.putExtra(MainDbAdapter.UOM_COL_UOMTYPE_NAME, extras.getString(MainDbAdapter.UOM_COL_UOMTYPE_NAME));
        }
        i.putExtra("Operation", "E");

        startActivityForResult(i, StaticValues.ACTIVITY_EDIT_REQUEST_CODE);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case StaticValues.CONTEXT_MENU_EDIT_ID:
                startEditActivity(mLongClickId);
                return true;
            case StaticValues.CONTEXT_MENU_DELETE_ID:
                //check if the car is the selected car. If yes it cannot be deleted.
                if(mTableName.equals(MainDbAdapter.CAR_TABLE_NAME)
                        && mPreferences.getLong("CurrentCar_ID", -1) == mLongClickId) {
                    errorAlertBuilder.setMessage(mRes.getString(R.string.CarListActivity_CurrentCarDeleteMessage));
                    errorAlert = errorAlertBuilder.create();
                    errorAlert.show();
                    return true;
                }
                else {
                    if(mTableName.equals(MainDbAdapter.DRIVER_TABLE_NAME)
                            && mPreferences.getLong("CurrentDriver_ID", -1) == mLongClickId) {
                        errorAlertBuilder.setMessage(mRes.getString(R.string.DriverListActivity_CurrentDriverDeleteMessage));
                        errorAlert = errorAlertBuilder.create();
                        errorAlert.show();
                        return true;
                    }
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(ListActivityBase.this);
                builder.setMessage(mRes.getString(R.string.GEN_DeleteConfirmation));
                builder.setCancelable(false);
                builder.setPositiveButton(mRes.getString(R.string.GEN_YES),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                int deleteResult = mMainDbAdapter.deleteRecord(mTableName, mLongClickId);
                                if(deleteResult != -1) {
                                    errorAlertBuilder.setMessage(mRes.getString(deleteResult));
                                    errorAlert = errorAlertBuilder.create();
                                    errorAlert.show();
                                }
                                else {
                                    fillData();
                                }
                            }
                        });
                builder.setNegativeButton(mRes.getString(R.string.GEN_NO),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
                return true;
            case StaticValues.CONTEXT_MENU_INSERT_ID:
                Intent insertIntent = new Intent(this, mInsertClass);
                if(mTableName.equals(MainDbAdapter.UOM_TABLE_NAME)) {
                    insertIntent.putExtra(MainDbAdapter.UOM_COL_UOMTYPE_NAME, extras.getString(MainDbAdapter.UOM_COL_UOMTYPE_NAME));
                }
                else if(mTableName.equals(MainDbAdapter.MILEAGE_TABLE_NAME)) {
                    insertIntent.putExtra("CurrentCar_ID", mPreferences.getLong("CurrentCar_ID", -1));
                    insertIntent.putExtra("CurrentDriver_ID", mPreferences.getLong("CurrentDriver_ID", -1));
                    insertIntent.putExtra("CurrentDriver_Name", mPreferences.getString("CurrentDriver_Name", ""));
                    insertIntent.putExtra("CurrentCar_Name", mPreferences.getString("CurrentCar_Name", ""));
                }
                else if(mTableName.equals(MainDbAdapter.REFUEL_TABLE_NAME)) {
                    insertIntent.putExtra("CurrentCar_ID", mPreferences.getLong("CurrentCar_ID", -1));
                    insertIntent.putExtra("CurrentDriver_ID", mPreferences.getLong("CurrentDriver_ID", -1));
                    insertIntent.putExtra("CurrentDriver_Name", mPreferences.getString("CurrentDriver_Name", ""));
                    insertIntent.putExtra("CurrentCar_Name", mPreferences.getString("CurrentCar_Name", ""));
                }
                insertIntent.putExtra("Operation", "N");

                startActivityForResult(insertIntent, StaticValues.ACTIVITY_NEW_REQUEST_CODE);
                return true;
            case StaticValues.CONTEXT_MENU_SENDASEMAIL_ID:
                Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
                emailIntent.setType("text/html");
                emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, " AndiCar GPS Track");
                Bundle b = new Bundle();
                b.putString(MainDbAdapter.sqlConcatTableColumn(MainDbAdapter.GPSTRACK_TABLE_NAME, MainDbAdapter.GEN_COL_ROWID_NAME) + "=", Long.toString(mLongClickId));
                ReportDbAdapter reportDbAdapter = new ReportDbAdapter(this, "gpsTrackListViewSelect", b);
                Cursor reportCursor = reportDbAdapter.fetchReport(1);
                if(reportCursor.moveToFirst()){
                    String emailText =
                            reportCursor.getString(reportCursor.getColumnIndex(ReportDbAdapter.FIRST_LINE_LIST_NAME)) + "\n" +
                            reportCursor.getString(reportCursor.getColumnIndex(ReportDbAdapter.SECOND_LINE_LIST_NAME))
                                .replace("[%1]", mRes.getString(R.string.GPSTrackReport_GPSTrackVar_1))
                                .replace("[%2]", mRes.getString(R.string.GPSTrackReport_GPSTrackVar_2))
                                .replace("[%3]", mRes.getString(R.string.GPSTrackReport_GPSTrackVar_3))
                                .replace("[%4]", mRes.getString(R.string.GPSTrackReport_GPSTrackVar_4))
                                .replace("[%5]", mRes.getString(R.string.GPSTrackReport_GPSTrackVar_5) +
                                        Utils.getTimeString(reportCursor.getLong(reportCursor.getColumnIndex(ReportDbAdapter.FOURTH_LINE_LIST_NAME)), false))
                                .replace("[%6]", mRes.getString(R.string.GPSTrackReport_GPSTrackVar_6) +
                                        Utils.getTimeString(reportCursor.getLong(reportCursor.getColumnIndex(ReportDbAdapter.FIFTH_LINE_LIST_NAME)), false))
                                .replace("[%7]", mRes.getString(R.string.GPSTrackReport_GPSTrackVar_7))
                                .replace("[%8]", mRes.getString(R.string.GPSTrackReport_GPSTrackVar_8))
                                .replace("[%9]", mRes.getString(R.string.GPSTrackReport_GPSTrackVar_9))
                            + "\n" +
                            reportCursor.getString(reportCursor.getColumnIndex(ReportDbAdapter.THIRD_LINE_LIST_NAME));
                            reportCursor.close();
                            reportDbAdapter.close();
                    emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, emailText + "\nSent by AndiCar (http://sites.google.com/site/andicarfree/)");
                }
                //attach the trackfiles
                byte[] buf = new byte[1024];
                ZipOutputStream out = null;
                try {
                    out = new ZipOutputStream(new FileOutputStream(StaticValues.TRACK_FOLDER + "trackFiles.zip"));

                    reportCursor = mMainDbAdapter.fetchForTable(MainDbAdapter.GPSTRACKDETAIL_TABLE_NAME,
                                        MainDbAdapter.gpsTrackDetailTableColNames,
                                        MainDbAdapter.GPSTRACKDETAIL_COL_GPSTRACK_ID_NAME + "=" + Long.toString(mLongClickId),
                                        MainDbAdapter.GPSTRACKDETAIL_COL_FILE_NAME);
                    while(reportCursor.moveToNext()){
                        try{
                            FileInputStream in = new FileInputStream(reportCursor.getString(MainDbAdapter.GPSTRACKDETAIL_COL_FILE_POS));
                            //zip entry name
                            String entryName = reportCursor.getString(MainDbAdapter.GPSTRACKDETAIL_COL_FILE_POS).replace(StaticValues.TRACK_FOLDER, "");
                            out.putNextEntry(new ZipEntry(entryName));
                            // Transfer bytes from the file to the ZIP file
                            int len;
                            while ((len = in.read(buf)) > 0) {
                                    out.write(buf, 0, len);
                            }
                            // Complete the entry
                            out.closeEntry();
                            in.close();
                        }
                        catch(FileNotFoundException ex){}
                    }
                    out.close();
                    reportCursor.close();
                    Uri trackFile = Uri.parse("file://" + StaticValues.TRACK_FOLDER + "trackFiles.zip");
                    if(trackFile != null)
                        emailIntent.putExtra(android.content.Intent.EXTRA_STREAM, trackFile);
                } catch (IOException ex) {
                    Logger.getLogger(ListActivityBase.class.getName()).log(Level.SEVERE, null, ex);
                }


                startActivity(Intent.createChooser(emailIntent, "Send mail..."));

        }
        return super.onContextItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        switch(requestCode) {
            case StaticValues.ACTIVITY_NEW_REQUEST_CODE:
                fillData();
                break;
        }
    }

    protected void fillData() {
        String tmpWhere = mWhereCondition;
        if(!showInactiveRecords) {
            if(tmpWhere != null && tmpWhere.length() > 0) {
                tmpWhere = tmpWhere + MainDbAdapter.isActiveWithAndCondition;
            }
            else {
                tmpWhere = MainDbAdapter.isActiveCondition;
            }
        }

        recordCursor = mMainDbAdapter.fetchForTable(mTableName, mColumns, tmpWhere, mOrderByColumn);
        startManagingCursor(recordCursor);

        setListAdapter(null);

        if(recordCursor.getCount() == 0) {
            return;
        }

        SimpleCursorAdapter listCursorAdapter =
                new SimpleCursorAdapter(this, mLayoutId, recordCursor, mDbMapFrom, mLayoutIdTo);
        if(mViewBinder != null)
            listCursorAdapter.setViewBinder(mViewBinder);
        setListAdapter(listCursorAdapter);

        if(getListAdapter() != null && getListAdapter().getCount() == 1) {
            if(mTableName.equals(MainDbAdapter.CAR_TABLE_NAME)) {
                Cursor selectedRecord = mMainDbAdapter.fetchForTable(mTableName, MainDbAdapter.carTableColNames,
                        null, null);
                selectedRecord.moveToFirst();
                SharedPreferences.Editor editor = mPreferences.edit();
                editor.putLong("CurrentCar_ID", selectedRecord.getLong(MainDbAdapter.GEN_COL_ROWID_POS));
                editor.putString("CurrentCar_Name", selectedRecord.getString(MainDbAdapter.GEN_COL_NAME_POS).trim());
                editor.putLong("CarUOMLength_ID", selectedRecord.getLong(MainDbAdapter.CAR_COL_UOMLENGTH_ID_POS));
                editor.putLong("CarUOMVolume_ID", selectedRecord.getLong(MainDbAdapter.CAR_COL_UOMVOLUME_ID_POS));
                editor.putLong("CarCurrency_ID", selectedRecord.getLong(MainDbAdapter.CAR_COL_CURRENCY_ID_POS));
                editor.commit();
            }
            else {
                if(mTableName.equals(MainDbAdapter.DRIVER_TABLE_NAME)) {
                    Cursor selectedRecord = mMainDbAdapter.fetchForTable(mTableName, MainDbAdapter.driverTableColNames,
                            null, null);
                    selectedRecord.moveToFirst();
                    SharedPreferences.Editor editor = mPreferences.edit();
                    editor.putLong("CurrentDriver_ID", selectedRecord.getLong(MainDbAdapter.GEN_COL_ROWID_POS));
                    editor.putString("CurrentDriver_Name", selectedRecord.getString(MainDbAdapter.GEN_COL_NAME_POS).trim());
                    editor.commit();
                }
            }

            //|| mTableName.equals(MainDbAdapter.DRIVER_TABLE_NAME)){
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case StaticValues.OPTION_MENU_ADD_ID:
                Intent insertIntent = new Intent(this, mInsertClass);
                if(mTableName.equals(MainDbAdapter.UOM_TABLE_NAME)) {
                    insertIntent.putExtra(MainDbAdapter.UOM_COL_UOMTYPE_NAME, extras.getString(MainDbAdapter.UOM_COL_UOMTYPE_NAME));
                }
                else if(mTableName.equals(MainDbAdapter.MILEAGE_TABLE_NAME)) {
                    insertIntent.putExtra("CurrentCar_ID", mPreferences.getLong("CurrentCar_ID", -1));
                    insertIntent.putExtra("CurrentDriver_ID", mPreferences.getLong("CurrentDriver_ID", -1));
                    insertIntent.putExtra("CurrentDriver_Name", mPreferences.getString("CurrentDriver_Name", ""));
                    insertIntent.putExtra("CurrentCar_Name", mPreferences.getString("CurrentCar_Name", ""));
                }
                else if(mTableName.equals(MainDbAdapter.REFUEL_TABLE_NAME)) {
                    insertIntent.putExtra("CurrentCar_ID", mPreferences.getLong("CurrentCar_ID", -1));
                    insertIntent.putExtra("CurrentDriver_ID", mPreferences.getLong("CurrentDriver_ID", -1));
                    insertIntent.putExtra("CurrentDriver_Name", mPreferences.getString("CurrentDriver_Name", ""));
                    insertIntent.putExtra("CurrentCar_Name", mPreferences.getString("CurrentCar_Name", ""));
                }
                else if(mTableName.equals(MainDbAdapter.EXPENSES_TABLE_NAME)) {
                    insertIntent.putExtra("CurrentCar_ID", mPreferences.getLong("CurrentCar_ID", -1));
                    insertIntent.putExtra("CurrentDriver_ID", mPreferences.getLong("CurrentDriver_ID", -1));
                }
                insertIntent.putExtra("Operation", "N");
                startActivityForResult(insertIntent, StaticValues.ACTIVITY_NEW_REQUEST_CODE);
                return true;
            case StaticValues.OPTION_MENU_SHOWINACTIVE_ID:
                showInactiveRecords = true;
                fillData();
                optionsMenu.removeItem(StaticValues.OPTION_MENU_SHOWINACTIVE_ID);
                optionsMenu.removeItem(StaticValues.OPTION_MENU_HIDEINACTIVE_ID);
                optionsMenu.add(0, StaticValues.OPTION_MENU_HIDEINACTIVE_ID, 0, mRes.getText(R.string.MENU_HideInactiveCaption)).setIcon(mRes.getDrawable(R.drawable.ic_menu_show_active));
                return true;
            case StaticValues.OPTION_MENU_HIDEINACTIVE_ID:
                showInactiveRecords = false;
                fillData();
                optionsMenu.removeItem(StaticValues.OPTION_MENU_SHOWINACTIVE_ID);
                optionsMenu.removeItem(StaticValues.OPTION_MENU_HIDEINACTIVE_ID);
                optionsMenu.add(0, StaticValues.OPTION_MENU_SHOWINACTIVE_ID, 0, mRes.getText(R.string.MENU_ShowInactiveCaption)).setIcon(mRes.getDrawable(R.drawable.ic_menu_show_inactive));
                return true;
        }
        return true;
    }
}
