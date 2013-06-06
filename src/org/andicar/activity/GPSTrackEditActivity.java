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

import org.andicar.activity.miscellaneous.GPSTrackMap;
import org.andicar.persistence.MainDbAdapter;
import org.andicar.persistence.ReportDbAdapter;
import org.andicar.utils.AndiCarStatistics;
import org.andicar.utils.Utils;
import org.andicar2.activity.R;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

/**
 *
 * @author miki
 */
public class GPSTrackEditActivity extends EditActivityBase {
    private TextView tvCarLabel;
    private Spinner spnDriver;
    private EditText etName;
    private ListView lvTrackFileList;
    private TextView tvTrackStats;
    private ImageButton btnGPSTrackShowOnMap;
    private ImageButton btnGPSTrackSendAsEmail;
    private ArrayAdapter<String> userCommentAdapter;
    private ArrayAdapter<String> tagAdapter;
    private long mTagId = 0;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {

        super.onCreate(icicle);
        
        tvCarLabel = (TextView)findViewById(R.id.tvCarLabel);
        etName = (EditText)findViewById(R.id.etName);
        acUserComment = (AutoCompleteTextView) findViewById( R.id.acUserComment );
        spnDriver = (Spinner)findViewById(R.id.spnDriver);
        tvTrackStats = (TextView)findViewById(R.id.tvTrackStats);
        lvTrackFileList = (ListView)findViewById(R.id.lvTrackFileList);
        btnGPSTrackSendAsEmail = (ImageButton)findViewById(R.id.btnGPSTrackSendAsEmail);
        btnGPSTrackSendAsEmail.setOnClickListener(mBtnSendAsEmailListener);
        btnGPSTrackShowOnMap = (ImageButton)findViewById(R.id.btnGPSTrackShowOnMap);
        btnGPSTrackShowOnMap.setOnClickListener(mBtnShowOnMapListener);

        userCommentAdapter = new ArrayAdapter<String>(GPSTrackEditActivity.this,
                android.R.layout.simple_list_item_1,
                mDbAdapter.getAutoCompleteText(MainDbAdapter.GPSTRACK_TABLE_NAME, null,
                mPreferences.getLong("CurrentCar_ID", -1), 30));
        acUserComment.setAdapter(userCommentAdapter);

        mRowId = mBundleExtras.getLong( MainDbAdapter.GEN_COL_ROWID_NAME );
        Cursor c = mDbAdapter.fetchRecord(MainDbAdapter.GPSTRACK_TABLE_NAME,
                MainDbAdapter.gpsTrackTableColNames, mRowId);
        mCarId = c.getLong(MainDbAdapter.GPSTRACK_COL_CAR_ID_POS);
        mDriverId = c.getLong(MainDbAdapter.GPSTRACK_COL_DRIVER_ID_POS);

        etName.setText(c.getString(MainDbAdapter.GEN_COL_NAME_POS));
        acUserComment.setText(c.getString(MainDbAdapter.GEN_COL_USER_COMMENT_POS));

        tvCarLabel.setText(mResource.getString(R.string.GEN_CarLabel) + " " +
                        mDbAdapter.fetchRecord(MainDbAdapter.CAR_TABLE_NAME, MainDbAdapter.genColName, mCarId).getString(1));


        
        acTag = ((AutoCompleteTextView) findViewById( R.id.acTag ));
        tagAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                mDbAdapter.getAutoCompleteText(MainDbAdapter.TAG_TABLE_NAME, null,
                0, 0));
        acTag.setAdapter(tagAdapter);
        //fill tag
        if(c.getString(MainDbAdapter.GPSTRACK_COL_TAG_ID_POS) != null
                && c.getString(MainDbAdapter.GPSTRACK_COL_TAG_ID_POS).length() > 0){
            mTagId = c.getLong(MainDbAdapter.GPSTRACK_COL_TAG_ID_POS);
            String selection = MainDbAdapter.GEN_COL_ROWID_NAME + "= ? ";
            String[] selectionArgs = {Long.toString(mTagId)};
            Cursor c2 = mDbAdapter.query(MainDbAdapter.TAG_TABLE_NAME, MainDbAdapter.genColName,
                        selection, selectionArgs, null, null, null);
            if(c2.moveToFirst())
                acTag.setText(c2.getString(MainDbAdapter.GEN_COL_NAME_POS));
            c2.close();
        }

