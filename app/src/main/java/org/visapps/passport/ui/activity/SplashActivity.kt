package org.visapps.passport.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import org.visapps.passport.R
import org.visapps.passport.ui.viewmodel.SplashActivityViewModel
import org.visapps.passport.util.UserState

class SplashActivity : AppCompatActivity() {

    companion object {
        const val ENCRYPT = 1
        const val DECRYPT = 2
        const val LOGIN = 3
        const val MAIN = 4
        val TAG = SplashActivity::class.java.name
    }

    private lateinit var viewModel : SplashActivityViewModel
    private var encryptInProgress = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        viewModel = ViewModelProviders.of(this).get(SplashActivityViewModel::class.java)
        viewModel.userStatus.observe(this, Observer<UserState>{
            when(it){
                UserState.QUIT -> {}
                UserState.FIRST_RUN -> openEncryptActivity()
                UserState.NOT_DECRYPTED -> openDecryptActivity()
                UserState.NOT_AUTHENTICATED -> openLoginActivity()
                else -> openMainActivity()
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if((requestCode == ENCRYPT ||
            requestCode == DECRYPT ||
            requestCode == LOGIN ||
            requestCode == MAIN)
            && resultCode != Activity.RESULT_OK){
            viewModel.quit()
            finish()
        }
    }

    override fun onDestroy() {
        if(isFinishing){
            viewModel.closeDatabase()
        }
        super.onDestroy()
    }

    private fun openEncryptActivity() {
        Log.i(TAG, "opening encrypt activity")
        val intent = Intent(this, EncryptActivity::class.java)
        startActivityForResult(intent, ENCRYPT)
    }

    private fun openDecryptActivity() {
        Log.i(TAG, "opening decrypt activity")
        val intent = Intent(this, EncryptActivity::class.java)
        startActivityForResult(intent, DECRYPT)
    }

    private fun openLoginActivity() {
        Log.i(TAG, "opening login activity")
        val intent = Intent(this, LoginActivity::class.java)
        startActivityForResult(intent, LOGIN)
    }

    private fun openMainActivity() {
        Log.i(TAG, "opening main activity")
        val intent = Intent(this, MainActivity::class.java)
        startActivityForResult(intent, MAIN)
    }

}
