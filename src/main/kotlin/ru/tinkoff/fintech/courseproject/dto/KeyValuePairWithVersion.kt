package ru.tinkoff.fintech.courseproject.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class KeyValuePairWithVersion(
    val key: String,
    val value: String,
    val revision: Int,
    @JsonProperty("updated_at")
    val updateTime: LocalDateTime
)
