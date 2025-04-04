package com.example.travelonna.api

import android.content.Context
import android.util.Log
import com.example.travelonna.util.TokenManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "http://travelonna.shop:8080/"
    private const val TAG = "RetrofitClient"
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply { 
        level = HttpLoggingInterceptor.Level.BODY 
    }
    
    private fun getAuthInterceptor(context: Context): Interceptor {
        return Interceptor { chain ->
            val original = chain.request()
            val token = TokenManager.getAccessToken(context)
            
            val requestBuilder: Request.Builder = original.newBuilder()
            
            if (token.isNullOrEmpty()) {
                Log.w(TAG, "Authorization token is empty or null when making API request")
                // 토큰이 없어도 요청은 진행
            } else {
                // 토큰이 있으면 인증 헤더 추가
                Log.d(TAG, "Adding token to request: Bearer ${token.substring(0, minOf(10, token.length))}...")
                requestBuilder.header("Authorization", "Bearer $token")
            }
            
            val request = requestBuilder.build()
            chain.proceed(request)
        }
    }
    
    private fun getOkHttpClient(context: Context): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(getAuthInterceptor(context))
            .addInterceptor(loggingInterceptor)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }
    
    fun createRetrofit(context: Context): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getOkHttpClient(context))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    fun getAuthApi(context: Context): AuthApi = createRetrofit(context).create(AuthApi::class.java)
    
    fun getPlanApi(context: Context): PlanApi = createRetrofit(context).create(PlanApi::class.java)
} 