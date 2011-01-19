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

import org.andicar.activity.R;
import org.andicar.utils.AndiCarExceptionHandler;
import org.andicar.utils.StaticValues;
import org.andicar.utils.Utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.andicar.addon.activity.ServiceSubscription;
import com.andicar.addon.services.FileMailer;
import com.andicar.addon.utils.AddOnDBObjectDef;
import com.andicar.addon.utils.AddOnStaticValues;

/**
 * Database object names and database creation/update
 * 
 * @author miki
 */
public class DB {
	public String lastErrorMessage = null;
	public Exception lasteException;
	protected DatabaseHelper mDbHelper = null;
	protected SQLiteDatabase mDb = null;
	protected final Context mCtx;
	protected String bkFolder = null;
	protected String bkFileName = null;

	protected static final String TAG = "MainDbAdapter";
	// drivers
	public static final String DRIVER_TABLE_NAME = "DEF_DRIVER";
	// cars
	public static final String CAR_TABLE_NAME = "DEF_CAR";
	// uoms
	public static final String UOM_TABLE_NAME = "DEF_UOM";
	// expense types
	public static final String EXPENSETYPE_TABLE_NAME = "DEF_EXPENSETYPE";
	// uom conversion rates
	public static final String UOM_CONVERSION_TABLE_NAME = "DEF_UOMCONVERTIONRATE";
	// currencies
	public static final String CURRENCY_TABLE_NAME = "DEF_CURRENCY";
	// mileages
	public static final String MILEAGE_TABLE_NAME = "CAR_MILEAGE";
	// refuel
	public static final String REFUEL_TABLE_NAME = "CAR_REFUEL";
	// expense categories (eg. Refuel, Service, Insurance, etc.
	public static final String EXPENSECATEGORY_TABLE_NAME = "DEF_EXPENSECATEGORY";
	// car expenses
	public static final String EXPENSE_TABLE_NAME = "CAR_EXPENSE";
	// currency rate
	public static final String CURRENCYRATE_TABLE_NAME = "DEF_CURRENCYRATE";
	// gps track table
	public static final String GPSTRACK_TABLE_NAME = "GPS_TRACK";
	public static final String GPSTRACKDETAIL_TABLE_NAME = "GPS_TRACKDETAIL";

	// business partner table
	public static final String BPARTNER_TABLE_NAME = "DEF_BPARTNER";
	// business partner locations table
	public static final String BPARTNER_LOCATION_TABLE_NAME = "DEF_BPARTNERLOCATION";

	// tags table
	public static final String TAG_TABLE_NAME = "DEF_TAG";

	// tasks/reminders/todo tables
	public static final String TASKTYPE_TABLE_NAME = "DEF_TASKTYPE";
	public static final String TASK_TABLE_NAME = "DEF_TASK";
	public static final String TASK_CAR_TABLE_NAME = "TASK_CAR";
	public static final String TODO_TABLE_NAME = "TASK_TODO";
	
	// column names. Some is general (GEN_) some is particular
	// generic columns must be first and must be created for ALL TABLES
	public static final String GEN_COL_ROWID_NAME = "_id";
	public static final String GEN_COL_NAME_NAME = "Name";
	public static final String GEN_COL_ISACTIVE_NAME = "IsActive";
	public static final String GEN_COL_USER_COMMENT_NAME = "UserComment";
	// driver specific column names
	public static final String DRIVER_COL_LICENSE_NO_NAME = "LicenseNo";
	// car specific column names
	public static final String CAR_COL_MODEL_NAME = "Model";
	public static final String CAR_COL_REGISTRATIONNO_NAME = "RegistrationNo";
	public static final String CAR_COL_INDEXSTART_NAME = "IndexStart";
	public static final String CAR_COL_INDEXCURRENT_NAME = "IndexCurrent";
	public static final String CAR_COL_UOMLENGTH_ID_NAME = UOM_TABLE_NAME
			+ "_Length_ID";
	public static final String CAR_COL_UOMVOLUME_ID_NAME = UOM_TABLE_NAME
			+ "_Volume_ID";
	public static final String CAR_COL_CURRENCY_ID_NAME = CURRENCY_TABLE_NAME
			+ "_ID";
	// uom specific column names
	public static final String UOM_COL_CODE_NAME = "Code";
	public static final String UOM_COL_UOMTYPE_NAME = "UOMType"; // V - Volume
																	// or L -
																	// Length
	// uom conversion specific column names
	public static final String UOM_CONVERSION_COL_UOMFROM_ID_NAME = UOM_TABLE_NAME
			+ "_From_ID";
	public static final String UOM_CONVERSION_COL_UOMTO_ID_NAME = UOM_TABLE_NAME
			+ "_To_ID";
	public static final String UOM_CONVERSION_COL_RATE_NAME = "ConvertionRate";
	// mileage specific columns
	public static final String MILEAGE_COL_DATE_NAME = "Date";
	public static final String MILEAGE_COL_CAR_ID_NAME = CAR_TABLE_NAME + "_ID";
	public static final String MILEAGE_COL_DRIVER_ID_NAME = DRIVER_TABLE_NAME
			+ "_ID";
	public static final String MILEAGE_COL_INDEXSTART_NAME = "IndexStart";
	public static final String MILEAGE_COL_INDEXSTOP_NAME = "IndexStop";
	public static final String MILEAGE_COL_UOMLENGTH_ID_NAME = UOM_TABLE_NAME
			+ "_Length_ID";
	public static final String MILEAGE_COL_EXPENSETYPE_ID_NAME = EXPENSETYPE_TABLE_NAME
			+ "_ID";
	public static final String MILEAGE_COL_GPSTRACKLOG_NAME = "GPSTrackLog";
	public static final String MILEAGE_COL_TAG_ID_NAME = TAG_TABLE_NAME + "_ID";
	// currencies
	public static final String CURRENCY_COL_CODE_NAME = "Code";
	// refuel
	public static final String REFUEL_COL_CAR_ID_NAME = CAR_TABLE_NAME + "_ID";
	public static final String REFUEL_COL_DRIVER_ID_NAME = DRIVER_TABLE_NAME
			+ "_ID";
	public static final String REFUEL_COL_EXPENSETYPE_ID_NAME = EXPENSETYPE_TABLE_NAME
			+ "_ID";
	public static final String REFUEL_COL_INDEX_NAME = "CarIndex";
	public static final String REFUEL_COL_QUANTITY_NAME = "Quantity";
	public static final String REFUEL_COL_UOMVOLUME_ID_NAME = UOM_TABLE_NAME
			+ "_Volume_ID";
	public static final String REFUEL_COL_PRICE_NAME = "Price";
	public static final String REFUEL_COL_CURRENCY_ID_NAME = CURRENCY_TABLE_NAME
			+ "_ID";
	public static final String REFUEL_COL_DATE_NAME = "Date";
	public static final String REFUEL_COL_DOCUMENTNO_NAME = "DocumentNo";
	public static final String REFUEL_COL_EXPENSECATEGORY_NAME = EXPENSECATEGORY_TABLE_NAME
			+ "_ID";
	public static final String REFUEL_COL_ISFULLREFUEL_NAME = "IsFullRefuel";
	public static final String REFUEL_COL_QUANTITYENTERED_NAME = "QuantityEntered";
	public static final String REFUEL_COL_UOMVOLUMEENTERED_ID_NAME = UOM_TABLE_NAME
			+ "_EnteredVolume_ID";
	public static final String REFUEL_COL_PRICEENTERED_NAME = "PriceEntered";
	public static final String REFUEL_COL_CURRENCYENTERED_ID_NAME = CURRENCY_TABLE_NAME
			+ "_Entered_ID";
	public static final String REFUEL_COL_CURRENCYRATE_NAME = "CurrencyRate"; // CurrencyEntered
																				// ->
																				// Car
																				// Base
																				// Currency
	public static final String REFUEL_COL_UOMVOLCONVERSIONRATE_NAME = "UOMVolumeConversionRate";
	public static final String REFUEL_COL_AMOUNT_NAME = "Amount";
	public static final String REFUEL_COL_AMOUNTENTERED_NAME = "AmountEntered";
	public static final String REFUEL_COL_BPARTNER_ID_NAME = BPARTNER_TABLE_NAME
			+ "_ID";
	public static final String REFUEL_COL_BPARTNER_LOCATION_ID_NAME = BPARTNER_LOCATION_TABLE_NAME
			+ "_ID";
	public static final String REFUEL_COL_TAG_ID_NAME = TAG_TABLE_NAME + "_ID";

	// expense category
	public static final String EXPENSECATEGORY_COL_ISEXCLUDEFROMMILEAGECOST_NAME = "IsExcludefromMileagecost";
	public static final String EXPENSECATEGORY_COL_ISFUEL_NAME = "IsFuel";
	// car expenses
	public static final String EXPENSE_COL_CAR_ID_NAME = CAR_TABLE_NAME + "_ID";
	public static final String EXPENSE_COL_DRIVER_ID_NAME = DRIVER_TABLE_NAME
			+ "_ID";
	public static final String EXPENSE_COL_EXPENSECATEGORY_ID_NAME = EXPENSECATEGORY_TABLE_NAME
			+ "_ID";
	public static final String EXPENSE_COL_EXPENSETYPE_ID_NAME = EXPENSETYPE_TABLE_NAME
			+ "_ID";
	public static final String EXPENSE_COL_AMOUNT_NAME = "Amount";
	public static final String EXPENSE_COL_CURRENCY_ID_NAME = CURRENCY_TABLE_NAME
			+ "_ID";
	public static final String EXPENSE_COL_DATE_NAME = "Date";
	public static final String EXPENSE_COL_DOCUMENTNO_NAME = "DocumentNo";
	public static final String EXPENSE_COL_INDEX_NAME = "CarIndex";
	public static final String EXPENSE_COL_FROMTABLE_NAME = "FromTable";
	public static final String EXPENSE_COL_FROMRECORD_ID_NAME = "FromRecordId";
	public static final String EXPENSE_COL_AMOUNTENTERED_NAME = "AmountEntered";
	public static final String EXPENSE_COL_CURRENCYENTERED_ID_NAME = CURRENCY_TABLE_NAME
			+ "_Entered_ID";
	public static final String EXPENSE_COL_CURRENCYRATE_NAME = "CurrencyRate"; // CurrencyEntered
																				// ->
																				// Car
																				// Base
																				// Currency
	public static final String EXPENSE_COL_QUANTITY_NAME = "Quantity";
	public static final String EXPENSE_COL_PRICE_NAME = "Price";
	public static final String EXPENSE_COL_PRICEENTERED_NAME = "PriceEntered";
	public static final String EXPENSE_COL_UOM_ID_NAME = UOM_TABLE_NAME + "_ID";
	public static final String EXPENSE_COL_BPARTNER_ID_NAME = BPARTNER_TABLE_NAME
			+ "_ID";
	public static final String EXPENSE_COL_BPARTNER_LOCATION_ID_NAME = BPARTNER_LOCATION_TABLE_NAME
			+ "_ID";
	public static final String EXPENSE_COL_TAG_ID_NAME = TAG_TABLE_NAME + "_ID";

	// currency rate
	public static final String CURRENCYRATE_COL_FROMCURRENCY_ID_NAME = CURRENCYRATE_TABLE_NAME
			+ "_From_ID";
	public static final String CURRENCYRATE_COL_TOCURRENCY_ID_NAME = CURRENCYRATE_TABLE_NAME
			+ "_To_ID";
	public static final String CURRENCYRATE_COL_RATE_NAME = "Rate";
	public static final String CURRENCYRATE_COL_INVERSERATE_NAME = "InverseRate";

	// gps track
	public static final String GPSTRACK_COL_CAR_ID_NAME = CAR_TABLE_NAME
			+ "_ID";
	public static final String GPSTRACK_COL_DRIVER_ID_NAME = DRIVER_TABLE_NAME
			+ "_ID";
	public static final String GPSTRACK_COL_MILEAGE_ID_NAME = MILEAGE_TABLE_NAME
			+ "_ID";
	public static final String GPSTRACK_COL_DATE_NAME = "Date";
	public static final String GPSTRACK_COL_MINACCURACY_NAME = "MinAccuracy";
	public static final String GPSTRACK_COL_AVGACCURACY_NAME = "AvgAccuracy";
	public static final String GPSTRACK_COL_MAXACCURACY_NAME = "MaxAccuracy";
	public static final String GPSTRACK_COL_MINALTITUDE_NAME = "MinAltitude";
	public static final String GPSTRACK_COL_MAXALTITUDE_NAME = "MaxAltitude";
	public static final String GPSTRACK_COL_TOTALTIME_NAME = "TotalTime"; // in
																			// seconds
	public static final String GPSTRACK_COL_MOVINGTIME_NAME = "MovingTime"; // in
																			// seconds
	public static final String GPSTRACK_COL_DISTANCE_NAME = "Distance";
	public static final String GPSTRACK_COL_MAXSPEED_NAME = "MaxSpeed";
	public static final String GPSTRACK_COL_AVGSPEED_NAME = "AvgSpeed";
	public static final String GPSTRACK_COL_AVGMOVINGSPEED_NAME = "AvgMovingSpeed";
	public static final String GPSTRACK_COL_TOTALTRACKPOINTS_NAME = "TotalTrackPoints";
	public static final String GPSTRACK_COL_INVALIDTRACKPOINTS_NAME = "InvalidTrackPoints";
	public static final String GPSTRACK_COL_TAG_ID_NAME = TAG_TABLE_NAME
			+ "_ID";

	// gps track detail
	public static final String GPSTRACKDETAIL_COL_GPSTRACK_ID_NAME = GPSTRACK_TABLE_NAME
			+ "_ID";
	public static final String GPSTRACKDETAIL_COL_FILE_NAME = "File";
	public static final String GPSTRACKDETAIL_COL_FILEFORMAT_NAME = "Format"; // see
																				// StaticValues.gpsTrackFormat...
	// business partner location
	public static final String BPARTNER_LOCATION_COL_BPARTNER_ID_NAME = BPARTNER_TABLE_NAME
			+ "_ID";
	public static final String BPARTNER_LOCATION_COL_ADDRESS_NAME = "Address";
	public static final String BPARTNER_LOCATION_COL_POSTAL_NAME = "Postal";
	public static final String BPARTNER_LOCATION_COL_CITY_NAME = "City";
	public static final String BPARTNER_LOCATION_COL_REGION_NAME = "Region";
	public static final String BPARTNER_LOCATION_COL_COUNTRY_NAME = "Country";
	public static final String BPARTNER_LOCATION_COL_PHONE_NAME = "Phone";
	public static final String BPARTNER_LOCATION_COL_PHONE2_NAME = "Phone2";
	public static final String BPARTNER_LOCATION_COL_FAX_NAME = "Fax";
	public static final String BPARTNER_LOCATION_COL_EMAIL_NAME = "Email";
	public static final String BPARTNER_LOCATION_COL_CONTACTPERSON_NAME = "ContactPerson";

