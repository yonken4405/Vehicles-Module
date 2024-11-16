package com.example.vehiclespage.BranchSelectionPage

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Service(
    val name: String,
    val sedanPrice: Double,
    val suvPrice: Double,
    val pickupPrice: Double,
    val estimatedTime: Int
) : Parcelable {
    // Returns the price based on vehicle type
    fun getPriceForClassification(classification: String): Double {
        return when (classification) {
            "Sedan" -> sedanPrice
            "SUV" -> suvPrice
            "Pickup" -> pickupPrice
            else -> sedanPrice // Default to Sedan if unknown
        }
    }
}
