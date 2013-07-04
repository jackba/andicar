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

import org.andicar.persistence.MainDbAdapter;
import org.andicar2.activity.R;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;

/**
 *
 * @author miki
 */
public class BPartnerLocationEditActivity extends EditActivityBase {
    private EditText etName = null;
    private EditText etUserComment = null;
    private EditText etAddress = null;
    private AutoCompleteTextView acPhone1 = null;
    private AutoCompleteTextView acPhone2 = null;
    private AutoCompleteTextView acPostal = null;
    private AutoCompleteTextView acFax = null;
    private AutoCompleteTextView acCity= null;
    private AutoCompleteTextView acRegion = null;
    private AutoCompleteTextView acCountry = null;
    private AutoCompleteTextView acContact = null;
    private AutoCompleteTextView acEmail = null;
    private CheckBox ckIsActive = null;
//    private ImageButton btnCallPhone1;
//    private ImageButton btnCallPhone2;
//    private ImageButton btnSms1;
//    private ImageButton btnSms2;
    private ImageButton btnEmail;

    private long mBPartnerId = -1;
    private ArrayAdapter<String> cityAdapter;
    private ArrayAdapter<String> regionAdapter;
    private ArrayAdapter<String> countryAdapter;
    private ArrayAdapter<String> contactAdapter;
    private ArrayAdapter<String> emailAdapter;
    private ArrayAdapter<String> phoneAdapter;
    private ArrayAdapter<String> phone2Adapter;
    private ArrayAdapter<String> postalAdapter;
    private ArrayAdapter<String> faxAdapter;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        String strOperationType = mBundleExtras.getString("Operation"); //E = edit, N = new
        mBPartnerId = mBundleExtras.getLong("BPartnerID");

        init();

        if( strOperationType.equals( "E") ) {
            mRowId = mBundleExtras.getLong(MainDbAdapter.COL_NAME_GEN_ROWID);
            Cursor c = mDbAdapter.fetchRecord(MainDbAdapter.TABLE_NAME_BPARTNERLOCATION,
                    MainDbAdapter.COL_LIST_BPARTNERLOCATION_TABLE, mRowId);

            String strName = c.getString( MainDbAdapter.COL_POS_GEN_NAME );
            String strIsActive = c.getString( MainDbAdapter.COL_POS_GEN_ISACTIVE );
            String strUserComment = c.getString( MainDbAdapter.COL_POS_GEN_USER_COMMENT );
            String strAddress = c.getString( MainDbAdapter.COL_POS_BPARTNERLOCATION__ADDRESS );
            String strPhone = c.getString( MainDbAdapter.COL_POS_BPARTNERLOCATION__PHONE );
            String strPhone2 = c.getString( MainDbAdapter.COL_POS_BPARTNERLOCATION__PHONE2 );
            String strPostal = c.getString( MainDbAdapter.COL_POS_BPARTNERLOCATION__POSTAL );
            String strFax = c.getString( MainDbAdapter.COL_POS_BPARTNERLOCATION__FAX );
            String strCity = c.getString( MainDbAdapter.COL_POS_BPARTNERLOCATION__CITY );
            String strRegion = c.getString( MainDbAdapter.COL_POS_BPARTNERLOCATION__REGION );
            String strCountry = c.getString( MainDbAdapter.COL_POS_BPARTNERLOCATION__COUNTRY );
            String strContact = c.getString( MainDbAdapter.COL_POS_BPARTNERLOCATION__CONTACTPERSON );
            String strEmail = c.getString( MainDbAdapter.COL_POS_BPARTNERLOCATION__EMAIL );

            if (strName != null)
                etName.setText(strName);
            if (strIsActive != null) 
                ckIsActive.setChecked(strIsActive.equals("Y"));
            if (strUserComment != null) 
                etUserComment.setText( strUserComment );
            if (strAddress != null)
                etAddress.setText( strAddress );
            if (strPhone != null)
                acPhone1.setText( strPhone );
            if (strPhone2 != null)
                acPhone2.setText( strPhone2 );
            if (strPostal != null)
                acPostal.setText( strPostal );
            if (strFax != null)
                acFax.setText( strFax );
            if (strCity != null)
                acCity.setText( strCity );
            if (strRegion != null)
                acRegion.setText( strRegion );
            if (strCountry != null)
                acCountry.setText( strCountry );
            if (strContact != null)
                acContact.setText( strContact );
            if (strEmail != null)
                acEmail.setText( strEmail );

            c.close();
        } else {
            ckIsActive.setChecked(true);
        }

