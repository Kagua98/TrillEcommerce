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
import com.trill.ecommerce.model.OrderModel
import com.trill.ecommerce.util.Common
import com.trill.ecommerce.util.HistoryItemClick
import com.trill.ecommerce.util.MenuCategoryClick
import com.trill.ecommerce.util.RecyclerItemClickListener
import org.greenrobot.eventbus.EventBus

class HistoryAdapter(
    private val context: Context,
    private val orderList: List<OrderModel>
): RecyclerView.Adapter<HistoryAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener{
        internal var imageView: ImageView? = null
        internal var textDate: TextView? = null
        internal var textOrderStatus: TextView? = null
        internal var textLocation: TextView? = null
        internal var textContactPerson: TextView? = null
        internal var textPhone: TextView? = null
        internal var textOrderNumber: TextView? = null
        internal var textOrderNotes: TextView? = null
        internal var textAmount: TextView? = null
        internal var textTime: TextView? = null

        private var onClickListener: RecyclerItemClickListener? = null

        fun setListener(listener: RecyclerItemClickListener) {
            this.onClickListener = listener
        }

        init {
            imageView = itemView.findViewById(R.id.imageView)
            textDate = itemView.findViewById(R.id.date)
            textOrderStatus = itemView.findViewById(R.id.status)
            textLocation = itemView.findViewById(R.id.deliveryLocation)
            textContactPerson = itemView.findViewById(R.id.contactPerson)
            textPhone = itemView.findViewById(R.id.phone)
            textTime = itemView.findViewById(R.id.time)
            textAmount = itemView.findViewById(R.id.amount)

            itemView.setOnClickListener(this)

            //textOrderNumber = itemView.findViewById(R.id.orderNumber)

        }

        override fun onClick(view: View?) {
            onClickListener!!.onItemClick(view!!, adapterPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        return OrderViewHolder(LayoutInflater.from(context).inflate(R.layout.history_item, parent,false))
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        Glide.with(context)
            .load(orderList[position].cartItemList!![0].productImage)
            .placeholder(R.drawable.art_item_placeholder)
            .into(holder.imageView!!)

        holder.textDate!!.text = orderList[position].date!!
        holder.textOrderStatus!!.text = convertStatusToText(orderList[position].orderStatus!!)

        holder.textLocation!!.text = orderList[position].shippingAddress!!
        holder.textContactPerson!!.text = orderList[position].userName!!
        holder.textAmount!!.text = "Ksh${"%, d".format(orderList[position].totalPayment)}"

        holder.textTime!!.text = orderList[position].time!!

        holder.setListener(object : RecyclerItemClickListener {
            override fun onItemClick(view: View, pos: Int) {
                Common.historyItemSelected = orderList[pos]
                EventBus.getDefault().postSticky(HistoryItemClick(true, orderList[pos]))
            }

        })


    }

    override fun getItemCount(): Int {
        return orderList.size
    }

    fun convertStatusToText(orderStatus: Int): String{
        when (orderStatus){
            0 -> return "Placed"
            1 -> return "Shipping"
            2 -> return "Shipped"
            -1 -> return "Cancelled"
            else -> return "In review"
        }
    }


}