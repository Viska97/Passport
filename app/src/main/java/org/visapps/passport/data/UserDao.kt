package org.visapps.passport.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UserDao {

    @Insert
    fun insertUser(user : User)

    @Query("SELECT * FROM users WHERE id = :id")
    fun getUserById(id : Int) : User

    @Query("SELECT * FROM users WHERE name = :name")
    fun getUserByName(name : String) : User

}