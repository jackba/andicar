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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import java.math.BigDecimal;
import org.andicar.persistence.MainDbAdapter;
import org.andicar.utils.StaticValues;

/**
 *
 * @author miki
 */
public class CarEditActivity extends EditActivityBase
{
//    private CarDbAdapter mCarDbHelper = null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate( Bundle icicle )
    {
        super.onCreate( icicle, R.layout.car_edit_activity, mOkClickListener );


        if( extras != null ) {
            mRowId = extras.getLong( MainDbAdapter.GEN_COL_ROWID_NAME );
            Cursor recordCursor = mMainDbAdapter.fetchRecord(MainDbAdapter.CAR_TABLE_NAME, MainDbAdapter.carTableColNames, mRowId);
            String name = recordCursor.getString( MainDbAdapter.GEN_COL_NAME_POS );
            String isActive = recordCursor.getString( MainDbAdapter.GEN_COL_ISACTIVE_POS );
            String userComment = recordCursor.getString( MainDbAdapter.GEN_COL_USER_COMMENT_POS );
            String model = recordCursor.getString( MainDbAdapter.CAR_COL_MODEL_POS );
            String registrationNo = recordCursor.getString( MainDbAdapter.CAR_COL_REGISTRATIONNO_POS );
            BigDecimal startIndex = new BigDecimal(recordCursor.getString( MainDbAdapter.CAR_COL_INDEXSTART_POS ));
            Long uomLengthId = recordCursor.getLong( MainDbAdapter.CAR_COL_UOMLENGTH_ID_POS );
            Long uomVolumeId = recordCursor.getLong( MainDbAdapter.CAR_COL_UOMVOLUME_ID_POS );
            Long currencyId = recordCursor.getLong( MainDbAdapter.CAR_COL_CURRENCY_ID_POS );
            //uom for length
            initSpinner((Spinner) findViewById( R.id.carEditUomLengthSpinner ), MainDbAdapter.UOM_TABLE_NAME,
                    MainDbAdapter.genColName, new String[]{MainDbAdapter.GEN_COL_NAME_NAME},
                    MainDbAdapter.UOM_COL_UOMTYPE_NAME + "='" + StaticValues.UOM_LENGTH_TYPE_CODE + "'" +
                        MainDbAdapter.isActiveWithAndCondition, MainDbAdapter.UOM_COL_CODE_NAME, uomLengthId, false);
            //uom for volume
            initSpinner((Spinner) findViewById( R.id.carEditUomVolumeSpinner ), MainDbAdapter.UOM_TABLE_NAME,
                    MainDbAdapter.genColName, new String[]{MainDbAdapter.GEN_COL_NAME_NAME},
                    MainDbAdapter.UOM_COL_UOMTYPE_NAME + "='" + StaticValues.UOM_VOLUME_TYPE_CODE + "'" +
                        MainDbAdapter.isActiveWithAndCondition, MainDbAdapter.UOM_COL_CODE_NAME, uomVolumeId, false);
             //default currency
            initSpinner((Spinner) findViewById( R.id.carEditCurrencySpinner ), MainDbAdapter.CURRENCY_TABLE_NAME,
                    MainDbAdapter.genColName, new String[]{MainDbAdapter.GEN_COL_NAME_NAME},
                    MainDbAdapter.isActiveCondition, MainDbAdapter.CURRENCY_COL_CODE_NAME, currencyId, false);

            if( name != null ) {
                ((EditText) findViewById( R.id.genNameEntry )).setText( name );
            }
            if( model != null ) {
                ((EditText) findViewById( R.id.carEditCarModelEntry )).setText( model );
            }
            if( registrationNo != null ) {
                ((EditText) findViewById( R.id.carEditCarRegNoEntry )).setText( registrationNo );
            }
            if( isActive != null ) {
                ((CheckBox) findViewById( R.id.genIsActiveCheck )).setChecked( isActive.equals( "Y" ) );
            }
            if( userComment != null ) {
                ((EditText) findViewById( R.id.genUserCommentEntry )).setText( userComment );
            }
            if( startIndex != null ) {
                ((EditText) findViewById( R.id.carEditCarStartIndexEntry )).setText( startIndex.toString() );
            }

            //cannot be inactivated if is the current car
            if( extras.getLong( "CurrentCar_ID" ) == mRowId ) {
                CheckBox cb = (CheckBox) findViewById( R.id.genIsActiveCheck );
                cb.setClickable( false );
                cb.setOnTouchListener( new View.OnTouchListener()
                {
                    public boolean onTouch( View arg0, MotionEvent arg1 )
                    {
                        Toast toast = Toast.makeText( getApplicationContext(),
                                mRes.getString( R.string.CURRENT_CAR_INACTIVATE_ERROR_MESSAGE ), Toast.LENGTH_SHORT );
                        toast.show();
                        return false;
                    }
                } );
            }
        }
        else {
            initSpinner((Spinner) findViewById( R.id.carEditUomLengthSpinner ), MainDbAdapter.UOM_TABLE_NAME,
                    MainDbAdapter.genColName, new String[]{MainDbAdapter.GEN_COL_NAME_NAME},
                    MainDbAdapter.UOM_COL_UOMTYPE_NAME + "='" + StaticValues.UOM_LENGTH_TYPE_CODE + "'" +
                        MainDbAdapter.isActiveWithAndCondition, MainDbAdapter.UOM_COL_CODE_NAME, 1, false);
            //uom for volume
            initSpinner((Spinner) findViewById( R.id.carEditUomVolumeSpinner ), MainDbAdapter.UOM_TABLE_NAME,
                    MainDbAdapter.genColName, new String[]{MainDbAdapter.GEN_COL_NAME_NAME},
                    MainDbAdapter.UOM_COL_UOMTYPE_NAME + "='" + StaticValues.UOM_VOLUME_TYPE_CODE + "'" +
                        MainDbAdapter.isActiveWithAndCondition, MainDbAdapter.UOM_COL_CODE_NAME, 3, false);
             //default currency
            initSpinner((Spinner) findViewById( R.id.carEditCurrencySpinner ), MainDbAdapter.CURRENCY_TABLE_NAME,
                    MainDbAdapter.genColName, new String[]{MainDbAdapter.GEN_COL_NAME_NAME},
                    MainDbAdapter.isActiveCondition, MainDbAdapter.CURRENCY_COL_CODE_NAME, 1, false);

            ((CheckBox) findViewById( R.id.genIsActiveCheck )).setChecked( true );
        }
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

                        BigDecimal startIndex = null;
                        String startIndexStr = ((EditText) findViewById( R.id.carEditCarStartIndexEntry )).getText().toString();
                        if( startIndexStr != null && startIndexStr.length() > 0 ) {
                            try {
                                startIndex = new BigDecimal( startIndexStr );
                            }
                            catch( NumberFormatException e ) {
                                Toast toast = Toast.makeText( getApplicationContext(),
                                        mRes.getString( R.string.GEN_NUMBER_FORMAT_EXCEPTION ) + ": "
                                        + mRes.getString( R.string.CAR_EDIT_ACTIVITY_STARTINDEX ), Toast.LENGTH_SHORT );
                                toast.show();
                                return;
                            }
                        }
                        ContentValues data = new ContentValues();
                        data.put( MainDbAdapter.GEN_COL_NAME_NAME,
                                ((EditText) findViewById( R.id.genNameEntry )).getText().toString());
                        data.put( MainDbAdapter.GEN_COL_ISACTIVE_NAME,
                                (((CheckBox) findViewById( R.id.genIsActiveCheck )).isChecked() ? "Y" : "N") );
                        data.put( MainDbAdapter.GEN_COL_USER_COMMENT_NAME,
                                ((EditText) findViewById( R.id.genUserCommentEntry )).getText().toString() );
                        data.put( MainDbAdapter.CAR_COL_MODEL_NAME,
                                ((EditText) findViewById( R.id.carEditCarModelEntry )).getText().toString() );
                        data.put( MainDbAdapter.CAR_COL_REGISTRATIONNO_NAME,
                                ((EditText) findViewById( R.id.carEditCarRegNoEntry )).getText().toString());
                        data.put( MainDbAdapter.CAR_COL_INDEXSTART_NAME, startIndex.toString() );
                        //when a new car defined the current index is same with the start index
                        data.put( MainDbAdapter.CAR_COL_INDEXCURRENT_NAME, startIndex.toString() );
                        data.put( MainDbAdapter.CAR_COL_UOMLENGTH_ID_NAME,
                                ((Spinner) findViewById( R.id.carEditUomLengthSpinner )).getSelectedItemId() );
                        data.put( MainDbAdapter.CAR_COL_UOMVOLUME_ID_NAME,
                                ((Spinner) findViewById( R.id.carEditUomVolumeSpinner )).getSelectedItemId() );
                        data.put( MainDbAdapter.CAR_COL_CURRENCY_ID_NAME,
                                ((Spinner) findViewById( R.id.carEditCurrencySpinner )).getSelectedItemId());

                        if( mRowId == null ) {
                            mMainDbAdapter.createRecord(MainDbAdapter.CAR_TABLE_NAME, data);
                            finish();
                        }
                        else {
                            int updResult = mMainDbAdapter.updateRecord(MainDbAdapter.CAR_TABLE_NAME, mRowId, data);
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

}
