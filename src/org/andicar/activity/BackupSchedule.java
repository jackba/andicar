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

/**
 * 
 */
package org.andicar.activity;

import org.andicar.activity.R;
import org.andicar.persistence.AddOnDBAdapter;
import org.andicar.persistence.DB;
import org.andicar.persistence.MainDbAdapter;
import org.andicar.service.BackupService;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.Toast;


/**
 * @author miki
 *
 */
public class BackupSchedule extends EditActivityBase {

    private Spinner spnScheduleFrequency = null;
//    private LinearLayout llDayList = null;
    private TableLayout tlDayList = null;
    private CheckBox ckIsActive = null;
    private CheckBox ckNotifyIfSuccess = null;
    private EditText etKeepLastNo = null;
    
    int[] ckDayOfWeekIDs = {R.id.ckDayOfWeek0, R.id.ckDayOfWeek1, R.id.ckDayOfWeek2, R.id.ckDayOfWeek3,
    							R.id.ckDayOfWeek4, R.id.ckDayOfWeek5, R.id.ckDayOfWeek6};
    private String activeDaysBitmap = "";

	/* (non-Javadoc)
	 * @see org.andicar.activity.EditActivityBase#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
        initTimeOnly = true;
		
		ckIsActive = (CheckBox)findViewById(R.id.ckIsActive);
		ckNotifyIfSuccess = (CheckBox)findViewById(R.id.ckNotifyIfSuccess);
//		llDayList = (LinearLayout)findViewById(R.id.llDayList);
		tlDayList = (TableLayout)findViewById(R.id.tlDayList);
        spnScheduleFrequency = (Spinner)findViewById(R.id.spnScheduleFrequency);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
            this, R.array.ao_bk_schedule_type_entries, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnScheduleFrequency.setAdapter(adapter);
        spnScheduleFrequency.setOnItemSelectedListener(spinnerScheduleFrequencyOnItemSelectedListener);
        etKeepLastNo = (EditText)findViewById(R.id.etKeepLastNo);

        Cursor c = mDbAdapter.query(AddOnDBAdapter.ADDON_BK_SCHEDULE_TABLE_NAME, AddOnDBAdapter.addonBKScheduleTableColNames, 
				null, null, null, null, null);
		
        long initTime = 0;
        
        //just one record should be in this table!!!
		if(c.moveToFirst()){
			mRowId = c.getLong(DB.GEN_COL_ROWID_POS);
	    	initTime = c.getLong(DB.GEN_COL_NAME_POS);
	    	ckIsActive.setChecked(c.getString(DB.GEN_COL_ISACTIVE_POS).equals("Y"));
	        if(c.getString(AddOnDBAdapter.ADDON_BK_SCHEDULE_COL_FREQUENCY_POS).equals("D")) //daily frequency
	        	spnScheduleFrequency.setSelection(0);
	    	else
	        	spnScheduleFrequency.setSelection(1);
	        activeDaysBitmap = c.getString(AddOnDBAdapter.ADDON_BK_SCHEDULE_COL_DAYS_POS);
	        etKeepLastNo.setText(c.getString(DB.GEN_COL_USER_COMMENT_POS));
	        ckNotifyIfSuccess.setChecked(mPreferences.getBoolean("AddOn_AutoBackupService_NotifyIfSuccess", true));
		}
		else{
			mRowId = -1;
        	initTime = 946753200778L; //just for time part (01-01-2000 21:00) 
        	ckIsActive.setChecked(true);
        	spnScheduleFrequency.setSelection(0);
        	activeDaysBitmap = "1111111";
        	etKeepLastNo.setText("10");
	        ckNotifyIfSuccess.setChecked(true);
		}
		c.close();
		
		for (int i = 0; i < 7; i++) {
			((CheckBox)findViewById(ckDayOfWeekIDs[i])).setChecked(activeDaysBitmap.charAt(i) == '1');
		}
        
        initDateTime(initTime);
	}

	
	/* (non-Javadoc)
	 * @see org.andicar.activity.EditActivityBase#onStop()
	 */
	@Override
	protected void onStop() {
		super.onStop();
        //start the service to update the next run
		Intent intent = new Intent(this, BackupService.class);
		intent.putExtra("Operation", "SetNextRun");
		PendingIntent pIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		AlarmManager am = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
		am.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), AlarmManager.INTERVAL_DAY, pIntent);
	}


	private AdapterView.OnItemSelectedListener spinnerScheduleFrequencyOnItemSelectedListener =
        new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            	 if(spnScheduleFrequency.getSelectedItemPosition() == 0){
            		 tlDayList.setVisibility(View.GONE);
            	 }
            	 else{
            		 tlDayList.setVisibility(View.VISIBLE);
            	 }
            }
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        };
        
    /* (non-Javadoc)
	 * @see org.andicar.activity.EditActivityBase#saveData()
	 */
	@Override
	protected boolean saveData() {
		
        String strRetVal = checkMandatory(vgRoot);
        if( strRetVal != null ) {
            Toast toast = Toast.makeText( getApplicationContext(),
                    mResource.getString( R.string.GEN_FillMandatory ) + ": " + strRetVal, Toast.LENGTH_SHORT );
            toast.show();
            return false;
        }

        strRetVal = checkNumeric(vgRoot, true);
        if( strRetVal != null ) {
            Toast toast = Toast.makeText( getApplicationContext(),
                    mResource.getString( R.string.GEN_NumberFormatException ) + ": " + strRetVal, Toast.LENGTH_SHORT );
            toast.show();
            return false;
        }

        ContentValues cvData = new ContentValues();
        cvData.put( MainDbAdapter.GEN_COL_NAME_NAME, mcalDateTime.getTimeInMillis());
        cvData.put( MainDbAdapter.GEN_COL_ISACTIVE_NAME, (ckIsActive.isChecked() ? "Y" : "N") );
        
        activeDaysBitmap = "";
		for (int i = 0; i < 7; i++) {
			if(((CheckBox)findViewById(ckDayOfWeekIDs[i])).isChecked())
				activeDaysBitmap = activeDaysBitmap + "1";
			else
				activeDaysBitmap = activeDaysBitmap + "0";
		}
		if(spnScheduleFrequency.getSelectedItemPosition() == 1 //weekly 
				&& activeDaysBitmap.equals("0000000")){ //but no days selected
            Toast toast = Toast.makeText( getApplicationContext(),
                    mResource.getString( R.string.AddOn_AutoBackupService_NoDaySelectedMsg ), Toast.LENGTH_SHORT );
            toast.show();
            return false;
		}
        cvData.put( AddOnDBAdapter.ADDON_BK_SCHEDULE_COL_DAYS_NAME, activeDaysBitmap);

        if(spnScheduleFrequency.getSelectedItemPosition() == 0)
            cvData.put( AddOnDBAdapter.ADDON_BK_SCHEDULE_COL_FREQUENCY_NAME, "D");
        else
            cvData.put( AddOnDBAdapter.ADDON_BK_SCHEDULE_COL_FREQUENCY_NAME, "W");

        cvData.put( DB.GEN_COL_USER_COMMENT_NAME, etKeepLastNo.getText().toString());

        int dbRetVal = -1;
        String strErrMsg = null;
        if( mRowId == -1 ) {
        	dbRetVal = ((Long)mDbAdapter.createRecord(AddOnDBAdapter.ADDON_BK_SCHEDULE_TABLE_NAME, cvData)).intValue();
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
        }
        else {
        	dbRetVal = mDbAdapter.updateRecord(AddOnDBAdapter.ADDON_BK_SCHEDULE_TABLE_NAME, mRowId, cvData);
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
            	mPrefEditor.putBoolean("AddOn_AutoBackupService_NotifyIfSuccess", ckNotifyIfSuccess.isChecked());
            	mPrefEditor.commit();
            }
        }
		finish();
		return true;
	}

	/* (non-Javadoc)
	 * @see org.andicar.activity.EditActivityBase#setLayout()
	 */
	@Override
	protected void setLayout() {
    	if(mPreferences.getString("UIStyle", "s01").equalsIgnoreCase("s00"))
    		setContentView(R.layout.addon_backup_schedule_s00);
    	else if(mPreferences.getString("UIStyle", "s01").equalsIgnoreCase("s01"))
    		setContentView(R.layout.addon_backup_schedule_s01);
	}


	@Override
	public void setSpecificLayout() {
	}


	@Override
	public void setDefaultValues() {
	}

}
