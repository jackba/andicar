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

import android.app.AlertDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
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
//import java.math.BigDecimal;

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
                mMainDbHelper.getAutoCompleteUserComments(MainDbAdapter.REFUEL_TABLE_NAME, mPreferences.getLong("CurrentCar_ID", -1), mPreferences.getLong("CurrentDriver_ID", -1), 30));
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
            carIndexEntry.setText(recordCursor.getString(MainDbAdapter.REFUEL_COL_INDEX_POS));
            qtyEntry.setText(recordCursor.getString(MainDbAdapter.REFUEL_COL_QUANTITY_POS));
            priceEntry.setText(recordCursor.getString(MainDbAdapter.REFUEL_COL_PRICE_POS));
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
                MainDbAdapter.UOM_COL_UOMTYPE_NAME + "='" + StaticValues.UOM_VOLUME_TYPE_CODE + "'" +
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
                        data.put( MainDbAdapter.REFUEL_COL_INDEX_NAME, carIndexEntry.getText().toString());
                        data.put( MainDbAdapter.REFUEL_COL_QUANTITY_NAME, qtyEntry.getText().toString());
                        data.put( MainDbAdapter.REFUEL_COL_UOMVOLUME_ID_NAME,
                                ((Spinner) findViewById( R.id.refuelEditUOMVolumeSpinner )).getSelectedItemId() );
                        data.put( MainDbAdapter.REFUEL_COL_PRICE_NAME, priceEntry.getText().toString());
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
                            mMainDbHelper.getAutoCompleteUserComments(MainDbAdapter.REFUEL_TABLE_NAME, refuelEditCarSpinner.getSelectedItemId(),
                                refuelEditDriverSpinner.getSelectedItemId(), 30));
                    refuelEditUserComment.setAdapter(userCommentAdapter);
                }
                public void onNothingSelected(AdapterView<?> arg0) {
                }
            };
}
