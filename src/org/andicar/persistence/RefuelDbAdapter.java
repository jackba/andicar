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
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import org.andicar.activity.R;

public class RefuelDbAdapter extends MainDbAdapter
{
    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public RefuelDbAdapter( Context ctx )
    {
        super( ctx );
    }

    /**
     * Return a Cursor over the list of all refuel's
     * @param withInactive if true fetch alse the inactive records
     * @return Cursor over all refuel's
     */
    public Cursor fetchAll( boolean withInactive )
    {
        if( withInactive ) {
            return mDb.query( REFUEL_TABLE_NAME, refuelTableColNames, null, null, null, null, GEN_COL_NAME_NAME );
        }
        else {
            return mDb.query( REFUEL_TABLE_NAME, refuelTableColNames, GEN_COL_ISACTIVE_NAME + "='Y'", null, null, null, GEN_COL_NAME_NAME );
        }
    }

    /**
     * Return a Cursor positioned at the Refuel that matches the given rowId
     * 
     * @param rowId id of Refuel to retrieve
     * @return Cursor positioned to matching car, if found
     * @throws SQLException if car could not be found/retrieved
     */
    public Cursor fetchRefuel( long rowId ) throws SQLException
    {
        Cursor mCursor =
                mDb.query( true, REFUEL_TABLE_NAME, refuelTableColNames,
                GEN_COL_ROWID_NAME + "=" + rowId, null, null, null, null, null );
        if( mCursor != null ) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public long createRefuel( String Name, String IsActive, String UserComment, 
            long carId, long driverId, long expenseTypeId, float carIndex, float qty, float price, long currencyId,
            long date, String documentNo)
    {
        ContentValues args = new ContentValues();
        args.put( GEN_COL_NAME_NAME, Name );
        args.put( GEN_COL_ISACTIVE_NAME, IsActive );
        args.put( GEN_COL_USER_COMMENT_NAME, UserComment );
        args.put( REFUEL_COL_CAR_ID_NAME, carId );
        args.put( REFUEL_COL_DRIVER_ID_NAME, driverId );
        args.put( REFUEL_COL_EXPENSETYPE_ID_NAME, expenseTypeId );
        args.put( REFUEL_COL_INDEX_NAME, carIndex );
        args.put( REFUEL_COL_QUANTITY_NAME, qty );
        args.put( REFUEL_COL_PRICE_NAME, price );
        args.put( REFUEL_COL_CURRENCY_ID_NAME, currencyId );
        args.put( REFUEL_COL_DATE_NAME, date );
        args.put( REFUEL_COL_DOCUMENTNO_NAME, documentNo );
        return mDb.insert( REFUEL_TABLE_NAME, null, args );
    }

    /**
     * Delete the refuel with the given rowId
     *
     * @param rowId id of the refuel to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteRefuel( long rowId )
    {
        return mDb.delete( REFUEL_TABLE_NAME, GEN_COL_ROWID_NAME + "=" + rowId, null ) > 0;
    }

    /**
     * Check preconditions for delete the record
     * @param rowId the rowId of the record
     * @param mRes for error messages string
     * @return null if the record can be deleted, error message if not
     */
    public String canDelete( long rowId, Resources mRes )
    {
        return null;
    }

    /**
     * Update the refuel using the details provided. The refuel to be updated is
     * specified using the rowId, and it is altered to use the values passed in
     *
     * @param rowId id of the refuel to update
     * @param Name the name of the refuel
     * @param IsActive the refuel is active or not
     * @param UserComment user comment/help
     * @param Code the refuel code
     * @param Type the refuel type: L: length, V: volume
     * @return true if the driver was successfully updated, false otherwise
     */
    public boolean updateRefuel( long rowId, String Name, String IsActive, String UserComment,
            long carId, long driverId, long expenseTypeId, float carIndex, float qty, float price, long currencyId,
            long date, String documentNo )
    {
        ContentValues args = new ContentValues();
        args.put( GEN_COL_NAME_NAME, Name );
        args.put( GEN_COL_ISACTIVE_NAME, IsActive );
        args.put( GEN_COL_USER_COMMENT_NAME, UserComment );
        args.put( REFUEL_COL_CAR_ID_NAME, carId );
        args.put( REFUEL_COL_DRIVER_ID_NAME, driverId );
        args.put( REFUEL_COL_EXPENSETYPE_ID_NAME, expenseTypeId );
        args.put( REFUEL_COL_INDEX_NAME, carIndex );
        args.put( REFUEL_COL_QUANTITY_NAME, qty );
        args.put( REFUEL_COL_PRICE_NAME, price );
        args.put( REFUEL_COL_CURRENCY_ID_NAME, currencyId );
        args.put( REFUEL_COL_DATE_NAME, date );
        args.put( REFUEL_COL_DOCUMENTNO_NAME, documentNo );

        return mDb.update( REFUEL_TABLE_NAME, args, GEN_COL_ROWID_NAME + "=" + rowId, null ) > 0;
    }
}
