/*
Copyright (C) 2009-2010 Miklos Keresztes - miklos.keresztes@gmail.com

This program is free software; you can redistribute it and/or modify it
under the terms of the GNU General Public License as published by the
Free Software Foundation; either version 2 of the License.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program;
if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.andicar.activity;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.view.KeyEvent;
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
//import java.sql.Timestamp;
import org.andicar.persistence.MainDbAdapter;
import org.andicar.utils.Constants;

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

    AlertDialog.Builder insertUpdateErrorAlertBuilder;
    AlertDialog insertUpdateErrorAlert;
    ArrayAdapter<String> userCommentAdapter;
    MileageEditActivity me;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle, R.layout.mileage_edit_activity, btnOkClickListener);
        me = this;
        insertUpdateErrorAlertBuilder = new AlertDialog.Builder( this );
        insertUpdateErrorAlertBuilder.setCancelable( false );
        insertUpdateErrorAlertBuilder.setPositiveButton( mRes.getString(R.string.GEN_OK), null );

        mileageEditCalculatedTextContent = (TextView) findViewById(R.id.mileageEditCalculatedTextContent);
        mileageEditInputEntry = (EditText) findViewById(R.id.mileageEditInputEntry);
        mileageEditInputEntry.setOnKeyListener(mileageEditInputEntryOnKeyListener);
        mileageEditStartIndexEntry = (EditText) findViewById(R.id.mileageEditStartIndexEntry);
        mileageEditStartIndexEntry.setOnKeyListener(startIndexEntryKeyListener);
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

            mCarId = mMainDbHelper.fetchRecord(MainDbAdapter.MILEAGE_TABLE_NAME, MainDbAdapter.mileageTableColNames, mRowId)
                                .getLong(MainDbAdapter.MILEAGE_COL_CAR_ID_POS);
            mDriverId = mMainDbHelper.fetchRecord(MainDbAdapter.MILEAGE_TABLE_NAME, MainDbAdapter.mileageTableColNames, mRowId)
                                .getLong(MainDbAdapter.MILEAGE_COL_DRIVER_ID_POS);

            currentDriverName = mMainDbHelper.fetchRecord(MainDbAdapter.DRIVER_TABLE_NAME, MainDbAdapter.driverTableColNames, mDriverId)
                                .getString(MainDbAdapter.GEN_COL_NAME_POS);
            currentCarName = mMainDbHelper.fetchRecord(MainDbAdapter.CAR_TABLE_NAME, MainDbAdapter.carTableColNames, mCarId)
                                .getString(MainDbAdapter.GEN_COL_NAME_POS);

            Cursor recordCursor = mMainDbHelper.fetchRecord(MainDbAdapter.MILEAGE_TABLE_NAME, MainDbAdapter.mileageTableColNames, mRowId);
            mStartIndex = new BigDecimal(recordCursor.getString(MainDbAdapter.MILEAGE_COL_INDEXSTART_POS));
            BigDecimal stopIndex = new BigDecimal(recordCursor.getString(MainDbAdapter.MILEAGE_COL_INDEXSTOP_POS));
            mileageEditInputEntry.setText(stopIndex.toString());
            mileageEditInsertModeIndexRb.setChecked(true);
            mInsertMode = Constants.mileageInsertModeNewIndex;
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
        userCommentAdapter = new ArrayAdapter<String>(me,
                android.R.layout.simple_dropdown_item_1line, mMainDbHelper.getAutoCompleteMileageUserComments(mCarId,mDriverId, 30));
        mileageEditUserCommentEntry.setAdapter(userCommentAdapter);
        
        if(mInsertMode == Constants.mileageInsertModeNewIndex) {
            mileageEditInsertModeIndexRb.setChecked(true);
            mileageEditInputLabel.setText(
                    mRes.getString(R.string.MILEAGE_EDIT_ACTIVITY_OPTION_INDEX) + ":");
            mileageEditCalculatedTextLabel.setText(
                    mRes.getString(R.string.MILEAGE_EDIT_ACTIVITY_OPTION_MILEAGE) + ":");
            mileageEditInputEntry.setTag(mRes.getString(R.string.MILEAGE_EDIT_ACTIVITY_OPTION_INDEX));
        }
        else {
            mileageEditInsertModeMileageRb.setChecked(true);
            mileageEditInputLabel.setText(
                    mRes.getString(R.string.MILEAGE_EDIT_ACTIVITY_OPTION_MILEAGE) + ":");
            mileageEditCalculatedTextLabel.setText(
                    mRes.getString(R.string.MILEAGE_EDIT_ACTIVITY_OPTION_INDEX) + ":");
            mileageEditInputEntry.setTag(mRes.getString(R.string.MILEAGE_EDIT_ACTIVITY_OPTION_MILEAGE));
        }
        mUOMLengthId = mMainDbHelper.fetchRecord(MainDbAdapter.CAR_TABLE_NAME, MainDbAdapter.carTableColNames, mCarId)
                                .getLong(MainDbAdapter.CAR_COL_UOMLENGTH_ID_POS);
        if(currentDriverName != null) {
            driverCarLbl = mRes.getString(R.string.CURRENT_DRIVER_NAME) + currentDriverName;
        }
        if(currentCarName != null) {
            driverCarLbl = driverCarLbl + "; "
                    + mRes.getString(R.string.CURRENT_CAR_NAME) + " " + currentCarName;
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

            if(mInsertMode == Constants.mileageInsertModeNewIndex) { //new index
                pNewIndex = pEntryMileageValue;
                if(pNewIndex.compareTo(pStartIndex) < 0) {
                    mileageEditCalculatedTextContent.setText("N/A");
                }
                else {
                    BigDecimal mileage = pNewIndex.subtract(pStartIndex);
                    mileageEditCalculatedTextContent.setText(mileage.toString());
                }
            }
            else { //mileage
                pNewIndex = mStartIndex.add(pEntryMileageValue);
                mileageEditCalculatedTextContent.setText(pNewIndex.toString());
            }
            mNewIndex = pNewIndex;
        }
        catch(NumberFormatException e){};
    }

    private BigDecimal fillGetCurrentIndex() throws SQLException {
        if(mStartIndex.equals(new BigDecimal("0")))
            mStartIndex = new BigDecimal(mMainDbHelper.fetchRecord(MainDbAdapter.CAR_TABLE_NAME, MainDbAdapter.carTableColNames, mCarId)
                    .getString(MainDbAdapter.CAR_COL_INDEXCURRENT_POS));
        
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
                String operationResult = null;
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
                    operationResult = mMainDbHelper.checkIndex(-1, mCarId, mStartIndex, mNewIndex);
                    if(operationResult == null){
                        if(mMainDbHelper.createRecord(MainDbAdapter.MILEAGE_TABLE_NAME, data) < 0){
                            insertUpdateErrorAlertBuilder.setMessage(mMainDbHelper.lastErrorMessage);
                            insertUpdateErrorAlert = insertUpdateErrorAlertBuilder.create();
                            insertUpdateErrorAlert.show();
                            return;
                        }
                    }
                }
                else{
                    operationResult = mMainDbHelper.checkIndex(mRowId, mCarId, mStartIndex, mNewIndex);
                    if(operationResult == null){
                        if(!mMainDbHelper.updateRecord(MainDbAdapter.MILEAGE_TABLE_NAME, mRowId, data)){
                            insertUpdateErrorAlertBuilder.setMessage(mRes.getString(R.string.ERR_007));
                            insertUpdateErrorAlert = insertUpdateErrorAlertBuilder.create();
                            insertUpdateErrorAlert.show();
                            return;
                        }
                    }
                }
                if( operationResult != null) //error
                {
                    if(operationResult.equals(Constants.errStartIndexOverlap)){
                        insertUpdateErrorAlertBuilder.setMessage(mRes.getString(R.string.ERR_001));
                    }
                    else if(operationResult.equals(Constants.errNewIndexOverlap)){
                        insertUpdateErrorAlertBuilder.setMessage(mRes.getString(R.string.ERR_002));
                    }
                    else if(operationResult.equals(Constants.errMileageOverlap)){
                        insertUpdateErrorAlertBuilder.setMessage(mRes.getString(R.string.ERR_003));
                    }
                    else if(operationResult.equals(Constants.errStopBeforeStartIndex)){
                        insertUpdateErrorAlertBuilder.setMessage(mRes.getString(R.string.ERR_004));
                    }
                    else
                        insertUpdateErrorAlertBuilder.setMessage(operationResult);
                    
                    insertUpdateErrorAlert = insertUpdateErrorAlertBuilder.create();
                    insertUpdateErrorAlert.show();
                }
                else{
                    Toast toast = Toast.makeText( getApplicationContext(),
                            (operationType.equals("N") ?
                                mRes.getString(R.string.MILEAGE_EDIT_ACTIVITY_INSERTOK_MESSAGE)
                                : mRes.getString(R.string.MILEAGE_EDIT_ACTIVITY_UPDATEOK_MESSAGE)) ,
                            Toast.LENGTH_SHORT );
                    toast.show();
                    userCommentAdapter = null;
                    userCommentAdapter = new ArrayAdapter<String>(me,
                            android.R.layout.simple_dropdown_item_1line, mMainDbHelper.getAutoCompleteMileageUserComments(mCarId,mDriverId, 30));
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

    private View.OnKeyListener startIndexEntryKeyListener =
            new View.OnKeyListener() {
                    public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
                        if(arg2.getAction() != KeyEvent.ACTION_UP) {
                            return false;
                        }
                        calculateMileageOrNewIndex();
                        return false;
                    };
                };

    private RadioGroup.OnCheckedChangeListener rgOnCheckedChangeListener  =
            new RadioGroup.OnCheckedChangeListener() {
                    public void onCheckedChanged(RadioGroup arg0, int checkedId) {
                        if(checkedId == mileageEditInsertModeIndexRb.getId()) {
                            mInsertMode = Constants.mileageInsertModeNewIndex; //new index
                            mileageEditInputLabel.setText(
                                    mRes.getString(R.string.MILEAGE_EDIT_ACTIVITY_OPTION_INDEX) + ":");
                            mileageEditCalculatedTextLabel.setText(
                                    mRes.getString(R.string.MILEAGE_EDIT_ACTIVITY_OPTION_MILEAGE) + ":");
                            mileageEditInputEntry.setTag(mRes.getString(R.string.MILEAGE_EDIT_ACTIVITY_OPTION_INDEX));
                        }
                        else {
                            mInsertMode = Constants.mileageEditInsertModeMileage;
                            mileageEditInputLabel.setText(
                                    mRes.getString(R.string.MILEAGE_EDIT_ACTIVITY_OPTION_MILEAGE) + ":");
                            mileageEditCalculatedTextLabel.setText(
                                    mRes.getString(R.string.MILEAGE_EDIT_ACTIVITY_OPTION_INDEX) + ":");
                            mileageEditInputEntry.setTag(mRes.getString(R.string.MILEAGE_EDIT_ACTIVITY_OPTION_MILEAGE));
                        }
                        SharedPreferences.Editor editor = mPreferences.edit();
                        editor.putInt("MileageInsertMode", mInsertMode);
                        editor.commit();
                        calculateMileageOrNewIndex();
                    }
                };

    private View.OnKeyListener mileageEditInputEntryOnKeyListener =
            new View.OnKeyListener() {
                    public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
                        if(arg2.getAction() != KeyEvent.ACTION_UP) {
                            return false;
                        }
                        calculateMileageOrNewIndex();
                        return false;
                    }
                };


}
