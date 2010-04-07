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
import android.view.KeyEvent;
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

/**
 *
 * @author miki
 */
public class ExpenseEditActivity extends EditActivityBase {
    private AutoCompleteTextView userComment;
    private Spinner carSpinner;
    private Spinner driverSpinner;
    private Spinner currencySpinner;
    private EditText carIndexEntry;
    private EditText amountEntry;
    private EditText docNo;
    private EditText conversionRateEntry;
    private TextView warningLabel;
    private TextView convertedAmountValue;
    private long mCurrencyId;
    private long carDefaultCurrencyId;
    private String carDefaultCurrencyCode;
//    String currencyCode;
    private BigDecimal conversionRate;
    private BigDecimal convertedAmount = null;
    private LinearLayout conversionRateZone;
    private boolean isActivityOnLoading = true;

    private ArrayAdapter<String> userCommentAdapter;

    private String operationType;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle, R.layout.expense_edit_activity, mOkClickListener);

        operationType = extras.getString("Operation");
        userComment = ((AutoCompleteTextView) findViewById( R.id.genUserCommentEntry ));

        carSpinner = (Spinner)findViewById(R.id.carSpinner);
        driverSpinner = (Spinner)findViewById(R.id.driverSpinner);
        carSpinner.setOnItemSelectedListener(spinnerCarDriverOnItemSelectedListener);
        driverSpinner.setOnItemSelectedListener(spinnerCarDriverOnItemSelectedListener);
        carSpinner.setOnTouchListener(spinnerOnTouchListener);
        driverSpinner.setOnTouchListener(spinnerOnTouchListener);

        currencySpinner = (Spinner)findViewById(R.id.currencySpinner);
        currencySpinner.setOnItemSelectedListener(spinnerCurrencyOnItemSelectedListener);
        currencySpinner.setOnTouchListener(spinnerOnTouchListener);

        userCommentAdapter = new ArrayAdapter<String>(ExpenseEditActivity.this,
                android.R.layout.simple_dropdown_item_1line,
                mMainDbAdapter.getAutoCompleteUserComments(MainDbAdapter.EXPENSES_TABLE_NAME,
                mPreferences.getLong("CurrentCar_ID", -1), 30));
        userComment.setAdapter(userCommentAdapter);

        carIndexEntry = (EditText)findViewById(R.id.indexEntry);
        amountEntry = (EditText)findViewById(R.id.amountEntry);
        amountEntry.setOnKeyListener(editTextOnKeyListener);
        
        docNo = (EditText)findViewById(R.id.documentNoEntry);

        warningLabel = (TextView)findViewById(R.id.warningLabel);

        conversionRateZone = (LinearLayout)findViewById(R.id.conversionRateZone);
        conversionRateEntry = (EditText)findViewById(R.id.conversionRateEntry);
        conversionRateEntry.setOnKeyListener(editTextOnKeyListener);
        convertedAmountValue = (TextView)findViewById(R.id.convertedAmountValue);

        long mCarId;
        long mDriverId;
        long mExpCategoryId = 0;
        long mExpTypeId = 0;

        carDefaultCurrencyId = mPreferences.getLong("CarCurrency_ID", -1);
        carDefaultCurrencyCode = mMainDbAdapter.fetchRecord(MainDbAdapter.CURRENCY_TABLE_NAME, MainDbAdapter.currencyTableColNames,
                carDefaultCurrencyId).getString(MainDbAdapter.CURRENCY_COL_CODE_POS);
