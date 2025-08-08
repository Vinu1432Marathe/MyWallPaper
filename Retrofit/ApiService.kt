package com.magic.hd.wallpapers.remote

import com.mahadev.shivahd.live.wallpaper.Model.CategoryModelDto
import com.mahadev.shivahd.live.wallpaper.Model.TokenDto
import com.mahadev.shivahd.live.wallpaper.Model.WallpaperDto
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import java.util.concurrent.Executor


interface ApiService {
//    @Headers("Content-Type: application/json")
//    @POST("token")
//    suspend fun getToken(): Response<TokenDto>

    @POST("Category")
     fun getCategories(
        @Header("parentid") parentId: Int = 1
    ): Call<CategoryModelDto>


    @Headers("Content-Type: application/json")
    @POST("token")
    fun getToken(): Call<TokenDto> // ✅ No suspend

    @POST("imagegallery")
     fun    getParticularCategoriesWallpaper(
        @Header("indexnumber") indexnumber: Int ,
        @Header("currentpage") currentpage: Int
    ): Call<WallpaperDto>



}



