package com.example.vehiclespage.BranchSelectionPage

import TimeSlot
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.vehiclespage.R

class TimeSlotAdapter(
    private var timeSlots: List<TimeSlot>,
    private val selectedServices: List<Service>, // Updated to handle multiple services
    private val onTimeSlotSelected: (TimeSlot) -> Unit
) : RecyclerView.Adapter<TimeSlotAdapter.TimeSlotViewHolder>() {

    private var selectedTimeSlotPosition: Int = -1

    inner class TimeSlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val timeTextView = itemView.findViewById<TextView>(R.id.tvTimeSlot)

        fun bind(timeSlot: TimeSlot, isSelected: Boolean) {
            // Format the time into a readable format (6:00 AM, 7:00 AM, etc.)
            val timeFormatted = formatTime(timeSlot.time)

            timeTextView.text = timeFormatted

            // Calculate the total time required by selected services
            val totalServiceTime = calculateTotalServiceTime()

            // Validate if the time slot fits the selected services' total duration
            val isAvailable = timeSlot.isAvailable && isTimeSlotValidForServices(timeSlot, totalServiceTime)
            timeTextView.setTextColor(
                if (isAvailable) ContextCompat.getColor(itemView.context, R.color.secondaryFont)
                else ContextCompat.getColor(itemView.context, R.color.disabledTextColor)
            )

            itemView.isEnabled = isAvailable
            itemView.setBackgroundResource(
                if (isSelected && isAvailable) R.drawable.selected_background else R.drawable.default_background
            )

            itemView.setOnClickListener {
                if (isAvailable) {
                    val previousSelectedPosition = selectedTimeSlotPosition
                    selectedTimeSlotPosition = adapterPosition
                    notifyItemChanged(previousSelectedPosition)
                    notifyItemChanged(selectedTimeSlotPosition)
                    onTimeSlotSelected(timeSlot)
                }
            }
        }

        // Format the time into a readable format (6:00 AM, 7:00 AM, etc.)
        private fun formatTime(hour: Int): String {
            return when (hour) {
                in 6..11 -> "${hour}:00 AM"
                in 12..15 -> "${hour - 12}:00 PM"
                else -> "Invalid time"
            }
        }

        // Calculate the total estimated time for all selected services
        private fun calculateTotalServiceTime(): Int {
            return selectedServices.sumOf { it.estimatedTime }
        }

        // Check if the selected services fit within the time slot
        private fun isTimeSlotValidForServices(timeSlot: TimeSlot, totalServiceTime: Int): Boolean {
            // Calculate if the total service time fits within the given time slot (assuming each slot is 1 hour)
            return timeSlot.time + totalServiceTime <= 16 // Ensure the slot fits within operating hours (6 AM - 4 PM)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeSlotViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_time_slot, parent, false)
        return TimeSlotViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimeSlotViewHolder, position: Int) {
        holder.bind(timeSlots[position], position == selectedTimeSlotPosition)
    }

    override fun getItemCount(): Int = timeSlots.size

    fun getSelectedTimeSlot(): TimeSlot? = if (selectedTimeSlotPosition != -1) timeSlots[selectedTimeSlotPosition] else null

    // Add this method to update the time slots in the adapter
    fun updateTimeSlots(newTimeSlots: List<TimeSlot>) {
        timeSlots = newTimeSlots
        notifyDataSetChanged() // Refresh the list
    }
}

