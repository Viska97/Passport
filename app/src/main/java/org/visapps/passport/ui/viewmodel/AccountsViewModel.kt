package org.visapps.passport.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.ViewModel;
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.visapps.passport.repository.UserRepository
import org.visapps.passport.util.UserStatus
import kotlin.coroutines.CoroutineContext

class AccountsViewModel : ViewModel(), CoroutineScope {

    private val repository : UserRepository = UserRepository.get()

    val users = repository.getUsers()
    val admin : LiveData<Boolean> = map(repository.userStatus){
        it == UserStatus.ADMIN
    }

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    private var job = Job()

    fun updateBlocked(id : Int) {
        this.launch(context = coroutineContext){
            repository.updateBlocked(id)
        }
    }

    fun updateLimit(id : Int) {
        this.launch(context = coroutineContext){
            repository.updateLimit(id)
        }
    }

    override fun onCleared() {
        job.cancel()
        super.onCleared()
    }

}