	public static final String TASK_COL_TASKTYPE_ID_NAME = TASKTYPE_TABLE_NAME + "_ID";
	/**
	 * this task is a recurent task? {Y|N}
	 */
	public static final String TASK_COL_ISRECURENT_NAME = "IsRecurent";
	/**
	 * Time|Mileage|Both (StaticValues.TASK_SCHEDULED_FOR_...)
	 */
	public static final String TASK_COL_SCHEDULEDFOR_NAME = "ScheduledFor";
	public static final String TASK_COL_ISDIFFERENTSTARTINGTIME_NAME = "IsDifferentSTime";
	/**
	 * no of days/weeks/years
	 */
	public static final String TASK_COL_TIMEFREQUENCY_NAME = "TimeFrequency";
	/**
	 * Type integer<br>
	 * Frequency type: 0 = Week, 1 = Month, 2 = Year (StaticValues.TASK_SCHEDULED_FREQTYPE_...
	 */
	public static final String TASK_COL_TIMEFREQUENCYTYPE_NAME = "TimeFrequencyType";
	
	/**
	 * run on a specified day when IsDifferentSTime = 'N' 
	 * <li>if TimeFrequencyType is week: 0 Sunday - 6 Saturday <br>
	 * <li>if TimeFrequencyType is month: 1 - 32. 32 for last day of the month
	 * <li>if TimeFrequencyType is year: 1 - 31
	 */
	public static final String TASK_COL_RUNDAY_NAME = "RunDay";

	/**
	 * run on a specified month when IsDifferentSTime = 'N' and TimeFrequencyType is year <br>
	 *  <li> 0 = January
	 *  <li> ...
	 *  <li> 11 = December
	 */
	public static final String TASK_COL_RUNMONTH_NAME = "RunMonth";
	/**
	 * Type Date<br><br>
	 * If IsRecurent = 'Y':
	 * <li>1970-01-01 hh:mm. Just the hh:mm part are considered<br><br>
	 * If IsRecurent = 'N':
	 * <li>Datetime
	 */
	public static final String TASK_COL_RUNTIME_NAME = "RunTime";
	/**
	 * Type integer
	 * No. of days to start reminders
	 */
	public static final String TASK_COL_REMINDERDAYS_NAME = "ReminderDays";
	/**
	 * If IsRecurent = 'Y':
	 * <li>Run on every mileage<br>
	 * else
	 * <li>Run on mileage
	 */
	public static final String TASK_COL_RUNMILEAGE_NAME = "RunMileage";
	/**
	 * No. of km|mi to start reminders
	 */
	public static final String TASK_COL_REMINDERMILEAGES_NAME = "ReminderMileages";
	
	
	/**
	 * the task from where this todo come
	 */
	public static final String TODO_COL_TASK_ID_NAME = TASK_TABLE_NAME + "_ID";
	/**
	 * the linked car to the task (if exist) 
	 */
	public static final String TODO_COL_CAR_ID_NAME = CAR_TABLE_NAME + "_ID";
	/**
	 * the due date based on start time and recurency settings
	 */
	public static final String TODO_COL_DUEDATE_NAME = "DueDate";
	/**
	 * the due mileage based on starting mileage and recurency mileage
	 */
	public static final String TODO_COL_DUEMILAGE_NAME = "DueMilage";
	/**
	 * postpone the next reminder until this date 
	 */
	public static final String TODO_COL_POSTPONEUNTIL_NAME = "PostponeUntil";
	/**
	 * if this todo is done {Y|N}
	 */
	public static final String TODO_COL_ISDONE_NAME = "IsDone";
	/**
	 * the date when this todo was done
	 */
	public static final String TODO_COL_DONEDATE_NAME = "DoneDate";
	/**
	 * stop the notification for this todo, even if is not done
	 */
	public static final String TODO_COL_ISSTOPNOTIFICATION_NAME = "IsStopNotification";
	
	public static final String TASK_CAR_COL_TASK_ID_NAME = TASK_TABLE_NAME + "_ID";
	public static final String TASK_CAR_COL_CAR_ID_NAME = CAR_TABLE_NAME + "_ID";
	public static final String TASK_CAR_COL_FIRSTRUN_DATE_NAME = "FirstRunDate";
	public static final String TASK_CAR_COL_FIRSTRUN_MILEAGE_NAME = "FirstRunMileage";

	// column positions. Some is general (GEN_) some is particular
	// generic columns must be first and must be created for ALL TABLES
	public static final int GEN_COL_ROWID_POS = 0;
	public static final int GEN_COL_NAME_POS = 1;
	public static final int GEN_COL_ISACTIVE_POS = 2;
	public static final int GEN_COL_USER_COMMENT_POS = 3;
	// driver specidfic column positions
	public static final int DRIVER_COL_LICENSE_NO_POS = 4;
	// car specific column positions
	public static final int CAR_COL_MODEL_POS = 4;
	public static final int CAR_COL_REGISTRATIONNO_POS = 5;
	public static final int CAR_COL_INDEXSTART_POS = 6;
	public static final int CAR_COL_INDEXCURRENT_POS = 7;
	public static final int CAR_COL_UOMLENGTH_ID_POS = 8;
	public static final int CAR_COL_UOMVOLUME_ID_POS = 9;
	public static final int CAR_COL_CURRENCY_ID_POS = 10;
	// uom specific column positions
	public static final int UOM_COL_CODE_POS = 4;
	public static final int UOM_COL_UOMTYPE_POS = 5;
	// uom convertion specific column positions
	public static final int UOM_CONVERSION_COL_UOMFROM_ID_POS = 4;
	public static final int UOM_CONVERSION_COL_UOMTO_ID_POS = 5;
	public static final int UOM_CONVERSION_COL_RATE_POS = 6;
	// mileage specific column positions
	public static final int MILEAGE_COL_DATE_POS = 4;
	public static final int MILEAGE_COL_CAR_ID_POS = 5;
	public static final int MILEAGE_COL_DRIVER_ID_POS = 6;
	public static final int MILEAGE_COL_INDEXSTART_POS = 7;
	public static final int MILEAGE_COL_INDEXSTOP_POS = 8;
	public static final int MILEAGE_COL_UOMLENGTH_ID_POS = 9;
	public static final int MILEAGE_COL_EXPENSETYPE_ID_POS = 10;
	public static final int MILEAGE_COL_GPSTRACKLOG_POS = 11;
	public static final int MILEAGE_COL_TAG_ID_POS = 12;
	// currencies
	public static int CURRENCY_COL_CODE_POS = 4;
	// refuel
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
	public static final int REFUEL_COL_AMOUNT_POS = 22;
	public static final int REFUEL_COL_AMOUNTENTERED_POS = 23;
	public static final int REFUEL_COL_BPARTNER_ID_POS = 24;
	public static final int REFUEL_COL_BPARTNER_LOCATION_ID_POS = 25;
	public static final int REFUEL_COL_TAG_ID_POS = 26;

	// expense category
	public static final int EXPENSECATEGORY_COL_ISEXCLUDEFROMMILEAGECOST_POS = 4;
	public static final int EXPENSECATEGORY_COL_ISFUEL_POS = 5;
	// car expenses
	public static final int EXPENSE_COL_CAR_ID_POS = 4;
	public static final int EXPENSE_COL_DRIVER_ID_POS = 5;
	public static final int EXPENSE_COL_EXPENSECATEGORY_POS = 6;
	public static final int EXPENSE_COL_EXPENSETYPE_ID_POS = 7;
	public static final int EXPENSE_COL_AMOUNT_POS = 8;
	public static final int EXPENSE_COL_CURRENCY_ID_POS = 9;
	public static final int EXPENSE_COL_DATE_POS = 10;
	public static final int EXPENSE_COL_DOCUMENTNO_POS = 11;
	public static final int EXPENSE_COL_INDEX_POS = 12;
	public static final int EXPENSE_COL_FROMTABLE_POS = 13;
	public static final int EXPENSE_COL_FROMRECORD_POS = 14;
	public static final int EXPENSE_COL_AMOUNTENTERED_POS = 15;
	public static final int EXPENSE_COL_CURRENCYENTERED_ID_POS = 16;
	public static final int EXPENSE_COL_CURRENCYRATE_POS = 17;
	public static final int EXPENSE_COL_QUANTITY_POS = 18;
	public static final int EXPENSE_COL_PRICE_POS = 19;
	public static final int EXPENSE_COL_PRICEENTERED_POS = 20;
	public static final int EXPENSE_COL_UOM_ID_POS = 21;
	public static final int EXPENSE_COL_BPARTNER_ID_POS = 22;
	public static final int EXPENSE_COL_BPARTNER_LOCATION_ID_POS = 23;
	public static final int EXPENSE_COL_TAG_ID_POS = 24;

	// currency rate
	public static final int CURRENCYRATE_COL_FROMCURRENCY_ID_POS = 4;
	public static final int CURRENCYRATE_COL_TOCURRENCY_ID_POS = 5;
	public static final int CURRENCYRATE_COL_RATE_POS = 6;
	public static final int CURRENCYRATE_COL_INVERSERATE_POS = 7;

	// gps track
	public static final int GPSTRACK_COL_CAR_ID_POS = 4;
	public static final int GPSTRACK_COL_DRIVER_ID_POS = 5;
	public static final int GPSTRACK_COL_MILEAGE_ID_POS = 6;
	public static final int GPSTRACK_COL_DATE_POS = 7;
	public static final int GPSTRACK_COL_MINACCURACY_POS = 8;
	public static final int GPSTRACK_COL_AVGACCURACY_POS = 9;
	public static final int GPSTRACK_COL_MAXACCURACY_POS = 10;
	public static final int GPSTRACK_COL_MINALTITUDE_POS = 11;
	public static final int GPSTRACK_COL_MAXALTITUDE_POS = 12;
	public static final int GPSTRACK_COL_TOTALTIME_POS = 13;
	public static final int GPSTRACK_COL_MOVINGTIME_POS = 14;
	public static final int GPSTRACK_COL_DISTANCE_POS = 15;
	public static final int GPSTRACK_COL_MAXSPEED_POS = 16;
	public static final int GPSTRACK_COL_AVGSPEED_POS = 17;
	public static final int GPSTRACK_COL_AVGMOVINGSPEED_POS = 18;
	public static final int GPSTRACK_COL_TOTALTRACKPOINTS_POS = 19;
	public static final int GPSTRACK_COL_INVALIDTRACKPOINTS_POS = 20;
	public static final int GPSTRACK_COL_TAG_ID_POS = 21;
	// gps track detail
	public static final int GPSTRACKDETAIL_COL_GPSTRACK_ID_POS = 4;
	public static final int GPSTRACKDETAIL_COL_FILE_POS = 5;
	public static final int GPSTRACKDETAIL_COL_FILEFORMAT_POS = 6;
	// business partner location
	public static final int BPARTNER_LOCATION_COL_BPARTNER_ID_POS = 4;
	public static final int BPARTNER_LOCATION_COL_ADDRESS_POS = 5;
	public static final int BPARTNER_LOCATION_COL_POSTAL_POS = 6;
	public static final int BPARTNER_LOCATION_COL_CITY_POS = 7;
	public static final int BPARTNER_LOCATION_COL_REGION_POS = 8;
	public static final int BPARTNER_LOCATION_COL_COUNTRY_POS = 9;
	public static final int BPARTNER_LOCATION_COL_PHONE_POS = 10;
	public static final int BPARTNER_LOCATION_COL_PHONE2_POS = 11;
	public static final int BPARTNER_LOCATION_COL_FAX_POS = 12;
	public static final int BPARTNER_LOCATION_COL_EMAIL_POS = 13;
	public static final int BPARTNER_LOCATION_COL_CONTACTPERSON_POS = 14;

	public static final int TASK_COL_TASKTYPE_ID_POS = 4;
	public static final int TASK_COL_ISRECURENT_POS = 5;
	public static final int TASK_COL_SCHEDULEDFOR_POS = 6;
	public static final int TASK_COL_ISDIFFERENTSTARTINGTIME_POS = 7;
	public static final int TASK_COL_TIMEFREQUENCY_POS = 8;
	public static final int TASK_COL_TIMEFREQUENCYTYPE_POS = 9;
	public static final int TASK_COL_RUNDAY_POS = 10;
	public static final int TASK_COL_RUNMONTH_POS = 11;
	public static final int TASK_COL_RUNTIME_POS = 12;
	public static final int TASK_COL_REMINDERDAYS_POS = 13;
	public static final int TASK_COL_RUNMILEAGE_POS = 14;
	public static final int TASK_COL_REMINDERMILEAGES_POS = 15;

	public static final int TASK_CAR_COL_TASK_ID_POS = 4;
	public static final int TASK_CAR_COL_CAR_ID_POS = 5;
	public static final int TASK_CAR_COL_FIRSTRUN_DATE_POS = 6;
	public static final int TASK_CAR_COL_FIRSTRUN_MILEAGE_POS = 7;
	
	public static final int TODO_COL_TASK_ID_POS = 4;
	public static final int TODO_COL_CAR_ID_POS = 5;
	public static final int TODO_COL_DUEDATE_POS = 6;
	public static final int TODO_COL_DUEMILAGE_POS = 7;
	public static final int TODO_COL_POSTPONEUNTI_POS = 8;
	public static final int TODO_COL_ISDONE_POS = 9;
	public static final int TODO_COL_DONEDATE_POS = 10;
	public static final int TODO_COL_ISSTOPNOTIFICATION_POS = 11;


	public static final String[] driverTableColNames = { GEN_COL_ROWID_NAME,
			GEN_COL_NAME_NAME, GEN_COL_ISACTIVE_NAME,
			GEN_COL_USER_COMMENT_NAME, DRIVER_COL_LICENSE_NO_NAME };

	public static final String[] carTableColNames = { GEN_COL_ROWID_NAME,
			GEN_COL_NAME_NAME, GEN_COL_ISACTIVE_NAME,
			GEN_COL_USER_COMMENT_NAME, CAR_COL_MODEL_NAME,
			CAR_COL_REGISTRATIONNO_NAME, CAR_COL_INDEXSTART_NAME,
			CAR_COL_INDEXCURRENT_NAME, CAR_COL_UOMLENGTH_ID_NAME,
			CAR_COL_UOMVOLUME_ID_NAME, CAR_COL_CURRENCY_ID_NAME };

	public static final String[] uomTableColNames = { GEN_COL_ROWID_NAME,
			GEN_COL_NAME_NAME, GEN_COL_ISACTIVE_NAME,
			GEN_COL_USER_COMMENT_NAME, UOM_COL_CODE_NAME, UOM_COL_UOMTYPE_NAME };

	public static final String[] uomConversionTableColNames = {
			GEN_COL_ROWID_NAME, GEN_COL_NAME_NAME, GEN_COL_ISACTIVE_NAME,
			GEN_COL_USER_COMMENT_NAME, UOM_CONVERSION_COL_UOMFROM_ID_NAME,
			UOM_CONVERSION_COL_UOMTO_ID_NAME, UOM_CONVERSION_COL_RATE_NAME };

	public static final String[] expenseTypeTableColNames = {
			GEN_COL_ROWID_NAME, GEN_COL_NAME_NAME, GEN_COL_ISACTIVE_NAME,
			GEN_COL_USER_COMMENT_NAME };

	public static final String[] mileageTableColNames = { GEN_COL_ROWID_NAME,
			GEN_COL_NAME_NAME, GEN_COL_ISACTIVE_NAME,
			GEN_COL_USER_COMMENT_NAME, MILEAGE_COL_DATE_NAME,
			MILEAGE_COL_CAR_ID_NAME, MILEAGE_COL_DRIVER_ID_NAME,
			MILEAGE_COL_INDEXSTART_NAME, MILEAGE_COL_INDEXSTOP_NAME,
			MILEAGE_COL_UOMLENGTH_ID_NAME, MILEAGE_COL_EXPENSETYPE_ID_NAME,
			MILEAGE_COL_GPSTRACKLOG_NAME, MILEAGE_COL_TAG_ID_NAME };

