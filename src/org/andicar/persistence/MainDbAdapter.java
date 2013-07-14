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
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Locale;

import org.andicar.utils.StaticValues;
import org.andicar2.activity.R;
import org.andicar.utils.AndiCarStatistics;

public class MainDbAdapter extends DB
{
    SharedPreferences mPref;
    boolean isSendCrashReport = true;
    private Context mCtx;

    public MainDbAdapter( Context ctx )
    {
        super(ctx);
        mCtx = ctx;
        mPref = ctx.getSharedPreferences(StaticValues.GLOBAL_PREFERENCE_NAME, Context.MODE_MULTI_PROCESS);
        isSendCrashReport = mPref.getBoolean("SendCrashReport", true);
    }

    /**
     * Return a Cursor positioned at the record that matches the given rowId from the given table
     *
     * @param rowId id of the record to retrieve
     * @return Cursor positioned to matching record, if found. Otherwise null.
     * @throws SQLException if the record could not be found/retrieved
     */
    public Cursor fetchRecord( String tableName, String[] columns, long rowId ) throws SQLException
    {
        Cursor mCursor =
                mDb.query( true, tableName, columns,
                            COL_NAME_GEN_ROWID + "=" + rowId, null, null, null, null, null );
        if( mCursor != null ){
        	if(mCursor.moveToFirst())
        		return mCursor;
        	else{
        		mCursor.close();
        		return null;
        	}
        }
        else
            return null;

    }

