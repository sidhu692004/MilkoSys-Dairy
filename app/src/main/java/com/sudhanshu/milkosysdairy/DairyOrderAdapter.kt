package com.sudhanshu.milkosysdairy

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DairyOrderAdapter(
    private val orders: List<DairyOrderModel>,
    private val onClick: (DairyOrderModel) -> Unit
) : RecyclerView.Adapter<DairyOrderAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val tvCustomer: TextView = v.findViewById(R.id.tvOrderCustomer)
        val tvMobile: TextView = v.findViewById(R.id.tvOrderMobile)
        val tvAddress: TextView = v.findViewById(R.id.tvOrderAddress)
        val tvTotal: TextView = v.findViewById(R.id.tvOrderTotal)
        val tvPayment: TextView = v.findViewById(R.id.tvOrderPayment)
        val tvStatus: TextView = v.findViewById(R.id.tvOrderStatus)
        val tvTime: TextView = v.findViewById(R.id.tvOrderTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_dairy_order, parent, false)
        return OrderViewHolder(v)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]

        holder.tvCustomer.text = order.name
        holder.tvMobile.text = order.mobile
        holder.tvAddress.text = order.address
        holder.tvTotal.text = "₹${order.totalPrice}"
        holder.tvPayment.text = order.paymentMode
        holder.tvStatus.text = order.status
        holder.tvTime.text = order.timestamp?.toDate().toString()

        holder.itemView.setOnClickListener { onClick(order) }
    }

    override fun getItemCount(): Int = orders.size
}
