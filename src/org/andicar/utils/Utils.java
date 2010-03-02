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
     * @param decodeType type of decode. See Constants.dateDecodeType...
     * @return
     */
    public static long decodeDateStr(String dateStr, String decodeType) throws IndexOutOfBoundsException, NumberFormatException{
        Calendar cal = Calendar.getInstance();
        cal.set( Integer.parseInt(dateStr.substring(0, 4)),
                    Integer.parseInt(dateStr.substring(5, 7)) - 1,
                    Integer.parseInt(dateStr.substring(8, 10)));
        if(dateStr.length() > 10 && decodeType.equals(Constants.dateDecodeTypeNoChange)){
            cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(dateStr.substring(11, 13)));
            cal.set(Calendar.MINUTE, Integer.parseInt(dateStr.substring(14, 16)));
            cal.set(Calendar.SECOND, Integer.parseInt(dateStr.substring(17, 19)));
        }
        else if(decodeType.equals(Constants.dateDecodeTypeTo0Hour)){
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
        }
        else if(decodeType.equals(Constants.dateDecodeTypeTo24Hour)){
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

}
