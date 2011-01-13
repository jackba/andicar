package org.andicar.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.andicar.addon.services.AndiCarAddOnServiceStarter;

public class AndiCarServiceStarter extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent rIntent) {
		if (rIntent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			//start AddOn services
			AndiCarAddOnServiceStarter.startServices(context);
		}
	}
	
//	public static void startServices(Context context){
//		boolean subsValid = false;
//		AlarmManager am;
//		try {
//			am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
//			Intent intent = null;
//			PendingIntent pIntent = null;
//
//			MainDbAdapter db = new MainDbAdapter(context);
//
//			try{
//				//start the subscription expire notification service
//				//check if at least one subscription to addons exists
//				Cursor c = db.query("SELECT * FROM " + AddOnDBObjectDef.ADDON_TABLE_NAME, null);
//				if(c.moveToFirst()){
//					intent = new Intent(context, CheckSubscriptionsService.class);
//					pIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//	
//					//check time will be set to 5:00 AM
//					Calendar checkTime = Calendar.getInstance();
//					//if the current hour > 5 AM set the next notification for tomorrow (add 1 day)
//					if(checkTime.get(Calendar.HOUR_OF_DAY) > 5)
//						checkTime.add(Calendar.DAY_OF_MONTH, 1);
//					checkTime.set(Calendar.HOUR_OF_DAY, 5);
//					checkTime.set(Calendar.MINUTE, 0);
//					am.setRepeating(AlarmManager.RTC_WAKEUP, checkTime.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pIntent);
//					Log.i(LOGTAG, "AddOn subscription check service scheduled. Next start:" + DateFormat.getDateFormat(context)
//							.format(checkTime.getTimeInMillis()) + " " + DateFormat.getTimeFormat(context)
//							.format(checkTime.getTimeInMillis()));
//				}
//				else
//					Log.i(LOGTAG, "No AddOn Subscriptions. Subscription check service not scheduled.");
//				c.close();
//			}
//			catch(Exception e){
//				e.printStackTrace();
//			}
//
//			//start the backup service if valid subscription exists
//			subsValid = ServiceSubscription.isSubscriptionValid(db, AddOnStaticValues.BACKUP_SERVICE_ID);
//			db.close();
//			if(subsValid){
//				intent = new Intent(context, BackupService.class);
//				intent.putExtra("Operation", "SetNextRun");
//				pIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//				am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), AlarmManager.INTERVAL_DAY, pIntent);
//				Log.i(LOGTAG, "Backup service scheduled.");
//			}
//			else
//				Log.i(LOGTAG, "No Backup service subscription. Backup service not scheduled.");
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

}
