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

import android.content.pm.PackageManager.NameNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.andicar.activity.report.RefuelListReportActivity;
import org.andicar.activity.report.MileageListReportActivity;
import org.andicar.activity.miscellaneous.PreferencesActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import org.andicar.utils.StaticValues;
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
import android.text.Html;
import org.andicar.activity.miscellaneous.AboutActivity;
import org.andicar.activity.miscellaneous.BackupRestoreActivity;
import org.andicar.persistence.FileUtils;
import org.andicar.persistence.MainDbAdapter;
import org.andicar.persistence.ReportDbAdapter;

/**
 *
 * @author miki
 */
public class MainActivity extends Activity {

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
    private boolean exitResume = false;
    private String appVersion;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mRes = getResources();
        mPreferences = getSharedPreferences(StaticValues.GLOBAL_PREFERENCE_NAME, 0);
        setContentView(R.layout.main_activity);
        mainContext = this;
        reportDb = new ReportDbAdapter(mainContext, null, null);
        try {
            appVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        }
        catch(NameNotFoundException ex) {
            appVersion = "N/A";
        }
        mileageInsertBtn = (Button) findViewById(R.id.mainActivityBtnInsertMileage);
        mileageInsertBtn.setOnClickListener(btnInsertMileageClickListener);
        refuelInsertBtn = (Button) findViewById(R.id.mainActivityBtnInsertRefuel);
        refuelInsertBtn.setOnClickListener(btnInsertRefuelClickListener);
        mileageListBtn = (Button) findViewById(R.id.mainActivityBtnMileageList);
        mileageListBtn.setOnClickListener(btnMileageListClickListener);
        refuelListBtn = (Button) findViewById(R.id.mainActivityBtnRefuelList);
        refuelListBtn.setOnClickListener(btnRefuelListClickListener);

