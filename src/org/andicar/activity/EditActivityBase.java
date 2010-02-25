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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import java.util.Calendar;
import org.andicar.persistence.MainDbAdapter;
import org.andicar.utils.Constants;
import android.widget.TimePicker;
import android.widget.DatePicker;

/**
 * Base class for all edit activities. Implement common functionalities:
 *  -onCreate:
 *      -aply the layout resource
 *      -initialise global resources: Bundle extras, Resources mRes, SharedPreferences mPreferences
 *      -serach for btnCancel and if exists initialize the OnCLickListener
 *      -serach for btnOk and if exists and initialize the OnCLickListener if it is provided to the onCreate method
 *
 * -implement common intialisations routins for:
 *      -spinners: see initSpinner
 *  * 
 */
public abstract class EditActivityBase extends Activity {
    protected Long mRowId = null;

    protected Bundle extras = null;
    protected Resources mRes = null;
    protected SharedPreferences mPreferences;
    protected Button btnOk = null;
    protected Button btnCancel = null;
    protected MainDbAdapter mMainDbHelper = null;
    protected int mYear;
    protected int mMonth;
    protected int mDay;
    protected int mHour;
    protected int mMinute;
    protected long mDateTimeInSeconds;
    protected TextView mDateTimeLabel;
    protected final Calendar mDateTimeCalendar = Calendar.getInstance();

    private AlertDialog.Builder exceptionAlertBuilder;
    private AlertDialog exceptionAlert;

    protected void onCreate(Bundle icicle, int layoutResID, View.OnClickListener btnOkClickListener){
        super.onCreate(icicle);
        
        setContentView(layoutResID);
        extras = getIntent().getExtras();
        mRes = getResources();
        mPreferences = getSharedPreferences(Constants.GLOBAL_PREFERENCE_NAME, 0);
        mMainDbHelper = new MainDbAdapter(this);

        exceptionAlertBuilder = new AlertDialog.Builder( this );
        exceptionAlertBuilder.setCancelable( false );
        exceptionAlertBuilder.setPositiveButton( mRes.getString(R.string.GEN_OK), null );

        btnCancel = (Button) findViewById( android.R.id.closeButton);
        if(btnCancel != null)
            btnCancel.setOnClickListener(cancelButtonClickListener);

        btnOk = (Button)findViewById( android.R.id.button1 );
        if(btnOkClickListener != null)
            btnOk.setOnClickListener(btnOkClickListener);

    }
    
