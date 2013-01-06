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

import org.andicar.persistence.MainDbAdapter;
import org.andicar.service.ToDoNotificationService;
import org.andicar.utils.AndiCarStatistics;
import org.andicar.utils.StaticValues;
import org.andicar.utils.Utils;
import org.andicar2.activity.R;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 *
 * @author miki
 */
public class ExpenseEditActivity extends EditActivityBase {
    private Spinner spnUOM;
    private EditText etCarIndex;
    private EditText etUserInput;
    private EditText etConversionRate;
    private EditText etQuantity;
    private TextView tvConvertedAmountValue;
    private TextView tvConvertedAmountLabel;
    private TextView tvCalculatedTextLabel;
    private TextView tvCalculatedTextContent;
    private RadioButton rbInsertModePrice;
    private RadioButton rbInsertModeAmount;

    private LinearLayout llConversionRateZone1;
    private LinearLayout llConversionRateZone2;

    private RelativeLayout lCarZone;
    private RelativeLayout lDriverZone;
    private RelativeLayout lExpTypeZone;
    private RelativeLayout lExpCatZone;

    private long carDefaultCurrencyId;
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

    private ArrayAdapter<String> userCommentAdapter;
    private ArrayAdapter<String> bpartnerNameAdapter;
    private ArrayAdapter<String> addressAdapter;
    private ArrayAdapter<String> tagAdapter;
    private static int INSERTMODE_PRICE = 0;
    private static int INSERTMODE_AMOUNT = 1;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        isUseTemplate = true;
        super.onCreate(icicle);

        if(icicle !=null)
            return; //restored from previous state

        operationType = mBundleExtras.getString("Operation");
        init();

        if (operationType.equals("E")) {
            mRowId = mBundleExtras.getLong( MainDbAdapter.GEN_COL_ROWID_NAME );
            Cursor c = mDbAdapter.fetchRecord(MainDbAdapter.EXPENSE_TABLE_NAME,
                    MainDbAdapter.expenseTableColNames, mRowId);
            setCarId(c.getLong(MainDbAdapter.EXPENSE_COL_CAR_ID_POS));
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

            etUserInput.setText(
            		Utils.numberToString(c.getDouble(MainDbAdapter.EXPENSE_COL_AMOUNTENTERED_POS), false, StaticValues.DECIMALS_AMOUNT, StaticValues.ROUNDING_MODE_AMOUNT));
//            		c.getString(MainDbAdapter.EXPENSE_COL_AMOUNTENTERED_POS));
            try{
                conversionRate = new BigDecimal(c.getString(MainDbAdapter.EXPENSE_COL_CURRENCYRATE_POS));
            }
            catch(NumberFormatException e){}
            etConversionRate.setText(conversionRate.toString());
            tvConvertedAmountValue.setText(
            		Utils.numberToString(c.getDouble(MainDbAdapter.EXPENSE_COL_AMOUNT_POS), false, StaticValues.DECIMALS_AMOUNT, StaticValues.ROUNDING_MODE_AMOUNT));
//            		c.getString(MainDbAdapter.EXPENSE_COL_AMOUNT_POS));

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
                setEditable((ViewGroup) findViewById(R.id.vgRoot), true);
                if(mDet != null)
                	mDet.setControlsEnabled(false);
            }
            else{
                if(mDet != null)
                	mDet.setControlsEnabled(true);
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
                        acAdress.setText(c2.getString(MainDbAdapter.BPARTNER_LOCATION_COL_ADDRESS_POS));
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
            setInsertMode(INSERTMODE_AMOUNT);
        }
        else {
        	setDefaultValues();
    	}

