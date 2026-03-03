package com.uday.policytracker.util

import android.content.Context
import android.content.Intent
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

fun resolveAttachmentUri(context: Context, raw: String): Uri? {
    if (raw.isBlank()) return null
    return when {
        raw.startsWith("content://") -> Uri.parse(raw)
        raw.startsWith("file://") -> Uri.parse(raw)
        raw.startsWith("/") -> {
            val file = File(raw)
            if (!file.exists()) null
            else FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        }
        else -> Uri.parse(raw)
    }
}

fun openAttachment(context: Context, raw: String, mimeType: String? = null): Boolean {
    val uri = resolveAttachmentUri(context, raw) ?: return false
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, mimeType ?: "*/*")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    return runCatching {
        context.startActivity(intent)
        true
    }.getOrDefault(false)
}

fun shareAttachments(context: Context, attachments: List<Pair<Uri, String>>) {
    if (attachments.isEmpty()) return
    val mime = if (attachments.size == 1) attachments.first().second else "*/*"
    val intent = if (attachments.size == 1) {
        Intent(Intent.ACTION_SEND).apply {
            type = mime
            putExtra(Intent.EXTRA_STREAM, attachments.first().first)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    } else {
        Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = mime
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(attachments.map { it.first }))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
    context.startActivity(Intent.createChooser(intent, "Share"))
}

fun downloadAttachmentToDownloads(
    context: Context,
    uri: Uri,
    displayName: String,
    mimeType: String
): Boolean {
    return runCatching {
        val input = context.contentResolver.openInputStream(uri) ?: return false
        input.use { stream ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, displayName)
                    put(MediaStore.Downloads.MIME_TYPE, mimeType)
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }
                val target = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                    ?: return false
                context.contentResolver.openOutputStream(target)?.use { out -> stream.copyTo(out) } ?: return false
                values.clear()
                values.put(MediaStore.Downloads.IS_PENDING, 0)
                context.contentResolver.update(target, values, null, null)
            } else {
                val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloads.exists()) downloads.mkdirs()
                val target = File(downloads, displayName)
                FileOutputStream(target).use { out -> stream.copyTo(out) }
            }
        }
        true
    }.getOrDefault(false)
}

fun copyAttachmentToUri(
    context: Context,
    sourceUri: Uri,
    targetUri: Uri
): Boolean {
    return runCatching {
        val input = context.contentResolver.openInputStream(sourceUri) ?: return false
        val output = context.contentResolver.openOutputStream(targetUri, "w") ?: return false
        input.use { inStream ->
            output.use { outStream ->
                inStream.copyTo(outStream)
            }
        }
        true
    }.getOrDefault(false)
}
