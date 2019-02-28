package org.visapps.passport.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.visapps.passport.repository.UserRepository
import org.visapps.passport.util.RequestResult
import org.visapps.passport.util.SingleLiveEvent
import kotlin.coroutines.CoroutineContext

class LoginActivityViewModel : ViewModel(), CoroutineScope {

    private val repository : UserRepository = UserRepository.get()

    val loading = MutableLiveData<Boolean>()
    val invalidUsername = SingleLiveEvent<Unit>()
    val invalidPassword = SingleLiveEvent<Int>()
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
                RequestResult.FAILURE -> {
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
}