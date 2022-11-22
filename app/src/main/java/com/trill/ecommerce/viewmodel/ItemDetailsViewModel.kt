package com.trill.ecommerce.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.trill.ecommerce.model.CommentsModel
import com.trill.ecommerce.model.MenuItemsModel
import com.trill.ecommerce.util.Common

class ItemDetailsViewModel : ViewModel() {

    private var mutableLiveDataMenuItem: MutableLiveData<MenuItemsModel>? = null
    private var mutableLiveDataComments: MutableLiveData<CommentsModel>? = null

    init {
        mutableLiveDataComments = MutableLiveData()
    }

    fun getMutableLiveDataMenuItem() : MutableLiveData<MenuItemsModel>{
         if (mutableLiveDataMenuItem == null)
             mutableLiveDataMenuItem = MutableLiveData()
        mutableLiveDataMenuItem!!.value = Common.listItemSelected

        return mutableLiveDataMenuItem!!
    }

    fun getMutableLiveDataComments() : MutableLiveData<CommentsModel>{
        if (mutableLiveDataMenuItem == null)
            mutableLiveDataMenuItem = MutableLiveData()

        return mutableLiveDataComments!!
    }

    fun setCommentsModel(commentsModel: CommentsModel) {
        if (mutableLiveDataComments != null){
            mutableLiveDataComments!!.value = commentsModel
        }
    }

    fun setMenuItemsModel(menuItemsModel: MenuItemsModel) {
        if (menuItemsModel != null)
            mutableLiveDataMenuItem!!.value = menuItemsModel
    }

}