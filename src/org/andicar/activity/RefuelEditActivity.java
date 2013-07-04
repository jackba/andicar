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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
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
public class RefuelEditActivity extends EditActivityBase {
    private Spinner spnUomVolume;
    private EditText etCarIndex;
    private EditText etQty;
    private EditText etUserInput;
    private TextView tvConvertedAmountLabel;
    private EditText etConversionRate;
    private LinearLayout llConversionRateZone;
    private LinearLayout llConvertedAmountZone;
    private CheckBox ckIsFullRefuel;
    private TextView tvCalculatedTextContent;
    private TextView tvCalculatedTextLabel;

    private LinearLayout llBaseUOMQtyZone;
    private TextView tvBaseUOMQtyLabel;
    private TextView tvBaseUOMQtyValue;
    private TextView tvConversionRateLabel;
    private RadioButton rbInsertModeAmount;
    private RadioButton rbInsertModePrice;

    private RelativeLayout lCarZone;
    private RelativeLayout lDriverZone;
    private RelativeLayout lExpTypeZone;
    private RelativeLayout lExpCatZone;


    private long carDefaultCurrencyId = 0;
    private long carDefaultUOMVolumeId = 0;
    private long mUomVolumeId = 0;
    private long mBPartnerId = 0;
    private long mAddressId = 0;
    private long mTagId = 0;
    private String carDefaultCurrencyCode = "";
    private String currencyCode = "";
    private String carDefaultUOMVolumeCode = "";
    private BigDecimal currencyConversionRate = null;
    private BigDecimal priceEntered = null;
    private BigDecimal priceConverted = null;
    private BigDecimal amountEntered = null;
    private BigDecimal amountConverted = null;
    private BigDecimal uomVolumeConversionRate= null;
    private BigDecimal baseUomQty = null;

    private ArrayAdapter<String> userCommentAdapter;
    private ArrayAdapter<String> bpartnerNameAdapter;
    private ArrayAdapter<String> addressAdapter;
    private ArrayAdapter<String> tagAdapter;
    private String operationType;

    private int mInsertMode = 0; //0 = price; 1 = amount
    private static int INSERTMODE_PRICE = 0;
    private static int INSERTMODE_AMOUNT = 1;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        isUseTemplate = true;
        super.onCreate(icicle);

        if(icicle !=null)
            return; //restoe from previous state

        operationType = mBundleExtras.getString("Operation");

        init();

        if (operationType.equals("E")) {
            mRowId = mBundleExtras.getLong( MainDbAdapter.COL_NAME_GEN_ROWID );
            Cursor c = mDbAdapter.fetchRecord(MainDbAdapter.TABLE_NAME_REFUEL, MainDbAdapter.COL_LIST_REFUEL_TABLE, mRowId);
            setCarId(c.getLong(MainDbAdapter.COL_POS_REFUEL__CAR_ID));
//            mCarId = c.getLong(MainDbAdapter.REFUEL_COL_CAR_ID_POS);
            mDriverId = c.getLong(MainDbAdapter.COL_POS_REFUEL__DRIVER_ID);
            mExpCategoryId = c.getLong(MainDbAdapter.COL_POS_REFUEL__EXPENSECATEGORY_ID);
            mExpTypeId = c.getLong(MainDbAdapter.COL_POS_REFUEL__EXPENSETYPE_ID);
            mUomVolumeId = c.getLong(MainDbAdapter.COL_POS_REFUEL__UOMVOLUMEENTERED_ID);
            mCurrencyId = c.getLong(MainDbAdapter.COL_POS_REFUEL__CURRENCYENTERED_ID);
            Cursor c2 = null;
            if(c.getString(MainDbAdapter.COL_POS_REFUEL__BPARTNER_ID) != null
                    && c.getString(MainDbAdapter.COL_POS_REFUEL__BPARTNER_ID).length() > 0){
                mBPartnerId = c.getLong(MainDbAdapter.COL_POS_REFUEL__BPARTNER_ID);
                String selection = MainDbAdapter.COL_NAME_GEN_ROWID + "= ? ";
                String[] selectionArgs = {Long.toString(mBPartnerId)};
                c2 = mDbAdapter.query(MainDbAdapter.TABLE_NAME_BPARTNER, MainDbAdapter.COL_LIST_GEN_ROWID_NAME,
                            selection, selectionArgs, null, null, null);
                if(c2.moveToFirst())
                    acBPartner.setText(c2.getString(MainDbAdapter.COL_POS_GEN_NAME));
                c2.close();

                if(c.getString(MainDbAdapter.COL_POS_REFUEL__BPARTNER_LOCATION_ID) != null
                        && c.getString(MainDbAdapter.COL_POS_REFUEL__BPARTNER_LOCATION_ID).length() > 0){
                    mAddressId = c.getLong(MainDbAdapter.COL_POS_REFUEL__BPARTNER_LOCATION_ID);
                    selectionArgs[0] = Long.toString(mAddressId);
                    c2 = mDbAdapter.query(MainDbAdapter.TABLE_NAME_BPARTNERLOCATION, MainDbAdapter.COL_LIST_BPARTNERLOCATION_TABLE,
                            selection, selectionArgs, null, null, null);
                    if(c2.moveToFirst())
                        acAdress.setText(c2.getString(MainDbAdapter.COL_POS_BPARTNERLOCATION__ADDRESS));
                    c2.close();
                }
            }
            else{
                acAdress.setEnabled(false);
                acAdress.setText(null);
                acAdress.setHint(mResource.getString(R.string.RefuelEditActivity_GasStation).replace(":", "") + " " +
                        mResource.getString(R.string.GEN_Required).replace(":", ""));
            }
            
            //fill tag
            if(c.getString(MainDbAdapter.COL_POS_REFUEL__TAG_ID) != null
                    && c.getString(MainDbAdapter.COL_POS_REFUEL__TAG_ID).length() > 0){
                mTagId = c.getLong(MainDbAdapter.COL_POS_REFUEL__TAG_ID);
                String selection = MainDbAdapter.COL_NAME_GEN_ROWID + "= ? ";
                String[] selectionArgs = {Long.toString(mTagId)};
                c2 = mDbAdapter.query(MainDbAdapter.TABLE_NAME_TAG, MainDbAdapter.COL_LIST_GEN_ROWID_NAME,
                            selection, selectionArgs, null, null, null);
                if(c2.moveToFirst())
                    acTag.setText(c2.getString(MainDbAdapter.COL_POS_GEN_NAME));
                c2.close();
            }

            try{
                currencyConversionRate = new BigDecimal(c.getString(MainDbAdapter.COL_POS_REFUEL__CURRENCYRATE));
                uomVolumeConversionRate = new BigDecimal(c.getString(MainDbAdapter.COL_POS_REFUEL__UOMVOLCONVERSIONRATE));
            }
            catch(NumberFormatException e){}
            etConversionRate.setText(currencyConversionRate.toString());
            initDateTime(c.getLong(MainDbAdapter.COL_POS_REFUEL__DATE) * 1000);
            etCarIndex.setText(
            		Utils.numberToString(c.getDouble(MainDbAdapter.COL_POS_REFUEL__INDEX), false, StaticValues.DECIMALS_LENGTH, StaticValues.ROUNDING_MODE_LENGTH));
//            		(new BigDecimal(c.getDouble(MainDbAdapter.REFUEL_COL_INDEX_POS)).setScale(StaticValues.DECIMALS_LENGTH, StaticValues.ROUNDING_MODE_LENGTH)).toPlainString());
//            		c.getString(MainDbAdapter.REFUEL_COL_INDEX_POS));
            etQty.setText(c.getString(MainDbAdapter.COL_POS_REFUEL__QUANTITYENTERED));
            etUserInput.setText(c.getString(MainDbAdapter.COL_POS_REFUEL__PRICEENTERED));
            etDocNo.setText(c.getString(MainDbAdapter.COL_POS_REFUEL__DOCUMENTNO));
            acUserComment.setText(c.getString(MainDbAdapter.COL_POS_GEN_USER_COMMENT));
            ckIsFullRefuel.setChecked(c.getString(MainDbAdapter.COL_POS_REFUEL__ISFULLREFUEL).equals("Y"));

            carDefaultUOMVolumeId = mDbAdapter.getCarUOMVolumeID(mCarId);
            carDefaultUOMVolumeCode = mDbAdapter.getUOMCode(carDefaultUOMVolumeId);
            if(carDefaultUOMVolumeId != mUomVolumeId){
                tvBaseUOMQtyValue.setText(
                		Utils.numberToString(c.getDouble(MainDbAdapter.COL_POS_REFUEL__QUANTITY), true, 
                				StaticValues.DECIMALS_VOLUME, StaticValues.ROUNDING_MODE_VOLUME) +
//                		c.getString(MainDbAdapter.REFUEL_COL_QUANTITY_POS) +
                        " " + carDefaultUOMVolumeCode);
                setBaseUOMQtyZoneVisibility(true);
            }
            if(mCurrencyId != carDefaultCurrencyId){
                currencyCode = mDbAdapter.getCurrencyCode(mCurrencyId);
            }
            c.close();
            setInsertMode(INSERTMODE_PRICE);
            rbInsertModePrice.setChecked(true);
        }
        else {
        	setDefaultValues();
        }

