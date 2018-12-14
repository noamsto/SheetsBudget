package com.noam.kotlindev.sheetsbudget.adapters

import android.content.Context
import android.graphics.Color
import android.support.v7.util.SortedList
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.ViewHolder
import android.support.v7.widget.util.SortedListAdapterCallback
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.noam.kotlindev.sheetsbudget.R
import com.noam.kotlindev.sheetsbudget.adapters.ExpenseAdapter.ExpenseViewHolder
import com.noam.kotlindev.sheetsbudget.constants.AccountColor
import com.noam.kotlindev.sheetsbudget.info.ExpenseEntry
import kotlinx.android.synthetic.main.expense_entry.view.*


class ExpenseAdapter(val context: Context)
    : RecyclerView.Adapter<ExpenseViewHolder>() {

    private val sortedExpensesList : SortedList<ExpenseEntry>

    init {
        sortedExpensesList = SortedList<ExpenseEntry>(ExpenseEntry::class.java, object: SortedListAdapterCallback<ExpenseEntry>(this){
            override fun areItemsTheSame(p0: ExpenseEntry?, p1: ExpenseEntry?) = false
            override fun compare(p0: ExpenseEntry?, p1: ExpenseEntry?) = p0!!.compareTo(p1!!)
            override fun areContentsTheSame(p0: ExpenseEntry?, p1: ExpenseEntry?) = false

        })
    }


    override fun onBindViewHolder(expenseVH: ExpenseViewHolder, position: Int) {
        val expenseEntry : ExpenseEntry = sortedExpensesList[position]
        val date = if (expenseEntry.date.isBlank()){
            "חסר תאריך"
        }else{
            expenseEntry.date
        }
        expenseVH.name.text = expenseEntry.name
        expenseVH.date.text = date
        expenseVH.description.text = expenseEntry.description
        expenseVH.amount.text = expenseEntry.amount
        val color = if (expenseEntry.name == "גל"){
            Color.rgb(AccountColor.GAL_COLOR.red, AccountColor.GAL_COLOR.green, AccountColor.GAL_COLOR.blue)
        }else{
            Color.rgb(AccountColor.NOAM_COLOR.red, AccountColor.NOAM_COLOR.green, AccountColor.NOAM_COLOR.blue)
        }
        expenseVH.itemView.setBackgroundColor(color)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        return ExpenseViewHolder(LayoutInflater.from(context)
            .inflate(R.layout.expense_entry, parent, false))
    }

    override fun getItemCount() = sortedExpensesList.size()

    fun addall(list: List<ExpenseEntry>) = sortedExpensesList.addAll(list)
    fun add(expenseEntry: ExpenseEntry) = sortedExpensesList.add(expenseEntry)
    fun remove(position: Int) = sortedExpensesList.removeItemAt(position)
    fun size() = sortedExpensesList.size()
    fun clear() = sortedExpensesList.clear()

    class ExpenseViewHolder(expenseView: View): ViewHolder(expenseView){
        val name = expenseView.name_tv!!
        val date = expenseView.date_tv!!
        val description = expenseView.desc_tv!!
        val amount = expenseView.amount_tv!!
    }
}