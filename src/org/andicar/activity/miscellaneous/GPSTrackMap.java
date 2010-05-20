/*
 *  AndiCar - car management software for Android powered devices
 *  Copyright (C) 2010 Miklos Keresztes (miklos.keresztes@gmail.com)
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT AY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.andicar.activity.miscellaneous;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.andicar.activity.R;
import org.andicar.persistence.FileUtils;
import org.andicar.utils.StaticValues;

/**
 *
 * @author Miklos Keresztes
 */
public class GPSTrackMap extends MapActivity implements Runnable{
    private Bundle mExtras;
    private SharedPreferences mPreferences;
    protected Resources mResource = null;
    protected AlertDialog.Builder madbErrorAlert;
    protected AlertDialog madError;
    protected ArrayList<GeoPoint> trackPoints;
    protected Projection projection;
    protected Path path = null;
    protected Paint mPaint = null;
    protected int maxLatitude = 0;
    protected int minLatitude = 0;
    protected int maxLongitude = 0;
    protected int minLongitude = 0;
    protected MapView mapView;
    protected List<Overlay> mapOverlays;
    protected MapController mMapController;
    protected String trackId;
    protected ProgressDialog progressDialog;
    private String showMode = "M";
    protected Menu optionsMenu;

    /*
     * Map Api Keys:
     * 1. 0aQTdJnsQSHfbEz5axy7VixTxQu4UkJkLgdkbjA a
     * 2. 0exdzR1McxKRFeUqK0G7bPCA4BhI8LfOjj-lDrg b
     * 3. 0aQTdJnsQSHdoTbAJ0paNl9sntpqEw8hVG6nhRg r
     */
    

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gps_track_map);
        mapView = (MapView) findViewById(R.id.gpstrackmap);

        mapView.setBuiltInZoomControls(true);
        projection = mapView.getProjection();

        mapOverlays = mapView.getOverlays();
        mExtras = getIntent().getExtras();
        mResource = getResources();
        mPreferences = getSharedPreferences(StaticValues.GLOBAL_PREFERENCE_NAME, 0);
        showMode = mPreferences.getString("GPSTrackShowMode", "M");

        if(trackPoints == null)
            trackPoints = new ArrayList<GeoPoint>();
        //get the the track id
        trackId = mExtras.getString("gpsTrackId");
