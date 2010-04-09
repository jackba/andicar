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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import org.andicar.activity.R;

/**
 * Copiryght (C) BIT Software S.R.L. (www.bitsoftware.ro)
 * All rights reserved.
 * @author Miklos Keresztes - BIT Software (www.bitsoftware.ro)
 * @product SocrateOpen (www.socrateopen.ro)
 */
public class ReportService extends Service {

    public class LocalBinder extends Binder {
        public ReportService getService() {
            return ReportService.this;
        }
    }
    private final IBinder mBinder = new LocalBinder();
    private NotificationManager mNM;

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private void showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(R.string.REPORT_SERVICE_STARTED);

        // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(R.drawable.ic_statusbar_reportservice_start, text,
                System.currentTimeMillis());

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, getText(R.string.REPORT_SERVICE_LABEL),
                       text, null);

        mNM.notify(R.string.REPORT_SERVICE_STARTED, notification);
    }
}
