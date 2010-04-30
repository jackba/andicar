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
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.andicar.activity.GPSTrackController;
import org.andicar.activity.R;
import org.andicar.persistence.FileUtils;
import org.andicar.persistence.MainDbAdapter;
import org.andicar.utils.StaticValues;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import org.andicar.utils.AndiCarExceptionHandler;
import org.andicar.utils.AndiCarStatistics;
import org.andicar.utils.Utils;

/**
 *
 * @author miki
 */
public class GPSTrackService extends Service {

    private static int NOTIF_TYPE_GPSTRACK_STARTED = 1;
    private static int NOTIF_TYPE_ACCURACY_WARNING = 2;
    private static int NOTIF_TYPE_ACCURACY_SHUTDOWN = 3;
    private static int NOTIF_TYPE_FILESYSTEM_ERROR = 4;

    private NotificationManager mNM;
    private SharedPreferences mPreferences;
    protected MainDbAdapter mMainDbAdapter = null;
    protected Resources mResource = null;

    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private double oldLocationLatitude = 0;
    private double oldLocationLongitude = 0;
    private double currentLocationLatitude = 0;
    private double currentLocationLongitude = 0;
    private double currentLocationAltitude = 0;
    private double lastGoodLocationLatitude = 0;
    private double lastGoodLocationLongitude = 0;
    private double lastGoodLocationAltitude = 0;
    private float currentAccuracy = 0 ;
    private float currentSpeed = 0;
    private long currentLocationTime = 0;
    private Calendar currentLocationDateTime = Calendar.getInstance();
    private String currentLocationDateTimeGPXStr = "";
    private float[] distanceArray = new float[1];
    private float distanceBetweenLocations = 0;
    private long gpsTrackId = 0;
    private File gpsTrackDetailCSVFile = null;
    private FileWriter gpsTrackDetailCSVFileWriter = null;
    private File gpsTrackDetailKMLFile = null;
    private FileWriter gpsTrackDetailKMLFileWriter = null;
    private File gpsTrackDetailGPXFile = null;
    private FileWriter gpsTrackDetailGPXFileWriter = null;
    /* tmp values */
    private String sName = null;
    private String sUserComment = null;
    private long lCarId = -1;
    private long lDriverId = -1;
    private boolean isUseKML = false;
    private boolean isUseGPX = false;
//    private boolean isShowOnMap = false;
    private String fileName = null;
    //statistics
    private float fMinAccuracy = 9999;
    private double dAvgAccuracy = 0;
    private float fMaxAccuracy = 0;
    private double dMinAltitude = 99999;
    private double dMaxAltitude = 0;
    private long lStartTime = 0;
    private long lStopTime = 0;
    private long lTotalMovingTime = 0;
    private float fDistance = 0;
    private float fMaxSpeed = 0;
    private double dAvgSpeed = 0;
    private double dAvgMovingSpeed = 0;
    private boolean isFirstPoint = true;
    private long lLastMovingTime = 0;
    private long lTmpMovingTime = 0;
    private int iMaxAccuracy = 9999999;
    private int iMaxAccuracyShutdownLimit = 30;
    private double dTotalTrackPoints = 0;
    private double dTotalSkippedTrackPoints = 0;
    private double dTotalUsedTrackPoints = 0;
    private double dTmpSkippedTrackPoints = 0;
    private boolean bNotificationShowed = false;
    private double skippedPointPercentage = 0;
    private boolean isSendCrashReport;

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
            Toast.makeText(this, R.string.SERVICE_GPSTRACK_GPSDISABLED, Toast.LENGTH_SHORT).show();
            stopSelf();
        }

        mLocationListener = new AndiCarLocationListener();
        mLocationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER, 
                Long.parseLong(mPreferences.getString("GPSTrackMinTime", "0")),
                /*Long.parseLong(mPreferences.getString("GPSTrackMinDistance", "5"))*/ 0,
                mLocationListener);

        mMainDbAdapter = new MainDbAdapter(this);

        sName = mPreferences.getString("GPSTrackTmp_Name", null);
        sUserComment = mPreferences.getString("GPSTrackTmp_UserComment", null);
        lCarId = mPreferences.getLong("GPSTrackTmp_DriverId", mPreferences.getLong("CurrentCar_ID", 0));
        lDriverId = mPreferences.getLong("GPSTrackTmp_CarId", mPreferences.getLong("CurrentDriver_ID", 0));
        isUseKML = mPreferences.getBoolean("GPSTrackTmp_IsUseKML", false);
        isUseGPX = mPreferences.getBoolean("GPSTrackTmp_IsUseGPX", false);
