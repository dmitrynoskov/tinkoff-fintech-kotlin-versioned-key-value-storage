package ru.tinkoff.fintech.courseproject.client

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject
import ru.tinkoff.fintech.courseproject.dto.ClientResponse
import ru.tinkoff.fintech.courseproject.exception.TokenExpiredException

@Service
class PhoneValidationClient(
    private val restTemplate: RestTemplate,
    @Value("\${validator.address}") private val phoneValidatorAddress: String,
    @Value("\${validator.token}") private val phoneValidatorToken: String
) {

    fun validate(phoneNumber: String): ClientResponse =
        try {
            restTemplate.getForObject(
                "$phoneValidatorAddress$GET_VALIDATION_BY_NUMBER",
                phoneValidatorToken,
                phoneNumber
            )
        } catch (e: HttpClientErrorException.BadRequest) {
            throw TokenExpiredException()
        }

}

private const val GET_VALIDATION_BY_NUMBER = "/?api_key={token}&phone={phoneNumber}"
