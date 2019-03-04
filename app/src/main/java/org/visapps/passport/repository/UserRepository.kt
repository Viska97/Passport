package org.visapps.passport.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.visapps.passport.PassportApp
import org.visapps.passport.util.AuthResult
import org.visapps.passport.util.UserStatus
import java.io.File
import java.lang.Exception
import androidx.lifecycle.LiveData
import org.visapps.passport.data.*
import org.visapps.passport.util.PasswordChangeResult
import org.visapps.passport.util.checkPassword

class UserRepository {

    val userStatus = MutableLiveData<UserStatus>()

    private val datafile: File
    private val tempfile: File
    private val cryptoProvider : CryptoProvider
    private var database : AppDatabase? = null
    private var inMemoryDatabase : InMemoryDatabase? = null
    private var userId : Int? = null

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

    init {
        datafile = File(PassportApp.instance.applicationContext.getExternalFilesDir(""), DATAFILE_NAME)
        tempfile = File(PassportApp.instance.applicationContext.getExternalFilesDir(""), TEMPFILE_NAME)
        updateStatus()
        cryptoProvider = CryptoProvider(KeysProvider(PassportApp.instance))
    }

    suspend fun loadUser() : User? {
        userId?.let {
            return withContext(Dispatchers.IO) {inMemoryDatabase?.getUserById(it)}
        }
        return null
    }

    suspend fun addUser(username: String) : Boolean  {
        val user = withContext(Dispatchers.IO) {inMemoryDatabase?.getUserByName(username)}
        user?.let {
            return false
        }
        withContext(Dispatchers.IO) {inMemoryDatabase?.insertUser(User(0,username, ""))}
        return true
    }

    suspend fun updateBlocked(id : Int) = withContext(Dispatchers.IO){
        val user = inMemoryDatabase?.getUserById(id)
        user?.let {
            it.blocked = !it.blocked
            inMemoryDatabase?.update(it)
        }
    }

    suspend fun updateLimit(id : Int)= withContext(Dispatchers.IO){
        val user = inMemoryDatabase?.getUserById(id)
        user?.let {
            it.limited = !it.limited
            inMemoryDatabase?.update(it)
        }
    }

    suspend fun changePassword(oldPassword : String, newPassword : String) : PasswordChangeResult {
        userId?.let {
            val user  = withContext(Dispatchers.IO) {inMemoryDatabase?.getUserById(it)}
            user?.let {
                if(!oldPassword.equals(it.password)){
                    return PasswordChangeResult.INVALID_CURRENT
                }
                val limited = it.limited
                if(!checkPassword(newPassword, limited)){
                    return PasswordChangeResult.INVALID_NEW
                }
                it.password = newPassword
                withContext(Dispatchers.IO) {inMemoryDatabase?.update(it)}
                return PasswordChangeResult.SUCCESS
            }
        }
        return PasswordChangeResult.INVALID_CURRENT
    }

    suspend fun changePassword(newPassword : String) : PasswordChangeResult {
        userId?.let {
            val user  = withContext(Dispatchers.IO) {inMemoryDatabase?.getUserById(it)}
            user?.let {
                val limited = it.limited
                if(!checkPassword(newPassword, limited)){
                    return PasswordChangeResult.INVALID_NEW
                }
                it.password = newPassword
                withContext(Dispatchers.IO) {inMemoryDatabase?.update(it)}
                if(it.id == 1){
                    userStatus.postValue(UserStatus.ADMIN)
                }
                else{
                    userStatus.postValue(UserStatus.USER)
                }
                return PasswordChangeResult.SUCCESS
            }
        }
        return PasswordChangeResult.INVALID_NEW
    }

    fun getUsers() : LiveData<List<User>> {
        return inMemoryDatabase?.getUsers() ?: MutableLiveData<List<User>>()
    }

    fun getUsername() : LiveData<String> {
        userId?.let {
            return inMemoryDatabase?.getUserName(it) ?: MutableLiveData<String>()
        }
        return MutableLiveData<String>()
    }

