package org.visapps.passport.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import org.visapps.passport.R
import org.visapps.passport.ui.viewmodel.NewUserViewModel
import org.visapps.passport.util.toVisibility
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.visapps.passport.util.afterTextChanged

class NewUserDialog : DialogFragment() {

    val TAG = "NewUserDialog"

    private lateinit var viewModel: NewUserViewModel
    private lateinit var fieldlayout : LinearLayout
    private lateinit var progressBar : ProgressBar
    private lateinit var add : Button
    private lateinit var cancel : Button
    private lateinit var username_layout : TextInputLayout
    private lateinit var username : TextInputEditText


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.newuser_dialog, container, false)
        fieldlayout = view.findViewById(R.id.fieldlayout)
        progressBar = view.findViewById(R.id.progressBar)
        add = view.findViewById(R.id.add)
        cancel = view.findViewById(R.id.cancel)
        username_layout = view.findViewById(R.id.username_layout)
        username = view.findViewById(R.id.username)
        username.afterTextChanged {
            username_layout.error = null
        }
        add.setOnClickListener {
            viewModel.addUser(username.text.toString())
        }
        cancel.setOnClickListener {
            this.dismiss()
        }
        dialog?.setTitle(getString(R.string.add))
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(NewUserViewModel::class.java)
        viewModel.loading.observe(this, Observer<Boolean> {
            fieldlayout.visibility = toVisibility(!it)
            progressBar.visibility = toVisibility(it)
        })
        viewModel.alert.observe(this, Observer<Int>{
            username_layout.error = getString(it)
        })
        viewModel.result.observe(this,Observer<Unit>{
            Toast.makeText(requireActivity(), getString(R.string.user_added), Toast.LENGTH_SHORT).show()
            this.dismiss()
        })
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}