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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import java.math.BigDecimal;
import org.andicar.utils.AndiCarStatistics;

/**
 *
 * @author miki
 */
public class ExpenseEditActivity extends EditActivityBase {
    private AutoCompleteTextView acUserComment;
    private AutoCompleteTextView acBPartner;
    private AutoCompleteTextView acAdress;
    private AutoCompleteTextView acTag;
    private Spinner spnCar;
    private Spinner spnDriver;
    private Spinner spnCurrency;
    private Spinner spnExpType;
    private Spinner spnExpCategory;
    private Spinner spnUOM;
    private EditText etCarIndex;
    private EditText etUserInput;
    private EditText etDocNo;
    private EditText etConversionRate;
    private EditText etQuantity;
    private TextView tvWarningLabel;
    private TextView tvConvertedAmountValue;
    private TextView tvConvertedAmountLabel;
    private TextView tvCalculatedTextLabel;
    private TextView tvCalculatedTextContent;

    private LinearLayout llConversionRateZone;

    private long mCurrencyId;
    private long carDefaultCurrencyId;
    private long mExpCategoryId = 0;
    private long mExpTypeId = 0;
    private long mUOMId = -1;
    private long mBPartnerId = 0;
    private long mAddressId = 0;
    private long mTagId = 0;
    private String carDefaultCurrencyCode = null;
    private String operationType = null;
    private BigDecimal conversionRate = BigDecimal.ONE;
    private BigDecimal convertedAmount = null;
    private BigDecimal convertedPrice = null;
    private BigDecimal price = null;
    private BigDecimal quantity = null;
    private BigDecimal amount = null;
    private RadioButton rbInsertModePrice;
    private boolean isActivityOnLoading = true;

    private ArrayAdapter<String> userCommentAdapter;
    private ArrayAdapter<String> bpartnerNameAdapter;
    private ArrayAdapter<String> addressAdapter;
    private ArrayAdapter<String> tagAdapter;
    private static int INSERTMODE_PRICE = 0;
    private static int INSERTMODE_AMOUNT = 1;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        if(icicle !=null)
            return; //restored from previous state

        operationType = mBundleExtras.getString("Operation");

        init();

