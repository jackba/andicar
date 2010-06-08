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

import org.andicar.activity.miscellaneous.GPSTrackMap;
import org.andicar.activity.miscellaneous.GPSTrackController;
import android.content.pm.PackageManager.NameNotFoundException;
import org.andicar.activity.report.RefuelListReportActivity;
import org.andicar.activity.report.MileageListReportActivity;
import org.andicar.activity.preference.AndiCarPreferencesActivity;
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
import android.widget.TextView;
import android.database.Cursor;
import android.text.Html;
import android.widget.ImageButton;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.andicar.activity.miscellaneous.AboutActivity;
import org.andicar.activity.miscellaneous.BackupRestoreActivity;
import org.andicar.activity.report.ExpensesListReportActivity;
import org.andicar.activity.report.GPSTrackListReportActivity;
import org.andicar.persistence.FileUtils;
import org.andicar.persistence.MainDbAdapter;
import org.andicar.persistence.ReportDbAdapter;
import org.andicar.utils.AndiCarExceptionHandler;
import org.andicar.utils.AndiCarStatistics;
import org.andicar.utils.Utils;

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

    private ImageButton btnMileageList;
    private ImageButton btnMileageInsert;
    private ImageButton btnGPSTrackList;
    private ImageButton btnGPSTrackInsert;
    private ImageButton btnGPSTrackShowOnMap;
    private ImageButton btnRefuelList;
    private ImageButton btnRefuelInsert;
    private ImageButton btnExpenseList;
    private ImageButton btnExpenseInsert;

    private ReportDbAdapter reportDb;
    private Cursor listCursor;
    private TextView tvThreeLineListMileageText1;
    private TextView tvThreeLineListMileageText2;
    private TextView tvThreeLineListMileageText3;
    private TextView tvThreeLineListGPSTrackText1;
    private TextView tvThreeLineListGPSTrackText2;
    private TextView tvThreeLineListGPSTrackText3;
    private TextView tvThreeLineListRefuelText1;
    private TextView tvThreeLineListRefuelText2;
    private TextView tvThreeLineListRefuelText3;
    private TextView tvThreeLineListExpenseText1;
    private TextView tvThreeLineListExpenseText2;
    private TextView tvThreeLineListExpenseText3;
    private TextView tvThreeLineListStatisticsText1;
    private TextView tvThreeLineListStatisticsText2;
    private TextView tvThreeLineListStatisticsText3;

    private TextView tvStatisticsHdr;

    private boolean exitResume = false;
    private String appVersion;
    private boolean showMileageZone = true;
    private boolean showGPSTrackZone = true;
    private boolean showRefuelZone = true;
    private boolean showExpenseZone = true;
    private boolean showStatistcsZone = true;

    private boolean isSendStatistics = true;
    private boolean isSendCrashReport;
    private long gpsTrackId = -1;

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

        mPreferences = getSharedPreferences(StaticValues.GLOBAL_PREFERENCE_NAME, 0);
        isSendStatistics = mPreferences.getBoolean("SendUsageStatistics", true);
        isSendCrashReport = mPreferences.getBoolean("SendCrashReport", true);
        if(isSendCrashReport)
            Thread.setDefaultUncaughtExceptionHandler(
                    new AndiCarExceptionHandler(Thread.getDefaultUncaughtExceptionHandler(), this));

        mRes = getResources();

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
            if(!updateMsg.equals("VersionChanged")){
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(mRes.getString(R.string.MainActivity_UpdateMessage));
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
            else
                initPreferenceValues(); //version update => init (new) preference values
        }

        btnMileageList = (ImageButton) findViewById(R.id.btnMileageList);
        btnMileageList.setOnClickListener(btnMileageListClickListener);
        btnMileageInsert = (ImageButton) findViewById(R.id.btnMileageInsert);
        btnMileageInsert.setOnClickListener(btnInsertMileageClickListener);
        btnRefuelList = (ImageButton) findViewById(R.id.btnRefuelList);
        btnRefuelList.setOnClickListener(btnRefuelListClickListener);
        btnRefuelInsert = (ImageButton) findViewById(R.id.btnRefuelInsert);
        btnRefuelInsert.setOnClickListener(btnInsertRefuelClickListener);
        btnExpenseList = (ImageButton) findViewById(R.id.btnExpenseList);
        btnExpenseList.setOnClickListener(btnExpenseListClickListener);
        btnExpenseInsert = (ImageButton) findViewById(R.id.btnExpenseInsert);
        btnExpenseInsert.setOnClickListener(btnInsertExpenseClickListener);
        btnGPSTrackInsert = (ImageButton) findViewById(R.id.btnGPSTrackInsert);
        btnGPSTrackInsert.setOnClickListener(btnGPSTrackInsertClickListener);
        btnGPSTrackList = (ImageButton) findViewById(R.id.btnGPSTrackList);
        btnGPSTrackList.setOnClickListener(btnGPSTrackListClickListener);
        btnGPSTrackShowOnMap = (ImageButton) findViewById(R.id.btnGPSTrackShowOnMap);
        btnGPSTrackShowOnMap.setOnClickListener(btnGPSTrackShowClickListener);

        tvThreeLineListMileageText1 = (TextView) findViewById(R.id.tvThreeLineListMileageText1);
        tvThreeLineListMileageText2 = (TextView) findViewById(R.id.tvThreeLineListMileageText2);
        tvThreeLineListMileageText3 = (TextView) findViewById(R.id.tvThreeLineListMileageText3);
        tvThreeLineListGPSTrackText1 = (TextView) findViewById(R.id.tvThreeLineListGPSTrackText1);
        tvThreeLineListGPSTrackText2 = (TextView) findViewById(R.id.tvThreeLineListGPSTrackText2);
        tvThreeLineListGPSTrackText3 = (TextView) findViewById(R.id.tvThreeLineListGPSTrackText3);
        tvThreeLineListRefuelText1 = (TextView) findViewById(R.id.tvThreeLineListRefuelText1);
        tvThreeLineListRefuelText2 = (TextView) findViewById(R.id.tvThreeLineListRefuelText2);
        tvThreeLineListRefuelText3 = (TextView) findViewById(R.id.tvThreeLineListRefuelText3);
        tvThreeLineListExpenseText1 = (TextView) findViewById(R.id.tvThreeLineListExpenseText1);
        tvThreeLineListExpenseText2 = (TextView) findViewById(R.id.tvThreeLineListExpenseText2);
        tvThreeLineListExpenseText3 = (TextView) findViewById(R.id.tvThreeLineListExpenseText3);
        tvStatisticsHdr = (TextView) findViewById(R.id.tvStatisticsHdr);
        tvThreeLineListStatisticsText1 = (TextView) findViewById(R.id.tvThreeLineListStatisticsText1);
        tvThreeLineListStatisticsText2 = (TextView) findViewById(R.id.tvThreeLineListStatisticsText2);
        tvThreeLineListStatisticsText3 = (TextView) findViewById(R.id.tvThreeLineListStatisticsText3);


        if (mPreferences == null || mPreferences.getAll().isEmpty()) { //fresh install
            exitResume = true;
            //test if backups exists
            if (FileUtils.getFileNames(StaticValues.BACKUP_FOLDER, null) != null && !FileUtils.getFileNames(StaticValues.BACKUP_FOLDER, null).isEmpty()) {
                initPreferenceValues(); //version update => init (new) preference values
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(mRes.getString(R.string.MainActivity_WellcomeBackMessage));
                builder.setMessage(mRes.getString(R.string.MainActivity_BackupExistMessage));
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
                initPreferenceValues(); //version update => init (new) preference values
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(mRes.getString(R.string.MainActivity_WellcomeMessage));
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
    }

    private void fillExpenseZone() {
        listCursor = null;
        Bundle whereConditions = new Bundle();
        whereConditions.putString(ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.EXPENSES_TABLE_NAME, MainDbAdapter.EXPENSES_COL_CAR_ID_NAME) + "=", String.valueOf(currentCarID));
        reportDb.setReportSql("reportExpensesListMainViewSelect", whereConditions);
        listCursor = reportDb.fetchReport(1);
        if(listCursor != null && listCursor.moveToFirst()) {
            tvThreeLineListExpenseText1.setText(listCursor.getString(listCursor.getColumnIndex(ReportDbAdapter.FIRST_LINE_LIST_NAME)));
            tvThreeLineListExpenseText2.setText(listCursor.getString(listCursor.getColumnIndex(ReportDbAdapter.SECOND_LINE_LIST_NAME)));
            tvThreeLineListExpenseText3.setText(listCursor.getString(listCursor.getColumnIndex(ReportDbAdapter.THIRD_LINE_LIST_NAME)));
            btnExpenseList.setEnabled(true);
        }
        else {
            tvThreeLineListExpenseText1.setText(mRes.getString(R.string.MainActivity_ExpenseNoDataText));
            tvThreeLineListExpenseText2.setText(mRes.getString(R.string.MainActivity_ExpenseNoDataAditionalText));
            tvThreeLineListExpenseText3.setText("");
            btnExpenseList.setEnabled(false);
        }
        if(listCursor != null)
            listCursor.close();
    }

    private void fillGpsZone() {
        listCursor = null;
        Bundle whereConditions = new Bundle();
        whereConditions.clear();
        whereConditions.putString(
                ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.GPSTRACK_TABLE_NAME, MainDbAdapter.GPSTRACK_COL_CAR_ID_NAME) + "=",
                String.valueOf(currentCarID));
        reportDb.setReportSql("gpsTrackListViewSelect", whereConditions);
        listCursor = reportDb.fetchReport(1);
        gpsTrackId = -1;
        if (listCursor != null && listCursor.moveToFirst()) {
            gpsTrackId = listCursor.getLong(listCursor.getColumnIndex(ReportDbAdapter.GEN_COL_ROWID_NAME));
            tvThreeLineListGPSTrackText1.setText(
                    listCursor.getString(listCursor.getColumnIndex(ReportDbAdapter.FIRST_LINE_LIST_NAME)));
            tvThreeLineListGPSTrackText2.setText(
                    listCursor.getString(listCursor.getColumnIndex(ReportDbAdapter.SECOND_LINE_LIST_NAME))
                    .replace("[%1]", mRes.getString(R.string.GPSTrackReport_GPSTrackVar_1))
                    .replace("[%2]", mRes.getString(R.string.GPSTrackReport_GPSTrackVar_2))
                    .replace("[%3]", mRes.getString(R.string.GPSTrackReport_GPSTrackVar_3))
                    .replace("[%4]", mRes.getString(R.string.GPSTrackReport_GPSTrackVar_4))
                    .replace("[%5]", mRes.getString(R.string.GPSTrackReport_GPSTrackVar_5) +
                            Utils.getTimeString(listCursor.getLong(listCursor.getColumnIndex(ReportDbAdapter.FOURTH_LINE_LIST_NAME)), false))
                    .replace("[%6]", mRes.getString(R.string.GPSTrackReport_GPSTrackVar_6) +
                            Utils.getTimeString(listCursor.getLong(listCursor.getColumnIndex(ReportDbAdapter.FIFTH_LINE_LIST_NAME)), false))
                    .replace("[%7]", mRes.getString(R.string.GPSTrackReport_GPSTrackVar_7))
                    .replace("[%8]", mRes.getString(R.string.GPSTrackReport_GPSTrackVar_8))
                    .replace("[%9]", mRes.getString(R.string.GPSTrackReport_GPSTrackVar_9))
                    .replace("[%10]", mRes.getString(R.string.GPSTrackReport_GPSTrackVar_10))
                    .replace("[%11]", mRes.getString(R.string.GPSTrackReport_GPSTrackVar_11))

                    );
            tvThreeLineListGPSTrackText3.setText(listCursor.getString(listCursor.getColumnIndex(ReportDbAdapter.THIRD_LINE_LIST_NAME)));
            btnGPSTrackList.setEnabled(true);
            btnGPSTrackShowOnMap.setEnabled(true);
        } else {
            tvThreeLineListGPSTrackText1.setText(mRes.getString(R.string.MainActivity_GPSTrackZoneNoDataText));
            tvThreeLineListGPSTrackText2.setText("");
            tvThreeLineListGPSTrackText3.setText("");
            btnGPSTrackList.setEnabled(false);
            btnGPSTrackShowOnMap.setEnabled(false);
        }
        if(listCursor != null)
            listCursor.close();
    }

    private void fillMileageZone() {
        listCursor = null;
        Bundle whereConditions = new Bundle();
        whereConditions = new Bundle();
        whereConditions.putString(ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.MILEAGE_TABLE_NAME, MainDbAdapter.MILEAGE_COL_CAR_ID_NAME) + "=", String.valueOf(currentCarID));
        reportDb.setReportSql("reportMileageListViewSelect", whereConditions);
        listCursor = reportDb.fetchReport(1);
        if(listCursor != null && listCursor.moveToFirst()) {
            tvThreeLineListMileageText1.setText(listCursor.getString(listCursor.getColumnIndex(ReportDbAdapter.FIRST_LINE_LIST_NAME)));
            tvThreeLineListMileageText2.setText(listCursor.getString(listCursor.getColumnIndex(ReportDbAdapter.SECOND_LINE_LIST_NAME)));
            tvThreeLineListMileageText3.setText(listCursor.getString(listCursor.getColumnIndex(ReportDbAdapter.THIRD_LINE_LIST_NAME)));
            btnMileageList.setEnabled(true);
        }
        else {
            tvThreeLineListMileageText1.setText(mRes.getString(R.string.MainActivity_MileageNoDataText));
            tvThreeLineListMileageText2.setText("");
            tvThreeLineListMileageText3.setText("");
            btnMileageList.setEnabled(false);
        }
        if(listCursor != null)
            listCursor.close();
    }

    private void fillRefuelZone() {
        listCursor = null;
        Bundle whereConditions = new Bundle();
        whereConditions.putString(ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.REFUEL_TABLE_NAME, MainDbAdapter.REFUEL_COL_CAR_ID_NAME) + "=", String.valueOf(currentCarID));
        reportDb.setReportSql("reportRefuelListViewSelect", whereConditions);
        listCursor = reportDb.fetchReport(1);
        if(listCursor != null && listCursor.moveToFirst()) {
            tvThreeLineListRefuelText1.setText(listCursor.getString(listCursor.getColumnIndex(ReportDbAdapter.FIRST_LINE_LIST_NAME)));
            tvThreeLineListRefuelText2.setText(listCursor.getString(listCursor.getColumnIndex(ReportDbAdapter.SECOND_LINE_LIST_NAME)));
            tvThreeLineListRefuelText3.setText(listCursor.getString(listCursor.getColumnIndex(ReportDbAdapter.THIRD_LINE_LIST_NAME)));
            btnRefuelList.setEnabled(true);
        }
        else {
            tvThreeLineListRefuelText1.setText(mRes.getString(R.string.MainActivity_RefuelNoDataText));
            tvThreeLineListRefuelText2.setText("");
            tvThreeLineListRefuelText3.setText("");
            btnRefuelList.setEnabled(false);
        }
        if(listCursor != null)
            listCursor.close();
    }

    private void fillStatisticsZone(){
        listCursor = null;
        Bundle whereConditions = new Bundle();
        whereConditions.putString(ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.CAR_TABLE_NAME, MainDbAdapter.GEN_COL_ROWID_NAME) + "=", String.valueOf(currentCarID));
        reportDb.setReportSql("statisticsMainViewSelect", whereConditions);
        listCursor = reportDb.fetchReport(1);
        if(listCursor != null && listCursor.moveToFirst()) {
            TextView tvHdrText = (TextView) findViewById(R.id.tvThreeLineListCarReportHdr);
            tvHdrText.setText(mRes.getString(R.string.MainActivity_StatisticsListHeaderCaption) + listCursor.getString(1));
            String avgConsUom = listCursor.getString(5);
            if(avgConsUom == null) {
                avgConsUom = " N/A ";
            }
            else {
                avgConsUom = avgConsUom + " / 100 " + listCursor.getString(6);
            }
            String avgConsStr = "";
            String totalFuelStr = listCursor.getString(2);
            String indexCurrentStr = listCursor.getString(3);
            String indexStartStr = listCursor.getString(4);
            BigDecimal mileage = null;
            if(indexCurrentStr != null && indexStartStr != null) {
                try{
                    mileage = (new BigDecimal(indexCurrentStr)).subtract(new BigDecimal(indexStartStr));
                    tvStatisticsHdr.setText(mRes.getString(R.string.MainActivity_StatisticsHeaderCaption) + " " + mileage.toString() + " " + listCursor.getString(6));
                }
                catch(NumberFormatException e){
                    mileage = null;
                }
            }
            String firstFullRefuelIndexStr = listCursor.getString(9);
            String lastFullRefuelIndexStr = listCursor.getString(10);
            if(firstFullRefuelIndexStr != null && firstFullRefuelIndexStr.length() == 0) {
                firstFullRefuelIndexStr = null;
            }
            if(lastFullRefuelIndexStr != null && lastFullRefuelIndexStr.length() == 0) {
                lastFullRefuelIndexStr = null;
            }
            if(firstFullRefuelIndexStr != null && lastFullRefuelIndexStr != null) {
                try{
                    BigDecimal firstFullRefuelIndex = new BigDecimal(firstFullRefuelIndexStr);
                    BigDecimal lastFullRefuelIndex = new BigDecimal(lastFullRefuelIndexStr);
                    if(firstFullRefuelIndex != null && lastFullRefuelIndex != null && lastFullRefuelIndex.compareTo(firstFullRefuelIndex) > 0) {
                        BigDecimal avgConsMileage = (lastFullRefuelIndex).subtract(firstFullRefuelIndex);
                        //avg. fuel consimption
                        if(totalFuelStr == null || totalFuelStr.length() == 0 || firstFullRefuelIndexStr == null ||
                                firstFullRefuelIndexStr.length() == 0 || lastFullRefuelIndexStr == null ||
                                lastFullRefuelIndexStr.length() == 0 || avgConsMileage == null ||
                                avgConsMileage.equals(BigDecimal.ZERO)) {
                            avgConsStr = mRes.getString(R.string.MainActivity_StatisticsAvgConsNoDataText);
                        }
                        else {
                            BigDecimal totalFuel = new BigDecimal(totalFuelStr);
                            BigDecimal avgCons = BigDecimal.ZERO;
                            avgCons = totalFuel.multiply(new BigDecimal("100"));
                            avgCons = avgCons.divide(avgConsMileage, 10, RoundingMode.HALF_UP).setScale(StaticValues.DECIMALS_AMOUNT, StaticValues.ROUNDING_MODE_AMOUNT);
                            //consumption: x uom volume (l or galon) / 100 uom length (km or mi)
                            avgConsStr = avgCons.toString() + " " + avgConsUom;
                            //efficienty: x uom length (km or mi) / uom volume (l or galon)
                            if(avgCons != null && avgCons.signum() != 0){
                                BigDecimal avgEff = (new BigDecimal("100")).divide(avgCons, 10, RoundingMode.HALF_UP).setScale(StaticValues.DECIMALS_AMOUNT, StaticValues.ROUNDING_MODE_AMOUNT);
                                avgConsStr = avgConsStr + "; " + avgEff.toString() + " " + listCursor.getString(6) + " / " + listCursor.getString(5);
                            }
                        }
                    }
                    else {
                        avgConsStr = mRes.getString(R.string.MainActivity_StatisticsAvgConsNoDataText);
                    }
                }
                catch(NumberFormatException e){}
            }
            else {
                avgConsStr = mRes.getString(R.string.MainActivity_StatisticsAvgConsNoDataText);
            }
            tvThreeLineListStatisticsText1.setText(mRes.getString(R.string.MainActivity_StatisticsAvgConsLabel) + avgConsStr);
            //total/mileage expenses
            String totalExpensesStr = listCursor.getString(7);
            String mileageExpenseStr = "N/A";
            String carCurrency = "";
            BigDecimal totalExpenses;
            BigDecimal mileageExpense;
            if(totalExpensesStr == null || totalExpensesStr.length() == 0 || mileage == null ||
                    mileage.equals(BigDecimal.ZERO)) {
                mileageExpenseStr = "N/A";
                totalExpensesStr = "N/A";
            }
            else {
                try{
                    totalExpenses = new BigDecimal(totalExpensesStr);
                    if(totalExpenses != null) {
                        totalExpensesStr = totalExpenses.setScale(StaticValues.DECIMALS_AMOUNT, StaticValues.ROUNDING_MODE_AMOUNT).toString();
                    }
                    mileageExpense = totalExpenses.multiply(new BigDecimal("100"));
                    mileageExpense = mileageExpense.divide(mileage, 10, RoundingMode.HALF_UP).setScale(StaticValues.DECIMALS_AMOUNT, StaticValues.ROUNDING_MODE_AMOUNT);
                    if(mileageExpense != null && !mileageExpense.equals(BigDecimal.ZERO)) {
                        carCurrency = listCursor.getString(8);
                        mileageExpenseStr = mileageExpense.toString() + " " + carCurrency + "/100 " + listCursor.getString(6);
                        BigDecimal mileageEff = (new BigDecimal("100")).divide(mileageExpense, 10, RoundingMode.HALF_UP).setScale(StaticValues.DECIMALS_AMOUNT, StaticValues.ROUNDING_MODE_AMOUNT);
                        mileageExpenseStr = mileageExpenseStr + "; " + mileageEff.toString() + " " + listCursor.getString(6) + "/" + carCurrency;
                    }
                }
                catch(NumberFormatException e){}
            }
            tvThreeLineListStatisticsText2.setText(mRes.getString(R.string.MainActivity_StatisticsTotalExpenseLabel) + " " + totalExpensesStr + " " + carCurrency);
            tvThreeLineListStatisticsText3.setText(mRes.getString(R.string.MainActivity_StatisticsMileageExpenseLabel) + " " + mileageExpenseStr);
        }
        else {
            tvThreeLineListStatisticsText2.setText("");
            tvThreeLineListStatisticsText2.setText("");
            tvThreeLineListStatisticsText3.setText("");
        }
        if(listCursor != null)
            listCursor.close();
    }

    private void initPreferenceValues() {
        SharedPreferences.Editor editor = mPreferences.edit();
        if (!mPreferences.contains("MainActivityShowMileage")) {
            editor.putBoolean("MainActivityShowMileage", true);
        }
        if (!mPreferences.contains("MainActivityShowGPSTrack")) {
            editor.putBoolean("MainActivityShowGPSTrack", true);
        }
        if (!mPreferences.contains("MainActivityShowRefuel")) {
            editor.putBoolean("MainActivityShowRefuel", true);
        }
        if (!mPreferences.contains("MainActivityShowExpense")) {
            editor.putBoolean("MainActivityShowExpense", true);
        }
        if (mPreferences.contains("MainActivityShowCarReport")) {
            editor.putBoolean("MainActivityShowStatistics",
                    mPreferences.getBoolean("MainActivityShowCarReport", true));
            editor.remove("MainActivityShowCarReport");
        }
        if (!mPreferences.contains("MainActivityShowStatistics")) {
            editor.putBoolean("MainActivityShowStatistics", true);
        }
        if (!mPreferences.contains("IsGPSTrackOnMap")) {
            editor.putBoolean("IsGPSTrackOnMap", false);
        }
        if (!mPreferences.contains("IsUseCSVTrack")) {
            editor.putBoolean("IsUseCSVTrack", true);
        }
        if (!mPreferences.contains("IsUseKMLTrack")) {
            editor.putBoolean("IsUseKMLTrack", true);
        }
        if (!mPreferences.contains("IsUseGPXTrack")) {
            editor.putBoolean("IsUseGPXTrack", true);
        }
        if (!mPreferences.contains("GPSTrackMinTime")) {
            editor.putString("GPSTrackMinTime", "0");
        }
//        if (!mPreferences.contains("GPSTrackMinDistance")) {
//            editor.putString("GPSTrackMinDistance", "0");
//            editor.commit();
//        }
        if (!mPreferences.contains("SendUsageStatistics")) {
            editor.putBoolean("SendUsageStatistics", true);
        }
        if (!mPreferences.contains("SendCrashReport")) {
            editor.putBoolean("SendCrashReport", true);
        }

        if (!mPreferences.contains("GPSTrackMaxAccuracy")) {
            editor.putString("GPSTrackMaxAccuracy", "20");
        }
        if (!mPreferences.contains("GPSTrackMaxAccuracyShutdownLimit")) {
            editor.putString("GPSTrackMaxAccuracyShutdownLimit", "30");
        }
        if (!mPreferences.contains("GPSTrackTrackFileSplitCount")) {
            editor.putString("GPSTrackTrackFileSplitCount", "0");
        }
        if (!mPreferences.contains("GPSTrackShowMode")) {
            editor.putString("GPSTrackShowMode", "M"); // M: map mode; S: satellite mode
        }
        if (!mPreferences.contains("GPSTrackCreateMileage")) {
            editor.putBoolean("GPSTrackCreateMileage", true); // M: map mode; S: satellite mode
        }
        editor.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(exitResume)
            return;
        isSendStatistics = mPreferences.getBoolean("SendUsageStatistics", true);
        if (mPreferences.getBoolean("MustClose", false)) {
            SharedPreferences.Editor editor = mPreferences.edit();
            editor.putBoolean("MustClose", false);
            editor.commit();
            finish();
        }

        if(reportDb == null)
            reportDb = new ReportDbAdapter(mainContext, null, null);

        showMileageZone = mPreferences.getBoolean("MainActivityShowMileage", true);
        showGPSTrackZone = mPreferences.getBoolean("MainActivityShowGPSTrack", true);
        showRefuelZone = mPreferences.getBoolean("MainActivityShowRefuel", true);
        showExpenseZone = mPreferences.getBoolean("MainActivityShowExpense", true);
        showStatistcsZone = mPreferences.getBoolean("MainActivityShowStatistics", true);

        CharSequence abt = mRes.getString(R.string.LM_MAIN_ACTIVITY_SHORTABOUT);
        CharSequence versionInfo = " " + appVersion;

        ((TextView)findViewById(R.id.tvShortAboutLbl)).setText(Html.fromHtml(abt.toString() + versionInfo.toString()));

        fillDriverCar();

        //fill mileage zone data
        if(showMileageZone)
            fillMileageZone();
        

        //fill gps track zone data
        if(showGPSTrackZone){
            fillGpsZone();
            if(mPreferences.getBoolean("isGpsTrackOn", false))
                btnGPSTrackInsert.setEnabled(false);
            else
                btnGPSTrackInsert.setEnabled(true);
        }
        

        //fill refuel zone data
        if(showRefuelZone)
            fillRefuelZone();
        

        //fill expense zone data
        if(showExpenseZone)
            fillExpenseZone();
        
        //fill statistics zone
        if(showStatistcsZone)
            fillStatisticsZone();

        listCursor = null;

        if(!showMileageZone)
            findViewById(R.id.llMileageZone).setVisibility(View.GONE);
        else
            findViewById(R.id.llMileageZone).setVisibility(View.VISIBLE);
        if(!showGPSTrackZone)
            findViewById(R.id.llGPSTrackZone).setVisibility(View.GONE);
        else
            findViewById(R.id.llGPSTrackZone).setVisibility(View.VISIBLE);
        if(!showRefuelZone)
            findViewById(R.id.llRefuelZone).setVisibility(View.GONE);
        else
            findViewById(R.id.llRefuelZone).setVisibility(View.VISIBLE);
        if(!showExpenseZone)
            findViewById(R.id.llExpenseZone).setVisibility(View.GONE);
        else
            findViewById(R.id.llExpenseZone).setVisibility(View.VISIBLE);
        if(!showStatistcsZone)
            findViewById(R.id.llStatistcsZone).setVisibility(View.GONE);
        else
            findViewById(R.id.llStatistcsZone).setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(reportDb != null){
            reportDb.close();
            reportDb = null;
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        if(isSendStatistics)
            AndiCarStatistics.sendFlurryStartSession(this);
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        if(isSendStatistics)
            AndiCarStatistics.sendFlurryEndSession(this);
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

    private OnClickListener btnGPSTrackInsertClickListener = new OnClickListener() {

        public void onClick(View arg0) {
            Intent gpsTrackInsertIntent = new Intent(mainContext, GPSTrackController.class);
            gpsTrackInsertIntent.putExtra("CurrentDriver_ID", currentDriverID);
            gpsTrackInsertIntent.putExtra("CurrentCar_ID", currentCarID);
            gpsTrackInsertIntent.putExtra("Operation", "N");
            startActivity(gpsTrackInsertIntent);
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

    private OnClickListener btnGPSTrackListClickListener = new OnClickListener() {

        public void onClick(View arg0) {
            Intent gpstrackReportIntent = new Intent(mainContext, GPSTrackListReportActivity.class);
            startActivity(gpstrackReportIntent);
        }
    };

    private OnClickListener btnGPSTrackShowClickListener = new OnClickListener() {

        public void onClick(View arg0) {
            Intent gpstrackShowMapIntent = new Intent(mainContext, GPSTrackMap.class);
            gpstrackShowMapIntent.putExtra("gpsTrackId", Long.toString(gpsTrackId));
            startActivity(gpstrackShowMapIntent);
        }
    };

    private void fillDriverCar() {
        if (mPreferences != null) {
            infoStr = mRes.getString(R.string.GEN_DriverLabel);

            //get the current driver id and name
            if (mPreferences.getLong("CurrentDriver_ID", -1) != -1 && !mPreferences.getAll().isEmpty()) {
                currentDriverID = mPreferences.getLong("CurrentDriver_ID", -1);
            } else { //no saved driver. start driver list activity in order to select one.
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(mRes.getString(R.string.MainActivity_NoCurrentDriverMessage));
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
                btnMileageList.setEnabled(false);
                btnMileageInsert.setEnabled(false);
                btnRefuelList.setEnabled(false);
                btnRefuelInsert.setEnabled(false);
                btnExpenseList.setEnabled(false);
                btnExpenseInsert.setEnabled(false);
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
                builder.setMessage(mRes.getString(R.string.MainActivity_NoCurrentCarMessage));
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
                btnMileageList.setEnabled(false);
                btnMileageInsert.setEnabled(false);
                btnRefuelList.setEnabled(false);
                btnRefuelInsert.setEnabled(false);
                btnExpenseList.setEnabled(false);
                btnExpenseInsert.setEnabled(false);
                return;
            }

            if (mPreferences.getString("CurrentCar_Name", "").length() > 0) {
                currentCarName = mPreferences.getString("CurrentCar_Name", "");
            }
            infoStr = infoStr + "; " + mRes.getString(R.string.GEN_CarLabel) + " " + currentCarName;
            ((TextView) findViewById(R.id.info)).setText(infoStr);

            if (currentCarID < 0 || currentDriverID < 0) {
                btnMileageInsert.setEnabled(false);
                btnMileageList.setEnabled(false);
                btnRefuelInsert.setEnabled(false);
                btnRefuelList.setEnabled(false);
                btnExpenseList.setEnabled(false);
                btnExpenseInsert.setEnabled(false);
            } else {
                btnMileageInsert.setEnabled(true);
                btnMileageList.setEnabled(true);
                btnRefuelInsert.setEnabled(true);
                btnRefuelList.setEnabled(true);
                btnExpenseList.setEnabled(true);
                btnExpenseInsert.setEnabled(true);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, StaticValues.MENU_MILEAGE_ID, 0,
                mRes.getText(R.string.MENU_MileageCaption)).setIcon(mRes.getDrawable(R.drawable.ic_menu_mileage));
        menu.add(0, StaticValues.MENU_GPSTRACK_ID, 0,
                mRes.getText(R.string.MENU_GPSTrackCaption)).setIcon(mRes.getDrawable(R.drawable.ic_menu_gpstrack));
        menu.add(0, StaticValues.MENU_REFUEL_ID, 0,
                mRes.getText(R.string.MENU_RefuelCaption)).setIcon(mRes.getDrawable(R.drawable.ic_menu_refuel));
        menu.add(0, StaticValues.MENU_EXPENSES_ID, 0,
                mRes.getText(R.string.MENU_ExpenseCaption)).setIcon(mRes.getDrawable(R.drawable.ic_menu_expenses));
        menu.add(0, StaticValues.MENU_PREFERENCES_ID, 0,
                mRes.getText(R.string.MENU_PreferencesCaption)).setIcon(mRes.getDrawable(R.drawable.ic_menu_preferences));
        menu.add(0, StaticValues.MENU_ABOUT_ID, 0,
                mRes.getText(R.string.MENU_AboutCaption)).setIcon(mRes.getDrawable(R.drawable.ic_menu_info_details));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == StaticValues.MENU_PREFERENCES_ID) {
            startActivity(new Intent(this, AndiCarPreferencesActivity.class));
//            Intent i = new Intent(this, AndiCarPreferencesActivity.class);
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
        } else if (item.getItemId() == StaticValues.MENU_GPSTRACK_ID) {
            startActivity(new Intent(this, GPSTrackListReportActivity.class));
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
