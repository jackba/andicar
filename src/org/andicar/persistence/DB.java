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
package org.andicar.persistence;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import org.andicar.utils.StaticValues;
import org.andicar.utils.Utils;

/**
 * Database object names and database creation/update
 * @author miki
 */
public class DB {
    public String lastErrorMessage = null;
    public Exception lasteException;
    protected static final String TAG = "MainDbAdapter";
    //drivers
    public static final String DRIVER_TABLE_NAME = "DEF_DRIVER";
    //cars
    public static final String CAR_TABLE_NAME = "DEF_CAR";
    //uoms
    public static final String UOM_TABLE_NAME = "DEF_UOM";
    //expense types
    public static final String EXPENSETYPE_TABLE_NAME = "DEF_EXPENSETYPE";
    //uom conversion rates
    public static final String UOM_CONVERSION_TABLE_NAME = "DEF_UOMCONVERTIONRATE";
    //currencies
    public static final String CURRENCY_TABLE_NAME = "DEF_CURRENCY";
    //mileages
    public static final String MILEAGE_TABLE_NAME = "CAR_MILEAGE";
    //refuel
    public static final String REFUEL_TABLE_NAME = "CAR_REFUEL";
    //expense categories (eg. Refuel, Service, Insurance, etc.
    public static final String EXPENSECATEGORY_TABLE_NAME = "DEF_EXPENSECATEGORY";
    //car expenses
    public static final String EXPENSES_TABLE_NAME = "CAR_EXPENSE";
    //currency rate
    public static final String CURRENCYRATE_TABLE_NAME = "DEF_CURRENCYRATE";
    //gps track table
    public static final String GPSTRACK_TABLE_NAME = "GPS_TRACK";
    public static final String GPSTRACKDETAIL_TABLE_NAME = "GPS_TRACKDETAIL";

    //column names. Some is general (GEN_) some is particular
    //generic columns must be first and must be created for ALL TABLES
    public static final String GEN_COL_ROWID_NAME = "_id";
    public static final String GEN_COL_NAME_NAME = "Name";
    public static final String GEN_COL_ISACTIVE_NAME = "IsActive";
    public static final String GEN_COL_USER_COMMENT_NAME = "UserComment";
    //driver specific column names
    public static final String DRIVER_COL_LICENSE_NO_NAME = "LicenseNo";
    //car specific column names
    public static final String CAR_COL_MODEL_NAME = "Model";
    public static final String CAR_COL_REGISTRATIONNO_NAME = "RegistrationNo";
    public static final String CAR_COL_INDEXSTART_NAME = "IndexStart";
    public static final String CAR_COL_INDEXCURRENT_NAME = "IndexCurrent";
    public static final String CAR_COL_UOMLENGTH_ID_NAME = UOM_TABLE_NAME + "_Length_ID";
    public static final String CAR_COL_UOMVOLUME_ID_NAME = UOM_TABLE_NAME + "_Volume_ID";
    public static final String CAR_COL_CURRENCY_ID_NAME = CURRENCY_TABLE_NAME + "_ID";
    //uom specific column names
    public static final String UOM_COL_CODE_NAME = "Code";
    public static final String UOM_COL_UOMTYPE_NAME = "UOMType"; //V - Volume or L - Length
    //uom conversion specific column names
    public static final String UOM_CONVERSION_COL_UOMFROM_ID_NAME = UOM_TABLE_NAME + "_From_ID";
    public static final String UOM_CONVERSION_COL_UOMTO_ID_NAME = UOM_TABLE_NAME + "_To_ID";
    public static final String UOM_CONVERSION_COL_RATE_NAME = "ConvertionRate";
    //mileage specific columns
    public static final String MILEAGE_COL_DATE_NAME = "Date";
    public static final String MILEAGE_COL_CAR_ID_NAME = CAR_TABLE_NAME + "_ID";
    public static final String MILEAGE_COL_DRIVER_ID_NAME = DRIVER_TABLE_NAME + "_ID";
    public static final String MILEAGE_COL_INDEXSTART_NAME = "IndexStart";
    public static final String MILEAGE_COL_INDEXSTOP_NAME = "IndexStop";
    public static final String MILEAGE_COL_UOMLENGTH_ID_NAME = UOM_TABLE_NAME + "_Length_ID";
    public static final String MILEAGE_COL_EXPENSETYPE_ID_NAME = EXPENSETYPE_TABLE_NAME + "_ID";
    public static final String MILEAGE_COL_GPSTRACKLOG_NAME = "GPSTrackLog";
    //currencies
    public static final String CURRENCY_COL_CODE_NAME = "Code";
    //refuel
    public static final String REFUEL_COL_CAR_ID_NAME = CAR_TABLE_NAME + "_ID";
    public static final String REFUEL_COL_DRIVER_ID_NAME = DRIVER_TABLE_NAME + "_ID";
    public static final String REFUEL_COL_EXPENSETYPE_ID_NAME = EXPENSETYPE_TABLE_NAME + "_ID";
    public static final String REFUEL_COL_INDEX_NAME = "CarIndex";
    public static final String REFUEL_COL_QUANTITY_NAME = "Quantity";
    public static final String REFUEL_COL_UOMVOLUME_ID_NAME = UOM_TABLE_NAME + "_Volume_ID";
    public static final String REFUEL_COL_PRICE_NAME = "Price";
    public static final String REFUEL_COL_CURRENCY_ID_NAME = CURRENCY_TABLE_NAME + "_ID";
    public static final String REFUEL_COL_DATE_NAME = "Date";
    public static final String REFUEL_COL_DOCUMENTNO_NAME = "DocumentNo";
    public static final String REFUEL_COL_EXPENSECATEGORY_NAME = EXPENSECATEGORY_TABLE_NAME + "_ID";
    public static final String REFUEL_COL_ISFULLREFUEL_NAME = "IsFullRefuel";
    public static final String REFUEL_COL_QUANTITYENTERED_NAME = "QuantityEntered";
    public static final String REFUEL_COL_UOMVOLUMEENTERED_ID_NAME = UOM_TABLE_NAME + "_EnteredVolume_ID";
    public static final String REFUEL_COL_PRICEENTERED_NAME = "PriceEntered";
    public static final String REFUEL_COL_CURRENCYENTERED_ID_NAME = CURRENCY_TABLE_NAME + "_Entered_ID";
    public static final String REFUEL_COL_CURRENCYRATE_NAME = "CurrencyRate"; //CurrencyEntered -> Car Base Currency
    public static final String REFUEL_COL_UOMVOLCONVERSIONRATE_NAME = "UOMVolumeConversionRate";

    //expense category
    public static final String EXPENSECATEGORY_COL_ISEXCLUDEFROMMILEAGECOST_NAME = "IsExcludefromMileagecost";
    //car expenses
    public static final String EXPENSES_COL_CAR_ID_NAME = CAR_TABLE_NAME + "_ID";
    public static final String EXPENSES_COL_DRIVER_ID_NAME = DRIVER_TABLE_NAME + "_ID";
    public static final String EXPENSES_COL_EXPENSECATEGORY_ID_NAME = EXPENSECATEGORY_TABLE_NAME + "_ID";
    public static final String EXPENSES_COL_EXPENSETYPE_ID_NAME = EXPENSETYPE_TABLE_NAME + "_ID";
    public static final String EXPENSES_COL_AMOUNT_NAME = "Amount";
    public static final String EXPENSES_COL_CURRENCY_ID_NAME = CURRENCY_TABLE_NAME + "_ID";
    public static final String EXPENSES_COL_DATE_NAME = "Date";
    public static final String EXPENSES_COL_DOCUMENTNO_NAME = "DocumentNo";
    public static final String EXPENSES_COL_INDEX_NAME = "CarIndex";
    public static final String EXPENSES_COL_FROMTABLE_NAME = "FromTable";
    public static final String EXPENSES_COL_FROMRECORD_ID_NAME = "FromRecordId";
    public static final String EXPENSES_COL_AMOUNTENTERED_NAME = "AmountEntered";
    public static final String EXPENSES_COL_CURRENCYENTERED_ID_NAME = CURRENCY_TABLE_NAME + "_Entered_ID";
    public static final String EXPENSES_COL_CURRENCYRATE_NAME = "CurrencyRate"; //CurrencyEntered -> Car Base Currency

