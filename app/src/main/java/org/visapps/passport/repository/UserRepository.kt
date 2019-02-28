package org.visapps.passport.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.visapps.passport.PassportApp
import org.visapps.passport.data.AppDatabase
import org.visapps.passport.data.User
import org.visapps.passport.util.RequestResult
import org.visapps.passport.util.UserState
import java.io.File

class UserRepository {

    val userStatus = MutableLiveData<UserState>()

    private val datafile: File
    private val tempfile: File
    private var database : AppDatabase? = null
    private var passphrase : String = ""
    private var userId : Int? = null

    init {
        datafile = File(PassportApp.instance.applicationContext.getExternalFilesDir(""), DATAFILE_NAME)
        tempfile = File(PassportApp.instance.applicationContext.getExternalFilesDir(""), TEMPFILE_NAME)
        if(datafile.exists()) {
            userStatus.postValue(UserState.NOT_DECRYPTED)
        }
        else{
            userStatus.postValue(UserState.FIRST_RUN)
        }
    }

    suspend fun loadUser() : User? {
        userId?.let {
            return withContext(Dispatchers.IO) {database?.userDao()?.getUserById(it)}
        }
        return null
    }

    suspend fun logIn(username : String, password: String) : RequestResult {
        val user = withContext(Dispatchers.IO){database?.userDao()?.getUserByName(username)}
        user?.let{
            if(it.password.equals(password)){
                userId = it.id
                if(it.id == 1){
                    userStatus.postValue(UserState.ADMIN)
                }
                else{
                    userStatus.postValue(UserState.USER)
                }
                return RequestResult.SUCCESS
            }
            return RequestResult.FAILURE
        }
        return RequestResult.NOT_FOUND
    }

    fun logOut() {
        userId = null
        userStatus.postValue(UserState.NOT_AUTHENTICATED)
    }

    suspend fun initDatabase(password : String) = withContext(Dispatchers.IO) {
        passphrase = password
        tempfile.createNewFile()
        if(!datafile.exists()){
            datafile.createNewFile()
            database = Room.databaseBuilder(PassportApp.instance.applicationContext, AppDatabase::class.java, tempfile.absolutePath).build()
            database?.userDao()?.insertUser(User(0,"admin", ""))
        }
        else{
            decryptDatabase()
            database = Room.databaseBuilder(PassportApp.instance.applicationContext, AppDatabase::class.java, tempfile.absolutePath).build()
        }
        userStatus.postValue(UserState.NOT_AUTHENTICATED)
        Log.i(TAG, "Database decrypted")
    }

    suspend fun closeDatabase() = withContext(Dispatchers.IO) {
        if(database!= null){
            encryptDatabase()
            database = null
            passphrase = ""
            userId = null
            userStatus.postValue(UserState.NOT_DECRYPTED)
            Log.i(TAG, "Database encrypted")
        }
    }

    private fun encryptDatabase() {
        tempfile.copyTo(datafile,true)
        tempfile.delete()
    }

    private fun decryptDatabase() {
        datafile.copyTo(tempfile, true)
    }

    companion object {

        private val TAG = UserRepository::class.java.name
        private const val DATAFILE_NAME = "passport.db"
        private const val TEMPFILE_NAME = "temp.db"

        @Volatile
        private var instance: UserRepository? = null

        fun get() =
            instance ?: synchronized(this) {
                instance ?: UserRepository().also { instance = it }
            }

    }
}