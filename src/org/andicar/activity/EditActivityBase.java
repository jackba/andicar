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
import org.andicar.utils.AndiCarStatistics;
import org.andicar.utils.StaticValues;
import org.andicar2.activity.R;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnKeyListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

/**
 * Base class for all edit activities. Implement common functionalities:
 * -onCreate: -aply the layout resource -initialise global resources: Bundle
 * mBundleExtras, Resources mResource, SharedPreferences mPreferences -serach
 * for btnCancel and if exists initialize the OnCLickListener -serach for btnOk
 * and if exists and initialize the OnCLickListener if it is provided to the
 * onCreate method
 * 
 * -implement common intialisations routins for: -spinners: see initSpinner *
 */
public abstract class EditActivityBase extends BaseActivity implements
		OnKeyListener {
	protected long mRowId = -1;
	protected Bundle mBundleExtras = null;
	protected AndiCarDialogBuilder mAndiCarDialogBuilder;
	protected ImageButton btnOk = null;
	protected ImageButton btnCancel = null;
	protected ImageButton btnPickDate = null;
	protected ImageButton btnPickTime = null;
	protected TimePickerDialog mTimePickerDialog = null;
	protected DatePickerDialog mDatePickerDialog = null;
	protected EditText etDocNo = null;
	protected AutoCompleteTextView acUserComment = null;
	protected AutoCompleteTextView acTag = null;
	protected AutoCompleteTextView acBPartner;
	protected AutoCompleteTextView acAdress;
	// protected ActionBar mActionBar = null;
	protected int mYear;
	protected int mMonth;
	protected int mDay;
	protected int mHour;
	protected int mMinute;
	protected long mlDateTimeInSeconds;
	protected TextView tvDateTimeValue;
	protected final Calendar mcalDateTime = Calendar.getInstance();
	protected boolean initTimeOnly = false;
	protected boolean initDateOnly = false;
	protected boolean isFinishAfterSave = true;
	protected boolean isUseTemplate = false;

	protected DataEntryTemplate mDet = null;

	abstract protected boolean saveData();

	abstract protected void setLayout();

	abstract public void setDefaultValues();

	@Override
	protected void onStart() {
		super.onStart();
		if (isSendStatistics)
			AndiCarStatistics.sendFlurryStartSession(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (isSendStatistics)
			AndiCarStatistics.sendFlurryEndSession(this);
	}

	/**
	 * Use instead onCreate(Bundle icicle, int layoutResID, View.OnClickListener
	 * btnOkClickListener)
	 */
	// @TargetApi(Build.VERSION_CODES.HONEYCOMB) //sdk = 11
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setLayout();

		mBundleExtras = getIntent().getExtras();

		btnCancel = (ImageButton) findViewById(android.R.id.closeButton);
		if (btnCancel != null)
			btnCancel.setOnClickListener(onCancelClickListener);

		btnOk = (ImageButton) findViewById(android.R.id.button1);
		if (onOkClickListener != null && btnOk != null)
			btnOk.setOnClickListener(onOkClickListener);

		vgRoot = (ViewGroup) findViewById(R.id.vgRoot);
		if (vgRoot != null)
			setInputType(vgRoot);
		btnPickTime = (ImageButton) findViewById(R.id.btnPickTime);
		btnPickDate = (ImageButton) findViewById(R.id.btnPickDate);

		if (isUseTemplate)
			mDet = new DataEntryTemplate(this, mDbAdapter);

		// if(android.os.Build.VERSION.SDK_INT >= 11 &&
		// (this instanceof ExpenseEditActivity ||
		// this instanceof MileageEditActivity ||
		// this instanceof GPSTrackEditActivity ||
		// this instanceof RefuelEditActivity)){
		// mActionBar = getActionBar();
		// mActionBar.setDisplayHomeAsUpEnabled(true);
		//
		// }
		// else
		// mActionBar = null;
	}

	// @Override
	// public boolean onOptionsItemSelected(MenuItem item) {
	// switch (item.getItemId()){
	// case android.R.id.home:
	// Intent intent = null;
	// if(this instanceof ExpenseEditActivity){
	// intent = new Intent(this, ExpensesListReportActivity.class);
	// }
	// else if(this instanceof MileageEditActivity){
	// intent = new Intent(this, MileageListReportActivity.class);
	// }
	// else if(this instanceof GPSTrackEditActivity){
	// intent = new Intent(this, GPSTrackListReportActivity.class);
	// }
	// else if(this instanceof RefuelEditActivity){
	// intent = new Intent(this, RefuelListReportActivity.class);
	// }
	// if(intent != null){
	// intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	// startActivity(intent);
	// }
	// break;
	// default:
	// break;
	// }
	//
	// return true;
	// }
	//
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mRowId = savedInstanceState.getLong("mRowId");
		mlDateTimeInSeconds = savedInstanceState.getLong("mlDateTimeInSeconds");
		mYear = savedInstanceState.getInt("mYear");
		mMonth = savedInstanceState.getInt("mMonth");
		mDay = savedInstanceState.getInt("mDay");
		mHour = savedInstanceState.getInt("mHour");
		mMinute = savedInstanceState.getInt("mMinute");
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong("mRowId", mRowId);
		outState.putLong("mlDateTimeInSeconds", mlDateTimeInSeconds);
		outState.putInt("mYear", mYear);
		outState.putInt("mMonth", mMonth);
		outState.putInt("mDay", mDay);
		outState.putInt("mHour", mHour);
		outState.putInt("mMinute", mMinute);
	}

	protected View.OnClickListener onCancelClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			finish();
		}
	};

	protected View.OnClickListener onOkClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			if (!beforeSave())
				return;
			if (!saveData())
				return;
			afterSave();
		}
	};

	/**
	 * Check mandatory fields. Mandatory fields are detected based on view hint
	 * (Required)
	 * 
	 * @return null or field tag if is empty
	 */
	protected String checkMandatory(ViewGroup vg) {
		View vwChild;
		EditText etChild;
		String strRetVal;
		if (vg == null)
			return null;

		for (int i = 0; i < vg.getChildCount(); i++) {
			vwChild = vg.getChildAt(i);
			if (vwChild instanceof ViewGroup) {
				strRetVal = checkMandatory((ViewGroup) vwChild);
				if (strRetVal != null)
					return strRetVal;
			} else if (vwChild instanceof EditText) {
				etChild = (EditText) vwChild;
				// if(etChild.getTag() != null &&
				// etChild.getTag().toString().length() > 0
				if (etChild.getHint() != null
						&& etChild
								.getHint()
								.toString()
								.equals(mResource
										.getString(R.string.GEN_Required))
						&& etChild.isShown()
						&& (etChild.getText().toString() == null || etChild
								.getText().toString().length() == 0)) {
					return etChild.getTag().toString().replace(":", "");
				}
			}
		}
		return null;
	}

	protected void setEditable(ViewGroup vg, boolean editable) {
		View vwChild;
		for (int i = 0; i < vg.getChildCount(); i++) {
			vwChild = vg.getChildAt(i);
			if (vwChild instanceof ViewGroup) {
				setEditable((ViewGroup) vwChild, editable);
			}
			if (vwChild.getId() != android.R.id.closeButton)
				vwChild.setEnabled(editable);
		}
	}

	protected void initDateTime(long dateTimeInMiliseconds) {

		mlDateTimeInSeconds = dateTimeInMiliseconds / 1000;
		mcalDateTime.setTimeInMillis(dateTimeInMiliseconds);
		mYear = mcalDateTime.get(Calendar.YEAR);
		mMonth = mcalDateTime.get(Calendar.MONTH);
		mDay = mcalDateTime.get(Calendar.DAY_OF_MONTH);
		mHour = mcalDateTime.get(Calendar.HOUR_OF_DAY);
		mMinute = mcalDateTime.get(Calendar.MINUTE);

		tvDateTimeValue = (TextView) findViewById(R.id.tvDateTimeValue);
		// if(dateTimeInMiliseconds > 0){
		if (initTimeOnly)
			updateTime();
		else if (initDateOnly)
			updateDate();
		else
			updateDateTime();
		// }

		if (btnPickDate != null)
			btnPickDate.setOnClickListener(new View.OnClickListener() {
				public void onClick(View arg0) {
					showDialog(StaticValues.DIALOG_DATE_PICKER);
				}
			});

		if (btnPickTime != null)
			btnPickTime.setOnClickListener(new View.OnClickListener() {
				public void onClick(View arg0) {
					showDialog(StaticValues.DIALOG_TIME_PICKER);
				}
			});
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case StaticValues.DIALOG_TIME_PICKER:
			mTimePickerDialog = new TimePickerDialog(this, onTimeSetListener,
					mHour, mMinute, false);
			return mTimePickerDialog;
		case StaticValues.DIALOG_DATE_PICKER:
			mDatePickerDialog = new DatePickerDialog(this, onDateSetListener,
					mYear, mMonth, mDay);
			return mDatePickerDialog;
		case StaticValues.DIALOG_NEW_TEMPLATE:
			LayoutInflater liLayoutFactory = LayoutInflater.from(this);
			mDialog = liLayoutFactory.inflate(R.layout.dialog_name, null);
			etName = (EditText) mDialog.findViewById(R.id.etName);
			mAndiCarDialogBuilder = new AndiCarDialogBuilder(this,
					AndiCarDialogBuilder.DIALOGTYPE_QUESTION,
					mResource.getString(R.string.DIALOG_TemplateNameTitle));
			mAndiCarDialogBuilder.setView(mDialog);
			mAndiCarDialogBuilder.setPositiveButton(R.string.GEN_OK,
					mNewTemplateDialogButtonlistener);
			return mAndiCarDialogBuilder.create();
		}
		return null;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
		case StaticValues.DIALOG_TIME_PICKER:
			if (mTimePickerDialog != null)
				mTimePickerDialog.updateTime(mHour, mMinute);
			break;
		case StaticValues.DIALOG_DATE_PICKER:
			if (mDatePickerDialog != null)
				mDatePickerDialog.updateDate(mYear, mMonth, mDay);
			break;
		}
	}

	private DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			mYear = year;
			mMonth = monthOfYear;
			mDay = dayOfMonth;
			if (initTimeOnly)
				updateTime();
			else if (initDateOnly)
				updateDate();
			else
				updateDateTime();
		}
	};

	private TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			mHour = hourOfDay;
			mMinute = minute;
			if (initTimeOnly)
				updateTime();
			else if (initDateOnly)
				updateDate();
			else
				updateDateTime();
		}
	};

	private DialogInterface.OnClickListener mNewTemplateDialogButtonlistener = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {

			if (!isUseTemplate || mDet == null)
				return;

			if (whichButton == DialogInterface.BUTTON_POSITIVE) {
				ContentValues cv = new ContentValues();
				cv.put("Name", etName.getText().toString());
				cv.put("Comment", etName.getText().toString());
				int dbRetVal = ((Long) mDet.saveTemplate(-1, cv)).intValue();
				String strErrMsg = null;
				if (dbRetVal < 0) {
					if (dbRetVal == -1) // DB Error
						strErrMsg = mDbAdapter.lastErrorMessage;
					else
						// precondition error
						strErrMsg = mResource.getString(-1 * dbRetVal);
					madbErrorAlert.setMessage(strErrMsg);
					madError = madbErrorAlert.create();
					madError.show();
				} else
					mDet.updateTemplateList(dbRetVal);

			}
		}
	};

	private void updateDateTime() {
		mcalDateTime.set(mYear, mMonth, mDay, mHour, mMinute, 0);
		mlDateTimeInSeconds = mcalDateTime.getTimeInMillis() / 1000;
		tvDateTimeValue.setText(DateFormat.getDateFormat(
				getApplicationContext()).format(mcalDateTime.getTime())
				+ " "
				+ DateFormat.getTimeFormat(getApplicationContext()).format(
						mcalDateTime.getTime()));
	}

	private void updateDate() {
		mHour = 0;
		mMinute = 0;
		mcalDateTime.set(mYear, mMonth, mDay, 0, 0, 0);
		mlDateTimeInSeconds = mcalDateTime.getTimeInMillis() / 1000;
		tvDateTimeValue.setText(DateFormat.getDateFormat(
				getApplicationContext()).format(mcalDateTime.getTime())
				+ " "
				+ DateFormat.getTimeFormat(getApplicationContext()).format(
						mcalDateTime.getTime()));
	}

	private void updateTime() {
		mcalDateTime.set(1970, Calendar.JANUARY, 1, mHour, mMinute, 0);
		mlDateTimeInSeconds = mcalDateTime.getTimeInMillis() / 1000;
		tvDateTimeValue.setText(DateFormat.getTimeFormat(
				getApplicationContext()).format(mcalDateTime.getTime()));
		// new StringBuilder() // Month is 0 based so add 1
		// .append(Utils.pad(mHour, 2)).append(":").append(Utils.pad(mMinute,
		// 2)));
	}

	/**
	 * called before saving data
	 * 
	 * @return
	 */
	protected boolean beforeSave() {
		if (this instanceof SecureBackupConfig
				&& !((CheckBox) findViewById(R.id.ckIsActive)).isChecked()) // inactive
																			// configuration
																			// =>
																			// can
																			// be
																			// saved
																			// without
																			// smtp
																			// auth.
																			// info
			return true;

		String strRetVal = checkMandatory(vgRoot);
		if (strRetVal != null) {
			Toast toast = Toast.makeText(getApplicationContext(),
					mResource.getString(R.string.GEN_FillMandatory) + ": "
							+ strRetVal, Toast.LENGTH_SHORT);
			toast.show();
			return false;
		}

		strRetVal = checkNumeric(vgRoot, false);
		if (strRetVal != null) {
			Toast toast = Toast.makeText(getApplicationContext(),
					mResource.getString(R.string.GEN_NumberFormatException)
							+ ": " + strRetVal, Toast.LENGTH_SHORT);
			toast.show();
			return false;
		}

		return true;
	}

	/**
	 * called after data saved
	 * 
	 * @return
	 */
	protected boolean afterSave() {
		if (isFinishAfterSave)
			finish();
		return true;
	}

	public void setBackgroundSettingsActive(boolean value) {
		isBackgroundSettingsActive = value;
	}

	public boolean isBackgroundSettingsActive() {
		return isBackgroundSettingsActive;
	}

	public View.OnTouchListener spinnerOnTouchListener = new View.OnTouchListener() {

		public boolean onTouch(View view, MotionEvent me) {
			isBackgroundSettingsActive = false;
			return false;
		}
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onKeyUp(int, android.view.KeyEvent)
	 */
	// @Override
	// public boolean onKeyUp(int keyCode, KeyEvent event) {
	// if (keyCode == KeyEvent.KEYCODE_ENTER) {
	//
	// if((event.getFlags() & KeyEvent.FLAG_EDITOR_ACTION ) !=
	// KeyEvent.FLAG_EDITOR_ACTION )
	// //enter key pressed instead of Next/Done
	// return super.onKeyUp(keyCode, event);
	//
	@Override
	public boolean onKey(View view, int keyCode, KeyEvent event) {
		if (event.getAction() != KeyEvent.ACTION_UP)
			return false;
		
		if (keyCode == KeyEvent.KEYCODE_ENTER) {
			if ((event.getFlags() & KeyEvent.FLAG_EDITOR_ACTION) != KeyEvent.FLAG_EDITOR_ACTION)
				// enter key pressed instead of Next/Done
				return false;

			if (acBPartner != null && acBPartner.hasFocus()) {
				if (acBPartner.getText().length() > 0) {
					if (acAdress != null) {
						acAdress.requestFocus();
						return true;
					} else if (etDocNo != null) {
						etDocNo.requestFocus();
						return true;
					} else if (acTag != null) {
						acTag.requestFocus();
						return true;
					} else if (acUserComment != null) {
						acUserComment.requestFocus();
						return true;
					}
				} else {
					if (etDocNo != null) {
						etDocNo.requestFocus();
						return true;
					}
					if (acTag != null) {
						acTag.requestFocus();
						return true;
					} else if (acUserComment != null) {
						acUserComment.requestFocus();
						return true;
					}
				}
			} else if (acAdress != null && acAdress.hasFocus()) {
				if (etDocNo != null) {
					etDocNo.requestFocus();
					return true;
				} else if (acTag != null) {
					acTag.requestFocus();
					return true;
				} else if (acUserComment != null) {
					acUserComment.requestFocus();
					return true;
				}
			} else if (etDocNo != null && etDocNo.hasFocus()) {
				if (acTag != null) {
					acTag.requestFocus();
					return true;
				} else if (acUserComment != null) {
					acUserComment.requestFocus();
					return true;
				}
			} else if (acTag != null && acTag.hasFocus()) {
				if (acUserComment != null) {
					acUserComment.requestFocus();
					return true;
				}
			} else if (acUserComment != null && acUserComment.hasFocus()) {
				// closes soft keyboard (user pressed "Done")
				InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				inputManager.hideSoftInputFromWindow(
						acUserComment.getWindowToken(),
						InputMethodManager.HIDE_NOT_ALWAYS);
				return true;
			}
		}
		return false; // super.onKeyUp(keyCode, event);
	}

}
