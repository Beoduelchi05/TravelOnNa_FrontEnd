package com.example.travelonna.api

import com.google.gson.annotations.SerializedName

// 추천 요청 모델
data class RecommendationRequest(
    @SerializedName("user_id")
    val userId: Int,
    
    @SerializedName("rec_type")
    val recType: String,
    
    @SerializedName("rec_limit")
    val recLimit: Int = 20
)

// 추천 API 응답 래퍼 (성공/실패 모두 포함)
data class RecommendationApiResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: RecommendationResponse?
)

// 추천 개수 조회 응답 모델
data class RecommendationCountResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: Int
)

// 추천 데이터 존재 여부 확인 응답 모델
data class RecommendationExistsResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: Boolean
)

// 추천 응답 모델
data class RecommendationResponse(
    @SerializedName("userId")
    val userId: Int,
    
    @SerializedName("itemType")
    val itemType: String,
    
    @SerializedName("recommendations")
    val recommendations: List<RecommendationItem>
)

// 추천 아이템 모델
data class RecommendationItem(
    @SerializedName("itemId")
    val itemId: Int,
    
    @SerializedName("score")
    val score: Double,
    
    @SerializedName("logId")
    val logId: Int,
    
    @SerializedName("userId")
    val userId: Int,
    
    @SerializedName("planId")
    val planId: Int,
    
    @SerializedName("comment")
    val comment: String,
    
    @SerializedName("createdAt")
    val createdAt: String,
    
    @SerializedName("isPublic")
    val isPublic: Boolean
) 