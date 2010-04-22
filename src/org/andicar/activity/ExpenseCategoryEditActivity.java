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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import org.andicar.persistence.MainDbAdapter;

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

    /** Called when the activity is first created. */
    @Override
    public void onCreate( Bundle icicle )
    {
        super.onCreate( icicle, R.layout.expensecategory_edit_activity, mOkClickListener );

        etName = (EditText) findViewById( R.id.etName );
        etUserComment = (EditText) findViewById( R.id.etUserComment );
        ckIsActive = (CheckBox) findViewById( R.id.ckIsActive );
        ckIsExcludeFromMileageCost = (CheckBox) findViewById( R.id.ckIsExcludeFromMileageCost );

        String strOperationType = mbundleExtras.getString("Operation"); //E = edit, N = new

        if( strOperationType.equals( "E") ) {
            mRowId = mbundleExtras.getLong( MainDbAdapter.GEN_COL_ROWID_NAME );
            Cursor dbcRecordCursor = mDbAdapter.fetchRecord(MainDbAdapter.EXPENSECATEGORY_TABLE_NAME,
                    MainDbAdapter.expenseCategoryTableColNames, mRowId);
            String strName = dbcRecordCursor.getString( MainDbAdapter.GEN_COL_NAME_POS );
            String strIsActive = dbcRecordCursor.getString( MainDbAdapter.GEN_COL_ISACTIVE_POS );
            String strUserComment = dbcRecordCursor.getString( MainDbAdapter.GEN_COL_USER_COMMENT_POS );
            String strExpCatIsExcludeFromMileageCostCheck = dbcRecordCursor.getString( MainDbAdapter.EXPENSECATEGORY_COL_ISEXCLUDEFROMMILEAGECOST_POS );

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
        }
        else {
            ckIsActive.setChecked( true );
        }

    }

    private View.OnClickListener mOkClickListener =
                new View.OnClickListener()
                {
                    public void onClick( View v )
                    {
                        String strRetVal = checkMandatory((ViewGroup) findViewById(R.id.vgRoot));
                        if( strRetVal != null ) {
                            Toast toast = Toast.makeText( getApplicationContext(),
                                    mResource.getString( R.string.GEN_FILL_MANDATORY ) + ": " + strRetVal, Toast.LENGTH_SHORT );
                            toast.show();
                            return;
                        }

                        ContentValues cvData = new ContentValues();
                        cvData.put( MainDbAdapter.GEN_COL_NAME_NAME,
                                etName.getText().toString());
                        cvData.put( MainDbAdapter.GEN_COL_ISACTIVE_NAME,
                                (ckIsActive.isChecked() ? "Y" : "N") );
                        cvData.put( MainDbAdapter.GEN_COL_USER_COMMENT_NAME,
                                etUserComment.getText().toString() );
                        cvData.put( MainDbAdapter.EXPENSECATEGORY_COL_ISEXCLUDEFROMMILEAGECOST_NAME,
                                (ckIsExcludeFromMileageCost.isChecked() ? "Y" : "N") );

                        if( mRowId == null ) {
                            mDbAdapter.createRecord(MainDbAdapter.EXPENSECATEGORY_TABLE_NAME, cvData);
                            finish();
                        }
                        else {
                            int iUpdateResult = mDbAdapter.updateRecord(MainDbAdapter.EXPENSECATEGORY_TABLE_NAME, mRowId, cvData);
                            if(iUpdateResult != -1){
                                String strErrMsg = "";
                                strErrMsg = mResource.getString(iUpdateResult);
                                if(iUpdateResult == R.string.ERR_000)
                                    strErrMsg = strErrMsg + "\n" + mDbAdapter.lastErrorMessage;
                                madbErrorAlert.setMessage(strErrMsg);
                                madError = madbErrorAlert.create();
                                madError.show();
                            }
                            else
                                finish();
                        }
                    }
                };

}
