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
package org.andicar.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.UrlQuerySanitizer.ValueSanitizer;
import android.util.Log;
import java.util.ArrayList;
import org.andicar.utils.Constants;

public class MainDbAdapter
{
    public String lastErrorMessage = null;
    public Exception lasteException;
    private static final String TAG = "MainDbAdapter";
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
    public static final int REFUEL_COL_EXPENSETYPE_ID_POS =6;
    public static final int REFUEL_COL_INDEX_POS = 7;
    public static final int REFUEL_COL_QUANTITY_POS = 8;
    public static final int REFUEL_COL_UOMVOLUME_ID_POS = 9;
    public static final int REFUEL_COL_PRICE_POS = 10;
    public static final int REFUEL_COL_CURRENCY_ID_POS = 11;
    public static final int REFUEL_COL_DATE_POS = 12;
    public static final int REFUEL_COL_DOCUMENTNO_POS = 13;

    public static final String[] driverTableColNames =
        {GEN_COL_ROWID_NAME, GEN_COL_NAME_NAME, GEN_COL_ISACTIVE_NAME, GEN_COL_USER_COMMENT_NAME,
            DRIVER_COL_LICENSE_NO_NAME};
    public static final String[] carTableColNames =
        {GEN_COL_ROWID_NAME, GEN_COL_NAME_NAME, GEN_COL_ISACTIVE_NAME, GEN_COL_USER_COMMENT_NAME,
            CAR_COL_MODEL_NAME, CAR_COL_REGISTRATIONNO_NAME, CAR_COL_INDEXSTART_NAME, CAR_COL_INDEXCURRENT_NAME,
            CAR_COL_UOMLENGTH_ID_NAME, CAR_COL_UOMVOLUME_ID_NAME, CAR_COL_CURRENCY_ID_NAME};
    public static final String[] uomTableColNames =
        {GEN_COL_ROWID_NAME, GEN_COL_NAME_NAME, GEN_COL_ISACTIVE_NAME, GEN_COL_USER_COMMENT_NAME,
            UOM_COL_CODE_NAME, UOM_COL_UOMTYPE_NAME};
    public static final String[] uomConversionTableColNames =
        {GEN_COL_ROWID_NAME, GEN_COL_NAME_NAME, GEN_COL_ISACTIVE_NAME, GEN_COL_USER_COMMENT_NAME,
            UOM_CONVERSION_COL_UOMFROM_ID_NAME, UOM_CONVERSION_COL_UOMTO_ID_NAME, UOM_CONVERSION_COL_RATE_NAME};
    public static final String[] expenseTableColNames =
        {GEN_COL_ROWID_NAME, GEN_COL_NAME_NAME, GEN_COL_ISACTIVE_NAME, GEN_COL_USER_COMMENT_NAME};
    public static final String[] mileageTableColNames =
        {GEN_COL_ROWID_NAME, GEN_COL_NAME_NAME, GEN_COL_ISACTIVE_NAME, GEN_COL_USER_COMMENT_NAME,
            MILEAGE_COL_DATE_NAME, MILEAGE_COL_CAR_ID_NAME, MILEAGE_COL_DRIVER_ID_NAME,
            MILEAGE_COL_INDEXSTART_NAME, MILEAGE_COL_INDEXSTOP_NAME, MILEAGE_COL_UOMLENGTH_ID_NAME,
            MILEAGE_COL_EXPENSETYPE_ID_NAME, MILEAGE_COL_GPSTRACKLOG_NAME};
    public static final String[] currencyTableColNames =
        {GEN_COL_ROWID_NAME, GEN_COL_NAME_NAME, GEN_COL_ISACTIVE_NAME, GEN_COL_USER_COMMENT_NAME,
            CURRENCY_COL_CODE_NAME};
    public static final String[] refuelTableColNames =
        {GEN_COL_ROWID_NAME, GEN_COL_NAME_NAME, GEN_COL_ISACTIVE_NAME, GEN_COL_USER_COMMENT_NAME,
            REFUEL_COL_CAR_ID_NAME, REFUEL_COL_DRIVER_ID_NAME, REFUEL_COL_EXPENSETYPE_ID_NAME, REFUEL_COL_INDEX_NAME,
            REFUEL_COL_QUANTITY_NAME, REFUEL_COL_UOMVOLUME_ID_NAME, REFUEL_COL_PRICE_NAME, REFUEL_COL_CURRENCY_ID_NAME, REFUEL_COL_DATE_NAME, REFUEL_COL_DOCUMENTNO_NAME};

