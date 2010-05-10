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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import org.andicar.utils.StaticValues;
import org.andicar.activity.R;
public class MainDbAdapter extends DB
{
    public MainDbAdapter( Context ctx )
    {
        super(ctx);
    }

    /**
     * Fetch a cursor containing all rows from the given table than match the given where condition.
     * The creturned cursor are NOT positioned anywhere. You most use the corsor movement methods in order to
     * read the containing rows.
     * @param tableName
     * @param columns
     * @param whereCondition
     * @param orderByColumn
     * @return
     */
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

    /**
     * Create a new record in the given table
     * @param tableName
     * @param content
     * @return (-1 * Error code) in case of error, the id of the record in case of success. For error codes see errors.xml
     */
    public long createRecord(String tableName, ContentValues content){
        long retVal = canCreate(tableName, content);
        if(retVal != -1)
            return -1 * retVal;
        
        long mCarId = -1;
        BigDecimal stopIndex = null;
        BigDecimal startIndex = null;
        try{
            if(tableName.equals(MILEAGE_TABLE_NAME)){
                mCarId = content.getAsLong(MILEAGE_COL_CAR_ID_NAME);
                stopIndex = new BigDecimal(content.getAsString(MILEAGE_COL_INDEXSTOP_NAME));
                startIndex = new BigDecimal(content.getAsString(MILEAGE_COL_INDEXSTART_NAME));
            }
            else if(tableName.equals(REFUEL_TABLE_NAME)){
                mCarId = content.getAsLong(REFUEL_COL_CAR_ID_NAME);
                stopIndex = new BigDecimal(content.getAsString(REFUEL_COL_INDEX_NAME));
            }
            else if(tableName.equals(EXPENSES_TABLE_NAME)){
                mCarId = content.getAsLong(EXPENSES_COL_CAR_ID_NAME);
                String newIndexStr = content.getAsString(EXPENSES_COL_INDEX_NAME);
                if(newIndexStr != null && newIndexStr.length() > 0)
                    stopIndex = new BigDecimal(newIndexStr);
            }

            if(mCarId != -1 && stopIndex != null){
               try{
                    mDb.beginTransaction();
                    retVal = mDb.insertOrThrow( tableName, null, content );
                    updateCarCurrentIndex(mCarId, stopIndex);
                    if(startIndex != null)
                        updateCarInitIndex(mCarId, startIndex);
                    else
                        updateCarInitIndex(mCarId, stopIndex);
                    mDb.setTransactionSuccessful();
                }catch(SQLException e){
                    lastErrorMessage = e.getMessage();
                    retVal = -1;
                }
                finally{
                    mDb.endTransaction();
                }
            }
            else{
                retVal = mDb.insertOrThrow(tableName, null, content);
            }
            if(retVal != -1 && tableName.equals(REFUEL_TABLE_NAME)){
                ContentValues expenseContent = null;
                expenseContent = new ContentValues();

                expenseContent.put(MainDbAdapter.GEN_COL_NAME_NAME, StaticValues.EXPENSES_COL_FROMREFUEL_TABLE_NAME);
                expenseContent.put(MainDbAdapter.GEN_COL_USER_COMMENT_NAME,
                        content.getAsString(MainDbAdapter.GEN_COL_USER_COMMENT_NAME));
                expenseContent.put(MainDbAdapter.EXPENSES_COL_CAR_ID_NAME,
                        content.getAsString(MainDbAdapter.REFUEL_COL_CAR_ID_NAME));
                expenseContent.put(MainDbAdapter.EXPENSES_COL_DRIVER_ID_NAME,
                        content.getAsString(MainDbAdapter.REFUEL_COL_DRIVER_ID_NAME));
                expenseContent.put(MainDbAdapter.EXPENSES_COL_EXPENSECATEGORY_ID_NAME,
                        content.getAsString(MainDbAdapter.REFUEL_COL_EXPENSECATEGORY_NAME));
                expenseContent.put(MainDbAdapter.EXPENSES_COL_EXPENSETYPE_ID_NAME,
                        content.getAsString(MainDbAdapter.REFUEL_COL_EXPENSETYPE_ID_NAME));
                expenseContent.put(MainDbAdapter.EXPENSES_COL_INDEX_NAME,
                        content.getAsString(MainDbAdapter.REFUEL_COL_INDEX_NAME));

                BigDecimal price = new BigDecimal(content.getAsString(MainDbAdapter.REFUEL_COL_PRICEENTERED_NAME));
                BigDecimal quantity = new BigDecimal(content.getAsString(MainDbAdapter.REFUEL_COL_QUANTITYENTERED_NAME));
                BigDecimal amt = (price.multiply(quantity)).setScale(StaticValues.DECIMALS_AMOUNT, StaticValues.ROUNDING_MODE_AMOUNT);
                expenseContent.put(MainDbAdapter.EXPENSES_COL_AMOUNTENTERED_NAME, amt.toString());
                expenseContent.put(MainDbAdapter.EXPENSES_COL_CURRENCYENTERED_ID_NAME,
                        content.getAsString(MainDbAdapter.REFUEL_COL_CURRENCYENTERED_ID_NAME));
                expenseContent.put(MainDbAdapter.EXPENSES_COL_CURRENCYRATE_NAME,
                        content.getAsString(MainDbAdapter.REFUEL_COL_CURRENCYRATE_NAME));

                BigDecimal convRate = new BigDecimal(content.getAsString(MainDbAdapter.REFUEL_COL_CURRENCYRATE_NAME));
                amt = (amt.multiply(convRate)).setScale(StaticValues.DECIMALS_AMOUNT, StaticValues.ROUNDING_MODE_AMOUNT);
                expenseContent.put(MainDbAdapter.EXPENSES_COL_AMOUNT_NAME, amt.toString());

                expenseContent.put(MainDbAdapter.EXPENSES_COL_CURRENCY_ID_NAME,
                        content.getAsString(MainDbAdapter.REFUEL_COL_CURRENCY_ID_NAME));
                expenseContent.put(MainDbAdapter.EXPENSES_COL_DATE_NAME,
                        content.getAsString(MainDbAdapter.REFUEL_COL_DATE_NAME));
                expenseContent.put(MainDbAdapter.EXPENSES_COL_DOCUMENTNO_NAME,
                        content.getAsString(MainDbAdapter.REFUEL_COL_DOCUMENTNO_NAME));
                expenseContent.put(MainDbAdapter.EXPENSES_COL_FROMTABLE_NAME, StaticValues.EXPENSES_COL_FROMREFUEL_TABLE_NAME);
                expenseContent.put(MainDbAdapter.EXPENSES_COL_FROMRECORD_ID_NAME, retVal);
                mDb.insertOrThrow( MainDbAdapter.EXPENSES_TABLE_NAME, null, expenseContent);
            }
        }
        catch(SQLException ex){
            lastErrorMessage = ex.getMessage();
            lasteException = ex;
            return -1;
        }
        return retVal;
    }

