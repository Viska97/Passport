package org.visapps.passport.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
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
    }

    private lateinit var viewModel : SplashActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        viewModel = ViewModelProviders.of(this).get(SplashActivityViewModel::class.java)
        viewModel.userStatus.observe(this, Observer<UserState>{
            when(it){
                UserState.FIRST_RUN -> openEncryptActivity()
                UserState.NOT_DECRYPTED -> openDecryptActivity()
                UserState.NOT_AUTHENTICATED -> openLoginActivity()
                else -> openMainActivity()
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == ENCRYPT || requestCode == DECRYPT || requestCode == LOGIN || requestCode == MAIN){
            if(resultCode == Activity.RESULT_OK){
                Log.i("Vasily", "result ok")
            }
            else{
                Log.i("Vasily", "result finish")
                finish()
            }
        }
        else{
            finish()
        }
    }

    override fun onDestroy() {
        viewModel.closeDatabase()
        super.onDestroy()
    }

    private fun openEncryptActivity() {
        val intent = Intent(this, EncryptActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        overridePendingTransition(0, 0);
        startActivityForResult(intent, ENCRYPT)
    }

    private fun openDecryptActivity() {
        val intent = Intent(this, EncryptActivity::class.java)
        startActivityForResult(intent, DECRYPT)
    }

    private fun openLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivityForResult(intent, LOGIN)
    }

    private fun openMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivityForResult(intent, MAIN)
    }

}
