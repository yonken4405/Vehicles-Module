package com.example.vehiclespage.BranchSelectionPage

import AddOn
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.vehiclespage.R

class AddOnsAdapter(
    private val addOns: List<AddOn>,
    private val onAddOnsSelected: (List<AddOn>) -> Unit // Add this parameter
) : RecyclerView.Adapter<AddOnsAdapter.AddOnViewHolder>() {
    private val selectedAddOns = mutableSetOf<AddOn>()

    inner class AddOnViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val addOnName = itemView.findViewById<TextView>(R.id.tvAddOnName)
        private val addOnPrice = itemView.findViewById<TextView>(R.id.tvAddOnPrice)

        fun bind(addOn: AddOn) {
            addOnName.text = addOn.name
            addOnPrice.text = "₱ ${addOn.price}"

            // Highlight selected add-ons
            itemView.setBackgroundResource(
                if (selectedAddOns.contains(addOn)) R.drawable.layered_stroke else R.drawable.default_background
            )

            itemView.setOnClickListener {
                if (selectedAddOns.contains(addOn)) {
                    selectedAddOns.remove(addOn)
                } else {
                    selectedAddOns.add(addOn)
                }
                notifyItemChanged(adapterPosition)

                // Notify the parent fragment with the updated selected add-ons list
                onAddOnsSelected(selectedAddOns.toList())
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddOnViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_add_on, parent, false)
        return AddOnViewHolder(view)
    }

    override fun onBindViewHolder(holder: AddOnViewHolder, position: Int) {
        holder.bind(addOns[position])
    }

    override fun getItemCount(): Int = addOns.size
}
