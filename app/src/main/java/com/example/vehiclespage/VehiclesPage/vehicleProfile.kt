package com.example.vehiclespage.VehiclesPage

data class vehicleProfile (
    val vname: String,
    val vplateNumber: String,
    val vehicleImageResId: Int,
    val vehicleClassification: String
) {
    // Add a no-argument constructor
    constructor() : this("", "", 0, "")


}