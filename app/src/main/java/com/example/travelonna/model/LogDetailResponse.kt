package com.example.travelonna.model

data class LogDetailResponse(
    val success: Boolean,
    val message: String,
    val data: LogDetail
)

data class LogDetail(
    val logId: Int,
    val userId: Int,
    val userName: String,
    val userProfileImage: String?,
    val comment: String,
    val createdAt: String,
    val isPublic: Boolean,
    val imageUrls: List<String>,
    val likeCount: Int,
    val commentCount: Int,
    val isLiked: Boolean,
    val plan: PlanInfo,
    val placeNames: List<String>
)

data class PlanInfo(
    val planId: Int,
    val startDate: String,
    val endDate: String,
    val transportInfo: String,
    val location: String,
    val title: String,
    val isPublic: Boolean,
    val totalCost: Int
) 