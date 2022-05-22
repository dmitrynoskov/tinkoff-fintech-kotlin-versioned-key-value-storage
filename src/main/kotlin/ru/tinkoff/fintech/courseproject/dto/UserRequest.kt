package ru.tinkoff.fintech.courseproject.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class UserRequest(
    val name: String,
    val email: String,
    @JsonProperty("phone_number") val phoneNumber: String
)
