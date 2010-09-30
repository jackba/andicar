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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.andicar.utils.AndiCarStatistics;

/**
 *
 * @author miki
 */
public class RefuelEditActivity extends EditActivityBase {
    private AutoCompleteTextView acUserComment;
    private AutoCompleteTextView acBPartner;
    private AutoCompleteTextView acAdress;
    private AutoCompleteTextView acTag;
    private Spinner spnCar;
    private Spinner spnDriver;
    private Spinner spnCurrency;
    private Spinner spnUomVolume;
    private Spinner spnExpType;
    private Spinner spnExpCategory;
    private EditText etCarIndex;
    private EditText etQty;
    private EditText etUserInput;
    private EditText etDocNo;
    private TextView tvConvertedAmountLabel;
    private EditText etConversionRate;
    private LinearLayout llConversionRateZone;
    private CheckBox ckIsFullRefuel;
    private TextView tvCalculatedTextContent;
    private TextView tvCalculatedTextLabel;

    private LinearLayout llBaseUOMQtyZone;
    private TextView tvBaseUOMQtyLabel;
    private TextView tvBaseUOMQtyValue;
    private TextView tvConversionRateLabel;
    private RadioButton rbInsertModeAmount;
    private RadioButton rbInsertModePrice;


    private long mCurrencyId = 0;
    private long carDefaultCurrencyId = 0;
    private long mCarId = 0;
    private long mDriverId = 0;
    private long mExpCategoryId = 0;
    private long mExpTypeId = 0;
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

    private boolean isActivityOnLoading = true;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        if(icicle !=null)
            return; //restoe from previous state

        operationType = mBundleExtras.getString("Operation");

        init();

