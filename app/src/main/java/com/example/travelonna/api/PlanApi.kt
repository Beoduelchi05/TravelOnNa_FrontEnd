package com.example.travelonna.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import com.google.gson.annotations.SerializedName

interface PlanApi {
    @POST("api/v1/plans")
    fun createPlan(@Body request: PlanRequest): Call<PlanResponse>
    
    @GET("api/v1/plans/{planId}/detail")
    fun getPlanDetail(@Path("planId") planId: Int): Call<PlanDetailResponse>
}

data class PlanRequest(
    @SerializedName("title") val title: String,
    @SerializedName("startDate") val startDate: String,
    @SerializedName("endDate") val endDate: String,
    @SerializedName("location") val location: String,
    @SerializedName("transportInfo") val transportInfo: String,
    @SerializedName("isPublic") val isPublic: Boolean = true,
    @SerializedName("totalCost") val totalCost: Int? = null,
    @SerializedName("memo") val memo: String? = null
)

data class PlanResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: PlanData?
)

data class PlanData(
    @SerializedName("planId") val planId: Int,
    @SerializedName("userId") val userId: Int,
    @SerializedName("title") val title: String,
    @SerializedName("startDate") val startDate: String,
    @SerializedName("endDate") val endDate: String,
    @SerializedName("location") val location: String,
    @SerializedName("transportInfo") val transportInfo: String,
    @SerializedName("groupId") val groupId: Int?,
    @SerializedName("isPublic") val isPublic: Boolean,
    @SerializedName("totalCost") val totalCost: Int?,
    @SerializedName("memo") val memo: String?,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String
)

// 일정 상세 조회 응답
data class PlanDetailResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: PlanDetailData?
)

data class PlanDetailData(
    @SerializedName("planId") val planId: Int,
    @SerializedName("userId") val userId: Int,
    @SerializedName("title") val title: String,
    @SerializedName("location") val location: String,
    @SerializedName("startDate") val startDate: String,
    @SerializedName("endDate") val endDate: String,
    @SerializedName("transportInfo") val transportInfo: String,
    @SerializedName("isPublic") val isPublic: Boolean,
    @SerializedName("totalCost") val totalCost: Int?,
    @SerializedName("memo") val memo: String?,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String,
    @SerializedName("places") val places: List<PlaceDetailData>
)

data class PlaceDetailData(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("address") val address: String,
    @SerializedName("order") val order: Int,
    @SerializedName("isPublic") val isPublic: Boolean,
    @SerializedName("visitDate") val visitDate: String,
    @SerializedName("day") val day: Int,
    @SerializedName("cost") val cost: Int?,
    @SerializedName("memo") val memo: String?,
    @SerializedName("lat") val lat: String,
    @SerializedName("lon") val lon: String,
    @SerializedName("googleId") val googleId: String?
) 