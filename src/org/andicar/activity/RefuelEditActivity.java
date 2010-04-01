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
import android.widget.CheckBox;
import android.widget.TextView;
import java.math.BigDecimal;
import java.math.BigInteger;
//import java.math.BigDecimal;

/**
 *
 * @author miki
 */
public class RefuelEditActivity extends EditActivityBase {
    AutoCompleteTextView userComment;
    Spinner carSpinner;
    Spinner driverSpinner;
    Spinner currencySpinner;
    EditText carIndexEntry;
    EditText qtyEntry;
    EditText priceEntry;
    EditText docNo;
    EditText conversionRateEntry;
    CheckBox refuelIsFullRefuel;
    BigDecimal fuelQuantity = null;
    BigDecimal fuelPrice = null;
    TextView amountValue;
    TextView conversionRateLabel;
    long mCurrencyId;
    long carDefaultCurrencyId;
    String carDefaultCurrencyCode;
    String currencyCode;
    BigDecimal conversionRate;

    ArrayAdapter<String> userCommentAdapter;
    private String operationType;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle, R.layout.refuel_edit_activity, mOkClickListener);

        operationType = extras.getString("Operation");
        userComment = ((AutoCompleteTextView) findViewById( R.id.genUserCommentEntry ));

        carSpinner = (Spinner)findViewById(R.id.carSpinner);
        driverSpinner = (Spinner)findViewById(R.id.driverSpinner);
        carSpinner.setOnItemSelectedListener(spinnerCarDriverOnItemSelectedListener);
        driverSpinner.setOnItemSelectedListener(spinnerCarDriverOnItemSelectedListener);
        currencySpinner = (Spinner) findViewById( R.id.currencySpinner );
        currencySpinner.setOnItemSelectedListener(spinnerCurrencyOnItemSelectedListener);
        
        userCommentAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line,
                mMainDbAdapter.getAutoCompleteUserComments(MainDbAdapter.REFUEL_TABLE_NAME,
                    mPreferences.getLong("CurrentCar_ID", -1), 30));
        userComment.setAdapter(userCommentAdapter);

        carIndexEntry = (EditText)findViewById(R.id.indexEntry);
        qtyEntry = (EditText)findViewById(R.id.quantityEntry);
        qtyEntry.setOnKeyListener(refuelEditOnKeyListener);
        
        priceEntry = (EditText)findViewById(R.id.priceEntry);
        priceEntry.setOnKeyListener(refuelEditOnKeyListener);
        docNo = (EditText)findViewById(R.id.documentNoEntry);
        refuelIsFullRefuel = (CheckBox) findViewById(R.id.refuelIsFullRefuel);
        conversionRateLabel = (TextView)findViewById(R.id.conversionRateLabel);
        conversionRateEntry = (EditText)findViewById(R.id.conversionRateEntry);
        conversionRateLabel.setVisibility(View.INVISIBLE);
        conversionRateEntry.setVisibility(View.INVISIBLE);

        amountValue = (TextView)findViewById(R.id.amountValue);


        long mCarId;
        long mDriverId;
        long mExpCategoryId;
        long mExpTypeId;
        long mQtyUmId;

        carDefaultCurrencyId = mPreferences.getLong("CarCurrency_ID", -1);
        carDefaultCurrencyCode = mMainDbAdapter.fetchRecord(MainDbAdapter.CURRENCY_TABLE_NAME, MainDbAdapter.currencyTableColNames,
                carDefaultCurrencyId).getString(MainDbAdapter.CURRENCY_COL_CODE_POS);
        currencyCode = carDefaultCurrencyCode;
        conversionRate = BigDecimal.ONE;

        if (operationType.equals("E")) {
            mRowId = extras.getLong( MainDbAdapter.GEN_COL_ROWID_NAME );
            Cursor recordCursor = mMainDbAdapter.fetchRecord(MainDbAdapter.REFUEL_TABLE_NAME, MainDbAdapter.refuelTableColNames, mRowId);
            mCarId = recordCursor.getLong(MainDbAdapter.REFUEL_COL_CAR_ID_POS);
            mDriverId = recordCursor.getLong(MainDbAdapter.REFUEL_COL_DRIVER_ID_POS);
            mExpCategoryId = recordCursor.getLong(MainDbAdapter.REFUEL_COL_EXPENSECATEGORY_ID_POS);
            mExpTypeId = recordCursor.getLong(MainDbAdapter.REFUEL_COL_EXPENSETYPE_ID_POS);
            mQtyUmId = recordCursor.getLong(MainDbAdapter.REFUEL_COL_UOMVOLUME_ID_POS);
            mCurrencyId = recordCursor.getLong(MainDbAdapter.REFUEL_COL_CURRENCY_ID_POS);
            initDateTime(recordCursor.getLong(MainDbAdapter.REFUEL_COL_DATE_POS) * 1000);
            carIndexEntry.setText(recordCursor.getString(MainDbAdapter.REFUEL_COL_INDEX_POS));
            qtyEntry.setText(recordCursor.getString(MainDbAdapter.REFUEL_COL_QUANTITY_POS));
            priceEntry.setText(recordCursor.getString(MainDbAdapter.REFUEL_COL_PRICE_POS));
            docNo.setText(recordCursor.getString(MainDbAdapter.REFUEL_COL_DOCUMENTNO_POS));
            userComment.setText(recordCursor.getString(MainDbAdapter.GEN_COL_USER_COMMENT_POS));
            refuelIsFullRefuel.setChecked(recordCursor.getString(MainDbAdapter.REFUEL_COL_ISFULLREFUEL_POS).equals("Y"));

        }
        else {
            mCarId = mPreferences.getLong("CurrentCar_ID", -1);
            mDriverId = mPreferences.getLong("CurrentDriver_ID", -1);
            mExpCategoryId = mPreferences.getLong("RefuelExpenseCategory_ID", 1);
            mExpTypeId = mPreferences.getLong("RefuelExpenseType_ID", -1);
            mQtyUmId = mPreferences.getLong("CarUOMVolume_ID", -1);
            mCurrencyId = mPreferences.getLong("CarCurrency_ID", -1);
            initDateTime(System.currentTimeMillis());
            refuelIsFullRefuel.setChecked(false);
        }
        
        initSpinner(carSpinner, MainDbAdapter.CAR_TABLE_NAME, MainDbAdapter.genColName,
                new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, MainDbAdapter.isActiveCondition, MainDbAdapter.GEN_COL_NAME_NAME,
                mCarId, false);

        initSpinner(driverSpinner, MainDbAdapter.DRIVER_TABLE_NAME, MainDbAdapter.genColName,
                new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, MainDbAdapter.isActiveCondition, MainDbAdapter.GEN_COL_NAME_NAME,
                mDriverId, false);

        initSpinner((Spinner)findViewById(R.id.expenseTypeSpinner), MainDbAdapter.EXPENSETYPE_TABLE_NAME, MainDbAdapter.genColName,
                new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, MainDbAdapter.isActiveCondition, MainDbAdapter.GEN_COL_NAME_NAME,
                mExpTypeId, true);

        initSpinner((Spinner)findViewById(R.id.expenseCategorySpinner), MainDbAdapter.EXPENSECATEGORY_TABLE_NAME, MainDbAdapter.genColName,
                new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, MainDbAdapter.isActiveCondition, MainDbAdapter.GEN_COL_NAME_NAME,
                mExpCategoryId, true);

        initSpinner((Spinner) findViewById( R.id.UOMVolumeSpinner ), MainDbAdapter.UOM_TABLE_NAME,
                MainDbAdapter.uomTableColNames, new String[]{MainDbAdapter.UOM_COL_CODE_NAME},
                MainDbAdapter.UOM_COL_UOMTYPE_NAME + "='" + StaticValues.UOM_VOLUME_TYPE_CODE + "'" +
                    MainDbAdapter.isActiveWithAndCondition, MainDbAdapter.UOM_COL_CODE_NAME,
                    mQtyUmId, false);
        initSpinner(currencySpinner, MainDbAdapter.CURRENCY_TABLE_NAME,
                MainDbAdapter.currencyTableColNames, new String[]{MainDbAdapter.CURRENCY_COL_CODE_NAME},
                    MainDbAdapter.isActiveCondition,
                    MainDbAdapter.CURRENCY_COL_CODE_NAME,
                    mCurrencyId, false);
        
