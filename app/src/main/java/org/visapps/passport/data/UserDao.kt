package org.visapps.passport.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUsers(users : List<User>)

    @Insert
    fun insertUser(user : User)

    @Query("SELECT * FROM users WHERE id = :id")
    fun getUserById(id : Int) : User

    @Query("SELECT * FROM users WHERE name = :name")
    fun getUserByName(name : String) : User

    @Query("SELECT * FROM users ORDER BY id ASC")
    fun getUsers() : LiveData<List<User>>

    @Query("SELECT name FROM users WHERE id = :id")
    fun getUserName(id : Int) : LiveData<String>

    @Query("SELECT * FROM users")
    fun getAll() : List<User>

    @Update
    fun update(user: User)

}