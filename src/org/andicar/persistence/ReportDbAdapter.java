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
        COL_NAME_GEN_ROWID,
        FIRST_LINE_LIST_NAME,
        SECOND_LINE_LIST_NAME,
        THIRD_LINE_LIST_NAME
    };

    public static String[] extendedReportListViewSelectCols = {
        COL_NAME_GEN_ROWID,
        FIRST_LINE_LIST_NAME,
        SECOND_LINE_LIST_NAME,
        THIRD_LINE_LIST_NAME,
        FOURTH_LINE_LIST_NAME,
        FIFTH_LINE_LIST_NAME
    };

    //used in main activity and mileage list activity
    public static String mileageListViewSelect =
            "SELECT " +
                sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_GEN_ROWID) + ", " + //#0
                sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_NAME) + " || '; ' || " + 
                	sqlConcatTableColumn(TABLE_NAME_DRIVER, COL_NAME_GEN_NAME) + " || '; ' || " +
                    sqlConcatTableColumn(TABLE_NAME_EXPENSETYPE, COL_NAME_GEN_NAME)  + " || '; ' || " +
                    " '[#1]' AS " + FIRST_LINE_LIST_NAME + ", " + //#1
                " '[#1] -> [#2] = [#3] ' || " +
                        sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_UOM__CODE) + " || " +
	                " ' [#4]' AS " + SECOND_LINE_LIST_NAME + ", " + //#2
                " COALESCE( " + sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_NAME) + " || '; ', '') || " + 
                		sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_GEN_USER_COMMENT) + 
                        " AS " + THIRD_LINE_LIST_NAME + ", " + //#3
                sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GEN_ROWID) + " AS gpsTrackId, " + //#4
                sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__DATE) + " AS Seconds, " + //#5
                sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__INDEXSTART) + ", " + //#6
                sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__INDEXSTOP) + ", " + //#7
                sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__INDEXSTOP) + " - " + 
                	sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__INDEXSTART) + " AS Mileage, " + //#8
                sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_ROWID) + " AS CarID, " +//#9
                sqlConcatTableColumn(TABLE_NAME_EXPENSETYPE, COL_NAME_GEN_ROWID) + " AS ExpenseTypeID, " +//#10
                sqlConcatTableColumn(TABLE_NAME_CURRENCY, COL_NAME_CURRENCY__CODE) + " AS CurrencyCode, " +//#11
                "( SELECT " + sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_REIMBURSEMENT_CAR_RATES__RATE) +
                " FROM " + TABLE_NAME_REIMBURSEMENT_CAR_RATES +
                " WHERE " +
						sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_REIMBURSEMENT_CAR_RATES__CAR_ID) + " = " +
								sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_ROWID) + 
						" AND " + sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_REIMBURSEMENT_CAR_RATES__EXPENSETYPE_ID) + " = " +
								sqlConcatTableColumn(TABLE_NAME_EXPENSETYPE, COL_NAME_GEN_ROWID) +
						" AND " + sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_REIMBURSEMENT_CAR_RATES__VALIDFROM) + " <= " +
								sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__DATE) +
						" AND " + sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_REIMBURSEMENT_CAR_RATES__VALIDTO) + " >= " +
								sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__DATE) +
				" ORDER BY " + sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_REIMBURSEMENT_CAR_RATES__VALIDFROM) + " DESC, " +
						sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_GEN_ROWID) + " DESC " +
				" LIMIT 1" +
                ") AS ReimbursementRate " + //#12
            " FROM " + TABLE_NAME_MILEAGE +
                    " JOIN " + TABLE_NAME_EXPENSETYPE +
                        " ON " + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__EXPENSETYPE_ID) + "=" +
                                            sqlConcatTableColumn(TABLE_NAME_EXPENSETYPE, COL_NAME_GEN_ROWID) +
                    " JOIN " + TABLE_NAME_DRIVER +
                        " ON " + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__DRIVER_ID) + "=" +
                                            sqlConcatTableColumn(TABLE_NAME_DRIVER, COL_NAME_GEN_ROWID) +
                    " JOIN " + TABLE_NAME_UOM +
                        " ON " + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__UOMLENGTH_ID) + "=" +
                                            sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_GEN_ROWID) +
                    " JOIN " + TABLE_NAME_CAR +
                        " ON " + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__CAR_ID) + "=" +
                                            sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_ROWID) +
	                    " JOIN " + TABLE_NAME_CURRENCY +
	                        " ON " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__CURRENCY_ID) + "=" +
	                                            sqlConcatTableColumn(TABLE_NAME_CURRENCY, COL_NAME_GEN_ROWID) +

                    " LEFT OUTER JOIN " + TABLE_NAME_TAG +
                    	" ON " + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__TAG_ID) + "=" +
                                        sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_ROWID) +
                    " LEFT OUTER JOIN " + TABLE_NAME_GPSTRACK +
                    	" ON " + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_GEN_ROWID) + "=" +
                                        sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__MILEAGE_ID) +
            " WHERE 1=1 ";

    //used in exported report
    public static String mileageListReportSelect =
            "SELECT " +
                sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_GEN_ROWID) + " AS MileageId, " +
                sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_GEN_USER_COMMENT) + ", " +
                "DATETIME(" + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__DATE) +
                    ", 'unixepoch', 'localtime') AS " + COL_NAME_MILEAGE__DATE + ", " +

                "CASE strftime(\"%w\", " + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__DATE) +
                    ", 'unixepoch', 'localtime') " +
                    "WHEN \"0\" THEN '[#d0]' " +
                    "WHEN \"1\" THEN '[#d1]' " +
                    "WHEN \"2\" THEN '[#d2]' " +
                    "WHEN \"3\" THEN '[#d3]' " +
                    "WHEN \"4\" THEN '[#d4]' " +
                    "WHEN \"5\" THEN '[#d5]' " +
                    "WHEN \"6\" THEN '[#d6]' " +
                "END AS " + StaticValues.DAY_OF_WEEK_NAME + ", " +

                    sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_NAME) + " AS CarName, " +
                sqlConcatTableColumn(TABLE_NAME_DRIVER, COL_NAME_GEN_NAME) + " AS DriverName, " +
                sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__INDEXSTART) + " AS " + COL_NAME_MILEAGE__INDEXSTART + "_DTypeN, " +
                sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__INDEXSTOP) + " AS " + COL_NAME_MILEAGE__INDEXSTOP + "_DTypeN, " +
                sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__INDEXSTOP) + " - " +
                    sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__INDEXSTART) + " AS Mileage_DTypeN, " +
                sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_UOM__CODE) + " AS UomCode, " +
                sqlConcatTableColumn(TABLE_NAME_EXPENSETYPE, COL_NAME_GEN_NAME) + " AS ExpenseTypeName, " +
                " COALESCE( " + sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_NAME) + " || '; ', '') AS Tag, " +
                "( SELECT " + sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_REIMBURSEMENT_CAR_RATES__RATE) +
                " FROM " + TABLE_NAME_REIMBURSEMENT_CAR_RATES +
                " WHERE " +
						sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_REIMBURSEMENT_CAR_RATES__CAR_ID) + " = " +
								sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_ROWID) + 
						" AND " + sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_REIMBURSEMENT_CAR_RATES__EXPENSETYPE_ID) + " = " +
								sqlConcatTableColumn(TABLE_NAME_EXPENSETYPE, COL_NAME_GEN_ROWID) +
						" AND " + sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_REIMBURSEMENT_CAR_RATES__VALIDFROM) + " <= " +
								sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__DATE) +
						" AND " + sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_REIMBURSEMENT_CAR_RATES__VALIDTO) + " >= " +
								sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__DATE) +
				" ORDER BY " + sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_REIMBURSEMENT_CAR_RATES__VALIDFROM) + " DESC, " +
						sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_GEN_ROWID) + " DESC " +
				" LIMIT 1" +
                ") AS ReimbursementRate_DTypeR, " +
                sqlConcatTableColumn(TABLE_NAME_CURRENCY, COL_NAME_CURRENCY__CODE) + " || '/' || " + 
                	sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_UOM__CODE) + " AS '', " +
                "(" +sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__INDEXSTOP) + " - " +
                	sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__INDEXSTART) + ") * " +
                "( SELECT " + sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_REIMBURSEMENT_CAR_RATES__RATE) +
                " FROM " + TABLE_NAME_REIMBURSEMENT_CAR_RATES +
                " WHERE " +
						sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_REIMBURSEMENT_CAR_RATES__CAR_ID) + " = " +
								sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_ROWID) + 
						" AND " + sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_REIMBURSEMENT_CAR_RATES__EXPENSETYPE_ID) + " = " +
								sqlConcatTableColumn(TABLE_NAME_EXPENSETYPE, COL_NAME_GEN_ROWID) +
						" AND " + sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_REIMBURSEMENT_CAR_RATES__VALIDFROM) + " <= " +
								sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__DATE) +
						" AND " + sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_REIMBURSEMENT_CAR_RATES__VALIDTO) + " >= " +
								sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__DATE) +
				" ORDER BY " + sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_REIMBURSEMENT_CAR_RATES__VALIDFROM) + " DESC, " +
						sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_GEN_ROWID) + " DESC " +
				" LIMIT 1 " +
				") AS ReimbursementValue_DTypeR" +
            " FROM " + TABLE_NAME_MILEAGE +
                    " JOIN " + TABLE_NAME_EXPENSETYPE +
                        " ON " + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__EXPENSETYPE_ID) + "=" +
                                            sqlConcatTableColumn(TABLE_NAME_EXPENSETYPE, COL_NAME_GEN_ROWID) +
                    " JOIN " + TABLE_NAME_DRIVER +
                        " ON " + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__DRIVER_ID) + "=" +
                                            sqlConcatTableColumn(TABLE_NAME_DRIVER, COL_NAME_GEN_ROWID) +
                    " JOIN " + TABLE_NAME_UOM +
                        " ON " + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__UOMLENGTH_ID) + "=" +
                                            sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_GEN_ROWID) +
                    " JOIN " + TABLE_NAME_CAR +
                        " ON " + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__CAR_ID) + "=" +
                                            sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_ROWID) +
	                    " JOIN " + TABLE_NAME_CURRENCY +
	                        " ON " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__CURRENCY_ID) + "=" +
	                                            sqlConcatTableColumn(TABLE_NAME_CURRENCY, COL_NAME_GEN_ROWID) +
                    " LEFT OUTER JOIN " + TABLE_NAME_TAG +
                    	" ON " + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__TAG_ID) + "=" +
                                    sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_ROWID) +
            " WHERE 1=1 ";

    //used in main activity and refuel list activity
    public static String refuelListViewSelect =
            "SELECT " +
                sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_GEN_ROWID) + ", " + //#0
                sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_NAME) + " || '; ' || " +
                	sqlConcatTableColumn(TABLE_NAME_DRIVER, COL_NAME_GEN_NAME) + " || '; ' || " +
                    sqlConcatTableColumn(TABLE_NAME_EXPENSETYPE, COL_NAME_GEN_NAME)  + " || '; [#1]'" +
                        " AS " + FIRST_LINE_LIST_NAME + ", " + //#1
                 "'[#1] ' || " +
                        sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_UOM__CODE) + " || " +
                " CASE WHEN " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__UOMVOLUME_ID) + " <> " +
                                    sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__UOMVOLUMEENTERED_ID) + " " +
                        " THEN " + "' ([#2]'" +
                                    " || ' ' || " + sqlConcatTableColumn("DefaultVolumeUOM", COL_NAME_UOM__CODE) + " || ')' " +
                        " ELSE " + "'' " +
                " END " +
                        " || ' x [#3] ' || " +
                        	sqlConcatTableColumn(TABLE_NAME_CURRENCY, COL_NAME_CURRENCY__CODE) + " || " +
                        " CASE WHEN " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__CURRENCY_ID) + " <> " +
                                            sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__CURRENCYENTERED_ID) + " " +
                                " THEN " + "' ([#4] ' || " + sqlConcatTableColumn("DefaultCurrency", COL_NAME_CURRENCY__CODE) + " || ')' " +
                                " ELSE " + "'' " +
                        " END " +

                        " || ' = [#5] ' || " +
                            sqlConcatTableColumn(TABLE_NAME_CURRENCY, COL_NAME_CURRENCY__CODE) + " || " +
                        " CASE WHEN " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__CURRENCY_ID) + " <> " +
                                            sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__CURRENCYENTERED_ID) + " " +
                                " THEN " + "' ([#6] ' || " +
                                                        sqlConcatTableColumn("DefaultCurrency", COL_NAME_CURRENCY__CODE) + " || ')' " +
                                " ELSE " + "'' " +
                        " END " +
                            " || ' at [#7] ' || " +
                            sqlConcatTableColumn("CarLengthUOM", COL_NAME_UOM__CODE) +
                            " || ' (' || " + sqlConcatTableColumn(TABLE_NAME_EXPENSECATEGORY, COL_NAME_GEN_NAME) + " || ')' " +
