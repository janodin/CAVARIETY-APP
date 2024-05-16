// Import necessary Android classes and libraries
package com.example.cavariety

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.webkit.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

// Suppress deprecated warning
@Suppress("DEPRECATION")
// MainActivity class definition, extending AppCompatActivity
class MainActivity : AppCompatActivity() {
    // Variables declaration
    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    private lateinit var webView: WebView
    private var cameraImageUri: Uri? = null

    // Called when activity is created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set content view from layout file
        setContentView(R.layout.activity_main)
        // Find WebView from layout
        webView = findViewById(R.id.webview)
        // Setup WebView
        setupWebView()
        // Check camera permission
        checkCameraPermission()
    }

    // Setup WebView
    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView.apply {
            // Enable JavaScript
            settings.javaScriptEnabled = true
            // Enable DOM storage
            settings.domStorageEnabled = true
            // Set WebViewClient
            webViewClient = WebViewClient()
            // Set WebChromeClient
            webChromeClient = createWebChromeClient()
            // Load initial URL
            loadUrl("https://cavariety-web.onrender.com")
        }
    }

    // Create WebChromeClient for handling file chooser
    private fun createWebChromeClient() = object : WebChromeClient() {
        override fun onShowFileChooser(
            webView: WebView?,
            filePathCallback: ValueCallback<Array<Uri>>?,
            fileChooserParams: FileChooserParams?
        ): Boolean {
            // Set filePathCallback
            this@MainActivity.filePathCallback = filePathCallback
            // Open image chooser
            openImageChooser()
            return true
        }
    }

    // Check camera permission
    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), PERMISSION_CAMERA_REQUEST_CODE)
        }
    }

    // Handle permission request result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_CAMERA_REQUEST_CODE && grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            // Show toast if permission denied
            Toast.makeText(this, "Camera permission is required to use the camera.", Toast.LENGTH_LONG).show()
        }
    }

    // Register activity result for image chooser
    private val imageChooser = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_CANCELED) {
            // Handle the case where the user cancels the image chooser
            filePathCallback?.onReceiveValue(null)
        } else {
            // Pass selected image(s) to WebView
            filePathCallback?.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(result.resultCode, result.data) ?: cameraImageUri?.let { arrayOf(it) })
        }
        // Reset callback and cameraImageUri
        filePathCallback = null
        cameraImageUri = null
    }

    // Open image chooser
    @SuppressLint("QueryPermissionsNeeded")
    private fun openImageChooser() {
        val galleryIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
            // Set type to image
            type = "image/*"
        }
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
            intent.resolveActivity(packageManager)?.let {
                cameraImageUri = FileProvider.getUriForFile(this, "${applicationContext.packageName}.fileprovider", createImageFile()).also { uri ->
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
                }
            }
        }
        val chooserIntent = Intent.createChooser(galleryIntent, "Select Picture").apply {
            // Add camera intent to chooser
            putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(cameraIntent))
        }
        // Launch image chooser
        imageChooser.launch(chooserIntent)
    }

    // Create image file
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "JPEG_${timeStamp}.jpg")
    }

    // Handle back button press
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // If WebView can go back, go back
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            // Otherwise, perform default back action
            super.onBackPressed()
        }
    }

    // Companion object
    companion object {
        // Request code for camera permission
        private const val PERMISSION_CAMERA_REQUEST_CODE = 100
    }
}
