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
import org.andicar.utils.StaticValues;
import org.andicar2.activity.R;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

/**
 *
 * @author miki
 */
public class UOMEditActivity extends EditActivityBase {
    private EditText etName = null;
    private EditText etUserComment = null;
    private EditText etCode = null;
    private CheckBox ckIsActive = null;
    private Spinner spnUomType = null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

//        uomType = mBundleExtras.getString(MainDbAdapter.UOM_COL_UOMTYPE_NAME);
        String operation = mBundleExtras.getString("Operation"); //E = edit, N = new

        etName = (EditText) findViewById(R.id.etName);
        etUserComment = (EditText) findViewById(R.id.etUserComment);
        etCode = (EditText) findViewById(R.id.etCode);
        ckIsActive = (CheckBox) findViewById(R.id.ckIsActive);
        spnUomType = (Spinner)findViewById(R.id.spnUomType);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
            this, R.array.uom_type_entries, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnUomType.setAdapter(adapter);

        if (operation.equals("E")) {
            mRowId = mBundleExtras.getLong( MainDbAdapter.COL_NAME_GEN_ROWID );
            Cursor c = mDbAdapter.fetchRecord(MainDbAdapter.TABLE_NAME_UOM,
                    MainDbAdapter.COL_LIST_UOM_TABLE, mRowId);
            String name = c.getString( MainDbAdapter.COL_POS_GEN_NAME );
            String isActive = c.getString( MainDbAdapter.COL_POS_GEN_ISACTIVE );
            String userComment = c.getString( MainDbAdapter.COL_POS_GEN_USER_COMMENT );
            String code = c.getString( MainDbAdapter.COL_POS_UOM__CODE );
            String uomType = c.getString( MainDbAdapter.COL_POS_UOM__UOMTYPE );
            if(uomType.equals(StaticValues.UOM_LENGTH_TYPE_CODE))
                spnUomType.setSelection(0);
            else if(uomType.equals(StaticValues.UOM_VOLUME_TYPE_CODE))
                spnUomType.setSelection(1);
            else if(uomType.equals(StaticValues.UOM_OTHER_TYPE_CODE))
                spnUomType.setSelection(2);

            if (name != null) {
                etName.setText(name);
            }
            if (isActive != null) {
                ckIsActive.setChecked(isActive.equals("Y"));
            }
            if (userComment != null) {
                etUserComment.setText( userComment );
            }
            if (code != null) {
                etCode.setText( code );
            }
            c.close();
        }
        else {
            ckIsActive.setChecked(true);
            spnUomType.setSelection(2);
        }
    }

    @Override
    protected boolean saveData() {

        ContentValues data = new ContentValues();
        data.put( MainDbAdapter.COL_NAME_GEN_NAME,
                etName.getText().toString());
        data.put( MainDbAdapter.COL_NAME_GEN_ISACTIVE,
                (ckIsActive.isChecked() ? "Y" : "N") );
        data.put( MainDbAdapter.COL_NAME_GEN_USER_COMMENT,
                etUserComment.getText().toString() );
        data.put( MainDbAdapter.COL_NAME_UOM__CODE,
                etCode.getText().toString());
        if(spnUomType.getSelectedItemPosition() == 0)
            data.put( MainDbAdapter.COL_NAME_UOM__UOMTYPE, StaticValues.UOM_LENGTH_TYPE_CODE);
        else if(spnUomType.getSelectedItemPosition() == 1)
            data.put( MainDbAdapter.COL_NAME_UOM__UOMTYPE, StaticValues.UOM_VOLUME_TYPE_CODE);
        else
            data.put( MainDbAdapter.COL_NAME_UOM__UOMTYPE, StaticValues.UOM_OTHER_TYPE_CODE);

        int dbRetVal = -1;
        String strErrMsg = null;
        if( mRowId == -1 ) {
        	dbRetVal = ((Long)mDbAdapter.createRecord(MainDbAdapter.TABLE_NAME_UOM, data)).intValue();
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
        	dbRetVal = mDbAdapter.updateRecord(MainDbAdapter.TABLE_NAME_UOM, mRowId, data);
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
   		setContentView(R.layout.uom_edit_activity_s01);
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
