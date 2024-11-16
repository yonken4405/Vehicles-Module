package com.example.vehiclespage.BranchSelectionPage

import AddOn
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.vehiclespage.R
import com.example.vehiclespage.databinding.ItemOrderSummaryBinding


class OrderSummaryAdapter(private val items: List<Any>, private val classification: String) : RecyclerView.Adapter<OrderSummaryAdapter.OrderSummaryViewHolder>() {

    // Define different view types for services and add-ons
    companion object {
        const val TYPE_SERVICE = 1
        const val TYPE_ADDON = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderSummaryViewHolder {
        val binding = ItemOrderSummaryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderSummaryViewHolder(binding, viewType)
    }

    override fun onBindViewHolder(holder: OrderSummaryViewHolder, position: Int) {
        val item = items[position]

        when (holder.viewType) {
            TYPE_SERVICE -> {
                if (item is Service) {
                    holder.bindService(item)
                }
            }
            TYPE_ADDON -> {
                if (item is AddOn) {
                    holder.bindAddOn(item)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is Service -> TYPE_SERVICE
            is AddOn -> TYPE_ADDON
            else -> super.getItemViewType(position)
        }
    }

    inner class OrderSummaryViewHolder(
        private val binding: ItemOrderSummaryBinding,
        val viewType: Int
    ) : RecyclerView.ViewHolder(binding.root) {

        // Bind service data to the view
        fun bindService(service: Service) {
            binding.serviceName.text = service.name
            // Replace service.price with service.getPriceForClassification(classification)
            binding.serviceAmount.text = "₱${"%.2f".format(service.getPriceForClassification(classification))}" // Use classification appropriately
        }


        // Bind add-on data to the view
        fun bindAddOn(addOn: AddOn) {
            binding.serviceName.text = addOn.name
            binding.serviceAmount.text = "₱${"%.2f".format(addOn.price)}"
        }
    }
}
