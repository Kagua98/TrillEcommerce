package com.trill.ecommerce.util

import com.trill.ecommerce.model.MenuCategoriesModel
import com.trill.ecommerce.model.MenuItemsModel
import com.trill.ecommerce.model.OrderModel
import com.trill.ecommerce.model.UserModel
import java.util.*
import kotlin.math.abs

object Common {
    fun createOrderNumber(): String {
        return StringBuilder()
            .append(System.currentTimeMillis())
            .append(abs(Random().nextInt()))
            .toString()
    }

    var currentToken: String = ""

    val ORDER_REFERNCE: String = "Orders"
    var listItemSelected: MenuItemsModel? = null
    var categorySelected: MenuCategoriesModel? = null
    val MENU_CATEGORY_REF: String = "Category"
    val FULL_WIDTH_COLUMN: Int = 1
    val DEFAULT_COLUMN_COUNT: Int = 0
    val USER_REFERENCE = "Users"
    var currentUser: UserModel? = null
    val COMMENT_REFERENCE: String = "Comments"
    val HOME_ADS_REF: String = "Banner"
    val currentOrder : OrderModel? = null
}