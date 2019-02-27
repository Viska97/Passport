package org.visapps.passport.ui.activity

import android.content.Intent
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import org.visapps.passport.R

import kotlinx.android.synthetic.main.activity_encrypt.*
import kotlinx.android.synthetic.main.content_encrypt.*
import org.visapps.passport.ui.viewmodel.EncryptActivityViewModel
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
        viewModel.finish.observe(this, Observer {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        })
        loginbutton.setOnClickListener {
            viewModel.process(password.text.toString())
        }
    }

}
