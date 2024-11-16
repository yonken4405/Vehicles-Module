package com.example.vehiclespage.BranchSelectionPage

import AddOn
import TimeSlot

data class Reservation(
    val services: List<Service>,  // Changed to a list of services
    val addOns: List<AddOn>,
    val timeSlot: TimeSlot
)

