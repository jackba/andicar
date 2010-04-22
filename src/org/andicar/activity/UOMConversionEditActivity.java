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
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import org.andicar.persistence.MainDbAdapter;

/**
 *
 * @author miki
 */
public class UOMConversionEditActivity extends EditActivityBase {

    private String uomFromType = "";
    private long uomFromId = -1;
    private long uomToId = -1;
    private Spinner spnUomFrom;
    private Spinner spnUomTo;
    private EditText etName;
    private EditText etUserComment;
    private EditText etConversionRate;
    private CheckBox ckIsActive;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle, R.layout.uomconversion_edit_activity, mOkClickListener);
        
        spnUomFrom = (Spinner) findViewById( R.id.spnUomFrom );
        spnUomFrom.setOnItemSelectedListener(uomFromSelectedListener);
        spnUomTo = (Spinner) findViewById( R.id.spnUomTo );
        etName = (EditText) findViewById(R.id.etName);
        etUserComment = (EditText) findViewById( R.id.etUserComment );
        etConversionRate = (EditText) findViewById( R.id.etConversionRate );
        ckIsActive = (CheckBox) findViewById(R.id.ckIsActive);

        String operation = mbundleExtras.getString("Operation"); //E = edit, N = new

        if( operation.equals( "E") ) {
            mRowId = mbundleExtras.getLong( MainDbAdapter.GEN_COL_ROWID_NAME );
            Cursor recordCursor = mDbAdapter.fetchRecord(MainDbAdapter.UOM_CONVERSION_TABLE_NAME,
                    MainDbAdapter.uomConversionTableColNames, mRowId);
            String name = recordCursor.getString( MainDbAdapter.GEN_COL_NAME_POS );
            String isActive = recordCursor.getString( MainDbAdapter.GEN_COL_ISACTIVE_POS );
            String userComment = recordCursor.getString( MainDbAdapter.GEN_COL_USER_COMMENT_POS );
            String conversionRate = recordCursor.getString( MainDbAdapter.UOM_CONVERSION_COL_RATE_POS );
            uomFromId = recordCursor.getLong( MainDbAdapter.UOM_CONVERSION_COL_UOMFROM_ID_POS );
            uomToId = recordCursor.getLong( MainDbAdapter.UOM_CONVERSION_COL_UOMTO_ID_POS );

            if (name != null) {
                etName.setText(name);
            }
            if (isActive != null) {
                ckIsActive.setChecked(isActive.equals("Y"));
            }
            if (userComment != null) {
                etUserComment.setText( userComment );
            }

            initSpinner(spnUomFrom, MainDbAdapter.UOM_TABLE_NAME,
                    MainDbAdapter.genColName, new String[]{MainDbAdapter.GEN_COL_NAME_NAME},
                        MainDbAdapter.isActiveCondition,
                        MainDbAdapter.UOM_COL_CODE_NAME, uomFromId, false);

            uomFromType = mDbAdapter.fetchRecord(MainDbAdapter.UOM_TABLE_NAME, MainDbAdapter.uomTableColNames, uomFromId)
                            .getString(MainDbAdapter.UOM_COL_UOMTYPE_POS);

            initSpinner(spnUomTo, MainDbAdapter.UOM_TABLE_NAME,
                    MainDbAdapter.genColName, new String[]{MainDbAdapter.GEN_COL_NAME_NAME},
                        MainDbAdapter.UOM_COL_UOMTYPE_NAME + "='" + uomFromType + "' " +
                        " AND " + MainDbAdapter.GEN_COL_ROWID_NAME + " <> " + uomFromId +
                        MainDbAdapter.isActiveWithAndCondition, MainDbAdapter.UOM_COL_CODE_NAME, uomToId, false);
            if (conversionRate != null) {
                etConversionRate.setText( conversionRate.toString() );
            }

        } else {
            initSpinner(spnUomFrom, MainDbAdapter.UOM_TABLE_NAME,
                    MainDbAdapter.genColName, new String[]{MainDbAdapter.GEN_COL_NAME_NAME},
                        MainDbAdapter.isActiveCondition, MainDbAdapter.UOM_COL_CODE_NAME, -1, false);
            ckIsActive.setChecked(true);
        }

    }

    private View.OnClickListener mOkClickListener =
                new View.OnClickListener() {

                    public void onClick(View v) {
                        //check mandatory fields
                        String convRateStr = etConversionRate.getText().toString();
                        String retVal = checkMandatory((ViewGroup) findViewById(R.id.vgRoot));
                        if( retVal != null ) {
                            Toast toast = Toast.makeText( getApplicationContext(),
                                    mResource.getString( R.string.GEN_FILL_MANDATORY ) + ": " + retVal, Toast.LENGTH_SHORT );
                            toast.show();
                            return;
                        }

                        long fromId = spnUomFrom.getSelectedItemId();
                        long toId = spnUomTo.getSelectedItemId();
                        retVal = null;
                        int retVal2 = mDbAdapter.canInsertUpdateUOMConversion(mRowId, fromId, toId);
                        if(retVal2 != -1){
                            madbErrorAlert.setMessage(mResource.getString(retVal2));
                            madError = madbErrorAlert.create();
                            madError.show();
                            return;
                        }

                        ContentValues data = new ContentValues();
                        data.put( MainDbAdapter.GEN_COL_NAME_NAME,
                                etName.getText().toString());
                        data.put( MainDbAdapter.GEN_COL_ISACTIVE_NAME,
                                (ckIsActive.isChecked() ? "Y" : "N") );
                        data.put( MainDbAdapter.GEN_COL_USER_COMMENT_NAME,
                                etUserComment.getText().toString() );
                        data.put( MainDbAdapter.UOM_CONVERSION_COL_UOMFROM_ID_NAME, fromId);
                        data.put( MainDbAdapter.UOM_CONVERSION_COL_UOMTO_ID_NAME, toId);
                        data.put( MainDbAdapter.UOM_CONVERSION_COL_RATE_NAME, convRateStr);

                        if( mRowId == null ) {
                            mDbAdapter.createRecord(MainDbAdapter.UOM_CONVERSION_TABLE_NAME, data);
                            finish();
                        }
                        else {
                            int updResult = mDbAdapter.updateRecord(MainDbAdapter.UOM_CONVERSION_TABLE_NAME, mRowId, data);
                            if(updResult != -1){
                                String errMsg = "";
                                errMsg = mResource.getString(updResult);
                                if(updResult == R.string.ERR_000)
                                    errMsg = errMsg + "\n" + mDbAdapter.lastErrorMessage;
                                madbErrorAlert.setMessage(errMsg);
                                madError = madbErrorAlert.create();
                                madError.show();
                            }
                            else
                                finish();
                        }
                    }
                };

    private OnItemSelectedListener uomFromSelectedListener =
                new OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                        uomFromType = mDbAdapter.fetchRecord(MainDbAdapter.UOM_TABLE_NAME, MainDbAdapter.uomTableColNames, arg3)
                            .getString(MainDbAdapter.UOM_COL_UOMTYPE_POS);

                        initSpinner(spnUomTo, MainDbAdapter.UOM_TABLE_NAME,
                                MainDbAdapter.genColName, new String[]{MainDbAdapter.GEN_COL_NAME_NAME},
                                    MainDbAdapter.UOM_COL_UOMTYPE_NAME + "='" + uomFromType + "' " +
                                    " AND " + MainDbAdapter.GEN_COL_ROWID_NAME + " <> " + arg3 +
                                    MainDbAdapter.isActiveWithAndCondition, MainDbAdapter.UOM_COL_CODE_NAME, uomToId, false);
                    }
                    public void onNothingSelected(AdapterView<?> arg0) {
                    }
                };

}
