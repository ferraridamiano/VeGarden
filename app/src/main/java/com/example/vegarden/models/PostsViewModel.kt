package com.example.vegarden.models

import java.util.Date

data class PostsViewModel(
    val viewType: Int,
    val textOrUrl: String?,
    val timestamp: Date,
    val userNameSurname: String?,
    val postUserUid: String,
)