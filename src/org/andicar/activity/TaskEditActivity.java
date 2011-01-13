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

import java.util.Calendar;

import org.andicar.activity.report.ExpensesListReportActivity;
import org.andicar.persistence.MainDbAdapter;
import org.andicar.utils.AndiCarDialogBuilder;
import org.andicar.utils.StaticValues;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

/**
 * 
 * @author miki
 */
public class TaskEditActivity extends EditActivityBase {
	private EditText etName = null;
	private EditText etUserComment = null;
	private EditText etOnDay = null;
	private EditText etFrequency = null;
	private EditText etReminderDays = null;
	private EditText etMileage = null;
	private EditText etReminderMileage = null;

	private CheckBox ckIsActive = null;
	private CheckBox ckIsDifferentStartingTime = null;
	private CheckBox ckOnLastDay = null;

	private Spinner spnTaskType = null;
	private Spinner spnScheduleFrequency = null;
	private Spinner spnDaysOfWeek = null;
	private Spinner spnMonthsOfYear = null;
	private Spinner spnMileageUOM = null;
    private Spinner spnLinkDialogTask;
    private Spinner spnLinkDialogCar;

	private TextView tvMileageLabelEvery = null;
	private TextView tvFirstTimeRunExplanation = null;
	private TextView tvFirstMileageRunExplanation = null;
	private TextView tvOnDay = null;
	private TextView tvReminderMileageCode = null;
	private TextView tvDateTimeValue2 = null;
	private TextView tvOr = null;

	private ImageButton btnNewTaskType = null;
	private ImageButton btnLinkCar = null;

	private RadioButton rbOneTime = null;
	private RadioButton rbRecurent = null;
	private RadioButton rbTimeDriven = null;
	private RadioButton rbMileageDriven = null;
	private RadioButton rbTimeAndMileageDriven = null;
	
	private LinearLayout llTimingZone = null;
	private LinearLayout llOneTimeSettings = null;
	private LinearLayout llRecurentTimeSettings = null;
	private LinearLayout llMoreExact = null;
	private LinearLayout llMileageZone = null;
	
    private View linkView;

    private Calendar mcalDateTime2 = Calendar.getInstance();
	private int mHour2;
	private int mMinute2;
	private long mRecurencyTypeId = -1;
	private boolean isDiffStartingTime = true;
	private boolean isTimingEnabled = true;
	private boolean isMileageEnabled = true;
	private boolean isRecurent = true;
	private String mScheduledFor = StaticValues.TASK_SCHEDULED_FOR_BOTH;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		etName = (EditText) findViewById(R.id.etName);
		etUserComment = (EditText) findViewById(R.id.etUserComment);
		ckIsActive = (CheckBox) findViewById(R.id.ckIsActive);
		spnTaskType = (Spinner) findViewById(R.id.spnTaskType);
		btnNewTaskType = (ImageButton) findViewById(R.id.btnNewTaskType);
		btnNewTaskType.setOnClickListener(onNewTaskTypeClickListener);
		btnLinkCar = (ImageButton) findViewById(R.id.btnLinkCar);
		btnLinkCar.setOnClickListener(onLinkCarClickListener);
		
		rbOneTime = (RadioButton) findViewById(R.id.rbOneTime);
		rbRecurent = (RadioButton) findViewById(R.id.rbRecurent);
		llMoreExact = (LinearLayout) findViewById(R.id.llMoreExact);
		ckIsDifferentStartingTime = (CheckBox) findViewById(R.id.ckIsDifferentStartingTime);
		ckIsDifferentStartingTime.setOnCheckedChangeListener(ckIsDifferentStartingTimeCheckedChange);
		llTimingZone = (LinearLayout) findViewById(R.id.llTimingZone);
		llOneTimeSettings = (LinearLayout) findViewById(R.id.llOneTimeSettings);
		llRecurentTimeSettings = (LinearLayout) findViewById(R.id.llRecurentTimeSettings);
		llMileageZone = (LinearLayout)findViewById(R.id.llMileageZone);
		tvMileageLabelEvery = (TextView)findViewById(R.id.tvMileageLabelEvery);
		tvFirstTimeRunExplanation = (TextView)findViewById(R.id.tvFirstTimeRunExplanation);
		tvFirstMileageRunExplanation = (TextView)findViewById(R.id.tvFirstMileageRunExplanation);
		spnMileageUOM = (Spinner)findViewById(R.id.spnMileageUOM);
		tvReminderMileageCode = (TextView)findViewById(R.id.tvReminderMileageCode);
		
