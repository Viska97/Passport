package org.visapps.passport.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import org.visapps.passport.repository.UserRepository
import org.visapps.passport.util.RequestResult
import org.visapps.passport.util.SingleLiveEvent
import org.visapps.passport.util.UserState
import kotlin.coroutines.CoroutineContext

class LoginActivityViewModel : ViewModel(), CoroutineScope {

    private val repository : UserRepository = UserRepository.get()

    val loading = MutableLiveData<Boolean>()
    val invalidUsername = SingleLiveEvent<Unit>()
    val invalidPassword = SingleLiveEvent<Int>()
    val userBlocked = SingleLiveEvent<Unit>()
    val result = SingleLiveEvent<Unit>()
    var attemptsCount = 3

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    private var job = Job()

    init {
        loading.postValue(false)
    }

    fun logIn(username : String, password : String) {
        if(username.isEmpty()){
            return
        }
        this.launch(context = coroutineContext) {
            loading.postValue(true)
            val authResult = repository.logIn(username, password)
            when(authResult){
                RequestResult.SUCCESS -> result.postValue(Unit)
                RequestResult.NOT_FOUND -> {
                    loading.postValue(false)
                    invalidUsername.postValue(Unit)}
                RequestResult.BLOCKED -> {
                    loading.postValue(false)
                    userBlocked.postValue(Unit)
                }
                RequestResult.INVALID_PASSWORD -> {
                    attemptsCount --
                    if(attemptsCount > 0){
                        loading.postValue(false)
                        invalidPassword.postValue(attemptsCount)
                    }
                    else{
                        repository.closeDatabase()
                        result.postValue(Unit)
                    }
                }
            }
        }
    }

    override fun onCleared() {
        GlobalScope.launch {
            repository.onQuitLogInState()
        }
        job.cancel()
        super.onCleared()
    }
}