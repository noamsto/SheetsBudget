package com.noam.kotlindev.sheetsbudget.info

class AccountInfo(val userName: String, val email: String) {
    companion object {
        private val NOAM_ACCOUNT = AccountInfo("נועם", "stellern@gmail.com")
        private val GAL_ACCOUNT = AccountInfo("גל", "gal.shaul092@gmail.com")

        fun getNameByEmail(email: String) : String{
            return when (email) {
                NOAM_ACCOUNT.email -> NOAM_ACCOUNT.userName
                GAL_ACCOUNT.userName -> GAL_ACCOUNT.userName
                else -> ""
            }
        }
    }
}