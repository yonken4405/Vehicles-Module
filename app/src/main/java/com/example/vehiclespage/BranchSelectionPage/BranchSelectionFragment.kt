package com.example.vehiclespage.BranchSelectionPage

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.example.vehiclespage.R
import com.example.vehiclespage.databinding.FragmentGoogleMapsBinding
import com.example.vehiclespage.databinding.BranchProfileBottomSheetBinding
import com.example.vehiclespage.myVehicles
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.android.material.bottomsheet.BottomSheetDialog

class BranchSelectionFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentGoogleMapsBinding? = null
    private val binding get() = _binding!!
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var databaseReference: DatabaseReference
    private val allProfiles = mutableListOf<BranchProfile>() // Store all profiles
    private val displayedProfiles = mutableListOf<BranchProfile>() // Store currently displayed profiles


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGoogleMapsBinding.inflate(inflater, container, false)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        // Change to the correct reference for your branch profiles
        databaseReference = FirebaseDatabase.getInstance().getReference("BranchProfile")


        // Change the color of the search icon
        val searchIcon = binding.searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_mag_icon)
        searchIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.lighterFont), PorterDuff.Mode.SRC_IN)

        // Set the text color and hint color for the search input
        val searchText = binding.searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchText.setTextColor(ContextCompat.getColor(requireContext(), R.color.primaryFont)) // Input text color
        searchText.setHintTextColor(ContextCompat.getColor(requireContext(), R.color.lighterFont)) // Hint text color
        searchText.textSize = 14f
        // Load custom font from res/font directory
        val typeface: Typeface? = ResourcesCompat.getFont(requireContext(), R.font.inter_semibold)

        // Apply the custom font to the SearchView text
        searchText.typeface = typeface



        // Setup the search view listener
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterBranches(newText)
                return true
            }
        })

        binding.btnBack.setOnClickListener {
            switchToNextFragment()
        }

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        return binding.root
    }

    private fun switchToNextFragment() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, myVehicles())
            .addToBackStack(null)
            .commit()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.map = googleMap

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        map.isMyLocationEnabled = true

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val userLocation = LatLng(it.latitude, it.longitude)
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 14f))
            } ?: run {
                Toast.makeText(requireContext(), "Unable to get current location", Toast.LENGTH_SHORT).show()
            }
        }

        fetchProfiles()
    }

    private fun fetchProfiles() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(@NonNull dataSnapshot: DataSnapshot) {
                allProfiles.clear() // Clear previous profiles
                displayedProfiles.clear() // Clear currently displayed profiles
                map.clear() // Clear previous markers

                for (profileSnapshot in dataSnapshot.children) {
                    val profile = profileSnapshot.getValue(BranchProfile::class.java)
                    profile?.let {
                        allProfiles.add(it) // Add to all profiles
                        displayedProfiles.add(it) // Add to displayed profiles
                        addMarkerToMap(it)
                    }
                }
            }

            override fun onCancelled(@NonNull databaseError: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load profiles: ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun filterBranches(query: String?) {
        displayedProfiles.clear()
        if (query.isNullOrEmpty()) {
            displayedProfiles.addAll(allProfiles) // Show all profiles if query is empty
        } else {
            val filteredProfiles = allProfiles.filter { profile ->
                profile.name.contains(query, ignoreCase = true) || // Search by branch name
                        profile.address.contains(query, ignoreCase = true) // Search by address
            }
            displayedProfiles.addAll(filteredProfiles) // Add filtered profiles to displayed
        }

        map.clear() // Clear previous markers
        displayedProfiles.forEach { addMarkerToMap(it) } // Re-add markers for displayed profiles
    }

    private fun addMarkerToMap(profile: BranchProfile) {
        val location = LatLng(profile.latitude, profile.longitude)
        val marker = map.addMarker(MarkerOptions().position(location).title(profile.address))

        marker?.tag = profile

        map.setOnMarkerClickListener { clickedMarker ->
            clickedMarker.tag?.let { tag ->
                if (tag is BranchProfile) {
                    showProfileBottomSheet(tag)
                }
            }
            true
        }
    }

    private fun showProfileBottomSheet(profile: BranchProfile) {
        // Check for location permission
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request permission if not granted
            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE)
            return // Exit the function until permission is granted
        }

        // Get the last location
        val lastLocationTask = fusedLocationClient.lastLocation
        lastLocationTask.addOnSuccessListener { location: Location? ->
            location?.let {
                val bottomSheetBinding = BranchProfileBottomSheetBinding.inflate(layoutInflater)
                val bottomSheetDialog = BottomSheetDialog(requireContext())
                bottomSheetDialog.setContentView(bottomSheetBinding.root)

                bottomSheetBinding.tvName.text = profile.name
                bottomSheetBinding.tvAddress.text = profile.address
                bottomSheetBinding.tvContactNumber.text = profile.contact_number
                bottomSheetBinding.tvSchedule.text = profile.schedule

                // Calculate and display distance
                val userLocation = LatLng(it.latitude, it.longitude)
                val distance = calculateDistance(userLocation, LatLng(profile.latitude, profile.longitude))
                bottomSheetBinding.tvDistance.text = formatDistance(distance)

                bottomSheetDialog.show()

                bottomSheetBinding.orderButton.setOnClickListener{
                    bottomSheetDialog.dismiss() // Close the bottom sheet
                    navigateToChooseServicesFragment(profile)
                }

            } ?: run {
                Toast.makeText(requireContext(), "Unable to get current location", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(requireContext(), "Failed to get location: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToChooseServicesFragment(profile: BranchProfile) {
        val bundle = Bundle().apply {
            putString("branchName", profile.name)
            putString("branchAddress", profile.address)
            putString("branchContact", profile.contact_number)
            putString("branchSchedule", profile.schedule)
        }

        val orderFragment = ChooseServicesFragment().apply {
            arguments = bundle
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, orderFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun calculateDistance(userLocation: LatLng, branchLocation: LatLng): Float {
        val locationA = Location("User Location").apply {
            latitude = userLocation.latitude
            longitude = userLocation.longitude
        }
        val locationB = Location("Branch Location").apply {
            latitude = branchLocation.latitude
            longitude = branchLocation.longitude
        }

        return locationA.distanceTo(locationB) // Returns distance in meters
    }

    private fun formatDistance(distance: Float): String {
        return if (distance >= 1000) {
            String.format("%.2f km", distance / 1000) // Convert to kilometers
        } else {
            String.format("%.0f m", distance) // Keep it in meters
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}
