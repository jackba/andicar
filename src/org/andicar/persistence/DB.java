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

import org.andicar2.activity.R;
import org.andicar.service.FileMailer;
import org.andicar.utils.AndiCarExceptionHandler;
import org.andicar.utils.StaticValues;
import org.andicar.utils.Utils;

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
	public static final String TABLE_NAME_DRIVER = "DEF_DRIVER";
	// cars
	public static final String TABLE_NAME_CAR = "DEF_CAR";
	// uoms
	public static final String TABLE_NAME_UOM = "DEF_UOM";
	// expense types
	public static final String TABLE_NAME_EXPENSETYPE = "DEF_EXPENSETYPE";
	// uom conversion rates
	public static final String TABLE_NAME_UOMCONVERSION = "DEF_UOMCONVERTIONRATE";
	// currencies
	public static final String TABLE_NAME_CURRENCY = "DEF_CURRENCY";
	// mileages
	public static final String TABLE_NAME_MILEAGE = "CAR_MILEAGE";
	// refuel
	public static final String TABLE_NAME_REFUEL = "CAR_REFUEL";
	// expense categories (eg. Refuel, Service, Insurance, etc.
	public static final String TABLE_NAME_EXPENSECATEGORY = "DEF_EXPENSECATEGORY";
	// car expenses
	public static final String TABLE_NAME_EXPENSE = "CAR_EXPENSE";
	// currency rate
	public static final String TABLE_NAME_CURRENCYRATE = "DEF_CURRENCYRATE";
	// gps track table
	public static final String TABLE_NAME_GPSTRACK = "GPS_TRACK";
	public static final String TABLE_NAME_GPSTRACKDETAIL = "GPS_TRACKDETAIL";

	// business partner table
	public static final String TABLE_NAME_BPARTNER = "DEF_BPARTNER";
	// business partner locations table
	public static final String TABLE_NAME_BPARTNERLOCATION = "DEF_BPARTNERLOCATION";

	// tags table
	public static final String TABLE_NAME_TAG = "DEF_TAG";

	// tasks/reminders/todo tables
	public static final String TABLE_NAME_TASKTYPE = "DEF_TASKTYPE";
	public static final String TABLE_NAME_TASK = "DEF_TASK";
	public static final String TABLE_NAME_TASK_CAR = "TASK_CAR";
	public static final String TABLE_NAME_TODO = "TASK_TODO";
	// link table between cars and reimbursement rates
	public static final String TABLE_NAME_REIMBURSEMENT_CAR_RATES = "REIMBURSEMENT_CAR";

	// column names. Some is general (GEN_) some is particular
	// generic columns must be first and must be created for ALL TABLES
	public static final String COL_NAME_GEN_ROWID = "_id";
	public static final String COL_NAME_GEN_NAME = "Name";
	public static final String COL_NAME_GEN_ISACTIVE = "IsActive";
	public static final String COL_NAME_GEN_USER_COMMENT = "UserComment";
	// driver specific column names
	public static final String COL_NAME_DRIVER__LICENSE_NO = "LicenseNo";
	// car specific column names
	public static final String COL_NAME_CAR__MODEL = "Model";
	public static final String COL_NAME_CAR__REGISTRATIONNO = "RegistrationNo";
	public static final String COL_NAME_CAR__INDEXSTART = "IndexStart";
	public static final String COL_NAME_CAR__INDEXCURRENT = "IndexCurrent";
	public static final String COL_NAME_CAR__UOMLENGTH_ID = TABLE_NAME_UOM
			+ "_Length_ID";
	public static final String COL_NAME_CAR__UOMVOLUME_ID = TABLE_NAME_UOM
			+ "_Volume_ID";
	public static final String COL_NAME_CAR__CURRENCY_ID = TABLE_NAME_CURRENCY
			+ "_ID";

	// uom specific column names
	public static final String COL_NAME_UOM__CODE = "Code";
	public static final String COL_NAME_UOM__UOMTYPE = "UOMType"; // V - Volume
																	// or L -
																	// Length
	// uom conversion specific column names
	public static final String COL_NAME_UOMCONVERSION__UOMFROM_ID = TABLE_NAME_UOM
			+ "_From_ID";
	public static final String COL_NAME_UOMCONVERSION__UOMTO_ID = TABLE_NAME_UOM
			+ "_To_ID";
	public static final String COL_NAME_UOMCONVERSION__RATE = "ConvertionRate";
	// mileage specific columns
	public static final String COL_NAME_MILEAGE__DATE = "Date";
	public static final String COL_NAME_MILEAGE__CAR_ID = TABLE_NAME_CAR
			+ "_ID";
	public static final String COL_NAME_MILEAGE__DRIVER_ID = TABLE_NAME_DRIVER
			+ "_ID";
	public static final String COL_NAME_MILEAGE__INDEXSTART = "IndexStart";
	public static final String COL_NAME_MILEAGE__INDEXSTOP = "IndexStop";
	public static final String COL_NAME_MILEAGE__UOMLENGTH_ID = TABLE_NAME_UOM
			+ "_Length_ID";
	public static final String COL_NAME_MILEAGE__EXPENSETYPE_ID = TABLE_NAME_EXPENSETYPE
			+ "_ID";
	public static final String COL_NAME_MILEAGE__GPSTRACKLOG = "GPSTrackLog";
	public static final String COL_NAME_MILEAGE__TAG_ID = TABLE_NAME_TAG
			+ "_ID";
	public static final String COL_NAME_MILEAGE__REIMBURSEMENT_RATE = "ReimbursementRate";
	public static final String COL_NAME_MILEAGE__REIMBURSEMENT_VALUE = "ReimbursementValue";
	// currencies
	public static final String COL_NAME_CURRENCY__CODE = "Code";
	// refuel
	public static final String COL_NAME_REFUEL__CAR_ID = TABLE_NAME_CAR + "_ID";
	public static final String COL_NAME_REFUEL__DRIVER_ID = TABLE_NAME_DRIVER
			+ "_ID";
	public static final String COL_NAME_REFUEL__EXPENSETYPE_ID = TABLE_NAME_EXPENSETYPE
			+ "_ID";
	public static final String COL_NAME_REFUEL__INDEX = "CarIndex";
	public static final String COL_NAME_REFUEL__QUANTITY = "Quantity";
	public static final String COL_NAME_REFUEL__UOMVOLUME_ID = TABLE_NAME_UOM
			+ "_Volume_ID";
	public static final String COL_NAME_REFUEL__PRICE = "Price";
	public static final String COL_NAME_REFUEL__CURRENCY_ID = TABLE_NAME_CURRENCY
			+ "_ID";
	public static final String COL_NAME_REFUEL__DATE = "Date";
	public static final String COL_NAME_REFUEL__DOCUMENTNO = "DocumentNo";
	public static final String COL_NAME_REFUEL__EXPENSECATEGORY_ID = TABLE_NAME_EXPENSECATEGORY
			+ "_ID";
	public static final String COL_NAME_REFUEL__ISFULLREFUEL = "IsFullRefuel";
	public static final String COL_NAME_REFUEL__QUANTITYENTERED = "QuantityEntered";
	public static final String COL_NAME_REFUEL__UOMVOLUMEENTERED_ID = TABLE_NAME_UOM
			+ "_EnteredVolume_ID";
	public static final String COL_NAME_REFUEL__PRICEENTERED = "PriceEntered";
	public static final String COL_NAME_REFUEL__CURRENCYENTERED_ID = TABLE_NAME_CURRENCY
			+ "_Entered_ID";
	public static final String COL_NAME_REFUEL__CURRENCYRATE = "CurrencyRate"; // CurrencyEntered
																				// ->
																				// Car
																				// Base
																				// Currency
	public static final String COL_NAME_REFUEL__UOMVOLCONVERSIONRATE = "UOMVolumeConversionRate";
	public static final String COL_NAME_REFUEL__AMOUNT = "Amount";
	public static final String COL_NAME_REFUEL__AMOUNTENTERED = "AmountEntered";
	public static final String COL_NAME_REFUEL__BPARTNER_ID = TABLE_NAME_BPARTNER
			+ "_ID";
	public static final String COL_NAME_REFUEL__BPARTNER_LOCATION_ID = TABLE_NAME_BPARTNERLOCATION
			+ "_ID";
	public static final String COL_NAME_REFUEL__TAG_ID = TABLE_NAME_TAG + "_ID";

	// expense type
	public static final String COL_NAME_EXPENSETYPE__ISCALCULATEREIMBURSEMENT = "IsCalculateReimbursement";

	// expense category
	public static final String COL_NAME_EXPENSECATEGORY__ISEXCLUDEFROMMILEAGECOST = "IsExcludefromMileagecost";
	public static final String COL_NAME_EXPENSECATEGORY__ISFUEL = "IsFuel";
	// car expenses
	public static final String COL_NAME_EXPENSE__CAR_ID = TABLE_NAME_CAR
			+ "_ID";
	public static final String COL_NAME_EXPENSE__DRIVER_ID = TABLE_NAME_DRIVER
			+ "_ID";
	public static final String COL_NAME_EXPENSE__EXPENSECATEGORY_ID = TABLE_NAME_EXPENSECATEGORY
			+ "_ID";
	public static final String COL_NAME_EXPENSE__EXPENSETYPE_ID = TABLE_NAME_EXPENSETYPE
			+ "_ID";
	public static final String COL_NAME_EXPENSE__AMOUNT = "Amount";
	public static final String COL_NAME_EXPENSE__CURRENCY_ID = TABLE_NAME_CURRENCY
			+ "_ID";
	public static final String COL_NAME_EXPENSE__DATE = "Date";
	public static final String COL_NAME_EXPENSE__DOCUMENTNO = "DocumentNo";
	public static final String COL_NAME_EXPENSE__INDEX = "CarIndex";
	public static final String COL_NAME_EXPENSE__FROMTABLE = "FromTable";
	public static final String COL_NAME_EXPENSE__FROMRECORD_ID = "FromRecordId";
	public static final String COL_NAME_EXPENSE__AMOUNTENTERED = "AmountEntered";
	public static final String COL_NAME_EXPENSE__CURRENCYENTERED_ID = TABLE_NAME_CURRENCY
			+ "_Entered_ID";
	public static final String COL_NAME_EXPENSE__CURRENCYRATE = "CurrencyRate"; // CurrencyEntered
																				// ->
																				// Car
																				// Base
																				// Currency
	public static final String COL_NAME_EXPENSE__QUANTITY = "Quantity";
	public static final String COL_NAME_EXPENSE__PRICE = "Price";
	public static final String COL_NAME_EXPENSE__PRICEENTERED = "PriceEntered";
	public static final String COL_NAME_EXPENSE__UOM_ID = TABLE_NAME_UOM
			+ "_ID";
	public static final String COL_NAME_EXPENSE__BPARTNER_ID = TABLE_NAME_BPARTNER
			+ "_ID";
	public static final String COL_NAME_EXPENSE__BPARTNER_LOCATION_ID = TABLE_NAME_BPARTNERLOCATION
			+ "_ID";
	public static final String COL_NAME_EXPENSE__TAG_ID = TABLE_NAME_TAG
			+ "_ID";

	// currency rate
	public static final String COL_NAME_CURRENCYRATE__FROMCURRENCY_ID = TABLE_NAME_CURRENCYRATE
			+ "_From_ID";
	public static final String COL_NAME_CURRENCYRATE__TOCURRENCY_ID = TABLE_NAME_CURRENCYRATE
			+ "_To_ID";
	public static final String COL_NAME_CURRENCYRATE__RATE = "Rate";
	public static final String COL_NAME_CURRENCYRATE__INVERSERATE = "InverseRate";

	// gps track
	public static final String COL_NAME_GPSTRACK__CAR_ID = TABLE_NAME_CAR
			+ "_ID";
	public static final String COL_NAME_GPSTRACK__DRIVER_ID = TABLE_NAME_DRIVER
			+ "_ID";
	public static final String COL_NAME_GPSTRACK__MILEAGE_ID = TABLE_NAME_MILEAGE
			+ "_ID";
	public static final String COL_NAME_GPSTRACK__DATE = "Date";
	public static final String COL_NAME_GPSTRACK__MINACCURACY = "MinAccuracy";
	public static final String COL_NAME_GPSTRACK__AVGACCURACY = "AvgAccuracy";
	public static final String COL_NAME_GPSTRACK__MAXACCURACY = "MaxAccuracy";
	public static final String COL_NAME_GPSTRACK__MINALTITUDE = "MinAltitude";
	public static final String COL_NAME_GPSTRACK__MAXALTITUDE = "MaxAltitude";
	public static final String COL_NAME_GPSTRACK__TOTALTIME = "TotalTime"; // in
																			// seconds
	public static final String COL_NAME_GPSTRACK__MOVINGTIME = "MovingTime"; // in
																				// seconds
	public static final String COL_NAME_GPSTRACK__DISTANCE = "Distance";
	public static final String COL_NAME_GPSTRACK__MAXSPEED = "MaxSpeed";
	public static final String COL_NAME_GPSTRACK__AVGSPEED = "AvgSpeed";
	public static final String COL_NAME_GPSTRACK__AVGMOVINGSPEED = "AvgMovingSpeed";
	public static final String COL_NAME_GPSTRACK__TOTALTRACKPOINTS = "TotalTrackPoints";
	public static final String COL_NAME_GPSTRACK__INVALIDTRACKPOINTS = "InvalidTrackPoints";
	public static final String COL_NAME_GPSTRACK__TAG_ID = TABLE_NAME_TAG
			+ "_ID";
	public static final String COL_NAME_GPSTRACK__TOTALPAUSETIME = "TotalPauseTime"; // in
																						// seconds

	// gps track detail
	public static final String COL_NAME_GPSTRACKDETAIL__GPSTRACK_ID = TABLE_NAME_GPSTRACK
			+ "_ID";
	public static final String COL_NAME_GPSTRACKDETAIL__FILE = "File";
	public static final String COL_NAME_GPSTRACKDETAIL__FILEFORMAT = "Format"; // see
																				// StaticValues.gpsTrackFormat...
	// business partner location
	public static final String COL_NAME_BPARTNERLOCATION__BPARTNER_ID = TABLE_NAME_BPARTNER
			+ "_ID";
	public static final String COL_NAME_BPARTNERLOCATION__ADDRESS = "Address";
	public static final String COL_NAME_BPARTNERLOCATION__POSTAL = "Postal";
	public static final String COL_NAME_BPARTNERLOCATION__CITY = "City";
	public static final String COL_NAME_BPARTNERLOCATION__REGION = "Region";
	public static final String COL_NAME_BPARTNERLOCATION__COUNTRY = "Country";
	public static final String COL_NAME_BPARTNERLOCATION__PHONE = "Phone";
	public static final String COL_NAME_BPARTNERLOCATION__PHONE2 = "Phone2";
	public static final String COL_NAME_BPARTNERLOCATION__FAX = "Fax";
	public static final String COL_NAME_BPARTNERLOCATION__EMAIL = "Email";
	public static final String COL_NAME_BPARTNERLOCATION__CONTACTPERSON = "ContactPerson";

	public static final String COL_NAME_TASK__TASKTYPE_ID = TABLE_NAME_TASKTYPE
			+ "_ID";
	/**
	 * Time|Mileage|Both (StaticValues.TASK_SCHEDULED_FOR_{TIME|MILEAGE|BOTH})
	 */
	public static final String COL_NAME_TASK__SCHEDULEDFOR = "ScheduledFor";
	/**
	 * recurrent or one time task {Y|N}
	 */
	public static final String COL_NAME_TASK__ISRECURRENT = "IsRecurrent";
	public static final String COL_NAME_TASK__ISDIFFERENTSTARTINGTIME = "IsDifferentSTime";
	/**
	 * recurrency (every X days/weeks/months/years depending on
	 * TASK_COL_TIMEFREQUENCYTYPE_NAME)
	 */
	public static final String COL_NAME_TASK__TIMEFREQUENCY = "TimeFrequency";
	/**
	 * Type integer<br>
	 * Frequency type: 0 = One time, 1 = Daily, 2 = Weekly, 3 = Monthly, 4 =
	 * Yearly (StaticValues.TASK_TIMEFREQUENCYTYPE_...
	 */
	public static final String COL_NAME_TASK__TIMEFREQUENCYTYPE = "TimeFrequencyType";

	/**
	 * Type Date<br>
	 * <br>
	 * If IsRecurent = 'Y': <li>The starting date<br>
	 * <br>
	 * If IsRecurent = 'N': <li>The run date <li>1970-mm-01 hh:mm if
	 * TASK_COL_TIMEFREQUENCYTYPE_NAME = Month and if is LastDay of the month
	 */
	public static final String COL_NAME_TASK__STARTINGTIME = "StartingTime";
	/**
	 * Type integer <br>
	 * <li>No. of days to start reminders if TASK_COL_TIMEFREQUENCYTYPE_NAME !=
	 * Day <li>No. of minutes to start reminders if
	 * TASK_COL_TIMEFREQUENCYTYPE_NAME == Day
	 */
	public static final String COL_NAME_TASK__TIMEREMINDERSTART = "TimeReminderStart";
	/**
	 * If IsRecurent = 'Y': <li>Run on every mileage<br>
	 * else <li>Run on mileage
	 */
	public static final String COL_NAME_TASK__RUNMILEAGE = "RunMileage";
	/**
	 * No. of km|mi to start reminders
	 */
	public static final String COL_NAME_TASK__MILEAGEREMINDERSTART = "MileageReminderStart";
	/**
	 * How many todos will be generated for this task
	 */
	public static final String COL_NAME_TASK__TODOCOUNT = "TodoCount";

	/**
	 * the task from where this todo come
	 */
	public static final String COL_NAME_TODO__TASK_ID = TABLE_NAME_TASK + "_ID";
	/**
	 * the linked car to the task (if exist)
	 */
	public static final String COL_NAME_TODO__CAR_ID = TABLE_NAME_CAR + "_ID";
	/**
	 * the due date based on start time and recurency settings
	 */
	public static final String COL_NAME_TODO__DUEDATE = "DueDate";
	/**
	 * the due mileage based on starting mileage and recurency mileage
	 */
	public static final String COL_NAME_TODO__DUEMILEAGE = "DueMileage";
	/**
	 * show the notification at this date
	 */
	public static final String COL_NAME_TODO__NOTIFICATIONDATE = "NotificationDate";
	/**
	 * show the notification at this mileage
	 */
	public static final String COL_NAME_TODO__NOTIFICATIONMILEAGE = "NotificationMileage";
	/**
	 * if this todo is done {Y|N}
	 */
	public static final String COL_NAME_TODO__ISDONE = "IsDone";
	/**
	 * the date when this todo was done
	 */
	public static final String COL_NAME_TODO__DONEDATE = "DoneDate";
	/**
	 * stop the notification for this todo, even if is not done
	 */
	public static final String COL_NAME_TODO__ISSTOPNOTIFICATION = "IsStopNotification";

	public static final String COL_NAME_TASK_CAR__TASK_ID = TABLE_NAME_TASK
			+ "_ID";
	public static final String COL_NAME_TASK_CAR__CAR_ID = TABLE_NAME_CAR
			+ "_ID";
	public static final String COL_NAME_TASK_CAR__FIRSTRUN_DATE = "FirstRunDate";
	public static final String COL_NAME_TASK_CAR__FIRSTRUN_MILEAGE = "FirstRunMileage";

	public static final String COL_NAME_REIMBURSEMENT_CAR_RATES__CAR_ID = TABLE_NAME_CAR
			+ "_ID";
	public static final String COL_NAME_REIMBURSEMENT_CAR_RATES__EXPENSETYPE_ID = TABLE_NAME_EXPENSE
			+ "_ID";
	public static final String COL_NAME_REIMBURSEMENT_CAR_RATES__RATE = "Rate";

	// column positions. Some is general (GEN_) some is particular
	// generic columns must be first and must be created for ALL TABLES
	public static final int COL_POS_GEN_ROWID = 0;
	public static final int COL_POS_GEN_NAME = 1;
	public static final int COL_POS_GEN_ISACTIVE = 2;
	public static final int COL_POS_GEN_USER_COMMENT = 3;

	// driver specidfic column positions
	public static final int COL_POS_DRIVER__LICENSE_NO = 4;
	// car specific column positions
	public static final int COL_POS_CAR__MODEL = 4;
	public static final int COL_POS_CAR__REGISTRATIONNO = 5;
	public static final int COL_POS_CAR__INDEXSTART = 6;
	public static final int COL_POS_CAR__INDEXCURRENT = 7;
	public static final int COL_POS_CAR__UOMLENGTH_ID = 8;
	public static final int COL_POS_CAR__UOMVOLUME_ID = 9;
	public static final int COL_POS_CAR__CURRENCY_ID = 10;

	// uom specific column positions
	public static final int COL_POS_UOM__CODE = 4;
	public static final int COL_POS_UOM__UOMTYPE = 5;
	// uom convertion specific column positions
	public static final int COL_POS_UOMCONVERSION__UOMFROM_ID = 4;
	public static final int COL_POS_UOMCONVERSION__UOMTO_ID = 5;
	public static final int COL_POS_UOMCONVERSION__RATE = 6;
	// mileage specific column positions
	public static final int COL_POS_MILEAGE__DATE = 4;
	public static final int COL_POS_MILEAGE__CAR_ID = 5;
	public static final int COL_POS_MILEAGE__DRIVER_ID = 6;
	public static final int COL_POS_MILEAGE__INDEXSTART = 7;
	public static final int COL_POS_MILEAGE__INDEXSTOP = 8;
	public static final int COL_POS_MILEAGE__UOMLENGTH_ID = 9;
	public static final int COL_POS_MILEAGE__EXPENSETYPE_ID = 10;
	public static final int COL_POS_MILEAGE__GPSTRACKLOG = 11;
	public static final int COL_POS_MILEAGE__TAG_ID = 12;
	public static final int COL_POS_MILEAGE__REIMBURSEMENT_RATE = 13;
	public static final int COL_POS_MILEAGE__REIMBURSEMENT_VALUE = 14;

	// currencies
	public static int COL_POS_CURRENCY__CODE = 4;
	// refuel
	public static final int COL_POS_REFUEL__CAR_ID = 4;
	public static final int COL_POS_REFUEL__DRIVER_ID = 5;
	public static final int COL_POS_REFUEL__EXPENSETYPE_ID = 6;
	public static final int COL_POS_REFUEL__INDEX = 7;
	public static final int COL_POS_REFUEL__QUANTITY = 8;
	public static final int COL_POS_REFUEL__UOMVOLUME_ID = 9;
	public static final int COL_POS_REFUEL__PRICE = 10;
	public static final int COL_POS_REFUEL__CURRENCY_ID = 11;
	public static final int COL_POS_REFUEL__DATE = 12;
	public static final int COL_POS_REFUEL__DOCUMENTNO = 13;
	public static final int COL_POS_REFUEL__EXPENSECATEGORY_ID = 14;
	public static final int COL_POS_REFUEL__ISFULLREFUEL = 15;
	public static final int COL_POS_REFUEL__QUANTITYENTERED = 16;
	public static final int COL_POS_REFUEL__UOMVOLUMEENTERED_ID = 17;
	public static final int COL_POS_REFUEL__PRICEENTERED = 18;
	public static final int COL_POS_REFUEL__CURRENCYENTERED_ID = 19;
	public static final int COL_POS_REFUEL__CURRENCYRATE = 20;
	public static final int COL_POS_REFUEL__UOMVOLCONVERSIONRATE = 21;
	public static final int COL_POS_REFUEL__AMOUNT = 22;
	public static final int COL_POS_REFUEL__AMOUNTENTERED = 23;
	public static final int COL_POS_REFUEL__BPARTNER_ID = 24;
	public static final int COL_POS_REFUEL__BPARTNER_LOCATION_ID = 25;
	public static final int COL_POS_REFUEL__TAG_ID = 26;

	public static final int COL_POS_EXPENSETYPE__ISCALCULATEREIMBURSEMENT = 4;

	// expense category
	public static final int COL_POS_EXPENSECATEGORY__ISEXCLUDEFROMMILEAGECOST = 4;
	public static final int COL_POS_EXPENSECATEGORY__ISFUEL = 5;
	// car expenses
	public static final int COL_POS_EXPENSE__CAR_ID = 4;
	public static final int COL_POS_EXPENSE__DRIVER_ID = 5;
	public static final int COL_POS_EXPENSE__EXPENSECATEGORY = 6;
	public static final int COL_POS_EXPENSE__EXPENSETYPE_ID = 7;
	public static final int COL_POS_EXPENSE__AMOUNT = 8;
	public static final int COL_POS_EXPENSE__CURRENCY_ID = 9;
	public static final int COL_POS_EXPENSE__DATE = 10;
	public static final int COL_POS_EXPENSE__DOCUMENTNO = 11;
	public static final int COL_POS_EXPENSE__INDEX = 12;
	public static final int COL_POS_EXPENSE__FROMTABLE = 13;
	public static final int COL_POS_EXPENSE__FROMRECORD = 14;
	public static final int COL_POS_EXPENSE__AMOUNTENTERED = 15;
	public static final int COL_POS_EXPENSE__CURRENCYENTERED_ID = 16;
	public static final int COL_POS_EXPENSE__CURRENCYRATE = 17;
	public static final int COL_POS_EXPENSE__QUANTITY = 18;
	public static final int COL_POS_EXPENSE__PRICE = 19;
	public static final int COL_POS_EXPENSE__PRICEENTERED = 20;
	public static final int COL_POS_EXPENSE__UOM_ID = 21;
	public static final int COL_POS_EXPENSE__BPARTNER_ID = 22;
	public static final int COL_POS_EXPENSE__BPARTNER_LOCATION_ID = 23;
	public static final int COL_POS_EXPENSE__TAG_ID = 24;

	// currency rate
	public static final int COL_POS_CURRENCYRATE__FROMCURRENCY_ID = 4;
	public static final int COL_POS_CURRENCYRATE__TOCURRENCY_ID = 5;
	public static final int COL_POS_CURRENCYRATE__RATE = 6;
	public static final int COL_POS_CURRENCYRATE__INVERSERATE = 7;

	// gps track
	public static final int COL_POS_GPSTRACK__CAR_ID = 4;
	public static final int COL_POS_GPSTRACK__DRIVER_ID = 5;
	public static final int COL_POS_GPSTRACK__MILEAGE_ID = 6;
	public static final int COL_POS_GPSTRACK__DATE = 7;
	public static final int COL_POS_GPSTRACK__MINACCURACY = 8;
	public static final int COL_POS_GPSTRACK__AVGACCURACY = 9;
	public static final int COL_POS_GPSTRACK__MAXACCURACY = 10;
	public static final int COL_POS_GPSTRACK__MINALTITUDE = 11;
	public static final int COL_POS_GPSTRACK__MAXALTITUDE = 12;
	public static final int COL_POS_GPSTRACK__TOTALTIME = 13;
	public static final int COL_POS_GPSTRACK__MOVINGTIME = 14;
	public static final int COL_POS_GPSTRACK__DISTANCE = 15;
	public static final int COL_POS_GPSTRACK__MAXSPEED = 16;
	public static final int COL_POS_GPSTRACK__AVGSPEED = 17;
	public static final int COL_POS_GPSTRACK__AVGMOVINGSPEED = 18;
	public static final int COL_POS_GPSTRACK__TOTALTRACKPOINTS = 19;
	public static final int COL_POS_GPSTRACK__INVALIDTRACKPOINTS = 20;
	public static final int COL_POS_GPSTRACK__TAG_ID = 21;
	public static final int COL_POS_GPSTRACK__TOTALPAUSETIME = 22;

	// gps track detail
	public static final int COL_POS_GPSTRACKDETAIL__GPSTRACK_ID = 4;
	public static final int COL_POS_GPSTRACKDETAIL__FILE = 5;
	public static final int COL_POS_GPSTRACKDETAIL__FILEFORMAT = 6;
	// business partner location
	public static final int COL_POS_BPARTNERLOCATION__BPARTNER_ID = 4;
	public static final int COL_POS_BPARTNERLOCATION__ADDRESS = 5;
	public static final int COL_POS_BPARTNERLOCATION__POSTAL = 6;
	public static final int COL_POS_BPARTNERLOCATION__CITY = 7;
	public static final int COL_POS_BPARTNERLOCATION__REGION = 8;
	public static final int COL_POS_BPARTNERLOCATION__COUNTRY = 9;
	public static final int COL_POS_BPARTNERLOCATION__PHONE = 10;
	public static final int COL_POS_BPARTNERLOCATION__PHONE2 = 11;
	public static final int COL_POS_BPARTNERLOCATION__FAX = 12;
	public static final int COL_POS_BPARTNERLOCATION__EMAIL = 13;
	public static final int COL_POS_BPARTNERLOCATION__CONTACTPERSON = 14;

	public static final int COL_POS_TASK__TASKTYPE_ID = 4;
	public static final int COL_POS_TASK__SCHEDULEDFOR = 5;
	public static final int COL_POS_TASK__ISRECURRENT = 6;
	public static final int COL_POS_TASK__ISDIFFERENTSTARTINGTIME = 7;
	public static final int COL_POS_TASK__TIMEFREQUENCY = 8;
	public static final int COL_POS_TASK__TIMEFREQUENCYTYPE = 9;
	public static final int COL_POS_TASK__STARTINGTIME = 10;
	public static final int COL_POS_TASK__TIMEREMINDERSTART = 11;
	public static final int COL_POS_TASK__RUNMILEAGE = 12;
	public static final int COL_POS_TASK__MILEAGEREMINDERSTART = 13;
	public static final int COL_POS_TASK__TODOCOUNT = 14;

	public static final int COL_POS_TASK_CAR__TASK_ID = 4;
	public static final int COL_POS_TASK_CAR__CAR_ID = 5;
	public static final int COL_POS_TASK_CAR__FIRSTRUN_DATE = 6;
	public static final int COL_POS_TASK_CAR__FIRSTRUN_MILEAGE = 7;

	public static final int COL_POS_TODO__TASK_ID = 4;
	public static final int COL_POS_TODO__CAR_ID = 5;
	public static final int COL_POS_TODO__DUEDATE = 6;
	public static final int COL_POS_TODO__DUEMILAGE = 7;
	public static final int COL_POS_TODO__NOTIFICATIONDATE = 8;
	public static final int COL_POS_TODO__NOTIFICATIONMILEAGE = 9;
	public static final int COL_POS_TODO__ISDONE = 10;
	public static final int COL_POS_TODO__DONEDATE = 11;
	public static final int COL_POS_TODO__ISSTOPNOTIFICATION = 12;

	public static final int COL_POS_REIMBURSEMENT_CAR_RATES__CAR_ID = 4;
	public static final int COL_POS_REIMBURSEMENT_CAR_RATES__EXPENSETYPE_ID = 5;
	public static final int COL_POS_REIMBURSEMENT_CAR_RATES__RATE = 6;

	public static final String[] COL_LIST_DRIVER_TABLE = { COL_NAME_GEN_ROWID,
			COL_NAME_GEN_NAME, COL_NAME_GEN_ISACTIVE,
			COL_NAME_GEN_USER_COMMENT, COL_NAME_DRIVER__LICENSE_NO };

	public static final String[] COL_LIST_CAR_TABLE = { COL_NAME_GEN_ROWID,
			COL_NAME_GEN_NAME, COL_NAME_GEN_ISACTIVE,
			COL_NAME_GEN_USER_COMMENT, COL_NAME_CAR__MODEL,
			COL_NAME_CAR__REGISTRATIONNO, COL_NAME_CAR__INDEXSTART,
			COL_NAME_CAR__INDEXCURRENT, COL_NAME_CAR__UOMLENGTH_ID,
			COL_NAME_CAR__UOMVOLUME_ID, COL_NAME_CAR__CURRENCY_ID };

	public static final String[] COL_LIST_UOM_TABLE = { COL_NAME_GEN_ROWID,
			COL_NAME_GEN_NAME, COL_NAME_GEN_ISACTIVE,
			COL_NAME_GEN_USER_COMMENT, COL_NAME_UOM__CODE,
			COL_NAME_UOM__UOMTYPE };

	public static final String[] COL_LIST_UOMCONVERSION_TABLE = {
			COL_NAME_GEN_ROWID, COL_NAME_GEN_NAME, COL_NAME_GEN_ISACTIVE,
			COL_NAME_GEN_USER_COMMENT, COL_NAME_UOMCONVERSION__UOMFROM_ID,
			COL_NAME_UOMCONVERSION__UOMTO_ID, COL_NAME_UOMCONVERSION__RATE };

	public static final String[] COL_LIST_EXPENSETYPE = { COL_NAME_GEN_ROWID,
			COL_NAME_GEN_NAME, COL_NAME_GEN_ISACTIVE,
			COL_NAME_GEN_USER_COMMENT,
			COL_NAME_EXPENSETYPE__ISCALCULATEREIMBURSEMENT };

	public static final String[] COL_LIST_MILEAGE_TABLE = { COL_NAME_GEN_ROWID,
			COL_NAME_GEN_NAME, COL_NAME_GEN_ISACTIVE,
			COL_NAME_GEN_USER_COMMENT, COL_NAME_MILEAGE__DATE,
			COL_NAME_MILEAGE__CAR_ID, COL_NAME_MILEAGE__DRIVER_ID,
			COL_NAME_MILEAGE__INDEXSTART, COL_NAME_MILEAGE__INDEXSTOP,
			COL_NAME_MILEAGE__UOMLENGTH_ID, COL_NAME_MILEAGE__EXPENSETYPE_ID,
			COL_NAME_MILEAGE__GPSTRACKLOG, COL_NAME_MILEAGE__TAG_ID };

	public static final String[] COL_LIST_CURRENCY_TABLE = {
			COL_NAME_GEN_ROWID, COL_NAME_GEN_NAME, COL_NAME_GEN_ISACTIVE,
			COL_NAME_GEN_USER_COMMENT, COL_NAME_CURRENCY__CODE };

	public static final String[] COL_LIST_REFUEL_TABLE = { COL_NAME_GEN_ROWID,
			COL_NAME_GEN_NAME, COL_NAME_GEN_ISACTIVE,
			COL_NAME_GEN_USER_COMMENT, COL_NAME_REFUEL__CAR_ID,
			COL_NAME_REFUEL__DRIVER_ID, COL_NAME_REFUEL__EXPENSETYPE_ID,
			COL_NAME_REFUEL__INDEX, COL_NAME_REFUEL__QUANTITY,
			COL_NAME_REFUEL__UOMVOLUME_ID, COL_NAME_REFUEL__PRICE,
			COL_NAME_REFUEL__CURRENCY_ID, COL_NAME_REFUEL__DATE,
			COL_NAME_REFUEL__DOCUMENTNO, COL_NAME_REFUEL__EXPENSECATEGORY_ID,
			COL_NAME_REFUEL__ISFULLREFUEL, COL_NAME_REFUEL__QUANTITYENTERED,
			COL_NAME_REFUEL__UOMVOLUMEENTERED_ID,
			COL_NAME_REFUEL__PRICEENTERED, COL_NAME_REFUEL__CURRENCYENTERED_ID,
			COL_NAME_REFUEL__CURRENCYRATE,
			COL_NAME_REFUEL__UOMVOLCONVERSIONRATE, COL_NAME_REFUEL__AMOUNT,
			COL_NAME_REFUEL__AMOUNTENTERED, COL_NAME_REFUEL__BPARTNER_ID,
			COL_NAME_REFUEL__BPARTNER_LOCATION_ID, COL_NAME_REFUEL__TAG_ID };

	public static final String[] COL_LIST_EXPENSECATEGORY_TABLE = {
			COL_NAME_GEN_ROWID, COL_NAME_GEN_NAME, COL_NAME_GEN_ISACTIVE,
			COL_NAME_GEN_USER_COMMENT,
			COL_NAME_EXPENSECATEGORY__ISEXCLUDEFROMMILEAGECOST,
			COL_NAME_EXPENSECATEGORY__ISFUEL };

	public static final String[] COL_LIST_EXPENSE_TABLE = { COL_NAME_GEN_ROWID,
			COL_NAME_GEN_NAME, COL_NAME_GEN_ISACTIVE,
			COL_NAME_GEN_USER_COMMENT, COL_NAME_EXPENSE__CAR_ID,
			COL_NAME_EXPENSE__DRIVER_ID, COL_NAME_EXPENSE__EXPENSECATEGORY_ID,
			COL_NAME_EXPENSE__EXPENSETYPE_ID, COL_NAME_EXPENSE__AMOUNT,
			COL_NAME_EXPENSE__CURRENCY_ID, COL_NAME_EXPENSE__DATE,
			COL_NAME_EXPENSE__DOCUMENTNO, COL_NAME_EXPENSE__INDEX,
			COL_NAME_EXPENSE__FROMTABLE, COL_NAME_EXPENSE__FROMRECORD_ID,
			COL_NAME_EXPENSE__AMOUNTENTERED,
			COL_NAME_EXPENSE__CURRENCYENTERED_ID,
			COL_NAME_EXPENSE__CURRENCYRATE, COL_NAME_EXPENSE__QUANTITY,
			COL_NAME_EXPENSE__PRICE, COL_NAME_EXPENSE__PRICEENTERED,
			COL_NAME_EXPENSE__UOM_ID, COL_NAME_EXPENSE__BPARTNER_ID,
			COL_NAME_EXPENSE__BPARTNER_LOCATION_ID, COL_NAME_EXPENSE__TAG_ID };

	public static final String[] COL_LIST_CURRENCYRATE_TABLE = {
			COL_NAME_GEN_ROWID, COL_NAME_GEN_NAME, COL_NAME_GEN_ISACTIVE,
			COL_NAME_GEN_USER_COMMENT, COL_NAME_CURRENCYRATE__FROMCURRENCY_ID,
			COL_NAME_CURRENCYRATE__TOCURRENCY_ID, COL_NAME_CURRENCYRATE__RATE,
			COL_NAME_CURRENCYRATE__INVERSERATE };

	public static final String[] COL_LIST_GPSTRACK_TABLE = {
			COL_NAME_GEN_ROWID, COL_NAME_GEN_NAME, COL_NAME_GEN_ISACTIVE,
			COL_NAME_GEN_USER_COMMENT, COL_NAME_GPSTRACK__CAR_ID,
			COL_NAME_GPSTRACK__DRIVER_ID, COL_NAME_GPSTRACK__MILEAGE_ID,
			COL_NAME_GPSTRACK__DATE, COL_NAME_GPSTRACK__MINACCURACY,
			COL_NAME_GPSTRACK__AVGACCURACY, COL_NAME_GPSTRACK__MAXACCURACY,
			COL_NAME_GPSTRACK__MINALTITUDE, COL_NAME_GPSTRACK__MAXALTITUDE,
			COL_NAME_GPSTRACK__TOTALTIME, COL_NAME_GPSTRACK__MOVINGTIME,
			COL_NAME_GPSTRACK__DISTANCE, COL_NAME_GPSTRACK__MAXSPEED,
			COL_NAME_GPSTRACK__AVGSPEED, COL_NAME_GPSTRACK__AVGMOVINGSPEED,
			COL_NAME_GPSTRACK__TOTALTRACKPOINTS,
			COL_NAME_GPSTRACK__INVALIDTRACKPOINTS, COL_NAME_GPSTRACK__TAG_ID,
			COL_NAME_GPSTRACK__TOTALPAUSETIME };

	public static final String[] COL_LIST_GPSTRACKDETAIL_TABLE = {
			COL_NAME_GEN_ROWID, COL_NAME_GEN_NAME, COL_NAME_GEN_ISACTIVE,
			COL_NAME_GEN_USER_COMMENT, COL_NAME_GPSTRACKDETAIL__GPSTRACK_ID,
			COL_NAME_GPSTRACKDETAIL__FILE, COL_NAME_GPSTRACKDETAIL__FILEFORMAT };

	public static final String[] COL_LIST_BPARTNER_TABLE = {
			COL_NAME_GEN_ROWID, COL_NAME_GEN_NAME, COL_NAME_GEN_ISACTIVE,
			COL_NAME_GEN_USER_COMMENT };

	// business partner location
	public static final String[] COL_LIST_BPARTNERLOCATION_TABLE = {
			COL_NAME_GEN_ROWID, COL_NAME_GEN_NAME, COL_NAME_GEN_ISACTIVE,
			COL_NAME_GEN_USER_COMMENT, COL_NAME_BPARTNERLOCATION__BPARTNER_ID,
			COL_NAME_BPARTNERLOCATION__ADDRESS,
			COL_NAME_BPARTNERLOCATION__POSTAL, COL_NAME_BPARTNERLOCATION__CITY,
			COL_NAME_BPARTNERLOCATION__REGION,
			COL_NAME_BPARTNERLOCATION__COUNTRY,
			COL_NAME_BPARTNERLOCATION__PHONE,
			COL_NAME_BPARTNERLOCATION__PHONE2, COL_NAME_BPARTNERLOCATION__FAX,
			COL_NAME_BPARTNERLOCATION__EMAIL,
			COL_NAME_BPARTNERLOCATION__CONTACTPERSON };

	public static final String[] COL_LIST_TAG_TABLE = { COL_NAME_GEN_ROWID,
			COL_NAME_GEN_NAME, COL_NAME_GEN_ISACTIVE, COL_NAME_GEN_USER_COMMENT };

	// tasks/reminders tables
	public static final String[] COL_LIST_TASKTYPE_TABLE = {
			COL_NAME_GEN_ROWID, COL_NAME_GEN_NAME, COL_NAME_GEN_ISACTIVE,
			COL_NAME_GEN_USER_COMMENT };

	public static final String[] COL_LIST_TASK_TABLE = { COL_NAME_GEN_ROWID,
			COL_NAME_GEN_NAME, COL_NAME_GEN_ISACTIVE,
			COL_NAME_GEN_USER_COMMENT, COL_NAME_TASK__TASKTYPE_ID,
			COL_NAME_TASK__SCHEDULEDFOR, COL_NAME_TASK__ISRECURRENT,
			COL_NAME_TASK__ISDIFFERENTSTARTINGTIME,
			COL_NAME_TASK__TIMEFREQUENCY, COL_NAME_TASK__TIMEFREQUENCYTYPE,
			COL_NAME_TASK__STARTINGTIME, COL_NAME_TASK__TIMEREMINDERSTART,
			COL_NAME_TASK__RUNMILEAGE, COL_NAME_TASK__MILEAGEREMINDERSTART,
			COL_NAME_TASK__TODOCOUNT };

	public static final String[] COL_LIST_TASK_CAR_TABLE = {
			COL_NAME_GEN_ROWID, COL_NAME_GEN_NAME, COL_NAME_GEN_ISACTIVE,
			COL_NAME_GEN_USER_COMMENT, COL_NAME_TASK_CAR__TASK_ID,
			COL_NAME_TASK_CAR__CAR_ID, COL_NAME_TASK_CAR__FIRSTRUN_DATE,
			COL_NAME_TASK_CAR__FIRSTRUN_MILEAGE };

	public static final String[] COL_LIST_TODO_TABLE = { COL_NAME_GEN_ROWID,
			COL_NAME_GEN_NAME, COL_NAME_GEN_ISACTIVE,
			COL_NAME_GEN_USER_COMMENT, COL_NAME_TODO__TASK_ID,
			COL_NAME_TODO__CAR_ID, COL_NAME_TODO__DUEDATE,
			COL_NAME_TODO__DUEMILEAGE, COL_NAME_TODO__NOTIFICATIONDATE,
			COL_NAME_TODO__NOTIFICATIONMILEAGE, COL_NAME_TODO__ISDONE,
			COL_NAME_TODO__DONEDATE, COL_NAME_TODO__ISSTOPNOTIFICATION };

	public static final String[] COL_LIST_REIMBURSEMENT_CAR_RATES_TABLE = {
			COL_NAME_GEN_ROWID, COL_NAME_GEN_NAME, COL_NAME_GEN_ISACTIVE,
			COL_NAME_GEN_USER_COMMENT,
			COL_NAME_REIMBURSEMENT_CAR_RATES__CAR_ID,
			COL_NAME_REIMBURSEMENT_CAR_RATES__EXPENSETYPE_ID,
			COL_NAME_REIMBURSEMENT_CAR_RATES__RATE };
	
	public static final String[] COL_LIST_GEN_ROWID_NAME = {
			COL_NAME_GEN_ROWID, COL_NAME_GEN_NAME };
	public static final String[] COL_LIST_GEN_ROWID = { COL_NAME_GEN_ROWID };
	public static final String WHERE_CONDITION_ISACTIVE = " "
			+ COL_NAME_GEN_ISACTIVE + "='Y' ";
	public static final String WHERE_CONDITION_ISACTIVE_ANDPREFIX = " AND"
			+ WHERE_CONDITION_ISACTIVE + " ";

	/**
	 * Database creation sql statements
	 */
	protected static final String CREATE_SQL_DRIVER_TABLE = "CREATE TABLE IF NOT EXISTS "
			+ TABLE_NAME_DRIVER
			+ " ( "
			+ COL_NAME_GEN_ROWID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ COL_NAME_GEN_NAME
			+ " TEXT NOT NULL, "
			+ COL_NAME_GEN_ISACTIVE
			+ " TEXT DEFAULT 'Y', "
			+ COL_NAME_GEN_USER_COMMENT
			+ " TEXT NULL, "
			+ COL_NAME_DRIVER__LICENSE_NO
			+ " TEXT NULL "
			+ ");";
	protected static final String CREATE_SQL_CAR_TABLE = "CREATE TABLE IF NOT EXISTS "
			+ TABLE_NAME_CAR
			+ " ( "
			+ COL_NAME_GEN_ROWID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ COL_NAME_GEN_NAME
			+ " TEXT NOT NULL, "
			+ COL_NAME_GEN_ISACTIVE
			+ " TEXT DEFAULT 'Y', "
			+ COL_NAME_GEN_USER_COMMENT
			+ " TEXT NULL, "
			+ COL_NAME_CAR__MODEL
			+ " TEXT NULL, "
			+ COL_NAME_CAR__REGISTRATIONNO
			+ " TEXT NULL, "
			+ COL_NAME_CAR__INDEXSTART
			+ " NUMERIC, "
			+ COL_NAME_CAR__INDEXCURRENT
			+ " NUMERIC, "
			+ COL_NAME_CAR__UOMLENGTH_ID
			+ " INTEGER, "
			+ COL_NAME_CAR__UOMVOLUME_ID
			+ " INTEGER, "
			+ COL_NAME_CAR__CURRENCY_ID + " INTEGER " + ");";

	protected static final String CREATE_SQL_UOM_TABLE = "CREATE TABLE IF NOT EXISTS "
			+ TABLE_NAME_UOM
			+ " ( "
			+ COL_NAME_GEN_ROWID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ COL_NAME_GEN_NAME
			+ " TEXT NOT NULL, "
			+ COL_NAME_GEN_ISACTIVE
			+ " TEXT DEFAULT 'Y', "
			+ COL_NAME_GEN_USER_COMMENT
			+ " TEXT NULL, "
			+ COL_NAME_UOM__CODE
			+ " TEXT NOT NULL, "
			+ COL_NAME_UOM__UOMTYPE + " TEXT NOT NULL " + ");";
	protected static final String CREATE_SQL_UOMCONVERSION_TABLE = "CREATE TABLE IF NOT EXISTS "
			+ TABLE_NAME_UOMCONVERSION
			+ " ( "
			+ COL_NAME_GEN_ROWID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ COL_NAME_GEN_NAME
			+ " TEXT NOT NULL, "
			+ COL_NAME_GEN_ISACTIVE
			+ " TEXT DEFAULT 'Y', "
			+ COL_NAME_GEN_USER_COMMENT
			+ " TEXT NULL, "
			+ COL_NAME_UOMCONVERSION__UOMFROM_ID
			+ " INTEGER NOT NULL, "
			+ COL_NAME_UOMCONVERSION__UOMTO_ID
			+ " INTEGER NOT NULL, "
			+ COL_NAME_UOMCONVERSION__RATE
			+ " NUMERIC NOT NULL " + ");";
	protected static final String CREATE_SQL_EXPENSETYPE_TABLE = "CREATE TABLE IF NOT EXISTS "
			+ TABLE_NAME_EXPENSETYPE
			+ " ( "
			+ COL_NAME_GEN_ROWID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ COL_NAME_GEN_NAME
			+ " TEXT NOT NULL, "
			+ COL_NAME_GEN_ISACTIVE
			+ " TEXT DEFAULT 'Y', "
			+ COL_NAME_GEN_USER_COMMENT
			+ " TEXT NULL, "
			+ COL_NAME_EXPENSETYPE__ISCALCULATEREIMBURSEMENT
			+ " TEXT NOT NULL DEFAULT 'N' " + ");";
	protected static final String CREATE_SQL_MILEAGE_TABLE = "CREATE TABLE IF NOT EXISTS "
			+ TABLE_NAME_MILEAGE
			+ " ( "
			+ COL_NAME_GEN_ROWID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ COL_NAME_GEN_NAME
			+ " TEXT NULL, "
			+ COL_NAME_GEN_ISACTIVE
			+ " TEXT DEFAULT 'Y', "
			+ COL_NAME_GEN_USER_COMMENT
			+ " TEXT NULL, "
			+ COL_NAME_MILEAGE__DATE
			+ " DATE NOT NULL, "
			+ COL_NAME_MILEAGE__CAR_ID
			+ " INTEGER NOT NULL, "
			+ COL_NAME_MILEAGE__DRIVER_ID
			+ " INTEGER NOT NULL, "
			+ COL_NAME_MILEAGE__INDEXSTART
			+ " NUMERIC NOT NULL, "
			+ COL_NAME_MILEAGE__INDEXSTOP
			+ " NUMERIC NOT NULL, "
			+ COL_NAME_MILEAGE__UOMLENGTH_ID
			+ " INTEGER NOT NULL, "
			+ COL_NAME_MILEAGE__EXPENSETYPE_ID
			+ " INTEGER NOT NULL, "
			+ COL_NAME_MILEAGE__GPSTRACKLOG
			+ " TEXT NULL, "
			+ COL_NAME_MILEAGE__TAG_ID
			+ " INTEGER NULL, "
			+ COL_NAME_MILEAGE__REIMBURSEMENT_RATE
			+ " NUMERIC NOT NULL, "
			+ COL_NAME_MILEAGE__REIMBURSEMENT_VALUE
			+ " NUMERIC NOT NULL "
			+ ");";
	protected static final String CREATE_SQL_CURRENCY_TABLE = "CREATE TABLE IF NOT EXISTS "
			+ TABLE_NAME_CURRENCY
			+ " ( "
			+ COL_NAME_GEN_ROWID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ COL_NAME_GEN_NAME
			+ " TEXT NOT NULL, "
			+ COL_NAME_GEN_ISACTIVE
			+ " TEXT DEFAULT 'Y', "
			+ COL_NAME_GEN_USER_COMMENT
			+ " TEXT NULL, "
			+ COL_NAME_CURRENCY__CODE
			+ " TEXT NOT NULL "
			+ ");";
	protected static final String CREATE_SQL_REFUEL_TABLE = "CREATE TABLE IF NOT EXISTS "
			+ TABLE_NAME_REFUEL
			+ " ( "
			+ COL_NAME_GEN_ROWID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ COL_NAME_GEN_NAME
			+ " TEXT NULL, "
			+ COL_NAME_GEN_ISACTIVE
			+ " TEXT DEFAULT 'Y', "
			+ COL_NAME_GEN_USER_COMMENT
			+ " TEXT NULL, "
			+ COL_NAME_REFUEL__CAR_ID
			+ " INTEGER, "
			+ COL_NAME_REFUEL__DRIVER_ID
			+ " INTEGER, "
			+ COL_NAME_REFUEL__EXPENSETYPE_ID
			+ " INTEGER, "
			+ COL_NAME_REFUEL__INDEX
			+ " NUMERIC, "
			+ COL_NAME_REFUEL__QUANTITY
			+ " NUMERIC, "
			+ COL_NAME_REFUEL__UOMVOLUME_ID
			+ " INTEGER, "
			+ COL_NAME_REFUEL__PRICE
			+ " NUMERIC, "
			+ COL_NAME_REFUEL__CURRENCY_ID
			+ " INTEGER, "
			+ COL_NAME_REFUEL__DATE
			+ " DATE NULL, "
			+ COL_NAME_REFUEL__DOCUMENTNO
			+ " TEXT NULL, "
			+ COL_NAME_REFUEL__EXPENSECATEGORY_ID
			+ " INTEGER, "
			+ COL_NAME_REFUEL__ISFULLREFUEL
			+ " TEXT DEFAULT 'N', "
			+ COL_NAME_REFUEL__QUANTITYENTERED
			+ " NUMERIC NULL, "
			+ COL_NAME_REFUEL__UOMVOLUMEENTERED_ID
			+ " INTEGER NULL, "
			+ COL_NAME_REFUEL__PRICEENTERED
			+ " NUMERIC NULL, "
			+ COL_NAME_REFUEL__CURRENCYENTERED_ID
			+ " INTEGER NULL, "
			+ COL_NAME_REFUEL__CURRENCYRATE
			+ " NUMERIC NULL, "
			+ COL_NAME_REFUEL__UOMVOLCONVERSIONRATE
			+ " NUMERIC NULL, "
			+ COL_NAME_REFUEL__AMOUNT
			+ " NUMERIC NULL, "
			+ COL_NAME_REFUEL__AMOUNTENTERED
			+ " NUMERIC NULL, "
			+ COL_NAME_REFUEL__BPARTNER_ID
			+ " INTEGER NULL, "
			+ COL_NAME_REFUEL__BPARTNER_LOCATION_ID
			+ " INTEGER NULL, "
			+ COL_NAME_REFUEL__TAG_ID + " INTEGER NULL " + ");";

	protected static final String CREATE_SQL_EXPENSECATEGORY_TABLE = "CREATE TABLE IF NOT EXISTS "
			+ TABLE_NAME_EXPENSECATEGORY
			+ " ( "
			+ COL_NAME_GEN_ROWID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ COL_NAME_GEN_NAME
			+ " TEXT NOT NULL, "
			+ COL_NAME_GEN_ISACTIVE
			+ " TEXT DEFAULT 'Y', "
			+ COL_NAME_GEN_USER_COMMENT
			+ " TEXT NULL, "
			+ COL_NAME_EXPENSECATEGORY__ISEXCLUDEFROMMILEAGECOST
			+ " TEXT DEFAULT 'N', "
			+ COL_NAME_EXPENSECATEGORY__ISFUEL
			+ " TEXT DEFAULT 'N' " + ");";

	protected static final String CREATE_SQL_EXPENSE_TABLE = "CREATE TABLE IF NOT EXISTS "
			+ TABLE_NAME_EXPENSE
			+ " ( "
			+ COL_NAME_GEN_ROWID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ COL_NAME_GEN_NAME
			+ " TEXT NOT NULL, "
			+ COL_NAME_GEN_ISACTIVE
			+ " TEXT DEFAULT 'Y', "
			+ COL_NAME_GEN_USER_COMMENT
			+ " TEXT NULL, "
			+ COL_NAME_EXPENSE__CAR_ID
			+ " INTEGER, "
			+ COL_NAME_EXPENSE__DRIVER_ID
			+ " INTEGER, "
			+ COL_NAME_EXPENSE__EXPENSECATEGORY_ID
			+ " INTEGER, "
			+ COL_NAME_EXPENSE__EXPENSETYPE_ID
			+ " INTEGER, "
			+ COL_NAME_EXPENSE__AMOUNT
			+ " NUMERIC, "
			+ COL_NAME_EXPENSE__CURRENCY_ID
			+ " INTEGER, "
			+ COL_NAME_EXPENSE__DATE
			+ " DATE NULL, "
			+ COL_NAME_EXPENSE__DOCUMENTNO
			+ " TEXT NULL, "
			+ COL_NAME_EXPENSE__INDEX
			+ " NUMERIC, "
			+ COL_NAME_EXPENSE__FROMTABLE
			+ " TEXT NULL, "
			+ COL_NAME_EXPENSE__FROMRECORD_ID
			+ " INTEGER, "
			+ COL_NAME_EXPENSE__AMOUNTENTERED
			+ " NUMERIC NULL, "
			+ COL_NAME_EXPENSE__CURRENCYENTERED_ID
			+ " INTEGER NULL, "
			+ COL_NAME_EXPENSE__CURRENCYRATE
			+ " NUMERIC NULL, "
			+ COL_NAME_EXPENSE__QUANTITY
			+ " NUMERIC NULL, "
			+ COL_NAME_EXPENSE__PRICE
			+ " NUMERIC NULL, "
			+ COL_NAME_EXPENSE__PRICEENTERED
			+ " NUMERIC NULL, "
			+ COL_NAME_EXPENSE__UOM_ID
			+ " INTEGER NULL, "
			+ COL_NAME_EXPENSE__BPARTNER_ID
			+ " INTEGER NULL, "
			+ COL_NAME_EXPENSE__BPARTNER_LOCATION_ID
			+ " INTEGER NULL, "
			+ COL_NAME_EXPENSE__TAG_ID + " INTEGER NULL " + ");";

	protected static final String CREATE_SQL_CURRENCYRATE_TABLE = "CREATE TABLE IF NOT EXISTS "
			+ TABLE_NAME_CURRENCYRATE
			+ " ( "
			+ COL_NAME_GEN_ROWID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ COL_NAME_GEN_NAME
			+ " TEXT, "
			+ COL_NAME_GEN_ISACTIVE
			+ " TEXT DEFAULT 'Y', "
			+ COL_NAME_GEN_USER_COMMENT
			+ " TEXT NULL, "
			+ COL_NAME_CURRENCYRATE__FROMCURRENCY_ID
			+ " INTEGER, "
			+ COL_NAME_CURRENCYRATE__TOCURRENCY_ID
			+ " INTEGER, "
			+ COL_NAME_CURRENCYRATE__RATE
			+ " NUMERIC, "
			+ COL_NAME_CURRENCYRATE__INVERSERATE + " NUMERIC " + ");";

	protected static final String CREATE_SQL_GPSTRACK_TABLE = "CREATE TABLE IF NOT EXISTS "
			+ TABLE_NAME_GPSTRACK
			+ " ( "
			+ COL_NAME_GEN_ROWID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ COL_NAME_GEN_NAME
			+ " TEXT NULL, "
			+ COL_NAME_GEN_ISACTIVE
			+ " TEXT DEFAULT 'Y', "
			+ COL_NAME_GEN_USER_COMMENT
			+ " TEXT NULL, "
			+ COL_NAME_GPSTRACK__CAR_ID
			+ " INTEGER NULL, "
			+ COL_NAME_GPSTRACK__DRIVER_ID
			+ " INTEGER NULL, "
			+ COL_NAME_GPSTRACK__MILEAGE_ID
			+ " INTEGER NULL, "
			+ COL_NAME_GPSTRACK__DATE
			+ " DATE NULL, "
			+ COL_NAME_GPSTRACK__MINACCURACY
			+ " NUMERIC NULL, "
			+ COL_NAME_GPSTRACK__AVGACCURACY
			+ " NUMERIC NULL, "
			+ COL_NAME_GPSTRACK__MAXACCURACY
			+ " NUMERIC NULL, "
			+ COL_NAME_GPSTRACK__MINALTITUDE
			+ " NUMERIC NULL, "
			+ COL_NAME_GPSTRACK__MAXALTITUDE
			+ " NUMERIC NULL, "
			+ COL_NAME_GPSTRACK__TOTALTIME
			+ " NUMERIC NULL, "
			+ COL_NAME_GPSTRACK__MOVINGTIME
			+ " NUMERIC NULL, "
			+ COL_NAME_GPSTRACK__DISTANCE
			+ " NUMERIC NULL, "
			+ COL_NAME_GPSTRACK__MAXSPEED
			+ " NUMERIC NULL, "
			+ COL_NAME_GPSTRACK__AVGSPEED
			+ " NUMERIC NULL, "
			+ COL_NAME_GPSTRACK__AVGMOVINGSPEED
			+ " NUMERIC NULL, "
			+ COL_NAME_GPSTRACK__TOTALTRACKPOINTS
			+ " INTEGER NULL, "
			+ COL_NAME_GPSTRACK__INVALIDTRACKPOINTS
			+ " INTEGER NULL, "
			+ COL_NAME_GPSTRACK__TAG_ID
			+ " INTEGER NULL, "
			+ COL_NAME_GPSTRACK__TOTALPAUSETIME + " INTEGER NULL " + ");";

	protected static final String CREATE_SQL_GPSTRACKDETAIL_TABLE = "CREATE TABLE IF NOT EXISTS "
			+ TABLE_NAME_GPSTRACKDETAIL
			+ " ( "
			+ COL_NAME_GEN_ROWID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ COL_NAME_GEN_NAME
			+ " TEXT NULL, "
			+ COL_NAME_GEN_ISACTIVE
			+ " TEXT DEFAULT 'Y', "
			+ COL_NAME_GEN_USER_COMMENT
			+ " TEXT NULL, "
			+ COL_NAME_GPSTRACKDETAIL__GPSTRACK_ID
			+ " INTEGER NOT NULL, "
			+ COL_NAME_GPSTRACKDETAIL__FILE
			+ " TEXT NULL, "
			+ COL_NAME_GPSTRACKDETAIL__FILEFORMAT + " TEXT NULL " + ");";

	protected static final String CREATE_SQL_BPARTNER_TABLE = "CREATE TABLE IF NOT EXISTS "
			+ TABLE_NAME_BPARTNER
			+ " ( "
			+ COL_NAME_GEN_ROWID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ COL_NAME_GEN_NAME
			+ " TEXT NOT NULL, "
			+ COL_NAME_GEN_ISACTIVE
			+ " TEXT DEFAULT 'Y', "
			+ COL_NAME_GEN_USER_COMMENT
			+ " TEXT NULL "
			+ ");";

	protected static final String CREATE_SQL_BPARTNERLOCATION_TABLE = "CREATE TABLE IF NOT EXISTS "
			+ TABLE_NAME_BPARTNERLOCATION
			+ " ( "
			+ COL_NAME_GEN_ROWID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ COL_NAME_GEN_NAME
			+ " TEXT NOT NULL, "
			+ COL_NAME_GEN_ISACTIVE
			+ " TEXT DEFAULT 'Y', "
			+ COL_NAME_GEN_USER_COMMENT
			+ " TEXT NULL, "
			+ COL_NAME_BPARTNERLOCATION__BPARTNER_ID
			+ " INTEGER NOT NULL, "
			+ COL_NAME_BPARTNERLOCATION__ADDRESS
			+ " TEXT NULL, "
			+ COL_NAME_BPARTNERLOCATION__POSTAL
			+ " TEXT NULL, "
			+ COL_NAME_BPARTNERLOCATION__CITY
			+ " TEXT NULL, "
			+ COL_NAME_BPARTNERLOCATION__REGION
			+ " TEXT NULL, "
			+ COL_NAME_BPARTNERLOCATION__COUNTRY
			+ " TEXT NULL, "
			+ COL_NAME_BPARTNERLOCATION__PHONE
			+ " TEXT NULL, "
			+ COL_NAME_BPARTNERLOCATION__PHONE2
			+ " TEXT NULL, "
			+ COL_NAME_BPARTNERLOCATION__FAX
			+ " TEXT NULL, "
			+ COL_NAME_BPARTNERLOCATION__EMAIL
			+ " TEXT NULL, "
			+ COL_NAME_BPARTNERLOCATION__CONTACTPERSON + " TEXT NULL " + ");";

	protected static final String CREATE_SQL_TAG_TABLE = "CREATE TABLE IF NOT EXISTS "
			+ TABLE_NAME_TAG
			+ " ( "
			+ COL_NAME_GEN_ROWID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ COL_NAME_GEN_NAME
			+ " TEXT NOT NULL, "
			+ COL_NAME_GEN_ISACTIVE
			+ " TEXT DEFAULT 'Y', "
			+ COL_NAME_GEN_USER_COMMENT
			+ " TEXT NULL "
			+ ");";

	protected static final String CREATE_SQL_TASKTYPE_TABLE = "CREATE TABLE IF NOT EXISTS "
			+ TABLE_NAME_TASKTYPE
			+ " ( "
			+ COL_NAME_GEN_ROWID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ COL_NAME_GEN_NAME
			+ " TEXT NOT NULL, "
			+ COL_NAME_GEN_ISACTIVE
			+ " TEXT DEFAULT 'Y', "
			+ COL_NAME_GEN_USER_COMMENT
			+ " TEXT NULL "
			+ ");";

	protected static final String CREATE_SQL_TASK_TABLE = "CREATE TABLE IF NOT EXISTS "
			+ TABLE_NAME_TASK
			+ " ( "
			+ COL_NAME_GEN_ROWID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ COL_NAME_GEN_NAME
			+ " TEXT NOT NULL, "
			+ COL_NAME_GEN_ISACTIVE
			+ " TEXT DEFAULT 'Y', "
			+ COL_NAME_GEN_USER_COMMENT
			+ " TEXT NULL, "
			+ COL_NAME_TASK__TASKTYPE_ID
			+ " INTEGER NOT NULL, "
			+ COL_NAME_TASK__SCHEDULEDFOR
			+ " TEXT NULL, "
			+ COL_NAME_TASK__ISRECURRENT
			+ " TEXT NOT NULL, "
			+ COL_NAME_TASK__ISDIFFERENTSTARTINGTIME
			+ " TEXT NULL, "
			+ COL_NAME_TASK__TIMEFREQUENCY
			+ " INTEGER NULL, "
			+ COL_NAME_TASK__TIMEFREQUENCYTYPE
			+ " INTEGER NULL, "
			+ COL_NAME_TASK__STARTINGTIME
			+ " DATE NULL, "
			+ COL_NAME_TASK__TIMEREMINDERSTART
			+ " INTEGER NULL, "
			+ COL_NAME_TASK__RUNMILEAGE
			+ " INTEGER NULL, "
			+ COL_NAME_TASK__MILEAGEREMINDERSTART
			+ " INTEGER NULL, "
			+ COL_NAME_TASK__TODOCOUNT
			+ " INTEGER NOT NULL, "
			+ " FOREIGN KEY("
			+ COL_NAME_TASK__TASKTYPE_ID
			+ ") REFERENCES "
			+ TABLE_NAME_TASKTYPE + "(" + COL_NAME_GEN_ROWID + ")" + ");";

	protected static final String CREATE_SQL_TASK_CAR_TABLE = "CREATE TABLE IF NOT EXISTS "
			+ TABLE_NAME_TASK_CAR
			+ " ( "
			+ COL_NAME_GEN_ROWID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ COL_NAME_GEN_NAME
			+ " TEXT NOT NULL, "
			+ COL_NAME_GEN_ISACTIVE
			+ " TEXT DEFAULT 'Y', "
			+ COL_NAME_GEN_USER_COMMENT
			+ " TEXT NULL, "
			+ COL_NAME_TASK_CAR__TASK_ID
			+ " INTEGER NOT NULL, "
			+ COL_NAME_TASK_CAR__CAR_ID
			+ " INTEGER NOT NULL, "
			+ COL_NAME_TASK_CAR__FIRSTRUN_DATE
			+ " DATE NULL, "
			+ COL_NAME_TASK_CAR__FIRSTRUN_MILEAGE
			+ " INTEGER NULL " + ");";

	protected static final String CREATE_SQL_TODO_TABLE = "CREATE TABLE IF NOT EXISTS "
			+ TABLE_NAME_TODO
			+ " ( "
			+ COL_NAME_GEN_ROWID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ COL_NAME_GEN_NAME
			+ " TEXT NOT NULL, "
			+ COL_NAME_GEN_ISACTIVE
			+ " TEXT DEFAULT 'Y', "
			+ COL_NAME_GEN_USER_COMMENT
			+ " TEXT NULL, "
			+ COL_NAME_TODO__TASK_ID
			+ " INTEGER NOT NULL, "
			+ COL_NAME_TODO__CAR_ID
			+ " INTEGER NULL, "
			+ COL_NAME_TODO__DUEDATE
			+ " DATE NULL, "
			+ COL_NAME_TODO__DUEMILEAGE
			+ " INTEGER NULL, "
			+ COL_NAME_TODO__NOTIFICATIONDATE
			+ " DATE NULL, "
			+ COL_NAME_TODO__NOTIFICATIONMILEAGE
			+ " INTEGER NULL, "
			+ COL_NAME_TODO__ISDONE
			+ " TEXT DEFAULT 'N', "
			+ COL_NAME_TODO__DONEDATE
			+ " DATE NULL, "
			+ COL_NAME_TODO__ISSTOPNOTIFICATION + " TEXT DEFAULT 'N' " + ");";

	protected static final String CREATE_SQL_REIMBURSEMENT_CAR_RATES_TABLE = "CREATE TABLE IF NOT EXISTS "
			+ TABLE_NAME_REIMBURSEMENT_CAR_RATES
			+ " ( "
			+ COL_NAME_GEN_ROWID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ COL_NAME_GEN_NAME
			+ " TEXT NOT NULL, "
			+ COL_NAME_GEN_ISACTIVE
			+ " TEXT DEFAULT 'Y', "
			+ COL_NAME_GEN_USER_COMMENT
			+ " TEXT NULL, "
			+ COL_NAME_REIMBURSEMENT_CAR_RATES__CAR_ID
			+ " INTEGER NOT NULL, "
			+ COL_NAME_REIMBURSEMENT_CAR_RATES__EXPENSETYPE_ID
			+ " INTEGER NOT NULL, "
			+ COL_NAME_REIMBURSEMENT_CAR_RATES__RATE
			+ " NUMBER NOT NULL DEFAULT 0 " + ");";

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
			// create drivers table
			createDriverTable(db);
			// create cars table
			createCarTable(db);
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
			AddOnDBAdapter.createAddOnTable(db);
			AddOnDBAdapter.createAddOnBKScheduleTable(db);
			AddOnDBAdapter.createAddOnSecureBKSettingsTable(db);

			createTaskToDoTables(db);

			AddOnDBAdapter.createAddOnDataTemplateTables(db);
			AddOnDBAdapter.createAddOnBTDeviceCarTable(db);

			createReimbursementCarRatesTable(db);

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

		/**
		 * @param db
		 */
		private void createCarTable(SQLiteDatabase db) {
			db.execSQL(CREATE_SQL_CAR_TABLE);
		}

		/**
		 * @param db
		 */
		private void createDriverTable(SQLiteDatabase db) {
			db.execSQL(CREATE_SQL_DRIVER_TABLE);
			String sql = "INSERT INTO " + TABLE_NAME_DRIVER + "( "
					+ COL_NAME_GEN_NAME + ", " + COL_NAME_GEN_ISACTIVE + ", "
					+ COL_NAME_GEN_USER_COMMENT + ") "
					+ " VALUES( 'I', 'Y', 'Customize me')";
			db.execSQL(sql);
		}

		private void createTaskToDoTables(SQLiteDatabase db)
				throws SQLException {
			// create task/reminder
			db.execSQL(CREATE_SQL_TASKTYPE_TABLE);
			db.execSQL(CREATE_SQL_TASK_TABLE);
			db.execSQL(CREATE_SQL_TASK_CAR_TABLE);
			db.execSQL(CREATE_SQL_TODO_TABLE);

			String sql = "INSERT INTO " + TABLE_NAME_TASKTYPE + "( "
					+ COL_NAME_GEN_NAME + ", " + COL_NAME_GEN_ISACTIVE + ", "
					+ COL_NAME_GEN_USER_COMMENT + ") " + " VALUES( " + "'"
					+ mResource.getString(R.string.DB_TaskType_ServiceName)
					+ "', " + "'Y', " + "'"
					+ mResource.getString(R.string.DB_TaskType_ServiceComment)
					+ "')";
			db.execSQL(sql);
			sql = "INSERT INTO " + TABLE_NAME_TASKTYPE + "( "
					+ COL_NAME_GEN_NAME + ", " + COL_NAME_GEN_ISACTIVE + ", "
					+ COL_NAME_GEN_USER_COMMENT + ") " + " VALUES( " + "'"
					+ mResource.getString(R.string.DB_TaskType_ReminderName)
					+ "', " + "'Y', " + "'"
					+ mResource.getString(R.string.DB_TaskType_ReminderComment)
					+ "')";
			db.execSQL(sql);

			// Calendar cal = Calendar.getInstance();
			// if(cal.get(Calendar.HOUR_OF_DAY) > 20){
			// cal.add(Calendar.DAY_OF_YEAR, 1);
			// }
			// cal.set(Calendar.HOUR_OF_DAY, 20);
			// cal.set(Calendar.MINUTE, 0);
			// cal.set(Calendar.SECOND, 0);
			//
			// sql = " INSERT INTO " + TASK_TABLE_NAME + "( " +
			// GEN_COL_NAME_NAME + ", " +
			// GEN_COL_ISACTIVE_NAME + ", " +
			// GEN_COL_USER_COMMENT_NAME + ", " +
			// TASK_COL_TASKTYPE_ID_NAME + ", " +
			// TASK_COL_SCHEDULEDFOR_NAME + ", " +
			// TASK_COL_ISRECURRENT_NAME + ", " +
			// TASK_COL_ISDIFFERENTSTARTINGTIME_NAME + ", " +
			// TASK_COL_TIMEFREQUENCY_NAME + ", " +
			// TASK_COL_TIMEFREQUENCYTYPE_NAME + ", " +
			// TASK_COL_STARTINGTIME_NAME + ", " +
			// TASK_COL_TIMEREMINDERSTART_NAME + ", " +
			// TASK_COL_TODOCOUNT_NAME + ") " +
			// "VALUES ( " +
			// "'" + mResource.getString(R.string.DB_DemoTask_Name) + "', " +
			// "'Y', " +
			// "'" + mResource.getString(R.string.DB_DemoTask_Comment) + "', " +
			// "2, " +
			// "'T', " +
			// "'Y', " +
			// "'N', " +
			// "1, " +
			// "1, " +
			// Long.toString(cal.getTimeInMillis() / 1000) + ", " +
			// "30, " +
			// "3 )";
			//
			// db.execSQL(sql);
			//
			// Intent intent = new Intent(mCtx, ToDoManagementService.class);
			// mCtx.startService(intent);
			//
		}

		private void createBPartnerTable(SQLiteDatabase db) throws SQLException {
			// business partner
			db.execSQL(CREATE_SQL_BPARTNER_TABLE);
			db.execSQL(CREATE_SQL_BPARTNERLOCATION_TABLE);
		}

		private void createGPSTrackTables(SQLiteDatabase db)
				throws SQLException {
			db.execSQL(CREATE_SQL_GPSTRACK_TABLE);
			db.execSQL(CREATE_SQL_GPSTRACKDETAIL_TABLE);
		}

		private void createUOMTable(SQLiteDatabase db) throws SQLException {
			// create uom table
			db.execSQL(CREATE_SQL_UOM_TABLE);
			// init uom's
			String colPart = "INSERT INTO " + TABLE_NAME_UOM + " ( "
					+ COL_NAME_GEN_NAME + ", " + COL_NAME_GEN_ISACTIVE + ", "
					+ COL_NAME_GEN_USER_COMMENT + ", " + COL_NAME_UOM__CODE
					+ ", " + COL_NAME_UOM__UOMTYPE + ") ";
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
			db.execSQL(CREATE_SQL_UOMCONVERSION_TABLE);
			// init default uom conversions
			String colPart = "INSERT INTO " + TABLE_NAME_UOMCONVERSION + " ( "
					+ COL_NAME_GEN_NAME + ", " + COL_NAME_GEN_ISACTIVE + ", "
					+ COL_NAME_GEN_USER_COMMENT + ", "
					+ COL_NAME_UOMCONVERSION__UOMFROM_ID + ", "
					+ COL_NAME_UOMCONVERSION__UOMTO_ID + ", "
					+ COL_NAME_UOMCONVERSION__RATE + " " + ") ";
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
			db.execSQL(CREATE_SQL_EXPENSETYPE_TABLE);
			// init some standard expenses
			String colPart = "INSERT INTO " + TABLE_NAME_EXPENSETYPE + " ( "
					+ COL_NAME_GEN_NAME + ", " + COL_NAME_GEN_ISACTIVE + ", "
					+ COL_NAME_GEN_USER_COMMENT + " " + ") ";
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
			db.execSQL(CREATE_SQL_MILEAGE_TABLE);
		}

		private void createRefuelTable(SQLiteDatabase db) throws SQLException {
			db.execSQL(CREATE_SQL_REFUEL_TABLE);
		}

		private void createTagTable(SQLiteDatabase db) throws SQLException {
			// business partner
			db.execSQL(CREATE_SQL_TAG_TABLE);
		}

		private void createReimbursementCarRatesTable(SQLiteDatabase db)
				throws SQLException {
			db.execSQL(CREATE_SQL_REIMBURSEMENT_CAR_RATES_TABLE);
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
				upgradeDbTo355(db, oldVersion);
				upgradeDbTo356(db, oldVersion);
				upgradeDbTo357(db, oldVersion);
				AddOnDBAdapter.upgradeTo358(db);
				AddOnDBAdapter.upgradeTo359(db);
				upgradeDbTo400(db, oldVersion);
				upgradeDbTo401(db, oldVersion);
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
				upgradeDbTo355(db, oldVersion);
				upgradeDbTo356(db, oldVersion);
				upgradeDbTo357(db, oldVersion);
				AddOnDBAdapter.upgradeTo358(db);
				AddOnDBAdapter.upgradeTo359(db);
				upgradeDbTo400(db, oldVersion);
				upgradeDbTo401(db, oldVersion);
			}
			// AndiCar 2.1.x
			else if (oldVersion == 210) {
				upgradeDbTo300(db, oldVersion); // update database to version
												// 210 //AndiCar 2.2.0
				upgradeDbTo310(db, oldVersion);
				upgradeDbTo330(db, oldVersion);
				upgradeDbTo340(db, oldVersion);
				upgradeDbTo350(db, oldVersion);
				upgradeDbTo355(db, oldVersion);
				upgradeDbTo356(db, oldVersion);
				upgradeDbTo357(db, oldVersion);
				AddOnDBAdapter.upgradeTo358(db);
				AddOnDBAdapter.upgradeTo359(db);
				upgradeDbTo400(db, oldVersion);
				upgradeDbTo401(db, oldVersion);
			}
			// AndiCar 3.0.x
			else if (oldVersion == 300) {
				upgradeDbTo310(db, oldVersion);
				upgradeDbTo330(db, oldVersion);
				upgradeDbTo340(db, oldVersion);
				upgradeDbTo350(db, oldVersion);
				upgradeDbTo355(db, oldVersion);
				upgradeDbTo356(db, oldVersion);
				upgradeDbTo357(db, oldVersion);
				AddOnDBAdapter.upgradeTo358(db);
				AddOnDBAdapter.upgradeTo359(db);
				upgradeDbTo401(db, oldVersion);
				upgradeDbTo400(db, oldVersion);
			}
			// AndiCar 3.1.x, 3.2.x
			else if (oldVersion == 310) {
				upgradeDbTo330(db, oldVersion);
				upgradeDbTo340(db, oldVersion);
				upgradeDbTo350(db, oldVersion);
				upgradeDbTo355(db, oldVersion);
				upgradeDbTo356(db, oldVersion);
				upgradeDbTo357(db, oldVersion);
				AddOnDBAdapter.upgradeTo358(db);
				AddOnDBAdapter.upgradeTo359(db);
				upgradeDbTo400(db, oldVersion);
				upgradeDbTo401(db, oldVersion);
			}
			// AndiCar 3.3.x
			else if (oldVersion == 330) {
				upgradeDbTo340(db, oldVersion);
				upgradeDbTo350(db, oldVersion);
				upgradeDbTo355(db, oldVersion);
				upgradeDbTo356(db, oldVersion);
				upgradeDbTo357(db, oldVersion);
				AddOnDBAdapter.upgradeTo358(db);
				AddOnDBAdapter.upgradeTo359(db);
				upgradeDbTo400(db, oldVersion);
				upgradeDbTo401(db, oldVersion);
			}
			// AndiCar 3.4.x
			else if (oldVersion == 340 || oldVersion == 350) { // upgrade again
																// because on
																// fresh 350
																// install addon
																// tables was
																// not created
				upgradeDbTo350(db, oldVersion);
				upgradeDbTo355(db, oldVersion);
				upgradeDbTo356(db, oldVersion);
				upgradeDbTo357(db, oldVersion);
				AddOnDBAdapter.upgradeTo358(db);
				AddOnDBAdapter.upgradeTo359(db);
				upgradeDbTo400(db, oldVersion);
				upgradeDbTo401(db, oldVersion);
			} else if (oldVersion == 351) {
				upgradeDbTo355(db, oldVersion);
				upgradeDbTo356(db, oldVersion);
				upgradeDbTo357(db, oldVersion);
				AddOnDBAdapter.upgradeTo358(db);
				AddOnDBAdapter.upgradeTo359(db);
				upgradeDbTo400(db, oldVersion);
				upgradeDbTo401(db, oldVersion);
			} else if (oldVersion == 353) {
				upgradeDbTo355(db, oldVersion);
				upgradeDbTo356(db, oldVersion);
				upgradeDbTo357(db, oldVersion);
				AddOnDBAdapter.upgradeTo358(db);
				AddOnDBAdapter.upgradeTo359(db);
				upgradeDbTo400(db, oldVersion);
				upgradeDbTo401(db, oldVersion);
			} else if (oldVersion == 355) {
				upgradeDbTo356(db, oldVersion);
				upgradeDbTo357(db, oldVersion);
				AddOnDBAdapter.upgradeTo358(db);
				AddOnDBAdapter.upgradeTo359(db);
				upgradeDbTo400(db, oldVersion);
				upgradeDbTo401(db, oldVersion);
			} else if (oldVersion == 356) {
				upgradeDbTo357(db, oldVersion);
				AddOnDBAdapter.upgradeTo358(db);
				AddOnDBAdapter.upgradeTo359(db);
				upgradeDbTo400(db, oldVersion);
				upgradeDbTo401(db, oldVersion);
			} else if (oldVersion == 357) {
				AddOnDBAdapter.upgradeTo358(db);
				AddOnDBAdapter.upgradeTo359(db);
				upgradeDbTo400(db, oldVersion);
				upgradeDbTo401(db, oldVersion);
			} else if (oldVersion == 358) {
				AddOnDBAdapter.upgradeTo359(db);
				upgradeDbTo400(db, oldVersion);
				upgradeDbTo401(db, oldVersion);
			} else if (oldVersion == 359) {
				upgradeDbTo400(db, oldVersion);
				upgradeDbTo401(db, oldVersion);
			} else if (oldVersion == 400) {
				upgradeDbTo401(db, oldVersion);
			}

			// !!!!!!!!!!!!!!DON'T FORGET onCREATE !!!!!!!!!!!!!!!!

			// create indexes
			createIndexes(db);
			// create folders on SDCARD
			FileUtils fu = new FileUtils(mCtx);
			if (fu.createFolderIfNotExists(FileUtils.ALL_FOLDER) != -1) {
				Log.e(TAG, fu.lastError);
			}

		}

		private void upgradeDbTo200(SQLiteDatabase db) throws SQLException {
			createExpenseCategory(db);
			String updateSql;
			if (!columnExists(db, TABLE_NAME_REFUEL,
					COL_NAME_REFUEL__EXPENSECATEGORY_ID)) {
				updateSql = "ALTER TABLE " + TABLE_NAME_REFUEL + " ADD "
						+ COL_NAME_REFUEL__EXPENSECATEGORY_ID + " INTEGER";
				db.execSQL(updateSql);
				updateSql = "UPDATE " + TABLE_NAME_REFUEL + " SET "
						+ COL_NAME_REFUEL__EXPENSECATEGORY_ID + " = 1";
				db.execSQL(updateSql);
			}
			if (!columnExists(db, TABLE_NAME_REFUEL,
					COL_NAME_REFUEL__ISFULLREFUEL)) {
				db.execSQL("ALTER TABLE " + TABLE_NAME_REFUEL + " ADD "
						+ COL_NAME_REFUEL__ISFULLREFUEL + " TEXT DEFAULT 'N' ");
			}
			createExpenses(db, true);
		}

		private void upgradeDbTo210(SQLiteDatabase db, int oldVersion)
				throws SQLException {
			String updSql = "";

			createCurrencyRateTable(db);
			if (!columnExists(db, TABLE_NAME_REFUEL,
					COL_NAME_REFUEL__QUANTITYENTERED)) {
				updSql = "ALTER TABLE " + TABLE_NAME_REFUEL + " ADD "
						+ COL_NAME_REFUEL__QUANTITYENTERED + " NUMERIC NULL ";
				db.execSQL(updSql);
				updSql = "UPDATE " + TABLE_NAME_REFUEL + " SET "
						+ COL_NAME_REFUEL__QUANTITYENTERED + " = "
						+ COL_NAME_REFUEL__QUANTITY;
				db.execSQL(updSql);
			}
			if (!columnExists(db, TABLE_NAME_REFUEL,
					COL_NAME_REFUEL__UOMVOLUMEENTERED_ID)) {
				updSql = "ALTER TABLE " + TABLE_NAME_REFUEL + " ADD "
						+ COL_NAME_REFUEL__UOMVOLUMEENTERED_ID
						+ " INTEGER NULL ";
				db.execSQL(updSql);
				updSql = "UPDATE " + TABLE_NAME_REFUEL + " SET "
						+ COL_NAME_REFUEL__UOMVOLUMEENTERED_ID + " = "
						+ COL_NAME_REFUEL__UOMVOLUME_ID;
				db.execSQL(updSql);
				updSql = "UPDATE "
						+ TABLE_NAME_REFUEL
						+ " SET "
						+ COL_NAME_REFUEL__UOMVOLUME_ID
						+ " = "
						+ "(SELECT "
						+ COL_NAME_CAR__UOMVOLUME_ID
						+ " "
						+ "FROM "
						+ TABLE_NAME_CAR
						+ " "
						+ "WHERE "
						+ COL_NAME_GEN_ROWID
						+ " = "
						+ sqlConcatTableColumn(TABLE_NAME_REFUEL,
								COL_NAME_REFUEL__CAR_ID) + ") ";
				db.execSQL(updSql);
			}
			if (!columnExists(db, TABLE_NAME_REFUEL,
					COL_NAME_REFUEL__PRICEENTERED)) {
				updSql = "ALTER TABLE " + TABLE_NAME_REFUEL + " ADD "
						+ COL_NAME_REFUEL__PRICEENTERED + " NUMERIC NULL ";
				db.execSQL(updSql);
				updSql = "UPDATE " + TABLE_NAME_REFUEL + " SET "
						+ COL_NAME_REFUEL__PRICEENTERED + " = "
						+ COL_NAME_REFUEL__PRICE;
				db.execSQL(updSql);
			}
			if (!columnExists(db, TABLE_NAME_REFUEL,
					COL_NAME_REFUEL__CURRENCYENTERED_ID)) {
				updSql = "ALTER TABLE " + TABLE_NAME_REFUEL + " ADD "
						+ COL_NAME_REFUEL__CURRENCYENTERED_ID
						+ " INTEGER NULL ";
				db.execSQL(updSql);
				updSql = "UPDATE " + TABLE_NAME_REFUEL + " SET "
						+ COL_NAME_REFUEL__CURRENCYENTERED_ID + " = "
						+ COL_NAME_REFUEL__CURRENCY_ID;
				db.execSQL(updSql);
				updSql = "UPDATE "
						+ TABLE_NAME_REFUEL
						+ " SET "
						+ COL_NAME_REFUEL__CURRENCY_ID
						+ " = "
						+ "(SELECT "
						+ COL_NAME_CAR__CURRENCY_ID
						+ " FROM "
						+ TABLE_NAME_CAR
						+ " WHERE "
						+ sqlConcatTableColumn(TABLE_NAME_CAR,
								COL_NAME_GEN_ROWID)
						+ " = "
						+ sqlConcatTableColumn(TABLE_NAME_REFUEL,
								COL_NAME_REFUEL__CAR_ID) + ") ";
				db.execSQL(updSql);
				Cursor c = db.rawQuery("SELECT COUNT(*) " + "FROM "
						+ TABLE_NAME_REFUEL + " " + "WHERE "
						+ COL_NAME_REFUEL__CURRENCY_ID + " <> "
						+ COL_NAME_REFUEL__CURRENCYENTERED_ID, null);
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
			if (!columnExists(db, TABLE_NAME_REFUEL,
					COL_NAME_REFUEL__CURRENCYRATE)) {
				updSql = "ALTER TABLE " + TABLE_NAME_REFUEL + " ADD "
						+ COL_NAME_REFUEL__CURRENCYRATE + " NUMERIC NULL ";
				db.execSQL(updSql);
				updSql = "UPDATE " + TABLE_NAME_REFUEL + " SET "
						+ COL_NAME_REFUEL__CURRENCYRATE + " = 1 ";
				db.execSQL(updSql);
			}
			if (!columnExists(db, TABLE_NAME_REFUEL,
					COL_NAME_REFUEL__UOMVOLCONVERSIONRATE)) {
				updSql = "ALTER TABLE " + TABLE_NAME_REFUEL + " ADD "
						+ COL_NAME_REFUEL__UOMVOLCONVERSIONRATE
						+ " NUMERIC NULL ";
				db.execSQL(updSql);
				updSql = "UPDATE " + TABLE_NAME_REFUEL + " SET "
						+ COL_NAME_REFUEL__UOMVOLCONVERSIONRATE + " = 1 ";
				db.execSQL(updSql);
				updSql = "UPDATE "
						+ TABLE_NAME_REFUEL
						+ " SET "
						+ COL_NAME_REFUEL__UOMVOLCONVERSIONRATE
						+ " = "
						+ "(SELECT "
						+ COL_NAME_UOMCONVERSION__RATE
						+ " "
						+ "FROM "
						+ TABLE_NAME_UOMCONVERSION
						+ " "
						+ "WHERE "
						+ COL_NAME_UOMCONVERSION__UOMFROM_ID
						+ " = "
						+ sqlConcatTableColumn(TABLE_NAME_REFUEL,
								COL_NAME_REFUEL__UOMVOLUMEENTERED_ID)
						+ " "
						+ "AND "
						+ COL_NAME_UOMCONVERSION__UOMTO_ID
						+ " = "
						+ sqlConcatTableColumn(TABLE_NAME_REFUEL,
								COL_NAME_REFUEL__UOMVOLUME_ID)
						+ "), "
						+ COL_NAME_REFUEL__QUANTITY
						+ " = "
						+ "ROUND( "
						+ COL_NAME_REFUEL__QUANTITYENTERED
						+ " * "
						+ "(SELECT "
						+ COL_NAME_UOMCONVERSION__RATE
						+ " "
						+ "FROM "
						+ TABLE_NAME_UOMCONVERSION
						+ " "
						+ "WHERE "
						+ COL_NAME_UOMCONVERSION__UOMFROM_ID
						+ " = "
						+ sqlConcatTableColumn(TABLE_NAME_REFUEL,
								COL_NAME_REFUEL__UOMVOLUMEENTERED_ID)
						+ " "
						+ "AND "
						+ COL_NAME_UOMCONVERSION__UOMTO_ID
						+ " = "
						+ sqlConcatTableColumn(TABLE_NAME_REFUEL,
								COL_NAME_REFUEL__UOMVOLUME_ID) + "), 2 ) "
						+ "WHERE " + COL_NAME_REFUEL__UOMVOLUME_ID + " <> "
						+ COL_NAME_REFUEL__UOMVOLUMEENTERED_ID;
				db.execSQL(updSql);
			}

			if (!columnExists(db, TABLE_NAME_EXPENSE,
					COL_NAME_EXPENSE__AMOUNTENTERED)) {
				updSql = "ALTER TABLE " + TABLE_NAME_EXPENSE + " ADD "
						+ COL_NAME_EXPENSE__AMOUNTENTERED + " NUMERIC NULL ";
				db.execSQL(updSql);
				updSql = "UPDATE " + TABLE_NAME_EXPENSE + " SET "
						+ COL_NAME_EXPENSE__AMOUNTENTERED + " = "
						+ COL_NAME_EXPENSE__AMOUNT;
				db.execSQL(updSql);
			}

			if (!columnExists(db, TABLE_NAME_EXPENSE,
					COL_NAME_EXPENSE__CURRENCYENTERED_ID)) {
				updSql = "ALTER TABLE " + TABLE_NAME_EXPENSE + " ADD "
						+ COL_NAME_EXPENSE__CURRENCYENTERED_ID
						+ " INTEGER NULL ";
				db.execSQL(updSql);
				updSql = "UPDATE " + TABLE_NAME_EXPENSE + " SET "
						+ COL_NAME_EXPENSE__CURRENCYENTERED_ID + " = "
						+ COL_NAME_EXPENSE__CURRENCY_ID;
				db.execSQL(updSql);
				updSql = "UPDATE "
						+ TABLE_NAME_EXPENSE
						+ " SET "
						+ COL_NAME_EXPENSE__CURRENCY_ID
						+ " = "
						+ "(SELECT "
						+ COL_NAME_CAR__CURRENCY_ID
						+ " FROM "
						+ TABLE_NAME_CAR
						+ " WHERE "
						+ sqlConcatTableColumn(TABLE_NAME_CAR,
								COL_NAME_GEN_ROWID)
						+ " = "
						+ sqlConcatTableColumn(TABLE_NAME_EXPENSE,
								COL_NAME_EXPENSE__CAR_ID) + ") ";
				db.execSQL(updSql);
				Cursor c = db
						.rawQuery(
								"SELECT COUNT(*) "
										+ "FROM "
										+ TABLE_NAME_EXPENSE
										+ " "
										+ "WHERE "
										+ COL_NAME_EXPENSE__CURRENCY_ID
										+ " <> "
										+ COL_NAME_EXPENSE__CURRENCYENTERED_ID
										+ " "
										+ "AND COALESCE("
										+ COL_NAME_EXPENSE__FROMTABLE
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

			if (!columnExists(db, TABLE_NAME_EXPENSE,
					COL_NAME_EXPENSE__CURRENCYRATE)) {
				updSql = "ALTER TABLE " + TABLE_NAME_EXPENSE + " ADD "
						+ COL_NAME_EXPENSE__CURRENCYRATE + " NUMERIC NULL ";
				db.execSQL(updSql);
				updSql = "UPDATE " + TABLE_NAME_EXPENSE + " SET "
						+ COL_NAME_EXPENSE__CURRENCYRATE + " = 1";
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
			if (!columnExists(db, TABLE_NAME_REFUEL, COL_NAME_REFUEL__AMOUNT)) {
				updSql = "ALTER TABLE " + TABLE_NAME_REFUEL + " ADD "
						+ COL_NAME_REFUEL__AMOUNT + " NUMERIC NULL ";
				db.execSQL(updSql);
				updSql = "UPDATE " + TABLE_NAME_REFUEL + " SET "
						+ COL_NAME_REFUEL__AMOUNT + " = "
						+ COL_NAME_REFUEL__QUANTITYENTERED + " * "
						+ COL_NAME_REFUEL__PRICE;
				db.execSQL(updSql);
			}
			if (!columnExists(db, TABLE_NAME_REFUEL,
					COL_NAME_REFUEL__AMOUNTENTERED)) {
				updSql = "ALTER TABLE " + TABLE_NAME_REFUEL + " ADD "
						+ COL_NAME_REFUEL__AMOUNTENTERED + " NUMERIC NULL ";
				db.execSQL(updSql);
				updSql = "UPDATE " + TABLE_NAME_REFUEL + " SET "
						+ COL_NAME_REFUEL__AMOUNTENTERED + " = "
						+ COL_NAME_REFUEL__QUANTITYENTERED + " * "
						+ COL_NAME_REFUEL__PRICEENTERED;
				db.execSQL(updSql);
			}
		}

		private void upgradeDbTo330(SQLiteDatabase db, int oldVersion)
				throws SQLException {
			String updSql = "";
			createBPartnerTable(db);

			if (!columnExists(db, TABLE_NAME_EXPENSE,
					COL_NAME_EXPENSE__QUANTITY)) {
				updSql = "ALTER TABLE " + TABLE_NAME_EXPENSE + " ADD "
						+ COL_NAME_EXPENSE__QUANTITY + " NUMERIC NULL ";
				db.execSQL(updSql);
			}
			if (!columnExists(db, TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__PRICE)) {
				updSql = "ALTER TABLE " + TABLE_NAME_EXPENSE + " ADD "
						+ COL_NAME_EXPENSE__PRICE + " NUMERIC NULL ";
				db.execSQL(updSql);
			}
			if (!columnExists(db, TABLE_NAME_EXPENSE,
					COL_NAME_EXPENSE__PRICEENTERED)) {
				updSql = "ALTER TABLE " + TABLE_NAME_EXPENSE + " ADD "
						+ COL_NAME_EXPENSE__PRICEENTERED + " NUMERIC NULL ";
				db.execSQL(updSql);
			}
			if (!columnExists(db, TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__UOM_ID)) {
				updSql = "ALTER TABLE " + TABLE_NAME_EXPENSE + " ADD "
						+ COL_NAME_EXPENSE__UOM_ID + " INTEGER NULL ";
				db.execSQL(updSql);
			}

			if (!columnExists(db, TABLE_NAME_EXPENSE,
					COL_NAME_EXPENSE__BPARTNER_ID)) {
				updSql = "ALTER TABLE " + TABLE_NAME_EXPENSE + " ADD "
						+ COL_NAME_EXPENSE__BPARTNER_ID + " INTEGER NULL ";
				db.execSQL(updSql);
			}
			if (!columnExists(db, TABLE_NAME_EXPENSE,
					COL_NAME_EXPENSE__BPARTNER_LOCATION_ID)) {
				updSql = "ALTER TABLE " + TABLE_NAME_EXPENSE + " ADD "
						+ COL_NAME_EXPENSE__BPARTNER_LOCATION_ID
						+ " INTEGER NULL ";
				db.execSQL(updSql);
			}

			if (!columnExists(db, TABLE_NAME_REFUEL,
					COL_NAME_REFUEL__BPARTNER_ID)) {
				updSql = "ALTER TABLE " + TABLE_NAME_REFUEL + " ADD "
						+ COL_NAME_REFUEL__BPARTNER_ID + " INTEGER NULL ";
				db.execSQL(updSql);
			}
			if (!columnExists(db, TABLE_NAME_REFUEL,
					COL_NAME_REFUEL__BPARTNER_LOCATION_ID)) {
				updSql = "ALTER TABLE " + TABLE_NAME_REFUEL + " ADD "
						+ COL_NAME_REFUEL__BPARTNER_LOCATION_ID
						+ " INTEGER NULL ";
				db.execSQL(updSql);
			}
		}

		private void upgradeDbTo340(SQLiteDatabase db, int oldVersion)
				throws SQLException {
			// createAddOnTable(db);
			createTagTable(db);
			String updSql = "";
			if (!columnExists(db, TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__TAG_ID)) {
				updSql = "ALTER TABLE " + TABLE_NAME_MILEAGE + " ADD "
						+ COL_NAME_MILEAGE__TAG_ID + " INTEGER NULL ";
				db.execSQL(updSql);
			}
			if (!columnExists(db, TABLE_NAME_REFUEL, COL_NAME_REFUEL__TAG_ID)) {
				updSql = "ALTER TABLE " + TABLE_NAME_REFUEL + " ADD "
						+ COL_NAME_REFUEL__TAG_ID + " INTEGER NULL ";

				db.execSQL(updSql);
			}
			if (!columnExists(db, TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__TAG_ID)) {
				updSql = "ALTER TABLE " + TABLE_NAME_EXPENSE + " ADD "
						+ COL_NAME_EXPENSE__TAG_ID + " INTEGER NULL ";
				db.execSQL(updSql);

			}
			if (!columnExists(db, TABLE_NAME_GPSTRACK,
					COL_NAME_GPSTRACK__TAG_ID)) {
				updSql = "ALTER TABLE " + TABLE_NAME_GPSTRACK + " ADD "
						+ COL_NAME_GPSTRACK__TAG_ID + " INTEGER NULL ";
				db.execSQL(updSql);
			}
		}

		private void upgradeDbTo350(SQLiteDatabase db, int oldVersion)
				throws SQLException {
			AddOnDBAdapter.createAddOnTable(db);
			AddOnDBAdapter.createAddOnBKScheduleTable(db);
		}

		private void upgradeDbTo355(SQLiteDatabase db, int oldVersion)
				throws SQLException {
			String updSql = null;
			if (!columnExists(db, TABLE_NAME_EXPENSECATEGORY,
					COL_NAME_EXPENSECATEGORY__ISFUEL)) {
				updSql = "ALTER TABLE " + TABLE_NAME_EXPENSECATEGORY + " ADD "
						+ COL_NAME_EXPENSECATEGORY__ISFUEL
						+ " TEXT DEFAULT 'N' ";
				db.execSQL(updSql);

				updSql = "UPDATE " + TABLE_NAME_EXPENSECATEGORY + " SET "
						+ COL_NAME_EXPENSECATEGORY__ISFUEL + " = 'Y' "
						+ " WHERE " + COL_NAME_GEN_ROWID + " = 1";

				db.execSQL(updSql);
			}
		}

		private void upgradeDbTo356(SQLiteDatabase db, int oldVersion)
				throws SQLException {
			AddOnDBAdapter.createAddOnSecureBKSettingsTable(db);
		}

		private void upgradeDbTo357(SQLiteDatabase db, int oldVersion)
				throws SQLException {
			String sql = "DROP TABLE IF EXISTS " + TABLE_NAME_TASKTYPE;
			db.execSQL(sql);
			sql = "DROP TABLE IF EXISTS " + TABLE_NAME_TASK_CAR;
			db.execSQL(sql);
			sql = "DROP TABLE IF EXISTS " + TABLE_NAME_TASK;
			db.execSQL(sql);
			sql = "DROP TABLE IF EXISTS " + TABLE_NAME_TODO;
			db.execSQL(sql);
			createTaskToDoTables(db);
		}

		private void upgradeDbTo400(SQLiteDatabase db, int oldVersion)
				throws SQLException {
			String updSql = null;
			if (!columnExists(db, TABLE_NAME_GPSTRACK,
					COL_NAME_GPSTRACK__TOTALPAUSETIME)) {
				updSql = "ALTER TABLE " + TABLE_NAME_GPSTRACK + " ADD "
						+ COL_NAME_GPSTRACK__TOTALPAUSETIME + " NUMBER NULL ";
				db.execSQL(updSql);

				updSql = "UPDATE " + TABLE_NAME_GPSTRACK + " SET "
						+ COL_NAME_GPSTRACK__TOTALPAUSETIME + " = 0";

				db.execSQL(updSql);
			}
		}

		private void upgradeDbTo401(SQLiteDatabase db, int oldVersion)
				throws SQLException {
			String updSql = null;
			createReimbursementCarRatesTable(db);
			if (!columnExists(db, TABLE_NAME_EXPENSETYPE,
					COL_NAME_EXPENSETYPE__ISCALCULATEREIMBURSEMENT)) {
				updSql = "ALTER TABLE " + TABLE_NAME_EXPENSETYPE + " ADD "
						+ COL_NAME_EXPENSETYPE__ISCALCULATEREIMBURSEMENT
						+ " TEXT NOT NULL DEFAULT 'N'";
				db.execSQL(updSql);
			}
			if (!columnExists(db, TABLE_NAME_MILEAGE,
					COL_NAME_MILEAGE__REIMBURSEMENT_RATE)) {
				updSql = "ALTER TABLE " + TABLE_NAME_MILEAGE + " ADD "
						+ COL_NAME_MILEAGE__REIMBURSEMENT_RATE
						+ " NUMERIC NOT NULL DEFAULT 0";
				db.execSQL(updSql);
			}
			if (!columnExists(db, TABLE_NAME_MILEAGE,
					COL_NAME_MILEAGE__REIMBURSEMENT_VALUE)) {
				updSql = "ALTER TABLE " + TABLE_NAME_MILEAGE + " ADD "
						+ COL_NAME_MILEAGE__REIMBURSEMENT_VALUE
						+ " NUMERIC NOT NULL DEFAULT 0";
				db.execSQL(updSql);
			}
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
			db.execSQL(CREATE_SQL_EXPENSECATEGORY_TABLE);
			String colPart = "INSERT INTO " + TABLE_NAME_EXPENSECATEGORY
					+ " ( " + COL_NAME_GEN_NAME + ", " + COL_NAME_GEN_ISACTIVE
					+ ", " + COL_NAME_GEN_USER_COMMENT + ", "
					+ COL_NAME_EXPENSECATEGORY__ISEXCLUDEFROMMILEAGECOST + ", "
					+ COL_NAME_EXPENSECATEGORY__ISFUEL + " " + ") ";

			// fuel types
			db.execSQL(colPart + "VALUES ( " + "'"
					+ mResource.getString(R.string.DB_FuelType_Diesel1D)
					+ "', " + "'Y', " + "'"
					+ mResource.getString(R.string.DB_FuelType_Diesel1DComment)
					+ "', " + "'N', 'Y' )");
			db.execSQL(colPart + "VALUES ( " + "'"
					+ mResource.getString(R.string.DB_FuelType_Diesel2D)
					+ "', " + "'Y', " + "'"
					+ mResource.getString(R.string.DB_FuelType_Diesel2DComment)
					+ "', " + "'N', 'Y' )");
			db.execSQL(colPart
					+ "VALUES ( "
					+ "'"
					+ mResource.getString(R.string.DB_FuelType_DieselBio)
					+ "', "
					+ "'Y', "
					+ "'"
					+ mResource
							.getString(R.string.DB_FuelType_DieselBioComment)
					+ "', " + "'N', 'Y' )");
			db.execSQL(colPart
					+ "VALUES ( "
					+ "'"
					+ mResource.getString(R.string.DB_FuelType_DieselSynthetic)
					+ "', "
					+ "'Y', "
					+ "'"
					+ mResource
							.getString(R.string.DB_FuelType_DieselSyntheticComment)
					+ "', " + "'N', 'Y' )");
			db.execSQL(colPart
					+ "VALUES ( "
					+ "'"
					+ mResource.getString(R.string.DB_FuelType_GasolineRegular)
					+ "', "
					+ "'Y', "
					+ "'"
					+ mResource
							.getString(R.string.DB_FuelType_GasolineRegularComment)
					+ "', " + "'N', 'Y' )");
			db.execSQL(colPart
					+ "VALUES ( "
					+ "'"
					+ mResource
							.getString(R.string.DB_FuelType_GasolineMidgrade)
					+ "', "
					+ "'Y', "
					+ "'"
					+ mResource
							.getString(R.string.DB_FuelType_GasolineMidgradeComment)
					+ "', " + "'N', 'Y' )");
			db.execSQL(colPart
					+ "VALUES ( "
					+ "'"
					+ mResource.getString(R.string.DB_FuelType_GasolinePremium)
					+ "', "
					+ "'Y', "
					+ "'"
					+ mResource
							.getString(R.string.DB_FuelType_GasolinePremiumComment)
					+ "', " + "'N', 'Y' )");
			db.execSQL(colPart + "VALUES ( " + "'"
					+ mResource.getString(R.string.DB_FuelType_LPG) + "', "
					+ "'Y', " + "'"
					+ mResource.getString(R.string.DB_FuelType_LPGComment)
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
			db.execSQL(CREATE_SQL_EXPENSE_TABLE);
			if (!isUpdate) {
				return;
			}
			// initialize refuel expenses
			String sql = "INSERT INTO " + TABLE_NAME_EXPENSE + "( "
					+ COL_NAME_GEN_NAME + ", " + COL_NAME_GEN_USER_COMMENT
					+ ", " + COL_NAME_GEN_ISACTIVE + ", "
					+ COL_NAME_EXPENSE__CAR_ID + ", "
					+ COL_NAME_EXPENSE__DRIVER_ID + ", "
					+ COL_NAME_EXPENSE__EXPENSECATEGORY_ID + ", "
					+ COL_NAME_EXPENSE__EXPENSETYPE_ID + ", "
					+ COL_NAME_EXPENSE__AMOUNT + ", "
					+ COL_NAME_EXPENSE__CURRENCY_ID + ", "
					+ COL_NAME_EXPENSE__DATE + ", "
					+ COL_NAME_EXPENSE__DOCUMENTNO + ", "
					+ COL_NAME_EXPENSE__INDEX + ", "
					+ COL_NAME_EXPENSE__FROMTABLE + ", "
					+ COL_NAME_EXPENSE__FROMRECORD_ID + " " + ") " + "SELECT "
					+ COL_NAME_GEN_NAME + ", " + COL_NAME_GEN_USER_COMMENT
					+ ", " + COL_NAME_GEN_ISACTIVE + ", "
					+ COL_NAME_REFUEL__CAR_ID + ", "
					+ COL_NAME_REFUEL__DRIVER_ID + ", "
					+ COL_NAME_REFUEL__EXPENSECATEGORY_ID + ", "
					+ COL_NAME_REFUEL__EXPENSETYPE_ID + ", "
					+ COL_NAME_REFUEL__QUANTITY + " * "
					+ COL_NAME_REFUEL__PRICE + ", "
					+ COL_NAME_REFUEL__CURRENCY_ID + ", "
					+ COL_NAME_REFUEL__DATE + ", "
					+ COL_NAME_REFUEL__DOCUMENTNO + ", "
					+ COL_NAME_REFUEL__INDEX + ", " + "'Refuel' " + ", "
					+ COL_NAME_GEN_ROWID + " " + "FROM " + TABLE_NAME_REFUEL;
			db.execSQL(sql);
		}

		private void createCurrencyTable(SQLiteDatabase db) throws SQLException {
			// currency table name
			db.execSQL(CREATE_SQL_CURRENCY_TABLE);
			// insert some currencies
			String colPart = "INSERT INTO " + TABLE_NAME_CURRENCY + " ( "
					+ COL_NAME_GEN_NAME + ", " + COL_NAME_GEN_ISACTIVE + ", "
					+ COL_NAME_GEN_USER_COMMENT + ", "
					+ COL_NAME_CURRENCY__CODE + ") ";
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
			db.execSQL(CREATE_SQL_CURRENCYRATE_TABLE);
		}
	}

	private void createIndexes(SQLiteDatabase db) {
		db.execSQL("CREATE INDEX IF NOT EXISTS " + TABLE_NAME_GPSTRACK
				+ "_IX1 " + "ON " + TABLE_NAME_GPSTRACK + " ("
				+ COL_NAME_GPSTRACK__CAR_ID + ")");
		db.execSQL("CREATE INDEX IF NOT EXISTS " + TABLE_NAME_GPSTRACK
				+ "_IX2 " + "ON " + TABLE_NAME_GPSTRACK + " ("
				+ COL_NAME_GPSTRACK__DRIVER_ID + ")");
		db.execSQL("CREATE INDEX IF NOT EXISTS " + TABLE_NAME_GPSTRACK
				+ "_IX3 " + "ON " + TABLE_NAME_GPSTRACK + " ("
				+ COL_NAME_GPSTRACK__MILEAGE_ID + " DESC )");
		db.execSQL("CREATE INDEX IF NOT EXISTS " + TABLE_NAME_GPSTRACK
				+ "_IX4 " + "ON " + TABLE_NAME_GPSTRACK + " ("
				+ COL_NAME_GPSTRACK__DATE + " DESC )");
		db.execSQL("CREATE INDEX IF NOT EXISTS " + TABLE_NAME_GPSTRACKDETAIL
				+ "_IX1 " + "ON " + TABLE_NAME_GPSTRACKDETAIL + " ("
				+ COL_NAME_GPSTRACKDETAIL__GPSTRACK_ID + ")");
		// create indexes on mileage table
		db.execSQL("CREATE INDEX IF NOT EXISTS " + TABLE_NAME_MILEAGE + "_IX1 "
				+ "ON " + TABLE_NAME_MILEAGE + " (" + COL_NAME_MILEAGE__CAR_ID
				+ ")");
		db.execSQL("CREATE INDEX IF NOT EXISTS " + TABLE_NAME_MILEAGE + "_IX2 "
				+ "ON " + TABLE_NAME_MILEAGE + " ("
				+ COL_NAME_MILEAGE__DRIVER_ID + ")");
		db.execSQL("CREATE INDEX IF NOT EXISTS " + TABLE_NAME_MILEAGE + "_IX3 "
				+ "ON " + TABLE_NAME_MILEAGE + " (" + COL_NAME_MILEAGE__DATE
				+ " DESC )");
		db.execSQL("CREATE INDEX IF NOT EXISTS " + TABLE_NAME_MILEAGE + "_IX4 "
				+ "ON " + TABLE_NAME_MILEAGE + " ("
				+ COL_NAME_MILEAGE__INDEXSTOP + " DESC )");
		db.execSQL("CREATE INDEX IF NOT EXISTS " + TABLE_NAME_MILEAGE + "_IX5 "
				+ "ON " + TABLE_NAME_MILEAGE + " (" + COL_NAME_GEN_USER_COMMENT
				+ ")");

		db.execSQL("CREATE INDEX IF NOT EXISTS " + TABLE_NAME_REFUEL + "_IX1 "
				+ "ON " + TABLE_NAME_REFUEL + " (" + COL_NAME_MILEAGE__DATE
				+ " DESC )");
		db.execSQL("CREATE INDEX IF NOT EXISTS " + TABLE_NAME_REFUEL + "_IX2 "
				+ "ON " + TABLE_NAME_REFUEL + " (" + COL_NAME_GEN_USER_COMMENT
				+ ")");
		db.execSQL("CREATE INDEX IF NOT EXISTS " + TABLE_NAME_REFUEL + "_IX3 "
				+ "ON " + TABLE_NAME_REFUEL + " ("
				+ COL_NAME_REFUEL__ISFULLREFUEL + ")");
		db.execSQL("CREATE INDEX IF NOT EXISTS " + TABLE_NAME_REFUEL + "_IX4 "
				+ "ON " + TABLE_NAME_REFUEL + " (" + COL_NAME_REFUEL__INDEX
				+ ")");

		db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS " + TABLE_NAME_TASK_CAR
				+ "_UK1 " + "ON " + TABLE_NAME_TASK_CAR + " ("
				+ COL_NAME_TASK_CAR__CAR_ID + ", " + COL_NAME_TASK_CAR__TASK_ID
				+ ")");

		db.execSQL("CREATE INDEX IF NOT EXISTS " + TABLE_NAME_TODO + "_IX1 "
				+ "ON " + TABLE_NAME_TODO + " (" + COL_NAME_TODO__TASK_ID + ")");
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
				Intent intent = new Intent(mCtx, FileMailer.class);
				intent.putExtra("bkFile", bkFolder);
				intent.putExtra("attachName", bkFileName);
				mCtx.startService(intent);
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
