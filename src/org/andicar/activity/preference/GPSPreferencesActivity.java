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
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.widget.Toast;
import org.andicar.utils.StaticValues;
import org.andicar2.activity.R;
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

        PreferenceScreen gpsTrackFileFormatPref = getPreferenceManager().createPreferenceScreen(this);
        gpsTrackFileFormatPref.setTitle(R.string.PREFGPSTrack_FileFormatTitle);
        gpsTrackFileFormatPref.setSummary(R.string.PREFGPSTrack_FileFormatSummary);
        prefScreenRoot.addPreference(gpsTrackFileFormatPref);


        EditTextPreference tv = new EditTextPreference(this);
        tv.setTitle(R.string.PREFGPSTrack_CSVFileFormatMessage);
        tv.setEnabled(false);
        gpsTrackFileFormatPref.addPreference(tv);
        
        ckpIsTrackKML = new CheckBoxPreference(this);
        ckpIsTrackKML.setTitle(R.string.PREFGPSTrack_KMLFileFormatTitle);
        ckpIsTrackKML.setSummary(R.string.PREFGPSTrack_KMLFileFormatSummary);
        ckpIsTrackKML.setKey("IsUseKMLTrack");
        gpsTrackFileFormatPref.addPreference(ckpIsTrackKML);

        ckpIsTrackGPX = new CheckBoxPreference(this);
        ckpIsTrackGPX.setTitle(R.string.PREFGPSTrack_GPXFileFormatTitle);
        ckpIsTrackGPX.setSummary(R.string.PREFGPSTrack_GPXFileFormatSummary);
        ckpIsTrackGPX.setKey("IsUseGPXTrack");
        gpsTrackFileFormatPref.addPreference(ckpIsTrackGPX);

        EditTextPreference tv2 = new EditTextPreference(this);
        tv2.setSummary(R.string.PREFGPSTrack_FileLocationMessage);
        tv2.setEnabled(false);
        gpsTrackFileFormatPref.addPreference(tv2);

        // Minimum time  between two recordings
        ListPreference gpsTrackMinTimePref = new ListPreference(this);
        gpsTrackMinTimePref.setEntries(R.array.gpstrack_preference_mintime_entries);
        gpsTrackMinTimePref.setEntryValues(R.array.gpstrack_preference_mintime_values);
        gpsTrackMinTimePref.setDialogTitle(R.string.GEN_ChooseOneTitle);
        gpsTrackMinTimePref.setKey("GPSTrackMinTime");
        gpsTrackMinTimePref.setTitle(R.string.PREFGPSTrack_MinimumTimeTitle);
        gpsTrackMinTimePref.setSummary(R.string.PREFGPSTrack_MinimumTimeSummary);
        prefScreenRoot.addPreference(gpsTrackMinTimePref);

        // Maximum deviation (accuracy)
        ListPreference gpsTrackMaxAccuracy = new ListPreference(this);
        gpsTrackMaxAccuracy.setEntries(R.array.gpstrack_preference_maxaccuracy_entries);
        gpsTrackMaxAccuracy.setEntryValues(R.array.gpstrack_preference_maxaccuracy_values);
        gpsTrackMaxAccuracy.setDialogTitle(R.string.GEN_ChooseOneTitle);
        gpsTrackMaxAccuracy.setKey("GPSTrackMaxAccuracy");
        gpsTrackMaxAccuracy.setTitle(R.string.PREFGPSTrack_AccuracyTitle);
        gpsTrackMaxAccuracy.setSummary(R.string.PREFGPSTrack_AccuracySummary);
        prefScreenRoot.addPreference(gpsTrackMaxAccuracy);

        // Maximum deviation (accuracy)
        //temporarry disabled
//        ListPreference gpsTrackMaxAccuracyShutdownLimit = new ListPreference(this);
//        gpsTrackMaxAccuracyShutdownLimit.setEntries(R.array.gpstrack_preference_maxaccuracyshutdownlimit_entries);
//        gpsTrackMaxAccuracyShutdownLimit.setEntryValues(R.array.gpstrack_preference_maxaccuracyshutdownlimit_values);
//        gpsTrackMaxAccuracyShutdownLimit.setDialogTitle(R.string.GEN_ChooseOneTitle);
//        gpsTrackMaxAccuracyShutdownLimit.setKey("GPSTrackMaxAccuracyShutdownLimit");
//        gpsTrackMaxAccuracyShutdownLimit.setTitle(R.string.PREFGPSTrack_AutoShutDownTitle);
//        gpsTrackMaxAccuracyShutdownLimit.setSummary(R.string.PREFGPSTrack_AutoShutDownSummary);
//        prefScreenRoot.addPreference(gpsTrackMaxAccuracyShutdownLimit);

        EditTextPreference tpSplitFile = new EditTextPreference(this);
        tpSplitFile.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            //check the value entered. must be an integer.
            public boolean onPreferenceChange(Preference prfrnc, Object o) {
                try{
                    Integer.parseInt((String)o);
                }
                catch(NumberFormatException e){
                    Toast.makeText(GPSPreferencesActivity.this, mRes.getString(R.string.GEN_NumberFormatException), Toast.LENGTH_SHORT).show();
                    return false;
                }
                return true;
            }
        });
        tpSplitFile.setTitle(R.string.PREFGPSTrack_FileSplitTitle);
        tpSplitFile.setSummary(R.string.PREFGPSTrack_FileSplitSummary);
        tpSplitFile.setKey("GPSTrackTrackFileSplitCount");
        prefScreenRoot.addPreference(tpSplitFile);

        return prefScreenRoot;
    }

}
