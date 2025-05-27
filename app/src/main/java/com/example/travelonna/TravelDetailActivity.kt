package com.example.travelonna

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.content.Intent
import android.widget.Toast
import android.widget.ToggleButton
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.example.travelonna.model.TravelPlace
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class TravelDetailActivity : AppCompatActivity() {

    private lateinit var titleTextView: TextView
    private lateinit var dateTextView: TextView
    private lateinit var typeTextView: TextView
    private lateinit var backButton: ImageView
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    // 날짜 관련 변수
    private lateinit var startDateCalendar: Calendar
    private lateinit var endDateCalendar: Calendar
    private var dayCount: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_travel_detail)

        // UI 요소 초기화
        titleTextView = findViewById(R.id.detailTitleTextView)
        dateTextView = findViewById(R.id.detailDateTextView)
        typeTextView = findViewById(R.id.detailTypeTextView)
        backButton = findViewById(R.id.backButton)
        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)

        // Intent에서 데이터 가져오기
        val title = intent.getStringExtra("TRAVEL_TITLE") ?: ""
        val date = intent.getStringExtra("TRAVEL_DATE") ?: ""
        val type = intent.getStringExtra("TRAVEL_TYPE") ?: ""
        val placesCount = intent.getIntExtra("TRAVEL_PLACES_COUNT", 0)

        // 기본 정보 표시
        titleTextView.text = title
        dateTextView.text = date
        typeTextView.text = type

        // 장소 목록 가져오기
        val places = mutableListOf<TravelPlace>()
        for (i in 0 until placesCount) {
            val name = intent.getStringExtra("PLACE_NAME_$i") ?: ""
            val address = intent.getStringExtra("PLACE_ADDRESS_$i") ?: ""
            val time = intent.getStringExtra("PLACE_TIME_$i") ?: ""
            val dayVisit = intent.getIntExtra("PLACE_DAY_$i", 1) // 기본값 1일차
            places.add(TravelPlace(name, address, time, dayVisit))
        }

        // 날짜 설정 (YYYY.MM.DD - YYYY.MM.DD 형식을 파싱)
        setupDates(date)

        // ViewPager 및 탭 설정
        setupViewPagerAndTabs(places)

        // 뒤로가기 버튼 설정
        backButton.setOnClickListener {
            finish()
        }
    }

    private fun setupDates(dateString: String) {
        val parts = dateString.split(" - ")
        if (parts.size == 2) {
            val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
            
            try {
                val startDate = dateFormat.parse(parts[0])
                val endDate = dateFormat.parse(parts[1])
                
                if (startDate != null && endDate != null) {
                    startDateCalendar = Calendar.getInstance()
                    endDateCalendar = Calendar.getInstance()
                    
                    startDateCalendar.time = startDate
                    endDateCalendar.time = endDate
                    
                    // 날짜 차이 계산 (일 수)
                    val diffInMillis = endDate.time - startDate.time
                    dayCount = (TimeUnit.MILLISECONDS.toDays(diffInMillis) + 1).toInt()
                }
            } catch (e: Exception) {
                dayCount = 1
                Toast.makeText(this, "날짜 형식을 파싱하는 데 오류가 발생했습니다", Toast.LENGTH_SHORT).show()
            }
        } else {
            dayCount = 1
        }
    }

    private fun setupViewPagerAndTabs(places: List<TravelPlace>) {
        // 일자별로 장소를 그룹화
        val dayPlaces = mutableMapOf<Int, MutableList<TravelPlace>>()
        
        // 각 일자별로 빈 목록 초기화
        for (day in 1..dayCount) {
            dayPlaces[day] = mutableListOf()
        }
        
        // 각 장소를 해당 일자에 할당
        for (place in places) {
            val day = place.dayVisit
            if (day in 1..dayCount) {
                dayPlaces[day]?.add(place)
            } else {
                // 잘못된 일자인 경우 첫째 날에 추가
                dayPlaces[1]?.add(place)
            }
        }
        
        // ViewPager 어댑터 설정
        val pagerAdapter = DayPagerAdapter(dayPlaces, dayCount)
        viewPager.adapter = pagerAdapter
        
        // TabLayout과 ViewPager 연결
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            val dayNumber = position + 1
            val dayCalendar = Calendar.getInstance()
            dayCalendar.time = startDateCalendar.time
            dayCalendar.add(Calendar.DAY_OF_MONTH, position)
            
            val dayOfWeek = SimpleDateFormat("E", Locale.KOREA).format(dayCalendar.time)
            val dayOfMonth = SimpleDateFormat("d", Locale.getDefault()).format(dayCalendar.time)
            
            tab.text = "Day $dayNumber"
            tab.setCustomView(R.layout.custom_tab_layout)
            val customView = tab.customView
            customView?.let {
                val tabDayText = it.findViewById<TextView>(R.id.tabDayNumber)
                val tabDateText = it.findViewById<TextView>(R.id.tabDateInfo)
                
                tabDayText.text = "Day $dayNumber"
                tabDateText.text = "$dayOfWeek, $dayOfMonth"
            }
        }.attach()
    }
}

// 일자별 페이저 어댑터
class DayPagerAdapter(
    private val dayPlaces: Map<Int, List<TravelPlace>>,
    private val dayCount: Int
) : androidx.recyclerview.widget.RecyclerView.Adapter<DayPagerAdapter.DayPageViewHolder>() {

    class DayPageViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerViewDayPlaces)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayPageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_day_page, parent, false)
        return DayPageViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayPageViewHolder, position: Int) {
        val day = position + 1
        val places = dayPlaces[day] ?: emptyList()
        
        holder.recyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.recyclerView.adapter = PlaceAdapter(places)
    }

    override fun getItemCount(): Int = dayCount
}

// 장소 어댑터
class PlaceAdapter(private val places: List<TravelPlace>) : 
    RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder>() {

    class PlaceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val placeImage: ImageView = view.findViewById(R.id.placeImage)
        val nameText: TextView = view.findViewById(R.id.placeName)
        val addressText: TextView = view.findViewById(R.id.placeAddress)
        val privacyToggle: ToggleButton = view.findViewById(R.id.privacyToggle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_travel_place, parent, false)
        return PlaceViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val place = places[position]
        holder.nameText.text = place.name
        holder.addressText.text = place.address
        
        // 이미지 설정 (여기서는 더미 이미지 사용)
        holder.placeImage.setImageResource(R.drawable.ic_place_holder)
        
        // 토글 버튼 설정 (예: 좋아요 기능 등)
        holder.privacyToggle.setOnCheckedChangeListener { _, isChecked ->
            // TODO: 좋아요 또는 즐겨찾기 등 기능 구현
        }
        
        // 아이템 클릭 이벤트 추가 - 기록 작성 화면으로 이동
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, PlaceMemoryActivity::class.java)
            intent.putExtra("PLACE_NAME", place.name)
            intent.putExtra("PLACE_ADDRESS", place.address)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = places.size
} 