    //currency rate
    public static final String CURRENCYRATE_COL_FROMCURRENCY_ID_NAME = CURRENCYRATE_TABLE_NAME + "_From_ID";
    public static final String CURRENCYRATE_COL_TOCURRENCY_ID_NAME = CURRENCYRATE_TABLE_NAME + "_To_ID";
    public static final String CURRENCYRATE_COL_RATE_NAME = "Rate";
    public static final String CURRENCYRATE_COL_INVERSERATE_NAME = "InverseRate";

    //gps track
    public static final String GPSTRACK_COL_CAR_ID_NAME = CAR_TABLE_NAME + "_ID";
    public static final String GPSTRACK_COL_DRIVER_ID_NAME = DRIVER_TABLE_NAME + "_ID";
    public static final String GPSTRACK_COL_MILEAGE_ID_NAME = MILEAGE_TABLE_NAME + "_ID";
    public static final String GPSTRACK_COL_DATE_NAME = "Date";
    //gps track detail
    public static final String GPSTRACKDETAIL_COL_GPSTRACK_ID_NAME = GPSTRACK_TABLE_NAME + "_ID";
    public static final String GPSTRACKDETAIL_COL_FILEFORMAT_NAME = "Format"; //see StaticValues.gpsTrackFormat...
    //for space usage consideration we use files on the sdcard to store the tracked locations
//    public static final String GPSTRACKDETAIL_COL_ACCURACY_NAME = "Accuracy";
//    public static final String GPSTRACKDETAIL_COL_ALTITUDE_NAME = "Altitude";
//    public static final String GPSTRACKDETAIL_COL_LATITUDE_NAME = "Latitude";
//    public static final String GPSTRACKDETAIL_COL_LONGITUDE_NAME = "Longitude";
//    public static final String GPSTRACKDETAIL_COL_SPEED_NAME = "Speed"; //meters/second
//    public static final String GPSTRACKDETAIL_COL_TIME_NAME = "Time"; //milliseconds since January 1, 1970.
//    public static final String GPSTRACKDETAIL_COL_DISTNACE_NAME = "Distance";
//    public static final String GPSTRACKDETAIL_COL_BEARING_NAME = "Bearing";

    //column positions. Some is general (GEN_) some is particular
    //generic columns must be first and must be created for ALL TABLES
    public static final int GEN_COL_ROWID_POS = 0;
    public static final int GEN_COL_NAME_POS = 1;
    public static final int GEN_COL_ISACTIVE_POS = 2;
    public static final int GEN_COL_USER_COMMENT_POS = 3;
    //driver specidfic column positions
    public static final int DRIVER_COL_LICENSE_NO_POS = 4;
    //car specific column positions
    public static final int CAR_COL_MODEL_POS = 4;
    public static final int CAR_COL_REGISTRATIONNO_POS = 5;
    public static final int CAR_COL_INDEXSTART_POS = 6;
    public static final int CAR_COL_INDEXCURRENT_POS = 7;
    public static final int CAR_COL_UOMLENGTH_ID_POS = 8;
    public static final int CAR_COL_UOMVOLUME_ID_POS = 9;
    public static final int CAR_COL_CURRENCY_ID_POS = 10;
    //uom specific column positions
    public static final int UOM_COL_CODE_POS = 4;
    public static final int UOM_COL_UOMTYPE_POS = 5;
    //uom convertion specific column positions
    public static final int UOM_CONVERSION_COL_UOMFROM_ID_POS = 4;
    public static final int UOM_CONVERSION_COL_UOMTO_ID_POS = 5;
    public static final int UOM_CONVERSION_COL_RATE_POS = 6;
    //mileage specific column positions
    public static final int MILEAGE_COL_DATE_POS = 4;
    public static final int MILEAGE_COL_CAR_ID_POS = 5;
    public static final int MILEAGE_COL_DRIVER_ID_POS = 6;
    public static final int MILEAGE_COL_INDEXSTART_POS = 7;
    public static final int MILEAGE_COL_INDEXSTOP_POS = 8;
    public static final int MILEAGE_COL_UOMLENGTH_ID_POS = 9;
    public static final int MILEAGE_COL_EXPENSETYPE_ID_POS = 10;
    public static final int MILEAGE_COL_GPSTRACKLOG_POS = 11;
    //currencies
    public static int CURRENCY_COL_CODE_POS = 4;
    //refuel
    public static final int REFUEL_COL_CAR_ID_POS = 4;
    public static final int REFUEL_COL_DRIVER_ID_POS = 5;
    public static final int REFUEL_COL_EXPENSETYPE_ID_POS = 6;
    public static final int REFUEL_COL_INDEX_POS = 7;
    public static final int REFUEL_COL_QUANTITY_POS = 8;
    public static final int REFUEL_COL_UOMVOLUME_ID_POS = 9;
    public static final int REFUEL_COL_PRICE_POS = 10;
    public static final int REFUEL_COL_CURRENCY_ID_POS = 11;
    public static final int REFUEL_COL_DATE_POS = 12;
    public static final int REFUEL_COL_DOCUMENTNO_POS = 13;
    public static final int REFUEL_COL_EXPENSECATEGORY_ID_POS = 14;
    public static final int REFUEL_COL_ISFULLREFUEL_POS = 15;
    public static final int REFUEL_COL_QUANTITYENTERED_POS = 16;
    public static final int REFUEL_COL_UOMVOLUMEENTERED_ID_POS = 17;
    public static final int REFUEL_COL_PRICEENTERED_POS = 18;
    public static final int REFUEL_COL_CURRENCYENTERED_ID_POS = 19;
    public static final int REFUEL_COL_CURRENCYRATE_POS = 20;
    public static final int REFUEL_COL_UOMVOLCONVERSIONRATE_POS = 21;
     //expense category
    public static final int EXPENSECATEGORY_COL_ISEXCLUDEFROMMILEAGECOST_POS = 4;
    //car expenses
    public static final int EXPENSES_COL_CAR_ID_POS = 4;
    public static final int EXPENSES_COL_DRIVER_ID_POS = 5;
    public static final int EXPENSES_COL_EXPENSECATEGORY_POS = 6;
    public static final int EXPENSES_COL_EXPENSETYPE_ID_POS = 7;
    public static final int EXPENSES_COL_AMOUNT_POS = 8;
    public static final int EXPENSES_COL_CURRENCY_ID_POS = 9;
    public static final int EXPENSES_COL_DATE_POS = 10;
    public static final int EXPENSES_COL_DOCUMENTNO_POS = 11;
    public static final int EXPENSES_COL_INDEX_POS = 12;
    public static final int EXPENSES_COL_FROMTABLE_POS = 13;
    public static final int EXPENSES_COL_FROMRECORD_POS = 14;
    public static final int EXPENSES_COL_AMOUNTENTERED_POS = 15;
    public static final int EXPENSES_COL_CURRENCYENTERED_ID_POS = 16;
    public static final int EXPENSES_COL_CURRENCYRATE_POS = 17;

    //currency rate
    public static final int CURRENCYRATE_COL_FROMCURRENCY_ID_POS = 4;
    public static final int CURRENCYRATE_COL_TOCURRENCY_ID_POS = 5;
    public static final int CURRENCYRATE_COL_RATE_POS = 6;
    public static final int CURRENCYRATE_COL_INVERSERATE_POS = 7;

