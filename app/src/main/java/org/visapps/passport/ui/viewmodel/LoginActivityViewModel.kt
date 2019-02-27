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

    val loading = MutableLiveData<Boolean>()
    val message = SingleLiveEvent<Unit>()
    val alert = SingleLiveEvent<Int>()
    val result = SingleLiveEvent<Boolean>()
    var attemptsCount = 3

    private val repository : UserRepository = UserRepository.get()

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    private var job = Job()

    init {
        loading.postValue(false)
    }

    fun logIn(username : String, password : String) {
        this.launch(context = coroutineContext) {
            loading.postValue(true)
            attemptsCount --
            val authResult = repository.logIn(username, password)
            when(authResult){
                RequestResult.SUCCESS -> result.postValue(true)
                RequestResult.NOT_FOUND -> message.postValue(Unit)
                RequestResult.FAILURE -> {
                    attemptsCount --
                    if(attemptsCount > 0){
                        loading.postValue(false)
                        alert.postValue(attemptsCount)
                    }
                    else{
                        result.postValue(false)
                    }
                }
            }
        }
    }
}