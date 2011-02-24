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
package org.andicar.activity.dialog;

import org.andicar.activity.EditActivityBase;
import org.andicar.activity.R;
import org.andicar.utils.StaticValues;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;

/**
 * @author Miklos Keresztes
 *
 */
public class ToDoNotificationDialog extends EditActivityBase {
	private boolean isOKPressed = false;
	
	/* (non-Javadoc)
	 * @see org.andicar.activity.EditActivityBase#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
	}

	/* (non-Javadoc)
	 * @see org.andicar.activity.EditActivityBase#saveData()
	 */
	@Override
	protected void saveData() {
		isOKPressed = true;
		finish();
	}

	/* (non-Javadoc)
	 * @see org.andicar.activity.EditActivityBase#setLayout()
	 */
	@Override
	protected void setLayout() {
		setContentView(R.layout.todo_notification_dialog);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		//show again the notification
		if(!isOKPressed){
			NotificationManager mNM = null;
			Notification notification = null;
		    mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		    notification = new Notification(R.drawable.icon_sys_info, "ToDo notif", System.currentTimeMillis());
			Intent i = new Intent(this, ToDoNotificationDialog.class);
	        notification.setLatestEventInfo(this, 
	        		"AndiCar ToDo Notification", "ToDo Notification", 
	        		PendingIntent.getActivity(this, StaticValues.ACTIVITY_REQUEST_CODE_BACKUPSERVICE_EXPIRE, i, PendingIntent.FLAG_UPDATE_CURRENT));
	        notification.flags |= Notification.DEFAULT_LIGHTS;
	        notification.flags |= Notification.DEFAULT_VIBRATE;
	        notification.flags |= Notification.DEFAULT_SOUND;
	        notification.flags |= Notification.FLAG_AUTO_CANCEL;
	        notification.flags |= Notification.FLAG_NO_CLEAR;
	        mNM.notify(StaticValues.NOTIF_UPDATECHECK_ID, notification);
		}
	}


}
