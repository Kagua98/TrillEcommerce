package com.trill.ecommerce.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.trill.ecommerce.callback.IOMenuCategoriesCallBackListener
import com.trill.ecommerce.model.HomeMenuItem
import com.trill.ecommerce.model.MenuCategoriesModel
import com.trill.ecommerce.repository.HomeRepository
import com.trill.ecommerce.util.Common

class HomeViewModel : ViewModel(), IOMenuCategoriesCallBackListener {
    override fun onMenuCategoryLoadSuccess(menuCategoriesList: List<MenuCategoriesModel>) {
        menuCategoriesListMutable?.value = menuCategoriesList
    }

    override fun onMenuCategoryLoadFailed(message: String) {
        messageError.value = message
    }

    private var menuCategoriesListMutable: MutableLiveData<List<MenuCategoriesModel>>? = null
    private var messageError: MutableLiveData<String> = MutableLiveData()
    private val menuCategoriesCallBackListener: IOMenuCategoriesCallBackListener

    init {
        menuCategoriesCallBackListener = this
    }

    fun getMenuCategoriesList():MutableLiveData<List<MenuCategoriesModel>>{
        if (menuCategoriesListMutable == null){
            menuCategoriesListMutable = MutableLiveData()
            loadMenu()
        }
        return menuCategoriesListMutable!!
    }

    fun getMessageError(): MutableLiveData<String>{
        return messageError
    }

    private fun loadMenu() {
        val tempList = ArrayList<MenuCategoriesModel>()
        val menuCategoryRef = FirebaseDatabase.getInstance().getReference(Common.MENU_CATEGORY_REF)
        menuCategoryRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                for (itemSnapshot in snapshot!!.children){
                    val model = itemSnapshot.getValue<MenuCategoriesModel> (MenuCategoriesModel::class.java)
                    model!!.menu_id = itemSnapshot.key
                    tempList.add(model!!)
                }

                menuCategoriesCallBackListener.onMenuCategoryLoadSuccess(tempList)
            }

            override fun onCancelled(error: DatabaseError) {
                menuCategoriesCallBackListener.onMenuCategoryLoadFailed((error.message!!))
            }

        })
    }
}