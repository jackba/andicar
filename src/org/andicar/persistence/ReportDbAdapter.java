/*
 *  Copyright (C) 2010 Miklos Keresztes (miklos.keresztes@gmail.com)
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.andicar.persistence;

import android.content.Context;
import android.database.Cursor;

/**
 *
 * @author miki
 */
public class ReportDbAdapter extends MainDbAdapter{

    public static String FIRST_LINE_LIST_NAME = "FIRSTLINE";
    public static String SECOND_LINE_LIST_NAME = "SECONDLINE";
    public static String THIRD_LINE_LIST_NAME = "THIRDLINE";
    protected String mReportSqlName;
    protected String[] mReportParams;

    public static String[] genericReportListViewSelectCols = {
        GEN_COL_ROWID_NAME,
        FIRST_LINE_LIST_NAME,
        SECOND_LINE_LIST_NAME,
        THIRD_LINE_LIST_NAME
    };

    public static String reportMileageListViewSelect =
            "SELECT " +
                sqlConcatTableColumn(MILEAGE_TABLE_NAME, GEN_COL_ROWID_NAME) + ", " +
                sqlConcatTableColumn(DRIVER_TABLE_NAME, GEN_COL_NAME_NAME) + " || ' - ' || " +
                    sqlConcatTableColumn(EXPENSETYPE_TABLE_NAME, GEN_COL_NAME_NAME)  + " || ' - ' || " +
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
            " WHERE " + sqlConcatTableColumn(MILEAGE_TABLE_NAME, MILEAGE_COL_CAR_ID_NAME) + "=? " +
            " ORDER BY " + sqlConcatTableColumn(MILEAGE_TABLE_NAME, MILEAGE_COL_INDEXSTOP_NAME) + " DESC";

    public static String reportRefuelListViewSelect =
            "SELECT " +
                sqlConcatTableColumn(REFUEL_TABLE_NAME, GEN_COL_ROWID_NAME) + ", " +
                sqlConcatTableColumn(DRIVER_TABLE_NAME, GEN_COL_NAME_NAME) + " || ' - ' || " +
                    sqlConcatTableColumn(EXPENSETYPE_TABLE_NAME, GEN_COL_NAME_NAME)  + " || ' - ' || " +
                    " SUBSTR(DATETIME(" + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_DATE_NAME) + ", 'unixepoch', 'localtime'), 1, 10)  " +
                        " AS " + FIRST_LINE_LIST_NAME + ", " +
                sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_QUANTITY_NAME) + " || ' ' || " +
                        sqlConcatTableColumn(UOM_TABLE_NAME, UOM_COL_CODE_NAME) + " || ' at ' || " +
                        sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_INDEX_NAME) + " || ' ' || " +
                            sqlConcatTableColumn("CarVUOM", UOM_COL_CODE_NAME) +
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
                        " ON " + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_UOMVOLUME_ID_NAME) + "=" +
                                            sqlConcatTableColumn(UOM_TABLE_NAME, GEN_COL_ROWID_NAME) +
                    " JOIN " + CAR_TABLE_NAME +
                        " ON " + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_CAR_ID_NAME) + "=" +
                                            sqlConcatTableColumn(CAR_TABLE_NAME, GEN_COL_ROWID_NAME) +
                            " JOIN " + UOM_TABLE_NAME + " AS CarVUOM " +
                                " ON " + sqlConcatTableColumn(CAR_TABLE_NAME, CAR_COL_UOMLENGTH_ID_NAME) + "=" +
                                                    sqlConcatTableColumn("CarVUOM", GEN_COL_ROWID_NAME) +
            " WHERE " + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_CAR_ID_NAME) + "=? " +
            " ORDER BY " + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_DATE_NAME) + " DESC";


    public ReportDbAdapter( Context ctx, String reportSqlName, String[] reportParams )
    {
        super(ctx);
        mReportSqlName = reportSqlName;
        mReportParams = reportParams;
    }

    public Cursor fetchReport(){
        if(mReportSqlName.equals("reportMileageListViewSelect"))
            return mDb.rawQuery(reportMileageListViewSelect, mReportParams);
        else if(mReportSqlName.equals("reportRefuelListViewSelect"))
            return mDb.rawQuery(reportRefuelListViewSelect, mReportParams);

        return null;
    }

    private static String sqlConcatTableColumn(String tableName, String columnName){
        return tableName + "." + columnName;
    }

}
