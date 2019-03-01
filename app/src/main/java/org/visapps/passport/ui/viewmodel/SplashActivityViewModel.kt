package org.visapps.passport.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations.switchMap
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.visapps.passport.repository.UserRepository
import org.visapps.passport.util.UserState

class SplashActivityViewModel : ViewModel() {

    private val repository : UserRepository = UserRepository.get()

    private val quit = MutableLiveData<Boolean>()

    val userStatus = repository.userStatus


    init {
        quit.postValue(false)
    }

    fun closeDatabase() {
        GlobalScope.launch {
            repository.closeDatabase()
        }
    }

}