package com.uday.policytracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.platform.LocalConfiguration
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.uday.policytracker.ui.components.DatePickerField
import com.uday.policytracker.util.formatEpochDay
import com.uday.policytracker.util.formatTodayUi
import com.uday.policytracker.util.formatIndianAmount
import com.uday.policytracker.viewmodel.LoanInput
import com.uday.policytracker.viewmodel.MoneyLendInput
import com.uday.policytracker.viewmodel.LoanUiModel
import com.uday.policytracker.viewmodel.LoanEventType
import com.uday.policytracker.viewmodel.LoanEventUiModel
import com.uday.policytracker.viewmodel.MoneyLendUiModel
import com.uday.policytracker.viewmodel.ClosedLoanUiModel
import com.uday.policytracker.viewmodel.PolicyViewModel
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDate

private enum class FinanceTab(val label: String) {
    LOANS("Loans"),
    LEND("Lendings")
}

private sealed interface LoanHistoryRow {
    val dateEpochDay: Long
    val sortKey: Long

    data class Payment(val item: com.uday.policytracker.viewmodel.LoanPaymentUiModel) : LoanHistoryRow {
        override val dateEpochDay: Long = item.dueDateEpochDay
        override val sortKey: Long = item.installmentNumber.toLong()
    }

    data class Event(val item: LoanEventUiModel) : LoanHistoryRow {
        override val dateEpochDay: Long = item.dateEpochDay
        override val sortKey: Long = item.id
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FinancialsScreen(
    paddingValues: PaddingValues,
    viewModel: PolicyViewModel,
    onBack: () -> Unit
) {
    val loans by viewModel.loansState.collectAsStateWithLifecycle()
    val payments by viewModel.selectedLoanPayments.collectAsStateWithLifecycle()
    val loanEvents by viewModel.selectedLoanEvents.collectAsStateWithLifecycle()
    val lendEntries by viewModel.moneyLendState.collectAsStateWithLifecycle()
    val loanOutstandingById by viewModel.loanOutstandingById.collectAsStateWithLifecycle()
    val closedLoans by viewModel.closedLoansState.collectAsStateWithLifecycle()
    val activeLendEntries = lendEntries.filter { !it.isRepaid }
    val repaidLendEntries = lendEntries.filter { it.isRepaid }
    var selectedTab by remember { mutableStateOf(FinanceTab.LOANS) }
    var showAddLoan by remember { mutableStateOf(false) }
    var showAddLend by remember { mutableStateOf(false) }
    var addError by remember { mutableStateOf(false) }
    var addLendError by remember { mutableStateOf(false) }
    var showActionInfo by remember { mutableStateOf<String?>(null) }
    var loanActionTarget by remember { mutableStateOf<LoanUiModel?>(null) }
    var lendActionTarget by remember { mutableStateOf<MoneyLendUiModel?>(null) }
    var editLoanTarget by remember { mutableStateOf<LoanUiModel?>(null) }
    var editLendTarget by remember { mutableStateOf<MoneyLendUiModel?>(null) }
    var repaidConfirmTarget by remember { mutableStateOf<MoneyLendUiModel?>(null) }
    var showHistoryScreen by remember { mutableStateOf(false) }
    var selectedClosedLoanHistory by remember { mutableStateOf<ClosedLoanUiModel?>(null) }
    var selectedRepaidLendHistory by remember { mutableStateOf<MoneyLendUiModel?>(null) }
    var showPrepaymentDialog by remember { mutableStateOf(false) }
    var showRateChangeDialog by remember { mutableStateOf(false) }
    var showCloseLoanDialog by remember { mutableStateOf(false) }
    val pagerState = rememberPagerState(pageCount = { maxOf(1, loans.size) })
    val lendPagerState = rememberPagerState(pageCount = { maxOf(1, activeLendEntries.size) })
    val todayEpochDay = LocalDate.now().toEpochDay()
    val sectionBackground = if (isSystemInDarkTheme()) Color(0xFF0E1725) else Color(0xFFF1F5FB)
    val summaryGradientTop = if (isSystemInDarkTheme()) Color(0xFF23456D) else Color(0xFF4B82C3)
    val summaryGradientBottom = if (isSystemInDarkTheme()) Color(0xFF142B47) else Color(0xFF2F5F95)
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val loanCardWidth = (screenWidth - 44.dp).coerceAtLeast(280.dp)

    LaunchedEffect(loans) {
        if (loans.isEmpty()) return@LaunchedEffect
        val safePage = pagerState.currentPage.coerceIn(0, loans.lastIndex)
        if (safePage != pagerState.currentPage) pagerState.scrollToPage(safePage)
        viewModel.selectLoan(loans[safePage].id)
    }

    LaunchedEffect(pagerState, loans) {
        if (loans.isEmpty()) return@LaunchedEffect
        snapshotFlow { pagerState.currentPage }.collectLatest { page ->
            loans.getOrNull(page)?.let { viewModel.selectLoan(it.id) }
        }
    }
    LaunchedEffect(activeLendEntries) {
        if (activeLendEntries.isEmpty()) return@LaunchedEffect
        val safePage = lendPagerState.currentPage.coerceIn(0, activeLendEntries.lastIndex)
        if (safePage != lendPagerState.currentPage) lendPagerState.scrollToPage(safePage)
    }

    val selectedLoan = loans.getOrNull(pagerState.currentPage)
    val selectedLoanMonthsLeft = payments.count { !it.isPaid }
    val safeLendPage = lendPagerState.currentPage.coerceIn(0, (activeLendEntries.size - 1).coerceAtLeast(0))
    val selectedLend = activeLendEntries.getOrNull(safeLendPage)
    val selectedLoanBalance = selectedLoan?.let { loanOutstandingById[it.id] } ?: 0.0
    val selectedLoanTotalOutstanding = payments.filterNot { it.isPaid }.sumOf { it.amountDue }.coerceAtLeast(0.0)
    val totalDebt = loans.sumOf { loan -> loanOutstandingById[loan.id] ?: loan.principalAmount }
    val totalLentAmount = activeLendEntries.sumOf { it.amount }
    val totalReceivableInterest = activeLendEntries.sumOf { calculateReceivableInterest(it, todayEpochDay) }
    val selectedLendSchedule = selectedLend?.let { buildMoneyLendSchedule(it, todayEpochDay) } ?: emptyList()
    val paidHistory = payments.filter { it.isPaid }.sortedByDescending { it.dueDateEpochDay }
    val duePayments = payments.filter { !it.isPaid && it.dueDateEpochDay >= todayEpochDay }.take(5)
    val mergedHistory = buildList<LoanHistoryRow> {
        paidHistory.forEach { add(LoanHistoryRow.Payment(it)) }
        loanEvents.forEach { add(LoanHistoryRow.Event(it)) }
    }.sortedWith(
        compareByDescending<LoanHistoryRow> { it.dateEpochDay }
            .thenByDescending { it.sortKey }
    )
    val summaryTitle = when (selectedTab) {
        FinanceTab.LOANS -> "Overall Debt (All Active Loans)"
        FinanceTab.LEND -> "Total Lendings"
    }
    val summaryAmount = when (selectedTab) {
        FinanceTab.LOANS -> totalDebt
        FinanceTab.LEND -> totalLentAmount
    }
    val summarySecondary = when (selectedTab) {
        FinanceTab.LEND -> "Pending Interest ₹${formatIndianAmount(totalReceivableInterest, 0)}"
        else -> null
    }

    if (showHistoryScreen) {
        FinancialHistoryScreen(
            tab = selectedTab,
            closedLoans = closedLoans,
            repaidLends = repaidLendEntries,
            selectedClosedLoan = selectedClosedLoanHistory,
            selectedRepaidLend = selectedRepaidLendHistory,
            onSelectClosedLoan = { selectedClosedLoanHistory = it },
            onSelectRepaidLend = { selectedRepaidLendHistory = it },
            onBack = {
                if (selectedClosedLoanHistory != null || selectedRepaidLendHistory != null) {
                    selectedClosedLoanHistory = null
                    selectedRepaidLendHistory = null
                } else {
                    showHistoryScreen = false
                }
            }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Financials") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        showHistoryScreen = true
                        selectedClosedLoanHistory = null
                        selectedRepaidLendHistory = null
                    }) {
                        Icon(Icons.Default.History, contentDescription = "History")
                    }
                    IconButton(onClick = {
                        when (selectedTab) {
                            FinanceTab.LOANS -> showAddLoan = true
                            FinanceTab.LEND -> showAddLend = true
                        }
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(sectionBackground)
                .padding(paddingValues)
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Column(
                        modifier = Modifier
                            .background(
                                Brush.verticalGradient(listOf(summaryGradientTop, summaryGradientBottom)),
                                RoundedCornerShape(28.dp)
                            )
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(summaryTitle, color = Color(0xFFCCDFFF), style = MaterialTheme.typography.bodyLarge)
                        Text("₹ ${formatIndianAmount(summaryAmount, 0)}", color = Color.White, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                        summarySecondary?.let {
                            Text(it, color = Color(0xFFDCE9FF), style = MaterialTheme.typography.bodyMedium)
                        }
                        BubbleTabs(selectedTab = selectedTab, onTab = { selectedTab = it })
                    }
                }
            }

            when (selectedTab) {
                FinanceTab.LOANS -> {
                    item {
                        if (loans.isEmpty()) {
                            EmptyFinancialCard("No loans yet. Tap + to add your first loan.")
                        } else {
                            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                HorizontalPager(
                                    modifier = Modifier.fillMaxWidth(),
                                    state = pagerState,
                                    pageSpacing = 12.dp,
                                    pageSize = PageSize.Fixed(loanCardWidth),
                                    contentPadding = PaddingValues(horizontal = 16.dp)
                                ) { page ->
                                    val loan = loans[page]
                                    LoanSwipeCard(
                                        modifier = Modifier.width(loanCardWidth),
                                        loan = loan,
                                        balanceLeft = if (loan.id == selectedLoan?.id) selectedLoanBalance else loan.principalAmount,
                                        monthsLeft = if (loan.id == selectedLoan?.id) selectedLoanMonthsLeft else loan.tenureMonths,
                                        onLongPress = { loanActionTarget = loan }
                                    )
                                }
                                LoanPagerDots(
                                    total = loans.size,
                                    current = pagerState.currentPage
                                )
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF22364A))
                                ) {
                                    Text(
                                        "Total Outstanding (Principal + Interest): ₹${formatIndianAmount(selectedLoanTotalOutstanding, 0)}",
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    item {
                        QuickActions(
                            onPrepay = {
                                if (selectedLoan != null) showPrepaymentDialog = true
                                else showActionInfo = "Select a loan first."
                            },
                            onRateChange = {
                                if (selectedLoan != null) showRateChangeDialog = true
                                else showActionInfo = "Select a loan first."
                            },
                            onClose = {
                                if (selectedLoan != null) showCloseLoanDialog = true
                                else showActionInfo = "Select a loan first."
                            }
                        )
                    }

                    item {
                        SectionTitle("Repayment Schedule")
                    }
                    if (duePayments.isEmpty()) {
                        item { EmptyFinancialCard("No upcoming dues.") }
                    } else {
                        items(duePayments, key = { it.id }) { payment ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                shape = RoundedCornerShape(18.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
                                        Text("#${payment.installmentNumber} • ${formatEpochDay(payment.dueDateEpochDay)}", fontWeight = FontWeight.SemiBold)
                                        Text("EMI ₹${formatIndianAmount(payment.amountDue, 0)}", style = MaterialTheme.typography.bodySmall)
                                        Text("Interest ₹${formatIndianAmount(payment.interestComponent, 0)}", style = MaterialTheme.typography.bodySmall)
                                    }
                                    Text("Auto debit", color = Color(0xFF2A7A55), fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    item {
                        SectionTitle("Payment History")
                    }
                    if (mergedHistory.isEmpty()) {
                        item { EmptyFinancialCard("No completed payments yet.") }
                    } else {
                        items(mergedHistory, key = {
                            when (it) {
                                is LoanHistoryRow.Payment -> "loan_payment_${it.item.id}"
                                is LoanHistoryRow.Event -> "loan_event_${it.item.id}"
                            }
                        }) { row ->
                            when (row) {
                                is LoanHistoryRow.Event -> LoanEventCard(event = row.item)
                                is LoanHistoryRow.Payment -> {
                                    val payment = row.item
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F6D49))
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("#${payment.installmentNumber} • ${formatEpochDay(payment.dueDateEpochDay)}", color = Color.White)
                                            Text("₹${formatIndianAmount(payment.amountDue, 0)} Paid", color = Color.White, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                FinanceTab.LEND -> {
                    if (activeLendEntries.isEmpty()) {
                        item { EmptyFinancialCard("No lending entries yet. Tap + to add one.") }
                    } else {
                        item {
                            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                HorizontalPager(
                                    modifier = Modifier.fillMaxWidth(),
                                    state = lendPagerState,
                                    pageSpacing = 12.dp,
                                    pageSize = PageSize.Fixed(loanCardWidth),
                                    contentPadding = PaddingValues(horizontal = 16.dp)
                                ) { page ->
                                    val lend = activeLendEntries.getOrNull(page) ?: return@HorizontalPager
                                    MoneyLendSwipeCard(
                                        modifier = Modifier.width(loanCardWidth),
                                        lend = lend,
                                        monthlyInterest = (lend.amount * lend.interestRate / 100.0) / 12.0,
                                        onLongPress = { lendActionTarget = lend },
                                        onRepaid = { repaidConfirmTarget = lend }
                                    )
                                }
                                LoanPagerDots(
                                    total = activeLendEntries.size,
                                    current = safeLendPage
                                )
                            }
                        }
                        item { SectionTitle("Payment History") }
                        if (selectedLendSchedule.isEmpty()) {
                            item { EmptyFinancialCard("No payment history available for this money lend.") }
                        } else {
                            items(
                                selectedLendSchedule,
                                key = { "${selectedLend?.id ?: 0}_${it.installmentNumber}_${it.dueDateEpochDay}" }
                            ) { row ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    shape = RoundedCornerShape(18.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
                                            Text("#${row.installmentNumber} • ${formatEpochDay(row.dueDateEpochDay)}", fontWeight = FontWeight.SemiBold)
                                            Text("Interest ₹${formatIndianAmount(row.amountDue, 0)}", style = MaterialTheme.typography.bodySmall)
                                        }
                                        if (row.isPaid) {
                                            Text("Paid", color = Color(0xFF1F6D49), fontWeight = FontWeight.SemiBold)
                                        } else {
                                            TextButton(
                                                modifier = Modifier.background(Color(0xFF1F6D49), RoundedCornerShape(12.dp)),
                                                onClick = {
                                                    selectedLend?.let { lend ->
                                                        viewModel.markMoneyLendInstallmentPaid(lend.id, row.installmentNumber)
                                                    }
                                                }
                                            ) {
                                                Text("Mark Paid", color = Color.White)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }
    }

    if (showAddLoan) {
        var input by remember {
            mutableStateOf(
                LoanInput(
                    loanName = "",
                    lenderName = "",
                    principalAmount = "",
                    annualInterestRate = "",
                    tenureMonths = "",
                    paymentFrequency = "Monthly",
                    startDate = formatTodayUi()
                )
            )
        }
        var frequencyMenuExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddLoan = false },
            title = { Text("Add Loan") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = input.loanName,
                        onValueChange = { input = input.copy(loanName = it) },
                        label = { Text("Loan Name") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = input.lenderName,
                        onValueChange = { input = input.copy(lenderName = it) },
                        label = { Text("Bank / Lender") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = input.principalAmount,
                        onValueChange = { input = input.copy(principalAmount = it) },
                        label = { Text("Principal Amount") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = input.annualInterestRate,
                        onValueChange = { input = input.copy(annualInterestRate = it) },
                        label = { Text("Interest Rate %") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = input.tenureMonths,
                        onValueChange = { input = input.copy(tenureMonths = it) },
                        label = { Text(if (input.paymentFrequency == "Yearly") "Tenure (Years)" else "Tenure (Months)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Box {
                        OutlinedButton(
                            onClick = { frequencyMenuExpanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Payment Frequency: ${input.paymentFrequency}")
                        }
                        DropdownMenu(
                            expanded = frequencyMenuExpanded,
                            onDismissRequest = { frequencyMenuExpanded = false }
                        ) {
                            listOf("Monthly", "Yearly").forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        input = input.copy(paymentFrequency = option)
                                        frequencyMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    DatePickerField(
                        label = "Start Date",
                        value = input.startDate,
                        onDateSelected = { input = input.copy(startDate = it) }
                    )
                    if (addError) {
                        Text("Please enter valid loan details.", color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val ok = viewModel.addLoan(input)
                    if (ok) {
                        addError = false
                        showAddLoan = false
                    } else {
                        addError = true
                    }
                }) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showAddLoan = false }) { Text("Cancel") }
            }
        )
    }

    if (showAddLend) {
        var input by remember {
            mutableStateOf(
                MoneyLendInput(
                    borrowerName = "",
                    amount = "",
                    interestRate = "",
                    startDate = formatTodayUi(),
                    dueDate = "",
                    notes = ""
                )
            )
        }
        AlertDialog(
            onDismissRequest = { showAddLend = false },
            title = { Text("Add Lending") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = input.borrowerName,
                        onValueChange = { input = input.copy(borrowerName = it) },
                        label = { Text("Borrower Name") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = input.amount,
                        onValueChange = { input = input.copy(amount = it) },
                        label = { Text("Amount") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = input.interestRate,
                        onValueChange = { input = input.copy(interestRate = it) },
                        label = { Text("Interest Rate % (Optional)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    DatePickerField(
                        label = "Start Date",
                        value = input.startDate,
                        onDateSelected = { input = input.copy(startDate = it) }
                    )
                    DatePickerField(
                        label = "Due Date (Optional)",
                        value = input.dueDate,
                        onDateSelected = { input = input.copy(dueDate = it) }
                    )
                    OutlinedTextField(
                        value = input.notes,
                        onValueChange = { input = input.copy(notes = it) },
                        label = { Text("Notes (Optional)") },
                        singleLine = true
                    )
                    if (addLendError) {
                        Text("Please enter valid lending details.", color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val ok = viewModel.addMoneyLend(input)
                    if (ok) {
                        addLendError = false
                        showAddLend = false
                    } else {
                        addLendError = true
                    }
                }) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showAddLend = false }) { Text("Cancel") }
            }
        )
    }

    loanActionTarget?.let { loan ->
        AlertDialog(
            onDismissRequest = { loanActionTarget = null },
            title = { Text("Loan Options") },
            text = { Text("Choose an action for ${loan.loanName}.") },
            confirmButton = {
                TextButton(onClick = {
                    editLoanTarget = loan
                    loanActionTarget = null
                }) { Text("Edit") }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.deleteLoan(loan.id)
                    loanActionTarget = null
                }) { Text("Delete") }
            }
        )
    }

    editLoanTarget?.let { loan ->
        var input by remember(loan.id) {
            mutableStateOf(
                LoanInput(
                    loanName = loan.loanName,
                    lenderName = loan.lenderName,
                    principalAmount = loan.principalAmount.toString(),
                    annualInterestRate = loan.annualInterestRate.toString(),
                    tenureMonths = loan.tenureMonths.toString(),
                    paymentFrequency = loan.paymentFrequency,
                    startDate = formatEpochDay(loan.startDateEpochDay)
                )
            )
        }
        var frequencyMenuExpanded by remember(editLoanTarget?.id) { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { editLoanTarget = null },
            title = { Text("Edit Loan") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = input.loanName,
                        onValueChange = { input = input.copy(loanName = it) },
                        label = { Text("Loan Name") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = input.lenderName,
                        onValueChange = { input = input.copy(lenderName = it) },
                        label = { Text("Bank / Lender") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = input.principalAmount,
                        onValueChange = { input = input.copy(principalAmount = it) },
                        label = { Text("Principal Amount") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = input.annualInterestRate,
                        onValueChange = { input = input.copy(annualInterestRate = it) },
                        label = { Text("Interest Rate %") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = input.tenureMonths,
                        onValueChange = { input = input.copy(tenureMonths = it) },
                        label = { Text(if (input.paymentFrequency == "Yearly") "Tenure (Years)" else "Tenure (Months)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Box {
                        OutlinedButton(
                            onClick = { frequencyMenuExpanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Payment Frequency: ${input.paymentFrequency}")
                        }
                        DropdownMenu(
                            expanded = frequencyMenuExpanded,
                            onDismissRequest = { frequencyMenuExpanded = false }
                        ) {
                            listOf("Monthly", "Yearly").forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        input = input.copy(paymentFrequency = option)
                                        frequencyMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    DatePickerField(
                        label = "Start Date",
                        value = input.startDate,
                        onDateSelected = { input = input.copy(startDate = it) }
                    )
                    if (addError) {
                        Text("Please enter valid loan details.", color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val ok = viewModel.editLoan(loan.id, input)
                    if (ok) {
                        addError = false
                        editLoanTarget = null
                    } else {
                        addError = true
                    }
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { editLoanTarget = null }) { Text("Cancel") }
            }
        )
    }

    lendActionTarget?.let { lend ->
        AlertDialog(
            onDismissRequest = { lendActionTarget = null },
            title = { Text("Lending Options") },
            text = { Text("Choose an action for ${lend.borrowerName}.") },
            confirmButton = {
                TextButton(onClick = {
                    editLendTarget = lend
                    lendActionTarget = null
                }) { Text("Edit") }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.deleteMoneyLend(lend.id)
                    lendActionTarget = null
                }) { Text("Delete") }
            }
        )
    }

    editLendTarget?.let { lend ->
        var input by remember(lend.id) {
            mutableStateOf(
                MoneyLendInput(
                    borrowerName = lend.borrowerName,
                    amount = lend.amount.toString(),
                    interestRate = lend.interestRate.toString(),
                    startDate = formatEpochDay(lend.startDateEpochDay),
                    dueDate = lend.dueDateEpochDay?.let { formatEpochDay(it) } ?: "",
                    notes = lend.notes
                )
            )
        }
        AlertDialog(
            onDismissRequest = { editLendTarget = null },
            title = { Text("Edit Lending") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = input.borrowerName,
                        onValueChange = { input = input.copy(borrowerName = it) },
                        label = { Text("Borrower Name") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = input.amount,
                        onValueChange = { input = input.copy(amount = it) },
                        label = { Text("Amount") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = input.interestRate,
                        onValueChange = { input = input.copy(interestRate = it) },
                        label = { Text("Interest Rate % (Optional)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    DatePickerField(
                        label = "Start Date",
                        value = input.startDate,
                        onDateSelected = { input = input.copy(startDate = it) }
                    )
                    DatePickerField(
                        label = "Due Date (Optional)",
                        value = input.dueDate,
                        onDateSelected = { input = input.copy(dueDate = it) }
                    )
                    OutlinedTextField(
                        value = input.notes,
                        onValueChange = { input = input.copy(notes = it) },
                        label = { Text("Notes (Optional)") },
                        singleLine = true
                    )
                    if (addLendError) {
                        Text("Please enter valid lending details.", color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val ok = viewModel.editMoneyLend(lend.id, input)
                    if (ok) {
                        addLendError = false
                        editLendTarget = null
                    } else {
                        addLendError = true
                    }
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { editLendTarget = null }) { Text("Cancel") }
            }
        )
    }

    repaidConfirmTarget?.let { lend ->
        var confirmation by remember(lend.id) { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { repaidConfirmTarget = null },
            title = { Text("Confirm Repaid") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Type `paid` to mark this lending as repaid and close the record.")
                    OutlinedTextField(
                        value = confirmation,
                        onValueChange = { confirmation = it },
                        label = { Text("Type paid") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (confirmation.trim().equals("paid", ignoreCase = true)) {
                            viewModel.markMoneyLendRepaid(lend.id)
                            repaidConfirmTarget = null
                        }
                    }
                ) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { repaidConfirmTarget = null }) { Text("Cancel") }
            }
        )
    }

    if (showPrepaymentDialog && selectedLoan != null) {
        var amount by remember(selectedLoan.id) { mutableStateOf("") }
        var effectiveDate by remember(selectedLoan.id) { mutableStateOf(formatTodayUi()) }
        AlertDialog(
            onDismissRequest = { showPrepaymentDialog = false },
            title = { Text("Prepayment") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Apply prepayment to ${selectedLoan.loanName}")
                    DatePickerField(
                        label = "Effective date",
                        value = effectiveDate,
                        onDateSelected = { effectiveDate = it }
                    )
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Prepayment Amount") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val effectiveEpoch = com.uday.policytracker.util.parseFlexibleDate(effectiveDate) ?: -1L
                    val ok = viewModel.applyLoanPrepayment(
                        selectedLoan.id,
                        amount.toDoubleOrNull() ?: -1.0,
                        effectiveEpoch
                    )
                    if (ok) showPrepaymentDialog = false
                }) { Text("Apply") }
            },
            dismissButton = { TextButton(onClick = { showPrepaymentDialog = false }) { Text("Cancel") } }
        )
    }

    if (showRateChangeDialog && selectedLoan != null) {
        var rate by remember(selectedLoan.id) { mutableStateOf(selectedLoan.annualInterestRate.toString()) }
        var effectiveDate by remember(selectedLoan.id) { mutableStateOf(formatTodayUi()) }
        AlertDialog(
            onDismissRequest = { showRateChangeDialog = false },
            title = { Text("Interest Rate Change") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Update annual rate for ${selectedLoan.loanName}")
                    DatePickerField(
                        label = "Effective date",
                        value = effectiveDate,
                        onDateSelected = { effectiveDate = it }
                    )
                    OutlinedTextField(
                        value = rate,
                        onValueChange = { rate = it },
                        label = { Text("New Annual Rate %") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val effectiveEpoch = com.uday.policytracker.util.parseFlexibleDate(effectiveDate) ?: -1L
                    val ok = viewModel.applyLoanRateChange(
                        selectedLoan.id,
                        rate.toDoubleOrNull() ?: -1.0,
                        effectiveEpoch
                    )
                    if (ok) showRateChangeDialog = false
                }) { Text("Apply") }
            },
            dismissButton = { TextButton(onClick = { showRateChangeDialog = false }) { Text("Cancel") } }
        )
    }

    if (showCloseLoanDialog && selectedLoan != null) {
        AlertDialog(
            onDismissRequest = { showCloseLoanDialog = false },
            title = { Text("Close Loan") },
            text = { Text("Move ${selectedLoan.loanName} to closed history?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.closeLoan(selectedLoan.id, "Closed manually")
                    showCloseLoanDialog = false
                }) { Text("Close Loan") }
            },
            dismissButton = { TextButton(onClick = { showCloseLoanDialog = false }) { Text("Cancel") } }
        )
    }

    showActionInfo?.let { info ->
        AlertDialog(
            onDismissRequest = { showActionInfo = null },
            title = { Text("Next Step") },
            text = { Text(info) },
            confirmButton = { TextButton(onClick = { showActionInfo = null }) { Text("OK") } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FinancialHistoryScreen(
    tab: FinanceTab,
    closedLoans: List<ClosedLoanUiModel>,
    repaidLends: List<MoneyLendUiModel>,
    selectedClosedLoan: ClosedLoanUiModel?,
    selectedRepaidLend: MoneyLendUiModel?,
    onSelectClosedLoan: (ClosedLoanUiModel?) -> Unit,
    onSelectRepaidLend: (MoneyLendUiModel?) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (tab == FinanceTab.LOANS) "Closed Loans" else "Repaid Lendings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            tab == FinanceTab.LOANS && selectedClosedLoan != null -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    item {
                        Card(shape = RoundedCornerShape(18.dp)) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(selectedClosedLoan.loan.loanName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text("Bank: ${selectedClosedLoan.loan.lenderName}")
                                val installmentLabel = if (selectedClosedLoan.loan.paymentFrequency.equals("Yearly", ignoreCase = true)) "Yearly" else "EMI"
                                Text("Rate ${selectedClosedLoan.loan.annualInterestRate}% | $installmentLabel ₹${formatIndianAmount(selectedClosedLoan.loan.emiAmount, 0)}")
                                Text("Closed On: ${formatEpochDay(selectedClosedLoan.closedOnEpochDay)}")
                            }
                        }
                    }
                    item { SectionTitle("Payment History") }
                    val closedEventsSorted = selectedClosedLoan.events.sortedWith(
                        compareByDescending<LoanEventUiModel> { it.dateEpochDay }.thenByDescending { it.id }
                    )
                    if (closedEventsSorted.isNotEmpty()) {
                        items(closedEventsSorted, key = { "closed_event_${it.id}" }) { event ->
                            LoanEventCard(event = event)
                        }
                    }
                    items(selectedClosedLoan.payments, key = { it.id }) { payment ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (payment.isPaid) Color(0xFF1F6D49) else Color(0xFF25364A)
                            )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("#${payment.installmentNumber} • ${formatEpochDay(payment.dueDateEpochDay)}", color = Color.White)
                                Text(
                                    "₹${formatIndianAmount(payment.amountDue, 0)} ${if (payment.isPaid) "Paid" else "Unpaid"}",
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            tab == FinanceTab.LEND && selectedRepaidLend != null -> {
                val todayEpoch = LocalDate.now().toEpochDay()
                val schedule = buildMoneyLendSchedule(selectedRepaidLend, todayEpoch)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    item {
                        Card(shape = RoundedCornerShape(18.dp)) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(selectedRepaidLend.borrowerName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text("Principal: ₹${formatIndianAmount(selectedRepaidLend.amount, 0)}")
                                Text("Rate: ${selectedRepaidLend.interestRate}%")
                                Text("Start: ${formatEpochDay(selectedRepaidLend.startDateEpochDay)}")
                            }
                        }
                    }
                    item { SectionTitle("Payment History") }
                    if (schedule.isEmpty()) {
                        item { EmptyFinancialCard("No payment history available.") }
                    } else {
                        items(schedule, key = { it.installmentNumber }) { row ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (row.isPaid) Color(0xFF1F6D49) else Color(0xFF25364A)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("#${row.installmentNumber} • ${formatEpochDay(row.dueDateEpochDay)}", color = Color.White)
                                    Text("₹${formatIndianAmount(row.amountDue, 0)} ${if (row.isPaid) "Paid" else "Unpaid"}", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            tab == FinanceTab.LOANS -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    if (closedLoans.isEmpty()) {
                        item { EmptyFinancialCard("No closed loans yet.") }
                    } else {
                        items(closedLoans, key = { it.id }) { item ->
                            Card(
                                modifier = Modifier.fillMaxWidth().clickable { onSelectClosedLoan(item) },
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF26364A))
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(item.loan.loanName, color = Color.White, fontWeight = FontWeight.Bold)
                                        Text("Closed ${formatEpochDay(item.closedOnEpochDay)}", color = Color(0xFFD6E7FF))
                                    }
                                    Text("View", color = Color(0xFFFDE5A3))
                                }
                            }
                        }
                    }
                }
            }

            tab == FinanceTab.LEND -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    if (repaidLends.isEmpty()) {
                        item { EmptyFinancialCard("No repaid records yet.") }
                    } else {
                        items(repaidLends, key = { it.id }) { item ->
                            Card(
                                modifier = Modifier.fillMaxWidth().clickable { onSelectRepaidLend(item) },
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF26364A))
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(item.borrowerName, color = Color.White, fontWeight = FontWeight.Bold)
                                        Text("Principal ₹${formatIndianAmount(item.amount, 0)}", color = Color(0xFFD6E7FF))
                                    }
                                    Text("View", color = Color(0xFFFDE5A3))
                                }
                            }
                        }
                    }
                }
            }

            else -> {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    Text("No history available for this section.")
                }
            }
        }
    }
}

@Composable
private fun BubbleTabs(selectedTab: FinanceTab, onTab: (FinanceTab) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        FinanceTab.entries.forEach { tab ->
            val selected = tab == selectedTab
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTab(tab) },
                shape = RoundedCornerShape(30.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (selected) Color.White else Color(0xFF365276)
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        tab.label,
                        color = if (selected) Color(0xFF183456) else Color(0xFFD2E3FF),
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LoanSwipeCard(
    modifier: Modifier = Modifier,
    loan: LoanUiModel,
    balanceLeft: Double,
    monthsLeft: Int,
    onLongPress: () -> Unit = {}
) {
    Card(
        modifier = modifier.combinedClickable(
            onClick = {},
            onLongClick = onLongPress
        ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(listOf(Color(0xFF3A70B2), Color(0xFF2A5182))),
                    RoundedCornerShape(24.dp)
                )
                .height(188.dp)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                loan.loanName,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text("Bank: ${loan.lenderName}", color = Color(0xFFE1ECFF), style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(2.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Balance Left", color = Color(0xFFDBE9FF), style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "₹ ${formatIndianAmount(balanceLeft, 0)}",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val installmentLabel = if (loan.paymentFrequency.equals("Yearly", ignoreCase = true)) "Yearly" else "EMI"
                    Text(
                        "Rate ${loan.annualInterestRate}%  |  $installmentLabel ₹${formatIndianAmount(loan.emiAmount, 0)}",
                        color = Color(0xFFFDE5A3),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.End
                    )
                    Text(
                        "Payments Left: $monthsLeft",
                        color = Color(0xFFD6E7FF),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MoneyLendSwipeCard(
    modifier: Modifier = Modifier,
    lend: MoneyLendUiModel,
    monthlyInterest: Double,
    onLongPress: () -> Unit = {},
    onRepaid: () -> Unit = {}
) {
    Card(
        modifier = modifier.combinedClickable(
            onClick = {},
            onLongClick = onLongPress
        ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(listOf(Color(0xFF2F5F95), Color(0xFF1E3F63))),
                    RoundedCornerShape(24.dp)
                )
                .height(188.dp)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    lend.borrowerName,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                OutlinedButton(onClick = onRepaid) {
                    Text("Repaid")
                }
            }
            Text("Principal: ₹${formatIndianAmount(lend.amount, 0)}", color = Color(0xFFE1ECFF), style = MaterialTheme.typography.bodyMedium)
            Text("Start: ${formatEpochDay(lend.startDateEpochDay)}", color = Color(0xFFD8E8FF), style = MaterialTheme.typography.bodySmall)
            lend.dueDateEpochDay?.let { Text("Due: ${formatEpochDay(it)}", color = Color(0xFFD8E8FF), style = MaterialTheme.typography.bodySmall) }
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Spacer(modifier = Modifier.weight(1f))
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        "Rate ${lend.interestRate}%  |  Monthly ₹${formatIndianAmount(monthlyInterest, 0)}",
                        color = Color(0xFFFDE5A3),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

@Composable
private fun LoanPagerDots(total: Int, current: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(total) { index ->
            val selected = index == current
            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .size(if (selected) 8.dp else 6.dp)
                    .background(
                        color = if (selected) Color(0xFF3B6BB0) else Color(0xFF9AA8B8),
                        shape = CircleShape
                    )
            )
        }
    }
}

@Composable
private fun QuickActions(
    onPrepay: () -> Unit,
    onRateChange: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Quick Actions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ActionTile("Prepayment", Icons.Default.Paid, Color(0xFF1F6D49), Modifier.weight(1f), onPrepay)
            ActionTile("Rate Change", Icons.Default.ShowChart, Color(0xFF1E4E86), Modifier.weight(1f), onRateChange)
            ActionTile("Close Loan", Icons.Default.Savings, Color(0xFF6E4B12), Modifier.weight(1f), onClose)
        }
    }
}

@Composable
private fun ActionTile(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(82.dp)
                .padding(vertical = 8.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterVertically)
        ) {
            Box(
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
                    .padding(5.dp)
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
            Text(
                title,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                minLines = 1,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        modifier = Modifier.padding(horizontal = 16.dp),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun EmptyFinancialCard(text: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(18.dp)
    ) {
        Text(text, modifier = Modifier.padding(16.dp))
    }
}

@Composable
private fun LoanEventCard(event: LoanEventUiModel) {
    val label = when (event.type) {
        LoanEventType.PREPAYMENT -> "Prepayment"
        LoanEventType.RATE_CHANGE -> "Rate Change"
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D4055))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("$label • ${formatEpochDay(event.dateEpochDay)}", color = Color.White, fontWeight = FontWeight.Bold)
            when (event.type) {
                LoanEventType.PREPAYMENT -> {
                    Text("Amount: ₹${formatIndianAmount(event.amount ?: 0.0, 0)}", color = Color(0xFFD5E4FF))
                    if (event.newEmi != null) {
                        Text("Updated EMI: ₹${formatIndianAmount(event.newEmi, 0)}", color = Color(0xFFD5E4FF))
                    }
                }

                LoanEventType.RATE_CHANGE -> {
                    Text(
                        "Rate: ${event.oldRate ?: 0.0}% → ${event.newRate ?: 0.0}%",
                        color = Color(0xFFD5E4FF)
                    )
                    Text(
                        "EMI: ₹${formatIndianAmount(event.oldEmi ?: 0.0, 0)} → ₹${formatIndianAmount(event.newEmi ?: 0.0, 0)}",
                        color = Color(0xFFD5E4FF)
                    )
                }
            }
        }
    }
}

private fun calculateReceivableInterest(entry: MoneyLendUiModel, todayEpochDay: Long): Double {
    return buildMoneyLendSchedule(entry, todayEpochDay)
        .filterNot { it.isPaid }
        .sumOf { it.amountDue }
}

private data class MoneyLendScheduleRow(
    val installmentNumber: Int,
    val dueDateEpochDay: Long,
    val amountDue: Double,
    val isPaid: Boolean
)

private fun buildMoneyLendSchedule(entry: MoneyLendUiModel, todayEpochDay: Long): List<MoneyLendScheduleRow> {
    if (entry.interestRate <= 0.0) return emptyList()
    val startDate = LocalDate.ofEpochDay(entry.startDateEpochDay)
    val today = LocalDate.ofEpochDay(todayEpochDay)
    val dueLimit = entry.dueDateEpochDay?.let { LocalDate.ofEpochDay(it) }
    val endDate = listOfNotNull(today, dueLimit).minOrNull() ?: today
    if (endDate.isBefore(startDate.plusMonths(1))) return emptyList()
    val monthlyInterest = (entry.amount * entry.interestRate / 100.0) / 12.0

    var installment = 1
    val rows = mutableListOf<MoneyLendScheduleRow>()
    var dueDate = startDate.plusMonths(1)
    while (!dueDate.isAfter(endDate)) {
        rows += MoneyLendScheduleRow(
            installmentNumber = installment,
            dueDateEpochDay = dueDate.toEpochDay(),
            amountDue = monthlyInterest,
            isPaid = installment in entry.paidInstallments
        )
        installment += 1
        dueDate = startDate.plusMonths(installment.toLong())
    }
    return rows
}
