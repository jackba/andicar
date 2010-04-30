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

package org.andicar.activity.preference;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import org.andicar.utils.StaticValues;
import org.andicar.activity.R;
import org.andicar.utils.AndiCarExceptionHandler;


/**
 *
 * @author Miklos Keresztes
 */
public class GPSPreferencesActivity extends PreferenceActivity {
    protected SharedPreferences mPreferences;
    private CheckBoxPreference ckpIsTrackKML;
    private CheckBoxPreference ckpIsTrackGPX;
    private boolean isSendCrashReport;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mPreferences = getSharedPreferences(StaticValues.GLOBAL_PREFERENCE_NAME, 0);
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

        PreferenceScreen gpsTrackFileFormatPref = getPreferenceManager().createPreferenceScreen(this);
        gpsTrackFileFormatPref.setTitle(R.string.PREF_GPSTRACK_FILEFORMAT_TITLE);
        gpsTrackFileFormatPref.setSummary(R.string.PREF_GPSTRACK_FILEFORMAT_SUMMARY);
        prefScreenRoot.addPreference(gpsTrackFileFormatPref);


        EditTextPreference tv = new EditTextPreference(this);
        tv.setTitle(R.string.PREF_GPSTRACK_FILEFORMATCSV_TITLE);
        tv.setEnabled(false);
        gpsTrackFileFormatPref.addPreference(tv);
        
        ckpIsTrackKML = new CheckBoxPreference(this);
        ckpIsTrackKML.setTitle(R.string.PREF_GPSTRACK_FILEFORMATKML_TITLE);
        ckpIsTrackKML.setSummary(R.string.PREF_GPSTRACK_FILEFORMATKML_SUMMARY);
        ckpIsTrackKML.setKey("IsUseKMLTrack");
        gpsTrackFileFormatPref.addPreference(ckpIsTrackKML);

        ckpIsTrackGPX = new CheckBoxPreference(this);
        ckpIsTrackGPX.setTitle(R.string.PREF_GPSTRACK_FILEFORMATGPX_TITLE);
        ckpIsTrackGPX.setSummary(R.string.PREF_GPSTRACK_FILEFORMATGPX_SUMMARY);
        ckpIsTrackGPX.setKey("IsUseGPXTrack");
        gpsTrackFileFormatPref.addPreference(ckpIsTrackGPX);

        // Minimum time  between two recordings
        ListPreference gpsTrackMinTimePref = new ListPreference(this);
        gpsTrackMinTimePref.setEntries(R.array.gpstrack_preference_mintime_entries);
        gpsTrackMinTimePref.setEntryValues(R.array.gpstrack_preference_mintime_values);
        gpsTrackMinTimePref.setDialogTitle(R.string.GEN_CHOOSEONE_DIALOGTITLE);
        gpsTrackMinTimePref.setKey("GPSTrackMinTime");
        gpsTrackMinTimePref.setTitle(R.string.PREF_GPSTRACK_MINTIME_TITLE);
        gpsTrackMinTimePref.setSummary(R.string.PREF_GPSTRACK_MINTIME_SUMMARY);
        prefScreenRoot.addPreference(gpsTrackMinTimePref);

        // Maximum deviation (accuracy)
        ListPreference gpsTrackMaxAccuracy = new ListPreference(this);
        gpsTrackMaxAccuracy.setEntries(R.array.gpstrack_preference_maxaccuracy_entries);
        gpsTrackMaxAccuracy.setEntryValues(R.array.gpstrack_preference_maxaccuracy_values);
        gpsTrackMaxAccuracy.setDialogTitle(R.string.GEN_CHOOSEONE_DIALOGTITLE);
        gpsTrackMaxAccuracy.setKey("GPSTrackMaxAccuracy");
        gpsTrackMaxAccuracy.setTitle(R.string.PREF_GPSTRACK_MAXACCURACY_TITLE);
        gpsTrackMaxAccuracy.setSummary(R.string.PREF_GPSTRACK_MAXACCURACY_SUMMARY);
        prefScreenRoot.addPreference(gpsTrackMaxAccuracy);

        // Maximum deviation (accuracy)
        ListPreference gpsTrackMaxAccuracyShutdownLimit = new ListPreference(this);
        gpsTrackMaxAccuracyShutdownLimit.setEntries(R.array.gpstrack_preference_maxaccuracyshutdownlimit_entries);
        gpsTrackMaxAccuracyShutdownLimit.setEntryValues(R.array.gpstrack_preference_maxaccuracyshutdownlimit_values);
        gpsTrackMaxAccuracyShutdownLimit.setDialogTitle(R.string.GEN_CHOOSEONE_DIALOGTITLE);
        gpsTrackMaxAccuracyShutdownLimit.setKey("GPSTrackMaxAccuracyShutdownLimit");
        gpsTrackMaxAccuracyShutdownLimit.setTitle(R.string.PREF_GPSTRACK_MAXACCURACYSHUTDOWNLIMIT_TITLE);
        gpsTrackMaxAccuracyShutdownLimit.setSummary(R.string.PREF_GPSTRACK_MAXACCURACYSHUTDOWNLIMIT_SUMMARY);
        prefScreenRoot.addPreference(gpsTrackMaxAccuracyShutdownLimit);

        return prefScreenRoot;
    }

}
