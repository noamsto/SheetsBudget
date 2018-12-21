package com.noam.kotlindev.sheetsbudget.sheetsAPI

import android.content.Context
import android.util.Log
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.json.jackson2.JacksonFactory
import com.noam.kotlindev.sheetsbudget.R.string.app_name
import com.noam.kotlindev.sheetsbudget.sheetsAPIrequestsHandlerThread.SheetGetRequest

class SheetRequestRunnerBuilder(context: Context, credential: GoogleAccountCredential,
                                private val onRequestResultListener: OnRequestResultListener)  {


    private var sheetApiService: com.google.api.services.sheets.v4.Sheets? = null

    init {
        val transport = AndroidHttp.newCompatibleTransport()
        val jsonFactory = JacksonFactory.getDefaultInstance()
        sheetApiService = com.google.api.services.sheets.v4.Sheets.Builder(
            transport, jsonFactory, credential
        )
            .setApplicationName(context.getString(app_name)).build()
    }

    fun buildRequest(sheetUpdateGetRequest: SheetUpdateGetRequestInterface): SheetRequestRunner {
        return SheetRequestRunner(sheetApiService!!, sheetUpdateGetRequest, onRequestResultListener)
    }

    companion object {
        private const val TAG = "SheetRequestRunnerBuilder"
        const val UPDATE_RESULT = 1000
        const val GET_RESULT = 1001
    }

    interface OnRequestResultListener{
        fun onRequestSuccess(list: List<List<String>>, resultCode : Int)
        fun onRequestFailed(error: java.lang.Exception)
    }

    class SheetRequestRunner( private val sheetApiService: com.google.api.services.sheets.v4.Sheets,
                              private val sheetUpdateGetRequest: SheetUpdateGetRequestInterface,
                              private val onRequestResultListener: OnRequestResultListener):Runnable{
        override fun run() {
            Log.d(TAG, "Executing sheet request.")
            val resultCode = if (sheetUpdateGetRequest is SheetGetRequest)
                GET_RESULT
            else
                UPDATE_RESULT

            try {
                onRequestResultListener.onRequestSuccess(sheetUpdateGetRequest.executeRequest(sheetApiService),
                    resultCode)
            }catch (googleJsonResponseException: GoogleJsonResponseException){
                Log.e(TAG, googleJsonResponseException.message)
                onRequestResultListener.onRequestFailed(googleJsonResponseException)
            }catch(userRecoverableAuthIOException : UserRecoverableAuthIOException) {
                onRequestResultListener.onRequestFailed(userRecoverableAuthIOException)
            }catch(exception :java.lang.Exception) {
                Log.e(TAG, exception.message)
                onRequestResultListener.onRequestFailed(exception)
            }
        }
        companion object {
            private const val TAG = "SheetRequestRunner"
        }

    }
}