        initControls();

    }

    private void init() {
        etName = (EditText) findViewById(R.id.etName);
        etUserComment = (EditText) findViewById( R.id.etUserComment );
        ckIsActive = (CheckBox) findViewById(R.id.ckIsActive);
        etAddress = (EditText) findViewById( R.id.etAddress );
        acPhone1 = (AutoCompleteTextView) findViewById( R.id.acPhone1 );
        acPhone2 = (AutoCompleteTextView) findViewById( R.id.acPhone2 );
        acPostal = (AutoCompleteTextView) findViewById( R.id.acPostal );
        acFax = (AutoCompleteTextView) findViewById( R.id.acFax );
        acCity = (AutoCompleteTextView) findViewById( R.id.acCity );
        acRegion = (AutoCompleteTextView) findViewById( R.id.acRegion );
        acCountry = (AutoCompleteTextView) findViewById( R.id.acCountry );
        acContact = (AutoCompleteTextView) findViewById( R.id.acContact );
        acEmail = (AutoCompleteTextView) findViewById( R.id.acEmail );
//        btnCallPhone1 = (ImageButton)findViewById(R.id.btnCallPhone1);
//        btnCallPhone1.setOnClickListener(onBtnClickListener);
//        btnCallPhone2 = (ImageButton)findViewById(R.id.btnCallPhone2);
//        btnCallPhone2.setOnClickListener(onBtnClickListener);
//        btnSms1 = (ImageButton)findViewById(R.id.btnSms1);
//        btnSms1.setOnClickListener(onBtnClickListener);
//        btnSms2 = (ImageButton)findViewById(R.id.btnSms2);
//        btnSms2.setOnClickListener(onBtnClickListener);
        btnEmail = (ImageButton)findViewById(R.id.btnEmail);
        btnEmail.setOnClickListener(onBtnClickListener);
    }

    private void initControls(){
        cityAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                mDbAdapter.getAutoCompleteText(MainDbAdapter.TABLE_NAME_BPARTNERLOCATION, MainDbAdapter.COL_NAME_BPARTNERLOCATION__CITY,
                    -1, 0));
        acCity.setAdapter(cityAdapter);

        regionAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                mDbAdapter.getAutoCompleteText(MainDbAdapter.TABLE_NAME_BPARTNERLOCATION, MainDbAdapter.COL_NAME_BPARTNERLOCATION__REGION,
                    -1, 0));
        acRegion.setAdapter(regionAdapter);

        countryAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                mDbAdapter.getAutoCompleteText(MainDbAdapter.TABLE_NAME_BPARTNERLOCATION, MainDbAdapter.COL_NAME_BPARTNERLOCATION__COUNTRY,
                    -1, 0));
        acCountry.setAdapter(countryAdapter);

        contactAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                mDbAdapter.getAutoCompleteText(MainDbAdapter.TABLE_NAME_BPARTNERLOCATION, MainDbAdapter.COL_NAME_BPARTNERLOCATION__CONTACTPERSON,
                    -1, 0));
        acContact.setAdapter(contactAdapter);

        emailAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                mDbAdapter.getAutoCompleteText(MainDbAdapter.TABLE_NAME_BPARTNERLOCATION, MainDbAdapter.COL_NAME_BPARTNERLOCATION__EMAIL,
                    -1, 0));
        acEmail.setAdapter(emailAdapter);

        phoneAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                mDbAdapter.getAutoCompleteText(MainDbAdapter.TABLE_NAME_BPARTNERLOCATION, MainDbAdapter.COL_NAME_BPARTNERLOCATION__PHONE,
                    mBPartnerId, 0));
        acPhone1.setAdapter(phoneAdapter);

        phone2Adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                mDbAdapter.getAutoCompleteText(MainDbAdapter.TABLE_NAME_BPARTNERLOCATION, MainDbAdapter.COL_NAME_BPARTNERLOCATION__PHONE2,
                    mBPartnerId, 0));
        acPhone2.setAdapter(phone2Adapter);

        postalAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                mDbAdapter.getAutoCompleteText(MainDbAdapter.TABLE_NAME_BPARTNERLOCATION, MainDbAdapter.COL_NAME_BPARTNERLOCATION__POSTAL,
                    -1, 0));
        acPostal.setAdapter(postalAdapter);

        faxAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                mDbAdapter.getAutoCompleteText(MainDbAdapter.TABLE_NAME_BPARTNERLOCATION, MainDbAdapter.COL_NAME_BPARTNERLOCATION__FAX,
                    mBPartnerId, 0));
        acFax.setAdapter(faxAdapter);
    }
    
    @Override
    protected boolean saveData() {
        ContentValues cvData = new ContentValues();
        cvData.put( MainDbAdapter.COL_NAME_GEN_NAME,
                etName.getText().toString());
        cvData.put( MainDbAdapter.COL_NAME_GEN_ISACTIVE,
                (ckIsActive.isChecked() ? "Y" : "N") );
        cvData.put( MainDbAdapter.COL_NAME_GEN_USER_COMMENT,
                etUserComment.getText().toString() );
        cvData.put(MainDbAdapter.COL_NAME_BPARTNERLOCATION__BPARTNER_ID, mBPartnerId);
        cvData.put(MainDbAdapter.COL_NAME_BPARTNERLOCATION__ADDRESS, etAddress.getText().toString());
        cvData.put(MainDbAdapter.COL_NAME_BPARTNERLOCATION__PHONE, acPhone1.getText().toString());
        cvData.put(MainDbAdapter.COL_NAME_BPARTNERLOCATION__PHONE2, acPhone2.getText().toString());
        cvData.put(MainDbAdapter.COL_NAME_BPARTNERLOCATION__POSTAL, acPostal.getText().toString());
        cvData.put(MainDbAdapter.COL_NAME_BPARTNERLOCATION__FAX, acFax.getText().toString());
        cvData.put(MainDbAdapter.COL_NAME_BPARTNERLOCATION__CITY, acCity.getText().toString());
        cvData.put(MainDbAdapter.COL_NAME_BPARTNERLOCATION__REGION, acRegion.getText().toString());
        cvData.put(MainDbAdapter.COL_NAME_BPARTNERLOCATION__COUNTRY, acCountry.getText().toString());
        cvData.put(MainDbAdapter.COL_NAME_BPARTNERLOCATION__CONTACTPERSON, acContact.getText().toString());
        cvData.put(MainDbAdapter.COL_NAME_BPARTNERLOCATION__EMAIL, acEmail.getText().toString());

        int dbRetVal = -1;
        String strErrMsg = null;
        if (mRowId == -1) {
        	dbRetVal = ((Long)mDbAdapter.createRecord(MainDbAdapter.TABLE_NAME_BPARTNERLOCATION, cvData)).intValue();
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
        	dbRetVal = mDbAdapter.updateRecord(MainDbAdapter.TABLE_NAME_BPARTNERLOCATION, mRowId, cvData);
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

    @Override
    protected void setLayout() {
   		setContentView(R.layout.bpartner_location_edit_activity_s01);
    }

    protected View.OnClickListener onBtnClickListener =
            new View.OnClickListener()
                {
                    public void onClick( View v )
                    {
                        Intent actionIntent;
//                        if(v.getId() == R.id.btnCallPhone1 &&
//                                    acPhone1.getText().toString() != null && acPhone1.getText().toString().length() > 0){
//
//                            startActivity(new Intent(Intent.ACTION_CALL,
//                                    Uri.parse("tel:" + acPhone1.getText().toString())));
//                        }
//                        else if(v.getId() == R.id.btnSms1 &&
//                                    acPhone1.getText().toString() != null && acPhone1.getText().toString().length() > 0){
//                            actionIntent = new Intent(Intent.ACTION_VIEW,
//                                    Uri.parse("smsto:" + acPhone1.getText().toString()));
//                            actionIntent.putExtra(android.content.Intent.EXTRA_PHONE_NUMBER, acPhone1.getText().toString());
//                            startActivity(actionIntent);
//                        }
//                        else if(v.getId() == R.id.btnCallPhone2 &&
//                                    acPhone2.getText().toString() != null && acPhone2.getText().toString().length() > 0){
//
//                            startActivity(new Intent(Intent.ACTION_CALL,
//                                    Uri.parse("tel:" + acPhone2.getText().toString())));
//                        }
//                        else if(v.getId() == R.id.btnSms2 &&
//                                    acPhone2.getText().toString() != null && acPhone2.getText().toString().length() > 0){
//
//                            startActivity(new Intent(Intent.ACTION_VIEW,
//                                    Uri.parse("smsto:" + acPhone2.getText().toString())));
//                        }
//                        else 
                    	if(v.getId() == R.id.btnEmail &&
                                    acEmail.getText().toString() != null && acEmail.getText().toString().length() > 0){
                            actionIntent = new Intent(Intent.ACTION_SEND,
                                    Uri.parse("mailto:" + acEmail.getText().toString()));
                            actionIntent.setType("text/html");
                            String to[] = {acEmail.getText().toString()};
                            actionIntent.putExtra(android.content.Intent.EXTRA_EMAIL, to);
                            actionIntent.putExtra(android.content.Intent.EXTRA_TEXT,
                                    "\n\n\n\n--\nSent from AndiCar (http://www.andicar.org)\n" +
                                    "Manage your cars with the power of open source.");
                            startActivity(Intent.createChooser(actionIntent, "Send mail..."));
                        }

                    }
                };

	/* (non-Javadoc)
	 * @see org.andicar.activity.BaseActivity#setSpecificLayout()
	 */
	@Override
	public void setSpecificLayout() {
	}

	/* (non-Javadoc)
	 * @see org.andicar.activity.EditActivityBase#setDefaultValues()
	 */
	@Override
	public void setDefaultValues() {
	}
}
