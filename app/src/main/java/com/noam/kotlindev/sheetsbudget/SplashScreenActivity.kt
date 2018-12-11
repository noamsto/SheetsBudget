package com.noam.kotlindev.sheetsbudget

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.sheets.v4.SheetsScopes
import java.util.*

class SplashScreenActivity : AppCompatActivity() {
    private lateinit var mCredential: GoogleAccountCredential

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)


        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
            applicationContext, Arrays.asList(*SCOPES)
        )
            .setBackOff(ExponentialBackOff())

        if (mCredential.selectedAccountName == null) {
        }
    }
    companion object {
        private val SCOPES = arrayOf(SheetsScopes.SPREADSHEETS)

    }
}
