package com.example.vehiclespage

import EditedDialogFragment
import SuccessDialogFragment
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.example.vehiclespage.databinding.FragmentAddVehiclesBinding
import com.example.vehiclespage.databinding.FragmentEditVehiclesBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class EditVehicles : Fragment(R.layout.fragment_edit_vehicles) {
    private var _binding: FragmentEditVehiclesBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: vehicleSpinnerAdapter
    val vehicleViewModel: vehicleViewModel by activityViewModels()
    private var selectedImageResId: Int = -1 // Variable to store selected image resource ID
    private lateinit var databaseReference : DatabaseReference

    companion object {
        const val ARG_VEHICLE_NAME = "arg_vehicle_name"
        const val ARG_VEHICLE_PLATE = "arg_vehicle_plate"
        const val ARG_VEHICLE_IMAGE = "arg_vehicle_image"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditVehiclesBinding.inflate(inflater, container, false)
        val spinner = binding.spVehicles // Reference spinner from layout

        val items = mutableListOf(
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
                if (selectedItem != null) {
                    handleSelectedItem(selectedItem)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle as needed
            }
        }

        // Get the default image resource ID from arguments
        val defaultImageResId = arguments?.getInt(ARG_VEHICLE_IMAGE, -1) ?: -1

        // Find the index of the item with the default image
        val defaultIndex = (0 until adapter.count).find {
            adapter.getItem(it)?.carPic == defaultImageResId
        }

        // Set the default selection
        if (defaultIndex != null) {
            spinner.setSelection(defaultIndex)
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
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEditVehiclesBinding.bind(view)
        databaseReference = FirebaseDatabase.getInstance().getReference("Vehicle Information")
        binding.etVehiclePlateNumber.filters = arrayOf<InputFilter>(AllCapsLengthFilter(10))//filter 1

        val vehicleName = arguments?.getString(ARG_VEHICLE_NAME)
        val vehiclePlate = arguments?.getString(ARG_VEHICLE_PLATE)
        val vehicleImageResId = arguments?.getInt(ARG_VEHICLE_IMAGE)

        if (vehicleName != null && vehiclePlate != null && vehicleImageResId != null) {
            binding.etVehicleName.setText(vehicleName)
            binding.etVehiclePlateNumber.setText(vehiclePlate)
            selectedImageResId = vehicleImageResId // Set the initial selected image

            // Find the index of the item with the matching image resource ID
            val defaultPosition = adapter.getItems().indexOfFirst { it.carPic == vehicleImageResId }
            if (defaultPosition != -1) {
                binding.spVehicles.setSelection(defaultPosition)
            }
        }

        binding.btnBack.setOnClickListener {
            val myVehicles = myVehicles()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, myVehicles) // Use requireActivity() here
                .addToBackStack(null) // Optional: Add to back stack for navigation
                .commit()
        }

        binding.btnDone.setOnClickListener{
            val updatedVehicleName = binding.etVehicleName.text.toString()
            val updatedVehiclePlateNumber = binding.etVehiclePlateNumber.text.toString()
            val updatedVehicle = vehicleProfile(updatedVehicleName, updatedVehiclePlateNumber, selectedImageResId)

            //  filter 2: Define the restricted characters
            val restrictedChars = setOf('.', '#', '$', '[', ']')

            // Check for restricted characters
            if (restrictedChars.any { it in updatedVehiclePlateNumber }) {
                binding.etVehiclePlateNumber.error = "Plate number cannot contain: ${restrictedChars.joinToString("")}"
                return@setOnClickListener // Stop further processing
            }

            if (updatedVehicleName.isEmpty() || updatedVehiclePlateNumber.isEmpty() || selectedImageResId == -1) {
                if (updatedVehicleName.isEmpty() && updatedVehiclePlateNumber.isEmpty() && selectedImageResId == -1){
                    Toast.makeText(requireContext(), "Please supply the necessary details", Toast.LENGTH_SHORT).show()
                }else{
                    if (updatedVehicleName.isEmpty()) {
                        binding.etVehicleName.error = "Vehicle name is required"
                    }
                    if (updatedVehiclePlateNumber.isEmpty()) {
                        binding.etVehiclePlateNumber.error = "Vehicle plate number is required"
                    }
                    if (selectedImageResId == -1) {
                        Toast.makeText(requireContext(), "Please select a vehicle type", Toast.LENGTH_SHORT).show()
                    }
                }
                return@setOnClickListener
            }else{
                // Handle plate number change
                vehiclePlate?.let { originalPlate ->
                    if (originalPlate != updatedVehiclePlateNumber) {
                        // Plate number has changed, delete old record and add new one
                        databaseReference.child(originalPlate).removeValue() // Delete old record
                            .addOnSuccessListener {
                                // Add new record with updated plate number
                                databaseReference.child(updatedVehiclePlateNumber).setValue(updatedVehicle)
                                    .addOnSuccessListener {
                                        val editedDialog = EditedDialogFragment()
                                        editedDialog.show(parentFragmentManager, "editedDialog")
                                    }
                                    .addOnFailureListener {
                                        // Handle update failure
                                        // ... (show error message) ...
                                    }
                            }
                            .addOnFailureListener {
                                // Handle deletion failure
                                // ... (show error message) ...
                            }
                    } else {
                        // Plate number remains the same, just update the other fields
                        databaseReference.child(originalPlate).setValue(updatedVehicle)
                            .addOnSuccessListener {
                                val editedDialog = EditedDialogFragment()
                                editedDialog.show(parentFragmentManager, "editedDialog")
                            }
                            .addOnFailureListener {
                                // Handle update failure
                                // ... (show error message) ...
                            }
                    }

                }
            }

        }


    }

    //filter 3
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


