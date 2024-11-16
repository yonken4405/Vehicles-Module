package com.example.vehiclespage.BranchSelectionPage

import AddOn
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.vehiclespage.R

class AddOnsAdapter(
    private var addOns: MutableList<AddOn>, // Make this mutable for updates
    private val onAddOnsSelected: (List<AddOn>) -> Unit // Callback for selected add-ons
) : RecyclerView.Adapter<AddOnsAdapter.AddOnViewHolder>() {

    // Set to track selected add-ons
    private val selectedAddOns = mutableSetOf<AddOn>()

    inner class AddOnViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val addOnName: TextView = itemView.findViewById(R.id.tvAddOnName)
        private val addOnPrice: TextView = itemView.findViewById(R.id.tvAddOnPrice)

        fun bind(addOn: AddOn) {
            addOnName.text = addOn.name
            addOnPrice.text = "₱${"%.2f".format(addOn.price)}" // Format price to 2 decimal places

            // Highlight selected add-ons
            itemView.setBackgroundResource(
                if (selectedAddOns.contains(addOn)) R.drawable.layered_stroke else R.drawable.default_background
            )

            itemView.setOnClickListener {
                // Toggle selection
                if (selectedAddOns.contains(addOn)) {
                    selectedAddOns.remove(addOn)
                } else {
                    selectedAddOns.add(addOn)
                }
                notifyItemChanged(adapterPosition)

                // Notify the parent with the updated list of selected add-ons
                onAddOnsSelected(selectedAddOns.toList())
            }
        }
    }

    fun updateAddOns(newAddOns: List<AddOn>) {
        // Clear current list and add new data
        addOns.clear()
        addOns.addAll(newAddOns)
        notifyDataSetChanged()
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
