/*
Copyright (C) 2009-2010 Miklos Keresztes - miklos.keresztes@gmail.com

This program is free software; you can redistribute it and/or modify it
under the terms of the GNU General Public License as published by the
Free Software Foundation; either version 2 of the License.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program;
if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

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

    /** Called when the activity is first created. */
    @Override
    public void onCreate( Bundle icicle )
    {
        super.onCreate( icicle, R.layout.expensetype_edit_activity, mOkClickListener );

        if( extras != null ) {
            mRowId = extras.getLong( MainDbAdapter.GEN_COL_ROWID_NAME );
            Cursor recordCursor = mMainDbHelper.fetchRecord(MainDbAdapter.EXPENSETYPE_TABLE_NAME,
                    MainDbAdapter.expenseTableColNames, mRowId);
            String name = recordCursor.getString( MainDbAdapter.GEN_COL_NAME_POS );
            String isActive = recordCursor.getString( MainDbAdapter.GEN_COL_ISACTIVE_POS );
            String userComment = recordCursor.getString( MainDbAdapter.GEN_COL_USER_COMMENT_POS );

            if( name != null ) {
                ((EditText) findViewById( R.id.genNameEntry )).setText( name );
            }
            if( isActive != null ) {
                ((CheckBox) findViewById( R.id.genIsActiveCheck )).setChecked( isActive.equals( "Y" ) );
            }
            if( userComment != null ) {
                ((EditText) findViewById( R.id.genUserCommentEntry )).setText( userComment );
            }
        }
        else {
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

                        ContentValues data = new ContentValues();
                        data.put( MainDbAdapter.GEN_COL_NAME_NAME,
                                ((EditText) findViewById( R.id.genNameEntry )).getText().toString());
                        data.put( MainDbAdapter.GEN_COL_ISACTIVE_NAME,
                                (((CheckBox) findViewById( R.id.genIsActiveCheck )).isChecked() ? "Y" : "N") );
                        data.put( MainDbAdapter.GEN_COL_USER_COMMENT_NAME,
                                ((EditText) findViewById( R.id.genUserCommentEntry )).getText().toString() );

                        if( mRowId == null ) {
                            mMainDbHelper.createRecord(MainDbAdapter.EXPENSETYPE_TABLE_NAME, data);
                        }
                        else {
                            mMainDbHelper.updateRecord(MainDbAdapter.EXPENSETYPE_TABLE_NAME, mRowId, data);
                        }
                        finish();
                    }
                };

}
