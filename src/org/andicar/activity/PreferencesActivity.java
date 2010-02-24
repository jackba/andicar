/*
    Copyright (C) 2009-2010 Miklos Keresztes - miklos.keresztes@gmail.com

    This program is free software; you can redistribute it and/or modify it
    under the terms of the GNU General Public License as published by the
    Free Software Foundation; either version 2 of the License.

    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
    without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
    See the GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along with this program;
    if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.andicar.activity;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import org.andicar.utils.Constants;
import org.andicar.persistence.MainDbAdapter;

/**
 *
 * @author miki
 */
public class PreferencesActivity extends PreferenceActivity {

    private Resources mRes = null;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        mRes = getResources();

        setPreferenceScreen(createPreferenceHierarchy());

    }

    private PreferenceScreen createPreferenceHierarchy() {

        PreferenceScreen prefScreenRoot = getPreferenceManager().createPreferenceScreen(this);

        //cars and drivers
        PreferenceCategory carDriverCategory = new PreferenceCategory(this);
        carDriverCategory.setTitle(mRes.getString(R.string.PREF_CARSDRIVER_CATEGORY_TITLE));
        prefScreenRoot.addPreference(carDriverCategory);
        //cars
        PreferenceScreen carPrefScreen = getPreferenceManager().createPreferenceScreen(this);
        carPrefScreen.setIntent(new Intent(this, CarListActivity.class));
        carPrefScreen.setTitle(mRes.getString(R.string.PREF_CARS_TITLE));
        carPrefScreen.setSummary(mRes.getString(R.string.PREF_CARS_SUMMARY));
        carDriverCategory.addPreference(carPrefScreen);
        //drivers
        PreferenceScreen driverPrefScreen = getPreferenceManager().createPreferenceScreen(this);
        driverPrefScreen.setIntent(new Intent(this, DriverListActivity.class));
        driverPrefScreen.setTitle(mRes.getString(R.string.PREF_DRIVERS_TITLE));
        driverPrefScreen.setSummary(mRes.getString(R.string.PREF_DRIVERS_SUMMARY));
        carDriverCategory.addPreference(driverPrefScreen);

        //uom's
        PreferenceCategory uomPrefCategory = new PreferenceCategory(this);
        uomPrefCategory.setTitle(mRes.getString( R.string.PREF_UOMS_CATEGORY_TITLE ));
        prefScreenRoot.addPreference(uomPrefCategory);
        //preference for length (distance) uom
        PreferenceScreen uomLengthPrefScreen = getPreferenceManager().createPreferenceScreen(this);
        Intent uomLengthIntent = new Intent(this, UOMListActivity.class);
        uomLengthIntent.putExtra( MainDbAdapter.UOM_COL_UOMTYPE_NAME, Constants.UOM_LENGTH_TYPE_CODE);
        uomLengthPrefScreen.setIntent(uomLengthIntent);
        uomLengthPrefScreen.setTitle(mRes.getString(R.string.PREF_UOMLENGTH_TITLE));
        uomLengthPrefScreen.setSummary(mRes.getString(R.string.PREF_UOMLENGTH_SUMMARY));
        uomPrefCategory.addPreference(uomLengthPrefScreen);
        //preference for volumne uom
        PreferenceScreen uomVolumePrefScreen = getPreferenceManager().createPreferenceScreen(this);
        Intent uomVolumeIntent = new Intent(this, UOMListActivity.class);
        uomVolumeIntent.putExtra( MainDbAdapter.UOM_COL_UOMTYPE_NAME, Constants.UOM_VOLUME_TYPE_CODE);
        uomVolumePrefScreen.setIntent(uomVolumeIntent);
        uomVolumePrefScreen.setTitle(mRes.getString(R.string.PREF_UOMVOLUME_TITLE));
        uomVolumePrefScreen.setSummary(mRes.getString(R.string.PREF_UOMVOLUME_SUMMARY));
        uomPrefCategory.addPreference(uomVolumePrefScreen);
        //uom conversions
        PreferenceScreen uomConversionPrefScreen = getPreferenceManager().createPreferenceScreen(this);
        uomConversionPrefScreen.setIntent(new Intent(this, UOMConversionListActivity.class));
        uomConversionPrefScreen.setTitle(mRes.getString(R.string.PREF_UOMCONVERSION_TITLE));
        uomConversionPrefScreen.setSummary(mRes.getString(R.string.PREF_UOMCONVERSION_SUMMARY));
        uomPrefCategory.addPreference(uomConversionPrefScreen);

        //Miscellaneous settings
        PreferenceCategory miscCategory = new PreferenceCategory(this);
        miscCategory.setTitle(mRes.getString(R.string.PREF_MISC_CATEGORY_TITLE));
        prefScreenRoot.addPreference(miscCategory);

        //expense types
        PreferenceScreen expTypePrefScreen = getPreferenceManager().createPreferenceScreen(this);
        expTypePrefScreen.setIntent(new Intent(this, ExpenseTypeListActivity.class));
        expTypePrefScreen.setTitle(mRes.getString(R.string.PREF_CAT_EXPTYPE_TITLE));
        expTypePrefScreen.setSummary(mRes.getString(R.string.PREF_CAT_EXPTYPE_SUMMARY));
        miscCategory.addPreference(expTypePrefScreen);

        //currencies
        PreferenceScreen currencyPrefScreen = getPreferenceManager().createPreferenceScreen(this);
        currencyPrefScreen.setIntent(new Intent(this, CurrencyListActivity.class));
        currencyPrefScreen.setTitle(mRes.getString(R.string.PREF_CAT_CURRENCYLIST_TITLE));
        currencyPrefScreen.setSummary(mRes.getString(R.string.PREF_CAT_CURRENCYLIST_SUMMARY));
        miscCategory.addPreference(currencyPrefScreen);

        return prefScreenRoot;
    }

}
