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

package org.andicar.persistence;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.andicar.activity.R;
import org.andicar.utils.Constants;

/**
 *
 * @author miki
 */
public class FileUtils {
    private AlertDialog.Builder exceptionAlertBuilder;
    private AlertDialog exceptionAlert;
    private Resources mRes;

    public String lastError = null;

    public int onCreate(Context ctx){
        try{
            lastError = null;
            mRes = ctx.getResources();
            File file = new File("/sdcard");
            if(!file.exists() || !file.isDirectory()){
                lastError = "SDCARD not found.";
                exceptionAlertBuilder = new AlertDialog.Builder(ctx);
                exceptionAlertBuilder.setCancelable( false );
                exceptionAlertBuilder.setPositiveButton( mRes.getString(R.string.GEN_OK), null );
                exceptionAlertBuilder.setMessage(mRes.getString(R.string.ERR_020));
                exceptionAlert = exceptionAlertBuilder.create();
                exceptionAlert.show();
                return R.string.ERR_020;
            }
            file = new File(Constants.reportFolder);
            if(!file.exists()){
                if(!file.mkdirs()){
                    lastError = "Report folder " +  Constants.reportFolder + " cannot be created.";
                    exceptionAlertBuilder = new AlertDialog.Builder(ctx);
                    exceptionAlertBuilder.setCancelable( false );
                    exceptionAlertBuilder.setPositiveButton( mRes.getString(R.string.GEN_OK), null );
                    exceptionAlertBuilder.setMessage(mRes.getString(R.string.ERR_021));
                    exceptionAlert = exceptionAlertBuilder.create();
                    exceptionAlert.show();
                    return R.string.ERR_021;
                }
            }
            file = new File(Constants.backupFolder);
            if(!file.exists()){
                if(!file.mkdirs()){
                    lastError = "Backup folder " +  Constants.backupFolder + " cannot be created.";
                    exceptionAlertBuilder = new AlertDialog.Builder(ctx);
                    exceptionAlertBuilder.setCancelable( false );
                    exceptionAlertBuilder.setPositiveButton( mRes.getString(R.string.GEN_OK), null );
                    exceptionAlertBuilder.setMessage(mRes.getString(R.string.ERR_024));
                    exceptionAlert = exceptionAlertBuilder.create();
                    exceptionAlert.show();
                    return R.string.ERR_024;
                }
            }
        }
        catch(SecurityException e){
            exceptionAlertBuilder = new AlertDialog.Builder(ctx);
            exceptionAlertBuilder.setCancelable( false );
            exceptionAlertBuilder.setPositiveButton( mRes.getString(R.string.GEN_OK), null );
            exceptionAlertBuilder.setMessage(e.getMessage());
            exceptionAlert = exceptionAlertBuilder.create();
            exceptionAlert.show();
            return -2;
        }
        return -1;
    }

    public int writeToFile(String content, String fileName){
        try
        {
            lastError = null;
            File file = new File(Constants.reportFolder + fileName);
            if(!file.createNewFile())
                return R.string.ERR_022;
            FileWriter fw = new FileWriter(file);
            fw.append(content);
            fw.flush();
            fw.close();
        }
        catch (IOException e)
        {
            lastError = e.getMessage();
            return R.string.ERR_023;
        }
        return -1;
    }

}
