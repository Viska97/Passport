package org.visapps.passport.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import org.visapps.passport.data.User

class UsersAdapter(private val lockListener: (Int)->Unit, private val passwordListener: (Int)->Unit) : RecyclerView.Adapter<UserItemViewHolder>(){

    private val users = mutableListOf<User>()

    override fun getItemCount() = users.size

    override fun onBindViewHolder(holder: UserItemViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserItemViewHolder {
        return UserItemViewHolder.create(parent, lockListener, passwordListener)
    }

    fun updateUsers(users : List<User>) {
        val diffCallback = UserDiffCallback(this.users, users)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        this.users.clear()
        this.users.addAll(users)
        diffResult.dispatchUpdatesTo(this)
    }

}