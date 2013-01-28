/*
 *  AndiCar - car management software for Android powered devices
 *  Copyright (C) 2010 Miklos Keresztes (miklos.keresztes@gmail.com)
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT AY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.andicar.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 */
public class AndiCarExceptionHandler
        implements Thread.UncaughtExceptionHandler {

    private Thread.UncaughtExceptionHandler mPreviousHandler;
    private Context mCtx;

    public AndiCarExceptionHandler(Thread.UncaughtExceptionHandler pPreviousHandler, Context ctx) {
        mPreviousHandler = pPreviousHandler;
        mCtx = ctx;
    }

    public void uncaughtException(Thread thread, Throwable thrwbl) {
    	SharedPreferences mPreferences = mCtx.getSharedPreferences(StaticValues.GLOBAL_PREFERENCE_NAME, Context.MODE_MULTI_PROCESS);
    	boolean isPayPal = false;
        Throwable cause = thrwbl.getCause();
        StackTraceElement[] stackTrace;
        if(cause != null)
            stackTrace = cause.getStackTrace();
        else
            stackTrace = thrwbl.getStackTrace();

        StackTraceElement stackTraceElement;
        String stackStr = "";
        for(int i = 0; i < stackTrace.length; i++) {
            stackTraceElement = stackTrace[i];
            if(stackTraceElement.getClassName().contains("andicar")) {
                stackStr = stackStr + stackTraceElement.getClassName() + "." + stackTraceElement.getMethodName() + ": " +
                        stackTraceElement.getLineNumber() + "\n";
            }
            
            if(stackTraceElement.getClassName().contains("com.paypal.android")) {
            	isPayPal = true;
            }
        }
        if(!mPreferences.getBoolean("IsBeta", false)){
            AndiCarStatistics.sendFlurryStartSession(mCtx);
            AndiCarStatistics.sendFlurryError(mCtx, "AndiCarError", stackStr, thrwbl.getClass().toString() + ": " + thrwbl.getMessage());
        }
        if(!isPayPal)
        	mPreviousHandler.uncaughtException(thread, thrwbl);
    }
}
