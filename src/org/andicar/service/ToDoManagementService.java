 /*
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

import org.andicar.persistence.MainDbAdapter;
import org.andicar.utils.AndiCarExceptionHandler;
import org.andicar.utils.StaticValues;

import android.content.Context;
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
 * <br>All <b>recurent</b> tasks have a number of todo entries for each linked car.
 * <br>A new todo is automatically created when an existing todo is done or deactivated. 
 *
 */
public class ToDoManagementService extends Service {

	private MainDbAdapter mDb =null;
	private Bundle mBundleExtras;
	private long mTaskID = 0;
	private long mCarID = 0;
	private boolean isSetJustNextRun = false;
//	private static int mTodoCount = 3;

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
		if(getSharedPreferences(StaticValues.GLOBAL_PREFERENCE_NAME, Context.MODE_MULTI_PROCESS).getBoolean("SendCrashReport", true))
			Thread.setDefaultUncaughtExceptionHandler(
	                    new AndiCarExceptionHandler(Thread.getDefaultUncaughtExceptionHandler(), this));
		
		mBundleExtras = intent.getExtras();
		if(mBundleExtras != null){
			mTaskID = mBundleExtras.getLong("TaskID");
			mCarID = mBundleExtras.getLong("CarID");
			isSetJustNextRun = mBundleExtras.getBoolean("setJustNextRun");
		}

		if(!isSetJustNextRun){
			mDb = new MainDbAdapter(this);
			createTaskTodos();
			mDb.close();
		}

		Intent i = new Intent(this, ToDoNotificationService.class);
		i.putExtra("setJustNextRun", isSetJustNextRun);
		this.startService(i);

