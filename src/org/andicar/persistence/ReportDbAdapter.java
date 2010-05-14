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

    //used in main activity and mileage list activity
    public static String mileageListViewSelect =
            "SELECT " +
                sqlConcatTableColumn(MILEAGE_TABLE_NAME, MILEAGE_COL_INDEXSTOP_NAME) + ", " +
                sqlConcatTableColumn(MILEAGE_TABLE_NAME, GEN_COL_ROWID_NAME) + ", " +
                sqlConcatTableColumn(CAR_TABLE_NAME, GEN_COL_NAME_NAME) + " || '; ' || " +
                sqlConcatTableColumn(DRIVER_TABLE_NAME, GEN_COL_NAME_NAME) + " || '; ' || " +
                    sqlConcatTableColumn(EXPENSETYPE_TABLE_NAME, GEN_COL_NAME_NAME)  + " || '; ' || " +
                    " SUBSTR(DATETIME(" + sqlConcatTableColumn(MILEAGE_TABLE_NAME, MILEAGE_COL_DATE_NAME) + ", 'unixepoch', 'localtime'), 1, 10)  " +
                        " AS " + FIRST_LINE_LIST_NAME + ", " +
                sqlConcatTableColumn(MILEAGE_TABLE_NAME, MILEAGE_COL_INDEXSTART_NAME) + " || ' to ' || " +
                    sqlConcatTableColumn(MILEAGE_TABLE_NAME, MILEAGE_COL_INDEXSTOP_NAME)  +  " || ' = ' || " +
                    "(" + sqlConcatTableColumn(MILEAGE_TABLE_NAME, MILEAGE_COL_INDEXSTOP_NAME) + " - " +
                        sqlConcatTableColumn(MILEAGE_TABLE_NAME, MILEAGE_COL_INDEXSTART_NAME) + ") || ' ' || " +
                        sqlConcatTableColumn(UOM_TABLE_NAME, UOM_COL_CODE_NAME) +
                            " AS " + SECOND_LINE_LIST_NAME + ", " +
                sqlConcatTableColumn(MILEAGE_TABLE_NAME, GEN_COL_USER_COMMENT_NAME) + 
                        " AS " + THIRD_LINE_LIST_NAME +
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
            " WHERE 1=1 ";

    //used in exported report
    public static String mileageListReportSelect =
            "SELECT " +
                sqlConcatTableColumn(MILEAGE_TABLE_NAME, GEN_COL_ROWID_NAME) + " AS MileageId, " +
                sqlConcatTableColumn(MILEAGE_TABLE_NAME, GEN_COL_USER_COMMENT_NAME) + ", " +
                "DATETIME(" + sqlConcatTableColumn(MILEAGE_TABLE_NAME, MILEAGE_COL_DATE_NAME) +
                    ", 'unixepoch', 'localtime') AS " + MILEAGE_COL_DATE_NAME + ", " +
                sqlConcatTableColumn(CAR_TABLE_NAME, GEN_COL_NAME_NAME) + " AS CarName, " +
                sqlConcatTableColumn(DRIVER_TABLE_NAME, GEN_COL_NAME_NAME) + " AS DriverName, " +
                sqlConcatTableColumn(MILEAGE_TABLE_NAME, MILEAGE_COL_INDEXSTART_NAME) + ", " +
                sqlConcatTableColumn(MILEAGE_TABLE_NAME, MILEAGE_COL_INDEXSTOP_NAME) + ", " +
                sqlConcatTableColumn(MILEAGE_TABLE_NAME, MILEAGE_COL_INDEXSTOP_NAME) + " - " +
                    sqlConcatTableColumn(MILEAGE_TABLE_NAME, MILEAGE_COL_INDEXSTART_NAME) + " AS Mileage, " +
                sqlConcatTableColumn(UOM_TABLE_NAME, UOM_COL_CODE_NAME) + " AS UomCode, " +
                sqlConcatTableColumn(EXPENSETYPE_TABLE_NAME, GEN_COL_NAME_NAME) + " AS ExpenseTypeName " +
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
            " WHERE 1=1 ";

    //used in main activity and refuel list activity
    public static String refuelListViewSelect =
            "SELECT " +
                sqlConcatTableColumn(REFUEL_TABLE_NAME, GEN_COL_ROWID_NAME) + ", " +
                sqlConcatTableColumn(CAR_TABLE_NAME, GEN_COL_NAME_NAME) + " || '; ' || " +
                sqlConcatTableColumn(DRIVER_TABLE_NAME, GEN_COL_NAME_NAME) + " || '; ' || " +
                    sqlConcatTableColumn(EXPENSETYPE_TABLE_NAME, GEN_COL_NAME_NAME)  + " || '; ' || " +
                    " SUBSTR(DATETIME(" + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_DATE_NAME) + ", 'unixepoch', 'localtime'), 1, 10)  " +
                        " AS " + FIRST_LINE_LIST_NAME + ", " +
                sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_QUANTITYENTERED_NAME) + " || ' ' || " +
                        sqlConcatTableColumn(UOM_TABLE_NAME, UOM_COL_CODE_NAME) + " || " +
                " CASE WHEN " + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_UOMVOLUME_ID_NAME) + " <> " +
                                    sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_UOMVOLUMEENTERED_ID_NAME) + " " +
                        " THEN " + "' (' || " + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_QUANTITY_NAME) +
                                    " || ' ' || " + sqlConcatTableColumn("DefaultVolumeUOM", UOM_COL_CODE_NAME) + " || ')' " +
                        " ELSE " + "'' " +
                " END " +
                        " || ' x ' || " +
                        sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_PRICEENTERED_NAME) + " || ' ' || " +
                        sqlConcatTableColumn(CURRENCY_TABLE_NAME, CURRENCY_COL_CODE_NAME) + " || " +
                        " CASE WHEN " + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_CURRENCY_ID_NAME) + " <> " +
                                            sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_CURRENCYENTERED_ID_NAME) + " " +
                                " THEN " + "' (' || " + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_PRICE_NAME) +
                                            " || ' ' || " + sqlConcatTableColumn("DefaultCurrency", CURRENCY_COL_CODE_NAME) + " || ')' " +
                                " ELSE " + "'' " +
                        " END " +

                        " || ' = ' || ROUND(" +
                        sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_QUANTITYENTERED_NAME) + " * " +
                            sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_PRICEENTERED_NAME) + ", 2) || ' ' ||" +
                            sqlConcatTableColumn(CURRENCY_TABLE_NAME, CURRENCY_COL_CODE_NAME) + " || " +
                        " CASE WHEN " + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_CURRENCY_ID_NAME) + " <> " +
                                            sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_CURRENCYENTERED_ID_NAME) + " " +
                                " THEN " + "' (' || ROUND(" + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_QUANTITYENTERED_NAME) + " * " +
                                                        sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_PRICE_NAME) + ", 2) || ' ' ||" +
                                                        sqlConcatTableColumn("DefaultCurrency", CURRENCY_COL_CODE_NAME) + " || ')' " +
                                " ELSE " + "'' " +
                        " END " +
                            " || ' at ' || " +
                        sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_INDEX_NAME) + " || ' ' || " +
                            sqlConcatTableColumn("CarLengthUOM", UOM_COL_CODE_NAME) +
                            " AS " + SECOND_LINE_LIST_NAME + ", " +
                sqlConcatTableColumn(REFUEL_TABLE_NAME, GEN_COL_USER_COMMENT_NAME) +
                        " AS " + THIRD_LINE_LIST_NAME +
            " FROM " + REFUEL_TABLE_NAME +
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
            " WHERE 1=1 ";

    //used in exported report
    public static String refuelListReportSelect =
            "SELECT " +
                sqlConcatTableColumn(REFUEL_TABLE_NAME, GEN_COL_ROWID_NAME) + " AS RefuelId, " +
                sqlConcatTableColumn(REFUEL_TABLE_NAME, GEN_COL_USER_COMMENT_NAME) + ", " +
                sqlConcatTableColumn(CAR_TABLE_NAME, GEN_COL_NAME_NAME) + " AS CarName, " +
                sqlConcatTableColumn(DRIVER_TABLE_NAME, GEN_COL_NAME_NAME) + " AS DriverName, " +
                sqlConcatTableColumn(EXPENSECATEGORY_TABLE_NAME, GEN_COL_NAME_NAME) + " AS ExpenseCategoryName, " +
                sqlConcatTableColumn(EXPENSETYPE_TABLE_NAME, GEN_COL_NAME_NAME) + " AS ExpenseTypeName, " +
                sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_INDEX_NAME) + ", " +
                sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_QUANTITY_NAME) + ", " +
                sqlConcatTableColumn(UOM_TABLE_NAME, UOM_COL_CODE_NAME) + " AS UOMCode, " +
                sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_PRICE_NAME) + ", " +
                sqlConcatTableColumn(CURRENCY_TABLE_NAME, CURRENCY_COL_CODE_NAME) + " AS CurrencyCode, " +
                "DATETIME(" + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_DATE_NAME) + ", 'unixepoch', 'localtime') AS Date, " +

                sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_QUANTITYENTERED_NAME) + ", " +
                sqlConcatTableColumn("UomVolEntered", UOM_COL_CODE_NAME) + " AS UomEntered, " +
                sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_UOMVOLCONVERSIONRATE_NAME) + ", " +
                sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_PRICEENTERED_NAME) + ", " +
                sqlConcatTableColumn("CurrencyEntered", CURRENCY_COL_CODE_NAME) + " AS CurrencyEntered, " +
                sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_CURRENCYRATE_NAME) + " " +
            " FROM " + REFUEL_TABLE_NAME +
                    " JOIN " + EXPENSETYPE_TABLE_NAME +
                        " ON " + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_EXPENSETYPE_ID_NAME) + "=" +
                                            sqlConcatTableColumn(EXPENSETYPE_TABLE_NAME, GEN_COL_ROWID_NAME) +
                    " JOIN " + EXPENSECATEGORY_TABLE_NAME +
                        " ON " + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_EXPENSECATEGORY_NAME) + "=" +
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
            " WHERE 1=1 ";

    //used in main activity
    public static String expensesListMainViewSelect =
            "SELECT " +
                sqlConcatTableColumn(EXPENSES_TABLE_NAME, GEN_COL_ROWID_NAME) + ", " +
                sqlConcatTableColumn(CAR_TABLE_NAME, GEN_COL_NAME_NAME) + " || '; ' || " +
                sqlConcatTableColumn(DRIVER_TABLE_NAME, GEN_COL_NAME_NAME) + " || '; ' || " +
                "DATE(" + sqlConcatTableColumn(EXPENSES_TABLE_NAME, EXPENSES_COL_DATE_NAME) + ", 'unixepoch', 'localtime') " +
                        "AS " + FIRST_LINE_LIST_NAME + ", " +
                sqlConcatTableColumn(EXPENSECATEGORY_TABLE_NAME, GEN_COL_NAME_NAME) + " || '; ' || " +
                sqlConcatTableColumn(EXPENSETYPE_TABLE_NAME, GEN_COL_NAME_NAME) + " || '; ' || " +
                sqlConcatTableColumn(EXPENSES_TABLE_NAME, EXPENSES_COL_AMOUNT_NAME) + " || ' ' || " +
                sqlConcatTableColumn(CURRENCY_TABLE_NAME, CURRENCY_COL_CODE_NAME) + " || ' at ' || " +
                sqlConcatTableColumn(EXPENSES_TABLE_NAME, EXPENSES_COL_INDEX_NAME) + " || ' ' || " +
                sqlConcatTableColumn(UOM_TABLE_NAME, UOM_COL_CODE_NAME) + " " +
                        "AS " + SECOND_LINE_LIST_NAME + ", " +
                sqlConcatTableColumn(EXPENSES_TABLE_NAME, GEN_COL_USER_COMMENT_NAME) + " " +
                        "AS " + THIRD_LINE_LIST_NAME +
            " FROM " + EXPENSES_TABLE_NAME +
                    " JOIN " + EXPENSETYPE_TABLE_NAME +
                        " ON " + sqlConcatTableColumn(EXPENSES_TABLE_NAME, EXPENSES_COL_EXPENSETYPE_ID_NAME) + "=" +
                                            sqlConcatTableColumn(EXPENSETYPE_TABLE_NAME, GEN_COL_ROWID_NAME) +
                    " JOIN " + EXPENSECATEGORY_TABLE_NAME +
                        " ON " + sqlConcatTableColumn(EXPENSES_TABLE_NAME, EXPENSES_COL_EXPENSECATEGORY_ID_NAME) + "=" +
                                            sqlConcatTableColumn(EXPENSECATEGORY_TABLE_NAME, GEN_COL_ROWID_NAME) +
                    " JOIN " + DRIVER_TABLE_NAME +
                        " ON " + sqlConcatTableColumn(EXPENSES_TABLE_NAME, EXPENSES_COL_DRIVER_ID_NAME) + "=" +
                                            sqlConcatTableColumn(DRIVER_TABLE_NAME, GEN_COL_ROWID_NAME) +
                    " JOIN " + CAR_TABLE_NAME +
                        " ON " + sqlConcatTableColumn(EXPENSES_TABLE_NAME, EXPENSES_COL_CAR_ID_NAME) + "=" +
                                            sqlConcatTableColumn(CAR_TABLE_NAME, GEN_COL_ROWID_NAME) +
                        " JOIN " + UOM_TABLE_NAME +
                            " ON " + sqlConcatTableColumn(CAR_TABLE_NAME, CAR_COL_UOMLENGTH_ID_NAME) + "=" +
                                                sqlConcatTableColumn(UOM_TABLE_NAME, GEN_COL_ROWID_NAME) +
                    " JOIN " + CURRENCY_TABLE_NAME +
                        " ON " + sqlConcatTableColumn(EXPENSES_TABLE_NAME, EXPENSES_COL_CURRENCY_ID_NAME) + "=" +
                                            sqlConcatTableColumn(CURRENCY_TABLE_NAME, GEN_COL_ROWID_NAME) +
            " WHERE COALESCE(" + EXPENSES_COL_FROMTABLE_NAME + ", '') = '' ";

    //used in expense list activity
    public static String expensesListViewSelect =
            "SELECT " +
                sqlConcatTableColumn(EXPENSES_TABLE_NAME, GEN_COL_ROWID_NAME) + ", " +
                sqlConcatTableColumn(CAR_TABLE_NAME, GEN_COL_NAME_NAME) + " || '; ' || " +
                sqlConcatTableColumn(DRIVER_TABLE_NAME, GEN_COL_NAME_NAME) + " || '; ' || " +
                "DATE(" + sqlConcatTableColumn(EXPENSES_TABLE_NAME, EXPENSES_COL_DATE_NAME) + ", 'unixepoch', 'localtime') " +
                        "AS " + FIRST_LINE_LIST_NAME + ", " +
                sqlConcatTableColumn(EXPENSECATEGORY_TABLE_NAME, GEN_COL_NAME_NAME) + " || '; ' || " +
                sqlConcatTableColumn(EXPENSETYPE_TABLE_NAME, GEN_COL_NAME_NAME) + " || '; ' || " +
                sqlConcatTableColumn(EXPENSES_TABLE_NAME, EXPENSES_COL_AMOUNT_NAME) + " || ' ' || " +
                sqlConcatTableColumn(CURRENCY_TABLE_NAME, CURRENCY_COL_CODE_NAME) + " || " +
                " CASE WHEN " + sqlConcatTableColumn(EXPENSES_TABLE_NAME, EXPENSES_COL_CURRENCYENTERED_ID_NAME) + " <> " +
                                    sqlConcatTableColumn(EXPENSES_TABLE_NAME, EXPENSES_COL_CURRENCY_ID_NAME) + " " +
                        " THEN " + "' (' || " + sqlConcatTableColumn(EXPENSES_TABLE_NAME, EXPENSES_COL_AMOUNTENTERED_NAME) +
                                    " || ' ' || " + sqlConcatTableColumn("ec", CURRENCY_COL_CODE_NAME) + " || ')' " +
                        " ELSE " + "'' " +
                " END " +
                " || ' at ' || " +
                sqlConcatTableColumn(EXPENSES_TABLE_NAME, EXPENSES_COL_INDEX_NAME) + " || ' ' || " +
                sqlConcatTableColumn(UOM_TABLE_NAME, UOM_COL_CODE_NAME) + " " +
                        "AS " + SECOND_LINE_LIST_NAME + ", " +
                sqlConcatTableColumn(EXPENSES_TABLE_NAME, GEN_COL_USER_COMMENT_NAME) + " " +
                        "AS " + THIRD_LINE_LIST_NAME +
            " FROM " + EXPENSES_TABLE_NAME +
                    " JOIN " + EXPENSETYPE_TABLE_NAME +
                        " ON " + sqlConcatTableColumn(EXPENSES_TABLE_NAME, EXPENSES_COL_EXPENSETYPE_ID_NAME) + "=" +
                                            sqlConcatTableColumn(EXPENSETYPE_TABLE_NAME, GEN_COL_ROWID_NAME) +
                    " JOIN " + EXPENSECATEGORY_TABLE_NAME +
                        " ON " + sqlConcatTableColumn(EXPENSES_TABLE_NAME, EXPENSES_COL_EXPENSECATEGORY_ID_NAME) + "=" +
                                            sqlConcatTableColumn(EXPENSECATEGORY_TABLE_NAME, GEN_COL_ROWID_NAME) +
                    " JOIN " + DRIVER_TABLE_NAME +
                        " ON " + sqlConcatTableColumn(EXPENSES_TABLE_NAME, EXPENSES_COL_DRIVER_ID_NAME) + "=" +
                                            sqlConcatTableColumn(DRIVER_TABLE_NAME, GEN_COL_ROWID_NAME) +
                    " JOIN " + CAR_TABLE_NAME +
                        " ON " + sqlConcatTableColumn(EXPENSES_TABLE_NAME, EXPENSES_COL_CAR_ID_NAME) + "=" +
                                            sqlConcatTableColumn(CAR_TABLE_NAME, GEN_COL_ROWID_NAME) +
                        " JOIN " + UOM_TABLE_NAME +
                            " ON " + sqlConcatTableColumn(CAR_TABLE_NAME, CAR_COL_UOMLENGTH_ID_NAME) + "=" +
                                                sqlConcatTableColumn(UOM_TABLE_NAME, GEN_COL_ROWID_NAME) +
                    " JOIN " + CURRENCY_TABLE_NAME +
                        " ON " + sqlConcatTableColumn(EXPENSES_TABLE_NAME, EXPENSES_COL_CURRENCY_ID_NAME) + "=" +
                                            sqlConcatTableColumn(CURRENCY_TABLE_NAME, GEN_COL_ROWID_NAME) +
                    " JOIN " + CURRENCY_TABLE_NAME + " AS ec " +
                        " ON " + sqlConcatTableColumn(EXPENSES_TABLE_NAME, EXPENSES_COL_CURRENCYENTERED_ID_NAME) + "=" +
                                            sqlConcatTableColumn("ec", GEN_COL_ROWID_NAME) +
            " WHERE 1=1 ";

    //used in exported report
    public static String expensesListReportSelect =
            "SELECT " +
                sqlConcatTableColumn(EXPENSES_TABLE_NAME, GEN_COL_ROWID_NAME) + " AS ExpenseId, " +
                sqlConcatTableColumn(CAR_TABLE_NAME, GEN_COL_NAME_NAME) + " AS CarName, " +
                "DATETIME(" + sqlConcatTableColumn(EXPENSES_TABLE_NAME, EXPENSES_COL_DATE_NAME) + ", 'unixepoch', 'localtime') AS Date, " +
                sqlConcatTableColumn(EXPENSES_TABLE_NAME, EXPENSES_COL_DOCUMENTNO_NAME) + ", " +
                sqlConcatTableColumn(EXPENSECATEGORY_TABLE_NAME, GEN_COL_NAME_NAME) + " AS ExpenseCategoryName, " +
                sqlConcatTableColumn(EXPENSETYPE_TABLE_NAME, GEN_COL_NAME_NAME) + " AS ExpenseTypeName, " +
                sqlConcatTableColumn(EXPENSES_TABLE_NAME, EXPENSES_COL_AMOUNT_NAME) + ", " +
                sqlConcatTableColumn(CURRENCY_TABLE_NAME, CURRENCY_COL_CODE_NAME) + " AS CurrencyCode, " +
                sqlConcatTableColumn(DRIVER_TABLE_NAME, GEN_COL_NAME_NAME) + " AS DriverName, " +
                sqlConcatTableColumn(EXPENSES_TABLE_NAME, GEN_COL_USER_COMMENT_NAME) + ", " +
                sqlConcatTableColumn(EXPENSES_TABLE_NAME, EXPENSES_COL_FROMTABLE_NAME) + " AS BaseExpense, " +
                sqlConcatTableColumn(EXPENSES_TABLE_NAME, EXPENSES_COL_FROMRECORD_ID_NAME) + " AS BaseExpenseId, " +
                sqlConcatTableColumn(EXPENSES_TABLE_NAME, EXPENSES_COL_INDEX_NAME) + ", " +
                sqlConcatTableColumn(EXPENSES_TABLE_NAME, EXPENSES_COL_AMOUNTENTERED_NAME) + ", " +
                sqlConcatTableColumn("CurrEntered", CURRENCY_COL_CODE_NAME) + " AS CurrencyEnteredCode, " +
                sqlConcatTableColumn(EXPENSES_TABLE_NAME, EXPENSES_COL_CURRENCYRATE_NAME) + " " +
            " FROM " + EXPENSES_TABLE_NAME +
                    " JOIN " + EXPENSETYPE_TABLE_NAME +
                        " ON " + sqlConcatTableColumn(EXPENSES_TABLE_NAME, EXPENSES_COL_EXPENSETYPE_ID_NAME) + "=" +
                                            sqlConcatTableColumn(EXPENSETYPE_TABLE_NAME, GEN_COL_ROWID_NAME) +
                    " JOIN " + EXPENSECATEGORY_TABLE_NAME +
                        " ON " + sqlConcatTableColumn(EXPENSES_TABLE_NAME, EXPENSES_COL_EXPENSECATEGORY_ID_NAME) + "=" +
                                            sqlConcatTableColumn(EXPENSECATEGORY_TABLE_NAME, GEN_COL_ROWID_NAME) +
                    " JOIN " + DRIVER_TABLE_NAME +
                        " ON " + sqlConcatTableColumn(EXPENSES_TABLE_NAME, EXPENSES_COL_DRIVER_ID_NAME) + "=" +
                                            sqlConcatTableColumn(DRIVER_TABLE_NAME, GEN_COL_ROWID_NAME) +
                    " JOIN " + CAR_TABLE_NAME +
                        " ON " + sqlConcatTableColumn(EXPENSES_TABLE_NAME, EXPENSES_COL_CAR_ID_NAME) + "=" +
                                            sqlConcatTableColumn(CAR_TABLE_NAME, GEN_COL_ROWID_NAME) +
                    " JOIN " + CURRENCY_TABLE_NAME +
                        " ON " + sqlConcatTableColumn(EXPENSES_TABLE_NAME, EXPENSES_COL_CURRENCY_ID_NAME) + "=" +
                                            sqlConcatTableColumn(CURRENCY_TABLE_NAME, GEN_COL_ROWID_NAME) +
                    " JOIN " + CURRENCY_TABLE_NAME + " AS CurrEntered " +
                        " ON " + sqlConcatTableColumn(EXPENSES_TABLE_NAME, EXPENSES_COL_CURRENCYENTERED_ID_NAME) + "=" +
                                            sqlConcatTableColumn("CurrEntered", GEN_COL_ROWID_NAME) +
            " WHERE 1=1 ";


    //used in main activity (statistics)
    public static String statisticsMainViewSelect =
            "SELECT " +
                sqlConcatTableColumn(CAR_TABLE_NAME, GEN_COL_ROWID_NAME) + ", " + //#0
                sqlConcatTableColumn(CAR_TABLE_NAME, CAR_COL_INDEXCURRENT_NAME) + " || ' ' || " +
                    sqlConcatTableColumn(UOM_TABLE_NAME, UOM_COL_CODE_NAME) + " " +
                        "AS ActualIndex, " + //#1
                " (SELECT " +
                    " SUM( " + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_QUANTITY_NAME) + ") AS FuelQty " +
                " FROM " + REFUEL_TABLE_NAME + " " +
                " WHERE " + sqlConcatTableColumn(REFUEL_TABLE_NAME, GEN_COL_ISACTIVE_NAME) + " = 'Y' " +
                        " AND " + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_DATE_NAME) + " > " +
                            " COALESCE( " + sqlConcatTableColumn("FullRefuels", "FirstFullRefuel") + ", 0) " +
                        " AND " + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_DATE_NAME) + " <= " +
                                    " COALESCE( " + sqlConcatTableColumn("FullRefuels", "LastFullRefuel") + ", 0) " +
                        " AND " + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_CAR_ID_NAME) + " = " +
                                            sqlConcatTableColumn(CAR_TABLE_NAME, GEN_COL_ROWID_NAME) +
                "  ) AS FuelQty, " + //#2
                sqlConcatTableColumn(CAR_TABLE_NAME, CAR_COL_INDEXCURRENT_NAME) + " AS IndexCurrent, " + //#3
                sqlConcatTableColumn(CAR_TABLE_NAME, CAR_COL_INDEXSTART_NAME) + " AS IndexStart, " + //#4
                sqlConcatTableColumn("UomVolume", UOM_COL_CODE_NAME) + " AS UomVolume, " + //#5
                sqlConcatTableColumn(UOM_TABLE_NAME, UOM_COL_CODE_NAME) + " AS UomLength, " + //#6
                sqlConcatTableColumn("Expenses", "TotalExpenses") + ", " + //#7
                sqlConcatTableColumn(CURRENCY_TABLE_NAME, CURRENCY_COL_CODE_NAME) + ", " + //#8
                " (SELECT " +
                    " MIN( " + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_INDEX_NAME) + ") " +
                " FROM " + REFUEL_TABLE_NAME + " " +
                " WHERE " + sqlConcatTableColumn(REFUEL_TABLE_NAME, GEN_COL_ISACTIVE_NAME) + " = 'Y' " +
                        " AND " + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_DATE_NAME) + " = " +
                            " COALESCE( " + sqlConcatTableColumn("FullRefuels", "FirstFullRefuel") + ", 0) "  +
                        " AND " + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_ISFULLREFUEL_NAME) + " = 'Y' " +
                        " AND " + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_CAR_ID_NAME) + " = " +
                                            sqlConcatTableColumn(CAR_TABLE_NAME, GEN_COL_ROWID_NAME) +
                "  ) AS FirstFullRefuelIndex, " + //#9
                " (SELECT " +
                    " MAX( " + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_INDEX_NAME) + ") " +
                " FROM " + REFUEL_TABLE_NAME + " " +
                " WHERE " + sqlConcatTableColumn(REFUEL_TABLE_NAME, GEN_COL_ISACTIVE_NAME) + " = 'Y' " +
                        " AND " + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_DATE_NAME) + " = " +
                            " COALESCE( " + sqlConcatTableColumn("FullRefuels", "LastFullRefuel") + ", 0) "  +
                        " AND " + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_ISFULLREFUEL_NAME) + " = 'Y' " +
                        " AND " + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_CAR_ID_NAME) + " = " +
                                            sqlConcatTableColumn(CAR_TABLE_NAME, GEN_COL_ROWID_NAME) +
                "  ) AS LastFullRefuelIndex " + //#10
                
            " FROM " + CAR_TABLE_NAME +
                        " JOIN " + UOM_TABLE_NAME +
                            " ON " + sqlConcatTableColumn(CAR_TABLE_NAME, CAR_COL_UOMLENGTH_ID_NAME) + "=" +
                                                sqlConcatTableColumn(UOM_TABLE_NAME, GEN_COL_ROWID_NAME) +
                        " JOIN " + UOM_TABLE_NAME + " AS UomVolume " +
                            " ON " + sqlConcatTableColumn(CAR_TABLE_NAME, CAR_COL_UOMVOLUME_ID_NAME) + "=" +
                                                sqlConcatTableColumn("UomVolume", GEN_COL_ROWID_NAME) +
                        " JOIN " + CURRENCY_TABLE_NAME +
                            " ON " + sqlConcatTableColumn(CAR_TABLE_NAME, CAR_COL_CURRENCY_ID_NAME) + "=" +
                                                sqlConcatTableColumn(CURRENCY_TABLE_NAME, GEN_COL_ROWID_NAME) +
                        //full refuels
                        " LEFT OUTER JOIN ( " +
                                " SELECT " +
                                    " MIN( " + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_DATE_NAME) + ") AS FirstFullRefuel, " +
                                    " MAX( " + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_DATE_NAME) + ") AS LastFullRefuel, " +
                                    REFUEL_COL_CAR_ID_NAME + " " +
                                " FROM " + REFUEL_TABLE_NAME + " " +
                                " WHERE " + sqlConcatTableColumn(REFUEL_TABLE_NAME, GEN_COL_ISACTIVE_NAME) + " = 'Y' " +
                                        " AND " + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_ISFULLREFUEL_NAME) + " = 'Y' " +
                                " GROUP BY " + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_CAR_ID_NAME) + " ) AS FullRefuels " +
                                " ON " + sqlConcatTableColumn(CAR_TABLE_NAME, GEN_COL_ROWID_NAME) + "=" +
                                                sqlConcatTableColumn("FullRefuels", REFUEL_COL_CAR_ID_NAME) +
                        " LEFT OUTER JOIN ( " +
                                " SELECT " +
                                    " SUM( " + sqlConcatTableColumn(EXPENSES_TABLE_NAME, EXPENSES_COL_AMOUNT_NAME) + ") AS TotalExpenses, " +
                                    EXPENSES_COL_CAR_ID_NAME + " " +
                                " FROM " + EXPENSES_TABLE_NAME + " " +
                                            " JOIN " + EXPENSECATEGORY_TABLE_NAME + " ON " +
                                                sqlConcatTableColumn(EXPENSES_TABLE_NAME, EXPENSES_COL_EXPENSECATEGORY_ID_NAME) + " = " +
                                                    sqlConcatTableColumn(EXPENSECATEGORY_TABLE_NAME, GEN_COL_ROWID_NAME) +
                                " WHERE " + sqlConcatTableColumn(EXPENSES_TABLE_NAME, GEN_COL_ISACTIVE_NAME) + " = 'Y' " +
                                    " AND " + sqlConcatTableColumn(EXPENSECATEGORY_TABLE_NAME, EXPENSECATEGORY_COL_ISEXCLUDEFROMMILEAGECOST_NAME) + " = 'N' " +
                                " GROUP BY " + sqlConcatTableColumn(EXPENSES_TABLE_NAME, EXPENSES_COL_CAR_ID_NAME) + " ) AS Expenses " +
                                " ON " + sqlConcatTableColumn(CAR_TABLE_NAME, GEN_COL_ROWID_NAME) + "=" +
                                                sqlConcatTableColumn("Expenses", REFUEL_COL_CAR_ID_NAME) +
            " WHERE 1=1 ";

    //used in main activity and mileage list activity
    public static String gpsTrackListViewSelect =
            "SELECT " +
                sqlConcatTableColumn(GPSTRACK_TABLE_NAME, GEN_COL_ROWID_NAME) + ", " +
                sqlConcatTableColumn(CAR_TABLE_NAME,  GEN_COL_NAME_NAME) + " || '; ' || " +
                        sqlConcatTableColumn(DRIVER_TABLE_NAME, GEN_COL_NAME_NAME) + " || '; ' || " +
                    " SUBSTR(DATETIME(" + sqlConcatTableColumn(GPSTRACK_TABLE_NAME, GPSTRACK_COL_DATE_NAME) + ", 'unixepoch', 'localtime'), 1, 10)  " +
                        " AS " + FIRST_LINE_LIST_NAME + ", " +
                "'%1' || ROUND(" + sqlConcatTableColumn(GPSTRACK_TABLE_NAME, GPSTRACK_COL_DISTNACE_NAME) + ", 2) || ' ' || " +
                                sqlConcatTableColumn(UOM_TABLE_NAME, UOM_COL_CODE_NAME) + " || '; ' || " +
                    "'%2' || ROUND(" + sqlConcatTableColumn(GPSTRACK_TABLE_NAME, GPSTRACK_COL_MAXSPEED_NAME) + ", 2) || ' ' || " +
                                sqlConcatTableColumn(UOM_TABLE_NAME, UOM_COL_CODE_NAME) + " || '/h; ' || " +
                    "'%3' || ROUND(" + sqlConcatTableColumn(GPSTRACK_TABLE_NAME, GPSTRACK_COL_AVGSPEED_NAME) + ", 2) || ' ' || " +
                                sqlConcatTableColumn(UOM_TABLE_NAME, UOM_COL_CODE_NAME) + " || '/h; ' || " +
                    "'%4' || ROUND(" + sqlConcatTableColumn(GPSTRACK_TABLE_NAME, GPSTRACK_COL_AVGMOVINGSPEED_NAME) + ", 2) || ' ' || " +
                                sqlConcatTableColumn(UOM_TABLE_NAME, UOM_COL_CODE_NAME) + " || '/h; ' || " +
                    "'%5' || '; ' || " +
                    "'%6' " +
                    " AS " + SECOND_LINE_LIST_NAME + ", " +
                sqlConcatTableColumn(GPSTRACK_TABLE_NAME, GEN_COL_NAME_NAME) + " || '; ' || " +
                    sqlConcatTableColumn(GPSTRACK_TABLE_NAME, GEN_COL_USER_COMMENT_NAME) +
                        " AS " + THIRD_LINE_LIST_NAME + ", " +
                "ROUND(" + sqlConcatTableColumn(GPSTRACK_TABLE_NAME, GPSTRACK_COL_TOTALTIME_NAME) + ", 2) AS " + FOURTH_LINE_LIST_NAME + ", " +
                "ROUND(" + sqlConcatTableColumn(GPSTRACK_TABLE_NAME, GPSTRACK_COL_MOVINGTIME_NAME)  + ", 2) AS " + FIFTH_LINE_LIST_NAME +
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
            //exclude the track in progress (the no. of trackpoints is updated after terminating the tracking)
            " WHERE " + sqlConcatTableColumn(GPSTRACK_TABLE_NAME, GPSTRACK_COL_TOTALTRACKPOINTS_NAME) + " IS NOT NULL ";

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
                whereCondition = whereCondition +
                                " AND " + whereColumn + " '" + mSearchCondition.getString(whereColumn) + "'";
            }
        }

        if(mReportSqlName.equals("reportMileageListViewSelect")){
            reportSql = mileageListViewSelect;
            if(whereCondition.length() > 0)
                reportSql = reportSql + whereCondition;

            reportSql = reportSql + 
                                    " ORDER BY " + sqlConcatTableColumn(MILEAGE_TABLE_NAME, MILEAGE_COL_INDEXSTOP_NAME) + " DESC";
        }
        else if(mReportSqlName.equals("reportMileageListReportSelect")){
            reportSql = mileageListReportSelect;
            if(whereCondition.length() > 0)
                reportSql = reportSql + whereCondition;

            reportSql = reportSql +
                                    " ORDER BY " + sqlConcatTableColumn(MILEAGE_TABLE_NAME, MILEAGE_COL_INDEXSTOP_NAME) + " DESC";
        }
        else if(mReportSqlName.equals("reportRefuelListViewSelect")){
            reportSql = refuelListViewSelect;
            if(whereCondition.length() > 0)
                reportSql = reportSql + whereCondition;

            reportSql = reportSql +
                                    " ORDER BY " + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_DATE_NAME) + " DESC";
        }
        else if(mReportSqlName.equals("reportRefuelListReportSelect")){
            reportSql = refuelListReportSelect;
            if(whereCondition.length() > 0)
                reportSql = reportSql + whereCondition;

            reportSql = reportSql +
                                    " ORDER BY " + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_DATE_NAME) + " DESC";
        }
        else if(mReportSqlName.equals("reportExpensesListMainViewSelect")){
            reportSql = expensesListMainViewSelect;
            if(whereCondition.length() > 0)
                reportSql = reportSql + whereCondition;

            reportSql = reportSql +
                                    " ORDER BY " + sqlConcatTableColumn(EXPENSES_TABLE_NAME, EXPENSES_COL_DATE_NAME) + " DESC";
        }
        else if(mReportSqlName.equals("reportExpensesListViewSelect")){
            reportSql = expensesListViewSelect;
            if(whereCondition.length() > 0)
                reportSql = reportSql + whereCondition;

            reportSql = reportSql +
                                    " ORDER BY " + sqlConcatTableColumn(EXPENSES_TABLE_NAME, EXPENSES_COL_DATE_NAME) + " DESC";
        }
        else if(mReportSqlName.equals("reportExpensesListReportSelect")){
            reportSql = expensesListReportSelect;
            if(whereCondition.length() > 0)
                reportSql = reportSql + whereCondition;

            reportSql = reportSql +
                                    " ORDER BY " + sqlConcatTableColumn(EXPENSES_TABLE_NAME, EXPENSES_COL_DATE_NAME) + " DESC";
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

        if(limitCount != -1)
            reportSql = reportSql + " LIMIT " + limitCount;

        return mDb.rawQuery(reportSql, null);

    }

    public Cursor query(String sql, String[] args){
        return mDb.rawQuery(sql, args);
    }

}