        if (operationType.equals("E")) {
            mRowId = mBundleExtras.getLong( MainDbAdapter.GEN_COL_ROWID_NAME );
            Cursor c = mDbAdapter.fetchRecord(MainDbAdapter.EXPENSE_TABLE_NAME,
                    MainDbAdapter.expenseTableColNames, mRowId);
            mCarId = c.getLong(MainDbAdapter.EXPENSE_COL_CAR_ID_POS);
            mDriverId = c.getLong(MainDbAdapter.EXPENSE_COL_DRIVER_ID_POS);
            mExpCategoryId = c.getLong(MainDbAdapter.EXPENSE_COL_EXPENSECATEGORY_POS);
            mExpTypeId = c.getLong(MainDbAdapter.EXPENSE_COL_EXPENSETYPE_ID_POS);
            initDateTime(c.getLong(MainDbAdapter.EXPENSE_COL_DATE_POS) * 1000);
            etCarIndex.setText(c.getString(MainDbAdapter.EXPENSE_COL_INDEX_POS));

            mCurrencyId = c.getLong(MainDbAdapter.EXPENSE_COL_CURRENCYENTERED_ID_POS);
            if(mCurrencyId == carDefaultCurrencyId)
                setConversionRateZoneVisible(false);
            else
                setConversionRateZoneVisible(true);

            etUserInput.setText(c.getString(MainDbAdapter.EXPENSE_COL_AMOUNTENTERED_POS));
            try{
                conversionRate = new BigDecimal(c.getString(MainDbAdapter.EXPENSE_COL_CURRENCYRATE_POS));
            }
            catch(NumberFormatException e){}
            etConversionRate.setText(conversionRate.toString());
            tvConvertedAmountValue.setText(c.getString(MainDbAdapter.EXPENSE_COL_AMOUNT_POS));

            etDocNo.setText(c.getString(MainDbAdapter.EXPENSE_COL_DOCUMENTNO_POS));
            acUserComment.setText(c.getString(MainDbAdapter.GEN_COL_USER_COMMENT_POS));
            etQuantity.setText(c.getString(MainDbAdapter.EXPENSE_COL_QUANTITY_POS));
            if(c.getString(MainDbAdapter.EXPENSE_COL_UOM_ID_POS) != null
                    && c.getString(MainDbAdapter.EXPENSE_COL_UOM_ID_POS).length() > 0){
                mUOMId = c.getLong(MainDbAdapter.EXPENSE_COL_UOM_ID_POS);
            }
            else
                mUOMId = -1;
            
            String fromTable = c.getString(MainDbAdapter.EXPENSE_COL_FROMTABLE_POS);
            if(fromTable == null){
                tvWarningLabel.setText("");
                setEditable((ViewGroup) findViewById(R.id.vgRoot), true);
            }
            else{
                if(fromTable.equals("Refuel")){
                    tvWarningLabel.setText(
                            mResource.getString(R.string.ExpenseEditActivity_CreatedFromWarning)
                                .replace("[%1]", mResource.getString(R.string.GEN_Refuel).toLowerCase())
                                .replace("[%1]", mResource.getString(R.string.GEN_Refuel).toLowerCase())+ "\n");
                }
                setEditable((ViewGroup) findViewById(R.id.vgRoot), false);
            }
            Cursor c2 = null;
            //bpartner
            if(c.getString(MainDbAdapter.EXPENSE_COL_BPARTNER_ID_POS) != null
                    && c.getString(MainDbAdapter.EXPENSE_COL_BPARTNER_ID_POS).length() > 0){
                mBPartnerId = c.getLong(MainDbAdapter.EXPENSE_COL_BPARTNER_ID_POS);
                String selection = MainDbAdapter.GEN_COL_ROWID_NAME + "= ? ";
                String[] selectionArgs = {Long.toString(mBPartnerId)};
                c2 = mDbAdapter.query(MainDbAdapter.BPARTNER_TABLE_NAME, MainDbAdapter.genColName, 
                            selection, selectionArgs, null, null, null);
                if(c2.moveToFirst())
                    acBPartner.setText(c2.getString(MainDbAdapter.GEN_COL_NAME_POS));
                c2.close();

                if(c.getString(MainDbAdapter.EXPENSE_COL_BPARTNER_LOCATION_ID_POS) != null
                        && c.getString(MainDbAdapter.EXPENSE_COL_BPARTNER_LOCATION_ID_POS).length() > 0){
                    mAddressId = c.getLong(MainDbAdapter.EXPENSE_COL_BPARTNER_LOCATION_ID_POS);
                    selectionArgs[0] = Long.toString(mAddressId);
                    c2 = mDbAdapter.query(MainDbAdapter.BPARTNER_LOCATION_TABLE_NAME, MainDbAdapter.bpartnerLocationTableColNames, 
                            selection, selectionArgs, null, null, null);
                    if(c2.moveToFirst())
                        acAdress.setText(c2.getString(MainDbAdapter.BPARTNER_LOCATION_ADDRESS_POS));
                    c2.close();
                }
            }
            else{
                acAdress.setEnabled(false);
                acAdress.setText(null);
                acAdress.setHint(mResource.getString(R.string.GEN_BPartner).replace(":", "") + " " +
                        mResource.getString(R.string.GEN_Required).replace(":", ""));
            }
            
            //fill tag
            if(c.getString(MainDbAdapter.EXPENSE_COL_TAG_ID_POS) != null
                    && c.getString(MainDbAdapter.EXPENSE_COL_TAG_ID_POS).length() > 0){
                mTagId = c.getLong(MainDbAdapter.EXPENSE_COL_TAG_ID_POS);
                String selection = MainDbAdapter.GEN_COL_ROWID_NAME + "= ? ";
                String[] selectionArgs = {Long.toString(mTagId)};
                c2 = mDbAdapter.query(MainDbAdapter.TAG_TABLE_NAME, MainDbAdapter.genColName,
                            selection, selectionArgs, null, null, null);
                if(c2.moveToFirst())
                    acTag.setText(c2.getString(MainDbAdapter.GEN_COL_NAME_POS));
                c2.close();
            }
            c.close();
        }
        else {
            tvWarningLabel.setText("");
            mCarId = mPreferences.getLong("CurrentCar_ID", -1);
            mDriverId = mPreferences.getLong("LastDriver_ID", -1);
            mCurrencyId = mPreferences.getLong("CarCurrency_ID", -1);
            initDateTime(System.currentTimeMillis());
            setEditable((ViewGroup) findViewById(R.id.vgRoot), true);
            setConversionRateZoneVisible(false);
            acAdress.setEnabled(false);
            acAdress.setText(null);
            acAdress.setHint(mResource.getString(R.string.GEN_BPartner).replace(":", "") + " " +
                    mResource.getString(R.string.GEN_Required).replace(":", ""));

            //init tag
            if(mPreferences.getBoolean("RememberLastTag", false) && mPreferences.getLong("LastTagId", 0) > 0){
	            mTagId = mPreferences.getLong("LastTagId", 0);
	            String selection = MainDbAdapter.GEN_COL_ROWID_NAME + "= ? ";
	            String[] selectionArgs = {Long.toString(mTagId)};
	            Cursor c = mDbAdapter.query(MainDbAdapter.TAG_TABLE_NAME, MainDbAdapter.genColName,
	                        selection, selectionArgs, null, null, null);
	            if(c.moveToFirst())
	                acTag.setText(c.getString(MainDbAdapter.GEN_COL_NAME_POS));
	            c.close();
            }
            
        }

