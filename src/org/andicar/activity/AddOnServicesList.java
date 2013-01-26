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


package org.andicar.activity;

import org.andicar.utils.StaticValues;
import org.andicar2.activity.R;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

/**
 *
 * @author miki
 */
public class AddOnServicesList extends PreferenceActivity {
	
	protected Resources mResource = null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mResource = getResources();
        setPreferenceScreen(createPreferenceHierarchy());
    }

    private PreferenceScreen createPreferenceHierarchy() {

        PreferenceManager prefMgr = getPreferenceManager();
        prefMgr.setSharedPreferencesName(StaticValues.GLOBAL_PREFERENCE_NAME);
        PreferenceScreen prefScreenRoot = prefMgr.createPreferenceScreen(this);

        
        PreferenceScreen secureBackup = getPreferenceManager().createPreferenceScreen(this);
        secureBackup.setIntent(new Intent(this, SecureBackupConfig.class));
        secureBackup.setTitle(mResource.getString(R.string.AddOn_SecureBackup_Title));
        secureBackup.setSummary(mResource.getString(R.string.AddOn_SecureBackup_Description));
        prefScreenRoot.addPreference(secureBackup);

        PreferenceScreen backupService = getPreferenceManager().createPreferenceScreen(this);
        backupService.setIntent(new Intent(this, BackupSchedule.class));
        backupService.setTitle(mResource.getString(R.string.AddOn_AutoBackupService_Title));
        backupService.setSummary(mResource.getString(R.string.AddOn_AutoBackupService_Description));
        prefScreenRoot.addPreference(backupService);
        
        PreferenceScreen dataEntryTemplate = getPreferenceManager().createPreferenceScreen(this);
        Intent i = new Intent(this, DataEntryTemplateList.class);
        i.putExtra("Class", "MEA");
        dataEntryTemplate.setIntent(i);
        dataEntryTemplate.setTitle(mResource.getString(R.string.AddOn_DataTemplate_Title));
        dataEntryTemplate.setSummary(mResource.getString(R.string.AddOn_DataTemplate_Description));
        prefScreenRoot.addPreference(dataEntryTemplate);

        PreferenceScreen btConnectionDetector = getPreferenceManager().createPreferenceScreen(this);
        btConnectionDetector.setIntent(new Intent(this, BTDeviceCarList.class));
        btConnectionDetector.setTitle(mResource.getString(R.string.AddOn_BTStarter_Title));
        btConnectionDetector.setSummary(mResource.getString(R.string.AddOn_BTStarter_Description));
        prefScreenRoot.addPreference(btConnectionDetector);

        return prefScreenRoot;
    }
}
