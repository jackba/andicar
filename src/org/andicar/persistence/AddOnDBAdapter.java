/*
 *  AndiCar - a car management software for Android powered devices.
 *
 *  Copyright (C) 2013 Miklos Keresztes (miklos.keresztes@gmail.com)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.andicar.persistence;

import android.content.ContentValues;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class AddOnDBAdapter {

    public static final String ADDON_TABLE_NAME = "SYS_ADDON";
    public static final String ADDON_BK_SCHEDULE_TABLE_NAME = "AO_BK_SCHEDULE";
    public static final String ADDON_SECURE_BK_SETTINGS_TABLE_NAME = "AO_SECURE_BK_SETTINGS";
    public static final String ADDON_DATA_TEMPLATE_TABLE_NAME = "AO_DATA_TEMPLATE";
    public static final String ADDON_DATA_TEMPLATE_VALUES_TABLE_NAME = "AO_DATA_TEMPLATE_VALUES";
    
    /**
     * links between bluetooth devices and cars
     */
    public static final String ADDON_BTDEVICE_CAR_TABLE_NAME = "AO_BTDEVICE_CAR";

    public static final String ADDON_BK_SCHEDULE_COL_FREQUENCY_NAME = "Frequency"; //W - weekly; D - daily
    public static final String ADDON_BK_SCHEDULE_COL_DAYS_NAME = "Days"; //7 char length {0|1} string (day 0 to day 6). 1-run bk; 0-skip day
    
    public static final String ADDON_SECURE_BK_SETTINGS_COL_ISINCLUDEGPS_NAME = "IsIncludeGPSTrack";
    public static final String ADDON_SECURE_BK_SETTINGS_COL_ISINCLUDEREPORTS_NAME = "IsIncludeReports";
    public static final String ADDON_SECURE_BK_SETTINGS_COL_ISSHOWNOTIF_NAME = "IsShowNotification";
    
    public static final String ADDON_DATA_TEMPLATE_COL_CLASS_NAME = "ForClass";

    public static final String ADDON_DATA_TEMPLATE_VALUES_COL_TEMPLATE_ID_NAME = ADDON_DATA_TEMPLATE_TABLE_NAME + "_ID";
    public static final String ADDON_DATA_TEMPLATE_VALUES_COL_VALUE_NAME = "Value";
    
    public static final String ADDON_BTDEVICE_CAR_COL_MACADDR_NAME = "DeviceMACAddress";
    public static final String ADDON_BTDEVICE_CAR_CAR_ID_NAME = DB.TABLE_NAME_CAR + "_ID";

    public static final int ADDON_BK_SCHEDULE_COL_FREQUENCY_POS = 4;
    public static final int ADDON_BK_SCHEDULE_COL_DAYS_POS = 5;

    public static final int ADDON_SECURE_BK_SETTINGS_COL_ISINCLUDEGPS_POS = 4;
    public static final int ADDON_SECURE_BK_SETTINGS_COL_ISINCLUDEREPORTS_POS = 5;
    public static final int ADDON_SECURE_BK_SETTINGS_COL_ISSHOWNOTIF_POS = 6;

    public static final int ADDON_DATA_TEMPLATE_COL_CLASS_POS = 4;

    public static final int ADDON_DATA_TEMPLATE_VALUES_COL_TEMPLATE_ID_POS = 4;
    public static final int ADDON_DATA_TEMPLATE_VALUES_COL_VALUE_POS = 5;

    public static final int ADDON_BTDEVICE_CAR_COL_MACADDR_POS = 4;
    public static final int ADDON_BTDEVICE_CAR_CAR_ID_POS = 5;

    public static final String[] addonBKScheduleTableColNames = {DB.COL_NAME_GEN_ROWID, DB.COL_NAME_GEN_NAME, DB.COL_NAME_GEN_ISACTIVE, DB.COL_NAME_GEN_USER_COMMENT,
        ADDON_BK_SCHEDULE_COL_FREQUENCY_NAME, ADDON_BK_SCHEDULE_COL_DAYS_NAME};

    public static final String[] addonSecureBKSettingsTableColNames = {DB.COL_NAME_GEN_ROWID, DB.COL_NAME_GEN_NAME, DB.COL_NAME_GEN_ISACTIVE, DB.COL_NAME_GEN_USER_COMMENT,
    	ADDON_SECURE_BK_SETTINGS_COL_ISINCLUDEGPS_NAME, ADDON_SECURE_BK_SETTINGS_COL_ISINCLUDEREPORTS_NAME, ADDON_SECURE_BK_SETTINGS_COL_ISSHOWNOTIF_NAME};
    
    public static final String[] addonDataTemplateTableColNames = {DB.COL_NAME_GEN_ROWID, DB.COL_NAME_GEN_NAME, DB.COL_NAME_GEN_ISACTIVE, DB.COL_NAME_GEN_USER_COMMENT,
    	ADDON_DATA_TEMPLATE_COL_CLASS_NAME};

    public static final String[] addonDataTemplateValuesTableColNames = {DB.COL_NAME_GEN_ROWID, DB.COL_NAME_GEN_NAME, DB.COL_NAME_GEN_ISACTIVE, DB.COL_NAME_GEN_USER_COMMENT,
    	ADDON_DATA_TEMPLATE_VALUES_COL_TEMPLATE_ID_NAME, ADDON_DATA_TEMPLATE_VALUES_COL_VALUE_NAME};

    public static final String[] addonBTDeviceCarTableColNames = {DB.COL_NAME_GEN_ROWID, DB.COL_NAME_GEN_NAME, DB.COL_NAME_GEN_ISACTIVE, DB.COL_NAME_GEN_USER_COMMENT,
    	ADDON_BTDEVICE_CAR_COL_MACADDR_NAME, ADDON_BTDEVICE_CAR_CAR_ID_NAME};

    public static final String ADDON_TABLE_CREATE_SQL =
        "CREATE TABLE IF NOT EXISTS " + ADDON_TABLE_NAME
        + " ( "
	        + DB.COL_NAME_GEN_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
	        + DB.COL_NAME_GEN_NAME + " TEXT NOT NULL, "
	        + DB.COL_NAME_GEN_ISACTIVE + " TEXT DEFAULT 'Y', "
	        + DB.COL_NAME_GEN_USER_COMMENT + " TEXT NULL "
        + ");";

    public static final String ADDON_BK_SCHEDULE_TABLE_CREATE_SQL  =
        "CREATE TABLE IF NOT EXISTS " + ADDON_BK_SCHEDULE_TABLE_NAME
        + " ( "
	        + DB.COL_NAME_GEN_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
	        + DB.COL_NAME_GEN_NAME + " TEXT NOT NULL, "
	        + DB.COL_NAME_GEN_ISACTIVE + " TEXT DEFAULT 'Y', "
	        + DB.COL_NAME_GEN_USER_COMMENT + " TEXT NULL, "
	        + ADDON_BK_SCHEDULE_COL_FREQUENCY_NAME + " TEXT NOT NULL, "
	        + ADDON_BK_SCHEDULE_COL_DAYS_NAME + " TEXT NOT NULL"
        + ");";

    public static final String ADDON_SECURE_BK_SETTINGS_TABLE_CREATE_SQL =
        "CREATE TABLE IF NOT EXISTS " + ADDON_SECURE_BK_SETTINGS_TABLE_NAME
        + " ( "
	        + DB.COL_NAME_GEN_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
	        + DB.COL_NAME_GEN_NAME + " TEXT NOT NULL, "
	        + DB.COL_NAME_GEN_ISACTIVE + " TEXT DEFAULT 'Y', "
	        + DB.COL_NAME_GEN_USER_COMMENT + " TEXT NULL, "
	        + ADDON_SECURE_BK_SETTINGS_COL_ISINCLUDEGPS_NAME + " TEXT NOT NULL DEFAULT 'N', "
	        + ADDON_SECURE_BK_SETTINGS_COL_ISINCLUDEREPORTS_NAME + " TEXT NOT NULL DEFAULT 'N', "
	        + ADDON_SECURE_BK_SETTINGS_COL_ISSHOWNOTIF_NAME + " TEXT NOT NULL DEFAULT 'Y'"
        + ");";

    public static final String ADDON_DATA_TEMPLATE_TABLE_CREATE_SQL =
        "CREATE TABLE IF NOT EXISTS " + ADDON_DATA_TEMPLATE_TABLE_NAME
        + " ( "
	        + DB.COL_NAME_GEN_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
	        + DB.COL_NAME_GEN_NAME + " TEXT NOT NULL, "
	        + DB.COL_NAME_GEN_ISACTIVE + " TEXT DEFAULT 'Y', "
	        + DB.COL_NAME_GEN_USER_COMMENT + " TEXT NULL, "
	        + ADDON_DATA_TEMPLATE_COL_CLASS_NAME + " TEXT NOT NULL"
        + ");";

    public static final String ADDON_DATA_TEMPLATE_VALUES_TABLE_CREATE_SQL =
        "CREATE TABLE IF NOT EXISTS " + ADDON_DATA_TEMPLATE_VALUES_TABLE_NAME
        + " ( "
	        + DB.COL_NAME_GEN_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
	        + DB.COL_NAME_GEN_NAME + " TEXT NOT NULL, "
	        + DB.COL_NAME_GEN_ISACTIVE + " TEXT DEFAULT 'Y', "
	        + DB.COL_NAME_GEN_USER_COMMENT + " TEXT NULL, "
	        + ADDON_DATA_TEMPLATE_VALUES_COL_TEMPLATE_ID_NAME + " INTEGER NOT NULL, "
	        + ADDON_DATA_TEMPLATE_VALUES_COL_VALUE_NAME + " TEXT NULL"
        + ");";

    public static final String ADDON_BTDEVICE_CAR_TABLE_CREATE_SQL =
        "CREATE TABLE IF NOT EXISTS " + ADDON_BTDEVICE_CAR_TABLE_NAME
        + " ( "
	        + DB.COL_NAME_GEN_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
	        + DB.COL_NAME_GEN_NAME + " TEXT NOT NULL, "
	        + DB.COL_NAME_GEN_ISACTIVE + " TEXT DEFAULT 'Y', "
	        + DB.COL_NAME_GEN_USER_COMMENT + " TEXT NULL, "
	        + ADDON_BTDEVICE_CAR_COL_MACADDR_NAME + " TEXT NOT NULL, "
	        + ADDON_BTDEVICE_CAR_CAR_ID_NAME + " INTEGER NOT NULL "
        + ");";

    public static void createAddOnTable(SQLiteDatabase db) throws SQLException {
        //create addon table
        db.execSQL(ADDON_TABLE_CREATE_SQL);
    }
    
    public static void createAddOnBKScheduleTable(SQLiteDatabase db) throws SQLException {
        //create addon table
        db.execSQL(ADDON_BK_SCHEDULE_TABLE_CREATE_SQL);
        String initSQL = 
        	"INSERT INTO " + ADDON_BK_SCHEDULE_TABLE_NAME + " ( " +
	            DB.COL_NAME_GEN_NAME + ", " +
	            DB.COL_NAME_GEN_ISACTIVE + ", " +
		        DB.COL_NAME_GEN_USER_COMMENT + ", " +
		        ADDON_BK_SCHEDULE_COL_FREQUENCY_NAME + ", " +
		        ADDON_BK_SCHEDULE_COL_DAYS_NAME + " ) " +
	        "VALUES ( " +
	        	"946753200778, " + //just for time part (01-01-2000 21:00)  
	        	"'Y', " +
	        	"'10', " +
	        	"'D', " +
	        	"'0000000' )";
        db.execSQL(initSQL);
    }

    public static void createAddOnSecureBKSettingsTable(SQLiteDatabase db) throws SQLException {
        //create addon table
        db.execSQL(ADDON_SECURE_BK_SETTINGS_TABLE_CREATE_SQL);
        String initSQL = 
        	"INSERT INTO " + ADDON_SECURE_BK_SETTINGS_TABLE_NAME + " ( " +
	            DB.COL_NAME_GEN_NAME + ", " +
	            DB.COL_NAME_GEN_ISACTIVE + ", " +
		        DB.COL_NAME_GEN_USER_COMMENT + ", " +
		        ADDON_SECURE_BK_SETTINGS_COL_ISINCLUDEGPS_NAME + ", " +
		        ADDON_SECURE_BK_SETTINGS_COL_ISINCLUDEREPORTS_NAME + ", " +
		        ADDON_SECURE_BK_SETTINGS_COL_ISSHOWNOTIF_NAME +
		        	" ) " +
	        "VALUES ( " +
	        	"'Default', " +   
	        	"'Y', " +
	        	"'Default', " +
	        	"'N', " +
	        	"'N', " +
	        	"'Y' " +
	        		" )";
        db.execSQL(initSQL);
    }

    public static void createAddOnDataTemplateTables(SQLiteDatabase db) throws SQLException {
        //create addon table
        db.execSQL(ADDON_DATA_TEMPLATE_TABLE_CREATE_SQL);
        db.execSQL(ADDON_DATA_TEMPLATE_VALUES_TABLE_CREATE_SQL);
		db.execSQL("CREATE INDEX IF NOT EXISTS " + ADDON_DATA_TEMPLATE_TABLE_NAME + "_IX1 " 
					+ "ON " + ADDON_DATA_TEMPLATE_TABLE_NAME + 
						" (" + ADDON_DATA_TEMPLATE_COL_CLASS_NAME + " )");
		db.execSQL("CREATE INDEX IF NOT EXISTS " + ADDON_DATA_TEMPLATE_VALUES_TABLE_NAME + "_IX1 " 
				+ "ON " + ADDON_DATA_TEMPLATE_VALUES_TABLE_NAME + 
					" (" + ADDON_DATA_TEMPLATE_VALUES_COL_TEMPLATE_ID_NAME + " )");
    }
    
    public static void createAddOnBTDeviceCarTable(SQLiteDatabase db) throws SQLException {
        //create addon table
        db.execSQL(ADDON_BTDEVICE_CAR_TABLE_CREATE_SQL);
    }

    public static void upgradeTo358(SQLiteDatabase db) throws SQLException {
    	createAddOnDataTemplateTables(db);
    }

    public static void upgradeTo359(SQLiteDatabase db) throws SQLException {
    	createAddOnBTDeviceCarTable(db);
    }
    
    public static boolean recordDeleted(SQLiteDatabase db, String table, long rowID){
    	
    	if(table.equals(MainDbAdapter.TABLE_NAME_CAR)){
    		db.delete(ADDON_BTDEVICE_CAR_TABLE_NAME, ADDON_BTDEVICE_CAR_CAR_ID_NAME + " = " + rowID, null);
    	}
    	return true;
    }

    public static boolean recordUpdated(SQLiteDatabase db, String table, long rowID, ContentValues content){
    	
    	if(table.equals(MainDbAdapter.TABLE_NAME_CAR)){
    		if(content.getAsString(MainDbAdapter.COL_NAME_GEN_ISACTIVE).equals("N"))
    			db.delete(ADDON_BTDEVICE_CAR_TABLE_NAME, ADDON_BTDEVICE_CAR_CAR_ID_NAME + " = " + rowID, null);    		
    	}
    	return true;
    }
    
	public static boolean isSubscriptionValid(MainDbAdapter db, int addOnId) throws Exception{
		return true;
	}

}
