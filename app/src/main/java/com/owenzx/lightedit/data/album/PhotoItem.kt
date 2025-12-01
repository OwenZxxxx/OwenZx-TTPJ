package com.owenzx.lightedit.data.album

import android.net.Uri

// 把一张照片的所有描述信息统一封装成一个 Kotlin 数据类
data class PhotoItem(
    val id: Long,
    val uri: Uri,
    val displayName: String,
    val dateAdded: Long,
    val bucketId: Long,            // 文件夹名（用于“按文件夹”Tab）
    val bucketName: String         // 用于排序：最新在前
)