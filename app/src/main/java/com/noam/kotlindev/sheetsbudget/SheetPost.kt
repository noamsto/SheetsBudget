package com.noam.kotlindev.sheetsbudget

import android.util.Log
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.sheets.v4.model.ValueRange
import com.noam.kotlindev.sheetsbudget.constants.AccountInfo
import com.noam.kotlindev.sheetsbudget.constants.Range
import com.noam.kotlindev.sheetsbudget.info.ExpenseEntry
import org.jetbrains.anko.getStackTraceString

class SheetPost (
    private val credential: GoogleAccountCredential, private  val  spreadsheetId: String,
    private val expenseEntry: ExpenseEntry, private val onRequestResultListener: OnPostSuccess
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
            onRequestResultListener.onPostSuccess(getDataFromApi())
        } catch (googleJsonResponseException: GoogleJsonResponseException) {
            Log.e(TAG, googleJsonResponseException.message)
            onRequestResultListener.onPostFailed(googleJsonResponseException)
        } catch (userRecoverableAuthIOException: UserRecoverableAuthIOException) {
            onRequestResultListener.onPostFailed(userRecoverableAuthIOException)
        } catch (exception: Exception) {
            Log.e(TAG, exception.getStackTraceString())
            onRequestResultListener.onPostFailed(exception)
        }
    }

    private fun getDataFromApi(): List<List<String>> {
        val name = credential.selectedAccountName
        val accountInfo = if (name == AccountInfo.NOAM_ACCOUNT.email){
            AccountInfo.NOAM_ACCOUNT
        }else{
            AccountInfo.GAL_ACCOUNT
        }
        val content = expenseEntry.getValues()
        val sheet = expenseEntry.date.removeRange(0, expenseEntry.date.indexOf("/").plus(1))
        val requestRange = "$sheet!A${expenseEntry.row}:" +"D${expenseEntry.row}"
//        val requestRange = if (name == AccountInfo.NOAM_ACCOUNT.email){l
//            content.addAll(0, listOf("","",""))
//            "$range!${Range.NOAM_RANGE.range}"
//        }else{
//            "$range!${Range.GAL_RANGE.range}"
//        }
        val requestBody = ValueRange().apply {
            range = requestRange
            majorDimension = "ROWS"
            setValues(listOf(content))
        }
        val response = this.mService!!.spreadsheets().values().update(spreadsheetId, requestRange,
            requestBody).apply {
            valueInputOption = "USER_ENTERED"
            includeValuesInResponse = true
        }.execute()
        response ?: throw Exception("Failed to get values from spreadsheet.")
        val updatedValues = response.updatedData.getValues()
        return updatedValues.map { row ->
            row.map { cell -> cell.toString().trimStart().trimEnd() }
        }

    }
    interface OnPostSuccess{
        fun onPostSuccess(list: List<List<String>>)
        fun onPostFailed(error: java.lang.Exception)
    }

        companion object {
        const val TAG = "SheetRequest"
    }
}