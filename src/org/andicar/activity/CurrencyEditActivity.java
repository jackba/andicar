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
public class CurrencyEditActivity extends EditActivityBase {

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle, R.layout.currency_edit_activity, mOkClickListener);

        String operation = extras.getString("Operation"); //E = edit, N = new

        if( operation.equals( "E") ) {
            mRowId = extras.getLong( MainDbAdapter.GEN_COL_ROWID_NAME );
            Cursor recordCursor = mMainDbAdapter.fetchRecord(MainDbAdapter.CURRENCY_TABLE_NAME,
                    MainDbAdapter.currencyTableColNames, mRowId);
            String name = recordCursor.getString( MainDbAdapter.GEN_COL_NAME_POS );
            String isActive = recordCursor.getString( MainDbAdapter.GEN_COL_ISACTIVE_POS );
            String userComment = recordCursor.getString( MainDbAdapter.GEN_COL_USER_COMMENT_POS );
            String code = recordCursor.getString( MainDbAdapter.CURRENCY_COL_CODE_POS );
            if (name != null) {
                ((EditText) findViewById(R.id.genNameEntry)).setText(name);
            }
            if (isActive != null) {
                ((CheckBox) findViewById(R.id.genIsActiveCheck)).setChecked(isActive.equals("Y"));
            }
            if (userComment != null) {
                ((EditText) findViewById( R.id.genUserCommentEntry )).setText( userComment );
            }
            if (code != null) {
                ((EditText) findViewById( R.id.currencyEditCodeEntry )).setText( code );
            }

        } else {
            ((CheckBox) findViewById(R.id.genIsActiveCheck)).setChecked(true);
        }

    }

    private View.OnClickListener mOkClickListener = new View.OnClickListener() {
                    public void onClick(View v) {
                        String retVal = checkMandatory((ViewGroup) findViewById(R.id.genRootViewGroup));
                        if( retVal != null ) {
                            Toast toast = Toast.makeText( getApplicationContext(),
                                    mRes.getString( R.string.GEN_FILL_MANDATORY ) + ": " + retVal, Toast.LENGTH_SHORT );
                            toast.show();
                            return;
                        }

                        ContentValues data = new ContentValues();
                        data.put( MainDbAdapter.GEN_COL_NAME_NAME,
                                ((EditText) findViewById(R.id.genNameEntry)).getText().toString());
                        data.put( MainDbAdapter.GEN_COL_ISACTIVE_NAME,
                                (((CheckBox) findViewById( R.id.genIsActiveCheck )).isChecked() ? "Y" : "N") );
                        data.put( MainDbAdapter.GEN_COL_USER_COMMENT_NAME,
                                ((EditText) findViewById( R.id.genUserCommentEntry )).getText().toString() );
                        data.put( MainDbAdapter.CURRENCY_COL_CODE_NAME,
                                ((EditText) findViewById(R.id.currencyEditCodeEntry)).getText().toString());

                        if (mRowId == null) {
                            mMainDbAdapter.createRecord(MainDbAdapter.CURRENCY_TABLE_NAME, data);
                            finish();
                        } else {
                            int updResult = mMainDbAdapter.updateRecord(MainDbAdapter.CURRENCY_TABLE_NAME, mRowId, data);
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
