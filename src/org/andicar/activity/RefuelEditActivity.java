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
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;
import java.sql.Timestamp;
import org.andicar.persistence.MainDbAdapter;
import org.andicar.utils.Constants;
import android.view.View;
import android.widget.EditText;

/**
 *
 * @author miki
 */
public class RefuelEditActivity extends EditActivityBase {

    AlertDialog.Builder insertUpdateErrorAlertBuilder;
    AlertDialog insertUpdateErrorAlert;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle, R.layout.refuel_edit_activity, mOkClickListener);
        insertUpdateErrorAlertBuilder = new AlertDialog.Builder( this );
        insertUpdateErrorAlertBuilder.setCancelable( false );
        insertUpdateErrorAlertBuilder.setPositiveButton( mRes.getString(R.string.GEN_OK), null );

        if (extras != null) {

        } else {
            ((CheckBox) findViewById(R.id.genIsActiveCheck)).setChecked(true);
        }

        initDateTime(System.currentTimeMillis());

        initSpinner(findViewById(R.id.refuelEditCarSpinner), MainDbAdapter.CAR_TABLE_NAME, MainDbAdapter.genColName,
                new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, MainDbAdapter.isActiveCondition, MainDbAdapter.GEN_COL_NAME_NAME,
                mPreferences.getLong("CurrentCar_ID", -1), false);

        initSpinner(findViewById(R.id.refuelEditDriverSpinner), MainDbAdapter.DRIVER_TABLE_NAME, MainDbAdapter.genColName,
                new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, MainDbAdapter.isActiveCondition, MainDbAdapter.GEN_COL_NAME_NAME,
                mPreferences.getLong("CurrentDriver_ID", -1), false);

        initSpinner((Spinner)findViewById(R.id.refuelEditExpenseTypeSpinner), MainDbAdapter.EXPENSETYPE_TABLE_NAME, MainDbAdapter.genColName,
                new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, MainDbAdapter.isActiveCondition, MainDbAdapter.GEN_COL_NAME_NAME,
                mPreferences.getLong("RefuelExpenseType_ID", -1), true);

        initSpinner((Spinner) findViewById( R.id.refuelEditUOMVolumeSpinner ), MainDbAdapter.UOM_TABLE_NAME,
                MainDbAdapter.uomTableColNames, new String[]{MainDbAdapter.UOM_COL_CODE_NAME},
                MainDbAdapter.UOM_COL_UOMTYPE_NAME + "='" + Constants.UOM_VOLUME_TYPE_CODE + "'" +
                    MainDbAdapter.isActiveWithAndCondition, MainDbAdapter.UOM_COL_CODE_NAME, mPreferences.getLong("CarUOMVolume_ID", -1), false);
        initSpinner((Spinner) findViewById( R.id.refuelEditCurrencySpinner ), MainDbAdapter.CURRENCY_TABLE_NAME,
                MainDbAdapter.currencyTableColNames, new String[]{MainDbAdapter.CURRENCY_COL_CODE_NAME},
                    MainDbAdapter.isActiveCondition,
                    MainDbAdapter.CURRENCY_COL_CODE_NAME, mPreferences.getLong("CarCurrency_ID", -1), false);
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
                                ((EditText) findViewById( R.id.genUserCommentEntry )).getText().toString() );
                        data.put( MainDbAdapter.REFUEL_COL_CAR_ID_NAME,
                                ((Spinner) findViewById( R.id.refuelEditCarSpinner )).getSelectedItemId() );
                        data.put( MainDbAdapter.REFUEL_COL_DRIVER_ID_NAME,
                                ((Spinner) findViewById( R.id.refuelEditDriverSpinner )).getSelectedItemId() );
                        data.put( MainDbAdapter.REFUEL_COL_EXPENSETYPE_ID_NAME,
                                ((Spinner) findViewById( R.id.refuelEditExpenseTypeSpinner )).getSelectedItemId() );

                        floatValStr = ((EditText) findViewById( R.id.refuelEditIndexEntry )).getText().toString();
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
                        floatValStr = ((EditText) findViewById( R.id.refuelEditQuantityEntry )).getText().toString();
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
                        floatValStr = ((EditText) findViewById( R.id.refuelEditPriceEntry )).getText().toString();
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
                        data.put( MainDbAdapter.REFUEL_COL_DATE_NAME, mDateTime);
                        data.put( MainDbAdapter.REFUEL_COL_DOCUMENTNO_NAME,
                                ((EditText) findViewById( R.id.refuelEditDocumentNoEntry )).getText().toString());

                        if( mRowId == null ) {
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
}
