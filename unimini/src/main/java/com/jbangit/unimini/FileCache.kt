package com.jbangit.unimini

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.jbangit.unimini.network.ApiManager

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import okio.Okio
import java.io.File

import java.lang.ref.SoftReference

/**
 * create by erolc at 2021/8/16 09:31.
 * 文件缓存，将一个在线的图片，视频，文件缓存在本地
 */
object FileCache {
    private var reference: SoftReference<Context>? = null
    private var childDir: File? = null
    private val okHttpClient = ApiManager.initClient()


    fun init(context: Context) {
        if (reference == null)
            reference = SoftReference(context)

        reference?.get()?.apply {
            if (childDir == null || childDir?.exists() == false) {
                val cacheDir = context.externalCacheDir
                cacheDir?.apply {
                    if (!exists()) {
                        mkdirs()
                    }
                }
                childDir = File(cacheDir!!.path, "files")
                childDir?.apply {
                    if (!exists()) {
                        mkdirs()
                    }
                }
            }
        }
    }

    private fun getDir(directory: String): File {
        val childDir = File(childDir!!.path, directory)
        childDir.apply {
            if (!exists()) {
                mkdirs()
            }
        }
        return childDir
    }

    fun getCacheDir(directory: String):File{
        return getDir(directory)
    }

    /**
     * name:包含后缀
     */
    suspend fun cache(url: String, directory: String = "", name: String = ""): String? =
        withContext(Dispatchers.IO) {
            var path: String? = null
            val request = Request.Builder().get().url(url).build()
            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val fileName = if (name.isEmpty()) {
                    val mimeType = response.header("Content-Type")
                    val extension = MimeTypes.getExtensionFromMimeType(mimeType)
                    "${System.currentTimeMillis()}.$extension"
                } else {
                    name
                }
                val dir = if (directory.isEmpty()) {
                    childDir
                } else getDir(directory)
                val file = File(dir, fileName)
                path = file.path
                val target = Okio.sink(file)
                response.body()?.source()?.readAll(target)
            }
            path
        }

    /**
     * 针对图片的转化
     */
    suspend fun toImgByteArray(url: String): ByteArray? = withContext(Dispatchers.IO) {
        val request = Request.Builder().get().url(url).build()
        val response = okHttpClient.newCall(request).execute()
        var result: ByteArray? = null
        if (response.isSuccessful) {
            val mimeType = response.header("Content-Type")
            mimeType?.apply {
                if (this.contains("image")) {
                    result = response.body()?.source()?.readByteArray()
                }
            }
        }
        result
    }

    /**
     * 所有文件的转化
     * @return mimeType 和 ByteArray
     */
    suspend fun toByteArray(url: String): Pair<String?, ByteArray?>? = withContext(Dispatchers.IO) {
        val request = Request.Builder().get().url(url).build()
        val response = okHttpClient.newCall(request).execute()
        var pair: Pair<String?, ByteArray?>? = null
        if (response.isSuccessful) {
            val mimeType = response.header("Content-Type")
            val array = response.body()?.source()?.readByteArray()
            pair = mimeType to array
        }
        pair
    }

    fun getCacheFilePath(directory: String = "", name: String): String? {
        val dir = if (directory.isEmpty()) {
            childDir
        } else getDir(directory)
        val file = File(dir, name)
        return if (file.exists()) {
            file.path
        } else {
            null
        }
    }

    fun getCacheFilePaths(directory: String = ""): Array<String>? {
        val dir = if (directory.isEmpty()) {
            childDir
        } else getDir(directory)
        return if (dir?.isDirectory == true) {
            dir.list()
        } else {
            null
        }
    }
}

/**
 * 文件缓存
 */
fun Context.fileCache(url: String, directory: String = "", name: String = ""): LiveData<String?> {
    FileCache.init(this)
    return liveData {
        emit(FileCache.cache(url, directory, name))
    }
}

suspend fun Context.fileCacheSync(url: String, directory: String = "", name: String = ""): String? {
    FileCache.init(this)
    return FileCache.cache(url, directory, name)
}

/**
 * 获取缓存的文件路径
 */
fun Context.getCacheFilePath(directory: String = "", name: String): String? {
    FileCache.init(this)
    return FileCache.getCacheFilePath(directory, name)
}

fun Context.getCacheFilePaths(directory: String = ""): Array<String>? {
    FileCache.init(this)
    return FileCache.getCacheFilePaths(directory)
}


/**
 * 将本地图片变成bitmap，只适合content://
 */
suspend fun getByteArray(context: Context, localUrl: String): ByteArray? =
    withContext(Dispatchers.IO) {
        val uri = Uri.parse(localUrl)
        if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            val stream = context.contentResolver.openInputStream(uri)
            stream?.let { Okio.buffer(Okio.source(it)).readByteArray() }
        } else null
    }


fun getByteArray(url: String) = liveData { emit(toByteArray(url)) }

suspend fun toByteArray(url: String) = FileCache.toImgByteArray(url)