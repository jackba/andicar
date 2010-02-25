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
import android.database.Cursor;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;
import org.andicar.persistence.MainDbAdapter;
import org.andicar.utils.Constants;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.AutoCompleteTextView;

/**
 *
 * @author miki
 */
public class RefuelEditActivity extends EditActivityBase {

    AlertDialog.Builder insertUpdateErrorAlertBuilder;
    AlertDialog insertUpdateErrorAlert;
    AutoCompleteTextView refuelEditUserComment;
    Spinner refuelEditCarSpinner;
    Spinner refuelEditDriverSpinner;
    EditText carIndexEntry;
    EditText qtyEntry;
    EditText priceEntry;
    EditText docNo;

    ArrayAdapter<String> userCommentAdapter;
    RefuelEditActivity ea;
    private String operationType;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle, R.layout.refuel_edit_activity, mOkClickListener);
        insertUpdateErrorAlertBuilder = new AlertDialog.Builder( this );
        insertUpdateErrorAlertBuilder.setCancelable( false );
        insertUpdateErrorAlertBuilder.setPositiveButton( mRes.getString(R.string.GEN_OK), null );

        operationType = extras.getString("Operation");
        refuelEditUserComment = ((AutoCompleteTextView) findViewById( R.id.genUserCommentEntry ));

        refuelEditCarSpinner = (Spinner)findViewById(R.id.refuelEditCarSpinner);
        refuelEditDriverSpinner = (Spinner)findViewById(R.id.refuelEditDriverSpinner);
        refuelEditCarSpinner.setOnItemSelectedListener(spinnerCarDriverOnItemSelectedListener);
        refuelEditDriverSpinner.setOnItemSelectedListener(spinnerCarDriverOnItemSelectedListener);

        ea = this;
        userCommentAdapter = new ArrayAdapter<String>(ea,
                android.R.layout.simple_dropdown_item_1line,
                mMainDbHelper.getAutoCompleteRefuelUserComments(mPreferences.getLong("CurrentCar_ID", -1), mPreferences.getLong("CurrentDriver_ID", -1)));
        refuelEditUserComment.setAdapter(userCommentAdapter);

        carIndexEntry = (EditText)findViewById(R.id.refuelEditIndexEntry);
        qtyEntry = (EditText)findViewById(R.id.refuelEditQuantityEntry);
        priceEntry = (EditText)findViewById(R.id.refuelEditPriceEntry);
        docNo = (EditText)findViewById(R.id.refuelEditDocumentNoEntry);


        long mCarId;
        long mDriverId;
        long mExpTypeId;
        long mQtyUmId;
        long mCurrencyId;

        if (operationType.equals("E")) {
            mRowId = extras.getLong( MainDbAdapter.GEN_COL_ROWID_NAME );
            Cursor recordCursor = mMainDbHelper.fetchRecord(MainDbAdapter.REFUEL_TABLE_NAME, MainDbAdapter.refuelTableColNames, mRowId);
            mCarId = recordCursor.getLong(MainDbAdapter.REFUEL_COL_CAR_ID_POS);
            mDriverId = recordCursor.getLong(MainDbAdapter.REFUEL_COL_DRIVER_ID_POS);
            mExpTypeId = recordCursor.getLong(MainDbAdapter.REFUEL_COL_EXPENSETYPE_ID_POS);
            mQtyUmId = recordCursor.getLong(MainDbAdapter.REFUEL_COL_UOMVOLUME_ID_POS);
            mCurrencyId = recordCursor.getLong(MainDbAdapter.REFUEL_COL_CURRENCY_ID_POS);
            initDateTime(recordCursor.getLong(MainDbAdapter.REFUEL_COL_DATE_POS) * 1000);
            Float floatVal = recordCursor.getFloat(MainDbAdapter.REFUEL_COL_INDEX_POS);
            if(floatVal == floatVal.intValue())
                carIndexEntry.setText(Integer.toString(floatVal.intValue()));
            else
                carIndexEntry.setText(Float.toString(floatVal));
            floatVal = recordCursor.getFloat(MainDbAdapter.REFUEL_COL_QUANTITY_POS);
            if(floatVal == floatVal.intValue())
                qtyEntry.setText(Integer.toString(floatVal.intValue()));
            else
                qtyEntry.setText(Float.toString(floatVal));
            floatVal = recordCursor.getFloat(MainDbAdapter.REFUEL_COL_PRICE_POS);
            if(floatVal == floatVal.intValue())
                priceEntry.setText(Integer.toString(floatVal.intValue()));
            else
                priceEntry.setText(Float.toString(floatVal));
            docNo.setText(recordCursor.getString(MainDbAdapter.REFUEL_COL_DOCUMENTNO_POS));
            refuelEditUserComment.setText(recordCursor.getString(MainDbAdapter.GEN_COL_USER_COMMENT_POS));

        }
        else {
            mCarId = mPreferences.getLong("CurrentCar_ID", -1);
            mDriverId = mPreferences.getLong("CurrentDriver_ID", -1);
            mExpTypeId = mPreferences.getLong("RefuelExpenseType_ID", -1);
            mQtyUmId = mPreferences.getLong("CarUOMVolume_ID", -1);
            mCurrencyId = mPreferences.getLong("CarCurrency_ID", -1);
            initDateTime(System.currentTimeMillis());

        }
        
        initSpinner(refuelEditCarSpinner, MainDbAdapter.CAR_TABLE_NAME, MainDbAdapter.genColName,
                new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, MainDbAdapter.isActiveCondition, MainDbAdapter.GEN_COL_NAME_NAME,
                mCarId, false);

        initSpinner(refuelEditDriverSpinner, MainDbAdapter.DRIVER_TABLE_NAME, MainDbAdapter.genColName,
                new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, MainDbAdapter.isActiveCondition, MainDbAdapter.GEN_COL_NAME_NAME,
                mDriverId, false);

        initSpinner((Spinner)findViewById(R.id.refuelEditExpenseTypeSpinner), MainDbAdapter.EXPENSETYPE_TABLE_NAME, MainDbAdapter.genColName,
                new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, MainDbAdapter.isActiveCondition, MainDbAdapter.GEN_COL_NAME_NAME,
                mExpTypeId, true);

        initSpinner((Spinner) findViewById( R.id.refuelEditUOMVolumeSpinner ), MainDbAdapter.UOM_TABLE_NAME,
                MainDbAdapter.uomTableColNames, new String[]{MainDbAdapter.UOM_COL_CODE_NAME},
                MainDbAdapter.UOM_COL_UOMTYPE_NAME + "='" + Constants.UOM_VOLUME_TYPE_CODE + "'" +
                    MainDbAdapter.isActiveWithAndCondition, MainDbAdapter.UOM_COL_CODE_NAME,
                    mQtyUmId, false);
        initSpinner((Spinner) findViewById( R.id.refuelEditCurrencySpinner ), MainDbAdapter.CURRENCY_TABLE_NAME,
                MainDbAdapter.currencyTableColNames, new String[]{MainDbAdapter.CURRENCY_COL_CODE_NAME},
                    MainDbAdapter.isActiveCondition,
                    MainDbAdapter.CURRENCY_COL_CODE_NAME,
                    mCurrencyId, false);
        
