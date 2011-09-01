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

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


/**
 *
 * @author miki
 */
public class MileageEditActivity extends EditActivityBase {
    //mileage insert mode: 0 = new index; 1 = mileage
    private int mInsertMode = 0;
    private long mUOMLengthId = -1;
    private long mGpsTrackId = -1;
    private long mTagId = 0;
    private String operationType = null;
    private BigDecimal mNewIndex = new BigDecimal("0");
    private BigDecimal mStartIndex = new BigDecimal("0");
    private BigDecimal mStopIndex = null;
    private BigDecimal mEntryMileageValue = BigDecimal.valueOf(0);
    private boolean isRecordMileage = false;

    private RadioButton rbInsertModeIndex;
    private RadioButton rbInsertModeMileage;
    private TextView tvCalculatedTextLabel;
    private TextView tvMileageRecInProgress;
    private EditText etStartIndex;
    private EditText etUserInput;
    private TextView tvCalculatedContent;
    private AutoCompleteTextView acUserComment;
    private AutoCompleteTextView acTag;
    ArrayAdapter<String> userCommentAdapter;
    private ArrayAdapter<String> tagAdapter;
    protected ImageButton btnStartStopMileageRecord = null;
    protected ImageButton btnOpenGPSTrack = null;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        isUseTemplate = true;
        super.onCreate(icicle);

        if(icicle !=null)
            return; //restored from previous state


        operationType = mBundleExtras.getString("Operation");
        init();
        
