package com.noam.kotlindev.sheetsbudget.sheetsAPI

import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.*
import com.noam.kotlindev.sheetsbudget.info.SheetStructure

class SheetAddSheetRequest(private val sheet: String, private  val  spreadsheetId: String): SheetRequestInterface {

    private var sheetID: Int = 0
    override fun executeRequest(sheetApiService: Sheets): List<List<String>>? {

        createNewSheet(sheetApiService)
        setSheetHeader(sheetApiService)
        setDataValidation(sheetApiService)
        return null
    }

    private fun createNewSheet(sheetApiService: Sheets){
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

        val newAddRequest = Request()
        newAddRequest.addSheet = addSheetRequest
        val batchUpdateSpreadsheetRequest = BatchUpdateSpreadsheetRequest().setRequests(arrayListOf(newAddRequest))
        val replies = sheetApiService.spreadsheets().batchUpdate(spreadsheetId, batchUpdateSpreadsheetRequest).execute()
        val addSheetResponse = replies.replies[0].addSheet
        sheetID = addSheetResponse.properties.sheetId
    }

    private fun setSheetHeader(sheetApiService: Sheets){
        val content = arrayListOf("מי:", "תאריך:", "פירוט:", "סכום:","", "סה\"כ", "הפרש:")
        val requestRange = "$sheet!A1:H1"
        val requestBody = ValueRange().apply {
            range = requestRange
            majorDimension = "ROWS"
            setValues(listOf(content))
        }

        sheetApiService.spreadsheets().values().update(spreadsheetId, requestRange,
            requestBody).apply {
            valueInputOption = "USER_ENTERED"
        }.execute()
    }

    private fun setDataValidation(sheetApiService: Sheets){
        sheetApiService.Spreadsheets().batchUpdate(
            spreadsheetId,
            BatchUpdateSpreadsheetRequest().apply {
                requests = arrayListOf(
                    Request().apply {
                        setDataValidation = SetDataValidationRequest().apply {
                            rule = SheetStructure.WHO.sheetHeaderObject.validationRule
                            range = GridRange().apply {
                                sheetId = sheetID
                                startRowIndex = SheetStructure.WHO.sheetHeaderObject.row.plus(1)
                                endRowIndex = 63
                                startColumnIndex = SheetStructure.WHO.sheetHeaderObject.column
                                endColumnIndex = SheetStructure.WHO.sheetHeaderObject.column.plus(1)
                            }

                        }
                    }
                )
            }
        ).execute()
    }
}


