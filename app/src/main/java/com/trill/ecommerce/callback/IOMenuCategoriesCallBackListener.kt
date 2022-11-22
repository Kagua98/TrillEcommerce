package com.trill.ecommerce.callback

import com.trill.ecommerce.model.MenuCategoriesModel

interface IOMenuCategoriesCallBackListener {
    fun onMenuCategoryLoadSuccess(menuCategoriesList:List<MenuCategoriesModel>)
    fun onMenuCategoryLoadFailed(message: String)

}