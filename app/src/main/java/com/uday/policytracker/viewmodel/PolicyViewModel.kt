package com.uday.policytracker.viewmodel

import android.app.Application
import android.content.Context
import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.uday.policytracker.data.db.AttachmentEntity
import com.uday.policytracker.data.db.CategoryFolderEntity
import com.uday.policytracker.data.db.FolderAttachmentEntity
import com.uday.policytracker.data.db.FolderWithDetails
import com.uday.policytracker.data.db.FuturePolicyEntity
import com.uday.policytracker.data.db.LoanEntity
import com.uday.policytracker.data.db.LoanPaymentEntity
import com.uday.policytracker.data.db.MoneyLendEntity
import com.uday.policytracker.data.db.PolicyDatabase
import com.uday.policytracker.data.db.PolicyCategoryDetailsEntity
import com.uday.policytracker.data.db.PolicyEntity
import com.uday.policytracker.data.db.PolicyFolderCrossRef
import com.uday.policytracker.data.db.PolicyHistoryEntity
import com.uday.policytracker.data.db.PolicyWithDetails
import com.uday.policytracker.data.model.PolicyCategory
import com.uday.policytracker.data.model.PolicyStatus
import com.uday.policytracker.data.repo.PolicyRepository
import com.uday.policytracker.util.daysToExpiry
import com.uday.policytracker.util.formatEpochDay
import com.uday.policytracker.util.formatFutureUi
import com.uday.policytracker.util.formatTodayUi
import com.uday.policytracker.util.parseFlexibleDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicLong
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlin.math.pow
import kotlin.math.round

private const val EXPIRY_WARNING_DAYS = 60L
private const val CLOSED_POLICY_MARKER = "[[PT_CLOSED]]"
private const val BACKUP_FORMAT_VERSION = 2
private const val BACKUP_METADATA_ENTRY = "backup/metadata.json"
private const val BACKUP_FILES_PREFIX = "backup/files/"

private data class BackupFileCandidate(
    val oldPath: String,
    val file: File,
    val entryName: String
)

class PolicyViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = PolicyRepository(PolicyDatabase.getInstance(application).policyDao())
    private val legacyMoneyLendPrefs by lazy {
        getApplication<Application>().getSharedPreferences("money_lend_store", Context.MODE_PRIVATE)
    }

    private val selectedCategory = MutableStateFlow<PolicyCategory?>(null)
    private val searchQuery = MutableStateFlow("")
    private val selectedLoanId = MutableStateFlow<Long?>(null)
    private val moneyLendIdSeed = AtomicLong(System.currentTimeMillis())
    private val incomeEntries = MutableStateFlow<List<IncomeUiModel>>(emptyList())
    private val closedLoans = MutableStateFlow<List<ClosedLoanUiModel>>(emptyList())
    private val loanEventsByLoanId = MutableStateFlow<Map<Long, List<LoanEventUiModel>>>(emptyMap())

    init {
        viewModelScope.launch {
            migrateLegacyMoneyLendsIfNeeded()
            applyDueFuturePolicies()
            autoMarkPastLoanPayments()
            ensureCreditCardLoanSeeded()
        }
    }

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.observePolicies(),
        repository.observeAllFolders(),
        selectedCategory,
        searchQuery
    ) { policies, folders, category, query ->
        val mapped = policies.map { it.toUiModel() }
        val closedPolicies = mapped.filter { it.isClosed }
        val openPolicies = mapped.filterNot { it.isClosed }
        val visiblePolicies = category?.let { selected -> openPolicies.filter { it.category == selected } } ?: openPolicies

        val totalExpiringSoon = openPolicies.count {
            !it.hasFutureRenewal && daysToExpiry(it.expiryDateEpochDay) in 0..EXPIRY_WARNING_DAYS
        }
        val visibleExpiringSoon = visiblePolicies.filter {
            !it.hasFutureRenewal && daysToExpiry(it.expiryDateEpochDay) in 0..EXPIRY_WARNING_DAYS
        }

        val normalized = query.trim().lowercase()
        val searchResults = if (normalized.isBlank()) {
            emptyList()
        } else {
            val policyMatches = openPolicies.filter {
                it.policyHolderName.lowercase().contains(normalized) ||
                it.policyName.lowercase().contains(normalized) ||
                it.policyNumber.lowercase().contains(normalized) ||
                it.insurerName.lowercase().contains(normalized)
            }.map { SearchResultUiModel.PolicyResult(it.id, it.policyName, it.policyNumber, it.category.label) }

            val folderMatches = folders.mapNotNull {
                if (!it.name.lowercase().contains(normalized)) return@mapNotNull null
                val categoryValue = runCatching { PolicyCategory.valueOf(it.category) }.getOrNull() ?: return@mapNotNull null
                SearchResultUiModel.FolderResult(it.id, it.name, categoryValue.label)
            }
            (policyMatches + folderMatches).take(30)
        }

        DashboardUiState(
            policies = visiblePolicies,
            expiringSoon = visibleExpiringSoon,
            selectedCategory = category,
            searchQuery = query,
            searchResults = searchResults,
            totalRecords = openPolicies.size,
            totalExpiringSoon = totalExpiringSoon,
            counts = PolicyCategory.entries.associateWith { cat -> openPolicies.count { it.category == cat } },
            closedPolicies = closedPolicies
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())

    val loansState: StateFlow<List<LoanUiModel>> = repository.observeLoans()
        .map { loans ->
            val mapped = loans.map { loan ->
                LoanUiModel(
                    id = loan.id,
                    loanName = loan.loanName,
                    lenderName = loan.lenderName,
                    principalAmount = loan.principalAmount,
                    annualInterestRate = loan.annualInterestRate,
                    tenureMonths = loan.tenureMonths,
                    emiAmount = loan.emiAmount,
                    paymentFrequency = loan.paymentFrequency,
                    startDateEpochDay = loan.startDateEpochDay
                )
            }
            if (selectedLoanId.value == null && mapped.isNotEmpty()) {
                selectedLoanId.value = mapped.first().id
            }
            mapped
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val selectedLoanPayments: StateFlow<List<LoanPaymentUiModel>> = selectedLoanId
        .flatMapLatest { loanId ->
            if (loanId == null) flowOf(emptyList())
            else repository.observeLoanPayments(loanId).map { rows ->
                rows.map {
                    LoanPaymentUiModel(
                        id = it.id,
                        installmentNumber = it.installmentNumber,
                        dueDateEpochDay = it.dueDateEpochDay,
                        amountDue = it.amountDue,
                        principalComponent = it.principalComponent,
                        interestComponent = it.interestComponent,
                        isPaid = it.isPaid,
                        paidOnEpochDay = it.paidOnEpochDay
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val loanOutstandingById: StateFlow<Map<Long, Double>> = repository.observeAllLoanPayments()
        .map { rows ->
            rows.groupBy { it.loanId }.mapValues { (_, payments) ->
                payments.filterNot { it.isPaid }.sumOf { it.principalComponent }.coerceAtLeast(0.0)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    val selectedLoanEvents: StateFlow<List<LoanEventUiModel>> = combine(
        selectedLoanId,
        loanEventsByLoanId
    ) { loanId, eventMap ->
        loanId?.let { eventMap[it] }.orEmpty()
            .sortedWith(
                compareByDescending<LoanEventUiModel> { it.dateEpochDay }
                    .thenByDescending { it.id }
            )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val moneyLendState: StateFlow<List<MoneyLendUiModel>> = repository.observeMoneyLends()
        .map { rows ->
            val mapped = rows.map { it.toUiModel() }
            val maxId = mapped.maxOfOrNull { it.id } ?: System.currentTimeMillis()
            moneyLendIdSeed.set(maxOf(moneyLendIdSeed.get(), maxId))
            mapped
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val incomeState: StateFlow<List<IncomeUiModel>> = incomeEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val closedLoansState: StateFlow<List<ClosedLoanUiModel>> = closedLoans
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setCategory(category: PolicyCategory?) {
        selectedCategory.value = if (category == null || category.supportsPolicyRecords) category else null
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun savePolicy(input: PolicyInput, policyId: Long? = null, onSaved: (Long) -> Unit = {}): Boolean {
        val parsed = parsePolicy(input) ?: return false
        viewModelScope.launch {
            val entity = PolicyEntity(
                id = policyId ?: 0,
                category = parsed.category.name,
                policyHolderName = parsed.policyHolderName,
                policyName = parsed.policyName,
                policyNumber = parsed.policyNumber,
                startDateEpochDay = parsed.startDate,
                expiryDateEpochDay = parsed.expiryDate,
                insurerName = parsed.insurerName,
                previousInsurerName = parsed.previousInsurerName,
                premiumAmount = parsed.premiumAmount,
                notes = parsed.notes
            )
            val id = if (policyId == null) {
                repository.insertPolicy(entity)
            } else {
                repository.updatePolicy(entity)
                policyId
            }
            repository.upsertPolicyCategoryDetails(parsed.details.copy(policyId = id))
            onSaved(id)
        }
        return true
    }

    fun deletePolicy(item: PolicyUiModel) {
        viewModelScope.launch {
            repository.deletePolicy(
                PolicyEntity(
                    id = item.id,
                    category = item.category.name,
                    policyHolderName = item.policyHolderName,
                    policyName = item.policyName,
                    policyNumber = item.policyNumber,
                    startDateEpochDay = item.startDateEpochDay,
                    expiryDateEpochDay = item.expiryDateEpochDay,
                    insurerName = item.insurerName,
                    previousInsurerName = item.previousInsurerName,
                    premiumAmount = item.premiumAmount,
                    notes = item.notes
                )
            )
        }
    }

    fun closePolicy(policyId: Long) {
        viewModelScope.launch {
            val current = repository.getPolicyById(policyId) ?: return@launch
            if (isPolicyClosed(current.notes)) return@launch
            repository.updatePolicy(current.copy(notes = withClosedMarker(current.notes)))
        }
    }

    fun observePolicy(policyId: Long): StateFlow<PolicyUiModel?> {
        return repository.observePolicy(policyId)
            .map { it?.toUiModel() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    }

    fun addHistory(
        policyId: Long,
        input: HistoryInput,
        attachmentUris: List<Uri> = emptyList(),
        contentResolver: ContentResolver? = null
    ): Boolean {
        val start = parseDateInput(input.startDate) ?: return false
        val end = parseDateInput(input.endDate) ?: return false
        val premium = input.premiumAmount.toDoubleOrNull() ?: return false
        if (input.insurerName.isBlank() || input.policyNumber.isBlank() || end < start) return false
        val attachmentRefs = if (attachmentUris.isNotEmpty() && contentResolver != null) {
            copyUrisToAttachmentRefs(attachmentUris, contentResolver, "history_attachments")
        } else {
            ""
        }

        viewModelScope.launch {
            repository.addHistory(
                PolicyHistoryEntity(
                    policyId = policyId,
                    policyHolderName = input.policyHolderName.trim(),
                    insurerName = input.insurerName.trim(),
                    policyNumber = input.policyNumber.trim(),
                    startDateEpochDay = start,
                    endDateEpochDay = end,
                    premiumAmount = premium,
                    attachmentRefs = attachmentRefs
                )
            )
            if (attachmentRefs.isNotBlank()) {
                parseAttachmentRefs(attachmentRefs).forEach { ref ->
                    repository.addAttachment(
                        AttachmentEntity(
                            policyId = policyId,
                            uri = ref.uri,
                            displayName = ref.displayName,
                            mimeType = ref.mimeType,
                            addedAtEpochMillis = System.currentTimeMillis()
                        )
                    )
                }
            }
        }
        return true
    }

    fun editHistory(
        historyId: Long,
        input: HistoryInput,
        attachmentUris: List<Uri> = emptyList(),
        contentResolver: ContentResolver? = null,
        onDone: (Boolean) -> Unit = {}
    ) {
        val start = parseDateInput(input.startDate) ?: run { onDone(false); return }
        val end = parseDateInput(input.endDate) ?: run { onDone(false); return }
        val premium = input.premiumAmount.toDoubleOrNull() ?: run { onDone(false); return }
        if (input.insurerName.isBlank() || input.policyNumber.isBlank() || end < start) {
            onDone(false)
            return
        }

        viewModelScope.launch {
            val current = repository.getHistoryById(historyId)
            if (current == null) {
                onDone(false)
                return@launch
            }
            val newRefs = if (attachmentUris.isNotEmpty() && contentResolver != null) {
                copyUrisToAttachmentRefs(attachmentUris, contentResolver, "history_attachments")
            } else {
                ""
            }
            repository.updateHistory(
                current.copy(
                    policyHolderName = input.policyHolderName.trim(),
                    insurerName = input.insurerName.trim(),
                    policyNumber = input.policyNumber.trim(),
                    startDateEpochDay = start,
                    endDateEpochDay = end,
                    premiumAmount = premium,
                    attachmentRefs = if (newRefs.isNotBlank()) {
                        if (current.attachmentRefs.isBlank()) newRefs
                        else listOf(current.attachmentRefs, newRefs).filter { it.isNotBlank() }.joinToString("||")
                    } else {
                        current.attachmentRefs
                    }
                )
            )
            if (newRefs.isNotBlank()) {
                parseAttachmentRefs(newRefs).forEach { ref ->
                    repository.addAttachment(
                        AttachmentEntity(
                            policyId = current.policyId,
                            uri = ref.uri,
                            displayName = ref.displayName,
                            mimeType = ref.mimeType,
                            addedAtEpochMillis = System.currentTimeMillis()
                        )
                    )
                }
            }
            onDone(true)
        }
    }

    fun deleteHistory(historyId: Long) {
        viewModelScope.launch { repository.deleteHistory(historyId) }
    }

    fun addAttachment(policyId: Long, uri: Uri, contentResolver: ContentResolver) {
        persistReadPermission(uri, contentResolver)
        val (displayName, mimeType) = resolveDocumentInfo(uri, contentResolver)
        val localPath = copyUriToAppStorage(uri, displayName, contentResolver, "policy_attachments") ?: return

        viewModelScope.launch {
            repository.addAttachment(
                AttachmentEntity(
                    policyId = policyId,
                    uri = localPath,
                    displayName = displayName,
                    mimeType = mimeType,
                    addedAtEpochMillis = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteAttachment(attachmentId: Long) {
        viewModelScope.launch { repository.deleteAttachment(attachmentId) }
    }

    fun renameAttachment(attachmentId: Long, displayName: String): Boolean {
        if (displayName.isBlank()) return false
        viewModelScope.launch { repository.renameAttachment(attachmentId, displayName.trim()) }
        return true
    }

    fun createFolder(category: PolicyCategory, input: FolderInput): Boolean {
        if (input.name.isBlank()) return false
        val today = parseFlexibleDate(formatTodayUi()) ?: return false

        viewModelScope.launch {
            repository.createFolder(
                CategoryFolderEntity(
                    category = category.name,
                    name = input.name.trim(),
                    startEpochDay = today,
                    endEpochDay = today,
                    createdAtEpochMillis = System.currentTimeMillis(),
                    colorHex = "#F6E49A"
                )
            )
        }
        return true
    }

    fun renameFolder(folderId: Long, newName: String): Boolean {
        if (newName.isBlank()) return false
        viewModelScope.launch {
            repository.renameFolder(folderId, newName.trim())
        }
        return true
    }

    fun updateFolderColor(folderId: Long, colorHex: String) {
        viewModelScope.launch {
            repository.updateFolderColor(folderId, colorHex)
        }
    }

    fun deleteFolder(folderId: Long) {
        viewModelScope.launch {
            repository.deleteFolder(folderId)
        }
    }

    fun selectLoan(loanId: Long) {
        selectedLoanId.value = loanId
    }

    fun addLoan(input: LoanInput): Boolean {
        val principal = input.principalAmount.toDoubleOrNull() ?: return false
        val rate = input.annualInterestRate.toDoubleOrNull() ?: return false
        val tenure = input.tenureMonths.toIntOrNull() ?: return false
        val startEpoch = parseDateInput(input.startDate) ?: return false
        val paymentFrequency = input.paymentFrequency.ifBlank { "Monthly" }
        if (input.loanName.isBlank() || input.lenderName.isBlank() || principal <= 0.0 || rate < 0.0 || tenure <= 0) return false

        val periodicRate = if (paymentFrequency.equals("Yearly", ignoreCase = true)) rate / 100.0 else rate / 1200.0
        val emi = if (periodicRate == 0.0) {
            principal / tenure
        } else {
            val factor = (1 + periodicRate).pow(tenure.toDouble())
            principal * periodicRate * factor / (factor - 1)
        }

        viewModelScope.launch {
            val loanId = repository.addLoan(
                com.uday.policytracker.data.db.LoanEntity(
                    loanName = input.loanName.trim(),
                    lenderName = input.lenderName.trim(),
                    principalAmount = principal,
                    annualInterestRate = rate,
                    tenureMonths = tenure,
                    emiAmount = emi,
                    paymentFrequency = paymentFrequency,
                    startDateEpochDay = startEpoch,
                    createdAtEpochMillis = System.currentTimeMillis()
                )
            )
            val firstDueEpoch = if (paymentFrequency.equals("Yearly", ignoreCase = true)) {
                LocalDate.ofEpochDay(startEpoch).plusYears(1).toEpochDay()
            } else {
                LocalDate.ofEpochDay(startEpoch).plusMonths(1).toEpochDay()
            }
            val payments = buildLoanPayments(
                loanId = loanId,
                principal = principal,
                annualRate = rate,
                tenureMonths = tenure,
                paymentFrequency = paymentFrequency,
                firstDueEpochDay = firstDueEpoch,
                emiOverride = emi
            )
            repository.addLoanPayments(payments)
            repository.autoMarkPastDueLoanPayments(LocalDate.now().toEpochDay())
            selectedLoanId.value = loanId
        }
        return true
    }

    fun editLoan(loanId: Long, input: LoanInput): Boolean {
        val principal = input.principalAmount.toDoubleOrNull() ?: return false
        val rate = input.annualInterestRate.toDoubleOrNull() ?: return false
        val tenure = input.tenureMonths.toIntOrNull() ?: return false
        val startEpoch = parseDateInput(input.startDate) ?: return false
        val paymentFrequency = input.paymentFrequency.ifBlank { "Monthly" }
        if (input.loanName.isBlank() || input.lenderName.isBlank() || principal <= 0.0 || rate < 0.0 || tenure <= 0) return false

        val periodicRate = if (paymentFrequency.equals("Yearly", ignoreCase = true)) rate / 100.0 else rate / 1200.0
        val emi = if (periodicRate == 0.0) {
            principal / tenure
        } else {
            val factor = (1 + periodicRate).pow(tenure.toDouble())
            principal * periodicRate * factor / (factor - 1)
        }

        viewModelScope.launch {
            repository.deleteLoan(loanId)
            val newLoanId = repository.addLoan(
                LoanEntity(
                    loanName = input.loanName.trim(),
                    lenderName = input.lenderName.trim(),
                    principalAmount = principal,
                    annualInterestRate = rate,
                    tenureMonths = tenure,
                    emiAmount = emi,
                    paymentFrequency = paymentFrequency,
                    startDateEpochDay = startEpoch,
                    createdAtEpochMillis = System.currentTimeMillis()
                )
            )
            val firstDueEpoch = if (paymentFrequency.equals("Yearly", ignoreCase = true)) {
                LocalDate.ofEpochDay(startEpoch).plusYears(1).toEpochDay()
            } else {
                LocalDate.ofEpochDay(startEpoch).plusMonths(1).toEpochDay()
            }
            val payments = buildLoanPayments(
                loanId = newLoanId,
                principal = principal,
                annualRate = rate,
                tenureMonths = tenure,
                paymentFrequency = paymentFrequency,
                firstDueEpochDay = firstDueEpoch,
                emiOverride = emi
            )
            repository.addLoanPayments(payments)
            repository.autoMarkPastDueLoanPayments(LocalDate.now().toEpochDay())
            selectedLoanId.value = newLoanId
        }
        return true
    }

    fun deleteLoan(loanId: Long) {
        viewModelScope.launch {
            repository.deleteLoan(loanId)
            loanEventsByLoanId.value = loanEventsByLoanId.value - loanId
            val remaining = loansState.value
            selectedLoanId.value = remaining.firstOrNull()?.id
        }
    }

    fun addMoneyLend(input: MoneyLendInput): Boolean {
        val principal = input.amount.toDoubleOrNull() ?: return false
        val rate = input.interestRate.toDoubleOrNull() ?: 0.0
        val start = parseDateInput(input.startDate) ?: return false
        val due = if (input.dueDate.isBlank()) null else parseDateInput(input.dueDate) ?: return false
        if (input.borrowerName.isBlank() || principal <= 0.0 || rate < 0.0) return false

        viewModelScope.launch {
            repository.addMoneyLend(
                MoneyLendEntity(
                    id = nextMoneyLendId(),
                    borrowerName = input.borrowerName.trim(),
                    amount = principal,
                    interestRate = rate,
                    startDateEpochDay = start,
                    dueDateEpochDay = due,
                    notes = input.notes.trim(),
                    paidInstallmentsJson = "[]",
                    isRepaid = false,
                    createdAtEpochMillis = System.currentTimeMillis()
                )
            )
        }
        return true
    }

    private fun nextMoneyLendId(): Long {
        val used = moneyLendState.value.map { it.id }.toHashSet()
        var next = moneyLendIdSeed.incrementAndGet()
        while (next in used) {
            next = moneyLendIdSeed.incrementAndGet()
        }
        return next
    }

    private suspend fun migrateLegacyMoneyLendsIfNeeded() {
        val raw = legacyMoneyLendPrefs.getString("entries_json", null).orEmpty()
        if (raw.isBlank()) return
        val existing = repository.getAllMoneyLends()
        if (existing.isNotEmpty()) return
        val parsed = runCatching {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    val paidArray = o.optJSONArray("paidInstallments")
                    val paidSet = buildSet {
                        if (paidArray != null) for (j in 0 until paidArray.length()) add(paidArray.getInt(j))
                    }
                    add(
                        MoneyLendEntity(
                            id = o.optLong("id", 0L),
                            borrowerName = o.optString("borrowerName"),
                            amount = o.optDouble("amount", 0.0),
                            interestRate = o.optDouble("interestRate", 0.0),
                            startDateEpochDay = o.optLong("startDateEpochDay", LocalDate.now().toEpochDay()),
                            dueDateEpochDay = if (o.isNull("dueDateEpochDay")) null else o.getLong("dueDateEpochDay"),
                            notes = o.optString("notes"),
                            paidInstallmentsJson = toPaidInstallmentsJson(paidSet),
                            isRepaid = o.optBoolean("isRepaid", false),
                            createdAtEpochMillis = System.currentTimeMillis()
                        )
                    )
                }
            }
        }.getOrElse { emptyList() }
        parsed.forEach { repository.addMoneyLend(it) }
        legacyMoneyLendPrefs.edit().remove("entries_json").apply()
    }

    fun editMoneyLend(entryId: Long, input: MoneyLendInput): Boolean {
        val principal = input.amount.toDoubleOrNull() ?: return false
        val rate = input.interestRate.toDoubleOrNull() ?: 0.0
        val start = parseDateInput(input.startDate) ?: return false
        val due = if (input.dueDate.isBlank()) null else parseDateInput(input.dueDate) ?: return false
        if (input.borrowerName.isBlank() || principal <= 0.0 || rate < 0.0) return false

        viewModelScope.launch {
            val current = repository.getMoneyLendById(entryId) ?: return@launch
            repository.updateMoneyLend(
                current.copy(
                    borrowerName = input.borrowerName.trim(),
                    amount = principal,
                    interestRate = rate,
                    startDateEpochDay = start,
                    dueDateEpochDay = due,
                    notes = input.notes.trim(),
                    paidInstallmentsJson = "[]",
                    isRepaid = false
                )
            )
        }
        return true
    }

    fun deleteMoneyLend(entryId: Long) {
        viewModelScope.launch { repository.deleteMoneyLend(entryId) }
    }

    fun markMoneyLendRepaid(entryId: Long) {
        viewModelScope.launch {
            val current = repository.getMoneyLendById(entryId) ?: return@launch
            repository.updateMoneyLend(current.copy(isRepaid = true))
        }
    }

    fun markMoneyLendInstallmentPaid(entryId: Long, installmentNumber: Int) {
        if (installmentNumber <= 0) return
        viewModelScope.launch {
            val current = repository.getMoneyLendById(entryId) ?: return@launch
            val updatedPaid = parsePaidInstallments(current.paidInstallmentsJson) + installmentNumber
            repository.updateMoneyLend(current.copy(paidInstallmentsJson = toPaidInstallmentsJson(updatedPaid)))
        }
    }

    fun addIncome(input: IncomeInput): Boolean {
        val amount = input.amount.toDoubleOrNull() ?: return false
        val date = parseDateInput(input.receivedDate) ?: return false
        if (input.sourceName.isBlank() || amount <= 0.0) return false

        val entry = IncomeUiModel(
            id = System.currentTimeMillis(),
            sourceName = input.sourceName.trim(),
            amount = amount,
            receivedDateEpochDay = date,
            notes = input.notes.trim()
        )
        incomeEntries.value = listOf(entry) + incomeEntries.value
        return true
    }

    fun markLoanPaymentPaid(paymentId: Long) {
        val today = parseFlexibleDate(formatTodayUi()) ?: return
        viewModelScope.launch {
            repository.markLoanPaymentPaid(paymentId, today)
        }
    }

    fun applyLoanRateChange(loanId: Long, newAnnualRate: Double, effectiveDateEpochDay: Long): Boolean {
        if (newAnnualRate < 0.0 || effectiveDateEpochDay <= 0L) return false
        viewModelScope.launch {
            val loan = repository.getLoanById(loanId) ?: return@launch
            val payments = repository.getLoanPaymentsByLoan(loanId)
            val fixedPrefix = payments.filter { it.dueDateEpochDay <= effectiveDateEpochDay }
            val tail = payments.filter { it.dueDateEpochDay > effectiveDateEpochDay }
            val unpaidCount = tail.size
            if (unpaidCount <= 0) return@launch
            val nextDue = tail.first().dueDateEpochDay
            val remainingPrincipal = tail.sumOf { it.principalComponent }.coerceAtLeast(0.0)
            val periodicRate = if (loan.paymentFrequency.equals("Yearly", ignoreCase = true)) newAnnualRate / 100.0 else newAnnualRate / 1200.0
            val emi = if (periodicRate == 0.0) remainingPrincipal / unpaidCount
            else {
                val factor = (1 + periodicRate).pow(unpaidCount.toDouble())
                remainingPrincipal * periodicRate * factor / (factor - 1)
            }

            val rebuiltUnpaid = buildLoanPayments(
                loanId = loanId,
                principal = remainingPrincipal,
                annualRate = newAnnualRate,
                tenureMonths = unpaidCount,
                paymentFrequency = loan.paymentFrequency,
                firstDueEpochDay = nextDue,
                emiOverride = emi,
                startInstallmentNumber = fixedPrefix.size + 1,
                dueDateEpochDays = tail.map { it.dueDateEpochDay },
                previousDateEpochDay = fixedPrefix.lastOrNull()?.dueDateEpochDay ?: loan.startDateEpochDay
            )
            repository.updateLoan(loan.copy(annualInterestRate = newAnnualRate, emiAmount = emi))
            repository.replaceLoanPayments(loanId, fixedPrefix + rebuiltUnpaid)
            repository.autoMarkPastDueLoanPayments(LocalDate.now().toEpochDay())
            appendLoanEvent(
                loanId = loanId,
                event = LoanEventUiModel(
                    id = System.currentTimeMillis(),
                    loanId = loanId,
                    dateEpochDay = effectiveDateEpochDay,
                    type = LoanEventType.RATE_CHANGE,
                    amount = null,
                    oldRate = loan.annualInterestRate,
                    newRate = newAnnualRate,
                    oldEmi = loan.emiAmount,
                    newEmi = emi,
                    note = "Interest rate updated"
                )
            )
        }
        return true
    }

    fun applyLoanPrepayment(loanId: Long, amount: Double, effectiveDateEpochDay: Long): Boolean {
        if (amount <= 0.0 || effectiveDateEpochDay <= 0L) return false
        viewModelScope.launch {
            val loan = repository.getLoanById(loanId) ?: return@launch
            val payments = repository.getLoanPaymentsByLoan(loanId)
            val fixedPrefix = payments.filter { it.dueDateEpochDay <= effectiveDateEpochDay }
            val tail = payments.filter { it.dueDateEpochDay > effectiveDateEpochDay }
            val unpaidCount = tail.size
            if (unpaidCount <= 0) return@launch
            val nextDue = tail.first().dueDateEpochDay
            val currentRemaining = tail.sumOf { it.principalComponent }.coerceAtLeast(0.0)
            val remainingPrincipal = (currentRemaining - amount).coerceAtLeast(0.0)
            appendLoanEvent(
                loanId = loanId,
                event = LoanEventUiModel(
                    id = System.currentTimeMillis(),
                    loanId = loanId,
                    dateEpochDay = effectiveDateEpochDay,
                    type = LoanEventType.PREPAYMENT,
                    amount = amount,
                    oldRate = loan.annualInterestRate,
                    newRate = loan.annualInterestRate,
                    oldEmi = loan.emiAmount,
                    newEmi = null,
                    note = "Prepayment applied"
                )
            )

            if (remainingPrincipal <= 0.0) {
                closeLoan(loanId, "Pre-closed")
                return@launch
            }

            val periodicRate = if (loan.paymentFrequency.equals("Yearly", ignoreCase = true)) loan.annualInterestRate / 100.0 else loan.annualInterestRate / 1200.0
            val emi = if (periodicRate == 0.0) remainingPrincipal / unpaidCount
            else {
                val factor = (1 + periodicRate).pow(unpaidCount.toDouble())
                remainingPrincipal * periodicRate * factor / (factor - 1)
            }
            val rebuiltUnpaid = buildLoanPayments(
                loanId = loanId,
                principal = remainingPrincipal,
                annualRate = loan.annualInterestRate,
                tenureMonths = unpaidCount,
                paymentFrequency = loan.paymentFrequency,
                firstDueEpochDay = nextDue,
                emiOverride = emi,
                startInstallmentNumber = fixedPrefix.size + 1,
                dueDateEpochDays = tail.map { it.dueDateEpochDay },
                previousDateEpochDay = fixedPrefix.lastOrNull()?.dueDateEpochDay ?: loan.startDateEpochDay
            )
            repository.updateLoan(loan.copy(emiAmount = emi))
            repository.replaceLoanPayments(loanId, fixedPrefix + rebuiltUnpaid)
            repository.autoMarkPastDueLoanPayments(LocalDate.now().toEpochDay())
        }
        return true
    }

    fun closeLoan(loanId: Long, reason: String = "Closed") {
        viewModelScope.launch {
            val loan = repository.getLoanById(loanId) ?: return@launch
            val payments = repository.getLoanPaymentsByLoan(loanId)
            val closedSnapshot = ClosedLoanUiModel(
                id = System.currentTimeMillis(),
                loan = LoanUiModel(
                    id = loan.id,
                    loanName = loan.loanName,
                    lenderName = loan.lenderName,
                    principalAmount = loan.principalAmount,
                    annualInterestRate = loan.annualInterestRate,
                    tenureMonths = loan.tenureMonths,
                    emiAmount = loan.emiAmount,
                    paymentFrequency = loan.paymentFrequency,
                    startDateEpochDay = loan.startDateEpochDay
                ),
                closedOnEpochDay = LocalDate.now().toEpochDay(),
                closeReason = reason,
                events = loanEventsByLoanId.value[loanId].orEmpty(),
                payments = payments.map {
                    LoanPaymentUiModel(
                        id = it.id,
                        installmentNumber = it.installmentNumber,
                        dueDateEpochDay = it.dueDateEpochDay,
                        amountDue = it.amountDue,
                        principalComponent = it.principalComponent,
                        interestComponent = it.interestComponent,
                        isPaid = it.isPaid,
                        paidOnEpochDay = it.paidOnEpochDay
                    )
                }
            )
            closedLoans.value = listOf(closedSnapshot) + closedLoans.value
            repository.deleteLoan(loanId)
            loanEventsByLoanId.value = loanEventsByLoanId.value - loanId
            selectedLoanId.value = loansState.value.firstOrNull { it.id != loanId }?.id
        }
    }

    private suspend fun autoMarkPastLoanPayments() {
        repository.autoMarkPastDueLoanPayments(LocalDate.now().toEpochDay())
    }

    private suspend fun ensureCreditCardLoanSeeded() {
        val existing = repository.getAllLoans()
        val alreadyPresent = existing.any {
            it.loanName.equals("Pranay HDFC Credit Card Loan", ignoreCase = true)
        }
        if (alreadyPresent) return

        val startDate = LocalDate.of(2022, 6, 17).toEpochDay()
        val principal = 312000.0
        val rate = 0.0
        val tenureMonths = 60
        val emi = 5200.0
        val loanId = repository.addLoan(
            LoanEntity(
                loanName = "Pranay HDFC Credit Card Loan",
                lenderName = "HDFC",
                principalAmount = principal,
                annualInterestRate = rate,
                tenureMonths = tenureMonths,
                emiAmount = emi,
                paymentFrequency = "Monthly",
                startDateEpochDay = startDate,
                createdAtEpochMillis = System.currentTimeMillis()
            )
        )
        val firstDueEpoch = LocalDate.ofEpochDay(startDate).plusMonths(1).toEpochDay()
        val payments = buildLoanPayments(
            loanId = loanId,
            principal = principal,
            annualRate = rate,
            tenureMonths = tenureMonths,
            paymentFrequency = "Monthly",
            firstDueEpochDay = firstDueEpoch,
            emiOverride = emi
        )
        repository.addLoanPayments(payments)
        repository.autoMarkPastDueLoanPayments(LocalDate.now().toEpochDay())
    }

    private fun buildLoanPayments(
        loanId: Long,
        principal: Double,
        annualRate: Double,
        tenureMonths: Int,
        paymentFrequency: String,
        firstDueEpochDay: Long,
        emiOverride: Double? = null,
        startInstallmentNumber: Int = 1,
        dueDateEpochDays: List<Long>? = null,
        previousDateEpochDay: Long? = null
    ): List<LoanPaymentEntity> {
        val effectiveTenure = dueDateEpochDays?.size ?: tenureMonths
        if (effectiveTenure <= 0 || principal <= 0.0) return emptyList()
        val periodicRate = if (paymentFrequency.equals("Yearly", ignoreCase = true)) annualRate / 100.0 else annualRate / 1200.0
        val emi = (emiOverride ?: if (periodicRate == 0.0) {
            principal / effectiveTenure
        } else {
            val factor = (1 + periodicRate).pow(effectiveTenure.toDouble())
            principal * periodicRate * factor / (factor - 1)
        }).let { kotlin.math.round(it * 100.0) / 100.0 }
        var remaining = principal
        val today = LocalDate.now().toEpochDay()
        var previousDate = previousDateEpochDay ?: if (paymentFrequency.equals("Yearly", ignoreCase = true)) {
            LocalDate.ofEpochDay(firstDueEpochDay).minusYears(1).toEpochDay()
        } else {
            LocalDate.ofEpochDay(firstDueEpochDay).minusMonths(1).toEpochDay()
        }
        return (1..effectiveTenure).map { offset ->
            val dueDate = dueDateEpochDays?.get(offset - 1)
                ?: if (paymentFrequency.equals("Yearly", ignoreCase = true)) {
                    LocalDate.ofEpochDay(firstDueEpochDay).plusYears((offset - 1).toLong()).toEpochDay()
                } else {
                    LocalDate.ofEpochDay(firstDueEpochDay).plusMonths((offset - 1).toLong()).toEpochDay()
                }
            val dayDiff = (dueDate - previousDate).coerceAtLeast(1L)
            val interest = if (annualRate == 0.0) 0.0 else round((remaining * annualRate * dayDiff / 36500.0) * 100.0) / 100.0
            var principalPart = (emi - interest).coerceAtLeast(0.0).coerceAtMost(remaining)
            if (offset == effectiveTenure) {
                principalPart = remaining
            }
            remaining = (remaining - principalPart).coerceAtLeast(0.0)
            previousDate = dueDate
            LoanPaymentEntity(
                loanId = loanId,
                installmentNumber = startInstallmentNumber + offset - 1,
                dueDateEpochDay = dueDate,
                amountDue = if (offset == effectiveTenure) round((principalPart + interest) * 100.0) / 100.0 else emi,
                principalComponent = principalPart,
                interestComponent = interest,
                isPaid = dueDate < today,
                paidOnEpochDay = if (dueDate < today) today else null
            )
        }
    }

    private fun appendLoanEvent(loanId: Long, event: LoanEventUiModel) {
        val existing = loanEventsByLoanId.value[loanId].orEmpty()
        loanEventsByLoanId.value = loanEventsByLoanId.value + (loanId to (existing + event))
    }

    fun renewPolicy(
        policyId: Long,
        input: RenewalInput,
        attachmentUris: List<Uri> = emptyList(),
        contentResolver: ContentResolver? = null,
        onDone: (Boolean) -> Unit = {}
    ) {
        val start = parseDateInput(input.startDate) ?: run { onDone(false); return }
        val end = parseDateInput(input.expiryDate) ?: run { onDone(false); return }
        val premium = input.premiumAmount.toDoubleOrNull() ?: 0.0
        if (input.policyName.isBlank() || input.policyNumber.isBlank() || input.insurerName.isBlank() || end < start) {
            onDone(false)
            return
        }
        val attachmentRefs = if (attachmentUris.isNotEmpty() && contentResolver != null) {
            copyUrisToAttachmentRefs(attachmentUris, contentResolver, "future_attachments")
        } else {
            ""
        }
        val today = LocalDate.now().toEpochDay()

        viewModelScope.launch {
            val current = repository.getPolicyById(policyId)
            if (current == null) {
                onDone(false)
                return@launch
            }
            if (start > today) {
                repository.addFuturePolicy(
                    FuturePolicyEntity(
                        policyId = current.id,
                        policyHolderName = input.policyHolderName.trim(),
                        policyName = input.policyName.trim(),
                        policyNumber = input.policyNumber.trim(),
                        startDateEpochDay = start,
                        expiryDateEpochDay = end,
                        insurerName = input.insurerName.trim(),
                        premiumAmount = premium,
                        createdAtEpochMillis = System.currentTimeMillis(),
                        attachmentRefs = attachmentRefs
                    )
                )
                if (attachmentRefs.isNotBlank()) {
                    parseAttachmentRefs(attachmentRefs).forEach { ref ->
                        repository.addAttachment(
                            AttachmentEntity(
                                policyId = current.id,
                                uri = ref.uri,
                                displayName = ref.displayName,
                                mimeType = ref.mimeType,
                                addedAtEpochMillis = System.currentTimeMillis()
                            )
                        )
                    }
                }
                onDone(true)
                return@launch
            }

            val currentRefs = repository.getPolicyWithDetails(policyId)?.attachments
                ?.joinToString("||") { "${it.displayName}::${it.uri}" }
                .orEmpty()
            repository.addHistory(
                PolicyHistoryEntity(
                    policyId = current.id,
                    policyHolderName = current.policyHolderName,
                    insurerName = current.insurerName,
                    policyNumber = current.policyNumber,
                    startDateEpochDay = current.startDateEpochDay,
                    endDateEpochDay = current.expiryDateEpochDay,
                    premiumAmount = current.premiumAmount,
                    attachmentRefs = currentRefs
                )
            )
            repository.updatePolicy(
                current.copy(
                    policyHolderName = input.policyHolderName.trim(),
                    policyName = input.policyName.trim(),
                    policyNumber = input.policyNumber.trim(),
                    startDateEpochDay = start,
                    expiryDateEpochDay = end,
                    insurerName = input.insurerName.trim(),
                    previousInsurerName = current.insurerName,
                    premiumAmount = premium
                )
            )
            if (attachmentRefs.isNotBlank()) {
                parseAttachmentRefs(attachmentRefs).forEach { futureAttachment ->
                    repository.addAttachment(
                        AttachmentEntity(
                            policyId = current.id,
                            uri = futureAttachment.uri,
                            displayName = futureAttachment.displayName,
                            mimeType = futureAttachment.mimeType,
                            addedAtEpochMillis = System.currentTimeMillis()
                        )
                    )
                }
            }
            onDone(true)
        }
    }

    fun observeCategoryFolders(category: PolicyCategory): StateFlow<ServiceCategoryUiState> {
        return combine(
            repository.observeFoldersByCategory(category.name),
            repository.observePoliciesByCategory(category.name)
        ) { folders, policies ->
            ServiceCategoryUiState(
                category = category,
                folders = folders.map { it.toFolderUiModel() },
                policies = policies.map { it.toCompactPolicy() }
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ServiceCategoryUiState(category = category))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeFolderDetail(folderId: Long): StateFlow<FolderDetailUiState> {
        val folderFlow = repository.observeFolderById(folderId)
        val policyDocsFlow = repository.observePolicyAttachmentsByFolder(folderId)

        return combine(folderFlow, policyDocsFlow) { folder, policyDocs ->
            if (folder == null) {
                FolderDetailUiState()
            } else {
                FolderDetailUiState(
                    folder = folder.toFolderUiModel(),
                    policiesInCategory = emptyList(),
                    policyDocuments = policyDocs.map {
                        FolderPolicyDocumentUiModel(
                            attachmentId = it.attachmentId,
                            policyId = it.policyId,
                            policyName = it.policyName,
                            policyNumber = it.policyNumber,
                            policyStartEpochDay = it.policyStartEpochDay,
                            policyExpiryEpochDay = it.policyExpiryEpochDay,
                            attachment = AttachmentUiModel(
                                id = it.attachmentId,
                                uri = it.uri,
                                displayName = it.displayName,
                                mimeType = it.mimeType,
                                addedAtEpochMillis = it.addedAtEpochMillis
                            )
                        )
                    }
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FolderDetailUiState())
    }

    fun observeFolderTags(category: PolicyCategory): StateFlow<List<FolderTagUiModel>> {
        return repository.observeFoldersByCategory(category.name)
            .map { folders ->
                folders.sortedByDescending { it.folder.createdAtEpochMillis }.map {
                    FolderTagUiModel(id = it.folder.id, name = it.folder.name, colorHex = it.folder.colorHex)
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    }

    fun observePolicyFolderTags(policyId: Long): StateFlow<Set<Long>> {
        return repository.observePolicyFolderIds(policyId)
            .map { it.toSet() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())
    }

    fun setPolicyFolderTags(policyId: Long, folderIds: Set<Long>) {
        viewModelScope.launch {
            repository.replacePolicyFolderTags(policyId, folderIds)
        }
    }

    fun togglePolicyFolderTag(folderId: Long, policyId: Long, currentlyTagged: Boolean) {
        viewModelScope.launch {
            if (currentlyTagged) {
                repository.removePolicyFromFolder(policyId, folderId)
            } else {
                repository.addPolicyToFolder(policyId, folderId)
            }
        }
    }

    fun addFolderAttachment(
        folderId: Long,
        uri: Uri,
        contentResolver: ContentResolver,
        displayNameOverride: String? = null
    ) {
        persistReadPermission(uri, contentResolver)
        val (displayName, mimeType) = resolveDocumentInfo(uri, contentResolver)
        val effectiveDisplayName = displayNameOverride?.takeIf { it.isNotBlank() } ?: displayName
        val localPath = copyUriToAppStorage(uri, effectiveDisplayName, contentResolver, "folder_attachments") ?: return

        viewModelScope.launch {
            repository.addFolderAttachment(
                FolderAttachmentEntity(
                    folderId = folderId,
                    uri = localPath,
                    displayName = effectiveDisplayName,
                    mimeType = mimeType,
                    addedAtEpochMillis = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteFolderAttachment(attachmentId: Long) {
        viewModelScope.launch { repository.deleteFolderAttachment(attachmentId) }
    }

    fun renameFolderAttachment(attachmentId: Long, displayName: String): Boolean {
        if (displayName.isBlank()) return false
        viewModelScope.launch { repository.renameFolderAttachment(attachmentId, displayName.trim()) }
        return true
    }

    fun exportBackup(targetUri: Uri, resolver: ContentResolver, onDone: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            runCatching {
                val attachments = repository.getAllAttachments()
                val folderAttachments = repository.getAllFolderAttachments()
                val history = repository.getAllHistory()
                val futurePolicies = repository.getAllFuturePolicies()
                val policyCategoryDetails = repository.getAllPolicyCategoryDetails()
                val fileCandidates = collectBackupFileCandidates(attachments, folderAttachments, history, futurePolicies)
                val fileManifest = JSONObject().apply {
                    fileCandidates.forEach { put(it.oldPath, it.entryName) }
                }
                val root = JSONObject().apply {
                    put("backupVersion", BACKUP_FORMAT_VERSION)
                    put("backupContainer", "zip")
                    put("createdAtEpochMillis", System.currentTimeMillis())
                    put("policies", JSONArray().apply { repository.getAllPolicies().forEach { put(it.toJson()) } })
                    put("history", JSONArray().apply { history.forEach { put(it.toJson()) } })
                    put("attachments", JSONArray().apply { attachments.forEach { put(it.toJson()) } })
                    put("futurePolicies", JSONArray().apply { futurePolicies.forEach { put(it.toJson()) } })
                    put("policyCategoryDetails", JSONArray().apply { policyCategoryDetails.forEach { put(it.toJson()) } })
                    put("folders", JSONArray().apply { repository.getAllFolders().forEach { put(it.toJson()) } })
                    put("folderRefs", JSONArray().apply { repository.getAllPolicyFolderRefs().forEach { put(it.toJson()) } })
                    put("folderAttachments", JSONArray().apply { folderAttachments.forEach { put(it.toJson()) } })
                    put("loans", JSONArray().apply { repository.getAllLoans().forEach { put(it.toJson()) } })
                    put("loanPayments", JSONArray().apply { repository.getAllLoanPayments().forEach { put(it.toJson()) } })
                    put("moneyLends", JSONArray().apply { repository.getAllMoneyLends().forEach { put(it.toJson()) } })
                    put("filesManifest", fileManifest)
                }
                resolver.openOutputStream(targetUri)?.use { rawOut ->
                    ZipOutputStream(BufferedOutputStream(rawOut)).use { zipOut ->
                        zipOut.putNextEntry(ZipEntry(BACKUP_METADATA_ENTRY))
                        zipOut.write(root.toString().toByteArray(StandardCharsets.UTF_8))
                        zipOut.closeEntry()

                        fileCandidates.forEach { item ->
                            zipOut.putNextEntry(ZipEntry(item.entryName))
                            FileInputStream(item.file).use { input -> input.copyTo(zipOut) }
                            zipOut.closeEntry()
                        }
                        zipOut.finish()
                    }
                } ?: error("Cannot write backup")
            }.fold(onSuccess = { onDone(true) }, onFailure = { onDone(false) })
        }
    }

    fun importBackup(sourceUri: Uri, resolver: ContentResolver, onDone: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val success = try {
                resolver.openInputStream(sourceUri)?.use { rawIn ->
                    BufferedInputStream(rawIn).use { input ->
                        input.mark(8)
                        val signature = ByteArray(4)
                        val readCount = input.read(signature)
                        input.reset()
                        val isZip = readCount == 4 &&
                            signature[0] == 'P'.code.toByte() &&
                            signature[1] == 'K'.code.toByte() &&
                            signature[2] == 0x03.toByte() &&
                            signature[3] == 0x04.toByte()

                        if (isZip) {
                            importZipBackup(input)
                        } else {
                            val jsonText = input.bufferedReader().use { it.readText() }
                            importLegacyJsonBackup(JSONObject(jsonText))
                        }
                    }
                } ?: error("Cannot read backup")
                true
            } catch (_: Throwable) {
                false
            }
            onDone(success)
        }
    }

    private fun collectBackupFileCandidates(
        attachments: List<AttachmentEntity>,
        folderAttachments: List<FolderAttachmentEntity>,
        history: List<PolicyHistoryEntity>,
        futurePolicies: List<FuturePolicyEntity>
    ): List<BackupFileCandidate> {
        val paths = linkedSetOf<String>()
        attachments.forEach { paths.add(it.uri) }
        folderAttachments.forEach { paths.add(it.uri) }
        history.forEach { parseAttachmentRefs(it.attachmentRefs).forEach { ref -> paths.add(ref.uri) } }
        futurePolicies.forEach { parseAttachmentRefs(it.attachmentRefs).forEach { ref -> paths.add(ref.uri) } }

        val result = mutableListOf<BackupFileCandidate>()
        var index = 0
        paths.forEach { oldPath ->
            if (!oldPath.startsWith("/")) return@forEach
            val file = File(oldPath)
            if (!file.exists() || !file.isFile) return@forEach
            val safeName = file.name.replace(Regex("[^A-Za-z0-9._-]"), "_").ifBlank { "file_$index" }
            val entryName = "$BACKUP_FILES_PREFIX${index}_$safeName"
            result += BackupFileCandidate(
                oldPath = oldPath,
                file = file,
                entryName = entryName
            )
            index += 1
        }
        return result
    }

    private suspend fun importZipBackup(input: BufferedInputStream) {
        val restoredFiles = mutableMapOf<String, String>()
        var root: JSONObject? = null
        var entryToOldPath: Map<String, String> = emptyMap()

        ZipInputStream(input).use { zipIn ->
            var entry = zipIn.nextEntry
            while (entry != null) {
                when {
                    entry.name == BACKUP_METADATA_ENTRY -> {
                        val jsonText = readCurrentZipEntryText(zipIn)
                        val metadata = JSONObject(jsonText)
                        root = metadata
                        entryToOldPath = metadata.optJSONObject("filesManifest")
                            .toBackupFileManifest()
                            .entries
                            .associate { (oldPath, entryName) -> entryName to oldPath }
                    }
                    entry.isDirectory -> Unit
                    else -> {
                        val oldPath = entryToOldPath[entry.name]
                        if (oldPath != null) {
                            val target = allocateRestoredFile(oldPath)
                            FileOutputStream(target).use { output -> zipIn.copyTo(output) }
                            restoredFiles[oldPath] = target.absolutePath
                        }
                    }
                }
                zipIn.closeEntry()
                entry = zipIn.nextEntry
            }
        }

        val metadata = root ?: error("Invalid backup: metadata missing")
        restoreFromRoot(metadata, restoredFiles)
    }

    private fun readCurrentZipEntryText(zipIn: ZipInputStream): String {
        val out = ByteArrayOutputStream()
        val buffer = ByteArray(8 * 1024)
        while (true) {
            val read = zipIn.read(buffer)
            if (read <= 0) break
            out.write(buffer, 0, read)
        }
        return out.toString(StandardCharsets.UTF_8.name())
    }

    private suspend fun importLegacyJsonBackup(root: JSONObject) {
        val restoredFiles = restoreLegacyEmbeddedFiles(root.optJSONObject("files"))
        restoreFromRoot(root, restoredFiles)
    }

    private suspend fun restoreFromRoot(root: JSONObject, restoredFiles: Map<String, String>) {
        val attachments = root.optJSONArray("attachments").toAttachments().map {
            it.copy(uri = restoredFiles[it.uri] ?: it.uri)
        }
        val folderAttachments = root.optJSONArray("folderAttachments").toFolderAttachments().map {
            it.copy(uri = restoredFiles[it.uri] ?: it.uri)
        }
        val history = root.optJSONArray("history").toHistory().map {
            it.copy(attachmentRefs = remapAttachmentRefs(it.attachmentRefs, restoredFiles))
        }
        val futurePolicies = root.optJSONArray("futurePolicies").toFuturePolicies().map {
            it.copy(attachmentRefs = remapAttachmentRefs(it.attachmentRefs, restoredFiles))
        }
        val policyCategoryDetails = root.optJSONArray("policyCategoryDetails").toPolicyCategoryDetails()
        repository.restoreAll(
            policies = root.optJSONArray("policies").toPolicies(),
            history = history,
            attachments = attachments,
            futurePolicies = futurePolicies,
            policyCategoryDetails = policyCategoryDetails,
            folders = root.optJSONArray("folders").toFolders(),
            refs = root.optJSONArray("folderRefs").toFolderRefs(),
            folderAttachments = folderAttachments,
            loans = root.optJSONArray("loans").toLoans(),
            loanPayments = root.optJSONArray("loanPayments").toLoanPayments(),
            moneyLends = root.optJSONArray("moneyLends").toMoneyLends()
        )
    }

    private fun restoreLegacyEmbeddedFiles(filesObject: JSONObject?): Map<String, String> {
        if (filesObject == null) return emptyMap()
        val result = mutableMapOf<String, String>()
        val keys = filesObject.keys()
        while (keys.hasNext()) {
            val oldPath = keys.next()
            val encoded = filesObject.optString(oldPath)
            if (encoded.isBlank()) continue
            val bytes = runCatching { android.util.Base64.decode(encoded, android.util.Base64.DEFAULT) }.getOrNull() ?: continue
            val target = allocateRestoredFile(oldPath)
            runCatching {
                FileOutputStream(target).use { it.write(bytes) }
                result[oldPath] = target.absolutePath
            }
        }
        return result
    }

    private fun allocateRestoredFile(oldPath: String): File {
        val appFiles = getApplication<Application>().filesDir
        val oldFile = File(oldPath)
        val parentName = oldFile.parentFile?.name?.replace(Regex("[^A-Za-z0-9._-]"), "_")
            ?.ifBlank { "restored_files" } ?: "restored_files"
        val folder = File(appFiles, parentName)
        if (!folder.exists()) folder.mkdirs()
        val originalName = oldFile.name.ifBlank { "restored_file" }
        val sanitized = originalName.replace(Regex("[^A-Za-z0-9._-]"), "_")
        var target = File(folder, sanitized)
        var counter = 1
        while (target.exists()) {
            val dot = sanitized.lastIndexOf('.')
            val namePart = if (dot > 0) sanitized.substring(0, dot) else sanitized
            val extPart = if (dot > 0) sanitized.substring(dot) else ""
            target = File(folder, "${namePart}_$counter$extPart")
            counter += 1
        }
        return target
    }

    private fun remapAttachmentRefs(raw: String, fileMap: Map<String, String>): String {
        if (raw.isBlank()) return raw
        return raw.split("||").mapNotNull { part ->
            val pieces = part.split("::")
            if (pieces.size < 2) return@mapNotNull null
            val name = pieces.first()
            val oldUri = pieces.drop(1).joinToString("::")
            val newUri = fileMap[oldUri] ?: oldUri
            "$name::$newUri"
        }.joinToString("||")
    }

    private fun persistReadPermission(uri: Uri, resolver: ContentResolver) {
        val flags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or android.content.Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        try {
            resolver.takePersistableUriPermission(uri, flags)
        } catch (_: Exception) {
        }
    }

    private fun resolveDocumentInfo(uri: Uri, resolver: ContentResolver): Pair<String, String> {
        var name = uri.lastPathSegment?.substringAfterLast('/') ?: "Document"
        runCatching {
            resolver.query(uri, null, null, null, null)?.use { cursor ->
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1 && cursor.moveToFirst()) {
                    name = cursor.getString(index) ?: name
                }
            }
        }
        return name to (resolver.getType(uri) ?: "application/octet-stream")
    }

    private fun copyUriToAppStorage(
        uri: Uri,
        displayName: String,
        resolver: ContentResolver,
        folderName: String
    ): String? {
        return runCatching {
            val folder = File(getApplication<Application>().filesDir, folderName)
            if (!folder.exists()) folder.mkdirs()
            val file = File(folder, "${System.currentTimeMillis()}_${displayName.replace(Regex("[^A-Za-z0-9._-]"), "_")}")
            resolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output -> input.copyTo(output) }
            } ?: return null
            file.absolutePath
        }.getOrNull()
    }

    private fun copyUrisToAttachmentRefs(
        uris: List<Uri>,
        resolver: ContentResolver,
        folderName: String
    ): String {
        return uris.mapNotNull { uri ->
            val (displayName, _) = resolveDocumentInfo(uri, resolver)
            val copiedPath = copyUriToAppStorage(uri, displayName, resolver, folderName) ?: return@mapNotNull null
            "${displayName.replace("::", "_")}::$copiedPath"
        }.joinToString("||")
    }

    private fun parsePolicy(input: PolicyInput): ParsedPolicy? {
        val start = parseDateInput(input.startDate) ?: return null
        val userExpiry = parseDateInput(input.expiryDate) ?: return null
        val premium = input.premiumAmount.toDoubleOrNull() ?: 0.0
        if (input.policyHolderName.isBlank() || userExpiry < start) return null

        val resolvedDocumentType = if (input.category == PolicyCategory.DRIVING_LICENCE) {
            if (input.documentType == "Custom") input.customDocumentType.trim() else input.documentType.trim()
        } else {
            input.documentType.trim()
        }
        val normalizedPolicyName = when (input.category) {
            PolicyCategory.DRIVING_LICENCE -> resolvedDocumentType.ifBlank { "Document" }
            PolicyCategory.VEHICLE_INSURANCE -> input.policyName.ifBlank { "Vehicle Policy" }.trim()
            else -> input.policyName.trim()
        }
        val normalizedPolicyNumber = input.policyNumber.trim()
        val normalizedInsurer = when (input.category) {
            PolicyCategory.DRIVING_LICENCE -> input.insurerName.ifBlank { "RTO" }.trim()
            else -> input.insurerName.trim()
        }

        val hasMinimum = when (input.category) {
            PolicyCategory.HEALTH_INSURANCE -> normalizedPolicyName.isNotBlank() && normalizedPolicyNumber.isNotBlank() && normalizedInsurer.isNotBlank()
            PolicyCategory.TERM_INSURANCE -> normalizedPolicyName.isNotBlank() && normalizedPolicyNumber.isNotBlank() && normalizedInsurer.isNotBlank()
            PolicyCategory.VEHICLE_INSURANCE -> input.vehicleNumber.isNotBlank() && normalizedPolicyNumber.isNotBlank() && normalizedInsurer.isNotBlank()
            PolicyCategory.DRIVING_LICENCE -> resolvedDocumentType.isNotBlank() && normalizedPolicyNumber.isNotBlank()
            else -> normalizedPolicyName.isNotBlank() && normalizedPolicyNumber.isNotBlank()
        }
        if (!hasMinimum) return null

        val normalizedFrequency = input.premiumFrequency.trim()
        val paymentStartEpoch = parseDateInput(input.premiumPaymentStartDate) ?: start
        val paymentEndEpoch = parseDateInput(input.premiumPaymentEndDate) ?: userExpiry
        if (paymentEndEpoch < paymentStartEpoch) return null

        val entryAge = input.entryAge.toIntOrNull()
        val coverageTillAge = input.coverageTillAge.toIntOrNull()
        val inputTermYears = input.policyTermYears.toIntOrNull()
        val inputPremiumPaymentTermYears = input.premiumPaymentTermYears.toIntOrNull()

        val computedTermYears = when {
            entryAge != null && coverageTillAge != null && coverageTillAge >= entryAge -> coverageTillAge - entryAge
            inputTermYears != null && inputTermYears >= 0 -> inputTermYears
            else -> null
        }
        val computedCoverageTillAge = when {
            entryAge != null && computedTermYears != null -> entryAge + computedTermYears
            else -> coverageTillAge
        }
        val computedPolicyValidityExpiry = when {
            input.category == PolicyCategory.TERM_INSURANCE && computedTermYears != null && computedTermYears > 0 ->
                LocalDate.ofEpochDay(start).plusYears(computedTermYears.toLong()).toEpochDay()
            else -> userExpiry
        }
        if (computedPolicyValidityExpiry < start) return null

        val computedPaymentTermYears = when {
            input.category != PolicyCategory.TERM_INSURANCE -> null
            inputPremiumPaymentTermYears != null && inputPremiumPaymentTermYears >= 0 -> inputPremiumPaymentTermYears
            else -> null
        }
        val computedPaymentEndEpoch = when {
            input.category != PolicyCategory.TERM_INSURANCE -> paymentEndEpoch
            computedPaymentTermYears != null -> LocalDate.ofEpochDay(paymentStartEpoch).plusYears(computedPaymentTermYears.toLong()).toEpochDay()
            else -> paymentEndEpoch
        }
        if (computedPaymentEndEpoch < paymentStartEpoch) return null

        val (computedTotalPayments, computedPaidPayments) = computePaymentStats(
            startEpochDay = if (input.category == PolicyCategory.TERM_INSURANCE) paymentStartEpoch else start,
            endEpochDay = if (input.category == PolicyCategory.TERM_INSURANCE) computedPaymentEndEpoch else computedPolicyValidityExpiry,
            frequency = normalizedFrequency
        )

        return ParsedPolicy(
            category = input.category,
            policyHolderName = input.policyHolderName.trim(),
            policyName = normalizedPolicyName,
            policyNumber = normalizedPolicyNumber,
            startDate = start,
            expiryDate = computedPolicyValidityExpiry,
            insurerName = normalizedInsurer,
            previousInsurerName = input.previousInsurerName.trim(),
            premiumAmount = premium,
            notes = input.notes.trim(),
            details = PolicyCategoryDetailsEntity(
                policyId = 0,
                premiumFrequency = normalizedFrequency,
                premiumDueDayOfMonth = input.premiumDueDayOfMonth.toIntOrNull(),
                coverageAmount = input.coverageAmount.toDoubleOrNull(),
                coverageAmountUnit = input.coverageAmountUnit.trim(),
                premiumPaymentStartEpochDay = if (input.category == PolicyCategory.TERM_INSURANCE) paymentStartEpoch else start,
                premiumPaymentEndEpochDay = if (input.category == PolicyCategory.TERM_INSURANCE) computedPaymentEndEpoch else computedPolicyValidityExpiry,
                premiumPaymentTermYears = if (input.category == PolicyCategory.TERM_INSURANCE) computedPaymentTermYears else null,
                policyValidityEndEpochDay = computedPolicyValidityExpiry,
                policyTermYears = computedTermYears,
                entryAge = entryAge,
                coverageTillAge = computedCoverageTillAge,
                nomineeName = input.nomineeName.trim(),
                nomineeRelationship = input.nomineeRelationship.trim(),
                riderAddons = input.riderAddons.trim(),
                paymentMode = input.paymentMode.trim(),
                gracePeriodDays = input.gracePeriodDays.toIntOrNull(),
                termPolicyStatus = input.termPolicyStatus.trim(),
                totalPayments = computedTotalPayments,
                paidPayments = computedPaidPayments,
                vehicleNumber = input.vehicleNumber.trim(),
                vehicleType = input.vehicleType.trim(),
                makeModelVariant = input.makeModelVariant.trim(),
                fuelType = input.fuelType.trim(),
                vehiclePolicyType = input.vehiclePolicyType.trim(),
                vehicleAddons = input.vehicleAddons.trim(),
                claimHistory = input.claimHistory.trim(),
                documentType = resolvedDocumentType,
                issuingRto = input.issuingRto.trim(),
                stateName = input.stateName.trim(),
                vehicleClass = input.vehicleClass.trim(),
                ownerName = input.ownerName.trim(),
                linkedVehicleNumber = input.linkedVehicleNumber.trim(),
                dateOfBirth = input.dateOfBirth.trim(),
                customFieldValuesJson = input.customFieldValuesJson.trim()
            )
        )
    }

    private fun parseDateInput(value: String): Long? = parseFlexibleDate(value)

    private fun computePaymentStats(startEpochDay: Long, endEpochDay: Long, frequency: String): Pair<Int?, Int?> {
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

    private suspend fun applyDueFuturePolicies() {
        val today = parseFlexibleDate(formatTodayUi()) ?: return
        val detailsList = repository.observePolicies().first()
        detailsList.forEach { details ->
            var current = details.policy
            val due = details.futurePolicies
                .sortedBy { it.startDateEpochDay }
                .filter { it.startDateEpochDay <= today && current.expiryDateEpochDay <= today }

            due.forEach { future ->
                val refs = details.attachments.joinToString("||") { "${it.displayName}::${it.uri}" }
                repository.addHistory(
                    PolicyHistoryEntity(
                        policyId = current.id,
                        policyHolderName = current.policyHolderName,
                        insurerName = current.insurerName,
                        policyNumber = current.policyNumber,
                        startDateEpochDay = current.startDateEpochDay,
                        endDateEpochDay = current.expiryDateEpochDay,
                        premiumAmount = current.premiumAmount,
                        attachmentRefs = refs
                    )
                )
                current = current.copy(
                    policyHolderName = future.policyHolderName,
                    policyName = future.policyName,
                    policyNumber = future.policyNumber,
                    startDateEpochDay = future.startDateEpochDay,
                    expiryDateEpochDay = future.expiryDateEpochDay,
                    insurerName = future.insurerName,
                    previousInsurerName = current.insurerName,
                    premiumAmount = future.premiumAmount
                )
                repository.updatePolicy(current)
                parseAttachmentRefs(future.attachmentRefs).forEach { futureAttachment ->
                    repository.addAttachment(
                        AttachmentEntity(
                            policyId = current.id,
                            uri = futureAttachment.uri,
                            displayName = futureAttachment.displayName,
                            mimeType = futureAttachment.mimeType,
                            addedAtEpochMillis = System.currentTimeMillis()
                        )
                    )
                }
                repository.deleteFuturePolicy(future.id)
            }
        }
    }

    private data class ParsedPolicy(
        val category: PolicyCategory,
        val policyHolderName: String,
        val policyName: String,
        val policyNumber: String,
        val startDate: Long,
        val expiryDate: Long,
        val insurerName: String,
        val previousInsurerName: String,
        val premiumAmount: Double,
        val notes: String,
        val details: PolicyCategoryDetailsEntity
    )
}

data class DashboardUiState(
    val policies: List<PolicyUiModel> = emptyList(),
    val expiringSoon: List<PolicyUiModel> = emptyList(),
    val selectedCategory: PolicyCategory? = null,
    val searchQuery: String = "",
    val searchResults: List<SearchResultUiModel> = emptyList(),
    val totalRecords: Int = 0,
    val totalExpiringSoon: Int = 0,
    val counts: Map<PolicyCategory, Int> = emptyMap(),
    val closedPolicies: List<PolicyUiModel> = emptyList()
)

sealed interface SearchResultUiModel {
    data class PolicyResult(
        val policyId: Long,
        val title: String,
        val subtitle: String,
        val categoryLabel: String
    ) : SearchResultUiModel

    data class FolderResult(
        val folderId: Long,
        val title: String,
        val subtitle: String
    ) : SearchResultUiModel
}

data class PolicyUiModel(
    val id: Long,
    val category: PolicyCategory,
    val policyHolderName: String,
    val policyName: String,
    val policyNumber: String,
    val startDateEpochDay: Long,
    val expiryDateEpochDay: Long,
    val insurerName: String,
    val previousInsurerName: String,
    val premiumAmount: Double,
    val notes: String,
    val isClosed: Boolean,
    val status: PolicyStatus,
    val hasFutureRenewal: Boolean,
    val daysToExpiry: Long,
    val categoryDetails: PolicyCategoryDetailsUiModel,
    val futurePolicies: List<FuturePolicyUiModel>,
    val history: List<HistoryUiModel>,
    val attachments: List<AttachmentUiModel>
)

data class FuturePolicyUiModel(
    val id: Long,
    val policyHolderName: String,
    val policyName: String,
    val policyNumber: String,
    val startDateEpochDay: Long,
    val expiryDateEpochDay: Long,
    val insurerName: String,
    val premiumAmount: Double,
    val attachments: List<AttachmentUiModel>
)

data class CompactPolicyUiModel(
    val id: Long,
    val name: String,
    val number: String,
    val insurer: String,
    val expiryLabel: String
)

data class HistoryUiModel(
    val id: Long,
    val policyHolderName: String,
    val insurerName: String,
    val policyNumber: String,
    val startDateEpochDay: Long,
    val endDateEpochDay: Long,
    val premiumAmount: Double,
    val attachmentRefs: List<AttachmentUiModel>
)

data class AttachmentUiModel(
    val id: Long,
    val uri: String,
    val displayName: String,
    val mimeType: String,
    val addedAtEpochMillis: Long
)

data class FolderUiModel(
    val id: Long,
    val category: PolicyCategory,
    val name: String,
    val startEpochDay: Long,
    val endEpochDay: Long,
    val colorHex: String,
    val taggedPolicyIds: Set<Long>,
    val policies: List<CompactPolicyUiModel>,
    val attachments: List<AttachmentUiModel>
)

data class FolderTagUiModel(
    val id: Long,
    val name: String,
    val colorHex: String
)

data class FolderPolicyDocumentUiModel(
    val attachmentId: Long,
    val policyId: Long,
    val policyName: String,
    val policyNumber: String,
    val policyStartEpochDay: Long,
    val policyExpiryEpochDay: Long,
    val attachment: AttachmentUiModel
)

data class ServiceCategoryUiState(
    val category: PolicyCategory,
    val folders: List<FolderUiModel> = emptyList(),
    val policies: List<CompactPolicyUiModel> = emptyList()
)

data class FolderDetailUiState(
    val folder: FolderUiModel? = null,
    val policiesInCategory: List<CompactPolicyUiModel> = emptyList(),
    val policyDocuments: List<FolderPolicyDocumentUiModel> = emptyList()
)

data class PolicyInput(
    val category: PolicyCategory,
    val policyHolderName: String,
    val policyName: String,
    val policyNumber: String,
    val startDate: String,
    val expiryDate: String,
    val insurerName: String,
    val previousInsurerName: String,
    val premiumAmount: String,
    val notes: String,
    val premiumFrequency: String = "",
    val premiumDueDayOfMonth: String = "",
    val coverageAmount: String = "",
    val coverageAmountUnit: String = "",
    val premiumPaymentStartDate: String = "",
    val premiumPaymentEndDate: String = "",
    val premiumPaymentTermYears: String = "",
    val policyTermYears: String = "",
    val entryAge: String = "",
    val coverageTillAge: String = "",
    val nomineeName: String = "",
    val nomineeRelationship: String = "",
    val riderAddons: String = "",
    val paymentMode: String = "",
    val gracePeriodDays: String = "",
    val termPolicyStatus: String = "",
    val totalPayments: String = "",
    val paidPayments: String = "",
    val vehicleNumber: String = "",
    val vehicleType: String = "",
    val makeModelVariant: String = "",
    val fuelType: String = "",
    val vehiclePolicyType: String = "",
    val vehicleAddons: String = "",
    val claimHistory: String = "",
    val documentType: String = "",
    val customDocumentType: String = "",
    val issuingRto: String = "",
    val stateName: String = "",
    val vehicleClass: String = "",
    val ownerName: String = "",
    val linkedVehicleNumber: String = "",
    val dateOfBirth: String = "",
    val customFieldValuesJson: String = ""
)

data class HistoryInput(
    val policyHolderName: String,
    val insurerName: String,
    val policyNumber: String,
    val startDate: String,
    val endDate: String,
    val premiumAmount: String
)

data class FolderInput(
    val name: String
)

data class RenewalInput(
    val policyHolderName: String,
    val policyName: String,
    val policyNumber: String,
    val startDate: String,
    val expiryDate: String,
    val insurerName: String,
    val premiumAmount: String
)

data class LoanInput(
    val loanName: String,
    val lenderName: String,
    val principalAmount: String,
    val annualInterestRate: String,
    val tenureMonths: String,
    val paymentFrequency: String = "Monthly",
    val startDate: String
)

data class LoanUiModel(
    val id: Long,
    val loanName: String,
    val lenderName: String,
    val principalAmount: Double,
    val annualInterestRate: Double,
    val tenureMonths: Int,
    val emiAmount: Double,
    val paymentFrequency: String = "Monthly",
    val startDateEpochDay: Long
)

data class LoanPaymentUiModel(
    val id: Long,
    val installmentNumber: Int,
    val dueDateEpochDay: Long,
    val amountDue: Double,
    val principalComponent: Double,
    val interestComponent: Double,
    val isPaid: Boolean,
    val paidOnEpochDay: Long?
)

data class MoneyLendInput(
    val borrowerName: String,
    val amount: String,
    val interestRate: String,
    val startDate: String,
    val dueDate: String,
    val notes: String
)

data class MoneyLendUiModel(
    val id: Long,
    val borrowerName: String,
    val amount: Double,
    val interestRate: Double,
    val startDateEpochDay: Long,
    val dueDateEpochDay: Long?,
    val notes: String,
    val paidInstallments: Set<Int>,
    val isRepaid: Boolean
)

data class IncomeInput(
    val sourceName: String,
    val amount: String,
    val receivedDate: String,
    val notes: String
)

data class IncomeUiModel(
    val id: Long,
    val sourceName: String,
    val amount: Double,
    val receivedDateEpochDay: Long,
    val notes: String
)

data class ClosedLoanUiModel(
    val id: Long,
    val loan: LoanUiModel,
    val closedOnEpochDay: Long,
    val closeReason: String,
    val events: List<LoanEventUiModel>,
    val payments: List<LoanPaymentUiModel>
)

enum class LoanEventType {
    PREPAYMENT,
    RATE_CHANGE
}

data class LoanEventUiModel(
    val id: Long,
    val loanId: Long,
    val dateEpochDay: Long,
    val type: LoanEventType,
    val amount: Double?,
    val oldRate: Double?,
    val newRate: Double?,
    val oldEmi: Double?,
    val newEmi: Double?,
    val note: String
)

data class PolicyCategoryDetailsUiModel(
    val premiumFrequency: String = "",
    val premiumDueDayOfMonth: Int? = null,
    val coverageAmount: Double? = null,
    val coverageAmountUnit: String = "",
    val premiumPaymentStartEpochDay: Long? = null,
    val premiumPaymentEndEpochDay: Long? = null,
    val premiumPaymentTermYears: Int? = null,
    val policyValidityEndEpochDay: Long? = null,
    val policyTermYears: Int? = null,
    val entryAge: Int? = null,
    val coverageTillAge: Int? = null,
    val nomineeName: String = "",
    val nomineeRelationship: String = "",
    val riderAddons: String = "",
    val paymentMode: String = "",
    val gracePeriodDays: Int? = null,
    val termPolicyStatus: String = "",
    val totalPayments: Int? = null,
    val paidPayments: Int? = null,
    val vehicleNumber: String = "",
    val vehicleType: String = "",
    val makeModelVariant: String = "",
    val fuelType: String = "",
    val vehiclePolicyType: String = "",
    val vehicleAddons: String = "",
    val claimHistory: String = "",
    val documentType: String = "",
    val issuingRto: String = "",
    val stateName: String = "",
    val vehicleClass: String = "",
    val ownerName: String = "",
    val linkedVehicleNumber: String = "",
    val dateOfBirth: String = "",
    val customFieldValuesJson: String = ""
) {
    val paymentsLeft: Int?
        get() = if (totalPayments != null && paidPayments != null) (totalPayments - paidPayments).coerceAtLeast(0) else null
}

private fun PolicyWithDetails.toUiModel(): PolicyUiModel {
    val closed = isPolicyClosed(policy.notes)
    val category = runCatching { PolicyCategory.valueOf(policy.category) }.getOrDefault(PolicyCategory.HEALTH_INSURANCE)
    val days = daysToExpiry(policy.expiryDateEpochDay)
    val hasFuture = futurePolicies.isNotEmpty()
    val status = when {
        hasFuture -> PolicyStatus.ACTIVE
        days < 0 -> PolicyStatus.EXPIRED
        days <= EXPIRY_WARNING_DAYS -> PolicyStatus.EXPIRING_SOON
        else -> PolicyStatus.ACTIVE
    }

    return PolicyUiModel(
        id = policy.id,
        category = category,
        policyHolderName = policy.policyHolderName,
        policyName = policy.policyName,
        policyNumber = policy.policyNumber,
        startDateEpochDay = policy.startDateEpochDay,
        expiryDateEpochDay = policy.expiryDateEpochDay,
        insurerName = policy.insurerName,
        previousInsurerName = policy.previousInsurerName,
        premiumAmount = policy.premiumAmount,
        notes = withoutClosedMarker(policy.notes),
        isClosed = closed,
        status = status,
        hasFutureRenewal = hasFuture,
        daysToExpiry = days,
        categoryDetails = categoryDetails?.toUiModel() ?: PolicyCategoryDetailsUiModel(),
        futurePolicies = futurePolicies.sortedBy { it.startDateEpochDay }.map {
            FuturePolicyUiModel(
                id = it.id,
                policyHolderName = it.policyHolderName,
                policyName = it.policyName,
                policyNumber = it.policyNumber,
                startDateEpochDay = it.startDateEpochDay,
                expiryDateEpochDay = it.expiryDateEpochDay,
                insurerName = it.insurerName,
                premiumAmount = it.premiumAmount,
                attachments = parseAttachmentRefs(it.attachmentRefs)
            )
        },
        history = history.sortedByDescending { it.endDateEpochDay }.map {
            HistoryUiModel(
                id = it.id,
                policyHolderName = it.policyHolderName,
                insurerName = it.insurerName,
                policyNumber = it.policyNumber,
                startDateEpochDay = it.startDateEpochDay,
                endDateEpochDay = it.endDateEpochDay,
                premiumAmount = it.premiumAmount,
                attachmentRefs = parseAttachmentRefs(it.attachmentRefs)
            )
        },
        attachments = attachments.map {
            AttachmentUiModel(
                id = it.id,
                uri = it.uri,
                displayName = it.displayName,
                mimeType = it.mimeType,
                addedAtEpochMillis = it.addedAtEpochMillis
            )
        }
    )
}

private fun isPolicyClosed(notes: String): Boolean = notes.contains(CLOSED_POLICY_MARKER)

private fun withClosedMarker(notes: String): String {
    if (isPolicyClosed(notes)) return notes
    return if (notes.isBlank()) CLOSED_POLICY_MARKER else "$notes\n$CLOSED_POLICY_MARKER"
}

private fun withoutClosedMarker(notes: String): String =
    notes.replace(CLOSED_POLICY_MARKER, "").trim()

private fun PolicyEntity.toCompactPolicy(): CompactPolicyUiModel {
    return CompactPolicyUiModel(
        id = id,
        name = policyName,
        number = policyNumber,
        insurer = insurerName,
        expiryLabel = formatEpochDay(expiryDateEpochDay)
    )
}

private fun FolderWithDetails.toFolderUiModel(): FolderUiModel {
    val category = runCatching { PolicyCategory.valueOf(folder.category) }.getOrDefault(PolicyCategory.HEALTH_INSURANCE)
    return FolderUiModel(
        id = folder.id,
        category = category,
        name = folder.name,
        startEpochDay = folder.startEpochDay,
        endEpochDay = folder.endEpochDay,
        colorHex = folder.colorHex,
        taggedPolicyIds = policies.map { it.id }.toSet(),
        policies = policies.map { it.toCompactPolicy() },
        attachments = attachments.map {
            AttachmentUiModel(
                id = it.id,
                uri = it.uri,
                displayName = it.displayName,
                mimeType = it.mimeType,
                addedAtEpochMillis = it.addedAtEpochMillis
            )
        }
    )
}

private fun PolicyCategoryDetailsEntity.toUiModel(): PolicyCategoryDetailsUiModel {
    return PolicyCategoryDetailsUiModel(
        premiumFrequency = premiumFrequency,
        premiumDueDayOfMonth = premiumDueDayOfMonth,
        coverageAmount = coverageAmount,
        coverageAmountUnit = coverageAmountUnit,
        premiumPaymentStartEpochDay = premiumPaymentStartEpochDay,
        premiumPaymentEndEpochDay = premiumPaymentEndEpochDay,
        premiumPaymentTermYears = premiumPaymentTermYears,
        policyValidityEndEpochDay = policyValidityEndEpochDay,
        policyTermYears = policyTermYears,
        entryAge = entryAge,
        coverageTillAge = coverageTillAge,
        nomineeName = nomineeName,
        nomineeRelationship = nomineeRelationship,
        riderAddons = riderAddons,
        paymentMode = paymentMode,
        gracePeriodDays = gracePeriodDays,
        termPolicyStatus = termPolicyStatus,
        totalPayments = totalPayments,
        paidPayments = paidPayments,
        vehicleNumber = vehicleNumber,
        vehicleType = vehicleType,
        makeModelVariant = makeModelVariant,
        fuelType = fuelType,
        vehiclePolicyType = vehiclePolicyType,
        vehicleAddons = vehicleAddons,
        claimHistory = claimHistory,
        documentType = documentType,
        issuingRto = issuingRto,
        stateName = stateName,
        vehicleClass = vehicleClass,
        ownerName = ownerName,
        linkedVehicleNumber = linkedVehicleNumber,
        dateOfBirth = dateOfBirth,
        customFieldValuesJson = customFieldValuesJson
    )
}

private fun parseAttachmentRefs(raw: String): List<AttachmentUiModel> {
    if (raw.isBlank()) return emptyList()
    return raw.split("||").mapNotNull { part ->
        val pieces = part.split("::")
        if (pieces.size < 2) return@mapNotNull null
        val uri = pieces.drop(1).joinToString("::")
        val fileName = pieces.first()
        AttachmentUiModel(
            id = 0,
            uri = uri,
            displayName = fileName,
            mimeType = guessMimeType(fileName, uri),
            addedAtEpochMillis = 0
        )
    }
}

private fun guessMimeType(fileName: String, uri: String): String {
    val text = "${fileName.lowercase()} ${uri.lowercase()}"
    return when {
        text.contains(".pdf") -> "application/pdf"
        text.contains(".jpg") || text.contains(".jpeg") -> "image/jpeg"
        text.contains(".png") -> "image/png"
        text.contains(".webp") -> "image/webp"
        else -> "application/octet-stream"
    }
}

fun defaultPolicyInput(category: PolicyCategory = PolicyCategory.HEALTH_INSURANCE): PolicyInput {
    return PolicyInput(
        category = category,
        policyHolderName = "",
        policyName = "",
        policyNumber = "",
        startDate = formatTodayUi(),
        expiryDate = formatFutureUi(365),
        insurerName = "",
        previousInsurerName = "",
        premiumAmount = "",
        notes = "",
        premiumFrequency = "",
        premiumDueDayOfMonth = "",
        coverageAmount = "",
        coverageAmountUnit = "",
        premiumPaymentStartDate = formatTodayUi(),
        premiumPaymentEndDate = formatFutureUi(365),
        premiumPaymentTermYears = "",
        policyTermYears = "",
        entryAge = "",
        coverageTillAge = "",
        nomineeName = "",
        nomineeRelationship = "",
        riderAddons = "",
        paymentMode = "",
        gracePeriodDays = "",
        termPolicyStatus = "",
        totalPayments = "",
        paidPayments = "",
        vehicleNumber = "",
        vehicleType = "",
        makeModelVariant = "",
        fuelType = "",
        vehiclePolicyType = "",
        vehicleAddons = "",
        claimHistory = "",
        documentType = "",
        customDocumentType = "",
        issuingRto = "",
        stateName = "",
        vehicleClass = "",
        ownerName = "",
        linkedVehicleNumber = "",
        dateOfBirth = "",
        customFieldValuesJson = ""
    )
}

fun defaultFolderInput(): FolderInput {
    return FolderInput(name = "")
}

private fun PolicyEntity.toJson() = JSONObject().apply {
    put("id", id)
    put("category", category)
    put("policyHolderName", policyHolderName)
    put("policyName", policyName)
    put("policyNumber", policyNumber)
    put("startDateEpochDay", startDateEpochDay)
    put("expiryDateEpochDay", expiryDateEpochDay)
    put("insurerName", insurerName)
    put("previousInsurerName", previousInsurerName)
    put("premiumAmount", jsonSafeDouble(premiumAmount))
    put("notes", notes)
}

private fun PolicyHistoryEntity.toJson() = JSONObject().apply {
    put("id", id)
    put("policyId", policyId)
    put("policyHolderName", policyHolderName)
    put("insurerName", insurerName)
    put("policyNumber", policyNumber)
    put("startDateEpochDay", startDateEpochDay)
    put("endDateEpochDay", endDateEpochDay)
    put("premiumAmount", jsonSafeDouble(premiumAmount))
    put("attachmentRefs", attachmentRefs)
}

private fun AttachmentEntity.toJson() = JSONObject().apply {
    put("id", id)
    put("policyId", policyId)
    put("uri", uri)
    put("displayName", displayName)
    put("mimeType", mimeType)
    put("addedAtEpochMillis", addedAtEpochMillis)
}

private fun FuturePolicyEntity.toJson() = JSONObject().apply {
    put("id", id)
    put("policyId", policyId)
    put("policyHolderName", policyHolderName)
    put("policyName", policyName)
    put("policyNumber", policyNumber)
    put("startDateEpochDay", startDateEpochDay)
    put("expiryDateEpochDay", expiryDateEpochDay)
    put("insurerName", insurerName)
    put("premiumAmount", jsonSafeDouble(premiumAmount))
    put("createdAtEpochMillis", createdAtEpochMillis)
    put("attachmentRefs", attachmentRefs)
}

private fun PolicyCategoryDetailsEntity.toJson() = JSONObject().apply {
    put("policyId", policyId)
    put("premiumFrequency", premiumFrequency)
    put("premiumDueDayOfMonth", premiumDueDayOfMonth)
    put("coverageAmount", coverageAmount)
    put("coverageAmountUnit", coverageAmountUnit)
    put("premiumPaymentStartEpochDay", premiumPaymentStartEpochDay)
    put("premiumPaymentEndEpochDay", premiumPaymentEndEpochDay)
    put("premiumPaymentTermYears", premiumPaymentTermYears)
    put("policyValidityEndEpochDay", policyValidityEndEpochDay)
    put("policyTermYears", policyTermYears)
    put("entryAge", entryAge)
    put("coverageTillAge", coverageTillAge)
    put("nomineeName", nomineeName)
    put("nomineeRelationship", nomineeRelationship)
    put("riderAddons", riderAddons)
    put("paymentMode", paymentMode)
    put("gracePeriodDays", gracePeriodDays)
    put("termPolicyStatus", termPolicyStatus)
    put("totalPayments", totalPayments)
    put("paidPayments", paidPayments)
    put("vehicleNumber", vehicleNumber)
    put("vehicleType", vehicleType)
    put("makeModelVariant", makeModelVariant)
    put("fuelType", fuelType)
    put("vehiclePolicyType", vehiclePolicyType)
    put("vehicleAddons", vehicleAddons)
    put("claimHistory", claimHistory)
    put("documentType", documentType)
    put("issuingRto", issuingRto)
    put("stateName", stateName)
    put("vehicleClass", vehicleClass)
    put("ownerName", ownerName)
    put("linkedVehicleNumber", linkedVehicleNumber)
    put("dateOfBirth", dateOfBirth)
    put("customFieldValuesJson", customFieldValuesJson)
}

private fun CategoryFolderEntity.toJson() = JSONObject().apply {
    put("id", id)
    put("category", category)
    put("name", name)
    put("startEpochDay", startEpochDay)
    put("endEpochDay", endEpochDay)
    put("createdAtEpochMillis", createdAtEpochMillis)
    put("colorHex", colorHex)
}

private fun PolicyFolderCrossRef.toJson() = JSONObject().apply {
    put("policyId", policyId)
    put("folderId", folderId)
}

private fun FolderAttachmentEntity.toJson() = JSONObject().apply {
    put("id", id)
    put("folderId", folderId)
    put("uri", uri)
    put("displayName", displayName)
    put("mimeType", mimeType)
    put("addedAtEpochMillis", addedAtEpochMillis)
}

private fun LoanEntity.toJson() = JSONObject().apply {
    put("id", id)
    put("loanName", loanName)
    put("lenderName", lenderName)
    put("principalAmount", jsonSafeDouble(principalAmount))
    put("annualInterestRate", jsonSafeDouble(annualInterestRate))
    put("tenureMonths", tenureMonths)
    put("emiAmount", jsonSafeDouble(emiAmount))
    put("paymentFrequency", paymentFrequency)
    put("startDateEpochDay", startDateEpochDay)
    put("createdAtEpochMillis", createdAtEpochMillis)
}

private fun LoanPaymentEntity.toJson() = JSONObject().apply {
    put("id", id)
    put("loanId", loanId)
    put("installmentNumber", installmentNumber)
    put("dueDateEpochDay", dueDateEpochDay)
    put("amountDue", jsonSafeDouble(amountDue))
    put("principalComponent", jsonSafeDouble(principalComponent))
    put("interestComponent", jsonSafeDouble(interestComponent))
    put("isPaid", isPaid)
    put("paidOnEpochDay", paidOnEpochDay)
}

private fun MoneyLendEntity.toJson() = JSONObject().apply {
    put("id", id)
    put("borrowerName", borrowerName)
    put("amount", jsonSafeDouble(amount))
    put("interestRate", jsonSafeDouble(interestRate))
    put("startDateEpochDay", startDateEpochDay)
    put("dueDateEpochDay", dueDateEpochDay)
    put("notes", notes)
    put("paidInstallmentsJson", paidInstallmentsJson)
    put("isRepaid", isRepaid)
    put("createdAtEpochMillis", createdAtEpochMillis)
}

private fun JSONArray?.toPolicies(): List<PolicyEntity> = buildList {
    if (this@toPolicies == null) return@buildList
    for (i in 0 until length()) {
        val o = getJSONObject(i)
        add(
            PolicyEntity(
                id = o.getLong("id"),
                category = o.getString("category"),
                policyHolderName = o.optString("policyHolderName"),
                policyName = o.getString("policyName"),
                policyNumber = o.getString("policyNumber"),
                startDateEpochDay = o.getLong("startDateEpochDay"),
                expiryDateEpochDay = o.getLong("expiryDateEpochDay"),
                insurerName = o.getString("insurerName"),
                previousInsurerName = o.optString("previousInsurerName"),
                premiumAmount = o.getDouble("premiumAmount"),
                notes = o.optString("notes")
            )
        )
    }
}

private fun JSONArray?.toHistory(): List<PolicyHistoryEntity> = buildList {
    if (this@toHistory == null) return@buildList
    for (i in 0 until length()) {
        val o = getJSONObject(i)
        add(
            PolicyHistoryEntity(
                id = o.getLong("id"),
                policyId = o.getLong("policyId"),
                policyHolderName = o.optString("policyHolderName"),
                insurerName = o.getString("insurerName"),
                policyNumber = o.getString("policyNumber"),
                startDateEpochDay = o.getLong("startDateEpochDay"),
                endDateEpochDay = o.getLong("endDateEpochDay"),
                premiumAmount = o.getDouble("premiumAmount"),
                attachmentRefs = o.optString("attachmentRefs")
            )
        )
    }
}

private fun JSONArray?.toAttachments(): List<AttachmentEntity> = buildList {
    if (this@toAttachments == null) return@buildList
    for (i in 0 until length()) {
        val o = getJSONObject(i)
        add(
            AttachmentEntity(
                id = o.getLong("id"),
                policyId = o.getLong("policyId"),
                uri = o.getString("uri"),
                displayName = o.getString("displayName"),
                mimeType = o.getString("mimeType"),
                addedAtEpochMillis = o.getLong("addedAtEpochMillis")
            )
        )
    }
}

private fun JSONArray?.toFuturePolicies(): List<FuturePolicyEntity> = buildList {
    if (this@toFuturePolicies == null) return@buildList
    for (i in 0 until length()) {
        val o = getJSONObject(i)
        add(
            FuturePolicyEntity(
                id = o.getLong("id"),
                policyId = o.getLong("policyId"),
                policyHolderName = o.optString("policyHolderName"),
                policyName = o.getString("policyName"),
                policyNumber = o.getString("policyNumber"),
                startDateEpochDay = o.getLong("startDateEpochDay"),
                expiryDateEpochDay = o.getLong("expiryDateEpochDay"),
                insurerName = o.getString("insurerName"),
                premiumAmount = o.getDouble("premiumAmount"),
                createdAtEpochMillis = o.getLong("createdAtEpochMillis"),
                attachmentRefs = o.optString("attachmentRefs")
            )
        )
    }
}

private fun JSONArray?.toPolicyCategoryDetails(): List<PolicyCategoryDetailsEntity> = buildList {
    if (this@toPolicyCategoryDetails == null) return@buildList
    for (i in 0 until length()) {
        val o = getJSONObject(i)
        add(
            PolicyCategoryDetailsEntity(
                policyId = o.getLong("policyId"),
                premiumFrequency = o.optString("premiumFrequency"),
                premiumDueDayOfMonth = if (o.isNull("premiumDueDayOfMonth")) null else o.getInt("premiumDueDayOfMonth"),
                coverageAmount = if (o.isNull("coverageAmount")) null else o.getDouble("coverageAmount"),
                coverageAmountUnit = o.optString("coverageAmountUnit"),
                premiumPaymentStartEpochDay = if (o.isNull("premiumPaymentStartEpochDay")) null else o.getLong("premiumPaymentStartEpochDay"),
                premiumPaymentEndEpochDay = if (o.isNull("premiumPaymentEndEpochDay")) null else o.getLong("premiumPaymentEndEpochDay"),
                premiumPaymentTermYears = if (o.isNull("premiumPaymentTermYears")) null else o.getInt("premiumPaymentTermYears"),
                policyValidityEndEpochDay = if (o.isNull("policyValidityEndEpochDay")) null else o.getLong("policyValidityEndEpochDay"),
                policyTermYears = if (o.isNull("policyTermYears")) null else o.getInt("policyTermYears"),
                entryAge = if (o.isNull("entryAge")) null else o.getInt("entryAge"),
                coverageTillAge = if (o.isNull("coverageTillAge")) null else o.getInt("coverageTillAge"),
                nomineeName = o.optString("nomineeName"),
                nomineeRelationship = o.optString("nomineeRelationship"),
                riderAddons = o.optString("riderAddons"),
                paymentMode = o.optString("paymentMode"),
                gracePeriodDays = if (o.isNull("gracePeriodDays")) null else o.getInt("gracePeriodDays"),
                termPolicyStatus = o.optString("termPolicyStatus"),
                totalPayments = if (o.isNull("totalPayments")) null else o.getInt("totalPayments"),
                paidPayments = if (o.isNull("paidPayments")) null else o.getInt("paidPayments"),
                vehicleNumber = o.optString("vehicleNumber"),
                vehicleType = o.optString("vehicleType"),
                makeModelVariant = o.optString("makeModelVariant"),
                fuelType = o.optString("fuelType"),
                vehiclePolicyType = o.optString("vehiclePolicyType"),
                vehicleAddons = o.optString("vehicleAddons"),
                claimHistory = o.optString("claimHistory"),
                documentType = o.optString("documentType"),
                issuingRto = o.optString("issuingRto"),
                stateName = o.optString("stateName"),
                vehicleClass = o.optString("vehicleClass"),
                ownerName = o.optString("ownerName"),
                linkedVehicleNumber = o.optString("linkedVehicleNumber"),
                dateOfBirth = o.optString("dateOfBirth"),
                customFieldValuesJson = o.optString("customFieldValuesJson")
            )
        )
    }
}

private fun JSONArray?.toFolders(): List<CategoryFolderEntity> = buildList {
    if (this@toFolders == null) return@buildList
    for (i in 0 until length()) {
        val o = getJSONObject(i)
        add(
            CategoryFolderEntity(
                id = o.getLong("id"),
                category = o.getString("category"),
                name = o.getString("name"),
                startEpochDay = o.getLong("startEpochDay"),
                endEpochDay = o.getLong("endEpochDay"),
                createdAtEpochMillis = o.getLong("createdAtEpochMillis"),
                colorHex = o.optString("colorHex", "#F6E49A")
            )
        )
    }
}

private fun JSONArray?.toFolderRefs(): List<PolicyFolderCrossRef> = buildList {
    if (this@toFolderRefs == null) return@buildList
    for (i in 0 until length()) {
        val o = getJSONObject(i)
        add(PolicyFolderCrossRef(policyId = o.getLong("policyId"), folderId = o.getLong("folderId")))
    }
}

private fun JSONArray?.toFolderAttachments(): List<FolderAttachmentEntity> = buildList {
    if (this@toFolderAttachments == null) return@buildList
    for (i in 0 until length()) {
        val o = getJSONObject(i)
        add(
            FolderAttachmentEntity(
                id = o.getLong("id"),
                folderId = o.getLong("folderId"),
                uri = o.getString("uri"),
                displayName = o.getString("displayName"),
                mimeType = o.getString("mimeType"),
                addedAtEpochMillis = o.getLong("addedAtEpochMillis")
            )
        )
    }
}

private fun JSONArray?.toLoans(): List<LoanEntity> = buildList {
    if (this@toLoans == null) return@buildList
    for (i in 0 until length()) {
        val o = getJSONObject(i)
        add(
            LoanEntity(
                id = o.getLong("id"),
                loanName = o.getString("loanName"),
                lenderName = o.getString("lenderName"),
                principalAmount = o.getDouble("principalAmount"),
                annualInterestRate = o.getDouble("annualInterestRate"),
                tenureMonths = o.getInt("tenureMonths"),
                emiAmount = o.getDouble("emiAmount"),
                paymentFrequency = o.optString("paymentFrequency", "Monthly"),
                startDateEpochDay = o.getLong("startDateEpochDay"),
                createdAtEpochMillis = o.getLong("createdAtEpochMillis")
            )
        )
    }
}

private fun JSONArray?.toLoanPayments(): List<LoanPaymentEntity> = buildList {
    if (this@toLoanPayments == null) return@buildList
    for (i in 0 until length()) {
        val o = getJSONObject(i)
        add(
            LoanPaymentEntity(
                id = o.getLong("id"),
                loanId = o.getLong("loanId"),
                installmentNumber = o.getInt("installmentNumber"),
                dueDateEpochDay = o.getLong("dueDateEpochDay"),
                amountDue = o.getDouble("amountDue"),
                principalComponent = o.getDouble("principalComponent"),
                interestComponent = o.getDouble("interestComponent"),
                isPaid = o.optBoolean("isPaid", false),
                paidOnEpochDay = if (o.isNull("paidOnEpochDay")) null else o.getLong("paidOnEpochDay")
            )
        )
    }
}

private fun JSONArray?.toMoneyLends(): List<MoneyLendEntity> = buildList {
    if (this@toMoneyLends == null) return@buildList
    for (i in 0 until length()) {
        val o = getJSONObject(i)
        add(
            MoneyLendEntity(
                id = o.getLong("id"),
                borrowerName = o.getString("borrowerName"),
                amount = o.getDouble("amount"),
                interestRate = o.getDouble("interestRate"),
                startDateEpochDay = o.getLong("startDateEpochDay"),
                dueDateEpochDay = if (o.isNull("dueDateEpochDay")) null else o.getLong("dueDateEpochDay"),
                notes = o.optString("notes"),
                paidInstallmentsJson = o.optString("paidInstallmentsJson", "[]"),
                isRepaid = o.optBoolean("isRepaid", false),
                createdAtEpochMillis = o.optLong("createdAtEpochMillis", System.currentTimeMillis())
            )
        )
    }
}

private fun JSONObject?.toBackupFileManifest(): Map<String, String> {
    if (this == null) return emptyMap()
    val result = mutableMapOf<String, String>()
    val keys = keys()
    while (keys.hasNext()) {
        val oldPath = keys.next()
        val entryName = optString(oldPath)
        if (entryName.isNotBlank()) {
            result[oldPath] = entryName
        }
    }
    return result
}

private fun parsePaidInstallments(raw: String): Set<Int> {
    if (raw.isBlank()) return emptySet()
    return runCatching {
        val arr = JSONArray(raw)
        buildSet {
            for (i in 0 until arr.length()) add(arr.getInt(i))
        }
    }.getOrDefault(emptySet())
}

private fun toPaidInstallmentsJson(values: Set<Int>): String =
    JSONArray().apply { values.sorted().forEach { put(it) } }.toString()

private fun jsonSafeDouble(value: Double): Double = if (value.isFinite()) value else 0.0

private fun MoneyLendEntity.toUiModel(): MoneyLendUiModel =
    MoneyLendUiModel(
        id = id,
        borrowerName = borrowerName,
        amount = amount,
        interestRate = interestRate,
        startDateEpochDay = startDateEpochDay,
        dueDateEpochDay = dueDateEpochDay,
        notes = notes,
        paidInstallments = parsePaidInstallments(paidInstallmentsJson),
        isRepaid = isRepaid
    )
