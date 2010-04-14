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
import org.andicar.persistence.MainDbAdapter;
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
    private BigDecimal mNewIndex = new BigDecimal("0");
    private BigDecimal mStartIndex = new BigDecimal("0");
    private BigDecimal mEntryMileageValue = BigDecimal.valueOf(0);
    private RadioButton mileageEditInsertModeIndexRb;
    private RadioButton mileageEditInsertModeMileageRb;
    private TextView mileageEditInputLabel;
    private TextView mileageEditCalculatedTextLabel;
    private EditText mileageEditStartIndexEntry;
    private EditText mileageEditInputEntry;
    private TextView mileageEditCalculatedTextContent;
    private AutoCompleteTextView mileageEditUserCommentEntry;
    private String operationType;
    Spinner mExpTypeSpinner;

    ArrayAdapter<String> userCommentAdapter;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle, R.layout.mileage_edit_activity, btnOkClickListener);

        mileageEditCalculatedTextContent = (TextView) findViewById(R.id.mileageEditCalculatedTextContent);
        mileageEditInputEntry = (EditText) findViewById(R.id.mileageEditInputEntry);
        mileageEditInputEntry.addTextChangedListener(mileageTextWatcher);
        mileageEditStartIndexEntry = (EditText) findViewById(R.id.mileageEditStartIndexEntry);
        mileageEditStartIndexEntry.addTextChangedListener(mileageTextWatcher);
        mileageEditInsertModeIndexRb = (RadioButton) findViewById(R.id.mileageEditInsertModeIndexRb);
        mileageEditInsertModeMileageRb = (RadioButton) findViewById(R.id.mileageEditInsertModeMileageRb);
        mileageEditInputLabel = ((TextView) findViewById(R.id.mileageEditInputLabel));
        mileageEditCalculatedTextLabel = ((TextView) findViewById(R.id.mileageEditCalculatedTextLabel));
        mExpTypeSpinner = (Spinner)findViewById(R.id.mileageEditExpenseTypeSpinner);
        mileageEditUserCommentEntry = (AutoCompleteTextView)findViewById(R.id.genUserCommentEntry);

        RadioGroup rg = (RadioGroup) findViewById(R.id.rgMileageInsertMode);
        rg.setOnCheckedChangeListener(rgOnCheckedChangeListener);
        String currentDriverName = null;
        String currentCarName = null;
        String driverCarLbl = "";
        operationType = extras.getString("Operation");

        if( operationType.equals("E") ) {
            mRowId = extras.getLong( MainDbAdapter.GEN_COL_ROWID_NAME );

            mCarId = mMainDbAdapter.fetchRecord(MainDbAdapter.MILEAGE_TABLE_NAME, MainDbAdapter.mileageTableColNames, mRowId)
                                .getLong(MainDbAdapter.MILEAGE_COL_CAR_ID_POS);
            mDriverId = mMainDbAdapter.fetchRecord(MainDbAdapter.MILEAGE_TABLE_NAME, MainDbAdapter.mileageTableColNames, mRowId)
                                .getLong(MainDbAdapter.MILEAGE_COL_DRIVER_ID_POS);

            currentDriverName = mMainDbAdapter.fetchRecord(MainDbAdapter.DRIVER_TABLE_NAME, MainDbAdapter.driverTableColNames, mDriverId)
                                .getString(MainDbAdapter.GEN_COL_NAME_POS);
            currentCarName = mMainDbAdapter.fetchRecord(MainDbAdapter.CAR_TABLE_NAME, MainDbAdapter.carTableColNames, mCarId)
                                .getString(MainDbAdapter.GEN_COL_NAME_POS);

            Cursor recordCursor = mMainDbAdapter.fetchRecord(MainDbAdapter.MILEAGE_TABLE_NAME, MainDbAdapter.mileageTableColNames, mRowId);
            mStartIndex = new BigDecimal(recordCursor.getString(MainDbAdapter.MILEAGE_COL_INDEXSTART_POS));
            mileageEditStartIndexEntry.setText(mStartIndex.toString());
            BigDecimal stopIndex = new BigDecimal(recordCursor.getString(MainDbAdapter.MILEAGE_COL_INDEXSTOP_POS));
            mileageEditInputEntry.setText(stopIndex.toString());
            mileageEditInsertModeIndexRb.setChecked(true);
            mInsertMode = StaticValues.mileageInsertModeNewIndex;
            initDateTime(recordCursor.getLong(MainDbAdapter.MILEAGE_COL_DATE_POS) * 1000);
            mileageEditUserCommentEntry.setText(recordCursor.getString(MainDbAdapter.GEN_COL_USER_COMMENT_POS));
            initSpinner(mExpTypeSpinner, MainDbAdapter.EXPENSETYPE_TABLE_NAME,
                    MainDbAdapter.genColName, new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, MainDbAdapter.isActiveCondition, MainDbAdapter.GEN_COL_NAME_NAME,
                    recordCursor.getLong(MainDbAdapter.MILEAGE_COL_EXPENSETYPE_ID_POS), true);
        }
        else{
            mCarId = extras.getLong("CurrentCar_ID");
            mDriverId = extras.getLong("CurrentDriver_ID");
            currentDriverName = extras.getString("CurrentDriver_Name");
            currentCarName = extras.getString("CurrentCar_Name");
            mInsertMode = mPreferences.getInt("MileageInsertMode", 0);

            initDateTime(System.currentTimeMillis());

            initSpinner(findViewById(R.id.mileageEditExpenseTypeSpinner), MainDbAdapter.EXPENSETYPE_TABLE_NAME,
                    MainDbAdapter.genColName, new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, MainDbAdapter.isActiveCondition, MainDbAdapter.GEN_COL_NAME_NAME,
                    mPreferences.getLong("MileageInsertExpenseType_ID", -1), true);

            ((EditText) findViewById(R.id.mileageEditInputEntry)).requestFocus();

        }
        userCommentAdapter = new ArrayAdapter<String>(MileageEditActivity.this,
                android.R.layout.simple_dropdown_item_1line, 
                mMainDbAdapter.getAutoCompleteUserComments(MainDbAdapter.MILEAGE_TABLE_NAME, mCarId, 30));
        mileageEditUserCommentEntry.setAdapter(userCommentAdapter);
        
        if(mInsertMode == StaticValues.mileageInsertModeNewIndex) {
            mileageEditInsertModeIndexRb.setChecked(true);
            mileageEditInputLabel.setText(
                    mRes.getString(R.string.MILEAGE_EDIT_ACTIVITY_OPTION_INDEX));
            mileageEditCalculatedTextLabel.setText(
                    mRes.getString(R.string.MILEAGE_EDIT_ACTIVITY_OPTION_MILEAGE));
            mileageEditInputEntry.setTag(mRes.getString(R.string.MILEAGE_EDIT_ACTIVITY_OPTION_INDEX));
        }
        else {
            mileageEditInsertModeMileageRb.setChecked(true);
            mileageEditInputLabel.setText(
                    mRes.getString(R.string.MILEAGE_EDIT_ACTIVITY_OPTION_MILEAGE));
            mileageEditCalculatedTextLabel.setText(
                    mRes.getString(R.string.MILEAGE_EDIT_ACTIVITY_OPTION_INDEX));
            mileageEditInputEntry.setTag(mRes.getString(R.string.MILEAGE_EDIT_ACTIVITY_OPTION_MILEAGE));
        }
        mUOMLengthId = mMainDbAdapter.fetchRecord(MainDbAdapter.CAR_TABLE_NAME, MainDbAdapter.carTableColNames, mCarId)
                                .getLong(MainDbAdapter.CAR_COL_UOMLENGTH_ID_POS);
        if(currentDriverName != null) {
            driverCarLbl = mRes.getString(R.string.GEN_DRIVER_LABEL) + currentDriverName;
        }
        if(currentCarName != null) {
            driverCarLbl = driverCarLbl + "; "
                    + mRes.getString(R.string.GEN_CAR_LABEL) + " " + currentCarName;
        }
        ((TextView) findViewById(R.id.mileageEditCarDriverLabel)).setText(driverCarLbl);

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

            if(mileageEditStartIndexEntry.getText() == null
                    || mileageEditStartIndexEntry.getText().toString().length() == 0)
                mStartIndex = new BigDecimal("0");
            else
                mStartIndex = new BigDecimal(mileageEditStartIndexEntry.getText().toString());

            if(mileageEditInputEntry.getText().toString().length() == 0)
                mEntryMileageValue = new BigDecimal("0");
            else
                mEntryMileageValue = new BigDecimal(mileageEditInputEntry.getText().toString());

            BigDecimal pEntryMileageValue = mEntryMileageValue;
            BigDecimal pStartIndex = mStartIndex;

            if(mInsertMode == StaticValues.mileageInsertModeNewIndex) { //new index
                pNewIndex = pEntryMileageValue;
                if(pNewIndex.compareTo(pStartIndex) < 0) {
                    mileageEditCalculatedTextContent.setText("N/A;");
                }
                else {
                    BigDecimal mileage = pNewIndex.subtract(pStartIndex);
                    mileageEditCalculatedTextContent.setText(mileage.toString() + ";");
                }
            }
            else { //mileage
                pNewIndex = mStartIndex.add(pEntryMileageValue);
                mileageEditCalculatedTextContent.setText(pNewIndex.toString() + ";");
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
            Cursor c = mMainDbAdapter.execSelectSql(sql);
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
        mileageEditStartIndexEntry.setText(mStartIndex.toString());
        return mStartIndex;
    }

    private View.OnClickListener btnOkClickListener =  new View.OnClickListener() {
            public void onClick(View arg0) {
                //check mandatory fileds & index preconditions

                calculateMileageOrNewIndex();
                String retVal = checkMandatory((ViewGroup) findViewById(R.id.genRootViewGroup));
                if( retVal != null ) {
                    Toast toast = Toast.makeText( getApplicationContext(),
                            mRes.getString( R.string.GEN_FILL_MANDATORY ) + ": " + retVal, Toast.LENGTH_SHORT );
                    toast.show();
                    return;
                }
                int operationResult = -1;
                ContentValues data = new ContentValues();
                data.put( MainDbAdapter.GEN_COL_NAME_NAME, "");
                data.put( MainDbAdapter.GEN_COL_ISACTIVE_NAME, "Y");
                data.put( MainDbAdapter.GEN_COL_USER_COMMENT_NAME,
                        mileageEditUserCommentEntry.getText().toString());
                data.put( MainDbAdapter.MILEAGE_COL_DATE_NAME, mDateTimeInSeconds);
                data.put( MainDbAdapter.MILEAGE_COL_CAR_ID_NAME, mCarId);
                data.put( MainDbAdapter.MILEAGE_COL_DRIVER_ID_NAME, mDriverId);
                data.put( MainDbAdapter.MILEAGE_COL_INDEXSTART_NAME, mStartIndex.toString());
                    data.put( MainDbAdapter.MILEAGE_COL_INDEXSTOP_NAME, mNewIndex.toString());
                data.put( MainDbAdapter.MILEAGE_COL_UOMLENGTH_ID_NAME, mUOMLengthId);
                data.put( MainDbAdapter.MILEAGE_COL_EXPENSETYPE_ID_NAME, mExpTypeSpinner.getSelectedItemId());
                data.put( MainDbAdapter.MILEAGE_COL_GPSTRACKLOG_NAME, "");
                if(operationType.equals("N")){
                    operationResult = mMainDbAdapter.checkIndex(-1, mCarId, mStartIndex, mNewIndex);
                    if(operationResult == -1){
                        Long createResult = mMainDbAdapter.createRecord(MainDbAdapter.MILEAGE_TABLE_NAME, data);
                        if( createResult.intValue() < 0){
                            if(createResult.intValue() == -1) //DB Error
                                errorAlertBuilder.setMessage(mMainDbAdapter.lastErrorMessage);
                            else //precondition error
                                errorAlertBuilder.setMessage(mRes.getString(-1 * createResult.intValue()));
                            errorAlert = errorAlertBuilder.create();
                            errorAlert.show();
                            return;
                        }
                    }
                }
                else{
                    operationResult = mMainDbAdapter.checkIndex(mRowId, mCarId, mStartIndex, mNewIndex);
                    if(operationResult == -1){
                        int updResult = mMainDbAdapter.updateRecord(MainDbAdapter.MILEAGE_TABLE_NAME, mRowId, data);
                        if(updResult != -1){
                            String errMsg = "";
                            errMsg = mRes.getString(updResult);
                            if(updResult == R.string.ERR_000)
                                errMsg = errMsg + "\n" + mMainDbAdapter.lastErrorMessage;
                            errorAlertBuilder.setMessage(errMsg);
                            errorAlert = errorAlertBuilder.create();
                            errorAlert.show();
                            return;
                        }
                    }
                }
                if( operationResult != -1) //error
                {
                    errorAlertBuilder.setMessage(mRes.getString(operationResult));
                    errorAlert = errorAlertBuilder.create();
                    errorAlert.show();
                    return;
                }
                else{
                    Toast toast = Toast.makeText( getApplicationContext(),
                            (operationType.equals("N") ?
                                mRes.getString(R.string.MILEAGE_EDIT_ACTIVITY_INSERTOK_MESSAGE)
                                : mRes.getString(R.string.MILEAGE_EDIT_ACTIVITY_UPDATEOK_MESSAGE)) ,
                            Toast.LENGTH_SHORT );
                    toast.show();
                    userCommentAdapter = null;
                    userCommentAdapter = new ArrayAdapter<String>(MileageEditActivity.this,
                            android.R.layout.simple_dropdown_item_1line, 
                            mMainDbAdapter.getAutoCompleteUserComments(MainDbAdapter.MILEAGE_TABLE_NAME, mCarId, 30));
                    mileageEditUserCommentEntry.setAdapter(userCommentAdapter);
                }

                //mileage inserted. reinit the activity for new mileage

                if(operationType.equals("N")){
                    mStartIndex = BigDecimal.valueOf(0);
                    fillGetCurrentIndex();

                    mileageEditInputEntry.setText("");
                    mNewIndex = BigDecimal.valueOf(0);
                    mEntryMileageValue = BigDecimal.valueOf(0);
                    mileageEditUserCommentEntry.setText("");
                    calculateMileageOrNewIndex();
                }
                finish();
            }
    };

    private RadioGroup.OnCheckedChangeListener rgOnCheckedChangeListener  =
            new RadioGroup.OnCheckedChangeListener() {
                    public void onCheckedChanged(RadioGroup arg0, int checkedId) {
                        if(checkedId == mileageEditInsertModeIndexRb.getId()) {
                            mInsertMode = StaticValues.mileageInsertModeNewIndex; //new index
                            mileageEditInputLabel.setText(
                                    mRes.getString(R.string.MILEAGE_EDIT_ACTIVITY_OPTION_INDEX));
                            mileageEditCalculatedTextLabel.setText(
                                    mRes.getString(R.string.MILEAGE_EDIT_ACTIVITY_OPTION_MILEAGE));
                            mileageEditInputEntry.setTag(mRes.getString(R.string.MILEAGE_EDIT_ACTIVITY_OPTION_INDEX));
                        }
                        else {
                            mInsertMode = StaticValues.mileageEditInsertModeMileage;
                            mileageEditInputLabel.setText(
                                    mRes.getString(R.string.MILEAGE_EDIT_ACTIVITY_OPTION_MILEAGE));
                            mileageEditCalculatedTextLabel.setText(
                                    mRes.getString(R.string.MILEAGE_EDIT_ACTIVITY_OPTION_INDEX));
                            mileageEditInputEntry.setTag(mRes.getString(R.string.MILEAGE_EDIT_ACTIVITY_OPTION_MILEAGE));
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
