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

package org.andicar.activity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.util.Calendar;
import org.andicar.persistence.MainDbAdapter;
import org.andicar.utils.StaticValues;
import android.widget.TimePicker;
import android.widget.DatePicker;
import org.andicar.utils.AndiCarStatistics;
import org.andicar.utils.Utils;

/**
 * Base class for all edit activities. Implement common functionalities:
 *  -onCreate:
 *      -aply the layout resource
 *      -initialise global resources: Bundle mBundleExtras, Resources mResource, SharedPreferences mPreferences
 *      -serach for btnCancel and if exists initialize the OnCLickListener
 *      -serach for btnOk and if exists and initialize the OnCLickListener if it is provided to the onCreate method
 *
 * -implement common intialisations routins for:
 *      -spinners: see initSpinner
 *  * 
 */
public abstract class EditActivityBase extends BaseActivity {
    protected long mRowId = -1;
    protected Bundle mBundleExtras = null;
    protected Button btnOk = null;
    protected Button btnCancel = null;
    protected int mYear;
    protected int mMonth;
    protected int mDay;
    protected int mHour;
    protected int mMinute;
    protected long mlDateTimeInSeconds;
    protected TextView tvDateTimeValue;
    protected final Calendar mcalDateTime = Calendar.getInstance();
//    protected int mLayoutResID = -1;

    abstract void saveData();
    abstract void setLayout();
    
    @Override
    protected void onStart()
    {
        super.onStart();
        if(isSendStatistics)
            AndiCarStatistics.sendFlurryStartSession(this);
    }
    @Override
    protected void onStop()
    {
        super.onStop();
        if(isSendStatistics)
            AndiCarStatistics.sendFlurryEndSession(this);
    }
    
    /** 
     * Use instead onCreate(Bundle icicle, int layoutResID, View.OnClickListener btnOkClickListener)
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setLayout();

        mBundleExtras = getIntent().getExtras();

        btnCancel = (Button) findViewById( android.R.id.closeButton);
        if(btnCancel != null)
            btnCancel.setOnClickListener(mCancelClickListener);

        btnOk = (Button)findViewById( android.R.id.button1 );
        if(mOkClickListener != null)
            btnOk.setOnClickListener(mOkClickListener);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mRowId = savedInstanceState.getLong("mRowId");
        mlDateTimeInSeconds = savedInstanceState.getLong("mlDateTimeInSeconds");
        mYear = savedInstanceState.getInt("mYear");
        mMonth = savedInstanceState.getInt("mMonth");
        mDay = savedInstanceState.getInt("mDay");
        mHour = savedInstanceState.getInt("mHour");
        mMinute = savedInstanceState.getInt("mMinute");
    }

    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("mRowId", mRowId);
        outState.putLong("mlDateTimeInSeconds", mlDateTimeInSeconds);
        outState.putInt("mYear", mYear);
        outState.putInt("mMonth", mMonth);
        outState.putInt("mDay", mDay);
        outState.putInt("mHour", mHour);
        outState.putInt("mMinute", mMinute);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(mDbAdapter == null)
            mDbAdapter = new MainDbAdapter(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mDbAdapter != null){
            mDbAdapter.close();
            mDbAdapter = null;
        }
    }

    protected View.OnClickListener mCancelClickListener =
            new View.OnClickListener(){
                public void onClick( View v )
                {
                    finish();
                }
            };

    protected View.OnClickListener mOkClickListener =
            new View.OnClickListener()
                {
                    public void onClick( View v )
                    {
                        saveData();
                    }
                };


   /**
    * Check mandatory fields. Mandatory fields are detected based on view tag (if contain a value => field is mandatory)
    * @return null or field tag if is empty
    */
   protected String checkMandatory(ViewGroup wg){
       View vwChild;
       EditText etChild;
       String strRetVal;
       for(int i = 0; i < wg.getChildCount(); i++)
       {
           vwChild = wg.getChildAt(i);
           if(vwChild instanceof ViewGroup){
               strRetVal = checkMandatory((ViewGroup)vwChild);
               if(strRetVal != null)
                   return strRetVal;
           }
           else if(vwChild instanceof EditText){
               etChild = (EditText) vwChild;
               if(etChild.getTag() != null && etChild.getTag().toString().length() > 0
                       && (etChild.getText().toString() == null || etChild.getText().toString().length() == 0)){
                   return etChild.getTag().toString();
               }
           }
       }
       return null;
   }

