package com.noam.kotlindev.sheetsbudget

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.sheets.v4.SheetsScopes
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import org.jetbrains.anko.toast
import java.util.*

class SpreadsheetInitActivity : AppCompatActivity() {
    internal lateinit var mCredential: GoogleAccountCredential
    private lateinit var mHandlerThread: HandlerThread
    private lateinit var handler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spreadsheet_init)

        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
            applicationContext, Arrays.asList(*SCOPES))
            .setBackOff(ExponentialBackOff())

        mHandlerThread = HandlerThread("SheetsRequests")
        mHandlerThread.start()
        handler = Handler(mHandlerThread.looper)

    }

    private fun getResultsFromApi() {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices()
        } else if (mCredential.selectedAccountName == null) {
            chooseAccount()
        } else if (!isDeviceOnline()) {
            toast("No network connection available.")
        } else {
            MakeRequestTask(mCredential).execute()
        }
    }
    private fun isGooglePlayServicesAvailable(): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this)
        return connectionStatusCode == ConnectionResult.SUCCESS
    }

    private fun chooseAccount() = runWithPermissions(Manifest.permission.GET_ACCOUNTS){
        val accountName = getPreferences(Context.MODE_PRIVATE)
            .getString(PREF_ACCOUNT_NAME, null)
        if (accountName != null) {
            mCredential.selectedAccountName = accountName
            getResultsFromApi()
        } else {
            // Start a dialog from which the user can choose an account
            startActivityForResult(
                mCredential.newChooseAccountIntent(),
                REQUEST_ACCOUNT_PICKER
            )
        }
    }
    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private fun acquireGooglePlayServices() {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this)
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode)
        }
    }

    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     * Google Play Services on this device.
     */
    internal fun showGooglePlayServicesAvailabilityErrorDialog(
        connectionStatusCode: Int
    ) {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val dialog = apiAvailability.getErrorDialog(
            this@SpreadsheetInitActivity,
            connectionStatusCode,
            REQUEST_GOOGLE_PLAY_SERVICES
        )
        dialog.show()
    }

    /**
     * Checks whether the device currently has a network connection.
     * @return true if the device has a network connection, false otherwise.
     */
    private fun isDeviceOnline(): Boolean {
        val connMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connMgr.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    companion object {
        private val SCOPES = arrayOf(SheetsScopes.SPREADSHEETS_READONLY)
        internal const val REQUEST_ACCOUNT_PICKER = 1000
        internal const val REQUEST_GOOGLE_PLAY_SERVICES = 1002
        private const val PREF_ACCOUNT_NAME = "accountName"


    }
}