        //gps track
    public static final int GPSTRACK_COL_CAR_ID_POS = 4;
    public static final int GPSTRACK_COL_DRIVER_ID_POS = 5;
    public static final int GPSTRACK_COL_MILEAGE_ID_POS = 6;
    public static final int GPSTRACK_COL_DATE_POS = 7;
    //gps track detail
    public static final int GPSTRACKDETAIL_COL_GPSTRACK_ID_POS = 4;
    //for space usage consideration we use files on the sdcard to store the tracked locations
//    public static final int GPSTRACKDETAIL_COL_ACCURACY_POS = 5;
//    public static final int GPSTRACKDETAIL_COL_ALTITUDE_POS = 6;
//    public static final int GPSTRACKDETAIL_COL_LATITUDE_POS = 7;
//    public static final int GPSTRACKDETAIL_COL_LONGITUDE_POS = 8;
//    public static final int GPSTRACKDETAIL_COL_SPEED_POS = 9;
//    public static final int GPSTRACKDETAIL_COL_TIME_POS = 10;
//    public static final int GPSTRACKDETAIL_COL_DISTNACE_POS = 11;
//    public static final int GPSTRACKDETAIL_COL_BEARING_POS = 12;

    public static final String[] driverTableColNames = {GEN_COL_ROWID_NAME, GEN_COL_NAME_NAME, GEN_COL_ISACTIVE_NAME, GEN_COL_USER_COMMENT_NAME,
        DRIVER_COL_LICENSE_NO_NAME};

    public static final String[] carTableColNames = {GEN_COL_ROWID_NAME, GEN_COL_NAME_NAME, GEN_COL_ISACTIVE_NAME, GEN_COL_USER_COMMENT_NAME,
        CAR_COL_MODEL_NAME, CAR_COL_REGISTRATIONNO_NAME, CAR_COL_INDEXSTART_NAME, CAR_COL_INDEXCURRENT_NAME,
        CAR_COL_UOMLENGTH_ID_NAME, CAR_COL_UOMVOLUME_ID_NAME, CAR_COL_CURRENCY_ID_NAME};

    public static final String[] uomTableColNames = {GEN_COL_ROWID_NAME, GEN_COL_NAME_NAME, GEN_COL_ISACTIVE_NAME, GEN_COL_USER_COMMENT_NAME,
        UOM_COL_CODE_NAME, UOM_COL_UOMTYPE_NAME};

    public static final String[] uomConversionTableColNames = {GEN_COL_ROWID_NAME, GEN_COL_NAME_NAME, GEN_COL_ISACTIVE_NAME, GEN_COL_USER_COMMENT_NAME,
        UOM_CONVERSION_COL_UOMFROM_ID_NAME, UOM_CONVERSION_COL_UOMTO_ID_NAME, UOM_CONVERSION_COL_RATE_NAME};

    public static final String[] expenseTypeTableColNames = {GEN_COL_ROWID_NAME, GEN_COL_NAME_NAME, GEN_COL_ISACTIVE_NAME, GEN_COL_USER_COMMENT_NAME};

    public static final String[] mileageTableColNames = {GEN_COL_ROWID_NAME, GEN_COL_NAME_NAME, GEN_COL_ISACTIVE_NAME, GEN_COL_USER_COMMENT_NAME,
        MILEAGE_COL_DATE_NAME, MILEAGE_COL_CAR_ID_NAME, MILEAGE_COL_DRIVER_ID_NAME,
        MILEAGE_COL_INDEXSTART_NAME, MILEAGE_COL_INDEXSTOP_NAME, MILEAGE_COL_UOMLENGTH_ID_NAME,
        MILEAGE_COL_EXPENSETYPE_ID_NAME, MILEAGE_COL_GPSTRACKLOG_NAME};

    public static final String[] currencyTableColNames = {GEN_COL_ROWID_NAME, GEN_COL_NAME_NAME, GEN_COL_ISACTIVE_NAME, GEN_COL_USER_COMMENT_NAME,
        CURRENCY_COL_CODE_NAME};

    public static final String[] refuelTableColNames = {GEN_COL_ROWID_NAME, GEN_COL_NAME_NAME, GEN_COL_ISACTIVE_NAME, GEN_COL_USER_COMMENT_NAME,
        REFUEL_COL_CAR_ID_NAME, REFUEL_COL_DRIVER_ID_NAME, REFUEL_COL_EXPENSETYPE_ID_NAME, REFUEL_COL_INDEX_NAME,
        REFUEL_COL_QUANTITY_NAME, REFUEL_COL_UOMVOLUME_ID_NAME, REFUEL_COL_PRICE_NAME,
        REFUEL_COL_CURRENCY_ID_NAME, REFUEL_COL_DATE_NAME, REFUEL_COL_DOCUMENTNO_NAME, REFUEL_COL_EXPENSECATEGORY_NAME,
        REFUEL_COL_ISFULLREFUEL_NAME, REFUEL_COL_QUANTITYENTERED_NAME, REFUEL_COL_UOMVOLUMEENTERED_ID_NAME, 
        REFUEL_COL_PRICEENTERED_NAME, REFUEL_COL_CURRENCYENTERED_ID_NAME, REFUEL_COL_CURRENCYRATE_NAME, REFUEL_COL_UOMVOLCONVERSIONRATE_NAME};

    public static final String[] expenseCategoryTableColNames = {GEN_COL_ROWID_NAME, GEN_COL_NAME_NAME, GEN_COL_ISACTIVE_NAME, GEN_COL_USER_COMMENT_NAME,
        EXPENSECATEGORY_COL_ISEXCLUDEFROMMILEAGECOST_NAME};

    public static final String[] expensesTableColNames = {GEN_COL_ROWID_NAME, GEN_COL_NAME_NAME, GEN_COL_ISACTIVE_NAME, GEN_COL_USER_COMMENT_NAME,
        EXPENSES_COL_CAR_ID_NAME, EXPENSES_COL_DRIVER_ID_NAME, EXPENSES_COL_EXPENSECATEGORY_ID_NAME,
        EXPENSES_COL_EXPENSETYPE_ID_NAME, EXPENSES_COL_AMOUNT_NAME, EXPENSES_COL_CURRENCY_ID_NAME,
        EXPENSES_COL_DATE_NAME, EXPENSES_COL_DOCUMENTNO_NAME, EXPENSES_COL_INDEX_NAME,
        EXPENSES_COL_FROMTABLE_NAME, EXPENSES_COL_FROMRECORD_ID_NAME, 
        EXPENSES_COL_AMOUNTENTERED_NAME, EXPENSES_COL_CURRENCYENTERED_ID_NAME, EXPENSES_COL_CURRENCYRATE_NAME};

    public static final String[] currencyRateTableColNames = {GEN_COL_ROWID_NAME, GEN_COL_NAME_NAME, GEN_COL_ISACTIVE_NAME, GEN_COL_USER_COMMENT_NAME,
        CURRENCYRATE_COL_FROMCURRENCY_ID_NAME, CURRENCYRATE_COL_TOCURRENCY_ID_NAME,
        CURRENCYRATE_COL_RATE_NAME, CURRENCYRATE_COL_INVERSERATE_NAME};

    public static final String[] gpsTrackTableColNames = {GEN_COL_ROWID_NAME, GEN_COL_NAME_NAME, GEN_COL_ISACTIVE_NAME, GEN_COL_USER_COMMENT_NAME,
        GPSTRACK_COL_CAR_ID_NAME, GPSTRACK_COL_DRIVER_ID_NAME, GPSTRACK_COL_MILEAGE_ID_NAME, GPSTRACK_COL_DATE_NAME};
    public static final String[] gpsTrackDetailTableColNames = {GEN_COL_ROWID_NAME, GEN_COL_NAME_NAME, GEN_COL_ISACTIVE_NAME, GEN_COL_USER_COMMENT_NAME,
        GPSTRACKDETAIL_COL_GPSTRACK_ID_NAME};
//        , GPSTRACKDETAIL_COL_ACCURACY_NAME, GPSTRACKDETAIL_COL_ALTITUDE_NAME,
//        GPSTRACKDETAIL_COL_LATITUDE_NAME, GPSTRACKDETAIL_COL_LONGITUDE_NAME, GPSTRACKDETAIL_COL_SPEED_NAME,
//        GPSTRACKDETAIL_COL_TIME_NAME, GPSTRACKDETAIL_COL_DISTNACE_NAME, GPSTRACKDETAIL_COL_BEARING_NAME};

