package com.mahadev.shivahd.live.wallpaper.Activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.app.WallpaperManager
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.mahadev.shivahd.live.wallpaper.Language.LocaleHelper
import com.mahadev.shivahd.live.wallpaper.Language.PreferencesHelper11
import com.mahadev.shivahd.live.wallpaper.Model.ModelWallpaper
import com.mahadev.shivahd.live.wallpaper.Other.NewSplashActivity
import com.mahadev.shivahd.live.wallpaper.Other.PrefsManager
import com.mahadev.shivahd.live.wallpaper.Other.SharePref
import com.mahadev.shivahd.live.wallpaper.Other.SharedPrefsManager
import com.mahadev.shivahd.live.wallpaper.R
import java.io.IOException
import java.io.OutputStream

class WallpaperShowActivity : BaseActivity() {

    private lateinit var imgFAv: ImageView
    private lateinit var btnBack: ImageView
    private lateinit var btnMore: ImageView
    private lateinit var btnSave: LinearLayout
    private lateinit var btnFavorite: LinearLayout
    private lateinit var container: LinearLayout
    private lateinit var btnShare: LinearLayout
    private lateinit var btnApply: TextView
    private lateinit var imgWallpaper: ImageView

    private var hasRated = false

    override fun attachBaseContext(newBase: Context) {
        val langCode = PreferencesHelper11(newBase).selectedLanguage ?: "en"
        super.attachBaseContext(LocaleHelper.setLocale(newBase, langCode))
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_wallpaper_show)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        hasRated = SharePref.isRate(this)

        val json = intent.getStringExtra("imageID")
        val image = Gson().fromJson(json, ModelWallpaper::class.java)

//        val image = intent.getSerializableExtra("imageID") as ModelWallpaper
        val favoriteList = PrefsManager.getFavoriteList(this)
        val isFavorite = favoriteList.any { it.albumImage == image.albumImage }

        btnBack = findViewById(R.id.btnBack)
        btnBack.setOnClickListener { onBackPressed() }

        imgFAv = findViewById(R.id.imgFAv)
        btnMore = findViewById(R.id.btnMore)
        btnSave = findViewById(R.id.llSave)
        btnFavorite = findViewById(R.id.llFavorite)
        btnShare = findViewById(R.id.llShare)
        btnApply = findViewById(R.id.btnApply)
        imgWallpaper = findViewById(R.id.imgWallpaper)



        imgFAv.setImageResource(
            if (isFavorite) R.drawable.favorite_icon1 else R.drawable.ic_fav_unfill
        )


        NewSplashActivity.ImgUri = SharedPrefsManager.getImgUri(this)
        Glide.with(this).load(NewSplashActivity.ImgUri+"hd/"+ image.albumImage + ".webp")
            .placeholder(R.drawable.ic_placeholder).error(R.drawable.ic_placeholder)
            .into(imgWallpaper)



        btnMore.setOnClickListener {
            showReportPopup(it) {

                val email = "escollabapp@gmail.com"// or hardcoded like "support@example.com"


                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:") // must be "mailto:" or it won't work
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(email)) // recipient
                    putExtra(Intent.EXTRA_SUBJECT, "Feedback for Your App")
                    putExtra(Intent.EXTRA_TEXT, "Hi team, I have a suggestion...")
                }
                try {
                    startActivity(Intent.createChooser(intent, "Send Email"))
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnSave.setOnClickListener {

            if (hasRated) {
                val drawable = imgWallpaper.drawable
                if (drawable != null && drawable is android.graphics.drawable.BitmapDrawable) {
                    val bitmap = drawable.bitmap
                    saveImageToGallery(bitmap)
                }
            } else {
                RateDialog()
            }

        }

        btnFavorite.setOnClickListener {
            if (isFavorite) {
                favoriteList.removeAll { it.albumImage == image.albumImage }
                imgFAv.setImageResource(R.drawable.ic_fav_unfill)
            } else {
                favoriteList.add(image)
                imgFAv.setImageResource(R.drawable.favorite_icon1)
            }
            PrefsManager.saveFavoriteList(this, favoriteList)

        }

        btnShare.setOnClickListener {
            val drawable = imgWallpaper.drawable
            if (drawable is BitmapDrawable) {
                val bitmap = drawable.bitmap
                shareImage(bitmap)
            }
        }

        btnApply.setOnClickListener {
            showBottomDialog()
        }


    }

