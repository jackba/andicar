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
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import org.andicar.persistence.MainDbAdapter;
import org.andicar.utils.StaticValues;

/**
 *
 * @author miki
 */
public class BPartnerEditActivity extends EditActivityBase {
    private EditText etName = null;
    private EditText etUserComment = null;
    private CheckBox ckIsActive = null;
    private ListView lvAddressList = null;
    private Cursor cAddressCursor = null;

    protected boolean showInactiveRecords = false;
    protected Menu optionsMenu;

    long mLongClickId = -1;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        String strOperationType = mBundleExtras.getString("Operation"); //E = edit, N = new

        init();

        if( strOperationType.equals( "E") ) {
            mRowId = mBundleExtras.getLong(MainDbAdapter.GEN_COL_ROWID_NAME);
            Cursor c = mDbAdapter.fetchRecord(MainDbAdapter.BPARTNER_TABLE_NAME,
                    MainDbAdapter.bpartnerTableColNames, mRowId);

            String strName = c.getString( MainDbAdapter.GEN_COL_NAME_POS );
            String strIsActive = c.getString( MainDbAdapter.GEN_COL_ISACTIVE_POS );
            String strUserComment = c.getString( MainDbAdapter.GEN_COL_USER_COMMENT_POS );

            if (strName != null) {
                etName.setText(strName);
            }
            if (strIsActive != null) {
                ckIsActive.setChecked(strIsActive.equals("Y"));
            }
            if (strUserComment != null) {
                etUserComment.setText( strUserComment );
            }
            c.close();
        } else {
            ckIsActive.setChecked(true);
        }

