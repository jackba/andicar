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

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Spinner;
import org.andicar.persistence.MainDbAdapter;
import org.andicar.utils.Utils;

/**
 *
 * @author miki
 */
public class GPSTrackController extends EditActivityBase {
    private Spinner carSpinner;
    private Spinner driverSpinner;
    private boolean isActivityOnLoading = true;
    private ArrayAdapter<String> userCommentAdapter;
    private AutoCompleteTextView userComment;
    private EditText nameText;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        long mCarId;
        long mDriverId;

        super.onCreate( icicle, R.layout.gpstrackcontroller_activity, null );
        carSpinner = (Spinner)findViewById(R.id.carSpinner);
        driverSpinner = (Spinner)findViewById(R.id.driverSpinner);
        carSpinner.setOnItemSelectedListener(spinnerCarDriverOnItemSelectedListener);
        driverSpinner.setOnItemSelectedListener(spinnerCarDriverOnItemSelectedListener);
        carSpinner.setOnTouchListener(spinnerOnTouchListener);
        driverSpinner.setOnTouchListener(spinnerOnTouchListener);
        userComment = ((AutoCompleteTextView) findViewById( R.id.genUserCommentEntry ));
        nameText = (EditText) findViewById(R.id.genNameEntry);

        nameText.setHint(Utils.getDateStr(true, true));
        
        mCarId = mPreferences.getLong("CurrentCar_ID", -1);
        mDriverId = mPreferences.getLong("CurrentDriver_ID", -1);

        initSpinner(carSpinner, MainDbAdapter.CAR_TABLE_NAME, MainDbAdapter.genColName,
                new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, MainDbAdapter.isActiveCondition, MainDbAdapter.GEN_COL_NAME_NAME,
                mCarId, false);

        initSpinner(driverSpinner, MainDbAdapter.DRIVER_TABLE_NAME, MainDbAdapter.genColName,
                new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, MainDbAdapter.isActiveCondition, MainDbAdapter.GEN_COL_NAME_NAME,
                mDriverId, false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isActivityOnLoading = true;
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
                            carSpinner.getSelectedItemId(), 30));
                    userComment.setAdapter(userCommentAdapter);
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


//        if(isGpsTrackOn)
//            btnStartStopGpsTrack.setText(mRes.getString(R.string.MAIN_ACTIVITY_GPSTRACKSTOP_BTN_CAPTION));
//        else
//            btnStartStopGpsTrack.setText(mRes.getString(R.string.MAIN_ACTIVITY_GPSTRACKSTART_BTN_CAPTION));


/*
    private OnClickListener mStartStopGPStrackListener = new OnClickListener() {
        public void onClick(View v)
        {
            if(isGpsTrackOn){
                stopService(new Intent(MainActivity.this, GPSTrackService.class));
                isGpsTrackOn = false;
                btnStartStopGpsTrack.setText(mRes.getString(R.string.MAIN_ACTIVITY_GPSTRACKSTART_BTN_CAPTION));
            }
            else{
                startService(new Intent(MainActivity.this, GPSTrackService.class));
                isGpsTrackOn = true; //mPreferences.getBoolean("isGpsTrackOn", false); //check if the service is started succesfull
                startActivity(new Intent(MainActivity.this, GPSTrackMap.class));
                if(isGpsTrackOn)
                    btnStartStopGpsTrack.setText(
                            mRes.getString(R.string.MAIN_ACTIVITY_GPSTRACKSTOP_BTN_CAPTION));
            }
        };
    };

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