//        trackId ="99";
        progressDialog = ProgressDialog.show(GPSTrackMap.this, "",
            mResource.getString(R.string.GPSTrackShowOnMap_ProgressMessage), true);
        Thread thread = new Thread(GPSTrackMap.this);
        thread.start();

    }

    public void run() {
        drawTrack();
    }

    private Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == 0)
                    progressDialog.dismiss();
                else{
                    progressDialog.dismiss();
                    madbErrorAlert = new AlertDialog.Builder( GPSTrackMap.this );
                    madbErrorAlert.setCancelable( false );
                    madbErrorAlert.setPositiveButton( mResource.getString(R.string.GEN_OK), null );
                    madbErrorAlert.setMessage(mResource.getString(msg.what));
                    madError = madbErrorAlert.create();
                    madError.show();
                }
            }
    };

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    private void drawTrack(){
        FileInputStream trackInputStream;
        DataInputStream trackData;
        BufferedReader trackBufferedReader;
        String trackLine;
        ArrayList<String> trackFiles;
        if(trackPoints.size() == 0){ //trackpoints are not loaded
            //get the list of gop files
//            trackId = "99";
            trackFiles = FileUtils.getFileNames(StaticValues.TRACK_FOLDER, trackId + "_[0-9][0-9][0-9].gop");
            if(trackFiles.isEmpty()){
                handler.sendEmptyMessage(R.string.ERR_036);
            }
            mMapController = mapView.getController();
            int latitudeE6;
            int longitudeE6;
            int c1;
            boolean isFirst = true;
            for(String trackFile : trackFiles) {
                try{
                    trackFile = StaticValues.TRACK_FOLDER + trackFile;
                    trackInputStream = new FileInputStream(trackFile);
                    trackData = new DataInputStream(trackInputStream);
                    trackBufferedReader = new BufferedReader(new InputStreamReader(trackData));
                    while ((trackLine = trackBufferedReader.readLine()) != null) {
                        if(trackLine.length() == 0 ||
                                trackLine.contains("Latitude")) //header line
                            continue;
                        c1 = trackLine.indexOf(",");
                        if(isFirst){
                            latitudeE6 = Integer.parseInt(trackLine.substring(0, c1));
                            longitudeE6 = Integer.parseInt(trackLine.substring(c1 + 1, trackLine.length()));
                            maxLatitude = latitudeE6;
                            minLatitude = maxLatitude;
                            maxLongitude = longitudeE6;
                            minLongitude = maxLongitude;
                            trackPoints.add(new GeoPoint(latitudeE6, longitudeE6));
                            isFirst = false;
                            continue;
                        }
                        latitudeE6 = Integer.parseInt(trackLine.substring(0, c1));
                        longitudeE6 = Integer.parseInt(trackLine.substring(c1 + 1, trackLine.length()));
                        trackPoints.add(new GeoPoint(latitudeE6, longitudeE6));
                        if( latitudeE6 > maxLatitude)
                            maxLatitude = latitudeE6;
                        if(latitudeE6 < minLatitude)
                            minLatitude = latitudeE6;
                        if(longitudeE6 > maxLongitude)
                            maxLongitude = longitudeE6;
                        if(longitudeE6 < minLongitude)
                            minLongitude = longitudeE6;
                    }
                    trackInputStream.close();
                    trackData.close();
                    trackBufferedReader.close();
                }
                catch(FileNotFoundException e){}
                catch(IOException e){}
            }
        }

        if(trackPoints.size() >= 1){
            mapOverlays.add(new DrawableMapOverlay(this, trackPoints.get(0), R.drawable.icon_start_point, "Start"));
            mapOverlays.add(new TrackRouteOverlay());
            mapOverlays.add(new DrawableMapOverlay(this, trackPoints.get(trackPoints.size() - 1),
                    R.drawable.icon_stop_point, "Stop"));
            mapView.postInvalidate();
            mMapController.zoomToSpan(
                          (maxLatitude - minLatitude),
                          (maxLongitude - minLongitude));
            // Animate to the center cluster of points
            mMapController.animateTo(new GeoPoint(
                           (maxLatitude + minLatitude)/2,
                           (maxLongitude + minLongitude)/2 ));
            if(showMode.equals("M"))
                mapView.setSatellite(false);
            else
                mapView.setSatellite(true);
        }
        handler.sendEmptyMessage(0);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        optionsMenu = menu;
        if(showMode.equals("M"))
            optionsMenu.add(0, StaticValues.GPSTRACKMAP_MENU_SATELLITEMODE_ID, 0,
                mResource.getText(R.string.GPSTrackShowOnMapMenu_SatelliteMode)).setIcon(mResource.getDrawable(R.drawable.ic_menu_mapmode));
        else
            optionsMenu.add(0, StaticValues.GPSTRACKMAP_MENU_MAPMODE_ID, 0,
                mResource.getText(R.string.GPSTrackShowOnMapMenu_MapMode)).setIcon(mResource.getDrawable(R.drawable.ic_menu_mapmode));
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == StaticValues.GPSTRACKMAP_MENU_SATELLITEMODE_ID){ //currently in map mode
            showMode = "S";
            mapView.setSatellite(true);
            optionsMenu.removeItem(StaticValues.GPSTRACKMAP_MENU_SATELLITEMODE_ID);
            optionsMenu.add(0, StaticValues.GPSTRACKMAP_MENU_MAPMODE_ID, 0,
                mResource.getText(R.string.GPSTrackShowOnMapMenu_MapMode)).setIcon(mResource.getDrawable(R.drawable.ic_menu_mapmode));
            SharedPreferences.Editor editor = mPreferences.edit();
            editor.putString("GPSTrackShowMode", "S");
            editor.commit();

        }
        else{
            showMode = "M";
            mapView.setSatellite(false);
            optionsMenu.removeItem(StaticValues.GPSTRACKMAP_MENU_MAPMODE_ID);
            optionsMenu.add(0, StaticValues.GPSTRACKMAP_MENU_SATELLITEMODE_ID, 0,
                mResource.getText(R.string.GPSTrackShowOnMapMenu_SatelliteMode)).setIcon(mResource.getDrawable(R.drawable.ic_menu_mapmode));
            SharedPreferences.Editor editor = mPreferences.edit();
            editor.putString("GPSTrackShowMode", "M");
            editor.commit();
        }

        return false;
    }

    class TrackRouteOverlay extends Overlay{
        public TrackRouteOverlay(){
        }

        @Override
        public void draw(Canvas canvas, MapView mapv, boolean shadow){
            super.draw(canvas, mapv, shadow);

            path = new Path();
            mPaint = new Paint();
            mPaint.setDither(true);
            mPaint.setColor(Color.RED);
            mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            mPaint.setStrokeJoin(Paint.Join.ROUND);
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            mPaint.setStrokeWidth(2);

            Point p1 = new Point();
            Point p2 = new Point();

            GeoPoint gp1 = null;

            for(GeoPoint gp : trackPoints){
                if(gp1 == null)
                    gp1 = gp;
                else{
                        projection.toPixels(gp1, p1);
                        projection.toPixels(gp, p2);
                        path.moveTo(p1.x, p1.y);
                        path.lineTo(p2.x,p2.y);
                        gp1 = gp;
                }
            }
            canvas.drawPath(path, mPaint);
        }
    }

    public class DrawableMapOverlay extends Overlay {

        private final GeoPoint geoPoint;
        private final Context context;
        private final int drawable;
        private String text;

        /**
         * @param context the context in which to display the overlay
         * @param geoPoint the geographical point where the overlay is located
         * @param drawable the ID of the desired drawable
         */
        public DrawableMapOverlay(Context context, GeoPoint geoPoint, int drawable, String text) {
            this.context = context;
            this.geoPoint = geoPoint;
            this.drawable = drawable;
            this.text = text;
        }

        @Override
        public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when) {
            super.draw(canvas, mapView, shadow);

            // Convert geo coordinates to screen pixels
            Point screenPoint = new Point();
            mapView.getProjection().toPixels(geoPoint, screenPoint);

            // Read the image
            Bitmap markerImage = BitmapFactory.decodeResource(context.getResources(), drawable);

            // Draw it, centered around the given coordinates
            canvas.drawBitmap(markerImage,
                    screenPoint.x - markerImage.getWidth() / 2,
                    screenPoint.y - markerImage.getHeight()/* / 2*/, null);
            if(text != null){
                Paint mTextPaint = new Paint();
                mTextPaint.setAntiAlias(true);
                mTextPaint.setTextSize(16);
                mTextPaint.setColor(0xFF000000);
//                setPadding(3, 3, 3, 3);

                canvas.drawText(text, screenPoint.x - markerImage.getWidth() / 2,
                                    screenPoint.y + mTextPaint.getTextSize(), mTextPaint);
            }
            return true;
        }

        @Override
        public boolean onTap(GeoPoint p, MapView mapView) {
            // Handle tapping on the overlay here
            return true;
        }
    }
}