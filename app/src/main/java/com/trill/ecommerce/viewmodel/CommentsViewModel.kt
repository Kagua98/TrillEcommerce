package com.trill.ecommerce.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.trill.ecommerce.model.CommentsModel
import com.trill.ecommerce.model.MenuItemsModel
import com.trill.ecommerce.util.Common

class CommentsViewModel : ViewModel() {

    var mutableLiveDataCommentsList: MutableLiveData<List<CommentsModel>>? = null

    init {
        mutableLiveDataCommentsList = MutableLiveData()
    }

    fun setCommentsList(commentsList: List<CommentsModel>){
        mutableLiveDataCommentsList!!.value = commentsList
    }



}