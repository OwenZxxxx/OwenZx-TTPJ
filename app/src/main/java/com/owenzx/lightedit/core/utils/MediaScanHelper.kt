package com.owenzx.lightedit.core.utils

import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import android.util.Log

object MediaScanHelper {

    private const val TAG = "MediaScan"

    /**
     * 扫描 /sdcard/Pictures 目录（包含子目录）
     * 把其中的图片更新到 MediaStore。
     *
     * 注意：仅用于开发调试，不要每次都在主流程里频繁调用。
     */
    fun scanPicturesRoot(context: Context) {
        // 这个 API 虽然标记了 deprecated，但在 Android 10+ 仍然可用，且正好是我们要的公有目录
        val picturesDir = Environment
            .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)

        if (picturesDir == null) {
            Log.w(TAG, "Pictures dir is null")
            return
        }

        Log.d(TAG, "Start scanning: ${picturesDir.absolutePath}")

        // 只传目录路径，系统会递归扫描
        MediaScannerConnection.scanFile(
            context,
            arrayOf(picturesDir.absolutePath),
            null
        ) { path, uri ->
            Log.d(TAG, "Scanned file: $path -> $uri")
        }
    }

    /**
     * 如果你有自定义目录，例如 /sdcard/Pictures/MyFolder，
     * 也可以单独扫描。
     */
    fun scanCustomDir(context: Context, dirPath: String) {
        MediaScannerConnection.scanFile(
            context,
            arrayOf(dirPath),
            null
        ) { path, uri ->
            Log.d(TAG, "Scanned custom: $path -> $uri")
        }
    }
}