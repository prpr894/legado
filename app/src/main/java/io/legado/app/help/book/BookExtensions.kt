@file:Suppress("unused")

package io.legado.app.help.book

import android.net.Uri
import io.legado.app.constant.BookSourceType
import io.legado.app.constant.BookType
import io.legado.app.data.entities.Book
import io.legado.app.data.entities.BookSource
import io.legado.app.exception.NoStackTraceException
import io.legado.app.help.config.AppConfig
import io.legado.app.utils.*
import splitties.init.appCtx
import java.io.File
import java.util.concurrent.ConcurrentHashMap


val Book.isAudio: Boolean
    get() {
        return type and BookType.audio > 0
    }

val Book.isImage: Boolean
    get() {
        return type and BookType.image > 0
    }

val Book.isLocal: Boolean
    get() {
        if (type == 0) {
            return origin == BookType.localTag || origin.startsWith(BookType.webDavTag)
        }
        return type and BookType.local > 0
    }

val Book.isLocalTxt: Boolean
    get() {
        return isLocal && originName.endsWith(".txt", true)
    }

val Book.isEpub: Boolean
    get() {
        return isLocal && originName.endsWith(".epub", true)
    }

val Book.isUmd: Boolean
    get() {
        return isLocal && originName.endsWith(".umd", true)
    }

val Book.isOnLineTxt: Boolean
    get() {
        return !isLocal && type and BookType.text > 0
    }

fun Book.contains(word: String?): Boolean {
    if (word.isNullOrEmpty()) {
        return true
    }
    return name.contains(word) || author.contains(word)
            || originName.contains(word) || origin.contains(word)
}

private val localUriCache by lazy {
    ConcurrentHashMap<String, Uri>()
}

fun Book.getLocalUri(): Uri {
    if (isLocal) {
        var uri = localUriCache[bookUrl]
        if (uri != null) {
            return uri
        }
        uri = if (bookUrl.isUri()) {
            Uri.parse(bookUrl)
        } else {
            Uri.fromFile(File(bookUrl))
        }
        //先检测uri是否有效,这个比较快
        uri.inputStream(appCtx).getOrNull()?.use {
            localUriCache[bookUrl] = uri
        }?.let {
            return uri
        }
        //不同的设备书籍保存路径可能不一样, uri无效时尝试寻找当前保存路径下的文件
        val defaultBookDir = AppConfig.defaultBookTreeUri
        if (defaultBookDir.isNullOrBlank()) {
            localUriCache[bookUrl] = uri
            return uri
        }
        val treeUri = Uri.parse(defaultBookDir)
        val treeFileDoc = FileDoc.fromUri(treeUri, true)
        val fileDoc = treeFileDoc.find(originName, 3)
        if (fileDoc != null) {
            localUriCache[bookUrl] = fileDoc.uri
            //更新bookUrl 重启不用再找一遍
            bookUrl = fileDoc.toString()
            save()
            return fileDoc.uri
        }
        localUriCache[bookUrl] = uri
        return uri
    }
    throw NoStackTraceException("不是本地书籍")
}

fun Book.cacheLocalUri(uri: Uri) {
    localUriCache[bookUrl] = uri
}

fun Book.removeLocalUriCache() {
    localUriCache.remove(bookUrl)
}

fun Book.getRemoteUrl(): String? {
    if (origin.startsWith(BookType.webDavTag)) {
        return origin.substring(8)
    }
    return null
}

fun Book.setType(@BookType.Type vararg types: Int) {
    type = 0
    addType(*types)
}

fun Book.addType(@BookType.Type vararg types: Int) {
    types.forEach {
        type = type or it
    }
}

fun Book.removeType(@BookType.Type vararg types: Int) {
    types.forEach {
        type = type and it.inv()
    }
}

fun Book.clearType() {
    type = 0
}

fun Book.upType() {
    if (type < 8) {
        type = when (type) {
            BookSourceType.image -> BookType.image
            BookSourceType.audio -> BookType.audio
            BookSourceType.file -> BookType.webFile
            else -> BookType.text
        }
        if (origin == "loc_book" || origin.startsWith(BookType.webDavTag)) {
            type = type or BookType.local
        }
    }
}

fun BookSource.getBookType(): Int {
    return when (bookSourceType) {
        BookSourceType.file -> BookType.text or BookType.webFile
        BookSourceType.image -> BookType.image
        BookSourceType.audio -> BookType.audio
        else -> BookType.text
    }
}
