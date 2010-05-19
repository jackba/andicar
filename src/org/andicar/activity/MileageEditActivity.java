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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.andicar.persistence.MainDbAdapter;
import org.andicar.utils.AndiCarStatistics;
import org.andicar.utils.StaticValues;

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
    private BigDecimal mNewIndex = new BigDecimal("0");
    private BigDecimal mStartIndex = new BigDecimal("0");
    private BigDecimal mEntryMileageValue = BigDecimal.valueOf(0);
    private RadioButton rbInsertModeIndex;
    private RadioButton rbInsertModeMileage;
    private TextView tvUserInputLabel;
    private TextView tvCalculatedTextLabel;
    private EditText etStartIndex;
    private EditText etUserInput;
    private TextView tvCalculatedContent;
    private AutoCompleteTextView acUserComment;
    private String operationType;
    private Spinner spnExpType;

    ArrayAdapter<String> userCommentAdapter;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle, R.layout.mileage_edit_activity, btnOkClickListener);

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

        RadioGroup rg = (RadioGroup) findViewById(R.id.rgMileageInsertMode);
        rg.setOnCheckedChangeListener(rgOnCheckedChangeListener);
        String currentDriverName = null;
        String currentCarName = null;
        String driverCarLbl = "";
        operationType = mBundleExtras.getString("Operation");

        if( operationType.equals("E") ) {
            mRowId = mBundleExtras.getLong( MainDbAdapter.GEN_COL_ROWID_NAME );

            mCarId = mDbAdapter.fetchRecord(MainDbAdapter.MILEAGE_TABLE_NAME, MainDbAdapter.mileageTableColNames, mRowId)
                                .getLong(MainDbAdapter.MILEAGE_COL_CAR_ID_POS);
            mDriverId = mDbAdapter.fetchRecord(MainDbAdapter.MILEAGE_TABLE_NAME, MainDbAdapter.mileageTableColNames, mRowId)
                                .getLong(MainDbAdapter.MILEAGE_COL_DRIVER_ID_POS);

            currentDriverName = mDbAdapter.fetchRecord(MainDbAdapter.DRIVER_TABLE_NAME, MainDbAdapter.driverTableColNames, mDriverId)
                                .getString(MainDbAdapter.GEN_COL_NAME_POS);
            currentCarName = mDbAdapter.fetchRecord(MainDbAdapter.CAR_TABLE_NAME, MainDbAdapter.carTableColNames, mCarId)
                                .getString(MainDbAdapter.GEN_COL_NAME_POS);

            Cursor recordCursor = mDbAdapter.fetchRecord(MainDbAdapter.MILEAGE_TABLE_NAME, MainDbAdapter.mileageTableColNames, mRowId);
            mStartIndex = new BigDecimal(recordCursor.getString(MainDbAdapter.MILEAGE_COL_INDEXSTART_POS));
            etStartIndex.setText(mStartIndex.toString());
            BigDecimal stopIndex = new BigDecimal(recordCursor.getString(MainDbAdapter.MILEAGE_COL_INDEXSTOP_POS));
            etUserInput.setText(stopIndex.toString());
            rbInsertModeIndex.setChecked(true);
            mInsertMode = StaticValues.MILEAGE_INSERTMODE_INDEX;
            initDateTime(recordCursor.getLong(MainDbAdapter.MILEAGE_COL_DATE_POS) * 1000);
            acUserComment.setText(recordCursor.getString(MainDbAdapter.GEN_COL_USER_COMMENT_POS));
            initSpinner(spnExpType, MainDbAdapter.EXPENSETYPE_TABLE_NAME,
                    MainDbAdapter.genColName, new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, MainDbAdapter.isActiveCondition, MainDbAdapter.GEN_COL_NAME_NAME,
                    recordCursor.getLong(MainDbAdapter.MILEAGE_COL_EXPENSETYPE_ID_POS), true);
            recordCursor.close();
        }
        else if(operationType.equals("TrackToMileage")){
            mGpsTrackId = mBundleExtras.getLong("Track_ID");
            Cursor recordCursor = mDbAdapter.fetchRecord(MainDbAdapter.GPSTRACK_TABLE_NAME, MainDbAdapter.gpsTrackTableColNames, mGpsTrackId);
            mInsertMode = StaticValues.MILEAGE_INSERTMODE_INDEX;
            mCarId = recordCursor.getLong(MainDbAdapter.GPSTRACK_COL_CAR_ID_POS);
            mDriverId = recordCursor.getLong(MainDbAdapter.GPSTRACK_COL_DRIVER_ID_POS);
            acUserComment.setText(recordCursor.getString(MainDbAdapter.GEN_COL_USER_COMMENT_POS));
            currentDriverName = mDbAdapter.fetchRecord(MainDbAdapter.DRIVER_TABLE_NAME, MainDbAdapter.driverTableColNames, mDriverId)
                                .getString(MainDbAdapter.GEN_COL_NAME_POS);
            currentCarName = mDbAdapter.fetchRecord(MainDbAdapter.CAR_TABLE_NAME, MainDbAdapter.carTableColNames, mCarId)
                                .getString(MainDbAdapter.GEN_COL_NAME_POS);
            initSpinner(spnExpType, MainDbAdapter.EXPENSETYPE_TABLE_NAME,
                    MainDbAdapter.genColName, new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, MainDbAdapter.isActiveCondition, MainDbAdapter.GEN_COL_NAME_NAME,
                    mPreferences.getLong("MileageInsertExpenseType_ID", -1), true);
            initDateTime(System.currentTimeMillis());
            mStartIndex = BigDecimal.ZERO;
            fillGetCurrentIndex();
            BigDecimal stopIndex = mStartIndex.add(new BigDecimal(recordCursor.getString(MainDbAdapter.GPSTRACK_COL_DISTANCE_POS))).setScale(0, BigDecimal.ROUND_HALF_DOWN);

            etUserInput.setText(stopIndex.toString());
            recordCursor.close();
        }
        else{
            mCarId = mBundleExtras.getLong("CurrentCar_ID");
            mDriverId = mBundleExtras.getLong("CurrentDriver_ID");
            currentDriverName = mBundleExtras.getString("CurrentDriver_Name");
            currentCarName = mBundleExtras.getString("CurrentCar_Name");
            mInsertMode = mPreferences.getInt("MileageInsertMode", 0);

            initDateTime(System.currentTimeMillis());

            initSpinner(spnExpType, MainDbAdapter.EXPENSETYPE_TABLE_NAME,
                    MainDbAdapter.genColName, new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, MainDbAdapter.isActiveCondition, MainDbAdapter.GEN_COL_NAME_NAME,
                    mPreferences.getLong("MileageInsertExpenseType_ID", -1), true);

            etUserInput.requestFocus();

        }
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
        mUOMLengthId = mDbAdapter.fetchRecord(MainDbAdapter.CAR_TABLE_NAME, MainDbAdapter.carTableColNames, mCarId)
                                .getLong(MainDbAdapter.CAR_COL_UOMLENGTH_ID_POS);
        if(currentDriverName != null) {
            driverCarLbl = mResource.getString(R.string.GEN_DriverLabel) + currentDriverName;
        }
        if(currentCarName != null) {
            driverCarLbl = driverCarLbl + "; "
                    + mResource.getString(R.string.GEN_CarLabel) + " " + currentCarName;
        }
        ((TextView) findViewById(R.id.tvCarDriverLabel)).setText(driverCarLbl);

        etUserInput.requestFocus();
        if(isSendStatistics)
            AndiCarStatistics.sendFlurryEvent("MileageEdit", null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fillGetCurrentIndex();
        calculateMileageOrNewIndex();
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
            if(mStartIndexStr == null)
                mStartIndexStr = "0";
            mStartIndex = new BigDecimal(mStartIndexStr);
            c.close();
        }
        etStartIndex.setText(mStartIndex.toString());
        return mStartIndex;
    }

    private View.OnClickListener btnOkClickListener =  new View.OnClickListener() {
            public void onClick(View arg0) {
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
    };

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
}
