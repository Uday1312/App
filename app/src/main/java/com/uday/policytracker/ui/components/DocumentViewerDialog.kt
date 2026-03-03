package com.uday.policytracker.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.uday.policytracker.util.copyAttachmentToUri
import com.uday.policytracker.util.resolveAttachmentUri
import com.uday.policytracker.util.shareAttachments
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

private data class PdfLoadResult(
    val pages: List<Bitmap> = emptyList(),
    val error: String? = null
)

@Composable
fun DocumentViewerDialog(
    rawUri: String,
    mimeType: String,
    displayName: String? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val resolvedUri = resolveAttachmentUri(context, rawUri)
    var pendingSourceForDownload by remember { mutableStateOf<Uri?>(null) }
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(mimeType.ifBlank { "*/*" })
    ) { targetUri ->
        val sourceUri = pendingSourceForDownload
        pendingSourceForDownload = null
        if (sourceUri == null || targetUri == null) return@rememberLauncherForActivityResult
        val success = copyAttachmentToUri(context, sourceUri, targetUri)
        Toast.makeText(
            context,
            if (success) "File saved successfully" else "Failed to save file",
            Toast.LENGTH_SHORT
        ).show()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            if (resolvedUri == null) {
                Text("Document not found", style = MaterialTheme.typography.titleMedium)
                return@Box
            }

            val isPdf = mimeType.contains("pdf", ignoreCase = true) || rawUri.endsWith(".pdf", ignoreCase = true)
            val isImage = mimeType.startsWith("image/", ignoreCase = true) ||
                rawUri.endsWith(".jpg", true) ||
                rawUri.endsWith(".jpeg", true) ||
                rawUri.endsWith(".png", true) ||
                rawUri.endsWith(".webp", true)

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        isImage -> ZoomableImage(uri = resolvedUri)
                        isPdf -> PdfPreview(uri = resolvedUri, rawUri = rawUri, context = context)
                        else -> Text(
                            "Preview not available for this type yet.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = {
                            shareAttachments(context, listOf(resolvedUri to mimeType))
                        }
                    ) { Text("Share") }
                    TextButton(
                        onClick = {
                            pendingSourceForDownload = resolvedUri
                            createDocumentLauncher.launch(
                                displayName ?: rawUri.substringAfterLast('/').ifBlank { "Document" }
                            )
                        }
                    ) { Text("Download") }
                    TextButton(onClick = onDismiss) { Text("Close") }
                }
            }
        }
    }
}

@Composable
private fun ZoomableImage(uri: Uri) {
    var zoom by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, gestureZoom, _ ->
                    val newZoom = (zoom * gestureZoom).coerceIn(1f, 6f)
                    zoom = newZoom
                    offsetX += pan.x
                    offsetY += pan.y
                }
            },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = uri,
            contentDescription = "Image document",
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = zoom,
                    scaleY = zoom,
                    translationX = offsetX,
                    translationY = offsetY
                ),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun PdfPreview(uri: Uri, rawUri: String, context: Context) {
    val loadResult by produceState(initialValue = PdfLoadResult(), uri, rawUri) {
        value = withContext(Dispatchers.IO) { renderPdfPages(context, uri, rawUri) }
    }

    when {
        loadResult.pages.isNotEmpty() -> PdfPageList(loadResult.pages)
        loadResult.error != null -> Text(loadResult.error ?: "Unable to open PDF", color = Color(0xFFE57373))
        else -> CircularProgressIndicator()
    }
}

@Composable
private fun PdfPageList(pages: List<Bitmap>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(pages) { index, page ->
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "Page ${index + 1}",
                    style = MaterialTheme.typography.labelLarge
                )
                ZoomableBitmapPage(bitmap = page)
            }
        }
    }
}

@Composable
private fun ZoomableBitmap(bitmap: Bitmap) {
    var zoom by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(bitmap) {
                detectTransformGestures { _, pan, gestureZoom, _ ->
                    val newZoom = (zoom * gestureZoom).coerceIn(1f, 6f)
                    zoom = newZoom
                    offsetX += pan.x
                    offsetY += pan.y
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "PDF preview",
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = zoom,
                    scaleY = zoom,
                    translationX = offsetX,
                    translationY = offsetY
                ),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun ZoomableBitmapPage(bitmap: Bitmap) {
    var zoom by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    val ratio = (bitmap.width.toFloat() / bitmap.height.toFloat()).coerceIn(0.5f, 2f)
    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        val newZoom = (zoom * zoomChange).coerceIn(1f, 6f)
        zoom = newZoom
        if (newZoom <= 1f) {
            offsetX = 0f
            offsetY = 0f
        } else {
            offsetX += panChange.x
            offsetY += panChange.y
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(ratio)
            .background(Color.White, RoundedCornerShape(12.dp))
            .transformable(state = transformState),
        contentAlignment = Alignment.Center
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "PDF page",
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = zoom,
                    scaleY = zoom,
                    translationX = if (zoom > 1f) offsetX else 0f,
                    translationY = if (zoom > 1f) offsetY else 0f
                ),
            contentScale = ContentScale.Fit
        )
    }
}

private fun renderPdfPages(context: Context, uri: Uri, rawUri: String): PdfLoadResult {
    val rendererResult = runCatching {
        openPdfDescriptor(context, uri, rawUri)?.use { pfd ->
            PdfRenderer(pfd).use { renderer ->
                if (renderer.pageCount == 0) {
                    return PdfLoadResult(error = "PDF has no pages")
                }
                val renderedPages = buildList {
                    for (index in 0 until renderer.pageCount) {
                        renderer.openPage(index).use { page ->
                            val width = (page.width * 2).coerceAtLeast(1)
                            val height = (page.height * 2).coerceAtLeast(1)
                            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                            Canvas(bitmap).drawColor(AndroidColor.WHITE)
                            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                            add(bitmap)
                        }
                    }
                }
                PdfLoadResult(pages = renderedPages)
            }
        } ?: PdfLoadResult(error = "Unable to read PDF file")
    }
    return rendererResult.getOrElse { PdfLoadResult(error = "Unable to render PDF") }
}

private fun openPdfDescriptor(context: Context, uri: Uri, rawUri: String): ParcelFileDescriptor? {
    return if (rawUri.startsWith("/")) {
        runCatching { ParcelFileDescriptor.open(File(rawUri), ParcelFileDescriptor.MODE_READ_ONLY) }.getOrNull()
            ?: runCatching { context.contentResolver.openFileDescriptor(uri, "r") }.getOrNull()
    } else {
        runCatching { context.contentResolver.openFileDescriptor(uri, "r") }.getOrNull()
    }
}
