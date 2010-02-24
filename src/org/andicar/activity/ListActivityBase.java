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

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import org.andicar.persistence.MainDbAdapter;
import org.andicar.utils.Constants;

/**
 *
 * @author Miklos Keresztes
 */
public class ListActivityBase extends ListActivity {

    protected Cursor recordCursor = null;
    protected long mLongClickId = -1;
    protected boolean showInactiveRecords = false;
    protected Menu optionsMenu;
    protected Class mEditClass = null;
    protected String mTableName = null;
    protected String[] mColumns = null;
    protected String mWhereCondition = null;
    protected String mOrderByColumn = null;
    protected int mLayoutId;
    protected String[] mDbMapFrom;
    protected int[] mLayoutIdTo;

    protected Resources mRes = null;
    protected SharedPreferences mPreferences;
    protected MainDbAdapter mMainDbHelper = null;
    protected Bundle extras = null;

    /** Use instead  */
    @Override
    @Deprecated
    public void onCreate(Bundle icicle){

    }
    
    protected void onCreate(Bundle icicle, OnItemClickListener mItemClickListener, Class editClass,
            String tableName, String[] columns, String whereCondition, String orderByColumn,
            int pLayoutId, String[] pDbMapFrom, int[] pLayoutIdTo) {

        super.onCreate(icicle);

        mEditClass = editClass;
        mTableName = tableName;
        mColumns = columns;
        mWhereCondition = whereCondition;
        mOrderByColumn = orderByColumn;
        mLayoutId = pLayoutId;
        mDbMapFrom = pDbMapFrom;
        mLayoutIdTo = pLayoutIdTo;

        mRes = getResources();
        mPreferences = getSharedPreferences(Constants.GLOBAL_PREFERENCE_NAME, 0);

        if(mMainDbHelper == null)
            mMainDbHelper = new MainDbAdapter(this);

        if(extras == null)
            extras = getIntent().getExtras();

        ListView lv = getListView();
        lv.setTextFilterEnabled( true );
        lv.setChoiceMode( ListView.CHOICE_MODE_SINGLE );
        lv.setOnItemClickListener( mItemClickListener );
        lv.setOnItemLongClickListener( mItemLongClickListener );
        registerForContextMenu( lv );

        fillData( );
        
        if( getListAdapter() == null || getListAdapter().getCount() == 0 ) {
            Intent i = new Intent( this, editClass );
            startActivityForResult( i, Constants.ACTIVITY_NEW_REQUEST_CODE );
        }

    }

    protected AdapterView.OnItemLongClickListener mItemLongClickListener =
            new AdapterView.OnItemLongClickListener(){
                public boolean onItemLongClick( AdapterView parent, View v, int position, long id )
                {
                    mLongClickId = id;
                    return false;
                }
            };

    @Override
    public void onCreateContextMenu( ContextMenu menu, View v,
            ContextMenu.ContextMenuInfo menuInfo )
    {
        super.onCreateContextMenu( menu, v, menuInfo );
        menu.add( 0, Constants.CONTEXT_MENU_EDIT_ID, 0, mRes.getString(R.string.MENU_EDIT_CAPTION) );
        menu.add( 0, Constants.CONTEXT_MENU_INSERT_ID, 0, mRes.getString(R.string.MENU_ADD_NEW_CAPTION) );
        menu.add( 0, Constants.CONTEXT_MENU_DELETE_ID, 0, mRes.getString(R.string.MENU_DELETE_CAPTION) );
    }
    
    @Override
    public boolean onCreateOptionsMenu( Menu menu )
    {
        optionsMenu = menu;
        optionsMenu.add( 0, Constants.OPTION_MENU_ADD_ID, 0, mRes.getText( R.string.MENU_ADD_NEW_CAPTION ) ).setIcon( mRes.getDrawable( R.drawable.ic_menu_add ) );
        if(!showInactiveRecords)
            optionsMenu.add( 0, Constants.OPTION_MENU_SHOWINACTIVE_ID, 0, mRes.getText( R.string.MENU_SHOWINACTIVE_CAPTION ) ).setIcon( mRes.getDrawable( R.drawable.ic_menu_show_inactive ) );
        else
            optionsMenu.add( 0, Constants.OPTION_MENU_HIDEINACTIVE_ID, 0, mRes.getText( R.string.MENU_HIDEINACTIVE_CAPTION ) ).setIcon( mRes.getDrawable( R.drawable.ic_menu_show_active ) );
        return true;
    }

