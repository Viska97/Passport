package org.visapps.passport.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import java.security.SecureRandom

class KeysProvider(val context : Context) {

    companion object {
        private const val NAME = "keys"
    }

    private val sharedPreferences : SharedPreferences = context.getSharedPreferences(NAME,0)

    fun createKey(name : String) : ByteArray {
        val random = SecureRandom()
        val key = ByteArray(16)
        random.nextBytes(key)
        val encodedKey = Base64.encodeToString(key, Base64.DEFAULT);
        sharedPreferences.edit().putString(name, encodedKey).apply()
        return key
    }

    fun getKey(name : String) : ByteArray {
        val encodedKey = sharedPreferences.getString(name, "")
        return Base64.decode(encodedKey, Base64.DEFAULT)
    }

}