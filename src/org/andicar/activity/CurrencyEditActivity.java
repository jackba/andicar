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

import org.andicar.persistence.MainDbAdapter;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;

/**
 *
 * @author miki
 */
public class CurrencyEditActivity extends EditActivityBase {
    private EditText etName = null;
    private EditText etUserComment = null;
    private EditText etCode = null;
    private CheckBox ckIsActive = null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        etName = (EditText) findViewById(R.id.etName);
        etUserComment = (EditText) findViewById(R.id.etUserComment);
        etCode = (EditText) findViewById(R.id.etCode);
        ckIsActive = (CheckBox) findViewById(R.id.ckIsActive);

        String strOperationType = mBundleExtras.getString("Operation"); //E = edit, N = new

        if( strOperationType.equals( "E") ) {
            mRowId = mBundleExtras.getLong( MainDbAdapter.GEN_COL_ROWID_NAME );
            Cursor c = mDbAdapter.fetchRecord(MainDbAdapter.CURRENCY_TABLE_NAME,
                    MainDbAdapter.currencyTableColNames, mRowId);
            String strName = c.getString( MainDbAdapter.GEN_COL_NAME_POS );
            String strIsActive = c.getString( MainDbAdapter.GEN_COL_ISACTIVE_POS );
            String strUserComment = c.getString( MainDbAdapter.GEN_COL_USER_COMMENT_POS );
            String strCode = c.getString( MainDbAdapter.CURRENCY_COL_CODE_POS );
            if (strName != null) {
                etName.setText(strName);
            }
            if (strIsActive != null) {
                ckIsActive.setChecked(strIsActive.equals("Y"));
            }
            if (strUserComment != null) {
                etUserComment.setText( strUserComment );
            }
            if (strCode != null) {
                etCode.setText( strCode );
            }
            c.close();
        } else {
            ckIsActive.setChecked(true);
        }

    }

    @Override
    protected void saveData() {
        if(!beforeSave())
        	return;

        ContentValues cvData = new ContentValues();
        cvData.put( MainDbAdapter.GEN_COL_NAME_NAME,
                etName.getText().toString());
        cvData.put( MainDbAdapter.GEN_COL_ISACTIVE_NAME,
                (ckIsActive.isChecked() ? "Y" : "N") );
        cvData.put( MainDbAdapter.GEN_COL_USER_COMMENT_NAME,
                etUserComment.getText().toString() );
        cvData.put( MainDbAdapter.CURRENCY_COL_CODE_NAME,
                etCode.getText().toString());

        if (mRowId == -1) {
            long newId = mDbAdapter.createRecord(MainDbAdapter.CURRENCY_TABLE_NAME, cvData);
            if(newId < 0){
            	newId = -1 * newId;
                String strErrMsg = mResource.getString((int) newId);
                madbErrorAlert.setMessage(strErrMsg);
                madError = madbErrorAlert.create();
                madError.show();
            }
            else{
	            setResult(RESULT_OK, (new Intent()).putExtra("mRowId", newId));
	            finish();
            }
        } else {
            int iUpdateResult = mDbAdapter.updateRecord(MainDbAdapter.CURRENCY_TABLE_NAME, mRowId, cvData);
            if(iUpdateResult != -1){
                String strErrMsg = "";
                strErrMsg = mResource.getString(iUpdateResult);
                if(iUpdateResult == R.string.ERR_000)
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
        setContentView( R.layout.currency_edit_activity);
    }
}
