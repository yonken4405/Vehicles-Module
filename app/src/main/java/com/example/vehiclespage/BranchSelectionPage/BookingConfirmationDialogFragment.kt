package com.example.vehiclespage.BranchSelectionPage

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.vehiclespage.R
import com.example.vehiclespage.VehiclesPage.myVehicles

class BookingConfirmationDialogFragment : DialogFragment() {

    private var bookingId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog) // Apply your full-screen dialog style
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.appointment_confirmed_dialog, container, false)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        // Set the dialog to be full screen
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        // Optionally, set the background color (if needed)
        dialog.window?.setBackgroundDrawableResource(R.color.colorPrimaryYellow)

        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set the booking ID passed to the dialog
        bookingId = arguments?.getString("bookingId")
        val bookingIdTextView: TextView = view.findViewById(R.id.appointmentId)
        val myBookingsButton: Button = view.findViewById(R.id.myBookingsBtn)
        val homeBtn: Button = view.findViewById(R.id.homeBtn)

        // Set the booking ID to the TextView
        bookingIdTextView.text = bookingId

        // Handle the "My Bookings" button click
        myBookingsButton.setOnClickListener {
            dismiss()
            val myVehicles = myVehicles()  // Assuming 'myVehicles' is your fragment
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, myVehicles)
                .addToBackStack(null)
                .commit()
        }

        // Handle the "My Bookings" button click
        homeBtn.setOnClickListener {
            dismiss()
            val myVehicles = myVehicles()  // Assuming 'myVehicles' is your fragment
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, myVehicles)
                .addToBackStack(null)
                .commit()
        }

        // Optional: Close the dialog when clicking outside the dialog (similar to `view.setOnClickListener` in `com.example.vehiclespage.VehiclesPage.DeletedDialogFragment`)
        view.setOnClickListener {
            dismiss() // Close the dialog
        }
    }

    companion object {
        fun newInstance(bookingId: String): BookingConfirmationDialogFragment {
            val fragment = BookingConfirmationDialogFragment()
            val bundle = Bundle()
            bundle.putString("bookingId", bookingId)
            fragment.arguments = bundle
            return fragment
        }
    }
}
