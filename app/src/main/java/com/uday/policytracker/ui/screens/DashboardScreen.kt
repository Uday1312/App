package com.uday.policytracker.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.uday.policytracker.data.model.PolicyCategory
import com.uday.policytracker.data.model.PolicyStatus
import com.uday.policytracker.util.epochDayToMillis
import com.uday.policytracker.util.formatEpochDay
import com.uday.policytracker.util.formatTimeLeft
import com.uday.policytracker.util.millisToUiDate
import com.uday.policytracker.util.parseFlexibleDate
import com.uday.policytracker.viewmodel.DashboardUiState
import com.uday.policytracker.viewmodel.PolicyUiModel
import com.uday.policytracker.viewmodel.RenewalInput
import com.uday.policytracker.viewmodel.SearchResultUiModel

@Composable
fun DashboardScreen(
    paddingValues: PaddingValues,
    uiState: DashboardUiState,
    onCategorySelected: (PolicyCategory?) -> Unit,
    onOpenServiceCategory: (PolicyCategory) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onAddPolicy: () -> Unit,
    onBackup: () -> Unit,
    onRestore: () -> Unit,
    onOpenPolicy: (Long) -> Unit,
    onOpenFolder: (Long) -> Unit,
    onOpenClosedPolicies: () -> Unit,
    onRenewPolicy: (Long, RenewalInput, List<Uri>, (Boolean) -> Unit) -> Unit,
    onDeletePolicy: (PolicyUiModel) -> Unit,
    onClosePolicy: (Long) -> Unit
) {
    var renewalTarget by remember { mutableStateOf<PolicyUiModel?>(null) }
    var deleteTarget by remember { mutableStateOf<PolicyUiModel?>(null) }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            item {
                TopHeader(uiState, onSearchQueryChanged, onAddPolicy, onBackup, onRestore)
            }

            if (uiState.searchQuery.isNotBlank()) {
                item {
                    Text(
                        "Search Results",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(uiState.searchResults) { result ->
                    SearchResultTile(result, onOpenPolicy, onOpenFolder)
                }
                if (uiState.searchResults.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("No matching policy or folder found.", modifier = Modifier.padding(14.dp))
                        }
                    }
                }
            }

            item {
                ServiceGrid { category ->
                    when {
                        category == PolicyCategory.FINANCIALS -> onOpenServiceCategory(category)
                        category.supportsPolicyRecords -> onCategorySelected(category)
                        else -> onCategorySelected(null)
                    }
                }
            }
            item {
                Text(
                    "Policies",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            item { CategoryTabs(uiState, onCategorySelected) }

            items(uiState.policies, key = { it.id }) { policy ->
                PolicyTile(
                    model = policy,
                    onClick = { onOpenPolicy(policy.id) },
                    onMarkRenewed = { renewalTarget = policy },
                    onClosePolicy = { onClosePolicy(policy.id) },
                    onLongPressDelete = { deleteTarget = policy }
                )
            }

            if (uiState.closedPolicies.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth()
                            .clickable { onOpenClosedPolicies() },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(
                            "Closed Policies (${uiState.closedPolicies.size})",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            if (uiState.policies.isEmpty()) {
                item { EmptyState(onAddPolicy) }
            }

            item {
                Text(
                    "Policy Management",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            item {
                PolicyManagementTabs(onOpenServiceCategory = onOpenServiceCategory)
            }
        }
    }

    renewalTarget?.let { target ->
        RenewalDialog(
            policy = target,
            onDismiss = { renewalTarget = null },
            onSubmit = { input, attachments ->
                onRenewPolicy(target.id, input, attachments) { success ->
                    if (success) renewalTarget = null
                }
            }
        )
    }

    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete policy") },
            text = { Text("Do you want to delete ${target.policyName}?") },
            confirmButton = {
                TextButton(onClick = {
                    onDeletePolicy(target)
                    deleteTarget = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun TopHeader(
    uiState: DashboardUiState,
    onSearchQueryChanged: (String) -> Unit,
    onAddPolicy: () -> Unit,
    onBackup: () -> Unit,
    onRestore: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(listOf(Color(0xFF176B5E), Color(0xFF1C8474))),
                shape = RoundedCornerShape(bottomStart = 22.dp, bottomEnd = 22.dp)
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Hi, Uday", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Welcome back", color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.bodyLarge)
            }
            TopMenuAction(onBackup = onBackup, onRestore = onRestore)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChanged,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search all policies and folders") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (uiState.searchQuery.isNotBlank()) {
                        IconButton(onClick = { onSearchQueryChanged("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search")
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp)
            )
        }

        Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF4F1))) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ElevatedButton(onClick = onAddPolicy) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Policy")
                }
                Text("Total: ${uiState.totalRecords}", color = Color(0xFF2F5953), style = MaterialTheme.typography.bodyMedium)
                Text("Expiring: ${uiState.totalExpiringSoon}", color = Color(0xFF2F5953), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun SearchResultTile(
    result: SearchResultUiModel,
    onOpenPolicy: (Long) -> Unit,
    onOpenFolder: (Long) -> Unit
) {
    val (title, subtitle, click) = when (result) {
        is SearchResultUiModel.PolicyResult -> Triple(
            result.title,
            "${result.subtitle} • ${result.categoryLabel}",
            { onOpenPolicy(result.policyId) }
        )

        is SearchResultUiModel.FolderResult -> Triple(
            result.title,
            "Folder • ${result.subtitle}",
            { onOpenFolder(result.folderId) }
        )
    }

    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clickable(onClick = click),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RenewalDialog(
    policy: PolicyUiModel,
    onDismiss: () -> Unit,
    onSubmit: (RenewalInput, List<Uri>) -> Unit
) {
    val attachmentUris = remember(policy.id) { mutableStateListOf<Uri>() }
    var policyName by remember(policy.id) { mutableStateOf(policy.policyName) }
    var policyHolderName by remember(policy.id) { mutableStateOf(policy.policyHolderName) }
    var policyNumber by remember(policy.id) { mutableStateOf(policy.policyNumber) }
    var startDate by remember(policy.id) { mutableStateOf(formatEpochDay(policy.startDateEpochDay)) }
    var expiryDate by remember(policy.id) { mutableStateOf(formatEpochDay(policy.expiryDateEpochDay)) }
    var insurerName by remember(policy.id) { mutableStateOf(policy.insurerName) }
    var premium by remember(policy.id) { mutableStateOf(policy.premiumAmount.toString()) }
    var showStartPicker by remember(policy.id) { mutableStateOf(false) }
    var showExpiryPicker by remember(policy.id) { mutableStateOf(false) }
    val attachmentPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) attachmentUris.add(uri)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add renewal follow-up") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = policyName, onValueChange = { policyName = it }, label = { Text("Policy name") }, singleLine = true)
                OutlinedTextField(
                    value = policyHolderName,
                    onValueChange = { policyHolderName = it },
                    label = { Text("Policy holder name") },
                    singleLine = true
                )
                OutlinedTextField(value = policyNumber, onValueChange = { policyNumber = it }, label = { Text("Policy number") }, singleLine = true)
                OutlinedTextField(
                    value = startDate,
                    onValueChange = { startDate = it },
                    label = { Text("Start (dd-MMM-yyyy)") },
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = { showStartPicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Pick start date")
                        }
                    }
                )
                OutlinedTextField(
                    value = expiryDate,
                    onValueChange = { expiryDate = it },
                    label = { Text("Expiry (dd-MMM-yyyy)") },
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = { showExpiryPicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Pick expiry date")
                        }
                    }
                )
                OutlinedTextField(value = insurerName, onValueChange = { insurerName = it }, label = { Text("Insurer") }, singleLine = true)
                OutlinedTextField(
                    value = premium,
                    onValueChange = { premium = it },
                    label = { Text("Premium") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                Button(onClick = { attachmentPicker.launch(arrayOf("*/*")) }) {
                    Text("Attach document (optional)")
                }
                if (attachmentUris.isNotEmpty()) {
                    Text("Selected: ${attachmentUris.size} file(s)")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSubmit(
                    RenewalInput(
                        policyHolderName = policyHolderName,
                        policyName = policyName,
                        policyNumber = policyNumber,
                        startDate = startDate,
                        expiryDate = expiryDate,
                        insurerName = insurerName,
                        premiumAmount = premium
                    ),
                    attachmentUris.toList()
                )
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )

    if (showStartPicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = parseFlexibleDate(startDate)?.let { epochDayToMillis(it) })
        DatePickerDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { startDate = millisToUiDate(it) }
                    showStartPicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showStartPicker = false }) { Text("Cancel") } }
        ) {
            DatePicker(state = state)
        }
    }

    if (showExpiryPicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = parseFlexibleDate(expiryDate)?.let { epochDayToMillis(it) })
        DatePickerDialog(
            onDismissRequest = { showExpiryPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { expiryDate = millisToUiDate(it) }
                    showExpiryPicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showExpiryPicker = false }) { Text("Cancel") } }
        ) {
            DatePicker(state = state)
        }
    }
}

