package org.visapps.passport.ui.fragment

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.accounts_fragment.*

import org.visapps.passport.R
import org.visapps.passport.data.User
import org.visapps.passport.ui.adapter.UsersAdapter
import org.visapps.passport.util.toVisibility

class AccountsFragment : Fragment() {

    companion object {
        fun newInstance() = AccountsFragment()
    }

    private lateinit var viewModel: AccountsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.accounts_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(AccountsViewModel::class.java)
        initAdapter()
        fab.setOnClickListener {
            val dialog = NewUserDialog()
            dialog.show(childFragmentManager, dialog.TAG)
        }
    }

    private fun initAdapter() {
        val adapter = UsersAdapter({
            viewModel.updateBlocked(it)
        },{
            viewModel.updateLimit(it)
        })
        accounts_list.adapter = adapter
        viewModel.users.observe(this, Observer<List<User>>{
            adapter.updateUsers(it)
        })
        viewModel.admin.observe(this, Observer<Boolean> {
            alert.visibility = toVisibility(!it)
            fab.visibility = toVisibility(it)
            accounts_list.visibility = toVisibility(it)
        })
    }

}
