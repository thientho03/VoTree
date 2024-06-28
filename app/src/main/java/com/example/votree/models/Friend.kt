package com.example.votree.models

data class Friend(
    val uid: String,
    val name: String,
    var lastMessage: String?,
    var lastMessageTime: Long?,
    var avatar: String?
)