package com.trill.ecommerce.model

class MenuItemsModel {
    var id: String? = null
    var description: String? = null
    var image: String? = null
    var name: String? = null
    var price: Long? = 0
    var key: String? = null
    var ratingValue: Double? = 0.toDouble()
    var ratingCount: Long = 0.toLong()
}