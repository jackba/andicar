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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.andicar.activity.R;

/**
 *
 * @author miki
 */
public class FileUtils {

    public String lastError = null;

    public int onCreate(){
        try{
            lastError = null;
            File file = new File("/sdcard");
            if(!file.exists() || !file.isDirectory()){
                lastError = "SDCARD not found.";
                return R.string.ERR_020;
            }
            file = new File("/sdcard/andicar/");
            if(!file.exists()){
                if(!file.mkdir()){
                    lastError = "Report folder (SDCARD/andicar) cannot be created.";
                    return R.string.ERR_021;
                }
            }
        }
        catch(SecurityException e){
            lastError = e.getMessage();
            return -2;
        }
        return -1;
    }

    public int writeToFile(String content, String fileName){
        try
        {
            lastError = null;
            File file = new File("/sdcard/andicar/" + fileName);
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
