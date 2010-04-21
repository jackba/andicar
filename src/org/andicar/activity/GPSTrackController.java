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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import org.andicar.persistence.MainDbAdapter;
import org.andicar.service.GPSTrackService;
import org.andicar.utils.Utils;

/**
 *
 * @author miki
 */
public class GPSTrackController extends EditActivityBase {
    private Spinner spinnerCar;
    private Spinner spinnerDriver;
    private boolean isActivityOnLoading = true;
    private ArrayAdapter<String> userCommentAdapter;
    private AutoCompleteTextView userComment;
    private EditText editTextName;
    private CheckBox ckGpsTrackUseKML;
    private CheckBox ckGpsTrackUseGPX;
    private CheckBox ckGpsTrackShowOnMap;
    private CheckBox ckGpsTrackCreateMileage;
    private Button btnGPSTrackStartStop;
    private boolean isGpsTrackOn = false;
    private ViewGroup vgRoot;
    private long mCarId;
    private long mDriverId;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {

        super.onCreate( icicle, R.layout.gpstrackcontroller_activity, null );

        mCarId = mPreferences.getLong("CurrentCar_ID", -1);
        mDriverId = mPreferences.getLong("CurrentDriver_ID", -1);

        vgRoot = (ViewGroup) findViewById(R.id.genRootViewGroup);
        spinnerCar = (Spinner)findViewById(R.id.carSpinner);
        spinnerDriver = (Spinner)findViewById(R.id.driverSpinner);
        spinnerCar.setOnItemSelectedListener(spinnerCarDriverOnItemSelectedListener);
        spinnerDriver.setOnItemSelectedListener(spinnerCarDriverOnItemSelectedListener);
        spinnerCar.setOnTouchListener(spinnerOnTouchListener);
        spinnerDriver.setOnTouchListener(spinnerOnTouchListener);
        userComment = ((AutoCompleteTextView) findViewById( R.id.genUserCommentEntry ));
        editTextName = (EditText) findViewById(R.id.genNameEntry);
        editTextName.setHint(Utils.getDateStr(true, true));
        ckGpsTrackUseKML = (CheckBox) findViewById(R.id.gpsTrackUseKMLCk);
        ckGpsTrackUseKML.setChecked(mPreferences.getBoolean("IsUseKMLTrack", true));
        ckGpsTrackUseGPX = (CheckBox) findViewById(R.id.gpsTrackUseGPXCk);
        ckGpsTrackUseGPX.setChecked(mPreferences.getBoolean("IsUseGPXTrack", true));
        ckGpsTrackShowOnMap = (CheckBox) findViewById(R.id.gpsTrackShowOnMap);
        ckGpsTrackShowOnMap.setChecked(mPreferences.getBoolean("IsGPSTrackOnMap", true));
        btnGPSTrackStartStop = (Button) findViewById(R.id.gpsTrackBtnStartStopGpsTrack);
        btnGPSTrackStartStop.setOnClickListener(btnGPSTrackStartStopListener);
        ckGpsTrackCreateMileage = (CheckBox) findViewById(R.id.gpsTrackCreateMileageCk);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isActivityOnLoading = true;
        isGpsTrackOn = mPreferences.getBoolean("isGpsTrackOn", false);

        if(isGpsTrackOn){
            btnGPSTrackStartStop.setText(mRes.getString(R.string.GPSTRACK_ACTIVITY_GPSTRACKSTOP_BTN_CAPTION));
            restoreState();
            setEditable(vgRoot, false);
        }
        else{
            mCarId = mPreferences.getLong("CurrentCar_ID", -1);
            mDriverId = mPreferences.getLong("CurrentDriver_ID", -1);
            btnGPSTrackStartStop.setText(mRes.getString(R.string.GPSTRACK_ACTIVITY_GPSTRACKSTART_BTN_CAPTION));
            setEditable(vgRoot, true);
        }
        initSpinner(spinnerCar, MainDbAdapter.CAR_TABLE_NAME, MainDbAdapter.genColName,
                new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, MainDbAdapter.isActiveCondition, MainDbAdapter.GEN_COL_NAME_NAME,
                mCarId, false);

        initSpinner(spinnerDriver, MainDbAdapter.DRIVER_TABLE_NAME, MainDbAdapter.genColName,
                new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, MainDbAdapter.isActiveCondition, MainDbAdapter.GEN_COL_NAME_NAME,
                mDriverId, false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveState();
    }

    @Override
    protected void onStop() {
        super.onStop();
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
        editor.putString("GPSTrackName", editTextName.getText().toString());
        editor.putString("GPSTrackComment", userComment.getText().toString());
        editor.putBoolean("GPSTrackCreateMileage", ckGpsTrackCreateMileage.isChecked());
        editor.putBoolean("GPSTrackUseKML", ckGpsTrackUseKML.isChecked());
        editor.putBoolean("GPSTrackUseGPX", ckGpsTrackUseGPX.isChecked());
        editor.putBoolean("GPSTrackShowMap", ckGpsTrackShowOnMap.isChecked());
        editor.commit();
    }

    private void restoreState(){
        mCarId = mPreferences.getLong("GPSTrackCarID", mCarId);
        mDriverId = mPreferences.getLong("GPSTrackDriverID", mDriverId);
        editTextName.setText(mPreferences.getString("GPSTrackName", ""));
        userComment.setText(mPreferences.getString("GPSTrackComment", ""));
        ckGpsTrackCreateMileage.setChecked(mPreferences.getBoolean("GPSTrackCreateMileage", true));
        ckGpsTrackUseKML.setChecked(mPreferences.getBoolean("GPSTrackUseKML", true));
        ckGpsTrackUseGPX.setChecked(mPreferences.getBoolean("GPSTrackUseGPX", true));
        ckGpsTrackShowOnMap.setChecked(mPreferences.getBoolean("GPSTrackShowMap", false));
    }

    @Override
   protected void setEditable(ViewGroup vg, boolean editable){
       View child;
       for(int i = 0; i < vg.getChildCount(); i++)
       {
           child = vg.getChildAt(i);
           if(child instanceof ViewGroup){
               setEditable((ViewGroup)child, editable);
           }

           if(!(child.getId() == R.id.gpsTrackCreateMileageCk
                   || child.getId() == R.id.gpsTrackBtnStartStopGpsTrack))
               child.setEnabled(editable);
       }
   }

    private AdapterView.OnItemSelectedListener spinnerCarDriverOnItemSelectedListener =
            new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    if(isActivityOnLoading)
                        return;
                    userCommentAdapter = null;
                    userCommentAdapter = new ArrayAdapter<String>(GPSTrackController.this,
                            android.R.layout.simple_dropdown_item_1line,
                            mMainDbAdapter.getAutoCompleteUserComments(MainDbAdapter.GPSTRACK_TABLE_NAME,
                            spinnerCar.getSelectedItemId(), 30));
                    userComment.setAdapter(userCommentAdapter);
                    mCarId = spinnerCar.getSelectedItemId();
                    mDriverId = spinnerDriver.getSelectedItemId();
                }
                public void onNothingSelected(AdapterView<?> arg0) {
                }
            };

    private View.OnTouchListener spinnerOnTouchListener = new View.OnTouchListener() {

        public boolean onTouch(View view, MotionEvent me) {
            isActivityOnLoading = false;
            return false;
        }
    };

    private View.OnClickListener btnGPSTrackStartStopListener = new View.OnClickListener() {
        public void onClick(View v)
        {
            if(isGpsTrackOn){
                stopService(new Intent(GPSTrackController.this, GPSTrackService.class));
                isGpsTrackOn = false;
                setEditable(vgRoot, true);
                editTextName.setText("");
                editTextName.setHint(Utils.getDateStr(true, true));
                btnGPSTrackStartStop.setText(mRes.getString(R.string.GPSTRACK_ACTIVITY_GPSTRACKSTART_BTN_CAPTION));
            }
            else{
                if(editTextName.getText().toString().length() == 0)
                    editTextName.setText(editTextName.getHint());
                startService(new Intent(GPSTrackController.this, GPSTrackService.class));
                isGpsTrackOn = true; 
                setEditable(vgRoot, false);
//                startActivity(new Intent(GPSTrackController.this, GPSTrackMap.class));
                if(isGpsTrackOn)
                    btnGPSTrackStartStop.setText(
                            mRes.getString(R.string.GPSTRACK_ACTIVITY_GPSTRACKSTOP_BTN_CAPTION));
            }
        };
    };
/*
    private OnClickListener mShowMapListener = new OnClickListener() {
        public void onClick(View v)
        {
            startActivity(new Intent(MainActivity.this, GPSTrackMap.class));
        };
    };
 */
    //    private boolean isGPSTrackingServiceRunning(){
//        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
//        List<ActivityManager.RunningServiceInfo> runningServices = activityManager.getRunningServices(50);
//        String temp;
//        for (int i = 0; i < runningServices.size(); i++) {
//            ActivityManager.RunningServiceInfo runningServiceInfo = runningServices.get(i);
////            if(runningServiceInfo.getClass().equals(GPSTrackService.class))
////                return true;
//            temp = runningServiceInfo.service.getClassName();
//            temp = runningServiceInfo.process;
//        }
//        return false;
//    }
//
}
