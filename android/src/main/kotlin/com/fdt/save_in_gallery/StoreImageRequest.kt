package com.fdt.save_in_gallery

import io.flutter.plugin.common.MethodChannel.Result

/**
 * Request parameters holder.
 */
data class StoreImageRequest(
    val method: String,
    val arguments: Map<String, Any>,
    val result: Result
)