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
import java.util.Calendar;
import org.andicar.utils.Utils;

/**
 *
 * @author miki
 */
public class GPSTrackService extends Service {
    private NotificationManager mNM;
    private SharedPreferences mPreferences;
    protected MainDbAdapter mMainDbAdapter = null;

    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    double oldLocationLatitude = 0;
    double oldLocationLongitude = 0;
    double currentLocationLatitude = 0;
    double currentLocationLongitude = 0;
    double currentLocationAltitude = 0;
    long currentLocationTime = 0;
    Calendar currentLocationDateTime = Calendar.getInstance();
    String currentLocationDateTimeGPXStr = "";
    float[] distanceArray = new float[1];
    float distanceBetweenLocations = 0;
    private long gpsTrackId = 0;
    private File gpsTrackDetailCSVFile = null;
    private FileWriter gpsTrackDetailCSVFileWriter = null;
    private File gpsTrackDetailKMLFile = null;
    private FileWriter gpsTrackDetailKMLFileWriter = null;
    private File gpsTrackDetailGPXFile = null;
    private FileWriter gpsTrackDetailGPXFileWriter = null;
    double totalDistance = 0;

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
                Long.parseLong(mPreferences.getString("GPSTrackMinDistance", "5")),
                mLocationListener);

        mMainDbAdapter = new MainDbAdapter(this);

        //create the master record
        //use direct table insert for increasing the speed of the DB operation
        String sqlStr = "INSERT INTO " + MainDbAdapter.GPSTRACK_TABLE_NAME
                    + " ( "
                        + MainDbAdapter.GEN_COL_NAME_NAME + ", "
                        + MainDbAdapter.GPSTRACK_COL_CAR_ID_NAME + ", "
                        + MainDbAdapter.GPSTRACK_COL_DRIVER_ID_NAME + ", "
