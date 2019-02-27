package org.visapps.passport.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.visapps.passport.repository.UserRepository
import org.visapps.passport.util.SingleLiveEvent
import kotlin.coroutines.CoroutineContext

class EncryptActivityViewModel : ViewModel(), CoroutineScope {

    val loading = MutableLiveData<Boolean>()
    val finish = SingleLiveEvent<Unit>()

    private val repository : UserRepository = UserRepository.get()

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    private var job = Job()

    init {
        loading.postValue(false)
    }

    fun process(password : String) {
        this.launch(context = coroutineContext) {
            loading.postValue(true)
            repository.initDatabase(password)
            finish.postValue(Unit)
        }
    }


}