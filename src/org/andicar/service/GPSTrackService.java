/*
    Copyright (C) 2009-2010 Miklos Keresztes - miklos.keresztes@gmail.com

    This program is free software; you can redistribute it and/or modify it
    under the terms of the GNU General Public License as published by the
    Free Software Foundation; either version 2 of the License.

    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
    without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
    See the GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along with this program;
    if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.andicar.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.Location;
import android.location.LocationProvider;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.andicar.activity.miscellaneous.GPSTrackController;
import org.andicar.activity.R;
import org.andicar.persistence.FileUtils;
import org.andicar.persistence.MainDbAdapter;
import org.andicar.utils.StaticValues;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import org.andicar.activity.MileageEditActivity;
import org.andicar.utils.AndiCarExceptionHandler;
import org.andicar.utils.AndiCarStatistics;
import org.andicar.utils.Utils;

/**
 *
 * @author miki
 */
public class GPSTrackService extends Service {

    private NotificationManager mNM;
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
//    private double dOldSpeed = 0;
    private long lCurrentLocationTime = 0;
    private long lOldLocationTime = 0;
    private Calendar currentLocationDateTime = Calendar.getInstance();
    private String currentLocationDateTimeGPXStr = "";
    private float[] fDistanceArray = new float[1];
    private double dDistanceBetweenLocations = 0;
    private long gpsTrackId = 0;
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
    private long lCarId = -1;
    private long lDriverId = -1;
    private boolean isUseKML = false;
    private boolean isUseGPX = false;
//    private boolean isShowOnMap = false;
    //statistics
    private double dMinAccuracy = 9999;
    private double dAvgAccuracy = 0;
    private double dMaxAccuracy = 0;
    private double dMinAltitude = 99999;
    private double dMaxAltitude = 0;
    private long lStartTime = 0;
    private long lStopTime = 0;
    private double dDistance = 0;
    private double dMaxSpeed = 0;
    private double dAvgSpeed = 0;
    private double dAvgMovingSpeed = 0;
    private boolean isFirstPoint = true;
    private long lFirstNonMovingTime = 0;
    private long lLastNonMovingTime = 0;
    private long lTotalNonMovingTime = 0;
    private int iMaxAccuracy = 9999999;
    private int iMaxAccuracyShutdownLimit = 30;
    private double dTotalTrackPoints = 0;
    private double dTotalSkippedTrackPoints = 0;
    private double dTotalUsedTrackPoints = 0;
    private double dTmpSkippedTrackPoints = 0;
    private boolean bNotificationShowed = false;
    private double skippedPointPercentage = 0;
    private boolean isSendCrashReport;
    private int iFileCount = 1; //for splitting large gps files
    private int iFileSplitCount = 0;
    private String sNonMovingTimes = "\n";
    private boolean isUseMetricUnits = true;
    private boolean isErrorStop = false;



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
        mPreferences = getSharedPreferences(StaticValues.GLOBAL_PREFERENCE_NAME, 0);
        mResource = getResources();
        isSendCrashReport = mPreferences.getBoolean("SendCrashReport", true);
        if(isSendCrashReport)
            Thread.setDefaultUncaughtExceptionHandler(
                    new AndiCarExceptionHandler(Thread.getDefaultUncaughtExceptionHandler(), this));
        
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
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
        lCarId = mPreferences.getLong("GPSTrackTmp_CarId", mPreferences.getLong("CurrentCar_ID", 0));
        lDriverId = mPreferences.getLong("GPSTrackTmp_DriverId", mPreferences.getLong("CurrentDriver_ID", 0));
        isUseKML = mPreferences.getBoolean("GPSTrackTmp_IsUseKML", false);
        isUseGPX = mPreferences.getBoolean("GPSTrackTmp_IsUseGPX", false);
        iMaxAccuracy = Integer.parseInt(mPreferences.getString("GPSTrackMaxAccuracy", "20"));
        iMaxAccuracyShutdownLimit = Integer.parseInt(mPreferences.getString("GPSTrackMaxAccuracyShutdownLimit", "30"));

        iFileSplitCount = Integer.parseInt(mPreferences.getString("GPSTrackTrackFileSplitCount", "0"));

