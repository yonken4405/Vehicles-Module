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
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vehiclespage.R
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.Random

class ChooseServicesFragment : Fragment() {
    private lateinit var btnConfirmBooking: ImageButton // Declare btnConfirmBooking at the class level

    private lateinit var databaseReference: DatabaseReference

    private var branchName: String? = null
    private var branchAddress: String? = null
    private var branchContact: String? = null
    private var branchSchedule: String? = null

    private lateinit var serviceAdapter: ServiceAdapter
    private lateinit var addOnsAdapter: AddOnsAdapter
    private lateinit var timeSlotAdapter: TimeSlotAdapter

    private var selectedServices: List<Service> = listOf()
    private var selectedAddOns: List<AddOn> = listOf()
    private var selectedTimeSlot: TimeSlot? = null

    private var totalEstimatedTime: Int = 0 // in hours

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            branchName = it.getString("branchName")
            branchAddress = it.getString("branchAddress")
            branchContact = it.getString("branchContact")
            branchSchedule = it.getString("branchSchedule")
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
        val line1 = view.findViewById<View>(R.id.line1)

        line1.setBackgroundColor(resources.getColor(R.color.colorPrimaryYellow))
        circle1.setBackgroundResource(R.drawable.circle_yellow)

        // Initialize btnConfirmBooking here
        btnConfirmBooking = view.findViewById(R.id.btnConfirmBooking)

        // Initially hide the confirm booking button
        btnConfirmBooking.visibility = View.GONE

        databaseReference = FirebaseDatabase.getInstance().reference

        val serviceRecyclerView = view.findViewById<RecyclerView>(R.id.rvServiceOptions)
        val addOnsRecyclerView = view.findViewById<RecyclerView>(R.id.rvAddOns)
        val timeSlotRecyclerView = view.findViewById<RecyclerView>(R.id.rvTimeSlots)

        serviceAdapter = ServiceAdapter(getServices()) { selectedService ->
            this.selectedServices = selectedService
            updateTotalTime()
        }

        addOnsAdapter = AddOnsAdapter(getAddOns()) { selectedAddOn ->
            this.selectedAddOns = selectedAddOn
            updateTotalTime()
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

        // Load time slots for the current date by default
        loadAvailableTimeSlots(defaultDate)

        btnSelectDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
                val selectedDate ="${month + 1} /$dayOfMonth/$year"
                btnSelectDate.text = selectedDate
                loadAvailableTimeSlots(selectedDate)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

            datePickerDialog.show()
        }

        // Confirm booking logic
        btnConfirmBooking.setOnClickListener {
            if (selectedServices.isNotEmpty() && selectedTimeSlot != null) {
                // Pass the selected details to the vehicle selection page
                updateTotalTime()
                val bundle = Bundle().apply {
                    putParcelableArrayList("services", ArrayList(selectedServices))
                    putParcelableArrayList("addOns", ArrayList(selectedAddOns))
                    putString("timeSlot", selectedTimeSlot?.time.toString()) // Pass only the time
                    putString("date", btnSelectDate.text.toString())
                    putString("branchName", branchName)
                    putString("branchAddress", branchAddress)
                    putInt("totalEstimatedTime", totalEstimatedTime)
                }
                val fragment = VehicleSelectionFragment()
                fragment.arguments = bundle

                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit()
            } else {
                Toast.makeText(requireContext(), "Please select service and time", Toast.LENGTH_SHORT).show()
            }
        }

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

    private fun getServices(): List<Service> {
        return listOf(
            Service("Body Wash", 150.0, 2, "Sedan"),
            Service("Value Wash", 185.0, 2, "SUV")
        )
    }

    private fun getAddOns(): List<AddOn> {
        return listOf(
            AddOn("Armour All", 100.0, 1),
            AddOn("Under Chassis", 200.0, 1)
        )
    }
}

