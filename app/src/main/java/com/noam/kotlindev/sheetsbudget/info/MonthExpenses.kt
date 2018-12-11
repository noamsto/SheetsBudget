package com.noam.kotlindev.sheetsbudget.info

class MonthExpenses(val month: Int){
    val expenses: ArrayList<ExpenseEntry> = ArrayList()
    var total = 0


    fun addExpense(expenseEntry: ExpenseEntry){
        expenses.add(expenseEntry)
        calcTotal()
    }

    private fun calcTotal() {
        expenses.forEach {expense -> total.plus(expense.amount.toInt()) }
    }

}