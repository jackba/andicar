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
import org.andicar2.activity.R;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;

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
            mRowId = mBundleExtras.getLong(MainDbAdapter.COL_NAME_GEN_ROWID);
            Cursor c = mDbAdapter.fetchRecord(MainDbAdapter.TABLE_NAME_DRIVER, MainDbAdapter.COL_LIST_DRIVER_TABLE, mRowId);

            String strName = c.getString( MainDbAdapter.COL_POS_GEN_NAME );
            String strIsActive = c.getString( MainDbAdapter.COL_POS_GEN_ISACTIVE );
            String strUserComment = c.getString( MainDbAdapter.COL_POS_GEN_USER_COMMENT );
            String strLicenseNo = c.getString( MainDbAdapter.COL_POS_DRIVER__LICENSE_NO);

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
    protected boolean saveData() {

    	ContentValues cvData = new ContentValues();
        cvData.put( MainDbAdapter.COL_NAME_GEN_NAME,
                etName.getText().toString());
        cvData.put( MainDbAdapter.COL_NAME_GEN_ISACTIVE,
                (ckIsActive.isChecked() ? "Y" : "N") );
        cvData.put( MainDbAdapter.COL_NAME_GEN_USER_COMMENT,
                etUserComment.getText().toString() );
        cvData.put( MainDbAdapter.COL_NAME_DRIVER__LICENSE_NO,
                etLicenseNo.getText().toString());

        int dbRetVal = -1;
        String strErrMsg = null;
        if (mRowId == -1) {
        	dbRetVal = ((Long)mDbAdapter.createRecord(MainDbAdapter.TABLE_NAME_DRIVER, cvData)).intValue();
            if(dbRetVal > 0){
            	finish();
            	return true;
            }
            else{
                if(dbRetVal == -1) //DB Error
                    strErrMsg = mDbAdapter.lastErrorMessage;
                else //precondition error
                    strErrMsg = mResource.getString(-1 * dbRetVal);

                madbErrorAlert.setMessage(strErrMsg);
                madError = madbErrorAlert.create();
                madError.show();
                return false;
            }
        } else {
        	dbRetVal = mDbAdapter.updateRecord(MainDbAdapter.TABLE_NAME_DRIVER, mRowId, cvData);
            if(dbRetVal != -1){
                strErrMsg = mResource.getString(dbRetVal);
                if(dbRetVal == R.string.ERR_000)
                    strErrMsg = strErrMsg + "\n" + mDbAdapter.lastErrorMessage;
                madbErrorAlert.setMessage(strErrMsg);
                madError = madbErrorAlert.create();
                madError.show();
                return false;
            }
            else{
                finish();
                return true;
            }
        }
    }

    @Override
    protected void setLayout() {
   		setContentView(R.layout.driver_edit_activity_s01);
    }

	/* (non-Javadoc)
	 * @see org.andicar.activity.BaseActivity#setSpecificLayout()
	 */
	@Override
	public void setSpecificLayout() {
	}

	/* (non-Javadoc)
	 * @see org.andicar.activity.EditActivityBase#setDefaultValues()
	 */
	@Override
	public void setDefaultValues() {
	}

}
