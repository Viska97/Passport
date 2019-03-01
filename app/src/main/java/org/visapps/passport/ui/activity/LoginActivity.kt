package org.visapps.passport.ui.activity

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.afollestad.materialdialogs.MaterialDialog
import org.visapps.passport.R

import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.content_login.*
import org.visapps.passport.ui.viewmodel.LoginActivityViewModel
import org.visapps.passport.util.toVisibility

class LoginActivity : AppCompatActivity() {

    private lateinit var viewModel: LoginActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setSupportActionBar(toolbar)
        viewModel = ViewModelProviders.of(this).get(LoginActivityViewModel::class.java)
        viewModel.loading.observe(this, Observer<Boolean> {
            fieldlayout.visibility = toVisibility(!it)
            progressBar.visibility = toVisibility(it)
        })
        viewModel.invalidUsername.observe(this, Observer<Unit> {
            MaterialDialog(this).show {
                message(R.string.invalid_username)
                positiveButton(R.string.ok)
            }
        })
        viewModel.invalidPassword.observe(this, Observer<Int>{
            MaterialDialog(this).show {
                message(text = getString(R.string.invalid_password, it))
                positiveButton(R.string.ok)
            }
        })
        viewModel.userBlocked.observe(this, Observer<Unit> {
            MaterialDialog(this).show {
                message(R.string.user_blocked)
                positiveButton(R.string.ok)
            }
        })
        viewModel.result.observe(this, Observer<Unit>{
            setResult(Activity.RESULT_OK)
            finish()
        })
        loginbutton.setOnClickListener {
            viewModel.logIn(login.text.toString(), password.text.toString())
        }
        exit.setOnClickListener {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}
