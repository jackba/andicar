/*
 *  AndiCar - a car management software for Android powered devices.
 *
 *  Copyright (C) 2013 Miklos Keresztes (miklos.keresztes@gmail.com)
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

import org.andicar2.activity.R;
import org.andicar.persistence.AddOnDBAdapter;
import org.andicar.persistence.DB;
import org.andicar.persistence.MainDbAdapter;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;


public class SecureBackupConfig extends EditActivityBase {
	
	private EditText etSMTPUserFrom = null;
	private EditText etSMTPUserPassword = null;
	private EditText etSMTPEmailTo = null;
    private CheckBox ckIsIncludeGPSTrack = null;
    private CheckBox ckIsIncludeReports = null;
    private CheckBox ckIsShowNotification = null;
    private CheckBox ckIsActive = null;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		etSMTPUserFrom = (EditText)findViewById(R.id.etSMTPUserFrom);
		etSMTPUserPassword = (EditText)findViewById(R.id.etSMTPUserPassword);
		etSMTPEmailTo = (EditText)findViewById(R.id.etSMTPEmailTo);
		ckIsIncludeGPSTrack = (CheckBox)findViewById(R.id.ckIsIncludeGPSTrack);
		ckIsIncludeReports = (CheckBox)findViewById(R.id.ckIsIncludeReports);
		ckIsShowNotification = (CheckBox)findViewById(R.id.ckIsShowNotification);
		ckIsActive = (CheckBox)findViewById(R.id.ckIsActive);

		etSMTPUserFrom.setText(mPreferences.getString("smtpAuthUserName", ""));
		etSMTPUserPassword.setText(mPreferences.getString("smtpAuthPassword", ""));
		etSMTPEmailTo.setText(mPreferences.getString("bkFileToEmailAddress", ""));
		
		Cursor c = mDbAdapter.query(AddOnDBAdapter.ADDON_SECURE_BK_SETTINGS_TABLE_NAME, 
        		AddOnDBAdapter.addonSecureBKSettingsTableColNames, 
				null, null, null, null, null);
		
        //just one record should be in this table!!!
		if(c.moveToFirst()){
			mRowId = c.getLong(DB.GEN_COL_ROWID_POS);
			ckIsIncludeGPSTrack.setChecked(c.getString(AddOnDBAdapter.ADDON_SECURE_BK_SETTINGS_COL_ISINCLUDEGPS_POS).equals("Y"));
			ckIsIncludeReports.setChecked(c.getString(AddOnDBAdapter.ADDON_SECURE_BK_SETTINGS_COL_ISINCLUDEREPORTS_POS).equals("Y"));
			ckIsShowNotification.setChecked(c.getString(AddOnDBAdapter.ADDON_SECURE_BK_SETTINGS_COL_ISSHOWNOTIF_POS).equals("Y"));
	    	ckIsActive.setChecked(c.getString(DB.GEN_COL_ISACTIVE_POS).equals("Y"));
		}
		else{
			mRowId = -1;
			ckIsIncludeGPSTrack.setChecked(false);
			ckIsIncludeReports.setChecked(false);
			ckIsShowNotification.setChecked(true);
        	ckIsActive.setChecked(true);
		}
		c.close();
		
}
	@Override
	protected boolean saveData() {

		if(ckIsActive.isChecked()){
	        mPrefEditor = mPreferences.edit();
	        mPrefEditor.putString("smtpAuthUserName", ((EditText)findViewById(R.id.etSMTPUserFrom)).getText().toString());
	        mPrefEditor.putString("smtpAuthPassword", ((EditText)findViewById(R.id.etSMTPUserPassword)).getText().toString());
	        mPrefEditor.putString("bkFileToEmailAddress", ((EditText)findViewById(R.id.etSMTPEmailTo)).getText().toString());
	        mPrefEditor.commit();
		}

        ContentValues cvData = new ContentValues();
        cvData.put( MainDbAdapter.GEN_COL_NAME_NAME, "UserModified");
        cvData.put( MainDbAdapter.GEN_COL_ISACTIVE_NAME, (ckIsActive.isChecked() ? "Y" : "N") );
        cvData.put( MainDbAdapter.GEN_COL_USER_COMMENT_NAME, "Default");
        cvData.put( AddOnDBAdapter.ADDON_SECURE_BK_SETTINGS_COL_ISINCLUDEGPS_NAME, (ckIsIncludeGPSTrack.isChecked() ? "Y" : "N") );
        cvData.put( AddOnDBAdapter.ADDON_SECURE_BK_SETTINGS_COL_ISINCLUDEREPORTS_NAME, (ckIsIncludeReports.isChecked() ? "Y" : "N") );
        cvData.put( AddOnDBAdapter.ADDON_SECURE_BK_SETTINGS_COL_ISSHOWNOTIF_NAME, (ckIsShowNotification.isChecked() ? "Y" : "N") );

        int dbRetVal = -1;
        String strErrMsg = null;
        if (mRowId == -1) {
            mDbAdapter.createRecord(AddOnDBAdapter.ADDON_SECURE_BK_SETTINGS_TABLE_NAME, cvData);
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
        	dbRetVal = mDbAdapter.updateRecord(AddOnDBAdapter.ADDON_SECURE_BK_SETTINGS_TABLE_NAME, mRowId, cvData);
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
   		setContentView(R.layout.addon_secure_bk_config_s01);
	}
	@Override
	public void setSpecificLayout() {
	}
	@Override
	public void setDefaultValues() {
	}

}