    @Override
    public boolean onContextItemSelected( MenuItem item )
    {
        SharedPreferences settings = getSharedPreferences( Constants.GLOBAL_PREFERENCE_NAME, 0 );

        switch( item.getItemId() ) {
            case Constants.CONTEXT_MENU_EDIT_ID:
                Intent i = new Intent( this, mEditClass );
                i.putExtra( MainDbAdapter.GEN_COL_ROWID_NAME, mLongClickId );
                if(mTableName.equals(MainDbAdapter.CAR_TABLE_NAME)){
                    i.putExtra( "CurrentCar_ID", settings.getLong( "CurrentCar_ID", -1 ) );
                }
                else if(mTableName.equals(MainDbAdapter.DRIVER_TABLE_NAME)){
                    i.putExtra( "CurrentDriver_ID", settings.getLong( "CurrentDriver_ID", -1 ) );
                }
                else if(mTableName.equals(MainDbAdapter.UOM_TABLE_NAME)){
                    i.putExtra( MainDbAdapter.UOM_COL_UOMTYPE_NAME, extras.getString(MainDbAdapter.UOM_COL_UOMTYPE_NAME));
                    i.putExtra( "Operation", "E" );
                }
                startActivityForResult( i, Constants.ACTIVITY_EDIT_REQUEST_CODE );
                return true;
            case Constants.CONTEXT_MENU_DELETE_ID:
                if(mTableName.equals(MainDbAdapter.CAR_TABLE_NAME)){
                    //check if the car is the selected car. If yes it cannot be deleted.
                    if( mPreferences.getLong( "CurrentCar_ID", -1 ) == mLongClickId ) {
                        AlertDialog.Builder currentCarDeleteAlertBuilder;
                        AlertDialog currentCarDeleteAlert;
                        currentCarDeleteAlertBuilder = new AlertDialog.Builder( this );
                        currentCarDeleteAlertBuilder.setCancelable( false );
                        currentCarDeleteAlertBuilder.setPositiveButton( mRes.getString(R.string.GEN_OK), null );
                        currentCarDeleteAlertBuilder.setMessage( mRes.getString(R.string.CURRENT_CAR_DELETE_ERROR_MESSAGE) );
                        currentCarDeleteAlert = currentCarDeleteAlertBuilder.create();
                        currentCarDeleteAlert.show();
                        return true;
                    }
                }
                if(mTableName.equals(MainDbAdapter.DRIVER_TABLE_NAME)){
                    if( mPreferences.getLong( "CurrentDriver_ID", -1 ) == mLongClickId ) {
                        AlertDialog.Builder currentDriverDeleteAlertBuilder;
                        AlertDialog currentDriverDeleteAlert;
                        currentDriverDeleteAlertBuilder = new AlertDialog.Builder( this );
                        currentDriverDeleteAlertBuilder.setCancelable( false );
                        currentDriverDeleteAlertBuilder.setPositiveButton( mRes.getString(R.string.GEN_OK), null );
                        currentDriverDeleteAlertBuilder.setMessage( mRes.getString(R.string.CURRENT_DRIVER_DELETE_ERROR_MESSAGE) );
                        currentDriverDeleteAlert = currentDriverDeleteAlertBuilder.create();
                        currentDriverDeleteAlert.show();
                        return true;
                    }

                }
                mMainDbHelper.deleteRecord(mTableName, mLongClickId);
                fillData();
                return true;
            case Constants.CONTEXT_MENU_INSERT_ID:
                Intent insertIntent = new Intent( this, mEditClass );
                if(mTableName.equals(MainDbAdapter.UOM_TABLE_NAME)){
                    insertIntent.putExtra( MainDbAdapter.UOM_COL_UOMTYPE_NAME, extras.getString(MainDbAdapter.UOM_COL_UOMTYPE_NAME));
                    insertIntent.putExtra( "Operation", "N");
                }
                startActivityForResult( insertIntent, Constants.ACTIVITY_NEW_REQUEST_CODE );
        }
        return super.onContextItemSelected( item );
    }

    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent intent )
    {
        super.onActivityResult( requestCode, resultCode, intent );
//		Bundle extras = intent.getExtras();

        switch( requestCode ) {
            case Constants.ACTIVITY_NEW_REQUEST_CODE:
                fillData();
                break;
        }
    }

    protected void fillData()
    {
        String tmpWhere = mWhereCondition;
        if(!showInactiveRecords){
            if(tmpWhere != null && tmpWhere.length() > 0)
                tmpWhere = tmpWhere + MainDbAdapter.isActiveWithAndCondition;
            else
                tmpWhere = MainDbAdapter.isActiveCondition;
        }

        recordCursor = mMainDbHelper.fetchForTable( mTableName, mColumns, tmpWhere, mOrderByColumn );
        startManagingCursor( recordCursor );

        setListAdapter( null );

        if( recordCursor.getCount() == 0 ) {
            return;
        }

        SimpleCursorAdapter cursorAdapter =
                new SimpleCursorAdapter( this, mLayoutId, recordCursor, mDbMapFrom, mLayoutIdTo );

        setListAdapter( cursorAdapter );

        if( getListAdapter() != null && getListAdapter().getCount() == 1 ) {
            if(mTableName.equals(MainDbAdapter.CAR_TABLE_NAME)) {
                Cursor selectedRecord = mMainDbHelper.fetchForTable(mTableName, MainDbAdapter.carTableColNames, 
                        null, null);
                selectedRecord.moveToFirst();
                SharedPreferences.Editor editor = mPreferences.edit();
                editor.putLong( "CurrentCar_ID", selectedRecord.getLong(MainDbAdapter.GEN_COL_ROWID_POS));
                editor.putString( "CurrentCar_Name", selectedRecord.getString( MainDbAdapter.GEN_COL_NAME_POS ).trim() );
                editor.putLong("CarUOMLength_ID", selectedRecord.getLong(MainDbAdapter.CAR_COL_UOMLENGTH_ID_POS));
                editor.putLong("CarUOMVolume_ID", selectedRecord.getLong(MainDbAdapter.CAR_COL_UOMVOLUME_ID_POS));
                editor.putLong("CarCurrency_ID", selectedRecord.getLong(MainDbAdapter.CAR_COL_CURRENCY_ID_POS));
                editor.commit();
            }
            else if(mTableName.equals(MainDbAdapter.DRIVER_TABLE_NAME)) {
                Cursor selectedRecord = mMainDbHelper.fetchForTable(mTableName, MainDbAdapter.driverTableColNames,
                        null, null);
                selectedRecord.moveToFirst();
                SharedPreferences.Editor editor = mPreferences.edit();
                editor.putLong( "CurrentDriver_ID", selectedRecord.getLong(MainDbAdapter.GEN_COL_ROWID_POS) );
                editor.putString( "CurrentDriver_Name", selectedRecord.getString( MainDbAdapter.GEN_COL_NAME_POS ).trim() );
                editor.commit();
            }

                    //|| mTableName.equals(MainDbAdapter.DRIVER_TABLE_NAME)){
        }

    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        switch( item.getItemId() ) {
            case Constants.OPTION_MENU_ADD_ID:
                Intent i = new Intent( this, mEditClass );
                if(mTableName.equals(MainDbAdapter.UOM_TABLE_NAME)){
                    i.putExtra( MainDbAdapter.UOM_COL_UOMTYPE_NAME, extras.getString(MainDbAdapter.UOM_COL_UOMTYPE_NAME));
                    i.putExtra( "Operation", "N");
                }
                startActivityForResult( i, Constants.ACTIVITY_NEW_REQUEST_CODE );
                return true;
            case Constants.OPTION_MENU_SHOWINACTIVE_ID:
                showInactiveRecords = true;
                fillData( );
                optionsMenu.removeItem( Constants.OPTION_MENU_SHOWINACTIVE_ID );
                optionsMenu.removeItem( Constants.OPTION_MENU_HIDEINACTIVE_ID );
                optionsMenu.add( 0, Constants.OPTION_MENU_HIDEINACTIVE_ID, 0, mRes.getText( R.string.MENU_HIDEINACTIVE_CAPTION ) ).setIcon( mRes.getDrawable( R.drawable.ic_menu_show_active ) );
                return true;
            case Constants.OPTION_MENU_HIDEINACTIVE_ID:
                showInactiveRecords = false;
                fillData( );
                optionsMenu.removeItem( Constants.OPTION_MENU_SHOWINACTIVE_ID );
                optionsMenu.removeItem( Constants.OPTION_MENU_HIDEINACTIVE_ID );
                optionsMenu.add( 0, Constants.OPTION_MENU_SHOWINACTIVE_ID, 0, mRes.getText( R.string.MENU_SHOWINACTIVE_CAPTION ) ).setIcon( mRes.getDrawable( R.drawable.ic_menu_show_inactive ) );
                return true;
        }
        return true;
    }

}
