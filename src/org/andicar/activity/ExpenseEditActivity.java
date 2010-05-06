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

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.Toast;
import org.andicar.persistence.MainDbAdapter;
import org.andicar.utils.StaticValues;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.math.BigDecimal;
import org.andicar.utils.AndiCarStatistics;

/**
 *
 * @author miki
 */
public class ExpenseEditActivity extends EditActivityBase {
    private AutoCompleteTextView acUserComment;
    private Spinner spnCar;
    private Spinner spnDriver;
    private Spinner spnCurrency;
    private Spinner spnExpType;
    private Spinner spnExpCategory;
    private EditText etCarIndex;
    private EditText atAmount;
    private EditText etDocNo;
    private EditText etConversionRate;
    private TextView tvWarningLabel;
    private TextView tbConvertedAmountValue;
    private long mCurrencyId;
    private long carDefaultCurrencyId;
    private String carDefaultCurrencyCode;
//    String currencyCode;
    private BigDecimal conversionRate;
    private BigDecimal convertedAmount = null;
    private LinearLayout llConversionRateZone;
    private boolean isActivityOnLoading = true;

    private ArrayAdapter<String> userCommentAdapter;

    private String operationType;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle, R.layout.expense_edit_activity, mOkClickListener);

        operationType = mBundleExtras.getString("Operation");
        acUserComment = ((AutoCompleteTextView) findViewById( R.id.acUserComment ));

        spnCar = (Spinner)findViewById(R.id.spnCar);
        spnDriver = (Spinner)findViewById(R.id.spnDriver);
        spnCar.setOnItemSelectedListener(spinnerCarDriverOnItemSelectedListener);
        spnDriver.setOnItemSelectedListener(spinnerCarDriverOnItemSelectedListener);
        spnCar.setOnTouchListener(spinnerOnTouchListener);
        spnDriver.setOnTouchListener(spinnerOnTouchListener);

        spnCurrency = (Spinner)findViewById(R.id.spnCurrency);
        spnCurrency.setOnItemSelectedListener(spinnerCurrencyOnItemSelectedListener);
        spnCurrency.setOnTouchListener(spinnerOnTouchListener);
        spnExpType = (Spinner)findViewById(R.id.spnExpType);
        spnExpCategory = (Spinner)findViewById(R.id.spnExpCategory);

        userCommentAdapter = new ArrayAdapter<String>(ExpenseEditActivity.this,
                android.R.layout.simple_dropdown_item_1line,
                mDbAdapter.getAutoCompleteUserComments(MainDbAdapter.EXPENSES_TABLE_NAME,
                mPreferences.getLong("CurrentCar_ID", -1), 30));
        acUserComment.setAdapter(userCommentAdapter);

        etCarIndex = (EditText)findViewById(R.id.etIndex);
        atAmount = (EditText)findViewById(R.id.etAmount);
        atAmount.addTextChangedListener(textWatcher);
        
        etDocNo = (EditText)findViewById(R.id.etDocumentNo);

        tvWarningLabel = (TextView)findViewById(R.id.tvWarningLabel);

        llConversionRateZone = (LinearLayout)findViewById(R.id.llConversionRateZone);
        etConversionRate = (EditText)findViewById(R.id.etConversionRate);
        etConversionRate.addTextChangedListener(textWatcher);
        tbConvertedAmountValue = (TextView)findViewById(R.id.tvConvertedAmountValue);

        long mCarId;
        long mDriverId;
        long mExpCategoryId = 0;
        long mExpTypeId = 0;

        carDefaultCurrencyId = mPreferences.getLong("CarCurrency_ID", -1);
        carDefaultCurrencyCode = mDbAdapter.fetchRecord(MainDbAdapter.CURRENCY_TABLE_NAME, MainDbAdapter.currencyTableColNames,
                carDefaultCurrencyId).getString(MainDbAdapter.CURRENCY_COL_CODE_POS);
