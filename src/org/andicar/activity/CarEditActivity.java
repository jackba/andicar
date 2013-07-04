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

import java.math.BigDecimal;

import org.andicar.persistence.MainDbAdapter;
import org.andicar.utils.StaticValues;
import org.andicar2.activity.R;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

/**
 *
 * @author miki
 */
public class CarEditActivity extends EditActivityBase
{
    private Spinner spnUomLength = null;
    private Spinner spnUomVolume = null;
    private Spinner spnCurrency = null;
    private EditText etName = null;
    private EditText etCarModel = null;
    private EditText etCarRegNo = null;
    private EditText etUserComment = null;
    private EditText etIndexStart = null;
    private CheckBox ckIsActive = null;
    private ImageButton btnNewCurrency = null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate( Bundle icicle )
    {
        super.onCreate(icicle);

        spnUomLength = (Spinner) findViewById( R.id.spnUomLength );
        spnUomVolume = (Spinner) findViewById( R.id.spnUomVolume );
        spnCurrency = (Spinner) findViewById( R.id.spnCurrency );
        etName = (EditText) findViewById( R.id.etName );
        etCarModel = (EditText) findViewById( R.id.etCarModel );
        etCarRegNo = (EditText) findViewById( R.id.etCarRegNo );
        etUserComment = (EditText) findViewById( R.id.etUserComment );
        etIndexStart = (EditText) findViewById( R.id.etIndexStart );
        ckIsActive = (CheckBox) findViewById( R.id.ckIsActive );
        btnNewCurrency = (ImageButton) findViewById( R.id.btnNewCurrency );
        btnNewCurrency.setOnClickListener(onNewCurrClickListener);
        
        String operation = mBundleExtras.getString("Operation"); //E = edit, N = new
        BigDecimal bdStartIndex = null;

        if( operation.equals( "E") ) {
            mRowId = mBundleExtras.getLong( MainDbAdapter.COL_NAME_GEN_ROWID );
            Cursor c = mDbAdapter.fetchRecord(MainDbAdapter.TABLE_NAME_CAR, MainDbAdapter.COL_LIST_CAR_TABLE, mRowId);
            String strName = c.getString( MainDbAdapter.COL_POS_GEN_NAME );
            String strIsActive = c.getString( MainDbAdapter.COL_POS_GEN_ISACTIVE );
            String strUserComment = c.getString( MainDbAdapter.COL_POS_GEN_USER_COMMENT );
            String strCarModel = c.getString( MainDbAdapter.COL_POS_CAR__MODEL );
            String strRegistrationNo = c.getString( MainDbAdapter.COL_POS_CAR__REGISTRATIONNO );
            try{
                bdStartIndex = new BigDecimal(c.getString( MainDbAdapter.COL_POS_CAR__INDEXSTART ));
            }
            catch(NumberFormatException e){};
            Long lUomLengthId = c.getLong( MainDbAdapter.COL_POS_CAR__UOMLENGTH_ID );
            Long lUomVolumeId = c.getLong( MainDbAdapter.COL_POS_CAR__UOMVOLUME_ID );
            Long lCurrencyId = c.getLong( MainDbAdapter.COL_POS_CAR__CURRENCY_ID );
            //uom for length
            initSpinner(spnUomLength, MainDbAdapter.TABLE_NAME_UOM,
                    MainDbAdapter.COL_LIST_GEN_ROWID_NAME, new String[]{MainDbAdapter.COL_NAME_GEN_NAME},
                    MainDbAdapter.COL_NAME_UOM__UOMTYPE + "='" + StaticValues.UOM_LENGTH_TYPE_CODE + "'" +
                        MainDbAdapter.WHERE_CONDITION_ISACTIVE_ANDPREFIX, null, MainDbAdapter.COL_NAME_GEN_NAME, lUomLengthId, false);
            //uom for volume
            initSpinner(spnUomVolume, MainDbAdapter.TABLE_NAME_UOM,
                    MainDbAdapter.COL_LIST_GEN_ROWID_NAME, new String[]{MainDbAdapter.COL_NAME_GEN_NAME},
                    MainDbAdapter.COL_NAME_UOM__UOMTYPE + "='" + StaticValues.UOM_VOLUME_TYPE_CODE + "'" +
                        MainDbAdapter.WHERE_CONDITION_ISACTIVE_ANDPREFIX, null, MainDbAdapter.COL_NAME_GEN_NAME, lUomVolumeId, false);
             //default currency
            initSpinner(spnCurrency, MainDbAdapter.TABLE_NAME_CURRENCY,
                    MainDbAdapter.COL_LIST_GEN_ROWID_NAME, new String[]{MainDbAdapter.COL_NAME_GEN_NAME},
                    MainDbAdapter.WHERE_CONDITION_ISACTIVE, null, MainDbAdapter.COL_NAME_GEN_NAME, lCurrencyId, false);

            if( strName != null ) {
                etName.setText( strName );
            }
            if( strCarModel != null ) {
                etCarModel.setText( strCarModel );
            }
            if( strRegistrationNo != null ) {
                etCarRegNo.setText( strRegistrationNo );
            }
            if( strIsActive != null ) {
                ckIsActive.setChecked( strIsActive.equals( "Y" ) );
            }
            if( strUserComment != null ) {
                etUserComment.setText( strUserComment );
            }
            if( bdStartIndex != null ) {
                etIndexStart.setText( bdStartIndex.toString() );
            }
            c.close();
        }
        else {
            initSpinner(spnUomLength, MainDbAdapter.TABLE_NAME_UOM,
                    MainDbAdapter.COL_LIST_GEN_ROWID_NAME, new String[]{MainDbAdapter.COL_NAME_GEN_NAME},
                    MainDbAdapter.COL_NAME_UOM__UOMTYPE + "='" + StaticValues.UOM_LENGTH_TYPE_CODE + "'" +
                        MainDbAdapter.WHERE_CONDITION_ISACTIVE_ANDPREFIX, null, MainDbAdapter.COL_NAME_GEN_NAME, 1, false);
            //uom for volume
            initSpinner(spnUomVolume, MainDbAdapter.TABLE_NAME_UOM,
                    MainDbAdapter.COL_LIST_GEN_ROWID_NAME, new String[]{MainDbAdapter.COL_NAME_GEN_NAME},
                    MainDbAdapter.COL_NAME_UOM__UOMTYPE + "='" + StaticValues.UOM_VOLUME_TYPE_CODE + "'" +
                        MainDbAdapter.WHERE_CONDITION_ISACTIVE_ANDPREFIX, null, MainDbAdapter.COL_NAME_GEN_NAME, 3, false);
             //default currency
            initSpinner(spnCurrency, MainDbAdapter.TABLE_NAME_CURRENCY,
                    MainDbAdapter.COL_LIST_GEN_ROWID_NAME, new String[]{MainDbAdapter.COL_NAME_GEN_NAME},
                    MainDbAdapter.WHERE_CONDITION_ISACTIVE, null, MainDbAdapter.COL_NAME_GEN_NAME, 1, false);

            ckIsActive.setChecked( true );
        }
    }

