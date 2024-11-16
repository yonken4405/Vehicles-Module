package com.example.vehiclespage.BranchSelectionPage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.recyclerview.widget.RecyclerView
import com.example.vehiclespage.R
import com.example.vehiclespage.databinding.VehicleOptionsBinding
import com.example.vehiclespage.VehiclesPage.vehicleProfile
import com.example.vehiclespage.VehiclesPage.vehicleViewModel

class VehicleAdapter(
    var vehicles: MutableList<vehicleProfile>,
    private val vehicleViewModel: vehicleViewModel,
    private val onVehicleSelected: (vehicleProfile) -> Unit // Add callback for when a vehicle is selected
) : RecyclerView.Adapter<VehicleAdapter.vehicleViewHolder>() {

    private var selectedPosition: Int = -1 // Track the selected item position

    inner class vehicleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = VehicleOptionsBinding.bind(itemView)
        val radioButton: RadioButton = itemView.findViewById(R.id.rbSelectVehicle)
    }

    private fun setSelectedPosition(position: Int) {
        val previousPosition = selectedPosition
        selectedPosition = position
        notifyItemChanged(previousPosition) // Unselect the previous item
        notifyItemChanged(selectedPosition) // Select the new item
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): vehicleViewHolder {
        return vehicleViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.vehicle_options, parent, false)
        )
    }

    override fun onBindViewHolder(holder: vehicleViewHolder, position: Int) {
        val currentVehicle = vehicles[position]

        holder.binding.tvVehicleName.text = currentVehicle.vname
        holder.binding.tvPlateNumber.text = currentVehicle.vplateNumber
        holder.binding.pngCar.setImageResource(currentVehicle.vehicleImageResId)

        // Set the RadioButton to be checked based on the selected position
        holder.radioButton.isChecked = (position == selectedPosition)

        holder.radioButton.setOnClickListener {
            setSelectedPosition(position) // Update the selected vehicle
            onVehicleSelected(currentVehicle) // Notify the selected vehicle to the parent fragment
        }
    }

    override fun getItemCount(): Int {
        return vehicles.size
    }
}