		spnMileageUOM.setOnItemSelectedListener(spnMileageOnItemSelectedListener);
		
		tvReminderMileageCode.setText(mDbAdapter.getUOMCode(mPreferences.getLong("CarUOMLength_ID", 1)));
		
		RadioGroup rg = (RadioGroup) findViewById(R.id.rgRepeating);
		rg.setOnCheckedChangeListener(rgRepeatingOnCheckedChangeListener);

		spnScheduleFrequency = (Spinner) findViewById(R.id.spnScheduleFrequency);
		spnScheduleFrequency.setOnItemSelectedListener(spnScheduleFrequencyOnItemSelectedListener);
		
		spnDaysOfWeek = (Spinner) findViewById(R.id.spnDaysOfWeek);

		tvOnDay = (TextView)findViewById(R.id.tvOnDay);
		etOnDay = (EditText)findViewById(R.id.etOnDay);
		ckOnLastDay = (CheckBox)findViewById(R.id.ckOnLastDay);
		ckOnLastDay.setOnCheckedChangeListener(ckOnLastDayChecked);
		
		RadioGroup rgScheduleType = (RadioGroup) findViewById(R.id.rgScheduleType);
		rgScheduleType.setOnCheckedChangeListener(rgTimingMileageEnabledChecked);
		
		spnMonthsOfYear = (Spinner) findViewById(R.id.spnMonthsOfYear);
		
		etFrequency = (EditText)findViewById(R.id.etFrequency);
		etReminderDays = (EditText)findViewById(R.id.etReminderDays);
		etMileage = (EditText)findViewById(R.id.etMileage);
		etReminderMileage = (EditText)findViewById(R.id.etReminderMileage);

		rbTimeDriven = (RadioButton) findViewById(R.id.rbTimeDriven);
		rbMileageDriven = (RadioButton) findViewById(R.id.rbMileageDriven);
		rbTimeAndMileageDriven = (RadioButton) findViewById(R.id.rbTimeAndMileageDriven);
		tvOr = (TextView)findViewById(R.id.tvOr);
		
		String operation = mBundleExtras.getString("Operation"); // E = edit, N
																	// = new

