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
import android.widget.CheckBox
import com.noam.kotlindev.sheetsbudget.R
import com.noam.kotlindev.sheetsbudget.adapters.ExpenseAdapter.ExpenseViewHolder
import com.noam.kotlindev.sheetsbudget.constants.AccountColor
import com.noam.kotlindev.sheetsbudget.info.ExpenseEntry
import kotlinx.android.synthetic.main.expense_entry.view.*


class ExpenseAdapter(private val context: Context, private val itemSelectedListener: ItemSelectedListener)
    : RecyclerView.Adapter<ExpenseViewHolder>() {

    private val sortedExpensesList : SortedList<ExpenseEntry>
    val selectedExpensesList = ArrayList<ExpenseEntry>()

    init {
        sortedExpensesList = SortedList<ExpenseEntry>(ExpenseEntry::class.java, object: SortedListAdapterCallback<ExpenseEntry>(this){
            override fun areItemsTheSame(p0: ExpenseEntry?, p1: ExpenseEntry?): Boolean
            {
                p0 ?: return false
                p1 ?: return false
                return !(p0.name != p1.name || p0.date != p1.date || p0.description != p1.description ||
                        p0.amount != p1.amount || p0.row != p1.row)

            }
            override fun compare(p0: ExpenseEntry?, p1: ExpenseEntry?) = p1!!.compareTo(p0!!)
            override fun areContentsTheSame(p0: ExpenseEntry?, p1: ExpenseEntry?): Boolean{
                p0 ?: return false
                p1 ?: return false
                return !(p0.name != p1.name || p0.date != p1.date || p0.description != p1.description ||
                        p0.amount != p1.amount || p0.row != p1.row)
            }
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
        expenseVH.checkBox.setOnClickListener { it ->
            val checkBox = it as CheckBox
            if (checkBox.isChecked){
                selectedExpensesList.add(expenseEntry)
                expenseVH.expenseView.alpha = 0.7F
                itemSelectedListener.onItemsSelected()
            }else{
                selectedExpensesList.remove(expenseEntry)
                expenseVH.expenseView.alpha = 1F
                if (selectedExpensesList.isEmpty())
                    itemSelectedListener.noItemsSelected()
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        return ExpenseViewHolder(LayoutInflater.from(context)
            .inflate(R.layout.expense_entry, parent, false))
    }

    override fun getItemCount() = sortedExpensesList.size()

    fun addall(list: List<ExpenseEntry>) = sortedExpensesList.addAll(list)
    fun add(expenseEntry: ExpenseEntry) = sortedExpensesList.add(expenseEntry)
    fun remove(position: Int) {
        sortedExpensesList.removeItemAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, sortedExpensesList.size())
    }
    fun removeSelected(){
        selectedExpensesList.forEach {
            remove(sortedExpensesList.indexOf(it))
        }
        selectedExpensesList.clear()
    }
    fun size() = sortedExpensesList.size()
    fun clear() = sortedExpensesList.clear()
    fun nextRow(){

    }

    class ExpenseViewHolder(val expenseView: View): ViewHolder(expenseView){
        val checkBox = expenseView.checkbox!!
        val name = expenseView.name_tv!!
        val date = expenseView.date_tv!!
        val description = expenseView.desc_tv!!
        val amount = expenseView.amount_tv!!

    }

    interface ItemSelectedListener{
        fun onItemsSelected()
        fun noItemsSelected()
    }

}