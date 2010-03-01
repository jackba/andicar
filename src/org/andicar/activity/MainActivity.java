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

import android.app.Activity;
import android.content.Context;
import android.view.View;
import org.andicar.utils.Constants;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.database.Cursor;
import org.andicar.persistence.MainDbAdapter;
import org.andicar.persistence.ReportDbAdapter;

/**
 *
 * @author miki
 */
public class MainActivity extends Activity
{
    private Resources mRes = null;

    private long currentDriverID = -1;
    private String currentDriverName = "";
    private long currentCarID = -1;
    private String currentCarName = "";
    private String infoStr = "";
    private Context mainContext;
    private int ACTIVITY_MILEAGEINSERT_REQUEST_CODE = 0;
    private int ACTIVITY_REFUELINSERT_REQUEST_CODE = 1;

    private SharedPreferences mPreferences;
    private Button mileageInsertBtn;
    private Button refuelInsertBtn;
    private Button mileageListBtn;
    private Button refuelListBtn;
    private ReportDbAdapter reportDb;
    private Cursor listCursor;

    private TextView threeLineListMileageText1;
    private TextView threeLineListMileageText2;
    private TextView threeLineListMileageText3;
    private TextView threeLineListRefuelText1;
    private TextView threeLineListRefuelText2;
    private TextView threeLineListRefuelText3;

    private static final int SETTINGS_ACTIVITY_REQUEST_CODE = 0;

    /** Called when the activity is first created. */
    @Override
    public void onCreate( Bundle icicle )
    {
        super.onCreate( icicle );

        mRes = getResources();
        mPreferences = getSharedPreferences( Constants.GLOBAL_PREFERENCE_NAME, 0 );
        setContentView( R.layout.main_activity );
        mainContext = this;
        reportDb = new ReportDbAdapter(mainContext, null, null);
        
        mileageInsertBtn = (Button) findViewById(R.id.mainActivityBtnInsertMileage);
        mileageInsertBtn.setOnClickListener(btnInsertMileageClickListener);
        refuelInsertBtn = (Button) findViewById(R.id.mainActivityBtnInsertRefuel);
        refuelInsertBtn.setOnClickListener(btnInsertRefuelClickListener);
        mileageListBtn = (Button) findViewById(R.id.mainActivityBtnMileageList);
        mileageListBtn.setOnClickListener(btnMileageListClickListener);
        refuelListBtn = (Button) findViewById(R.id.mainActivityBtnRefuelList);
        refuelListBtn.setOnClickListener(btnRefuelListClickListener);

        threeLineListMileageText1 = (TextView)findViewById(R.id.mainActivityThreeLineListMileageText1);
        threeLineListMileageText2 = (TextView)findViewById(R.id.mainActivityThreeLineListMileageText2);
        threeLineListMileageText3 = (TextView)findViewById(R.id.mainActivityThreeLineListMileageText3);
        threeLineListRefuelText1 = (TextView)findViewById(R.id.mainActivityThreeLineListRefuelText1);
        threeLineListRefuelText2 = (TextView)findViewById(R.id.mainActivityThreeLineListRefuelText2);
        threeLineListRefuelText3 = (TextView)findViewById(R.id.mainActivityThreeLineListRefuelText3);

//        fillDriverCar();
}