	public static final String[] currencyTableColNames = { GEN_COL_ROWID_NAME,
			GEN_COL_NAME_NAME, GEN_COL_ISACTIVE_NAME,
			GEN_COL_USER_COMMENT_NAME, CURRENCY_COL_CODE_NAME };

	public static final String[] refuelTableColNames = { GEN_COL_ROWID_NAME,
			GEN_COL_NAME_NAME, GEN_COL_ISACTIVE_NAME,
			GEN_COL_USER_COMMENT_NAME, REFUEL_COL_CAR_ID_NAME,
			REFUEL_COL_DRIVER_ID_NAME, REFUEL_COL_EXPENSETYPE_ID_NAME,
			REFUEL_COL_INDEX_NAME, REFUEL_COL_QUANTITY_NAME,
			REFUEL_COL_UOMVOLUME_ID_NAME, REFUEL_COL_PRICE_NAME,
			REFUEL_COL_CURRENCY_ID_NAME, REFUEL_COL_DATE_NAME,
			REFUEL_COL_DOCUMENTNO_NAME, REFUEL_COL_EXPENSECATEGORY_NAME,
			REFUEL_COL_ISFULLREFUEL_NAME, REFUEL_COL_QUANTITYENTERED_NAME,
			REFUEL_COL_UOMVOLUMEENTERED_ID_NAME, REFUEL_COL_PRICEENTERED_NAME,
			REFUEL_COL_CURRENCYENTERED_ID_NAME, REFUEL_COL_CURRENCYRATE_NAME,
			REFUEL_COL_UOMVOLCONVERSIONRATE_NAME, REFUEL_COL_AMOUNT_NAME,
			REFUEL_COL_AMOUNTENTERED_NAME, REFUEL_COL_BPARTNER_ID_NAME,
			REFUEL_COL_BPARTNER_LOCATION_ID_NAME, REFUEL_COL_TAG_ID_NAME };

	public static final String[] expenseCategoryTableColNames = {
			GEN_COL_ROWID_NAME, GEN_COL_NAME_NAME, GEN_COL_ISACTIVE_NAME,
			GEN_COL_USER_COMMENT_NAME,
			EXPENSECATEGORY_COL_ISEXCLUDEFROMMILEAGECOST_NAME,
			EXPENSECATEGORY_COL_ISFUEL_NAME };

	public static final String[] expenseTableColNames = { GEN_COL_ROWID_NAME,
			GEN_COL_NAME_NAME, GEN_COL_ISACTIVE_NAME,
			GEN_COL_USER_COMMENT_NAME, EXPENSE_COL_CAR_ID_NAME,
			EXPENSE_COL_DRIVER_ID_NAME, EXPENSE_COL_EXPENSECATEGORY_ID_NAME,
			EXPENSE_COL_EXPENSETYPE_ID_NAME, EXPENSE_COL_AMOUNT_NAME,
			EXPENSE_COL_CURRENCY_ID_NAME, EXPENSE_COL_DATE_NAME,
			EXPENSE_COL_DOCUMENTNO_NAME, EXPENSE_COL_INDEX_NAME,
			EXPENSE_COL_FROMTABLE_NAME, EXPENSE_COL_FROMRECORD_ID_NAME,
			EXPENSE_COL_AMOUNTENTERED_NAME,
			EXPENSE_COL_CURRENCYENTERED_ID_NAME, EXPENSE_COL_CURRENCYRATE_NAME,
			EXPENSE_COL_QUANTITY_NAME, EXPENSE_COL_PRICE_NAME,
			EXPENSE_COL_PRICEENTERED_NAME, EXPENSE_COL_UOM_ID_NAME,
			EXPENSE_COL_BPARTNER_ID_NAME,
			EXPENSE_COL_BPARTNER_LOCATION_ID_NAME, EXPENSE_COL_TAG_ID_NAME };

	public static final String[] currencyRateTableColNames = {
			GEN_COL_ROWID_NAME, GEN_COL_NAME_NAME, GEN_COL_ISACTIVE_NAME,
			GEN_COL_USER_COMMENT_NAME, CURRENCYRATE_COL_FROMCURRENCY_ID_NAME,
			CURRENCYRATE_COL_TOCURRENCY_ID_NAME, CURRENCYRATE_COL_RATE_NAME,
			CURRENCYRATE_COL_INVERSERATE_NAME };

	public static final String[] gpsTrackTableColNames = { GEN_COL_ROWID_NAME,
			GEN_COL_NAME_NAME, GEN_COL_ISACTIVE_NAME,
			GEN_COL_USER_COMMENT_NAME, GPSTRACK_COL_CAR_ID_NAME,
			GPSTRACK_COL_DRIVER_ID_NAME, GPSTRACK_COL_MILEAGE_ID_NAME,
			GPSTRACK_COL_DATE_NAME, GPSTRACK_COL_MINACCURACY_NAME,
			GPSTRACK_COL_AVGACCURACY_NAME, GPSTRACK_COL_MAXACCURACY_NAME,
			GPSTRACK_COL_MINALTITUDE_NAME, GPSTRACK_COL_MAXALTITUDE_NAME,
			GPSTRACK_COL_TOTALTIME_NAME, GPSTRACK_COL_MOVINGTIME_NAME,
			GPSTRACK_COL_DISTANCE_NAME, GPSTRACK_COL_MAXSPEED_NAME,
			GPSTRACK_COL_AVGSPEED_NAME, GPSTRACK_COL_AVGMOVINGSPEED_NAME,
			GPSTRACK_COL_TOTALTRACKPOINTS_NAME,
			GPSTRACK_COL_INVALIDTRACKPOINTS_NAME, GPSTRACK_COL_TAG_ID_NAME };

	public static final String[] gpsTrackDetailTableColNames = {
			GEN_COL_ROWID_NAME, GEN_COL_NAME_NAME, GEN_COL_ISACTIVE_NAME,
			GEN_COL_USER_COMMENT_NAME, GPSTRACKDETAIL_COL_GPSTRACK_ID_NAME,
			GPSTRACKDETAIL_COL_FILE_NAME, GPSTRACKDETAIL_COL_FILEFORMAT_NAME };

	public static final String[] bpartnerTableColNames = { GEN_COL_ROWID_NAME,
			GEN_COL_NAME_NAME, GEN_COL_ISACTIVE_NAME, GEN_COL_USER_COMMENT_NAME };

	// business partner location
	public static final String[] bpartnerLocationTableColNames = {
			GEN_COL_ROWID_NAME, GEN_COL_NAME_NAME, GEN_COL_ISACTIVE_NAME,
			GEN_COL_USER_COMMENT_NAME, BPARTNER_LOCATION_COL_BPARTNER_ID_NAME,
			BPARTNER_LOCATION_COL_ADDRESS_NAME,
			BPARTNER_LOCATION_COL_POSTAL_NAME, BPARTNER_LOCATION_COL_CITY_NAME,
			BPARTNER_LOCATION_COL_REGION_NAME,
			BPARTNER_LOCATION_COL_COUNTRY_NAME,
			BPARTNER_LOCATION_COL_PHONE_NAME,
			BPARTNER_LOCATION_COL_PHONE2_NAME, BPARTNER_LOCATION_COL_FAX_NAME,
			BPARTNER_LOCATION_COL_EMAIL_NAME,
			BPARTNER_LOCATION_COL_CONTACTPERSON_NAME };

	public static final String[] tagTableColNames = { GEN_COL_ROWID_NAME,
			GEN_COL_NAME_NAME, GEN_COL_ISACTIVE_NAME, GEN_COL_USER_COMMENT_NAME };

	// tasks/reminders tables
	public static final String[] taskTypeTableColNames = { GEN_COL_ROWID_NAME,
			GEN_COL_NAME_NAME, GEN_COL_ISACTIVE_NAME, GEN_COL_USER_COMMENT_NAME };
	public static final String[] taskTableColNames = { GEN_COL_ROWID_NAME,
			GEN_COL_NAME_NAME, GEN_COL_ISACTIVE_NAME,
			GEN_COL_USER_COMMENT_NAME, TASK_COL_TASKTYPE_ID_NAME, TASK_COL_ISRECURENT_NAME,
			TASK_COL_SCHEDULEDFOR_NAME, TASK_COL_ISDIFFERENTSTARTINGTIME_NAME, TASK_COL_TIMEFREQUENCY_NAME,
			TASK_COL_TIMEFREQUENCYTYPE_NAME, TASK_COL_RUNDAY_NAME, TASK_COL_RUNMONTH_NAME, TASK_COL_RUNTIME_NAME,
			TASK_COL_REMINDERDAYS_NAME, TASK_COL_RUNMILEAGE_NAME, TASK_COL_REMINDERMILEAGES_NAME};
	public static final String[] taskCarTableColNames = { GEN_COL_ROWID_NAME,GEN_COL_NAME_NAME, GEN_COL_ISACTIVE_NAME, GEN_COL_USER_COMMENT_NAME,
			TASK_CAR_COL_TASK_ID_NAME, TASK_CAR_COL_CAR_ID_NAME, TASK_CAR_COL_FIRSTRUN_DATE_NAME, TASK_CAR_COL_FIRSTRUN_MILEAGE_NAME};

	public static final String[] todoCarTableColNames = { GEN_COL_ROWID_NAME,GEN_COL_NAME_NAME, GEN_COL_ISACTIVE_NAME, GEN_COL_USER_COMMENT_NAME,
		TODO_COL_TASK_ID_NAME, TODO_COL_CAR_ID_NAME, TODO_COL_DUEDATE_NAME, TODO_COL_DUEMILAGE_NAME, TODO_COL_POSTPONEUNTIL_NAME, 
		TODO_COL_ISDONE_NAME, TODO_COL_DONEDATE_NAME, TODO_COL_ISSTOPNOTIFICATION_NAME};

	public static final String[] genColName = { GEN_COL_ROWID_NAME,
			GEN_COL_NAME_NAME };
	public static final String[] genColRowId = { GEN_COL_ROWID_NAME };
	public static final String isActiveCondition = " " + GEN_COL_ISACTIVE_NAME
			+ "='Y' ";
	public static final String isActiveWithAndCondition = " AND"
			+ isActiveCondition + " ";

	/**
	 * Database creation sql statements
	 */
	protected static final String DRIVER_TABLE_CREATE_SQL = "CREATE TABLE IF NOT EXISTS "
			+ DRIVER_TABLE_NAME
			+ " ( "
			+ GEN_COL_ROWID_NAME
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ GEN_COL_NAME_NAME
			+ " TEXT NOT NULL, "
			+ GEN_COL_ISACTIVE_NAME
			+ " TEXT DEFAULT 'Y', "
			+ GEN_COL_USER_COMMENT_NAME
			+ " TEXT NULL, "
			+ DRIVER_COL_LICENSE_NO_NAME
			+ " TEXT NULL "
			+ ");";
	protected static final String CAR_TABLE_CREATE_SQL = "CREATE TABLE IF NOT EXISTS "
			+ CAR_TABLE_NAME
			+ " ( "
			+ GEN_COL_ROWID_NAME
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ GEN_COL_NAME_NAME
			+ " TEXT NOT NULL, "
			+ GEN_COL_ISACTIVE_NAME
			+ " TEXT DEFAULT 'Y', "
			+ GEN_COL_USER_COMMENT_NAME
			+ " TEXT NULL, "
			+ CAR_COL_MODEL_NAME
			+ " TEXT NULL, "
			+ CAR_COL_REGISTRATIONNO_NAME
			+ " TEXT NULL, "
			+ CAR_COL_INDEXSTART_NAME
			+ " NUMERIC, "
			+ CAR_COL_INDEXCURRENT_NAME
			+ " NUMERIC, "
			+ CAR_COL_UOMLENGTH_ID_NAME
			+ " INTEGER, "
			+ CAR_COL_UOMVOLUME_ID_NAME
			+ " INTEGER, "
			+ CAR_COL_CURRENCY_ID_NAME + " INTEGER " + ");";
	protected static final String UOM_TABLE_CREATE_SQL = "CREATE TABLE IF NOT EXISTS "
			+ UOM_TABLE_NAME
			+ " ( "
			+ GEN_COL_ROWID_NAME
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ GEN_COL_NAME_NAME
			+ " TEXT NOT NULL, "
			+ GEN_COL_ISACTIVE_NAME
			+ " TEXT DEFAULT 'Y', "
			+ GEN_COL_USER_COMMENT_NAME
			+ " TEXT NULL, "
			+ UOM_COL_CODE_NAME
			+ " TEXT NOT NULL, "
			+ UOM_COL_UOMTYPE_NAME + " TEXT NOT NULL " + ");";
	protected static final String UOM_CONVERSION_TABLE_CREATE_SQL = "CREATE TABLE IF NOT EXISTS "
			+ UOM_CONVERSION_TABLE_NAME
			+ " ( "
			+ GEN_COL_ROWID_NAME
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ GEN_COL_NAME_NAME
			+ " TEXT NOT NULL, "
			+ GEN_COL_ISACTIVE_NAME
			+ " TEXT DEFAULT 'Y', "
			+ GEN_COL_USER_COMMENT_NAME
			+ " TEXT NULL, "
			+ UOM_CONVERSION_COL_UOMFROM_ID_NAME
			+ " INTEGER NOT NULL, "
			+ UOM_CONVERSION_COL_UOMTO_ID_NAME
			+ " INTEGER NOT NULL, "
			+ UOM_CONVERSION_COL_RATE_NAME
			+ " NUMERIC NOT NULL " + ");";
	protected static final String EXPENSETYPE_TABLE_CREATE_SQL = "CREATE TABLE IF NOT EXISTS "
			+ EXPENSETYPE_TABLE_NAME
			+ " ( "
			+ GEN_COL_ROWID_NAME
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ GEN_COL_NAME_NAME
			+ " TEXT NOT NULL, "
			+ GEN_COL_ISACTIVE_NAME
			+ " TEXT DEFAULT 'Y', "
			+ GEN_COL_USER_COMMENT_NAME
			+ " TEXT NULL "
			+ ");";
	protected static final String MILEAGE_TABLE_CREATE_SQL = "CREATE TABLE IF NOT EXISTS "
			+ MILEAGE_TABLE_NAME
			+ " ( "
			+ GEN_COL_ROWID_NAME
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ GEN_COL_NAME_NAME
			+ " TEXT NULL, "
			+ GEN_COL_ISACTIVE_NAME
			+ " TEXT DEFAULT 'Y', "
			+ GEN_COL_USER_COMMENT_NAME
			+ " TEXT NULL, "
			+ MILEAGE_COL_DATE_NAME
			+ " DATE NOT NULL, "
			+ MILEAGE_COL_CAR_ID_NAME
			+ " INTEGER NOT NULL, "
			+ MILEAGE_COL_DRIVER_ID_NAME
			+ " INTEGER NOT NULL, "
			+ MILEAGE_COL_INDEXSTART_NAME
			+ " NUMERIC NOT NULL, "
			+ MILEAGE_COL_INDEXSTOP_NAME
			+ " NUMERIC NOT NULL, "
			+ MILEAGE_COL_UOMLENGTH_ID_NAME
			+ " INTEGER NOT NULL, "
			+ MILEAGE_COL_EXPENSETYPE_ID_NAME
			+ " INTEGER NOT NULL, "
			+ MILEAGE_COL_GPSTRACKLOG_NAME
			+ " TEXT NULL, "
			+ MILEAGE_COL_TAG_ID_NAME + " INTEGER NULL " + ");";
	protected static final String CURRENCY_TABLE_CREATE_SQL = "CREATE TABLE IF NOT EXISTS "
			+ CURRENCY_TABLE_NAME
			+ " ( "
			+ GEN_COL_ROWID_NAME
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ GEN_COL_NAME_NAME
			+ " TEXT NOT NULL, "
			+ GEN_COL_ISACTIVE_NAME
			+ " TEXT DEFAULT 'Y', "
			+ GEN_COL_USER_COMMENT_NAME
			+ " TEXT NULL, "
			+ CURRENCY_COL_CODE_NAME
			+ " TEXT NOT NULL "
			+ ");";
	protected static final String REFUEL_TABLE_CREATE_SQL = "CREATE TABLE IF NOT EXISTS "
			+ REFUEL_TABLE_NAME
			+ " ( "
			+ GEN_COL_ROWID_NAME
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ GEN_COL_NAME_NAME
			+ " TEXT NULL, "
			+ GEN_COL_ISACTIVE_NAME
			+ " TEXT DEFAULT 'Y', "
			+ GEN_COL_USER_COMMENT_NAME
			+ " TEXT NULL, "
			+ REFUEL_COL_CAR_ID_NAME
			+ " INTEGER, "
			+ REFUEL_COL_DRIVER_ID_NAME
			+ " INTEGER, "
			+ REFUEL_COL_EXPENSETYPE_ID_NAME
			+ " INTEGER, "
			+ REFUEL_COL_INDEX_NAME
			+ " NUMERIC, "
			+ REFUEL_COL_QUANTITY_NAME
			+ " NUMERIC, "
			+ REFUEL_COL_UOMVOLUME_ID_NAME
			+ " INTEGER, "
			+ REFUEL_COL_PRICE_NAME
			+ " NUMERIC, "
			+ REFUEL_COL_CURRENCY_ID_NAME
			+ " INTEGER, "
			+ REFUEL_COL_DATE_NAME
			+ " DATE NULL, "
			+ REFUEL_COL_DOCUMENTNO_NAME
			+ " TEXT NULL, "
			+ REFUEL_COL_EXPENSECATEGORY_NAME
			+ " INTEGER, "
			+ REFUEL_COL_ISFULLREFUEL_NAME
			+ " TEXT DEFAULT 'N', "
			+ REFUEL_COL_QUANTITYENTERED_NAME
			+ " NUMERIC NULL, "
			+ REFUEL_COL_UOMVOLUMEENTERED_ID_NAME
			+ " INTEGER NULL, "
			+ REFUEL_COL_PRICEENTERED_NAME
			+ " NUMERIC NULL, "
			+ REFUEL_COL_CURRENCYENTERED_ID_NAME
			+ " INTEGER NULL, "
			+ REFUEL_COL_CURRENCYRATE_NAME
			+ " NUMERIC NULL, "
			+ REFUEL_COL_UOMVOLCONVERSIONRATE_NAME
			+ " NUMERIC NULL, "
			+ REFUEL_COL_AMOUNT_NAME
			+ " NUMERIC NULL, "
			+ REFUEL_COL_AMOUNTENTERED_NAME
			+ " NUMERIC NULL, "
			+ REFUEL_COL_BPARTNER_ID_NAME
			+ " INTEGER NULL, "
			+ REFUEL_COL_BPARTNER_LOCATION_ID_NAME
			+ " INTEGER NULL, "
			+ REFUEL_COL_TAG_ID_NAME + " INTEGER NULL " + ");";

