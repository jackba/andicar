 /*
 * AndiCar - car management software for Android powered devices
 * Copyright (C) 2011 Miklos Keresztes (miklos.keresztes@gmail.com)
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
package org.andicar.activity.dialog;

import org.andicar2.activity.R;

import android.app.AlertDialog.Builder;
import android.content.Context;

/**
 * @author Miklos Keresztes
 *
 */
public class AndiCarDialogBuilder extends Builder {
	
	public static final int DIALOGTYPE_ERROR = 0;
	public static final int DIALOGTYPE_WARNING = 1;
	public static final int DIALOGTYPE_INFO = 2;
	public static final int DIALOGTYPE_SEARCH = 3;
	public static final int DIALOGTYPE_QUESTION = 4;
	public static final int DIALOGTYPE_CAR = 5;

	/**
	 * @param arg0
	 * @param dialogType error, warning, info, search
	 */
	public AndiCarDialogBuilder(Context arg0, int dialogType, CharSequence title) {
		super(arg0);
		
		if(title != null)
			setTitle(title);
		switch (dialogType) {
			case DIALOGTYPE_ERROR:
				setIcon(R.drawable.icon_dialog_error1_32x32);
				break;
			case DIALOGTYPE_WARNING:
				setIcon(R.drawable.icon_dialog_warning2_32x32);
				break;
			case DIALOGTYPE_INFO:
				setIcon(R.drawable.icon_dialog_info1_32x32);
				break;
			case DIALOGTYPE_SEARCH:
				setIcon(R.drawable.icon_dialog_search1_32x32);
				break;
			case DIALOGTYPE_QUESTION:
				setIcon(R.drawable.icon_dialog_question1_32x32);
				break;
			case DIALOGTYPE_CAR:
				setIcon(R.drawable.icon_dialog_car_32x32);
				break;
			default:
				break;
		}
	}

}
