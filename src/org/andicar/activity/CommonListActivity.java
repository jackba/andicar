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

import android.os.Bundle;
import org.andicar.persistence.MainDbAdapter;
import org.andicar.utils.StaticValues;

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
        	mPreferences = getSharedPreferences(StaticValues.GLOBAL_PREFERENCE_NAME, 0);

        initStyle();
    	
    	if(listSource.equals(MainDbAdapter.CAR_TABLE_NAME)){
	        super.onCreate( icicle, null, CarEditActivity.class, null,
	                MainDbAdapter.CAR_TABLE_NAME, MainDbAdapter.carTableColNames, null, MainDbAdapter.GEN_COL_NAME_NAME,
	                simpleListItem2, new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, new int[]{android.R.id.text1}, null);
	        setTitle(R.string.APP_Activity_CarList);
    	}
    	else if(listSource.equals(MainDbAdapter.DRIVER_TABLE_NAME)){
            super.onCreate( icicle, null, DriverEditActivity.class, null,
                    MainDbAdapter.DRIVER_TABLE_NAME, MainDbAdapter.driverTableColNames, null, MainDbAdapter.GEN_COL_NAME_NAME,
                    simpleListItem2,
                    new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, new int[]{android.R.id.text1}, null);
	        setTitle(R.string.APP_Activity_DriverList);
    	}
    	else if(listSource.equals(MainDbAdapter.BPARTNER_TABLE_NAME)){
            super.onCreate( icicle, null, BPartnerEditActivity.class, null,
                    MainDbAdapter.BPARTNER_TABLE_NAME, MainDbAdapter.bpartnerTableColNames, null, MainDbAdapter.GEN_COL_NAME_NAME, 
                    simpleListItem2,
                    new String[]{MainDbAdapter.GEN_COL_NAME_NAME}, new int[]{android.R.id.text1}, null);
	        setTitle(R.string.APP_Activity_BPartnerList);
    	}
    	
    	
    	else if(listSource.equals(MainDbAdapter.TASK_TABLE_NAME)){
            super.onCreate( icicle, null, TaskEditActivity.class, null,
                    MainDbAdapter.TASK_TABLE_NAME, MainDbAdapter.taskTableColNames, null, MainDbAdapter.GEN_COL_NAME_NAME,
                    twolineListActivity,
                    new String[]{MainDbAdapter.GEN_COL_NAME_NAME, MainDbAdapter.GEN_COL_USER_COMMENT_NAME},
                    new int[]{R.id.tvTwoLineListText1, R.id.tvTwoLineListText2}, null);
	        setTitle(R.string.APP_Activity_TaskList);
    	} 
    	else if(listSource.equals(MainDbAdapter.TASKTYPE_TABLE_NAME)){
            super.onCreate( icicle, null, TaskTypeEditActivity.class, null,
                    MainDbAdapter.TASKTYPE_TABLE_NAME, MainDbAdapter.taskTypeTableColNames, null, MainDbAdapter.GEN_COL_NAME_NAME,
                    twolineListActivity,
                    new String[]{MainDbAdapter.GEN_COL_NAME_NAME, MainDbAdapter.GEN_COL_USER_COMMENT_NAME},
                    new int[]{R.id.tvTwoLineListText1, R.id.tvTwoLineListText2}, null);
	        setTitle(R.string.APP_Activity_TaskTypeList);
    	} 
    	else if(listSource.equals(MainDbAdapter.UOM_TABLE_NAME)){
            super.onCreate( icicle, null, UOMEditActivity.class, null,
                    MainDbAdapter.UOM_TABLE_NAME, MainDbAdapter.uomTableColNames, 
                    null, MainDbAdapter.GEN_COL_NAME_NAME,
                    twolineListActivity,
                    new String[]{MainDbAdapter.GEN_COL_NAME_NAME, MainDbAdapter.UOM_COL_CODE_NAME},
                    new int[]{R.id.tvTwoLineListText1, R.id.tvTwoLineListText2}, null);
	        setTitle(R.string.APP_Activity_UOMList);
    	} 
    	else if(listSource.equals(MainDbAdapter.EXPENSECATEGORY_TABLE_NAME)){
    		if(!mBundleExtras.getBoolean("IsFuel")){
    	        super.onCreate( icicle, null, ExpenseCategoryEditActivity.class, null,
    	                MainDbAdapter.EXPENSECATEGORY_TABLE_NAME, MainDbAdapter.expenseCategoryTableColNames, MainDbAdapter.EXPENSECATEGORY_COL_ISFUEL_NAME + "='N'",
    	                MainDbAdapter.GEN_COL_NAME_NAME,
    	                twolineListActivity,
    	                new String[]{MainDbAdapter.GEN_COL_NAME_NAME, MainDbAdapter.GEN_COL_USER_COMMENT_NAME},
    	                new int[]{R.id.tvTwoLineListText1, R.id.tvTwoLineListText2}, null);
    	        setTitle(R.string.APP_Activity_ExpenseCategoryList);
    		}
    		else{
    	        super.onCreate( icicle, null, ExpenseCategoryEditActivity.class, null,
    	                MainDbAdapter.EXPENSECATEGORY_TABLE_NAME, MainDbAdapter.expenseCategoryTableColNames, MainDbAdapter.EXPENSECATEGORY_COL_ISFUEL_NAME + "='Y'",
    	                MainDbAdapter.GEN_COL_NAME_NAME,
    	                twolineListActivity,
    	                new String[]{MainDbAdapter.GEN_COL_NAME_NAME, MainDbAdapter.GEN_COL_USER_COMMENT_NAME},
    	                new int[]{R.id.tvTwoLineListText1, R.id.tvTwoLineListText2}, null);
    	        setTitle(R.string.APP_Activity_FuelCategoryList);
    		}
    	} 
    	else if(listSource.equals(MainDbAdapter.EXPENSETYPE_TABLE_NAME)){
            super.onCreate( icicle, null, ExpenseTypeEditActivity.class, null,
                    MainDbAdapter.EXPENSETYPE_TABLE_NAME, MainDbAdapter.expenseTypeTableColNames, null, MainDbAdapter.GEN_COL_NAME_NAME,
                    twolineListActivity,
                    new String[]{MainDbAdapter.GEN_COL_NAME_NAME, MainDbAdapter.GEN_COL_USER_COMMENT_NAME},
                    new int[]{R.id.tvTwoLineListText1, R.id.tvTwoLineListText2}, null);
	        setTitle(R.string.APP_Activity_ExpenseTypeList);
    	} 
    	else if(listSource.equals(MainDbAdapter.CURRENCY_TABLE_NAME)){
            super.onCreate( icicle, null, CurrencyEditActivity.class, null,
                    MainDbAdapter.CURRENCY_TABLE_NAME, MainDbAdapter.currencyTableColNames, null, MainDbAdapter.GEN_COL_NAME_NAME,
                    twolineListActivity,
                    new String[]{MainDbAdapter.GEN_COL_NAME_NAME, MainDbAdapter.CURRENCY_COL_CODE_NAME},
                    new int[]{R.id.tvTwoLineListText1, R.id.tvTwoLineListText2}, null);
	        setTitle(R.string.APP_Activity_CurrencyList);
    	} 
    	else if(listSource.equals(MainDbAdapter.TAG_TABLE_NAME)){
            super.onCreate( icicle, null, TagEditActivity.class, null,
                    MainDbAdapter.TAG_TABLE_NAME, MainDbAdapter.tagTableColNames, null, MainDbAdapter.GEN_COL_NAME_NAME,
                    twolineListActivity,
                    new String[]{MainDbAdapter.GEN_COL_NAME_NAME, MainDbAdapter.GEN_COL_USER_COMMENT_NAME},
                    new int[]{R.id.tvTwoLineListText1, R.id.tvTwoLineListText2}, null);
	        setTitle(R.string.APP_Activity_TagList);
    	} 

    	else if(listSource.equals(MainDbAdapter.UOM_CONVERSION_TABLE_NAME)){
            super.onCreate( icicle, null, UOMConversionEditActivity.class, null,
                    MainDbAdapter.UOM_CONVERSION_TABLE_NAME, MainDbAdapter.uomConversionTableColNames,
                    null, MainDbAdapter.GEN_COL_NAME_NAME,
                    threeLineListActivity,
                    new String[]{MainDbAdapter.GEN_COL_NAME_NAME, MainDbAdapter.GEN_COL_USER_COMMENT_NAME, MainDbAdapter.UOM_CONVERSION_COL_RATE_NAME},
                    new int[]{R.id.tvThreeLineListText1, R.id.tvThreeLineListText2, R.id.tvThreeLineListText3}, null);
	        setTitle(R.string.APP_Activity_UOMConversionList);
    	} 
    	else if(listSource.equals(MainDbAdapter.CURRENCYRATE_TABLE_NAME)){
            super.onCreate( icicle, null, CurrencyRateEditActivity.class, null,
                    MainDbAdapter.CURRENCYRATE_TABLE_NAME, MainDbAdapter.currencyRateTableColNames, null,
                    MainDbAdapter.GEN_COL_NAME_NAME,
                    threeLineListActivity,
                    new String[]{MainDbAdapter.GEN_COL_NAME_NAME, MainDbAdapter.CURRENCYRATE_COL_RATE_NAME, MainDbAdapter.CURRENCYRATE_COL_INVERSERATE_NAME},
                    new int[]{R.id.tvThreeLineListText1, R.id.tvThreeLineListText2, R.id.tvThreeLineListText3}, null);
	        setTitle(R.string.APP_Activity_CurrencyRateList);
    	} 
    }
}
