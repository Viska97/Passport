package org.visapps.passport.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import org.visapps.passport.R
import org.visapps.passport.ui.viewmodel.SplashActivityViewModel
import org.visapps.passport.util.UserStatus

class SplashActivity : AppCompatActivity() {

    private lateinit var viewModel : SplashActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        viewModel = ViewModelProviders.of(this).get(SplashActivityViewModel::class.java)
        viewModel.userStatus.observe(this, Observer<UserStatus>{
            when(it){
                UserStatus.NEED_CHANGE -> {openChangeActivity()}
                UserStatus.IN_PROGRESS -> {}
                UserStatus.QUIT -> finish()
                UserStatus.FIRST_RUN -> openEncryptActivity()
                UserStatus.NOT_DECRYPTED -> openEncryptActivity()
                UserStatus.NOT_AUTHENTICATED -> openLoginActivity()
                else -> openMainActivity()
            }
        })
    }

    override fun onDestroy() {
        if(isFinishing){
            viewModel.quit()
        }
        super.onDestroy()
    }

    private fun openChangeActivity() {
        val intent = Intent(this, ChangePasswordActivity::class.java)
        startActivity(intent)
    }

    private fun openEncryptActivity() {
        val intent = Intent(this, EncryptActivity::class.java)
        startActivity(intent)
    }

    private fun openLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    private fun openMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

}
