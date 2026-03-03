package com.uday.policytracker.data.repo

import com.uday.policytracker.data.db.AttachmentEntity
import com.uday.policytracker.data.db.CategoryFolderEntity
import com.uday.policytracker.data.db.FolderAttachmentEntity
import com.uday.policytracker.data.db.FolderPolicyAttachmentRow
import com.uday.policytracker.data.db.FolderWithDetails
import com.uday.policytracker.data.db.FuturePolicyEntity
import com.uday.policytracker.data.db.LoanEntity
import com.uday.policytracker.data.db.LoanPaymentEntity
import com.uday.policytracker.data.db.MoneyLendEntity
import com.uday.policytracker.data.db.PolicyDao
import com.uday.policytracker.data.db.PolicyCategoryDetailsEntity
import com.uday.policytracker.data.db.PolicyEntity
import com.uday.policytracker.data.db.PolicyFolderCrossRef
import com.uday.policytracker.data.db.PolicyHistoryEntity
import com.uday.policytracker.data.db.PolicyWithDetails
import kotlinx.coroutines.flow.Flow

class PolicyRepository(private val dao: PolicyDao) {
    fun observePolicies(): Flow<List<PolicyWithDetails>> = dao.observePoliciesWithDetails()

    fun observePolicy(policyId: Long): Flow<PolicyWithDetails?> = dao.observePolicyWithDetails(policyId)

    suspend fun getPolicyWithDetails(policyId: Long): PolicyWithDetails? = dao.getPolicyWithDetailsById(policyId)

    suspend fun insertPolicy(policy: PolicyEntity): Long = dao.insertPolicy(policy)

    suspend fun updatePolicy(policy: PolicyEntity) = dao.updatePolicy(policy)

    suspend fun deletePolicy(policy: PolicyEntity) = dao.deletePolicy(policy)

    suspend fun addHistory(entry: PolicyHistoryEntity) = dao.insertHistory(entry)

    suspend fun updateHistory(entry: PolicyHistoryEntity) = dao.updateHistory(entry)

    suspend fun getHistoryById(historyId: Long): PolicyHistoryEntity? = dao.getHistoryById(historyId)

    suspend fun deleteHistory(historyId: Long) = dao.deleteHistoryById(historyId)

    suspend fun addAttachment(attachment: AttachmentEntity) = dao.insertAttachment(attachment)

    suspend fun renameAttachment(attachmentId: Long, displayName: String) =
        dao.renameAttachmentById(attachmentId, displayName)

    suspend fun deleteAttachment(attachmentId: Long) = dao.deleteAttachmentById(attachmentId)

    suspend fun upsertPolicyCategoryDetails(details: PolicyCategoryDetailsEntity) =
        dao.upsertPolicyCategoryDetails(details)

    suspend fun getPolicyCategoryDetails(policyId: Long): PolicyCategoryDetailsEntity? =
        dao.getPolicyCategoryDetails(policyId)

    fun observeFoldersByCategory(category: String): Flow<List<FolderWithDetails>> = dao.observeFoldersByCategory(category)

    fun observeFolderById(folderId: Long): Flow<FolderWithDetails?> = dao.observeFolderById(folderId)

    fun observePoliciesByCategory(category: String): Flow<List<PolicyEntity>> = dao.observePoliciesByCategory(category)

    suspend fun getPolicyById(policyId: Long): PolicyEntity? = dao.getPolicyById(policyId)

    fun observeAllFolders(): Flow<List<CategoryFolderEntity>> = dao.observeAllFolders()

    suspend fun createFolder(folder: CategoryFolderEntity): Long = dao.insertFolder(folder)

    suspend fun renameFolder(folderId: Long, name: String) = dao.renameFolder(folderId, name)

    suspend fun updateFolderColor(folderId: Long, colorHex: String) = dao.updateFolderColor(folderId, colorHex)

    suspend fun deleteFolder(folderId: Long) {
        dao.deletePolicyFolderTagsForFolder(folderId)
        dao.deleteFolderById(folderId)
    }

    suspend fun addPolicyToFolder(policyId: Long, folderId: Long) =
        dao.insertPolicyFolderTag(PolicyFolderCrossRef(policyId = policyId, folderId = folderId))

    fun observePolicyFolderIds(policyId: Long): Flow<List<Long>> = dao.observePolicyFolderIds(policyId)

    suspend fun replacePolicyFolderTags(policyId: Long, folderIds: Set<Long>) {
        dao.deletePolicyFolderTagsForPolicy(policyId)
        if (folderIds.isNotEmpty()) {
            dao.insertPolicyFolderRefs(folderIds.map { folderId ->
                PolicyFolderCrossRef(policyId = policyId, folderId = folderId)
            })
        }
    }

    suspend fun removePolicyFromFolder(policyId: Long, folderId: Long) =
        dao.removePolicyFolderTag(policyId = policyId, folderId = folderId)

    fun observePolicyAttachmentsByFolder(folderId: Long): Flow<List<FolderPolicyAttachmentRow>> =
        dao.observePolicyAttachmentsByFolder(folderId)