	protected static final String EXPENSECATEGORY_TABLE_CREATE_SQL = "CREATE TABLE IF NOT EXISTS "
			+ EXPENSECATEGORY_TABLE_NAME
			+ " ( "
			+ GEN_COL_ROWID_NAME
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ GEN_COL_NAME_NAME
			+ " TEXT NOT NULL, "
			+ GEN_COL_ISACTIVE_NAME
			+ " TEXT DEFAULT 'Y', "
			+ GEN_COL_USER_COMMENT_NAME
			+ " TEXT NULL, "
			+ EXPENSECATEGORY_COL_ISEXCLUDEFROMMILEAGECOST_NAME
			+ " TEXT DEFAULT 'N', "
			+ EXPENSECATEGORY_COL_ISFUEL_NAME
			+ " TEXT DEFAULT 'N' " + ");";

	protected static final String EXPENSE_TABLE_CREATE_SQL = "CREATE TABLE IF NOT EXISTS "
			+ EXPENSE_TABLE_NAME
			+ " ( "
			+ GEN_COL_ROWID_NAME
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ GEN_COL_NAME_NAME
			+ " TEXT NOT NULL, "
			+ GEN_COL_ISACTIVE_NAME
			+ " TEXT DEFAULT 'Y', "
			+ GEN_COL_USER_COMMENT_NAME
			+ " TEXT NULL, "
			+ EXPENSE_COL_CAR_ID_NAME
			+ " INTEGER, "
			+ EXPENSE_COL_DRIVER_ID_NAME
			+ " INTEGER, "
			+ EXPENSE_COL_EXPENSECATEGORY_ID_NAME
			+ " INTEGER, "
			+ EXPENSE_COL_EXPENSETYPE_ID_NAME
			+ " INTEGER, "
			+ EXPENSE_COL_AMOUNT_NAME
			+ " NUMERIC, "
			+ EXPENSE_COL_CURRENCY_ID_NAME
			+ " INTEGER, "
			+ EXPENSE_COL_DATE_NAME
			+ " DATE NULL, "
			+ EXPENSE_COL_DOCUMENTNO_NAME
			+ " TEXT NULL, "
			+ EXPENSE_COL_INDEX_NAME
			+ " NUMERIC, "
			+ EXPENSE_COL_FROMTABLE_NAME
			+ " TEXT NULL, "
			+ EXPENSE_COL_FROMRECORD_ID_NAME
			+ " INTEGER, "
			+ EXPENSE_COL_AMOUNTENTERED_NAME
			+ " NUMERIC NULL, "
			+ EXPENSE_COL_CURRENCYENTERED_ID_NAME
			+ " INTEGER NULL, "
			+ EXPENSE_COL_CURRENCYRATE_NAME
			+ " NUMERIC NULL, "
			+ EXPENSE_COL_QUANTITY_NAME
			+ " NUMERIC NULL, "
			+ EXPENSE_COL_PRICE_NAME
			+ " NUMERIC NULL, "
			+ EXPENSE_COL_PRICEENTERED_NAME
			+ " NUMERIC NULL, "
			+ EXPENSE_COL_UOM_ID_NAME
			+ " INTEGER NULL, "
			+ EXPENSE_COL_BPARTNER_ID_NAME
			+ " INTEGER NULL, "
			+ EXPENSE_COL_BPARTNER_LOCATION_ID_NAME
			+ " INTEGER NULL, "
			+ EXPENSE_COL_TAG_ID_NAME + " INTEGER NULL " + ");";

	protected static final String CURRENCYRATE_TABLE_CREATE_SQL = "CREATE TABLE IF NOT EXISTS "
			+ CURRENCYRATE_TABLE_NAME
			+ " ( "
			+ GEN_COL_ROWID_NAME
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ GEN_COL_NAME_NAME
			+ " TEXT, "
			+ GEN_COL_ISACTIVE_NAME
			+ " TEXT DEFAULT 'Y', "
			+ GEN_COL_USER_COMMENT_NAME
			+ " TEXT NULL, "
			+ CURRENCYRATE_COL_FROMCURRENCY_ID_NAME
			+ " INTEGER, "
			+ CURRENCYRATE_COL_TOCURRENCY_ID_NAME
			+ " INTEGER, "
			+ CURRENCYRATE_COL_RATE_NAME
			+ " NUMERIC, "
			+ CURRENCYRATE_COL_INVERSERATE_NAME + " NUMERIC " + ");";

	protected static final String GPSTRACK_TABLE_CREATE_SQL = "CREATE TABLE IF NOT EXISTS "
			+ GPSTRACK_TABLE_NAME
			+ " ( "
			+ GEN_COL_ROWID_NAME
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ GEN_COL_NAME_NAME
			+ " TEXT NULL, "
			+ GEN_COL_ISACTIVE_NAME
			+ " TEXT DEFAULT 'Y', "
			+ GEN_COL_USER_COMMENT_NAME
			+ " TEXT NULL, "
			+ GPSTRACK_COL_CAR_ID_NAME
			+ " INTEGER NULL, "
			+ GPSTRACK_COL_DRIVER_ID_NAME
			+ " INTEGER NULL, "
			+ GPSTRACK_COL_MILEAGE_ID_NAME
			+ " INTEGER NULL, "
			+ GPSTRACK_COL_DATE_NAME
			+ " DATE NULL, "
			+ GPSTRACK_COL_MINACCURACY_NAME
			+ " NUMERIC NULL, "
			+ GPSTRACK_COL_AVGACCURACY_NAME
			+ " NUMERIC NULL, "
			+ GPSTRACK_COL_MAXACCURACY_NAME
			+ " NUMERIC NULL, "
			+ GPSTRACK_COL_MINALTITUDE_NAME
			+ " NUMERIC NULL, "
			+ GPSTRACK_COL_MAXALTITUDE_NAME
			+ " NUMERIC NULL, "
			+ GPSTRACK_COL_TOTALTIME_NAME
			+ " NUMERIC NULL, "
			+ GPSTRACK_COL_MOVINGTIME_NAME
			+ " NUMERIC NULL, "
			+ GPSTRACK_COL_DISTANCE_NAME
			+ " NUMERIC NULL, "
			+ GPSTRACK_COL_MAXSPEED_NAME
			+ " NUMERIC NULL, "
			+ GPSTRACK_COL_AVGSPEED_NAME
			+ " NUMERIC NULL, "
			+ GPSTRACK_COL_AVGMOVINGSPEED_NAME
			+ " NUMERIC NULL, "
			+ GPSTRACK_COL_TOTALTRACKPOINTS_NAME
			+ " INTEGER NULL, "
			+ GPSTRACK_COL_INVALIDTRACKPOINTS_NAME
			+ " INTEGER NULL, "
			+ GPSTRACK_COL_TAG_ID_NAME + " INTEGER NULL " + ");";

	protected static final String GPSTRACKDETAIL_TABLE_CREATE_SQL = "CREATE TABLE IF NOT EXISTS "
			+ GPSTRACKDETAIL_TABLE_NAME
			+ " ( "
			+ GEN_COL_ROWID_NAME
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ GEN_COL_NAME_NAME
			+ " TEXT NULL, "
			+ GEN_COL_ISACTIVE_NAME
			+ " TEXT DEFAULT 'Y', "
			+ GEN_COL_USER_COMMENT_NAME
			+ " TEXT NULL, "
			+ GPSTRACKDETAIL_COL_GPSTRACK_ID_NAME
			+ " INTEGER NOT NULL, "
			+ GPSTRACKDETAIL_COL_FILE_NAME
			+ " TEXT NULL, "
			+ GPSTRACKDETAIL_COL_FILEFORMAT_NAME + " TEXT NULL " + ");";

	protected static final String BPARTNER_TABLE_CREATE_SQL = "CREATE TABLE IF NOT EXISTS "
			+ BPARTNER_TABLE_NAME
			+ " ( "
			+ GEN_COL_ROWID_NAME
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ GEN_COL_NAME_NAME
			+ " TEXT NOT NULL, "
			+ GEN_COL_ISACTIVE_NAME
			+ " TEXT DEFAULT 'Y', "
			+ GEN_COL_USER_COMMENT_NAME
			+ " TEXT NULL "
			+ ");";

	protected static final String BPARTNER_LOCATION_TABLE_CREATE_SQL = "CREATE TABLE IF NOT EXISTS "
			+ BPARTNER_LOCATION_TABLE_NAME
			+ " ( "
			+ GEN_COL_ROWID_NAME
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ GEN_COL_NAME_NAME
			+ " TEXT NOT NULL, "
			+ GEN_COL_ISACTIVE_NAME
			+ " TEXT DEFAULT 'Y', "
			+ GEN_COL_USER_COMMENT_NAME
			+ " TEXT NULL, "
			+ BPARTNER_LOCATION_COL_BPARTNER_ID_NAME
			+ " INTEGER NOT NULL, "
			+ BPARTNER_LOCATION_COL_ADDRESS_NAME
			+ " TEXT NULL, "
			+ BPARTNER_LOCATION_COL_POSTAL_NAME
			+ " TEXT NULL, "
			+ BPARTNER_LOCATION_COL_CITY_NAME
			+ " TEXT NULL, "
			+ BPARTNER_LOCATION_COL_REGION_NAME
			+ " TEXT NULL, "
			+ BPARTNER_LOCATION_COL_COUNTRY_NAME
			+ " TEXT NULL, "
			+ BPARTNER_LOCATION_COL_PHONE_NAME
			+ " TEXT NULL, "
			+ BPARTNER_LOCATION_COL_PHONE2_NAME
			+ " TEXT NULL, "
			+ BPARTNER_LOCATION_COL_FAX_NAME
			+ " TEXT NULL, "
			+ BPARTNER_LOCATION_COL_EMAIL_NAME
			+ " TEXT NULL, "
			+ BPARTNER_LOCATION_COL_CONTACTPERSON_NAME + " TEXT NULL " + ");";

	protected static final String TAG_TABLE_CREATE_SQL = "CREATE TABLE IF NOT EXISTS "
			+ TAG_TABLE_NAME
			+ " ( "
			+ GEN_COL_ROWID_NAME
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ GEN_COL_NAME_NAME
			+ " TEXT NOT NULL, "
			+ GEN_COL_ISACTIVE_NAME
			+ " TEXT DEFAULT 'Y', "
			+ GEN_COL_USER_COMMENT_NAME
			+ " TEXT NULL "
			+ ");";

	protected static final String TASKTYPE_TABLE_CREATE_SQL = "CREATE TABLE IF NOT EXISTS "
			+ TASKTYPE_TABLE_NAME
			+ " ( "
			+ GEN_COL_ROWID_NAME
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ GEN_COL_NAME_NAME
			+ " TEXT NOT NULL, "
			+ GEN_COL_ISACTIVE_NAME
			+ " TEXT DEFAULT 'Y', "
			+ GEN_COL_USER_COMMENT_NAME
			+ " TEXT NULL "
			+ ");";

