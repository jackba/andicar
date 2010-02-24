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
    protected String mReportSqlName;
    protected String[] mReportParams;

    public static String reportMileageListViewSelect =
            "SELECT " +
                MILEAGE_TABLE_NAME + "." + GEN_COL_ROWID_NAME +", " +
                EXPENSETYPE_TABLE_NAME + "." + GEN_COL_NAME_NAME + " AS " + FIRST_LINE_LIST_NAME + ", " +
                MILEAGE_TABLE_NAME + "." + GEN_COL_USER_COMMENT_NAME + ", " +
                MILEAGE_TABLE_NAME + "." + MILEAGE_COL_INDEXSTART_NAME + " || '-' || " +
                        MILEAGE_TABLE_NAME + "." + MILEAGE_COL_INDEXSTOP_NAME  + " || ' (' || " +
                        " datetime(" + MILEAGE_TABLE_NAME + "." + MILEAGE_COL_DATE_NAME + ", 'unixepoch', 'localtime')  || ')' AS " + SECOND_LINE_LIST_NAME +
            " FROM " + MILEAGE_TABLE_NAME +
                    " JOIN " + EXPENSETYPE_TABLE_NAME +
                        " ON " + MILEAGE_TABLE_NAME + "." + MILEAGE_COL_EXPENSETYPE_ID_NAME + "=" +
                                EXPENSETYPE_TABLE_NAME + "." + GEN_COL_ROWID_NAME +
            " WHERE " + MILEAGE_TABLE_NAME + "." + MILEAGE_COL_CAR_ID_NAME + "=? " +
            " ORDER BY " + MILEAGE_TABLE_NAME + "." + MILEAGE_COL_INDEXSTOP_NAME + " DESC";

    public static String[] reportMileageListViewSelectCols = {
        GEN_COL_ROWID_NAME,
        FIRST_LINE_LIST_NAME,
        GEN_COL_USER_COMMENT_NAME,
        SECOND_LINE_LIST_NAME
    };

    public ReportDbAdapter( Context ctx, String reportSqlName, String[] reportParams )
    {
        super(ctx);
        mReportSqlName = reportSqlName;
        mReportParams = reportParams;
    }

    public Cursor fetchReport(){
        if(mReportSqlName.equals("reportMileageListViewSelect"))
            return mDb.rawQuery(reportMileageListViewSelect, mReportParams);

        return null;
    }

}
