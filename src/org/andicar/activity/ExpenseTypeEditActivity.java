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
public class ExpenseTypeEditActivity extends EditActivityBase
{
    private EditText etName = null;
    private EditText etUserComment = null;
    private CheckBox ckIsActive = null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate( Bundle icicle )
    {
        super.onCreate( icicle, R.layout.expensetype_edit_activity, mOkClickListener );

        etName = (EditText) findViewById( R.id.etName );
        etUserComment = (EditText) findViewById( R.id.etUserComment );
        ckIsActive = (CheckBox) findViewById( R.id.ckIsActive );

        String operation = mBundleExtras.getString("Operation"); //E = edit, N = new

        if( operation.equals( "E") ) {
            mRowId = mBundleExtras.getLong( MainDbAdapter.GEN_COL_ROWID_NAME );
            Cursor recordCursor = mDbAdapter.fetchRecord(MainDbAdapter.EXPENSETYPE_TABLE_NAME,
                    MainDbAdapter.expenseTypeTableColNames, mRowId);
            String name = recordCursor.getString( MainDbAdapter.GEN_COL_NAME_POS );
            String isActive = recordCursor.getString( MainDbAdapter.GEN_COL_ISACTIVE_POS );
            String userComment = recordCursor.getString( MainDbAdapter.GEN_COL_USER_COMMENT_POS );

            if( name != null ) {
                etName.setText( name );
            }
            if( isActive != null ) {
                ckIsActive.setChecked( isActive.equals( "Y" ) );
            }
            if( userComment != null ) {
                etUserComment.setText( userComment );
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
                        String retVal = checkMandatory((ViewGroup) findViewById(R.id.vgRoot));
                        if( retVal != null ) {
                            Toast toast = Toast.makeText( getApplicationContext(),
                                    mResource.getString( R.string.GEN_FILL_MANDATORY ) + ": " + retVal, Toast.LENGTH_SHORT );
                            toast.show();
                            return;
                        }

                        ContentValues data = new ContentValues();
                        data.put( MainDbAdapter.GEN_COL_NAME_NAME,
                                etName.getText().toString());
                        data.put( MainDbAdapter.GEN_COL_ISACTIVE_NAME,
                                (ckIsActive.isChecked() ? "Y" : "N") );
                        data.put( MainDbAdapter.GEN_COL_USER_COMMENT_NAME,
                                etUserComment.getText().toString() );

                        if( mRowId == null ) {
                            mDbAdapter.createRecord(MainDbAdapter.EXPENSETYPE_TABLE_NAME, data);
                            finish();
                        }
                        else {
                            int updResult = mDbAdapter.updateRecord(MainDbAdapter.EXPENSETYPE_TABLE_NAME, mRowId, data);
                            if(updResult != -1){
                                String errMsg = "";
                                errMsg = mResource.getString(updResult);
                                if(updResult == R.string.ERR_000)
                                    errMsg = errMsg + "\n" + mDbAdapter.lastErrorMessage;
                                madbErrorAlert.setMessage(errMsg);
                                madError = madbErrorAlert.create();
                                madError.show();
                            }
                            else
                                finish();
                        }
                    }
                };

}
