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
    public static final int DATABASE_VERSION = 222;
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
    
    public static final int OPTION_MENU_ADD_ID = 21;
    public static final int OPTION_MENU_SHOWINACTIVE_ID = 22;
    public static final int OPTION_MENU_HIDEINACTIVE_ID = 23;
    public static final int OPTION_MENU_SEARCH_ID = 24;
    public static final int OPTION_MENU_REPORT_ID = 25;
    
    public static final int CONTEXT_MENU_EDIT_ID = 31;
    public static final int CONTEXT_MENU_INSERT_ID = 32;
    public static final int CONTEXT_MENU_DELETE_ID = 33;
    public static final int CONTEXT_MENU_SENDASEMAIL_ID = 34;

    public static final int GPSTRACKMAP_MENU_MAPMODE_ID = 41;
    public static final int GPSTRACKMAP_MENU_SATELLITEMODE_ID = 42;

    public static final int ACTIVITY_NEW_REQUEST_CODE = 0;
    public static final int ACTIVITY_EDIT_REQUEST_CODE = 1;

    //no. of decimals when converting uom's to/from base uom
    public static final int DECIMALS_VOLUME = 2;
    public static final RoundingMode ROUNDING_MODE_VOLUME = RoundingMode.HALF_UP;
    public static final int DECIMALS_LENGTH = 2;
    public static final RoundingMode ROUNDING_MODE_LENGTH = RoundingMode.HALF_UP;
    public static final int DECIMALS_AMOUNT = 2;
    public static final RoundingMode ROUNDING_MODE_AMOUNT = RoundingMode.HALF_UP;
    public static final int DECIMALS_CONVERSIONS = 4;
    public static final RoundingMode ROUNDING_MODE_CONVERSIONS = RoundingMode.HALF_UP;


    public static final String UOM_LENGTH_TYPE_CODE = "L";
    public static final String UOM_VOLUME_TYPE_CODE = "V";

    //mileage insert mode: 0 = new index; 1 = mileage
    public static int MILEAGE_INSERTMODE_INDEX = 0;
    public static int MILEAGE_INSERTMODE_MILEAGE = 1;

    public static final int TIME_DIALOG_ID = 0;
    public static final int DATE_DIALOG_ID = 1;

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

    public static int DIALOG_LOCAL_SEARCH = 0;
    public static int DIALOG_REPORT_OPTIONS = 1;

    public static final String REPORT_FOLDER = "/sdcard/andicar/reports/";
    public static final String BACKUP_FOLDER = "/sdcard/andicar/backups/";
    public static final String TRACK_FOLDER = "/sdcard/andicar/gpstrack/";
    public static final String BACKUP_PREFIX = "bk";
    public static final String BACKUP_SUFIX = ".db";

    public static final String CSV_FORMAT = "csv";
    public static final String KML_FORMAT = "kml";//Keyhole Markup Language (http://en.wikipedia.org/wiki/KML)
    public static final String GPX_FORMAT = "gpx";//GPS eXchange Format (http://en.wikipedia.org/wiki/Gpx)

    public static final String EXPENSES_COL_FROMREFUEL_TABLE_NAME = "Refuel";
    
    public static int NOTIF_TYPE_GPSTRACK_STARTED_ID = 1;
    public static int NOTIF_TYPE_ACCURACY_WARNING_ID = 2;
    public static int NOTIF_TYPE_FILESYSTEM_ERROR_ID = 3;
    public static int NOTIF_TYPE_ACCURACY_SHUTDOWN_ID = 4;

    
    public static String LM_COPYRIGHT_HTML1 =
            "&nbsp;&nbsp;&nbsp;&nbsp;<b><i>AndiCar</i></b> is a free and open source car management " +
            "software for Android powered devices. It is licensed under the terms of the GNU General Public License.<br>" +
            "&nbsp;&nbsp;&nbsp;&nbsp;For more information (how to's, latest news, etc.) please visit the project home page at <a href=\"http://sites.google.com/site/andicarfree/\"> " +
            "http://sites.google.com/site/andicarfree/</a><br>" +
            "<small>If you get a \"Web page not available\" message, refresh the page in your browser.</small><br>" +
            "&nbsp;&nbsp;&nbsp;&nbsp;For questions you are welcome to the <b><i>AndiCar</i></b> discussion list at " +
            "<a href=\"http://groups.google.com/group/andicar\">http://groups.google.com/group/andicar</a><br>" +
            "&nbsp;&nbsp;&nbsp;&nbsp;For bug reports or enhancement requests don\'t hesitate to use the <b><i>AndiCar</i></b> issue system at " +
            "<a href=\"https://code.google.com/p/andicar/issues/list\">https://code.google.com/p/andicar/issues/list</a>.<br>" +
            "&nbsp;&nbsp;&nbsp;&nbsp;You can subscribe for the project news at <a href=\"http://sites.google.com/site/andicarfree/News/\">http://sites.google.com/site/andicarfree/News/</a>.<br>" +
            "&nbsp;&nbsp;&nbsp;&nbsp;For other things you can send an email to andicar.support@gmail.com.<br><br>" +
            "&nbsp;&nbsp;&nbsp;&nbsp;You can help the <a href=\"http://sites.google.com/site/andicarfree/development-directions\">future development</a> of <b><i>AndiCar</b></i> by making a donation. " +
            "All donations can be made using a credit card or PayPal account.";
    public static String LM_COPYRIGHT_HTML2 =
            "<br><br>&nbsp;&nbsp;&nbsp;&nbsp;Thank you for using <b><i>AndiCar</i></b>.<br><br>" +
            "<br><b><i>Copyright notice</i></b>:<br> " +
            "&nbsp;&nbsp;&nbsp;&nbsp;Copyright (C) 2010 Miklos Keresztes (miklos.keresztes@gmail.com)<br> " +
            "&nbsp;&nbsp;&nbsp;&nbsp;This program is free software: you can redistribute it and/or modify " +
            "it under the terms of the GNU General Public License as published by " +
            "the Free Software Foundation, either version 3 of the License, or (at your option) any later version.<br> " +
            "&nbsp;&nbsp;&nbsp;&nbsp;This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; " +
            "without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. " +
            "See the GNU General Public License for more details.<br> " +
            "&nbsp;&nbsp;&nbsp;&nbsp;You should have received a copy of the GNU General Public License along with this program. " +
            "If not, see <a href=\"http://www.gnu.org/licenses\">http://www.gnu.org/licenses</a>.<br> " +
            "&nbsp;&nbsp;&nbsp;&nbsp;The whole license can be found <a href=\"http://www.gnu.org/licenses/gpl.html\">here</a>.<br>";
    
    public static final String FLURRY_ID = "E8C8QUTB7KS46SHMEP6V";
}
