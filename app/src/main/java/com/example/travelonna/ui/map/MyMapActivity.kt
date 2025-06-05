package com.example.travelonna.ui.map

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.travelonna.R
import com.example.travelonna.databinding.ActivityMyMapBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import java.io.IOException
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlinx.coroutines.*

class MyMapActivity : AppCompatActivity(), OnMapReadyCallback {
    
    companion object {
        private const val TAG = "MyMapActivity"
    }
    
    private lateinit var binding: ActivityMyMapBinding
    private lateinit var map: GoogleMap
    private var sigData: JsonObject? = null
    private val polygonInfoList = mutableListOf<String>()
    private val activityScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyMapBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupMap()
        setupFab()
        loadSigData()
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }
    
    private fun setupMap() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }
    
    private fun setupFab() {
        binding.fabLegend.setOnClickListener {
            showLegendDialog()
        }
        
        // 확대 버튼
        binding.fabZoomIn.setOnClickListener {
            if (::map.isInitialized) {
                val currentZoom = map.cameraPosition.zoom
                map.animateCamera(CameraUpdateFactory.zoomTo(currentZoom + 1))
            }
        }
        
        // 축소 버튼
        binding.fabZoomOut.setOnClickListener {
            if (::map.isInitialized) {
                val currentZoom = map.cameraPosition.zoom
                map.animateCamera(CameraUpdateFactory.zoomTo(currentZoom - 1))
            }
        }
    }
    
    private fun showLegendDialog() {
        if (polygonInfoList.isEmpty()) {
            Toast.makeText(this, "표시할 지역 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }
        
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("지역 정보")
        
        val message = polygonInfoList.joinToString("\n\n") { "• $it" }
        dialogBuilder.setMessage(message)
        
        dialogBuilder.setPositiveButton("확인") { dialog, _ ->
            dialog.dismiss()
        }
        
        dialogBuilder.create().show()
    }
    
    private fun loadSigData() {
        activityScope.launch {
            try {
                // 백그라운드에서 파일 읽기
                val jsonString = withContext(Dispatchers.IO) {
                    val inputStream = assets.open("sig.json")
                    val reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
                    reader.use { it.readText() }
                }
                
                Log.d(TAG, "JSON 파일 내용 길이: ${jsonString.length}")
                Log.d(TAG, "JSON 파일 첫 100자: ${jsonString.take(100)}")
                
                // 백그라운드에서 JSON 파싱
                val jsonElement = withContext(Dispatchers.Default) {
                    JsonParser.parseString(jsonString.trim())
                }
                
                // 메인 스레드에서 UI 업데이트
                if (jsonElement.isJsonObject) {
                    sigData = jsonElement.asJsonObject
                    Log.d(TAG, "sig.json 파일 로드 성공")
                    
                    // 지도가 준비되면 폴리곤 그리기
                    if (::map.isInitialized) {
                        drawPolygons()
                    }
                } else {
                    Log.e(TAG, "JSON이 객체가 아닙니다: ${jsonElement.javaClass.simpleName}")
                    Toast.makeText(this@MyMapActivity, "JSON 형식이 올바르지 않습니다.", Toast.LENGTH_SHORT).show()
                }
                
            } catch (e: IOException) {
                Log.e(TAG, "sig.json 파일 로드 실패", e)
                Toast.makeText(this@MyMapActivity, "지도 데이터를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e(TAG, "JSON 파싱 실패", e)
                Toast.makeText(this@MyMapActivity, "지도 데이터 형식이 올바르지 않습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        
        // 기본 Google Maps 스타일 사용 (한글 지원)
        // 맵 스타일 설정은 제거하고 기본 스타일 사용
        
        // 초기 위치를 대한민국으로 설정
        val korea = LatLng(36.0, 128.0)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(korea, 7f))
        
        // sig 데이터가 이미 로드되었다면 폴리곤 그리기
        if (sigData != null) {
            drawPolygons()
        }
    }
    
    private fun drawPolygons() {
        sigData?.let { data ->
            activityScope.launch {
                try {
                    // 백그라운드에서 폴리곤 데이터 처리
                    val polygonData = withContext(Dispatchers.Default) {
                        processPolygonData(data)
                    }
                    
                    // 메인 스레드에서 UI 업데이트 (배치 처리)
                    drawPolygonsOnMap(polygonData)
                    
                } catch (e: Exception) {
                    Log.e(TAG, "폴리곤 그리기 실패", e)
                    Toast.makeText(this@MyMapActivity, "지도 표시 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private data class PolygonData(
        val polygonOptions: PolygonOptions,
        val name: String,
        val description: String,
        val bounds: LatLng
    )
    
    private fun processPolygonData(data: JsonObject): List<PolygonData> {
        val polygonDataList = mutableListOf<PolygonData>()
        
        when {
            data.has("features") -> {
                // FeatureCollection 형태
                val features = data.getAsJsonArray("features")
                processFeatures(features, polygonDataList)
            }
            data.has("geometry") -> {
                // 단일 Feature 형태
                processSingleFeature(data, polygonDataList)
            }
            data.has("coordinates") -> {
                // 직접 좌표 배열 형태
                processDirectCoordinates(data, polygonDataList)
            }
        }
        
        return polygonDataList
    }
    
    private fun processFeatures(features: JsonArray, polygonDataList: MutableList<PolygonData>) {
        for (i in 0 until features.size()) {
            val feature = features[i].asJsonObject
            val geometry = feature.getAsJsonObject("geometry")
            val type = geometry.get("type").asString
            
            // 속성 정보 추출
            val properties = feature.getAsJsonObject("properties")
            val name = properties?.get("name")?.asString ?: "이름 없음"
            val description = properties?.get("description")?.asString ?: ""
            
            when (type) {
                "Polygon" -> {
                    val coordinates = geometry.getAsJsonArray("coordinates")
                    val polygonOptions = createPolygonOptions()
                    var centerLat = 0.0
                    var centerLng = 0.0
                    var pointCount = 0
                    
                    // 외부 링 (첫 번째 배열)
                    val outerRing = coordinates[0].asJsonArray
                    for (j in 0 until outerRing.size()) {
                        val coord = outerRing[j].asJsonArray
                        val lng = coord[0].asDouble
                        val lat = coord[1].asDouble
                        val latLng = LatLng(lat, lng)
                        polygonOptions.add(latLng)
                        
                        centerLat += lat
                        centerLng += lng
                        pointCount++
                    }
                    
                    val center = LatLng(centerLat / pointCount, centerLng / pointCount)
                    polygonDataList.add(PolygonData(polygonOptions, name, description, center))
                }
                "MultiPolygon" -> {
                    val coordinates = geometry.getAsJsonArray("coordinates")
                    for (k in 0 until coordinates.size()) {
                        val polygon = coordinates[k].asJsonArray
                        val polygonOptions = createPolygonOptions()
                        var centerLat = 0.0
                        var centerLng = 0.0
                        var pointCount = 0
                        
                        val outerRing = polygon[0].asJsonArray
                        for (j in 0 until outerRing.size()) {
                            val coord = outerRing[j].asJsonArray
                            val lng = coord[0].asDouble
                            val lat = coord[1].asDouble
                            val latLng = LatLng(lat, lng)
                            polygonOptions.add(latLng)
                            
                            centerLat += lat
                            centerLng += lng
                            pointCount++
                        }
                        
                        val center = LatLng(centerLat / pointCount, centerLng / pointCount)
                        polygonDataList.add(PolygonData(polygonOptions, name, description, center))
                    }
                }
            }
        }
    }
    
    private fun processSingleFeature(feature: JsonObject, polygonDataList: MutableList<PolygonData>) {
        val geometry = feature.getAsJsonObject("geometry")
        val coordinates = geometry.getAsJsonArray("coordinates")
        
        val polygonOptions = createPolygonOptions()
        var centerLat = 0.0
        var centerLng = 0.0
        var pointCount = 0
        
        val outerRing = coordinates[0].asJsonArray
        for (i in 0 until outerRing.size()) {
            val coord = outerRing[i].asJsonArray
            val lng = coord[0].asDouble
            val lat = coord[1].asDouble
            polygonOptions.add(LatLng(lat, lng))
            
            centerLat += lat
            centerLng += lng
            pointCount++
        }
        
        // 속성 정보 추출
        val properties = feature.getAsJsonObject("properties")
        val name = properties?.get("name")?.asString ?: "이름 없음"
        val description = properties?.get("description")?.asString ?: ""
        
        val center = LatLng(centerLat / pointCount, centerLng / pointCount)
        polygonDataList.add(PolygonData(polygonOptions, name, description, center))
    }
    
    private fun processDirectCoordinates(data: JsonObject, polygonDataList: MutableList<PolygonData>) {
        val coordinates = data.getAsJsonArray("coordinates")
        val polygonOptions = createPolygonOptions()
        var centerLat = 0.0
        var centerLng = 0.0
        
        for (i in 0 until coordinates.size()) {
            val coord = coordinates[i].asJsonArray
            val lng = coord[0].asDouble
            val lat = coord[1].asDouble
            polygonOptions.add(LatLng(lat, lng))
            
            centerLat += lat
            centerLng += lng
        }
        
        val center = LatLng(centerLat / coordinates.size(), centerLng / coordinates.size())
        polygonDataList.add(PolygonData(polygonOptions, "사용자 정의 영역", "", center))
    }
    
    private fun createPolygonOptions(): PolygonOptions {
        return PolygonOptions()
            .strokeColor(Color.BLUE)
            .strokeWidth(3f)
            .fillColor(Color.argb(50, 0, 0, 255))
    }
    
    private fun drawPolygonsOnMap(polygonDataList: List<PolygonData>) {
        polygonInfoList.clear()
        
        // 배치로 폴리곤 추가
        polygonDataList.forEach { polygonData ->
            map.addPolygon(polygonData.polygonOptions)
            polygonInfoList.add("${polygonData.name}${if (polygonData.description.isNotEmpty()) " - ${polygonData.description}" else ""}")
        }
        
        // 카메라 위치 조정 (첫 번째 폴리곤 중심으로)
        if (polygonDataList.isNotEmpty()) {
            val firstCenter = polygonDataList[0].bounds
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(firstCenter, 10f))
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        activityScope.cancel()
    }
} 