    /** Use instead onCreate(Bundle icicle, int layoutResID, View.OnClickListener btnOkClickListener) */
    @Override
    @Deprecated
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
    }

    protected void initSpinner(View pSpinner, String tableName, String[] columns, String[] from, String whereCondition, String orderBy,
            long selectedId, boolean addListener){
        try{
            Spinner spinner = (Spinner) pSpinner;
            MainDbAdapter mLocalDbHelper = new MainDbAdapter(this);
            Cursor mCursor = mLocalDbHelper.fetchForTable( tableName, columns, whereCondition, orderBy);
            startManagingCursor( mCursor );
            int[] to = new int[]{android.R.id.text1};
            SimpleCursorAdapter mCursorAdapter =
                    new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, mCursor,
                    from, to);
            mCursorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(mCursorAdapter);

            if(selectedId >= 0){
            //set the spinner to the last used id
                mCursor.moveToFirst();
                for( int i = 0; i < mCursor.getCount(); i++ ) {
                    if( mCursor.getLong( MainDbAdapter.GEN_COL_ROWID_POS ) == selectedId) {
                        spinner.setSelection( i );
                        break;
                    }
                    mCursor.moveToNext();
                }
            }

            if(addListener)
                spinner.setOnItemSelectedListener(spinnerOnItemSelectedListener);
        }
        catch(Exception e){
            exceptionAlertBuilder.setMessage(e.getMessage());
            exceptionAlert = exceptionAlertBuilder.create();
            exceptionAlert.show();
        }

    }

    protected AdapterView.OnItemSelectedListener spinnerOnItemSelectedListener =
            new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

                    if( ((Spinner)arg0).equals(findViewById(R.id.refuelEditExpenseTypeSpinner))){
                        SharedPreferences.Editor editor = mPreferences.edit();
                        editor.putLong("RefuelExpenseType_ID", arg3);
                        editor.commit();
                    }
                    else if( ((Spinner)arg0).equals(findViewById(R.id.mileageEditExpenseTypeSpinner))){
                        SharedPreferences.Editor editor = mPreferences.edit();
                        editor.putLong("MileageInsertExpenseType_ID", arg3);
                        editor.commit();
                    }
                }
                public void onNothingSelected(AdapterView<?> arg0) {
                }
            };

    protected View.OnClickListener cancelButtonClickListener =
            new View.OnClickListener(){
                public void onClick( View v )
                {
                    finish();
                }
            };

   /**
    * Check mandatory fields. Mandatory fields are detected based on view tag (if contain a value => field is mandatory)
    * @return null or field tag if is empty
    */
   protected String checkMandatory(ViewGroup wg){
       View child;
       EditText etChild;
       String retVal;
       for(int i = 0; i < wg.getChildCount(); i++)
       {
           child = wg.getChildAt(i);
           if(child instanceof ViewGroup){
               retVal = checkMandatory((ViewGroup)child);
               if(retVal != null)
                   return retVal;
           }
           else if(child instanceof EditText){
               etChild = (EditText) child;
               if(etChild.getTag() != null && etChild.getTag().toString().length() > 0
                       && (etChild.getText().toString() == null || etChild.getText().toString().length() == 0)){
                   return etChild.getTag().toString();
               }
           }
       }
       return null;
   }

   protected void initDateTime(long dateTimeInMiliseconds){
        mDateTimeInSeconds = dateTimeInMiliseconds / 1000;
        mDateTimeCalendar.setTimeInMillis(dateTimeInMiliseconds);
        mYear = mDateTimeCalendar.get(Calendar.YEAR);
        mMonth = mDateTimeCalendar.get(Calendar.MONTH);
        mDay = mDateTimeCalendar.get(Calendar.DAY_OF_MONTH);
        mHour = mDateTimeCalendar.get(Calendar.HOUR_OF_DAY);
        mMinute = mDateTimeCalendar.get(Calendar.MINUTE);

        mDateTimeLabel = (TextView) findViewById(R.id.genDateTimeText);
        updateDateTime();

        Button pickDate = (Button) findViewById(R.id.genPickDateButton);
        pickDate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                showDialog(Constants.DATE_DIALOG_ID);
            }
        });

        Button pickTime = (Button) findViewById(R.id.genPickTimeButton);
        pickTime.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                showDialog(Constants.TIME_DIALOG_ID);
            }
        });
   }
    @Override
    protected Dialog onCreateDialog(int id) {
        switch(id) {
            case Constants.TIME_DIALOG_ID:
                return new TimePickerDialog(this,
                        mTimeSetListener, mHour, mMinute, false);
            case Constants.DATE_DIALOG_ID:
                return new DatePickerDialog(this,
                        mDateSetListener,
                        mYear, mMonth, mDay);
        }
        return null;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch(id) {
            case Constants.TIME_DIALOG_ID:
                ((TimePickerDialog) dialog).updateTime(mHour, mMinute);
                break;
            case Constants.DATE_DIALOG_ID:
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
        mDateTimeCalendar.set(mYear, mMonth, mDay, mHour, mMinute, 0);
        mDateTimeInSeconds = mDateTimeCalendar.getTimeInMillis() / 1000;
        mDateTimeLabel.setText(
                new StringBuilder() // Month is 0 based so add 1
                .append(pad(mMonth + 1)).append("-").append(pad(mDay)).append("-").append(mYear).append(" ").append(pad(mHour)).append(":").append(pad(mMinute)));
    }

    private static String pad(int c) {
        if(c >= 10) {
            return String.valueOf(c);
        }
        else {
            return "0" + String.valueOf(c);
        }
    }
}
