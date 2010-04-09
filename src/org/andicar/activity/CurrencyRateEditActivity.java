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
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.andicar.persistence.MainDbAdapter;
import org.andicar.utils.StaticValues;

/**
 *
 * @author miki
 */
public class CurrencyRateEditActivity extends EditActivityBase
{

    private EditText currencyRateEntry = null;
    private TextView currencyInverseRateLabel = null;
    private TextView currencyInverseRateToLabel = null;
    private TextView currencyInverseRateValue = null;
    private TextView currencyRateLabel = null;
    private TextView currencyRateToLabel = null;
    private Spinner currencyFromSpinner = null;
    private Spinner currencyToSpinner = null;
    private BigDecimal rate = null;
    private BigDecimal inverseRate = null;
    private String currencyFromCode = null;
    private String currencyToCode = null;
    private long currencyFromId = 1;
    private long currencyToId = 2;

    /** Called when the activity is first created. */
    @Override
    public void onCreate( Bundle icicle )
    {
        super.onCreate( icicle, R.layout.currencyrate_edit_activity, mOkClickListener );

        String operation = extras.getString("Operation"); //E = edit, N = new

        currencyFromSpinner = (Spinner) findViewById( R.id.currencyFromSpinner );
        currencyFromSpinner.setOnItemSelectedListener(spinnerCurrencyFromOnItemSelectedListener);
        currencyToSpinner = (Spinner) findViewById( R.id.currencyToSpinner );
        currencyToSpinner.setOnItemSelectedListener(spinnerCurrencyToOnItemSelectedListener);
        currencyRateEntry = (EditText)findViewById(R.id.currencyRateEntry);
        currencyRateEntry.addTextChangedListener(textWatcher);
        currencyInverseRateLabel = (TextView)findViewById(R.id.currencyInverseRateLabel);
        currencyInverseRateValue = (TextView)findViewById(R.id.currencyInverseRateValue);
        currencyInverseRateToLabel = (TextView)findViewById(R.id.currencyInverseRateToLabel);
        currencyRateLabel = (TextView)findViewById(R.id.currencyRateLabel);
        currencyRateToLabel = (TextView)findViewById(R.id.currencyRateToLabel);
        
        if( operation.equals( "E") ) {
            mRowId = extras.getLong( MainDbAdapter.GEN_COL_ROWID_NAME );
            Cursor recordCursor = mMainDbAdapter.fetchRecord(MainDbAdapter.CURRENCYRATE_TABLE_NAME,
                    MainDbAdapter.currencyRateTableColNames, mRowId);
            currencyFromId = recordCursor.getLong( MainDbAdapter.CURRENCYRATE_COL_FROMCURRENCY_ID_POS );
            currencyToId = recordCursor.getLong( MainDbAdapter.CURRENCYRATE_COL_TOCURRENCY_ID_POS );
            String rateStr = recordCursor.getString( MainDbAdapter.CURRENCYRATE_COL_RATE_POS );
            String inverseRateStr = recordCursor.getString( MainDbAdapter.CURRENCYRATE_COL_INVERSERATE_POS );
            String isActive = recordCursor.getString( MainDbAdapter.GEN_COL_ISACTIVE_POS );
            String userComment = recordCursor.getString( MainDbAdapter.GEN_COL_USER_COMMENT_POS );
            currencyFromSpinner.setEnabled(false);
            currencyToSpinner.setEnabled(false);

            if(rateStr != null){
              currencyRateEntry.setText(rateStr);
            }
            if(inverseRateStr != null){
              currencyInverseRateValue.setText(inverseRateStr);
            }
            if( isActive != null ) {
                ((CheckBox) findViewById( R.id.genIsActiveCheck )).setChecked( isActive.equals( "Y" ) );
            }
            if( userComment != null ) {
                ((EditText) findViewById( R.id.genUserCommentEntry )).setText( userComment );
            }
        }
        else {
            ((CheckBox) findViewById( R.id.genIsActiveCheck )).setChecked( true );
            currencyFromSpinner.setEnabled(true);
            currencyToSpinner.setEnabled(true);
        }

        initSpinner(currencyFromSpinner, MainDbAdapter.CURRENCY_TABLE_NAME,
                MainDbAdapter.currencyTableColNames, new String[]{MainDbAdapter.CURRENCY_COL_CODE_NAME},
                MainDbAdapter.isActiveCondition, MainDbAdapter.CURRENCY_COL_CODE_NAME, currencyFromId, false);

        initSpinner(currencyToSpinner, MainDbAdapter.CURRENCY_TABLE_NAME,
                MainDbAdapter.currencyTableColNames, new String[]{MainDbAdapter.CURRENCY_COL_CODE_NAME},
                MainDbAdapter.isActiveCondition, MainDbAdapter.CURRENCY_COL_CODE_NAME, currencyToId, false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateLabel();
        calculateInverseRate();
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
                        String currFromCode = mMainDbAdapter.fetchRecord(MainDbAdapter.CURRENCY_TABLE_NAME,
                                MainDbAdapter.currencyTableColNames, currencyFromSpinner.getSelectedItemId())
                                    .getString(MainDbAdapter.CURRENCY_COL_CODE_POS);
                        String currToCode = mMainDbAdapter.fetchRecord(MainDbAdapter.CURRENCY_TABLE_NAME,
                                MainDbAdapter.currencyTableColNames, currencyToSpinner.getSelectedItemId())
                                    .getString(MainDbAdapter.CURRENCY_COL_CODE_POS);
                        data.put( MainDbAdapter.GEN_COL_NAME_NAME,
                                currFromCode + " <-> " + currToCode);
                        data.put( MainDbAdapter.GEN_COL_ISACTIVE_NAME,
                                (((CheckBox) findViewById( R.id.genIsActiveCheck )).isChecked() ? "Y" : "N") );
                        data.put( MainDbAdapter.GEN_COL_USER_COMMENT_NAME,
                                ((EditText) findViewById( R.id.genUserCommentEntry )).getText().toString() );
                        data.put( MainDbAdapter.CURRENCYRATE_COL_FROMCURRENCY_ID_NAME,
                                currencyFromSpinner.getSelectedItemId());
                        data.put( MainDbAdapter.CURRENCYRATE_COL_TOCURRENCY_ID_NAME,
                                currencyToSpinner.getSelectedItemId());
                        data.put( MainDbAdapter.CURRENCYRATE_COL_RATE_NAME,
                                rate.toString());
                        data.put( MainDbAdapter.CURRENCYRATE_COL_INVERSERATE_NAME,
                                inverseRate.toString());

                        if( mRowId == null ) {
                            Long createResult = mMainDbAdapter.createRecord(MainDbAdapter.CURRENCYRATE_TABLE_NAME, data);
                            if( createResult.intValue() < 0){
                                if(createResult.intValue() == -1) //DB Error
                                    errorAlertBuilder.setMessage(mMainDbAdapter.lastErrorMessage);
                                else //precondition error
                                    errorAlertBuilder.setMessage(mRes.getString(-1 * createResult.intValue()));
                                errorAlert = errorAlertBuilder.create();
                                errorAlert.show();
                                return;
                            }
                            finish();
                        }
                        else {
                            int updResult = mMainDbAdapter.updateRecord(MainDbAdapter.CURRENCYRATE_TABLE_NAME, mRowId, data);
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

    private TextWatcher textWatcher =
        new TextWatcher() {

            public void beforeTextChanged(CharSequence cs, int i, int i1, int i2) {
                return;
            }

            public void onTextChanged(CharSequence cs, int i, int i1, int i2) {
                return;
            }

            public void afterTextChanged(Editable edtbl) {
                calculateInverseRate();
            }
        };

    private AdapterView.OnItemSelectedListener spinnerCurrencyFromOnItemSelectedListener =
            new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    currencyFromId = arg3;
                    updateLabel();
                }
                public void onNothingSelected(AdapterView<?> arg0) {
                }
            };
                
    private AdapterView.OnItemSelectedListener spinnerCurrencyToOnItemSelectedListener =
            new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    currencyToId = arg3;
                    updateLabel();
                }
                public void onNothingSelected(AdapterView<?> arg0) {
                }
            };


    private void updateLabel(){
        currencyFromCode = mMainDbAdapter.fetchRecord(MainDbAdapter.CURRENCY_TABLE_NAME, MainDbAdapter.currencyTableColNames,
                            currencyFromId).getString(MainDbAdapter.CURRENCY_COL_CODE_POS);
        currencyToCode = mMainDbAdapter.fetchRecord(MainDbAdapter.CURRENCY_TABLE_NAME, MainDbAdapter.currencyTableColNames,
                currencyToId).getString(MainDbAdapter.CURRENCY_COL_CODE_POS);
        currencyRateLabel.setText(mRes.getString(R.string.CURRENCYRATE_EDIT_ACTIVITY_CURRENCYRATE_LABEL).replaceAll("%", currencyFromCode));
        currencyRateToLabel.setText(currencyToCode);

        currencyInverseRateLabel.setText(mRes.getString(R.string.CURRENCYRATE_EDIT_ACTIVITY_INVERSECURRENCYRATE_LABEL).replaceAll("%", currencyToCode));
        currencyInverseRateToLabel.setText(currencyFromCode);
    }

    private void calculateInverseRate() {
        String rateStr = currencyRateEntry.getText().toString();
        if(rateStr != null && rateStr.length() > 0) {
            rate = new BigDecimal(rateStr);
            if(rate.equals(BigDecimal.ZERO))
                inverseRate = rate;
            else
                inverseRate = BigDecimal.ONE.divide(rate, 10, RoundingMode.HALF_UP)
                        .setScale(StaticValues.conversionDecimals, StaticValues.conversionRoundingMode);
            if(rate.compareTo(rate.setScale(StaticValues.conversionDecimals, StaticValues.conversionRoundingMode)) != 0){
                rate = rate.setScale(StaticValues.conversionDecimals, StaticValues.conversionRoundingMode);
                currencyRateEntry.setText("");
                currencyRateEntry.append(rate.toString());
                Toast toast = Toast.makeText( getApplicationContext(),
                        mRes.getString( R.string.CURRENCYRATE_EDIT_ACTIVITY_MAXALLOWEDDECIMALS_LABEL ) +
                            StaticValues.conversionDecimals, Toast.LENGTH_SHORT );
                toast.show();
            }
            currencyInverseRateValue.setText(inverseRate.toString());
        }
    }
}
