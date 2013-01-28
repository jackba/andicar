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

import org.andicar.activity.dialog.AndiCarDialogBuilder;
import org.andicar.activity.miscellaneous.GPSTrackMap;
import org.andicar.activity.report.GPSTrackListReportActivity;
import org.andicar.activity.report.MileageListReportActivity;
import org.andicar.activity.report.ToDoListReportActivity;
import org.andicar.persistence.MainDbAdapter;
import org.andicar.utils.AndiCarExceptionHandler;
import org.andicar.utils.AndiCarStatistics;
import org.andicar.utils.StaticValues;
import org.andicar.utils.Utils;
import org.andicar2.activity.R;

import android.content.Context;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;


/**
 *
 * @author Miklos Keresztes
 */
public class ListActivityBase extends ListActivity {
    protected Cursor recordCursor = null;
    protected long mLongClickId = -1;
    protected long mGpsTrackId = -1;
    protected boolean showInactiveRecords = false;
    protected Menu optionsMenu;
    @SuppressWarnings("rawtypes")
	protected Class mEditClass = null;
    @SuppressWarnings("rawtypes")
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
    protected MainDbAdapter mDbAdapter = null;
    protected Bundle mBundleExtras = null;
    protected AndiCarDialogBuilder errorAlertBuilder;
    protected AlertDialog errorAlert;
    protected boolean isSendStatistics = true;
    protected boolean isSendCrashReport = true;
    protected boolean isExitAfterInsert = false;
    protected boolean isInsertDisplayed = false;
    protected ListView lvBaseList = null;
    protected SimpleCursorAdapter.ViewBinder mViewBinder;
    protected int twolineListActivity = R.layout.twoline_list_activity_s01;
    protected int simpleListItem2 = R.layout.simple_list_item_2_s01;
    protected int threeLineListActivity = R.layout.threeline_list_activity_s01;
    protected int threeLineListReportActivity = R.layout.threeline_listreport_activity_s01;
    protected String uiStyle = "s01";

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

    @SuppressWarnings("rawtypes")
	protected void onCreate(Bundle icicle, OnItemClickListener mItemClickListener, Class editClass, Class insertClass,
            String tableName, String[] columns, String whereCondition, String orderByColumn,
            int pLayoutId, String[] pDbMapFrom, int[] pLayoutIdTo, SimpleCursorAdapter.ViewBinder pViewBinder) {

        super.onCreate(icicle);

        if(mPreferences == null)
        	mPreferences = getSharedPreferences(StaticValues.GLOBAL_PREFERENCE_NAME, Context.MODE_MULTI_PROCESS);

        isSendStatistics = mPreferences.getBoolean("SendUsageStatistics", true);
        isSendCrashReport = mPreferences.getBoolean("SendCrashReport", true);
        if(isSendCrashReport)
            Thread.setDefaultUncaughtExceptionHandler(
                    new AndiCarExceptionHandler(Thread.getDefaultUncaughtExceptionHandler(), this));
        mRes = getResources();
        mPrefEditor = mPreferences.edit();
        mDbAdapter = new MainDbAdapter(this);

        mViewBinder = pViewBinder;

        if(mBundleExtras == null)
            mBundleExtras = getIntent().getExtras();
         
        if(mBundleExtras != null && mBundleExtras.containsKey("ExitAfterInsert"))
        	isExitAfterInsert = mBundleExtras.getBoolean("ExitAfterInsert");
        

        lvBaseList = getListView();
        
        errorAlertBuilder = new AndiCarDialogBuilder(ListActivityBase.this, 
        		AndiCarDialogBuilder.DIALOGTYPE_ERROR, mRes.getString(R.string.GEN_Error));
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
        lvBaseList.setOnItemLongClickListener(mItemLongClickListener);
        registerForContextMenu(lvBaseList);
        if(mItemClickListener != null)
        	lvBaseList.setOnItemClickListener(mItemClickListener);
        else
        	lvBaseList.setOnItemClickListener(this.mItemClickListener);

        fillData();

//        if((getListAdapter() == null || getListAdapter().getCount() == 0)
//                	&& mInsertClass != null) {
//            long currentCarID = mPreferences.getLong("CurrentCar_ID", -1);
//            
//            Intent i = new Intent(this, mInsertClass);
//            i.putExtra("Operation", "N");
//            if(mInsertClass.equals(MileageEditActivity.class)){
//                i.putExtra("CurrentCar_ID", currentCarID);
//            }
//            startActivityForResult(i, StaticValues.ACTIVITY_NEW_REQUEST_CODE);
//        }
    }