		if (operation.equals("E")) {
			mRowId = mBundleExtras.getLong(MainDbAdapter.GEN_COL_ROWID_NAME);
			Cursor c = mDbAdapter.fetchRecord(MainDbAdapter.TASK_TABLE_NAME,
					MainDbAdapter.taskTableColNames, mRowId);
			String name = c.getString(MainDbAdapter.GEN_COL_NAME_POS);
			String isActive = c.getString(MainDbAdapter.GEN_COL_ISACTIVE_POS);
			String userComment = c.getString(MainDbAdapter.GEN_COL_USER_COMMENT_POS);
			Long lTaskTypeId = c.getLong(MainDbAdapter.TASK_COL_TASKTYPE_ID_POS);

			if (name != null) {
				etName.setText(name);
			}
			if (isActive != null) {
				ckIsActive.setChecked(isActive.equals("Y"));
			}
			if (userComment != null) {
				etUserComment.setText(userComment);
			}

			initSpinner(spnTaskType, MainDbAdapter.TASKTYPE_TABLE_NAME,
					MainDbAdapter.genColName,
					new String[] { MainDbAdapter.GEN_COL_NAME_NAME },
					MainDbAdapter.isActiveCondition, null,
					MainDbAdapter.GEN_COL_NAME_NAME, lTaskTypeId, false);
			
			isRecurent = c.getString(MainDbAdapter.TASK_COL_ISRECURENT_POS).equals("Y");
			if(isRecurent)
				rbRecurent.setChecked(true);
			else
				rbOneTime.setChecked(true);
			

			mScheduledFor = c.getString(MainDbAdapter.TASK_COL_SCHEDULEDFOR_POS); 
			if(mScheduledFor.equals(StaticValues.TASK_SCHEDULED_FOR_BOTH)){
				isMileageEnabled = true;
				isTimingEnabled = true;
				rbTimeAndMileageDriven.setChecked(true);
			}
			else if(mScheduledFor.equals(StaticValues.TASK_SCHEDULED_FOR_TIME)){
				isMileageEnabled = false;
				isTimingEnabled = true;
				rbTimeDriven.setChecked(true);
			}
			else if(mScheduledFor.equals(StaticValues.TASK_SCHEDULED_FOR_MILEAGE)){
				isMileageEnabled = true;
				isTimingEnabled = false;
				rbMileageDriven.setChecked(true);
			}
			ckIsDifferentStartingTime.setChecked(c.getString(MainDbAdapter.TASK_COL_ISDIFFERENTSTARTINGTIME_POS).equals("Y"));
			isDiffStartingTime = c.getString(MainDbAdapter.TASK_COL_ISDIFFERENTSTARTINGTIME_POS).equals("Y");
			etFrequency.setText(c.getString(MainDbAdapter.TASK_COL_TIMEFREQUENCY_POS));
			mRecurencyTypeId = c.getLong(MainDbAdapter.TASK_COL_TIMEFREQUENCYTYPE_POS);
			spnScheduleFrequency.setSelection( ((Long)mRecurencyTypeId).intValue());
			
			int runDays = ((Long)c.getLong(MainDbAdapter.TASK_COL_RUNDAY_POS)).intValue();
			if(mRecurencyTypeId == StaticValues.TASK_SCHEDULED_FREQTYPE_WEEK)
				spnDaysOfWeek.setSelection(runDays);
			else{
				if(mRecurencyTypeId == StaticValues.TASK_SCHEDULED_FREQTYPE_MONTH 
						&& runDays == 32) //last day
					ckOnLastDay.setChecked(true);
				else
					etOnDay.setText(((Integer)runDays).toString());
			}
				
			if(mRecurencyTypeId == StaticValues.TASK_SCHEDULED_FREQTYPE_YEAR)
				spnMonthsOfYear.setSelection(((Long)c.getLong(MainDbAdapter.TASK_COL_RUNMONTH_POS)).intValue());
			
			Long runTime = c.getLong(MainDbAdapter.TASK_COL_RUNTIME_POS) * 1000;
			if(!isRecurent){
	            initDateTime(runTime);
	            initTime2(0);
//	        	mcalDateTime.setTimeInMillis(mlDateTimeInSeconds * 1000);
//	            tvDateTimeValue.setText(
//	            		DateFormat.getDateFormat(getApplicationContext()).format(mcalDateTime.getTime()) + " " +
//	    					DateFormat.getTimeFormat(getApplicationContext()).format(mcalDateTime.getTime()));
			}
			else{
				initDateOnly = true;
	            initDateTime(System.currentTimeMillis());
				initDateOnly = false;
				initTime2(runTime);
			}
			
			if(c.getString(MainDbAdapter.TASK_COL_REMINDERDAYS_POS) != null)
				etReminderDays.setText(c.getString(MainDbAdapter.TASK_COL_REMINDERDAYS_POS));
			
			if(c.getString(MainDbAdapter.TASK_COL_RUNMILEAGE_POS) != null)
				etMileage.setText(c.getString(MainDbAdapter.TASK_COL_RUNMILEAGE_POS));

			initSpinner(spnMileageUOM, MainDbAdapter.UOM_TABLE_NAME,
	                MainDbAdapter.genColName, new String[]{MainDbAdapter.GEN_COL_NAME_NAME},
	                MainDbAdapter.UOM_COL_UOMTYPE_NAME + "='" + StaticValues.UOM_LENGTH_TYPE_CODE + "'" +
	                    MainDbAdapter.isActiveWithAndCondition, null, MainDbAdapter.GEN_COL_NAME_NAME, 
	                    c.getLong(MainDbAdapter.TASK_COL_MILEAGEUOM_ID_POS), false);
			etReminderMileage.setText(c.getString(MainDbAdapter.TASK_COL_REMINDERMILEAGES_POS));

			c.close();
		} else { // new
			ckIsActive.setChecked(true);
			initSpinner(spnTaskType, MainDbAdapter.TASKTYPE_TABLE_NAME,
					MainDbAdapter.genColName,
					new String[] { MainDbAdapter.GEN_COL_NAME_NAME },
					MainDbAdapter.isActiveCondition, null,
					MainDbAdapter.GEN_COL_NAME_NAME, 0, false);
			initSpinner(spnMileageUOM, MainDbAdapter.UOM_TABLE_NAME,
	                MainDbAdapter.genColName, new String[]{MainDbAdapter.GEN_COL_NAME_NAME},
	                MainDbAdapter.UOM_COL_UOMTYPE_NAME + "='" + StaticValues.UOM_LENGTH_TYPE_CODE + "'" +
	                    MainDbAdapter.isActiveWithAndCondition, null, MainDbAdapter.GEN_COL_NAME_NAME, 
	                    mPreferences.getLong("CarUOMLength_ID", 1), false);
			rbRecurent.setChecked(true);
			llOneTimeSettings.setVisibility(View.GONE);
			llRecurentTimeSettings.setVisibility(View.VISIBLE);
			initDateOnly = true;
			initDateTime(System.currentTimeMillis());
			initDateOnly = false;
			initTime2(0);
		}
		setSpecificLayout();
	}

	@Override
	protected void saveData() {
		String strRetVal = checkMandatory(vgRoot);
		if (strRetVal != null) {
			Toast toast = Toast.makeText(getApplicationContext(),
					mResource.getString(R.string.GEN_FillMandatory) + ": "
							+ strRetVal, Toast.LENGTH_SHORT);
			toast.show();
			return;
		}

		strRetVal = checkNumeric(vgRoot, false);
		if (strRetVal != null) {
			Toast toast = Toast.makeText(getApplicationContext(),
					mResource.getString(R.string.GEN_NumberFormatException)
							+ ": " + strRetVal, Toast.LENGTH_SHORT);
			toast.show();
			return;
		}

		ContentValues data = new ContentValues();
		data.put(MainDbAdapter.GEN_COL_NAME_NAME, etName.getText().toString());
		data.put(MainDbAdapter.GEN_COL_ISACTIVE_NAME,
				(ckIsActive.isChecked() ? "Y" : "N"));
		data.put(MainDbAdapter.GEN_COL_USER_COMMENT_NAME, etUserComment
				.getText().toString());

		data.put(MainDbAdapter.TASK_COL_TASKTYPE_ID_NAME, spnTaskType.getSelectedItemId());
		
		data.put(MainDbAdapter.TASK_COL_ISRECURENT_NAME, (isRecurent ? "Y" : "N"));
		data.put(MainDbAdapter.TASK_COL_SCHEDULEDFOR_NAME, mScheduledFor);
		data.put(MainDbAdapter.TASK_COL_ISDIFFERENTSTARTINGTIME_NAME, (isDiffStartingTime ? "Y" : "N"));
		data.put(MainDbAdapter.TASK_COL_TIMEFREQUENCY_NAME, etFrequency.getText().toString());
		data.put(MainDbAdapter.TASK_COL_TIMEFREQUENCYTYPE_NAME, mRecurencyTypeId);
		
		if(mRecurencyTypeId == StaticValues.TASK_SCHEDULED_FREQTYPE_WEEK) //week
			data.put(MainDbAdapter.TASK_COL_RUNDAY_NAME, spnDaysOfWeek.getSelectedItemId());
		else{
			if(mRecurencyTypeId == StaticValues.TASK_SCHEDULED_FREQTYPE_MONTH
					&& ckOnLastDay.isChecked())
				data.put(MainDbAdapter.TASK_COL_RUNDAY_NAME, 32);
			else
				data.put(MainDbAdapter.TASK_COL_RUNDAY_NAME, etOnDay.getText().toString());
		}
		
		if(mRecurencyTypeId == StaticValues.TASK_SCHEDULED_FREQTYPE_YEAR)
			data.put(MainDbAdapter.TASK_COL_RUNMONTH_NAME, spnMonthsOfYear.getSelectedItemId());
		else
			data.put(MainDbAdapter.TASK_COL_RUNMONTH_NAME, (String)null);
		
		if(rbOneTime.isChecked())
			data.put(MainDbAdapter.TASK_COL_RUNTIME_NAME, mlDateTimeInSeconds);
		else
			data.put(MainDbAdapter.TASK_COL_RUNTIME_NAME, mcalDateTime2.getTimeInMillis() / 1000);
		
		data.put(MainDbAdapter.TASK_COL_REMINDERDAYS_NAME, etReminderDays.getText().toString());
		data.put(MainDbAdapter.TASK_COL_RUNMILEAGE_NAME, etMileage.getText().toString());
		data.put(MainDbAdapter.TASK_COL_MILEAGEUOM_ID_NAME, spnMileageUOM.getSelectedItemId());
		data.put(MainDbAdapter.TASK_COL_REMINDERMILEAGES_NAME, etReminderMileage.getText().toString());
		
		if (mRowId == -1) {
			mDbAdapter.createRecord(MainDbAdapter.TASK_TABLE_NAME, data);
			finish();
		} else {
			int updResult = mDbAdapter.updateRecord(
					MainDbAdapter.TASK_TABLE_NAME, mRowId, data);
			if (updResult != -1) {
				String errMsg = "";
				errMsg = mResource.getString(updResult);
				if (updResult == R.string.ERR_000)
					errMsg = errMsg + "\n" + mDbAdapter.lastErrorMessage;
				madbErrorAlert.setMessage(errMsg);
				madError = madbErrorAlert.create();
				madError.show();
			} else
				finish();
		}
	}

	@Override
	protected void setLayout() {
		setContentView(R.layout.task_edit_activity);
	}

	protected View.OnClickListener onNewTaskTypeClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			Intent i = new Intent(TaskEditActivity.this,
					TaskTypeEditActivity.class);
			i.putExtra("Operation", "N");
			startActivityForResult(i, 0);
		}
	};

	protected View.OnClickListener onLinkCarClickListener = new View.OnClickListener() {
		public void onClick(View v) {
            showDialog(StaticValues.DIALOG_TASK_CAR_LINK);
		}
	};

