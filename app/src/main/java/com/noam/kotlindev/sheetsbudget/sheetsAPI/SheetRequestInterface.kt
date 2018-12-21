package com.noam.kotlindev.sheetsbudget.sheetsAPI

interface SheetRequestInterface {
    fun executeRequest(sheetApiService: com.google.api.services.sheets.v4.Sheets): List<List<String>>?
}