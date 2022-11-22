package com.trill.ecommerce.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.trill.ecommerce.model.MenuItemsModel
import com.trill.ecommerce.util.Common

class MenuListViewModel : ViewModel() {

    private var mutableMenuItemsListData: MutableLiveData<List<MenuItemsModel>>? = null


    fun getMutableModelMenuList(): MutableLiveData<List<MenuItemsModel>> {
        if (mutableMenuItemsListData == null)
            mutableMenuItemsListData = MutableLiveData()

        mutableMenuItemsListData!!.value = Common.categorySelected!!.products

        return mutableMenuItemsListData!!

    }

}