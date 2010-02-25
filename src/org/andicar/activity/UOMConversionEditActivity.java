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
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import java.math.BigDecimal;
import org.andicar.persistence.MainDbAdapter;

/**
 *
 * @author miki
 */
public class UOMConversionEditActivity extends EditActivityBase {

    private String uomFromType = "";
    private AlertDialog.Builder uomConversionCannotSaveAlertBuilder;
    private AlertDialog uomConversionCannotSaveAlert;
    private long uomFromId = -1;
    private long uomToId = -1;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle, R.layout.uomconversion_edit_activity, mOkClickListener);
        
        uomConversionCannotSaveAlertBuilder = new AlertDialog.Builder( this );
        uomConversionCannotSaveAlertBuilder.setCancelable( false );
        uomConversionCannotSaveAlertBuilder.setPositiveButton( mRes.getString(R.string.GEN_OK), null );

        Spinner mUOMFromSpinner = (Spinner) findViewById( R.id.uomConversionEditUomFromSpinner );
        mUOMFromSpinner.setOnItemSelectedListener(uomFromSelectedListener);

        if (extras != null) {
            mRowId = extras.getLong( MainDbAdapter.GEN_COL_ROWID_NAME );
            Cursor recordCursor = mMainDbHelper.fetchRecord(MainDbAdapter.UOM_CONVERSION_TABLE_NAME,
                    MainDbAdapter.uomConversionTableColNames, mRowId);
            String name = recordCursor.getString( MainDbAdapter.GEN_COL_NAME_POS );
            String isActive = recordCursor.getString( MainDbAdapter.GEN_COL_ISACTIVE_POS );
            String userComment = recordCursor.getString( MainDbAdapter.GEN_COL_USER_COMMENT_POS );
            String conversionRate = recordCursor.getString( MainDbAdapter.UOM_CONVERSION_COL_RATE_POS );
            uomFromId = recordCursor.getLong( MainDbAdapter.UOM_CONVERSION_COL_UOMFROM_ID_POS );
            uomToId = recordCursor.getLong( MainDbAdapter.UOM_CONVERSION_COL_UOMTO_ID_POS );

            if (name != null) {
                ((EditText) findViewById(R.id.genNameEntry)).setText(name);
            }
            if (isActive != null) {
                ((CheckBox) findViewById(R.id.genIsActiveCheck)).setChecked(isActive.equals("Y"));
            }
            if (userComment != null) {
                ((EditText) findViewById( R.id.genUserCommentEntry )).setText( userComment );
            }

            initSpinner(mUOMFromSpinner, MainDbAdapter.UOM_TABLE_NAME,
                    MainDbAdapter.genColName, new String[]{MainDbAdapter.GEN_COL_NAME_NAME},
                        MainDbAdapter.isActiveCondition,
                        MainDbAdapter.UOM_COL_CODE_NAME, uomFromId, false);

            uomFromType = mMainDbHelper.fetchRecord(MainDbAdapter.UOM_TABLE_NAME, MainDbAdapter.uomTableColNames, uomFromId)
                            .getString(MainDbAdapter.UOM_COL_UOMTYPE_POS);

            initSpinner((Spinner) findViewById( R.id.uomConversionEditUomToSpinner ), MainDbAdapter.UOM_TABLE_NAME,
                    MainDbAdapter.genColName, new String[]{MainDbAdapter.GEN_COL_NAME_NAME},
                        MainDbAdapter.UOM_COL_UOMTYPE_NAME + "='" + uomFromType + "' " +
                        " AND " + MainDbAdapter.GEN_COL_ROWID_NAME + " <> " + uomFromId +
                        MainDbAdapter.isActiveWithAndCondition, MainDbAdapter.UOM_COL_CODE_NAME, uomToId, false);
            if (conversionRate != null) {
                ((EditText) findViewById( R.id.uomConversionEditRateEntry )).setText( conversionRate.toString() );
            }

        } else {
            initSpinner(mUOMFromSpinner, MainDbAdapter.UOM_TABLE_NAME,
                    MainDbAdapter.genColName, new String[]{MainDbAdapter.GEN_COL_NAME_NAME},
                        MainDbAdapter.isActiveCondition, MainDbAdapter.UOM_COL_CODE_NAME, -1, false);
            ((CheckBox) findViewById(R.id.genIsActiveCheck)).setChecked(true);
        }

    }

    private View.OnClickListener mOkClickListener =
                new View.OnClickListener() {

                    public void onClick(View v) {
                        //check mandatory fields
                        String convRateStr = ((EditText) findViewById(R.id.uomConversionEditRateEntry)).getText().toString();
                        String retVal = checkMandatory((ViewGroup) findViewById(R.id.genRootViewGroup));
                        if( retVal != null ) {
                            Toast toast = Toast.makeText( getApplicationContext(),
                                    mRes.getString( R.string.GEN_FILL_MANDATORY ) + ": " + retVal, Toast.LENGTH_SHORT );
                            toast.show();
                            return;
                        }

                        long fromId = ((Spinner) findViewById( R.id.uomConversionEditUomFromSpinner )).getSelectedItemId();
                        long toId = ((Spinner) findViewById( R.id.uomConversionEditUomToSpinner )).getSelectedItemId();
                        retVal = null;
                        retVal = mMainDbHelper.canInsertUpdateUOMConversion(mRowId, fromId, toId);
                        if(retVal != null){
                            uomConversionCannotSaveAlertBuilder.setMessage(mRes.getString(R.string.ERR_005));
                            uomConversionCannotSaveAlert = uomConversionCannotSaveAlertBuilder.create();
                            uomConversionCannotSaveAlert.show();
                            return;
                        }

                        ContentValues data = new ContentValues();
                        data.put( MainDbAdapter.GEN_COL_NAME_NAME,
                                ((EditText) findViewById(R.id.genNameEntry)).getText().toString());
                        data.put( MainDbAdapter.GEN_COL_ISACTIVE_NAME,
                                (((CheckBox) findViewById( R.id.genIsActiveCheck )).isChecked() ? "Y" : "N") );
                        data.put( MainDbAdapter.GEN_COL_USER_COMMENT_NAME,
                                ((EditText) findViewById( R.id.genUserCommentEntry )).getText().toString() );
                        data.put( MainDbAdapter.UOM_CONVERSION_COL_UOMFROM_ID_NAME, fromId);
                        data.put( MainDbAdapter.UOM_CONVERSION_COL_UOMTO_ID_NAME, toId);
                        data.put( MainDbAdapter.UOM_CONVERSION_COL_RATE_NAME, convRateStr);

                        if( mRowId == null ) {
                            mMainDbHelper.createRecord(MainDbAdapter.UOM_CONVERSION_TABLE_NAME, data);
                        }
                        else {
                            mMainDbHelper.updateRecord(MainDbAdapter.UOM_CONVERSION_TABLE_NAME, mRowId, data);
                        }
                        finish();
                    }
                };

    private OnItemSelectedListener uomFromSelectedListener =
                new OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                        uomFromType = mMainDbHelper.fetchRecord(MainDbAdapter.UOM_TABLE_NAME, MainDbAdapter.uomTableColNames, arg3)
                            .getString(MainDbAdapter.UOM_COL_UOMTYPE_POS);

                        initSpinner((Spinner) findViewById( R.id.uomConversionEditUomToSpinner ), MainDbAdapter.UOM_TABLE_NAME,
                                MainDbAdapter.genColName, new String[]{MainDbAdapter.GEN_COL_NAME_NAME},
                                    MainDbAdapter.UOM_COL_UOMTYPE_NAME + "='" + uomFromType + "' " +
                                    " AND " + MainDbAdapter.GEN_COL_ROWID_NAME + " <> " + arg3 +
                                    MainDbAdapter.isActiveWithAndCondition, MainDbAdapter.UOM_COL_CODE_NAME, uomToId, false);
                    }
                    public void onNothingSelected(AdapterView<?> arg0) {
                    }
                };

}
