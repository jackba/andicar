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
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
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

    EditText currencyRateEntry = null;
    TextView currencyInverseRateValue = null;
    Spinner currencyFromSpinner = null;
    Spinner currencyToSpinner = null;
    BigDecimal rate = null;
    BigDecimal inverseRate = null;
    /** Called when the activity is first created. */
    @Override
    public void onCreate( Bundle icicle )
    {
        super.onCreate( icicle, R.layout.currencyrate_edit_activity, mOkClickListener );

        String operation = extras.getString("Operation"); //E = edit, N = new
        long fromCurrId = 1;
        long toCurrId = 2;

        currencyFromSpinner = (Spinner) findViewById( R.id.currencyFromSpinner );
        currencyToSpinner = (Spinner) findViewById( R.id.currencyToSpinner );
        currencyRateEntry = (EditText)findViewById(R.id.currencyRateEntry);
        currencyRateEntry.setOnKeyListener(currencyRateEntryInputEntryOnKeyListener);
        currencyInverseRateValue = (TextView)findViewById(R.id.currencyInverseRateValue);
        
        if( operation.equals( "E") ) {
            mRowId = extras.getLong( MainDbAdapter.GEN_COL_ROWID_NAME );
            Cursor recordCursor = mMainDbAdapter.fetchRecord(MainDbAdapter.CURRENCYRATE_TABLE_NAME,
                    MainDbAdapter.currencyRateTableColNames, mRowId);
            fromCurrId = recordCursor.getLong( MainDbAdapter.CURRENCYRATE_COL_FROMCURRENCY_ID_POS );
            toCurrId = recordCursor.getLong( MainDbAdapter.CURRENCYRATE_COL_TOCURRENCY_ID_POS );
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
                MainDbAdapter.isActiveCondition, MainDbAdapter.CURRENCY_COL_CODE_NAME, fromCurrId, false);

        initSpinner(currencyToSpinner, MainDbAdapter.CURRENCY_TABLE_NAME,
                MainDbAdapter.currencyTableColNames, new String[]{MainDbAdapter.CURRENCY_COL_CODE_NAME},
                MainDbAdapter.isActiveCondition, MainDbAdapter.CURRENCY_COL_CODE_NAME, toCurrId, false);
    }

    @Override
    protected void onResume() {
        super.onResume();
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
                                currFromCode + " -> " + currToCode);
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

    private View.OnKeyListener currencyRateEntryInputEntryOnKeyListener =
            new View.OnKeyListener() {
                    public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
                        if(arg2.getAction() != KeyEvent.ACTION_UP) {
                            return false;
                        }
                        calculateInverseRate();
                        return false;
                    }
                };
                
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