    public int getVersion(){
        return mDb.getVersion();
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
            if(tableName.equals(TABLE_NAME_MILEAGE)){
                mCarId = content.getAsLong(COL_NAME_MILEAGE__CAR_ID);
                stopIndex = new BigDecimal(content.getAsString(COL_NAME_MILEAGE__INDEXSTOP));
                startIndex = new BigDecimal(content.getAsString(COL_NAME_MILEAGE__INDEXSTART));
            }
            else if(tableName.equals(TABLE_NAME_REFUEL)){
                mCarId = content.getAsLong(COL_NAME_REFUEL__CAR_ID);
                stopIndex = new BigDecimal(content.getAsString(COL_NAME_REFUEL__INDEX));
            }
            else if(tableName.equals(TABLE_NAME_EXPENSE)){
                mCarId = content.getAsLong(COL_NAME_EXPENSE__CAR_ID);
                String newIndexStr = content.getAsString(COL_NAME_EXPENSE__INDEX);
                if(newIndexStr != null && newIndexStr.length() > 0)
                    stopIndex = new BigDecimal(newIndexStr);
            }

            if(mCarId != -1 && stopIndex != null){ //update the car current index
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
                }catch(NumberFormatException e){
                    lastErrorMessage = e.getMessage();
                    retVal = -1;
                }
                finally{
                	if(mDb.inTransaction()) //issue no: 84 (https://code.google.com/p/andicar/issues/detail?id=84) 
                		mDb.endTransaction();
                }
            }
            else{
                retVal = mDb.insertOrThrow(tableName, null, content);
            }
            if(retVal != -1 && tableName.equals(TABLE_NAME_REFUEL)){ //create expense
                ContentValues expenseContent = null;
                expenseContent = new ContentValues();

                expenseContent.put(MainDbAdapter.COL_NAME_GEN_NAME, StaticValues.EXPENSES_COL_FROMREFUEL_TABLE_NAME);
                expenseContent.put(MainDbAdapter.COL_NAME_GEN_USER_COMMENT,
                        content.getAsString(MainDbAdapter.COL_NAME_GEN_USER_COMMENT));
                expenseContent.put(MainDbAdapter.COL_NAME_EXPENSE__CAR_ID,
                        content.getAsString(MainDbAdapter.COL_NAME_REFUEL__CAR_ID));
                expenseContent.put(MainDbAdapter.COL_NAME_EXPENSE__DRIVER_ID,
                        content.getAsString(MainDbAdapter.COL_NAME_REFUEL__DRIVER_ID));
                expenseContent.put(MainDbAdapter.COL_NAME_EXPENSE__EXPENSECATEGORY_ID,
                        content.getAsString(MainDbAdapter.COL_NAME_REFUEL__EXPENSECATEGORY_ID));
                expenseContent.put(MainDbAdapter.COL_NAME_EXPENSE__EXPENSETYPE_ID,
                        content.getAsString(MainDbAdapter.COL_NAME_REFUEL__EXPENSETYPE_ID));
                expenseContent.put(MainDbAdapter.COL_NAME_EXPENSE__INDEX,
                        content.getAsString(MainDbAdapter.COL_NAME_REFUEL__INDEX));

                BigDecimal price = new BigDecimal(content.getAsString(MainDbAdapter.COL_NAME_REFUEL__PRICEENTERED));
                BigDecimal quantity = new BigDecimal(content.getAsString(MainDbAdapter.COL_NAME_REFUEL__QUANTITYENTERED));
                BigDecimal amt = (price.multiply(quantity)).setScale(StaticValues.DECIMALS_AMOUNT, StaticValues.ROUNDING_MODE_AMOUNT);
                expenseContent.put(MainDbAdapter.COL_NAME_EXPENSE__AMOUNTENTERED, amt.toString());
                expenseContent.put(MainDbAdapter.COL_NAME_EXPENSE__CURRENCYENTERED_ID,
                        content.getAsString(MainDbAdapter.COL_NAME_REFUEL__CURRENCYENTERED_ID));
                expenseContent.put(MainDbAdapter.COL_NAME_EXPENSE__CURRENCYRATE,
                        content.getAsString(MainDbAdapter.COL_NAME_REFUEL__CURRENCYRATE));

                BigDecimal convRate = new BigDecimal(content.getAsString(MainDbAdapter.COL_NAME_REFUEL__CURRENCYRATE));
                amt = (amt.multiply(convRate)).setScale(StaticValues.DECIMALS_AMOUNT, StaticValues.ROUNDING_MODE_AMOUNT);
                expenseContent.put(MainDbAdapter.COL_NAME_EXPENSE__AMOUNT, amt.toString());

                expenseContent.put(MainDbAdapter.COL_NAME_EXPENSE__CURRENCY_ID,
                        content.getAsString(MainDbAdapter.COL_NAME_REFUEL__CURRENCY_ID));
                expenseContent.put(MainDbAdapter.COL_NAME_EXPENSE__DATE,
                        content.getAsString(MainDbAdapter.COL_NAME_REFUEL__DATE));
                expenseContent.put(MainDbAdapter.COL_NAME_EXPENSE__DOCUMENTNO,
                        content.getAsString(MainDbAdapter.COL_NAME_REFUEL__DOCUMENTNO));
                expenseContent.put(MainDbAdapter.COL_NAME_EXPENSE__FROMTABLE, StaticValues.EXPENSES_COL_FROMREFUEL_TABLE_NAME);
                expenseContent.put(MainDbAdapter.COL_NAME_EXPENSE__FROMRECORD_ID, retVal);
                expenseContent.put(MainDbAdapter.COL_NAME_EXPENSE__BPARTNER_ID,
                        content.getAsString(MainDbAdapter.COL_NAME_REFUEL__BPARTNER_ID));
                expenseContent.put(MainDbAdapter.COL_NAME_EXPENSE__BPARTNER_LOCATION_ID,
                        content.getAsString(MainDbAdapter.COL_NAME_REFUEL__BPARTNER_LOCATION_ID));
                expenseContent.put(MainDbAdapter.COL_NAME_EXPENSE__TAG_ID,
                        content.getAsString(MainDbAdapter.COL_NAME_REFUEL__TAG_ID));
                mDb.insertOrThrow( MainDbAdapter.TABLE_NAME_EXPENSE, null, expenseContent);
            }
        }
        catch(SQLException ex){
            lastErrorMessage = ex.getMessage();
            lasteException = ex;
            return -1;
        }
        catch(NumberFormatException ex){
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
        if(tableName.equals(TABLE_NAME_CURRENCYRATE)){
            long currencyFromId = content.getAsLong(COL_NAME_CURRENCYRATE__FROMCURRENCY_ID);
            long currencyToId = content.getAsLong(COL_NAME_CURRENCYRATE__TOCURRENCY_ID);
            if(currencyFromId == currencyToId)
                return R.string.ERR_032;

            //check duplicates
            checkSql = "SELECT * " +
                        "FROM " + TABLE_NAME_CURRENCYRATE + " " +
                        "WHERE (" +
                                    COL_NAME_CURRENCYRATE__FROMCURRENCY_ID + " =  " + currencyFromId +
                                    " AND " +
                                    COL_NAME_CURRENCYRATE__TOCURRENCY_ID + " =  " + currencyToId + " " +
                                ") " +
                                " OR " +
                                "(" +
                                    COL_NAME_CURRENCYRATE__TOCURRENCY_ID + " =  " + currencyFromId +
                                    " AND " +
                                    COL_NAME_CURRENCYRATE__FROMCURRENCY_ID + " =  " + currencyToId + " " +
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
        else if(tableName.equals(TABLE_NAME_UOMCONVERSION)){
            if(content.getAsLong(COL_NAME_UOMCONVERSION__UOMFROM_ID) ==
                    content.getAsLong(COL_NAME_UOMCONVERSION__UOMTO_ID))
                return R.string.ERR_031;
        }
        else if(tableName.equals(TABLE_NAME_CURRENCY)){
            String checkSelect =
                "SELECT " + COL_NAME_GEN_ROWID + " " +
                "FROM " + TABLE_NAME_CURRENCY + " " +
                "WHERE UPPER( " + COL_NAME_CURRENCY__CODE + ") = ? ";
	        String[] selectionArgs = {content.getAsString(COL_NAME_CURRENCY__CODE).toUpperCase(Locale.US)};
	        Cursor c = execSelectSql(checkSelect, selectionArgs);
	        if(c.moveToFirst()){ //duplicate currency code
	            c.close();
	            return R.string.ERR_059;
            }
	        c.close();
        }
        else if(tableName.equals(TABLE_NAME_TASK_CAR)){
            String checkSelect =
                "SELECT " + COL_NAME_GEN_ROWID + " " +
                "FROM " + TABLE_NAME_TASK_CAR + " " +
                "WHERE " + COL_NAME_TASK_CAR__CAR_ID + " = ? " +
                			" AND " + COL_NAME_TASK_CAR__TASK_ID + " = ? ";
	        String[] selectionArgs = {content.getAsString(COL_NAME_TASK_CAR__CAR_ID), 
	        							content.getAsString(COL_NAME_TASK_CAR__TASK_ID)};
	        Cursor c = execSelectSql(checkSelect, selectionArgs);
	        if(c.moveToFirst()){ //duplicate record
	            c.close();
	            return R.string.ERR_059;
            }
	        c.close();
        }
        else if(tableName.equals(TABLE_NAME_UOM)){
            String checkSelect =
                "SELECT " + COL_NAME_GEN_ROWID + " " +
                "FROM " + TABLE_NAME_UOM + " " +
                "WHERE UPPER( " + COL_NAME_UOM__CODE + ") = ? ";
	        String[] selectionArgs = {content.getAsString(COL_NAME_UOM__CODE).toUpperCase(Locale.US)};
	        Cursor c = execSelectSql(checkSelect, selectionArgs);
	        if(c.moveToFirst()){ //duplicate currency code
	            c.close();
	            return R.string.ERR_059;
            }
	        c.close();
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
    public int updateRecord(String tableName, long rowId, ContentValues content)
    {
        int retVal = canUpdate(tableName, content, rowId);
        if(retVal != -1)
            return retVal;

        long mCarId = -1;
        BigDecimal stopIndex = null;
        BigDecimal startIndex = null;
        try{
            if(tableName.equals(TABLE_NAME_MILEAGE)){
                mCarId = content.getAsLong(COL_NAME_MILEAGE__CAR_ID);
                stopIndex = new BigDecimal(content.getAsString(COL_NAME_MILEAGE__INDEXSTOP));
                startIndex = new BigDecimal(content.getAsString(COL_NAME_MILEAGE__INDEXSTART));
            }
            else if(tableName.equals(TABLE_NAME_REFUEL)){
                mCarId = content.getAsLong(COL_NAME_REFUEL__CAR_ID);
                stopIndex = new BigDecimal(content.getAsString(COL_NAME_REFUEL__INDEX));
            }
            else if(tableName.equals(TABLE_NAME_EXPENSE)){
                mCarId = content.getAsLong(COL_NAME_EXPENSE__CAR_ID);
                String newIndexStr = content.getAsString(COL_NAME_EXPENSE__INDEX);
                if(newIndexStr != null && newIndexStr.length() > 0)
                    stopIndex = new BigDecimal(newIndexStr);
            }
            else if(tableName.equals(TABLE_NAME_CAR)){ //inactivate/activate the related task-car links/todos
                String[] whereArgs = {Long.toString(rowId)};
                ContentValues isActiveContent = new ContentValues();
                isActiveContent.put(COL_NAME_GEN_ISACTIVE, content.getAsString(COL_NAME_GEN_ISACTIVE));
                mDb.update(TABLE_NAME_TASK_CAR, isActiveContent, COL_NAME_TASK_CAR__CAR_ID + " = ?", whereArgs);
                mDb.update(TABLE_NAME_TODO, isActiveContent, COL_NAME_TODO__CAR_ID + " = ?", whereArgs);
                if(content.getAsString(COL_NAME_GEN_ISACTIVE).equals("N")){
	                if(!AddOnDBAdapter.recordUpdated(mDb, TABLE_NAME_CAR, rowId, content))
	                	return R.string.ERR_063;
                }
                
            }

            if(mCarId != -1 && stopIndex != null){
               try{
                    mDb.beginTransaction();
                    mDb.update( tableName, content, COL_NAME_GEN_ROWID + "=" + rowId, null);
                    if(tableName.equals(TABLE_NAME_REFUEL)){ //update the coresponding expense record
                        long expenseId = -1;
                        String expenseIdSelect =
                                "SELECT " + COL_NAME_GEN_ROWID + " " +
                                "FROM " + TABLE_NAME_EXPENSE + " " +
                                "WHERE " + COL_NAME_EXPENSE__FROMTABLE + " = 'Refuel' " +
                                    "AND " + COL_NAME_EXPENSE__FROMRECORD_ID + " = ?";
                        String[] selectionArgs = {Long.toString(rowId)};
                        Cursor c = execSelectSql(expenseIdSelect, selectionArgs);
                        if(c.moveToFirst())
                            expenseId = c.getLong(0);
                        c.close();
                        if(expenseId != -1){
                            ContentValues expenseContent = null;
                            expenseContent = new ContentValues();

                            expenseContent.put(MainDbAdapter.COL_NAME_GEN_USER_COMMENT,
                                    content.getAsString(MainDbAdapter.COL_NAME_GEN_USER_COMMENT));
                            expenseContent.put(MainDbAdapter.COL_NAME_EXPENSE__CAR_ID,
                                    content.getAsString(MainDbAdapter.COL_NAME_REFUEL__CAR_ID));
                            expenseContent.put(MainDbAdapter.COL_NAME_EXPENSE__DRIVER_ID,
                                    content.getAsString(MainDbAdapter.COL_NAME_REFUEL__DRIVER_ID));
                            expenseContent.put(MainDbAdapter.COL_NAME_EXPENSE__EXPENSECATEGORY_ID,
                                    content.getAsString(MainDbAdapter.COL_NAME_REFUEL__EXPENSECATEGORY_ID));
                            expenseContent.put(MainDbAdapter.COL_NAME_EXPENSE__EXPENSETYPE_ID,
                                    content.getAsString(MainDbAdapter.COL_NAME_REFUEL__EXPENSETYPE_ID));
                            expenseContent.put(MainDbAdapter.COL_NAME_EXPENSE__INDEX,
                                    content.getAsString(MainDbAdapter.COL_NAME_REFUEL__INDEX));

                            BigDecimal price = new BigDecimal(content.getAsString(MainDbAdapter.COL_NAME_REFUEL__PRICEENTERED));
                            BigDecimal quantity = new BigDecimal(content.getAsString(MainDbAdapter.COL_NAME_REFUEL__QUANTITYENTERED));
                            BigDecimal amt = (price.multiply(quantity)).setScale(StaticValues.DECIMALS_AMOUNT, StaticValues.ROUNDING_MODE_AMOUNT);
                            expenseContent.put(MainDbAdapter.COL_NAME_EXPENSE__AMOUNTENTERED, amt.toString());
                            expenseContent.put(MainDbAdapter.COL_NAME_EXPENSE__CURRENCYENTERED_ID,
                                    content.getAsString(MainDbAdapter.COL_NAME_REFUEL__CURRENCYENTERED_ID));
                            expenseContent.put(MainDbAdapter.COL_NAME_EXPENSE__CURRENCYRATE,
                                    content.getAsString(MainDbAdapter.COL_NAME_REFUEL__CURRENCYRATE));

                            BigDecimal convRate = new BigDecimal(content.getAsString(MainDbAdapter.COL_NAME_REFUEL__CURRENCYRATE));
                            amt = (amt.multiply(convRate)).setScale(StaticValues.DECIMALS_AMOUNT, StaticValues.ROUNDING_MODE_AMOUNT);
                            expenseContent.put(MainDbAdapter.COL_NAME_EXPENSE__AMOUNT, amt.toString());

                            expenseContent.put(MainDbAdapter.COL_NAME_EXPENSE__CURRENCY_ID,
                                    content.getAsString(MainDbAdapter.COL_NAME_REFUEL__CURRENCY_ID));
                            expenseContent.put(MainDbAdapter.COL_NAME_EXPENSE__DATE,
                                    content.getAsString(MainDbAdapter.COL_NAME_REFUEL__DATE));
                            expenseContent.put(MainDbAdapter.COL_NAME_EXPENSE__DOCUMENTNO,
                                    content.getAsString(MainDbAdapter.COL_NAME_REFUEL__DOCUMENTNO));
                            expenseContent.put(MainDbAdapter.COL_NAME_EXPENSE__FROMTABLE, "Refuel");
                            expenseContent.put(MainDbAdapter.COL_NAME_EXPENSE__BPARTNER_ID,
                                    content.getAsString(MainDbAdapter.COL_NAME_REFUEL__BPARTNER_ID));
                            expenseContent.put(MainDbAdapter.COL_NAME_EXPENSE__BPARTNER_LOCATION_ID,
                                    content.getAsString(MainDbAdapter.COL_NAME_REFUEL__BPARTNER_LOCATION_ID));
                            expenseContent.put(MainDbAdapter.COL_NAME_EXPENSE__TAG_ID,
                                    content.getAsString(MainDbAdapter.COL_NAME_REFUEL__TAG_ID));
                            mDb.update( MainDbAdapter.TABLE_NAME_EXPENSE, expenseContent, COL_NAME_GEN_ROWID + "=" + expenseId, null );
                        }
                    }

                    updateCarCurrentIndex(mCarId, stopIndex);
                    if(startIndex != null)
                        updateCarInitIndex(mCarId, startIndex);
                    else
                        updateCarInitIndex(mCarId, stopIndex);
                    mDb.setTransactionSuccessful();
                }
                catch(SQLException e){
                    lastErrorMessage = e.getMessage();
                    retVal = R.string.ERR_000;
                }
                catch(NumberFormatException e){
                    lastErrorMessage = e.getMessage();
                    retVal = R.string.ERR_000;
                }
                finally{
                	if(mDb.inTransaction()) //issue no: 84 (https://code.google.com/p/andicar/issues/detail?id=84) 
                		mDb.endTransaction();
                }
            }
            else{
                mDb.update( tableName, content, COL_NAME_GEN_ROWID + "=" + rowId, null );
            }
        }
        catch(SQLException e){
            lastErrorMessage = e.getMessage();
            lasteException = e;
            retVal = R.string.ERR_000;
        }
        catch(NumberFormatException e){
            lastErrorMessage = e.getMessage();
            lasteException = e;
            retVal = R.string.ERR_000;
        }
        return retVal;
    }

    /**
     * Update the car curren index
     * @param content
     * @throws SQLException
     */
    private void updateCarCurrentIndex(long mCarId, BigDecimal newIndex) throws SQLException, NumberFormatException {
        //update car curent index
    	Cursor c = fetchRecord(TABLE_NAME_CAR, COL_LIST_CAR_TABLE, mCarId);
        BigDecimal carCurrentIndex = new BigDecimal(c.getString(COL_POS_CAR__INDEXCURRENT));
        c.close();
        ContentValues content = new ContentValues();
        if (newIndex.compareTo(carCurrentIndex) > 0) {
            content.put(COL_NAME_CAR__INDEXCURRENT, newIndex.toString());
            if (mDb.update(TABLE_NAME_CAR, content, COL_NAME_GEN_ROWID + "=" + mCarId, null) == 0) {
                throw new SQLException("Car Update error");
            }
        }
        else
            updateCarCurrentIndex(mCarId);
    }

    private void updateCarCurrentIndex(long mCarId) throws SQLException, NumberFormatException {
        BigDecimal newStopIndex = BigDecimal.ZERO;
        BigDecimal tmpStopIndex = BigDecimal.ZERO;
        String tmpStr;
        Cursor c = null;

        String sql = "SELECT MAX(" + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__INDEXSTOP) + ") " +
                        " FROM " + TABLE_NAME_MILEAGE +
                        " WHERE " + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__CAR_ID) + " = ? " +
                        " GROUP BY " + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__CAR_ID);
        String[] selectionArgs = {Long.toString(mCarId)};
        c = execSelectSql(sql, selectionArgs);
        if(c.moveToFirst()){
            try{
                tmpStr = c.getString(0);
                if(tmpStr != null && tmpStr.length() > 0)
                    tmpStopIndex = new BigDecimal(tmpStr);
            }catch(NumberFormatException e){
                tmpStopIndex = null;
                if(isSendCrashReport)
                    AndiCarStatistics.sendFlurryError(mCtx, "DB Error - updateCarCurrentIndex", "NFE1: c.getString(0) = " + c.getString(0), this.toString());
            }
        }
        if(tmpStopIndex == null)
            tmpStopIndex = BigDecimal.ZERO;
        c.close();
        c = null;
        if(tmpStopIndex.compareTo(newStopIndex) > 0)
            newStopIndex = tmpStopIndex;

        sql = "SELECT MAX(" + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__INDEX) + ") " +
                        " FROM " + TABLE_NAME_REFUEL +
                        " WHERE " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__CAR_ID) + " = ? " +
                        " GROUP BY " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__CAR_ID);

        selectionArgs[0] = Long.toString(mCarId);
        c = execSelectSql(sql, selectionArgs);
        if(c.moveToFirst()){
            try{
                tmpStr = c.getString(0);
                if(tmpStr != null && tmpStr.length() > 0)
                    tmpStopIndex = new BigDecimal(tmpStr);
            }catch(NumberFormatException e){
                tmpStopIndex = null;
                if(isSendCrashReport)
                    AndiCarStatistics.sendFlurryError(mCtx, "DB Error - updateCarCurrentIndex", "NFE2: c.getString(0) = " + c.getString(0), this.toString());
            }
        }
        if(tmpStopIndex == null)
            tmpStopIndex = BigDecimal.ZERO;
        c.close();
        c = null;
        
        if(tmpStopIndex.compareTo(newStopIndex) > 0)
            newStopIndex = tmpStopIndex;

        sql = "SELECT MAX(" + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__INDEX) + ") " +
                        " FROM " + TABLE_NAME_EXPENSE +
                        " WHERE " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__CAR_ID) + " = ? " +
                                    " AND " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__INDEX) + " IS NOT NULL " +
                        " GROUP BY " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__CAR_ID);
        selectionArgs[0] = Long.toString(mCarId);
        c = execSelectSql(sql, selectionArgs);

        if(c.moveToFirst()){
            try{
                tmpStr = c.getString(0);
                if(tmpStr != null && tmpStr.length() > 0)
                    tmpStopIndex = new BigDecimal(tmpStr);
            }catch(NumberFormatException e){
                tmpStopIndex = null;
                if(isSendCrashReport)
                    AndiCarStatistics.sendFlurryError(mCtx, "DB Error - updateCarCurrentIndex", "NFE3: c.getString(0) = " + c.getString(0), this.toString());
            }
        }
        if(tmpStopIndex == null)
            tmpStopIndex = BigDecimal.ZERO;
        c.close();
        c = null;

        if(tmpStopIndex.compareTo(newStopIndex) > 0)
            newStopIndex = tmpStopIndex;

        if(newStopIndex.signum() == 0){
        	c = fetchRecord(TABLE_NAME_CAR, COL_LIST_CAR_TABLE, mCarId);
            newStopIndex = new BigDecimal(c.getString(COL_POS_CAR__INDEXSTART));
            c.close();
        }
        ContentValues content = new ContentValues();
        content.put(COL_NAME_CAR__INDEXCURRENT, newStopIndex.toString());
        if (mDb.update(TABLE_NAME_CAR, content, COL_NAME_GEN_ROWID + "=" + mCarId, null) == 0) {
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
    	Cursor c = fetchRecord(TABLE_NAME_CAR, COL_LIST_CAR_TABLE, mCarId);
        BigDecimal carInitIndex = new BigDecimal(c.getString(COL_POS_CAR__INDEXSTART));
        c.close();
        
        ContentValues content = new ContentValues();
        if (newIndex.compareTo(carInitIndex) < 0) {
            content.put(COL_NAME_CAR__INDEXSTART, newIndex.toString());
            if (mDb.update(TABLE_NAME_CAR, content, COL_NAME_GEN_ROWID + "=" + mCarId, null) == 0) {
                throw new SQLException("Car Update error");
            }
        }
    }

    /**
     * 
     * @param tableName
     * @param selection
     * @param selectionArgs
     * @param newContent
     * @return TODO return values on error (eg. record cannot be deleted)
     */
    public int updateRecords( String tableName, String selection, String[] selectionArgs, ContentValues newContent)
    {
//    	int checkVal;
    	String checkSql =
    		"SELECT * " +
    		" FROM " + tableName;
    	if(selection != null)
    		checkSql = checkSql +
    				" WHERE " + selection;
    	Cursor c = query(checkSql, selectionArgs);
    	while(c.moveToNext()){
    		updateRecord(tableName, c.getLong(COL_POS_GEN_ROWID), newContent);
    	}
    	c.close();
    	return 1;
    }

    /**
     * 
     * @param tableName
     * @param selection
     * @param selectionArgs
     * @return TODO return values on error (eg. record cannot be deleted)
     */
    public int deleteRecords( String tableName, String selection, String[] selectionArgs)
    {
//    	int checkVal;
    	String checkSql =
    		"SELECT * " +
    		" FROM " + tableName;
    	if(selection != null)
    		checkSql = checkSql +
    				" WHERE " + selection;
    	Cursor c = query(checkSql, selectionArgs);
    	while(c.moveToNext()){
    		deleteRecord(tableName, c.getLong(COL_POS_GEN_ROWID));
    	}
    	c.close();
    	return 1;
    }

    /**
     * Delete the record with rowId from tableName
     * @param tableName
     * @param rowId
     * @return -1 if the row deleted, an error code otherwise. For error codes see errors.xml
     */
    public int deleteRecord( String tableName, long rowId )
    {
        int checkVal;
        try{
        	Cursor c = null;
            checkVal = canDelete(tableName, rowId);
            // 1 -> -1
            if(checkVal == -1){
                if(tableName.equals(TABLE_NAME_MILEAGE)){ // update the car curent index
                	c = fetchRecord(TABLE_NAME_MILEAGE, COL_LIST_MILEAGE_TABLE, rowId);
                    long carId = c.getLong(COL_POS_MILEAGE__CAR_ID);
                    c.close();
                    checkVal = (-1 * mDb.delete(tableName, COL_NAME_GEN_ROWID + "=" + rowId, null ));
                    if(checkVal == -1)
                        updateCarCurrentIndex(carId);
                    //set null in gpstrack table col. mileage id
                    String selection = COL_NAME_GPSTRACK__MILEAGE_ID + "= ?";
                    String[] selectionArgs = {Long.toString(rowId)};
                    c = query(TABLE_NAME_GPSTRACK, COL_LIST_GPSTRACK_TABLE, selection, selectionArgs, null, null, null);
//                            fetchForTable(GPSTRACK_TABLE_NAME, gpsTrackTableColNames, GPSTRACK_COL_MILEAGE_ID_NAME + "=" + rowId, null);
                    ContentValues cv = new ContentValues();
                    cv.put(COL_NAME_GPSTRACK__MILEAGE_ID, (Long)null);
                    while(c.moveToNext()){
                        updateRecord(TABLE_NAME_GPSTRACK, c.getLong(COL_POS_GEN_ROWID), cv);
                    }
                    c.close();
                }
                else if(tableName.equals(TABLE_NAME_REFUEL)){
                    long expenseId = -1;
                    c = fetchRecord(TABLE_NAME_REFUEL, COL_LIST_REFUEL_TABLE, rowId);
                    long carId = c.getLong(COL_POS_REFUEL__CAR_ID);
                    c.close();
                    checkVal = (-1 * mDb.delete(tableName, COL_NAME_GEN_ROWID + "=" + rowId, null ));
                    if(checkVal == -1){
                        String expenseIdSelect =
                                "SELECT " + COL_NAME_GEN_ROWID + " " +
                                "FROM " + TABLE_NAME_EXPENSE + " " +
                                "WHERE " + COL_NAME_EXPENSE__FROMTABLE + " = 'Refuel' " +
                                    "AND " + COL_NAME_EXPENSE__FROMRECORD_ID + " = ? ";
                        String[] selectionArgs = {Long.toString(rowId)};
                        c = execSelectSql(expenseIdSelect, selectionArgs);
                        if(c.moveToFirst())
                            expenseId = c.getLong(0);
                        c.close();
                        if(expenseId != -1){
                            mDb.delete(TABLE_NAME_EXPENSE, COL_NAME_GEN_ROWID + "=" + expenseId, null);
                        }
                        updateCarCurrentIndex(carId);
                    }
                }
                else if(tableName.equals(TABLE_NAME_EXPENSE)){
                	c = fetchRecord(TABLE_NAME_EXPENSE, COL_LIST_EXPENSE_TABLE, rowId);
                    long carId = c.getLong(COL_POS_EXPENSE__CAR_ID);
                    c.close();
                    checkVal = (-1 * mDb.delete(tableName, COL_NAME_GEN_ROWID + "=" + rowId, null ));
                    if(checkVal == -1)
                        updateCarCurrentIndex(carId);
                }
                else if(tableName.equals(TABLE_NAME_GPSTRACK)){
                    //delete gps trtack details
                    String fileName = "";
                    String selection = COL_NAME_GPSTRACKDETAIL__GPSTRACK_ID + "= ?";
                    String[] selectionArgs = {Long.toString(rowId)};
                    c = query(TABLE_NAME_GPSTRACKDETAIL, COL_LIST_GPSTRACKDETAIL_TABLE, selection, selectionArgs, null, null, null);
//                    Cursor c = fetchForTable(GPSTRACKDETAIL_TABLE_NAME, gpsTrackDetailTableColNames,
//                            GPSTRACKDETAIL_COL_GPSTRACK_ID_NAME + "=" + rowId, null);
                    while(c.moveToNext()){
                        //delete track files
                        fileName = c.getString(COL_POS_GPSTRACKDETAIL__FILE);
                        if(fileName != null)
                            FileUtils.deleteFile(fileName);
                        //delete from gpstrack detail
                        deleteRecord(TABLE_NAME_GPSTRACKDETAIL, c.getInt(COL_POS_GEN_ROWID));
                    }
                    c.close();
                    checkVal = (-1 * mDb.delete(tableName, COL_NAME_GEN_ROWID + "=" + rowId, null ));
                }
                else if(tableName.equals(TABLE_NAME_CURRENCY)){
                    long currRateId = -1;
                    checkVal = (-1 * mDb.delete(tableName, COL_NAME_GEN_ROWID + "=" + rowId, null ));
                    if(checkVal == -1){
                        String currencyRateSelect =
                                "SELECT " + COL_NAME_GEN_ROWID + " " +
                                "FROM " + TABLE_NAME_CURRENCYRATE + " " +
                                "WHERE " + COL_NAME_CURRENCYRATE__FROMCURRENCY_ID + " = ? " +
                                    " OR " + COL_NAME_CURRENCYRATE__TOCURRENCY_ID + " = ? ";
                        String[] selectionArgs = {Long.toString(rowId), Long.toString(rowId)};
                        c = execSelectSql(currencyRateSelect, selectionArgs);
                        while(c.moveToNext()){
                            currRateId = c.getLong(0);
                            mDb.delete(TABLE_NAME_CURRENCYRATE, COL_NAME_GEN_ROWID + "=" + currRateId, null);
                        }
                        c.close();
                    }
                }
                else if(tableName.equals(TABLE_NAME_BPARTNER)){
                    //also delete the locations
                    mDb.delete(TABLE_NAME_BPARTNERLOCATION, COL_NAME_BPARTNERLOCATION__BPARTNER_ID + "=" + rowId, null);
                    checkVal = (-1 * mDb.delete(tableName, COL_NAME_GEN_ROWID + "=" + rowId, null ));
                }
                else if(tableName.equals(TABLE_NAME_CAR)){
                    //also delete the locations
                    mDb.delete(TABLE_NAME_TASK_CAR, COL_NAME_TASK_CAR__CAR_ID + "=" + rowId, null);
                    mDb.delete(TABLE_NAME_TODO, COL_NAME_TODO__CAR_ID + "=" + rowId, null);
                    mDb.delete(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_REIMBURSEMENT_CAR_RATES__CAR_ID + "=" + rowId, null);
                    if(!AddOnDBAdapter.recordDeleted(mDb, TABLE_NAME_CAR, rowId))
                    	return R.string.ERR_063;
                    
                    checkVal = (-1 * mDb.delete(tableName, COL_NAME_GEN_ROWID + "=" + rowId, null ));
                }
                else if(tableName.equals(TABLE_NAME_TASK)){
                    //also delete the locations
                    mDb.delete(TABLE_NAME_TASK_CAR, COL_NAME_TASK_CAR__TASK_ID + "=" + rowId, null);
                    mDb.delete(TABLE_NAME_TODO, COL_NAME_TODO__TASK_ID + "=" + rowId, null);
                    checkVal = (-1 * mDb.delete(tableName, COL_NAME_GEN_ROWID + "=" + rowId, null ));
                }
                else if(tableName.equals(TABLE_NAME_EXPENSETYPE)){
                    mDb.delete(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_REIMBURSEMENT_CAR_RATES__EXPENSETYPE_ID + "=" + rowId, null);
                    if(!AddOnDBAdapter.recordDeleted(mDb, TABLE_NAME_CAR, rowId))
                    	return R.string.ERR_063;
                    
                    checkVal = (-1 * mDb.delete(tableName, COL_NAME_GEN_ROWID + "=" + rowId, null ));
                }
                else
                    checkVal = (-1 * mDb.delete(tableName, COL_NAME_GEN_ROWID + "=" + rowId, null ));
            }
        }
        catch(SQLException e){
            lastErrorMessage = e.getMessage();
            lasteException = e;
            checkVal = R.string.ERR_000;
        }
        catch(NumberFormatException e){
            lastErrorMessage = e.getMessage();
            lasteException = e;
            checkVal = R.string.ERR_000;
        }
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
        if(tableName.equals(TABLE_NAME_DRIVER)){
            //check if exists mileage for this driver
            checkSql = "SELECT * " +
                        "FROM " + TABLE_NAME_MILEAGE + " " +
                        "WHERE " + COL_NAME_MILEAGE__DRIVER_ID + " = " + rowId + " " +
                        "LIMIT 1";
            checkCursor = mDb.rawQuery(checkSql, null);
            if(checkCursor.moveToFirst()){ //record exists
                checkCursor.close();
                return R.string.ERR_009;
            }
            checkCursor.close();
            //check refuels
            checkSql = "SELECT * " +
                        "FROM " + TABLE_NAME_REFUEL + " " +
                        "WHERE " + COL_NAME_REFUEL__DRIVER_ID + " = " + rowId + " " +
                        "LIMIT 1";
            checkCursor = mDb.rawQuery(checkSql, null);
            if(checkCursor.moveToFirst()){ //record exists
                checkCursor.close();
                return R.string.ERR_010;
            }
            checkCursor.close();
            //check expenses
            checkSql = "SELECT * " +
                        "FROM " + TABLE_NAME_EXPENSE + " " +
                        "WHERE " + COL_NAME_EXPENSE__DRIVER_ID + " = " + rowId + " " +
                        "LIMIT 1";
            checkCursor = mDb.rawQuery(checkSql, null);
            if(checkCursor.moveToFirst()){ //record exists
                checkCursor.close();
                return R.string.ERR_039;
            }
            checkCursor.close();
        }
        else if(tableName.equals(TABLE_NAME_CAR)){
            //check if exists mileage for this driver
            checkSql = "SELECT * " +
                        "FROM " + TABLE_NAME_MILEAGE + " " +
                        "WHERE " + COL_NAME_MILEAGE__CAR_ID + " = " + rowId + " " +
                        "LIMIT 1";
            checkCursor = mDb.rawQuery(checkSql, null);
            if(checkCursor.moveToFirst()){ //record exists
                checkCursor.close();
                return R.string.ERR_011;
            }
            checkCursor.close();
            //check refuels
            checkSql = "SELECT * " +
                        "FROM " + TABLE_NAME_REFUEL + " " +
                        "WHERE " + COL_NAME_REFUEL__CAR_ID + " = " + rowId + " " +
                        "LIMIT 1";
            checkCursor = mDb.rawQuery(checkSql, null);
            if(checkCursor.moveToFirst()){ //record exists
                checkCursor.close();
                return R.string.ERR_012;
            }
            checkCursor.close();
            //check expenses
            checkSql = "SELECT * " +
                        "FROM " + TABLE_NAME_EXPENSE + " " +
                        "WHERE " + COL_NAME_EXPENSE__CAR_ID + " = " + rowId + " " +
                        "LIMIT 1";
            checkCursor = mDb.rawQuery(checkSql, null);
            if(checkCursor.moveToFirst()){ //record exists
                checkCursor.close();
                return R.string.ERR_040;
            }
            checkCursor.close();
        }
        else if(tableName.equals(TABLE_NAME_UOM)){
            //check if exists mileage for this driver
            checkSql = "SELECT * " +
                        "FROM " + TABLE_NAME_MILEAGE + " " +
                        "WHERE " + COL_NAME_MILEAGE__UOMLENGTH_ID + " = " + rowId + " " +
                        "LIMIT 1";
            checkCursor = mDb.rawQuery(checkSql, null);
            if(checkCursor.moveToFirst()){ //record exists
                checkCursor.close();
                return R.string.ERR_013;
            }
            checkCursor.close();
            //check refuels
            checkSql = "SELECT * " +
                        "FROM " + TABLE_NAME_REFUEL + " " +
                        "WHERE " + COL_NAME_REFUEL__UOMVOLUME_ID + " = " + rowId + " " +
                            " OR " + COL_NAME_REFUEL__UOMVOLUMEENTERED_ID + " = " + rowId + " " +
                        "LIMIT 1";
            checkCursor = mDb.rawQuery(checkSql, null);
            if(checkCursor.moveToFirst()){ //record exists
                checkCursor.close();
                return R.string.ERR_014;
            }
            checkCursor.close();
            //check uom conversions
            checkSql = "SELECT * " +
                        "FROM " + TABLE_NAME_UOMCONVERSION + " " +
                        "WHERE " + COL_NAME_UOMCONVERSION__UOMFROM_ID + " = " + rowId + " " +
                                "OR " + COL_NAME_UOMCONVERSION__UOMTO_ID + " = " + rowId + " " +
                        "LIMIT 1";
            checkCursor = mDb.rawQuery(checkSql, null);
            if(checkCursor.moveToFirst()){ //record exists
                checkCursor.close();
                return R.string.ERR_015;
            }
            checkCursor.close();
        }
        else if(tableName.equals(TABLE_NAME_CURRENCY)){
            //check cars
            checkSql = "SELECT * " +
                        "FROM " + TABLE_NAME_CAR + " " +
                        "WHERE " + COL_NAME_CAR__CURRENCY_ID + " = " + rowId + " " +
                        "LIMIT 1";
            checkCursor = mDb.rawQuery(checkSql, null);
            if(checkCursor.moveToFirst()){ //record exists
                checkCursor.close();
                return R.string.ERR_016;
            }
            checkCursor.close();
            //check refuels
            checkSql = "SELECT * " +
                        "FROM " + TABLE_NAME_REFUEL + " " +
                        "WHERE " + COL_NAME_REFUEL__CURRENCY_ID + " = " + rowId + " " +
                            " OR " + COL_NAME_REFUEL__CURRENCYENTERED_ID + " = " + rowId + " " +
                        "LIMIT 1";
            checkCursor = mDb.rawQuery(checkSql, null);
            if(checkCursor.moveToFirst()){ //record exists
                checkCursor.close();
                return R.string.ERR_017;
            }
            checkCursor.close();
            //check expenses
            checkSql = "SELECT * " +
                        "FROM " + TABLE_NAME_EXPENSE + " " +
                        "WHERE " + COL_NAME_EXPENSE__CURRENCY_ID + " = " + rowId + " " +
                            " OR " + COL_NAME_EXPENSE__CURRENCYENTERED_ID + " = " + rowId + " " +
                        "LIMIT 1";
            checkCursor = mDb.rawQuery(checkSql, null);
            if(checkCursor.moveToFirst()){ //record exists
                checkCursor.close();
                return R.string.ERR_038;
            }
            checkCursor.close();
        }
        else if(tableName.equals(TABLE_NAME_EXPENSETYPE)){
            //check if exists mileage for this driver
            checkSql = "SELECT * " +
                        "FROM " + TABLE_NAME_MILEAGE + " " +
                        "WHERE " + COL_NAME_MILEAGE__EXPENSETYPE_ID + " = " + rowId + " " +
                        "LIMIT 1";
            checkCursor = mDb.rawQuery(checkSql, null);
            if(checkCursor.moveToFirst()){ //record exists
                checkCursor.close();
                return R.string.ERR_018;
            }
            checkCursor.close();
            //check refuels
            checkSql = "SELECT * " +
                        "FROM " + TABLE_NAME_REFUEL + " " +
                        "WHERE " + COL_NAME_REFUEL__EXPENSETYPE_ID + " = " + rowId + " " +
                        "LIMIT 1";
            checkCursor = mDb.rawQuery(checkSql, null);
            if(checkCursor.moveToFirst()){ //record exists
                checkCursor.close();
                return R.string.ERR_019;
            }
            checkCursor.close();
            //check expenses
            checkSql = "SELECT * " +
                        "FROM " + TABLE_NAME_EXPENSE + " " +
                        "WHERE " + COL_NAME_EXPENSE__EXPENSETYPE_ID + " = " + rowId + " " +
                        "LIMIT 1";
            checkCursor = mDb.rawQuery(checkSql, null);
            if(checkCursor.moveToFirst()){ //record exists
                checkCursor.close();
                return R.string.ERR_041;
            }
            checkCursor.close();
        }
        else if(tableName.equals(TABLE_NAME_EXPENSECATEGORY)){
            //check refuels
            checkSql = "SELECT * " +
                        "FROM " + TABLE_NAME_REFUEL + " " +
                        "WHERE " + COL_NAME_REFUEL__EXPENSECATEGORY_ID + " = " + rowId + " " +
                        "LIMIT 1";
            checkCursor = mDb.rawQuery(checkSql, null);
            if(checkCursor.moveToFirst()){ //record exists
                checkCursor.close();
                return R.string.ERR_027;
            }
            checkCursor.close();
            //check expenses
            checkSql = "SELECT * " +
                        "FROM " + TABLE_NAME_EXPENSE + " " +
                        "WHERE " + COL_NAME_EXPENSE__EXPENSECATEGORY_ID + " = " + rowId + " " +
                        "LIMIT 1";
            checkCursor = mDb.rawQuery(checkSql, null);
            if(checkCursor.moveToFirst()){ //record exists
                checkCursor.close();
                return R.string.ERR_028;
            }
            checkCursor.close();
        }
        else if(tableName.equals(TABLE_NAME_EXPENSE)){
            //check refuels
            checkSql = "SELECT * " +
                        "FROM " + TABLE_NAME_REFUEL + " " +
                        "WHERE " + COL_NAME_GEN_ROWID + " = " +
                                    "( SELECT " + COL_NAME_EXPENSE__FROMRECORD_ID + " " +
                                        "FROM " + TABLE_NAME_EXPENSE + " " +
                                        "WHERE " + COL_NAME_GEN_ROWID + " = " + rowId + " " +
                                                " AND " + COL_NAME_EXPENSE__FROMTABLE + " = '" +
                                                        StaticValues.EXPENSES_COL_FROMREFUEL_TABLE_NAME + "' ) " +
                        "LIMIT 1";
            checkCursor = mDb.rawQuery(checkSql, null);
            if(checkCursor.moveToFirst()){ //record exists
                checkCursor.close();
                return R.string.ERR_030;
            }
            checkCursor.close();
        }
        else if(tableName.equals(TABLE_NAME_BPARTNER)){
            //check refuels
            checkSql = "SELECT * " +
                        "FROM " + TABLE_NAME_REFUEL + " " +
                        "WHERE " + COL_NAME_REFUEL__BPARTNER_ID + " = " + rowId + " " +
                        "LIMIT 1";
            checkCursor = mDb.rawQuery(checkSql, null);
            if(checkCursor.moveToFirst()){ //record exists
                checkCursor.close();
                return R.string.ERR_042;
            }
            checkCursor.close();
            //check expenses
            checkSql = "SELECT * " +
                        "FROM " + TABLE_NAME_EXPENSE + " " +
                        "WHERE " + COL_NAME_EXPENSE__BPARTNER_ID + " = " + rowId + " " +
                        "LIMIT 1";
            checkCursor = mDb.rawQuery(checkSql, null);
            if(checkCursor.moveToFirst()){ //record exists
                checkCursor.close();
                return R.string.ERR_043;
            }
            checkCursor.close();
        }
        else if(tableName.equals(TABLE_NAME_BPARTNERLOCATION)){
            //check refuels
            checkSql = "SELECT * " +
                        "FROM " + TABLE_NAME_REFUEL + " " +
                        "WHERE " + COL_NAME_REFUEL__BPARTNER_LOCATION_ID + " = " + rowId + " " +
                        "LIMIT 1";
            checkCursor = mDb.rawQuery(checkSql, null);
            if(checkCursor.moveToFirst()){ //record exists
                checkCursor.close();
                return R.string.ERR_044;
            }
            checkCursor.close();
            //check expenses
            checkSql = "SELECT * " +
                        "FROM " + TABLE_NAME_EXPENSE + " " +
                        "WHERE " + COL_NAME_EXPENSE__BPARTNER_LOCATION_ID + " = " + rowId + " " +
                        "LIMIT 1";
            checkCursor = mDb.rawQuery(checkSql, null);
            if(checkCursor.moveToFirst()){ //record exists
                checkCursor.close();
                return R.string.ERR_045;
            }
            checkCursor.close();
        }
        else if(tableName.equals(TABLE_NAME_TAG)){
            checkSql = "SELECT * " +
                        "FROM " + TABLE_NAME_MILEAGE + " " +
                        "WHERE " + COL_NAME_MILEAGE__TAG_ID + " = " + rowId + " " +
                        "LIMIT 1";
            checkCursor = mDb.rawQuery(checkSql, null);
            if(checkCursor.moveToFirst()){ //record exists
                checkCursor.close();
                return R.string.ERR_046;
            }
            checkCursor.close();
            
            checkSql = "SELECT * " +
			            "FROM " + TABLE_NAME_REFUEL + " " +
			            "WHERE " + COL_NAME_REFUEL__TAG_ID + " = " + rowId + " " +
			            "LIMIT 1";
			checkCursor = mDb.rawQuery(checkSql, null);
			if(checkCursor.moveToFirst()){ //record exists
			    checkCursor.close();
			    return R.string.ERR_047;
			}
			checkCursor.close();

            checkSql = "SELECT * " +
                        "FROM " + TABLE_NAME_EXPENSE + " " +
                        "WHERE " + COL_NAME_EXPENSE__TAG_ID + " = " + rowId + " " +
                        "LIMIT 1";
            checkCursor = mDb.rawQuery(checkSql, null);
            if(checkCursor.moveToFirst()){ //record exists
                checkCursor.close();
                return R.string.ERR_048;
            }
            checkCursor.close();

            checkSql = "SELECT * " +
			            "FROM " + TABLE_NAME_GPSTRACK + " " +
			            "WHERE " + COL_NAME_GPSTRACK__TAG_ID + " = " + rowId + " " +
			            "LIMIT 1";
			checkCursor = mDb.rawQuery(checkSql, null);
			if(checkCursor.moveToFirst()){ //record exists
			    checkCursor.close();
			    return R.string.ERR_049;
			}
			checkCursor.close();
        }
        else if(tableName.equals(TABLE_NAME_TASKTYPE)){
            checkSql = "SELECT * " +
                        "FROM " + TABLE_NAME_TASK + " " +
                        "WHERE " + COL_NAME_TASK__TASKTYPE_ID + " = " + rowId + " " +
                        "LIMIT 1";
            checkCursor = mDb.rawQuery(checkSql, null);
            if(checkCursor.moveToFirst()){ //record exists
                checkCursor.close();
                return R.string.ERR_060;
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
        if(tableName.equals(TABLE_NAME_UOM)){
            if(content.containsKey(MainDbAdapter.COL_NAME_GEN_ISACTIVE)
                    && content.getAsString(MainDbAdapter.COL_NAME_GEN_ISACTIVE).equals("N"))
            {
                //check if the uom are used in an active car definition
                checkSql = "SELECT * " +
                            "FROM " + TABLE_NAME_CAR + " " +
                            "WHERE " + COL_NAME_GEN_ISACTIVE + " = 'Y' " +
                                    "AND (" + COL_NAME_CAR__UOMLENGTH_ID + " = " + rowId + " OR " +
                                            COL_NAME_CAR__UOMVOLUME_ID + " = " + rowId + ") " +
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
        else if(tableName.equals(TABLE_NAME_CURRENCY)){
            if(content.containsKey(MainDbAdapter.COL_NAME_GEN_ISACTIVE)
                    && content.getAsString(MainDbAdapter.COL_NAME_GEN_ISACTIVE).equals("N"))
            {
                //check if the currency are used in an active car definition
                checkSql = "SELECT * " +
                            "FROM " + TABLE_NAME_CAR + " " +
                            "WHERE " + COL_NAME_GEN_ISACTIVE + " = 'Y' " +
                                    "AND " + COL_NAME_CAR__CURRENCY_ID + " = " + rowId + " " +
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
                        " FROM " + MainDbAdapter.TABLE_NAME_UOMCONVERSION +
                        " WHERE " + MainDbAdapter.COL_NAME_UOMCONVERSION__UOMFROM_ID + " = " + fromId +
                            " AND " + MainDbAdapter.COL_NAME_UOMCONVERSION__UOMTO_ID + " = " + toId;
        if(rowId != null)
            sql = sql + " AND " + MainDbAdapter.COL_NAME_GEN_ROWID + " <> " + rowId.toString();
        Cursor resultCursor = mDb.rawQuery( sql, null );
        if(resultCursor.getCount() > 0)
        {
        	resultCursor.close();
            return R.string.ERR_005;
        }
        if(resultCursor != null)
        	resultCursor.close();
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
                    " FROM " + TABLE_NAME_MILEAGE +
                    " WHERE " + COL_NAME_MILEAGE__CAR_ID + "=" + carId +
                            " AND " + COL_NAME_MILEAGE__INDEXSTART + " <= " + startIndex.toString() +
                                " AND " + COL_NAME_MILEAGE__INDEXSTOP + " > " + startIndex.toString();
        if (rowId >= 0)
            checkSql = checkSql + " AND " + COL_NAME_GEN_ROWID + "<>" + rowId;

        Cursor checkCursor = mDb.rawQuery(checkSql, null); 
        if(checkCursor.getCount() > 0){
        	checkCursor.close();
            return R.string.ERR_001;
        }
    	checkCursor.close();
        
        checkSql = "SELECT * " +
                    " FROM " + TABLE_NAME_MILEAGE +
                    " WHERE " + COL_NAME_MILEAGE__CAR_ID + "=" + carId +
                            " AND " + COL_NAME_MILEAGE__INDEXSTART + " < " + stopIndex.toString() +
                                " AND " + COL_NAME_MILEAGE__INDEXSTOP + " >= " + stopIndex.toString();
        if (rowId >= 0)
            checkSql = checkSql + " AND " + COL_NAME_GEN_ROWID + "<>" + rowId;
        checkCursor = mDb.rawQuery(checkSql, null);
        if(checkCursor.getCount() > 0){
        	checkCursor.close();
            return R.string.ERR_002;
        }
    	checkCursor.close();

        checkSql = "SELECT * " +
                    " FROM " + TABLE_NAME_MILEAGE +
                    " WHERE " + COL_NAME_MILEAGE__CAR_ID + "=" + carId +
                            " AND " + COL_NAME_MILEAGE__INDEXSTART + " >= " + startIndex.toString() +
                                " AND " + COL_NAME_MILEAGE__INDEXSTOP + " <= " + stopIndex.toString();
        if (rowId >= 0)
            checkSql = checkSql + " AND " + COL_NAME_GEN_ROWID + "<>" + rowId;

        checkCursor = mDb.rawQuery(checkSql, null);
        if(checkCursor.getCount() > 0){
        	checkCursor.close();
            return R.string.ERR_003;
        }
    	checkCursor.close();

        return -1;
    }

    /**
     * Get a list of the recent user comments used in the fromTable
     * @param whereId
     * @param driverId
     * @param limitCount limit the size of the returned comments
     * @return a string array containig the last limitCount user comment from fromTable for the carId and DriverId
     */
    public String[] getAutoCompleteText(String fromTable, String fromColumn,
            long whereId, int limitCount){
        String[] retVal = null;
        ArrayList<String> commentList = new ArrayList<String>();
        String selectSql;
        if(fromTable.equals(TABLE_NAME_MILEAGE))
            selectSql = "SELECT DISTINCT " + COL_NAME_GEN_USER_COMMENT +
                            " FROM " + TABLE_NAME_MILEAGE +
                            " WHERE " + COL_NAME_MILEAGE__CAR_ID + " = " + whereId +
                                " AND " + COL_NAME_GEN_USER_COMMENT + " IS NOT NULL " +
                                " AND " + COL_NAME_GEN_USER_COMMENT + " <> '' " +
                            " ORDER BY " + COL_NAME_MILEAGE__INDEXSTOP + " DESC " +
                            " LIMIT " + limitCount;
        else if(fromTable.equals(TABLE_NAME_REFUEL))
            selectSql = "SELECT DISTINCT " + COL_NAME_GEN_USER_COMMENT +
                            " FROM " + TABLE_NAME_REFUEL +
                            " WHERE " + COL_NAME_REFUEL__CAR_ID + " = " + whereId +
                                " AND " + COL_NAME_GEN_USER_COMMENT + " IS NOT NULL " +
                                " AND " + COL_NAME_GEN_USER_COMMENT + " <> '' " +
                            " ORDER BY " + COL_NAME_REFUEL__DATE + " DESC " +
                            " LIMIT " + limitCount;
        else if(fromTable.equals(TABLE_NAME_EXPENSE))
            selectSql = "SELECT DISTINCT " + COL_NAME_GEN_USER_COMMENT +
                            " FROM " + TABLE_NAME_EXPENSE +
                            " WHERE " + COL_NAME_EXPENSE__CAR_ID + " = " + whereId +
                                " AND " + COL_NAME_GEN_USER_COMMENT + " IS NOT NULL " +
                                " AND " + COL_NAME_GEN_USER_COMMENT + " <> '' " +
                            " ORDER BY " + COL_NAME_EXPENSE__DATE + " DESC " +
                            " LIMIT " + limitCount;
        else if(fromTable.equals(TABLE_NAME_GPSTRACK))
            selectSql = "SELECT DISTINCT " + COL_NAME_GEN_USER_COMMENT +
                            " FROM " + TABLE_NAME_GPSTRACK +
                            " WHERE " + COL_NAME_GPSTRACK__CAR_ID + " = " + whereId +
                                " AND " + COL_NAME_GEN_USER_COMMENT + " IS NOT NULL " +
                                " AND " + COL_NAME_GEN_USER_COMMENT + " <> '' " +
                            " ORDER BY " + COL_NAME_GPSTRACK__DATE + " DESC " +
                            " LIMIT " + limitCount;
        else if(fromTable.equals(TABLE_NAME_BPARTNER))
            selectSql = "SELECT DISTINCT " + COL_NAME_GEN_NAME +
                            " FROM " + TABLE_NAME_BPARTNER +
                            " WHERE " + COL_NAME_GEN_ISACTIVE + " = \'Y\'" +
                            " ORDER BY " + COL_NAME_GEN_NAME;
        else if(fromTable.equals(TABLE_NAME_BPARTNERLOCATION)){
            selectSql = "SELECT DISTINCT " + fromColumn +
                            " FROM " + TABLE_NAME_BPARTNERLOCATION +
                            " WHERE " + COL_NAME_GEN_ISACTIVE + " = \'Y\' ";
            if(whereId != -1)
                selectSql = selectSql +
                                " AND " + COL_NAME_BPARTNERLOCATION__BPARTNER_ID + " = " + whereId;

            selectSql = selectSql +
                            " ORDER BY " + COL_NAME_BPARTNERLOCATION__ADDRESS;
        }
        else if(fromTable.equals(TABLE_NAME_TAG)){
            selectSql = "SELECT " + COL_NAME_GEN_NAME +
                            " FROM " + TABLE_NAME_TAG +
                            " WHERE " + COL_NAME_GEN_ISACTIVE + " = \'Y\' " +
                            " ORDER BY " + COL_NAME_GEN_NAME;
        }
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

    public Cursor execSelectSql(String selectSql, String[] selectionArgs){
        return mDb.rawQuery(selectSql, selectionArgs);
    }

    public void execSql(String sql){
        mDb.execSQL(sql);
    }

    public BigDecimal getCurrencyRate(long fromCurrencyId, long toCurrencyId){
        BigDecimal retVal = null;
        String retValStr = null;
        if(fromCurrencyId == toCurrencyId)
            return BigDecimal.ONE;
        try{
            String selectSql = "";
            Cursor selectCursor;

            selectSql = " SELECT * " +
                        " FROM " + TABLE_NAME_CURRENCYRATE +
                        " WHERE " + COL_NAME_GEN_ISACTIVE + "='Y' " +
                            " AND " + COL_NAME_CURRENCYRATE__FROMCURRENCY_ID + " = ? " +
                            " AND " + COL_NAME_CURRENCYRATE__TOCURRENCY_ID + " = ?";
            String[] selectionArgs = {Long.toString(fromCurrencyId), Long.toString(toCurrencyId)};
            selectCursor = execSelectSql(selectSql, selectionArgs);
            if(selectCursor.moveToFirst())
                retValStr = selectCursor.getString(COL_POS_CURRENCYRATE__RATE);
            selectCursor.close();
            if(retValStr != null && retValStr.length() > 0)
                return new BigDecimal(retValStr);

            selectSql = " SELECT * " +
                        " FROM " + TABLE_NAME_CURRENCYRATE +
                        " WHERE " + COL_NAME_GEN_ISACTIVE + "='Y' " +
                            " AND " + COL_NAME_CURRENCYRATE__TOCURRENCY_ID + " = ? " +
                            " AND " + COL_NAME_CURRENCYRATE__FROMCURRENCY_ID + " = ?";
            selectCursor = execSelectSql(selectSql, selectionArgs);
            if(selectCursor.moveToFirst())
                retValStr = selectCursor.getString(COL_POS_CURRENCYRATE__RATE);
            selectCursor.close();
            if(retValStr != null && retValStr.length() > 0){
                retVal = new BigDecimal(retValStr);
                if(retVal.signum() != 0)
                    return BigDecimal.ONE.divide(retVal, 10, RoundingMode.HALF_UP)
                            .setScale(StaticValues.DECIMALS_RATES, StaticValues.ROUNDING_MODE_RATES);
                else
                    return BigDecimal.ZERO;
            }
        }
        catch(NumberFormatException e){}
        return retVal;
    }

    public BigDecimal getUOMConversionRate(long fromId, long toId){
        BigDecimal retVal = null;
        String retValStr = null;
        if(fromId == toId)
            return BigDecimal.ONE;
        try{
            String selectSql = "";
            Cursor selectCursor;

            selectSql = " SELECT * " +
                        " FROM " + TABLE_NAME_UOMCONVERSION +
                        " WHERE " + COL_NAME_GEN_ISACTIVE + "='Y' " +
                            " AND " + COL_NAME_UOMCONVERSION__UOMFROM_ID + " = ? " +
                            " AND " + COL_NAME_UOMCONVERSION__UOMTO_ID + " = ?";
            String[] selectionArgs = {Long.toString(fromId), Long.toString(toId)};
            selectCursor = execSelectSql(selectSql, selectionArgs);
            if(selectCursor.moveToFirst())
                retValStr = selectCursor.getString(COL_POS_UOMCONVERSION__RATE);
            selectCursor.close();

            if(retValStr != null && retValStr.length() > 0)
                return new BigDecimal(retValStr);
        }
        catch(NumberFormatException e){}
        return retVal;
    }
    
    /**
     * @param carID: Car ID
     * @param expTypeID: Expense Type ID
     * @param date Date in seconds
     * @return
     */
    public BigDecimal getReimbursementRate(long carID, long expTypeID, long date){
    	BigDecimal retVal = null;
    	Cursor selectCursor = null;
    	
        try{
	    	String selectSql = 
	    			"SELECT " + sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_REIMBURSEMENT_CAR_RATES__RATE) +
					" FROM " + TABLE_NAME_REIMBURSEMENT_CAR_RATES +
						" JOIN " + TABLE_NAME_EXPENSETYPE + " ON " + 
							sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_REIMBURSEMENT_CAR_RATES__EXPENSETYPE_ID) + " = " +
								sqlConcatTableColumn(TABLE_NAME_EXPENSETYPE, COL_NAME_GEN_ROWID) +
					" WHERE " + 
						sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_REIMBURSEMENT_CAR_RATES__CAR_ID) + " = ? " +
						" AND " + sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_REIMBURSEMENT_CAR_RATES__EXPENSETYPE_ID) + "=? " +
						" AND " + sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_REIMBURSEMENT_CAR_RATES__VALIDFROM) + "<=? " + 
						" AND " + sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_REIMBURSEMENT_CAR_RATES__VALIDTO) + ">=? " +
					" ORDER BY " + sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_REIMBURSEMENT_CAR_RATES__VALIDFROM) + " DESC, " +
							sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_GEN_ROWID) + " DESC" +
					" LIMIT 1";
	    	String[] selectionArgs = {Long.toString(carID), Long.toString(expTypeID), Long.toString(date), Long.toString(date)};
	        selectCursor = execSelectSql(selectSql, selectionArgs);
	        if(selectCursor.moveToFirst())
	            retVal = new BigDecimal(selectCursor.getString(0));
        }	
        catch(NumberFormatException e){}
    	finally{
    		try{if(selectCursor != null) selectCursor.close();}catch(Exception e){};
    	}
    	return retVal;
    }
