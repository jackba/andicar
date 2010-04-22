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
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.math.BigDecimal;

/**
 *
 * @author miki
 */
public class RefuelEditActivity extends EditActivityBase {
    private AutoCompleteTextView acUserComment;
    private Spinner spnCar;
    private Spinner spnDriver;
    private Spinner spnCurrency;
    private Spinner spnUomVolume;
    private Spinner spnExpType;
    private Spinner spnExpCategory;
    private EditText etCarIndex;
    private EditText etQty;
    private EditText etPrice;
    private EditText etDocNo;
    private TextView tvConvertedAmountLabel;
    private EditText etConversionRate;
    private LinearLayout llConversionRateZone;
    private CheckBox ckIsFullRefuel;
    private BigDecimal baseFuelPrice = null;
    private TextView tvAmountValue;
    private long mCurrencyId;
    private long carDefaultCurrencyId;
    private String carDefaultCurrencyCode;
    private String currencyCode;
    private BigDecimal currencyConversionRate;
    private BigDecimal convertedAmount;
    
    private LinearLayout llBaseUOMQtyZone;
    private TextView tvBaseUOMQtyLabel;
    private TextView tvBaseUOMQtyValue;

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

        operationType = mbundleExtras.getString("Operation");
        acUserComment = ((AutoCompleteTextView) findViewById( R.id.etUserComment ));

        spnCar = (Spinner)findViewById(R.id.spnCar);
        spnDriver = (Spinner)findViewById(R.id.spnDriver);
        spnCar.setOnItemSelectedListener(spinnerCarDriverOnItemSelectedListener);
        spnCar.setOnTouchListener(spinnerOnTouchListener);
        spnDriver.setOnItemSelectedListener(spinnerCarDriverOnItemSelectedListener);
        spnDriver.setOnTouchListener(spinnerOnTouchListener);
        spnCurrency = (Spinner) findViewById( R.id.spnCurrency );
        spnCurrency.setOnItemSelectedListener(spinnerCurrencyOnItemSelectedListener);
        spnCurrency.setOnTouchListener(spinnerOnTouchListener);

        spnUomVolume = (Spinner)findViewById(R.id.spnUomVolume);
        spnUomVolume.setOnItemSelectedListener(spinnerUOMOnItemSelectedListener);
        spnUomVolume.setOnTouchListener(spinnerOnTouchListener);

        spnExpType = (Spinner)findViewById(R.id.spnExpType);
        spnExpCategory = (Spinner)findViewById(R.id.spnExpCategory);
        
        userCommentAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line,
                mDbAdapter.getAutoCompleteUserComments(MainDbAdapter.REFUEL_TABLE_NAME,
                    mPreferences.getLong("CurrentCar_ID", -1), 30));
        acUserComment.setAdapter(userCommentAdapter);

        etCarIndex = (EditText)findViewById(R.id.etIndex);
        etQty = (EditText)findViewById(R.id.etQuantity);
        etQty.addTextChangedListener(textWatcher);
        
        etPrice = (EditText)findViewById(R.id.etPrice);
        etPrice.addTextChangedListener(textWatcher);
        etDocNo = (EditText)findViewById(R.id.etDocumentNo);
        ckIsFullRefuel = (CheckBox) findViewById(R.id.ckIsFullRefuel);
        tvConvertedAmountLabel = (TextView)findViewById(R.id.tvConvertedAmountLabel);
        etConversionRate = (EditText)findViewById(R.id.etConversionRate);
        etConversionRate.addTextChangedListener(textWatcher);
        tvAmountValue = (TextView)findViewById(R.id.tvAmountValue);
        llConversionRateZone = (LinearLayout)findViewById(R.id.conversionRateZone);
