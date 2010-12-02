 /**
 * AndiCar - car management software for Android powered devices
 * Copyright (C) 2010 Miklos Keresztes (miklos.keresztes@gmail.com)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT AY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.andicar.activity.miscellaneous;

import org.andicar.activity.R;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

/**
 * @author Miklos Keresztes
 *
 */
public class MessageActivity extends Activity {

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.message_activity);
		Bundle bundle = getIntent().getExtras();

		TextView tvMessageTitle = (TextView)findViewById(R.id.tvMessageTitle); 
		TextView tvMessageBody1 = (TextView)findViewById(R.id.tvMessageBody1);
		tvMessageTitle.setText(bundle.getString("MessageTitle"));
		tvMessageBody1.setText(bundle.getString("MessageBody"));
		int lMessageId = bundle.getInt("LongMessageID");
		if(lMessageId != -1){
			TextView tvMessageBody2 = (TextView)findViewById(R.id.tvMessageBody2);
			tvMessageBody2.setText(lMessageId);
		}
	}
	

}
