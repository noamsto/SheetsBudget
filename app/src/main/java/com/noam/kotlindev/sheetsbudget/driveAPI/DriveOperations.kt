package com.noam.kotlindev.sheetsbudget.driveAPI

import android.content.Context
import android.util.Log
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.noam.kotlindev.sheetsbudget.R


class DriveOperations(context: Context, credential: GoogleAccountCredential) {


    private var driveAPI: Drive? = null

    init {
        val transport = AndroidHttp.newCompatibleTransport()
        val jsonFactory = JacksonFactory.getDefaultInstance()
        driveAPI = Drive.Builder(
            transport, jsonFactory, credential
        ).setApplicationName(context.getString(R.string.app_name)).build()
    }


    fun listSpreadSheets() {
        var pageToken: String? = null
        do {
            val result = driveAPI!!.files().list().apply{
                q = "mimeType='application/vnd.google-apps.spreadsheet'"
                spaces = "drive"
                fields = "nextPageToken, files(id, name)"
                this.pageToken = pageToken
            }.execute()
            for (file in result.files) {
                Log.d(TAG, "Found file: ${file.name}, ${file.id}")
            }
            pageToken = result.nextPageToken
        } while (pageToken != null)
    }

    companion object {
        private const val TAG = "DriveOperations"
    }

}
