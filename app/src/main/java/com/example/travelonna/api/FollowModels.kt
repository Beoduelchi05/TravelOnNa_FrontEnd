package com.example.travelonna.api

import com.google.gson.annotations.SerializedName

// 팔로우 요청 모델
data class FollowRequest(
    @SerializedName("toUser")
    val toUser: Int,
    @SerializedName("fromUser")
    val fromUser: Int
)

// 팔로우 데이터 모델
data class FollowData(
    @SerializedName("id")
    val id: Int,
    @SerializedName("fromUser")
    val fromUser: Int,
    @SerializedName("toUser")
    val toUser: Int,
    @SerializedName("profileId")
    val profileId: Int,
    @SerializedName("following")
    val following: Boolean
)

// 팔로우 응답 모델
data class FollowResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: FollowData? = null
)

// 언팔로우 응답 모델 (data는 빈 객체)
data class UnfollowResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: Any? = null  // 빈 객체 {}
)

// 팔로우 상태 응답 모델
data class FollowStatusResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: Map<String, Boolean>? = null  // 팔로우 상태 정보들
) 