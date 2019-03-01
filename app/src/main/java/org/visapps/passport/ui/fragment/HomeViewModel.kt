package org.visapps.passport.ui.fragment

import androidx.lifecycle.ViewModel;
import org.visapps.passport.repository.UserRepository

class HomeViewModel : ViewModel() {

    private val repository : UserRepository = UserRepository.get()

    val username = repository.getUsername()

}