//        currencyCode = carDefaultCurrencyCode;
        conversionRate = BigDecimal.ONE;

        if (operationType.equals("E")) {
            mRowId = mBundleExtras.getLong( MainDbAdapter.GEN_COL_ROWID_NAME );
            Cursor recordCursor = mDbAdapter.fetchRecord(MainDbAdapter.EXPENSES_TABLE_NAME,
                    MainDbAdapter.expensesTableColNames, mRowId);
            mCarId = recordCursor.getLong(MainDbAdapter.EXPENSES_COL_CAR_ID_POS);
            mDriverId = recordCursor.getLong(MainDbAdapter.EXPENSES_COL_DRIVER_ID_POS);
            mExpCategoryId = recordCursor.getLong(MainDbAdapter.EXPENSES_COL_EXPENSECATEGORY_POS);
            mExpTypeId = recordCursor.getLong(MainDbAdapter.EXPENSES_COL_EXPENSETYPE_ID_POS);
            initDateTime(recordCursor.getLong(MainDbAdapter.EXPENSES_COL_DATE_POS) * 1000);
            etCarIndex.setText(recordCursor.getString(MainDbAdapter.EXPENSES_COL_INDEX_POS));

            mCurrencyId = recordCursor.getLong(MainDbAdapter.EXPENSES_COL_CURRENCYENTERED_ID_POS);
            if(mCurrencyId == carDefaultCurrencyId)
                setConversionRateZoneVisible(false);
            else
                setConversionRateZoneVisible(true);

            atAmount.setText(recordCursor.getString(MainDbAdapter.EXPENSES_COL_AMOUNTENTERED_POS));
            conversionRate = new BigDecimal(recordCursor.getString(MainDbAdapter.EXPENSES_COL_CURRENCYRATE_POS));
            etConversionRate.setText(conversionRate.toString());
            tbConvertedAmountValue.setText(recordCursor.getString(MainDbAdapter.EXPENSES_COL_AMOUNT_POS));

            etDocNo.setText(recordCursor.getString(MainDbAdapter.EXPENSES_COL_DOCUMENTNO_POS));
            acUserComment.setText(recordCursor.getString(MainDbAdapter.GEN_COL_USER_COMMENT_POS));

            String fromTable = recordCursor.getString(MainDbAdapter.EXPENSES_COL_FROMTABLE_POS);
            if(fromTable == null){
                tvWarningLabel.setText("");
                setEditable((ViewGroup) findViewById(R.id.vgRoot), true);
            }
            else{
                if(fromTable.equals("Refuel")){
                    tvWarningLabel.setText(mResource.getString(R.string.ExpenseEditActivity_CreatedFromWarning).replaceAll("%",
                            mResource.getString(R.string.GEN_Refuel).toLowerCase()) + "\n");
                }
                setEditable((ViewGroup) findViewById(R.id.vgRoot), false);
            }

        }
        else {
            tvWarningLabel.setText("");
            mCarId = mPreferences.getLong("CurrentCar_ID", -1);
            mDriverId = mPreferences.getLong("CurrentDriver_ID", -1);
            mCurrencyId = mPreferences.getLong("CarCurrency_ID", -1);
            initDateTime(System.currentTimeMillis());
            setEditable((ViewGroup) findViewById(R.id.vgRoot), true);
            setConversionRateZoneVisible(false);
        }

        initSpinner(spnCar, MainDbAdapter.CAR_TABLE_NAME, MainDbAdapter.genColName,
                new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, MainDbAdapter.isActiveCondition, MainDbAdapter.GEN_COL_NAME_NAME,
                mCarId, false);

        initSpinner(spnDriver, MainDbAdapter.DRIVER_TABLE_NAME, MainDbAdapter.genColName,
                new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, MainDbAdapter.isActiveCondition, MainDbAdapter.GEN_COL_NAME_NAME,
                mDriverId, false);

        initSpinner(spnExpType, MainDbAdapter.EXPENSETYPE_TABLE_NAME, MainDbAdapter.genColName,
                new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, MainDbAdapter.isActiveCondition, MainDbAdapter.GEN_COL_NAME_NAME,
                mExpTypeId, false);

        initSpinner(spnExpCategory, MainDbAdapter.EXPENSECATEGORY_TABLE_NAME, MainDbAdapter.genColName,
                new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, MainDbAdapter.isActiveCondition, MainDbAdapter.GEN_COL_NAME_NAME,
                mExpCategoryId, false);

        initSpinner(spnCurrency, MainDbAdapter.CURRENCY_TABLE_NAME,
                MainDbAdapter.currencyTableColNames, new String[]{MainDbAdapter.CURRENCY_COL_CODE_NAME},
                    MainDbAdapter.isActiveCondition,
                    MainDbAdapter.CURRENCY_COL_CODE_NAME,
                    mCurrencyId, false);
        if(isSendStatistics)
            AndiCarStatistics.sendFlurryEvent("ExpenseEdit", null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isActivityOnLoading = true;
        if(mCurrencyId != carDefaultCurrencyId)
        {
            setConversionRateZoneVisible(true);
            if(conversionRate != null)
                calculateConvertedAmount();
        }
        else
            setConversionRateZoneVisible(false);
    }

    private View.OnTouchListener spinnerOnTouchListener = new View.OnTouchListener() {

        public boolean onTouch(View view, MotionEvent me) {
            isActivityOnLoading = false;
            return false;
        }
    };

    private View.OnClickListener mOkClickListener =
            new View.OnClickListener()
                {
                    public void onClick( View v )
                    {
                        String retVal = checkMandatory((ViewGroup) findViewById(R.id.vgRoot));
                        if( retVal != null ) {
                            Toast toast = Toast.makeText( getApplicationContext(),
                                    mResource.getString( R.string.GEN_FillMandatory ) + ": " + retVal, Toast.LENGTH_SHORT );
                            toast.show();
                            return;
                        }

                        ContentValues data = new ContentValues();
                        data.put( MainDbAdapter.GEN_COL_NAME_NAME,
                                "Expense");
                        data.put( MainDbAdapter.GEN_COL_ISACTIVE_NAME, "Y");
                        data.put( MainDbAdapter.GEN_COL_USER_COMMENT_NAME,
                                acUserComment.getText().toString() );
                        data.put( MainDbAdapter.EXPENSES_COL_CAR_ID_NAME,
                                spnCar.getSelectedItemId() );
                        data.put( MainDbAdapter.EXPENSES_COL_DRIVER_ID_NAME,
                                spnDriver.getSelectedItemId() );
                        data.put( MainDbAdapter.EXPENSES_COL_EXPENSECATEGORY_ID_NAME,
                                spnExpCategory.getSelectedItemId() );
                        data.put( MainDbAdapter.EXPENSES_COL_EXPENSETYPE_ID_NAME,
                                spnExpType.getSelectedItemId() );
                        data.put( MainDbAdapter.EXPENSES_COL_INDEX_NAME, etCarIndex.getText().toString());
                        
                        data.put( MainDbAdapter.EXPENSES_COL_AMOUNTENTERED_NAME, atAmount.getText().toString());
                        data.put( MainDbAdapter.EXPENSES_COL_CURRENCYENTERED_ID_NAME,
                                spnCurrency.getSelectedItemId() );
                        data.put( MainDbAdapter.EXPENSES_COL_CURRENCY_ID_NAME, carDefaultCurrencyId);
                        if(mCurrencyId == carDefaultCurrencyId){
                            data.put( MainDbAdapter.EXPENSES_COL_AMOUNT_NAME, atAmount.getText().toString());
                            data.put( MainDbAdapter.EXPENSES_COL_CURRENCYRATE_NAME, "1");
                        }
                        else{
                            data.put( MainDbAdapter.EXPENSES_COL_AMOUNT_NAME, convertedAmount.toString());
                            data.put( MainDbAdapter.EXPENSES_COL_CURRENCYRATE_NAME, conversionRate.toString());
                        }


                        data.put( MainDbAdapter.EXPENSES_COL_DATE_NAME, mlDateTimeInSeconds);
                        data.put( MainDbAdapter.EXPENSES_COL_DOCUMENTNO_NAME,
                                etDocNo.getText().toString());

                        if( operationType.equals("N") ) {
                            Long createResult = mDbAdapter.createRecord(MainDbAdapter.EXPENSES_TABLE_NAME, data);
                            if( createResult.intValue() < 0){
                                if(createResult.intValue() == -1) //DB Error
                                    madbErrorAlert.setMessage(mDbAdapter.lastErrorMessage);
                                else //precondition error
                                    madbErrorAlert.setMessage(mResource.getString(-1 * createResult.intValue()));
                                madError = madbErrorAlert.create();
                                madError.show();
                            }
                            else
                                finish();
                        }
                        else {
                            int updResult = mDbAdapter.updateRecord(MainDbAdapter.EXPENSES_TABLE_NAME, mRowId, data);
                            if(updResult != -1){
                                String errMsg = "";
                                errMsg = mResource.getString(updResult);
                                if(updResult == R.string.ERR_000)
                                    errMsg = errMsg + "\n" + mDbAdapter.lastErrorMessage;
                                madbErrorAlert.setMessage(errMsg);
                                madError = madbErrorAlert.create();
                                madError.show();
                            }
                            else
                                finish();
                        }
                    }
                };

    private AdapterView.OnItemSelectedListener spinnerCarDriverOnItemSelectedListener =
            new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    if(isActivityOnLoading)
                        return;
                    userCommentAdapter = null;
                    userCommentAdapter = new ArrayAdapter<String>(ExpenseEditActivity.this,
                            android.R.layout.simple_dropdown_item_1line,
                            mDbAdapter.getAutoCompleteUserComments(MainDbAdapter.EXPENSES_TABLE_NAME, spnCar.getSelectedItemId(), 30));
                    acUserComment.setAdapter(userCommentAdapter);
                    //change the currency
                    Long newCarCurrencyId = mDbAdapter.fetchRecord(MainDbAdapter.CAR_TABLE_NAME, MainDbAdapter.carTableColNames,
                            spnCar.getSelectedItemId()).getLong(MainDbAdapter.CAR_COL_CURRENCY_ID_POS);

                    if(newCarCurrencyId != mCurrencyId){
                        initSpinner(spnCurrency, MainDbAdapter.CURRENCY_TABLE_NAME,
                                MainDbAdapter.currencyTableColNames, new String[]{MainDbAdapter.CURRENCY_COL_CODE_NAME},
                                    MainDbAdapter.isActiveCondition,
                                    MainDbAdapter.CURRENCY_COL_CODE_NAME,
                                    newCarCurrencyId, false);
                        mCurrencyId = newCarCurrencyId;
                        carDefaultCurrencyId = mCurrencyId;
                        carDefaultCurrencyCode = mDbAdapter.fetchRecord(MainDbAdapter.CURRENCY_TABLE_NAME, MainDbAdapter.currencyTableColNames,
                                carDefaultCurrencyId).getString(MainDbAdapter.CURRENCY_COL_CODE_POS);
                        conversionRate = BigDecimal.ONE;

                        setConversionRateZoneVisible(false);
                    }
                }
                public void onNothingSelected(AdapterView<?> arg0) {
                }
            };

    private AdapterView.OnItemSelectedListener spinnerCurrencyOnItemSelectedListener =
            new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    if(isActivityOnLoading)
                        return;
                    mCurrencyId = spnCurrency.getSelectedItemId();
                    if(mCurrencyId != carDefaultCurrencyId){
                        setConversionRateZoneVisible(true);
                    }
                    else{
                        setConversionRateZoneVisible(false);
                    }
