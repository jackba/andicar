/*
 *  AndiCar - a car management software for Android powered devices.
 *
 *  Copyright (C) 2010 Miklos Keresztes (miklos.keresztes@gmail.com)
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

package org.andicar.utils;

import java.math.RoundingMode;

/**
 * Global constants
 * @author miki
 */
public class StaticValues
{
    public static final int DB_URI_DRIVERS = 1;
    public static final int DATABASE_VERSION = 357;
    public static final String DATABASE_NAME = "AndiCar.db";
    public static final String GLOBAL_PREFERENCE_NAME = "AndiCarPreferences";
    
    public static final int MENU_PREFERENCES_ID = 10;
    public static final int MENU_REPORTS_ID = 11;
    public static final int MENU_ABOUT_ID = 12;
    public static final int MENU_HELP_ID = 13;
    public static final int MENU_MILEAGE_ID = 14;
    public static final int MENU_REFUEL_ID = 15;
    public static final int MENU_EXPENSES_ID = 16;
    public static final int MENU_GPSTRACK_ID = 17;
    public static final int MENU_RATE_COMMENT_ID = 18;
    public static final int MENU_ADDON_ID = 19;
    public static final int MENU_LOCALIZE_ID = 20;
//    public static final int MENU_TASK_ID = 21;
    public static final int MENU_TODO_ID = 22;
    
    public static final int OPTION_MENU_ADD_ID = 21;
    public static final int OPTION_MENU_SHOWINACTIVE_ID = 22;
    public static final int OPTION_MENU_HIDEINACTIVE_ID = 23;
    public static final int OPTION_MENU_SEARCH_ID = 24;
    public static final int OPTION_MENU_REPORT_ID = 25;
    
    public static final int CONTEXT_MENU_EDIT_ID = 31;
    public static final int CONTEXT_MENU_INSERT_ID = 32;
    public static final int CONTEXT_MENU_DELETE_ID = 33;
    public static final int CONTEXT_MENU_SENDASEMAIL_ID = 34;
    public static final int CONTEXT_MENU_SHOWONMAP_ID = 35;
    public static final int CONTEXT_MENU_OPENGPSTRACK_ID = 36;
    public static final int CONTEXT_MENU_TODO_DONE_ID = 37;

    public static final int GPSTRACKMAP_MENU_MAPMODE_ID = 41;
    public static final int GPSTRACKMAP_MENU_SATELLITEMODE_ID = 42;

    public static final int ACTIVITY_NEW_REQUEST_CODE = 0;
    public static final int ACTIVITY_EDIT_REQUEST_CODE = 1;

    public static final int NUMBERTYPE_VOLUME = 1;
    public static final int NUMBERTYPE_LENGTH = 2;
    public static final int NUMBERTYPE_AMOUNT = 3;
    public static final int NUMBERTYPE_CONVERSION = 4;
    public static final int NUMBERTYPE_PRICE = 5;
    //no. of decimals & rounding modes
    public static final int DECIMALS_VOLUME = 2;
    public static final RoundingMode ROUNDING_MODE_VOLUME = RoundingMode.HALF_UP;
    public static final int DECIMALS_LENGTH = 2;
    public static final RoundingMode ROUNDING_MODE_LENGTH = RoundingMode.HALF_UP;
    public static final int DECIMALS_AMOUNT = 2;
    public static final RoundingMode ROUNDING_MODE_AMOUNT = RoundingMode.HALF_UP;
    public static final int DECIMALS_CONVERSIONS = 4;
    public static final RoundingMode ROUNDING_MODE_CONVERSIONS = RoundingMode.HALF_UP;
    public static final int DECIMALS_PRICE = 3;
    public static final RoundingMode ROUNDING_MODE_PRICE = RoundingMode.HALF_UP;

    public static final String UOM_LENGTH_TYPE_CODE = "L";
    public static final String UOM_VOLUME_TYPE_CODE = "V";
    public static final String UOM_OTHER_TYPE_CODE = "O";

    //mileage insert mode: 0 = new index; 1 = mileage
    public static int MILEAGE_INSERTMODE_INDEX = 0;
    public static int MILEAGE_INSERTMODE_MILEAGE = 1;