	protected static final String TASK_TABLE_CREATE_SQL = "CREATE TABLE IF NOT EXISTS "
			+ TASK_TABLE_NAME
			+ " ( "
			+ GEN_COL_ROWID_NAME
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ GEN_COL_NAME_NAME
			+ " TEXT NOT NULL, "
			+ GEN_COL_ISACTIVE_NAME
			+ " TEXT DEFAULT 'Y', "
			+ GEN_COL_USER_COMMENT_NAME
			+ " TEXT NULL, "
			+ TASK_COL_TASKTYPE_ID_NAME
			+ " INTEGER NOT NULL, "
			+ TASK_COL_ISRECURENT_NAME 
			+ " TEXT DEFAULT 'Y', "
			+ TASK_COL_SCHEDULEDFOR_NAME
			+ " TEXT NULL, "
			+ TASK_COL_ISDIFFERENTSTARTINGTIME_NAME
			+ " TEXT NULL, "
			+ TASK_COL_TIMEFREQUENCY_NAME
			+ " INTEGER NULL, "
			+ TASK_COL_TIMEFREQUENCYTYPE_NAME
			+ " INTEGER NULL, "
			+ TASK_COL_RUNDAY_NAME
			+ " INTEGER NULL, "
			+ TASK_COL_RUNMONTH_NAME
			+ " INTEGER NULL, "
			+ TASK_COL_RUNTIME_NAME
			+ " DATE NULL, "
			+ TASK_COL_REMINDERDAYS_NAME
			+ " INTEGER NULL, "
			+ TASK_COL_RUNMILEAGE_NAME
			+ " INTEGER NULL, "
			+ TASK_COL_REMINDERMILEAGES_NAME
			+ " INTEGER NULL, "
			+ " FOREIGN KEY(" + TASK_COL_TASKTYPE_ID_NAME + ") REFERENCES " + TASKTYPE_TABLE_NAME + "(" + GEN_COL_ROWID_NAME + ")"
			+ ");";

	protected static final String TASK_CAR_TABLE_CREATE_SQL = "CREATE TABLE IF NOT EXISTS "
		+ TASK_CAR_TABLE_NAME
		+ " ( "
		+ GEN_COL_ROWID_NAME
		+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
		+ GEN_COL_NAME_NAME
		+ " TEXT NOT NULL, "
		+ GEN_COL_ISACTIVE_NAME
		+ " TEXT DEFAULT 'Y', "
		+ GEN_COL_USER_COMMENT_NAME
		+ " TEXT NULL, "
		+ TASK_CAR_COL_TASK_ID_NAME
		+ " INTEGER NOT NULL, "
		+ TASK_CAR_COL_CAR_ID_NAME
		+ " INTEGER NOT NULL, "
		+ TASK_CAR_COL_FIRSTRUN_DATE_NAME 
		+ " DATE NULL, "
		+ TASK_CAR_COL_FIRSTRUN_MILEAGE_NAME
		+ " INTEGER NULL "
		+ ");";

	protected static final String TODO_TABLE_CREATE_SQL = "CREATE TABLE IF NOT EXISTS "
		+ TODO_TABLE_NAME
		+ " ( "
		+ GEN_COL_ROWID_NAME
		+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
		+ GEN_COL_NAME_NAME
		+ " TEXT NOT NULL, "
		+ GEN_COL_ISACTIVE_NAME
		+ " TEXT DEFAULT 'Y', "
		+ GEN_COL_USER_COMMENT_NAME
		+ " TEXT NULL, "
		+ TODO_COL_TASK_ID_NAME 
		+ " INTEGER NOT NULL, "
		+ TODO_COL_CAR_ID_NAME
		+ " INTEGER NULL, "
		+ TODO_COL_DUEDATE_NAME
		+ " DATE NULL, "
		+ TODO_COL_DUEMILAGE_NAME
		+ " INTEGER NULL, "
		+ TODO_COL_POSTPONEUNTIL_NAME
		+ " DATE NULL, "
		+ TODO_COL_ISDONE_NAME
		+ " TEXT DEFAULT 'N', "
		+ TODO_COL_DONEDATE_NAME
		+ " DATE NULL, "
		+ TODO_COL_ISSTOPNOTIFICATION_NAME
		+ " TEXT DEFAULT 'N' "
		+ ");";

