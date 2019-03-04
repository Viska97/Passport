package org.visapps.passport.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import kotlinx.coroutines.runBlocking

class InMemoryDatabase(initial : List<User>) {

    private var users = mutableListOf<User>()
    private val usersLiveData = MutableLiveData<List<User>>()

    init{
        users.addAll(initial)
        users.sortBy { it.id }
        usersLiveData.postValue(users)
    }

    fun insertUser(user : User) = runBlocking {
        users.sortBy { it.id }
        val id = users[users.size-1].id + 1
        user.id = id
        users.add(user)
        updateLiveData()
    }

    fun getUserById(id : Int) : User? {
        return runBlocking {
            return@runBlocking users.find { it.id == id }
        }
    }

    fun getUserByName(name : String) : User? {
        return runBlocking {
            return@runBlocking users.find { it.name == name }
        }
    }

    fun getUsers() : LiveData<List<User>> {
        return usersLiveData
    }

    fun getUserName(id : Int) : LiveData<String> {
        return Transformations.map(usersLiveData){
            it.first { user-> user.id == id }.name
        }
    }

    fun update(user: User) {
        users.remove(user)
        users.add(user)
        updateLiveData()
    }

    fun getAll() : List<User> {
        return users
    }

    private fun updateLiveData() {
        users.sortBy { it.id }
        usersLiveData.postValue(users)
    }

}