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

import org.andicar.persistence.DB;
import org.andicar.persistence.MainDbAdapter;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
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
		
		mDb = new MainDbAdapter(this);
		
		//#1 check todo's for none recurent tasks
		maintainNonRecurentTasks();
		
		mDb.close();
	}
	
	private void maintainNonRecurentTasks(){
		Cursor taskCursor = null;
		Cursor todoCursor = null;
		String taskSelection = null;
		String todoSelection = null;
		String selectSql = null;
		String[] taskSelectionArgs = {Long.toString(System.currentTimeMillis() / 1000)}; //miliseconds to seconds
		String[] todoSelectionArgs = null;
		ContentValues toDoContent = new ContentValues();
		long taskId = -1;
		long carId = -1;

		//check tasks with only time scedule
		taskSelection = MainDbAdapter.isActiveCondition + 
				" AND " + MainDbAdapter.TASK_COL_ISRECURENT_NAME + "='N' " +
				" AND " + MainDbAdapter.TASK_COL_SCHEDULEDFOR_NAME + "='T' " +
				" AND " + MainDbAdapter.TASK_COL_RUNTIME_NAME + " > ?";
		
		taskCursor = mDb.query(MainDbAdapter.TASK_TABLE_NAME, MainDbAdapter.taskTableColNames, taskSelection, taskSelectionArgs, null, null, null);
		
		todoSelection = MainDbAdapter.TODO_COL_TASK_ID_NAME + "=?";
		todoSelectionArgs = new String[1];
		while(taskCursor.moveToNext()){
			taskId = taskCursor.getLong(MainDbAdapter.GEN_COL_ROWID_POS);
			todoSelectionArgs[0] = Long.toString(taskId);
			todoCursor = mDb.query(MainDbAdapter.TODO_TABLE_NAME, MainDbAdapter.todoCarTableColNames, todoSelection, todoSelectionArgs, null, null, null);
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

		taskCursor = mDb.query(selectSql, null);

		todoSelectionArgs = null;
		todoSelection = MainDbAdapter.TODO_COL_TASK_ID_NAME + "=? AND " + MainDbAdapter.TODO_COL_CAR_ID_NAME + "=?";
		todoSelectionArgs = new String[2];
		while(taskCursor.moveToNext()){
			taskId = taskCursor.getLong(MainDbAdapter.GEN_COL_ROWID_POS);
			carId = taskCursor.getLong(taskCursor.getColumnIndex("TaskCar_CarId"));
			todoSelectionArgs[0] = Long.toString(taskId);
			todoSelectionArgs[1] = Long.toString(carId);

			todoCursor = mDb.query(MainDbAdapter.TODO_TABLE_NAME, MainDbAdapter.todoCarTableColNames, 
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


}
