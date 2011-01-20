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
import java.util.TimeZone;

import org.andicar.persistence.DB;
import org.andicar.persistence.MainDbAdapter;
import org.andicar.persistence.TaskCarLinkDataBinder;
import org.andicar.service.TodoManagementService;
import org.andicar.utils.AndiCarDialogBuilder;
import org.andicar.utils.StaticValues;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleCursorAdapter;
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
	private EditText etLinkedCarIndexStart = null;

	private CheckBox ckIsActive = null;
	private CheckBox ckIsDifferentStartingTime = null;
	private CheckBox ckOnLastDay = null;

	private Spinner spnTaskType = null;
	private Spinner spnScheduleFrequency = null;
	private Spinner spnDaysOfWeek = null;
	private Spinner spnMonthsOfYear = null;
    private Spinner spnLinkDialogCar;

	private TextView tvMileageLabelEvery = null;
	private TextView tvFirstTimeRunExplanation = null;
	private TextView tvFirstMileageRunExplanation = null;
	private TextView tvOnDay = null;
	private TextView tvDateTimeValue2 = null;
	private TextView tvOr = null;
	private TextView tvLinkCarDialogFirstRunDate = null;

	private ImageButton btnNewTaskType = null;
	private ImageButton btnLinkCar = null;

	private RadioGroup rgRepeating = null;
	private RadioGroup rgScheduleType = null;
	
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
	private LinearLayout llLinkedCarsZone = null;
	private LinearLayout llLinkedCarsHelp = null;
	private LinearLayout llLinkedCarsList = null;
	
	//used in link dialog
	private LinearLayout llDialogStartingDateZone = null;
	private LinearLayout llDialogStartingMileageZone = null;

    private ListView lvLinkedCarsList = null;
	
    private View linkView;

    private Calendar mcalDateTime2 = Calendar.getInstance();
	private Calendar mLinkDialogStartingDateTimeCal;

	private int mHour2;
	private int mMinute2;
	private int mLinkDialogStartingYear;
	private int mLinkDialogStartingMonth;
	private int mLinkDialogStartingDay;
	private int mLinkDialogStartingHour;
	private int mLinkDialogStartingMinute;

	private long mRecurencyTypeId = -1;
	private long mlStartingDateTimeInSeconds;
	private long mLongClickId;
	private boolean isDiffStartingTime = true;
	private boolean isTimingEnabled = true;
	private boolean isMileageEnabled = true;
	private boolean isRecurent = true;
	private boolean isFinishAfterSave = true;
	private boolean saveSuccess = true;
	private boolean isDeleteLinkedCarsOnSave = false;
	private String mScheduledFor = StaticValues.TASK_SCHEDULED_FOR_BOTH;
	private String mLinkDialogCarSelectCondition = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		String operation = mBundleExtras.getString("Operation"); // E = edit, N = new
		//mcalDateTime2 is used only for the time part => should not be influenced by the time zone 
		mcalDateTime2.setTimeZone(TimeZone.getTimeZone("UTC"));

		init();

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
				spnDaysOfWeek.setSelection(runDays - 1);
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

			etReminderMileage.setText(c.getString(MainDbAdapter.TASK_COL_REMINDERMILEAGES_POS));

			c.close();
		} else { // new
			ckIsActive.setChecked(true);
			initSpinner(spnTaskType, MainDbAdapter.TASKTYPE_TABLE_NAME,
					MainDbAdapter.genColName,
					new String[] { MainDbAdapter.GEN_COL_NAME_NAME },
					MainDbAdapter.isActiveCondition, null,
					MainDbAdapter.GEN_COL_NAME_NAME, 0, false);
			rbRecurent.setChecked(true);
			llOneTimeSettings.setVisibility(View.GONE);
			llRecurentTimeSettings.setVisibility(View.VISIBLE);
			initDateOnly = true;
			initDateTime(System.currentTimeMillis());
			initDateOnly = false;
			initTime2(0);
		}

		setSpecificLayout();

		initControls();
		fillLinkedCarsData();	
	}

	private void init() {
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
		
		llLinkedCarsZone = (LinearLayout)findViewById(R.id.llLinkedCarsZone);
		llLinkedCarsHelp = (LinearLayout)findViewById(R.id.llLinkedCarsHelp);

		rgRepeating = (RadioGroup) findViewById(R.id.rgRepeating);

		spnScheduleFrequency = (Spinner) findViewById(R.id.spnScheduleFrequency);
		spnScheduleFrequency.setOnItemSelectedListener(spnScheduleFrequencyOnItemSelectedListener);
		
		spnDaysOfWeek = (Spinner) findViewById(R.id.spnDaysOfWeek);
		spnMonthsOfYear = (Spinner) findViewById(R.id.spnMonthsOfYear);

		tvOnDay = (TextView)findViewById(R.id.tvOnDay);
		etOnDay = (EditText)findViewById(R.id.etOnDay);
		ckOnLastDay = (CheckBox)findViewById(R.id.ckOnLastDay);

		rgScheduleType = (RadioGroup) findViewById(R.id.rgScheduleType);
		etFrequency = (EditText)findViewById(R.id.etFrequency);
		etReminderDays = (EditText)findViewById(R.id.etReminderDays);
		etMileage = (EditText)findViewById(R.id.etMileage);
		etReminderMileage = (EditText)findViewById(R.id.etReminderMileage);

		rbTimeDriven = (RadioButton) findViewById(R.id.rbTimeDriven);
		rbMileageDriven = (RadioButton) findViewById(R.id.rbMileageDriven);
		rbTimeAndMileageDriven = (RadioButton) findViewById(R.id.rbTimeAndMileageDriven);
		tvOr = (TextView)findViewById(R.id.tvOr);
		
		llLinkedCarsList = (LinearLayout)findViewById(R.id.llLinkedCarsList);
		lvLinkedCarsList = (ListView)findViewById(R.id.lvLinkedCarsList);
	}

	private void initControls() {
		lvLinkedCarsList.setOnItemClickListener(mLinkedCarListItemClickListener);
		lvLinkedCarsList.setOnItemLongClickListener(mItemLongClickListener);
		lvLinkedCarsList.setOnCreateContextMenuListener(this);
		rgRepeating.setOnCheckedChangeListener(rgRepeatingOnCheckedChangeListener);
		ckOnLastDay.setOnCheckedChangeListener(ckOnLastDayChecked);
		rgScheduleType.setOnCheckedChangeListener(rgTimingMileageEnabledChecked);
	}

    private void fillLinkedCarsData(){
//        String selection = MainDbAdapter.TASK_CAR_COL_TASK_ID_NAME + "=?";
    	String firstRun = ""; 
    	if(isRecurent){
			if(isTimingEnabled && isDiffStartingTime){
				firstRun = "'" + mResource.getString(R.string.TaskCarEditActivity_StartDate) + " ' || " +
						" '[#1]'";
			}
			if(isMileageEnabled){
				if(firstRun.length() > 0)
					firstRun = firstRun + " || '; " + mResource.getString(R.string.TaskCarEditActivity_StartMileage) + " ' || " + 
						DB.sqlConcatTableColumn(MainDbAdapter.TASK_CAR_TABLE_NAME, MainDbAdapter.TASK_CAR_COL_FIRSTRUN_MILEAGE_NAME) + 
							" || ' ' || " + 
						DB.sqlConcatTableColumn(MainDbAdapter.UOM_TABLE_NAME, MainDbAdapter.UOM_COL_CODE_NAME);
				else
					firstRun = "'" + mResource.getString(R.string.TaskCarEditActivity_StartMileage) + " ' || " + 
						DB.sqlConcatTableColumn(MainDbAdapter.TASK_CAR_TABLE_NAME, MainDbAdapter.TASK_CAR_COL_FIRSTRUN_MILEAGE_NAME) + 
							" || ' ' || " + 
						DB.sqlConcatTableColumn(MainDbAdapter.UOM_TABLE_NAME, MainDbAdapter.UOM_COL_CODE_NAME);
			}
    	}
    	else{
			 firstRun = "''";
    	}
		
		if(firstRun.length() == 0)
			return;
		
		firstRun = firstRun + " AS FirstRun, ";
    	
        String[] selectionArgs = {Long.toString(mRowId)};
    	String selectSql = 
    		"SELECT " + 
				DB.sqlConcatTableColumn(MainDbAdapter.TASK_CAR_TABLE_NAME, MainDbAdapter.GEN_COL_ROWID_NAME) + ", " + //#0
				DB.sqlConcatTableColumn(MainDbAdapter.CAR_TABLE_NAME, MainDbAdapter.GEN_COL_NAME_NAME) + ", " + //#1
				firstRun +
				DB.sqlConcatTableColumn(MainDbAdapter.TASK_CAR_TABLE_NAME, MainDbAdapter.TASK_CAR_COL_FIRSTRUN_DATE_NAME) + " " + //#3
			" FROM " + MainDbAdapter.TASK_CAR_TABLE_NAME + " " +
				" JOIN " + MainDbAdapter.CAR_TABLE_NAME + " ON " + 
					DB.sqlConcatTableColumn(MainDbAdapter.TASK_CAR_TABLE_NAME, MainDbAdapter.TASK_CAR_COL_CAR_ID_NAME) + " = " +
					DB.sqlConcatTableColumn(MainDbAdapter.CAR_TABLE_NAME, MainDbAdapter.GEN_COL_ROWID_NAME) + 
					" JOIN " + MainDbAdapter.UOM_TABLE_NAME + " ON " + 
                		DB.sqlConcatTableColumn(MainDbAdapter.CAR_TABLE_NAME, MainDbAdapter.CAR_COL_UOMLENGTH_ID_NAME) + "=" +
                                        	DB.sqlConcatTableColumn(MainDbAdapter.UOM_TABLE_NAME, MainDbAdapter.GEN_COL_ROWID_NAME) +
			" WHERE " + 
				DB.sqlConcatTableColumn(MainDbAdapter.TASK_CAR_TABLE_NAME, MainDbAdapter.TASK_CAR_COL_TASK_ID_NAME) + " = ? " +
				" AND " + DB.sqlConcatTableColumn(MainDbAdapter.TASK_CAR_TABLE_NAME, MainDbAdapter.GEN_COL_ISACTIVE_NAME) + " = 'Y'" +
			" ORDER BY " + 
				DB.sqlConcatTableColumn(MainDbAdapter.CAR_TABLE_NAME, MainDbAdapter.GEN_COL_NAME_NAME);

        SimpleCursorAdapter cursorAdapter =
        	new SimpleCursorAdapter(this, R.layout.twoline_list2_activity,
        							mDbAdapter.execSelectSql(selectSql, selectionArgs),
                                    new String[]{MainDbAdapter.GEN_COL_NAME_NAME, "FirstRun"}, 
                                    new int[]{R.id.tvTwoLineListText1, R.id.tvTwoLineListText2}
                );
        cursorAdapter.setViewBinder(new TaskCarLinkDataBinder());
        lvLinkedCarsList.setAdapter(cursorAdapter);
    }

    protected AdapterView.OnItemClickListener mLinkedCarListItemClickListener =
        new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> av, View view, int position, long id) {
    			isFinishAfterSave = false;
    			saveData();
    			isFinishAfterSave = true;
    			mPrefEditor.putLong("TaskCarLinkId", id);
    			mPrefEditor.commit();
                showDialog(StaticValues.DIALOG_TASK_CAR_LINK);
            }
    };

	
	/* (non-Javadoc)
	 * @see org.andicar.activity.BaseActivity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		fillLinkedCarsData();
	}

	@Override
	protected void saveData() {
		String strRetVal = checkMandatory(vgRoot);
		if (strRetVal != null) {
			Toast toast = Toast.makeText(getApplicationContext(),
					mResource.getString(R.string.GEN_FillMandatory) + ": "
							+ strRetVal, Toast.LENGTH_SHORT);
			toast.show();
			saveSuccess = false;
			return;
		}

		strRetVal = checkNumeric(vgRoot, false);
		if (strRetVal != null) {
			Toast toast = Toast.makeText(getApplicationContext(),
					mResource.getString(R.string.GEN_NumberFormatException)
							+ ": " + strRetVal, Toast.LENGTH_SHORT);
			toast.show();
			saveSuccess = false;
			return;
		}
		
		if(isMileageEnabled && 
				(etMileage.getText().toString() == null || etMileage.getText().toString().length() == 0)){
			Toast toast = Toast.makeText(getApplicationContext(),
					mResource.getString(R.string.TaskEditActivity_FillMileage), Toast.LENGTH_SHORT);
			toast.show();
			etMileage.requestFocus();
			saveSuccess = false;
			return;
		}

		if(isTimingEnabled && isRecurent && 
				(etFrequency.getText().toString() == null || etFrequency.getText().toString().length() == 0)){
			Toast toast = Toast.makeText(getApplicationContext(),
					mResource.getString(R.string.TaskEditActivity_FillFrequency), Toast.LENGTH_SHORT);
			toast.show();
			etFrequency.requestFocus();
			saveSuccess = false;
			return;
		}

		//at least one linked car required
		if( isFinishAfterSave && !isDeleteLinkedCarsOnSave){
			if(lvLinkedCarsList.getCount() == 0){ //no linked car
				Toast toast = Toast.makeText(getApplicationContext(),
						mResource.getString(R.string.TaskEditActivity_NoLinkedCarsMsg), Toast.LENGTH_SHORT);
				toast.show();
				return;
			}
		}
		if(!isRecurent && isTimingEnabled){//check if the starting time is in the future
			if(mlDateTimeInSeconds * 1000 < System.currentTimeMillis()){
				Toast toast = Toast.makeText(getApplicationContext(),
						mResource.getString(R.string.TaskEditActivity_StartingTimeInFutureMsg), Toast.LENGTH_SHORT);
				toast.show();
				saveSuccess = false;
				return;
			}
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
			data.put(MainDbAdapter.TASK_COL_RUNDAY_NAME, spnDaysOfWeek.getSelectedItemId() + 1);
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
		
		if(!isRecurent)
			data.put(MainDbAdapter.TASK_COL_RUNTIME_NAME, mlDateTimeInSeconds);
		else
			data.put(MainDbAdapter.TASK_COL_RUNTIME_NAME, mcalDateTime2.getTimeInMillis() / 1000);
		
		data.put(MainDbAdapter.TASK_COL_REMINDERDAYS_NAME, etReminderDays.getText().toString());
		data.put(MainDbAdapter.TASK_COL_RUNMILEAGE_NAME, etMileage.getText().toString());
		data.put(MainDbAdapter.TASK_COL_REMINDERMILEAGES_NAME, etReminderMileage.getText().toString());
		
		if (mRowId == -1) {
			mRowId = mDbAdapter.createRecord(MainDbAdapter.TASK_TABLE_NAME, data);
			if(mRowId > 0)
				saveSuccess = true;
			else
				saveSuccess = false;
			if(isFinishAfterSave){
				Intent intent = new Intent(this, TodoManagementService.class);
				intent.putExtra("TaskID", mRowId);
				this.startService(intent);
				finish();
			}
		} else {
			int updResult = mDbAdapter.updateRecord(
					MainDbAdapter.TASK_TABLE_NAME, mRowId, data);
			if (updResult != -1) {
				saveSuccess = false;
				String errMsg = "";
				errMsg = mResource.getString(updResult);
				if (updResult == R.string.ERR_000)
					errMsg = errMsg + "\n" + mDbAdapter.lastErrorMessage;
				madbErrorAlert.setMessage(errMsg);
				madError = madbErrorAlert.create();
				madError.show();
			} else{
				saveSuccess = true;
				if(isFinishAfterSave){
					Intent intent = new Intent(this, TodoManagementService.class);
					intent.putExtra("TaskID", mRowId);
					this.startService(intent);
					finish();
				}
			}
		}
		//delete existent linked cars if the configuration not support linked cars
		if(isFinishAfterSave && isDeleteLinkedCarsOnSave){
			String[] selectionArgs = {Long.toString(mRowId)};
			mDbAdapter.deleteRecords(MainDbAdapter.TASK_CAR_TABLE_NAME, 
						MainDbAdapter.TASK_CAR_COL_TASK_ID_NAME + "= ?", selectionArgs);
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
			isFinishAfterSave = false;
			saveData();
			isFinishAfterSave = true;
			mPrefEditor.putLong("TaskCarLinkId", -1);
			mPrefEditor.commit();
			if(!saveSuccess)
				return;
			mLinkDialogCarSelectCondition = MainDbAdapter.isActiveCondition +
				" AND " + MainDbAdapter.GEN_COL_ROWID_NAME + 
												" NOT IN (SELECT " + MainDbAdapter.TASK_CAR_COL_CAR_ID_NAME + 
														" FROM " + MainDbAdapter.TASK_CAR_TABLE_NAME + " " +
														" WHERE " + MainDbAdapter.TASK_CAR_COL_TASK_ID_NAME + " = " + Long.toString(mRowId) +
														")";
			if(!isRecurent && isMileageEnabled && !isTimingEnabled){ //select only cars with current mileage < due mileage
				mLinkDialogCarSelectCondition  = mLinkDialogCarSelectCondition  + 
					" AND " + MainDbAdapter.CAR_COL_INDEXCURRENT_NAME + " < " + etMileage.getText().toString();
			}
			
			//check if unlinked cars exists.
			String checkSQL = "SELECT * " +
								" FROM " + MainDbAdapter.CAR_TABLE_NAME + " " +
								" WHERE " + mLinkDialogCarSelectCondition;
			Cursor c = mDbAdapter.query(checkSQL, null);
			
			if(!c.moveToNext()){//no record exist
				c.close();
	            AndiCarDialogBuilder builder = null; 
				if(!isRecurent && isMileageEnabled && !isTimingEnabled){
		            builder = new AndiCarDialogBuilder(TaskEditActivity.this, 
		            		AndiCarDialogBuilder.DIALOGTYPE_WARNING, mResource.getString(R.string.GEN_Warning));
		            builder.setMessage(mResource.getString(R.string.TaskEditActivity_NoCarsLinkedMsg));
				}
				else{
		            builder = new AndiCarDialogBuilder(TaskEditActivity.this, 
		            		AndiCarDialogBuilder.DIALOGTYPE_INFO, mResource.getString(R.string.GEN_Info));
		            builder.setMessage(mResource.getString(R.string.TaskEditActivity_AllCarsLinkedMsg));
				}
	            builder.setCancelable(false);
	            
	            builder.setPositiveButton(mResource.getString(R.string.GEN_OK),
	                    new DialogInterface.OnClickListener() {
		                    public void onClick(DialogInterface dialog, int id) {
		                    };
	            		});
	            AlertDialog alert = builder.create();
	            alert.show();
	            
			}
			else{
				c.close();
				showDialog(StaticValues.DIALOG_TASK_CAR_LINK);
			}
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
			fillLinkedCarsData();
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
			fillLinkedCarsData();
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
		tvDateTimeValue2.setText(DateFormat.format(DateFormat.HOUR + ":" + DateFormat.MINUTE + " " + DateFormat.AM_PM, mcalDateTime2));
//		tvDateTimeValue2.setText(DateFormat.getTimeFormat(
//				getApplicationContext()).format(mcalDateTime2.getTime()));
	}

	
	/* (non-Javadoc)
	 * @see android.app.Activity#onPrepareDialog(int, android.app.Dialog)
	 */
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
		if(id != StaticValues.DIALOG_TASK_CAR_LINK)
			return;
		
        initDialog();
		long linkId = mPreferences.getLong("TaskCarLinkId", -1);
		if(linkId != -1){
			Cursor c = mDbAdapter.fetchRecord(MainDbAdapter.TASK_CAR_TABLE_NAME, MainDbAdapter.taskCarTableColNames, linkId);
	        initDialogControls(c.getLong(MainDbAdapter.TASK_CAR_COL_CAR_ID_POS));
	        long linkedCarStartDate = c.getLong(MainDbAdapter.TASK_CAR_COL_FIRSTRUN_DATE_POS) * 1000;
	        mLinkDialogStartingDateTimeCal = Calendar.getInstance();
	        mLinkDialogStartingDateTimeCal.setTimeInMillis(linkedCarStartDate);
	        mLinkDialogStartingYear = mLinkDialogStartingDateTimeCal.get(Calendar.YEAR);
	        mLinkDialogStartingMonth = mLinkDialogStartingDateTimeCal.get(Calendar.MONTH);
	        mLinkDialogStartingDay = mLinkDialogStartingDateTimeCal.get(Calendar.DAY_OF_MONTH);
	        mLinkDialogStartingHour = mLinkDialogStartingDateTimeCal.get(Calendar.HOUR_OF_DAY);
	        mLinkDialogStartingMinute = mLinkDialogStartingDateTimeCal.get(Calendar.MINUTE);
	        updateLinkDialogStartingDateTime();
	        etLinkedCarIndexStart.setText(c.getString(MainDbAdapter.TASK_CAR_COL_FIRSTRUN_MILEAGE_POS));
	        c.close();
		}
		else{
			initDialogControls(-1);
				
			mLinkDialogStartingDateTimeCal = Calendar.getInstance();
			mLinkDialogStartingDateTimeCal.add(Calendar.HOUR_OF_DAY, 1);
			mLinkDialogStartingDateTimeCal.set(Calendar.MINUTE, 0);
			mLinkDialogStartingDateTimeCal.set(Calendar.SECOND, 0);
			mLinkDialogStartingDateTimeCal.set(Calendar.MILLISECOND, 0);
			mLinkDialogStartingYear = mLinkDialogStartingDateTimeCal.get(Calendar.YEAR);
			mLinkDialogStartingMonth = mLinkDialogStartingDateTimeCal.get(Calendar.MONTH);
			mLinkDialogStartingDay = mLinkDialogStartingDateTimeCal.get(Calendar.DAY_OF_MONTH);
			mLinkDialogStartingHour = mLinkDialogStartingDateTimeCal.get(Calendar.HOUR_OF_DAY);
			mLinkDialogStartingMinute = mLinkDialogStartingDateTimeCal.get(Calendar.MINUTE);
	        updateLinkDialogStartingDateTime();
	        etLinkedCarIndexStart.setText("0");
		}
	}

	private void initDialog() {
		llDialogStartingDateZone = (LinearLayout) linkView.findViewById(R.id.llStartingDateZone);
        llDialogStartingMileageZone = (LinearLayout) linkView.findViewById(R.id.llStartingMileageZone);

        if(!isRecurent){
        	llDialogStartingDateZone.setVisibility(View.GONE);
        	llDialogStartingMileageZone.setVisibility(View.GONE);
        	return;
        }
        else{
        	llDialogStartingDateZone.setVisibility(View.VISIBLE);
        	llDialogStartingMileageZone.setVisibility(View.VISIBLE);
        }
        	
        if(isTimingEnabled && isDiffStartingTime)
        	llDialogStartingDateZone.setVisibility(View.VISIBLE);
        else
        	llDialogStartingDateZone.setVisibility(View.GONE);

        if(isMileageEnabled)
        	llDialogStartingMileageZone.setVisibility(View.VISIBLE);
        else
        	llDialogStartingMileageZone.setVisibility(View.GONE);
	}

	private void initDialogControls(long carId) {
		if(carId != -1)
			mLinkDialogCarSelectCondition = MainDbAdapter.isActiveCondition;
		
		initSpinner(spnLinkDialogCar, MainDbAdapter.CAR_TABLE_NAME,
			MainDbAdapter.genColName,
			new String[] { MainDbAdapter.GEN_COL_NAME_NAME },
			mLinkDialogCarSelectCondition, null,
			MainDbAdapter.GEN_COL_NAME_NAME, carId, false);
		
		if(carId != -1)
			spnLinkDialogCar.setEnabled(false);
		else
			spnLinkDialogCar.setEnabled(true);
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
	        		onStartingDateSetListener, mLinkDialogStartingYear, mLinkDialogStartingMonth, mLinkDialogStartingDay);
		}
		//time part
		else if(id == StaticValues.DIALOG_DATE_TO_PICKER){
	        return new TimePickerDialog(this,
	        		onStartingTimeSetListener, mLinkDialogStartingHour, mLinkDialogStartingMinute, false);
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
	        tvLinkCarDialogFirstRunDate = (TextView) linkView.findViewById(R.id.tvFirstRunDate);
	        etLinkedCarIndexStart = (EditText) linkView.findViewById(R.id.etIndexStart);
	        
			//we don't need here the task selection zone.
			((LinearLayout) linkView.findViewById(R.id.llTaskZone)).setVisibility(View.GONE);
//			spnLinkDialogTask = (Spinner) linkView.findViewById(R.id.spnTask);
//			initSpinner(spnLinkDialogTask, MainDbAdapter.TASK_TABLE_NAME,
//					MainDbAdapter.genColName,
//					new String[] { MainDbAdapter.GEN_COL_NAME_NAME },
//					MainDbAdapter.isActiveCondition, null,
//					MainDbAdapter.GEN_COL_NAME_NAME, 1, false);

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

	
    private DatePickerDialog.OnDateSetListener onStartingDateSetListener =
        new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                    int dayOfMonth) {
                mLinkDialogStartingYear = year;
                mLinkDialogStartingMonth = monthOfYear;
                mLinkDialogStartingDay = dayOfMonth;
            	updateLinkDialogStartingDateTime();
            }
        };
        
    private TimePickerDialog.OnTimeSetListener onStartingTimeSetListener =
        new TimePickerDialog.OnTimeSetListener() {
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                mLinkDialogStartingHour = hourOfDay;
                mLinkDialogStartingMinute = minute;
               	updateLinkDialogStartingDateTime();
            }
        };

    private void updateLinkDialogStartingDateTime() {
        mLinkDialogStartingDateTimeCal.set(mLinkDialogStartingYear, mLinkDialogStartingMonth, mLinkDialogStartingDay, mLinkDialogStartingHour, mLinkDialogStartingMinute, 0);
        mlStartingDateTimeInSeconds = mLinkDialogStartingDateTimeCal.getTimeInMillis() / 1000;
        tvLinkCarDialogFirstRunDate.setText(
        		DateFormat.getDateFormat(getApplicationContext()).format(mLinkDialogStartingDateTimeCal.getTime()) + " " +
					DateFormat.getTimeFormat(getApplicationContext()).format(mLinkDialogStartingDateTimeCal.getTime())
        );
    }

    private DialogInterface.OnClickListener linkDialogButtonlistener = new DialogInterface.OnClickListener() {
	    public void onClick(DialogInterface dialog, int whichButton) {
	        if (whichButton == DialogInterface.BUTTON_POSITIVE) {

	        	if(isMileageEnabled && etLinkedCarIndexStart.getText().toString().length() == 0){
	    			Toast toast = Toast.makeText(getApplicationContext(),
	    					mResource.getString(R.string.TaskEditActivity_FillMileage), Toast.LENGTH_SHORT);
	    			toast.show();
	    			return;
	        	}
	        	long selectedCarId = spnLinkDialogCar.getSelectedItemId();
	        	String selectedCarName = "";
	            String selection = MainDbAdapter.GEN_COL_ROWID_NAME + "= ? ";
	            String[] selectionArgs = {Long.toString(selectedCarId)};
	            
	        	Cursor c = mDbAdapter.query(MainDbAdapter.CAR_TABLE_NAME, MainDbAdapter.genColName, 
	        			selection, selectionArgs, null, null, null);
	        	if(c.moveToNext())
	        		selectedCarName = c.getString(1);
	        	c.close();
	        	
	    		ContentValues data = new ContentValues();
	    		//name = task name <-> car name
	    		data.put(MainDbAdapter.GEN_COL_NAME_NAME, etName.getText().toString() + " <-> " + selectedCarName);
	    		data.put(MainDbAdapter.GEN_COL_ISACTIVE_NAME, "Y");
	    		data.put(MainDbAdapter.TASK_CAR_COL_TASK_ID_NAME, mRowId);
	    		data.put(MainDbAdapter.TASK_CAR_COL_CAR_ID_NAME, selectedCarId);
	    		data.put(MainDbAdapter.TASK_CAR_COL_FIRSTRUN_DATE_NAME, mlStartingDateTimeInSeconds);
	    		data.put(MainDbAdapter.TASK_CAR_COL_FIRSTRUN_MILEAGE_NAME, etLinkedCarIndexStart.getText().toString());

	    		long linkId = mPreferences.getLong("TaskCarLinkId", -1);
	    		if(linkId == -1){ //new link
		    		long retVal = mDbAdapter.createRecord(MainDbAdapter.TASK_CAR_TABLE_NAME, data);
		    		if(retVal < 0){
		    			String errorMessage;
		    			if(retVal == -1)
		    				errorMessage = mDbAdapter.lastErrorMessage;
		    			else{
		    				int errCode = -1 * ((Long)retVal).intValue();
		    				errorMessage = mResource.getString(errCode);
		    			}
						madbErrorAlert.setMessage(errorMessage);
						madError = madbErrorAlert.create();
						madError.show();
		    		}
	    		}
	    		else{
		    		long retVal = mDbAdapter.updateRecord(MainDbAdapter.TASK_CAR_TABLE_NAME, linkId, data);
		    		if(retVal > -1){
		    			String errorMessage;
		    			if(retVal == -1)
		    				errorMessage = mDbAdapter.lastErrorMessage;
		    			else{
		    				int errCode = -1 * ((Long)retVal).intValue();
		    				errorMessage = mResource.getString(errCode);
		    			}
						madbErrorAlert.setMessage(errorMessage);
						madError = madbErrorAlert.create();
						madError.show();
		    		}
	    		}
	    		fillLinkedCarsData();
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
			fillLinkedCarsData();
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
		
		if((isTimingEnabled && !isMileageEnabled && !isDiffStartingTime) ||
				(!isRecurent && !isMileageEnabled)){
			llLinkedCarsZone.setVisibility(View.GONE);
			llLinkedCarsHelp.setVisibility(View.GONE);
			llLinkedCarsList.setVisibility(View.GONE);
			isDeleteLinkedCarsOnSave = true;
		}
		else{
			llLinkedCarsZone.setVisibility(View.VISIBLE);
			llLinkedCarsHelp.setVisibility(View.VISIBLE);
			llLinkedCarsList.setVisibility(View.VISIBLE);
			isDeleteLinkedCarsOnSave = false;
		}

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
        menu.add(0, StaticValues.CONTEXT_MENU_DELETE_ID, 0, mResource.getString(R.string.MENU_DeleteCaption));
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case StaticValues.CONTEXT_MENU_EDIT_ID:
    			isFinishAfterSave = false;
    			saveData();
    			isFinishAfterSave = true;
    			mPrefEditor.putLong("TaskCarLinkId", mLongClickId);
    			mPrefEditor.commit();
    			if(saveSuccess)
    				showDialog(StaticValues.DIALOG_TASK_CAR_LINK);
                return true;
            case StaticValues.CONTEXT_MENU_DELETE_ID:
                AndiCarDialogBuilder builder = new AndiCarDialogBuilder(TaskEditActivity.this, 
                		AndiCarDialogBuilder.DIALOGTYPE_QUESTION, mResource.getString(R.string.GEN_Confirm));
                builder.setMessage(mResource.getString(R.string.GEN_DeleteConfirmation));
                builder.setCancelable(false);
                builder.setPositiveButton(mResource.getString(R.string.GEN_YES),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                int deleteResult = mDbAdapter.deleteRecord(MainDbAdapter.TASK_CAR_TABLE_NAME, mLongClickId);
                                if(deleteResult != -1) {
                                    madbErrorAlert.setMessage(mResource.getString(deleteResult));
                                    madError = madbErrorAlert.create();
                                    madError.show();
                                }
                                else {
                                    fillLinkedCarsData();
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
        }
        return super.onContextItemSelected(item);
    }
}
