package com.aura.model.login

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val granted: Boolean
)
