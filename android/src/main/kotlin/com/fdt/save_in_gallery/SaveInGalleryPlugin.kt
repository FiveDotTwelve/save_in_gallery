package com.fdt.save_in_gallery

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Environment.DIRECTORY_PICTURES
import android.os.Environment.getExternalStoragePublicDirectory
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.common.PluginRegistry.Registrar
import java.io.File
import java.io.IOException
import java.util.*

class SaveInGalleryPlugin(
    private val context: Activity
) : MethodCallHandler,
    PluginRegistry.RequestPermissionsResultListener {

    companion object {

        private const val CHANNEL_NAME = "com.fdt/save_in_gallery_channel"
        private const val SAVE_IMAGE_METHOD_KEY = "saveImageKey"
        private const val SAVE_IMAGES_METHOD_KEY = "saveImagesKey"
        private const val SAVE_NAMED_IMAGES_METHOD_KEY = "saveNamedImagesKey"
        private const val STORAGE_PERMISSION_REQUEST = 3

        @JvmStatic
        fun registerWith(registrar: Registrar) {
            val channel = MethodChannel(registrar.messenger(), CHANNEL_NAME)
            val galleryPlugin = SaveInGalleryPlugin(registrar.activity())
            channel.setMethodCallHandler(galleryPlugin)
            registrar.addRequestPermissionsResultListener(galleryPlugin)
        }
    }

    private val storeImagesQue = ArrayDeque<StoreImageRequest>()

    override fun onMethodCall(call: MethodCall, result: Result) {
        onMethodCalled(StoreImageRequest(call.method, call.arguments(), result))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>?,
        grantResults: IntArray?
    ): Boolean = when (requestCode) {
        STORAGE_PERMISSION_REQUEST -> {
            onStoragePermissionResult(grantResults ?: intArrayOf())
            true
        }
        else -> false
    }

    private fun onMethodCalled(request: StoreImageRequest) {
        when (request.method) {
            SAVE_IMAGE_METHOD_KEY -> onSaveImageCalled(request)
            SAVE_IMAGES_METHOD_KEY -> onSaveImagesCalled(request)
            SAVE_NAMED_IMAGES_METHOD_KEY -> onSaveNamedImagesCalled(request)
            else -> request.result.notImplemented()
        }
    }

    private fun onSaveImageCalled(request: StoreImageRequest) {
        val arguments = request.arguments
        val imageBytes = arguments["imageBytes"] as ByteArray
        val imageName = arguments["imageName"] as? String
        val directoryName = arguments["directoryName"] as? String

        if (!checkWriteStoragePermission(request)) {
            return
        }

        val namedImages = listOf(NamedImage(imageName, imageBytes))

        saveImages(directoryName, namedImages, request.result)
    }

    private fun onSaveImagesCalled(request: StoreImageRequest) {
        val arguments = request.arguments

        @Suppress("UNCHECKED_CAST") val images = arguments["images"] as List<ByteArray>
        val directoryName = arguments["directoryName"] as? String

        if (!checkWriteStoragePermission(request)) {
            return
        }

        val namedImages = images.map { NamedImage(it) }

        saveImages(directoryName, namedImages, request.result)
    }

    private fun onSaveNamedImagesCalled(request: StoreImageRequest) {
        val arguments = request.arguments

        val directoryName = arguments["directoryName"] as? String
        @Suppress("UNCHECKED_CAST") val images = arguments["images"] as Map<String, ByteArray>

        if (!checkWriteStoragePermission(request)) {
            return
        }

        val namedImages = images.map { NamedImage(it.key, it.value) }

        saveImages(directoryName, namedImages, request.result)
    }

    @SuppressLint("DefaultLocale")
    private fun saveImages(
        directoryName: String?,
        images: List<NamedImage>,
        result: Result
    ) {
        val directory = if (directoryName.isNullOrEmpty()) {
            getExternalStoragePublicDirectory(DIRECTORY_PICTURES)
        } else {
            File(getExternalStoragePublicDirectory(DIRECTORY_PICTURES), directoryName)
        }

        if (!directory.exists()) {
            directory.mkdirs()
        }

        images.forEach { namedImage ->
            val nameWithCompressFormat = namedImage.fileNameWithCompressFormat()
            val data = namedImage.data
            val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)

            val fileName = nameWithCompressFormat.first
            val format = nameWithCompressFormat.second
            val fullName = "%s.%s".format(fileName, format.name.toLowerCase())

            //clean up previous file
            File(directory, fullName).apply {
                if (exists()) {
                    delete()
                }
            }

            try {
                File(directory, fullName).outputStream().use { out ->
                    bitmap.compress(format, 100, out)
                }
            } catch (e: IOException) {
                result.error(
                    ErrorCode.SAVE_IMAGE_ERROR_CODE,
                    "Error while saving image into file: ${e.message}",
                    null
                )
            }
        }

        result.success(true)
    }

    private fun checkWriteStoragePermission(request: StoreImageRequest): Boolean =
        if (hasWriteStoragePermission()) {
            true
        } else {
            storeImagesQue.add(request)
            requestStoragePermission()
            false
        }

    private fun hasWriteStoragePermission(): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(
            context,
            arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
            STORAGE_PERMISSION_REQUEST
        )
    }

    private fun onStoragePermissionResult(grantResults: IntArray) {
        val hasWritePermission =
            grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED

        while (!storeImagesQue.isEmpty()) {
            val imageRequest = storeImagesQue.pop()

            if (hasWritePermission) {
                onMethodCalled(imageRequest)
            } else {
                imageRequest.result.error(
                    ErrorCode.WRITE_STORAGE_PERMISSION_ERROR_CODE,
                    "Saving in gallery error for $imageRequest",
                    null
                )
            }
        }
    }
}
