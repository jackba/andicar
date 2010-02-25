/*
Copyright (C) 2009-2010 Miklos Keresztes - miklos.keresztes@gmail.com

This program is free software; you can redistribute it and/or modify it
under the terms of the GNU General Public License as published by the
Free Software Foundation; either version 2 of the License.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program;
if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 */
package org.andicar.utils;

import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Global constants
 * @author miki
 */
public class Constants
{
    public static final int MENU_PREFERENCES_ID = 10;
    
    public static final int OPTION_MENU_ADD_ID = 21;
    public static final int OPTION_MENU_SHOWINACTIVE_ID = 22;
    public static final int OPTION_MENU_HIDEINACTIVE_ID = 23;
    public static final int OPTION_MENU_SEARCH_ID = 24;
    
    public  static final int CONTEXT_MENU_EDIT_ID = 1;
    public  static final int CONTEXT_MENU_INSERT_ID = 2;
    public  static final int CONTEXT_MENU_DELETE_ID = 3;

    public static final int ACTIVITY_NEW_REQUEST_CODE = 0;
    public static final int ACTIVITY_EDIT_REQUEST_CODE = 1;

    public static final int DB_URI_DRIVERS = 1;
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "MangeCarDb";
    public static final String GLOBAL_PREFERENCE_NAME = "ManageCarPref";

    //no. of decimals when converting uom's to/from base uom
    public static final int volumeDecimals = 2;
    public static final int lengthDecimals = 2;
    public static final MathContext mcVolume = new MathContext( volumeDecimals, RoundingMode.HALF_UP);
    public static final MathContext mcLength = new MathContext( lengthDecimals, RoundingMode.HALF_UP);

    public static final String UOM_LENGTH_TYPE_CODE = "L";
    public static final String UOM_VOLUME_TYPE_CODE = "V";

    //mileage insert mode: 0 = new index; 1 = mileage
    public static int mileageInsertModeNewIndex = 0;
    public static int mileageEditInsertModeMileage = 1;

    public static String errStartIndexOverlap = "ERR_001";
    public static String errNewIndexOverlap = "ERR_002";
    public static String errMileageOverlap = "ERR_003";
    public static String errStopBeforeStartIndex = "ERR_004";

    public static final int TIME_DIALOG_ID = 0;
    public static final int DATE_DIALOG_ID = 1;

}
