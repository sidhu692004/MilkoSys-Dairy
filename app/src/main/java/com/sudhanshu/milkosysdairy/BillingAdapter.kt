package com.sudhanshu.milkosysdairy

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BillingAdapter(
    private val orders: List<DairyOrderModel>
) : RecyclerView.Adapter<BillingAdapter.BillingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_billing_order, parent, false)
        return BillingViewHolder(view)
    }

    override fun getItemCount(): Int = orders.size

    override fun onBindViewHolder(holder: BillingViewHolder, position: Int) {
        val order = orders[position]
        holder.bind(order)
    }

    class BillingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvOrderId: TextView = itemView.findViewById(R.id.tvOrderId)
        private val tvOrderItems: TextView = itemView.findViewById(R.id.tvOrderItems)
        private val tvOrderTotal: TextView = itemView.findViewById(R.id.tvOrderTotal)
        private val tvDeliveryBoy: TextView = itemView.findViewById(R.id.tvDeliveryBoy)

        fun bind(order: DairyOrderModel) {
            tvOrderId.text = "Order ID: ${order.orderId}"

            val sb = StringBuilder()
            var total = 0.0
            order.items.forEach { item ->
                val itemTotal = item.quantity * item.price
                sb.append("  ${item.quantity} = ₹$itemTotal\n")
                total += itemTotal
            }
            tvOrderItems.text = sb.toString().trim()
            tvOrderTotal.text = "Total: ₹$total"
            tvDeliveryBoy.text = "Delivery: ${order.deliveryBoyName ?: "N/A"}"
        }
    }
}
