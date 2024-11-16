package com.example.vehiclespage.BranchSelectionPage

import AddOn
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vehiclespage.R
import com.example.vehiclespage.VehiclesPage.vehicleProfile
import com.example.vehiclespage.VehiclesPage.vehicleViewModel

class VehicleSelectionFragment : Fragment() {

    private var branchContact: String? = null
    private var branchSchedule: String? = null

    private var services: List<Service>? = null
    private var addOns: List<AddOn>? = null
    private var timeSlot: String? = null
    private var selectedDate: String? = null
    private var branchName: String? = null
    private var branchAddress: String? = null
    private var selectedVehicle: vehicleProfile? = null // Store the selected vehicle
    private var totalEstimatedTime: Int? = null // Store the selected vehicle

    private lateinit var viewModel: vehicleViewModel // ViewModel for vehicles
    private lateinit var adapter: VehicleAdapter // Adapter for RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            branchName = it.getString("branchName")
            branchAddress = it.getString("branchAddress")
            branchContact = it.getString("branchContact")
            branchSchedule = it.getString("branchSchedule")
        }

//        arguments?.let {
//            services = it.getParcelableArrayList("services")
//            addOns = it.getParcelableArrayList("addOns")
//            timeSlot = it.getString("timeSlot")
//            selectedDate = it.getString("date")
//            branchName = it.getString("branchName")
//            branchAddress = it.getString("branchAddress")
//            totalEstimatedTime = it.getInt("totalEstimatedTime")
//        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_vehicle_selection, container, false)

        // Highlight all steps
        val circle1 = view.findViewById<View>(R.id.circle1)
        val line1 = view.findViewById<View>(R.id.line1)

        line1.setBackgroundColor(resources.getColor(R.color.colorPrimaryYellow))
        circle1.setBackgroundResource(R.drawable.circle_yellow)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[vehicleViewModel::class.java]

        // Initialize RecyclerView and Adapter
        val recyclerView = view.findViewById<RecyclerView>(R.id.rvVehicles)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())


        // Pass the callback function to the adapter
        adapter = VehicleAdapter(mutableListOf(), viewModel) { selectedVehicle ->
            this.selectedVehicle = selectedVehicle // Save selected vehicle details

            Log.d("VehicleSelectionFragment", "Selected Vehicle: ${selectedVehicle.vname}, Plate: ${selectedVehicle.vplateNumber}")
        }
        recyclerView.adapter = adapter

        // Observe the vehicle list from the ViewModel
        viewModel.vehicleList.observe(viewLifecycleOwner) { vehicles ->
            adapter.vehicles = vehicles // Update adapter's data
            adapter.notifyDataSetChanged() // Refresh the RecyclerView
        }

        // Button to navigate to the next fragment
        val btnSelectVehicle = view.findViewById<ImageButton>(R.id.btnSelectVehicle)
        btnSelectVehicle.setOnClickListener {
            if (selectedVehicle != null) {
                // Pass data to the next fragment
                val bundle = Bundle().apply {
                    //putParcelableArrayList("services", ArrayList(services))
                    //putParcelableArrayList("addOns", ArrayList(addOns))
                    //putString("timeSlot", timeSlot) // Pass only the time
                    //putString("date", selectedDate)
                    putString("branchName", branchName)
                    putString("branchAddress", branchAddress)
                    putString("vehicleName", selectedVehicle?.vname)  // Pass vehicle name
                    putString("plateNumber", selectedVehicle?.vplateNumber)  // Pass plate number
                    putString("classification", selectedVehicle?.vehicleClassification)  // Pass classification
                    //putInt("totalEstimatedTime", totalEstimatedTime ?: 0)
                }

                // Create the next fragment and set arguments
                val fragment = ChooseServicesFragment().apply {
                    arguments = bundle
                }

                // Navigate to the next fragment
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit()
            } else {
                // Handle the case where no vehicle is selected
                Toast.makeText(context, "Please select a vehicle", Toast.LENGTH_SHORT).show()
            }
        }

        val btnBack = view.findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener{
            parentFragmentManager.popBackStack()
        }

        return view
    }
}

