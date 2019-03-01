package org.visapps.passport.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.visapps.passport.R
import org.visapps.passport.data.User

class UserItemViewHolder(private val view: View, private val lockListener: (Int)->Unit, private val passwordListener: (Int)->Unit) :
    RecyclerView.ViewHolder(view){

    private val username = view.findViewById<TextView>(R.id.username)
    private val lock = view.findViewById<Switch>(R.id.lock)
    private val password = view.findViewById<CheckBox>(R.id.password)

    private var user : User? = null

    init{
        lock.setOnClickListener{
            user?.let {
                lockListener.invoke(it.id)
            }
        }
        password.setOnClickListener {
            user?.let {
                passwordListener.invoke(it.id)
            }
        }
    }

    fun bind(user: User?) {
        this.user = user
        user?.let {
            username.text = it.name
            lock.isChecked = !it.blocked
            password.isChecked = it.limited
            if(it.id == 1){
                lock.isEnabled = false
                password.isEnabled = false
            }
        }
    }

    companion object {
        fun create(parent: ViewGroup, lockListener: (Int)->Unit, passwordListener: (Int)->Unit): UserItemViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.user_item, parent, false)
            return UserItemViewHolder(view, lockListener, passwordListener)
        }
    }

}