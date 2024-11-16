package com.example.vehiclespage.BranchSelectionPage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.vehiclespage.R

class ServiceAdapter(
    private val services: List<Service>,
    private val onServiceSelected: (List<Service>) -> Unit // Add this parameter to notify parent
) : RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder>() {

    private val selectedServices = mutableSetOf<Service>() // Use a mutable set for unique selections

    inner class ServiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val serviceName = itemView.findViewById<TextView>(R.id.tvServiceName)
        private val servicePrice = itemView.findViewById<TextView>(R.id.tvServicePrice)

        fun bind(service: Service) {
            serviceName.text = service.name
            servicePrice.text = "₱ ${service.price}"

            // Highlight selected services
            itemView.setBackgroundResource(
                if (selectedServices.contains(service)) R.drawable.layered_stroke else R.drawable.default_background
            )

            itemView.setOnClickListener {
                if (selectedServices.contains(service)) {
                    selectedServices.remove(service) // Deselect if already selected
                } else {
                    selectedServices.add(service) // Add to selected if not already selected
                }
                notifyItemChanged(adapterPosition)

                // Notify the parent fragment with the updated selected services list
                onServiceSelected(selectedServices.toList()) // Pass the selected services list
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_service, parent, false)
        return ServiceViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        holder.bind(services[position])
    }

    override fun getItemCount(): Int = services.size
}
