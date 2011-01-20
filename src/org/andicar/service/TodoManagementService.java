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

import org.andicar.persistence.DB;
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
		}
		
		mDb = new MainDbAdapter(this);
		
		//#1 check todo's for none recurent tasks
		maintainNonRecurentTasks();
		maintainRecurentTasks();
		
		mDb.close();
		stopSelf();
	}
	
	private void maintainNonRecurentTasks(){
		Cursor taskCursor = null;
		Cursor todoCursor = null;
		String taskSelection = null;
		String todoSelection = null;
		String selectSql = null;
		String[] taskSelectionArgs = null;
		String[] todoSelectionArgs = null;
		ContentValues toDoContent = new ContentValues();
		long taskId = -1;
		long carId = -1;

		//check tasks with only time scedule
		taskSelection = MainDbAdapter.isActiveCondition + 
				" AND " + MainDbAdapter.TASK_COL_ISRECURENT_NAME + "='N' " +
				" AND " + MainDbAdapter.TASK_COL_SCHEDULEDFOR_NAME + "='T' " +
				" AND " + MainDbAdapter.TASK_COL_RUNTIME_NAME + " > ?";
		
		if(mTaskID > 0){
			taskSelection = taskSelection + " AND " + MainDbAdapter.GEN_COL_ROWID_NAME + " = ?";
			taskSelectionArgs = new String[2];
			taskSelectionArgs[0] = Long.toString(System.currentTimeMillis() / 1000); //miliseconds to seconds
			taskSelectionArgs[1] = Long.toString(mTaskID);
		}
		else{
			taskSelectionArgs = new String[1];
			taskSelectionArgs[0] = Long.toString(System.currentTimeMillis() / 1000); //miliseconds to seconds
		}
		
		taskCursor = mDb.query(MainDbAdapter.TASK_TABLE_NAME, MainDbAdapter.taskTableColNames, taskSelection, taskSelectionArgs, null, null, null);
		
		todoSelection = MainDbAdapter.TODO_COL_TASK_ID_NAME + "=?";
		todoSelectionArgs = new String[1];
		while(taskCursor.moveToNext()){
			taskId = taskCursor.getLong(MainDbAdapter.GEN_COL_ROWID_POS);
			todoSelectionArgs[0] = Long.toString(taskId);
			todoCursor = mDb.query(MainDbAdapter.TODO_TABLE_NAME, MainDbAdapter.todoTableColNames, todoSelection, todoSelectionArgs, null, null, null);
			toDoContent.clear();
			toDoContent.put(MainDbAdapter.GEN_COL_NAME_NAME, taskCursor.getString(MainDbAdapter.GEN_COL_NAME_POS));
			toDoContent.put(MainDbAdapter.GEN_COL_USER_COMMENT_NAME, taskCursor.getString(MainDbAdapter.GEN_COL_USER_COMMENT_POS));
			toDoContent.put(MainDbAdapter.TODO_COL_TASK_ID_NAME, taskId);
			toDoContent.put(MainDbAdapter.TODO_COL_DUEDATE_NAME, taskCursor.getLong(MainDbAdapter.TASK_COL_RUNTIME_POS));
			if(todoCursor.moveToNext()){ //the todo exist -> just update it
				if(!todoCursor.getString(MainDbAdapter.TODO_COL_ISDONE_POS).equals("Y")) //if the todo is done leave it untached
					mDb.updateRecord(MainDbAdapter.TODO_TABLE_NAME, todoCursor.getLong(MainDbAdapter.GEN_COL_ROWID_POS), toDoContent);
			}
			else{ //the todo does not exist -> create it
				mDb.createRecord(MainDbAdapter.TODO_TABLE_NAME, toDoContent);
			}
			todoCursor.close();
		}
		taskCursor.close();
		
		//check tasks with time & mileage scedule
		selectSql = "SELECT " + MainDbAdapter.TASK_TABLE_NAME + ".*, " + 
								DB.sqlConcatTableColumn(MainDbAdapter.TASK_CAR_TABLE_NAME, MainDbAdapter.TASK_CAR_COL_CAR_ID_NAME) + " AS TaskCar_CarId " 
						+ " FROM " + MainDbAdapter.TASK_TABLE_NAME
							+ " JOIN " + MainDbAdapter.TASK_CAR_TABLE_NAME 
									+ " ON " + DB.sqlConcatTableColumn(MainDbAdapter.TASK_TABLE_NAME, MainDbAdapter.GEN_COL_ROWID_NAME)
											+ " = " + DB.sqlConcatTableColumn(MainDbAdapter.TASK_CAR_TABLE_NAME, MainDbAdapter.TASK_CAR_COL_TASK_ID_NAME)
						+ " WHERE " + DB.sqlConcatTableColumn(MainDbAdapter.TASK_TABLE_NAME, MainDbAdapter.GEN_COL_ISACTIVE_NAME) + " = 'Y' "
							+ " AND " + DB.sqlConcatTableColumn(MainDbAdapter.TASK_TABLE_NAME, MainDbAdapter.TASK_COL_ISRECURENT_NAME) + "='N' "
							+ " AND " + DB.sqlConcatTableColumn(MainDbAdapter.TASK_TABLE_NAME, MainDbAdapter.TASK_COL_SCHEDULEDFOR_NAME) + "!='T' "
							;
		taskSelectionArgs = null;
		if(mTaskID > 0){
			selectSql = selectSql + " AND " + DB.sqlConcatTableColumn(MainDbAdapter.TASK_TABLE_NAME, MainDbAdapter.GEN_COL_ROWID_NAME) + " = ?";
			taskSelectionArgs = new String[1];
			taskSelectionArgs[0] = Long.toString(mTaskID);
		}

		taskCursor = mDb.query(selectSql, taskSelectionArgs);

		todoSelection = MainDbAdapter.TODO_COL_TASK_ID_NAME + "=? AND " + MainDbAdapter.TODO_COL_CAR_ID_NAME + "=?";
		todoSelectionArgs = null;
		todoSelectionArgs = new String[2];
		while(taskCursor.moveToNext()){
			taskId = taskCursor.getLong(MainDbAdapter.GEN_COL_ROWID_POS);
			carId = taskCursor.getLong(taskCursor.getColumnIndex("TaskCar_CarId"));
			todoSelectionArgs[0] = Long.toString(taskId);
			todoSelectionArgs[1] = Long.toString(carId);

			todoCursor = mDb.query(MainDbAdapter.TODO_TABLE_NAME, MainDbAdapter.todoTableColNames, 
					todoSelection, todoSelectionArgs, null, null, null);
			toDoContent.clear();
			toDoContent.put(MainDbAdapter.GEN_COL_NAME_NAME, taskCursor.getString(MainDbAdapter.GEN_COL_NAME_POS));
			toDoContent.put(MainDbAdapter.GEN_COL_USER_COMMENT_NAME, taskCursor.getString(MainDbAdapter.GEN_COL_USER_COMMENT_POS));
			toDoContent.put(MainDbAdapter.TODO_COL_TASK_ID_NAME, taskId);
			toDoContent.put(MainDbAdapter.TODO_COL_CAR_ID_NAME, carId);
			if(taskCursor.getString(MainDbAdapter.TASK_COL_SCHEDULEDFOR_POS).equals("M")){ //just mileage
				toDoContent.put(MainDbAdapter.TODO_COL_DUEMILAGE_NAME, taskCursor.getLong(MainDbAdapter.TASK_COL_RUNMILEAGE_POS));
				toDoContent.put(MainDbAdapter.TODO_COL_DUEDATE_NAME, (Long)null);
			}
			else{ //both
				toDoContent.put(MainDbAdapter.TODO_COL_DUEMILAGE_NAME, taskCursor.getLong(MainDbAdapter.TASK_COL_RUNMILEAGE_POS));
				toDoContent.put(MainDbAdapter.TODO_COL_DUEDATE_NAME, taskCursor.getLong(MainDbAdapter.TASK_COL_RUNTIME_POS));
			}
			if(todoCursor.moveToNext()){ //the todo exist -> just update it
				if(!todoCursor.getString(MainDbAdapter.TODO_COL_ISDONE_POS).equals("Y")) //if the todo is done leave it untached
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
		String taskSelection = null;
		String todoSelection = null;
		String selectSql = null;
		String[] taskSelectionArgs = null;
		String[] todoSelectionArgs = null;
		ContentValues toDoContent = new ContentValues();
		long taskId = -1;
		int todoCount = 1;
		boolean isDifferentStartingTime = true;

		//check tasks with only time scedule
		taskSelection = MainDbAdapter.isActiveCondition + 
				" AND " + MainDbAdapter.TASK_COL_ISRECURENT_NAME + "='Y' " +
				" AND " + MainDbAdapter.TASK_COL_SCHEDULEDFOR_NAME + "='T' ";
		
		taskSelectionArgs = null;
		if(mTaskID > 0){
			taskSelection = taskSelection + " AND " + MainDbAdapter.GEN_COL_ROWID_NAME + " = ?";
			taskSelectionArgs = new String[1];
			taskSelectionArgs[0] = Long.toString(mTaskID);
		}
		
		taskCursor = mDb.query(MainDbAdapter.TASK_TABLE_NAME, MainDbAdapter.taskTableColNames, taskSelection, taskSelectionArgs, null, null, null);

		todoSelection = MainDbAdapter.TODO_COL_TASK_ID_NAME + "=? AND " + MainDbAdapter.TODO_COL_ISDONE_NAME + "='N'";
		todoSelectionArgs = new String[1];
		while(taskCursor.moveToNext()){
			taskId = taskCursor.getLong(MainDbAdapter.GEN_COL_ROWID_POS);
			isDifferentStartingTime = taskCursor.getString(MainDbAdapter.TASK_COL_ISDIFFERENTSTARTINGTIME_POS).equals("Y");
			
			todoSelectionArgs[0] = Long.toString(taskId);
			todoCursor = mDb.query(MainDbAdapter.TODO_TABLE_NAME, MainDbAdapter.todoTableColNames, todoSelection, todoSelectionArgs, null, null, null);
			todoCount = todoCursor.getCount();
			todoCursor.close();
			toDoContent.clear();
			toDoContent.put(MainDbAdapter.GEN_COL_NAME_NAME, taskCursor.getString(MainDbAdapter.GEN_COL_NAME_POS));
			toDoContent.put(MainDbAdapter.GEN_COL_USER_COMMENT_NAME, taskCursor.getString(MainDbAdapter.GEN_COL_USER_COMMENT_POS));
			toDoContent.put(MainDbAdapter.TODO_COL_TASK_ID_NAME, taskId);
			if(todoCount < mTodoCount){
				for( int i = todoCount; i < mTodoCount; i++){
					toDoContent.put(MainDbAdapter.TODO_COL_DUEDATE_NAME, calculateNextToDoTime(taskId, taskCursor));
					mDb.createRecord(MainDbAdapter.TODO_TABLE_NAME, toDoContent);
				}
			}
			
		}
	}
	
	private long calculateNextToDoTime(long taskId, Cursor currentTask){
//		Calendar refCalendar = Calendar.getInstance();
		Calendar nextToDoCalendar = Calendar.getInstance();
		Cursor lastTodoCursor = null; //base time for calculating next run time
		String todoSelectCondition = "";
		String[] todoSelectArgs = null;
		String todoSelectOrderBy = "";
		int frequencyType = currentTask.getInt(MainDbAdapter.TASK_COL_TIMEFREQUENCYTYPE_POS);
		int timeFrequency = currentTask.getInt(MainDbAdapter.TASK_COL_TIMEFREQUENCY_POS);
		int runDay = currentTask.getInt(MainDbAdapter.TASK_COL_RUNDAY_POS);
		int runMonth = currentTask.getInt(MainDbAdapter.TASK_COL_RUNMONTH_POS);
		long currentTaskRunHour = currentTask.getLong(MainDbAdapter.TASK_COL_RUNTIME_POS) * 1000;
		
		
		//select the last todo
		todoSelectCondition = MainDbAdapter.isActiveCondition
				+ " AND " + MainDbAdapter.TODO_COL_TASK_ID_NAME + " = ? ";
		todoSelectArgs = new String[1];
		todoSelectArgs[0] = Long.toString(taskId);
		todoSelectOrderBy = MainDbAdapter.TODO_COL_DUEDATE_NAME + " DESC";
		lastTodoCursor = mDb.query(MainDbAdapter.TODO_TABLE_NAME, MainDbAdapter.todoTableColNames, todoSelectCondition, todoSelectArgs, null, null, todoSelectOrderBy);
		
		if(lastTodoCursor.moveToNext()){ //just add the recurrence period to the last todo
			nextToDoCalendar.setTimeInMillis(lastTodoCursor.getLong(MainDbAdapter.TODO_COL_DUEDATE_POS) * 1000);
			if(frequencyType == StaticValues.TASK_SCHEDULED_FREQTYPE_WEEK){
				nextToDoCalendar.add(Calendar.WEEK_OF_YEAR, timeFrequency);
			}
			else if(frequencyType == StaticValues.TASK_SCHEDULED_FREQTYPE_MONTH){
				if(runDay == 32){ //last day of the month
					nextToDoCalendar.set(Calendar.DAY_OF_MONTH, 1);
					nextToDoCalendar.add(Calendar.MONTH, timeFrequency + 1);
					nextToDoCalendar.add(Calendar.DAY_OF_YEAR, -1); //go back to the last day of the previous month
				}
				else
					nextToDoCalendar.add(Calendar.MONTH, timeFrequency);
			}
			else if(frequencyType == StaticValues.TASK_SCHEDULED_FREQTYPE_YEAR){
				nextToDoCalendar.add(Calendar.YEAR, timeFrequency);
			}
			lastTodoCursor.close();
		}
		else{ //this is the first todo
			lastTodoCursor.close();
			nextToDoCalendar = Calendar.getInstance();
			nextToDoCalendar.set(Calendar.HOUR_OF_DAY, 0);
			nextToDoCalendar.set(Calendar.MINUTE, 0);
			nextToDoCalendar.set(Calendar.SECOND, 1);
			nextToDoCalendar.set(Calendar.MILLISECOND, 0);
			if(frequencyType == StaticValues.TASK_SCHEDULED_FREQTYPE_WEEK){
				if(nextToDoCalendar.get(Calendar.DAY_OF_WEEK) > runDay)
					nextToDoCalendar.add(Calendar.WEEK_OF_YEAR, 1);
				else if(nextToDoCalendar.get(Calendar.DAY_OF_WEEK) == runDay){ //check the hour
					long l1 = System.currentTimeMillis();
					
					nextToDoCalendar.setTimeInMillis(nextToDoCalendar.getTimeInMillis() + currentTaskRunHour); //just for comparison
					long diff = nextToDoCalendar.getTimeInMillis() - l1;
					nextToDoCalendar.setTimeInMillis(nextToDoCalendar.getTimeInMillis() - currentTaskRunHour); //revert to initial value
					if(diff < 0)
						nextToDoCalendar.add(Calendar.WEEK_OF_YEAR, 1);
				}
				nextToDoCalendar.set(Calendar.DAY_OF_WEEK, runDay);
				//set the hour:min
				nextToDoCalendar.setTimeInMillis(nextToDoCalendar.getTimeInMillis() + currentTaskRunHour);
			}
			else if(frequencyType == StaticValues.TASK_SCHEDULED_FREQTYPE_MONTH){
				if(nextToDoCalendar.get(Calendar.DAY_OF_MONTH) > runDay){
					nextToDoCalendar.add(Calendar.MONTH, 1);
				}
				else if(nextToDoCalendar.get(Calendar.DAY_OF_MONTH) == runDay){ //check the hour
					long l1 = System.currentTimeMillis();
					nextToDoCalendar.setTimeInMillis(nextToDoCalendar.getTimeInMillis() + currentTaskRunHour); //just for comparison
					long diff = nextToDoCalendar.getTimeInMillis() - l1;
					nextToDoCalendar.setTimeInMillis(nextToDoCalendar.getTimeInMillis() - currentTaskRunHour); //revert to initial value
					if(diff < 0)
						nextToDoCalendar.add(Calendar.MONTH, 1);
				}
				if(runDay == 32){ //last day
					nextToDoCalendar.set(Calendar.DAY_OF_MONTH, 1);
					nextToDoCalendar.add(Calendar.MONTH, 1); //go to the 1'st of the next month
					nextToDoCalendar.add(Calendar.DAY_OF_YEAR, -1); //go back to the last day of the previous month
				}
				else
					nextToDoCalendar.set(Calendar.DAY_OF_MONTH, runDay);
				//set the hour:min
				nextToDoCalendar.setTimeInMillis(nextToDoCalendar.getTimeInMillis() + currentTaskRunHour);
			}
			else if(frequencyType == StaticValues.TASK_SCHEDULED_FREQTYPE_YEAR){
				if(nextToDoCalendar.get(Calendar.MONTH) > runMonth || 
						(nextToDoCalendar.get(Calendar.MONTH) == runMonth && nextToDoCalendar.get(Calendar.DAY_OF_MONTH) > runDay)){
					nextToDoCalendar.add(Calendar.YEAR, 1);
				}
				else if(nextToDoCalendar.get(Calendar.MONTH) == runMonth && nextToDoCalendar.get(Calendar.DAY_OF_MONTH) == runDay){
					long l1 = System.currentTimeMillis();
					nextToDoCalendar.setTimeInMillis(nextToDoCalendar.getTimeInMillis() + currentTaskRunHour); //just for comparison
					long diff = nextToDoCalendar.getTimeInMillis() - l1;
					nextToDoCalendar.setTimeInMillis(nextToDoCalendar.getTimeInMillis() - currentTaskRunHour); //revert to initial value
					if(diff < 0)
						nextToDoCalendar.add(Calendar.YEAR, 1);
				}
				nextToDoCalendar.set(Calendar.MONTH, runMonth);
				nextToDoCalendar.set(Calendar.DAY_OF_MONTH, runDay);
				nextToDoCalendar.setTimeInMillis(nextToDoCalendar.getTimeInMillis() + currentTaskRunHour);
			}
		}


		return nextToDoCalendar.getTimeInMillis() / 1000;
	}
}
