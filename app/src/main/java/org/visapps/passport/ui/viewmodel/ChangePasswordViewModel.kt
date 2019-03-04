package org.visapps.passport.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import org.visapps.passport.repository.UserRepository
import org.visapps.passport.util.PasswordChangeResult
import org.visapps.passport.util.SingleLiveEvent
import kotlin.coroutines.CoroutineContext

class ChangePasswordViewModel : ViewModel(), CoroutineScope {

    private val repository : UserRepository = UserRepository.get()

    val logOut = SingleLiveEvent<Unit>()
    val loading = MutableLiveData<Boolean>()
    val passwordsDoNotMatch = SingleLiveEvent<Unit>()
    val weakPassword = SingleLiveEvent<Unit>()
    val limitedPassword = SingleLiveEvent<Unit>()
    val success = SingleLiveEvent<Unit>()

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    private var job = Job()

    init {
        loading.postValue(false)
    }

    fun changePassword(password : String, confirmPassword : String) {
        if(password.isEmpty()){
            return
        }
        if(!password.equals(confirmPassword)){
            passwordsDoNotMatch.postValue(Unit)
            return
        }
        if(password.length !in 8..50){
            weakPassword.postValue(Unit)
            return
        }
        this.launch(context = coroutineContext) {
            loading.postValue(true)
            val result = repository.changePassword(password)
            loading.postValue(false)
            if(result == PasswordChangeResult.SUCCESS){
                success.postValue(Unit)
            }
            else {
                limitedPassword.postValue(Unit)
            }
        }
    }

    fun logOut() {
        GlobalScope.launch {
            repository.logOut()
        }
        logOut.postValue(Unit)
    }

    override fun onCleared() {
        job.cancel()
        super.onCleared()
    }

}