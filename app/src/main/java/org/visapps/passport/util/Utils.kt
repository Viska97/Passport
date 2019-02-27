package org.visapps.passport.util

import android.view.View

enum class UserState{
    FIRST_RUN,
    NOT_DECRYPTED,
    NOT_AUTHENTICATED,
    USER,
    ADMIN
}

enum class RequestResult{
    SUCCESS,
    FAILURE,
    NOT_FOUND
}

fun toVisibility(constraint : Boolean): Int {
    return if (constraint) {
        View.VISIBLE
    } else {
        View.GONE
    }
}

