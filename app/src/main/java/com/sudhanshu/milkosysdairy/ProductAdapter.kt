package com.sudhanshu.milkosysdairy

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class ProductAdapter(private val productList: List<ProductModel>) :
    RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productCard: CardView = itemView.findViewById(R.id.productCard)
        val productName: TextView = itemView.findViewById(R.id.tvProductName)
        val productQty: TextView = itemView.findViewById(R.id.tvProductQty)
        val productRate: TextView = itemView.findViewById(R.id.tvProductRate)
        val productDate: TextView = itemView.findViewById(R.id.tvProductDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product_card, parent, false)
        return ProductViewHolder(view)
    }

    override fun getItemCount(): Int = productList.size

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]
        holder.productName.text = product.productName
        holder.productQty.text = "Quantity: ${product.quantity}"
        holder.productRate.text = "Rate: ₹${product.rate}"
        holder.productDate.text = "Date: ${product.date}"
    }
}
