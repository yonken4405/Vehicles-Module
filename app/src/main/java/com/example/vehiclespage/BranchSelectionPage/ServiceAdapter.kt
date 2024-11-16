package com.example.vehiclespage.BranchSelectionPage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.vehiclespage.R

class ServiceAdapter(
    private val services: MutableList<Service>, // Change List to MutableList
    private val onServiceSelected: (List<Service>) -> Unit // Notify the parent when services are selected
) : RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder>() {

    private val selectedServices = mutableSetOf<Service>() // Use a mutable set for unique selections
    private var selectedVehicleType: String = "Sedan" // Default to Sedan, will be updated later

    fun setSelectedVehicleType(vehicleType: String) {
        selectedVehicleType = vehicleType
        notifyDataSetChanged()
    }

    inner class ServiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val serviceName = itemView.findViewById<TextView>(R.id.tvServiceName)
        private val sedanPrice = itemView.findViewById<TextView>(R.id.tvSedanPrice)
        private val suvPrice = itemView.findViewById<TextView>(R.id.tvSuvPrice)
        private val pickupPrice = itemView.findViewById<TextView>(R.id.tvPickupPrice)



        fun bind(service: Service) {
            serviceName.text = service.name

            sedanPrice.text = "₱ ${service.sedanPrice}"
            suvPrice.text = "₱ ${service.suvPrice}"
            pickupPrice.text = "₱ ${service.pickupPrice}"

//            // Set the relevant price based on selected vehicle type
//            when (selectedVehicleType) {
//                "Sedan" -> {
//                    sedanPrice.text = "₱ ${service.sedanPrice}"
//                    suvPrice.text = "₱ ${service.suvPrice}"
//                    pickupPrice.text = "₱ ${service.pickupPrice}"
//                }
//                "SUV" -> {
//                    suvPrice.text = "₱ ${service.suvPrice}"
//                    sedanPrice.text = "₱ ${service.sedanPrice}"
//                    pickupPrice.text = "₱ ${service.pickupPrice}"
//                }
//                "Pickup" -> {
//                    pickupPrice.text = "₱ ${service.pickupPrice}"
//                    sedanPrice.text = "₱ ${service.sedanPrice}"
//                    suvPrice.text = "₱ ${service.suvPrice}"
//                }
//                else -> {
//                    sedanPrice.text = "₱ ${service.sedanPrice}"
//                    suvPrice.text = "₱ ${service.suvPrice}"
//                    pickupPrice.text = "₱ ${service.pickupPrice}"
//                }
//            }

            // Highlight the selected service
            itemView.setBackgroundResource(
                if (selectedServices.contains(service)) R.drawable.layered_stroke else R.drawable.default_background
            )

            // Toggle service selection on click
            itemView.setOnClickListener {
                if (selectedServices.contains(service)) {
                    selectedServices.remove(service)
                } else {
                    selectedServices.add(service)
                }
                notifyItemChanged(adapterPosition)
                onServiceSelected(selectedServices.toList())
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

    // Method to update the service list dynamically
    fun updateServices(newServices: List<Service>) {
        services.clear()  // Now works because services is a MutableList
        services.addAll(newServices)  // Now works because services is a MutableList
        notifyDataSetChanged()
    }
}
