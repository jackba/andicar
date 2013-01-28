/*
 *  AndiCar - car management software for Android powered devices
 *  Copyright (C) 2010 Miklos Keresztes (miklos.keresztes@gmail.com)
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT AY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.andicar.service;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;

import org.andicar2.activity.R;
import org.andicar.activity.dialog.WhatsNewDialog;
import org.andicar.utils.AndiCarExceptionHandler;
import org.andicar.utils.StaticValues;
import org.apache.http.util.ByteArrayBuffer;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class UpdateCheckService extends Service{
    NotificationManager mNM = null;
    Notification notification = null;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);

		if(getSharedPreferences(StaticValues.GLOBAL_PREFERENCE_NAME, Context.MODE_MULTI_PROCESS).getBoolean("SendCrashReport", true))
			Thread.setDefaultUncaughtExceptionHandler(
	                    new AndiCarExceptionHandler(Thread.getDefaultUncaughtExceptionHandler(), this));

        try {
        	Bundle extras = intent.getExtras();
        	if(extras == null || extras.getBoolean("setJustNextRun") || !extras.getBoolean("AutoUpdateCheck")){
        		setNextRun();
        		stopSelf();
        	}
        	
            URL updateURL = new URL(StaticValues.VERSION_FILE_URL);
            URLConnection conn = updateURL.openConnection();
            if(conn == null)
                return;
            InputStream is = conn.getInputStream();
            if(is == null)
            	return;
            BufferedInputStream bis = new BufferedInputStream(is);
            ByteArrayBuffer baf = new ByteArrayBuffer(50);
            int current = 0;
            while((current = bis.read()) != -1){
                 baf.append((byte)current);
            }

            /* Convert the Bytes read to a String. */
            String s = new String(baf.toByteArray());
            /* Get current Version Number */
            int curVersion = getPackageManager().getPackageInfo("org.andicar.activity", 0).versionCode;
            int newVersion = Integer.valueOf(s);

            /* Is a higher version than the current already out? */
            if (newVersion > curVersion) { 
            	//get the whats new message
                updateURL = new URL(StaticValues.WHATS_NEW_FILE_URL);
                conn = updateURL.openConnection();
                if(conn == null)
                    return;
                is = conn.getInputStream();
                if(is == null)
                	return;
                bis = new BufferedInputStream(is);
                baf = new ByteArrayBuffer(50);
                current = 0;
                while((current = bis.read()) != -1){
                     baf.append((byte)current);
                }

                /* Convert the Bytes read to a String. */
                s = new String(baf.toByteArray());

            	
            	mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                notification = null;
                Intent i = new Intent(this, WhatsNewDialog.class);
                i.putExtra("UpdateMsg", s);
                PendingIntent contentIntent = PendingIntent.getActivity(UpdateCheckService.this, 0, i, 0);

                CharSequence title = getText(R.string.Notif_UpdateTitle);
                String message = getString(R.string.Notif_UpdateMsg);
                notification = new Notification(R.drawable.icon_sys_info, message,
                        System.currentTimeMillis());
		        notification.flags |= Notification.DEFAULT_LIGHTS;
		        notification.flags |= Notification.DEFAULT_SOUND;
                notification.flags |= Notification.FLAG_AUTO_CANCEL;
                notification.setLatestEventInfo(UpdateCheckService.this, title, message, contentIntent);
                mNM.notify(StaticValues.NOTIF_UPDATECHECK_ID, notification);
                setNextRun();
            }
            stopSelf();
        } catch (Exception e) {
    		Log.i("UpdateService", "Service failed.");
        	e.printStackTrace();
        }
	}
	
	private void setNextRun(){
		Calendar cal = Calendar.getInstance();
		if(cal.get(Calendar.HOUR_OF_DAY) >= 18)
			cal.add(Calendar.DAY_OF_YEAR, 1);
		cal.set(Calendar.HOUR_OF_DAY, 20);
		cal.set(Calendar.MINUTE, 0);
		Intent i = new Intent(this, UpdateCheckService.class);
		PendingIntent pIntent = PendingIntent.getService(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.RTC, cal.getTimeInMillis(), pIntent);
	}
	
}