    public static final String[] genColName = {GEN_COL_ROWID_NAME, GEN_COL_NAME_NAME};
    public static final String[] genColRowId = {GEN_COL_ROWID_NAME};
    public static final String isActiveCondition = " " + GEN_COL_ISACTIVE_NAME + "='Y' ";
    public static final String isActiveWithAndCondition = " AND" + isActiveCondition + " ";
    /**
     * Database creation sql statements
     */
    protected static final String DRIVERS_TABLE_CREATE_SQL =
            "CREATE TABLE " + DRIVER_TABLE_NAME
            + " ( "
            + GEN_COL_ROWID_NAME + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + GEN_COL_NAME_NAME + " TEXT NOT NULL, "
            + GEN_COL_ISACTIVE_NAME + " TEXT DEFAULT 'Y', "
            + GEN_COL_USER_COMMENT_NAME + " TEXT NULL, "
            + DRIVER_COL_LICENSE_NO_NAME + " TEXT NULL "
            + ");";
    protected static final String CAR_TABLE_CREATE_SQL =
            "CREATE TABLE " + CAR_TABLE_NAME
            + " ( "
            + GEN_COL_ROWID_NAME + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + GEN_COL_NAME_NAME + " TEXT NOT NULL, "
            + GEN_COL_ISACTIVE_NAME + " TEXT DEFAULT 'Y', "
            + GEN_COL_USER_COMMENT_NAME + " TEXT NULL, "
            + CAR_COL_MODEL_NAME + " TEXT NULL, "
            + CAR_COL_REGISTRATIONNO_NAME + " TEXT NULL, "
            + CAR_COL_INDEXSTART_NAME + " NUMERIC, "
            + CAR_COL_INDEXCURRENT_NAME + " NUMERIC, "
            + CAR_COL_UOMLENGTH_ID_NAME + " INTEGER, "
            + CAR_COL_UOMVOLUME_ID_NAME + " INTEGER, "
            + CAR_COL_CURRENCY_ID_NAME + " INTEGER "
            + ");";
    protected static final String UOM_TABLE_CREATE_SQL =
            "CREATE TABLE " + UOM_TABLE_NAME
            + " ( "
            + GEN_COL_ROWID_NAME + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + GEN_COL_NAME_NAME + " TEXT NOT NULL, "
            + GEN_COL_ISACTIVE_NAME + " TEXT DEFAULT 'Y', "
            + GEN_COL_USER_COMMENT_NAME + " TEXT NULL, "
            + UOM_COL_CODE_NAME + " TEXT NOT NULL, "
            + UOM_COL_UOMTYPE_NAME + " TEXT NOT NULL "
            + ");";
    protected static final String UOM_CONVERSION_TABLE_CREATE_SQL =
            "CREATE TABLE " + UOM_CONVERSION_TABLE_NAME
            + " ( "
            + GEN_COL_ROWID_NAME + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + GEN_COL_NAME_NAME + " TEXT NOT NULL, "
            + GEN_COL_ISACTIVE_NAME + " TEXT DEFAULT 'Y', "
            + GEN_COL_USER_COMMENT_NAME + " TEXT NULL, "
            + UOM_CONVERSION_COL_UOMFROM_ID_NAME + " INTEGER NOT NULL, "
            + UOM_CONVERSION_COL_UOMTO_ID_NAME + " INTEGER NOT NULL, "
            + UOM_CONVERSION_COL_RATE_NAME + " NUMERIC NOT NULL "
            + ");";
    protected static final String EXPENSETYPE_TABLE_CREATE_SQL =
            "CREATE TABLE " + EXPENSETYPE_TABLE_NAME
            + " ( "
            + GEN_COL_ROWID_NAME + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + GEN_COL_NAME_NAME + " TEXT NOT NULL, "
            + GEN_COL_ISACTIVE_NAME + " TEXT DEFAULT 'Y', "
            + GEN_COL_USER_COMMENT_NAME + " TEXT NULL "
            + ");";
    protected static final String MILEAGE_TABLE_CREATE_SQL =
            "CREATE TABLE " + MILEAGE_TABLE_NAME
            + " ( "
            + GEN_COL_ROWID_NAME + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + GEN_COL_NAME_NAME + " TEXT NULL, "
            + GEN_COL_ISACTIVE_NAME + " TEXT DEFAULT 'Y', "
            + GEN_COL_USER_COMMENT_NAME + " TEXT NULL, "
            + MILEAGE_COL_DATE_NAME + " DATE NOT NULL, "
            + MILEAGE_COL_CAR_ID_NAME + " INTEGER NOT NULL, "
            + MILEAGE_COL_DRIVER_ID_NAME + " INTEGER NOT NULL, "
            + MILEAGE_COL_INDEXSTART_NAME + " NUMERIC NOT NULL, "
            + MILEAGE_COL_INDEXSTOP_NAME + " NUMERIC NOT NULL, "
            + MILEAGE_COL_UOMLENGTH_ID_NAME + " INTEGER NOT NULL, "
            + MILEAGE_COL_EXPENSETYPE_ID_NAME + " INTEGER NOT NULL, "
            + MILEAGE_COL_GPSTRACKLOG_NAME + " TEXT NULL "
            + ");";
    protected static final String CURRENCY_TABLE_CREATE_SQL =
            "CREATE TABLE " + CURRENCY_TABLE_NAME
            + " ( "
            + GEN_COL_ROWID_NAME + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + GEN_COL_NAME_NAME + " TEXT NOT NULL, "
            + GEN_COL_ISACTIVE_NAME + " TEXT DEFAULT 'Y', "
            + GEN_COL_USER_COMMENT_NAME + " TEXT NULL, "
            + CURRENCY_COL_CODE_NAME + " TEXT NOT NULL "
            + ");";
    protected static final String REFUEL_TABLE_CREATE_SQL =
            "CREATE TABLE " + REFUEL_TABLE_NAME
            + " ( "
            + GEN_COL_ROWID_NAME + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + GEN_COL_NAME_NAME + " TEXT NULL, "
            + GEN_COL_ISACTIVE_NAME + " TEXT DEFAULT 'Y', "
            + GEN_COL_USER_COMMENT_NAME + " TEXT NULL, "
            + REFUEL_COL_CAR_ID_NAME + " INTEGER, "
            + REFUEL_COL_DRIVER_ID_NAME + " INTEGER, "
            + REFUEL_COL_EXPENSETYPE_ID_NAME + " INTEGER, "
            + REFUEL_COL_INDEX_NAME + " NUMERIC, "
            + REFUEL_COL_QUANTITY_NAME + " NUMERIC, "
            + REFUEL_COL_UOMVOLUME_ID_NAME + " INTEGER, "
            + REFUEL_COL_PRICE_NAME + " NUMERIC, "
            + REFUEL_COL_CURRENCY_ID_NAME + " INTEGER, "
            + REFUEL_COL_DATE_NAME + " DATE NULL, "
            + REFUEL_COL_DOCUMENTNO_NAME + " TEXT NULL, "
            + REFUEL_COL_EXPENSECATEGORY_NAME + " INTEGER, "
            + REFUEL_COL_ISFULLREFUEL_NAME + " TEXT DEFAULT 'N', "
            + REFUEL_COL_QUANTITYENTERED_NAME + " NUMERIC NULL, "
            + REFUEL_COL_UOMVOLUMEENTERED_ID_NAME + " INTEGER NULL, "
            + REFUEL_COL_PRICEENTERED_NAME  + " NUMERIC NULL, "
            + REFUEL_COL_CURRENCYENTERED_ID_NAME + " INTEGER NULL, "
            + REFUEL_COL_CURRENCYRATE_NAME + " NUMERIC NULL, "
            + REFUEL_COL_UOMVOLCONVERSIONRATE_NAME + " NUMERIC NULL "
            + ");";

