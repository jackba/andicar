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

package org.andicar.activity.miscellaneous;

import java.math.BigDecimal;

import org.andicar.activity.EditActivityBase;
import org.andicar2.activity.R;
import org.andicar.persistence.FileUtils;
import org.andicar.persistence.MainDbAdapter;
import org.andicar.service.GPSTrackService;
import org.andicar.utils.StaticValues;
import org.andicar.utils.Utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

/**
 *
 * @author miki
 */
public class GPSTrackController extends EditActivityBase {
    private ArrayAdapter<String> aaUserComment;
    private ArrayAdapter<String> tagAdapter;
    private EditText etName;
    private EditText etIndexStart;
    private CheckBox ckIsUseKML;
    private CheckBox ckIsUseGPX;
    private CheckBox ckIsCreateMileage;
    private ImageButton btnGPSTrackStartStop;
    private RelativeLayout llIndexStartZone;
    private RelativeLayout lCarZone;
    private RelativeLayout lDriverZone;
    
    private boolean isCreateMileage = true;
    private boolean isGpsTrackOn = false;
    private ViewGroup vgRoot;
    private long mTagId = 0;
    private Context mCtx = null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {

    	isUseTemplate = true;

        super.onCreate(icicle);
        
        mCtx = this;
        String strOperationType = null;
        if(mBundleExtras != null){
        	strOperationType = mBundleExtras.getString("Operation");
	        if(strOperationType != null && strOperationType.equals("BT"))
	        	mCarId = mBundleExtras.getLong("CarID");
        }
        if(mCarId <= 0L)
        	mCarId = mPreferences.getLong("CurrentCar_ID", 1);
        
        mDriverId = mPreferences.getLong("LastDriver_ID", 1);

        vgRoot = (ViewGroup) findViewById(R.id.vgRoot);
        spnCar = (Spinner)findViewById(R.id.spnCar);
        spnDriver = (Spinner)findViewById(R.id.spnDriver);
        spnCar.setOnItemSelectedListener(spinnerCarOnItemSelectedListener);
        spnDriver.setOnItemSelectedListener(spinnerDriverOnItemSelectedListener);
        spnCar.setOnTouchListener(spinnerOnTouchListener);
        spnDriver.setOnTouchListener(spinnerOnTouchListener);
        acUserComment = ((AutoCompleteTextView) findViewById( R.id.acUserComment ));
        aaUserComment = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line,
                mDbAdapter.getAutoCompleteText(MainDbAdapter.GPSTRACK_TABLE_NAME, null,
                		mCarId, 30));
        acUserComment.setAdapter(aaUserComment);
        acTag = ((AutoCompleteTextView) findViewById( R.id.acTag ));
        tagAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line,
                mDbAdapter.getAutoCompleteText(MainDbAdapter.TAG_TABLE_NAME, null,
                0, 0));
        acTag.setAdapter(tagAdapter);
        etName = (EditText) findViewById(R.id.etName);
        etIndexStart = (EditText) findViewById(R.id.etIndexStart);
        llIndexStartZone = (RelativeLayout) findViewById(R.id.llIndexStartZone);
        lCarZone = (RelativeLayout) findViewById(R.id.lCarZone);
        lDriverZone = (RelativeLayout) findViewById(R.id.lDriverZone);
        
        etName.setHint(mResource.getString(R.string.GEN_CreatedOn) + " " +
				        		DateFormat.getDateFormat(getApplicationContext()).format(System.currentTimeMillis()) + " " +
								DateFormat.getTimeFormat(getApplicationContext()).format(System.currentTimeMillis())
				         		);
        ckIsUseKML = (CheckBox) findViewById(R.id.ckIsUseKML);
        ckIsUseKML.setChecked(mPreferences.getBoolean("IsUseKMLTrack", true));
        ckIsUseGPX = (CheckBox) findViewById(R.id.ckIsUseGPX);
        ckIsUseGPX.setChecked(mPreferences.getBoolean("IsUseGPXTrack", true));
        btnGPSTrackStartStop = (ImageButton) findViewById(R.id.btnStartStopGpsTrack);
        btnGPSTrackStartStop.setOnClickListener(btnGPSTrackStartStopListener);
        ckIsCreateMileage = (CheckBox) findViewById(R.id.ckIsCreateMileage);
        isCreateMileage = mPreferences.getBoolean("GPSTrackCreateMileage", true);
        ckIsCreateMileage.setChecked(isCreateMileage);
        ckIsCreateMileage.setOnCheckedChangeListener(ckCreateMilegeOnCheckedChangeListener);
        vgRoot = (ViewGroup) findViewById(R.id.vgRoot);
        //init tag
        if(mPreferences.getBoolean("RememberLastTag", false) && mPreferences.getLong("LastTagId", 0) > 0){
            mTagId = mPreferences.getLong("LastTagId", 0);
            String selection = MainDbAdapter.GEN_COL_ROWID_NAME + "= ? ";
            String[] selectionArgs = {Long.toString(mTagId)};
            Cursor c = mDbAdapter.query(MainDbAdapter.TAG_TABLE_NAME, MainDbAdapter.genColName,
                        selection, selectionArgs, null, null, null);
            if(c.moveToFirst())
                acTag.setText(c.getString(MainDbAdapter.GEN_COL_NAME_POS));
            c.close();
        }

        if(vgRoot != null)
        	setInputType(vgRoot);
        initControls();
        fillStartIndex();
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isBackgroundSettingsActive = true;
        isGpsTrackOn = mPreferences.getBoolean("isGpsTrackOn", false);

        if(isGpsTrackOn){
            btnGPSTrackStartStop.setImageDrawable(mResource.getDrawable(R.drawable.icon_record_gps_stop_24x24));
            restoreState();
            setEditable(vgRoot, false);
        }
        else{
        	if(mCarId <= 0)
        		mCarId = mPreferences.getLong("CurrentCar_ID", -1);
//            mDriverId = mPreferences.getLong("CurrentDriver_ID", -1);
            btnGPSTrackStartStop.setImageDrawable(mResource.getDrawable(R.drawable.icon_record_gps_start_24x24));
            setEditable(vgRoot, true);
        }
    }

    private void initControls(){
    	long checkID;
    	if(lCarZone != null){
	    	checkID = mDbAdapter.isSingleActiveRecord(MainDbAdapter.CAR_TABLE_NAME, null); 
	    	if(checkID > -1){ //one single car
	    		mCarId = checkID;
	    		lCarZone.setVisibility(View.GONE);
	    	}
	    	else{
	    		lCarZone.setVisibility(View.VISIBLE);
	            initSpinner(spnCar, MainDbAdapter.CAR_TABLE_NAME, MainDbAdapter.genColName,
	                    new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, MainDbAdapter.isActiveCondition, null, MainDbAdapter.GEN_COL_NAME_NAME,
	                    mCarId, false);
	    	}
    	}
    	else{
            initSpinner(spnCar, MainDbAdapter.CAR_TABLE_NAME, MainDbAdapter.genColName,
                    new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, MainDbAdapter.isActiveCondition, null, MainDbAdapter.GEN_COL_NAME_NAME,
                    mCarId, false);
    	}
    	if(lDriverZone != null){
	    	checkID = mDbAdapter.isSingleActiveRecord(MainDbAdapter.DRIVER_TABLE_NAME, null); 
	    	if(checkID > -1){ //one single driver
	    		mDriverId = checkID;
	    		lDriverZone.setVisibility(View.GONE);
	    	}
	    	else{
	    		lDriverZone.setVisibility(View.VISIBLE);
	            initSpinner(spnDriver, MainDbAdapter.DRIVER_TABLE_NAME, MainDbAdapter.genColName,
	                    new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, MainDbAdapter.isActiveCondition, null, MainDbAdapter.GEN_COL_NAME_NAME,
	                    mDriverId, false);
	    	}
    	}
    	else{
            initSpinner(spnDriver, MainDbAdapter.DRIVER_TABLE_NAME, MainDbAdapter.genColName,
                    new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, MainDbAdapter.isActiveCondition, null, MainDbAdapter.GEN_COL_NAME_NAME,
                    mDriverId, false);
    	}
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveState();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveState();
    }

    private void saveState(){
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putLong("GPSTrackCarID", mCarId);
        editor.putLong("GPSTrackDriverID", mDriverId);
        editor.putString("GPSTrackName", etName.getText().toString());
        editor.putString("GPSTrackComment", acUserComment.getText().toString());
        editor.putString("GPSTrackTag", acTag.getText().toString());
        editor.putBoolean("GPSTrackCreateMileage", ckIsCreateMileage.isChecked());
        editor.putBoolean("GPSTrackUseKML", ckIsUseKML.isChecked());
        editor.putBoolean("GPSTrackUseGPX", ckIsUseGPX.isChecked());
        editor.commit();
    }

    private void restoreState(){
        mCarId = mPreferences.getLong("GPSTrackCarID", mCarId);
        mDriverId = mPreferences.getLong("GPSTrackDriverID", mDriverId);
        etName.setText(mPreferences.getString("GPSTrackName", ""));
        acUserComment.setText(mPreferences.getString("GPSTrackComment", ""));
        acTag.setText(mPreferences.getString("GPSTrackTag", null));
        isCreateMileage = mPreferences.getBoolean("GPSTrackCreateMileage", true);
        ckIsCreateMileage.setChecked(isCreateMileage);
        ckIsUseKML.setChecked(mPreferences.getBoolean("GPSTrackUseKML", true));
        ckIsUseGPX.setChecked(mPreferences.getBoolean("GPSTrackUseGPX", true));
        etIndexStart.setText(mPreferences.getString("GPSTrackStartIndex", ""));
//        ckIsShowOnMap.setChecked(mPreferences.getBoolean("GPSTrackShowMap", false));
    }

   protected void setEditable(ViewGroup vg, boolean editable){
       View child;
       for(int i = 0; i < vg.getChildCount(); i++)
       {
           child = vg.getChildAt(i);
           if(child instanceof ViewGroup){
               setEditable((ViewGroup)child, editable);
           }

           if(!(child.getId() == R.id.ckIsCreateMileage
                   || child.getId() == R.id.btnStartStopGpsTrack
                   || child.getId() == R.id.etIndexStart))
               child.setEnabled(editable);
       }
   }
   
   private void fillStartIndex(){
	   if(isCreateMileage){
		   llIndexStartZone.setVisibility(View.VISIBLE);
	       try{
	       		BigDecimal startIndex = mDbAdapter.getCarLastMileageIndex(mCarId);
	       		if(startIndex != null){
	       			etIndexStart.setText(Utils.numberToString(startIndex, false, StaticValues.DECIMALS_LENGTH, StaticValues.ROUNDING_MODE_LENGTH));
	       		}
	       }
	       catch(NumberFormatException e){}
	   }
	   else{
		   llIndexStartZone.setVisibility(View.GONE);
		   etIndexStart.setText(null);
	   }
   }

   private OnCheckedChangeListener ckCreateMilegeOnCheckedChangeListener = 
	   new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				isCreateMileage = isChecked;
				fillStartIndex();
			}
   };

   private AdapterView.OnItemSelectedListener spinnerCarOnItemSelectedListener =
            new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    if(isBackgroundSettingsActive)
                        return;
                    setCarId(arg3);
                }
                public void onNothingSelected(AdapterView<?> arg0) {
                }
            };

    private AdapterView.OnItemSelectedListener spinnerDriverOnItemSelectedListener =
        new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                if(isBackgroundSettingsActive)
                    return;
                setDriverId(arg3);
            }
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        };
        
    private View.OnClickListener btnGPSTrackStartStopListener = new View.OnClickListener() {
        public void onClick(View v)
        {
            Intent gpsTrackIntent = new Intent(GPSTrackController.this, GPSTrackService.class);
            if(isGpsTrackOn){
                mPrefEditor.putBoolean("GPSTrackCreateMileage", isCreateMileage);
                if(isCreateMileage)
                	mPrefEditor.putString("GPSTrackStartIndex", etIndexStart.getText().toString());
                mPrefEditor.commit();
                stopService(gpsTrackIntent);
                isGpsTrackOn = false;
                setEditable(vgRoot, true);
                etName.setText("");
                etName.setHint(mResource.getString(R.string.GEN_CreatedOn) + " " +
		        		DateFormat.getDateFormat(getApplicationContext()).format(System.currentTimeMillis()) + " " +
						DateFormat.getTimeFormat(getApplicationContext()).format(System.currentTimeMillis())
		         		);
                btnGPSTrackStartStop.setImageDrawable(mResource.getDrawable(R.drawable.icon_record_gps_start_24x24));
                mPrefEditor.putBoolean("isGpsTrackOn", false);
                mPrefEditor.commit();
                finish();
            }
            else{
                String strRetVal = checkNumeric(vgRoot, false);
                if( strRetVal != null ) {
                    Toast toast = Toast.makeText( getApplicationContext(),
                            mResource.getString( R.string.GEN_NumberFormatException ) + ": " + strRetVal, Toast.LENGTH_SHORT );
                    toast.show();
                    return;
                }
            	
                if(etName.getText().toString().length() == 0)
                    etName.setText(etName.getHint());

               	mPrefEditor.putString("GPSTrackStartIndex", etIndexStart.getText().toString());
                mPrefEditor.putString("GPSTrackTmp_Name", etName.getText().toString());
                mPrefEditor.putString("GPSTrackTmp_UserComment", acUserComment.getText().toString());
                mPrefEditor.putString("GPSTrackTmp_Tag", acTag.getText().toString());
                mPrefEditor.putLong("GPSTrackTmp_CarId", mCarId);
                mPrefEditor.putLong("GPSTrackTmp_DriverId", mDriverId);
                mPrefEditor.putBoolean("GPSTrackTmp_IsUseKML", ckIsUseKML.isChecked());
                mPrefEditor.putBoolean("GPSTrackTmp_IsUseGPX", ckIsUseGPX.isChecked());
                mPrefEditor.commit();

                FileUtils fu = new FileUtils(mCtx);
        		fu.createFolderIfNotExists(FileUtils.TRACK_FOLDER);
        		fu = null;

                startService(gpsTrackIntent);
                isGpsTrackOn = true; 
                setEditable(vgRoot, false);
//                startActivity(new Intent(GPSTrackController.this, GPSTrackMap.class));
                if(isGpsTrackOn)
                    btnGPSTrackStartStop.setImageDrawable(mResource.getDrawable(R.drawable.icon_record_gps_stop_24x24));
                afterSave();
//                finish();
            }
        };
    };

	/* (non-Javadoc)
	 * @see org.andicar.activity.EditActivityBase#saveData()
	 */
	@Override
	protected boolean saveData() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.andicar.activity.EditActivityBase#setLayout()
	 */
	@Override
	protected void setLayout() {
		setContentView(R.layout.gpstrack_controller_activity_s01);
	}

	/* (non-Javadoc)
	 * @see org.andicar.activity.BaseActivity#setSpecificLayout()
	 */
	@Override
	public void setSpecificLayout() {
	}
    
	/**
	 * @param carId the mCarId to set
	 */
	public void setCarId(long carId) {
		this.mCarId = carId;

        aaUserComment = null;
        aaUserComment = new ArrayAdapter<String>(GPSTrackController.this,
                android.R.layout.simple_dropdown_item_1line,
                mDbAdapter.getAutoCompleteText(MainDbAdapter.GPSTRACK_TABLE_NAME, null,
                spnCar.getSelectedItemId(), 30));
        acUserComment.setAdapter(aaUserComment);
        fillStartIndex();
	}

	/**
	 * @param driverId the mDriverId to set
	 */
	public void setDriverId(long driverId) {
		this.mDriverId = driverId;
	}

	/* (non-Javadoc)
	 * @see org.andicar.activity.EditActivityBase#setDefaultValues()
	 */
	@Override
	public void setDefaultValues() {
        setCarId(mPreferences.getLong("CurrentCar_ID", 1));
		setSpinnerSelectedID(spnCar, mCarId);
		fillStartIndex();
		
        setDriverId(mDriverId = mPreferences.getLong("LastDriver_ID", 1));
		setSpinnerSelectedID(spnDriver, mDriverId);

        ckIsUseKML.setChecked(mPreferences.getBoolean("IsUseKMLTrack", true));
        ckIsUseGPX.setChecked(mPreferences.getBoolean("IsUseGPXTrack", true));
        isCreateMileage = mPreferences.getBoolean("GPSTrackCreateMileage", true);
        ckIsCreateMileage.setChecked(isCreateMileage);
        
        etName.setText(null);
        acUserComment.setText(null);
        
        if(mPreferences.getBoolean("RememberLastTag", false) && mPreferences.getLong("LastTagId", 0) > 0){
            mTagId = mPreferences.getLong("LastTagId", 0);
            String selection = MainDbAdapter.GEN_COL_ROWID_NAME + "= ? ";
            String[] selectionArgs = {Long.toString(mTagId)};
            Cursor c = mDbAdapter.query(MainDbAdapter.TAG_TABLE_NAME, MainDbAdapter.genColName,
                        selection, selectionArgs, null, null, null);
            if(c.moveToFirst())
                acTag.setText(c.getString(MainDbAdapter.GEN_COL_NAME_POS));
            c.close();
        }
        else
        	acTag.setText(null);
	}

}