    @Override
    protected boolean saveData() {

        BigDecimal bdStartIndex = null;
        String strIndexStart = etIndexStart.getText().toString();
        if( strIndexStart != null && strIndexStart.length() > 0 ) {
            try {
                bdStartIndex = new BigDecimal( strIndexStart );
            }
            catch( NumberFormatException e ) {
                Toast toast = Toast.makeText( getApplicationContext(),
                        mResource.getString( R.string.GEN_NumberFormatException ) + ": "
                        + mResource.getString( R.string.CarEditActivity_IndexStartLabel ), Toast.LENGTH_SHORT );
                toast.show();
                return false;
            }
        }
        ContentValues cvData = new ContentValues();
        cvData.put( MainDbAdapter.COL_NAME_GEN_NAME,
                etName.getText().toString());
        cvData.put( MainDbAdapter.COL_NAME_GEN_ISACTIVE,
                (ckIsActive.isChecked() ? "Y" : "N") );
        cvData.put( MainDbAdapter.COL_NAME_GEN_USER_COMMENT,
                etUserComment.getText().toString() );
        cvData.put( MainDbAdapter.COL_NAME_CAR__MODEL,
                etCarModel.getText().toString() );
        cvData.put( MainDbAdapter.COL_NAME_CAR__REGISTRATIONNO,
                etCarRegNo.getText().toString());
        cvData.put( MainDbAdapter.COL_NAME_CAR__INDEXSTART, bdStartIndex.toString() );
        cvData.put( MainDbAdapter.COL_NAME_CAR__UOMLENGTH_ID,
                spnUomLength.getSelectedItemId() );
        cvData.put( MainDbAdapter.COL_NAME_CAR__UOMVOLUME_ID,
                spnUomVolume.getSelectedItemId() );
        cvData.put( MainDbAdapter.COL_NAME_CAR__CURRENCY_ID,
                spnCurrency.getSelectedItemId());

        int dbRetVal = -1;
        String strErrMsg = null;
        if( mRowId == -1 ) {
            //when a new car defined the current index is same with the start index
            cvData.put( MainDbAdapter.COL_NAME_CAR__INDEXCURRENT, bdStartIndex.toString() );
            dbRetVal = ((Long)mDbAdapter.createRecord(MainDbAdapter.TABLE_NAME_CAR, cvData)).intValue();
            if(dbRetVal > 0){
            	finish();
            	return true;
            }
            else{
                if(dbRetVal == -1) //DB Error
                    strErrMsg = mDbAdapter.lastErrorMessage;
                else //precondition error
                    strErrMsg = mResource.getString(-1 * dbRetVal);
                madbErrorAlert.setMessage(strErrMsg);
                madError = madbErrorAlert.create();
                madError.show();
                return false;
            }
        }
        else {
        	dbRetVal = mDbAdapter.updateRecord(MainDbAdapter.TABLE_NAME_CAR, mRowId, cvData);
            if(dbRetVal != -1){
                strErrMsg = mResource.getString(dbRetVal);
                if(dbRetVal == R.string.ERR_000)
                    strErrMsg = strErrMsg + "\n" + mDbAdapter.lastErrorMessage;
                madbErrorAlert.setMessage(strErrMsg);
                madError = madbErrorAlert.create();
                madError.show();
                return false;
            }
            else{
            	//if the selected car in the main activity is inactivated, invalidate it
            	if(mRowId == mPreferences.getLong("CurrentCar_ID", -1)
            			&& !ckIsActive.isChecked()){
            		mPrefEditor.putLong("CurrentCar_ID", -1);
                    mPrefEditor.commit();
            	}
                finish();
                return true;
            }
        }
    }

