package com.example.vehiclespage.VehiclesPage

import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.vehiclespage.R
import com.example.vehiclespage.databinding.FragmentAddVehiclesBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class addVehicles : Fragment(R.layout.fragment_add_vehicles) {
    private var _binding: FragmentAddVehiclesBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: vehicleSpinnerAdapter
    val vehicleViewModel: vehicleViewModel by activityViewModels()
    private var selectedImageResId: Int = -1 // Variable to store selected image resource ID
    private lateinit var databaseReference : DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddVehiclesBinding.inflate(inflater, container, false)
        val spinner = binding.spVehicles // Reference spinner from layout

        val items = mutableListOf(
            spinnerItems(0, "Vehicle Classification", ""),
            spinnerItems(R.drawable.sedan, "Sedan","(EX: Toyota Vios, Honda City, Toyota Corolla)"),
            spinnerItems(R.drawable.suv, "SUV","(EX: Toyota Fortuner, Ford Everest, Suzuki Jimny)"),
            spinnerItems(R.drawable.pickup, "Pick Up","(EX: Toyota Hilux, Ford Ranger, Nissan Navara)"),
            spinnerItems(R.drawable.motorcycle_small, "Motorcycle (Small)","(399 CC or below)"),
            spinnerItems(R.drawable.motorcycle_large, "Motorcycle (Large)","(400 CC or above)"),
        )

        adapter = vehicleSpinnerAdapter(requireContext(), items) // Initialize class property
        spinner.adapter = adapter


        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedItem = adapter.getItem(position)
                if (position == 0) {
                    // Handle hint selection (e.g., reset selectedImageResId, disable "Done" button)
                    selectedImageResId = -1
                } else {
                    val selectedItem = adapter.getItem(position)
                    if (selectedItem != null) {
                        handleSelectedItem(selectedItem)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle as needed
            }
        }
        return binding.root
    }


    private fun handleSelectedItem(selectedItem: spinnerItems) {
        val newDrawableResId = when (selectedItem.carType) {
            "Sedan" -> R.drawable.sedan
            "SUV" -> R.drawable.suv
            "Pick Up" -> R.drawable.pickup
            "Motorcycle (Small)" -> R.drawable.motorcycle_small
            "Motorcycle (Large)" -> R.drawable.motorcycle_large
            else -> selectedItem.carPic
        }

        selectedImageResId = newDrawableResId // Update the selected image resource ID

        val updatedItem = selectedItem.copy(carPic = newDrawableResId)

        // Find the index of the selected item
        val index = (0 until adapter.count).find { adapter.getItem(it) == selectedItem }
        if (index != null) {
            adapter.remove(selectedItem) // Remove the old item
            adapter.insert(updatedItem, index) // Insert the updated item at the same position
            adapter.notifyDataSetChanged() // Notify the adapter of the change
        }

        // Store the classification string in the vehicle profile
        vehicleViewModel.setVehicleClassification(selectedItem.carType)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddVehiclesBinding.bind(view)
        binding.etVehiclePlateNumber.filters = arrayOf<InputFilter>(AllCapsLengthFilter(10))

        binding.btnBack.setOnClickListener {
            val myVehicles = myVehicles()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, myVehicles) // Use requireActivity() here
                .addToBackStack(null) // Optional: Add to back stack for navigation
                .commit()
        }

        //View model: 1. get the data from the input fields
        binding.btnDone.setOnClickListener {
            val vehicleName = binding.etVehicleName.text.toString()
            val vehiclePlateNumber = binding.etVehiclePlateNumber.text.toString()

            // Get the selected classification string from the ViewModel
            val vehicleClassification = vehicleViewModel.vehicleClassification.value ?: ""

            val vehicle = vehicleProfile(vehicleName, vehiclePlateNumber, selectedImageResId, vehicleClassification)

            // Define the restricted characters
            val restrictedChars = setOf('.', '#', '$', '[', ']')

            // Check for restricted characters
            if (restrictedChars.any { it in vehiclePlateNumber }) {
                binding.etVehiclePlateNumber.error = "Plate number cannot contain: ${restrictedChars.joinToString("")}"
                return@setOnClickListener // Stop further processing
            }

            if (vehicleName.isEmpty() || vehiclePlateNumber.isEmpty() || selectedImageResId == -1 || selectedImageResId == 0) {
                if (vehicleName.isEmpty() && vehiclePlateNumber.isEmpty() && selectedImageResId == -1 || selectedImageResId == 0){
                    Toast.makeText(requireContext(), "Please supply the necessary details", Toast.LENGTH_SHORT).show()
                }else{
                    if (vehicleName.isEmpty()) {
                        binding.etVehicleName.error = "Vehicle name is required"
                    }
                    if (vehiclePlateNumber.isEmpty()) {
                        binding.etVehiclePlateNumber.error = "Vehicle plate number is required"
                    }
                    if (selectedImageResId == -1 || selectedImageResId == 0) {
                        Toast.makeText(requireContext(), "Please select a vehicle type", Toast.LENGTH_SHORT).show()
                    }
               }
                return@setOnClickListener
            }else{

                Log.e("Submit", "Button Clicked")

                //upload to database
                databaseReference = FirebaseDatabase.getInstance().getReference("Vehicle Information")

                // Check if vehiclePlateNumber already exists
                databaseReference.child(vehiclePlateNumber).addListenerForSingleValueEvent(object :
                    ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            Log.e("Submit", "Duplicate on database")
                            // Plate number already exists, show error
                            binding.etVehiclePlateNumber.error = "Plate number already registered"
                        } else {
                            Log.e("Submit", "Uploaded to database")
                            // Plate number is unique, proceed with upload
                            databaseReference.child(vehiclePlateNumber).setValue(vehicle)

                            vehicleViewModel.addVehicle(vehicle)

                            val successDialog = SuccessDialogFragment()
                            successDialog.show(parentFragmentManager, "successDialog")
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("FirebaseError", "Error checking plate number: ${error.message}")
                    }
                })

            }

        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class AllCapsLengthFilter(private val maxLength: Int) : InputFilter {
        override fun filter(
            source: CharSequence,
            start: Int,
            end: Int,
            dest: Spanned,
            dstart: Int,
            dend: Int
        ): CharSequence? {
            val uppercaseSource = source.toString().uppercase()
            val potentialResult = StringBuilder()
                .append(dest.subSequence(0, dstart))
                .append(uppercaseSource)
                .append(dest.subSequence(dend, dest.length))

            return if (potentialResult.length <= maxLength) {
                uppercaseSource
            } else {
                "" // Prevent input beyond maxLength
            }
        }
    }



}

