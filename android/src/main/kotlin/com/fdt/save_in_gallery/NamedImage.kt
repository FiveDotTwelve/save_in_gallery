package com.fdt.save_in_gallery

import android.graphics.Bitmap
import java.util.*

/**
 *  Stores data from [io.flutter.plugin.common.MethodChannel.Result]
 *  passed as argument and converted in single image.
 */
data class NamedImage(
    val name: String?,
    val data: ByteArray
) {

    constructor(data: ByteArray) : this(null, data)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NamedImage

        if (name != other.name) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name?.hashCode() ?: 0
        result = 31 * result + data.contentHashCode()
        return result
    }

    /**
     * Converts [name] parameter to pair of
     * filename and appropriate format via [Bitmap.CompressFormat].
     *
     * This method supports any of [Bitmap.CompressFormat] values.
     *
     * If [name] is not provided or empty then current timestamp [Date.toISOTimestamp] is used
     * as name.
     *
     * Examples
     * name -> name.png to [Bitmap.CompressFormat.PNG]
     * name. -> name.png to [Bitmap.CompressFormat.PNG]
     * . -> [Date.toISOTimestamp].png to [Bitmap.CompressFormat.PNG]
     * name.jpg -> name.jpeg to [Bitmap.CompressFormat.JPEG]
     * name.JPEG -> name.jpeg to [Bitmap.CompressFormat.JPEG]
     * name.jpeg -> name.jpeg to [Bitmap.CompressFormat.JPEG]
     * name.webp -> name.webp to [Bitmap.CompressFormat.WEBP]
     */
    fun fileNameWithCompressFormat(): Pair<String, Bitmap.CompressFormat> {
        val name = name
        if (name.isNullOrEmpty()) {
            val filename = Date().toISOTimestamp()
            return filename to Bitmap.CompressFormat.PNG
        }

        val index = name.indexOfLast { it == '.' }

        if (index == -1) {
            val filename = name.takeIf { it.isNotEmpty() } ?: Date().toISOTimestamp()
            return filename to Bitmap.CompressFormat.PNG
        }

        val filename =
            name.substring(0, index).takeIf { it.isNotEmpty() } ?: Date().toISOTimestamp()
        val extension = name.substring(index + 1, name.length)

        val format = Bitmap.CompressFormat
            .values()
            .find { format ->
                when (format) {
                    Bitmap.CompressFormat.PNG,
                    Bitmap.CompressFormat.WEBP -> format.name.equals(extension, true)
                    Bitmap.CompressFormat.JPEG -> {
                        format.name.equals(extension, true) || "jpg".equals(extension, true)
                    }
                }
            } ?: Bitmap.CompressFormat.PNG

        return filename to format
    }
}