package com.noam.kotlindev.sheetsbudget.info

import java.io.Serializable

class ExpenseEntry(val name: String, val date: String, val description: String, val amount: String, val row: Int): Comparable<ExpenseEntry>, Serializable{
    override fun compareTo(other: ExpenseEntry): Int {
        val dayOfMonth1 = date.substringBefore('/').toInt()
        val dayOfMonth2 = other.date.substringBefore('/').toInt()
        return dayOfMonth1.compareTo(dayOfMonth2)
    }

    fun getValues()= mutableListOf(name, date, description, amount)

}
