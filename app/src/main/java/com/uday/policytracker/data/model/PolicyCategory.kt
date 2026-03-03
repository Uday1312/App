package com.uday.policytracker.data.model

enum class PolicyCategory(val label: String) {
    HEALTH_INSURANCE("Health insurance"),
    TERM_INSURANCE("Term insurance"),
    VEHICLE_INSURANCE("Vehicle insurance"),
    DRIVING_LICENCE("RTA"),
    MEDICAL_RECORDS("Medical records"),
    BANKING("Banking"),
    IDENTITY("Identity"),
    EDUCATION("Education"),
    GENERAL("General"),
    FINANCIALS("Financials")

    ;

    val supportsPolicyRecords: Boolean
        get() = this in listOf(
            HEALTH_INSURANCE,
            TERM_INSURANCE,
            VEHICLE_INSURANCE,
            DRIVING_LICENCE
        )

    val isContainerOnlyService: Boolean
        get() = this in listOf(
            MEDICAL_RECORDS,
            BANKING,
            IDENTITY,
            EDUCATION,
            GENERAL
        )
}
