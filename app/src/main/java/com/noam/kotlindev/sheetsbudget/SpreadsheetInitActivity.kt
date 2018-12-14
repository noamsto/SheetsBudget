//package com.noam.kotlindev.sheetsbudget
//
//import android.Manifest
//import android.accounts.AccountManager
//import android.app.Activity
//import android.content.Context
//import android.content.Intent
//import android.net.ConnectivityManager
//import android.os.Bundle
//import android.os.Handler
//import android.os.HandlerThread
//import android.support.v7.app.AppCompatActivity
//import android.support.v7.widget.LinearLayoutManager
//import android.util.Log
//import com.google.android.gms.common.ConnectionResult
//import com.google.android.gms.common.GoogleApiAvailability
//import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
//import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
//import com.google.api.client.googleapis.json.GoogleJsonResponseException
//import com.google.api.client.util.ExponentialBackOff
//import com.google.api.services.sheets.v4.SheetsScopes
//import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
//import com.noam.kotlindev.sheetsbudget.SheetRequest.OnRequestResultListener
//import com.noam.kotlindev.sheetsbudget.adapters.ExpenseAdapter
//import com.noam.kotlindev.sheetsbudget.info.ExpenseEntry
//import kotlinx.android.synthetic.main.activity_spreadsheet_init.*
//import org.jetbrains.anko.longToast
//import org.jetbrains.anko.toast
//import java.util.*
//
//class SpreadsheetInitActivity : AppCompatActivity(), OnRequestResultListener, SheetPost.OnPostSuccess {
//
//
//    private val spreadsheetId = "1Q3VO5VLAIKi2uyhc7HIH-AOOt5FRujTRkh6D8QJ5IYE"
//
//    private lateinit var range : String
//    private lateinit var mCredential: GoogleAccountCredential
//    private lateinit var mHandlerThread: HandlerThread
//    private lateinit var handler: Handler
//
//    private var expenseEntries = ArrayList<ExpenseEntry>()
//    private lateinit var expenseAdapter: ExpenseAdapter
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_spreadsheet_init)
//
//        val calendar = Calendar.getInstance()
//        month_picker.apply {
//            minValue = 0
//            maxValue = 12
//            value = calendar.get(Calendar.MONTH)
//        }
//        year_picker.apply {
//            year_picker.minValue = 2012
//            year_picker.maxValue = 2020
//            year_picker.value = calendar.get(Calendar.YEAR)
//        }
//
//        expenses_RV.layoutManager = LinearLayoutManager(this)
//        expenseAdapter = ExpenseAdapter(expenseEntries, this)
//        expenses_RV.adapter = expenseAdapter
//
//
//        // Initialize credentials and service object.
//        mCredential = GoogleAccountCredential.usingOAuth2(
//            applicationContext, Arrays.asList(*SCOPES))
//            .setBackOff(ExponentialBackOff())
//
//        mHandlerThread = HandlerThread("SheetsRequests")
//        mHandlerThread.start()
//        handler = Handler(mHandlerThread.looper)
//
//        make_request_btn.setOnClickListener {
//            range = "${month_picker.value}/${year_picker.value-2000}$GAL_RANGE"
//            getResultsFromApi()
//        }
//
//    }
//
//    private fun getResultsFromApi() {
//        if (!isGooglePlayServicesAvailable()) {
//            acquireGooglePlayServices()
//        } else if (mCredential.selectedAccountName == null) {
//            chooseAccount()
//        } else if (!isDeviceOnline()) {
//            toast("No network connection available.")
//        } else {
//            handler.post(SheetRequest(mCredential, range, spreadsheetId, this))
////            handler.post(SheetPost(mCredential, range, spreadsheetId, "", this))
//        }
//    }
//    private fun isGooglePlayServicesAvailable(): Boolean {
//        val apiAvailability = GoogleApiAvailability.getInstance()
//        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this)
//        return connectionStatusCode == ConnectionResult.SUCCESS
//    }
//
//    private fun chooseAccount() = runWithPermissions(Manifest.permission.GET_ACCOUNTS){
//        val accountName = getPreferences(Context.MODE_PRIVATE)
//            .getString(PREF_ACCOUNT_NAME, null)
//        if (accountName != null) {
//            mCredential.selectedAccountName = accountName
//            getResultsFromApi()
//        } else {
//            // Start a dialog from which the user can choose an account
//            startActivityForResult(
//                mCredential.newChooseAccountIntent(),
//                REQUEST_ACCOUNT_PICKER
//            )
//        }
//    }
//    /**
//     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
//     * Play Services installation via a user dialog, if possible.
//     */
//    private fun acquireGooglePlayServices() {
//        val apiAvailability = GoogleApiAvailability.getInstance()
//        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this)
//        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
//            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode)
//        }
//    }
//
//    /**
//     * Display an error dialog showing that Google Play Services is missing
//     * or out of date.
//     * @param connectionStatusCode code describing the presence (or lack of)
//     * Google Play Services on this device.
//     */
//    private fun showGooglePlayServicesAvailabilityErrorDialog(
//        connectionStatusCode: Int
//    ) {
//        val apiAvailability = GoogleApiAvailability.getInstance()
//        val dialog = apiAvailability.getErrorDialog(
//            this@SpreadsheetInitActivity,
//            connectionStatusCode,
//            REQUEST_GOOGLE_PLAY_SERVICES
//        )
//        dialog.show()
//    }
//
//    /**
//     * Checks whether the device currently has a network connection.
//     * @return true if the device has a network connection, false otherwise.
//     */
//    private fun isDeviceOnline(): Boolean {
//        val connMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//        val networkInfo = connMgr.activeNetworkInfo
//        return networkInfo != null && networkInfo.isConnected
//    }
//
//    /**
//     * Called when an activity launched here (specifically, AccountPicker
//     * and authorization) exits, giving you the requestCode you started it with,
//     * the resultCode it returned, and any additional data from it.
//     * @param requestCode code indicating which activity result is incoming.
//     * @param resultCode code indicating the result of the incoming
//     * activity result.
//     * @param data Intent (containing result data) returned by incoming
//     * activity result.
//     */
//    override fun onActivityResult(
//        requestCode: Int, resultCode: Int, data: Intent?
//    ) {
//        super.onActivityResult(requestCode, resultCode, data)
//        when (requestCode) {
//            REQUEST_GOOGLE_PLAY_SERVICES -> if (resultCode != Activity.RESULT_OK) {
//                Log.e(TAG,
//                    "This app requires Google Play Services. Please install " + "Google Play Services on your device and relaunch this app."
//                )
//            } else {
//                getResultsFromApi()
//            }
//            REQUEST_ACCOUNT_PICKER -> if (resultCode == Activity.RESULT_OK && data != null &&
//                data.extras != null
//            ) {
//                val accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
//                if (accountName != null) {
//                    val settings = getPreferences(Context.MODE_PRIVATE)
//                    val editor = settings.edit()
//                    editor.putString(PREF_ACCOUNT_NAME, accountName)
//                    editor.apply()
//                    mCredential.selectedAccountName = accountName
//                    getResultsFromApi()
//                }
//            }
//        }
//    }
//
//
//    override fun onResultSuccess(list: List<List<String>>) {
//        expenseEntries.clear()
//        list.forEach { entry ->
//            expenseEntries.add(0, ExpenseEntry(entry[0], entry[1], entry[2], entry[3], expenseEntries.size.plus(3)))
//        }
//        runOnUiThread {
//            expenseAdapter.notifyDataSetChanged()
//        }
//    }
//
//    override fun onResultFailed(error: Exception) {
//        when(error.javaClass){
//            UserRecoverableAuthIOException::class.java -> {
//                startActivityForResult(
//                    (error as UserRecoverableAuthIOException).intent, REQUEST_AUTHORIZATION
//                )
//            }
//            GoogleJsonResponseException::class.java -> {
//                longToast((error as GoogleJsonResponseException).details.message)
//            }
//            else ->{
//                longToast(error.message!!)
//            }
//        }
//    }
//
//    override fun onPostSuccess(list: List<List<String>>) {
//        Log.d(TAG, list.toString())
//        longToast("Success!")
//    }
//
//
//    override fun onPostFailed(error: Exception) {
//        when(error.javaClass){
//            UserRecoverableAuthIOException::class.java -> {
//                startActivityForResult(
//                    (error as UserRecoverableAuthIOException).intent, REQUEST_AUTHORIZATION
//                )
//            }
//            GoogleJsonResponseException::class.java -> {
//                longToast((error as GoogleJsonResponseException).details.message)
//            }
//            else ->{
//                longToast(error.message!!)
//            }
//        }
//    }
//
//    companion object {
//        private val SCOPES = arrayOf(SheetsScopes.SPREADSHEETS)
//        private const val REQUEST_ACCOUNT_PICKER = 1000
//        private const val REQUEST_AUTHORIZATION = 1001
//        private const val REQUEST_GOOGLE_PLAY_SERVICES = 1002
//        private const val PREF_ACCOUNT_NAME = "accountName"
//        private const val TAG = "SpreadsheetInitActivity"
//        const val GAL_RANGE = "!A3:C"
//        const val NOAM_RANGE = "!D3:I"
//    }
//}
