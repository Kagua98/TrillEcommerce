package com.trill.ecommerce.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.trill.ecommerce.R
import com.trill.ecommerce.database.CartDataSource
import com.trill.ecommerce.database.CartDatabase
import com.trill.ecommerce.database.LocalCartDataSource
import com.trill.ecommerce.model.MenuItemsModel
import com.trill.ecommerce.util.Common
import com.trill.ecommerce.util.MenuCategoryClick
import com.trill.ecommerce.util.MenuListItemClick
import com.trill.ecommerce.util.RecyclerItemClickListener
import io.reactivex.disposables.CompositeDisposable
import org.greenrobot.eventbus.EventBus

class MenuListAdapter(
    internal var context: Context,
    internal var menuItemsList: List<MenuItemsModel>
) :
    RecyclerView.Adapter<MenuListAdapter.MenuListViewHolder>() {

    private val compositeDisposable : CompositeDisposable
    private lateinit var cartDataSource: CartDataSource

    init {
        compositeDisposable = CompositeDisposable()
        cartDataSource = LocalCartDataSource(CartDatabase.getInstance(context).cartDAO())

    }


    inner class MenuListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        var childName: TextView? = null
        var childImage: ImageView? = null
        var childDescription: TextView? = null
        var childPrice: TextView? = null
        var favoriteImage: ImageView? = null

        private var onClickListener: RecyclerItemClickListener? = null

        fun setListener(listener: RecyclerItemClickListener) {
            this.onClickListener = listener
        }


        init {
            childName = itemView.findViewById(R.id.title) as TextView
            childImage = itemView.findViewById(R.id.imageView) as ImageView
            childPrice = itemView.findViewById(R.id.price) as TextView
            favoriteImage = itemView.findViewById(R.id.favorite) as ImageView
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            onClickListener!!.onItemClick(view!!, adapterPosition)

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuListViewHolder {
        return MenuListViewHolder(
            LayoutInflater.from(context).inflate(R.layout.menu_list_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MenuListViewHolder, position: Int) {
        Glide.with(context)
            .load(menuItemsList[position].image)
            .placeholder(R.drawable.art_item_placeholder)
            .into(holder.childImage!!)
        holder.childName!!.text = menuItemsList[position].name
        holder.childPrice!!.text = "Ksh${"%, d".format(menuItemsList[position].price)}"

        holder.setListener(object : RecyclerItemClickListener {
            override fun onItemClick(view: View, pos: Int) {
                Common.listItemSelected = menuItemsList[pos]
                Common.listItemSelected!!.key = pos.toString()
                EventBus.getDefault().postSticky(MenuListItemClick(true, menuItemsList[pos]))
            }

        })
    }

    override fun getItemCount(): Int {
        return menuItemsList.size
    }


    fun onStop(){
        if (compositeDisposable != null)
               compositeDisposable.clear()
    }

}