package com.uday.policytracker.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PolicyDao {
    @Transaction
    @Query("SELECT * FROM policies ORDER BY expiryDateEpochDay ASC")
    fun observePoliciesWithDetails(): Flow<List<PolicyWithDetails>>

    @Transaction
    @Query("SELECT * FROM policies WHERE id = :policyId LIMIT 1")
    fun observePolicyWithDetails(policyId: Long): Flow<PolicyWithDetails?>

    @Transaction
    @Query("SELECT * FROM policies WHERE id = :policyId LIMIT 1")
    suspend fun getPolicyWithDetailsById(policyId: Long): PolicyWithDetails?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPolicy(policy: PolicyEntity): Long

    @Update
    suspend fun updatePolicy(policy: PolicyEntity)

    @Delete
    suspend fun deletePolicy(policy: PolicyEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: PolicyHistoryEntity)

    @Update
    suspend fun updateHistory(history: PolicyHistoryEntity)

    @Query("SELECT * FROM policy_history WHERE id = :historyId LIMIT 1")
    suspend fun getHistoryById(historyId: Long): PolicyHistoryEntity?

    @Query("DELETE FROM policy_history WHERE id = :historyId")
    suspend fun deleteHistoryById(historyId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttachment(attachment: AttachmentEntity)

    @Query("UPDATE attachments SET displayName = :displayName WHERE id = :attachmentId")
    suspend fun renameAttachmentById(attachmentId: Long, displayName: String)

    @Query("DELETE FROM attachments WHERE id = :attachmentId")
    suspend fun deleteAttachmentById(attachmentId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPolicyCategoryDetails(details: PolicyCategoryDetailsEntity)

    @Query("SELECT * FROM policy_category_details WHERE policyId = :policyId LIMIT 1")
    suspend fun getPolicyCategoryDetails(policyId: Long): PolicyCategoryDetailsEntity?

    @Transaction
    @Query("SELECT * FROM category_folders WHERE category = :category ORDER BY startEpochDay DESC, name ASC")
    fun observeFoldersByCategory(category: String): Flow<List<FolderWithDetails>>

    @Transaction
    @Query("SELECT * FROM category_folders WHERE id = :folderId LIMIT 1")
    fun observeFolderById(folderId: Long): Flow<FolderWithDetails?>

    @Query("SELECT * FROM policies WHERE category = :category ORDER BY expiryDateEpochDay DESC")
    fun observePoliciesByCategory(category: String): Flow<List<PolicyEntity>>

    @Query("SELECT * FROM policies WHERE id = :policyId LIMIT 1")
    suspend fun getPolicyById(policyId: Long): PolicyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: CategoryFolderEntity): Long

    @Query("UPDATE category_folders SET name = :name WHERE id = :folderId")
    suspend fun renameFolder(folderId: Long, name: String)

    @Query("UPDATE category_folders SET colorHex = :colorHex WHERE id = :folderId")
    suspend fun updateFolderColor(folderId: Long, colorHex: String)

    @Query("DELETE FROM category_folders WHERE id = :folderId")
    suspend fun deleteFolderById(folderId: Long)

    @Query("SELECT * FROM category_folders ORDER BY createdAtEpochMillis DESC")
    fun observeAllFolders(): Flow<List<CategoryFolderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPolicyFolderTag(crossRef: PolicyFolderCrossRef)

    @Query("SELECT folderId FROM policy_folder_cross_ref WHERE policyId = :policyId")
    fun observePolicyFolderIds(policyId: Long): Flow<List<Long>>

    @Query("DELETE FROM policy_folder_cross_ref WHERE policyId = :policyId")
    suspend fun deletePolicyFolderTagsForPolicy(policyId: Long)

    @Query("DELETE FROM policy_folder_cross_ref WHERE policyId = :policyId AND folderId = :folderId")
    suspend fun removePolicyFolderTag(policyId: Long, folderId: Long)

    @Query("DELETE FROM policy_folder_cross_ref WHERE folderId = :folderId")
    suspend fun deletePolicyFolderTagsForFolder(folderId: Long)

    @Query(
        """
        SELECT 
            a.id AS attachmentId,
            a.policyId AS policyId,
            a.uri AS uri,
            a.displayName AS displayName,
            a.mimeType AS mimeType,
            a.addedAtEpochMillis AS addedAtEpochMillis,
            p.policyName AS policyName,
            p.policyNumber AS policyNumber,
            p.startDateEpochDay AS policyStartEpochDay,
            p.expiryDateEpochDay AS policyExpiryEpochDay
        FROM attachments a
        INNER JOIN policy_folder_cross_ref r ON r.policyId = a.policyId
        INNER JOIN policies p ON p.id = a.policyId
        WHERE r.folderId = :folderId
        ORDER BY p.startDateEpochDay DESC, a.addedAtEpochMillis DESC
        """
    )
    fun observePolicyAttachmentsByFolder(folderId: Long): Flow<List<FolderPolicyAttachmentRow>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolderAttachment(attachment: FolderAttachmentEntity)

    @Query("UPDATE folder_attachments SET displayName = :displayName WHERE id = :attachmentId")
    suspend fun renameFolderAttachmentById(attachmentId: Long, displayName: String)

    @Query("DELETE FROM folder_attachments WHERE id = :attachmentId")
    suspend fun deleteFolderAttachmentById(attachmentId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFuturePolicy(futurePolicy: FuturePolicyEntity)

    @Query("DELETE FROM future_policies WHERE id = :futureId")
    suspend fun deleteFuturePolicyById(futureId: Long)

    @Query("SELECT * FROM loans ORDER BY createdAtEpochMillis DESC")
    fun observeLoans(): Flow<List<LoanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoan(loan: LoanEntity): Long

    @Update
    suspend fun updateLoan(loan: LoanEntity)

    @Query("DELETE FROM loans WHERE id = :loanId")
    suspend fun deleteLoanById(loanId: Long)

    @Query("SELECT * FROM loans WHERE id = :loanId LIMIT 1")
    suspend fun getLoanById(loanId: Long): LoanEntity?

    @Query("SELECT * FROM loan_payments WHERE loanId = :loanId ORDER BY installmentNumber ASC")
    fun observeLoanPaymentsByLoan(loanId: Long): Flow<List<LoanPaymentEntity>>

    @Query("SELECT * FROM loan_payments ORDER BY loanId ASC, installmentNumber ASC")
    fun observeAllLoanPayments(): Flow<List<LoanPaymentEntity>>

    @Query("SELECT * FROM money_lend_entries ORDER BY createdAtEpochMillis DESC")
    fun observeMoneyLends(): Flow<List<MoneyLendEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoneyLend(entry: MoneyLendEntity): Long

    @Update
    suspend fun updateMoneyLend(entry: MoneyLendEntity)

    @Query("SELECT * FROM money_lend_entries WHERE id = :entryId LIMIT 1")
    suspend fun getMoneyLendById(entryId: Long): MoneyLendEntity?

    @Query("DELETE FROM money_lend_entries WHERE id = :entryId")
    suspend fun deleteMoneyLendById(entryId: Long)

    @Query("SELECT * FROM loan_payments WHERE loanId = :loanId ORDER BY installmentNumber ASC")
    suspend fun getLoanPaymentsByLoan(loanId: Long): List<LoanPaymentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoanPayments(items: List<LoanPaymentEntity>)

    @Query("DELETE FROM loan_payments WHERE loanId = :loanId")
    suspend fun deleteLoanPaymentsByLoanId(loanId: Long)

    @Query("UPDATE loan_payments SET isPaid = 1, paidOnEpochDay = :paidOnEpochDay WHERE id = :paymentId")
    suspend fun markLoanPaymentPaid(paymentId: Long, paidOnEpochDay: Long)

    @Query(
        """
        UPDATE loan_payments
        SET isPaid = 1, paidOnEpochDay = :todayEpochDay
        WHERE isPaid = 0 AND dueDateEpochDay < :todayEpochDay
        """
    )
    suspend fun autoMarkPastDueLoanPayments(todayEpochDay: Long)

    @Query("SELECT * FROM policies")
    suspend fun getAllPolicies(): List<PolicyEntity>

    @Query("SELECT * FROM policy_history")
    suspend fun getAllHistory(): List<PolicyHistoryEntity>

    @Query("SELECT * FROM attachments")
    suspend fun getAllAttachments(): List<AttachmentEntity>

    @Query("SELECT * FROM future_policies")
    suspend fun getAllFuturePolicies(): List<FuturePolicyEntity>

    @Query("SELECT * FROM policy_category_details")
    suspend fun getAllPolicyCategoryDetails(): List<PolicyCategoryDetailsEntity>

    @Query("SELECT * FROM category_folders")
    suspend fun getAllFolders(): List<CategoryFolderEntity>

    @Query("SELECT * FROM policy_folder_cross_ref")
    suspend fun getAllPolicyFolderRefs(): List<PolicyFolderCrossRef>

    @Query("SELECT * FROM folder_attachments")
    suspend fun getAllFolderAttachments(): List<FolderAttachmentEntity>

    @Query("SELECT * FROM loans")
    suspend fun getAllLoans(): List<LoanEntity>

    @Query("SELECT * FROM loan_payments")
    suspend fun getAllLoanPayments(): List<LoanPaymentEntity>

    @Query("SELECT * FROM money_lend_entries")
    suspend fun getAllMoneyLends(): List<MoneyLendEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPolicies(items: List<PolicyEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoryList(items: List<PolicyHistoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttachments(items: List<AttachmentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFuturePolicies(items: List<FuturePolicyEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPolicyCategoryDetails(items: List<PolicyCategoryDetailsEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolders(items: List<CategoryFolderEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPolicyFolderRefs(items: List<PolicyFolderCrossRef>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolderAttachments(items: List<FolderAttachmentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoans(items: List<LoanEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoanPaymentsBulk(items: List<LoanPaymentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoneyLends(items: List<MoneyLendEntity>)

    @Query("DELETE FROM folder_attachments")
    suspend fun clearFolderAttachments()

    @Query("DELETE FROM policy_folder_cross_ref")
    suspend fun clearPolicyFolderRefs()

    @Query("DELETE FROM category_folders")
    suspend fun clearFolders()

    @Query("DELETE FROM future_policies")
    suspend fun clearFuturePolicies()

    @Query("DELETE FROM policy_category_details")
    suspend fun clearPolicyCategoryDetails()

    @Query("DELETE FROM attachments")
    suspend fun clearAttachments()

    @Query("DELETE FROM policy_history")
    suspend fun clearHistory()

    @Query("DELETE FROM policies")
    suspend fun clearPolicies()

    @Query("DELETE FROM loan_payments")
    suspend fun clearLoanPayments()

    @Query("DELETE FROM loans")
    suspend fun clearLoans()

    @Query("DELETE FROM money_lend_entries")
    suspend fun clearMoneyLends()
}