        if( operationType.equals("E") ) {
        	tvMileageRecInProgress.setVisibility(View.GONE);
        	btnStartStopMileageRecord.setVisibility(View.GONE);
        	isRecordMileage = false;
        	btnOk.setEnabled(true);
    		spnCar.setEnabled(false);
            mRowId = mBundleExtras.getLong( MainDbAdapter.GEN_COL_ROWID_NAME );
            
            Cursor c = mDbAdapter.fetchRecord(MainDbAdapter.MILEAGE_TABLE_NAME, MainDbAdapter.mileageTableColNames, mRowId);
            mCarId = c.getLong(MainDbAdapter.MILEAGE_COL_CAR_ID_POS);
            c.close();
            c = mDbAdapter.fetchRecord(MainDbAdapter.MILEAGE_TABLE_NAME, MainDbAdapter.mileageTableColNames, mRowId);
            mDriverId = c.getLong(MainDbAdapter.MILEAGE_COL_DRIVER_ID_POS);
            c.close();
            c = mDbAdapter.fetchRecord(MainDbAdapter.MILEAGE_TABLE_NAME, MainDbAdapter.mileageTableColNames, mRowId);
            try{
                mStartIndex = (new BigDecimal(c.getDouble(MainDbAdapter.MILEAGE_COL_INDEXSTART_POS))
                				.setScale(StaticValues.DECIMALS_LENGTH, StaticValues.ROUNDING_MODE_LENGTH));
                	
//                	new BigDecimal(c.getString(MainDbAdapter.MILEAGE_COL_INDEXSTART_POS));
                etStartIndex.setText(Utils.numberToString(mStartIndex, false, StaticValues.DECIMALS_LENGTH, StaticValues.ROUNDING_MODE_LENGTH));
                //mStartIndex.toPlainString());
                mStopIndex = (new BigDecimal(c.getDouble(MainDbAdapter.MILEAGE_COL_INDEXSTOP_POS))
            					.setScale(StaticValues.DECIMALS_LENGTH, StaticValues.ROUNDING_MODE_LENGTH)); 
                	
//                	new BigDecimal(c.getString(MainDbAdapter.MILEAGE_COL_INDEXSTOP_POS));
                etUserInput.setText(Utils.numberToString(mStopIndex, false, StaticValues.DECIMALS_LENGTH, StaticValues.ROUNDING_MODE_LENGTH));
                		//mStopIndex.toPlainString());
            }
            catch(NumberFormatException e){}
            rbInsertModeIndex.setChecked(true);
            mInsertMode = StaticValues.MILEAGE_INSERTMODE_INDEX;
            acUserComment.setText(c.getString(MainDbAdapter.GEN_COL_USER_COMMENT_POS));
            mExpTypeId = c.getLong(MainDbAdapter.MILEAGE_COL_EXPENSETYPE_ID_POS);
            
            //fill tag
            if(c.getString(MainDbAdapter.MILEAGE_COL_TAG_ID_POS) != null
                    && c.getString(MainDbAdapter.MILEAGE_COL_TAG_ID_POS).length() > 0){
                mTagId = c.getLong(MainDbAdapter.MILEAGE_COL_TAG_ID_POS);
                String selection = MainDbAdapter.GEN_COL_ROWID_NAME + "= ? ";
                String[] selectionArgs = {Long.toString(mTagId)};
                Cursor c2 = mDbAdapter.query(MainDbAdapter.TAG_TABLE_NAME, MainDbAdapter.genColName,
                            selection, selectionArgs, null, null, null);
                if(c2.moveToFirst())
                    acTag.setText(c2.getString(MainDbAdapter.GEN_COL_NAME_POS));
                c2.close();
            }
            
            initDateTime(c.getLong(MainDbAdapter.MILEAGE_COL_DATE_POS) * 1000);
            c.close();
            
            //get the gps track id (if exists)
            String selection = MainDbAdapter.GPSTRACK_COL_MILEAGE_ID_NAME + "= ? ";
            String[] selectionArgs = {Long.toString(mRowId)};
            Cursor c2 = mDbAdapter.query(MainDbAdapter.GPSTRACK_TABLE_NAME, MainDbAdapter.genColRowId,
                        selection, selectionArgs, null, null, null);
            if(c2.moveToFirst()){
                mGpsTrackId =  c2.getLong(0);
                btnOpenGPSTrack.setVisibility(View.VISIBLE);
            }
            else{
                btnOpenGPSTrack.setVisibility(View.GONE);
            }
            c2.close();
            
        }
        else if(operationType.equals("TrackToMileage")){
        	tvMileageRecInProgress.setVisibility(View.GONE);
        	btnStartStopMileageRecord.setVisibility(View.GONE);
            btnOpenGPSTrack.setVisibility(View.GONE);
        	isRecordMileage = false;
        	btnOk.setEnabled(true);
            mGpsTrackId = mBundleExtras.getLong("Track_ID");
            Cursor c = mDbAdapter.fetchRecord(MainDbAdapter.GPSTRACK_TABLE_NAME, MainDbAdapter.gpsTrackTableColNames, mGpsTrackId);
            mInsertMode = StaticValues.MILEAGE_INSERTMODE_INDEX;
            mCarId = c.getLong(MainDbAdapter.GPSTRACK_COL_CAR_ID_POS);
            mDriverId = c.getLong(MainDbAdapter.GPSTRACK_COL_DRIVER_ID_POS);
            acUserComment.setText(c.getString(MainDbAdapter.GEN_COL_USER_COMMENT_POS));
            mExpTypeId = mPreferences.getLong("MileageInsertExpenseType_ID", -1);
            acTag.setText(mBundleExtras.getString("Tag"));

            if(mPreferences.contains("GPSTrackStartIndex") &&
            		mPreferences.getString("GPSTrackStartIndex", null) != null){
            	String startIndex = mPreferences.getString("GPSTrackStartIndex", "");
            	try{
            		mStartIndex = new BigDecimal(startIndex);
            	}
            	catch(Exception e){
            		mStartIndex = BigDecimal.ZERO; 
            	}
            }
            else
            	mStartIndex = BigDecimal.ZERO;
            fillGetCurrentIndex();
            try{
                BigDecimal stopIndex = mStartIndex.add(new BigDecimal(c.getString(MainDbAdapter.GPSTRACK_COL_DISTANCE_POS))).setScale(0, BigDecimal.ROUND_HALF_DOWN);
                etUserInput.setText(stopIndex.toString());
            }
            catch(NumberFormatException e){}
            initDateTime(System.currentTimeMillis());
            c.close();
        }
        else{
        	btnStartStopMileageRecord.setVisibility(View.VISIBLE);
            btnOpenGPSTrack.setVisibility(View.GONE);

        	if(mPreferences.getBoolean("MileageRec_IsRecording", false)){ //mileage rec in progress
        		isRecordMileage = true;
            	tvMileageRecInProgress.setVisibility(View.VISIBLE);
        		spnCar.setEnabled(false);
        		spnDriver.setEnabled(false);
        		spnExpType.setEnabled(false);
        		acTag.setEnabled(false);
        		rbInsertModeIndex.setEnabled(false);
        		rbInsertModeMileage.setEnabled(false);
        		etStartIndex.setEnabled(false);
        		etUserInput.setEnabled(false);
        		acUserComment.setEnabled(false);
            	btnOk.setEnabled(false);
                btnStartStopMileageRecord.setImageDrawable(mResource.getDrawable(R.drawable.icon_mileage_stop_record_24x24));
        		mCarId = mPreferences.getLong("MileageRec_CarId", mBundleExtras.getLong("MileageRec_CarId"));
        		mDriverId = mPreferences.getLong("MileageRec_DriverId", mBundleExtras.getLong("MileageRec_DriverId"));
        		mExpTypeId = mPreferences.getLong("MileageRec_ExpenseTypeId", mBundleExtras.getLong("MileageRec_ExpenseTypeId"));
        		mInsertMode = mPreferences.getInt("MileageRec_InsertMode", mPreferences.getInt("MileageInsertMode", 0));
        		acTag.setText(mPreferences.getString("MileageRec_Tag", ""));
        		etStartIndex.setText(mPreferences.getString("MileageRec_StartIndex", ""));
        		acUserComment.setText(mPreferences.getString("MileageRec_Comment", ""));
        		try{
        			mStartIndex = new BigDecimal(mPreferences.getString("MileageRec_StartIndex", "0"));
        		}
        		catch(NumberFormatException e){
        			mStartIndex = BigDecimal.ZERO;
        		}
        		etUserInput.setEnabled(false);
	            initDateTime(System.currentTimeMillis());
//	            etUserInput.requestFocus();
            	if(mDet != null)
            		mDet.setControlsEnabled(false);
        	}
        	else{
        		setDefaultValues();
        	}
        }
        
