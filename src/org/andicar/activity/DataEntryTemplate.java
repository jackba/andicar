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
/**
 * 
 */
package org.andicar.activity;

import java.util.Iterator;
import java.util.Set;

import org.andicar2.activity.R;
import org.andicar.activity.miscellaneous.GPSTrackController;
import org.andicar.persistence.AddOnDBAdapter;
import org.andicar.persistence.MainDbAdapter;
import org.andicar.utils.AndiCarExceptionHandler;
import org.andicar.utils.AndiCarStatistics;
import org.andicar.utils.StaticValues;

import android.content.Context;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * @author miki
 *
 */
public class DataEntryTemplate{
	
	private MainDbAdapter mDbAdapter = null;
	private EditActivityBase mActivity = null;

	private ImageButton btnUpdate = null;
	private ImageButton btnNew = null;
	private Spinner spnTemplate = null;
	private LinearLayout llTemplateZone = null;
	private long mTemplateID = -1;
	
	private boolean isSendCrashReport = true;
	private boolean isSendStatistics = true;

	private SharedPreferences mPreferences = null;
	private Resources mResource = null;

	/**
	 * the edit activity class:<br>
	 * <li><b>EEA:<b> ExpenseEditActivity
	 * <li><b>MEA:<b> MileageEditActivity
	 * <li><b>REA:<b> RefuelEditActivity
	 * <li><b>GTC:<b> GPSTrackController 
	 */
	private String mActivityClass = null;

	public DataEntryTemplate(EditActivityBase ea, MainDbAdapter db) {
		mActivity = ea;
		mPreferences = mActivity.getSharedPreferences(StaticValues.GLOBAL_PREFERENCE_NAME, Context.MODE_MULTI_PROCESS);
		
		mResource = mActivity.getResources();
		
        isSendCrashReport = mPreferences.getBoolean("SendCrashReport", true);
        isSendStatistics = mPreferences.getBoolean("SendUsageStatistics", true);
        
        if(isSendCrashReport)
            Thread.setDefaultUncaughtExceptionHandler(
                    new AndiCarExceptionHandler(Thread.getDefaultUncaughtExceptionHandler(), mActivity));

        btnNew = (ImageButton)mActivity.findViewById(R.id.btnNew);
		btnUpdate = (ImageButton)mActivity.findViewById(R.id.btnUpdate);
		spnTemplate = (Spinner)mActivity.findViewById(R.id.spnTemplate);
		llTemplateZone = (LinearLayout)mActivity.findViewById(R.id.llTemplateZone);
		if(llTemplateZone != null)
			llTemplateZone.setVisibility(View.VISIBLE);
		
		mDbAdapter = db;
		if(mActivity instanceof ExpenseEditActivity)
			mActivityClass = "EEA";
		else if(mActivity instanceof MileageEditActivity)
			mActivityClass = "MEA";
		else if(mActivity instanceof RefuelEditActivity)
			mActivityClass = "REA";
		else if(mActivity instanceof GPSTrackController)
			mActivityClass = "GTC";
		
		if(btnNew != null)
			btnNew.setOnClickListener(onBtnNewClickListener);

		if(btnUpdate != null){
			btnUpdate.setOnClickListener(onBtnUpdateClickListener);
			btnUpdate.setEnabled(false);
		}
		
		if(spnTemplate != null){
			spnTemplate.setOnItemSelectedListener(spnTemplateOnItemSelectedListener);
			mActivity.initSpinner(spnTemplate, AddOnDBAdapter.ADDON_DATA_TEMPLATE_TABLE_NAME, MainDbAdapter.genColName, 
					new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, 
					MainDbAdapter.isActiveCondition + " AND " + AddOnDBAdapter.ADDON_DATA_TEMPLATE_COL_CLASS_NAME + " = '" + mActivityClass +"'", 
					null, MainDbAdapter.GEN_COL_NAME_NAME, -1, true);
			spnTemplate.setOnTouchListener(mActivity.spinnerOnTouchListener);
		}
	}
	
    private View.OnClickListener onBtnNewClickListener =
        new View.OnClickListener()
            {
                public void onClick( View v )
                {
                	mActivity.showDialog(StaticValues.DIALOG_NEW_TEMPLATE);
                }
            };

    private View.OnClickListener onBtnUpdateClickListener =
        new View.OnClickListener()
            {
                public void onClick( View v )
                {
                	if(mTemplateID != -1)
                		saveTemplate(mTemplateID, null);
                };
            };
	
