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

import java.math.BigDecimal;

import org.andicar.activity.BaseActivity;
import org.andicar.activity.R;
import org.andicar.persistence.MainDbAdapter;
import org.andicar.service.GPSTrackService;
import org.andicar.utils.Utils;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;

/**
 *
 * @author miki
 */
public class GPSTrackController extends BaseActivity {
    private Spinner spnCar;
    private Spinner spnDriver;
    private boolean bIsActivityOnLoading = true;
    private ArrayAdapter<String> aaUserComment;
    private ArrayAdapter<String> tagAdapter;
    private AutoCompleteTextView acUserComment;
    private AutoCompleteTextView acTag;
    private EditText etName;
    private EditText etIndexStart;
    private CheckBox ckIsUseKML;
    private CheckBox ckIsUseGPX;
    private CheckBox ckIsCreateMileage;
    private ImageButton btnGPSTrackStartStop;
    private LinearLayout llIndexStartZone;
    private boolean isCreateMileage = true;
    private boolean isGpsTrackOn = false;
    private ViewGroup vgRoot;
    private long mCarId;
    private long mDriverId;
    private long mTagId = 0;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {

        super.onCreate(icicle);
        setContentView(R.layout.gpstrack_controller_activity);

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
        llIndexStartZone = (LinearLayout) findViewById(R.id.llIndexStartZone);
        etName.setHint(Utils.getDateStr(true, true));
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
        bIsActivityOnLoading = true;
        isGpsTrackOn = mPreferences.getBoolean("isGpsTrackOn", false);

        if(isGpsTrackOn){
            btnGPSTrackStartStop.setImageDrawable(mResource.getDrawable(R.drawable.icon_stop24x24));
            restoreState();
            setEditable(vgRoot, false);
        }
        else{
            mCarId = mPreferences.getLong("CurrentCar_ID", -1);
            mDriverId = mPreferences.getLong("CurrentDriver_ID", -1);
            btnGPSTrackStartStop.setImageDrawable(mResource.getDrawable(R.drawable.icon_record24x24));
            setEditable(vgRoot, true);
        }
        initSpinner(spnCar, MainDbAdapter.CAR_TABLE_NAME, MainDbAdapter.genColName,
                new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, MainDbAdapter.isActiveCondition, null, MainDbAdapter.GEN_COL_NAME_NAME,
                mCarId, false);

        initSpinner(spnDriver, MainDbAdapter.DRIVER_TABLE_NAME, MainDbAdapter.genColName,
                new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, MainDbAdapter.isActiveCondition, null, MainDbAdapter.GEN_COL_NAME_NAME,
                mDriverId, false);
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
	       		BigDecimal startIndex = mDbAdapter.getMileageStartIndex(mCarId);
	       		if(startIndex != null){
	       			etIndexStart.setText(startIndex.toString());
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
   private AdapterView.OnItemSelectedListener spinnerCarDriverOnItemSelectedListener =
            new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    if(bIsActivityOnLoading)
                        return;
                    aaUserComment = null;
                    aaUserComment = new ArrayAdapter<String>(GPSTrackController.this,
                            android.R.layout.simple_dropdown_item_1line,
                            mDbAdapter.getAutoCompleteText(MainDbAdapter.GPSTRACK_TABLE_NAME, null,
                            spnCar.getSelectedItemId(), 30));
                    acUserComment.setAdapter(aaUserComment);
                    mCarId = spnCar.getSelectedItemId();
                    mDriverId = spnDriver.getSelectedItemId();
                    fillStartIndex();
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
                mPrefEditor.putBoolean("GPSTrackCreateMileage", isCreateMileage);
                if(isCreateMileage)
                	mPrefEditor.putString("GPSTrackStartIndex", etIndexStart.getText().toString());
                mPrefEditor.commit();
                stopService(gpsTrackIntent);
                isGpsTrackOn = false;
                setEditable(vgRoot, true);
                etName.setText("");
                etName.setHint(Utils.getDateStr(true, true));
                btnGPSTrackStartStop.setImageDrawable(mResource.getDrawable(R.drawable.icon_record24x24));
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

                startService(gpsTrackIntent);
                isGpsTrackOn = true; 
                setEditable(vgRoot, false);
//                startActivity(new Intent(GPSTrackController.this, GPSTrackMap.class));
                if(isGpsTrackOn)
                    btnGPSTrackStartStop.setImageDrawable(mResource.getDrawable(R.drawable.icon_stop24x24));
                finish();
            }
        };
    };
    
}