//        setConversionRateVisibility(false);

        llBaseUOMQtyZone = (LinearLayout)findViewById(R.id.llBaseUOMQtyZone);
        tvBaseUOMQtyLabel = (TextView)findViewById(R.id.tvBaseUOMQtyLabel);
        tvBaseUOMQtyValue = (TextView)findViewById(R.id.tvBaseUOMQtyValue);
        setBaseUOMQtyZoneVisibility(false);

        long mCarId;
        long mDriverId;
        long mExpCategoryId;
        long mExpTypeId;

        carDefaultCurrencyId = mPreferences.getLong("CarCurrency_ID", -1);
        carDefaultCurrencyCode = mDbAdapter.fetchRecord(MainDbAdapter.CURRENCY_TABLE_NAME, MainDbAdapter.currencyTableColNames,
                carDefaultCurrencyId).getString(MainDbAdapter.CURRENCY_COL_CODE_POS);
        currencyCode = carDefaultCurrencyCode;
        currencyConversionRate = BigDecimal.ONE;
        uomVolumeConversionRate = BigDecimal.ONE;

        if (operationType.equals("E")) {
            mRowId = mbundleExtras.getLong( MainDbAdapter.GEN_COL_ROWID_NAME );
            Cursor recordCursor = mDbAdapter.fetchRecord(MainDbAdapter.REFUEL_TABLE_NAME, MainDbAdapter.refuelTableColNames, mRowId);
            mCarId = recordCursor.getLong(MainDbAdapter.REFUEL_COL_CAR_ID_POS);
            mDriverId = recordCursor.getLong(MainDbAdapter.REFUEL_COL_DRIVER_ID_POS);
            mExpCategoryId = recordCursor.getLong(MainDbAdapter.REFUEL_COL_EXPENSECATEGORY_ID_POS);
            mExpTypeId = recordCursor.getLong(MainDbAdapter.REFUEL_COL_EXPENSETYPE_ID_POS);
            mUomVolumeId = recordCursor.getLong(MainDbAdapter.REFUEL_COL_UOMVOLUMEENTERED_ID_POS);
//            mUomVolumeId = recordCursor.getLong(MainDbAdapter.REFUEL_COL_UOMVOLUME_ID_POS);
            mCurrencyId = recordCursor.getLong(MainDbAdapter.REFUEL_COL_CURRENCYENTERED_ID_POS);
//            mCurrencyId = recordCursor.getLong(MainDbAdapter.REFUEL_COL_CURRENCY_ID_POS);
            currencyConversionRate = new BigDecimal(recordCursor.getString(MainDbAdapter.REFUEL_COL_CURRENCYRATE_POS));
            etConversionRate.setText(currencyConversionRate.toString());
            initDateTime(recordCursor.getLong(MainDbAdapter.REFUEL_COL_DATE_POS) * 1000);
            etCarIndex.setText(recordCursor.getString(MainDbAdapter.REFUEL_COL_INDEX_POS));
            etQty.setText(recordCursor.getString(MainDbAdapter.REFUEL_COL_QUANTITYENTERED_POS));
//            qtyEntry.setText(recordCursor.getString(MainDbAdapter.REFUEL_COL_QUANTITY_POS));
            etPrice.setText(recordCursor.getString(MainDbAdapter.REFUEL_COL_PRICEENTERED_POS));
//            priceEntry.setText(recordCursor.getString(MainDbAdapter.REFUEL_COL_PRICE_POS));
            etDocNo.setText(recordCursor.getString(MainDbAdapter.REFUEL_COL_DOCUMENTNO_POS));
            acUserComment.setText(recordCursor.getString(MainDbAdapter.GEN_COL_USER_COMMENT_POS));
            ckIsFullRefuel.setChecked(recordCursor.getString(MainDbAdapter.REFUEL_COL_ISFULLREFUEL_POS).equals("Y"));

            carDefaultUOMVolumeId = mDbAdapter.fetchRecord(MainDbAdapter.CAR_TABLE_NAME, MainDbAdapter.carTableColNames,
                    mCarId).getLong(MainDbAdapter.CAR_COL_UOMVOLUME_ID_POS);
            carDefaultUOMVolumeCode = mDbAdapter.fetchRecord(MainDbAdapter.UOM_TABLE_NAME, MainDbAdapter.uomTableColNames,
                    carDefaultUOMVolumeId).getString(MainDbAdapter.UOM_COL_CODE_POS);
            uomVolumeConversionRate = new BigDecimal(recordCursor.getString(MainDbAdapter.REFUEL_COL_UOMVOLCONVERSIONRATE_POS));
            if(carDefaultUOMVolumeId != mUomVolumeId){
                tvBaseUOMQtyValue.setText(recordCursor.getString(MainDbAdapter.REFUEL_COL_QUANTITY_POS) +
                        " " + carDefaultUOMVolumeCode);
                setBaseUOMQtyZoneVisibility(true);
            }
            if(mCurrencyId != carDefaultCurrencyId){
                currencyCode = mDbAdapter.fetchRecord(MainDbAdapter.CURRENCY_TABLE_NAME, MainDbAdapter.currencyTableColNames,
                    mCurrencyId).getString(MainDbAdapter.CURRENCY_COL_CODE_POS);
//                setConversionRateVisibility(true);
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
            ckIsFullRefuel.setChecked(false);
            carDefaultUOMVolumeId = mDbAdapter.fetchRecord(MainDbAdapter.CAR_TABLE_NAME, MainDbAdapter.carTableColNames,
                    mCarId).getLong(MainDbAdapter.CAR_COL_UOMVOLUME_ID_POS);
            carDefaultUOMVolumeCode = mDbAdapter.fetchRecord(MainDbAdapter.UOM_TABLE_NAME, MainDbAdapter.uomTableColNames,
                    carDefaultUOMVolumeId).getString(MainDbAdapter.UOM_COL_CODE_POS);
        }

        tvBaseUOMQtyLabel.setText(mResource.getString(R.string.REFUEL_BASEUOMQTY_LABEL));
        
        initSpinner(spnCar, MainDbAdapter.CAR_TABLE_NAME, MainDbAdapter.genColName,
                new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, MainDbAdapter.isActiveCondition, MainDbAdapter.GEN_COL_NAME_NAME,
                mCarId, false);

        initSpinner(spnDriver, MainDbAdapter.DRIVER_TABLE_NAME, MainDbAdapter.genColName,
                new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, MainDbAdapter.isActiveCondition, MainDbAdapter.GEN_COL_NAME_NAME,
                mDriverId, false);

        initSpinner(spnExpType, MainDbAdapter.EXPENSETYPE_TABLE_NAME, MainDbAdapter.genColName,
                new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, MainDbAdapter.isActiveCondition, MainDbAdapter.GEN_COL_NAME_NAME,
                mExpTypeId, true);

        initSpinner(spnExpCategory, MainDbAdapter.EXPENSECATEGORY_TABLE_NAME, MainDbAdapter.genColName,
                new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, MainDbAdapter.isActiveCondition, MainDbAdapter.GEN_COL_NAME_NAME,
                mExpCategoryId, true);

        initSpinner(spnUomVolume, MainDbAdapter.UOM_TABLE_NAME,
                MainDbAdapter.uomTableColNames, new String[]{MainDbAdapter.UOM_COL_CODE_NAME},
                MainDbAdapter.UOM_COL_UOMTYPE_NAME + "='" + StaticValues.UOM_VOLUME_TYPE_CODE + "'" +
                    MainDbAdapter.isActiveWithAndCondition, MainDbAdapter.UOM_COL_CODE_NAME,
                    mUomVolumeId, false);
        initSpinner(spnCurrency, MainDbAdapter.CURRENCY_TABLE_NAME,
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
        isActivityOnLoading = true;

        if(carDefaultCurrencyId != mCurrencyId)
            setConversionRateVisibility(true);
        else
            setConversionRateVisibility(false);

        if(carDefaultUOMVolumeId != mUomVolumeId)
            setBaseUOMQtyZoneVisibility(true);
        else
            setBaseUOMQtyZoneVisibility(false);

        calculateAmount();
        calculateBaseUOMQty();
    }


    private View.OnClickListener mOkClickListener =
            new View.OnClickListener()
                {
                    public void onClick( View v )
                    {
                        String retVal = checkMandatory((ViewGroup) findViewById(R.id.vgRoot));
                        if( retVal != null ) {
                            Toast toast = Toast.makeText( getApplicationContext(),
                                    mResource.getString( R.string.GEN_FILL_MANDATORY ) + ": " + retVal, Toast.LENGTH_SHORT );
                            toast.show();
                            return;
                        }

                        ContentValues data = new ContentValues();
                        data.put( MainDbAdapter.GEN_COL_NAME_NAME,
                                "Refuel");
                        data.put( MainDbAdapter.GEN_COL_ISACTIVE_NAME, "Y");
                        data.put( MainDbAdapter.GEN_COL_USER_COMMENT_NAME,
                                acUserComment.getText().toString() );
                        data.put( MainDbAdapter.REFUEL_COL_CAR_ID_NAME,
                                spnCar.getSelectedItemId() );
                        data.put( MainDbAdapter.REFUEL_COL_DRIVER_ID_NAME,
                                spnDriver.getSelectedItemId() );
                        data.put( MainDbAdapter.REFUEL_COL_EXPENSECATEGORY_NAME,
                                spnExpCategory.getSelectedItemId() );
                        data.put( MainDbAdapter.REFUEL_COL_EXPENSETYPE_ID_NAME,
                                spnExpType.getSelectedItemId() );
                        data.put( MainDbAdapter.REFUEL_COL_INDEX_NAME, etCarIndex.getText().toString());
                        data.put( MainDbAdapter.REFUEL_COL_QUANTITYENTERED_NAME, etQty.getText().toString());
                        data.put( MainDbAdapter.REFUEL_COL_UOMVOLUMEENTERED_ID_NAME,
                                spnUomVolume.getSelectedItemId());
                        data.put( MainDbAdapter.REFUEL_COL_PRICEENTERED_NAME, etPrice.getText().toString());
                        data.put( MainDbAdapter.REFUEL_COL_CURRENCYENTERED_ID_NAME,
                                spnCurrency.getSelectedItemId());
                        data.put( MainDbAdapter.REFUEL_COL_DATE_NAME, mlDateTimeInSeconds);
                        data.put( MainDbAdapter.REFUEL_COL_DOCUMENTNO_NAME,
                                etDocNo.getText().toString());
                        data.put( MainDbAdapter.REFUEL_COL_ISFULLREFUEL_NAME,
                                (ckIsFullRefuel.isChecked() ? "Y" : "N"));

                        if(mUomVolumeId == carDefaultUOMVolumeId){
                            data.put( MainDbAdapter.REFUEL_COL_QUANTITY_NAME, etQty.getText().toString());
                            data.put( MainDbAdapter.REFUEL_COL_UOMVOLUME_ID_NAME, spnUomVolume.getSelectedItemId());
                            data.put( MainDbAdapter.REFUEL_COL_UOMVOLCONVERSIONRATE_NAME, "1");
                        }
                        else{
                            data.put( MainDbAdapter.REFUEL_COL_QUANTITY_NAME, baseUomQty.toString());
                            data.put( MainDbAdapter.REFUEL_COL_UOMVOLUME_ID_NAME, carDefaultUOMVolumeId);
                            data.put( MainDbAdapter.REFUEL_COL_UOMVOLCONVERSIONRATE_NAME, uomVolumeConversionRate.toString());
                        }

                        if(mCurrencyId == carDefaultCurrencyId){
                            data.put( MainDbAdapter.REFUEL_COL_PRICE_NAME, etPrice.getText().toString());
                            data.put( MainDbAdapter.REFUEL_COL_CURRENCY_ID_NAME, carDefaultCurrencyId);
                            data.put( MainDbAdapter.REFUEL_COL_CURRENCYRATE_NAME, "1");
                        }
                        else{
                            data.put( MainDbAdapter.REFUEL_COL_PRICE_NAME, baseFuelPrice.toString());
                            data.put( MainDbAdapter.REFUEL_COL_CURRENCY_ID_NAME, carDefaultCurrencyId);
                            data.put( MainDbAdapter.REFUEL_COL_CURRENCYRATE_NAME, currencyConversionRate.toString());
                        }

                        if( operationType.equals("N") ) {
                            Long createResult = mDbAdapter.createRecord(MainDbAdapter.REFUEL_TABLE_NAME, data);
                            if(createResult.intValue() < 0){
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
                            int updResult = mDbAdapter.updateRecord(MainDbAdapter.REFUEL_TABLE_NAME, mRowId, data);
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
                    userCommentAdapter = new ArrayAdapter<String>(RefuelEditActivity.this,
                            android.R.layout.simple_dropdown_item_1line,
                            mDbAdapter.getAutoCompleteUserComments(MainDbAdapter.REFUEL_TABLE_NAME,
                            spnCar.getSelectedItemId(), 30));
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
                    mCurrencyId = spnCurrency.getSelectedItemId();
                    if(mCurrencyId != carDefaultCurrencyId){
                        setConversionRateVisibility(true);
                    }
                    else{
                        setConversionRateVisibility(false);
                    }
                    currencyCode = mDbAdapter.fetchRecord(MainDbAdapter.CURRENCY_TABLE_NAME, MainDbAdapter.currencyTableColNames,
                            mCurrencyId).getString(MainDbAdapter.CURRENCY_COL_CODE_POS);
                    currencyConversionRate = mDbAdapter.getCurrencyRate(mCurrencyId, carDefaultCurrencyId);
                    etConversionRate.setText("");
                    if(currencyConversionRate != null){
                        etConversionRate.append(currencyConversionRate.toString());
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
                    mUomVolumeId = spnUomVolume.getSelectedItemId();
                    if(mUomVolumeId != carDefaultUOMVolumeId){
                        setBaseUOMQtyZoneVisibility(true);
                    }
                    else{
                        setBaseUOMQtyZoneVisibility(false);
                    }

                    uomVolumeConversionRate = mDbAdapter.getUOMConvertionRate(mUomVolumeId, carDefaultUOMVolumeId);
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

    private TextWatcher textWatcher =
        new TextWatcher() {

            public void beforeTextChanged(CharSequence cs, int i, int i1, int i2) {
                return;
            }

            public void onTextChanged(CharSequence cs, int i, int i1, int i2) {
                return;
            }

            public void afterTextChanged(Editable edtbl) {
                if(etConversionRate.getText().toString() != null &&
                        etConversionRate.getText().toString().length() > 0)
                    currencyConversionRate = new BigDecimal(etConversionRate.getText().toString());
                if(mUomVolumeId != carDefaultUOMVolumeId && etQty.getText().toString() != null &&
                        etQty.toString().length() > 0)
                    calculateBaseUOMQty();
                calculateAmount();
            }
        };

    private void calculateAmount() {
        String qtyStr = etQty.getText().toString();
        String priceStr = etPrice.getText().toString();
        String amountStr = "";
        convertedAmount = null;
        if(qtyStr != null && qtyStr.length() > 0
                && priceStr != null && priceStr.length() > 0) {
            BigDecimal amount = (new BigDecimal(qtyStr)).multiply(new BigDecimal(priceStr))
                    .setScale(StaticValues.DECIMALS_AMOUNT, StaticValues.ROUNDING_MODE_AMOUNT);
            amountStr = amount.toString() + " " + currencyCode;
            tvAmountValue.setText(amountStr);
            if(carDefaultCurrencyId != mCurrencyId && currencyConversionRate != null){
                convertedAmount = amount.multiply(currencyConversionRate).
                        setScale(StaticValues.DECIMALS_AMOUNT, StaticValues.ROUNDING_MODE_AMOUNT);
                baseFuelPrice = (new BigDecimal(priceStr)).multiply(currencyConversionRate).
                        setScale(StaticValues.DECIMALS_AMOUNT, StaticValues.ROUNDING_MODE_AMOUNT);

                amountStr = convertedAmount.toString() + " " + carDefaultCurrencyCode;
                tvConvertedAmountLabel.setText(mResource.getString(R.string.GEN_CONVERTEDAMOUNT_LABEL) + amountStr);
            }

            
        }
    }

    private void calculateBaseUOMQty() {
        if(mUomVolumeId == carDefaultUOMVolumeId){
            return;
        }

        if(uomVolumeConversionRate == null){
            tvBaseUOMQtyValue.setText(mResource.getString(R.string.REFUEL_NOUOMCONVERSION_MSG));
            return;
        }
        String qtyStr = etQty.getText().toString();
        String amountStr;
        if(qtyStr != null && qtyStr.length() > 0) {
            baseUomQty = (new BigDecimal(qtyStr)).multiply(uomVolumeConversionRate)
                    .setScale(StaticValues.DECIMALS_VOLUME, StaticValues.ROUNDING_MODE_VOLUME);
            amountStr = baseUomQty.toString() + " " + carDefaultUOMVolumeCode;

            tvBaseUOMQtyValue.setText(amountStr);
        }
    }

    private void setConversionRateVisibility(boolean visible){
        if(visible){
            llConversionRateZone.setVisibility(View.VISIBLE);
            etConversionRate.setTag(mResource.getString(R.string.GEN_CONVRATE_LABEL));
        }else{
            llConversionRateZone.setVisibility(View.GONE);
            etConversionRate.setTag(null);
        }
    }

    private void setBaseUOMQtyZoneVisibility(boolean visible){
        if(visible)
            llBaseUOMQtyZone.setVisibility(View.VISIBLE);
        else
            llBaseUOMQtyZone.setVisibility(View.GONE);
    }

}
