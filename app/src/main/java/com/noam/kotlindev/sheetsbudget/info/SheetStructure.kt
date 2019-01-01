package com.noam.kotlindev.sheetsbudget.info

import com.google.api.services.sheets.v4.model.BooleanCondition
import com.google.api.services.sheets.v4.model.DataValidationRule

enum class SheetStructure(val sheetHeaderObject: SheetHeaderObject){
    WHO(SheetHeaderObject(
        "מי:",
        0, 0,
        DataValidationRule().apply{
            condition = BooleanCondition().apply {
                type = "ONE_OF_LIST"
                setValues(kotlin.collections.mutableListOf(
                    com.google.api.services.sheets.v4.model.ConditionValue().apply {
                        userEnteredValue = "נועם"
                    },
                    com.google.api.services.sheets.v4.model.ConditionValue().apply {
                        userEnteredValue = "גל"
                    }
                ))
            }
            strict = true
            showCustomUi = true
        })),
    DATE(
        SheetHeaderObject(
            "תאריך:",
            1, 0,
            DataValidationRule().apply {
                condition = com.google.api.services.sheets.v4.model.BooleanCondition().setType("DATE_IS_VALID")
                strict = true
                showCustomUi = true
            }
        )),
    DESCRIPTION(SheetHeaderObject("פירוט:", 2, 0, null )),
    AMOUNT(SheetHeaderObject("סכום:", 3, 0, null))
}
