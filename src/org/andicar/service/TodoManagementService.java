 /**
 * AndiCar - car management software for Android powered devices
 * Copyright (C) 2011 Miklos Keresztes (miklos.keresztes@gmail.com)
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

import java.util.Calendar;
import java.util.TimeZone;

import org.andicar.persistence.MainDbAdapter;
import org.andicar.utils.AndiCarExceptionHandler;
import org.andicar.utils.StaticValues;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;

/**
 * @author Miklos Keresztes
 * 
 * <hr>
 * <br><b> Description:</b> 
 * <br>This service maintain the todo entries based on the task properties (due date, mileage, linked cars etc.) 
 * <br>All <b>recurent</b> tasks have a predefined number (currently 3) of todo entries for each linked car.
 * <br>A new todo is automatically created when an existing todo is done or deactivated. 
 *
 */
public class TodoManagementService extends Service {

	private MainDbAdapter mDb =null;
	private Bundle mBundleExtras;
	private long mTaskID = 0;
	private long mCarID = 0;
	private static int mTodoCount = 3;

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
		
		mBundleExtras = intent.getExtras();
		if(mBundleExtras != null){
			mTaskID = mBundleExtras.getLong("TaskID");
			mCarID = mBundleExtras.getLong("CarID");
		}
		
		mDb = new MainDbAdapter(this);
		
		//#1 check todo's for none recurrent tasks
		maintainNonRecurrentTasks();
		//#2 check todo's for recurrent tasks
		maintainRecurentTasks();