        initControls();

        if (operationType.equals("E")){
            calculatePriceAmount();
            calculateBaseUOMQty();
        }

        if(isSendStatistics)
            AndiCarStatistics.sendFlurryEvent(this, "RefuelEdit", null);
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
        spnCar.setOnTouchListener(spinnerOnTouchListener);
        spnDriver.setOnItemSelectedListener(spinnerDriverOnItemSelectedListener);
        spnDriver.setOnTouchListener(spinnerOnTouchListener);
        spnCurrency = (Spinner) findViewById( R.id.spnCurrency );
        spnCurrency.setOnItemSelectedListener(spinnerCurrencyOnItemSelectedListener);
        spnCurrency.setOnTouchListener(spinnerOnTouchListener);
        spnUomVolume = (Spinner) findViewById(R.id.spnUomVolume);
        spnUomVolume.setOnItemSelectedListener(spinnerUOMOnItemSelectedListener);
        spnUomVolume.setOnTouchListener(spinnerOnTouchListener);
        spnExpType = (Spinner) findViewById(R.id.spnExpType);
        spnExpType.setOnItemSelectedListener(spinnerExpTypeOnItemSelectedListener);
        spnExpType.setOnTouchListener(spinnerOnTouchListener);
        spnExpCategory = (Spinner) findViewById(R.id.spnExpCategory);
        spnExpCategory.setOnItemSelectedListener(spinnerExpCatOnItemSelectedListener);
        spnExpCategory.setOnTouchListener(spinnerOnTouchListener);
        etCarIndex = (EditText) findViewById(R.id.etIndex);
        etQty = (EditText) findViewById(R.id.etQuantity);
        etQty.addTextChangedListener(textWatcher);
        etUserInput = (EditText) findViewById(R.id.etUserInput);
        etUserInput.addTextChangedListener(textWatcher);
        etDocNo = (EditText) findViewById(R.id.etDocumentNo);
        ckIsFullRefuel = (CheckBox) findViewById(R.id.ckIsFullRefuel);
        tvConvertedAmountLabel = (TextView) findViewById(R.id.tvConvertedAmountLabel);
        tvConversionRateLabel = (TextView) findViewById(R.id.tvConversionRateLabel);
        etConversionRate = (EditText) findViewById(R.id.etConversionRate);
        etConversionRate.addTextChangedListener(textWatcher);
        tvCalculatedTextLabel = (TextView) findViewById(R.id.tvCalculatedTextLabel);
        tvCalculatedTextContent = (TextView) findViewById(R.id.tvCalculatedTextContent);
        llConversionRateZone = (LinearLayout) findViewById(R.id.llConversionRateZone);
        llConvertedAmountZone = (LinearLayout) findViewById(R.id.llConvertedAmountZone);
        rbInsertModePrice = (RadioButton) findViewById(R.id.rbInsertModePrice);
        rbInsertModeAmount = (RadioButton) findViewById(R.id.rbInsertModeAmount);
        RadioGroup rg = (RadioGroup) findViewById(R.id.rgExpenseInsertMode);
        rg.setOnCheckedChangeListener(rgOnCheckedChangeListener);
        llBaseUOMQtyZone = (LinearLayout) findViewById(R.id.llBaseUOMQtyZone);
        tvBaseUOMQtyLabel = (TextView) findViewById(R.id.tvBaseUOMQtyLabel);
        tvBaseUOMQtyLabel.setText(mResource.getString(R.string.RefuelEditActivity_QtyInBaseUOMLabel));
        tvBaseUOMQtyValue = (TextView) findViewById(R.id.tvBaseUOMQtyValue);
        setBaseUOMQtyZoneVisibility(false);
        currencyCode = carDefaultCurrencyCode;
        currencyConversionRate = BigDecimal.ONE;
        uomVolumeConversionRate = BigDecimal.ONE;
        