        initControls();
        if(isSendStatistics)
            AndiCarStatistics.sendFlurryEvent(this, "ExpenseEdit", null);
    }

    private void initControls() {
    	long checkID;
    	
    	if(lCarZone != null){
	    	checkID = mDbAdapter.isSingleActiveRecord(MainDbAdapter.CAR_TABLE_NAME, null); 
	    	if(checkID > -1){ //one single car
	    		mCarId = checkID;
	    		lCarZone.setVisibility(View.GONE);
	    	}
	    	else{
	    		lCarZone.setVisibility(View.VISIBLE);
		        initSpinner(spnCar, MainDbAdapter.CAR_TABLE_NAME, MainDbAdapter.genColName,
		                new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, MainDbAdapter.isActiveCondition, null,
		                MainDbAdapter.GEN_COL_NAME_NAME,
		                mCarId, false);
	    	}
    	}
    	else{
	        initSpinner(spnCar, MainDbAdapter.CAR_TABLE_NAME, MainDbAdapter.genColName,
	                new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, MainDbAdapter.isActiveCondition, null,
	                MainDbAdapter.GEN_COL_NAME_NAME,
	                mCarId, false);
    	}
    	
    	if(lDriverZone != null){
	    	checkID = mDbAdapter.isSingleActiveRecord(MainDbAdapter.DRIVER_TABLE_NAME, null); 
	    	if(checkID > -1){ //one single driver
	    		mDriverId = checkID;
	    		lDriverZone.setVisibility(View.GONE);
	    	}
	    	else{
	    		lDriverZone.setVisibility(View.VISIBLE);
		        initSpinner(spnDriver, MainDbAdapter.DRIVER_TABLE_NAME, MainDbAdapter.genColName,
		                new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, MainDbAdapter.isActiveCondition, null,
		                MainDbAdapter.GEN_COL_NAME_NAME, mDriverId, false);
	    	}
    	}
    	else{
	        initSpinner(spnDriver, MainDbAdapter.DRIVER_TABLE_NAME, MainDbAdapter.genColName,
	                new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, MainDbAdapter.isActiveCondition, null,
	                MainDbAdapter.GEN_COL_NAME_NAME, mDriverId, false);
    	}

    	if(lExpTypeZone != null){
	    	checkID = mDbAdapter.isSingleActiveRecord(MainDbAdapter.EXPENSETYPE_TABLE_NAME, null); 
	    	if(checkID > -1){ //one single type
	    		mExpTypeId = checkID;
	    		lExpTypeZone.setVisibility(View.GONE);
	    	}
	    	else{
	    		lExpTypeZone.setVisibility(View.VISIBLE);
		        initSpinner(spnExpType, MainDbAdapter.EXPENSETYPE_TABLE_NAME,
		                MainDbAdapter.genColName, new String[]{MainDbAdapter.GEN_COL_NAME_NAME},
		                MainDbAdapter.isActiveCondition, null, MainDbAdapter.GEN_COL_NAME_NAME,
		                mExpTypeId, false);
	    	}
    	}
    	else{
	        initSpinner(spnExpType, MainDbAdapter.EXPENSETYPE_TABLE_NAME,
	                MainDbAdapter.genColName, new String[]{MainDbAdapter.GEN_COL_NAME_NAME},
	                MainDbAdapter.isActiveCondition, null, MainDbAdapter.GEN_COL_NAME_NAME,
	                mExpTypeId, false);
    	}
    	
    	if(lExpCatZone != null){
	    	checkID = mDbAdapter.isSingleActiveRecord(MainDbAdapter.EXPENSECATEGORY_TABLE_NAME, MainDbAdapter.EXPENSECATEGORY_COL_ISFUEL_NAME + "='N'"); 
	    	if(checkID > -1){ //one single type
	    		mExpCategoryId= checkID;
	    		lExpCatZone.setVisibility(View.GONE);
	    	}
	    	else{
	    		lExpCatZone.setVisibility(View.VISIBLE);
	        	initSpinner(spnExpCategory, MainDbAdapter.EXPENSECATEGORY_TABLE_NAME, MainDbAdapter.genColName, 
	                    new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, 
	                    MainDbAdapter.isActiveCondition + " AND " + MainDbAdapter.EXPENSECATEGORY_COL_ISFUEL_NAME + " = 'N'", null,
	                    MainDbAdapter.GEN_COL_NAME_NAME, mExpCategoryId, false);
	    	}
    	}
    	else{
        	initSpinner(spnExpCategory, MainDbAdapter.EXPENSECATEGORY_TABLE_NAME, MainDbAdapter.genColName, 
                    new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, 
                    MainDbAdapter.isActiveCondition + " AND " + MainDbAdapter.EXPENSECATEGORY_COL_ISFUEL_NAME + " = 'N'", null,
                    MainDbAdapter.GEN_COL_NAME_NAME, mExpCategoryId, false);
    	}
    	
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
                mDbAdapter.getAutoCompleteText(MainDbAdapter.BPARTNER_LOCATION_TABLE_NAME, MainDbAdapter.BPARTNER_LOCATION_COL_ADDRESS_NAME,
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
        spnCar.setOnItemSelectedListener(spinnerCarOnItemSelectedListener);
        spnDriver.setOnItemSelectedListener(spinnerDriverOnItemSelectedListener);
        spnCar.setOnTouchListener(spinnerOnTouchListener);
        spnDriver.setOnTouchListener(spinnerOnTouchListener);
        spnCurrency = (Spinner) findViewById(R.id.spnCurrency);
        spnCurrency.setOnItemSelectedListener(spinnerCurrencyOnItemSelectedListener);
        spnCurrency.setOnTouchListener(spinnerOnTouchListener);
        spnExpType = (Spinner) findViewById(R.id.spnExpType);
        spnExpType.setOnItemSelectedListener(spinnerExpTypeOnItemSelectedListener);
        spnExpType.setOnTouchListener(spinnerOnTouchListener);
        spnExpCategory = (Spinner) findViewById(R.id.spnExpCategory);
        spnExpCategory.setOnItemSelectedListener(spinnerExpCatOnItemSelectedListener);
        spnExpCategory.setOnTouchListener(spinnerOnTouchListener);
        spnUOM = (Spinner) findViewById(R.id.spnUOM);
        etCarIndex = (EditText) findViewById(R.id.etIndex);
        etUserInput = (EditText) findViewById(R.id.etUserInput);
        etUserInput.addTextChangedListener(userInputTextWatcher);
        etDocNo = (EditText) findViewById(R.id.etDocumentNo);
        
        llConversionRateZone1 = (LinearLayout) findViewById(R.id.llConversionRateZone1);
        llConversionRateZone2 = (LinearLayout) findViewById(R.id.llConversionRateZone2);
        etConversionRate = (EditText) findViewById(R.id.etConversionRate);
        etConversionRate.addTextChangedListener(userInputTextWatcher);
        etQuantity = (EditText) findViewById(R.id.etQuantity);
        etQuantity.addTextChangedListener(userInputTextWatcher);
        tvCalculatedTextLabel = (TextView) findViewById(R.id.tvCalculatedTextLabel);
        tvCalculatedTextContent = (TextView) findViewById(R.id.tvCalculatedTextContent);
        tvConvertedAmountValue = (TextView) findViewById(R.id.tvConvertedAmountValue);
        tvConvertedAmountLabel = (TextView) findViewById(R.id.tvConvertedAmountLabel);
        rbInsertModePrice = (RadioButton) findViewById(R.id.rbInsertModePrice);
        rbInsertModeAmount = (RadioButton) findViewById(R.id.rbInsertModeAmount);
        RadioGroup rg = (RadioGroup) findViewById(R.id.rgInsertMode);
        rg.setOnCheckedChangeListener(rgOnCheckedChangeListener);

        lCarZone = (RelativeLayout) findViewById(R.id.lCarZone);
        lDriverZone = (RelativeLayout) findViewById(R.id.lDriverZone);
        lExpTypeZone = (RelativeLayout) findViewById(R.id.lExpTypeZone);
        lExpCatZone = (RelativeLayout) findViewById(R.id.lExpCatZone);
        
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
                        mDbAdapter.getAutoCompleteText(MainDbAdapter.BPARTNER_LOCATION_TABLE_NAME, MainDbAdapter.BPARTNER_LOCATION_COL_ADDRESS_NAME,
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
        isBackgroundSettingsActive = true;
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

    private AdapterView.OnItemSelectedListener spinnerCarOnItemSelectedListener =
            new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    if(isBackgroundSettingsActive)
                        return;
                    setCarId(arg3);
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

    private AdapterView.OnItemSelectedListener spinnerDriverOnItemSelectedListener =
        new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                if(isBackgroundSettingsActive)
                    return;
                setDriverId(arg3);
            }
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        };
    private AdapterView.OnItemSelectedListener spinnerCurrencyOnItemSelectedListener =
            new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    setSpinnerTextToCode(arg0, arg3, arg1);
                    if(isBackgroundSettingsActive)
                        return;
                    mCurrencyId = spnCurrency.getSelectedItemId();
                    setSpecificLayout();
                    if(rbInsertModeAmount.isChecked())
                    	calculatePrice();
                    else
                    	calculateAmount();
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

    private AdapterView.OnItemSelectedListener spinnerExpCatOnItemSelectedListener =
		    new AdapterView.OnItemSelectedListener() {
		        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		            if(isBackgroundSettingsActive)
		                return;
		            setExpCategoryId(arg3);
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
            tvCalculatedTextLabel.setText(mResource.getString(R.string.GEN_AmountLabel) + " =");
            etUserInput.setTag(mResource.getString(R.string.GEN_PriceLabel));
            etQuantity.setHint(mResource.getString(R.string.GEN_Required));
            calculateAmount();
        }
        else{
            tvCalculatedTextLabel.setText(mResource.getString(R.string.GEN_PriceLabel) + " =");
            etUserInput.setTag(mResource.getString(R.string.GEN_AmountLabel));
            etQuantity.setHint(null);
            calculatePrice();
            if(mCurrencyId != carDefaultCurrencyId)
                calculateConvertedAmount();
        }
    }

    private void setConversionRateZoneVisible(boolean isVisible){
        if(isVisible){
            llConversionRateZone1.setVisibility(View.VISIBLE);
            llConversionRateZone2.setVisibility(View.VISIBLE);
            if(carDefaultCurrencyCode == null)
            	carDefaultCurrencyCode = "";
            tvConvertedAmountLabel.setText((mResource.getString(R.string.GEN_ConvertedAmountLabel))
                    .replace("[#1]", carDefaultCurrencyCode) + " = ");
            etConversionRate.setTag(mResource.getString(R.string.GEN_ConversionRateLabel));
        }
        else{
            etConversionRate.setTag(null);
            llConversionRateZone1.setVisibility(View.GONE);
            llConversionRateZone2.setVisibility(View.GONE);
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
                convertedAmountStr = 
            		Utils.numberToString(convertedAmount, true, StaticValues.DECIMALS_AMOUNT, StaticValues.ROUNDING_MODE_AMOUNT);
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
	                tvCalculatedTextContent.setText(
	                		Utils.numberToString(price, true, StaticValues.DECIMALS_PRICE, StaticValues.ROUNDING_MODE_PRICE)
		            		+ " " + 
		            		mDbAdapter.getCurrencyCode(mCurrencyId));
//	                		price.toString());
                }
                else{
                	price = null;
                	tvCalculatedTextContent.setText(null);
                }
            }
            catch(NumberFormatException e){}
        }
        else{
        	price = null;
        	tvCalculatedTextContent.setText(null);
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
                tvCalculatedTextContent.setText(
                		Utils.numberToString(amount, true, StaticValues.DECIMALS_AMOUNT, StaticValues.ROUNDING_MODE_AMOUNT)
                		+ " " + 
                		mDbAdapter.getCurrencyCode(mCurrencyId));
//                		amount.toString());
            }
            catch(NumberFormatException e){}
        }
        else{
        	amount = null;
        	tvCalculatedTextContent.setText(null);
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
    protected boolean saveData() {

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
                mExpCategoryId);
        data.put( MainDbAdapter.EXPENSE_COL_EXPENSETYPE_ID_NAME,
                mExpTypeId);
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
	                    return false;
	        		}
        		}
        		else{
                    Toast toast = Toast.makeText( getApplicationContext(),
                            mResource.getString( R.string.GEN_AmountLabel ) + " ?", Toast.LENGTH_LONG );
                    toast.show();
                    return false;
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
                selection = "UPPER (" + MainDbAdapter.BPARTNER_LOCATION_COL_ADDRESS_NAME + ") = ? AND " +
                                            MainDbAdapter.BPARTNER_LOCATION_COL_BPARTNER_ID_NAME + " = ?";
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
                    tmpData.put(MainDbAdapter.BPARTNER_LOCATION_COL_BPARTNER_ID_NAME, mBPartnerId);
                    tmpData.put(MainDbAdapter.GEN_COL_NAME_NAME, acAdress.getText().toString());
                    tmpData.put(MainDbAdapter.BPARTNER_LOCATION_COL_ADDRESS_NAME, acAdress.getText().toString());
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
                return false;
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
                return false;
            }
        }
    	
        if(mPreferences.getBoolean("RememberLastTag", false) && mTagId > 0)
    		mPrefEditor.putLong("LastTagId", mTagId);
    	
    	mPrefEditor.putLong("LastDriver_ID", mDriverId);
    	mPrefEditor.putLong("ExpenseExpCategory_ID", mExpCategoryId);
    	mPrefEditor.putLong("ExpenseExpenseType_ID", mExpTypeId);
		mPrefEditor.commit();

		//check if mileage todo exists
		if(etCarIndex.getText().toString() != null && etCarIndex.getText().toString().length() > 0){
			Intent intent = new Intent(this, ToDoNotificationService.class);
			intent.putExtra("setJustNextRun", false);
			intent.putExtra("CarID", mCarId);
			this.startService(intent);
		}
		
        return true;
    }

    @Override
    protected void setLayout() {
    	if(mPreferences.getString("UIStyle", "s01").equalsIgnoreCase("s00"))
    		setContentView(R.layout.expense_edit_activity_s00);
    	else if(mPreferences.getString("UIStyle", "s01").equalsIgnoreCase("s01"))
    		setContentView(R.layout.expense_edit_activity_s01);
    }

    public void setSpecificLayout() {
        if(mCurrencyId != carDefaultCurrencyId){
            setConversionRateZoneVisible(true);
            conversionRate = mDbAdapter.getCurrencyRate(mCurrencyId, carDefaultCurrencyId);
            etConversionRate.setText("");
            tvConvertedAmountValue.setText("");
            if(conversionRate != null){
                etConversionRate.append(conversionRate.toString());
            }
        }
        else{
            setConversionRateZoneVisible(false);
        }
    }

    /**
	 * @return the mUOMId
	 */
	public long getmUOMId() {
		return mUOMId;
	}

	/**
	 * @param uOMId the mUOMId to set
	 */
	public void setmUOMId(long uOMId) {
		this.mUOMId = uOMId;
		setSpinnerSelectedID(spnUOM, uOMId);
	}

	/**
	 * @param carId the mCarId to set
	 */
	public void setCarId(long carId) {
		this.mCarId = carId;
		carDefaultCurrencyId = mDbAdapter.getCarCurrencyID(carId);
		carDefaultCurrencyCode = mDbAdapter.getCurrencyCode(carDefaultCurrencyId);
        userCommentAdapter = null;
        userCommentAdapter = new ArrayAdapter<String>(ExpenseEditActivity.this,
                android.R.layout.simple_dropdown_item_1line,
                mDbAdapter.getAutoCompleteText(MainDbAdapter.EXPENSE_TABLE_NAME, null, mCarId, 30));
        acUserComment.setAdapter(userCommentAdapter);
	}

	/**
	 * @param driverId the mDriverId to set
	 */
	public void setDriverId(long driverId) {
		this.mDriverId = driverId;
	}

	/* (non-Javadoc)
	 * @see org.andicar.activity.EditActivityBase#setDefaultValues()
	 */
	@Override
	public void setDefaultValues() {
		isBackgroundSettingsActive = true;
		
        mCarId = mPreferences.getLong("CurrentCar_ID", -1);
        setSpinnerSelectedID(spnCar, mCarId);
        carDefaultCurrencyId = mDbAdapter.getCarCurrencyID(mCarId); 
        carDefaultCurrencyCode = mDbAdapter.getCurrencyCode(carDefaultCurrencyId);

        mDriverId = mPreferences.getLong("LastDriver_ID", 1);
        setSpinnerSelectedID(spnDriver, mDriverId);
        
        mCurrencyId = carDefaultCurrencyId;
        setSpinnerSelectedID(spnCurrency, mCurrencyId);
        
        mUOMId = -1;
        setSpinnerSelectedID(spnUOM, mUOMId);
        
		setExpCategoryId(mPreferences.getLong("ExpenseExpCategory_ID", -1));
		if(mExpCategoryId == -1 || //mPreferences.getLong("ExpenseExpCategory_ID" not exist
				!mDbAdapter.isIDActive(MainDbAdapter.EXPENSECATEGORY_TABLE_NAME, mExpCategoryId)){ 
			mExpCategoryId = mDbAdapter.getFirstActiveID(MainDbAdapter.EXPENSECATEGORY_TABLE_NAME, MainDbAdapter.EXPENSECATEGORY_COL_ISFUEL_NAME + "='N'", MainDbAdapter.GEN_COL_NAME_NAME);
		}
		setSpinnerSelectedID(spnExpCategory, mExpCategoryId);

		setExpTypeId(mPreferences.getLong("ExpenseExpenseType_ID", 1));
		if(mExpTypeId == -1 || //mPreferences.getLong("ExpenseExpCategory_ID" not exist
				!mDbAdapter.isIDActive(MainDbAdapter.EXPENSETYPE_TABLE_NAME, mExpTypeId)){ 
			mExpTypeId = mDbAdapter.getFirstActiveID(MainDbAdapter.EXPENSETYPE_TABLE_NAME, null, MainDbAdapter.GEN_COL_NAME_NAME);
		}
		setSpinnerSelectedID(spnExpType, mExpTypeId);
        
        
        acUserComment.setText(null);
        etUserInput.setText(null);
        etQuantity.setText(null);
        tvCalculatedTextContent.setText(null);
        etConversionRate.setText(null);
        price = null;
        amount = null;
        quantity = null;
        conversionRate = null;
        convertedAmount = null;
        convertedPrice = null;
        
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
        else {
        	mTagId = 0;
        	acTag.setText("");
        }
        
        acBPartner.setText(null);
        acAdress.setEnabled(false);
        acAdress.setText(null);
        acAdress.setHint(mResource.getString(R.string.GEN_BPartner).replace(":", "") + " " +
                mResource.getString(R.string.GEN_Required).replace(":", ""));
        setInsertMode(INSERTMODE_AMOUNT);
        rbInsertModeAmount.setChecked(true);
        calculatePrice();
        initDateTime(System.currentTimeMillis());
        setEditable((ViewGroup) findViewById(R.id.vgRoot), true);
        setConversionRateZoneVisible(false);
	}

}
