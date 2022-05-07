package com.example.vegarden.models

data class User(
    val name: String? = null,
    val surname: String? = null,
    val email: String? = null,
    val uid: String? = null,
    var profilePhoto: String? = null,
    var myFriends: List<String> = listOf(),
)
