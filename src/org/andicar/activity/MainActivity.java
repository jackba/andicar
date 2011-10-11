/**
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;

import org.andicar.activity.dialog.AndiCarDialogBuilder;
import org.andicar.activity.dialog.WhatsNewDialog;
import org.andicar.activity.miscellaneous.AboutActivity;
import org.andicar.activity.miscellaneous.BackupRestoreActivity;
import org.andicar.activity.miscellaneous.GPSTrackController;
import org.andicar.activity.miscellaneous.GPSTrackMap;
import org.andicar.activity.preference.AndiCarPreferencesActivity;
import org.andicar.activity.report.ExpensesListReportActivity;
import org.andicar.activity.report.GPSTrackListReportActivity;
import org.andicar.activity.report.MileageListReportActivity;
import org.andicar.activity.report.RefuelListReportActivity;
import org.andicar.activity.report.ToDoListReportActivity;
import org.andicar.persistence.FileUtils;
import org.andicar.persistence.MainDbAdapter;
import org.andicar.persistence.ReportDbAdapter;
import org.andicar.service.AndiCarServiceStarter;
import org.andicar.utils.AndiCarExceptionHandler;
import org.andicar.utils.AndiCarStatistics;
import org.andicar.utils.StaticValues;
import org.andicar.utils.Utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.andicar.addon.activity.AddOnServicesList;
import com.andicar.addon.services.AndiCarAddOnServiceStarter;

/**
 * 
 * @author miki
 */
public class MainActivity extends BaseActivity {

	private Context mainContext;
	private int ACTIVITY_MILEAGEINSERT_REQUEST_CODE = 0;
	private int ACTIVITY_REFUELINSERT_REQUEST_CODE = 1;
	private int ACTIVITY_EXPENSEINSERT_REQUEST_CODE = 2;
	private int ACTIVITY_TODOINSERT_REQUEST_CODE = 3;
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
	private ImageButton btnToDoList;
	private ImageButton btnToDoInsert;

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
	private TextView tvThreeLineListToDoText1;
	private TextView tvThreeLineListToDoText2;
	private TextView tvThreeLineListToDoText3;
	private TextView tvStatisticsLastKnownOdometer;
	private TextView tvStatisticsAvgFuelEff;
	private TextView tvStatisticsLastFuelEff;
	private TextView tvStatisticsTotalExpenses;
	private TextView tvStatisticsMileageExpense;
	private TextView tvCarIndex;

	private TextView tvStatisticsHdr;

	private boolean exitResume = false;
	private String appVersion;
	private String dbVersion;
	private boolean showMileageZone = true;
	private boolean showGPSTrackZone = true;
	private boolean showRefuelZone = true;
	private boolean showExpenseZone = true;
	private boolean showStatistcsZone = true;
	private boolean showToDoZone = true;
	private boolean isActivityOnLoading = true;

	private boolean isSendStatistics = true;
	private boolean isSendCrashReport;
	private boolean isJustInstalled = false;
	private long gpsTrackId = -1;

