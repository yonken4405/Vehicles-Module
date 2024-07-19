package com.example.vehiclespage

import DeletedDialogFragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vehiclespage.databinding.FragmentMyVehiclesBinding

class myVehicles : Fragment(R.layout.fragment_my_vehicles), vehicleAdapter.OnVehicleDeletedListener {
    private var _binding: FragmentMyVehiclesBinding? = null
    private val binding get() = _binding!!

    val vehicleViewModel: vehicleViewModel by activityViewModels()//after making viewModelClass

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMyVehiclesBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnAdd.setOnClickListener {
            val addVehicles = addVehicles()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, addVehicles) // Use requireActivity() here
                .addToBackStack(null) // Optional: Add to back stack for navigation
                .commit()
        }



            //10. declare variables
            // Set up the RecyclerView AFTER observing LiveData
        val vehicleAdapter = vehicleAdapter(mutableListOf(), vehicleViewModel)
        binding.rvVehicles.adapter = vehicleAdapter
        binding.rvVehicles.layoutManager = LinearLayoutManager(requireContext())


        //View Model: 5. basically updates the rv when a new vehicle is added
        vehicleViewModel.vehicleList.observe(viewLifecycleOwner) { vehicles ->
            vehicleAdapter.vehicles = vehicles // Update the adapter's list directly
            vehicleAdapter.notifyDataSetChanged()
            Log.d("Fragment", "Adapter item count: ${vehicleAdapter.itemCount}")
        }

        // *** ADD THIS LINE ***
        vehicleAdapter.setOnVehicleDeletedListener(this)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onVehicleDeleted() {
        val deletedDialog = DeletedDialogFragment()
        deletedDialog.show(childFragmentManager, "deletedDialog")
    }






}