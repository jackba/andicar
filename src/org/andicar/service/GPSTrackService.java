/*
 *  AndiCar - a car management software for Android powered devices.
 *
 *  Copyright (C) 2013 Miklos Keresztes (miklos.keresztes@gmail.com)
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

package org.andicar.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.andicar.activity.MileageEditActivity;
import org.andicar.activity.miscellaneous.GPSTrackController;
import org.andicar.persistence.FileUtils;
import org.andicar.persistence.MainDbAdapter;
import org.andicar.utils.AndiCarExceptionHandler;
import org.andicar.utils.AndiCarStatistics;
import org.andicar.utils.StaticValues;
import org.andicar.utils.Utils;
import org.andicar2.activity.R;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.text.format.DateFormat;
import android.widget.Toast;

/**
 *
 * @author miki
 */
public class GPSTrackService extends Service {

    private SharedPreferences mPreferences;
    protected MainDbAdapter mDbAdapter = null;
    protected Resources mResource = null;

    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private double dOldLocationLatitude = 0;
    private double dOldLocationLongitude = 0;
    private double dCurrentLocationLatitude = 0;
    private double dCurrentLocationLongitude = 0;
    private double dCurrentLocationAltitude = 0;
    private double dCurrentLocationBearing = 0;
    private double lastGoodLocationLatitude = 0;
    private double lastGoodLocationLongitude = 0;
    private double lastGoodLocationAltitude = 0;
    private double dCurrentAccuracy = 0 ;
    private double dCurrentSpeed = 0;
    private double dOldSpeed = 0;
    private long lCurrentLocationTime = 0;
    private long lOldLocationTime = 0;
    private Calendar currentLocationDateTime = Calendar.getInstance();
    private String currentLocationDateTimeGPXStr = "";
    private float[] fDistanceArray = new float[1];
    private double dDistanceBetweenLocations = 0;
    private long gpsTrackId = 0;
    private File debugLogFile = null;
    private FileWriter debugLogFileWriter = null;
    private File gpsTrackDetailCSVFile = null;
    private FileWriter gpsTrackDetailCSVFileWriter = null;
    private File gpsTrackDetailGOPFile = null;
    private FileWriter gpsTrackDetailGOPFileWriter = null;
    private File gpsTrackDetailKMLFile = null;
    private FileWriter gpsTrackDetailKMLFileWriter = null;
    private File gpsTrackDetailGPXFile = null;
    private FileWriter gpsTrackDetailGPXFileWriter = null;
    /* tmp values */
    private String sName = null;
    private String sUserComment = null;
    private String sTag = null;
    private long mCarId = -1;
    private long mDriverId = -1;
    private boolean isUseKML = false;
    private boolean isUseGPX = false;
    //statistics
    private double dMinAccuracy = 9999;
    private double dAvgAccuracy = 0;
    private double dMaxAccuracy = 0;
    private double dMinAltitude = 99999;
    private double dMaxAltitude = 0;
    private long lStartTime = 0;
    private long lStopTime = 0;
    private double dTotalDistance = 0;
    private double dMaxSpeed = 0;
    private double dAvgSpeed = 0;
    private double dAvgMovingSpeed = 0;
    private long lTotalTime = 0;
    private long lTotalMovingTime = 0;
    private long lTotalPauseTime = 0;
    private long lCurrentPauseStartTime = 0;
    private long lCurrentPauseEndTime = 0;
    private long lCurrentPauseTime = 0;
    private boolean isFirstPoint = true;
    private boolean isFirstPointAfterResume = false;
    private long lFirstNonMovingTime = 0;
    private long lLastNonMovingTime = 0;
    private long lTotalNonMovingTime = 0;
    private int iMaxAccuracy = 9999999;
    private int iMaxAccuracyShutdownLimit = 30;
    private double dTotalTrackPoints = 0;
    private double dTotalSkippedTrackPoints = 0;
    private double dTotalUsedTrackPoints = 0;
    private double skippedPointPercentage = 0;
    private boolean isSendCrashReport;
    private int iFileCount = 1; //for splitting large gps files
    private int iFileSplitCount = 0;
    private boolean isUseMetricUnits = true;
    private boolean isErrorStop = false;
    
    private boolean isEnableDebugLog = false;
    