    @Override
    protected void setLayout() {
   		setContentView(R.layout.car_edit_activity_s01);
    }

    protected View.OnClickListener onNewCurrClickListener =
        new View.OnClickListener(){
            public void onClick( View v )
            {
				Intent i = new Intent(CarEditActivity.this, CurrencyEditActivity.class);
				i.putExtra("Operation", "N");
				startActivityForResult(i, 0);
            }
        };

	/* (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		long newId = 0;
		if(data != null)
			newId =	data.getLongExtra("mRowId", 0);
			
        initSpinner(spnCurrency, MainDbAdapter.TABLE_NAME_CURRENCY,
                MainDbAdapter.COL_LIST_GEN_ROWID_NAME, new String[]{MainDbAdapter.COL_NAME_GEN_NAME},
                MainDbAdapter.WHERE_CONDITION_ISACTIVE, null, MainDbAdapter.COL_NAME_GEN_NAME, newId, false);
	}

	/* (non-Javadoc)
	 * @see org.andicar.activity.BaseActivity#setSpecificLayout()
	 */
	@Override
	public void setSpecificLayout() {
	}

	/* (non-Javadoc)
	 * @see org.andicar.activity.EditActivityBase#setDefaultValues()
	 */
	@Override
	public void setDefaultValues() {
	}

}
