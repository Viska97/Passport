package org.visapps.passport.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.visapps.passport.repository.UserRepository

class SplashActivityViewModel : ViewModel() {

    private val repository : UserRepository = UserRepository.get()

    val userStatus = repository.userStatus

    fun closeDatabase() {
        GlobalScope.launch {
            repository.closeDatabase()
        }
    }

}