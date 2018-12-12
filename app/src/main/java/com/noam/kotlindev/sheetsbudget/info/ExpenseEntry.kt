package com.noam.kotlindev.sheetsbudget.info

import java.io.Serializable

class ExpenseEntry(val date: String, val description: String, val amount: String, val row: Int): Serializable{
    fun getValues()= mutableListOf(date, description, amount)
}
