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

import java.math.BigDecimal;
import java.util.Calendar;

import org.andicar.persistence.MainDbAdapter;
import org.andicar.utils.StaticValues;
import org.andicar.utils.Utils;
import org.andicar2.activity.R;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

/**
 *
 * @author miki
 */
public class ReimbursementRateEditActivity extends EditActivityBase
{
    private EditText etRate = null;
    private TextView tvRateUOM = null;
    private TextView tvValidFromValue = null;
    private TextView tvValidToValue = null;
    private ImageButton btnPickDateValidFrom = null;
    private ImageButton btnPickDateValidTo = null;
    
    private static final int DATE_FROM_DIALOG = 1;
    private static final int DATE_TO_DIALOG = 2;
    private Calendar calValidFrom = Calendar.getInstance();
    private Calendar calValidTo = Calendar.getInstance();
    

    /** Called when the activity is first created. */
    @Override
    public void onCreate( Bundle icicle )
    {
        super.onCreate(icicle);
        calValidFrom.set(calValidFrom.get(Calendar.YEAR), 0, 1);
        calValidTo.set(calValidFrom.get(Calendar.YEAR), 11, 31);

        String operation = mBundleExtras.getString("Operation"); //E = edit, N = new
        init();
        if( operation.equals( "E") ) {
            mRowId = mBundleExtras.getLong( MainDbAdapter.COL_NAME_GEN_ROWID );
            Cursor c = mDbAdapter.fetchRecord(MainDbAdapter.TABLE_NAME_REIMBURSEMENT_CAR_RATES,
                    MainDbAdapter.COL_LIST_REIMBURSEMENT_CAR_RATES_TABLE, mRowId);
            mExpTypeId = c.getLong(MainDbAdapter.COL_POS_REIMBURSEMENT_CAR_RATES__EXPENSETYPE_ID);
            mCarId = c.getLong(MainDbAdapter.COL_POS_REIMBURSEMENT_CAR_RATES__CAR_ID);
            etRate.setText(Utils.numberToString(
            		(new BigDecimal(c.getDouble(MainDbAdapter.COL_POS_REIMBURSEMENT_CAR_RATES__RATE)).stripTrailingZeros()), 
            		false, StaticValues.DECIMALS_RATES, StaticValues.ROUNDING_MODE_RATES));
            calValidFrom.setTimeInMillis(c.getLong(MainDbAdapter.COL_POS_REIMBURSEMENT_CAR_RATES__VALIDFROM) * 1000);
            calValidTo.setTimeInMillis(c.getLong(MainDbAdapter.COL_POS_REIMBURSEMENT_CAR_RATES__VALIDTO) * 1000);

            tvValidFromValue.setText(DateFormat.getDateFormat(
    				getApplicationContext()).format(calValidFrom.getTime()));
            tvValidToValue.setText(DateFormat.getDateFormat(
    				getApplicationContext()).format(calValidTo.getTime()));

            c.close();
        }
        else {
        	if(mBundleExtras.getLong("expenseTypeId") != 0L)
        		mExpTypeId = mBundleExtras.getLong("expenseTypeId");
        }
        initControls();
    }