    suspend fun logIn(username : String, password: String) : AuthResult {
        val user = withContext(Dispatchers.IO){inMemoryDatabase?.getUserByName(username)}
        user?.let{
            if(it.password.equals(password)){
                if(it.blocked){
                    return AuthResult.BLOCKED
                }
                userId = it.id
                val limited = it.limited
                if(!checkPassword(it.password, limited)){
                    userStatus.postValue(UserStatus.NEED_CHANGE)
                }
                else if(it.id == 1){
                    userStatus.postValue(UserStatus.ADMIN)
                }
                else{
                    userStatus.postValue(UserStatus.USER)
                }
                return AuthResult.SUCCESS
            }
            return AuthResult.INVALID_PASSWORD
        }
        return AuthResult.NOT_FOUND
    }

    fun logOut() {
        userId = null
        userStatus.postValue(UserStatus.NOT_AUTHENTICATED)
    }

    fun quitAllStates(){
        updateStatus()
    }

    fun quitEncryptState() {
        if(userStatus.value == UserStatus.NOT_DECRYPTED || userStatus.value == UserStatus.FIRST_RUN){
            userStatus.value = UserStatus.QUIT
        }
    }

    suspend fun quitLogInState() = withContext(Dispatchers.IO){
        if(userStatus.value == UserStatus.NOT_AUTHENTICATED){
            closeDatabase()
        }
    }

    suspend fun initDatabase(password : String) : Boolean  {
        return withContext(Dispatchers.IO){
            userStatus.postValue(UserStatus.IN_PROGRESS)
            tempfile.createNewFile()
            cryptoProvider.setPassphrase(password)
            if(!datafile.exists()){
                datafile.createNewFile()
                database = Room.databaseBuilder(PassportApp.instance.applicationContext, AppDatabase::class.java, tempfile.absolutePath).build()
                database?.userDao()?.insertUser(User(0,"admin", ""))
                database?.let {
                    inMemoryDatabase = InMemoryDatabase(it.userDao().getAll())
                }
                database?.close()
                cryptoProvider.encryptDatabase(datafile, tempfile)
                userStatus.postValue(UserStatus.NOT_AUTHENTICATED)
                Log.i(TAG, "Database decrypted")
                return@withContext true
            }
            else{
                val success = cryptoProvider.decryptDatabase(datafile, tempfile)
                if(success){
                    try{
                        database = Room.databaseBuilder(PassportApp.instance.applicationContext, AppDatabase::class.java, tempfile.absolutePath).build()
                        database?.let {
                            inMemoryDatabase = InMemoryDatabase(it.userDao().getAll())
                        }
                        database?.close()
                        cryptoProvider.encryptDatabase(datafile, tempfile)
                        userStatus.postValue(UserStatus.NOT_AUTHENTICATED)
                        Log.i(TAG, "Database decrypted")
                        return@withContext true
                    }
                    catch (e : Exception){
                        tempfile.delete()
                        Log.i(TAG, "Corrupted database file")
                    }
                }
                userStatus.postValue(UserStatus.NOT_DECRYPTED)
                Log.i(TAG, "Database not decrypted")
                return@withContext false
            }
        }
    }

    suspend fun closeDatabase() = withContext(Dispatchers.IO) {
        inMemoryDatabase?.let {
            if(userStatus.value != UserStatus.IN_PROGRESS ){
                userStatus.postValue(UserStatus.IN_PROGRESS)
                cryptoProvider.decryptDatabase(datafile, tempfile)
                database = Room.databaseBuilder(PassportApp.instance.applicationContext, AppDatabase::class.java, tempfile.absolutePath).build()
                database?.userDao()?.insertUsers(it.getAll())
                database?.close()
                Log.i(TAG, "Database closed")
                cryptoProvider.encryptDatabase(datafile, tempfile)
                inMemoryDatabase = null
                database = null
                cryptoProvider.resetPassphrase()
                userId = null
                updateStatus()
            }
        }
        updateStatus()
    }

    private fun updateStatus() {
        if(datafile.exists()) {
            userStatus.postValue(UserStatus.NOT_DECRYPTED)
        }
        else{
            userStatus.postValue(UserStatus.FIRST_RUN)
        }
    }

}