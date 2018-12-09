package com.noam.kotlindev.sheetsbudget

import android.util.Log
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.sheets.v4.model.ValueRange
import org.jetbrains.anko.getStackTraceString
import java.util.*

class SheetPost (credential: GoogleAccountCredential, private val range: String, private  val  spreadsheetId: String,
                 private val content: String, private val onRequestResultListener: OnPostSuccess
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
            getDataFromApi()
            onRequestResultListener.onPostSuccess("Success!")
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

    private fun getDataFromApi(){
        val calendar = Calendar.getInstance()
        val requestBody = ValueRange().apply {
            range = "11/18!A7:C7"
            majorDimension = "ROWS"
            setValues(listOf(mutableListOf("", "stam stam", "3123")))
//            setValues(listOf(mutableListOf("${calendar.get(Calendar.DAY_OF_MONTH)}" +
//                    "/${calendar.get(Calendar.MONTH)}/${calendar.get(Calendar.YEAR)}", "stam stam", "3123")))
        }

        this.mService!!.spreadsheets().values().update(spreadsheetId, "11/18!A7:C7", requestBody).apply {
            valueInputOption = "USER_ENTERED"
        }.execute()
    }
    interface OnPostSuccess{
        fun onPostSuccess(message: String)
        fun onPostFailed(error: Exception)
    }
    companion object {
        const val TAG = "SheetRequest"
    }
}