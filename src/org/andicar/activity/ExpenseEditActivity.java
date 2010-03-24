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
import android.widget.TextView;
//import java.math.BigDecimal;

/**
 *
 * @author miki
 */
public class ExpenseEditActivity extends EditActivityBase {
    AutoCompleteTextView userComment;
    Spinner carSpinner;
    Spinner driverSpinner;
    EditText carIndexEntry;
    EditText amountEntry;
    EditText docNo;
    TextView warningLabel;

    ArrayAdapter<String> userCommentAdapter;
//    ExpenseEditActivity ea;
    private String operationType;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle, R.layout.expense_edit_activity, mOkClickListener);

        operationType = extras.getString("Operation");
        userComment = ((AutoCompleteTextView) findViewById( R.id.genUserCommentEntry ));

        carSpinner = (Spinner)findViewById(R.id.carSpinner);
        driverSpinner = (Spinner)findViewById(R.id.driverSpinner);
        carSpinner.setOnItemSelectedListener(spinnerCarDriverOnItemSelectedListener);
        driverSpinner.setOnItemSelectedListener(spinnerCarDriverOnItemSelectedListener);

//        ea = this;
        userCommentAdapter = new ArrayAdapter<String>(ExpenseEditActivity.this,
                android.R.layout.simple_dropdown_item_1line,
                mMainDbAdapter.getAutoCompleteUserComments(MainDbAdapter.EXPENSES_TABLE_NAME,
                mPreferences.getLong("CurrentCar_ID", -1), 30));
        userComment.setAdapter(userCommentAdapter);

        carIndexEntry = (EditText)findViewById(R.id.indexEntry);
        amountEntry = (EditText)findViewById(R.id.amountEntry);
        docNo = (EditText)findViewById(R.id.documentNoEntry);

        warningLabel = (TextView)findViewById(R.id.warningLabel);


        long mCarId;
        long mDriverId;
        long mExpCategoryId = 0;
        long mExpTypeId = 0;
        long mCurrencyId;

        if (operationType.equals("E")) {
            mRowId = extras.getLong( MainDbAdapter.GEN_COL_ROWID_NAME );
            Cursor recordCursor = mMainDbAdapter.fetchRecord(MainDbAdapter.EXPENSES_TABLE_NAME,
                    MainDbAdapter.expensesTableColNames, mRowId);
            mCarId = recordCursor.getLong(MainDbAdapter.EXPENSES_COL_CAR_ID_POS);
            mDriverId = recordCursor.getLong(MainDbAdapter.EXPENSES_COL_DRIVER_ID_POS);
            mExpCategoryId = recordCursor.getLong(MainDbAdapter.EXPENSES_COL_EXPENSECATEGORY_POS);
            mExpTypeId = recordCursor.getLong(MainDbAdapter.EXPENSES_COL_EXPENSETYPE_ID_POS);
            mCurrencyId = recordCursor.getLong(MainDbAdapter.EXPENSES_COL_CURRENCY_ID_POS);
            initDateTime(recordCursor.getLong(MainDbAdapter.EXPENSES_COL_DATE_POS) * 1000);
            carIndexEntry.setText(recordCursor.getString(MainDbAdapter.EXPENSES_COL_INDEX_POS));
            amountEntry.setText(recordCursor.getString(MainDbAdapter.EXPENSES_COL_AMOUNT_POS));
            docNo.setText(recordCursor.getString(MainDbAdapter.EXPENSES_COL_DOCUMENTNO_POS));
            userComment.setText(recordCursor.getString(MainDbAdapter.GEN_COL_USER_COMMENT_POS));

            String fromTable = recordCursor.getString(MainDbAdapter.EXPENSES_COL_FROMTABLE_POS);
            if(fromTable == null){
                warningLabel.setText("");
                setEditable((ViewGroup) findViewById(R.id.genRootViewGroup), true);
            }
            else{
                if(fromTable.equals("Refuel")){
                    warningLabel.setText(mRes.getString(R.string.EXPENSEEDIT_ACTIVITY_WARNING_LABEL).replaceAll("%",
                            mRes.getString(R.string.APP_ACTIVITY_REFUEL).toLowerCase()) + "\n");
                }
                setEditable((ViewGroup) findViewById(R.id.genRootViewGroup), false);
            }

        }
        else {
            warningLabel.setText("");
            mCarId = mPreferences.getLong("CurrentCar_ID", -1);
            mDriverId = mPreferences.getLong("CurrentDriver_ID", -1);
            mCurrencyId = mPreferences.getLong("CarCurrency_ID", -1);
            initDateTime(System.currentTimeMillis());
            setEditable((ViewGroup) findViewById(R.id.genRootViewGroup), true);
        }
        
        initSpinner(carSpinner, MainDbAdapter.CAR_TABLE_NAME, MainDbAdapter.genColName,
                new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, MainDbAdapter.isActiveCondition, MainDbAdapter.GEN_COL_NAME_NAME,
                mCarId, false);

        initSpinner(driverSpinner, MainDbAdapter.DRIVER_TABLE_NAME, MainDbAdapter.genColName,
                new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, MainDbAdapter.isActiveCondition, MainDbAdapter.GEN_COL_NAME_NAME,
                mDriverId, false);

        initSpinner((Spinner)findViewById(R.id.expenseTypeSpinner), MainDbAdapter.EXPENSETYPE_TABLE_NAME, MainDbAdapter.genColName,
                new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, MainDbAdapter.isActiveCondition, MainDbAdapter.GEN_COL_NAME_NAME,
                mExpTypeId, false);

        initSpinner((Spinner)findViewById(R.id.expenseCategorySpinner), MainDbAdapter.EXPENSECATEGORY_TABLE_NAME, MainDbAdapter.genColName,
                new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, MainDbAdapter.isActiveCondition, MainDbAdapter.GEN_COL_NAME_NAME,
                mExpCategoryId, false);

        initSpinner((Spinner) findViewById( R.id.currencySpinner ), MainDbAdapter.CURRENCY_TABLE_NAME,
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
                                "Expense");
                        data.put( MainDbAdapter.GEN_COL_ISACTIVE_NAME, "Y");
                        data.put( MainDbAdapter.GEN_COL_USER_COMMENT_NAME,
                                userComment.getText().toString() );
                        data.put( MainDbAdapter.EXPENSES_COL_CAR_ID_NAME,
                                carSpinner.getSelectedItemId() );
                        data.put( MainDbAdapter.EXPENSES_COL_DRIVER_ID_NAME,
                                driverSpinner.getSelectedItemId() );
                        data.put( MainDbAdapter.EXPENSES_COL_EXPENSECATEGORY_ID_NAME,
                                ((Spinner) findViewById( R.id.expenseCategorySpinner )).getSelectedItemId() );
                        data.put( MainDbAdapter.EXPENSES_COL_EXPENSETYPE_ID_NAME,
                                ((Spinner) findViewById( R.id.expenseTypeSpinner )).getSelectedItemId() );
                        data.put( MainDbAdapter.EXPENSES_COL_INDEX_NAME, carIndexEntry.getText().toString());
                        data.put( MainDbAdapter.EXPENSES_COL_AMOUNT_NAME, amountEntry.getText().toString());
                        data.put( MainDbAdapter.EXPENSES_COL_CURRENCY_ID_NAME,
                                ((Spinner) findViewById( R.id.currencySpinner )).getSelectedItemId() );
                        data.put( MainDbAdapter.EXPENSES_COL_DATE_NAME, mDateTimeInSeconds);
                        data.put( MainDbAdapter.EXPENSES_COL_DOCUMENTNO_NAME,
                                docNo.getText().toString());

                        if( operationType.equals("N") ) {
                            if(mMainDbAdapter.createRecord(MainDbAdapter.EXPENSES_TABLE_NAME, data) < 0){
                                errorAlertBuilder.setMessage(mMainDbAdapter.lastErrorMessage);
                                errorAlert = errorAlertBuilder.create();
                                errorAlert.show();
                            }
                            else
                                finish();
                        }
                        else {
                            int updResult = mMainDbAdapter.updateRecord(MainDbAdapter.EXPENSES_TABLE_NAME, mRowId, data);
                            if(updResult != -1){
                                String errMsg = "";
                                errMsg = mRes.getString(updResult);
                                if(updResult == R.string.ERR_000)
                                    errMsg = errMsg + "\n" + mMainDbAdapter.lastErrorMessage;
                                errorAlertBuilder.setMessage(errMsg);
                                errorAlert = errorAlertBuilder.create();
                                errorAlert.show();
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
                    userCommentAdapter = new ArrayAdapter<String>(ExpenseEditActivity.this,
                            android.R.layout.simple_dropdown_item_1line,
                            mMainDbAdapter.getAutoCompleteUserComments(MainDbAdapter.EXPENSES_TABLE_NAME, carSpinner.getSelectedItemId(), 30));
                    userComment.setAdapter(userCommentAdapter);
                }
                public void onNothingSelected(AdapterView<?> arg0) {
                }
            };
}
