/*
 *  AndiCar - a car management software for Android powered devices.
 *
 *  Copyright (C) 2013 Miklos Keresztes (miklos.keresztes@gmail.com)
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

package org.andicar.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import org.andicar2.activity.R;
import org.andicar.activity.miscellaneous.BackupRestoreActivity;
import org.andicar.persistence.FileUtils;
import org.andicar.persistence.MainDbAdapter;
import org.andicar.persistence.AddOnDBAdapter;
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
import android.os.IBinder;
import android.text.format.DateFormat;
import android.util.Log;

public class BackupService extends Service {
	
	private final String LOGTAG = "AndiCarBKService";
    NotificationManager mNM = null;
    Notification notification = null;
    MainDbAdapter db;
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(db != null)
			db.close();
	}


	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		//check subscription validity
		if(getSharedPreferences(StaticValues.GLOBAL_PREFERENCE_NAME, Context.MODE_MULTI_PROCESS).getBoolean("SendCrashReport", true))
			Thread.setDefaultUncaughtExceptionHandler(
	                    new AndiCarExceptionHandler(Thread.getDefaultUncaughtExceptionHandler(), this));

		try {
			db = new MainDbAdapter(this);
		} catch (Exception e1) {
			e1.printStackTrace();
			stopSelf();
		}
		
		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		if(intent.getExtras().getString("Operation").equals("SetNextRun"))
		{
			setNextRun();
		}
		else{
			try{
				backupDb();
			}
			catch(Exception e){
			    notification = new Notification(R.drawable.icon_sys_error, "AndiCar " + 
			    		getString(R.string.AddOn_AutoBackupService_Title),
		                System.currentTimeMillis());
		        notification.flags |= Notification.DEFAULT_LIGHTS;
		        notification.flags |= Notification.DEFAULT_SOUND;

		        notification.setLatestEventInfo(this, 
		        		"AndiCar " + getString(R.string.AddOn_AutoBackupService_Title), 
		        		getString(R.string.AddOn_AutoBackupService_BackupFailed) + "\n" + e.getMessage(), 
		        		PendingIntent.getActivity(this, 0,
		                        new Intent(this, BackupRestoreActivity.class), 0));
		        mNM.notify(StaticValues.NOTIF_BACKUP_SERVICE_EXCEPTION_ID, notification);
			}
			finally{
				setNextRun();
				try{
					deleteOldBackups();
				}
				catch(Exception e){};
			}
		}
		stopSelf();
	}
	
	/**
	 * set the next run date
	 */
	private void setNextRun(){
		Calendar nextSchedule = Calendar.getInstance();
		Calendar currentDate = Calendar.getInstance();
		long timeInMilisToNextRun = -1;
		String scheduleDays;
		
		Intent intent = new Intent(this, BackupService.class);
		intent.putExtra("Operation", "Normal");
		PendingIntent pIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

		String selectSql = "SELECT * " +
							" FROM " + AddOnDBAdapter.ADDON_BK_SCHEDULE_TABLE_NAME +
							" WHERE " + MainDbAdapter.GEN_COL_ISACTIVE_NAME + " = 'Y'";
		Cursor c = db.query(selectSql, null);
		if(c.moveToNext()){ //active schedule exists
			nextSchedule.setTimeInMillis(c.getLong(MainDbAdapter.GEN_COL_NAME_POS)); //set the time part
			//set date to current day
			nextSchedule.set(currentDate.get(Calendar.YEAR), 
					currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DAY_OF_MONTH));

			if(c.getString(AddOnDBAdapter.ADDON_BK_SCHEDULE_COL_FREQUENCY_POS).equals("D")){ //daily schedule
				if(nextSchedule.compareTo(currentDate) < 0){ //current hour > scheduled hour => next run tomorrow
					nextSchedule.add(Calendar.DAY_OF_MONTH, 1);
				}
			}
			else{ //weekly schedule
				scheduleDays = c.getString(AddOnDBAdapter.ADDON_BK_SCHEDULE_COL_DAYS_POS);
				int daysToAdd = -1;
				for(int i = currentDate.get(Calendar.DAY_OF_WEEK) - 1; i < 7; i++){
					if(scheduleDays.substring(i, i+1).equals("1")){
						if(i == (currentDate.get(Calendar.DAY_OF_WEEK) - 1) &&
								nextSchedule.compareTo(currentDate) < 0){ //current hour > scheduled hour => get next run day
							continue;
						}
						else{
							daysToAdd = i - (currentDate.get(Calendar.DAY_OF_WEEK) - 1);
							break;
						}
					}
				}
				if(daysToAdd == -1){ //no next run day in this week
					for(int j = 0; j < currentDate.get(Calendar.DAY_OF_WEEK); j++){
						if(scheduleDays.substring(j, j+1).equals("1")){
							daysToAdd = (7-currentDate.get(Calendar.DAY_OF_WEEK)) + j;
							break;
						}
					}
				}
				nextSchedule.add(Calendar.DAY_OF_MONTH, daysToAdd);
			}
			timeInMilisToNextRun = nextSchedule.getTimeInMillis() - currentDate.getTimeInMillis();
			//set next run of the service
			long triggerTime = System.currentTimeMillis() + timeInMilisToNextRun;
			am.setRepeating(AlarmManager.RTC_WAKEUP, triggerTime, AlarmManager.INTERVAL_DAY, pIntent);
			Log.i(LOGTAG, "BackupService scheduled. Next start:" + DateFormat.getDateFormat(this).format(triggerTime) + " " + DateFormat.getTimeFormat(this).format(triggerTime));
		}
		else{ //no active schedule exists => remove scheduled runs
			am.cancel(pIntent);
			Log.i(LOGTAG, "BackupService not scheduled. No active schedule found.");
		}
		c.close();
	}

	private void backupDb(){
		Intent i = new Intent(this, BackupRestoreActivity.class);

		if(db.backupDb(null, "abk_")){
			if( getSharedPreferences(StaticValues.GLOBAL_PREFERENCE_NAME, Context.MODE_MULTI_PROCESS)
					.getBoolean("AddOn_AutoBackupService_NotifyIfSuccess", true)){
			    notification = new Notification(R.drawable.icon_sys_info, 
			    		"AndiCar " + getString(R.string.AddOn_AutoBackupService_Title),
		                System.currentTimeMillis());
		        notification.flags |= Notification.DEFAULT_LIGHTS;
		        notification.flags |= Notification.DEFAULT_SOUND;
		        notification.flags |= Notification.FLAG_AUTO_CANCEL;
	
		        notification.setLatestEventInfo(this, 
		        		"AndiCar " + getString(R.string.AddOn_AutoBackupService_Title), 
		        		getString(R.string.AddOn_AutoBackupService_BackupSucceeded), 
		        		PendingIntent.getActivity(this, 0, i, 0));
		        mNM.notify(StaticValues.NOTIF_BACKUP_SERVICE_INFO_ID, notification);
			}
		}
		else{
		    notification = new Notification(R.drawable.icon_sys_error, 
		    		"AndiCar " + getString(R.string.AddOn_AutoBackupService_Title),
	                System.currentTimeMillis());
	        notification.flags |= Notification.DEFAULT_LIGHTS;
	        notification.flags |= Notification.DEFAULT_SOUND;
	        notification.flags |= Notification.FLAG_AUTO_CANCEL;

	        notification.setLatestEventInfo(this, 
	        		"AndiCar " + getString(R.string.AddOn_AutoBackupService_Title), 
	        		getString(R.string.AddOn_AutoBackupService_BackupFailed), 
	        		PendingIntent.getActivity(this, 0, i, 0));
	        mNM.notify(StaticValues.NOTIF_BACKUP_SERVICE_ERROR_ID, notification);
		}
		db = new MainDbAdapter(this);
			
	}
	
	private void deleteOldBackups(){
		int noOfBk = 0;
		
		String selectSql = "SELECT * " +
							" FROM " + AddOnDBAdapter.ADDON_BK_SCHEDULE_TABLE_NAME +
							" WHERE " + MainDbAdapter.GEN_COL_ISACTIVE_NAME + " = 'Y'";
		Cursor c = db.query(selectSql, null);
		if(c.moveToFirst()){
			noOfBk = Integer.parseInt(c.getString(MainDbAdapter.GEN_COL_USER_COMMENT_POS));
		}
		c.close();
		if(noOfBk <= 0)
			return;
		
		ArrayList<String> autoBkFileNames = FileUtils.getFileNames(StaticValues.BACKUP_FOLDER, "abk_\\S+[.]db");
		if(autoBkFileNames != null && autoBkFileNames.size() > noOfBk){
            Collections.sort(autoBkFileNames, String.CASE_INSENSITIVE_ORDER);
            Collections.reverse(autoBkFileNames);
            for(int i = noOfBk; i < autoBkFileNames.size(); i++){
            	FileUtils.deleteFile(StaticValues.BACKUP_FOLDER + autoBkFileNames.get(i));            }
		}
			
	}
	
}
