package com.example.vegarden
import android.util.Patterns

fun String?.isValidEmail() : Boolean = !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()
fun String?.isValidPassword() : Boolean = !isNullOrEmpty() && this.length >= 8