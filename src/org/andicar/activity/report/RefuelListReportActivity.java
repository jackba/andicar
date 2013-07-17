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

package org.andicar.activity.report;

import org.andicar2.activity.R;
import org.andicar.activity.RefuelEditActivity;
import org.andicar.activity.dialog.AndiCarDialogBuilder;
import org.andicar.persistence.MainDbAdapter;
import org.andicar.persistence.RefuelListDataBinder;
import org.andicar.persistence.ReportDbAdapter;
import org.andicar.utils.StaticValues;
import org.andicar.utils.Utils;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

/**
 *
 * @author miki
 */
public class RefuelListReportActivity extends ReportListActivityBase{
    private View searchView;
    private EditText etUserCommentSearch;
    private TextView tvDateFromSearch;
    private TextView tvDateToSearch;
    private AutoCompleteTextView acTag;
    private Spinner spnDriverSearch;
    private Spinner spnCarSearch;
    private Spinner spnExpCategory;
    private Spinner spnExpTypeSearch;
//    private Spinner spnIsActive;
    private ArrayAdapter<String> tagAdapter;
    private Long mCarId;

    @Override
    public void onCreate( Bundle icicle )
    {
        reportSelectName = "refuelListViewSelect";
        mCarId = getSharedPreferences( StaticValues.GLOBAL_PREFERENCE_NAME, 0 ).getLong("CurrentCar_ID", 0);
        if(icicle == null){
            whereConditions = new Bundle();
            whereConditions.putString(
                    ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.TABLE_NAME_REFUEL, MainDbAdapter.COL_NAME_REFUEL__CAR_ID) + "=",
                    mCarId.toString() );
//    		whereConditions.putString(
//    				ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.REFUEL_TABLE_NAME, MainDbAdapter.GEN_COL_ISACTIVE_NAME) + " = ", "Y"); 
        }
        else
            whereConditions = (Bundle)getLastNonConfigurationInstance();

        initStyle();

        RefuelListDataBinder rfDb = new RefuelListDataBinder();
        rfDb.initCtx(getApplicationContext());
        super.onCreate( icicle, null, RefuelEditActivity.class, null,
                MainDbAdapter.TABLE_NAME_REFUEL, ReportDbAdapter.genericReportListViewSelectCols, null,
                null,
                threeLineListReportActivity,
                new String[]{ReportDbAdapter.FIRST_LINE_LIST_NAME, ReportDbAdapter.SECOND_LINE_LIST_NAME, ReportDbAdapter.THIRD_LINE_LIST_NAME},
                new int[]{R.id.tvThreeLineListReportText1, R.id.tvThreeLineListReportText2, R.id.tvThreeLineListReportText3},
                reportSelectName,  whereConditions, rfDb /*new RefuelListDataBinder()*/);

    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        //save existing data whwn the activity restart (for example on screen orientation change)
        return whereConditions;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == StaticValues.OPTION_MENU_SEARCH_ID){
            showDialog(StaticValues.DIALOG_LOCAL_SEARCH);
        }
        else{
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if(id != StaticValues.DIALOG_LOCAL_SEARCH)
            return super.onCreateDialog(id);

        LayoutInflater liLayoutFactory = LayoutInflater.from(this);
        searchView = liLayoutFactory.inflate(R.layout.search_dialog_refuel, null);
        AndiCarDialogBuilder searchDialog = new AndiCarDialogBuilder(RefuelListReportActivity.this, 
        		AndiCarDialogBuilder.DIALOGTYPE_SEARCH, mRes.getString(R.string.DIALOGSearch_DialogTitle));
        searchDialog.setView(searchView);
        searchDialog.setPositiveButton(R.string.GEN_OK, searchDialogButtonlistener);
        searchDialog.setNegativeButton(R.string.GEN_CANCEL, searchDialogButtonlistener);
        spnExpCategory = (Spinner) searchView.findViewById(R.id.spnExpCategorySearch);
        initSpinner(spnExpCategory, MainDbAdapter.TABLE_NAME_EXPENSECATEGORY, 
        		MainDbAdapter.COL_NAME_EXPENSECATEGORY__ISFUEL + " = 'Y'", null, -1);
        spnExpTypeSearch = (Spinner) searchView.findViewById(R.id.spnExpTypeSearch);
        initSpinner(spnExpTypeSearch, MainDbAdapter.TABLE_NAME_EXPENSETYPE, null, null, -1);
        etUserCommentSearch = (EditText) searchView.findViewById(R.id.etUserCommentSearch);
        etUserCommentSearch.setText("%");
        tvDateFromSearch = (TextView) searchView.findViewById(R.id.tvDateFromSearch);
        tvDateToSearch = (TextView) searchView.findViewById(R.id.tvDateToSearch);
        spnCarSearch = (Spinner) searchView.findViewById(R.id.spnCarSearch);
        initSpinner(spnCarSearch, MainDbAdapter.TABLE_NAME_CAR, null, null, mCarId);
        spnDriverSearch = (Spinner) searchView.findViewById(R.id.spnDriverSearch);
        initSpinner(spnDriverSearch, MainDbAdapter.TABLE_NAME_DRIVER, null, null, -1);
        acTag = ((AutoCompleteTextView) searchView.findViewById( R.id.acTag ));
        tagAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                mDbAdapter.getAutoCompleteText(MainDbAdapter.TABLE_NAME_TAG, null,
                0, 0));
        acTag.setAdapter(tagAdapter);
        acTag.setText("%");
