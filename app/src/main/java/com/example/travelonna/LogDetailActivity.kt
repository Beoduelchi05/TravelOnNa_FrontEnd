package com.example.travelonna

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.travelonna.api.RetrofitClient
import com.example.travelonna.model.LogDetail
import com.example.travelonna.model.LogDetailResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LogDetailActivity : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var userProfileImage: ImageView
    private lateinit var userNameTextView: TextView
    private lateinit var createdAtTextView: TextView
    private lateinit var commentTextView: TextView
    private lateinit var logImageView: ImageView
    private lateinit var likeCountTextView: TextView
    private lateinit var commentCountTextView: TextView
    private lateinit var planTitleTextView: TextView
    private lateinit var planDateTextView: TextView
    private lateinit var placeNamesTextView: TextView
    private lateinit var progressBar: View
    private lateinit var accessDeniedView: View
    private lateinit var notFoundView: View
    private lateinit var contentScrollView: ScrollView
    
    private var logId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_detail)

        // UI 요소 초기화
        backButton = findViewById(R.id.backButton)
        userProfileImage = findViewById(R.id.userProfileImage)
        userNameTextView = findViewById(R.id.userNameTextView)
        createdAtTextView = findViewById(R.id.createdAtTextView)
        commentTextView = findViewById(R.id.commentTextView)
        logImageView = findViewById(R.id.logImageView)
        likeCountTextView = findViewById(R.id.likeCountTextView)
        commentCountTextView = findViewById(R.id.commentCountTextView)
        planTitleTextView = findViewById(R.id.planTitleTextView)
        planDateTextView = findViewById(R.id.planDateTextView)
        placeNamesTextView = findViewById(R.id.placeNamesTextView)
        progressBar = findViewById(R.id.progressBar)
        accessDeniedView = findViewById(R.id.accessDeniedLayout)
        notFoundView = findViewById(R.id.notFoundLayout)
        contentScrollView = findViewById(R.id.contentScrollView)

        // 접근 제한 뷰 초기 상태 설정
        accessDeniedView.visibility = View.GONE
        notFoundView.visibility = View.GONE

        // Intent에서 logId 가져오기
        logId = intent.getIntExtra("LOG_ID", 0)
        
        if (logId == 0) {
            Toast.makeText(this, "잘못된 접근입니다", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 뒤로가기 버튼 클릭 이벤트
        backButton.setOnClickListener {
            finish()
        }

        // 여행 기록 상세 정보 가져오기
        fetchLogDetail(logId)
    }

    private fun fetchLogDetail(logId: Int) {
        progressBar.visibility = View.VISIBLE
        
        RetrofitClient.apiService.getLogDetail(logId).enqueue(object : Callback<LogDetailResponse> {
            override fun onResponse(call: Call<LogDetailResponse>, response: Response<LogDetailResponse>) {
                progressBar.visibility = View.GONE
                
                when (response.code()) {
                    200 -> {
                        // 성공적으로 데이터를 받은 경우
                        val logDetailResponse = response.body()
                        
                        if (logDetailResponse?.success == true) {
                            displayLogDetail(logDetailResponse.data)
                        } else {
                            Toast.makeText(this@LogDetailActivity, 
                                logDetailResponse?.message ?: "불러오기 실패", 
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                    403 -> {
                        // 접근 권한이 없는 경우
                        handleAccessDenied()
                    }
                    404 -> {
                        // 기록을 찾을 수 없는 경우
                        handleNotFound()
                    }
                    else -> {
                        // 다른 오류 응답 처리
                        Toast.makeText(this@LogDetailActivity, 
                            "서버 오류: ${response.code()}", 
                            Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<LogDetailResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                Log.e("LogDetailActivity", "API 호출 실패", t)
                Toast.makeText(this@LogDetailActivity, 
                    "네트워크 오류: ${t.message}", 
                    Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun handleAccessDenied() {
        // 권한 접근 거부 UI 표시
        accessDeniedView.visibility = View.VISIBLE
        notFoundView.visibility = View.GONE
        
        // 다른 콘텐츠 숨기기 로직 추가
        contentScrollView.visibility = View.GONE
        
        Log.w("LogDetailActivity", "접근 권한이 없는 기록입니다: logId=$logId")
    }

    private fun handleNotFound() {
        // 기록 찾을 수 없음 UI 표시
        notFoundView.visibility = View.VISIBLE
        accessDeniedView.visibility = View.GONE
        
        // 다른 콘텐츠 숨기기 로직 추가
        contentScrollView.visibility = View.GONE
        
        Log.w("LogDetailActivity", "기록을 찾을 수 없습니다: logId=$logId")
    }

    private fun displayLogDetail(logDetail: LogDetail) {
        // 콘텐츠 ScrollView 표시
        contentScrollView.visibility = View.VISIBLE
        accessDeniedView.visibility = View.GONE
        notFoundView.visibility = View.GONE
        
        // 사용자 정보 표시
        userNameTextView.text = logDetail.userName
        
        // 프로필 이미지 로드
        if (!logDetail.userProfileImage.isNullOrEmpty()) {
            Glide.with(this)
                .load(logDetail.userProfileImage)
                .circleCrop()
                .into(userProfileImage)
        }
        
        // 생성 날짜 표시
        createdAtTextView.text = logDetail.createdAt ?: "날짜 정보 없음"
        
        // 코멘트 표시
        commentTextView.text = logDetail.comment
        
        // 이미지 표시 (첫 번째 이미지만)
        if (logDetail.imageUrls.isNotEmpty()) {
            logImageView.visibility = View.VISIBLE
            Glide.with(this)
                .load(logDetail.imageUrls[0])
                .centerCrop()
                .into(logImageView)
        } else {
            logImageView.visibility = View.GONE
        }
        
        // 좋아요, 댓글 수 표시
        likeCountTextView.text = "좋아요 ${logDetail.likeCount}개"
        commentCountTextView.text = "댓글 ${logDetail.commentCount}개"
        
        // 여행 계획 정보 표시
        planTitleTextView.text = logDetail.plan.title
        planDateTextView.text = "${logDetail.plan.startDate} - ${logDetail.plan.endDate}"
        
        // 장소 이름 표시
        val placeNamesText = if (logDetail.placeNames.isNotEmpty()) {
            logDetail.placeNames.joinToString(", ")
        } else {
            "방문 장소 없음"
        }
        placeNamesTextView.text = placeNamesText
    }
} 