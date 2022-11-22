package com.trill.ecommerce.util

import com.trill.ecommerce.model.MenuCategoriesModel
import com.trill.ecommerce.model.MenuItemsModel
import com.trill.ecommerce.model.UserModel

object Common {
    var listItemSelected: MenuItemsModel? = null
    var categorySelected: MenuCategoriesModel? = null
    val MENU_CATEGORY_REF: String = "Category"
    val FULL_WIDTH_COLUMN: Int = 1
    val DEFAULT_COLUMN_COUNT: Int = 0
    val USER_REFERENCE = "Users"
    var currentUser : UserModel? = null
    val COMMENT_REFERENCE: String = "Comments"
}