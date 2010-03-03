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
import android.widget.Toast;
import org.andicar.activity.MileageEditActivity;
import org.andicar.activity.R;
import org.andicar.persistence.MainDbAdapter;
import org.andicar.persistence.ReportDbAdapter;
import org.andicar.utils.StaticValues;
import org.andicar.utils.Utils;

/**
 *
 * @author miki
 */
public class DailyMileageReportActivity extends ReportListActivityBase {

    private View searchView;
    private EditText genUserCommentEntry;
    private Spinner searchExpTypeSpinner;
    private EditText searchDateFromEntry;
    private EditText searchDateToEntry;
    private Spinner searchDriverSpinner;
    private Spinner searchCarSpinner;

    @Override
    public void onCreate(Bundle icicle) {
        Long mCarId = getSharedPreferences(StaticValues.GLOBAL_PREFERENCE_NAME, 0).getLong("CurrentCar_ID", 0);
        whereConditions = new Bundle();
        whereConditions.putString(
                ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.MILEAGE_TABLE_NAME, MainDbAdapter.MILEAGE_COL_CAR_ID_NAME) + "=",
                mCarId.toString());

        super.onCreate(icicle, null, MileageEditActivity.class,
                MainDbAdapter.MILEAGE_TABLE_NAME, ReportDbAdapter.genericReportListViewSelectCols, null,
                null,
                R.layout.threeline_listreport_activity,
                new String[]{ReportDbAdapter.FIRST_LINE_LIST_NAME, ReportDbAdapter.SECOND_LINE_LIST_NAME, ReportDbAdapter.THIRD_LINE_LIST_NAME},
                new int[]{R.id.threeLineListReportText1, R.id.threeLineListReportText2, R.id.threeLineListReportText3},
                "reportMileageListViewSelect", whereConditions);

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
        searchView = factory.inflate(R.layout.daily_mileage_search_dialog, null);
        AlertDialog.Builder searchDialog = new AlertDialog.Builder(DailyMileageReportActivity.this);
        searchDialog.setTitle(R.string.SEARCH_DIALOG_TITLE);
        searchDialog.setView(searchView);
        searchDialog.setPositiveButton(R.string.GEN_OK, searchDialogButtonlistener);
        searchDialog.setNegativeButton(R.string.GEN_CANCEL, searchDialogButtonlistener);
        searchExpTypeSpinner = (Spinner) searchView.findViewById(R.id.searchExpenseTypeSpinner);
        initSpinner(searchExpTypeSpinner, "ExpenseType");
        genUserCommentEntry = (EditText) searchView.findViewById(R.id.genUserCommentEntry);
        genUserCommentEntry.setText("%");
        searchDateFromEntry = (EditText) searchView.findViewById(R.id.searchDateFromEntry);
        searchDateToEntry = (EditText) searchView.findViewById(R.id.searchDateToEntry);
        searchCarSpinner = (Spinner) searchView.findViewById(R.id.searchCarSpinner);
        initSpinner(searchCarSpinner, "Car");
        searchDriverSpinner = (Spinner) searchView.findViewById(R.id.searchDriverSpinner);
        initSpinner(searchDriverSpinner, "Driver");
        return searchDialog.create();
    }
    private DialogInterface.OnClickListener searchDialogButtonlistener = new DialogInterface.OnClickListener() {

        public void onClick(DialogInterface dialog, int whichButton) {
            if (whichButton == DialogInterface.BUTTON_POSITIVE) {
                try {
                    whereConditions.clear();
                    if (searchExpTypeSpinner.getSelectedItemId() != -1) {
                        whereConditions.putString(
                                ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.MILEAGE_TABLE_NAME,
                                MainDbAdapter.MILEAGE_COL_EXPENSETYPE_ID_NAME) + "=",
                                String.valueOf(searchExpTypeSpinner.getSelectedItemId()));
                    }
                    if (genUserCommentEntry.getText().toString().length() > 0) {
                        whereConditions.putString(
                                ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.MILEAGE_TABLE_NAME,
                                MainDbAdapter.GEN_COL_USER_COMMENT_NAME) + " LIKE ",
                                genUserCommentEntry.getText().toString());
                    }
                    if (searchDateFromEntry.getText().toString().length() > 0) {
                        whereConditions.putString(
                                ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.MILEAGE_TABLE_NAME,
                                MainDbAdapter.MILEAGE_COL_DATE_NAME) + " >= ",
                                Long.toString(Utils.decodeDateStr(searchDateFromEntry.getText().toString(),
                                StaticValues.dateDecodeTypeTo0Hour) / 1000));
                    }
                    if (searchDateToEntry.getText().toString().length() > 0) {
                        whereConditions.putString(
                                ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.MILEAGE_TABLE_NAME,
                                MainDbAdapter.MILEAGE_COL_DATE_NAME) + " <= ",
                                Long.toString(Utils.decodeDateStr(searchDateToEntry.getText().toString(),
                                StaticValues.dateDecodeTypeTo24Hour) / 1000));
                    }
                    if (searchCarSpinner.getSelectedItemId() != -1) {
                        whereConditions.putString(
                                ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.MILEAGE_TABLE_NAME,
                                MainDbAdapter.MILEAGE_COL_CAR_ID_NAME) + "=",
                                String.valueOf(searchCarSpinner.getSelectedItemId()));
                    }
                    if (searchDriverSpinner.getSelectedItemId() != -1) {
                        whereConditions.putString(
                                ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.MILEAGE_TABLE_NAME,
                                MainDbAdapter.MILEAGE_COL_DRIVER_ID_NAME) + "=",
                                String.valueOf(searchDriverSpinner.getSelectedItemId()));
                    }
                    mListDbHelper.setReportSql("reportMileageListViewSelect", whereConditions);
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
