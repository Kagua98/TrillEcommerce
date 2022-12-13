package com.trill.ecommerce.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.trill.ecommerce.R
import com.trill.ecommerce.database.CartItem
import com.trill.ecommerce.model.HistoryModel
import com.trill.ecommerce.util.Common
import com.trill.ecommerce.util.MenuCategoryClick
import com.trill.ecommerce.util.RecyclerItemClickListener
import org.greenrobot.eventbus.EventBus

class ReceiptAdapter(
    internal var context: Context,
    internal var historyItemsList: List<CartItem>
) :
    RecyclerView.Adapter<ReceiptAdapter.ReceiptViewHolder>() {


    inner class ReceiptViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name: TextView? = null
        var image: ImageView? = null
        var price: TextView? = null
        var quantity: TextView? = null

        init {

            name = itemView.findViewById(R.id.title)
            image = itemView.findViewById(R.id.imageView)
            price = itemView.findViewById(R.id.price)
            quantity = itemView.findViewById(R.id.quantity)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReceiptAdapter.ReceiptViewHolder {
        return ReceiptViewHolder(
            LayoutInflater.from(context).inflate(R.layout.receipt_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ReceiptAdapter.ReceiptViewHolder, position: Int) {
        Glide.with(context)
            .load(historyItemsList[position].productImage)
            .placeholder(R.drawable.art_item_placeholder)
            .into(holder.image!!)
        holder.name!!.text = historyItemsList[position].productName
        holder.quantity!!.text = historyItemsList[position].productQuantity.toString()
        holder.price!!.text = historyItemsList[position].productPrice.toString()
    }

    override fun getItemCount(): Int {
        return historyItemsList.size
    }


}