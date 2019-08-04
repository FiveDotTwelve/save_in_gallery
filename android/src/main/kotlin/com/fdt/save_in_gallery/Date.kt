package com.fdt.save_in_gallery

import java.text.SimpleDateFormat
import java.util.*

fun Date.toISOTimestamp(): String {
    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    return simpleDateFormat.format(this)
}