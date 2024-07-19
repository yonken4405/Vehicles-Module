package com.example.vehiclespage

data class vehicleProfile (
    val vname: String,
    val vplateNumber: String,
    val vehicleImageResId: Int
) {
    // Add a no-argument constructor
    constructor() : this("", "", 0)


}