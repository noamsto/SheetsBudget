package com.noam.kotlindev.sheetsbudget

import java.util.*

class DateOperations {
    private val calender: Calendar = Calendar.getInstance()
    val day = calender.get(Calendar.DAY_OF_MONTH)
    private val month = calender.get(Calendar.MONTH).plus(1)
    private val year = calender.get(Calendar.YEAR)

    fun getMonthYear() = "$month/${year - 2000}"
}