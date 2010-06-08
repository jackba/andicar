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
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import java.math.BigDecimal;
import org.andicar.persistence.MainDbAdapter;
import org.andicar.utils.AndiCarStatistics;
import org.andicar.utils.StaticValues;
import android.view.View;


/**
 *
 * @author miki
 */
public class MileageEditActivity extends EditActivityBase {
    //mileage insert mode: 0 = new index; 1 = mileage
    private int mInsertMode = 0;
    private long mCarId = -1;
    private long mDriverId = -1;
    private long mUOMLengthId = -1;
    private long mGpsTrackId = -1;
    private String operationType = null;
    private long mExpTypeId = 0;
    private BigDecimal mNewIndex = new BigDecimal("0");
    private BigDecimal mStartIndex = new BigDecimal("0");
    private BigDecimal mStopIndex = null;
    private BigDecimal mEntryMileageValue = BigDecimal.valueOf(0);
    private boolean isActivityOnLoading = true;

    private RadioButton rbInsertModeIndex;
    private RadioButton rbInsertModeMileage;
    private TextView tvUserInputLabel;
    private TextView tvCalculatedTextLabel;
    private EditText etStartIndex;
    private EditText etUserInput;
    private TextView tvCalculatedContent;
    private AutoCompleteTextView acUserComment;
    private Spinner spnExpType;
    private Spinner spnCar;
    private Spinner spnDriver;
    ArrayAdapter<String> userCommentAdapter;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        if(icicle !=null)
            return; //restored from previous state


        operationType = mBundleExtras.getString("Operation");

        init();
        
        if( operationType.equals("E") ) {
            mRowId = mBundleExtras.getLong( MainDbAdapter.GEN_COL_ROWID_NAME );

            mCarId = mDbAdapter.fetchRecord(MainDbAdapter.MILEAGE_TABLE_NAME, MainDbAdapter.mileageTableColNames, mRowId)
                                .getLong(MainDbAdapter.MILEAGE_COL_CAR_ID_POS);
            mDriverId = mDbAdapter.fetchRecord(MainDbAdapter.MILEAGE_TABLE_NAME, MainDbAdapter.mileageTableColNames, mRowId)
                                .getLong(MainDbAdapter.MILEAGE_COL_DRIVER_ID_POS);
            Cursor c = mDbAdapter.fetchRecord(MainDbAdapter.MILEAGE_TABLE_NAME, MainDbAdapter.mileageTableColNames, mRowId);
            try{
                mStartIndex = new BigDecimal(c.getString(MainDbAdapter.MILEAGE_COL_INDEXSTART_POS));
                etStartIndex.setText(mStartIndex.toString());
                mStopIndex = new BigDecimal(c.getString(MainDbAdapter.MILEAGE_COL_INDEXSTOP_POS));
                etUserInput.setText(mStopIndex.toString());
            }
            catch(NumberFormatException e){}
            rbInsertModeIndex.setChecked(true);
            mInsertMode = StaticValues.MILEAGE_INSERTMODE_INDEX;
            acUserComment.setText(c.getString(MainDbAdapter.GEN_COL_USER_COMMENT_POS));
            mExpTypeId = c.getLong(MainDbAdapter.MILEAGE_COL_EXPENSETYPE_ID_POS);
            initDateTime(c.getLong(MainDbAdapter.MILEAGE_COL_DATE_POS) * 1000);
            c.close();
        }
        else if(operationType.equals("TrackToMileage")){
            mGpsTrackId = mBundleExtras.getLong("Track_ID");
            Cursor c = mDbAdapter.fetchRecord(MainDbAdapter.GPSTRACK_TABLE_NAME, MainDbAdapter.gpsTrackTableColNames, mGpsTrackId);
            mInsertMode = StaticValues.MILEAGE_INSERTMODE_INDEX;
            mCarId = c.getLong(MainDbAdapter.GPSTRACK_COL_CAR_ID_POS);
            mDriverId = c.getLong(MainDbAdapter.GPSTRACK_COL_DRIVER_ID_POS);
            acUserComment.setText(c.getString(MainDbAdapter.GEN_COL_USER_COMMENT_POS));
            mExpTypeId = mPreferences.getLong("MileageInsertExpenseType_ID", -1);
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
            mCarId = mBundleExtras.getLong("CurrentCar_ID");
            mDriverId = mBundleExtras.getLong("CurrentDriver_ID");
            mInsertMode = mPreferences.getInt("MileageInsertMode", 0);
            mExpTypeId = mPreferences.getLong("MileageInsertExpenseType_ID", -1);
            initDateTime(System.currentTimeMillis());
            etUserInput.requestFocus();

        }

        initControls();

        mUOMLengthId = mDbAdapter.fetchRecord(MainDbAdapter.CAR_TABLE_NAME, MainDbAdapter.carTableColNames, mCarId)
                                        .getLong(MainDbAdapter.CAR_COL_UOMLENGTH_ID_POS);

