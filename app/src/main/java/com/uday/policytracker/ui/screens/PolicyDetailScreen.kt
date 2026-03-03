package com.uday.policytracker.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.uday.policytracker.data.model.PolicyStatus
import com.uday.policytracker.ui.components.DatePickerField
import com.uday.policytracker.ui.components.DocumentViewerDialog
import com.uday.policytracker.util.formatEpochDay
import com.uday.policytracker.util.formatIndianAmount
import com.uday.policytracker.viewmodel.HistoryInput
import com.uday.policytracker.viewmodel.AttachmentUiModel
import com.uday.policytracker.viewmodel.PolicyViewModel
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PolicyDetailScreen(
    paddingValues: PaddingValues,
    viewModel: PolicyViewModel,
    policyId: Long,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    val policyFlow = remember(policyId) { viewModel.observePolicy(policyId) }
    val policy by policyFlow.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val attachmentLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            viewModel.addAttachment(policyId, uri, context.contentResolver)
        }
    }
    val historyAttachmentUris = remember { mutableStateListOf<android.net.Uri>() }
    val historyAttachmentLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) historyAttachmentUris.add(uri)
    }
    val editHistoryAttachmentUris = remember { mutableStateListOf<android.net.Uri>() }
    val editHistoryAttachmentLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) editHistoryAttachmentUris.add(uri)
    }

    if (policy == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Policy details") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Policy not found")
            }
        }
        return
    }

    var historyForm by remember {
        mutableStateOf(
            HistoryInput(
                policyHolderName = "",
                insurerName = "",
                policyNumber = "",
                startDate = "",
                endDate = "",
                premiumAmount = ""
            )
        )
    }
    var editHistoryTarget by remember { mutableStateOf<com.uday.policytracker.viewmodel.HistoryUiModel?>(null) }
    var selectedHistoryDetail by remember { mutableStateOf<com.uday.policytracker.viewmodel.HistoryUiModel?>(null) }
    var editHistoryError by remember { mutableStateOf(false) }
    var historyError by remember { mutableStateOf(false) }
    var showAddHistoryDialog by remember { mutableStateOf(false) }
    var viewerAttachment by remember { mutableStateOf<AttachmentUiModel?>(null) }
    val currentPolicy = policy ?: return
    val activeAttachmentUris = currentPolicy.attachments.map { it.uri }.toSet()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentPolicy.policyName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = {
                        viewModel.deletePolicy(currentPolicy)
                        onBack()
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(innerPadding),
            contentPadding = PaddingValues(vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(currentPolicy.category.label, color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.labelLarge)
                        if (currentPolicy.policyHolderName.isNotBlank()) {
                            Text("Holder: ${currentPolicy.policyHolderName}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        }
                        Text("Policy # ${currentPolicy.policyNumber}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Start: ${formatEpochDay(currentPolicy.startDateEpochDay)}", style = MaterialTheme.typography.bodyMedium)
                            Text("Expiry: ${formatEpochDay(currentPolicy.expiryDateEpochDay)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Insurer: ${currentPolicy.insurerName}", style = MaterialTheme.typography.bodyMedium)
                            Text("Prev: ${currentPolicy.previousInsurerName.ifBlank { "N/A" }}", style = MaterialTheme.typography.bodyMedium)
                        }
                        Text("Premium: ₹${formatIndianAmount(currentPolicy.premiumAmount, 2)}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        when (currentPolicy.category) {
                            com.uday.policytracker.data.model.PolicyCategory.TERM_INSURANCE -> {
                                val d = currentPolicy.categoryDetails
                                if (d.coverageAmount != null) Text("Coverage: ₹${formatIndianAmount(d.coverageAmount, 2)} ${d.coverageAmountUnit}")
                                if (d.premiumFrequency.isNotBlank()) Text("Frequency: ${d.premiumFrequency}")
                                d.premiumDueDayOfMonth?.let { Text("Due day: $it") }
                                if (d.entryAge != null || d.coverageTillAge != null) {
                                    Text("Age coverage: ${d.entryAge ?: "-"} to ${d.coverageTillAge ?: "-"}")
                                }
                                if (d.premiumPaymentStartEpochDay != null) {
                                    Text("Premium pay start: ${formatEpochDay(d.premiumPaymentStartEpochDay)}")
                                }
                                if (d.premiumPaymentEndEpochDay != null) {
                                    Text("Premium pay end: ${formatEpochDay(d.premiumPaymentEndEpochDay)}")
                                }
                                d.premiumPaymentTermYears?.let { Text("Premium pay term: $it year(s)") }
                                if (d.policyValidityEndEpochDay != null) {
                                    Text("Policy valid till: ${formatEpochDay(d.policyValidityEndEpochDay)}")
                                }
                                if (d.totalPayments != null || d.paidPayments != null) {
                                    Text("Payments: Paid ${d.paidPayments ?: 0} / Total ${d.totalPayments ?: 0} / Left ${d.paymentsLeft ?: 0}")
                                }
                            }
                            com.uday.policytracker.data.model.PolicyCategory.VEHICLE_INSURANCE -> {
                                val d = currentPolicy.categoryDetails
                                if (d.vehicleNumber.isNotBlank()) Text("Vehicle #: ${d.vehicleNumber}")
                                if (d.vehicleType.isNotBlank()) Text("Type: ${d.vehicleType}")
                                if (d.makeModelVariant.isNotBlank()) Text("Model: ${d.makeModelVariant}")
                                if (d.fuelType.isNotBlank()) Text("Fuel: ${d.fuelType}")
                                if (d.vehiclePolicyType.isNotBlank()) Text("Policy type: ${d.vehiclePolicyType}")
                                if (d.vehicleAddons.isNotBlank()) Text("Add-ons: ${d.vehicleAddons}")
                                if (d.claimHistory.isNotBlank()) {
                                    val count = runCatching { org.json.JSONArray(d.claimHistory).length() }.getOrDefault(0)
                                    Text("Claims: $count record(s)")
                                }
                                if (d.totalPayments != null || d.paidPayments != null) {
                                    Text("Payments: Paid ${d.paidPayments ?: 0} / Total ${d.totalPayments ?: 0} / Left ${d.paymentsLeft ?: 0}")
                                }
                            }
                            com.uday.policytracker.data.model.PolicyCategory.DRIVING_LICENCE -> {
                                val d = currentPolicy.categoryDetails
                                if (d.documentType.isNotBlank()) Text("Document type: ${d.documentType}")
                                if (d.dateOfBirth.isNotBlank()) Text("DOB: ${d.dateOfBirth}")
                                if (d.vehicleClass.isNotBlank()) Text("Vehicle class: ${d.vehicleClass}")
                                if (d.issuingRto.isNotBlank()) Text("RTO: ${d.issuingRto}")
                                if (d.stateName.isNotBlank()) Text("State: ${d.stateName}")
                                if (d.ownerName.isNotBlank()) Text("Owner: ${d.ownerName}")
                                if (d.linkedVehicleNumber.isNotBlank()) Text("Linked vehicle: ${d.linkedVehicleNumber}")
                                if (d.totalPayments != null || d.paidPayments != null) {
                                    Text("Payments: Paid ${d.paidPayments ?: 0} / Total ${d.totalPayments ?: 0} / Left ${d.paymentsLeft ?: 0}")
                                }
                            }
                            else -> Unit
                        }
                        val customValues = parseCustomFieldValuesJson(currentPolicy.categoryDetails.customFieldValuesJson)
                        if (customValues.isNotEmpty()) {
                            Text("Custom fields", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            customValues.entries.forEach { (k, v) ->
                                val label = loadCustomFieldDefinitions(context, currentPolicy.category).firstOrNull { it.id == k }?.label ?: k
                                Text("$label: $v")
                            }
                        }
                        val statusLabel = when (currentPolicy.status) {
                            PolicyStatus.ACTIVE -> "Active"
                            PolicyStatus.EXPIRING_SOON -> "Expiring soon"
                            PolicyStatus.EXPIRED -> "Expired"
                        }
                        Text("Status: $statusLabel")
                        if (currentPolicy.notes.isNotBlank()) {
                            Text("Notes: ${currentPolicy.notes}")
                        }
                    }
                }
            }

            if (currentPolicy.futurePolicies.isNotEmpty()) {
                item {
                    Text(
                        text = "Future Policies",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                items(currentPolicy.futurePolicies, key = { it.id }) { future ->
                    Card(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1D3C6A))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(future.policyName, color = Color.White, fontWeight = FontWeight.SemiBold)
                            if (future.policyHolderName.isNotBlank()) {
                                Text("Holder: ${future.policyHolderName}", color = Color(0xFFEAF1FF))
                            }
                            Text("Policy # ${future.policyNumber}", color = Color(0xFFDCE6FF))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Start: ${formatEpochDay(future.startDateEpochDay)}", color = Color(0xFFDCE6FF))
                                Text("Expiry: ${formatEpochDay(future.expiryDateEpochDay)}", color = Color(0xFFDCE6FF))
                            }
                            Text("Insurer: ${future.insurerName}", color = Color(0xFFEAF1FF))
                            Text("Premium: ₹${formatIndianAmount(future.premiumAmount, 2)}", color = Color.White)
                            val linkedFutureAttachments = future.attachments.filter { it.uri in activeAttachmentUris }
                            if (linkedFutureAttachments.isNotEmpty()) {
                                TextButton(onClick = { viewerAttachment = linkedFutureAttachments.first() }) {
                                    Text("Show Document", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Past History",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            items(currentPolicy.history, key = { it.id }) { history ->
                Card(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .clickable { selectedHistoryDetail = history },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2D3A56))
                ) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(history.insurerName, fontWeight = FontWeight.SemiBold, color = Color.White)
                            Row {
                                IconButton(onClick = { editHistoryTarget = history }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit history", tint = Color.White)
                                }
                                IconButton(onClick = { viewModel.deleteHistory(history.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete history", tint = Color.White)
                                }
                            }
                        }
                        Text("Policy # ${history.policyNumber}", color = Color(0xFFDCE6FF))
                        if (history.policyHolderName.isNotBlank()) {
                            Text("Holder: ${history.policyHolderName}", color = Color(0xFFDCE6FF))
                        }
                        Text("Start: ${formatEpochDay(history.startDateEpochDay)}", color = Color(0xFFDCE6FF))
                        Text("End: ${formatEpochDay(history.endDateEpochDay)}", color = Color(0xFFDCE6FF))
                        Text("Premium: ₹${formatIndianAmount(history.premiumAmount, 2)}", color = Color.White, fontWeight = FontWeight.Medium)
                        val linkedHistoryAttachments = history.attachmentRefs.filter { it.uri in activeAttachmentUris }
                        if (linkedHistoryAttachments.isNotEmpty()) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                Text(
                                    text = "Show Document",
                                    color = Color.White,
                                    modifier = Modifier
                                        .background(Color(0xFF1A6A5E), RoundedCornerShape(50))
                                        .clickable { viewerAttachment = linkedHistoryAttachments.first() }
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }
            }

            item {
                Card(modifier = Modifier.padding(horizontal = 16.dp), shape = RoundedCornerShape(20.dp)) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Attachments", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Button(onClick = { attachmentLauncher.launch(arrayOf("*/*")) }) {
                                Icon(Icons.Default.AttachFile, contentDescription = null)
                                Text(" Add")
                            }
                        }
                        if (currentPolicy.attachments.isEmpty()) {
                            Text("No attachments yet.")
                        }
                        currentPolicy.attachments.forEach { attachment ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewerAttachment = attachment }
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(attachment.displayName, maxLines = 1)
                                    Text(attachment.mimeType, style = MaterialTheme.typography.labelSmall)
                                }
                                IconButton(onClick = { viewModel.deleteAttachment(attachment.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete attachment")
                                }
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                        .clickable { showAddHistoryDialog = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Text(
                        "Add past history",
                        modifier = Modifier.padding(14.dp),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }

    editHistoryTarget?.let { target ->
        var insurer by remember(target.id) { mutableStateOf(target.insurerName) }
        var holder by remember(target.id) { mutableStateOf(target.policyHolderName) }
        var number by remember(target.id) { mutableStateOf(target.policyNumber) }
        var start by remember(target.id) { mutableStateOf(formatEpochDay(target.startDateEpochDay)) }
        var end by remember(target.id) { mutableStateOf(formatEpochDay(target.endDateEpochDay)) }
        var premium by remember(target.id) { mutableStateOf(target.premiumAmount.toString()) }

        AlertDialog(
            onDismissRequest = {
                editHistoryAttachmentUris.clear()
                editHistoryTarget = null
            },
            title = { Text("Edit history") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = holder, onValueChange = { holder = it }, label = { Text("Policy holder") }, singleLine = true)
                    OutlinedTextField(value = insurer, onValueChange = { insurer = it }, label = { Text("Insurer") }, singleLine = true)
                    OutlinedTextField(value = number, onValueChange = { number = it }, label = { Text("Policy #") }, singleLine = true)
                    DatePickerField(label = "Start", value = start, onDateSelected = { start = it })
                    DatePickerField(label = "End", value = end, onDateSelected = { end = it })
                    OutlinedTextField(
                        value = premium,
                        onValueChange = { premium = it },
                        label = { Text("Premium") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    Button(onClick = { editHistoryAttachmentLauncher.launch(arrayOf("*/*")) }) {
                        Text("Attach document (optional)")
                    }
                    if (editHistoryAttachmentUris.isNotEmpty()) {
                        Text("Selected: ${editHistoryAttachmentUris.size} file(s)")
                    }
                    if (editHistoryError) {
                        Text("Enter valid values to update history.", color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.editHistory(
                        historyId = target.id,
                        input = HistoryInput(
                            policyHolderName = holder,
                            insurerName = insurer,
                            policyNumber = number,
                            startDate = start,
                            endDate = end,
                            premiumAmount = premium
                        ),
                        attachmentUris = editHistoryAttachmentUris.toList(),
                        contentResolver = context.contentResolver
                    ) { success ->
                        if (success) {
                            editHistoryTarget = null
                            editHistoryAttachmentUris.clear()
                            editHistoryError = false
                        } else {
                            editHistoryError = true
                        }
                    }
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    editHistoryAttachmentUris.clear()
                    editHistoryTarget = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    selectedHistoryDetail?.let { history ->
        AlertDialog(
            onDismissRequest = { selectedHistoryDetail = null },
            title = { Text("Past history detail") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (history.policyHolderName.isNotBlank()) {
                        Text("Holder: ${history.policyHolderName}")
                    }
                    Text("Insurer: ${history.insurerName}")
                    Text("Policy #: ${history.policyNumber}")
                    Text("From: ${formatEpochDay(history.startDateEpochDay)}")
                    Text("To: ${formatEpochDay(history.endDateEpochDay)}")
                    Text("Premium: ₹${formatIndianAmount(history.premiumAmount, 2)}")
                    val linkedHistoryAttachments = history.attachmentRefs.filter { it.uri in activeAttachmentUris }
                    if (linkedHistoryAttachments.isNotEmpty()) {
                        Text("Attachments:", fontWeight = FontWeight.SemiBold)
                        linkedHistoryAttachments.forEach { attachment ->
                            Text(
                                text = attachment.displayName,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable { viewerAttachment = attachment }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedHistoryDetail = null }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedHistoryDetail = null }) {
                    Text("Close")
                }
            }
        )
    }

    if (showAddHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showAddHistoryDialog = false },
            title = { Text("Add past history") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = historyForm.policyHolderName,
                        onValueChange = { historyForm = historyForm.copy(policyHolderName = it) },
                        label = { Text("Policy holder") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = historyForm.insurerName,
                        onValueChange = { historyForm = historyForm.copy(insurerName = it) },
                        label = { Text("Insurer") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = historyForm.policyNumber,
                        onValueChange = { historyForm = historyForm.copy(policyNumber = it) },
                        label = { Text("Policy #") },
                        singleLine = true
                    )
                    DatePickerField(label = "Start", value = historyForm.startDate, onDateSelected = { historyForm = historyForm.copy(startDate = it) })
                    DatePickerField(label = "End", value = historyForm.endDate, onDateSelected = { historyForm = historyForm.copy(endDate = it) })
                    OutlinedTextField(
                        value = historyForm.premiumAmount,
                        onValueChange = { historyForm = historyForm.copy(premiumAmount = it) },
                        label = { Text("Premium amount") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    Button(onClick = { historyAttachmentLauncher.launch(arrayOf("*/*")) }) {
                        Text("Attach document (optional)")
                    }
                    if (historyAttachmentUris.isNotEmpty()) {
                        Text("Selected: ${historyAttachmentUris.size} file(s)")
                    }
                    if (historyError) {
                        Text(
                            "Please enter all details correctly before adding history.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val added = viewModel.addHistory(
                        policyId = policyId,
                        input = historyForm,
                        attachmentUris = historyAttachmentUris.toList(),
                        contentResolver = context.contentResolver
                    )
                    if (added) {
                        historyForm = HistoryInput("", "", "", "", "", "")
                        historyAttachmentUris.clear()
                        historyError = false
                        showAddHistoryDialog = false
                    } else {
                        historyError = true
                    }
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddHistoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    viewerAttachment?.let { attachment ->
        DocumentViewerDialog(
            rawUri = attachment.uri,
            mimeType = attachment.mimeType,
            onDismiss = { viewerAttachment = null }
        )
    }
}
