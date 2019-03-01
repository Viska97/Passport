package org.visapps.passport.ui.fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.ViewModel;
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.visapps.passport.repository.UserRepository
import org.visapps.passport.util.UserState
import kotlin.coroutines.CoroutineContext

class AccountsViewModel : ViewModel(), CoroutineScope {

    private val repository : UserRepository = UserRepository.get()

    val users = repository.getUsers()
    val admin : LiveData<Boolean> = map(repository.userStatus){
        it == UserState.ADMIN
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

}
