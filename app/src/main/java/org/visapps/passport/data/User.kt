package org.visapps.passport.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    var id : Int,
    var name : String,
    var password : String,
    var blocked : Boolean = false,
    var limited : Boolean = true
) {
}