		stopSelf();
	}
	
	private void createTaskTodos(){
		Cursor taskCursor = null;
		Cursor todoCursor = null;
		Cursor taskCarCursor = null;
		String taskSelection = null;
		String taskCarSelection = null;
		String todoSelection = null;
//		String taskScheduledFor = null;
		String[] taskSelectionArgs = null;
		String[] todoSelectionArgs = null;
		String[] taskCarSelectArgs = null;
		int todoCount = 1;
		boolean isRecurrentTask = false;
//		boolean isDiffStartingTime = true;

//		taskSelection = MainDbAdapter.isActiveCondition + 
//				" AND " + MainDbAdapter.TASK_COL_TIMEFREQUENCYTYPE_NAME + "!='" + StaticValues.TASK_TIMEFREQUENCYTYPE_ONETIME + "'";
		taskSelection = MainDbAdapter.WHERE_CONDITION_ISACTIVE;
		taskSelectionArgs = null;
		if(mTaskID > 0){
			taskSelection = taskSelection + " AND " + MainDbAdapter.COL_NAME_GEN_ROWID + " = ?";
			taskSelectionArgs = new String[1];
			taskSelectionArgs[0] = Long.toString(mTaskID);
		}

		taskCarSelection = 
			MainDbAdapter.WHERE_CONDITION_ISACTIVE +
				" AND " + MainDbAdapter.COL_NAME_TASK_CAR__TASK_ID + " =? ";
		taskCursor = mDb.query(MainDbAdapter.TABLE_NAME_TASK, 
				MainDbAdapter.COL_LIST_TASK_TABLE, taskSelection, taskSelectionArgs, null, null, null);

		int taskToDoCount = 3;
		while(taskCursor.moveToNext()){
			isRecurrentTask = taskCursor.getString(MainDbAdapter.COL_POS_TASK__ISRECURRENT).equals("Y");
			todoSelection = MainDbAdapter.COL_NAME_TODO__TASK_ID + "=? AND " + MainDbAdapter.COL_NAME_TODO__ISDONE + "='N'";
			taskToDoCount = taskCursor.getInt(MainDbAdapter.COL_POS_TASK__TODOCOUNT);
			
			if(mCarID > 0){
				taskCarSelection = taskCarSelection + " AND " +
						MainDbAdapter.COL_NAME_TASK_CAR__CAR_ID + " =? ";
				taskCarSelectArgs = new String[2];
				taskCarSelectArgs[0] = taskCursor.getString(MainDbAdapter.COL_POS_GEN_ROWID);
				taskCarSelectArgs[1] = Long.toString(mCarID);
			
			}
			else{
				taskCarSelectArgs = new String[1];
				taskCarSelectArgs[0] = taskCursor.getString(MainDbAdapter.COL_POS_GEN_ROWID);
			}
			
			taskCarCursor = mDb.query(MainDbAdapter.TABLE_NAME_TASK_CAR, MainDbAdapter.COL_LIST_TASK_CAR_TABLE, 
					taskCarSelection, taskCarSelectArgs, null, null, null);
			
			if(taskCarCursor.getCount() > 0){//cars are linked to task
				todoSelection = todoSelection + " AND " + MainDbAdapter.COL_NAME_TODO__CAR_ID + "=?";
				todoSelectionArgs = new String[2];
				todoSelectionArgs[0] = taskCursor.getString(MainDbAdapter.COL_POS_GEN_ROWID);
				while(taskCarCursor.moveToNext()){
					if(isRecurrentTask){
						todoSelectionArgs[1] = taskCarCursor.getString(MainDbAdapter.COL_POS_TASK_CAR__CAR_ID); 
						todoCursor = mDb.query(MainDbAdapter.TABLE_NAME_TODO, MainDbAdapter.COL_LIST_TODO_TABLE, todoSelection, todoSelectionArgs, null, null, null);
						todoCount = todoCursor.getCount();
						todoCursor.close();
						if(todoCount < taskToDoCount){
							for( int i = todoCount; i < taskToDoCount; i++){
								createToDo(taskCursor, taskCarCursor);
							}
						}
					}
					else
						createToDo(taskCursor, taskCarCursor);
				}
			}
			else{ //no cars are linked to task
				if(isRecurrentTask){
					todoSelectionArgs = new String[1];
					todoSelectionArgs[0] = taskCursor.getString(MainDbAdapter.COL_POS_GEN_ROWID);
					todoCursor = mDb.query(MainDbAdapter.TABLE_NAME_TODO, MainDbAdapter.COL_LIST_TODO_TABLE, todoSelection, todoSelectionArgs, null, null, null);
					todoCount = todoCursor.getCount();
					todoCursor.close();
					if(todoCount < taskToDoCount){
						for( int i = todoCount; i < taskToDoCount; i++){
							createToDo(taskCursor, null);
						}
					}
				}
				else
					createToDo(taskCursor, null);
			}
			taskCarCursor.close();
		}
		taskCursor.close();
	}
	
	private void createToDo(Cursor taskCursor, Cursor taskCarCursor){
		Calendar nextToDoCalendar = Calendar.getInstance();
		long nextToDoMileage = 0;
		long firstRunMileage = 0;
		long carCurrentIndex = 0;
		ContentValues nextToDoContent = new ContentValues();
		Cursor lastTodoCursor = null; //base time for calculating next run time
		String todoSelectCondition = null;
		String[] todoSelectArgs = null;
		String todoSelectOrderBy = null;
		long taskId = taskCursor.getLong(MainDbAdapter.COL_POS_GEN_ROWID);
		long carId = 0;
		boolean isRecurrentTask = taskCursor.getString(MainDbAdapter.COL_POS_TASK__ISRECURRENT).equals("Y");
		boolean isDiffStartingTime = false; 
		if( taskCursor.getString(MainDbAdapter.COL_POS_TASK__ISDIFFERENTSTARTINGTIME) == null ||
				taskCursor.getString(MainDbAdapter.COL_POS_TASK__ISDIFFERENTSTARTINGTIME).equals("N")){
			isDiffStartingTime = false;
		}
		else
			isDiffStartingTime = true;

		if(taskCarCursor != null)
			carId = taskCarCursor.getLong(MainDbAdapter.COL_POS_TASK_CAR__CAR_ID);
		
		String taskScheduledFor = taskCursor.getString(MainDbAdapter.COL_POS_TASK__SCHEDULEDFOR);
		int frequencyType = taskCursor.getInt(MainDbAdapter.COL_POS_TASK__TIMEFREQUENCYTYPE);
		int timeFrequency = taskCursor.getInt(MainDbAdapter.COL_POS_TASK__TIMEFREQUENCY);
		int mileageFrequency = taskCursor.getInt(MainDbAdapter.COL_POS_TASK__RUNMILEAGE);

		nextToDoContent.put(MainDbAdapter.COL_NAME_GEN_NAME, taskCursor.getString(MainDbAdapter.COL_POS_GEN_NAME));
		nextToDoContent.put(MainDbAdapter.COL_NAME_GEN_USER_COMMENT, taskCursor.getString(MainDbAdapter.COL_POS_GEN_USER_COMMENT));
		nextToDoContent.put(MainDbAdapter.COL_NAME_TODO__TASK_ID, taskId);

		
		//select the last todo
		todoSelectCondition = MainDbAdapter.WHERE_CONDITION_ISACTIVE + " AND " + MainDbAdapter.COL_NAME_TODO__ISDONE + " = 'N' " +
				" AND " + MainDbAdapter.COL_NAME_TODO__TASK_ID + " = ? ";
				
		if(taskCarCursor == null){
			todoSelectArgs = new String[1];
			todoSelectArgs[0] = Long.toString(taskId);
		}
		else{
			todoSelectCondition = todoSelectCondition
					+ " AND " + MainDbAdapter.COL_NAME_TODO__CAR_ID + " = ? ";
			todoSelectArgs = new String[2];
			todoSelectArgs[0] = Long.toString(taskId);
			todoSelectArgs[1] = Long.toString(carId);
		}
		
		if(taskScheduledFor.equals(StaticValues.TASK_SCHEDULED_FOR_MILEAGE))
			todoSelectOrderBy = MainDbAdapter.COL_NAME_TODO__DUEMILEAGE + " DESC";
		else
			todoSelectOrderBy = MainDbAdapter.COL_NAME_TODO__DUEDATE + " DESC";

		lastTodoCursor = mDb.query(MainDbAdapter.TABLE_NAME_TODO, MainDbAdapter.COL_LIST_TODO_TABLE, 
				todoSelectCondition, todoSelectArgs, null, null, todoSelectOrderBy);
		
		if(lastTodoCursor.moveToNext()){ //an existing todo exist => this is the reference for the next todo
			if(taskCarCursor != null)
				nextToDoContent.put(MainDbAdapter.COL_NAME_TODO__CAR_ID, taskCarCursor.getLong(MainDbAdapter.COL_POS_TASK_CAR__CAR_ID));
			else
				nextToDoContent.put(MainDbAdapter.COL_NAME_TODO__CAR_ID, (Long)null);

			if(taskScheduledFor.equals(StaticValues.TASK_SCHEDULED_FOR_TIME) ||
					taskScheduledFor.equals(StaticValues.TASK_SCHEDULED_FOR_BOTH)){
				
				if(taskCarCursor != null && isRecurrentTask && isDiffStartingTime)
					nextToDoCalendar.setTimeInMillis(taskCarCursor.getLong(MainDbAdapter.COL_POS_TASK_CAR__FIRSTRUN_DATE) * 1000);
				else{
					if(taskCursor.getString(MainDbAdapter.COL_POS_TASK__STARTINGTIME) != null)
						nextToDoCalendar.setTimeInMillis(taskCursor.getLong(MainDbAdapter.COL_POS_TASK__STARTINGTIME) * 1000);
					else
						nextToDoCalendar.setTimeInMillis(System.currentTimeMillis());
				}
				boolean isLastDayOfMonth = (nextToDoCalendar.get(Calendar.YEAR) == 1970);
				
				//calculate the next todo date
				nextToDoCalendar.setTimeInMillis(lastTodoCursor.getLong(MainDbAdapter.COL_POS_TODO__DUEDATE) * 1000);

				//set the calendar for next todo based on task definition
				setNextToDoCalendar(nextToDoCalendar, frequencyType, timeFrequency, isLastDayOfMonth);
				nextToDoContent.put(MainDbAdapter.COL_NAME_TODO__DUEDATE, nextToDoCalendar.getTimeInMillis() / 1000);
				//set the alarm date
				if(taskCursor.getInt(MainDbAdapter.COL_POS_TASK__TIMEFREQUENCYTYPE) == StaticValues.TASK_TIMEFREQUENCYTYPE_DAILY){
					nextToDoCalendar.add(Calendar.MINUTE, -1 * taskCursor.getInt(MainDbAdapter.COL_POS_TASK__TIMEREMINDERSTART));
				}
				else{
					nextToDoCalendar.add(Calendar.DAY_OF_YEAR, -1 * taskCursor.getInt(MainDbAdapter.COL_POS_TASK__TIMEREMINDERSTART));
				}
				if(nextToDoCalendar.getTimeInMillis() < System.currentTimeMillis())
					nextToDoContent.put(MainDbAdapter.COL_NAME_TODO__NOTIFICATIONDATE, (System.currentTimeMillis() / 1000) + 60);
				else
					nextToDoContent.put(MainDbAdapter.COL_NAME_TODO__NOTIFICATIONDATE, nextToDoCalendar.getTimeInMillis() / 1000);
			}
			if(taskScheduledFor.equals(StaticValues.TASK_SCHEDULED_FOR_MILEAGE) ||
					taskScheduledFor.equals(StaticValues.TASK_SCHEDULED_FOR_BOTH)){
				nextToDoMileage = lastTodoCursor.getLong(MainDbAdapter.COL_POS_TODO__DUEMILAGE) + mileageFrequency;
				nextToDoContent.put(MainDbAdapter.COL_NAME_TODO__DUEMILEAGE, nextToDoMileage);
				nextToDoContent.put(MainDbAdapter.COL_NAME_TODO__NOTIFICATIONMILEAGE, 
						nextToDoMileage - taskCursor.getLong(MainDbAdapter.COL_POS_TASK__MILEAGEREMINDERSTART));
			}
			
			if(taskScheduledFor.equals(StaticValues.TASK_SCHEDULED_FOR_MILEAGE)){
				nextToDoContent.put(MainDbAdapter.COL_NAME_TODO__DUEDATE, (Long)null);
				nextToDoContent.put(MainDbAdapter.COL_NAME_TODO__NOTIFICATIONDATE, (Long)null);
			}
			if(taskScheduledFor.equals(StaticValues.TASK_SCHEDULED_FOR_TIME)){
				nextToDoContent.put(MainDbAdapter.COL_NAME_TODO__DUEMILEAGE, (Long)null);
				nextToDoContent.put(MainDbAdapter.COL_NAME_TODO__NOTIFICATIONMILEAGE, (Long)null); 
			}
		}
		else{ //this is the first todo
			if(taskCarCursor != null)
				nextToDoContent.put(MainDbAdapter.COL_NAME_TODO__CAR_ID, taskCarCursor.getLong(MainDbAdapter.COL_POS_TASK_CAR__CAR_ID));
			else
				nextToDoContent.put(MainDbAdapter.COL_NAME_TODO__CAR_ID, (Long)null);
			
			if(taskScheduledFor.equals(StaticValues.TASK_SCHEDULED_FOR_MILEAGE)){
				if(taskCarCursor != null){
					if(isRecurrentTask){
						firstRunMileage = taskCarCursor.getLong(MainDbAdapter.COL_POS_TASK_CAR__FIRSTRUN_MILEAGE);
						carCurrentIndex = mDb.getCarCurrentIndex(carId).longValue();
						while(firstRunMileage < carCurrentIndex)
							firstRunMileage = firstRunMileage + mileageFrequency;

					}
					else
						firstRunMileage = taskCursor.getLong(MainDbAdapter.COL_POS_TASK__RUNMILEAGE);

					nextToDoContent.put(MainDbAdapter.COL_NAME_TODO__DUEMILEAGE, firstRunMileage);
					nextToDoContent.put(MainDbAdapter.COL_NAME_TODO__NOTIFICATIONMILEAGE, 
							firstRunMileage - taskCursor.getLong(MainDbAdapter.COL_POS_TASK__MILEAGEREMINDERSTART));
					nextToDoContent.put(MainDbAdapter.COL_NAME_TODO__DUEDATE, (Long)null);
					nextToDoContent.put(MainDbAdapter.COL_NAME_TODO__NOTIFICATIONDATE, (Long)null);
				}
			}
			else if(taskScheduledFor.equals(StaticValues.TASK_SCHEDULED_FOR_BOTH)
					|| taskScheduledFor.equals(StaticValues.TASK_SCHEDULED_FOR_TIME)){
				
				//at this point the nextToDoCalendar is initialized with the current time.
				int currentYear = nextToDoCalendar.get(Calendar.YEAR);
				int currentMonth = nextToDoCalendar.get(Calendar.MONTH);
				int currentDay = nextToDoCalendar.get(Calendar.DAY_OF_MONTH);
				nextToDoCalendar.add(Calendar.DAY_OF_YEAR, 1);
				boolean isLastDayOfCurrentMonth = (currentMonth != nextToDoCalendar.get(Calendar.MONTH));
				nextToDoCalendar.add(Calendar.DAY_OF_YEAR, -1);
					
				
				if(taskCarCursor != null && isRecurrentTask && isDiffStartingTime)
					nextToDoCalendar.setTimeInMillis(taskCarCursor.getLong(MainDbAdapter.COL_POS_TASK_CAR__FIRSTRUN_DATE) * 1000);
				else{
					if(taskCursor.getString(MainDbAdapter.COL_POS_TASK__STARTINGTIME) != null)
						nextToDoCalendar.setTimeInMillis(taskCursor.getLong(MainDbAdapter.COL_POS_TASK__STARTINGTIME) * 1000);
					else
						nextToDoCalendar.setTimeInMillis(System.currentTimeMillis());
				}

				boolean isLastDayOfMonth = (nextToDoCalendar.get(Calendar.YEAR) == 1970); 
				if(isLastDayOfMonth)
					nextToDoCalendar.set(Calendar.YEAR, currentYear);

				if(isLastDayOfMonth){
					if(isLastDayOfCurrentMonth)
						//wee need to compare the hour:minute of the task with the current hour:minute
						nextToDoCalendar.set(Calendar.DAY_OF_MONTH, currentDay);
					else
						//wee not need to compare the hour:minute of the task with the current hour:minute
						nextToDoCalendar.set(Calendar.DAY_OF_MONTH, currentDay + 1);
					
					if(nextToDoCalendar.getTimeInMillis() > System.currentTimeMillis()){
						//set the first run at the end of the month specified in the task definition
						nextToDoCalendar.set(Calendar.DAY_OF_MONTH, 1);
						nextToDoCalendar.add(Calendar.MONTH, 1); //go to the first day of the next month
						nextToDoCalendar.add(Calendar.DAY_OF_YEAR, -1); //go back to the last day of the previous month
					}
				}
				while(nextToDoCalendar.getTimeInMillis() < System.currentTimeMillis()){
					@SuppressWarnings("unused")
					String test = taskCursor.getString(MainDbAdapter.COL_POS_GEN_NAME);
					setNextToDoCalendar(nextToDoCalendar, frequencyType, timeFrequency, isLastDayOfMonth);
				}
				
				nextToDoContent.put(MainDbAdapter.COL_NAME_TODO__DUEDATE, nextToDoCalendar.getTimeInMillis() / 1000);
				//set the alarm date
				if(taskCursor.getInt(MainDbAdapter.COL_POS_TASK__TIMEFREQUENCYTYPE) == StaticValues.TASK_TIMEFREQUENCYTYPE_DAILY){
					nextToDoCalendar.add(Calendar.MINUTE, -1 * taskCursor.getInt(MainDbAdapter.COL_POS_TASK__TIMEREMINDERSTART));
				}
				else{
					nextToDoCalendar.add(Calendar.DAY_OF_YEAR, -1 * taskCursor.getInt(MainDbAdapter.COL_POS_TASK__TIMEREMINDERSTART));
				}
				if(nextToDoCalendar.getTimeInMillis() < System.currentTimeMillis())
					nextToDoContent.put(MainDbAdapter.COL_NAME_TODO__NOTIFICATIONDATE, (System.currentTimeMillis() / 1000) + 60);
				else
					nextToDoContent.put(MainDbAdapter.COL_NAME_TODO__NOTIFICATIONDATE, nextToDoCalendar.getTimeInMillis() / 1000);

				if(taskScheduledFor.equals(StaticValues.TASK_SCHEDULED_FOR_BOTH)){
					if(taskCarCursor != null){
						if(isRecurrentTask){
							firstRunMileage = taskCarCursor.getLong(MainDbAdapter.COL_POS_TASK_CAR__FIRSTRUN_MILEAGE);
							carCurrentIndex = mDb.getCarCurrentIndex(carId).longValue();
							while(firstRunMileage <= carCurrentIndex)
								firstRunMileage = firstRunMileage + mileageFrequency;
						}
						else
							firstRunMileage = taskCursor.getLong(MainDbAdapter.COL_POS_TASK__RUNMILEAGE);
						nextToDoContent.put(MainDbAdapter.COL_NAME_TODO__DUEMILEAGE, firstRunMileage);
						nextToDoContent.put(MainDbAdapter.COL_NAME_TODO__NOTIFICATIONMILEAGE, 
								firstRunMileage - taskCursor.getLong(MainDbAdapter.COL_POS_TASK__MILEAGEREMINDERSTART));
					}
				}
				else{
					nextToDoContent.put(MainDbAdapter.COL_NAME_TODO__DUEMILEAGE, (Long)null);
					nextToDoContent.put(MainDbAdapter.COL_NAME_TODO__NOTIFICATIONMILEAGE, (Long)null);
				}
			}
			
		}
		mDb.createRecord(MainDbAdapter.TABLE_NAME_TODO, nextToDoContent);
		lastTodoCursor.close();
	}

	private void setNextToDoCalendar(Calendar nextToDoCalendar, int frequencyType, int timeFrequency, boolean isLastDayOfMonth) {
		if(frequencyType == StaticValues.TASK_TIMEFREQUENCYTYPE_DAILY){
			nextToDoCalendar.add(Calendar.DAY_OF_YEAR, timeFrequency);
		}
		else if(frequencyType == StaticValues.TASK_TIMEFREQUENCYTYPE_WEEKLY){
			nextToDoCalendar.add(Calendar.WEEK_OF_YEAR, timeFrequency);
		}
		else if(frequencyType == StaticValues.TASK_TIMEFREQUENCYTYPE_MONTHLY){
			if(isLastDayOfMonth){ //last day of the month
				nextToDoCalendar.set(Calendar.DAY_OF_MONTH, 1);
				nextToDoCalendar.add(Calendar.MONTH, timeFrequency + 1); //go to the first day of the next month
				nextToDoCalendar.add(Calendar.DAY_OF_YEAR, -1); //go back to the last day of the previous month
			}
			else
				nextToDoCalendar.add(Calendar.MONTH, timeFrequency);
		}
		else if(frequencyType == StaticValues.TASK_TIMEFREQUENCYTYPE_YEARLY){
			nextToDoCalendar.add(Calendar.YEAR, timeFrequency);
		}
	}
}
