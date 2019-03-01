package org.visapps.passport.ui.fragment

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.password_fragment.*

import org.visapps.passport.R
import org.visapps.passport.util.afterTextChanged
import org.visapps.passport.util.toVisibility

class PasswordFragment : Fragment() {

    companion object {
        fun newInstance() = PasswordFragment()
    }

    private lateinit var viewModel: PasswordViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.password_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(PasswordViewModel::class.java)
        viewModel.loading.observe(this, Observer<Boolean> {
            fieldlayout.visibility = toVisibility(!it)
            progressBar.visibility = toVisibility(it)
        })
        viewModel.invalidCurrentPassword.observe(this, Observer<Unit> {
            password_layout_old.error = getString(R.string.invalid_current_password)
        })
        viewModel.passwordsDoNotMatch.observe(this, Observer<Unit> {
            password_layout_repeat.error = getString(R.string.passwords_not_match)
        })
        viewModel.weakPassword.observe(this, Observer<Unit> {
            password_layout_new.error = getString(R.string.weak_password)
        })
        viewModel.limitedPassword.observe(this, Observer<Unit> {
            password_layout_new.error = getString(R.string.limited_password)
        })
        viewModel.success.observe(this, Observer<Unit> {
            password_layout_old.error = null
            password_layout_new.error = null
            password_layout_repeat.error = null
            old_password.text = null
            new_password.text = null
            repeat_password.text = null
            Toast.makeText(requireActivity(), getString(R.string.password_changed), Toast.LENGTH_SHORT).show()
        })
        old_password.afterTextChanged {
            password_layout_old.error = null
        }
        new_password.afterTextChanged {
            password_layout_new.error = null
        }
        repeat_password.afterTextChanged {
            password_layout_repeat.error = null
        }
        proceed.setOnClickListener {
            viewModel.changePassword(old_password.text.toString(), new_password.text.toString(), repeat_password.text.toString())
        }
    }

}
