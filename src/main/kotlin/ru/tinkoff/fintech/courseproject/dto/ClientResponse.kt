package ru.tinkoff.fintech.courseproject.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ClientResponse(val valid: Boolean)
