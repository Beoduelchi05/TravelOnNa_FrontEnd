package com.example.travelonna.api

import com.example.travelonna.model.LogDetailResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.DELETE
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Query
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.HeaderMap
import okhttp3.RequestBody
import okhttp3.MultipartBody

interface ApiService {
    @GET("api/v1/plans/{planId}/detail")
    fun getPlanDetail(@Path("planId") planId: Int): Call<PlanDetailResponse>
    
    @POST("api/v1/plans/{planId}/places")
    fun createPlace(@Path("planId") planId: Int, @Body request: PlaceCreateRequest): Call<PlaceCreateResponse>
    
    @PUT("api/v1/plans/{planId}/places/{placeId}")
    fun updatePlace(@Path("planId") planId: Int, @Path("placeId") placeId: Int, @Body request: PlaceCreateRequest): Call<BasicResponse>
    
    @POST("api/v1/plans")
    fun createPlan(@Body request: PlanCreateRequest): Call<PlanCreateResponse>
    
    @POST("api/v1/plans/transportation/search")
    fun searchTransportation(@Body request: TransportationRequest): Call<TransportationResponse>
    
    @POST("api/v1/groups")
    fun createGroupUrl(@Body request: GroupUrlRequest): Call<GroupUrlResponse>

    @GET("api/v1/plans")
    fun getPlans(): Call<PlanListResponse>
    
    @GET("api/v1/logs/{logId}")
    fun getLogDetail(@Path("logId") logId: Int): Call<LogDetailResponse>
    
    @GET("api/v1/groups/my")
    fun getMyGroups(): Call<List<GroupInfoResponse>>
    
    @GET("api/v1/groups/plan/{planId}")
    fun getGroupInfo(@Path("planId") planId: Int): Call<GroupInfoResponse>
    
    @POST("api/v1/groups/join/{url}")
    fun joinGroup(@Path("url") urlCode: String): Call<Void>
    
    @GET("api/v1/stations/search")
    fun searchStations(@Query("keyword") keyword: String): Call<StationSearchResponse>
    
    @GET("api/v1/stations/region")
    fun searchStationsByRegion(@Query("region") region: String): Call<StationSearchResponse>
    
    @DELETE("api/v1/plans/{planId}/places/{placeId}")
    fun deletePlace(@Path("planId") planId: Int, @Path("placeId") placeId: Int): Call<BasicResponse>
    
    @DELETE("api/v1/plans/{planId}")
    fun deletePlan(@Path("planId") planId: Int): Call<BasicResponse>
    
    // User profile endpoints
    @GET("api/v1/profiles/user/{userId}")
    fun getUserProfile(@Path("userId") userId: Int): Call<ProfileResponse>
    
    @POST("api/v1/profiles")
    fun createUserProfile(@Body request: ProfileCreateRequest): Call<ProfileResponse>
    
    // Multipart 요청을 위한 프로필 생성 API
    @Multipart
    @POST("api/v1/profiles")
    fun createUserProfileWithImage(
        @Part("userId") userId: RequestBody,
        @Part("nickname") nickname: RequestBody,
        @Part("introduction") introduction: RequestBody,
        @Part profileImage: MultipartBody.Part?
    ): Call<ProfileResponse>
    
    // Follow count endpoints
    @GET("api/v1/follows/count/followers/{profileId}")
    fun getFollowersCount(@Path("profileId") profileId: Int): Call<FollowCountResponse>
    
    @GET("api/v1/follows/count/followings/{profileId}")
    fun getFollowingsCount(@Path("profileId") profileId: Int): Call<FollowCountResponse>
    
    // Search endpoint
    @GET("api/v1/search")
    fun search(@Query("keyword") keyword: String): Call<SearchResponse>
    
    // Recommendation endpoints
    @POST("api/v1/recommendations")
    fun getRecommendations(@Body request: RecommendationRequest): Call<RecommendationApiResponse>
    
    @GET("api/v1/recommendations/count")
    fun getRecommendationCount(
        @Query("userId") userId: Int,
        @Query("type") type: String = "log"
    ): Call<RecommendationCountResponse>
    
    @GET("api/v1/recommendations/exists")
    fun checkRecommendationExists(
        @Query("userId") userId: Int,
        @Query("type") type: String = "log"
    ): Call<RecommendationExistsResponse>
    
    // User logs endpoints
    @GET("api/v1/logs/users/{userId}")
    fun getUserLogs(@Path("userId") userId: Int): Call<UserLogsResponse>
    
    @GET("api/v1/plans/{planId}/places/view")
    fun getPlanPlaces(@Path("planId") planId: Int): Call<PlanPlacesResponse>
    
    // Like toggle endpoint
    @POST("api/v1/logs/{logId}/likes")
    fun toggleLogLike(@Path("logId") logId: Int): Call<LikeToggleResponse>
    
    // Log detail endpoint 
    @GET("api/v1/logs/{logId}")
    fun getLogDetailFromApi(@Path("logId") logId: Int): Call<LogDetailApiResponse>
    
    // Comments endpoint
    @GET("api/logs/{logId}/comments")
    fun getLogComments(@Path("logId") logId: Int): Call<CommentsResponse>
    
    // Create comment endpoint
    @POST("api/logs/{logId}/comments")
    fun createComment(
        @Path("logId") logId: Int,
        @Body request: CreateCommentRequest
    ): Call<CommentData>
    
    // Update comment endpoint
    @PUT("api/logs/comments/{commentId}")
    fun updateComment(
        @Path("commentId") commentId: Int,
        @Body request: CreateCommentRequest
    ): Call<UpdateCommentResponse>
    
    // Delete comment endpoint
    @DELETE("api/logs/comments/{commentId}")
    fun deleteComment(
        @Path("commentId") commentId: Int
    ): Call<DeleteCommentResponse>
    
    // Follow endpoints
    @POST("api/v1/follows")
    fun followUser(@Body request: FollowRequest): Call<FollowResponse>
    
    @DELETE("api/v1/follows/{toUser}")
    fun unfollowUser(@Path("toUser") toUser: Int): Call<UnfollowResponse>
    
    @GET("api/v1/follows/status/{toUser}")
    fun getFollowStatus(@Path("toUser") toUser: Int): Call<FollowStatusResponse>
    
    // 일정별 여행기록 목록 조회 API 추가
    @GET("api/v1/logs/plans/{planId}")
    fun getLogsByPlan(@Path("planId") planId: Int): Call<LogsByPlanResponse>
} 