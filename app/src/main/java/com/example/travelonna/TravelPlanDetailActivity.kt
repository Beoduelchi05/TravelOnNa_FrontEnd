package com.example.travelonna

import android.content.Intent
import android.os.Bundle
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
import com.example.travelonna.model.ScheduleItem

class TravelPlanDetailActivity : AppCompatActivity() {
    
    companion object {
        const val EXTRA_POST_ID = "extra_post_id"
    }
    
    private lateinit var backButton: ImageView
    private lateinit var followText: TextView
    private lateinit var followToggle: Switch
    private lateinit var dayOneTab: LinearLayout
    private lateinit var dayTwoTab: LinearLayout
    private lateinit var scheduleRecyclerView: RecyclerView
    private lateinit var scheduleAdapter: ScheduleItemAdapter
    
    // 게시물 ID
    private var postId: Long = -1L
    
    // 일정 데이터
    private val allScheduleItems = mutableListOf<ScheduleItem>()
    private val day1Items = mutableListOf<ScheduleItem>()
    private val day2Items = mutableListOf<ScheduleItem>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_travel_plan_detail)
        
        // Initialize views
        initViews()
        
        // Initialize RecyclerView
        initRecyclerView()
        
        // Set up listeners
        setupListeners()
        
        // Load schedule data
        postId = intent.getLongExtra(EXTRA_POST_ID, -1L)
        if (postId != -1L) {
            loadScheduleData(postId)
        } else {
            // If no post ID, load dummy data for preview
            loadDummyData()
        }
    }
    
    private fun initViews() {
        backButton = findViewById(R.id.backButton)
        followText = findViewById(R.id.followText)
        followToggle = findViewById(R.id.followToggle)
        dayOneTab = findViewById(R.id.dayOneTab)
        dayTwoTab = findViewById(R.id.dayTwoTab)
        scheduleRecyclerView = findViewById(R.id.scheduleRecyclerView)
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
        
        // Follow toggle listener
        followToggle.setOnCheckedChangeListener { _, isChecked ->
            updateFollowStatus(isChecked)
            
            // 토스트 메시지로 팔로우 상태 변경 알림
            val message = if (isChecked) "팔로우 되었습니다." else "팔로우가 취소되었습니다."
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
        
        // Day 탭 클릭 이벤트
        dayOneTab.setOnClickListener {
            // Day 1 탭 활성화 UI 업데이트
            updateDayTabSelection(isFirstDaySelected = true)
            
            // Day 1 일정 데이터 표시
            scheduleAdapter.updateItems(day1Items)
        }
        
        dayTwoTab.setOnClickListener {
            // Day 2 탭 활성화 UI 업데이트
            updateDayTabSelection(isFirstDaySelected = false)
            
            // Day 2 일정 데이터 표시
            scheduleAdapter.updateItems(day2Items)
            
            // 다른 날짜 선택 시 토스트 메시지 표시
            Toast.makeText(this, "DAY 02 일정입니다.", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun updateDayTabSelection(isFirstDaySelected: Boolean) {
        // 첫 번째 탭 선택 상태 변경
        val dayOneText = dayOneTab.findViewById<TextView>(R.id.dayOneText)
        val dayOneIndicator = dayOneTab.findViewById<View>(R.id.dayOneIndicator)
        
        // 두 번째 탭 선택 상태 변경
        val dayTwoText = dayTwoTab.findViewById<TextView>(R.id.dayTwoText)
        val dayTwoIndicator = dayTwoTab.findViewById<View>(R.id.dayTwoIndicator)
        
        if (isFirstDaySelected) {
            // Day 1 탭 활성화
            dayOneText?.setTextColor(getColor(R.color.blue))
            dayOneIndicator?.visibility = View.VISIBLE
            
            // Day 2 탭 비활성화
            dayTwoText?.setTextColor(getColor(R.color.gray_text))
            dayTwoIndicator?.visibility = View.INVISIBLE
        } else {
            // Day 1 탭 비활성화
            dayOneText?.setTextColor(getColor(R.color.gray_text))
            dayOneIndicator?.visibility = View.INVISIBLE
            
            // Day 2 탭 활성화
            dayTwoText?.setTextColor(getColor(R.color.blue))
            dayTwoIndicator?.visibility = View.VISIBLE
        }
    }
    
    private fun loadScheduleData(postId: Long) {
        // In a real app, this would fetch data from an API or database
        // 실제 구현에서는 postId를 사용하여 서버에서 여행 일정 데이터를 가져옴
        // 예: RetrofitClient.apiService.getTravelSchedule(postId)...
        
        // 여기서는 더미 데이터 사용
        loadDummyData()
        
        // 개발자용 메시지 (실제 앱에서는 제거할 것)
        if (BuildConfig.DEBUG) {
            Toast.makeText(this, "포스트 ID: $postId 일정 로드 중", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun loadDummyData() {
        // 팔로우 상태 설정 (초기 상태를 팔로우 중으로 설정)
        followToggle.isChecked = true
        updateFollowStatus(true)
        
        // 첫째 날 더미 데이터
        day1Items.clear()
        day1Items.addAll(listOf(
            ScheduleItem(1, 1, "도쿄역", false, 1),
            ScheduleItem(2, 2, "", true, 1),
            ScheduleItem(3, 3, "긴자 카페 방문", false, 1),
            ScheduleItem(4, 4, "스시 오마카세", false, 1)
        ))
        
        // 둘째 날 더미 데이터
        day2Items.clear()
        day2Items.addAll(listOf(
            ScheduleItem(5, 1, "디즈니랜드", false, 2),
            ScheduleItem(6, 2, "하라주쿠", false, 2),
            ScheduleItem(7, 3, "", true, 2)
        ))
        
        // 전체 데이터 합치기
        allScheduleItems.clear()
        allScheduleItems.addAll(day1Items)
        allScheduleItems.addAll(day2Items)
        
        // 첫 번째 날짜 탭 활성화 및 데이터 표시
        updateDayTabSelection(isFirstDaySelected = true)
        scheduleAdapter.updateItems(day1Items)
    }
    
    private fun updateFollowStatus(isFollowing: Boolean) {
        if (isFollowing) {
            followText.text = "팔로우 중"
            followText.setTextColor(getColor(R.color.blue))
        } else {
            followText.text = "팔로우"
            followText.setTextColor(getColor(R.color.gray_text))
        }
    }
} 