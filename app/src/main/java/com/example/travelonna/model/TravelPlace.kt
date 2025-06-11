package com.example.travelonna.model

// 여행 장소 데이터 클래스
data class TravelPlace(
    val name: String,
    val address: String,
    val visitTime: String,
    val dayVisit: Int = 1, // 몇일차 방문인지 (기본값: 1일차)
    val latitude: Double? = null, // 위도
    val longitude: Double? = null, // 경도
    val description: String? = null // 장소 설명/기록
) 