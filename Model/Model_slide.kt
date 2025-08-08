package com.mahadev.shivahd.live.wallpaper.Model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

data class Model_slide(val Image: Int, val Title: String, val Dis: String, val ads_show : Int)
data class WallpaperModel(
    val id: Int,
    val wallPaperImage: Int, // Assuming drawable image
    var isSelected: Boolean = false
)

data class CategoryModelDto(
    @SerializedName("data")
    val data: List<CategoryModel>,
    @SerializedName("status")
    val status: Int,
    @SerializedName("total_category")
    val totalCategory: Int
)
data class CategoryModel(
    @SerializedName("categoryDesc")
    val categoryDesc: String,
    @SerializedName("categoryID")
    val categoryID: Int,
    @SerializedName("categoryImage")
    val categoryImage: String,
    @SerializedName("categoryName")
    val categoryName: String,
    var isSelected: Boolean = false
)

data class TokenDto(
    @SerializedName("token")
    val token: String
)


data class WallpaperDto(
    @SerializedName("status")
    val status: Int,
    @SerializedName("totalRecords")
    val totalRecords: Int,
    @SerializedName("rowsPerPage")
    val rowsPerPage: Int,
    @SerializedName("data")
    val data: List<ModelWallpaper>



)
@Parcelize
data class ModelWallpaper(
    @SerializedName("imageID")
    val imageID: Int,
    @SerializedName("categoryID")
    val categoryID: Int,
    @SerializedName("albumImage")
    val albumImage: String,
    @SerializedName("totalLikes")
    val totalLikes: Int,
    @SerializedName("totalDownloads")
    val totalDownloads: Int,
    @SerializedName("creatorInfo")
    val creatorInfo: String? = null,
    @SerializedName("creatorLink")
    val creatorLink: String?,
    @SerializedName("licence")
    val licence: String? = null

): Parcelable {
    override fun equals(other: Any?): Boolean {
        return other is ModelWallpaper && imageID == other.imageID
    }

    override fun hashCode(): Int {
        return imageID
    }
}
//
//@Parcelize
//data class WallpaperModel(
//   val imageID: Int,
//    @SerializedName("albumImage")
//    val albumImage: String,
//    @SerializedName("categoryID")
//    val categoryID: Int,
//    @SerializedName("creatorInfo")
//    val creatorInfo: String,
//    @SerializedName("creatorLink")
//    val creatorLink: String?,
////    @SerializedName("imageID")
//    @SerializedName("licence")
//    val licence: String,
//    @SerializedName("totalDownloads")
//    val totalDownloads: Int,
//    @SerializedName("totalLikes")
//    val totalLikes: Int,
//    var isFavorite: Boolean = false
//): Parcelable