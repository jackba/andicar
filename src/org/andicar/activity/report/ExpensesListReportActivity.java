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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import org.andicar.activity.ExpenseEditActivity;
import org.andicar.activity.R;
import org.andicar.activity.RefuelEditActivity;
import org.andicar.persistence.MainDbAdapter;
import org.andicar.persistence.ReportDbAdapter;
import org.andicar.utils.StaticValues;
import org.andicar.utils.Utils;

/**
 *
 * @author miki
 */
public class ExpensesListReportActivity extends ReportListActivityBase{
    private View searchView;
    private EditText genUserCommentEntry;
    private Spinner searchExpCategorySpinner;
    private Spinner searchExpTypeSpinner;
    private EditText searchDateFromEntry;
    private EditText searchDateToEntry;
    private Spinner searchDriverSpinner;
    private Spinner searchCarSpinner;

    @Override
    public void onCreate( Bundle icicle )
    {
        Long mCarId = getSharedPreferences( StaticValues.GLOBAL_PREFERENCE_NAME, 0 ).getLong("CurrentCar_ID", 0);
        whereConditions = new Bundle();
        whereConditions.putString(
                ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.EXPENSES_TABLE_NAME, MainDbAdapter.EXPENSES_COL_CAR_ID_NAME) + "=",
                mCarId.toString() );

        super.onCreate( icicle, null, ExpenseEditActivity.class,
                MainDbAdapter.EXPENSES_TABLE_NAME, ReportDbAdapter.genericReportListViewSelectCols, null,
                null,
                R.layout.threeline_listreport_activity,
                new String[]{ReportDbAdapter.FIRST_LINE_LIST_NAME, ReportDbAdapter.SECOND_LINE_LIST_NAME, ReportDbAdapter.THIRD_LINE_LIST_NAME},
                new int[]{R.id.threeLineListReportText1, R.id.threeLineListReportText2, R.id.threeLineListReportText3},
                "reportExpensesListViewSelect",  whereConditions);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == StaticValues.OPTION_MENU_SEARCH_ID){
            showDialog(StaticValues.localSearchDialog);
        }
        else{
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if(id != StaticValues.localSearchDialog)
            return super.onCreateDialog(id);

        LayoutInflater factory = LayoutInflater.from(this);
        searchView = factory.inflate(R.layout.expenses_search_dialog, null);
        AlertDialog.Builder searchDialog = new AlertDialog.Builder(ExpensesListReportActivity.this);
        searchDialog.setTitle(R.string.SEARCH_DIALOG_TITLE);
        searchDialog.setView(searchView);
        searchDialog.setPositiveButton(R.string.GEN_OK, searchDialogButtonlistener);
        searchDialog.setNegativeButton(R.string.GEN_CANCEL, searchDialogButtonlistener);
        searchExpCategorySpinner = (Spinner) searchView.findViewById(R.id.searchExpenseCategorySpinner);
        initSpinner(searchExpCategorySpinner, MainDbAdapter.EXPENSECATEGORY_TABLE_NAME);
        searchExpTypeSpinner = (Spinner) searchView.findViewById(R.id.searchExpenseTypeSpinner);
        initSpinner(searchExpTypeSpinner, MainDbAdapter.EXPENSETYPE_TABLE_NAME);
        genUserCommentEntry = (EditText) searchView.findViewById(R.id.genUserCommentEntry);
        genUserCommentEntry.setText("%");
        searchDateFromEntry = (EditText) searchView.findViewById(R.id.searchDateFromEntry);
        searchDateToEntry = (EditText) searchView.findViewById(R.id.searchDateToEntry);
        searchCarSpinner = (Spinner) searchView.findViewById(R.id.searchCarSpinner);
        initSpinner(searchCarSpinner, MainDbAdapter.CAR_TABLE_NAME);
        searchDriverSpinner = (Spinner) searchView.findViewById(R.id.searchDriverSpinner);
        initSpinner(searchDriverSpinner, MainDbAdapter.DRIVER_TABLE_NAME);
        return searchDialog.create();
    }
    private DialogInterface.OnClickListener searchDialogButtonlistener = new DialogInterface.OnClickListener() {

        public void onClick(DialogInterface dialog, int whichButton) {
            if (whichButton == DialogInterface.BUTTON_POSITIVE) {
                try {
                    whereConditions.clear();
                    if (searchExpCategorySpinner.getSelectedItemId() != -1) {
                        whereConditions.putString(
                                ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.EXPENSES_TABLE_NAME,
                                MainDbAdapter.EXPENSES_COL_EXPENSECATEGORY_ID_NAME) + "=",
                                String.valueOf(searchExpCategorySpinner.getSelectedItemId()));
                    }
                    if (searchExpTypeSpinner.getSelectedItemId() != -1) {
                        whereConditions.putString(
                                ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.EXPENSES_TABLE_NAME,
                                MainDbAdapter.EXPENSES_COL_EXPENSETYPE_ID_NAME) + "=",
                                String.valueOf(searchExpTypeSpinner.getSelectedItemId()));
                    }
                    if (genUserCommentEntry.getText().toString().length() > 0) {
                        whereConditions.putString(
                                ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.EXPENSES_TABLE_NAME,
                                MainDbAdapter.GEN_COL_USER_COMMENT_NAME) + " LIKE ",
                                genUserCommentEntry.getText().toString());
                    }
                    if (searchDateFromEntry.getText().toString().length() > 0) {
                        whereConditions.putString(
                                ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.EXPENSES_TABLE_NAME,
                                MainDbAdapter.EXPENSES_COL_DATE_NAME) + " >= ",
                                Long.toString(Utils.decodeDateStr(searchDateFromEntry.getText().toString(),
                                StaticValues.dateDecodeTypeTo0Hour) / 1000));
                    }
                    if (searchDateToEntry.getText().toString().length() > 0) {
                        whereConditions.putString(
                                ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.EXPENSES_TABLE_NAME,
                                MainDbAdapter.EXPENSES_COL_DATE_NAME) + " <= ",
                                Long.toString(Utils.decodeDateStr(searchDateToEntry.getText().toString(),
                                StaticValues.dateDecodeTypeTo24Hour) / 1000));
                    }
                    if (searchCarSpinner.getSelectedItemId() != -1) {
                        whereConditions.putString(
                                ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.EXPENSES_TABLE_NAME,
                                MainDbAdapter.EXPENSES_COL_CAR_ID_NAME) + "=",
                                String.valueOf(searchCarSpinner.getSelectedItemId()));
                    }
                    if (searchDriverSpinner.getSelectedItemId() != -1) {
                        whereConditions.putString(
                                ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.EXPENSES_TABLE_NAME,
                                MainDbAdapter.EXPENSES_COL_DRIVER_ID_NAME) + "=",
                                String.valueOf(searchDriverSpinner.getSelectedItemId()));
                    }
                    mListDbHelper.setReportSql("reportExpensesListViewSelect", whereConditions);
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

}