//                    currencyCode = mDbAdapter.fetchRecord(MainDbAdapter.CURRENCY_TABLE_NAME, MainDbAdapter.currencyTableColNames,
//                            mCurrencyId).getString(MainDbAdapter.CURRENCY_COL_CODE_POS);
                    conversionRate = mDbAdapter.getCurrencyRate(mCurrencyId, carDefaultCurrencyId);
                    etConversionRate.setText("");
                    tbConvertedAmountValue.setText("");
                    if(conversionRate != null){
                        etConversionRate.append(conversionRate.toString());
                        calculateConvertedAmount();
                    }

                }
                public void onNothingSelected(AdapterView<?> arg0) {
                }
            };

    private void setConversionRateZoneVisible(boolean isVisible){
        if(isVisible){
            llConversionRateZone.setVisibility(View.VISIBLE);
            etConversionRate.setTag(mResource.getString(R.string.GEN_ConvertionRateLabel));
        }
        else{
            etConversionRate.setTag(null);
            llConversionRateZone.setVisibility(View.GONE);
        }
    }
    private void calculateConvertedAmount() {
        if(conversionRate == null){
            tbConvertedAmountValue.setText("");
            return;
        }
        String amountStr = atAmount.getText().toString();
        String convertedAmountStr = "";
        if(amountStr != null && amountStr.length() > 0) {
            BigDecimal amount = (new BigDecimal(amountStr))
                    .setScale(StaticValues.DECIMALS_AMOUNT, StaticValues.ROUNDING_MODE_AMOUNT);
            if(carDefaultCurrencyId != mCurrencyId){
                convertedAmount = amount.multiply(conversionRate).
                        setScale(StaticValues.DECIMALS_AMOUNT, StaticValues.ROUNDING_MODE_AMOUNT);
                convertedAmountStr = convertedAmount.toString() + " " + carDefaultCurrencyCode;
            }

            tbConvertedAmountValue.setText(convertedAmountStr);
        }
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
                        if(mCurrencyId == carDefaultCurrencyId)
                            return;
                        if(etConversionRate.getText().toString() != null && etConversionRate.getText().toString().length() > 0){
                                conversionRate = new BigDecimal(etConversionRate.getText().toString());
                        }

                        if(conversionRate != null)
                            calculateConvertedAmount();
            }
        };
}
