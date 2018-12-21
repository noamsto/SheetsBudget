package com.noam.kotlindev.sheetsbudget.info

class MonthExpenses(month: String){
    val expenses: ArrayList<ExpenseEntry> = ArrayList()
    var total = 0
    var galExpenses = 0
    var noamExpenses = 0

    fun addExpense(expenseEntry: ExpenseEntry){
        expenses.add(expenseEntry)
        calcDiffExpenses()
        calcTotal()
    }

    fun removeExpense(expenseEntry: ExpenseEntry){
        expenses.remove(expenseEntry)
        calcAllExpenses()
    }

    private fun calcAllExpenses(){
        calcDiffExpenses()
        calcTotal()
    }

    private fun calcDiffExpenses(){
        galExpenses = 0
        noamExpenses = 0
        expenses.forEach {
            if (it.name == "גל")
                galExpenses += it.amount.filter { c -> c.isDigit()}.toInt()
            else
                noamExpenses += it.amount.filter { c -> c.isDigit()}.toInt()
        }
    }

    private fun calcTotal() {
        total = galExpenses + noamExpenses
    }

    fun removeAll(expensesToRemove: ArrayList<ExpenseEntry>) {
        expenses.removeAll(expensesToRemove)
        calcAllExpenses()
    }
}