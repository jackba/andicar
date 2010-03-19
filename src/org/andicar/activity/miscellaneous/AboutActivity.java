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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.andicar.paypal.GetPayPalActivity;
import org.andicar.activity.EditActivityBase;
import org.andicar.activity.R;
import org.andicar.utils.StaticValues;


/**
 *
 * @author miki
 */
public class AboutActivity extends EditActivityBase {

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.about_activity);
        LinearLayout mLinearLayout = (LinearLayout) findViewById(R.id.aboutLinearLayout);
        mLinearLayout.setOrientation(LinearLayout.VERTICAL);
        mLinearLayout.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView tw1 = new TextView(this);
        tw1.setText(Html.fromHtml(StaticValues.LM_COPYRIGHT_HTML1));
        tw1.setMovementMethod(LinkMovementMethod.getInstance());
        tw1.setGravity(Gravity.FILL);
        mLinearLayout.addView(tw1);

        ImageView i = new ImageView(this);
        i.setImageResource(R.drawable.btn_donate);
        i.setAdjustViewBounds(true); // set the ImageView bounds to match the Drawable's dimensions
        i.setLayoutParams(new Gallery.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        mLinearLayout.addView(i);
        i.setOnTouchListener(new OnTouchListener() {

                public boolean onTouch(View arg0, MotionEvent arg1) {
                    GetPayPalActivity ppa = new GetPayPalActivity();
                    Intent i = ppa.getPayPalIntent();
                    startActivity(i);
                    return true;
                }
            });
        TextView tw2 = new TextView(this);
        tw2.setText(Html.fromHtml(StaticValues.LM_COPYRIGHT_HTML2));
        tw2.setMovementMethod(LinkMovementMethod.getInstance());
        tw2.setGravity(Gravity.FILL);
        mLinearLayout.addView(tw2);

    }

}