//                            " || '[#8] (' || " + sqlConcatTableColumn(EXPENSECATEGORY_TABLE_NAME, GEN_COL_NAME_NAME) + " || ')' " +
                            " AS " + SECOND_LINE_LIST_NAME + ", " + //#2
                " COALESCE( " + sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_NAME) + " || '; ', '') || " + 
            			sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_GEN_USER_COMMENT) +
                        " AS " + THIRD_LINE_LIST_NAME + ", " + //#3
                sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__DATE) + " AS Seconds, " + //#4
                sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__QUANTITYENTERED) + " AS QtyEntered, " + //#5
                sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__QUANTITY) + " AS Qty, " + //#6
                sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__PRICEENTERED) + " AS PriceEntered, " + //#7
                sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__PRICE) + " AS Price, " + //#8
                sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__AMOUNTENTERED) + " AS AmountEntered, " + //#9
                sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__AMOUNT) + " AS Amount, " + //#10
                sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__INDEX) + " AS CarIndex, " + //#11
                sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__ISFULLREFUEL) + " " + //#12
//                "( SELECT " + REFUEL_COL_INDEX_NAME + " " +
//                	" FROM " + REFUEL_TABLE_NAME + " AS PreviousFullRefuel " +
//                	" WHERE " + isActiveCondition + 
//                			" AND " + REFUEL_COL_ISFULLREFUEL_NAME + " = 'Y' " +
//                			" AND " + sqlConcatTableColumn("PreviousFullRefuel", REFUEL_COL_CAR_ID_NAME) + " = " + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_CAR_ID_NAME) +
//                			" AND " + sqlConcatTableColumn("PreviousFullRefuel", REFUEL_COL_INDEX_NAME) + " < " + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_INDEX_NAME) +
//        			" ORDER BY " + REFUEL_COL_INDEX_NAME + " DESC " +
//        			" LIMIT 1 " +
//                ") AS PreviousFullRefuelIndex " + //#13
            " FROM " + TABLE_NAME_REFUEL +
                    " JOIN " + TABLE_NAME_EXPENSECATEGORY +
                        " ON " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__EXPENSECATEGORY_ID) + "=" +
                                            sqlConcatTableColumn(TABLE_NAME_EXPENSECATEGORY, COL_NAME_GEN_ROWID) +
                    " JOIN " + TABLE_NAME_EXPENSETYPE +
                        " ON " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__EXPENSETYPE_ID) + "=" +
                                            sqlConcatTableColumn(TABLE_NAME_EXPENSETYPE, COL_NAME_GEN_ROWID) +
                    " JOIN " + TABLE_NAME_DRIVER +
                        " ON " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__DRIVER_ID) + "=" +
                                            sqlConcatTableColumn(TABLE_NAME_DRIVER, COL_NAME_GEN_ROWID) +
                    " JOIN " + TABLE_NAME_UOM +
                        " ON " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__UOMVOLUMEENTERED_ID) + "=" +
                                            sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_GEN_ROWID) +
                    " JOIN " + TABLE_NAME_UOM + " AS DefaultVolumeUOM " +
                        " ON " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__UOMVOLUME_ID) + "=" +
                                            sqlConcatTableColumn("DefaultVolumeUOM", COL_NAME_GEN_ROWID) +
                    " JOIN " + TABLE_NAME_CAR +
                        " ON " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__CAR_ID) + "=" +
                                            sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_ROWID) +
                            " JOIN " + TABLE_NAME_UOM + " AS CarLengthUOM " +
                                " ON " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__UOMLENGTH_ID) + "=" +
                                                    sqlConcatTableColumn("CarLengthUOM", COL_NAME_GEN_ROWID) +
                    " JOIN " + TABLE_NAME_CURRENCY +
                        " ON " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__CURRENCYENTERED_ID) + "=" +
                                            sqlConcatTableColumn(TABLE_NAME_CURRENCY, COL_NAME_GEN_ROWID) +
                    " JOIN " + TABLE_NAME_CURRENCY + " AS DefaultCurrency " +
                        " ON " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__CURRENCY_ID) + "=" +
                                            sqlConcatTableColumn("DefaultCurrency", COL_NAME_GEN_ROWID) +
                    " LEFT OUTER JOIN " + TABLE_NAME_TAG +
                    	" ON " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__TAG_ID) + "=" +
                                    sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_ROWID) +
                    " WHERE 1=1 ";

    //used in exported report
    public static String refuelListReportSelect =
            "SELECT " +
                sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_GEN_ROWID) + " AS RefuelId, " +
                sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_GEN_USER_COMMENT) + ", " +
                sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_NAME) + " AS CarName, " +
                sqlConcatTableColumn(TABLE_NAME_DRIVER, COL_NAME_GEN_NAME) + " AS DriverName, " +
                sqlConcatTableColumn(TABLE_NAME_EXPENSECATEGORY, COL_NAME_GEN_NAME) + " AS FuelCategory, " +
                sqlConcatTableColumn(TABLE_NAME_EXPENSETYPE, COL_NAME_GEN_NAME) + " AS ExpenseTypeName, " +
                sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__INDEX) + " AS " + COL_NAME_REFUEL__INDEX + "_DTypeN, " +
                sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__QUANTITY) + " AS " + COL_NAME_REFUEL__QUANTITY + "_DTypeN, " +
                sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__ISFULLREFUEL) + ", " +
                sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_UOM__CODE) + " AS UOMCode, " +
                sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__PRICE) + " AS " + COL_NAME_REFUEL__PRICE + "_DTypeN, " +
                sqlConcatTableColumn(TABLE_NAME_CURRENCY, COL_NAME_CURRENCY__CODE) + " AS CurrencyCode, " +
                "DATETIME(" + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__DATE) + ", 'unixepoch', 'localtime') AS Date, " +

                "CASE strftime(\"%w\", " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__DATE) +
                    ", 'unixepoch', 'localtime') " +
                    "WHEN \"0\" THEN '[#d0]' " +
                    "WHEN \"1\" THEN '[#d1]' " +
                    "WHEN \"2\" THEN '[#d2]' " +
                    "WHEN \"3\" THEN '[#d3]' " +
                    "WHEN \"4\" THEN '[#d4]' " +
                    "WHEN \"5\" THEN '[#d5]' " +
                    "WHEN \"6\" THEN '[#d6]' " +
                "END AS " + StaticValues.DAY_OF_WEEK_NAME + ", " +

                sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__QUANTITYENTERED) + " AS " + COL_NAME_REFUEL__QUANTITYENTERED + "_DTypeN, " +
                sqlConcatTableColumn("UomVolEntered", COL_NAME_UOM__CODE) + " AS UomEntered, " +
                sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__UOMVOLCONVERSIONRATE) + ", " +
                sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__PRICEENTERED) + " AS " + COL_NAME_REFUEL__PRICEENTERED + "_DTypeN, " +
                sqlConcatTableColumn("CurrencyEntered", COL_NAME_CURRENCY__CODE) + " AS CurrencyEntered, " +
                sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__CURRENCYRATE) + " AS " + COL_NAME_REFUEL__CURRENCYRATE + "_DTypeN, " +
                sqlConcatTableColumn(TABLE_NAME_BPARTNER, COL_NAME_GEN_NAME) + " AS Vendor, " +
                sqlConcatTableColumn(TABLE_NAME_BPARTNERLOCATION, COL_NAME_GEN_NAME) +
                        " || COALESCE( '; ' || " + sqlConcatTableColumn(TABLE_NAME_BPARTNERLOCATION, COL_NAME_BPARTNERLOCATION__ADDRESS) + ", '') " +
                        " || COALESCE( '; ' || " + sqlConcatTableColumn(TABLE_NAME_BPARTNERLOCATION, COL_NAME_BPARTNERLOCATION__CITY) + ", '') " +
                        " || COALESCE( '; ' || " + sqlConcatTableColumn(TABLE_NAME_BPARTNERLOCATION, COL_NAME_BPARTNERLOCATION__REGION) + ", '') " +
                        " || COALESCE( '; ' || " + sqlConcatTableColumn(TABLE_NAME_BPARTNERLOCATION, COL_NAME_BPARTNERLOCATION__COUNTRY) + ", '') " +
                        " || COALESCE( '; ' || " + sqlConcatTableColumn(TABLE_NAME_BPARTNERLOCATION, COL_NAME_BPARTNERLOCATION__POSTAL) + ", '') " +
                            " AS Location, " +
                " COALESCE( " + sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_NAME) + " || '; ', '') AS Tag " + 
            " FROM " + TABLE_NAME_REFUEL +
                    " JOIN " + TABLE_NAME_EXPENSETYPE +
                        " ON " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__EXPENSETYPE_ID) + "=" +
                                            sqlConcatTableColumn(TABLE_NAME_EXPENSETYPE, COL_NAME_GEN_ROWID) +
                    " JOIN " + TABLE_NAME_EXPENSECATEGORY +
                        " ON " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__EXPENSECATEGORY_ID) + "=" +
                                            sqlConcatTableColumn(TABLE_NAME_EXPENSECATEGORY, COL_NAME_GEN_ROWID) +
                    " JOIN " + TABLE_NAME_DRIVER +
                        " ON " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__DRIVER_ID) + "=" +
                                            sqlConcatTableColumn(TABLE_NAME_DRIVER, COL_NAME_GEN_ROWID) +
                    " JOIN " + TABLE_NAME_UOM +
                        " ON " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__UOMVOLUME_ID) + "=" +
                                            sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_GEN_ROWID) +
                    " JOIN " + TABLE_NAME_UOM + " AS UomVolEntered " +
                        " ON " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__UOMVOLUMEENTERED_ID) + "=" +
                                            sqlConcatTableColumn("UomVolEntered", COL_NAME_GEN_ROWID) +
                    " JOIN " + TABLE_NAME_CAR +
                        " ON " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__CAR_ID) + "=" +
                                            sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_ROWID) +
                    " JOIN " + TABLE_NAME_CURRENCY +
                        " ON " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__CURRENCY_ID) + "=" +
                                            sqlConcatTableColumn(TABLE_NAME_CURRENCY, COL_NAME_GEN_ROWID) +
                    " JOIN " + TABLE_NAME_CURRENCY + " AS CurrencyEntered " +
                        " ON " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__CURRENCYENTERED_ID) + "=" +
                                            sqlConcatTableColumn("CurrencyEntered", COL_NAME_GEN_ROWID) +
                    " LEFT OUTER JOIN " + TABLE_NAME_BPARTNER +
                        " ON " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__BPARTNER_ID) + "=" +
                                            sqlConcatTableColumn(TABLE_NAME_BPARTNER, COL_NAME_GEN_ROWID) +
                    " LEFT OUTER JOIN " + TABLE_NAME_BPARTNERLOCATION +
                        " ON " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__BPARTNER_LOCATION_ID) + "=" +
                                            sqlConcatTableColumn(TABLE_NAME_BPARTNERLOCATION, COL_NAME_GEN_ROWID) +
                    " LEFT OUTER JOIN " + TABLE_NAME_TAG +
                    	" ON " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__TAG_ID) + "=" +
                                    sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_ROWID) +
            " WHERE 1=1 ";

    //used in main activity & list view
    public static String expensesListViewSelect =
            "SELECT " +
                sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_GEN_ROWID) + ", " + //#0
                sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_NAME) + " || '; ' || " +
                	sqlConcatTableColumn(TABLE_NAME_DRIVER, COL_NAME_GEN_NAME) + " || '; ' || " +
                	sqlConcatTableColumn(TABLE_NAME_EXPENSETYPE, COL_NAME_GEN_NAME) + " || '; [#1]'" +
                        	" AS " + FIRST_LINE_LIST_NAME + ", " + //#1
                sqlConcatTableColumn(TABLE_NAME_EXPENSECATEGORY, COL_NAME_GEN_NAME) + " || '; [#1] ' || " +
                	sqlConcatTableColumn(TABLE_NAME_CURRENCY, COL_NAME_CURRENCY__CODE) + " || " +
                    " CASE WHEN " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__CURRENCY_ID) + " <> " +
                    	sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__CURRENCYENTERED_ID) + " " +
                    	" THEN " + "' ([#2] ' || " +
                                sqlConcatTableColumn("DefaultCurrency", COL_NAME_CURRENCY__CODE) + " || ')' " +
                        " ELSE " + "'' " +
                    " END " 
                		+ " || " + 
                    " CASE WHEN COALESCE(" + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__INDEX) + ", '') <> '' " +
                		" THEN " + "' at [#3] ' || " + sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_UOM__CODE) +
                		" ELSE " + "''" +
                	" END " +
                        " AS " + SECOND_LINE_LIST_NAME + ", " + //#2
                    " COALESCE( " + sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_NAME) + " || '; ', '') || " + 
                	sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_GEN_USER_COMMENT) + " " +
                        "AS " + THIRD_LINE_LIST_NAME +  ", " + //#3
                sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__DATE) + " AS Second, " + //#4
                sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__AMOUNTENTERED) + " AS AmountEntered, " + //#5
                sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__AMOUNT) + " AS Amount, " + //#6
                sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__INDEX) + " AS CarIndex " + //#7
            " FROM " + TABLE_NAME_EXPENSE +
                    " JOIN " + TABLE_NAME_EXPENSETYPE +
                        " ON " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__EXPENSETYPE_ID) + "=" +
                                            sqlConcatTableColumn(TABLE_NAME_EXPENSETYPE, COL_NAME_GEN_ROWID) +
                    " JOIN " + TABLE_NAME_EXPENSECATEGORY +
                        " ON " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__EXPENSECATEGORY_ID) + "=" +
                                            sqlConcatTableColumn(TABLE_NAME_EXPENSECATEGORY, COL_NAME_GEN_ROWID) +
                    " JOIN " + TABLE_NAME_DRIVER +
                        " ON " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__DRIVER_ID) + "=" +
                                            sqlConcatTableColumn(TABLE_NAME_DRIVER, COL_NAME_GEN_ROWID) +
                    " JOIN " + TABLE_NAME_CAR +
                        " ON " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__CAR_ID) + "=" +
                                            sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_ROWID) +
                        " JOIN " + TABLE_NAME_UOM +
                            " ON " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__UOMLENGTH_ID) + "=" +
                                                sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_GEN_ROWID) +
                    " JOIN " + TABLE_NAME_CURRENCY +
                        " ON " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__CURRENCYENTERED_ID) + "=" +
                                            sqlConcatTableColumn(TABLE_NAME_CURRENCY, COL_NAME_GEN_ROWID) +
                    " JOIN " + TABLE_NAME_CURRENCY + " AS DefaultCurrency " +
                    	" ON " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__CURRENCY_ID) + "=" +
                                        sqlConcatTableColumn("DefaultCurrency", COL_NAME_GEN_ROWID) +
                    " LEFT OUTER JOIN " + TABLE_NAME_TAG +
                    	" ON " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__TAG_ID) + "=" +
                                	sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_ROWID) +