	public void fillFromTemplate(long templateID){

		long tmpID = 0;
		String selArgs[] = {Long.toString(templateID)};
		Cursor c = mDbAdapter.query(AddOnDBAdapter.ADDON_DATA_TEMPLATE_VALUES_TABLE_NAME, 
				AddOnDBAdapter.addonDataTemplateValuesTableColNames, 
				AddOnDBAdapter.ADDON_DATA_TEMPLATE_VALUES_COL_TEMPLATE_ID_NAME + " = ?", selArgs, null, null, null);
		if(c == null)
			return;
		mActivity.setBackgroundSettingsActive(true);
		if(mActivity instanceof ExpenseEditActivity){
			ExpenseEditActivity tmpActivity = (ExpenseEditActivity)mActivity;
			while(c.moveToNext()){
				if(c.getString(MainDbAdapter.GEN_COL_NAME_POS).equals("spnCar")){
					tmpID = c.getLong(AddOnDBAdapter.ADDON_DATA_TEMPLATE_VALUES_COL_VALUE_POS);
					tmpActivity.setCarId(tmpID);
					if(tmpActivity.findViewById(R.id.lCarZone) != null &&
							tmpActivity.findViewById(R.id.lCarZone).getVisibility() == View.VISIBLE)
						tmpActivity.setSpinnerSelectedID((Spinner)tmpActivity.findViewById(R.id.spnCar), tmpID);
				}
				else if(c.getString(MainDbAdapter.GEN_COL_NAME_POS).equals("spnDriver")){
					tmpID = c.getLong(AddOnDBAdapter.ADDON_DATA_TEMPLATE_VALUES_COL_VALUE_POS);
					tmpActivity.setDriverId(tmpID);
					if(tmpActivity.findViewById(R.id.lDriverZone) != null &&
							tmpActivity.findViewById(R.id.lDriverZone).getVisibility() == View.VISIBLE)
						tmpActivity.setSpinnerSelectedID((Spinner)tmpActivity.findViewById(R.id.spnDriver), tmpID);
				}
				else if(c.getString(MainDbAdapter.GEN_COL_NAME_POS).equals("spnExpCategory")){
					tmpID = c.getLong(AddOnDBAdapter.ADDON_DATA_TEMPLATE_VALUES_COL_VALUE_POS);
					tmpActivity.setExpCategoryId(tmpID);
					if(tmpActivity.findViewById(R.id.lExpCatZone) != null &&
							tmpActivity.findViewById(R.id.lExpCatZone).getVisibility() == View.VISIBLE)
						tmpActivity.setSpinnerSelectedID((Spinner)tmpActivity.findViewById(R.id.spnExpCategory), tmpID);
				}
				else if(c.getString(MainDbAdapter.GEN_COL_NAME_POS).equals("spnExpType")){
					tmpID = c.getLong(AddOnDBAdapter.ADDON_DATA_TEMPLATE_VALUES_COL_VALUE_POS);
					tmpActivity.setExpTypeId(tmpID);
					if(tmpActivity.findViewById(R.id.lExpTypeZone) != null &&
							tmpActivity.findViewById(R.id.lExpTypeZone).getVisibility() == View.VISIBLE)
						tmpActivity.setSpinnerSelectedID((Spinner)tmpActivity.findViewById(R.id.spnExpType), tmpID);
				}
				else if(c.getString(MainDbAdapter.GEN_COL_NAME_POS).equals("spnCurrency")){
					tmpID = c.getLong(AddOnDBAdapter.ADDON_DATA_TEMPLATE_VALUES_COL_VALUE_POS);
					tmpActivity.setCurrencyId(tmpID);
					tmpActivity.setSpinnerSelectedID((Spinner)tmpActivity.findViewById(R.id.spnCurrency), tmpID);
				}
				else if(c.getString(MainDbAdapter.GEN_COL_NAME_POS).equals("spnUOM")){
					tmpID = c.getLong(AddOnDBAdapter.ADDON_DATA_TEMPLATE_VALUES_COL_VALUE_POS);
					tmpActivity.setmUOMId(tmpID);
					tmpActivity.setSpinnerSelectedID((Spinner)tmpActivity.findViewById(R.id.spnUOM), tmpID);
				}
				else if(c.getString(MainDbAdapter.GEN_COL_NAME_POS).equals("etQuantity"))
					((EditText)tmpActivity.findViewById(R.id.etQuantity)).setText(
							c.getString(AddOnDBAdapter.ADDON_DATA_TEMPLATE_VALUES_COL_VALUE_POS));
				else if(c.getString(MainDbAdapter.GEN_COL_NAME_POS).equals("etUserInput"))
					((EditText)tmpActivity.findViewById(R.id.etUserInput)).setText(
							c.getString(AddOnDBAdapter.ADDON_DATA_TEMPLATE_VALUES_COL_VALUE_POS));
				else if(c.getString(MainDbAdapter.GEN_COL_NAME_POS).equals("acBPartner"))
					((AutoCompleteTextView)tmpActivity.findViewById(R.id.acBPartner)).setText(
							c.getString(AddOnDBAdapter.ADDON_DATA_TEMPLATE_VALUES_COL_VALUE_POS));
				else if(c.getString(MainDbAdapter.GEN_COL_NAME_POS).equals("acAdress"))
					((AutoCompleteTextView)tmpActivity.findViewById(R.id.acAdress)).setText(
							c.getString(AddOnDBAdapter.ADDON_DATA_TEMPLATE_VALUES_COL_VALUE_POS));
				else if(c.getString(MainDbAdapter.GEN_COL_NAME_POS).equals("acTag"))
					((AutoCompleteTextView)tmpActivity.findViewById(R.id.acTag)).setText(
							c.getString(AddOnDBAdapter.ADDON_DATA_TEMPLATE_VALUES_COL_VALUE_POS));
				else if(c.getString(MainDbAdapter.GEN_COL_NAME_POS).equals("acUserComment"))
					((AutoCompleteTextView)tmpActivity.findViewById(R.id.acUserComment)).setText(
							c.getString(AddOnDBAdapter.ADDON_DATA_TEMPLATE_VALUES_COL_VALUE_POS));
				else if(c.getString(MainDbAdapter.GEN_COL_NAME_POS).equals("rbInsertModeAmount"))
					((RadioButton)tmpActivity.findViewById(R.id.rbInsertModeAmount)).setChecked(
							c.getString(AddOnDBAdapter.ADDON_DATA_TEMPLATE_VALUES_COL_VALUE_POS).equals("Y"));
				else if(c.getString(MainDbAdapter.GEN_COL_NAME_POS).equals("rbInsertModePrice"))
					((RadioButton)tmpActivity.findViewById(R.id.rbInsertModePrice)).setChecked(
							c.getString(AddOnDBAdapter.ADDON_DATA_TEMPLATE_VALUES_COL_VALUE_POS).equals("Y"));
			}
		}
		else if (mActivity instanceof RefuelEditActivity){
			RefuelEditActivity tmpActivity = (RefuelEditActivity)mActivity;
			while(c.moveToNext()){
				if(c.getString(MainDbAdapter.GEN_COL_NAME_POS).equals("spnCar")){
					tmpID = c.getLong(AddOnDBAdapter.ADDON_DATA_TEMPLATE_VALUES_COL_VALUE_POS);
					tmpActivity.setCarId(tmpID);
					if(tmpActivity.findViewById(R.id.lCarZone) != null &&
							tmpActivity.findViewById(R.id.lCarZone).getVisibility() == View.VISIBLE)
						tmpActivity.setSpinnerSelectedID((Spinner)tmpActivity.findViewById(R.id.spnCar), tmpID);
				}
				else if(c.getString(MainDbAdapter.GEN_COL_NAME_POS).equals("spnDriver")){
					tmpID = c.getLong(AddOnDBAdapter.ADDON_DATA_TEMPLATE_VALUES_COL_VALUE_POS);
					tmpActivity.setDriverId(tmpID);
					if(tmpActivity.findViewById(R.id.lDriverZone) != null &&
							tmpActivity.findViewById(R.id.lDriverZone).getVisibility() == View.VISIBLE)
						tmpActivity.setSpinnerSelectedID((Spinner)tmpActivity.findViewById(R.id.spnDriver), tmpID);
				}
				else if(c.getString(MainDbAdapter.GEN_COL_NAME_POS).equals("spnExpCategory")){
					tmpID = c.getLong(AddOnDBAdapter.ADDON_DATA_TEMPLATE_VALUES_COL_VALUE_POS);
					tmpActivity.setExpCategoryId(tmpID);
					if(tmpActivity.findViewById(R.id.lExpCatZone) != null &&
							tmpActivity.findViewById(R.id.lExpCatZone).getVisibility() == View.VISIBLE)
						tmpActivity.setSpinnerSelectedID((Spinner)tmpActivity.findViewById(R.id.spnExpCategory), tmpID);
				}
				else if(c.getString(MainDbAdapter.GEN_COL_NAME_POS).equals("spnExpType")){
					tmpID = c.getLong(AddOnDBAdapter.ADDON_DATA_TEMPLATE_VALUES_COL_VALUE_POS);
					tmpActivity.setExpTypeId(tmpID);
					if(tmpActivity.findViewById(R.id.lExpTypeZone) != null &&
							tmpActivity.findViewById(R.id.lExpTypeZone).getVisibility() == View.VISIBLE)
						tmpActivity.setSpinnerSelectedID((Spinner)tmpActivity.findViewById(R.id.spnExpType), tmpID);
				}
				else if(c.getString(MainDbAdapter.GEN_COL_NAME_POS).equals("spnCurrency")){
					tmpID = c.getLong(AddOnDBAdapter.ADDON_DATA_TEMPLATE_VALUES_COL_VALUE_POS);
					tmpActivity.setCurrencyId(tmpID);
					tmpActivity.setSpinnerSelectedID((Spinner)tmpActivity.findViewById(R.id.spnCurrency), tmpID);
				}
				else if(c.getString(MainDbAdapter.GEN_COL_NAME_POS).equals("spnUomVolume")){
					tmpID = c.getLong(AddOnDBAdapter.ADDON_DATA_TEMPLATE_VALUES_COL_VALUE_POS);
					tmpActivity.setUOMVolumeId(tmpID);
					tmpActivity.setSpinnerSelectedID((Spinner)tmpActivity.findViewById(R.id.spnUomVolume), tmpID);
				}
				else if(c.getString(MainDbAdapter.GEN_COL_NAME_POS).equals("etUserInput"))
					((EditText)tmpActivity.findViewById(R.id.etUserInput)).setText(
							c.getString(AddOnDBAdapter.ADDON_DATA_TEMPLATE_VALUES_COL_VALUE_POS));
				else if(c.getString(MainDbAdapter.GEN_COL_NAME_POS).equals("acBPartner"))
					((AutoCompleteTextView)tmpActivity.findViewById(R.id.acBPartner)).setText(
							c.getString(AddOnDBAdapter.ADDON_DATA_TEMPLATE_VALUES_COL_VALUE_POS));
				else if(c.getString(MainDbAdapter.GEN_COL_NAME_POS).equals("acAdress"))
					((AutoCompleteTextView)tmpActivity.findViewById(R.id.acAdress)).setText(
							c.getString(AddOnDBAdapter.ADDON_DATA_TEMPLATE_VALUES_COL_VALUE_POS));
				else if(c.getString(MainDbAdapter.GEN_COL_NAME_POS).equals("acTag"))
					((AutoCompleteTextView)tmpActivity.findViewById(R.id.acTag)).setText(
							c.getString(AddOnDBAdapter.ADDON_DATA_TEMPLATE_VALUES_COL_VALUE_POS));
				else if(c.getString(MainDbAdapter.GEN_COL_NAME_POS).equals("acUserComment"))
					((AutoCompleteTextView)tmpActivity.findViewById(R.id.acUserComment)).setText(
							c.getString(AddOnDBAdapter.ADDON_DATA_TEMPLATE_VALUES_COL_VALUE_POS));
				else if(c.getString(MainDbAdapter.GEN_COL_NAME_POS).equals("rbInsertModeAmount"))
					((RadioButton)tmpActivity.findViewById(R.id.rbInsertModeAmount)).setChecked(
							c.getString(AddOnDBAdapter.ADDON_DATA_TEMPLATE_VALUES_COL_VALUE_POS).equals("Y"));
				else if(c.getString(MainDbAdapter.GEN_COL_NAME_POS).equals("rbInsertModePrice"))
					((RadioButton)tmpActivity.findViewById(R.id.rbInsertModePrice)).setChecked(
							c.getString(AddOnDBAdapter.ADDON_DATA_TEMPLATE_VALUES_COL_VALUE_POS).equals("Y"));
			}
		}
		else if (mActivity instanceof MileageEditActivity){
			MileageEditActivity tmpActivity = (MileageEditActivity)mActivity;
			String etUserInputStr = "";
			boolean isMileageInsertMode = false;

			while(c.moveToNext()){
				if(c.getString(MainDbAdapter.GEN_COL_NAME_POS).equals("spnCar")){
					tmpID = c.getLong(AddOnDBAdapter.ADDON_DATA_TEMPLATE_VALUES_COL_VALUE_POS);
					tmpActivity.setCarId(tmpID);
					if(tmpActivity.findViewById(R.id.lCarZone) != null &&
							tmpActivity.findViewById(R.id.lCarZone).getVisibility() == View.VISIBLE)
						tmpActivity.setSpinnerSelectedID((Spinner)tmpActivity.findViewById(R.id.spnCar), tmpID);
				}
				else if(c.getString(MainDbAdapter.GEN_COL_NAME_POS).equals("spnDriver")){
					tmpID = c.getLong(AddOnDBAdapter.ADDON_DATA_TEMPLATE_VALUES_COL_VALUE_POS);
					tmpActivity.setDriverId(tmpID);
					if(tmpActivity.findViewById(R.id.lDriverZone) != null &&
							tmpActivity.findViewById(R.id.lDriverZone).getVisibility() == View.VISIBLE)
						tmpActivity.setSpinnerSelectedID((Spinner)tmpActivity.findViewById(R.id.spnDriver), tmpID);
				}
				else if(c.getString(MainDbAdapter.GEN_COL_NAME_POS).equals("spnExpType")){
					tmpID = c.getLong(AddOnDBAdapter.ADDON_DATA_TEMPLATE_VALUES_COL_VALUE_POS);
					tmpActivity.setExpTypeId(tmpID);
					if(tmpActivity.findViewById(R.id.lExpTypeZone) != null &&
							tmpActivity.findViewById(R.id.lExpTypeZone).getVisibility() == View.VISIBLE)
						tmpActivity.setSpinnerSelectedID((Spinner)tmpActivity.findViewById(R.id.spnExpType), tmpID);
				}
				else if(c.getString(MainDbAdapter.GEN_COL_NAME_POS).equals("rbInsertModeIndex")){
					((RadioButton)tmpActivity.findViewById(R.id.rbInsertModeIndex)).setChecked(
							c.getString(AddOnDBAdapter.ADDON_DATA_TEMPLATE_VALUES_COL_VALUE_POS).equals("Y"));
					if(c.getString(AddOnDBAdapter.ADDON_DATA_TEMPLATE_VALUES_COL_VALUE_POS).equals("Y"))
						tmpActivity.setInsertMode(StaticValues.MILEAGE_INSERTMODE_INDEX);
				}
				else if(c.getString(MainDbAdapter.GEN_COL_NAME_POS).equals("rbInsertModeMileage")){
					isMileageInsertMode = c.getString(AddOnDBAdapter.ADDON_DATA_TEMPLATE_VALUES_COL_VALUE_POS).equals("Y");
					((RadioButton)tmpActivity.findViewById(R.id.rbInsertModeMileage)).setChecked(isMileageInsertMode);
					if(isMileageInsertMode)
						tmpActivity.setInsertMode(StaticValues.MILEAGE_INSERTMODE_MILEAGE);
				}
				else if(c.getString(MainDbAdapter.GEN_COL_NAME_POS).equals("etUserInput"))
					etUserInputStr = c.getString(AddOnDBAdapter.ADDON_DATA_TEMPLATE_VALUES_COL_VALUE_POS);
				else if(c.getString(MainDbAdapter.GEN_COL_NAME_POS).equals("acTag"))
					((AutoCompleteTextView)tmpActivity.findViewById(R.id.acTag)).setText(
							c.getString(AddOnDBAdapter.ADDON_DATA_TEMPLATE_VALUES_COL_VALUE_POS));
				else if(c.getString(MainDbAdapter.GEN_COL_NAME_POS).equals("acUserComment"))
					((AutoCompleteTextView)tmpActivity.findViewById(R.id.acUserComment)).setText(
							c.getString(AddOnDBAdapter.ADDON_DATA_TEMPLATE_VALUES_COL_VALUE_POS));
			}
			if(isMileageInsertMode && etUserInputStr != null)
				((EditText)tmpActivity.findViewById(R.id.etUserInput)).setText(etUserInputStr);
			else
				((EditText)tmpActivity.findViewById(R.id.etUserInput)).setText(null);
			
			tmpActivity.calculateMileageOrNewIndex();
		}
		else if (mActivity instanceof GPSTrackController){
			GPSTrackController tmpActivity = (GPSTrackController)mActivity;
			while(c.moveToNext()){
				if(c.getString(MainDbAdapter.GEN_COL_NAME_POS).equals("spnCar")){
					tmpID = c.getLong(AddOnDBAdapter.ADDON_DATA_TEMPLATE_VALUES_COL_VALUE_POS);
					tmpActivity.setCarId(tmpID);
					if(tmpActivity.findViewById(R.id.lCarZone) != null &&
							tmpActivity.findViewById(R.id.lCarZone).getVisibility() == View.VISIBLE)
						tmpActivity.setSpinnerSelectedID((Spinner)tmpActivity.findViewById(R.id.spnCar), tmpID);
				}
				else if(c.getString(MainDbAdapter.GEN_COL_NAME_POS).equals("spnDriver")){
					tmpID = c.getLong(AddOnDBAdapter.ADDON_DATA_TEMPLATE_VALUES_COL_VALUE_POS);
					tmpActivity.setDriverId(tmpID);
					if(tmpActivity.findViewById(R.id.lDriverZone) != null &&
							tmpActivity.findViewById(R.id.lDriverZone).getVisibility() == View.VISIBLE)
						tmpActivity.setSpinnerSelectedID((Spinner)tmpActivity.findViewById(R.id.spnDriver), tmpID);
				}
				else if(c.getString(MainDbAdapter.GEN_COL_NAME_POS).equals("etName"))
					((EditText)tmpActivity.findViewById(R.id.etName)).setText(
							c.getString(AddOnDBAdapter.ADDON_DATA_TEMPLATE_VALUES_COL_VALUE_POS));
				else if(c.getString(MainDbAdapter.GEN_COL_NAME_POS).equals("acTag"))
					((AutoCompleteTextView)tmpActivity.findViewById(R.id.acTag)).setText(
							c.getString(AddOnDBAdapter.ADDON_DATA_TEMPLATE_VALUES_COL_VALUE_POS));
				else if(c.getString(MainDbAdapter.GEN_COL_NAME_POS).equals("acUserComment"))
					((AutoCompleteTextView)tmpActivity.findViewById(R.id.acUserComment)).setText(
							c.getString(AddOnDBAdapter.ADDON_DATA_TEMPLATE_VALUES_COL_VALUE_POS));
				else if(c.getString(MainDbAdapter.GEN_COL_NAME_POS).equals("ckIsCreateMileage"))
					((CheckBox)tmpActivity.findViewById(R.id.ckIsCreateMileage)).setChecked(
							c.getString(AddOnDBAdapter.ADDON_DATA_TEMPLATE_VALUES_COL_VALUE_POS).equals("Y"));
				else if(c.getString(MainDbAdapter.GEN_COL_NAME_POS).equals("ckIsUseKML"))
					((CheckBox)tmpActivity.findViewById(R.id.ckIsUseKML)).setChecked(
							c.getString(AddOnDBAdapter.ADDON_DATA_TEMPLATE_VALUES_COL_VALUE_POS).equals("Y"));
				else if(c.getString(MainDbAdapter.GEN_COL_NAME_POS).equals("ckIsUseGPX"))
					((CheckBox)tmpActivity.findViewById(R.id.ckIsUseGPX)).setChecked(
							c.getString(AddOnDBAdapter.ADDON_DATA_TEMPLATE_VALUES_COL_VALUE_POS).equals("Y"));
			}
		}
		c.close();
		mActivity.setSpecificLayout();
	}
	
