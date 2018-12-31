package com.noam.kotlindev.sheetsbudget

import android.Manifest
import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.sheets.v4.SheetsScopes
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import com.noam.kotlindev.sheetsbudget.constants.SpreadSheetInfo
import com.noam.kotlindev.sheetsbudget.info.ExpenseEntry
import com.noam.kotlindev.sheetsbudget.info.MonthExpenses
import com.noam.kotlindev.sheetsbudget.sheetsAPI.SheetAddSheetRequest
import com.noam.kotlindev.sheetsbudget.sheetsAPI.SheetRequestHandler
import com.noam.kotlindev.sheetsbudget.sheetsAPI.SheetRequestRunnerBuilder
import com.noam.kotlindev.sheetsbudget.sheetsAPIrequestsHandlerThread.SheetGetRequest
import org.jetbrains.anko.*
import java.lang.Exception
import java.util.*

class SplashScreenActivity : AppCompatActivity(), SheetRequestRunnerBuilder.OnRequestResultListener {


    private var accountEmail: String? = null
    private lateinit var accountCredential: GoogleAccountCredential
    private lateinit var currentMonthExpense : MonthExpenses
    private lateinit var monthRequest: SheetGetRequest
    private val sheetRequestHandler = SheetRequestHandler()
    private lateinit var sheetRequestRunnerBuilder: SheetRequestRunnerBuilder
    private lateinit var  sheet: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        accountCredential = GoogleAccountCredential.usingOAuth2(
                applicationContext, Arrays.asList(*SpreadSheetInfo.SCOPES)).setBackOff(ExponentialBackOff())!!

        if (!isDeviceOnline()){
            toast("No internet connection, working offline.")
        }

        if (!isGooglePlayServicesAvailable())
            acquireGooglePlayServices()



        sheetRequestRunnerBuilder = SheetRequestRunnerBuilder(this, accountCredential, this)
        val calender = Calendar.getInstance()
        val month = calender.get(Calendar.MONTH).plus(1)
        val year = calender.get(Calendar.YEAR)
        val sheet = "$month/${year - 2000}"
        currentMonthExpense = MonthExpenses(sheet)
        monthRequest = SheetGetRequest(sheet, SpreadSheetInfo.ID)

        if (accountCredential.selectedAccountName == null)
            chooseAccount()
    }

    private fun sendRequestToApi(request: SheetRequestRunnerBuilder.SheetRequestRunner) {
        sheetRequestHandler.postRequest(request)
    }

    private fun addNewSheet(){
        val sheetAddSheetRequest = SheetAddSheetRequest(sheet, SpreadSheetInfo.ID)
        sheetRequestHandler.postRequest(sheetRequestRunnerBuilder.buildRequest(sheetAddSheetRequest))
    }

    private fun startApp(){
        val mainActivityIntent = Intent(this, MainActivity::class.java)
        mainActivityIntent.putExtra(MONTH_EXPENSE_TAG, currentMonthExpense)
        mainActivityIntent.putExtra(ACCOUNT_EMAIL_TAG, accountEmail)
        startActivity(mainActivityIntent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_GOOGLE_PLAY_SERVICES ->
                if (resultCode != Activity.RESULT_OK) {
                    Log.e(TAG,"This app requires Google Play Services. Please install " +
                            "Google Play Services on your device and relaunch this app.")
                    alert ("This app requires Google Play Services. Please install\n " +
                            "Google Play Services on your device and relaunch this app.") {
                        okButton { finish() }
                    }.show()
                }else{
                    sendRequestToApi(sheetRequestRunnerBuilder.buildRequest(monthRequest))
                }
            REQUEST_ACCOUNT_PICKER ->
                if (resultCode == Activity.RESULT_OK && data != null && data.extras != null
                ) {
                    accountEmail = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                    if (accountEmail != null) {
                        val settings = getPreferences(Context.MODE_PRIVATE)
                        val editor = settings.edit()
                        editor.putString(PREF_ACCOUNT_NAME, accountEmail)
                        editor.apply()
                        accountCredential.selectedAccountName = accountEmail
                    }
                    sendRequestToApi(sheetRequestRunnerBuilder.buildRequest(monthRequest))
                }else{
                    alert ("Account must be chosen for this app to work, Do you want to try again?") {
                        yesButton { chooseAccount() }
                        noButton { finish() }
                    }.show()
                }
        }
    }

    private fun chooseAccount() = runWithPermissions(Manifest.permission.GET_ACCOUNTS){
        val accountName = getPreferences(Context.MODE_PRIVATE)
            .getString(PREF_ACCOUNT_NAME, null)
        if (accountName != null) {
            accountCredential.selectedAccountName = accountName
            accountEmail = accountName
            sendRequestToApi(sheetRequestRunnerBuilder.buildRequest(monthRequest))
        } else {
            // Start a dialog from which the user can choose an account
            startActivityForResult(
                accountCredential.newChooseAccountIntent(),
                REQUEST_ACCOUNT_PICKER
            )
        }
    }

    private fun isGooglePlayServicesAvailable(): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this)
        return connectionStatusCode == ConnectionResult.SUCCESS
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

    private fun isDeviceOnline(): Boolean {
        val connMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connMgr.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    private fun showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode: Int) {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val dialog = apiAvailability.getErrorDialog(
            this@SplashScreenActivity,
            connectionStatusCode,
            REQUEST_GOOGLE_PLAY_SERVICES
        )
        dialog.show()
    }

    override fun onRequestSuccess(list: List<List<String>>?, resultCode: Int) {
        currentMonthExpense.expenses.clear()
        list?.forEach { entry ->
            if (entry.size == 4){
                val expense = ExpenseEntry(entry[0], entry[1], entry[2], entry[3],
                    currentMonthExpense.expenses.size.plus(1))
                currentMonthExpense.addExpense(expense)
            }
        }
        startApp()
    }

    override fun onRequestFailed(error: Exception) {
        when(error.javaClass){
            UserRecoverableAuthIOException::class.java -> {
                startActivityForResult(
                    (error as UserRecoverableAuthIOException).intent, MainActivity.REQUEST_AUTHORIZATION_REQUEST
                )
            }
            GoogleJsonResponseException::class.java -> {
                when((error as GoogleJsonResponseException).details.code){
                    400 -> addNewSheet()
                    else ->
                        longToast(error.details.message)
                }
            }
            else ->{
                longToast(error.message!!)
            }
        }
    }

    companion object {
        internal const val REQUEST_ACCOUNT_PICKER = 1000
        internal const val REQUEST_AUTHORIZATION_REQUEST = 1001
        internal const val REQUEST_AUTHORIZATION_POST = 1002
        internal const val REQUEST_GOOGLE_PLAY_SERVICES = 1003
        private const val PREF_ACCOUNT_NAME = "accountName"
        private const val TAG = "SplashScreenActivity"
        const val MONTH_EXPENSE_TAG = "month_expense"
        const val ACCOUNT_EMAIL_TAG = "account_email"
    }

}
