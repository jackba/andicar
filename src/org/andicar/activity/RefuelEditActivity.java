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
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.math.BigDecimal;

/**
 *
 * @author miki
 */
public class RefuelEditActivity extends EditActivityBase {
    private AutoCompleteTextView userComment;
    private Spinner carSpinner;
    private Spinner driverSpinner;
    private Spinner currencySpinner;
    private Spinner uomVolumeSpinner;
    private EditText carIndexEntry;
    private EditText qtyEntry;
    private EditText priceEntry;
    private EditText docNo;
    private TextView convertedAmountLabel;
    private EditText conversionRateEntry;
    private LinearLayout conversionRateZone;
    private CheckBox refuelIsFullRefuel;
    private BigDecimal baseFuelPrice = null;
    private TextView amountValue;
    private long mCurrencyId;
    private long carDefaultCurrencyId;
    private String carDefaultCurrencyCode;
    private String currencyCode;
    private BigDecimal currencyConversionRate;
    private BigDecimal convertedAmount;
    
    private LinearLayout baseUOMQtyZone;
    private TextView baseUOMQtyLabel;
    private TextView baseUOMQtyValue;

    private long carDefaultUOMVolumeId;
    private long mUomVolumeId;
    private String carDefaultUOMVolumeCode;
    private BigDecimal uomVolumeConversionRate;
    private BigDecimal baseUomQty;

    private ArrayAdapter<String> userCommentAdapter;
    private String operationType;

