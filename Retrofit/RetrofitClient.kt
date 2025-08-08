package com.magic.hd.wallpapers.remote

import android.app.Activity
import android.content.Context
import android.util.Log
import com.mahadev.shivahd.live.wallpaper.Other.NewSplashActivity
import com.mahadev.shivahd.live.wallpaper.Other.SharedPrefsManager
import com.mahadev.shivahd.live.wallpaper.Other.Splash
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitClient {

    @JvmStatic
    fun createUnauthenticatedRetrofit(path: String): Retrofit {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // Can also be HEADERS or BASIC
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(path)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    @JvmStatic
    fun createRetrofit( path: String): Retrofit {
//        val tokenProvider = { SharedPrefsManager.getToken(context) }
        val tokenProvider = { NewSplashActivity.strToken }

        Log.e("CheckInRetro", "Token :: ${NewSplashActivity.strToken}")

        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenProvider))
            .build()

        return Retrofit.Builder()
            .baseUrl(path)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun create(context: Context): ApiService {

        NewSplashActivity.strToken = SharedPrefsManager.getTokenMain(context)

        val tokenProvider = { NewSplashActivity.strToken }

//        val client = OkHttpClient.Builder()
//            .addInterceptor(HttpLoggingInterceptor().apply {
//                level = HttpLoggingInterceptor.Level.BODY
//            }).build()


        NewSplashActivity.BaseUrl = SharedPrefsManager.getToken(context)
        Log.e("CheckInRetro", "baseUrl :: ${NewSplashActivity.BaseUrl}")
        Log.e("CheckInRetro", "baseUrl Token:: ${NewSplashActivity.strToken}")

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // Can also be HEADERS or BASIC
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenProvider))
            .addInterceptor(loggingInterceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(NewSplashActivity.BaseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

