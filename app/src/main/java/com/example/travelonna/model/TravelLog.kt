package com.example.travelonna.model

// 여행 로그 데이터 클래스
data class TravelLog(
    val title: String,
    val date: String,
    val type: String,
    val places: List<TravelPlace> = listOf() // 장소 목록 추가
) 