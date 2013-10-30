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
 * Link a Bluetooth device to a car
 */
package org.andicar.activity;

import java.util.ArrayList;
import java.util.Set;

import org.andicar.persistence.AddOnDBAdapter;
import org.andicar.persistence.MainDbAdapter;
import org.andicar2.activity.R;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;


/**
 * @author miki
 *
 */
public class BTDeviceCarLink extends EditActivityBase {

	private CheckBox ckIsActive = null;
	private Spinner spnBTPairedDevices = null;
	private EditText etUserComment = null;
	private BluetoothAdapter mBtAdapter;
	private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    ArrayList<String> mPairedDevicesMAC = new ArrayList<String>(); 
    ArrayList<String> mPairedDevicesName = new ArrayList<String>(); 
    private String mMAC = null;
    
	@Override
    public void onCreate( Bundle icicle )
    {
        super.onCreate(icicle);
        init();
        
        ckIsActive = (CheckBox) findViewById( R.id.ckIsActive );
        etUserComment = (EditText)findViewById(R.id.etUserComment);
        spnBTPairedDevices = (Spinner)findViewById(R.id.spnBTPairedDevices);

        String operation = mBundleExtras.getString("Operation"); //E = edit, N = new
        if( operation.equals( "E") ) {
            mRowId = mBundleExtras.getLong( MainDbAdapter.COL_NAME_GEN_ROWID );
            Cursor c = mDbAdapter.fetchRecord(AddOnDBAdapter.ADDON_BTDEVICE_CAR_TABLE_NAME,
            		AddOnDBAdapter.addonBTDeviceCarTableColNames, mRowId);
            String isActive = c.getString( MainDbAdapter.COL_POS_GEN_ISACTIVE );
            mMAC = c.getString( AddOnDBAdapter.ADDON_BTDEVICE_CAR_COL_MACADDR_POS );
            mCarId = c.getLong(AddOnDBAdapter.ADDON_BTDEVICE_CAR_CAR_ID_POS);
            if( isActive != null ) {
                ckIsActive.setChecked( isActive.equals( "Y" ) );
            }
            etUserComment.setText(c.getString(MainDbAdapter.COL_POS_GEN_USER_COMMENT));
            c.close();
        }
        else {
            ckIsActive.setChecked( true );
        }
        
        initControls();

    }
    
