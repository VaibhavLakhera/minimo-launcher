package com.minimo.launcher.ui.entities

import com.minimo.launcher.data.entities.AppItemType

data class AppOrderUpdate(
    val packageName: String,
    val itemType: AppItemType,
    val targetId: String,
    val userHandle: Int,
    val orderIndex: Int
)
