package com.aura.model.login

import kotlinx.serialization.Serializable

/**
 * Modèle représentant un utilisateur.
 */
@Serializable
data class User(
    val id: String,
    val firstName: String,
    val lastName: String,
    val password: String,
    val balance: Double
)