//        android.R.color.white
    }

    private View.OnClickListener mOkClickListener =
            new View.OnClickListener()
                {
                    public void onClick( View v )
                    {
                        Float floatVal = null;
                        String floatValStr = null;

                        String retVal = checkMandatory((ViewGroup) findViewById(R.id.genRootViewGroup));
                        if( retVal != null ) {
                            Toast toast = Toast.makeText( getApplicationContext(),
                                    mRes.getString( R.string.GEN_FILL_MANDATORY ) + ": " + retVal, Toast.LENGTH_SHORT );
                            toast.show();
                            return;
                        }

                        ContentValues data = new ContentValues();
                        data.put( MainDbAdapter.GEN_COL_NAME_NAME,
                                "Refuel");
                        data.put( MainDbAdapter.GEN_COL_ISACTIVE_NAME, "Y");
                        data.put( MainDbAdapter.GEN_COL_USER_COMMENT_NAME,
                                refuelEditUserComment.getText().toString() );
                        data.put( MainDbAdapter.REFUEL_COL_CAR_ID_NAME,
                                refuelEditCarSpinner.getSelectedItemId() );
                        data.put( MainDbAdapter.REFUEL_COL_DRIVER_ID_NAME,
                                refuelEditDriverSpinner.getSelectedItemId() );
                        data.put( MainDbAdapter.REFUEL_COL_EXPENSETYPE_ID_NAME,
                                ((Spinner) findViewById( R.id.refuelEditExpenseTypeSpinner )).getSelectedItemId() );

                        floatValStr = carIndexEntry.getText().toString();
                        if( floatValStr != null && floatValStr.length() > 0 ) {
                            try {
                                floatVal = Float.parseFloat( floatValStr );
                            }
                            catch( NumberFormatException e ) {
                                Toast toast = Toast.makeText( getApplicationContext(),
                                        mRes.getString( R.string.GEN_NUMBER_FORMAT_EXCEPTION ) + ": "
                                        + mRes.getString( R.string.REFUEL_EDIT_ACTIVITY_INDEX_LABEL ), Toast.LENGTH_SHORT );
                                toast.show();
                                return;
                            }
                        }
                        data.put( MainDbAdapter.REFUEL_COL_INDEX_NAME, floatVal);

                        floatVal = null;
                        floatValStr = qtyEntry.getText().toString();
                        if( floatValStr != null && floatValStr.length() > 0 ) {
                            try {
                                floatVal = Float.parseFloat( floatValStr );
                            }
                            catch( NumberFormatException e ) {
                                Toast toast = Toast.makeText( getApplicationContext(),
                                        mRes.getString( R.string.GEN_NUMBER_FORMAT_EXCEPTION ) + ": "
                                        + mRes.getString( R.string.REFUEL_EDIT_ACTIVITY_QUANTITY_LABEL ), Toast.LENGTH_SHORT );
                                toast.show();
                                return;
                            }
                        }
                        data.put( MainDbAdapter.REFUEL_COL_QUANTITY_NAME, floatVal);

                        data.put( MainDbAdapter.REFUEL_COL_UOMVOLUME_ID_NAME,
                                ((Spinner) findViewById( R.id.refuelEditUOMVolumeSpinner )).getSelectedItemId() );

                        floatVal = null;
                        floatValStr = priceEntry.getText().toString();
                        if( floatValStr != null && floatValStr.length() > 0 ) {
                            try {
                                floatVal = Float.parseFloat( floatValStr );
                            }
                            catch( NumberFormatException e ) {
                                Toast toast = Toast.makeText( getApplicationContext(),
                                        mRes.getString( R.string.GEN_NUMBER_FORMAT_EXCEPTION ) + ": "
                                        + mRes.getString( R.string.REFUEL_EDIT_ACTIVITY_PRICE_LABEL ), Toast.LENGTH_SHORT );
                                toast.show();
                                return;
                            }
                        }
                        data.put( MainDbAdapter.REFUEL_COL_PRICE_NAME, floatVal);

                        data.put( MainDbAdapter.REFUEL_COL_CURRENCY_ID_NAME,
                                ((Spinner) findViewById( R.id.refuelEditCurrencySpinner )).getSelectedItemId() );
                        data.put( MainDbAdapter.REFUEL_COL_DATE_NAME, mDateTimeInSeconds);
                        data.put( MainDbAdapter.REFUEL_COL_DOCUMENTNO_NAME,
                                docNo.getText().toString());

                        if( operationType.equals("N") ) {
                            if(mMainDbHelper.createRecord(MainDbAdapter.REFUEL_TABLE_NAME, data) < 0){
                                insertUpdateErrorAlertBuilder.setMessage(mMainDbHelper.lastErrorMessage);
                                insertUpdateErrorAlert = insertUpdateErrorAlertBuilder.create();
                                insertUpdateErrorAlert.show();
                            }
                            else
                                finish();
                        }
                        else {
                            if(!mMainDbHelper.updateRecord(MainDbAdapter.REFUEL_TABLE_NAME, mRowId, data)){
                                insertUpdateErrorAlertBuilder.setMessage(mRes.getString(R.string.ERR_007));
                                insertUpdateErrorAlert = insertUpdateErrorAlertBuilder.create();
                                insertUpdateErrorAlert.show();
                            }
                            else
                                finish();
                        }
                    }
                };

    private AdapterView.OnItemSelectedListener spinnerCarDriverOnItemSelectedListener =
            new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    userCommentAdapter = null;
                    userCommentAdapter = new ArrayAdapter<String>(ea,
                            android.R.layout.simple_dropdown_item_1line,
                            mMainDbHelper.getAutoCompleteRefuelUserComments(refuelEditCarSpinner.getSelectedItemId(),
                                refuelEditDriverSpinner.getSelectedItemId()));
                    refuelEditUserComment.setAdapter(userCommentAdapter);
                }
                public void onNothingSelected(AdapterView<?> arg0) {
                }
            };
}