@Composable
private fun CircleAction(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = Color.White)
    }
}

@Composable
private fun TopMenuAction(onBackup: () -> Unit, onRestore: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            CircleAction(Icons.Default.MoreVert)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("Back Up") }, onClick = {
                expanded = false
                onBackup()
            })
            DropdownMenuItem(text = { Text("Restore") }, onClick = {
                expanded = false
                onRestore()
            })
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ServiceGrid(onOpenServiceCategory: (PolicyCategory) -> Unit) {
    val pageOne = listOf(
        Triple("Health Insurance", Icons.Default.HealthAndSafety, PolicyCategory.HEALTH_INSURANCE),
        Triple("Term Insurance", Icons.Default.FavoriteBorder, PolicyCategory.TERM_INSURANCE),
        Triple("Vehicle Insurance", Icons.Default.CreditCard, PolicyCategory.VEHICLE_INSURANCE),
        Triple("RTA", Icons.Default.Description, PolicyCategory.DRIVING_LICENCE),
        Triple("Medical Records", Icons.Default.Shield, PolicyCategory.MEDICAL_RECORDS),
        Triple("Financials", Icons.Default.AccountBalance, PolicyCategory.FINANCIALS)
    )
    val pageTwo = listOf(
        Triple("Banking", Icons.Default.AccountBalanceWallet, PolicyCategory.BANKING),
        Triple("Identity", Icons.Default.Badge, PolicyCategory.IDENTITY),
        Triple("Education", Icons.Default.School, PolicyCategory.EDUCATION),
        Triple("General", Icons.Default.Folder, PolicyCategory.GENERAL)
    )
    val pages = remember { listOf(pageOne, pageTwo) }
    val pagerState = rememberPagerState(pageCount = { pages.size })

    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Service", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        HorizontalPager(
            state = pagerState,
            pageSize = PageSize.Fill,
            pageSpacing = 10.dp,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val services = pages[page]
            val rows = services.chunked(2)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                repeat(3) { rowIndex ->
                    val rowItems = rows.getOrElse(rowIndex) { emptyList() }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        rowItems.forEach { (title, icon, category) ->
                            ServiceTile(Modifier.weight(1f), title, icon) { onOpenServiceCategory(category) }
                        }
                        when (rowItems.size) {
                            0 -> {
                                Spacer(modifier = Modifier.weight(1f).height(130.dp))
                                Spacer(modifier = Modifier.weight(1f).height(130.dp))
                            }
                            1 -> Spacer(modifier = Modifier.weight(1f).height(130.dp))
                        }
                    }
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pages.size) { index ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (pagerState.currentPage == index) 9.dp else 7.dp)
                        .clip(CircleShape)
                        .background(
                            if (pagerState.currentPage == index) Color(0xFF1A6A5E)
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                )
            }
        }
    }
}