//    @Override
//    protected Dialog onCreateDialog(int id) {
//
//    }

    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		long newId = 0;
		if (data != null)
			newId = data.getLongExtra("mRowId", 0);

		initSpinner(spnTaskType, MainDbAdapter.TASKTYPE_TABLE_NAME,
				MainDbAdapter.genColName,
				new String[] { MainDbAdapter.GEN_COL_NAME_NAME },
				MainDbAdapter.isActiveCondition, null,
				MainDbAdapter.GEN_COL_NAME_NAME, newId, false);
	}

	private RadioGroup.OnCheckedChangeListener rgRepeatingOnCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
		public void onCheckedChanged(RadioGroup arg0, int checkedId) {
			if (checkedId == rbOneTime.getId()) {
				isRecurent = false;
			} else { //
				isRecurent = true;
			}
			setSpecificLayout();
		}
	};
	
	private RadioGroup.OnCheckedChangeListener rgTimingMileageEnabledChecked = new RadioGroup.OnCheckedChangeListener() {
		public void onCheckedChanged(RadioGroup arg0, int checkedId) {
			if (checkedId == R.id.rbTimeDriven) {
				isTimingEnabled = true;
				isMileageEnabled = false;
				mScheduledFor = StaticValues.TASK_SCHEDULED_FOR_TIME;
			} 
			else if (checkedId == R.id.rbMileageDriven) {
				isTimingEnabled = false;
				isMileageEnabled = true;
				mScheduledFor = StaticValues.TASK_SCHEDULED_FOR_MILEAGE;
			}
			else if (checkedId == R.id.rbTimeAndMileageDriven) {
				isTimingEnabled = true;
				isMileageEnabled = true;
				mScheduledFor = StaticValues.TASK_SCHEDULED_FOR_BOTH;
			}
			setSpecificLayout();
		}
	};

	protected void initTime2(long dateTimeInMiliseconds) {
		mcalDateTime2.setTimeInMillis(dateTimeInMiliseconds);
		if(dateTimeInMiliseconds != 0){
			mHour2 = mcalDateTime2.get(Calendar.HOUR_OF_DAY);
			mMinute2 = mcalDateTime2.get(Calendar.MINUTE);
		}
		else{
			//avoid time zone influence
			mHour2 = 0;
			mMinute2 = 0;
			mcalDateTime2.set(1970, Calendar.JANUARY, 1);
			mcalDateTime2.set(Calendar.HOUR_OF_DAY, 0);
			mcalDateTime2.set(Calendar.MINUTE, 0);
		}
			
		tvDateTimeValue2 = (TextView) findViewById(R.id.tvDateTimeValue2);
		updateTime2();

		ImageButton btnPickTime2 = (ImageButton) findViewById(R.id.btnPickTime2);
		if (btnPickTime2 != null)
			btnPickTime2.setOnClickListener(new View.OnClickListener() {
				public void onClick(View arg0) {
					showDialog(StaticValues.DIALOG_TIME_PICKER2);
				}
			});
	}

	private TimePickerDialog.OnTimeSetListener onTime2SetListener = new TimePickerDialog.OnTimeSetListener() {
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			mHour2 = hourOfDay;
			mMinute2 = minute;
			updateTime2();
		}
	};

	private void updateTime2() {
//		mcalDateTime2.set(2000, Calendar.JANUARY, 1, mHour2, mMinute2, 0);
		mcalDateTime2.set(Calendar.HOUR_OF_DAY, mHour2);
		mcalDateTime2.set(Calendar.MINUTE, mMinute2);
		mcalDateTime2.set(Calendar.SECOND, 0);
		// mlDateTimeInSeconds2 = mcalDateTime2.getTimeInMillis() / 1000;
		tvDateTimeValue2.setText(DateFormat.getTimeFormat(
				getApplicationContext()).format(mcalDateTime2.getTime()));
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		if(id < 3)
			return super.onCreateDialog(id);
		else if(id == StaticValues.DIALOG_TIME_PICKER2)
			return new TimePickerDialog(this, onTime2SetListener, mHour2, mMinute2, false);
		//date part
		else if(id == StaticValues.DIALOG_DATE_FROM_PICKER){
	        return new DatePickerDialog(this,
	                null /*onDateFromSetListener*/,
	                2011 /*mYearFrom*/, 0 /*mMonthFrom*/, 12/*mDayFrom*/);
		}
		//time part
		else if(id == StaticValues.DIALOG_DATE_TO_PICKER){
	        return new TimePickerDialog(this,
	                null /*onDateFromSetListener*/,
	                11 /*mHour*/, 0 /*mMinute*/, false);
		}
		else if(id == StaticValues.DIALOG_TASK_CAR_LINK){
	        LayoutInflater liLayoutFactory = LayoutInflater.from(this);
	        linkView = liLayoutFactory.inflate(R.layout.task_car_link_dialog, null);
	        AndiCarDialogBuilder linkDialog = new AndiCarDialogBuilder(TaskEditActivity.this, 
	        		AndiCarDialogBuilder.DIALOGTYPE_CAR, mResource.getString(R.string.TaskCarDialogActivity_Title));
	        linkDialog.setView(linkView);
	        linkDialog.setPositiveButton(R.string.GEN_OK, linkDialogButtonlistener);
	        linkDialog.setNegativeButton(R.string.GEN_CANCEL, linkDialogButtonlistener);
	        spnLinkDialogCar = (Spinner) linkView.findViewById(R.id.spnCar);
			initSpinner(spnLinkDialogCar, MainDbAdapter.CAR_TABLE_NAME,
					MainDbAdapter.genColName,
					new String[] { MainDbAdapter.GEN_COL_NAME_NAME },
					MainDbAdapter.isActiveCondition, null,
					MainDbAdapter.GEN_COL_NAME_NAME, 1, false);

			spnLinkDialogTask = (Spinner) linkView.findViewById(R.id.spnTask);
			initSpinner(spnLinkDialogTask, MainDbAdapter.TASK_TABLE_NAME,
					MainDbAdapter.genColName,
					new String[] { MainDbAdapter.GEN_COL_NAME_NAME },
					MainDbAdapter.isActiveCondition, null,
					MainDbAdapter.GEN_COL_NAME_NAME, 1, false);

	        ImageButton btnPickDate = (ImageButton) linkView.findViewById(R.id.btnPickDate);
	        if(btnPickDate != null)
	        	btnPickDate.setOnClickListener(new View.OnClickListener() {
	                public void onClick(View arg0) {
	                    showDialog(StaticValues.DIALOG_DATE_FROM_PICKER);
	                }
	            });
	        
	        ImageButton btnPickTime = (ImageButton) linkView.findViewById(R.id.btnPickTime);
	        if(btnPickTime != null)
	            btnPickTime.setOnClickListener(new View.OnClickListener() {
	                public void onClick(View arg0) {
	                    showDialog(StaticValues.DIALOG_DATE_TO_PICKER);
	                }
	            });
	        return linkDialog.create();
		}
		return null;
	}

	
    private DialogInterface.OnClickListener linkDialogButtonlistener = new DialogInterface.OnClickListener() {

        public void onClick(DialogInterface dialog, int whichButton) {
            if (whichButton == DialogInterface.BUTTON_POSITIVE) {
            	
            }
        }
    };
	
	protected AdapterView.OnItemSelectedListener spnScheduleFrequencyOnItemSelectedListener = new AdapterView.OnItemSelectedListener() {
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {

//			if (isActivityOnLoading)
//				return;
			mRecurencyTypeId = arg3;
			setSpecificLayout();
		}

		public void onNothingSelected(AdapterView<?> arg0) {
		}
	};
	
	protected AdapterView.OnItemSelectedListener spnMileageOnItemSelectedListener = new AdapterView.OnItemSelectedListener() {
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			tvReminderMileageCode.setText(mDbAdapter.getUOMCode(arg3));
		}

		public void onNothingSelected(AdapterView<?> arg0) {
		}
	};

	protected CheckBox.OnCheckedChangeListener ckOnLastDayChecked = new CheckBox.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if(mRecurencyTypeId != 1)
				return; //invalid
			else
				setSpecificLayout();
		}
	};
	
	
	protected CheckBox.OnCheckedChangeListener ckIsDifferentStartingTimeCheckedChange = new CheckBox.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			isDiffStartingTime = isChecked;
			setSpecificLayout();
		}
	};
	
	private void setSpecificLayout(){

		if (isRecurent) {
			llOneTimeSettings.setVisibility(View.GONE);
			llRecurentTimeSettings.setVisibility(View.VISIBLE);
			ckIsDifferentStartingTime.setVisibility(View.VISIBLE);
			tvMileageLabelEvery.setVisibility(View.VISIBLE);
			tvFirstMileageRunExplanation.setVisibility(View.VISIBLE);
		} else { //
			llOneTimeSettings.setVisibility(View.VISIBLE);
			llRecurentTimeSettings.setVisibility(View.GONE);
			ckIsDifferentStartingTime.setVisibility(View.GONE);
			tvMileageLabelEvery.setVisibility(View.GONE);
			tvFirstMileageRunExplanation.setVisibility(View.GONE);
		}
		
		if(isTimingEnabled && isMileageEnabled)
			tvOr.setVisibility(View.VISIBLE);
		else
			tvOr.setVisibility(View.GONE);

		if(isTimingEnabled){
			
			llTimingZone.setVisibility(View.VISIBLE);

			if(mRecurencyTypeId == 0){ //wekly
				spnDaysOfWeek.setVisibility(View.VISIBLE);
				tvOnDay.setVisibility(View.GONE);
				etOnDay.setVisibility(View.GONE);
				ckOnLastDay.setVisibility(View.GONE);
				spnMonthsOfYear.setVisibility(View.GONE);
			}
			else if(mRecurencyTypeId == 1){ //monthly
				spnDaysOfWeek.setVisibility(View.GONE);
				spnMonthsOfYear.setVisibility(View.GONE);
				if(ckOnLastDay.isChecked()){
					tvOnDay.setVisibility(View.GONE);
					etOnDay.setVisibility(View.GONE);
				}
				else{
					tvOnDay.setVisibility(View.VISIBLE);
					etOnDay.setVisibility(View.VISIBLE);
				}
				ckOnLastDay.setVisibility(View.VISIBLE);
			}
			else{ //yearly
				spnDaysOfWeek.setVisibility(View.GONE);
				ckOnLastDay.setChecked(false);
				ckOnLastDay.setVisibility(View.GONE);
				spnMonthsOfYear.setVisibility(View.VISIBLE);
				tvOnDay.setVisibility(View.VISIBLE);
				etOnDay.setVisibility(View.VISIBLE);
			}
			if(isDiffStartingTime){
				tvFirstTimeRunExplanation.setVisibility(View.VISIBLE);
				llMoreExact.setVisibility(View.GONE);
			}
			else{
				llMoreExact.setVisibility(View.VISIBLE);
				tvFirstTimeRunExplanation.setVisibility(View.GONE);
			}
		}
		else{
			llTimingZone.setVisibility(View.GONE);
		}
		
		if(isMileageEnabled){
			llMileageZone.setVisibility(View.VISIBLE);
		}
		else{
			llMileageZone.setVisibility(View.GONE);
		}
		
	}
}
