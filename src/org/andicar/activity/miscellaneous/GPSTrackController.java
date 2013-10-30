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

import android.R.id;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
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
    private ImageButton btnGPSTrackPauseResume;
    private RelativeLayout llIndexStartZone;
    private RelativeLayout lCarZone;
    private RelativeLayout lDriverZone;
    
    private boolean isCreateMileage = true;
    private boolean isGpsTrackOn = false;
    private boolean isGpsTrackPaused = false;
    private ViewGroup vgRoot;
    private long mTagId = 0;
    private Context mCtx = null;
    
    /** Messenger for communicating with the service. */
    Messenger mService = null;

    /** Flag indicating whether we have called bind on the service. */
    boolean isBound;
    
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
        aaUserComment = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                mDbAdapter.getAutoCompleteText(MainDbAdapter.TABLE_NAME_GPSTRACK, null,
                		mCarId, 30));
        acUserComment.setAdapter(aaUserComment);
        acTag = ((AutoCompleteTextView) findViewById( R.id.acTag ));
        tagAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                mDbAdapter.getAutoCompleteText(MainDbAdapter.TABLE_NAME_TAG, null,
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
        btnGPSTrackPauseResume = (ImageButton) findViewById(R.id.btnPauseResumeGpsTrack);
        btnGPSTrackPauseResume.setOnClickListener(btnGPSTrackPauseResumeListener);
        ckIsCreateMileage = (CheckBox) findViewById(R.id.ckIsCreateMileage);
        isCreateMileage = mPreferences.getBoolean("GPSTrackCreateMileage", true);
        ckIsCreateMileage.setChecked(isCreateMileage);
        ckIsCreateMileage.setOnCheckedChangeListener(ckCreateMilegeOnCheckedChangeListener);
        vgRoot = (ViewGroup) findViewById(R.id.vgRoot);
        //init tag
        if(mPreferences.getBoolean("RememberLastTag", false) && mPreferences.getLong("LastTagId", 0) > 0){
            mTagId = mPreferences.getLong("LastTagId", 0);
            String selection = MainDbAdapter.COL_NAME_GEN_ROWID + "= ? ";
            String[] selectionArgs = {Long.toString(mTagId)};
            Cursor c = mDbAdapter.query(MainDbAdapter.TABLE_NAME_TAG, MainDbAdapter.COL_LIST_GEN_ROWID_NAME,
                        selection, selectionArgs, null, null, null);
            if(c.moveToFirst())
                acTag.setText(c.getString(MainDbAdapter.COL_POS_GEN_NAME));
            c.close();
        }

        if(vgRoot != null)
        	setInputType(vgRoot);
        initControls();
        fillStartIndex();
    }

    @Override
    protected void onStop() {
    	try{
	        super.onStop();
	        if(isBound)
	        	unbindService(mConnection);
	        saveState();
    	}
    	catch(Exception e){};
    }

    @Override
    protected void onResume() {
        super.onResume();
        isBackgroundSettingsActive = true;
        isGpsTrackOn = mPreferences.getBoolean("isGpsTrackOn", false);
        isGpsTrackPaused = mPreferences.getBoolean("isGpsTrackPaused", false);

        if(isGpsTrackOn){
            btnGPSTrackStartStop.setImageDrawable(mResource.getDrawable(R.drawable.icon_record_gps_stop_24x24));
            
            if(isGpsTrackPaused)
            	btnGPSTrackPauseResume.setImageDrawable(mResource.getDrawable(R.drawable.icon_resume24x24));
            else
            	btnGPSTrackPauseResume.setImageDrawable(mResource.getDrawable(R.drawable.icon_pause24x24));
            
            btnGPSTrackPauseResume.setVisibility(View.VISIBLE);
            restoreState();
            setEditable(vgRoot, false);
           	bindService(new Intent(GPSTrackController.this, GPSTrackService.class), mConnection, Context.BIND_WAIVE_PRIORITY);            	
        }
        else{
        	if(mCarId <= 0)
        		mCarId = mPreferences.getLong("CurrentCar_ID", -1);
//            mDriverId = mPreferences.getLong("CurrentDriver_ID", -1);
            btnGPSTrackStartStop.setImageDrawable(mResource.getDrawable(R.drawable.icon_record_gps_start_24x24));
            btnGPSTrackPauseResume.setVisibility(View.GONE);
        	mPrefEditor.putBoolean("isGpsTrackPaused", false);
            setEditable(vgRoot, true);
        }
    }

    private void initControls(){
    	long checkID;
    	if(lCarZone != null){
	    	checkID = mDbAdapter.isSingleActiveRecord(MainDbAdapter.TABLE_NAME_CAR, null); 
	    	if(checkID > -1){ //one single car
	    		mCarId = checkID;
	    		lCarZone.setVisibility(View.GONE);
	    	}
	    	else{
	    		lCarZone.setVisibility(View.VISIBLE);
	            initSpinner(spnCar, MainDbAdapter.TABLE_NAME_CAR, MainDbAdapter.COL_LIST_GEN_ROWID_NAME,
	                    new String[]{MainDbAdapter.COL_NAME_GEN_NAME}, MainDbAdapter.WHERE_CONDITION_ISACTIVE, null, MainDbAdapter.COL_NAME_GEN_NAME,
	                    mCarId, false);
	    	}
    	}
    	else{
            initSpinner(spnCar, MainDbAdapter.TABLE_NAME_CAR, MainDbAdapter.COL_LIST_GEN_ROWID_NAME,
                    new String[]{MainDbAdapter.COL_NAME_GEN_NAME}, MainDbAdapter.WHERE_CONDITION_ISACTIVE, null, MainDbAdapter.COL_NAME_GEN_NAME,
                    mCarId, false);
    	}
    	if(lDriverZone != null){
	    	checkID = mDbAdapter.isSingleActiveRecord(MainDbAdapter.TABLE_NAME_DRIVER, null); 
	    	if(checkID > -1){ //one single driver
	    		mDriverId = checkID;
	    		lDriverZone.setVisibility(View.GONE);
	    	}
	    	else{
	    		lDriverZone.setVisibility(View.VISIBLE);
	            initSpinner(spnDriver, MainDbAdapter.TABLE_NAME_DRIVER, MainDbAdapter.COL_LIST_GEN_ROWID_NAME,
	                    new String[]{MainDbAdapter.COL_NAME_GEN_NAME}, MainDbAdapter.WHERE_CONDITION_ISACTIVE, null, MainDbAdapter.COL_NAME_GEN_NAME,
	                    mDriverId, false);
	    	}
    	}
    	else{
            initSpinner(spnDriver, MainDbAdapter.TABLE_NAME_DRIVER, MainDbAdapter.COL_LIST_GEN_ROWID_NAME,
                    new String[]{MainDbAdapter.COL_NAME_GEN_NAME}, MainDbAdapter.WHERE_CONDITION_ISACTIVE, null, MainDbAdapter.COL_NAME_GEN_NAME,
                    mDriverId, false);
    	}

    	if(acAdress != null)
        	acAdress.setOnKeyListener(this);
        if(acBPartner != null)
        	acBPartner.setOnKeyListener(this);
        if(acTag != null)
        	acTag.setOnKeyListener(this);
        if(acUserComment != null)
        	acUserComment.setOnKeyListener(this);
    	
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
                   || child.getId() == R.id.btnPauseResumeGpsTrack
                   || child.getId() == R.id.etIndexStart
                   || child.getId() == id.closeButton))
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
                android.R.layout.simple_list_item_1,
                mDbAdapter.getAutoCompleteText(MainDbAdapter.TABLE_NAME_GPSTRACK, null,
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
            String selection = MainDbAdapter.COL_NAME_GEN_ROWID + "= ? ";
            String[] selectionArgs = {Long.toString(mTagId)};
            Cursor c = mDbAdapter.query(MainDbAdapter.TABLE_NAME_TAG, MainDbAdapter.COL_LIST_GEN_ROWID_NAME,
                        selection, selectionArgs, null, null, null);
            if(c.moveToFirst())
                acTag.setText(c.getString(MainDbAdapter.COL_POS_GEN_NAME));
            c.close();
        }
        else
        	acTag.setText(null);
	}

    private View.OnClickListener btnGPSTrackPauseResumeListener = new View.OnClickListener() {
        public void onClick(View v)
        {
        	if(!isBound || mService == null)
        		return;

        	isGpsTrackPaused = mPreferences.getBoolean("isGpsTrackPaused", false);

            Message msg;
            try {
                if(isGpsTrackPaused){
                	msg = Message.obtain(null, StaticValues.MSG_GPS_TRACK_SERVICE_RESUME, 0, 0);
                	btnGPSTrackPauseResume.setImageDrawable(mResource.getDrawable(R.drawable.icon_pause24x24));
                	mPrefEditor.putBoolean("isGpsTrackPaused", false);
                }
                else{
                	msg = Message.obtain(null, StaticValues.MSG_GPS_TRACK_SERVICE_PAUSE, 0, 0);
                	btnGPSTrackPauseResume.setImageDrawable(mResource.getDrawable(R.drawable.icon_resume24x24));
                	mPrefEditor.putBoolean("isGpsTrackPaused", true);
                }
                mPrefEditor.commit();
                
                mService.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }            
        };
    };

    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the object we can use to
            // interact with the service.  We are communicating with the
            // service using a Messenger, so here we get a client-side
            // representation of that from the raw IBinder object.
            mService = new Messenger(service);
            isBound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
            isBound = false;
        }
    };
}
