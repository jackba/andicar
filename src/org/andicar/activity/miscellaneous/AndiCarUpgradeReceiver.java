 /**
 * AndiCar - car management software for Android powered devices
 * Copyright (C) 2010 Miklos Keresztes (miklos.keresztes@gmail.com)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT AY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.andicar.activity.miscellaneous;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * @author Miklos Keresztes
 *
 */
public class AndiCarUpgradeReceiver extends BroadcastReceiver {

	/* (non-Javadoc)
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent rIntent) {
		if (rIntent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED)){
			Log.i("AndiCarUpgradeReceiver", "Upgrade detected.");
//			Resources mRes = context.getResources();
//            AlertDialog.Builder builder = new AlertDialog.Builder(context);
//            builder.setTitle("Whats news");
//            builder.setMessage("Test");
//            builder.setCancelable(false);
//            builder.setPositiveButton(mRes.getString(R.string.GEN_OK),
//                    new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int id) {
//                            dialog.cancel();
//                        }
//                    });
//            AlertDialog alert = builder.create();
//            alert.show();
		}

	}

}
