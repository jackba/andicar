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

import org.andicar.activity.dialog.AndiCarDialogBuilder;
import org.andicar.persistence.DB;
import org.andicar.persistence.MainDbAdapter;
import org.andicar.persistence.TaskCarLinkDataBinder;
import org.andicar.service.ToDoManagementService;
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
	private EditText etFrequency = null;
	private EditText etTimeReminder = null;
	private EditText etMileage = null;
	private EditText etReminderMileage = null;
	private EditText etLinkedCarIndexStart = null;
	private EditText etNoOfNextToDo = null;

	private CheckBox ckIsActive = null;
	private CheckBox ckIsDifferentStartingTime = null;
	private CheckBox ckOnLastDay = null;

	private Spinner spnTaskType = null;
	private Spinner spnScheduleFrequency = null;
    private Spinner spnLinkDialogCar = null;
    private Spinner spnLastDayMonth = null;

	private TextView tvMileageLabelEvery = null;
	private TextView tvFirstTimeRunExplanation = null;
	private TextView tvFirstMileageRunExplanation = null;
	private TextView tvOr = null;
	private TextView tvOrLastDay = null;
	private TextView tvLinkCarDialogFirstRunDate = null;
	private TextView tvStartingTimeLbl = null;
	private TextView tvTimeReminderUnitLbl = null;

	private ImageButton btnNewTaskType = null;
	private ImageButton btnLinkCar = null;

	private RadioGroup rgRepeating = null;
	private RadioGroup rgScheduleType = null;
	
	private RadioButton rbOneTime = null;
	private RadioButton rbRecurent = null;
	private RadioButton rbTimeDriven = null;
	private RadioButton rbMileageDriven = null;
	private RadioButton rbTimeAndMileageDriven = null;
	
	private LinearLayout llStartingTime = null;
	private LinearLayout llRecurentTimeSettings = null;
	private LinearLayout llMileageZone = null;
	private LinearLayout llLinkedCarsZone = null;
	private LinearLayout llLinkedCarsHelp = null;
	private LinearLayout llLinkedCarsList = null;
	private LinearLayout llLastMonthDay = null;
	private LinearLayout llTimeReminder = null;
	private LinearLayout llToDoCountZone = null;
	//used in link dialog
	private LinearLayout llDialogStartingDateZone = null;
	private LinearLayout llDialogStartingMileageZone = null;
    protected TimePickerDialog mLinkDialogTimePickerDialog = null;
    protected DatePickerDialog mLinkDialogDatePickerDialog = null;

    private ListView lvLinkedCarsList = null;
	
    private View linkView;

	private Calendar mLinkDialogStartingDateTimeCal;

	private int mLinkDialogStartingYear;
	private int mLinkDialogStartingMonth;
	private int mLinkDialogStartingDay;
	private int mLinkDialogStartingHour;
	private int mLinkDialogStartingMinute;

	private long mTimeFrequencyTypeId = -1;
	private long mlStartingDateTimeInSeconds;
	private long mLongClickId;
	private boolean isDiffStartingTime = true;
	private boolean isTimingEnabled = true;
	private boolean isMileageEnabled = true;
	private boolean isRecurrent = true;
	private boolean saveSuccess = true;
	private boolean isDeleteLinkedCarsOnSave = false;
	private String mScheduledFor = StaticValues.TASK_SCHEDULED_FOR_BOTH;
	private String mLinkDialogCarSelectCondition = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		String operation = mBundleExtras.getString("Operation"); // E = edit, N = new

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
			isRecurrent = (c.getString(MainDbAdapter.TASK_COL_ISRECURRENT_POS).equals("Y"));
			if(isRecurrent)
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
			if(c.getString(MainDbAdapter.TASK_COL_ISDIFFERENTSTARTINGTIME_POS) != null){
				ckIsDifferentStartingTime.setChecked(c.getString(MainDbAdapter.TASK_COL_ISDIFFERENTSTARTINGTIME_POS).equals("Y"));
				isDiffStartingTime = c.getString(MainDbAdapter.TASK_COL_ISDIFFERENTSTARTINGTIME_POS).equals("Y");
				etFrequency.setText(c.getString(MainDbAdapter.TASK_COL_TIMEFREQUENCY_POS));
			}
			mTimeFrequencyTypeId = c.getLong(MainDbAdapter.TASK_COL_TIMEFREQUENCYTYPE_POS);
			spnScheduleFrequency.setSelection( ((Long)mTimeFrequencyTypeId).intValue() - 1);
			Long startingTimeInMilis;
			if(c.getString(MainDbAdapter.TASK_COL_STARTINGTIME_POS) != null)
				startingTimeInMilis = c.getLong(MainDbAdapter.TASK_COL_STARTINGTIME_POS) * 1000;
			else
				startingTimeInMilis = System.currentTimeMillis();
			if(isTimingEnabled){
	            initDateTime(startingTimeInMilis);
//				if(startingTimeInMilis < StaticValues.ONE_DAY_IN_MILISECONDS){
	            if(mcalDateTime.get(Calendar.YEAR) == 1970){
					ckOnLastDay.setChecked(true);
					spnLastDayMonth.setSelection(mcalDateTime.get(Calendar.MONTH));
					initTimeOnly = true;
				}
				else{
					initTimeOnly = false;
				}
				initDateTime(startingTimeInMilis);
			}
			else
				initDateTime(System.currentTimeMillis() + StaticValues.ONE_DAY_IN_MILISECONDS);
			
			if(c.getString(MainDbAdapter.TASK_COL_TIMEREMINDERSTART_POS) != null)
				etTimeReminder.setText(c.getString(MainDbAdapter.TASK_COL_TIMEREMINDERSTART_POS));
			
			if(c.getString(MainDbAdapter.TASK_COL_RUNMILEAGE_POS) != null)
				etMileage.setText(c.getString(MainDbAdapter.TASK_COL_RUNMILEAGE_POS));

			etReminderMileage.setText(c.getString(MainDbAdapter.TASK_COL_MILEAGEREMINDERSTART_POS));
			
			etNoOfNextToDo.setText(c.getString(MainDbAdapter.TASK_COL_TODOCOUNT_POS));

			c.close();
		} else { // new
			ckIsActive.setChecked(true);
			initSpinner(spnTaskType, MainDbAdapter.TASKTYPE_TABLE_NAME,
					MainDbAdapter.genColName,
					new String[] { MainDbAdapter.GEN_COL_NAME_NAME },
					MainDbAdapter.isActiveCondition, null,
					MainDbAdapter.GEN_COL_NAME_NAME, 0, false);
			rbRecurent.setChecked(true);
			rbTimeAndMileageDriven.setChecked(true);
			llStartingTime.setVisibility(View.GONE);
			llRecurentTimeSettings.setVisibility(View.VISIBLE);
			initDateTime(System.currentTimeMillis() + StaticValues.ONE_DAY_IN_MILISECONDS);
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
		spnTaskType.setOnItemSelectedListener(spnTaskTypeOnItemSelectedListener);
		btnNewTaskType = (ImageButton) findViewById(R.id.btnNewTaskType);
		btnNewTaskType.setOnClickListener(onNewTaskTypeClickListener);
		btnLinkCar = (ImageButton) findViewById(R.id.btnLinkCar);
		btnLinkCar.setOnClickListener(onLinkCarClickListener);
		
		rbOneTime = (RadioButton) findViewById(R.id.rbOneTime);
		rbRecurent = (RadioButton) findViewById(R.id.rbRecurent);
		ckIsDifferentStartingTime = (CheckBox) findViewById(R.id.ckIsDifferentStartingTime);
		ckIsDifferentStartingTime.setOnCheckedChangeListener(ckIsDifferentStartingTimeCheckedChange);
		llStartingTime = (LinearLayout) findViewById(R.id.llStartingTime);
		llRecurentTimeSettings = (LinearLayout) findViewById(R.id.llRecurentTimeSettings);
		llLastMonthDay = (LinearLayout) findViewById(R.id.llLastMonthDay);
		llMileageZone = (LinearLayout)findViewById(R.id.llMileageZone);
		tvMileageLabelEvery = (TextView)findViewById(R.id.tvMileageLabelEvery);
		tvFirstTimeRunExplanation = (TextView)findViewById(R.id.tvFirstTimeRunExplanation);
		tvOrLastDay = (TextView)findViewById(R.id.tvOrLastDay);
		tvStartingTimeLbl = (TextView)findViewById(R.id.tvStartingTimeLbl);
		
		llLinkedCarsZone = (LinearLayout)findViewById(R.id.llLinkedCarsZone);
		llLinkedCarsHelp = (LinearLayout)findViewById(R.id.llLinkedCarsHelp);
		llTimeReminder = (LinearLayout)findViewById(R.id.llTimeReminder);
		llToDoCountZone = (LinearLayout)findViewById(R.id.llToDoCountZone);

		rgRepeating = (RadioGroup) findViewById(R.id.rgRepeating);

		spnScheduleFrequency = (Spinner) findViewById(R.id.spnScheduleFrequency);
		spnScheduleFrequency.setOnItemSelectedListener(spnScheduleFrequencyOnItemSelectedListener);
		spnLastDayMonth = (Spinner) findViewById(R.id.spnLastDayMonth);
		
		ckOnLastDay = (CheckBox)findViewById(R.id.ckOnLastDay);

		rgScheduleType = (RadioGroup) findViewById(R.id.rgScheduleType);
		etFrequency = (EditText)findViewById(R.id.etFrequency);
		etTimeReminder = (EditText)findViewById(R.id.etTimeReminder);
		tvTimeReminderUnitLbl = (TextView)findViewById(R.id.tvTimeReminderUnitLbl);
		etMileage = (EditText)findViewById(R.id.etMileage);
		etReminderMileage = (EditText)findViewById(R.id.etReminderMileage);

		rbTimeDriven = (RadioButton) findViewById(R.id.rbTimeDriven);
		rbMileageDriven = (RadioButton) findViewById(R.id.rbMileageDriven);
		rbTimeAndMileageDriven = (RadioButton) findViewById(R.id.rbTimeAndMileageDriven);
		tvOr = (TextView)findViewById(R.id.tvOr);
		
		llLinkedCarsList = (LinearLayout)findViewById(R.id.llLinkedCarsList);
		lvLinkedCarsList = (ListView)findViewById(R.id.lvLinkedCarsList);
		
		tvFirstMileageRunExplanation = (TextView)findViewById(R.id.tvFirstMileageRunExplanation);
		
		etNoOfNextToDo = (EditText)findViewById(R.id.etNoOfNextToDo);
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
    	if(isRecurrent){
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

    	int listLayout = R.layout.twoline_list2_activity_s01;
    	if(mPreferences.getString("UIStyle", "s01").equalsIgnoreCase("s00"))
    		listLayout = R.layout.twoline_list2_activity_s00;
    	else if(mPreferences.getString("UIStyle", "s01").equalsIgnoreCase("s01"))
    		listLayout = R.layout.twoline_list2_activity_s01;

        SimpleCursorAdapter cursorAdapter =
        	new SimpleCursorAdapter(this, listLayout,
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
//    			isFinishAfterSave = false;
//    			saveData();
//    			isFinishAfterSave = true;
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
	protected boolean saveData() {
		
		if(isMileageEnabled){ 
			if(etMileage.getText().toString() == null || etMileage.getText().toString().length() == 0 
					|| Integer.parseInt(etMileage.getText().toString()) == 0){
				Toast toast = Toast.makeText(getApplicationContext(),
						mResource.getString(R.string.TaskEditActivity_FillMileage), Toast.LENGTH_SHORT);
				toast.show();
				etMileage.requestFocus();
				saveSuccess = false;
				return false;
			}
			if(etReminderMileage.getText().toString() == null || etReminderMileage.getText().toString().length() == 0){
				etReminderMileage.setText("0");
			}
			
			if(Integer.parseInt(etMileage.getText().toString()) < Integer.parseInt(etReminderMileage.getText().toString())){
				Toast toast = Toast.makeText(getApplicationContext(),
						mResource.getString(R.string.TaskEditActivity_MileageFrequencySmallerThanReminder), Toast.LENGTH_SHORT);
				toast.show();
				etMileage.requestFocus();
				saveSuccess = false;
				return false;
			}

		}

		if(isTimingEnabled && isRecurrent){
			if(etFrequency.getText().toString() == null || etFrequency.getText().toString().length() == 0
						|| Integer.parseInt(etFrequency.getText().toString()) == 0){
				Toast toast = Toast.makeText(getApplicationContext(),
						mResource.getString(R.string.TaskEditActivity_FillFrequency), Toast.LENGTH_SHORT);
				toast.show();
				etFrequency.requestFocus();
				saveSuccess = false;
				return false;
			}
			if(etTimeReminder.getText().toString() == null || etTimeReminder.getText().toString().length() == 0){
				etTimeReminder.setText("0");
			}
		}

		//at least one linked car required
		if( isFinishAfterSave && !isDeleteLinkedCarsOnSave){
			if(lvLinkedCarsList.getCount() == 0){ //no linked car
				Toast toast = Toast.makeText(getApplicationContext(),
						mResource.getString(R.string.TaskEditActivity_NoLinkedCarsMsg), Toast.LENGTH_SHORT);
				toast.show();
				return false;
			}
		}
		if(!isRecurrent && isTimingEnabled){//check if the starting time is in the future
			if(mlDateTimeInSeconds * 1000 < System.currentTimeMillis()){
				Toast toast = Toast.makeText(getApplicationContext(),
						mResource.getString(R.string.TaskEditActivity_StartingTimeInFutureMsg), Toast.LENGTH_SHORT);
				toast.show();
				saveSuccess = false;
				return false;
			}
		}
		
		if(isRecurrent && 
				(etNoOfNextToDo.getText().toString() == null || etNoOfNextToDo.getText().toString().length() == 0)){
			Toast toast = Toast.makeText(getApplicationContext(),
					mResource.getString(R.string.GEN_FillMandatory) + ": " + mResource.getString(R.string.TaskEditActivity_ToDoCount), 
					Toast.LENGTH_SHORT);
			toast.show();
			saveSuccess = false;
			etNoOfNextToDo.requestFocus();
			return false;
		}
		
		ContentValues data = new ContentValues();
		data.put(MainDbAdapter.GEN_COL_NAME_NAME, etName.getText().toString());
		data.put(MainDbAdapter.GEN_COL_ISACTIVE_NAME,
				(ckIsActive.isChecked() ? "Y" : "N"));
		data.put(MainDbAdapter.GEN_COL_USER_COMMENT_NAME, etUserComment
				.getText().toString());

		data.put(MainDbAdapter.TASK_COL_TASKTYPE_ID_NAME, spnTaskType.getSelectedItemId());
		data.put(MainDbAdapter.TASK_COL_SCHEDULEDFOR_NAME, mScheduledFor);
		data.put(MainDbAdapter.TASK_COL_ISRECURRENT_NAME, (isRecurrent ? "Y" : "N"));
		if(isRecurrent)
			data.put(MainDbAdapter.TASK_COL_TODOCOUNT_NAME, etNoOfNextToDo.getText().toString());
		else
			data.put(MainDbAdapter.TASK_COL_TODOCOUNT_NAME, 1);
		if(isTimingEnabled){
			if(isRecurrent){
				data.put(MainDbAdapter.TASK_COL_ISDIFFERENTSTARTINGTIME_NAME, (isDiffStartingTime ? "Y" : "N"));
				data.put(MainDbAdapter.TASK_COL_TIMEFREQUENCY_NAME, etFrequency.getText().toString());
				if(isDiffStartingTime)
					data.put(MainDbAdapter.TASK_COL_STARTINGTIME_NAME, (Long)null);
				else{
					if(mTimeFrequencyTypeId == StaticValues.TASK_TIMEFREQUENCYTYPE_MONTHLY && ckOnLastDay.isChecked()){
						mcalDateTime.set(Calendar.MONTH, spnLastDayMonth.getSelectedItemPosition());
						data.put(MainDbAdapter.TASK_COL_STARTINGTIME_NAME, mcalDateTime.getTimeInMillis() / 1000);
					}
					else
						data.put(MainDbAdapter.TASK_COL_STARTINGTIME_NAME, mlDateTimeInSeconds);
				}
			}
			else{
				data.put(MainDbAdapter.TASK_COL_ISDIFFERENTSTARTINGTIME_NAME, (String)null);
				data.put(MainDbAdapter.TASK_COL_TIMEFREQUENCY_NAME, (String)null);
				data.put(MainDbAdapter.TASK_COL_STARTINGTIME_NAME, mlDateTimeInSeconds);
			}

			data.put(MainDbAdapter.TASK_COL_TIMEFREQUENCYTYPE_NAME, mTimeFrequencyTypeId);
			data.put(MainDbAdapter.TASK_COL_TIMEREMINDERSTART_NAME, etTimeReminder.getText().toString());
		}
		else{
			data.put(MainDbAdapter.TASK_COL_STARTINGTIME_NAME, (Integer)null);
			data.put(MainDbAdapter.TASK_COL_TIMEREMINDERSTART_NAME, (Integer)null);
			data.put(MainDbAdapter.TASK_COL_TIMEREMINDERSTART_NAME, (Integer)null);
		}
		if(isMileageEnabled){
			data.put(MainDbAdapter.TASK_COL_RUNMILEAGE_NAME, etMileage.getText().toString());
			data.put(MainDbAdapter.TASK_COL_MILEAGEREMINDERSTART_NAME, etReminderMileage.getText().toString());
		}
		else{
			data.put(MainDbAdapter.TASK_COL_RUNMILEAGE_NAME, (Integer)null);
			data.put(MainDbAdapter.TASK_COL_MILEAGEREMINDERSTART_NAME, (Integer)null);
		}
		
		if (mRowId == -1) {
			mRowId = mDbAdapter.createRecord(MainDbAdapter.TASK_TABLE_NAME, data);
			if(mRowId > 0)
				saveSuccess = true;
			else
				saveSuccess = false;
			if(isFinishAfterSave){
				//generate the To-Do's
                Intent intent = new Intent(this, ToDoManagementService.class);
				intent.putExtra("TaskID", mRowId);
				this.startService(intent);
				finish();
				return true;
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
				return false;
			} else{
				saveSuccess = true;
				if(isFinishAfterSave){
					//final save => recreate the todos (delete existing & recreate)
					String[] deleteArgs = {Long.toString(mRowId)};
//	                mDbAdapter.deleteRecords(MainDbAdapter.TASK_CAR_TABLE_NAME, MainDbAdapter.TASK_CAR_COL_TASK_ID_NAME + " = ?", deleteArgs);
	                mDbAdapter.deleteRecords(MainDbAdapter.TODO_TABLE_NAME, 
	                		MainDbAdapter.TODO_COL_TASK_ID_NAME + " = ? AND " + MainDbAdapter.TODO_COL_ISDONE_NAME + " = 'N'", 
	                		deleteArgs);

					Intent intent = new Intent(this, ToDoManagementService.class);
					intent.putExtra("TaskID", mRowId);
					this.startService(intent);
					finish();
					return true;
				}
			}
		}
		//delete existent linked cars if the current configuration not support linked cars
		String[] selectionArgs = {Long.toString(mRowId)};
		if(isFinishAfterSave && isDeleteLinkedCarsOnSave){
			mDbAdapter.deleteRecords(MainDbAdapter.TASK_CAR_TABLE_NAME, 
						MainDbAdapter.TASK_CAR_COL_TASK_ID_NAME + "= ?", selectionArgs);
		}
		
		ContentValues newContent = new ContentValues();
		if(!isTimingEnabled){
			newContent.put(MainDbAdapter.TASK_CAR_COL_FIRSTRUN_DATE_NAME, (Long)null);
			mDbAdapter.updateRecords(MainDbAdapter.TASK_CAR_TABLE_NAME, MainDbAdapter.TASK_CAR_COL_TASK_ID_NAME + " = ?", 
					selectionArgs, newContent);
		}
		if(!isMileageEnabled){
			newContent.put(MainDbAdapter.TASK_CAR_COL_FIRSTRUN_MILEAGE_NAME, (Integer)null);
			mDbAdapter.updateRecords(MainDbAdapter.TASK_CAR_TABLE_NAME, MainDbAdapter.TASK_CAR_COL_TASK_ID_NAME + " = ?", 
					selectionArgs, newContent);
		}
		return true;
	}

	@Override
	protected void setLayout() {
    	if(mPreferences.getString("UIStyle", "s01").equalsIgnoreCase("s00"))
    		setContentView(R.layout.task_edit_activity_s00);
    	else if(mPreferences.getString("UIStyle", "s01").equalsIgnoreCase("s01"))
    		setContentView(R.layout.task_edit_activity_s01);
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
			if(!isRecurrent && isMileageEnabled && !isTimingEnabled){ //select only cars with current mileage < due mileage
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
				if(!isRecurrent && isMileageEnabled && !isTimingEnabled){
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
				isRecurrent = false;
				mTimeFrequencyTypeId = StaticValues.TASK_TIMEFREQUENCYTYPE_ONETIME;
			} else { //
				isRecurrent = true;
				mTimeFrequencyTypeId = spnScheduleFrequency.getSelectedItemId() + 1;
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
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onPrepareDialog(int, android.app.Dialog)
	 */
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
//		if(id != StaticValues.DIALOG_TASK_CAR_LINK)
//			return;
		
		if(id == StaticValues.DIALOG_TASK_CAR_LINK){
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
//        mYear = mLinkDialogStartingYear;
//        mMonth = mLinkDialogStartingMonth;
//        mDay = mLinkDialogStartingDay;
//        mHour = mLinkDialogStartingHour;
//        mMinute = mLinkDialogStartingMinute;
        switch(id) {
	        case StaticValues.DIALOG_DATE_TO_PICKER:
	        	if(mLinkDialogTimePickerDialog != null)
	        		mLinkDialogTimePickerDialog.updateTime(mLinkDialogStartingHour, mLinkDialogStartingMinute);
	            break;
	        case StaticValues.DIALOG_DATE_FROM_PICKER:
	        	if(mLinkDialogDatePickerDialog != null)
	        		mLinkDialogDatePickerDialog.updateDate(mLinkDialogStartingYear, mLinkDialogStartingMonth, mLinkDialogStartingDay);
	            break;
        }
	}

	private void initDialog() {
		llDialogStartingDateZone = (LinearLayout) linkView.findViewById(R.id.llStartingDateZone);
        llDialogStartingMileageZone = (LinearLayout) linkView.findViewById(R.id.llStartingMileageZone);

        if(!isRecurrent){
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
		//date part
		else if(id == StaticValues.DIALOG_DATE_FROM_PICKER){
			mLinkDialogDatePickerDialog = new DatePickerDialog(this,
	        		onStartingDateSetListener, mLinkDialogStartingYear, mLinkDialogStartingMonth, mLinkDialogStartingDay); 
	        return mLinkDialogDatePickerDialog;
		}
		//time part
		else if(id == StaticValues.DIALOG_DATE_TO_PICKER){
			mLinkDialogTimePickerDialog = new TimePickerDialog(this,
	        		onStartingTimeSetListener, mLinkDialogStartingHour, mLinkDialogStartingMinute, false); 
	        return mLinkDialogTimePickerDialog;
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
	    		if(isTimingEnabled){
	    			if(!isDiffStartingTime|| !isRecurrent)
	    				data.put(MainDbAdapter.TASK_CAR_COL_FIRSTRUN_DATE_NAME, mlDateTimeInSeconds);
	    			else
	    				data.put(MainDbAdapter.TASK_CAR_COL_FIRSTRUN_DATE_NAME, mlStartingDateTimeInSeconds);
	    		}
	    		else
	    			data.put(MainDbAdapter.TASK_CAR_COL_FIRSTRUN_DATE_NAME, (Long)null);
	    		
	    		if(isMileageEnabled && isRecurrent)
    				data.put(MainDbAdapter.TASK_CAR_COL_FIRSTRUN_MILEAGE_NAME, etLinkedCarIndexStart.getText().toString());
	    		else
	    			data.put(MainDbAdapter.TASK_CAR_COL_FIRSTRUN_MILEAGE_NAME, (Long)null);

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
			
			mTimeFrequencyTypeId = arg3 + 1; //0 is one time
			initDateTime(mlDateTimeInSeconds * 1000);
			setSpecificLayout();
		}

		public void onNothingSelected(AdapterView<?> arg0) {
		}
	};
	
	protected AdapterView.OnItemSelectedListener spnTaskTypeOnItemSelectedListener = new AdapterView.OnItemSelectedListener() {
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
//			if(etUserComment.getText().toString() == null || etUserComment.getText().toString().length() == 0){
//				Cursor c = mDbAdapter.fetchRecord(MainDbAdapter.TASKTYPE_TABLE_NAME, MainDbAdapter.taskTypeTableColNames, arg3);
//				if(c != null){
//					String comment = c.getString(MainDbAdapter.GEN_COL_USER_COMMENT_POS);
//					if(comment != null && comment.length() > 0)
//						etUserComment.setText(comment);
//					c.close();
//				}
//			}
				
		}

		public void onNothingSelected(AdapterView<?> arg0) {
		}
	};

	protected CheckBox.OnCheckedChangeListener ckOnLastDayChecked = new CheckBox.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if(mTimeFrequencyTypeId != StaticValues.TASK_TIMEFREQUENCYTYPE_MONTHLY)
				return; //invalid
			else{
				if(isChecked){
					initTimeOnly = true; //just the time because we know the day (last day of month)
					initDateTime(mlDateTimeInSeconds * 1000);
					initTimeOnly = false; //put back the default value
				}
				else{
					initTimeOnly = false;
					if(mcalDateTime.get(Calendar.YEAR) == 1970)
						initDateTime(System.currentTimeMillis() + StaticValues.ONE_DAY_IN_MILISECONDS);
					else
						initDateTime(mlDateTimeInSeconds * 1000);
				}
				
				setSpecificLayout();
			}
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
	
	public void setSpecificLayout(){
		if (isRecurrent) {
			llToDoCountZone.setVisibility(View.VISIBLE);
			if(isTimingEnabled){
				tvStartingTimeLbl.setText(R.string.TaskEditActivity_StartingTimeLbl);
				ckIsDifferentStartingTime.setVisibility(View.VISIBLE);
				llRecurentTimeSettings.setVisibility(View.VISIBLE);
				llTimeReminder.setVisibility(View.VISIBLE);
				if(mTimeFrequencyTypeId == StaticValues.TASK_TIMEFREQUENCYTYPE_DAILY){
					tvTimeReminderUnitLbl.setText(R.string.GEN_Minutes);
					if(mRowId == -1){ //new record
						etTimeReminder.setText("30");
					}

				}
				else{
					tvTimeReminderUnitLbl.setText(R.string.GEN_Days);
					if(mRowId == -1){ //new record
						etTimeReminder.setText("3");
					}
				}
					
				if(mTimeFrequencyTypeId == StaticValues.TASK_TIMEFREQUENCYTYPE_MONTHLY
						&&!isDiffStartingTime){
					llLastMonthDay.setVisibility(View.VISIBLE);
				}
				else{
					llLastMonthDay.setVisibility(View.GONE);
				}
				if(isDiffStartingTime){
					tvFirstTimeRunExplanation.setVisibility(View.VISIBLE);
					llStartingTime.setVisibility(View.GONE);
				}
				else{
					tvFirstTimeRunExplanation.setVisibility(View.GONE);
					llStartingTime.setVisibility(View.VISIBLE);
					if(mTimeFrequencyTypeId == StaticValues.TASK_TIMEFREQUENCYTYPE_MONTHLY
							&& ckOnLastDay.isChecked()){
						spnLastDayMonth.setVisibility(View.VISIBLE);
						btnPickDate.setVisibility(View.GONE);
						tvOrLastDay.setVisibility(View.GONE);
					}
					else{
						spnLastDayMonth.setVisibility(View.GONE);
						btnPickDate.setVisibility(View.VISIBLE);
						tvOrLastDay.setVisibility(View.VISIBLE);
					}
				}
				if(isDiffStartingTime){
					llLinkedCarsZone.setVisibility(View.VISIBLE);
					llLinkedCarsHelp.setVisibility(View.VISIBLE);
					llLinkedCarsList.setVisibility(View.VISIBLE);
					isDeleteLinkedCarsOnSave = false;
				}
				else{
					llLinkedCarsZone.setVisibility(View.GONE);
					llLinkedCarsHelp.setVisibility(View.GONE);
					llLinkedCarsList.setVisibility(View.GONE);
					isDeleteLinkedCarsOnSave = true;
				}
			}
			else{
				tvFirstTimeRunExplanation.setVisibility(View.GONE);
				ckIsDifferentStartingTime.setVisibility(View.GONE);
				llRecurentTimeSettings.setVisibility(View.GONE);
				llStartingTime.setVisibility(View.GONE);
				llTimeReminder.setVisibility(View.GONE);
				llLastMonthDay.setVisibility(View.GONE);
			}
			
			if(isTimingEnabled && isMileageEnabled)
				tvOr.setVisibility(View.VISIBLE);
			else
				tvOr.setVisibility(View.GONE);
			
			if(isMileageEnabled){
				llMileageZone.setVisibility(View.VISIBLE);
				llLinkedCarsZone.setVisibility(View.VISIBLE);
				llLinkedCarsHelp.setVisibility(View.VISIBLE);
				llLinkedCarsList.setVisibility(View.VISIBLE);
				isDeleteLinkedCarsOnSave = false;
				tvFirstMileageRunExplanation.setVisibility(View.VISIBLE);
			}
			else{
				llMileageZone.setVisibility(View.GONE);
			}
		}
		else{ //one time
			llToDoCountZone.setVisibility(View.GONE);
			tvStartingTimeLbl.setText(R.string.GEN_On);
			tvFirstTimeRunExplanation.setVisibility(View.GONE);
			tvFirstMileageRunExplanation.setVisibility(View.GONE);
			llRecurentTimeSettings.setVisibility(View.GONE);
			llLastMonthDay.setVisibility(View.GONE);
			ckIsDifferentStartingTime.setVisibility(View.GONE);
			tvMileageLabelEvery.setVisibility(View.GONE);
			tvTimeReminderUnitLbl.setText(R.string.GEN_Days);
			spnLastDayMonth.setVisibility(View.GONE);

			if(isTimingEnabled){
				llStartingTime.setVisibility(View.VISIBLE);
				llTimeReminder.setVisibility(View.VISIBLE);
			}
			else{
				llStartingTime.setVisibility(View.GONE);
				llTimeReminder.setVisibility(View.GONE);
			}
			
			if(isTimingEnabled && isMileageEnabled)
				tvOr.setVisibility(View.VISIBLE);
			else
				tvOr.setVisibility(View.GONE);

			if(isMileageEnabled){
				llMileageZone.setVisibility(View.VISIBLE);
				llLinkedCarsZone.setVisibility(View.VISIBLE);
				llLinkedCarsHelp.setVisibility(View.VISIBLE);
				llLinkedCarsList.setVisibility(View.VISIBLE);
				isDeleteLinkedCarsOnSave = false;
			}
			else{
				llMileageZone.setVisibility(View.GONE);
				llLinkedCarsZone.setVisibility(View.GONE);
				llLinkedCarsHelp.setVisibility(View.GONE);
				llLinkedCarsList.setVisibility(View.GONE);
				isDeleteLinkedCarsOnSave = true;
			}
		}
	}
	
    protected AdapterView.OnItemLongClickListener mItemLongClickListener =
        new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
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
//    			isFinishAfterSave = false;
//    			saveData();
//    			isFinishAfterSave = true;
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
            					String[] deleteArgs = {Long.toString(mLongClickId)};
            					mDbAdapter.deleteRecords(MainDbAdapter.TODO_TABLE_NAME, 
            	                		MainDbAdapter.TODO_COL_ISDONE_NAME + " = 'N' " + " AND " + 
            	                		MainDbAdapter.TODO_COL_CAR_ID_NAME + " = " +
            	                					"(SELECT " + MainDbAdapter.TASK_CAR_COL_CAR_ID_NAME + 
            	                					" FROM " + MainDbAdapter.TASK_CAR_TABLE_NAME +
            	                					" WHERE " + MainDbAdapter.GEN_COL_ROWID_NAME + " = ? )",
            	                		deleteArgs);
                                int deleteResult = mDbAdapter.deleteRecord(MainDbAdapter.TASK_CAR_TABLE_NAME, mLongClickId);
                                if(deleteResult != -1) {
                                    madbErrorAlert.setMessage(mResource.getString(deleteResult));
                                    madError = madbErrorAlert.create();
                                    madError.show();
                                }
                                else {
                                	//also delete the existing todo's
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

	/* (non-Javadoc)
	 * @see org.andicar.activity.EditActivityBase#setDefaultValues()
	 */
	@Override
	public void setDefaultValues() {
	}
}
