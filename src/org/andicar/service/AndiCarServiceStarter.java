package org.andicar.service;

import org.andicar.utils.AndiCarExceptionHandler;
import org.andicar.utils.StaticValues;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.andicar.addon.services.AndiCarAddOnServiceStarter;

public class AndiCarServiceStarter extends BroadcastReceiver {

	private static final String LOGTAG = "AndiCarServiceStarter";

	@Override
	public void onReceive(Context context, Intent rIntent) {
		if(context.getSharedPreferences(StaticValues.GLOBAL_PREFERENCE_NAME, 0).getBoolean("SendCrashReport", true))
			Thread.setDefaultUncaughtExceptionHandler(
	                    new AndiCarExceptionHandler(Thread.getDefaultUncaughtExceptionHandler(), context));
		try{
			if (rIntent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
					//start services
					startServices(context);
					//start AddOn services
					AndiCarAddOnServiceStarter.startServices(context);
			}
			else if(rIntent.getAction().equals(Intent.ACTION_DATE_CHANGED)){
				startServices(context);
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void startServices(Context context) throws Exception{
		Log.i(LOGTAG, "Starting To-Do Notification Service...");
		Intent intent = new Intent(context, ToDoNotificationService.class);
		intent.putExtra("setJustNextRun", false);
		context.startService(intent);
		Log.i(LOGTAG, "Done");
	}

}