	/**
	 * save a new or update an existing template
	 * @param templateID if > 0 update the existing template. If == 0  create a new one
	 * @param templateMetaData the name of the new template 
	 * @param activity for what activity save the template
	 * @param db reference to AndiCar db
	 * @return -1 * error code on failure or the ID of the template
	 */
	
	public long saveTemplate(long templateID, ContentValues templateMetaData){
		long retVal = -1;
		long dbRetVal = -1;
		ContentValues cv = new ContentValues();
		String name = null;
		
		//fill the master values
		if(templateMetaData != null && templateMetaData.containsKey("Name"))
			name = templateMetaData.getAsString("Name");

		if(templateID < 0 && (name == null || name.length() == 0))
			return -1;
			
		cv.put( MainDbAdapter.GEN_COL_NAME_NAME, name);
       
		if(templateMetaData != null && templateMetaData.containsKey("Comment"))
	        cv.put( MainDbAdapter.GEN_COL_NAME_NAME, templateMetaData.getAsString("Comment"));
		else
			cv.put( MainDbAdapter.GEN_COL_USER_COMMENT_NAME, "");
		
        cv.put( MainDbAdapter.GEN_COL_ISACTIVE_NAME, "Y");
        cv.put( AddOnDBAdapter.ADDON_DATA_TEMPLATE_COL_CLASS_NAME, mActivityClass);
        if(templateID < 0){
        	retVal = mDbAdapter.createRecord(AddOnDBAdapter.ADDON_DATA_TEMPLATE_TABLE_NAME, cv);
            if(retVal < 0){
                return retVal;
            }
        }
        else{
        	retVal = templateID;
			mDbAdapter.deleteRecords(AddOnDBAdapter.ADDON_DATA_TEMPLATE_VALUES_TABLE_NAME, 
					AddOnDBAdapter.ADDON_DATA_TEMPLATE_VALUES_COL_TEMPLATE_ID_NAME + " = " + templateID, null);
        }
        
        Bundle values = new Bundle();
		fillValues(values);
		Set<String> keys = values.keySet();
		for (Iterator<String> iterator = keys.iterator(); iterator.hasNext();) {
			String value = (String) iterator.next();
			cv.clear();
			cv.put( MainDbAdapter.GEN_COL_NAME_NAME, value);
			cv.put( MainDbAdapter.GEN_COL_ISACTIVE_NAME, "Y");
			cv.put(AddOnDBAdapter.ADDON_DATA_TEMPLATE_VALUES_COL_TEMPLATE_ID_NAME, retVal);
			cv.put(AddOnDBAdapter.ADDON_DATA_TEMPLATE_VALUES_COL_VALUE_NAME, values.getString(value));
			dbRetVal = mDbAdapter.createRecord(AddOnDBAdapter.ADDON_DATA_TEMPLATE_VALUES_TABLE_NAME, cv);
	        if(dbRetVal < 0){
	            return dbRetVal;
	        }
		}
		
		if(templateID < 0){ //new twmplate
            Toast toast = Toast.makeText( mActivity, mResource.getString(R.string.AddOn_DataTemplate_CreatedMsg), Toast.LENGTH_LONG );
            toast.show();
		}
		else{
            Toast toast = Toast.makeText( mActivity, mResource.getString(R.string.AddOn_DataTemplate_UpdatedMsg), Toast.LENGTH_SHORT );
            toast.show();
		}
			
		return retVal;
	}
	
