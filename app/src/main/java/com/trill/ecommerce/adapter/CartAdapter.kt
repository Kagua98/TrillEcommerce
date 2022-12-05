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
import com.trill.ecommerce.database.CartDataSource
import com.trill.ecommerce.database.CartDatabase
import com.trill.ecommerce.database.CartItem
import com.trill.ecommerce.database.LocalCartDataSource
import com.trill.ecommerce.eventbus.UpdateItemInCart
import com.trill.ecommerce.util.ui.NumberButton
import io.reactivex.disposables.CompositeDisposable
import org.greenrobot.eventbus.EventBus

class CartAdapter(
    internal var context: Context,
    internal var cartItems: List<CartItem>
) :
    RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    internal var compositeDisposable: CompositeDisposable
    internal var cartDataSource: CartDataSource

    init {
        compositeDisposable = CompositeDisposable()
        cartDataSource = LocalCartDataSource(CartDatabase.getInstance(context).cartDAO())
    }

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var cartItemName: TextView? = null
        var cartItemImage: ImageView? = null
        var cartItemPrice: TextView? = null
        var cartNumberButton: NumberButton

        init {
            cartItemImage = itemView.findViewById(R.id.imageView) as ImageView
            cartItemName = itemView.findViewById(R.id.title) as TextView
            cartItemPrice = itemView.findViewById(R.id.description) as TextView
            cartNumberButton = itemView.findViewById(R.id.numberButton) as NumberButton
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        return CartViewHolder(
            LayoutInflater.from(context).inflate(R.layout.cart_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        Glide.with(context)
            .load(cartItems[position].productImage)
            .placeholder(R.drawable.art_item_placeholder)
            .into(holder.cartItemImage!!)

         holder.cartItemName!!.text = cartItems[position].productName
         holder.cartItemPrice!!.text = "Ksh ${"%, d".format(cartItems[position].productPrice).toString()}"
         holder.cartNumberButton.number = cartItems[position].productQuantity.toString()

        holder.cartNumberButton.isEnabled = false
        holder.cartNumberButton.isClickable = false

        //Event
        holder.cartNumberButton.setOnValueChangeListener(object :
            NumberButton.OnValueChangeListener {
            override fun onValueChange(view: NumberButton?, oldValue: Int, newValue: Int) {
                cartItems[holder.bindingAdapterPosition].productQuantity = newValue
                EventBus.getDefault()
                    .postSticky(UpdateItemInCart(cartItems[holder.bindingAdapterPosition]))
            }

        })
    }

    fun getItemAtPosition(pos: Int): CartItem {
        return cartItems[pos]
    }

    override fun getItemCount(): Int {
        return cartItems.size
    }
}