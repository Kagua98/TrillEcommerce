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


    inner class MenuCategoriesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        var parentName: TextView? = null
        var parentImage: ImageView? = null
        var parentDescription: TextView? = null


        private var onClickListener: RecyclerItemClickListener? = null

        fun setListener(listener: RecyclerItemClickListener) {
            this.onClickListener = listener
        }

        init {
            parentName = itemView.findViewById(R.id.title) as TextView
            parentImage = itemView.findViewById(R.id.imageView) as ImageView
         // parentDescription = itemView.findViewById(R.id.subtitle) as TextView
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            onClickListener!!.onItemClick(view!!, adapterPosition)

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuCategoriesViewHolder {
        return MenuCategoriesViewHolder(
            LayoutInflater.from(context).inflate(R.layout.menu_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MenuCategoriesViewHolder, position: Int) {
        Glide.with(context)
            .load(menuCategoriesList[position].image)
            .placeholder(R.drawable.art_item_placeholder)
            .into(holder.parentImage!!)
        holder.parentName!!.text = menuCategoriesList[position].name
        //  holder.parentDescription!!.text = menuCategoriesList[position].description

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