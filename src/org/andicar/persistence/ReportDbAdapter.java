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
import android.database.Cursor;
import android.os.Bundle;
import java.util.Iterator;
import java.util.Set;
import org.andicar.utils.StaticValues;

/**
 *
 * @author miki
 */
public class ReportDbAdapter extends MainDbAdapter{

    public static String FIRST_LINE_LIST_NAME = "FIRSTLINE";
    public static String SECOND_LINE_LIST_NAME = "SECONDLINE";
    public static String THIRD_LINE_LIST_NAME = "THIRDLINE";
    public static String FOURTH_LINE_LIST_NAME = "FOURTHLINE";
    public static String FIFTH_LINE_LIST_NAME = "FIFTHLINE";
    protected String mReportSqlName;
    protected Bundle mSearchCondition;

    public static String[] genericReportListViewSelectCols = {
        GEN_COL_ROWID_NAME,
        FIRST_LINE_LIST_NAME,
        SECOND_LINE_LIST_NAME,
        THIRD_LINE_LIST_NAME
    };

    public static String[] extendedReportListViewSelectCols = {
        GEN_COL_ROWID_NAME,
        FIRST_LINE_LIST_NAME,
        SECOND_LINE_LIST_NAME,
        THIRD_LINE_LIST_NAME,
        FOURTH_LINE_LIST_NAME,
        FIFTH_LINE_LIST_NAME
    };

    //used in main activity and mileage list activity
    public static String mileageListViewSelect =
            "SELECT " +
//                sqlConcatTableColumn(MILEAGE_TABLE_NAME, MILEAGE_COL_INDEXSTOP_NAME) + ", " + //#0
                sqlConcatTableColumn(MILEAGE_TABLE_NAME, GEN_COL_ROWID_NAME) + ", " + //#0
                sqlConcatTableColumn(CAR_TABLE_NAME, GEN_COL_NAME_NAME) + " || '; ' || " + 
                	sqlConcatTableColumn(DRIVER_TABLE_NAME, GEN_COL_NAME_NAME) + " || '; ' || " +
                    sqlConcatTableColumn(EXPENSETYPE_TABLE_NAME, GEN_COL_NAME_NAME)  + " || '; ' || " +
                    " '[#1]' AS " + FIRST_LINE_LIST_NAME + ", " + //#1
                " '[#1] -> [#2] = [#3] ' || " +
                        sqlConcatTableColumn(UOM_TABLE_NAME, UOM_COL_CODE_NAME) +
                            " AS " + SECOND_LINE_LIST_NAME + ", " + //#2
                " COALESCE( " + sqlConcatTableColumn(TAG_TABLE_NAME, GEN_COL_NAME_NAME) + " || '; ', '') || " + 
                		sqlConcatTableColumn(MILEAGE_TABLE_NAME, GEN_COL_USER_COMMENT_NAME) + 
                        " AS " + THIRD_LINE_LIST_NAME + ", " + //#3
                sqlConcatTableColumn(GPSTRACK_TABLE_NAME, GEN_COL_ROWID_NAME) + " AS gpsTrackId, " + //#4
                sqlConcatTableColumn(MILEAGE_TABLE_NAME, MILEAGE_COL_DATE_NAME) + " AS Seconds, " + //#5
                sqlConcatTableColumn(MILEAGE_TABLE_NAME, MILEAGE_COL_INDEXSTART_NAME) + ", " + //#6
                sqlConcatTableColumn(MILEAGE_TABLE_NAME, MILEAGE_COL_INDEXSTOP_NAME) + ", " + //#7
                sqlConcatTableColumn(MILEAGE_TABLE_NAME, MILEAGE_COL_INDEXSTOP_NAME) + " - " + 
                	sqlConcatTableColumn(MILEAGE_TABLE_NAME, MILEAGE_COL_INDEXSTART_NAME) + " AS Mileage " + //#8
                
            " FROM " + MILEAGE_TABLE_NAME +
                    " JOIN " + EXPENSETYPE_TABLE_NAME +
                        " ON " + sqlConcatTableColumn(MILEAGE_TABLE_NAME, MILEAGE_COL_EXPENSETYPE_ID_NAME) + "=" +
                                            sqlConcatTableColumn(EXPENSETYPE_TABLE_NAME, GEN_COL_ROWID_NAME) +
                    " JOIN " + DRIVER_TABLE_NAME +
                        " ON " + sqlConcatTableColumn(MILEAGE_TABLE_NAME, MILEAGE_COL_DRIVER_ID_NAME) + "=" +
                                            sqlConcatTableColumn(DRIVER_TABLE_NAME, GEN_COL_ROWID_NAME) +
                    " JOIN " + UOM_TABLE_NAME +
                        " ON " + sqlConcatTableColumn(MILEAGE_TABLE_NAME, MILEAGE_COL_UOMLENGTH_ID_NAME) + "=" +
                                            sqlConcatTableColumn(UOM_TABLE_NAME, GEN_COL_ROWID_NAME) +
                    " JOIN " + CAR_TABLE_NAME +
                        " ON " + sqlConcatTableColumn(MILEAGE_TABLE_NAME, MILEAGE_COL_CAR_ID_NAME) + "=" +
                                            sqlConcatTableColumn(CAR_TABLE_NAME, GEN_COL_ROWID_NAME) +
                    " LEFT OUTER JOIN " + TAG_TABLE_NAME +
                    	" ON " + sqlConcatTableColumn(MILEAGE_TABLE_NAME, MILEAGE_COL_TAG_ID_NAME) + "=" +
                                        sqlConcatTableColumn(TAG_TABLE_NAME, GEN_COL_ROWID_NAME) +
                    " LEFT OUTER JOIN " + GPSTRACK_TABLE_NAME +
                    	" ON " + sqlConcatTableColumn(MILEAGE_TABLE_NAME, GEN_COL_ROWID_NAME) + "=" +
                                        sqlConcatTableColumn(GPSTRACK_TABLE_NAME, GPSTRACK_COL_MILEAGE_ID_NAME) +
            " WHERE 1=1 ";

    //used in exported report
    public static String mileageListReportSelect =
            "SELECT " +
                sqlConcatTableColumn(MILEAGE_TABLE_NAME, GEN_COL_ROWID_NAME) + " AS MileageId, " +
                sqlConcatTableColumn(MILEAGE_TABLE_NAME, GEN_COL_USER_COMMENT_NAME) + ", " +
                "DATETIME(" + sqlConcatTableColumn(MILEAGE_TABLE_NAME, MILEAGE_COL_DATE_NAME) +
                    ", 'unixepoch', 'localtime') AS " + MILEAGE_COL_DATE_NAME + ", " +

                "CASE strftime(\"%w\", " + sqlConcatTableColumn(MILEAGE_TABLE_NAME, MILEAGE_COL_DATE_NAME) +
                    ", 'unixepoch', 'localtime') " +
                    "WHEN \"0\" THEN '[#d0]' " +
                    "WHEN \"1\" THEN '[#d1]' " +
                    "WHEN \"2\" THEN '[#d2]' " +
                    "WHEN \"3\" THEN '[#d3]' " +
                    "WHEN \"4\" THEN '[#d4]' " +
                    "WHEN \"5\" THEN '[#d5]' " +
                    "WHEN \"6\" THEN '[#d6]' " +
                "END AS " + StaticValues.DAY_OF_WEEK_NAME + ", " +

                    sqlConcatTableColumn(CAR_TABLE_NAME, GEN_COL_NAME_NAME) + " AS CarName, " +
                sqlConcatTableColumn(DRIVER_TABLE_NAME, GEN_COL_NAME_NAME) + " AS DriverName, " +
                sqlConcatTableColumn(MILEAGE_TABLE_NAME, MILEAGE_COL_INDEXSTART_NAME) + " AS " + MILEAGE_COL_INDEXSTART_NAME + "_DTypeN, " +
                sqlConcatTableColumn(MILEAGE_TABLE_NAME, MILEAGE_COL_INDEXSTOP_NAME) + " AS " + MILEAGE_COL_INDEXSTOP_NAME + "_DTypeN, " +
                sqlConcatTableColumn(MILEAGE_TABLE_NAME, MILEAGE_COL_INDEXSTOP_NAME) + " - " +
                    sqlConcatTableColumn(MILEAGE_TABLE_NAME, MILEAGE_COL_INDEXSTART_NAME) + " AS Mileage_DTypeN, " +
                sqlConcatTableColumn(UOM_TABLE_NAME, UOM_COL_CODE_NAME) + " AS UomCode, " +
                sqlConcatTableColumn(EXPENSETYPE_TABLE_NAME, GEN_COL_NAME_NAME) + " AS ExpenseTypeName, " +
                " COALESCE( " + sqlConcatTableColumn(TAG_TABLE_NAME, GEN_COL_NAME_NAME) + " || '; ', '') AS Tag " +
//                sqlConcatTableColumn(MILEAGE_TABLE_NAME, MILEAGE_COL_DATE_NAME) + " AS Seconds " +
            " FROM " + MILEAGE_TABLE_NAME +
                    " JOIN " + EXPENSETYPE_TABLE_NAME +
                        " ON " + sqlConcatTableColumn(MILEAGE_TABLE_NAME, MILEAGE_COL_EXPENSETYPE_ID_NAME) + "=" +
                                            sqlConcatTableColumn(EXPENSETYPE_TABLE_NAME, GEN_COL_ROWID_NAME) +
                    " JOIN " + DRIVER_TABLE_NAME +
                        " ON " + sqlConcatTableColumn(MILEAGE_TABLE_NAME, MILEAGE_COL_DRIVER_ID_NAME) + "=" +
                                            sqlConcatTableColumn(DRIVER_TABLE_NAME, GEN_COL_ROWID_NAME) +
                    " JOIN " + UOM_TABLE_NAME +
                        " ON " + sqlConcatTableColumn(MILEAGE_TABLE_NAME, MILEAGE_COL_UOMLENGTH_ID_NAME) + "=" +
                                            sqlConcatTableColumn(UOM_TABLE_NAME, GEN_COL_ROWID_NAME) +
                    " JOIN " + CAR_TABLE_NAME +
                        " ON " + sqlConcatTableColumn(MILEAGE_TABLE_NAME, MILEAGE_COL_CAR_ID_NAME) + "=" +
                                            sqlConcatTableColumn(CAR_TABLE_NAME, GEN_COL_ROWID_NAME) +
                    " LEFT OUTER JOIN " + TAG_TABLE_NAME +
                    	" ON " + sqlConcatTableColumn(MILEAGE_TABLE_NAME, MILEAGE_COL_TAG_ID_NAME) + "=" +
                                    sqlConcatTableColumn(TAG_TABLE_NAME, GEN_COL_ROWID_NAME) +
            " WHERE 1=1 ";

