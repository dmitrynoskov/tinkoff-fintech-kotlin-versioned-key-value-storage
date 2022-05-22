package ru.tinkoff.fintech.courseproject.configuration

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate
import java.time.Duration

@Configuration
class ServiceConfiguration(
    @Value("\${client.timeout.connect}") private val connectTimeoutInSeconds: Long,
    @Value("\${client.timeout.read}") private val readTimeoutInSeconds: Long
) {

    @Bean
    fun restTemplate(builder: RestTemplateBuilder): RestTemplate = builder
        .setConnectTimeout(Duration.ofSeconds(connectTimeoutInSeconds))
        .setReadTimeout(Duration.ofSeconds(readTimeoutInSeconds))
        .build()

}
