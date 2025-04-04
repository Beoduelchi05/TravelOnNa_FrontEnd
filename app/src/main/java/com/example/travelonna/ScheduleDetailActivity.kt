package com.example.travelonna

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import android.widget.HorizontalScrollView
import android.content.Intent
import android.widget.Toast
import com.example.travelonna.ui.schedule.AddPlaceActivity
import androidx.recyclerview.widget.ItemTouchHelper
import android.app.AlertDialog
import android.util.Log
import com.example.travelonna.api.PlanDetailResponse
import com.example.travelonna.api.PlaceDetailData
import com.example.travelonna.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ScheduleDetailActivity : AppCompatActivity() {

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var confirmButton: TextView
    
    private val startDateCalendar = Calendar.getInstance()
    private val endDateCalendar = Calendar.getInstance()
    private var dayCount = 0
    private var planId: Int = -1
    private var planTitle: String = ""
    
    // 각 일자별 장소 목록 저장
    private val dayPlaces: MutableMap<Int, MutableList<PlaceItem>> = mutableMapOf()
    
    companion object {
        private const val TAG = "ScheduleDetailActivity"
        private const val ADD_PLACE_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule_detail)
        
        // 뷰 초기화
        viewPager = findViewById(R.id.viewPager)
        confirmButton = findViewById(R.id.confirmButton)
        
        // 일정 ID 가져오기
        planId = intent.getIntExtra("PLAN_ID", -1)
        
        if (planId > 0) {
            // API를 통해 일정 상세 정보 조회
            fetchPlanDetail(planId)
        } else {
            // 인텐트에서 날짜 데이터 가져오기
            val startDate = intent.getLongExtra("START_DATE", System.currentTimeMillis())
            val endDate = intent.getLongExtra("END_DATE", System.currentTimeMillis())
            
            startDateCalendar.timeInMillis = startDate
            endDateCalendar.timeInMillis = endDate
            
            // 총 일수 계산
            val diffInMillis = endDateCalendar.timeInMillis - startDateCalendar.timeInMillis
            dayCount = TimeUnit.MILLISECONDS.toDays(diffInMillis).toInt() + 1
            
            // 일정 제목 가져오기
            planTitle = intent.getStringExtra("SCHEDULE_NAME") ?: "새 일정"
            
            setupUI()
        }
        
        // 완료 버튼 리스너
        confirmButton.setOnClickListener {
            finish() // 현재 화면 종료하고 이전 화면으로 돌아가기
        }
    }
    
    private fun fetchPlanDetail(planId: Int) {
        // 로딩 다이얼로그 표시
        val loadingDialog = AlertDialog.Builder(this)
            .setMessage("일정 정보를 불러오는 중...")
            .setCancelable(false)
            .create()
        loadingDialog.show()
        
        // API 호출
        RetrofitClient.getPlanApi(this).getPlanDetail(planId).enqueue(object : Callback<PlanDetailResponse> {
            override fun onResponse(call: Call<PlanDetailResponse>, response: Response<PlanDetailResponse>) {
                loadingDialog.dismiss()
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val planDetailData = response.body()?.data
                    if (planDetailData != null) {
                        // 일정 정보 설정
                        planTitle = planDetailData.title
                        
                        // 날짜 파싱
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        try {
                            val startDate = dateFormat.parse(planDetailData.startDate)
                            val endDate = dateFormat.parse(planDetailData.endDate)
                            
                            if (startDate != null && endDate != null) {
                                startDateCalendar.time = startDate
                                endDateCalendar.time = endDate
                                
                                // 총 일수 계산
                                val diffInMillis = endDateCalendar.timeInMillis - startDateCalendar.timeInMillis
                                dayCount = TimeUnit.MILLISECONDS.toDays(diffInMillis).toInt() + 1
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "날짜 파싱 오류: ${e.message}")
                        }
                        
                        // 장소 정보 처리
                        processPlaceData(planDetailData.places)
                        
                        // UI 설정
                        setupUI()
                    } else {
                        Toast.makeText(this@ScheduleDetailActivity, "일정 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@ScheduleDetailActivity, "일정 정보를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            
            override fun onFailure(call: Call<PlanDetailResponse>, t: Throwable) {
                loadingDialog.dismiss()
                Toast.makeText(this@ScheduleDetailActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    
    private fun processPlaceData(places: List<PlaceDetailData>) {
        // 기존 데이터 초기화
        dayPlaces.clear()
        
        // 장소 데이터를 일자별로 분류
        for (place in places) {
            val dayList = dayPlaces.getOrPut(place.day) { mutableListOf() }
            dayList.add(
                PlaceItem(
                    id = place.id,
                    name = place.name, 
                    address = place.address, 
                    imageUrl = null, // API에서 이미지 URL은 현재 제공하지 않음
                    day = place.day,
                    order = place.order,
                    lat = place.lat,
                    lng = place.lon,
                    memo = place.memo,
                    cost = place.cost,
                    googleId = place.googleId
                )
            )
        }
        
        // 각 일자별로 순서에 따라 정렬
        for ((_, placeList) in dayPlaces) {
            placeList.sortBy { it.order }
        }
    }
    
    private fun setupUI() {
        // 액션바 타이틀 설정
        supportActionBar?.title = planTitle
        
        // ViewPager 설정
        viewPager.adapter = DayPagerAdapter(this, dayCount, startDateCalendar)
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateTabSelection(position)
            }
        })
        
        // 커스텀 탭 설정
        setupCustomTabs()
    }
    
    // 결과 처리를 위한 onActivityResult 추가
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_PLACE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            val placeName = data.getStringExtra("placeName") ?: return
            val placeAddress = data.getStringExtra("placeAddress") ?: return
            val placeLat = data.getDoubleExtra("placeLat", 0.0)
            val placeLng = data.getDoubleExtra("placeLng", 0.0)
            val placeImageUrl = data.getStringExtra("placeImageUrl")
            val day = data.getIntExtra("day", viewPager.currentItem + 1)
            val googleId = data.getStringExtra("placeId")
            
            // 장소 추가 (UI 갱신)
            addPlaceToUI(placeName, placeAddress, placeImageUrl, day, googleId, placeLat.toString(), placeLng.toString())
            
            // TODO: API 호출하여 서버에 장소 추가
            Toast.makeText(this, "장소가 추가되었습니다: $placeName", Toast.LENGTH_SHORT).show()
        }
    }
    
    // 장소 UI에 추가
    private fun addPlaceToUI(name: String, address: String, imageUrl: String?, day: Int, googleId: String?, lat: String, lng: String) {
        // 해당 일자의 장소 목록 가져오기
        val placesForDay = dayPlaces.getOrPut(day) { mutableListOf() }
        
        // 임시 장소 객체 생성
        val newPlace = PlaceItem(
            id = -1,  // 임시 ID (서버에서 할당받기 전)
            name = name,
            address = address,
            imageUrl = imageUrl,
            day = day,
            order = placesForDay.size + 1,
            lat = lat,
            lng = lng,
            memo = null,
            cost = null,
            googleId = googleId
        )
        
        // 일자별 장소 목록에 추가
        placesForDay.add(newPlace)
        
        // 현재 페이지가 해당 일자의 페이지인 경우 리스트 갱신
        if (viewPager.currentItem == day - 1) {
            viewPager.adapter?.notifyDataSetChanged()
        }
    }
    
    // 장소 목록 어댑터
    inner class PlaceAdapter(private val places: MutableList<PlaceItem>) : 
        RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder>() {
        
        inner class PlaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val numberCircle: TextView = itemView.findViewById(R.id.numberCircle)
            val placeImage: ImageView = itemView.findViewById(R.id.placeImage)
            val placeName: TextView = itemView.findViewById(R.id.placeName)
            val placeAddress: TextView = itemView.findViewById(R.id.placeAddress)
            val editButton: TextView = itemView.findViewById(R.id.editButton)
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_place, parent, false)
            return PlaceViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
            val place = places[position]
            holder.numberCircle.text = (position + 1).toString()
            
            // 이미지 처리
            holder.placeImage.setImageResource(R.drawable.ic_launcher_background) // 기본 이미지
            
            // TODO: 이미지 URL이 있으면 Glide 등으로 로드
            
            holder.placeName.text = place.name
            holder.placeAddress.text = place.address
            
            holder.editButton.setOnClickListener {
                // TODO: 편집 기능 구현
                Toast.makeText(this@ScheduleDetailActivity, "${place.name} 편집", Toast.LENGTH_SHORT).show()
            }
            
            // 아이템 전체 클릭 시 AddPlaceActivity로 이동
            holder.itemView.setOnClickListener {
                val intent = Intent(this@ScheduleDetailActivity, AddPlaceActivity::class.java)
                intent.putExtra("PLAN_ID", planId)
                intent.putExtra("day", place.day)
                startActivityForResult(intent, ADD_PLACE_REQUEST_CODE)
            }
        }

        fun moveItem(fromPosition: Int, toPosition: Int) {
            if (fromPosition < toPosition) {
                for (i in fromPosition until toPosition) {
                    places.swap(i, i + 1)
                }
            } else {
                for (i in fromPosition downTo toPosition + 1) {
                    places.swap(i, i - 1)
                }
            }
            notifyItemMoved(fromPosition, toPosition)
            
            // 순서 필드 업데이트
            for (i in places.indices) {
                places[i] = places[i].copy(order = i + 1)
            }
        }

        private fun MutableList<PlaceItem>.swap(index1: Int, index2: Int) {
            val tmp = this[index1]
            this[index1] = this[index2]
            this[index2] = tmp
        }
        
        override fun getItemCount() = places.size
    }
    
    // Day별 페이지 어댑터
    inner class DayPagerAdapter(private val activity: AppCompatActivity, 
                               private val dayCount: Int, 
                               private val startDate: Calendar) : 
        RecyclerView.Adapter<DayPagerAdapter.DayPageHolder>() {
        
        inner class DayPageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val recyclerView: RecyclerView = itemView.findViewById(R.id.dayRecyclerView)
            val emptyStateLayout: LinearLayout = itemView.findViewById(R.id.emptyStateLayout)
            val plusButton: ImageView = itemView.findViewById(R.id.plusButton)
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayPageHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.day_page_layout, parent, false)
            return DayPageHolder(view)
        }
        
        override fun onBindViewHolder(holder: DayPageHolder, position: Int) {
            val places = dayPlaces[position + 1] ?: listOf()
            
            holder.recyclerView.layoutManager = LinearLayoutManager(activity)
            holder.recyclerView.adapter = PlaceAdapter(places.toMutableList())
            
            // 빈 상태 처리
            if (places.isEmpty()) {
                holder.emptyStateLayout.visibility = View.VISIBLE
                holder.recyclerView.visibility = View.GONE
                
                // + 버튼 클릭 리스너 설정
                holder.plusButton.setOnClickListener {
                    onAddPlaceButtonClicked(position + 1)
                }
            } else {
                holder.emptyStateLayout.visibility = View.GONE
                holder.recyclerView.visibility = View.VISIBLE
            }
            
            setupItemTouchHelper(holder.recyclerView)
        }
        
        override fun getItemCount(): Int {
            return dayCount
        }
    }
    
    // 장소 아이템 데이터 클래스
    data class PlaceItem(
        val id: Int,
        val name: String, 
        val address: String, 
        val imageUrl: String? = null,
        val day: Int,
        val order: Int,
        val lat: String,
        val lng: String,
        val memo: String? = null,
        val cost: Int? = null,
        val googleId: String? = null
    )
    
    // 장소 추가 버튼 클릭 처리
    private fun onAddPlaceButtonClicked(day: Int) {
        try {
            val intent = Intent(this@ScheduleDetailActivity, AddPlaceActivity::class.java)
            intent.putExtra("PLAN_ID", planId)
            intent.putExtra("day", day)
            
            // 선택된 날짜 계산 (시작일 + day - 1)
            val selectedDate = Calendar.getInstance()
            selectedDate.timeInMillis = startDateCalendar.timeInMillis
            selectedDate.add(Calendar.DAY_OF_MONTH, day - 1)
            val selectedDateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate.time)
            intent.putExtra("selected_date", selectedDateString)
            
            startActivityForResult(intent, ADD_PLACE_REQUEST_CODE)
        } catch (e: Exception) {
            Toast.makeText(this, "오류 발생: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }
    
    // 드래그 앤 드롭 헬퍼 설정
    private fun setupItemTouchHelper(recyclerView: RecyclerView) {
        val itemTouchHelper = ItemTouchHelper(PlaceItemTouchHelperCallback(viewPager.currentItem + 1))
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }
    
    // 드래그 앤 드롭 콜백 클래스
    inner class PlaceItemTouchHelperCallback(private val day: Int) : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN,  // 드래그 방향
        0  // 스와이프 사용하지 않음
    ) {
        private var dragFrom = -1
        private var dragTo = -1

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val adapter = recyclerView.adapter as PlaceAdapter
            val fromPosition = viewHolder.adapterPosition
            val toPosition = target.adapterPosition

            if (dragFrom == -1) {
                dragFrom = fromPosition
            }
            dragTo = toPosition

            adapter.moveItem(fromPosition, toPosition)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            // 스와이프 기능 사용하지 않음
        }

        override fun isLongPressDragEnabled(): Boolean {
            return true  // 롱 프레스로 드래그 활성화
        }

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            super.clearView(recyclerView, viewHolder)
            
            if (dragFrom != -1 && dragTo != -1 && dragFrom != dragTo) {
                // 드래그가 끝났을 때 전체 리스트 갱신
                recyclerView.adapter?.notifyDataSetChanged()
                
                // TODO: 서버에 순서 변경을 알리는 API 호출
            }
            
            // 드래그 위치 초기화
            dragFrom = -1
            dragTo = -1
        }
    }
    
    // 커스텀 탭 설정
    private fun setupCustomTabs() {
        val tabContainer = findViewById<LinearLayout>(R.id.tabContainer)
        tabContainer.removeAllViews()
        
        for (i in 0 until dayCount) {
            val tabView = LayoutInflater.from(this).inflate(R.layout.tab_day_item, tabContainer, false)
            val dayNumber = tabView.findViewById<TextView>(R.id.dayNumber)
            val dayDate = tabView.findViewById<TextView>(R.id.dayDate)
            
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = startDateCalendar.timeInMillis
            calendar.add(Calendar.DAY_OF_MONTH, i)
            
            // 한국어 날짜 형식으로 표시
            val dayFormat = SimpleDateFormat("dd", Locale.KOREAN)
            val monthFormat = SimpleDateFormat("MM", Locale.KOREAN)
            val yearFormat = SimpleDateFormat("yyyy", Locale.KOREAN)
            
            dayNumber.text = "DAY ${i + 1}"
            dayDate.text = "${dayFormat.format(calendar.time)}일 ${monthFormat.format(calendar.time)}월"
            
            // 첫 번째 탭을 선택된 상태로 표시
            if (i == 0) {
                dayNumber.setTextColor(ContextCompat.getColor(this, R.color.blue_primary))
                dayDate.setTextColor(ContextCompat.getColor(this, R.color.blue_primary))
                tabView.setBackgroundResource(R.drawable.selected_tab_background)
            }
            
            tabView.tag = i
            tabView.setOnClickListener { v ->
                // 모든 탭 초기화
                for (j in 0 until tabContainer.childCount) {
                    val tab = tabContainer.getChildAt(j)
                    tab.findViewById<TextView>(R.id.dayNumber).setTextColor(ContextCompat.getColor(this, R.color.black))
                    tab.findViewById<TextView>(R.id.dayDate).setTextColor(ContextCompat.getColor(this, R.color.gray_text))
                    tab.setBackgroundResource(0)
                }
                
                // 선택된 탭 하이라이트
                v.findViewById<TextView>(R.id.dayNumber).setTextColor(ContextCompat.getColor(this, R.color.blue_primary))
                v.findViewById<TextView>(R.id.dayDate).setTextColor(ContextCompat.getColor(this, R.color.blue_primary))
                v.setBackgroundResource(R.drawable.selected_tab_background)
                
                // ViewPager 페이지 변경
                viewPager.currentItem = v.tag as Int
            }
            
            tabContainer.addView(tabView)
        }
    }
    
    // 페이지 변경 시 탭 선택 상태 업데이트
    private fun updateTabSelection(position: Int) {
        val tabContainer = findViewById<LinearLayout>(R.id.tabContainer)
        
        // 모든 탭 초기화
        for (i in 0 until tabContainer.childCount) {
            val tab = tabContainer.getChildAt(i)
            tab.findViewById<TextView>(R.id.dayNumber).setTextColor(ContextCompat.getColor(this, R.color.black))
            tab.findViewById<TextView>(R.id.dayDate).setTextColor(ContextCompat.getColor(this, R.color.gray_text))
            tab.setBackgroundResource(0)
        }
        
        // 선택된 탭 하이라이트
        val selectedTab = tabContainer.getChildAt(position)
        selectedTab.findViewById<TextView>(R.id.dayNumber).setTextColor(ContextCompat.getColor(this, R.color.blue_primary))
        selectedTab.findViewById<TextView>(R.id.dayDate).setTextColor(ContextCompat.getColor(this, R.color.blue_primary))
        selectedTab.setBackgroundResource(R.drawable.selected_tab_background)
        
        // 선택된 탭이 보이도록 스크롤
        val scrollView = findViewById<HorizontalScrollView>(R.id.tabScrollView)
        scrollView.smoothScrollTo(selectedTab.left - 50, 0)
    }
}