   protected void setEditable(ViewGroup vg, boolean editable){
       View vwChild;
       for(int i = 0; i < vg.getChildCount(); i++)
       {
           vwChild = vg.getChildAt(i);
           if(vwChild instanceof ViewGroup){
               setEditable((ViewGroup)vwChild, editable);
           }
           if(vwChild.getId() != R.id.tvWarningLabel)
               vwChild.setEnabled(editable);
       }
   }

   protected void initDateTime(long dateTimeInMiliseconds){
        mlDateTimeInSeconds = dateTimeInMiliseconds / 1000;
        mcalDateTime.setTimeInMillis(dateTimeInMiliseconds);
        mYear = mcalDateTime.get(Calendar.YEAR);
        mMonth = mcalDateTime.get(Calendar.MONTH);
        mDay = mcalDateTime.get(Calendar.DAY_OF_MONTH);
        mHour = mcalDateTime.get(Calendar.HOUR_OF_DAY);
        mMinute = mcalDateTime.get(Calendar.MINUTE);

        tvDateTimeValue = (TextView) findViewById(R.id.tvDateTimeValue);
        updateDateTime();

        Button btnPickDate = (Button) findViewById(R.id.btnPickDate);
        if(btnPickDate != null)
            btnPickDate.setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {
                    showDialog(StaticValues.DATE_DIALOG_ID);
                }
            });

        Button btnPickTime = (Button) findViewById(R.id.btnPickTime);
        if(btnPickTime != null)
            btnPickTime.setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {
                    showDialog(StaticValues.TIME_DIALOG_ID);
                }
            });
   }
    @Override
    protected Dialog onCreateDialog(int id) {
        switch(id) {
            case StaticValues.TIME_DIALOG_ID:
                return new TimePickerDialog(this,
                        mTimeSetListener, mHour, mMinute, false);
            case StaticValues.DATE_DIALOG_ID:
                return new DatePickerDialog(this,
                        mDateSetListener,
                        mYear, mMonth, mDay);
        }
        return null;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch(id) {
            case StaticValues.TIME_DIALOG_ID:
                ((TimePickerDialog) dialog).updateTime(mHour, mMinute);
                break;
            case StaticValues.DATE_DIALOG_ID:
                ((DatePickerDialog) dialog).updateDate(mYear, mMonth, mDay);
                break;
        }
    }
    private DatePickerDialog.OnDateSetListener mDateSetListener =
            new DatePickerDialog.OnDateSetListener() {
                public void onDateSet(DatePicker view, int year, int monthOfYear,
                        int dayOfMonth) {
                    mYear = year;
                    mMonth = monthOfYear;
                    mDay = dayOfMonth;
                    updateDateTime();
                }
            };
            
    private TimePickerDialog.OnTimeSetListener mTimeSetListener =
            new TimePickerDialog.OnTimeSetListener() {
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    mHour = hourOfDay;
                    mMinute = minute;
                    updateDateTime();
                }
            };

    private void updateDateTime() {
        mcalDateTime.set(mYear, mMonth, mDay, mHour, mMinute, 0);
        mlDateTimeInSeconds = mcalDateTime.getTimeInMillis() / 1000;
        tvDateTimeValue.setText(
                new StringBuilder() // Month is 0 based so add 1
                .append(Utils.pad(mMonth + 1, 2)).append("-")
                .append(Utils.pad(mDay, 2)).append("-")
                .append(mYear).append(" ")
                .append(Utils.pad(mHour, 2)).append(":").append(Utils.pad(mMinute, 2)));
    }

}
