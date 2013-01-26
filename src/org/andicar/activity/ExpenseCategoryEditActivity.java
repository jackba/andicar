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
import android.widget.CheckBox;
import android.widget.EditText;

/**
 *
 * @author miki
 */
public class ExpenseCategoryEditActivity extends EditActivityBase
{
    private EditText etName = null;
    private EditText etUserComment = null;
    private CheckBox ckIsActive = null;
    private CheckBox ckIsExcludeFromMileageCost = null;
    private boolean mIsFuel = false;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate( Bundle icicle )
    {
        super.onCreate(icicle);

        String strOperationType = mBundleExtras.getString("Operation"); //E = edit, N = new
        mIsFuel = mBundleExtras.getBoolean("IsFuel");

        etName = (EditText) findViewById( R.id.etName );
        etUserComment = (EditText) findViewById( R.id.etUserComment );
        ckIsActive = (CheckBox) findViewById( R.id.ckIsActive );
        ckIsExcludeFromMileageCost = (CheckBox) findViewById( R.id.ckIsExcludeFromMileageCost );
        
        if( strOperationType.equals( "E") ) {
            mRowId = mBundleExtras.getLong( MainDbAdapter.GEN_COL_ROWID_NAME );
            Cursor c = mDbAdapter.fetchRecord(MainDbAdapter.EXPENSECATEGORY_TABLE_NAME,
                    MainDbAdapter.expenseCategoryTableColNames, mRowId);
            String strName = c.getString( MainDbAdapter.GEN_COL_NAME_POS );
            String strIsActive = c.getString( MainDbAdapter.GEN_COL_ISACTIVE_POS );
            String strUserComment = c.getString( MainDbAdapter.GEN_COL_USER_COMMENT_POS );
            String strExpCatIsExcludeFromMileageCostCheck = c.getString( MainDbAdapter.EXPENSECATEGORY_COL_ISEXCLUDEFROMMILEAGECOST_POS );
            mIsFuel = c.getString( MainDbAdapter.EXPENSECATEGORY_COL_ISFUEL_POS ).equals("Y");

            if( strName != null ) {
                etName.setText( strName );
            }
            if( strIsActive != null ) {
                ckIsActive.setChecked( strIsActive.equals( "Y" ) );
            }
            if( strUserComment != null ) {
                etUserComment.setText( strUserComment );
            }
            if( strExpCatIsExcludeFromMileageCostCheck != null ) {
                ckIsExcludeFromMileageCost.setChecked( strExpCatIsExcludeFromMileageCostCheck.equals( "Y" ) );
            }
            c.close();
        }
        else {
            ckIsActive.setChecked( true );
        }
        if(mIsFuel)
        	setTitle(R.string.APP_Activity_FuelCategoryEdit);
        else
        	setTitle(R.string.APP_Activity_ExpenseCategoryEdit);
        	

    }

    @Override
    protected boolean saveData() {

        ContentValues cvData = new ContentValues();
        cvData.put( MainDbAdapter.GEN_COL_NAME_NAME,
                etName.getText().toString());
        cvData.put( MainDbAdapter.GEN_COL_ISACTIVE_NAME,
                (ckIsActive.isChecked() ? "Y" : "N") );
        cvData.put( MainDbAdapter.GEN_COL_USER_COMMENT_NAME,
                etUserComment.getText().toString() );
        cvData.put( MainDbAdapter.EXPENSECATEGORY_COL_ISEXCLUDEFROMMILEAGECOST_NAME,
                (ckIsExcludeFromMileageCost.isChecked() ? "Y" : "N") );
        cvData.put( MainDbAdapter.EXPENSECATEGORY_COL_ISFUEL_NAME, mIsFuel ? "Y" : "N");

        int dbRetVal = -1;
        String strErrMsg = null;
        if( mRowId == -1 ) {
        	dbRetVal = ((Long)mDbAdapter.createRecord(MainDbAdapter.EXPENSECATEGORY_TABLE_NAME, cvData)).intValue();
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
        	dbRetVal = mDbAdapter.updateRecord(MainDbAdapter.EXPENSECATEGORY_TABLE_NAME, mRowId, cvData);
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
                finish();
                return true;
            }
        }
    }

    @Override
    protected void setLayout() {
   		setContentView(R.layout.expensecategory_edit_activity_s01);
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
