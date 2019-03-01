package org.visapps.passport.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.android.material.navigation.NavigationView
import androidx.core.view.GravityCompat
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.afollestad.materialdialogs.MaterialDialog
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.nav_header_main.view.*
import org.visapps.passport.R
import org.visapps.passport.data.User
import org.visapps.passport.ui.fragment.AccountsFragment
import org.visapps.passport.ui.fragment.HomeFragment
import org.visapps.passport.ui.fragment.PasswordFragment
import org.visapps.passport.ui.viewmodel.MainActivityViewModel
import org.visapps.passport.util.UserState

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    companion object {
        const val FRAGMENT_HOME = 0
        const val FRAGMENT_ACCOUNTS = 1
        const val FRAGMENT_PASSWORD = 2
    }

    private lateinit var viewModel: MainActivityViewModel
    private var selectedFragment = FRAGMENT_HOME

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        nav_view.setNavigationItemSelectedListener(this)
        val name = nav_view.getHeaderView(0).findViewById<TextView>(R.id.name)
        val type = nav_view.getHeaderView(0).findViewById<TextView>(R.id.type)
        nav_view.menu.getItem(0).isChecked = true
        viewModel = ViewModelProviders.of(this).get(MainActivityViewModel::class.java)
        viewModel.user.observe(this, Observer<User>{
            name.text = it.name
        })
        viewModel.userStatus.observe(this, Observer<UserState>{
            when(it){
                UserState.USER -> type.text = getString(R.string.user)
                UserState.ADMIN -> type.text = getString(R.string.admin)
                else -> openSplashActivity()
            }
        })
        viewModel.logOut.observe(this, Observer {
            setResult(Activity.RESULT_OK)
            finish()
        })
        savedInstanceState?.let {
            selectedFragment = it.getInt("selectedFragment")
        }
        changeFragment()
    }

    private fun openSplashActivity() {
        val intent = Intent(this, SplashActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            MaterialDialog(this).show {
                message(R.string.quit)
                positiveButton(R.string.ok) { _ ->
                    viewModel.logOut()
                }
                negativeButton(R.string.cancel) { dialog ->
                    dialog.dismiss()
                }
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                selectedFragment = FRAGMENT_HOME
                changeFragment()
            }
            R.id.nav_users -> {
                selectedFragment = FRAGMENT_ACCOUNTS
                changeFragment()
            }
            R.id.nav_password -> {
                selectedFragment = FRAGMENT_PASSWORD
                changeFragment()
            }
            R.id.nav_exit -> {
                viewModel.logOut()
            }
            R.id.nav_about -> {
                val intent = Intent(this, AboutActivity::class.java)
                startActivity(intent)
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        //viewModel.logOut()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("selectedFragment", selectedFragment)
    }

    private fun changeFragment() {
        when(selectedFragment){
            FRAGMENT_HOME -> {
                supportFragmentManager.beginTransaction().replace(R.id.main_frame, HomeFragment()).commit()
                supportActionBar?.title = getString(R.string.home)
            }
            FRAGMENT_ACCOUNTS -> {
                supportFragmentManager.beginTransaction().replace(R.id.main_frame, AccountsFragment()).commit()
                supportActionBar?.title = getString(R.string.users_list)
            }
            FRAGMENT_PASSWORD -> {
                supportFragmentManager.beginTransaction().replace(R.id.main_frame, PasswordFragment()).commit()
                supportActionBar?.title = getString(R.string.change_password)
            }
        }
    }
}
