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
import org.andicar2.activity.R;

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
            mRowId = mBundleExtras.getLong( MainDbAdapter.COL_NAME_GEN_ROWID );
            Cursor c = mDbAdapter.fetchRecord(MainDbAdapter.TABLE_NAME_UOMCONVERSION,
                    MainDbAdapter.COL_LIST_UOMCONVERSION_TABLE, mRowId);
            String name = c.getString( MainDbAdapter.COL_POS_GEN_NAME );
            String isActive = c.getString( MainDbAdapter.COL_POS_GEN_ISACTIVE );
            String userComment = c.getString( MainDbAdapter.COL_POS_GEN_USER_COMMENT );
            String conversionRate = c.getString( MainDbAdapter.COL_POS_UOMCONVERSION__RATE );
            uomFromId = c.getLong( MainDbAdapter.COL_POS_UOMCONVERSION__UOMFROM_ID );
            uomToId = c.getLong( MainDbAdapter.COL_POS_UOMCONVERSION__UOMTO_ID );
            c.close();

            if (name != null) {
                etName.setText(name);
            }
            if (isActive != null) {
                ckIsActive.setChecked(isActive.equals("Y"));
            }
            if (userComment != null) {
                etUserComment.setText( userComment );
            }

            initSpinner(spnUomFrom, MainDbAdapter.TABLE_NAME_UOM,
                    MainDbAdapter.COL_LIST_GEN_ROWID_NAME, new String[]{MainDbAdapter.COL_NAME_GEN_NAME},
                        MainDbAdapter.WHERE_CONDITION_ISACTIVE, null,
                        MainDbAdapter.COL_NAME_GEN_NAME, uomFromId, false);

            c = mDbAdapter.fetchRecord(MainDbAdapter.TABLE_NAME_UOM, MainDbAdapter.COL_LIST_UOM_TABLE, uomFromId);
            uomFromType = c.getString(MainDbAdapter.COL_POS_UOM__UOMTYPE);
            c.close();

            initSpinner(spnUomTo, MainDbAdapter.TABLE_NAME_UOM,
                    MainDbAdapter.COL_LIST_GEN_ROWID_NAME, new String[]{MainDbAdapter.COL_NAME_GEN_NAME},
                        MainDbAdapter.COL_NAME_UOM__UOMTYPE + "='" + uomFromType + "' " +
                        " AND " + MainDbAdapter.COL_NAME_GEN_ROWID + " <> " + uomFromId +
                        MainDbAdapter.WHERE_CONDITION_ISACTIVE_ANDPREFIX, null, MainDbAdapter.COL_NAME_GEN_NAME, uomToId, false);
            if (conversionRate != null) {
                etConversionRate.setText( conversionRate.toString() );
            }
        } else {
            initSpinner(spnUomFrom, MainDbAdapter.TABLE_NAME_UOM,
                    MainDbAdapter.COL_LIST_GEN_ROWID_NAME, new String[]{MainDbAdapter.COL_NAME_GEN_NAME},
                        MainDbAdapter.WHERE_CONDITION_ISACTIVE, null, MainDbAdapter.COL_NAME_GEN_NAME, -1, false);
            ckIsActive.setChecked(true);
        }

    }

    private OnItemSelectedListener uomFromSelectedListener =
                new OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                        setSpinnerTextToCode(arg0, arg3, arg1);
                        Cursor c = mDbAdapter.fetchRecord(MainDbAdapter.TABLE_NAME_UOM, MainDbAdapter.COL_LIST_UOM_TABLE, arg3);
                        uomFromType = c.getString(MainDbAdapter.COL_POS_UOM__UOMTYPE);
                        c.close();
                        
                        initSpinner(spnUomTo, MainDbAdapter.TABLE_NAME_UOM,
                                MainDbAdapter.COL_LIST_GEN_ROWID_NAME, new String[]{MainDbAdapter.COL_NAME_GEN_NAME},
                                    MainDbAdapter.COL_NAME_UOM__UOMTYPE + "='" + uomFromType + "' " +
                                    " AND " + MainDbAdapter.COL_NAME_GEN_ROWID + " <> " + arg3 +
                                    MainDbAdapter.WHERE_CONDITION_ISACTIVE_ANDPREFIX, null, MainDbAdapter.COL_NAME_GEN_NAME, uomToId, false);
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
        data.put( MainDbAdapter.COL_NAME_GEN_NAME,
                etName.getText().toString());
        data.put( MainDbAdapter.COL_NAME_GEN_ISACTIVE,
                (ckIsActive.isChecked() ? "Y" : "N") );
        data.put( MainDbAdapter.COL_NAME_GEN_USER_COMMENT,
                etUserComment.getText().toString() );
        data.put( MainDbAdapter.COL_NAME_UOMCONVERSION__UOMFROM_ID, fromId);
        data.put( MainDbAdapter.COL_NAME_UOMCONVERSION__UOMTO_ID, toId);
        data.put( MainDbAdapter.COL_NAME_UOMCONVERSION__RATE, convRateStr);

        int dbRetVal = -1;
        String strErrMsg = null;
        if( mRowId == -1 ) {
        	dbRetVal = ((Long)mDbAdapter.createRecord(MainDbAdapter.TABLE_NAME_UOMCONVERSION, data)).intValue();
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
        	dbRetVal = mDbAdapter.updateRecord(MainDbAdapter.TABLE_NAME_UOMCONVERSION, mRowId, data);
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
