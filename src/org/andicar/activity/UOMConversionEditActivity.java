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

import org.andicar.persistence.MainDbAdapter;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

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
        super.onCreate(icicle);
        
        spnUomFrom = (Spinner) findViewById( R.id.spnUomFrom );
        spnUomFrom.setOnItemSelectedListener(uomFromSelectedListener);
        spnUomTo = (Spinner) findViewById( R.id.spnUomTo );
        etName = (EditText) findViewById(R.id.etName);
        etUserComment = (EditText) findViewById( R.id.etUserComment );
        etConversionRate = (EditText) findViewById( R.id.etConversionRate );
        ckIsActive = (CheckBox) findViewById(R.id.ckIsActive);

        String operation = mBundleExtras.getString("Operation"); //E = edit, N = new

        if( operation.equals( "E") ) {
            mRowId = mBundleExtras.getLong( MainDbAdapter.GEN_COL_ROWID_NAME );
            Cursor c = mDbAdapter.fetchRecord(MainDbAdapter.UOM_CONVERSION_TABLE_NAME,
                    MainDbAdapter.uomConversionTableColNames, mRowId);
            String name = c.getString( MainDbAdapter.GEN_COL_NAME_POS );
            String isActive = c.getString( MainDbAdapter.GEN_COL_ISACTIVE_POS );
            String userComment = c.getString( MainDbAdapter.GEN_COL_USER_COMMENT_POS );
            String conversionRate = c.getString( MainDbAdapter.UOM_CONVERSION_COL_RATE_POS );
            uomFromId = c.getLong( MainDbAdapter.UOM_CONVERSION_COL_UOMFROM_ID_POS );
            uomToId = c.getLong( MainDbAdapter.UOM_CONVERSION_COL_UOMTO_ID_POS );

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
                        MainDbAdapter.isActiveCondition, null,
                        MainDbAdapter.GEN_COL_NAME_NAME, uomFromId, false);

            uomFromType = mDbAdapter.fetchRecord(MainDbAdapter.UOM_TABLE_NAME, MainDbAdapter.uomTableColNames, uomFromId)
                            .getString(MainDbAdapter.UOM_COL_UOMTYPE_POS);

            initSpinner(spnUomTo, MainDbAdapter.UOM_TABLE_NAME,
                    MainDbAdapter.genColName, new String[]{MainDbAdapter.GEN_COL_NAME_NAME},
                        MainDbAdapter.UOM_COL_UOMTYPE_NAME + "='" + uomFromType + "' " +
                        " AND " + MainDbAdapter.GEN_COL_ROWID_NAME + " <> " + uomFromId +
                        MainDbAdapter.isActiveWithAndCondition, null, MainDbAdapter.GEN_COL_NAME_NAME, uomToId, false);
            if (conversionRate != null) {
                etConversionRate.setText( conversionRate.toString() );
            }
            c.close();
        } else {
            initSpinner(spnUomFrom, MainDbAdapter.UOM_TABLE_NAME,
                    MainDbAdapter.genColName, new String[]{MainDbAdapter.GEN_COL_NAME_NAME},
                        MainDbAdapter.isActiveCondition, null, MainDbAdapter.GEN_COL_NAME_NAME, -1, false);
            ckIsActive.setChecked(true);
        }

    }

    private OnItemSelectedListener uomFromSelectedListener =
                new OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                        setSpinnerTextToCode(arg0, arg3, arg1);
                        Cursor c = mDbAdapter.fetchRecord(MainDbAdapter.UOM_TABLE_NAME, MainDbAdapter.uomTableColNames, arg3);
                        uomFromType = c.getString(MainDbAdapter.UOM_COL_UOMTYPE_POS);
                        c.close();
                        
                        initSpinner(spnUomTo, MainDbAdapter.UOM_TABLE_NAME,
                                MainDbAdapter.genColName, new String[]{MainDbAdapter.GEN_COL_NAME_NAME},
                                    MainDbAdapter.UOM_COL_UOMTYPE_NAME + "='" + uomFromType + "' " +
                                    " AND " + MainDbAdapter.GEN_COL_ROWID_NAME + " <> " + arg3 +
                                    MainDbAdapter.isActiveWithAndCondition, null, MainDbAdapter.GEN_COL_NAME_NAME, uomToId, false);
                    }
                    public void onNothingSelected(AdapterView<?> arg0) {
                    }
                };

    @Override
    protected boolean saveData() {
        //check mandatory fields
        String convRateStr = etConversionRate.getText().toString();

        long fromId = spnUomFrom.getSelectedItemId();
        long toId = spnUomTo.getSelectedItemId();
        int retVal2 = mDbAdapter.canInsertUpdateUOMConversion(mRowId, fromId, toId);
        if(retVal2 != -1){
            madbErrorAlert.setMessage(mResource.getString(retVal2));
            madError = madbErrorAlert.create();
            madError.show();
            return false;
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

        int dbRetVal = -1;
        String strErrMsg = null;
        if( mRowId == -1 ) {
        	dbRetVal = ((Long)mDbAdapter.createRecord(MainDbAdapter.UOM_CONVERSION_TABLE_NAME, data)).intValue();
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
        	dbRetVal = mDbAdapter.updateRecord(MainDbAdapter.UOM_CONVERSION_TABLE_NAME, mRowId, data);
            if(dbRetVal != -1){
                String errMsg = "";
                errMsg = mResource.getString(dbRetVal);
                if(dbRetVal == R.string.ERR_000)
                    errMsg = errMsg + "\n" + mDbAdapter.lastErrorMessage;
                madbErrorAlert.setMessage(errMsg);
                madError = madbErrorAlert.create();
                madError.show();
                return false;
            }
            else{
                finish();
                return true;
            }
        }
    }

    @Override
    protected void setLayout() {
    	if(mPreferences.getString("UIStyle", "s01").equalsIgnoreCase("s00"))
    		setContentView(R.layout.uomconversion_edit_activity_s00);
    	else if(mPreferences.getString("UIStyle", "s01").equalsIgnoreCase("s01"))
    		setContentView(R.layout.uomconversion_edit_activity_s01);
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
