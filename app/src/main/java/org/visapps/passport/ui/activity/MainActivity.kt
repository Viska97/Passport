package org.visapps.passport.ui.activity

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
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.nav_header_main.view.*
import org.visapps.passport.R
import org.visapps.passport.data.User
import org.visapps.passport.ui.viewmodel.MainActivityViewModel
import org.visapps.passport.util.UserState

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var forceExit = false

    private lateinit var viewModel: MainActivityViewModel

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
        viewModel = ViewModelProviders.of(this).get(MainActivityViewModel::class.java)
        viewModel.user.observe(this, Observer<User>{
            name.text = it.name
        })
        viewModel.userStatus.observe(this, Observer<UserState>{
            when(it){
                UserState.FIRST_RUN -> openDecrypt()
                UserState.NOT_DECRYPTED -> openDecrypt()
                UserState.NOT_AUTHENTICATED -> openAuth()
                UserState.USER -> type.text = "USER"
                UserState.ADMIN -> type.text = "ADMIN"
                else -> {}
            }
        })
        viewModel.finish.observe(this, Observer {
            finish()
        })
    }

    private fun openDecrypt() {
        val intent = Intent(this, EncryptActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun openAuth() {

    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {

            }
            R.id.nav_users -> {

            }
            R.id.nav_password -> {

            }
            R.id.nav_exit -> {
                viewModel.closeDatabase()
            }
            R.id.nav_about -> {

            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }
}
