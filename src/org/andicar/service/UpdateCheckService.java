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

import org.andicar.activity.R;
import org.andicar.utils.StaticValues;
import org.apache.http.util.ByteArrayBuffer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
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

//		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        try {
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
            final String s = new String(baf.toByteArray());
            /* Get current Version Number */
            int curVersion = getPackageManager().getPackageInfo("org.andicar.activity", 0).versionCode;
            int newVersion = Integer.valueOf(s);

            /* Is a higher version than the current already out? */
            if (newVersion > curVersion) {
                mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                notification = null;
                Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pname:org.andicar.activity"));
                PendingIntent contentIntent = PendingIntent.getActivity(UpdateCheckService.this, 0, marketIntent, 0);

                CharSequence title = getText(R.string.Notif_UpdateTitle);
                String message = getString(R.string.Notif_UpdateMsg);
                notification = new Notification(R.drawable.stat_sys_info_2, message,
                        System.currentTimeMillis());
		        notification.flags |= Notification.DEFAULT_LIGHTS;
		        notification.flags |= Notification.DEFAULT_SOUND;
                notification.flags |= Notification.FLAG_AUTO_CANCEL;
                notification.setLatestEventInfo(UpdateCheckService.this, title, message, contentIntent);
                mNM.notify(0, notification);
            }
        } catch (Exception e) {
    		Log.i("UpdateService", "Service failed.");
        	e.printStackTrace();
        }
	}
}
