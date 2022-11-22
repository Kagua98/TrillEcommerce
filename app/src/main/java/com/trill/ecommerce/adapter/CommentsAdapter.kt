package com.trill.ecommerce.adapter

import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.trill.ecommerce.R
import com.trill.ecommerce.model.CommentsModel
import com.trill.ecommerce.model.MenuCategoriesModel
import org.w3c.dom.Text

class CommentsAdapter (internal var context: Context,
                       internal var commentsList: List<CommentsModel>): RecyclerView.Adapter<CommentsAdapter.CommentsViewHolder>() {

    inner class CommentsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var name: TextView? = null
        var date: TextView? = null
        var comment: TextView? = null
        var ratingBar: RatingBar? = null

        init {
            name = itemView.findViewById(R.id.name) as TextView
            date = itemView.findViewById(R.id.date) as TextView
            comment = itemView.findViewById(R.id.comment) as TextView
            ratingBar = itemView.findViewById(R.id.ratingBar) as RatingBar
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentsViewHolder {
        return CommentsViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_details_comments_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: CommentsViewHolder, position: Int) {
        val timeStamp = commentsList[position].commentTimeStamp!!["timeStamp"]!!.toString().toLong()
        holder.date!!.text = DateUtils.getRelativeTimeSpanString(timeStamp)
        holder.name!!.text = commentsList[position].name
        holder.comment!!.text = commentsList[position].comment
        holder.ratingBar!!.rating = commentsList[position].ratingValue


    }

    override fun getItemCount(): Int {
        return commentsList.size
    }
}