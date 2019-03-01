package org.visapps.passport.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.visapps.passport.R
import org.visapps.passport.repository.UserRepository
import org.visapps.passport.util.SingleLiveEvent
import kotlin.coroutines.CoroutineContext

class NewUserViewModel : ViewModel(), CoroutineScope {

    private val repository : UserRepository = UserRepository.get()

    val loading = MutableLiveData<Boolean>()
    val alert = SingleLiveEvent<Int>()
    val result = SingleLiveEvent<Unit>()

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    private var job = Job()

    init {
        loading.postValue(false)
    }

    fun addUser(username : String) {
        if(username.isEmpty()){
            return
        }
        this.launch(context = coroutineContext){
            loading.postValue(true)
            val success = repository.addUser(username)
            if(success){
                result.postValue(Unit)
            }
            else{
                loading.postValue(false)
                alert.postValue(R.string.username_exists)
            }
        }
    }

    override fun onCleared() {
        job.cancel()
        super.onCleared()
    }


}