//    public BigDecimal getConvertedDistance(long fromId, long toId, BigDecimal distance){
//        return distance.multiply(getUOMConversionRate(fromId, toId)).setScale(StaticValues.DECIMALS_CONVERSIONS, StaticValues.ROUNDING_MODE_CONVERSIONS);
//    }

    public long getCarUOMVolumeID(long carID){
    	Cursor c = fetchRecord(TABLE_NAME_CAR, COL_LIST_CAR_TABLE, carID);
    	long retVal = -1;
    	if(c != null){
    		retVal = c.getLong(COL_POS_CAR__UOMVOLUME_ID);
        	c.close();
    	}
    	return retVal;
                
    }

    public long getCarUOMLengthID(long carID){
    	Cursor c = fetchRecord(TABLE_NAME_CAR, COL_LIST_CAR_TABLE, carID);
    	long retVal = -1;
    	if(c != null){
    		retVal = c.getLong(COL_POS_CAR__UOMLENGTH_ID);
        	c.close();
    	}
    	return retVal;
    }

    public long getCarCurrencyID(long carID){
    	Cursor c = fetchRecord(TABLE_NAME_CAR, COL_LIST_CAR_TABLE, carID);
    	long retVal = -1;
    	if(c != null){
    		retVal = c.getLong(COL_POS_CAR__CURRENCY_ID);
        	c.close();
    	}
    	return retVal;
    }

    public String getCarName(long carID){
    	Cursor c = fetchRecord(TABLE_NAME_CAR, COL_LIST_CAR_TABLE, carID);
    	String retVal = null;
    	if(c != null){
    		retVal = c.getString(COL_POS_GEN_NAME);
        	c.close();
    	}
    	return retVal;
    }

    public String getCurrencyCode(long currencyID){
    	Cursor c = fetchRecord(MainDbAdapter.TABLE_NAME_CURRENCY, MainDbAdapter.COL_LIST_CURRENCY_TABLE, currencyID);
    	String retVal = null;
    	if(c != null){
    		retVal = c.getString(MainDbAdapter.COL_POS_CURRENCY__CODE);
        	c.close();
    	}
    	return retVal;
    }

    public String getUOMCode(long uomID){
    	Cursor c = fetchRecord(MainDbAdapter.TABLE_NAME_UOM, MainDbAdapter.COL_LIST_UOM_TABLE, uomID);
    	String retVal = null;
    	if(c != null){
    		retVal = c.getString(MainDbAdapter.COL_POS_UOM__CODE);
        	c.close();
    	}
    	return retVal;
    }

    public long getIdFromCode(String tableName, String code){
        String selection = "Code = ?";
        String[] selectionArgs = {code};
        Cursor c = query(tableName, COL_LIST_GEN_ROWID, selection, selectionArgs, null, null, null);
//                fetchForTable(tableName, genColRowId, "Code = '" + code + "'", "Code");
        long retVal = -1;
        if(c.moveToFirst())
            retVal = c.getLong(0);
        c.close();
        return retVal;
    }

    public Cursor query(String sql, String[] args){
        return mDb.rawQuery(sql, args);
    }
    
    public Cursor query (String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy){
        return mDb.query (table, columns, selection, selectionArgs, groupBy, having, orderBy);
    }

    /**
     * get the start index for a new mileage record
     */
    public BigDecimal getCarLastMileageIndex(long mCarId){
        Double mStartIndexStr = null;
        String sql = "SELECT MAX( " + MainDbAdapter.COL_NAME_MILEAGE__INDEXSTOP + "), 1 As Pos " +
                        "FROM " + MainDbAdapter.TABLE_NAME_MILEAGE + " " +
                        "WHERE " + MainDbAdapter.COL_NAME_GEN_ISACTIVE + " = 'Y' " +
                            "AND " + MainDbAdapter.COL_NAME_MILEAGE__CAR_ID + " = ? " +
                      "UNION " +
                      "SELECT " + MainDbAdapter.COL_NAME_CAR__INDEXCURRENT + ", 2 As Pos " +
                      "FROM " + MainDbAdapter.TABLE_NAME_CAR + " " +
                      "WHERE " + MainDbAdapter.COL_NAME_GEN_ROWID + " = ? " +
                      "ORDER BY Pos ASC";
        String[] selectionArgs = {Long.toString(mCarId), Long.toString(mCarId)};
        Cursor c = execSelectSql(sql, selectionArgs);
        if(c.moveToFirst() && c.getString(0) != null){
            mStartIndexStr = c.getDouble(0);
        }
        if((mStartIndexStr == null)
                && c.moveToNext() && c.getString(0) != null)
            mStartIndexStr = c.getDouble(0);
        if(mStartIndexStr == null)
            mStartIndexStr = Double.valueOf("0");
        c.close();
    	return new BigDecimal(mStartIndexStr).setScale(StaticValues.DECIMALS_LENGTH, StaticValues.ROUNDING_MODE_LENGTH);
    }

    /**
     * get the current index of the car
     */
    public BigDecimal getCarCurrentIndex(long mCarId){
        Double mStartIndexStr = null;
        String sql = 
                      "SELECT " + MainDbAdapter.COL_NAME_CAR__INDEXCURRENT + 
                      " FROM " + MainDbAdapter.TABLE_NAME_CAR + " " +
                      " WHERE " + MainDbAdapter.COL_NAME_GEN_ROWID + " = ? ";
        String[] selectionArgs = {Long.toString(mCarId)};
        Cursor c = execSelectSql(sql, selectionArgs);
        if(c.moveToFirst() && c.getString(0) != null){
            mStartIndexStr = c.getDouble(0);
        }
        c.close();
        if(mStartIndexStr == null)
            mStartIndexStr = Double.valueOf("0");
    	return new BigDecimal(mStartIndexStr).setScale(StaticValues.DECIMALS_LENGTH, StaticValues.ROUNDING_MODE_LENGTH);
    }
    
    /**
     * check if only one active record exist in a given table
     * @param table the table where we look
     * @param additionalWhere additional where clause to IsActive condition 
     * @return if one record exists the id of the record, otherwise -1
     */
    public long isSingleActiveRecord(String table, String additionalWhere){
        String selectSql = "";
        Cursor selectCursor;
        long retVal = -1;

        //if max == min => one single record
        selectSql = " SELECT MAX(" + MainDbAdapter.COL_NAME_GEN_ROWID + "), MIN(" + MainDbAdapter.COL_NAME_GEN_ROWID + ") " +
                    " FROM " + table +
                    " WHERE " + MainDbAdapter.COL_NAME_GEN_ISACTIVE + "='Y' ";
        
        if(additionalWhere != null && additionalWhere.length() > 0)
        	selectSql = selectSql + " AND " + additionalWhere;
        
        selectCursor = execSelectSql(selectSql, null);
        if(selectCursor.moveToFirst()){
        	if(selectCursor.getLong(0) == selectCursor.getLong(1)){ // one single active record
        		retVal = selectCursor.getLong(0);
        	}
        }
        selectCursor.close();
       	return retVal;
    }
    /**
     * 
     * @param table
     * @param additionalWhere
     * @param orderBy
     * @return the first record ID
     */
    public long getFirstActiveID(String table, String additionalWhere, String orderBy){
    	long retVal = -1;
        String selectSql = "";
        Cursor selectCursor;
        selectSql = " SELECT " + MainDbAdapter.COL_NAME_GEN_ROWID +
                " FROM " + table +
                " WHERE " + MainDbAdapter.COL_NAME_GEN_ISACTIVE + "='Y' ";
    
	    if(additionalWhere != null && additionalWhere.length() > 0)
	    	selectSql = selectSql + " AND " + additionalWhere;
	    
	    if(orderBy != null && orderBy.length() > 0)
	    	selectSql = selectSql + " ORDER BY " + orderBy;
	    
	    selectCursor = execSelectSql(selectSql, null);
	    if(selectCursor.moveToFirst()){
    		retVal = selectCursor.getLong(0);
	    }
	    selectCursor.close();
    	
    	return retVal;
    }

    public boolean isIDActive(String table, long Id){
    	boolean retVal = false;
        String selectSql = "";
        Cursor selectCursor;
        selectSql = " SELECT " + MainDbAdapter.COL_NAME_GEN_ISACTIVE +
                " FROM " + table +
                " WHERE " + MainDbAdapter.COL_NAME_GEN_ROWID + " = " + Id;
	    
	    selectCursor = execSelectSql(selectSql, null);
	    if(selectCursor.moveToFirst()){
    		retVal = selectCursor.getString(0).equals("Y");
	    }
	    selectCursor.close();
    	
    	return retVal;
    	
    }
}
