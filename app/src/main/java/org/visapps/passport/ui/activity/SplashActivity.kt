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

    private val TAG = SplashActivity::class.java.name

    private lateinit var viewModel : SplashActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        viewModel = ViewModelProviders.of(this).get(SplashActivityViewModel::class.java)
        viewModel.userStatus.observe(this, Observer<UserState>{
            when(it){
                UserState.IN_PROGRESS -> {}
                UserState.QUIT -> finish()
                UserState.FIRST_RUN -> openEncryptActivity()
                UserState.NOT_DECRYPTED -> openDecryptActivity()
                UserState.NOT_AUTHENTICATED -> openLoginActivity()
                else -> openMainActivity()
            }
        })
    }

    override fun onDestroy() {
        if(isFinishing){

        }
        super.onDestroy()
    }

    private fun openEncryptActivity() {
        Log.i(TAG, "opening encrypt activity")
        val intent = Intent(this, EncryptActivity::class.java)
        startActivity(intent)
    }

    private fun openDecryptActivity() {
        Log.i(TAG, "opening decrypt activity")
        val intent = Intent(this, EncryptActivity::class.java)
        startActivity(intent)
    }

    private fun openLoginActivity() {
        Log.i(TAG, "opening login activity")
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    private fun openMainActivity() {
        Log.i(TAG, "opening main activity")
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

}