	@Override
	protected void onPause() {
		super.onPause();
		if (reportDb != null) {
			reportDb.close();
			reportDb = null;
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		try {
			mainContext = this;
			mPreferences = getSharedPreferences(
					StaticValues.GLOBAL_PREFERENCE_NAME, 0);

			if(mPreferences.getString("UIStyle", "s01").equalsIgnoreCase("s00"))
				setContentView(R.layout.main_activity_s00);
	    	else if(mPreferences.getString("UIStyle", "s01").equalsIgnoreCase("s01"))
				setContentView(R.layout.main_activity_s01);

	    	if(mPreferences.getAll().size() == 0)
				isJustInstalled = true;
			SharedPreferences.Editor editor = mPreferences.edit();
			
			mResource = getResources();

			// FileUtils fu = new FileUtils(this);
			// fu.copyFile(this,
			// "/data/data/org.andicar.activity/databases/AndiCar.db",
			// StaticValues.BASE_FOLDER + "debug.db", true);

			isSendStatistics = mPreferences.getBoolean("SendUsageStatistics",
					true);
			isSendCrashReport = mPreferences
					.getBoolean("SendCrashReport", true);
			if (isSendCrashReport)
				Thread.setDefaultUncaughtExceptionHandler(new AndiCarExceptionHandler(
						Thread.getDefaultUncaughtExceptionHandler(), this));

			reportDb = new ReportDbAdapter(mainContext, null, null);

			String updateMsg = mPreferences.getString("UpdateMsg", null);
			if (updateMsg != null) {
				if (!updateMsg.equals("VersionChanged")) {
	                AndiCarDialogBuilder builder = new AndiCarDialogBuilder(MainActivity.this, 
	                		AndiCarDialogBuilder.DIALOGTYPE_INFO, mResource.getString(R.string.MainActivity_UpdateMessage));
					builder.setMessage(updateMsg);
					builder.setCancelable(false);
					builder.setPositiveButton(mResource.getString(R.string.GEN_OK),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							});
					AlertDialog alert = builder.create();
					alert.show();
					editor.remove("UpdateMsg");
					editor.commit();
				}
			}

			spnCar = (Spinner) findViewById(R.id.spnCar);
			spnCar.setOnItemSelectedListener(spinnerCarOnItemSelectedListener);
			spnCar.setOnTouchListener(spinnerOnTouchListener);
			btnMileageList = (ImageButton) findViewById(R.id.btnMileageList);
			btnMileageList.setOnClickListener(btnListMileageClickListener);
			btnMileageInsert = (ImageButton) findViewById(R.id.btnMileageInsert);
			btnMileageInsert.setOnClickListener(btnInsertMileageClickListener);
			btnRefuelList = (ImageButton) findViewById(R.id.btnRefuelList);
			btnRefuelList.setOnClickListener(btnListRefuelClickListener);
			btnRefuelInsert = (ImageButton) findViewById(R.id.btnRefuelInsert);
			btnRefuelInsert.setOnClickListener(btnInsertRefuelClickListener);
			btnExpenseList = (ImageButton) findViewById(R.id.btnExpenseList);
			btnExpenseList.setOnClickListener(btnExpenseListClickListener);
			btnExpenseInsert = (ImageButton) findViewById(R.id.btnExpenseInsert);
			btnExpenseInsert.setOnClickListener(btnInsertExpenseClickListener);
			btnToDoList = (ImageButton) findViewById(R.id.btnToDoList);
			btnToDoList.setOnClickListener(btnListToDoClickListener);
			btnToDoInsert = (ImageButton) findViewById(R.id.btnToDoInsert);
			btnToDoInsert.setOnClickListener(btnInsertToDoClickListener);
			btnGPSTrackInsert = (ImageButton) findViewById(R.id.btnGPSTrackInsert);
			btnGPSTrackInsert
					.setOnClickListener(btnInsertGPSTrackClickListener);
			btnGPSTrackList = (ImageButton) findViewById(R.id.btnGPSTrackList);
			btnGPSTrackList.setOnClickListener(btnListGPSTrackClickListener);
			btnGPSTrackShowOnMap = (ImageButton) findViewById(R.id.btnGPSTrackShowOnMap);
			btnGPSTrackShowOnMap
					.setOnClickListener(btnShowOnMapGPSTrackClickListener);

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
			tvThreeLineListToDoText1 = (TextView) findViewById(R.id.tvThreeLineListToDoText1);
			tvThreeLineListToDoText2 = (TextView) findViewById(R.id.tvThreeLineListToDoText2);
			tvThreeLineListToDoText3 = (TextView) findViewById(R.id.tvThreeLineListToDoText3);
			tvStatisticsHdr = (TextView) findViewById(R.id.tvStatisticsHdr);
			tvStatisticsLastKnownOdometer = (TextView) findViewById(R.id.tvStatisticsLastKnownOdometer);
			tvStatisticsAvgFuelEff = (TextView) findViewById(R.id.tvStatisticsAvgFuelEff);
			tvStatisticsLastFuelEff = (TextView) findViewById(R.id.tvStatisticsLastFuelEff);
			tvStatisticsTotalExpenses = (TextView) findViewById(R.id.tvStatisticsTotalExpenses);
			tvStatisticsMileageExpense = (TextView) findViewById(R.id.tvStatisticsMileageExpense);
			tvCarIndex = (TextView) findViewById(R.id.tvCarIndex);

			if (isJustInstalled) {
				exitResume = true;
				editor.putString("InitialInstallSource", StaticValues.INITIAL_INSTALL_SOURCE);
				editor.commit();
				// test if backups exists
				if (FileUtils.getFileNames(StaticValues.BACKUP_FOLDER, null) != null
						&& !FileUtils.getFileNames(StaticValues.BACKUP_FOLDER,
								null).isEmpty()) {
					initPreferenceValues(); // version update => init (new)
											// preference values
	                AndiCarDialogBuilder builder = new AndiCarDialogBuilder(MainActivity.this, 
	                		AndiCarDialogBuilder.DIALOGTYPE_INFO, mResource.getString(R.string.MainActivity_WellcomeBackMessage));
					builder.setMessage(mResource
							.getString(R.string.MainActivity_BackupExistMessage));
					builder.setCancelable(false);
					builder.setPositiveButton(mResource.getString(R.string.GEN_YES),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									Intent i = new Intent(MainActivity.this,
											BackupRestoreActivity.class);
									startActivity(i);
									exitResume = false;
								}
							});
					builder.setNegativeButton(mResource.getString(R.string.GEN_NO),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
									exitResume = false;
									onResume();
								}
							});
					AlertDialog alert = builder.create();
					alert.show();

				} else {
					exitResume = true;
					initPreferenceValues(); // init preference values
	                AndiCarDialogBuilder builder = new AndiCarDialogBuilder(MainActivity.this, 
	                		AndiCarDialogBuilder.DIALOGTYPE_INFO, mResource.getString(R.string.MainActivity_WellcomeMessage));
					builder.setTitle(mResource
							.getString(R.string.MainActivity_WellcomeMessage));
					builder.setMessage(mResource
							.getString(R.string.LM_MAIN_ACTIVITY_WELLCOME_MESSAGE2));
					builder.setCancelable(false);
					builder.setPositiveButton(mResource.getString(R.string.GEN_OK),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
//									exitResume = false;
									AndiCarDialogBuilder builder2 = new AndiCarDialogBuilder(MainActivity.this, 
					                		AndiCarDialogBuilder.DIALOGTYPE_INFO, mResource.getString(R.string.MainActivity_HowToMessage));
									builder2.setTitle(mResource
											.getString(R.string.MainActivity_HowToMessage));
									builder2.setMessage(mResource
											.getString(R.string.LM_MAIN_ACTIVITY_USABILITY_MESSAGE));
									builder2.setCancelable(false);
									builder2.setPositiveButton(mResource.getString(R.string.GEN_OK),
											new DialogInterface.OnClickListener() {
												public void onClick(DialogInterface dialog,
														int id) {
													dialog.cancel();
													exitResume = false;
													onResume();
												}
											});
									AlertDialog alert2 = builder2.create();
									alert2.show();
//									onResume();
								}
							});
					AlertDialog alert = builder.create();
					alert.show();
				}
			}
			
			if(!mPreferences.contains("InitialInstallSource"))
				editor.putString("InitialInstallSource", StaticValues.INITIAL_INSTALL_SOURCE);
			editor.commit();

			mCarId = mPreferences.getLong("CurrentCar_ID", -1);
			initSpinner(spnCar, MainDbAdapter.CAR_TABLE_NAME,
					MainDbAdapter.genColName,
					new String[] { MainDbAdapter.GEN_COL_NAME_NAME },
					MainDbAdapter.isActiveCondition, null,
					MainDbAdapter.GEN_COL_NAME_NAME, mCarId, false);

			try {
				appVersion = getPackageManager().getPackageInfo(
						getPackageName(), 0).versionName;
				
				String deviceId = ((TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId(); 
				if (appVersion == null || appVersion.contains("Beta")
						|| deviceId == null || deviceId.equals("000000000000000")) //emulator
					editor.putBoolean("IsBeta", true); // no flurry statistics
														// are send for beta
														// versions
				else
					editor.putBoolean("IsBeta", false);
				editor.commit();

				dbVersion = reportDb.getVersion() + "";

				// check if upgrade occurred. if yes init preference values &
				// restart the background services (auto backup, update check,
				// etc.)
				int appVersionCode = getPackageManager().getPackageInfo(
						getPackageName(), 0).versionCode;
				if (!mPreferences.contains("appVersionCode")
						|| mPreferences.getInt("appVersionCode", 0) < appVersionCode //version update
//						|| appVersion.endsWith("Beta")  // for beta testing
						) {

					initPreferenceValues(); // version update => init (new)
											// preference values
					AndiCarServiceStarter.startServices(this);
					AndiCarAddOnServiceStarter.startServices(this);
					if(!isJustInstalled)
						startActivity(new Intent(mainContext, WhatsNewDialog.class));
				}
				if(mPreferences.getInt("appVersionCode", 0) != appVersionCode){
					editor.putInt("appVersionCode", appVersionCode);
					editor.commit();
				}
			} catch (NameNotFoundException ex) {
				appVersion = "N/A";
			}

			// check for app update once a day
//			Long currentTime = System.currentTimeMillis();
//			Long lastTime = mPreferences.getLong("lastUpdateCheckTime", 0);
//			if (mPreferences.getBoolean("AutoUpdateCheck", true)) {
//				if ((lastTime + oneDayInMilis) < currentTime) {
//					Intent intent = new Intent(this, UpdateCheckService.class);
//					this.startService(intent);
//					editor.putLong("lastUpdateCheckTime", lastTime);
//					editor.commit();
//				}
//			}

			//debug
//			Intent intent = new Intent(this, ToDoNotificationService.class);
////			intent.putExtra("ToDoID", 22L);
//			this.startService(intent);
			
			
//			lastTime = mPreferences.getLong("lastAddOnCheckTime", 0);
//			if(isJustInstalled){
//				//show the question above 10 days 
//				editor.putLong("lastAddOnCheckTime", currentTime - (5 * StaticValues.ONE_DAY_IN_MILISECONDS));
//				editor.commit();
//			}
//			else{
//				if(lastTime + (15 * StaticValues.ONE_DAY_IN_MILISECONDS) < currentTime && 
//						!ServiceSubscription.isAddOnsUsed(mDbAdapter)){
//					editor.putLong("lastAddOnCheckTime", currentTime);
//					editor.commit();
//					
//					AndiCarDialogBuilder builder = new AndiCarDialogBuilder(MainActivity.this, 
//							AndiCarDialogBuilder.DIALOGTYPE_QUESTION, mResource.getString(R.string.MainActivity_DidYouKnow));
//		            builder.setMessage(mResource.getString(R.string.MainActivity_AddOnMessage));
//		            builder.setCancelable(false);
//		            builder.setPositiveButton(mResource.getString(R.string.GEN_YES),
//		                       new DialogInterface.OnClickListener() {
//		                           public void onClick(DialogInterface dialog, int id) {
//		                        	   startActivity(new Intent(MainActivity.this, AddOnServicesList.class));
//		                        	   dialog.cancel();
//		                           }
//		                       });
//		            builder.setNegativeButton(mResource.getString(R.string.GEN_NO),
//		                        new DialogInterface.OnClickListener() {
//		                           public void onClick(DialogInterface dialog, int id) {
//		                                dialog.cancel();
//		                           }
//		                        });
//		            AlertDialog alert = builder.create();
//		            alert.show();
//				}
//			}
			
			//inneractive
//			InnerActiveAdView iaView = (InnerActiveAdView) findViewById(R.id.ad1);
//			if(iaView != null){
//				iaView.setVisibility(View.GONE);
////				iaView.setVisibility(View.VISIBLE);
////				iaView.setRefreshInterval(120); //120 seconds
//			}
			
		} catch (Exception e) {
			String logFile = "startup.log";
			FileUtils.deleteFile(StaticValues.BASE_FOLDER + logFile);
			FileUtils fu = new FileUtils(mainContext);
			Throwable cause = e.getCause();
			StackTraceElement[] stackTrace;
			if (cause != null)
				stackTrace = cause.getStackTrace();
			else
				stackTrace = e.getStackTrace();

			StackTraceElement stackTraceElement;
			String stackStr = e.getMessage();
			for (int i = 0; i < stackTrace.length; i++) {
				stackTraceElement = stackTrace[i];
				stackStr = stackStr + stackTraceElement.getClassName() + "."
						+ stackTraceElement.getMethodName() + ": "
						+ stackTraceElement.getLineNumber() + "  ";
			}
			fu.writeToLogFile(stackStr, logFile);

			madbErrorAlert.setMessage(e.getMessage());
			madError = madbErrorAlert.create();
			madError.show();
		}
		
	}

	private void fillExpenseZone() {
		listCursor = null;
		Bundle whereConditions = new Bundle();
		whereConditions.putString(
				ReportDbAdapter.sqlConcatTableColumn(
						MainDbAdapter.EXPENSE_TABLE_NAME,
						MainDbAdapter.EXPENSE_COL_CAR_ID_NAME)
						+ "=", String.valueOf(mCarId));
		whereConditions.putString("COALESCE("
				+ MainDbAdapter.EXPENSE_COL_FROMTABLE_NAME + ", '') = ", "");
		reportDb.setReportSql("expensesListViewSelect",
				whereConditions);
		listCursor = reportDb.fetchReport(1);
		if (listCursor != null && listCursor.moveToFirst()) {
			if(listCursor.getString(1) != null)
				tvThreeLineListExpenseText1.setText(listCursor.getString(1)
					.replace(
							"[#1]",
							DateFormat.getDateFormat(getApplicationContext())
									.format(listCursor.getLong(4) * 1000)));

			if(listCursor.getString(2) != null)
				tvThreeLineListExpenseText2.setText(listCursor.getString(2)
					.replace(
							"[#1]",
							Utils.numberToString(listCursor.getDouble(5), true,
									StaticValues.DECIMALS_AMOUNT,
									StaticValues.ROUNDING_MODE_AMOUNT))
					.replace(
							"[#2]",
							Utils.numberToString(listCursor.getDouble(6), true,
									StaticValues.DECIMALS_AMOUNT,
									StaticValues.ROUNDING_MODE_AMOUNT))
					.replace(
							"[#3]",
							Utils.numberToString(listCursor.getDouble(7), true,
									StaticValues.DECIMALS_LENGTH,
									StaticValues.ROUNDING_MODE_LENGTH)));
			
			tvThreeLineListExpenseText3.setText(listCursor.getString(listCursor
					.getColumnIndex(ReportDbAdapter.THIRD_LINE_LIST_NAME)));
			btnExpenseList.setEnabled(true);
		} else {
			tvThreeLineListExpenseText1.setText(mResource
					.getString(R.string.MainActivity_ExpenseNoDataText));
			tvThreeLineListExpenseText2.setText("");
			// tvThreeLineListExpenseText2.setText(mRes.getString(R.string.MainActivity_ExpenseNoDataAditionalText));
			tvThreeLineListExpenseText3.setText("");
			btnExpenseList.setEnabled(false);
		}
		if (listCursor != null)
			listCursor.close();
	}

	private void fillGpsZone() {
		listCursor = null;
		Bundle whereConditions = new Bundle();
		whereConditions.clear();
		whereConditions.putString(
				ReportDbAdapter.sqlConcatTableColumn(
						MainDbAdapter.GPSTRACK_TABLE_NAME,
						MainDbAdapter.GPSTRACK_COL_CAR_ID_NAME)
						+ "=", String.valueOf(mCarId));
		reportDb.setReportSql("gpsTrackListViewSelect", whereConditions);
		listCursor = reportDb.fetchReport(1);
		gpsTrackId = -1;
		if (listCursor != null && listCursor.moveToFirst()) {
			gpsTrackId = listCursor.getLong(0);
			if(listCursor.getString(1) != null)
				tvThreeLineListGPSTrackText1.setText(listCursor.getString(1)
					.replace(
							"[#1]",
							DateFormat.getDateFormat(getApplicationContext())
									.format(listCursor.getLong(7) * 1000)));
			
			if(listCursor.getString(2) != null){
				tvThreeLineListGPSTrackText2
						.setText(listCursor.getString(2)
								.replace(
										"[#1]",
										mResource.getString(R.string.GPSTrackReport_GPSTrackVar_1))
								.replace(
										"[#2]",
										mResource.getString(R.string.GPSTrackReport_GPSTrackVar_2))
								.replace(
										"[#3]",
										mResource.getString(R.string.GPSTrackReport_GPSTrackVar_3))
								.replace(
										"[#4]",
										mResource.getString(R.string.GPSTrackReport_GPSTrackVar_4))
								.replace(
										"[#5]",
										mResource.getString(R.string.GPSTrackReport_GPSTrackVar_5)
												+ Utils.getTimeString(
														listCursor.getLong(4),
														false))
								.replace(
										"[#6]",
										mResource.getString(R.string.GPSTrackReport_GPSTrackVar_6)
												+ Utils.getTimeString(
														listCursor.getLong(5),
														false))
								.replace(
										"[#7]",
										mResource.getString(R.string.GPSTrackReport_GPSTrackVar_7))
								.replace(
										"[#8]",
										mResource.getString(R.string.GPSTrackReport_GPSTrackVar_8))
								.replace(
										"[#9]",
										mResource.getString(R.string.GPSTrackReport_GPSTrackVar_9))
								.replace(
										"[#10]",
										mResource.getString(R.string.GPSTrackReport_GPSTrackVar_10))
								.replace(
										"[#11]",
										mResource.getString(R.string.GPSTrackReport_GPSTrackVar_11))

					);
			}
			tvThreeLineListGPSTrackText3.setText(listCursor.getString(3));
			btnGPSTrackList.setEnabled(true);
			btnGPSTrackShowOnMap.setEnabled(true);
		} else {
			tvThreeLineListGPSTrackText1.setText(mResource
					.getString(R.string.MainActivity_GPSTrackZoneNoDataText));
			tvThreeLineListGPSTrackText2.setText("");
			tvThreeLineListGPSTrackText3.setText("");
			btnGPSTrackList.setEnabled(false);
			btnGPSTrackShowOnMap.setEnabled(false);
		}
		if (listCursor != null)
			listCursor.close();
	}

	private void fillMileageZone() {
		listCursor = null;
		Bundle whereConditions = new Bundle();
		whereConditions = new Bundle();
		whereConditions.putString(
				ReportDbAdapter.sqlConcatTableColumn(
						MainDbAdapter.MILEAGE_TABLE_NAME,
						MainDbAdapter.MILEAGE_COL_CAR_ID_NAME)
						+ "=", String.valueOf(mCarId));

		reportDb.setReportSql("mileageListViewSelect", whereConditions);
		listCursor = reportDb.fetchReport(1);
		if (listCursor != null && listCursor.moveToFirst()) {
			if(listCursor.getString(1) != null)
				tvThreeLineListMileageText1.setText(listCursor.getString(1)
					.replace(
							"[#1]",
							DateFormat.getDateFormat(getApplicationContext())
									.format(listCursor.getLong(5) * 1000)));
			
			if(listCursor.getString(2) != null)
				tvThreeLineListMileageText2.setText(listCursor.getString(2)
					.replace(
							"[#1]",
							Utils.numberToString(listCursor.getDouble(6), true,
									StaticValues.DECIMALS_LENGTH,
									StaticValues.ROUNDING_MODE_LENGTH))
					.replace(
							"[#2]",
							Utils.numberToString(listCursor.getDouble(7), true,
									StaticValues.DECIMALS_LENGTH,
									StaticValues.ROUNDING_MODE_LENGTH))
					.replace(
							"[#3]",
							Utils.numberToString(listCursor.getDouble(8), true,
									StaticValues.DECIMALS_LENGTH,
									StaticValues.ROUNDING_MODE_LENGTH))

			);
			tvThreeLineListMileageText3.setText(listCursor.getString(3));
			btnMileageList.setEnabled(true);
		} else {
			tvThreeLineListMileageText1.setText(mResource
					.getString(R.string.MainActivity_MileageNoDataText));
			tvThreeLineListMileageText2.setText("");
			tvThreeLineListMileageText3.setText("");
			btnMileageList.setEnabled(false);
		}
		if (listCursor != null)
			listCursor.close();
	}

	private void fillRefuelZone() {
		listCursor = null;
		Bundle whereConditions = new Bundle();
		whereConditions.putString(
				ReportDbAdapter.sqlConcatTableColumn(
						MainDbAdapter.REFUEL_TABLE_NAME,
						MainDbAdapter.REFUEL_COL_CAR_ID_NAME)
						+ "=", String.valueOf(mCarId));
		reportDb.setReportSql("refuelListViewSelect", whereConditions);
		listCursor = reportDb.fetchReport(1);
		if (listCursor != null && listCursor.moveToFirst()) {
			
			if(listCursor.getString(1) != null)
				tvThreeLineListRefuelText1.setText(listCursor.getString(1).replace(
					"[#1]",
					DateFormat.getDateFormat(getApplicationContext()).format(
							listCursor.getLong(4) * 1000)));
			
			if(listCursor.getString(2) != null)
				tvThreeLineListRefuelText2.setText(listCursor.getString(2)
					.replace(
							"[#1]",
							Utils.numberToString(listCursor.getDouble(5), true,
									StaticValues.DECIMALS_VOLUME,
									StaticValues.ROUNDING_MODE_VOLUME))
					.replace(
							"[#2]",
							Utils.numberToString(listCursor.getDouble(6), true,
									StaticValues.DECIMALS_VOLUME,
									StaticValues.ROUNDING_MODE_VOLUME))
					.replace(
							"[#3]",
							Utils.numberToString(listCursor.getDouble(7), true,
									StaticValues.DECIMALS_PRICE,
									StaticValues.ROUNDING_MODE_PRICE))
					.replace(
							"[#4]",
							Utils.numberToString(listCursor.getDouble(8), true,
									StaticValues.DECIMALS_PRICE,
									StaticValues.ROUNDING_MODE_PRICE))
					.replace(
							"[#5]",
							Utils.numberToString(listCursor.getDouble(9), true,
									StaticValues.DECIMALS_AMOUNT,
									StaticValues.ROUNDING_MODE_AMOUNT))
					.replace(
							"[#6]",
							Utils.numberToString(listCursor.getDouble(10),
									true, StaticValues.DECIMALS_AMOUNT,
									StaticValues.ROUNDING_MODE_AMOUNT))
					.replace(
							"[#7]",
							Utils.numberToString(listCursor.getDouble(11),
									true, StaticValues.DECIMALS_LENGTH,
									StaticValues.ROUNDING_MODE_LENGTH)));
			tvThreeLineListRefuelText3.setText(listCursor.getString(listCursor
					.getColumnIndex(ReportDbAdapter.THIRD_LINE_LIST_NAME)));
			btnRefuelList.setEnabled(true);
		} else {
			tvThreeLineListRefuelText1.setText(mResource
					.getString(R.string.MainActivity_RefuelNoDataText));
			tvThreeLineListRefuelText2.setText("");
			tvThreeLineListRefuelText3.setText("");
			btnRefuelList.setEnabled(false);
		}
		if (listCursor != null)
			listCursor.close();
	}

	private void fillToDoZone(){
		listCursor = null;
		Bundle whereConditions = new Bundle();
		whereConditions.putString(
				ReportDbAdapter.sqlConcatTableColumn(
						MainDbAdapter.TODO_TABLE_NAME,
						MainDbAdapter.TODO_COL_ISDONE_NAME)
						+ "=", "N");
		reportDb.setReportSql("todoListViewSelect", whereConditions);
		listCursor = reportDb.fetchReport(1);
		if (listCursor != null && listCursor.moveToFirst()) {
			String dataString = listCursor.getString(1);
			if(dataString == null)
				dataString = "";
			
    		if(dataString.contains("[#5]"))
    			tvThreeLineListToDoText1.setTextColor(Color.RED);
    		else if(dataString.contains("[#15]"))
    			tvThreeLineListToDoText1.setTextColor(Color.GREEN);
//    		else
//    			tvThreeLineListToDoText1.setTextColor(Color.WHITE);
			tvThreeLineListToDoText1.setText(
				dataString
					.replace("[#1]", mResource.getString(R.string.GEN_TypeLabel))
					.replace("[#2]", mResource.getString(R.string.GEN_TaskLabel))
					.replace("[#3]", mResource.getString(R.string.GEN_CarLabel))
					.replace("[#4]", mResource.getString(R.string.GEN_StatusLabel))
					.replace("[#5]", mResource.getString(R.string.ToDo_OverdueLabel))
					.replace("[#6]", mResource.getString(R.string.ToDo_ScheduledLabel))
					.replace("[#15]", mResource.getString(R.string.ToDo_DoneLabel))
					);
			
    		long time = System.currentTimeMillis();
    		Calendar now = Calendar.getInstance();
    		Calendar cal = Calendar.getInstance();
    		
    		long estMileageDueDays = listCursor.getLong(7);
    		String timeStr = "";
			if(estMileageDueDays >= 0){
				if(estMileageDueDays == 99999999999L)
					timeStr = mResource.getString(R.string.ToDo_EstimatedMileageDateNoData);
				else{
					if(listCursor.getString(1).contains("[#5]"))
						timeStr = mResource.getString(R.string.ToDo_OverdueLabel);
					else{
						cal.setTimeInMillis(time + (estMileageDueDays * StaticValues.ONE_DAY_IN_MILISECONDS));
						if(cal.get(Calendar.YEAR) - now.get(Calendar.YEAR) > 5)
							timeStr = mResource.getString(R.string.ToDo_EstimatedMileageDateTooFar);
						else{
							if(cal.getTimeInMillis() - now.getTimeInMillis() < 365 * StaticValues.ONE_DAY_IN_MILISECONDS) // 1 year
    							timeStr = DateFormat.getDateFormat(this)
													.format(time + (estMileageDueDays * StaticValues.ONE_DAY_IN_MILISECONDS));
							else{
								timeStr = DateFormat.format("MMM, yyyy", cal).toString();
							}
								
						}
					}
				}
			}
			time = time + listCursor.getLong(7);
			if(listCursor.getString(2) != null)
				tvThreeLineListToDoText2.setText(
					listCursor.getString(2)
    					.replace("[#7]", mResource.getString(R.string.ToDo_ScheduledDateLabel)) 
    					.replace("[#8]",  
	    							DateFormat.getDateFormat(this)
									.format(listCursor.getLong(4) * 1000) + " " +
			 							DateFormat.getTimeFormat(this)
				 								.format(listCursor.getLong(4) * 1000))
						.replace("[#9]", mResource.getString(R.string.GEN_Or2))
    					.replace("[#10]", mResource.getString(R.string.ToDo_ScheduledMileageLabel))
    					.replace("[#11]", Utils.numberToString(listCursor.getDouble(5) , true, StaticValues.DECIMALS_LENGTH, StaticValues.ROUNDING_MODE_LENGTH))
    					.replace("[#12]", mResource.getString(R.string.GEN_Mileage))
    					.replace("[#13]", mResource.getString(R.string.ToDo_EstimatedMileageDate))
    					.replace("[#14]", timeStr)
			 );
			tvThreeLineListToDoText3.setText(listCursor.getString(listCursor
					.getColumnIndex(ReportDbAdapter.THIRD_LINE_LIST_NAME)));
			btnToDoList.setEnabled(true);
		} else {
			tvThreeLineListToDoText1.setText(mResource
					.getString(R.string.MainActivity_ToDoNoDataText));
			tvThreeLineListToDoText2.setText("");
			tvThreeLineListToDoText3.setText("");
			btnToDoList.setEnabled(false);
		}
		if (listCursor != null)
			listCursor.close();
	}
	private void fillStatisticsZone() {
		listCursor = null;
		Bundle whereConditions = new Bundle();
		whereConditions.putString(
				ReportDbAdapter.sqlConcatTableColumn(
						MainDbAdapter.CAR_TABLE_NAME,
						MainDbAdapter.GEN_COL_ROWID_NAME)
						+ "=", String.valueOf(mCarId));
		reportDb.setReportSql("statisticsMainViewSelect", whereConditions);
		listCursor = reportDb.fetchReport(1);
		if (listCursor != null && listCursor.moveToFirst()) {
			BigDecimal startIndex = null;
			BigDecimal currentIndex = null;
			BigDecimal mileage = null;
			BigDecimal expenses = null;
			try {
				startIndex = new BigDecimal(listCursor.getDouble(1)).setScale(
						StaticValues.DECIMALS_LENGTH,
						StaticValues.ROUNDING_MODE_LENGTH);
				// listCursor.getString(1));
				currentIndex = new BigDecimal(listCursor.getDouble(2))
						.setScale(StaticValues.DECIMALS_LENGTH,
								StaticValues.ROUNDING_MODE_LENGTH);
				// new BigDecimal(listCursor.getString(2));
				expenses = new BigDecimal(listCursor.getDouble(4)).setScale(
						StaticValues.DECIMALS_AMOUNT,
						StaticValues.ROUNDING_MODE_AMOUNT);

			} catch (NumberFormatException e) {
			}
			String carUOMLengthCode = listCursor.getString(3);
			String carUOMVolumeCode = listCursor.getString(7);
			String carCurrencyCode = listCursor.getString(6);

			if (startIndex != null && currentIndex != null)
				mileage = currentIndex.subtract(startIndex);
			else
				mileage = BigDecimal.ZERO;

			tvStatisticsHdr.setText(mResource
					.getString(R.string.MainActivity_StatisticsHeaderCaption)
					+ " "
					+ Utils.numberToString(mileage, true,
							StaticValues.DECIMALS_LENGTH,
							StaticValues.ROUNDING_MODE_LENGTH)
					+ " "
					+ carUOMLengthCode);
			tvStatisticsLastKnownOdometer
					.setText(mResource
							.getString(R.string.MainActivity_StatisticsListLastOdoLabel)
							+ Utils.numberToString(currentIndex, true,
									StaticValues.DECIMALS_LENGTH,
									StaticValues.ROUNDING_MODE_LENGTH)
							+ " "
							+ carUOMLengthCode);
			tvStatisticsTotalExpenses
					.setText(mResource
							.getString(R.string.MainActivity_StatisticsTotalExpenseLabel)
							+ " "
							+ (expenses != null ? Utils.numberToString(
									expenses, true,
									StaticValues.DECIMALS_AMOUNT,
									StaticValues.ROUNDING_MODE_AMOUNT) : "0")
							+ " " + carCurrencyCode);

			// mileage expense
			BigDecimal mileageExpense = null;
			BigDecimal mileageEff = null;
			String mileageExpenseStr = null;
			if (listCursor.getString(5) != null) {
				try {
					expenses = new BigDecimal(listCursor.getDouble(5))
							.setScale(StaticValues.DECIMALS_AMOUNT,
									StaticValues.ROUNDING_MODE_AMOUNT);
				} catch (NumberFormatException e) {
				}
			}
			if (expenses != null && mileage != null && mileage.signum() != 0) {
				mileageExpense = expenses.multiply(new BigDecimal("100"));
				mileageExpense = mileageExpense.divide(mileage, 10,
						RoundingMode.HALF_UP).setScale(
						StaticValues.DECIMALS_AMOUNT,
						StaticValues.ROUNDING_MODE_AMOUNT);
				if (mileageExpense != null
						&& (mileageExpense.setScale(10, RoundingMode.HALF_UP))
								.signum() != 0) {
					mileageExpenseStr = Utils.numberToString(mileageExpense,
							true, StaticValues.DECIMALS_AMOUNT,
							StaticValues.ROUNDING_MODE_AMOUNT)
							+ " "
							+ carCurrencyCode
							+ "/100 "
							+ carUOMLengthCode;
					if (mileageExpense.signum() != 0) {
						mileageEff = ((new BigDecimal("100")).divide(
								mileageExpense, 10, RoundingMode.HALF_UP))
								.setScale(StaticValues.DECIMALS_AMOUNT,
										StaticValues.ROUNDING_MODE_AMOUNT);
						if (mileageEff != null)
							mileageExpenseStr = mileageExpenseStr
									+ "; "
									+ Utils.numberToString(mileageEff, true,
											StaticValues.DECIMALS_AMOUNT,
											StaticValues.ROUNDING_MODE_AMOUNT)
									+ " " + carUOMLengthCode + "/"
									+ carCurrencyCode;
					}
				}
			}
			tvStatisticsMileageExpense
					.setText(mResource
							.getString(R.string.MainActivity_StatisticsMileageExpenseLabel)
							+ " "
							+ (mileageExpenseStr != null ? mileageExpenseStr
									: "N/A"));

			// fuel efficiency
			Cursor c = null;
			String fuelEffStr = mResource
					.getString(R.string.MainActivity_StatisticsAvgConsNoDataText);
			String lastFuelEffStr = mResource.getString(R.string.MainActivity_StatisticsLastConsNoData);
			String sql = "";
			BigDecimal tmpFullRefuelIndex = null;
			BigDecimal lastFullRefuelIndex = null;
			BigDecimal totalFuelQty = null;

			// select first full refuel index
			sql = "SELECT " + MainDbAdapter.REFUEL_COL_INDEX_NAME + " FROM "
					+ MainDbAdapter.REFUEL_TABLE_NAME + " WHERE "
					+ MainDbAdapter.REFUEL_COL_CAR_ID_NAME + " = " + mCarId
					+ " " + " AND " + MainDbAdapter.GEN_COL_ISACTIVE_NAME
					+ " = \'Y\' " + " AND "
					+ MainDbAdapter.REFUEL_COL_ISFULLREFUEL_NAME + " = \'Y\' "
					+ " ORDER BY " + MainDbAdapter.REFUEL_COL_INDEX_NAME
					+ " ASC " + " LIMIT 1";
			c = reportDb.execSelectSql(sql, null);

			if (c.moveToFirst()) {
				tmpFullRefuelIndex = new BigDecimal(c.getDouble(0)).setScale(
						StaticValues.DECIMALS_LENGTH,
						StaticValues.ROUNDING_MODE_LENGTH);

				c.close();
				// get the last full refuel index
				sql = "SELECT " + MainDbAdapter.REFUEL_COL_INDEX_NAME
						+ " FROM " + MainDbAdapter.REFUEL_TABLE_NAME
						+ " WHERE " + MainDbAdapter.REFUEL_COL_CAR_ID_NAME
						+ " = " + mCarId + " " + " AND "
						+ MainDbAdapter.GEN_COL_ISACTIVE_NAME + " = \'Y\' "
						+ " AND " + MainDbAdapter.REFUEL_COL_ISFULLREFUEL_NAME
						+ " = \'Y\' " + " AND "
						+ MainDbAdapter.REFUEL_COL_INDEX_NAME + " <> "
						+ tmpFullRefuelIndex.toPlainString()
						+ // convert from xxxe+10 => xxxxxxxxxx...
						" ORDER BY " + MainDbAdapter.REFUEL_COL_INDEX_NAME
						+ " DESC " + " LIMIT 1";
				c = reportDb.execSelectSql(sql, null);
				if (c.moveToFirst()) {
					lastFullRefuelIndex = new BigDecimal(c.getDouble(0))
							.setScale(StaticValues.DECIMALS_LENGTH,
									StaticValues.ROUNDING_MODE_LENGTH);
					c.close();
					if (tmpFullRefuelIndex != null
							&& lastFullRefuelIndex != null
							&& lastFullRefuelIndex.subtract(tmpFullRefuelIndex)
									.signum() != 0) {
						// get the total fuel quantity between the first and
						// last refuels
						sql = "SELECT SUM("
								+ MainDbAdapter.REFUEL_COL_QUANTITY_NAME + ") "
								+ " FROM " + MainDbAdapter.REFUEL_TABLE_NAME
								+ " WHERE "
								+ MainDbAdapter.REFUEL_COL_CAR_ID_NAME + " = "
								+ mCarId + " " + " AND "
								+ MainDbAdapter.GEN_COL_ISACTIVE_NAME
								+ " = \'Y\' " + " AND "
								+ MainDbAdapter.REFUEL_COL_INDEX_NAME + " > "
								+ tmpFullRefuelIndex.toPlainString() + " AND "
								+ MainDbAdapter.REFUEL_COL_INDEX_NAME + " <= "
								+ lastFullRefuelIndex.toPlainString();
						c = reportDb.execSelectSql(sql, null);
						if (c.moveToFirst()) {
							try {
								totalFuelQty = new BigDecimal(c.getDouble(0))
										.setScale(
												StaticValues.DECIMALS_VOLUME,
												StaticValues.ROUNDING_MODE_VOLUME);
								// new BigDecimal(c.getString(0));
							} catch (NumberFormatException e) {
							}
						}

						c.close();
						if (totalFuelQty != null) {
							// calculate the avg cons and fuel eff.
							BigDecimal avgCons = BigDecimal.ZERO;
							avgCons = totalFuelQty.multiply(new BigDecimal(
									"100"));
							avgCons = avgCons
									.divide(lastFullRefuelIndex
											.subtract(tmpFullRefuelIndex),
											10, RoundingMode.HALF_UP).setScale(
											StaticValues.DECIMALS_AMOUNT,
											StaticValues.ROUNDING_MODE_AMOUNT);
							fuelEffStr = Utils.numberToString(avgCons, true,
									StaticValues.DECIMALS_VOLUME,
									StaticValues.ROUNDING_MODE_VOLUME)
									+ " "
									+ carUOMVolumeCode
									+ "/100"
									+ carUOMLengthCode;
							// //efficiency: x uom length (km or mi) / uom
							// volume (l or galon)
							if (avgCons != null && avgCons.signum() != 0) {
								BigDecimal avgEff = (new BigDecimal("100"))
										.divide(avgCons, 10,
												RoundingMode.HALF_UP)
										.setScale(
												StaticValues.DECIMALS_VOLUME,
												StaticValues.ROUNDING_MODE_VOLUME);
								fuelEffStr = fuelEffStr
										+ "; "
										+ Utils.numberToString(
												avgEff,
												true,
												StaticValues.DECIMALS_LENGTH,
												StaticValues.ROUNDING_MODE_LENGTH)
										+ " " + carUOMLengthCode + "/"
										+ carUOMVolumeCode;
							}
						}

						// calculate the last fuel eff (for the last two full
						// refuels)

						// get the second last full refuel
						sql = "SELECT " + MainDbAdapter.REFUEL_COL_INDEX_NAME
								+ " FROM " + MainDbAdapter.REFUEL_TABLE_NAME
								+ " WHERE "
								+ MainDbAdapter.REFUEL_COL_CAR_ID_NAME + " = "
								+ mCarId + " " + " AND "
								+ MainDbAdapter.GEN_COL_ISACTIVE_NAME
								+ " = \'Y\' " + " AND "
								+ MainDbAdapter.REFUEL_COL_ISFULLREFUEL_NAME
								+ " = \'Y\' " + " AND "
								+ MainDbAdapter.REFUEL_COL_INDEX_NAME + " < "
								+ lastFullRefuelIndex.toPlainString()
								+ " ORDER BY "
								+ MainDbAdapter.REFUEL_COL_INDEX_NAME
								+ " DESC " + " LIMIT 1";
						c = reportDb.execSelectSql(sql, null);
						if (c.moveToFirst()) {
							tmpFullRefuelIndex = (new BigDecimal(c.getDouble(0))
									.setScale(StaticValues.DECIMALS_LENGTH,
											StaticValues.ROUNDING_MODE_LENGTH));
							c.close();
							// get the total fuel qty between the last two full
							// refuels
							sql = "SELECT SUM("
									+ MainDbAdapter.REFUEL_COL_QUANTITY_NAME
									+ ") " + " FROM "
									+ MainDbAdapter.REFUEL_TABLE_NAME
									+ " WHERE "
									+ MainDbAdapter.REFUEL_COL_CAR_ID_NAME
									+ " = " + mCarId + " " + " AND "
									+ MainDbAdapter.GEN_COL_ISACTIVE_NAME
									+ " = \'Y\' " + " AND "
									+ MainDbAdapter.REFUEL_COL_INDEX_NAME
									+ " > "
									+ tmpFullRefuelIndex.toPlainString()
									+ " AND "
									+ MainDbAdapter.REFUEL_COL_INDEX_NAME
									+ " <= "
									+ lastFullRefuelIndex.toPlainString();
							c = reportDb.execSelectSql(sql, null);
							if (c.moveToFirst()) {
								if (c.getString(0) != null)
									totalFuelQty = new BigDecimal(
											c.getString(0));
								else
									totalFuelQty = null;
							}
							c.close();
							if (totalFuelQty != null) {
								// calculate the avg cons and fuel eff.
								BigDecimal avgCons = BigDecimal.ZERO;
								avgCons = totalFuelQty.multiply(new BigDecimal(
										"100"));
								avgCons = avgCons
										.divide(lastFullRefuelIndex
												.subtract(tmpFullRefuelIndex),
												10, RoundingMode.HALF_UP)
										.setScale(
												StaticValues.DECIMALS_AMOUNT,
												StaticValues.ROUNDING_MODE_AMOUNT);
								lastFuelEffStr = Utils.numberToString(avgCons,
										true, StaticValues.DECIMALS_VOLUME,
										StaticValues.ROUNDING_MODE_VOLUME)
										+ " "
										+ carUOMVolumeCode
										+ "/100"
										+ carUOMLengthCode;
								// //efficiency: x uom length (km or mi) / uom
								// volume (l or galon)
								if (avgCons != null && avgCons.signum() != 0) {
									BigDecimal avgEff = (new BigDecimal("100"))
											.divide(avgCons, 10,
													RoundingMode.HALF_UP)
											.setScale(
													StaticValues.DECIMALS_AMOUNT,
													StaticValues.ROUNDING_MODE_AMOUNT);
									lastFuelEffStr = lastFuelEffStr
											+ "; "
											+ Utils.numberToString(
													avgEff,
													true,
													StaticValues.DECIMALS_LENGTH,
													StaticValues.ROUNDING_MODE_LENGTH)
											+ " " + carUOMLengthCode + "/"
											+ carUOMVolumeCode;
								}
							}
							c.close();
						} else
							c.close();
					}
				} else
					c.close(); // no last full refuel => no 2 full refuels =>
								// cannot calculate fuel eff.
			} else { // no full refuel recorded
				c.close();
			}
			tvStatisticsAvgFuelEff.setText(mResource
					.getString(R.string.MainActivity_StatisticsAvgConsLabel)
					+ fuelEffStr);
			tvStatisticsLastFuelEff.setText(mResource
					.getString(R.string.MainActivity_StatisticsLastConsLabel)
					+ lastFuelEffStr);
		} else {
			tvStatisticsLastFuelEff.setText(mResource
					.getString(R.string.MainActivity_StatisticsLastConsNoData));
			tvStatisticsTotalExpenses.setText("N/A");
			tvStatisticsMileageExpense.setText("N/A");
		}
		if (listCursor != null)
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
		// if (!mPreferences.contains("GPSTrackMinDistance")) {
		// editor.putString("GPSTrackMinDistance", "0");
		// editor.commit();
		// }
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
			editor.putString("GPSTrackShowMode", "M"); // M: map mode; S:
														// satellite mode
		}
		if (!mPreferences.contains("GPSTrackCreateMileage")) {
			editor.putBoolean("GPSTrackCreateMileage", true); // M: map mode; S:
																// satellite
																// mode
		}
		if (!mPreferences.contains("UseNumericKeypad")) {
			editor.putBoolean("UseNumericKeypad", true);
		}
		if (!mPreferences.contains("RememberLastTag")) {
			editor.putBoolean("RememberLastTag", false);
		}
		if (!mPreferences.contains("UseNumericKeypad")) {
			editor.putBoolean("UseNumericKeypad", true);
		}
		if (!mPreferences.contains("AutoUpdateCheck")) {
			editor.putBoolean("AutoUpdateCheck", true);
		}
		if (!mPreferences.contains("UIStyle")) {
			editor.putString("UIStyle", "s01");
		}

		editor.commit();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (exitResume)
			return;
		// try{
		isActivityOnLoading = true;
		isSendStatistics = mPreferences.getBoolean("SendUsageStatistics", true);
		if (mPreferences.getBoolean("MustClose", false)) {
			SharedPreferences.Editor editor = mPreferences.edit();
			editor.putBoolean("MustClose", false);
			editor.commit();
			finish();
		}

		mCarId = mPreferences.getLong("CurrentCar_ID", -1);

		if (reportDb == null)
			reportDb = new ReportDbAdapter(mainContext, null, null);

		showMileageZone = mPreferences.getBoolean("MainActivityShowMileage", true);
		showGPSTrackZone = mPreferences.getBoolean("MainActivityShowGPSTrack", true);
		showRefuelZone = mPreferences.getBoolean("MainActivityShowRefuel", true);
		showExpenseZone = mPreferences.getBoolean("MainActivityShowExpense", true);
		showStatistcsZone = mPreferences.getBoolean("MainActivityShowStatistics", true);
		showToDoZone = mPreferences.getBoolean("MainActivityShowToDo", true);

		CharSequence abt = mResource.getText(R.string.LM_MAIN_ACTIVITY_SHORTABOUT);
		String versionInfo = " " + appVersion + " (DBv: " + dbVersion + ")";

		((TextView) findViewById(R.id.tvShortAboutLbl)).setText(abt);
		((TextView) findViewById(R.id.tvShortAboutAppVersion)).setText(mResource
				.getText(R.string.MainActivity_AppVersion) + versionInfo);

		fillDriverCar();
		initZones();

		listCursor = null;
		// }
		// catch(Exception e){
		//
		// String logFile = StaticValues.BASE_FOLDER + "startup.log";
		// FileUtils fu = new FileUtils(this);
		// fu.writeToLogFile(e.getMessage(), logFile);
		// Throwable cause = e.getCause();
		// StackTraceElement[] stackTrace;
		// if(cause != null)
		// stackTrace = cause.getStackTrace();
		// else
		// stackTrace = e.getStackTrace();
		//
		// StackTraceElement stackTraceElement;
		// String stackStr = "";
		// for(int i = 0; i < stackTrace.length; i++) {
		// stackTraceElement = stackTrace[i];
		// stackStr = stackStr + stackTraceElement.getClassName() + "." +
		// stackTraceElement.getMethodName() + ": " +
		// stackTraceElement.getLineNumber() + "  ";
		// }
		// fu.writeToLogFile(stackStr, logFile);
		//
		// madbErrorAlert.setMessage(e.getMessage());
		// madError = madbErrorAlert.create();
		// madError.show();
		// }
	}

	private void initZones() {
		// fill mileage zone data
		if (showMileageZone) {
			findViewById(R.id.llMileageZone).setVisibility(View.VISIBLE);
			fillMileageZone();
		} else
			findViewById(R.id.llMileageZone).setVisibility(View.GONE);

		// fill gps track zone data
		if (showGPSTrackZone) {
			findViewById(R.id.llGPSTrackZone).setVisibility(View.VISIBLE);
			fillGpsZone();
			if (mPreferences.getBoolean("isGpsTrackOn", false))
				btnGPSTrackInsert.setEnabled(false);
			else
				btnGPSTrackInsert.setEnabled(true);
		} else
			findViewById(R.id.llGPSTrackZone).setVisibility(View.GONE);

		// fill refuel zone data
		if (showRefuelZone) {
			findViewById(R.id.llRefuelZone).setVisibility(View.VISIBLE);
			fillRefuelZone();
		} else
			findViewById(R.id.llRefuelZone).setVisibility(View.GONE);

		// fill expense zone data
		if (showExpenseZone) {
			findViewById(R.id.llExpenseZone).setVisibility(View.VISIBLE);
			fillExpenseZone();
		} else
			findViewById(R.id.llExpenseZone).setVisibility(View.GONE);

		// fill statistics zone
		if (showStatistcsZone) {
			findViewById(R.id.llStatistcsZone).setVisibility(View.VISIBLE);
			fillStatisticsZone();
		} else
			findViewById(R.id.llStatistcsZone).setVisibility(View.GONE);

		// fill statistics zone
		if (showToDoZone) {
			findViewById(R.id.llToDoZone).setVisibility(View.VISIBLE);
			fillToDoZone();
		} else
			findViewById(R.id.llToDoZone).setVisibility(View.GONE);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (reportDb != null) {
			reportDb.close();
			reportDb = null;
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (isSendStatistics)
			AndiCarStatistics.sendFlurryStartSession(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (isSendStatistics)
			AndiCarStatistics.sendFlurryEndSession(this);
	}

	protected AdapterView.OnItemSelectedListener spinnerCarOnItemSelectedListener = new AdapterView.OnItemSelectedListener() {
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {

			if (isActivityOnLoading)
				return;

			mCarId = arg3;
			mPrefEditor.putLong("CurrentCar_ID", arg3);
			mPrefEditor.putLong("CarCurrency_ID",
					mDbAdapter.getCarCurrencyID(arg3));
			mPrefEditor.putLong("CarUOMVolume_ID",
					mDbAdapter.getCarUOMVolumeID(arg3));
			mPrefEditor.putLong("CarUOMLength_ID",
					mDbAdapter.getCarUOMLengthID(arg3));
			mPrefEditor.commit();
			fillCarIndexLabel();
			initZones();
		}

		public void onNothingSelected(AdapterView<?> arg0) {
		}
	};

	private View.OnTouchListener spinnerOnTouchListener = new View.OnTouchListener() {
		public boolean onTouch(View view, MotionEvent me) {
			isActivityOnLoading = false;
			return false;
		}
	};

	private OnClickListener btnListMileageClickListener = new OnClickListener() {

		public void onClick(View arg0) {
			Intent i = new Intent(mainContext,
					MileageListReportActivity.class);
			startActivity(i);
		}
	};

	private OnClickListener btnInsertMileageClickListener = new OnClickListener() {

		public void onClick(View arg0) {
			Intent i = new Intent(mainContext,
					MileageEditActivity.class);
			i.putExtra("Operation", "N");
			startActivityForResult(i,
					ACTIVITY_MILEAGEINSERT_REQUEST_CODE);
		}
	};

	private OnClickListener btnListRefuelClickListener = new OnClickListener() {

		public void onClick(View arg0) {
			Intent i = new Intent(mainContext,
					RefuelListReportActivity.class);
			startActivity(i);
		}
	};

	private OnClickListener btnInsertRefuelClickListener = new OnClickListener() {

		public void onClick(View arg0) {
			Intent i = new Intent(mainContext,
					RefuelEditActivity.class);
			// refuelInsertIntent.putExtra("CurrentCar_ID", mCarId);
			i.putExtra("Operation", "N");
			startActivityForResult(i,
					ACTIVITY_REFUELINSERT_REQUEST_CODE);
		}
	};

	private OnClickListener btnExpenseListClickListener = new OnClickListener() {

		public void onClick(View arg0) {
			Intent i = new Intent(mainContext,
					ExpensesListReportActivity.class);
			startActivity(i);
		}
	};

	private OnClickListener btnInsertExpenseClickListener = new OnClickListener() {

		public void onClick(View arg0) {
			Intent i = new Intent(mainContext,
					ExpenseEditActivity.class);
			// refuelInsertIntent.putExtra("CurrentCar_ID", mCarId);
			i.putExtra("Operation", "N");
			startActivityForResult(i,
					ACTIVITY_EXPENSEINSERT_REQUEST_CODE);
		}
	};

	private OnClickListener btnListToDoClickListener = new OnClickListener() {

		public void onClick(View arg0) {
			Intent i = new Intent(mainContext,
					ToDoListReportActivity.class);
			startActivity(i);
		}
	};

	private OnClickListener btnInsertToDoClickListener = new OnClickListener() {

		public void onClick(View arg0) {
			Intent i = new Intent(mainContext,
					TaskEditActivity.class);
			// refuelInsertIntent.putExtra("CurrentCar_ID", mCarId);
			i.putExtra("Operation", "N");
			startActivityForResult(i,
					ACTIVITY_TODOINSERT_REQUEST_CODE);
		}
	};

	private OnClickListener btnInsertGPSTrackClickListener = new OnClickListener() {

		public void onClick(View arg0) {
			Intent i = new Intent(mainContext,
					GPSTrackController.class);
			i.putExtra("Operation", "N");
			startActivity(i);
		}
	};

	private OnClickListener btnListGPSTrackClickListener = new OnClickListener() {

		public void onClick(View arg0) {
			Intent i = new Intent(mainContext,
					GPSTrackListReportActivity.class);
			startActivity(i);
		}
	};

	private OnClickListener btnShowOnMapGPSTrackClickListener = new OnClickListener() {

		public void onClick(View arg0) {
			Intent i = new Intent(mainContext,
					GPSTrackMap.class);
			i.putExtra("gpsTrackId",
					Long.toString(gpsTrackId));
			startActivity(i);
		}
	};

	private void fillDriverCar() {
		Cursor c = null;
		
		// get the last selected car id
		mCarId = mPreferences.getLong("CurrentCar_ID", -1);
		if (mCarId == -1) { // new install or last used car was
							// deleted/inactivated
			c = mDbAdapter.query(MainDbAdapter.CAR_TABLE_NAME,
					MainDbAdapter.genColName,
					MainDbAdapter.GEN_COL_ISACTIVE_NAME + " = \'Y\'", null,
					null, null, MainDbAdapter.GEN_COL_NAME_NAME);
			if (c.moveToFirst()) { // active car exists
				mCarId = c.getLong(MainDbAdapter.GEN_COL_ROWID_POS);
				initSpinner(spnCar, MainDbAdapter.CAR_TABLE_NAME,
						MainDbAdapter.genColName,
						new String[] { MainDbAdapter.GEN_COL_NAME_NAME },
						MainDbAdapter.isActiveCondition, null,
						MainDbAdapter.GEN_COL_NAME_NAME, mCarId, false);
				c.close();
				mPrefEditor.putLong("CurrentCar_ID", mCarId);
				mPrefEditor.putLong("CarCurrency_ID",
						mDbAdapter.getCarCurrencyID(mCarId));
				mPrefEditor.putLong("CarUOMVolume_ID",
						mDbAdapter.getCarUOMVolumeID(mCarId));
				mPrefEditor.putLong("CarUOMLength_ID",
						mDbAdapter.getCarUOMLengthID(mCarId));
				mPrefEditor.commit();
			} 
			else {
                AndiCarDialogBuilder builder = new AndiCarDialogBuilder(MainActivity.this, 
                		AndiCarDialogBuilder.DIALOGTYPE_INFO, mResource.getString(R.string.GEN_Info));
				builder.setMessage(mResource
						.getString(R.string.MainActivity_NoCurrentCarMessage));
				builder.setCancelable(false);
				builder.setPositiveButton(mResource.getString(R.string.GEN_OK),
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog, int id) {
								Intent i = new Intent(MainActivity.this, CommonListActivity.class);
						        i.putExtra("ListSource", MainDbAdapter.CAR_TABLE_NAME);
								i.putExtra("ExitAfterInsert", true);
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
				c.close();
				return;
			}
		}
		fillCarIndexLabel();
		// check if drivers exists
		c = mDbAdapter.query(MainDbAdapter.DRIVER_TABLE_NAME,
				MainDbAdapter.genColName, MainDbAdapter.GEN_COL_ISACTIVE_NAME
						+ " = \'Y\'", null, null, null, null);
		if (!c.moveToFirst()) { // no active driver exist
            AndiCarDialogBuilder builder = new AndiCarDialogBuilder(MainActivity.this, 
            		AndiCarDialogBuilder.DIALOGTYPE_INFO, mResource.getString(R.string.GEN_Info));
			builder.setMessage(mResource
					.getString(R.string.MainActivity_NoCurrentDriverMessage));
			builder.setCancelable(false);
			builder.setPositiveButton(mResource.getString(R.string.GEN_OK),
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int id) {
							Intent i = new Intent(MainActivity.this, CommonListActivity.class);
							i.putExtra("ListSource", MainDbAdapter.DRIVER_TABLE_NAME);
							i.putExtra("ExitAfterInsert", true);
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
			c.close();
			return;
		}
		if (mPreferences.getLong("LastDriver_ID", -1) < 0) {
			mPrefEditor.putLong("LastDriver_ID",
					c.getLong(MainDbAdapter.GEN_COL_ROWID_POS));
			mPrefEditor.commit();
		}

		c.close();

		btnMileageInsert.setEnabled(true);
		btnMileageList.setEnabled(true);
		btnRefuelInsert.setEnabled(true);
		btnRefuelList.setEnabled(true);
		btnExpenseList.setEnabled(true);
		btnExpenseInsert.setEnabled(true);
	}

	private void fillCarIndexLabel() {
		
		String carIndexLabel;
		BigDecimal carIndex = mDbAdapter.getCarCurrentIndex(mCarId);
		if(mCarId > -1 && carIndex != null){
			tvCarIndex.setVisibility(View.VISIBLE);
			carIndexLabel = mResource.getString(R.string.GEN_IndexLabel) + " " +
					Utils.numberToString(carIndex, true, StaticValues.DECIMALS_LENGTH,StaticValues.ROUNDING_MODE_LENGTH) + " " +
					mDbAdapter.getUOMCode(mPreferences.getLong("CarUOMLength_ID", 0));
			tvCarIndex.setText(carIndexLabel);
		}
		else
			tvCarIndex.setVisibility(View.GONE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, StaticValues.MENU_MILEAGE_ID, 0,
				mResource.getText(R.string.MENU_MileageCaption)).setIcon(
				mResource.getDrawable(R.drawable.ic_menu_mileage));
		menu.add(0, StaticValues.MENU_GPSTRACK_ID, 0,
				mResource.getText(R.string.MENU_GPSTrackCaption)).setIcon(
				mResource.getDrawable(R.drawable.ic_menu_gpstrack));
		menu.add(0, StaticValues.MENU_REFUEL_ID, 0,
				mResource.getText(R.string.MENU_RefuelCaption)).setIcon(
				mResource.getDrawable(R.drawable.ic_menu_refuel));
		menu.add(0, StaticValues.MENU_EXPENSES_ID, 0,
				mResource.getText(R.string.MENU_ExpenseCaption)).setIcon(
				mResource.getDrawable(R.drawable.ic_menu_expenses));
		menu.add(0, StaticValues.MENU_TODO_ID, 0,
				mResource.getText(R.string.MENU_TodoCaption)).setIcon(
				mResource.getDrawable(R.drawable.ic_menu_task));
		menu.add(0, StaticValues.MENU_ADDON_ID, 0,
				mResource.getText(R.string.MENU_AddOnServicesCaption)).setIcon(
				mResource.getDrawable(R.drawable.ic_menu_star));
		menu.add(0, StaticValues.MENU_PREFERENCES_ID, 0,
				mResource.getText(R.string.MENU_PreferencesCaption)).setIcon(
				mResource.getDrawable(R.drawable.ic_menu_preferences));
		menu.add(0, StaticValues.MENU_ABOUT_ID, 0,
				mResource.getText(R.string.MENU_AboutCaption)).setIcon(
				mResource.getDrawable(R.drawable.ic_menu_info_details));
//		menu.add(0, StaticValues.MENU_RATE_COMMENT_ID, 0,
//				mResource.getText(R.string.MENU_RateCommentCaption)).setIcon(
//				mResource.getDrawable(R.drawable.ic_menu_star));
		menu.add(0, StaticValues.MENU_LOCALIZE_ID, 0,
				mResource.getText(R.string.MENU_LocalizeAndiCar)).setIcon(
				mResource.getDrawable(R.drawable.ic_menu_edit));
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == StaticValues.MENU_PREFERENCES_ID) {
			startActivity(new Intent(this, AndiCarPreferencesActivity.class));
			// Intent i = new Intent(this, AndiCarPreferencesActivity.class);
			// startActivityForResult(i, SETTINGS_ACTIVITY_REQUEST_CODE);
			// return true;
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
		} else if (item.getItemId() == StaticValues.MENU_RATE_COMMENT_ID) {
			try {
				startActivity(new Intent(
						Intent.ACTION_VIEW,
						Uri.parse("market://search?q=pname:org.andicar.activity")));
			} catch (Exception e) {
	            AndiCarDialogBuilder builder = new AndiCarDialogBuilder(MainActivity.this, 
	            		AndiCarDialogBuilder.DIALOGTYPE_WARNING, mResource.getString(R.string.GEN_Warning));
				builder.setMessage(mResource
						.getString(R.string.MainActivity_NoMarketAccessMsg));
				builder.setCancelable(false);
				builder.setPositiveButton(mResource.getString(R.string.GEN_OK),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
				AlertDialog alert = builder.create();
				alert.show();
			}
		} else if (item.getItemId() == StaticValues.MENU_ADDON_ID) {
			startActivity(new Intent(this, AddOnServicesList.class));
		} else if (item.getItemId() == StaticValues.MENU_LOCALIZE_ID) {
			startActivity(new Intent(
					Intent.ACTION_VIEW,
					Uri.parse("http://www.andicar.org/localizing-andicar")));
		}
		else if (item.getItemId() == StaticValues.MENU_TODO_ID) {
			startActivity(new Intent(this, ToDoListReportActivity.class));
		}
		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
	}

	/* (non-Javadoc)
	 * @see org.andicar.activity.BaseActivity#setSpecificLayout()
	 */
	@Override
	public void setSpecificLayout() {
	}
}
