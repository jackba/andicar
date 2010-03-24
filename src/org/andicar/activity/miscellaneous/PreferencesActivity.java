/*
 *  AndiCar - a car management software for Android powered devices.
 *
 *  Copyright (C) 2010 Miklos Keresztes (miklos.keresztes@gmail.com)
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

package org.andicar.activity.miscellaneous;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import org.andicar.activity.CarListActivity;
import org.andicar.activity.CurrencyListActivity;
import org.andicar.activity.DriverListActivity;
import org.andicar.activity.ExpenseCategoryListActivity;
import org.andicar.activity.ExpenseTypeListActivity;
import org.andicar.activity.R;
import org.andicar.activity.UOMConversionListActivity;
import org.andicar.activity.UOMListActivity;
import org.andicar.utils.StaticValues;
import org.andicar.persistence.MainDbAdapter;

/**
 *
 * @author miki
 */
public class PreferencesActivity extends PreferenceActivity {

    private Resources mRes = null;
    protected SharedPreferences mPreferences;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mPreferences = getSharedPreferences(StaticValues.GLOBAL_PREFERENCE_NAME, 0);
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
        uomLengthIntent.putExtra( MainDbAdapter.UOM_COL_UOMTYPE_NAME, StaticValues.UOM_LENGTH_TYPE_CODE);
        uomLengthPrefScreen.setIntent(uomLengthIntent);
        uomLengthPrefScreen.setTitle(mRes.getString(R.string.PREF_UOMLENGTH_TITLE));
        uomLengthPrefScreen.setSummary(mRes.getString(R.string.PREF_UOMLENGTH_SUMMARY));
        uomPrefCategory.addPreference(uomLengthPrefScreen);
        //preference for volumne uom
        PreferenceScreen uomVolumePrefScreen = getPreferenceManager().createPreferenceScreen(this);
        Intent uomVolumeIntent = new Intent(this, UOMListActivity.class);
        uomVolumeIntent.putExtra( MainDbAdapter.UOM_COL_UOMTYPE_NAME, StaticValues.UOM_VOLUME_TYPE_CODE);
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

        //Backup/Restore
        PreferenceCategory bkRestoreCategory = new PreferenceCategory(this);
        bkRestoreCategory.setTitle(mRes.getString(R.string.PREF_BKRESTORE_CATEGORY_TITLE));
        prefScreenRoot.addPreference(bkRestoreCategory);
        PreferenceScreen bkRestorePrefScreen = getPreferenceManager().createPreferenceScreen(this);
        bkRestorePrefScreen.setIntent(new Intent(this, BackupRestoreActivity.class));
        bkRestorePrefScreen.setTitle(mRes.getString(R.string.PREF_BKRESTORE_TITLE));
        bkRestorePrefScreen.setSummary(mRes.getString(R.string.PREF_BKRESTORE_SUMMARY));
        bkRestoreCategory.addPreference(bkRestorePrefScreen);

        //Expenses settings
        PreferenceCategory expenseCategory = new PreferenceCategory(this);
        expenseCategory.setTitle(mRes.getString(R.string.PREF_EXPENSE_CATEGORY_TITLE));
        prefScreenRoot.addPreference(expenseCategory);

        //expense categories
        PreferenceScreen expCategoryPrefScreen = getPreferenceManager().createPreferenceScreen(this);
        expCategoryPrefScreen.setIntent(new Intent(this, ExpenseCategoryListActivity.class));
        expCategoryPrefScreen.setTitle(mRes.getString(R.string.PREF_CAT_EXPCATEGORY_TITLE));
        expCategoryPrefScreen.setSummary(mRes.getString(R.string.PREF_CAT_EXPCATEGORY_SUMMARY));
        expenseCategory.addPreference(expCategoryPrefScreen);

        //expense types
        PreferenceScreen expTypePrefScreen = getPreferenceManager().createPreferenceScreen(this);
        expTypePrefScreen.setIntent(new Intent(this, ExpenseTypeListActivity.class));
        expTypePrefScreen.setTitle(mRes.getString(R.string.PREF_CAT_EXPTYPE_TITLE));
        expTypePrefScreen.setSummary(mRes.getString(R.string.PREF_CAT_EXPTYPE_SUMMARY));
        expenseCategory.addPreference(expTypePrefScreen);

        //currencies
        PreferenceScreen currencyPrefScreen = getPreferenceManager().createPreferenceScreen(this);
        currencyPrefScreen.setIntent(new Intent(this, CurrencyListActivity.class));
        currencyPrefScreen.setTitle(mRes.getString(R.string.PREF_CAT_CURRENCYLIST_TITLE));
        currencyPrefScreen.setSummary(mRes.getString(R.string.PREF_CAT_CURRENCYLIST_SUMMARY));
        expenseCategory.addPreference(currencyPrefScreen);

        //Misc settings
        PreferenceCategory miscCategory = new PreferenceCategory(this);
        miscCategory.setTitle(mRes.getString(R.string.PREF_MISC_CATEGORY_TITLE));
        prefScreenRoot.addPreference(miscCategory);

        //main screen pref
        PreferenceScreen mainScreenPref = getPreferenceManager().createPreferenceScreen(this);
        mainScreenPref.setIntent(new Intent(this, MainScreenPreferenceActivity.class));
        mainScreenPref.setTitle(mRes.getString(R.string.PREF_CAT_MAINSCREENCATEGORY_TITLE));
        mainScreenPref.setSummary(mRes.getString(R.string.PREF_CAT_MAINSCREENCATEGORY_SUMMARY));
        miscCategory.addPreference(mainScreenPref);

        return prefScreenRoot;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mPreferences.getBoolean("MustClose", false)){
            finish();
        }
    }
}
