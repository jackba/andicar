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

import com.flurry.android.FlurryAgent;
import java.util.Map;

/**
 */
public class AndiCarStatistics {
    public static void sendFlurryStartSession(Context ctx){
    	SharedPreferences mPreferences = ctx.getSharedPreferences(StaticValues.GLOBAL_PREFERENCE_NAME, 0);
        if(!mPreferences.getBoolean("IsBeta", false)){
            FlurryAgent.setReportLocation(false);
            FlurryAgent.onStartSession(ctx, StaticValues.FLURRY_ID);
        }
    }
    public static void sendFlurryEndSession(Context ctx){
    	SharedPreferences mPreferences = ctx.getSharedPreferences(StaticValues.GLOBAL_PREFERENCE_NAME, 0);
        if(!mPreferences.getBoolean("IsBeta", false)){
            FlurryAgent.onEndSession(ctx);
        }
    }
    public static void sendFlurryEvent(Context ctx, String event, Map<String, String> parameters){
    	SharedPreferences mPreferences = ctx.getSharedPreferences(StaticValues.GLOBAL_PREFERENCE_NAME, 0);
        if(!mPreferences.getBoolean("IsBeta", false)){
            FlurryAgent.onEvent(event, parameters);
        }
    }

    public static void sendFlurryError(Context ctx, String errorId, String message, String errorClass){
    	SharedPreferences mPreferences = ctx.getSharedPreferences(StaticValues.GLOBAL_PREFERENCE_NAME, 0);
        if(!mPreferences.getBoolean("IsBeta", false)){
            FlurryAgent.onError(errorId, message, errorClass);
        }
    }
}
