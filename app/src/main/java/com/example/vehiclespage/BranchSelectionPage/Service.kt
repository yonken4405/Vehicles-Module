package com.example.vehiclespage.BranchSelectionPage

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Service(
    val name: String,
    val price: Double,
    val estimatedTime: Int, // Estimated time in hours
    val serviceType: String // e.g., Sedan, SUV
) : Parcelable
