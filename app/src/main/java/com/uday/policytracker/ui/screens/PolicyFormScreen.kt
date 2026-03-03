package com.uday.policytracker.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.uday.policytracker.data.model.PolicyCategory
import com.uday.policytracker.ui.components.DatePickerField
import com.uday.policytracker.util.formatEpochDay
import com.uday.policytracker.util.parseFlexibleDate
import com.uday.policytracker.viewmodel.PolicyInput
import com.uday.policytracker.viewmodel.PolicyViewModel
import com.uday.policytracker.viewmodel.defaultPolicyInput
import java.time.LocalDate
import org.json.JSONArray
import org.json.JSONObject

private val rtaDocumentTypes = listOf(
    "Driving License",
    "RC",
    "Pollution Certificate",
    "Fitness Certificate",
    "Road Tax",
    "Permit",
    "Custom"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PolicyFormScreen(
    paddingValues: PaddingValues,
    viewModel: PolicyViewModel,
    policyId: Long?,
    onBack: () -> Unit,
    onSaved: (Long) -> Unit
) {
    val context = LocalContext.current
    val policyFlow = remember(policyId) { policyId?.let { viewModel.observePolicy(it) } }
    val policyState = policyFlow?.collectAsStateWithLifecycle()

    var form by remember(policyId) { mutableStateOf(defaultPolicyInput()) }
    val availableTags by remember(form.category) { viewModel.observeFolderTags(form.category) }.collectAsStateWithLifecycle()
    val existingTagIdsState = remember(policyId) { policyId?.let { viewModel.observePolicyFolderTags(it) } }
    val existingTagIds by (existingTagIdsState?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(emptySet()) })
    var loaded by remember(policyId) { mutableStateOf(false) }
    var showValidationError by remember(policyId) { mutableStateOf(false) }
    var showCreateTagDialog by remember { mutableStateOf(false) }
    var newTagName by remember { mutableStateOf("") }
    var newTagError by remember { mutableStateOf(false) }
    var tagDeleteTargetId by remember { mutableStateOf<Long?>(null) }
    var tagDeleteTargetName by remember { mutableStateOf("") }
    val selectedAttachments = remember(policyId) { mutableStateListOf<Uri>() }
    val selectedTagIds = remember(policyId) { mutableStateListOf<Long>() }
    var showClaimDialog by remember { mutableStateOf(false) }
    var claimDate by remember { mutableStateOf("") }
    var claimPurpose by remember { mutableStateOf("") }
    var claimAmount by remember { mutableStateOf("") }
    var claimNotes by remember { mutableStateOf("") }
    var customFieldDefinitions by remember(form.category) {
        mutableStateOf(loadCustomFieldDefinitions(context, form.category))
    }
    var customFieldValues by remember(policyId, form.category) { mutableStateOf(mutableMapOf<String, String>()) }
    var showManageCustomFieldsDialog by remember { mutableStateOf(false) }
    var newCustomFieldLabel by remember { mutableStateOf("") }
    var newCustomFieldType by remember { mutableStateOf(CustomFieldType.TEXT) }
    var newCustomFieldOptions by remember { mutableStateOf("") }
    var customFieldError by remember { mutableStateOf(false) }

    val attachmentPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) selectedAttachments.add(uri)
    }

    LaunchedEffect(policyState?.value, loaded) {
        val policy = policyState?.value ?: return@LaunchedEffect
        if (!loaded) {
            form = PolicyInput(
                category = policy.category,
                policyHolderName = policy.policyHolderName,
                policyName = policy.policyName,
                policyNumber = policy.policyNumber,
                startDate = formatEpochDay(policy.startDateEpochDay),
                expiryDate = formatEpochDay(policy.expiryDateEpochDay),
                insurerName = policy.insurerName,
                previousInsurerName = policy.previousInsurerName,
                premiumAmount = policy.premiumAmount.toString(),
                notes = policy.notes,
                premiumFrequency = policy.categoryDetails.premiumFrequency,
                premiumDueDayOfMonth = policy.categoryDetails.premiumDueDayOfMonth?.toString().orEmpty(),
                coverageAmount = policy.categoryDetails.coverageAmount?.toString().orEmpty(),
                coverageAmountUnit = policy.categoryDetails.coverageAmountUnit,
                premiumPaymentStartDate = policy.categoryDetails.premiumPaymentStartEpochDay?.let(::formatEpochDay)
                    ?: formatEpochDay(policy.startDateEpochDay),
                premiumPaymentEndDate = policy.categoryDetails.premiumPaymentEndEpochDay?.let(::formatEpochDay)
                    ?: formatEpochDay(policy.expiryDateEpochDay),
                premiumPaymentTermYears = policy.categoryDetails.premiumPaymentTermYears?.toString().orEmpty(),
                policyTermYears = policy.categoryDetails.policyTermYears?.toString().orEmpty(),
                entryAge = policy.categoryDetails.entryAge?.toString().orEmpty(),
                coverageTillAge = policy.categoryDetails.coverageTillAge?.toString().orEmpty(),
                nomineeName = policy.categoryDetails.nomineeName,
                nomineeRelationship = policy.categoryDetails.nomineeRelationship,
                riderAddons = policy.categoryDetails.riderAddons,
                paymentMode = policy.categoryDetails.paymentMode,
                gracePeriodDays = policy.categoryDetails.gracePeriodDays?.toString().orEmpty(),
                termPolicyStatus = policy.categoryDetails.termPolicyStatus,
                totalPayments = policy.categoryDetails.totalPayments?.toString().orEmpty(),
                paidPayments = policy.categoryDetails.paidPayments?.toString().orEmpty(),
                vehicleNumber = policy.categoryDetails.vehicleNumber,
                vehicleType = policy.categoryDetails.vehicleType,
                makeModelVariant = policy.categoryDetails.makeModelVariant,
                fuelType = policy.categoryDetails.fuelType,
                vehiclePolicyType = policy.categoryDetails.vehiclePolicyType,
                vehicleAddons = policy.categoryDetails.vehicleAddons,
                claimHistory = policy.categoryDetails.claimHistory,
                documentType = if (policy.category == PolicyCategory.DRIVING_LICENCE && policy.categoryDetails.documentType.isNotBlank() && policy.categoryDetails.documentType !in rtaDocumentTypes) "Custom" else policy.categoryDetails.documentType,
                customDocumentType = if (policy.category == PolicyCategory.DRIVING_LICENCE && policy.categoryDetails.documentType.isNotBlank() && policy.categoryDetails.documentType !in rtaDocumentTypes) policy.categoryDetails.documentType else "",
                issuingRto = policy.categoryDetails.issuingRto,
                stateName = policy.categoryDetails.stateName,
                vehicleClass = policy.categoryDetails.vehicleClass,
                ownerName = policy.categoryDetails.ownerName,
                linkedVehicleNumber = policy.categoryDetails.linkedVehicleNumber,
                dateOfBirth = policy.categoryDetails.dateOfBirth,
                customFieldValuesJson = policy.categoryDetails.customFieldValuesJson
            )
            customFieldValues = parseCustomFieldValuesJson(policy.categoryDetails.customFieldValuesJson)
            loaded = true
        }
    }

    LaunchedEffect(existingTagIds, loaded, policyId) {
        if (policyId != null && loaded) {
            selectedTagIds.clear()
            selectedTagIds.addAll(existingTagIds)
        }
    }

    LaunchedEffect(form.category) {
        customFieldDefinitions = loadCustomFieldDefinitions(context, form.category)
        customFieldValues = mutableMapOf()
        if (selectedTagIds.isNotEmpty()) {
            val validIds = availableTags.map { it.id }.toSet()
            selectedTagIds.retainAll(validIds)
        }
    }

    LaunchedEffect(loaded, policyId, form.customFieldValuesJson, form.category) {
        if (policyId != null && loaded) {
            customFieldValues = parseCustomFieldValuesJson(form.customFieldValuesJson)
        }
    }

    LaunchedEffect(form.category, form.premiumPaymentStartDate, form.entryAge, form.coverageTillAge) {
        if (form.category != PolicyCategory.TERM_INSURANCE) return@LaunchedEffect
        val start = parseFlexibleDate(form.premiumPaymentStartDate) ?: return@LaunchedEffect
        val entry = form.entryAge.toIntOrNull() ?: return@LaunchedEffect
        val till = form.coverageTillAge.toIntOrNull() ?: return@LaunchedEffect
        if (till < entry) return@LaunchedEffect
        val years = till - entry
        val expiry = formatEpochDay(LocalDate.ofEpochDay(start).plusYears(years.toLong()).toEpochDay())
        if (form.startDate != form.premiumPaymentStartDate || form.policyTermYears != years.toString() || form.expiryDate != expiry) {
            form = form.copy(startDate = form.premiumPaymentStartDate, policyTermYears = years.toString(), expiryDate = expiry)
        }
    }

    LaunchedEffect(form.category, form.premiumPaymentStartDate, form.entryAge, form.policyTermYears) {
        if (form.category != PolicyCategory.TERM_INSURANCE) return@LaunchedEffect
        val start = parseFlexibleDate(form.premiumPaymentStartDate) ?: return@LaunchedEffect
        val entry = form.entryAge.toIntOrNull() ?: return@LaunchedEffect
        val years = form.policyTermYears.toIntOrNull() ?: return@LaunchedEffect
        if (years < 0) return@LaunchedEffect
        val tillAge = (entry + years).toString()
        val expiry = formatEpochDay(LocalDate.ofEpochDay(start).plusYears(years.toLong()).toEpochDay())
        if (form.coverageTillAge != tillAge || form.expiryDate != expiry || form.startDate != form.premiumPaymentStartDate) {
            form = form.copy(coverageTillAge = tillAge, expiryDate = expiry, startDate = form.premiumPaymentStartDate)
        }
    }

    LaunchedEffect(form.category, form.premiumPaymentStartDate, form.premiumPaymentTermYears) {
        if (form.category != PolicyCategory.TERM_INSURANCE) return@LaunchedEffect
        val start = parseFlexibleDate(form.premiumPaymentStartDate) ?: return@LaunchedEffect
        val years = form.premiumPaymentTermYears.toIntOrNull() ?: return@LaunchedEffect
        if (years < 0) return@LaunchedEffect
        val paymentEnd = formatEpochDay(LocalDate.ofEpochDay(start).plusYears(years.toLong()).toEpochDay())
        if (form.premiumPaymentEndDate != paymentEnd) {
            form = form.copy(premiumPaymentEndDate = paymentEnd, startDate = form.premiumPaymentStartDate)
        }
    }

    LaunchedEffect(form.category, form.premiumPaymentStartDate, form.premiumPaymentEndDate) {
        if (form.category != PolicyCategory.TERM_INSURANCE) return@LaunchedEffect
        val start = parseFlexibleDate(form.premiumPaymentStartDate) ?: return@LaunchedEffect
        val end = parseFlexibleDate(form.premiumPaymentEndDate) ?: return@LaunchedEffect
        if (end < start) return@LaunchedEffect
        val years = java.time.temporal.ChronoUnit.YEARS.between(LocalDate.ofEpochDay(start), LocalDate.ofEpochDay(end)).toInt()
        if (form.premiumPaymentTermYears != years.toString()) {
            form = form.copy(premiumPaymentTermYears = years.toString(), startDate = form.premiumPaymentStartDate)
        }
    }

    LaunchedEffect(form.category, form.startDate, form.expiryDate, form.premiumFrequency, form.premiumPaymentStartDate, form.premiumPaymentEndDate) {
        if (form.category !in listOf(
                PolicyCategory.TERM_INSURANCE,
                PolicyCategory.VEHICLE_INSURANCE,
                PolicyCategory.DRIVING_LICENCE
            )
        ) return@LaunchedEffect
        val start = if (form.category == PolicyCategory.TERM_INSURANCE) {
            parseFlexibleDate(form.premiumPaymentStartDate)
        } else {
            parseFlexibleDate(form.startDate)
        } ?: return@LaunchedEffect
        val end = if (form.category == PolicyCategory.TERM_INSURANCE) {
            parseFlexibleDate(form.premiumPaymentEndDate)
        } else {
            parseFlexibleDate(form.expiryDate)
        } ?: return@LaunchedEffect
        if (end < start) return@LaunchedEffect
        val frequency = form.premiumFrequency.ifBlank { "Yearly" }
        val (total, paid) = computePaymentStatsUi(start, end, frequency)
        if (total != null && paid != null) {
            val totalText = total.toString()
            val paidText = paid.toString()
            if (form.totalPayments != totalText || form.paidPayments != paidText) {
                form = form.copy(totalPayments = totalText, paidPayments = paidText, premiumFrequency = frequency)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (policyId == null) "New Policy" else "Edit Policy") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        CategoryField(selected = form.category, onSelected = { form = form.copy(category = it) })
                        when (form.category) {
                            PolicyCategory.HEALTH_INSURANCE -> {
                                LabeledTextField("Policy holder name", form.policyHolderName) { form = form.copy(policyHolderName = it) }
                                LabeledTextField("Policy name", form.policyName) { form = form.copy(policyName = it) }
                                LabeledTextField("Policy number", form.policyNumber) { form = form.copy(policyNumber = it) }
                                DatePickerField(
                                    label = "Start date",
                                    value = form.startDate,
                                    onDateSelected = {
                                        val autoExpiry = parseFlexibleDate(it)
                                            ?.let { epoch -> LocalDate.ofEpochDay(epoch).plusYears(1).toEpochDay() }
                                            ?.let(::formatEpochDay)
                                            ?: form.expiryDate
                                        form = form.copy(startDate = it, expiryDate = autoExpiry)
                                    }
                                )
                                DatePickerField(
                                    label = "Expiry date",
                                    value = form.expiryDate,
                                    onDateSelected = { form = form.copy(expiryDate = it) }
                                )
                                LabeledTextField("Insurer name", form.insurerName) { form = form.copy(insurerName = it) }
                                LabeledTextField("Previous insurer name", form.previousInsurerName) { form = form.copy(previousInsurerName = it) }
                                LabeledTextField("Premium amount", form.premiumAmount, keyboardType = KeyboardType.Decimal) {
                                    form = form.copy(premiumAmount = it)
                                }
                                LabeledTextField("Notes", form.notes, singleLine = false) { form = form.copy(notes = it) }
                            }
                            PolicyCategory.TERM_INSURANCE -> {
                                LabeledTextField("Policy holder name", form.policyHolderName) { form = form.copy(policyHolderName = it) }
                                LabeledTextField("Plan name", form.policyName) { form = form.copy(policyName = it) }
                                LabeledTextField("Policy number", form.policyNumber) { form = form.copy(policyNumber = it) }
                                DatePickerField(
                                    label = "Premium payment start date",
                                    value = form.premiumPaymentStartDate,
                                    onDateSelected = { form = form.copy(premiumPaymentStartDate = it, startDate = it) }
                                )
                                DatePickerField(
                                    label = "Premium payment end date",
                                    value = form.premiumPaymentEndDate,
                                    onDateSelected = { form = form.copy(premiumPaymentEndDate = it) }
                                )
                                LabeledTextField("Premium pay term (years)", form.premiumPaymentTermYears, keyboardType = KeyboardType.Number) {
                                    form = form.copy(premiumPaymentTermYears = it)
                                }
                                Text(
                                    "Policy valid till: ${form.expiryDate}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                LabeledTextField("Insurer name", form.insurerName) { form = form.copy(insurerName = it) }
                                LabeledTextField("Premium amount", form.premiumAmount, keyboardType = KeyboardType.Decimal) { form = form.copy(premiumAmount = it) }
                            }
                            PolicyCategory.VEHICLE_INSURANCE -> {
                                LabeledTextField("Policy holder name", form.policyHolderName) { form = form.copy(policyHolderName = it) }
                                LabeledTextField("Vehicle name", form.makeModelVariant) { form = form.copy(makeModelVariant = it) }
                                LabeledTextField("Policy number", form.policyNumber) { form = form.copy(policyNumber = it) }
                                DatePickerField(label = "Start date", value = form.startDate, onDateSelected = { form = form.copy(startDate = it) })
                                DatePickerField(label = "Expiry date", value = form.expiryDate, onDateSelected = { form = form.copy(expiryDate = it) })
                                LabeledTextField("Insurer name", form.insurerName) { form = form.copy(insurerName = it) }
                                LabeledTextField("Premium amount", form.premiumAmount, keyboardType = KeyboardType.Decimal) { form = form.copy(premiumAmount = it) }
                            }
                            PolicyCategory.DRIVING_LICENCE -> {
                                LabeledTextField("Policy holder name", form.policyHolderName) { form = form.copy(policyHolderName = it) }
                                LabeledTextField("Document number", form.policyNumber) { form = form.copy(policyNumber = it) }
                                DatePickerField(label = "Issue date", value = form.startDate, onDateSelected = { form = form.copy(startDate = it) })
                                DatePickerField(label = "Expiry date", value = form.expiryDate, onDateSelected = { form = form.copy(expiryDate = it) })
                            }
                            else -> {
                                LabeledTextField("Policy holder name", form.policyHolderName) { form = form.copy(policyHolderName = it) }
                                LabeledTextField("Policy name", form.policyName) { form = form.copy(policyName = it) }
                                LabeledTextField("Policy number", form.policyNumber) { form = form.copy(policyNumber = it) }
                                DatePickerField(label = "Start date", value = form.startDate, onDateSelected = { form = form.copy(startDate = it) })
                                DatePickerField(label = "Expiry date", value = form.expiryDate, onDateSelected = { form = form.copy(expiryDate = it) })
                                LabeledTextField("Insurer name", form.insurerName) { form = form.copy(insurerName = it) }
                            }
                        }

                        when (form.category) {
                            PolicyCategory.TERM_INSURANCE -> {
                                Text("Term Insurance Details", style = MaterialTheme.typography.titleSmall)
                                OptionDropdown(
                                    label = "Premium frequency",
                                    selected = form.premiumFrequency,
                                    options = listOf("Monthly", "Quarterly", "Half-yearly", "Yearly")
                                ) { form = form.copy(premiumFrequency = it) }
                                OptionDropdown(
                                    label = "Payment mode",
                                    selected = form.paymentMode,
                                    options = listOf("Auto-debit", "Manual")
                                ) { form = form.copy(paymentMode = it) }
                                OptionDropdown(
                                    label = "Term status",
                                    selected = form.termPolicyStatus,
                                    options = listOf("Active", "Lapsed", "Paid-up", "Closed")
                                ) { form = form.copy(termPolicyStatus = it) }
                                OptionDropdown(
                                    label = "Premium due day",
                                    selected = form.premiumDueDayOfMonth,
                                    options = (1..31).map { it.toString() }
                                ) { form = form.copy(premiumDueDayOfMonth = it) }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    LabeledTextField(
                                        label = "Coverage amount",
                                        value = form.coverageAmount,
                                        keyboardType = KeyboardType.Decimal,
                                        modifier = Modifier.weight(1f)
                                    ) { form = form.copy(coverageAmount = it) }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(modifier = Modifier.weight(0.8f)) {
                                        OptionDropdown(
                                            label = "Unit",
                                            selected = form.coverageAmountUnit,
                                            options = listOf("Lakhs", "Crores", "Thousands", "Rupees")
                                        ) { form = form.copy(coverageAmountUnit = it) }
                                    }
                                }
                                LabeledTextField("Policy term (years)", form.policyTermYears, keyboardType = KeyboardType.Number) { form = form.copy(policyTermYears = it) }
                                LabeledTextField("Entry age", form.entryAge, keyboardType = KeyboardType.Number) { form = form.copy(entryAge = it) }
                                LabeledTextField("Coverage till age", form.coverageTillAge, keyboardType = KeyboardType.Number) { form = form.copy(coverageTillAge = it) }
                                LabeledTextField("Nominee name", form.nomineeName) { form = form.copy(nomineeName = it) }
                                OptionDropdown(
                                    label = "Nominee relationship",
                                    selected = form.nomineeRelationship,
                                    options = listOf("Spouse", "Father", "Mother", "Son", "Daughter", "Brother", "Sister", "Other")
                                ) { form = form.copy(nomineeRelationship = it) }
                                LabeledTextField("Rider add-ons", form.riderAddons) { form = form.copy(riderAddons = it) }
                                LabeledTextField("Grace period days", form.gracePeriodDays, keyboardType = KeyboardType.Number) { form = form.copy(gracePeriodDays = it) }
                                val left = (form.totalPayments.toIntOrNull() ?: 0) - (form.paidPayments.toIntOrNull() ?: 0)
                                Text(
                                    "Payments: Paid ${form.paidPayments.ifBlank { "0" }} / Total ${form.totalPayments.ifBlank { "0" }} / Left ${left.coerceAtLeast(0)}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            PolicyCategory.VEHICLE_INSURANCE -> {
                                Text("Vehicle Insurance Details", style = MaterialTheme.typography.titleSmall)
                                LabeledTextField("Vehicle number", form.vehicleNumber) { form = form.copy(vehicleNumber = it) }
                                OptionDropdown(
                                    label = "Vehicle type",
                                    selected = form.vehicleType,
                                    options = listOf("2W", "4W", "Commercial")
                                ) { form = form.copy(vehicleType = it) }
                                LabeledTextField("Make / Model / Variant (optional)", form.makeModelVariant) { form = form.copy(makeModelVariant = it) }
                                OptionDropdown(
                                    label = "Fuel type",
                                    selected = form.fuelType,
                                    options = listOf("Petrol", "Diesel", "CNG", "EV", "Hybrid", "Other")
                                ) { form = form.copy(fuelType = it) }
                                OptionDropdown(
                                    label = "Policy type",
                                    selected = form.vehiclePolicyType,
                                    options = listOf("Comprehensive", "Third-party", "Own-damage")
                                ) { form = form.copy(vehiclePolicyType = it) }
                                OptionDropdown(
                                    label = "Premium frequency",
                                    selected = form.premiumFrequency,
                                    options = listOf("Monthly", "Quarterly", "Half-yearly", "Yearly")
                                ) { form = form.copy(premiumFrequency = it) }
                                LabeledTextField("Add-ons", form.vehicleAddons) { form = form.copy(vehicleAddons = it) }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(onClick = { showClaimDialog = true }) {
                                        Text("Claim history")
                                    }
                                    val count = parseVehicleClaims(form.claimHistory).size
                                    Text("$count record(s)", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            PolicyCategory.DRIVING_LICENCE -> {
                                Text("RTA Details", style = MaterialTheme.typography.titleSmall)
                                OptionDropdown(
                                    label = "Document type",
                                    selected = form.documentType,
                                    options = rtaDocumentTypes
                                ) { form = form.copy(documentType = it) }
                                if (form.documentType == "Custom") {
                                    LabeledTextField("Custom document type", form.customDocumentType) {
                                        form = form.copy(customDocumentType = it)
                                    }
                                }
                                DatePickerField(label = "Date of birth", value = form.dateOfBirth, onDateSelected = { form = form.copy(dateOfBirth = it) })
                                OptionDropdown(
                                    label = "Vehicle class",
                                    selected = form.vehicleClass,
                                    options = listOf("LMV", "MCWG", "MCWOG", "Transport", "Other")
                                ) { form = form.copy(vehicleClass = it) }
                                LabeledTextField("Issuing RTO", form.issuingRto) { form = form.copy(issuingRto = it) }
                                LabeledTextField("State", form.stateName) { form = form.copy(stateName = it) }
                                LabeledTextField("Owner name", form.ownerName) { form = form.copy(ownerName = it) }
                                LabeledTextField("Linked vehicle number", form.linkedVehicleNumber) { form = form.copy(linkedVehicleNumber = it) }
                                OptionDropdown(
                                    label = "Document payment frequency",
                                    selected = form.premiumFrequency,
                                    options = listOf("Yearly", "Half-yearly", "Quarterly", "Monthly")
                                ) { form = form.copy(premiumFrequency = it) }
                                val left = (form.totalPayments.toIntOrNull() ?: 0) - (form.paidPayments.toIntOrNull() ?: 0)
                                Text(
                                    "Payments: Paid ${form.paidPayments.ifBlank { "0" }} / Total ${form.totalPayments.ifBlank { "0" }} / Left ${left.coerceAtLeast(0)}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            else -> Unit
                        }

                        Text("Custom Fields", style = MaterialTheme.typography.titleSmall)
                        if (customFieldDefinitions.isEmpty()) {
                            Text(
                                "No custom fields configured for this category.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        customFieldDefinitions.forEach { field ->
                            when (field.type) {
                                CustomFieldType.TEXT -> {
                                    LabeledTextField(
                                        label = field.label,
                                        value = customFieldValues[field.id].orEmpty()
                                    ) {
                                        customFieldValues = customFieldValues.toMutableMap().apply { put(field.id, it) }
                                    }
                                }
                                CustomFieldType.NUMBER -> {
                                    LabeledTextField(
                                        label = field.label,
                                        value = customFieldValues[field.id].orEmpty(),
                                        keyboardType = KeyboardType.Decimal
                                    ) {
                                        customFieldValues = customFieldValues.toMutableMap().apply { put(field.id, it) }
                                    }
                                }
                                CustomFieldType.DATE -> {
                                    DatePickerField(
                                        label = field.label,
                                        value = customFieldValues[field.id].orEmpty(),
                                        onDateSelected = {
                                            customFieldValues = customFieldValues.toMutableMap().apply { put(field.id, it) }
                                        }
                                    )
                                }
                                CustomFieldType.DROPDOWN -> {
                                    OptionDropdown(
                                        label = field.label,
                                        selected = customFieldValues[field.id].orEmpty(),
                                        options = field.options
                                    ) {
                                        customFieldValues = customFieldValues.toMutableMap().apply { put(field.id, it) }
                                    }
                                }
                            }
                        }
                        TextButton(onClick = { showManageCustomFieldsDialog = true }) {
                            Text("Manage fields")
                        }

                        Text("Tags (Optional)", style = MaterialTheme.typography.titleSmall)
                        if (availableTags.isEmpty()) {
                            Text("No tags yet. Create one for this category.", style = MaterialTheme.typography.bodySmall)
                        }
                        if (availableTags.isNotEmpty() && selectedTagIds.isEmpty()) {
                            Text("No tag selected. Policy will not be linked to any folder.", style = MaterialTheme.typography.bodySmall)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            availableTags.forEach { tag ->
                                val selected = selectedTagIds.contains(tag.id)
                                FilterChip(
                                    modifier = Modifier.pointerInput(tag.id) {
                                        detectTapGestures(
                                            onLongPress = {
                                                tagDeleteTargetId = tag.id
                                                tagDeleteTargetName = tag.name
                                            }
                                        )
                                    },
                                    selected = selected,
                                    onClick = {
                                        if (selected) selectedTagIds.remove(tag.id) else selectedTagIds.add(tag.id)
                                    },
                                    label = { Text(tag.name) },
                                    leadingIcon = {
                                        if (selected) {
                                            Icon(Icons.Default.Check, contentDescription = null)
                                        }
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        selectedContainerColor = Color(0xFF1A6A5E),
                                        selectedLabelColor = Color.White,
                                        selectedLeadingIconColor = Color.White
                                    )
                                )
                            }
                        }
                        TextButton(onClick = { showCreateTagDialog = true }) {
                            Text("+ Create tag")
                        }

                        Button(onClick = { attachmentPicker.launch(arrayOf("*/*")) }) {
                            Icon(Icons.Default.AttachFile, contentDescription = null)
                            Text(" Attach document (optional)")
                        }
                        if (selectedAttachments.isNotEmpty()) {
                            Text("Selected: ${selectedAttachments.size} file(s)", style = MaterialTheme.typography.bodySmall)
                        }

                        if (showValidationError) {
                            Text(
                                "Please enter valid required fields for selected category in dd-MMM-yyyy format.",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(modifier = Modifier.weight(1f), onClick = onBack) { Text("Cancel") }
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val payload = form.copy(
                                startDate = if (form.category == PolicyCategory.TERM_INSURANCE) form.premiumPaymentStartDate else form.startDate,
                                customFieldValuesJson = toCustomFieldValuesJson(customFieldValues)
                            )
                            val saved = viewModel.savePolicy(payload, policyId) { id ->
                                viewModel.setPolicyFolderTags(id, selectedTagIds.toSet())
                                selectedAttachments.forEach { uri ->
                                    viewModel.addAttachment(id, uri, context.contentResolver)
                                }
                                onSaved(id)
                            }
                            showValidationError = !saved
                        }
                    ) {
                        Text(if (policyId == null) "Create" else "Save")
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    if (showManageCustomFieldsDialog) {
        AlertDialog(
            onDismissRequest = { showManageCustomFieldsDialog = false },
            title = { Text("Manage Custom Fields") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (customFieldDefinitions.isEmpty()) {
                        Text("No custom fields yet for ${form.category.label}.")
                    } else {
                        customFieldDefinitions.forEach { field ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${field.label} (${field.type.name.lowercase().replaceFirstChar { it.uppercase() }})")
                                IconButton(onClick = {
                                    val updated = customFieldDefinitions.filterNot { it.id == field.id }
                                    customFieldDefinitions = updated
                                    customFieldValues = customFieldValues.toMutableMap().apply { remove(field.id) }
                                    saveCustomFieldDefinitions(context, form.category, updated)
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete field")
                                }
                            }
                        }
                    }
                    OutlinedTextField(
                        value = newCustomFieldLabel,
                        onValueChange = { newCustomFieldLabel = it },
                        label = { Text("Field label") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OptionDropdown(
                        label = "Field type",
                        selected = newCustomFieldType.name.lowercase().replaceFirstChar { it.uppercase() },
                        options = CustomFieldType.entries.map { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } }
                    ) { selectedLabel ->
                        newCustomFieldType = CustomFieldType.valueOf(selectedLabel.uppercase())
                    }
                    if (newCustomFieldType == CustomFieldType.DROPDOWN) {
                        OutlinedTextField(
                            value = newCustomFieldOptions,
                            onValueChange = { newCustomFieldOptions = it },
                            label = { Text("Dropdown options (comma separated)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (customFieldError) {
                        Text("Enter valid field details.", color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val label = newCustomFieldLabel.trim()
                    val options = newCustomFieldOptions.split(",").map { it.trim() }.filter { it.isNotBlank() }
                    val isValid = label.isNotBlank() &&
                        (newCustomFieldType != CustomFieldType.DROPDOWN || options.isNotEmpty())
                    if (!isValid) {
                        customFieldError = true
                        return@TextButton
                    }
                    val newField = CustomFieldDefinition(
                        id = "field_${System.currentTimeMillis()}",
                        label = label,
                        type = newCustomFieldType,
                        options = if (newCustomFieldType == CustomFieldType.DROPDOWN) options else emptyList()
                    )
                    val updated = customFieldDefinitions + newField
                    customFieldDefinitions = updated
                    saveCustomFieldDefinitions(context, form.category, updated)
                    newCustomFieldLabel = ""
                    newCustomFieldOptions = ""
                    newCustomFieldType = CustomFieldType.TEXT
                    customFieldError = false
                }) { Text("Add Field") }
            },
            dismissButton = {
                TextButton(onClick = { showManageCustomFieldsDialog = false }) { Text("Done") }
            }
        )
    }

    if (showCreateTagDialog) {
        AlertDialog(
            onDismissRequest = { showCreateTagDialog = false },
            title = { Text("Create tag") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newTagName,
                        onValueChange = { newTagName = it },
                        label = { Text("Tag name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (newTagError) {
                        Text("Enter a valid tag name.", color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val created = viewModel.createFolder(form.category, com.uday.policytracker.viewmodel.FolderInput(name = newTagName))
                    if (created) {
                        newTagName = ""
                        newTagError = false
                        showCreateTagDialog = false
                    } else {
                        newTagError = true
                    }
                }) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateTagDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showClaimDialog) {
        AlertDialog(
            onDismissRequest = { showClaimDialog = false },
            title = { Text("Add claim history") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DatePickerField(label = "Claim date", value = claimDate, onDateSelected = { claimDate = it })
                    LabeledTextField("Purpose", claimPurpose) { claimPurpose = it }
                    LabeledTextField("Claim amount", claimAmount, keyboardType = KeyboardType.Decimal) { claimAmount = it }
                    LabeledTextField("Notes", claimNotes, singleLine = false) { claimNotes = it }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val claims = parseVehicleClaims(form.claimHistory).toMutableList()
                        claims.add(
                            VehicleClaimRecord(
                                date = claimDate,
                                purpose = claimPurpose.trim(),
                                amount = claimAmount.trim(),
                                notes = claimNotes.trim()
                            )
                        )
                        form = form.copy(claimHistory = serializeVehicleClaims(claims))
                        claimDate = ""
                        claimPurpose = ""
                        claimAmount = ""
                        claimNotes = ""
                        showClaimDialog = false
                    }
                ) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { showClaimDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (tagDeleteTargetId != null) {
        AlertDialog(
            onDismissRequest = { tagDeleteTargetId = null },
            title = { Text("Delete tag") },
            text = { Text("Delete tag \"$tagDeleteTargetName\"? This will delete its folder as well.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteFolder(tagDeleteTargetId!!)
                    selectedTagIds.remove(tagDeleteTargetId!!)
                    tagDeleteTargetId = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { tagDeleteTargetId = null }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryField(selected: PolicyCategory, onSelected: (PolicyCategory) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selected.label,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            label = { Text("Category") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            PolicyCategory.entries.filter { it.supportsPolicyRecords }.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.label) },
                    onClick = {
                        onSelected(category)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun LabeledTextField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = singleLine,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OptionDropdown(
    label: String,
    selected: String,
    options: List<String>,
    onSelected: (String) -> Unit
) {
    var expanded by remember(label, selected) { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

private data class VehicleClaimRecord(
    val date: String,
    val purpose: String,
    val amount: String,
    val notes: String
)

private fun parseVehicleClaims(raw: String): List<VehicleClaimRecord> {
    if (raw.isBlank()) return emptyList()
    return runCatching {
        val array = JSONArray(raw)
        buildList {
            for (i in 0 until array.length()) {
                val o = array.getJSONObject(i)
                add(
                    VehicleClaimRecord(
                        date = o.optString("date"),
                        purpose = o.optString("purpose"),
                        amount = o.optString("amount"),
                        notes = o.optString("notes")
                    )
                )
            }
        }
    }.getOrDefault(emptyList())
}

private fun serializeVehicleClaims(items: List<VehicleClaimRecord>): String {
    return JSONArray().apply {
        items.forEach { item ->
            put(
                JSONObject().apply {
                    put("date", item.date)
                    put("purpose", item.purpose)
                    put("amount", item.amount)
                    put("notes", item.notes)
                }
            )
        }
    }.toString()
}

private fun computePaymentStatsUi(startEpochDay: Long, endEpochDay: Long, frequency: String): Pair<Int?, Int?> {
    val stepMonths = when (frequency.lowercase()) {
        "monthly" -> 1
        "quarterly" -> 3
        "half-yearly" -> 6
        "yearly" -> 12
        else -> return null to null
    }
    val start = LocalDate.ofEpochDay(startEpochDay)
    val end = LocalDate.ofEpochDay(endEpochDay)
    if (end.isBefore(start)) return 0 to 0
    val today = LocalDate.now()
    var total = 0
    var paid = 0
    var dueDate = start
    while (!dueDate.isAfter(end)) {
        total += 1
        if (!dueDate.isAfter(today)) paid += 1
        dueDate = dueDate.plusMonths(stepMonths.toLong())
    }
    return total to paid
}
