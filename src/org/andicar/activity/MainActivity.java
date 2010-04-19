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
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.andicar.activity.miscellaneous.AboutActivity;
import org.andicar.activity.miscellaneous.BackupRestoreActivity;
import org.andicar.activity.report.ExpensesListReportActivity;
import org.andicar.persistence.FileUtils;
import org.andicar.persistence.MainDbAdapter;
import org.andicar.persistence.ReportDbAdapter;
import org.andicar.service.GPSTrackService;

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
    private int ACTIVITY_EXPENSEINSERT_REQUEST_CODE = 2;
    private SharedPreferences mPreferences;

    private Button mileageListBtn;
    private Button mileageInsertBtn;
    private Button mainActivityBtnStartStopGpsTrack;
    private Button refuelListBtn;
    private Button refuelInsertBtn;
    private Button expenseListBtn;
    private Button expenseInsertBtn;

    private ReportDbAdapter reportDb;
    private Cursor listCursor;
    private TextView threeLineListMileageText1;
    private TextView threeLineListMileageText2;
    private TextView threeLineListMileageText3;
    private TextView threeLineListRefuelText1;
    private TextView threeLineListRefuelText2;
    private TextView threeLineListRefuelText3;
    private TextView threeLineListExpenseText1;
    private TextView threeLineListExpenseText2;
    private TextView threeLineListExpenseText3;
    private TextView threeLineListCarReportText1;
    private TextView threeLineListCarReportText2;
    private TextView threeLineListCarReportText3;

    private boolean exitResume = false;
    private String appVersion;
    private boolean showMileageZone = true;
    private boolean showRefuelZone = true;
    private boolean showExpenseZone = true;
    private boolean showCarReportZone = true;

    private boolean isGpsTrackOn = false;

    @Override
    protected void onPause() {
        super.onPause();
        if(reportDb != null){
            reportDb.close();
            reportDb = null;
        }
    }

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

        String updateMsg = mPreferences.getString("UpdateMsg", null);
        if(updateMsg != null){
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(mRes.getString(R.string.MAIN_ACTIVITY_UPDATE_MESSAG));
            builder.setMessage(updateMsg);
            builder.setCancelable(false);
            builder.setPositiveButton(mRes.getString(R.string.GEN_OK),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
            SharedPreferences.Editor editor = mPreferences.edit();
            editor.remove("UpdateMsg");
            editor.commit();
        }

        mileageListBtn = (Button) findViewById(R.id.mainActivityBtnMileageList);
        mileageListBtn.setOnClickListener(btnMileageListClickListener);
        mileageInsertBtn = (Button) findViewById(R.id.mainActivityBtnInsertMileage);
        mileageInsertBtn.setOnClickListener(btnInsertMileageClickListener);
        mainActivityBtnStartStopGpsTrack = (Button) findViewById(R.id.mainActivityBtnStartStopGpsTrack);
        mainActivityBtnStartStopGpsTrack.setOnClickListener(mStartStopGPStrackListener);
        refuelListBtn = (Button) findViewById(R.id.mainActivityBtnRefuelList);
        refuelListBtn.setOnClickListener(btnRefuelListClickListener);
        refuelInsertBtn = (Button) findViewById(R.id.mainActivityBtnInsertRefuel);
        refuelInsertBtn.setOnClickListener(btnInsertRefuelClickListener);
        expenseListBtn = (Button) findViewById(R.id.mainActivityBtnExpenseList);
        expenseListBtn.setOnClickListener(btnExpenseListClickListener);
        expenseInsertBtn = (Button) findViewById(R.id.mainActivityBtnInsertExpense);
        expenseInsertBtn.setOnClickListener(btnInsertExpenseClickListener);

        threeLineListMileageText1 = (TextView) findViewById(R.id.mainActivityThreeLineListMileageText1);
        threeLineListMileageText2 = (TextView) findViewById(R.id.mainActivityThreeLineListMileageText2);
        threeLineListMileageText3 = (TextView) findViewById(R.id.mainActivityThreeLineListMileageText3);
        threeLineListRefuelText1 = (TextView) findViewById(R.id.mainActivityThreeLineListRefuelText1);
        threeLineListRefuelText2 = (TextView) findViewById(R.id.mainActivityThreeLineListRefuelText2);
        threeLineListRefuelText3 = (TextView) findViewById(R.id.mainActivityThreeLineListRefuelText3);
        threeLineListExpenseText1 = (TextView) findViewById(R.id.mainActivityThreeLineListExpenseText1);
        threeLineListExpenseText2 = (TextView) findViewById(R.id.mainActivityThreeLineListExpenseText2);
        threeLineListExpenseText3 = (TextView) findViewById(R.id.mainActivityThreeLineListExpenseText3);
        threeLineListCarReportText1 = (TextView) findViewById(R.id.mainActivityThreeLineListCarReportText1);
        threeLineListCarReportText2 = (TextView) findViewById(R.id.mainActivityThreeLineListCarReportText2);
        threeLineListCarReportText3 = (TextView) findViewById(R.id.mainActivityThreeLineListCarReportText3);

        if (mPreferences == null || mPreferences.getAll().isEmpty()) { //fresh install
            exitResume = true;
            //test if backups exists
            if (FileUtils.getBkFileNames() != null && !FileUtils.getBkFileNames().isEmpty()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(mRes.getString(R.string.MAIN_ACTIVITY_WELCOMEBACK));
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
                builder.setTitle(mRes.getString(R.string.MAIN_ACTIVITY_WELLCOME_MESSAGE_TITLE));
                builder.setMessage(mRes.getString(R.string.LM_MAIN_ACTIVITY_WELLCOME_MESSAGE2));
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
        
        SharedPreferences.Editor editor = mPreferences.edit();
        if(!mPreferences.contains("MainActivityShowMileage")){
            editor.putBoolean("MainActivityShowMileage", true);
            editor.commit();
        }
        if(!mPreferences.contains("MainActivityShowRefuel")){
            editor.putBoolean("MainActivityShowRefuel", true);
            editor.commit();
        }
        if(!mPreferences.contains("MainActivityShowExpense")){
            editor.putBoolean("MainActivityShowExpense", false);
            editor.commit();
        }
        if(!mPreferences.contains("MainActivityShowCarReport")){
            editor.putBoolean("MainActivityShowCarReport", true);
            editor.commit();
        }
        if(!mPreferences.contains("IsGPSTrackOnMap")){
            editor.putBoolean("IsGPSTrackOnMap", false);
            editor.commit();
        }

        if(!mPreferences.contains("IsUseCSVTrack")){
            editor.putBoolean("IsUseCSVTrack", true);
            editor.commit();
        }
        if(!mPreferences.contains("IsUseKMLTrack")){
            editor.putBoolean("IsUseKMLTrack", true);
            editor.commit();
        }
        if(!mPreferences.contains("IsUseGPXTrack")){
            editor.putBoolean("IsUseGPXTrack", true);
            editor.commit();
        }
        if(!mPreferences.contains("GPSTrackMinTime")){
            editor.putString("GPSTrackMinTime", "0");
            editor.commit();
        }
        if(!mPreferences.contains("GPSTrackMinDistance")){
            editor.putString("GPSTrackMinDistance", "5");
            editor.commit();
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

        if(reportDb == null)
            reportDb = new ReportDbAdapter(mainContext, null, null);

        isGpsTrackOn = mPreferences.getBoolean("isGpsTrackOn", false);

        showMileageZone = mPreferences.getBoolean("MainActivityShowMileage", true);
        showRefuelZone = mPreferences.getBoolean("MainActivityShowRefuel", true);
        showExpenseZone = mPreferences.getBoolean("MainActivityShowExpense", true);
        showCarReportZone = mPreferences.getBoolean("MainActivityShowCarReport", true);

        ((TextView)findViewById(R.id.mainActivityShortAboutLbl)).setText(Html.fromHtml(
                "<b><i>AndiCar</i></b> is a free and open source car management software for Android powered devices. " +
                "It is licensed under the terms of the GNU General Public License, version 3.<br>" +
                "For more details see the About page.<br>Copyright © 2010 Miklos Keresztes.<br> " +
                "Thank you for using <b><i>AndiCar</i></b>!<br>" +
                "Application version: " + appVersion));
        fillDriverCar();

        //fill mileage zone data
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

        if(isGpsTrackOn)
            mainActivityBtnStartStopGpsTrack.setText(mRes.getString(R.string.MAIN_ACTIVITY_GPSTRACKSTOP_BTN_CAPTION));
        else
            mainActivityBtnStartStopGpsTrack.setText(mRes.getString(R.string.MAIN_ACTIVITY_GPSTRACKSTART_BTN_CAPTION));

        //fill refuel zone data
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

        //fill expense zone data
        listCursor = null;
        whereConditions.clear();
        whereConditions.putString(
                ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.EXPENSES_TABLE_NAME,
                    MainDbAdapter.EXPENSES_COL_CAR_ID_NAME) + "=",
                String.valueOf(currentCarID));
        reportDb.setReportSql("reportExpensesListMainViewSelect", whereConditions);
        listCursor = reportDb.fetchReport(1);
        if (listCursor.moveToFirst()) {
            threeLineListExpenseText1.setText(listCursor.getString(listCursor.getColumnIndex(ReportDbAdapter.FIRST_LINE_LIST_NAME)));
            threeLineListExpenseText2.setText(listCursor.getString(listCursor.getColumnIndex(ReportDbAdapter.SECOND_LINE_LIST_NAME)));
            threeLineListExpenseText3.setText(listCursor.getString(listCursor.getColumnIndex(ReportDbAdapter.THIRD_LINE_LIST_NAME)));
            expenseListBtn.setEnabled(true);
        } else {
            threeLineListExpenseText1.setText(mRes.getString(R.string.MAIN_ACTIVITY_NOEXPENSETEXT));
            threeLineListExpenseText2.setText(mRes.getString(R.string.MAIN_ACTIVITY_NOEXPENSE_ADITIONAL_TEXT));
            threeLineListExpenseText3.setText("");
        }

        //fill statistics (car report) zone data
        listCursor = null;
        whereConditions.clear();
        whereConditions.putString(
                ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.CAR_TABLE_NAME,
                    MainDbAdapter.GEN_COL_ROWID_NAME) + "=", String.valueOf(currentCarID));
        reportDb.setReportSql("carReportSelect", whereConditions);
        listCursor = reportDb.fetchReport(1);
        if (listCursor.moveToFirst()) {
            TextView hdrText = (TextView) findViewById(R.id.mainActivityThreeLineListCarReportHdr);
            hdrText.setText(mRes.getString(R.string.MAIN_ACTIVITY_CARREPORT_LISTHDR_CAPTION) +
                    listCursor.getString(1));
            String avgConsUom = listCursor.getString(5);
            if(avgConsUom == null)
                avgConsUom = " N/A ";
            else
                avgConsUom = avgConsUom + " / 100 " + listCursor.getString(6);
            
            String avgConsStr = "";
            String totalFuelStr = listCursor.getString(2);
            String indexCurrentStr = listCursor.getString(3);
            String indexStartStr = listCursor.getString(4);
            BigDecimal mileage = null;
            if(indexCurrentStr != null && indexStartStr != null)
                mileage = (new BigDecimal(indexCurrentStr)).subtract(new BigDecimal(indexStartStr));

            String firstFullRefuelIndexStr = listCursor.getString(9);
            String lastFullRefuelIndexStr = listCursor.getString(10);
            if(firstFullRefuelIndexStr != null && firstFullRefuelIndexStr.length() == 0)
                firstFullRefuelIndexStr = null;
            if(lastFullRefuelIndexStr != null && lastFullRefuelIndexStr.length() == 0)
                lastFullRefuelIndexStr = null;
            if(firstFullRefuelIndexStr != null && lastFullRefuelIndexStr != null){
                BigDecimal firstFullRefuelIndex = new BigDecimal(firstFullRefuelIndexStr);
                BigDecimal lastFullRefuelIndex = new BigDecimal(lastFullRefuelIndexStr);
                if(firstFullRefuelIndex != null && lastFullRefuelIndex != null &&
                        lastFullRefuelIndex.compareTo(firstFullRefuelIndex) > 0){
                    BigDecimal avgConsMileage = (lastFullRefuelIndex).subtract(firstFullRefuelIndex);

                    //avg. fuel consimption
                    if(totalFuelStr == null || totalFuelStr.length() == 0 ||
                            firstFullRefuelIndexStr == null || firstFullRefuelIndexStr.length() == 0 ||
                            lastFullRefuelIndexStr == null || lastFullRefuelIndexStr.length() == 0 ||
                            avgConsMileage == null || avgConsMileage.equals(BigDecimal.ZERO))
                        avgConsStr = mRes.getString(R.string.MAIN_ACTIVITY_CARREPORT_AVGCONS_NODATA);
                    else{
                        BigDecimal totalFuel = new BigDecimal(totalFuelStr);
                        BigDecimal avgCons = BigDecimal.ZERO;
                        avgCons = totalFuel.multiply(new BigDecimal("100"));
                        avgCons = avgCons.divide(avgConsMileage, 10, RoundingMode.HALF_UP)
                                .setScale(StaticValues.amountDecimals, StaticValues.amountRoundingMode);
                        avgConsStr = avgCons.toString() + " " + avgConsUom;
                    }
                }
                else
                    avgConsStr = mRes.getString(R.string.MAIN_ACTIVITY_CARREPORT_AVGCONS_NODATA);
            }
            else
                avgConsStr = mRes.getString(R.string.MAIN_ACTIVITY_CARREPORT_AVGCONS_NODATA);
            threeLineListCarReportText1.setText(mRes.getString(R.string.MAIN_ACTIVITY_CARREPORT_AVGCONS_LABEL) +
                    avgConsStr);

            //total/mileage expenses
            String totalExpensesStr = listCursor.getString(7);
            String mileageExpenseStr = "N/A";
            String carCurrency = "";
            BigDecimal totalExpenses;
            BigDecimal mileageExpense;
            if(totalExpensesStr == null || totalExpensesStr.length() == 0 || mileage == null || mileage.equals(BigDecimal.ZERO)){
                mileageExpenseStr = "N/A";
                totalExpensesStr = "N/A";
            }
            else{
                totalExpenses = new BigDecimal(totalExpensesStr);
                if(totalExpenses != null)
                    totalExpensesStr = totalExpenses
                            .setScale(StaticValues.amountDecimals, StaticValues.amountRoundingMode).toString();
                mileageExpense = totalExpenses.multiply(new BigDecimal("100"));
                mileageExpense = mileageExpense.divide(mileage, 10, RoundingMode.HALF_UP)
                        .setScale(StaticValues.amountDecimals, StaticValues.amountRoundingMode);
                if(mileageExpense != null){
                    carCurrency = listCursor.getString(8);
                    mileageExpenseStr = mileageExpense.toString() + " " + carCurrency + " / 100 " + listCursor.getString(6);
                }
            }
            threeLineListCarReportText2.setText(mRes.getString(R.string.MAIN_ACTIVITY_CARREPORT_TOTALEXP) +
                    " " + totalExpensesStr + " " + carCurrency);
                    
            threeLineListCarReportText3.setText(mRes.getString(R.string.MAIN_ACTIVITY_CARREPORT_MILEAGEEXP) + " " +
                    mileageExpenseStr);
        } else {
            threeLineListCarReportText2.setText("");
            threeLineListCarReportText2.setText("");
            threeLineListCarReportText3.setText("");
        }

        listCursor = null;

        if(!showMileageZone)
            findViewById(R.id.mainActivityMileageZone).setVisibility(View.GONE);
        else
            findViewById(R.id.mainActivityMileageZone).setVisibility(View.VISIBLE);
        if(!showRefuelZone)
            findViewById(R.id.mainActivityRefuelZone).setVisibility(View.GONE);
        else
            findViewById(R.id.mainActivityRefuelZone).setVisibility(View.VISIBLE);
        if(!showExpenseZone)
            findViewById(R.id.mainActivityExpenseZone).setVisibility(View.GONE);
        else
            findViewById(R.id.mainActivityExpenseZone).setVisibility(View.VISIBLE);
        if(!showCarReportZone)
            findViewById(R.id.mainActivityCarReportZone).setVisibility(View.GONE);
        else
            findViewById(R.id.mainActivityCarReportZone).setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(reportDb != null){
            reportDb.close();
            reportDb = null;
        }
    }

    private OnClickListener btnMileageListClickListener = new OnClickListener() {

        public void onClick(View arg0) {
            Intent mileageReportIntent = new Intent(mainContext, MileageListReportActivity.class);
            startActivity(mileageReportIntent);
        }
    };

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

    private OnClickListener btnRefuelListClickListener = new OnClickListener() {

        public void onClick(View arg0) {
            Intent mileageReportIntent = new Intent(mainContext, RefuelListReportActivity.class);
            startActivity(mileageReportIntent);
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

    private OnClickListener btnExpenseListClickListener = new OnClickListener() {

        public void onClick(View arg0) {
            Intent mileageReportIntent = new Intent(mainContext, ExpensesListReportActivity.class);
            startActivity(mileageReportIntent);
        }
    };

    private OnClickListener btnInsertExpenseClickListener = new OnClickListener() {

        public void onClick(View arg0) {
            Intent refuelInsertIntent = new Intent(mainContext, ExpenseEditActivity.class);
            refuelInsertIntent.putExtra("CurrentDriver_ID", currentDriverID);
            refuelInsertIntent.putExtra("CurrentCar_ID", currentCarID);
            refuelInsertIntent.putExtra("Operation", "N");
            startActivityForResult(refuelInsertIntent, ACTIVITY_EXPENSEINSERT_REQUEST_CODE);
        }
    };

    private OnClickListener mStartStopGPStrackListener = new OnClickListener() {
        public void onClick(View v)
        {
            if(isGpsTrackOn){
                stopService(new Intent(MainActivity.this, GPSTrackService.class));
                isGpsTrackOn = false;
                mainActivityBtnStartStopGpsTrack.setText(mRes.getString(R.string.MAIN_ACTIVITY_GPSTRACKSTART_BTN_CAPTION));
            }
            else{
                startService(new Intent(MainActivity.this, GPSTrackService.class));
                isGpsTrackOn = true; //mPreferences.getBoolean("isGpsTrackOn", false); //check if the service is started succesfull
                if(isGpsTrackOn)
                    mainActivityBtnStartStopGpsTrack.setText(
                            mRes.getString(R.string.MAIN_ACTIVITY_GPSTRACKSTOP_BTN_CAPTION));
            }
        };
    };

//    private boolean isGPSTrackingServiceRunning(){
//        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
//        List<ActivityManager.RunningServiceInfo> runningServices = activityManager.getRunningServices(50);
//        String temp;
//        for (int i = 0; i < runningServices.size(); i++) {
//            ActivityManager.RunningServiceInfo runningServiceInfo = runningServices.get(i);
////            if(runningServiceInfo.getClass().equals(GPSTrackService.class))
////                return true;
//            temp = runningServiceInfo.service.getClassName();
//            temp = runningServiceInfo.process;
//        }
//        return false;
//    }
//
    private void fillDriverCar() {
        if (mPreferences != null) {
            infoStr = mRes.getString(R.string.GEN_DRIVER_LABEL);

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
                expenseListBtn.setEnabled(false);
                expenseInsertBtn.setEnabled(false);
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
                expenseListBtn.setEnabled(false);
                expenseInsertBtn.setEnabled(false);
                return;
            }

            if (mPreferences.getString("CurrentCar_Name", "").length() > 0) {
                currentCarName = mPreferences.getString("CurrentCar_Name", "");
            }
            infoStr = infoStr + "; " + mRes.getString(R.string.GEN_CAR_LABEL) + " " + currentCarName;
            ((TextView) findViewById(R.id.info)).setText(infoStr);

            if (currentCarID < 0 || currentDriverID < 0) {
                mileageInsertBtn.setEnabled(false);
                mileageListBtn.setEnabled(false);
                refuelInsertBtn.setEnabled(false);
                refuelListBtn.setEnabled(false);
                expenseListBtn.setEnabled(false);
                expenseInsertBtn.setEnabled(false);
            } else {
                mileageInsertBtn.setEnabled(true);
                mileageListBtn.setEnabled(true);
                refuelInsertBtn.setEnabled(true);
                refuelListBtn.setEnabled(true);
                expenseListBtn.setEnabled(true);
                expenseInsertBtn.setEnabled(true);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, StaticValues.MENU_PREFERENCES_ID, 0,
                mRes.getText(R.string.MENU_PREFERENCES_CAPTION)).setIcon(mRes.getDrawable(R.drawable.ic_menu_preferences));
        menu.add(0, StaticValues.MENU_ABOUT_ID, 0,
                mRes.getText(R.string.MENU_ABOUT_CAPTION)).setIcon(mRes.getDrawable(R.drawable.ic_menu_info_details));
        menu.add(0, StaticValues.MENU_MILEAGE_ID, 0,
                mRes.getText(R.string.MENU_MILEAGE_CAPTION)).setIcon(mRes.getDrawable(R.drawable.ic_menu_mileage));
        menu.add(0, StaticValues.MENU_REFUEL_ID, 0,
                mRes.getText(R.string.MENU_REFUEL_CAPTION)).setIcon(mRes.getDrawable(R.drawable.ic_menu_refuel));
        menu.add(0, StaticValues.MENU_EXPENSES_ID, 0,
                mRes.getText(R.string.MENU_EXPENSES_CAPTION)).setIcon(mRes.getDrawable(R.drawable.ic_menu_expenses));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == StaticValues.MENU_PREFERENCES_ID) {
            startActivity(new Intent(this, PreferencesActivity.class));
//            Intent i = new Intent(this, PreferencesActivity.class);
//            startActivityForResult(i, SETTINGS_ACTIVITY_REQUEST_CODE);
//            return true;
        } else if (item.getItemId() == StaticValues.MENU_ABOUT_ID) {
            startActivity(new Intent(this, AboutActivity.class));
        } else if (item.getItemId() == StaticValues.MENU_MILEAGE_ID) {
            startActivity(new Intent(this, MileageListReportActivity.class));
        } else if (item.getItemId() == StaticValues.MENU_REFUEL_ID) {
            startActivity(new Intent(this, RefuelListReportActivity.class));
        } else if (item.getItemId() == StaticValues.MENU_EXPENSES_ID) {
            startActivity(new Intent(this, ExpensesListReportActivity.class));
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