	/**
	 * Constructor - takes the context to allow the database to be
	 * opened/created
	 * 
	 * @param ctx
	 *            the Context within which to work
	 */
	public DB(Context ctx) {
		if (ctx != null
				&& ctx.getSharedPreferences(
						StaticValues.GLOBAL_PREFERENCE_NAME, 0).getBoolean(
						"SendCrashReport", true))
			Thread.setDefaultUncaughtExceptionHandler(new AndiCarExceptionHandler(
					Thread.getDefaultUncaughtExceptionHandler(), ctx));
		this.mCtx = ctx;
		if (mDb == null) {
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
	 * @throws SQLException
	 *             if the database could be neither opened or created
	 */
	public DB open() throws SQLException {
		if (mDbHelper == null) {
			mDbHelper = new DatabaseHelper(mCtx);
			if (mDb == null || !mDb.isOpen())
				mDb = mDbHelper.getWritableDatabase();
		}
		return this;
	}

	public void close() {
		try {
			mDbHelper.close();
			mDbHelper = null;
			mDb.close();
			mDb = null;
		} catch (SQLiteException e) {
		}
	}

	public static String sqlConcatTableColumn(String tableName,
			String columnName) {
		return tableName + "." + columnName;
	}

	protected class DatabaseHelper extends SQLiteOpenHelper {
		protected Resources mResource = null;

		DatabaseHelper(Context context) {
			super(context, StaticValues.DATABASE_NAME, null,
					StaticValues.DATABASE_VERSION);
			mResource = context.getResources();
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
            //create drivers table
            db.execSQL(DRIVER_TABLE_CREATE_SQL);
			// create cars table
			db.execSQL(CAR_TABLE_CREATE_SQL);
			createUOMTable(db);

			// create uom conversions table
			createUOMConversionTable(db);

			createExpenseTypeTable(db);

			createMileageTable(db);

			// create & init currencies
			createCurrencyTable(db);
			createRefuelTable(db);
			createExpenseCategory(db);
			// expenses table
			createExpenses(db, false);
			// currency rate
			createCurrencyRateTable(db);

			// gps track
			createGPSTrackTables(db);
			createBPartnerTable(db);

			createTagTable(db);
			AddOnDBObjectDef.createAddOnTable(db);
			AddOnDBObjectDef.createAddOnBKScheduleTable(db);
			AddOnDBObjectDef.createAddOnSecureBKSettingsTable(db);

			createTaskTables(db);

			// create indexes
			createIndexes(db);

			// create the folders on SDCARD
			FileUtils fu = new FileUtils(mCtx);
			if (fu.createFolderIfNotExists(0) != -1) {
				Log.e(TAG, fu.lastError);
			}

			// }
			// catch(SQLException ex) {
			// Log.e(TAG, ex.getMessage());
			// }

		}

		private void createTaskTables(SQLiteDatabase db) throws SQLException {
			// create task/reminder
			db.execSQL(TASKTYPE_TABLE_CREATE_SQL);
			db.execSQL(TASK_TABLE_CREATE_SQL);
			db.execSQL(TASK_CAR_TABLE_CREATE_SQL);
			db.execSQL(TODO_TABLE_CREATE_SQL);
		}

		private void createBPartnerTable(SQLiteDatabase db) throws SQLException {
			// business partner
			db.execSQL(BPARTNER_TABLE_CREATE_SQL);
			db.execSQL(BPARTNER_LOCATION_TABLE_CREATE_SQL);
		}

		private void createGPSTrackTables(SQLiteDatabase db)
				throws SQLException {
			db.execSQL(GPSTRACK_TABLE_CREATE_SQL);
			db.execSQL(GPSTRACKDETAIL_TABLE_CREATE_SQL);
		}

		private void createUOMTable(SQLiteDatabase db) throws SQLException {
			// create uom table
			db.execSQL(UOM_TABLE_CREATE_SQL);
			// init uom's
			String colPart = "INSERT INTO " + UOM_TABLE_NAME + " ( "
					+ GEN_COL_NAME_NAME + ", " + GEN_COL_ISACTIVE_NAME + ", "
					+ GEN_COL_USER_COMMENT_NAME + ", " + UOM_COL_CODE_NAME
					+ ", " + UOM_COL_UOMTYPE_NAME + ") ";
			db.execSQL(colPart + "VALUES ( " + "'"
					+ mResource.getString(R.string.DB_UOM_KmName) + "', "
					+ "'Y', " + "'"
					+ mResource.getString(R.string.DB_UOM_KmComment) + "', "
					+ "'km', " + "'L' )"); // _id = 1
			db.execSQL(colPart + "VALUES ( " + "'"
					+ mResource.getString(R.string.DB_UOM_MiName) + "', "
					+ "'Y', " + "'"
					+ mResource.getString(R.string.DB_UOM_MiComment) + "', "
					+ "'mi', " + "'L' )"); // _id = 2 1609,344 m
			db.execSQL(colPart + "VALUES ( " + "'"
					+ mResource.getString(R.string.DB_UOM_LName) + "', "
					+ "'Y', " + "'"
					+ mResource.getString(R.string.DB_UOM_LComment) + "', "
					+ "'l', " + "'V' )"); // _id = 3
			db.execSQL(colPart + "VALUES ( " + "'"
					+ mResource.getString(R.string.DB_UOM_USGName) + "', "
					+ "'Y', " + "'"
					+ mResource.getString(R.string.DB_UOM_USGComment) + "', "
					+ "'gal US', " + "'V' )"); // _id = 4 3,785 411 784 l
			db.execSQL(colPart + "VALUES ( " + "'"
					+ mResource.getString(R.string.DB_UOM_GBGName) + "', "
					+ "'Y', " + "'"
					+ mResource.getString(R.string.DB_UOM_GBGComment) + "', "
					+ "'gal GB', " + "'V' )"); // _id = 5 4,546 09 l
		}

		private void createUOMConversionTable(SQLiteDatabase db)
				throws SQLException {
			db.execSQL(UOM_CONVERSION_TABLE_CREATE_SQL);
			// init default uom conversions
			String colPart = "INSERT INTO " + UOM_CONVERSION_TABLE_NAME + " ( "
					+ GEN_COL_NAME_NAME + ", " + GEN_COL_ISACTIVE_NAME + ", "
					+ GEN_COL_USER_COMMENT_NAME + ", "
					+ UOM_CONVERSION_COL_UOMFROM_ID_NAME + ", "
					+ UOM_CONVERSION_COL_UOMTO_ID_NAME + ", "
					+ UOM_CONVERSION_COL_RATE_NAME + " " + ") ";
			db.execSQL(colPart + "VALUES ( " + "'"
					+ mResource.getString(R.string.DB_UOMConv_MiToKmName)
					+ "', " + "'Y', " + "'"
					+ mResource.getString(R.string.DB_UOMConv_MiToKmComment)
					+ "', " + "2, " + "1, " + "1.609344 )");
			db.execSQL(colPart + "VALUES ( " + "'"
					+ mResource.getString(R.string.DB_UOMConv_KmToMiName)
					+ "', " + "'Y', " + "'"
					+ mResource.getString(R.string.DB_UOMConv_KmToMiComment)
					+ "', " + "1, " + "2, " + "0.621371 )");
			db.execSQL(colPart + "VALUES ( " + "'"
					+ mResource.getString(R.string.DB_UOMConv_USGToLName)
					+ "', " + "'Y', " + "'"
					+ mResource.getString(R.string.DB_UOMConv_USGToLComment)
					+ "', " + "4, " + "3, " + "3.785412 )");
			db.execSQL(colPart + "VALUES ( " + "'"
					+ mResource.getString(R.string.DB_UOMConv_LToUSGName)
					+ "', " + "'Y', " + "'"
					+ mResource.getString(R.string.DB_UOMConv_LToUSGComment)
					+ "', " + "3, " + "4, " + "0.264172 )");
			db.execSQL(colPart + "VALUES ( " + "'"
					+ mResource.getString(R.string.DB_UOMConv_GBGToLName)
					+ "', " + "'Y', " + "'"
					+ mResource.getString(R.string.DB_UOMConv_GBGToLComment)
					+ "', " + "5, " + "3, " + "4.54609 )");
			db.execSQL(colPart + "VALUES ( " + "'"
					+ mResource.getString(R.string.DB_UOMConv_LToGBGName)
					+ "', " + "'Y', " + "'"
					+ mResource.getString(R.string.DB_UOMConv_LToGBGComment)
					+ "', " + "3, " + "5, " + "0.219969 )");
			db.execSQL(colPart + "VALUES ( " + "'"
					+ mResource.getString(R.string.DB_UOMConv_GBGToUSGName)
					+ "', " + "'Y', " + "'"
					+ mResource.getString(R.string.DB_UOMConv_GBGToUSGComment)
					+ "', " + "5, " + "4, " + "1.200950 )");
			db.execSQL(colPart + "VALUES ( " + "'"
					+ mResource.getString(R.string.DB_UOMConv_USGToGBGName)
					+ "', " + "'Y', " + "'"
					+ mResource.getString(R.string.DB_UOMConv_USGToGBGComment)
					+ "', " + "4, " + "5, " + "0.832674 )");
		}

		private void createExpenseTypeTable(SQLiteDatabase db)
				throws SQLException {
			// create expense types table
			db.execSQL(EXPENSETYPE_TABLE_CREATE_SQL);
			// init some standard expenses
			String colPart = "INSERT INTO " + EXPENSETYPE_TABLE_NAME + " ( "
					+ GEN_COL_NAME_NAME + ", " + GEN_COL_ISACTIVE_NAME + ", "
					+ GEN_COL_USER_COMMENT_NAME + " " + ") ";
			db.execSQL(colPart + "VALUES ( " + "'"
					+ mResource.getString(R.string.DB_ExpType_PersonalName)
					+ "', " + "'Y', " + "'"
					+ mResource.getString(R.string.DB_ExpType_PersonalComment)
					+ "' " + ")");
			db.execSQL(colPart + "VALUES ( " + "'"
					+ mResource.getString(R.string.DB_ExpType_EmployerName)
					+ "', " + "'Y', " + "'"
					+ mResource.getString(R.string.DB_ExpType_EmployerComment)
					+ "' " + ")");
		}

		private void createMileageTable(SQLiteDatabase db) throws SQLException {
			// create mileage table
			db.execSQL(MILEAGE_TABLE_CREATE_SQL);
		}

		private void createRefuelTable(SQLiteDatabase db) throws SQLException {
			db.execSQL(REFUEL_TABLE_CREATE_SQL);
		}

		private void createTagTable(SQLiteDatabase db) throws SQLException {
			// business partner
			db.execSQL(TAG_TABLE_CREATE_SQL);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

			// !!!!!!!!!!!!!!DON'T FORGET onCREATE !!!!!!!!!!!!!!!!

			// AndiCar 1.0.0
			if (oldVersion == 1) {
				upgradeDbTo200(db); // update database to version 200 //AndiCar
									// 2.0.0
				upgradeDbTo210(db, oldVersion); // update database to version
												// 210 //AndiCar 2.1.0
				upgradeDbTo300(db, oldVersion);
				upgradeDbTo310(db, oldVersion);
				upgradeDbTo330(db, oldVersion);
				upgradeDbTo340(db, oldVersion);
				upgradeDbTo350(db, oldVersion);
				AddOnDBObjectDef.upgradeTo353(db);
				upgradeDbTo355(db, oldVersion);
				upgradeDbTo356(db, oldVersion);
				upgradeDbTo357(db, oldVersion);
			}
			// AndiCar 2.0.x
			else if (oldVersion == 200) {
				upgradeDbTo210(db, oldVersion); // update database to version
												// 210 //AndiCar 2.1.0
				upgradeDbTo300(db, oldVersion);
				upgradeDbTo310(db, oldVersion);
				upgradeDbTo330(db, oldVersion);
				upgradeDbTo340(db, oldVersion);
				upgradeDbTo350(db, oldVersion);
				AddOnDBObjectDef.upgradeTo353(db);
				upgradeDbTo355(db, oldVersion);
				upgradeDbTo356(db, oldVersion);
				upgradeDbTo357(db, oldVersion);
			}
			// AndiCar 2.1.x
			else if (oldVersion == 210) {
				upgradeDbTo300(db, oldVersion); // update database to version
												// 210 //AndiCar 2.2.0
				upgradeDbTo310(db, oldVersion);
				upgradeDbTo330(db, oldVersion);
				upgradeDbTo340(db, oldVersion);
				upgradeDbTo350(db, oldVersion);
				AddOnDBObjectDef.upgradeTo353(db);
				upgradeDbTo355(db, oldVersion);
				upgradeDbTo356(db, oldVersion);
				upgradeDbTo357(db, oldVersion);
			}
			// AndiCar 3.0.x
			else if (oldVersion == 300) {
				upgradeDbTo310(db, oldVersion);
				upgradeDbTo330(db, oldVersion);
				upgradeDbTo340(db, oldVersion);
				upgradeDbTo350(db, oldVersion);
				AddOnDBObjectDef.upgradeTo353(db);
				upgradeDbTo355(db, oldVersion);
				upgradeDbTo356(db, oldVersion);
				upgradeDbTo357(db, oldVersion);
			}
			// AndiCar 3.1.x, 3.2.x
			else if (oldVersion == 310) {
				upgradeDbTo330(db, oldVersion);
				upgradeDbTo340(db, oldVersion);
				upgradeDbTo350(db, oldVersion);
				AddOnDBObjectDef.upgradeTo353(db);
				upgradeDbTo355(db, oldVersion);
				upgradeDbTo356(db, oldVersion);
				upgradeDbTo357(db, oldVersion);
			}
			// AndiCar 3.3.x
			else if (oldVersion == 330) {
				upgradeDbTo340(db, oldVersion);
				upgradeDbTo350(db, oldVersion);
				AddOnDBObjectDef.upgradeTo353(db);
				upgradeDbTo355(db, oldVersion);
				upgradeDbTo356(db, oldVersion);
				upgradeDbTo357(db, oldVersion);
			}
			// AndiCar 3.4.x
			else if (oldVersion == 340 || oldVersion == 350) { // upgrade again
																// because on
																// fresh 350
																// install addon
																// tables was
																// not created
				upgradeDbTo350(db, oldVersion);
				AddOnDBObjectDef.upgradeTo353(db);
				upgradeDbTo355(db, oldVersion);
				upgradeDbTo356(db, oldVersion);
				upgradeDbTo357(db, oldVersion);
			} else if (oldVersion == 351) {
				AddOnDBObjectDef.upgradeTo353(db);
				upgradeDbTo355(db, oldVersion);
				upgradeDbTo356(db, oldVersion);
				upgradeDbTo357(db, oldVersion);
			} else if (oldVersion == 353) {
				upgradeDbTo355(db, oldVersion);
				upgradeDbTo356(db, oldVersion);
				upgradeDbTo357(db, oldVersion);
			} else if (oldVersion == 355) {
				upgradeDbTo356(db, oldVersion);
				upgradeDbTo357(db, oldVersion);
			} else if (oldVersion == 356) {
				upgradeDbTo357(db, oldVersion);
			}
			 upgradeDbTo357(db, oldVersion);
			// !!!!!!!!!!!!!!DON'T FORGET onCREATE !!!!!!!!!!!!!!!!

			// create indexes
			createIndexes(db);
			// create the missing folders on SDCARD
			FileUtils fu = new FileUtils(mCtx);
			if (fu.createFolderIfNotExists(FileUtils.ALL_FOLDER) != -1) {
				Log.e(TAG, fu.lastError);
			}

		}

		private void upgradeDbTo200(SQLiteDatabase db) throws SQLException {
			createExpenseCategory(db);
			String updateSql;
			if (!columnExists(db, REFUEL_TABLE_NAME,
					REFUEL_COL_EXPENSECATEGORY_NAME)) {
				updateSql = "ALTER TABLE " + REFUEL_TABLE_NAME + " ADD "
						+ REFUEL_COL_EXPENSECATEGORY_NAME + " INTEGER";
				db.execSQL(updateSql);
				updateSql = "UPDATE " + REFUEL_TABLE_NAME + " SET "
						+ REFUEL_COL_EXPENSECATEGORY_NAME + " = 1";
				db.execSQL(updateSql);
			}
			if (!columnExists(db, REFUEL_TABLE_NAME,
					REFUEL_COL_ISFULLREFUEL_NAME)) {
				db.execSQL("ALTER TABLE " + REFUEL_TABLE_NAME + " ADD "
						+ REFUEL_COL_ISFULLREFUEL_NAME + " TEXT DEFAULT 'N' ");
			}
			createExpenses(db, true);
		}

		private void upgradeDbTo210(SQLiteDatabase db, int oldVersion)
				throws SQLException {
			String updSql = "";

			createCurrencyRateTable(db);
			if (!columnExists(db, REFUEL_TABLE_NAME,
					REFUEL_COL_QUANTITYENTERED_NAME)) {
				updSql = "ALTER TABLE " + REFUEL_TABLE_NAME + " ADD "
						+ REFUEL_COL_QUANTITYENTERED_NAME + " NUMERIC NULL ";
				db.execSQL(updSql);
				updSql = "UPDATE " + REFUEL_TABLE_NAME + " SET "
						+ REFUEL_COL_QUANTITYENTERED_NAME + " = "
						+ REFUEL_COL_QUANTITY_NAME;
				db.execSQL(updSql);
			}
			if (!columnExists(db, REFUEL_TABLE_NAME,
					REFUEL_COL_UOMVOLUMEENTERED_ID_NAME)) {
				updSql = "ALTER TABLE " + REFUEL_TABLE_NAME + " ADD "
						+ REFUEL_COL_UOMVOLUMEENTERED_ID_NAME
						+ " INTEGER NULL ";
				db.execSQL(updSql);
				updSql = "UPDATE " + REFUEL_TABLE_NAME + " SET "
						+ REFUEL_COL_UOMVOLUMEENTERED_ID_NAME + " = "
						+ REFUEL_COL_UOMVOLUME_ID_NAME;
				db.execSQL(updSql);
				updSql = "UPDATE "
						+ REFUEL_TABLE_NAME
						+ " SET "
						+ REFUEL_COL_UOMVOLUME_ID_NAME
						+ " = "
						+ "(SELECT "
						+ CAR_COL_UOMVOLUME_ID_NAME
						+ " "
						+ "FROM "
						+ CAR_TABLE_NAME
						+ " "
						+ "WHERE "
						+ GEN_COL_ROWID_NAME
						+ " = "
						+ sqlConcatTableColumn(REFUEL_TABLE_NAME,
								REFUEL_COL_CAR_ID_NAME) + ") ";
				db.execSQL(updSql);
			}
			if (!columnExists(db, REFUEL_TABLE_NAME,
					REFUEL_COL_PRICEENTERED_NAME)) {
				updSql = "ALTER TABLE " + REFUEL_TABLE_NAME + " ADD "
						+ REFUEL_COL_PRICEENTERED_NAME + " NUMERIC NULL ";
				db.execSQL(updSql);
				updSql = "UPDATE " + REFUEL_TABLE_NAME + " SET "
						+ REFUEL_COL_PRICEENTERED_NAME + " = "
						+ REFUEL_COL_PRICE_NAME;
				db.execSQL(updSql);
			}
			if (!columnExists(db, REFUEL_TABLE_NAME,
					REFUEL_COL_CURRENCYENTERED_ID_NAME)) {
				updSql = "ALTER TABLE " + REFUEL_TABLE_NAME + " ADD "
						+ REFUEL_COL_CURRENCYENTERED_ID_NAME + " INTEGER NULL ";
				db.execSQL(updSql);
				updSql = "UPDATE " + REFUEL_TABLE_NAME + " SET "
						+ REFUEL_COL_CURRENCYENTERED_ID_NAME + " = "
						+ REFUEL_COL_CURRENCY_ID_NAME;
				db.execSQL(updSql);
				updSql = "UPDATE "
						+ REFUEL_TABLE_NAME
						+ " SET "
						+ REFUEL_COL_CURRENCY_ID_NAME
						+ " = "
						+ "(SELECT "
						+ CAR_COL_CURRENCY_ID_NAME
						+ " FROM "
						+ CAR_TABLE_NAME
						+ " WHERE "
						+ sqlConcatTableColumn(CAR_TABLE_NAME,
								GEN_COL_ROWID_NAME)
						+ " = "
						+ sqlConcatTableColumn(REFUEL_TABLE_NAME,
								REFUEL_COL_CAR_ID_NAME) + ") ";
				db.execSQL(updSql);
				Cursor c = db.rawQuery("SELECT COUNT(*) " + "FROM "
						+ REFUEL_TABLE_NAME + " " + "WHERE "
						+ REFUEL_COL_CURRENCY_ID_NAME + " <> "
						+ REFUEL_COL_CURRENCYENTERED_ID_NAME, null);
				if (c.moveToFirst() && c.getInt(0) > 0) {
					SharedPreferences mPreferences = mCtx.getSharedPreferences(
							StaticValues.GLOBAL_PREFERENCE_NAME, 0);
					SharedPreferences.Editor editor = mPreferences.edit();
					editor.putString(
							"UpdateMsg",
							"During the upgrade process we found foreign currencies in refuels.\n"
									+ "Please review and correct the currency conversion rates in your refuels.");
					editor.commit();
				}
				c.close();
			}
			if (!columnExists(db, REFUEL_TABLE_NAME,
					REFUEL_COL_CURRENCYRATE_NAME)) {
				updSql = "ALTER TABLE " + REFUEL_TABLE_NAME + " ADD "
						+ REFUEL_COL_CURRENCYRATE_NAME + " NUMERIC NULL ";
				db.execSQL(updSql);
				updSql = "UPDATE " + REFUEL_TABLE_NAME + " SET "
						+ REFUEL_COL_CURRENCYRATE_NAME + " = 1 ";
				db.execSQL(updSql);
			}
			if (!columnExists(db, REFUEL_TABLE_NAME,
					REFUEL_COL_UOMVOLCONVERSIONRATE_NAME)) {
				updSql = "ALTER TABLE " + REFUEL_TABLE_NAME + " ADD "
						+ REFUEL_COL_UOMVOLCONVERSIONRATE_NAME
						+ " NUMERIC NULL ";
				db.execSQL(updSql);
				updSql = "UPDATE " + REFUEL_TABLE_NAME + " SET "
						+ REFUEL_COL_UOMVOLCONVERSIONRATE_NAME + " = 1 ";
				db.execSQL(updSql);
				updSql = "UPDATE "
						+ REFUEL_TABLE_NAME
						+ " SET "
						+ REFUEL_COL_UOMVOLCONVERSIONRATE_NAME
						+ " = "
						+ "(SELECT "
						+ UOM_CONVERSION_COL_RATE_NAME
						+ " "
						+ "FROM "
						+ UOM_CONVERSION_TABLE_NAME
						+ " "
						+ "WHERE "
						+ UOM_CONVERSION_COL_UOMFROM_ID_NAME
						+ " = "
						+ sqlConcatTableColumn(REFUEL_TABLE_NAME,
								REFUEL_COL_UOMVOLUMEENTERED_ID_NAME)
						+ " "
						+ "AND "
						+ UOM_CONVERSION_COL_UOMTO_ID_NAME
						+ " = "
						+ sqlConcatTableColumn(REFUEL_TABLE_NAME,
								REFUEL_COL_UOMVOLUME_ID_NAME)
						+ "), "
						+ REFUEL_COL_QUANTITY_NAME
						+ " = "
						+ "ROUND( "
						+ REFUEL_COL_QUANTITYENTERED_NAME
						+ " * "
						+ "(SELECT "
						+ UOM_CONVERSION_COL_RATE_NAME
						+ " "
						+ "FROM "
						+ UOM_CONVERSION_TABLE_NAME
						+ " "
						+ "WHERE "
						+ UOM_CONVERSION_COL_UOMFROM_ID_NAME
						+ " = "
						+ sqlConcatTableColumn(REFUEL_TABLE_NAME,
								REFUEL_COL_UOMVOLUMEENTERED_ID_NAME)
						+ " "
						+ "AND "
						+ UOM_CONVERSION_COL_UOMTO_ID_NAME
						+ " = "
						+ sqlConcatTableColumn(REFUEL_TABLE_NAME,
								REFUEL_COL_UOMVOLUME_ID_NAME) + "), 2 ) "
						+ "WHERE " + REFUEL_COL_UOMVOLUME_ID_NAME + " <> "
						+ REFUEL_COL_UOMVOLUMEENTERED_ID_NAME;
				db.execSQL(updSql);
			}

			if (!columnExists(db, EXPENSE_TABLE_NAME,
					EXPENSE_COL_AMOUNTENTERED_NAME)) {
				updSql = "ALTER TABLE " + EXPENSE_TABLE_NAME + " ADD "
						+ EXPENSE_COL_AMOUNTENTERED_NAME + " NUMERIC NULL ";
				db.execSQL(updSql);
				updSql = "UPDATE " + EXPENSE_TABLE_NAME + " SET "
						+ EXPENSE_COL_AMOUNTENTERED_NAME + " = "
						+ EXPENSE_COL_AMOUNT_NAME;
				db.execSQL(updSql);
			}

			if (!columnExists(db, EXPENSE_TABLE_NAME,
					EXPENSE_COL_CURRENCYENTERED_ID_NAME)) {
				updSql = "ALTER TABLE " + EXPENSE_TABLE_NAME + " ADD "
						+ EXPENSE_COL_CURRENCYENTERED_ID_NAME
						+ " INTEGER NULL ";
				db.execSQL(updSql);
				updSql = "UPDATE " + EXPENSE_TABLE_NAME + " SET "
						+ EXPENSE_COL_CURRENCYENTERED_ID_NAME + " = "
						+ EXPENSE_COL_CURRENCY_ID_NAME;
				db.execSQL(updSql);
				updSql = "UPDATE "
						+ EXPENSE_TABLE_NAME
						+ " SET "
						+ EXPENSE_COL_CURRENCY_ID_NAME
						+ " = "
						+ "(SELECT "
						+ CAR_COL_CURRENCY_ID_NAME
						+ " FROM "
						+ CAR_TABLE_NAME
						+ " WHERE "
						+ sqlConcatTableColumn(CAR_TABLE_NAME,
								GEN_COL_ROWID_NAME)
						+ " = "
						+ sqlConcatTableColumn(EXPENSE_TABLE_NAME,
								EXPENSE_COL_CAR_ID_NAME) + ") ";
				db.execSQL(updSql);
				Cursor c = db
						.rawQuery(
								"SELECT COUNT(*) "
										+ "FROM "
										+ EXPENSE_TABLE_NAME
										+ " "
										+ "WHERE "
										+ EXPENSE_COL_CURRENCY_ID_NAME
										+ " <> "
										+ EXPENSE_COL_CURRENCYENTERED_ID_NAME
										+ " "
										+ "AND COALESCE("
										+ EXPENSE_COL_FROMTABLE_NAME
										+ ", 'X') <> '"
										+ StaticValues.EXPENSES_COL_FROMREFUEL_TABLE_NAME
										+ "'", null);
				if (c.moveToFirst() && c.getInt(0) > 0) {
					SharedPreferences mPreferences = mCtx.getSharedPreferences(
							StaticValues.GLOBAL_PREFERENCE_NAME, 0);
					String updateMsg = mPreferences
							.getString("UpdateMsg", null);
					if (updateMsg != null)
						updateMsg = "During the upgrade process we found foreign currencies in refuels and expenses.\n"
								+ "Please review and correct the currency conversion rates in your refuels and expenses.";
					else
						updateMsg = "During the upgrade process we found foreign currencies in expenses.\n"
								+ "Please review and correct the currency conversion rates in your expenses.";

					SharedPreferences.Editor editor = mPreferences.edit();
					editor.putString("UpdateMsg", updateMsg);
					editor.commit();
				}
				c.close();
			}

			if (!columnExists(db, EXPENSE_TABLE_NAME,
					EXPENSE_COL_CURRENCYRATE_NAME)) {
				updSql = "ALTER TABLE " + EXPENSE_TABLE_NAME + " ADD "
						+ EXPENSE_COL_CURRENCYRATE_NAME + " NUMERIC NULL ";
				db.execSQL(updSql);
				updSql = "UPDATE " + EXPENSE_TABLE_NAME + " SET "
						+ EXPENSE_COL_CURRENCYRATE_NAME + " = 1";
				db.execSQL(updSql);
			}

		}

		private void upgradeDbTo300(SQLiteDatabase db, int oldVersion)
				throws SQLException {
			createGPSTrackTables(db);

			FileUtils fu = new FileUtils(mCtx);
			fu.updateTo220();
			SharedPreferences mPreferences = mCtx.getSharedPreferences(
					StaticValues.GLOBAL_PREFERENCE_NAME, 0);
			SharedPreferences.Editor editor = mPreferences.edit();
			editor.putString("UpdateMsg", "VersionChanged");
			editor.commit();
		}

		private void upgradeDbTo310(SQLiteDatabase db, int oldVersion)
				throws SQLException {
			String updSql = "";
			if (!columnExists(db, REFUEL_TABLE_NAME, REFUEL_COL_AMOUNT_NAME)) {
				updSql = "ALTER TABLE " + REFUEL_TABLE_NAME + " ADD "
						+ REFUEL_COL_AMOUNT_NAME + " NUMERIC NULL ";
				db.execSQL(updSql);
				updSql = "UPDATE " + REFUEL_TABLE_NAME + " SET "
						+ REFUEL_COL_AMOUNT_NAME + " = "
						+ REFUEL_COL_QUANTITYENTERED_NAME + " * "
						+ REFUEL_COL_PRICE_NAME;
				db.execSQL(updSql);
			}
			if (!columnExists(db, REFUEL_TABLE_NAME,
					REFUEL_COL_AMOUNTENTERED_NAME)) {
				updSql = "ALTER TABLE " + REFUEL_TABLE_NAME + " ADD "
						+ REFUEL_COL_AMOUNTENTERED_NAME + " NUMERIC NULL ";
				db.execSQL(updSql);
				updSql = "UPDATE " + REFUEL_TABLE_NAME + " SET "
						+ REFUEL_COL_AMOUNTENTERED_NAME + " = "
						+ REFUEL_COL_QUANTITYENTERED_NAME + " * "
						+ REFUEL_COL_PRICEENTERED_NAME;
				db.execSQL(updSql);
			}
		}

		private void upgradeDbTo330(SQLiteDatabase db, int oldVersion)
				throws SQLException {
			String updSql = "";
			createBPartnerTable(db);

			if (!columnExists(db, EXPENSE_TABLE_NAME, EXPENSE_COL_QUANTITY_NAME)) {
				updSql = "ALTER TABLE " + EXPENSE_TABLE_NAME + " ADD "
						+ EXPENSE_COL_QUANTITY_NAME + " NUMERIC NULL ";
				db.execSQL(updSql);
			}
			if (!columnExists(db, EXPENSE_TABLE_NAME, EXPENSE_COL_PRICE_NAME)) {
				updSql = "ALTER TABLE " + EXPENSE_TABLE_NAME + " ADD "
						+ EXPENSE_COL_PRICE_NAME + " NUMERIC NULL ";
				db.execSQL(updSql);
			}
			if (!columnExists(db, EXPENSE_TABLE_NAME,
					EXPENSE_COL_PRICEENTERED_NAME)) {
				updSql = "ALTER TABLE " + EXPENSE_TABLE_NAME + " ADD "
						+ EXPENSE_COL_PRICEENTERED_NAME + " NUMERIC NULL ";
				db.execSQL(updSql);
			}
			if (!columnExists(db, EXPENSE_TABLE_NAME, EXPENSE_COL_UOM_ID_NAME)) {
				updSql = "ALTER TABLE " + EXPENSE_TABLE_NAME + " ADD "
						+ EXPENSE_COL_UOM_ID_NAME + " INTEGER NULL ";
				db.execSQL(updSql);
			}

			if (!columnExists(db, EXPENSE_TABLE_NAME,
					EXPENSE_COL_BPARTNER_ID_NAME)) {
				updSql = "ALTER TABLE " + EXPENSE_TABLE_NAME + " ADD "
						+ EXPENSE_COL_BPARTNER_ID_NAME + " INTEGER NULL ";
				db.execSQL(updSql);
			}
			if (!columnExists(db, EXPENSE_TABLE_NAME,
					EXPENSE_COL_BPARTNER_LOCATION_ID_NAME)) {
				updSql = "ALTER TABLE " + EXPENSE_TABLE_NAME + " ADD "
						+ EXPENSE_COL_BPARTNER_LOCATION_ID_NAME
						+ " INTEGER NULL ";
				db.execSQL(updSql);
			}

			if (!columnExists(db, REFUEL_TABLE_NAME,
					REFUEL_COL_BPARTNER_ID_NAME)) {
				updSql = "ALTER TABLE " + REFUEL_TABLE_NAME + " ADD "
						+ REFUEL_COL_BPARTNER_ID_NAME + " INTEGER NULL ";
				db.execSQL(updSql);
			}
			if (!columnExists(db, REFUEL_TABLE_NAME,
					REFUEL_COL_BPARTNER_LOCATION_ID_NAME)) {
				updSql = "ALTER TABLE " + REFUEL_TABLE_NAME + " ADD "
						+ REFUEL_COL_BPARTNER_LOCATION_ID_NAME
						+ " INTEGER NULL ";
				db.execSQL(updSql);
			}
		}

		private void upgradeDbTo340(SQLiteDatabase db, int oldVersion)
				throws SQLException {
			// createAddOnTable(db);
			createTagTable(db);
			String updSql = "";
			if (!columnExists(db, MILEAGE_TABLE_NAME, MILEAGE_COL_TAG_ID_NAME)) {
				updSql = "ALTER TABLE " + MILEAGE_TABLE_NAME + " ADD "
						+ MILEAGE_COL_TAG_ID_NAME + " INTEGER NULL ";
				db.execSQL(updSql);
			}
			if (!columnExists(db, REFUEL_TABLE_NAME, REFUEL_COL_TAG_ID_NAME)) {
				updSql = "ALTER TABLE " + REFUEL_TABLE_NAME + " ADD "
						+ REFUEL_COL_TAG_ID_NAME + " INTEGER NULL ";

				db.execSQL(updSql);
			}
			if (!columnExists(db, EXPENSE_TABLE_NAME, EXPENSE_COL_TAG_ID_NAME)) {
				updSql = "ALTER TABLE " + EXPENSE_TABLE_NAME + " ADD "
						+ EXPENSE_COL_TAG_ID_NAME + " INTEGER NULL ";
				db.execSQL(updSql);

			}
			if (!columnExists(db, GPSTRACK_TABLE_NAME, GPSTRACK_COL_TAG_ID_NAME)) {
				updSql = "ALTER TABLE " + GPSTRACK_TABLE_NAME + " ADD "
						+ GPSTRACK_COL_TAG_ID_NAME + " INTEGER NULL ";
				db.execSQL(updSql);
			}
		}

		private void upgradeDbTo350(SQLiteDatabase db, int oldVersion)
				throws SQLException {
			AddOnDBObjectDef.createAddOnTable(db);
			AddOnDBObjectDef.createAddOnBKScheduleTable(db);
		}

		private void upgradeDbTo355(SQLiteDatabase db, int oldVersion)
				throws SQLException {
			String updSql = null;
			if (!columnExists(db, EXPENSECATEGORY_TABLE_NAME,
					EXPENSECATEGORY_COL_ISFUEL_NAME)) {
				updSql = "ALTER TABLE " + EXPENSECATEGORY_TABLE_NAME + " ADD "
						+ EXPENSECATEGORY_COL_ISFUEL_NAME
						+ " TEXT DEFAULT 'N' ";
				db.execSQL(updSql);

				updSql = "UPDATE " + EXPENSECATEGORY_TABLE_NAME + " SET "
						+ EXPENSECATEGORY_COL_ISFUEL_NAME + " = 'Y' "
						+ " WHERE " + GEN_COL_ROWID_NAME + " = 1";

				db.execSQL(updSql);
			}
		}

		private void upgradeDbTo356(SQLiteDatabase db, int oldVersion)
				throws SQLException {
			AddOnDBObjectDef.createAddOnSecureBKSettingsTable(db);
		}

		private void upgradeDbTo357(SQLiteDatabase db, int oldVersion) throws SQLException {
			createTaskTables(db);
		}

		private boolean columnExists(SQLiteDatabase db, String table,
				String column) {
			String testSql = "SELECT " + column + " FROM " + table
					+ " WHERE 1=2";
			try {
				db.rawQuery(testSql, null);
				return true;
			} catch (SQLiteException e) {
				return false;
			}
		}

		private void createExpenseCategory(SQLiteDatabase db)
				throws SQLException {
			// expense category
			db.execSQL(EXPENSECATEGORY_TABLE_CREATE_SQL);
			String colPart = "INSERT INTO " + EXPENSECATEGORY_TABLE_NAME
					+ " ( " + GEN_COL_NAME_NAME + ", " + GEN_COL_ISACTIVE_NAME
					+ ", " + GEN_COL_USER_COMMENT_NAME + ", "
					+ EXPENSECATEGORY_COL_ISEXCLUDEFROMMILEAGECOST_NAME + ", "
					+ EXPENSECATEGORY_COL_ISFUEL_NAME + " " + ") ";
			db.execSQL(colPart + "VALUES ( " + "'"
					+ mResource.getString(R.string.DB_ExpCat_FuelName) + "', "
					+ "'Y', " + "'"
					+ mResource.getString(R.string.DB_ExpCat_FuelComment)
					+ "', " + "'N', 'Y' )");
			db.execSQL(colPart + "VALUES ( " + "'"
					+ mResource.getString(R.string.DB_ExpCat_ServiceName)
					+ "', " + "'Y', " + "'"
					+ mResource.getString(R.string.DB_ExpCat_ServiceComment)
					+ "', " + "'N', 'N' )");
			db.execSQL(colPart + "VALUES ( " + "'"
					+ mResource.getString(R.string.DB_ExpCat_InsuranceName)
					+ "', " + "'Y', " + "'"
					+ mResource.getString(R.string.DB_ExpCat_InsuranceComment)
					+ "', " + "'N', 'N' )");
		}

		private void createExpenses(SQLiteDatabase db, boolean isUpdate)
				throws SQLException {
			// expenses table
			db.execSQL(EXPENSE_TABLE_CREATE_SQL);
			if (!isUpdate) {
				return;
			}
			// initialize refuel expenses
			String sql = "INSERT INTO " + EXPENSE_TABLE_NAME + "( "
					+ GEN_COL_NAME_NAME + ", " + GEN_COL_USER_COMMENT_NAME
					+ ", " + GEN_COL_ISACTIVE_NAME + ", "
					+ EXPENSE_COL_CAR_ID_NAME + ", "
					+ EXPENSE_COL_DRIVER_ID_NAME + ", "
					+ EXPENSE_COL_EXPENSECATEGORY_ID_NAME + ", "
					+ EXPENSE_COL_EXPENSETYPE_ID_NAME + ", "
					+ EXPENSE_COL_AMOUNT_NAME + ", "
					+ EXPENSE_COL_CURRENCY_ID_NAME + ", "
					+ EXPENSE_COL_DATE_NAME + ", "
					+ EXPENSE_COL_DOCUMENTNO_NAME + ", "
					+ EXPENSE_COL_INDEX_NAME + ", "
					+ EXPENSE_COL_FROMTABLE_NAME + ", "
					+ EXPENSE_COL_FROMRECORD_ID_NAME + " " + ") " + "SELECT "
					+ GEN_COL_NAME_NAME + ", " + GEN_COL_USER_COMMENT_NAME
					+ ", " + GEN_COL_ISACTIVE_NAME + ", "
					+ REFUEL_COL_CAR_ID_NAME + ", " + REFUEL_COL_DRIVER_ID_NAME
					+ ", " + REFUEL_COL_EXPENSECATEGORY_NAME + ", "
					+ REFUEL_COL_EXPENSETYPE_ID_NAME + ", "
					+ REFUEL_COL_QUANTITY_NAME + " * " + REFUEL_COL_PRICE_NAME
					+ ", " + REFUEL_COL_CURRENCY_ID_NAME + ", "
					+ REFUEL_COL_DATE_NAME + ", " + REFUEL_COL_DOCUMENTNO_NAME
					+ ", " + REFUEL_COL_INDEX_NAME + ", " + "'Refuel' " + ", "
					+ GEN_COL_ROWID_NAME + " " + "FROM " + REFUEL_TABLE_NAME;
			db.execSQL(sql);
		}

		private void createCurrencyTable(SQLiteDatabase db) throws SQLException {
			// currency table name
			db.execSQL(CURRENCY_TABLE_CREATE_SQL);
			// insert some currencies
			String colPart = "INSERT INTO " + CURRENCY_TABLE_NAME + " ( "
					+ GEN_COL_NAME_NAME + ", " + GEN_COL_ISACTIVE_NAME + ", "
					+ GEN_COL_USER_COMMENT_NAME + ", " + CURRENCY_COL_CODE_NAME
					+ ") ";
			db.execSQL(colPart + "VALUES ( " + "'"
					+ mResource.getString(R.string.DB_Curr_EUR) + "', "
					+ "'Y', " + "'" + mResource.getString(R.string.DB_Curr_EUR)
					+ "', " + "'EUR' )");
			db.execSQL(colPart + "VALUES ( " + "'"
					+ mResource.getString(R.string.DB_Curr_USD) + "', "
					+ "'Y', " + "'" + mResource.getString(R.string.DB_Curr_USD)
					+ "', " + "'USD' )");
			db.execSQL(colPart + "VALUES ( " + "'"
					+ mResource.getString(R.string.DB_Curr_HUF) + "', "
					+ "'Y', " + "'" + mResource.getString(R.string.DB_Curr_HUF)
					+ "', " + "'HUF' )");
			db.execSQL(colPart + "VALUES ( " + "'"
					+ mResource.getString(R.string.DB_Curr_RON) + "', "
					+ "'Y', " + "'" + mResource.getString(R.string.DB_Curr_RON)
					+ "', " + "'RON' )");
			db.execSQL(colPart + "VALUES ( " + "'"
					+ mResource.getString(R.string.DB_Curr_AUD) + "', "
					+ "'Y', " + "'" + mResource.getString(R.string.DB_Curr_AUD)
					+ "', " + "'AUD' )");
			db.execSQL(colPart + "VALUES ( " + "'"
					+ mResource.getString(R.string.DB_Curr_CAD) + "', "
					+ "'Y', " + "'" + mResource.getString(R.string.DB_Curr_CAD)
					+ "', " + "'CAD' )");
			db.execSQL(colPart + "VALUES ( " + "'"
					+ mResource.getString(R.string.DB_Curr_CHF) + "', "
					+ "'Y', " + "'" + mResource.getString(R.string.DB_Curr_CHF)
					+ "', " + "'CHF' )");
			db.execSQL(colPart + "VALUES ( " + "'"
					+ mResource.getString(R.string.DB_Curr_CNY) + "', "
					+ "'Y', " + "'" + mResource.getString(R.string.DB_Curr_CNY)
					+ "', " + "'CNY' )");
			db.execSQL(colPart + "VALUES ( " + "'"
					+ mResource.getString(R.string.DB_Curr_GBP) + "', "
					+ "'Y', " + "'" + mResource.getString(R.string.DB_Curr_GBP)
					+ "', " + "'GBP' )");
			db.execSQL(colPart + "VALUES ( " + "'"
					+ mResource.getString(R.string.DB_Curr_JPY) + "', "
					+ "'Y', " + "'" + mResource.getString(R.string.DB_Curr_JPY)
					+ "', " + "'JPY' )");
			db.execSQL(colPart + "VALUES ( " + "'"
					+ mResource.getString(R.string.DB_Curr_RUB) + "', "
					+ "'Y', " + "'" + mResource.getString(R.string.DB_Curr_RUB)
					+ "', " + "'RUB' )");
			db.execSQL(colPart + "VALUES ( " + "'"
					+ mResource.getString(R.string.DB_Curr_INR) + "', "
					+ "'Y', " + "'" + mResource.getString(R.string.DB_Curr_INR)
					+ "', " + "'INR' )");
			// ////////
			db.execSQL(colPart + "VALUES ( " + "'"
					+ mResource.getString(R.string.DB_Curr_ARS) + "', "
					+ "'Y', " + "'" + mResource.getString(R.string.DB_Curr_ARS)
					+ "', " + "'ARS' )");
			db.execSQL(colPart + "VALUES ( " + "'"
					+ mResource.getString(R.string.DB_Curr_BRL) + "', "
					+ "'Y', " + "'" + mResource.getString(R.string.DB_Curr_BRL)
					+ "', " + "'BRL' )");
			db.execSQL(colPart + "VALUES ( " + "'"
					+ mResource.getString(R.string.DB_Curr_BGL) + "', "
					+ "'Y', " + "'" + mResource.getString(R.string.DB_Curr_BGL)
					+ "', " + "'BGL' )");
			db.execSQL(colPart + "VALUES ( " + "'"
					+ mResource.getString(R.string.DB_Curr_XAF) + "', "
					+ "'Y', " + "'" + mResource.getString(R.string.DB_Curr_XAF)
					+ "', " + "'XAF' )");
			db.execSQL(colPart + "VALUES ( " + "'"
					+ mResource.getString(R.string.DB_Curr_COP) + "', "
					+ "'Y', " + "'" + mResource.getString(R.string.DB_Curr_COP)
					+ "', " + "'COP' )");
			db.execSQL(colPart + "VALUES ( " + "'"
					+ mResource.getString(R.string.DB_Curr_HRK) + "', "
					+ "'Y', " + "'" + mResource.getString(R.string.DB_Curr_HRK)
					+ "', " + "'HRK' )");
			db.execSQL(colPart + "VALUES ( " + "'"
					+ mResource.getString(R.string.DB_Curr_CZK) + "', "
					+ "'Y', " + "'" + mResource.getString(R.string.DB_Curr_CZK)
					+ "', " + "'CZK' )");
			db.execSQL(colPart + "VALUES ( " + "'"
					+ mResource.getString(R.string.DB_Curr_DKK) + "', "
					+ "'Y', " + "'" + mResource.getString(R.string.DB_Curr_DKK)
					+ "', " + "'DKK' )");
			db.execSQL(colPart + "VALUES ( " + "'"
					+ mResource.getString(R.string.DB_Curr_EGP) + "', "
					+ "'Y', " + "'" + mResource.getString(R.string.DB_Curr_EGP)
					+ "', " + "'EGP' )");
			db.execSQL(colPart + "VALUES ( " + "'"
					+ mResource.getString(R.string.DB_Curr_EEK) + "', "
					+ "'Y', " + "'" + mResource.getString(R.string.DB_Curr_EEK)
					+ "', " + "'EEK' )");
			db.execSQL(colPart + "VALUES ( " + "'"
					+ mResource.getString(R.string.DB_Curr_DKK) + "', "
					+ "'Y', " + "'" + mResource.getString(R.string.DB_Curr_DKK)
					+ "', " + "'DKK' )");
			db.execSQL(colPart + "VALUES ( " + "'"
					+ mResource.getString(R.string.DB_Curr_GEL) + "', "
					+ "'Y', " + "'" + mResource.getString(R.string.DB_Curr_GEL)
					+ "', " + "'GEL' )");
			db.execSQL(colPart + "VALUES ( " + "'"
					+ mResource.getString(R.string.DB_Curr_HKD) + "', "
					+ "'Y', " + "'" + mResource.getString(R.string.DB_Curr_HKD)
					+ "', " + "'HKD' )");
			db.execSQL(colPart + "VALUES ( " + "'"
					+ mResource.getString(R.string.DB_Curr_ILS) + "', "
					+ "'Y', " + "'" + mResource.getString(R.string.DB_Curr_ILS)
					+ "', " + "'ILS' )");
			db.execSQL(colPart + "VALUES ( " + "'"
					+ mResource.getString(R.string.DB_Curr_KRW) + "', "
					+ "'Y', " + "'" + mResource.getString(R.string.DB_Curr_KRW)
					+ "', " + "'KRW' )");
			db.execSQL(colPart + "VALUES ( " + "'"
					+ mResource.getString(R.string.DB_Curr_MXN) + "', "
					+ "'Y', " + "'" + mResource.getString(R.string.DB_Curr_MXN)
					+ "', " + "'MXN' )");
			db.execSQL(colPart + "VALUES ( " + "'"
					+ mResource.getString(R.string.DB_Curr_NOK) + "', "
					+ "'Y', " + "'" + mResource.getString(R.string.DB_Curr_NOK)
					+ "', " + "'NOK' )");
			db.execSQL(colPart + "VALUES ( " + "'"
					+ mResource.getString(R.string.DB_Curr_PLN) + "', "
					+ "'Y', " + "'" + mResource.getString(R.string.DB_Curr_PLN)
					+ "', " + "'PLN' )");
			db.execSQL(colPart + "VALUES ( " + "'"
					+ mResource.getString(R.string.DB_Curr_SAR) + "', "
					+ "'Y', " + "'" + mResource.getString(R.string.DB_Curr_SAR)
					+ "', " + "'SAR' )");
			db.execSQL(colPart + "VALUES ( " + "'"
					+ mResource.getString(R.string.DB_Curr_ZAR) + "', "
					+ "'Y', " + "'" + mResource.getString(R.string.DB_Curr_ZAR)
					+ "', " + "'ZAR' )");
			db.execSQL(colPart + "VALUES ( " + "'"
					+ mResource.getString(R.string.DB_Curr_SEK) + "', "
					+ "'Y', " + "'" + mResource.getString(R.string.DB_Curr_SEK)
					+ "', " + "'SEK' )");
			db.execSQL(colPart + "VALUES ( " + "'"
					+ mResource.getString(R.string.DB_Curr_TRY) + "', "
					+ "'Y', " + "'" + mResource.getString(R.string.DB_Curr_TRY)
					+ "', " + "'TRY' )");
			db.execSQL(colPart + "VALUES ( " + "'"
					+ mResource.getString(R.string.DB_Curr_UAH) + "', "
					+ "'Y', " + "'" + mResource.getString(R.string.DB_Curr_UAH)
					+ "', " + "'UAH' )");
			db.execSQL(colPart + "VALUES ( " + "'"
					+ mResource.getString(R.string.DB_Curr_AED) + "', "
					+ "'Y', " + "'" + mResource.getString(R.string.DB_Curr_AED)
					+ "', " + "'AED' )");
		}

		private void createCurrencyRateTable(SQLiteDatabase db)
				throws SQLException {
			// create currency rate table
			db.execSQL(CURRENCYRATE_TABLE_CREATE_SQL);
		}
	}

	private void createIndexes(SQLiteDatabase db) {
		db.execSQL("CREATE INDEX IF NOT EXISTS " + GPSTRACK_TABLE_NAME
				+ "_IX1 " + "ON " + GPSTRACK_TABLE_NAME + " ("
				+ GPSTRACK_COL_CAR_ID_NAME + ")");
		db.execSQL("CREATE INDEX IF NOT EXISTS " + GPSTRACK_TABLE_NAME
				+ "_IX2 " + "ON " + GPSTRACK_TABLE_NAME + " ("
				+ GPSTRACK_COL_DRIVER_ID_NAME + ")");
		db.execSQL("CREATE INDEX IF NOT EXISTS " + GPSTRACK_TABLE_NAME
				+ "_IX3 " + "ON " + GPSTRACK_TABLE_NAME + " ("
				+ GPSTRACK_COL_MILEAGE_ID_NAME + " DESC )");
		db.execSQL("CREATE INDEX IF NOT EXISTS " + GPSTRACK_TABLE_NAME
				+ "_IX4 " + "ON " + GPSTRACK_TABLE_NAME + " ("
				+ GPSTRACK_COL_DATE_NAME + " DESC )");
		db.execSQL("CREATE INDEX IF NOT EXISTS " + GPSTRACKDETAIL_TABLE_NAME
				+ "_IX1 " + "ON " + GPSTRACKDETAIL_TABLE_NAME + " ("
				+ GPSTRACKDETAIL_COL_GPSTRACK_ID_NAME + ")");
		// create indexes on mileage table
		db.execSQL("CREATE INDEX IF NOT EXISTS " + MILEAGE_TABLE_NAME + "_IX1 "
				+ "ON " + MILEAGE_TABLE_NAME + " (" + MILEAGE_COL_CAR_ID_NAME
				+ ")");
		db.execSQL("CREATE INDEX IF NOT EXISTS " + MILEAGE_TABLE_NAME + "_IX2 "
				+ "ON " + MILEAGE_TABLE_NAME + " ("
				+ MILEAGE_COL_DRIVER_ID_NAME + ")");
		db.execSQL("CREATE INDEX IF NOT EXISTS " + MILEAGE_TABLE_NAME + "_IX3 "
				+ "ON " + MILEAGE_TABLE_NAME + " (" + MILEAGE_COL_DATE_NAME
				+ " DESC )");
		db.execSQL("CREATE INDEX IF NOT EXISTS " + MILEAGE_TABLE_NAME + "_IX4 "
				+ "ON " + MILEAGE_TABLE_NAME + " ("
				+ MILEAGE_COL_INDEXSTOP_NAME + " DESC )");
		db.execSQL("CREATE INDEX IF NOT EXISTS " + MILEAGE_TABLE_NAME + "_IX5 "
				+ "ON " + MILEAGE_TABLE_NAME + " (" + GEN_COL_USER_COMMENT_NAME
				+ ")");

		db.execSQL("CREATE INDEX IF NOT EXISTS " + REFUEL_TABLE_NAME + "_IX1 "
				+ "ON " + REFUEL_TABLE_NAME + " (" + MILEAGE_COL_DATE_NAME
				+ " DESC )");
		db.execSQL("CREATE INDEX IF NOT EXISTS " + REFUEL_TABLE_NAME + "_IX2 "
				+ "ON " + REFUEL_TABLE_NAME + " (" + GEN_COL_USER_COMMENT_NAME
				+ ")");
		db.execSQL("CREATE INDEX IF NOT EXISTS " + REFUEL_TABLE_NAME + "_IX3 "
				+ "ON " + REFUEL_TABLE_NAME + " ("
				+ REFUEL_COL_ISFULLREFUEL_NAME + ")");
		db.execSQL("CREATE INDEX IF NOT EXISTS " + REFUEL_TABLE_NAME + "_IX4 "
				+ "ON " + REFUEL_TABLE_NAME + " (" + REFUEL_COL_INDEX_NAME
				+ ")");
		
		db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS " + TASK_CAR_TABLE_NAME + "_UK1 "
				+ "ON " + TASK_CAR_TABLE_NAME + " (" + TASK_CAR_COL_CAR_ID_NAME + ", " + TASK_CAR_COL_TASK_ID_NAME + ")");
		
		db.execSQL("CREATE INDEX IF NOT EXISTS " + TODO_TABLE_NAME + "_IX1 "
				+ "ON " + TODO_TABLE_NAME + " (" + TODO_COL_TASK_ID_NAME + ")");
	}

	public boolean backupDb(String bkName, String bkPrefix) {
		boolean retVal;
		String fromFile = mDb.getPath();
		bkFolder = StaticValues.BACKUP_FOLDER;
		bkFileName = Utils.appendDateTime(
				bkPrefix == null ? StaticValues.BACKUP_PREFIX : bkPrefix, true,
				true, true)
				+ StaticValues.BACKUP_SUFIX;
		if (bkName == null)
			bkFolder = bkFolder + bkFileName;
		else
			bkFolder = bkFolder + bkName + StaticValues.BACKUP_SUFIX;

		mDb.close();
		FileUtils fu = new FileUtils(mCtx);
		fu.createFolderIfNotExists(FileUtils.BACKUP_FOLDER);
		retVal = fu.copyFile(fromFile, bkFolder, false);
		if (retVal == false) {
			lastErrorMessage = fu.lastError;
		} else { // send backup file as email att. if secure backup subscription
					// exists
			try {
				MainDbAdapter db = new MainDbAdapter(mCtx);
				boolean subsValid = ServiceSubscription.isSubscriptionValid(db,
						AddOnStaticValues.SECURE_BACKUP_ID);
				db.close();
				if (subsValid) {
					AlarmManager am = (AlarmManager) mCtx
							.getSystemService(Context.ALARM_SERVICE);
					Intent intent = new Intent(mCtx, FileMailer.class);
					intent.putExtra("bkFile", bkFolder);
					intent.putExtra("attachName", bkFileName);
					PendingIntent pIntent = PendingIntent.getService(mCtx, 0,
							intent, PendingIntent.FLAG_CANCEL_CURRENT);
					am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
							pIntent);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		open();
		return retVal;
	}

	public boolean restoreDb(String restoreFile) {
		boolean retVal;
		String toFile = mDb.getPath();
		mDb.close();
		FileUtils fu = new FileUtils(mCtx);
		retVal = fu.copyFile(StaticValues.BACKUP_FOLDER + restoreFile, toFile,
				true);
		if (retVal == false) {
			lastErrorMessage = fu.lastError;
		}
		return retVal;
	}
}
