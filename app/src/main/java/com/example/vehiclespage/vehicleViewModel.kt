package com.example.vehiclespage

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class vehicleViewModel : ViewModel(){
    private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Vehicle Information")

    //View model: 3. basically holds the list vehicles(?) that will be displayed on the rv
    val vehicleList = MutableLiveData<MutableList<vehicleProfile>>(mutableListOf())//list of all the vehicles

    init {// The fetchVehicles() function is called in the init block,
        // so data fetching starts as soon as the ViewModel is created.
        fetchVehicles()
    }

    fun addVehicle(vehicle: vehicleProfile) {//function to add the current vehicle details to the vehicleList(used earlier number 2)
       /* val currentList = vehicleList.value ?: mutableListOf()
        currentList.add(vehicle)
        vehicleList.value = currentList*/
        val currentList = vehicleList.value ?: mutableListOf()
        currentList.add(vehicle)
        vehicleList.postValue(currentList) // Use postValue to update LiveData
    }


    private fun fetchVehicles() {
        databaseReference.addValueEventListener(object : ValueEventListener {//listens to realtime updates
            override fun onDataChange(snapshot: DataSnapshot) {
                val vehicles = mutableListOf<vehicleProfile>()
                for (vehicleSnapshot in snapshot.children) {
                    val vehicle = vehicleSnapshot.getValue(vehicleProfile::class.java)
                    vehicle?.let { vehicles.add(it) }
                }
                Log.d("ViewModel", "Fetched Vehicles: $vehicles")
                vehicleList.postValue(vehicles) // Update LiveData on main thread
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle errors
                Log.e("ViewModel", "Firebase data fetching failed: ${error.message}")
            }
        })
    }

    fun deleteVehicle(vehicle: vehicleProfile, onResult: (Boolean) -> Unit) {
        val vehiclePLateNumber = vehicle.vplateNumber
        if (vehiclePLateNumber != null) {
            Log.d("VehicleViewModel", "Attempting to delete vehicle with name: $vehiclePLateNumber")
            databaseReference.child(vehiclePLateNumber).removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Update the vehicleList in the ViewModel
                    val currentList = vehicleList.value ?: mutableListOf()
                    currentList.remove(vehicle)
                    vehicleList.postValue(currentList) // This triggers RecyclerView update

                    Log.d("VehicleViewModel", "Successfully deleted vehicle: ${vehicle.vplateNumber}")
                    onResult(true)
                } else {
                    Log.e("VehicleViewModel", "Failed to delete vehicle: ${task.exception?.message}")
                    onResult(false)
                }
            }
        } else {
            Log.e("VehicleViewModel", "Vehicle name is null, cannot delete")
            onResult(false)
        }
    }



}