    private long csvPointsCount = 0;
    private long kmlPointsCount = 0;
    private long gpxPointsCount = 0;
    private long gopPointsCount = 0;



    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        GPSTrackService getService() {
            return GPSTrackService.this;
        }
    }

    @Override
    public void onCreate() {
    	if(isEnableDebugLog)
    		logDebugInfo("onCreate() started", new Throwable());
    	
    	if(gpsTrackId > 0)
    		return;
    	
        mPreferences = getSharedPreferences(StaticValues.GLOBAL_PREFERENCE_NAME, Context.MODE_MULTI_PROCESS);
        mResource = getResources();
        isSendCrashReport = mPreferences.getBoolean("SendCrashReport", true);
        if(isSendCrashReport)
            Thread.setDefaultUncaughtExceptionHandler(
                    new AndiCarExceptionHandler(Thread.getDefaultUncaughtExceptionHandler(), this));
        
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, R.string.GPSTrackService_GPSDisabledMessage, Toast.LENGTH_SHORT).show();
            isErrorStop = true;
            stopSelf();
        }

        mLocationListener = new AndiCarLocationListener();
        mLocationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER, 
                Long.parseLong(mPreferences.getString("GPSTrackMinTime", "0")),
                /*Long.parseLong(mPreferences.getString("GPSTrackMinDistance", "5"))*/ 0,
                mLocationListener);

        mDbAdapter = new MainDbAdapter(this);

        sName = mPreferences.getString("GPSTrackTmp_Name", null);
        sUserComment = mPreferences.getString("GPSTrackTmp_UserComment", null);
        sTag = mPreferences.getString("GPSTrackTmp_Tag", null);
        mCarId = mPreferences.getLong("GPSTrackTmp_CarId", mPreferences.getLong("CurrentCar_ID", 1));
        mDriverId = mPreferences.getLong("GPSTrackTmp_DriverId", mPreferences.getLong("LastDriver_ID", 1));
        isUseKML = mPreferences.getBoolean("GPSTrackTmp_IsUseKML", false);
        isUseGPX = mPreferences.getBoolean("GPSTrackTmp_IsUseGPX", false);
        iMaxAccuracy = Integer.parseInt(mPreferences.getString("GPSTrackMaxAccuracy", "20"));
        iMaxAccuracyShutdownLimit = Integer.parseInt(mPreferences.getString("GPSTrackMaxAccuracyShutdownLimit", "30"));

        iFileSplitCount = Integer.parseInt(mPreferences.getString("GPSTrackTrackFileSplitCount", "0"));

        //create the master record
        //use direct table insert for increasing the speed of the DB operation
        ContentValues cvData = new ContentValues();
        cvData.put(MainDbAdapter.COL_NAME_GEN_NAME, sName);
        cvData.put(MainDbAdapter.COL_NAME_GEN_USER_COMMENT, sUserComment);
        cvData.put(MainDbAdapter.COL_NAME_GPSTRACK__CAR_ID, mCarId);
        cvData.put(MainDbAdapter.COL_NAME_GPSTRACK__DRIVER_ID, mDriverId);
        cvData.put(MainDbAdapter.COL_NAME_GPSTRACK__DATE, (System.currentTimeMillis() / 1000));
        if(sTag != null && sTag.length() > 0){
        	long mTagId = 0;
            String selection = "UPPER (" + MainDbAdapter.COL_NAME_GEN_NAME + ") = ?";
            String[] selectionArgs = {sTag.toUpperCase(Locale.US)};
            Cursor c = mDbAdapter.query(MainDbAdapter.TABLE_NAME_TAG, MainDbAdapter.COL_LIST_GEN_ROWID_NAME, selection, selectionArgs,
                    null, null, null);
            String tagIdStr = null;
            if(c.moveToFirst())
                tagIdStr = c.getString(MainDbAdapter.COL_POS_GEN_ROWID);
            c.close();
            if(tagIdStr != null && tagIdStr.length() > 0){
                mTagId = Long.parseLong(tagIdStr);
                cvData.put(MainDbAdapter.COL_NAME_GPSTRACK__TAG_ID, mTagId);
            }
            else{
                ContentValues tmpData = new ContentValues();
                tmpData.put(MainDbAdapter.COL_NAME_GEN_NAME, sTag);
                mTagId = mDbAdapter.createRecord(MainDbAdapter.TABLE_NAME_TAG, tmpData);
                if(mTagId >= 0)
                	cvData.put(MainDbAdapter.COL_NAME_GPSTRACK__TAG_ID, mTagId);
            }
        }
        else
        	cvData.put(MainDbAdapter.COL_NAME_GPSTRACK__TAG_ID, (String)null);

        gpsTrackId = mDbAdapter.createRecord(MainDbAdapter.TABLE_NAME_GPSTRACK, cvData);
    	if(isEnableDebugLog)
    		logDebugInfo("onCreate(): Track saved in DB gpsTrackId = " + gpsTrackId, null);

        long lMileId = mDbAdapter.getIdFromCode(MainDbAdapter.TABLE_NAME_UOM, "mi");
        long lCarUomId = mDbAdapter.getCarUOMLengthID(mCarId);
        if(lCarUomId == lMileId)
            isUseMetricUnits = false; //use imperial units
        else
            isUseMetricUnits = true;

        //create the track detail file(s)
        try {
            createFiles();

            // Display a notification about us starting.  We put an icon in the status bar.
            showNotification(StaticValues.NOTIF_GPS_TRACKING_STARTED_ID, false);
            SharedPreferences.Editor editor = mPreferences.edit();
            editor.putBoolean("isGpsTrackOn", true);
            editor.commit();
        }
        catch(IOException ex) {
            Logger.getLogger(GPSTrackService.class.getName()).log(Level.SEVERE, null, ex);
            showNotification(StaticValues.NOTIF_FILESYSTEM_ERROR_ID, true);
            isErrorStop = true;
            stopSelf();
        }
        //close the database
        if(mDbAdapter != null){
            mDbAdapter.close();
            mDbAdapter = null;
        }

        if(isSendCrashReport)
            AndiCarStatistics.sendFlurryEvent(this, "GPSTrack", null);
    }

    private void createCSVFile(String fileName) throws IOException {
        gpsTrackDetailCSVFile = FileUtils.createGpsTrackDetailFile(StaticValues.CSV_FORMAT, fileName);
        gpsTrackDetailCSVFileWriter = new FileWriter(gpsTrackDetailCSVFile);
//    	gpsTrackDetailCSVFileWriter = FileUtils.createGpsTrackDetailFileWriter(StaticValues.CSV_FORMAT, fileName);
    	
        //create the header
        gpsTrackDetailCSVFileWriter.append(MainDbAdapter.COL_NAME_GPSTRACKDETAIL__GPSTRACK_ID + "," +
                                                "Accuracy" + (isUseMetricUnits? " [m]" : " [yd]") + "," +
//                                                "Accuracy [m], " + //debug
                                                "Altitude" + (isUseMetricUnits? " [m]" : " [yd]") + "," +
//                                                "Altitude [m]," + //debug
                                                "Latitude" + "," +
                                                "Longitude" + "," +
                                                "Speed" + (isUseMetricUnits? " [km/h]" : " [mi/h]") + "," +
//                                                "Speed [m/s]," + //debug
                                                "Time" + "," +
                                                "Distance" + (isUseMetricUnits? " [m]" : " [yd]") + "," +
//                                                "Distance [m]," + //debug
                                                "Bearing" + "," +
                                                "TotalTrackPointCount" + "," +
                                                "InvalidTrackPointCount" + "," +
                                                "IsValidPoint" + "\n");
        ContentValues cvData = new ContentValues();
        cvData.put(MainDbAdapter.COL_NAME_GPSTRACKDETAIL__GPSTRACK_ID, gpsTrackId);
        cvData.put(MainDbAdapter.COL_NAME_GPSTRACKDETAIL__FILEFORMAT, StaticValues.CSV_FORMAT);
        cvData.put(MainDbAdapter.COL_NAME_GPSTRACKDETAIL__FILE, gpsTrackDetailCSVFile.getAbsolutePath());
        mDbAdapter.createRecord(MainDbAdapter.TABLE_NAME_GPSTRACKDETAIL, cvData);
        gpsTrackDetailCSVFileWriter.flush();
        
    	if(isEnableDebugLog)
    		logDebugInfo("createCSVFile: File created. gpsTrackDetailCSVFile = " + fileName, null);
        
    }

    private void createGOPFile(String fileName) throws IOException {
        gpsTrackDetailGOPFile = FileUtils.createGpsTrackDetailFile(StaticValues.GOP_FORMAT, fileName);
        gpsTrackDetailGOPFileWriter = new FileWriter(gpsTrackDetailGOPFile);
//    	gpsTrackDetailGOPFileWriter = FileUtils.createGpsTrackDetailFileWriter(StaticValues.GOP_FORMAT, fileName);
    	
        //create the header
        gpsTrackDetailGOPFileWriter.append( "LatitudeE6,LongitudeE6,PointType\n");

        ContentValues cvData = new ContentValues();
        cvData.put(MainDbAdapter.COL_NAME_GPSTRACKDETAIL__GPSTRACK_ID, gpsTrackId);
        cvData.put(MainDbAdapter.COL_NAME_GPSTRACKDETAIL__FILEFORMAT, StaticValues.GOP_FORMAT);
        cvData.put(MainDbAdapter.COL_NAME_GPSTRACKDETAIL__FILE, gpsTrackDetailGOPFile.getAbsolutePath());
        mDbAdapter.createRecord(MainDbAdapter.TABLE_NAME_GPSTRACKDETAIL, cvData);
        
        gpsTrackDetailGOPFileWriter.flush();

    	if(isEnableDebugLog)
    		logDebugInfo("createGOPFile: File created. gpsTrackDetailGOPFile = " + fileName, null);

    }

    private void createGPXFile(String fileName) throws IOException{
        gpsTrackDetailGPXFile = FileUtils.createGpsTrackDetailFile(StaticValues.GPX_FORMAT, fileName);
        gpsTrackDetailGPXFileWriter = new FileWriter(gpsTrackDetailGPXFile);
//    	gpsTrackDetailGPXFileWriter = FileUtils.createGpsTrackDetailFileWriter(StaticValues.GPX_FORMAT, fileName);
    	
        if(gpsTrackDetailGPXFileWriter == null)
            return;
        ContentValues cvData = new ContentValues();
        cvData.put(MainDbAdapter.COL_NAME_GPSTRACKDETAIL__GPSTRACK_ID, gpsTrackId);
        cvData.put(MainDbAdapter.COL_NAME_GPSTRACKDETAIL__FILEFORMAT, StaticValues.GPX_FORMAT);
        cvData.put(MainDbAdapter.COL_NAME_GPSTRACKDETAIL__FILE, gpsTrackDetailGPXFile.getAbsolutePath());
        mDbAdapter.createRecord(MainDbAdapter.TABLE_NAME_GPSTRACKDETAIL, cvData);

        gpsTrackDetailGPXFileWriter.append(
                "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" standalone=\"yes\"?>\n"
                + "<?xml-stylesheet type=\"text/xsl\" href=\"details.xsl\"?>\n"
                + "<gpx\n"
                    + "version=\"1.0\"\n"
                    + "creator=\"AndiCar\"\n"
                    + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                    + "xmlns=\"http://www.topografix.com/GPX/1/0\"\n"
                    + "xmlns:topografix=\"http://www.topografix.com/GPX/Private/TopoGrafix/0/1\" "
                        + "xsi:schemaLocation=\"http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd http://www.topografix.com/GPX/Private/TopoGrafix/0/1 http://www.topografix.com/GPX/Private/TopoGrafix/0/1/topografix.xsd\">\n"
                + "<trk>\n"
                + "<name><![CDATA[Trip record for '" + sName +"']]></name>\n"
                + "<desc><![CDATA[Recorded with <a href='http://www.andicar.org'>AndiCar</a><br>" + Utils.getDateStr(true, true, true) + "]]></desc>\n"
//                + "<number>" + gpsTrackId + "_" + iFileCount + "</number>\n" //not a valid number
                + "<topografix:color>c0c0c0</topografix:color>\n"
                + "<trkseg>\n"
            );
        gpsTrackDetailGPXFileWriter.flush();

    	if(isEnableDebugLog)
    		logDebugInfo("createGPXFile: File created. gpsTrackDetailGPXFile = " + fileName, null);

    }

    private void createKMLFile(String fileName) throws IOException{
        gpsTrackDetailKMLFile = FileUtils.createGpsTrackDetailFile(StaticValues.KML_FORMAT, fileName);
        gpsTrackDetailKMLFileWriter = new FileWriter(gpsTrackDetailKMLFile);
//    	gpsTrackDetailKMLFileWriter = FileUtils.createGpsTrackDetailFileWriter(StaticValues.KML_FORMAT, fileName);
    	
        if(gpsTrackDetailKMLFileWriter == null)
            return;
        
        String name = "";
        if(iFileCount == 1){ //first file
            name = sName;
        }
        else{
            name = sName + " part #" + iFileCount;
        }
        
        ContentValues cvData = new ContentValues();
        cvData.put(MainDbAdapter.COL_NAME_GPSTRACKDETAIL__GPSTRACK_ID, gpsTrackId);
        cvData.put(MainDbAdapter.COL_NAME_GPSTRACKDETAIL__FILEFORMAT, StaticValues.KML_FORMAT);
        cvData.put(MainDbAdapter.COL_NAME_GPSTRACKDETAIL__FILE, gpsTrackDetailKMLFile.getAbsolutePath());
        mDbAdapter.createRecord(MainDbAdapter.TABLE_NAME_GPSTRACKDETAIL, cvData);

        //initialize the file header

        gpsTrackDetailKMLFileWriter.append(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://earth.google.com/kml/2.0\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n"
            + "<Document>\n"
            + "<atom:author><atom:name>AndiCar</atom:name></atom:author>\n"
            + "<name><![CDATA[" + name + "]]></name>\n"
            + "<description><![CDATA[Recorded with <a href='http://www.andicar.org'>AndiCar</a>]]></description>\n"
            + "<Style id=\"track\"><LineStyle><color>7f0000ff</color><width>4</width></LineStyle></Style>\n"
            + "<Style id=\"sh_green-circle\"><IconStyle><scale>1.3</scale><Icon><href>http://maps.google.com/mapfiles/kml/paddle/grn-circle.png</href></Icon><hotSpot x=\"32\" y=\"1\" xunits=\"pixels\" yunits=\"pixels\"/></IconStyle></Style>\n"
            + "<Style id=\"sh_red-circle\"><IconStyle><scale>1.3</scale><Icon><href>http://maps.google.com/mapfiles/kml/paddle/red-circle.png</href></Icon><hotSpot x=\"32\" y=\"1\" xunits=\"pixels\" yunits=\"pixels\"/></IconStyle></Style>\n"
            + "<Style id=\"icon28\"><IconStyle><scale>1.3</scale><Icon><href>http://maps.google.com/mapfiles/kml/pal4/icon28.png</href></Icon><hotSpot x=\"20\" y=\"2\" xunits=\"pixels\" yunits=\"pixels\"/></IconStyle></Style>\n"
            + "<Style id=\"icon29\"><IconStyle><scale>1.3</scale><Icon><href>http://maps.google.com/mapfiles/kml/pal4/icon29.png</href></Icon><hotSpot x=\"20\" y=\"2\" xunits=\"pixels\" yunits=\"pixels\"/></IconStyle></Style>"
        	);
        gpsTrackDetailKMLFileWriter.flush();

    	if(isEnableDebugLog)
    		logDebugInfo("createKMLFile: File created. gpsTrackDetailKMLFile = " + fileName, null);

    }

    private void appendKMLStartPoint(){
        if(gpsTrackDetailKMLFileWriter == null){
        	if(isEnableDebugLog)
        		logDebugInfo("appendKMLStartPoint: File writer is NULL!", null);
            return;
        }
        try{
            String pointName = "";
            String pointStyle = "";
            String pointDescription = "";
            
            if(iFileCount == 1){ //first file
                pointName = "Start trip";
                pointStyle = "#sh_green-circle";
                pointDescription = "Start of trip '" + sName + "'<br>" + 
                		DateFormat.getDateFormat(getApplicationContext()).format(lStartTime) + " " + DateFormat.getTimeFormat(getApplicationContext()).format(lStartTime);
            }
            else{
                pointName = "Start of part #" + iFileCount;
                pointStyle = "#icon28";
                pointDescription = "";
            }
        	
            gpsTrackDetailKMLFileWriter.append(
                "<Placemark>\n"
                + "<name><![CDATA[" + pointName + "]]></name>\n"
                + "<description><![CDATA[" + pointDescription + "]]></description>\n"
                + "<styleUrl>"+ pointStyle + "</styleUrl>\n"
                + "<Point>\n"
                + "<coordinates>" + dCurrentLocationLongitude + "," + dCurrentLocationLatitude + "," +
                            dCurrentLocationAltitude + "</coordinates>\n"
                + "</Point>\n"
                + "</Placemark>\n"
                + "<Placemark>\n"
                + "<name><![CDATA[Track file #" + iFileCount + "]]></name>\n"
                + "<description><![CDATA[]]></description>\n"
                + "<styleUrl>#track</styleUrl>\n"
                + "<MultiGeometry>\n"
                + "<LineString>\n"
                + "<coordinates>\n");

        	if(isEnableDebugLog)
        		logDebugInfo("appendKMLStartPoint: Start poin added", null);

        }
        catch(IOException e){
        	if(isEnableDebugLog)
        		logDebugInfo("appendKMLStartPoint: Exception = " + e.getMessage(), null);
        }
    }

    private void appendKMLFooter(boolean isLastFile){
        if(gpsTrackDetailKMLFileWriter == null){
        	if(isEnableDebugLog)
        		logDebugInfo("appendKMLFooter: File writer is NULL!", null);
            return;
        }
        try{
            String pointName = "";
            String pointStyle = "";
            if(isLastFile){ //first file
                pointName = "End trip";
                pointStyle = "#sh_red-circle";
            }
            else{
                pointName = "End of part #" + iFileCount;
                pointStyle = "#icon28";
            }

            String footerTxt = "";
            if(dTotalUsedTrackPoints == 0){
                dCurrentLocationLongitude = 0;
                dCurrentLocationLatitude = 0;
                dCurrentLocationAltitude = 0;
                appendKMLStartPoint(); //for kml file consistency. coordinates is 0,0,0
            }
            
            footerTxt = footerTxt +
                "\n</coordinates>\n"
                + "</LineString>\n"
                + "</MultiGeometry>\n"
                + "</Placemark>\n"
                + "<Placemark>\n"
                  + "<name><![CDATA[" + pointName + "]]></name>\n";
            	if(isLastFile){
            		footerTxt = footerTxt
	                    + "<description>\n<![CDATA[End of trip '" + sName + "'<br>" + Utils.getDateStr(true, true, true);
            			try{
            				footerTxt = footerTxt + "\n<br>Start at: " + DateFormat.getDateFormat(getApplicationContext()).format(lStartTime * 1000) + " " + DateFormat.getTimeFormat(getApplicationContext()).format(lStartTime * 1000);
            			} catch(Exception e){};
            			try{
            				footerTxt = footerTxt + "\n<br>End at: " + DateFormat.getDateFormat(getApplicationContext()).format(lStopTime * 1000) + " " + DateFormat.getTimeFormat(getApplicationContext()).format(lStopTime * 1000);
            			} catch(Exception e){};
            			footerTxt = footerTxt + "\n<hr>";
            			try{
            				footerTxt = footerTxt + "\nDistance: " + (BigDecimal.valueOf(dTotalDistance).setScale(2, BigDecimal.ROUND_HALF_DOWN).toString()) + (isUseMetricUnits? " km" : " mi");
            			} catch(Exception e){};
            			try{
            				footerTxt = footerTxt + "\n<br>Max. speed: " + (BigDecimal.valueOf(dMaxSpeed).setScale(0, BigDecimal.ROUND_HALF_DOWN).toString()) + (isUseMetricUnits? " km/h" : " mi/h");
            			} catch(Exception e){};
            			try{
            				footerTxt = footerTxt + "\n<br>Avg. speed: " + (BigDecimal.valueOf(dAvgSpeed).setScale(0, BigDecimal.ROUND_HALF_DOWN).toString()) + (isUseMetricUnits? " km/h" : " mi/h");
            			} catch(Exception e){};
            			try{
            				footerTxt = footerTxt + "\n<br>Avg. moving speed: " + (BigDecimal.valueOf(dAvgMovingSpeed).setScale(0, BigDecimal.ROUND_HALF_DOWN).toString()) + (isUseMetricUnits? " km/h" : " mi/h");
            			} catch(Exception e){};
            			try{
            				footerTxt = footerTxt + "\n<br>Total time: " + Utils.getTimeString(lTotalTime, false);
            			} catch(Exception e){};
            			try{
            				footerTxt = footerTxt + "\n<br>Total moving time: " + Utils.getTimeString(lTotalMovingTime, false);
            			} catch(Exception e){};
            			try{
            				footerTxt = footerTxt + "\n<br>Pause: " + Utils.getTimeString(lTotalPauseTime, false);
            			} catch(Exception e){};
            			footerTxt = footerTxt + "\n<hr>" 
            			+ "\nRecorded with <a href='http://www.andicar.org'><b>AndiCar</b></a>";
            		
            	}
            	else{
            		footerTxt = footerTxt
                        + "\n<description><![CDATA[End of part " + iFileCount;
            	};
            	footerTxt = footerTxt 
                      + "]]>\n</description>\n"
                  + "<styleUrl>" + pointStyle + "</styleUrl>\n"
                  + "<Point>\n"
                    + "<coordinates>" + lastGoodLocationLongitude + "," + lastGoodLocationLatitude + "," +
                            lastGoodLocationAltitude + "</coordinates>\n"
                  + "</Point>\n"
                + "</Placemark>\n"
//            footerTxt = footerTxt
                + "</Document>\n"
                + "</kml>\n";
            gpsTrackDetailKMLFileWriter.append(footerTxt);

            if(isEnableDebugLog)
        		logDebugInfo("appendKMLFooter: Footer added", null);
            
        }
        catch(IOException e){
        	if(isEnableDebugLog)
        		logDebugInfo("appendKMLFooter: Exception = " + e.getMessage(), null);
        }
    }

    /**
     * Create a pause placemaker
     * @param pointType: PS - Pause Start; PE - Pause End; P - Pause (one point for Start and End)
     */
    private void appendKMLPausePoint(){
        if(gpsTrackDetailKMLFileWriter == null){
        	if(isEnableDebugLog)
        		logDebugInfo("appendKMLPausePoint: File writer is NULL!", null);
            return;
        }
        try{

            String kmlTxt = "";
            if(dDistanceBetweenLocations <= 10) // if the distance between the pause starting point and ending poin is less than 10m create a single pause point
            {
                kmlTxt = 
                	"\n</coordinates>\n"
	                + "</LineString>\n"
	                + "</MultiGeometry>\n"
	                + "</Placemark>\n"
	                
	                + "<Placemark>\n"
	                + "<name><![CDATA[Pause]]></name>\n"
	                + "<description>\n<![CDATA[Pause for " + Utils.getTimeString(lCurrentPauseTime / 1000,false)
		    				+ "\n<br>From: " + DateFormat.getDateFormat(getApplicationContext()).format(lCurrentPauseStartTime) + " " + DateFormat.getTimeFormat(getApplicationContext()).format(lCurrentPauseStartTime)
		    				+ "\n<br>To: " + DateFormat.getDateFormat(getApplicationContext()).format(lCurrentPauseEndTime) + " " + DateFormat.getTimeFormat(getApplicationContext()).format(lCurrentPauseEndTime)
	                + "]]>\n</description>\n"
	                + "<styleUrl>#icon29</styleUrl>\n"
	                + "<Point>\n"
	                    + "<coordinates>" + lastGoodLocationLongitude + "," + lastGoodLocationLatitude + "," +
	                            lastGoodLocationAltitude + "</coordinates>\n"
	                + "</Point>\n"
	                + "</Placemark>\n"
	                
		            + "<Placemark>\n"
		            + "<name><![CDATA[Track file #" + iFileCount + " (continuation)]]></name>\n"
		            + "<description><![CDATA[]]></description>\n"
		            + "<styleUrl>#track</styleUrl>\n"
		            + "<MultiGeometry>\n"
		            + "<LineString>\n"
		            + "<coordinates>\n";
	            
            	gpsTrackDetailKMLFileWriter.append(kmlTxt);
            }
            else{ //create two points for pause (Start and End)
                kmlTxt = 
                    	"\n</coordinates>\n"
    	                + "</LineString>\n"
    	                + "</MultiGeometry>\n"
    	                + "</Placemark>\n"
    	                
    	                + "<Placemark>\n"
    	                + "<name><![CDATA[Pause start]]></name>\n"
    	                + "<description>\n<![CDATA[Pause start at: " + DateFormat.getDateFormat(getApplicationContext()).format(lCurrentPauseStartTime) + " " + DateFormat.getTimeFormat(getApplicationContext()).format(lCurrentPauseStartTime)
    	                + "]]>\n</description>\n"
    	                + "<styleUrl>#icon29</styleUrl>\n"
    	                + "<Point>\n"
    	                    + "<coordinates>" + lastGoodLocationLongitude + "," + lastGoodLocationLatitude + "," + lastGoodLocationAltitude + "</coordinates>\n"
    	                + "</Point>\n"
    	                + "</Placemark>\n"
    	                
    	                + "<Placemark>\n"
    	                + "<name><![CDATA[Pause end]]></name>\n"
    	                + "<description>\n<![CDATA["
    	                + "\nPause duration: " + Utils.getTimeString(lCurrentPauseTime / 1000,false)
		    				+ "\n\n<br><br>From: " + DateFormat.getDateFormat(getApplicationContext()).format(lCurrentPauseStartTime) + " " + DateFormat.getTimeFormat(getApplicationContext()).format(lCurrentPauseStartTime)
		    				+ "\n<br>To: " + DateFormat.getDateFormat(getApplicationContext()).format(lCurrentPauseEndTime) + " " + DateFormat.getTimeFormat(getApplicationContext()).format(lCurrentPauseEndTime)
    	                + "]]>\n</description>\n"
    	                + "<styleUrl>#icon29</styleUrl>\n"
    	                + "<Point>\n"
    	                    + "<coordinates>" + dCurrentLocationLongitude + "," + dCurrentLocationLatitude + "," + dCurrentLocationAltitude + "</coordinates>\n"
    	                + "</Point>\n"
    	                + "</Placemark>\n"

    	                + "<Placemark>\n"
    		            + "<name><![CDATA[Track file #" + iFileCount + " (continuation)]]></name>\n"
    		            + "<description><![CDATA[]]></description>\n"
    		            + "<styleUrl>#track</styleUrl>\n"
    		            + "<MultiGeometry>\n"
    		            + "<LineString>\n"
    		            + "<coordinates>\n";
    	            
    	            	gpsTrackDetailKMLFileWriter.append(kmlTxt);
            }
            if(isEnableDebugLog)
        		logDebugInfo("appendKMLPausePoint: Point added", null);
            
        }
        catch(IOException e){
        	if(isEnableDebugLog)
        		logDebugInfo("appendKMLPausePoint: Exception = " + e.getMessage(), null);
        }
    }

    private void appendGPXFooter(){
        if(gpsTrackDetailGPXFileWriter == null){
        	if(isEnableDebugLog)
        		logDebugInfo("appendGPXFooter: File writer is NULL!", null);
            return;
        }
        try{
            gpsTrackDetailGPXFileWriter.append(
                "</trkseg>\n"
                + "</trk>\n"
                + "</gpx>"
            );

            if(isEnableDebugLog)
        		logDebugInfo("appendGPXFooter: Footer added", null);
        }
        catch(IOException e){
        	if(isEnableDebugLog)
        		logDebugInfo("appendGPXFooter: Exception = " + e.getMessage(), null);
        }
    }

    private void appendCSVTrackPoint(boolean isValid) throws IOException {
/*
    MainDbAdapter.GPSTRACKDETAIL_COL_GPSTRACK_ID_NAME + "," +
    "Accuracy" + (isUseMetricUnits? " [m]" : " [yd]") + "," +
    "Accuracy [m], " + //debug
    "Altitude" + (isUseMetricUnits? " [m]" : " [yd]") + "," +
    "Altitude [m]," + //debug
    "Latitude" + "," +
    "Longitude" + "," +
    "Speed" + (isUseMetricUnits? " [km/h]" : " [mi/h]") + "," +
    "Speed [m/s]," + //debug
    "Time" + "," +
    "Distance" + (isUseMetricUnits? " [m]" : " [yd]") + "," +
    "Distance [m]," + //debug
    "Bearing" + "," +
    "TotalTrackPointCount" + "," +
    "InvalidTrackPointCount" + "," +
    "IsValidPoint" + "\n");
 */
        gpsTrackDetailCSVFileWriter.append(
                gpsTrackId + "," +
                (isUseMetricUnits ? dCurrentAccuracy : dCurrentAccuracy * 1.093613)  + "," + //m to yd
//                dCurrentAccuracy + "," + //debug
                (isUseMetricUnits ? dCurrentLocationAltitude : dCurrentLocationAltitude * 1.093613) + "," + //m to yd
//                dCurrentLocationAltitude + "," + //debug
                dCurrentLocationLatitude + "," + 
                dCurrentLocationLongitude + "," + 
                (isUseMetricUnits ? dCurrentSpeed * 3.6 : dCurrentSpeed * 2.2369) + "," + //m/s to km/h or mi/h
//                dCurrentSpeed + "," + //debug
                lCurrentLocationTime + "," +
                (isUseMetricUnits ? dDistanceBetweenLocations : dDistanceBetweenLocations * 1.093613) + "," +
//                dDistanceBetweenLocations + "," + //debug
                dCurrentLocationBearing + "," +
                dTotalTrackPoints + "," +
                dTotalSkippedTrackPoints + "," +
                (isValid ? "Yes" : "No") + "\n");

        csvPointsCount++;
        if(csvPointsCount == 20){
        	gpsTrackDetailCSVFileWriter.flush();
        	csvPointsCount = 0;
        }
        
    }

    private void appendGOPTrackPoint(String pointType) throws IOException {

        gpsTrackDetailGOPFileWriter.append(
                (int)(dCurrentLocationLatitude * 1E6) + "," +
                (int)(dCurrentLocationLongitude * 1E6) + "," + 
                		pointType + "\n");

        gopPointsCount++;
        if(gopPointsCount == 20){
        	gpsTrackDetailGOPFileWriter.flush();
        	gopPointsCount = 0;
        }
    }

    private void appendGPXTrackPoint() throws IOException {
    	double tmpSpeed;
    	BigDecimal speed;
        if(!isUseMetricUnits){
            tmpSpeed = dCurrentSpeed * 2.23693; //m/s to mi/h
        }
        else{ // only m/s need to be converted to km/h
            tmpSpeed = dCurrentSpeed * 3.6; //m/s to mi/h
        }
        
        try{
        	speed = BigDecimal.valueOf(tmpSpeed).setScale(1, BigDecimal.ROUND_HALF_DOWN);
        }
        catch(NumberFormatException e)
        {
        	return;
        }
        
        currentLocationDateTime.setTimeInMillis(lCurrentLocationTime);
        currentLocationDateTimeGPXStr =
                currentLocationDateTime.get(Calendar.YEAR) + "-" +
                Utils.pad(currentLocationDateTime.get(Calendar.MONTH) + 1, 2) + "-" +
                Utils.pad(currentLocationDateTime.get(Calendar.DAY_OF_MONTH), 2) + "T" +
                Utils.pad(currentLocationDateTime.get(Calendar.HOUR_OF_DAY), 2) + ":" +
                Utils.pad(currentLocationDateTime.get(Calendar.MINUTE), 2) + ":" +
                Utils.pad(currentLocationDateTime.get(Calendar.SECOND), 2) + "Z";
        gpsTrackDetailGPXFileWriter.append(
                "<trkpt lat=\"" + dCurrentLocationLatitude +
                    "\" lon=\"" + dCurrentLocationLongitude +
                    "\">\n" + "<ele>" + dCurrentLocationAltitude + "</ele>\n" +
                    "<time>" + currentLocationDateTimeGPXStr + "</time>\n" +
                    "<cmt>Current speed: " + speed.toPlainString() + (isUseMetricUnits? " km/h" : " mi/h") + "</cmt>\n" +
                "</trkpt>\n");

        gpxPointsCount++;
        if(gpxPointsCount == 20){
        	gpsTrackDetailGPXFileWriter.flush();
        	gpxPointsCount = 0;
        }
        
    }

    private void appendKMLTrackPoint() throws IOException {
        gpsTrackDetailKMLFileWriter.append(
                dCurrentLocationLongitude + "," +
                dCurrentLocationLatitude + "," +
                dCurrentLocationAltitude + " \n");

        kmlPointsCount++;
        if(kmlPointsCount == 20){
        	gpsTrackDetailKMLFileWriter.flush();
        	kmlPointsCount = 0;
        }
    }

    private void createFiles() throws IOException {
    	
    	if(isEnableDebugLog)
    		logDebugInfo("createFiles() started", null);

    	String fileName = gpsTrackId + "_" + Utils.pad(iFileCount, 3);
        if(mDbAdapter == null)
            mDbAdapter = new MainDbAdapter(this);

        createCSVFile(fileName);
        createGOPFile(fileName);
        if(isUseKML) {
            createKMLFile(fileName);
        }
        else {
            gpsTrackDetailKMLFile = null;
        }
        if(isUseGPX) {
            createGPXFile(fileName);
        }
        else {
            gpsTrackDetailGPXFile = null;
        }

        if(mDbAdapter != null){
            mDbAdapter.close();
            mDbAdapter = null;
        }

        if(isEnableDebugLog)
    		logDebugInfo("createFiles() terminated", null);
    }

    private void closeFiles(boolean isLastFile) {
        try {
            if(isEnableDebugLog)
        		logDebugInfo("closeFiles started", null);

            if(gpsTrackDetailCSVFileWriter != null) {
                gpsTrackDetailCSVFileWriter.flush();
                gpsTrackDetailCSVFileWriter.close();
            }
            if(gpsTrackDetailGOPFileWriter != null) {
                gpsTrackDetailGOPFileWriter.flush();
                gpsTrackDetailGOPFileWriter.close();
            }
            if(gpsTrackDetailKMLFileWriter != null) {
                appendKMLFooter(isLastFile);
                gpsTrackDetailKMLFileWriter.flush();
                gpsTrackDetailKMLFileWriter.close();
            }
            if(gpsTrackDetailGPXFileWriter != null) {
                appendGPXFooter();
                gpsTrackDetailGPXFileWriter.flush();
                gpsTrackDetailGPXFileWriter.close();
            }
            if(isEnableDebugLog)
        		logDebugInfo("closeFiles terminated", null);
        }
        catch(IOException ex) {
            if(isEnableDebugLog)
        		logDebugInfo("closeFiles exception: " + ex.getMessage(), null);
            Logger.getLogger(GPSTrackService.class.getName()).log(Level.SEVERE, null, ex);
            showNotification(StaticValues.NOTIF_FILESYSTEM_ERROR_ID, true);
        }
    }

    @Override
    public void onDestroy() {
        // Cancel the persistent notifications.
        if(isEnableDebugLog)
    		logDebugInfo("onDestroy() started", null);
        
        mLocationManager.removeUpdates(mLocationListener);
        mLocationManager = null;
        
//        mNM.cancelAll(); //R.string.GPSTrackService_TrackInProgressMessage);
        stopForeground(true);
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean("isGpsTrackOn", false);
        editor.commit();
        lCurrentPauseStartTime = 0;
        lCurrentPauseEndTime = 0;
        //update the statistics for the track
        updateStatistics();
        //close the database
        if(mDbAdapter != null){
            mDbAdapter.close();
            mDbAdapter = null;
        }
        closeFiles(true);
        
        if(mPreferences.getBoolean("GPSTrackCreateMileage", true) && !isErrorStop){
            Intent mileageInsertIntent = new Intent(GPSTrackService.this, MileageEditActivity.class);
            mileageInsertIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mileageInsertIntent.putExtra("Operation", "TrackToMileage");
            mileageInsertIntent.putExtra("Track_ID", gpsTrackId);
            mileageInsertIntent.putExtra("Tag", sTag);
            startActivity(mileageInsertIntent);
        }
        if(isEnableDebugLog)
    		logDebugInfo("onDestroy() terminated", null);
    }

    /**
     * Show a notification while this service is running.
     */
    private void showNotification(int what, boolean showToast) {
        String message;
        CharSequence title;
        Notification notification = null;
        PendingIntent contentIntent;
        contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, GPSTrackController.class), 0);

        if(what == StaticValues.NOTIF_GPS_TRACKING_STARTED_ID){
            title = getText(R.string.GPSTrackService_TrackInProgressTitle);
            message = getString(R.string.GPSTrackService_TrackInProgressMessage);

            notification = new Notification(R.drawable.andicar_gps_anim, message,
                    System.currentTimeMillis());
            notification.flags |= Notification.FLAG_NO_CLEAR;

            notification.setLatestEventInfo(this, title, message, contentIntent); 
        }
        else if(what == StaticValues.NOTIF_GPS_ACCURACY_WARNING_ID){
            title = getText(R.string.GPSTrackService_AccuracyProblemTitle);
            message = getString(R.string.GPSTrackService_AccuracyProblemMessage);

            notification = new Notification(R.drawable.icon_sys_warning, message,
                    System.currentTimeMillis());
            notification.flags |= Notification.DEFAULT_LIGHTS;
            notification.flags |= Notification.DEFAULT_SOUND;

            notification.setLatestEventInfo(this, title, message, contentIntent);
            if(showToast)
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
        else if(what == StaticValues.NOTIF_FILESYSTEM_ERROR_ID){
            title = getText(R.string.GPSTrackService_FileSystemErrorTitle);
            message = getString(R.string.ERR_034);

            notification = new Notification(R.drawable.icon_sys_error, message,
                    System.currentTimeMillis());

            notification.setLatestEventInfo(this, title, message, contentIntent);
            if(showToast)
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
        else if(what == StaticValues.NOTIF_GPS_ACCURACY_SHUTDOWN_ID){
            try{
                title = getText(R.string.GPSTrackService_AutoShutDownTitle);
                message = getString(R.string.GPSTrackService_AutoShutDownMessage);
                BigDecimal bdSkippedPointPercentage = new BigDecimal(skippedPointPercentage).setScale(0, BigDecimal.ROUND_HALF_UP);
                message = message.replace("[#1]",  bdSkippedPointPercentage.toString() + "%").
                        replace("[#2]", iMaxAccuracyShutdownLimit + "%");

                notification = new Notification(R.drawable.icon_sys_error, message,
                        System.currentTimeMillis());
                notification.flags |= Notification.DEFAULT_LIGHTS;
                notification.flags |= Notification.DEFAULT_SOUND;

                notification.setLatestEventInfo(this, title, message, contentIntent);
                if(showToast)
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
            catch(NumberFormatException e){}
        }
        else if(what == StaticValues.NOTIF_GPS_DISABLED_ID){
            title = getText(R.string.GPSTrackService_AutoShutDownTitle);
            message = getString(R.string.GPSTrackService_GPSDisabledMessage);
            notification = new Notification(R.drawable.icon_sys_error, message,
                    System.currentTimeMillis());
            notification.flags |= Notification.DEFAULT_LIGHTS;
            notification.flags |= Notification.DEFAULT_SOUND;

            notification.setLatestEventInfo(this, title, message, contentIntent);
            if(showToast)
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
        else if(what == StaticValues.NOTIF_GPS_OUTOFSERVICE_ID){
            title = getText(R.string.GPSTrackService_AutoShutDownTitle);
            message = getString(R.string.GPSTrackService_GPSOutOfService);
            notification = new Notification(R.drawable.icon_sys_error, message,
                    System.currentTimeMillis());
            notification.flags |= Notification.DEFAULT_LIGHTS;
            notification.flags |= Notification.DEFAULT_SOUND;

            notification.setLatestEventInfo(this, title, message, contentIntent);
            if(showToast)
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
        else if(what == StaticValues.NOTIF_GPS_PAUSED_ID){
            title = getText(R.string.GPSTrackService_TrackPausedTitle);
            message = getString(R.string.GPSTrackService_TrackPausedMessage);
            notification = new Notification(R.drawable.andicar_gps_paused, message,
                    System.currentTimeMillis());
            notification.flags |= Notification.DEFAULT_LIGHTS;
            notification.flags |= Notification.DEFAULT_SOUND;

            notification.setLatestEventInfo(this, title, message, contentIntent);
            if(showToast)
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }

//        mNM.notify(what, notification);
        startForeground(what, notification);
    }

    private void updateStatistics(){

    	if(isEnableDebugLog)
    		logDebugInfo("updateStatistics() started", null);
        
    	lStartTime = lStartTime / 1000; //convert to second
    	lStopTime = lStopTime / 1000; //convert to second
    	lTotalTime = lStopTime - lStartTime;
        
        if(lLastNonMovingTime != 0 && lFirstNonMovingTime != 0)
            lTotalNonMovingTime = lTotalNonMovingTime + (lLastNonMovingTime - lFirstNonMovingTime);
        
        if(lCurrentPauseStartTime > 0)
        	lTotalPauseTime = lTotalPauseTime + (lCurrentLocationTime - lCurrentPauseStartTime);
        
        lTotalNonMovingTime = lTotalNonMovingTime / 1000; //convert to second
        lTotalPauseTime = lTotalPauseTime / 1000; //convert to second
        
        lTotalMovingTime = lTotalTime - lTotalPauseTime - lTotalNonMovingTime; 

        if(dTotalUsedTrackPoints != 0)
            //at this moment dAvgAccuracy = SUM(CurrentAccuracy)
            dAvgAccuracy = dAvgAccuracy / dTotalUsedTrackPoints;
        else
            dAvgAccuracy = 0;

        if(lStopTime - lStartTime != 0)
            dAvgSpeed = dTotalDistance / (lTotalTime - lTotalPauseTime); // m/s
        else
            dAvgSpeed = 0;

        if(lTotalMovingTime != 0)
            dAvgMovingSpeed = dTotalDistance / lTotalMovingTime; // m/s
        else
            dAvgMovingSpeed = 0;
        if(!isUseMetricUnits){
            dMinAccuracy = dMinAccuracy * 1.093613; //m to yd
            dMaxAccuracy = dMaxAccuracy * 1.093613; //m to yd
            dAvgAccuracy = dAvgAccuracy * 1.093613; //m to yd
            dMinAltitude = dMinAltitude * 1.093613; //m to yd
            dMaxAltitude = dMaxAltitude * 1.093613; //m to yd
            dTotalDistance = dTotalDistance * 0.000621371; //m to mi
            dAvgSpeed = dAvgSpeed * 2.23693; //m/s to mi/h
            dAvgMovingSpeed = dAvgMovingSpeed * 2.23693; //m/s to mi/h
            dMaxSpeed = dMaxSpeed  * 2.23693;
        }
        else{ // only m/s need to be converted to km/h
            dTotalDistance = dTotalDistance * 0.001; //m to km
            dAvgSpeed = dAvgSpeed * 3.6; //m/s to km/h
            dAvgMovingSpeed = dAvgMovingSpeed * 3.6; //m/s to km/h
            dMaxSpeed = dMaxSpeed  * 3.6;
        }

        ContentValues cvData = new ContentValues();
        cvData.put( MainDbAdapter.COL_NAME_GPSTRACK__MINACCURACY, (Math.round(dMinAccuracy * 100)*1d)/100); //round to 2 decimals
        cvData.put( MainDbAdapter.COL_NAME_GPSTRACK__MAXACCURACY, (Math.round(dMaxAccuracy * 100)*1d)/100);
        cvData.put( MainDbAdapter.COL_NAME_GPSTRACK__AVGACCURACY, (Math.round(dAvgAccuracy * 100)*1d)/100);
        cvData.put( MainDbAdapter.COL_NAME_GPSTRACK__MINALTITUDE, (Math.round(dMinAltitude * 100)*1d)/100);
        cvData.put( MainDbAdapter.COL_NAME_GPSTRACK__MAXALTITUDE, (Math.round(dMaxAltitude * 100)*1d)/100);
        cvData.put( MainDbAdapter.COL_NAME_GPSTRACK__TOTALTIME, lTotalTime);
        cvData.put( MainDbAdapter.COL_NAME_GPSTRACK__MOVINGTIME, lTotalMovingTime);
        cvData.put( MainDbAdapter.COL_NAME_GPSTRACK__DISTANCE, (Math.round(dTotalDistance * 100)*1d)/100);
        cvData.put( MainDbAdapter.COL_NAME_GPSTRACK__AVGSPEED, (Math.round(dAvgSpeed * 100)*1d)/100);
        cvData.put( MainDbAdapter.COL_NAME_GPSTRACK__AVGMOVINGSPEED, (Math.round(dAvgMovingSpeed * 100)*1d)/100);
        cvData.put( MainDbAdapter.COL_NAME_GPSTRACK__MAXSPEED, (Math.round(dMaxSpeed * 100)*1d)/100);
        cvData.put( MainDbAdapter.COL_NAME_GPSTRACK__TOTALTRACKPOINTS, dTotalTrackPoints);
        cvData.put( MainDbAdapter.COL_NAME_GPSTRACK__INVALIDTRACKPOINTS, dTotalSkippedTrackPoints);
        cvData.put( MainDbAdapter.COL_NAME_GPSTRACK__TOTALPAUSETIME, lTotalPauseTime);
        
        if(mDbAdapter == null)
            mDbAdapter = new MainDbAdapter(this);
        mDbAdapter.updateRecord(MainDbAdapter.TABLE_NAME_GPSTRACK, gpsTrackId, cvData);
        
    	if(isEnableDebugLog)
    		logDebugInfo("updateStatistics() terminated", null);
    }

    private class AndiCarLocationListener implements LocationListener
    {
        @Override
        public void onLocationChanged(Location loc) {
        	
            boolean isValid = true;
            double gopDistance = 0;
            
            if(gpsTrackDetailCSVFileWriter == null){
            	if(isEnableDebugLog)
            		logDebugInfo("onLocationChanged: Error: gpsTrackDetailCSVFileWriter == null", null);
            	
                Toast.makeText(GPSTrackService.this, "No File Writer!", Toast.LENGTH_LONG).show();
                isErrorStop = true;
                stopSelf();
                return;
            }
//            Log.w("GPSTrackServuce", "onLocationChanged: dTotalTrackPoints = " + dTotalTrackPoints);

            if (loc == null){
            	if(isEnableDebugLog)
            		logDebugInfo("onLocationChanged: Error: loc == null", null);
            	return;
            }

            try {
                dTotalTrackPoints++;
                dCurrentLocationLatitude = loc.getLatitude();
                dCurrentLocationLongitude = loc.getLongitude();
                dCurrentLocationAltitude = loc.getAltitude();
                lCurrentLocationTime = loc.getTime();
                dCurrentAccuracy = loc.getAccuracy();
                dCurrentLocationBearing = loc.getBearing();

                if(isFirstPoint && iFileCount == 1){
                    lStartTime = lCurrentLocationTime;
                	lOldLocationTime = lCurrentLocationTime;
                	dCurrentSpeed = 0;
                }

                if(dCurrentAccuracy > iMaxAccuracy){
                    isValid = false;
                    //
                    if(lCurrentLocationTime - lStartTime > 60000){ //leave time for GPS initialization (1 min)
                        dTotalSkippedTrackPoints++;
                    }
                    
                    skippedPointPercentage = (dTotalSkippedTrackPoints / dTotalTrackPoints) * 100;
                }

                if(isValid){
                    if(isFirstPoint){
                        //the first valid location => write the starting point
                        if(gpsTrackDetailKMLFileWriter != null 
                        		&& iFileCount == 1){ //this is the first track file (multiple track file can be used)
                            appendKMLStartPoint();
                        }
                        appendGOPTrackPoint("SP"); //Start Point 
                        isFirstPoint = false;
                    }
                    else{
                    	//get the distance between the current and previous location
                        Location.distanceBetween(dOldLocationLatitude, dOldLocationLongitude,
                                dCurrentLocationLatitude, dCurrentLocationLongitude, fDistanceArray);
                        
                        dDistanceBetweenLocations = fDistanceArray[0];
                        
                    	if(!isFirstPointAfterResume){
	                        if(lCurrentLocationTime - lOldLocationTime > 0)
	                        	dCurrentSpeed = dDistanceBetweenLocations / ((lCurrentLocationTime - lOldLocationTime) / 1000);
	                        else
	                        	dCurrentSpeed = 0;
                        
	                    	//check acceleration. 
	                        //if too big (wrong data from the gps sensor) ignore the current location (see issue #32)
	                    	if((lCurrentLocationTime - lOldLocationTime) / 1000 != 0){
		                    	double acceleration = (dCurrentSpeed - dOldSpeed)/((lCurrentLocationTime - lOldLocationTime) / 1000);
		                    	
		                    	if(Math.abs(acceleration) > 13){ //a > 13 m/s2  (0 to 100 km/h in less than 2 second) => probably wrong sensor data or wrong vehicle type (rocket or Dragster :-) )
		                            isValid = false;
		                            dTotalSkippedTrackPoints++;
		                    	}
		                    	else{
			                        isValid = true;
			                        dTotalUsedTrackPoints++;
			                    	dOldSpeed = dCurrentSpeed;
		                    	}
	                    	}
                    	}
                    	else{
                            appendGOPTrackPoint("PEP"); //Pause End Point
	                    	dCurrentSpeed = 0;
	                    	lCurrentPauseEndTime = lCurrentLocationTime;
	                	}
                    }
                }

                appendCSVTrackPoint(isValid);

                if(!isValid)
                    return;

                //for drawing on the map add only a minimum 5 m distanced point from the previous point - performance reason
                gopDistance = gopDistance + dDistanceBetweenLocations;
                if(gopDistance >= 5){
                    appendGOPTrackPoint("NP"); //Normal Point
                    gopDistance = 0;
                }

                //statistics
            	if(!isFirstPointAfterResume){
                    //non moving time
	                if(dCurrentSpeed == 0){
	                    if(lFirstNonMovingTime == 0)
	                        lFirstNonMovingTime = lCurrentLocationTime;
	                    lLastNonMovingTime = lCurrentLocationTime;
	                }
	                else{ //currentSpeed > 0
	                    if(lFirstNonMovingTime != 0){
	                        lTotalNonMovingTime = lTotalNonMovingTime + (lLastNonMovingTime - lFirstNonMovingTime);
	//                            sNonMovingTimes = sNonMovingTimes + "" +
	//                                        lFirstNonMovingTime + "," + lLastNonMovingTime + "\n";
	                        //reset
	                        lLastNonMovingTime = 0;
	                        lFirstNonMovingTime = 0;
	                    }
	                }
            	}
                else{
                	lCurrentPauseTime = lCurrentPauseEndTime - lCurrentPauseStartTime;
                    lTotalPauseTime = lTotalPauseTime + lCurrentPauseTime;
                    
                    if(gpsTrackDetailKMLFileWriter != null)
                        appendKMLPausePoint();
                    
                    lCurrentPauseStartTime = 0;
                    lCurrentPauseEndTime = 0;
                    isFirstPointAfterResume = false;
                }

                if(dCurrentAccuracy < dMinAccuracy)
                    dMinAccuracy = dCurrentAccuracy;
                if(dCurrentAccuracy > dMaxAccuracy)
                    dMaxAccuracy = dCurrentAccuracy;

                //at the end of the tracking fAvgAccuracy will be fAvgAccuracy / iTrackPointCount
                dAvgAccuracy = dAvgAccuracy + dCurrentAccuracy;

                if(dCurrentLocationAltitude < dMinAltitude)
                    dMinAltitude = dCurrentLocationAltitude;
                if(dCurrentLocationAltitude > dMaxAltitude)
                    dMaxAltitude = dCurrentLocationAltitude;

                if(dCurrentSpeed > dMaxSpeed){
            		dMaxSpeed = dCurrentSpeed;
                }

                if(gpsTrackDetailKMLFileWriter != null && dDistanceBetweenLocations != 0)
                    appendKMLTrackPoint();

                if(gpsTrackDetailGPXFileWriter != null  && dDistanceBetweenLocations != 0){
                    appendGPXTrackPoint();
                }

                dOldLocationLatitude = dCurrentLocationLatitude;
                dOldLocationLongitude = dCurrentLocationLongitude;
                lastGoodLocationLatitude = dCurrentLocationLatitude;
                lastGoodLocationLongitude = dCurrentLocationLongitude;
                lastGoodLocationAltitude = dCurrentLocationAltitude;
            	lOldLocationTime = lCurrentLocationTime;
                dTotalDistance = dTotalDistance + dDistanceBetweenLocations;
                //set the stop time on each location change => the last will be the final lTotalTimeStop
                lStopTime = lCurrentLocationTime;


//                    Log.w("GPSTrackServuce", "onLocationChanged: iFileSplitCount = " + iFileSplitCount + ", iFileCount = " + iFileCount);

                //split the track files
                if(iFileSplitCount > 0){
                    if(dTotalTrackPoints >= (iFileCount * iFileSplitCount)){
                        closeFiles(false);
                        iFileCount++;
                        isFirstPoint = true;
                        createFiles();
                        appendKMLStartPoint();
                        appendKMLTrackPoint();
                    }
                }
            }
            catch(IOException ex) {
            	if(isEnableDebugLog)
            		logDebugInfo("onLocationChanged: Exception: " + ex.getMessage(), null);
            	
                Logger.getLogger(GPSTrackService.class.getName()).log(Level.SEVERE, null, ex);
                Toast.makeText(GPSTrackService.this, "File error!\n" + ex.getMessage(), Toast.LENGTH_LONG).show();
                isErrorStop = true;
                stopSelf();
            }
            
        }

        @Override
        public void onProviderDisabled(String provider) {
        	if(lCurrentPauseStartTime > 0) //tracking in pause
        		return;
        	
            if(provider.equals(LocationManager.GPS_PROVIDER)){
                showNotification(StaticValues.NOTIF_GPS_DISABLED_ID, false);
//                stopSelf();
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
        	if(lCurrentPauseStartTime > 0) //tracking in pause
        		return;
        	showNotification(StaticValues.NOTIF_GPS_TRACKING_STARTED_ID, false);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            if(provider.equals(LocationManager.GPS_PROVIDER)){
                if(status == LocationProvider.OUT_OF_SERVICE)
                	showNotification(StaticValues.NOTIF_GPS_OUTOFSERVICE_ID, false);
                if(status == LocationProvider.AVAILABLE)
                	showNotification(StaticValues.NOTIF_GPS_TRACKING_STARTED_ID, false);
            }
        }
    }

    private void logDebugInfo(String msg, Throwable t){
    	try{
	        debugLogFile = new File(StaticValues.TEMP_FOLDER + "gpsDBG" + System.currentTimeMillis() + ".log");
	        debugLogFileWriter = new FileWriter(debugLogFile);
	        msg = Utils.getDateStr(true, true, true) + " -> " + msg;
	        debugLogFileWriter.append(msg + "\n");
	        if(t != null){
	        	StackTraceElement[] e = t.getStackTrace();
	        	for (int i = 0; i < e.length; i++) {
					StackTraceElement stackTraceElement = e[i];
					debugLogFileWriter.append(stackTraceElement.getClassName() + "." + stackTraceElement.getMethodName() + ": " + stackTraceElement.getLineNumber() + "\n");
				}
	        }
	        debugLogFileWriter.flush();
	        debugLogFileWriter.close();
    	}
    	catch(IOException e)
    	{};
    }

    /**
     * Handler of incoming messages from controller.
     */
    class IncomingMessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case StaticValues.MSG_GPS_TRACK_SERVICE_PAUSE:
                    mLocationManager.removeUpdates(mLocationListener);
                    showNotification(StaticValues.NOTIF_GPS_PAUSED_ID, false);
                    try{
                    	appendGOPTrackPoint("PSP"); //Pause Start Point
                    }catch(IOException e){}
                    lCurrentPauseStartTime = lCurrentLocationTime;
                    if(lFirstNonMovingTime != 0){
                        lTotalNonMovingTime = lTotalNonMovingTime + (lLastNonMovingTime - lFirstNonMovingTime);
                        //reset
                        lLastNonMovingTime = 0;
                        lFirstNonMovingTime = 0;
                    }
                    break;
                case StaticValues.MSG_GPS_TRACK_SERVICE_RESUME:
                    isFirstPointAfterResume = true;
                    mLocationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER, Long.parseLong(mPreferences.getString("GPSTrackMinTime", "0")), 0, mLocationListener);

                    if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                        showNotification(StaticValues.NOTIF_GPS_DISABLED_ID, false);
                    else
                    	showNotification(StaticValues.NOTIF_GPS_TRACKING_STARTED_ID, false);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingMessageHandler());

    /**
     * When binding to the service, we return an interface to our messenger
     * for sending messages to the service.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }
    
}
