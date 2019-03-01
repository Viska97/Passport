package org.visapps.passport.repository

import android.annotation.SuppressLint
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
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.Exception
import java.nio.charset.Charset
import java.security.MessageDigest
import java.util.*
import javax.crypto.Cipher
import javax.crypto.CipherOutputStream
import javax.crypto.spec.SecretKeySpec
import android.R.attr.key
import androidx.lifecycle.LiveData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.visapps.passport.util.PasswordChangeResult
import org.visapps.passport.util.checkPassword
import javax.crypto.CipherInputStream


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
        updateStatus()
    }

    suspend fun loadUser() : User? {
        userId?.let {
            return withContext(Dispatchers.IO) {database?.userDao()?.getUserById(it)}
        }
        return null
    }

    suspend fun addUser(username: String) : Boolean  {
        val user = withContext(Dispatchers.IO) {database?.userDao()?.getUserByName(username)}
        user?.let {
            return false
        }
        withContext(Dispatchers.IO) {database?.userDao()?.insertUser(User(0,username, ""))}
        return true
    }

    suspend fun updateBlocked(id : Int) = withContext(Dispatchers.IO){
        val user = database?.userDao()?.getUserById(id)
        user?.let {
            it.blocked = !it.blocked
            database?.userDao()?.update(it)
        }
    }

    suspend fun updateLimit(id : Int)= withContext(Dispatchers.IO){
        val user = database?.userDao()?.getUserById(id)
        user?.let {
            it.limited = !it.limited
            database?.userDao()?.update(it)
        }
    }

    suspend fun changePassword(oldPassword : String, newPassword : String) : PasswordChangeResult {
        userId?.let {
            val user  = withContext(Dispatchers.IO) {database?.userDao()?.getUserById(it)}
            user?.let {
                if(!oldPassword.equals(it.password)){
                    return PasswordChangeResult.INVALID_CURRENT
                }
                val limited = it.limited
                Log.i(TAG, limited.toString())
                if(!checkPassword(newPassword, limited)){
                    Log.i(TAG, "PasswordChangeResult.INVALID_NEW")
                    return PasswordChangeResult.INVALID_NEW
                }
                it.password = newPassword
                withContext(Dispatchers.IO) {database?.userDao()?.update(it)}
                return PasswordChangeResult.SUCCESS
            }
        }
        return PasswordChangeResult.INVALID_CURRENT
    }

    fun getUsers() : LiveData<List<User>> {
        return database?.userDao()?.getUsers() ?: MutableLiveData<List<User>>()
    }

    fun getUsername() : LiveData<String> {
        userId?.let {
            return database?.userDao()?.getUserName(it) ?: MutableLiveData<String>()
        }
        return MutableLiveData<String>()
    }

    suspend fun logIn(username : String, password: String) : RequestResult {
        val user = withContext(Dispatchers.IO){database?.userDao()?.getUserByName(username)}
        user?.let{
            if(it.password.equals(password)){
                if(it.blocked){
                    return RequestResult.BLOCKED
                }
                userId = it.id
                if(it.id == 1){
                    userStatus.postValue(UserState.ADMIN)
                }
                else{
                    userStatus.postValue(UserState.USER)
                }
                return RequestResult.SUCCESS
            }
            return RequestResult.INVALID_PASSWORD
        }
        return RequestResult.NOT_FOUND
    }

    fun logOut() {
        userId = null
        userStatus.postValue(UserState.NOT_AUTHENTICATED)
    }

    fun onQuitEncryptState() {
        if(userStatus.value == UserState.NOT_DECRYPTED || userStatus.value == UserState.FIRST_RUN){
            userStatus.postValue(UserState.QUIT)
        }
    }

    suspend fun onQuitLogInState() = withContext(Dispatchers.IO){
        if(userStatus.value == UserState.NOT_AUTHENTICATED){
            closeDatabase()
        }
    }

    suspend fun initDatabase(password : String) : Boolean  {
        return withContext(Dispatchers.IO){
            userStatus.postValue(UserState.IN_PROGRESS)
            passphrase = password
            tempfile.createNewFile()
            if(!datafile.exists()){
                datafile.createNewFile()
                database = Room.databaseBuilder(PassportApp.instance.applicationContext, AppDatabase::class.java, tempfile.absolutePath).build()
                database?.userDao()?.insertUser(User(0,"admin", ""))
                userStatus.postValue(UserState.NOT_AUTHENTICATED)
                Log.i(TAG, "Database decrypted")
                return@withContext true
            }
            else{
                val success = decryptDatabase()
                if(success){
                    database = Room.databaseBuilder(PassportApp.instance.applicationContext, AppDatabase::class.java, tempfile.absolutePath).build()
                    userStatus.postValue(UserState.NOT_AUTHENTICATED)
                    Log.i(TAG, "Database decrypted")
                    return@withContext true
                }
                else{
                    userStatus.postValue(UserState.NOT_DECRYPTED)
                    Log.i(TAG, "Database not decrypted")
                    return@withContext false
                }
            }
        }
    }

    suspend fun closeDatabase() = withContext(Dispatchers.IO) {
        if(database!= null){
            if(userStatus.value != UserState.IN_PROGRESS ){
                userStatus.postValue(UserState.IN_PROGRESS)
                database?.close()
                Log.i(TAG, "Database closed")
                encryptDatabase()
                database = null
                passphrase = ""
                userId = null
                updateStatus()
            }
        }
        else{
            updateStatus()
        }
    }

    private fun updateStatus() {
        if(tempfile.exists()){
            database = Room.databaseBuilder(PassportApp.instance.applicationContext, AppDatabase::class.java, tempfile.absolutePath).build()
            userStatus.postValue(UserState.NOT_AUTHENTICATED)
        }
        else{
            if(datafile.exists()) {
                userStatus.postValue(UserState.NOT_DECRYPTED)
            }
            else{
                userStatus.postValue(UserState.FIRST_RUN)
            }
        }
    }

    private fun encryptDatabase() : Boolean {
        try{
            val fis = FileInputStream(tempfile)
            val fos = FileOutputStream(datafile, false)
            var key = (SALT + passphrase).toByteArray(Charset.forName("UTF-8"))
            val sha = MessageDigest.getInstance("MD5")
            key = sha.digest(key)
            key = Arrays.copyOf(key, 16)
            val sks = SecretKeySpec(key, "AES")
            val cipher = Cipher.getInstance("AES")
            cipher.init(Cipher.ENCRYPT_MODE, sks)
            val cos = CipherOutputStream(fos, cipher)
            var b: Int? = null
            val d = ByteArray(8)
            while ({ b =  fis.read(d); b }() != -1) {
                b?.let {
                    cos.write(d, 0, it)
                }
            }
            cos.flush()
            cos.close()
            fis.close()
            tempfile.delete()
            return true
        }
        catch(e : Exception){
            return false
        }
    }

    private fun decryptDatabase() : Boolean {
        try{
            val fis = FileInputStream(datafile)
            val fos = FileOutputStream(tempfile, false)
            var key = (SALT + passphrase).toByteArray(Charset.forName("UTF-8"))
            val sha = MessageDigest.getInstance("MD5")
            key = sha.digest(key)
            key = Arrays.copyOf(key, 16)
            val sks = SecretKeySpec(key, "AES")
            val cipher = Cipher.getInstance("AES")
            cipher.init(Cipher.DECRYPT_MODE, sks)
            val cis = CipherInputStream(fis, cipher)
            var b: Int? = null
            val d = ByteArray(8)
            while ({ b =  cis.read(d); b }() != -1) {
                b?.let {
                    fos.write(d, 0, it)
                }
            }
            fos.flush();
            fos.close();
            cis.close();
            return true
        }
        catch(e : Exception){
            tempfile.delete()
            return false
        }
    }

    companion object {

        private val TAG = UserRepository::class.java.name
        private const val DATAFILE_NAME = "passport.db"
        private const val TEMPFILE_NAME = "temp.db"
        private const val SALT = "qK~r)Sg6dB3tzBoJtuCKT+#~m||"

        @Volatile
        private var instance: UserRepository? = null

        fun get() =
            instance ?: synchronized(this) {
                instance ?: UserRepository().also { instance = it }
            }

    }
}