    /**
     * check create preconditions
     * @param tableName
     * @param content content of the record
     * @return -1 if the row can be inserted, an error code otherwise. For error codes see errors.xml
     */
    public int canCreate( String tableName, ContentValues content){

        String checkSql = "";
        Cursor checkCursor = null;
//        int retVal = -1;
        if(tableName.equals(CURRENCYRATE_TABLE_NAME)){
            long currencyFromId = content.getAsLong(CURRENCYRATE_COL_FROMCURRENCY_ID_NAME);
            long currencyToId = content.getAsLong(CURRENCYRATE_COL_TOCURRENCY_ID_NAME);
            if(currencyFromId == currencyToId)
                return R.string.ERR_032;

            //check duplicates
            checkSql = "SELECT * " +
                        "FROM " + CURRENCYRATE_TABLE_NAME + " " +
                        "WHERE (" +
                                    CURRENCYRATE_COL_FROMCURRENCY_ID_NAME + " =  " + currencyFromId +
                                    " AND " +
                                    CURRENCYRATE_COL_TOCURRENCY_ID_NAME + " =  " + currencyToId + " " +
                                ") " +
                                " OR " +
                                "(" +
                                    CURRENCYRATE_COL_TOCURRENCY_ID_NAME + " =  " + currencyFromId +
                                    " AND " +
                                    CURRENCYRATE_COL_FROMCURRENCY_ID_NAME + " =  " + currencyToId + " " +
                                ") " +
                        "LIMIT 1";
            checkCursor = mDb.rawQuery(checkSql, null);
            if(checkCursor.moveToFirst()){ //record exists
                checkCursor.close();
                return R.string.ERR_029;
            }
            if(!checkCursor.isClosed())
                checkCursor.close();
        }
        else if(tableName.equals(UOM_CONVERSION_TABLE_NAME)){
            if(content.getAsLong(UOM_CONVERSION_COL_UOMFROM_ID_NAME) ==
                    content.getAsLong(UOM_CONVERSION_COL_UOMTO_ID_NAME))
                return R.string.ERR_031;
        }
        return -1;
    }

