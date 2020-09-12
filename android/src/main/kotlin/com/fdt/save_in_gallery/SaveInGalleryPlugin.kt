package com.fdt.save_in_gallery

import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Bitmap
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
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

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
        private const val IMAGE_FILE_EXTENSION = "png"

        @JvmStatic
        fun registerWith(registrar: Registrar) {
            val channel = MethodChannel(registrar.messenger(), CHANNEL_NAME)
            val galleryPlugin = SaveInGalleryPlugin(registrar.activity())
            channel.setMethodCallHandler(galleryPlugin)
            registrar.addRequestPermissionsResultListener(galleryPlugin)
        }
    }

    data class StoreImageRequest(
        val method: String,
        val arguments: Map<String, Any>,
        val result: Result
    )

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

        if (!hasWriteStoragePermission()) {
            storeImagesQue.add(request)
            requestStoragePermission()
            return
        }

        val directory = if (directoryName == null || directoryName.isEmpty()) {
            getExternalStoragePublicDirectory(DIRECTORY_PICTURES)
        } else {
            File(getExternalStoragePublicDirectory(DIRECTORY_PICTURES), directoryName)
        }

        if (!directory.exists()) {
            directory.mkdirs()
        }

        val name =
            imageName ?: TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()).toString()
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

        val formattedName = makeSureNameFormatIsCorrect(name)
        val imageFile = File(directory, formattedName)

        if (imageFile.exists()) {
            imageFile.delete()
        }

        try {
            FileOutputStream(File(directory, formattedName)).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            request.result.success(true)
        } catch (e: IOException) {
            request.result.error("ERROR", "Error while saving image into file: ${e.message}", null)
        }
    }

    private fun onSaveImagesCalled(request: StoreImageRequest) {
        val arguments = request.arguments

        @Suppress("UNCHECKED_CAST") val images = arguments["images"] as List<ByteArray>
        val directoryName = arguments["directoryName"] as? String

        if (!hasWriteStoragePermission()) {
            storeImagesQue.add(request)
            requestStoragePermission()
            return
        }

        val directory = if (directoryName == null || directoryName.isEmpty()) {
            getExternalStoragePublicDirectory(DIRECTORY_PICTURES)
        } else {
            File(getExternalStoragePublicDirectory(DIRECTORY_PICTURES), directoryName)
        }

        if (!directory.exists()) {
            directory.mkdirs()
        }


        images.forEach { imageBytes ->
            val name = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()).toString()
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

            val formattedName = makeSureNameFormatIsCorrect(name)
            val imageFile = File(directory, formattedName)

            if (imageFile.exists()) {
                imageFile.delete()
            }

            try {
                FileOutputStream(File(directory, formattedName)).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
            } catch (e: IOException) {
                request.result.error(
                    "ERROR",
                    "Error while saving image into file: ${e.message}",
                    null
                )
            }
        }

        request.result.success(true)
    }

    private fun onSaveNamedImagesCalled(request: StoreImageRequest) {
        val arguments = request.arguments

        val directoryName = arguments["directoryName"] as? String
        @Suppress("UNCHECKED_CAST") val images = arguments["images"] as Map<String, ByteArray>

        if (!hasWriteStoragePermission()) {
            storeImagesQue.add(request)
            requestStoragePermission()
            return
        }

        val directory = if (directoryName == null || directoryName.isEmpty()) {
            getExternalStoragePublicDirectory(DIRECTORY_PICTURES)
        } else {
            File(getExternalStoragePublicDirectory(DIRECTORY_PICTURES), directoryName)
        }

        if (!directory.exists()) {
            directory.mkdirs()
        }

        images.forEach { imageMap ->
            val name = imageMap.key
            val imageBytes = imageMap.value
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            val formattedName = makeSureNameFormatIsCorrect(name)

            val imageFile = File(directory, formattedName)

            if (imageFile.exists()) {
                imageFile.delete()
            }

            try {
                FileOutputStream(File(directory, formattedName)).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
            } catch (e: IOException) {
                request.result.error(
                    "ERROR",
                    "Error while saving image into file: ${e.message}",
                    null
                )
            }
        }

        request.result.success(true)
    }

    private fun makeSureNameFormatIsCorrect(name: String): String =
        when {
            !name.contains('.') -> "$name.$IMAGE_FILE_EXTENSION"
            name.substringAfterLast('.', "").isEmpty() ||
                    !name.substringAfterLast(
                        '.',
                        ""
                    ).equals(IMAGE_FILE_EXTENSION, ignoreCase = true) -> {
                name.replaceAfterLast('.', IMAGE_FILE_EXTENSION)
            }
            else -> name
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
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            while (!storeImagesQue.isEmpty()) {
                val imageRequest = storeImagesQue.pop()
                onMethodCalled(imageRequest)
            }
        } else {
            while (!storeImagesQue.isEmpty()) {
                val imageRequest = storeImagesQue.pop()
                imageRequest.result.error(
                    "ERROR",
                    "Saving in gallery error for $imageRequest",
                    null
                )
            }
        }
    }
}
