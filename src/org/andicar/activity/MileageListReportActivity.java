/*
 *  Copyright (C) 2010 Miklos Keresztes (miklos.keresztes@gmail.com)
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.andicar.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import org.andicar.persistence.MainDbAdapter;
import org.andicar.persistence.ReportDbAdapter;
import org.andicar.utils.Constants;

/**
 *
 * @author miki
 */
public class MileageListReportActivity extends ReportListActivityBase{

    private Bundle whereConditions;
    private View searchView;
    private EditText genUserCommentEntry;
    private Spinner expTypesearchSpinner;
    private EditText searchDateFromEntry;
    private EditText searchDateToEntry;


    @Override
    public void onCreate( Bundle icicle )
    {
        Long mCarId = getSharedPreferences( Constants.GLOBAL_PREFERENCE_NAME, 0 ).getLong("CurrentCar_ID", 0);
        whereConditions = new Bundle();
        whereConditions.putString(
                ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.MILEAGE_TABLE_NAME, MainDbAdapter.MILEAGE_COL_CAR_ID_NAME) + "=",
                mCarId.toString() );

        super.onCreate( icicle, null, MileageEditActivity.class,
                MainDbAdapter.MILEAGE_TABLE_NAME, ReportDbAdapter.genericReportListViewSelectCols, null,
                null,
                R.layout.threeline_listreport_activity,
                new String[]{ReportDbAdapter.FIRST_LINE_LIST_NAME, ReportDbAdapter.SECOND_LINE_LIST_NAME, ReportDbAdapter.THIRD_LINE_LIST_NAME},
                new int[]{R.id.threeLineListReportText1, R.id.threeLineListReportText2, R.id.threeLineListReportText3},
                "reportMileageListViewSelect",  whereConditions);

    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        if(item.getItemId() == Constants.OPTION_MENU_ADD_ID)
            return super.onOptionsItemSelected( item );
        else if(item.getItemId() == Constants.OPTION_MENU_SEARCH_ID){
            showDialog(0);
        }
        return true;
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
            LayoutInflater factory = LayoutInflater.from(this);
            searchView = factory.inflate(R.layout.mileage_search_dialog, null);
            AlertDialog.Builder searchDialog = new AlertDialog.Builder(MileageListReportActivity.this);
            searchDialog.setTitle(R.string.SEARCH_DIALOG_TITLE);
            searchDialog.setView(searchView);
            searchDialog.setPositiveButton(R.string.GEN_OK, searchDialogButtonlistener);
            searchDialog.setNegativeButton(R.string.GEN_CANCEL, searchDialogButtonlistener);
            expTypesearchSpinner = (Spinner)searchView.findViewById(R.id.searchExpenseTypeSpinner);
            genUserCommentEntry = (EditText)searchView.findViewById(R.id.genUserCommentEntry);
            searchDateFromEntry = (EditText)searchView.findViewById(R.id.searchDateFromEntry);
            searchDateToEntry = (EditText)searchView.findViewById(R.id.searchDateToEntry);
            initSpinner(expTypesearchSpinner, "ExpenseType");
            return searchDialog.create();
    }

    private DialogInterface.OnClickListener searchDialogButtonlistener = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if(whichButton == DialogInterface.BUTTON_POSITIVE){
                            if(expTypesearchSpinner.getSelectedItemId() != -1)
                                whereConditions.putString(
                                        ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.MILEAGE_TABLE_NAME, MainDbAdapter.MILEAGE_COL_EXPENSETYPE_ID_NAME) + "=",
                                        String.valueOf(expTypesearchSpinner.getSelectedItemId()) );
                            if(genUserCommentEntry.getText().toString().length() > 0)
                                whereConditions.putString(
                                        ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.MILEAGE_TABLE_NAME, MainDbAdapter.GEN_COL_USER_COMMENT_NAME) + " LIKE ",
                                        genUserCommentEntry.getText().toString());
                            if(searchDateFromEntry.getText().toString().length() > 0)
                                whereConditions.putString(
                                        ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.MILEAGE_TABLE_NAME, MainDbAdapter.MILEAGE_COL_DATE_NAME) + " >= ",
                                        searchDateFromEntry.getText().toString());
                            if(searchDateToEntry.getText().toString().length() > 0)
                                whereConditions.putString(
                                        ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.MILEAGE_TABLE_NAME, MainDbAdapter.MILEAGE_COL_DATE_NAME) + " <= ",
                                        searchDateToEntry.getText().toString());
                        }
                    };
    };

}
