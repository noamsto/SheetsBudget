package com.noam.kotlindev.sheetsbudget.info

class TotalExpenses(val name: String){

    private val allMonthExpenses: ArrayList<MonthExpenses> = ArrayList()
    var total = 0

    fun addMonth(monthExpenses: MonthExpenses){
        allMonthExpenses.add(monthExpenses)
        calcExpenses()
    }

    private fun calcExpenses(){
        allMonthExpenses.forEach { expense -> total.plus(expense.total) }
    }

}