	protected void initStyle() {
		if(mPreferences == null)
        	mPreferences = getSharedPreferences(StaticValues.GLOBAL_PREFERENCE_NAME, Context.MODE_MULTI_PROCESS);

		getListView().setBackgroundColor(Color.WHITE);
		uiStyle = "s01";
		simpleListItem2 = R.layout.simple_list_item_2_s01;
		twolineListActivity = R.layout.twoline_list_activity_s01;
		threeLineListActivity = R.layout.threeline_list_activity_s01;
		threeLineListReportActivity = R.layout.threeline_listreport_activity_s01;
	}

    @Override
    protected void onResume() {
        super.onResume();

        if((getListAdapter() == null || getListAdapter().getCount() == 0)
            	&& mInsertClass != null) {
        	if(!isInsertDisplayed){
        		isInsertDisplayed = true;
		        long currentCarID = mPreferences.getLong("CurrentCar_ID", -1);
		        Intent i = new Intent(this, mInsertClass);
		        i.putExtra("Operation", "N");
		        if(mInsertClass.equals(MileageEditActivity.class)){
		            i.putExtra("CurrentCar_ID", currentCarID);
		        }
		        startActivityForResult(i, StaticValues.ACTIVITY_NEW_REQUEST_CODE);
        	}
        	else{
        		finish();
        	}
	    }
        
        isSendStatistics = mPreferences.getBoolean("SendUsageStatistics", true);
        if(mDbAdapter == null)
            mDbAdapter = new MainDbAdapter(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(recordCursor != null && !recordCursor.isClosed())
            recordCursor.close();

        if(mDbAdapter != null){
            mDbAdapter.close();
            mDbAdapter = null;
        }
    }

    protected AdapterView.OnItemClickListener mItemClickListener =
            new OnItemClickListener() {
                public void onItemClick(AdapterView<?> av, View view, int i, long l) {
                        startEditActivity(l);
                }
    };

    protected AdapterView.OnItemLongClickListener mItemLongClickListener =
            new AdapterView.OnItemLongClickListener() {
                public boolean onItemLongClick(@SuppressWarnings("rawtypes") AdapterView parent, View v, int position, long id) {
                    mLongClickId = id;
                    return false;
                }
            };

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        
        if(mEditClass != null){
	        menu.add(0, StaticValues.CONTEXT_MENU_EDIT_ID, 0, mRes.getString(R.string.MENU_EditCaption));
	        menu.add(0, StaticValues.CONTEXT_MENU_INSERT_ID, 0, mRes.getString(R.string.MENU_AddNewCaption));
        }
        if(this instanceof ToDoListReportActivity){
        	Cursor c = mDbAdapter.fetchRecord(MainDbAdapter.TODO_TABLE_NAME, MainDbAdapter.todoTableColNames, mLongClickId);
        	if(c.getString(MainDbAdapter.TODO_COL_ISDONE_POS).equals("N"))
        		menu.add(0, StaticValues.CONTEXT_MENU_TODO_DONE_ID, 0, mRes.getString(R.string.ToDo_IsDoneCaption));
        	c.close();
        }
        else
        	menu.add(0, StaticValues.CONTEXT_MENU_DELETE_ID, 0, mRes.getString(R.string.MENU_DeleteCaption));

        if(this instanceof GPSTrackListReportActivity){
            menu.add( 0, StaticValues.CONTEXT_MENU_SENDASEMAIL_ID, 0, mRes.getText( R.string.MENU_SendAsEmailCaption ));
            menu.add( 0, StaticValues.CONTEXT_MENU_SHOWONMAP_ID, 0, mRes.getText( R.string.MENU_ShowOnMap ));
        }
        else if(this instanceof MileageListReportActivity){
            String selection = MainDbAdapter.GPSTRACK_COL_MILEAGE_ID_NAME + "= ? ";
            String[] selectionArgs = {Long.toString(mLongClickId)};
            Cursor c2 = mDbAdapter.query(MainDbAdapter.GPSTRACK_TABLE_NAME, MainDbAdapter.genColRowId,
                        selection, selectionArgs, null, null, null);
            if(c2.moveToFirst()){
            	mGpsTrackId =  c2.getLong(0);
                menu.add( 0, StaticValues.CONTEXT_MENU_OPENGPSTRACK_ID, 0, mRes.getText( R.string.MENU_OpenGPSTrack ));
            }
            c2.close();
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
    	if(mEditClass == null)
    		return;

    	Intent i = new Intent(this, mEditClass);
        if(mTableName.equals(MainDbAdapter.TODO_TABLE_NAME)) {
        	Cursor c = mDbAdapter.fetchRecord(MainDbAdapter.TODO_TABLE_NAME, MainDbAdapter.todoTableColNames, id);
        	long taskId = c.getLong(MainDbAdapter.TODO_COL_TASK_ID_POS);
        	c.close();
        	i.putExtra(MainDbAdapter.GEN_COL_ROWID_NAME, taskId);
        }
        else
        	i.putExtra(MainDbAdapter.GEN_COL_ROWID_NAME, id);
        
        if(mTableName.equals(MainDbAdapter.CAR_TABLE_NAME)) {
            i.putExtra("CurrentCar_ID", mPreferences.getLong("CurrentCar_ID", -1));
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
                AndiCarDialogBuilder builder = new AndiCarDialogBuilder(ListActivityBase.this, 
                		AndiCarDialogBuilder.DIALOGTYPE_QUESTION, mRes.getString(R.string.GEN_Confirm));
                builder.setMessage(mRes.getString(R.string.GEN_DeleteConfirmation));
                builder.setCancelable(false);
                builder.setPositiveButton(mRes.getString(R.string.GEN_YES),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                int deleteResult = mDbAdapter.deleteRecord(mTableName, mLongClickId);
                                if(deleteResult != -1) {
                                	//issue #34
                                	try{
                                		errorAlertBuilder.setMessage(mRes.getString(deleteResult));
                                	}
                                	catch(Resources.NotFoundException e){
                                		errorAlertBuilder.setMessage(mRes.getString(R.string.ERR_000));
                                	}
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
                if(mTableName.equals(MainDbAdapter.MILEAGE_TABLE_NAME)) {
                    insertIntent.putExtra("CurrentCar_ID", mPreferences.getLong("CurrentCar_ID", -1));
                }
                else if(mTableName.equals(MainDbAdapter.REFUEL_TABLE_NAME)) {
                    insertIntent.putExtra("CurrentCar_ID", mPreferences.getLong("CurrentCar_ID", -1));
                }
                else if(mTableName.equals(MainDbAdapter.EXPENSECATEGORY_TABLE_NAME))
                    insertIntent.putExtra("IsFuel", mBundleExtras.getBoolean("IsFuel"));
                
                insertIntent.putExtra("Operation", "N");

                startActivityForResult(insertIntent, StaticValues.ACTIVITY_NEW_REQUEST_CODE);
                return true;
            case StaticValues.CONTEXT_MENU_SENDASEMAIL_ID:
                Utils u = new Utils();
                u.sendGPSTrackAsEmail(this, mRes, mLongClickId);
                return true;
            case StaticValues.CONTEXT_MENU_SHOWONMAP_ID:
                Intent gpstrackShowMapIntent = new Intent(this, GPSTrackMap.class);
                gpstrackShowMapIntent.putExtra("gpsTrackId", Long.toString(mLongClickId));
                startActivity(gpstrackShowMapIntent);
                return true;
            case StaticValues.CONTEXT_MENU_OPENGPSTRACK_ID:
            	if(mGpsTrackId > -1){
	                Intent gpsTrackEditIntent = new Intent(this, GPSTrackEditActivity.class);
	                gpsTrackEditIntent.putExtra(MainDbAdapter.GEN_COL_ROWID_NAME, mGpsTrackId);
	                startActivity(gpsTrackEditIntent);
            	}
                return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if(isExitAfterInsert)
        	finish();
        
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
        recordCursor = mDbAdapter.query(mTableName, mColumns, tmpWhere, null, null, null, mOrderByColumn);
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

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case StaticValues.OPTION_MENU_ADD_ID:
                Intent insertIntent = new Intent(this, mInsertClass);
                if(mTableName.equals(MainDbAdapter.MILEAGE_TABLE_NAME)) {
                    insertIntent.putExtra("CurrentCar_ID", mPreferences.getLong("CurrentCar_ID", -1));
                }
                else if(mTableName.equals(MainDbAdapter.REFUEL_TABLE_NAME)) {
                    insertIntent.putExtra("CurrentCar_ID", mPreferences.getLong("CurrentCar_ID", -1));
                }
                else if(mTableName.equals(MainDbAdapter.EXPENSE_TABLE_NAME)) {
                    insertIntent.putExtra("CurrentCar_ID", mPreferences.getLong("CurrentCar_ID", -1));
                }
                else if(mTableName.equals(MainDbAdapter.EXPENSECATEGORY_TABLE_NAME))
                    insertIntent.putExtra("IsFuel", mBundleExtras.getBoolean("IsFuel"));

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

	/* (non-Javadoc)
	 * @see android.app.Activity#onPrepareDialog(int, android.app.Dialog)
	 */
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
		LinearLayout fakeFocus = (LinearLayout)dialog.findViewById(R.id.fakeFocus);
		if(fakeFocus != null)
			fakeFocus.requestFocus();
	}

    
}