//        android.R.color.white
    }

    @Override
    protected void onResume() {
        super.onResume();
        calculateAmount();
    }


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
                                "Refuel");
                        data.put( MainDbAdapter.GEN_COL_ISACTIVE_NAME, "Y");
                        data.put( MainDbAdapter.GEN_COL_USER_COMMENT_NAME,
                                userComment.getText().toString() );
                        data.put( MainDbAdapter.REFUEL_COL_CAR_ID_NAME,
                                carSpinner.getSelectedItemId() );
                        data.put( MainDbAdapter.REFUEL_COL_DRIVER_ID_NAME,
                                driverSpinner.getSelectedItemId() );
                        data.put( MainDbAdapter.REFUEL_COL_EXPENSECATEGORY_NAME,
                                ((Spinner) findViewById( R.id.expenseCategorySpinner )).getSelectedItemId() );
                        data.put( MainDbAdapter.REFUEL_COL_EXPENSETYPE_ID_NAME,
                                ((Spinner) findViewById( R.id.expenseTypeSpinner )).getSelectedItemId() );
                        data.put( MainDbAdapter.REFUEL_COL_INDEX_NAME, carIndexEntry.getText().toString());
                        data.put( MainDbAdapter.REFUEL_COL_QUANTITY_NAME, qtyEntry.getText().toString());
                        data.put( MainDbAdapter.REFUEL_COL_UOMVOLUME_ID_NAME,
                                ((Spinner) findViewById( R.id.UOMVolumeSpinner )).getSelectedItemId() );
                        data.put( MainDbAdapter.REFUEL_COL_PRICE_NAME, priceEntry.getText().toString());
                        data.put( MainDbAdapter.REFUEL_COL_CURRENCY_ID_NAME,
                                currencySpinner.getSelectedItemId() );
                        data.put( MainDbAdapter.REFUEL_COL_DATE_NAME, mDateTimeInSeconds);
                        data.put( MainDbAdapter.REFUEL_COL_DOCUMENTNO_NAME,
                                docNo.getText().toString());
                        data.put( MainDbAdapter.REFUEL_COL_ISFULLREFUEL_NAME,
                                (refuelIsFullRefuel.isChecked() ? "Y" : "N"));

                        if( operationType.equals("N") ) {
                            Long createResult = mMainDbAdapter.createRecord(MainDbAdapter.REFUEL_TABLE_NAME, data);
                            if(createResult.intValue() < 0){
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
                            int updResult = mMainDbAdapter.updateRecord(MainDbAdapter.REFUEL_TABLE_NAME, mRowId, data);
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
                    userCommentAdapter = null;
                    userCommentAdapter = new ArrayAdapter<String>(RefuelEditActivity.this,
                            android.R.layout.simple_dropdown_item_1line,
                            mMainDbAdapter.getAutoCompleteUserComments(MainDbAdapter.REFUEL_TABLE_NAME,
                            carSpinner.getSelectedItemId(), 30));
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

                        conversionRateLabel.setVisibility(View.INVISIBLE);
                        conversionRateEntry.setVisibility(View.INVISIBLE);
                        conversionRateEntry.setTag(null);
                        calculateAmount();
                    }
                }
                public void onNothingSelected(AdapterView<?> arg0) {
                }
            };

    private AdapterView.OnItemSelectedListener spinnerCurrencyOnItemSelectedListener =
            new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    mCurrencyId = currencySpinner.getSelectedItemId();
                    if(mCurrencyId != carDefaultCurrencyId){
                        conversionRateLabel.setVisibility(View.VISIBLE);
                        conversionRateEntry.setVisibility(View.VISIBLE);
                        conversionRateEntry.setTag(mRes.getString(R.string.REFUELEDIT_CONVRATE_LABEL));
                    }
                    else{
                        conversionRateLabel.setVisibility(View.INVISIBLE);
                        conversionRateEntry.setVisibility(View.INVISIBLE);
                        conversionRateEntry.setTag(null);
                    }
                    currencyCode = mMainDbAdapter.fetchRecord(MainDbAdapter.CURRENCY_TABLE_NAME, MainDbAdapter.currencyTableColNames,
                            mCurrencyId).getString(MainDbAdapter.CURRENCY_COL_CODE_POS);
                    conversionRate = mMainDbAdapter.getCurrencyRate(carDefaultCurrencyId, mCurrencyId);
                    conversionRateEntry.setText("");
                    if(conversionRate != null){
                        conversionRateEntry.append(conversionRate.toString());
                    }

                    calculateAmount();
                }
                public void onNothingSelected(AdapterView<?> arg0) {
                }
            };

    private View.OnKeyListener refuelEditOnKeyListener =
            new View.OnKeyListener() {
                    public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
                        if(arg2.getAction() != KeyEvent.ACTION_UP) {
                            return false;
                        }
                        calculateAmount();
                        return false;
                    }
                };

    private void calculateAmount() {
        String qtyStr = qtyEntry.getText().toString();
        String priceStr = priceEntry.getText().toString();
        String amountStr = "";
        BigDecimal convertedAmount = null;
        if(qtyStr != null && qtyStr.length() > 0
                && priceStr != null && priceStr.length() > 0) {
            BigDecimal amount = (new BigDecimal(qtyStr)).multiply(new BigDecimal(priceStr))
                    .setScale(StaticValues.amountDecimals, StaticValues.amountRoundingMode);
            amountStr = amount.toString() + " " + currencyCode;
            if(carDefaultCurrencyId != mCurrencyId){
                convertedAmount = amount.multiply(conversionRate).
                        setScale(StaticValues.amountDecimals, StaticValues.amountRoundingMode);
                amountStr = amountStr + " ( " + convertedAmount.toString() + " " + carDefaultCurrencyCode + " )";
            }

            amountValue.setText(amountStr);
        }
    }
}
