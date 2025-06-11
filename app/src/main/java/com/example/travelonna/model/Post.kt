package com.example.travelonna.model

data class Post(
    val id: Long,
    val imageResource: Int,
    val imageUrl: String? = null,  // 이미지 URL
    val hasImage: Boolean = false,  // 이미지 유무
    val userName: String,
    val userId: Int = 0,  // 게시물 작성자 ID
    var isFollowing: Boolean = false,
    val description: String,
    val date: String,
    var isLiked: Boolean = false,
    var likeCount: Int = 0,
    var commentCount: Int = 0,
    val planId: Int = 0  // 여행 계획 ID
) 