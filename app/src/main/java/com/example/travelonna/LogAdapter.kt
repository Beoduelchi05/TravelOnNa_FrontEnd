package com.example.travelonna

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.travelonna.model.TravelLog
import com.example.travelonna.model.TravelPlace
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

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
                
                // 각 장소의 일자 계산
                val dayVisitMap = assignDaysToPlaces(log.date, log.places)
                
                // 장소 정보 전달 (간단하게 하기 위해 인덱스로 전달)
                log.places.forEachIndexed { index, place ->
                    putExtra("PLACE_NAME_$index", place.name)
                    putExtra("PLACE_ADDRESS_$index", place.address)
                    putExtra("PLACE_TIME_$index", place.visitTime)
                    
                    // 계산된 일자 정보 전달
                    val dayVisit = dayVisitMap[place] ?: 1
                    putExtra("PLACE_DAY_$index", dayVisit)
                }
            }
            holder.itemView.context.startActivity(intent)
        }
    }

    // 각 장소에 일자를 할당하는 메서드
    private fun assignDaysToPlaces(dateString: String, places: List<TravelPlace>): Map<TravelPlace, Int> {
        val result = mutableMapOf<TravelPlace, Int>()
        
        // 날짜 형식 파싱
        val parts = dateString.split(" - ")
        if (parts.size == 2) {
            val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
            
            try {
                val startDate = dateFormat.parse(parts[0])
                val endDate = dateFormat.parse(parts[1])
                
                if (startDate != null && endDate != null) {
                    // 날짜 차이 계산 (일 수)
                    val diffInMillis = endDate.time - startDate.time
                    val dayCount = (TimeUnit.MILLISECONDS.toDays(diffInMillis) + 1).toInt()
                    
                    if (dayCount <= 1) {
                        // 하루짜리 여행인 경우 모든 장소를 첫째 날로 설정
                        places.forEach { place ->
                            result[place] = 1
                        }
                    } else {
                        // 여러 날의 여행인 경우 장소를 균등하게 분배
                        val placesPerDay = (places.size / dayCount).coerceAtLeast(1)
                        
                        places.forEachIndexed { index, place ->
                            // 기존에 지정된 dayVisit이 있고 유효한 범위인 경우 그 값 사용
                            if (place.dayVisit in 1..dayCount) {
                                result[place] = place.dayVisit
                            } else {
                                // 균등 배분 (인덱스 기반)
                                val day = (index / placesPerDay) + 1
                                result[place] = if (day <= dayCount) day else dayCount
                            }
                        }
                    }
                    
                    return result
                }
            } catch (e: Exception) {
                // 파싱 오류 시 모든 장소를 첫째 날로 설정
                places.forEach { place ->
                    result[place] = 1
                }
            }
        } else {
            // 날짜 형식이 잘못된 경우 모든 장소를 첫째 날로 설정
            places.forEach { place ->
                result[place] = 1
            }
        }
        
        return result
    }

    override fun getItemCount(): Int = logs.size
} 