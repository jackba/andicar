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

import org.andicar.persistence.MainDbAdapter;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.CheckBox;
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
	private CheckBox ckIsActive = null;
	private Spinner spnTaskType = null;
	private ImageButton btnNewTaskType = null;
	private RadioButton rbOneTime;
	private RadioButton rbRecurent;
	private LinearLayout llOneTimeSettings;
	private LinearLayout llRecurentSettings;

	private Calendar mcalDateTime2 = Calendar.getInstance();
	private int mHour2;
	private int mMinute2;
	private TextView tvDateTimeValue2;

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
		rbOneTime = (RadioButton) findViewById(R.id.rbOneTime);
		rbRecurent = (RadioButton) findViewById(R.id.rbRecurent);
		llOneTimeSettings = (LinearLayout) findViewById(R.id.llOneTimeSettings);
		llRecurentSettings = (LinearLayout) findViewById(R.id.llRecurentSettings);

		RadioGroup rg = (RadioGroup) findViewById(R.id.rgRepeating);
		rg.setOnCheckedChangeListener(rgRepeatingOnCheckedChangeListener);

		String operation = mBundleExtras.getString("Operation"); // E = edit, N
																	// = new

		if (operation.equals("E")) {
			mRowId = mBundleExtras.getLong(MainDbAdapter.GEN_COL_ROWID_NAME);
			Cursor c = mDbAdapter.fetchRecord(MainDbAdapter.TASK_TABLE_NAME,
					MainDbAdapter.tagTableColNames, mRowId);
			String name = c.getString(MainDbAdapter.GEN_COL_NAME_POS);
			String isActive = c.getString(MainDbAdapter.GEN_COL_ISACTIVE_POS);
			String userComment = c
					.getString(MainDbAdapter.GEN_COL_USER_COMMENT_POS);
			Long lTaskTypeId = c
					.getLong(MainDbAdapter.TASK_COL_TASKTYPE_ID_POS);

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
			llRecurentSettings.setVisibility(View.VISIBLE);
			initDateOnly = true;
			initDateTime(System.currentTimeMillis());
			initDateOnly = false;
			initTime2(0);
		}

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
				llOneTimeSettings.setVisibility(View.VISIBLE);
				llRecurentSettings.setVisibility(View.GONE);
			} else {
				llOneTimeSettings.setVisibility(View.GONE);
				llRecurentSettings.setVisibility(View.VISIBLE);
			}
		}
	};

	protected void initTime2(long dateTimeInMiliseconds) {
		mcalDateTime2.setTimeInMillis(dateTimeInMiliseconds);
		mHour2 = mcalDateTime2.get(Calendar.HOUR_OF_DAY);
		mMinute2 = mcalDateTime2.get(Calendar.MINUTE);

		tvDateTimeValue2 = (TextView) findViewById(R.id.tvDateTimeValue2);
		if (dateTimeInMiliseconds > 0) {
			updateTime2();
		}

		ImageButton btnPickTime2 = (ImageButton) findViewById(R.id.btnPickTime2);
		if (btnPickTime2 != null)
			btnPickTime2.setOnClickListener(new View.OnClickListener() {
				public void onClick(View arg0) {
					showDialog(3);
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

	@Override
	protected Dialog onCreateDialog(int id) {
		if (id < 3)
			return super.onCreateDialog(id);
		else
			return new TimePickerDialog(this, onTime2SetListener, mHour2, mMinute2, false);
	}

//	@Override
//	protected void onPrepareDialog(int id, Dialog dialog) {
//		if (id < 3)
//			super.onPrepareDialog(id, dialog);
//		else
//			((TimePickerDialog) dialog).updateTime(mHour2, mMinute2);
//	}

	private void updateTime2() {
		mcalDateTime2.set(2000, Calendar.JANUARY, 1, mHour2, mMinute2, 0);
		// mlDateTimeInSeconds2 = mcalDateTime2.getTimeInMillis() / 1000;
		tvDateTimeValue2.setText(DateFormat.getTimeFormat(
				getApplicationContext()).format(mcalDateTime2.getTime()));
	}
}