        //create the master record
        //use direct table insert for increasing the speed of the DB operation
        ContentValues cvData = new ContentValues();
        cvData.put(MainDbAdapter.GEN_COL_NAME_NAME, sName);
        cvData.put(MainDbAdapter.GEN_COL_USER_COMMENT_NAME, sUserComment);
        cvData.put(MainDbAdapter.GPSTRACK_COL_CAR_ID_NAME, lCarId);
        cvData.put(MainDbAdapter.GPSTRACK_COL_DRIVER_ID_NAME, lDriverId);
        cvData.put(MainDbAdapter.GPSTRACK_COL_DATE_NAME, (System.currentTimeMillis() / 1000));
        if(sTag != null && sTag.length() > 0){
        	long mTagId = 0;
            String selection = "UPPER (" + MainDbAdapter.GEN_COL_NAME_NAME + ") = ?";
            String[] selectionArgs = {sTag.toUpperCase()};
            Cursor c = mDbAdapter.query(MainDbAdapter.TAG_TABLE_NAME, MainDbAdapter.genColName, selection, selectionArgs,
                    null, null, null);
            String tagIdStr = null;
            if(c.moveToFirst())
                tagIdStr = c.getString(MainDbAdapter.GEN_COL_ROWID_POS);
            c.close();
            if(tagIdStr != null && tagIdStr.length() > 0){
                mTagId = Long.parseLong(tagIdStr);
                cvData.put(MainDbAdapter.GPSTRACK_COL_TAG_ID_NAME, mTagId);
            }
            else{
                ContentValues tmpData = new ContentValues();
                tmpData.put(MainDbAdapter.GEN_COL_NAME_NAME, sTag);
                mTagId = mDbAdapter.createRecord(MainDbAdapter.TAG_TABLE_NAME, tmpData);
                if(mTagId >= 0)
                	cvData.put(MainDbAdapter.GPSTRACK_COL_TAG_ID_NAME, mTagId);
            }
        }
        else
        	cvData.put(MainDbAdapter.GPSTRACK_COL_TAG_ID_NAME, (String)null);

        gpsTrackId = mDbAdapter.createRecord(MainDbAdapter.GPSTRACK_TABLE_NAME, cvData);

//        String sqlStr;
//        = "INSERT INTO " + MainDbAdapter.GPSTRACK_TABLE_NAME
//                    + " ( "
//                        + MainDbAdapter.GEN_COL_NAME_NAME + ", "
//                        + MainDbAdapter.GEN_COL_USER_COMMENT_NAME + ", "
//                        + MainDbAdapter.GPSTRACK_COL_CAR_ID_NAME + ", "
//                        + MainDbAdapter.GPSTRACK_COL_DRIVER_ID_NAME + ", "
////                        + MainDbAdapter.GPSTRACK_COL_MILEAGE_ID_NAME + ", "
//                        + MainDbAdapter.GPSTRACK_COL_DATE_NAME + " "
//                    + " ) "
//                    + " VALUES ( "
//                        + " '" + sName + "', "
//                        + " '" + sUserComment + "', "
//                        + lCarId + ", "
//                        + lDriverId + ", "
//                        + (System.currentTimeMillis() / 1000) + ") ";
//
//        mDbAdapter.execSql(sqlStr);

//        sqlStr = "SELECT MAX(" + MainDbAdapter.GEN_COL_ROWID_NAME + ") FROM " + MainDbAdapter.GPSTRACK_TABLE_NAME;
//        Cursor c = mDbAdapter.execSelectSql(sqlStr);
//        c.moveToNext();
//        gpsTrackId = c.getLong(0);
//        c.close();
        long lMileId = mDbAdapter.getIdFromCode(MainDbAdapter.UOM_TABLE_NAME, "mi");
        long lCarUomId = mDbAdapter.getCarUOMLengthID(lCarId);
        if(lCarUomId == lMileId)
            isUseMetricUnits = false; //use imperial units
        else
            isUseMetricUnits = true;

