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
import android.preference.DialogPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.widget.Toast;
import org.andicar.utils.StaticValues;
import org.andicar.activity.R;


/**
 *
 * @author Miklos Keresztes
 */
public class GPSPreferencesActivity extends PreferenceActivity {
    private Resources mRes = null;
    protected SharedPreferences mPreferences;
    private CheckBoxPreference gpsTrackCSVFileFormatCk;
    private CheckBoxPreference gpsTrackKMLFileFormatCk;
    private CheckBoxPreference gpsTrackGPXFileFormatCk;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mPreferences = getSharedPreferences(StaticValues.GLOBAL_PREFERENCE_NAME, 0);
        mRes = getResources();
        setPreferenceScreen(createPreferenceHierarchy());
    }

    private PreferenceScreen createPreferenceHierarchy() {
        PreferenceManager prefMgr = getPreferenceManager();
        prefMgr.setSharedPreferencesName(StaticValues.GLOBAL_PREFERENCE_NAME);
        PreferenceScreen prefScreenRoot = prefMgr.createPreferenceScreen(this);

        CheckBoxPreference gpsTrackOnMapCk = new CheckBoxPreference(this);
        gpsTrackOnMapCk.setKey("IsGPSTrackOnMap");
        gpsTrackOnMapCk.setTitle(mRes.getString(R.string.PREF_GPSTRACK_SHOWONMAP_TITLE));
        gpsTrackOnMapCk.setSummary(mRes.getString(R.string.PREF_GPSTRACK_SHOWONMAP_SUMMARY));
        prefScreenRoot.addPreference(gpsTrackOnMapCk);

        PreferenceScreen gpsTrackFileFormatPref = getPreferenceManager().createPreferenceScreen(this);
        gpsTrackFileFormatPref.setTitle(R.string.PREF_GPSTRACK_FILEFORMAT_TITLE);
        gpsTrackFileFormatPref.setSummary(R.string.PREF_GPSTRACK_FILEFORMAT_SUMMARY);
        prefScreenRoot.addPreference(gpsTrackFileFormatPref);

        gpsTrackCSVFileFormatCk = new CheckBoxPreference(this);
        gpsTrackCSVFileFormatCk.setTitle(R.string.PREF_GPSTRACK_FILEFORMATCSV_TITLE);
        gpsTrackCSVFileFormatCk.setSummary(R.string.PREF_GPSTRACK_FILEFORMATCSV_SUMMARY);
        gpsTrackCSVFileFormatCk.setKey("IsUseCSVTrack");
        gpsTrackCSVFileFormatCk.setOnPreferenceClickListener(gpsTrackFileFormat);
        gpsTrackFileFormatPref.addPreference(gpsTrackCSVFileFormatCk);

        gpsTrackKMLFileFormatCk = new CheckBoxPreference(this);
        gpsTrackKMLFileFormatCk.setTitle(R.string.PREF_GPSTRACK_FILEFORMATKML_TITLE);
        gpsTrackKMLFileFormatCk.setSummary(R.string.PREF_GPSTRACK_FILEFORMATKML_SUMMARY);
        gpsTrackKMLFileFormatCk.setKey("IsUseKMLTrack");
        gpsTrackKMLFileFormatCk.setOnPreferenceClickListener(gpsTrackFileFormat);
        gpsTrackFileFormatPref.addPreference(gpsTrackKMLFileFormatCk);

        gpsTrackGPXFileFormatCk = new CheckBoxPreference(this);
        gpsTrackGPXFileFormatCk.setTitle(R.string.PREF_GPSTRACK_FILEFORMATGPX_TITLE);
        gpsTrackGPXFileFormatCk.setSummary(R.string.PREF_GPSTRACK_FILEFORMATGPX_SUMMARY);
        gpsTrackGPXFileFormatCk.setKey("IsUseGPXTrack");
        gpsTrackGPXFileFormatCk.setOnPreferenceClickListener(gpsTrackFileFormat);
        gpsTrackFileFormatPref.addPreference(gpsTrackGPXFileFormatCk);

        return prefScreenRoot;
    }

    OnPreferenceClickListener gpsTrackFileFormat = new OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference prfrnc) {
                if(!gpsTrackGPXFileFormatCk.isChecked()
                        && !gpsTrackCSVFileFormatCk.isChecked()
                        && !gpsTrackKMLFileFormatCk.isChecked()){
                    Toast.makeText(GPSPreferencesActivity.this, mRes.getString(R.string.GEN_CHOOSEONE_MESSAGE), Toast.LENGTH_SHORT).show();
                    ((CheckBoxPreference)prfrnc).setChecked(true);
                }
                return true;
            }
        };

}
