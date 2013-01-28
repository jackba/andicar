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

import org.andicar.utils.AndiCarExceptionHandler;
import org.andicar.utils.StaticValues;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AndiCarServiceStarter extends BroadcastReceiver {

	private static final String LOGTAG = "AndiCarServiceStarter";

	@Override
	public void onReceive(Context context, Intent rIntent) {
		if(context.getSharedPreferences(StaticValues.GLOBAL_PREFERENCE_NAME, Context.MODE_MULTI_PROCESS).getBoolean("SendCrashReport", true))
			Thread.setDefaultUncaughtExceptionHandler(
	                    new AndiCarExceptionHandler(Thread.getDefaultUncaughtExceptionHandler(), context));
		try{
			if (rIntent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
					//start services
					startServices(context, "All");
			}
			else if(rIntent.getAction().equals(Intent.ACTION_DATE_CHANGED)){
				startServices(context, "All");
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void startServices(Context context, String whatService) throws Exception{
		AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = null;
		PendingIntent pIntent = null;

		if(whatService.equals("All") || whatService.equals("ToDoNotificationService")){
			//start TO-DO notification service
			Log.i(LOGTAG, "Starting To-Do Notification Service...");
			intent = new Intent(context, ToDoNotificationService.class);
			intent.putExtra("setJustNextRun", false);
			context.startService(intent);
			Log.i(LOGTAG, "Done");
		}

		if(whatService.equals("All") || whatService.equals("BackupService")){
		//start backup service
			Log.i(LOGTAG, "Starting Backup Service...");
			intent = new Intent(context, BackupService.class);
			intent.putExtra("Operation", "SetNextRun");
			pIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), AlarmManager.INTERVAL_DAY, pIntent);
			Log.i(LOGTAG, "Backup service subscription exist.");
		}
	}

}
