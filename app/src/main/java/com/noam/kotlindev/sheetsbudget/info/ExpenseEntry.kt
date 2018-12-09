package com.noam.kotlindev.sheetsbudget.info

import java.io.Serializable

class ExpenseEntry(val date: String, val description: String, val amount: String): Serializable{

    constructor(row: List<String>): this(
        date = row[0],
        description = row[1],
        amount = row[2]
    )

}