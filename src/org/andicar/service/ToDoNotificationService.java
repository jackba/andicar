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
import org.andicar.activity.dialog.AndiCarDialogBuilder;
import org.andicar.activity.dialog.ToDoNotificationDialog;
import org.andicar.activity.report.ToDoListReportActivity;
import org.andicar.persistence.DB;
import org.andicar.persistence.MainDbAdapter;
import org.andicar.utils.AndiCarExceptionHandler;
import org.andicar.utils.StaticValues;
import org.andicar.utils.Utils;

import com.andicar.addon.activity.ServiceSubscription;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

/**
 * @author Miklos Keresztes
 *
 */
public class ToDoNotificationService extends Service {

	private MainDbAdapter mDb =null;
	private long mToDoID = -1;
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

//		mBundleExtras  = intent.getExtras();
//		if(mBundleExtras != null){ //postponed to-do
//			mToDoID = mBundleExtras.getLong("ToDoID");
//		}
//		
//		mDb = new MainDbAdapter(this);
//		
//		mDb.close();
		checkNotifToDo();
		stopSelf();
	}
	
	private void checkNotifToDo(){
//		String sql = " SELECT " + MainDbAdapter.TODO_TABLE_NAME + ".* " +
//						" FROM " + MainDbAdapter.TODO_TABLE_NAME +
//							" JOIN " + MainDbAdapter.TASK_TABLE_NAME + " ON " + 
//									DB.sqlConcatTableColumn(MainDbAdapter.TODO_TABLE_NAME, MainDbAdapter.TODO_COL_TASK_ID_NAME) + "=" +
//										DB.sqlConcatTableColumn(MainDbAdapter.TASK_TABLE_NAME, MainDbAdapter.GEN_COL_ROWID_NAME) +
//						" WHERE " + DB.sqlConcatTableColumn(MainDbAdapter.TODO_TABLE_NAME, MainDbAdapter.TODO_COL_ISDONE_NAME) + "='N' " +
//								" AND " + DB.sqlConcatTableColumn(MainDbAdapter.TODO_TABLE_NAME, MainDbAdapter.GEN_COL_ROWID_NAME) + " = ?";
//		String argVals[] = {Long.toString(mToDoID)};
//		Cursor c = mDb.query(sql, argVals);
//		if(c.moveToNext()){
//            mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
//            notification = null;
////	        Intent i = new Intent(this, ServiceSubscription.class);
////	        i.putExtra("Operation", subsOperation);
////	        i.putExtra("AddOnType", subsId);
////	        i.putExtra("NotificationMessage", notifMsg);
////
////            CharSequence title = getText(R.string.Notif_UpdateTitle);
////            String message = getString(R.string.Notif_UpdateMsg);
////            notification = new Notification(R.drawable.icon_sys_info, message,
////                    System.currentTimeMillis());
////	        notification.flags |= Notification.DEFAULT_LIGHTS;
////	        notification.flags |= Notification.DEFAULT_SOUND;
////            notification.flags |= Notification.FLAG_AUTO_CANCEL;
////            notification.setLatestEventInfo(UpdateCheckService.this, title, message, contentIntent);
////            mNM.notify(StaticValues.NOTIF_UPDATECHECK_ID, notification);
//		}
		
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
