package ru.tinkoff.fintech.courseproject.client

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject
import ru.tinkoff.fintech.courseproject.dto.ClientResponse

@Service
class PhoneValidationClient(
    private val restTemplate: RestTemplate,
    @Value("\${validator.address}") private val phoneValidatorAddress: String,
    @Value("\${validator.token}") private val phoneValidatorToken: String
) {

    fun validate(phoneNumber: String): ClientResponse =
        restTemplate.getForObject(
            "$phoneValidatorAddress$GET_VALIDATION_BY_NUMBER",
            phoneValidatorToken,
            phoneNumber
        )
}

private const val GET_VALIDATION_BY_NUMBER = "/?api_key={token}&phone={phoneNumber}"
