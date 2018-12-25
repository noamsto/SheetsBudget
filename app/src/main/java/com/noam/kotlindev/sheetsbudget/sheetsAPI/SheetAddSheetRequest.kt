package com.noam.kotlindev.sheetsbudget.sheetsAPI

import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.*

class SheetAddSheetRequest(private val sheet: String, private  val  spreadsheetId: String): SheetRequestInterface {

    override fun executeRequest(sheetApiService: Sheets): List<List<String>>? {

        val addSheetRequest = AddSheetRequest()
        val sheetProperties = SheetProperties()
        val gridProperties = GridProperties()

        gridProperties.apply {
            columnCount = 10
            rowCount = 62
        }
        sheetProperties.apply {
            title = sheet
            rightToLeft = true
            hidden = false
            setGridProperties(gridProperties)
        }
        addSheetRequest.properties = sheetProperties

        val newRequest = Request()
        newRequest.addSheet = addSheetRequest
        val batchUpdateSpreadsheetRequest = BatchUpdateSpreadsheetRequest().setRequests(arrayListOf(newRequest))
        sheetApiService.spreadsheets().batchUpdate(spreadsheetId, batchUpdateSpreadsheetRequest).execute()
        return null
    }
}
