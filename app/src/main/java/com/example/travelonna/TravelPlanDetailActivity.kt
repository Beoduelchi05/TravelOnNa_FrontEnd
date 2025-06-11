package com.example.travelonna

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.travelonna.adapter.ScheduleItemAdapter
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import com.example.travelonna.api.PlanDetailResponse
import com.example.travelonna.api.PlanDetail
import com.example.travelonna.api.PlaceDetail
import com.example.travelonna.api.LogsByPlanResponse
import com.example.travelonna.api.PlanLogData
import com.example.travelonna.api.PlanInfo
import com.example.travelonna.api.RetrofitClient
import com.example.travelonna.model.ScheduleItem
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TravelPlanDetailActivity : AppCompatActivity() {
    
    companion object {
        const val EXTRA_POST_ID = "extra_post_id"
        const val EXTRA_PLAN_ID = "extra_plan_id"
        private const val TAG = "TravelPlanDetailActivity"
    }
    
    private lateinit var backButton: ImageView
    private lateinit var followText: TextView
    private lateinit var followToggle: Switch
    // 동적으로 생성되는 day 탭들을 관리
    private lateinit var dayTabContainer: LinearLayout
    private val dayTabViews = mutableMapOf<Int, LinearLayout>()
    private lateinit var scheduleRecyclerView: RecyclerView
    private lateinit var scheduleAdapter: ScheduleItemAdapter
    // Plan UI views are not present in the current layout
    // private var planTitleText: TextView? = null
    // private var planLocationText: TextView? = null
    // private var planDateText: TextView? = null
    
    // 게시물 ID 및 계획 ID
    private var postId: Long = -1L
    private var planId: Int = 0
    
    // 일정 데이터 - 동적으로 날짜별 관리
    private val allScheduleItems = mutableListOf<ScheduleItem>()
    private val dayItemsMap = mutableMapOf<Int, MutableList<ScheduleItem>>()
    private var totalDays = 1
    private var currentSelectedDay = 1
    
    // 현재 로드된 계획 정보
    private var currentPlan: PlanDetail? = null
    private var currentPlanInfo: PlanInfo? = null
    private var planLogs: List<PlanLogData> = emptyList()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_travel_plan_detail)
        
        // Initialize views
        initViews()
        
        // 팔로우 관련 UI 숨기기 (TravelPlanDetailActivity에서는 팔로우 기능 비활성화)
        followToggle.visibility = View.GONE
        followText.visibility = View.GONE
        
        // Initialize RecyclerView
        initRecyclerView()
        
        // Set up listeners
        setupListeners()
        
        // Load schedule data
        postId = intent.getLongExtra(EXTRA_POST_ID, -1L)
        planId = intent.getIntExtra(EXTRA_PLAN_ID, 0)
        
        Log.d(TAG, "Received postId: $postId, planId: $planId")
        
        if (planId > 0) {
            loadPlanLogs(planId)
        } else {
            Log.w(TAG, "No valid planId provided, loading dummy data")
            showError("여행 계획 정보가 없습니다.")
            loadDummyData()
        }
    }
    
    private fun initViews() {
        backButton = findViewById(R.id.backButton)
        followText = findViewById(R.id.followText)
        followToggle = findViewById(R.id.followToggle)
        dayTabContainer = findViewById(R.id.dayTabContainer)
        scheduleRecyclerView = findViewById(R.id.scheduleRecyclerView)
        
        // 계획 정보 표시용 뷰들은 현재 레이아웃에 없음
        // TODO: 레이아웃에 계획 정보 표시 뷰들이 추가되면 활성화
        /*
        planTitleText = try {
            findViewById(R.id.planTitleText)
        } catch (e: Exception) {
            Log.w(TAG, "planTitleText not found in layout: ${e.message}")
            null
        }
        
        planLocationText = try {
            findViewById(R.id.planLocationText)
        } catch (e: Exception) {
            Log.w(TAG, "planLocationText not found in layout: ${e.message}")
            null
        }
        
        planDateText = try {
            findViewById(R.id.planDateText)
        } catch (e: Exception) {
            Log.w(TAG, "planDateText not found in layout: ${e.message}")
            null
        }
        */
    }
    
    private fun initRecyclerView() {
        scheduleAdapter = ScheduleItemAdapter()
        scheduleRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@TravelPlanDetailActivity)
            adapter = scheduleAdapter
        }
    }
    
    private fun setupListeners() {
        // Back button click listener
        backButton.setOnClickListener {
            finish() // 이전 화면으로 돌아가기
        }
        
        // Day 탭 클릭 이벤트는 동적으로 생성될 때 설정됩니다
    }
    
    /**
     * 날짜 탭 UI 업데이트 (동적으로 탭 수 조정)
     */
    private fun updateDayTabsUI() {
        // 기존 탭 제거
        dayTabContainer.removeAllViews()
        dayTabViews.clear()
        
        // totalDays만큼 탭 생성
        for (day in 1..totalDays) {
            val dayTab = createDayTab(day)
            dayTabViews[day] = dayTab
            dayTabContainer.addView(dayTab)
        }
        
        Log.d(TAG, "Updated day tabs UI for $totalDays days")
    }
    
    /**
     * 개별 Day 탭 생성
     */
    private fun createDayTab(day: Int): LinearLayout {
        val dayTab = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                if (day > 1) {
                    marginStart = resources.getDimensionPixelSize(R.dimen.day_tab_margin)
                }
            }
        }
        
        // Day 텍스트
        val dayText = TextView(this).apply {
            id = View.generateViewId()
            text = "DAY ${String.format("%02d", day)}"
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(if (day == 1) getColor(R.color.blue) else getColor(R.color.gray_text))
        }
        
        // 인디케이터
        val indicator = View(this).apply {
            id = View.generateViewId()
            layoutParams = LinearLayout.LayoutParams(
                resources.getDimensionPixelSize(R.dimen.day_indicator_width),
                resources.getDimensionPixelSize(R.dimen.day_indicator_height)
            ).apply {
                topMargin = resources.getDimensionPixelSize(R.dimen.day_indicator_margin_top)
            }
            setBackgroundColor(if (day == 1) getColor(R.color.blue) else android.graphics.Color.TRANSPARENT)
        }
        
        dayTab.addView(dayText)
        dayTab.addView(indicator)
        
        // 클릭 이벤트
        dayTab.setOnClickListener {
            selectDay(day)
        }
        
        // 태그로 day 번호 저장
        dayTab.tag = day
        
        return dayTab
    }
    
    /**
     * 특정 날짜 선택
     */
    private fun selectDay(day: Int) {
        if (day in 1..totalDays && dayItemsMap.containsKey(day)) {
            currentSelectedDay = day
            updateDayTabSelection(day)
            scheduleAdapter.updateItems(dayItemsMap[day] ?: emptyList())
            
            Toast.makeText(this, "DAY ${String.format("%02d", day)} 일정입니다.", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Selected day $day with ${dayItemsMap[day]?.size ?: 0} items")
        } else {
            Log.w(TAG, "Invalid day selection: $day (totalDays: $totalDays)")
        }
    }
    
    /**
     * 날짜 탭 선택 상태 업데이트
     */
    private fun updateDayTabSelection(selectedDay: Int) {
        // 모든 탭을 비활성화 상태로 설정
        dayTabViews.forEach { (day, tabView) ->
            val dayText = tabView.getChildAt(0) as TextView
            val indicator = tabView.getChildAt(1) as View
            
            if (day == selectedDay) {
                // 선택된 탭 활성화
                dayText.setTextColor(getColor(R.color.blue))
                indicator.setBackgroundColor(getColor(R.color.blue))
            } else {
                // 선택되지 않은 탭 비활성화
                dayText.setTextColor(getColor(R.color.gray_text))
                indicator.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            }
        }
    }
    
    private fun loadPlanLogs(planId: Int) {
        Log.d(TAG, "Loading plan logs for planId: $planId")
        
        RetrofitClient.apiService.getLogsByPlan(planId).enqueue(object : Callback<LogsByPlanResponse> {
            override fun onResponse(call: Call<LogsByPlanResponse>, response: Response<LogsByPlanResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val planLogsResponse = response.body()!!
                    
                    if (planLogsResponse.success && planLogsResponse.data.isNotEmpty()) {
                        Log.d(TAG, "Plan logs loaded successfully: ${planLogsResponse.data.size} logs")
                        planLogs = planLogsResponse.data
                        
                        // 첫 번째 로그에서 계획 정보 추출
                        val firstLog = planLogsResponse.data.first()
                        currentPlanInfo = firstLog.plan
                        
                        updatePlanInfoUI(firstLog.plan)
                        convertLogsToScheduleItems(planLogsResponse.data)
                    } else if (planLogsResponse.success && planLogsResponse.data.isEmpty()) {
                        Log.w(TAG, "No logs found for plan: $planId")
                        showError("이 여행 계획에는 아직 기록이 없습니다.")
                        loadDummyData()
                    } else {
                        Log.w(TAG, "Failed to load plan logs: ${planLogsResponse.message}")
                        showError("여행 기록을 불러올 수 없습니다: ${planLogsResponse.message}")
                        loadDummyData()
                    }
                } else {
                    Log.e(TAG, "HTTP error loading plan logs: ${response.code()}")
                    
                    // 서버에서 반환하는 에러 메시지 파싱 시도
                    val errorMessage = try {
                        val errorBody = response.errorBody()?.string()
                        Log.d(TAG, "Error response body: $errorBody")
                        
                        // JSON에서 message 필드 추출 시도
                        val jsonRegex = "\"message\"\\s*:\\s*\"([^\"]+)\"".toRegex()
                        val match = jsonRegex.find(errorBody ?: "")
                        match?.groupValues?.get(1) ?: "알 수 없는 오류가 발생했습니다."
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to parse error message: ${e.message}")
                        "알 수 없는 오류가 발생했습니다."
                    }
                    
                    when (response.code()) {
                        400 -> showError("잘못된 요청: $errorMessage")
                        403 -> showError("이 여행 계획에 접근할 권한이 없습니다.")
                        404 -> showError("여행 계획을 찾을 수 없습니다: $errorMessage")
                        else -> showError("여행 기록을 불러오는 중 오류가 발생했습니다: $errorMessage")
                    }
                    loadDummyData()
                }
            }
            
            override fun onFailure(call: Call<LogsByPlanResponse>, t: Throwable) {
                Log.e(TAG, "Network error loading plan logs", t)
                showError("네트워크 오류가 발생했습니다. 인터넷 연결을 확인해주세요.")
                loadDummyData()
            }
        })
    }
    
    private fun updatePlanInfoUI(planInfo: PlanInfo) {
        // 계획 기본 정보 업데이트 (현재 레이아웃에 해당 뷰들이 없음)
        // TODO: 레이아웃에 계획 정보 표시 뷰들이 추가되면 활성화
        /*
        planTitleText?.text = planInfo.title
        planLocationText?.text = planInfo.location
        planDateText?.text = "${planInfo.startDate} ~ ${planInfo.endDate}"
        */
        
        Log.d(TAG, "Updated plan UI - Title: ${planInfo.title}, Location: ${planInfo.location}")
    }
    
    private fun convertLogsToScheduleItems(logs: List<PlanLogData>) {
        Log.d(TAG, "Converting ${logs.size} logs to schedule items")
        
        if (logs.isEmpty() || currentPlanInfo == null) {
            Log.w(TAG, "No logs or plan info available")
            loadDummyData()
            return
        }
        
        // 기존 데이터 클리어
        allScheduleItems.clear()
        dayItemsMap.clear()
        
        // 계획 기간 계산
        val planInfo = currentPlanInfo!!
        val startDate = parseDate(planInfo.startDate)
        val endDate = parseDate(planInfo.endDate)
        
        if (startDate == null || endDate == null) {
            Log.e(TAG, "Failed to parse plan dates: ${planInfo.startDate} - ${planInfo.endDate}")
            loadDummyData()
            return
        }
        
        totalDays = calculateDaysBetween(startDate, endDate) + 1
        Log.d(TAG, "Plan duration: $totalDays days (${planInfo.startDate} to ${planInfo.endDate})")
        
        // totalDays가 1보다 작으면 최소 1일로 설정
        if (totalDays < 1) {
            totalDays = 1
            Log.w(TAG, "Calculated totalDays was < 1, setting to 1")
        }
        
        // UI는 이제 모든 일수를 지원합니다
        Log.d(TAG, "Plan has $totalDays days, UI will display all days")
        
        // 날짜별 Map 초기화
        for (day in 1..totalDays) {
            dayItemsMap[day] = mutableListOf()
        }
        
        // 모든 로그에서 placeNames 배열을 추출 (여행 순서를 나타냄)
        val allPlaceNames = if (logs.isNotEmpty()) {
            // 첫 번째 로그의 placeNames가 전체 여행 순서를 포함하고 있음
            logs.first().placeNames
        } else {
            emptyList()
        }
        
        Log.d(TAG, "Plan dates: ${planInfo.startDate} to ${planInfo.endDate}, total days: $totalDays")
        Log.d(TAG, "Total places in travel order: ${allPlaceNames.size}")
        Log.d(TAG, "Places: $allPlaceNames")
        
        // 전체 장소를 날짜별로 균등 분배
        if (allPlaceNames.isNotEmpty()) {
            val placesPerDay = allPlaceNames.size / totalDays
            val remainingPlaces = allPlaceNames.size % totalDays
            
            Log.d(TAG, "Places per day: $placesPerDay, remaining: $remainingPlaces")
            
            var currentPlaceIndex = 0
            var itemId = 1L
            
            for (day in 1..totalDays) {
                val placesForThisDay = placesPerDay + if (day <= remainingPlaces) 1 else 0
                
                Log.d(TAG, "Day $day will have $placesForThisDay places")
                
                for (i in 0 until placesForThisDay) {
                    if (currentPlaceIndex < allPlaceNames.size) {
                        val placeName = allPlaceNames[currentPlaceIndex]
                        
                        // 빈 문자열이 아닌 경우만 추가
                        if (placeName.isNotBlank()) {
                            val scheduleItem = ScheduleItem(
                                id = itemId++,
                                number = i + 1,
                                placeName = placeName,
                                isLocked = false,
                                day = day
                            )
                            
                            allScheduleItems.add(scheduleItem)
                            dayItemsMap[day]?.add(scheduleItem)
                        }
                        
                        currentPlaceIndex++
                    }
                }
                
                Log.d(TAG, "Day $day: ${dayItemsMap[day]?.size ?: 0} places assigned")
                dayItemsMap[day]?.forEach { item ->
                    Log.d(TAG, "  - ${item.placeName}")
                }
            }
        } else {
            Log.w(TAG, "No place names found in logs")
        }
        
        // UI 업데이트
        updateDayTabsUI()
        currentSelectedDay = 1
        updateDayTabSelection(currentSelectedDay)
        scheduleAdapter.updateItems(dayItemsMap[currentSelectedDay] ?: emptyList())
        
        val totalPlaces = allScheduleItems.size
        if (totalPlaces == 0) {
            Toast.makeText(this, "이 여행 계획에는 아직 장소가 없습니다.", Toast.LENGTH_SHORT).show()
        } else {
            Log.d(TAG, "Converted logs to schedule items: Total $totalPlaces places across $totalDays days")
        }
    }
    
    /**
     * 날짜 문자열을 Date 객체로 파싱
     */
    private fun parseDate(dateString: String): Date? {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            format.parse(dateString)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing date: $dateString", e)
            null
        }
    }
    
    /**
     * 두 날짜 사이의 일수 계산
     */
    private fun calculateDaysBetween(startDate: Date, endDate: Date): Int {
        val diffInMillis = endDate.time - startDate.time
        return TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS).toInt()
    }
    
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
    
    private fun loadDummyData() {
        // 더미 데이터용 기본 설정
        totalDays = 2
        currentSelectedDay = 1
        dayItemsMap.clear()
        allScheduleItems.clear()
        
        // Day 1 더미 데이터
        val day1Items = mutableListOf(
            ScheduleItem(1L, 1, "도쿄역", false, 1),
            ScheduleItem(2L, 2, "", false, 1),
            ScheduleItem(3L, 3, "긴자 카페 방문", false, 1),
            ScheduleItem(4L, 4, "스시 오마카세", false, 1)
        )
        
        // Day 2 더미 데이터
        val day2Items = mutableListOf(
            ScheduleItem(5L, 1, "디즈니랜드", false, 2),
            ScheduleItem(6L, 2, "하라주쿠", false, 2),
            ScheduleItem(7L, 3, "", false, 2)
        )
        
        // 날짜별 Map에 데이터 저장
        dayItemsMap[1] = day1Items
        dayItemsMap[2] = day2Items
        
        // 전체 데이터 합치기
        allScheduleItems.addAll(day1Items)
        allScheduleItems.addAll(day2Items)
        
        // UI 업데이트
        updateDayTabsUI()
        updateDayTabSelection(1)
        scheduleAdapter.updateItems(dayItemsMap[1] ?: emptyList())
        
        Log.d(TAG, "Loaded dummy data: $totalDays days")
    }
} 