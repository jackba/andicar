 /*
 * AndiCar - car management software for Android powered devices
 * Copyright (C) 2010 - 2011 Miklos Keresztes (miklos.keresztes@gmail.com)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT AY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.andicar.service;

import org.andicar2.activity.R;
import org.andicar.activity.dialog.ToDoNotificationDialog;
import org.andicar.persistence.DB;
import org.andicar.persistence.MainDbAdapter;
import org.andicar.utils.AndiCarExceptionHandler;
import org.andicar.utils.StaticValues;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;

/**
 * @author Miklos Keresztes
 *
 */
public class ToDoNotificationService extends Service {

	public static final int TRIGGERED_BY_TIME = 0; 
	public static final int TRIGGERED_BY_MILEAGE = 1; 

	private MainDbAdapter mDb =null;
	private long mToDoID = 0;
	private long mCarID = 0;
	private Bundle mBundleExtras = null;
	private NotificationManager mNM = null;
	private Notification notification = null;
	
	/**
	 * 
	 */
	public ToDoNotificationService() {
	}

	/* (non-Javadoc)
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	/* (non-Javadoc)
	 * @see android.app.Service#onStart(android.content.Intent, int)
	 */
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		if(getSharedPreferences(StaticValues.GLOBAL_PREFERENCE_NAME, Context.MODE_MULTI_PROCESS).getBoolean("SendCrashReport", true))
			Thread.setDefaultUncaughtExceptionHandler(
	                    new AndiCarExceptionHandler(Thread.getDefaultUncaughtExceptionHandler(), this));

