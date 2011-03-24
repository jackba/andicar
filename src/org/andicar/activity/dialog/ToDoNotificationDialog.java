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

import java.math.BigDecimal;
import java.util.Calendar;

import org.andicar.activity.EditActivityBase;
import org.andicar.activity.R;
import org.andicar.persistence.MainDbAdapter;
import org.andicar.persistence.ReportDbAdapter;
import org.andicar.service.ToDoManagementService;
import org.andicar.service.ToDoNotificationService;
import org.andicar.utils.StaticValues;
import org.andicar.utils.Utils;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Miklos Keresztes
 *
 */
public class ToDoNotificationDialog extends EditActivityBase {
	private boolean isOKPressed = false;
	private boolean isTodoOK = true;
	String notifTitle = "";
	String notifText = "";
	private long mToDoID;
	private long mTaskID; 
	int triggeredBy = -1;
	private TextView tvText1;
	private TextView tvText2;
	private TextView tvText3;
	private TextView tvText4;
	private TextView tvPostponeUOM;
	private EditText etPostpone;
	private LinearLayout llActionZone;
	private long carCurrentOdodmeter;
	private long todoDueMileage;
	private long todoDueDateSec;
	private String carUOMCode;
	private String minutesOrDays;
	
	/* (non-Javadoc)
	 * @see org.andicar.activity.EditActivityBase#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle icicle) {
//		final boolean customTitleSupported = requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
//		TextView myTitleText = null;
//	    if ( customTitleSupported ) {
//	        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.todonotif_titlebar);
//	    }

	    super.onCreate(icicle);
//	    if(customTitleSupported){
//	        myTitleText = (TextView) findViewById(R.id.tvTitle);
//		    myTitleText.setText("AndiCar " + getString(R.string.GEN_ToDoAlert));
//	    }
//	    else
		setTitle("AndiCar " + getString(R.string.GEN_ToDoAlert));

		notifTitle = mBundleExtras.getString("NotifTitle");
		notifText = mBundleExtras.getString("NotifText");
		mToDoID = mBundleExtras.getLong("ToDoID");
		triggeredBy = mBundleExtras.getInt("TriggeredBy");
		carUOMCode = mBundleExtras.getString("CarUOMCode");
		minutesOrDays = mBundleExtras.getString("MinutesOrDays");

		Bundle whereConditions = new Bundle();
		whereConditions.putString(
				ReportDbAdapter.sqlConcatTableColumn(
						MainDbAdapter.TODO_TABLE_NAME,
						MainDbAdapter.GEN_COL_ROWID_NAME)
						+ "= ", Long.toString(mToDoID));
		
		ReportDbAdapter reportDb = new ReportDbAdapter(this, "todoListViewSelect", whereConditions);
		reportDb.setReportSql("todoListViewSelect", whereConditions);
		Cursor todoReportCursor = reportDb.fetchReport(1);

		if (todoReportCursor != null && todoReportCursor.moveToFirst()) {
			tvText2.setTextColor(Color.RED);
			todoDueMileage = todoReportCursor.getLong(5);
			carCurrentOdodmeter = todoReportCursor.getLong(12);
			todoDueDateSec= todoReportCursor.getLong(4);
			
			if(triggeredBy == ToDoNotificationService.TRIGGERED_BY_MILEAGE){
				tvPostponeUOM.setText(carUOMCode);
				etPostpone.setText("100");
				if(todoDueMileage - carCurrentOdodmeter > 0){
					tvText2.setText(getString(R.string.ToDo_MileageLeft) + 
							Utils.numberToString(todoDueMileage - carCurrentOdodmeter, true, StaticValues.DECIMALS_LENGTH, StaticValues.ROUNDING_MODE_LENGTH) +
							" " + carUOMCode);
				}
				else{
					tvText2.setText(getString(R.string.ToDo_CurrentIndex) + 
							Utils.numberToString(carCurrentOdodmeter, true, StaticValues.DECIMALS_LENGTH, StaticValues.ROUNDING_MODE_LENGTH) +
							" " + carUOMCode);
				}
			}
			else{
				tvPostponeUOM.setText(minutesOrDays);
				long currentSecs = System.currentTimeMillis() / 1000;
				if(minutesOrDays.equals(getString(R.string.GEN_Min))){
					etPostpone.setText("30");
					tvText2.setText(getString(R.string.ToDo_MinutesLeft) + 
							Utils.numberToString(((todoDueDateSec - currentSecs) / 60) + 1, true, StaticValues.DECIMALS_LENGTH, StaticValues.ROUNDING_MODE_LENGTH));
				}
				else{
					etPostpone.setText("1");
					tvText2.setText(getString(R.string.ToDo_DaysLeft) + 
							Utils.numberToString(((todoDueDateSec - currentSecs) / 3600 / 24) + 1, true, StaticValues.DECIMALS_LENGTH, StaticValues.ROUNDING_MODE_LENGTH));
				}
				if(todoDueDateSec - currentSecs < 0){
					tvText2.setText("");
				}
			}
			
			
			String dataString = todoReportCursor.getString(1);
			mTaskID = todoReportCursor.getLong(11);
    		if(dataString.contains("[#5]"))
    			tvText1.setTextColor(Color.RED);
    		else if(dataString.contains("[#15]"))
    			tvText1.setTextColor(Color.GREEN);
    		else
    			tvText1.setTextColor(Color.YELLOW);
    		
    		String text = dataString
							.replace("[#1]", mResource.getString(R.string.GEN_TypeLabel))
							.replace("[#2]", mResource.getString(R.string.GEN_TaskLabel))
							.replace("[#3]", "\n" + mResource.getString(R.string.GEN_CarLabel))
							.replace("[#4]", mResource.getString(R.string.GEN_StatusLabel))
							.replace("[#5]", mResource.getString(R.string.ToDo_OverdueLabel))
							.replace("[#6]", mResource.getString(R.string.ToDo_ScheduledLabel))
							.replace("[#15]", mResource.getString(R.string.ToDo_DoneLabel));
 
			tvText1.setText(text);
			
    		long time = System.currentTimeMillis();
    		
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
					.replace("([#13] [#14])","");
//					.replace("[#13]", mResource.getString(R.string.ToDo_EstimatedMileageDate))
//					.replace("[#14]", timeStr);
 
			tvText3.setText(text);
			text = todoReportCursor.getString(todoReportCursor
					.getColumnIndex(ReportDbAdapter.THIRD_LINE_LIST_NAME));
			tvText4.setText(text);
			isTodoOK = true;
		}
		else{
			llActionZone.setVisibility(View.GONE);
			tvText1.setTextColor(Color.RED);
			tvText1.setText(R.string.ERR_061);
			isTodoOK = false;
		}
		if(todoReportCursor != null)
			todoReportCursor.close();
	}

	/* (non-Javadoc)
	 * @see org.andicar.activity.EditActivityBase#saveData()
	 */
	@Override
	protected boolean saveData() {
		isOKPressed = true;
		if(!isTodoOK){
			finish();
			return false;
		}
		CheckBox ckIsDone = (CheckBox)findViewById(R.id.ckIsDone); 
		EditText etPostpone = (EditText)findViewById(R.id.etPostpone);
		BigDecimal postPoneFor;

		ContentValues cvData = new ContentValues();
		if(ckIsDone.isChecked()){
			cvData.put( MainDbAdapter.TODO_COL_ISDONE_NAME,
	                (ckIsDone.isChecked() ? "Y" : "N") );
		}
		else{
	        String strPostPoneFor = etPostpone.getText().toString();
	        if( strPostPoneFor != null && strPostPoneFor.length() > 0 ) {
	            try {
	            	postPoneFor = new BigDecimal( strPostPoneFor );
	            }
	            catch( NumberFormatException e ) {
	                Toast toast = Toast.makeText( getApplicationContext(),
	                        mResource.getString( R.string.GEN_NumberFormatException ), Toast.LENGTH_SHORT );
	                toast.show();
		            etPostpone.requestFocus();
	                return false;
	            }
	        }
	        else{
	            Toast toast = Toast.makeText( getApplicationContext(),
	                    mResource.getString( R.string.GEN_FillMandatory ) + "", Toast.LENGTH_SHORT );
	            toast.show();
	            etPostpone.requestFocus();
	            return false;
	        }
			if(triggeredBy == ToDoNotificationService.TRIGGERED_BY_MILEAGE){
				cvData.put( MainDbAdapter.TODO_COL_NOTIFICATIONMILEAGE_NAME, carCurrentOdodmeter +  postPoneFor.longValue());
			}
			else{
				Calendar cal = Calendar.getInstance();
				if(minutesOrDays.equals(getString(R.string.GEN_Min)))
					cal.add(Calendar.MINUTE, postPoneFor.intValue());
				else
					cal.add(Calendar.DAY_OF_YEAR, postPoneFor.intValue());
				cvData.put( MainDbAdapter.TODO_COL_NOTIFICATIONDATE_NAME, cal.getTimeInMillis() / 1000);
			}
		}
		mDbAdapter.updateRecord(MainDbAdapter.TODO_TABLE_NAME, mToDoID, cvData);
		Intent intent = new Intent(this, ToDoManagementService.class);
		intent.putExtra("TaskID", mTaskID);
		intent.putExtra("setJustNextRun", true);
		this.startService(intent);
		finish();
		return true;
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
		tvText4 = (TextView) findViewById(R.id.tvText4);
		etPostpone = (EditText) findViewById(R.id.etPostpone);
		tvPostponeUOM = (TextView) findViewById(R.id.tvPostponeUOM);
		((LinearLayout) findViewById(R.id.fakeFocus)).requestFocus();
		llActionZone = (LinearLayout) findViewById(R.id.llActionZone);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		//if back key used show again the notification
		if(!isOKPressed){
			Intent i = new Intent(this, ToDoNotificationService.class);
			i.putExtra("setJustNextRun", false);
			i.putExtra("ToDoID", mToDoID);
			this.startService(i);
		}
	}

	/* (non-Javadoc)
	 * @see org.andicar.activity.BaseActivity#setSpecificLayout()
	 */
	@Override
	public void setSpecificLayout() {
	}

	/* (non-Javadoc)
	 * @see org.andicar.activity.EditActivityBase#setDefaultValues()
	 */
	@Override
	public void setDefaultValues() {
	}


}
