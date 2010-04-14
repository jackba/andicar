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
import org.andicar.activity.MainActivity;
import org.andicar.activity.R;
import org.andicar.persistence.MainDbAdapter;
import org.andicar.utils.StaticValues;

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
    private long gpsTrackId = 0;
    private String gpsDetailInsertStr = "";

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
            LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);

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

        // Display a notification about us starting.  We put an icon in the status bar.
        showNotification();
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean("isGpsTrackOn", true);
        editor.commit();
    }


    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        mNM.cancel(R.string.MAIN_ACTIVITY_GPSTRACKSERVICESTARTED_MESSAGE);
        if(mMainDbAdapter != null){
            mMainDbAdapter.close();
            mMainDbAdapter = null;
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
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(R.string.MAIN_ACTIVITY_GPSTRACKSERVICESTARTED_MESSAGE);

        // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(R.drawable.andicar_gps_anim, text,
                System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, getText(R.string.SERVICE_GPSTRACK_LABEL),
                       text, contentIntent);

        // Send the notification.
        // We use a layout id because it is a unique number.  We use it later to cancel.
        mNM.notify(R.string.MAIN_ACTIVITY_GPSTRACKSERVICESTARTED_MESSAGE, notification);
    }

    private class AndiCarLocationListener implements LocationListener
    {
        @Override
        public void onLocationChanged(Location loc) {
            if (loc != null) {
                //create gps detail record
                //use direct table insert for increasing the speed of the DB operation
                gpsDetailInsertStr = "INSERT INTO " + MainDbAdapter.GPSTRACKDETAIL_TABLE_NAME
                            + " ( "
                                + MainDbAdapter.GEN_COL_NAME_NAME + ", "
                                + MainDbAdapter.GPSTRACKDETAIL_COL_GPSTRACK_ID_NAME + ", "
                                + MainDbAdapter.GPSTRACKDETAIL_COL_ACCURACY_NAME + ", "
                                + MainDbAdapter.GPSTRACKDETAIL_COL_ALTITUDE_NAME + ", "
                                + MainDbAdapter.GPSTRACKDETAIL_COL_LATITUDE_NAME + ", "
                                + MainDbAdapter.GPSTRACKDETAIL_COL_LONGITUDE_NAME + ", "
                                + MainDbAdapter.GPSTRACKDETAIL_COL_SPEED_NAME + ", "
                                + MainDbAdapter.GPSTRACKDETAIL_COL_TIME_NAME + ", "
                                + MainDbAdapter.GPSTRACKDETAIL_COL_DISTNACE_NAME + ", "
                                + MainDbAdapter.GPSTRACKDETAIL_COL_BEARING_NAME + " "
                            + " ) "
                            + " VALUES ( "
                                + " '" + loc.toString() + "', "
                                + gpsTrackId + ", "
                                + loc.getAccuracy() + ", "
                                + loc.getAltitude() + ", "
                                + loc.getLatitude() + ", "
                                + loc.getLongitude() + ", "
                                + loc.getSpeed() + ", "
                                + loc.getTime() + ", "
                                + " 0 " + ", "
                                + loc.getBearing()
                            + " ) ";
                mMainDbAdapter.execSql(gpsDetailInsertStr);
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onStatusChanged(String provider, int status,
            Bundle extras) {
            // TODO Auto-generated method stub
        }
    }

}
