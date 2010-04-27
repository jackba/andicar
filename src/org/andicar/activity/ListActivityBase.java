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
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import org.andicar.persistence.MainDbAdapter;
import org.andicar.utils.AndiCarExceptionHandler;
import org.andicar.utils.StaticValues;

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
    boolean isSendStatistics = true;

    /** Use instead  */
    @Override
    @Deprecated
    public void onCreate(Bundle icicle) {
    }

    protected void onCreate(Bundle icicle, OnItemClickListener mItemClickListener, Class editClass,
            String tableName, String[] columns, String whereCondition, String orderByColumn,
            int pLayoutId, String[] pDbMapFrom, int[] pLayoutIdTo) {

        super.onCreate(icicle);

        mPreferences = getSharedPreferences(StaticValues.GLOBAL_PREFERENCE_NAME, 0);
        isSendStatistics = mPreferences.getBoolean("SendUsageStatistics", true);
        if(isSendStatistics)
            Thread.setDefaultUncaughtExceptionHandler(
                    new AndiCarExceptionHandler(Thread.getDefaultUncaughtExceptionHandler(), this));
        mRes = getResources();
        mPrefEditor = mPreferences.edit();
        mMainDbAdapter = new MainDbAdapter(this);

        if(extras == null) {
            extras = getIntent().getExtras();
        }


        errorAlertBuilder = new AlertDialog.Builder(this);
        errorAlertBuilder.setCancelable(false);
        errorAlertBuilder.setPositiveButton(mRes.getString(R.string.GEN_OK), null);

        mEditClass = editClass;
        mTableName = tableName;
        mColumns = columns;
        mWhereCondition = whereCondition;
        mOrderByColumn = orderByColumn;
        mLayoutId = pLayoutId;
        mDbMapFrom = pDbMapFrom;
        mLayoutIdTo = pLayoutIdTo;

        ListView lvBaseList = getListView();
        lvBaseList.setTextFilterEnabled(true);
        lvBaseList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lvBaseList.setOnItemClickListener(mItemClickListener);
        lvBaseList.setOnItemLongClickListener(mItemLongClickListener);
        registerForContextMenu(lvBaseList);

        fillData();

        if(/*!(this instanceof ReportListActivityBase)
                && */ (getListAdapter() == null || getListAdapter().getCount() == 0)
                && editClass != null) {
            Intent i = new Intent(this, editClass);
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

        menu.add(0, StaticValues.CONTEXT_MENU_EDIT_ID, 0, mRes.getString(R.string.MENU_EDIT_CAPTION));
        menu.add(0, StaticValues.CONTEXT_MENU_INSERT_ID, 0, mRes.getString(R.string.MENU_ADD_NEW_CAPTION));
        menu.add(0, StaticValues.CONTEXT_MENU_DELETE_ID, 0, mRes.getString(R.string.MENU_DELETE_CAPTION));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(mEditClass == null)
            return false;
        optionsMenu = menu;
        optionsMenu.add(0, StaticValues.OPTION_MENU_ADD_ID, 0, mRes.getText(R.string.MENU_ADD_NEW_CAPTION)).setIcon(mRes.getDrawable(R.drawable.ic_menu_add));
        if(!showInactiveRecords) {
            optionsMenu.add(0, StaticValues.OPTION_MENU_SHOWINACTIVE_ID, 0, mRes.getText(R.string.MENU_SHOWINACTIVE_CAPTION)).setIcon(mRes.getDrawable(R.drawable.ic_menu_show_inactive));
        }
        else {
            optionsMenu.add(0, StaticValues.OPTION_MENU_HIDEINACTIVE_ID, 0, mRes.getText(R.string.MENU_HIDEINACTIVE_CAPTION)).setIcon(mRes.getDrawable(R.drawable.ic_menu_show_active));
        }
        return true;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case StaticValues.CONTEXT_MENU_EDIT_ID:
                Intent i = new Intent(this, mEditClass);
                i.putExtra(MainDbAdapter.GEN_COL_ROWID_NAME, mLongClickId);
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
                return true;
            case StaticValues.CONTEXT_MENU_DELETE_ID:
                //check if the car is the selected car. If yes it cannot be deleted.
                if(mTableName.equals(MainDbAdapter.CAR_TABLE_NAME)
                        && mPreferences.getLong("CurrentCar_ID", -1) == mLongClickId) {
                    errorAlertBuilder.setMessage(mRes.getString(R.string.CURRENT_CAR_DELETE_ERROR_MESSAGE));
                    errorAlert = errorAlertBuilder.create();
                    errorAlert.show();
                    return true;
                }
                else {
                    if(mTableName.equals(MainDbAdapter.DRIVER_TABLE_NAME)
                            && mPreferences.getLong("CurrentDriver_ID", -1) == mLongClickId) {
                        errorAlertBuilder.setMessage(mRes.getString(R.string.CURRENT_DRIVER_DELETE_ERROR_MESSAGE));
                        errorAlert = errorAlertBuilder.create();
                        errorAlert.show();
                        return true;
                    }
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(ListActivityBase.this);
                builder.setMessage(mRes.getString(R.string.GEN_DELETE_CONFIRM));
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
                Intent insertIntent = new Intent(this, mEditClass);
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

        SimpleCursorAdapter cursorAdapter =
                new SimpleCursorAdapter(this, mLayoutId, recordCursor, mDbMapFrom, mLayoutIdTo);

        setListAdapter(cursorAdapter);

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
                Intent insertIntent = new Intent(this, mEditClass);
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
                optionsMenu.add(0, StaticValues.OPTION_MENU_HIDEINACTIVE_ID, 0, mRes.getText(R.string.MENU_HIDEINACTIVE_CAPTION)).setIcon(mRes.getDrawable(R.drawable.ic_menu_show_active));
                return true;
            case StaticValues.OPTION_MENU_HIDEINACTIVE_ID:
                showInactiveRecords = false;
                fillData();
                optionsMenu.removeItem(StaticValues.OPTION_MENU_SHOWINACTIVE_ID);
                optionsMenu.removeItem(StaticValues.OPTION_MENU_HIDEINACTIVE_ID);
                optionsMenu.add(0, StaticValues.OPTION_MENU_SHOWINACTIVE_ID, 0, mRes.getText(R.string.MENU_SHOWINACTIVE_CAPTION)).setIcon(mRes.getDrawable(R.drawable.ic_menu_show_inactive));
                return true;
        }
        return true;
    }
}
