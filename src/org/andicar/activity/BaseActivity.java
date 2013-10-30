/*
 *  AndiCar - car management software for Android powered devices
 *  Copyright (C) 2010 Miklos Keresztes (miklos.keresztes@gmail.com)
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT AY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.andicar.activity;

import java.math.BigDecimal;

import org.andicar.activity.dialog.AndiCarDialogBuilder;
import org.andicar.persistence.MainDbAdapter;
import org.andicar.utils.AndiCarExceptionHandler;
import org.andicar.utils.StaticValues;
import org.andicar2.activity.MainActivity;
import org.andicar2.activity.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

/**
 *
 * @author Miklos Keresztes
 */
public abstract class BaseActivity extends Activity {
    protected MainDbAdapter mDbAdapter = null;
    protected Resources mResource = null;
    protected SharedPreferences mPreferences;
    protected AndiCarDialogBuilder madbErrorAlert;
    
    protected AlertDialog madError;
    protected View mDialog;
    protected ViewGroup vgRoot;
    protected Spinner spnCar;
    protected Spinner spnDriver;
    protected Spinner spnCurrency;
    protected Spinner spnExpType;
    protected Spinner spnExpCategory;
    protected EditText etName = null;
    

    protected SharedPreferences.Editor mPrefEditor;
    protected boolean isSendCrashReport;
    protected boolean isUseNumericInput = true;

    protected long mCarId = -1;
    protected long mDriverId = -1;
    protected long mCurrencyId = -1;
    protected long mExpCategoryId = -1;
    protected long mExpTypeId = -1;

    protected boolean isBackgroundSettingsActive = false;

    public abstract void setSpecificLayout();

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        mPreferences = getSharedPreferences(StaticValues.GLOBAL_PREFERENCE_NAME, Context.MODE_MULTI_PROCESS);
        isSendCrashReport = mPreferences.getBoolean("SendCrashReport", true);
        if(isSendCrashReport)
            Thread.setDefaultUncaughtExceptionHandler(
                    new AndiCarExceptionHandler(Thread.getDefaultUncaughtExceptionHandler(), this));

        mPrefEditor = mPreferences.edit();
        mDbAdapter = new MainDbAdapter(this);
        mResource = getResources();

        madbErrorAlert = new AndiCarDialogBuilder(BaseActivity.this, AndiCarDialogBuilder.DIALOGTYPE_ERROR, mResource.getString(R.string.GEN_Error));
        madbErrorAlert.setCancelable( false );
        madbErrorAlert.setPositiveButton( mResource.getString(R.string.GEN_OK), null );

