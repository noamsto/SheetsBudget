package com.noam.kotlindev.sheetsbudget.sheetsAPI

import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.*
import com.noam.kotlindev.sheetsbudget.info.SheetStructure

class SheetAddSheetRequest(private val sheet: String, private  val  spreadsheetId: String): SheetRequestInterface {

    private var sheetID: Int = 0
    override fun executeRequest(sheetApiService: Sheets): List<List<String>>? {

        createNewSheet(sheetApiService)
        setSheetHeader(sheetApiService)
        formatSheet(sheetApiService)
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
        val content = listOf(
            arrayListOf("מי:", "תאריך:", "סכום:", "פירוט:", "", "סה\"כ"),
            arrayListOf("", "", "", "", "", "גל", ""),
            arrayListOf("", "", "", "", "", "נועם", ""),
            arrayListOf("", "", "", "", "", "ביחד", ""))
        val requestRange = "$sheet!A1:G4"
        val requestBody = ValueRange().apply {
            range = requestRange
            majorDimension = "ROWS"
            setValues(content)
        }

        sheetApiService.spreadsheets().values().update(spreadsheetId, requestRange,
            requestBody).apply {
            valueInputOption = "USER_ENTERED"
        }.execute()
    }

    private fun formatSheet(sheetApiService: Sheets){

        val whoDataValidation = Request().apply {
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

        val dateDataValidation = Request().apply {
            setDataValidation = SetDataValidationRequest().apply {
                rule = SheetStructure.DATE.sheetHeaderObject.validationRule
                range = GridRange().apply {
                    sheetId = sheetID
                    startRowIndex = SheetStructure.DATE.sheetHeaderObject.row.plus(1)
                    endRowIndex = 63
                    startColumnIndex = SheetStructure.DATE.sheetHeaderObject.column
                    endColumnIndex = SheetStructure.DATE.sheetHeaderObject.column.plus(1)
                }
            }
        }

        val amountFormat = Request().apply {
            repeatCell = RepeatCellRequest().apply {
                range = GridRange().apply {
                    sheetId = sheetID
                    startRowIndex = SheetStructure.AMOUNT.sheetHeaderObject.row.plus(1)
                    endRowIndex = 63
                    startColumnIndex = SheetStructure.AMOUNT.sheetHeaderObject.column
                    endColumnIndex = SheetStructure.AMOUNT.sheetHeaderObject.column.plus(1)
                }
                cell = CellData().apply {
                    userEnteredFormat = CellFormat().apply {
                        numberFormat = NumberFormat().apply {
                            type = "CURRENCY"
                            pattern = "\"₪\"#,##0"
                        }
                    }
                }
                fields = "userEnteredFormat.numberFormat"
            }
        }

        val mergeSummary =  Request().apply {
            mergeCells = MergeCellsRequest().apply {
                range = GridRange().apply {
                    sheetId = sheetID
                    startRowIndex = SheetStructure.SUMMARY.sheetHeaderObject.row
                    endRowIndex = SheetStructure.SUMMARY.sheetHeaderObject.row.plus(1)
                    startColumnIndex = SheetStructure.SUMMARY.sheetHeaderObject.column
                    endColumnIndex = SheetStructure.SUMMARY.sheetHeaderObject.column.plus(2)
                }
                mergeType = "MERGE_ALL"
            }
        }

        val summaryCurrencyCol = Request().apply {
            repeatCell = RepeatCellRequest().apply {
                range = GridRange().apply {
                    sheetId = sheetID
                    startRowIndex = SheetStructure.SUMMARY.sheetHeaderObject.row.plus(1)
                    endRowIndex = SheetStructure.SUMMARY.sheetHeaderObject.row.plus(3)
                    startColumnIndex = SheetStructure.SUMMARY.sheetHeaderObject.column.plus(1)
                    endColumnIndex = SheetStructure.SUMMARY.sheetHeaderObject.column.plus(2)
                }
                cell = CellData().apply {
                    userEnteredFormat = CellFormat().apply {
                        numberFormat = NumberFormat().apply {
                            type = "CURRENCY"
                            pattern = "\"₪\"#,##0"
                        }
                    }
                    userEnteredValue = ExtendedValue().apply {
                        formulaValue = "=SUMIF(A:A, TO_TEXT(F2), D:D)"
                    }
                }
                fields = "userEnteredFormat.numberFormat, userEnteredValue.formulaValue"
            }
        }

        val togetherFormula = Request().apply {
            updateCells = UpdateCellsRequest().apply {
                rows = listOf(RowData().apply {
                    setValues(listOf(CellData().apply {
                        userEnteredFormat = CellFormat().apply {
                            numberFormat = NumberFormat().apply {
                                type = "CURRENCY"
                                pattern = "\"₪\"#,##0"
                            }
                        }
                        userEnteredValue = ExtendedValue().apply {
                            formulaValue = "=SUM(G2:G3)"
                        }
                    }))
                    start = GridCoordinate().apply {
                        sheetId = sheetID
                        rowIndex = SheetStructure.SUMMARY.sheetHeaderObject.row.plus(3)
                        columnIndex = SheetStructure.SUMMARY.sheetHeaderObject.column.plus(1)
                    }
                    fields = "userEnteredFormat.numberFormat, userEnteredValue.formulaValue"
                })
            }

        }

        val boldHeader = Request().apply {
            repeatCell = RepeatCellRequest().apply {
                range = GridRange().apply {
                    sheetId = sheetID
                    startRowIndex = SheetStructure.WHO.sheetHeaderObject.row
                    endRowIndex = SheetStructure.WHO.sheetHeaderObject.row.plus(1)
                    startColumnIndex = SheetStructure.WHO.sheetHeaderObject.column
                    endColumnIndex = SheetStructure.DIFF.sheetHeaderObject.column.plus(1)
                }
                cell = CellData().apply {
                    userEnteredFormat = CellFormat().apply {
                        textFormat = TextFormat().apply {
                            bold = true
                        }
                        horizontalAlignment = "CENTER"
                    }
                }
                fields = "userEnteredFormat(textFormat,horizontalAlignment)"
            }
        }

        sheetApiService.Spreadsheets().batchUpdate(
            spreadsheetId,
            BatchUpdateSpreadsheetRequest().apply {
                requests = arrayListOf(whoDataValidation, dateDataValidation, amountFormat, mergeSummary,
                    summaryCurrencyCol, boldHeader, togetherFormula
                )
            }
        ).execute()
    }

}


