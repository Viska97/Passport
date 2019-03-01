package org.visapps.passport.worker

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.runBlocking
import org.visapps.passport.repository.UserRepository

class EncryptWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result = runBlocking {
        val repository = UserRepository.get()
        repository.closeDatabase()
        return@runBlocking Result.success()
    }
}