        lCarZone = (RelativeLayout) findViewById(R.id.lCarZone);
        lDriverZone = (RelativeLayout) findViewById(R.id.lDriverZone);
        lExpTypeZone = (RelativeLayout) findViewById(R.id.lExpTypeZone);
        lExpCatZone = (RelativeLayout) findViewById(R.id.lExpCatZone);
        
    }

    private void initControls() {
    	long checkID;
    	
    	if(lCarZone != null){
	    	checkID = mDbAdapter.isSingleActiveRecord(MainDbAdapter.TABLE_NAME_CAR, null); 
	    	if(checkID > -1){ //one single car
	    		mCarId = checkID;
	    		lCarZone.setVisibility(View.GONE);
	    	}
	    	else{
	    		lCarZone.setVisibility(View.VISIBLE);
		        initSpinner(spnCar, MainDbAdapter.TABLE_NAME_CAR, MainDbAdapter.COL_LIST_GEN_ROWID_NAME,
		                new String[]{MainDbAdapter.COL_NAME_GEN_NAME}, MainDbAdapter.WHERE_CONDITION_ISACTIVE, null,
		                MainDbAdapter.COL_NAME_GEN_NAME,
		                mCarId, false);
	    	}
    	}
    	else{
	        initSpinner(spnCar, MainDbAdapter.TABLE_NAME_CAR, MainDbAdapter.COL_LIST_GEN_ROWID_NAME,
	                new String[]{MainDbAdapter.COL_NAME_GEN_NAME}, MainDbAdapter.WHERE_CONDITION_ISACTIVE, null,
	                MainDbAdapter.COL_NAME_GEN_NAME,
	                mCarId, false);
    	}
    	
    	if(lDriverZone != null){
	    	checkID = mDbAdapter.isSingleActiveRecord(MainDbAdapter.TABLE_NAME_DRIVER, null); 
	    	if(checkID > -1){ //one single driver
	    		mDriverId = checkID;
	    		lDriverZone.setVisibility(View.GONE);
	    	}
	    	else{
	    		lDriverZone.setVisibility(View.VISIBLE);
		        initSpinner(spnDriver, MainDbAdapter.TABLE_NAME_DRIVER, MainDbAdapter.COL_LIST_GEN_ROWID_NAME,
		                new String[]{MainDbAdapter.COL_NAME_GEN_NAME}, MainDbAdapter.WHERE_CONDITION_ISACTIVE, null,
		                MainDbAdapter.COL_NAME_GEN_NAME, mDriverId, false);
	    	}
    	}
    	else{
	        initSpinner(spnDriver, MainDbAdapter.TABLE_NAME_DRIVER, MainDbAdapter.COL_LIST_GEN_ROWID_NAME,
	                new String[]{MainDbAdapter.COL_NAME_GEN_NAME}, MainDbAdapter.WHERE_CONDITION_ISACTIVE, null,
	                MainDbAdapter.COL_NAME_GEN_NAME, mDriverId, false);
    	}

    	if(lExpTypeZone != null){
	    	checkID = mDbAdapter.isSingleActiveRecord(MainDbAdapter.TABLE_NAME_EXPENSETYPE, null); 
	    	if(checkID > -1){ //one single type
	    		mExpTypeId = checkID;
	    		lExpTypeZone.setVisibility(View.GONE);
	    	}
	    	else{
	    		lExpTypeZone.setVisibility(View.VISIBLE);
		        initSpinner(spnExpType, MainDbAdapter.TABLE_NAME_EXPENSETYPE,
		                MainDbAdapter.COL_LIST_GEN_ROWID_NAME, new String[]{MainDbAdapter.COL_NAME_GEN_NAME},
		                MainDbAdapter.WHERE_CONDITION_ISACTIVE, null, MainDbAdapter.COL_NAME_GEN_NAME,
		                mExpTypeId, false);
	    	}
    	}
    	else{
	        initSpinner(spnExpType, MainDbAdapter.TABLE_NAME_EXPENSETYPE,
	                MainDbAdapter.COL_LIST_GEN_ROWID_NAME, new String[]{MainDbAdapter.COL_NAME_GEN_NAME},
	                MainDbAdapter.WHERE_CONDITION_ISACTIVE, null, MainDbAdapter.COL_NAME_GEN_NAME,
	                mExpTypeId, false);
    	}
    	
    	if(lExpCatZone != null){
	    	checkID = mDbAdapter.isSingleActiveRecord(MainDbAdapter.TABLE_NAME_EXPENSECATEGORY, MainDbAdapter.COL_NAME_EXPENSECATEGORY__ISFUEL + "='Y'"); 
	    	if(checkID > -1){ //one single type
	    		mExpCategoryId= checkID;
	    		lExpCatZone.setVisibility(View.GONE);
	    	}
	    	else{
	    		lExpCatZone.setVisibility(View.VISIBLE);
	        	initSpinner(spnExpCategory, MainDbAdapter.TABLE_NAME_EXPENSECATEGORY, MainDbAdapter.COL_LIST_GEN_ROWID_NAME, 
	                    new String[]{MainDbAdapter.COL_NAME_GEN_NAME}, 
	                    MainDbAdapter.WHERE_CONDITION_ISACTIVE + " AND " + MainDbAdapter.COL_NAME_EXPENSECATEGORY__ISFUEL + " = 'Y'", null,
	                    MainDbAdapter.COL_NAME_GEN_NAME, mExpCategoryId, false);
	    	}
    	}
    	else{
        	initSpinner(spnExpCategory, MainDbAdapter.TABLE_NAME_EXPENSECATEGORY, MainDbAdapter.COL_LIST_GEN_ROWID_NAME, 
                    new String[]{MainDbAdapter.COL_NAME_GEN_NAME}, 
                    MainDbAdapter.WHERE_CONDITION_ISACTIVE + " AND " + MainDbAdapter.COL_NAME_EXPENSECATEGORY__ISFUEL + " = 'Y'", null,
                    MainDbAdapter.COL_NAME_GEN_NAME, mExpCategoryId, false);
    	}

        initSpinner(spnUomVolume, MainDbAdapter.TABLE_NAME_UOM, MainDbAdapter.COL_LIST_GEN_ROWID_NAME,
                new String[]{MainDbAdapter.COL_NAME_GEN_NAME}, MainDbAdapter.COL_NAME_UOM__UOMTYPE + "='" + StaticValues.UOM_VOLUME_TYPE_CODE + "'" + MainDbAdapter.WHERE_CONDITION_ISACTIVE_ANDPREFIX, null,
                MainDbAdapter.COL_NAME_GEN_NAME, mUomVolumeId, false);
        initSpinner(spnCurrency, MainDbAdapter.TABLE_NAME_CURRENCY, MainDbAdapter.COL_LIST_GEN_ROWID_NAME,
                new String[]{MainDbAdapter.COL_NAME_GEN_NAME}, MainDbAdapter.WHERE_CONDITION_ISACTIVE, null,
                MainDbAdapter.COL_NAME_GEN_NAME, mCurrencyId, false);

        userCommentAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                mDbAdapter.getAutoCompleteText(MainDbAdapter.TABLE_NAME_REFUEL, null,
                mCarId, 30));
        acUserComment.setAdapter(userCommentAdapter);
        bpartnerNameAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                mDbAdapter.getAutoCompleteText(MainDbAdapter.TABLE_NAME_BPARTNER, null,
                0, 0));
        acBPartner.setAdapter(bpartnerNameAdapter);
        addressAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                mDbAdapter.getAutoCompleteText(MainDbAdapter.TABLE_NAME_BPARTNERLOCATION, MainDbAdapter.COL_NAME_BPARTNERLOCATION__ADDRESS,
                mBPartnerId, 0));
        acAdress.setAdapter(addressAdapter);
        tagAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                mDbAdapter.getAutoCompleteText(MainDbAdapter.TABLE_NAME_TAG, null,
                0, 0));
        acTag.setAdapter(tagAdapter);
        
        if(acAdress != null)
        	acAdress.setOnKeyListener(this);
        if(acBPartner != null)
        	acBPartner.setOnKeyListener(this);
        if(acTag != null)
        	acTag.setOnKeyListener(this);
        if(acUserComment != null)
        	acUserComment.setOnKeyListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isBackgroundSettingsActive = true;

        if(carDefaultCurrencyId != mCurrencyId)
            setConversionRateVisibility(true);
        else
            setConversionRateVisibility(false);

        if(carDefaultUOMVolumeId != mUomVolumeId)
            setBaseUOMQtyZoneVisibility(true);
        else
            setBaseUOMQtyZoneVisibility(false);
        if(acBPartner.getText().toString() == null || acBPartner.getText().toString().length() == 0){
            acAdress.setEnabled(false);
            acAdress.setText(null);
            acAdress.setHint(mResource.getString(R.string.RefuelEditActivity_GasStation).replace(":", "") + " " +
                                mResource.getString(R.string.GEN_Required).replace(":", ""));
        }
        else{
            acAdress.setEnabled(true);
            acAdress.setHint(null);
        }

        calculatePriceAmount();
        calculateBaseUOMQty();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("mCurrencyId", mCurrencyId);
        outState.putLong("carDefaultCurrencyId", carDefaultCurrencyId);
        outState.putLong("carDefaultUOMVolumeId", carDefaultUOMVolumeId);
        outState.putLong("mUomVolumeId", mUomVolumeId);

        if(priceConverted != null)
            outState.putString("priceConverted", priceConverted.toString());
        if(priceEntered != null)
            outState.putString("priceEntered", priceEntered.toString());
        if(amountConverted != null)
            outState.putString("amountConverted", amountConverted.toString());
        if(amountEntered != null)
            outState.putString("amountEntered", amountEntered.toString());

        if(currencyConversionRate != null)
            outState.putString("currencyConversionRate", currencyConversionRate.toString());
        if(uomVolumeConversionRate != null)
            outState.putString("uomVolumeConversionRate", uomVolumeConversionRate.toString());
        if(baseUomQty != null)
            outState.putString("baseUomQty", baseUomQty.toString());
        outState.putString("carDefaultCurrencyCode", carDefaultCurrencyCode);
        outState.putString("currencyCode", currencyCode);
        outState.putString("carDefaultUOMVolumeCode", carDefaultUOMVolumeCode);
        outState.putString("operationType", operationType);
        outState.putInt("mInsertMode", mInsertMode);
        outState.putLong("mCarId", mCarId);
        outState.putLong("mDriverId", mDriverId);
        outState.putLong("mExpCategoryId", mExpCategoryId);
        outState.putLong("mExpTypeId", mExpTypeId);

    }

    @Override
    protected void onRestoreInstanceState (Bundle savedInstanceState){
        try{
            init();
            super.onRestoreInstanceState(savedInstanceState);
            mCurrencyId = savedInstanceState.getLong("mCurrencyId");
            carDefaultCurrencyId = savedInstanceState.getLong("carDefaultCurrencyId");
            carDefaultUOMVolumeId = savedInstanceState.getLong("carDefaultUOMVolumeId");
            mUomVolumeId = savedInstanceState.getLong("mUomVolumeId");

            if(savedInstanceState.containsKey("amountEntered"))
                priceConverted = new BigDecimal(savedInstanceState.getString("amountEntered"));
            if(savedInstanceState.containsKey("priceEntered"))
                priceEntered = new BigDecimal(savedInstanceState.getString("priceEntered"));
            if(savedInstanceState.containsKey("amountConverted"))
                amountConverted = new BigDecimal(savedInstanceState.getString("amountConverted"));
            if(savedInstanceState.containsKey("amountEntered"))
                amountEntered = new BigDecimal(savedInstanceState.getString("amountEntered"));

            if(savedInstanceState.containsKey("currencyConversionRate"))
                currencyConversionRate = new BigDecimal(savedInstanceState.getString("currencyConversionRate"));
            if(savedInstanceState.containsKey("uomVolumeConversionRate"))
                uomVolumeConversionRate = new BigDecimal(savedInstanceState.getString("uomVolumeConversionRate"));
            if(savedInstanceState.containsKey("baseUomQty"))
                baseUomQty = new BigDecimal(savedInstanceState.getString("baseUomQty"));
            carDefaultCurrencyCode = savedInstanceState.getString("carDefaultCurrencyCode");
            currencyCode = savedInstanceState.getString("currencyCode");
            carDefaultUOMVolumeCode = savedInstanceState.getString("carDefaultUOMVolumeCode");
            operationType = savedInstanceState.getString("operationType");
            mInsertMode = savedInstanceState.getInt("mInsertMode");
            mCarId = savedInstanceState.getLong("mCarId");
            mDriverId = savedInstanceState.getLong("mDriverId");
            mExpCategoryId = savedInstanceState.getLong("mExpCategoryId");
            mExpTypeId = savedInstanceState.getLong("mExpTypeId");

            initControls();
            calculateBaseUOMQty();
//            calculatePriceAmount();
            initDateTime(mlDateTimeInSeconds * 1000);
            setInsertMode(mInsertMode);
        }
        catch(NumberFormatException e){}
    }

    //change the address autocomplete list when the vendor change
    private View.OnFocusChangeListener vendorChangeListener = new View.OnFocusChangeListener() {

        public void onFocusChange(View view, boolean hasFocus) {
            if(!hasFocus){
                String selection = "UPPER (" + MainDbAdapter.COL_NAME_GEN_NAME + ") = ?";
                String[] selectionArgs = {acBPartner.getText().toString().toUpperCase()};
                Cursor c = mDbAdapter.query(MainDbAdapter.TABLE_NAME_BPARTNER, MainDbAdapter.COL_LIST_GEN_ROWID_NAME, selection, selectionArgs,
                        null, null, null);
//                Cursor c = mDbAdapter.fetchForTable(MainDbAdapter.BPARTNER_TABLE_NAME, MainDbAdapter.genColName,
//                            "UPPER(" + MainDbAdapter.GEN_COL_NAME_NAME + ") = '" + acBPartner.getText().toString().toUpperCase() + "'", null);
                String bPartnerIdStr = null;
                if(c.moveToFirst())
                    bPartnerIdStr = c.getString(MainDbAdapter.COL_POS_GEN_ROWID);
                c.close();
                if(bPartnerIdStr != null && bPartnerIdStr.length() > 0)
                    mBPartnerId = Long.parseLong(bPartnerIdStr);
                else
                    mBPartnerId = 0;
                addressAdapter = new ArrayAdapter<String>(RefuelEditActivity.this, android.R.layout.simple_list_item_1,
                        mDbAdapter.getAutoCompleteText(MainDbAdapter.TABLE_NAME_BPARTNERLOCATION, MainDbAdapter.COL_NAME_BPARTNERLOCATION__ADDRESS,
                        mBPartnerId, 0));
                acAdress.setAdapter(addressAdapter);
                
            }
        }
    };

    private AdapterView.OnItemSelectedListener spinnerCarOnItemSelectedListener =
            new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    if(isBackgroundSettingsActive)
                        return;
                    setCarId(arg3);
                    
                    //change the currency
                    long newCarCurrencyId = mDbAdapter.getCarCurrencyID(mCarId);
                    if(newCarCurrencyId != mCurrencyId){
                        initSpinner(spnCurrency, MainDbAdapter.TABLE_NAME_CURRENCY,
                                MainDbAdapter.COL_LIST_GEN_ROWID_NAME, new String[]{MainDbAdapter.COL_NAME_GEN_NAME},
                                    MainDbAdapter.WHERE_CONDITION_ISACTIVE, null,
                                    MainDbAdapter.COL_NAME_GEN_NAME,
                                    newCarCurrencyId, false);
                        mCurrencyId = newCarCurrencyId;
                        carDefaultCurrencyId = mCurrencyId;
                        carDefaultCurrencyCode = mDbAdapter.getCurrencyCode(carDefaultCurrencyId);
                        currencyConversionRate = BigDecimal.ONE;

                        setConversionRateVisibility(false);
                        calculatePriceAmount();
                    }
                    long newCarUOMVolumeId = mDbAdapter.getCarUOMVolumeID(mCarId);
                    if(newCarUOMVolumeId != mUomVolumeId){
                        mUomVolumeId = newCarUOMVolumeId;
                        carDefaultUOMVolumeId = mUomVolumeId;
                        carDefaultUOMVolumeCode = mDbAdapter.getUOMCode(carDefaultUOMVolumeId);
                        initSpinner(spnUomVolume, MainDbAdapter.TABLE_NAME_UOM,
                                MainDbAdapter.COL_LIST_GEN_ROWID_NAME, new String[]{MainDbAdapter.COL_NAME_GEN_NAME},
                                MainDbAdapter.COL_NAME_UOM__UOMTYPE + "='" + StaticValues.UOM_VOLUME_TYPE_CODE + "'" + MainDbAdapter.WHERE_CONDITION_ISACTIVE_ANDPREFIX, null,
                                MainDbAdapter.COL_NAME_GEN_NAME, mUomVolumeId, false);
                        setBaseUOMQtyZoneVisibility(false);
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
                }
                public void onNothingSelected(AdapterView<?> arg0) {
                }
            };

    private AdapterView.OnItemSelectedListener spinnerUOMOnItemSelectedListener =
            new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    setSpinnerTextToCode(arg0, arg3, arg1);
                    if(isBackgroundSettingsActive)
                        return;
                    setUOMVolumeId(arg3);
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
                    acAdress.setHint(mResource.getString(R.string.RefuelEditActivity_GasStation).replace(":", "") + " " +
                                        mResource.getString(R.string.GEN_Required).replace(":", ""));
                }
                else{
                    acAdress.setEnabled(true);
                    acAdress.setHint(null);
                }
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
                        etConversionRate.getText().toString().length() > 0){
                    try{
                        currencyConversionRate = new BigDecimal(etConversionRate.getText().toString());
                    }
                    catch(NumberFormatException e){
                        currencyConversionRate = null;
                    }
                }
                if(mUomVolumeId != carDefaultUOMVolumeId && etQty.getText().toString() != null &&
                        etQty.toString().length() > 0)
                    calculateBaseUOMQty();
                calculatePriceAmount();
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

    private void calculatePriceAmount() {
        String qtyStr = etQty.getText().toString();
        String userInputStr = etUserInput.getText().toString();
        String calculatedValueStr = "";
        BigDecimal calculatedValue;

        amountConverted = null;
        if(qtyStr != null && qtyStr.length() > 0
                && userInputStr != null && userInputStr.length() > 0) {
            try{
            	BigDecimal qtyBd = new BigDecimal(qtyStr);
            	if(qtyBd.signum() == 0)
            		return;

            	if(mInsertMode == INSERTMODE_PRICE){ //calculate amount
                    priceEntered = new BigDecimal(userInputStr);
                    calculatedValue = qtyBd.multiply(priceEntered);
                    amountEntered = calculatedValue;
                }
                else{ //INSERTMODE_AMOUNT - calculate price
                    amountEntered = new BigDecimal(userInputStr);
                    calculatedValue = amountEntered.divide(qtyBd, 10, RoundingMode.HALF_UP);
                    priceEntered = calculatedValue;
                }
                calculatedValueStr =
                	Utils.numberToString(calculatedValue, true, StaticValues.DECIMALS_AMOUNT, StaticValues.ROUNDING_MODE_AMOUNT)
                            + " " + currencyCode;
                tvCalculatedTextContent.setText(calculatedValueStr);
                if(carDefaultCurrencyId != mCurrencyId && currencyConversionRate != null){
                    amountConverted = amountEntered.multiply(currencyConversionRate);
                    priceConverted = priceEntered.multiply(currencyConversionRate);

                    if(carDefaultCurrencyCode == null)
                    	carDefaultCurrencyCode = "";
                    calculatedValueStr =
                            (mResource.getString(R.string.GEN_ConvertedPriceLabel)).replace("[#1]", carDefaultCurrencyCode) + " = " +
                        		Utils.numberToString(priceConverted, true, StaticValues.DECIMALS_AMOUNT, StaticValues.ROUNDING_MODE_AMOUNT)
                                    + "; " +
                            (mResource.getString(R.string.GEN_ConvertedAmountLabel)).replace("[#1]", carDefaultCurrencyCode) + " = " +
                    			Utils.numberToString(amountConverted, true, StaticValues.DECIMALS_AMOUNT, StaticValues.ROUNDING_MODE_AMOUNT);
                    tvConvertedAmountLabel.setText(calculatedValueStr);
                }
            }
            catch(NumberFormatException e){
            }
        }
    }

    private void calculateBaseUOMQty() {
        if(mUomVolumeId == carDefaultUOMVolumeId){
            return;
        }

        if(uomVolumeConversionRate == null){
            tvBaseUOMQtyValue.setText(mResource.getString(R.string.RefuelEditActivity_NoConversionRateMessage));
            return;
        }
        String qtyStr = etQty.getText().toString();
        String amountStr;
        if(qtyStr != null && qtyStr.length() > 0) {
            try{
                baseUomQty = (new BigDecimal(qtyStr)).multiply(uomVolumeConversionRate);
                amountStr = 
        			Utils.numberToString(baseUomQty, true, StaticValues.DECIMALS_VOLUME, StaticValues.ROUNDING_MODE_VOLUME)
                		+ " " + carDefaultUOMVolumeCode;

                tvBaseUOMQtyValue.setText(amountStr);
            }
            catch(NumberFormatException e){}
        }
    }

    private void setConversionRateVisibility(boolean visible){
        if(visible){
            llConvertedAmountZone.setVisibility(View.VISIBLE);
            if(llConversionRateZone != null)
            	llConversionRateZone.setVisibility(View.VISIBLE);
            etConversionRate.setVisibility(View.VISIBLE);
            etConversionRate.setTag(mResource.getString(R.string.GEN_ConversionRateLabel));
            tvConversionRateLabel.setVisibility(View.VISIBLE);

        }else{
            llConvertedAmountZone.setVisibility(View.GONE);
            if(llConversionRateZone != null)
            	llConversionRateZone.setVisibility(View.GONE);
            etConversionRate.setTag(null);
            etConversionRate.setVisibility(View.INVISIBLE);
            tvConversionRateLabel.setVisibility(View.INVISIBLE);
        }
    }

    private void setBaseUOMQtyZoneVisibility(boolean visible){
        if(visible)
            llBaseUOMQtyZone.setVisibility(View.VISIBLE);
        else
            llBaseUOMQtyZone.setVisibility(View.GONE);
    }

    public void setInsertMode(int insertMode){
        mInsertMode = insertMode;
        if(mInsertMode == INSERTMODE_PRICE){
            tvCalculatedTextLabel.setText(mResource.getString(R.string.GEN_AmountLabel));
            etUserInput.setTag(mResource.getString(R.string.GEN_PriceLabel));
        }
        else{
            tvCalculatedTextLabel.setText(mResource.getString(R.string.GEN_PriceLabel));
            etUserInput.setTag(mResource.getString(R.string.GEN_AmountLabel));
        }
        calculatePriceAmount();
    }

    @Override
    protected boolean saveData() {
        
        //final calculations
        calculatePriceAmount();
        calculateBaseUOMQty();

        ContentValues data = new ContentValues();
        data.put( MainDbAdapter.COL_NAME_GEN_NAME,
                "Refuel");
        data.put( MainDbAdapter.COL_NAME_GEN_ISACTIVE, "Y");
        data.put( MainDbAdapter.COL_NAME_GEN_USER_COMMENT,
                acUserComment.getText().toString() );
        data.put( MainDbAdapter.COL_NAME_REFUEL__CAR_ID,
                mCarId);
        data.put( MainDbAdapter.COL_NAME_REFUEL__DRIVER_ID,
                mDriverId);
        data.put( MainDbAdapter.COL_NAME_REFUEL__EXPENSECATEGORY_ID,
                mExpCategoryId);
        data.put( MainDbAdapter.COL_NAME_REFUEL__EXPENSETYPE_ID,
                mExpTypeId);
        data.put( MainDbAdapter.COL_NAME_REFUEL__INDEX, etCarIndex.getText().toString());
        data.put( MainDbAdapter.COL_NAME_REFUEL__QUANTITYENTERED, etQty.getText().toString());
        data.put( MainDbAdapter.COL_NAME_REFUEL__UOMVOLUMEENTERED_ID,
                spnUomVolume.getSelectedItemId());
        //just for 
        calculatePriceAmount();
        if(priceEntered == null || amountEntered == null){
            Toast toast = Toast.makeText( getApplicationContext(),
                    mResource.getString(R.string.GEN_PriceLabel) + ": " + mResource.getString(R.string.GEN_Required), Toast.LENGTH_SHORT );
            toast.show();
            etUserInput.setText("");
            return false;
        }
        	
        data.put( MainDbAdapter.COL_NAME_REFUEL__PRICEENTERED, priceEntered.toString());
        data.put( MainDbAdapter.COL_NAME_REFUEL__AMOUNTENTERED, amountEntered.toString());
        data.put( MainDbAdapter.COL_NAME_REFUEL__CURRENCYENTERED_ID,
                spnCurrency.getSelectedItemId());
        data.put( MainDbAdapter.COL_NAME_REFUEL__DATE, mlDateTimeInSeconds);
        data.put( MainDbAdapter.COL_NAME_REFUEL__DOCUMENTNO,
                etDocNo.getText().toString());
        data.put( MainDbAdapter.COL_NAME_REFUEL__ISFULLREFUEL,
                (ckIsFullRefuel.isChecked() ? "Y" : "N"));

        if(mUomVolumeId == carDefaultUOMVolumeId){
            data.put( MainDbAdapter.COL_NAME_REFUEL__QUANTITY, etQty.getText().toString());
            data.put( MainDbAdapter.COL_NAME_REFUEL__UOMVOLUME_ID, spnUomVolume.getSelectedItemId());
            data.put( MainDbAdapter.COL_NAME_REFUEL__UOMVOLCONVERSIONRATE, "1");
        }
        else{
            data.put( MainDbAdapter.COL_NAME_REFUEL__QUANTITY, baseUomQty.toString());
            data.put( MainDbAdapter.COL_NAME_REFUEL__UOMVOLUME_ID, carDefaultUOMVolumeId);
            data.put( MainDbAdapter.COL_NAME_REFUEL__UOMVOLCONVERSIONRATE, uomVolumeConversionRate.toString());
        }

        if(mCurrencyId == carDefaultCurrencyId){
            data.put( MainDbAdapter.COL_NAME_REFUEL__PRICE, priceEntered.toString());
            data.put( MainDbAdapter.COL_NAME_REFUEL__AMOUNT, amountEntered.toString());
            data.put( MainDbAdapter.COL_NAME_REFUEL__CURRENCY_ID, carDefaultCurrencyId);
            data.put( MainDbAdapter.COL_NAME_REFUEL__CURRENCYRATE, "1");
        }
        else{
            data.put( MainDbAdapter.COL_NAME_REFUEL__PRICE, priceConverted.toString());
            data.put( MainDbAdapter.COL_NAME_REFUEL__AMOUNT, amountConverted.toString());
            data.put( MainDbAdapter.COL_NAME_REFUEL__CURRENCY_ID, carDefaultCurrencyId);
            data.put( MainDbAdapter.COL_NAME_REFUEL__CURRENCYRATE, currencyConversionRate.toString());
        }

        if(acBPartner.getText().toString() != null && acBPartner.getText().toString().length() > 0){
            String selection = "UPPER (" + MainDbAdapter.COL_NAME_GEN_NAME + ") = ?";
            String[] selectionArgs = {acBPartner.getText().toString().toUpperCase()};
            Cursor c = mDbAdapter.query(MainDbAdapter.TABLE_NAME_BPARTNER, MainDbAdapter.COL_LIST_GEN_ROWID_NAME, selection, selectionArgs,
                    null, null, null);
            String bPartnerIdStr = null;
            if(c.moveToFirst())
                bPartnerIdStr = c.getString(MainDbAdapter.COL_POS_GEN_ROWID);
            c.close();
            if(bPartnerIdStr != null && bPartnerIdStr.length() > 0){
                mBPartnerId = Long.parseLong(bPartnerIdStr);
                data.put(MainDbAdapter.COL_NAME_REFUEL__BPARTNER_ID, mBPartnerId);
            }
            else{
                ContentValues tmpData = new ContentValues();
                tmpData.put(MainDbAdapter.COL_NAME_GEN_NAME, acBPartner.getText().toString());
                mBPartnerId = mDbAdapter.createRecord(MainDbAdapter.TABLE_NAME_BPARTNER, tmpData);
                if(mBPartnerId >= 0)
                    data.put(MainDbAdapter.COL_NAME_REFUEL__BPARTNER_ID, mBPartnerId);
            }

            if(acAdress.getText().toString() != null && acAdress.getText().toString().length() > 0){
                selection = "UPPER (" + MainDbAdapter.COL_NAME_BPARTNERLOCATION__ADDRESS + ") = ? AND " +
                                            MainDbAdapter.COL_NAME_BPARTNERLOCATION__BPARTNER_ID + " = ?";
                String[] selectionArgs2 = {acAdress.getText().toString().toUpperCase(), Long.toString(mBPartnerId)};
                c = mDbAdapter.query(MainDbAdapter.TABLE_NAME_BPARTNERLOCATION, MainDbAdapter.COL_LIST_GEN_ROWID_NAME, selection, selectionArgs2,
                        null, null, null);
                String addressIdStr = null;
                if(c.moveToFirst())
                    addressIdStr = c.getString(MainDbAdapter.COL_POS_GEN_ROWID);
                c.close();
                if(addressIdStr != null && addressIdStr.length() > 0)
                    data.put(MainDbAdapter.COL_NAME_REFUEL__BPARTNER_LOCATION_ID, Long.parseLong(addressIdStr));
                else{
                    ContentValues tmpData = new ContentValues();
                    tmpData.put(MainDbAdapter.COL_NAME_BPARTNERLOCATION__BPARTNER_ID, mBPartnerId);
                    tmpData.put(MainDbAdapter.COL_NAME_GEN_NAME, acAdress.getText().toString());
                    tmpData.put(MainDbAdapter.COL_NAME_BPARTNERLOCATION__ADDRESS, acAdress.getText().toString());
                    long newAddressId = mDbAdapter.createRecord(MainDbAdapter.TABLE_NAME_BPARTNERLOCATION, tmpData);
                    if(newAddressId >= 0)
                        data.put(MainDbAdapter.COL_NAME_REFUEL__BPARTNER_LOCATION_ID, newAddressId);
                }
            }
            else
                data.put(MainDbAdapter.COL_NAME_REFUEL__BPARTNER_LOCATION_ID, (String)null);
        }
        else{
            data.put(MainDbAdapter.COL_NAME_REFUEL__BPARTNER_ID, (String)null);
            data.put(MainDbAdapter.COL_NAME_REFUEL__BPARTNER_LOCATION_ID, (String)null);
        }

        if(acTag.getText().toString() != null && acTag.getText().toString().length() > 0){
            String selection = "UPPER (" + MainDbAdapter.COL_NAME_GEN_NAME + ") = ?";
            String[] selectionArgs = {acTag.getText().toString().toUpperCase()};
            Cursor c = mDbAdapter.query(MainDbAdapter.TABLE_NAME_TAG, MainDbAdapter.COL_LIST_GEN_ROWID_NAME, selection, selectionArgs,
                    null, null, null);
            String tagIdStr = null;
            if(c.moveToFirst())
                tagIdStr = c.getString(MainDbAdapter.COL_POS_GEN_ROWID);
            c.close();
            if(tagIdStr != null && tagIdStr.length() > 0){
                mTagId = Long.parseLong(tagIdStr);
                data.put(MainDbAdapter.COL_NAME_REFUEL__TAG_ID, mTagId);
            }
            else{
                ContentValues tmpData = new ContentValues();
                tmpData.put(MainDbAdapter.COL_NAME_GEN_NAME, acTag.getText().toString());
                mTagId = mDbAdapter.createRecord(MainDbAdapter.TABLE_NAME_TAG, tmpData);
                if(mTagId >= 0)
                    data.put(MainDbAdapter.COL_NAME_REFUEL__TAG_ID, mTagId);
            }
        }
        else{
            data.put(MainDbAdapter.COL_NAME_REFUEL__TAG_ID, (String)null);
        }
        
        
        if( operationType.equals("N") ) {
            Long createResult = mDbAdapter.createRecord(MainDbAdapter.TABLE_NAME_REFUEL, data);
            if(createResult.intValue() < 0){
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
            int updResult = mDbAdapter.updateRecord(MainDbAdapter.TABLE_NAME_REFUEL, mRowId, data);
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
    	mPrefEditor.putLong("RefuelExpCategory_ID", mExpCategoryId);
    	mPrefEditor.putLong("RefuelExpenseType_ID", mExpTypeId);
		mPrefEditor.commit();
    	
		Intent intent = new Intent(this, ToDoNotificationService.class);
		intent.putExtra("setJustNextRun", false);
		intent.putExtra("CarID", mCarId);
		this.startService(intent);

		return true;
    }

    @Override
    protected void setLayout() {
   		setContentView(R.layout.refuel_edit_activity_s01);
    }

	/* (non-Javadoc)
	 * @see org.andicar.activity.BaseActivity#setSpecificLayout()
	 */
	@Override
	public void setSpecificLayout() {
        if(mUomVolumeId != carDefaultUOMVolumeId){
            setBaseUOMQtyZoneVisibility(true);
        }
        else{
            setBaseUOMQtyZoneVisibility(false);
        }

        uomVolumeConversionRate = mDbAdapter.getUOMConversionRate(mUomVolumeId, carDefaultUOMVolumeId);
        if(uomVolumeConversionRate == null)
            btnOk.setEnabled(false);
        else
            btnOk.setEnabled(true);
        calculateBaseUOMQty();

        if(mCurrencyId != carDefaultCurrencyId){
            setConversionRateVisibility(true);
        }
        else{
            setConversionRateVisibility(false);
        }
        currencyCode = mDbAdapter.getCurrencyCode(mCurrencyId);
        currencyConversionRate = mDbAdapter.getCurrencyRate(mCurrencyId, carDefaultCurrencyId);
        etConversionRate.setText("");
        if(currencyConversionRate != null){
            etConversionRate.append(currencyConversionRate.toString());
        }
        calculatePriceAmount();
	}

	/**
	 * @param carId the mCarId to set
	 */
	public void setCarId(long carId) {
		this.mCarId = carId;

        userCommentAdapter = null;
        userCommentAdapter = new ArrayAdapter<String>(RefuelEditActivity.this,
                android.R.layout.simple_list_item_1,
                mDbAdapter.getAutoCompleteText(MainDbAdapter.TABLE_NAME_REFUEL, null,
                mCarId, 30));
        acUserComment.setAdapter(userCommentAdapter);
        
        carDefaultCurrencyId = mDbAdapter.getCarCurrencyID(mCarId);
        carDefaultUOMVolumeId = mDbAdapter.getCarUOMVolumeID(mCarId);

	}
	
	/**
	 * @param driverId the mDriverId to set
	 */
	public void setDriverId(long driverId) {
		this.mDriverId = driverId;
	}
	/**
	 * @param driverId the mDriverId to set
	 */
	public void setUOMVolumeId(long uomId) {
	    mUomVolumeId = uomId;
	    setSpecificLayout();
	}

	/* (non-Javadoc)
	 * @see org.andicar.activity.EditActivityBase#setDefaultValues()
	 */
	@Override
	public void setDefaultValues() {
		isBackgroundSettingsActive = true;
		
		setCarId(mPreferences.getLong("CurrentCar_ID", -1));
		setSpinnerSelectedID(spnCar, mCarId);
        carDefaultCurrencyId =  mDbAdapter.getCarCurrencyID(mCarId);
       	carDefaultCurrencyCode = mDbAdapter.getCurrencyCode(carDefaultCurrencyId);

		setDriverId(mPreferences.getLong("LastDriver_ID", -1));
		setSpinnerSelectedID(spnDriver, mDriverId);

		setExpCategoryId(mPreferences.getLong("RefuelExpCategory_ID", -1));
		if(mExpCategoryId == -1 || //mPreferences.getLong("ExpenseExpCategory_ID" not exist
				!mDbAdapter.isIDActive(MainDbAdapter.TABLE_NAME_EXPENSECATEGORY, mExpCategoryId)){ 
			mExpCategoryId = mDbAdapter.getFirstActiveID(MainDbAdapter.TABLE_NAME_EXPENSECATEGORY, MainDbAdapter.COL_NAME_EXPENSECATEGORY__ISFUEL + "='Y'", MainDbAdapter.COL_NAME_GEN_NAME);
		}
		setSpinnerSelectedID(spnExpCategory, mExpCategoryId);

		setExpTypeId(mPreferences.getLong("RefuelExpenseType_ID", 1));
		if(mExpTypeId == -1 || //mPreferences.getLong("ExpenseExpCategory_ID" not exist
				!mDbAdapter.isIDActive(MainDbAdapter.TABLE_NAME_EXPENSETYPE, mExpTypeId)){ 
			mExpTypeId = mDbAdapter.getFirstActiveID(MainDbAdapter.TABLE_NAME_EXPENSETYPE, null, MainDbAdapter.COL_NAME_GEN_NAME);
		}
		setSpinnerSelectedID(spnExpType, mExpTypeId);

		setUOMVolumeId(mDbAdapter.getCarUOMVolumeID(mCarId));
		setSpinnerSelectedID(spnUomVolume, mUomVolumeId);

		setCurrencyId(carDefaultCurrencyId);
		setSpinnerSelectedID(spnCurrency, carDefaultCurrencyId);
        currencyCode = mDbAdapter.getCurrencyCode(mCurrencyId);

		initDateTime(System.currentTimeMillis());
        ckIsFullRefuel.setChecked(false);
        carDefaultUOMVolumeId = mUomVolumeId;
        carDefaultUOMVolumeCode = mDbAdapter.getUOMCode(carDefaultUOMVolumeId);
        setInsertMode(INSERTMODE_AMOUNT);
        rbInsertModeAmount.setChecked(true);
        acBPartner.setText(null);
        acAdress.setEnabled(false);
        acAdress.setText(null);
        acAdress.setHint(mResource.getString(R.string.RefuelEditActivity_GasStation).replace(":", "") + " " +
                mResource.getString(R.string.GEN_Required).replace(":", ""));
//        initControls();

        //init tag
        if(mPreferences.getBoolean("RememberLastTag", false) && mPreferences.getLong("LastTagId", 0) > 0){
            mTagId = mPreferences.getLong("LastTagId", 0);
            String selection = MainDbAdapter.COL_NAME_GEN_ROWID + "= ? ";
            String[] selectionArgs = {Long.toString(mTagId)};
            Cursor c = mDbAdapter.query(MainDbAdapter.TABLE_NAME_TAG, MainDbAdapter.COL_LIST_GEN_ROWID_NAME,
                        selection, selectionArgs, null, null, null);
            if(c.moveToFirst())
                acTag.setText(c.getString(MainDbAdapter.COL_POS_GEN_NAME));
            c.close();
        }
        else
        	acTag.setText(null);
        
        etConversionRate.setText(null);
        etQty.setText(null);
        etUserInput.setText(null);
        acUserComment.setText(null);
	}

}