//        isShowOnMap = mPreferences.getBoolean("GPSTrackTmp_IsShowOnMap", false);
        iMaxAccuracy = Integer.parseInt(mPreferences.getString("GPSTrackMaxAccuracy", "20"));
        iMaxAccuracyShutdownLimit = Integer.parseInt(mPreferences.getString("GPSTrackMaxAccuracyShutdownLimit", "30"));

        //create the master record
        //use direct table insert for increasing the speed of the DB operation
        String sqlStr = "INSERT INTO " + MainDbAdapter.GPSTRACK_TABLE_NAME
                    + " ( "
                        + MainDbAdapter.GEN_COL_NAME_NAME + ", "
                        + MainDbAdapter.GEN_COL_USER_COMMENT_NAME + ", "
                        + MainDbAdapter.GPSTRACK_COL_CAR_ID_NAME + ", "
                        + MainDbAdapter.GPSTRACK_COL_DRIVER_ID_NAME + ", "
//                        + MainDbAdapter.GPSTRACK_COL_MILEAGE_ID_NAME + ", "
                        + MainDbAdapter.GPSTRACK_COL_DATE_NAME + " "
                    + " ) "
                    + " VALUES ( "
                        + " '" + sName + "', "
                        + " '" + sUserComment + "', "
                        + lCarId + ", "
                        + lDriverId + ", "
                        + System.currentTimeMillis() + ") ";

        mMainDbAdapter.execSql(sqlStr);
        sqlStr = "SELECT MAX(" + MainDbAdapter.GEN_COL_ROWID_NAME + ") FROM " + MainDbAdapter.GPSTRACK_TABLE_NAME;
        Cursor c = mMainDbAdapter.execSelectSql(sqlStr);
        c.moveToNext();
        gpsTrackId = c.getLong(0);
        c.close();
        fileName = "" + gpsTrackId;
        //create the track detail file(s)
        try {
            gpsTrackDetailCSVFile = FileUtils.createGpsTrackDetailFile(StaticValues.CSV_FORMAT, fileName);
            if(isUseKML)
                gpsTrackDetailKMLFile = FileUtils.createGpsTrackDetailFile(StaticValues.KML_FORMAT, fileName);
            else
                gpsTrackDetailKMLFile = null;
            if(isUseGPX)
                gpsTrackDetailGPXFile = FileUtils.createGpsTrackDetailFile(StaticValues.GPX_FORMAT, fileName);
            else
                gpsTrackDetailGPXFile = null;

            //create a link for between the master track and the file
            if(gpsTrackDetailCSVFile != null){
                    gpsTrackDetailCSVFileWriter = new FileWriter(gpsTrackDetailCSVFile);
                    sqlStr = "INSERT INTO " + MainDbAdapter.GPSTRACKDETAIL_TABLE_NAME
                            + " ( "
                                + MainDbAdapter.GPSTRACKDETAIL_COL_GPSTRACK_ID_NAME + ", "
                                + MainDbAdapter.GPSTRACKDETAIL_COL_FILEFORMAT_NAME + ", "
                                + MainDbAdapter.GPSTRACKDETAIL_COL_FILE_NAME
                            + " ) "
                            + " VALUES ( "
                                + gpsTrackId + ", "
                                + "'" + StaticValues.CSV_FORMAT + "', "
                                + "'" + gpsTrackDetailCSVFile.getAbsolutePath() + "'"
                            + " ) ";
                    mMainDbAdapter.execSql(sqlStr);
                    //create the header
                    gpsTrackDetailCSVFileWriter.append(MainDbAdapter.GPSTRACKDETAIL_COL_GPSTRACK_ID_NAME + ","
                                                        + "Accuracy" + ","
                                                        + "Altitude" + ","
                                                        + "Latitude" + ","
                                                        + "Longitude" + ","
                                                        + "Speed" + ","
                                                        + "Time" + ","
                                                        + "Distance" + ","
                                                        + "Bearing" + ","
                                                        + "TotalTrackPointCount" + ","
                                                        + "InvalidTrackPointCount" + ","
                                                        + "SkippedPointPercentage" + ","
                                                        + "IsValidPoint"+ "\n"
                                                        );
            }
            if(gpsTrackDetailKMLFile != null){
                    gpsTrackDetailKMLFileWriter = new FileWriter(gpsTrackDetailKMLFile);
                    sqlStr = "INSERT INTO " + MainDbAdapter.GPSTRACKDETAIL_TABLE_NAME
                            + " ( "
                                + MainDbAdapter.GPSTRACKDETAIL_COL_GPSTRACK_ID_NAME + ", "
                                + MainDbAdapter.GPSTRACKDETAIL_COL_FILEFORMAT_NAME + ", "
                                + MainDbAdapter.GPSTRACKDETAIL_COL_FILE_NAME
                            + " ) "
                            + " VALUES ( "
                                + gpsTrackId + ", "
                                + "'" + StaticValues.CSV_FORMAT + "', "
                                + "'" + gpsTrackDetailKMLFile.getAbsolutePath() + "'"
                            + " ) ";
                    mMainDbAdapter.execSql(sqlStr);
                    //initialize the file header
                    createKMLHeader();
            }
            if(gpsTrackDetailGPXFile != null){
                    gpsTrackDetailGPXFileWriter = new FileWriter(gpsTrackDetailGPXFile);
                    sqlStr = "INSERT INTO " + MainDbAdapter.GPSTRACKDETAIL_TABLE_NAME
                            + " ( "
                                + MainDbAdapter.GPSTRACKDETAIL_COL_GPSTRACK_ID_NAME + ", "
                                + MainDbAdapter.GPSTRACKDETAIL_COL_FILEFORMAT_NAME + ", "
                                + MainDbAdapter.GPSTRACKDETAIL_COL_FILE_NAME
                            + " ) "
                            + " VALUES ( "
                                + gpsTrackId + ", "
                                + "'" + StaticValues.GPX_FORMAT + "', "
                                + "'" + gpsTrackDetailGPXFile.getAbsolutePath() + "'"
                            + " ) ";
                    mMainDbAdapter.execSql(sqlStr);
                    //initialize the file header
                    createGPXHeader();
            }
            // Display a notification about us starting.  We put an icon in the status bar.
            showNotification(NOTIF_TYPE_GPSTRACK_STARTED, false);
            SharedPreferences.Editor editor = mPreferences.edit();
            editor.putBoolean("isGpsTrackOn", true);
            editor.commit();
        }
        catch(IOException ex) {
            Logger.getLogger(GPSTrackService.class.getName()).log(Level.SEVERE, null, ex);
            showNotification(NOTIF_TYPE_FILESYSTEM_ERROR, true);
            stopSelf();
        }
        //close the database
        if(mMainDbAdapter != null){
            mMainDbAdapter.close();
            mMainDbAdapter = null;
        }

        if(isSendCrashReport)
            AndiCarStatistics.sendFlurryEvent("GPSTrack", null);
    }

    private void createKMLHeader(){
        if(gpsTrackDetailKMLFileWriter == null)
            return;
        try{
            gpsTrackDetailKMLFileWriter.append(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<kml xmlns=\"http://earth.google.com/kml/2.0\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n"
                + "<Document>\n"
                + "<atom:author><atom:name>Tracks running on AndiCar</atom:name></atom:author>\n"
                + "<name><![CDATA[" + sName + "]]></name>\n"
                + "<description><![CDATA[Created by <a href='http://sites.google.com/site/andicarfree'>AndiCar</a>]]></description>\n"
                + "<Style id=\"track\"><LineStyle><color>7f0000ff</color><width>4</width></LineStyle></Style>\n"
                + "<Style id=\"sh_green-circle\"><IconStyle><scale>1.3</scale><Icon><href>http://maps.google.com/mapfiles/kml/paddle/grn-circle.png</href></Icon><hotSpot x=\"32\" y=\"1\" xunits=\"pixels\" yunits=\"pixels\"/></IconStyle></Style>\n"
                + "<Style id=\"sh_red-circle\"><IconStyle><scale>1.3</scale><Icon><href>http://maps.google.com/mapfiles/kml/paddle/red-circle.png</href></Icon><hotSpot x=\"32\" y=\"1\" xunits=\"pixels\" yunits=\"pixels\"/></IconStyle></Style>\n"
                + "<Style id=\"sh_ylw-pushpin\"><IconStyle><scale>1.3</scale><Icon><href>http://maps.google.com/mapfiles/kml/pushpin/ylw-pushpin.png</href></Icon><hotSpot x=\"20\" y=\"2\" xunits=\"pixels\" yunits=\"pixels\"/></IconStyle></Style>\n"
                + "<Style id=\"sh_blue-pushpin\"><IconStyle><scale>1.3</scale><Icon><href>http://maps.google.com/mapfiles/kml/pushpin/blue-pushpin.png</href></Icon><hotSpot x=\"20\" y=\"2\" xunits=\"pixels\" yunits=\"pixels\"/></IconStyle></Style>\n"
                + "<Placemark>\n"
                  + "<name><![CDATA[(Start)]]></name>\n"
                  + "<description><![CDATA[]]></description>\n"
                  + "<styleUrl>#sh_green-circle</styleUrl>\n"
                  + "<Point>\n");
        }
        catch(IOException e){
            
        }
    }

    private void createGPXHeader(){
        if(gpsTrackDetailGPXFileWriter == null)
            return;
        try{
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
                    + "<number>" + gpsTrackId + "</number>\n"
                    + "<topografix:color>c0c0c0</topografix:color>\n"
                    + "<trkseg>\n"
                );
        }
        catch(IOException e){

        }
    }

    private void appendKMLStartPoint(){
        if(gpsTrackDetailKMLFileWriter == null)
            return;
        try{
            gpsTrackDetailKMLFileWriter.append(
                 "<coordinates>" + currentLocationLongitude + "," + currentLocationLatitude + "," +
                            currentLocationAltitude + "</coordinates>\n"
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

    private void appendKMLFooter(){
        if(gpsTrackDetailKMLFileWriter == null)
            return;
        try{
            gpsTrackDetailKMLFileWriter.append(
                "\n</coordinates>\n"
                + "</LineString>\n"
                + "</MultiGeometry>\n"
                + "</Placemark>\n"
                + "<Placemark>\n"
                  + "<name><![CDATA[(End)]]></name>\n"
                  + "<description><![CDATA[Created by <a href='http://sites.google.com/site/andicarfree'>AndiCar</a>."
                  + "<p>Total Distance: " + (fDistance / 1000.00) + "km\n" //(2.2 mi)<br>Total Time: 10:57<br>Moving Time: 8:12<br>Average Speed: 21.20 km/h (13.2 mi/h)<br>Average Moving Speed: 26.38 km/h (16.4 mi/h)<br>Max Speed: 59.40 km/h (36.9 mi/h)<br>Min Elevation: 588 m (1927 ft)<br>Max Elevation: 605 m (1986 ft)<br>Elevation Gain: 25 m (81 ft)<br>Max Grade: 5 %<br>Min Grade: -2 %<br>Recorded: Thu Apr 08 13:11:35 GMT+02:00 2010<br>Activity type: -<br><img border="0" src="http://chart.apis.google.com/chart?&chs=600x350&cht=lxy&chtt=Elevation&chxt=x,y&chxr=0,0,3,0%257C1,500.0,700.0,25&chco=009A00&chm=B,00AA00,0,0,0&chg=100000,12.5,1,0&chd=e:,"/>
                      + "]]></description>\n"
                  + "<styleUrl>#sh_red-circle</styleUrl>\n"
                  + "<Point>\n"
                    + "<coordinates>" + lastGoodLocationLongitude + "," + lastGoodLocationLatitude + "," +
                            lastGoodLocationAltitude + "</coordinates>\n"
                  + "</Point>\n"
                + "</Placemark>\n"
                + "</Document>\n"
                + "</kml>\n");
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

    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        mNM.cancel(R.string.GPSTRACK_SERVICE_GPSTRACKSERVICEINPROGRESS_MESSAGE);
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean("isGpsTrackOn", false);
        editor.commit();
        //update the statistics for the track
        updateStatistics();
        //close the database
        if(mMainDbAdapter != null){
            mMainDbAdapter.close();
            mMainDbAdapter = null;
        }
        try {
            if(gpsTrackDetailCSVFileWriter != null)
                gpsTrackDetailCSVFileWriter.flush();
            if(gpsTrackDetailKMLFileWriter != null){
                appendKMLFooter();
                gpsTrackDetailKMLFileWriter.flush();
            }
            if(gpsTrackDetailGPXFileWriter != null){
                appendGPXFooter();
                gpsTrackDetailGPXFileWriter.flush();
            }
        }
        catch(IOException ex) {
            Logger.getLogger(GPSTrackService.class.getName()).log(Level.SEVERE, null, ex);
            showNotification(NOTIF_TYPE_FILESYSTEM_ERROR, true);
        }
        
        mLocationManager.removeUpdates(mLocationListener);
        mLocationManager = null;
        
        // Tell the user we stopped.
        Toast.makeText(this, R.string.SERVICE_GPSTRACK_SERVICESTOPPED_MESSAGE, Toast.LENGTH_SHORT).show();
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
        int id = 0;
        title = getText(R.string.SERVICE_GPSTRACK_LABEL);
        
        contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, GPSTrackController.class), 0);
        if(what == NOTIF_TYPE_GPSTRACK_STARTED){
            message = getString(R.string.GPSTRACK_SERVICE_GPSTRACKSERVICEINPROGRESS_MESSAGE);
            id = R.string.GPSTRACK_SERVICE_GPSTRACKSERVICEINPROGRESS_MESSAGE;

            notification = new Notification(R.drawable.andicar_gps_anim, message,
                    System.currentTimeMillis());
            notification.setLatestEventInfo(this, title, message, contentIntent); 
        }
        else if(what == NOTIF_TYPE_ACCURACY_WARNING){
            message = getString(R.string.SERVICE_GPSTRACK_ACCURACYPROBLEM);
            id = R.string.SERVICE_GPSTRACK_ACCURACYPROBLEM;

            notification = new Notification(R.drawable.stat_sys_warning, message,
                    System.currentTimeMillis());
            notification.setLatestEventInfo(this, title, message, contentIntent);
            if(showToast)
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
        else if(what == NOTIF_TYPE_FILESYSTEM_ERROR){
            message = getString(R.string.ERR_034);
            id = R.string.ERR_034;

            notification = new Notification(R.drawable.stat_sys_error, message,
                    System.currentTimeMillis());
            notification.setLatestEventInfo(this, title, message, contentIntent);
            if(showToast)
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
        else if(what == NOTIF_TYPE_ACCURACY_SHUTDOWN){
            message = getString(R.string.SERVICE_GPSTRACK_AUTOSHUTDOWN_MSG);
            BigDecimal bdSkippedPointPercentage = new BigDecimal(skippedPointPercentage).setScale(0, BigDecimal.ROUND_HALF_UP);
            message = message.replace("%1",  bdSkippedPointPercentage.toString() + "%").
                    replace("%2", iMaxAccuracyShutdownLimit + "%");
            id = R.string.SERVICE_GPSTRACK_AUTOSHUTDOWN_MSG;

            notification = new Notification(R.drawable.stat_sys_error, message,
                    System.currentTimeMillis());
            notification.setLatestEventInfo(this, title, message, contentIntent);
            if(showToast)
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }

        mNM.notify(id, notification);
    }

    private void updateStatistics(){
        //TODO
        /*
         * convertion for distance and speed to car uom!
         */
        if(dTotalUsedTrackPoints != 0)
            dAvgAccuracy = dAvgAccuracy / dTotalUsedTrackPoints;
        else
            dAvgAccuracy = 0;

        if(lStopTime - lStartTime != 0)
            dAvgSpeed = fDistance / ((lStopTime - lStartTime)/1000); // m/s
        else
            dAvgSpeed = 0;

        if(lTotalMovingTime != 0)
            dAvgMovingSpeed = fDistance / (lTotalMovingTime / 1000); // m/s
        else
            dAvgMovingSpeed = 0;
        
        String updateSql = "UPDATE " + MainDbAdapter.GPSTRACK_TABLE_NAME +
                " SET "
                    + MainDbAdapter.GPSTRACK_COL_MINACCURACY_NAME + " = " + fMinAccuracy + ", "
                    + MainDbAdapter.GPSTRACK_COL_MAXACCURACY_NAME + " = " + fMaxAccuracy + ", "
                    + MainDbAdapter.GPSTRACK_COL_AVGACCURACY_NAME + " = " + (dAvgAccuracy > 0 ? dAvgAccuracy : null) + ", "
                    + MainDbAdapter.GPSTRACK_COL_MINALTITUDE_NAME + " = " + dMinAltitude + ", "
                    + MainDbAdapter.GPSTRACK_COL_MAXALTITUDE_NAME + " = " + dMaxAltitude + ", "
                    + MainDbAdapter.GPSTRACK_COL_TOTALTIME_NAME + " = " + (lStopTime - lStartTime) + ", "
                    + MainDbAdapter.GPSTRACK_COL_MOVINGTIME_NAME + " = " + lTotalMovingTime + ", "
                    + MainDbAdapter.GPSTRACK_COL_DISTNACE_NAME + " = " + fDistance + ", "
                    + MainDbAdapter.GPSTRACK_COL_AVGSPEED_NAME + " = " + dAvgSpeed + ", "
                    + MainDbAdapter.GPSTRACK_COL_AVGMOVINGSPEED_NAME + " = " + (dAvgMovingSpeed > 0 ? dAvgMovingSpeed : null)  + ", "
                    + MainDbAdapter.GPSTRACK_COL_MAXSPEED_NAME + " = " + fMaxSpeed + " " +
                " WHERE "+ MainDbAdapter.GEN_COL_ROWID_NAME + " = " + gpsTrackId;

        if(mMainDbAdapter == null)
            mMainDbAdapter = new MainDbAdapter(this);

        mMainDbAdapter.execSql(updateSql);
    }

    private class AndiCarLocationListener implements LocationListener
    {
        @Override
        public void onLocationChanged(Location loc) {
            boolean isValid = true;
            if(gpsTrackDetailCSVFileWriter == null){
                Toast.makeText(GPSTrackService.this, "No File Writer!", Toast.LENGTH_LONG).show();
                stopSelf();
            }
            if (loc != null) {
                try {
                    dTotalTrackPoints++;
                    currentLocationLatitude = loc.getLatitude();
                    currentLocationLongitude = loc.getLongitude();
                    currentLocationAltitude = loc.getAltitude();
                    currentLocationTime = loc.getTime();
                    currentAccuracy = loc.getAccuracy();
                    currentSpeed = loc.getSpeed();

                    if(isFirstPoint)
                        lStartTime = currentLocationTime;

                    if(currentAccuracy > iMaxAccuracy){
                        isValid = false;

                        //
                        if(currentLocationTime - lStartTime > 20000){
                            dTotalSkippedTrackPoints++;
                            dTmpSkippedTrackPoints++;
                        }
                        if(dTmpSkippedTrackPoints > 10 && !bNotificationShowed){
                            //notify the user
                            showNotification(NOTIF_TYPE_ACCURACY_WARNING, true);
                            bNotificationShowed = true;

                        }
                        skippedPointPercentage = (dTotalSkippedTrackPoints / dTotalTrackPoints) * 100;
                        if(skippedPointPercentage > iMaxAccuracyShutdownLimit){
                            showNotification(NOTIF_TYPE_ACCURACY_SHUTDOWN, true);
                            stopSelf();
                        }
                    }
                    else{
                        isValid = true;
                        dTotalUsedTrackPoints++;
                        dTmpSkippedTrackPoints = 0;
                        bNotificationShowed = false;
                    }

                    if(isValid){
                        if(isFirstPoint){
                            //write the starting point
                            if(gpsTrackDetailKMLFileWriter != null)
                                appendKMLStartPoint();
                            lStartTime = currentLocationTime;
                            isFirstPoint = false;
                        }
                        else{
                            Location.distanceBetween(oldLocationLatitude, oldLocationLongitude,
                                    currentLocationLatitude, currentLocationLongitude, distanceArray);
                            distanceBetweenLocations = distanceArray[0];
                            fDistance = fDistance + distanceBetweenLocations;
                        }
                    }

                    gpsTrackDetailCSVFileWriter.append(gpsTrackId + ","
                                                        + currentAccuracy + ","
                                                        + currentLocationAltitude + ","
                                                        + currentLocationLatitude + ","
                                                        + currentLocationLongitude + ","
                                                        + currentSpeed + ","
                                                        + currentLocationTime + ","
                                                        + distanceBetweenLocations + ","
                                                        + loc.getBearing() + ","
                                                        + dTotalTrackPoints + ","
                                                        + dTotalSkippedTrackPoints + ","
                                                        + skippedPointPercentage + ","
                                                        + (isValid ? "Yes" : "No") + "\n");
                    if(!isValid)
                        return;

                    //set the stop time on each location change => the last will be the final lTotalTimeStop
                    lStopTime = currentLocationTime;

                    //statistics
                    if(currentSpeed > 0){
                        if(lLastMovingTime != 0)
                            lTmpMovingTime = lTmpMovingTime + (currentLocationTime - lLastMovingTime);
                        lLastMovingTime = currentLocationTime;
                    }
                    else{
                        lTotalMovingTime = lTotalMovingTime + lTmpMovingTime;
                        lLastMovingTime = 0;
                    }

                    if(currentAccuracy < fMinAccuracy)
                        fMinAccuracy = currentAccuracy;
                    if(currentAccuracy > fMaxAccuracy)
                        fMaxAccuracy = currentAccuracy;

                    //at the end of the tracking fAvgAccuracy will be fAvgAccuracy / iTrackPointCount
                    dAvgAccuracy = dAvgAccuracy + currentAccuracy;

                    if(currentLocationAltitude < dMinAltitude)
                        dMinAltitude = currentLocationAltitude;
                    if(currentLocationAltitude > dMaxAltitude)
                        dMaxAltitude = currentLocationAltitude;

                    if(currentSpeed > fMaxSpeed)
                        fMaxSpeed = currentSpeed;

                    if(gpsTrackDetailKMLFileWriter != null)
                        gpsTrackDetailKMLFileWriter.append(currentLocationLongitude + ","
                                                    + currentLocationLatitude + ","
                                                    + currentLocationAltitude + " ");

                    if(gpsTrackDetailGPXFileWriter != null){
                        currentLocationDateTime.setTimeInMillis(currentLocationTime);
                        currentLocationDateTimeGPXStr =
                                currentLocationDateTime.get(Calendar.YEAR) + "-"
                                + Utils.pad(currentLocationDateTime.get(Calendar.MONTH) + 1) + "-"
                                + Utils.pad(currentLocationDateTime.get(Calendar.DAY_OF_MONTH)) + "T"
                                + Utils.pad(currentLocationDateTime.get(Calendar.HOUR_OF_DAY)) + ":"
                                + Utils.pad(currentLocationDateTime.get(Calendar.MINUTE)) + ":"
                                + Utils.pad(currentLocationDateTime.get(Calendar.SECOND)) + "Z";
                        gpsTrackDetailGPXFileWriter.append(
                                "<trkpt lat=\"" + currentLocationLatitude + "\" lon=\"" + currentLocationLongitude + "\">\n"
                                + "<ele>" + currentLocationAltitude + "</ele>\n"
                                + "<time>" + currentLocationDateTimeGPXStr + "</time>\n"
                                + "</trkpt>\n"
                                );
                    }

                    oldLocationLatitude = currentLocationLatitude;
                    oldLocationLongitude = currentLocationLongitude;
                    lastGoodLocationLatitude = currentLocationLatitude;
                    lastGoodLocationLongitude = currentLocationLongitude;
                    lastGoodLocationAltitude = currentLocationAltitude;
                }
                catch(IOException ex) {
                    Logger.getLogger(GPSTrackService.class.getName()).log(Level.SEVERE, null, ex);
                    Toast.makeText(GPSTrackService.this, "File error!\n" + ex.getMessage(), Toast.LENGTH_LONG).show();
                    stopSelf();
                }
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status,
            Bundle extras) {
        }
    }

}
