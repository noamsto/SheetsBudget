package com.noam.kotlindev.sheetsbudget.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.ViewHolder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.noam.kotlindev.sheetsbudget.R
import com.noam.kotlindev.sheetsbudget.adapters.ExpenseAdapter.ExpenseViewHolder
import com.noam.kotlindev.sheetsbudget.info.ExpenseEntry
import kotlinx.android.synthetic.main.expense_entry.view.*


class ExpenseAdapter(private val expenseEntries: List<ExpenseEntry>, val context: Context)
    : RecyclerView.Adapter<ExpenseViewHolder>() {

    override fun onBindViewHolder(expenseVH: ExpenseViewHolder, position: Int) {
        val expenseEntry = expenseEntries[position]
        expenseVH.date.text = expenseEntry.date
        expenseVH.description.text = expenseEntry.description
        expenseVH.amount.text = expenseEntry.amount
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        return ExpenseViewHolder(LayoutInflater.from(context)
            .inflate(R.layout.expense_entry, parent, false))
    }

    override fun getItemCount() = expenseEntries.size

    class ExpenseViewHolder(expenseView: View): ViewHolder(expenseView){
        val date = expenseView.date_tv
        val description = expenseView.desc_tv
        val amount = expenseView.amount_tv
    }
}