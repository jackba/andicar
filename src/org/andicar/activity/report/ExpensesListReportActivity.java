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

import org.andicar.activity.ExpenseEditActivity;
import org.andicar2.activity.R;
import org.andicar.activity.dialog.AndiCarDialogBuilder;
import org.andicar.persistence.ExpenseListDataBinder;
import org.andicar.persistence.MainDbAdapter;
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
public class ExpensesListReportActivity extends ReportListActivityBase{
    private View searchView;
    private EditText etUserComment;
    private TextView tvDateFromSearch;
    private TextView tvDateToSearch;
    private AutoCompleteTextView acTag;
    private Spinner spnDriver;
    private Spinner spnCar;
    private Spinner spnExpCategory;
    private Spinner spnExpType;
//    private Spinner spnIsActive;
    private ArrayAdapter<String> tagAdapter;
    private Long mCarId;

    @Override
    public void onCreate( Bundle icicle )
    {
        reportSelectName = "expensesListViewSelect";
        mCarId = getSharedPreferences( StaticValues.GLOBAL_PREFERENCE_NAME, 0 ).getLong("CurrentCar_ID", 0);
        if(icicle == null){
            whereConditions = new Bundle();
            whereConditions.putString(
                    ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.TABLE_NAME_EXPENSE,
                    			MainDbAdapter.COL_NAME_EXPENSE__CAR_ID) + "=",
                    mCarId.toString() );
    		whereConditions.putString("COALESCE(" + MainDbAdapter.COL_NAME_EXPENSE__FROMTABLE + ", '') = ", ""); 
        }
        else
            whereConditions = (Bundle)getLastNonConfigurationInstance();

        initStyle();

        super.onCreate( icicle, null, ExpenseEditActivity.class, null,
                MainDbAdapter.TABLE_NAME_EXPENSE, ReportDbAdapter.genericReportListViewSelectCols, null,
                null, threeLineListReportActivity,
                new String[]{ReportDbAdapter.FIRST_LINE_LIST_NAME, ReportDbAdapter.SECOND_LINE_LIST_NAME, ReportDbAdapter.THIRD_LINE_LIST_NAME},
                new int[]{R.id.tvThreeLineListReportText1, R.id.tvThreeLineListReportText2, R.id.tvThreeLineListReportText3},
                reportSelectName,  whereConditions, new ExpenseListDataBinder());

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
        searchView = liLayoutFactory.inflate(R.layout.search_dialog_expense, null);
        AndiCarDialogBuilder searchDialog = new AndiCarDialogBuilder(ExpensesListReportActivity.this, 
        		AndiCarDialogBuilder.DIALOGTYPE_SEARCH, mRes.getString(R.string.DIALOGSearch_DialogTitle));
        searchDialog.setView(searchView);
        searchDialog.setPositiveButton(R.string.GEN_OK, searchDialogButtonlistener);
        searchDialog.setNegativeButton(R.string.GEN_CANCEL, searchDialogButtonlistener);
        spnExpCategory = (Spinner) searchView.findViewById(R.id.spnExpCategorySearch);
        initSpinner(spnExpCategory, MainDbAdapter.TABLE_NAME_EXPENSECATEGORY, 
        		MainDbAdapter.COL_NAME_EXPENSECATEGORY__ISFUEL + " = 'N'", null, -1);
        spnExpType = (Spinner) searchView.findViewById(R.id.spnExpTypeSearch);
        initSpinner(spnExpType, MainDbAdapter.TABLE_NAME_EXPENSETYPE, null, null, -1);
        etUserComment = (EditText) searchView.findViewById(R.id.etUserCommentSearch);
        etUserComment.setText("%");
        tvDateFromSearch = (TextView) searchView.findViewById(R.id.tvDateFromSearch);
        tvDateToSearch = (TextView) searchView.findViewById(R.id.tvDateToSearch);
        spnCar = (Spinner) searchView.findViewById(R.id.spnCarSearch);
        initSpinner(spnCar, MainDbAdapter.TABLE_NAME_CAR, null, null, mCarId);
        spnDriver = (Spinner) searchView.findViewById(R.id.spnDriverSearch);
        initSpinner(spnDriver, MainDbAdapter.TABLE_NAME_DRIVER, null, null, -1);
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
                    //exclude expenses created from refuels
            		whereConditions.putString("COALESCE(" + MainDbAdapter.COL_NAME_EXPENSE__FROMTABLE + ", '') = ", "");
            		
