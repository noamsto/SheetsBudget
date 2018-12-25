package com.noam.kotlindev.sheetsbudget.info

import com.google.api.services.sheets.v4.model.BooleanCondition
import com.google.api.services.sheets.v4.model.ConditionValue
import com.google.api.services.sheets.v4.model.DataValidationRule

enum class SheetStructure(val sheetHeaderObject: SheetHeaderObject){
    WHO(SheetHeaderObject(
        "מי:",
        0, 0,
        DataValidationRule().setCondition(BooleanCondition().setValues(
                mutableListOf(ConditionValue().apply {
                    userEnteredValue="נועם:"
                }, ConditionValue().apply {
                    userEnteredValue="גל:"
                }))))),
    DATE(
        SheetHeaderObject(
        "תאריך:",
        1, 0,
        DataValidationRule().setCondition(BooleanCondition().setType("dd/mm/yy")
        ))),
    DESCRIPTION(SheetHeaderObject("פירוט:", 2, 0, null )),
    AMOUNT(SheetHeaderObject("סכום:", 3, 0, null))
}