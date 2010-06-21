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

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import org.andicar.persistence.MainDbAdapter;

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
            mRowId = mBundleExtras.getLong(MainDbAdapter.GEN_COL_ROWID_NAME);
            Cursor c = mDbAdapter.fetchRecord(MainDbAdapter.BPARTNER_LOCATION_TABLE_NAME,
                    MainDbAdapter.bpartnerLocationTableColNames, mRowId);

            String strName = c.getString( MainDbAdapter.GEN_COL_NAME_POS );
            String strIsActive = c.getString( MainDbAdapter.GEN_COL_ISACTIVE_POS );
            String strUserComment = c.getString( MainDbAdapter.GEN_COL_USER_COMMENT_POS );
            String strAddress = c.getString( MainDbAdapter.BPARTNER_LOCATION_ADDRESS_POS );
            String strPhone = c.getString( MainDbAdapter.BPARTNER_LOCATION_PHONE_POS );
            String strPhone2 = c.getString( MainDbAdapter.BPARTNER_LOCATION_PHONE2_POS );
            String strPostal = c.getString( MainDbAdapter.BPARTNER_LOCATION_POSTAL_POS );
            String strFax = c.getString( MainDbAdapter.BPARTNER_LOCATION_FAX_POS );
            String strCity = c.getString( MainDbAdapter.BPARTNER_LOCATION_CITY_POS );
            String strRegion = c.getString( MainDbAdapter.BPARTNER_LOCATION_REGION_POS );
            String strCountry = c.getString( MainDbAdapter.BPARTNER_LOCATION_COUNTRY_POS );
            String strContact = c.getString( MainDbAdapter.BPARTNER_LOCATION_CONTACTPERSON_POS );
            String strEmail = c.getString( MainDbAdapter.BPARTNER_LOCATION_EMAIL_POS );

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
    }

    private void initControls(){
        cityAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line,
                mDbAdapter.getAutoCompleteText(MainDbAdapter.BPARTNER_LOCATION_TABLE_NAME, MainDbAdapter.BPARTNER_LOCATION_CITY_NAME,
                    -1, 0));
        acCity.setAdapter(cityAdapter);

        regionAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line,
                mDbAdapter.getAutoCompleteText(MainDbAdapter.BPARTNER_LOCATION_TABLE_NAME, MainDbAdapter.BPARTNER_LOCATION_REGION_NAME,
                    -1, 0));
        acRegion.setAdapter(regionAdapter);

        countryAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line,
                mDbAdapter.getAutoCompleteText(MainDbAdapter.BPARTNER_LOCATION_TABLE_NAME, MainDbAdapter.BPARTNER_LOCATION_COUNTRY_NAME,
                    -1, 0));
        acCountry.setAdapter(countryAdapter);

        contactAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line,
                mDbAdapter.getAutoCompleteText(MainDbAdapter.BPARTNER_LOCATION_TABLE_NAME, MainDbAdapter.BPARTNER_LOCATION_CONTACTPERSON_NAME,
                    -1, 0));
        acContact.setAdapter(contactAdapter);

        emailAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line,
                mDbAdapter.getAutoCompleteText(MainDbAdapter.BPARTNER_LOCATION_TABLE_NAME, MainDbAdapter.BPARTNER_LOCATION_EMAIL_NAME,
                    -1, 0));
        acEmail.setAdapter(emailAdapter);

        phoneAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line,
                mDbAdapter.getAutoCompleteText(MainDbAdapter.BPARTNER_LOCATION_TABLE_NAME, MainDbAdapter.BPARTNER_LOCATION_PHONE_NAME,
                    mBPartnerId, 0));
        acPhone1.setAdapter(phoneAdapter);

        phone2Adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line,
                mDbAdapter.getAutoCompleteText(MainDbAdapter.BPARTNER_LOCATION_TABLE_NAME, MainDbAdapter.BPARTNER_LOCATION_PHONE2_NAME,
                    mBPartnerId, 0));
        acPhone2.setAdapter(phone2Adapter);

        postalAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line,
                mDbAdapter.getAutoCompleteText(MainDbAdapter.BPARTNER_LOCATION_TABLE_NAME, MainDbAdapter.BPARTNER_LOCATION_POSTAL_NAME,
                    -1, 0));
        acPostal.setAdapter(postalAdapter);

        faxAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line,
                mDbAdapter.getAutoCompleteText(MainDbAdapter.BPARTNER_LOCATION_TABLE_NAME, MainDbAdapter.BPARTNER_LOCATION_FAX_NAME,
                    mBPartnerId, 0));
        acFax.setAdapter(faxAdapter);
    }
    
    @Override
    void saveData() {
        String strRetVal = checkMandatory(vgRoot);
        if( strRetVal != null ) {
            Toast toast = Toast.makeText( getApplicationContext(),
                    mResource.getString( R.string.GEN_FillMandatory ) + ": " + strRetVal, Toast.LENGTH_SHORT );
            toast.show();
            return;
        }

        strRetVal = checkNumeric(vgRoot);
        if( strRetVal != null ) {
            Toast toast = Toast.makeText( getApplicationContext(),
                    mResource.getString( R.string.GEN_NumberFormatException ) + ": " + strRetVal, Toast.LENGTH_SHORT );
            toast.show();
            return;
        }

        ContentValues cvData = new ContentValues();
        cvData.put( MainDbAdapter.GEN_COL_NAME_NAME,
                etName.getText().toString());
        cvData.put( MainDbAdapter.GEN_COL_ISACTIVE_NAME,
                (ckIsActive.isChecked() ? "Y" : "N") );
        cvData.put( MainDbAdapter.GEN_COL_USER_COMMENT_NAME,
                etUserComment.getText().toString() );
        cvData.put(MainDbAdapter.BPARTNER_LOCATION_BPARTNER_ID_NAME, mBPartnerId);
        cvData.put(MainDbAdapter.BPARTNER_LOCATION_ADDRESS_NAME, etAddress.getText().toString());
        cvData.put(MainDbAdapter.BPARTNER_LOCATION_PHONE_NAME, acPhone1.getText().toString());
        cvData.put(MainDbAdapter.BPARTNER_LOCATION_PHONE2_NAME, acPhone2.getText().toString());
        cvData.put(MainDbAdapter.BPARTNER_LOCATION_POSTAL_NAME, acPostal.getText().toString());
        cvData.put(MainDbAdapter.BPARTNER_LOCATION_FAX_NAME, acFax.getText().toString());
        cvData.put(MainDbAdapter.BPARTNER_LOCATION_CITY_NAME, acCity.getText().toString());
        cvData.put(MainDbAdapter.BPARTNER_LOCATION_REGION_NAME, acRegion.getText().toString());
        cvData.put(MainDbAdapter.BPARTNER_LOCATION_COUNTRY_NAME, acCountry.getText().toString());
        cvData.put(MainDbAdapter.BPARTNER_LOCATION_CONTACTPERSON_NAME, acContact.getText().toString());
        cvData.put(MainDbAdapter.BPARTNER_LOCATION_EMAIL_NAME, acEmail.getText().toString());

        if (mRowId == -1) {
            mDbAdapter.createRecord(MainDbAdapter.BPARTNER_LOCATION_TABLE_NAME, cvData);
            finish();
        } else {
            int strUpdateResult = mDbAdapter.updateRecord(MainDbAdapter.BPARTNER_LOCATION_TABLE_NAME, mRowId, cvData);
            if(strUpdateResult != -1){
                String strErrMsg = "";
                strErrMsg = mResource.getString(strUpdateResult);
                if(strUpdateResult == R.string.ERR_000)
                    strErrMsg = strErrMsg + "\n" + mDbAdapter.lastErrorMessage;
                madbErrorAlert.setMessage(strErrMsg);
                madError = madbErrorAlert.create();
                madError.show();
            }
            else
                finish();
        }
    }

    @Override
    void setLayout() {
        setContentView(R.layout.bpartner_location_edit_activity);
    }

}
