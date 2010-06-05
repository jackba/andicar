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

package org.andicar.activity.miscellaneous;

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
import org.andicar.activity.BaseActivity;
import org.andicar.activity.R;
import org.andicar.persistence.MainDbAdapter;
import org.andicar.service.GPSTrackService;
import org.andicar.utils.Utils;

/**
 *
 * @author miki
 */
public class GPSTrackController extends BaseActivity {
    private Spinner spnCar;
    private Spinner spnDriver;
    private boolean bIsActivityOnLoading = true;
    private ArrayAdapter<String> aaUserComment;
    private AutoCompleteTextView acUserComment;
    private EditText etName;
    private CheckBox ckIsUseKML;
    private CheckBox ckIsUseGPX;
    private CheckBox ckIsCreateMileage;
    private Button btnGPSTrackStartStop;
    private boolean isGpsTrackOn = false;
    private ViewGroup vgRoot;
    private long mCarId;
    private long mDriverId;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {

        super.onCreate(icicle);
        setContentView(R.layout.gpstrackcontroller_activity);

        mCarId = mPreferences.getLong("CurrentCar_ID", -1);
        mDriverId = mPreferences.getLong("CurrentDriver_ID", -1);

        vgRoot = (ViewGroup) findViewById(R.id.vgRoot);
        spnCar = (Spinner)findViewById(R.id.spnCar);
        spnDriver = (Spinner)findViewById(R.id.spnDriver);
        spnCar.setOnItemSelectedListener(spinnerCarDriverOnItemSelectedListener);
        spnDriver.setOnItemSelectedListener(spinnerCarDriverOnItemSelectedListener);
        spnCar.setOnTouchListener(spinnerOnTouchListener);
        spnDriver.setOnTouchListener(spinnerOnTouchListener);
        acUserComment = ((AutoCompleteTextView) findViewById( R.id.acUserComment ));
        etName = (EditText) findViewById(R.id.etName);
        etName.setHint(Utils.getDateStr(true, true));
        ckIsUseKML = (CheckBox) findViewById(R.id.ckIsUseKML);
        ckIsUseKML.setChecked(mPreferences.getBoolean("IsUseKMLTrack", true));
        ckIsUseGPX = (CheckBox) findViewById(R.id.ckIsUseGPX);
        ckIsUseGPX.setChecked(mPreferences.getBoolean("IsUseGPXTrack", true));
//        ckIsShowOnMap = (CheckBox) findViewById(R.id.ckIsShowOnMap);
//        ckIsShowOnMap.setChecked(mPreferences.getBoolean("IsGPSTrackOnMap", true));
        btnGPSTrackStartStop = (Button) findViewById(R.id.btnStartStopGpsTrack);
        btnGPSTrackStartStop.setOnClickListener(btnGPSTrackStartStopListener);
        ckIsCreateMileage = (CheckBox) findViewById(R.id.ckIsCreateMileage);
        ckIsCreateMileage.setChecked(mPreferences.getBoolean("GPSTrackCreateMileage", true));
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bIsActivityOnLoading = true;
        isGpsTrackOn = mPreferences.getBoolean("isGpsTrackOn", false);

        if(isGpsTrackOn){
            btnGPSTrackStartStop.setText(mResource.getString(R.string.GPSTrackControllerActivity_StopBtnCaption));
            restoreState();
            setEditable(vgRoot, false);
        }
        else{
            mCarId = mPreferences.getLong("CurrentCar_ID", -1);
            mDriverId = mPreferences.getLong("CurrentDriver_ID", -1);
            btnGPSTrackStartStop.setText(mResource.getString(R.string.GPSTrackControllerActivity_StartBtnCaption));
            setEditable(vgRoot, true);
        }
        initSpinner(spnCar, MainDbAdapter.CAR_TABLE_NAME, MainDbAdapter.genColName,
                new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, MainDbAdapter.isActiveCondition, MainDbAdapter.GEN_COL_NAME_NAME,
                mCarId);

        initSpinner(spnDriver, MainDbAdapter.DRIVER_TABLE_NAME, MainDbAdapter.genColName,
                new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, MainDbAdapter.isActiveCondition, MainDbAdapter.GEN_COL_NAME_NAME,
                mDriverId);
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
        editor.putBoolean("GPSTrackCreateMileage", ckIsCreateMileage.isChecked());
        editor.putBoolean("GPSTrackUseKML", ckIsUseKML.isChecked());
        editor.putBoolean("GPSTrackUseGPX", ckIsUseGPX.isChecked());
//        editor.putBoolean("GPSTrackShowMap", ckIsShowOnMap.isChecked());
        editor.commit();
    }