        if(mCarId == -1){
            madbErrorAlert.setMessage(mResource.getString(R.string.ERR_051));
            madError = madbErrorAlert.create();
            madError.show();
            return;
        }

        initControls();
        Cursor c = mDbAdapter.fetchRecord(MainDbAdapter.CAR_TABLE_NAME, MainDbAdapter.carTableColNames, mCarId);
        if(c != null){
        	mUOMLengthId = c.getLong(MainDbAdapter.CAR_COL_UOMLENGTH_ID_POS);
        }
        else{
            madbErrorAlert.setMessage(mResource.getString(R.string.ERR_051));
            madError = madbErrorAlert.create();
            madError.show();
            return;
        }
    	c.close();

        if(isSendStatistics)
            AndiCarStatistics.sendFlurryEvent(this, "MileageEdit", null);
    }

    private void initControls(){
        initSpinner(spnCar, MainDbAdapter.CAR_TABLE_NAME, MainDbAdapter.genColName,
                new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, MainDbAdapter.isActiveCondition, null,
                MainDbAdapter.GEN_COL_NAME_NAME,
                mCarId, false);
        initSpinner(spnDriver, MainDbAdapter.DRIVER_TABLE_NAME, MainDbAdapter.genColName,
                new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, MainDbAdapter.isActiveCondition, null,
                MainDbAdapter.GEN_COL_NAME_NAME, mDriverId, false);
        initSpinner(spnExpType, MainDbAdapter.EXPENSETYPE_TABLE_NAME,
                MainDbAdapter.genColName, new String[]{MainDbAdapter.GEN_COL_NAME_NAME},
                MainDbAdapter.isActiveCondition, null, MainDbAdapter.GEN_COL_NAME_NAME,
                mExpTypeId, false);
        userCommentAdapter = new ArrayAdapter<String>(MileageEditActivity.this,
                android.R.layout.simple_dropdown_item_1line,
                mDbAdapter.getAutoCompleteText(MainDbAdapter.MILEAGE_TABLE_NAME, null, mCarId, 30));
        acUserComment.setAdapter(userCommentAdapter);
        tagAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line,
                mDbAdapter.getAutoCompleteText(MainDbAdapter.TAG_TABLE_NAME, null,
                0, 0));
        acTag.setAdapter(tagAdapter);

        if(mInsertMode == StaticValues.MILEAGE_INSERTMODE_INDEX) {
            rbInsertModeIndex.setChecked(true);
            tvCalculatedTextLabel.setText(
                    mResource.getString(R.string.MileageEditActivity_OptionMileageLabel) + ": ");
            etUserInput.setTag(mResource.getString(R.string.MileageEditActivity_OptionIndexLabel));
        }
        else {
            rbInsertModeMileage.setChecked(true);
            tvCalculatedTextLabel.setText(
                    mResource.getString(R.string.MileageEditActivity_OptionIndexLabel) + ": ");
            etUserInput.setTag(mResource.getString(R.string.MileageEditActivity_OptionMileageLabel));
        }
    }
    
    private void init(){
        tvCalculatedContent = (TextView) findViewById(R.id.tvCalculatedTextContent);
        etUserInput = (EditText) findViewById(R.id.etUserInput);
        etUserInput.addTextChangedListener(mileageTextWatcher);
        etStartIndex = (EditText) findViewById(R.id.etIndexStart);
        etStartIndex.addTextChangedListener(mileageTextWatcher);
        rbInsertModeIndex = (RadioButton) findViewById(R.id.rbInsertModeIndex);
        rbInsertModeMileage = (RadioButton) findViewById(R.id.rbInsertModeMileage);
        tvCalculatedTextLabel = ((TextView) findViewById(R.id.tvCalculatedTextLabel));
        spnExpType = (Spinner)findViewById(R.id.spnExpType);
        acUserComment = (AutoCompleteTextView)findViewById(R.id.acUserComment);
        acTag = ((AutoCompleteTextView) findViewById( R.id.acTag ));
        spnCar = (Spinner) findViewById(R.id.spnCar);
        spnCar.setOnItemSelectedListener(spinnerCarOnItemSelectedListener);
        spnCar.setOnTouchListener(spinnerOnTouchListener);
        spnDriver = (Spinner) findViewById(R.id.spnDriver);
        spnDriver.setOnItemSelectedListener(spinnerDriverOnItemSelectedListener);
        spnDriver.setOnTouchListener(spinnerOnTouchListener);
        btnStartStopMileageRecord = (ImageButton)findViewById( R.id.btnStartStopMileageRecord );
        btnStartStopMileageRecord.setOnClickListener(onStartStopRecordClickListener);
        btnOpenGPSTrack = (ImageButton)findViewById( R.id.btnOpenGPSTrack );
        btnOpenGPSTrack.setOnClickListener(onStartStopRecordClickListener);

        tvMileageRecInProgress = (TextView) findViewById(R.id.tvMileageRecInProgress);
        tvMileageRecInProgress.setTextColor(Color.RED);
        RadioGroup rg = (RadioGroup) findViewById(R.id.rgMileageInsertMode);
        rg.setOnCheckedChangeListener(rgOnCheckedChangeListener);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        try{
            init();
            super.onRestoreInstanceState(savedInstanceState);
            mCarId = savedInstanceState.getLong("mCarId");
            mDriverId = savedInstanceState.getLong("mDriverId");
            mExpTypeId = savedInstanceState.getLong("mExpTypeId");
            mUOMLengthId = savedInstanceState.getLong("mUOMLengthId");
            mGpsTrackId = savedInstanceState.getLong("mGpsTrackId");
            mInsertMode = savedInstanceState.getInt("mInsertMode");
            operationType = savedInstanceState.getString("operationType");

            if(savedInstanceState.containsKey("mNewIndex"))
                mNewIndex = new BigDecimal(savedInstanceState.getString("mNewIndex"));
            if(savedInstanceState.containsKey("mStartIndex"))
                mStartIndex = new BigDecimal(savedInstanceState.getString("mStartIndex"));
            if(savedInstanceState.containsKey("mStopIndex"))
                mStopIndex = new BigDecimal(savedInstanceState.getString("mStopIndex"));
            if(savedInstanceState.containsKey("mEntryMileageValue"))
                mEntryMileageValue = new BigDecimal(savedInstanceState.getString("mEntryMileageValue"));

            initControls();
            if( operationType.equals("E") ) {
            	tvMileageRecInProgress.setVisibility(View.GONE);
            	btnStartStopMileageRecord.setVisibility(View.GONE);
            	isRecordMileage = false;
            	btnOk.setEnabled(true);
        		spnCar.setEnabled(false);
        		if(mGpsTrackId > -1)
                    btnOpenGPSTrack.setVisibility(View.VISIBLE);
        		else
                    btnOpenGPSTrack.setVisibility(View.GONE);
            }
            else if(operationType.equals("TrackToMileage")){
            	tvMileageRecInProgress.setVisibility(View.GONE);
            	btnStartStopMileageRecord.setVisibility(View.GONE);
                btnOpenGPSTrack.setVisibility(View.GONE);
            	isRecordMileage = false;
            	btnOk.setEnabled(true);
            }
            else{
            	btnStartStopMileageRecord.setVisibility(View.VISIBLE);
                btnOpenGPSTrack.setVisibility(View.GONE);

            	if(mPreferences.getBoolean("MileageRec_IsRecording", false)){ //mileage rec in progress
            		isRecordMileage = true;
                	tvMileageRecInProgress.setVisibility(View.VISIBLE);
            		spnCar.setEnabled(false);
            		spnDriver.setEnabled(false);
            		spnExpType.setEnabled(false);
            		acTag.setEnabled(false);
            		rbInsertModeIndex.setEnabled(false);
            		rbInsertModeMileage.setEnabled(false);
            		etStartIndex.setEnabled(false);
            		etUserInput.setEnabled(false);
            		acUserComment.setEnabled(false);
                	btnOk.setEnabled(false);
                    btnStartStopMileageRecord.setImageDrawable(mResource.getDrawable(R.drawable.icon_mileage_stop_record_24x24));
            	}
            	else{
            		isRecordMileage = false;
                	tvMileageRecInProgress.setVisibility(View.GONE);
                	btnOk.setEnabled(true);
            		btnStartStopMileageRecord.setImageDrawable(mResource.getDrawable(R.drawable.icon_mileage_start_record_24x24));
            	}
            }
            initDateTime(mlDateTimeInSeconds * 1000);
        }
        catch(NumberFormatException e){}
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("mCarId", mCarId);
        outState.putLong("mDriverId", mDriverId);
        outState.putLong("mUOMLengthId", mUOMLengthId);
        outState.putLong("mGpsTrackId", mGpsTrackId);
        outState.putLong("mExpTypeId", spnExpType.getSelectedItemId());
        outState.putInt("mInsertMode", mInsertMode);
        outState.putString("operationType", operationType);

        if(mNewIndex != null)
            outState.putString("mNewIndex", mNewIndex.toString());
        if(mStartIndex != null)
            outState.putString("mStartIndex", mStartIndex.toString());
        if(mStopIndex != null)
            outState.putString("mStopIndex", mStopIndex.toString());
        if(mEntryMileageValue != null)
            outState.putString("mEntryMileageValue", mEntryMileageValue.toString());
 }


    @Override
    protected void onResume() {
        super.onResume();
        isBackgroundSettingsActive = true;
        fillGetCurrentIndex();
        calculateMileageOrNewIndex();
    }

    @Override
    protected void setLayout() {
        setContentView(R.layout.mileage_edit_activity);
    }

    public void calculateMileageOrNewIndex() throws NumberFormatException {
        try{
            BigDecimal pNewIndex = new BigDecimal("0");

            if(etStartIndex.getText() == null
                    || etStartIndex.getText().toString().length() == 0)
                mStartIndex = new BigDecimal("0");
            else
                mStartIndex = new BigDecimal(etStartIndex.getText().toString());

            if(etUserInput.getText().toString().length() == 0)
                mEntryMileageValue = new BigDecimal("0");
            else
                mEntryMileageValue = new BigDecimal(etUserInput.getText().toString());

            BigDecimal pEntryMileageValue = mEntryMileageValue;
            BigDecimal pStartIndex = mStartIndex;

            if(mInsertMode == StaticValues.MILEAGE_INSERTMODE_INDEX) { //new index
                pNewIndex = pEntryMileageValue;
                if(pNewIndex.compareTo(pStartIndex) < 0) {
                    tvCalculatedContent.setText("N/A;");
                }
                else {
                    BigDecimal mileage = pNewIndex.subtract(pStartIndex);
                    tvCalculatedContent.setText(Utils.numberToString(mileage, true, StaticValues.DECIMALS_LENGTH, StaticValues.ROUNDING_MODE_LENGTH));
                }
            }
            else { //mileage
                pNewIndex = mStartIndex.add(pEntryMileageValue);
                tvCalculatedContent.setText(Utils.numberToString(pNewIndex, true, StaticValues.DECIMALS_LENGTH, StaticValues.ROUNDING_MODE_LENGTH));
            }
            mNewIndex = pNewIndex;
        }
        catch(NumberFormatException e){
            Toast toast = Toast.makeText(getApplicationContext(),
                    mResource.getString(R.string.GEN_NumberFormatException), Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private BigDecimal fillGetCurrentIndex() throws SQLException {
        try{
            if(mStartIndex.equals(new BigDecimal("0"))){
                mStartIndex = mDbAdapter.getCarLastMileageIndex(mCarId);
            }
            etStartIndex.setText(Utils.numberToString(mStartIndex, false, StaticValues.DECIMALS_LENGTH, StaticValues.ROUNDING_MODE_LENGTH));
        }
        catch(NumberFormatException e){}
        return mStartIndex;
    }

    private RadioGroup.OnCheckedChangeListener rgOnCheckedChangeListener  =
            new RadioGroup.OnCheckedChangeListener() {
                    public void onCheckedChanged(RadioGroup arg0, int checkedId) {
                        if(checkedId == rbInsertModeIndex.getId()) {
                        	setInsertMode(StaticValues.MILEAGE_INSERTMODE_INDEX);//new index
                        }
                        else {
                        	setInsertMode(StaticValues.MILEAGE_INSERTMODE_MILEAGE);
                        }
                        SharedPreferences.Editor editor = mPreferences.edit();
                        editor.putInt("MileageInsertMode", mInsertMode);
                        editor.commit();
                        calculateMileageOrNewIndex();
                    }
                };

    public void setInsertMode(int insertMode){
    	mInsertMode = insertMode;
    	if(mInsertMode == StaticValues.MILEAGE_INSERTMODE_INDEX){
            tvCalculatedTextLabel.setText(
                    mResource.getString(R.string.MileageEditActivity_OptionMileageLabel) + ": ");
            etUserInput.setTag(mResource.getString(R.string.MileageEditActivity_OptionIndexLabel));
    	}
    	else{
            tvCalculatedTextLabel.setText(
                    mResource.getString(R.string.MileageEditActivity_OptionIndexLabel) + ": ");
            etUserInput.setTag(mResource.getString(R.string.MileageEditActivity_OptionMileageLabel));
    	}
    	setSpecificLayout();
    }
    
    private TextWatcher mileageTextWatcher =
        new TextWatcher() {

            public void beforeTextChanged(CharSequence cs, int i, int i1, int i2) {
                return;
            }

            public void onTextChanged(CharSequence cs, int i, int i1, int i2) {
                return;
            }

            public void afterTextChanged(Editable edtbl) {
            	if(etUserInput.getText().toString().length() > 0)
            		btnStartStopMileageRecord.setVisibility(View.GONE);
            	else
            		btnStartStopMileageRecord.setVisibility(View.VISIBLE);
                calculateMileageOrNewIndex();
            }
        };

    @Override
    protected boolean saveData() {
        //check mandatory fileds & index preconditions
        calculateMileageOrNewIndex();

        int operationResult = -1;
        ContentValues data = new ContentValues();
        data.put( MainDbAdapter.GEN_COL_NAME_NAME, "");
        data.put( MainDbAdapter.GEN_COL_ISACTIVE_NAME, "Y");
        data.put( MainDbAdapter.GEN_COL_USER_COMMENT_NAME,
                acUserComment.getText().toString());
        data.put( MainDbAdapter.MILEAGE_COL_DATE_NAME, mlDateTimeInSeconds);
        data.put( MainDbAdapter.MILEAGE_COL_CAR_ID_NAME, mCarId);
        data.put( MainDbAdapter.MILEAGE_COL_DRIVER_ID_NAME, mDriverId);
        data.put( MainDbAdapter.MILEAGE_COL_INDEXSTART_NAME, mStartIndex.toString());
            data.put( MainDbAdapter.MILEAGE_COL_INDEXSTOP_NAME, mNewIndex.toString());
        data.put( MainDbAdapter.MILEAGE_COL_UOMLENGTH_ID_NAME, mUOMLengthId);
        data.put( MainDbAdapter.MILEAGE_COL_EXPENSETYPE_ID_NAME, spnExpType.getSelectedItemId());
        data.put( MainDbAdapter.MILEAGE_COL_GPSTRACKLOG_NAME, "");
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
                data.put(MainDbAdapter.MILEAGE_COL_TAG_ID_NAME, mTagId);
            }
            else{
                ContentValues tmpData = new ContentValues();
                tmpData.put(MainDbAdapter.GEN_COL_NAME_NAME, acTag.getText().toString());
                mTagId = mDbAdapter.createRecord(MainDbAdapter.TAG_TABLE_NAME, tmpData);
                if(mTagId >= 0)
                    data.put(MainDbAdapter.MILEAGE_COL_TAG_ID_NAME, mTagId);
            }
        }
        else{
            data.put(MainDbAdapter.MILEAGE_COL_TAG_ID_NAME, (String)null);
        }
        
        

        if(operationType.equals("N") || operationType.equals("TrackToMileage")){
            operationResult = mDbAdapter.checkIndex(-1, mCarId, mStartIndex, mNewIndex);
            if(operationResult == -1){
                Long result = mDbAdapter.createRecord(MainDbAdapter.MILEAGE_TABLE_NAME, data);
                if( result.intValue() < 0){
                    if(result.intValue() == -1) //DB Error
                        madbErrorAlert.setMessage(mDbAdapter.lastErrorMessage);
                    else //precondition error
                        madbErrorAlert.setMessage(mResource.getString(-1 * result.intValue()));
                    madError = madbErrorAlert.create();
                    madError.show();
                    return false;
                }
                //set the mileage id on the gps track
                ContentValues cv = new ContentValues();
                cv.put(MainDbAdapter.GPSTRACK_COL_MILEAGE_ID_NAME, result);
                mDbAdapter.updateRecord(MainDbAdapter.GPSTRACK_TABLE_NAME, mGpsTrackId, cv);
            }
        }
        else{
            operationResult = mDbAdapter.checkIndex(mRowId, mCarId, mStartIndex, mNewIndex);
            if(operationResult == -1){
                int updResult = mDbAdapter.updateRecord(MainDbAdapter.MILEAGE_TABLE_NAME, mRowId, data);
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
        }
        if( operationResult != -1) //error
        {
            madbErrorAlert.setMessage(mResource.getString(operationResult));
            madError = madbErrorAlert.create();
            madError.show();
            return false;
        }
        else{
            Toast toast = Toast.makeText( getApplicationContext(),
                    (operationType.equals("N") || operationType.equals("TrackToMileage") ?
                        mResource.getString(R.string.MileageEditActivity_InsertOkMessage)
                        : mResource.getString(R.string.MileageEditActivity_UpdateOkMessage)) ,
                    Toast.LENGTH_SHORT );
            toast.show();
            userCommentAdapter = null;
            userCommentAdapter = new ArrayAdapter<String>(MileageEditActivity.this,
                    android.R.layout.simple_dropdown_item_1line,
                    mDbAdapter.getAutoCompleteText(MainDbAdapter.MILEAGE_TABLE_NAME, null, mCarId, 30));
            acUserComment.setAdapter(userCommentAdapter);
        }

        //mileage inserted. reinit the activity for new mileage

//        if(operationType.equals("N")){
//            mStartIndex = BigDecimal.valueOf(0);
//            fillGetCurrentIndex();
//
//            etUserInput.setText("");
//            mNewIndex = BigDecimal.valueOf(0);
//            mEntryMileageValue = BigDecimal.valueOf(0);
//            acUserComment.setText("");
//            calculateMileageOrNewIndex();
//        }

        if(mPreferences.getBoolean("RememberLastTag", false) && mTagId > 0)
    		mPrefEditor.putLong("LastTagId", mTagId);

    	mPrefEditor.putLong("LastDriver_ID", mDriverId);
    	mPrefEditor.putLong("MileageInsertExpenseType_ID", spnExpType.getSelectedItemId());
		mPrefEditor.commit();
        
		//check if mileage todo exists
		Intent intent = new Intent(this, ToDoNotificationService.class);
		intent.putExtra("setJustNextRun", false);
		intent.putExtra("CarID", mCarId);
		this.startService(intent);
		
		return true;
    }

    protected View.OnClickListener onStartStopRecordClickListener =
        new View.OnClickListener()
            {
                public void onClick( View v )
                {
                	if(v.getId() == R.id.btnStartStopMileageRecord){
	                    if(!isRecordMileage){ //start recording
	                    	isRecordMileage = true;
	                    	mPrefEditor.putBoolean("MileageRec_IsRecording", isRecordMileage);
	                    	mPrefEditor.putLong("MileageRec_CarId", mCarId);
	                    	mPrefEditor.putLong("MileageRec_DriverId", mDriverId);
	                    	mPrefEditor.putLong("MileageRec_ExpenseTypeId", mExpTypeId);
	                    	mPrefEditor.putInt("MileageRec_InsertMode", mInsertMode);
	                    	mPrefEditor.putString("MileageRec_Tag", acTag.getText().toString());
	                    	mPrefEditor.putString("MileageRec_StartIndex", etStartIndex.getText().toString());
	                    	mPrefEditor.putString("MileageRec_Comment", acUserComment.getText().toString());
	                    	mPrefEditor.commit();
	                    	if(mDet != null)
	                    		mDet.setControlsEnabled(false);
	                    	finish();
	                    }
	                    else{//stop recording
	                    	isRecordMileage = false;
	                    	tvMileageRecInProgress.setVisibility(View.GONE);
	                    	mPrefEditor.putBoolean("MileageRec_IsRecording", isRecordMileage);
	                    	mPrefEditor.commit();
	                        btnStartStopMileageRecord.setImageDrawable(mResource.getDrawable(R.drawable.icon_mileage_start_record_24x24));
	                		spnCar.setEnabled(true);
	                		spnDriver.setEnabled(true);
	                		spnExpType.setEnabled(true);
	                		acTag.setEnabled(true);
	                		rbInsertModeIndex.setEnabled(true);
	                		rbInsertModeMileage.setEnabled(true);
	                		etStartIndex.setEnabled(true);
	                		etUserInput.setEnabled(true);
	                		acUserComment.setEnabled(true);
	                    	btnOk.setEnabled(true);
	                    	if(mDet != null)
	                    		mDet.setControlsEnabled(true);
	                    }
                	}
                	else if(v.getId() == R.id.btnOpenGPSTrack && mGpsTrackId > -1){
    	                Intent gpsTrackEditIntent = new Intent(MileageEditActivity.this, GPSTrackEditActivity.class);
    	                gpsTrackEditIntent.putExtra(MainDbAdapter.GEN_COL_ROWID_NAME, mGpsTrackId);
    	                startActivity(gpsTrackEditIntent);
                	}
                }
            };

    private AdapterView.OnItemSelectedListener spinnerCarOnItemSelectedListener =
            new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    if(isBackgroundSettingsActive)
                        return;
                    setCarId(arg3);
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


	/* (non-Javadoc)
	 * @see org.andicar.activity.BaseActivity#setSpecificLayout()
	 */
	@Override
	public void setSpecificLayout() {
    	if(etUserInput.getText().toString().length() > 0)
    		btnStartStopMileageRecord.setVisibility(View.GONE);
    	else
    		btnStartStopMileageRecord.setVisibility(View.VISIBLE);
        calculateMileageOrNewIndex();
	}

	/**
	 * @param carId the mCarId to set
	 */
	public void setCarId(long carId) {
		this.mCarId = carId;

		userCommentAdapter = null;
        userCommentAdapter = new ArrayAdapter<String>(MileageEditActivity.this,
                android.R.layout.simple_dropdown_item_1line,
                mDbAdapter.getAutoCompleteText(MainDbAdapter.MILEAGE_TABLE_NAME, null,
                mCarId, 30));
        acUserComment.setAdapter(userCommentAdapter);
        mStartIndex = BigDecimal.ZERO;
        fillGetCurrentIndex();
        calculateMileageOrNewIndex();
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
		
		isRecordMileage = false;
    	tvMileageRecInProgress.setVisibility(View.GONE);
    	btnOk.setEnabled(true);
		spnCar.setEnabled(true);
		btnStartStopMileageRecord.setImageDrawable(mResource.getDrawable(R.drawable.icon_mileage_start_record_24x24));
        
		mCarId = mPreferences.getLong("CurrentCar_ID", -1);
        setSpinnerSelectedID(spnCar, mCarId);

        mDriverId = mPreferences.getLong("LastDriver_ID", -1);
        setSpinnerSelectedID(spnDriver, mDriverId);

        mExpTypeId = mPreferences.getLong("MileageInsertExpenseType_ID", -1);
        setSpinnerSelectedID(spnExpType, mExpTypeId);

        mInsertMode = mPreferences.getInt("MileageInsertMode", 0);
        rbInsertModeIndex.setChecked(true);
        rbInsertModeMileage.setChecked(false);
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
        else
        	acTag.setText("");

        etUserInput.setText("");
        acUserComment.setText("");
//        initControls();
        initDateTime(System.currentTimeMillis());
        setSpecificLayout();
	}

}
