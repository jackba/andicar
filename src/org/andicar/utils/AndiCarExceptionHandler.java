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
import com.flurry.android.FlurryAgent;
import java.util.HashMap;
import java.util.Map;

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
        Throwable cause = thrwbl.getCause();
        StackTraceElement[] stackTrace = cause.getStackTrace();
        StackTraceElement stackTraceElement;
        String stackStr = thrwbl.getMessage() + "\n";
        for(int i = 0; i < stackTrace.length; i++) {
            stackTraceElement = stackTrace[i];
            if(stackTraceElement.getClassName().contains("org.andicar")) {
                stackStr = stackStr + stackTraceElement.getMethodName() + ": " +
                        stackTraceElement.getLineNumber() + "\n";
            }
        }
        AndiCarStatistics.sendFlurryStartSession(mCtx);
        AndiCarStatistics.sendFlurryError("AndiCarError", thrwbl.getMessage(), stackStr);
        AndiCarStatistics.sendFlurryEndSession(mCtx);
        mPreviousHandler.uncaughtException(thread, thrwbl);
    }
}
