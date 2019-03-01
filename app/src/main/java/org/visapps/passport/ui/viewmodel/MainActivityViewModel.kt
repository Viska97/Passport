package org.visapps.passport.ui.viewmodel

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import org.visapps.passport.data.User
import org.visapps.passport.repository.UserRepository
import org.visapps.passport.util.SingleLiveEvent
import org.visapps.passport.util.UserState
import kotlin.coroutines.CoroutineContext

class MainActivityViewModel() : ViewModel(), CoroutineScope {

    private val repository : UserRepository = UserRepository.get()

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    val userStatus = repository.userStatus
    val user = MediatorLiveData<User>()
    val logOut = SingleLiveEvent<Unit>()

    private var job = Job()

    init {
        user.postValue(User(-1,"", ""))
        user.addSource(userStatus) {processUser(it)};
    }

    private fun processUser(status : UserState) {
        if(status == UserState.ADMIN || status == UserState.USER){
            this.launch(context = coroutineContext) {
                val result = repository.loadUser()
                result?.let {
                    user.postValue(result)
                }
            }
        }
    }

    fun logOut() {
        GlobalScope.launch {
            repository.logOut()
            logOut.postValue(Unit)
        }
    }

    override fun onCleared() {
        job.cancel()
        super.onCleared()
    }
}