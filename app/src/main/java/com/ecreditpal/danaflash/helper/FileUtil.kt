package com.ecreditpal.danaflash.helper

import android.content.Context
import android.database.Cursor
import android.os.Build
import android.provider.ContactsContract
import android.provider.MediaStore
import java.io.File


object FileUtil {
    /**
     * 获取文件大小，单位byte
     *
     * @param path
     * @return
     */
    fun getFileSize(path: String?): Long {
        val file = File(path)
        return if (file.exists() && file.isFile) {
            file.length()
        } else {
            0
        }
    }

    fun convertFileSizeByteToKB(filesize: Long): Long {
        return filesize / 1024
    }

    /**
     * 获取本机音乐列表
     * @return
     */
    fun getMusics(context: Context, ext: Boolean): Int {
        var num = 0
        var c: Cursor? = null
        try {
            c = context.contentResolver.query(
                if (ext) MediaStore.Audio.Media.EXTERNAL_CONTENT_URI else MediaStore.Audio.Media.INTERNAL_CONTENT_URI,
                null, null, null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER
            )
            while (c!!.moveToNext()) {
                val path = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)) // 路径
                if (!isExists(path)) {
                    continue
                }
                num++
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            c?.close()
        }
        return num
    }

    /**
     * 获取本机视频列表
     * @return
     */
    fun getVideos(context: Context, ext: Boolean): Int {
        var num = 0
        var c: Cursor? = null
        try {
            c = context.contentResolver.query(
                if (ext) MediaStore.Video.Media.EXTERNAL_CONTENT_URI else MediaStore.Video.Media.INTERNAL_CONTENT_URI,
                null, null, null, MediaStore.Video.Media.DEFAULT_SORT_ORDER
            )
            while (c!!.moveToNext()) {
                val path = c.getString(c.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)) // 路径
                if (!isExists(path)) {
                    continue
                }
                num++
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            c?.close()
        }
        return num
    }

    fun getImages(context: Context, ext: Boolean): Int {
        var num = 0
        var c: Cursor? = null
        try {
            c = context.contentResolver.query(
                if (ext) MediaStore.Images.Media.EXTERNAL_CONTENT_URI else MediaStore.Images.Media.INTERNAL_CONTENT_URI,
                null, null, null, MediaStore.Images.Media.DEFAULT_SORT_ORDER
            )
            while (c!!.moveToNext()) {
                val path = c.getString(c.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)) // 路径
                if (!isExists(path)) {
                    continue
                }
                num++
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            c?.close()
        }
        return num
    }

    fun getDownload(context: Context): Int {
        var num = 0
        var c: Cursor? = null
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                c = context.contentResolver.query(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    null,
                    null,
                    null,
                    null
                )
            }
            while (c!!.moveToNext()) {
                num++
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                c = context.contentResolver.query(
                    MediaStore.Downloads.INTERNAL_CONTENT_URI,
                    null,
                    null,
                    null,
                    null
                )
            }
            while (c!!.moveToNext()) {
                num++
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            c?.close()
        }
        return num
    }

    fun getContacts(context: Context): Int {
        var num = 0
        var c: Cursor? = null
        try {
            c = context.contentResolver.query(
                ContactsContract.Groups.CONTENT_URI,
                null,
                null,
                null,
                null
            )
            while (c!!.moveToNext()) {
                num++
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            c?.close()
        }
        return num
    }

    /**
     * 判断文件是否存在
     * @param path 文件的路径
     * @return
     */
    fun isExists(path: String?): Boolean {
        val file = File(path)
        return file.exists()
    }
}
