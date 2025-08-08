package com.mahadev.shivahd.live.wallpaper.Activity

import android.Manifest
import android.app.AlertDialog
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mahadev.shivahd.live.wallpaper.Adapter.FolderImageAdapter
import com.mahadev.shivahd.live.wallpaper.Language.LocaleHelper
import com.mahadev.shivahd.live.wallpaper.Language.PreferencesHelper11
import com.mahadev.shivahd.live.wallpaper.R

class FolderImageActivity : BaseActivity() {

    private val REQUEST_CODE_PERMISSION = 1001

    private lateinit var rlNoData: RelativeLayout
    private lateinit var imgBack: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FolderImageAdapter

    override fun attachBaseContext(newBase: Context) {
        val langCode = PreferencesHelper11(newBase).selectedLanguage ?: "en"
        super.attachBaseContext(LocaleHelper.setLocale(newBase, langCode))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_folder_image)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        rlNoData = findViewById(R.id.rlNoData)
        imgBack = findViewById(R.id.imgBack)
        imgBack.setOnClickListener { onBackPressed() }
        ensurePermission()

    }


    private fun ensurePermission() {
        val permission = if (Build.VERSION.SDK_INT >= 33) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), REQUEST_CODE_PERMISSION)
        } else {
            loadImages()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadImages()
        } else {
            AlertDialog.Builder(this)
                .setTitle("Permission Required")
                .setMessage("Please allow notification permission from settings to proceed.")
                .setPositiveButton("Go to Settings") { _, _ ->
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.data = Uri.parse("package:$packageName")
                    startActivity(intent)
                }
                .setNegativeButton("Cancel", null)
                .show()
            loadImages()
        }
    }

    private fun loadImages() {
        val images = loadLordShivaImages(this)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewDownload)

        if (images.isEmpty()){
            rlNoData.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        }else{
            rlNoData.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            adapter = FolderImageAdapter(this,images)
            val layoutManager = GridLayoutManager(this, 2)
            layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if ((adapter.getItemViewType(position) == adapter.TYPE_NORMAL)) 1 else 2
                }
            }
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter =adapter
        }


    }

    private fun loadLordShivaImages(ctx: Context): List<android.net.Uri> {
        val result = mutableListOf<android.net.Uri>()

        val projection = arrayOf(MediaStore.Images.Media._ID)
        val selection: String
        val selectionArgs: Array<String>

        if (Build.VERSION.SDK_INT >= 29) {
            selection = "${MediaStore.Images.Media.RELATIVE_PATH} LIKE ?"
            selectionArgs = arrayOf("%Pictures/LordShivaWallpapers/%")
        } else {
            val path = android.os.Environment.getExternalStoragePublicDirectory(
                android.os.Environment.DIRECTORY_PICTURES + "/LordShivaWallpapers"
            ).absolutePath
            selection = "${MediaStore.Images.Media.DATA} LIKE ?"
            selectionArgs = arrayOf("$path/%")
        }

        ctx.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            MediaStore.Images.Media.DATE_ADDED + " DESC"
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                result.add(uri)
            }
        }
        return result
    }
}