        isUseNumericInput = mPreferences.getBoolean("UseNumericKeypad", true);
    }

    @Override
    protected void onDestroy() {
        try{
            super.onDestroy();
            if(mDbAdapter != null){
                mDbAdapter.close();
                mDbAdapter = null;
            }
        }catch(Exception e){}
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(mDbAdapter == null)
            mDbAdapter = new MainDbAdapter(this);

        LinearLayout fakeFocus = (LinearLayout)findViewById(R.id.fakeFocus);
		if(fakeFocus != null)
			fakeFocus.requestFocus();
    }
    
    public void setSpinnerSelectedID(Spinner sp, long id){
    	SimpleCursorAdapter sca = (SimpleCursorAdapter)sp.getAdapter();
    	if(sca == null)
    		return;
    	int count = sca.getCount();
    	
    	for(int i = 0; i < count; i++){
    		if(sca.getItemId(i) == id){
    			sp.setSelection(i);
    			return;
    		}
    	}
    }

    public void initSpinner(View pSpinner, String tableName, String[] columns, String[] from,
            String selection, String[] selectionArgs,
            String orderBy, long selectedId, boolean addEmptyValue){
        try{
            Spinner spnCurrentSpinner = (Spinner) pSpinner;
            Cursor dbcRecordCursor;
            if(addEmptyValue){
                String selectSql = "SELECT -1 AS " + MainDbAdapter.COL_NAME_GEN_ROWID + ", " +
                                    "null AS " + MainDbAdapter.COL_NAME_GEN_NAME +
                                    " UNION " +
                                    " SELECT " + MainDbAdapter.COL_NAME_GEN_ROWID + ", " +
                                                MainDbAdapter.COL_NAME_GEN_NAME +
                                    " FROM " + tableName;
                if(selection != null && selection.length() > 0)
                    selectSql = selectSql + " WHERE " + selection;
                selectSql = selectSql + " ORDER BY " + MainDbAdapter.COL_NAME_GEN_NAME;
                dbcRecordCursor = mDbAdapter.execSelectSql(selectSql, selectionArgs);
            }
            else{
                dbcRecordCursor = mDbAdapter.query(tableName, columns, selection, selectionArgs, null, null, orderBy);
            }
            
            startManagingCursor( dbcRecordCursor );
            int[] to = new int[]{android.R.id.text1};
            SimpleCursorAdapter scaCursorAdapter =
                    new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, dbcRecordCursor,
                    from, to);
            scaCursorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spnCurrentSpinner.setAdapter(scaCursorAdapter);

            if(selectedId >= 0){
            //set the spinner to this id
                dbcRecordCursor.moveToFirst();
                for( int i = 0; i < dbcRecordCursor.getCount(); i++ ) {
                    if( dbcRecordCursor.getLong( MainDbAdapter.COL_POS_GEN_ROWID ) == selectedId) {
                        spnCurrentSpinner.setSelection( i );
                        break;
                    }
                    dbcRecordCursor.moveToNext();
                }
            }
            if(spnCurrentSpinner.getOnItemSelectedListener() == null)
                spnCurrentSpinner.setOnItemSelectedListener(spinnerOnItemSelectedListener);
        }
        catch(Exception e){
            madbErrorAlert.setMessage(e.getMessage());
            madError = madbErrorAlert.create();
            madError.show();
        }

    }

    /**
     * Check numeric fields. Numeric fields are detected based on input type (TYPE_CLASS_PHONE)
     * @param integerOnly just integer allowed
     * @return null or field tag if is empty
     */
    protected String checkNumeric(ViewGroup wg, boolean integerOnly){
        View vwChild;
        EditText etChild;
        String strRetVal;
        if(wg == null)
            return null;

        for(int i = 0; i < wg.getChildCount(); i++)
        {
            vwChild = wg.getChildAt(i);
            if(vwChild instanceof ViewGroup){
                strRetVal = checkNumeric((ViewGroup)vwChild, integerOnly);
                if(strRetVal != null)
                    return strRetVal;
            }
            else if(vwChild instanceof EditText){
                etChild = (EditText) vwChild;
                String sValue = ((EditText)etChild).getText().toString();
                if(etChild.getOnFocusChangeListener() == null &&
                        (etChild.getInputType() == InputType.TYPE_CLASS_PHONE
                             || etChild.getInputType() == InputType.TYPE_CLASS_NUMBER)) {
                     if(sValue != null && sValue.length() > 0)
                     {
                         try{
                             //check if valid number
                        	 if(integerOnly)
                        		 Integer.parseInt(sValue);
                        	 else
                        		 new BigDecimal(sValue);
                         }
                         catch(NumberFormatException e){
                         	if(etChild.getTag() != null && etChild.getTag().toString() != null)
                         		return etChild.getTag().toString().replace(":", "");
                         	else
                         		return "";
                         	
                         }
                     }
                }
            }
        }
        return null;
    }

    protected void setSpinnerTextToCode(AdapterView<?> arg0, long arg3, View arg1) {
        if(arg1 == null)
            return;
        String code = null;
        //set the spinner text to the selected item code
        if ((((Spinner) arg0).equals(findViewById(R.id.spnUomFrom))
                || ((Spinner) arg0).equals(findViewById(R.id.spnUomLength))
                || ((Spinner) arg0).equals(findViewById(R.id.spnUomTo))
                || ((Spinner) arg0).equals(findViewById(R.id.spnUomVolume))
                )
                && arg3 > 0) {
            code = mDbAdapter.getUOMCode(arg3);
            if (code != null) {
                ((TextView) arg1).setText(code);
            }
        } else if ((((Spinner) arg0).equals(findViewById(R.id.spnCurrency))
                || ((Spinner) arg0).equals(findViewById(R.id.spnCurrencyFrom))
                || ((Spinner) arg0).equals(findViewById(R.id.spnCurrencyTo)))
                && arg3 > 0) {
            code = mDbAdapter.getCurrencyCode(arg3);
            if (code != null) {
                ((TextView) arg1).setText(code);
            }
        }
    }

    protected AdapterView.OnItemSelectedListener spinnerOnItemSelectedListener =
            new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                	
                	if(isBackgroundSettingsActive)
                		return;
                	
                    /*if(BaseActivity.this instanceof RefuelEditActivity){
                        if( ((Spinner)arg0).equals(findViewById(R.id.spnExpType))){
                            mPrefEditor.putLong("RefuelExpenseType_ID", arg3);
                            mPrefEditor.commit();
                        }
                        else if( ((Spinner)arg0).equals(findViewById(R.id.spnExpCategory))){
                            mPrefEditor.putLong("RefuelExpenseCategory_ID", arg3);
                            mPrefEditor.commit();
                        }
                    }
                    else if(BaseActivity.this instanceof MileageEditActivity){
                        if( ((Spinner)arg0).equals(findViewById(R.id.spnExpType))){
                            mPrefEditor.putLong("MileageInsertExpenseType_ID", arg3);
                            mPrefEditor.commit();
                        }
                    }
                    else*/ if(BaseActivity.this instanceof MainActivity){
                        if( ((Spinner)arg0).equals(findViewById(R.id.spnCar))){
                        	mCarId = arg3;
                            mPrefEditor.putLong("CurrentCar_ID", arg3);
                            mPrefEditor.commit();
                        }
                    }
                    setSpinnerTextToCode(arg0, arg3, arg1);
                }
                
                public void onNothingSelected(AdapterView<?> arg0) {
                }
            };


    /**
     * set the input type associated to a numeric field
     * on some devices the input type PHONE does not show the dot simbol
     * see issue #29
     * @param wg
     */
    protected void setInputType(ViewGroup wg){
        if(wg == null)
            return;

        View vwChild;
        EditText etChild;

        for(int i = 0; i < wg.getChildCount(); i++)
        {
            vwChild = wg.getChildAt(i);
            if(vwChild instanceof ViewGroup){
                setInputType((ViewGroup)vwChild);
            }
            else if(vwChild instanceof EditText){
                etChild = (EditText) vwChild;
                if(etChild.getInputType() == InputType.TYPE_CLASS_PHONE
                             || etChild.getInputType() == InputType.TYPE_CLASS_NUMBER) { //numeric field
                     if(isUseNumericInput)
                         etChild.setRawInputType(InputType.TYPE_CLASS_PHONE);
                     else
                         etChild.setRawInputType(InputType.TYPE_CLASS_NUMBER);
                }
            }
        }
    }
    
    
    
    public ViewGroup getRootViewGroup(){
    	return vgRoot;
    }

	/**
	 * @param currencyId the mCurrencyId to set
	 */
	public void setCurrencyId(long currencyId) {
		this.mCurrencyId = currencyId;
	}

	/**
	 * @param expCategoryId the mExpCategoryId to set
	 */
	public void setExpCategoryId(long expCategoryId) {
		this.mExpCategoryId = expCategoryId;
	}

	/**
	 * @param expTypeId the mExpTypeId to set
	 */
	public void setExpTypeId(long expTypeId) {
		this.mExpTypeId = expTypeId;
	}

	/**
	 * @return the mCarId
	 */
	public long getCarId() {
		return mCarId;
	}

	/**
	 * @return the mDriverId
	 */
	public long getDriverId() {
		return mDriverId;
	}

	/**
	 * @return the mCurrencyId
	 */
	public long getCurrencyId() {
		return mCurrencyId;
	}

	/**
	 * @return the mExpCategoryId
	 */
	public long getExpCategoryId() {
		return mExpCategoryId;
	}

	/**
	 * @return the mExpTypeId
	 */
	public long getExpTypeId() {
		return mExpTypeId;
	}
}