        initControls();

    }

    private void init() {
        etName = (EditText) findViewById(R.id.etName);
        etUserComment = (EditText) findViewById( R.id.etUserComment );
        ckIsActive = (CheckBox) findViewById(R.id.ckIsActive);
        lvAddressList = (ListView)findViewById(R.id.lvAddressList);
    }

    private void initControls(){
        lvAddressList.setOnItemClickListener(mItemClickListener);
        lvAddressList.setOnItemLongClickListener(mItemLongClickListener);
        lvAddressList.setOnCreateContextMenuListener(this);
        fillData();
    }

    private void fillData(){
        String selection = MainDbAdapter.BPARTNER_LOCATION_BPARTNER_ID_NAME + "= ? ";
        String[] selectionArgs = {Long.toString(mRowId)};
        if(!showInactiveRecords)
            selection = selection +
                                " AND " + MainDbAdapter.GEN_COL_ISACTIVE_NAME + " = \'Y\'";

        cAddressCursor = mDbAdapter.query(MainDbAdapter.BPARTNER_LOCATION_TABLE_NAME, MainDbAdapter.genColName, 
                selection, selectionArgs, null, null, MainDbAdapter.GEN_COL_NAME_NAME);
//                fetchForTable(MainDbAdapter.BPARTNER_LOCATION_TABLE_NAME,
//                    MainDbAdapter.genColName, selection, MainDbAdapter.GEN_COL_NAME_NAME);
        startManagingCursor(cAddressCursor);

        lvAddressList.setAdapter(null);

        SimpleCursorAdapter listCursorAdapter =
                new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2,
                cAddressCursor, new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, new int[]{android.R.id.text1});
        lvAddressList.setAdapter(listCursorAdapter);
    }

    @Override
    protected void saveData() {
        String strRetVal = checkMandatory(vgRoot);
        if( strRetVal != null ) {
            Toast toast = Toast.makeText( getApplicationContext(),
                    mResource.getString( R.string.GEN_FillMandatory ) + ": " + strRetVal, Toast.LENGTH_SHORT );
            toast.show();
            return;
        }

        strRetVal = checkNumeric(vgRoot);
        if( strRetVal != null ) {
            Toast toast = Toast.makeText( getApplicationContext(),
                    mResource.getString( R.string.GEN_NumberFormatException ) + ": " + strRetVal, Toast.LENGTH_SHORT );
            toast.show();
            return;
        }

        ContentValues cvData = new ContentValues();
        cvData.put( MainDbAdapter.GEN_COL_NAME_NAME,
                etName.getText().toString());
        cvData.put( MainDbAdapter.GEN_COL_ISACTIVE_NAME,
                (ckIsActive.isChecked() ? "Y" : "N") );
        cvData.put( MainDbAdapter.GEN_COL_USER_COMMENT_NAME,
                etUserComment.getText().toString() );

        if (mRowId == -1) {
            mDbAdapter.createRecord(MainDbAdapter.BPARTNER_TABLE_NAME, cvData);
            finish();
        } else {
            int strUpdateResult = mDbAdapter.updateRecord(MainDbAdapter.BPARTNER_TABLE_NAME, mRowId, cvData);
            if(strUpdateResult != -1){
                String strErrMsg = "";
                strErrMsg = mResource.getString(strUpdateResult);
                if(strUpdateResult == R.string.ERR_000)
                    strErrMsg = strErrMsg + "\n" + mDbAdapter.lastErrorMessage;
                madbErrorAlert.setMessage(strErrMsg);
                madError = madbErrorAlert.create();
                madError.show();
            }
            else
                finish();
        }
    }

    @Override
    protected void setLayout() {
        setContentView(R.layout.bpartner_edit_activity);
    }

    protected AdapterView.OnItemClickListener mItemClickListener =
            new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> av, View view, int i, long l) {
                    Intent intent = new Intent(BPartnerEditActivity.this, BPartnerLocationEditActivity.class);
                    intent.putExtra(MainDbAdapter.GEN_COL_ROWID_NAME, l);
                    intent.putExtra("BPartnerID", mRowId);
                    intent.putExtra("Operation", "E");
                    startActivityForResult(intent, StaticValues.ACTIVITY_EDIT_REQUEST_CODE);
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

        menu.add(0, StaticValues.CONTEXT_MENU_EDIT_ID, 0, mResource.getString(R.string.MENU_EditCaption));
        menu.add(0, StaticValues.CONTEXT_MENU_INSERT_ID, 0, mResource.getString(R.string.MENU_AddNewCaption));
        menu.add(0, StaticValues.CONTEXT_MENU_DELETE_ID, 0, mResource.getString(R.string.MENU_DeleteCaption));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        optionsMenu = menu;
        optionsMenu.add(0, StaticValues.OPTION_MENU_ADD_ID, 0, 
                mResource.getText(R.string.MENU_AddNewCaption)).setIcon(mResource.getDrawable(R.drawable.ic_menu_add));
        if(!showInactiveRecords) {
            optionsMenu.add(0, StaticValues.OPTION_MENU_SHOWINACTIVE_ID, 0, 
                    mResource.getText(R.string.MENU_ShowInactiveCaption)).setIcon(mResource.getDrawable(R.drawable.ic_menu_show_inactive));
        }
        else {
            optionsMenu.add(0, StaticValues.OPTION_MENU_HIDEINACTIVE_ID, 0, 
                    mResource.getText(R.string.MENU_HideInactiveCaption)).setIcon(mResource.getDrawable(R.drawable.ic_menu_show_active));
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case StaticValues.OPTION_MENU_ADD_ID:
                Intent insertIntent = new Intent(this, BPartnerLocationEditActivity.class);
                insertIntent.putExtra("BPartnerID", mRowId);
                insertIntent.putExtra("Operation", "N");
                startActivityForResult(insertIntent, StaticValues.ACTIVITY_NEW_REQUEST_CODE);
                return true;
            case StaticValues.OPTION_MENU_SHOWINACTIVE_ID:
                showInactiveRecords = true;
                fillData();
                optionsMenu.removeItem(StaticValues.OPTION_MENU_SHOWINACTIVE_ID);
                optionsMenu.removeItem(StaticValues.OPTION_MENU_HIDEINACTIVE_ID);
                optionsMenu.add(0, StaticValues.OPTION_MENU_HIDEINACTIVE_ID, 0, 
                        mResource.getText(R.string.MENU_HideInactiveCaption)).setIcon(mResource.getDrawable(R.drawable.ic_menu_show_active));
                return true;
            case StaticValues.OPTION_MENU_HIDEINACTIVE_ID:
                showInactiveRecords = false;
                fillData();
                optionsMenu.removeItem(StaticValues.OPTION_MENU_SHOWINACTIVE_ID);
                optionsMenu.removeItem(StaticValues.OPTION_MENU_HIDEINACTIVE_ID);
                optionsMenu.add(0, StaticValues.OPTION_MENU_SHOWINACTIVE_ID, 0, 
                        mResource.getText(R.string.MENU_ShowInactiveCaption)).setIcon(mResource.getDrawable(R.drawable.ic_menu_show_inactive));
                return true;
        }
        return true;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Intent insertIntent;
        switch(item.getItemId()) {
            case StaticValues.CONTEXT_MENU_EDIT_ID:
                insertIntent = new Intent(this, BPartnerLocationEditActivity.class);
                insertIntent.putExtra("BPartnerID", mRowId);
                insertIntent.putExtra(MainDbAdapter.GEN_COL_ROWID_NAME, mLongClickId);
                insertIntent.putExtra("Operation", "E");
                startActivityForResult(insertIntent, StaticValues.ACTIVITY_NEW_REQUEST_CODE);
                return true;
            case StaticValues.CONTEXT_MENU_DELETE_ID:
                //check if the car is the selected car. If yes it cannot be deleted.
                AlertDialog.Builder builder = new AlertDialog.Builder(BPartnerEditActivity.this);
                builder.setMessage(mResource.getString(R.string.GEN_DeleteConfirmation));
                builder.setCancelable(false);
                builder.setPositiveButton(mResource.getString(R.string.GEN_YES),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                int deleteResult = mDbAdapter.deleteRecord(MainDbAdapter.BPARTNER_LOCATION_TABLE_NAME, mLongClickId);
                                if(deleteResult != -1) {
                                    madbErrorAlert.setMessage(mResource.getString(deleteResult));
                                    madError = madbErrorAlert.create();
                                    madError.show();
                                }
                                else {
                                    fillData();
                                }
                            }
                        });
                builder.setNegativeButton(mResource.getString(R.string.GEN_NO),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
                return true;
            case StaticValues.CONTEXT_MENU_INSERT_ID:
                insertIntent = new Intent(this, BPartnerLocationEditActivity.class);
                insertIntent.putExtra("Operation", "N");
                insertIntent.putExtra("BPartnerID", mRowId);

                startActivityForResult(insertIntent, StaticValues.ACTIVITY_NEW_REQUEST_CODE);
                return true;
        }
        return super.onContextItemSelected(item);
    }
}