    private void init(){
        spnCar = (Spinner) findViewById(R.id.spnCar);
        spnCar.setOnTouchListener(spinnerOnTouchListener);
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
	private void initControls(){
    	String extraWhere = 
    		" AND " + MainDbAdapter.COL_NAME_GEN_ROWID + " " +
    				" NOT IN (SELECT " + AddOnDBAdapter.ADDON_BTDEVICE_CAR_CAR_ID_NAME + " " +
    							" FROM " + AddOnDBAdapter.ADDON_BTDEVICE_CAR_TABLE_NAME + " " +
								" WHERE " + MainDbAdapter.WHERE_CONDITION_ISACTIVE + " " +
										" AND " + AddOnDBAdapter.ADDON_BTDEVICE_CAR_CAR_ID_NAME + " != " + mCarId +
										" )";
    		
        initSpinner(spnCar, MainDbAdapter.TABLE_NAME_CAR, MainDbAdapter.COL_LIST_GEN_ROWID_NAME, 
                new String[]{MainDbAdapter.COL_NAME_GEN_NAME}, MainDbAdapter.WHERE_CONDITION_ISACTIVE + extraWhere, null,
                MainDbAdapter.COL_NAME_GEN_NAME, mCarId, false);
        
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBtAdapter == null){
            madbErrorAlert.setCancelable( false );
            madbErrorAlert.setPositiveButton( mResource.getString(R.string.GEN_OK), null );
            madbErrorAlert.setMessage(mResource.getString(R.string.ERR_064));
            madError = madbErrorAlert.create();
            madError.show();
//            finish();
            return;
        }
        // Get a set of currently paired devices
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
        int i = 0;
        int editId = 0;
        if (pairedDevices.size() > 0) {
        	String[] args = new String[1];
        	Cursor c = null;
        	String tmpStr;
            for (BluetoothDevice device : pairedDevices) {
//                mPairedDevicesArrayAdapter.add(device.getName()); // + "\nMAC:" + device.getAddress()
            	tmpStr = device.getAddress();
            	if(mRowId > 0){
            		if(mMAC != null && mMAC.equals(tmpStr))
            			editId = i;
            	}
            	
            	//eliminate already linked devices excepting the currently edited device
            	args[0] = tmpStr;
            	c = mDbAdapter.query(
            			"SELECT * " +
            			" FROM " + AddOnDBAdapter.ADDON_BTDEVICE_CAR_TABLE_NAME + " " +
    					" WHERE " + MainDbAdapter.WHERE_CONDITION_ISACTIVE + " " +
    							" AND " + MainDbAdapter.COL_NAME_GEN_ROWID + " != " + mRowId + 
    							" AND " + AddOnDBAdapter.ADDON_BTDEVICE_CAR_COL_MACADDR_NAME + " = ? ", args);
            	if(c.moveToNext()){
            		//already linked
            		c.close();
            		continue;
            	}
            	c.close();
            	mPairedDevicesMAC.add(tmpStr);
            	mPairedDevicesName.add(device.getName());
            	i++;
            }
        }
        String[] mPairedDevices = new String[mPairedDevicesName.size()];
        mPairedDevicesName.toArray(mPairedDevices);
        mPairedDevicesArrayAdapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_item, mPairedDevices); // simple_spinner_item
        spnBTPairedDevices.setAdapter(mPairedDevicesArrayAdapter);
        if(editId > 0)
        	spnBTPairedDevices.setSelection(editId);
    }
	
	/* (non-Javadoc)
	 * @see org.andicar.activity.EditActivityBase#saveData()
	 */
	@Override
	protected boolean saveData() {
		if(spnBTPairedDevices.getCount() <= 0 || spnCar.getCount() <= 0){
			finish();
			return false;
		}
        ContentValues cvData = new ContentValues();
        cvData.put( MainDbAdapter.COL_NAME_GEN_ISACTIVE, ckIsActive.isChecked() ? "Y" : "N");
        cvData.put( MainDbAdapter.COL_NAME_GEN_USER_COMMENT, etUserComment.getText().toString() );
        cvData.put( AddOnDBAdapter.ADDON_BTDEVICE_CAR_CAR_ID_NAME, spnCar.getSelectedItemId() );
        cvData.put( AddOnDBAdapter.ADDON_BTDEVICE_CAR_COL_MACADDR_NAME, 
        		mPairedDevicesMAC.get(spnBTPairedDevices.getSelectedItemPosition()));
        cvData.put( MainDbAdapter.COL_NAME_GEN_NAME, 
        		mPairedDevicesName.get(spnBTPairedDevices.getSelectedItemPosition()));
        
        int dbRetVal = -1;
        String strErrMsg = null;
        if (mRowId == -1) {
        	dbRetVal = ((Long)mDbAdapter.createRecord(AddOnDBAdapter.ADDON_BTDEVICE_CAR_TABLE_NAME, cvData)).intValue();
            if(dbRetVal > 0){
            	finish();
            	return true;
            }
            else{
                if(dbRetVal == -1) //DB Error
                    strErrMsg = mDbAdapter.lastErrorMessage;
                else //precondition error
                    strErrMsg = mResource.getString(-1 * dbRetVal);
                madbErrorAlert.setMessage(strErrMsg);
                madError = madbErrorAlert.create();
                madError.show();
                return false;
            }
        } else {
        	dbRetVal = mDbAdapter.updateRecord(AddOnDBAdapter.ADDON_BTDEVICE_CAR_TABLE_NAME, mRowId, cvData);
            if(dbRetVal != -1){
                strErrMsg = mResource.getString(dbRetVal);
                if(dbRetVal == R.string.ERR_000)
                    strErrMsg = strErrMsg + "\n" + mDbAdapter.lastErrorMessage;
                madbErrorAlert.setMessage(strErrMsg);
                madError = madbErrorAlert.create();
                madError.show();
                return false;
            }
            else{
                finish();
                return true;
            }
        }
	}

	/* (non-Javadoc)
	 * @see org.andicar.activity.EditActivityBase#setLayout()
	 */
	@Override
	protected void setLayout() {
   		setContentView(R.layout.addon_btdevice_car_link_s01);
	}

	/* (non-Javadoc)
	 * @see org.andicar.activity.EditActivityBase#setDefaultValues()
	 */
	@Override
	public void setDefaultValues() {
	}

	/* (non-Javadoc)
	 * @see org.andicar.activity.BaseActivity#setSpecificLayout()
	 */
	@Override
	public void setSpecificLayout() {
	}

}
