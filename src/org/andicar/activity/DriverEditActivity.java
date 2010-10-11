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
import android.database.Cursor;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

/**
 *
 * @author miki
 */
public class DriverEditActivity extends EditActivityBase {
    private EditText etName = null;
    private EditText etLicenseNo = null;
    private EditText etUserComment = null;
    private CheckBox ckIsActive = null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        etName = (EditText) findViewById(R.id.etName);
        etLicenseNo = (EditText) findViewById( R.id.etLicenseNo );
        etUserComment = (EditText) findViewById( R.id.etUserComment );
        ckIsActive = (CheckBox) findViewById(R.id.ckIsActive);
        
        String strOperationType = mBundleExtras.getString("Operation"); //E = edit, N = new

        if( strOperationType.equals( "E") ) {
            mRowId = mBundleExtras.getLong(MainDbAdapter.GEN_COL_ROWID_NAME);
            Cursor c = mDbAdapter.fetchRecord(MainDbAdapter.DRIVER_TABLE_NAME, MainDbAdapter.driverTableColNames, mRowId);

            String strName = c.getString( MainDbAdapter.GEN_COL_NAME_POS );
            String strIsActive = c.getString( MainDbAdapter.GEN_COL_ISACTIVE_POS );
            String strUserComment = c.getString( MainDbAdapter.GEN_COL_USER_COMMENT_POS );
            String strLicenseNo = c.getString( MainDbAdapter.DRIVER_COL_LICENSE_NO_POS);

            if (strName != null) {
                etName.setText(strName);
            }
            if (strLicenseNo != null) {
                etLicenseNo.setText( strLicenseNo );
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

        strRetVal = checkNumeric(vgRoot, false);
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
        cvData.put( MainDbAdapter.DRIVER_COL_LICENSE_NO_NAME,
                etLicenseNo.getText().toString());

        if (mRowId == -1) {
            mDbAdapter.createRecord(MainDbAdapter.DRIVER_TABLE_NAME, cvData);
            finish();
        } else {
            int strUpdateResult = mDbAdapter.updateRecord(MainDbAdapter.DRIVER_TABLE_NAME, mRowId, cvData);
            if(strUpdateResult != -1){
                String strErrMsg = "";
                strErrMsg = mResource.getString(strUpdateResult);
                if(strUpdateResult == R.string.ERR_000)
                    strErrMsg = strErrMsg + "\n" + mDbAdapter.lastErrorMessage;
                madbErrorAlert.setMessage(strErrMsg);
                madError = madbErrorAlert.create();
                madError.show();
            }
            else{
                finish();
            }
        }
    }

    @Override
    protected void setLayout() {
        setContentView(R.layout.driver_edit_activity);
    }

}
