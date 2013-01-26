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

import org.andicar.activity.BaseActivity;
import org.andicar2.activity.R;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;


/**
 *
 * @author miki
 */
public class AboutActivity extends BaseActivity{

	protected TextView tvAbout1;
	protected TextView tvAbout2;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.about_activity_s01);
        

        tvAbout1 = (TextView)findViewById(R.id.tvAbout1);
        tvAbout1.setMovementMethod(LinkMovementMethod.getInstance());

        tvAbout2 = (TextView)findViewById(R.id.tvAbout2);
        tvAbout2.setMovementMethod(LinkMovementMethod.getInstance());
    }

	/* (non-Javadoc)
	 * @see org.andicar.activity.BaseActivity#setSpecificLayout()
	 */
	@Override
	public void setSpecificLayout() {
	}
}
