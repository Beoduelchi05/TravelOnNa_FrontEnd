package com.example.travelonna.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.travelonna.R
import com.example.travelonna.model.ScheduleItem

class ScheduleItemAdapter(
    private var items: List<ScheduleItem> = emptyList()
) : RecyclerView.Adapter<ScheduleItemAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_travel_schedule, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<ScheduleItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val numberCircle: TextView = itemView.findViewById(R.id.numberCircle)
        private val placeNameText: TextView = itemView.findViewById(R.id.placeName)
        private val lockIcon: ImageView = itemView.findViewById(R.id.lockIcon)

        fun bind(item: ScheduleItem) {
            numberCircle.text = item.number.toString()
            
            if (item.isLocked) {
                lockIcon.visibility = View.VISIBLE
                placeNameText.visibility = View.GONE
                
                // Gray background for locked items
                itemView.setBackgroundResource(R.color.light_gray)
                
                // Use gray circle for locked items
                numberCircle.setBackgroundResource(R.drawable.circle_background_gray)
            } else {
                lockIcon.visibility = View.GONE
                placeNameText.visibility = View.VISIBLE
                placeNameText.text = item.placeName
                
                // White background for normal items
                itemView.setBackgroundResource(android.R.color.white)
                
                // Use blue circle for normal items
                numberCircle.setBackgroundResource(R.drawable.circle_background)
            }
        }
    }
} 