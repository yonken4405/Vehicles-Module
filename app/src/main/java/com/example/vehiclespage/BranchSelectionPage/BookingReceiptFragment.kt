package com.example.vehiclespage.BranchSelectionPage

import AddOn
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vehiclespage.R
import com.example.vehiclespage.databinding.FragmentBookingReceiptBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Random


class BookingReceiptFragment : Fragment() {

    private var _binding: FragmentBookingReceiptBinding? = null
    private val binding get() = _binding!!

    private lateinit var servicesAdapter: OrderSummaryAdapter
    private lateinit var addOnsAdapter: OrderSummaryAdapter

    private lateinit var databaseReference : DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBookingReceiptBinding.inflate(inflater, container, false)
        //databaseReference = FirebaseDatabase.getInstance().getReference("Reservations")

        // Highlight all steps
        binding.line1.setBackgroundColor(resources.getColor(R.color.colorPrimaryYellow))
        binding.circle1.setBackgroundResource(R.drawable.circle_yellow)
        binding.line2.setBackgroundColor(resources.getColor(R.color.colorPrimaryYellow))
        binding.circle2.setBackgroundResource(R.drawable.circle_yellow)
        binding.line3.setBackgroundColor(resources.getColor(R.color.colorPrimaryYellow))
        binding.circle3.setBackgroundResource(R.drawable.circle_yellow)
        binding.line4.setBackgroundColor(resources.getColor(R.color.colorPrimaryYellow))

        // Fetching data from arguments (instead of intent)
        val branchName = arguments?.getString("branchName") ?: "DefaultBranch"

        databaseReference = FirebaseDatabase.getInstance().getReference("Branches")


        val branchAddress = arguments?.getString("branchAddress")
        val vehicleName = arguments?.getString("vehicleName")
        val plateNumber = arguments?.getString("plateNumber")
        val classification = arguments?.getString("classification")
        val appointmentDate = arguments?.getString("date")
        val appointmentTime = arguments?.getString("timeslot")
        //val bookingFee = arguments?.getDouble("bookingFee") ?: 20.0
        var amountDue = arguments?.getDouble("amountDue") ?: 0.0
        val paymentMethod = arguments?.getString("paymentMethod")
        val estimatedCompletion = arguments?.getInt("totalEstimatedTime")
        //val noteToBranch = arguments?.getString("noteToBranch")

        val services = arguments?.getParcelableArrayList<Service>("services")
        val addOns = arguments?.getParcelableArrayList<AddOn>("addOns")

        // Calculate the total amount due by adding the prices of services and add-ons
        val servicesTotal = services?.sumOf { service ->
            when (classification) {
                "Sedan" -> service.sedanPrice
                "SUV" -> service.suvPrice
                "Pickup" -> service.pickupPrice
                else -> 0.0 // Default to 0 if classification is unknown
            }
        } ?: 0.0

        val addOnsTotal = addOns?.sumOf { it.price } ?: 0.0
        amountDue += servicesTotal + addOnsTotal + 20  // Add service and add-on prices to amountDue + fee

        // Set data to the views using the binding object
        binding.branchName.text = branchName
        binding.branchAddress.text = branchAddress
        binding.vehicleName.text = vehicleName
        binding.vehicleDetails.text = "$plateNumber - $classification"
        binding.date.text = appointmentDate
        binding.time.text = appointmentTime
        binding.bookingFee.text = "₱20"
        binding.amountDue.text = "₱${"%.2f".format(amountDue)}"  // Display updated amount due
        binding.paymentMethod.text = paymentMethod
        binding.estimatedCompletion.text = estimatedCompletion.toString()
        // binding.feedbackText.text = noteToBranch

        // Combine services and add-ons into a single list
        val allItems = mutableListOf<Any>().apply {
            addAll(services ?: emptyList())  // Add services to the list
            addAll(addOns ?: emptyList())    // Add add-ons to the list
        }

        // Set up RecyclerView for both services and add-ons
        setupRecyclerView(binding.orderSummaryServices, allItems, classification ?: "")

        // Complete button logic
        val completeBtn = binding.completeButton
        completeBtn.setOnClickListener {
            // Generate Booking ID in format ND-123456
            val bookingId = generateBookingId()

            val feedbackNote = view?.findViewById<TextInputEditText>(R.id.feedbackText)?.text?.toString()?.trim()

            // Prepare data structure to upload to Firebase
            val reservationData = hashMapOf<String, Any>(
                //"branchName" to (branchName ?: ""),
                "branchAddress" to (branchAddress ?: ""),
                "vehicleName" to (vehicleName ?: ""),
                "plateNumber" to (plateNumber ?: ""),
                "classification" to (classification ?: ""),
                "paymentMethod" to (paymentMethod ?: ""),
                "note" to (feedbackNote ?: ""),
                "status" to ("Pending"),
                "services" to (services ?: emptyList<Service>()).map { service ->
                    mapOf(
                        "name" to service.name,
                        "estimatedTime" to service.estimatedTime,
                        "price" to service.getPriceForClassification(classification ?: "")
                    )
                },
                "addOns" to (addOns ?: emptyList<AddOn>()).map { addOn ->
                    mapOf(
                        "name" to addOn.name,
                        "estimatedTime" to addOn.estimatedTime,
                        "price" to addOn.price
                    )
                },
                "timeSlot" to mapOf(
                    "appointmentDate" to (appointmentDate ?: ""),
                    "time" to appointmentTime,
                    "available" to true // Assuming it's always available
                )
            )


            // Upload the reservation data to Firebase
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val reservationsRef = databaseReference.child(sanitizePathRemoveInvalidChars(branchName))//remove invalid chars

            reservationsRef.child("Reservations").child(currentDate).child(bookingId).setValue(reservationData)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Show the confirmation dialog with the booking ID
                        val confirmationDialog = BookingConfirmationDialogFragment.newInstance(bookingId)
                        confirmationDialog.show(requireActivity().supportFragmentManager, "BookingConfirmation")
                    } else {
                        // Handle failure case
                        Toast.makeText(requireContext(), "Failed to complete booking. Please try again.", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        binding.btnBack.setOnClickListener{
            parentFragmentManager.popBackStack()
        }

        return binding.root
    }

    fun sanitizePathRemoveInvalidChars(path: String): String {
        val invalidChars = listOf('.', '#', '$', '[', ']')
        return path.filterNot { it in invalidChars }
    }


    private fun setupRecyclerView(recyclerView: RecyclerView, items: List<Any>?, classification: String) {
        recyclerView.layoutManager = LinearLayoutManager(context)

        val adapter = OrderSummaryAdapter(items ?: emptyList(), classification)
        recyclerView.adapter = adapter
    }

    private fun generateBookingId(): String {
        // Generate random number for the booking ID
        val random = Random()
        val bookingId = "ND-" + (100000..999999).random()
        return bookingId
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // Avoid memory leaks by clearing the binding reference
    }
}
