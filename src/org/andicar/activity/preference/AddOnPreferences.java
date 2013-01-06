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

package org.andicar.activity.preference;

import org.andicar.activity.BTDeviceCarList;
import org.andicar.activity.BackupSchedule;
import org.andicar.activity.DataEntryTemplateList;
import org.andicar2.activity.R;
import org.andicar.activity.SecureBackupConfig;
import org.andicar.utils.AndiCarExceptionHandler;
import org.andicar.utils.StaticValues;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;


/**
 *
 * @author Miklos Keresztes
 */
public class AddOnPreferences extends PreferenceActivity {
    protected SharedPreferences mPreferences;
    private boolean isSendCrashReport;
    private Resources mRes;
    

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mPreferences = getSharedPreferences(StaticValues.GLOBAL_PREFERENCE_NAME, 0);
        mRes = getResources();
        isSendCrashReport = mPreferences.getBoolean("SendCrashReport", true);
        if(isSendCrashReport)
            Thread.setDefaultUncaughtExceptionHandler(
                    new AndiCarExceptionHandler(Thread.getDefaultUncaughtExceptionHandler(), this));

        setPreferenceScreen(createPreferenceHierarchy());
    }

    private PreferenceScreen createPreferenceHierarchy() {
        PreferenceManager prefMgr = getPreferenceManager();
        prefMgr.setSharedPreferencesName(StaticValues.GLOBAL_PREFERENCE_NAME);
        PreferenceScreen prefScreenRoot = prefMgr.createPreferenceScreen(this);
        PreferenceScreen addOnPrefScreen;
        try {
			addOnPrefScreen = getPreferenceManager().createPreferenceScreen(this);
			addOnPrefScreen.setIntent(new Intent(this, BackupSchedule.class));
			addOnPrefScreen.setTitle(mRes.getString(R.string.AddOn_AutoBackupService_ScheduleTitle));
			addOnPrefScreen.setSummary(mRes.getString(R.string.AddOn_AutoBackupService_Description));
	        prefScreenRoot.addPreference(addOnPrefScreen);

	        addOnPrefScreen = getPreferenceManager().createPreferenceScreen(this);
			addOnPrefScreen.setIntent(new Intent(this, SecureBackupConfig.class));
			addOnPrefScreen.setTitle(mRes.getString(R.string.AddOn_SecureBackup_Title));
			addOnPrefScreen.setSummary(mRes.getString(R.string.AddOn_SecureBackup_Description));
	        prefScreenRoot.addPreference(addOnPrefScreen);

	        addOnPrefScreen = getPreferenceManager().createPreferenceScreen(this);
			addOnPrefScreen.setIntent(new Intent(this, BTDeviceCarList.class));
			addOnPrefScreen.setTitle(mRes.getString(R.string.AddOn_BTStarter_Title));
			addOnPrefScreen.setSummary(mRes.getString(R.string.AddOn_BTStarter_Description));
	        prefScreenRoot.addPreference(addOnPrefScreen);

	        PreferenceCategory dataEntryCategory = new PreferenceCategory(this);
	        dataEntryCategory.setTitle(mRes.getString(R.string.AddOn_DataEntryPref_CategoryTitle));
	        prefScreenRoot.addPreference(dataEntryCategory);

	        addOnPrefScreen = getPreferenceManager().createPreferenceScreen(this);
	        Intent i = new Intent(this, DataEntryTemplateList.class);
	        i.putExtra("Class", "MEA");
			addOnPrefScreen.setIntent(i);
			addOnPrefScreen.setTitle(mRes.getString(R.string.AddOn_DataEntryPref_MileageTitle));
//					addOnPrefScreen.setSummary(mRes.getString(R.string.AddOn_SecureBackup_Description));
			dataEntryCategory.addPreference(addOnPrefScreen);
	        
	        addOnPrefScreen = getPreferenceManager().createPreferenceScreen(this);
			i = new Intent(this, DataEntryTemplateList.class);
	        i.putExtra("Class", "REA");
			addOnPrefScreen.setIntent(i);
			addOnPrefScreen.setTitle(mRes.getString(R.string.AddOn_DataEntryPref_RefuelTitle));
//					addOnPrefScreen.setSummary(mRes.getString(R.string.AddOn_SecureBackup_Description));
			dataEntryCategory.addPreference(addOnPrefScreen);
	        
	        addOnPrefScreen = getPreferenceManager().createPreferenceScreen(this);
			i = new Intent(this, DataEntryTemplateList.class);
	        i.putExtra("Class", "EEA");
			addOnPrefScreen.setIntent(i);
			addOnPrefScreen.setTitle(mRes.getString(R.string.AddOn_DataEntryPref_ExpenseTitle));
//					addOnPrefScreen.setSummary(mRes.getString(R.string.AddOn_SecureBackup_Description));
			dataEntryCategory.addPreference(addOnPrefScreen);
	        
	        addOnPrefScreen = getPreferenceManager().createPreferenceScreen(this);
			i = new Intent(this, DataEntryTemplateList.class);
	        i.putExtra("Class", "GTC");
			addOnPrefScreen.setIntent(i);
			addOnPrefScreen.setTitle(mRes.getString(R.string.AddOn_DataEntryPref_GPSTitle));
//					addOnPrefScreen.setSummary(mRes.getString(R.string.AddOn_SecureBackup_Description));
			dataEntryCategory.addPreference(addOnPrefScreen);

			PreferenceCategory dataEntryCategoryEndBand = new PreferenceCategory(this);
	        dataEntryCategoryEndBand.setTitle("");
	        prefScreenRoot.addPreference(dataEntryCategoryEndBand);
        } catch (Exception e) {
			e.printStackTrace();
		}
        return prefScreenRoot;
    }

}