    /**
     * update the record with recordId = rowId in the given table with the given content
     * @param tableName
     * @param rowId
     * @param content
     * @return -1 if the row updated, an error code otherwise. For error codes see errors.xml
     */
    public int updateRecord( String tableName, long rowId, ContentValues content)
    {
        int retVal = canUpdate(tableName, content, rowId);
        if(retVal != -1)
            return retVal;

        long mCarId = -1;
        BigDecimal stopIndex = null;
        BigDecimal startIndex = null;
        try{
            if(tableName.equals(MILEAGE_TABLE_NAME)){
                mCarId = content.getAsLong(MILEAGE_COL_CAR_ID_NAME);
                stopIndex = new BigDecimal(content.getAsString(MILEAGE_COL_INDEXSTOP_NAME));
                startIndex = new BigDecimal(content.getAsString(MILEAGE_COL_INDEXSTART_NAME));
            }
            else if(tableName.equals(REFUEL_TABLE_NAME)){
                mCarId = content.getAsLong(REFUEL_COL_CAR_ID_NAME);
                stopIndex = new BigDecimal(content.getAsString(REFUEL_COL_INDEX_NAME));
            }
            else if(tableName.equals(EXPENSES_TABLE_NAME)){
                mCarId = content.getAsLong(EXPENSES_COL_CAR_ID_NAME);
                String newIndexStr = content.getAsString(EXPENSES_COL_INDEX_NAME);
                if(newIndexStr != null && newIndexStr.length() > 0)
                    stopIndex = new BigDecimal(newIndexStr);
            }

            if(mCarId != -1 && stopIndex != null){
               try{
                    mDb.beginTransaction();
                    mDb.update( tableName, content, GEN_COL_ROWID_NAME + "=" + rowId, null);
                    if(tableName.equals(REFUEL_TABLE_NAME)){ //update the coresponding expense record
                        long expenseId = -1;
                        String expenseIdSelect =
                                "SELECT " + GEN_COL_ROWID_NAME + " " +
                                "FROM " + EXPENSES_TABLE_NAME + " " +
                                "WHERE " + EXPENSES_COL_FROMTABLE_NAME + " = 'Refuel' " +
                                    "AND " + EXPENSES_COL_FROMRECORD_ID_NAME + " = " + rowId;
                        Cursor c = execSelectSql(expenseIdSelect);
                        if(c.moveToFirst())
                            expenseId = c.getLong(0);
                        c.close();
                        if(expenseId != -1){
                            ContentValues expenseContent = null;
                            expenseContent = new ContentValues();

                            expenseContent.put(MainDbAdapter.GEN_COL_USER_COMMENT_NAME,
                                    content.getAsString(MainDbAdapter.GEN_COL_USER_COMMENT_NAME));
                            expenseContent.put(MainDbAdapter.EXPENSES_COL_CAR_ID_NAME,
                                    content.getAsString(MainDbAdapter.REFUEL_COL_CAR_ID_NAME));
                            expenseContent.put(MainDbAdapter.EXPENSES_COL_DRIVER_ID_NAME,
                                    content.getAsString(MainDbAdapter.REFUEL_COL_DRIVER_ID_NAME));
                            expenseContent.put(MainDbAdapter.EXPENSES_COL_EXPENSECATEGORY_ID_NAME,
                                    content.getAsString(MainDbAdapter.REFUEL_COL_EXPENSECATEGORY_NAME));
                            expenseContent.put(MainDbAdapter.EXPENSES_COL_EXPENSETYPE_ID_NAME,
                                    content.getAsString(MainDbAdapter.REFUEL_COL_EXPENSETYPE_ID_NAME));
                            expenseContent.put(MainDbAdapter.EXPENSES_COL_INDEX_NAME,
                                    content.getAsString(MainDbAdapter.REFUEL_COL_INDEX_NAME));

                            BigDecimal price = new BigDecimal(content.getAsString(MainDbAdapter.REFUEL_COL_PRICEENTERED_NAME));
                            BigDecimal quantity = new BigDecimal(content.getAsString(MainDbAdapter.REFUEL_COL_QUANTITYENTERED_NAME));
                            BigDecimal amt = (price.multiply(quantity)).setScale(StaticValues.DECIMALS_AMOUNT, StaticValues.ROUNDING_MODE_AMOUNT);
                            expenseContent.put(MainDbAdapter.EXPENSES_COL_AMOUNTENTERED_NAME, amt.toString());
                            expenseContent.put(MainDbAdapter.EXPENSES_COL_CURRENCYENTERED_ID_NAME,
                                    content.getAsString(MainDbAdapter.REFUEL_COL_CURRENCYENTERED_ID_NAME));
                            expenseContent.put(MainDbAdapter.EXPENSES_COL_CURRENCYRATE_NAME,
                                    content.getAsString(MainDbAdapter.REFUEL_COL_CURRENCYRATE_NAME));

                            BigDecimal convRate = new BigDecimal(content.getAsString(MainDbAdapter.REFUEL_COL_CURRENCYRATE_NAME));
                            amt = (amt.multiply(convRate)).setScale(StaticValues.DECIMALS_AMOUNT, StaticValues.ROUNDING_MODE_AMOUNT);
                            expenseContent.put(MainDbAdapter.EXPENSES_COL_AMOUNT_NAME, amt.toString());

                            expenseContent.put(MainDbAdapter.EXPENSES_COL_CURRENCY_ID_NAME,
                                    content.getAsString(MainDbAdapter.REFUEL_COL_CURRENCY_ID_NAME));
                            expenseContent.put(MainDbAdapter.EXPENSES_COL_DATE_NAME,
                                    content.getAsString(MainDbAdapter.REFUEL_COL_DATE_NAME));
                            expenseContent.put(MainDbAdapter.EXPENSES_COL_DOCUMENTNO_NAME,
                                    content.getAsString(MainDbAdapter.REFUEL_COL_DOCUMENTNO_NAME));
                            expenseContent.put(MainDbAdapter.EXPENSES_COL_FROMTABLE_NAME, "Refuel");
                            mDb.update( MainDbAdapter.EXPENSES_TABLE_NAME, expenseContent, GEN_COL_ROWID_NAME + "=" + expenseId, null );
                        }
                    }

                    updateCarCurrentIndex(mCarId, stopIndex);
                    if(startIndex != null)
                        updateCarInitIndex(mCarId, startIndex);
                    else
                        updateCarInitIndex(mCarId, stopIndex);
                    mDb.setTransactionSuccessful();
                }catch(SQLException e){
                    lastErrorMessage = e.getMessage();
                    retVal = 1;
                }
                finally{
                    mDb.endTransaction();
                }
            }
            else{
                mDb.update( tableName, content, GEN_COL_ROWID_NAME + "=" + rowId, null );
            }
        }
        catch(SQLException e){
            lastErrorMessage = e.getMessage();
            lasteException = e;
            return -1;
        }
        return retVal;
    }

    /**
     * Update the car curren index
     * @param content
     * @throws SQLException
     */
    private void updateCarCurrentIndex(long mCarId, BigDecimal newIndex) throws SQLException {
        //update car curent index
        BigDecimal carCurrentIndex = new BigDecimal(fetchRecord(CAR_TABLE_NAME, carTableColNames, mCarId)
                                                    .getString(CAR_COL_INDEXCURRENT_POS));
        ContentValues content = new ContentValues();
        if (newIndex.compareTo(carCurrentIndex) > 0) {
            content.put(CAR_COL_INDEXCURRENT_NAME, newIndex.toString());
            if (mDb.update(CAR_TABLE_NAME, content, GEN_COL_ROWID_NAME + "=" + mCarId, null) == 0) {
                throw new SQLException("Car Update error");
            }
        }
        else
            updateCarCurrentIndex(mCarId);
    }