        threeLineListMileageText1 = (TextView) findViewById(R.id.mainActivityThreeLineListMileageText1);
        threeLineListMileageText2 = (TextView) findViewById(R.id.mainActivityThreeLineListMileageText2);
        threeLineListMileageText3 = (TextView) findViewById(R.id.mainActivityThreeLineListMileageText3);
        threeLineListRefuelText1 = (TextView) findViewById(R.id.mainActivityThreeLineListRefuelText1);
        threeLineListRefuelText2 = (TextView) findViewById(R.id.mainActivityThreeLineListRefuelText2);
        threeLineListRefuelText3 = (TextView) findViewById(R.id.mainActivityThreeLineListRefuelText3);
        if (mPreferences == null || mPreferences.getAll().isEmpty()) { //fresh install
            exitResume = true;
            //test if backups exists
            if (FileUtils.getBkFileNames() != null && !FileUtils.getBkFileNames().isEmpty()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(mRes.getString(R.string.MAIN_ACTIVITY_BACKUPEXISTS));
                builder.setCancelable(false);
                builder.setPositiveButton(mRes.getString(R.string.GEN_YES),
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int id) {
                                Intent i = new Intent(MainActivity.this, BackupRestoreActivity.class);
                                startActivity(i);
                                exitResume = false;
                            }
                        });
                builder.setNegativeButton(mRes.getString(R.string.GEN_NO),
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                exitResume = false;
                                onResume();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();

            } else {
                exitResume = true;
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(mRes.getString(R.string.MAIN_ACTIVITY_WELLCOME_MESSAGE) + "\n"
                        + mRes.getString(R.string.LM_MAIN_ACTIVITY_WELLCOME_MESSAGE2));
                builder.setCancelable(false);
                builder.setPositiveButton(mRes.getString(R.string.GEN_OK),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                exitResume = false;
                                onResume();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(exitResume)
            return;
        if (mPreferences.getBoolean("MustClose", false)) {
            SharedPreferences.Editor editor = mPreferences.edit();
            editor.putBoolean("MustClose", false);
            editor.commit();
            finish();
        }
        ((TextView)findViewById(R.id.mainActivityShortAboutLbl)).setText(Html.fromHtml(
                "<b><i>AndiCar</i></b> is a free and open source car management software for Android powered devices. " +
                "It is licensed under the terms of the GNU General Public License, version 3.<br>" +
                "For more details see the About page.<br>Copyright © 2010 Miklos Keresztes.<br> " +
                "Thank you for using <b><i>AndiCar</i></b>!<br>" +
                "Application version: " + appVersion));
        fillDriverCar();
        Bundle whereConditions = new Bundle();
        whereConditions.putString(
                ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.MILEAGE_TABLE_NAME, MainDbAdapter.MILEAGE_COL_CAR_ID_NAME) + "=",
                String.valueOf(currentCarID));
        reportDb.setReportSql("reportMileageListViewSelect", whereConditions);
        listCursor = reportDb.fetchReport(1);
        if (listCursor.moveToFirst()) {
            threeLineListMileageText1.setText(listCursor.getString(listCursor.getColumnIndex(ReportDbAdapter.FIRST_LINE_LIST_NAME)));
            threeLineListMileageText2.setText(listCursor.getString(listCursor.getColumnIndex(ReportDbAdapter.SECOND_LINE_LIST_NAME)));
            threeLineListMileageText3.setText(listCursor.getString(listCursor.getColumnIndex(ReportDbAdapter.THIRD_LINE_LIST_NAME)));
            mileageListBtn.setEnabled(true);
        } else {
            threeLineListMileageText1.setText(mRes.getString(R.string.MAIN_ACTIVITY_NOMILEAGETEXT));
            threeLineListMileageText2.setText("");
            threeLineListMileageText3.setText("");
            mileageListBtn.setEnabled(false);
        }
        listCursor = null;
        whereConditions.clear();
        whereConditions.putString(
                ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.REFUEL_TABLE_NAME, MainDbAdapter.REFUEL_COL_CAR_ID_NAME) + "=",
                String.valueOf(currentCarID));
        reportDb.setReportSql("reportRefuelListViewSelect", whereConditions);
        listCursor = reportDb.fetchReport(1);
        if (listCursor.moveToFirst()) {
            threeLineListRefuelText1.setText(listCursor.getString(listCursor.getColumnIndex(ReportDbAdapter.FIRST_LINE_LIST_NAME)));
            threeLineListRefuelText2.setText(listCursor.getString(listCursor.getColumnIndex(ReportDbAdapter.SECOND_LINE_LIST_NAME)));
            threeLineListRefuelText3.setText(listCursor.getString(listCursor.getColumnIndex(ReportDbAdapter.THIRD_LINE_LIST_NAME)));
            refuelListBtn.setEnabled(true);
        } else {
            threeLineListRefuelText1.setText(mRes.getString(R.string.MAIN_ACTIVITY_NOREFUELTEXT));
            threeLineListRefuelText2.setText("");
            threeLineListRefuelText3.setText("");
            refuelListBtn.setEnabled(false);
        }
        listCursor = null;
    }
    private OnClickListener btnInsertMileageClickListener = new OnClickListener() {

        public void onClick(View arg0) {
            Intent mileageInsertIntent = new Intent(mainContext, MileageEditActivity.class);
            mileageInsertIntent.putExtra("CurrentDriver_ID", currentDriverID);
            mileageInsertIntent.putExtra("CurrentCar_ID", currentCarID);
            mileageInsertIntent.putExtra("CurrentDriver_Name", currentDriverName);
            mileageInsertIntent.putExtra("CurrentCar_Name", currentCarName);
            mileageInsertIntent.putExtra("Operation", "N");
            startActivityForResult(mileageInsertIntent, ACTIVITY_MILEAGEINSERT_REQUEST_CODE);
        }
    };
    private OnClickListener btnInsertRefuelClickListener = new OnClickListener() {

        public void onClick(View arg0) {
            Intent refuelInsertIntent = new Intent(mainContext, RefuelEditActivity.class);
            refuelInsertIntent.putExtra("CurrentDriver_ID", currentDriverID);
            refuelInsertIntent.putExtra("CurrentCar_ID", currentCarID);
            refuelInsertIntent.putExtra("CurrentDriver_Name", currentDriverName);
            refuelInsertIntent.putExtra("CurrentCar_Name", currentCarName);
            refuelInsertIntent.putExtra("Operation", "N");
            startActivityForResult(refuelInsertIntent, ACTIVITY_REFUELINSERT_REQUEST_CODE);
        }
    };
    private OnClickListener btnMileageListClickListener = new OnClickListener() {

        public void onClick(View arg0) {
            Intent mileageReportIntent = new Intent(mainContext, MileageListReportActivity.class);
            startActivity(mileageReportIntent);
        }
    };
    private OnClickListener btnRefuelListClickListener = new OnClickListener() {

        public void onClick(View arg0) {
            Intent mileageReportIntent = new Intent(mainContext, RefuelListReportActivity.class);
            startActivity(mileageReportIntent);
        }
    };

    private void fillDriverCar() {
        if (mPreferences != null) {
            infoStr = mRes.getString(R.string.CURRENT_DRIVER_NAME);

            //get the current driver id and name
            if (mPreferences.getLong("CurrentDriver_ID", -1) != -1 && !mPreferences.getAll().isEmpty()) {
                currentDriverID = mPreferences.getLong("CurrentDriver_ID", -1);
            } else { //no saved driver. start driver list activity in order to select one.
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(mRes.getString(R.string.MAIN_ACTIVITY_NO_CURRENT_DRIVER));
                builder.setCancelable(false);
                builder.setPositiveButton(mRes.getString(R.string.GEN_OK),
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int id) {
                                Intent i = new Intent(MainActivity.this, DriverListActivity.class);
                                startActivity(i);
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
                mileageListBtn.setEnabled(false);
                mileageInsertBtn.setEnabled(false);
                refuelListBtn.setEnabled(false);
                refuelInsertBtn.setEnabled(false);
                return;
            }

            if (mPreferences.getString("CurrentDriver_Name", "").length() > 0) {
                currentDriverName = mPreferences.getString("CurrentDriver_Name", "");
            }
            infoStr = infoStr + " " + currentDriverName;
            ((TextView) findViewById(R.id.info)).setText(infoStr);

            //get the current car id and name
            if (mPreferences.getLong("CurrentCar_ID", -1) != -1 && !mPreferences.getAll().isEmpty()) {
                currentCarID = mPreferences.getLong("CurrentCar_ID", -1);
            } else { //no saved car. start car list activity in order to select one.
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(mRes.getString(R.string.MAIN_ACTIVITY_NO_CURRENT_CAR));
                builder.setCancelable(false);
                builder.setPositiveButton(mRes.getString(R.string.GEN_OK),
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int id) {
                                Intent i = new Intent(MainActivity.this, CarListActivity.class);
                                startActivity(i);
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
                mileageListBtn.setEnabled(false);
                mileageInsertBtn.setEnabled(false);
                refuelListBtn.setEnabled(false);
                refuelInsertBtn.setEnabled(false);
                return;
            }

            if (mPreferences.getString("CurrentCar_Name", "").length() > 0) {
                currentCarName = mPreferences.getString("CurrentCar_Name", "");
            }
            infoStr = infoStr + "; " + mRes.getString(R.string.CURRENT_CAR_NAME) + " " + currentCarName;
            ((TextView) findViewById(R.id.info)).setText(infoStr);

            if (currentCarID < 0 || currentDriverID < 0) {
                mileageInsertBtn.setEnabled(false);
                mileageListBtn.setEnabled(false);
                refuelInsertBtn.setEnabled(false);
                refuelListBtn.setEnabled(false);
            } else {
                mileageInsertBtn.setEnabled(true);
                mileageListBtn.setEnabled(true);
                refuelInsertBtn.setEnabled(true);
                refuelListBtn.setEnabled(true);
            }
        } else { //no saved mPreferences. start driver list activity in order to create one.
//            Intent i = new Intent( this, DriverListActivity.class );
//            startActivityForResult( i, ACTIVITY_DRIVER_LIST );
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, StaticValues.MENU_PREFERENCES_ID, 0,
                mRes.getText(R.string.MENU_PREFERENCES_CAPTION)).setIcon(mRes.getDrawable(R.drawable.ic_menu_preferences));
//        menu.add( 0, StaticValues.MENU_REPORTS_ID, 0,
//                mRes.getText( R.string.MENU_REPORTS_CAPTION ) )
//                    .setIcon( mRes.getDrawable( R.drawable.ic_menu_report ) );
        menu.add(0, StaticValues.MENU_ABOUT_ID, 0,
                mRes.getText(R.string.MENU_ABOUT_CAPTION)).setIcon(mRes.getDrawable(R.drawable.ic_menu_info_details));
//        menu.add( 0, StaticValues.MENU_HELP_ID, 0,
//                mRes.getText( R.string.MENU_HELP_CAPTION ) )
//                    .setIcon( mRes.getDrawable( R.drawable.ic_menu_help ) );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == StaticValues.MENU_PREFERENCES_ID) {
            Intent i = new Intent(this, PreferencesActivity.class);
            startActivityForResult(i, SETTINGS_ACTIVITY_REQUEST_CODE);
            return true;
        } else if (item.getItemId() == StaticValues.MENU_ABOUT_ID) {
            startActivity(new Intent(this, AboutActivity.class));
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
//		Bundle extras = intent.getExtras();

//        switch( requestCode ) {
//            case SETTINGS_ACTIVITY_REQUEST_CODE:
//                fillDriverCar();
//                break;
//        }
    }
}
