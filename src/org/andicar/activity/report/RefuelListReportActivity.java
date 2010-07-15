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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Spinner;
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
public class RefuelListReportActivity extends ReportListActivityBase{
    private View searchView;
    private EditText etUserCommentSearch;
    private EditText etDateFromSearch;
    private EditText etDateToSearch;
    private AutoCompleteTextView acTag;
    private Spinner spnDriverSearch;
    private Spinner spnCarSearch;
    private Spinner spnExpTypeSearch;
    private ArrayAdapter<String> tagAdapter;

    @Override
    public void onCreate( Bundle icicle )
    {
        reportSelectName = "reportRefuelListViewSelect";
        Long mCarId = getSharedPreferences( StaticValues.GLOBAL_PREFERENCE_NAME, 0 ).getLong("CurrentCar_ID", 0);
        if(icicle == null){
            whereConditions = new Bundle();
            whereConditions.putString(
                    ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.REFUEL_TABLE_NAME, MainDbAdapter.REFUEL_COL_CAR_ID_NAME) + "=",
                    mCarId.toString() );
        }
        else
            whereConditions = (Bundle)getLastNonConfigurationInstance();

        super.onCreate( icicle, null, RefuelEditActivity.class, null,
                MainDbAdapter.REFUEL_TABLE_NAME, ReportDbAdapter.genericReportListViewSelectCols, null,
                null,
                R.layout.threeline_listreport_activity,
                new String[]{ReportDbAdapter.FIRST_LINE_LIST_NAME, ReportDbAdapter.SECOND_LINE_LIST_NAME, ReportDbAdapter.THIRD_LINE_LIST_NAME},
                new int[]{R.id.tvThreeLineListReportText1, R.id.tvThreeLineListReportText2, R.id.tvThreeLineListReportText3},
                reportSelectName,  whereConditions, null);

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
        searchView = liLayoutFactory.inflate(R.layout.refuel_search_dialog, null);
        AlertDialog.Builder searchDialog = new AlertDialog.Builder(RefuelListReportActivity.this);
        searchDialog.setTitle(R.string.DIALOGSearch_DialogTitle);
        searchDialog.setView(searchView);
        searchDialog.setPositiveButton(R.string.GEN_OK, searchDialogButtonlistener);
        searchDialog.setNegativeButton(R.string.GEN_CANCEL, searchDialogButtonlistener);
        spnExpTypeSearch = (Spinner) searchView.findViewById(R.id.spnExpTypeSearch);
        initSpinner(spnExpTypeSearch, MainDbAdapter.EXPENSETYPE_TABLE_NAME);
        etUserCommentSearch = (EditText) searchView.findViewById(R.id.etUserCommentSearch);
        etUserCommentSearch.setText("%");
        etDateFromSearch = (EditText) searchView.findViewById(R.id.etDateFromSearch);
        etDateToSearch = (EditText) searchView.findViewById(R.id.etDateToSearch);
        spnCarSearch = (Spinner) searchView.findViewById(R.id.spnCarSearch);
        initSpinner(spnCarSearch, MainDbAdapter.CAR_TABLE_NAME);
        spnDriverSearch = (Spinner) searchView.findViewById(R.id.spnDriverSearch);
        initSpinner(spnDriverSearch, MainDbAdapter.DRIVER_TABLE_NAME);
        acTag = ((AutoCompleteTextView) searchView.findViewById( R.id.acTag ));
        tagAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line,
                mDbAdapter.getAutoCompleteText(MainDbAdapter.TAG_TABLE_NAME, null,
                0, 0));
        acTag.setAdapter(tagAdapter);
        acTag.setText("%");
        return searchDialog.create();
    }
    private DialogInterface.OnClickListener searchDialogButtonlistener = new DialogInterface.OnClickListener() {

        public void onClick(DialogInterface dialog, int whichButton) {
            if (whichButton == DialogInterface.BUTTON_POSITIVE) {
                try {
                    whereConditions.clear();
                    if (spnExpTypeSearch.getSelectedItemId() != -1) {
                        whereConditions.putString(
                                ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.REFUEL_TABLE_NAME,
                                MainDbAdapter.REFUEL_COL_EXPENSETYPE_ID_NAME) + "=",
                                String.valueOf(spnExpTypeSearch.getSelectedItemId()));
                    }
                    if (etUserCommentSearch.getText().toString().length() > 0) {
                        whereConditions.putString(
                                ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.REFUEL_TABLE_NAME,
                                MainDbAdapter.GEN_COL_USER_COMMENT_NAME) + " LIKE ",
                                etUserCommentSearch.getText().toString());
                    }
                    if (etDateFromSearch.getText().toString().length() > 0) {
                        whereConditions.putString(
                                ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.REFUEL_TABLE_NAME,
                                MainDbAdapter.REFUEL_COL_DATE_NAME) + " >= ",
                                Long.toString(Utils.decodeDateStr(etDateFromSearch.getText().toString(),
                                StaticValues.DATE_DECODE_TO_ZERO) / 1000));
                    }
                    if (etDateToSearch.getText().toString().length() > 0) {
                        whereConditions.putString(
                                ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.REFUEL_TABLE_NAME,
                                MainDbAdapter.REFUEL_COL_DATE_NAME) + " <= ",
                                Long.toString(Utils.decodeDateStr(etDateToSearch.getText().toString(),
                                StaticValues.DATE_DECODE_TO_24) / 1000));
                    }
                    if (spnCarSearch.getSelectedItemId() != -1) {
                        whereConditions.putString(
                                ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.REFUEL_TABLE_NAME,
                                MainDbAdapter.REFUEL_COL_CAR_ID_NAME) + "=",
                                String.valueOf(spnCarSearch.getSelectedItemId()));
                    }
                    if (spnDriverSearch.getSelectedItemId() != -1) {
                        whereConditions.putString(
                                ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.REFUEL_TABLE_NAME,
                                MainDbAdapter.REFUEL_COL_DRIVER_ID_NAME) + "=",
                                String.valueOf(spnDriverSearch.getSelectedItemId()));
                    }
                    if (acTag.getText().toString() != null) {
                    	if(acTag.getText().toString().length() == 0)
                            whereConditions.putString(
                                    ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.REFUEL_TABLE_NAME,
                                    							MainDbAdapter.REFUEL_COL_TAG_ID_NAME) + " is ",
                                    "null");
                    	else
                            whereConditions.putString(
                            		"COALESCE( " +
	                                    ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.TAG_TABLE_NAME,
	                                    							MainDbAdapter.GEN_COL_NAME_NAME) + ", '') LIKE ",
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

}
