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
import com.trill.ecommerce.model.MenuCategoriesModel
import com.trill.ecommerce.util.Common
import com.trill.ecommerce.util.MenuCategoryClick
import com.trill.ecommerce.util.RecyclerItemClickListener
import org.greenrobot.eventbus.EventBus

class MenuCategoriesAdapter(
    internal var context: Context,
    internal var menuCategoriesList: List<MenuCategoriesModel>
) :
    RecyclerView.Adapter<MenuCategoriesAdapter.MenuCategoriesViewHolder>() {

    var menuListAdapter: MenuCategoriesNestedAdapter? = null

    inner class MenuCategoriesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        var parentName: TextView? = null
        var seeMoreText: TextView? = null
        var recyclerView: RecyclerView? = null


        private var onClickListener: RecyclerItemClickListener? = null

        fun setListener(listener: RecyclerItemClickListener) {
            this.onClickListener = listener
        }

        init {
            parentName = itemView.findViewById(R.id.title) as TextView
            seeMoreText = itemView.findViewById(R.id.subtitle) as TextView
            recyclerView = itemView.findViewById(R.id.recyclerView) as RecyclerView


            seeMoreText!!.setOnClickListener(this)
        //    itemView.setOnClickListener(this)

        }

        override fun onClick(view: View?) {
            onClickListener!!.onItemClick(view!!, adapterPosition)

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuCategoriesViewHolder {
        return MenuCategoriesViewHolder(
            LayoutInflater.from(context).inflate(R.layout.menu_item_header, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MenuCategoriesViewHolder, position: Int) {
        holder.parentName!!.text = menuCategoriesList[position].name

        menuListAdapter = MenuCategoriesNestedAdapter(context, menuCategoriesList[position]!!.products!!)
        holder.recyclerView!!.adapter = menuListAdapter

        holder.setListener(object : RecyclerItemClickListener {
            override fun onItemClick(view: View, pos: Int) {
                Common.categorySelected = menuCategoriesList[pos]
                EventBus.getDefault().postSticky(MenuCategoryClick(true, menuCategoriesList[pos]))
            }

        })
    }

    override fun getItemCount(): Int {
        return menuCategoriesList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (menuCategoriesList.size == 1)
            Common.DEFAULT_COLUMN_COUNT
        else {
            if (menuCategoriesList.size % 2 == 0)
                Common.DEFAULT_COLUMN_COUNT
            else
                if (position > 1 && position == menuCategoriesList.size - 1)
                    Common.FULL_WIDTH_COLUMN else Common.DEFAULT_COLUMN_COUNT
        }
    }


}