package com.noam.kotlindev.sheetsbudget

import android.Manifest
import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.sheets.v4.SheetsScopes
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import com.noam.kotlindev.sheetsbudget.adapters.ExpenseAdapter
import com.noam.kotlindev.sheetsbudget.info.AccountInfo
import com.noam.kotlindev.sheetsbudget.info.ExpenseEntry
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast
import java.util.*

class MainActivity : AppCompatActivity(), SheetRequest.OnRequestResultListener, SheetPost.OnPostSuccess {

    private val spreadsheetId = "1Q3VO5VLAIKi2uyhc7HIH-AOOt5FRujTRkh6D8QJ5IYE"
    private lateinit var sheet : String
    private lateinit var mCredential: GoogleAccountCredential
    private lateinit var mHandlerThread: HandlerThread
    private lateinit var handler: Handler
    private lateinit var monthRequest: SheetRequest
    private lateinit var lastPostRequest: SheetPost
    private var accountEmail: String? = null
    private lateinit var expenseAdapter: ExpenseAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        month_expenses_rv.layoutManager = LinearLayoutManager(this)
        expenseAdapter = ExpenseAdapter(this)
        month_expenses_rv.adapter = expenseAdapter



        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
            applicationContext, Arrays.asList(*SCOPES))
            .setBackOff(ExponentialBackOff())

        mHandlerThread = HandlerThread("SheetsRequests")
        mHandlerThread.start()
        handler = Handler(mHandlerThread.looper)

        chooseAccount()

        val calender = Calendar.getInstance()
        val day = calender.get(Calendar.DAY_OF_MONTH)
        val month = calender.get(Calendar.MONTH).plus(1)
        val year = calender.get(Calendar.YEAR)

        main_date_tv.text = "${day.toString().padStart(2, '0')}/$month/${year.toString().removeRange(0,2)}"
        sheet = "$month/${year - 2000}"
        monthRequest = SheetRequest(mCredential, sheet, spreadsheetId, this)

        getResultsFromApi(monthRequest)
        send_btn.setOnClickListener {
            val desc = desc_et.text.toString()
            val amount = amount_et.text.toString()
            if (desc.isNotBlank() && amount.isNotBlank() ){
                lastPostRequest = SheetPost(mCredential, spreadsheetId, sheet, ExpenseEntry(AccountInfo.getNameByEmail(accountEmail!!), main_date_tv.text.toString(),
                    desc_et.text.toString(), amount_et.text.toString(), expenseAdapter.size() + 2), this)
                getResultsFromApi(lastPostRequest)
                getResultsFromApi(monthRequest)
            }
        }
    }


    private fun getResultsFromApi(request: Runnable) {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices()
        } else if (mCredential.selectedAccountName == null) {
            chooseAccount()
        } else if (!isDeviceOnline()) {
            toast("No network connection available.")
        } else {
            handler.post(request)
//            handler.post()
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
            accountEmail = accountName
        } else {
            // Start a dialog from which the user can choose an account
            startActivityForResult(
                mCredential.newChooseAccountIntent(),
                REQUEST_ACCOUNT_PICKER)
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
    private fun showGooglePlayServicesAvailabilityErrorDialog(
        connectionStatusCode: Int
    ) {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val dialog = apiAvailability.getErrorDialog(
            this@MainActivity,
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

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode code indicating the result of the incoming
     * activity result.
     * @param data Intent (containing result data) returned by incoming
     * activity result.
     */
    override fun onActivityResult(
        requestCode: Int, resultCode: Int, data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_GOOGLE_PLAY_SERVICES -> if (resultCode != Activity.RESULT_OK) {
                Log.e(
                    TAG,
                    "This app requires Google Play Services. Please install " + "Google Play Services on your device and relaunch this app."
                )
            } else {
                if (requestCode == REQUEST_AUTHORIZATION_REQUEST || requestCode == REQUEST_ACCOUNT_PICKER)
                    getResultsFromApi(monthRequest)
                else{
                    getResultsFromApi(lastPostRequest)
                }
            }
            REQUEST_ACCOUNT_PICKER -> if (resultCode == Activity.RESULT_OK && data != null &&
                data.extras != null
            ) {
                accountEmail = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                if (accountEmail != null) {
                    val settings = getPreferences(Context.MODE_PRIVATE)
                    val editor = settings.edit()
                    editor.putString(PREF_ACCOUNT_NAME, accountEmail)
                    editor.apply()
                    mCredential.selectedAccountName = accountEmail
                }
            }
        }
    }


    override fun onResultSuccess(list: List<List<String>>) {
        runOnUiThread {
            expenseAdapter.clear()
            list.forEach { entry ->
                if (entry.size == 4)
                    expenseAdapter.add(ExpenseEntry(entry[0], entry[1], entry[2], entry[3], expenseAdapter.size().plus(2) ))
        }

        }
//        runOnUiThread {
////            expenseAdapter.notifyDataSetChanged()
//        }
    }

    override fun onResultFailed(error: Exception) {
        when(error.javaClass){
            UserRecoverableAuthIOException::class.java -> {
                startActivityForResult(
                    (error as UserRecoverableAuthIOException).intent, REQUEST_AUTHORIZATION_REQUEST
                )
            }
            GoogleJsonResponseException::class.java -> {
                longToast((error as GoogleJsonResponseException).details.message)
            }
            else ->{
                longToast(error.message!!)
            }
        }
    }
    override fun onPostSuccess(list: List<List<String>>) {
        Log.d(TAG, list.toString())
        longToast("Success!")
    }

    override fun onPostFailed(error: Exception) {
        when(error.javaClass){
            UserRecoverableAuthIOException::class.java -> {
                startActivityForResult(
                    (error as UserRecoverableAuthIOException).intent, REQUEST_AUTHORIZATION_POST
                )
            }
            GoogleJsonResponseException::class.java -> {
                longToast((error as GoogleJsonResponseException).details.message)
            }
            else ->{
                longToast(error.message!!)
            }
        }
    }

    companion object {
        private val SCOPES = arrayOf(SheetsScopes.SPREADSHEETS)
        internal const val REQUEST_ACCOUNT_PICKER = 1000
        internal const val REQUEST_AUTHORIZATION_REQUEST = 1001
        internal const val REQUEST_AUTHORIZATION_POST = 1002
        internal const val REQUEST_GOOGLE_PLAY_SERVICES = 1003
        private const val PREF_ACCOUNT_NAME = "accountName"
        private const val TAG = "MainActivity"
    }
}
