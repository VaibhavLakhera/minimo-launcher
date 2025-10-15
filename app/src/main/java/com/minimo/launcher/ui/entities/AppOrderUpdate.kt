package com.minimo.launcher.ui.entities

data class AppOrderUpdate(
    val className: String,
    val packageName: String,
    val userHandle: Int,
    val orderIndex: Int
)