        if (operationType.equals("E")) {
            mRowId = mBundleExtras.getLong( MainDbAdapter.GEN_COL_ROWID_NAME );
            Cursor c = mDbAdapter.fetchRecord(MainDbAdapter.REFUEL_TABLE_NAME, MainDbAdapter.refuelTableColNames, mRowId);
            mCarId = c.getLong(MainDbAdapter.REFUEL_COL_CAR_ID_POS);
            mDriverId = c.getLong(MainDbAdapter.REFUEL_COL_DRIVER_ID_POS);
            mExpCategoryId = c.getLong(MainDbAdapter.REFUEL_COL_EXPENSECATEGORY_ID_POS);
            mExpTypeId = c.getLong(MainDbAdapter.REFUEL_COL_EXPENSETYPE_ID_POS);
            mUomVolumeId = c.getLong(MainDbAdapter.REFUEL_COL_UOMVOLUMEENTERED_ID_POS);
            mCurrencyId = c.getLong(MainDbAdapter.REFUEL_COL_CURRENCYENTERED_ID_POS);
            Cursor c2 = null;
            if(c.getString(MainDbAdapter.REFUEL_COL_BPARTNER_ID_POS) != null
                    && c.getString(MainDbAdapter.REFUEL_COL_BPARTNER_ID_POS).length() > 0){
                mBPartnerId = c.getLong(MainDbAdapter.REFUEL_COL_BPARTNER_ID_POS);
                String selection = MainDbAdapter.GEN_COL_ROWID_NAME + "= ? ";
                String[] selectionArgs = {Long.toString(mBPartnerId)};
                c2 = mDbAdapter.query(MainDbAdapter.BPARTNER_TABLE_NAME, MainDbAdapter.genColName,
                            selection, selectionArgs, null, null, null);
                if(c2.moveToFirst())
                    acBPartner.setText(c2.getString(MainDbAdapter.GEN_COL_NAME_POS));
                c2.close();

                if(c.getString(MainDbAdapter.REFUEL_COL_BPARTNER_LOCATION_ID_POS) != null
                        && c.getString(MainDbAdapter.REFUEL_COL_BPARTNER_LOCATION_ID_POS).length() > 0){
                    mAddressId = c.getLong(MainDbAdapter.REFUEL_COL_BPARTNER_LOCATION_ID_POS);
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
            if(c.getString(MainDbAdapter.REFUEL_COL_TAG_ID_POS) != null
                    && c.getString(MainDbAdapter.REFUEL_COL_TAG_ID_POS).length() > 0){
                mTagId = c.getLong(MainDbAdapter.REFUEL_COL_TAG_ID_POS);
                String selection = MainDbAdapter.GEN_COL_ROWID_NAME + "= ? ";
                String[] selectionArgs = {Long.toString(mTagId)};
                c2 = mDbAdapter.query(MainDbAdapter.TAG_TABLE_NAME, MainDbAdapter.genColName,
                            selection, selectionArgs, null, null, null);
                if(c2.moveToFirst())
                    acTag.setText(c2.getString(MainDbAdapter.GEN_COL_NAME_POS));
                c2.close();
            }

            try{
                currencyConversionRate = new BigDecimal(c.getString(MainDbAdapter.REFUEL_COL_CURRENCYRATE_POS));
                uomVolumeConversionRate = new BigDecimal(c.getString(MainDbAdapter.REFUEL_COL_UOMVOLCONVERSIONRATE_POS));
            }
            catch(NumberFormatException e){}
            etConversionRate.setText(currencyConversionRate.toString());
            initDateTime(c.getLong(MainDbAdapter.REFUEL_COL_DATE_POS) * 1000);
            etCarIndex.setText(c.getString(MainDbAdapter.REFUEL_COL_INDEX_POS));
            etQty.setText(c.getString(MainDbAdapter.REFUEL_COL_QUANTITYENTERED_POS));
            etUserInput.setText(c.getString(MainDbAdapter.REFUEL_COL_PRICEENTERED_POS));
            etDocNo.setText(c.getString(MainDbAdapter.REFUEL_COL_DOCUMENTNO_POS));
            acUserComment.setText(c.getString(MainDbAdapter.GEN_COL_USER_COMMENT_POS));
            ckIsFullRefuel.setChecked(c.getString(MainDbAdapter.REFUEL_COL_ISFULLREFUEL_POS).equals("Y"));

            carDefaultUOMVolumeId = mDbAdapter.getCarUOMVolumeID(mCarId);
            carDefaultUOMVolumeCode = mDbAdapter.getUOMCode(carDefaultUOMVolumeId);
            if(carDefaultUOMVolumeId != mUomVolumeId){
                tvBaseUOMQtyValue.setText(c.getString(MainDbAdapter.REFUEL_COL_QUANTITY_POS) +
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
            mCarId = mPreferences.getLong("CurrentCar_ID", -1);
            mDriverId = mPreferences.getLong("CurrentDriver_ID", -1);
            mExpCategoryId = mPreferences.getLong("RefuelExpenseCategory_ID", 1);
            mExpTypeId = mPreferences.getLong("RefuelExpenseType_ID", -1);
            mUomVolumeId = mPreferences.getLong("CarUOMVolume_ID", -1);
            mCurrencyId = mPreferences.getLong("CarCurrency_ID", -1);
            initDateTime(System.currentTimeMillis());
            ckIsFullRefuel.setChecked(false);
            carDefaultUOMVolumeId = mDbAdapter.getCarUOMVolumeID(mCarId);
            carDefaultUOMVolumeCode = mDbAdapter.getUOMCode(carDefaultUOMVolumeId);
            setInsertMode(INSERTMODE_AMOUNT);
            rbInsertModeAmount.setChecked(true);
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
        spnCar.setOnItemSelectedListener(spinnerCarDriverOnItemSelectedListener);
        spnCar.setOnTouchListener(spinnerOnTouchListener);
        spnDriver.setOnItemSelectedListener(spinnerCarDriverOnItemSelectedListener);
        spnDriver.setOnTouchListener(spinnerOnTouchListener);
        spnCurrency = (Spinner) findViewById( R.id.spnCurrency );
        spnCurrency.setOnItemSelectedListener(spinnerCurrencyOnItemSelectedListener);
        spnCurrency.setOnTouchListener(spinnerOnTouchListener);
        spnUomVolume = (Spinner) findViewById(R.id.spnUomVolume);
        spnUomVolume.setOnItemSelectedListener(spinnerUOMOnItemSelectedListener);
        spnUomVolume.setOnTouchListener(spinnerOnTouchListener);
        spnExpType = (Spinner) findViewById(R.id.spnExpType);
        spnExpCategory = (Spinner) findViewById(R.id.spnExpCategory);
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
        rbInsertModePrice = (RadioButton) findViewById(R.id.rbInsertModePrice);
        rbInsertModeAmount = (RadioButton) findViewById(R.id.rbInsertModeAmount);
        RadioGroup rg = (RadioGroup) findViewById(R.id.rgExpenseInsertMode);
        rg.setOnCheckedChangeListener(rgOnCheckedChangeListener);
        llBaseUOMQtyZone = (LinearLayout) findViewById(R.id.llBaseUOMQtyZone);
        tvBaseUOMQtyLabel = (TextView) findViewById(R.id.tvBaseUOMQtyLabel);
        tvBaseUOMQtyLabel.setText(mResource.getString(R.string.RefuelEditActivity_QtyInBaseUOMLabel));
        tvBaseUOMQtyValue = (TextView) findViewById(R.id.tvBaseUOMQtyValue);
        setBaseUOMQtyZoneVisibility(false);
        carDefaultCurrencyId = mPreferences.getLong("CarCurrency_ID", -1);
        carDefaultCurrencyCode = mDbAdapter.getCurrencyCode(carDefaultCurrencyId);
        currencyCode = carDefaultCurrencyCode;
        currencyConversionRate = BigDecimal.ONE;
        uomVolumeConversionRate = BigDecimal.ONE;
    }

    private void initControls() {
        initSpinner(spnCar, MainDbAdapter.CAR_TABLE_NAME, MainDbAdapter.genColName,
                new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, MainDbAdapter.isActiveCondition, null,
                MainDbAdapter.GEN_COL_NAME_NAME,
                mCarId, false);
        initSpinner(spnDriver, MainDbAdapter.DRIVER_TABLE_NAME, MainDbAdapter.genColName, 
                new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, MainDbAdapter.isActiveCondition, null,
                MainDbAdapter.GEN_COL_NAME_NAME, mDriverId, false);
        initSpinner(spnExpType, MainDbAdapter.EXPENSETYPE_TABLE_NAME, MainDbAdapter.genColName, 
                new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, MainDbAdapter.isActiveCondition, null,
                MainDbAdapter.GEN_COL_NAME_NAME, mExpTypeId, false);
        initSpinner(spnExpCategory, MainDbAdapter.EXPENSECATEGORY_TABLE_NAME, MainDbAdapter.genColName, 
                new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, MainDbAdapter.isActiveCondition, null,
                MainDbAdapter.GEN_COL_NAME_NAME, mExpCategoryId, false);
        initSpinner(spnUomVolume, MainDbAdapter.UOM_TABLE_NAME, MainDbAdapter.genColName,
                new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, MainDbAdapter.UOM_COL_UOMTYPE_NAME + "='" + StaticValues.UOM_VOLUME_TYPE_CODE + "'" + MainDbAdapter.isActiveWithAndCondition, null,
                MainDbAdapter.GEN_COL_NAME_NAME, mUomVolumeId, false);
        initSpinner(spnCurrency, MainDbAdapter.CURRENCY_TABLE_NAME, MainDbAdapter.genColName,
                new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, MainDbAdapter.isActiveCondition, null,
                MainDbAdapter.GEN_COL_NAME_NAME, mCurrencyId, false);

        userCommentAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line,
                mDbAdapter.getAutoCompleteText(MainDbAdapter.REFUEL_TABLE_NAME, null,
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
        outState.putLong("mExpCategoryId", spnExpCategory.getSelectedItemId());
        outState.putLong("mExpTypeId", spnExpType.getSelectedItemId());

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
                String selection = "UPPER (" + MainDbAdapter.GEN_COL_NAME_NAME + ") = ?";
                String[] selectionArgs = {acBPartner.getText().toString().toUpperCase()};
                Cursor c = mDbAdapter.query(MainDbAdapter.BPARTNER_TABLE_NAME, MainDbAdapter.genColName, selection, selectionArgs,
                        null, null, null);
//                Cursor c = mDbAdapter.fetchForTable(MainDbAdapter.BPARTNER_TABLE_NAME, MainDbAdapter.genColName,
//                            "UPPER(" + MainDbAdapter.GEN_COL_NAME_NAME + ") = '" + acBPartner.getText().toString().toUpperCase() + "'", null);
                String bPartnerIdStr = null;
                if(c.moveToFirst())
                    bPartnerIdStr = c.getString(MainDbAdapter.GEN_COL_ROWID_POS);
                c.close();
                if(bPartnerIdStr != null && bPartnerIdStr.length() > 0)
                    mBPartnerId = Long.parseLong(bPartnerIdStr);
                else
                    mBPartnerId = 0;
                addressAdapter = new ArrayAdapter<String>(RefuelEditActivity.this, android.R.layout.simple_dropdown_item_1line,
                        mDbAdapter.getAutoCompleteText(MainDbAdapter.BPARTNER_LOCATION_TABLE_NAME, MainDbAdapter.BPARTNER_LOCATION_ADDRESS_NAME,
                        mBPartnerId, 0));
                acAdress.setAdapter(addressAdapter);
                
            }
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
                    userCommentAdapter = new ArrayAdapter<String>(RefuelEditActivity.this,
                            android.R.layout.simple_dropdown_item_1line,
                            mDbAdapter.getAutoCompleteText(MainDbAdapter.REFUEL_TABLE_NAME, null,
                            mCarId, 30));
                    acUserComment.setAdapter(userCommentAdapter);

                    //change the currency
                    long newCarCurrencyId = mDbAdapter.getCarCurrencyID(mCarId);
                    if(newCarCurrencyId != mCurrencyId){
                        initSpinner(spnCurrency, MainDbAdapter.CURRENCY_TABLE_NAME,
                                MainDbAdapter.genColName, new String[]{MainDbAdapter.GEN_COL_NAME_NAME},
                                    MainDbAdapter.isActiveCondition, null,
                                    MainDbAdapter.GEN_COL_NAME_NAME,
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
                        initSpinner(spnUomVolume, MainDbAdapter.UOM_TABLE_NAME,
                                MainDbAdapter.genColName, new String[]{MainDbAdapter.GEN_COL_NAME_NAME},
                                MainDbAdapter.UOM_COL_UOMTYPE_NAME + "='" + StaticValues.UOM_VOLUME_TYPE_CODE + "'" + MainDbAdapter.isActiveWithAndCondition, null,
                                MainDbAdapter.GEN_COL_NAME_NAME, mUomVolumeId, false);
                        setBaseUOMQtyZoneVisibility(false);
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
                        setConversionRateVisibility(true);
                    }
                    else{
                        setConversionRateVisibility(false);
                    }
                    currencyCode = mDbAdapter.getCurrencyCode(mCurrencyId);
//                    fetchRecord(MainDbAdapter.CURRENCY_TABLE_NAME, MainDbAdapter.currencyTableColNames,
//                            mCurrencyId).getString(MainDbAdapter.CURRENCY_COL_CODE_POS);
                    currencyConversionRate = mDbAdapter.getCurrencyRate(mCurrencyId, carDefaultCurrencyId);
                    etConversionRate.setText("");
                    if(currencyConversionRate != null){
                        etConversionRate.append(currencyConversionRate.toString());
                    }

                    calculatePriceAmount();
                }
                public void onNothingSelected(AdapterView<?> arg0) {
                }
            };

    private AdapterView.OnItemSelectedListener spinnerUOMOnItemSelectedListener =
            new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    setSpinnerTextToCode(arg0, arg3, arg1);
                    if(isActivityOnLoading)
                        return;
                    mUomVolumeId = spnUomVolume.getSelectedItemId();
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
                calculatedValueStr = calculatedValue.setScale(StaticValues.DECIMALS_AMOUNT, StaticValues.ROUNDING_MODE_AMOUNT)
                            .toString() + " " + currencyCode;
                tvCalculatedTextContent.setText(calculatedValueStr);
                if(carDefaultCurrencyId != mCurrencyId && currencyConversionRate != null){
                    amountConverted = amountEntered.multiply(currencyConversionRate);
                    priceConverted = priceEntered.multiply(currencyConversionRate);

                    calculatedValueStr =
                            (mResource.getString(R.string.GEN_ConvertedPriceLabel)).replace("[%1]", carDefaultCurrencyCode) + " = " +
                                priceConverted.
                                    setScale(StaticValues.DECIMALS_AMOUNT, StaticValues.ROUNDING_MODE_AMOUNT).toString() + "; " +
                            (mResource.getString(R.string.GEN_ConvertedAmountLabel)).replace("[%1]", carDefaultCurrencyCode) + " = " +
                                amountConverted.
                                    setScale(StaticValues.DECIMALS_AMOUNT, StaticValues.ROUNDING_MODE_AMOUNT).toString();
                    tvConvertedAmountLabel.setText(calculatedValueStr);
                }
            }
            catch(NumberFormatException e){
//                if(isSendCrashReport)
//                    AndiCarStatistics.sendFlurryError("RefuelError",
//                            "NFE1: qtyStr = " + qtyStr + "; priceStr = " + userInputStr, this.toString());
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
                amountStr = baseUomQty.setScale(StaticValues.DECIMALS_VOLUME, StaticValues.ROUNDING_MODE_VOLUME)
                                .toString() + " " + carDefaultUOMVolumeCode;

                tvBaseUOMQtyValue.setText(amountStr);
            }
            catch(NumberFormatException e){}
        }
    }

    private void setConversionRateVisibility(boolean visible){
        if(visible){
            llConversionRateZone.setVisibility(View.VISIBLE);
            etConversionRate.setVisibility(View.VISIBLE);
            etConversionRate.setTag(mResource.getString(R.string.GEN_ConversionRateLabel));
            tvConversionRateLabel.setVisibility(View.VISIBLE);

        }else{
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

    private void setInsertMode(int insertMode){
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
                "Refuel");
        data.put( MainDbAdapter.GEN_COL_ISACTIVE_NAME, "Y");
        data.put( MainDbAdapter.GEN_COL_USER_COMMENT_NAME,
                acUserComment.getText().toString() );
        data.put( MainDbAdapter.REFUEL_COL_CAR_ID_NAME,
                mCarId);
        data.put( MainDbAdapter.REFUEL_COL_DRIVER_ID_NAME,
                mDriverId);
        data.put( MainDbAdapter.REFUEL_COL_EXPENSECATEGORY_NAME,
                spnExpCategory.getSelectedItemId() );
        data.put( MainDbAdapter.REFUEL_COL_EXPENSETYPE_ID_NAME,
                spnExpType.getSelectedItemId() );
        data.put( MainDbAdapter.REFUEL_COL_INDEX_NAME, etCarIndex.getText().toString());
        data.put( MainDbAdapter.REFUEL_COL_QUANTITYENTERED_NAME, etQty.getText().toString());
        data.put( MainDbAdapter.REFUEL_COL_UOMVOLUMEENTERED_ID_NAME,
                spnUomVolume.getSelectedItemId());
        data.put( MainDbAdapter.REFUEL_COL_PRICEENTERED_NAME, priceEntered.toString());
        data.put( MainDbAdapter.REFUEL_COL_AMOUNTENTERED_NAME, amountEntered.toString());
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
            data.put( MainDbAdapter.REFUEL_COL_PRICE_NAME, priceEntered.toString());
            data.put( MainDbAdapter.REFUEL_COL_AMOUNT_NAME, amountEntered.toString());
            data.put( MainDbAdapter.REFUEL_COL_CURRENCY_ID_NAME, carDefaultCurrencyId);
            data.put( MainDbAdapter.REFUEL_COL_CURRENCYRATE_NAME, "1");
        }
        else{
            data.put( MainDbAdapter.REFUEL_COL_PRICE_NAME, priceConverted.toString());
            data.put( MainDbAdapter.REFUEL_COL_AMOUNT_NAME, amountConverted.toString());
            data.put( MainDbAdapter.REFUEL_COL_CURRENCY_ID_NAME, carDefaultCurrencyId);
            data.put( MainDbAdapter.REFUEL_COL_CURRENCYRATE_NAME, currencyConversionRate.toString());
        }

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
                data.put(MainDbAdapter.REFUEL_COL_BPARTNER_ID_NAME, mBPartnerId);
            }
            else{
                ContentValues tmpData = new ContentValues();
                tmpData.put(MainDbAdapter.GEN_COL_NAME_NAME, acBPartner.getText().toString());
                mBPartnerId = mDbAdapter.createRecord(MainDbAdapter.BPARTNER_TABLE_NAME, tmpData);
                if(mBPartnerId >= 0)
                    data.put(MainDbAdapter.REFUEL_COL_BPARTNER_ID_NAME, mBPartnerId);
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
                    data.put(MainDbAdapter.REFUEL_COL_BPARTNER_LOCATION_ID_NAME, Long.parseLong(addressIdStr));
                else{
                    ContentValues tmpData = new ContentValues();
                    tmpData.put(MainDbAdapter.BPARTNER_LOCATION_BPARTNER_ID_NAME, mBPartnerId);
                    tmpData.put(MainDbAdapter.GEN_COL_NAME_NAME, acAdress.getText().toString());
                    tmpData.put(MainDbAdapter.BPARTNER_LOCATION_ADDRESS_NAME, acAdress.getText().toString());
                    long newAddressId = mDbAdapter.createRecord(MainDbAdapter.BPARTNER_LOCATION_TABLE_NAME, tmpData);
                    if(newAddressId >= 0)
                        data.put(MainDbAdapter.REFUEL_COL_BPARTNER_LOCATION_ID_NAME, newAddressId);
                }
            }
            else
                data.put(MainDbAdapter.REFUEL_COL_BPARTNER_LOCATION_ID_NAME, (String)null);
        }
        else{
            data.put(MainDbAdapter.REFUEL_COL_BPARTNER_ID_NAME, (String)null);
            data.put(MainDbAdapter.REFUEL_COL_BPARTNER_LOCATION_ID_NAME, (String)null);
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
                data.put(MainDbAdapter.REFUEL_COL_TAG_ID_NAME, mTagId);
            }
            else{
                ContentValues tmpData = new ContentValues();
                tmpData.put(MainDbAdapter.GEN_COL_NAME_NAME, acTag.getText().toString());
                mTagId = mDbAdapter.createRecord(MainDbAdapter.TAG_TABLE_NAME, tmpData);
                if(mTagId >= 0)
                    data.put(MainDbAdapter.REFUEL_COL_TAG_ID_NAME, mTagId);
            }
        }
        else{
            data.put(MainDbAdapter.REFUEL_COL_TAG_ID_NAME, (String)null);
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
            else{
            	if(mPreferences.getBoolean("RememberLastTag", false) && mTagId > 0){
            		mPrefEditor.putLong("LastTagId", mTagId);
            		mPrefEditor.commit();
            	}
                finish();
            }
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
            else{
            	if(mPreferences.getBoolean("RememberLastTag", false) && mTagId > 0){
            		mPrefEditor.putLong("LastTagId", mTagId);
            		mPrefEditor.commit();
            	}
                finish();
            }
        }
    }

    @Override
    protected void setLayout() {
        setContentView(R.layout.refuel_edit_activity);
    }

}