    fun isInFavorites(url: String, list: List<ModelWallpaper>) = list.any { it.albumImage == url }

    //menu Report
    private fun showReportPopup(anchorView: View, callback: () -> Unit) {
        val inflater = LayoutInflater.from(this)
        val popupView = inflater.inflate(R.layout.report_menu, null)

        val popupWindow = PopupWindow(
            popupView,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            true
        )

        popupWindow.elevation = 10f
        popupWindow.isOutsideTouchable = true

        // Convert 10dp to pixels
        val marginRightPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 10f, resources.displayMetrics
        ).toInt()

        // Get anchorView location
        val location = IntArray(2)
        anchorView.getLocationOnScreen(location)
        val anchorX = location[0]
        val anchorY = location[1]

        // Measure popup width
        popupView.measure(
            View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED
        )
        val popupWidth = popupView.measuredWidth

        // Calculate X position to keep 10dp from right edge
        val screenWidth = resources.displayMetrics.widthPixels
        val x = screenWidth - popupWidth - marginRightPx
        val y = anchorY + anchorView.height

        // Show the popup at calculated position
        popupWindow.showAtLocation(anchorView, Gravity.NO_GRAVITY, x, y)

        // Handle click
        val btnApply = popupView.findViewById<TextView>(R.id.btnApply)
        btnApply.setOnClickListener {
            callback()
            popupWindow.dismiss()
        }
    }

    //btn Share
    private fun shareImage(bitmap: Bitmap) {
        val fileName = "shared_image_${System.currentTimeMillis()}.png"
        val resolver = contentResolver
        val imageCollection =
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

        val imageDetails = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        val imageUri = resolver.insert(imageCollection, imageDetails)

        imageUri?.let { uri ->
            resolver.openOutputStream(uri)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }

            imageDetails.clear()
            imageDetails.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, imageDetails, null, null)

            // Share intent
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, uri)
                type = "image/png"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(shareIntent, "Share Image"))
        }
    }

    //btn Save
    private fun saveImageToGallery(bitmap: Bitmap) {
        val filename = "IMG_${System.currentTimeMillis()}.jpg"
        val fos: OutputStream?

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(
                MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/LordShivaWallpapers"
            ) // Creates a folder
        }

        val imageUri =
            contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        if (imageUri != null) {
            fos = contentResolver.openOutputStream(imageUri)
            fos?.use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            }
            hasRated = true
            Toast.makeText(this, getString(R.string.saved_to_gallery), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, getString(R.string.error_saving_image), Toast.LENGTH_SHORT).show()
        }
    }

    //btn Apply
    @SuppressLint("MissingInflatedId")
    private fun showBottomDialog() {
        val dialogView = layoutInflater.inflate(R.layout.bottom_dialogbox, null)
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(dialogView)

        val rbHome = dialogView.findViewById<RadioButton>(R.id.rbHome)
        val rbLock = dialogView.findViewById<RadioButton>(R.id.rbLock)
        val rbBoth = dialogView.findViewById<RadioButton>(R.id.rvHomeLock)
        val btnSetWallpaper = dialogView.findViewById<TextView>(R.id.btnSetWallpaper)

        val radioButtons = listOf(rbHome, rbLock, rbBoth)
        for (rb in radioButtons) {
            rb.setOnClickListener {
                radioButtons.forEach { it.isChecked = false }
                rb.isChecked = true
            }
        }

        btnSetWallpaper.setOnClickListener {
            val drawable = imgWallpaper.drawable
            if (drawable != null && drawable is BitmapDrawable) {
                val bitmap = drawable.bitmap
                val wallpaperManager = WallpaperManager.getInstance(applicationContext)

                try {
                    when {
                        rbHome.isChecked -> {
                            wallpaperManager.setBitmap(
                                bitmap, null, true, WallpaperManager.FLAG_SYSTEM
                            )
                        }

                        rbLock.isChecked -> {
                            wallpaperManager.setBitmap(
                                bitmap, null, true, WallpaperManager.FLAG_LOCK
                            )
                        }

                        rbBoth.isChecked -> {
                            wallpaperManager.setBitmap(bitmap)
                        }

                        else -> {
                            Toast.makeText(
                                this,
                                getString(R.string.please_select_an_option),
                                Toast.LENGTH_SHORT
                            ).show()
                            return@setOnClickListener
                        }
                    }

                    showSuccessDialog()
                    bottomSheetDialog.dismiss()

                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(
                        this, getString(R.string.failed_to_set_wallpaper), Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(this, getString(R.string.invalid_image), Toast.LENGTH_SHORT).show()
            }
        }

        // ✅ Show dialog outside the click listener
        bottomSheetDialog.show()
    }

    private fun showSuccessDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dilogbox_success)
        dialog.setCancelable(false)

        val window = dialog.window
        val wlp = window?.attributes
        wlp?.gravity = Gravity.CENTER
        wlp?.flags = wlp?.flags?.and(WindowManager.LayoutParams.FLAG_BLUR_BEHIND.inv()) ?: 0
        window?.attributes = wlp
        window?.setBackgroundDrawableResource(R.color.transparent)
        window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT
        )
        val btnReportUs = dialog.findViewById<TextView>(R.id.btnReportUs)
        btnReportUs.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }


    private fun RateDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dilogbox_rate, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).setCancelable(true).create()


        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val star1 = dialogView.findViewById<ImageView>(R.id.star1)
        val star2 = dialogView.findViewById<ImageView>(R.id.star2)
        val star3 = dialogView.findViewById<ImageView>(R.id.star3)
        val star4 = dialogView.findViewById<ImageView>(R.id.star4)
        val star5 = dialogView.findViewById<ImageView>(R.id.star5)
        val txtYes = dialogView.findViewById<TextView>(R.id.txtYes)

        star1.setOnClickListener {

            star1.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.star_select))
            star2.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.star_unselectr))
            star3.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.star_unselectr))
            star4.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.star_unselectr))
            star5.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.star_unselectr))
            FeebBack()
            dialog.dismiss()
        }
        star2.setOnClickListener {

            star1.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.star_select))
            star2.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.star_select))
            star3.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.star_unselectr))
            star4.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.star_unselectr))
            star5.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.star_unselectr))
            FeebBack()
            dialog.dismiss()
        }
        star3.setOnClickListener {

            star1.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.star_select))
            star2.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.star_select))
            star3.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.star_select))
            star4.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.star_unselectr))
            star5.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.star_unselectr))
            FeebBack()
            dialog.dismiss()
        }
        star4.setOnClickListener {

            star1.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.star_select))
            star2.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.star_select))
            star3.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.star_select))
            star4.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.star_select))
            star5.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.star_unselectr))
            SharePref.rateUs(this)
            hasRated = true
            SharePref.setRate(this, true)
            dialog.dismiss()

        }
        star5.setOnClickListener {

            star1.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.star_select))
            star2.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.star_select))
            star3.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.star_select))
            star4.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.star_select))
            star5.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.star_select))
            SharePref.rateUs(this)
            hasRated = true
            SharePref.setRate(this, true)
            dialog.dismiss()

        }

        txtYes.setOnClickListener {
            hasRated = true
            dialog.dismiss()
        }

        dialog.show()
    }

    fun FeebBack() {
        SharePref.setRate(this, true)
        hasRated = true
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            setPackage("com.google.android.gm")
            putExtra(Intent.EXTRA_EMAIL, arrayOf("escollabapp@gmail.com"))
            putExtra(Intent.EXTRA_SUBJECT, "Subject")
            putExtra(Intent.EXTRA_TEXT, "Body here...")
        }

        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, getString(R.string.gmail_app_is_not_installed), Toast.LENGTH_SHORT)
                .show()
        }
    }

}