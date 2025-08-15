package com.sudhanshu.milkosysdairy

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class AdminFeatureAdapter(
    private val features: List<AdminFeature>,
    private val onClick: (AdminFeature) -> Unit
) : RecyclerView.Adapter<AdminFeatureAdapter.FeatureViewHolder>() {

    class FeatureViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val card: CardView = itemView.findViewById(R.id.cardFeature)
        val icon: ImageView = itemView.findViewById(R.id.iconFeature)
        val title: TextView = itemView.findViewById(R.id.textFeature)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeatureViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_feature, parent, false)
        return FeatureViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeatureViewHolder, position: Int) {
        val feature = features[position]
        holder.icon.setImageResource(feature.iconResId)
        holder.title.text = feature.title
        holder.card.setOnClickListener { onClick(feature) }
    }

    override fun getItemCount(): Int = features.size
}
