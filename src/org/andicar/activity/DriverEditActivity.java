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

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import org.andicar.persistence.MainDbAdapter;

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
        super.onCreate(icicle, R.layout.driver_edit_activity, mOkClickListener);

        etName = (EditText) findViewById(R.id.etName);
        etLicenseNo = (EditText) findViewById( R.id.etLicenseNo );
        etUserComment = (EditText) findViewById( R.id.etUserComment );
        ckIsActive = (CheckBox) findViewById(R.id.ckIsActive);
        
        String strOperationType = mBundleExtras.getString("Operation"); //E = edit, N = new

        if( strOperationType.equals( "E") ) {
            mRowId = mBundleExtras.getLong(MainDbAdapter.GEN_COL_ROWID_NAME);
            Cursor dbcRecordCursor = mDbAdapter.fetchRecord(MainDbAdapter.DRIVER_TABLE_NAME, MainDbAdapter.driverTableColNames, mRowId);

            String strName = dbcRecordCursor.getString( MainDbAdapter.GEN_COL_NAME_POS );
            String strIsActive = dbcRecordCursor.getString( MainDbAdapter.GEN_COL_ISACTIVE_POS );
            String strUserComment = dbcRecordCursor.getString( MainDbAdapter.GEN_COL_USER_COMMENT_POS );
            String strLicenseNo = dbcRecordCursor.getString( MainDbAdapter.DRIVER_COL_LICENSE_NO_POS);

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

            //cannot be inactivated if is the current driver
            if (mBundleExtras.getLong("CurrentDriver_ID") == mRowId) {
                ckIsActive.setClickable( false );
                ckIsActive.setOnTouchListener( new View.OnTouchListener() {
                    public boolean onTouch( View arg0, MotionEvent arg1 )
                    {
                        Toast toast = Toast.makeText( getApplicationContext(),
                                mResource.getString(R.string.CURRENT_DRIVER_INACTIVATE_ERROR_MESSAGE), Toast.LENGTH_SHORT );
                        toast.show();
                        return false;
                    }
                });
            }
        } else {
            ckIsActive.setChecked(true);
        }

    }

    private View.OnClickListener mOkClickListener = 
                new View.OnClickListener() {

                    public void onClick(View v) {
                        //check mandatory fields
                        String strRetVal = checkMandatory((ViewGroup) findViewById(R.id.vgRoot));
                        if( strRetVal != null ) {
                            Toast toast = Toast.makeText( getApplicationContext(),
                                    mResource.getString( R.string.GEN_FILL_MANDATORY ) + ": " + strRetVal, Toast.LENGTH_SHORT );
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

                        if (mRowId == null) {
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
                            else
                                finish();
                        }
                    }
                };

}
