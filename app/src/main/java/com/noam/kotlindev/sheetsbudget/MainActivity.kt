package com.noam.kotlindev.sheetsbudget

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.services.sheets.v4.SheetsScopes
import com.noam.kotlindev.sheetsbudget.adapters.SortedExpenseAdapter
import com.noam.kotlindev.sheetsbudget.info.AccountInfo
import com.noam.kotlindev.sheetsbudget.info.ExpenseEntry
import com.noam.kotlindev.sheetsbudget.info.MonthExpenses
import com.noam.kotlindev.sheetsbudget.sheetsAPI.SheetDeleteRangeRequest
import com.noam.kotlindev.sheetsbudget.sheetsAPI.SheetRequestHandler
import com.noam.kotlindev.sheetsbudget.sheetsAPI.SheetRequestRunnerBuilder
import com.noam.kotlindev.sheetsbudget.sheetsAPI.SheetRequestRunnerBuilder.Companion.DELETE_RESULT
import com.noam.kotlindev.sheetsbudget.sheetsAPI.SheetRequestRunnerBuilder.Companion.GET_RESULT
import com.noam.kotlindev.sheetsbudget.sheetsAPI.SheetRequestRunnerBuilder.Companion.UPDATE_RESULT
import com.noam.kotlindev.sheetsbudget.sheetsAPI.SheetRequestRunnerBuilder.SheetRequestRunner
import com.noam.kotlindev.sheetsbudget.sheetsAPI.SheetUpdateRequest
import com.noam.kotlindev.sheetsbudget.sheetsAPIrequestsHandlerThread.SheetGetRequest
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.expense_entry.view.*
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast
import java.util.*

class MainActivity : AppCompatActivity(), SheetRequestRunnerBuilder.OnRequestResultListener,
    SortedExpenseAdapter.ItemSelectedListener {

    private val spreadsheetId = "1Q3VO5VLAIKi2uyhc7HIH-AOOt5FRujTRkh6D8QJ5IYE"
    private lateinit var sheet : String
    private lateinit var accountCredential: GoogleAccountCredential
    private val sheetRequestExecutor = SheetRequestHandler()
    private lateinit var sheetRequestRunnerBuilder: SheetRequestRunnerBuilder
    private lateinit var monthRequest: SheetGetRequest
    private lateinit var lastUpdateRequest: SheetUpdateRequest
    private var accountEmail: String? = null
    private lateinit var sortedExpenseAdapter: SortedExpenseAdapter
    private lateinit var currentMonthExpense : MonthExpenses
    private lateinit var servicesOperations: ServicesOperations

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        expense_entry_lt.checkbox.visibility = View.GONE
        month_expenses_rv.layoutManager = LinearLayoutManager(this)
        sortedExpenseAdapter = SortedExpenseAdapter(this, this)
        month_expenses_rv.adapter = sortedExpenseAdapter

        servicesOperations = ServicesOperations(this)
        accountCredential = servicesOperations.accountCredential
        accountEmail = servicesOperations.getAccountEmail()

        sheetRequestRunnerBuilder = SheetRequestRunnerBuilder(this, accountCredential, this)

        val calender = Calendar.getInstance()
        val day = calender.get(Calendar.DAY_OF_MONTH)
        val month = calender.get(Calendar.MONTH).plus(1)
        val year = calender.get(Calendar.YEAR)

        main_date_tv.text = "${day.toString().padStart(2, '0')}/$month/${year.toString().removeRange(0,2)}"
        sheet = "$month/${year - 2000}"
        currentMonthExpense = MonthExpenses(sheet)
        monthRequest = SheetGetRequest(sheet, spreadsheetId)

        sendRequestToApi(sheetRequestRunnerBuilder.buildRequest(monthRequest))
        send_btn.setOnClickListener {
            val desc = desc_et.text.toString()
            val amount = amount_et.text.toString()
            if (desc.isNotBlank() && amount.isNotBlank() ){
                lastUpdateRequest = SheetUpdateRequest(spreadsheetId, sheet, ExpenseEntry(
                    AccountInfo.getNameByEmail(accountEmail!!), main_date_tv.text.toString(),
                    desc_et.text.toString(), amount_et.text.toString(), sortedExpenseAdapter.size() + 1)
                )
                sendRequestToApi(sheetRequestRunnerBuilder.buildRequest(lastUpdateRequest))
                sendRequestToApi(sheetRequestRunnerBuilder.buildRequest(monthRequest))
            }
        }
        delete_btn.setOnClickListener {
            val rows =  ArrayList<Int>()
            sortedExpenseAdapter.selectedExpensesList.forEach { expense ->
                rows.add(expense.row)
            }
            val sheetDeleteRangeRequest = SheetDeleteRangeRequest(sheet, spreadsheetId, rows)
            sendRequestToApi(sheetRequestRunnerBuilder.buildRequest(sheetDeleteRangeRequest))
        }
    }

    private fun sendRequestToApi(request: SheetRequestRunner) {
        if (servicesOperations.sheetApiInteractionPrerequisite())
            sheetRequestExecutor.postRequest(request)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        servicesOperations.onActivityResult(requestCode, resultCode, data)
    }

    private fun showCalculatedSums(){
        sum_edit_tv.text = "${currentMonthExpense.total}₪"
        gal_sum_edit_tv.text = "${currentMonthExpense.galExpenses}₪"
        noam_sum_edit_tv.text = "${currentMonthExpense.noamExpenses}₪"
    }

    override fun onRequestSuccess(list: List<List<String>>?, resultCode: Int) {
        runOnUiThread {
            when(resultCode){
                GET_RESULT -> {
                    currentMonthExpense.expenses.clear()
                    sortedExpenseAdapter.clear()
                    list!!.forEach { entry ->
                        if (entry.size == 4){
                            val expense = ExpenseEntry(entry[0], entry[1], entry[2], entry[3], sortedExpenseAdapter.size().plus(1))
                            sortedExpenseAdapter.add(expense)
                            currentMonthExpense.addExpense(expense)
                        }
                    }
                }
                UPDATE_RESULT ->{
                    list!!.forEach { entry ->
                        sortedExpenseAdapter.add(ExpenseEntry(entry[0], entry[1], entry[2], entry[3],
                            sortedExpenseAdapter.size().plus(1)))
                    }
                    desc_et.setText("")
                    amount_et.setText("")
                }
                DELETE_RESULT -> {
                    toast("Delete range succeeded.")
                    currentMonthExpense.removeAll(sortedExpenseAdapter.selectedExpensesList)
                    sortedExpenseAdapter.removeSelected()
                    delete_btn.visibility = View.GONE
                }
            }
            showCalculatedSums()
        }
    }

    override fun onRequestFailed(error: Exception) {
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

    override fun onItemsSelected(){
        delete_btn.visibility = View.VISIBLE
    }

    override fun noItemsSelected() {
        delete_btn.visibility = View.GONE
    }

    companion object {
        internal const val REQUEST_AUTHORIZATION_REQUEST = 1001
        internal const val REQUEST_AUTHORIZATION_POST = 1002
        private const val TAG = "MainActivity"
    }

}
