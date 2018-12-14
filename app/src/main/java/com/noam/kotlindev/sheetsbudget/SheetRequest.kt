package com.noam.kotlindev.sheetsbudget

import android.util.Log
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.json.jackson2.JacksonFactory
import com.noam.kotlindev.sheetsbudget.constants.Range

class SheetRequest(private val credential: GoogleAccountCredential, private val sheet: String, private  val  spreadsheetId: String,
                   private val onRequestResultListener: OnRequestResultListener) : Runnable {

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
            onRequestResultListener.onResultSuccess(getDataFromApi())
        }catch (googleJsonResponseException: GoogleJsonResponseException){
            Log.e(TAG, googleJsonResponseException.message)
            onRequestResultListener.onResultFailed(googleJsonResponseException)
        }catch(userRecoverableAuthIOException :UserRecoverableAuthIOException) {
            onRequestResultListener.onResultFailed(userRecoverableAuthIOException)
        }catch(exception :java.lang.Exception) {
            Log.e(TAG, exception.message)
            onRequestResultListener.onResultFailed(exception)
        }
    }

    private fun getDataFromApi(): List<List<String>> {
        val requestRange = "$sheet!${Range.START.range}:${Range.END.range}"
        val response = this.mService!!.spreadsheets().values()
            .get(spreadsheetId, requestRange)
            .execute()
        val values = response.getValues()
        values ?: throw Exception("Failed to get values from spreadsheet.")
        return values.map { row ->
            row.map { cell -> cell.toString().trimStart().trimEnd() }
        }
    }

    interface OnRequestResultListener{
        fun onResultSuccess(list: List<List<String>>)
        fun onResultFailed(error: java.lang.Exception)
    }

    companion object {
        const val TAG = "SheetRequest"
    }
}