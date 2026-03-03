package com.uday.policytracker.ui.screens

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.documentfile.provider.DocumentFile
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.uday.policytracker.ui.components.DocumentViewerDialog
import com.uday.policytracker.util.copyAttachmentToUri
import com.uday.policytracker.util.formatEpochDay
import com.uday.policytracker.util.resolveAttachmentUri
import com.uday.policytracker.util.shareAttachments
import com.uday.policytracker.viewmodel.AttachmentUiModel
import com.uday.policytracker.viewmodel.PolicyViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

private enum class FolderFilterMode { ALL, YEAR, FOLDER_UPLOADS }
private enum class FolderDocSource { POLICY, FOLDER_UPLOAD }

private const val SUBFOLDER_PREFIX = "__sf__"
private const val SUBFOLDER_SEPARATOR = "__"

private data class FolderDocItem(
    val key: String,
    val attachment: AttachmentUiModel,
    val displayName: String,
    val subfolderName: String?,
    val year: Int,
    val source: FolderDocSource,
    val policyLabel: String? = null,
    val policyId: Long? = null,
    val policyStartEpochDay: Long? = null,
    val policyExpiryEpochDay: Long? = null
)

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FolderDetailScreen(
    paddingValues: PaddingValues,
    viewModel: PolicyViewModel,
    folderId: Long,
    onBack: () -> Unit,
    onOpenPolicy: (Long) -> Unit
) {
    val context = LocalContext.current
    val state by remember(folderId) { viewModel.observeFolderDetail(folderId) }.collectAsStateWithLifecycle()
    val folder = state.folder

    var filterMode by remember { mutableStateOf(FolderFilterMode.ALL) }
    var selectedYear by remember { mutableStateOf<Int?>(null) }
    var showYearMenu by remember { mutableStateOf(false) }
    var viewerAttachment by remember { mutableStateOf<AttachmentUiModel?>(null) }
    var selectionMode by remember { mutableStateOf(false) }
    val selectedKeys = remember { mutableStateListOf<String>() }
    var pendingDownloads by remember { mutableStateOf<List<FolderDocItem>>(emptyList()) }
    var awaitingDownloadPick by remember { mutableStateOf(false) }
    var actionDoc by remember { mutableStateOf<FolderDocItem?>(null) }
    var renameDoc by remember { mutableStateOf<FolderDocItem?>(null) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameText by remember { mutableStateOf("") }
    var deleteConfirmDoc by remember { mutableStateOf<FolderDocItem?>(null) }

    var selectedSubfolder by remember(folderId) { mutableStateOf<String?>(null) }
    var savedSubfolders by remember(folderId) { mutableStateOf(loadSubfolders(context, folderId)) }
    var showCreateSubfolderDialog by remember { mutableStateOf(false) }
    var newSubfolderName by remember { mutableStateOf("") }
    var createSubfolderError by remember { mutableStateOf(false) }
    var showUploadChoiceDialog by remember { mutableStateOf(false) }
    var subfolderSelectionMode by remember { mutableStateOf(false) }
    val selectedSubfolders = remember { mutableStateListOf<String>() }
    var subfolderActionTarget by remember { mutableStateOf<String?>(null) }
    var showRenameSubfolderDialog by remember { mutableStateOf(false) }
    var renameSubfolderTarget by remember { mutableStateOf<String?>(null) }
    var renameSubfolderText by remember { mutableStateOf("") }
    var renameSubfolderError by remember { mutableStateOf(false) }
    var deleteSubfolderTargets by remember { mutableStateOf<List<String>>(emptyList()) }

    val isContainerFolder = folder?.category?.isContainerOnlyService == true

    val filesPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (folder == null || uris.isEmpty()) return@rememberLauncherForActivityResult
        val targetSubfolder = if (isContainerFolder) selectedSubfolder else null
        uris.forEach { uri ->
            val rawName = resolveDisplayNameFromUri(context, uri)
            val encodedName = encodeSubfolderDisplayName(rawName, targetSubfolder)
            viewModel.addFolderAttachment(folder.id, uri, context.contentResolver, encodedName)
        }
        if (!targetSubfolder.isNullOrBlank() && targetSubfolder !in savedSubfolders) {
            savedSubfolders = (savedSubfolders + targetSubfolder).distinct().sorted()
            saveSubfolders(context, folder.id, savedSubfolders)
        }
        Toast.makeText(context, "Uploaded ${uris.size} file(s)", Toast.LENGTH_SHORT).show()
    }
    val folderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { treeUri ->
        if (treeUri != null && folder != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    treeUri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            val treeRoot = DocumentFile.fromTreeUri(context, treeUri)
            if (treeRoot == null || !treeRoot.isDirectory) {
                Toast.makeText(context, "Unable to read selected folder", Toast.LENGTH_SHORT).show()
                return@rememberLauncherForActivityResult
            }
            val files = collectAllFiles(treeRoot)
            if (files.isEmpty()) {
                Toast.makeText(context, "No files found in folder", Toast.LENGTH_SHORT).show()
                return@rememberLauncherForActivityResult
            }
            val baseSubfolder = if (isContainerFolder) {
                val pickedName = treeRoot.name?.trim().orEmpty()
                selectedSubfolder ?: pickedName.ifBlank { "Imported Folder" }
            } else {
                null
            }
            if (!baseSubfolder.isNullOrBlank() && baseSubfolder !in savedSubfolders) {
                savedSubfolders = (savedSubfolders + baseSubfolder).distinct().sorted()
                saveSubfolders(context, folder.id, savedSubfolders)
                selectedSubfolder = baseSubfolder
            }
            var imported = 0
            files.forEach { fileDoc ->
                val name = fileDoc.name?.ifBlank { "Document" } ?: "Document"
                val encodedName = encodeSubfolderDisplayName(name, baseSubfolder)
                viewModel.addFolderAttachment(folder.id, fileDoc.uri, context.contentResolver, encodedName)
                imported += 1
            }
            Toast.makeText(context, "Imported $imported file(s)", Toast.LENGTH_SHORT).show()
        }
    }

    val downloadPicker = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("*/*")) { targetUri ->
        awaitingDownloadPick = false
        val current = pendingDownloads.firstOrNull() ?: return@rememberLauncherForActivityResult
        if (targetUri == null) {
            pendingDownloads = pendingDownloads.drop(1)
            return@rememberLauncherForActivityResult
        }

        val sourceUri = resolveAttachmentUri(context, current.attachment.uri)
        val success = sourceUri?.let { copyAttachmentToUri(context, it, targetUri) } == true
        Toast.makeText(
            context,
            if (success) "File saved successfully" else "Failed to save file",
            Toast.LENGTH_SHORT
        ).show()

        pendingDownloads = pendingDownloads.drop(1)
    }

    LaunchedEffect(pendingDownloads, awaitingDownloadPick) {
        if (!awaitingDownloadPick && pendingDownloads.isNotEmpty()) {
            awaitingDownloadPick = true
            val first = pendingDownloads.first()
            downloadPicker.launch(first.displayName.ifBlank { "Document" })
        }
    }

    if (folder == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Folder") },
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { Text("Folder not found") }
        }
        return
    }

    val policyDocs = state.policyDocuments.map { doc ->
        FolderDocItem(
            key = "policy_${doc.attachmentId}",
            attachment = doc.attachment,
            displayName = doc.attachment.displayName,
            subfolderName = null,
            year = LocalDate.ofEpochDay(doc.policyStartEpochDay).year,
            source = FolderDocSource.POLICY,
            policyLabel = "${doc.policyName} • ${doc.policyNumber}",
            policyId = doc.policyId,
            policyStartEpochDay = doc.policyStartEpochDay,
            policyExpiryEpochDay = doc.policyExpiryEpochDay
        )
    }

    val uploadedDocs = folder.attachments.map { file ->
        val (decodedName, subfolder) = decodeSubfolderDisplayName(file.displayName)
        val year = Instant.ofEpochMilli(file.addedAtEpochMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .year
        FolderDocItem(
            key = "upload_${file.id}",
            attachment = file,
            displayName = decodedName,
            subfolderName = subfolder,
            year = year,
            source = FolderDocSource.FOLDER_UPLOAD
        )
    }

    val effectiveSubfolders = (savedSubfolders + uploadedDocs.mapNotNull { it.subfolderName }).distinct().sorted()

    fun renameSubfolder(oldName: String, newName: String): Boolean {
        val clean = newName.trim()
        if (clean.isBlank() || clean == oldName || clean in effectiveSubfolders) return false
        uploadedDocs.filter { it.subfolderName == oldName }.forEach { doc ->
            val encoded = encodeSubfolderDisplayName(doc.displayName, clean)
            viewModel.renameFolderAttachment(doc.attachment.id, encoded)
        }
        val updated = savedSubfolders.map { if (it == oldName) clean else it }.distinct().sorted()
        savedSubfolders = updated
        saveSubfolders(context, folder.id, updated)
        if (selectedSubfolder == oldName) selectedSubfolder = clean
        if (oldName in selectedSubfolders) {
            selectedSubfolders.remove(oldName)
            selectedSubfolders.add(clean)
        }
        return true
    }

    fun deleteSubfolders(names: List<String>) {
        if (names.isEmpty()) return
        uploadedDocs.filter { it.subfolderName in names }.forEach { doc ->
            viewModel.deleteFolderAttachment(doc.attachment.id)
        }
        val updated = savedSubfolders.filterNot { it in names }.distinct().sorted()
        savedSubfolders = updated
        saveSubfolders(context, folder.id, updated)
        if (selectedSubfolder != null && selectedSubfolder in names) selectedSubfolder = null
        selectedSubfolders.removeAll(names.toSet())
        subfolderSelectionMode = false
    }

    val allDocs = (policyDocs + uploadedDocs).sortedByDescending { it.attachment.addedAtEpochMillis }
    val years = allDocs.map { it.year }.distinct().sortedDescending()

    val subfolderFilteredDocs = if (isContainerFolder && !selectedSubfolder.isNullOrBlank()) {
        val target = selectedSubfolder ?: ""
        allDocs.filter { (it.subfolderName ?: "") == target }
    } else {
        allDocs
    }

    val filteredDocs = when (filterMode) {
        FolderFilterMode.ALL -> subfolderFilteredDocs
        FolderFilterMode.YEAR -> subfolderFilteredDocs.filter { selectedYear == null || it.year == selectedYear }
        FolderFilterMode.FOLDER_UPLOADS -> subfolderFilteredDocs.filter { it.source == FolderDocSource.FOLDER_UPLOAD }
    }

    val grouped = filteredDocs.groupBy { it.year }.toSortedMap(compareByDescending { it })
    val selectedItems = allDocs.filter { it.key in selectedKeys }

    fun shareItems(items: List<FolderDocItem>) {
        val resolved = items.mapNotNull {
            resolveAttachmentUri(context, it.attachment.uri)?.let { uri -> uri to it.attachment.mimeType }
        }
        shareAttachments(context, resolved)
    }

    fun downloadItems(items: List<FolderDocItem>) {
        if (items.isEmpty()) return
        awaitingDownloadPick = false
        pendingDownloads = items
        if (items.size > 1) {
            Toast.makeText(context, "Choose save location for each file", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteItems(items: List<FolderDocItem>) {
        items.forEach { item ->
            when (item.source) {
                FolderDocSource.POLICY -> viewModel.deleteAttachment(item.attachment.id)
                FolderDocSource.FOLDER_UPLOAD -> viewModel.deleteFolderAttachment(item.attachment.id)
            }
        }
        val keys = items.map { it.key }.toSet()
        selectedKeys.removeAll { it in keys }
        Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
    }

    fun renameItem(item: FolderDocItem, newName: String) {
        val updated = when (item.source) {
            FolderDocSource.POLICY -> viewModel.renameAttachment(item.attachment.id, newName)
            FolderDocSource.FOLDER_UPLOAD -> {
                val encoded = encodeSubfolderDisplayName(newName, item.subfolderName)
                viewModel.renameFolderAttachment(item.attachment.id, encoded)
            }
        }
        Toast.makeText(
            context,
            if (updated) "Renamed successfully" else "Enter a valid file name",
            Toast.LENGTH_SHORT
        ).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(folder.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    if (selectionMode) {
                        Text("${selectedKeys.size}")
                        IconButton(onClick = { if (selectedItems.isNotEmpty()) shareItems(selectedItems) }) {
                            Icon(Icons.Default.Share, contentDescription = "Share selected")
                        }
                        IconButton(onClick = { if (selectedItems.isNotEmpty()) downloadItems(selectedItems) }) {
                            Icon(Icons.Default.Download, contentDescription = "Download selected")
                        }
                        IconButton(onClick = { if (selectedItems.isNotEmpty()) deleteItems(selectedItems) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete selected")
                        }
                        IconButton(onClick = {
                            selectionMode = false
                            selectedKeys.clear()
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Close selection")
                        }
                    } else {
                        IconButton(onClick = { selectionMode = true }) {
                            Icon(Icons.Default.CheckBoxOutlineBlank, contentDescription = "Select multiple")
                        }
                    }
                    TextButton(onClick = { showUploadChoiceDialog = true }) {
                        Icon(Icons.Default.AttachFile, contentDescription = "Upload file")
                        Text(" Upload")
                    }
                    if (isContainerFolder) {
                        IconButton(onClick = { showCreateSubfolderDialog = true }) {
                            Icon(Icons.Default.CreateNewFolder, contentDescription = "Create subfolder")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Tagged policies: ${folder.policies.size}", fontWeight = FontWeight.SemiBold)
                        Text("Documents: ${allDocs.size}", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            if (isContainerFolder) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Subfolders", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (subfolderSelectionMode) {
                                Text("${selectedSubfolders.size}")
                                IconButton(onClick = {
                                    if (selectedSubfolders.isNotEmpty()) {
                                        deleteSubfolderTargets = selectedSubfolders.toList()
                                    }
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete selected subfolders")
                                }
                                IconButton(onClick = {
                                    subfolderSelectionMode = false
                                    selectedSubfolders.clear()
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close subfolder selection")
                                }
                            } else {
                                IconButton(onClick = { subfolderSelectionMode = true }) {
                                    Icon(Icons.Default.CheckBoxOutlineBlank, contentDescription = "Select subfolders")
                                }
                            }
                        }
                    }
                }
                items(effectiveSubfolders, key = { it }) { sub ->
                    val selected = sub in selectedSubfolders
                    val subFiles = uploadedDocs.count { it.subfolderName == sub }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = {
                                    if (subfolderSelectionMode) {
                                        if (selected) selectedSubfolders.remove(sub) else selectedSubfolders.add(sub)
                                    } else {
                                        selectedSubfolder = if (selectedSubfolder == sub) null else sub
                                    }
                                },
                                onLongClick = {
                                    if (!subfolderSelectionMode) subfolderActionTarget = sub
                                }
                            ),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Folder, contentDescription = null)
                                Text(sub, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            }
                            Text("Tagged policies: 0")
                            Text("Files: $subFiles")
                        }
                    }
                }
                if (effectiveSubfolders.isEmpty()) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                            Text("No subfolders yet. Tap + to create one.", modifier = Modifier.padding(14.dp))
                        }
                    }
                }
            }

            item { Text("Filter", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    AssistChip(
                        onClick = {
                            filterMode = FolderFilterMode.ALL
                            selectedYear = null
                        },
                        label = { Text("All") },
                        leadingIcon = { if (filterMode == FolderFilterMode.ALL) Icon(Icons.Default.Check, contentDescription = null) }
                    )
                    Box {
                        AssistChip(
                            onClick = { showYearMenu = true },
                            label = { Text(selectedYear?.toString() ?: "All years") },
                            leadingIcon = {
                                if (filterMode == FolderFilterMode.YEAR) Icon(Icons.Default.Check, contentDescription = null)
                            }
                        )
                        DropdownMenu(expanded = showYearMenu, onDismissRequest = { showYearMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("All years") },
                                onClick = {
                                    selectedYear = null
                                    filterMode = FolderFilterMode.YEAR
                                    showYearMenu = false
                                }
                            )
                            years.forEach { year ->
                                DropdownMenuItem(
                                    text = { Text(year.toString()) },
                                    onClick = {
                                        selectedYear = year
                                        filterMode = FolderFilterMode.YEAR
                                        showYearMenu = false
                                    }
                                )
                            }
                        }
                    }
                    AssistChip(
                        onClick = { filterMode = FolderFilterMode.FOLDER_UPLOADS },
                        label = { Text("Folder Uploads") },
                        leadingIcon = {
                            if (filterMode == FolderFilterMode.FOLDER_UPLOADS) Icon(Icons.Default.Check, contentDescription = null)
                        }
                    )
                }
            }

            if (filteredDocs.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                        Text(
                            "No documents for this filter.",
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            } else {
                grouped.forEach { (year, yearDocs) ->
                    item {
                        Text(year.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }
                    items(yearDocs, key = { it.key }) { doc ->
                        val selected = doc.key in selectedKeys
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = {
                                        if (selectionMode) {
                                            if (selected) selectedKeys.remove(doc.key) else selectedKeys.add(doc.key)
                                        } else {
                                            viewerAttachment = doc.attachment.copy(displayName = doc.displayName)
                                        }
                                    },
                                    onLongClick = {
                                        if (!selectionMode) actionDoc = doc
                                    }
                                ),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(doc.displayName, fontWeight = FontWeight.SemiBold)
                                        if (doc.source == FolderDocSource.POLICY) {
                                            Text(doc.policyLabel.orEmpty(), style = MaterialTheme.typography.bodySmall)
                                        } else {
                                            Text(
                                                if (doc.subfolderName.isNullOrBlank()) "Folder upload" else "Folder upload • ${doc.subfolderName}",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                    if (selected) {
                                        Icon(
                                            Icons.Default.CheckBox,
                                            contentDescription = "Selected",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Row {
                                        IconButton(onClick = { shareItems(listOf(doc)) }) {
                                            Icon(Icons.Default.Share, contentDescription = "Share")
                                        }
                                        IconButton(onClick = { downloadItems(listOf(doc)) }) {
                                            Icon(Icons.Default.Download, contentDescription = "Download")
                                        }
                                    }
                                }
                                if (doc.source == FolderDocSource.POLICY) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Start: ${formatEpochDay(doc.policyStartEpochDay ?: 0)}", style = MaterialTheme.typography.bodySmall)
                                        Text("Expiry: ${formatEpochDay(doc.policyExpiryEpochDay ?: 0)}", style = MaterialTheme.typography.bodySmall)
                                    }
                                    doc.policyId?.let { policyId ->
                                        Text(
                                            "Open policy",
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.clickable { onOpenPolicy(policyId) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    viewerAttachment?.let { attachment ->
        DocumentViewerDialog(
            rawUri = attachment.uri,
            mimeType = attachment.mimeType,
            displayName = attachment.displayName,
            onDismiss = { viewerAttachment = null }
        )
    }

    actionDoc?.let { doc ->
        AlertDialog(
            onDismissRequest = { actionDoc = null },
            title = { Text("Document options") },
            text = { Text(doc.displayName) },
            confirmButton = {
                Row {
                    TextButton(
                        onClick = {
                            renameText = doc.displayName
                            renameDoc = doc
                            showRenameDialog = true
                            actionDoc = null
                        }
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Text(" Rename")
                    }
                    TextButton(
                        onClick = {
                            deleteConfirmDoc = doc
                            actionDoc = null
                        }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Text(" Delete")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { actionDoc = null }) { Text("Cancel") }
            }
        )
    }

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename document") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true,
                    label = { Text("File name") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        renameDoc?.let { renameItem(it, renameText) }
                        renameDoc = null
                        showRenameDialog = false
                    }
                ) {
                    Icon(Icons.Default.Done, contentDescription = null)
                    Text(" Save")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    renameDoc = null
                    showRenameDialog = false
                }) { Text("Cancel") }
            }
        )
    }

    deleteConfirmDoc?.let { doc ->
        AlertDialog(
            onDismissRequest = { deleteConfirmDoc = null },
            title = { Text("Delete document?") },
            text = { Text("This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    deleteItems(listOf(doc))
                    deleteConfirmDoc = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmDoc = null }) { Text("Cancel") }
            }
        )
    }

    if (showCreateSubfolderDialog && isContainerFolder) {
        AlertDialog(
            onDismissRequest = { showCreateSubfolderDialog = false },
            title = { Text("Create subfolder") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newSubfolderName,
                        onValueChange = { newSubfolderName = it },
                        label = { Text("Subfolder name") },
                        singleLine = true
                    )
                    if (createSubfolderError) {
                        Text("Enter a valid unique subfolder name.", color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val name = newSubfolderName.trim()
                    if (name.isBlank() || name in effectiveSubfolders) {
                        createSubfolderError = true
                        return@TextButton
                    }
                    val updated = (savedSubfolders + name).distinct().sorted()
                    savedSubfolders = updated
                    saveSubfolders(context, folder.id, updated)
                    selectedSubfolder = name
                    newSubfolderName = ""
                    createSubfolderError = false
                    showCreateSubfolderDialog = false
                }) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateSubfolderDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showUploadChoiceDialog) {
        AlertDialog(
            onDismissRequest = { showUploadChoiceDialog = false },
            title = { Text("Upload") },
            text = { Text("Choose what to upload") },
            confirmButton = {
                TextButton(onClick = {
                    showUploadChoiceDialog = false
                    filesPicker.launch(arrayOf("*/*"))
                }) { Text("Upload Files") }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        showUploadChoiceDialog = false
                        folderPicker.launch(null)
                    }) { Text("Upload Folder") }
                    TextButton(onClick = { showUploadChoiceDialog = false }) { Text("Cancel") }
                }
            }
        )
    }

    subfolderActionTarget?.let { sub ->
        AlertDialog(
            onDismissRequest = { subfolderActionTarget = null },
            title = { Text("Subfolder options") },
            text = { Text(sub) },
            confirmButton = {
                Row {
                    TextButton(onClick = {
                        renameSubfolderTarget = sub
                        renameSubfolderText = sub
                        renameSubfolderError = false
                        showRenameSubfolderDialog = true
                        subfolderActionTarget = null
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Text(" Rename")
                    }
                    TextButton(onClick = {
                        deleteSubfolderTargets = listOf(sub)
                        subfolderActionTarget = null
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Text(" Delete")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { subfolderActionTarget = null }) { Text("Cancel") }
            }
        )
    }

    if (showRenameSubfolderDialog) {
        AlertDialog(
            onDismissRequest = { showRenameSubfolderDialog = false },
            title = { Text("Rename subfolder") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = renameSubfolderText,
                        onValueChange = { renameSubfolderText = it },
                        label = { Text("Subfolder name") },
                        singleLine = true
                    )
                    if (renameSubfolderError) {
                        Text("Enter a unique valid name.", color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val ok = renameSubfolder(renameSubfolderTarget.orEmpty(), renameSubfolderText)
                    if (ok) {
                        showRenameSubfolderDialog = false
                        renameSubfolderTarget = null
                        renameSubfolderError = false
                    } else {
                        renameSubfolderError = true
                    }
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRenameSubfolderDialog = false
                    renameSubfolderTarget = null
                }) { Text("Cancel") }
            }
        )
    }

    if (deleteSubfolderTargets.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { deleteSubfolderTargets = emptyList() },
            title = { Text("Delete subfolder(s)?") },
            text = { Text("Documents inside selected subfolder(s) will also be deleted.") },
            confirmButton = {
                TextButton(onClick = {
                    deleteSubfolders(deleteSubfolderTargets)
                    deleteSubfolderTargets = emptyList()
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { deleteSubfolderTargets = emptyList() }) { Text("Cancel") }
            }
        )
    }
}

private fun encodeSubfolderDisplayName(displayName: String, subfolderName: String?): String {
    val cleanName = displayName.ifBlank { "Document" }
    val folder = subfolderName?.trim().orEmpty()
    return if (folder.isBlank()) cleanName else "$SUBFOLDER_PREFIX$folder$SUBFOLDER_SEPARATOR$cleanName"
}

private fun decodeSubfolderDisplayName(displayName: String): Pair<String, String?> {
    if (!displayName.startsWith(SUBFOLDER_PREFIX)) return displayName to null
    val rest = displayName.removePrefix(SUBFOLDER_PREFIX)
    val idx = rest.indexOf(SUBFOLDER_SEPARATOR)
    if (idx <= 0 || idx >= rest.length - SUBFOLDER_SEPARATOR.length) return displayName to null
    val folder = rest.substring(0, idx)
    val name = rest.substring(idx + SUBFOLDER_SEPARATOR.length)
    return name to folder
}

private fun resolveDisplayNameFromUri(context: Context, uri: Uri): String {
    return runCatching {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIdx >= 0 && cursor.moveToFirst()) cursor.getString(nameIdx) else null
        } ?: "Document"
    }.getOrDefault("Document")
}

private fun loadSubfolders(context: Context, folderId: Long): List<String> {
    val prefs = context.getSharedPreferences("folder_subfolders", Context.MODE_PRIVATE)
    val raw = prefs.getString("folder_$folderId", "").orEmpty()
    return raw.split("||").map { it.trim() }.filter { it.isNotBlank() }.distinct().sorted()
}

private fun saveSubfolders(context: Context, folderId: Long, subfolders: List<String>) {
    context.getSharedPreferences("folder_subfolders", Context.MODE_PRIVATE)
        .edit()
        .putString("folder_$folderId", subfolders.joinToString("||"))
        .apply()
}

private fun collectAllFiles(root: DocumentFile): List<DocumentFile> {
    if (root.isFile) return listOf(root)
    if (!root.isDirectory) return emptyList()
    return root.listFiles().flatMap { child ->
        when {
            child.isFile -> listOf(child)
            child.isDirectory -> collectAllFiles(child)
            else -> emptyList()
        }
    }
}
