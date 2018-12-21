package com.noam.kotlindev.sheetsbudget.sheetsAPI

import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest
import com.google.api.services.sheets.v4.model.DeleteRangeRequest
import com.google.api.services.sheets.v4.model.GridRange
import com.google.api.services.sheets.v4.model.Request

class SheetDeleteRangeRequest(private val sheet: String, private  val  spreadsheetId: String,
                              private val rows: List<Int>): SheetRequestInterface {

    override fun executeRequest(sheetApiService: Sheets): List<List<String>>? {
        val requestSheetID = sheetApiService.spreadsheets().get(spreadsheetId)
        requestSheetID.apply {
            ranges = (mutableListOf(sheet))
        }
        val responseSheetID = requestSheetID.execute()
        val sheetId: Int = responseSheetID.sheets[0].properties.sheetId

        val deleteRequestList = ArrayList<Request>()

        rows.forEach {row ->
            val gridRange = GridRange().apply {
                this.sheetId = sheetId
                startRowIndex = row
                endRowIndex = row.plus(1)
                startColumnIndex = 0
                endColumnIndex = 4
            }
            val deleteRangeRequest = DeleteRangeRequest().apply {
                range = gridRange
                shiftDimension = "ROWS"
            }
            val deleteRowRequest = Request().setDeleteRange(deleteRangeRequest)
            deleteRequestList.add(deleteRowRequest)
        }


        val batchUpdateSpreadsheetRequest = BatchUpdateSpreadsheetRequest().setRequests(deleteRequestList)
        sheetApiService.spreadsheets().batchUpdate(spreadsheetId, batchUpdateSpreadsheetRequest).execute()
        return null
    }
}
