package com.noam.kotlindev.sheetsbudget.info

import java.io.Serializable

class ExpenseEntry(val date: String, val description: String, val amount: String): Serializable
