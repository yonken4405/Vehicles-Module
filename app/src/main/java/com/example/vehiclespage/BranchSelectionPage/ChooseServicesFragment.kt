package com.example.vehiclespage.BranchSelectionPage

import AddOn
import TimeSlot
import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vehiclespage.R
import com.example.vehiclespage.vehicleProfile
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ChooseServicesFragment : Fragment() {
    private lateinit var btnConfirmBooking: ImageButton // Declare btnConfirmBooking at the class level
    private lateinit var databaseReference: DatabaseReference

//    private var branchName: String? = null
//    private var branchAddress: String? = null
//    private var branchContact: String? = null
//    private var branchSchedule: String? = null

    private var services: List<Service>? = null
    private var addOns: List<AddOn>? = null
    private var vehicleName: String? = null
    private var plateNumber: String? = null
    private var classification: String? = null
    private var timeSlot: String? = null
    private var selectedDate: String? = null
    private var branchName: String? = null
    private var branchAddress: String? = null
    private var selectedVehicle: vehicleProfile? = null // Store the selected vehicle
    //private var totalEstimatedTime: Int? = null // Store the selected vehicle

    private lateinit var serviceAdapter: ServiceAdapter
    private lateinit var addOnsAdapter: AddOnsAdapter
    private lateinit var timeSlotAdapter: TimeSlotAdapter

    private var selectedServices: List<Service> = listOf()
    private var selectedAddOns: List<AddOn> = listOf()
    private var selectedTimeSlot: TimeSlot? = null

    private var totalEstimatedTime: Int = 0 // in hours

    private lateinit var codRadio: RadioButton
    private lateinit var gcashRadio: RadioButton
    private lateinit var cardRadio: RadioButton
    private lateinit var confirmButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            vehicleName = it.getString("vehicleName")
            plateNumber = it.getString("plateNumber")
            classification = it.getString("classification")
            timeSlot = it.getString("timeSlot")
            selectedDate = it.getString("date")
            branchName = it.getString("branchName")
            branchAddress = it.getString("branchAddress")
            totalEstimatedTime = it.getInt("totalEstimatedTime")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_choose_services, container, false)

        // Initialize the current date
        val calendar = Calendar.getInstance()
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH) + 1 // Calendar month is zero-based
        val year = calendar.get(Calendar.YEAR)

        // Format the date as MM/dd/yy
        val simpleDateFormat = SimpleDateFormat("MM/dd/yy", Locale.getDefault())
        val defaultDate = simpleDateFormat.format(calendar.time)

        // Set the default date as text in the button
        val btnSelectDate = view.findViewById<Button>(R.id.btnSelectDate)
        btnSelectDate.text = defaultDate // Set the default date as text

        // Highlight all steps
        val circle1 = view.findViewById<View>(R.id.circle1)
        val circle2 = view.findViewById<View>(R.id.circle2)
        val line1 = view.findViewById<View>(R.id.line1)
        val line2 = view.findViewById<View>(R.id.line2)

        line1.setBackgroundColor(resources.getColor(R.color.colorPrimaryYellow))
        circle1.setBackgroundResource(R.drawable.circle_yellow)
        line2.setBackgroundColor(resources.getColor(R.color.colorPrimaryYellow))
        circle2.setBackgroundResource(R.drawable.circle_yellow)

        // Initialize btnConfirmBooking here
        btnConfirmBooking = view.findViewById(R.id.btnConfirmBooking)

        // Initially hide the confirm booking button
        btnConfirmBooking.visibility = View.GONE

        databaseReference = FirebaseDatabase.getInstance().reference

        // Initialize views
        codRadio = view.findViewById(R.id.cod_radio)
        gcashRadio = view.findViewById(R.id.gcash_radio)
        cardRadio = view.findViewById(R.id.card_radio)

        // Set listeners for manual toggling
        codRadio.setOnClickListener {
            gcashRadio.isChecked = false
            cardRadio.isChecked = false
        }

        gcashRadio.setOnClickListener {
            codRadio.isChecked = false
            cardRadio.isChecked = false
        }

        cardRadio.setOnClickListener {
            codRadio.isChecked = false
            gcashRadio.isChecked = false
        }

        val serviceRecyclerView = view.findViewById<RecyclerView>(R.id.rvServiceOptions)
        val addOnsRecyclerView = view.findViewById<RecyclerView>(R.id.rvAddOns)
        val timeSlotRecyclerView = view.findViewById<RecyclerView>(R.id.rvTimeSlots)

        serviceAdapter = ServiceAdapter(mutableListOf()) { selectedService ->
            this.selectedServices = selectedService
            updateTotalTime()
        }

        addOnsAdapter = AddOnsAdapter(mutableListOf()) { selectedAddOns ->
            this.selectedAddOns = selectedAddOns
            updateTotalTime() // Update total estimated time based on selected add-ons
        }

        timeSlotAdapter = TimeSlotAdapter(listOf(), selectedServices) { selectedTimeSlot ->
            this.selectedTimeSlot = selectedTimeSlot
            checkInputValidation() // Check input validation when a time slot is selected
        }

        serviceRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        serviceRecyclerView.adapter = serviceAdapter

        addOnsRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        addOnsRecyclerView.adapter = addOnsAdapter

        timeSlotRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        timeSlotRecyclerView.adapter = timeSlotAdapter

        // Load services from Firebase
        getServicesFromFirebase()

        // Load add-ons from Firebase
        getAddOnsFromFirebase()

        // Load time slots for the current date by default
        loadAvailableTimeSlots(defaultDate)

        btnSelectDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
                val selectedDate = "${month + 1}/$dayOfMonth/$year"
                btnSelectDate.text = selectedDate
                loadAvailableTimeSlots(selectedDate)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

            datePickerDialog.show()
        }

        // Confirm booking logic
        btnConfirmBooking.setOnClickListener {
            if (selectedServices.isNotEmpty() && selectedTimeSlot != null) {
                val selectedPaymentMethod = when {
                    codRadio.isChecked -> "COD"
                    gcashRadio.isChecked -> "E-Wallet"
                    cardRadio.isChecked -> "Card Payment"
                    else -> null
                }

                if (selectedPaymentMethod == null) {
                    Toast.makeText(requireContext(), "Please select a payment method", Toast.LENGTH_SHORT).show()
                } else {
                    // Pass the selected details to the booking receipt page
                    updateTotalTime()
                    val bundle = Bundle().apply {
                        putParcelableArrayList("services", ArrayList(selectedServices))
                        putParcelableArrayList("addOns", ArrayList(selectedAddOns))
                        putString("timeSlot", selectedTimeSlot?.time.toString()) // Pass only the time
                        putString("date", btnSelectDate.text.toString())
                        putString("branchName", branchName)
                        putString("branchAddress", branchAddress)
                        putInt("totalEstimatedTime", totalEstimatedTime)
                        putString("vehicleName", vehicleName)  // Pass vehicle name
                        putString("plateNumber", plateNumber)  // Pass plate number
                        putString("classification", classification)  // Pass classification
                        putString("paymentMethod", selectedPaymentMethod)  // Pass payment method
                    }
                    val fragment = BookingReceiptFragment()
                    fragment.arguments = bundle

                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, fragment)
                        .addToBackStack(null)
                        .commit()
                }
            } else {
                Toast.makeText(requireContext(), "Please select service and time", Toast.LENGTH_SHORT).show()
            }
        }

        // Load services from Firebase
        getServicesFromFirebase()

        return view
    }

    private fun checkInputValidation() {
        // Check if services, add-ons, and time slot are selected
        if (selectedServices.isNotEmpty() && selectedAddOns.isNotEmpty() && selectedTimeSlot != null) {
            // If all inputs are valid, show the confirm button
            btnConfirmBooking.visibility = View.VISIBLE
        } else {
            // If any input is missing, hide the confirm button
            btnConfirmBooking.visibility = View.GONE
        }
    }

    private fun updateTotalTime() {
        totalEstimatedTime = selectedServices.sumOf { it.estimatedTime } + selectedAddOns.sumOf { it.estimatedTime }
        checkInputValidation() // Check if the inputs are valid every time the total time is updated
    }

    private fun loadAvailableTimeSlots(date: String) {
        val operatingHours = 6..16
        val defaultTimeSlots = operatingHours.map { hour ->
            TimeSlot(time = hour, isAvailable = true)
        }
        timeSlotAdapter.updateTimeSlots(defaultTimeSlots)
    }

    fun sanitizePathRemoveInvalidChars(path: String): String {
        val invalidChars = listOf('.', '#', '$', '[', ']')
        return path.filterNot { it in invalidChars }
    }

    private fun getServicesFromFirebase() {
        databaseReference.child("Branches").child(sanitizePathRemoveInvalidChars(branchName ?: "")).child("Services").get().addOnSuccessListener { dataSnapshot ->
            val services = mutableListOf<Service>()
            dataSnapshot.children.forEach { snapshot ->
                val name = snapshot.child("name").getValue(String::class.java) ?: ""
                val sedanPrice = snapshot.child("sedanPrice").getValue(Double::class.java) ?: 0.0
                val suvPrice = snapshot.child("suvPrice").getValue(Double::class.java) ?: 0.0
                val pickupPrice = snapshot.child("pickupPrice").getValue(Double::class.java) ?: 0.0
                val estimatedTime = snapshot.child("estimatedTime").getValue(Int::class.java) ?: 0

                services.add(Service(name, sedanPrice, suvPrice, pickupPrice, estimatedTime))
            }

            // Now pass the selected classification to the service adapter
            val updatedServices = services.map { service ->
                service.copy(
                    // Update price based on the selected vehicle classification
                    sedanPrice = service.getPriceForClassification(classification ?: "Sedan")
                )
            }

            // Update the service adapter with the data from Firebase
            serviceAdapter.updateServices(updatedServices)
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Failed to load services", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getAddOnsFromFirebase() {
        val addOnsPath = sanitizePathRemoveInvalidChars(branchName ?: "")
        databaseReference.child("Branches").child(addOnsPath).child("AddOns")
            .get()
            .addOnSuccessListener { dataSnapshot ->
                // Create a mutable list to hold AddOn objects
                val addOns = mutableListOf<AddOn>()
                dataSnapshot.children.forEach { snapshot ->
                    // Safely retrieve data from snapshot
                    val name = snapshot.child("name").getValue(String::class.java) ?: ""
                    val price = snapshot.child("price").getValue(Double::class.java) ?: 0.0
                    val estimatedTime = snapshot.child("estimatedTime").getValue(Int::class.java) ?: 0

                    // Add each add-on to the list
                    addOns.add(AddOn(name, price, estimatedTime))
                }

                // Update the adapter with the fetched add-ons
                addOnsAdapter.updateAddOns(addOns)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load add-ons", Toast.LENGTH_SHORT).show()
            }
    }



}
