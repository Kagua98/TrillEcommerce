package com.trill.ecommerce.callback

import com.trill.ecommerce.model.MenuCategoriesModel

interface IMenuCategoriesCallBackListener {
    fun onMenuCategoryLoadSuccess(menuCategoriesList:List<MenuCategoriesModel>)
    fun onMenuCategoryLoadFailed(message: String)

}