package com.noam.kotlindev.sheetsbudget

import android.Manifest
import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.support.v4.app.ActivityCompat.startActivityForResult
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.sheets.v4.SheetsScopes
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import org.jetbrains.anko.toast
import java.util.*

class ServicesOperations(private val activity: Activity) {


    private var accountEmail: String? = null
    val accountCredential = GoogleAccountCredential.usingOAuth2(
        activity.applicationContext, Arrays.asList(*SCOPES)).setBackOff(ExponentialBackOff())!!


    fun sheetApiInteractionPrerequisite(): Boolean {
        return if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices()
            false
        } else if (accountCredential.selectedAccountName == null) {
            chooseAccount()
            false
        } else if (!isDeviceOnline()) {
            activity.toast("No network connection available.")
            false
        }else
            true
    }


    private fun isGooglePlayServicesAvailable(): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(activity)
        return connectionStatusCode == ConnectionResult.SUCCESS
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private fun acquireGooglePlayServices() {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(activity)
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode)
        }
    }


    private fun isDeviceOnline(): Boolean {
        val connMgr = activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connMgr.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    private fun showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode: Int) {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val dialog = apiAvailability.getErrorDialog(
            activity,
            connectionStatusCode,
            REQUEST_GOOGLE_PLAY_SERVICES
        )
        dialog.show()
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_GOOGLE_PLAY_SERVICES -> if (resultCode != Activity.RESULT_OK) {
                Log.e(
                    TAG,
                    "This app requires Google Play Services. Please install " + "Google Play Services on your device and relaunch this app."
                )
            }
            REQUEST_ACCOUNT_PICKER -> if (resultCode == Activity.RESULT_OK && data != null &&
                data.extras != null
            ) {
                accountEmail = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                if (accountEmail != null) {
                    val settings = activity.getPreferences(Context.MODE_PRIVATE)
                    val editor = settings.edit()
                    editor.putString(PREF_ACCOUNT_NAME, accountEmail)
                    editor.apply()
                    accountCredential.selectedAccountName = accountEmail
                }
            }
        }
    }

    fun getAccountEmail(): String? {
        return if (accountEmail == null) {
            chooseAccount()
            accountEmail
        }else
            accountEmail
    }

    private fun chooseAccount() = activity.runWithPermissions(Manifest.permission.GET_ACCOUNTS){
        val accountName = activity.getPreferences(Context.MODE_PRIVATE)
            .getString(PREF_ACCOUNT_NAME, null)
        if (accountName != null) {
            accountCredential.selectedAccountName = accountName
            accountEmail = accountName
        } else {
            // Start a dialog from which the user can choose an account
            activity.startActivityForResult(
                accountCredential.newChooseAccountIntent(),
                REQUEST_ACCOUNT_PICKER
            )
        }
    }

    companion object {
        private val SCOPES = arrayOf(SheetsScopes.SPREADSHEETS)

        internal const val REQUEST_ACCOUNT_PICKER = 1000
        internal const val REQUEST_AUTHORIZATION_REQUEST = 1001
        internal const val REQUEST_AUTHORIZATION_POST = 1002
        internal const val REQUEST_GOOGLE_PLAY_SERVICES = 1003
        private const val PREF_ACCOUNT_NAME = "accountName"
        private const val TAG = "ServicesOperations"
    }
}