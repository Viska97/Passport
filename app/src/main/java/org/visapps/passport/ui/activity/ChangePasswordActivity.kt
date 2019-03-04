package org.visapps.passport.ui.activity

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import org.visapps.passport.R

import kotlinx.android.synthetic.main.activity_change_password.*
import kotlinx.android.synthetic.main.content_change_password.*
import org.visapps.passport.ui.viewmodel.ChangePasswordViewModel
import org.visapps.passport.util.afterTextChanged
import org.visapps.passport.util.toVisibility

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var viewModel : ChangePasswordViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)
        setSupportActionBar(toolbar)
        viewModel = ViewModelProviders.of(this).get(ChangePasswordViewModel::class.java)
        viewModel.logOut.observe(this, Observer<Unit> {
            setResult(Activity.RESULT_OK)
            finish()
        })
        viewModel.loading.observe(this, Observer<Boolean> {
            fieldlayout.visibility = toVisibility(!it)
            progressBar.visibility = toVisibility(it)
        })
        viewModel.passwordsDoNotMatch.observe(this, Observer<Unit> {
            password_layout_repeat.error = getString(R.string.passwords_not_match)
        })
        viewModel.weakPassword.observe(this, Observer<Unit> {
            password_layout.error = getString(R.string.weak_password)
        })
        viewModel.limitedPassword.observe(this, Observer<Unit> {
            password_layout.error = getString(R.string.limited_password)
        })
        viewModel.success.observe(this, Observer<Unit> {
            finish()
        })
        change.setOnClickListener {
            viewModel.changePassword(password.text.toString(), repeat_password.text.toString())
        }
        cancel.setOnClickListener {
            viewModel.logOut()
        }
        password.afterTextChanged {
            password_layout.error = null
        }
        repeat_password.afterTextChanged {
            password_layout_repeat.error = null
        }
    }

    override fun onBackPressed() {
        viewModel.logOut()
    }

}
