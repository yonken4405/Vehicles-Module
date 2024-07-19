package com.example.vehiclespage

import DeletedDialogFragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.vehiclespage.R
import com.example.vehiclespage.vehicleProfile
import com.example.vehiclespage.databinding.VehicleProfileBinding//6.1 binding neccessity

class vehicleAdapter(
    //2. pass data from data class
    var vehicles: MutableList<vehicleProfile>,
    private val vehicleViewModel: vehicleViewModel
) : RecyclerView.Adapter<vehicleAdapter.vehicleViewHolder>() {//3. we're making an adapter for recyclerView

    //1. Make a viewholder
    inner class vehicleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val binding = VehicleProfileBinding.bind(itemView)//6. add binding to access views
    }

    //delete 1: similar to void in other languages
    interface OnVehicleDeletedListener {
        fun onVehicleDeleted()
    }
    //delete 2: Listener Variable
    private var onVehicleDeletedListener: OnVehicleDeletedListener? = null
    //delete 3:
    fun setOnVehicleDeletedListener(listener: OnVehicleDeletedListener) {
        this.onVehicleDeletedListener = listener
    }

    //View model: 4. transfers data from viewmodel to the adapter
    /*fun updateVehicles(newVehicles: List<vehicleProfile>) {
        Log.d("Adapter", "Updating vehicles with: $newVehicles")
        vehicles.clear()
        vehicles.addAll(newVehicles)
        notifyDataSetChanged()
    }*/


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
        holder.binding.pngCar.setImageResource(currentVehicle.vehicleImageResId) // Set image


        //10. go to main kt

        holder.binding.removeIcon.setOnClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to delete this vehicle?")
                .setPositiveButton("Delete") { dialog, _ ->
                    vehicleViewModel.deleteVehicle(currentVehicle) { success ->
                        if (success) {
                            notifyItemRemoved(position)
                            onVehicleDeletedListener?.onVehicleDeleted() // Trigger the listener
                        } else {
                            Toast.makeText(holder.itemView.context, "Failed to delete vehicle", Toast.LENGTH_SHORT).show()
                        }
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }


        holder.binding.editIcon.setOnClickListener {
            val editVehiclesFragment = EditVehicles()
            val bundle = Bundle()
            bundle.putString(EditVehicles.ARG_VEHICLE_NAME, currentVehicle.vname)
            bundle.putString(EditVehicles.ARG_VEHICLE_PLATE, currentVehicle.vplateNumber)
            bundle.putInt(EditVehicles.ARG_VEHICLE_IMAGE, currentVehicle.vehicleImageResId)
            editVehiclesFragment.arguments = bundle

            (holder.itemView.context as? AppCompatActivity)?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.fragmentContainer, editVehiclesFragment)
                ?.addToBackStack(null)
                ?.commit()
        }
    }

    override fun getItemCount(): Int {
        return vehicles.size//5. just return the number of vehicles on the list(vehicles: List<vehicleProfile>)
    }
}