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
import java.math.RoundingMode;

import org.andicar.persistence.MainDbAdapter;
import org.andicar.utils.StaticValues;
import org.andicar2.activity.R;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 *
 * @author miki
 */
public class CurrencyRateEditActivity extends EditActivityBase
{

    private EditText etCurrencyRate = null;
    private EditText etUserComment = null;
    private TextView tvInverseRateLabel = null;
    private TextView tvInverseRateToLabel = null;
    private TextView tvInverseRateValue = null;
    private TextView tvCurrencyRateLabel = null;
    private TextView tvCurrencyRateToLabel = null;
    private Spinner spnCurrencyFromSpinner = null;
    private Spinner spnCurrencyToSpinner = null;
    private CheckBox ckIsActive = null;
    private BigDecimal bdRate = null;
    private BigDecimal bdInverseRate = null;
    private String strCurrencyFromCode = null;
    private String strCurrencyToCode = null;
    private long lCurrencyFromId = 1;
    private long lCurrencyToId = 2;

    /** Called when the activity is first created. */
    @Override
    public void onCreate( Bundle icicle )
    {
        super.onCreate(icicle);

        String strOperationType = mBundleExtras.getString("Operation"); //E = edit, N = new

        spnCurrencyFromSpinner = (Spinner) findViewById( R.id.spnCurrencyFrom );
        spnCurrencyFromSpinner.setOnItemSelectedListener(spinnerCurrencyFromOnItemSelectedListener);
        spnCurrencyToSpinner = (Spinner) findViewById( R.id.spnCurrencyTo );
        spnCurrencyToSpinner.setOnItemSelectedListener(spinnerCurrencyToOnItemSelectedListener);
        etCurrencyRate = (EditText)findViewById(R.id.etCurrencyRate);
        etCurrencyRate.addTextChangedListener(textWatcher);
        tvInverseRateLabel = (TextView)findViewById(R.id.tvInverseRateLabel);
        tvInverseRateValue = (TextView)findViewById(R.id.tvInverseRateValue);
        tvInverseRateToLabel = (TextView)findViewById(R.id.tvInverseRateToLabel);
        tvCurrencyRateLabel = (TextView)findViewById(R.id.tvCurrencyRateLabel);
        tvCurrencyRateToLabel = (TextView)findViewById(R.id.tvCurrencyRateToLabel);
        etUserComment = (EditText)findViewById(R.id.etUserComment);
        ckIsActive = (CheckBox)findViewById(R.id.ckIsActive);
        
        if( strOperationType.equals( "E") ) {
            mRowId = mBundleExtras.getLong( MainDbAdapter.COL_NAME_GEN_ROWID );
            Cursor c = mDbAdapter.fetchRecord(MainDbAdapter.TABLE_NAME_CURRENCYRATE,
                    MainDbAdapter.COL_LIST_CURRENCYRATE_TABLE, mRowId);
            lCurrencyFromId = c.getLong( MainDbAdapter.COL_POS_CURRENCYRATE__FROMCURRENCY_ID );
            lCurrencyToId = c.getLong( MainDbAdapter.COL_POS_CURRENCYRATE__TOCURRENCY_ID );
            String strCurrencyRate = c.getString( MainDbAdapter.COL_POS_CURRENCYRATE__RATE );
            String strCurrencyIverseRate = c.getString( MainDbAdapter.COL_POS_CURRENCYRATE__INVERSERATE );
            String strIsActive = c.getString( MainDbAdapter.COL_POS_GEN_ISACTIVE );
            String strUserComment = c.getString( MainDbAdapter.COL_POS_GEN_USER_COMMENT );
            spnCurrencyFromSpinner.setEnabled(false);
            spnCurrencyToSpinner.setEnabled(false);

            if(strCurrencyRate != null){
              etCurrencyRate.setText(strCurrencyRate);
            }
            if(strCurrencyIverseRate != null){
              tvInverseRateValue.setText(strCurrencyIverseRate);
            }
            if( strIsActive != null ) {
                ckIsActive.setChecked( strIsActive.equals( "Y" ) );
            }
            if( strUserComment != null ) {
                etUserComment.setText( strUserComment );
            }
            c.close();
        }
        else {
            ckIsActive.setChecked( true );
            spnCurrencyFromSpinner.setEnabled(true);
            spnCurrencyToSpinner.setEnabled(true);
        }

        initSpinner(spnCurrencyFromSpinner, MainDbAdapter.TABLE_NAME_CURRENCY,
                MainDbAdapter.COL_LIST_GEN_ROWID_NAME, new String[]{MainDbAdapter.COL_NAME_GEN_NAME},
                MainDbAdapter.WHERE_CONDITION_ISACTIVE, null, MainDbAdapter.COL_NAME_GEN_NAME, lCurrencyFromId, false);

        initSpinner(spnCurrencyToSpinner, MainDbAdapter.TABLE_NAME_CURRENCY,
                MainDbAdapter.COL_LIST_GEN_ROWID_NAME, new String[]{MainDbAdapter.COL_NAME_GEN_NAME},
                MainDbAdapter.WHERE_CONDITION_ISACTIVE, null, MainDbAdapter.COL_NAME_GEN_NAME, lCurrencyToId, false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateLabel();
        calculateInverseRate();
    }

    private TextWatcher textWatcher =
        new TextWatcher() {

            public void beforeTextChanged(CharSequence cs, int i, int i1, int i2) {
                return;
            }

            public void onTextChanged(CharSequence cs, int i, int i1, int i2) {
                return;
            }

            public void afterTextChanged(Editable edtbl) {
                calculateInverseRate();
            }
        };

    private AdapterView.OnItemSelectedListener spinnerCurrencyFromOnItemSelectedListener =
            new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    setSpinnerTextToCode(arg0, arg3, arg1);
                    lCurrencyFromId = arg3;
                    updateLabel();
                }
                public void onNothingSelected(AdapterView<?> arg0) {
                }
            };
                
    private AdapterView.OnItemSelectedListener spinnerCurrencyToOnItemSelectedListener =
            new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    setSpinnerTextToCode(arg0, arg3, arg1);
                    lCurrencyToId = arg3;
                    updateLabel();
                }
                public void onNothingSelected(AdapterView<?> arg0) {
                }
            };


    private void updateLabel(){
    	Cursor c = mDbAdapter.fetchRecord(MainDbAdapter.TABLE_NAME_CURRENCY, MainDbAdapter.COL_LIST_CURRENCY_TABLE,
                lCurrencyFromId);
    	if(c != null){
    		strCurrencyFromCode = c.getString(MainDbAdapter.COL_POS_CURRENCY__CODE);
    		c.close();
    	}
    	if(strCurrencyFromCode == null)
    		strCurrencyFromCode = "";
    	
    	c = mDbAdapter.fetchRecord(MainDbAdapter.TABLE_NAME_CURRENCY, MainDbAdapter.COL_LIST_CURRENCY_TABLE,
                lCurrencyToId);
    	if(c != null){
    		strCurrencyToCode = c.getString(MainDbAdapter.COL_POS_CURRENCY__CODE);
    		c.close();
    	}
    	if(strCurrencyToCode == null)
    		strCurrencyToCode = "";
    	
        tvCurrencyRateLabel.setText(mResource.getString(R.string.CurrencyRateEditActivity_CurrencyRateLabel).
                replace("[#1]", strCurrencyFromCode));
        tvCurrencyRateToLabel.setText(strCurrencyToCode);

        tvInverseRateLabel.setText(
                mResource.getString(R.string.CurrencyRateEditActivity_InverseCurrencyRateLabel)
                    .replace("[#1]", strCurrencyToCode));
        tvInverseRateToLabel.setText(strCurrencyFromCode);
    }

    private void calculateInverseRate() {
        String strCurrencyRate = etCurrencyRate.getText().toString();
        if(strCurrencyRate != null && strCurrencyRate.length() > 0) {
            try{
                bdRate = new BigDecimal(strCurrencyRate);
                if(bdRate.signum() == 0)
                    bdInverseRate = bdRate;
                else
                    bdInverseRate = BigDecimal.ONE.divide(bdRate, 10, RoundingMode.HALF_UP)
                            .setScale(StaticValues.DECIMALS_RATES, StaticValues.ROUNDING_MODE_RATES);
                if(bdRate.compareTo(bdRate.setScale(StaticValues.DECIMALS_RATES, StaticValues.ROUNDING_MODE_RATES)) != 0){
                    bdRate = bdRate.setScale(StaticValues.DECIMALS_RATES, StaticValues.ROUNDING_MODE_RATES);
                    etCurrencyRate.setText("");
                    etCurrencyRate.append(bdRate.toString());
                    Toast toast = Toast.makeText( getApplicationContext(),
                            mResource.getString( R.string.CurrencyRateEditActivity_MaxDecimalsLabel ) +
                                StaticValues.DECIMALS_RATES, Toast.LENGTH_SHORT );
                    toast.show();
                }
                tvInverseRateValue.setText(bdInverseRate.toString());
            }
            catch(NumberFormatException e){}
        }
    }

    @Override
    protected boolean saveData() {

        ContentValues cvData = new ContentValues();
        
        String strCurrFromCode = null;
        Cursor c = mDbAdapter.fetchRecord(MainDbAdapter.TABLE_NAME_CURRENCY,
                MainDbAdapter.COL_LIST_CURRENCY_TABLE, spnCurrencyFromSpinner.getSelectedItemId());
        if(c != null){
        	strCurrFromCode = c.getString(MainDbAdapter.COL_POS_CURRENCY__CODE);
        	c.close();
        }

        String strCurrToCode = null;
        c = mDbAdapter.fetchRecord(MainDbAdapter.TABLE_NAME_CURRENCY,
              MainDbAdapter.COL_LIST_CURRENCY_TABLE, spnCurrencyToSpinner.getSelectedItemId());
        if(c != null){
        	strCurrToCode = c.getString(MainDbAdapter.COL_POS_CURRENCY__CODE);
        	c.close();
        }
        cvData.put( MainDbAdapter.COL_NAME_GEN_NAME,
                strCurrFromCode + " <-> " + strCurrToCode);
        cvData.put( MainDbAdapter.COL_NAME_GEN_ISACTIVE,
                (ckIsActive.isChecked() ? "Y" : "N") );
        cvData.put( MainDbAdapter.COL_NAME_GEN_USER_COMMENT,
                etUserComment.getText().toString() );
        cvData.put( MainDbAdapter.COL_NAME_CURRENCYRATE__FROMCURRENCY_ID,
                spnCurrencyFromSpinner.getSelectedItemId());
        cvData.put( MainDbAdapter.COL_NAME_CURRENCYRATE__TOCURRENCY_ID,
                spnCurrencyToSpinner.getSelectedItemId());
        cvData.put( MainDbAdapter.COL_NAME_CURRENCYRATE__RATE,
                bdRate.toString());
        cvData.put( MainDbAdapter.COL_NAME_CURRENCYRATE__INVERSERATE,
                bdInverseRate.toString());

        int dbRetVal = -1;
        String strErrMsg = null;
        if( mRowId == -1 ) {
        	dbRetVal = ((Long)mDbAdapter.createRecord(MainDbAdapter.TABLE_NAME_CURRENCYRATE, cvData)).intValue();
            if( dbRetVal < 0){
                if(dbRetVal == -1) //DB Error
                    madbErrorAlert.setMessage(mDbAdapter.lastErrorMessage);
                else //precondition error
                    madbErrorAlert.setMessage(mResource.getString(-1 * dbRetVal));
                madError = madbErrorAlert.create();
                madError.show();
                return false;
            }
            else{
            	finish();
                return true;
            }
        }
        else {
        	dbRetVal = mDbAdapter.updateRecord(MainDbAdapter.TABLE_NAME_CURRENCYRATE, mRowId, cvData);
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
   		setContentView(R.layout.currencyrate_edit_activity_s01);
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