//        spnIsActive = (Spinner) searchView.findViewById(R.id.spnIsActive);
//        spnIsActive.setSelection(1); //yes
        
        ImageButton btnPickDateFrom = (ImageButton) searchView.findViewById(R.id.btnPickDateFrom);
        if(btnPickDateFrom != null)
            btnPickDateFrom.setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {
                    showDialog(StaticValues.DIALOG_DATE_FROM_PICKER);
                }
            });
        
        ImageButton btnPickDateTo = (ImageButton) searchView.findViewById(R.id.btnPickDateTo);
        if(btnPickDateTo != null)
            btnPickDateTo.setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {
                    showDialog(StaticValues.DIALOG_DATE_TO_PICKER);
                }
            });
        return searchDialog.create();
    }
    private DialogInterface.OnClickListener searchDialogButtonlistener = new DialogInterface.OnClickListener() {

        public void onClick(DialogInterface dialog, int whichButton) {
            if (whichButton == DialogInterface.BUTTON_POSITIVE) {
            	if(whereConditions == null)
            		whereConditions = new Bundle();
                try {
                    whereConditions.clear();
                    if (spnExpCategory.getSelectedItemId() != -1) {
                        whereConditions.putString(
                                ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.TABLE_NAME_REFUEL,
                                		MainDbAdapter.COL_NAME_REFUEL__EXPENSECATEGORY_ID) + "=",
                                String.valueOf(spnExpCategory.getSelectedItemId()));
                    }
                    if (spnExpTypeSearch.getSelectedItemId() != -1) {
                        whereConditions.putString(
                                ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.TABLE_NAME_REFUEL,
                                		MainDbAdapter.COL_NAME_REFUEL__EXPENSETYPE_ID) + "=",
                                String.valueOf(spnExpTypeSearch.getSelectedItemId()));
                    }
                    if (etUserCommentSearch.getText().toString().length() > 0) {
                        whereConditions.putString(
                                ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.TABLE_NAME_REFUEL,
                                		MainDbAdapter.COL_NAME_GEN_USER_COMMENT) + " LIKE ",
                                etUserCommentSearch.getText().toString());
                    }
                    if (tvDateFromSearch.getText().toString().length() > 0) {
                        whereConditions.putString(
                                ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.TABLE_NAME_REFUEL,
                                		MainDbAdapter.COL_NAME_REFUEL__DATE) + " >= ",
                                Long.toString(Utils.decodeDateStr(tvDateFromSearch.getText().toString(),
                                StaticValues.DATE_DECODE_TO_ZERO) / 1000));
                    }
                    if (tvDateToSearch.getText().toString().length() > 0) {
                        whereConditions.putString(
                                ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.TABLE_NAME_REFUEL,
                                		MainDbAdapter.COL_NAME_REFUEL__DATE) + " <= ",
                                Long.toString(Utils.decodeDateStr(tvDateToSearch.getText().toString(),
                                StaticValues.DATE_DECODE_TO_24) / 1000));
                    }
                    if (spnCarSearch.getSelectedItemId() != -1) {
                        whereConditions.putString(
                                ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.TABLE_NAME_REFUEL,
                                		MainDbAdapter.COL_NAME_REFUEL__CAR_ID) + "=",
                                String.valueOf(spnCarSearch.getSelectedItemId()));
                    }
                    if (spnDriverSearch.getSelectedItemId() != -1) {
                        whereConditions.putString(
                                ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.TABLE_NAME_REFUEL,
                                		MainDbAdapter.COL_NAME_REFUEL__DRIVER_ID) + "=",
                                String.valueOf(spnDriverSearch.getSelectedItemId()));
                    }
                    if (acTag.getText().toString() != null) {
                    	if(acTag.getText().toString().length() == 0)
                            whereConditions.putString(
                                    ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.TABLE_NAME_REFUEL,
                                    							MainDbAdapter.COL_NAME_REFUEL__TAG_ID) + " is ",
                                    "null");
                    	else
                            whereConditions.putString(
                            		"COALESCE( " +
	                                    ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.TABLE_NAME_TAG,
	                                    							MainDbAdapter.COL_NAME_GEN_NAME) + ", '') LIKE ",
        							acTag.getText().toString());
                    }
                    mListDbHelper.setReportSql(reportSelectName, whereConditions);
                    fillData();
                } catch (IndexOutOfBoundsException e) {
                    errorAlertBuilder.setMessage(mRes.getString(R.string.ERR_008));
                    errorAlert = errorAlertBuilder.create();
                    errorAlert.show();
                } catch (NumberFormatException e) {
                    errorAlertBuilder.setMessage(mRes.getString(R.string.ERR_008));
                    errorAlert = errorAlertBuilder.create();
                    errorAlert.show();
                }
            }
        };
    };

	/* (non-Javadoc)
	 * @see org.andicar.activity.report.ReportListActivityBase#updateDate(int)
	 */
	@Override
	protected void updateDate(int what) {
		if(what == 1){ //date from
			tvDateFromSearch.setText(mYearFrom + "-" + Utils.pad((mMonthFrom + 1), 2) + "-" + Utils.pad(mDayFrom, 2));
		}
		else{ //date to
			tvDateToSearch.setText(mYearTo + "-" + Utils.pad((mMonthTo + 1), 2) + "-" + Utils.pad(mDayTo, 2));
		}
	}

}
