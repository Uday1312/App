package com.uday.policytracker.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        PolicyEntity::class,
        PolicyHistoryEntity::class,
        AttachmentEntity::class,
        FuturePolicyEntity::class,
        PolicyCategoryDetailsEntity::class,
        CategoryFolderEntity::class,
        PolicyFolderCrossRef::class,
        FolderAttachmentEntity::class,
        LoanEntity::class,
        LoanPaymentEntity::class,
        MoneyLendEntity::class
    ],
    version = 13,
    exportSchema = false
)
abstract class PolicyDatabase : RoomDatabase() {
    abstract fun policyDao(): PolicyDao

    companion object {
        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE policies ADD COLUMN policyHolderName TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE policy_history ADD COLUMN policyHolderName TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE future_policies ADD COLUMN policyHolderName TEXT NOT NULL DEFAULT ''")
            }
        }
        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS policy_category_details (
                        policyId INTEGER NOT NULL PRIMARY KEY,
                        premiumFrequency TEXT NOT NULL DEFAULT '',
                        premiumDueDayOfMonth INTEGER,
                        coverageAmount REAL,
                        policyTermYears INTEGER,
                        entryAge INTEGER,
                        coverageTillAge INTEGER,
                        nomineeName TEXT NOT NULL DEFAULT '',
                        nomineeRelationship TEXT NOT NULL DEFAULT '',
                        riderAddons TEXT NOT NULL DEFAULT '',
                        paymentMode TEXT NOT NULL DEFAULT '',
                        gracePeriodDays INTEGER,
                        termPolicyStatus TEXT NOT NULL DEFAULT '',
                        totalPayments INTEGER,
                        paidPayments INTEGER,
                        vehicleNumber TEXT NOT NULL DEFAULT '',
                        vehicleType TEXT NOT NULL DEFAULT '',
                        makeModelVariant TEXT NOT NULL DEFAULT '',
                        fuelType TEXT NOT NULL DEFAULT '',
                        vehiclePolicyType TEXT NOT NULL DEFAULT '',
                        vehicleAddons TEXT NOT NULL DEFAULT '',
                        claimHistory TEXT NOT NULL DEFAULT '',
                        documentType TEXT NOT NULL DEFAULT '',
                        issuingRto TEXT NOT NULL DEFAULT '',
                        stateName TEXT NOT NULL DEFAULT '',
                        vehicleClass TEXT NOT NULL DEFAULT '',
                        ownerName TEXT NOT NULL DEFAULT '',
                        linkedVehicleNumber TEXT NOT NULL DEFAULT '',
                        dateOfBirth TEXT NOT NULL DEFAULT '',
                        FOREIGN KEY(policyId) REFERENCES policies(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_policy_category_details_policyId ON policy_category_details(policyId)")
            }
        }
        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE policy_category_details ADD COLUMN coverageAmountUnit TEXT NOT NULL DEFAULT ''")
            }
        }
        private val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE policy_category_details ADD COLUMN premiumPaymentStartEpochDay INTEGER")
                db.execSQL("ALTER TABLE policy_category_details ADD COLUMN premiumPaymentEndEpochDay INTEGER")
                db.execSQL("ALTER TABLE policy_category_details ADD COLUMN premiumPaymentTermYears INTEGER")
                db.execSQL("ALTER TABLE policy_category_details ADD COLUMN policyValidityEndEpochDay INTEGER")
                db.execSQL("ALTER TABLE policy_category_details ADD COLUMN customFieldValuesJson TEXT NOT NULL DEFAULT ''")
            }
        }
        private val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE loans ADD COLUMN paymentFrequency TEXT NOT NULL DEFAULT 'Monthly'")
            }
        }
        private val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS money_lend_entries (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        borrowerName TEXT NOT NULL,
                        amount REAL NOT NULL,
                        interestRate REAL NOT NULL,
                        startDateEpochDay INTEGER NOT NULL,
                        dueDateEpochDay INTEGER,
                        notes TEXT NOT NULL,
                        paidInstallmentsJson TEXT NOT NULL,
                        isRepaid INTEGER NOT NULL,
                        createdAtEpochMillis INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        @Volatile
        private var INSTANCE: PolicyDatabase? = null

        fun getInstance(context: Context): PolicyDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    PolicyDatabase::class.java,
                    "policy_tracker.db"
                )
                    .addMigrations(MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13)
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