    @Override
    protected void onResume() {
        super.onResume();
        fillDriverCar();
        Bundle whereConditions = new Bundle();
        whereConditions.putString(
                ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.MILEAGE_TABLE_NAME, MainDbAdapter.MILEAGE_COL_CAR_ID_NAME) + "=",
                String.valueOf(currentCarID) );
        reportDb.setReportSql("reportMileageListViewSelect", whereConditions);
        listCursor = reportDb.fetchReport(1);
        if(listCursor.moveToFirst()){
            threeLineListMileageText1.setText(listCursor.getString(listCursor.getColumnIndex(ReportDbAdapter.FIRST_LINE_LIST_NAME)));
            threeLineListMileageText2.setText(listCursor.getString(listCursor.getColumnIndex(ReportDbAdapter.SECOND_LINE_LIST_NAME)));
            threeLineListMileageText3.setText(listCursor.getString(listCursor.getColumnIndex(ReportDbAdapter.THIRD_LINE_LIST_NAME)));
            mileageListBtn.setEnabled(true);
        }
        else{
            threeLineListMileageText1.setText(mRes.getString(R.string.MAIN_ACTIVITY_NOMILEAGETEXT));
            threeLineListMileageText2.setText("");
            threeLineListMileageText3.setText("");
            mileageListBtn.setEnabled(false);
        }
        listCursor = null;
        whereConditions.clear();
        whereConditions.putString(
                ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.REFUEL_TABLE_NAME, MainDbAdapter.REFUEL_COL_CAR_ID_NAME) + "=",
                String.valueOf(currentCarID) );
        reportDb.setReportSql("reportRefuelListViewSelect", whereConditions);
        listCursor = reportDb.fetchReport(1);
        if(listCursor.moveToFirst()){
            threeLineListRefuelText1.setText(listCursor.getString(listCursor.getColumnIndex(ReportDbAdapter.FIRST_LINE_LIST_NAME)));
            threeLineListRefuelText2.setText(listCursor.getString(listCursor.getColumnIndex(ReportDbAdapter.SECOND_LINE_LIST_NAME)));
            threeLineListRefuelText3.setText(listCursor.getString(listCursor.getColumnIndex(ReportDbAdapter.THIRD_LINE_LIST_NAME)));
            refuelListBtn.setEnabled(true);
        }
        else{
            threeLineListRefuelText1.setText(mRes.getString(R.string.MAIN_ACTIVITY_NOREFUELTEXT));
            threeLineListRefuelText2.setText("");
            threeLineListRefuelText3.setText("");
            refuelListBtn.setEnabled(false);
        }
        listCursor = null;
    }


    private OnClickListener btnInsertMileageClickListener =  new OnClickListener() {
        public void onClick(View arg0) {
            Intent mileageInsertIntent = new Intent(mainContext, MileageEditActivity.class);
            mileageInsertIntent.putExtra("CurrentDriver_ID", currentDriverID);
            mileageInsertIntent.putExtra("CurrentCar_ID", currentCarID);
            mileageInsertIntent.putExtra("CurrentDriver_Name", currentDriverName);
            mileageInsertIntent.putExtra("CurrentCar_Name", currentCarName);
            mileageInsertIntent.putExtra( "Operation", "N" );
            startActivityForResult( mileageInsertIntent, ACTIVITY_MILEAGEINSERT_REQUEST_CODE );
        }
    };

    private OnClickListener btnInsertRefuelClickListener =  new OnClickListener() {
        public void onClick(View arg0) {
            Intent refuelInsertIntent = new Intent(mainContext, RefuelEditActivity.class);
            refuelInsertIntent.putExtra("CurrentDriver_ID", currentDriverID);
            refuelInsertIntent.putExtra("CurrentCar_ID", currentCarID);
            refuelInsertIntent.putExtra("CurrentDriver_Name", currentDriverName);
            refuelInsertIntent.putExtra("CurrentCar_Name", currentCarName);
            refuelInsertIntent.putExtra( "Operation", "N" );
            startActivityForResult( refuelInsertIntent, ACTIVITY_REFUELINSERT_REQUEST_CODE );
        }
    };

    private OnClickListener btnMileageListClickListener =  new OnClickListener() {
        public void onClick(View arg0) {
            Intent mileageReportIntent = new Intent(mainContext, MileageListReportActivity.class);
            startActivity( mileageReportIntent );
        }
    };

    private OnClickListener btnRefuelListClickListener =  new OnClickListener() {
        public void onClick(View arg0) {
            Intent mileageReportIntent = new Intent(mainContext, RefuelListReportActivity.class);
            startActivity( mileageReportIntent );
        }
    };

    private void fillDriverCar()
    {
        if( mPreferences != null ) {
            infoStr = mRes.getString( R.string.CURRENT_DRIVER_NAME );

            //get the current driver id and name
            if( mPreferences.getLong( "CurrentDriver_ID", -1 ) != -1 ) {
                currentDriverID = mPreferences.getLong( "CurrentDriver_ID", -1 );
            }
            else { //no saved driver. start driver list activity in order to select one.
//                Intent i = new Intent( this, DriverListActivity.class );
//                startActivityForResult( i, ACTIVITY_DRIVER_LIST );
//                return;
            }

            if( mPreferences.getString( "CurrentDriver_Name", "" ).length() > 0 ) {
                currentDriverName = mPreferences.getString( "CurrentDriver_Name", "" );
            }
            infoStr = infoStr + " " + currentDriverName;
            ((TextView) findViewById( R.id.info )).setText( infoStr );

            //get the current car id and name
            if( mPreferences.getLong( "CurrentCar_ID", -1 ) != -1 ) {
                currentCarID = mPreferences.getLong( "CurrentCar_ID", -1 );
            }
            else { //no saved car. start car list activity in order to select one.
//                Intent i = new Intent( this, CarListActivity.class );
//                startActivityForResult( i, ACTIVITY_CAR_LIST );
//                return;
            }

            if( mPreferences.getString( "CurrentCar_Name", "" ).length() > 0 ) {
                currentCarName = mPreferences.getString( "CurrentCar_Name", "" );
            }
            infoStr = infoStr + "; " + mRes.getString( R.string.CURRENT_CAR_NAME ) + " " + currentCarName;
            ((TextView) findViewById( R.id.info )).setText( infoStr );

            if(currentCarID < 0 || currentDriverID < 0){
                mileageInsertBtn.setEnabled(false);
                mileageListBtn.setEnabled(false);
                refuelInsertBtn.setEnabled(false);
                refuelListBtn.setEnabled(false);
            }
            else{
                mileageInsertBtn.setEnabled(true);
                mileageListBtn.setEnabled(true);
                refuelInsertBtn.setEnabled(true);
                refuelListBtn.setEnabled(true);
            }
        }
        else { //no saved mPreferences. start driver list activity in order to create one.
//            Intent i = new Intent( this, DriverListActivity.class );
//            startActivityForResult( i, ACTIVITY_DRIVER_LIST );
        }
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu )
    {
        menu.add( 0, Constants.MENU_PREFERENCES_ID, 0,
                mRes.getText( R.string.MENU_PREFERENCES_CAPTION ) ).setIcon( mRes.getDrawable( R.drawable.ic_menu_preferences ) );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        if( item.getItemId() == Constants.MENU_PREFERENCES_ID ) {
                Intent i = new Intent( this, PreferencesActivity.class );
                startActivityForResult( i, SETTINGS_ACTIVITY_REQUEST_CODE );
                return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent intent )
    {
        super.onActivityResult( requestCode, resultCode, intent );
//		Bundle extras = intent.getExtras();

//        switch( requestCode ) {
//            case SETTINGS_ACTIVITY_REQUEST_CODE:
//                fillDriverCar();
//                break;
//        }
    }
}
