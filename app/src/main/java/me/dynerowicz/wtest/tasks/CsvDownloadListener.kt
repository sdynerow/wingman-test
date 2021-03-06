package me.dynerowicz.wtest.tasks

import android.util.Log

interface CsvDownloadListener {

    fun onDownloadUpdate(percentage: Int) { Log.v(TAG, "Download in progress : $percentage %") }
    fun onDownloadComplete(success: Boolean) { Log.i(TAG, "Download completed successfully : $success") }
    fun onDownloadCancelled() { Log.i(TAG, "Download cancelled") }
    fun onDownloadFailed(reason: String) { Log.i(TAG, "Download failed $reason") }

    companion object {
        const val TAG = "DownloadListener"
    }
}