		mBundleExtras  = intent.getExtras();
		mDb = new MainDbAdapter(this);
		if(mBundleExtras != null){ //postponed to-do
			if(!mBundleExtras.getBoolean("setJustNextRun")){
				mToDoID = mBundleExtras.getLong("ToDoID");
				mCarID = mBundleExtras.getLong("CarID");
				String sql = " SELECT * "+
							" FROM " + MainDbAdapter.TODO_TABLE_NAME +
							" WHERE " + 
								DB.sqlConcatTableColumn(MainDbAdapter.TODO_TABLE_NAME, MainDbAdapter.TODO_COL_ISDONE_NAME) + "='N' " + 
								" AND " + DB.sqlConcatTableColumn(MainDbAdapter.TODO_TABLE_NAME, MainDbAdapter.GEN_COL_ISACTIVE_NAME) + "='Y' ";
				if(mToDoID > 0){
					sql = sql + " AND " + 
							DB.sqlConcatTableColumn(MainDbAdapter.TODO_TABLE_NAME, MainDbAdapter.GEN_COL_ROWID_NAME) + " = " + mToDoID;
				}
				if(mCarID > 0){
					sql = sql + " AND " + 
							DB.sqlConcatTableColumn(MainDbAdapter.TODO_TABLE_NAME, MainDbAdapter.TODO_COL_CAR_ID_NAME) + " = " + mCarID;
				}
	
				Cursor toDoCursor = mDb.execSelectSql(sql, null);
				while(toDoCursor.moveToNext())
					checkNotifToDo(toDoCursor);
				toDoCursor.close();
			}
			
			setNextRunForDate();
		}
		mDb.close();
		stopSelf();
	}
	
	private void checkNotifToDo(Cursor toDoCursor){
		long toDoID = toDoCursor.getLong(MainDbAdapter.GEN_COL_ROWID_POS);
		String sql = " SELECT * " +
						" FROM " + MainDbAdapter.TASK_TABLE_NAME +
						" WHERE " + DB.sqlConcatTableColumn(MainDbAdapter.TASK_TABLE_NAME, MainDbAdapter.GEN_COL_ROWID_NAME) + " = ?";
		String argVals[] = {Long.toString(toDoCursor.getLong(MainDbAdapter.TODO_COL_TASK_ID_POS))};
		Cursor taskCursor = mDb.query(sql, argVals);
		boolean showNotif = false;
		String contentText = "";
		String carUOMCode = "";
		String minutesOrDays = "";
		long carCurrentOdodmeter = 0;
//		long todoDueMileage = 0;
		long todoAlarmMileage = 0;
//		long todoDueDateSec = 0;
		long todoAlarmDate = 0;
		long currentDateSec = System.currentTimeMillis() / 1000;
		int notifTrigger = -1;
		
		if(taskCursor != null && taskCursor.moveToNext()){
			
			if(toDoCursor.getString(MainDbAdapter.TODO_COL_CAR_ID_POS) != null){
				Cursor carCursor = mDb.fetchRecord(MainDbAdapter.CAR_TABLE_NAME, MainDbAdapter.carTableColNames, toDoCursor.getLong(MainDbAdapter.TODO_COL_CAR_ID_POS));
				if(carCursor != null){
					contentText = getString(R.string.GEN_CarLabel) + " " + carCursor.getString(MainDbAdapter.GEN_COL_NAME_POS);
					carCurrentOdodmeter = carCursor.getLong(MainDbAdapter.CAR_COL_INDEXCURRENT_POS);
					carUOMCode = mDb.getUOMCode(carCursor.getLong(MainDbAdapter.CAR_COL_UOMLENGTH_ID_POS));
					carCursor.close();
				}
			}
			
			if(taskCursor.getString(MainDbAdapter.TASK_COL_SCHEDULEDFOR_POS).equals(StaticValues.TASK_SCHEDULED_FOR_TIME) || 
					taskCursor.getString(MainDbAdapter.TASK_COL_SCHEDULEDFOR_POS).equals(StaticValues.TASK_SCHEDULED_FOR_BOTH)){
				//check time
//				todoDueDateSec = toDoCursor.getLong(MainDbAdapter.TODO_COL_DUEDATE_POS);
				todoAlarmDate = toDoCursor.getLong(MainDbAdapter.TODO_COL_NOTIFICATIONDATE_POS);
				currentDateSec = System.currentTimeMillis() / 1000;

				if(todoAlarmDate  <= currentDateSec){
					showNotif = true;
					notifTrigger = TRIGGERED_BY_TIME;
				}
						
			}
			if(taskCursor.getString(MainDbAdapter.TASK_COL_SCHEDULEDFOR_POS).equals(StaticValues.TASK_SCHEDULED_FOR_MILEAGE) || 
						taskCursor.getString(MainDbAdapter.TASK_COL_SCHEDULEDFOR_POS).equals(StaticValues.TASK_SCHEDULED_FOR_BOTH)){
//				todoDueMileage = toDoCursor.getLong(MainDbAdapter.TODO_COL_DUEMILAGE_POS);
				todoAlarmMileage = toDoCursor.getLong(MainDbAdapter.TODO_COL_NOTIFICATIONMILEAGE_POS);
				if(todoAlarmMileage <= carCurrentOdodmeter){
					showNotif = true;
					notifTrigger = TRIGGERED_BY_MILEAGE;
				}
				
			}
			if(taskCursor.getInt(MainDbAdapter.TASK_COL_TIMEFREQUENCYTYPE_POS) == StaticValues.TASK_TIMEFREQUENCYTYPE_DAILY){
				minutesOrDays = getString(R.string.GEN_Min);
			}
			else{
				minutesOrDays = getString(R.string.GEN_Days);
			}

			if(showNotif){
			    mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
			    notification = new Notification(R.drawable.icon_sys_alarm, "AndiCar " + getString(R.string.GEN_ToDoAlert), System.currentTimeMillis());
				CharSequence contentTitle = getString(R.string.GEN_TaskLabel) + " " + 
						taskCursor.getString(MainDbAdapter.GEN_COL_NAME_POS);
				Intent i = new Intent(this, ToDoNotificationDialog.class);
				i.putExtra("ToDoID", toDoID);
				i.putExtra("NotifTitle", contentTitle);
				i.putExtra("NotifText", contentText);
				i.putExtra("TriggeredBy", notifTrigger);
//				i.putExtra("CarCurrentOdodmeter",carCurrentOdodmeter);
//				i.putExtra("TodoDueMileage",todoDueMileage);
//				i.putExtra("TodoDueDateSec",todoDueDateSec);
				i.putExtra("CarUOMCode",carUOMCode);
				i.putExtra("MinutesOrDays", minutesOrDays);
				
		        notification.setLatestEventInfo(this, 
		        		contentTitle, contentText, 
		        		PendingIntent.getActivity(this, ((Long)toDoID).intValue(), i, PendingIntent.FLAG_UPDATE_CURRENT));
		        notification.flags |= Notification.DEFAULT_LIGHTS;
		        notification.flags |= Notification.DEFAULT_VIBRATE;
		        notification.flags |= Notification.DEFAULT_SOUND;
		        notification.flags |= Notification.FLAG_AUTO_CANCEL;
		        notification.flags |= Notification.FLAG_NO_CLEAR;
		        mNM.notify( ((Long)toDoID).intValue(), notification);
			}
		}
		else{
			
		}
		if(taskCursor != null)
			taskCursor.close();
	}
	
	private void setNextRunForDate(){
		String sql = 
			" SELECT * " +
			" FROM " + MainDbAdapter.TODO_TABLE_NAME + 
			" WHERE " + 
				DB.sqlConcatTableColumn(MainDbAdapter.TODO_TABLE_NAME, MainDbAdapter.GEN_COL_ISACTIVE_NAME) + "='Y' " +
				" AND " + DB.sqlConcatTableColumn(MainDbAdapter.TODO_TABLE_NAME, MainDbAdapter.TODO_COL_ISDONE_NAME) + "='N' " +
				" AND " + DB.sqlConcatTableColumn(MainDbAdapter.TODO_TABLE_NAME, MainDbAdapter.TODO_COL_NOTIFICATIONDATE_NAME) + " >= ? " +
				" AND " + DB.sqlConcatTableColumn(MainDbAdapter.TODO_TABLE_NAME, MainDbAdapter.TODO_COL_NOTIFICATIONDATE_NAME) + " IS NOT NULL " +
			" ORDER BY " + DB.sqlConcatTableColumn(MainDbAdapter.TODO_TABLE_NAME, MainDbAdapter.TODO_COL_NOTIFICATIONDATE_NAME) + " ASC ";
		long currentSec = System.currentTimeMillis() / 1000;
		String selArgs[] = {Long.toString(currentSec)};
		Cursor c = mDb.execSelectSql(sql, selArgs);
		if(c.moveToNext()){
			long notifDate = c.getLong(MainDbAdapter.TODO_COL_NOTIFICATIONDATE_POS);
			Intent i = new Intent(this, ToDoNotificationService.class);
			i.putExtra("setJustNextRun", false);
			PendingIntent pIntent = PendingIntent.getService(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
			AlarmManager am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
			am.set(AlarmManager.RTC_WAKEUP, notifDate * 1000, pIntent);
		}
		c.close();
	}
}