        initSpinner(spnDriver, MainDbAdapter.DRIVER_TABLE_NAME, MainDbAdapter.genColName,
                new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, MainDbAdapter.isActiveCondition, null, MainDbAdapter.GEN_COL_NAME_NAME,
                mDriverId, false);
        initDateTime(c.getLong(MainDbAdapter.GPSTRACK_COL_DATE_POS) * 1000);
        tvDateTimeValue.setText(mResource.getString(R.string.GEN_DateTimeLabel) + " " + tvDateTimeValue.getText());
        c.close();

        //statistics
        Bundle whereConditions = new Bundle();
        whereConditions.clear();
        whereConditions.putString(
            ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.GPSTRACK_TABLE_NAME, MainDbAdapter.GEN_COL_ROWID_NAME) + "=",
                    String.valueOf(mRowId));
        ReportDbAdapter reportDb = new ReportDbAdapter(this, "gpsTrackListViewSelect", whereConditions);
        c = reportDb.fetchReport(1);
        if (c.moveToFirst()) {
            tvTrackStats.setText(
                    c.getString(c.getColumnIndex(ReportDbAdapter.SECOND_LINE_LIST_NAME))
                    .replace("[#1]", mResource.getString(R.string.GPSTrackReport_GPSTrackVar_1))
                    .replace("[#2]", "\n" + mResource.getString(R.string.GPSTrackReport_GPSTrackVar_2))
                    .replace("[#3]", "\n" + mResource.getString(R.string.GPSTrackReport_GPSTrackVar_3))
                    .replace("[#4]", "\n" + mResource.getString(R.string.GPSTrackReport_GPSTrackVar_4))
                    .replace("[#5]", "\n" + mResource.getString(R.string.GPSTrackReport_GPSTrackVar_5) +
                            Utils.getTimeString(c.getLong(c.getColumnIndex(ReportDbAdapter.FOURTH_LINE_LIST_NAME)), false))
                    .replace("[#6]", "\n" + mResource.getString(R.string.GPSTrackReport_GPSTrackVar_6) +
                            Utils.getTimeString(c.getLong(c.getColumnIndex(ReportDbAdapter.FIFTH_LINE_LIST_NAME)), false))
                    .replace("[#12]", "\n" + mResource.getString(R.string.GPSTrackReport_GPSTrackVar_12) +
                            Utils.getTimeString(c.getLong(c.getColumnIndex(ReportDbAdapter.GPSTRACK_COL_TOTALPAUSETIME_NAME)), false))
                    .replace("[#7]", "\n" + mResource.getString(R.string.GPSTrackReport_GPSTrackVar_7))
                    .replace("[#8]", "\n" + mResource.getString(R.string.GPSTrackReport_GPSTrackVar_8))
                    .replace("[#9]", "\n" + mResource.getString(R.string.GPSTrackReport_GPSTrackVar_9))
                    .replace("[#10]", "\n" + mResource.getString(R.string.GPSTrackReport_GPSTrackVar_10))
                    .replace("[#11]", "\n" + mResource.getString(R.string.GPSTrackReport_GPSTrackVar_11))
                    );
        }
        c.close();
        reportDb.close();

        String selection = MainDbAdapter.GPSTRACKDETAIL_COL_GPSTRACK_ID_NAME + "=?";
        String[] selectionArgs = {Long.toString(mRowId)};
        int layout = R.layout.oneline_list_layout_small_s01;
   		layout = R.layout.oneline_list_layout_small_s01;

        SimpleCursorAdapter cursorAdapter =
                new SimpleCursorAdapter(this, layout,
                                    mDbAdapter.query(MainDbAdapter.GPSTRACKDETAIL_TABLE_NAME,
                                            MainDbAdapter.gpsTrackDetailTableColNames, selection, selectionArgs, null, null, MainDbAdapter.GPSTRACKDETAIL_COL_FILE_NAME),
                                    new String[]{MainDbAdapter.GPSTRACKDETAIL_COL_FILE_NAME}, new int[]{R.id.tvOneLineListTextSmall});

        lvTrackFileList.setAdapter(cursorAdapter);
        
        if(acAdress != null)
        	acAdress.setOnKeyListener(this);
        if(acBPartner != null)
        	acBPartner.setOnKeyListener(this);
        if(acTag != null)
        	acTag.setOnKeyListener(this);
        if(acUserComment != null)
        	acUserComment.setOnKeyListener(this);
        

        if(isSendStatistics)
            AndiCarStatistics.sendFlurryEvent(this, "GPSTrackEdit", null);
    }

    private View.OnClickListener mBtnSendAsEmailListener =
            new View.OnClickListener()
                {
                    public void onClick( View v )
                    {
                        Utils u = new Utils();
                        u.sendGPSTrackAsEmail(GPSTrackEditActivity.this, mResource, mRowId);
                    }
                };

    private View.OnClickListener mBtnShowOnMapListener =
            new View.OnClickListener() {
                public void onClick(View v) {
                    Intent gpstrackShowMapIntent = new Intent(GPSTrackEditActivity.this, GPSTrackMap.class);
                    gpstrackShowMapIntent.putExtra("gpsTrackId", Long.toString(mRowId));
                    startActivity(gpstrackShowMapIntent);
                }
            };

    @Override
    protected boolean saveData() {

        ContentValues data = new ContentValues();
        data.put( MainDbAdapter.GEN_COL_NAME_NAME,
                etName.getText().toString());
        data.put( MainDbAdapter.GEN_COL_ISACTIVE_NAME, "Y");
        data.put( MainDbAdapter.GEN_COL_USER_COMMENT_NAME,
                acUserComment.getText().toString() );
        data.put( MainDbAdapter.GPSTRACK_COL_DRIVER_ID_NAME,
                spnDriver.getSelectedItemId() );
        if(acTag.getText().toString() != null && acTag.getText().toString().length() > 0){
            String selection = "UPPER (" + MainDbAdapter.GEN_COL_NAME_NAME + ") = ?";
            String[] selectionArgs = {acTag.getText().toString().toUpperCase()};
            Cursor c = mDbAdapter.query(MainDbAdapter.TAG_TABLE_NAME, MainDbAdapter.genColName, selection, selectionArgs,
                    null, null, null);
            String tagIdStr = null;
            if(c.moveToFirst())
                tagIdStr = c.getString(MainDbAdapter.GEN_COL_ROWID_POS);
            c.close();
            if(tagIdStr != null && tagIdStr.length() > 0){
                mTagId = Long.parseLong(tagIdStr);
                data.put(MainDbAdapter.GPSTRACK_COL_TAG_ID_NAME, mTagId);
            }
            else{
                ContentValues tmpData = new ContentValues();
                tmpData.put(MainDbAdapter.GEN_COL_NAME_NAME, acTag.getText().toString());
                mTagId = mDbAdapter.createRecord(MainDbAdapter.TAG_TABLE_NAME, tmpData);
                if(mTagId >= 0)
                    data.put(MainDbAdapter.GPSTRACK_COL_TAG_ID_NAME, mTagId);
            }
        }
        else{
            data.put(MainDbAdapter.GPSTRACK_COL_TAG_ID_NAME, (String)null);
        }


        int updResult = mDbAdapter.updateRecord(MainDbAdapter.GPSTRACK_TABLE_NAME, mRowId, data);
        if(updResult != -1){
            String errMsg = "";
            errMsg = mResource.getString(updResult);
            if(updResult == R.string.ERR_000)
                errMsg = errMsg + "\n" + mDbAdapter.lastErrorMessage;
            madbErrorAlert.setMessage(errMsg);
            madError = madbErrorAlert.create();
            madError.show();
            return false;
        }
        else{
        	if(mPreferences.getBoolean("RememberLastTag", false) && mTagId > 0){
        		mPrefEditor.putLong("LastTagId", mTagId);
        		mPrefEditor.commit();
        	}
            finish();
            return true;
        }
    }

    @Override
    protected void setLayout() {
    	setContentView(R.layout.gpstrack_edit_activity_s01);
    }

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
