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

import org.andicar.utils.StaticValues;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkStatusChangeReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context ctx, Intent intent) {
		NetworkInfo networkInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
		if(networkInfo.isConnected()) {
			SharedPreferences mPreferences = ctx.getSharedPreferences(StaticValues.GLOBAL_PREFERENCE_NAME, 0);
			if(!mPreferences.getBoolean("SecureBkSuccess", true)){
				if(mPreferences.contains("SecureBkFile") && mPreferences.getString("SecureBkFile", "").length() > 0 && mPreferences.contains("SecureBkAttName")){
//					editor.putString("SecureBkAttName", attachName);
					Intent i = new Intent(ctx, FileMailer.class);
					i.putExtra("bkFile", mPreferences.getString("SecureBkFile", ""));
					i.putExtra("attachName", mPreferences.getString("SecureBkAttName", ""));
					ctx.startService(i);
				}
			}
		}	
	}

}