    private boolean isActivityOnLoading = true;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle, R.layout.refuel_edit_activity, mOkClickListener);

        operationType = extras.getString("Operation");
        userComment = ((AutoCompleteTextView) findViewById( R.id.genUserCommentEntry ));

        carSpinner = (Spinner)findViewById(R.id.carSpinner);
        driverSpinner = (Spinner)findViewById(R.id.driverSpinner);
        carSpinner.setOnItemSelectedListener(spinnerCarDriverOnItemSelectedListener);
        carSpinner.setOnTouchListener(spinnerOnTouchListener);
        driverSpinner.setOnItemSelectedListener(spinnerCarDriverOnItemSelectedListener);
        driverSpinner.setOnTouchListener(spinnerOnTouchListener);
        currencySpinner = (Spinner) findViewById( R.id.currencySpinner );
        currencySpinner.setOnItemSelectedListener(spinnerCurrencyOnItemSelectedListener);
        currencySpinner.setOnTouchListener(spinnerOnTouchListener);

        uomVolumeSpinner = (Spinner)findViewById(R.id.UOMVolumeSpinner);
        uomVolumeSpinner.setOnItemSelectedListener(spinnerUOMOnItemSelectedListener);
        uomVolumeSpinner.setOnTouchListener(spinnerOnTouchListener);
        
        userCommentAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line,
                mMainDbAdapter.getAutoCompleteUserComments(MainDbAdapter.REFUEL_TABLE_NAME,
                    mPreferences.getLong("CurrentCar_ID", -1), 30));
        userComment.setAdapter(userCommentAdapter);

        carIndexEntry = (EditText)findViewById(R.id.indexEntry);
        qtyEntry = (EditText)findViewById(R.id.quantityEntry);
        qtyEntry.setOnKeyListener(editTextOnKeyListener);
        
        priceEntry = (EditText)findViewById(R.id.priceEntry);
        priceEntry.setOnKeyListener(editTextOnKeyListener);
        docNo = (EditText)findViewById(R.id.documentNoEntry);
        refuelIsFullRefuel = (CheckBox) findViewById(R.id.refuelIsFullRefuel);
        convertedAmountLabel = (TextView)findViewById(R.id.convertedAmountLabel);
        conversionRateEntry = (EditText)findViewById(R.id.conversionRateEntry);
        conversionRateEntry.setOnKeyListener(editTextOnKeyListener);
        amountValue = (TextView)findViewById(R.id.amountValue);
        conversionRateZone = (LinearLayout)findViewById(R.id.conversionRateZone);
        setConversionRateVisibility(false);

        baseUOMQtyZone = (LinearLayout)findViewById(R.id.baseUOMQtyZone);
        baseUOMQtyLabel = (TextView)findViewById(R.id.baseUOMQtyLabel);
        baseUOMQtyValue = (TextView)findViewById(R.id.baseUOMQtyValue);
        setBaseUOMQtyZoneVisibility(false);

        long mCarId;
        long mDriverId;
        long mExpCategoryId;
        long mExpTypeId;

        carDefaultCurrencyId = mPreferences.getLong("CarCurrency_ID", -1);
        carDefaultCurrencyCode = mMainDbAdapter.fetchRecord(MainDbAdapter.CURRENCY_TABLE_NAME, MainDbAdapter.currencyTableColNames,
                carDefaultCurrencyId).getString(MainDbAdapter.CURRENCY_COL_CODE_POS);
        currencyCode = carDefaultCurrencyCode;
        currencyConversionRate = BigDecimal.ONE;
        uomVolumeConversionRate = BigDecimal.ONE;

        if (operationType.equals("E")) {
            mRowId = extras.getLong( MainDbAdapter.GEN_COL_ROWID_NAME );
            Cursor recordCursor = mMainDbAdapter.fetchRecord(MainDbAdapter.REFUEL_TABLE_NAME, MainDbAdapter.refuelTableColNames, mRowId);
            mCarId = recordCursor.getLong(MainDbAdapter.REFUEL_COL_CAR_ID_POS);
            mDriverId = recordCursor.getLong(MainDbAdapter.REFUEL_COL_DRIVER_ID_POS);
            mExpCategoryId = recordCursor.getLong(MainDbAdapter.REFUEL_COL_EXPENSECATEGORY_ID_POS);
            mExpTypeId = recordCursor.getLong(MainDbAdapter.REFUEL_COL_EXPENSETYPE_ID_POS);
            mUomVolumeId = recordCursor.getLong(MainDbAdapter.REFUEL_COL_UOMVOLUMEENTERED_ID_POS);
//            mUomVolumeId = recordCursor.getLong(MainDbAdapter.REFUEL_COL_UOMVOLUME_ID_POS);
            mCurrencyId = recordCursor.getLong(MainDbAdapter.REFUEL_COL_CURRENCYENTERED_ID_POS);
//            mCurrencyId = recordCursor.getLong(MainDbAdapter.REFUEL_COL_CURRENCY_ID_POS);
            currencyConversionRate = new BigDecimal(recordCursor.getString(MainDbAdapter.REFUEL_COL_CURRENCYRATE_POS));
            conversionRateEntry.setText(currencyConversionRate.toString());
            initDateTime(recordCursor.getLong(MainDbAdapter.REFUEL_COL_DATE_POS) * 1000);
            carIndexEntry.setText(recordCursor.getString(MainDbAdapter.REFUEL_COL_INDEX_POS));
            qtyEntry.setText(recordCursor.getString(MainDbAdapter.REFUEL_COL_QUANTITYENTERED_POS));
//            qtyEntry.setText(recordCursor.getString(MainDbAdapter.REFUEL_COL_QUANTITY_POS));
            priceEntry.setText(recordCursor.getString(MainDbAdapter.REFUEL_COL_PRICEENTERED_POS));
//            priceEntry.setText(recordCursor.getString(MainDbAdapter.REFUEL_COL_PRICE_POS));
            docNo.setText(recordCursor.getString(MainDbAdapter.REFUEL_COL_DOCUMENTNO_POS));
            userComment.setText(recordCursor.getString(MainDbAdapter.GEN_COL_USER_COMMENT_POS));
            refuelIsFullRefuel.setChecked(recordCursor.getString(MainDbAdapter.REFUEL_COL_ISFULLREFUEL_POS).equals("Y"));

            carDefaultUOMVolumeId = mMainDbAdapter.fetchRecord(MainDbAdapter.CAR_TABLE_NAME, MainDbAdapter.carTableColNames,
                    mCarId).getLong(MainDbAdapter.CAR_COL_UOMVOLUME_ID_POS);
            carDefaultUOMVolumeCode = mMainDbAdapter.fetchRecord(MainDbAdapter.UOM_TABLE_NAME, MainDbAdapter.uomTableColNames,
                    carDefaultUOMVolumeId).getString(MainDbAdapter.UOM_COL_CODE_POS);
            uomVolumeConversionRate = new BigDecimal(recordCursor.getString(MainDbAdapter.REFUEL_COL_UOMVOLCONVERSIONRATE_POS));
            if(carDefaultUOMVolumeId != mUomVolumeId){
                baseUOMQtyValue.setText(recordCursor.getString(MainDbAdapter.REFUEL_COL_QUANTITY_POS) +
                        " " + carDefaultUOMVolumeCode);
                setBaseUOMQtyZoneVisibility(true);
            }
            if(mCurrencyId != carDefaultCurrencyId){
                currencyCode = mMainDbAdapter.fetchRecord(MainDbAdapter.CURRENCY_TABLE_NAME, MainDbAdapter.currencyTableColNames,
                    mCurrencyId).getString(MainDbAdapter.CURRENCY_COL_CODE_POS);
                setConversionRateVisibility(true);
            }
        }
        else {
            mCarId = mPreferences.getLong("CurrentCar_ID", -1);
            mDriverId = mPreferences.getLong("CurrentDriver_ID", -1);
            mExpCategoryId = mPreferences.getLong("RefuelExpenseCategory_ID", 1);
            mExpTypeId = mPreferences.getLong("RefuelExpenseType_ID", -1);
            mUomVolumeId = mPreferences.getLong("CarUOMVolume_ID", -1);
            mCurrencyId = mPreferences.getLong("CarCurrency_ID", -1);
            initDateTime(System.currentTimeMillis());
            refuelIsFullRefuel.setChecked(false);
            carDefaultUOMVolumeId = mMainDbAdapter.fetchRecord(MainDbAdapter.CAR_TABLE_NAME, MainDbAdapter.carTableColNames,
                    mCarId).getLong(MainDbAdapter.CAR_COL_UOMVOLUME_ID_POS);
            carDefaultUOMVolumeCode = mMainDbAdapter.fetchRecord(MainDbAdapter.UOM_TABLE_NAME, MainDbAdapter.uomTableColNames,
                    carDefaultUOMVolumeId).getString(MainDbAdapter.UOM_COL_CODE_POS);
        }

        baseUOMQtyLabel.setText(mRes.getString(R.string.REFUEL_BASEUOMQTY_LABEL));
        
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

        initSpinner(uomVolumeSpinner, MainDbAdapter.UOM_TABLE_NAME,
                MainDbAdapter.uomTableColNames, new String[]{MainDbAdapter.UOM_COL_CODE_NAME},
                MainDbAdapter.UOM_COL_UOMTYPE_NAME + "='" + StaticValues.UOM_VOLUME_TYPE_CODE + "'" +
                    MainDbAdapter.isActiveWithAndCondition, MainDbAdapter.UOM_COL_CODE_NAME,
                    mUomVolumeId, false);
        initSpinner(currencySpinner, MainDbAdapter.CURRENCY_TABLE_NAME,
                MainDbAdapter.currencyTableColNames, new String[]{MainDbAdapter.CURRENCY_COL_CODE_NAME},
                    MainDbAdapter.isActiveCondition,
                    MainDbAdapter.CURRENCY_COL_CODE_NAME,
                    mCurrencyId, false);
        if (operationType.equals("E")){
            calculateAmount();
            calculateBaseUOMQty();
        }
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
                        data.put( MainDbAdapter.REFUEL_COL_QUANTITYENTERED_NAME, qtyEntry.getText().toString());
                        data.put( MainDbAdapter.REFUEL_COL_UOMVOLUMEENTERED_ID_NAME,
                                uomVolumeSpinner.getSelectedItemId());
                        data.put( MainDbAdapter.REFUEL_COL_PRICEENTERED_NAME, priceEntry.getText().toString());
                        data.put( MainDbAdapter.REFUEL_COL_CURRENCYENTERED_ID_NAME,
                                currencySpinner.getSelectedItemId());
                        data.put( MainDbAdapter.REFUEL_COL_DATE_NAME, mDateTimeInSeconds);
                        data.put( MainDbAdapter.REFUEL_COL_DOCUMENTNO_NAME,
                                docNo.getText().toString());
                        data.put( MainDbAdapter.REFUEL_COL_ISFULLREFUEL_NAME,
                                (refuelIsFullRefuel.isChecked() ? "Y" : "N"));

                        if(mUomVolumeId == carDefaultUOMVolumeId){
                            data.put( MainDbAdapter.REFUEL_COL_QUANTITY_NAME, qtyEntry.getText().toString());
                            data.put( MainDbAdapter.REFUEL_COL_UOMVOLUME_ID_NAME, uomVolumeSpinner.getSelectedItemId());
                            data.put( MainDbAdapter.REFUEL_COL_UOMVOLCONVERSIONRATE_NAME, "1");
                        }
                        else{
                            data.put( MainDbAdapter.REFUEL_COL_QUANTITY_NAME, baseUomQty.toString());
                            data.put( MainDbAdapter.REFUEL_COL_UOMVOLUME_ID_NAME, carDefaultUOMVolumeId);
                            data.put( MainDbAdapter.REFUEL_COL_UOMVOLCONVERSIONRATE_NAME, uomVolumeConversionRate.toString());
                        }

                        if(mCurrencyId == carDefaultCurrencyId){
                            data.put( MainDbAdapter.REFUEL_COL_PRICE_NAME, priceEntry.getText().toString());
                            data.put( MainDbAdapter.REFUEL_COL_CURRENCY_ID_NAME, carDefaultCurrencyId);
                            data.put( MainDbAdapter.REFUEL_COL_CURRENCYRATE_NAME, "1");
                        }
                        else{
                            data.put( MainDbAdapter.REFUEL_COL_PRICE_NAME, baseFuelPrice.toString());
                            data.put( MainDbAdapter.REFUEL_COL_CURRENCY_ID_NAME, carDefaultCurrencyId);
                            data.put( MainDbAdapter.REFUEL_COL_CURRENCYRATE_NAME, currencyConversionRate.toString());
                        }

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
                    if(isActivityOnLoading)
                        return;
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
                        currencyConversionRate = BigDecimal.ONE;

                        setConversionRateVisibility(false);
                        calculateAmount();
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
                        setConversionRateVisibility(true);
                    }
                    else{
                        setConversionRateVisibility(false);
                    }
                    currencyCode = mMainDbAdapter.fetchRecord(MainDbAdapter.CURRENCY_TABLE_NAME, MainDbAdapter.currencyTableColNames,
                            mCurrencyId).getString(MainDbAdapter.CURRENCY_COL_CODE_POS);
                    currencyConversionRate = mMainDbAdapter.getCurrencyRate(mCurrencyId, carDefaultCurrencyId);
                    conversionRateEntry.setText("");
                    if(currencyConversionRate != null){
                        conversionRateEntry.append(currencyConversionRate.toString());
                    }

                    calculateAmount();
                }
                public void onNothingSelected(AdapterView<?> arg0) {
                }
            };

    private AdapterView.OnItemSelectedListener spinnerUOMOnItemSelectedListener =
            new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    if(isActivityOnLoading)
                        return;
                    mUomVolumeId = uomVolumeSpinner.getSelectedItemId();
                    if(mUomVolumeId != carDefaultUOMVolumeId){
                        setBaseUOMQtyZoneVisibility(true);
                    }
                    else{
                        setBaseUOMQtyZoneVisibility(false);
                    }

                    uomVolumeConversionRate = mMainDbAdapter.getUOMConvertionRate(mUomVolumeId, carDefaultUOMVolumeId);
                    if(uomVolumeConversionRate == null)
                        btnOk.setEnabled(false);
                    else
                        btnOk.setEnabled(true);

                    calculateBaseUOMQty();
                }
                public void onNothingSelected(AdapterView<?> arg0) {
                }
            };

    private View.OnTouchListener spinnerOnTouchListener = new View.OnTouchListener() {

        public boolean onTouch(View view, MotionEvent me) {
            isActivityOnLoading = false;
            return false;
        }
    };

    private View.OnKeyListener editTextOnKeyListener =
            new View.OnKeyListener() {
                    public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
                        if(arg2.getAction() != KeyEvent.ACTION_UP) {
                            return false;
                        }
                        if(arg0 instanceof EditText && ((EditText) arg0).getId() == R.id.conversionRateEntry){
                            if(((EditText) arg0).getText().toString() != null
                                    && ((EditText) arg0).getText().toString().length() > 0)
                                currencyConversionRate = new BigDecimal(((EditText) arg0).getText().toString());
                        }
                        else if(arg0 instanceof EditText 
                                    && ((EditText) arg0).getId() == R.id.quantityEntry
                                    && mUomVolumeId != carDefaultUOMVolumeId){
                            if(((EditText) arg0).getText().toString() != null
                                    && ((EditText) arg0).getText().toString().length() > 0)
                                calculateBaseUOMQty();
                        }

                        calculateAmount();
                        return false;
                    }
                };

    private void calculateAmount() {
        String qtyStr = qtyEntry.getText().toString();
        String priceStr = priceEntry.getText().toString();
        String amountStr = "";
        convertedAmount = null;
        if(qtyStr != null && qtyStr.length() > 0
                && priceStr != null && priceStr.length() > 0) {
            BigDecimal amount = (new BigDecimal(qtyStr)).multiply(new BigDecimal(priceStr))
                    .setScale(StaticValues.amountDecimals, StaticValues.amountRoundingMode);
            amountStr = amount.toString() + " " + currencyCode;
            amountValue.setText(amountStr);
            if(carDefaultCurrencyId != mCurrencyId && currencyConversionRate != null){
                convertedAmount = amount.multiply(currencyConversionRate).
                        setScale(StaticValues.amountDecimals, StaticValues.amountRoundingMode);
                baseFuelPrice = (new BigDecimal(priceStr)).multiply(currencyConversionRate).
                        setScale(StaticValues.amountDecimals, StaticValues.amountRoundingMode);

                amountStr = convertedAmount.toString() + " " + carDefaultCurrencyCode;
                convertedAmountLabel.setText(mRes.getString(R.string.GEN_CONVERTEDAMOUNT_LABEL) + amountStr);
            }

            
        }
    }

    private void calculateBaseUOMQty() {
        if(mUomVolumeId == carDefaultUOMVolumeId){
            return;
        }
        if(uomVolumeConversionRate == null){
            baseUOMQtyValue.setText(mRes.getString(R.string.REFUEL_NOUOMCONVERSION_MSG));
            return;
        }
        String qtyStr = qtyEntry.getText().toString();
        String amountStr;
        if(qtyStr != null && qtyStr.length() > 0) {
            baseUomQty = (new BigDecimal(qtyStr)).multiply(uomVolumeConversionRate)
                    .setScale(StaticValues.volumeDecimals, StaticValues.volumeRoundingMode);
            amountStr = baseUomQty.toString() + " " + carDefaultUOMVolumeCode;

            baseUOMQtyValue.setText(amountStr);
        }
    }

    private void setConversionRateVisibility(boolean visible){
        if(visible){
            conversionRateZone.setVisibility(View.VISIBLE);
            conversionRateEntry.setTag(mRes.getString(R.string.GEN_CONVRATE_LABEL));
        }else{
            conversionRateZone.setVisibility(View.GONE);
            conversionRateEntry.setTag(null);
        }
    }

    private void setBaseUOMQtyZoneVisibility(boolean visible){
        if(visible)
            baseUOMQtyZone.setVisibility(View.VISIBLE);
        else
            baseUOMQtyZone.setVisibility(View.GONE);
    }

}