        etUserInput.requestFocus();
        if(isSendStatistics)
            AndiCarStatistics.sendFlurryEvent("MileageEdit", null);
    }

    private void initControls(){
        initSpinner(spnCar, MainDbAdapter.CAR_TABLE_NAME, MainDbAdapter.genColName,
                new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, MainDbAdapter.isActiveCondition,
                MainDbAdapter.GEN_COL_NAME_NAME,
                mCarId);
        initSpinner(spnDriver, MainDbAdapter.DRIVER_TABLE_NAME, MainDbAdapter.genColName,
                new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, MainDbAdapter.isActiveCondition,
                MainDbAdapter.GEN_COL_NAME_NAME, mDriverId);
        initSpinner(spnExpType, MainDbAdapter.EXPENSETYPE_TABLE_NAME,
                MainDbAdapter.genColName, new String[]{MainDbAdapter.GEN_COL_NAME_NAME},
                MainDbAdapter.isActiveCondition, MainDbAdapter.GEN_COL_NAME_NAME,
                mExpTypeId);
        userCommentAdapter = new ArrayAdapter<String>(MileageEditActivity.this,
                android.R.layout.simple_dropdown_item_1line,
                mDbAdapter.getAutoCompleteUserComments(MainDbAdapter.MILEAGE_TABLE_NAME, mCarId, 30));
        acUserComment.setAdapter(userCommentAdapter);

        if(mInsertMode == StaticValues.MILEAGE_INSERTMODE_INDEX) {
            rbInsertModeIndex.setChecked(true);
            tvUserInputLabel.setText(
                    mResource.getString(R.string.MileageEditActivity_OptionIndexLabel));
            tvCalculatedTextLabel.setText(
                    mResource.getString(R.string.MileageEditActivity_OptionMileageLabel));
            etUserInput.setTag(mResource.getString(R.string.MileageEditActivity_OptionIndexLabel));
        }
        else {
            rbInsertModeMileage.setChecked(true);
            tvUserInputLabel.setText(
                    mResource.getString(R.string.MileageEditActivity_OptionMileageLabel));
            tvCalculatedTextLabel.setText(
                    mResource.getString(R.string.MileageEditActivity_OptionIndexLabel));
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
        tvUserInputLabel = ((TextView) findViewById(R.id.tvUserInputLabel));
        tvCalculatedTextLabel = ((TextView) findViewById(R.id.tvCalculatedTextLabel));
        spnExpType = (Spinner)findViewById(R.id.spnExpType);
        acUserComment = (AutoCompleteTextView)findViewById(R.id.acUserComment);
        spnCar = (Spinner) findViewById(R.id.spnCar);
        spnDriver = (Spinner) findViewById(R.id.spnDriver);
        spnCar.setOnItemSelectedListener(spinnerCarDriverOnItemSelectedListener);
        spnCar.setOnTouchListener(spinnerOnTouchListener);
        spnDriver.setOnItemSelectedListener(spinnerCarDriverOnItemSelectedListener);
        spnDriver.setOnTouchListener(spinnerOnTouchListener);

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

            if(savedInstanceState.containsKey("mNewIndex"))
                mNewIndex = new BigDecimal(savedInstanceState.getString("mNewIndex"));
            if(savedInstanceState.containsKey("mStartIndex"))
                mStartIndex = new BigDecimal(savedInstanceState.getString("mStartIndex"));
            if(savedInstanceState.containsKey("mStopIndex"))
                mStopIndex = new BigDecimal(savedInstanceState.getString("mStopIndex"));
            if(savedInstanceState.containsKey("mEntryMileageValue"))
                mEntryMileageValue = new BigDecimal(savedInstanceState.getString("mEntryMileageValue"));

            initControls();
//            calculateMileageOrNewIndex();
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
        isActivityOnLoading = true;
        fillGetCurrentIndex();
        calculateMileageOrNewIndex();
    }

    @Override
    void setLayout() {
        setContentView(R.layout.mileage_edit_activity);
    }

    private void calculateMileageOrNewIndex() throws NumberFormatException {
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
                    tvCalculatedContent.setText(mileage.toString() + ";");
                }
            }
            else { //mileage
                pNewIndex = mStartIndex.add(pEntryMileageValue);
                tvCalculatedContent.setText(pNewIndex.toString() + ";");
            }
            mNewIndex = pNewIndex;
        }
        catch(NumberFormatException e){
            Toast toast = Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG);
            toast.show();
        }
    }

    private BigDecimal fillGetCurrentIndex() throws SQLException {
        try{
            if(mStartIndex.equals(new BigDecimal("0"))){
                String mStartIndexStr = null;
                String sql = "SELECT MAX( " + MainDbAdapter.MILEAGE_COL_INDEXSTOP_NAME + "), 1 As Pos " +
                                "FROM " + MainDbAdapter.MILEAGE_TABLE_NAME + " " +
                                "WHERE " + MainDbAdapter.GEN_COL_ISACTIVE_NAME + " = 'Y' " +
                                    "AND " + MainDbAdapter.MILEAGE_COL_CAR_ID_NAME + " = " + mCarId + " " +
                              "UNION " +
                              "SELECT " + MainDbAdapter.CAR_COL_INDEXCURRENT_NAME + ", 2 As Pos " +
                              "FROM " + MainDbAdapter.CAR_TABLE_NAME + " " +
                              "WHERE " + MainDbAdapter.GEN_COL_ROWID_NAME + " = " + mCarId + " " +
                              "ORDER BY Pos ASC";
                Cursor c = mDbAdapter.execSelectSql(sql);
                if(c.moveToFirst()){
                    mStartIndexStr = c.getString(0);
                }
                if((mStartIndexStr == null || mStartIndexStr.length() == 0)
                        && c.moveToNext())
                    mStartIndexStr = c.getString(0);
                if(mStartIndexStr == null || mStartIndexStr.length() == 0)
                    mStartIndexStr = "0";
                mStartIndex = new BigDecimal(mStartIndexStr);
                c.close();
            }
            etStartIndex.setText(mStartIndex.toString());
        }
        catch(NumberFormatException e){}
        return mStartIndex;
    }

    private RadioGroup.OnCheckedChangeListener rgOnCheckedChangeListener  =
            new RadioGroup.OnCheckedChangeListener() {
                    public void onCheckedChanged(RadioGroup arg0, int checkedId) {
                        if(checkedId == rbInsertModeIndex.getId()) {
                            mInsertMode = StaticValues.MILEAGE_INSERTMODE_INDEX; //new index
                            tvUserInputLabel.setText(
                                    mResource.getString(R.string.MileageEditActivity_OptionIndexLabel));
                            tvCalculatedTextLabel.setText(
                                    mResource.getString(R.string.MileageEditActivity_OptionMileageLabel));
                            etUserInput.setTag(mResource.getString(R.string.MileageEditActivity_OptionIndexLabel));
                        }
                        else {
                            mInsertMode = StaticValues.MILEAGE_INSERTMODE_MILEAGE;
                            tvUserInputLabel.setText(
                                    mResource.getString(R.string.MileageEditActivity_OptionMileageLabel));
                            tvCalculatedTextLabel.setText(
                                    mResource.getString(R.string.MileageEditActivity_OptionIndexLabel));
                            etUserInput.setTag(mResource.getString(R.string.MileageEditActivity_OptionMileageLabel));
                        }
                        SharedPreferences.Editor editor = mPreferences.edit();
                        editor.putInt("MileageInsertMode", mInsertMode);
                        editor.commit();
                        calculateMileageOrNewIndex();
                    }
                };

    private TextWatcher mileageTextWatcher =
        new TextWatcher() {

            public void beforeTextChanged(CharSequence cs, int i, int i1, int i2) {
                return;
            }

            public void onTextChanged(CharSequence cs, int i, int i1, int i2) {
                return;
            }

            public void afterTextChanged(Editable edtbl) {
                calculateMileageOrNewIndex();
            }
        };

    @Override
    void saveData() {
        //check mandatory fileds & index preconditions
        calculateMileageOrNewIndex();
        String retVal = checkMandatory((ViewGroup) findViewById(R.id.vgRoot));
        if( retVal != null ) {
            Toast toast = Toast.makeText( getApplicationContext(),
                    mResource.getString( R.string.GEN_FillMandatory ) + ": " + retVal, Toast.LENGTH_SHORT );
            toast.show();
            return;
        }
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
                    return;
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
                    return;
                }
            }
        }
        if( operationResult != -1) //error
        {
            madbErrorAlert.setMessage(mResource.getString(operationResult));
            madError = madbErrorAlert.create();
            madError.show();
            return;
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
                    mDbAdapter.getAutoCompleteUserComments(MainDbAdapter.MILEAGE_TABLE_NAME, mCarId, 30));
            acUserComment.setAdapter(userCommentAdapter);
        }

        //mileage inserted. reinit the activity for new mileage

        if(operationType.equals("N")){
            mStartIndex = BigDecimal.valueOf(0);
            fillGetCurrentIndex();

            etUserInput.setText("");
            mNewIndex = BigDecimal.valueOf(0);
            mEntryMileageValue = BigDecimal.valueOf(0);
            acUserComment.setText("");
            calculateMileageOrNewIndex();
        }
        finish();
    }

    private AdapterView.OnItemSelectedListener spinnerCarDriverOnItemSelectedListener =
            new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    if(isActivityOnLoading)
                        return;
                    mCarId = spnCar.getSelectedItemId();
                    mDriverId = spnDriver.getSelectedItemId();

                    userCommentAdapter = null;
                    userCommentAdapter = new ArrayAdapter<String>(MileageEditActivity.this,
                            android.R.layout.simple_dropdown_item_1line,
                            mDbAdapter.getAutoCompleteUserComments(MainDbAdapter.MILEAGE_TABLE_NAME,
                            mCarId, 30));
                    acUserComment.setAdapter(userCommentAdapter);
                    mStartIndex = BigDecimal.ZERO;
                    fillGetCurrentIndex();
                    calculateMileageOrNewIndex();

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

}
