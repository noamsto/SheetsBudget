package com.noam.kotlindev.sheetsbudget.constants

import com.google.api.services.drive.DriveScopes
import com.google.api.services.sheets.v4.SheetsScopes

class SpreadSheetInfo {
    companion object {
        const val ID = "1Q3VO5VLAIKi2uyhc7HIH-AOOt5FRujTRkh6D8QJ5IYE"
        val SCOPES = arrayOf(SheetsScopes.SPREADSHEETS, DriveScopes.DRIVE_METADATA_READONLY)
    }
}