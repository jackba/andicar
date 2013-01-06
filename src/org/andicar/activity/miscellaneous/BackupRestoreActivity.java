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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.andicar.activity.BaseActivity;
import org.andicar2.activity.R;
import org.andicar.activity.dialog.AndiCarDialogBuilder;
import org.andicar.persistence.FileUtils;
import org.andicar.service.AndiCarServiceStarter;
import org.andicar.utils.AndiCarStatistics;
import org.andicar.utils.StaticValues;

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

/**
 *
 * @author miki
 */
public class BackupRestoreActivity extends BaseActivity {

    private ArrayList<String> bkFileList;
    private String selectedFile = null;
    private Button btnRestore;
    private Button btnBackup;
    private Button btnDelete;
    private ListView lvBackupList;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
    	if(mPreferences.getString("UIStyle", "s01").equalsIgnoreCase("s00"))
    		setContentView(R.layout.backup_restore_activity_s00);
    	else if(mPreferences.getString("UIStyle", "s01").equalsIgnoreCase("s01"))
    		setContentView(R.layout.backup_restore_activity_s01);

        btnBackup = (Button) findViewById(R.id.btnBackup);
        btnBackup.setOnClickListener(btnBkClickListener);
        btnRestore = (Button) findViewById(R.id.btnRestore);
        btnRestore.setOnClickListener(btnRestoreClickListener);
        btnRestore.setEnabled(false);
        btnDelete = (Button) findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(btnDeleteBkClickListener);
        btnDelete.setEnabled(false);
        lvBackupList = (ListView) findViewById(R.id.lvBackupList);
        fillBkList();

    }

    private void fillBkList() {
        bkFileList = FileUtils.getBkFiles();
        if(bkFileList == null || bkFileList.isEmpty()){
            btnRestore.setEnabled(false);
            btnDelete.setEnabled(false);
            lvBackupList.setAdapter(null);
            return;
        }
        
        int listLayout = R.layout.simple_list_item_single_choice_s01;
    	if(mPreferences.getString("UIStyle", "s01").equalsIgnoreCase("s00"))
    		listLayout = android.R.layout.simple_list_item_single_choice;
    	else if(mPreferences.getString("UIStyle", "s01").equalsIgnoreCase("s01"))
    		listLayout = R.layout.simple_list_item_single_choice_s01;
    	
        ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(this,
        		listLayout, bkFileList);
        lvBackupList.setAdapter(listAdapter);
        lvBackupList.setItemsCanFocus(false);
        lvBackupList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lvBackupList.setOnItemClickListener(bkFileSelectedListener);
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
                if(mDbAdapter.backupDb(null, null)){
                    Toast toast = Toast.
                            makeText( getApplicationContext(),
                            mResource.getString( R.string.BackupRestoreEditActivity_BackupCreatedMessage ), Toast.LENGTH_SHORT);
                    toast.show();
                    fillBkList();
                    if(isSendStatistics){
                        Map<String, String> parameters = new HashMap<String, String>();
                        parameters.put("Operation", "Backup");
                        AndiCarStatistics.sendFlurryEvent(BackupRestoreActivity.this, "BackupRestore", parameters);
                    }
                }
                else{
                    madbErrorAlert.setMessage(mResource.getString( R.string.BackupRestoreEditActivity_BackupFailedMessage ) + "\n" +
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
            AndiCarDialogBuilder builder = new AndiCarDialogBuilder(BackupRestoreActivity.this, 
            		AndiCarDialogBuilder.DIALOGTYPE_WARNING, mResource.getString(R.string.GEN_Confirm));
            builder.setMessage(mResource.getString(R.string.BackupRestoreEditActivity_RestoreConfirmation));
            builder.setCancelable(false);
            builder.setPositiveButton(mResource.getString(R.string.GEN_YES),
                       new DialogInterface.OnClickListener() {
                           public void onClick(DialogInterface dialog, int id) {
                                if(mDbAdapter.restoreDb(selectedFile)){
                                    SharedPreferences.Editor editor = mPreferences.edit();
                                    editor.putBoolean("MustClose", true);
                                    //reset current car
                                    editor.putLong( "CurrentCar_ID", -1);
//                                    editor.putLong( "CurrentDriver_ID", -1);
                                    editor.commit();
                                    try{
                                    	AndiCarServiceStarter.startServices(BackupRestoreActivity.this, "BackupService");
                                    }
                                    catch(Exception e){}
                                    if(isSendStatistics){
                                        Map<String, String> parameters = new HashMap<String, String>();
                                        parameters.put("Operation", "Restore");
                                        AndiCarStatistics.sendFlurryEvent(BackupRestoreActivity.this, "BackupRestore", parameters);
                                    }

                                    AndiCarDialogBuilder builder = new AndiCarDialogBuilder(BackupRestoreActivity.this, 
                                    		AndiCarDialogBuilder.DIALOGTYPE_INFO, mResource.getString(R.string.GEN_Info));
                                    builder.setMessage(mResource.getString(R.string.BackupRestoreEditActivity_RestoreOKMessage));
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
                                    madbErrorAlert.setMessage(mResource.getString( R.string.BackupRestoreEditActivity_RestoreFailedMessage ) + "\n" +
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
            AndiCarDialogBuilder builder = new AndiCarDialogBuilder(BackupRestoreActivity.this, 
            		AndiCarDialogBuilder.DIALOGTYPE_QUESTION, mResource.getString(R.string.GEN_Confirm));
            builder.setMessage(mResource.getString(R.string.BackupRestoreEditActivity_BackupDeleteConfirmation));
            builder.setCancelable(false);
            builder.setPositiveButton(mResource.getString(R.string.GEN_YES),
                       new DialogInterface.OnClickListener() {
                           public void onClick(DialogInterface dialog, int id) {
                                FileUtils.deleteFile(StaticValues.BACKUP_FOLDER + selectedFile);
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

	/* (non-Javadoc)
	 * @see org.andicar.activity.BaseActivity#setSpecificLayout()
	 */
	@Override
	public void setSpecificLayout() {
	}
}
