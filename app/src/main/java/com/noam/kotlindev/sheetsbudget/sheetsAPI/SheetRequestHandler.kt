package com.noam.kotlindev.sheetsbudget.sheetsAPI

import android.os.Handler
import android.os.HandlerThread
import android.util.Log

class SheetRequestExecutor {

    private val requestsHandlerThread: HandlerThread = HandlerThread("SheetRequestExecutor")
    private val requestsHandler: Handler
    init {

        requestsHandlerThread.start()
        requestsHandler = Handler(requestsHandlerThread.looper)
    }

    fun postRequest(sheetRequest: SheetRequestRunnerBuilder.SheetRequestRunner){
        Log.d(TAG, "Posting sheetRequest to handler.")
        requestsHandler.post(sheetRequest)
    }


    fun postRequest(sheetRequest: Runnable){
        Log.d(TAG, "Posting sheetRequest to handler.")
        requestsHandler.post(sheetRequest)
    }
    companion object {
        private  const val TAG= "SheetRequestExecutor"
    }
}