    private void restoreState(){
        mCarId = mPreferences.getLong("GPSTrackCarID", mCarId);
        mDriverId = mPreferences.getLong("GPSTrackDriverID", mDriverId);
        etName.setText(mPreferences.getString("GPSTrackName", ""));
        acUserComment.setText(mPreferences.getString("GPSTrackComment", ""));
        ckIsCreateMileage.setChecked(mPreferences.getBoolean("GPSTrackCreateMileage", true));
        ckIsUseKML.setChecked(mPreferences.getBoolean("GPSTrackUseKML", true));
        ckIsUseGPX.setChecked(mPreferences.getBoolean("GPSTrackUseGPX", true));
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
                   || child.getId() == R.id.btnStartStopGpsTrack))
               child.setEnabled(editable);
       }
   }

    private AdapterView.OnItemSelectedListener spinnerCarDriverOnItemSelectedListener =
            new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    if(bIsActivityOnLoading)
                        return;
                    aaUserComment = null;
                    aaUserComment = new ArrayAdapter<String>(GPSTrackController.this,
                            android.R.layout.simple_dropdown_item_1line,
                            mDbAdapter.getAutoCompleteUserComments(MainDbAdapter.GPSTRACK_TABLE_NAME,
                            spnCar.getSelectedItemId(), 30));
                    acUserComment.setAdapter(aaUserComment);
                    mCarId = spnCar.getSelectedItemId();
                    mDriverId = spnDriver.getSelectedItemId();
                }
                public void onNothingSelected(AdapterView<?> arg0) {
                }
            };

    private View.OnTouchListener spinnerOnTouchListener = new View.OnTouchListener() {

        public boolean onTouch(View view, MotionEvent me) {
            bIsActivityOnLoading = false;
            return false;
        }
    };

    private View.OnClickListener btnGPSTrackStartStopListener = new View.OnClickListener() {
        public void onClick(View v)
        {
            Intent gpsTrackIntent = new Intent(GPSTrackController.this, GPSTrackService.class);
            if(isGpsTrackOn){
                mPrefEditor.putBoolean("GPSTrackCreateMileage", ckIsCreateMileage.isChecked());
                mPrefEditor.commit();
                stopService(gpsTrackIntent);
                isGpsTrackOn = false;
                setEditable(vgRoot, true);
                etName.setText("");
                etName.setHint(Utils.getDateStr(true, true));
                btnGPSTrackStartStop.setText(mResource.getString(R.string.GPSTrackControllerActivity_StartBtnCaption));
                mPrefEditor.putBoolean("isGpsTrackOn", false);
                mPrefEditor.commit();
                finish();
            }
            else{
                if(etName.getText().toString().length() == 0)
                    etName.setText(etName.getHint());

                mPrefEditor.putString("GPSTrackTmp_Name", etName.getText().toString());
                mPrefEditor.putString("GPSTrackTmp_UserComment", acUserComment.getText().toString());
                mPrefEditor.putLong("GPSTrackTmp_CarId", mCarId);
                mPrefEditor.putLong("GPSTrackTmp_DriverId", mDriverId);
                mPrefEditor.putBoolean("GPSTrackTmp_IsUseKML", ckIsUseKML.isChecked());
                mPrefEditor.putBoolean("GPSTrackTmp_IsUseGPX", ckIsUseGPX.isChecked());
//                mPrefEditor.putBoolean("GPSTrackTmp_IsShowOnMap", ckIsShowOnMap.isChecked());
                mPrefEditor.commit();

                startService(gpsTrackIntent);
                isGpsTrackOn = true; 
                setEditable(vgRoot, false);
//                startActivity(new Intent(GPSTrackController.this, GPSTrackMap.class));
                if(isGpsTrackOn)
                    btnGPSTrackStartStop.setText(
                            mResource.getString(R.string.GPSTrackControllerActivity_StopBtnCaption));
                finish();
            }
        };
    };
}