package com.noam.kotlindev.sheetsbudget.info

import java.io.Serializable

class ExpenseEntry(val name: String, val date: String, val description: String, val amount: String, val row: Int): Comparable<ExpenseEntry>, Serializable{
    override fun compareTo(other: ExpenseEntry): Int {
        return date.compareTo(date)
    }

    fun getValues()= mutableListOf(name, date, description, amount)
}
