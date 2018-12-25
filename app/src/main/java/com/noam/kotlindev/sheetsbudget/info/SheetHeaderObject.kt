package com.noam.kotlindev.sheetsbudget.info

import com.google.api.services.sheets.v4.model.DataValidationRule

class SheetHeaderObject(val name: String, val column: Int, val row: Int, val validationRule: DataValidationRule?)