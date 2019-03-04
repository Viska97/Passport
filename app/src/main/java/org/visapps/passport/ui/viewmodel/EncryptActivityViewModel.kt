package org.visapps.passport.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.visapps.passport.repository.UserRepository
import org.visapps.passport.util.SingleLiveEvent
import org.visapps.passport.util.UserStatus
import kotlin.coroutines.CoroutineContext

class EncryptActivityViewModel : ViewModel(), CoroutineScope {

    private val repository : UserRepository = UserRepository.get()

    val loading = MutableLiveData<Boolean>()
    val finish = SingleLiveEvent<Unit>()
    val weakPassword = SingleLiveEvent<Unit>()
    val invalidPassword = SingleLiveEvent<Unit>()
    val firstRun : LiveData<Boolean> = map(repository.userStatus){
        it == UserStatus.FIRST_RUN
    }

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    private var job = Job()

    init {
        loading.postValue(false)
    }

    fun process(password : String) {
        if(password.isEmpty()){
            return
        }
        if(firstRun.value == true && password.length !in 8..50){
            weakPassword.postValue(Unit)
            return
        }
        this.launch(context = coroutineContext) {
            loading.postValue(true)
            val success = repository.initDatabase(password)
            if(success){
                finish.postValue(Unit)
            }
            else{
                loading.postValue(false)
                invalidPassword.postValue(Unit)
            }
        }
    }

    fun quit() {
        repository.quitEncryptState()
        finish.postValue(Unit)
    }

    override fun onCleared() {
        job.cancel()
        super.onCleared()
    }

}