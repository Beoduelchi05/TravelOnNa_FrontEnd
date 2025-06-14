package com.example.travelonna.api

import com.google.gson.annotations.SerializedName

data class PlanDetailResponse(
    val success: Boolean,
    val message: String,
    val data: PlanDetail?
)

data class PlanDetail(
    val planId: Int,
    val userId: Int,
    val title: String,
    val location: String,
    val startDate: String,
    val endDate: String,
    val transportInfo: String,
    val isPublic: Boolean,
    val totalCost: Int,
    val memo: String,
    val createdAt: String?,
    val updatedAt: String?,
    val places: List<PlaceDetail>,
    @SerializedName("is_group") val isGroup: Boolean,
    @SerializedName("isGroup") val isGroup2: Boolean,
    @SerializedName("group_id") val groupId: Int?,
    @SerializedName("groupId") val groupId2: Int?
) 