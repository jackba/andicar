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

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;

import org.andicar2.activity.R;
import org.andicar.activity.miscellaneous.MessageActivity;
import org.andicar.persistence.AddOnDBAdapter;
import org.andicar.persistence.FileUtils;
import org.andicar.persistence.MainDbAdapter;
import org.andicar.utils.AndiCarExceptionHandler;
import org.andicar.utils.Mail;
import org.andicar.utils.StaticValues;
import org.andicar.utils.Utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

public class FileMailer extends Service {

	NotificationManager mNM = null;
	Notification notification = null;
    protected Resources mResource = null;
    protected SharedPreferences mPreferences = null;
    protected String mMessage = "";
    protected int mLongMessageId = -1;
	File logFile = null;
    Intent mIntent;
	boolean sendReports = false;
	boolean sendGPSTracks = false;
	boolean isActive = false;
	boolean isShowNotification = true;
	String bkFile;
	String attachName;

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);

		if(getSharedPreferences(StaticValues.GLOBAL_PREFERENCE_NAME, Context.MODE_MULTI_PROCESS).getBoolean("SendCrashReport", true))
			Thread.setDefaultUncaughtExceptionHandler(
	                    new AndiCarExceptionHandler(Thread.getDefaultUncaughtExceptionHandler(), this));

		try{
			mIntent = intent;

			FileUtils fu = new FileUtils(this);
			fu.createFolderIfNotExists(FileUtils.TEMP_FOLDER);
			fu = null;
			
            logFile = new File(StaticValues.TEMP_FOLDER + "securebk.log");
	
			mNM = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
			mPreferences = getSharedPreferences(StaticValues.GLOBAL_PREFERENCE_NAME, Context.MODE_MULTI_PROCESS);
			mResource = getResources();
	
			Bundle extras = intent.getExtras();
	
			MainDbAdapter db = new MainDbAdapter(this);
			Cursor c = db.query(AddOnDBAdapter.ADDON_SECURE_BK_SETTINGS_TABLE_NAME, 
					AddOnDBAdapter.addonSecureBKSettingsTableColNames, null, null, null, null, null);
			if(c != null && c.moveToFirst()){
				isActive = c.getString(MainDbAdapter.GEN_COL_ISACTIVE_POS).equals("Y");
				isShowNotification = c.getString(AddOnDBAdapter.ADDON_SECURE_BK_SETTINGS_COL_ISSHOWNOTIF_POS).equals("Y");
				sendReports = c.getString(AddOnDBAdapter.ADDON_SECURE_BK_SETTINGS_COL_ISINCLUDEREPORTS_POS).equals("Y");
				sendGPSTracks = c.getString(AddOnDBAdapter.ADDON_SECURE_BK_SETTINGS_COL_ISINCLUDEGPS_POS).equals("Y");
				
				//send files only if the user configured the service
				isActive = isActive && c.getString(MainDbAdapter.GEN_COL_NAME_POS).equals("UserModified");
			}
			c.close();
			db.close();
			
			if(isActive){
				bkFile = extras.getString("bkFile");
				attachName = extras.getString("attachName");
				new Thread() {
					public void run() {
						String[] toEmailAddresses = { mPreferences.getString("bkFileToEmailAddress", "")};
						secureBackup(bkFile, attachName, sendReports, sendGPSTracks, toEmailAddresses, isShowNotification);
					}
				}.start();
			}
		}
		catch (Exception e) {}
	}

	public boolean secureBackup(String bkFile, String attachName, boolean sendReports, boolean sendGPSTracks, String[] toEmailAddresses, boolean isShowNotification) {
		boolean retVal = false;
	    FileWriter fw = null;
	    
		SharedPreferences.Editor editor = mPreferences.edit();

		try {
            fw = new FileWriter(logFile);
			
			fw.append("\n" + Utils.getDateStr(true, true, true) + "  starting secureBK");
			fw.flush();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		String smtpAuthUserName = mPreferences.getString("smtpAuthUserName", "");
		if(smtpAuthUserName.length() == 0){
			mMessage = mResource.getString(R.string.ERR_052);
			mLongMessageId = R.string.AddOn_SecureBackup_Configure;
			showNotification(StaticValues.NOTIF_SECURE_BACKUP_ERROR_ID);
			return false;
		}
		String smtpAuthPassword = mPreferences.getString("smtpAuthPassword", ""); 
		if(smtpAuthPassword.length() == 0){
			mMessage = mResource.getString(R.string.ERR_053);
			mLongMessageId = R.string.AddOn_SecureBackup_Configure;
			showNotification(StaticValues.NOTIF_SECURE_BACKUP_ERROR_ID);
			return false;
		}
		if(toEmailAddresses == null || toEmailAddresses.length == 0){
			mMessage = mResource.getString(R.string.ERR_054);
			mLongMessageId = R.string.AddOn_SecureBackup_Configure;
			showNotification(StaticValues.NOTIF_SECURE_BACKUP_ERROR_ID);
			return false;
		}

		editor.putString("SecureBkFile", bkFile);
		editor.putString("SecureBkAttName", attachName);
		editor.putBoolean("SecureBkSuccess", false);
		editor.commit();

		TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		
		String subject = mResource.getString(R.string.AddOn_SecureBackup_MailSubject);
		if(tm.getDeviceId() != null)
			subject = subject + " - device ID: " + tm.getDeviceId();
        Mail m = new Mail(smtpAuthUserName, smtpAuthPassword);
		m.setTo(toEmailAddresses);
		m.setFrom(smtpAuthUserName);
		m.setSubject(subject);
		m.setBody("Sent by AndiCar - http://www.andicar.org");

		String bkZipFile = StaticValues.TEMP_FOLDER + attachName + ".zip";
		//zip the backup
		Bundle bkFileBundle = new Bundle();
		bkFileBundle.putString(attachName, bkFile);
		try {
			if(FileUtils.zipFiles(bkFileBundle, bkZipFile) != null){
				m.addAttachment(bkZipFile, attachName + ".zip");
				if(sendReports){
					ArrayList<String> reportFiles = FileUtils.getFileNames(StaticValues.REPORT_FOLDER, null);
					if(reportFiles != null && reportFiles.size() > 0){
						Bundle reportFilesBundle = new Bundle();
						String reportFile = "";
						for (Iterator<String> iterator = reportFiles.iterator(); iterator.hasNext();) {
							reportFile = (String) iterator.next();
							reportFilesBundle.putString(reportFile, StaticValues.REPORT_FOLDER + reportFile);
						}
						String reportZipFile = StaticValues.TEMP_FOLDER + "reports.zip";
						if(FileUtils.zipFiles(reportFilesBundle, reportZipFile) != null){
							m.addAttachment(reportZipFile, "reports.zip");
						}
					}
				}
				if(sendGPSTracks){
					ArrayList<String> gpsTrackFiles = FileUtils.getFileNames(StaticValues.TRACK_FOLDER, null);
					if(gpsTrackFiles != null && gpsTrackFiles.size() > 0){
						Bundle reportFilesBundle = new Bundle();
						String gpsTrackFile = "";
						for (Iterator<String> iterator = gpsTrackFiles.iterator(); iterator.hasNext();) {
							gpsTrackFile = (String) iterator.next();
							reportFilesBundle.putString(gpsTrackFile, StaticValues.TRACK_FOLDER + gpsTrackFile);
						}
						String gpsTrackZipFile = StaticValues.TEMP_FOLDER + "gpsTracks.zip";
						if(FileUtils.zipFiles(reportFilesBundle, gpsTrackZipFile) != null){
							m.addAttachment(gpsTrackZipFile, "gpsTracks.zip");
						}
					}
				}
				//check if connection network is up
				ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
				NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
				boolean netAvailable = (wifi.isAvailable() || mobile.isAvailable());
				int waitCount = 0;
				fw.append("\n" + Utils.getDateStr(true, true, true) + "  network available (first try) = " + netAvailable);
				Log.i("FileMailer", "network available (first try) = " + netAvailable);
				while(!netAvailable && waitCount < 10){
					fw.append("\n" + Utils.getDateStr(true, true, true) + "  waiting count = " + (waitCount + 1));
					fw.flush();
					Log.i("FileMailer", "waiting count = " + (waitCount + 1));
					Thread.sleep(1000); //wait 1 second
					wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
					mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
					netAvailable = (wifi.isAvailable() || mobile.isAvailable());
					fw.append("\n" + Utils.getDateStr(true, true, true) + "  network available = " + netAvailable);
					fw.flush();
					Log.i("FileMailer", "network available = " + netAvailable);
					waitCount++;
				}
					
				for(int i = 0; i < 5;  i++){
					try{
						fw.append("\n" + Utils.getDateStr(true, true, true) + "  try count to send mail = " + i);
						fw.flush();
						if(m.send()){
							mMessage = mResource.getString(R.string.AddOn_SecureBackup_MailSent);
							mLongMessageId = -1;
							if(isShowNotification)
								showNotification(StaticValues.NOTIF_SECURE_BACKUP_INFO_ID);
							retVal = true;
							editor.putBoolean("SecureBkSuccess", true);
							editor.commit();
							break;
						}
					}
					catch(Exception e){
						if(i == 4){
							throw new Exception(e);
						}
					}
				}
			}
			else{
				mMessage = mResource.getString(R.string.ERR_057) + " " + mResource.getString(R.string.ERR_056); //"Failed to send mail. Unknow error.";
				showNotification(StaticValues.NOTIF_SECURE_BACKUP_ERROR_ID);
			}
            fw.flush();
            fw.close();

		} catch (Exception e) {
			mMessage = mResource.getString(R.string.ERR_055) + " " + e.getMessage(); //"Failed to send mail. ...
			showNotification(StaticValues.NOTIF_SECURE_BACKUP_ERROR_ID);
		}
		FileUtils.deleteFile(bkZipFile);
		return retVal;
	}
	
	private void showNotification(int notifId) {
		Intent i = new Intent(this, MessageActivity.class);
		i.putExtra("MessageTitle", "AndiCar secure backup");
		i.putExtra("MessageBody", mMessage);
		i.putExtra("LongMessageID", mLongMessageId);
		int iconId = 0;
		if(notifId == StaticValues.NOTIF_SECURE_BACKUP_ERROR_ID)
			iconId = R.drawable.icon_sys_error;
		else
			iconId = R.drawable.icon_sys_info;
		notification = new Notification(iconId, mMessage, System.currentTimeMillis());
		notification.flags |= Notification.DEFAULT_LIGHTS;
		notification.flags |= Notification.DEFAULT_SOUND;
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.setLatestEventInfo(this, "AndiCar secure backup", mMessage, 
									PendingIntent.getActivity(this, StaticValues.ACTIVITY_REQUEST_CODE_FILEMAILER, i, PendingIntent.FLAG_UPDATE_CURRENT));
		try{
			mNM.notify(notifId, notification);
		}catch(IllegalArgumentException e){}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
}
