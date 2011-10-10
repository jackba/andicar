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
            mRowId = mBundleExtras.getLong( MainDbAdapter.GEN_COL_ROWID_NAME );
            Cursor c = mDbAdapter.fetchRecord(MainDbAdapter.CURRENCYRATE_TABLE_NAME,
                    MainDbAdapter.currencyRateTableColNames, mRowId);
            lCurrencyFromId = c.getLong( MainDbAdapter.CURRENCYRATE_COL_FROMCURRENCY_ID_POS );
            lCurrencyToId = c.getLong( MainDbAdapter.CURRENCYRATE_COL_TOCURRENCY_ID_POS );
            String strCurrencyRate = c.getString( MainDbAdapter.CURRENCYRATE_COL_RATE_POS );
            String strCurrencyIverseRate = c.getString( MainDbAdapter.CURRENCYRATE_COL_INVERSERATE_POS );
            String strIsActive = c.getString( MainDbAdapter.GEN_COL_ISACTIVE_POS );
            String strUserComment = c.getString( MainDbAdapter.GEN_COL_USER_COMMENT_POS );
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

        initSpinner(spnCurrencyFromSpinner, MainDbAdapter.CURRENCY_TABLE_NAME,
                MainDbAdapter.genColName, new String[]{MainDbAdapter.GEN_COL_NAME_NAME},
                MainDbAdapter.isActiveCondition, null, MainDbAdapter.GEN_COL_NAME_NAME, lCurrencyFromId, false);

        initSpinner(spnCurrencyToSpinner, MainDbAdapter.CURRENCY_TABLE_NAME,
                MainDbAdapter.genColName, new String[]{MainDbAdapter.GEN_COL_NAME_NAME},
                MainDbAdapter.isActiveCondition, null, MainDbAdapter.GEN_COL_NAME_NAME, lCurrencyToId, false);
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
    	Cursor c = mDbAdapter.fetchRecord(MainDbAdapter.CURRENCY_TABLE_NAME, MainDbAdapter.currencyTableColNames,
                lCurrencyFromId);
    	if(c != null){
    		strCurrencyFromCode = c.getString(MainDbAdapter.CURRENCY_COL_CODE_POS);
    		c.close();
    	}
    	if(strCurrencyFromCode == null)
    		strCurrencyFromCode = "";
    	
    	c = mDbAdapter.fetchRecord(MainDbAdapter.CURRENCY_TABLE_NAME, MainDbAdapter.currencyTableColNames,
                lCurrencyToId);
    	if(c != null){
    		strCurrencyToCode = c.getString(MainDbAdapter.CURRENCY_COL_CODE_POS);
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
                            .setScale(StaticValues.DECIMALS_CONVERSIONS, StaticValues.ROUNDING_MODE_CONVERSIONS);
                if(bdRate.compareTo(bdRate.setScale(StaticValues.DECIMALS_CONVERSIONS, StaticValues.ROUNDING_MODE_CONVERSIONS)) != 0){
                    bdRate = bdRate.setScale(StaticValues.DECIMALS_CONVERSIONS, StaticValues.ROUNDING_MODE_CONVERSIONS);
                    etCurrencyRate.setText("");
                    etCurrencyRate.append(bdRate.toString());
                    Toast toast = Toast.makeText( getApplicationContext(),
                            mResource.getString( R.string.CurrencyRateEditActivity_MaxDecimalsLabel ) +
                                StaticValues.DECIMALS_CONVERSIONS, Toast.LENGTH_SHORT );
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
        Cursor c = mDbAdapter.fetchRecord(MainDbAdapter.CURRENCY_TABLE_NAME,
                MainDbAdapter.currencyTableColNames, spnCurrencyFromSpinner.getSelectedItemId());
        if(c != null){
        	strCurrFromCode = c.getString(MainDbAdapter.CURRENCY_COL_CODE_POS);
        	c.close();
        }

        String strCurrToCode = null;
        c = mDbAdapter.fetchRecord(MainDbAdapter.CURRENCY_TABLE_NAME,
              MainDbAdapter.currencyTableColNames, spnCurrencyToSpinner.getSelectedItemId());
        if(c != null){
        	strCurrToCode = c.getString(MainDbAdapter.CURRENCY_COL_CODE_POS);
        	c.close();
        }
        cvData.put( MainDbAdapter.GEN_COL_NAME_NAME,
                strCurrFromCode + " <-> " + strCurrToCode);
        cvData.put( MainDbAdapter.GEN_COL_ISACTIVE_NAME,
                (ckIsActive.isChecked() ? "Y" : "N") );
        cvData.put( MainDbAdapter.GEN_COL_USER_COMMENT_NAME,
                etUserComment.getText().toString() );
        cvData.put( MainDbAdapter.CURRENCYRATE_COL_FROMCURRENCY_ID_NAME,
                spnCurrencyFromSpinner.getSelectedItemId());
        cvData.put( MainDbAdapter.CURRENCYRATE_COL_TOCURRENCY_ID_NAME,
                spnCurrencyToSpinner.getSelectedItemId());
        cvData.put( MainDbAdapter.CURRENCYRATE_COL_RATE_NAME,
                bdRate.toString());
        cvData.put( MainDbAdapter.CURRENCYRATE_COL_INVERSERATE_NAME,
                bdInverseRate.toString());

        int dbRetVal = -1;
        String strErrMsg = null;
        if( mRowId == -1 ) {
        	dbRetVal = ((Long)mDbAdapter.createRecord(MainDbAdapter.CURRENCYRATE_TABLE_NAME, cvData)).intValue();
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
        	dbRetVal = mDbAdapter.updateRecord(MainDbAdapter.CURRENCYRATE_TABLE_NAME, mRowId, cvData);
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
    	if(mPreferences.getString("UIStyle", "s01").equalsIgnoreCase("s00"))
    		setContentView(R.layout.currencyrate_edit_activity_s00);
    	else if(mPreferences.getString("UIStyle", "s01").equalsIgnoreCase("s01"))
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
