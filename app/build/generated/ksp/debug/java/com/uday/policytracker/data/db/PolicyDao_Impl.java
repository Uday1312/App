package com.uday.policytracker.data.db;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.LongSparseArray;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.RelationUtil;
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Double;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class PolicyDao_Impl implements PolicyDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<PolicyEntity> __insertionAdapterOfPolicyEntity;

  private final EntityInsertionAdapter<PolicyHistoryEntity> __insertionAdapterOfPolicyHistoryEntity;

  private final EntityInsertionAdapter<AttachmentEntity> __insertionAdapterOfAttachmentEntity;

  private final EntityInsertionAdapter<PolicyCategoryDetailsEntity> __insertionAdapterOfPolicyCategoryDetailsEntity;

  private final EntityInsertionAdapter<CategoryFolderEntity> __insertionAdapterOfCategoryFolderEntity;

  private final EntityInsertionAdapter<PolicyFolderCrossRef> __insertionAdapterOfPolicyFolderCrossRef;

  private final EntityInsertionAdapter<FolderAttachmentEntity> __insertionAdapterOfFolderAttachmentEntity;

  private final EntityInsertionAdapter<FuturePolicyEntity> __insertionAdapterOfFuturePolicyEntity;

  private final EntityInsertionAdapter<LoanEntity> __insertionAdapterOfLoanEntity;

  private final EntityInsertionAdapter<MoneyLendEntity> __insertionAdapterOfMoneyLendEntity;

  private final EntityInsertionAdapter<LoanPaymentEntity> __insertionAdapterOfLoanPaymentEntity;

  private final EntityInsertionAdapter<PolicyEntity> __insertionAdapterOfPolicyEntity_1;

  private final EntityDeletionOrUpdateAdapter<PolicyEntity> __deletionAdapterOfPolicyEntity;

  private final EntityDeletionOrUpdateAdapter<PolicyEntity> __updateAdapterOfPolicyEntity;

  private final EntityDeletionOrUpdateAdapter<PolicyHistoryEntity> __updateAdapterOfPolicyHistoryEntity;

  private final EntityDeletionOrUpdateAdapter<LoanEntity> __updateAdapterOfLoanEntity;

  private final EntityDeletionOrUpdateAdapter<MoneyLendEntity> __updateAdapterOfMoneyLendEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteHistoryById;

  private final SharedSQLiteStatement __preparedStmtOfRenameAttachmentById;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAttachmentById;

  private final SharedSQLiteStatement __preparedStmtOfRenameFolder;

  private final SharedSQLiteStatement __preparedStmtOfUpdateFolderColor;

  private final SharedSQLiteStatement __preparedStmtOfDeleteFolderById;

  private final SharedSQLiteStatement __preparedStmtOfDeletePolicyFolderTagsForPolicy;

  private final SharedSQLiteStatement __preparedStmtOfRemovePolicyFolderTag;

  private final SharedSQLiteStatement __preparedStmtOfDeletePolicyFolderTagsForFolder;

  private final SharedSQLiteStatement __preparedStmtOfRenameFolderAttachmentById;

  private final SharedSQLiteStatement __preparedStmtOfDeleteFolderAttachmentById;

  private final SharedSQLiteStatement __preparedStmtOfDeleteFuturePolicyById;

  private final SharedSQLiteStatement __preparedStmtOfDeleteLoanById;

  private final SharedSQLiteStatement __preparedStmtOfDeleteMoneyLendById;

  private final SharedSQLiteStatement __preparedStmtOfDeleteLoanPaymentsByLoanId;

  private final SharedSQLiteStatement __preparedStmtOfMarkLoanPaymentPaid;

  private final SharedSQLiteStatement __preparedStmtOfAutoMarkPastDueLoanPayments;

  private final SharedSQLiteStatement __preparedStmtOfClearFolderAttachments;

  private final SharedSQLiteStatement __preparedStmtOfClearPolicyFolderRefs;

  private final SharedSQLiteStatement __preparedStmtOfClearFolders;

  private final SharedSQLiteStatement __preparedStmtOfClearFuturePolicies;

  private final SharedSQLiteStatement __preparedStmtOfClearPolicyCategoryDetails;

  private final SharedSQLiteStatement __preparedStmtOfClearAttachments;

  private final SharedSQLiteStatement __preparedStmtOfClearHistory;

  private final SharedSQLiteStatement __preparedStmtOfClearPolicies;

  private final SharedSQLiteStatement __preparedStmtOfClearLoanPayments;

  private final SharedSQLiteStatement __preparedStmtOfClearLoans;

  private final SharedSQLiteStatement __preparedStmtOfClearMoneyLends;

  public PolicyDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfPolicyEntity = new EntityInsertionAdapter<PolicyEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `policies` (`id`,`category`,`policyHolderName`,`policyName`,`policyNumber`,`startDateEpochDay`,`expiryDateEpochDay`,`insurerName`,`previousInsurerName`,`premiumAmount`,`notes`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PolicyEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getCategory());
        statement.bindString(3, entity.getPolicyHolderName());
        statement.bindString(4, entity.getPolicyName());
        statement.bindString(5, entity.getPolicyNumber());
        statement.bindLong(6, entity.getStartDateEpochDay());
        statement.bindLong(7, entity.getExpiryDateEpochDay());
        statement.bindString(8, entity.getInsurerName());
        statement.bindString(9, entity.getPreviousInsurerName());
        statement.bindDouble(10, entity.getPremiumAmount());
        statement.bindString(11, entity.getNotes());
      }
    };
    this.__insertionAdapterOfPolicyHistoryEntity = new EntityInsertionAdapter<PolicyHistoryEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `policy_history` (`id`,`policyId`,`policyHolderName`,`insurerName`,`policyNumber`,`startDateEpochDay`,`endDateEpochDay`,`premiumAmount`,`attachmentRefs`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PolicyHistoryEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getPolicyId());
        statement.bindString(3, entity.getPolicyHolderName());
        statement.bindString(4, entity.getInsurerName());
        statement.bindString(5, entity.getPolicyNumber());
        statement.bindLong(6, entity.getStartDateEpochDay());
        statement.bindLong(7, entity.getEndDateEpochDay());
        statement.bindDouble(8, entity.getPremiumAmount());
        statement.bindString(9, entity.getAttachmentRefs());
      }
    };
    this.__insertionAdapterOfAttachmentEntity = new EntityInsertionAdapter<AttachmentEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `attachments` (`id`,`policyId`,`uri`,`displayName`,`mimeType`,`addedAtEpochMillis`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AttachmentEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getPolicyId());
        statement.bindString(3, entity.getUri());
        statement.bindString(4, entity.getDisplayName());
        statement.bindString(5, entity.getMimeType());
        statement.bindLong(6, entity.getAddedAtEpochMillis());
      }
    };
    this.__insertionAdapterOfPolicyCategoryDetailsEntity = new EntityInsertionAdapter<PolicyCategoryDetailsEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `policy_category_details` (`policyId`,`premiumFrequency`,`premiumDueDayOfMonth`,`coverageAmount`,`coverageAmountUnit`,`premiumPaymentStartEpochDay`,`premiumPaymentEndEpochDay`,`premiumPaymentTermYears`,`policyValidityEndEpochDay`,`policyTermYears`,`entryAge`,`coverageTillAge`,`nomineeName`,`nomineeRelationship`,`riderAddons`,`paymentMode`,`gracePeriodDays`,`termPolicyStatus`,`totalPayments`,`paidPayments`,`vehicleNumber`,`vehicleType`,`makeModelVariant`,`fuelType`,`vehiclePolicyType`,`vehicleAddons`,`claimHistory`,`documentType`,`issuingRto`,`stateName`,`vehicleClass`,`ownerName`,`linkedVehicleNumber`,`dateOfBirth`,`customFieldValuesJson`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PolicyCategoryDetailsEntity entity) {
        statement.bindLong(1, entity.getPolicyId());
        statement.bindString(2, entity.getPremiumFrequency());
        if (entity.getPremiumDueDayOfMonth() == null) {
          statement.bindNull(3);
        } else {
          statement.bindLong(3, entity.getPremiumDueDayOfMonth());
        }
        if (entity.getCoverageAmount() == null) {
          statement.bindNull(4);
        } else {
          statement.bindDouble(4, entity.getCoverageAmount());
        }
        statement.bindString(5, entity.getCoverageAmountUnit());
        if (entity.getPremiumPaymentStartEpochDay() == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, entity.getPremiumPaymentStartEpochDay());
        }
        if (entity.getPremiumPaymentEndEpochDay() == null) {
          statement.bindNull(7);
        } else {
          statement.bindLong(7, entity.getPremiumPaymentEndEpochDay());
        }
        if (entity.getPremiumPaymentTermYears() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getPremiumPaymentTermYears());
        }
        if (entity.getPolicyValidityEndEpochDay() == null) {
          statement.bindNull(9);
        } else {
          statement.bindLong(9, entity.getPolicyValidityEndEpochDay());
        }
        if (entity.getPolicyTermYears() == null) {
          statement.bindNull(10);
        } else {
          statement.bindLong(10, entity.getPolicyTermYears());
        }
        if (entity.getEntryAge() == null) {
          statement.bindNull(11);
        } else {
          statement.bindLong(11, entity.getEntryAge());
        }
        if (entity.getCoverageTillAge() == null) {
          statement.bindNull(12);
        } else {
          statement.bindLong(12, entity.getCoverageTillAge());
        }
        statement.bindString(13, entity.getNomineeName());
        statement.bindString(14, entity.getNomineeRelationship());
        statement.bindString(15, entity.getRiderAddons());
        statement.bindString(16, entity.getPaymentMode());
        if (entity.getGracePeriodDays() == null) {
          statement.bindNull(17);
        } else {
          statement.bindLong(17, entity.getGracePeriodDays());
        }
        statement.bindString(18, entity.getTermPolicyStatus());
        if (entity.getTotalPayments() == null) {
          statement.bindNull(19);
        } else {
          statement.bindLong(19, entity.getTotalPayments());
        }
        if (entity.getPaidPayments() == null) {
          statement.bindNull(20);
        } else {
          statement.bindLong(20, entity.getPaidPayments());
        }
        statement.bindString(21, entity.getVehicleNumber());
        statement.bindString(22, entity.getVehicleType());
        statement.bindString(23, entity.getMakeModelVariant());
        statement.bindString(24, entity.getFuelType());
        statement.bindString(25, entity.getVehiclePolicyType());
        statement.bindString(26, entity.getVehicleAddons());
        statement.bindString(27, entity.getClaimHistory());
        statement.bindString(28, entity.getDocumentType());
        statement.bindString(29, entity.getIssuingRto());
        statement.bindString(30, entity.getStateName());
        statement.bindString(31, entity.getVehicleClass());
        statement.bindString(32, entity.getOwnerName());
        statement.bindString(33, entity.getLinkedVehicleNumber());
        statement.bindString(34, entity.getDateOfBirth());
        statement.bindString(35, entity.getCustomFieldValuesJson());
      }
    };
    this.__insertionAdapterOfCategoryFolderEntity = new EntityInsertionAdapter<CategoryFolderEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `category_folders` (`id`,`category`,`name`,`startEpochDay`,`endEpochDay`,`createdAtEpochMillis`,`colorHex`) VALUES (nullif(?, 0),?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final CategoryFolderEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getCategory());
        statement.bindString(3, entity.getName());
        statement.bindLong(4, entity.getStartEpochDay());
        statement.bindLong(5, entity.getEndEpochDay());
        statement.bindLong(6, entity.getCreatedAtEpochMillis());
        statement.bindString(7, entity.getColorHex());
      }
    };
    this.__insertionAdapterOfPolicyFolderCrossRef = new EntityInsertionAdapter<PolicyFolderCrossRef>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `policy_folder_cross_ref` (`policyId`,`folderId`) VALUES (?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PolicyFolderCrossRef entity) {
        statement.bindLong(1, entity.getPolicyId());
        statement.bindLong(2, entity.getFolderId());
      }
    };
    this.__insertionAdapterOfFolderAttachmentEntity = new EntityInsertionAdapter<FolderAttachmentEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `folder_attachments` (`id`,`folderId`,`uri`,`displayName`,`mimeType`,`addedAtEpochMillis`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final FolderAttachmentEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getFolderId());
        statement.bindString(3, entity.getUri());
        statement.bindString(4, entity.getDisplayName());
        statement.bindString(5, entity.getMimeType());
        statement.bindLong(6, entity.getAddedAtEpochMillis());
      }
    };
    this.__insertionAdapterOfFuturePolicyEntity = new EntityInsertionAdapter<FuturePolicyEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `future_policies` (`id`,`policyId`,`policyHolderName`,`policyName`,`policyNumber`,`startDateEpochDay`,`expiryDateEpochDay`,`insurerName`,`premiumAmount`,`createdAtEpochMillis`,`attachmentRefs`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final FuturePolicyEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getPolicyId());
        statement.bindString(3, entity.getPolicyHolderName());
        statement.bindString(4, entity.getPolicyName());
        statement.bindString(5, entity.getPolicyNumber());
        statement.bindLong(6, entity.getStartDateEpochDay());
        statement.bindLong(7, entity.getExpiryDateEpochDay());
        statement.bindString(8, entity.getInsurerName());
        statement.bindDouble(9, entity.getPremiumAmount());
        statement.bindLong(10, entity.getCreatedAtEpochMillis());
        statement.bindString(11, entity.getAttachmentRefs());
      }
    };
    this.__insertionAdapterOfLoanEntity = new EntityInsertionAdapter<LoanEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `loans` (`id`,`loanName`,`lenderName`,`principalAmount`,`annualInterestRate`,`tenureMonths`,`emiAmount`,`paymentFrequency`,`startDateEpochDay`,`createdAtEpochMillis`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final LoanEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getLoanName());
        statement.bindString(3, entity.getLenderName());
        statement.bindDouble(4, entity.getPrincipalAmount());
        statement.bindDouble(5, entity.getAnnualInterestRate());
        statement.bindLong(6, entity.getTenureMonths());
        statement.bindDouble(7, entity.getEmiAmount());
        statement.bindString(8, entity.getPaymentFrequency());
        statement.bindLong(9, entity.getStartDateEpochDay());
        statement.bindLong(10, entity.getCreatedAtEpochMillis());
      }
    };
    this.__insertionAdapterOfMoneyLendEntity = new EntityInsertionAdapter<MoneyLendEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `money_lend_entries` (`id`,`borrowerName`,`amount`,`interestRate`,`startDateEpochDay`,`dueDateEpochDay`,`notes`,`paidInstallmentsJson`,`isRepaid`,`createdAtEpochMillis`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MoneyLendEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getBorrowerName());
        statement.bindDouble(3, entity.getAmount());
        statement.bindDouble(4, entity.getInterestRate());
        statement.bindLong(5, entity.getStartDateEpochDay());
        if (entity.getDueDateEpochDay() == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, entity.getDueDateEpochDay());
        }
        statement.bindString(7, entity.getNotes());
        statement.bindString(8, entity.getPaidInstallmentsJson());
        final int _tmp = entity.isRepaid() ? 1 : 0;
        statement.bindLong(9, _tmp);
        statement.bindLong(10, entity.getCreatedAtEpochMillis());
      }
    };
    this.__insertionAdapterOfLoanPaymentEntity = new EntityInsertionAdapter<LoanPaymentEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `loan_payments` (`id`,`loanId`,`installmentNumber`,`dueDateEpochDay`,`amountDue`,`principalComponent`,`interestComponent`,`isPaid`,`paidOnEpochDay`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final LoanPaymentEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getLoanId());
        statement.bindLong(3, entity.getInstallmentNumber());
        statement.bindLong(4, entity.getDueDateEpochDay());
        statement.bindDouble(5, entity.getAmountDue());
        statement.bindDouble(6, entity.getPrincipalComponent());
        statement.bindDouble(7, entity.getInterestComponent());
        final int _tmp = entity.isPaid() ? 1 : 0;
        statement.bindLong(8, _tmp);
        if (entity.getPaidOnEpochDay() == null) {
          statement.bindNull(9);
        } else {
          statement.bindLong(9, entity.getPaidOnEpochDay());
        }
      }
    };
    this.__insertionAdapterOfPolicyEntity_1 = new EntityInsertionAdapter<PolicyEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `policies` (`id`,`category`,`policyHolderName`,`policyName`,`policyNumber`,`startDateEpochDay`,`expiryDateEpochDay`,`insurerName`,`previousInsurerName`,`premiumAmount`,`notes`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PolicyEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getCategory());
        statement.bindString(3, entity.getPolicyHolderName());
        statement.bindString(4, entity.getPolicyName());
        statement.bindString(5, entity.getPolicyNumber());
        statement.bindLong(6, entity.getStartDateEpochDay());
        statement.bindLong(7, entity.getExpiryDateEpochDay());
        statement.bindString(8, entity.getInsurerName());
        statement.bindString(9, entity.getPreviousInsurerName());
        statement.bindDouble(10, entity.getPremiumAmount());
        statement.bindString(11, entity.getNotes());
      }
    };
    this.__deletionAdapterOfPolicyEntity = new EntityDeletionOrUpdateAdapter<PolicyEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `policies` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PolicyEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfPolicyEntity = new EntityDeletionOrUpdateAdapter<PolicyEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `policies` SET `id` = ?,`category` = ?,`policyHolderName` = ?,`policyName` = ?,`policyNumber` = ?,`startDateEpochDay` = ?,`expiryDateEpochDay` = ?,`insurerName` = ?,`previousInsurerName` = ?,`premiumAmount` = ?,`notes` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PolicyEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getCategory());
        statement.bindString(3, entity.getPolicyHolderName());
        statement.bindString(4, entity.getPolicyName());
        statement.bindString(5, entity.getPolicyNumber());
        statement.bindLong(6, entity.getStartDateEpochDay());
        statement.bindLong(7, entity.getExpiryDateEpochDay());
        statement.bindString(8, entity.getInsurerName());
        statement.bindString(9, entity.getPreviousInsurerName());
        statement.bindDouble(10, entity.getPremiumAmount());
        statement.bindString(11, entity.getNotes());
        statement.bindLong(12, entity.getId());
      }
    };
    this.__updateAdapterOfPolicyHistoryEntity = new EntityDeletionOrUpdateAdapter<PolicyHistoryEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `policy_history` SET `id` = ?,`policyId` = ?,`policyHolderName` = ?,`insurerName` = ?,`policyNumber` = ?,`startDateEpochDay` = ?,`endDateEpochDay` = ?,`premiumAmount` = ?,`attachmentRefs` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PolicyHistoryEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getPolicyId());
        statement.bindString(3, entity.getPolicyHolderName());
        statement.bindString(4, entity.getInsurerName());
        statement.bindString(5, entity.getPolicyNumber());
        statement.bindLong(6, entity.getStartDateEpochDay());
        statement.bindLong(7, entity.getEndDateEpochDay());
        statement.bindDouble(8, entity.getPremiumAmount());
        statement.bindString(9, entity.getAttachmentRefs());
        statement.bindLong(10, entity.getId());
      }
    };
    this.__updateAdapterOfLoanEntity = new EntityDeletionOrUpdateAdapter<LoanEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `loans` SET `id` = ?,`loanName` = ?,`lenderName` = ?,`principalAmount` = ?,`annualInterestRate` = ?,`tenureMonths` = ?,`emiAmount` = ?,`paymentFrequency` = ?,`startDateEpochDay` = ?,`createdAtEpochMillis` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final LoanEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getLoanName());
        statement.bindString(3, entity.getLenderName());
        statement.bindDouble(4, entity.getPrincipalAmount());
        statement.bindDouble(5, entity.getAnnualInterestRate());
        statement.bindLong(6, entity.getTenureMonths());
        statement.bindDouble(7, entity.getEmiAmount());
        statement.bindString(8, entity.getPaymentFrequency());
        statement.bindLong(9, entity.getStartDateEpochDay());
        statement.bindLong(10, entity.getCreatedAtEpochMillis());
        statement.bindLong(11, entity.getId());
      }
    };
    this.__updateAdapterOfMoneyLendEntity = new EntityDeletionOrUpdateAdapter<MoneyLendEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `money_lend_entries` SET `id` = ?,`borrowerName` = ?,`amount` = ?,`interestRate` = ?,`startDateEpochDay` = ?,`dueDateEpochDay` = ?,`notes` = ?,`paidInstallmentsJson` = ?,`isRepaid` = ?,`createdAtEpochMillis` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MoneyLendEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getBorrowerName());
        statement.bindDouble(3, entity.getAmount());
        statement.bindDouble(4, entity.getInterestRate());
        statement.bindLong(5, entity.getStartDateEpochDay());
        if (entity.getDueDateEpochDay() == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, entity.getDueDateEpochDay());
        }
        statement.bindString(7, entity.getNotes());
        statement.bindString(8, entity.getPaidInstallmentsJson());
        final int _tmp = entity.isRepaid() ? 1 : 0;
        statement.bindLong(9, _tmp);
        statement.bindLong(10, entity.getCreatedAtEpochMillis());
        statement.bindLong(11, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteHistoryById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM policy_history WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfRenameAttachmentById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE attachments SET displayName = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteAttachmentById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM attachments WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfRenameFolder = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE category_folders SET name = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateFolderColor = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE category_folders SET colorHex = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteFolderById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM category_folders WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeletePolicyFolderTagsForPolicy = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM policy_folder_cross_ref WHERE policyId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfRemovePolicyFolderTag = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM policy_folder_cross_ref WHERE policyId = ? AND folderId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeletePolicyFolderTagsForFolder = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM policy_folder_cross_ref WHERE folderId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfRenameFolderAttachmentById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE folder_attachments SET displayName = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteFolderAttachmentById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM folder_attachments WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteFuturePolicyById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM future_policies WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteLoanById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM loans WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteMoneyLendById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM money_lend_entries WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteLoanPaymentsByLoanId = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM loan_payments WHERE loanId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfMarkLoanPaymentPaid = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE loan_payments SET isPaid = 1, paidOnEpochDay = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfAutoMarkPastDueLoanPayments = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "\n"
                + "        UPDATE loan_payments\n"
                + "        SET isPaid = 1, paidOnEpochDay = ?\n"
                + "        WHERE isPaid = 0 AND dueDateEpochDay < ?\n"
                + "        ";
        return _query;
      }
    };
    this.__preparedStmtOfClearFolderAttachments = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM folder_attachments";
        return _query;
      }
    };
    this.__preparedStmtOfClearPolicyFolderRefs = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM policy_folder_cross_ref";
        return _query;
      }
    };
    this.__preparedStmtOfClearFolders = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM category_folders";
        return _query;
      }
    };
    this.__preparedStmtOfClearFuturePolicies = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM future_policies";
        return _query;
      }
    };
    this.__preparedStmtOfClearPolicyCategoryDetails = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM policy_category_details";
        return _query;
      }
    };
    this.__preparedStmtOfClearAttachments = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM attachments";
        return _query;
      }
    };
    this.__preparedStmtOfClearHistory = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM policy_history";
        return _query;
      }
    };
    this.__preparedStmtOfClearPolicies = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM policies";
        return _query;
      }
    };
    this.__preparedStmtOfClearLoanPayments = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM loan_payments";
        return _query;
      }
    };
    this.__preparedStmtOfClearLoans = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM loans";
        return _query;
      }
    };
    this.__preparedStmtOfClearMoneyLends = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM money_lend_entries";
        return _query;
      }
    };
  }

  @Override
  public Object insertPolicy(final PolicyEntity policy,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfPolicyEntity.insertAndReturnId(policy);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertHistory(final PolicyHistoryEntity history,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfPolicyHistoryEntity.insert(history);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertAttachment(final AttachmentEntity attachment,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfAttachmentEntity.insert(attachment);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object upsertPolicyCategoryDetails(final PolicyCategoryDetailsEntity details,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfPolicyCategoryDetailsEntity.insert(details);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertFolder(final CategoryFolderEntity folder,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfCategoryFolderEntity.insertAndReturnId(folder);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertPolicyFolderTag(final PolicyFolderCrossRef crossRef,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfPolicyFolderCrossRef.insert(crossRef);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertFolderAttachment(final FolderAttachmentEntity attachment,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfFolderAttachmentEntity.insert(attachment);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertFuturePolicy(final FuturePolicyEntity futurePolicy,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfFuturePolicyEntity.insert(futurePolicy);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertLoan(final LoanEntity loan, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfLoanEntity.insertAndReturnId(loan);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertMoneyLend(final MoneyLendEntity entry,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfMoneyLendEntity.insertAndReturnId(entry);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertLoanPayments(final List<LoanPaymentEntity> items,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfLoanPaymentEntity.insert(items);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertPolicies(final List<PolicyEntity> items,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfPolicyEntity_1.insert(items);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertHistoryList(final List<PolicyHistoryEntity> items,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfPolicyHistoryEntity.insert(items);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertAttachments(final List<AttachmentEntity> items,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfAttachmentEntity.insert(items);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertFuturePolicies(final List<FuturePolicyEntity> items,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfFuturePolicyEntity.insert(items);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertPolicyCategoryDetails(final List<PolicyCategoryDetailsEntity> items,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfPolicyCategoryDetailsEntity.insert(items);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertFolders(final List<CategoryFolderEntity> items,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfCategoryFolderEntity.insert(items);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertPolicyFolderRefs(final List<PolicyFolderCrossRef> items,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfPolicyFolderCrossRef.insert(items);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertFolderAttachments(final List<FolderAttachmentEntity> items,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfFolderAttachmentEntity.insert(items);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertLoans(final List<LoanEntity> items,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfLoanEntity.insert(items);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertLoanPaymentsBulk(final List<LoanPaymentEntity> items,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfLoanPaymentEntity.insert(items);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertMoneyLends(final List<MoneyLendEntity> items,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfMoneyLendEntity.insert(items);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deletePolicy(final PolicyEntity policy,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfPolicyEntity.handle(policy);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updatePolicy(final PolicyEntity policy,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfPolicyEntity.handle(policy);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateHistory(final PolicyHistoryEntity history,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfPolicyHistoryEntity.handle(history);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateLoan(final LoanEntity loan, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfLoanEntity.handle(loan);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateMoneyLend(final MoneyLendEntity entry,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfMoneyLendEntity.handle(entry);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteHistoryById(final long historyId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteHistoryById.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, historyId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteHistoryById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object renameAttachmentById(final long attachmentId, final String displayName,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfRenameAttachmentById.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, displayName);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, attachmentId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfRenameAttachmentById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAttachmentById(final long attachmentId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAttachmentById.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, attachmentId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteAttachmentById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object renameFolder(final long folderId, final String name,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfRenameFolder.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, name);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, folderId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfRenameFolder.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateFolderColor(final long folderId, final String colorHex,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateFolderColor.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, colorHex);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, folderId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfUpdateFolderColor.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteFolderById(final long folderId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteFolderById.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, folderId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteFolderById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deletePolicyFolderTagsForPolicy(final long policyId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeletePolicyFolderTagsForPolicy.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, policyId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeletePolicyFolderTagsForPolicy.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object removePolicyFolderTag(final long policyId, final long folderId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfRemovePolicyFolderTag.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, policyId);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, folderId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfRemovePolicyFolderTag.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deletePolicyFolderTagsForFolder(final long folderId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeletePolicyFolderTagsForFolder.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, folderId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeletePolicyFolderTagsForFolder.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object renameFolderAttachmentById(final long attachmentId, final String displayName,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfRenameFolderAttachmentById.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, displayName);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, attachmentId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfRenameFolderAttachmentById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteFolderAttachmentById(final long attachmentId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteFolderAttachmentById.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, attachmentId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteFolderAttachmentById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteFuturePolicyById(final long futureId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteFuturePolicyById.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, futureId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteFuturePolicyById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteLoanById(final long loanId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteLoanById.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, loanId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteLoanById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteMoneyLendById(final long entryId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteMoneyLendById.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, entryId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteMoneyLendById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteLoanPaymentsByLoanId(final long loanId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteLoanPaymentsByLoanId.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, loanId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteLoanPaymentsByLoanId.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object markLoanPaymentPaid(final long paymentId, final long paidOnEpochDay,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMarkLoanPaymentPaid.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, paidOnEpochDay);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, paymentId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfMarkLoanPaymentPaid.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object autoMarkPastDueLoanPayments(final long todayEpochDay,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfAutoMarkPastDueLoanPayments.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, todayEpochDay);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, todayEpochDay);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfAutoMarkPastDueLoanPayments.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clearFolderAttachments(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearFolderAttachments.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClearFolderAttachments.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clearPolicyFolderRefs(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearPolicyFolderRefs.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClearPolicyFolderRefs.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clearFolders(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearFolders.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClearFolders.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clearFuturePolicies(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearFuturePolicies.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClearFuturePolicies.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clearPolicyCategoryDetails(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearPolicyCategoryDetails.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClearPolicyCategoryDetails.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clearAttachments(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearAttachments.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClearAttachments.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clearHistory(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearHistory.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClearHistory.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clearPolicies(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearPolicies.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClearPolicies.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clearLoanPayments(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearLoanPayments.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClearLoanPayments.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clearLoans(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearLoans.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClearLoans.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clearMoneyLends(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearMoneyLends.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClearMoneyLends.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<PolicyWithDetails>> observePoliciesWithDetails() {
    final String _sql = "SELECT * FROM policies ORDER BY expiryDateEpochDay ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, true, new String[] {"policy_category_details",
        "policy_history", "attachments", "future_policies",
        "policies"}, new Callable<List<PolicyWithDetails>>() {
      @Override
      @NonNull
      public List<PolicyWithDetails> call() throws Exception {
        __db.beginTransaction();
        try {
          final Cursor _cursor = DBUtil.query(__db, _statement, true, null);
          try {
            final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
            final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
            final int _cursorIndexOfPolicyHolderName = CursorUtil.getColumnIndexOrThrow(_cursor, "policyHolderName");
            final int _cursorIndexOfPolicyName = CursorUtil.getColumnIndexOrThrow(_cursor, "policyName");
            final int _cursorIndexOfPolicyNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "policyNumber");
            final int _cursorIndexOfStartDateEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "startDateEpochDay");
            final int _cursorIndexOfExpiryDateEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "expiryDateEpochDay");
            final int _cursorIndexOfInsurerName = CursorUtil.getColumnIndexOrThrow(_cursor, "insurerName");
            final int _cursorIndexOfPreviousInsurerName = CursorUtil.getColumnIndexOrThrow(_cursor, "previousInsurerName");
            final int _cursorIndexOfPremiumAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "premiumAmount");
            final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
            final LongSparseArray<PolicyCategoryDetailsEntity> _collectionCategoryDetails = new LongSparseArray<PolicyCategoryDetailsEntity>();
            final LongSparseArray<ArrayList<PolicyHistoryEntity>> _collectionHistory = new LongSparseArray<ArrayList<PolicyHistoryEntity>>();
            final LongSparseArray<ArrayList<AttachmentEntity>> _collectionAttachments = new LongSparseArray<ArrayList<AttachmentEntity>>();
            final LongSparseArray<ArrayList<FuturePolicyEntity>> _collectionFuturePolicies = new LongSparseArray<ArrayList<FuturePolicyEntity>>();
            while (_cursor.moveToNext()) {
              final long _tmpKey;
              _tmpKey = _cursor.getLong(_cursorIndexOfId);
              _collectionCategoryDetails.put(_tmpKey, null);
              final long _tmpKey_1;
              _tmpKey_1 = _cursor.getLong(_cursorIndexOfId);
              if (!_collectionHistory.containsKey(_tmpKey_1)) {
                _collectionHistory.put(_tmpKey_1, new ArrayList<PolicyHistoryEntity>());
              }
              final long _tmpKey_2;
              _tmpKey_2 = _cursor.getLong(_cursorIndexOfId);
              if (!_collectionAttachments.containsKey(_tmpKey_2)) {
                _collectionAttachments.put(_tmpKey_2, new ArrayList<AttachmentEntity>());
              }
              final long _tmpKey_3;
              _tmpKey_3 = _cursor.getLong(_cursorIndexOfId);
              if (!_collectionFuturePolicies.containsKey(_tmpKey_3)) {
                _collectionFuturePolicies.put(_tmpKey_3, new ArrayList<FuturePolicyEntity>());
              }
            }
            _cursor.moveToPosition(-1);
            __fetchRelationshippolicyCategoryDetailsAscomUdayPolicytrackerDataDbPolicyCategoryDetailsEntity(_collectionCategoryDetails);
            __fetchRelationshippolicyHistoryAscomUdayPolicytrackerDataDbPolicyHistoryEntity(_collectionHistory);
            __fetchRelationshipattachmentsAscomUdayPolicytrackerDataDbAttachmentEntity(_collectionAttachments);
            __fetchRelationshipfuturePoliciesAscomUdayPolicytrackerDataDbFuturePolicyEntity(_collectionFuturePolicies);
            final List<PolicyWithDetails> _result = new ArrayList<PolicyWithDetails>(_cursor.getCount());
            while (_cursor.moveToNext()) {
              final PolicyWithDetails _item;
              final PolicyEntity _tmpPolicy;
              final long _tmpId;
              _tmpId = _cursor.getLong(_cursorIndexOfId);
              final String _tmpCategory;
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
              final String _tmpPolicyHolderName;
              _tmpPolicyHolderName = _cursor.getString(_cursorIndexOfPolicyHolderName);
              final String _tmpPolicyName;
              _tmpPolicyName = _cursor.getString(_cursorIndexOfPolicyName);
              final String _tmpPolicyNumber;
              _tmpPolicyNumber = _cursor.getString(_cursorIndexOfPolicyNumber);
              final long _tmpStartDateEpochDay;
              _tmpStartDateEpochDay = _cursor.getLong(_cursorIndexOfStartDateEpochDay);
              final long _tmpExpiryDateEpochDay;
              _tmpExpiryDateEpochDay = _cursor.getLong(_cursorIndexOfExpiryDateEpochDay);
              final String _tmpInsurerName;
              _tmpInsurerName = _cursor.getString(_cursorIndexOfInsurerName);
              final String _tmpPreviousInsurerName;
              _tmpPreviousInsurerName = _cursor.getString(_cursorIndexOfPreviousInsurerName);
              final double _tmpPremiumAmount;
              _tmpPremiumAmount = _cursor.getDouble(_cursorIndexOfPremiumAmount);
              final String _tmpNotes;
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
              _tmpPolicy = new PolicyEntity(_tmpId,_tmpCategory,_tmpPolicyHolderName,_tmpPolicyName,_tmpPolicyNumber,_tmpStartDateEpochDay,_tmpExpiryDateEpochDay,_tmpInsurerName,_tmpPreviousInsurerName,_tmpPremiumAmount,_tmpNotes);
              final PolicyCategoryDetailsEntity _tmpCategoryDetails;
              final long _tmpKey_4;
              _tmpKey_4 = _cursor.getLong(_cursorIndexOfId);
              _tmpCategoryDetails = _collectionCategoryDetails.get(_tmpKey_4);
              final ArrayList<PolicyHistoryEntity> _tmpHistoryCollection;
              final long _tmpKey_5;
              _tmpKey_5 = _cursor.getLong(_cursorIndexOfId);
              _tmpHistoryCollection = _collectionHistory.get(_tmpKey_5);
              final ArrayList<AttachmentEntity> _tmpAttachmentsCollection;
              final long _tmpKey_6;
              _tmpKey_6 = _cursor.getLong(_cursorIndexOfId);
              _tmpAttachmentsCollection = _collectionAttachments.get(_tmpKey_6);
              final ArrayList<FuturePolicyEntity> _tmpFuturePoliciesCollection;
              final long _tmpKey_7;
              _tmpKey_7 = _cursor.getLong(_cursorIndexOfId);
              _tmpFuturePoliciesCollection = _collectionFuturePolicies.get(_tmpKey_7);
              _item = new PolicyWithDetails(_tmpPolicy,_tmpCategoryDetails,_tmpHistoryCollection,_tmpAttachmentsCollection,_tmpFuturePoliciesCollection);
              _result.add(_item);
            }
            __db.setTransactionSuccessful();
            return _result;
          } finally {
            _cursor.close();
          }
        } finally {
          __db.endTransaction();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<PolicyWithDetails> observePolicyWithDetails(final long policyId) {
    final String _sql = "SELECT * FROM policies WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, policyId);
    return CoroutinesRoom.createFlow(__db, true, new String[] {"policy_category_details",
        "policy_history", "attachments", "future_policies",
        "policies"}, new Callable<PolicyWithDetails>() {
      @Override
      @Nullable
      public PolicyWithDetails call() throws Exception {
        __db.beginTransaction();
        try {
          final Cursor _cursor = DBUtil.query(__db, _statement, true, null);
          try {
            final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
            final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
            final int _cursorIndexOfPolicyHolderName = CursorUtil.getColumnIndexOrThrow(_cursor, "policyHolderName");
            final int _cursorIndexOfPolicyName = CursorUtil.getColumnIndexOrThrow(_cursor, "policyName");
            final int _cursorIndexOfPolicyNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "policyNumber");
            final int _cursorIndexOfStartDateEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "startDateEpochDay");
            final int _cursorIndexOfExpiryDateEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "expiryDateEpochDay");
            final int _cursorIndexOfInsurerName = CursorUtil.getColumnIndexOrThrow(_cursor, "insurerName");
            final int _cursorIndexOfPreviousInsurerName = CursorUtil.getColumnIndexOrThrow(_cursor, "previousInsurerName");
            final int _cursorIndexOfPremiumAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "premiumAmount");
            final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
            final LongSparseArray<PolicyCategoryDetailsEntity> _collectionCategoryDetails = new LongSparseArray<PolicyCategoryDetailsEntity>();
            final LongSparseArray<ArrayList<PolicyHistoryEntity>> _collectionHistory = new LongSparseArray<ArrayList<PolicyHistoryEntity>>();
            final LongSparseArray<ArrayList<AttachmentEntity>> _collectionAttachments = new LongSparseArray<ArrayList<AttachmentEntity>>();
            final LongSparseArray<ArrayList<FuturePolicyEntity>> _collectionFuturePolicies = new LongSparseArray<ArrayList<FuturePolicyEntity>>();
            while (_cursor.moveToNext()) {
              final long _tmpKey;
              _tmpKey = _cursor.getLong(_cursorIndexOfId);
              _collectionCategoryDetails.put(_tmpKey, null);
              final long _tmpKey_1;
              _tmpKey_1 = _cursor.getLong(_cursorIndexOfId);
              if (!_collectionHistory.containsKey(_tmpKey_1)) {
                _collectionHistory.put(_tmpKey_1, new ArrayList<PolicyHistoryEntity>());
              }
              final long _tmpKey_2;
              _tmpKey_2 = _cursor.getLong(_cursorIndexOfId);
              if (!_collectionAttachments.containsKey(_tmpKey_2)) {
                _collectionAttachments.put(_tmpKey_2, new ArrayList<AttachmentEntity>());
              }
              final long _tmpKey_3;
              _tmpKey_3 = _cursor.getLong(_cursorIndexOfId);
              if (!_collectionFuturePolicies.containsKey(_tmpKey_3)) {
                _collectionFuturePolicies.put(_tmpKey_3, new ArrayList<FuturePolicyEntity>());
              }
            }
            _cursor.moveToPosition(-1);
            __fetchRelationshippolicyCategoryDetailsAscomUdayPolicytrackerDataDbPolicyCategoryDetailsEntity(_collectionCategoryDetails);
            __fetchRelationshippolicyHistoryAscomUdayPolicytrackerDataDbPolicyHistoryEntity(_collectionHistory);
            __fetchRelationshipattachmentsAscomUdayPolicytrackerDataDbAttachmentEntity(_collectionAttachments);
            __fetchRelationshipfuturePoliciesAscomUdayPolicytrackerDataDbFuturePolicyEntity(_collectionFuturePolicies);
            final PolicyWithDetails _result;
            if (_cursor.moveToFirst()) {
              final PolicyEntity _tmpPolicy;
              final long _tmpId;
              _tmpId = _cursor.getLong(_cursorIndexOfId);
              final String _tmpCategory;
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
              final String _tmpPolicyHolderName;
              _tmpPolicyHolderName = _cursor.getString(_cursorIndexOfPolicyHolderName);
              final String _tmpPolicyName;
              _tmpPolicyName = _cursor.getString(_cursorIndexOfPolicyName);
              final String _tmpPolicyNumber;
              _tmpPolicyNumber = _cursor.getString(_cursorIndexOfPolicyNumber);
              final long _tmpStartDateEpochDay;
              _tmpStartDateEpochDay = _cursor.getLong(_cursorIndexOfStartDateEpochDay);
              final long _tmpExpiryDateEpochDay;
              _tmpExpiryDateEpochDay = _cursor.getLong(_cursorIndexOfExpiryDateEpochDay);
              final String _tmpInsurerName;
              _tmpInsurerName = _cursor.getString(_cursorIndexOfInsurerName);
              final String _tmpPreviousInsurerName;
              _tmpPreviousInsurerName = _cursor.getString(_cursorIndexOfPreviousInsurerName);
              final double _tmpPremiumAmount;
              _tmpPremiumAmount = _cursor.getDouble(_cursorIndexOfPremiumAmount);
              final String _tmpNotes;
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
              _tmpPolicy = new PolicyEntity(_tmpId,_tmpCategory,_tmpPolicyHolderName,_tmpPolicyName,_tmpPolicyNumber,_tmpStartDateEpochDay,_tmpExpiryDateEpochDay,_tmpInsurerName,_tmpPreviousInsurerName,_tmpPremiumAmount,_tmpNotes);
              final PolicyCategoryDetailsEntity _tmpCategoryDetails;
              final long _tmpKey_4;
              _tmpKey_4 = _cursor.getLong(_cursorIndexOfId);
              _tmpCategoryDetails = _collectionCategoryDetails.get(_tmpKey_4);
              final ArrayList<PolicyHistoryEntity> _tmpHistoryCollection;
              final long _tmpKey_5;
              _tmpKey_5 = _cursor.getLong(_cursorIndexOfId);
              _tmpHistoryCollection = _collectionHistory.get(_tmpKey_5);
              final ArrayList<AttachmentEntity> _tmpAttachmentsCollection;
              final long _tmpKey_6;
              _tmpKey_6 = _cursor.getLong(_cursorIndexOfId);
              _tmpAttachmentsCollection = _collectionAttachments.get(_tmpKey_6);
              final ArrayList<FuturePolicyEntity> _tmpFuturePoliciesCollection;
              final long _tmpKey_7;
              _tmpKey_7 = _cursor.getLong(_cursorIndexOfId);
              _tmpFuturePoliciesCollection = _collectionFuturePolicies.get(_tmpKey_7);
              _result = new PolicyWithDetails(_tmpPolicy,_tmpCategoryDetails,_tmpHistoryCollection,_tmpAttachmentsCollection,_tmpFuturePoliciesCollection);
            } else {
              _result = null;
            }
            __db.setTransactionSuccessful();
            return _result;
          } finally {
            _cursor.close();
          }
        } finally {
          __db.endTransaction();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getPolicyWithDetailsById(final long policyId,
      final Continuation<? super PolicyWithDetails> $completion) {
    final String _sql = "SELECT * FROM policies WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, policyId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, true, _cancellationSignal, new Callable<PolicyWithDetails>() {
      @Override
      @Nullable
      public PolicyWithDetails call() throws Exception {
        __db.beginTransaction();
        try {
          final Cursor _cursor = DBUtil.query(__db, _statement, true, null);
          try {
            final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
            final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
            final int _cursorIndexOfPolicyHolderName = CursorUtil.getColumnIndexOrThrow(_cursor, "policyHolderName");
            final int _cursorIndexOfPolicyName = CursorUtil.getColumnIndexOrThrow(_cursor, "policyName");
            final int _cursorIndexOfPolicyNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "policyNumber");
            final int _cursorIndexOfStartDateEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "startDateEpochDay");
            final int _cursorIndexOfExpiryDateEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "expiryDateEpochDay");
            final int _cursorIndexOfInsurerName = CursorUtil.getColumnIndexOrThrow(_cursor, "insurerName");
            final int _cursorIndexOfPreviousInsurerName = CursorUtil.getColumnIndexOrThrow(_cursor, "previousInsurerName");
            final int _cursorIndexOfPremiumAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "premiumAmount");
            final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
            final LongSparseArray<PolicyCategoryDetailsEntity> _collectionCategoryDetails = new LongSparseArray<PolicyCategoryDetailsEntity>();
            final LongSparseArray<ArrayList<PolicyHistoryEntity>> _collectionHistory = new LongSparseArray<ArrayList<PolicyHistoryEntity>>();
            final LongSparseArray<ArrayList<AttachmentEntity>> _collectionAttachments = new LongSparseArray<ArrayList<AttachmentEntity>>();
            final LongSparseArray<ArrayList<FuturePolicyEntity>> _collectionFuturePolicies = new LongSparseArray<ArrayList<FuturePolicyEntity>>();
            while (_cursor.moveToNext()) {
              final long _tmpKey;
              _tmpKey = _cursor.getLong(_cursorIndexOfId);
              _collectionCategoryDetails.put(_tmpKey, null);
              final long _tmpKey_1;
              _tmpKey_1 = _cursor.getLong(_cursorIndexOfId);
              if (!_collectionHistory.containsKey(_tmpKey_1)) {
                _collectionHistory.put(_tmpKey_1, new ArrayList<PolicyHistoryEntity>());
              }
              final long _tmpKey_2;
              _tmpKey_2 = _cursor.getLong(_cursorIndexOfId);
              if (!_collectionAttachments.containsKey(_tmpKey_2)) {
                _collectionAttachments.put(_tmpKey_2, new ArrayList<AttachmentEntity>());
              }
              final long _tmpKey_3;
              _tmpKey_3 = _cursor.getLong(_cursorIndexOfId);
              if (!_collectionFuturePolicies.containsKey(_tmpKey_3)) {
                _collectionFuturePolicies.put(_tmpKey_3, new ArrayList<FuturePolicyEntity>());
              }
            }
            _cursor.moveToPosition(-1);
            __fetchRelationshippolicyCategoryDetailsAscomUdayPolicytrackerDataDbPolicyCategoryDetailsEntity(_collectionCategoryDetails);
            __fetchRelationshippolicyHistoryAscomUdayPolicytrackerDataDbPolicyHistoryEntity(_collectionHistory);
            __fetchRelationshipattachmentsAscomUdayPolicytrackerDataDbAttachmentEntity(_collectionAttachments);
            __fetchRelationshipfuturePoliciesAscomUdayPolicytrackerDataDbFuturePolicyEntity(_collectionFuturePolicies);
            final PolicyWithDetails _result;
            if (_cursor.moveToFirst()) {
              final PolicyEntity _tmpPolicy;
              final long _tmpId;
              _tmpId = _cursor.getLong(_cursorIndexOfId);
              final String _tmpCategory;
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
              final String _tmpPolicyHolderName;
              _tmpPolicyHolderName = _cursor.getString(_cursorIndexOfPolicyHolderName);
              final String _tmpPolicyName;
              _tmpPolicyName = _cursor.getString(_cursorIndexOfPolicyName);
              final String _tmpPolicyNumber;
              _tmpPolicyNumber = _cursor.getString(_cursorIndexOfPolicyNumber);
              final long _tmpStartDateEpochDay;
              _tmpStartDateEpochDay = _cursor.getLong(_cursorIndexOfStartDateEpochDay);
              final long _tmpExpiryDateEpochDay;
              _tmpExpiryDateEpochDay = _cursor.getLong(_cursorIndexOfExpiryDateEpochDay);
              final String _tmpInsurerName;
              _tmpInsurerName = _cursor.getString(_cursorIndexOfInsurerName);
              final String _tmpPreviousInsurerName;
              _tmpPreviousInsurerName = _cursor.getString(_cursorIndexOfPreviousInsurerName);
              final double _tmpPremiumAmount;
              _tmpPremiumAmount = _cursor.getDouble(_cursorIndexOfPremiumAmount);
              final String _tmpNotes;
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
              _tmpPolicy = new PolicyEntity(_tmpId,_tmpCategory,_tmpPolicyHolderName,_tmpPolicyName,_tmpPolicyNumber,_tmpStartDateEpochDay,_tmpExpiryDateEpochDay,_tmpInsurerName,_tmpPreviousInsurerName,_tmpPremiumAmount,_tmpNotes);
              final PolicyCategoryDetailsEntity _tmpCategoryDetails;
              final long _tmpKey_4;
              _tmpKey_4 = _cursor.getLong(_cursorIndexOfId);
              _tmpCategoryDetails = _collectionCategoryDetails.get(_tmpKey_4);
              final ArrayList<PolicyHistoryEntity> _tmpHistoryCollection;
              final long _tmpKey_5;
              _tmpKey_5 = _cursor.getLong(_cursorIndexOfId);
              _tmpHistoryCollection = _collectionHistory.get(_tmpKey_5);
              final ArrayList<AttachmentEntity> _tmpAttachmentsCollection;
              final long _tmpKey_6;
              _tmpKey_6 = _cursor.getLong(_cursorIndexOfId);
              _tmpAttachmentsCollection = _collectionAttachments.get(_tmpKey_6);
              final ArrayList<FuturePolicyEntity> _tmpFuturePoliciesCollection;
              final long _tmpKey_7;
              _tmpKey_7 = _cursor.getLong(_cursorIndexOfId);
              _tmpFuturePoliciesCollection = _collectionFuturePolicies.get(_tmpKey_7);
              _result = new PolicyWithDetails(_tmpPolicy,_tmpCategoryDetails,_tmpHistoryCollection,_tmpAttachmentsCollection,_tmpFuturePoliciesCollection);
            } else {
              _result = null;
            }
            __db.setTransactionSuccessful();
            return _result;
          } finally {
            _cursor.close();
            _statement.release();
          }
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object getHistoryById(final long historyId,
      final Continuation<? super PolicyHistoryEntity> $completion) {
    final String _sql = "SELECT * FROM policy_history WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, historyId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<PolicyHistoryEntity>() {
      @Override
      @Nullable
      public PolicyHistoryEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPolicyId = CursorUtil.getColumnIndexOrThrow(_cursor, "policyId");
          final int _cursorIndexOfPolicyHolderName = CursorUtil.getColumnIndexOrThrow(_cursor, "policyHolderName");
          final int _cursorIndexOfInsurerName = CursorUtil.getColumnIndexOrThrow(_cursor, "insurerName");
          final int _cursorIndexOfPolicyNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "policyNumber");
          final int _cursorIndexOfStartDateEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "startDateEpochDay");
          final int _cursorIndexOfEndDateEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "endDateEpochDay");
          final int _cursorIndexOfPremiumAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "premiumAmount");
          final int _cursorIndexOfAttachmentRefs = CursorUtil.getColumnIndexOrThrow(_cursor, "attachmentRefs");
          final PolicyHistoryEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpPolicyId;
            _tmpPolicyId = _cursor.getLong(_cursorIndexOfPolicyId);
            final String _tmpPolicyHolderName;
            _tmpPolicyHolderName = _cursor.getString(_cursorIndexOfPolicyHolderName);
            final String _tmpInsurerName;
            _tmpInsurerName = _cursor.getString(_cursorIndexOfInsurerName);
            final String _tmpPolicyNumber;
            _tmpPolicyNumber = _cursor.getString(_cursorIndexOfPolicyNumber);
            final long _tmpStartDateEpochDay;
            _tmpStartDateEpochDay = _cursor.getLong(_cursorIndexOfStartDateEpochDay);
            final long _tmpEndDateEpochDay;
            _tmpEndDateEpochDay = _cursor.getLong(_cursorIndexOfEndDateEpochDay);
            final double _tmpPremiumAmount;
            _tmpPremiumAmount = _cursor.getDouble(_cursorIndexOfPremiumAmount);
            final String _tmpAttachmentRefs;
            _tmpAttachmentRefs = _cursor.getString(_cursorIndexOfAttachmentRefs);
            _result = new PolicyHistoryEntity(_tmpId,_tmpPolicyId,_tmpPolicyHolderName,_tmpInsurerName,_tmpPolicyNumber,_tmpStartDateEpochDay,_tmpEndDateEpochDay,_tmpPremiumAmount,_tmpAttachmentRefs);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getPolicyCategoryDetails(final long policyId,
      final Continuation<? super PolicyCategoryDetailsEntity> $completion) {
    final String _sql = "SELECT * FROM policy_category_details WHERE policyId = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, policyId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<PolicyCategoryDetailsEntity>() {
      @Override
      @Nullable
      public PolicyCategoryDetailsEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfPolicyId = CursorUtil.getColumnIndexOrThrow(_cursor, "policyId");
          final int _cursorIndexOfPremiumFrequency = CursorUtil.getColumnIndexOrThrow(_cursor, "premiumFrequency");
          final int _cursorIndexOfPremiumDueDayOfMonth = CursorUtil.getColumnIndexOrThrow(_cursor, "premiumDueDayOfMonth");
          final int _cursorIndexOfCoverageAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "coverageAmount");
          final int _cursorIndexOfCoverageAmountUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "coverageAmountUnit");
          final int _cursorIndexOfPremiumPaymentStartEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "premiumPaymentStartEpochDay");
          final int _cursorIndexOfPremiumPaymentEndEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "premiumPaymentEndEpochDay");
          final int _cursorIndexOfPremiumPaymentTermYears = CursorUtil.getColumnIndexOrThrow(_cursor, "premiumPaymentTermYears");
          final int _cursorIndexOfPolicyValidityEndEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "policyValidityEndEpochDay");
          final int _cursorIndexOfPolicyTermYears = CursorUtil.getColumnIndexOrThrow(_cursor, "policyTermYears");
          final int _cursorIndexOfEntryAge = CursorUtil.getColumnIndexOrThrow(_cursor, "entryAge");
          final int _cursorIndexOfCoverageTillAge = CursorUtil.getColumnIndexOrThrow(_cursor, "coverageTillAge");
          final int _cursorIndexOfNomineeName = CursorUtil.getColumnIndexOrThrow(_cursor, "nomineeName");
          final int _cursorIndexOfNomineeRelationship = CursorUtil.getColumnIndexOrThrow(_cursor, "nomineeRelationship");
          final int _cursorIndexOfRiderAddons = CursorUtil.getColumnIndexOrThrow(_cursor, "riderAddons");
          final int _cursorIndexOfPaymentMode = CursorUtil.getColumnIndexOrThrow(_cursor, "paymentMode");
          final int _cursorIndexOfGracePeriodDays = CursorUtil.getColumnIndexOrThrow(_cursor, "gracePeriodDays");
          final int _cursorIndexOfTermPolicyStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "termPolicyStatus");
          final int _cursorIndexOfTotalPayments = CursorUtil.getColumnIndexOrThrow(_cursor, "totalPayments");
          final int _cursorIndexOfPaidPayments = CursorUtil.getColumnIndexOrThrow(_cursor, "paidPayments");
          final int _cursorIndexOfVehicleNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "vehicleNumber");
          final int _cursorIndexOfVehicleType = CursorUtil.getColumnIndexOrThrow(_cursor, "vehicleType");
          final int _cursorIndexOfMakeModelVariant = CursorUtil.getColumnIndexOrThrow(_cursor, "makeModelVariant");
          final int _cursorIndexOfFuelType = CursorUtil.getColumnIndexOrThrow(_cursor, "fuelType");
          final int _cursorIndexOfVehiclePolicyType = CursorUtil.getColumnIndexOrThrow(_cursor, "vehiclePolicyType");
          final int _cursorIndexOfVehicleAddons = CursorUtil.getColumnIndexOrThrow(_cursor, "vehicleAddons");
          final int _cursorIndexOfClaimHistory = CursorUtil.getColumnIndexOrThrow(_cursor, "claimHistory");
          final int _cursorIndexOfDocumentType = CursorUtil.getColumnIndexOrThrow(_cursor, "documentType");
          final int _cursorIndexOfIssuingRto = CursorUtil.getColumnIndexOrThrow(_cursor, "issuingRto");
          final int _cursorIndexOfStateName = CursorUtil.getColumnIndexOrThrow(_cursor, "stateName");
          final int _cursorIndexOfVehicleClass = CursorUtil.getColumnIndexOrThrow(_cursor, "vehicleClass");
          final int _cursorIndexOfOwnerName = CursorUtil.getColumnIndexOrThrow(_cursor, "ownerName");
          final int _cursorIndexOfLinkedVehicleNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "linkedVehicleNumber");
          final int _cursorIndexOfDateOfBirth = CursorUtil.getColumnIndexOrThrow(_cursor, "dateOfBirth");
          final int _cursorIndexOfCustomFieldValuesJson = CursorUtil.getColumnIndexOrThrow(_cursor, "customFieldValuesJson");
          final PolicyCategoryDetailsEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpPolicyId;
            _tmpPolicyId = _cursor.getLong(_cursorIndexOfPolicyId);
            final String _tmpPremiumFrequency;
            _tmpPremiumFrequency = _cursor.getString(_cursorIndexOfPremiumFrequency);
            final Integer _tmpPremiumDueDayOfMonth;
            if (_cursor.isNull(_cursorIndexOfPremiumDueDayOfMonth)) {
              _tmpPremiumDueDayOfMonth = null;
            } else {
              _tmpPremiumDueDayOfMonth = _cursor.getInt(_cursorIndexOfPremiumDueDayOfMonth);
            }
            final Double _tmpCoverageAmount;
            if (_cursor.isNull(_cursorIndexOfCoverageAmount)) {
              _tmpCoverageAmount = null;
            } else {
              _tmpCoverageAmount = _cursor.getDouble(_cursorIndexOfCoverageAmount);
            }
            final String _tmpCoverageAmountUnit;
            _tmpCoverageAmountUnit = _cursor.getString(_cursorIndexOfCoverageAmountUnit);
            final Long _tmpPremiumPaymentStartEpochDay;
            if (_cursor.isNull(_cursorIndexOfPremiumPaymentStartEpochDay)) {
              _tmpPremiumPaymentStartEpochDay = null;
            } else {
              _tmpPremiumPaymentStartEpochDay = _cursor.getLong(_cursorIndexOfPremiumPaymentStartEpochDay);
            }
            final Long _tmpPremiumPaymentEndEpochDay;
            if (_cursor.isNull(_cursorIndexOfPremiumPaymentEndEpochDay)) {
              _tmpPremiumPaymentEndEpochDay = null;
            } else {
              _tmpPremiumPaymentEndEpochDay = _cursor.getLong(_cursorIndexOfPremiumPaymentEndEpochDay);
            }
            final Integer _tmpPremiumPaymentTermYears;
            if (_cursor.isNull(_cursorIndexOfPremiumPaymentTermYears)) {
              _tmpPremiumPaymentTermYears = null;
            } else {
              _tmpPremiumPaymentTermYears = _cursor.getInt(_cursorIndexOfPremiumPaymentTermYears);
            }
            final Long _tmpPolicyValidityEndEpochDay;
            if (_cursor.isNull(_cursorIndexOfPolicyValidityEndEpochDay)) {
              _tmpPolicyValidityEndEpochDay = null;
            } else {
              _tmpPolicyValidityEndEpochDay = _cursor.getLong(_cursorIndexOfPolicyValidityEndEpochDay);
            }
            final Integer _tmpPolicyTermYears;
            if (_cursor.isNull(_cursorIndexOfPolicyTermYears)) {
              _tmpPolicyTermYears = null;
            } else {
              _tmpPolicyTermYears = _cursor.getInt(_cursorIndexOfPolicyTermYears);
            }
            final Integer _tmpEntryAge;
            if (_cursor.isNull(_cursorIndexOfEntryAge)) {
              _tmpEntryAge = null;
            } else {
              _tmpEntryAge = _cursor.getInt(_cursorIndexOfEntryAge);
            }
            final Integer _tmpCoverageTillAge;
            if (_cursor.isNull(_cursorIndexOfCoverageTillAge)) {
              _tmpCoverageTillAge = null;
            } else {
              _tmpCoverageTillAge = _cursor.getInt(_cursorIndexOfCoverageTillAge);
            }
            final String _tmpNomineeName;
            _tmpNomineeName = _cursor.getString(_cursorIndexOfNomineeName);
            final String _tmpNomineeRelationship;
            _tmpNomineeRelationship = _cursor.getString(_cursorIndexOfNomineeRelationship);
            final String _tmpRiderAddons;
            _tmpRiderAddons = _cursor.getString(_cursorIndexOfRiderAddons);
            final String _tmpPaymentMode;
            _tmpPaymentMode = _cursor.getString(_cursorIndexOfPaymentMode);
            final Integer _tmpGracePeriodDays;
            if (_cursor.isNull(_cursorIndexOfGracePeriodDays)) {
              _tmpGracePeriodDays = null;
            } else {
              _tmpGracePeriodDays = _cursor.getInt(_cursorIndexOfGracePeriodDays);
            }
            final String _tmpTermPolicyStatus;
            _tmpTermPolicyStatus = _cursor.getString(_cursorIndexOfTermPolicyStatus);
            final Integer _tmpTotalPayments;
            if (_cursor.isNull(_cursorIndexOfTotalPayments)) {
              _tmpTotalPayments = null;
            } else {
              _tmpTotalPayments = _cursor.getInt(_cursorIndexOfTotalPayments);
            }
            final Integer _tmpPaidPayments;
            if (_cursor.isNull(_cursorIndexOfPaidPayments)) {
              _tmpPaidPayments = null;
            } else {
              _tmpPaidPayments = _cursor.getInt(_cursorIndexOfPaidPayments);
            }
            final String _tmpVehicleNumber;
            _tmpVehicleNumber = _cursor.getString(_cursorIndexOfVehicleNumber);
            final String _tmpVehicleType;
            _tmpVehicleType = _cursor.getString(_cursorIndexOfVehicleType);
            final String _tmpMakeModelVariant;
            _tmpMakeModelVariant = _cursor.getString(_cursorIndexOfMakeModelVariant);
            final String _tmpFuelType;
            _tmpFuelType = _cursor.getString(_cursorIndexOfFuelType);
            final String _tmpVehiclePolicyType;
            _tmpVehiclePolicyType = _cursor.getString(_cursorIndexOfVehiclePolicyType);
            final String _tmpVehicleAddons;
            _tmpVehicleAddons = _cursor.getString(_cursorIndexOfVehicleAddons);
            final String _tmpClaimHistory;
            _tmpClaimHistory = _cursor.getString(_cursorIndexOfClaimHistory);
            final String _tmpDocumentType;
            _tmpDocumentType = _cursor.getString(_cursorIndexOfDocumentType);
            final String _tmpIssuingRto;
            _tmpIssuingRto = _cursor.getString(_cursorIndexOfIssuingRto);
            final String _tmpStateName;
            _tmpStateName = _cursor.getString(_cursorIndexOfStateName);
            final String _tmpVehicleClass;
            _tmpVehicleClass = _cursor.getString(_cursorIndexOfVehicleClass);
            final String _tmpOwnerName;
            _tmpOwnerName = _cursor.getString(_cursorIndexOfOwnerName);
            final String _tmpLinkedVehicleNumber;
            _tmpLinkedVehicleNumber = _cursor.getString(_cursorIndexOfLinkedVehicleNumber);
            final String _tmpDateOfBirth;
            _tmpDateOfBirth = _cursor.getString(_cursorIndexOfDateOfBirth);
            final String _tmpCustomFieldValuesJson;
            _tmpCustomFieldValuesJson = _cursor.getString(_cursorIndexOfCustomFieldValuesJson);
            _result = new PolicyCategoryDetailsEntity(_tmpPolicyId,_tmpPremiumFrequency,_tmpPremiumDueDayOfMonth,_tmpCoverageAmount,_tmpCoverageAmountUnit,_tmpPremiumPaymentStartEpochDay,_tmpPremiumPaymentEndEpochDay,_tmpPremiumPaymentTermYears,_tmpPolicyValidityEndEpochDay,_tmpPolicyTermYears,_tmpEntryAge,_tmpCoverageTillAge,_tmpNomineeName,_tmpNomineeRelationship,_tmpRiderAddons,_tmpPaymentMode,_tmpGracePeriodDays,_tmpTermPolicyStatus,_tmpTotalPayments,_tmpPaidPayments,_tmpVehicleNumber,_tmpVehicleType,_tmpMakeModelVariant,_tmpFuelType,_tmpVehiclePolicyType,_tmpVehicleAddons,_tmpClaimHistory,_tmpDocumentType,_tmpIssuingRto,_tmpStateName,_tmpVehicleClass,_tmpOwnerName,_tmpLinkedVehicleNumber,_tmpDateOfBirth,_tmpCustomFieldValuesJson);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<FolderWithDetails>> observeFoldersByCategory(final String category) {
    final String _sql = "SELECT * FROM category_folders WHERE category = ? ORDER BY startEpochDay DESC, name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, category);
    return CoroutinesRoom.createFlow(__db, true, new String[] {"policy_folder_cross_ref",
        "policies", "folder_attachments",
        "category_folders"}, new Callable<List<FolderWithDetails>>() {
      @Override
      @NonNull
      public List<FolderWithDetails> call() throws Exception {
        __db.beginTransaction();
        try {
          final Cursor _cursor = DBUtil.query(__db, _statement, true, null);
          try {
            final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
            final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
            final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
            final int _cursorIndexOfStartEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "startEpochDay");
            final int _cursorIndexOfEndEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "endEpochDay");
            final int _cursorIndexOfCreatedAtEpochMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAtEpochMillis");
            final int _cursorIndexOfColorHex = CursorUtil.getColumnIndexOrThrow(_cursor, "colorHex");
            final LongSparseArray<ArrayList<PolicyEntity>> _collectionPolicies = new LongSparseArray<ArrayList<PolicyEntity>>();
            final LongSparseArray<ArrayList<FolderAttachmentEntity>> _collectionAttachments = new LongSparseArray<ArrayList<FolderAttachmentEntity>>();
            while (_cursor.moveToNext()) {
              final long _tmpKey;
              _tmpKey = _cursor.getLong(_cursorIndexOfId);
              if (!_collectionPolicies.containsKey(_tmpKey)) {
                _collectionPolicies.put(_tmpKey, new ArrayList<PolicyEntity>());
              }
              final long _tmpKey_1;
              _tmpKey_1 = _cursor.getLong(_cursorIndexOfId);
              if (!_collectionAttachments.containsKey(_tmpKey_1)) {
                _collectionAttachments.put(_tmpKey_1, new ArrayList<FolderAttachmentEntity>());
              }
            }
            _cursor.moveToPosition(-1);
            __fetchRelationshippoliciesAscomUdayPolicytrackerDataDbPolicyEntity(_collectionPolicies);
            __fetchRelationshipfolderAttachmentsAscomUdayPolicytrackerDataDbFolderAttachmentEntity(_collectionAttachments);
            final List<FolderWithDetails> _result = new ArrayList<FolderWithDetails>(_cursor.getCount());
            while (_cursor.moveToNext()) {
              final FolderWithDetails _item;
              final CategoryFolderEntity _tmpFolder;
              final long _tmpId;
              _tmpId = _cursor.getLong(_cursorIndexOfId);
              final String _tmpCategory;
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
              final String _tmpName;
              _tmpName = _cursor.getString(_cursorIndexOfName);
              final long _tmpStartEpochDay;
              _tmpStartEpochDay = _cursor.getLong(_cursorIndexOfStartEpochDay);
              final long _tmpEndEpochDay;
              _tmpEndEpochDay = _cursor.getLong(_cursorIndexOfEndEpochDay);
              final long _tmpCreatedAtEpochMillis;
              _tmpCreatedAtEpochMillis = _cursor.getLong(_cursorIndexOfCreatedAtEpochMillis);
              final String _tmpColorHex;
              _tmpColorHex = _cursor.getString(_cursorIndexOfColorHex);
              _tmpFolder = new CategoryFolderEntity(_tmpId,_tmpCategory,_tmpName,_tmpStartEpochDay,_tmpEndEpochDay,_tmpCreatedAtEpochMillis,_tmpColorHex);
              final ArrayList<PolicyEntity> _tmpPoliciesCollection;
              final long _tmpKey_2;
              _tmpKey_2 = _cursor.getLong(_cursorIndexOfId);
              _tmpPoliciesCollection = _collectionPolicies.get(_tmpKey_2);
              final ArrayList<FolderAttachmentEntity> _tmpAttachmentsCollection;
              final long _tmpKey_3;
              _tmpKey_3 = _cursor.getLong(_cursorIndexOfId);
              _tmpAttachmentsCollection = _collectionAttachments.get(_tmpKey_3);
              _item = new FolderWithDetails(_tmpFolder,_tmpPoliciesCollection,_tmpAttachmentsCollection);
              _result.add(_item);
            }
            __db.setTransactionSuccessful();
            return _result;
          } finally {
            _cursor.close();
          }
        } finally {
          __db.endTransaction();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<FolderWithDetails> observeFolderById(final long folderId) {
    final String _sql = "SELECT * FROM category_folders WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, folderId);
    return CoroutinesRoom.createFlow(__db, true, new String[] {"policy_folder_cross_ref",
        "policies", "folder_attachments", "category_folders"}, new Callable<FolderWithDetails>() {
      @Override
      @Nullable
      public FolderWithDetails call() throws Exception {
        __db.beginTransaction();
        try {
          final Cursor _cursor = DBUtil.query(__db, _statement, true, null);
          try {
            final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
            final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
            final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
            final int _cursorIndexOfStartEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "startEpochDay");
            final int _cursorIndexOfEndEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "endEpochDay");
            final int _cursorIndexOfCreatedAtEpochMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAtEpochMillis");
            final int _cursorIndexOfColorHex = CursorUtil.getColumnIndexOrThrow(_cursor, "colorHex");
            final LongSparseArray<ArrayList<PolicyEntity>> _collectionPolicies = new LongSparseArray<ArrayList<PolicyEntity>>();
            final LongSparseArray<ArrayList<FolderAttachmentEntity>> _collectionAttachments = new LongSparseArray<ArrayList<FolderAttachmentEntity>>();
            while (_cursor.moveToNext()) {
              final long _tmpKey;
              _tmpKey = _cursor.getLong(_cursorIndexOfId);
              if (!_collectionPolicies.containsKey(_tmpKey)) {
                _collectionPolicies.put(_tmpKey, new ArrayList<PolicyEntity>());
              }
              final long _tmpKey_1;
              _tmpKey_1 = _cursor.getLong(_cursorIndexOfId);
              if (!_collectionAttachments.containsKey(_tmpKey_1)) {
                _collectionAttachments.put(_tmpKey_1, new ArrayList<FolderAttachmentEntity>());
              }
            }
            _cursor.moveToPosition(-1);
            __fetchRelationshippoliciesAscomUdayPolicytrackerDataDbPolicyEntity(_collectionPolicies);
            __fetchRelationshipfolderAttachmentsAscomUdayPolicytrackerDataDbFolderAttachmentEntity(_collectionAttachments);
            final FolderWithDetails _result;
            if (_cursor.moveToFirst()) {
              final CategoryFolderEntity _tmpFolder;
              final long _tmpId;
              _tmpId = _cursor.getLong(_cursorIndexOfId);
              final String _tmpCategory;
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
              final String _tmpName;
              _tmpName = _cursor.getString(_cursorIndexOfName);
              final long _tmpStartEpochDay;
              _tmpStartEpochDay = _cursor.getLong(_cursorIndexOfStartEpochDay);
              final long _tmpEndEpochDay;
              _tmpEndEpochDay = _cursor.getLong(_cursorIndexOfEndEpochDay);
              final long _tmpCreatedAtEpochMillis;
              _tmpCreatedAtEpochMillis = _cursor.getLong(_cursorIndexOfCreatedAtEpochMillis);
              final String _tmpColorHex;
              _tmpColorHex = _cursor.getString(_cursorIndexOfColorHex);
              _tmpFolder = new CategoryFolderEntity(_tmpId,_tmpCategory,_tmpName,_tmpStartEpochDay,_tmpEndEpochDay,_tmpCreatedAtEpochMillis,_tmpColorHex);
              final ArrayList<PolicyEntity> _tmpPoliciesCollection;
              final long _tmpKey_2;
              _tmpKey_2 = _cursor.getLong(_cursorIndexOfId);
              _tmpPoliciesCollection = _collectionPolicies.get(_tmpKey_2);
              final ArrayList<FolderAttachmentEntity> _tmpAttachmentsCollection;
              final long _tmpKey_3;
              _tmpKey_3 = _cursor.getLong(_cursorIndexOfId);
              _tmpAttachmentsCollection = _collectionAttachments.get(_tmpKey_3);
              _result = new FolderWithDetails(_tmpFolder,_tmpPoliciesCollection,_tmpAttachmentsCollection);
            } else {
              _result = null;
            }
            __db.setTransactionSuccessful();
            return _result;
          } finally {
            _cursor.close();
          }
        } finally {
          __db.endTransaction();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<PolicyEntity>> observePoliciesByCategory(final String category) {
    final String _sql = "SELECT * FROM policies WHERE category = ? ORDER BY expiryDateEpochDay DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, category);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"policies"}, new Callable<List<PolicyEntity>>() {
      @Override
      @NonNull
      public List<PolicyEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfPolicyHolderName = CursorUtil.getColumnIndexOrThrow(_cursor, "policyHolderName");
          final int _cursorIndexOfPolicyName = CursorUtil.getColumnIndexOrThrow(_cursor, "policyName");
          final int _cursorIndexOfPolicyNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "policyNumber");
          final int _cursorIndexOfStartDateEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "startDateEpochDay");
          final int _cursorIndexOfExpiryDateEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "expiryDateEpochDay");
          final int _cursorIndexOfInsurerName = CursorUtil.getColumnIndexOrThrow(_cursor, "insurerName");
          final int _cursorIndexOfPreviousInsurerName = CursorUtil.getColumnIndexOrThrow(_cursor, "previousInsurerName");
          final int _cursorIndexOfPremiumAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "premiumAmount");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final List<PolicyEntity> _result = new ArrayList<PolicyEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PolicyEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final String _tmpPolicyHolderName;
            _tmpPolicyHolderName = _cursor.getString(_cursorIndexOfPolicyHolderName);
            final String _tmpPolicyName;
            _tmpPolicyName = _cursor.getString(_cursorIndexOfPolicyName);
            final String _tmpPolicyNumber;
            _tmpPolicyNumber = _cursor.getString(_cursorIndexOfPolicyNumber);
            final long _tmpStartDateEpochDay;
            _tmpStartDateEpochDay = _cursor.getLong(_cursorIndexOfStartDateEpochDay);
            final long _tmpExpiryDateEpochDay;
            _tmpExpiryDateEpochDay = _cursor.getLong(_cursorIndexOfExpiryDateEpochDay);
            final String _tmpInsurerName;
            _tmpInsurerName = _cursor.getString(_cursorIndexOfInsurerName);
            final String _tmpPreviousInsurerName;
            _tmpPreviousInsurerName = _cursor.getString(_cursorIndexOfPreviousInsurerName);
            final double _tmpPremiumAmount;
            _tmpPremiumAmount = _cursor.getDouble(_cursorIndexOfPremiumAmount);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            _item = new PolicyEntity(_tmpId,_tmpCategory,_tmpPolicyHolderName,_tmpPolicyName,_tmpPolicyNumber,_tmpStartDateEpochDay,_tmpExpiryDateEpochDay,_tmpInsurerName,_tmpPreviousInsurerName,_tmpPremiumAmount,_tmpNotes);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getPolicyById(final long policyId,
      final Continuation<? super PolicyEntity> $completion) {
    final String _sql = "SELECT * FROM policies WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, policyId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<PolicyEntity>() {
      @Override
      @Nullable
      public PolicyEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfPolicyHolderName = CursorUtil.getColumnIndexOrThrow(_cursor, "policyHolderName");
          final int _cursorIndexOfPolicyName = CursorUtil.getColumnIndexOrThrow(_cursor, "policyName");
          final int _cursorIndexOfPolicyNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "policyNumber");
          final int _cursorIndexOfStartDateEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "startDateEpochDay");
          final int _cursorIndexOfExpiryDateEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "expiryDateEpochDay");
          final int _cursorIndexOfInsurerName = CursorUtil.getColumnIndexOrThrow(_cursor, "insurerName");
          final int _cursorIndexOfPreviousInsurerName = CursorUtil.getColumnIndexOrThrow(_cursor, "previousInsurerName");
          final int _cursorIndexOfPremiumAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "premiumAmount");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final PolicyEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final String _tmpPolicyHolderName;
            _tmpPolicyHolderName = _cursor.getString(_cursorIndexOfPolicyHolderName);
            final String _tmpPolicyName;
            _tmpPolicyName = _cursor.getString(_cursorIndexOfPolicyName);
            final String _tmpPolicyNumber;
            _tmpPolicyNumber = _cursor.getString(_cursorIndexOfPolicyNumber);
            final long _tmpStartDateEpochDay;
            _tmpStartDateEpochDay = _cursor.getLong(_cursorIndexOfStartDateEpochDay);
            final long _tmpExpiryDateEpochDay;
            _tmpExpiryDateEpochDay = _cursor.getLong(_cursorIndexOfExpiryDateEpochDay);
            final String _tmpInsurerName;
            _tmpInsurerName = _cursor.getString(_cursorIndexOfInsurerName);
            final String _tmpPreviousInsurerName;
            _tmpPreviousInsurerName = _cursor.getString(_cursorIndexOfPreviousInsurerName);
            final double _tmpPremiumAmount;
            _tmpPremiumAmount = _cursor.getDouble(_cursorIndexOfPremiumAmount);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            _result = new PolicyEntity(_tmpId,_tmpCategory,_tmpPolicyHolderName,_tmpPolicyName,_tmpPolicyNumber,_tmpStartDateEpochDay,_tmpExpiryDateEpochDay,_tmpInsurerName,_tmpPreviousInsurerName,_tmpPremiumAmount,_tmpNotes);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<CategoryFolderEntity>> observeAllFolders() {
    final String _sql = "SELECT * FROM category_folders ORDER BY createdAtEpochMillis DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"category_folders"}, new Callable<List<CategoryFolderEntity>>() {
      @Override
      @NonNull
      public List<CategoryFolderEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfStartEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "startEpochDay");
          final int _cursorIndexOfEndEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "endEpochDay");
          final int _cursorIndexOfCreatedAtEpochMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAtEpochMillis");
          final int _cursorIndexOfColorHex = CursorUtil.getColumnIndexOrThrow(_cursor, "colorHex");
          final List<CategoryFolderEntity> _result = new ArrayList<CategoryFolderEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CategoryFolderEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final long _tmpStartEpochDay;
            _tmpStartEpochDay = _cursor.getLong(_cursorIndexOfStartEpochDay);
            final long _tmpEndEpochDay;
            _tmpEndEpochDay = _cursor.getLong(_cursorIndexOfEndEpochDay);
            final long _tmpCreatedAtEpochMillis;
            _tmpCreatedAtEpochMillis = _cursor.getLong(_cursorIndexOfCreatedAtEpochMillis);
            final String _tmpColorHex;
            _tmpColorHex = _cursor.getString(_cursorIndexOfColorHex);
            _item = new CategoryFolderEntity(_tmpId,_tmpCategory,_tmpName,_tmpStartEpochDay,_tmpEndEpochDay,_tmpCreatedAtEpochMillis,_tmpColorHex);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<Long>> observePolicyFolderIds(final long policyId) {
    final String _sql = "SELECT folderId FROM policy_folder_cross_ref WHERE policyId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, policyId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"policy_folder_cross_ref"}, new Callable<List<Long>>() {
      @Override
      @NonNull
      public List<Long> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final List<Long> _result = new ArrayList<Long>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Long _item;
            _item = _cursor.getLong(0);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<FolderPolicyAttachmentRow>> observePolicyAttachmentsByFolder(
      final long folderId) {
    final String _sql = "\n"
            + "        SELECT \n"
            + "            a.id AS attachmentId,\n"
            + "            a.policyId AS policyId,\n"
            + "            a.uri AS uri,\n"
            + "            a.displayName AS displayName,\n"
            + "            a.mimeType AS mimeType,\n"
            + "            a.addedAtEpochMillis AS addedAtEpochMillis,\n"
            + "            p.policyName AS policyName,\n"
            + "            p.policyNumber AS policyNumber,\n"
            + "            p.startDateEpochDay AS policyStartEpochDay,\n"
            + "            p.expiryDateEpochDay AS policyExpiryEpochDay\n"
            + "        FROM attachments a\n"
            + "        INNER JOIN policy_folder_cross_ref r ON r.policyId = a.policyId\n"
            + "        INNER JOIN policies p ON p.id = a.policyId\n"
            + "        WHERE r.folderId = ?\n"
            + "        ORDER BY p.startDateEpochDay DESC, a.addedAtEpochMillis DESC\n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, folderId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"attachments",
        "policy_folder_cross_ref", "policies"}, new Callable<List<FolderPolicyAttachmentRow>>() {
      @Override
      @NonNull
      public List<FolderPolicyAttachmentRow> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfAttachmentId = 0;
          final int _cursorIndexOfPolicyId = 1;
          final int _cursorIndexOfUri = 2;
          final int _cursorIndexOfDisplayName = 3;
          final int _cursorIndexOfMimeType = 4;
          final int _cursorIndexOfAddedAtEpochMillis = 5;
          final int _cursorIndexOfPolicyName = 6;
          final int _cursorIndexOfPolicyNumber = 7;
          final int _cursorIndexOfPolicyStartEpochDay = 8;
          final int _cursorIndexOfPolicyExpiryEpochDay = 9;
          final List<FolderPolicyAttachmentRow> _result = new ArrayList<FolderPolicyAttachmentRow>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final FolderPolicyAttachmentRow _item;
            final long _tmpAttachmentId;
            _tmpAttachmentId = _cursor.getLong(_cursorIndexOfAttachmentId);
            final long _tmpPolicyId;
            _tmpPolicyId = _cursor.getLong(_cursorIndexOfPolicyId);
            final String _tmpUri;
            _tmpUri = _cursor.getString(_cursorIndexOfUri);
            final String _tmpDisplayName;
            _tmpDisplayName = _cursor.getString(_cursorIndexOfDisplayName);
            final String _tmpMimeType;
            _tmpMimeType = _cursor.getString(_cursorIndexOfMimeType);
            final long _tmpAddedAtEpochMillis;
            _tmpAddedAtEpochMillis = _cursor.getLong(_cursorIndexOfAddedAtEpochMillis);
            final String _tmpPolicyName;
            _tmpPolicyName = _cursor.getString(_cursorIndexOfPolicyName);
            final String _tmpPolicyNumber;
            _tmpPolicyNumber = _cursor.getString(_cursorIndexOfPolicyNumber);
            final long _tmpPolicyStartEpochDay;
            _tmpPolicyStartEpochDay = _cursor.getLong(_cursorIndexOfPolicyStartEpochDay);
            final long _tmpPolicyExpiryEpochDay;
            _tmpPolicyExpiryEpochDay = _cursor.getLong(_cursorIndexOfPolicyExpiryEpochDay);
            _item = new FolderPolicyAttachmentRow(_tmpAttachmentId,_tmpPolicyId,_tmpUri,_tmpDisplayName,_tmpMimeType,_tmpAddedAtEpochMillis,_tmpPolicyName,_tmpPolicyNumber,_tmpPolicyStartEpochDay,_tmpPolicyExpiryEpochDay);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<LoanEntity>> observeLoans() {
    final String _sql = "SELECT * FROM loans ORDER BY createdAtEpochMillis DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"loans"}, new Callable<List<LoanEntity>>() {
      @Override
      @NonNull
      public List<LoanEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfLoanName = CursorUtil.getColumnIndexOrThrow(_cursor, "loanName");
          final int _cursorIndexOfLenderName = CursorUtil.getColumnIndexOrThrow(_cursor, "lenderName");
          final int _cursorIndexOfPrincipalAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "principalAmount");
          final int _cursorIndexOfAnnualInterestRate = CursorUtil.getColumnIndexOrThrow(_cursor, "annualInterestRate");
          final int _cursorIndexOfTenureMonths = CursorUtil.getColumnIndexOrThrow(_cursor, "tenureMonths");
          final int _cursorIndexOfEmiAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "emiAmount");
          final int _cursorIndexOfPaymentFrequency = CursorUtil.getColumnIndexOrThrow(_cursor, "paymentFrequency");
          final int _cursorIndexOfStartDateEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "startDateEpochDay");
          final int _cursorIndexOfCreatedAtEpochMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAtEpochMillis");
          final List<LoanEntity> _result = new ArrayList<LoanEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final LoanEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpLoanName;
            _tmpLoanName = _cursor.getString(_cursorIndexOfLoanName);
            final String _tmpLenderName;
            _tmpLenderName = _cursor.getString(_cursorIndexOfLenderName);
            final double _tmpPrincipalAmount;
            _tmpPrincipalAmount = _cursor.getDouble(_cursorIndexOfPrincipalAmount);
            final double _tmpAnnualInterestRate;
            _tmpAnnualInterestRate = _cursor.getDouble(_cursorIndexOfAnnualInterestRate);
            final int _tmpTenureMonths;
            _tmpTenureMonths = _cursor.getInt(_cursorIndexOfTenureMonths);
            final double _tmpEmiAmount;
            _tmpEmiAmount = _cursor.getDouble(_cursorIndexOfEmiAmount);
            final String _tmpPaymentFrequency;
            _tmpPaymentFrequency = _cursor.getString(_cursorIndexOfPaymentFrequency);
            final long _tmpStartDateEpochDay;
            _tmpStartDateEpochDay = _cursor.getLong(_cursorIndexOfStartDateEpochDay);
            final long _tmpCreatedAtEpochMillis;
            _tmpCreatedAtEpochMillis = _cursor.getLong(_cursorIndexOfCreatedAtEpochMillis);
            _item = new LoanEntity(_tmpId,_tmpLoanName,_tmpLenderName,_tmpPrincipalAmount,_tmpAnnualInterestRate,_tmpTenureMonths,_tmpEmiAmount,_tmpPaymentFrequency,_tmpStartDateEpochDay,_tmpCreatedAtEpochMillis);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getLoanById(final long loanId, final Continuation<? super LoanEntity> $completion) {
    final String _sql = "SELECT * FROM loans WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, loanId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<LoanEntity>() {
      @Override
      @Nullable
      public LoanEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfLoanName = CursorUtil.getColumnIndexOrThrow(_cursor, "loanName");
          final int _cursorIndexOfLenderName = CursorUtil.getColumnIndexOrThrow(_cursor, "lenderName");
          final int _cursorIndexOfPrincipalAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "principalAmount");
          final int _cursorIndexOfAnnualInterestRate = CursorUtil.getColumnIndexOrThrow(_cursor, "annualInterestRate");
          final int _cursorIndexOfTenureMonths = CursorUtil.getColumnIndexOrThrow(_cursor, "tenureMonths");
          final int _cursorIndexOfEmiAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "emiAmount");
          final int _cursorIndexOfPaymentFrequency = CursorUtil.getColumnIndexOrThrow(_cursor, "paymentFrequency");
          final int _cursorIndexOfStartDateEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "startDateEpochDay");
          final int _cursorIndexOfCreatedAtEpochMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAtEpochMillis");
          final LoanEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpLoanName;
            _tmpLoanName = _cursor.getString(_cursorIndexOfLoanName);
            final String _tmpLenderName;
            _tmpLenderName = _cursor.getString(_cursorIndexOfLenderName);
            final double _tmpPrincipalAmount;
            _tmpPrincipalAmount = _cursor.getDouble(_cursorIndexOfPrincipalAmount);
            final double _tmpAnnualInterestRate;
            _tmpAnnualInterestRate = _cursor.getDouble(_cursorIndexOfAnnualInterestRate);
            final int _tmpTenureMonths;
            _tmpTenureMonths = _cursor.getInt(_cursorIndexOfTenureMonths);
            final double _tmpEmiAmount;
            _tmpEmiAmount = _cursor.getDouble(_cursorIndexOfEmiAmount);
            final String _tmpPaymentFrequency;
            _tmpPaymentFrequency = _cursor.getString(_cursorIndexOfPaymentFrequency);
            final long _tmpStartDateEpochDay;
            _tmpStartDateEpochDay = _cursor.getLong(_cursorIndexOfStartDateEpochDay);
            final long _tmpCreatedAtEpochMillis;
            _tmpCreatedAtEpochMillis = _cursor.getLong(_cursorIndexOfCreatedAtEpochMillis);
            _result = new LoanEntity(_tmpId,_tmpLoanName,_tmpLenderName,_tmpPrincipalAmount,_tmpAnnualInterestRate,_tmpTenureMonths,_tmpEmiAmount,_tmpPaymentFrequency,_tmpStartDateEpochDay,_tmpCreatedAtEpochMillis);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<LoanPaymentEntity>> observeLoanPaymentsByLoan(final long loanId) {
    final String _sql = "SELECT * FROM loan_payments WHERE loanId = ? ORDER BY installmentNumber ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, loanId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"loan_payments"}, new Callable<List<LoanPaymentEntity>>() {
      @Override
      @NonNull
      public List<LoanPaymentEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfLoanId = CursorUtil.getColumnIndexOrThrow(_cursor, "loanId");
          final int _cursorIndexOfInstallmentNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "installmentNumber");
          final int _cursorIndexOfDueDateEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "dueDateEpochDay");
          final int _cursorIndexOfAmountDue = CursorUtil.getColumnIndexOrThrow(_cursor, "amountDue");
          final int _cursorIndexOfPrincipalComponent = CursorUtil.getColumnIndexOrThrow(_cursor, "principalComponent");
          final int _cursorIndexOfInterestComponent = CursorUtil.getColumnIndexOrThrow(_cursor, "interestComponent");
          final int _cursorIndexOfIsPaid = CursorUtil.getColumnIndexOrThrow(_cursor, "isPaid");
          final int _cursorIndexOfPaidOnEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "paidOnEpochDay");
          final List<LoanPaymentEntity> _result = new ArrayList<LoanPaymentEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final LoanPaymentEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpLoanId;
            _tmpLoanId = _cursor.getLong(_cursorIndexOfLoanId);
            final int _tmpInstallmentNumber;
            _tmpInstallmentNumber = _cursor.getInt(_cursorIndexOfInstallmentNumber);
            final long _tmpDueDateEpochDay;
            _tmpDueDateEpochDay = _cursor.getLong(_cursorIndexOfDueDateEpochDay);
            final double _tmpAmountDue;
            _tmpAmountDue = _cursor.getDouble(_cursorIndexOfAmountDue);
            final double _tmpPrincipalComponent;
            _tmpPrincipalComponent = _cursor.getDouble(_cursorIndexOfPrincipalComponent);
            final double _tmpInterestComponent;
            _tmpInterestComponent = _cursor.getDouble(_cursorIndexOfInterestComponent);
            final boolean _tmpIsPaid;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsPaid);
            _tmpIsPaid = _tmp != 0;
            final Long _tmpPaidOnEpochDay;
            if (_cursor.isNull(_cursorIndexOfPaidOnEpochDay)) {
              _tmpPaidOnEpochDay = null;
            } else {
              _tmpPaidOnEpochDay = _cursor.getLong(_cursorIndexOfPaidOnEpochDay);
            }
            _item = new LoanPaymentEntity(_tmpId,_tmpLoanId,_tmpInstallmentNumber,_tmpDueDateEpochDay,_tmpAmountDue,_tmpPrincipalComponent,_tmpInterestComponent,_tmpIsPaid,_tmpPaidOnEpochDay);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<LoanPaymentEntity>> observeAllLoanPayments() {
    final String _sql = "SELECT * FROM loan_payments ORDER BY loanId ASC, installmentNumber ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"loan_payments"}, new Callable<List<LoanPaymentEntity>>() {
      @Override
      @NonNull
      public List<LoanPaymentEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfLoanId = CursorUtil.getColumnIndexOrThrow(_cursor, "loanId");
          final int _cursorIndexOfInstallmentNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "installmentNumber");
          final int _cursorIndexOfDueDateEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "dueDateEpochDay");
          final int _cursorIndexOfAmountDue = CursorUtil.getColumnIndexOrThrow(_cursor, "amountDue");
          final int _cursorIndexOfPrincipalComponent = CursorUtil.getColumnIndexOrThrow(_cursor, "principalComponent");
          final int _cursorIndexOfInterestComponent = CursorUtil.getColumnIndexOrThrow(_cursor, "interestComponent");
          final int _cursorIndexOfIsPaid = CursorUtil.getColumnIndexOrThrow(_cursor, "isPaid");
          final int _cursorIndexOfPaidOnEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "paidOnEpochDay");
          final List<LoanPaymentEntity> _result = new ArrayList<LoanPaymentEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final LoanPaymentEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpLoanId;
            _tmpLoanId = _cursor.getLong(_cursorIndexOfLoanId);
            final int _tmpInstallmentNumber;
            _tmpInstallmentNumber = _cursor.getInt(_cursorIndexOfInstallmentNumber);
            final long _tmpDueDateEpochDay;
            _tmpDueDateEpochDay = _cursor.getLong(_cursorIndexOfDueDateEpochDay);
            final double _tmpAmountDue;
            _tmpAmountDue = _cursor.getDouble(_cursorIndexOfAmountDue);
            final double _tmpPrincipalComponent;
            _tmpPrincipalComponent = _cursor.getDouble(_cursorIndexOfPrincipalComponent);
            final double _tmpInterestComponent;
            _tmpInterestComponent = _cursor.getDouble(_cursorIndexOfInterestComponent);
            final boolean _tmpIsPaid;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsPaid);
            _tmpIsPaid = _tmp != 0;
            final Long _tmpPaidOnEpochDay;
            if (_cursor.isNull(_cursorIndexOfPaidOnEpochDay)) {
              _tmpPaidOnEpochDay = null;
            } else {
              _tmpPaidOnEpochDay = _cursor.getLong(_cursorIndexOfPaidOnEpochDay);
            }
            _item = new LoanPaymentEntity(_tmpId,_tmpLoanId,_tmpInstallmentNumber,_tmpDueDateEpochDay,_tmpAmountDue,_tmpPrincipalComponent,_tmpInterestComponent,_tmpIsPaid,_tmpPaidOnEpochDay);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<MoneyLendEntity>> observeMoneyLends() {
    final String _sql = "SELECT * FROM money_lend_entries ORDER BY createdAtEpochMillis DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"money_lend_entries"}, new Callable<List<MoneyLendEntity>>() {
      @Override
      @NonNull
      public List<MoneyLendEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfBorrowerName = CursorUtil.getColumnIndexOrThrow(_cursor, "borrowerName");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfInterestRate = CursorUtil.getColumnIndexOrThrow(_cursor, "interestRate");
          final int _cursorIndexOfStartDateEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "startDateEpochDay");
          final int _cursorIndexOfDueDateEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "dueDateEpochDay");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfPaidInstallmentsJson = CursorUtil.getColumnIndexOrThrow(_cursor, "paidInstallmentsJson");
          final int _cursorIndexOfIsRepaid = CursorUtil.getColumnIndexOrThrow(_cursor, "isRepaid");
          final int _cursorIndexOfCreatedAtEpochMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAtEpochMillis");
          final List<MoneyLendEntity> _result = new ArrayList<MoneyLendEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MoneyLendEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpBorrowerName;
            _tmpBorrowerName = _cursor.getString(_cursorIndexOfBorrowerName);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final double _tmpInterestRate;
            _tmpInterestRate = _cursor.getDouble(_cursorIndexOfInterestRate);
            final long _tmpStartDateEpochDay;
            _tmpStartDateEpochDay = _cursor.getLong(_cursorIndexOfStartDateEpochDay);
            final Long _tmpDueDateEpochDay;
            if (_cursor.isNull(_cursorIndexOfDueDateEpochDay)) {
              _tmpDueDateEpochDay = null;
            } else {
              _tmpDueDateEpochDay = _cursor.getLong(_cursorIndexOfDueDateEpochDay);
            }
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final String _tmpPaidInstallmentsJson;
            _tmpPaidInstallmentsJson = _cursor.getString(_cursorIndexOfPaidInstallmentsJson);
            final boolean _tmpIsRepaid;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsRepaid);
            _tmpIsRepaid = _tmp != 0;
            final long _tmpCreatedAtEpochMillis;
            _tmpCreatedAtEpochMillis = _cursor.getLong(_cursorIndexOfCreatedAtEpochMillis);
            _item = new MoneyLendEntity(_tmpId,_tmpBorrowerName,_tmpAmount,_tmpInterestRate,_tmpStartDateEpochDay,_tmpDueDateEpochDay,_tmpNotes,_tmpPaidInstallmentsJson,_tmpIsRepaid,_tmpCreatedAtEpochMillis);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getMoneyLendById(final long entryId,
      final Continuation<? super MoneyLendEntity> $completion) {
    final String _sql = "SELECT * FROM money_lend_entries WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, entryId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<MoneyLendEntity>() {
      @Override
      @Nullable
      public MoneyLendEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfBorrowerName = CursorUtil.getColumnIndexOrThrow(_cursor, "borrowerName");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfInterestRate = CursorUtil.getColumnIndexOrThrow(_cursor, "interestRate");
          final int _cursorIndexOfStartDateEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "startDateEpochDay");
          final int _cursorIndexOfDueDateEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "dueDateEpochDay");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfPaidInstallmentsJson = CursorUtil.getColumnIndexOrThrow(_cursor, "paidInstallmentsJson");
          final int _cursorIndexOfIsRepaid = CursorUtil.getColumnIndexOrThrow(_cursor, "isRepaid");
          final int _cursorIndexOfCreatedAtEpochMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAtEpochMillis");
          final MoneyLendEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpBorrowerName;
            _tmpBorrowerName = _cursor.getString(_cursorIndexOfBorrowerName);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final double _tmpInterestRate;
            _tmpInterestRate = _cursor.getDouble(_cursorIndexOfInterestRate);
            final long _tmpStartDateEpochDay;
            _tmpStartDateEpochDay = _cursor.getLong(_cursorIndexOfStartDateEpochDay);
            final Long _tmpDueDateEpochDay;
            if (_cursor.isNull(_cursorIndexOfDueDateEpochDay)) {
              _tmpDueDateEpochDay = null;
            } else {
              _tmpDueDateEpochDay = _cursor.getLong(_cursorIndexOfDueDateEpochDay);
            }
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final String _tmpPaidInstallmentsJson;
            _tmpPaidInstallmentsJson = _cursor.getString(_cursorIndexOfPaidInstallmentsJson);
            final boolean _tmpIsRepaid;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsRepaid);
            _tmpIsRepaid = _tmp != 0;
            final long _tmpCreatedAtEpochMillis;
            _tmpCreatedAtEpochMillis = _cursor.getLong(_cursorIndexOfCreatedAtEpochMillis);
            _result = new MoneyLendEntity(_tmpId,_tmpBorrowerName,_tmpAmount,_tmpInterestRate,_tmpStartDateEpochDay,_tmpDueDateEpochDay,_tmpNotes,_tmpPaidInstallmentsJson,_tmpIsRepaid,_tmpCreatedAtEpochMillis);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getLoanPaymentsByLoan(final long loanId,
      final Continuation<? super List<LoanPaymentEntity>> $completion) {
    final String _sql = "SELECT * FROM loan_payments WHERE loanId = ? ORDER BY installmentNumber ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, loanId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<LoanPaymentEntity>>() {
      @Override
      @NonNull
      public List<LoanPaymentEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfLoanId = CursorUtil.getColumnIndexOrThrow(_cursor, "loanId");
          final int _cursorIndexOfInstallmentNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "installmentNumber");
          final int _cursorIndexOfDueDateEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "dueDateEpochDay");
          final int _cursorIndexOfAmountDue = CursorUtil.getColumnIndexOrThrow(_cursor, "amountDue");
          final int _cursorIndexOfPrincipalComponent = CursorUtil.getColumnIndexOrThrow(_cursor, "principalComponent");
          final int _cursorIndexOfInterestComponent = CursorUtil.getColumnIndexOrThrow(_cursor, "interestComponent");
          final int _cursorIndexOfIsPaid = CursorUtil.getColumnIndexOrThrow(_cursor, "isPaid");
          final int _cursorIndexOfPaidOnEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "paidOnEpochDay");
          final List<LoanPaymentEntity> _result = new ArrayList<LoanPaymentEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final LoanPaymentEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpLoanId;
            _tmpLoanId = _cursor.getLong(_cursorIndexOfLoanId);
            final int _tmpInstallmentNumber;
            _tmpInstallmentNumber = _cursor.getInt(_cursorIndexOfInstallmentNumber);
            final long _tmpDueDateEpochDay;
            _tmpDueDateEpochDay = _cursor.getLong(_cursorIndexOfDueDateEpochDay);
            final double _tmpAmountDue;
            _tmpAmountDue = _cursor.getDouble(_cursorIndexOfAmountDue);
            final double _tmpPrincipalComponent;
            _tmpPrincipalComponent = _cursor.getDouble(_cursorIndexOfPrincipalComponent);
            final double _tmpInterestComponent;
            _tmpInterestComponent = _cursor.getDouble(_cursorIndexOfInterestComponent);
            final boolean _tmpIsPaid;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsPaid);
            _tmpIsPaid = _tmp != 0;
            final Long _tmpPaidOnEpochDay;
            if (_cursor.isNull(_cursorIndexOfPaidOnEpochDay)) {
              _tmpPaidOnEpochDay = null;
            } else {
              _tmpPaidOnEpochDay = _cursor.getLong(_cursorIndexOfPaidOnEpochDay);
            }
            _item = new LoanPaymentEntity(_tmpId,_tmpLoanId,_tmpInstallmentNumber,_tmpDueDateEpochDay,_tmpAmountDue,_tmpPrincipalComponent,_tmpInterestComponent,_tmpIsPaid,_tmpPaidOnEpochDay);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getAllPolicies(final Continuation<? super List<PolicyEntity>> $completion) {
    final String _sql = "SELECT * FROM policies";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<PolicyEntity>>() {
      @Override
      @NonNull
      public List<PolicyEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfPolicyHolderName = CursorUtil.getColumnIndexOrThrow(_cursor, "policyHolderName");
          final int _cursorIndexOfPolicyName = CursorUtil.getColumnIndexOrThrow(_cursor, "policyName");
          final int _cursorIndexOfPolicyNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "policyNumber");
          final int _cursorIndexOfStartDateEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "startDateEpochDay");
          final int _cursorIndexOfExpiryDateEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "expiryDateEpochDay");
          final int _cursorIndexOfInsurerName = CursorUtil.getColumnIndexOrThrow(_cursor, "insurerName");
          final int _cursorIndexOfPreviousInsurerName = CursorUtil.getColumnIndexOrThrow(_cursor, "previousInsurerName");
          final int _cursorIndexOfPremiumAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "premiumAmount");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final List<PolicyEntity> _result = new ArrayList<PolicyEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PolicyEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final String _tmpPolicyHolderName;
            _tmpPolicyHolderName = _cursor.getString(_cursorIndexOfPolicyHolderName);
            final String _tmpPolicyName;
            _tmpPolicyName = _cursor.getString(_cursorIndexOfPolicyName);
            final String _tmpPolicyNumber;
            _tmpPolicyNumber = _cursor.getString(_cursorIndexOfPolicyNumber);
            final long _tmpStartDateEpochDay;
            _tmpStartDateEpochDay = _cursor.getLong(_cursorIndexOfStartDateEpochDay);
            final long _tmpExpiryDateEpochDay;
            _tmpExpiryDateEpochDay = _cursor.getLong(_cursorIndexOfExpiryDateEpochDay);
            final String _tmpInsurerName;
            _tmpInsurerName = _cursor.getString(_cursorIndexOfInsurerName);
            final String _tmpPreviousInsurerName;
            _tmpPreviousInsurerName = _cursor.getString(_cursorIndexOfPreviousInsurerName);
            final double _tmpPremiumAmount;
            _tmpPremiumAmount = _cursor.getDouble(_cursorIndexOfPremiumAmount);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            _item = new PolicyEntity(_tmpId,_tmpCategory,_tmpPolicyHolderName,_tmpPolicyName,_tmpPolicyNumber,_tmpStartDateEpochDay,_tmpExpiryDateEpochDay,_tmpInsurerName,_tmpPreviousInsurerName,_tmpPremiumAmount,_tmpNotes);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getAllHistory(final Continuation<? super List<PolicyHistoryEntity>> $completion) {
    final String _sql = "SELECT * FROM policy_history";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<PolicyHistoryEntity>>() {
      @Override
      @NonNull
      public List<PolicyHistoryEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPolicyId = CursorUtil.getColumnIndexOrThrow(_cursor, "policyId");
          final int _cursorIndexOfPolicyHolderName = CursorUtil.getColumnIndexOrThrow(_cursor, "policyHolderName");
          final int _cursorIndexOfInsurerName = CursorUtil.getColumnIndexOrThrow(_cursor, "insurerName");
          final int _cursorIndexOfPolicyNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "policyNumber");
          final int _cursorIndexOfStartDateEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "startDateEpochDay");
          final int _cursorIndexOfEndDateEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "endDateEpochDay");
          final int _cursorIndexOfPremiumAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "premiumAmount");
          final int _cursorIndexOfAttachmentRefs = CursorUtil.getColumnIndexOrThrow(_cursor, "attachmentRefs");
          final List<PolicyHistoryEntity> _result = new ArrayList<PolicyHistoryEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PolicyHistoryEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpPolicyId;
            _tmpPolicyId = _cursor.getLong(_cursorIndexOfPolicyId);
            final String _tmpPolicyHolderName;
            _tmpPolicyHolderName = _cursor.getString(_cursorIndexOfPolicyHolderName);
            final String _tmpInsurerName;
            _tmpInsurerName = _cursor.getString(_cursorIndexOfInsurerName);
            final String _tmpPolicyNumber;
            _tmpPolicyNumber = _cursor.getString(_cursorIndexOfPolicyNumber);
            final long _tmpStartDateEpochDay;
            _tmpStartDateEpochDay = _cursor.getLong(_cursorIndexOfStartDateEpochDay);
            final long _tmpEndDateEpochDay;
            _tmpEndDateEpochDay = _cursor.getLong(_cursorIndexOfEndDateEpochDay);
            final double _tmpPremiumAmount;
            _tmpPremiumAmount = _cursor.getDouble(_cursorIndexOfPremiumAmount);
            final String _tmpAttachmentRefs;
            _tmpAttachmentRefs = _cursor.getString(_cursorIndexOfAttachmentRefs);
            _item = new PolicyHistoryEntity(_tmpId,_tmpPolicyId,_tmpPolicyHolderName,_tmpInsurerName,_tmpPolicyNumber,_tmpStartDateEpochDay,_tmpEndDateEpochDay,_tmpPremiumAmount,_tmpAttachmentRefs);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getAllAttachments(final Continuation<? super List<AttachmentEntity>> $completion) {
    final String _sql = "SELECT * FROM attachments";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<AttachmentEntity>>() {
      @Override
      @NonNull
      public List<AttachmentEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPolicyId = CursorUtil.getColumnIndexOrThrow(_cursor, "policyId");
          final int _cursorIndexOfUri = CursorUtil.getColumnIndexOrThrow(_cursor, "uri");
          final int _cursorIndexOfDisplayName = CursorUtil.getColumnIndexOrThrow(_cursor, "displayName");
          final int _cursorIndexOfMimeType = CursorUtil.getColumnIndexOrThrow(_cursor, "mimeType");
          final int _cursorIndexOfAddedAtEpochMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "addedAtEpochMillis");
          final List<AttachmentEntity> _result = new ArrayList<AttachmentEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AttachmentEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpPolicyId;
            _tmpPolicyId = _cursor.getLong(_cursorIndexOfPolicyId);
            final String _tmpUri;
            _tmpUri = _cursor.getString(_cursorIndexOfUri);
            final String _tmpDisplayName;
            _tmpDisplayName = _cursor.getString(_cursorIndexOfDisplayName);
            final String _tmpMimeType;
            _tmpMimeType = _cursor.getString(_cursorIndexOfMimeType);
            final long _tmpAddedAtEpochMillis;
            _tmpAddedAtEpochMillis = _cursor.getLong(_cursorIndexOfAddedAtEpochMillis);
            _item = new AttachmentEntity(_tmpId,_tmpPolicyId,_tmpUri,_tmpDisplayName,_tmpMimeType,_tmpAddedAtEpochMillis);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getAllFuturePolicies(
      final Continuation<? super List<FuturePolicyEntity>> $completion) {
    final String _sql = "SELECT * FROM future_policies";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<FuturePolicyEntity>>() {
      @Override
      @NonNull
      public List<FuturePolicyEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPolicyId = CursorUtil.getColumnIndexOrThrow(_cursor, "policyId");
          final int _cursorIndexOfPolicyHolderName = CursorUtil.getColumnIndexOrThrow(_cursor, "policyHolderName");
          final int _cursorIndexOfPolicyName = CursorUtil.getColumnIndexOrThrow(_cursor, "policyName");
          final int _cursorIndexOfPolicyNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "policyNumber");
          final int _cursorIndexOfStartDateEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "startDateEpochDay");
          final int _cursorIndexOfExpiryDateEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "expiryDateEpochDay");
          final int _cursorIndexOfInsurerName = CursorUtil.getColumnIndexOrThrow(_cursor, "insurerName");
          final int _cursorIndexOfPremiumAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "premiumAmount");
          final int _cursorIndexOfCreatedAtEpochMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAtEpochMillis");
          final int _cursorIndexOfAttachmentRefs = CursorUtil.getColumnIndexOrThrow(_cursor, "attachmentRefs");
          final List<FuturePolicyEntity> _result = new ArrayList<FuturePolicyEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final FuturePolicyEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpPolicyId;
            _tmpPolicyId = _cursor.getLong(_cursorIndexOfPolicyId);
            final String _tmpPolicyHolderName;
            _tmpPolicyHolderName = _cursor.getString(_cursorIndexOfPolicyHolderName);
            final String _tmpPolicyName;
            _tmpPolicyName = _cursor.getString(_cursorIndexOfPolicyName);
            final String _tmpPolicyNumber;
            _tmpPolicyNumber = _cursor.getString(_cursorIndexOfPolicyNumber);
            final long _tmpStartDateEpochDay;
            _tmpStartDateEpochDay = _cursor.getLong(_cursorIndexOfStartDateEpochDay);
            final long _tmpExpiryDateEpochDay;
            _tmpExpiryDateEpochDay = _cursor.getLong(_cursorIndexOfExpiryDateEpochDay);
            final String _tmpInsurerName;
            _tmpInsurerName = _cursor.getString(_cursorIndexOfInsurerName);
            final double _tmpPremiumAmount;
            _tmpPremiumAmount = _cursor.getDouble(_cursorIndexOfPremiumAmount);
            final long _tmpCreatedAtEpochMillis;
            _tmpCreatedAtEpochMillis = _cursor.getLong(_cursorIndexOfCreatedAtEpochMillis);
            final String _tmpAttachmentRefs;
            _tmpAttachmentRefs = _cursor.getString(_cursorIndexOfAttachmentRefs);
            _item = new FuturePolicyEntity(_tmpId,_tmpPolicyId,_tmpPolicyHolderName,_tmpPolicyName,_tmpPolicyNumber,_tmpStartDateEpochDay,_tmpExpiryDateEpochDay,_tmpInsurerName,_tmpPremiumAmount,_tmpCreatedAtEpochMillis,_tmpAttachmentRefs);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getAllPolicyCategoryDetails(
      final Continuation<? super List<PolicyCategoryDetailsEntity>> $completion) {
    final String _sql = "SELECT * FROM policy_category_details";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<PolicyCategoryDetailsEntity>>() {
      @Override
      @NonNull
      public List<PolicyCategoryDetailsEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfPolicyId = CursorUtil.getColumnIndexOrThrow(_cursor, "policyId");
          final int _cursorIndexOfPremiumFrequency = CursorUtil.getColumnIndexOrThrow(_cursor, "premiumFrequency");
          final int _cursorIndexOfPremiumDueDayOfMonth = CursorUtil.getColumnIndexOrThrow(_cursor, "premiumDueDayOfMonth");
          final int _cursorIndexOfCoverageAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "coverageAmount");
          final int _cursorIndexOfCoverageAmountUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "coverageAmountUnit");
          final int _cursorIndexOfPremiumPaymentStartEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "premiumPaymentStartEpochDay");
          final int _cursorIndexOfPremiumPaymentEndEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "premiumPaymentEndEpochDay");
          final int _cursorIndexOfPremiumPaymentTermYears = CursorUtil.getColumnIndexOrThrow(_cursor, "premiumPaymentTermYears");
          final int _cursorIndexOfPolicyValidityEndEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "policyValidityEndEpochDay");
          final int _cursorIndexOfPolicyTermYears = CursorUtil.getColumnIndexOrThrow(_cursor, "policyTermYears");
          final int _cursorIndexOfEntryAge = CursorUtil.getColumnIndexOrThrow(_cursor, "entryAge");
          final int _cursorIndexOfCoverageTillAge = CursorUtil.getColumnIndexOrThrow(_cursor, "coverageTillAge");
          final int _cursorIndexOfNomineeName = CursorUtil.getColumnIndexOrThrow(_cursor, "nomineeName");
          final int _cursorIndexOfNomineeRelationship = CursorUtil.getColumnIndexOrThrow(_cursor, "nomineeRelationship");
          final int _cursorIndexOfRiderAddons = CursorUtil.getColumnIndexOrThrow(_cursor, "riderAddons");
          final int _cursorIndexOfPaymentMode = CursorUtil.getColumnIndexOrThrow(_cursor, "paymentMode");
          final int _cursorIndexOfGracePeriodDays = CursorUtil.getColumnIndexOrThrow(_cursor, "gracePeriodDays");
          final int _cursorIndexOfTermPolicyStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "termPolicyStatus");
          final int _cursorIndexOfTotalPayments = CursorUtil.getColumnIndexOrThrow(_cursor, "totalPayments");
          final int _cursorIndexOfPaidPayments = CursorUtil.getColumnIndexOrThrow(_cursor, "paidPayments");
          final int _cursorIndexOfVehicleNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "vehicleNumber");
          final int _cursorIndexOfVehicleType = CursorUtil.getColumnIndexOrThrow(_cursor, "vehicleType");
          final int _cursorIndexOfMakeModelVariant = CursorUtil.getColumnIndexOrThrow(_cursor, "makeModelVariant");
          final int _cursorIndexOfFuelType = CursorUtil.getColumnIndexOrThrow(_cursor, "fuelType");
          final int _cursorIndexOfVehiclePolicyType = CursorUtil.getColumnIndexOrThrow(_cursor, "vehiclePolicyType");
          final int _cursorIndexOfVehicleAddons = CursorUtil.getColumnIndexOrThrow(_cursor, "vehicleAddons");
          final int _cursorIndexOfClaimHistory = CursorUtil.getColumnIndexOrThrow(_cursor, "claimHistory");
          final int _cursorIndexOfDocumentType = CursorUtil.getColumnIndexOrThrow(_cursor, "documentType");
          final int _cursorIndexOfIssuingRto = CursorUtil.getColumnIndexOrThrow(_cursor, "issuingRto");
          final int _cursorIndexOfStateName = CursorUtil.getColumnIndexOrThrow(_cursor, "stateName");
          final int _cursorIndexOfVehicleClass = CursorUtil.getColumnIndexOrThrow(_cursor, "vehicleClass");
          final int _cursorIndexOfOwnerName = CursorUtil.getColumnIndexOrThrow(_cursor, "ownerName");
          final int _cursorIndexOfLinkedVehicleNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "linkedVehicleNumber");
          final int _cursorIndexOfDateOfBirth = CursorUtil.getColumnIndexOrThrow(_cursor, "dateOfBirth");
          final int _cursorIndexOfCustomFieldValuesJson = CursorUtil.getColumnIndexOrThrow(_cursor, "customFieldValuesJson");
          final List<PolicyCategoryDetailsEntity> _result = new ArrayList<PolicyCategoryDetailsEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PolicyCategoryDetailsEntity _item;
            final long _tmpPolicyId;
            _tmpPolicyId = _cursor.getLong(_cursorIndexOfPolicyId);
            final String _tmpPremiumFrequency;
            _tmpPremiumFrequency = _cursor.getString(_cursorIndexOfPremiumFrequency);
            final Integer _tmpPremiumDueDayOfMonth;
            if (_cursor.isNull(_cursorIndexOfPremiumDueDayOfMonth)) {
              _tmpPremiumDueDayOfMonth = null;
            } else {
              _tmpPremiumDueDayOfMonth = _cursor.getInt(_cursorIndexOfPremiumDueDayOfMonth);
            }
            final Double _tmpCoverageAmount;
            if (_cursor.isNull(_cursorIndexOfCoverageAmount)) {
              _tmpCoverageAmount = null;
            } else {
              _tmpCoverageAmount = _cursor.getDouble(_cursorIndexOfCoverageAmount);
            }
            final String _tmpCoverageAmountUnit;
            _tmpCoverageAmountUnit = _cursor.getString(_cursorIndexOfCoverageAmountUnit);
            final Long _tmpPremiumPaymentStartEpochDay;
            if (_cursor.isNull(_cursorIndexOfPremiumPaymentStartEpochDay)) {
              _tmpPremiumPaymentStartEpochDay = null;
            } else {
              _tmpPremiumPaymentStartEpochDay = _cursor.getLong(_cursorIndexOfPremiumPaymentStartEpochDay);
            }
            final Long _tmpPremiumPaymentEndEpochDay;
            if (_cursor.isNull(_cursorIndexOfPremiumPaymentEndEpochDay)) {
              _tmpPremiumPaymentEndEpochDay = null;
            } else {
              _tmpPremiumPaymentEndEpochDay = _cursor.getLong(_cursorIndexOfPremiumPaymentEndEpochDay);
            }
            final Integer _tmpPremiumPaymentTermYears;
            if (_cursor.isNull(_cursorIndexOfPremiumPaymentTermYears)) {
              _tmpPremiumPaymentTermYears = null;
            } else {
              _tmpPremiumPaymentTermYears = _cursor.getInt(_cursorIndexOfPremiumPaymentTermYears);
            }
            final Long _tmpPolicyValidityEndEpochDay;
            if (_cursor.isNull(_cursorIndexOfPolicyValidityEndEpochDay)) {
              _tmpPolicyValidityEndEpochDay = null;
            } else {
              _tmpPolicyValidityEndEpochDay = _cursor.getLong(_cursorIndexOfPolicyValidityEndEpochDay);
            }
            final Integer _tmpPolicyTermYears;
            if (_cursor.isNull(_cursorIndexOfPolicyTermYears)) {
              _tmpPolicyTermYears = null;
            } else {
              _tmpPolicyTermYears = _cursor.getInt(_cursorIndexOfPolicyTermYears);
            }
            final Integer _tmpEntryAge;
            if (_cursor.isNull(_cursorIndexOfEntryAge)) {
              _tmpEntryAge = null;
            } else {
              _tmpEntryAge = _cursor.getInt(_cursorIndexOfEntryAge);
            }
            final Integer _tmpCoverageTillAge;
            if (_cursor.isNull(_cursorIndexOfCoverageTillAge)) {
              _tmpCoverageTillAge = null;
            } else {
              _tmpCoverageTillAge = _cursor.getInt(_cursorIndexOfCoverageTillAge);
            }
            final String _tmpNomineeName;
            _tmpNomineeName = _cursor.getString(_cursorIndexOfNomineeName);
            final String _tmpNomineeRelationship;
            _tmpNomineeRelationship = _cursor.getString(_cursorIndexOfNomineeRelationship);
            final String _tmpRiderAddons;
            _tmpRiderAddons = _cursor.getString(_cursorIndexOfRiderAddons);
            final String _tmpPaymentMode;
            _tmpPaymentMode = _cursor.getString(_cursorIndexOfPaymentMode);
            final Integer _tmpGracePeriodDays;
            if (_cursor.isNull(_cursorIndexOfGracePeriodDays)) {
              _tmpGracePeriodDays = null;
            } else {
              _tmpGracePeriodDays = _cursor.getInt(_cursorIndexOfGracePeriodDays);
            }
            final String _tmpTermPolicyStatus;
            _tmpTermPolicyStatus = _cursor.getString(_cursorIndexOfTermPolicyStatus);
            final Integer _tmpTotalPayments;
            if (_cursor.isNull(_cursorIndexOfTotalPayments)) {
              _tmpTotalPayments = null;
            } else {
              _tmpTotalPayments = _cursor.getInt(_cursorIndexOfTotalPayments);
            }
            final Integer _tmpPaidPayments;
            if (_cursor.isNull(_cursorIndexOfPaidPayments)) {
              _tmpPaidPayments = null;
            } else {
              _tmpPaidPayments = _cursor.getInt(_cursorIndexOfPaidPayments);
            }
            final String _tmpVehicleNumber;
            _tmpVehicleNumber = _cursor.getString(_cursorIndexOfVehicleNumber);
            final String _tmpVehicleType;
            _tmpVehicleType = _cursor.getString(_cursorIndexOfVehicleType);
            final String _tmpMakeModelVariant;
            _tmpMakeModelVariant = _cursor.getString(_cursorIndexOfMakeModelVariant);
            final String _tmpFuelType;
            _tmpFuelType = _cursor.getString(_cursorIndexOfFuelType);
            final String _tmpVehiclePolicyType;
            _tmpVehiclePolicyType = _cursor.getString(_cursorIndexOfVehiclePolicyType);
            final String _tmpVehicleAddons;
            _tmpVehicleAddons = _cursor.getString(_cursorIndexOfVehicleAddons);
            final String _tmpClaimHistory;
            _tmpClaimHistory = _cursor.getString(_cursorIndexOfClaimHistory);
            final String _tmpDocumentType;
            _tmpDocumentType = _cursor.getString(_cursorIndexOfDocumentType);
            final String _tmpIssuingRto;
            _tmpIssuingRto = _cursor.getString(_cursorIndexOfIssuingRto);
            final String _tmpStateName;
            _tmpStateName = _cursor.getString(_cursorIndexOfStateName);
            final String _tmpVehicleClass;
            _tmpVehicleClass = _cursor.getString(_cursorIndexOfVehicleClass);
            final String _tmpOwnerName;
            _tmpOwnerName = _cursor.getString(_cursorIndexOfOwnerName);
            final String _tmpLinkedVehicleNumber;
            _tmpLinkedVehicleNumber = _cursor.getString(_cursorIndexOfLinkedVehicleNumber);
            final String _tmpDateOfBirth;
            _tmpDateOfBirth = _cursor.getString(_cursorIndexOfDateOfBirth);
            final String _tmpCustomFieldValuesJson;
            _tmpCustomFieldValuesJson = _cursor.getString(_cursorIndexOfCustomFieldValuesJson);
            _item = new PolicyCategoryDetailsEntity(_tmpPolicyId,_tmpPremiumFrequency,_tmpPremiumDueDayOfMonth,_tmpCoverageAmount,_tmpCoverageAmountUnit,_tmpPremiumPaymentStartEpochDay,_tmpPremiumPaymentEndEpochDay,_tmpPremiumPaymentTermYears,_tmpPolicyValidityEndEpochDay,_tmpPolicyTermYears,_tmpEntryAge,_tmpCoverageTillAge,_tmpNomineeName,_tmpNomineeRelationship,_tmpRiderAddons,_tmpPaymentMode,_tmpGracePeriodDays,_tmpTermPolicyStatus,_tmpTotalPayments,_tmpPaidPayments,_tmpVehicleNumber,_tmpVehicleType,_tmpMakeModelVariant,_tmpFuelType,_tmpVehiclePolicyType,_tmpVehicleAddons,_tmpClaimHistory,_tmpDocumentType,_tmpIssuingRto,_tmpStateName,_tmpVehicleClass,_tmpOwnerName,_tmpLinkedVehicleNumber,_tmpDateOfBirth,_tmpCustomFieldValuesJson);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getAllFolders(final Continuation<? super List<CategoryFolderEntity>> $completion) {
    final String _sql = "SELECT * FROM category_folders";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<CategoryFolderEntity>>() {
      @Override
      @NonNull
      public List<CategoryFolderEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfStartEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "startEpochDay");
          final int _cursorIndexOfEndEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "endEpochDay");
          final int _cursorIndexOfCreatedAtEpochMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAtEpochMillis");
          final int _cursorIndexOfColorHex = CursorUtil.getColumnIndexOrThrow(_cursor, "colorHex");
          final List<CategoryFolderEntity> _result = new ArrayList<CategoryFolderEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CategoryFolderEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final long _tmpStartEpochDay;
            _tmpStartEpochDay = _cursor.getLong(_cursorIndexOfStartEpochDay);
            final long _tmpEndEpochDay;
            _tmpEndEpochDay = _cursor.getLong(_cursorIndexOfEndEpochDay);
            final long _tmpCreatedAtEpochMillis;
            _tmpCreatedAtEpochMillis = _cursor.getLong(_cursorIndexOfCreatedAtEpochMillis);
            final String _tmpColorHex;
            _tmpColorHex = _cursor.getString(_cursorIndexOfColorHex);
            _item = new CategoryFolderEntity(_tmpId,_tmpCategory,_tmpName,_tmpStartEpochDay,_tmpEndEpochDay,_tmpCreatedAtEpochMillis,_tmpColorHex);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getAllPolicyFolderRefs(
      final Continuation<? super List<PolicyFolderCrossRef>> $completion) {
    final String _sql = "SELECT * FROM policy_folder_cross_ref";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<PolicyFolderCrossRef>>() {
      @Override
      @NonNull
      public List<PolicyFolderCrossRef> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfPolicyId = CursorUtil.getColumnIndexOrThrow(_cursor, "policyId");
          final int _cursorIndexOfFolderId = CursorUtil.getColumnIndexOrThrow(_cursor, "folderId");
          final List<PolicyFolderCrossRef> _result = new ArrayList<PolicyFolderCrossRef>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PolicyFolderCrossRef _item;
            final long _tmpPolicyId;
            _tmpPolicyId = _cursor.getLong(_cursorIndexOfPolicyId);
            final long _tmpFolderId;
            _tmpFolderId = _cursor.getLong(_cursorIndexOfFolderId);
            _item = new PolicyFolderCrossRef(_tmpPolicyId,_tmpFolderId);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getAllFolderAttachments(
      final Continuation<? super List<FolderAttachmentEntity>> $completion) {
    final String _sql = "SELECT * FROM folder_attachments";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<FolderAttachmentEntity>>() {
      @Override
      @NonNull
      public List<FolderAttachmentEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfFolderId = CursorUtil.getColumnIndexOrThrow(_cursor, "folderId");
          final int _cursorIndexOfUri = CursorUtil.getColumnIndexOrThrow(_cursor, "uri");
          final int _cursorIndexOfDisplayName = CursorUtil.getColumnIndexOrThrow(_cursor, "displayName");
          final int _cursorIndexOfMimeType = CursorUtil.getColumnIndexOrThrow(_cursor, "mimeType");
          final int _cursorIndexOfAddedAtEpochMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "addedAtEpochMillis");
          final List<FolderAttachmentEntity> _result = new ArrayList<FolderAttachmentEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final FolderAttachmentEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpFolderId;
            _tmpFolderId = _cursor.getLong(_cursorIndexOfFolderId);
            final String _tmpUri;
            _tmpUri = _cursor.getString(_cursorIndexOfUri);
            final String _tmpDisplayName;
            _tmpDisplayName = _cursor.getString(_cursorIndexOfDisplayName);
            final String _tmpMimeType;
            _tmpMimeType = _cursor.getString(_cursorIndexOfMimeType);
            final long _tmpAddedAtEpochMillis;
            _tmpAddedAtEpochMillis = _cursor.getLong(_cursorIndexOfAddedAtEpochMillis);
            _item = new FolderAttachmentEntity(_tmpId,_tmpFolderId,_tmpUri,_tmpDisplayName,_tmpMimeType,_tmpAddedAtEpochMillis);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getAllLoans(final Continuation<? super List<LoanEntity>> $completion) {
    final String _sql = "SELECT * FROM loans";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<LoanEntity>>() {
      @Override
      @NonNull
      public List<LoanEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfLoanName = CursorUtil.getColumnIndexOrThrow(_cursor, "loanName");
          final int _cursorIndexOfLenderName = CursorUtil.getColumnIndexOrThrow(_cursor, "lenderName");
          final int _cursorIndexOfPrincipalAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "principalAmount");
          final int _cursorIndexOfAnnualInterestRate = CursorUtil.getColumnIndexOrThrow(_cursor, "annualInterestRate");
          final int _cursorIndexOfTenureMonths = CursorUtil.getColumnIndexOrThrow(_cursor, "tenureMonths");
          final int _cursorIndexOfEmiAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "emiAmount");
          final int _cursorIndexOfPaymentFrequency = CursorUtil.getColumnIndexOrThrow(_cursor, "paymentFrequency");
          final int _cursorIndexOfStartDateEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "startDateEpochDay");
          final int _cursorIndexOfCreatedAtEpochMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAtEpochMillis");
          final List<LoanEntity> _result = new ArrayList<LoanEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final LoanEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpLoanName;
            _tmpLoanName = _cursor.getString(_cursorIndexOfLoanName);
            final String _tmpLenderName;
            _tmpLenderName = _cursor.getString(_cursorIndexOfLenderName);
            final double _tmpPrincipalAmount;
            _tmpPrincipalAmount = _cursor.getDouble(_cursorIndexOfPrincipalAmount);
            final double _tmpAnnualInterestRate;
            _tmpAnnualInterestRate = _cursor.getDouble(_cursorIndexOfAnnualInterestRate);
            final int _tmpTenureMonths;
            _tmpTenureMonths = _cursor.getInt(_cursorIndexOfTenureMonths);
            final double _tmpEmiAmount;
            _tmpEmiAmount = _cursor.getDouble(_cursorIndexOfEmiAmount);
            final String _tmpPaymentFrequency;
            _tmpPaymentFrequency = _cursor.getString(_cursorIndexOfPaymentFrequency);
            final long _tmpStartDateEpochDay;
            _tmpStartDateEpochDay = _cursor.getLong(_cursorIndexOfStartDateEpochDay);
            final long _tmpCreatedAtEpochMillis;
            _tmpCreatedAtEpochMillis = _cursor.getLong(_cursorIndexOfCreatedAtEpochMillis);
            _item = new LoanEntity(_tmpId,_tmpLoanName,_tmpLenderName,_tmpPrincipalAmount,_tmpAnnualInterestRate,_tmpTenureMonths,_tmpEmiAmount,_tmpPaymentFrequency,_tmpStartDateEpochDay,_tmpCreatedAtEpochMillis);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getAllLoanPayments(
      final Continuation<? super List<LoanPaymentEntity>> $completion) {
    final String _sql = "SELECT * FROM loan_payments";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<LoanPaymentEntity>>() {
      @Override
      @NonNull
      public List<LoanPaymentEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfLoanId = CursorUtil.getColumnIndexOrThrow(_cursor, "loanId");
          final int _cursorIndexOfInstallmentNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "installmentNumber");
          final int _cursorIndexOfDueDateEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "dueDateEpochDay");
          final int _cursorIndexOfAmountDue = CursorUtil.getColumnIndexOrThrow(_cursor, "amountDue");
          final int _cursorIndexOfPrincipalComponent = CursorUtil.getColumnIndexOrThrow(_cursor, "principalComponent");
          final int _cursorIndexOfInterestComponent = CursorUtil.getColumnIndexOrThrow(_cursor, "interestComponent");
          final int _cursorIndexOfIsPaid = CursorUtil.getColumnIndexOrThrow(_cursor, "isPaid");
          final int _cursorIndexOfPaidOnEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "paidOnEpochDay");
          final List<LoanPaymentEntity> _result = new ArrayList<LoanPaymentEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final LoanPaymentEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpLoanId;
            _tmpLoanId = _cursor.getLong(_cursorIndexOfLoanId);
            final int _tmpInstallmentNumber;
            _tmpInstallmentNumber = _cursor.getInt(_cursorIndexOfInstallmentNumber);
            final long _tmpDueDateEpochDay;
            _tmpDueDateEpochDay = _cursor.getLong(_cursorIndexOfDueDateEpochDay);
            final double _tmpAmountDue;
            _tmpAmountDue = _cursor.getDouble(_cursorIndexOfAmountDue);
            final double _tmpPrincipalComponent;
            _tmpPrincipalComponent = _cursor.getDouble(_cursorIndexOfPrincipalComponent);
            final double _tmpInterestComponent;
            _tmpInterestComponent = _cursor.getDouble(_cursorIndexOfInterestComponent);
            final boolean _tmpIsPaid;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsPaid);
            _tmpIsPaid = _tmp != 0;
            final Long _tmpPaidOnEpochDay;
            if (_cursor.isNull(_cursorIndexOfPaidOnEpochDay)) {
              _tmpPaidOnEpochDay = null;
            } else {
              _tmpPaidOnEpochDay = _cursor.getLong(_cursorIndexOfPaidOnEpochDay);
            }
            _item = new LoanPaymentEntity(_tmpId,_tmpLoanId,_tmpInstallmentNumber,_tmpDueDateEpochDay,_tmpAmountDue,_tmpPrincipalComponent,_tmpInterestComponent,_tmpIsPaid,_tmpPaidOnEpochDay);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getAllMoneyLends(final Continuation<? super List<MoneyLendEntity>> $completion) {
    final String _sql = "SELECT * FROM money_lend_entries";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<MoneyLendEntity>>() {
      @Override
      @NonNull
      public List<MoneyLendEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfBorrowerName = CursorUtil.getColumnIndexOrThrow(_cursor, "borrowerName");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfInterestRate = CursorUtil.getColumnIndexOrThrow(_cursor, "interestRate");
          final int _cursorIndexOfStartDateEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "startDateEpochDay");
          final int _cursorIndexOfDueDateEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "dueDateEpochDay");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfPaidInstallmentsJson = CursorUtil.getColumnIndexOrThrow(_cursor, "paidInstallmentsJson");
          final int _cursorIndexOfIsRepaid = CursorUtil.getColumnIndexOrThrow(_cursor, "isRepaid");
          final int _cursorIndexOfCreatedAtEpochMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAtEpochMillis");
          final List<MoneyLendEntity> _result = new ArrayList<MoneyLendEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MoneyLendEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpBorrowerName;
            _tmpBorrowerName = _cursor.getString(_cursorIndexOfBorrowerName);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final double _tmpInterestRate;
            _tmpInterestRate = _cursor.getDouble(_cursorIndexOfInterestRate);
            final long _tmpStartDateEpochDay;
            _tmpStartDateEpochDay = _cursor.getLong(_cursorIndexOfStartDateEpochDay);
            final Long _tmpDueDateEpochDay;
            if (_cursor.isNull(_cursorIndexOfDueDateEpochDay)) {
              _tmpDueDateEpochDay = null;
            } else {
              _tmpDueDateEpochDay = _cursor.getLong(_cursorIndexOfDueDateEpochDay);
            }
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final String _tmpPaidInstallmentsJson;
            _tmpPaidInstallmentsJson = _cursor.getString(_cursorIndexOfPaidInstallmentsJson);
            final boolean _tmpIsRepaid;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsRepaid);
            _tmpIsRepaid = _tmp != 0;
            final long _tmpCreatedAtEpochMillis;
            _tmpCreatedAtEpochMillis = _cursor.getLong(_cursorIndexOfCreatedAtEpochMillis);
            _item = new MoneyLendEntity(_tmpId,_tmpBorrowerName,_tmpAmount,_tmpInterestRate,_tmpStartDateEpochDay,_tmpDueDateEpochDay,_tmpNotes,_tmpPaidInstallmentsJson,_tmpIsRepaid,_tmpCreatedAtEpochMillis);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }

  private void __fetchRelationshippolicyCategoryDetailsAscomUdayPolicytrackerDataDbPolicyCategoryDetailsEntity(
      @NonNull final LongSparseArray<PolicyCategoryDetailsEntity> _map) {
    if (_map.isEmpty()) {
      return;
    }
    if (_map.size() > RoomDatabase.MAX_BIND_PARAMETER_CNT) {
      RelationUtil.recursiveFetchLongSparseArray(_map, false, (map) -> {
        __fetchRelationshippolicyCategoryDetailsAscomUdayPolicytrackerDataDbPolicyCategoryDetailsEntity(map);
        return Unit.INSTANCE;
      });
      return;
    }
    final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("SELECT `policyId`,`premiumFrequency`,`premiumDueDayOfMonth`,`coverageAmount`,`coverageAmountUnit`,`premiumPaymentStartEpochDay`,`premiumPaymentEndEpochDay`,`premiumPaymentTermYears`,`policyValidityEndEpochDay`,`policyTermYears`,`entryAge`,`coverageTillAge`,`nomineeName`,`nomineeRelationship`,`riderAddons`,`paymentMode`,`gracePeriodDays`,`termPolicyStatus`,`totalPayments`,`paidPayments`,`vehicleNumber`,`vehicleType`,`makeModelVariant`,`fuelType`,`vehiclePolicyType`,`vehicleAddons`,`claimHistory`,`documentType`,`issuingRto`,`stateName`,`vehicleClass`,`ownerName`,`linkedVehicleNumber`,`dateOfBirth`,`customFieldValuesJson` FROM `policy_category_details` WHERE `policyId` IN (");
    final int _inputSize = _map.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(")");
    final String _sql = _stringBuilder.toString();
    final int _argCount = 0 + _inputSize;
    final RoomSQLiteQuery _stmt = RoomSQLiteQuery.acquire(_sql, _argCount);
    int _argIndex = 1;
    for (int i = 0; i < _map.size(); i++) {
      final long _item = _map.keyAt(i);
      _stmt.bindLong(_argIndex, _item);
      _argIndex++;
    }
    final Cursor _cursor = DBUtil.query(__db, _stmt, false, null);
    try {
      final int _itemKeyIndex = CursorUtil.getColumnIndex(_cursor, "policyId");
      if (_itemKeyIndex == -1) {
        return;
      }
      final int _cursorIndexOfPolicyId = 0;
      final int _cursorIndexOfPremiumFrequency = 1;
      final int _cursorIndexOfPremiumDueDayOfMonth = 2;
      final int _cursorIndexOfCoverageAmount = 3;
      final int _cursorIndexOfCoverageAmountUnit = 4;
      final int _cursorIndexOfPremiumPaymentStartEpochDay = 5;
      final int _cursorIndexOfPremiumPaymentEndEpochDay = 6;
      final int _cursorIndexOfPremiumPaymentTermYears = 7;
      final int _cursorIndexOfPolicyValidityEndEpochDay = 8;
      final int _cursorIndexOfPolicyTermYears = 9;
      final int _cursorIndexOfEntryAge = 10;
      final int _cursorIndexOfCoverageTillAge = 11;
      final int _cursorIndexOfNomineeName = 12;
      final int _cursorIndexOfNomineeRelationship = 13;
      final int _cursorIndexOfRiderAddons = 14;
      final int _cursorIndexOfPaymentMode = 15;
      final int _cursorIndexOfGracePeriodDays = 16;
      final int _cursorIndexOfTermPolicyStatus = 17;
      final int _cursorIndexOfTotalPayments = 18;
      final int _cursorIndexOfPaidPayments = 19;
      final int _cursorIndexOfVehicleNumber = 20;
      final int _cursorIndexOfVehicleType = 21;
      final int _cursorIndexOfMakeModelVariant = 22;
      final int _cursorIndexOfFuelType = 23;
      final int _cursorIndexOfVehiclePolicyType = 24;
      final int _cursorIndexOfVehicleAddons = 25;
      final int _cursorIndexOfClaimHistory = 26;
      final int _cursorIndexOfDocumentType = 27;
      final int _cursorIndexOfIssuingRto = 28;
      final int _cursorIndexOfStateName = 29;
      final int _cursorIndexOfVehicleClass = 30;
      final int _cursorIndexOfOwnerName = 31;
      final int _cursorIndexOfLinkedVehicleNumber = 32;
      final int _cursorIndexOfDateOfBirth = 33;
      final int _cursorIndexOfCustomFieldValuesJson = 34;
      while (_cursor.moveToNext()) {
        final long _tmpKey;
        _tmpKey = _cursor.getLong(_itemKeyIndex);
        if (_map.containsKey(_tmpKey)) {
          final PolicyCategoryDetailsEntity _item_1;
          final long _tmpPolicyId;
          _tmpPolicyId = _cursor.getLong(_cursorIndexOfPolicyId);
          final String _tmpPremiumFrequency;
          _tmpPremiumFrequency = _cursor.getString(_cursorIndexOfPremiumFrequency);
          final Integer _tmpPremiumDueDayOfMonth;
          if (_cursor.isNull(_cursorIndexOfPremiumDueDayOfMonth)) {
            _tmpPremiumDueDayOfMonth = null;
          } else {
            _tmpPremiumDueDayOfMonth = _cursor.getInt(_cursorIndexOfPremiumDueDayOfMonth);
          }
          final Double _tmpCoverageAmount;
          if (_cursor.isNull(_cursorIndexOfCoverageAmount)) {
            _tmpCoverageAmount = null;
          } else {
            _tmpCoverageAmount = _cursor.getDouble(_cursorIndexOfCoverageAmount);
          }
          final String _tmpCoverageAmountUnit;
          _tmpCoverageAmountUnit = _cursor.getString(_cursorIndexOfCoverageAmountUnit);
          final Long _tmpPremiumPaymentStartEpochDay;
          if (_cursor.isNull(_cursorIndexOfPremiumPaymentStartEpochDay)) {
            _tmpPremiumPaymentStartEpochDay = null;
          } else {
            _tmpPremiumPaymentStartEpochDay = _cursor.getLong(_cursorIndexOfPremiumPaymentStartEpochDay);
          }
          final Long _tmpPremiumPaymentEndEpochDay;
          if (_cursor.isNull(_cursorIndexOfPremiumPaymentEndEpochDay)) {
            _tmpPremiumPaymentEndEpochDay = null;
          } else {
            _tmpPremiumPaymentEndEpochDay = _cursor.getLong(_cursorIndexOfPremiumPaymentEndEpochDay);
          }
          final Integer _tmpPremiumPaymentTermYears;
          if (_cursor.isNull(_cursorIndexOfPremiumPaymentTermYears)) {
            _tmpPremiumPaymentTermYears = null;
          } else {
            _tmpPremiumPaymentTermYears = _cursor.getInt(_cursorIndexOfPremiumPaymentTermYears);
          }
          final Long _tmpPolicyValidityEndEpochDay;
          if (_cursor.isNull(_cursorIndexOfPolicyValidityEndEpochDay)) {
            _tmpPolicyValidityEndEpochDay = null;
          } else {
            _tmpPolicyValidityEndEpochDay = _cursor.getLong(_cursorIndexOfPolicyValidityEndEpochDay);
          }
          final Integer _tmpPolicyTermYears;
          if (_cursor.isNull(_cursorIndexOfPolicyTermYears)) {
            _tmpPolicyTermYears = null;
          } else {
            _tmpPolicyTermYears = _cursor.getInt(_cursorIndexOfPolicyTermYears);
          }
          final Integer _tmpEntryAge;
          if (_cursor.isNull(_cursorIndexOfEntryAge)) {
            _tmpEntryAge = null;
          } else {
            _tmpEntryAge = _cursor.getInt(_cursorIndexOfEntryAge);
          }
          final Integer _tmpCoverageTillAge;
          if (_cursor.isNull(_cursorIndexOfCoverageTillAge)) {
            _tmpCoverageTillAge = null;
          } else {
            _tmpCoverageTillAge = _cursor.getInt(_cursorIndexOfCoverageTillAge);
          }
          final String _tmpNomineeName;
          _tmpNomineeName = _cursor.getString(_cursorIndexOfNomineeName);
          final String _tmpNomineeRelationship;
          _tmpNomineeRelationship = _cursor.getString(_cursorIndexOfNomineeRelationship);
          final String _tmpRiderAddons;
          _tmpRiderAddons = _cursor.getString(_cursorIndexOfRiderAddons);
          final String _tmpPaymentMode;
          _tmpPaymentMode = _cursor.getString(_cursorIndexOfPaymentMode);
          final Integer _tmpGracePeriodDays;
          if (_cursor.isNull(_cursorIndexOfGracePeriodDays)) {
            _tmpGracePeriodDays = null;
          } else {
            _tmpGracePeriodDays = _cursor.getInt(_cursorIndexOfGracePeriodDays);
          }
          final String _tmpTermPolicyStatus;
          _tmpTermPolicyStatus = _cursor.getString(_cursorIndexOfTermPolicyStatus);
          final Integer _tmpTotalPayments;
          if (_cursor.isNull(_cursorIndexOfTotalPayments)) {
            _tmpTotalPayments = null;
          } else {
            _tmpTotalPayments = _cursor.getInt(_cursorIndexOfTotalPayments);
          }
          final Integer _tmpPaidPayments;
          if (_cursor.isNull(_cursorIndexOfPaidPayments)) {
            _tmpPaidPayments = null;
          } else {
            _tmpPaidPayments = _cursor.getInt(_cursorIndexOfPaidPayments);
          }
          final String _tmpVehicleNumber;
          _tmpVehicleNumber = _cursor.getString(_cursorIndexOfVehicleNumber);
          final String _tmpVehicleType;
          _tmpVehicleType = _cursor.getString(_cursorIndexOfVehicleType);
          final String _tmpMakeModelVariant;
          _tmpMakeModelVariant = _cursor.getString(_cursorIndexOfMakeModelVariant);
          final String _tmpFuelType;
          _tmpFuelType = _cursor.getString(_cursorIndexOfFuelType);
          final String _tmpVehiclePolicyType;
          _tmpVehiclePolicyType = _cursor.getString(_cursorIndexOfVehiclePolicyType);
          final String _tmpVehicleAddons;
          _tmpVehicleAddons = _cursor.getString(_cursorIndexOfVehicleAddons);
          final String _tmpClaimHistory;
          _tmpClaimHistory = _cursor.getString(_cursorIndexOfClaimHistory);
          final String _tmpDocumentType;
          _tmpDocumentType = _cursor.getString(_cursorIndexOfDocumentType);
          final String _tmpIssuingRto;
          _tmpIssuingRto = _cursor.getString(_cursorIndexOfIssuingRto);
          final String _tmpStateName;
          _tmpStateName = _cursor.getString(_cursorIndexOfStateName);
          final String _tmpVehicleClass;
          _tmpVehicleClass = _cursor.getString(_cursorIndexOfVehicleClass);
          final String _tmpOwnerName;
          _tmpOwnerName = _cursor.getString(_cursorIndexOfOwnerName);
          final String _tmpLinkedVehicleNumber;
          _tmpLinkedVehicleNumber = _cursor.getString(_cursorIndexOfLinkedVehicleNumber);
          final String _tmpDateOfBirth;
          _tmpDateOfBirth = _cursor.getString(_cursorIndexOfDateOfBirth);
          final String _tmpCustomFieldValuesJson;
          _tmpCustomFieldValuesJson = _cursor.getString(_cursorIndexOfCustomFieldValuesJson);
          _item_1 = new PolicyCategoryDetailsEntity(_tmpPolicyId,_tmpPremiumFrequency,_tmpPremiumDueDayOfMonth,_tmpCoverageAmount,_tmpCoverageAmountUnit,_tmpPremiumPaymentStartEpochDay,_tmpPremiumPaymentEndEpochDay,_tmpPremiumPaymentTermYears,_tmpPolicyValidityEndEpochDay,_tmpPolicyTermYears,_tmpEntryAge,_tmpCoverageTillAge,_tmpNomineeName,_tmpNomineeRelationship,_tmpRiderAddons,_tmpPaymentMode,_tmpGracePeriodDays,_tmpTermPolicyStatus,_tmpTotalPayments,_tmpPaidPayments,_tmpVehicleNumber,_tmpVehicleType,_tmpMakeModelVariant,_tmpFuelType,_tmpVehiclePolicyType,_tmpVehicleAddons,_tmpClaimHistory,_tmpDocumentType,_tmpIssuingRto,_tmpStateName,_tmpVehicleClass,_tmpOwnerName,_tmpLinkedVehicleNumber,_tmpDateOfBirth,_tmpCustomFieldValuesJson);
          _map.put(_tmpKey, _item_1);
        }
      }
    } finally {
      _cursor.close();
    }
  }

  private void __fetchRelationshippolicyHistoryAscomUdayPolicytrackerDataDbPolicyHistoryEntity(
      @NonNull final LongSparseArray<ArrayList<PolicyHistoryEntity>> _map) {
    if (_map.isEmpty()) {
      return;
    }
    if (_map.size() > RoomDatabase.MAX_BIND_PARAMETER_CNT) {
      RelationUtil.recursiveFetchLongSparseArray(_map, true, (map) -> {
        __fetchRelationshippolicyHistoryAscomUdayPolicytrackerDataDbPolicyHistoryEntity(map);
        return Unit.INSTANCE;
      });
      return;
    }
    final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("SELECT `id`,`policyId`,`policyHolderName`,`insurerName`,`policyNumber`,`startDateEpochDay`,`endDateEpochDay`,`premiumAmount`,`attachmentRefs` FROM `policy_history` WHERE `policyId` IN (");
    final int _inputSize = _map.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(")");
    final String _sql = _stringBuilder.toString();
    final int _argCount = 0 + _inputSize;
    final RoomSQLiteQuery _stmt = RoomSQLiteQuery.acquire(_sql, _argCount);
    int _argIndex = 1;
    for (int i = 0; i < _map.size(); i++) {
      final long _item = _map.keyAt(i);
      _stmt.bindLong(_argIndex, _item);
      _argIndex++;
    }
    final Cursor _cursor = DBUtil.query(__db, _stmt, false, null);
    try {
      final int _itemKeyIndex = CursorUtil.getColumnIndex(_cursor, "policyId");
      if (_itemKeyIndex == -1) {
        return;
      }
      final int _cursorIndexOfId = 0;
      final int _cursorIndexOfPolicyId = 1;
      final int _cursorIndexOfPolicyHolderName = 2;
      final int _cursorIndexOfInsurerName = 3;
      final int _cursorIndexOfPolicyNumber = 4;
      final int _cursorIndexOfStartDateEpochDay = 5;
      final int _cursorIndexOfEndDateEpochDay = 6;
      final int _cursorIndexOfPremiumAmount = 7;
      final int _cursorIndexOfAttachmentRefs = 8;
      while (_cursor.moveToNext()) {
        final long _tmpKey;
        _tmpKey = _cursor.getLong(_itemKeyIndex);
        final ArrayList<PolicyHistoryEntity> _tmpRelation = _map.get(_tmpKey);
        if (_tmpRelation != null) {
          final PolicyHistoryEntity _item_1;
          final long _tmpId;
          _tmpId = _cursor.getLong(_cursorIndexOfId);
          final long _tmpPolicyId;
          _tmpPolicyId = _cursor.getLong(_cursorIndexOfPolicyId);
          final String _tmpPolicyHolderName;
          _tmpPolicyHolderName = _cursor.getString(_cursorIndexOfPolicyHolderName);
          final String _tmpInsurerName;
          _tmpInsurerName = _cursor.getString(_cursorIndexOfInsurerName);
          final String _tmpPolicyNumber;
          _tmpPolicyNumber = _cursor.getString(_cursorIndexOfPolicyNumber);
          final long _tmpStartDateEpochDay;
          _tmpStartDateEpochDay = _cursor.getLong(_cursorIndexOfStartDateEpochDay);
          final long _tmpEndDateEpochDay;
          _tmpEndDateEpochDay = _cursor.getLong(_cursorIndexOfEndDateEpochDay);
          final double _tmpPremiumAmount;
          _tmpPremiumAmount = _cursor.getDouble(_cursorIndexOfPremiumAmount);
          final String _tmpAttachmentRefs;
          _tmpAttachmentRefs = _cursor.getString(_cursorIndexOfAttachmentRefs);
          _item_1 = new PolicyHistoryEntity(_tmpId,_tmpPolicyId,_tmpPolicyHolderName,_tmpInsurerName,_tmpPolicyNumber,_tmpStartDateEpochDay,_tmpEndDateEpochDay,_tmpPremiumAmount,_tmpAttachmentRefs);
          _tmpRelation.add(_item_1);
        }
      }
    } finally {
      _cursor.close();
    }
  }

  private void __fetchRelationshipattachmentsAscomUdayPolicytrackerDataDbAttachmentEntity(
      @NonNull final LongSparseArray<ArrayList<AttachmentEntity>> _map) {
    if (_map.isEmpty()) {
      return;
    }
    if (_map.size() > RoomDatabase.MAX_BIND_PARAMETER_CNT) {
      RelationUtil.recursiveFetchLongSparseArray(_map, true, (map) -> {
        __fetchRelationshipattachmentsAscomUdayPolicytrackerDataDbAttachmentEntity(map);
        return Unit.INSTANCE;
      });
      return;
    }
    final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("SELECT `id`,`policyId`,`uri`,`displayName`,`mimeType`,`addedAtEpochMillis` FROM `attachments` WHERE `policyId` IN (");
    final int _inputSize = _map.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(")");
    final String _sql = _stringBuilder.toString();
    final int _argCount = 0 + _inputSize;
    final RoomSQLiteQuery _stmt = RoomSQLiteQuery.acquire(_sql, _argCount);
    int _argIndex = 1;
    for (int i = 0; i < _map.size(); i++) {
      final long _item = _map.keyAt(i);
      _stmt.bindLong(_argIndex, _item);
      _argIndex++;
    }
    final Cursor _cursor = DBUtil.query(__db, _stmt, false, null);
    try {
      final int _itemKeyIndex = CursorUtil.getColumnIndex(_cursor, "policyId");
      if (_itemKeyIndex == -1) {
        return;
      }
      final int _cursorIndexOfId = 0;
      final int _cursorIndexOfPolicyId = 1;
      final int _cursorIndexOfUri = 2;
      final int _cursorIndexOfDisplayName = 3;
      final int _cursorIndexOfMimeType = 4;
      final int _cursorIndexOfAddedAtEpochMillis = 5;
      while (_cursor.moveToNext()) {
        final long _tmpKey;
        _tmpKey = _cursor.getLong(_itemKeyIndex);
        final ArrayList<AttachmentEntity> _tmpRelation = _map.get(_tmpKey);
        if (_tmpRelation != null) {
          final AttachmentEntity _item_1;
          final long _tmpId;
          _tmpId = _cursor.getLong(_cursorIndexOfId);
          final long _tmpPolicyId;
          _tmpPolicyId = _cursor.getLong(_cursorIndexOfPolicyId);
          final String _tmpUri;
          _tmpUri = _cursor.getString(_cursorIndexOfUri);
          final String _tmpDisplayName;
          _tmpDisplayName = _cursor.getString(_cursorIndexOfDisplayName);
          final String _tmpMimeType;
          _tmpMimeType = _cursor.getString(_cursorIndexOfMimeType);
          final long _tmpAddedAtEpochMillis;
          _tmpAddedAtEpochMillis = _cursor.getLong(_cursorIndexOfAddedAtEpochMillis);
          _item_1 = new AttachmentEntity(_tmpId,_tmpPolicyId,_tmpUri,_tmpDisplayName,_tmpMimeType,_tmpAddedAtEpochMillis);
          _tmpRelation.add(_item_1);
        }
      }
    } finally {
      _cursor.close();
    }
  }

  private void __fetchRelationshipfuturePoliciesAscomUdayPolicytrackerDataDbFuturePolicyEntity(
      @NonNull final LongSparseArray<ArrayList<FuturePolicyEntity>> _map) {
    if (_map.isEmpty()) {
      return;
    }
    if (_map.size() > RoomDatabase.MAX_BIND_PARAMETER_CNT) {
      RelationUtil.recursiveFetchLongSparseArray(_map, true, (map) -> {
        __fetchRelationshipfuturePoliciesAscomUdayPolicytrackerDataDbFuturePolicyEntity(map);
        return Unit.INSTANCE;
      });
      return;
    }
    final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("SELECT `id`,`policyId`,`policyHolderName`,`policyName`,`policyNumber`,`startDateEpochDay`,`expiryDateEpochDay`,`insurerName`,`premiumAmount`,`createdAtEpochMillis`,`attachmentRefs` FROM `future_policies` WHERE `policyId` IN (");
    final int _inputSize = _map.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(")");
    final String _sql = _stringBuilder.toString();
    final int _argCount = 0 + _inputSize;
    final RoomSQLiteQuery _stmt = RoomSQLiteQuery.acquire(_sql, _argCount);
    int _argIndex = 1;
    for (int i = 0; i < _map.size(); i++) {
      final long _item = _map.keyAt(i);
      _stmt.bindLong(_argIndex, _item);
      _argIndex++;
    }
    final Cursor _cursor = DBUtil.query(__db, _stmt, false, null);
    try {
      final int _itemKeyIndex = CursorUtil.getColumnIndex(_cursor, "policyId");
      if (_itemKeyIndex == -1) {
        return;
      }
      final int _cursorIndexOfId = 0;
      final int _cursorIndexOfPolicyId = 1;
      final int _cursorIndexOfPolicyHolderName = 2;
      final int _cursorIndexOfPolicyName = 3;
      final int _cursorIndexOfPolicyNumber = 4;
      final int _cursorIndexOfStartDateEpochDay = 5;
      final int _cursorIndexOfExpiryDateEpochDay = 6;
      final int _cursorIndexOfInsurerName = 7;
      final int _cursorIndexOfPremiumAmount = 8;
      final int _cursorIndexOfCreatedAtEpochMillis = 9;
      final int _cursorIndexOfAttachmentRefs = 10;
      while (_cursor.moveToNext()) {
        final long _tmpKey;
        _tmpKey = _cursor.getLong(_itemKeyIndex);
        final ArrayList<FuturePolicyEntity> _tmpRelation = _map.get(_tmpKey);
        if (_tmpRelation != null) {
          final FuturePolicyEntity _item_1;
          final long _tmpId;
          _tmpId = _cursor.getLong(_cursorIndexOfId);
          final long _tmpPolicyId;
          _tmpPolicyId = _cursor.getLong(_cursorIndexOfPolicyId);
          final String _tmpPolicyHolderName;
          _tmpPolicyHolderName = _cursor.getString(_cursorIndexOfPolicyHolderName);
          final String _tmpPolicyName;
          _tmpPolicyName = _cursor.getString(_cursorIndexOfPolicyName);
          final String _tmpPolicyNumber;
          _tmpPolicyNumber = _cursor.getString(_cursorIndexOfPolicyNumber);
          final long _tmpStartDateEpochDay;
          _tmpStartDateEpochDay = _cursor.getLong(_cursorIndexOfStartDateEpochDay);
          final long _tmpExpiryDateEpochDay;
          _tmpExpiryDateEpochDay = _cursor.getLong(_cursorIndexOfExpiryDateEpochDay);
          final String _tmpInsurerName;
          _tmpInsurerName = _cursor.getString(_cursorIndexOfInsurerName);
          final double _tmpPremiumAmount;
          _tmpPremiumAmount = _cursor.getDouble(_cursorIndexOfPremiumAmount);
          final long _tmpCreatedAtEpochMillis;
          _tmpCreatedAtEpochMillis = _cursor.getLong(_cursorIndexOfCreatedAtEpochMillis);
          final String _tmpAttachmentRefs;
          _tmpAttachmentRefs = _cursor.getString(_cursorIndexOfAttachmentRefs);
          _item_1 = new FuturePolicyEntity(_tmpId,_tmpPolicyId,_tmpPolicyHolderName,_tmpPolicyName,_tmpPolicyNumber,_tmpStartDateEpochDay,_tmpExpiryDateEpochDay,_tmpInsurerName,_tmpPremiumAmount,_tmpCreatedAtEpochMillis,_tmpAttachmentRefs);
          _tmpRelation.add(_item_1);
        }
      }
    } finally {
      _cursor.close();
    }
  }

  private void __fetchRelationshippoliciesAscomUdayPolicytrackerDataDbPolicyEntity(
      @NonNull final LongSparseArray<ArrayList<PolicyEntity>> _map) {
    if (_map.isEmpty()) {
      return;
    }
    if (_map.size() > RoomDatabase.MAX_BIND_PARAMETER_CNT) {
      RelationUtil.recursiveFetchLongSparseArray(_map, true, (map) -> {
        __fetchRelationshippoliciesAscomUdayPolicytrackerDataDbPolicyEntity(map);
        return Unit.INSTANCE;
      });
      return;
    }
    final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("SELECT `policies`.`id` AS `id`,`policies`.`category` AS `category`,`policies`.`policyHolderName` AS `policyHolderName`,`policies`.`policyName` AS `policyName`,`policies`.`policyNumber` AS `policyNumber`,`policies`.`startDateEpochDay` AS `startDateEpochDay`,`policies`.`expiryDateEpochDay` AS `expiryDateEpochDay`,`policies`.`insurerName` AS `insurerName`,`policies`.`previousInsurerName` AS `previousInsurerName`,`policies`.`premiumAmount` AS `premiumAmount`,`policies`.`notes` AS `notes`,_junction.`folderId` FROM `policy_folder_cross_ref` AS _junction INNER JOIN `policies` ON (_junction.`policyId` = `policies`.`id`) WHERE _junction.`folderId` IN (");
    final int _inputSize = _map.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(")");
    final String _sql = _stringBuilder.toString();
    final int _argCount = 0 + _inputSize;
    final RoomSQLiteQuery _stmt = RoomSQLiteQuery.acquire(_sql, _argCount);
    int _argIndex = 1;
    for (int i = 0; i < _map.size(); i++) {
      final long _item = _map.keyAt(i);
      _stmt.bindLong(_argIndex, _item);
      _argIndex++;
    }
    final Cursor _cursor = DBUtil.query(__db, _stmt, false, null);
    try {
      // _junction.folderId;
      final int _itemKeyIndex = 11;
      if (_itemKeyIndex == -1) {
        return;
      }
      final int _cursorIndexOfId = 0;
      final int _cursorIndexOfCategory = 1;
      final int _cursorIndexOfPolicyHolderName = 2;
      final int _cursorIndexOfPolicyName = 3;
      final int _cursorIndexOfPolicyNumber = 4;
      final int _cursorIndexOfStartDateEpochDay = 5;
      final int _cursorIndexOfExpiryDateEpochDay = 6;
      final int _cursorIndexOfInsurerName = 7;
      final int _cursorIndexOfPreviousInsurerName = 8;
      final int _cursorIndexOfPremiumAmount = 9;
      final int _cursorIndexOfNotes = 10;
      while (_cursor.moveToNext()) {
        final long _tmpKey;
        _tmpKey = _cursor.getLong(_itemKeyIndex);
        final ArrayList<PolicyEntity> _tmpRelation = _map.get(_tmpKey);
        if (_tmpRelation != null) {
          final PolicyEntity _item_1;
          final long _tmpId;
          _tmpId = _cursor.getLong(_cursorIndexOfId);
          final String _tmpCategory;
          _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
          final String _tmpPolicyHolderName;
          _tmpPolicyHolderName = _cursor.getString(_cursorIndexOfPolicyHolderName);
          final String _tmpPolicyName;
          _tmpPolicyName = _cursor.getString(_cursorIndexOfPolicyName);
          final String _tmpPolicyNumber;
          _tmpPolicyNumber = _cursor.getString(_cursorIndexOfPolicyNumber);
          final long _tmpStartDateEpochDay;
          _tmpStartDateEpochDay = _cursor.getLong(_cursorIndexOfStartDateEpochDay);
          final long _tmpExpiryDateEpochDay;
          _tmpExpiryDateEpochDay = _cursor.getLong(_cursorIndexOfExpiryDateEpochDay);
          final String _tmpInsurerName;
          _tmpInsurerName = _cursor.getString(_cursorIndexOfInsurerName);
          final String _tmpPreviousInsurerName;
          _tmpPreviousInsurerName = _cursor.getString(_cursorIndexOfPreviousInsurerName);
          final double _tmpPremiumAmount;
          _tmpPremiumAmount = _cursor.getDouble(_cursorIndexOfPremiumAmount);
          final String _tmpNotes;
          _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
          _item_1 = new PolicyEntity(_tmpId,_tmpCategory,_tmpPolicyHolderName,_tmpPolicyName,_tmpPolicyNumber,_tmpStartDateEpochDay,_tmpExpiryDateEpochDay,_tmpInsurerName,_tmpPreviousInsurerName,_tmpPremiumAmount,_tmpNotes);
          _tmpRelation.add(_item_1);
        }
      }
    } finally {
      _cursor.close();
    }
  }

  private void __fetchRelationshipfolderAttachmentsAscomUdayPolicytrackerDataDbFolderAttachmentEntity(
      @NonNull final LongSparseArray<ArrayList<FolderAttachmentEntity>> _map) {
    if (_map.isEmpty()) {
      return;
    }
    if (_map.size() > RoomDatabase.MAX_BIND_PARAMETER_CNT) {
      RelationUtil.recursiveFetchLongSparseArray(_map, true, (map) -> {
        __fetchRelationshipfolderAttachmentsAscomUdayPolicytrackerDataDbFolderAttachmentEntity(map);
        return Unit.INSTANCE;
      });
      return;
    }
    final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("SELECT `id`,`folderId`,`uri`,`displayName`,`mimeType`,`addedAtEpochMillis` FROM `folder_attachments` WHERE `folderId` IN (");
    final int _inputSize = _map.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(")");
    final String _sql = _stringBuilder.toString();
    final int _argCount = 0 + _inputSize;
    final RoomSQLiteQuery _stmt = RoomSQLiteQuery.acquire(_sql, _argCount);
    int _argIndex = 1;
    for (int i = 0; i < _map.size(); i++) {
      final long _item = _map.keyAt(i);
      _stmt.bindLong(_argIndex, _item);
      _argIndex++;
    }
    final Cursor _cursor = DBUtil.query(__db, _stmt, false, null);
    try {
      final int _itemKeyIndex = CursorUtil.getColumnIndex(_cursor, "folderId");
      if (_itemKeyIndex == -1) {
        return;
      }
      final int _cursorIndexOfId = 0;
      final int _cursorIndexOfFolderId = 1;
      final int _cursorIndexOfUri = 2;
      final int _cursorIndexOfDisplayName = 3;
      final int _cursorIndexOfMimeType = 4;
      final int _cursorIndexOfAddedAtEpochMillis = 5;
      while (_cursor.moveToNext()) {
        final long _tmpKey;
        _tmpKey = _cursor.getLong(_itemKeyIndex);
        final ArrayList<FolderAttachmentEntity> _tmpRelation = _map.get(_tmpKey);
        if (_tmpRelation != null) {
          final FolderAttachmentEntity _item_1;
          final long _tmpId;
          _tmpId = _cursor.getLong(_cursorIndexOfId);
          final long _tmpFolderId;
          _tmpFolderId = _cursor.getLong(_cursorIndexOfFolderId);
          final String _tmpUri;
          _tmpUri = _cursor.getString(_cursorIndexOfUri);
          final String _tmpDisplayName;
          _tmpDisplayName = _cursor.getString(_cursorIndexOfDisplayName);
          final String _tmpMimeType;
          _tmpMimeType = _cursor.getString(_cursorIndexOfMimeType);
          final long _tmpAddedAtEpochMillis;
          _tmpAddedAtEpochMillis = _cursor.getLong(_cursorIndexOfAddedAtEpochMillis);
          _item_1 = new FolderAttachmentEntity(_tmpId,_tmpFolderId,_tmpUri,_tmpDisplayName,_tmpMimeType,_tmpAddedAtEpochMillis);
          _tmpRelation.add(_item_1);
        }
      }
    } finally {
      _cursor.close();
    }
  }
}
