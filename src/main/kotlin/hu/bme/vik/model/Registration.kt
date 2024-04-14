package hu.bme.vik.model

import kotlinx.serialization.Serializable

@Serializable
data class Registration(val username: String, val password: String)