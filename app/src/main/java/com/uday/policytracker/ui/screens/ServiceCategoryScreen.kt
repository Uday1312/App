package com.uday.policytracker.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.uday.policytracker.data.model.PolicyCategory
import com.uday.policytracker.viewmodel.PolicyViewModel
import com.uday.policytracker.viewmodel.FolderUiModel
import com.uday.policytracker.viewmodel.defaultFolderInput

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ServiceCategoryScreen(
    paddingValues: PaddingValues,
    viewModel: PolicyViewModel,
    category: PolicyCategory,
    onBack: () -> Unit,
    onOpenFolder: (Long) -> Unit
) {
    val state by remember(category) { viewModel.observeCategoryFolders(category) }.collectAsStateWithLifecycle()
    val defaultFolderColor = Color(0xFF6D747D)
    var showCreateDialog by remember(category) { mutableStateOf(false) }
    var folderInput by remember(category) { mutableStateOf(defaultFolderInput()) }
    var folderError by remember(category) { mutableStateOf(false) }
    var selectedFolder by remember(category) { mutableStateOf<FolderUiModel?>(null) }
    var showFolderActions by remember(category) { mutableStateOf(false) }
    var showRenameDialog by remember(category) { mutableStateOf(false) }
    var showDeleteDialog by remember(category) { mutableStateOf(false) }
    var renameInput by remember(category) { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(category.label) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (category != PolicyCategory.FINANCIALS) {
                        IconButton(onClick = { showCreateDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Create folder")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (category == PolicyCategory.FINANCIALS) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Spacer(modifier = Modifier.height(14.dp))
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Financials module", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text("This section is reserved for upcoming financial tracking features.")
                    }
                }
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            if (state.folders.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            "No folders yet. Tap + to create a folder.",
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }

            items(state.folders, key = { it.id }) { folder ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = { onOpenFolder(folder.id) },
                            onLongClick = {
                                selectedFolder = folder
                                renameInput = folder.name
                                showFolderActions = true
                            }
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = defaultFolderColor)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Folder, contentDescription = null)
                            Text(folder.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }
                        Text("Tagged policies: ${folder.policies.size}")
                        if (category.isContainerOnlyService) {
                            Text("Files: ${folder.attachments.size}")
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }

    if (showCreateDialog && category != PolicyCategory.FINANCIALS) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create folder") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = folderInput.name,
                        onValueChange = { folderInput = folderInput.copy(name = it) },
                        label = { Text("Folder name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (folderError) {
                        Text("Enter valid folder details.", color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val created = viewModel.createFolder(category, folderInput)
                    if (created) {
                        folderInput = defaultFolderInput()
                        folderError = false
                        showCreateDialog = false
                    } else {
                        folderError = true
                    }
                }) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showFolderActions && selectedFolder != null) {
        val target = selectedFolder ?: return@ServiceCategoryScreen
        AlertDialog(
            onDismissRequest = { showFolderActions = false },
            title = { Text("Folder Options") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            showFolderActions = false
                            showRenameDialog = true
                        }
                    ) {
                        Text("Rename Folder")
                    }
                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            showFolderActions = false
                            showDeleteDialog = true
                        }
                    ) {
                        Text("Delete Folder", color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFolderActions = false }) { Text("Close") }
            },
            dismissButton = {}
        )
    }

    if (showRenameDialog && selectedFolder != null) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Folder") },
            text = {
                OutlinedTextField(
                    value = renameInput,
                    onValueChange = { renameInput = it },
                    label = { Text("Folder name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val ok = viewModel.renameFolder(selectedFolder!!.id, renameInput)
                    if (ok) showRenameDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showDeleteDialog && selectedFolder != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Folder") },
            text = { Text("Delete ${selectedFolder!!.name}? This also removes its tag.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteFolder(selectedFolder!!.id)
                    showDeleteDialog = false
                    selectedFolder = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}
