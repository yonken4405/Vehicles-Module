package com.example.vehiclespage.VehiclesPage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.vehiclespage.R
import com.example.vehiclespage.vehicleProfile
import com.example.vehiclespage.databinding.VehicleProfileBinding//6.1 binding neccessity
import com.example.vehiclespage.vehicleViewModel

class vehicleAdapter(
    //2. pass data from data class
    var vehicles: List<vehicleProfile>,
    private val vehicleViewModel: vehicleViewModel
) : RecyclerView.Adapter<vehicleAdapter.vehicleViewHolder>() {//3. we're making an adapter for recyclerView

    //1. Make a viewholder
    inner class vehicleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val binding = VehicleProfileBinding.bind(itemView)//6. add binding to access views
    }

    //4. press ctrl.i
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): vehicleViewHolder {
        return vehicleViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.vehicle_profile, parent, false)//6.2. make a inflater
        )
    }

    override fun onBindViewHolder(holder: vehicleViewHolder, position: Int) {
        //7. bind the data from list(data class) to the template(vehicleProfile)
        val currentVehicle = vehicles[position]//8. set variable to get the current vehicle for the rv to present
        holder.binding.tvVehicleName.text = currentVehicle.vname //9. transfer the data from data class to the template
        holder.binding.tvPlateNumber.text = currentVehicle.vplateNumber

        //10. go to main kt

        //remove the selected view from the database
        holder.binding.editIcon.setOnClickListener {
            vehicleViewModel.deleteVehicle(currentVehicle) { success ->
                if (success) {
                    notifyItemRemoved(position)
                } else {
                    Toast.makeText(holder.itemView.context, "Failed to delete vehicle", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    override fun getItemCount(): Int {
        return vehicles.size//5. just return the number of vehicles on the list(vehicles: List<vehicleProfile>)
    }
}