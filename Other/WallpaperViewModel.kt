package com.mahadev.shivahd.live.wallpaper.Other

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.magic.hd.wallpapers.remote.ApiService
import com.mahadev.shivahd.live.wallpaper.Model.ModelWallpaper
import com.mahadev.shivahd.live.wallpaper.Model.WallpaperDto
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WallpaperViewModel(private val api: ApiService) : ViewModel() {

    private val wallpaperDataMap = mutableMapOf<Int, MutableLiveData<MutableList<ModelWallpaper>>>()
    private val pageMap = mutableMapOf<Int, Int>()
    private val totalPagesMap = mutableMapOf<Int, Int>()
    private val loadingMap = mutableMapOf<Int, Boolean>()
     val loadingStateMap = mutableMapOf<Int, MutableLiveData<Boolean>>() // ✅ Added

    fun getWallpapersLiveData(categoryId: Int): LiveData<MutableList<ModelWallpaper>> {
        return wallpaperDataMap.getOrPut(categoryId) { MutableLiveData(mutableListOf()) }
    }

    fun getLoadingLiveData(categoryId: Int): LiveData<Boolean> {
        return loadingStateMap.getOrPut(categoryId) { MutableLiveData(false) }
    }

    fun loadMoreWallpapers(context: Context, categoryId: Int) {
        if (loadingMap[categoryId] == true) return

        val currentPage = pageMap.getOrDefault(categoryId, 1)
        val totalPages = totalPagesMap.getOrDefault(categoryId, Int.MAX_VALUE)

        if (currentPage > totalPages) return

        // Start loading
        loadingMap[categoryId] = true
        loadingStateMap.getOrPut(categoryId) { MutableLiveData() }.postValue(true)

        api.getParticularCategoriesWallpaper(indexnumber = categoryId, currentpage = currentPage)
            .enqueue(object : Callback<WallpaperDto> {
                override fun onResponse(call: Call<WallpaperDto>, response: Response<WallpaperDto>) {
                    loadingMap[categoryId] = false
                    loadingStateMap[categoryId]?.postValue(false)

                    if (response.isSuccessful) {
                        val newData = response.body()?.data ?: emptyList()
                        totalPagesMap[categoryId] = response.body()?.totalRecords ?: 10

                        if (newData.isNotEmpty()) {
                            val liveData = wallpaperDataMap.getOrPut(categoryId) {
                                MutableLiveData(mutableListOf())
                            }
                            val currentList = liveData.value ?: mutableListOf()
                            currentList.addAll(newData)
                            liveData.postValue(currentList)
                            pageMap[categoryId] = currentPage + 1
                        } else {
                            totalPagesMap[categoryId] = currentPage
                        }
                    }
                }

                override fun onFailure(call: Call<WallpaperDto>, t: Throwable) {
                    loadingMap[categoryId] = false
                    loadingStateMap[categoryId]?.postValue(false)
                    t.printStackTrace()
                }
            })
    }

    fun resetPagination(categoryId: Int) {
        pageMap[categoryId] = 1
        totalPagesMap[categoryId] = Int.MAX_VALUE
        wallpaperDataMap[categoryId]?.postValue(mutableListOf())
        loadingStateMap[categoryId]?.postValue(false)
        loadingMap[categoryId] = false
    }
}


class WallpaperVMFactory(private val api: ApiService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return WallpaperViewModel(api) as T
    }
}