    //used in main activity and refuel list activity
    public static String refuelListViewSelect =
            "SELECT " +
                sqlConcatTableColumn(REFUEL_TABLE_NAME, GEN_COL_ROWID_NAME) + ", " + //#0
                sqlConcatTableColumn(CAR_TABLE_NAME, GEN_COL_NAME_NAME) + " || '; ' || " +
                	sqlConcatTableColumn(DRIVER_TABLE_NAME, GEN_COL_NAME_NAME) + " || '; ' || " +
                    sqlConcatTableColumn(EXPENSETYPE_TABLE_NAME, GEN_COL_NAME_NAME)  + " || '; [#1]'" +
                        " AS " + FIRST_LINE_LIST_NAME + ", " + //#1
                 "'[#1] ' || " +
                        sqlConcatTableColumn(UOM_TABLE_NAME, UOM_COL_CODE_NAME) + " || " +
                " CASE WHEN " + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_UOMVOLUME_ID_NAME) + " <> " +
                                    sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_UOMVOLUMEENTERED_ID_NAME) + " " +
                        " THEN " + "' ([#2]'" +
                                    " || ' ' || " + sqlConcatTableColumn("DefaultVolumeUOM", UOM_COL_CODE_NAME) + " || ')' " +
                        " ELSE " + "'' " +
                " END " +
                        " || ' x [#3] ' || " +
                        	sqlConcatTableColumn(CURRENCY_TABLE_NAME, CURRENCY_COL_CODE_NAME) + " || " +
                        " CASE WHEN " + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_CURRENCY_ID_NAME) + " <> " +
                                            sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_CURRENCYENTERED_ID_NAME) + " " +
                                " THEN " + "' ([#4] ' || " + sqlConcatTableColumn("DefaultCurrency", CURRENCY_COL_CODE_NAME) + " || ')' " +
                                " ELSE " + "'' " +
                        " END " +

                        " || ' = [#5] ' || " +
                            sqlConcatTableColumn(CURRENCY_TABLE_NAME, CURRENCY_COL_CODE_NAME) + " || " +
                        " CASE WHEN " + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_CURRENCY_ID_NAME) + " <> " +
                                            sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_CURRENCYENTERED_ID_NAME) + " " +
                                " THEN " + "' ([#6] ' || " +
                                                        sqlConcatTableColumn("DefaultCurrency", CURRENCY_COL_CODE_NAME) + " || ')' " +
                                " ELSE " + "'' " +
                        " END " +
                            " || ' at [#7] ' || " +
                            sqlConcatTableColumn("CarLengthUOM", UOM_COL_CODE_NAME) +
                            " || ' (' || " + sqlConcatTableColumn(EXPENSECATEGORY_TABLE_NAME, GEN_COL_NAME_NAME) + " || ')' " +
//                            " || '[#8] (' || " + sqlConcatTableColumn(EXPENSECATEGORY_TABLE_NAME, GEN_COL_NAME_NAME) + " || ')' " +
                            " AS " + SECOND_LINE_LIST_NAME + ", " + //#2
                " COALESCE( " + sqlConcatTableColumn(TAG_TABLE_NAME, GEN_COL_NAME_NAME) + " || '; ', '') || " + 
            			sqlConcatTableColumn(REFUEL_TABLE_NAME, GEN_COL_USER_COMMENT_NAME) +
                        " AS " + THIRD_LINE_LIST_NAME + ", " + //#3
                sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_DATE_NAME) + " AS Seconds, " + //#4
                sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_QUANTITYENTERED_NAME) + " AS QtyEntered, " + //#5
                sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_QUANTITY_NAME) + " AS Qty, " + //#6
                sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_PRICEENTERED_NAME) + " AS PriceEntered, " + //#7
                sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_PRICE_NAME) + " AS Price, " + //#8
                sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_AMOUNTENTERED_NAME) + " AS AmountEntered, " + //#9
                sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_AMOUNT_NAME) + " AS Amount, " + //#10
                sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_INDEX_NAME) + " AS CarIndex, " + //#11
                sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_ISFULLREFUEL_NAME) + " " + //#12
//                "( SELECT " + REFUEL_COL_INDEX_NAME + " " +
//                	" FROM " + REFUEL_TABLE_NAME + " AS PreviousFullRefuel " +
//                	" WHERE " + isActiveCondition + 
//                			" AND " + REFUEL_COL_ISFULLREFUEL_NAME + " = 'Y' " +
//                			" AND " + sqlConcatTableColumn("PreviousFullRefuel", REFUEL_COL_CAR_ID_NAME) + " = " + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_CAR_ID_NAME) +
//                			" AND " + sqlConcatTableColumn("PreviousFullRefuel", REFUEL_COL_INDEX_NAME) + " < " + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_INDEX_NAME) +
//        			" ORDER BY " + REFUEL_COL_INDEX_NAME + " DESC " +
//        			" LIMIT 1 " +
//                ") AS PreviousFullRefuelIndex " + //#13
            " FROM " + REFUEL_TABLE_NAME +
                    " JOIN " + EXPENSECATEGORY_TABLE_NAME +
                        " ON " + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_EXPENSECATEGORY_ID_NAME) + "=" +
                                            sqlConcatTableColumn(EXPENSECATEGORY_TABLE_NAME, GEN_COL_ROWID_NAME) +
                    " JOIN " + EXPENSETYPE_TABLE_NAME +
                        " ON " + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_EXPENSETYPE_ID_NAME) + "=" +
                                            sqlConcatTableColumn(EXPENSETYPE_TABLE_NAME, GEN_COL_ROWID_NAME) +
                    " JOIN " + DRIVER_TABLE_NAME +
                        " ON " + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_DRIVER_ID_NAME) + "=" +
                                            sqlConcatTableColumn(DRIVER_TABLE_NAME, GEN_COL_ROWID_NAME) +
                    " JOIN " + UOM_TABLE_NAME +
                        " ON " + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_UOMVOLUMEENTERED_ID_NAME) + "=" +
                                            sqlConcatTableColumn(UOM_TABLE_NAME, GEN_COL_ROWID_NAME) +
                    " JOIN " + UOM_TABLE_NAME + " AS DefaultVolumeUOM " +
                        " ON " + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_UOMVOLUME_ID_NAME) + "=" +
                                            sqlConcatTableColumn("DefaultVolumeUOM", GEN_COL_ROWID_NAME) +
                    " JOIN " + CAR_TABLE_NAME +
                        " ON " + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_CAR_ID_NAME) + "=" +
                                            sqlConcatTableColumn(CAR_TABLE_NAME, GEN_COL_ROWID_NAME) +
                            " JOIN " + UOM_TABLE_NAME + " AS CarLengthUOM " +
                                " ON " + sqlConcatTableColumn(CAR_TABLE_NAME, CAR_COL_UOMLENGTH_ID_NAME) + "=" +
                                                    sqlConcatTableColumn("CarLengthUOM", GEN_COL_ROWID_NAME) +
                    " JOIN " + CURRENCY_TABLE_NAME +
                        " ON " + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_CURRENCYENTERED_ID_NAME) + "=" +
                                            sqlConcatTableColumn(CURRENCY_TABLE_NAME, GEN_COL_ROWID_NAME) +
                    " JOIN " + CURRENCY_TABLE_NAME + " AS DefaultCurrency " +
                        " ON " + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_CURRENCY_ID_NAME) + "=" +
                                            sqlConcatTableColumn("DefaultCurrency", GEN_COL_ROWID_NAME) +
                    " LEFT OUTER JOIN " + TAG_TABLE_NAME +
                    	" ON " + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_TAG_ID_NAME) + "=" +
                                    sqlConcatTableColumn(TAG_TABLE_NAME, GEN_COL_ROWID_NAME) +
                    " WHERE 1=1 ";

    //used in exported report
    public static String refuelListReportSelect =
            "SELECT " +
                sqlConcatTableColumn(REFUEL_TABLE_NAME, GEN_COL_ROWID_NAME) + " AS RefuelId, " +
                sqlConcatTableColumn(REFUEL_TABLE_NAME, GEN_COL_USER_COMMENT_NAME) + ", " +
                sqlConcatTableColumn(CAR_TABLE_NAME, GEN_COL_NAME_NAME) + " AS CarName, " +
                sqlConcatTableColumn(DRIVER_TABLE_NAME, GEN_COL_NAME_NAME) + " AS DriverName, " +
                sqlConcatTableColumn(EXPENSECATEGORY_TABLE_NAME, GEN_COL_NAME_NAME) + " AS FuelCategory, " +
                sqlConcatTableColumn(EXPENSETYPE_TABLE_NAME, GEN_COL_NAME_NAME) + " AS ExpenseTypeName, " +
                sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_INDEX_NAME) + " AS " + REFUEL_COL_INDEX_NAME + "_DTypeN, " +
                sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_QUANTITY_NAME) + " AS " + REFUEL_COL_QUANTITY_NAME + "_DTypeN, " +
                sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_ISFULLREFUEL_NAME) + ", " +
                sqlConcatTableColumn(UOM_TABLE_NAME, UOM_COL_CODE_NAME) + " AS UOMCode, " +
                sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_PRICE_NAME) + " AS " + REFUEL_COL_PRICE_NAME + "_DTypeN, " +
                sqlConcatTableColumn(CURRENCY_TABLE_NAME, CURRENCY_COL_CODE_NAME) + " AS CurrencyCode, " +
                "DATETIME(" + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_DATE_NAME) + ", 'unixepoch', 'localtime') AS Date, " +

                "CASE strftime(\"%w\", " + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_DATE_NAME) +
                    ", 'unixepoch', 'localtime') " +
                    "WHEN \"0\" THEN '[#d0]' " +
                    "WHEN \"1\" THEN '[#d1]' " +
                    "WHEN \"2\" THEN '[#d2]' " +
                    "WHEN \"3\" THEN '[#d3]' " +
                    "WHEN \"4\" THEN '[#d4]' " +
                    "WHEN \"5\" THEN '[#d5]' " +
                    "WHEN \"6\" THEN '[#d6]' " +
                "END AS " + StaticValues.DAY_OF_WEEK_NAME + ", " +

                sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_QUANTITYENTERED_NAME) + " AS " + REFUEL_COL_QUANTITYENTERED_NAME + "_DTypeN, " +
                sqlConcatTableColumn("UomVolEntered", UOM_COL_CODE_NAME) + " AS UomEntered, " +
                sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_UOMVOLCONVERSIONRATE_NAME) + ", " +
                sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_PRICEENTERED_NAME) + " AS " + REFUEL_COL_PRICEENTERED_NAME + "_DTypeN, " +
                sqlConcatTableColumn("CurrencyEntered", CURRENCY_COL_CODE_NAME) + " AS CurrencyEntered, " +
                sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_CURRENCYRATE_NAME) + " AS " + REFUEL_COL_CURRENCYRATE_NAME + "_DTypeN, " +
                sqlConcatTableColumn(BPARTNER_TABLE_NAME, GEN_COL_NAME_NAME) + " AS Vendor, " +
                sqlConcatTableColumn(BPARTNER_LOCATION_TABLE_NAME, GEN_COL_NAME_NAME) +
                        " || COALESCE( '; ' || " + sqlConcatTableColumn(BPARTNER_LOCATION_TABLE_NAME, BPARTNER_LOCATION_COL_ADDRESS_NAME) + ", '') " +
                        " || COALESCE( '; ' || " + sqlConcatTableColumn(BPARTNER_LOCATION_TABLE_NAME, BPARTNER_LOCATION_COL_CITY_NAME) + ", '') " +
                        " || COALESCE( '; ' || " + sqlConcatTableColumn(BPARTNER_LOCATION_TABLE_NAME, BPARTNER_LOCATION_COL_REGION_NAME) + ", '') " +
                        " || COALESCE( '; ' || " + sqlConcatTableColumn(BPARTNER_LOCATION_TABLE_NAME, BPARTNER_LOCATION_COL_COUNTRY_NAME) + ", '') " +
                        " || COALESCE( '; ' || " + sqlConcatTableColumn(BPARTNER_LOCATION_TABLE_NAME, BPARTNER_LOCATION_COL_POSTAL_NAME) + ", '') " +
                            " AS Location, " +
                " COALESCE( " + sqlConcatTableColumn(TAG_TABLE_NAME, GEN_COL_NAME_NAME) + " || '; ', '') AS Tag " + 
            " FROM " + REFUEL_TABLE_NAME +
                    " JOIN " + EXPENSETYPE_TABLE_NAME +
                        " ON " + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_EXPENSETYPE_ID_NAME) + "=" +
                                            sqlConcatTableColumn(EXPENSETYPE_TABLE_NAME, GEN_COL_ROWID_NAME) +
                    " JOIN " + EXPENSECATEGORY_TABLE_NAME +
                        " ON " + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_EXPENSECATEGORY_ID_NAME) + "=" +
                                            sqlConcatTableColumn(EXPENSECATEGORY_TABLE_NAME, GEN_COL_ROWID_NAME) +
                    " JOIN " + DRIVER_TABLE_NAME +
                        " ON " + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_DRIVER_ID_NAME) + "=" +
                                            sqlConcatTableColumn(DRIVER_TABLE_NAME, GEN_COL_ROWID_NAME) +
                    " JOIN " + UOM_TABLE_NAME +
                        " ON " + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_UOMVOLUME_ID_NAME) + "=" +
                                            sqlConcatTableColumn(UOM_TABLE_NAME, GEN_COL_ROWID_NAME) +
                    " JOIN " + UOM_TABLE_NAME + " AS UomVolEntered " +
                        " ON " + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_UOMVOLUMEENTERED_ID_NAME) + "=" +
                                            sqlConcatTableColumn("UomVolEntered", GEN_COL_ROWID_NAME) +
                    " JOIN " + CAR_TABLE_NAME +
                        " ON " + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_CAR_ID_NAME) + "=" +
                                            sqlConcatTableColumn(CAR_TABLE_NAME, GEN_COL_ROWID_NAME) +
                    " JOIN " + CURRENCY_TABLE_NAME +
                        " ON " + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_CURRENCY_ID_NAME) + "=" +
                                            sqlConcatTableColumn(CURRENCY_TABLE_NAME, GEN_COL_ROWID_NAME) +
                    " JOIN " + CURRENCY_TABLE_NAME + " AS CurrencyEntered " +
                        " ON " + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_CURRENCYENTERED_ID_NAME) + "=" +
                                            sqlConcatTableColumn("CurrencyEntered", GEN_COL_ROWID_NAME) +
                    " LEFT OUTER JOIN " + BPARTNER_TABLE_NAME +
                        " ON " + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_BPARTNER_ID_NAME) + "=" +
                                            sqlConcatTableColumn(BPARTNER_TABLE_NAME, GEN_COL_ROWID_NAME) +
                    " LEFT OUTER JOIN " + BPARTNER_LOCATION_TABLE_NAME +
                        " ON " + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_BPARTNER_LOCATION_ID_NAME) + "=" +
                                            sqlConcatTableColumn(BPARTNER_LOCATION_TABLE_NAME, GEN_COL_ROWID_NAME) +
                    " LEFT OUTER JOIN " + TAG_TABLE_NAME +
                    	" ON " + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_TAG_ID_NAME) + "=" +
                                    sqlConcatTableColumn(TAG_TABLE_NAME, GEN_COL_ROWID_NAME) +
            " WHERE 1=1 ";

    //used in main activity & list view
    public static String expensesListViewSelect =
            "SELECT " +
                sqlConcatTableColumn(EXPENSE_TABLE_NAME, GEN_COL_ROWID_NAME) + ", " + //#0
                sqlConcatTableColumn(CAR_TABLE_NAME, GEN_COL_NAME_NAME) + " || '; ' || " +
                	sqlConcatTableColumn(DRIVER_TABLE_NAME, GEN_COL_NAME_NAME) + " || '; ' || " +
                	sqlConcatTableColumn(EXPENSETYPE_TABLE_NAME, GEN_COL_NAME_NAME) + " || '; [#1]'" +
                        	" AS " + FIRST_LINE_LIST_NAME + ", " + //#1
                sqlConcatTableColumn(EXPENSECATEGORY_TABLE_NAME, GEN_COL_NAME_NAME) + " || '; [#1] ' || " +
                	sqlConcatTableColumn(CURRENCY_TABLE_NAME, CURRENCY_COL_CODE_NAME) + " || " +
                    " CASE WHEN " + sqlConcatTableColumn(EXPENSE_TABLE_NAME, EXPENSE_COL_CURRENCY_ID_NAME) + " <> " +
                    	sqlConcatTableColumn(EXPENSE_TABLE_NAME, EXPENSE_COL_CURRENCYENTERED_ID_NAME) + " " +
                    	" THEN " + "' ([#2] ' || " +
                                sqlConcatTableColumn("DefaultCurrency", CURRENCY_COL_CODE_NAME) + " || ')' " +
                        " ELSE " + "'' " +
                    " END " 
                		+ " || " + 
                    " CASE WHEN COALESCE(" + sqlConcatTableColumn(EXPENSE_TABLE_NAME, EXPENSE_COL_INDEX_NAME) + ", '') <> '' " +
                		" THEN " + "' at [#3] ' || " + sqlConcatTableColumn(UOM_TABLE_NAME, UOM_COL_CODE_NAME) +
                		" ELSE " + "''" +
                	" END " +
                        " AS " + SECOND_LINE_LIST_NAME + ", " + //#2
                    " COALESCE( " + sqlConcatTableColumn(TAG_TABLE_NAME, GEN_COL_NAME_NAME) + " || '; ', '') || " + 
                	sqlConcatTableColumn(EXPENSE_TABLE_NAME, GEN_COL_USER_COMMENT_NAME) + " " +
                        "AS " + THIRD_LINE_LIST_NAME +  ", " + //#3
                sqlConcatTableColumn(EXPENSE_TABLE_NAME, EXPENSE_COL_DATE_NAME) + " AS Second, " + //#4
                sqlConcatTableColumn(EXPENSE_TABLE_NAME, EXPENSE_COL_AMOUNTENTERED_NAME) + " AS AmountEntered, " + //#5
                sqlConcatTableColumn(EXPENSE_TABLE_NAME, EXPENSE_COL_AMOUNT_NAME) + " AS Amount, " + //#6
                sqlConcatTableColumn(EXPENSE_TABLE_NAME, EXPENSE_COL_INDEX_NAME) + " AS CarIndex " + //#7
            " FROM " + EXPENSE_TABLE_NAME +
                    " JOIN " + EXPENSETYPE_TABLE_NAME +
                        " ON " + sqlConcatTableColumn(EXPENSE_TABLE_NAME, EXPENSE_COL_EXPENSETYPE_ID_NAME) + "=" +
                                            sqlConcatTableColumn(EXPENSETYPE_TABLE_NAME, GEN_COL_ROWID_NAME) +
                    " JOIN " + EXPENSECATEGORY_TABLE_NAME +
                        " ON " + sqlConcatTableColumn(EXPENSE_TABLE_NAME, EXPENSE_COL_EXPENSECATEGORY_ID_NAME) + "=" +
                                            sqlConcatTableColumn(EXPENSECATEGORY_TABLE_NAME, GEN_COL_ROWID_NAME) +
                    " JOIN " + DRIVER_TABLE_NAME +
                        " ON " + sqlConcatTableColumn(EXPENSE_TABLE_NAME, EXPENSE_COL_DRIVER_ID_NAME) + "=" +
                                            sqlConcatTableColumn(DRIVER_TABLE_NAME, GEN_COL_ROWID_NAME) +
                    " JOIN " + CAR_TABLE_NAME +
                        " ON " + sqlConcatTableColumn(EXPENSE_TABLE_NAME, EXPENSE_COL_CAR_ID_NAME) + "=" +
                                            sqlConcatTableColumn(CAR_TABLE_NAME, GEN_COL_ROWID_NAME) +
                        " JOIN " + UOM_TABLE_NAME +
                            " ON " + sqlConcatTableColumn(CAR_TABLE_NAME, CAR_COL_UOMLENGTH_ID_NAME) + "=" +
                                                sqlConcatTableColumn(UOM_TABLE_NAME, GEN_COL_ROWID_NAME) +
                    " JOIN " + CURRENCY_TABLE_NAME +
                        " ON " + sqlConcatTableColumn(EXPENSE_TABLE_NAME, EXPENSE_COL_CURRENCYENTERED_ID_NAME) + "=" +
                                            sqlConcatTableColumn(CURRENCY_TABLE_NAME, GEN_COL_ROWID_NAME) +
                    " JOIN " + CURRENCY_TABLE_NAME + " AS DefaultCurrency " +
                    	" ON " + sqlConcatTableColumn(EXPENSE_TABLE_NAME, EXPENSE_COL_CURRENCY_ID_NAME) + "=" +
                                        sqlConcatTableColumn("DefaultCurrency", GEN_COL_ROWID_NAME) +
                    " LEFT OUTER JOIN " + TAG_TABLE_NAME +
                    	" ON " + sqlConcatTableColumn(EXPENSE_TABLE_NAME, EXPENSE_COL_TAG_ID_NAME) + "=" +
                                	sqlConcatTableColumn(TAG_TABLE_NAME, GEN_COL_ROWID_NAME) +

