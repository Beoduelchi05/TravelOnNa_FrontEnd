package com.example.travelonna.api

import com.google.gson.annotations.SerializedName

// 일정별 여행기록 목록 조회 응답 모델
data class LogsByPlanResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: List<PlanLogData>
)

// 일정별 여행기록 데이터 모델
data class PlanLogData(
    @SerializedName("logId")
    val logId: Int,
    
    @SerializedName("userId")
    val userId: Int,
    
    @SerializedName("userName")
    val userName: String,
    
    @SerializedName("userProfileImage")
    val userProfileImage: String?,
    
    @SerializedName("comment")
    val comment: String,
    
    @SerializedName("createdAt")
    val createdAt: String,
    
    @SerializedName("isPublic")
    val isPublic: Boolean,
    
    @SerializedName("imageUrls")
    val imageUrls: List<String>,
    
    @SerializedName("likeCount")
    val likeCount: Int,
    
    @SerializedName("commentCount")
    val commentCount: Int,
    
    @SerializedName("isLiked")
    val isLiked: Boolean,
    
    @SerializedName("plan")
    val plan: PlanInfo,
    
    @SerializedName("placeId")
    val placeId: Int,
    
    @SerializedName("placeName")
    val placeName: String,
    
    @SerializedName("placeIds")
    val placeIds: List<Int>?,
    
    @SerializedName("placeNames")
    val placeNames: List<String>
)

// 계획 정보 모델
data class PlanInfo(
    @SerializedName("planId")
    val planId: Int,
    
    @SerializedName("startDate")
    val startDate: String,
    
    @SerializedName("endDate")
    val endDate: String,
    
    @SerializedName("transportInfo")
    val transportInfo: String,
    
    @SerializedName("location")
    val location: String,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("isPublic")
    val isPublic: Boolean,
    
    @SerializedName("totalCost")
    val totalCost: Int
) 