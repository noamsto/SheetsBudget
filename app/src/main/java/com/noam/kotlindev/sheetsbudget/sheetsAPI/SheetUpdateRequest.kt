package com.noam.kotlindev.sheetsbudget.sheetsAPI

import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.ValueRange
import com.noam.kotlindev.sheetsbudget.info.ExpenseEntry

class SheetUpdateRequest (private  val  spreadsheetId: String, private val sheet: String, private val expenseEntry: ExpenseEntry
) : SheetRequestInterface {

    override fun executeRequest(sheetApiService: Sheets): List<List<String>>? {
        val content = expenseEntry.getValues()
        val requestRange = "$sheet!A${expenseEntry.row.plus(1)}:" +"D${expenseEntry.row.plus(1)}"
        val requestBody = ValueRange().apply {
            range = requestRange
            majorDimension = "ROWS"
            setValues(listOf(content))
        }
        val response = sheetApiService.spreadsheets().values().update(spreadsheetId, requestRange,
            requestBody).apply {
            valueInputOption = "USER_ENTERED"
            includeValuesInResponse = true
        }.execute()
        response ?: throw Exception("Failed to get values from spreadsheet.")
        val updatedValues = response.updatedData.getValues()
        updatedValues ?: return mutableListOf()
        return updatedValues.map { row ->
            row.map { cell -> cell.toString().trimStart().trimEnd()}
        }
    }
        companion object {
        const val TAG = "SheetGetRequest"
    }
}