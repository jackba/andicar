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
    public static final int MENU_PREFERENCES_ID = 10;
    public static final int MENU_REPORTS_ID = 11;
    public static final int MENU_ABOUT_ID = 12;
    public static final int MENU_HELP_ID = 13;
    public static final int MENU_MILEAGE_ID = 14;
    public static final int MENU_REFUEL_ID = 15;
    public static final int MENU_EXPENSES_ID = 16;
    
    public static final int OPTION_MENU_ADD_ID = 21;
    public static final int OPTION_MENU_SHOWINACTIVE_ID = 22;
    public static final int OPTION_MENU_HIDEINACTIVE_ID = 23;
    public static final int OPTION_MENU_SEARCH_ID = 24;
    public static final int OPTION_MENU_REPORT_ID = 25;
    
    public  static final int CONTEXT_MENU_EDIT_ID = 1;
    public  static final int CONTEXT_MENU_INSERT_ID = 2;
    public  static final int CONTEXT_MENU_DELETE_ID = 3;

    public static final int ACTIVITY_NEW_REQUEST_CODE = 0;
    public static final int ACTIVITY_EDIT_REQUEST_CODE = 1;

    public static final int DB_URI_DRIVERS = 1;
    public static final int DATABASE_VERSION = 201;
    public static final String DATABASE_NAME = "AndiCar.db";
    public static final String GLOBAL_PREFERENCE_NAME = "AndiCarPreferences";

    //no. of decimals when converting uom's to/from base uom
    public static final int volumeDecimals = 2;
    public static final RoundingMode volumeRoundingMode = RoundingMode.HALF_UP;
    public static final int lengthDecimals = 2;
    public static final RoundingMode lengthRoundingMode = RoundingMode.HALF_UP;
    public static final int amtDecimals = 2;
    public static final RoundingMode amountRoundingMode = RoundingMode.HALF_UP;
    public static final int conversionDecimals = 4;
    public static final RoundingMode conversionRoundingMode = RoundingMode.HALF_UP;


    public static final String UOM_LENGTH_TYPE_CODE = "L";
    public static final String UOM_VOLUME_TYPE_CODE = "V";

    //mileage insert mode: 0 = new index; 1 = mileage
    public static int mileageInsertModeNewIndex = 0;
    public static int mileageEditInsertModeMileage = 1;

    public static final int TIME_DIALOG_ID = 0;
    public static final int DATE_DIALOG_ID = 1;

    /**
     * 
     */
    public static String dateDecodeTypeTo0Hour = "0";
    /**
     * upper the hour to 23:59.999
     */
    public static String dateDecodeTypeTo24Hour = "24";
    /**
     * leave the hour unchanged
     */
    public static String dateDecodeTypeNoChange = "12";

    public static int localSearchDialog = 0;
    public static int reportOptionsDialog = 1;

    public static final String reportFolder = "/sdcard/andicar/reports/";
    public static final String backupFolder = "/sdcard/andicar/backups/";
    public static final String backupPrefix = "bk";
    public static final String backupSufix = ".db";

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
}
