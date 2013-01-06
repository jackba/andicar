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

import org.andicar.activity.miscellaneous.GPSTrackController;
import org.andicar.persistence.AddOnDBAdapter;
import org.andicar.persistence.DB;
import org.andicar.persistence.MainDbAdapter;
import org.andicar.utils.AndiCarExceptionHandler;
import org.andicar.utils.AndiCarStatistics;
import org.andicar.utils.StaticValues;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

public class BTConnectionReceiver {

    protected PowerManager pm = null;
    protected WakeLock mWakeLock = null;

	public void onReceive(Context context, Intent intent) {
		if(context.getSharedPreferences(StaticValues.GLOBAL_PREFERENCE_NAME, 0).getBoolean("SendCrashReport", true))
			Thread.setDefaultUncaughtExceptionHandler(
	                    new AndiCarExceptionHandler(Thread.getDefaultUncaughtExceptionHandler(), context));
		try{
//        	Toast toast = Toast.makeText( context, "BT connection detected", Toast.LENGTH_SHORT );
//        	toast.show();
			MainDbAdapter mDb = new MainDbAdapter(context);
			
			if (intent.getAction().equals(BluetoothDevice.ACTION_ACL_CONNECTED)) {
				//check if GPS track active
				boolean isGpsTrackOn = context.getSharedPreferences(StaticValues.GLOBAL_PREFERENCE_NAME, 0).getBoolean("isGpsTrackOn", false);
				if(isGpsTrackOn)
					return;
//	        	toast = Toast.makeText( context, "BT connected", Toast.LENGTH_SHORT );
//	        	toast.show();
	        	
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if(device != null){
					String deviceMAC = device.getAddress();
//					deviceInfo = device.getName();
//					deviceInfo = device.getBluetoothClass().toString();
					//check if this device is linked with a car
					if(deviceMAC == null || deviceMAC.length() == 0)
						return;
					String[] selArgs = {deviceMAC};
					Cursor c = mDb.query(AddOnDBAdapter.ADDON_BTDEVICE_CAR_TABLE_NAME, AddOnDBAdapter.addonBTDeviceCarTableColNames, 
							MainDbAdapter.isActiveCondition + " AND " + DB.sqlConcatTableColumn(AddOnDBAdapter.ADDON_BTDEVICE_CAR_TABLE_NAME, AddOnDBAdapter.ADDON_BTDEVICE_CAR_COL_MACADDR_NAME) + " = ?", 
							selArgs, null, null, MainDbAdapter.GEN_COL_ROWID_NAME + " DESC");
					if(c == null)
						return;
					if(c.moveToFirst()){ //linked car exist
						try{
							if(context.getSharedPreferences(StaticValues.GLOBAL_PREFERENCE_NAME, 0).getBoolean("SendUsageStatistics", true)){
					            AndiCarStatistics.sendFlurryStartSession(context);
						        AndiCarStatistics.sendFlurryEvent(context, "BTStarterUsed", null);
					        }
						}
						catch(Exception e){}

						pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
						mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, 
														"AndiCar BT Starter");
						mWakeLock.acquire();
						
						Intent i = new Intent(context, GPSTrackController.class);
						i.putExtra("Operation", "BT");
						Long carId = c.getLong(AddOnDBAdapter.ADDON_BTDEVICE_CAR_CAR_ID_POS);
						i.putExtra("CarID", carId);
						i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						context.startActivity(i);
						mWakeLock.release();
					}
					c.close();
					mDb.close();
				}
				else{
//	            	toast = Toast.makeText( context, "device null", Toast.LENGTH_LONG );
//	            	toast.show();
	            }
			}
		}
		catch(Exception e){
			e.printStackTrace();
			if(mWakeLock != null)
				mWakeLock.release();
		}
	}
}

