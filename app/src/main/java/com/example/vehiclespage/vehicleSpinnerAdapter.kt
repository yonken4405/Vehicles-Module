package com.example.vehiclespage

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.vehiclespage.databinding.VehiclesDropdownBinding

class vehicleSpinnerAdapter(context: Context, private val items: List<spinnerItems>) :
    ArrayAdapter<spinnerItems>(context, 0, items) { // Use 0 for resource ID

    // Add a getter for the items list
    fun getItems(): List<spinnerItems> {
        return items
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createItemView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createItemView(position, convertView, parent)
    }

    private fun createItemView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding = if (convertView == null) {
            VehiclesDropdownBinding.inflate(LayoutInflater.from(context), parent, false)
        } else {
            VehiclesDropdownBinding.bind(convertView)
        }

        val item = getItem(position) ?: return binding.root  // Handle null item gracefully


        binding.ivCarPic.setImageResource(item.carPic)
        binding.tvCarType.text = item.carType
        binding.tvExample.text = item.carEx // Use the correct property name



        return binding.root
    }
}