//                	" WHERE COALESCE(" + EXPENSE_COL_FROMTABLE_NAME + ", '') = '' ";
    				" WHERE 1=1 ";


    //used in exported report
    public static String expensesListReportSelect =
            "SELECT " +
                sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_GEN_ROWID) + " AS ExpenseId, " +
                sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_NAME) + " AS CarName, " +
                "DATETIME(" + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__DATE) + ", 'unixepoch', 'localtime') AS Date, " +

                "CASE strftime(\"%w\", " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__DATE) +
                    ", 'unixepoch', 'localtime') " +
                    "WHEN \"0\" THEN '[#d0]' " +
                    "WHEN \"1\" THEN '[#d1]' " +
                    "WHEN \"2\" THEN '[#d2]' " +
                    "WHEN \"3\" THEN '[#d3]' " +
                    "WHEN \"4\" THEN '[#d4]' " +
                    "WHEN \"5\" THEN '[#d5]' " +
                    "WHEN \"6\" THEN '[#d6]' " +
                "END AS " + StaticValues.DAY_OF_WEEK_NAME + ", " +

                sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__DOCUMENTNO) + ", " +
                sqlConcatTableColumn(TABLE_NAME_EXPENSECATEGORY, COL_NAME_GEN_NAME) + " AS ExpenseCategoryName, " +
                sqlConcatTableColumn(TABLE_NAME_EXPENSETYPE, COL_NAME_GEN_NAME) + " AS ExpenseTypeName, " +
                sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__AMOUNT) + " AS " + COL_NAME_EXPENSE__AMOUNT + "_DTypeN, " +
                sqlConcatTableColumn(TABLE_NAME_CURRENCY, COL_NAME_CURRENCY__CODE) + " AS CurrencyCode, " +
                sqlConcatTableColumn(TABLE_NAME_DRIVER, COL_NAME_GEN_NAME) + " AS DriverName, " +
                sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_GEN_USER_COMMENT) + ", " +
                sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__FROMTABLE) + " AS BaseExpense, " +
                sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__FROMRECORD_ID) + " AS BaseExpenseId, " +
                sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__INDEX) + " AS " + COL_NAME_EXPENSE__INDEX + "_DTypeN, " +
                sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__AMOUNTENTERED) + " AS " + COL_NAME_EXPENSE__AMOUNTENTERED + "_DTypeN, " +
                sqlConcatTableColumn("CurrEntered", COL_NAME_CURRENCY__CODE) + " AS CurrencyEnteredCode, " +
                sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__CURRENCYRATE) + " AS " + COL_NAME_EXPENSE__CURRENCYRATE + "_DTypeN, " +
                sqlConcatTableColumn(TABLE_NAME_BPARTNER, COL_NAME_GEN_NAME) + " AS Vendor, " +
                sqlConcatTableColumn(TABLE_NAME_BPARTNERLOCATION, COL_NAME_GEN_NAME) +
                        " || COALESCE( '; ' || " + sqlConcatTableColumn(TABLE_NAME_BPARTNERLOCATION, COL_NAME_BPARTNERLOCATION__ADDRESS) + ", '') " +
                        " || COALESCE( '; ' || " + sqlConcatTableColumn(TABLE_NAME_BPARTNERLOCATION, COL_NAME_BPARTNERLOCATION__CITY) + ", '') " +
                        " || COALESCE( '; ' || " + sqlConcatTableColumn(TABLE_NAME_BPARTNERLOCATION, COL_NAME_BPARTNERLOCATION__REGION) + ", '') " +
                        " || COALESCE( '; ' || " + sqlConcatTableColumn(TABLE_NAME_BPARTNERLOCATION, COL_NAME_BPARTNERLOCATION__COUNTRY) + ", '') " +
                        " || COALESCE( '; ' || " + sqlConcatTableColumn(TABLE_NAME_BPARTNERLOCATION, COL_NAME_BPARTNERLOCATION__POSTAL) + ", '') " +
                            " AS Location, " +
                " COALESCE( " + sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_NAME) + " || '; ', '') AS Tag " + 
            " FROM " + TABLE_NAME_EXPENSE +
                    " JOIN " + TABLE_NAME_EXPENSETYPE +
                        " ON " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__EXPENSETYPE_ID) + "=" +
                                            sqlConcatTableColumn(TABLE_NAME_EXPENSETYPE, COL_NAME_GEN_ROWID) +
                    " JOIN " + TABLE_NAME_EXPENSECATEGORY +
                        " ON " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__EXPENSECATEGORY_ID) + "=" +
                                            sqlConcatTableColumn(TABLE_NAME_EXPENSECATEGORY, COL_NAME_GEN_ROWID) +
                    " JOIN " + TABLE_NAME_DRIVER +
                        " ON " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__DRIVER_ID) + "=" +
                                            sqlConcatTableColumn(TABLE_NAME_DRIVER, COL_NAME_GEN_ROWID) +
                    " JOIN " + TABLE_NAME_CAR +
                        " ON " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__CAR_ID) + "=" +
                                            sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_ROWID) +
                    " JOIN " + TABLE_NAME_CURRENCY +
                        " ON " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__CURRENCY_ID) + "=" +
                                            sqlConcatTableColumn(TABLE_NAME_CURRENCY, COL_NAME_GEN_ROWID) +
                    " JOIN " + TABLE_NAME_CURRENCY + " AS CurrEntered " +
                        " ON " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__CURRENCYENTERED_ID) + "=" +
                                            sqlConcatTableColumn("CurrEntered", COL_NAME_GEN_ROWID) +
                    " LEFT OUTER JOIN " + TABLE_NAME_BPARTNER +
                        " ON " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__BPARTNER_ID) + "=" +
                                            sqlConcatTableColumn(TABLE_NAME_BPARTNER, COL_NAME_GEN_ROWID) +
                    " LEFT OUTER JOIN " + TABLE_NAME_BPARTNERLOCATION +
                        " ON " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__BPARTNER_LOCATION_ID) + "=" +
                        					sqlConcatTableColumn(TABLE_NAME_BPARTNERLOCATION, COL_NAME_GEN_ROWID) +
                    " LEFT OUTER JOIN " + TABLE_NAME_TAG +
                		" ON " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__TAG_ID) + "=" +
                            	sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_ROWID) +
            " WHERE 1=1 ";

    public static String statisticsMainViewSelect =
        "SELECT " +
		        sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_ROWID) + ", " + //#0
                sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__INDEXSTART) + ", " + //#1
		        sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__INDEXCURRENT) + ", " + //#2
	            sqlConcatTableColumn("UomLength", COL_NAME_UOM__CODE) + " AS UOMLength, " + //#3
	            sqlConcatTableColumn("TotalExpenses", "Expense") + " AS TotalExpense, " + //#4
	            sqlConcatTableColumn("TotalMileageExpenses", "Expense") + " AS TotalMileageExpense, " + //#5
                sqlConcatTableColumn(TABLE_NAME_CURRENCY, COL_NAME_CURRENCY__CODE) + ", " + //#6
	            sqlConcatTableColumn("UomVolume", COL_NAME_UOM__CODE) + " AS UOMVolume " + //#7
        " FROM " + TABLE_NAME_CAR + 
	        " JOIN " + TABLE_NAME_UOM + " AS UomLength " +
	            " ON " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__UOMLENGTH_ID) + "=" +
	                                sqlConcatTableColumn("UomLength", COL_NAME_GEN_ROWID) +
	        " JOIN " + TABLE_NAME_UOM + " AS UomVolume " +
	            " ON " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__UOMVOLUME_ID) + "=" +
	                                sqlConcatTableColumn("UomVolume", COL_NAME_GEN_ROWID) +
	        " JOIN " + TABLE_NAME_CURRENCY +
	            " ON " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__CURRENCY_ID) + "=" +
	                                sqlConcatTableColumn(TABLE_NAME_CURRENCY, COL_NAME_GEN_ROWID) +
            //total expenses
            " LEFT OUTER JOIN ( " +
                    " SELECT " +
                        " SUM( " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__AMOUNT) + ") AS Expense, " +
                        COL_NAME_EXPENSE__CAR_ID + " " +
                    " FROM " + TABLE_NAME_EXPENSE + " " +
                    " WHERE " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_GEN_ISACTIVE) + " = 'Y' " +
                    " GROUP BY " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__CAR_ID) + " ) AS TotalExpenses " +
		                    " ON " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_ROWID) + "=" +
		                                    sqlConcatTableColumn("TotalExpenses", COL_NAME_EXPENSE__CAR_ID) +
            //total expenses for mileage cost (exclude exp. category which have "Is exclude from mileage cost" attribute
            " LEFT OUTER JOIN ( " +
                                    " SELECT " +
                                        " SUM( " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__AMOUNT) + ") AS Expense, " +
                                        COL_NAME_EXPENSE__CAR_ID + " " +
                                    " FROM " + TABLE_NAME_EXPENSE + " " +
                                                " JOIN " + TABLE_NAME_EXPENSECATEGORY + " ON " +
                                                    sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__EXPENSECATEGORY_ID) + " = " +
                                                        sqlConcatTableColumn(TABLE_NAME_EXPENSECATEGORY, COL_NAME_GEN_ROWID) +
                                    " WHERE " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_GEN_ISACTIVE) + " = 'Y' " +
                                        " AND " + sqlConcatTableColumn(TABLE_NAME_EXPENSECATEGORY, COL_NAME_EXPENSECATEGORY__ISEXCLUDEFROMMILEAGECOST) + " = 'N' " +
                                    " GROUP BY " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__CAR_ID) + " ) AS TotalMileageExpenses " +
		                                    " ON " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_ROWID) + "=" +
		                                                    sqlConcatTableColumn("TotalMileageExpenses", COL_NAME_EXPENSE__CAR_ID) +
        " WHERE 1=1 ";
        
    //used in main activity and GPS Track list activity
    public static String gpsTrackListViewSelect =
            "SELECT " +
                sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GEN_ROWID) + ", " + //#0
                sqlConcatTableColumn(TABLE_NAME_CAR,  COL_NAME_GEN_NAME) + " || '; ' || " +
                        sqlConcatTableColumn(TABLE_NAME_DRIVER, COL_NAME_GEN_NAME) + " || '; ' || '[#1]'" +
                        " AS " + FIRST_LINE_LIST_NAME + ", " +  //#1
                "'[#1]' || ROUND(" + sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__DISTANCE) + ", " + StaticValues.DECIMALS_LENGTH + ") || ' ' || " +
                                sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_UOM__CODE) + " || '; ' || " +
                    "'[#2]' || ROUND(" + sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__MAXSPEED) + ", 2) || ' ' || " +
                                sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_UOM__CODE) + " || '/h; ' || " +
                    "'[#3]' || ROUND(" + sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__AVGSPEED) + ", 2) || ' ' || " +
                                sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_UOM__CODE) + " || '/h; ' || " +
                    "'[#4]' || ROUND(" + sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__AVGMOVINGSPEED) + ", 2) || ' ' || " +
                                sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_UOM__CODE) + " || '/h; ' || " +
                    "'[#5]' || '; ' || " +
                    "'[#6]' || '; ' || " +
                    "'[#12]' || '; ' || " +
                    "'[#7]' || ROUND(" + sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__MINACCURACY) + ", 2) || " +
                        " CASE WHEN UPPER(" + sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_UOM__CODE) + ") == 'KM' " +
                                " THEN " + "' m; ' " +
                                " ELSE " + "' yd; ' " +
                        " END || " +
                    "'[#8]' || ROUND(" + sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__MAXACCURACY) + ", 2) || " +
                        " CASE WHEN UPPER(" + sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_UOM__CODE) + ") == 'KM' " +
                                " THEN " + "' m; ' " +
                                " ELSE " + "' yd; ' " +
                        " END || " +
                    "'[#9]' || ROUND(" + sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__AVGACCURACY) + ", 2) || " +
                        " CASE WHEN UPPER(" + sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_UOM__CODE) + ") == 'KM' " +
                                " THEN " + "' m; ' " +
                                " ELSE " + "' yd; ' " +
                        " END || " +
                    "'[#10]' || " + sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__MINALTITUDE) + " || " +
                        " CASE WHEN UPPER(" + sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_UOM__CODE) + ") == 'KM' " +
                                " THEN " + "' m; ' " +
                                " ELSE " + "' yd; ' " +
                        " END || " +
                    "'[#11]' || " + sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__MAXALTITUDE) + " || " +
                        " CASE WHEN UPPER(" + sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_UOM__CODE) + ") == 'KM' " +
                                " THEN " + "' m; ' " +
                                " ELSE " + "' yd; ' " +
                        " END " +

                    " AS " + SECOND_LINE_LIST_NAME + ", " +  //#2
                " COALESCE( " + sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_NAME) + " || '; ', '') || " + 
			                sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GEN_NAME) + " || '; ' || " +
			                    sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GEN_USER_COMMENT) +
                        " AS " + THIRD_LINE_LIST_NAME + ", " + //#3
                "ROUND(" + sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__TOTALTIME) + ", 2) AS " + FOURTH_LINE_LIST_NAME + ", " + //#4
                "ROUND(" + sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__MOVINGTIME)  + ", 2) AS " + FIFTH_LINE_LIST_NAME + ", " + //#5
                sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GEN_NAME) + ", " + //#6
                sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__DATE) + " AS Seconds, " + //#7
                "ROUND(" + sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__TOTALPAUSETIME)  + ", 2) AS " + COL_NAME_GPSTRACK__TOTALPAUSETIME + //#8
            " FROM " + TABLE_NAME_GPSTRACK +
                    " JOIN " + TABLE_NAME_DRIVER +
                        " ON " + sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__DRIVER_ID) + "=" +
                                            sqlConcatTableColumn(TABLE_NAME_DRIVER, COL_NAME_GEN_ROWID) +
                    " JOIN " + TABLE_NAME_CAR +
                        " ON " + sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__CAR_ID) + "=" +
                                            sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_ROWID) +
                        " JOIN " + TABLE_NAME_UOM +
                            " ON " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__UOMLENGTH_ID) + "=" +
                                                sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_GEN_ROWID) +
                    " LEFT OUTER JOIN " + TABLE_NAME_TAG +
            			" ON " + sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__TAG_ID) + "=" +
                        		sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_ROWID) +
            //exclude the track in progress (the no. of trackpoints is updated after terminating the tracking)
            " WHERE " + sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__TOTALTRACKPOINTS) + " IS NOT NULL ";

    //used in exported report
    public static String gpsTrackListReportSelect =
            "SELECT " +
                sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GEN_ROWID) + " AS TrackId, " +
                sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_NAME) + " AS CarName, " +
                sqlConcatTableColumn(TABLE_NAME_DRIVER, COL_NAME_GEN_NAME) + " AS DriverName, " +
                "DATETIME(" + sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__DATE) + ", 'unixepoch', 'localtime') AS Date, " +

                "CASE strftime(\"%w\", " + sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__DATE) +
                    ", 'unixepoch', 'localtime') " +
                    "WHEN \"0\" THEN '[#d0]' " +
                    "WHEN \"1\" THEN '[#d1]' " +
                    "WHEN \"2\" THEN '[#d2]' " +
                    "WHEN \"3\" THEN '[#d3]' " +
                    "WHEN \"4\" THEN '[#d4]' " +
                    "WHEN \"5\" THEN '[#d5]' " +
                    "WHEN \"6\" THEN '[#d6]' " +
                "END AS " + StaticValues.DAY_OF_WEEK_NAME + ", " +

                sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__MAXACCURACY) + " AS " + COL_NAME_GPSTRACK__MINACCURACY + ", " +
                sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__MINACCURACY) + " AS " + COL_NAME_GPSTRACK__MAXACCURACY + ", " +
                sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__AVGACCURACY) + ", " +
                sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__DISTANCE) + ", " +
                sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__MAXSPEED) + ", " +
                sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__AVGMOVINGSPEED) + ", " +
                sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__AVGSPEED) + ", " +
                sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__MAXALTITUDE) + ", " +
                sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__MINALTITUDE) + ", " +
                sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__TOTALTIME) + " AS '" + COL_NAME_GPSTRACK__TOTALTIME + " [s]', " +
                sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__MOVINGTIME) + " AS '" + COL_NAME_GPSTRACK__MOVINGTIME + " [s]', " +
                sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__TOTALPAUSETIME) + " AS '" + COL_NAME_GPSTRACK__TOTALPAUSETIME + " [s]', " +
                sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__TOTALTRACKPOINTS) + ", " +
                sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__INVALIDTRACKPOINTS) + ", " +
                sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__MILEAGE_ID) + ", " +
                " COALESCE( " + sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_NAME) + " || '; ', '') AS Tag " + 
            " FROM " + TABLE_NAME_GPSTRACK +
                    " JOIN " + TABLE_NAME_DRIVER +
                        " ON " + sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__DRIVER_ID) + "=" +
                                            sqlConcatTableColumn(TABLE_NAME_DRIVER, COL_NAME_GEN_ROWID) +
                    " JOIN " + TABLE_NAME_CAR +
                        " ON " + sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__CAR_ID) + "=" +
                                            sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_ROWID) +
                    " LEFT OUTER JOIN " + TABLE_NAME_TAG +
        				" ON " + sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__TAG_ID) + "=" +
                    						sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_ROWID) +
            " WHERE 1=1 ";
    
    ////used in main activity and todo list activity
    public static String todoListViewSelect = 
    	"SELECT "
			+ sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_GEN_ROWID) + ", "  // #0
			+ "'[#1] ' || " + sqlConcatTableColumn(TABLE_NAME_TASKTYPE, COL_NAME_GEN_NAME) + " || ', ' || " //type - GEN_TypeLabel 
				+ "'[#2] ' || " + sqlConcatTableColumn(TABLE_NAME_TASK, COL_NAME_GEN_NAME) + " || ', ' || " //task - GEN_TaskLabel
				+ " CASE "
					+ " WHEN "  + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_NAME) + " IS NOT NULL "
						+ " THEN "
								+ "'[#3] ' || COALESCE(" + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_NAME) + ", '') || ', ' " //car - GEN_CarLabel
					+ " ELSE '' "
				+ " END || "
				+ "'[#4] ' || " //task status - GEN_StatusLabel
				+ " CASE "
					+ " WHEN " + sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__ISDONE) + " == 'Y' "
						+ " THEN '[#15]' " //done - ToDo_DoneLabel
					+ " WHEN " + sqlConcatTableColumn(TABLE_NAME_TASK, COL_NAME_TASK__SCHEDULEDFOR) + " == '" + StaticValues.TASK_SCHEDULED_FOR_TIME + "' " +
										" AND " + sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__DUEDATE) + " < strftime('%s','now') "
								+ " THEN '[#5]' " //overdue - Todo_OverdueLabel
					+ " WHEN " + sqlConcatTableColumn(TABLE_NAME_TASK, COL_NAME_TASK__SCHEDULEDFOR) + " == '" + StaticValues.TASK_SCHEDULED_FOR_MILEAGE + "' " +
										" AND " + sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__DUEMILEAGE) + " < " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__INDEXCURRENT)
									+ " THEN '[#5]' " //overdue - Todo_OverdueLabel 
					+ " WHEN " + sqlConcatTableColumn(TABLE_NAME_TASK, COL_NAME_TASK__SCHEDULEDFOR) + " == '" + StaticValues.TASK_SCHEDULED_FOR_BOTH + "' " +
										" AND ( " 
											+ sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__DUEMILEAGE) + " < " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__INDEXCURRENT)
											+ " OR "
											+ sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__DUEDATE) + " < strftime('%s','now')) "
									+ " THEN '[#5]' " //overdue - Todo_OverdueLabel
					+ " ELSE '[#6]' " //scheduled - Todo_ScheduledLabel
				+ " END AS "  + FIRST_LINE_LIST_NAME + ", " //datetime(task_todo.DueDate, 'unixepoch', 'localtime') // #1
				
    		+ " CASE "
    				+ " WHEN " + sqlConcatTableColumn(TABLE_NAME_TASK, COL_NAME_TASK__SCHEDULEDFOR) + " == '" + StaticValues.TASK_SCHEDULED_FOR_TIME + "' "
    					+ " THEN '[#7] [#8]' " //duedate label/ToDo_ScheduledDateLabel + date
    				+ " WHEN " + sqlConcatTableColumn(TABLE_NAME_TASK, COL_NAME_TASK__SCHEDULEDFOR) + " == '" + StaticValues.TASK_SCHEDULED_FOR_MILEAGE + "' "
    					+ " THEN '[#10] [#11] ' || "
    							+ " COALESCE (" + sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_UOM__CODE) + ", '') || "
    							+ " ' ([#13] [#14])' " //duemileage label/ToDo_ScheduledMileageLabel + mileage + (estimated date)
    				+ " ELSE '[#7] [#8] [#9] [#11] ' || " // [#12]
								+ " COALESCE (" + sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_UOM__CODE) + ", '') || "
    						+ "' ([#13] [#14])' "
    		+ " END " //duemileage label/ToDo_ScheduledMileageLabel + mileage 
    			+ " AS " + SECOND_LINE_LIST_NAME + ", " // #2
    		+ " COALESCE( " + sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_GEN_USER_COMMENT) + ", '') AS " + THIRD_LINE_LIST_NAME + ", " // #3
			+ sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__DUEDATE) + ", " // #4
    		+ sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__DUEMILEAGE) + ", " // #5
    		+ sqlConcatTableColumn(TABLE_NAME_TASK, COL_NAME_TASK__SCHEDULEDFOR) + ", " // #6
    		+ " CASE "
    			+ " WHEN Minimums.Mileage IS NOT NULL "
		    		+ " THEN ( " + sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__DUEMILEAGE) 
		    				+ " - " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__INDEXCURRENT) + ") " //no of mileages until the todo
					+ " / "
					+ "("
							//avg. daily mileage
		    				+ "( " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__INDEXCURRENT) + " - Minimums.Mileage ) " 
		    				+ " / "
		    				+ " (strftime('%J','now', 'localtime') - COALESCE(strftime('%J', datetime(Minimums.Date, 'unixepoch'), 'localtime'), 0) ) "
					+ ") " 
				+ " ELSE 99999999999 "
			+ " END " + " AS EstDaysUntilDueMileage, "  //Estimated days until the due mileage #7 
			+ "( " 
					+ " COALESCE(strftime('%J', datetime(" 
							+ sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__DUEDATE) + ", 'unixepoch'), 'localtime'), 0) "
					+ " - "
					+ " strftime('%J','now', 'localtime') " 
			+ " ) AS DaysUntilDueDate, " // #8
			+ " CASE "
				+ " WHEN "
					+ sqlConcatTableColumn(TABLE_NAME_TASK, COL_NAME_TASK__SCHEDULEDFOR) + " = '" + StaticValues.TASK_SCHEDULED_FOR_BOTH + "' "
							+ " AND Minimums.Mileage IS NOT NULL AND Minimums.Date IS NOT NULL "
							+ " AND ( "
					    		+ "( ( " + sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__DUEMILEAGE) 
								+ " - " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__INDEXCURRENT) + ") " //no of mileages until the todo
								+ " / "
								+ "("
										+ "( " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__INDEXCURRENT) + " - Minimums.Mileage ) " 
										+ " / "
										+ " (strftime('%J','now', 'localtime') - COALESCE(strftime('%J', datetime(Minimums.Date, 'unixepoch'), 'localtime'), 0) ) "
								+ ") ) " 
								+ " < " 
								+ "( " 
									+ " COALESCE(strftime('%J', datetime(" 
											+ sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__DUEDATE) + ", 'unixepoch'), 'localtime'), 0) "
									+ " - "
									+ " strftime('%J','now', 'localtime') " 
								+ " ) "
							+ " ) "
						+ " THEN "
				    		+ "( ( " + sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__DUEMILEAGE) 
							+ " - " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__INDEXCURRENT) + ") " //no of mileages until the todo
							+ " / "
							+ "("
									+ "( " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__INDEXCURRENT) + " - Minimums.Mileage ) " 
									+ " / "
									+ " (strftime('%J','now', 'localtime') - COALESCE(strftime('%J', datetime(Minimums.Date, 'unixepoch'), 'localtime'), 0) ) "
							+ ") ) " 
				+ " WHEN "
						+ sqlConcatTableColumn(TABLE_NAME_TASK, COL_NAME_TASK__SCHEDULEDFOR) + " = '" + StaticValues.TASK_SCHEDULED_FOR_BOTH + "' "
								+ " AND Minimums.Mileage IS NOT NULL AND Minimums.Date IS NOT NULL "
								+ " AND ( "
						    		+ "( ( " + sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__DUEMILEAGE) 
									+ " - " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__INDEXCURRENT) + ") " //no of mileages until the todo
									+ " / "
									+ "("
											+ "( " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__INDEXCURRENT) + " - Minimums.Mileage ) " 
											+ " / "
											+ " (strftime('%J','now', 'localtime') - COALESCE(strftime('%J', datetime(Minimums.Date, 'unixepoch'), 'localtime'), 0) ) "
									+ ") ) " 
									+ " > " 
									+ "( " 
										+ " COALESCE(strftime('%J', datetime(" 
												+ sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__DUEDATE) + ", 'unixepoch'), 'localtime'), 0) "
										+ " - "
										+ " strftime('%J','now', 'localtime') " 
									+ " ) "
								+ " ) "
							+ " THEN "
								+ "( " 
									+ " COALESCE(strftime('%J', datetime(" 
											+ sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__DUEDATE) + ", 'unixepoch'), 'localtime'), 0) "
									+ " - "
									+ " strftime('%J','now', 'localtime') " 
								+ " ) "
				+ " WHEN "
					+ "(" + sqlConcatTableColumn(TABLE_NAME_TASK, COL_NAME_TASK__SCHEDULEDFOR) + " = '" + StaticValues.TASK_SCHEDULED_FOR_BOTH + "' "
								+ " OR " + sqlConcatTableColumn(TABLE_NAME_TASK, COL_NAME_TASK__SCHEDULEDFOR) + " = '" + StaticValues.TASK_SCHEDULED_FOR_MILEAGE + "') "
							+ " AND " 
								+ " Minimums.Mileage IS NULL "
						+ " THEN 99999999999 " 
				+ " WHEN "
						+ sqlConcatTableColumn(TABLE_NAME_TASK, COL_NAME_TASK__SCHEDULEDFOR) + " = '" + StaticValues.TASK_SCHEDULED_FOR_MILEAGE + "' "
								+ " AND Minimums.Mileage IS NOT NULL "
						+ " THEN "
				    		+ "( ( " + sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__DUEMILEAGE) 
							+ " - " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__INDEXCURRENT) + ") " //no of mileages until the todo
							+ " / "
							+ "("
									+ "( " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__INDEXCURRENT) + " - Minimums.Mileage ) " 
									+ " / "
									+ " (strftime('%J','now', 'localtime') - COALESCE(strftime('%J', datetime(Minimums.Date, 'unixepoch'), 'localtime'), 0) ) "
							+ ") ) " 
				+ " WHEN "
						+ sqlConcatTableColumn(TABLE_NAME_TASK, COL_NAME_TASK__SCHEDULEDFOR) + " = '" + StaticValues.TASK_SCHEDULED_FOR_TIME + "' "
						+ " THEN "
							+ "( " 
							+ " COALESCE(strftime('%J', datetime(" 
									+ sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__DUEDATE) + ", 'unixepoch'), 'localtime'), 0) "
							+ " - "
							+ " strftime('%J','now', 'localtime') " 
						+ " ) "
			+ " END AS EstDueDays, " //#9
			+ sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_NAME) + " AS CarName, " //#10
			+ sqlConcatTableColumn(TABLE_NAME_TASK, COL_NAME_GEN_ROWID) + " AS TaskID, " //#11
			+ sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__INDEXCURRENT) + " AS CarCurrentIndex " //#12
		+ " FROM " + TABLE_NAME_TODO
			+ " JOIN " + TABLE_NAME_TASK + " ON " + sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__TASK_ID) + " = " +
															sqlConcatTableColumn(TABLE_NAME_TASK, COL_NAME_GEN_ROWID)
				+ " JOIN " + TABLE_NAME_TASKTYPE + " ON "+ sqlConcatTableColumn(TABLE_NAME_TASK, COL_NAME_TASK__TASKTYPE_ID) + " = " +
															sqlConcatTableColumn(TABLE_NAME_TASKTYPE, COL_NAME_GEN_ROWID)
			+ " LEFT OUTER JOIN " + TABLE_NAME_CAR + " ON " + sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__CAR_ID) + " = " +
															sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_ROWID)
				+ " LEFT OUTER JOIN " + TABLE_NAME_UOM + " ON " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__UOMLENGTH_ID) + " = " +
															sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_GEN_ROWID)
			+ " LEFT OUTER JOIN ( "
					+ " SELECT MIN(Date) AS Date, MIN(Mileage) AS Mileage, CAR_ID "  
					+ " FROM "  
						+ " (SELECT MIN(" + COL_NAME_MILEAGE__DATE + ") AS Date, "
									+ " MIN(" + COL_NAME_MILEAGE__INDEXSTART + ") AS Mileage, " 
									+ COL_NAME_MILEAGE__CAR_ID + " AS CAR_ID " 
						+ " FROM " + TABLE_NAME_MILEAGE
						+ " WHERE " + COL_NAME_GEN_ISACTIVE + " = 'Y' "
						+ " GROUP BY " + COL_NAME_MILEAGE__CAR_ID  
						+ " UNION "  
						+ " SELECT MIN(" + COL_NAME_REFUEL__DATE + ") AS Date, "
								+ " MIN(" + COL_NAME_REFUEL__INDEX + ") AS Mileage, " 
								+ COL_NAME_REFUEL__CAR_ID + " AS CAR_ID " 
						+ " FROM " + TABLE_NAME_REFUEL
						+ " WHERE " + COL_NAME_GEN_ISACTIVE + " = 'Y' "
						+ " GROUP BY " + COL_NAME_REFUEL__CAR_ID
						+ " UNION "
						+ " SELECT MIN(" + COL_NAME_EXPENSE__DATE + ") AS Date, " 
								+ " MIN(" + COL_NAME_EXPENSE__INDEX + ") AS Mileage, " 
								+ COL_NAME_EXPENSE__CAR_ID + " AS CAR_ID " 
						+ " FROM " + TABLE_NAME_EXPENSE
						+ " WHERE " + COL_NAME_GEN_ISACTIVE + " = 'Y' "
						+ " GROUP BY " + COL_NAME_EXPENSE__CAR_ID
						+ " ) "
					+ " GROUP BY CAR_ID ) AS Minimums ON Minimums.CAR_ID = " + sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__CAR_ID)
		+ " WHERE 1=1 "
	;
    
    //used in exported reports
    public static String todoListReportSelect = 
    	"SELECT "
			+ sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_GEN_ROWID) + " AS ToDoID, " //#0
			+ sqlConcatTableColumn(TABLE_NAME_TASKTYPE, COL_NAME_GEN_NAME) + " AS TaskType, "
			+ sqlConcatTableColumn(TABLE_NAME_TASK, COL_NAME_GEN_NAME) + " AS Task, "
			+ sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_NAME) + " AS Car, "
			+ " CASE "
					+ " WHEN " + sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__ISDONE) + " == 'Y' "
						+ " THEN '[#TDR1]' " //done - ToDo_DoneLabel
					+ " WHEN " + sqlConcatTableColumn(TABLE_NAME_TASK, COL_NAME_TASK__SCHEDULEDFOR) + " == '" + StaticValues.TASK_SCHEDULED_FOR_TIME + "' " +
										" AND " + sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__DUEDATE) + " < strftime('%s','now') "
								+ " THEN '[#TDR2]' " //overdue - Todo_OverdueLabel
					+ " WHEN " + sqlConcatTableColumn(TABLE_NAME_TASK, COL_NAME_TASK__SCHEDULEDFOR) + " == '" + StaticValues.TASK_SCHEDULED_FOR_MILEAGE + "' " +
										" AND " + sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__DUEMILEAGE) + " < " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__INDEXCURRENT)
									+ " THEN '[#TDR2]' " //overdue - Todo_OverdueLabel 
					+ " WHEN " + sqlConcatTableColumn(TABLE_NAME_TASK, COL_NAME_TASK__SCHEDULEDFOR) + " == '" + StaticValues.TASK_SCHEDULED_FOR_BOTH + "' " +
										" AND ( " 
											+ sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__DUEMILEAGE) + " < " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__INDEXCURRENT)
											+ " OR "
											+ sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__DUEDATE) + " < strftime('%s','now')) "
									+ " THEN '[#TDR2]' " //overdue - Todo_OverdueLabel
					+ " ELSE '[#TDR3]' " //scheduled - Todo_ScheduledLabel
				+ " END AS Status, "
				
    		+ " CASE " + sqlConcatTableColumn(TABLE_NAME_TASK, COL_NAME_TASK__SCHEDULEDFOR)
    				+ " WHEN '" + StaticValues.TASK_SCHEDULED_FOR_TIME + "' "
    					+ " THEN '[#TDR4]'" //time
    				+ " WHEN '" + StaticValues.TASK_SCHEDULED_FOR_MILEAGE + "' "
    					+ " THEN '[#TDR5]'" //mileage
    				+ " ELSE '[#TDR6]'"
    		+ " END AS ScheduledFor, "  
			+ sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__DUEDATE) + " AS ScheduledDate_DTypeD, " 
    		+ sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__DUEMILEAGE) + " AS ScheduledMileage_DTypeN, " 
    		+ " CASE "
    			+ " WHEN Minimums.Mileage IS NOT NULL "
		    		+ " THEN ( " + sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__DUEMILEAGE) 
		    				+ " - " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__INDEXCURRENT) + ") " //no of mileages until the todo
					+ " / "
					+ "("
							//avg. daily mileage
		    				+ "( " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__INDEXCURRENT) + " - Minimums.Mileage ) " 
		    				+ " / "
		    				+ " (strftime('%J','now', 'localtime') - COALESCE(strftime('%J', datetime(Minimums.Date, 'unixepoch'), 'localtime'), 0) ) "
					+ ") " 
				+ " ELSE 99999999999 "
			+ " END " + " AS EstimatedScheduledMileageDate_DTypeL, "  //Estimated days until the due mileage 
			+ " CASE "
				+ " WHEN "
					+ sqlConcatTableColumn(TABLE_NAME_TASK, COL_NAME_TASK__SCHEDULEDFOR) + " = '" + StaticValues.TASK_SCHEDULED_FOR_BOTH + "' "
							+ " AND Minimums.Mileage IS NOT NULL AND Minimums.Date IS NOT NULL "
							+ " AND ( "
					    		+ "( ( " + sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__DUEMILEAGE) 
								+ " - " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__INDEXCURRENT) + ") " //no of mileages until the todo
								+ " / "
								+ "("
										+ "( " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__INDEXCURRENT) + " - Minimums.Mileage ) " 
										+ " / "
										+ " (strftime('%J','now', 'localtime') - COALESCE(strftime('%J', datetime(Minimums.Date, 'unixepoch'), 'localtime'), 0) ) "
								+ ") ) " 
								+ " < " 
								+ "( " 
									+ " COALESCE(strftime('%J', datetime(" 
											+ sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__DUEDATE) + ", 'unixepoch'), 'localtime'), 0) "
									+ " - "
									+ " strftime('%J','now', 'localtime') " 
								+ " ) "
							+ " ) "
						+ " THEN "
				    		+ "( ( " + sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__DUEMILEAGE) 
							+ " - " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__INDEXCURRENT) + ") " //no of mileages until the todo
							+ " / "
							+ "("
									+ "( " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__INDEXCURRENT) + " - Minimums.Mileage ) " 
									+ " / "
									+ " (strftime('%J','now', 'localtime') - COALESCE(strftime('%J', datetime(Minimums.Date, 'unixepoch'), 'localtime'), 0) ) "
							+ ") ) " 
				+ " WHEN "
						+ sqlConcatTableColumn(TABLE_NAME_TASK, COL_NAME_TASK__SCHEDULEDFOR) + " = '" + StaticValues.TASK_SCHEDULED_FOR_BOTH + "' "
								+ " AND Minimums.Mileage IS NOT NULL AND Minimums.Date IS NOT NULL "
								+ " AND ( "
						    		+ "( ( " + sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__DUEMILEAGE) 
									+ " - " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__INDEXCURRENT) + ") " //no of mileages until the todo
									+ " / "
									+ "("
											+ "( " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__INDEXCURRENT) + " - Minimums.Mileage ) " 
											+ " / "
											+ " (strftime('%J','now', 'localtime') - COALESCE(strftime('%J', datetime(Minimums.Date, 'unixepoch'), 'localtime'), 0) ) "
									+ ") ) " 
									+ " > " 
									+ "( " 
										+ " COALESCE(strftime('%J', datetime(" 
												+ sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__DUEDATE) + ", 'unixepoch'), 'localtime'), 0) "
										+ " - "
										+ " strftime('%J','now', 'localtime') " 
									+ " ) "
								+ " ) "
							+ " THEN "
								+ "( " 
									+ " COALESCE(strftime('%J', datetime(" 
											+ sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__DUEDATE) + ", 'unixepoch'), 'localtime'), 0) "
									+ " - "
									+ " strftime('%J','now', 'localtime') " 
								+ " ) "
				+ " WHEN "
					+ "(" + sqlConcatTableColumn(TABLE_NAME_TASK, COL_NAME_TASK__SCHEDULEDFOR) + " = '" + StaticValues.TASK_SCHEDULED_FOR_BOTH + "' "
								+ " OR " + sqlConcatTableColumn(TABLE_NAME_TASK, COL_NAME_TASK__SCHEDULEDFOR) + " = '" + StaticValues.TASK_SCHEDULED_FOR_MILEAGE + "') "
							+ " AND " 
								+ " Minimums.Mileage IS NULL "
						+ " THEN 99999999999 " 
				+ " WHEN "
						+ sqlConcatTableColumn(TABLE_NAME_TASK, COL_NAME_TASK__SCHEDULEDFOR) + " = '" + StaticValues.TASK_SCHEDULED_FOR_MILEAGE + "' "
								+ " AND Minimums.Mileage IS NOT NULL "
						+ " THEN "
				    		+ "( ( " + sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__DUEMILEAGE) 
							+ " - " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__INDEXCURRENT) + ") " //no of mileages until the todo
							+ " / "
							+ "("
									+ "( " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__INDEXCURRENT) + " - Minimums.Mileage ) " 
									+ " / "
									+ " (strftime('%J','now', 'localtime') - COALESCE(strftime('%J', datetime(Minimums.Date, 'unixepoch'), 'localtime'), 0) ) "
							+ ") ) " 
				+ " WHEN "
						+ sqlConcatTableColumn(TABLE_NAME_TASK, COL_NAME_TASK__SCHEDULEDFOR) + " = '" + StaticValues.TASK_SCHEDULED_FOR_TIME + "' "
						+ " THEN "
							+ "( " 
							+ " COALESCE(strftime('%J', datetime(" 
									+ sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__DUEDATE) + ", 'unixepoch'), 'localtime'), 0) "
							+ " - "
							+ " strftime('%J','now', 'localtime') " 
						+ " ) "
			+ " END AS EstimatedDueDate_DTypeL, "
    		+ " COALESCE( " + sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_GEN_USER_COMMENT) + ", '') AS Description "
							
		+ " FROM " + TABLE_NAME_TODO
			+ " JOIN " + TABLE_NAME_TASK + " ON " + sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__TASK_ID) + " = " +
															sqlConcatTableColumn(TABLE_NAME_TASK, COL_NAME_GEN_ROWID)
				+ " JOIN " + TABLE_NAME_TASKTYPE + " ON "+ sqlConcatTableColumn(TABLE_NAME_TASK, COL_NAME_TASK__TASKTYPE_ID) + " = " +
															sqlConcatTableColumn(TABLE_NAME_TASKTYPE, COL_NAME_GEN_ROWID)
			+ " LEFT OUTER JOIN " + TABLE_NAME_CAR + " ON " + sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__CAR_ID) + " = " +
															sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_ROWID)
				+ " LEFT OUTER JOIN " + TABLE_NAME_UOM + " ON " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__UOMLENGTH_ID) + " = " +
															sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_GEN_ROWID)
			+ " LEFT OUTER JOIN ( "
					+ " SELECT MIN(Date) AS Date, MIN(Mileage) AS Mileage, CAR_ID "  
					+ " FROM "  
						+ " (SELECT MIN(" + COL_NAME_MILEAGE__DATE + ") AS Date, "
									+ " MIN(" + COL_NAME_MILEAGE__INDEXSTART + ") AS Mileage, " 
									+ COL_NAME_MILEAGE__CAR_ID + " AS CAR_ID " 
						+ " FROM " + TABLE_NAME_MILEAGE
						+ " WHERE " + COL_NAME_GEN_ISACTIVE + " = 'Y' "
						+ " GROUP BY " + COL_NAME_MILEAGE__CAR_ID  
						+ " UNION "  
						+ " SELECT MIN(" + COL_NAME_REFUEL__DATE + ") AS Date, "
								+ " MIN(" + COL_NAME_REFUEL__INDEX + ") AS Mileage, " 
								+ COL_NAME_REFUEL__CAR_ID + " AS CAR_ID " 
						+ " FROM " + TABLE_NAME_REFUEL
						+ " WHERE " + COL_NAME_GEN_ISACTIVE + " = 'Y' "
						+ " GROUP BY " + COL_NAME_REFUEL__CAR_ID
						+ " UNION "
						+ " SELECT MIN(" + COL_NAME_EXPENSE__DATE + ") AS Date, " 
								+ " MIN(" + COL_NAME_EXPENSE__INDEX + ") AS Mileage, " 
								+ COL_NAME_EXPENSE__CAR_ID + " AS CAR_ID " 
						+ " FROM " + TABLE_NAME_EXPENSE
						+ " WHERE " + COL_NAME_GEN_ISACTIVE + " = 'Y' "
						+ " GROUP BY " + COL_NAME_EXPENSE__CAR_ID
						+ " ) "
					+ " GROUP BY CAR_ID ) AS Minimums ON Minimums.CAR_ID = " + sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__CAR_ID)
		+ " WHERE 1=1 "
	;

    //used in main activity and mileage list activity
    public static String reimbursementRateListViewSelect =
            "SELECT " +
                sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_GEN_ROWID) + ", " + //#0
                sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_NAME) + " || '; ' || " + 
                    sqlConcatTableColumn(TABLE_NAME_EXPENSETYPE, COL_NAME_GEN_NAME)  + 
                    		" AS " + FIRST_LINE_LIST_NAME + ", " + //#1
                " '[#1] -> [#2] = [#3] ' || " +
                	sqlConcatTableColumn(TABLE_NAME_CURRENCY, COL_NAME_CURRENCY__CODE) + " || '/' || " +
                		sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_UOM__CODE) +  
                	" AS " + SECOND_LINE_LIST_NAME + ", " + //#2
                COL_NAME_REIMBURSEMENT_CAR_RATES__VALIDFROM + ", " + //#3
                COL_NAME_REIMBURSEMENT_CAR_RATES__VALIDTO + ", " + //#4
                COL_NAME_REIMBURSEMENT_CAR_RATES__RATE + //#5
            " FROM " + TABLE_NAME_REIMBURSEMENT_CAR_RATES +
                    " JOIN " + TABLE_NAME_EXPENSETYPE +
                        " ON " + sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_REIMBURSEMENT_CAR_RATES__EXPENSETYPE_ID) + "=" +
                                            sqlConcatTableColumn(TABLE_NAME_EXPENSETYPE, COL_NAME_GEN_ROWID) +
                    " JOIN " + TABLE_NAME_CAR +
                        " ON " + sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_MILEAGE__CAR_ID) + "=" +
                                            sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_ROWID) +
	                    " JOIN " + TABLE_NAME_UOM + 
	                        " ON " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__UOMLENGTH_ID) + "=" +
	                                            sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_GEN_ROWID) +
	                    " JOIN " + TABLE_NAME_CURRENCY + 
	                        " ON " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__CURRENCY_ID) + "=" +
	                                            sqlConcatTableColumn(TABLE_NAME_CURRENCY, COL_NAME_GEN_ROWID) +
            " WHERE 1=1 ";

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
                                    " ORDER BY " + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__INDEXSTOP) + " DESC";
        }
        else if(mReportSqlName.equals("mileageListReportSelect")){
            reportSql = mileageListReportSelect;
            if(whereCondition.length() > 0)
                reportSql = reportSql + whereCondition;

            reportSql = reportSql +
                                    " ORDER BY " + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__INDEXSTOP) + " DESC";
        }
        else if(mReportSqlName.equals("refuelListViewSelect")){
            reportSql = refuelListViewSelect;
            if(whereCondition.length() > 0)
                reportSql = reportSql + whereCondition;

            reportSql = reportSql +
                                    " ORDER BY " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__DATE) + " DESC";
        }
        else if(mReportSqlName.equals("refuelListReportSelect")){
            reportSql = refuelListReportSelect;
            if(whereCondition.length() > 0)
                reportSql = reportSql + whereCondition;

            reportSql = reportSql +
                                    " ORDER BY " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__DATE) + " DESC";
        }
        else if(mReportSqlName.equals("expensesListViewSelect")){
            reportSql = expensesListViewSelect;
            if(whereCondition.length() > 0)
                reportSql = reportSql + whereCondition;

            reportSql = reportSql +
                                    " ORDER BY " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__DATE) + " DESC";
        }
        else if(mReportSqlName.equals("expensesListReportSelect")){
            reportSql = expensesListReportSelect;
            if(whereCondition.length() > 0)
                reportSql = reportSql + whereCondition;

            reportSql = reportSql +
                                    " ORDER BY " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__DATE) + " DESC";
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
                                    " ORDER BY " + sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__DATE) + " DESC";
        }
        else if(mReportSqlName.equals("gpsTrackListReportSelect")){
            reportSql = gpsTrackListReportSelect;
            if(whereCondition.length() > 0)
                reportSql = reportSql + whereCondition;

            reportSql = reportSql +
                                    " ORDER BY " + sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__DATE) + " DESC";
        }
        else if(mReportSqlName.equals("todoListViewSelect")){
            reportSql = todoListViewSelect;
            if(whereCondition.length() > 0)
                reportSql = reportSql + whereCondition;

            reportSql = reportSql +
                                    " ORDER BY EstDueDays ASC, " + 
                                    	"COALESCE (" + sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__DUEMILEAGE) + ", 0) ASC ";
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
        else if(mReportSqlName.equals("reimbursementRateListViewSelect")){
            reportSql = reimbursementRateListViewSelect;
            if(whereCondition.length() > 0)
                reportSql = reportSql + whereCondition;

            reportSql = reportSql +" ORDER BY " + 
            			sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_REIMBURSEMENT_CAR_RATES__VALIDFROM) + " DESC , " +
            			sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_REIMBURSEMENT_CAR_RATES__EXPENSETYPE_ID) + ", " +
            			sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_REIMBURSEMENT_CAR_RATES__CAR_ID);
        }

        if(limitCount != -1)
            reportSql = reportSql + " LIMIT " + limitCount;

        Cursor retVal = null;
        retVal = mDb.rawQuery(reportSql, null);
        return retVal;

    }

}
