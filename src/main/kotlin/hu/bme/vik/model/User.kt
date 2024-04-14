package hu.bme.vik.model

import kotlinx.serialization.Serializable

@Serializable
data class User(val username: String, val hashedPassword: String)