package org.visapps.passport.util

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText

enum class UserState{
    QUIT,
    IN_PROGRESS,
    FIRST_RUN,
    NOT_DECRYPTED,
    NOT_AUTHENTICATED,
    USER,
    ADMIN
}

enum class RequestResult{
    SUCCESS,
    INVALID_PASSWORD,
    BLOCKED,
    NOT_FOUND
}

enum class PasswordChangeResult{
    SUCCESS,
    INVALID_CURRENT,
    INVALID_NEW
}

fun toVisibility(constraint : Boolean): Int {
    return if (constraint) {
        View.VISIBLE
    } else {
        View.GONE
    }
}

fun checkPassword(password : String, limited : Boolean) : Boolean {
    val passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[\\-+=*/])(?=\\S+$).{4,}$"
    val passwordMatcher = Regex(passwordPattern)
    if(limited){
        return password.length in 8..50 && passwordMatcher.find(password) != null
    }
    else{
        return password.length in 8..50
    }
}

fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }
    })
}