    public static final String[] genColName = {GEN_COL_ROWID_NAME, GEN_COL_NAME_NAME};
    public static final String[] genColRowId = {GEN_COL_ROWID_NAME};
    public static final String isActiveCondition = " " + GEN_COL_ISACTIVE_NAME + "='Y' ";
    public static final String isActiveWithAndCondition = " AND" + isActiveCondition + " ";

    private DatabaseHelper mDbHelper = null;
    protected SQLiteDatabase mDb = null;

    protected final Context mCtx;

    /**
     * Database creation sql statements
     */
    private static final String DRIVERS_TABLE_CREATE_SQL =
            "CREATE TABLE " + DRIVER_TABLE_NAME
            + " ( "
            + GEN_COL_ROWID_NAME + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + GEN_COL_NAME_NAME + " TEXT NOT NULL, "
            + GEN_COL_ISACTIVE_NAME + " TEXT DEFAULT 'Y', "
            + GEN_COL_USER_COMMENT_NAME + " TEXT NULL, "
            + DRIVER_COL_LICENSE_NO_NAME + " TEXT NULL "
            + ");";
    private static final String CAR_TABLE_CREATE_SQL =
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
    private static final String UOM_TABLE_CREATE_SQL =
            "CREATE TABLE " + UOM_TABLE_NAME
            + " ( "
            + GEN_COL_ROWID_NAME + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + GEN_COL_NAME_NAME + " TEXT NOT NULL, "
            + GEN_COL_ISACTIVE_NAME + " TEXT DEFAULT 'Y', "
            + GEN_COL_USER_COMMENT_NAME + " TEXT NULL, "
            + UOM_COL_CODE_NAME + " TEXT NOT NULL, "
            + UOM_COL_UOMTYPE_NAME + " TEXT NOT NULL "
            + ");";
    private static final String UOM_CONVERSION_TABLE_CREATE_SQL =
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
    private static final String EXPENSE_TABLE_CREATE_SQL =
            "CREATE TABLE " + EXPENSETYPE_TABLE_NAME
            + " ( "
            + GEN_COL_ROWID_NAME + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + GEN_COL_NAME_NAME + " TEXT NOT NULL, "
            + GEN_COL_ISACTIVE_NAME + " TEXT DEFAULT 'Y', "
            + GEN_COL_USER_COMMENT_NAME + " TEXT NULL "
            + ");";
    private static final String MILEAGE_TABLE_CREATE_SQL =
            "CREATE TABLE " + MILEAGE_TABLE_NAME
            + " ( "
            + GEN_COL_ROWID_NAME + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + GEN_COL_NAME_NAME + " TEXT NOT NULL, "
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

    private static final String CURRENCY_TABLE_CREATE_SQL =
            "CREATE TABLE " + CURRENCY_TABLE_NAME
            + " ( "
            + GEN_COL_ROWID_NAME + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + GEN_COL_NAME_NAME + " TEXT NOT NULL, "
            + GEN_COL_ISACTIVE_NAME + " TEXT DEFAULT 'Y', "
            + GEN_COL_USER_COMMENT_NAME + " TEXT NULL, "
            + CURRENCY_COL_CODE_NAME + " TEXT NOT NULL "
            + ");";