//                        + MainDbAdapter.GPSTRACK_COL_MILEAGE_ID_NAME + ", "
                        + MainDbAdapter.GPSTRACK_COL_DATE_NAME + " "
                    + " ) "
                    + " VALUES ( "
                        + " 'Gps Track', "
                        + mPreferences.getLong("CurrentCar_ID", 0) + ", "
                        + mPreferences.getLong("CurrentDriver_ID", 0) + ", "
                        + System.currentTimeMillis() + ") ";

        mMainDbAdapter.execSql(sqlStr);
        sqlStr = "SELECT MAX(" + MainDbAdapter.GEN_COL_ROWID_NAME + ") FROM " + MainDbAdapter.GPSTRACK_TABLE_NAME;
        Cursor c = mMainDbAdapter.execSelectSql(sqlStr);
        c.moveToNext();
        gpsTrackId = c.getLong(0);
        c.close();
        sqlStr = "UPDATE " + MainDbAdapter.GPSTRACK_TABLE_NAME
                    + " SET " + MainDbAdapter.GEN_COL_NAME_NAME + " = " + MainDbAdapter.GEN_COL_NAME_NAME + " || '_" + gpsTrackId + "' "
                    + " WHERE " + MainDbAdapter.GEN_COL_ROWID_NAME + " = " + gpsTrackId;
        mMainDbAdapter.execSql(sqlStr);
        //create the track detail file(s)
        try {
            gpsTrackDetailCSVFile = FileUtils.createGpsTrackDetailFile(StaticValues.CSV_FORMAT, "" + gpsTrackId);
            gpsTrackDetailKMLFile = FileUtils.createGpsTrackDetailFile(StaticValues.KML_FORMAT, "" + gpsTrackId);
            gpsTrackDetailGPXFile = FileUtils.createGpsTrackDetailFile(StaticValues.GPX_FORMAT, "" + gpsTrackId);

            //create a link for between the master track and the file
            if(gpsTrackDetailCSVFile != null){
                    gpsTrackDetailCSVFileWriter = new FileWriter(gpsTrackDetailCSVFile);
                    sqlStr = "INSERT INTO " + MainDbAdapter.GPSTRACKDETAIL_TABLE_NAME
                            + " ( "
                                + MainDbAdapter.GPSTRACKDETAIL_COL_GPSTRACK_ID_NAME + ", "
                                + MainDbAdapter.GPSTRACKDETAIL_COL_FILEFORMAT_NAME + ", "
                                + MainDbAdapter.GEN_COL_NAME_NAME
                            + " ) "
                            + " VALUES ( "
                                + gpsTrackId + ", "
                                + "'" + StaticValues.CSV_FORMAT + "', "
                                + "'" + gpsTrackDetailCSVFile.getAbsolutePath() + "'"
                            + " ) ";
                    mMainDbAdapter.execSql(sqlStr);
                    gpsTrackDetailCSVFileWriter.append(MainDbAdapter.GPSTRACKDETAIL_COL_GPSTRACK_ID_NAME + ","
                                                        + "ACCURACY" + ","
                                                        + "ALTITUDE" + ","
                                                        + "LATITUDE" + ","
                                                        + "LONGITUDE" + ","
                                                        + "SPEED" + ","
                                                        + "TIME" + ","
                                                        + "DISTNACE" + ","
                                                        + "BEARING" + "\n"
                                                        );
            }
            if(gpsTrackDetailKMLFile != null){
                    gpsTrackDetailKMLFileWriter = new FileWriter(gpsTrackDetailKMLFile);
                    sqlStr = "INSERT INTO " + MainDbAdapter.GPSTRACKDETAIL_TABLE_NAME
                            + " ( "
                                + MainDbAdapter.GPSTRACKDETAIL_COL_GPSTRACK_ID_NAME + ", "
                                + MainDbAdapter.GPSTRACKDETAIL_COL_FILEFORMAT_NAME + ", "
                                + MainDbAdapter.GEN_COL_NAME_NAME
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
                                + MainDbAdapter.GEN_COL_NAME_NAME
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
            showNotification();
            SharedPreferences.Editor editor = mPreferences.edit();
            editor.putBoolean("isGpsTrackOn", true);
            editor.commit();
        }
        catch(IOException ex) {
            Logger.getLogger(GPSTrackService.class.getName()).log(Level.SEVERE, null, ex);
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
            stopSelf();
        }
        //close the database
        if(mMainDbAdapter != null){
            mMainDbAdapter.close();
            mMainDbAdapter = null;
        }
    }

    private void createKMLHeader(){
        try{
            gpsTrackDetailKMLFileWriter.append(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<kml xmlns=\"http://earth.google.com/kml/2.0\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n"
                + "<Document>\n"
                + "<atom:author><atom:name>Tracks running on AndiCar</atom:name></atom:author>\n"
                + "<name><![CDATA[" + gpsTrackDetailKMLFile.getName() + "]]></name>\n"
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
                    + "<name><![CDATA[" + gpsTrackDetailGPXFile.getName() +"]]></name>\n"
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
        try{
            gpsTrackDetailKMLFileWriter.append(
                 "<coordinates>" + currentLocationLongitude + "," + currentLocationLatitude + "," + currentLocationAltitude + "</coordinates>\n"
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
        try{
            gpsTrackDetailKMLFileWriter.append(
                "\n</coordinates>\n"
                + "</LineString>\n"
                + "</MultiGeometry>\n"
                + "</Placemark>\n"
                + "<Placemark>\n"
                  + "<name><![CDATA[(End)]]></name>\n"
                  + "<description><![CDATA[Created by <a href='http://sites.google.com/site/andicarfree'>AndiCar</a>."
                  + "<p>Total Distance: " + (totalDistance / 1000.00) + "km\n" //(2.2 mi)<br>Total Time: 10:57<br>Moving Time: 8:12<br>Average Speed: 21.20 km/h (13.2 mi/h)<br>Average Moving Speed: 26.38 km/h (16.4 mi/h)<br>Max Speed: 59.40 km/h (36.9 mi/h)<br>Min Elevation: 588 m (1927 ft)<br>Max Elevation: 605 m (1986 ft)<br>Elevation Gain: 25 m (81 ft)<br>Max Grade: 5 %<br>Min Grade: -2 %<br>Recorded: Thu Apr 08 13:11:35 GMT+02:00 2010<br>Activity type: -<br><img border="0" src="http://chart.apis.google.com/chart?&chs=600x350&cht=lxy&chtt=Elevation&chxt=x,y&chxr=0,0,3,0%257C1,500.0,700.0,25&chco=009A00&chm=B,00AA00,0,0,0&chg=100000,12.5,1,0&chd=e:,"/>
                      + "]]></description>\n"
                  + "<styleUrl>#sh_red-circle</styleUrl>\n"
                  + "<Point>\n"
                    + "<coordinates>" + currentLocationLongitude + "," + currentLocationLatitude + "," + currentLocationAltitude + "</coordinates>\n"
                  + "</Point>\n"
                + "</Placemark>\n"
                + "</Document>\n"
                + "</kml>\n");
        }
        catch(IOException e){

        }
    }

    private void appendGPXFooter(){
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
        //close the database
        if(mMainDbAdapter != null){
            mMainDbAdapter.close();
            mMainDbAdapter = null;
        }
        try {
            gpsTrackDetailCSVFileWriter.flush();
            appendKMLFooter();
            gpsTrackDetailKMLFileWriter.flush();
            appendGPXFooter();
            gpsTrackDetailGPXFileWriter.flush();
        }
        catch(IOException ex) {
            Logger.getLogger(GPSTrackService.class.getName()).log(Level.SEVERE, null, ex);
            Toast.makeText(this, "Closing file failed!", Toast.LENGTH_LONG).show();
        }
        
        mLocationManager.removeUpdates(mLocationListener);
        mLocationManager = null;
        
        // Tell the user we stopped.
        Toast.makeText(this, R.string.SERVICE_GPSTRACK_SERVICESTOPPED_MESSAGE, Toast.LENGTH_SHORT).show();

        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean("isGpsTrackOn", false);
        editor.commit();
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
    private void showNotification() {
        CharSequence text = getText(R.string.GPSTRACK_SERVICE_GPSTRACKSERVICEINPROGRESS_MESSAGE);

        // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(R.drawable.andicar_gps_anim, text,
                System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, GPSTrackController.class), 0);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, getText(R.string.SERVICE_GPSTRACK_LABEL),
                       text, contentIntent);

        // Send the notification.
        // We use a layout id because it is a unique number.  We use it later to cancel.
        mNM.notify(R.string.GPSTRACK_SERVICE_GPSTRACKSERVICEINPROGRESS_MESSAGE, notification);
    }

    private class AndiCarLocationListener implements LocationListener
    {
        @Override
        public void onLocationChanged(Location loc) {
            if(gpsTrackDetailCSVFileWriter == null){
                Toast.makeText(GPSTrackService.this, "No File Writer!", Toast.LENGTH_LONG).show();
                stopSelf();
            }
            if (loc != null) {
                try {
                    //create gps detail 
                    currentLocationLatitude = loc.getLatitude();
                    currentLocationLongitude = loc.getLongitude();
                    currentLocationAltitude = loc.getAltitude();
                    currentLocationTime = loc.getTime();
                    if(oldLocationLatitude != 0) {
                        Location.distanceBetween(oldLocationLatitude, oldLocationLongitude, currentLocationLatitude, currentLocationLongitude, distanceArray);
                        distanceBetweenLocations = distanceArray[0];
                        totalDistance = totalDistance + distanceBetweenLocations;
                    }
                    else{
                        //write the starting point
                        appendKMLStartPoint();
                    }
                    gpsTrackDetailCSVFileWriter.append(gpsTrackId + ","
                                                        + loc.getAccuracy() + ","
                                                        + currentLocationAltitude + ","
                                                        + currentLocationLatitude + ","
                                                        + currentLocationLongitude + ","
                                                        + loc.getSpeed() + ","
                                                        + currentLocationTime + ","
                                                        + distanceBetweenLocations + ","
                                                        + loc.getBearing() + "\n");
                    gpsTrackDetailKMLFileWriter.append(currentLocationLongitude + ","
                                                + currentLocationLatitude + ","
                                                + currentLocationAltitude + " ");

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

                    oldLocationLatitude = currentLocationLatitude;
                    oldLocationLongitude = currentLocationLongitude;
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
