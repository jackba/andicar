/*
 *  AndiCar - a car management software for Android powered devices.
 *
 *  Copyright (C) 2013 Miklos Keresztes (miklos.keresztes@gmail.com)
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

package org.andicar.service;

import org.andicar.utils.AndiCarExceptionHandler;
import org.andicar.utils.StaticValues;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AndiCarBTConnectionReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if(context.getSharedPreferences(StaticValues.GLOBAL_PREFERENCE_NAME, 0).getBoolean("SendCrashReport", true))
			Thread.setDefaultUncaughtExceptionHandler(
	                    new AndiCarExceptionHandler(Thread.getDefaultUncaughtExceptionHandler(), context));
		try{
			BTConnectionReceiver cr = new BTConnectionReceiver();
			cr.onReceive(context, intent);
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

}