    public static final int DIALOG_TIME_PICKER = 0;
    public static final int DIALOG_DATE_PICKER = 1;
    public static final int DIALOG_LOCAL_SEARCH = 2;
    public static final int DIALOG_REPORT_OPTIONS = 3;
    public static final int DIALOG_DATE_FROM_PICKER = 4;
    public static final int DIALOG_DATE_TO_PICKER = 5;
//    public static final int DIALOG_TIME_PICKER2 = 6;
    public static final int DIALOG_TASK_CAR_LINK = 7;

    public static final int NOTIF_UPDATECHECK_ID = 0;
    public static final int NOTIF_GPS_TRACK_STARTED_ID = 1;
    public static final int NOTIF_GPS_ACCURACY_WARNING_ID = 2;
    public static final int NOTIF_FILESYSTEM_ERROR_ID = 3;
    public static final int NOTIF_GPS_ACCURACY_SHUTDOWN_ID = 4;
    public static final int NOTIF_GPS_DISABLED_ID = 5;
    public static final int NOTIF_GPS_OUTOFSERVICE_ID = 6;
    public static final int NOTIF_BACKUP_SERVICE_EXCEPTION_ID = 7;
    public static final int NOTIF_BACKUP_SERVICE_INFO_ID = 8;
    public static final int NOTIF_BACKUP_SERVICE_ERROR_ID = 9;
    public static final int NOTIF_BACKUP_SERVICE_EXPIRE_ID = 10;
    public static final int NOTIF_SECURE_BACKUP_EXPIRE_ID = 11;
    public static final int NOTIF_SECURE_BACKUP_INFO_ID = 12;
    public static final int NOTIF_SECURE_BACKUP_ERROR_ID = 13;
    
    //used for notification PendingIntents
    public static final int ACTIVITY_REQUEST_CODE_FILEMAILER = 10;
    public static final int ACTIVITY_REQUEST_CODE_BACKUPSERVICE_EXPIRE = 20;
    public static final int ACTIVITY_REQUEST_CODE_SECUREBACKUP_EXPIRE = 30;

    /**
     * 
     */
    public static String DATE_DECODE_TO_ZERO = "0";
    /**
     * upper the hour to 23:59.999
     */
    public static String DATE_DECODE_TO_24 = "24";
    /**
     * leave the hour unchanged
     */
    public static String DATE_DECODE_NO_CHANGE = "12";

    public static final String BASE_FOLDER = "/sdcard/andicar/";
    public static final String REPORT_FOLDER = BASE_FOLDER + "reports/";
    public static final String BACKUP_FOLDER = BASE_FOLDER + "backups/";
    public static final String TRACK_FOLDER = BASE_FOLDER + "gpstrack/";
    public static final String TEMP_FOLDER = BASE_FOLDER + "temp/";
    
    public static final String BACKUP_PREFIX = "bk";
    public static final String BACKUP_SUFIX = ".db";

    public static final String CSV_FORMAT = "csv";
    public static final String KML_FORMAT = "kml";//Keyhole Markup Language (http://en.wikipedia.org/wiki/KML)
    public static final String GPX_FORMAT = "gpx";//GPS eXchange Format (http://en.wikipedia.org/wiki/Gpx)
    public static final String GOP_FORMAT = "gop"; //geopoint coordinates. Used to draw the track on the map

    public static final String EXPENSES_COL_FROMREFUEL_TABLE_NAME = "Refuel";
    
    public static final String DAY_OF_WEEK_NAME = "DayOfWeek";
    
    public static final String FLURRY_ID = "E8C8QUTB7KS46SHMEP6V";

    public static final String VERSION_FILE_URL = "http://sites.google.com/site/andicarservices/andicarversion.txt";
    
    //used in task definition
    public static final String TASK_SCHEDULED_FOR_TIME = "T";
    public static final String TASK_SCHEDULED_FOR_MILEAGE = "M";
    public static final String TASK_SCHEDULED_FOR_BOTH = "B";
    
    public static final int TASK_TIMEFREQUENCYTYPE_ONETIME = 0;
    public static final int TASK_TIMEFREQUENCYTYPE_DAILY = 1;
    public static final int TASK_TIMEFREQUENCYTYPE_WEEKLY = 2;
    public static final int TASK_TIMEFREQUENCYTYPE_MONTHLY = 3;
    public static final int TASK_TIMEFREQUENCYTYPE_YEARLY = 4;
    
    public static final long ONE_DAY_IN_MILISECONDS = 86400000;
}
