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

import java.util.Calendar;

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
     *
     * @return the current date in the form of yyyy-mm-dd
     */
    public static String getDateStr(boolean appendHour, boolean appendMinute){
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

}
