package com.noam.kotlindev.sheetsbudget.sheetsAPIrequestsHandlerThread
import com.noam.kotlindev.sheetsbudget.constants.Range
import com.noam.kotlindev.sheetsbudget.sheetsAPI.SheetRequestInterface

class SheetGetRequest(private val sheet: String, private  val  spreadsheetId: String):
    SheetRequestInterface {


    override fun executeRequest(sheetApiService: com.google.api.services.sheets.v4.Sheets): List<List<String>>? {
        val requestRange = "$sheet!${Range.START.range}:${Range.END.range}"
        val response = sheetApiService.spreadsheets().values()
            .get(spreadsheetId, requestRange)
            .execute()
        val values = response.getValues()
        values ?: throw Exception("Failed to get values from spreadsheet.")
        return values.map { row ->
            row.map { cell -> cell.toString().trimStart().trimEnd() }
        }
    }

    companion object {
        const val TAG = "SheetGetRequest"
    }
}