//        currencyCode = carDefaultCurrencyCode;
        conversionRate = BigDecimal.ONE;

        if (operationType.equals("E")) {
            mRowId = extras.getLong( MainDbAdapter.GEN_COL_ROWID_NAME );
            Cursor recordCursor = mMainDbAdapter.fetchRecord(MainDbAdapter.EXPENSES_TABLE_NAME,
                    MainDbAdapter.expensesTableColNames, mRowId);
            mCarId = recordCursor.getLong(MainDbAdapter.EXPENSES_COL_CAR_ID_POS);
            mDriverId = recordCursor.getLong(MainDbAdapter.EXPENSES_COL_DRIVER_ID_POS);
            mExpCategoryId = recordCursor.getLong(MainDbAdapter.EXPENSES_COL_EXPENSECATEGORY_POS);
            mExpTypeId = recordCursor.getLong(MainDbAdapter.EXPENSES_COL_EXPENSETYPE_ID_POS);
            initDateTime(recordCursor.getLong(MainDbAdapter.EXPENSES_COL_DATE_POS) * 1000);
            carIndexEntry.setText(recordCursor.getString(MainDbAdapter.EXPENSES_COL_INDEX_POS));

            mCurrencyId = recordCursor.getLong(MainDbAdapter.EXPENSES_COL_CURRENCYENTERED_ID_POS);
            if(mCurrencyId == carDefaultCurrencyId)
                setConversionRateZoneVisible(false);
            else
                setConversionRateZoneVisible(true);

            amountEntry.setText(recordCursor.getString(MainDbAdapter.EXPENSES_COL_AMOUNTENTERED_POS));
            conversionRate = new BigDecimal(recordCursor.getString(MainDbAdapter.EXPENSES_COL_CURRENCYRATE_POS));
            conversionRateEntry.setText(conversionRate.toString());
            convertedAmountValue.setText(recordCursor.getString(MainDbAdapter.EXPENSES_COL_AMOUNT_POS));

            docNo.setText(recordCursor.getString(MainDbAdapter.EXPENSES_COL_DOCUMENTNO_POS));
            userComment.setText(recordCursor.getString(MainDbAdapter.GEN_COL_USER_COMMENT_POS));

            String fromTable = recordCursor.getString(MainDbAdapter.EXPENSES_COL_FROMTABLE_POS);
            if(fromTable == null){
                warningLabel.setText("");
                setEditable((ViewGroup) findViewById(R.id.genRootViewGroup), true);
            }
            else{
                if(fromTable.equals("Refuel")){
                    warningLabel.setText(mRes.getString(R.string.EXPENSEEDIT_ACTIVITY_WARNING_LABEL).replaceAll("%",
                            mRes.getString(R.string.APP_ACTIVITY_REFUEL).toLowerCase()) + "\n");
                }
                setEditable((ViewGroup) findViewById(R.id.genRootViewGroup), false);
            }

        }
        else {
            warningLabel.setText("");
            mCarId = mPreferences.getLong("CurrentCar_ID", -1);
            mDriverId = mPreferences.getLong("CurrentDriver_ID", -1);
            mCurrencyId = mPreferences.getLong("CarCurrency_ID", -1);
            initDateTime(System.currentTimeMillis());
            setEditable((ViewGroup) findViewById(R.id.genRootViewGroup), true);
            setConversionRateZoneVisible(false);
        }

        initSpinner(carSpinner, MainDbAdapter.CAR_TABLE_NAME, MainDbAdapter.genColName,
                new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, MainDbAdapter.isActiveCondition, MainDbAdapter.GEN_COL_NAME_NAME,
                mCarId, false);

        initSpinner(driverSpinner, MainDbAdapter.DRIVER_TABLE_NAME, MainDbAdapter.genColName,
                new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, MainDbAdapter.isActiveCondition, MainDbAdapter.GEN_COL_NAME_NAME,
                mDriverId, false);

        initSpinner((Spinner)findViewById(R.id.expenseTypeSpinner), MainDbAdapter.EXPENSETYPE_TABLE_NAME, MainDbAdapter.genColName,
                new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, MainDbAdapter.isActiveCondition, MainDbAdapter.GEN_COL_NAME_NAME,
                mExpTypeId, false);

        initSpinner((Spinner)findViewById(R.id.expenseCategorySpinner), MainDbAdapter.EXPENSECATEGORY_TABLE_NAME, MainDbAdapter.genColName,
                new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, MainDbAdapter.isActiveCondition, MainDbAdapter.GEN_COL_NAME_NAME,
                mExpCategoryId, false);

        initSpinner(currencySpinner, MainDbAdapter.CURRENCY_TABLE_NAME,
                MainDbAdapter.currencyTableColNames, new String[]{MainDbAdapter.CURRENCY_COL_CODE_NAME},
                    MainDbAdapter.isActiveCondition,
                    MainDbAdapter.CURRENCY_COL_CODE_NAME,
                    mCurrencyId, false);
        
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mCurrencyId != carDefaultCurrencyId && conversionRate != null)
            calculateConvertedAmount();
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
                        String retVal = checkMandatory((ViewGroup) findViewById(R.id.genRootViewGroup));
                        if( retVal != null ) {
                            Toast toast = Toast.makeText( getApplicationContext(),
                                    mRes.getString( R.string.GEN_FILL_MANDATORY ) + ": " + retVal, Toast.LENGTH_SHORT );
                            toast.show();
                            return;
                        }

                        ContentValues data = new ContentValues();
                        data.put( MainDbAdapter.GEN_COL_NAME_NAME,
                                "Expense");
                        data.put( MainDbAdapter.GEN_COL_ISACTIVE_NAME, "Y");
                        data.put( MainDbAdapter.GEN_COL_USER_COMMENT_NAME,
                                userComment.getText().toString() );
                        data.put( MainDbAdapter.EXPENSES_COL_CAR_ID_NAME,
                                carSpinner.getSelectedItemId() );
                        data.put( MainDbAdapter.EXPENSES_COL_DRIVER_ID_NAME,
                                driverSpinner.getSelectedItemId() );
                        data.put( MainDbAdapter.EXPENSES_COL_EXPENSECATEGORY_ID_NAME,
                                ((Spinner) findViewById( R.id.expenseCategorySpinner )).getSelectedItemId() );
                        data.put( MainDbAdapter.EXPENSES_COL_EXPENSETYPE_ID_NAME,
                                ((Spinner) findViewById( R.id.expenseTypeSpinner )).getSelectedItemId() );
                        data.put( MainDbAdapter.EXPENSES_COL_INDEX_NAME, carIndexEntry.getText().toString());
                        
                        data.put( MainDbAdapter.EXPENSES_COL_AMOUNTENTERED_NAME, amountEntry.getText().toString());
                        data.put( MainDbAdapter.EXPENSES_COL_CURRENCYENTERED_ID_NAME,
                                currencySpinner.getSelectedItemId() );
                        data.put( MainDbAdapter.EXPENSES_COL_CURRENCY_ID_NAME, carDefaultCurrencyId);
                        if(mCurrencyId == carDefaultCurrencyId){
                            data.put( MainDbAdapter.EXPENSES_COL_AMOUNT_NAME, amountEntry.getText().toString());
                            data.put( MainDbAdapter.EXPENSES_COL_CURRENCYRATE_NAME, "1");
                        }
                        else{
                            data.put( MainDbAdapter.EXPENSES_COL_AMOUNT_NAME, convertedAmount.toString());
                            data.put( MainDbAdapter.EXPENSES_COL_CURRENCYRATE_NAME, conversionRate.toString());
                        }


                        data.put( MainDbAdapter.EXPENSES_COL_DATE_NAME, mDateTimeInSeconds);
                        data.put( MainDbAdapter.EXPENSES_COL_DOCUMENTNO_NAME,
                                docNo.getText().toString());

                        if( operationType.equals("N") ) {
                            Long createResult = mMainDbAdapter.createRecord(MainDbAdapter.EXPENSES_TABLE_NAME, data);
                            if( createResult.intValue() < 0){
                                if(createResult.intValue() == -1) //DB Error
                                    errorAlertBuilder.setMessage(mMainDbAdapter.lastErrorMessage);
                                else //precondition error
                                    errorAlertBuilder.setMessage(mRes.getString(-1 * createResult.intValue()));
                                errorAlert = errorAlertBuilder.create();
                                errorAlert.show();
                            }
                            else
                                finish();
                        }
                        else {
                            int updResult = mMainDbAdapter.updateRecord(MainDbAdapter.EXPENSES_TABLE_NAME, mRowId, data);
                            if(updResult != -1){
                                String errMsg = "";
                                errMsg = mRes.getString(updResult);
                                if(updResult == R.string.ERR_000)
                                    errMsg = errMsg + "\n" + mMainDbAdapter.lastErrorMessage;
                                errorAlertBuilder.setMessage(errMsg);
                                errorAlert = errorAlertBuilder.create();
                                errorAlert.show();
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
                            mMainDbAdapter.getAutoCompleteUserComments(MainDbAdapter.EXPENSES_TABLE_NAME, carSpinner.getSelectedItemId(), 30));
                    userComment.setAdapter(userCommentAdapter);
                    //change the currency
                    Long newCarCurrencyId = mMainDbAdapter.fetchRecord(MainDbAdapter.CAR_TABLE_NAME, MainDbAdapter.carTableColNames,
                            carSpinner.getSelectedItemId()).getLong(MainDbAdapter.CAR_COL_CURRENCY_ID_POS);

                    if(newCarCurrencyId != mCurrencyId){
                        initSpinner(currencySpinner, MainDbAdapter.CURRENCY_TABLE_NAME,
                                MainDbAdapter.currencyTableColNames, new String[]{MainDbAdapter.CURRENCY_COL_CODE_NAME},
                                    MainDbAdapter.isActiveCondition,
                                    MainDbAdapter.CURRENCY_COL_CODE_NAME,
                                    newCarCurrencyId, false);
                        mCurrencyId = newCarCurrencyId;
                        carDefaultCurrencyId = mCurrencyId;
                        carDefaultCurrencyCode = mMainDbAdapter.fetchRecord(MainDbAdapter.CURRENCY_TABLE_NAME, MainDbAdapter.currencyTableColNames,
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
                    mCurrencyId = currencySpinner.getSelectedItemId();
                    if(mCurrencyId != carDefaultCurrencyId){
                        setConversionRateZoneVisible(true);
                    }
                    else{
                        setConversionRateZoneVisible(false);
                    }
//                    currencyCode = mMainDbAdapter.fetchRecord(MainDbAdapter.CURRENCY_TABLE_NAME, MainDbAdapter.currencyTableColNames,
//                            mCurrencyId).getString(MainDbAdapter.CURRENCY_COL_CODE_POS);
                    conversionRate = mMainDbAdapter.getCurrencyRate(mCurrencyId, carDefaultCurrencyId);
                    conversionRateEntry.setText("");
                    convertedAmountValue.setText("");
                    if(conversionRate != null){
                        conversionRateEntry.append(conversionRate.toString());
                        calculateConvertedAmount();
                    }

                }
                public void onNothingSelected(AdapterView<?> arg0) {
                }
            };

    private void setConversionRateZoneVisible(boolean isVisible){
        if(isVisible){
            conversionRateZone.setVisibility(View.VISIBLE);
            conversionRateEntry.setTag(mRes.getString(R.string.GEN_CONVRATE_LABEL));
        }
        else{
            conversionRateEntry.setTag(null);
            conversionRateZone.setVisibility(View.GONE);
        }
    }
    private void calculateConvertedAmount() {
        if(conversionRate == null){
            convertedAmountValue.setText("");
            return;
        }
        String amountStr = amountEntry.getText().toString();
        String convertedAmountStr = "";
        if(amountStr != null && amountStr.length() > 0) {
            BigDecimal amount = (new BigDecimal(amountStr))
                    .setScale(StaticValues.amountDecimals, StaticValues.amountRoundingMode);
            if(carDefaultCurrencyId != mCurrencyId){
                convertedAmount = amount.multiply(conversionRate).
                        setScale(StaticValues.amountDecimals, StaticValues.amountRoundingMode);
                convertedAmountStr = convertedAmount.toString() + " " + carDefaultCurrencyCode;
            }

            convertedAmountValue.setText(convertedAmountStr);
        }
    }

    private View.OnKeyListener editTextOnKeyListener =
            new View.OnKeyListener() {
                    public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
                        if(arg2.getAction() != KeyEvent.ACTION_UP) {
                            return false;
                        }
                        if(mCurrencyId == carDefaultCurrencyId)
                            return false;
                        if(arg0 instanceof EditText && ((EditText) arg0).getId() == R.id.conversionRateEntry){
                            if(((EditText) arg0).getText().toString() != null
                                    && ((EditText) arg0).getText().toString().length() > 0)
                                conversionRate = new BigDecimal(((EditText) arg0).getText().toString());
                        }

                        if(conversionRate != null)
                            calculateConvertedAmount();
                        return false;
                    }
                };
}
