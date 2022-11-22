package com.trill.ecommerce.util

import android.view.View

interface RecyclerItemClickListener {
    fun onItemClick(view: View, pos: Int)
}