        //create the track detail file(s)
        try {
            createFiles();

            // Display a notification about us starting.  We put an icon in the status bar.
            showNotification(StaticValues.NOTIF_TYPE_GPS_TRACK_STARTED_ID, false);
            SharedPreferences.Editor editor = mPreferences.edit();
            editor.putBoolean("isGpsTrackOn", true);
            editor.commit();
        }
        catch(IOException ex) {
            Logger.getLogger(GPSTrackService.class.getName()).log(Level.SEVERE, null, ex);
            showNotification(StaticValues.NOTIF_TYPE_FILESYSTEM_ERROR_ID, true);
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
        //create the header
        gpsTrackDetailCSVFileWriter.append(MainDbAdapter.GPSTRACKDETAIL_COL_GPSTRACK_ID_NAME + "," +
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
        cvData.put(MainDbAdapter.GPSTRACKDETAIL_COL_GPSTRACK_ID_NAME, gpsTrackId);
        cvData.put(MainDbAdapter.GPSTRACKDETAIL_COL_FILEFORMAT_NAME, StaticValues.CSV_FORMAT);
        cvData.put(MainDbAdapter.GPSTRACKDETAIL_COL_FILE_NAME, gpsTrackDetailCSVFile.getAbsolutePath());
        mDbAdapter.createRecord(MainDbAdapter.GPSTRACKDETAIL_TABLE_NAME, cvData);
//        String sqlStr = "INSERT INTO " + MainDbAdapter.GPSTRACKDETAIL_TABLE_NAME
//                + " ( "
//                    + MainDbAdapter.GPSTRACKDETAIL_COL_GPSTRACK_ID_NAME + ", "
//                    + MainDbAdapter.GPSTRACKDETAIL_COL_FILEFORMAT_NAME + ", "
//                    + MainDbAdapter.GPSTRACKDETAIL_COL_FILE_NAME
//                + " ) "
//                + " VALUES ( "
//                    + gpsTrackId + ", "
//                    + "'" + StaticValues.CSV_FORMAT + "', "
//                    + "'" + gpsTrackDetailCSVFile.getAbsolutePath() + "'"
//                + " ) ";
//        mDbAdapter.execSql(sqlStr);
    }

    private void createGOPFile(String fileName) throws IOException {
        gpsTrackDetailGOPFile = FileUtils.createGpsTrackDetailFile(StaticValues.GOP_FORMAT, fileName);
        gpsTrackDetailGOPFileWriter = new FileWriter(gpsTrackDetailGOPFile);
        //create the header
        gpsTrackDetailGOPFileWriter.append( "LatitudeE6" + "," +
                                                "LongitudeE6" + "\n");

        ContentValues cvData = new ContentValues();
        cvData.put(MainDbAdapter.GPSTRACKDETAIL_COL_GPSTRACK_ID_NAME, gpsTrackId);
        cvData.put(MainDbAdapter.GPSTRACKDETAIL_COL_FILEFORMAT_NAME, StaticValues.GOP_FORMAT);
        cvData.put(MainDbAdapter.GPSTRACKDETAIL_COL_FILE_NAME, gpsTrackDetailGOPFile.getAbsolutePath());
        mDbAdapter.createRecord(MainDbAdapter.GPSTRACKDETAIL_TABLE_NAME, cvData);

//        String sqlStr = "INSERT INTO " + MainDbAdapter.GPSTRACKDETAIL_TABLE_NAME
//                + " ( "
//                    + MainDbAdapter.GPSTRACKDETAIL_COL_GPSTRACK_ID_NAME + ", "
//                    + MainDbAdapter.GPSTRACKDETAIL_COL_FILEFORMAT_NAME + ", "
//                    + MainDbAdapter.GPSTRACKDETAIL_COL_FILE_NAME
//                + " ) "
//                + " VALUES ( "
//                    + gpsTrackId + ", "
//                    + "'" + StaticValues.GOP_FORMAT + "', "
//                    + "'" + gpsTrackDetailGOPFile.getAbsolutePath() + "'"
//                + " ) ";
//        mDbAdapter.execSql(sqlStr);
    }

    private void createGPXFile(String fileName) throws IOException{
        gpsTrackDetailGPXFile = FileUtils.createGpsTrackDetailFile(StaticValues.GPX_FORMAT, fileName);
        gpsTrackDetailGPXFileWriter = new FileWriter(gpsTrackDetailGPXFile);
        if(gpsTrackDetailGPXFileWriter == null)
            return;
        ContentValues cvData = new ContentValues();
        cvData.put(MainDbAdapter.GPSTRACKDETAIL_COL_GPSTRACK_ID_NAME, gpsTrackId);
        cvData.put(MainDbAdapter.GPSTRACKDETAIL_COL_FILEFORMAT_NAME, StaticValues.GPX_FORMAT);
        cvData.put(MainDbAdapter.GPSTRACKDETAIL_COL_FILE_NAME, gpsTrackDetailGPXFile.getAbsolutePath());
        mDbAdapter.createRecord(MainDbAdapter.GPSTRACKDETAIL_TABLE_NAME, cvData);

//        String sqlStr = "INSERT INTO " + MainDbAdapter.GPSTRACKDETAIL_TABLE_NAME
//                + " ( "
//                    + MainDbAdapter.GPSTRACKDETAIL_COL_GPSTRACK_ID_NAME + ", "
//                    + MainDbAdapter.GPSTRACKDETAIL_COL_FILEFORMAT_NAME + ", "
//                    + MainDbAdapter.GPSTRACKDETAIL_COL_FILE_NAME
//                + " ) "
//                + " VALUES ( "
//                    + gpsTrackId + ", "
//                    + "'" + StaticValues.GPX_FORMAT + "', "
//                    + "'" + gpsTrackDetailGPXFile.getAbsolutePath() + "'"
//                + " ) ";
//        mDbAdapter.execSql(sqlStr);
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
                + "<name><![CDATA[" + sName +"]]></name>\n"
                + "<desc><![CDATA[Created by <a href='http://sites.google.com/site/andicarfree'>AndiCar</a> on an Android powered device]]></desc>\n"
                + "<number>" + gpsTrackId + "_" + iFileCount + "</number>\n"
                + "<topografix:color>c0c0c0</topografix:color>\n"
                + "<trkseg>\n"
            );
    }

