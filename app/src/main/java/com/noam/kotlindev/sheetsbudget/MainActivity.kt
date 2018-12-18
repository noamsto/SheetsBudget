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
import android.view.View
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.sheets.v4.SheetsScopes
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import com.noam.kotlindev.sheetsbudget.adapters.SortedExpenseAdapter
import com.noam.kotlindev.sheetsbudget.info.AccountInfo
import com.noam.kotlindev.sheetsbudget.info.ExpenseEntry
import com.noam.kotlindev.sheetsbudget.info.MonthExpenses
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.expense_entry.view.*
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast
import java.util.*

class MainActivity : AppCompatActivity(), SheetRequest.OnRequestResultListener, SheetPost.OnPostSuccess,
    SortedExpenseAdapter.ItemSelectedListener, SheetDeleteRow.OnDeleteListener {



    private val spreadsheetId = "1Q3VO5VLAIKi2uyhc7HIH-AOOt5FRujTRkh6D8QJ5IYE"
    private lateinit var sheet : String
    private lateinit var mCredential: GoogleAccountCredential
    private lateinit var mHandlerThread: HandlerThread
    private lateinit var handler: Handler
    private lateinit var monthRequest: SheetRequest
    private lateinit var lastPostRequest: SheetPost
    private var accountEmail: String? = null
    private lateinit var sortedExpenseAdapter: SortedExpenseAdapter
    private lateinit var currentMonthExpense : MonthExpenses

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        expense_entry_lt.checkbox.visibility = View.GONE

        month_expenses_rv.layoutManager = LinearLayoutManager(this)
        sortedExpenseAdapter = SortedExpenseAdapter(this, this)
        month_expenses_rv.adapter = sortedExpenseAdapter

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
        currentMonthExpense = MonthExpenses(sheet)
        monthRequest = SheetRequest(mCredential, sheet, spreadsheetId, this)

        sendRequestToApi(monthRequest)
        send_btn.setOnClickListener {
            val desc = desc_et.text.toString()
            val amount = amount_et.text.toString()
            if (desc.isNotBlank() && amount.isNotBlank() ){
                lastPostRequest = SheetPost(mCredential, spreadsheetId, sheet, ExpenseEntry(AccountInfo.getNameByEmail(accountEmail!!), main_date_tv.text.toString(),
                    desc_et.text.toString(), amount_et.text.toString(), sortedExpenseAdapter.size() + 1), this)
                sendRequestToApi(lastPostRequest)
                sendRequestToApi(monthRequest)
            }
        }
        delete_btn.setOnClickListener {
            sortedExpenseAdapter.selectedExpensesList.forEach { expense ->
                sendRequestToApi(SheetDeleteRow(mCredential, spreadsheetId, sheet,expense.row, this))
            }

        }
    }

    private fun sendRequestToApi(request: Runnable) {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices()
        } else if (mCredential.selectedAccountName == null) {
            chooseAccount()
        } else if (!isDeviceOnline()) {
            toast("No network connection available.")
        } else {
            handler.post(request)
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
                    sendRequestToApi(monthRequest)
                else{
                    sendRequestToApi(lastPostRequest)
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

    private fun showCalculatedSums(){
        sum_edit_tv.text = "${currentMonthExpense.total}₪"
        gal_sum_edit_tv.text = "${currentMonthExpense.galExpenses}₪"
        noam_sum_edit_tv.text = "${currentMonthExpense.noamExpenses}₪"
    }

    override fun onResultSuccess(list: List<List<String>>) {
        runOnUiThread {
            sortedExpenseAdapter.clear()
            currentMonthExpense.expenses.clear()
            list.forEach { entry ->
                if (entry.size == 4){
                    val expense = ExpenseEntry(entry[0], entry[1], entry[2], entry[3], sortedExpenseAdapter.size().plus(1))
                    sortedExpenseAdapter.add(expense)
                    currentMonthExpense.addExpense(expense)
                }
            }
            showCalculatedSums()
        }
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

    override fun onItemsSelected()
    {
        delete_btn.visibility = View.VISIBLE
    }

    override fun noItemsSelected() {
        delete_btn.visibility = View.GONE
    }
    override fun onDeleteSuccess() {
        toast("Deleted!")
        runOnUiThread{
            currentMonthExpense.removeAll(sortedExpenseAdapter.selectedExpensesList)
            sortedExpenseAdapter.removeSelected()
            showCalculatedSums()
        }
    }

    override fun onDeleteFailed(error: java.lang.Exception) {
        Log.e(TAG, error.message)
        toast(error.message.toString())
    }
}
