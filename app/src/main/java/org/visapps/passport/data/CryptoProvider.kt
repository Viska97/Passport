package org.visapps.passport.data

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.Exception
import java.nio.charset.Charset
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class CryptoProvider(private val keysProvider: KeysProvider) {

    companion object {
        private const val SALT = "SALT"
        private const val IV = "IV"
    }

    private var passphrase : String = ""

    fun setPassphrase(passphrase : String) {
        this.passphrase = passphrase
    }

    fun resetPassphrase(){
        passphrase = ""
    }

    fun encryptDatabase(datafile : File, tempfile : File) : Boolean {
        try{
            val fis = FileInputStream(tempfile)
            val fos = FileOutputStream(datafile, false)
            val salt = keysProvider.createKey(SALT)
            var key = passphrase.toByteArray(Charset.forName("UTF-8")).plus(salt)
            val md5 = MessageDigest.getInstance("MD5")
            key = md5.digest(key)
            key = key.copyOf(16)
            val sks = SecretKeySpec(key, "AES")
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val iv = keysProvider.createKey(IV)
            cipher.init(Cipher.ENCRYPT_MODE, sks, IvParameterSpec(iv))
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
            tempfile.delete()
            return false
        }
    }

    fun decryptDatabase(datafile : File, tempfile : File) : Boolean {
        try{
            val fis = FileInputStream(datafile)
            val fos = FileOutputStream(tempfile, false)
            val salt = keysProvider.getKey(SALT)
            var key = passphrase.toByteArray(Charset.forName("UTF-8")).plus(salt)
            val md5 = MessageDigest.getInstance("MD5")
            key = md5.digest(key)
            key = key.copyOf(16)
            val sks = SecretKeySpec(key, "AES")
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val iv = keysProvider.getKey(IV)
            cipher.init(Cipher.DECRYPT_MODE, sks, IvParameterSpec(iv))
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

}