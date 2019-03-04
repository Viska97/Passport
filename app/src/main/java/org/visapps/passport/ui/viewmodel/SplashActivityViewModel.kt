package org.visapps.passport.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.visapps.passport.repository.UserRepository

class SplashActivityViewModel : ViewModel() {

    private val repository : UserRepository = UserRepository.get()

    private val quit = MutableLiveData<Boolean>()

    val userStatus = repository.userStatus

    init {
        quit.postValue(false)
    }

    fun quit() {
        repository.quitAllStates()
    }

}