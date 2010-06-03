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
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.andicar.paypal.GetPayPalActivity;
import org.andicar.activity.R;
import org.andicar.utils.StaticValues;
import android.os.Vibrator;
import android.content.Context;
import java.util.HashMap;
import java.util.Map;
import org.andicar.activity.BaseActivity;
import org.andicar.utils.AndiCarStatistics;


/**
 *
 * @author miki
 */
public class AboutActivity extends BaseActivity{

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.about_activity);

        TextView tvAbout1 = (TextView)findViewById(R.id.tvAbout1);
        CharSequence abt = mResource.getString(R.string.LM_COPYRIGHT_HTML1);

//        tvAbout1.setText(Html.fromHtml(StaticValues.LM_COPYRIGHT_HTML1));
        tvAbout1.setText(Html.fromHtml(abt.toString()));
        tvAbout1.setMovementMethod(LinkMovementMethod.getInstance());

        ImageView imgEUR = (ImageView)findViewById(R.id.aboutDonateEUR);
        imgEUR.setImageResource(R.drawable.btn_donate);
        imgEUR.setOnClickListener(mDonateClickListener);

        ImageView imgUSD = (ImageView)findViewById(R.id.aboutDonateUSD);
        imgUSD.setImageResource(R.drawable.btn_donate);
        imgUSD.setOnClickListener(mDonateClickListener);

        TextView tvAbout2 = (TextView)findViewById(R.id.tvAbout2);
        abt = mResource.getString(R.string.LM_COPYRIGHT_HTML2);
        tvAbout2.setText(Html.fromHtml(abt.toString()));
        tvAbout2.setMovementMethod(LinkMovementMethod.getInstance());
    }

    View.OnClickListener mDonateClickListener = new View.OnClickListener() {
        public  void onClick(View v) {

            ImageView srcImg = (ImageView)v;
            String payPalCurrency = "";
            if(srcImg.getId() == R.id.aboutDonateEUR){
                payPalCurrency = "EUR";
                if(isSendStatistics){
                    Map<String, String> parameters = new HashMap<String, String>();
                    parameters.put("Currency", "Eur");
                    AndiCarStatistics.sendFlurryEvent("PayPal", parameters);
                }
            }
            else if(srcImg.getId() == R.id.aboutDonateUSD){
                payPalCurrency = "USD";
                if(isSendStatistics){
                    Map<String, String> parameters = new HashMap<String, String>();
                    parameters.put("Currency", "USD");
                    AndiCarStatistics.sendFlurryEvent("PayPal", parameters);
                }
            }
            Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(100);
            GetPayPalActivity ppa = new GetPayPalActivity();
            Intent i = ppa.getPayPalIntent(payPalCurrency);
            startActivity(i);
        }
    };
}
