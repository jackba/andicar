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

package org.andicar.activity.miscellaneous;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
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
    private Resources mRes = null;
    protected SharedPreferences mPreferences;
    private CheckBoxPreference ckpIsTrackKML;
    private CheckBoxPreference ckpIsTrackGPX;
    private CheckBoxPreference ckpIsTrackOnMap;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mPreferences = getSharedPreferences(StaticValues.GLOBAL_PREFERENCE_NAME, 0);
        boolean isSendStatistics = mPreferences.getBoolean("SendUsageStatistics", true);
        if(isSendStatistics)
            Thread.setDefaultUncaughtExceptionHandler(
                    new AndiCarExceptionHandler(Thread.getDefaultUncaughtExceptionHandler(), this));

        mRes = getResources();
        setPreferenceScreen(createPreferenceHierarchy());
    }

    private PreferenceScreen createPreferenceHierarchy() {
        PreferenceManager prefMgr = getPreferenceManager();
        prefMgr.setSharedPreferencesName(StaticValues.GLOBAL_PREFERENCE_NAME);
        PreferenceScreen prefScreenRoot = prefMgr.createPreferenceScreen(this);

        ckpIsTrackOnMap = new CheckBoxPreference(this);
        ckpIsTrackOnMap.setKey("IsGPSTrackOnMap");
        ckpIsTrackOnMap.setTitle(mRes.getString(R.string.PREF_GPSTRACK_SHOWONMAP_TITLE));
        ckpIsTrackOnMap.setSummary(mRes.getString(R.string.PREF_GPSTRACK_SHOWONMAP_SUMMARY));
        prefScreenRoot.addPreference(ckpIsTrackOnMap);

        PreferenceScreen gpsTrackFileFormatPref = getPreferenceManager().createPreferenceScreen(this);
        gpsTrackFileFormatPref.setTitle(R.string.PREF_GPSTRACK_FILEFORMAT_TITLE);
        gpsTrackFileFormatPref.setSummary(R.string.PREF_GPSTRACK_FILEFORMAT_SUMMARY);
        prefScreenRoot.addPreference(gpsTrackFileFormatPref);

//        gpsTrackCSVFileFormatCk = new CheckBoxPreference(this);
//        gpsTrackCSVFileFormatCk.setTitle(R.string.PREF_GPSTRACK_FILEFORMATCSV_TITLE);
//        gpsTrackCSVFileFormatCk.setSummary(R.string.PREF_GPSTRACK_FILEFORMATCSV_SUMMARY);
//        gpsTrackCSVFileFormatCk.setKey("IsUseCSVTrack");
//        gpsTrackCSVFileFormatCk.setOnPreferenceClickListener(gpsTrackFileFormat);
//        gpsTrackFileFormatPref.addPreference(gpsTrackCSVFileFormatCk);

        ckpIsTrackKML = new CheckBoxPreference(this);
        ckpIsTrackKML.setTitle(R.string.PREF_GPSTRACK_FILEFORMATKML_TITLE);
        ckpIsTrackKML.setSummary(R.string.PREF_GPSTRACK_FILEFORMATKML_SUMMARY);
        ckpIsTrackKML.setKey("IsUseKMLTrack");
//        gpsTrackKMLFileFormatCk.setOnPreferenceClickListener(gpsTrackFileFormat);
        gpsTrackFileFormatPref.addPreference(ckpIsTrackKML);

        ckpIsTrackGPX = new CheckBoxPreference(this);
        ckpIsTrackGPX.setTitle(R.string.PREF_GPSTRACK_FILEFORMATGPX_TITLE);
        ckpIsTrackGPX.setSummary(R.string.PREF_GPSTRACK_FILEFORMATGPX_SUMMARY);
        ckpIsTrackGPX.setKey("IsUseGPXTrack");
//        gpsTrackGPXFileFormatCk.setOnPreferenceClickListener(gpsTrackFileFormat);
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

        // Minimum distance between two recordings
        ListPreference gpsTrackMinDistPref = new ListPreference(this);
        gpsTrackMinDistPref.setEntries(R.array.gpstrack_preference_mindistance_entries);
        gpsTrackMinDistPref.setEntryValues(R.array.gpstrack_preference_mindistance_values);
        gpsTrackMinDistPref.setDialogTitle(R.string.GEN_CHOOSEONE_DIALOGTITLE);
        gpsTrackMinDistPref.setKey("GPSTrackMinDistance");
        gpsTrackMinDistPref.setTitle(R.string.PREF_GPSTRACK_MINDISTANCE_TITLE);
        gpsTrackMinDistPref.setSummary(R.string.PREF_GPSTRACK_MINDISTANCE_SUMMARY);
        prefScreenRoot.addPreference(gpsTrackMinDistPref);

        return prefScreenRoot;
    }

//    OnPreferenceClickListener gpsTrackFileFormat = new OnPreferenceClickListener() {
//            public boolean onPreferenceClick(Preference prfrnc) {
//                if(!gpsTrackGPXFileFormatCk.isChecked()
////                        && !gpsTrackCSVFileFormatCk.isChecked()
//                        && !gpsTrackKMLFileFormatCk.isChecked()){
//                    Toast.makeText(GPSPreferencesActivity.this, mRes.getString(R.string.GEN_CHOOSEONE_MESSAGE), Toast.LENGTH_SHORT).show();
//                    ((CheckBoxPreference)prfrnc).setChecked(true);
//                }
//                return true;
//            }
//        };

}
