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

package org.andicar.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Calendar;

import org.andicar.persistence.FileUtils;
import org.andicar.persistence.MainDbAdapter;
import org.andicar.persistence.ReportDbAdapter;
import org.andicar2.activity.R;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;

import com.google.android.maps.GeoPoint;

/**
 *
 * @author miki
 */
public class Utils {
    /**
     * decode a string representing a date in form of YYYY-MM-DD [HH:MM:SS]
     * @param dateStr the string representing the date to decode
     * @param decodeType type of decode. See StaticValues.dateDecodeType...
     * @return
     */
    public static long decodeDateStr(String dateStr, String decodeType) throws IndexOutOfBoundsException, NumberFormatException{
        Calendar cal = Calendar.getInstance();
        cal.set( Integer.parseInt(dateStr.substring(0, 4)),
                    Integer.parseInt(dateStr.substring(5, 7)) - 1,
                    Integer.parseInt(dateStr.substring(8, 10)));
        if(dateStr.length() > 10 && decodeType.equals(StaticValues.DATE_DECODE_NO_CHANGE)){
            cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(dateStr.substring(11, 13)));
            cal.set(Calendar.MINUTE, Integer.parseInt(dateStr.substring(14, 16)));
            cal.set(Calendar.SECOND, Integer.parseInt(dateStr.substring(17, 19)));
        }
        else if(decodeType.equals(StaticValues.DATE_DECODE_TO_ZERO)){
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
        }
        else if(decodeType.equals(StaticValues.DATE_DECODE_TO_24)){
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            cal.set(Calendar.MILLISECOND, 999);
        }
         return cal.getTimeInMillis();
    }

    public static String pad(long value, int length) {
        return pad(Long.toString(value), length);
    }
    public static String pad(String value, int length) {
        String retVal = value;
        if(retVal.length() >= length)
            return retVal;
        else
            return pad("0" + retVal, length);

    }

    public static String appendDateTime(String inStr, boolean appendHour, boolean appendMinute,
            boolean appendSecondMilisecond){
        Calendar cal = Calendar.getInstance();
        inStr = inStr +
                    cal.get(Calendar.YEAR) + "" +
                    pad(cal.get(Calendar.MONTH) + 1, 2) +
                    pad(cal.get(Calendar.DAY_OF_MONTH), 2);
        if(appendHour)
            inStr = inStr +
                    pad(cal.get(Calendar.HOUR_OF_DAY), 2);
        if(appendMinute)
            inStr = inStr +
                    pad(cal.get(Calendar.MINUTE), 2);
        if(appendSecondMilisecond)
            inStr = inStr +
                    pad(cal.get(Calendar.SECOND), 2) +
                    cal.get(Calendar.MILLISECOND);
        return inStr;
    }

    /**
     * @return the current date in the form of yyyy-mm-dd
     */
    public static String getDateStr(boolean appendHour, boolean appendMinute, boolean appendSecond){
        Calendar cal = Calendar.getInstance();
        String retVal = cal.get(Calendar.YEAR) + "-" +
                    pad(cal.get(Calendar.MONTH) + 1, 2) + "-" +
                    pad(cal.get(Calendar.DAY_OF_MONTH), 2);
        if(appendHour){
            retVal = retVal + " " +
                    pad(cal.get(Calendar.HOUR_OF_DAY), 2);
        }
        if(appendMinute){
            if(appendHour)
                retVal = retVal + ":" +
                        pad(cal.get(Calendar.MINUTE), 2);
            else
                retVal = retVal + " " +
                        pad(cal.get(Calendar.MINUTE), 2);
        }
        if(appendSecond){
        	retVal = retVal + " " +
            		pad(cal.get(Calendar.SECOND), 2);
        }
        return retVal;

    }

    /**
     * Convert seconds in format X Days Y h Z min [S s]
     * @param lSeconds the seconds to be converted
     * @param withSeconds include the remaining seconds in the time string
     * @return a string representing the time in format X Days Y h Z min [S s]
     */
    public static String getTimeString(long lSeconds, boolean withSeconds){
        String retVal ="";
        long days = lSeconds / 86400;
        //get the remaining seconds
        long remaining = lSeconds - (days * 86400);
        long hours = remaining / 3600;
        remaining = remaining - (hours * 3600);
        long minuts = remaining / 60;
        remaining = remaining - (minuts * 60);
        retVal = (days > 0 ? days + (days > 1 ? " Days " : " Day ") : "") + 
                    (hours > 0 ? hours + " h " : "") +
                    minuts + " min" +
                (withSeconds ? " " + remaining + " s" : "");
        return retVal;
    }

    public static GeoPoint getGeopointFromCSVTrackLine(String csvTrackLine){
        GeoPoint retVal = null;
        for(int i = 0; i < 3; i++){
            csvTrackLine = csvTrackLine.replaceFirst(csvTrackLine.substring(0, csvTrackLine.indexOf(",") + 1), "");
        }
        String latitude = csvTrackLine.substring(0, csvTrackLine.indexOf(","));
        csvTrackLine = csvTrackLine.replaceFirst(csvTrackLine.substring(0, csvTrackLine.indexOf(",") + 1), "");
        String longitude = csvTrackLine.substring(0, csvTrackLine.indexOf(","));
        try{
            retVal = new GeoPoint(
                            (int) (Double.parseDouble(latitude) * 1E6),
                            (int) (Double.parseDouble(longitude) * 1E6));
        }
        catch(NumberFormatException e){
            retVal = null;
        }
        return retVal;
    }

    public static double getDistanceFromCSVTrackLine(String csvTrackLine){
        double retVal = 0;
        for(int i = 0; i < 7; i++){
            csvTrackLine = csvTrackLine.replaceFirst(csvTrackLine.substring(0, csvTrackLine.indexOf(",") + 1), "");
        }
        String distance = csvTrackLine.substring(0, csvTrackLine.indexOf(","));
        try{
            retVal = Double.parseDouble(distance);
        }
        catch(NumberFormatException e){
            retVal = 0;
        }
        return retVal;
    }

    public void sendGPSTrackAsEmail(Context ctx, Resources mRes, long gpsTrackID){
        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        emailIntent.setType("text/html");
        Bundle b = new Bundle();
        MainDbAdapter mMainDbAdapter = new MainDbAdapter(ctx);
        String emailSubject = "AndiCar GPS Track";

        b.putString(MainDbAdapter.sqlConcatTableColumn(MainDbAdapter.TABLE_NAME_GPSTRACK, MainDbAdapter.COL_NAME_GEN_ROWID) + "=", Long.toString(gpsTrackID));
        ReportDbAdapter reportDbAdapter = new ReportDbAdapter(ctx, "gpsTrackListViewSelect", b);
        Cursor c = reportDbAdapter.fetchReport(1);
        if(c.moveToFirst()){
            String emailText =
                    c.getString(c.getColumnIndex(ReportDbAdapter.FIRST_LINE_LIST_NAME))
                    	.replace("[#1]", DateFormat.getDateFormat(ctx.getApplicationContext())
            				.format(c.getLong(7) * 1000))+ "\n" +
                    c.getString(c.getColumnIndex(ReportDbAdapter.SECOND_LINE_LIST_NAME))
                        .replace("[#1]", mRes.getString(R.string.GPSTrackReport_GPSTrackVar_1))
                        .replace("[#2]", mRes.getString(R.string.GPSTrackReport_GPSTrackVar_2))
                        .replace("[#3]", mRes.getString(R.string.GPSTrackReport_GPSTrackVar_3))
                        .replace("[#4]", mRes.getString(R.string.GPSTrackReport_GPSTrackVar_4))
                        .replace("[#5]", mRes.getString(R.string.GPSTrackReport_GPSTrackVar_5) +
                                Utils.getTimeString(c.getLong(c.getColumnIndex(ReportDbAdapter.FOURTH_LINE_LIST_NAME)), false))
                        .replace("[#6]", mRes.getString(R.string.GPSTrackReport_GPSTrackVar_6) +
                                Utils.getTimeString(c.getLong(c.getColumnIndex(ReportDbAdapter.FIFTH_LINE_LIST_NAME)), false))
                        .replace("[#12]", mRes.getString(R.string.GPSTrackReport_GPSTrackVar_12) +
                                Utils.getTimeString(c.getLong(c.getColumnIndex(ReportDbAdapter.COL_NAME_GPSTRACK__TOTALPAUSETIME)), false))
                        .replace("[#7]", mRes.getString(R.string.GPSTrackReport_GPSTrackVar_7))
                        .replace("[#8]", mRes.getString(R.string.GPSTrackReport_GPSTrackVar_8))
                        .replace("[#9]", mRes.getString(R.string.GPSTrackReport_GPSTrackVar_9))
                        .replace("[#10]", mRes.getString(R.string.GPSTrackReport_GPSTrackVar_10))
                        .replace("[#11]", mRes.getString(R.string.GPSTrackReport_GPSTrackVar_11))
                    + "\n" +
                    c.getString(c.getColumnIndex(ReportDbAdapter.THIRD_LINE_LIST_NAME));
            emailSubject = emailSubject + " - " +
                    c.getString(c.getColumnIndex(ReportDbAdapter.COL_NAME_GEN_NAME));
            c.close();
            reportDbAdapter.close();

            emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, emailText + "\nSent by AndiCar (http://www.andicar.org)");
        }

        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, emailSubject);
        
        //get the track files
        String selection = MainDbAdapter.COL_NAME_GPSTRACKDETAIL__GPSTRACK_ID + "= ? ";
        String[] selectionArgs = {Long.toString(gpsTrackID)};
        c = mMainDbAdapter.query(MainDbAdapter.TABLE_NAME_GPSTRACKDETAIL, MainDbAdapter.COL_LIST_GPSTRACKDETAIL_TABLE, 
                selection, selectionArgs, null, null, MainDbAdapter.COL_NAME_GPSTRACKDETAIL__FILE);
        
        Bundle trackFiles = new Bundle();
        String trackFile = "";
        while(c.moveToNext()){
        	trackFile = c.getString(MainDbAdapter.COL_POS_GPSTRACKDETAIL__FILE);
        	trackFiles.putString(trackFile.replace(StaticValues.TRACK_FOLDER, ""), trackFile);
        }
        
        //create the zip file
        Uri trackFileZip = FileUtils.zipFiles(trackFiles, StaticValues.TRACK_FOLDER + "AndiCarGPSTrack.zip");
        if(trackFileZip != null)
            emailIntent.putExtra(android.content.Intent.EXTRA_STREAM, trackFileZip);
        ctx.startActivity(Intent.createChooser(emailIntent, mRes.getString(R.string.GEN_Share)));
    }

    /**
     * @param number: the number which will be converted to string
     * @param localeFormat: also format the returned string according to locale formats
     * @return the string representation of the number
     */
    public static String numberToString(Object number, boolean localeFormat, int scale, RoundingMode roundingMode){
    	try{
	    	BigDecimal bdNumber = null;
	    	
	    	if(number instanceof Double)
	    		bdNumber = new BigDecimal((Double)number);
	    	else if(number instanceof Float)
	    		bdNumber = new BigDecimal((Float)number);
	    	else if(number instanceof Float)
	    		bdNumber = new BigDecimal((Float)number);
	    	else if(number instanceof Integer)
	    		bdNumber = new BigDecimal((Integer)number);
	    	else if(number instanceof Long)
	    		bdNumber = new BigDecimal((Long)number);
	    	else if(number instanceof Short)
	    		bdNumber = new BigDecimal((Short)number);
	    	else if(number instanceof BigDecimal)
	    		bdNumber = (BigDecimal)number;
	    	else if(number instanceof String)
	    		bdNumber = new BigDecimal((String)number);
	    	
	    	bdNumber = bdNumber.setScale(scale, roundingMode);
	    	bdNumber = bdNumber.stripTrailingZeros();
	    	
	    	if(localeFormat){
	    		NumberFormat nf = NumberFormat.getInstance();
	    		if(nf instanceof DecimalFormat){
		    		DecimalFormatSymbols dfs = ((DecimalFormat)nf).getDecimalFormatSymbols();
		    		nf.setMinimumFractionDigits(scale);
		    		String retVal = nf.format(bdNumber);
		    		if(retVal.contains("" + dfs.getDecimalSeparator())){
			    		//strip trailing zeroes 
			    		while ((retVal.endsWith("0") || retVal.endsWith("" + dfs.getDecimalSeparator()) || retVal.endsWith("" + dfs.getGroupingSeparator())) 
			    				&& retVal.length() > 1) {
			    			if(retVal.endsWith("" + dfs.getDecimalSeparator())){
			        			retVal = retVal.substring(0, retVal.length() - 1);
			        			break;
			    			}
			    			retVal = retVal.substring(0, retVal.length() - 1);
						}
		    		}
		    		return retVal;
	    		}
	    		else
	    			return numberToStringOld(number, localeFormat, scale, roundingMode);
	    	}
	    	return bdNumber.toPlainString();
    	}
    	catch(Exception e){
    		return numberToStringOld(number, localeFormat, scale, roundingMode);
    	}
    }
    
    public static String numberToStringOld(Object number, boolean localeFormat, int scale, RoundingMode roundingMode){
        BigDecimal bdNumber = null;
        
        if(number instanceof Double)
                bdNumber = new BigDecimal((Double)number);
        else if(number instanceof Float)
                bdNumber = new BigDecimal((Float)number);
        else if(number instanceof Float)
                bdNumber = new BigDecimal((Float)number);
        else if(number instanceof Integer)
                bdNumber = new BigDecimal((Integer)number);
        else if(number instanceof Long)
                bdNumber = new BigDecimal((Long)number);
        else if(number instanceof Short)
                bdNumber = new BigDecimal((Short)number);
        else if(number instanceof BigDecimal)
                bdNumber = (BigDecimal)number;
        
        bdNumber = bdNumber.setScale(scale, roundingMode);
        bdNumber = bdNumber.stripTrailingZeros();
        if(localeFormat)
                return NumberFormat.getInstance().format(bdNumber);

        return bdNumber.toPlainString();
    }
    
    public static String getDaysHoursMinsFromSec(long sec){
    	int days = (int) (sec / (86400d));
    	sec = sec - (days * 86400);
    	int hours = (int) (sec / 3600d);
    	sec = sec - (hours * 3600);
    	int minutes = (int) (sec / 60d);
    	return (days != 0 ? days + "d ": "" ) + (hours < 10 ? "0" : "" ) + hours + ":" + minutes;
    }
}
