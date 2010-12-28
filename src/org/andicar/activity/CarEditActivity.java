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
            mRowId = mBundleExtras.getLong( MainDbAdapter.GEN_COL_ROWID_NAME );
            Cursor c = mDbAdapter.fetchRecord(MainDbAdapter.CAR_TABLE_NAME, MainDbAdapter.carTableColNames, mRowId);
            String strName = c.getString( MainDbAdapter.GEN_COL_NAME_POS );
            String strIsActive = c.getString( MainDbAdapter.GEN_COL_ISACTIVE_POS );
            String strUserComment = c.getString( MainDbAdapter.GEN_COL_USER_COMMENT_POS );
            String strCarModel = c.getString( MainDbAdapter.CAR_COL_MODEL_POS );
            String strRegistrationNo = c.getString( MainDbAdapter.CAR_COL_REGISTRATIONNO_POS );
            try{
                bdStartIndex = new BigDecimal(c.getString( MainDbAdapter.CAR_COL_INDEXSTART_POS ));
            }
            catch(NumberFormatException e){};
            Long lUomLengthId = c.getLong( MainDbAdapter.CAR_COL_UOMLENGTH_ID_POS );
            Long lUomVolumeId = c.getLong( MainDbAdapter.CAR_COL_UOMVOLUME_ID_POS );
            Long lCurrencyId = c.getLong( MainDbAdapter.CAR_COL_CURRENCY_ID_POS );
            //uom for length
            initSpinner(spnUomLength, MainDbAdapter.UOM_TABLE_NAME,
                    MainDbAdapter.genColName, new String[]{MainDbAdapter.GEN_COL_NAME_NAME},
                    MainDbAdapter.UOM_COL_UOMTYPE_NAME + "='" + StaticValues.UOM_LENGTH_TYPE_CODE + "'" +
                        MainDbAdapter.isActiveWithAndCondition, null, MainDbAdapter.GEN_COL_NAME_NAME, lUomLengthId, false);
            //uom for volume
            initSpinner(spnUomVolume, MainDbAdapter.UOM_TABLE_NAME,
                    MainDbAdapter.genColName, new String[]{MainDbAdapter.GEN_COL_NAME_NAME},
                    MainDbAdapter.UOM_COL_UOMTYPE_NAME + "='" + StaticValues.UOM_VOLUME_TYPE_CODE + "'" +
                        MainDbAdapter.isActiveWithAndCondition, null, MainDbAdapter.GEN_COL_NAME_NAME, lUomVolumeId, false);
             //default currency
            initSpinner(spnCurrency, MainDbAdapter.CURRENCY_TABLE_NAME,
                    MainDbAdapter.genColName, new String[]{MainDbAdapter.GEN_COL_NAME_NAME},
                    MainDbAdapter.isActiveCondition, null, MainDbAdapter.GEN_COL_NAME_NAME, lCurrencyId, false);

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
            initSpinner(spnUomLength, MainDbAdapter.UOM_TABLE_NAME,
                    MainDbAdapter.genColName, new String[]{MainDbAdapter.GEN_COL_NAME_NAME},
                    MainDbAdapter.UOM_COL_UOMTYPE_NAME + "='" + StaticValues.UOM_LENGTH_TYPE_CODE + "'" +
                        MainDbAdapter.isActiveWithAndCondition, null, MainDbAdapter.GEN_COL_NAME_NAME, 1, false);
            //uom for volume
            initSpinner(spnUomVolume, MainDbAdapter.UOM_TABLE_NAME,
                    MainDbAdapter.genColName, new String[]{MainDbAdapter.GEN_COL_NAME_NAME},
                    MainDbAdapter.UOM_COL_UOMTYPE_NAME + "='" + StaticValues.UOM_VOLUME_TYPE_CODE + "'" +
                        MainDbAdapter.isActiveWithAndCondition, null, MainDbAdapter.GEN_COL_NAME_NAME, 3, false);
             //default currency
            initSpinner(spnCurrency, MainDbAdapter.CURRENCY_TABLE_NAME,
                    MainDbAdapter.genColName, new String[]{MainDbAdapter.GEN_COL_NAME_NAME},
                    MainDbAdapter.isActiveCondition, null, MainDbAdapter.GEN_COL_NAME_NAME, 1, false);

            ckIsActive.setChecked( true );
        }
    }

    @Override
    protected void saveData() {
        String strRetVal = checkMandatory(vgRoot);
        if( strRetVal != null ) {
            Toast toast = Toast.makeText( getApplicationContext(),
                    mResource.getString( R.string.GEN_FillMandatory ) + ": " + strRetVal, Toast.LENGTH_SHORT );
            toast.show();
            return;
        }

        strRetVal = checkNumeric(vgRoot, false);
        if( strRetVal != null ) {
            Toast toast = Toast.makeText( getApplicationContext(),
                    mResource.getString( R.string.GEN_NumberFormatException ) + ": " + strRetVal, Toast.LENGTH_SHORT );
            toast.show();
            return;
        }

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
                return;
            }
        }
        ContentValues cvData = new ContentValues();
        cvData.put( MainDbAdapter.GEN_COL_NAME_NAME,
                etName.getText().toString());
        cvData.put( MainDbAdapter.GEN_COL_ISACTIVE_NAME,
                (ckIsActive.isChecked() ? "Y" : "N") );
        cvData.put( MainDbAdapter.GEN_COL_USER_COMMENT_NAME,
                etUserComment.getText().toString() );
        cvData.put( MainDbAdapter.CAR_COL_MODEL_NAME,
                etCarModel.getText().toString() );
        cvData.put( MainDbAdapter.CAR_COL_REGISTRATIONNO_NAME,
                etCarRegNo.getText().toString());
        cvData.put( MainDbAdapter.CAR_COL_INDEXSTART_NAME, bdStartIndex.toString() );
        cvData.put( MainDbAdapter.CAR_COL_UOMLENGTH_ID_NAME,
                spnUomLength.getSelectedItemId() );
        cvData.put( MainDbAdapter.CAR_COL_UOMVOLUME_ID_NAME,
                spnUomVolume.getSelectedItemId() );
        cvData.put( MainDbAdapter.CAR_COL_CURRENCY_ID_NAME,
                spnCurrency.getSelectedItemId());

        if( mRowId == -1 ) {
            //when a new car defined the current index is same with the start index
            cvData.put( MainDbAdapter.CAR_COL_INDEXCURRENT_NAME, bdStartIndex.toString() );
            mDbAdapter.createRecord(MainDbAdapter.CAR_TABLE_NAME, cvData);
            finish();
        }
        else {
            int iUpdateResult = mDbAdapter.updateRecord(MainDbAdapter.CAR_TABLE_NAME, mRowId, cvData);
            if(iUpdateResult != -1){
                String strErrMsg = "";
                strErrMsg = mResource.getString(iUpdateResult);
                if(iUpdateResult == R.string.ERR_000)
                    strErrMsg = strErrMsg + "\n" + mDbAdapter.lastErrorMessage;
                madbErrorAlert.setMessage(strErrMsg);
                madError = madbErrorAlert.create();
                madError.show();
            }
            else{
            	//if the selected car in the main activity is inactivated, invalidate it
            	if(mRowId == mPreferences.getLong("CurrentCar_ID", -1)
            			&& !ckIsActive.isChecked()){
            		mPrefEditor.putLong("CurrentCar_ID", -1);
                    mPrefEditor.commit();
            	}
                finish();
            }
        }
    }

    @Override
    protected void setLayout() {
        setContentView(R.layout.car_edit_activity);
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
			
        initSpinner(spnCurrency, MainDbAdapter.CURRENCY_TABLE_NAME,
                MainDbAdapter.genColName, new String[]{MainDbAdapter.GEN_COL_NAME_NAME},
                MainDbAdapter.isActiveCondition, null, MainDbAdapter.GEN_COL_NAME_NAME, newId, false);
	}

}