//                	" WHERE COALESCE(" + EXPENSE_COL_FROMTABLE_NAME + ", '') = '' ";
    				" WHERE 1=1 ";


    //used in exported report
    public static String expensesListReportSelect =
            "SELECT " +
                sqlConcatTableColumn(EXPENSE_TABLE_NAME, GEN_COL_ROWID_NAME) + " AS ExpenseId, " +
                sqlConcatTableColumn(CAR_TABLE_NAME, GEN_COL_NAME_NAME) + " AS CarName, " +
                "DATETIME(" + sqlConcatTableColumn(EXPENSE_TABLE_NAME, EXPENSE_COL_DATE_NAME) + ", 'unixepoch', 'localtime') AS Date, " +

                "CASE strftime(\"%w\", " + sqlConcatTableColumn(EXPENSE_TABLE_NAME, EXPENSE_COL_DATE_NAME) +
                    ", 'unixepoch', 'localtime') " +
                    "WHEN \"0\" THEN '[#d0]' " +
                    "WHEN \"1\" THEN '[#d1]' " +
                    "WHEN \"2\" THEN '[#d2]' " +
                    "WHEN \"3\" THEN '[#d3]' " +
                    "WHEN \"4\" THEN '[#d4]' " +
                    "WHEN \"5\" THEN '[#d5]' " +
                    "WHEN \"6\" THEN '[#d6]' " +
                "END AS " + StaticValues.DAY_OF_WEEK_NAME + ", " +

                sqlConcatTableColumn(EXPENSE_TABLE_NAME, EXPENSE_COL_DOCUMENTNO_NAME) + ", " +
                sqlConcatTableColumn(EXPENSECATEGORY_TABLE_NAME, GEN_COL_NAME_NAME) + " AS ExpenseCategoryName, " +
                sqlConcatTableColumn(EXPENSETYPE_TABLE_NAME, GEN_COL_NAME_NAME) + " AS ExpenseTypeName, " +
                sqlConcatTableColumn(EXPENSE_TABLE_NAME, EXPENSE_COL_AMOUNT_NAME) + " AS " + EXPENSE_COL_AMOUNT_NAME + "_DTypeN, " +
                sqlConcatTableColumn(CURRENCY_TABLE_NAME, CURRENCY_COL_CODE_NAME) + " AS CurrencyCode, " +
                sqlConcatTableColumn(DRIVER_TABLE_NAME, GEN_COL_NAME_NAME) + " AS DriverName, " +
                sqlConcatTableColumn(EXPENSE_TABLE_NAME, GEN_COL_USER_COMMENT_NAME) + ", " +
                sqlConcatTableColumn(EXPENSE_TABLE_NAME, EXPENSE_COL_FROMTABLE_NAME) + " AS BaseExpense, " +
                sqlConcatTableColumn(EXPENSE_TABLE_NAME, EXPENSE_COL_FROMRECORD_ID_NAME) + " AS BaseExpenseId, " +
                sqlConcatTableColumn(EXPENSE_TABLE_NAME, EXPENSE_COL_INDEX_NAME) + " AS " + EXPENSE_COL_INDEX_NAME + "_DTypeN, " +
                sqlConcatTableColumn(EXPENSE_TABLE_NAME, EXPENSE_COL_AMOUNTENTERED_NAME) + " AS " + EXPENSE_COL_AMOUNTENTERED_NAME + "_DTypeN, " +
                sqlConcatTableColumn("CurrEntered", CURRENCY_COL_CODE_NAME) + " AS CurrencyEnteredCode, " +
                sqlConcatTableColumn(EXPENSE_TABLE_NAME, EXPENSE_COL_CURRENCYRATE_NAME) + " AS " + EXPENSE_COL_CURRENCYRATE_NAME + "_DTypeN, " +
                sqlConcatTableColumn(BPARTNER_TABLE_NAME, GEN_COL_NAME_NAME) + " AS Vendor, " +
                sqlConcatTableColumn(BPARTNER_LOCATION_TABLE_NAME, GEN_COL_NAME_NAME) +
                        " || COALESCE( '; ' || " + sqlConcatTableColumn(BPARTNER_LOCATION_TABLE_NAME, BPARTNER_LOCATION_COL_ADDRESS_NAME) + ", '') " +
                        " || COALESCE( '; ' || " + sqlConcatTableColumn(BPARTNER_LOCATION_TABLE_NAME, BPARTNER_LOCATION_COL_CITY_NAME) + ", '') " +
                        " || COALESCE( '; ' || " + sqlConcatTableColumn(BPARTNER_LOCATION_TABLE_NAME, BPARTNER_LOCATION_COL_REGION_NAME) + ", '') " +
                        " || COALESCE( '; ' || " + sqlConcatTableColumn(BPARTNER_LOCATION_TABLE_NAME, BPARTNER_LOCATION_COL_COUNTRY_NAME) + ", '') " +
                        " || COALESCE( '; ' || " + sqlConcatTableColumn(BPARTNER_LOCATION_TABLE_NAME, BPARTNER_LOCATION_COL_POSTAL_NAME) + ", '') " +
                            " AS Location, " +
                " COALESCE( " + sqlConcatTableColumn(TAG_TABLE_NAME, GEN_COL_NAME_NAME) + " || '; ', '') AS Tag " + 
            " FROM " + EXPENSE_TABLE_NAME +
                    " JOIN " + EXPENSETYPE_TABLE_NAME +
                        " ON " + sqlConcatTableColumn(EXPENSE_TABLE_NAME, EXPENSE_COL_EXPENSETYPE_ID_NAME) + "=" +
                                            sqlConcatTableColumn(EXPENSETYPE_TABLE_NAME, GEN_COL_ROWID_NAME) +
                    " JOIN " + EXPENSECATEGORY_TABLE_NAME +
                        " ON " + sqlConcatTableColumn(EXPENSE_TABLE_NAME, EXPENSE_COL_EXPENSECATEGORY_ID_NAME) + "=" +
                                            sqlConcatTableColumn(EXPENSECATEGORY_TABLE_NAME, GEN_COL_ROWID_NAME) +
                    " JOIN " + DRIVER_TABLE_NAME +
                        " ON " + sqlConcatTableColumn(EXPENSE_TABLE_NAME, EXPENSE_COL_DRIVER_ID_NAME) + "=" +
                                            sqlConcatTableColumn(DRIVER_TABLE_NAME, GEN_COL_ROWID_NAME) +
                    " JOIN " + CAR_TABLE_NAME +
                        " ON " + sqlConcatTableColumn(EXPENSE_TABLE_NAME, EXPENSE_COL_CAR_ID_NAME) + "=" +
                                            sqlConcatTableColumn(CAR_TABLE_NAME, GEN_COL_ROWID_NAME) +
                    " JOIN " + CURRENCY_TABLE_NAME +
                        " ON " + sqlConcatTableColumn(EXPENSE_TABLE_NAME, EXPENSE_COL_CURRENCY_ID_NAME) + "=" +
                                            sqlConcatTableColumn(CURRENCY_TABLE_NAME, GEN_COL_ROWID_NAME) +
                    " JOIN " + CURRENCY_TABLE_NAME + " AS CurrEntered " +
                        " ON " + sqlConcatTableColumn(EXPENSE_TABLE_NAME, EXPENSE_COL_CURRENCYENTERED_ID_NAME) + "=" +
                                            sqlConcatTableColumn("CurrEntered", GEN_COL_ROWID_NAME) +
                    " LEFT OUTER JOIN " + BPARTNER_TABLE_NAME +
                        " ON " + sqlConcatTableColumn(EXPENSE_TABLE_NAME, EXPENSE_COL_BPARTNER_ID_NAME) + "=" +
                                            sqlConcatTableColumn(BPARTNER_TABLE_NAME, GEN_COL_ROWID_NAME) +
                    " LEFT OUTER JOIN " + BPARTNER_LOCATION_TABLE_NAME +
                        " ON " + sqlConcatTableColumn(EXPENSE_TABLE_NAME, EXPENSE_COL_BPARTNER_LOCATION_ID_NAME) + "=" +
                        					sqlConcatTableColumn(BPARTNER_LOCATION_TABLE_NAME, GEN_COL_ROWID_NAME) +
                    " LEFT OUTER JOIN " + TAG_TABLE_NAME +
                		" ON " + sqlConcatTableColumn(EXPENSE_TABLE_NAME, EXPENSE_COL_TAG_ID_NAME) + "=" +
                            	sqlConcatTableColumn(TAG_TABLE_NAME, GEN_COL_ROWID_NAME) +
            " WHERE 1=1 ";

    public static String statisticsMainViewSelect =
        "SELECT " +
		        sqlConcatTableColumn(CAR_TABLE_NAME, GEN_COL_ROWID_NAME) + ", " + //#0
                sqlConcatTableColumn(CAR_TABLE_NAME, CAR_COL_INDEXSTART_NAME) + ", " + //#1
		        sqlConcatTableColumn(CAR_TABLE_NAME, CAR_COL_INDEXCURRENT_NAME) + ", " + //#2
	            sqlConcatTableColumn("UomLength", UOM_COL_CODE_NAME) + " AS UOMLength, " + //#3
	            sqlConcatTableColumn("TotalExpenses", "Expense") + " AS TotalExpense, " + //#4
	            sqlConcatTableColumn("TotalMileageExpenses", "Expense") + " AS TotalMileageExpense, " + //#5
                sqlConcatTableColumn(CURRENCY_TABLE_NAME, CURRENCY_COL_CODE_NAME) + ", " + //#6
	            sqlConcatTableColumn("UomVolume", UOM_COL_CODE_NAME) + " AS UOMVolume " + //#7
        " FROM " + CAR_TABLE_NAME + 
	        " JOIN " + UOM_TABLE_NAME + " AS UomLength " +
	            " ON " + sqlConcatTableColumn(CAR_TABLE_NAME, CAR_COL_UOMLENGTH_ID_NAME) + "=" +
	                                sqlConcatTableColumn("UomLength", GEN_COL_ROWID_NAME) +
	        " JOIN " + UOM_TABLE_NAME + " AS UomVolume " +
	            " ON " + sqlConcatTableColumn(CAR_TABLE_NAME, CAR_COL_UOMVOLUME_ID_NAME) + "=" +
	                                sqlConcatTableColumn("UomVolume", GEN_COL_ROWID_NAME) +
	        " JOIN " + CURRENCY_TABLE_NAME +
	            " ON " + sqlConcatTableColumn(CAR_TABLE_NAME, CAR_COL_CURRENCY_ID_NAME) + "=" +
	                                sqlConcatTableColumn(CURRENCY_TABLE_NAME, GEN_COL_ROWID_NAME) +
            //total expenses
            " LEFT OUTER JOIN ( " +
                    " SELECT " +
                        " SUM( " + sqlConcatTableColumn(EXPENSE_TABLE_NAME, EXPENSE_COL_AMOUNT_NAME) + ") AS Expense, " +
                        EXPENSE_COL_CAR_ID_NAME + " " +
                    " FROM " + EXPENSE_TABLE_NAME + " " +
                    " WHERE " + sqlConcatTableColumn(EXPENSE_TABLE_NAME, GEN_COL_ISACTIVE_NAME) + " = 'Y' " +
                    " GROUP BY " + sqlConcatTableColumn(EXPENSE_TABLE_NAME, EXPENSE_COL_CAR_ID_NAME) + " ) AS TotalExpenses " +
		                    " ON " + sqlConcatTableColumn(CAR_TABLE_NAME, GEN_COL_ROWID_NAME) + "=" +
		                                    sqlConcatTableColumn("TotalExpenses", EXPENSE_COL_CAR_ID_NAME) +
            //total expenses for mileage cost (exclude exp. category which have "Is exclude from mileage cost" attribute
            " LEFT OUTER JOIN ( " +
                                    " SELECT " +
                                        " SUM( " + sqlConcatTableColumn(EXPENSE_TABLE_NAME, EXPENSE_COL_AMOUNT_NAME) + ") AS Expense, " +
                                        EXPENSE_COL_CAR_ID_NAME + " " +
                                    " FROM " + EXPENSE_TABLE_NAME + " " +
                                                " JOIN " + EXPENSECATEGORY_TABLE_NAME + " ON " +
                                                    sqlConcatTableColumn(EXPENSE_TABLE_NAME, EXPENSE_COL_EXPENSECATEGORY_ID_NAME) + " = " +
                                                        sqlConcatTableColumn(EXPENSECATEGORY_TABLE_NAME, GEN_COL_ROWID_NAME) +
                                    " WHERE " + sqlConcatTableColumn(EXPENSE_TABLE_NAME, GEN_COL_ISACTIVE_NAME) + " = 'Y' " +
                                        " AND " + sqlConcatTableColumn(EXPENSECATEGORY_TABLE_NAME, EXPENSECATEGORY_COL_ISEXCLUDEFROMMILEAGECOST_NAME) + " = 'N' " +
                                    " GROUP BY " + sqlConcatTableColumn(EXPENSE_TABLE_NAME, EXPENSE_COL_CAR_ID_NAME) + " ) AS TotalMileageExpenses " +
		                                    " ON " + sqlConcatTableColumn(CAR_TABLE_NAME, GEN_COL_ROWID_NAME) + "=" +
		                                                    sqlConcatTableColumn("TotalMileageExpenses", EXPENSE_COL_CAR_ID_NAME) +
        " WHERE 1=1 ";
        
    //used in main activity and GPS Track list activity
    public static String gpsTrackListViewSelect =
            "SELECT " +
                sqlConcatTableColumn(GPSTRACK_TABLE_NAME, GEN_COL_ROWID_NAME) + ", " + //#0
                sqlConcatTableColumn(CAR_TABLE_NAME,  GEN_COL_NAME_NAME) + " || '; ' || " +
                        sqlConcatTableColumn(DRIVER_TABLE_NAME, GEN_COL_NAME_NAME) + " || '; ' || '[#1]'" +
                        " AS " + FIRST_LINE_LIST_NAME + ", " +  //#1
                "'[#1]' || ROUND(" + sqlConcatTableColumn(GPSTRACK_TABLE_NAME, GPSTRACK_COL_DISTANCE_NAME) + ", " + StaticValues.DECIMALS_LENGTH + ") || ' ' || " +
                                sqlConcatTableColumn(UOM_TABLE_NAME, UOM_COL_CODE_NAME) + " || '; ' || " +
                    "'[#2]' || ROUND(" + sqlConcatTableColumn(GPSTRACK_TABLE_NAME, GPSTRACK_COL_MAXSPEED_NAME) + ", 2) || ' ' || " +
                                sqlConcatTableColumn(UOM_TABLE_NAME, UOM_COL_CODE_NAME) + " || '/h; ' || " +
                    "'[#3]' || ROUND(" + sqlConcatTableColumn(GPSTRACK_TABLE_NAME, GPSTRACK_COL_AVGSPEED_NAME) + ", 2) || ' ' || " +
                                sqlConcatTableColumn(UOM_TABLE_NAME, UOM_COL_CODE_NAME) + " || '/h; ' || " +
                    "'[#4]' || ROUND(" + sqlConcatTableColumn(GPSTRACK_TABLE_NAME, GPSTRACK_COL_AVGMOVINGSPEED_NAME) + ", 2) || ' ' || " +
                                sqlConcatTableColumn(UOM_TABLE_NAME, UOM_COL_CODE_NAME) + " || '/h; ' || " +
                    "'[#5]' || '; ' || " +
                    "'[#6]' || '; ' || " +
                    "'[#12]' || '; ' || " +
                    "'[#7]' || ROUND(" + sqlConcatTableColumn(GPSTRACK_TABLE_NAME, GPSTRACK_COL_MINACCURACY_NAME) + ", 2) || " +
                        " CASE WHEN UPPER(" + sqlConcatTableColumn(UOM_TABLE_NAME, UOM_COL_CODE_NAME) + ") == 'KM' " +
                                " THEN " + "' m; ' " +
                                " ELSE " + "' yd; ' " +
                        " END || " +
                    "'[#8]' || ROUND(" + sqlConcatTableColumn(GPSTRACK_TABLE_NAME, GPSTRACK_COL_MAXACCURACY_NAME) + ", 2) || " +
                        " CASE WHEN UPPER(" + sqlConcatTableColumn(UOM_TABLE_NAME, UOM_COL_CODE_NAME) + ") == 'KM' " +
                                " THEN " + "' m; ' " +
                                " ELSE " + "' yd; ' " +
                        " END || " +
                    "'[#9]' || ROUND(" + sqlConcatTableColumn(GPSTRACK_TABLE_NAME, GPSTRACK_COL_AVGACCURACY_NAME) + ", 2) || " +
                        " CASE WHEN UPPER(" + sqlConcatTableColumn(UOM_TABLE_NAME, UOM_COL_CODE_NAME) + ") == 'KM' " +
                                " THEN " + "' m; ' " +
                                " ELSE " + "' yd; ' " +
                        " END || " +
                    "'[#10]' || " + sqlConcatTableColumn(GPSTRACK_TABLE_NAME, GPSTRACK_COL_MINALTITUDE_NAME) + " || " +
                        " CASE WHEN UPPER(" + sqlConcatTableColumn(UOM_TABLE_NAME, UOM_COL_CODE_NAME) + ") == 'KM' " +
                                " THEN " + "' m; ' " +
                                " ELSE " + "' yd; ' " +
                        " END || " +
                    "'[#11]' || " + sqlConcatTableColumn(GPSTRACK_TABLE_NAME, GPSTRACK_COL_MAXALTITUDE_NAME) + " || " +
                        " CASE WHEN UPPER(" + sqlConcatTableColumn(UOM_TABLE_NAME, UOM_COL_CODE_NAME) + ") == 'KM' " +
                                " THEN " + "' m; ' " +
                                " ELSE " + "' yd; ' " +
                        " END " +

                    " AS " + SECOND_LINE_LIST_NAME + ", " +  //#2
                " COALESCE( " + sqlConcatTableColumn(TAG_TABLE_NAME, GEN_COL_NAME_NAME) + " || '; ', '') || " + 
			                sqlConcatTableColumn(GPSTRACK_TABLE_NAME, GEN_COL_NAME_NAME) + " || '; ' || " +
			                    sqlConcatTableColumn(GPSTRACK_TABLE_NAME, GEN_COL_USER_COMMENT_NAME) +
                        " AS " + THIRD_LINE_LIST_NAME + ", " + //#3
                "ROUND(" + sqlConcatTableColumn(GPSTRACK_TABLE_NAME, GPSTRACK_COL_TOTALTIME_NAME) + ", 2) AS " + FOURTH_LINE_LIST_NAME + ", " + //#4
                "ROUND(" + sqlConcatTableColumn(GPSTRACK_TABLE_NAME, GPSTRACK_COL_MOVINGTIME_NAME)  + ", 2) AS " + FIFTH_LINE_LIST_NAME + ", " + //#5
                sqlConcatTableColumn(GPSTRACK_TABLE_NAME, GEN_COL_NAME_NAME) + ", " + //#6
                sqlConcatTableColumn(GPSTRACK_TABLE_NAME, GPSTRACK_COL_DATE_NAME) + " AS Seconds, " + //#7
                "ROUND(" + sqlConcatTableColumn(GPSTRACK_TABLE_NAME, GPSTRACK_COL_TOTALPAUSETIME_NAME)  + ", 2) AS " + GPSTRACK_COL_TOTALPAUSETIME_NAME + //#8
            " FROM " + GPSTRACK_TABLE_NAME +
                    " JOIN " + DRIVER_TABLE_NAME +
                        " ON " + sqlConcatTableColumn(GPSTRACK_TABLE_NAME, GPSTRACK_COL_DRIVER_ID_NAME) + "=" +
                                            sqlConcatTableColumn(DRIVER_TABLE_NAME, GEN_COL_ROWID_NAME) +
                    " JOIN " + CAR_TABLE_NAME +
                        " ON " + sqlConcatTableColumn(GPSTRACK_TABLE_NAME, GPSTRACK_COL_CAR_ID_NAME) + "=" +
                                            sqlConcatTableColumn(CAR_TABLE_NAME, GEN_COL_ROWID_NAME) +
                        " JOIN " + UOM_TABLE_NAME +
                            " ON " + sqlConcatTableColumn(CAR_TABLE_NAME, CAR_COL_UOMLENGTH_ID_NAME) + "=" +
                                                sqlConcatTableColumn(UOM_TABLE_NAME, GEN_COL_ROWID_NAME) +
                    " LEFT OUTER JOIN " + TAG_TABLE_NAME +
            			" ON " + sqlConcatTableColumn(GPSTRACK_TABLE_NAME, GPSTRACK_COL_TAG_ID_NAME) + "=" +
                        		sqlConcatTableColumn(TAG_TABLE_NAME, GEN_COL_ROWID_NAME) +
            //exclude the track in progress (the no. of trackpoints is updated after terminating the tracking)
            " WHERE " + sqlConcatTableColumn(GPSTRACK_TABLE_NAME, GPSTRACK_COL_TOTALTRACKPOINTS_NAME) + " IS NOT NULL ";

    //used in exported report
    public static String gpsTrackListReportSelect =
            "SELECT " +
                sqlConcatTableColumn(GPSTRACK_TABLE_NAME, GEN_COL_ROWID_NAME) + " AS TrackId, " +
                sqlConcatTableColumn(CAR_TABLE_NAME, GEN_COL_NAME_NAME) + " AS CarName, " +
                sqlConcatTableColumn(DRIVER_TABLE_NAME, GEN_COL_NAME_NAME) + " AS DriverName, " +
                "DATETIME(" + sqlConcatTableColumn(GPSTRACK_TABLE_NAME, GPSTRACK_COL_DATE_NAME) + ", 'unixepoch', 'localtime') AS Date, " +

                "CASE strftime(\"%w\", " + sqlConcatTableColumn(GPSTRACK_TABLE_NAME, GPSTRACK_COL_DATE_NAME) +
                    ", 'unixepoch', 'localtime') " +
                    "WHEN \"0\" THEN '[#d0]' " +
                    "WHEN \"1\" THEN '[#d1]' " +
                    "WHEN \"2\" THEN '[#d2]' " +
                    "WHEN \"3\" THEN '[#d3]' " +
                    "WHEN \"4\" THEN '[#d4]' " +
                    "WHEN \"5\" THEN '[#d5]' " +
                    "WHEN \"6\" THEN '[#d6]' " +
                "END AS " + StaticValues.DAY_OF_WEEK_NAME + ", " +

                sqlConcatTableColumn(GPSTRACK_TABLE_NAME, GPSTRACK_COL_MAXACCURACY_NAME) + " AS " + GPSTRACK_COL_MINACCURACY_NAME + ", " +
                sqlConcatTableColumn(GPSTRACK_TABLE_NAME, GPSTRACK_COL_MINACCURACY_NAME) + " AS " + GPSTRACK_COL_MAXACCURACY_NAME + ", " +
                sqlConcatTableColumn(GPSTRACK_TABLE_NAME, GPSTRACK_COL_AVGACCURACY_NAME) + ", " +
                sqlConcatTableColumn(GPSTRACK_TABLE_NAME, GPSTRACK_COL_DISTANCE_NAME) + ", " +
                sqlConcatTableColumn(GPSTRACK_TABLE_NAME, GPSTRACK_COL_MAXSPEED_NAME) + ", " +
                sqlConcatTableColumn(GPSTRACK_TABLE_NAME, GPSTRACK_COL_AVGMOVINGSPEED_NAME) + ", " +
                sqlConcatTableColumn(GPSTRACK_TABLE_NAME, GPSTRACK_COL_AVGSPEED_NAME) + ", " +
                sqlConcatTableColumn(GPSTRACK_TABLE_NAME, GPSTRACK_COL_MAXALTITUDE_NAME) + ", " +
                sqlConcatTableColumn(GPSTRACK_TABLE_NAME, GPSTRACK_COL_MINALTITUDE_NAME) + ", " +
                sqlConcatTableColumn(GPSTRACK_TABLE_NAME, GPSTRACK_COL_TOTALTIME_NAME) + " AS '" + GPSTRACK_COL_TOTALTIME_NAME + " [s]', " +
                sqlConcatTableColumn(GPSTRACK_TABLE_NAME, GPSTRACK_COL_MOVINGTIME_NAME) + " AS '" + GPSTRACK_COL_MOVINGTIME_NAME + " [s]', " +
                sqlConcatTableColumn(GPSTRACK_TABLE_NAME, GPSTRACK_COL_TOTALPAUSETIME_NAME) + " AS '" + GPSTRACK_COL_TOTALPAUSETIME_NAME + " [s]', " +
                sqlConcatTableColumn(GPSTRACK_TABLE_NAME, GPSTRACK_COL_TOTALTRACKPOINTS_NAME) + ", " +
                sqlConcatTableColumn(GPSTRACK_TABLE_NAME, GPSTRACK_COL_INVALIDTRACKPOINTS_NAME) + ", " +
                sqlConcatTableColumn(GPSTRACK_TABLE_NAME, GPSTRACK_COL_MILEAGE_ID_NAME) + ", " +
                " COALESCE( " + sqlConcatTableColumn(TAG_TABLE_NAME, GEN_COL_NAME_NAME) + " || '; ', '') AS Tag " + 
            " FROM " + GPSTRACK_TABLE_NAME +
                    " JOIN " + DRIVER_TABLE_NAME +
                        " ON " + sqlConcatTableColumn(GPSTRACK_TABLE_NAME, GPSTRACK_COL_DRIVER_ID_NAME) + "=" +
                                            sqlConcatTableColumn(DRIVER_TABLE_NAME, GEN_COL_ROWID_NAME) +
                    " JOIN " + CAR_TABLE_NAME +
                        " ON " + sqlConcatTableColumn(GPSTRACK_TABLE_NAME, GPSTRACK_COL_CAR_ID_NAME) + "=" +
                                            sqlConcatTableColumn(CAR_TABLE_NAME, GEN_COL_ROWID_NAME) +
                    " LEFT OUTER JOIN " + TAG_TABLE_NAME +
        				" ON " + sqlConcatTableColumn(GPSTRACK_TABLE_NAME, GPSTRACK_COL_TAG_ID_NAME) + "=" +
                    						sqlConcatTableColumn(TAG_TABLE_NAME, GEN_COL_ROWID_NAME) +
            " WHERE 1=1 ";
    
    ////used in main activity and todo list activity
    public static String todoListViewSelect = 
    	"SELECT "
			+ sqlConcatTableColumn(TODO_TABLE_NAME, GEN_COL_ROWID_NAME) + ", "  // #0
			+ "'[#1] ' || " + sqlConcatTableColumn(TASKTYPE_TABLE_NAME, GEN_COL_NAME_NAME) + " || ', ' || " //type - GEN_TypeLabel 
				+ "'[#2] ' || " + sqlConcatTableColumn(TASK_TABLE_NAME, GEN_COL_NAME_NAME) + " || ', ' || " //task - GEN_TaskLabel
				+ " CASE "
					+ " WHEN "  + sqlConcatTableColumn(CAR_TABLE_NAME, GEN_COL_NAME_NAME) + " IS NOT NULL "
						+ " THEN "
								+ "'[#3] ' || COALESCE(" + sqlConcatTableColumn(CAR_TABLE_NAME, GEN_COL_NAME_NAME) + ", '') || ', ' " //car - GEN_CarLabel
					+ " ELSE '' "
				+ " END || "
				+ "'[#4] ' || " //task status - GEN_StatusLabel
				+ " CASE "
					+ " WHEN " + sqlConcatTableColumn(TODO_TABLE_NAME, TODO_COL_ISDONE_NAME) + " == 'Y' "
						+ " THEN '[#15]' " //done - ToDo_DoneLabel
					+ " WHEN " + sqlConcatTableColumn(TASK_TABLE_NAME, TASK_COL_SCHEDULEDFOR_NAME) + " == '" + StaticValues.TASK_SCHEDULED_FOR_TIME + "' " +
										" AND " + sqlConcatTableColumn(TODO_TABLE_NAME, TODO_COL_DUEDATE_NAME) + " < strftime('%s','now') "
								+ " THEN '[#5]' " //overdue - Todo_OverdueLabel
					+ " WHEN " + sqlConcatTableColumn(TASK_TABLE_NAME, TASK_COL_SCHEDULEDFOR_NAME) + " == '" + StaticValues.TASK_SCHEDULED_FOR_MILEAGE + "' " +
										" AND " + sqlConcatTableColumn(TODO_TABLE_NAME, TODO_COL_DUEMILEAGE_NAME) + " < " + sqlConcatTableColumn(CAR_TABLE_NAME, CAR_COL_INDEXCURRENT_NAME)
									+ " THEN '[#5]' " //overdue - Todo_OverdueLabel 
					+ " WHEN " + sqlConcatTableColumn(TASK_TABLE_NAME, TASK_COL_SCHEDULEDFOR_NAME) + " == '" + StaticValues.TASK_SCHEDULED_FOR_BOTH + "' " +
										" AND ( " 
											+ sqlConcatTableColumn(TODO_TABLE_NAME, TODO_COL_DUEMILEAGE_NAME) + " < " + sqlConcatTableColumn(CAR_TABLE_NAME, CAR_COL_INDEXCURRENT_NAME)
											+ " OR "
											+ sqlConcatTableColumn(TODO_TABLE_NAME, TODO_COL_DUEDATE_NAME) + " < strftime('%s','now')) "
									+ " THEN '[#5]' " //overdue - Todo_OverdueLabel
					+ " ELSE '[#6]' " //scheduled - Todo_ScheduledLabel
				+ " END AS "  + FIRST_LINE_LIST_NAME + ", " //datetime(task_todo.DueDate, 'unixepoch', 'localtime') // #1
				
    		+ " CASE "
    				+ " WHEN " + sqlConcatTableColumn(TASK_TABLE_NAME, TASK_COL_SCHEDULEDFOR_NAME) + " == '" + StaticValues.TASK_SCHEDULED_FOR_TIME + "' "
    					+ " THEN '[#7] [#8]' " //duedate label/ToDo_ScheduledDateLabel + date
    				+ " WHEN " + sqlConcatTableColumn(TASK_TABLE_NAME, TASK_COL_SCHEDULEDFOR_NAME) + " == '" + StaticValues.TASK_SCHEDULED_FOR_MILEAGE + "' "
    					+ " THEN '[#10] [#11] ' || "
    							+ " COALESCE (" + sqlConcatTableColumn(UOM_TABLE_NAME, UOM_COL_CODE_NAME) + ", '') || "
    							+ " ' ([#13] [#14])' " //duemileage label/ToDo_ScheduledMileageLabel + mileage + (estimated date)
    				+ " ELSE '[#7] [#8] [#9] [#11] ' || " // [#12]
								+ " COALESCE (" + sqlConcatTableColumn(UOM_TABLE_NAME, UOM_COL_CODE_NAME) + ", '') || "
    						+ "' ([#13] [#14])' "
    		+ " END " //duemileage label/ToDo_ScheduledMileageLabel + mileage 
    			+ " AS " + SECOND_LINE_LIST_NAME + ", " // #2
    		+ " COALESCE( " + sqlConcatTableColumn(TODO_TABLE_NAME, GEN_COL_USER_COMMENT_NAME) + ", '') AS " + THIRD_LINE_LIST_NAME + ", " // #3
			+ sqlConcatTableColumn(TODO_TABLE_NAME, TODO_COL_DUEDATE_NAME) + ", " // #4
    		+ sqlConcatTableColumn(TODO_TABLE_NAME, TODO_COL_DUEMILEAGE_NAME) + ", " // #5
    		+ sqlConcatTableColumn(TASK_TABLE_NAME, TASK_COL_SCHEDULEDFOR_NAME) + ", " // #6
    		+ " CASE "
    			+ " WHEN Minimums.Mileage IS NOT NULL "
		    		+ " THEN ( " + sqlConcatTableColumn(TODO_TABLE_NAME, TODO_COL_DUEMILEAGE_NAME) 
		    				+ " - " + sqlConcatTableColumn(CAR_TABLE_NAME, CAR_COL_INDEXCURRENT_NAME) + ") " //no of mileages until the todo
					+ " / "
					+ "("
							//avg. daily mileage
		    				+ "( " + sqlConcatTableColumn(CAR_TABLE_NAME, CAR_COL_INDEXCURRENT_NAME) + " - Minimums.Mileage ) " 
		    				+ " / "
		    				+ " (strftime('%J','now', 'localtime') - COALESCE(strftime('%J', datetime(Minimums.Date, 'unixepoch'), 'localtime'), 0) ) "
					+ ") " 
				+ " ELSE 99999999999 "
			+ " END " + " AS EstDaysUntilDueMileage, "  //Estimated days until the due mileage #7 
			+ "( " 
					+ " COALESCE(strftime('%J', datetime(" 
							+ sqlConcatTableColumn(TODO_TABLE_NAME, TODO_COL_DUEDATE_NAME) + ", 'unixepoch'), 'localtime'), 0) "
					+ " - "
					+ " strftime('%J','now', 'localtime') " 
			+ " ) AS DaysUntilDueDate, " // #8
			+ " CASE "
				+ " WHEN "
					+ sqlConcatTableColumn(TASK_TABLE_NAME, TASK_COL_SCHEDULEDFOR_NAME) + " = '" + StaticValues.TASK_SCHEDULED_FOR_BOTH + "' "
							+ " AND Minimums.Mileage IS NOT NULL AND Minimums.Date IS NOT NULL "
							+ " AND ( "
					    		+ "( ( " + sqlConcatTableColumn(TODO_TABLE_NAME, TODO_COL_DUEMILEAGE_NAME) 
								+ " - " + sqlConcatTableColumn(CAR_TABLE_NAME, CAR_COL_INDEXCURRENT_NAME) + ") " //no of mileages until the todo
								+ " / "
								+ "("
										+ "( " + sqlConcatTableColumn(CAR_TABLE_NAME, CAR_COL_INDEXCURRENT_NAME) + " - Minimums.Mileage ) " 
										+ " / "
										+ " (strftime('%J','now', 'localtime') - COALESCE(strftime('%J', datetime(Minimums.Date, 'unixepoch'), 'localtime'), 0) ) "
								+ ") ) " 
								+ " < " 
								+ "( " 
									+ " COALESCE(strftime('%J', datetime(" 
											+ sqlConcatTableColumn(TODO_TABLE_NAME, TODO_COL_DUEDATE_NAME) + ", 'unixepoch'), 'localtime'), 0) "
									+ " - "
									+ " strftime('%J','now', 'localtime') " 
								+ " ) "
							+ " ) "
						+ " THEN "
				    		+ "( ( " + sqlConcatTableColumn(TODO_TABLE_NAME, TODO_COL_DUEMILEAGE_NAME) 
							+ " - " + sqlConcatTableColumn(CAR_TABLE_NAME, CAR_COL_INDEXCURRENT_NAME) + ") " //no of mileages until the todo
							+ " / "
							+ "("
									+ "( " + sqlConcatTableColumn(CAR_TABLE_NAME, CAR_COL_INDEXCURRENT_NAME) + " - Minimums.Mileage ) " 
									+ " / "
									+ " (strftime('%J','now', 'localtime') - COALESCE(strftime('%J', datetime(Minimums.Date, 'unixepoch'), 'localtime'), 0) ) "
							+ ") ) " 
				+ " WHEN "
						+ sqlConcatTableColumn(TASK_TABLE_NAME, TASK_COL_SCHEDULEDFOR_NAME) + " = '" + StaticValues.TASK_SCHEDULED_FOR_BOTH + "' "
								+ " AND Minimums.Mileage IS NOT NULL AND Minimums.Date IS NOT NULL "
								+ " AND ( "
						    		+ "( ( " + sqlConcatTableColumn(TODO_TABLE_NAME, TODO_COL_DUEMILEAGE_NAME) 
									+ " - " + sqlConcatTableColumn(CAR_TABLE_NAME, CAR_COL_INDEXCURRENT_NAME) + ") " //no of mileages until the todo
									+ " / "
									+ "("
											+ "( " + sqlConcatTableColumn(CAR_TABLE_NAME, CAR_COL_INDEXCURRENT_NAME) + " - Minimums.Mileage ) " 
											+ " / "
											+ " (strftime('%J','now', 'localtime') - COALESCE(strftime('%J', datetime(Minimums.Date, 'unixepoch'), 'localtime'), 0) ) "
									+ ") ) " 
									+ " > " 
									+ "( " 
										+ " COALESCE(strftime('%J', datetime(" 
												+ sqlConcatTableColumn(TODO_TABLE_NAME, TODO_COL_DUEDATE_NAME) + ", 'unixepoch'), 'localtime'), 0) "
										+ " - "
										+ " strftime('%J','now', 'localtime') " 
									+ " ) "
								+ " ) "
							+ " THEN "
								+ "( " 
									+ " COALESCE(strftime('%J', datetime(" 
											+ sqlConcatTableColumn(TODO_TABLE_NAME, TODO_COL_DUEDATE_NAME) + ", 'unixepoch'), 'localtime'), 0) "
									+ " - "
									+ " strftime('%J','now', 'localtime') " 
								+ " ) "
				+ " WHEN "
					+ "(" + sqlConcatTableColumn(TASK_TABLE_NAME, TASK_COL_SCHEDULEDFOR_NAME) + " = '" + StaticValues.TASK_SCHEDULED_FOR_BOTH + "' "
								+ " OR " + sqlConcatTableColumn(TASK_TABLE_NAME, TASK_COL_SCHEDULEDFOR_NAME) + " = '" + StaticValues.TASK_SCHEDULED_FOR_MILEAGE + "') "
							+ " AND " 
								+ " Minimums.Mileage IS NULL "
						+ " THEN 99999999999 " 
				+ " WHEN "
						+ sqlConcatTableColumn(TASK_TABLE_NAME, TASK_COL_SCHEDULEDFOR_NAME) + " = '" + StaticValues.TASK_SCHEDULED_FOR_MILEAGE + "' "
								+ " AND Minimums.Mileage IS NOT NULL "
						+ " THEN "
				    		+ "( ( " + sqlConcatTableColumn(TODO_TABLE_NAME, TODO_COL_DUEMILEAGE_NAME) 
							+ " - " + sqlConcatTableColumn(CAR_TABLE_NAME, CAR_COL_INDEXCURRENT_NAME) + ") " //no of mileages until the todo
							+ " / "
							+ "("
									+ "( " + sqlConcatTableColumn(CAR_TABLE_NAME, CAR_COL_INDEXCURRENT_NAME) + " - Minimums.Mileage ) " 
									+ " / "
									+ " (strftime('%J','now', 'localtime') - COALESCE(strftime('%J', datetime(Minimums.Date, 'unixepoch'), 'localtime'), 0) ) "
							+ ") ) " 
				+ " WHEN "
						+ sqlConcatTableColumn(TASK_TABLE_NAME, TASK_COL_SCHEDULEDFOR_NAME) + " = '" + StaticValues.TASK_SCHEDULED_FOR_TIME + "' "
						+ " THEN "
							+ "( " 
							+ " COALESCE(strftime('%J', datetime(" 
									+ sqlConcatTableColumn(TODO_TABLE_NAME, TODO_COL_DUEDATE_NAME) + ", 'unixepoch'), 'localtime'), 0) "
							+ " - "
							+ " strftime('%J','now', 'localtime') " 
						+ " ) "
			+ " END AS EstDueDays, " //#9
			+ sqlConcatTableColumn(CAR_TABLE_NAME, GEN_COL_NAME_NAME) + " AS CarName, " //#10
			+ sqlConcatTableColumn(TASK_TABLE_NAME, GEN_COL_ROWID_NAME) + " AS TaskID, " //#11
			+ sqlConcatTableColumn(CAR_TABLE_NAME, CAR_COL_INDEXCURRENT_NAME) + " AS CarCurrentIndex " //#12
		+ " FROM " + TODO_TABLE_NAME
			+ " JOIN " + TASK_TABLE_NAME + " ON " + sqlConcatTableColumn(TODO_TABLE_NAME, TODO_COL_TASK_ID_NAME) + " = " +
															sqlConcatTableColumn(TASK_TABLE_NAME, GEN_COL_ROWID_NAME)
				+ " JOIN " + TASKTYPE_TABLE_NAME + " ON "+ sqlConcatTableColumn(TASK_TABLE_NAME, TASK_COL_TASKTYPE_ID_NAME) + " = " +
															sqlConcatTableColumn(TASKTYPE_TABLE_NAME, GEN_COL_ROWID_NAME)
			+ " LEFT OUTER JOIN " + CAR_TABLE_NAME + " ON " + sqlConcatTableColumn(TODO_TABLE_NAME, TODO_COL_CAR_ID_NAME) + " = " +
															sqlConcatTableColumn(CAR_TABLE_NAME, GEN_COL_ROWID_NAME)
				+ " LEFT OUTER JOIN " + UOM_TABLE_NAME + " ON " + sqlConcatTableColumn(CAR_TABLE_NAME, CAR_COL_UOMLENGTH_ID_NAME) + " = " +
															sqlConcatTableColumn(UOM_TABLE_NAME, GEN_COL_ROWID_NAME)
			+ " LEFT OUTER JOIN ( "
					+ " SELECT MIN(Date) AS Date, MIN(Mileage) AS Mileage, CAR_ID "  
					+ " FROM "  
						+ " (SELECT MIN(" + MILEAGE_COL_DATE_NAME + ") AS Date, "
									+ " MIN(" + MILEAGE_COL_INDEXSTART_NAME + ") AS Mileage, " 
									+ MILEAGE_COL_CAR_ID_NAME + " AS CAR_ID " 
						+ " FROM " + MILEAGE_TABLE_NAME
						+ " WHERE " + GEN_COL_ISACTIVE_NAME + " = 'Y' "
						+ " GROUP BY " + MILEAGE_COL_CAR_ID_NAME  
						+ " UNION "  
						+ " SELECT MIN(" + REFUEL_COL_DATE_NAME + ") AS Date, "
								+ " MIN(" + REFUEL_COL_INDEX_NAME + ") AS Mileage, " 
								+ REFUEL_COL_CAR_ID_NAME + " AS CAR_ID " 
						+ " FROM " + REFUEL_TABLE_NAME
						+ " WHERE " + GEN_COL_ISACTIVE_NAME + " = 'Y' "
						+ " GROUP BY " + REFUEL_COL_CAR_ID_NAME
						+ " UNION "
						+ " SELECT MIN(" + EXPENSE_COL_DATE_NAME + ") AS Date, " 
								+ " MIN(" + EXPENSE_COL_INDEX_NAME + ") AS Mileage, " 
								+ EXPENSE_COL_CAR_ID_NAME + " AS CAR_ID " 
						+ " FROM " + EXPENSE_TABLE_NAME
						+ " WHERE " + GEN_COL_ISACTIVE_NAME + " = 'Y' "
						+ " GROUP BY " + EXPENSE_COL_CAR_ID_NAME
						+ " ) "
					+ " GROUP BY CAR_ID ) AS Minimums ON Minimums.CAR_ID = " + sqlConcatTableColumn(TODO_TABLE_NAME, TODO_COL_CAR_ID_NAME)
		+ " WHERE 1=1 "
	;
    
    //used in exported reports
    public static String todoListReportSelect = 
    	"SELECT "
			+ sqlConcatTableColumn(TODO_TABLE_NAME, GEN_COL_ROWID_NAME) + " AS ToDoID, " //#0
			+ sqlConcatTableColumn(TASKTYPE_TABLE_NAME, GEN_COL_NAME_NAME) + " AS TaskType, "
			+ sqlConcatTableColumn(TASK_TABLE_NAME, GEN_COL_NAME_NAME) + " AS Task, "
			+ sqlConcatTableColumn(CAR_TABLE_NAME, GEN_COL_NAME_NAME) + " AS Car, "
			+ " CASE "
					+ " WHEN " + sqlConcatTableColumn(TODO_TABLE_NAME, TODO_COL_ISDONE_NAME) + " == 'Y' "
						+ " THEN '[#TDR1]' " //done - ToDo_DoneLabel
					+ " WHEN " + sqlConcatTableColumn(TASK_TABLE_NAME, TASK_COL_SCHEDULEDFOR_NAME) + " == '" + StaticValues.TASK_SCHEDULED_FOR_TIME + "' " +
										" AND " + sqlConcatTableColumn(TODO_TABLE_NAME, TODO_COL_DUEDATE_NAME) + " < strftime('%s','now') "
								+ " THEN '[#TDR2]' " //overdue - Todo_OverdueLabel
					+ " WHEN " + sqlConcatTableColumn(TASK_TABLE_NAME, TASK_COL_SCHEDULEDFOR_NAME) + " == '" + StaticValues.TASK_SCHEDULED_FOR_MILEAGE + "' " +
										" AND " + sqlConcatTableColumn(TODO_TABLE_NAME, TODO_COL_DUEMILEAGE_NAME) + " < " + sqlConcatTableColumn(CAR_TABLE_NAME, CAR_COL_INDEXCURRENT_NAME)
									+ " THEN '[#TDR2]' " //overdue - Todo_OverdueLabel 
					+ " WHEN " + sqlConcatTableColumn(TASK_TABLE_NAME, TASK_COL_SCHEDULEDFOR_NAME) + " == '" + StaticValues.TASK_SCHEDULED_FOR_BOTH + "' " +
										" AND ( " 
											+ sqlConcatTableColumn(TODO_TABLE_NAME, TODO_COL_DUEMILEAGE_NAME) + " < " + sqlConcatTableColumn(CAR_TABLE_NAME, CAR_COL_INDEXCURRENT_NAME)
											+ " OR "
											+ sqlConcatTableColumn(TODO_TABLE_NAME, TODO_COL_DUEDATE_NAME) + " < strftime('%s','now')) "
									+ " THEN '[#TDR2]' " //overdue - Todo_OverdueLabel
					+ " ELSE '[#TDR3]' " //scheduled - Todo_ScheduledLabel
				+ " END AS Status, "
				
    		+ " CASE " + sqlConcatTableColumn(TASK_TABLE_NAME, TASK_COL_SCHEDULEDFOR_NAME)
    				+ " WHEN '" + StaticValues.TASK_SCHEDULED_FOR_TIME + "' "
    					+ " THEN '[#TDR4]'" //time
    				+ " WHEN '" + StaticValues.TASK_SCHEDULED_FOR_MILEAGE + "' "
    					+ " THEN '[#TDR5]'" //mileage
    				+ " ELSE '[#TDR6]'"
    		+ " END AS ScheduledFor, "  
			+ sqlConcatTableColumn(TODO_TABLE_NAME, TODO_COL_DUEDATE_NAME) + " AS ScheduledDate_DTypeD, " 
    		+ sqlConcatTableColumn(TODO_TABLE_NAME, TODO_COL_DUEMILEAGE_NAME) + " AS ScheduledMileage_DTypeN, " 
    		+ " CASE "
    			+ " WHEN Minimums.Mileage IS NOT NULL "
		    		+ " THEN ( " + sqlConcatTableColumn(TODO_TABLE_NAME, TODO_COL_DUEMILEAGE_NAME) 
		    				+ " - " + sqlConcatTableColumn(CAR_TABLE_NAME, CAR_COL_INDEXCURRENT_NAME) + ") " //no of mileages until the todo
					+ " / "
					+ "("
							//avg. daily mileage
		    				+ "( " + sqlConcatTableColumn(CAR_TABLE_NAME, CAR_COL_INDEXCURRENT_NAME) + " - Minimums.Mileage ) " 
		    				+ " / "
		    				+ " (strftime('%J','now', 'localtime') - COALESCE(strftime('%J', datetime(Minimums.Date, 'unixepoch'), 'localtime'), 0) ) "
					+ ") " 
				+ " ELSE 99999999999 "
			+ " END " + " AS EstimatedScheduledMileageDate_DTypeL, "  //Estimated days until the due mileage 
			+ " CASE "
				+ " WHEN "
					+ sqlConcatTableColumn(TASK_TABLE_NAME, TASK_COL_SCHEDULEDFOR_NAME) + " = '" + StaticValues.TASK_SCHEDULED_FOR_BOTH + "' "
							+ " AND Minimums.Mileage IS NOT NULL AND Minimums.Date IS NOT NULL "
							+ " AND ( "
					    		+ "( ( " + sqlConcatTableColumn(TODO_TABLE_NAME, TODO_COL_DUEMILEAGE_NAME) 
								+ " - " + sqlConcatTableColumn(CAR_TABLE_NAME, CAR_COL_INDEXCURRENT_NAME) + ") " //no of mileages until the todo
								+ " / "
								+ "("
										+ "( " + sqlConcatTableColumn(CAR_TABLE_NAME, CAR_COL_INDEXCURRENT_NAME) + " - Minimums.Mileage ) " 
										+ " / "
										+ " (strftime('%J','now', 'localtime') - COALESCE(strftime('%J', datetime(Minimums.Date, 'unixepoch'), 'localtime'), 0) ) "
								+ ") ) " 
								+ " < " 
								+ "( " 
									+ " COALESCE(strftime('%J', datetime(" 
											+ sqlConcatTableColumn(TODO_TABLE_NAME, TODO_COL_DUEDATE_NAME) + ", 'unixepoch'), 'localtime'), 0) "
									+ " - "
									+ " strftime('%J','now', 'localtime') " 
								+ " ) "
							+ " ) "
						+ " THEN "
				    		+ "( ( " + sqlConcatTableColumn(TODO_TABLE_NAME, TODO_COL_DUEMILEAGE_NAME) 
							+ " - " + sqlConcatTableColumn(CAR_TABLE_NAME, CAR_COL_INDEXCURRENT_NAME) + ") " //no of mileages until the todo
							+ " / "
							+ "("
									+ "( " + sqlConcatTableColumn(CAR_TABLE_NAME, CAR_COL_INDEXCURRENT_NAME) + " - Minimums.Mileage ) " 
									+ " / "
									+ " (strftime('%J','now', 'localtime') - COALESCE(strftime('%J', datetime(Minimums.Date, 'unixepoch'), 'localtime'), 0) ) "
							+ ") ) " 
				+ " WHEN "
						+ sqlConcatTableColumn(TASK_TABLE_NAME, TASK_COL_SCHEDULEDFOR_NAME) + " = '" + StaticValues.TASK_SCHEDULED_FOR_BOTH + "' "
								+ " AND Minimums.Mileage IS NOT NULL AND Minimums.Date IS NOT NULL "
								+ " AND ( "
						    		+ "( ( " + sqlConcatTableColumn(TODO_TABLE_NAME, TODO_COL_DUEMILEAGE_NAME) 
									+ " - " + sqlConcatTableColumn(CAR_TABLE_NAME, CAR_COL_INDEXCURRENT_NAME) + ") " //no of mileages until the todo
									+ " / "
									+ "("
											+ "( " + sqlConcatTableColumn(CAR_TABLE_NAME, CAR_COL_INDEXCURRENT_NAME) + " - Minimums.Mileage ) " 
											+ " / "
											+ " (strftime('%J','now', 'localtime') - COALESCE(strftime('%J', datetime(Minimums.Date, 'unixepoch'), 'localtime'), 0) ) "
									+ ") ) " 
									+ " > " 
									+ "( " 
										+ " COALESCE(strftime('%J', datetime(" 
												+ sqlConcatTableColumn(TODO_TABLE_NAME, TODO_COL_DUEDATE_NAME) + ", 'unixepoch'), 'localtime'), 0) "
										+ " - "
										+ " strftime('%J','now', 'localtime') " 
									+ " ) "
								+ " ) "
							+ " THEN "
								+ "( " 
									+ " COALESCE(strftime('%J', datetime(" 
											+ sqlConcatTableColumn(TODO_TABLE_NAME, TODO_COL_DUEDATE_NAME) + ", 'unixepoch'), 'localtime'), 0) "
									+ " - "
									+ " strftime('%J','now', 'localtime') " 
								+ " ) "
				+ " WHEN "
					+ "(" + sqlConcatTableColumn(TASK_TABLE_NAME, TASK_COL_SCHEDULEDFOR_NAME) + " = '" + StaticValues.TASK_SCHEDULED_FOR_BOTH + "' "
								+ " OR " + sqlConcatTableColumn(TASK_TABLE_NAME, TASK_COL_SCHEDULEDFOR_NAME) + " = '" + StaticValues.TASK_SCHEDULED_FOR_MILEAGE + "') "
							+ " AND " 
								+ " Minimums.Mileage IS NULL "
						+ " THEN 99999999999 " 
				+ " WHEN "
						+ sqlConcatTableColumn(TASK_TABLE_NAME, TASK_COL_SCHEDULEDFOR_NAME) + " = '" + StaticValues.TASK_SCHEDULED_FOR_MILEAGE + "' "
								+ " AND Minimums.Mileage IS NOT NULL "
						+ " THEN "
				    		+ "( ( " + sqlConcatTableColumn(TODO_TABLE_NAME, TODO_COL_DUEMILEAGE_NAME) 
							+ " - " + sqlConcatTableColumn(CAR_TABLE_NAME, CAR_COL_INDEXCURRENT_NAME) + ") " //no of mileages until the todo
							+ " / "
							+ "("
									+ "( " + sqlConcatTableColumn(CAR_TABLE_NAME, CAR_COL_INDEXCURRENT_NAME) + " - Minimums.Mileage ) " 
									+ " / "
									+ " (strftime('%J','now', 'localtime') - COALESCE(strftime('%J', datetime(Minimums.Date, 'unixepoch'), 'localtime'), 0) ) "
							+ ") ) " 
				+ " WHEN "
						+ sqlConcatTableColumn(TASK_TABLE_NAME, TASK_COL_SCHEDULEDFOR_NAME) + " = '" + StaticValues.TASK_SCHEDULED_FOR_TIME + "' "
						+ " THEN "
							+ "( " 
							+ " COALESCE(strftime('%J', datetime(" 
									+ sqlConcatTableColumn(TODO_TABLE_NAME, TODO_COL_DUEDATE_NAME) + ", 'unixepoch'), 'localtime'), 0) "
							+ " - "
							+ " strftime('%J','now', 'localtime') " 
						+ " ) "
			+ " END AS EstimatedDueDate_DTypeL, "
    		+ " COALESCE( " + sqlConcatTableColumn(TODO_TABLE_NAME, GEN_COL_USER_COMMENT_NAME) + ", '') AS Description "
							
		+ " FROM " + TODO_TABLE_NAME
			+ " JOIN " + TASK_TABLE_NAME + " ON " + sqlConcatTableColumn(TODO_TABLE_NAME, TODO_COL_TASK_ID_NAME) + " = " +
															sqlConcatTableColumn(TASK_TABLE_NAME, GEN_COL_ROWID_NAME)
				+ " JOIN " + TASKTYPE_TABLE_NAME + " ON "+ sqlConcatTableColumn(TASK_TABLE_NAME, TASK_COL_TASKTYPE_ID_NAME) + " = " +
															sqlConcatTableColumn(TASKTYPE_TABLE_NAME, GEN_COL_ROWID_NAME)
			+ " LEFT OUTER JOIN " + CAR_TABLE_NAME + " ON " + sqlConcatTableColumn(TODO_TABLE_NAME, TODO_COL_CAR_ID_NAME) + " = " +
															sqlConcatTableColumn(CAR_TABLE_NAME, GEN_COL_ROWID_NAME)
				+ " LEFT OUTER JOIN " + UOM_TABLE_NAME + " ON " + sqlConcatTableColumn(CAR_TABLE_NAME, CAR_COL_UOMLENGTH_ID_NAME) + " = " +
															sqlConcatTableColumn(UOM_TABLE_NAME, GEN_COL_ROWID_NAME)
			+ " LEFT OUTER JOIN ( "
					+ " SELECT MIN(Date) AS Date, MIN(Mileage) AS Mileage, CAR_ID "  
					+ " FROM "  
						+ " (SELECT MIN(" + MILEAGE_COL_DATE_NAME + ") AS Date, "
									+ " MIN(" + MILEAGE_COL_INDEXSTART_NAME + ") AS Mileage, " 
									+ MILEAGE_COL_CAR_ID_NAME + " AS CAR_ID " 
						+ " FROM " + MILEAGE_TABLE_NAME
						+ " WHERE " + GEN_COL_ISACTIVE_NAME + " = 'Y' "
						+ " GROUP BY " + MILEAGE_COL_CAR_ID_NAME  
						+ " UNION "  
						+ " SELECT MIN(" + REFUEL_COL_DATE_NAME + ") AS Date, "
								+ " MIN(" + REFUEL_COL_INDEX_NAME + ") AS Mileage, " 
								+ REFUEL_COL_CAR_ID_NAME + " AS CAR_ID " 
						+ " FROM " + REFUEL_TABLE_NAME
						+ " WHERE " + GEN_COL_ISACTIVE_NAME + " = 'Y' "
						+ " GROUP BY " + REFUEL_COL_CAR_ID_NAME
						+ " UNION "
						+ " SELECT MIN(" + EXPENSE_COL_DATE_NAME + ") AS Date, " 
								+ " MIN(" + EXPENSE_COL_INDEX_NAME + ") AS Mileage, " 
								+ EXPENSE_COL_CAR_ID_NAME + " AS CAR_ID " 
						+ " FROM " + EXPENSE_TABLE_NAME
						+ " WHERE " + GEN_COL_ISACTIVE_NAME + " = 'Y' "
						+ " GROUP BY " + EXPENSE_COL_CAR_ID_NAME
						+ " ) "
					+ " GROUP BY CAR_ID ) AS Minimums ON Minimums.CAR_ID = " + sqlConcatTableColumn(TODO_TABLE_NAME, TODO_COL_CAR_ID_NAME)
		+ " WHERE 1=1 "
	;

    public ReportDbAdapter( Context ctx, String reportSqlName, Bundle searchCondition )
    {
        super(ctx);
        mReportSqlName = reportSqlName;
        mSearchCondition = searchCondition;
    }

    public void setReportSql(String reportSqlName, Bundle searchCondition){
        mReportSqlName = reportSqlName;
        mSearchCondition = searchCondition;
    }

    public Cursor fetchReport(int limitCount){
        if(mReportSqlName == null)
            return null;
        Set<String> whereColumns = null;
        String whereColumn;
        String reportSql = "";
        String whereCondition = "";
        if(mSearchCondition != null)
            whereColumns = mSearchCondition.keySet();

        if(whereColumns != null && whereColumns.size() > 0){
            for(Iterator<String> it = whereColumns.iterator(); it.hasNext();) {
                whereColumn = it.next();
                //TODO data type treatment required!
                if(whereColumn.startsWith("EstDueDays"))
	                whereCondition = whereCondition +
                    		" AND " + whereColumn + " " + mSearchCondition.getString(whereColumn);
                else{ 
                	if(!mSearchCondition.getString(whereColumn).toUpperCase().equals("NULL"))
                		whereCondition = whereCondition +
	                                " AND " + whereColumn + " '" + mSearchCondition.getString(whereColumn) + "'";
                	else
                		whereCondition = whereCondition +
                    				" AND " + whereColumn + " " + mSearchCondition.getString(whereColumn);
                }
            }
        }

        if(mReportSqlName.equals("mileageListViewSelect")){
            reportSql = mileageListViewSelect;
            if(whereCondition.length() > 0)
                reportSql = reportSql + whereCondition;

            reportSql = reportSql + 
                                    " ORDER BY " + sqlConcatTableColumn(MILEAGE_TABLE_NAME, MILEAGE_COL_INDEXSTOP_NAME) + " DESC";
        }
        else if(mReportSqlName.equals("mileageListReportSelect")){
            reportSql = mileageListReportSelect;
            if(whereCondition.length() > 0)
                reportSql = reportSql + whereCondition;

            reportSql = reportSql +
                                    " ORDER BY " + sqlConcatTableColumn(MILEAGE_TABLE_NAME, MILEAGE_COL_INDEXSTOP_NAME) + " DESC";
        }
        else if(mReportSqlName.equals("refuelListViewSelect")){
            reportSql = refuelListViewSelect;
            if(whereCondition.length() > 0)
                reportSql = reportSql + whereCondition;

            reportSql = reportSql +
                                    " ORDER BY " + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_DATE_NAME) + " DESC";
        }
        else if(mReportSqlName.equals("refuelListReportSelect")){
            reportSql = refuelListReportSelect;
            if(whereCondition.length() > 0)
                reportSql = reportSql + whereCondition;

            reportSql = reportSql +
                                    " ORDER BY " + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_DATE_NAME) + " DESC";
        }
        else if(mReportSqlName.equals("expensesListViewSelect")){
            reportSql = expensesListViewSelect;
            if(whereCondition.length() > 0)
                reportSql = reportSql + whereCondition;

            reportSql = reportSql +
                                    " ORDER BY " + sqlConcatTableColumn(EXPENSE_TABLE_NAME, EXPENSE_COL_DATE_NAME) + " DESC";
        }
        else if(mReportSqlName.equals("expensesListReportSelect")){
            reportSql = expensesListReportSelect;
            if(whereCondition.length() > 0)
                reportSql = reportSql + whereCondition;

            reportSql = reportSql +
                                    " ORDER BY " + sqlConcatTableColumn(EXPENSE_TABLE_NAME, EXPENSE_COL_DATE_NAME) + " DESC";
        }
        else if(mReportSqlName.equals("statisticsMainViewSelect")){
            reportSql = statisticsMainViewSelect;
            if(whereCondition.length() > 0)
                reportSql = reportSql + whereCondition;
        }
        else if(mReportSqlName.equals("gpsTrackListViewSelect")){
            reportSql = gpsTrackListViewSelect;
            if(whereCondition.length() > 0)
                reportSql = reportSql + whereCondition;

            reportSql = reportSql +
                                    " ORDER BY " + sqlConcatTableColumn(GPSTRACK_TABLE_NAME, GPSTRACK_COL_DATE_NAME) + " DESC";
        }
        else if(mReportSqlName.equals("gpsTrackListReportSelect")){
            reportSql = gpsTrackListReportSelect;
            if(whereCondition.length() > 0)
                reportSql = reportSql + whereCondition;

            reportSql = reportSql +
                                    " ORDER BY " + sqlConcatTableColumn(GPSTRACK_TABLE_NAME, GPSTRACK_COL_DATE_NAME) + " DESC";
        }
        else if(mReportSqlName.equals("todoListViewSelect")){
            reportSql = todoListViewSelect;
            if(whereCondition.length() > 0)
                reportSql = reportSql + whereCondition;

            reportSql = reportSql +
                                    " ORDER BY EstDueDays ASC, " + 
                                    	"COALESCE (" + sqlConcatTableColumn(TODO_TABLE_NAME, TODO_COL_DUEMILEAGE_NAME) + ", 0) ASC ";
        }
        else if(mReportSqlName.equals("todoListReportSelect")){
            reportSql = todoListReportSelect;
            if(whereCondition.length() > 0)
                reportSql = reportSql + whereCondition;

            reportSql = reportSql +
                                    " ORDER BY EstimatedDueDate_DTypeL ASC, " + 
                                    	"COALESCE (ScheduledDate_DTypeD , 99999999999) ASC, " +
                                    			"COALESCE(EstimatedScheduledMileageDate_DTypeL, 99999999999) ASC ";
        }

        if(limitCount != -1)
            reportSql = reportSql + " LIMIT " + limitCount;

        Cursor retVal = null;
        retVal = mDb.rawQuery(reportSql, null);
        return retVal;

    }

}