@Composable
private fun ServiceTile(modifier: Modifier, title: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .height(130.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 20.dp, horizontal = 12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEEF5F3)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color(0xFF1A6A5E))
            }
            Text(title, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun CategoryTabs(uiState: DashboardUiState, onCategorySelected: (PolicyCategory?) -> Unit) {
    val policyManagementCategories = remember {
        PolicyCategory.entries.filter { it.supportsPolicyRecords }
    }
    val totalCount = policyManagementCategories.sumOf { uiState.counts[it] ?: 0 }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AssistChip(
            onClick = { onCategorySelected(null) },
            label = { Text("All ($totalCount)") },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = if (uiState.selectedCategory == null) Color(0xFF1A6A5E) else MaterialTheme.colorScheme.surface,
                labelColor = if (uiState.selectedCategory == null) Color.White else MaterialTheme.colorScheme.onSurface
            )
        )
        policyManagementCategories.forEach { category ->
            AssistChip(
                onClick = { onCategorySelected(category) },
                label = { Text("${category.label} (${uiState.counts[category] ?: 0})") },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (uiState.selectedCategory == category) Color(0xFF1A6A5E) else MaterialTheme.colorScheme.surface,
                    labelColor = if (uiState.selectedCategory == category) Color.White else MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}

@Composable
private fun PolicyManagementTabs(
    onOpenServiceCategory: (PolicyCategory) -> Unit
) {
    val folderCategories = remember {
        PolicyCategory.entries.filter { it != PolicyCategory.FINANCIALS }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        folderCategories.forEach { category ->
            AssistChip(
                onClick = { onOpenServiceCategory(category) },
                label = { Text(category.label) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    labelColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PolicyTile(
    model: PolicyUiModel,
    onClick: () -> Unit,
    onMarkRenewed: () -> Unit,
    onClosePolicy: () -> Unit,
    onLongPressDelete: () -> Unit
) {
    val accent = when (model.status) {
        PolicyStatus.ACTIVE -> Color(0xFF25C59A)
        PolicyStatus.EXPIRING_SOON -> Color(0xFFFFFFFF)
        PolicyStatus.EXPIRED -> Color(0xFF111111)
    }
    val background = when (model.status) {
        PolicyStatus.ACTIVE -> Color(0xFF0F5A34)
        PolicyStatus.EXPIRING_SOON -> Color(0xFFC62828)
        PolicyStatus.EXPIRED -> Color(0xFFD32F2F)
    }
    val primaryText = Color(0xFFF3FFF7)
    val secondaryText = when (model.status) {
        PolicyStatus.ACTIVE -> Color(0xFFD5F8DF)
        else -> Color(0xFFFFDADA)
    }

    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongPressDelete),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = background)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (model.category) {
                            PolicyCategory.HEALTH_INSURANCE -> Icons.Default.HealthAndSafety
                            PolicyCategory.TERM_INSURANCE -> Icons.Default.Security
                            PolicyCategory.VEHICLE_INSURANCE -> Icons.Default.CreditCard
                            PolicyCategory.DRIVING_LICENCE -> Icons.Default.Description
                            PolicyCategory.MEDICAL_RECORDS -> Icons.Default.Description
                            PolicyCategory.BANKING -> Icons.Default.AccountBalanceWallet
                            PolicyCategory.IDENTITY -> Icons.Default.Badge
                            PolicyCategory.EDUCATION -> Icons.Default.School
                            PolicyCategory.GENERAL -> Icons.Default.Folder
                            PolicyCategory.FINANCIALS -> Icons.Default.AccountBalance
                        },
                        contentDescription = null,
                        tint = accent
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        if (model.policyHolderName.isNotBlank()) {
                            Text(
                                model.policyHolderName,
                                style = MaterialTheme.typography.labelSmall,
                                color = secondaryText
                            )
                        }
                        val title = if (model.category == PolicyCategory.VEHICLE_INSURANCE) {
                            when {
                                model.categoryDetails.makeModelVariant.isNotBlank() -> model.categoryDetails.makeModelVariant
                                model.categoryDetails.vehicleNumber.isNotBlank() -> model.categoryDetails.vehicleNumber
                                else -> "Vehicle"
                            }
                        } else {
                            model.policyName
                        }
                        Text(title, fontWeight = FontWeight.SemiBold, color = primaryText)
                        Text(model.category.label, style = MaterialTheme.typography.labelSmall, color = secondaryText)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (model.hasFutureRenewal) {
                        Text(
                            "Renewed",
                            color = Color(0xFFB8F5CC),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = primaryText)
                }
            }
            Text("Insurer: ${model.insurerName}", style = MaterialTheme.typography.bodySmall, color = primaryText)
            Text("Policy #: ${model.policyNumber}", style = MaterialTheme.typography.bodySmall, color = primaryText)
            when (model.category) {
                PolicyCategory.TERM_INSURANCE -> {
                    val paid = model.categoryDetails.paidPayments ?: 0
                    val left = model.categoryDetails.paymentsLeft ?: 0
                    Text("Term payments: paid $paid, left $left", style = MaterialTheme.typography.bodySmall, color = secondaryText)
                }
                PolicyCategory.VEHICLE_INSURANCE -> {
                    if (model.categoryDetails.vehicleNumber.isNotBlank()) {
                        Text("Vehicle: ${model.categoryDetails.vehicleNumber}", style = MaterialTheme.typography.bodySmall, color = secondaryText)
                    }
                }
                PolicyCategory.DRIVING_LICENCE -> {
                    val paid = model.categoryDetails.paidPayments
                    val left = model.categoryDetails.paymentsLeft
                    if (paid != null && left != null) {
                        Text("Payments: paid $paid, left $left", style = MaterialTheme.typography.bodySmall, color = secondaryText)
                    }
                }
                else -> Unit
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Expires ${formatEpochDay(model.expiryDateEpochDay)}", style = MaterialTheme.typography.bodyMedium, color = primaryText)
                Text(
                    text = formatTimeLeft(model.expiryDateEpochDay),
                    color = accent,
                    fontWeight = FontWeight.Bold
                )
            }
            if (model.status != PolicyStatus.ACTIVE && !model.hasFutureRenewal) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onMarkRenewed) {
                        Text("Mark Renewed")
                    }
                    TextButton(onClick = onClosePolicy) {
                        Text("Close", color = primaryText)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(onAddPolicy: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("No policy records yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("Add your first insurance policy and track renewal dates in one place.")
            ElevatedButton(onClick = onAddPolicy) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create first policy")
            }
        }
    }
}
