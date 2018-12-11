package com.noam.kotlindev.sheetsbudget

import android.util.Log
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.sheets.v4.model.ValueRange
import com.noam.kotlindev.sheetsbudget.constants.AccountNames
import com.noam.kotlindev.sheetsbudget.constants.Ranges
import org.jetbrains.anko.getStackTraceString
import java.util.*

class SheetPost (
    private val credential: GoogleAccountCredential, private val range: String, private  val  spreadsheetId: String,
    private val content: MutableList<String>, private val onRequestResultListener: OnPostSuccess
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
        val name = credential.selectedAccountName
        val requestRange = if (name == AccountNames.NOAM_ACCOUNT.email){
            content.addAll(0, listOf("","",""))
            "$range!${Ranges.NOAM_RANGE.range}"
        }else{
            "$range!${Ranges.GAL_RANGE.range}"
        }
        val requestBody = ValueRange().apply {
            range = requestRange
            majorDimension = "ROWS"
            setValues(listOf(content))
        }
        this.mService!!.spreadsheets().values().append(spreadsheetId, requestRange, requestBody).apply {
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