	private void fillValues(Bundle cv){
		if(mActivity instanceof ExpenseEditActivity){
			cv.putString("spnCar", Long.toString(mActivity.getCarId()));
			cv.putString("spnDriver", Long.toString(mActivity.getDriverId()));
			cv.putString("spnExpCategory", Long.toString(mActivity.getExpCategoryId()));
			cv.putString("spnExpType", Long.toString(mActivity.getExpTypeId()));
			cv.putString("spnCurrency", Long.toString(mActivity.getCurrencyId()));
			cv.putString("rbInsertModeAmount", ((RadioButton)mActivity.findViewById(R.id.rbInsertModeAmount)).isChecked() ? "Y" : "N");
			cv.putString("rbInsertModePrice", ((RadioButton)mActivity.findViewById(R.id.rbInsertModePrice)).isChecked() ? "Y" : "N");
			cv.putString("etQuantity", ((EditText)mActivity.findViewById(R.id.etQuantity)).getText().toString());
			cv.putString("spnUOM", Long.toString(((Spinner)mActivity.findViewById(R.id.spnUOM)).getSelectedItemId()));
			cv.putString("acBPartner", ((AutoCompleteTextView)mActivity.findViewById(R.id.acBPartner)).getText().toString());
			cv.putString("acAdress", ((AutoCompleteTextView)mActivity.findViewById(R.id.acAdress)).getText().toString());
			cv.putString("acTag", ((AutoCompleteTextView)mActivity.findViewById(R.id.acTag)).getText().toString());
			cv.putString("acUserComment", ((AutoCompleteTextView)mActivity.findViewById(R.id.acUserComment)).getText().toString());
			cv.putString("etUserInput", ((EditText)mActivity.findViewById(R.id.etUserInput)).getText().toString());
		}
		else if(mActivity instanceof RefuelEditActivity){
			cv.putString("spnCar", Long.toString(mActivity.getCarId()));
			cv.putString("spnDriver", Long.toString(mActivity.getDriverId()));
			cv.putString("spnExpCategory", Long.toString(mActivity.getExpCategoryId()));
			cv.putString("spnExpType", Long.toString(mActivity.getExpTypeId()));
			cv.putString("spnCurrency", Long.toString(mActivity.getCurrencyId()));
			cv.putString("spnUomVolume", Long.toString(((Spinner)mActivity.findViewById(R.id.spnUomVolume)).getSelectedItemId()));
			cv.putString("rbInsertModeAmount", ((RadioButton)mActivity.findViewById(R.id.rbInsertModeAmount)).isChecked() ? "Y" : "N");
			cv.putString("rbInsertModePrice", ((RadioButton)mActivity.findViewById(R.id.rbInsertModePrice)).isChecked() ? "Y" : "N");
			cv.putString("etUserInput", ((EditText)mActivity.findViewById(R.id.etUserInput)).getText().toString());
			cv.putString("acBPartner", ((AutoCompleteTextView)mActivity.findViewById(R.id.acBPartner)).getText().toString());
			cv.putString("acAdress", ((AutoCompleteTextView)mActivity.findViewById(R.id.acAdress)).getText().toString());
			cv.putString("acTag", ((AutoCompleteTextView)mActivity.findViewById(R.id.acTag)).getText().toString());
			cv.putString("acUserComment", ((AutoCompleteTextView)mActivity.findViewById(R.id.acUserComment)).getText().toString());
		}
		else if(mActivity instanceof MileageEditActivity){
			cv.putString("spnCar", Long.toString(mActivity.getCarId()));
			cv.putString("spnDriver", Long.toString(mActivity.getDriverId()));
			cv.putString("spnExpType", Long.toString(mActivity.getExpTypeId()));
			cv.putString("rbInsertModeIndex", ((RadioButton)mActivity.findViewById(R.id.rbInsertModeIndex)).isChecked() ? "Y" : "N");
			cv.putString("rbInsertModeMileage", ((RadioButton)mActivity.findViewById(R.id.rbInsertModeMileage)).isChecked() ? "Y" : "N");
			cv.putString("etUserInput", ((EditText)mActivity.findViewById(R.id.etUserInput)).getText().toString());
			cv.putString("acTag", ((AutoCompleteTextView)mActivity.findViewById(R.id.acTag)).getText().toString());
			cv.putString("acUserComment", ((AutoCompleteTextView)mActivity.findViewById(R.id.acUserComment)).getText().toString());
		}
		else if(mActivity instanceof GPSTrackController){
			cv.putString("spnCar", Long.toString(mActivity.getCarId()));
			cv.putString("spnDriver", Long.toString(mActivity.getDriverId()));
			cv.putString("etName", ((EditText)mActivity.findViewById(R.id.etName)).getText().toString());
			cv.putString("ckIsCreateMileage", ((CheckBox)mActivity.findViewById(R.id.ckIsCreateMileage)).isChecked() ? "Y" : "N");
			cv.putString("acTag", ((AutoCompleteTextView)mActivity.findViewById(R.id.acTag)).getText().toString());
			cv.putString("acUserComment", ((AutoCompleteTextView)mActivity.findViewById(R.id.acUserComment)).getText().toString());
			cv.putString("ckIsUseKML", ((CheckBox)mActivity.findViewById(R.id.ckIsUseKML)).isChecked() ? "Y" : "N");
			cv.putString("ckIsUseGPX", ((CheckBox)mActivity.findViewById(R.id.ckIsUseGPX)).isChecked() ? "Y" : "N");
		}
	}

