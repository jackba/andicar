/*
 *  AndiCar - car management software for Android powered devices
 *  Copyright (C) 2010 Miklos Keresztes (miklos.keresztes@gmail.com)
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT AY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.andicar.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import org.andicar.persistence.MainDbAdapter;
import org.andicar.utils.AndiCarExceptionHandler;
import org.andicar.utils.StaticValues;

/**
 *
 * @author Miklos Keresztes
 */
public class BaseActivity extends Activity {
    protected MainDbAdapter mDbAdapter = null;
    protected Resources mResource = null;
    protected SharedPreferences mPreferences;
    protected AlertDialog.Builder madbErrorAlert;
    protected AlertDialog madError;
    protected boolean isSendStatistics = true;
    protected boolean isSendCrashReport;
    protected SharedPreferences.Editor mPrefEditor;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        mPreferences = getSharedPreferences(StaticValues.GLOBAL_PREFERENCE_NAME, 0);
        isSendStatistics = mPreferences.getBoolean("SendUsageStatistics", true);
        isSendCrashReport = mPreferences.getBoolean("SendCrashReport", true);
        if(isSendCrashReport)
            Thread.setDefaultUncaughtExceptionHandler(
                    new AndiCarExceptionHandler(Thread.getDefaultUncaughtExceptionHandler(), this));

        mPrefEditor = mPreferences.edit();
        mDbAdapter = new MainDbAdapter(this);
        mResource = getResources();

        madbErrorAlert = new AlertDialog.Builder( this );
        madbErrorAlert.setCancelable( false );
        madbErrorAlert.setPositiveButton( mResource.getString(R.string.GEN_OK), null );

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mDbAdapter != null){
            mDbAdapter.close();
            mDbAdapter = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(mDbAdapter == null)
            mDbAdapter = new MainDbAdapter(this);
    }

    protected void initSpinner(View pSpinner, String tableName, String[] columns, String[] from, String whereCondition,
            String orderBy, long selectedId, boolean addEmptyValue){
        try{
            Spinner spnCurrentSpinner = (Spinner) pSpinner;
            Cursor dbcRecordCursor;
            if(addEmptyValue){
                String selectSql = "SELECT -1 AS " + MainDbAdapter.GEN_COL_ROWID_NAME + ", " +
                                    "null AS " + MainDbAdapter.GEN_COL_NAME_NAME +
                                    " UNION " +
                                    " SELECT " + MainDbAdapter.GEN_COL_ROWID_NAME + ", " +
                                                MainDbAdapter.GEN_COL_NAME_NAME +
                                    " FROM " + tableName;
                if(whereCondition != null && whereCondition.length() > 0)
                    selectSql = selectSql + " WHERE " + whereCondition;
                selectSql = selectSql + " ORDER BY " + MainDbAdapter.GEN_COL_NAME_NAME;
                dbcRecordCursor = mDbAdapter.execSelectSql(selectSql);
            }
            else
                dbcRecordCursor = mDbAdapter.fetchForTable( tableName, columns, whereCondition, orderBy);
            
            startManagingCursor( dbcRecordCursor );
            int[] to = new int[]{android.R.id.text1};
            SimpleCursorAdapter scaCursorAdapter =
                    new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, dbcRecordCursor,
//                    new SimpleCursorAdapter(this, android.R.layout.two_line_list_item, dbcRecordCursor,
                    from, to);
            scaCursorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//            scaCursorAdapter.setDropDownViewResource(android.R.layout.two_line_list_item);
            spnCurrentSpinner.setAdapter(scaCursorAdapter);

            if(selectedId >= 0){
            //set the spinner to the last used id
                dbcRecordCursor.moveToFirst();
                for( int i = 0; i < dbcRecordCursor.getCount(); i++ ) {
                    if( dbcRecordCursor.getLong( MainDbAdapter.GEN_COL_ROWID_POS ) == selectedId) {
                        spnCurrentSpinner.setSelection( i );
                        break;
                    }
                    dbcRecordCursor.moveToNext();
                }
            }
            if(spnCurrentSpinner.getOnItemSelectedListener() == null)
                spnCurrentSpinner.setOnItemSelectedListener(spinnerOnItemSelectedListener);
        }
        catch(Exception e){
            madbErrorAlert.setMessage(e.getMessage());
            madError = madbErrorAlert.create();
            madError.show();
        }

    }

    protected void setSpinnerTextToCode(AdapterView<?> arg0, long arg3, View arg1) {
        if(arg1 == null)
            return;
        String code = null;
        //set the spinner text to the selected item code
        if ((((Spinner) arg0).equals(findViewById(R.id.spnUomFrom))
                || ((Spinner) arg0).equals(findViewById(R.id.spnUomLength))
                || ((Spinner) arg0).equals(findViewById(R.id.spnUomTo))
                || ((Spinner) arg0).equals(findViewById(R.id.spnUomVolume))
                )
                && arg3 > 0) {
            code = mDbAdapter.getUOMCode(arg3);
            if (code != null) {
                ((TextView) arg1).setText(code);
            }
        } else if ((((Spinner) arg0).equals(findViewById(R.id.spnCurrency))
                || ((Spinner) arg0).equals(findViewById(R.id.spnCurrencyFrom))
                || ((Spinner) arg0).equals(findViewById(R.id.spnCurrencyTo)))
                && arg3 > 0) {
            code = mDbAdapter.getCurrencyCode(arg3);
            if (code != null) {
                ((TextView) arg1).setText(code);
            }
        }
    }

    protected AdapterView.OnItemSelectedListener spinnerOnItemSelectedListener =
            new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    if(BaseActivity.this instanceof RefuelEditActivity){
                        if( ((Spinner)arg0).equals(findViewById(R.id.spnExpType))){
                            mPrefEditor.putLong("RefuelExpenseType_ID", arg3);
                            mPrefEditor.commit();
                        }
                        else if( ((Spinner)arg0).equals(findViewById(R.id.spnExpCategory))){
                            mPrefEditor.putLong("RefuelExpenseCategory_ID", arg3);
                            mPrefEditor.commit();
                        }
                    }
                    else if(BaseActivity.this instanceof MileageEditActivity){
                        if( ((Spinner)arg0).equals(findViewById(R.id.spnExpType))){
                            mPrefEditor.putLong("MileageInsertExpenseType_ID", arg3);
                            mPrefEditor.commit();
                        }
                    }
                    setSpinnerTextToCode(arg0, arg3, arg1);
                }
                
                public void onNothingSelected(AdapterView<?> arg0) {
                }
            };

}
