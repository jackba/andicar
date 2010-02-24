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
import java.math.BigDecimal;
import java.sql.Timestamp;
import org.andicar.utils.Constants;

public class MileageDbAdapter extends MainDbAdapter
{

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public MileageDbAdapter( Context ctx )
    {
        super(ctx);
    }


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
        retVal = checkIndex(mCarId, mStartIndex, mStopIndex);
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
            //update car current index
            
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
                    Timestamp mDateTime, long mCarId, long mDriverId,
                    float mStartIndex, float mStopIndex, long mUOMLengthId,
                    long mExpTypeId, String mGpsTrackLog)
    {
        String retVal = checkIndex(mCarId, mStartIndex, mStopIndex);
        if(retVal != null)
            return retVal;
        
        ContentValues data = new ContentValues();
        data.put( GEN_COL_NAME_NAME, mName );
        data.put( GEN_COL_ISACTIVE_NAME, mIsActive );
        data.put( GEN_COL_USER_COMMENT_NAME, mUserComment );
        data.put(MILEAGE_COL_DATE_NAME, mDateTime.getTime());
        data.put(MILEAGE_COL_CAR_ID_NAME, mCarId);
        data.put(MILEAGE_COL_DRIVER_ID_NAME, mDriverId);
        data.put(MILEAGE_COL_INDEXSTART_NAME, mStartIndex);
        data.put(MILEAGE_COL_INDEXSTOP_NAME, mStopIndex);
        data.put(MILEAGE_COL_UOMLENGTH_ID_NAME, mUOMLengthId);
        data.put(MILEAGE_COL_EXPENSETYPE_ID_NAME, mExpTypeId);
        data.put(MILEAGE_COL_GPSTRACKLOG_NAME, mGpsTrackLog);
        try{
            mDb.update( MILEAGE_TABLE_NAME, data, GEN_COL_ROWID_NAME + "=" + rowId, null );
        }
        catch(SQLException e){
            return e.getMessage();
        }
        return null;
    }


    private String checkIndex(long carId, float startIndex, float stopIndex){

        if(stopIndex <= startIndex)
            return Constants.errStopBeforeStartIndex;

        String checkSql = "";
        checkSql = "SELECT * " +
                    " FROM " + MILEAGE_TABLE_NAME +
                    " WHERE " + MILEAGE_COL_CAR_ID_NAME + "=" + carId +
                            " AND " + MILEAGE_COL_INDEXSTART_NAME + " <= " + startIndex +
                                " AND " + MILEAGE_COL_INDEXSTOP_NAME + " > " + startIndex;
        if(mDb.rawQuery(checkSql, null).getCount() > 0)
            return Constants.errStartIndexOverlap;
        checkSql = "SELECT * " +
                    " FROM " + MILEAGE_TABLE_NAME +
                    " WHERE " + MILEAGE_COL_CAR_ID_NAME + "=" + carId +
                            " AND " + MILEAGE_COL_INDEXSTART_NAME + " < " + stopIndex +
                                " AND " + MILEAGE_COL_INDEXSTOP_NAME + " >= " + stopIndex;
        if(mDb.rawQuery(checkSql, null).getCount() > 0)
            return Constants.errNewIndexOverlap;

        checkSql = "SELECT * " +
                    " FROM " + MILEAGE_TABLE_NAME +
                    " WHERE " + MILEAGE_COL_CAR_ID_NAME + "=" + carId +
                            " AND " + MILEAGE_COL_INDEXSTART_NAME + " >= " + startIndex +
                                " AND " + MILEAGE_COL_INDEXSTOP_NAME + " <= " + stopIndex;
        if(mDb.rawQuery(checkSql, null).getCount() > 0)
            return Constants.errMileageOverlap;

        return null;
    }


}
