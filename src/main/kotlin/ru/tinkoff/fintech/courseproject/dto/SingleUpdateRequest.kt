package ru.tinkoff.fintech.courseproject.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class SingleUpdateRequest(
    @JsonProperty("phone_number") val phoneNumber: String,
    val key: String,
    val value: String
)
