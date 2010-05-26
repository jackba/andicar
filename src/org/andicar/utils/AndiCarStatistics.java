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
import java.util.Map;

/**
 */
public class AndiCarStatistics {
    public static void sendFlurryStartSession(Context ctx){
        if(StaticValues.isReleaseVersion){
            FlurryAgent.setReportLocation(true);
            FlurryAgent.onStartSession(ctx, StaticValues.FLURRY_ID);
        }
    }
    public static void sendFlurryEndSession(Context ctx){
        if(StaticValues.isReleaseVersion){
            FlurryAgent.onEndSession(ctx);
        }
    }
    public static void sendFlurryEvent(String event, Map<String, String> parameters){
        if(StaticValues.isReleaseVersion){
            FlurryAgent.onEvent(event, parameters);
        }
    }

    public static void sendFlurryError(String errorId, String message, String errorClass){
        if(StaticValues.isReleaseVersion){
            FlurryAgent.onError(errorId, message, errorClass);
        }
    }
}