    private void updateCarCurrentIndex(long mCarId) throws SQLException {
        BigDecimal newStopIndex = BigDecimal.ZERO;
        BigDecimal tmpStopIndex = BigDecimal.ZERO;
        Cursor c = null;

        String sql = "SELECT MAX(" + sqlConcatTableColumn(MILEAGE_TABLE_NAME, MILEAGE_COL_INDEXSTOP_NAME) + ") " +
                        " FROM " + MILEAGE_TABLE_NAME +
                        " WHERE " + sqlConcatTableColumn(MILEAGE_TABLE_NAME, MILEAGE_COL_CAR_ID_NAME) + " = " + mCarId +
                        " GROUP BY " + sqlConcatTableColumn(MILEAGE_TABLE_NAME, MILEAGE_COL_CAR_ID_NAME);
        c = execSelectSql(sql);
        if(c.moveToFirst())
            tmpStopIndex = new BigDecimal(c.getString(0));
        if(tmpStopIndex == null)
            tmpStopIndex = BigDecimal.ZERO;
        c.close();
        c = null;
        if(tmpStopIndex.compareTo(newStopIndex) > 0)
            newStopIndex = tmpStopIndex;

        sql = "SELECT MAX(" + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_INDEX_NAME) + ") " +
                        " FROM " + REFUEL_TABLE_NAME +
                        " WHERE " + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_CAR_ID_NAME) + " = " + mCarId +
                        " GROUP BY " + sqlConcatTableColumn(REFUEL_TABLE_NAME, REFUEL_COL_CAR_ID_NAME);
        c = execSelectSql(sql);
        if(c.moveToFirst())
            tmpStopIndex = new BigDecimal(c.getString(0));
        if(tmpStopIndex == null)
            tmpStopIndex = BigDecimal.ZERO;
        c.close();
        c = null;
        
        if(tmpStopIndex.compareTo(newStopIndex) > 0)
            newStopIndex = tmpStopIndex;

        sql = "SELECT MAX(" + sqlConcatTableColumn(EXPENSES_TABLE_NAME, EXPENSES_COL_INDEX_NAME) + ") " +
                        " FROM " + EXPENSES_TABLE_NAME +
                        " WHERE " + sqlConcatTableColumn(EXPENSES_TABLE_NAME, EXPENSES_COL_CAR_ID_NAME) + " = " + mCarId +
                        " GROUP BY " + sqlConcatTableColumn(EXPENSES_TABLE_NAME, EXPENSES_COL_CAR_ID_NAME);
        c = execSelectSql(sql);
        if(c.moveToFirst())
            tmpStopIndex = new BigDecimal(c.getString(0));
        if(tmpStopIndex == null)
            tmpStopIndex = BigDecimal.ZERO;
        c.close();
        c = null;

        if(tmpStopIndex.compareTo(newStopIndex) > 0)
            newStopIndex = tmpStopIndex;

        if(newStopIndex.equals(BigDecimal.ZERO))
            newStopIndex = new BigDecimal(fetchRecord(CAR_TABLE_NAME, carTableColNames, mCarId)
                                                    .getString(CAR_COL_INDEXSTART_POS));
        ContentValues content = new ContentValues();
        content.put(CAR_COL_INDEXCURRENT_NAME, newStopIndex.toString());
        if (mDb.update(CAR_TABLE_NAME, content, GEN_COL_ROWID_NAME + "=" + mCarId, null) == 0) {
            throw new SQLException("Car Update error");
        }
    }

    /**
     * Update the car init index
     * @param content
     * @throws SQLException
     */
    private void updateCarInitIndex(long mCarId, BigDecimal newIndex) throws SQLException {
        //update car curent index
        BigDecimal carInitIndex = new BigDecimal(fetchRecord(CAR_TABLE_NAME, carTableColNames, mCarId)
                                                    .getString(CAR_COL_INDEXSTART_POS));
        ContentValues content = new ContentValues();
        if (newIndex.compareTo(carInitIndex) < 0) {
            content.put(CAR_COL_INDEXSTART_NAME, newIndex.toString());
            if (mDb.update(CAR_TABLE_NAME, content, GEN_COL_ROWID_NAME + "=" + mCarId, null) == 0) {
                throw new SQLException("Car Update error");
            }
        }
    }

    /**
     * Delete the record with rowId from tableName
     * @param tableName
     * @param rowId
     * @return -1 if the row deleted, an error code otherwise. For error codes see errors.xml
     */
    public int deleteRecord( String tableName, long rowId )
    {
        int checkVal = canDelete(tableName, rowId);
        // 1 -> -1
        if(checkVal == -1){
            if(tableName.equals(MILEAGE_TABLE_NAME)){ // update the car curent index
                long carId = fetchRecord(MILEAGE_TABLE_NAME, mileageTableColNames, rowId)
                                .getLong(MILEAGE_COL_CAR_ID_POS);
                checkVal = (-1 * mDb.delete(tableName, GEN_COL_ROWID_NAME + "=" + rowId, null ));
                if(checkVal == -1)
                    updateCarCurrentIndex(carId);
            }
            else if(tableName.equals(REFUEL_TABLE_NAME)){
                long expenseId = -1;
                long carId = fetchRecord(REFUEL_TABLE_NAME, refuelTableColNames, rowId)
                                .getLong(REFUEL_COL_CAR_ID_POS);
                checkVal = (-1 * mDb.delete(tableName, GEN_COL_ROWID_NAME + "=" + rowId, null ));
                if(checkVal == -1){
                    String expenseIdSelect =
                            "SELECT " + GEN_COL_ROWID_NAME + " " +
                            "FROM " + EXPENSES_TABLE_NAME + " " +
                            "WHERE " + EXPENSES_COL_FROMTABLE_NAME + " = 'Refuel' " +
                                "AND " + EXPENSES_COL_FROMRECORD_ID_NAME + " = " + rowId;
                    Cursor c = execSelectSql(expenseIdSelect);
                    if(c.moveToFirst())
                        expenseId = c.getLong(0);
                    c.close();
                    if(expenseId != -1){
                        mDb.delete(EXPENSES_TABLE_NAME, GEN_COL_ROWID_NAME + "=" + expenseId, null);
                    }
                    updateCarCurrentIndex(carId);
                }
            }
            else if(tableName.equals(EXPENSES_TABLE_NAME)){
                long carId = fetchRecord(EXPENSES_TABLE_NAME, expensesTableColNames, rowId)
                                .getLong(EXPENSES_COL_CAR_ID_POS);
                checkVal = (-1 * mDb.delete(tableName, GEN_COL_ROWID_NAME + "=" + rowId, null ));
                if(checkVal == -1)
                    updateCarCurrentIndex(carId);
            }
            else
                checkVal = (-1 * mDb.delete(tableName, GEN_COL_ROWID_NAME + "=" + rowId, null ));
        }
        else
            return checkVal;

        return checkVal;
    }

    /**
     * check deletion preconditions (referencial integrity, etc.)
     * @param tableName
     * @param rowId
     * @return -1 if the row can be deleted, an error code otherwise. For error codes see errors.xml
     */
    public int canDelete( String tableName, long rowId ){

        String checkSql = "";
        Cursor checkCursor = null;
//        int retVal = -1;
        if(tableName.equals(DRIVER_TABLE_NAME)){
            //check if exists mileage for this driver
            checkSql = "SELECT * " +
                        "FROM " + MILEAGE_TABLE_NAME + " " +
                        "WHERE " + MILEAGE_COL_DRIVER_ID_NAME + " = " + rowId + " " +
                        "LIMIT 1";
            checkCursor = mDb.rawQuery(checkSql, null);
            if(checkCursor.moveToFirst()){ //record exists
                checkCursor.close();
                return R.string.ERR_009;
            }
            checkCursor.close();
            //check refuels
            checkSql = "SELECT * " +
                        "FROM " + REFUEL_TABLE_NAME + " " +
                        "WHERE " + REFUEL_COL_DRIVER_ID_NAME + " = " + rowId + " " +
                        "LIMIT 1";
            checkCursor = mDb.rawQuery(checkSql, null);
            if(checkCursor.moveToFirst()){ //record exists
                checkCursor.close();
                return R.string.ERR_010;
            }
            checkCursor.close();
        }
        else if(tableName.equals(CAR_TABLE_NAME)){
            //check if exists mileage for this driver
            checkSql = "SELECT * " +
                        "FROM " + MILEAGE_TABLE_NAME + " " +
                        "WHERE " + MILEAGE_COL_CAR_ID_NAME + " = " + rowId + " " +
                        "LIMIT 1";
            checkCursor = mDb.rawQuery(checkSql, null);
            if(checkCursor.moveToFirst()){ //record exists
                checkCursor.close();
                return R.string.ERR_011;
            }
            checkCursor.close();
            //check refuels
            checkSql = "SELECT * " +
                        "FROM " + REFUEL_TABLE_NAME + " " +
                        "WHERE " + REFUEL_COL_CAR_ID_NAME + " = " + rowId + " " +
                        "LIMIT 1";
            checkCursor = mDb.rawQuery(checkSql, null);
            if(checkCursor.moveToFirst()){ //record exists
                checkCursor.close();
                return R.string.ERR_012;
            }
            checkCursor.close();
        }
        else if(tableName.equals(UOM_TABLE_NAME)){
            //check if exists mileage for this driver
            checkSql = "SELECT * " +
                        "FROM " + MILEAGE_TABLE_NAME + " " +
                        "WHERE " + MILEAGE_COL_UOMLENGTH_ID_NAME + " = " + rowId + " " +
                        "LIMIT 1";
            checkCursor = mDb.rawQuery(checkSql, null);
            if(checkCursor.moveToFirst()){ //record exists
                checkCursor.close();
                return R.string.ERR_013;
            }
            checkCursor.close();
            //check refuels
            checkSql = "SELECT * " +
                        "FROM " + REFUEL_TABLE_NAME + " " +
                        "WHERE " + REFUEL_COL_UOMVOLUME_ID_NAME + " = " + rowId + " " +
                        "LIMIT 1";
            checkCursor = mDb.rawQuery(checkSql, null);
            if(checkCursor.moveToFirst()){ //record exists
                checkCursor.close();
                return R.string.ERR_014;
            }
            checkCursor.close();
            //check uom conversions
            checkSql = "SELECT * " +
                        "FROM " + UOM_CONVERSION_TABLE_NAME + " " +
                        "WHERE " + UOM_CONVERSION_COL_UOMFROM_ID_NAME + " = " + rowId + " " +
                                "OR " + UOM_CONVERSION_COL_UOMTO_ID_NAME + " = " + rowId + " " +
                        "LIMIT 1";
            checkCursor = mDb.rawQuery(checkSql, null);
            if(checkCursor.moveToFirst()){ //record exists
                checkCursor.close();
                return R.string.ERR_015;
            }
            checkCursor.close();
        }
        else if(tableName.equals(CURRENCY_TABLE_NAME)){
            //check cars
            checkSql = "SELECT * " +
                        "FROM " + CAR_TABLE_NAME + " " +
                        "WHERE " + CAR_COL_CURRENCY_ID_NAME + " = " + rowId + " " +
                        "LIMIT 1";
            checkCursor = mDb.rawQuery(checkSql, null);
            if(checkCursor.moveToFirst()){ //record exists
                checkCursor.close();
                return R.string.ERR_016;
            }
            checkCursor.close();
            //check refuels
            checkSql = "SELECT * " +
                        "FROM " + REFUEL_TABLE_NAME + " " +
                        "WHERE " + REFUEL_COL_CURRENCY_ID_NAME + " = " + rowId + " " +
                        "LIMIT 1";
            checkCursor = mDb.rawQuery(checkSql, null);
            if(checkCursor.moveToFirst()){ //record exists
                checkCursor.close();
                return R.string.ERR_017;
            }
            checkCursor.close();
        }
        else if(tableName.equals(EXPENSETYPE_TABLE_NAME)){
            //check if exists mileage for this driver
            checkSql = "SELECT * " +
                        "FROM " + MILEAGE_TABLE_NAME + " " +
                        "WHERE " + MILEAGE_COL_EXPENSETYPE_ID_NAME + " = " + rowId + " " +
                        "LIMIT 1";
            checkCursor = mDb.rawQuery(checkSql, null);
            if(checkCursor.moveToFirst()){ //record exists
                checkCursor.close();
                return R.string.ERR_018;
            }
            checkCursor.close();
            //check refuels
            checkSql = "SELECT * " +
                        "FROM " + REFUEL_TABLE_NAME + " " +
                        "WHERE " + REFUEL_COL_EXPENSETYPE_ID_NAME + " = " + rowId + " " +
                        "LIMIT 1";
            checkCursor = mDb.rawQuery(checkSql, null);
            if(checkCursor.moveToFirst()){ //record exists
                checkCursor.close();
                return R.string.ERR_019;
            }
            checkCursor.close();
        }
        else if(tableName.equals(EXPENSECATEGORY_TABLE_NAME)){
            //check refuels
            checkSql = "SELECT * " +
                        "FROM " + REFUEL_TABLE_NAME + " " +
                        "WHERE " + REFUEL_COL_EXPENSECATEGORY_NAME + " = " + rowId + " " +
                        "LIMIT 1";
            checkCursor = mDb.rawQuery(checkSql, null);
            if(checkCursor.moveToFirst()){ //record exists
                checkCursor.close();
                return R.string.ERR_027;
            }
            checkCursor.close();
            //check expenses
            checkSql = "SELECT * " +
                        "FROM " + EXPENSES_TABLE_NAME + " " +
                        "WHERE " + EXPENSES_COL_EXPENSECATEGORY_ID_NAME + " = " + rowId + " " +
                        "LIMIT 1";
            checkCursor = mDb.rawQuery(checkSql, null);
            if(checkCursor.moveToFirst()){ //record exists
                checkCursor.close();
                return R.string.ERR_028;
            }
            checkCursor.close();
        }
        else if(tableName.equals(EXPENSES_TABLE_NAME)){
            //check refuels
            checkSql = "SELECT * " +
                        "FROM " + REFUEL_TABLE_NAME + " " +
                        "WHERE " + GEN_COL_ROWID_NAME + " = " +
                                    "( SELECT " + EXPENSES_COL_FROMRECORD_ID_NAME + " " +
                                        "FROM " + EXPENSES_TABLE_NAME + " " +
                                        "WHERE " + GEN_COL_ROWID_NAME + " = " + rowId + " " +
                                                " AND " + EXPENSES_COL_FROMTABLE_NAME + " = '" +
                                                        StaticValues.EXPENSES_COL_FROMREFUEL_TABLE_NAME + "' ) " +
                        "LIMIT 1";
            checkCursor = mDb.rawQuery(checkSql, null);
            if(checkCursor.moveToFirst()){ //record exists
                checkCursor.close();
                return R.string.ERR_030;
            }
            checkCursor.close();
        }

        return -1;
    }

    /**
     * check update preconditions 
     * @param tableName
     * @param rowId
     * @return -1 if the row can be deleted, an error code otherwise. For error codes see errors.xml
     */
    public int canUpdate( String tableName, ContentValues content, long rowId ){

        String checkSql = "";
        Cursor checkCursor = null;
//        int retVal = -1;
        if(tableName.equals(UOM_TABLE_NAME)){
            if(content.containsKey(MainDbAdapter.GEN_COL_ISACTIVE_NAME)
                    && content.getAsString(MainDbAdapter.GEN_COL_ISACTIVE_NAME).equals("N"))
            {
                //check if the uom are used in an active car definition
                checkSql = "SELECT * " +
                            "FROM " + CAR_TABLE_NAME + " " +
                            "WHERE " + GEN_COL_ISACTIVE_NAME + " = 'Y' " +
                                    "AND (" + CAR_COL_UOMLENGTH_ID_NAME + " = " + rowId + " OR " +
                                            CAR_COL_UOMVOLUME_ID_NAME + " = " + rowId + ") " +
                            "LIMIT 1";
                checkCursor = mDb.rawQuery(checkSql, null);
                if(checkCursor.moveToFirst()){ //record exists
                    checkCursor.close();
                    return R.string.ERR_025;
                }
                if(!checkCursor.isClosed())
                    checkCursor.close();
            }
        }
        else if(tableName.equals(CURRENCY_TABLE_NAME)){
            if(content.containsKey(MainDbAdapter.GEN_COL_ISACTIVE_NAME)
                    && content.getAsString(MainDbAdapter.GEN_COL_ISACTIVE_NAME).equals("N"))
            {
                //check if the currency are used in an active car definition
                checkSql = "SELECT * " +
                            "FROM " + CAR_TABLE_NAME + " " +
                            "WHERE " + GEN_COL_ISACTIVE_NAME + " = 'Y' " +
                                    "AND " + CAR_COL_CURRENCY_ID_NAME + " = " + rowId + " " +
                            "LIMIT 1";
                checkCursor = mDb.rawQuery(checkSql, null);
                if(checkCursor.moveToFirst()){ //record exists
                    checkCursor.close();
                    return R.string.ERR_026;
                }
                if(!checkCursor.isClosed())
                    checkCursor.close();
            }
        }
        return -1;
    }

    /**
     * check some preconditions for inserting/updating UOM conversions in order to prevent duplicates
     * @param rowId
     * @param fromId
     * @param toId
     * @return -1 if uom conversion can be added/updated. An error code otherwise. For error codes see errors.xml
     */
    public int canInsertUpdateUOMConversion( Long rowId, long fromId, long toId )
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
            return R.string.ERR_005;
        }

        return -1;
    }

    /**
     * check some preconditions for inserting/updating the car start/stop index
     * @param rowId
     * @param carId
     * @param startIndex
     * @param stopIndex
     * @return -1 if index OK. An error code otherwise. For error codes see errors.xml
     */
    public int checkIndex(long rowId,long carId, BigDecimal startIndex, BigDecimal stopIndex){

        if(stopIndex.compareTo(startIndex) <= 0)
            return R.string.ERR_004;

        String checkSql = "";
        checkSql = "SELECT * " +
                    " FROM " + MILEAGE_TABLE_NAME +
                    " WHERE " + MILEAGE_COL_CAR_ID_NAME + "=" + carId +
                            " AND " + MILEAGE_COL_INDEXSTART_NAME + " <= " + startIndex.toString() +
                                " AND " + MILEAGE_COL_INDEXSTOP_NAME + " > " + startIndex.toString();
        if (rowId >= 0)
            checkSql = checkSql + " AND " + GEN_COL_ROWID_NAME + "<>" + rowId;

        if(mDb.rawQuery(checkSql, null).getCount() > 0)
            return R.string.ERR_001;
        checkSql = "SELECT * " +
                    " FROM " + MILEAGE_TABLE_NAME +
                    " WHERE " + MILEAGE_COL_CAR_ID_NAME + "=" + carId +
                            " AND " + MILEAGE_COL_INDEXSTART_NAME + " < " + stopIndex.toString() +
                                " AND " + MILEAGE_COL_INDEXSTOP_NAME + " >= " + stopIndex.toString();
        if (rowId >= 0)
            checkSql = checkSql + " AND " + GEN_COL_ROWID_NAME + "<>" + rowId;
        if(mDb.rawQuery(checkSql, null).getCount() > 0)
            return R.string.ERR_002;

        checkSql = "SELECT * " +
                    " FROM " + MILEAGE_TABLE_NAME +
                    " WHERE " + MILEAGE_COL_CAR_ID_NAME + "=" + carId +
                            " AND " + MILEAGE_COL_INDEXSTART_NAME + " >= " + startIndex.toString() +
                                " AND " + MILEAGE_COL_INDEXSTOP_NAME + " <= " + stopIndex.toString();
        if (rowId >= 0)
            checkSql = checkSql + " AND " + GEN_COL_ROWID_NAME + "<>" + rowId;

        if(mDb.rawQuery(checkSql, null).getCount() > 0)
            return R.string.ERR_003;

        return -1;
    }

    /**
     * Get a list of the recent user comments used in the fromTable
     * @param carId
     * @param driverId
     * @param limitCount limit the size of the returned comments
     * @return a string array containig the last limitCount user comment from fromTable for the carId and DriverId
     */
    public String[] getAutoCompleteUserComments(String fromTable, long carId /*, long driverId*/, int limitCount){
        String[] retVal = null;
        ArrayList<String> commentList = new ArrayList<String>();
        String selectSql;
        if(fromTable.equals(MILEAGE_TABLE_NAME))
            selectSql = "SELECT DISTINCT " + GEN_COL_USER_COMMENT_NAME +
                            " FROM " + MILEAGE_TABLE_NAME +
                            " WHERE " + MILEAGE_COL_CAR_ID_NAME + " = " + carId +
                                " AND " + GEN_COL_USER_COMMENT_NAME + " IS NOT NULL " +
                                " AND " + GEN_COL_USER_COMMENT_NAME + " <> '' " +
                            " ORDER BY " + MILEAGE_COL_INDEXSTOP_NAME + " DESC " +
                            " LIMIT " + limitCount;
        else if(fromTable.equals(REFUEL_TABLE_NAME))
            selectSql = "SELECT DISTINCT " + GEN_COL_USER_COMMENT_NAME +
                            " FROM " + REFUEL_TABLE_NAME +
                            " WHERE " + REFUEL_COL_CAR_ID_NAME + " = " + carId +
                                " AND " + GEN_COL_USER_COMMENT_NAME + " IS NOT NULL " +
                                " AND " + GEN_COL_USER_COMMENT_NAME + " <> '' " +
                            " ORDER BY " + REFUEL_COL_DATE_NAME + " DESC " +
                            " LIMIT " + limitCount;
        else if(fromTable.equals(EXPENSES_TABLE_NAME))
            selectSql = "SELECT DISTINCT " + GEN_COL_USER_COMMENT_NAME +
                            " FROM " + EXPENSES_TABLE_NAME +
                            " WHERE " + EXPENSES_COL_CAR_ID_NAME + " = " + carId +
                                " AND " + GEN_COL_USER_COMMENT_NAME + " IS NOT NULL " +
                                " AND " + GEN_COL_USER_COMMENT_NAME + " <> '' " +
                            " ORDER BY " + EXPENSES_COL_DATE_NAME + " DESC " +
                            " LIMIT " + limitCount;
        else if(fromTable.equals(GPSTRACK_TABLE_NAME))
            selectSql = "SELECT DISTINCT " + GEN_COL_USER_COMMENT_NAME +
                            " FROM " + GPSTRACK_TABLE_NAME +
                            " WHERE " + GPSTRACK_COL_CAR_ID_NAME + " = " + carId +
                                " AND " + GEN_COL_USER_COMMENT_NAME + " IS NOT NULL " +
                                " AND " + GEN_COL_USER_COMMENT_NAME + " <> '' " +
                            " ORDER BY " + GPSTRACK_COL_DATE_NAME + " DESC " +
                            " LIMIT " + limitCount;
        else
            return null;
        Cursor commentCursor = mDb.rawQuery(selectSql, null);
        while(commentCursor.moveToNext()){
            commentList.add(commentCursor.getString(0));
        }
        commentCursor.close();
        retVal = new String[commentList.size()];
        commentList.toArray(retVal);
        return retVal;
    }

    public Cursor execSelectSql(String selectSql){
        return mDb.rawQuery(selectSql, null);
    }

    public void execSql(String sql){
        mDb.execSQL(sql);
    }
    public BigDecimal getCurrencyRate(long fromCurrencyId, long toCurrencyId){
        BigDecimal retVal = null;
        String retValStr = null;
        if(fromCurrencyId == toCurrencyId)
            return BigDecimal.ONE;
        String selectSql = "";
        Cursor selectCursor;

        selectSql = " SELECT * " +
                    " FROM " + CURRENCYRATE_TABLE_NAME +
                    " WHERE " + GEN_COL_ISACTIVE_NAME + "='Y' " +
                        " AND " + CURRENCYRATE_COL_FROMCURRENCY_ID_NAME + " = " + fromCurrencyId +
                        " AND " + CURRENCYRATE_COL_TOCURRENCY_ID_NAME + " = " + toCurrencyId;
        selectCursor = execSelectSql(selectSql);
        if(selectCursor.moveToFirst())
            retValStr = selectCursor.getString(CURRENCYRATE_COL_RATE_POS);
        selectCursor.close();
        if(retValStr != null && retValStr.length() > 0)
            return new BigDecimal(retValStr);

        selectSql = " SELECT * " +
                    " FROM " + CURRENCYRATE_TABLE_NAME +
                    " WHERE " + GEN_COL_ISACTIVE_NAME + "='Y' " +
                        " AND " + CURRENCYRATE_COL_TOCURRENCY_ID_NAME + " = " + fromCurrencyId +
                        " AND " + CURRENCYRATE_COL_FROMCURRENCY_ID_NAME + " = " + toCurrencyId;
        selectCursor = execSelectSql(selectSql);
        if(selectCursor.moveToFirst())
            retValStr = selectCursor.getString(CURRENCYRATE_COL_RATE_POS);
        selectCursor.close();
        if(retValStr != null && retValStr.length() > 0){
            retVal = new BigDecimal(retValStr);
            if(retVal.compareTo(BigDecimal.ZERO) != 0)
                return BigDecimal.ONE.divide(retVal, 10, RoundingMode.HALF_UP)
                        .setScale(StaticValues.DECIMALS_CONVERSIONS, StaticValues.ROUNDING_MODE_CONVERSIONS);
            else
                return BigDecimal.ZERO;
        }

        return retVal;
    }

    public BigDecimal getUOMConvertionRate(long fromId, long toId){
        BigDecimal retVal = null;
        String retValStr = null;
        if(fromId == toId)
            return BigDecimal.ONE;
        String selectSql = "";
        Cursor selectCursor;

        selectSql = " SELECT * " +
                    " FROM " + UOM_CONVERSION_TABLE_NAME +
                    " WHERE " + GEN_COL_ISACTIVE_NAME + "='Y' " +
                        " AND " + UOM_CONVERSION_COL_UOMFROM_ID_NAME + " = " + fromId +
                        " AND " + UOM_CONVERSION_COL_UOMTO_ID_NAME + " = " + toId;
        selectCursor = execSelectSql(selectSql);
        if(selectCursor.moveToFirst())
            retValStr = selectCursor.getString(UOM_CONVERSION_COL_RATE_POS);
        selectCursor.close();

        if(retValStr != null && retValStr.length() > 0)
            return new BigDecimal(retValStr);

        return retVal;
    }

