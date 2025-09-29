package com.aura.ui.home.model

import kotlinx.serialization.Serializable

@Serializable
data class Account(
    val id: String,
    val main: Boolean,
    val balance: Double
)
