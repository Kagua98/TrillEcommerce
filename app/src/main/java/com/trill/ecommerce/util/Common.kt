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

    fun createReceiptNumber(): String{
        return StringBuilder()
            .append(System.currentTimeMillis() % 10000)
            .append(abs(Random().nextInt()))
            .toString()
    }

    val SALES_AGENT_REFERENCE: String = "Agents"
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

    var historyItemSelected: OrderModel? = null

    val MPESA_CALLBACK_URL = "http://trill-mpesa-callback.matrixtelematic.co.ke/callback"
    val MPESA_BUSINESS_SHORTCODE = "174379"

    var order_address: String? = null
    var order_time: String? = null
    var order_date: String? = null
}