package com.noam.kotlindev.sheetsbudget

import android.util.Log
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.*
import org.jetbrains.anko.getStackTraceString

class SheetDeleteRow (credential: GoogleAccountCredential, private  val  spreadsheetId: String, private val sheet: String,
                      private val row: Int, private val onDeleteListener: OnDeleteListener
) : Runnable {

    private var mService: com.google.api.services.sheets.v4.Sheets? = null

    init {
        val transport = AndroidHttp.newCompatibleTransport()
        val jsonFactory = JacksonFactory.getDefaultInstance()
        mService = com.google.api.services.sheets.v4.Sheets.Builder(
            transport, jsonFactory, credential
        )
            .setApplicationName("SheetBudget")
            .build()
    }

    override fun run() {
        try {
            deleteRow()
            onDeleteListener.onDeleteSuccess()
        } catch (googleJsonResponseException: GoogleJsonResponseException) {
            Log.e(TAG, googleJsonResponseException.message)
            onDeleteListener.onDeleteFailed(googleJsonResponseException)
        } catch (userRecoverableAuthIOException: UserRecoverableAuthIOException) {
            onDeleteListener.onDeleteFailed(userRecoverableAuthIOException)
        } catch (exception: Exception) {
            Log.e(TAG, exception.getStackTraceString())
            onDeleteListener.onDeleteFailed(exception)
        }
    }

    private fun deleteRow(){


        val requestSheetID = mService!!.spreadsheets().get(spreadsheetId)
        requestSheetID.apply {
            ranges = (mutableListOf(sheet))
        }
        val responseSheetID = requestSheetID.execute()
        val sheetId: Int = responseSheetID.sheets[0].properties.sheetId


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

        val batchUpdateSpreadsheetRequest = BatchUpdateSpreadsheetRequest()
            .setRequests(mutableListOf(deleteRowRequest))
        this.mService!!.spreadsheets().batchUpdate(spreadsheetId,
            batchUpdateSpreadsheetRequest).execute()
    }

    interface OnDeleteListener{
        fun onDeleteSuccess()
        fun onDeleteFailed(error: java.lang.Exception)
    }

    companion object {
        const val TAG = "SheetRequest"
    }
}