    @Override
    protected boolean saveData() {
        ContentValues data = new ContentValues();
        data.put( MainDbAdapter.COL_NAME_GEN_NAME,"");
        data.put( MainDbAdapter.COL_NAME_REIMBURSEMENT_CAR_RATES__EXPENSETYPE_ID, mExpTypeId);
        data.put( MainDbAdapter.COL_NAME_REIMBURSEMENT_CAR_RATES__CAR_ID, mCarId);
        calValidFrom.set(Calendar.HOUR_OF_DAY, 0);
        calValidFrom.set(Calendar.MINUTE, 0);
        calValidFrom.set(Calendar.SECOND, 0);
        calValidFrom.set(Calendar.MILLISECOND, 0);
        calValidTo.set(Calendar.HOUR_OF_DAY, 23);
        calValidTo.set(Calendar.MINUTE, 59);
        calValidTo.set(Calendar.SECOND, 59);
        calValidTo.set(Calendar.MILLISECOND, 999);

        data.put( MainDbAdapter.COL_NAME_REIMBURSEMENT_CAR_RATES__VALIDFROM, calValidFrom.getTimeInMillis() / 1000);
        data.put( MainDbAdapter.COL_NAME_REIMBURSEMENT_CAR_RATES__VALIDTO, calValidTo.getTimeInMillis() / 1000);
        data.put( MainDbAdapter.COL_NAME_REIMBURSEMENT_CAR_RATES__RATE, etRate.getText().toString());

        int dbRetVal = -1;
        String strErrMsg = null;
        if( mRowId == -1 ) {
        	dbRetVal = ((Long)mDbAdapter.createRecord(MainDbAdapter.TABLE_NAME_REIMBURSEMENT_CAR_RATES, data)).intValue();
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
            int updResult = mDbAdapter.updateRecord(MainDbAdapter.TABLE_NAME_REIMBURSEMENT_CAR_RATES, mRowId, data);
            if(updResult != -1){
                String errMsg = "";
                errMsg = mResource.getString(updResult);
                if(updResult == R.string.ERR_000)
                    errMsg = errMsg + "\n" + mDbAdapter.lastErrorMessage;
                madbErrorAlert.setMessage(errMsg);
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
   		setContentView(R.layout.reimbursementrates_edit_activity);
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
	
    private void init() {
    	etRate = (EditText) findViewById(R.id.etRate);
    	tvRateUOM = (TextView) findViewById(R.id.tvRateUOM);
        tvValidFromValue = (TextView) findViewById(R.id.tvValidFromValue);
        tvValidFromValue.setText(DateFormat.getDateFormat(
				getApplicationContext()).format(calValidFrom.getTime()));

        tvValidToValue = (TextView) findViewById(R.id.tvValidToValue);
        tvValidToValue.setText(DateFormat.getDateFormat(
				getApplicationContext()).format(calValidTo.getTime()));

        btnPickDateValidFrom = (ImageButton) findViewById(R.id.btnPickDateValidFrom);
    	btnPickDateValidFrom.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(DATE_FROM_DIALOG);			}
		});
    	btnPickDateValidTo = (ImageButton) findViewById(R.id.btnPickDateValidTo);
    	btnPickDateValidTo.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(DATE_TO_DIALOG);			}
		});
        spnExpType = (Spinner) findViewById(R.id.spnExpType);
        spnExpType.setOnItemSelectedListener(spinnerExpTypeOnItemSelectedListener);
        spnExpType.setOnTouchListener(spinnerOnTouchListener);
        spnCar = (Spinner) findViewById(R.id.spnCar);
        spnCar.setOnItemSelectedListener(spinnerCarOnItemSelectedListener);
        spnCar.setOnTouchListener(spinnerOnTouchListener);
    }	

    private void initControls() {
        initSpinner(spnCar, MainDbAdapter.TABLE_NAME_CAR, MainDbAdapter.COL_LIST_GEN_ROWID_NAME,
                new String[]{MainDbAdapter.COL_NAME_GEN_NAME}, MainDbAdapter.WHERE_CONDITION_ISACTIVE, null,
                MainDbAdapter.COL_NAME_GEN_NAME,
                mCarId, false);
        initSpinner(spnExpType, MainDbAdapter.TABLE_NAME_EXPENSETYPE,
                MainDbAdapter.COL_LIST_GEN_ROWID_NAME, new String[]{MainDbAdapter.COL_NAME_GEN_NAME},
                MainDbAdapter.WHERE_CONDITION_ISACTIVE, null, MainDbAdapter.COL_NAME_GEN_NAME,
                mExpTypeId, false);
    }

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
			case DATE_FROM_DIALOG:
				if (mDatePickerDialog != null)
					mDatePickerDialog.updateDate(
							calValidFrom.get(Calendar.YEAR), calValidFrom.get(Calendar.MONTH), calValidFrom.get(Calendar.DAY_OF_MONTH));
				break;
			case DATE_TO_DIALOG:
				if (mDatePickerDialog != null)
					mDatePickerDialog.updateDate(
							calValidTo.get(Calendar.YEAR), calValidTo.get(Calendar.MONTH), calValidTo.get(Calendar.DAY_OF_MONTH));
				break;
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case DATE_FROM_DIALOG:
				mDatePickerDialog = new DatePickerDialog(this, onDateFromSetListener,
						calValidFrom.get(Calendar.YEAR), calValidFrom.get(Calendar.MONTH), calValidFrom.get(Calendar.DAY_OF_MONTH));
				return mDatePickerDialog;
			case DATE_TO_DIALOG:
				mDatePickerDialog = new DatePickerDialog(this, onDateToSetListener,
						calValidTo.get(Calendar.YEAR), calValidTo.get(Calendar.MONTH), calValidTo.get(Calendar.DAY_OF_MONTH));
				return mDatePickerDialog;
			}
		return null;
	}
	private DatePickerDialog.OnDateSetListener onDateFromSetListener = new DatePickerDialog.OnDateSetListener() {
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			calValidFrom.set(year, monthOfYear, dayOfMonth);
	        tvValidFromValue.setText(DateFormat.getDateFormat(
					getApplicationContext()).format(calValidFrom.getTime()));
		}
	};

	private DatePickerDialog.OnDateSetListener onDateToSetListener = new DatePickerDialog.OnDateSetListener() {
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			calValidTo.set(year, monthOfYear, dayOfMonth);
	        tvValidToValue.setText(DateFormat.getDateFormat(
					getApplicationContext()).format(calValidTo.getTime()));
		}
	};

	private AdapterView.OnItemSelectedListener spinnerCarOnItemSelectedListener =
            new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    if(isBackgroundSettingsActive)
                        return;
                    
                    mCarId = arg3;
                    
                    //change the currency
                    long newCarCurrencyId = mDbAdapter.getCarCurrencyID(mCarId);
                    long newCarUOMDistanceId = mDbAdapter.getCarUOMLengthID(mCarId);
                    tvRateUOM.setText(
                    		mDbAdapter.getCurrencyCode(newCarCurrencyId) + "/" + mDbAdapter.getUOMCode(newCarUOMDistanceId));
                }
                public void onNothingSelected(AdapterView<?> arg0) {
                }
    };
    
    private AdapterView.OnItemSelectedListener spinnerExpTypeOnItemSelectedListener =
		    new AdapterView.OnItemSelectedListener() {
		        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		            if(isBackgroundSettingsActive)
		                return;
		            setExpTypeId(arg3);
		        }
		        public void onNothingSelected(AdapterView<?> arg0) {
		        }
    };
}