    private void createKMLFile(String fileName) throws IOException{
        gpsTrackDetailKMLFile = FileUtils.createGpsTrackDetailFile(StaticValues.KML_FORMAT, fileName);
        gpsTrackDetailKMLFileWriter = new FileWriter(gpsTrackDetailKMLFile);
        if(gpsTrackDetailKMLFileWriter == null)
            return;
        String name = "";
        String pointName = "";
        String pointStyle = "";
        if(iFileCount == 1){ //first file
            name = sName;
            pointName = "(Start)";
            pointStyle = "#sh_green-circle";
        }
        else{
            name = sName + " (part " + iFileCount + ")";
            pointName = "(Part " + iFileCount + " start)";
            pointStyle = "#icon28";
        }
        ContentValues cvData = new ContentValues();
        cvData.put(MainDbAdapter.GPSTRACKDETAIL_COL_GPSTRACK_ID_NAME, gpsTrackId);
        cvData.put(MainDbAdapter.GPSTRACKDETAIL_COL_FILEFORMAT_NAME, StaticValues.KML_FORMAT);
        cvData.put(MainDbAdapter.GPSTRACKDETAIL_COL_FILE_NAME, gpsTrackDetailKMLFile.getAbsolutePath());
        mDbAdapter.createRecord(MainDbAdapter.GPSTRACKDETAIL_TABLE_NAME, cvData);

//        String sqlStr = "INSERT INTO " + MainDbAdapter.GPSTRACKDETAIL_TABLE_NAME
//                + " ( "
//                    + MainDbAdapter.GPSTRACKDETAIL_COL_GPSTRACK_ID_NAME + ", "
//                    + MainDbAdapter.GPSTRACKDETAIL_COL_FILEFORMAT_NAME + ", "
//                    + MainDbAdapter.GPSTRACKDETAIL_COL_FILE_NAME
//                + " ) "
//                + " VALUES ( "
//                    + gpsTrackId + ", "
//                    + "'" + StaticValues.CSV_FORMAT + "', "
//                    + "'" + gpsTrackDetailKMLFile.getAbsolutePath() + "'"
//                + " ) ";
//        mDbAdapter.execSql(sqlStr);
        //initialize the file header

        gpsTrackDetailKMLFileWriter.append(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<kml xmlns=\"http://earth.google.com/kml/2.0\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n"
            + "<Document>\n"
            + "<atom:author><atom:name>Tracks running on AndiCar</atom:name></atom:author>\n"
            + "<name><![CDATA[" + name + "]]></name>\n"
            + "<description><![CDATA[Created by <a href='http://sites.google.com/site/andicarfree'>AndiCar</a>]]></description>\n"
            + "<Style id=\"track\"><LineStyle><color>7f0000ff</color><width>4</width></LineStyle></Style>\n"
            + "<Style id=\"sh_green-circle\"><IconStyle><scale>1.3</scale><Icon><href>http://maps.google.com/mapfiles/kml/paddle/grn-circle.png</href></Icon><hotSpot x=\"32\" y=\"1\" xunits=\"pixels\" yunits=\"pixels\"/></IconStyle></Style>\n"
            + "<Style id=\"sh_red-circle\"><IconStyle><scale>1.3</scale><Icon><href>http://maps.google.com/mapfiles/kml/paddle/red-circle.png</href></Icon><hotSpot x=\"32\" y=\"1\" xunits=\"pixels\" yunits=\"pixels\"/></IconStyle></Style>\n"
            + "<Style id=\"icon28\"><IconStyle><scale>1.3</scale><Icon><href>http://maps.google.com/mapfiles/kml/pal4/icon28.png</href></Icon><hotSpot x=\"20\" y=\"2\" xunits=\"pixels\" yunits=\"pixels\"/></IconStyle></Style>\n"
            + "<Placemark>\n"
              + "<name><![CDATA[" + pointName + "]]></name>\n"
              + "<description><![CDATA[]]></description>\n"
              + "<styleUrl>"+ pointStyle + "</styleUrl>\n"
              + "<Point>\n");

    }

    private void appendKMLStartPoint(){
        if(gpsTrackDetailKMLFileWriter == null)
            return;
        try{
            gpsTrackDetailKMLFileWriter.append(
                 "<coordinates>" + dCurrentLocationLongitude + "," + dCurrentLocationLatitude + "," +
                            dCurrentLocationAltitude + "</coordinates>\n"
                  + " </Point>\n"
                + "</Placemark>\n"
                + "<Placemark>\n"
                + "<name><![CDATA[" + gpsTrackDetailKMLFile.getName() + "]]></name>\n"
                + "<description><![CDATA[]]></description>\n"
                + "<styleUrl>#track</styleUrl>\n"
                + "<MultiGeometry>\n"
                + "<LineString>\n"
                + "<coordinates>\n");
        }
        catch(IOException e){

        }
    }

