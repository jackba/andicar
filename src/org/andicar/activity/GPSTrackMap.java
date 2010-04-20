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

package org.andicar.activity;

import android.os.Bundle;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import org.andicar.activity.R;

/**
 *
 * @author Miklos Keresztes
 */
public class GPSTrackMap extends MapActivity {

    /*
     * Map Api Keys:
     * 1. 0aQTdJnsQSHfbEz5axy7VixTxQu4UkJkLgdkbjA a
     * 2. 0exdzR1McxKRFeUqK0G7bPCA4BhI8LfOjj-lDrg b
     * 3. 0aQTdJnsQSHdoTbAJ0paNl9sntpqEw8hVG6nhRg r
     */
    

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gps_track_map);
        MapView mapView = (MapView) findViewById(R.id.gpstrackmap);
        mapView.setBuiltInZoomControls(true);
    }
}
