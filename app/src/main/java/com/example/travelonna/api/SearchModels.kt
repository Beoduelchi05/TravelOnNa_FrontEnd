package com.example.travelonna.api

import com.google.gson.annotations.SerializedName

// 검색 응답 모델
data class SearchResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: SearchData
)

data class SearchData(
    @SerializedName("places")
    val places: List<SearchPlace>,
    
    @SerializedName("users")
    val users: List<SearchUser>,
    
    @SerializedName("logs")
    val logs: List<SearchLog>
)

data class SearchPlace(
    @SerializedName("placeId")
    val placeId: Int,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("address")
    val address: String,
    
    @SerializedName("lat")
    val lat: String,
    
    @SerializedName("lon")
    val lon: String,
    
    @SerializedName("googleId")
    val googleId: String
)

data class SearchUser(
    @SerializedName("userId")
    val userId: Int,
    
    @SerializedName("nickname")
    val nickname: String,
    
    @SerializedName("profileImage")
    val profileImage: String,
    
    @SerializedName("introduction")
    val introduction: String
)

data class SearchLog(
    @SerializedName("logId")
    val logId: Int,
    
    @SerializedName("userId")
    val userId: Int,
    
    @SerializedName("userName")
    val userName: String,
    
    @SerializedName("comment")
    val comment: String,
    
    @SerializedName("createdAt")
    val createdAt: String,
    
    @SerializedName("imageUrls")
    val imageUrls: List<String>,
    
    @SerializedName("likeCount")
    val likeCount: Int
) 