                    if (spnExpCategory.getSelectedItemId() != -1) {
                        whereConditions.putString(
                                ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.TABLE_NAME_EXPENSE,
                                		MainDbAdapter.COL_NAME_EXPENSE__EXPENSECATEGORY_ID) + "=",
                                String.valueOf(spnExpCategory.getSelectedItemId()));
                    }
                    if (spnExpType.getSelectedItemId() != -1) {
                        whereConditions.putString(
                                ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.TABLE_NAME_EXPENSE,
                                		MainDbAdapter.COL_NAME_EXPENSE__EXPENSETYPE_ID) + "=",
                                String.valueOf(spnExpType.getSelectedItemId()));
                    }
                    if (etUserComment.getText().toString().length() > 0) {
                        whereConditions.putString(
                                ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.TABLE_NAME_EXPENSE,
                                		MainDbAdapter.COL_NAME_GEN_USER_COMMENT) + " LIKE ",
                                etUserComment.getText().toString());
                    }
                    if (tvDateFromSearch.getText().toString().length() > 0) {
                        whereConditions.putString(
                                ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.TABLE_NAME_EXPENSE,
                                		MainDbAdapter.COL_NAME_EXPENSE__DATE) + " >= ",
                                Long.toString(Utils.decodeDateStr(tvDateFromSearch.getText().toString(),
                                		StaticValues.DATE_DECODE_TO_ZERO) / 1000));
                    }
                    if (tvDateToSearch.getText().toString().length() > 0) {
                        whereConditions.putString(
                                ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.TABLE_NAME_EXPENSE,
                                		MainDbAdapter.COL_NAME_EXPENSE__DATE) + " <= ",
                                Long.toString(Utils.decodeDateStr(tvDateToSearch.getText().toString(),
                                		StaticValues.DATE_DECODE_TO_24) / 1000));
                    }
                    if (spnCar.getSelectedItemId() != -1) {
                        whereConditions.putString(
                                ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.TABLE_NAME_EXPENSE,
                                		MainDbAdapter.COL_NAME_EXPENSE__CAR_ID) + "=",
                                String.valueOf(spnCar.getSelectedItemId()));
                    }
                    if (spnDriver.getSelectedItemId() != -1) {
                        whereConditions.putString(
                                ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.TABLE_NAME_EXPENSE,
                                		MainDbAdapter.COL_NAME_EXPENSE__DRIVER_ID) + "=",
                                String.valueOf(spnDriver.getSelectedItemId()));
                    }
                    if (acTag.getText().toString() != null) {
                    	if(acTag.getText().toString().length() == 0)
                            whereConditions.putString(
                                    ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.TABLE_NAME_EXPENSE,
                                    							MainDbAdapter.COL_NAME_EXPENSE__TAG_ID) + " is ",
                                    "null");
                    	else
                            whereConditions.putString(
                            		"COALESCE( " +
	                                    ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.TABLE_NAME_TAG,
	                                    							MainDbAdapter.COL_NAME_GEN_NAME) + ", '') LIKE ",
        							acTag.getText().toString());
                    }
//                    if(spnIsActive.getSelectedItemId() > 0){
//	                    whereConditions.putString(
//	                            ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.EXPENSE_TABLE_NAME,
//	                            			MainDbAdapter.GEN_COL_ISACTIVE_NAME) + "=",
//	                        			(spnIsActive.getSelectedItemId() == 1 ? "Y" : "N"));
//                    }
                    mListDbHelper.setReportSql(reportSelectName, whereConditions);
                    fillData();
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
