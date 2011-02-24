 /**
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

import org.andicar.activity.R;
import org.andicar.activity.dialog.ToDoNotificationDialog;
import org.andicar.persistence.DB;
import org.andicar.persistence.MainDbAdapter;
import org.andicar.utils.AndiCarExceptionHandler;
import org.andicar.utils.StaticValues;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
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
		if(getSharedPreferences(StaticValues.GLOBAL_PREFERENCE_NAME, 0).getBoolean("SendCrashReport", true))
			Thread.setDefaultUncaughtExceptionHandler(
	                    new AndiCarExceptionHandler(Thread.getDefaultUncaughtExceptionHandler(), this));

		mDb = new MainDbAdapter(this);

		String sql = " SELECT * "+
					" FROM " + MainDbAdapter.TODO_TABLE_NAME +
					" WHERE " + DB.sqlConcatTableColumn(MainDbAdapter.TODO_TABLE_NAME, MainDbAdapter.TODO_COL_ISDONE_NAME) + "='N' ";
		String argVals[] = null;

		mBundleExtras  = intent.getExtras();
		if(mBundleExtras != null){ //postponed to-do
			mToDoID = mBundleExtras.getLong("ToDoID");
			if(mToDoID > 0){
				sql = sql + " AND " + DB.sqlConcatTableColumn(MainDbAdapter.TODO_TABLE_NAME, MainDbAdapter.GEN_COL_ROWID_NAME) + " = ?";
				argVals = new String[1];
				argVals[0] = Long.toString(mToDoID);
			}
		}

		Cursor toDoCursor = mDb.execSelectSql(sql, argVals);
		while(toDoCursor.moveToNext())
			checkNotifToDo(toDoCursor);

		toDoCursor.close();
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
		
		if(taskCursor != null && taskCursor.moveToNext()){
			if(taskCursor.getString(MainDbAdapter.TASK_COL_SCHEDULEDFOR_POS).equals(StaticValues.TASK_SCHEDULED_FOR_TIME) || 
					taskCursor.getString(MainDbAdapter.TASK_COL_SCHEDULEDFOR_POS).equals(StaticValues.TASK_SCHEDULED_FOR_BOTH)){
				//check time
				long todoDueDateSec = toDoCursor.getLong(MainDbAdapter.TODO_COL_DUEDATE_POS);
				long todoTimeReminderStart = taskCursor.getLong(MainDbAdapter.TASK_COL_TIMEREMINDERSTART_POS);
				long todoPostPone;
				long currentSec = System.currentTimeMillis() / 1000;
				if(toDoCursor.getString(MainDbAdapter.TODO_COL_POSTPONEUNTIL_POS) != null)
					todoPostPone = toDoCursor.getLong(MainDbAdapter.TODO_COL_POSTPONEUNTIL_POS);
				else
					todoPostPone = -1;
				
				if(taskCursor.getInt(MainDbAdapter.TASK_COL_TIMEFREQUENCY_POS) == StaticValues.TASK_TIMEFREQUENCYTYPE_DAILY)
					//in this case the reminder start is in hours
					todoTimeReminderStart = todoTimeReminderStart * 3600; //in sec
				else
					//in this case the reminder start is in days
					todoTimeReminderStart = todoTimeReminderStart * 24 * 3600; //in sec
				
				if(todoPostPone > -1 && todoPostPone <= currentSec)
					showNotif = true;
				else if(todoDueDateSec - todoTimeReminderStart <= currentSec)
					showNotif = true;
						
				if(showNotif){
				    mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
				    notification = new Notification(R.drawable.icon_sys_info, "AndiCar " + getString(R.string.GEN_ToDo), System.currentTimeMillis());
					Intent i = new Intent(this, ToDoNotificationDialog.class);
					i.putExtra("TriggeredBy", TRIGGERED_BY_TIME);
					i.putExtra("ToDoID", toDoID);
					CharSequence contentTitle = getString(R.string.GEN_TaskLabel) + " " + taskCursor.getString(MainDbAdapter.GEN_COL_NAME_POS);
					i.putExtra("NotifTitle", contentTitle);
					CharSequence contentText = "";
					if(toDoCursor.getString(MainDbAdapter.TODO_COL_CAR_ID_POS) != null){
						Cursor carCursor = mDb.fetchRecord(MainDbAdapter.CAR_TABLE_NAME, MainDbAdapter.genColName, toDoCursor.getLong(MainDbAdapter.TODO_COL_CAR_ID_POS));
						if(carCursor != null){
							contentText = getString(R.string.GEN_CarLabel) + " " + carCursor.getString(MainDbAdapter.GEN_COL_NAME_POS);
							carCursor.close();
						}
					}
					i.putExtra("NotifText", contentText);
			        notification.setLatestEventInfo(this, 
			        		contentTitle, contentText, 
			        		PendingIntent.getActivity(this, StaticValues.ACTIVITY_REQUEST_CODE_BACKUPSERVICE_EXPIRE, i, PendingIntent.FLAG_UPDATE_CURRENT));
			        notification.flags |= Notification.DEFAULT_LIGHTS;
			        notification.flags |= Notification.DEFAULT_VIBRATE;
			        notification.flags |= Notification.DEFAULT_SOUND;
			        notification.flags |= Notification.FLAG_AUTO_CANCEL;
			        notification.flags |= Notification.FLAG_NO_CLEAR;
			        mNM.notify( ((Long)toDoID).intValue(), notification);
				}
			}
			taskCursor.close();
		}
	}
	
}