    suspend fun addFolderAttachment(attachment: FolderAttachmentEntity) = dao.insertFolderAttachment(attachment)

    suspend fun renameFolderAttachment(attachmentId: Long, displayName: String) =
        dao.renameFolderAttachmentById(attachmentId, displayName)

    suspend fun deleteFolderAttachment(attachmentId: Long) = dao.deleteFolderAttachmentById(attachmentId)

    suspend fun addFuturePolicy(futurePolicy: FuturePolicyEntity) = dao.insertFuturePolicy(futurePolicy)

    suspend fun deleteFuturePolicy(futureId: Long) = dao.deleteFuturePolicyById(futureId)

    fun observeLoans(): Flow<List<LoanEntity>> = dao.observeLoans()

    suspend fun addLoan(loan: LoanEntity): Long = dao.insertLoan(loan)

    suspend fun updateLoan(loan: LoanEntity) = dao.updateLoan(loan)

    suspend fun deleteLoan(loanId: Long) = dao.deleteLoanById(loanId)

    suspend fun getLoanById(loanId: Long) = dao.getLoanById(loanId)

    fun observeLoanPayments(loanId: Long): Flow<List<LoanPaymentEntity>> = dao.observeLoanPaymentsByLoan(loanId)
    fun observeAllLoanPayments(): Flow<List<LoanPaymentEntity>> = dao.observeAllLoanPayments()
    fun observeMoneyLends(): Flow<List<MoneyLendEntity>> = dao.observeMoneyLends()

    suspend fun getLoanPaymentsByLoan(loanId: Long) = dao.getLoanPaymentsByLoan(loanId)
    suspend fun getMoneyLendById(entryId: Long) = dao.getMoneyLendById(entryId)

    suspend fun addLoanPayments(payments: List<LoanPaymentEntity>) = dao.insertLoanPayments(payments)
    suspend fun addMoneyLend(entry: MoneyLendEntity): Long = dao.insertMoneyLend(entry)
    suspend fun updateMoneyLend(entry: MoneyLendEntity) = dao.updateMoneyLend(entry)
    suspend fun deleteMoneyLend(entryId: Long) = dao.deleteMoneyLendById(entryId)

    suspend fun replaceLoanPayments(loanId: Long, payments: List<LoanPaymentEntity>) {
        dao.deleteLoanPaymentsByLoanId(loanId)
        dao.insertLoanPayments(payments)
    }

    suspend fun markLoanPaymentPaid(paymentId: Long, paidOnEpochDay: Long) = dao.markLoanPaymentPaid(paymentId, paidOnEpochDay)

    suspend fun autoMarkPastDueLoanPayments(todayEpochDay: Long) = dao.autoMarkPastDueLoanPayments(todayEpochDay)

    suspend fun getAllPolicies() = dao.getAllPolicies()
    suspend fun getAllHistory() = dao.getAllHistory()
    suspend fun getAllAttachments() = dao.getAllAttachments()
    suspend fun getAllFuturePolicies() = dao.getAllFuturePolicies()
    suspend fun getAllPolicyCategoryDetails() = dao.getAllPolicyCategoryDetails()
    suspend fun getAllFolders() = dao.getAllFolders()
    suspend fun getAllPolicyFolderRefs() = dao.getAllPolicyFolderRefs()
    suspend fun getAllFolderAttachments() = dao.getAllFolderAttachments()
    suspend fun getAllLoans() = dao.getAllLoans()
    suspend fun getAllLoanPayments() = dao.getAllLoanPayments()
    suspend fun getAllMoneyLends() = dao.getAllMoneyLends()

    suspend fun restoreAll(
        policies: List<PolicyEntity>,
        history: List<PolicyHistoryEntity>,
        attachments: List<AttachmentEntity>,
        futurePolicies: List<FuturePolicyEntity>,
        policyCategoryDetails: List<PolicyCategoryDetailsEntity>,
        folders: List<CategoryFolderEntity>,
        refs: List<PolicyFolderCrossRef>,
        folderAttachments: List<FolderAttachmentEntity>,
        loans: List<LoanEntity>,
        loanPayments: List<LoanPaymentEntity>,
        moneyLends: List<MoneyLendEntity>
    ) {
        dao.clearMoneyLends()
        dao.clearLoanPayments()
        dao.clearLoans()
        dao.clearFolderAttachments()
        dao.clearPolicyFolderRefs()
        dao.clearFolders()
        dao.clearFuturePolicies()
        dao.clearPolicyCategoryDetails()
        dao.clearAttachments()
        dao.clearHistory()
        dao.clearPolicies()

        dao.insertPolicies(policies)
        dao.insertHistoryList(history)
        dao.insertAttachments(attachments)
        dao.insertFuturePolicies(futurePolicies)
        dao.insertPolicyCategoryDetails(policyCategoryDetails)
        dao.insertFolders(folders)
        dao.insertPolicyFolderRefs(refs)
        dao.insertFolderAttachments(folderAttachments)
        dao.insertLoans(loans)
        dao.insertLoanPaymentsBulk(loanPayments)
        dao.insertMoneyLends(moneyLends)
    }
}