    protected static final String EXPENSECATEGORY_TABLE_CREATE_SQL =
            "CREATE TABLE " + EXPENSECATEGORY_TABLE_NAME
            + " ( "
            + GEN_COL_ROWID_NAME + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + GEN_COL_NAME_NAME + " TEXT NOT NULL, "
            + GEN_COL_ISACTIVE_NAME + " TEXT DEFAULT 'Y', "
            + GEN_COL_USER_COMMENT_NAME + " TEXT NULL, "
            + EXPENSECATEGORY_COL_ISEXCLUDEFROMMILEAGECOST_NAME + " TEXT DEFAULT 'N' "
            + ");";
    protected static final String EXPENSES_TABLE_CREATE_SQL =
            "CREATE TABLE " + EXPENSES_TABLE_NAME
            + " ( "
            + GEN_COL_ROWID_NAME + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + GEN_COL_NAME_NAME + " TEXT NOT NULL, "
            + GEN_COL_ISACTIVE_NAME + " TEXT DEFAULT 'Y', "
            + GEN_COL_USER_COMMENT_NAME + " TEXT NULL, "
            + EXPENSES_COL_CAR_ID_NAME + " INTEGER, "
            + EXPENSES_COL_DRIVER_ID_NAME + " INTEGER, "
            + EXPENSES_COL_EXPENSECATEGORY_ID_NAME + " INTEGER, "
            + EXPENSES_COL_EXPENSETYPE_ID_NAME + " INTEGER, "
            + EXPENSES_COL_AMOUNT_NAME + " NUMERIC, "
            + EXPENSES_COL_CURRENCY_ID_NAME + " INTEGER, "
            + EXPENSES_COL_DATE_NAME + " DATE NULL, "
            + EXPENSES_COL_DOCUMENTNO_NAME + " TEXT NULL, "
            + EXPENSES_COL_INDEX_NAME + " NUMERIC, "
            + EXPENSES_COL_FROMTABLE_NAME + " TEXT NULL, "
            + EXPENSES_COL_FROMRECORD_ID_NAME + " INTEGER, "
            + EXPENSES_COL_AMOUNTENTERED_NAME + " NUMERIC NULL, "
            + EXPENSES_COL_CURRENCYENTERED_ID_NAME + " INTEGER NULL, "
            + EXPENSES_COL_CURRENCYRATE_NAME + " NUMERIC NULL "
            + ");";

    protected static final String CURRENCYRATE_TABLE_CREATE_SQL =
            "CREATE TABLE " + CURRENCYRATE_TABLE_NAME
            + " ( "
            + GEN_COL_ROWID_NAME + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + GEN_COL_NAME_NAME + " TEXT, "
            + GEN_COL_ISACTIVE_NAME + " TEXT DEFAULT 'Y', "
            + GEN_COL_USER_COMMENT_NAME + " TEXT NULL, "
            + CURRENCYRATE_COL_FROMCURRENCY_ID_NAME + " INTEGER, "
            + CURRENCYRATE_COL_TOCURRENCY_ID_NAME + " INTEGER, "
            + CURRENCYRATE_COL_RATE_NAME + " NUMERIC, "
            + CURRENCYRATE_COL_INVERSERATE_NAME + " NUMERIC "
            + ");";

    protected static final String GPSTRACK_TABLE_CREATE_SQL =
            "CREATE TABLE " + GPSTRACK_TABLE_NAME
            + " ( "
            + GEN_COL_ROWID_NAME + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + GEN_COL_NAME_NAME + " TEXT NULL, "
            + GEN_COL_ISACTIVE_NAME + " TEXT DEFAULT 'Y', "
            + GEN_COL_USER_COMMENT_NAME + " TEXT NULL, "
            + GPSTRACK_COL_CAR_ID_NAME + " INTEGER NULL, "
            + GPSTRACK_COL_DRIVER_ID_NAME + " INTEGER NULL, "
            + GPSTRACK_COL_MILEAGE_ID_NAME + " INTEGER NULL, "
            + GPSTRACK_COL_DATE_NAME + " DATE NULL "
            + ");";

    protected static final String GPSTRACKDETAIL_TABLE_CREATE_SQL =
            "CREATE TABLE " + GPSTRACKDETAIL_TABLE_NAME
            + " ( "
            + GEN_COL_ROWID_NAME + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + GEN_COL_NAME_NAME + " TEXT NULL, "
            + GEN_COL_ISACTIVE_NAME + " TEXT DEFAULT 'Y', "
            + GEN_COL_USER_COMMENT_NAME + " TEXT NULL, "
            + GPSTRACKDETAIL_COL_GPSTRACK_ID_NAME + " INTEGER NOT NULL, "
            + GPSTRACKDETAIL_COL_FILEFORMAT_NAME + " TEXT NULL "
//            + GPSTRACKDETAIL_COL_ACCURACY_NAME + " NUMERIC NULL, "
//            + GPSTRACKDETAIL_COL_ALTITUDE_NAME + " NUMERIC NULL, "
//            + GPSTRACKDETAIL_COL_LATITUDE_NAME + " NUMERIC NULL, "
//            + GPSTRACKDETAIL_COL_LONGITUDE_NAME + " NUMERIC NULL, "
//            + GPSTRACKDETAIL_COL_SPEED_NAME + " NUMERIC NULL, "
//            + GPSTRACKDETAIL_COL_TIME_NAME + " DATE NULL, "
//            + GPSTRACKDETAIL_COL_DISTNACE_NAME + " NUMERIC NULL, "
//            + GPSTRACKDETAIL_COL_BEARING_NAME + " NUMERIC NULL "
            + ");";

    protected DatabaseHelper mDbHelper = null;
    protected SQLiteDatabase mDb = null;
    protected final Context mCtx;

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     *
     * @param ctx the Context within which to work
     */
    public DB(Context ctx) {
        this.mCtx = ctx;
        if(mDb == null) {
            open();
        }
    }

