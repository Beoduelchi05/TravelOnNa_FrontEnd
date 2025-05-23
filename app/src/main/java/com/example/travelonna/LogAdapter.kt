package com.example.travelonna

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// 여행 장소 데이터 클래스
data class TravelPlace(
    val name: String,
    val address: String,
    val visitTime: String
)

// 여행 로그 데이터 클래스
data class TravelLog(
    val title: String,
    val date: String,
    val type: String,
    val places: List<TravelPlace> = listOf() // 장소 목록 추가
)

// RecyclerView 어댑터
class LogAdapter(private val logs: List<TravelLog>) : 
    RecyclerView.Adapter<LogAdapter.LogViewHolder>() {

    class LogViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleText: TextView = view.findViewById(R.id.travelTitle)
        val dateText: TextView = view.findViewById(R.id.travelDate)
        val typeText: TextView = view.findViewById(R.id.travelType)
        val itemContainer: View = view
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_travel_log, parent, false)
        return LogViewHolder(view)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        val log = logs[position]
        holder.titleText.text = log.title
        holder.dateText.text = log.date
        holder.typeText.text = log.type
        
        // 아이템 클릭 이벤트 처리
        holder.itemContainer.setOnClickListener {
            val intent = Intent(holder.itemView.context, TravelDetailActivity::class.java).apply {
                putExtra("TRAVEL_TITLE", log.title)
                putExtra("TRAVEL_DATE", log.date)
                putExtra("TRAVEL_TYPE", log.type)
                putExtra("TRAVEL_PLACES_COUNT", log.places.size)
                
                // 장소 정보 전달 (간단하게 하기 위해 인덱스로 전달)
                log.places.forEachIndexed { index, place ->
                    putExtra("PLACE_NAME_$index", place.name)
                    putExtra("PLACE_ADDRESS_$index", place.address)
                    putExtra("PLACE_TIME_$index", place.visitTime)
                }
            }
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount() = logs.size
} 