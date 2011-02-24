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

import java.util.Calendar;

import org.andicar.activity.EditActivityBase;
import org.andicar.activity.R;
import org.andicar.persistence.MainDbAdapter;
import org.andicar.persistence.ReportDbAdapter;
import org.andicar.utils.StaticValues;
import org.andicar.utils.Utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TextView;

/**
 * @author Miklos Keresztes
 *
 */
public class ToDoNotificationDialog extends EditActivityBase {
	private boolean isOKPressed = false;
	String notifTitle = "";
	String notifText = "";
	long toDoID;
	int triggeredBy = -1;
	private TextView tvText1;
	private TextView tvText2;
	private TextView tvText3;
	
	/* (non-Javadoc)
	 * @see org.andicar.activity.EditActivityBase#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		notifTitle = mBundleExtras.getString("NotifTitle");
		notifText = mBundleExtras.getString("NotifText");
		toDoID = mBundleExtras.getLong("ToDoID");
		triggeredBy = mBundleExtras.getInt("TriggeredBy");

		Bundle whereConditions = new Bundle();
		whereConditions.putString(
				ReportDbAdapter.sqlConcatTableColumn(
						MainDbAdapter.TODO_TABLE_NAME,
						MainDbAdapter.GEN_COL_ROWID_NAME)
						+ "= ", Long.toString(toDoID));
		
		ReportDbAdapter reportDb = new ReportDbAdapter(this, "todoListViewSelect", whereConditions);
		reportDb.setReportSql("todoListViewSelect", whereConditions);
		Cursor todoReportCursor = reportDb.fetchReport(1);
		
		if (todoReportCursor != null && todoReportCursor.moveToFirst()) {
			String dataString = todoReportCursor.getString(1);
    		if(dataString.contains("[#5]"))
    			tvText1.setTextColor(Color.RED);
    		else if(dataString.contains("[#15]"))
    			tvText1.setTextColor(Color.GREEN);
    		else
    			tvText1.setTextColor(Color.WHITE);
    		
    		String text = dataString
							.replace("[#1]", mResource.getString(R.string.GEN_TypeLabel))
							.replace("[#2]", mResource.getString(R.string.GEN_TaskLabel))
							.replace("[#3]", mResource.getString(R.string.GEN_CarLabel))
							.replace("[#4]", mResource.getString(R.string.GEN_StatusLabel))
							.replace("[#5]", mResource.getString(R.string.ToDo_OverdueLabel))
							.replace("[#6]", mResource.getString(R.string.ToDo_ScheduledLabel))
							.replace("[#15]", mResource.getString(R.string.ToDo_DoneLabel));
 
			tvText1.setText(text);
			
    		long time = System.currentTimeMillis();
    		Calendar now = Calendar.getInstance();
    		Calendar cal = Calendar.getInstance();
    		
    		long estMileageDueDays = todoReportCursor.getLong(7);
    		String timeStr = "";
			if(estMileageDueDays >= 0){
				if(estMileageDueDays == 99999999999L)
					timeStr = mResource.getString(R.string.ToDo_EstimatedMileageDateNoData);
				else{
					if(todoReportCursor.getString(1).contains("[#5]"))
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
			time = time + todoReportCursor.getLong(7);
			text = todoReportCursor.getString(2);
			text = text
					.replace("[#7]", mResource.getString(R.string.ToDo_ScheduledDateLabel)) 
					.replace("[#8]",  
								DateFormat.getDateFormat(this)
								.format(todoReportCursor.getLong(4) * 1000) + " " +
		 							DateFormat.getTimeFormat(this)
			 								.format(todoReportCursor.getLong(4) * 1000))
					.replace("[#9]", mResource.getString(R.string.GEN_Or2))
					.replace("[#10]", mResource.getString(R.string.ToDo_ScheduledMileageLabel))
					.replace("[#11]", Utils.numberToString(todoReportCursor.getDouble(5) , true, StaticValues.DECIMALS_LENGTH, StaticValues.ROUNDING_MODE_LENGTH))
					.replace("[#12]", mResource.getString(R.string.GEN_Mileage))
					.replace("[#13]", mResource.getString(R.string.ToDo_EstimatedMileageDate))
					.replace("[#14]", timeStr);
 
			tvText2.setText(text);
			text = todoReportCursor.getString(todoReportCursor
					.getColumnIndex(ReportDbAdapter.THIRD_LINE_LIST_NAME));
			tvText3.setText(text);
		}
		if(todoReportCursor != null)
			todoReportCursor.close();
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
		tvText1 = (TextView) findViewById(R.id.tvText1);
		tvText2 = (TextView) findViewById(R.id.tvText2);
		tvText3 = (TextView) findViewById(R.id.tvText3);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		//if back key used show again the notification
		if(!isOKPressed){
			NotificationManager mNM = null;
			Notification notification = null;
		    mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		    notification = new Notification(R.drawable.icon_sys_info, "AndiCar " + getString(R.string.GEN_ToDo), System.currentTimeMillis());
			Intent i = new Intent(this, ToDoNotificationDialog.class);
			i.putExtra("NotifTitle", notifTitle);
			i.putExtra("NotifText", notifText);
			i.putExtra("ToDoID", toDoID);
	        notification.setLatestEventInfo(this, 
	        		notifTitle, notifText, 
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
