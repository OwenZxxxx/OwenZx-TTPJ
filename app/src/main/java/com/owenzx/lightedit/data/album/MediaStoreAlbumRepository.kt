package com.owenzx.lightedit.data.album

import android.content.ContentResolver
import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import android.util.Log

object MediaStoreAlbumRepository {

    /**
     * 查询本地所有图片（Android 10+）
     */
    fun queryAllPhotos(contentResolver: ContentResolver): List<PhotoItem> {

        val photos = mutableListOf<PhotoItem>()

        // Android 10+ 直接用 VOLUME_EXTERNAL
        val collection: Uri =
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME
        )

        // 时间倒序-符合用户需求，新拍的放在前面
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        // 访问系统相册的入口
        val cursor = contentResolver.query(
            collection,
            projection,
            null,
            null,
            sortOrder
        ) ?: return emptyList()

        // 安全读取cursor
        cursor.use { c ->
            val idCol = c.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameCol = c.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val dateCol = c.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
            val bucketIdCol = c.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID)
            val bucketNameCol =
                c.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)

            while (c.moveToNext()) {
                val id = c.getLong(idCol)
                val displayName = c.getString(nameCol)
                val dateAdded = c.getLong(dateCol)
                val bucketId = c.getLong(bucketIdCol)
                val bucketName = c.getString(bucketNameCol)

                // 拼出每张图的 Uri
                val contentUri = ContentUris.withAppendedId(collection, id)

                // 构建统一的数据对象
                photos.add(
                    PhotoItem(
                        id = id,
                        uri = contentUri,
                        displayName = displayName ?: "图片",
                        dateAdded = dateAdded,
                        bucketId = bucketId,
                        bucketName = bucketName ?: "未分类"
                    )
                )
            }
        }
        Log.d("AlbumRepo", "queryAllPhotos result size = ${photos.size}")
        return photos
    }
}