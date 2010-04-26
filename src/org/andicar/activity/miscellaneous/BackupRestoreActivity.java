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
import com.flurry.android.FlurryAgent;
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
    private Button btnDelete;
    private ListView lvBackupSet;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView( R.layout.backup_restore_activity );
        btnBackup = (Button) findViewById(R.id.btnBackup);
        btnBackup.setOnClickListener(btnBkClickListener);
        btnRestore = (Button) findViewById(R.id.btnRestore);
        btnRestore.setOnClickListener(btnRestoreClickListener);
        btnRestore.setEnabled(false);
        btnDelete = (Button) findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(btnDeleteBkClickListener);
        btnDelete.setEnabled(false);
        lvBackupSet = (ListView) findViewById(R.id.lvBackupList);
        fillBkList();
    }

    @Override
    public void onStart()
    {
        super.onStart();
        FlurryAgent.setReportLocation(false);
        FlurryAgent.onStartSession(this, "E8C8QUTB7KS46SHMEP6V");
    }
    @Override
    public void onStop()
    {
       super.onStop();
       FlurryAgent.onEndSession(this);
    }


    private void fillBkList() {
        bkFileList = getBkFiles();
        if(bkFileList == null || bkFileList.isEmpty()){
            btnRestore.setEnabled(false);
            btnDelete.setEnabled(false);
            lvBackupSet.setAdapter(null);
            return;
        }
        ArrayAdapter listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, bkFileList);
        lvBackupSet.setAdapter(listAdapter);
        lvBackupSet.setItemsCanFocus(false);
        lvBackupSet.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lvBackupSet.setOnItemClickListener(bkFileSelectedListener);
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
                btnDelete.setEnabled(true);
            }
        };

    private View.OnClickListener btnBkClickListener =  new View.OnClickListener() {
            public void onClick(View arg0) {
                if(mDbAdapter.backupDb(null)){
                    Toast toast = Toast.
                            makeText( getApplicationContext(),
                            mResource.getString( R.string.BKRESTORE_ACTIVITY_BKOK_MESSAGE ), Toast.LENGTH_SHORT);
                    toast.show();
                    fillBkList();
                }
                else{
                    madbErrorAlert.setMessage(mResource.getString( R.string.BKRESTORE_ACTIVITY_BKFAILED_MESSAGE ) + "\n" +
                            mDbAdapter.lastErrorMessage);
                    madError = madbErrorAlert.create();
                    madError.show();
                }
                btnRestore.setEnabled(false);
                btnDelete.setEnabled(false);
            }
    };

    private View.OnClickListener btnRestoreClickListener =  new View.OnClickListener() {
        public void onClick(View arg0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(BackupRestoreActivity.this);
            builder.setMessage(mResource.getString(R.string.BKRESTORE_ACTIVITY_RESTORE_CONFIRM));
            builder.setCancelable(false);
            builder.setPositiveButton(mResource.getString(R.string.GEN_YES),
                       new DialogInterface.OnClickListener() {
                           public void onClick(DialogInterface dialog, int id) {
                                if(mDbAdapter.restoreDb(selectedFile)){
                                    SharedPreferences.Editor editor = mPreferences.edit();
                                    editor.putBoolean("MustClose", true);
                                    editor.putLong( "CurrentCar_ID", -1);
                                    editor.putLong( "CurrentDriver_ID", -1);
                                    editor.commit();
                                    AlertDialog.Builder builder = new AlertDialog.Builder(BackupRestoreActivity.this);
                                    builder.setMessage(mResource.getString(R.string.BKRESTORE_ACTIVITY_RESTOREOK_MESSAGE));
                                    builder.setCancelable(false);
                                    builder.setPositiveButton(mResource.getString(R.string.GEN_OK),
                                               new DialogInterface.OnClickListener() {
                                                   public void onClick(DialogInterface dialog, int id) {
                                                       finish();
                                                   }
                                               });
                                    AlertDialog alert = builder.create();
                                    alert.show();
                                }
                                else{
                                    madbErrorAlert.setMessage(mResource.getString( R.string.BKRESTORE_ACTIVITY_BKFAILED_MESSAGE ) + "\n" +
                                            mDbAdapter.lastErrorMessage);
                                    madError = madbErrorAlert.create();
                                    madError.show();
                                }
                           }
                       });
            builder.setNegativeButton(mResource.getString(R.string.GEN_NO),
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
            AlertDialog.Builder builder = new AlertDialog.Builder(BackupRestoreActivity.this);
            builder.setMessage(mResource.getString(R.string.BKRESTORE_ACTIVITY_DELETE_CONFIRM));
            builder.setCancelable(false);
            builder.setPositiveButton(mResource.getString(R.string.GEN_YES),
                       new DialogInterface.OnClickListener() {
                           public void onClick(DialogInterface dialog, int id) {
                                FileUtils fu = new FileUtils();
                                fu.deleteFile(StaticValues.BACKUP_FOLDER + selectedFile);
                                fillBkList();
                                btnRestore.setEnabled(false);
                                btnDelete.setEnabled(false);
                           }
                       });
            builder.setNegativeButton(mResource.getString(R.string.GEN_NO),
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
