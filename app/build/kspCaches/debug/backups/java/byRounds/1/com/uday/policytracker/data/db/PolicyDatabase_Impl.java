package com.uday.policytracker.data.db;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class PolicyDatabase_Impl extends PolicyDatabase {
  private volatile PolicyDao _policyDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(13) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `policies` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `category` TEXT NOT NULL, `policyHolderName` TEXT NOT NULL, `policyName` TEXT NOT NULL, `policyNumber` TEXT NOT NULL, `startDateEpochDay` INTEGER NOT NULL, `expiryDateEpochDay` INTEGER NOT NULL, `insurerName` TEXT NOT NULL, `previousInsurerName` TEXT NOT NULL, `premiumAmount` REAL NOT NULL, `notes` TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `policy_history` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `policyId` INTEGER NOT NULL, `policyHolderName` TEXT NOT NULL, `insurerName` TEXT NOT NULL, `policyNumber` TEXT NOT NULL, `startDateEpochDay` INTEGER NOT NULL, `endDateEpochDay` INTEGER NOT NULL, `premiumAmount` REAL NOT NULL, `attachmentRefs` TEXT NOT NULL, FOREIGN KEY(`policyId`) REFERENCES `policies`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_policy_history_policyId` ON `policy_history` (`policyId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `attachments` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `policyId` INTEGER NOT NULL, `uri` TEXT NOT NULL, `displayName` TEXT NOT NULL, `mimeType` TEXT NOT NULL, `addedAtEpochMillis` INTEGER NOT NULL, FOREIGN KEY(`policyId`) REFERENCES `policies`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_attachments_policyId` ON `attachments` (`policyId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `future_policies` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `policyId` INTEGER NOT NULL, `policyHolderName` TEXT NOT NULL, `policyName` TEXT NOT NULL, `policyNumber` TEXT NOT NULL, `startDateEpochDay` INTEGER NOT NULL, `expiryDateEpochDay` INTEGER NOT NULL, `insurerName` TEXT NOT NULL, `premiumAmount` REAL NOT NULL, `createdAtEpochMillis` INTEGER NOT NULL, `attachmentRefs` TEXT NOT NULL, FOREIGN KEY(`policyId`) REFERENCES `policies`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_future_policies_policyId` ON `future_policies` (`policyId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `policy_category_details` (`policyId` INTEGER NOT NULL, `premiumFrequency` TEXT NOT NULL, `premiumDueDayOfMonth` INTEGER, `coverageAmount` REAL, `coverageAmountUnit` TEXT NOT NULL, `premiumPaymentStartEpochDay` INTEGER, `premiumPaymentEndEpochDay` INTEGER, `premiumPaymentTermYears` INTEGER, `policyValidityEndEpochDay` INTEGER, `policyTermYears` INTEGER, `entryAge` INTEGER, `coverageTillAge` INTEGER, `nomineeName` TEXT NOT NULL, `nomineeRelationship` TEXT NOT NULL, `riderAddons` TEXT NOT NULL, `paymentMode` TEXT NOT NULL, `gracePeriodDays` INTEGER, `termPolicyStatus` TEXT NOT NULL, `totalPayments` INTEGER, `paidPayments` INTEGER, `vehicleNumber` TEXT NOT NULL, `vehicleType` TEXT NOT NULL, `makeModelVariant` TEXT NOT NULL, `fuelType` TEXT NOT NULL, `vehiclePolicyType` TEXT NOT NULL, `vehicleAddons` TEXT NOT NULL, `claimHistory` TEXT NOT NULL, `documentType` TEXT NOT NULL, `issuingRto` TEXT NOT NULL, `stateName` TEXT NOT NULL, `vehicleClass` TEXT NOT NULL, `ownerName` TEXT NOT NULL, `linkedVehicleNumber` TEXT NOT NULL, `dateOfBirth` TEXT NOT NULL, `customFieldValuesJson` TEXT NOT NULL, PRIMARY KEY(`policyId`), FOREIGN KEY(`policyId`) REFERENCES `policies`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_policy_category_details_policyId` ON `policy_category_details` (`policyId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `category_folders` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `category` TEXT NOT NULL, `name` TEXT NOT NULL, `startEpochDay` INTEGER NOT NULL, `endEpochDay` INTEGER NOT NULL, `createdAtEpochMillis` INTEGER NOT NULL, `colorHex` TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `policy_folder_cross_ref` (`policyId` INTEGER NOT NULL, `folderId` INTEGER NOT NULL, PRIMARY KEY(`policyId`, `folderId`))");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_policy_folder_cross_ref_folderId` ON `policy_folder_cross_ref` (`folderId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_policy_folder_cross_ref_policyId` ON `policy_folder_cross_ref` (`policyId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `folder_attachments` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `folderId` INTEGER NOT NULL, `uri` TEXT NOT NULL, `displayName` TEXT NOT NULL, `mimeType` TEXT NOT NULL, `addedAtEpochMillis` INTEGER NOT NULL, FOREIGN KEY(`folderId`) REFERENCES `category_folders`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_folder_attachments_folderId` ON `folder_attachments` (`folderId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `loans` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `loanName` TEXT NOT NULL, `lenderName` TEXT NOT NULL, `principalAmount` REAL NOT NULL, `annualInterestRate` REAL NOT NULL, `tenureMonths` INTEGER NOT NULL, `emiAmount` REAL NOT NULL, `paymentFrequency` TEXT NOT NULL, `startDateEpochDay` INTEGER NOT NULL, `createdAtEpochMillis` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `loan_payments` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `loanId` INTEGER NOT NULL, `installmentNumber` INTEGER NOT NULL, `dueDateEpochDay` INTEGER NOT NULL, `amountDue` REAL NOT NULL, `principalComponent` REAL NOT NULL, `interestComponent` REAL NOT NULL, `isPaid` INTEGER NOT NULL, `paidOnEpochDay` INTEGER, FOREIGN KEY(`loanId`) REFERENCES `loans`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_loan_payments_loanId` ON `loan_payments` (`loanId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `money_lend_entries` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `borrowerName` TEXT NOT NULL, `amount` REAL NOT NULL, `interestRate` REAL NOT NULL, `startDateEpochDay` INTEGER NOT NULL, `dueDateEpochDay` INTEGER, `notes` TEXT NOT NULL, `paidInstallmentsJson` TEXT NOT NULL, `isRepaid` INTEGER NOT NULL, `createdAtEpochMillis` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '67c5ca831d13b3b69f7199a5b0920f6c')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `policies`");
        db.execSQL("DROP TABLE IF EXISTS `policy_history`");
        db.execSQL("DROP TABLE IF EXISTS `attachments`");
        db.execSQL("DROP TABLE IF EXISTS `future_policies`");
        db.execSQL("DROP TABLE IF EXISTS `policy_category_details`");
        db.execSQL("DROP TABLE IF EXISTS `category_folders`");
        db.execSQL("DROP TABLE IF EXISTS `policy_folder_cross_ref`");
        db.execSQL("DROP TABLE IF EXISTS `folder_attachments`");
        db.execSQL("DROP TABLE IF EXISTS `loans`");
        db.execSQL("DROP TABLE IF EXISTS `loan_payments`");
        db.execSQL("DROP TABLE IF EXISTS `money_lend_entries`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        db.execSQL("PRAGMA foreign_keys = ON");
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsPolicies = new HashMap<String, TableInfo.Column>(11);
        _columnsPolicies.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPolicies.put("category", new TableInfo.Column("category", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPolicies.put("policyHolderName", new TableInfo.Column("policyHolderName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPolicies.put("policyName", new TableInfo.Column("policyName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPolicies.put("policyNumber", new TableInfo.Column("policyNumber", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPolicies.put("startDateEpochDay", new TableInfo.Column("startDateEpochDay", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPolicies.put("expiryDateEpochDay", new TableInfo.Column("expiryDateEpochDay", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPolicies.put("insurerName", new TableInfo.Column("insurerName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPolicies.put("previousInsurerName", new TableInfo.Column("previousInsurerName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPolicies.put("premiumAmount", new TableInfo.Column("premiumAmount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPolicies.put("notes", new TableInfo.Column("notes", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPolicies = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPolicies = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoPolicies = new TableInfo("policies", _columnsPolicies, _foreignKeysPolicies, _indicesPolicies);
        final TableInfo _existingPolicies = TableInfo.read(db, "policies");
        if (!_infoPolicies.equals(_existingPolicies)) {
          return new RoomOpenHelper.ValidationResult(false, "policies(com.uday.policytracker.data.db.PolicyEntity).\n"
                  + " Expected:\n" + _infoPolicies + "\n"
                  + " Found:\n" + _existingPolicies);
        }
        final HashMap<String, TableInfo.Column> _columnsPolicyHistory = new HashMap<String, TableInfo.Column>(9);
        _columnsPolicyHistory.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPolicyHistory.put("policyId", new TableInfo.Column("policyId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPolicyHistory.put("policyHolderName", new TableInfo.Column("policyHolderName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPolicyHistory.put("insurerName", new TableInfo.Column("insurerName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPolicyHistory.put("policyNumber", new TableInfo.Column("policyNumber", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPolicyHistory.put("startDateEpochDay", new TableInfo.Column("startDateEpochDay", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPolicyHistory.put("endDateEpochDay", new TableInfo.Column("endDateEpochDay", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPolicyHistory.put("premiumAmount", new TableInfo.Column("premiumAmount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPolicyHistory.put("attachmentRefs", new TableInfo.Column("attachmentRefs", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPolicyHistory = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysPolicyHistory.add(new TableInfo.ForeignKey("policies", "CASCADE", "NO ACTION", Arrays.asList("policyId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesPolicyHistory = new HashSet<TableInfo.Index>(1);
        _indicesPolicyHistory.add(new TableInfo.Index("index_policy_history_policyId", false, Arrays.asList("policyId"), Arrays.asList("ASC")));
        final TableInfo _infoPolicyHistory = new TableInfo("policy_history", _columnsPolicyHistory, _foreignKeysPolicyHistory, _indicesPolicyHistory);
        final TableInfo _existingPolicyHistory = TableInfo.read(db, "policy_history");
        if (!_infoPolicyHistory.equals(_existingPolicyHistory)) {
          return new RoomOpenHelper.ValidationResult(false, "policy_history(com.uday.policytracker.data.db.PolicyHistoryEntity).\n"
                  + " Expected:\n" + _infoPolicyHistory + "\n"
                  + " Found:\n" + _existingPolicyHistory);
        }
        final HashMap<String, TableInfo.Column> _columnsAttachments = new HashMap<String, TableInfo.Column>(6);
        _columnsAttachments.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAttachments.put("policyId", new TableInfo.Column("policyId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAttachments.put("uri", new TableInfo.Column("uri", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAttachments.put("displayName", new TableInfo.Column("displayName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAttachments.put("mimeType", new TableInfo.Column("mimeType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAttachments.put("addedAtEpochMillis", new TableInfo.Column("addedAtEpochMillis", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysAttachments = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysAttachments.add(new TableInfo.ForeignKey("policies", "CASCADE", "NO ACTION", Arrays.asList("policyId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesAttachments = new HashSet<TableInfo.Index>(1);
        _indicesAttachments.add(new TableInfo.Index("index_attachments_policyId", false, Arrays.asList("policyId"), Arrays.asList("ASC")));
        final TableInfo _infoAttachments = new TableInfo("attachments", _columnsAttachments, _foreignKeysAttachments, _indicesAttachments);
        final TableInfo _existingAttachments = TableInfo.read(db, "attachments");
        if (!_infoAttachments.equals(_existingAttachments)) {
          return new RoomOpenHelper.ValidationResult(false, "attachments(com.uday.policytracker.data.db.AttachmentEntity).\n"
                  + " Expected:\n" + _infoAttachments + "\n"
                  + " Found:\n" + _existingAttachments);
        }
        final HashMap<String, TableInfo.Column> _columnsFuturePolicies = new HashMap<String, TableInfo.Column>(11);
        _columnsFuturePolicies.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFuturePolicies.put("policyId", new TableInfo.Column("policyId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFuturePolicies.put("policyHolderName", new TableInfo.Column("policyHolderName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFuturePolicies.put("policyName", new TableInfo.Column("policyName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFuturePolicies.put("policyNumber", new TableInfo.Column("policyNumber", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFuturePolicies.put("startDateEpochDay", new TableInfo.Column("startDateEpochDay", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFuturePolicies.put("expiryDateEpochDay", new TableInfo.Column("expiryDateEpochDay", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFuturePolicies.put("insurerName", new TableInfo.Column("insurerName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFuturePolicies.put("premiumAmount", new TableInfo.Column("premiumAmount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFuturePolicies.put("createdAtEpochMillis", new TableInfo.Column("createdAtEpochMillis", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFuturePolicies.put("attachmentRefs", new TableInfo.Column("attachmentRefs", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysFuturePolicies = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysFuturePolicies.add(new TableInfo.ForeignKey("policies", "CASCADE", "NO ACTION", Arrays.asList("policyId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesFuturePolicies = new HashSet<TableInfo.Index>(1);
        _indicesFuturePolicies.add(new TableInfo.Index("index_future_policies_policyId", false, Arrays.asList("policyId"), Arrays.asList("ASC")));
        final TableInfo _infoFuturePolicies = new TableInfo("future_policies", _columnsFuturePolicies, _foreignKeysFuturePolicies, _indicesFuturePolicies);
        final TableInfo _existingFuturePolicies = TableInfo.read(db, "future_policies");
        if (!_infoFuturePolicies.equals(_existingFuturePolicies)) {
          return new RoomOpenHelper.ValidationResult(false, "future_policies(com.uday.policytracker.data.db.FuturePolicyEntity).\n"
                  + " Expected:\n" + _infoFuturePolicies + "\n"
                  + " Found:\n" + _existingFuturePolicies);
        }
        final HashMap<String, TableInfo.Column> _columnsPolicyCategoryDetails = new HashMap<String, TableInfo.Column>(35);
        _columnsPolicyCategoryDetails.put("policyId", new TableInfo.Column("policyId", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPolicyCategoryDetails.put("premiumFrequency", new TableInfo.Column("premiumFrequency", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPolicyCategoryDetails.put("premiumDueDayOfMonth", new TableInfo.Column("premiumDueDayOfMonth", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPolicyCategoryDetails.put("coverageAmount", new TableInfo.Column("coverageAmount", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPolicyCategoryDetails.put("coverageAmountUnit", new TableInfo.Column("coverageAmountUnit", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPolicyCategoryDetails.put("premiumPaymentStartEpochDay", new TableInfo.Column("premiumPaymentStartEpochDay", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPolicyCategoryDetails.put("premiumPaymentEndEpochDay", new TableInfo.Column("premiumPaymentEndEpochDay", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPolicyCategoryDetails.put("premiumPaymentTermYears", new TableInfo.Column("premiumPaymentTermYears", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPolicyCategoryDetails.put("policyValidityEndEpochDay", new TableInfo.Column("policyValidityEndEpochDay", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPolicyCategoryDetails.put("policyTermYears", new TableInfo.Column("policyTermYears", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPolicyCategoryDetails.put("entryAge", new TableInfo.Column("entryAge", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPolicyCategoryDetails.put("coverageTillAge", new TableInfo.Column("coverageTillAge", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPolicyCategoryDetails.put("nomineeName", new TableInfo.Column("nomineeName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPolicyCategoryDetails.put("nomineeRelationship", new TableInfo.Column("nomineeRelationship", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPolicyCategoryDetails.put("riderAddons", new TableInfo.Column("riderAddons", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPolicyCategoryDetails.put("paymentMode", new TableInfo.Column("paymentMode", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPolicyCategoryDetails.put("gracePeriodDays", new TableInfo.Column("gracePeriodDays", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPolicyCategoryDetails.put("termPolicyStatus", new TableInfo.Column("termPolicyStatus", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPolicyCategoryDetails.put("totalPayments", new TableInfo.Column("totalPayments", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPolicyCategoryDetails.put("paidPayments", new TableInfo.Column("paidPayments", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPolicyCategoryDetails.put("vehicleNumber", new TableInfo.Column("vehicleNumber", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPolicyCategoryDetails.put("vehicleType", new TableInfo.Column("vehicleType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPolicyCategoryDetails.put("makeModelVariant", new TableInfo.Column("makeModelVariant", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPolicyCategoryDetails.put("fuelType", new TableInfo.Column("fuelType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPolicyCategoryDetails.put("vehiclePolicyType", new TableInfo.Column("vehiclePolicyType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPolicyCategoryDetails.put("vehicleAddons", new TableInfo.Column("vehicleAddons", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPolicyCategoryDetails.put("claimHistory", new TableInfo.Column("claimHistory", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPolicyCategoryDetails.put("documentType", new TableInfo.Column("documentType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPolicyCategoryDetails.put("issuingRto", new TableInfo.Column("issuingRto", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPolicyCategoryDetails.put("stateName", new TableInfo.Column("stateName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPolicyCategoryDetails.put("vehicleClass", new TableInfo.Column("vehicleClass", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPolicyCategoryDetails.put("ownerName", new TableInfo.Column("ownerName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPolicyCategoryDetails.put("linkedVehicleNumber", new TableInfo.Column("linkedVehicleNumber", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPolicyCategoryDetails.put("dateOfBirth", new TableInfo.Column("dateOfBirth", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPolicyCategoryDetails.put("customFieldValuesJson", new TableInfo.Column("customFieldValuesJson", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPolicyCategoryDetails = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysPolicyCategoryDetails.add(new TableInfo.ForeignKey("policies", "CASCADE", "NO ACTION", Arrays.asList("policyId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesPolicyCategoryDetails = new HashSet<TableInfo.Index>(1);
        _indicesPolicyCategoryDetails.add(new TableInfo.Index("index_policy_category_details_policyId", false, Arrays.asList("policyId"), Arrays.asList("ASC")));
        final TableInfo _infoPolicyCategoryDetails = new TableInfo("policy_category_details", _columnsPolicyCategoryDetails, _foreignKeysPolicyCategoryDetails, _indicesPolicyCategoryDetails);
        final TableInfo _existingPolicyCategoryDetails = TableInfo.read(db, "policy_category_details");
        if (!_infoPolicyCategoryDetails.equals(_existingPolicyCategoryDetails)) {
          return new RoomOpenHelper.ValidationResult(false, "policy_category_details(com.uday.policytracker.data.db.PolicyCategoryDetailsEntity).\n"
                  + " Expected:\n" + _infoPolicyCategoryDetails + "\n"
                  + " Found:\n" + _existingPolicyCategoryDetails);
        }
        final HashMap<String, TableInfo.Column> _columnsCategoryFolders = new HashMap<String, TableInfo.Column>(7);
        _columnsCategoryFolders.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategoryFolders.put("category", new TableInfo.Column("category", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategoryFolders.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategoryFolders.put("startEpochDay", new TableInfo.Column("startEpochDay", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategoryFolders.put("endEpochDay", new TableInfo.Column("endEpochDay", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategoryFolders.put("createdAtEpochMillis", new TableInfo.Column("createdAtEpochMillis", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategoryFolders.put("colorHex", new TableInfo.Column("colorHex", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCategoryFolders = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCategoryFolders = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoCategoryFolders = new TableInfo("category_folders", _columnsCategoryFolders, _foreignKeysCategoryFolders, _indicesCategoryFolders);
        final TableInfo _existingCategoryFolders = TableInfo.read(db, "category_folders");
        if (!_infoCategoryFolders.equals(_existingCategoryFolders)) {
          return new RoomOpenHelper.ValidationResult(false, "category_folders(com.uday.policytracker.data.db.CategoryFolderEntity).\n"
                  + " Expected:\n" + _infoCategoryFolders + "\n"
                  + " Found:\n" + _existingCategoryFolders);
        }
        final HashMap<String, TableInfo.Column> _columnsPolicyFolderCrossRef = new HashMap<String, TableInfo.Column>(2);
        _columnsPolicyFolderCrossRef.put("policyId", new TableInfo.Column("policyId", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPolicyFolderCrossRef.put("folderId", new TableInfo.Column("folderId", "INTEGER", true, 2, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPolicyFolderCrossRef = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPolicyFolderCrossRef = new HashSet<TableInfo.Index>(2);
        _indicesPolicyFolderCrossRef.add(new TableInfo.Index("index_policy_folder_cross_ref_folderId", false, Arrays.asList("folderId"), Arrays.asList("ASC")));
        _indicesPolicyFolderCrossRef.add(new TableInfo.Index("index_policy_folder_cross_ref_policyId", false, Arrays.asList("policyId"), Arrays.asList("ASC")));
        final TableInfo _infoPolicyFolderCrossRef = new TableInfo("policy_folder_cross_ref", _columnsPolicyFolderCrossRef, _foreignKeysPolicyFolderCrossRef, _indicesPolicyFolderCrossRef);
        final TableInfo _existingPolicyFolderCrossRef = TableInfo.read(db, "policy_folder_cross_ref");
        if (!_infoPolicyFolderCrossRef.equals(_existingPolicyFolderCrossRef)) {
          return new RoomOpenHelper.ValidationResult(false, "policy_folder_cross_ref(com.uday.policytracker.data.db.PolicyFolderCrossRef).\n"
                  + " Expected:\n" + _infoPolicyFolderCrossRef + "\n"
                  + " Found:\n" + _existingPolicyFolderCrossRef);
        }
        final HashMap<String, TableInfo.Column> _columnsFolderAttachments = new HashMap<String, TableInfo.Column>(6);
        _columnsFolderAttachments.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFolderAttachments.put("folderId", new TableInfo.Column("folderId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFolderAttachments.put("uri", new TableInfo.Column("uri", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFolderAttachments.put("displayName", new TableInfo.Column("displayName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFolderAttachments.put("mimeType", new TableInfo.Column("mimeType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFolderAttachments.put("addedAtEpochMillis", new TableInfo.Column("addedAtEpochMillis", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysFolderAttachments = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysFolderAttachments.add(new TableInfo.ForeignKey("category_folders", "CASCADE", "NO ACTION", Arrays.asList("folderId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesFolderAttachments = new HashSet<TableInfo.Index>(1);
        _indicesFolderAttachments.add(new TableInfo.Index("index_folder_attachments_folderId", false, Arrays.asList("folderId"), Arrays.asList("ASC")));
        final TableInfo _infoFolderAttachments = new TableInfo("folder_attachments", _columnsFolderAttachments, _foreignKeysFolderAttachments, _indicesFolderAttachments);
        final TableInfo _existingFolderAttachments = TableInfo.read(db, "folder_attachments");
        if (!_infoFolderAttachments.equals(_existingFolderAttachments)) {
          return new RoomOpenHelper.ValidationResult(false, "folder_attachments(com.uday.policytracker.data.db.FolderAttachmentEntity).\n"
                  + " Expected:\n" + _infoFolderAttachments + "\n"
                  + " Found:\n" + _existingFolderAttachments);
        }
        final HashMap<String, TableInfo.Column> _columnsLoans = new HashMap<String, TableInfo.Column>(10);
        _columnsLoans.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLoans.put("loanName", new TableInfo.Column("loanName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLoans.put("lenderName", new TableInfo.Column("lenderName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLoans.put("principalAmount", new TableInfo.Column("principalAmount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLoans.put("annualInterestRate", new TableInfo.Column("annualInterestRate", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLoans.put("tenureMonths", new TableInfo.Column("tenureMonths", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLoans.put("emiAmount", new TableInfo.Column("emiAmount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLoans.put("paymentFrequency", new TableInfo.Column("paymentFrequency", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLoans.put("startDateEpochDay", new TableInfo.Column("startDateEpochDay", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLoans.put("createdAtEpochMillis", new TableInfo.Column("createdAtEpochMillis", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysLoans = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesLoans = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoLoans = new TableInfo("loans", _columnsLoans, _foreignKeysLoans, _indicesLoans);
        final TableInfo _existingLoans = TableInfo.read(db, "loans");
        if (!_infoLoans.equals(_existingLoans)) {
          return new RoomOpenHelper.ValidationResult(false, "loans(com.uday.policytracker.data.db.LoanEntity).\n"
                  + " Expected:\n" + _infoLoans + "\n"
                  + " Found:\n" + _existingLoans);
        }
        final HashMap<String, TableInfo.Column> _columnsLoanPayments = new HashMap<String, TableInfo.Column>(9);
        _columnsLoanPayments.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLoanPayments.put("loanId", new TableInfo.Column("loanId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLoanPayments.put("installmentNumber", new TableInfo.Column("installmentNumber", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLoanPayments.put("dueDateEpochDay", new TableInfo.Column("dueDateEpochDay", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLoanPayments.put("amountDue", new TableInfo.Column("amountDue", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLoanPayments.put("principalComponent", new TableInfo.Column("principalComponent", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLoanPayments.put("interestComponent", new TableInfo.Column("interestComponent", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLoanPayments.put("isPaid", new TableInfo.Column("isPaid", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLoanPayments.put("paidOnEpochDay", new TableInfo.Column("paidOnEpochDay", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysLoanPayments = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysLoanPayments.add(new TableInfo.ForeignKey("loans", "CASCADE", "NO ACTION", Arrays.asList("loanId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesLoanPayments = new HashSet<TableInfo.Index>(1);
        _indicesLoanPayments.add(new TableInfo.Index("index_loan_payments_loanId", false, Arrays.asList("loanId"), Arrays.asList("ASC")));
        final TableInfo _infoLoanPayments = new TableInfo("loan_payments", _columnsLoanPayments, _foreignKeysLoanPayments, _indicesLoanPayments);
        final TableInfo _existingLoanPayments = TableInfo.read(db, "loan_payments");
        if (!_infoLoanPayments.equals(_existingLoanPayments)) {
          return new RoomOpenHelper.ValidationResult(false, "loan_payments(com.uday.policytracker.data.db.LoanPaymentEntity).\n"
                  + " Expected:\n" + _infoLoanPayments + "\n"
                  + " Found:\n" + _existingLoanPayments);
        }
        final HashMap<String, TableInfo.Column> _columnsMoneyLendEntries = new HashMap<String, TableInfo.Column>(10);
        _columnsMoneyLendEntries.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMoneyLendEntries.put("borrowerName", new TableInfo.Column("borrowerName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMoneyLendEntries.put("amount", new TableInfo.Column("amount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMoneyLendEntries.put("interestRate", new TableInfo.Column("interestRate", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMoneyLendEntries.put("startDateEpochDay", new TableInfo.Column("startDateEpochDay", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMoneyLendEntries.put("dueDateEpochDay", new TableInfo.Column("dueDateEpochDay", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMoneyLendEntries.put("notes", new TableInfo.Column("notes", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMoneyLendEntries.put("paidInstallmentsJson", new TableInfo.Column("paidInstallmentsJson", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMoneyLendEntries.put("isRepaid", new TableInfo.Column("isRepaid", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMoneyLendEntries.put("createdAtEpochMillis", new TableInfo.Column("createdAtEpochMillis", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysMoneyLendEntries = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesMoneyLendEntries = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoMoneyLendEntries = new TableInfo("money_lend_entries", _columnsMoneyLendEntries, _foreignKeysMoneyLendEntries, _indicesMoneyLendEntries);
        final TableInfo _existingMoneyLendEntries = TableInfo.read(db, "money_lend_entries");
        if (!_infoMoneyLendEntries.equals(_existingMoneyLendEntries)) {
          return new RoomOpenHelper.ValidationResult(false, "money_lend_entries(com.uday.policytracker.data.db.MoneyLendEntity).\n"
                  + " Expected:\n" + _infoMoneyLendEntries + "\n"
                  + " Found:\n" + _existingMoneyLendEntries);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "67c5ca831d13b3b69f7199a5b0920f6c", "1674950bc3f296e67f21ab9756c2b177");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "policies","policy_history","attachments","future_policies","policy_category_details","category_folders","policy_folder_cross_ref","folder_attachments","loans","loan_payments","money_lend_entries");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    final boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `policies`");
      _db.execSQL("DELETE FROM `policy_history`");
      _db.execSQL("DELETE FROM `attachments`");
      _db.execSQL("DELETE FROM `future_policies`");
      _db.execSQL("DELETE FROM `policy_category_details`");
      _db.execSQL("DELETE FROM `category_folders`");
      _db.execSQL("DELETE FROM `policy_folder_cross_ref`");
      _db.execSQL("DELETE FROM `folder_attachments`");
      _db.execSQL("DELETE FROM `loans`");
      _db.execSQL("DELETE FROM `loan_payments`");
      _db.execSQL("DELETE FROM `money_lend_entries`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(PolicyDao.class, PolicyDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public PolicyDao policyDao() {
    if (_policyDao != null) {
      return _policyDao;
    } else {
      synchronized(this) {
        if(_policyDao == null) {
          _policyDao = new PolicyDao_Impl(this);
        }
        return _policyDao;
      }
    }
  }
}
