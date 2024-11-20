package com.evanemran.imagemarker

import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class MainActivity : AppCompatActivity() {

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri != null) {
            handleImageUri(uri)
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openPhotoPicker() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13 (API level 33) or above
            // Code for Android 13 and above
            pickMedia.launch(
                PickVisualMediaRequest.Builder()
                    .setMediaType(ImageOnly)
                    .build()
            )
        } else {
            Toast.makeText(this, "Photo Picker requires Android 13+", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleImageUri(uri: Uri) {
        val imageView = findViewById<ImageView>(R.id.imageView)
        imageView.setImageURI(uri)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
            }
        }

        val btnSelectPhoto = findViewById<FloatingActionButton>(R.id.fab_image_picker)
        btnSelectPhoto.setOnClickListener {
            openPhotoPicker()
        }

        val drawingView: DrawingView = findViewById(R.id.drawingView)
        val undoButton: ImageButton = findViewById(R.id.undoButton)
        val redoButton: ImageButton = findViewById(R.id.redoButton)
        val saveButton: ImageButton = findViewById(R.id.saveButton)

        undoButton.setOnClickListener {
            drawingView.undo() // Call the undo function
        }

        redoButton.setOnClickListener {
            drawingView.redo() // Call the redo function
        }

        saveButton.setOnClickListener {
            saveEditedImage()
        }
    }

    fun saveEditedImage() {
        val imageView: ImageView = findViewById(R.id.imageView)
        val drawingView: DrawingView = findViewById(R.id.drawingView)

        val bitmap = Bitmap.createBitmap(imageView.width, imageView.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        imageView.draw(canvas)
        drawingView.draw(canvas)

        // Save bitmap to file or gallery
        saveBitmapToGallery(this, bitmap)
    }

    private fun saveBitmapToGallery(context: Context, bitmap: Bitmap) {
        val filename = "${System.currentTimeMillis()}.png"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Use MediaStore for Android 10 (API level 29) and above
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MyApp") // Save in Pictures/MyApp folder
            }

            val contentResolver = context.contentResolver
            val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

            uri?.let {
                val outputStream: OutputStream? = contentResolver.openOutputStream(it)
                outputStream?.use {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                }
            }
        } else {
            // For Android 9 (API level 28) and below
            val picturesDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_PICTURES)
            val appDir = java.io.File(picturesDir, "MyApp")
            if (!appDir.exists()) appDir.mkdirs()

            val file = java.io.File(appDir, filename)
            java.io.FileOutputStream(file).use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }

            // Notify MediaScanner about the new file
            android.media.MediaScannerConnection.scanFile(context, arrayOf(file.toString()), null, null)
        }
    }

    /*private fun saveBitmapToFile(bitmap: Bitmap) {
        val filename = "${System.currentTimeMillis()}.png"
        val file = File(this.getExternalFilesDir(null), filename)
        FileOutputStream(file).use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }*/
}