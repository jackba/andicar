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

package org.andicar.activity;

import android.content.Context;
import android.os.Bundle;
import org.andicar.persistence.MainDbAdapter;
import org.andicar.utils.StaticValues;
import org.andicar2.activity.R;

/**
 *
 * @author miki
 */
public class CommonListActivity extends ListActivityBase
{

    /** Called when the activity is first created. */
    @Override
    public void onCreate( Bundle icicle )
    {
        if(mBundleExtras == null)
            mBundleExtras = getIntent().getExtras();
    	
    	String listSource = mBundleExtras.getString("ListSource");
    	
        if(mPreferences == null)
        	mPreferences = getSharedPreferences(StaticValues.GLOBAL_PREFERENCE_NAME, Context.MODE_MULTI_PROCESS);

        initStyle();
    	
    	if(listSource.equals(MainDbAdapter.TABLE_NAME_CAR)){
	        super.onCreate( icicle, null, CarEditActivity.class, null,
	                MainDbAdapter.TABLE_NAME_CAR, MainDbAdapter.COL_LIST_CAR_TABLE, null, MainDbAdapter.COL_NAME_GEN_NAME,
	                simpleListItem2, new String[]{MainDbAdapter.COL_NAME_GEN_NAME}, new int[]{android.R.id.text1}, null);
	        setTitle(R.string.APP_Activity_CarList);
    	}
    	else if(listSource.equals(MainDbAdapter.TABLE_NAME_DRIVER)){
            super.onCreate( icicle, null, DriverEditActivity.class, null,
                    MainDbAdapter.TABLE_NAME_DRIVER, MainDbAdapter.COL_LIST_DRIVER_TABLE, null, MainDbAdapter.COL_NAME_GEN_NAME,
                    simpleListItem2,
                    new String[]{MainDbAdapter.COL_NAME_GEN_NAME}, new int[]{android.R.id.text1}, null);
	        setTitle(R.string.APP_Activity_DriverList);
    	}
    	else if(listSource.equals(MainDbAdapter.TABLE_NAME_BPARTNER)){
            super.onCreate( icicle, null, BPartnerEditActivity.class, null,
                    MainDbAdapter.TABLE_NAME_BPARTNER, MainDbAdapter.COL_LIST_BPARTNER_TABLE, null, MainDbAdapter.COL_NAME_GEN_NAME, 
                    simpleListItem2,
                    new String[]{MainDbAdapter.COL_NAME_GEN_NAME}, new int[]{android.R.id.text1}, null);
	        setTitle(R.string.APP_Activity_BPartnerList);
    	}
    	
    	
    	else if(listSource.equals(MainDbAdapter.TABLE_NAME_TASK)){
            super.onCreate( icicle, null, TaskEditActivity.class, null,
                    MainDbAdapter.TABLE_NAME_TASK, MainDbAdapter.COL_LIST_TASK_TABLE, null, MainDbAdapter.COL_NAME_GEN_NAME,
                    twolineListActivity,
                    new String[]{MainDbAdapter.COL_NAME_GEN_NAME, MainDbAdapter.COL_NAME_GEN_USER_COMMENT},
                    new int[]{R.id.tvTwoLineListText1, R.id.tvTwoLineListText2}, null);
	        setTitle(R.string.APP_Activity_TaskList);
    	} 
    	else if(listSource.equals(MainDbAdapter.TABLE_NAME_TASKTYPE)){
            super.onCreate( icicle, null, TaskTypeEditActivity.class, null,
                    MainDbAdapter.TABLE_NAME_TASKTYPE, MainDbAdapter.COL_LIST_TASKTYPE_TABLE, null, MainDbAdapter.COL_NAME_GEN_NAME,
                    twolineListActivity,
                    new String[]{MainDbAdapter.COL_NAME_GEN_NAME, MainDbAdapter.COL_NAME_GEN_USER_COMMENT},
                    new int[]{R.id.tvTwoLineListText1, R.id.tvTwoLineListText2}, null);
	        setTitle(R.string.APP_Activity_TaskTypeList);
    	} 
    	else if(listSource.equals(MainDbAdapter.TABLE_NAME_UOM)){
            super.onCreate( icicle, null, UOMEditActivity.class, null,
                    MainDbAdapter.TABLE_NAME_UOM, MainDbAdapter.COL_LIST_UOM_TABLE, 
                    null, MainDbAdapter.COL_NAME_GEN_NAME,
                    twolineListActivity,
                    new String[]{MainDbAdapter.COL_NAME_GEN_NAME, MainDbAdapter.COL_NAME_UOM__CODE},
                    new int[]{R.id.tvTwoLineListText1, R.id.tvTwoLineListText2}, null);
	        setTitle(R.string.APP_Activity_UOMList);
    	} 
    	else if(listSource.equals(MainDbAdapter.TABLE_NAME_EXPENSECATEGORY)){
    		if(!mBundleExtras.getBoolean("IsFuel")){
    	        super.onCreate( icicle, null, ExpenseCategoryEditActivity.class, null,
    	                MainDbAdapter.TABLE_NAME_EXPENSECATEGORY, MainDbAdapter.COL_LIST_EXPENSECATEGORY_TABLE, MainDbAdapter.COL_NAME_EXPENSECATEGORY__ISFUEL + "='N'",
    	                MainDbAdapter.COL_NAME_GEN_NAME,
    	                twolineListActivity,
    	                new String[]{MainDbAdapter.COL_NAME_GEN_NAME, MainDbAdapter.COL_NAME_GEN_USER_COMMENT},
    	                new int[]{R.id.tvTwoLineListText1, R.id.tvTwoLineListText2}, null);
    	        setTitle(R.string.APP_Activity_ExpenseCategoryList);
    		}
    		else{
    	        super.onCreate( icicle, null, ExpenseCategoryEditActivity.class, null,
    	                MainDbAdapter.TABLE_NAME_EXPENSECATEGORY, MainDbAdapter.COL_LIST_EXPENSECATEGORY_TABLE, MainDbAdapter.COL_NAME_EXPENSECATEGORY__ISFUEL + "='Y'",
    	                MainDbAdapter.COL_NAME_GEN_NAME,
    	                twolineListActivity,
    	                new String[]{MainDbAdapter.COL_NAME_GEN_NAME, MainDbAdapter.COL_NAME_GEN_USER_COMMENT},
    	                new int[]{R.id.tvTwoLineListText1, R.id.tvTwoLineListText2}, null);
    	        setTitle(R.string.APP_Activity_FuelCategoryList);
    		}
    	} 
    	else if(listSource.equals(MainDbAdapter.TABLE_NAME_EXPENSETYPE)){
            super.onCreate( icicle, null, ExpenseTypeEditActivity.class, null,
                    MainDbAdapter.TABLE_NAME_EXPENSETYPE, MainDbAdapter.COL_LIST_EXPENSETYPE, null, MainDbAdapter.COL_NAME_GEN_NAME,
                    twolineListActivity,
                    new String[]{MainDbAdapter.COL_NAME_GEN_NAME, MainDbAdapter.COL_NAME_GEN_USER_COMMENT},
                    new int[]{R.id.tvTwoLineListText1, R.id.tvTwoLineListText2}, null);
	        setTitle(R.string.APP_Activity_ExpenseTypeList);
    	} 
    	else if(listSource.equals(MainDbAdapter.TABLE_NAME_CURRENCY)){
            super.onCreate( icicle, null, CurrencyEditActivity.class, null,
                    MainDbAdapter.TABLE_NAME_CURRENCY, MainDbAdapter.COL_LIST_CURRENCY_TABLE, null, MainDbAdapter.COL_NAME_GEN_NAME,
                    twolineListActivity,
                    new String[]{MainDbAdapter.COL_NAME_GEN_NAME, MainDbAdapter.COL_NAME_CURRENCY__CODE},
                    new int[]{R.id.tvTwoLineListText1, R.id.tvTwoLineListText2}, null);
	        setTitle(R.string.APP_Activity_CurrencyList);
    	} 
    	else if(listSource.equals(MainDbAdapter.TABLE_NAME_TAG)){
            super.onCreate( icicle, null, TagEditActivity.class, null,
                    MainDbAdapter.TABLE_NAME_TAG, MainDbAdapter.COL_LIST_TAG_TABLE, null, MainDbAdapter.COL_NAME_GEN_NAME,
                    twolineListActivity,
                    new String[]{MainDbAdapter.COL_NAME_GEN_NAME, MainDbAdapter.COL_NAME_GEN_USER_COMMENT},
                    new int[]{R.id.tvTwoLineListText1, R.id.tvTwoLineListText2}, null);
	        setTitle(R.string.APP_Activity_TagList);
    	} 

    	else if(listSource.equals(MainDbAdapter.TABLE_NAME_UOMCONVERSION)){
            super.onCreate( icicle, null, UOMConversionEditActivity.class, null,
                    MainDbAdapter.TABLE_NAME_UOMCONVERSION, MainDbAdapter.COL_LIST_UOMCONVERSION_TABLE,
                    null, MainDbAdapter.COL_NAME_GEN_NAME,
                    threeLineListActivity,
                    new String[]{MainDbAdapter.COL_NAME_GEN_NAME, MainDbAdapter.COL_NAME_GEN_USER_COMMENT, MainDbAdapter.COL_NAME_UOMCONVERSION__RATE},
                    new int[]{R.id.tvThreeLineListText1, R.id.tvThreeLineListText2, R.id.tvThreeLineListText3}, null);
	        setTitle(R.string.APP_Activity_UOMConversionList);
    	} 
    	else if(listSource.equals(MainDbAdapter.TABLE_NAME_CURRENCYRATE)){
            super.onCreate( icicle, null, CurrencyRateEditActivity.class, null,
                    MainDbAdapter.TABLE_NAME_CURRENCYRATE, MainDbAdapter.COL_LIST_CURRENCYRATE_TABLE, null,
                    MainDbAdapter.COL_NAME_GEN_NAME,
                    threeLineListActivity,
                    new String[]{MainDbAdapter.COL_NAME_GEN_NAME, MainDbAdapter.COL_NAME_CURRENCYRATE__RATE, MainDbAdapter.COL_NAME_CURRENCYRATE__INVERSERATE},
                    new int[]{R.id.tvThreeLineListText1, R.id.tvThreeLineListText2, R.id.tvThreeLineListText3}, null);
	        setTitle(R.string.APP_Activity_CurrencyRateList);
    	} 
    }
}