		mDb.close();
		stopSelf();
	}
	
	private void maintainNonRecurrentTasks(){
		Cursor taskCursor = null;
		Cursor todoCursor = null;
		String taskSelection = null;
		String taskScheduledFor = null; //time, mileage, both
		String todoSelection = null;
		String[] taskSelectionArgs = null;
		String[] todoSelectionArgs = null;
		ContentValues toDoContent = new ContentValues();
		long taskId = -1;

		taskSelection = MainDbAdapter.isActiveCondition + 
				" AND " + MainDbAdapter.TASK_COL_ISRECURENT_NAME + "='N' ";
		
		if(mTaskID > 0){
			taskSelection = taskSelection + " AND " + MainDbAdapter.GEN_COL_ROWID_NAME + " = ?";
			taskSelectionArgs = new String[1];
			taskSelectionArgs[0] = Long.toString(mTaskID);
		}
		
		taskCursor = mDb.query(MainDbAdapter.TASK_TABLE_NAME, MainDbAdapter.taskTableColNames, taskSelection, taskSelectionArgs, null, null, null);
		
		todoSelection = MainDbAdapter.TODO_COL_TASK_ID_NAME + "=? ";
		todoSelectionArgs = new String[1];

		while(taskCursor.moveToNext()){
			taskId = taskCursor.getLong(MainDbAdapter.GEN_COL_ROWID_POS);
			taskScheduledFor = taskCursor.getString(MainDbAdapter.TASK_COL_SCHEDULEDFOR_POS);
			todoSelectionArgs[0] = Long.toString(taskId);
			todoCursor = mDb.query(MainDbAdapter.TODO_TABLE_NAME, MainDbAdapter.todoTableColNames, todoSelection, todoSelectionArgs, null, null, null);
			toDoContent.clear();
			toDoContent.put(MainDbAdapter.GEN_COL_NAME_NAME, taskCursor.getString(MainDbAdapter.GEN_COL_NAME_POS));
			toDoContent.put(MainDbAdapter.GEN_COL_USER_COMMENT_NAME, taskCursor.getString(MainDbAdapter.GEN_COL_USER_COMMENT_POS));
			toDoContent.put(MainDbAdapter.TODO_COL_TASK_ID_NAME, taskId);

			if(taskScheduledFor.equals(StaticValues.TASK_SCHEDULED_FOR_TIME) || taskScheduledFor.equals(StaticValues.TASK_SCHEDULED_FOR_BOTH))
				toDoContent.put(MainDbAdapter.TODO_COL_DUEDATE_NAME, taskCursor.getLong(MainDbAdapter.TASK_COL_STARTINGTIME_POS));
			else
				toDoContent.put(MainDbAdapter.TODO_COL_DUEDATE_NAME, (Long)null);
			
			if(taskScheduledFor.equals(StaticValues.TASK_SCHEDULED_FOR_MILEAGE) || taskScheduledFor.equals(StaticValues.TASK_SCHEDULED_FOR_BOTH))
				toDoContent.put(MainDbAdapter.TODO_COL_DUEMILAGE_NAME, taskCursor.getLong(MainDbAdapter.TASK_COL_RUNMILEAGE_POS));
			else
				toDoContent.put(MainDbAdapter.TODO_COL_DUEMILAGE_NAME, (Long)null);
				
			if(todoCursor.moveToNext()){ //the todo exist -> just update it
				if(!todoCursor.getString(MainDbAdapter.TODO_COL_ISDONE_POS).equals("Y")) //if the todo is done leave it as is
					mDb.updateRecord(MainDbAdapter.TODO_TABLE_NAME, todoCursor.getLong(MainDbAdapter.GEN_COL_ROWID_POS), toDoContent);
			}
			else{ //the todo does not exist -> create it
				mDb.createRecord(MainDbAdapter.TODO_TABLE_NAME, toDoContent);
			}
			todoCursor.close();
		}
		taskCursor.close();
		
	}

	private void maintainRecurentTasks(){
		Cursor taskCursor = null;
		Cursor todoCursor = null;
		Cursor taskCarCursor = null;
		String taskSelection = null;
		String taskCarSelection = null;
		String todoSelection = null;
		String taskScheduledFor = null;
		String[] taskSelectionArgs = null;
		String[] todoSelectionArgs = null;
		String[] taskCarSelectArgs = null;
		int todoCount = 1;
		boolean isDiffStartingTime = true;

		taskSelection = MainDbAdapter.isActiveCondition + 
				" AND " + MainDbAdapter.TASK_COL_ISRECURENT_NAME + "='Y' ";
		taskSelectionArgs = null;
		if(mTaskID > 0){
			taskSelection = taskSelection + " AND " + MainDbAdapter.GEN_COL_ROWID_NAME + " = ?";
			taskSelectionArgs = new String[1];
			taskSelectionArgs[0] = Long.toString(mTaskID);
		}

		taskCarSelection = 
			MainDbAdapter.isActiveCondition +
				" AND " + MainDbAdapter.TASK_CAR_COL_TASK_ID_NAME + " =? ";
		taskCursor = mDb.query(MainDbAdapter.TASK_TABLE_NAME, 
				MainDbAdapter.taskTableColNames, taskSelection, taskSelectionArgs, null, null, null);

		while(taskCursor.moveToNext()){
			
			todoSelection = MainDbAdapter.TODO_COL_TASK_ID_NAME + "=? AND " + MainDbAdapter.TODO_COL_ISDONE_NAME + "='N'";
			
			isDiffStartingTime = (taskCursor.getString(MainDbAdapter.TASK_COL_ISDIFFERENTSTARTINGTIME_POS) != null &&
					taskCursor.getString(MainDbAdapter.TASK_COL_ISDIFFERENTSTARTINGTIME_POS).equals("Y"));
			taskScheduledFor = taskCursor.getString(MainDbAdapter.TASK_COL_SCHEDULEDFOR_POS);
			if(mCarID > 0){
				taskCarSelection = taskCarSelection + " AND " +
						MainDbAdapter.TASK_CAR_COL_CAR_ID_NAME + " =? ";
				taskCarSelectArgs = new String[2];
				taskCarSelectArgs[0] = taskCursor.getString(MainDbAdapter.GEN_COL_ROWID_POS);
				taskCarSelectArgs[1] = Long.toString(mCarID);
			
			}
			else{
				taskCarSelectArgs = new String[1];
				taskCarSelectArgs[0] = taskCursor.getString(MainDbAdapter.GEN_COL_ROWID_POS);
			}
			
			if(isDiffStartingTime ||
					taskScheduledFor.equals(StaticValues.TASK_SCHEDULED_FOR_MILEAGE) ||
						taskScheduledFor.equals(StaticValues.TASK_SCHEDULED_FOR_BOTH)){ //cars are linked to task
				taskCarCursor = mDb.query(MainDbAdapter.TASK_CAR_TABLE_NAME, MainDbAdapter.taskCarTableColNames, 
						taskCarSelection, taskCarSelectArgs, null, null, null);
				todoSelection = todoSelection + " AND " + MainDbAdapter.TODO_COL_CAR_ID_NAME + "=?";
				todoSelectionArgs = new String[2];
				todoSelectionArgs[0] = taskCursor.getString(MainDbAdapter.GEN_COL_ROWID_POS);
				while(taskCarCursor.moveToNext()){
					todoSelectionArgs[1] = taskCarCursor.getString(MainDbAdapter.TASK_CAR_COL_CAR_ID_POS); 
					todoCursor = mDb.query(MainDbAdapter.TODO_TABLE_NAME, MainDbAdapter.todoTableColNames, todoSelection, todoSelectionArgs, null, null, null);
					todoCount = todoCursor.getCount();
					todoCursor.close();
					if(todoCount < mTodoCount){
						for( int i = todoCount; i < mTodoCount; i++){
							createNextRecurrentToDo(taskCursor, taskCarCursor);
						}
					}
				}
				taskCarCursor.close();
			}
			else{
				todoSelectionArgs = new String[1];
				todoSelectionArgs[0] = taskCursor.getString(MainDbAdapter.GEN_COL_ROWID_POS);
				todoCursor = mDb.query(MainDbAdapter.TODO_TABLE_NAME, MainDbAdapter.todoTableColNames, todoSelection, todoSelectionArgs, null, null, null);
				todoCount = todoCursor.getCount();
				todoCursor.close();
				if(todoCount < mTodoCount){
					for( int i = todoCount; i < mTodoCount; i++){
						createNextRecurrentToDo(taskCursor, null);
					}
				}
			}
		}
		taskCursor.close();
	}
	
	private void createNextRecurrentToDo(Cursor taskCursor, Cursor taskCarCursor){
		Calendar nextToDoCalendar = Calendar.getInstance();
		long nextToDoMileage = 0;
		long firstRunMileage = 0;
		long carCurrentIndex = 0;
		ContentValues nextToDoContent = new ContentValues();
		Cursor lastTodoCursor = null; //base time for calculating next run time
		String todoSelectCondition = null;
		String[] todoSelectArgs = null;
		String todoSelectOrderBy = null;
		long taskId = taskCursor.getLong(MainDbAdapter.GEN_COL_ROWID_POS);
		long carId = 0;
		if(taskCarCursor != null)
			carId = taskCarCursor.getLong(MainDbAdapter.TASK_CAR_COL_CAR_ID_POS);
		
		String taskScheduledFor = taskCursor.getString(MainDbAdapter.TASK_COL_SCHEDULEDFOR_POS);
		int frequencyType = taskCursor.getInt(MainDbAdapter.TASK_COL_TIMEFREQUENCYTYPE_POS);
		int timeFrequency = taskCursor.getInt(MainDbAdapter.TASK_COL_TIMEFREQUENCY_POS);
		int mileageFrequency = taskCursor.getInt(MainDbAdapter.TASK_COL_RUNMILEAGE_POS);

		nextToDoContent.put(MainDbAdapter.GEN_COL_NAME_NAME, taskCursor.getString(MainDbAdapter.GEN_COL_NAME_POS));
		nextToDoContent.put(MainDbAdapter.GEN_COL_USER_COMMENT_NAME, taskCursor.getString(MainDbAdapter.GEN_COL_USER_COMMENT_POS));
		nextToDoContent.put(MainDbAdapter.TODO_COL_TASK_ID_NAME, taskId);

		
		//select the last todo
		todoSelectCondition = MainDbAdapter.isActiveCondition + " AND " + MainDbAdapter.TODO_COL_TASK_ID_NAME + " = ? ";
		if(taskCarCursor == null){
			todoSelectArgs = new String[1];
			todoSelectArgs[0] = Long.toString(taskId);
		}
		else{
			todoSelectCondition = todoSelectCondition
					+ " AND " + MainDbAdapter.TODO_COL_CAR_ID_NAME + " = ? ";
			todoSelectArgs = new String[2];
			todoSelectArgs[0] = Long.toString(taskId);
			todoSelectArgs[1] = Long.toString(carId);
		}
		
		if(taskScheduledFor.equals(StaticValues.TASK_SCHEDULED_FOR_MILEAGE))
			todoSelectOrderBy = MainDbAdapter.TODO_COL_DUEMILAGE_NAME + " DESC";
		else
			todoSelectOrderBy = MainDbAdapter.TODO_COL_DUEDATE_NAME + " DESC";

		lastTodoCursor = mDb.query(MainDbAdapter.TODO_TABLE_NAME, MainDbAdapter.todoTableColNames, 
				todoSelectCondition, todoSelectArgs, null, null, todoSelectOrderBy);
		
		if(lastTodoCursor.moveToNext()){ //an existing todo exist => this is the reference for the next todo
			if(taskCarCursor != null)
				nextToDoContent.put(MainDbAdapter.TODO_COL_CAR_ID_NAME, taskCarCursor.getLong(MainDbAdapter.TASK_CAR_COL_CAR_ID_POS));
			else
				nextToDoContent.put(MainDbAdapter.TODO_COL_CAR_ID_NAME, (Long)null);

			if(taskScheduledFor.equals(StaticValues.TASK_SCHEDULED_FOR_TIME) ||
					taskScheduledFor.equals(StaticValues.TASK_SCHEDULED_FOR_BOTH)){
				
				nextToDoCalendar.setTimeInMillis(taskCursor.getLong(MainDbAdapter.TASK_COL_STARTINGTIME_POS) * 1000);
				boolean isLastDayOfMonth = (nextToDoCalendar.get(Calendar.YEAR) == 1970);
				
				//calculate the next todo date
				nextToDoCalendar.setTimeInMillis(lastTodoCursor.getLong(MainDbAdapter.TODO_COL_DUEDATE_POS) * 1000);
				if(frequencyType == StaticValues.TASK_SCHEDULED_FREQTYPE_DAY){
					nextToDoCalendar.add(Calendar.DAY_OF_YEAR, timeFrequency);
				}
				else if(frequencyType == StaticValues.TASK_SCHEDULED_FREQTYPE_WEEK){
					nextToDoCalendar.add(Calendar.WEEK_OF_YEAR, timeFrequency);
				}
				else if(frequencyType == StaticValues.TASK_SCHEDULED_FREQTYPE_MONTH){
					if(isLastDayOfMonth){ //last day of the month
						nextToDoCalendar.set(Calendar.DAY_OF_MONTH, 1);
						nextToDoCalendar.add(Calendar.MONTH, timeFrequency + 1); //go to the first day of the month
						nextToDoCalendar.add(Calendar.DAY_OF_YEAR, -1); //go back to the last day of the previous month
					}
					else
						nextToDoCalendar.add(Calendar.MONTH, timeFrequency);
				}
				else if(frequencyType == StaticValues.TASK_SCHEDULED_FREQTYPE_YEAR){
					nextToDoCalendar.add(Calendar.YEAR, timeFrequency);
				}
				nextToDoContent.put(MainDbAdapter.TODO_COL_DUEDATE_NAME, nextToDoCalendar.getTimeInMillis() / 1000);
			}
			if(taskScheduledFor.equals(StaticValues.TASK_SCHEDULED_FOR_MILEAGE) ||
					taskScheduledFor.equals(StaticValues.TASK_SCHEDULED_FOR_BOTH)){
				nextToDoMileage = lastTodoCursor.getLong(MainDbAdapter.TODO_COL_DUEMILAGE_POS) + mileageFrequency;
				nextToDoContent.put(MainDbAdapter.TODO_COL_DUEMILAGE_NAME, nextToDoMileage);
			}
			
			if(taskScheduledFor.equals(StaticValues.TASK_SCHEDULED_FOR_MILEAGE))
				nextToDoContent.put(MainDbAdapter.TODO_COL_DUEDATE_NAME, (Long)null);
			if(taskScheduledFor.equals(StaticValues.TASK_SCHEDULED_FOR_TIME))
				nextToDoContent.put(MainDbAdapter.TODO_COL_DUEMILAGE_NAME, (Long)null);
		}
		else{ //this is the first todo
			if(taskCarCursor != null)
				nextToDoContent.put(MainDbAdapter.TODO_COL_CAR_ID_NAME, taskCarCursor.getLong(MainDbAdapter.TASK_CAR_COL_CAR_ID_POS));
			else
				nextToDoContent.put(MainDbAdapter.TODO_COL_CAR_ID_NAME, (Long)null);
			
			if(taskScheduledFor.equals(StaticValues.TASK_SCHEDULED_FOR_MILEAGE)){
				if(taskCarCursor != null){
					firstRunMileage = taskCarCursor.getLong(MainDbAdapter.TASK_CAR_COL_FIRSTRUN_MILEAGE_POS) + mileageFrequency;
					carCurrentIndex = mDb.getCarCurrentIndex(carId).longValue();
					while(firstRunMileage <= carCurrentIndex)
						firstRunMileage = firstRunMileage + mileageFrequency;
					nextToDoContent.put(MainDbAdapter.TODO_COL_DUEMILAGE_NAME, firstRunMileage);

					nextToDoContent.put(MainDbAdapter.TODO_COL_DUEDATE_NAME, (Long)null);
				}
			}
			else if(taskScheduledFor.equals(StaticValues.TASK_SCHEDULED_FOR_BOTH)
					|| taskScheduledFor.equals(StaticValues.TASK_SCHEDULED_FOR_TIME)){
				
				int currentYear = nextToDoCalendar.get(Calendar.YEAR);
				
				if(taskCarCursor != null)
					nextToDoCalendar.setTimeInMillis(taskCarCursor.getLong(MainDbAdapter.TASK_CAR_COL_FIRSTRUN_DATE_POS) * 1000);
				else
					nextToDoCalendar.setTimeInMillis(taskCursor.getLong(MainDbAdapter.TASK_COL_STARTINGTIME_POS) * 1000);

				//for recurrent tasks
				
				nextToDoCalendar.setTimeInMillis(taskCursor.getLong(MainDbAdapter.TASK_COL_STARTINGTIME_POS) * 1000);
				boolean isLastDayOfMonth = (nextToDoCalendar.get(Calendar.YEAR) == 1970); 
				nextToDoCalendar.set(Calendar.YEAR, currentYear);
				
//				nextToDoCalendar.set(Calendar.HOUR, 0);
//				nextToDoCalendar.set(Calendar.MINUTE, 0);
//				nextToDoCalendar.set(Calendar.SECOND, 0);
				while(nextToDoCalendar.getTimeInMillis() < System.currentTimeMillis()){
					if(frequencyType == StaticValues.TASK_SCHEDULED_FREQTYPE_DAY){
						nextToDoCalendar.add(Calendar.DAY_OF_YEAR, timeFrequency);
					}
					else if(frequencyType == StaticValues.TASK_SCHEDULED_FREQTYPE_WEEK){
						nextToDoCalendar.add(Calendar.WEEK_OF_YEAR, timeFrequency);
					}
					else if(frequencyType == StaticValues.TASK_SCHEDULED_FREQTYPE_MONTH){
						if(isLastDayOfMonth){ //last day of the month
							nextToDoCalendar.set(Calendar.DAY_OF_MONTH, 1);
							nextToDoCalendar.add(Calendar.MONTH, timeFrequency + 1); //go to the first day of the month
							nextToDoCalendar.add(Calendar.DAY_OF_YEAR, -1); //go back to the last day of the previous month
						}
						else
							nextToDoCalendar.add(Calendar.MONTH, timeFrequency);
					}
					else if(frequencyType == StaticValues.TASK_SCHEDULED_FREQTYPE_YEAR){
						nextToDoCalendar.add(Calendar.YEAR, timeFrequency);
					}
				}
				
				nextToDoContent.put(MainDbAdapter.TODO_COL_DUEDATE_NAME, nextToDoCalendar.getTimeInMillis() / 1000);

				if(taskScheduledFor.equals(StaticValues.TASK_SCHEDULED_FOR_BOTH)){
					if(taskCarCursor != null){
						firstRunMileage = taskCarCursor.getLong(MainDbAdapter.TASK_CAR_COL_FIRSTRUN_MILEAGE_POS) + mileageFrequency;
						carCurrentIndex = mDb.getCarCurrentIndex(carId).longValue();
						while(firstRunMileage <= carCurrentIndex)
							firstRunMileage = firstRunMileage + mileageFrequency;
						nextToDoContent.put(MainDbAdapter.TODO_COL_DUEMILAGE_NAME, firstRunMileage);
					}
				}
				else
					nextToDoContent.put(MainDbAdapter.TODO_COL_DUEMILAGE_NAME, (Long)null); 
			}
			
		}
		mDb.createRecord(MainDbAdapter.TODO_TABLE_NAME, nextToDoContent);
		lastTodoCursor.close();
	}
}