    private void appendKMLFooter(boolean isLastFile){
        if(gpsTrackDetailKMLFileWriter == null)
            return;
        try{
            String pointName = "";
            String pointStyle = "";
            if(isLastFile){ //first file
                pointName = "(End)";
                pointStyle = "#sh_red-circle";
            }
            else{
                pointName = "(Part " + iFileCount + " end)";
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
//            if(isLastFile)
//                footerTxt = footerTxt
                + "<Placemark>\n"
                  + "<name><![CDATA[" + pointName + "]]></name>\n"
                  + "<description><![CDATA[Created by <a href='http://sites.google.com/site/andicarfree'>AndiCar</a>."
                  + "<p>Distance: " + (dDistance) + (isUseMetricUnits? " km" : " mi")
                      + "]]></description>\n"
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
        }
        catch(IOException e){

        }
    }

    private void appendGPXFooter(){
        if(gpsTrackDetailGPXFileWriter == null)
            return;
        try{
            gpsTrackDetailGPXFileWriter.append(
                "</trkseg>\n"
                + "</trk>\n"
                + "</gpx>"
            );
        }
        catch(IOException e){
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
    }

    private void appendGOPTrackPoint() throws IOException {

        gpsTrackDetailGOPFileWriter.append(
                (int)(dCurrentLocationLatitude * 1E6)+ "," +
                (int)(dCurrentLocationLongitude * 1E6) + "\n");
    }

    private void appendGPXTrackPoint() throws IOException {
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
                "</trkpt>\n");
    }

    private void appendKMLTrackPoint() throws IOException {
        gpsTrackDetailKMLFileWriter.append(
                dCurrentLocationLongitude + "," +
                dCurrentLocationLatitude + "," +
                dCurrentLocationAltitude + " ");
    }

    private void createFiles() throws IOException {
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
    }

    private void closeFiles(boolean isLastFile) {
        try {
            if(gpsTrackDetailCSVFileWriter != null) {
                gpsTrackDetailCSVFileWriter.flush();
            }
            if(gpsTrackDetailGOPFileWriter != null) {
                gpsTrackDetailGOPFileWriter.flush();
            }
            if(gpsTrackDetailKMLFileWriter != null) {
                appendKMLFooter(isLastFile);
                gpsTrackDetailKMLFileWriter.flush();
            }
            if(gpsTrackDetailGPXFileWriter != null) {
                appendGPXFooter();
                gpsTrackDetailGPXFileWriter.flush();
            }
        }
        catch(IOException ex) {
            Logger.getLogger(GPSTrackService.class.getName()).log(Level.SEVERE, null, ex);
            showNotification(StaticValues.NOTIF_TYPE_FILESYSTEM_ERROR_ID, true);
        }
    }

    @Override
    public void onDestroy() {
        // Cancel the persistent notifications.
        mNM.cancelAll(); //R.string.GPSTrackService_TrackInProgressMessage);
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean("isGpsTrackOn", false);
        editor.commit();
        //update the statistics for the track
        updateStatistics();
        //close the database
        if(mDbAdapter != null){
            mDbAdapter.close();
            mDbAdapter = null;
        }
        closeFiles(true);
        
        mLocationManager.removeUpdates(mLocationListener);
        mLocationManager = null;
        if(mPreferences.getBoolean("GPSTrackCreateMileage", true) && !isErrorStop){
            Intent mileageInsertIntent = new Intent(GPSTrackService.this, MileageEditActivity.class);
            mileageInsertIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mileageInsertIntent.putExtra("Operation", "TrackToMileage");
            mileageInsertIntent.putExtra("Track_ID", gpsTrackId);
            mileageInsertIntent.putExtra("Tag", sTag);
            startActivity(mileageInsertIntent);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();

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

        if(what == StaticValues.NOTIF_TYPE_GPS_TRACK_STARTED_ID){
            title = getText(R.string.GPSTrackService_TrackInProgressTitle);
            message = getString(R.string.GPSTrackService_TrackInProgressMessage);

            notification = new Notification(R.drawable.andicar_gps_anim, message,
                    System.currentTimeMillis());
            notification.flags |= Notification.FLAG_NO_CLEAR;

            notification.setLatestEventInfo(this, title, message, contentIntent); 
        }
        else if(what == StaticValues.NOTIF_TYPE_GPS_ACCURACY_WARNING_ID){
            title = getText(R.string.GPSTrackService_AccuracyProblemTitle);
            message = getString(R.string.GPSTrackService_AccuracyProblemMessage);

            notification = new Notification(R.drawable.stat_sys_warning, message,
                    System.currentTimeMillis());
            notification.flags |= Notification.DEFAULT_LIGHTS;
            notification.flags |= Notification.DEFAULT_SOUND;

            notification.setLatestEventInfo(this, title, message, contentIntent);
            if(showToast)
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
        else if(what == StaticValues.NOTIF_TYPE_FILESYSTEM_ERROR_ID){
            title = getText(R.string.GPSTrackService_FileSystemErrorTitle);
            message = getString(R.string.ERR_034);

            notification = new Notification(R.drawable.stat_sys_error, message,
                    System.currentTimeMillis());

            notification.setLatestEventInfo(this, title, message, contentIntent);
            if(showToast)
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
        else if(what == StaticValues.NOTIF_TYPE_GPS_ACCURACY_SHUTDOWN_ID){
            try{
                title = getText(R.string.GPSTrackService_AutoShutDownTitle);
                message = getString(R.string.GPSTrackService_AutoShutDownMessage);
                BigDecimal bdSkippedPointPercentage = new BigDecimal(skippedPointPercentage).setScale(0, BigDecimal.ROUND_HALF_UP);
                message = message.replace("[%1]",  bdSkippedPointPercentage.toString() + "%").
                        replace("[%2]", iMaxAccuracyShutdownLimit + "%");

                notification = new Notification(R.drawable.stat_sys_error, message,
                        System.currentTimeMillis());
                notification.flags |= Notification.DEFAULT_LIGHTS;
                notification.flags |= Notification.DEFAULT_SOUND;

                notification.setLatestEventInfo(this, title, message, contentIntent);
                if(showToast)
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
            catch(NumberFormatException e){}
        }
        else if(what == StaticValues.NOTIF_TYPE_GPS_DISABLED_ID){
            title = getText(R.string.GPSTrackService_AutoShutDownTitle);
            message = getString(R.string.GPSTrackService_GPSDisabledMessage);
            notification = new Notification(R.drawable.stat_sys_error, message,
                    System.currentTimeMillis());
            notification.flags |= Notification.DEFAULT_LIGHTS;
            notification.flags |= Notification.DEFAULT_SOUND;

            notification.setLatestEventInfo(this, title, message, contentIntent);
            if(showToast)
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
        else if(what == StaticValues.NOTIF_TYPE_GPS_OUTOFSERVICE_ID){
            title = getText(R.string.GPSTrackService_AutoShutDownTitle);
            message = getString(R.string.GPSTrackService_GPSOutOfService);
            notification = new Notification(R.drawable.stat_sys_error, message,
                    System.currentTimeMillis());
            notification.flags |= Notification.DEFAULT_LIGHTS;
            notification.flags |= Notification.DEFAULT_SOUND;

            notification.setLatestEventInfo(this, title, message, contentIntent);
            if(showToast)
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }

        mNM.notify(what, notification);
    }

    private void updateStatistics(){
        
    	long lTotalTime = (lStopTime - lStartTime) / 1000; //in seconds; lStopTime & lStartTime are in miliseconds
        
        if(lLastNonMovingTime != 0 && lFirstNonMovingTime != 0)
            lTotalNonMovingTime = lTotalNonMovingTime + (lLastNonMovingTime - lFirstNonMovingTime);
        
        long lTotalMovingTime = lTotalTime - (lTotalNonMovingTime / 1000); //lTotalNonMovingTime is in milisecond

        if(dTotalUsedTrackPoints != 0)
            //at this moment dAvgAccuracy = SUM(CurrentAccuracy)
            dAvgAccuracy = dAvgAccuracy / dTotalUsedTrackPoints;
        else
            dAvgAccuracy = 0;

        if(lStopTime - lStartTime != 0)
            dAvgSpeed = dDistance / lTotalTime; // m/s
        else
            dAvgSpeed = 0;

        if(lTotalMovingTime != 0)
            dAvgMovingSpeed = dDistance / lTotalMovingTime; // m/s
        else
            dAvgMovingSpeed = 0;
        if(!isUseMetricUnits){
            dMinAccuracy = dMinAccuracy * 1.093613; //m to yd
            dMaxAccuracy = dMaxAccuracy * 1.093613; //m to yd
            dAvgAccuracy = dAvgAccuracy * 1.093613; //m to yd
            dMinAltitude = dMinAltitude * 1.093613; //m to yd
            dMaxAltitude = dMaxAltitude * 1.093613; //m to yd
            dDistance = dDistance * 0.000621371; //m to mi
            dAvgSpeed = dAvgSpeed * 2.23693; //m/s to mi/h
            dAvgMovingSpeed = dAvgMovingSpeed * 2.23693; //m/s to mi/h
            dMaxSpeed = dMaxSpeed  * 2.23693;
        }
        else{ // only m/s need to be converted to km/h
            dDistance = dDistance * 0.001; //m to km
            dAvgSpeed = dAvgSpeed * 3.6; //m/s to km/h
            dAvgMovingSpeed = dAvgMovingSpeed * 3.6; //m/s to km/h
            dMaxSpeed = dMaxSpeed  * 3.6;
        }

        ContentValues cvData = new ContentValues();
        cvData.put( MainDbAdapter.GPSTRACK_COL_MINACCURACY_NAME, dMinAccuracy);
        cvData.put( MainDbAdapter.GPSTRACK_COL_MAXACCURACY_NAME, dMaxAccuracy);
        cvData.put( MainDbAdapter.GPSTRACK_COL_AVGACCURACY_NAME, dAvgAccuracy);
        cvData.put( MainDbAdapter.GPSTRACK_COL_MINALTITUDE_NAME, dMinAltitude);
        cvData.put( MainDbAdapter.GPSTRACK_COL_MAXALTITUDE_NAME, dMaxAltitude);
        cvData.put( MainDbAdapter.GPSTRACK_COL_TOTALTIME_NAME, lTotalTime);
        cvData.put( MainDbAdapter.GPSTRACK_COL_MOVINGTIME_NAME, lTotalMovingTime);
        cvData.put( MainDbAdapter.GPSTRACK_COL_DISTANCE_NAME, dDistance);
        cvData.put( MainDbAdapter.GPSTRACK_COL_AVGSPEED_NAME, dAvgSpeed);
        cvData.put( MainDbAdapter.GPSTRACK_COL_AVGMOVINGSPEED_NAME, dAvgMovingSpeed);
        cvData.put( MainDbAdapter.GPSTRACK_COL_MAXSPEED_NAME, dMaxSpeed);
        cvData.put( MainDbAdapter.GPSTRACK_COL_TOTALTRACKPOINTS_NAME, dTotalTrackPoints);
        cvData.put( MainDbAdapter.GPSTRACK_COL_INVALIDTRACKPOINTS_NAME, dTotalSkippedTrackPoints);
        
        if(mDbAdapter == null)
            mDbAdapter = new MainDbAdapter(this);
        mDbAdapter.updateRecord(MainDbAdapter.GPSTRACK_TABLE_NAME, gpsTrackId, cvData);
        
//        String updateSql = "UPDATE " + MainDbAdapter.GPSTRACK_TABLE_NAME +
//                " SET "
//                    + MainDbAdapter.GPSTRACK_COL_MINACCURACY_NAME + " = " + dMinAccuracy + ", "
//                    + MainDbAdapter.GPSTRACK_COL_MAXACCURACY_NAME + " = " + dMaxAccuracy + ", "
//                    + MainDbAdapter.GPSTRACK_COL_AVGACCURACY_NAME + " = " + dAvgAccuracy + ", "
//                    + MainDbAdapter.GPSTRACK_COL_MINALTITUDE_NAME + " = " + dMinAltitude + ", "
//                    + MainDbAdapter.GPSTRACK_COL_MAXALTITUDE_NAME + " = " + dMaxAltitude + ", "
//                    + MainDbAdapter.GPSTRACK_COL_TOTALTIME_NAME + " = " + lTotalTime + ", " //in seconds
//                    + MainDbAdapter.GPSTRACK_COL_MOVINGTIME_NAME + " = " + lTotalMovingTime + ", " //in seconds
//                    + MainDbAdapter.GPSTRACK_COL_DISTANCE_NAME + " = " + dDistance + ", "
//                    + MainDbAdapter.GPSTRACK_COL_AVGSPEED_NAME + " = " + dAvgSpeed + ", "
//                    + MainDbAdapter.GPSTRACK_COL_AVGMOVINGSPEED_NAME + " = " + dAvgMovingSpeed  + ", "
//                    + MainDbAdapter.GPSTRACK_COL_MAXSPEED_NAME + " = " + dMaxSpeed + ", "
//                    + MainDbAdapter.GPSTRACK_COL_TOTALTRACKPOINTS_NAME + " = " + dTotalTrackPoints + ", "
//                    + MainDbAdapter.GPSTRACK_COL_INVALIDTRACKPOINTS_NAME + " = " + dTotalSkippedTrackPoints
//                + " WHERE "+ MainDbAdapter.GEN_COL_ROWID_NAME + " = " + gpsTrackId;
//
//        if(mDbAdapter == null)
//            mDbAdapter = new MainDbAdapter(this);
//
//        mDbAdapter.execSql(updateSql);
    }

    private class AndiCarLocationListener implements LocationListener
    {
        @Override
        public void onLocationChanged(Location loc) {
            boolean isValid = true;
            double gopDistance = 0;
            if(gpsTrackDetailCSVFileWriter == null){
                Toast.makeText(GPSTrackService.this, "No File Writer!", Toast.LENGTH_LONG).show();
                isErrorStop = true;
                stopSelf();
                return;
            }
//            Log.w("GPSTrackServuce", "onLocationChanged: dTotalTrackPoints = " + dTotalTrackPoints);

            if (loc != null) {
                try {
                    dTotalTrackPoints++;
                    dCurrentLocationLatitude = loc.getLatitude();
                    dCurrentLocationLongitude = loc.getLongitude();
                    dCurrentLocationAltitude = loc.getAltitude();
                    lCurrentLocationTime = loc.getTime();
                    dCurrentAccuracy = loc.getAccuracy();
//                    dCurrentSpeed = loc.getSpeed();
                    dCurrentLocationBearing = loc.getBearing();

                    if(isFirstPoint && iFileCount == 1){
                        lStartTime = lCurrentLocationTime;
                    	lOldLocationTime = lCurrentLocationTime;
                    	dCurrentSpeed = 0;
//                    	dOldSpeed = 0;
                    }

                    if(dCurrentAccuracy > iMaxAccuracy){
                        isValid = false;
                        //
                        if(lCurrentLocationTime - lStartTime > 30000){ //leave time for GPS initialization (30 sec)
                            dTotalSkippedTrackPoints++;
                            dTmpSkippedTrackPoints++;
                        }
                        if(dTmpSkippedTrackPoints > 10 && !bNotificationShowed){
                            //notify the user
                            showNotification(StaticValues.NOTIF_TYPE_GPS_ACCURACY_WARNING_ID, true);
                            bNotificationShowed = true;

                        }
                        skippedPointPercentage = (dTotalSkippedTrackPoints / dTotalTrackPoints) * 100;
                        if(skippedPointPercentage > iMaxAccuracyShutdownLimit){
                            showNotification(StaticValues.NOTIF_TYPE_GPS_ACCURACY_SHUTDOWN_ID, true);
                            isErrorStop = true;
                            stopSelf();
                        }
                    }
                    else{
//                    	//check acceleration. if too big (wrong data from the gps sensor) ignore the current location (see issue #32)
//                    	double acceleration = (dCurrentSpeed - dOldSpeed)/((lCurrentLocationTime - lOldLocationTime) / 1000);
//                    	if(acceleration > 13.88){ //13.88 m/s2 = 0 to 100 km/h in 2 seconds
//                            isValid = false;
//                    	}
//                    	else{
	                        isValid = true;
	                        dTotalUsedTrackPoints++;
	                        dTmpSkippedTrackPoints = 0;
	                        bNotificationShowed = false;
//	                    	dOldSpeed = dCurrentSpeed;
//                    	}
                    }

                    if(isValid){
                        if(isFirstPoint){
                            //write the starting point
                            if(gpsTrackDetailKMLFileWriter != null && iFileCount == 1){ //first file
                                appendKMLStartPoint();
                                appendGOPTrackPoint();
                            }
//                            lStartTime = lCurrentLocationTime;
                            isFirstPoint = false;
                        }
                        else{
                            Location.distanceBetween(dOldLocationLatitude, dOldLocationLongitude,
                                    dCurrentLocationLatitude, dCurrentLocationLongitude, fDistanceArray);
                            dDistanceBetweenLocations = fDistanceArray[0];
                            dDistance = dDistance + dDistanceBetweenLocations;
                            if(lCurrentLocationTime - lOldLocationTime > 0)
                            	dCurrentSpeed = dDistanceBetweenLocations / ((lCurrentLocationTime - lOldLocationTime) / 1000);
                            else
                            	dCurrentSpeed = 0;
                        	lOldLocationTime = lCurrentLocationTime;
                        }
                    }

                    appendCSVTrackPoint(isValid);

                    if(!isValid)
                        return;

                    //for drawing on the map add only a minimum 5 m distanced point from the previous point - performance reason
                    gopDistance = gopDistance + dDistanceBetweenLocations;
                    if(gopDistance >= 5){
                        appendGOPTrackPoint();
                        gopDistance = 0;
                    }

                    //set the stop time on each location change => the last will be the final lTotalTimeStop
                    lStopTime = lCurrentLocationTime;

                    //statistics
                    //non moving time
                    if(dCurrentSpeed == 0){
                        if(lFirstNonMovingTime == 0)
                            lFirstNonMovingTime = lCurrentLocationTime;
                        lLastNonMovingTime = lCurrentLocationTime;
                    }
                    else{ //currentSpeed > 0
                        if(lFirstNonMovingTime != 0){
                            lTotalNonMovingTime = lTotalNonMovingTime + (lLastNonMovingTime - lFirstNonMovingTime);
                            sNonMovingTimes = sNonMovingTimes + "" +
                                        lFirstNonMovingTime + "," + lLastNonMovingTime + "\n";
                            //reset
                            lLastNonMovingTime = 0;
                            lFirstNonMovingTime = 0;
                        }
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
                    Logger.getLogger(GPSTrackService.class.getName()).log(Level.SEVERE, null, ex);
                    Toast.makeText(GPSTrackService.this, "File error!\n" + ex.getMessage(), Toast.LENGTH_LONG).show();
                    isErrorStop = true;
                    stopSelf();
                }
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            if(provider.equals(LocationManager.GPS_PROVIDER)){
                showNotification(StaticValues.NOTIF_TYPE_GPS_DISABLED_ID, false);
//                stopSelf();
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
            mNM.cancel(StaticValues.NOTIF_TYPE_GPS_DISABLED_ID);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            if(provider.equals(LocationManager.GPS_PROVIDER)
                    && status == LocationProvider.OUT_OF_SERVICE){
                showNotification(StaticValues.NOTIF_TYPE_GPS_OUTOFSERVICE_ID, false);
//                stopSelf();
            }
        }
    }

}