//    public BigDecimal getConvertedDistance(long fromId, long toId, BigDecimal distance){
//        return distance.multiply(getUOMConvertionRate(fromId, toId)).setScale(StaticValues.DECIMALS_CONVERSIONS, StaticValues.ROUNDING_MODE_CONVERSIONS);
//    }

    public long getCarUOMVolumeID(long carID){
        return fetchRecord(CAR_TABLE_NAME, carTableColNames, carID)
                .getLong(CAR_COL_UOMVOLUME_ID_POS);
    }

    public long getCarUOMLengthID(long carID){
        return fetchRecord(CAR_TABLE_NAME, carTableColNames, carID)
                .getLong(CAR_COL_UOMLENGTH_ID_POS);
    }

    public long getCarCurrencyID(long carID){
        return fetchRecord(CAR_TABLE_NAME, carTableColNames, carID)
                .getLong(CAR_COL_CURRENCY_ID_POS);
    }

    public String getCurrencyCode(long currencyID){
        return fetchRecord(MainDbAdapter.CURRENCY_TABLE_NAME, MainDbAdapter.currencyTableColNames, currencyID)
                .getString(MainDbAdapter.CURRENCY_COL_CODE_POS);    
    }

    public String getUOMCode(long uomID){
        return fetchRecord(MainDbAdapter.UOM_TABLE_NAME, MainDbAdapter.uomTableColNames,
                    uomID).getString(MainDbAdapter.UOM_COL_CODE_POS);
    }

    public long getIdFromCode(String tableName, String code){
        Cursor c = fetchForTable(tableName, genColRowId, "Code = '" + code + "'", "Code");
        long retVal = -1;
        if(c.moveToFirst())
            retVal = c.getLong(0);
        c.close();
        return retVal;
    }
}