    /**
     * Open the notes database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     *
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public DB open() throws SQLException {
        if(mDbHelper == null) {
            mDbHelper = new DatabaseHelper(mCtx);
            if(mDb == null || !mDb.isOpen())
                mDb = mDbHelper.getWritableDatabase();
        }
        return this;
    }

    public void close() {
        mDbHelper.close();
        mDbHelper = null;
        mDb.close();
        mDb = null;
    }

    public static String sqlConcatTableColumn(String tableName, String columnName){
        return tableName + "." + columnName;
    }


    protected class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, StaticValues.DATABASE_NAME, null, StaticValues.DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                //create drivers table
                db.execSQL(DRIVERS_TABLE_CREATE_SQL);
                //create cars table
                db.execSQL(CAR_TABLE_CREATE_SQL);
                createUOMTable(db);

                //create uom conversions table
                createUOMConversionTable(db);

                createExpenseTypeTable(db);

                createMileageTable(db);

                //create & init currencies
                createCurrencyTable(db);
                createRefuelTable(db);
                createExpenseCategory(db);
                //expenses table
                createExpenses(db, false);
                //currency rate
                createCurrencyRateTable(db);

                //create the report folder on SDCARD
                FileUtils fu = new FileUtils();
                if(fu.onCreate(mCtx) != -1) {
                    Log.e(TAG, fu.lastError);
                }

            }
            catch(SQLException ex) {
                Log.e(TAG, ex.getMessage());
            }

        }

        private void createUOMTable(SQLiteDatabase db) throws SQLException {
            //create uom table
            db.execSQL(UOM_TABLE_CREATE_SQL);
            //init uom's
            String colPart = "INSERT INTO " + UOM_TABLE_NAME + " ( " + GEN_COL_NAME_NAME + ", " + GEN_COL_ISACTIVE_NAME + ", " + GEN_COL_USER_COMMENT_NAME + ", " + UOM_COL_CODE_NAME + ", " + UOM_COL_UOMTYPE_NAME + ") ";
            db.execSQL(colPart + "VALUES ( 'Kilometer', 'Y', 'Kilometer', 'km', 'L' )"); //_id = 1
            db.execSQL(colPart + "VALUES ( 'Mile', 'Y', 'Mile', 'mi', 'L' )"); //_id = 2 1609,344 m
            db.execSQL(colPart + "VALUES ( 'Liter', 'Y', 'Liter', 'l', 'V' )"); //_id = 3
            db.execSQL(colPart + "VALUES ( 'US gallon', 'Y', 'U.S. liquid gallon', 'gal US', 'V' )"); //_id = 4 3,785 411 784 l
            db.execSQL(colPart + "VALUES ( 'Imperial gallon', 'Y', 'Imperial (UK) gallon', 'gal GB', 'V' )"); //_id = 5 4,546 09 l
        }

        private void createUOMConversionTable(SQLiteDatabase db) throws SQLException {
            db.execSQL(UOM_CONVERSION_TABLE_CREATE_SQL);
            //init default uom conversions
            String colPart = "INSERT INTO " + UOM_CONVERSION_TABLE_NAME + " ( " + GEN_COL_NAME_NAME + ", " + GEN_COL_ISACTIVE_NAME + ", " + GEN_COL_USER_COMMENT_NAME + ", " + UOM_CONVERSION_COL_UOMFROM_ID_NAME + ", " + UOM_CONVERSION_COL_UOMTO_ID_NAME + ", " + UOM_CONVERSION_COL_RATE_NAME + " " + ") ";
            db.execSQL(colPart + "VALUES ( 'mi to km', 'Y', 'Mile to Kilometer', 2, 1, 1.609344 )");
            db.execSQL(colPart + "VALUES ( 'km to mi', 'Y', 'Kilometer to Mile', 1, 2, 0.621371 )");
            db.execSQL(colPart + "VALUES ( 'US gal to l', 'Y', 'US gallon to Liter', 4, 3, 3.785412 )");
            db.execSQL(colPart + "VALUES ( 'l to US gal', 'Y', 'Liter to US gallon', 3, 4, 0.264172 )");
            db.execSQL(colPart + "VALUES ( 'GB gal to l', 'Y', 'Imperial gallon to Liter', 5, 3, 4.54609 )");
            db.execSQL(colPart + "VALUES ( 'l to GB gal', 'Y', 'Liter to Imperial gallon', 3, 5, 0.219969 )");
            db.execSQL(colPart + "VALUES ( 'GB gal to US gal', 'Y', 'Imperial gallon to US gallon', 5, 4, 1.200950 )");
            db.execSQL(colPart + "VALUES ( 'US gal to GB gal', 'Y', 'US gallon to Imperial gallon', 4, 5, 0.832674 )");
        }

        private void createExpenseTypeTable(SQLiteDatabase db) throws SQLException {
            //create expense types table
            db.execSQL(EXPENSETYPE_TABLE_CREATE_SQL);
            //init some standard expenses
            String colPart = "INSERT INTO " + EXPENSETYPE_TABLE_NAME + " ( " + GEN_COL_NAME_NAME + ", " + GEN_COL_ISACTIVE_NAME + ", " + GEN_COL_USER_COMMENT_NAME + " " + ") ";
            db.execSQL(colPart + "VALUES ( 'Personal', 'Y', 'Expenses suported by you' )");
            db.execSQL(colPart + "VALUES ( 'Employer', 'Y', 'Expenses suported by your employer' )");
        }

        private void createMileageTable(SQLiteDatabase db) throws SQLException {
            //create mileage table
            db.execSQL(MILEAGE_TABLE_CREATE_SQL);
            //create indexes on mileage table
            db.execSQL("CREATE INDEX " + MILEAGE_TABLE_NAME + "_IX1 " + "ON " + MILEAGE_TABLE_NAME + " (" + MILEAGE_COL_CAR_ID_NAME + ")");
            db.execSQL("CREATE INDEX " + MILEAGE_TABLE_NAME + "_IX2 " + "ON " + MILEAGE_TABLE_NAME + " (" + MILEAGE_COL_DRIVER_ID_NAME + ")");
            db.execSQL("CREATE INDEX " + MILEAGE_TABLE_NAME + "_IX3 " + "ON " + MILEAGE_TABLE_NAME + " (" + MILEAGE_COL_DATE_NAME + " DESC )");
            db.execSQL("CREATE INDEX " + MILEAGE_TABLE_NAME + "_IX4 " + "ON " + MILEAGE_TABLE_NAME + " (" + MILEAGE_COL_INDEXSTOP_NAME + " DESC )");
            db.execSQL("CREATE INDEX " + MILEAGE_TABLE_NAME + "_IX5 " + "ON " + MILEAGE_TABLE_NAME + " (" + GEN_COL_USER_COMMENT_NAME + ")");
        }

        private void createRefuelTable(SQLiteDatabase db) throws SQLException {
            db.execSQL(REFUEL_TABLE_CREATE_SQL);
            db.execSQL("CREATE INDEX " + REFUEL_TABLE_NAME + "_IX1 " + "ON " + REFUEL_TABLE_NAME + " (" + MILEAGE_COL_DATE_NAME + " DESC )");
            db.execSQL("CREATE INDEX " + REFUEL_TABLE_NAME + "_IX2 " + "ON " + REFUEL_TABLE_NAME + " (" + GEN_COL_USER_COMMENT_NAME + ")");
            db.execSQL("CREATE INDEX " + REFUEL_TABLE_NAME + "_IX3 " + "ON " + REFUEL_TABLE_NAME + " (" + REFUEL_COL_ISFULLREFUEL_NAME + ")");
            db.execSQL("CREATE INDEX " + REFUEL_TABLE_NAME + "_IX4 " + "ON " + REFUEL_TABLE_NAME + " (" + REFUEL_COL_INDEX_NAME + ")");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            //AndiCar 1.0.0
            if(oldVersion == 1) {
                upgradeDbTo200(db); //update database to version 200 //AndiCar 2.0.0
                upradeDbTo210(db, oldVersion); //update database to version 210 //AndiCar 2.1.0
                upgradeDbTo220(db, oldVersion);
            }
            //AndiCar 2.0.x
            else if(oldVersion == 200){
                upradeDbTo210(db, oldVersion); //update database to version 210 //AndiCar 2.1.0
                upgradeDbTo220(db, oldVersion);
            }
            //AndiCar 2.1.x
            else if(oldVersion == 210){
                upgradeDbTo220(db, oldVersion); //update database to version 210 //AndiCar 2.2.0
            }
            upgradeDbTo220(db, oldVersion);
        }

        private void upgradeDbTo200(SQLiteDatabase db) throws SQLException {
            createExpenseCategory(db);
            String updateSql = "ALTER TABLE " + REFUEL_TABLE_NAME
                    + " ADD " + REFUEL_COL_EXPENSECATEGORY_NAME + " INTEGER";
            db.execSQL(updateSql);
            updateSql = "UPDATE " + REFUEL_TABLE_NAME
                    + " SET " + REFUEL_COL_EXPENSECATEGORY_NAME + " = 1";
            db.execSQL(updateSql);

            db.execSQL("ALTER TABLE " + REFUEL_TABLE_NAME + " ADD " + REFUEL_COL_ISFULLREFUEL_NAME + " TEXT DEFAULT 'N' ");
            db.execSQL("CREATE INDEX " + REFUEL_TABLE_NAME + "_IX3 " + "ON " + REFUEL_TABLE_NAME + " (" + REFUEL_COL_ISFULLREFUEL_NAME + ")");
            db.execSQL("CREATE INDEX " + REFUEL_TABLE_NAME + "_IX4 " + "ON " + REFUEL_TABLE_NAME + " (" + REFUEL_COL_INDEX_NAME + ")");
            createExpenses(db, true);
        }

        private void upradeDbTo210(SQLiteDatabase db, int oldVersion) throws SQLException {
            String updSql = "";

            createCurrencyRateTable(db);
            updSql = "ALTER TABLE " + REFUEL_TABLE_NAME + " ADD " + REFUEL_COL_QUANTITYENTERED_NAME + " NUMERIC NULL ";
            db.execSQL(updSql);
            updSql = "ALTER TABLE " + REFUEL_TABLE_NAME + " ADD " + REFUEL_COL_UOMVOLUMEENTERED_ID_NAME + " INTEGER NULL ";
            db.execSQL(updSql);
            updSql = "ALTER TABLE " + REFUEL_TABLE_NAME + " ADD " + REFUEL_COL_PRICEENTERED_NAME  + " NUMERIC NULL ";
            db.execSQL(updSql);
            updSql = "ALTER TABLE " + REFUEL_TABLE_NAME + " ADD " + REFUEL_COL_CURRENCYENTERED_ID_NAME + " INTEGER NULL ";
            db.execSQL(updSql);
            updSql = "ALTER TABLE " + REFUEL_TABLE_NAME + " ADD " + REFUEL_COL_CURRENCYRATE_NAME + " NUMERIC NULL ";
            db.execSQL(updSql);
            updSql = "ALTER TABLE " + REFUEL_TABLE_NAME + " ADD " + REFUEL_COL_UOMVOLCONVERSIONRATE_NAME + " NUMERIC NULL ";
            db.execSQL(updSql);

            updSql = "UPDATE " + REFUEL_TABLE_NAME +
                        " SET " +
                            REFUEL_COL_QUANTITYENTERED_NAME + " = " + REFUEL_COL_QUANTITY_NAME + ", " +
                            REFUEL_COL_UOMVOLUMEENTERED_ID_NAME + " = " + REFUEL_COL_UOMVOLUME_ID_NAME + ", " +
                            REFUEL_COL_PRICEENTERED_NAME + " = " + REFUEL_COL_PRICE_NAME + ", " +
                            REFUEL_COL_CURRENCYENTERED_ID_NAME + " = " + REFUEL_COL_CURRENCY_ID_NAME + ", " +
                            REFUEL_COL_CURRENCYRATE_NAME + " = 1, " +
                            REFUEL_COL_UOMVOLCONVERSIONRATE_NAME + " = 1 ";
            db.execSQL(updSql);

            updSql = "UPDATE " + REFUEL_TABLE_NAME +
                        " SET " +
                            REFUEL_COL_UOMVOLUME_ID_NAME + " = " +
                                "(SELECT " + CAR_COL_UOMVOLUME_ID_NAME + " " +
                                "FROM " + CAR_TABLE_NAME + " " +
                                "WHERE " + GEN_COL_ROWID_NAME + " = " +
                                        sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_CAR_ID_NAME) + ") ";
            db.execSQL(updSql);

            updSql = "UPDATE " + REFUEL_TABLE_NAME +
                        " SET " +
                            REFUEL_COL_UOMVOLCONVERSIONRATE_NAME + " = " +
                                "(SELECT " + UOM_CONVERSION_COL_RATE_NAME + " " +
                                "FROM " + UOM_CONVERSION_TABLE_NAME + " " +
                                "WHERE " + UOM_CONVERSION_COL_UOMFROM_ID_NAME + " = " +
                                                sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_UOMVOLUMEENTERED_ID_NAME) + " " +
                                        "AND " + UOM_CONVERSION_COL_UOMTO_ID_NAME + " = " +
                                                sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_UOMVOLUME_ID_NAME) + "), " +
                            REFUEL_COL_QUANTITY_NAME + " = " +
                                        "ROUND( " + REFUEL_COL_QUANTITYENTERED_NAME + " * " +
                                            "(SELECT " + UOM_CONVERSION_COL_RATE_NAME + " " +
                                            "FROM " + UOM_CONVERSION_TABLE_NAME + " " +
                                            "WHERE " + UOM_CONVERSION_COL_UOMFROM_ID_NAME + " = " +
                                                            sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_UOMVOLUMEENTERED_ID_NAME) + " " +
                                                    "AND " + UOM_CONVERSION_COL_UOMTO_ID_NAME + " = " +
                                                            sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_UOMVOLUME_ID_NAME) + "), 2 ) " +
                      "WHERE " + REFUEL_COL_UOMVOLUME_ID_NAME + " <> " + REFUEL_COL_UOMVOLUMEENTERED_ID_NAME;
            db.execSQL(updSql);

            updSql = "UPDATE " + REFUEL_TABLE_NAME +
                        " SET " +
                            REFUEL_COL_CURRENCY_ID_NAME + " = " +
                                "(SELECT " + CAR_COL_CURRENCY_ID_NAME +
                                " FROM " + CAR_TABLE_NAME +
                                " WHERE " + sqlConcatTableColumn(CAR_TABLE_NAME, GEN_COL_ROWID_NAME) + " = " +
                                        sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_CAR_ID_NAME) +
                            ") ";
            db.execSQL(updSql);

            Cursor checkCursor = db.rawQuery("SELECT COUNT(*) " +
                                                "FROM " + REFUEL_TABLE_NAME + " " +
                                                "WHERE " + REFUEL_COL_CURRENCY_ID_NAME + " <> " + REFUEL_COL_CURRENCYENTERED_ID_NAME, null);
            if(checkCursor.moveToFirst() && checkCursor.getInt(0) > 0){
                SharedPreferences mPreferences = mCtx.getSharedPreferences(StaticValues.GLOBAL_PREFERENCE_NAME, 0);
                SharedPreferences.Editor editor = mPreferences.edit();
                editor.putString("UpdateMsg", "During the upgrade process we found foreign currencies in refuels.\n" +
                                                "Please review and correct the currency conversion rates in your refuels.");
                editor.commit();
            }
            checkCursor.close();

            if(oldVersion == 200){
                updSql = "ALTER TABLE " + EXPENSES_TABLE_NAME + " ADD " + EXPENSES_COL_AMOUNTENTERED_NAME + " NUMERIC NULL ";
                db.execSQL(updSql);

                updSql = "ALTER TABLE " + EXPENSES_TABLE_NAME + " ADD " + EXPENSES_COL_CURRENCYENTERED_ID_NAME + " INTEGER NULL ";
                db.execSQL(updSql);

                updSql = "ALTER TABLE " + EXPENSES_TABLE_NAME + " ADD " + EXPENSES_COL_CURRENCYRATE_NAME + " NUMERIC NULL ";
                db.execSQL(updSql);
            }

            updSql = "UPDATE " + EXPENSES_TABLE_NAME +
                        " SET " +
                            EXPENSES_COL_AMOUNTENTERED_NAME + " = " + EXPENSES_COL_AMOUNT_NAME + ", " +
                            EXPENSES_COL_CURRENCYENTERED_ID_NAME + " = " + EXPENSES_COL_CURRENCY_ID_NAME + ", " +
                            EXPENSES_COL_CURRENCYRATE_NAME + " = 1";
            db.execSQL(updSql);

            updSql = "UPDATE " + EXPENSES_TABLE_NAME +
                        " SET " +
                            EXPENSES_COL_CURRENCY_ID_NAME + " = " +
                                "(SELECT " + CAR_COL_CURRENCY_ID_NAME +
                                " FROM " + CAR_TABLE_NAME +
                                " WHERE " + sqlConcatTableColumn(CAR_TABLE_NAME, GEN_COL_ROWID_NAME) + " = " +
                                        sqlConcatTableColumn(EXPENSES_TABLE_NAME, EXPENSES_COL_CAR_ID_NAME) +
                            ") ";
            db.execSQL(updSql);

            checkCursor = db.rawQuery("SELECT COUNT(*) " +
                                                "FROM " + EXPENSES_TABLE_NAME + " " +
                                                "WHERE " + EXPENSES_COL_CURRENCY_ID_NAME + " <> " + EXPENSES_COL_CURRENCYENTERED_ID_NAME + " " +
                                                        "AND COALESCE(" + EXPENSES_COL_FROMTABLE_NAME + ", 'X') <> '" + StaticValues.EXPENSES_COL_FROMREFUEL_TABLE_NAME + "'", null);
            if(checkCursor.moveToFirst() && checkCursor.getInt(0) > 0){
                SharedPreferences mPreferences = mCtx.getSharedPreferences(StaticValues.GLOBAL_PREFERENCE_NAME, 0);
                String updateMsg = mPreferences.getString("UpdateMsg", null);
                if(updateMsg != null)
                    updateMsg = "During the upgrade process we found foreign currencies in refuels and expenses.\n" +
                                                "Please review and correct the currency conversion rates in your refuels and expenses.";
                else
                    updateMsg = "During the upgrade process we found foreign currencies in expenses.\n" +
                                                "Please review and correct the currency conversion rates in your expenses.";

                SharedPreferences.Editor editor = mPreferences.edit();
                editor.putString("UpdateMsg", updateMsg);
                editor.commit();
            }
            checkCursor.close();
        }

        private void upgradeDbTo220(SQLiteDatabase db, int oldVersion) throws SQLException {
            //gps track
//            db.execSQL(GPSTRACK_TABLE_CREATE_SQL);
            String tmpStr = "DROP TABLE " + GPSTRACKDETAIL_TABLE_NAME;
            db.execSQL(tmpStr);
            db.execSQL(GPSTRACKDETAIL_TABLE_CREATE_SQL);
            FileUtils fu = new FileUtils();
            fu.updateTo220(mCtx);
        }

        private void createExpenseCategory(SQLiteDatabase db) throws SQLException {
            //expense category
            db.execSQL(EXPENSECATEGORY_TABLE_CREATE_SQL);
            String colPart = "INSERT INTO " + EXPENSECATEGORY_TABLE_NAME
                    + " ( "
                    + GEN_COL_NAME_NAME + ", "
                    + GEN_COL_ISACTIVE_NAME + ", "
                    + GEN_COL_USER_COMMENT_NAME + ", "
                    + EXPENSECATEGORY_COL_ISEXCLUDEFROMMILEAGECOST_NAME + " "
                    + ") ";
            db.execSQL(colPart + "VALUES ( 'Fuel', 'Y', 'Expenses with refuels', 'N' )");
            db.execSQL(colPart + "VALUES ( 'Service', 'Y', 'Expenses with services', 'N' )");
            db.execSQL(colPart + "VALUES ( 'Insurance', 'Y', 'Expenses with insurance', 'N' )");
        }

        private void createExpenses(SQLiteDatabase db, boolean isUpdate) throws SQLException {
            //expenses table
            db.execSQL(EXPENSES_TABLE_CREATE_SQL);
            if(!isUpdate) {
                return;
            }
            //initialize refuel expenses
            String sql = "INSERT INTO " + EXPENSES_TABLE_NAME
                    + "( "
                    + GEN_COL_NAME_NAME + ", "
                    + GEN_COL_USER_COMMENT_NAME + ", "
                    + GEN_COL_ISACTIVE_NAME + ", "
                    + EXPENSES_COL_CAR_ID_NAME + ", "
                    + EXPENSES_COL_DRIVER_ID_NAME + ", "
                    + EXPENSES_COL_EXPENSECATEGORY_ID_NAME + ", "
                    + EXPENSES_COL_EXPENSETYPE_ID_NAME + ", "
                    + EXPENSES_COL_AMOUNT_NAME + ", "
                    + EXPENSES_COL_CURRENCY_ID_NAME + ", "
                    + EXPENSES_COL_DATE_NAME + ", "
                    + EXPENSES_COL_DOCUMENTNO_NAME + ", "
                    + EXPENSES_COL_INDEX_NAME + ", "
                    + EXPENSES_COL_FROMTABLE_NAME + ", "
                    + EXPENSES_COL_FROMRECORD_ID_NAME + " "
                    + ") "
                    + "SELECT "
                    + GEN_COL_NAME_NAME + ", "
                    + GEN_COL_USER_COMMENT_NAME + ", "
                    + GEN_COL_ISACTIVE_NAME + ", "
                    + REFUEL_COL_CAR_ID_NAME + ", "
                    + REFUEL_COL_DRIVER_ID_NAME + ", "
                    + REFUEL_COL_EXPENSECATEGORY_NAME + ", "
                    + REFUEL_COL_EXPENSETYPE_ID_NAME + ", "
                    + REFUEL_COL_QUANTITY_NAME + " * " + REFUEL_COL_PRICE_NAME + ", "
                    + REFUEL_COL_CURRENCY_ID_NAME + ", "
                    + REFUEL_COL_DATE_NAME + ", "
                    + REFUEL_COL_DOCUMENTNO_NAME + ", "
                    + REFUEL_COL_INDEX_NAME + ", "
                    + "'Refuel' " + ", "
                    + GEN_COL_ROWID_NAME + " "
                    + "FROM " + REFUEL_TABLE_NAME;
            db.execSQL(sql);
        }

        private void createCurrencyTable(SQLiteDatabase db) throws SQLException {
            //currency table name
            db.execSQL(CURRENCY_TABLE_CREATE_SQL);
            //insert some currencies
            String colPart = "INSERT INTO " + CURRENCY_TABLE_NAME
                    + " ( "
                    + GEN_COL_NAME_NAME + ", "
                    + GEN_COL_ISACTIVE_NAME + ", "
                    + GEN_COL_USER_COMMENT_NAME + ", "
                    + CURRENCY_COL_CODE_NAME
                    + ") ";
            db.execSQL(colPart + "VALUES ( 'Euro', 'Y', 'Euro', 'EUR' )");
            db.execSQL(colPart + "VALUES ( 'US Dollar', 'Y', 'US Dollar', 'USD' )");
            db.execSQL(colPart + "VALUES ( 'Hungary Forint', 'Y', 'Hungary Forint', 'HUF' )");
            db.execSQL(colPart + "VALUES ( 'Romania (new) Lei', 'Y', 'Romania (new) Lei', 'RON' )");
            db.execSQL(colPart + "VALUES ( 'Australia Dollar', 'Y', 'Australia Dollar', 'AUD' )");
            db.execSQL(colPart + "VALUES ( 'Canada Dollar', 'Y', 'Canada Dollar', 'CAD' )");
            db.execSQL(colPart + "VALUES ( 'Switzerland Francs', 'Y', 'Switzerland Francs', 'CHF' )");
            db.execSQL(colPart + "VALUES ( 'China Yuan', 'Y', 'China Yuan', 'CNY' )");
            db.execSQL(colPart + "VALUES ( 'United Kingdom Pounds', 'Y', 'United Kingdom Pounds', 'GBP' )");
            db.execSQL(colPart + "VALUES ( 'Japan Yen', 'Y', 'Japan Yen', 'JPY' )");
            db.execSQL(colPart + "VALUES ( 'Russia Ruble', 'Y', 'Russia Ruble', 'RUB' )");
        }

        private void createCurrencyRateTable(SQLiteDatabase db) throws SQLException {
            //create currency rate table
            db.execSQL(CURRENCYRATE_TABLE_CREATE_SQL);
        }
    }

    public boolean backupDb(String bkName) {
        boolean retVal;
        String fromFile = mDb.getPath();
        String toFile = StaticValues.backupFolder;
        if(bkName == null)
            toFile = toFile + Utils.appendDateTime(StaticValues.backupPrefix, true, true, true)
                + StaticValues.backupSufix;
        else
            toFile = toFile + bkName + StaticValues.backupSufix;
        
        mDb.close();
        FileUtils fu = new FileUtils();
        retVal = fu.copyFile(mCtx, fromFile, toFile, false);
        if(retVal == false) {
            lastErrorMessage = fu.lastError;
        }
        open();
        return retVal;
    }

    public boolean restoreDb(String restoreFile) {
        boolean retVal;
        String toFile = mDb.getPath();
        mDb.close();
        FileUtils fu = new FileUtils();
        retVal = fu.copyFile(mCtx, StaticValues.backupFolder + restoreFile, toFile, true);
        if(retVal == false) {
            lastErrorMessage = fu.lastError;
        }
        return retVal;
    }
}
