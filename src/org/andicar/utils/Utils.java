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
        if(dateStr.length() > 10 && decodeType.equals(StaticValues.dateDecodeTypeNoChange)){
            cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(dateStr.substring(11, 13)));
            cal.set(Calendar.MINUTE, Integer.parseInt(dateStr.substring(14, 16)));
            cal.set(Calendar.SECOND, Integer.parseInt(dateStr.substring(17, 19)));
        }
        else if(decodeType.equals(StaticValues.dateDecodeTypeTo0Hour)){
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
        }
        else if(decodeType.equals(StaticValues.dateDecodeTypeTo24Hour)){
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            cal.set(Calendar.MILLISECOND, 999);
        }
         return cal.getTimeInMillis();
    }

    public static String pad(int c) {
        if(c >= 10) {
            return String.valueOf(c);
        }
        else {
            return "0" + String.valueOf(c);
        }
    }

    public static String appendDateTime(String inStr, boolean appendHour, boolean appendMinute,
            boolean appendSecondMilisecond){
        Calendar reportCal = Calendar.getInstance();
        inStr = inStr +
                    reportCal.get(Calendar.YEAR) + "" +
                    pad(reportCal.get(Calendar.MONTH) + 1) +
                    pad(reportCal.get(Calendar.DAY_OF_MONTH));
        if(appendHour)
            inStr = inStr +
                    pad(reportCal.get(Calendar.HOUR_OF_DAY));
        if(appendMinute)
            inStr = inStr +
                    pad(reportCal.get(Calendar.MINUTE));
        if(appendSecondMilisecond)
            inStr = inStr +
                    pad(reportCal.get(Calendar.SECOND)) +
                    reportCal.get(Calendar.MILLISECOND);
        return inStr;
    }

}
