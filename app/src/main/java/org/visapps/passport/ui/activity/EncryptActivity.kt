package org.visapps.passport.ui.activity

import android.content.Intent
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.afollestad.materialdialogs.MaterialDialog
import org.visapps.passport.R

import kotlinx.android.synthetic.main.activity_encrypt.*
import kotlinx.android.synthetic.main.content_encrypt.*
import org.visapps.passport.ui.viewmodel.EncryptActivityViewModel
import org.visapps.passport.util.afterTextChanged
import org.visapps.passport.util.toVisibility

class EncryptActivity : AppCompatActivity() {

    private lateinit var viewModel: EncryptActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_encrypt)
        setSupportActionBar(toolbar)
        viewModel = ViewModelProviders.of(this).get(EncryptActivityViewModel::class.java)
        viewModel.loading.observe(this, Observer<Boolean> {
            fieldlayout.visibility = toVisibility(!it)
            progressBar.visibility = toVisibility(it)
        })
        viewModel.firstRun.observe(this, Observer<Boolean>{
            if(it){
                message.text = getString(R.string.set_master_password)
            }
            else{
                message.text = getString(R.string.enter_master_password)
            }
        })
        viewModel.weakPassword.observe(this, Observer<Unit> {
            password_layout.error = getString(R.string.weak_password)
        })
        viewModel.invalidPassword.observe(this, Observer<Unit> {
            MaterialDialog(this).show {
                message(R.string.invalid_master_password)
                positiveButton(R.string.ok)
            }
        })
        viewModel.finish.observe(this, Observer {
            finish()
        })
        password.afterTextChanged {
            password_layout.error = null
        }
        loginbutton.setOnClickListener {
            viewModel.process(password.text.toString())
        }
    }

}