        initControls();
        setInsertMode(INSERTMODE_AMOUNT);
        calculatePrice();
        if(isSendStatistics)
            AndiCarStatistics.sendFlurryEvent(this, "ExpenseEdit", null);
    }

    private void initControls() {
        initSpinner(spnCar, MainDbAdapter.CAR_TABLE_NAME, MainDbAdapter.genColName, 
                new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, MainDbAdapter.isActiveCondition, null,
                MainDbAdapter.GEN_COL_NAME_NAME, mCarId, false);
        initSpinner(spnDriver, MainDbAdapter.DRIVER_TABLE_NAME, MainDbAdapter.genColName, 
                new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, MainDbAdapter.isActiveCondition, null,
                MainDbAdapter.GEN_COL_NAME_NAME, mDriverId, false);
        initSpinner(spnExpType, MainDbAdapter.EXPENSETYPE_TABLE_NAME, MainDbAdapter.genColName, 
                new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, MainDbAdapter.isActiveCondition, null,
                MainDbAdapter.GEN_COL_NAME_NAME, mExpTypeId, false);
        initSpinner(spnExpCategory, MainDbAdapter.EXPENSECATEGORY_TABLE_NAME, MainDbAdapter.genColName, 
                new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, MainDbAdapter.isActiveCondition, null,
                MainDbAdapter.GEN_COL_NAME_NAME, mExpCategoryId, false);
        initSpinner(spnCurrency, MainDbAdapter.CURRENCY_TABLE_NAME, MainDbAdapter.genColName,
                new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, MainDbAdapter.isActiveCondition, null,
                MainDbAdapter.GEN_COL_NAME_NAME, mCurrencyId, false);
        initSpinner(spnUOM, MainDbAdapter.UOM_TABLE_NAME, MainDbAdapter.genColName,
                new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, MainDbAdapter.isActiveCondition, null,
                MainDbAdapter.GEN_COL_NAME_NAME, mUOMId, true);
        userCommentAdapter = new ArrayAdapter<String>(ExpenseEditActivity.this, android.R.layout.simple_dropdown_item_1line, 
                mDbAdapter.getAutoCompleteText(MainDbAdapter.EXPENSE_TABLE_NAME, null,
                mCarId, 30));
        acUserComment.setAdapter(userCommentAdapter);
        bpartnerNameAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line,
                mDbAdapter.getAutoCompleteText(MainDbAdapter.BPARTNER_TABLE_NAME, null,
                0, 0));
        acBPartner.setAdapter(bpartnerNameAdapter);
        addressAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line,
                mDbAdapter.getAutoCompleteText(MainDbAdapter.BPARTNER_LOCATION_TABLE_NAME, MainDbAdapter.BPARTNER_LOCATION_ADDRESS_NAME,
                mBPartnerId, 0));
        acAdress.setAdapter(addressAdapter);
        tagAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line,
                mDbAdapter.getAutoCompleteText(MainDbAdapter.TAG_TABLE_NAME, null,
                0, 0));
        acTag.setAdapter(tagAdapter);
    }

    private void init() {
        acUserComment = ((AutoCompleteTextView) findViewById( R.id.acUserComment ));
        acBPartner = ((AutoCompleteTextView) findViewById( R.id.acBPartner ));
        acBPartner.setOnFocusChangeListener(vendorChangeListener);
        acBPartner.addTextChangedListener(bPartnerTextWatcher);
        acAdress = ((AutoCompleteTextView) findViewById( R.id.acAdress ));
        acTag = ((AutoCompleteTextView) findViewById( R.id.acTag ));
        spnCar = (Spinner) findViewById(R.id.spnCar);
        spnDriver = (Spinner) findViewById(R.id.spnDriver);
        spnCar.setOnItemSelectedListener(spinnerCarDriverOnItemSelectedListener);
        spnDriver.setOnItemSelectedListener(spinnerCarDriverOnItemSelectedListener);
        spnCar.setOnTouchListener(spinnerOnTouchListener);
        spnDriver.setOnTouchListener(spinnerOnTouchListener);
        spnCurrency = (Spinner) findViewById(R.id.spnCurrency);
        spnCurrency.setOnItemSelectedListener(spinnerCurrencyOnItemSelectedListener);
        spnCurrency.setOnTouchListener(spinnerOnTouchListener);
        spnExpType = (Spinner) findViewById(R.id.spnExpType);
        spnExpCategory = (Spinner) findViewById(R.id.spnExpCategory);
        spnUOM = (Spinner) findViewById(R.id.spnUOM);
        etCarIndex = (EditText) findViewById(R.id.etIndex);
        etUserInput = (EditText) findViewById(R.id.etUserInput);
        etUserInput.addTextChangedListener(userInputTextWatcher);
        etDocNo = (EditText) findViewById(R.id.etDocumentNo);
        tvWarningLabel = (TextView) findViewById(R.id.tvWarningLabel);
        llConversionRateZone = (LinearLayout) findViewById(R.id.llConversionRateZone);
        etConversionRate = (EditText) findViewById(R.id.etConversionRate);
        etConversionRate.addTextChangedListener(userInputTextWatcher);
        etQuantity = (EditText) findViewById(R.id.etQuantity);
        etQuantity.addTextChangedListener(userInputTextWatcher);
        tvCalculatedTextLabel = (TextView) findViewById(R.id.tvCalculatedTextLabel);
        tvCalculatedTextContent = (TextView) findViewById(R.id.tvCalculatedTextContent);
        tvConvertedAmountValue = (TextView) findViewById(R.id.tvConvertedAmountValue);
        tvConvertedAmountLabel = (TextView) findViewById(R.id.tvConvertedAmountLabel);
        rbInsertModePrice = (RadioButton) findViewById(R.id.rbInsertModePrice);
        RadioGroup rg = (RadioGroup) findViewById(R.id.rgInsertMode);
        rg.setOnCheckedChangeListener(rgOnCheckedChangeListener);
        carDefaultCurrencyId = mPreferences.getLong("CarCurrency_ID", -1);
        carDefaultCurrencyCode = mDbAdapter.getCurrencyCode(carDefaultCurrencyId);
    }

    //change the address autocomplete list when the vendor change
    private View.OnFocusChangeListener vendorChangeListener = new View.OnFocusChangeListener() {
        public void onFocusChange(View view, boolean hasFocus) {
            if(!hasFocus){
                String selection = "UPPER (" + MainDbAdapter.GEN_COL_NAME_NAME + ") = ?";
                String[] selectionArgs = {acBPartner.getText().toString().toUpperCase()};
                Cursor c = mDbAdapter.query(MainDbAdapter.BPARTNER_TABLE_NAME, MainDbAdapter.genColName, selection, selectionArgs, 
                        null, null, null);
//                        fetchForTable(MainDbAdapter.BPARTNER_TABLE_NAME, MainDbAdapter.genColName,
//                            "UPPER(" + MainDbAdapter.GEN_COL_NAME_NAME + ") = '" + acBPartner.getText().toString().toUpperCase() + "'", null);
                String bPartnerIdStr = null;
                if(c.moveToFirst())
                    bPartnerIdStr = c.getString(MainDbAdapter.GEN_COL_ROWID_POS);
                c.close();
                if(bPartnerIdStr != null && bPartnerIdStr.length() > 0)
                    mBPartnerId = Long.parseLong(bPartnerIdStr);
                else
                    mBPartnerId = 0;
                addressAdapter = new ArrayAdapter<String>(ExpenseEditActivity.this, android.R.layout.simple_dropdown_item_1line,
                        mDbAdapter.getAutoCompleteText(MainDbAdapter.BPARTNER_LOCATION_TABLE_NAME, MainDbAdapter.BPARTNER_LOCATION_ADDRESS_NAME,
                        mBPartnerId, 0));
                acAdress.setAdapter(addressAdapter);
            }
        }
    };

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        try{
            init();
            super.onRestoreInstanceState(savedInstanceState);
            mCurrencyId = savedInstanceState.getLong("mCurrencyId");
            carDefaultCurrencyId = savedInstanceState.getLong("carDefaultCurrencyId");
            mCarId = savedInstanceState.getLong("mCarId");
            mDriverId = savedInstanceState.getLong("mDriverId");
            mExpCategoryId = savedInstanceState.getLong("mExpCategoryId");
            mExpTypeId = savedInstanceState.getLong("mExpTypeId");
            mUOMId = savedInstanceState.getLong("mUOMId");
            if(savedInstanceState.containsKey("carDefaultCurrencyCode"))
                carDefaultCurrencyCode = savedInstanceState.getString("carDefaultCurrencyCode");
            if(savedInstanceState.containsKey("operationType"))
                operationType = savedInstanceState.getString("operationType");
            if(savedInstanceState.containsKey("conversionRate"))
                conversionRate = new BigDecimal(savedInstanceState.getString("conversionRate"));
            if(savedInstanceState.containsKey("convertedAmount"))
                convertedAmount = new BigDecimal(savedInstanceState.getString("convertedAmount"));

            initControls();
//            calculateConvertedAmount();
            initDateTime(mlDateTimeInSeconds * 1000);
        }
        catch(NumberFormatException e){}
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("mCurrencyId", mCurrencyId);
        outState.putLong("carDefaultCurrencyId", carDefaultCurrencyId);
        outState.putLong("mCarId", mCarId);
        outState.putLong("mDriverId", mDriverId);
        outState.putLong("mExpCategoryId", spnExpCategory.getSelectedItemId());
        outState.putLong("mExpTypeId", spnExpType.getSelectedItemId());
        outState.putLong("mUOMId", spnUOM.getSelectedItemId());

        if(carDefaultCurrencyCode != null)
            outState.putString("carDefaultCurrencyCode", carDefaultCurrencyCode);
        if(operationType != null)
            outState.putString("operationType", operationType);
        if(conversionRate != null)
            outState.putString("conversionRate", conversionRate.toString());
        if(convertedAmount != null)
            outState.putString("convertedAmount", convertedAmount.toString());

    }

    @Override
    protected void onResume() {
        super.onResume();
        isActivityOnLoading = true;
        if(mCurrencyId != carDefaultCurrencyId)
        {
            setConversionRateZoneVisible(true);
//            if(conversionRate != null)
//                calculateConvertedAmount();
        }
        else
            setConversionRateZoneVisible(false);
        if(acBPartner.getText().toString() == null || acBPartner.getText().toString().length() == 0){
            acAdress.setEnabled(false);
            acAdress.setText(null);
            acAdress.setHint(mResource.getString(R.string.GEN_BPartner).replace(":", "") + " " +
                                mResource.getString(R.string.GEN_Required).replace(":", ""));
        }
        else{
            acAdress.setEnabled(true);
            acAdress.setHint(null);
        }
    }

    private View.OnTouchListener spinnerOnTouchListener = new View.OnTouchListener() {

        public boolean onTouch(View view, MotionEvent me) {
            isActivityOnLoading = false;
            return false;
        }
    };

    private AdapterView.OnItemSelectedListener spinnerCarDriverOnItemSelectedListener =
            new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    if(isActivityOnLoading)
                        return;
                    mCarId = spnCar.getSelectedItemId();
                    mDriverId = spnDriver.getSelectedItemId();
                    userCommentAdapter = null;
                    userCommentAdapter = new ArrayAdapter<String>(ExpenseEditActivity.this,
                            android.R.layout.simple_dropdown_item_1line,
                            mDbAdapter.getAutoCompleteText(MainDbAdapter.EXPENSE_TABLE_NAME, null, mCarId, 30));
                    acUserComment.setAdapter(userCommentAdapter);
                    //change the currency
                    Long newCarCurrencyId = mDbAdapter.getCarCurrencyID(mCarId);

                    if(newCarCurrencyId != mCurrencyId){
                        initSpinner(spnCurrency, MainDbAdapter.CURRENCY_TABLE_NAME,
                                MainDbAdapter.genColName, new String[]{MainDbAdapter.GEN_COL_NAME_NAME},
                                    MainDbAdapter.isActiveCondition, null,
                                    MainDbAdapter.GEN_COL_NAME_NAME,
                                    newCarCurrencyId, false);
                        mCurrencyId = newCarCurrencyId;
                        carDefaultCurrencyId = mCurrencyId;
                        carDefaultCurrencyCode = mDbAdapter.getCurrencyCode(carDefaultCurrencyId);
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
                    setSpinnerTextToCode(arg0, arg3, arg1);
                    if(isActivityOnLoading)
                        return;
                    mCurrencyId = spnCurrency.getSelectedItemId();
                    if(mCurrencyId != carDefaultCurrencyId){
                        setConversionRateZoneVisible(true);
                    }
                    else{
                        setConversionRateZoneVisible(false);
                    }
                    conversionRate = mDbAdapter.getCurrencyRate(mCurrencyId, carDefaultCurrencyId);
                    etConversionRate.setText("");
                    tvConvertedAmountValue.setText("");
                    if(conversionRate != null){
                        etConversionRate.append(conversionRate.toString());
                    }

                }
                public void onNothingSelected(AdapterView<?> arg0) {
                }
            };

    private RadioGroup.OnCheckedChangeListener rgOnCheckedChangeListener  =
            new RadioGroup.OnCheckedChangeListener() {
                    public void onCheckedChanged(RadioGroup arg0, int checkedId) {
                        if(checkedId == rbInsertModePrice.getId()) {
                            setInsertMode(INSERTMODE_PRICE);
                        }
                        else {
                            setInsertMode(INSERTMODE_AMOUNT);
                        }
                    }
                };

    private void setInsertMode(int insertMode){
        if(insertMode == INSERTMODE_PRICE){
            tvCalculatedTextLabel.setText(mResource.getString(R.string.GEN_AmountLabel) + "=");
            etUserInput.setTag(mResource.getString(R.string.GEN_PriceLabel));
            etQuantity.setHint(mResource.getString(R.string.GEN_Required));
            calculateAmount();
        }
        else{
            tvCalculatedTextLabel.setText(mResource.getString(R.string.GEN_PriceLabel) + "=");
            etUserInput.setTag(mResource.getString(R.string.GEN_AmountLabel));
            etQuantity.setHint(null);
            calculatePrice();
            if(mCurrencyId != carDefaultCurrencyId)
                calculateConvertedAmount();
        }
    }

    private void setConversionRateZoneVisible(boolean isVisible){
        if(isVisible){
            llConversionRateZone.setVisibility(View.VISIBLE);
            tvConvertedAmountLabel.setText((mResource.getString(R.string.GEN_ConvertedAmountLabel))
                    .replace("[%1]", carDefaultCurrencyCode) + " = ");
            etConversionRate.setTag(mResource.getString(R.string.GEN_ConversionRateLabel));
        }
        else{
            etConversionRate.setTag(null);
            llConversionRateZone.setVisibility(View.GONE);
        }
    }

    private void calculateConvertedAmount() {
        if(conversionRate == null){
            tvConvertedAmountValue.setText("");
            return;
        }
        if(carDefaultCurrencyId == mCurrencyId)
            return;
        String convertedAmountStr = "";
        try{
            if(amount != null) {
                convertedAmount = amount.multiply(conversionRate).
                        setScale(StaticValues.DECIMALS_AMOUNT, StaticValues.ROUNDING_MODE_AMOUNT);
                convertedAmountStr = convertedAmount.toString() + " " + carDefaultCurrencyCode;
                tvConvertedAmountValue.setText(convertedAmountStr);
            }
            if(price != null) {
                convertedPrice = price.multiply(conversionRate).
                        setScale(StaticValues.DECIMALS_AMOUNT, StaticValues.ROUNDING_MODE_AMOUNT);
            }
        }
        catch(NumberFormatException e){}
    }

    private void calculatePrice() {
        String qtyStr = etQuantity.getText().toString();
        if(qtyStr == null || qtyStr.length() == 0)
            return;
        String amountStr = etUserInput.getText().toString();
        if(amountStr != null && amountStr.length() > 0) {
            try{
                amount = new BigDecimal(amountStr);
                quantity = new BigDecimal(qtyStr);
                if(quantity.signum() != 0){
	                price = amount.divide(quantity, StaticValues.DECIMALS_PRICE, StaticValues.ROUNDING_MODE_PRICE);
	                tvCalculatedTextContent.setText(price.toString());
                }
            }
            catch(NumberFormatException e){}
        }
    }

    private void calculateAmount() {
        String qtyStr = etQuantity.getText().toString();
        if(qtyStr == null || qtyStr.length() == 0)
            return;
        String priceStr = etUserInput.getText().toString();
        if(priceStr != null && priceStr.length() > 0) {
            try{
                price = new BigDecimal(priceStr);
                quantity = new BigDecimal(qtyStr);
                amount = price.multiply(quantity).setScale(StaticValues.DECIMALS_AMOUNT, StaticValues.ROUNDING_MODE_AMOUNT);
                tvCalculatedTextContent.setText(amount.toString());
            }
            catch(NumberFormatException e){}
        }
        if(mCurrencyId != carDefaultCurrencyId)
            calculateConvertedAmount();
    }

    private TextWatcher userInputTextWatcher =
        new TextWatcher() {

            public void beforeTextChanged(CharSequence cs, int i, int i1, int i2) {
                return;
            }

            public void onTextChanged(CharSequence cs, int i, int i1, int i2) {
                return;
            }

            public void afterTextChanged(Editable edtbl) {
                try{
                    if(rbInsertModePrice.isChecked()){
                        price = new BigDecimal(etUserInput.getText().toString());
                        calculateAmount();
                    }
                    else{
                        amount = new BigDecimal(etUserInput.getText().toString());
                        calculatePrice();
                    }
                    
                }
                catch(NumberFormatException e){}
                if(mCurrencyId != carDefaultCurrencyId){
                    try{
                        if(etConversionRate.getText().toString() != null && etConversionRate.getText().toString().length() > 0)
                                conversionRate = new BigDecimal(etConversionRate.getText().toString());
                        if(conversionRate != null)
                            calculateConvertedAmount();
                    }
                    catch(NumberFormatException e){}
                }
            }
        };

    private TextWatcher bPartnerTextWatcher =
        new TextWatcher() {

            public void beforeTextChanged(CharSequence cs, int i, int i1, int i2) {
                return;
            }

            public void onTextChanged(CharSequence cs, int i, int i1, int i2) {
                return;
            }

            public void afterTextChanged(Editable edtbl) {
                if(edtbl.toString() == null || edtbl.toString().length() == 0){
                    acAdress.setEnabled(false);
                    acAdress.setText(null);
                    acAdress.setHint(mResource.getString(R.string.GEN_BPartner).replace(":", "") + " " +
                                        mResource.getString(R.string.GEN_Required).replace(":", ""));
                }
                else{
                    acAdress.setEnabled(true);
                    acAdress.setHint(null);
                }
            }
        };

    @Override
    protected void saveData() {
        String strRetVal = checkMandatory(vgRoot);
        if( strRetVal != null ) {
            Toast toast = Toast.makeText( getApplicationContext(),
                    mResource.getString( R.string.GEN_FillMandatory ) + ": " + strRetVal, Toast.LENGTH_SHORT );
            toast.show();
            return;
        }

        strRetVal = checkNumeric(vgRoot, false);
        if( strRetVal != null ) {
            Toast toast = Toast.makeText( getApplicationContext(),
                    mResource.getString( R.string.GEN_NumberFormatException ) + ": " + strRetVal, Toast.LENGTH_SHORT );
            toast.show();
            return;
        }

        ContentValues data = new ContentValues();
        data.put( MainDbAdapter.GEN_COL_NAME_NAME,
                "Expense");
        data.put( MainDbAdapter.GEN_COL_ISACTIVE_NAME, "Y");
        data.put( MainDbAdapter.GEN_COL_USER_COMMENT_NAME,
                acUserComment.getText().toString() );
        data.put( MainDbAdapter.EXPENSE_COL_CAR_ID_NAME,
                mCarId );
        data.put( MainDbAdapter.EXPENSE_COL_DRIVER_ID_NAME,
                mDriverId );
        data.put( MainDbAdapter.EXPENSE_COL_EXPENSECATEGORY_ID_NAME,
                spnExpCategory.getSelectedItemId() );
        data.put( MainDbAdapter.EXPENSE_COL_EXPENSETYPE_ID_NAME,
                spnExpType.getSelectedItemId() );
        data.put( MainDbAdapter.EXPENSE_COL_INDEX_NAME, etCarIndex.getText().toString());

//        data.put( MainDbAdapter.EXPENSE_COL_AMOUNTENTERED_NAME, etUserInput.getText().toString());
        if(amount != null)
        	data.put( MainDbAdapter.EXPENSE_COL_AMOUNTENTERED_NAME, amount.toString());
        if(price != null)
            data.put( MainDbAdapter.EXPENSE_COL_PRICEENTERED_NAME, price.toString());
        data.put( MainDbAdapter.EXPENSE_COL_QUANTITY_NAME, etQuantity.getText().toString());
        if(spnUOM.getSelectedItemId() != -1)
            data.put( MainDbAdapter.EXPENSE_COL_UOM_ID_NAME, spnUOM.getSelectedItemId());
        
        data.put( MainDbAdapter.EXPENSE_COL_CURRENCYENTERED_ID_NAME,
                mCurrencyId);
        data.put( MainDbAdapter.EXPENSE_COL_CURRENCY_ID_NAME, carDefaultCurrencyId);
        if(mCurrencyId == carDefaultCurrencyId){
        	if(amount == null){
        		if(rbInsertModePrice.isChecked()){
	        		calculateAmount();
	        		//check again
	        		if(amount == null){ //notify the user
	                    Toast toast = Toast.makeText( getApplicationContext(),
	                            mResource.getString( R.string.GEN_AmountLabel ) + " ?", Toast.LENGTH_LONG );
	                    toast.show();
	                    return;
	        		}
        		}
        		else{
                    Toast toast = Toast.makeText( getApplicationContext(),
                            mResource.getString( R.string.GEN_AmountLabel ) + " ?", Toast.LENGTH_LONG );
                    toast.show();
                    return;
        		}
        	}
            data.put( MainDbAdapter.EXPENSE_COL_AMOUNT_NAME, amount.toString());
            if(price != null)
                data.put( MainDbAdapter.EXPENSE_COL_PRICE_NAME, price.toString());
            data.put( MainDbAdapter.EXPENSE_COL_CURRENCYRATE_NAME, "1");
        }
        else{
            data.put( MainDbAdapter.EXPENSE_COL_AMOUNT_NAME, convertedAmount.toString());
            if(convertedPrice != null)
                data.put( MainDbAdapter.EXPENSE_COL_PRICE_NAME, convertedPrice.toString());
            data.put( MainDbAdapter.EXPENSE_COL_CURRENCYRATE_NAME, conversionRate.toString());
        }


        data.put( MainDbAdapter.EXPENSE_COL_DATE_NAME, mlDateTimeInSeconds);
        data.put( MainDbAdapter.EXPENSE_COL_DOCUMENTNO_NAME,
                etDocNo.getText().toString());

        if(acBPartner.getText().toString() != null && acBPartner.getText().toString().length() > 0){
            String selection = "UPPER (" + MainDbAdapter.GEN_COL_NAME_NAME + ") = ?";
            String[] selectionArgs = {acBPartner.getText().toString().toUpperCase()};
            Cursor c = mDbAdapter.query(MainDbAdapter.BPARTNER_TABLE_NAME, MainDbAdapter.genColName, selection, selectionArgs,
                    null, null, null);
            String bPartnerIdStr = null;
            if(c.moveToFirst())
                bPartnerIdStr = c.getString(MainDbAdapter.GEN_COL_ROWID_POS);
            c.close();
            if(bPartnerIdStr != null && bPartnerIdStr.length() > 0){
                mBPartnerId = Long.parseLong(bPartnerIdStr);
                data.put(MainDbAdapter.EXPENSE_COL_BPARTNER_ID_NAME, mBPartnerId);
            }
            else{
                ContentValues tmpData = new ContentValues();
                tmpData.put(MainDbAdapter.GEN_COL_NAME_NAME, acBPartner.getText().toString());
                mBPartnerId = mDbAdapter.createRecord(MainDbAdapter.BPARTNER_TABLE_NAME, tmpData);
                if(mBPartnerId >= 0)
                    data.put(MainDbAdapter.EXPENSE_COL_BPARTNER_ID_NAME, mBPartnerId);
            }

            if(acAdress.getText().toString() != null && acAdress.getText().toString().length() > 0){
                selection = "UPPER (" + MainDbAdapter.BPARTNER_LOCATION_ADDRESS_NAME + ") = ? AND " +
                                            MainDbAdapter.BPARTNER_LOCATION_BPARTNER_ID_NAME + " = ?";
                String[] selectionArgs2 = {acAdress.getText().toString().toUpperCase(), Long.toString(mBPartnerId)};
                c = mDbAdapter.query(MainDbAdapter.BPARTNER_LOCATION_TABLE_NAME, MainDbAdapter.genColName, selection, selectionArgs2,
                        null, null, null);
                String addressIdStr = null;
                if(c.moveToFirst())
                    addressIdStr = c.getString(MainDbAdapter.GEN_COL_ROWID_POS);
                c.close();
                if(addressIdStr != null && addressIdStr.length() > 0)
                    data.put(MainDbAdapter.EXPENSE_COL_BPARTNER_LOCATION_ID_NAME, Long.parseLong(addressIdStr));
                else{
                    ContentValues tmpData = new ContentValues();
                    tmpData.put(MainDbAdapter.BPARTNER_LOCATION_BPARTNER_ID_NAME, mBPartnerId);
                    tmpData.put(MainDbAdapter.GEN_COL_NAME_NAME, acAdress.getText().toString());
                    tmpData.put(MainDbAdapter.BPARTNER_LOCATION_ADDRESS_NAME, acAdress.getText().toString());
                    long newAddressId = mDbAdapter.createRecord(MainDbAdapter.BPARTNER_LOCATION_TABLE_NAME, tmpData);
                    if(newAddressId >= 0)
                        data.put(MainDbAdapter.EXPENSE_COL_BPARTNER_LOCATION_ID_NAME, newAddressId);
                }
            }
            else
                data.put(MainDbAdapter.EXPENSE_COL_BPARTNER_LOCATION_ID_NAME, (String)null);
        }
        else{
            data.put(MainDbAdapter.EXPENSE_COL_BPARTNER_ID_NAME, (String)null);
            data.put(MainDbAdapter.EXPENSE_COL_BPARTNER_LOCATION_ID_NAME, (String)null);
        }

        if(acTag.getText().toString() != null && acTag.getText().toString().length() > 0){
            String selection = "UPPER (" + MainDbAdapter.GEN_COL_NAME_NAME + ") = ?";
            String[] selectionArgs = {acTag.getText().toString().toUpperCase()};
            Cursor c = mDbAdapter.query(MainDbAdapter.TAG_TABLE_NAME, MainDbAdapter.genColName, selection, selectionArgs,
                    null, null, null);
            String tagIdStr = null;
            if(c.moveToFirst())
                tagIdStr = c.getString(MainDbAdapter.GEN_COL_ROWID_POS);
            c.close();
            if(tagIdStr != null && tagIdStr.length() > 0){
                mTagId = Long.parseLong(tagIdStr);
                data.put(MainDbAdapter.EXPENSE_COL_TAG_ID_NAME, mTagId);
            }
            else{
                ContentValues tmpData = new ContentValues();
                tmpData.put(MainDbAdapter.GEN_COL_NAME_NAME, acTag.getText().toString());
                mTagId = mDbAdapter.createRecord(MainDbAdapter.TAG_TABLE_NAME, tmpData);
                if(mTagId >= 0)
                    data.put(MainDbAdapter.EXPENSE_COL_TAG_ID_NAME, mTagId);
            }
        }
        else{
            data.put(MainDbAdapter.EXPENSE_COL_TAG_ID_NAME, (String)null);
        }
        
        
        if( operationType.equals("N") ) {
            Long createResult = mDbAdapter.createRecord(MainDbAdapter.EXPENSE_TABLE_NAME, data);
            if( createResult.intValue() < 0){
                if(createResult.intValue() == -1) //DB Error
                    madbErrorAlert.setMessage(mDbAdapter.lastErrorMessage);
                else //precondition error
                    madbErrorAlert.setMessage(mResource.getString(-1 * createResult.intValue()));
                madError = madbErrorAlert.create();
                madError.show();
                return;
            }
        }
        else {
            int updResult = mDbAdapter.updateRecord(MainDbAdapter.EXPENSE_TABLE_NAME, mRowId, data);
            if(updResult != -1){
                String errMsg = "";
                errMsg = mResource.getString(updResult);
                if(updResult == R.string.ERR_000)
                    errMsg = errMsg + "\n" + mDbAdapter.lastErrorMessage;
                madbErrorAlert.setMessage(errMsg);
                madError = madbErrorAlert.create();
                madError.show();
                return;
            }
        }
    	
        if(mPreferences.getBoolean("RememberLastTag", false) && mTagId > 0)
    		mPrefEditor.putLong("LastTagId", mTagId);
    	
    	mPrefEditor.putLong("LastDriver_ID", mDriverId);
		mPrefEditor.commit();
        finish();
        
    }

    @Override
    protected void setLayout() {
        setContentView(R.layout.expense_edit_activity);
    }
}