	private OnItemSelectedListener spnTemplateOnItemSelectedListener = new OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
//			mActivity.setDefaultValues();
			if(mActivity.isBackgroundSettingsActive())
				return;
			
			mTemplateID = id;
			if(id == -1){
				btnUpdate.setEnabled(false);
				mActivity.setDefaultValues();
			}
			else{
				btnUpdate.setEnabled(true);
				fillFromTemplate(id);
			}

			if(isSendStatistics)
	            AndiCarStatistics.sendFlurryEvent(mActivity, "DataEntryTemplate", null);
			
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
		}
	};
	
	public void updateTemplateList(long newID){
		
		mActivity.setBackgroundSettingsActive(true);
		
		mActivity.initSpinner(spnTemplate, AddOnDBAdapter.ADDON_DATA_TEMPLATE_TABLE_NAME, MainDbAdapter.genColName, 
				new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, 
				MainDbAdapter.isActiveCondition + " AND " + AddOnDBAdapter.ADDON_DATA_TEMPLATE_COL_CLASS_NAME + " = '" + mActivityClass +"'", 
				null, MainDbAdapter.GEN_COL_NAME_NAME, newID, true);
	}

	public void setControlsEnabled(boolean isEnabled){
		
		btnUpdate.setEnabled(isEnabled);
		btnNew.setEnabled(isEnabled);
		spnTemplate.setEnabled(isEnabled);
		
		if(isEnabled && spnTemplate.getSelectedItemId() == -1)
			btnUpdate.setEnabled(false);
	}
}
