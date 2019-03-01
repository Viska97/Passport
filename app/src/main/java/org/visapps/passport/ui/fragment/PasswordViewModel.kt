package org.visapps.passport.ui.fragment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel;
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.visapps.passport.repository.UserRepository
import org.visapps.passport.util.PasswordChangeResult
import org.visapps.passport.util.SingleLiveEvent
import kotlin.coroutines.CoroutineContext

class PasswordViewModel : ViewModel(), CoroutineScope {

    private val repository : UserRepository = UserRepository.get()

    val loading = MutableLiveData<Boolean>()
    val invalidCurrentPassword = SingleLiveEvent<Unit>()
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

    fun changePassword(oldPassword : String, newPassword : String, confirmPassword : String) {
        if(newPassword.isEmpty() || confirmPassword.isEmpty()){
            return
        }
        if(!newPassword.equals(confirmPassword)){
            passwordsDoNotMatch.postValue(Unit)
            return
        }
        if(newPassword.length !in 8..50){
            weakPassword.postValue(Unit)
            return
        }
        this.launch(context = coroutineContext) {
            loading.postValue(true)
            val result = repository.changePassword(oldPassword, newPassword)
            loading.postValue(false)
            when(result){
                PasswordChangeResult.SUCCESS -> {
                    success.postValue(Unit)
                }
                PasswordChangeResult.INVALID_CURRENT -> {
                    invalidCurrentPassword.postValue(Unit)
                }
                PasswordChangeResult.INVALID_NEW -> {
                    limitedPassword.postValue(Unit)
                }
            }
        }
    }

}
