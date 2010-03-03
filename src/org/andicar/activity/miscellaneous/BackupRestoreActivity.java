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

package org.andicar.activity.miscellaneous;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Collections;
import org.andicar.activity.EditActivityBase;
import org.andicar.activity.R;
import org.andicar.persistence.FileUtils;
import org.andicar.utils.StaticValues;


/**
 *
 * @author miki
 */
public class BackupRestoreActivity extends EditActivityBase {

    private ArrayList<String> bkFileList;
    private String selectedFile = null;
    private Button btnRestore;
    private Button btnBackup;
    private Button btnDeleteBk;
    private ListView backupSetList;
    private BackupRestoreActivity me;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView( R.layout.backup_restore_activity );
        btnBackup = (Button) findViewById(R.id.bkRestoreBackupBtn);
        btnBackup.setOnClickListener(btnBkClickListener);
        btnRestore = (Button) findViewById(R.id.bkRestoreRestoreBtn);
        btnRestore.setOnClickListener(btnRestoreClickListener);
        btnRestore.setEnabled(false);
        btnDeleteBk = (Button) findViewById(R.id.bkRestoreDeleteBkBtn);
        btnDeleteBk.setOnClickListener(btnDeleteBkClickListener);
        btnDeleteBk.setEnabled(false);
        backupSetList = (ListView) findViewById(android.R.id.list);
        me = this;
        fillBkList();
    }

    private void fillBkList() {
        bkFileList = getBkFiles();
        if(bkFileList == null || bkFileList.isEmpty()){
            btnRestore.setEnabled(false);
            btnDeleteBk.setEnabled(false);
            backupSetList.setAdapter(null);
            return;
        }
        ArrayAdapter listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, bkFileList);
        backupSetList.setAdapter(listAdapter);
        backupSetList.setItemsCanFocus(false);
        backupSetList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        backupSetList.setOnItemClickListener(bkFileSelectedListener);
    }

        protected ArrayList<String> getBkFiles() {
        ArrayList<String> myData = FileUtils.getBkFileNames();
        if(myData != null){
            Collections.sort(myData, String.CASE_INSENSITIVE_ORDER);
            Collections.reverse(myData);
        }
        return myData;
    }

    protected AdapterView.OnItemClickListener bkFileSelectedListener = new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                selectedFile = bkFileList.get(arg2);
                btnRestore.setEnabled(true);
                btnDeleteBk.setEnabled(true);
            }
        };

    private View.OnClickListener btnBkClickListener =  new View.OnClickListener() {
            public void onClick(View arg0) {
                if(mMainDbHelper.backupDb()){
                    Toast toast = Toast.
                            makeText( getApplicationContext(),
                            mRes.getString( R.string.BKRESTORE_ACTIVITY_BKOK_MESSAGE ), Toast.LENGTH_SHORT);
                    toast.show();
                    fillBkList();
                }
                else{
                    exceptionAlertBuilder.setMessage(mRes.getString( R.string.BKRESTORE_ACTIVITY_BKFAILED_MESSAGE ) + "\n" +
                            mMainDbHelper.lastErrorMessage);
                    exceptionAlert = exceptionAlertBuilder.create();
                    exceptionAlert.show();
                }
                btnRestore.setEnabled(false);
                btnDeleteBk.setEnabled(false);
            }
    };

    private View.OnClickListener btnRestoreClickListener =  new View.OnClickListener() {
        public void onClick(View arg0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(me);
            builder.setMessage(mRes.getString(R.string.BKRESTORE_ACTIVITY_RESTORE_CONFIRM));
            builder.setCancelable(false);
            builder.setPositiveButton(mRes.getString(R.string.GEN_YES),
                       new DialogInterface.OnClickListener() {
                           public void onClick(DialogInterface dialog, int id) {
                                if(mMainDbHelper.restoreDb(selectedFile)){
                                    SharedPreferences.Editor editor = mPreferences.edit();
                                    editor.putBoolean("MustClose", true);
                                    editor.putLong( "CurrentCar_ID", -1);
                                    editor.putLong( "CurrentDriver_ID", -1);
                                    editor.commit();
                                    AlertDialog.Builder builder = new AlertDialog.Builder(me);
                                    builder.setMessage(mRes.getString(R.string.BKRESTORE_ACTIVITY_RESTOREOK_MESSAGE));
                                    builder.setCancelable(false);
                                    builder.setPositiveButton(mRes.getString(R.string.GEN_OK),
                                               new DialogInterface.OnClickListener() {
                                                   public void onClick(DialogInterface dialog, int id) {
                                                       finish();
                                                   }
                                               });
                                    AlertDialog alert = builder.create();
                                    alert.show();
                                }
                                else{
                                    exceptionAlertBuilder.setMessage(mRes.getString( R.string.BKRESTORE_ACTIVITY_BKFAILED_MESSAGE ) + "\n" +
                                            mMainDbHelper.lastErrorMessage);
                                    exceptionAlert = exceptionAlertBuilder.create();
                                    exceptionAlert.show();
                                }
                           }
                       });
            builder.setNegativeButton(mRes.getString(R.string.GEN_NO),
                        new DialogInterface.OnClickListener() {
                           public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                           }
                        });
            AlertDialog alert = builder.create();
            alert.show();
            }
    };

    private View.OnClickListener btnDeleteBkClickListener =  new View.OnClickListener() {
        public void onClick(View arg0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(me);
            builder.setMessage(mRes.getString(R.string.BKRESTORE_ACTIVITY_DELETE_CONFIRM));
            builder.setCancelable(false);
            builder.setPositiveButton(mRes.getString(R.string.GEN_YES),
                       new DialogInterface.OnClickListener() {
                           public void onClick(DialogInterface dialog, int id) {
                                FileUtils fu = new FileUtils();
                                fu.deleteFile(StaticValues.backupFolder + selectedFile);
                                fillBkList();
                                btnRestore.setEnabled(false);
                                btnDeleteBk.setEnabled(false);
                           }
                       });
            builder.setNegativeButton(mRes.getString(R.string.GEN_NO),
                        new DialogInterface.OnClickListener() {
                           public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                           }
                        });
            AlertDialog alert = builder.create();
            alert.show();
            }
    };
}
