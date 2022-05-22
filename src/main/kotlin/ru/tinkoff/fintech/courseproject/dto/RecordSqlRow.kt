package ru.tinkoff.fintech.courseproject.dto

import java.time.LocalDateTime

data class RecordSqlRow(
    val name: String,
    val email: String,
    val phoneNumber: String,
    val key: String,
    val value: String,
    val revision: Int,
    val updateTime: LocalDateTime
)