    private static final String REFUEL_TABLE_CREATE_SQL =
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
            + REFUEL_COL_DATE_NAME  + " DATE NULL, "
            + REFUEL_COL_DOCUMENTNO_NAME + " TEXT NULL "
            + ");";

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     *
     * @param ctx the Context within which to work
     */
    public MainDbAdapter( Context ctx )
    {
        this.mCtx = ctx;
        if(mDb == null)
            open();
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
    public MainDbAdapter open() throws SQLException
    {
        mDbHelper = new DatabaseHelper( mCtx );
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close()
    {
        mDbHelper.close();
    }


    private static class DatabaseHelper extends SQLiteOpenHelper
    {
        DatabaseHelper( Context context )
        {
            super( context, Constants.DATABASE_NAME, null, Constants.DATABASE_VERSION );
        }

        @Override
        public void onCreate( SQLiteDatabase db )
        {
           //used for insert (columns part)
            String colPart = "";
            try{
                //create drivers table
                db.execSQL( DRIVERS_TABLE_CREATE_SQL );
                //create cars table
                db.execSQL( CAR_TABLE_CREATE_SQL );
                //create uom table
                db.execSQL( UOM_TABLE_CREATE_SQL );
                //init uom's
                colPart = "INSERT INTO " + UOM_TABLE_NAME +
                            " ( " +
                                GEN_COL_NAME_NAME + ", " +
                                GEN_COL_ISACTIVE_NAME + ", " +
                                GEN_COL_USER_COMMENT_NAME + ", " +
                                UOM_COL_CODE_NAME + ", " +
                                UOM_COL_UOMTYPE_NAME +
                             ") ";
                db.execSQL( colPart +
                        "VALUES ( 'Kilometer', 'Y', 'Kilometer', 'km', 'L' )" ); //_id = 1
                db.execSQL( colPart +
                        "VALUES ( 'Mile', 'Y', 'Mile', 'mi', 'L' )" ); //_id = 2 1609,344 m
                db.execSQL( colPart +
                        "VALUES ( 'Liter', 'Y', 'Liter', 'l', 'V' )" ); //_id = 3
                db.execSQL( colPart +
                        "VALUES ( 'US gallon', 'Y', 'U.S. liquid gallon', 'gal US', 'V' )" ); //_id = 4 3,785 411 784 l
                db.execSQL( colPart +
                        "VALUES ( 'Imperial gallon', 'Y', 'Imperial (UK) gallon', 'gal GB', 'V' )" ); //_id = 5 4,546 09 l
                //create uom conversions table
                db.execSQL( UOM_CONVERSION_TABLE_CREATE_SQL);
                //init default uom conversions
                colPart = "INSERT INTO " + UOM_CONVERSION_TABLE_NAME +
                            " ( " +
                                GEN_COL_NAME_NAME + ", " +
                                GEN_COL_ISACTIVE_NAME + ", " +
                                GEN_COL_USER_COMMENT_NAME + ", " +
                                UOM_CONVERSION_COL_UOMFROM_ID_NAME + ", " +
                                UOM_CONVERSION_COL_UOMTO_ID_NAME + ", " +
                                UOM_CONVERSION_COL_RATE_NAME + " " +
                             ") ";
                db.execSQL( colPart +
                        "VALUES ( 'mi to km', 'Y', 'Mile to Kilometer', 2, 1, 1.609344 )" );
                db.execSQL( colPart +
                        "VALUES ( 'km to mi', 'Y', 'Kilometer to Mile', 1, 2, 0.621371 )" );
                db.execSQL( colPart +
                        "VALUES ( 'US gal to l', 'Y', 'US gallon to Liter', 4, 3, 3.785412 )" );
                db.execSQL( colPart +
                        "VALUES ( 'l to US gal', 'Y', 'Liter to US gallon', 3, 4, 0.264172 )" );
                db.execSQL( colPart +
                        "VALUES ( 'GB gal to l', 'Y', 'Imperial gallon to Liter', 5, 3, 4.54609 )" );
                db.execSQL( colPart +
                        "VALUES ( 'l to GB gal', 'Y', 'Liter to Imperial gallon', 3, 5, 0.219969 )" );
                db.execSQL( colPart +
                        "VALUES ( 'GB gal to US gal', 'Y', 'Imperial gallon to US gallon', 5, 4, 1.200950 )" );
                db.execSQL( colPart +
                        "VALUES ( 'US gal to GB gal', 'Y', 'US gallon to Imperial gallon', 4, 5, 0.832674 )" );

                //create expense types table
                db.execSQL( EXPENSE_TABLE_CREATE_SQL );
                //init some standard expenses
                colPart = "INSERT INTO " + EXPENSETYPE_TABLE_NAME +
                            " ( " +
                                GEN_COL_NAME_NAME + ", " +
                                GEN_COL_ISACTIVE_NAME + ", " +
                                GEN_COL_USER_COMMENT_NAME + " " +
                             ") ";
                db.execSQL( colPart +
                        "VALUES ( 'Personal', 'Y', 'Expenses suported by you' )" );
                db.execSQL( colPart +
                        "VALUES ( 'Employer', 'Y', 'Expenses suported by your employer' )" );

                //create mileage table
                db.execSQL( MILEAGE_TABLE_CREATE_SQL );
                //create indexes on mileage table
                db.execSQL("CREATE INDEX " + MILEAGE_TABLE_NAME + "_IX1 " +
                                "ON " + MILEAGE_TABLE_NAME + " (" + MILEAGE_COL_CAR_ID_NAME + ")");
                db.execSQL("CREATE INDEX " + MILEAGE_TABLE_NAME + "_IX2 " +
                                "ON " + MILEAGE_TABLE_NAME + " (" + MILEAGE_COL_DRIVER_ID_NAME + ")");
                db.execSQL("CREATE INDEX " + MILEAGE_TABLE_NAME + "_IX3 " +
                                "ON " + MILEAGE_TABLE_NAME + " (" + MILEAGE_COL_DATE_NAME + " DESC )");
                db.execSQL("CREATE INDEX " + MILEAGE_TABLE_NAME + "_IX4 " +
                                "ON " + MILEAGE_TABLE_NAME + " (" + MILEAGE_COL_INDEXSTOP_NAME + " DESC )");
                db.execSQL("CREATE INDEX " + MILEAGE_TABLE_NAME + "_IX5 " +
                                "ON " + MILEAGE_TABLE_NAME + " (" + GEN_COL_USER_COMMENT_NAME + ")");

                //create & init currencies
                createCurrencyTable( db );

                //refuel table
                db.execSQL(REFUEL_TABLE_CREATE_SQL);
                db.execSQL("CREATE INDEX " + REFUEL_TABLE_NAME + "_IX1 " +
                                "ON " + REFUEL_TABLE_NAME + " (" + MILEAGE_COL_DATE_NAME + " DESC )");
                db.execSQL("CREATE INDEX " + REFUEL_TABLE_NAME + "_IX2 " +
                                "ON " + REFUEL_TABLE_NAME + " (" + GEN_COL_USER_COMMENT_NAME + ")");

            }
            catch(SQLException ex){
                Log.e(TAG, ex.getMessage());
            }

        }

        @Override
        public void onUpgrade( SQLiteDatabase db, int oldVersion, int newVersion )
        {
//            db.execSQL("ALTER TABLE " + CAR_TABLE_NAME + " ADD " + CAR_COL_CURRENCY_ID_NAME + " INTEGER NULL ");
//            db.execSQL(REFUEL_TABLE_CREATE_SQL);
        }

        private void createCurrencyTable(SQLiteDatabase db) throws SQLException {
            //currency table name
            db.execSQL(CURRENCY_TABLE_CREATE_SQL);
            //insert some currencies
            String colPart = "INSERT INTO " + CURRENCY_TABLE_NAME +
                        " ( " +
                            GEN_COL_NAME_NAME + ", " +
                            GEN_COL_ISACTIVE_NAME + ", " +
                            GEN_COL_USER_COMMENT_NAME + ", " +
                            CURRENCY_COL_CODE_NAME +
                         ") ";
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

    }

    public Cursor fetchForTable(String tableName, String[] columns, String whereCondition, String orderByColumn){
        return mDb.query( tableName, columns, whereCondition, null, null, null, orderByColumn );
    }

    /**
     * Return a Cursor positioned at the record that matches the given rowId from the given table
     *
     * @param rowId id of the record to retrieve
     * @return Cursor positioned to matching record, if found
     * @throws SQLException if the record could not be found/retrieved
     */
    public Cursor fetchRecord( String tableName, String[] columns, long rowId ) throws SQLException
    {
        Cursor mCursor =
                mDb.query( true, tableName, columns,
                            GEN_COL_ROWID_NAME + "=" + rowId, null, null, null, null, null );
        if( mCursor != null ) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    public long createRecord(String tableName, ContentValues content){
        long retVal;
        try{
            retVal = mDb.insertOrThrow(tableName, tableName, content);
        }
        catch(SQLException e){
            lastErrorMessage = e.getMessage();
            lasteException = e;
            return -1;
        }
        return retVal;
    }

    public boolean updateRecord( String tableName, long rowId, ContentValues content)
    {
            return mDb.update( tableName, content, GEN_COL_ROWID_NAME + "=" + rowId, null ) > 0;
    }

    public String deleteRecord( String tableName, long rowId )
    {
        //todo check deletion prerequests (data validations)
        if(mDb.delete(tableName, GEN_COL_ROWID_NAME + "=" + rowId, null ) > 0)
            return null;

        return "";
    }

    public String canInsertUpdateUOMConversion( Long rowId, long fromId, long toId )
    {
        //chek for duplicates
        String sql = "SELECT * " +
                        " FROM " + MainDbAdapter.UOM_CONVERSION_TABLE_NAME +
                        " WHERE " + MainDbAdapter.UOM_CONVERSION_COL_UOMFROM_ID_NAME + " = " + fromId +
                            " AND " + MainDbAdapter.UOM_CONVERSION_COL_UOMTO_ID_NAME + " = " + toId;
        if(rowId != null)
            sql = sql + " AND " + MainDbAdapter.GEN_COL_ROWID_NAME + " <> " + rowId.toString();
        Cursor resultCursor = mDb.rawQuery( sql, null );
        if(resultCursor.getCount() > 0)
        {
            return "ERR_005";
        }

        return null;
    }

    //mileage specific
    /**
     * Create a new car.
     * If the record is successfully created return the new rowId for that record, otherwise return
     * a -1 to indicate failure.
     *
     * @param mName (not used)
     * @param mIsActive the user is active or not
     * @param mUserComment an arbitrary comment/helper text
     * @param mDateTime the date of the stop index
     * @param mCarId the id of the car
     * @param mDriverId the id of the driver
     * @param mStartIndex start index
     * @param mStopIndex stop index
     * @param mUOMLengthId uom for length (used for uom conversion in reports)
     * @param mExpTypeId expense type id
     * @param mGpsTrackLog gps track log data (not used yet)
     * @return null or the error message
     */
    public String createMileage( String mName, String mIsActive, String mUserComment ,
            long mDateTime, long mCarId, long mDriverId,
            float mStartIndex, float mStopIndex, long mUOMLengthId,
            long mExpTypeId, String mGpsTrackLog)
    {
        String retVal = null;
        retVal = checkIndex(-1, mCarId, mStartIndex, mStopIndex);
        if(retVal != null)
            return retVal;

        Long mileageId = new Long(-1);
        ContentValues data = new ContentValues();
        data.put( GEN_COL_NAME_NAME, mName );
        data.put( GEN_COL_ISACTIVE_NAME, mIsActive );
        data.put( GEN_COL_USER_COMMENT_NAME, mUserComment );
        data.put(MILEAGE_COL_DATE_NAME, mDateTime);
        data.put(MILEAGE_COL_CAR_ID_NAME, mCarId);
        data.put(MILEAGE_COL_DRIVER_ID_NAME, mDriverId);
        data.put(MILEAGE_COL_INDEXSTART_NAME, mStartIndex);
        data.put(MILEAGE_COL_INDEXSTOP_NAME, mStopIndex);
        data.put(MILEAGE_COL_UOMLENGTH_ID_NAME, mUOMLengthId);
        data.put(MILEAGE_COL_EXPENSETYPE_ID_NAME, mExpTypeId);
        data.put(MILEAGE_COL_GPSTRACKLOG_NAME, mGpsTrackLog);
        try{
            Float carCurrentIndex = fetchRecord(CAR_TABLE_NAME, carTableColNames, mCarId)
                .getFloat(CAR_COL_INDEXCURRENT_POS);

            mDb.beginTransaction();
            mileageId = mDb.insert( MILEAGE_TABLE_NAME, null, data );
            if(mileageId < 0)
                throw new SQLException("Mileage insert error");
            //update car curent index

            if(mStopIndex > carCurrentIndex)
            {
                data.clear();
                data.put(CAR_COL_INDEXCURRENT_NAME, mStopIndex);
                if(mDb.update( CAR_TABLE_NAME, data, GEN_COL_ROWID_NAME + "=" + mCarId, null ) == 0)
                    throw new SQLException("Car Update error");
            }
            mDb.setTransactionSuccessful();
        }catch(SQLException e){
            retVal = e.getMessage();
        }
        finally{
            mDb.endTransaction();
        }

        return retVal;

    }

    /**
     * Update the Mileage using the details provided. The Mileage to be updated is
     * specified using the rowId, and it is altered to use the values passed in
     *
     * @param rowId id of note to update
     * @param mName the name of the Mileage
     * @param mIsActive the Mileage is active or not
     * @param mUserComment user comment/help
     * @param mDateTime the date of the stop index
     * @param mCarId the id of the car
     * @param mDriverId the id of the driver
     * @param mStartIndex start index
     * @param mStopIndex stop index
     * @param mUOMLengthId uom for length (used for uom conversion in reports)
     * @param mExpTypeId expense type id
     * @param mGpsTrackLog gps track log data (not used yet)
     * @return true if the Mileage was successfully updated, false otherwise
     */
    public String updateMileage( long rowId, String mName, String mIsActive, String mUserComment ,
                    long mDateTime, long mCarId, long mDriverId,
                    float mStartIndex, float mStopIndex, long mUOMLengthId,
                    long mExpTypeId, String mGpsTrackLog)
    {
        String retVal = checkIndex(rowId, mCarId, mStartIndex, mStopIndex);
        if(retVal != null)
            return retVal;

        ContentValues data = new ContentValues();
        data.put( GEN_COL_NAME_NAME, mName );
        data.put( GEN_COL_ISACTIVE_NAME, mIsActive );
        data.put( GEN_COL_USER_COMMENT_NAME, mUserComment );
        data.put(MILEAGE_COL_DATE_NAME, mDateTime);
        data.put(MILEAGE_COL_CAR_ID_NAME, mCarId);
        data.put(MILEAGE_COL_DRIVER_ID_NAME, mDriverId);
        data.put(MILEAGE_COL_INDEXSTART_NAME, mStartIndex);
        data.put(MILEAGE_COL_INDEXSTOP_NAME, mStopIndex);
        data.put(MILEAGE_COL_UOMLENGTH_ID_NAME, mUOMLengthId);
        data.put(MILEAGE_COL_EXPENSETYPE_ID_NAME, mExpTypeId);
        data.put(MILEAGE_COL_GPSTRACKLOG_NAME, mGpsTrackLog);

        try{
            Float carCurrentIndex = fetchRecord(CAR_TABLE_NAME, carTableColNames, mCarId)
                .getFloat(CAR_COL_INDEXCURRENT_POS);
            mDb.beginTransaction();
            if(mDb.update( MILEAGE_TABLE_NAME, data, GEN_COL_ROWID_NAME + "=" + rowId, null ) == 0)
                throw new SQLException("Mileage insert error");
            //update the car curent index
            if(mStopIndex > carCurrentIndex)
            {
                data.clear();
                data.put(CAR_COL_INDEXCURRENT_NAME, mStopIndex);
                if(mDb.update( CAR_TABLE_NAME, data, GEN_COL_ROWID_NAME + "=" + mCarId, null ) == 0)
                    throw new SQLException("Car Update error");
            }
            mDb.setTransactionSuccessful();
        }catch(SQLException e){
            retVal = e.getMessage();
        }
        finally{
            mDb.endTransaction();
        }
        return retVal;
    }


    private String checkIndex(long rowId,long carId, float startIndex, float stopIndex){

        if(stopIndex <= startIndex)
            return Constants.errStopBeforeStartIndex;

        String checkSql = "";
        checkSql = "SELECT * " +
                    " FROM " + MILEAGE_TABLE_NAME +
                    " WHERE " + MILEAGE_COL_CAR_ID_NAME + "=" + carId +
                            " AND " + MILEAGE_COL_INDEXSTART_NAME + " <= " + startIndex +
                                " AND " + MILEAGE_COL_INDEXSTOP_NAME + " > " + startIndex;
        if (rowId >= 0)
            checkSql = checkSql + " AND " + GEN_COL_ROWID_NAME + "<>" + rowId;

        if(mDb.rawQuery(checkSql, null).getCount() > 0)
            return Constants.errStartIndexOverlap;
        checkSql = "SELECT * " +
                    " FROM " + MILEAGE_TABLE_NAME +
                    " WHERE " + MILEAGE_COL_CAR_ID_NAME + "=" + carId +
                            " AND " + MILEAGE_COL_INDEXSTART_NAME + " < " + stopIndex +
                                " AND " + MILEAGE_COL_INDEXSTOP_NAME + " >= " + stopIndex;
        if (rowId >= 0)
            checkSql = checkSql + " AND " + GEN_COL_ROWID_NAME + "<>" + rowId;
        if(mDb.rawQuery(checkSql, null).getCount() > 0)
            return Constants.errNewIndexOverlap;

        checkSql = "SELECT * " +
                    " FROM " + MILEAGE_TABLE_NAME +
                    " WHERE " + MILEAGE_COL_CAR_ID_NAME + "=" + carId +
                            " AND " + MILEAGE_COL_INDEXSTART_NAME + " >= " + startIndex +
                                " AND " + MILEAGE_COL_INDEXSTOP_NAME + " <= " + stopIndex;
        if (rowId >= 0)
            checkSql = checkSql + " AND " + GEN_COL_ROWID_NAME + "<>" + rowId;

        if(mDb.rawQuery(checkSql, null).getCount() > 0)
            return Constants.errMileageOverlap;

        return null;
    }

    public String[] getAutoCompleteMileageUserComments(long carId, long driverId){
        String[] retVal = null;
        ArrayList<String> commentList = new ArrayList<String>();
        String selectSql = "SELECT DISTINCT " + GEN_COL_USER_COMMENT_NAME +
                            " FROM " + MILEAGE_TABLE_NAME +
                            " WHERE " + MILEAGE_COL_CAR_ID_NAME + " = " + carId +
                                " AND " + MILEAGE_COL_DRIVER_ID_NAME  + " = " + driverId +
                                " AND " + GEN_COL_USER_COMMENT_NAME + " IS NOT NULL " +
                                " AND " + GEN_COL_USER_COMMENT_NAME + " <> '' " +
                            " ORDER BY " + MILEAGE_COL_INDEXSTOP_NAME + " DESC " +
                            " LIMIT 100";
        Cursor commentCursor = mDb.rawQuery(selectSql, null);
        while(commentCursor.moveToNext()){
            commentList.add(commentCursor.getString(0));
        }
        commentCursor.close();
        retVal = new String[commentList.size()];
        commentList.toArray(retVal);
        return retVal;
    }

    public String[] getAutoCompleteRefuelUserComments(long carId, long driverId){
        String[] retVal = null;
        ArrayList<String> commentList = new ArrayList<String>();
        String selectSql = "SELECT DISTINCT " + GEN_COL_USER_COMMENT_NAME +
                            " FROM " + REFUEL_TABLE_NAME +
                            " WHERE " + REFUEL_COL_CAR_ID_NAME + " = " + carId +
                                " AND " + REFUEL_COL_DRIVER_ID_NAME  + " = " + driverId +
                                " AND " + GEN_COL_USER_COMMENT_NAME + " IS NOT NULL " +
                                " AND " + GEN_COL_USER_COMMENT_NAME + " <> '' " +
                            " ORDER BY " + REFUEL_COL_DATE_NAME + " DESC " +
                            " LIMIT 100";
        Cursor commentCursor = mDb.rawQuery(selectSql, null);
        while(commentCursor.moveToNext()){
            commentList.add(commentCursor.getString(0));
        }
        commentCursor.close();
        retVal = new String[commentList.size()];
        commentList.toArray(retVal);
        return retVal;
    }
}
