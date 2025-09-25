package com.aura.model.login

import kotlinx.serialization.Serializable

@Serializable
data class Credentials(
    val id: String,
    val password: String
)
