package ru.tinkoff.fintech.courseproject.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.tinkoff.fintech.courseproject.client.PhoneValidationClient
import ru.tinkoff.fintech.courseproject.dto.MultiUpdateRequest
import ru.tinkoff.fintech.courseproject.dto.SingleUpdateRequest
import ru.tinkoff.fintech.courseproject.dto.UserRequest
import ru.tinkoff.fintech.courseproject.dto.UserResponseWithKV
import ru.tinkoff.fintech.courseproject.exception.BadNumberException
import ru.tinkoff.fintech.courseproject.exception.NoSuchKeyExistsException
import ru.tinkoff.fintech.courseproject.exception.NoSuchUserExistsException
import ru.tinkoff.fintech.courseproject.util.mapMultiRequestToListSingleRequest
import ru.tinkoff.fintech.courseproject.util.mapToUserResponseWithKV
import ru.tinkoff.fintech.courseproject.repository.JdbcRepository
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class KeyValueStorageService(
    private val repository: JdbcRepository,
    private val client: PhoneValidationClient
) {

    fun addUser(userRequest: UserRequest) {
        if (userRequest.phoneNumber.isEmpty() || !client.validate(userRequest.phoneNumber).valid) {
            throw BadNumberException(userRequest.phoneNumber)
        }
        if (!repository.saveUser(userRequest)) throw IllegalArgumentException("aaa")
    }

    @Transactional
    fun addSingleKV(singleUpdateRequest: SingleUpdateRequest) {
        repository.getUser(singleUpdateRequest.phoneNumber)
            ?: throw NoSuchUserExistsException(singleUpdateRequest.phoneNumber)
        repository.saveKV(singleUpdateRequest, LocalDateTime.now())
    }

    @Transactional
    fun addMultiKV(multiUpdateRequest: MultiUpdateRequest) {
        repository.getUser(multiUpdateRequest.phoneNumber)
            ?: throw NoSuchUserExistsException(multiUpdateRequest.phoneNumber)
        mapMultiRequestToListSingleRequest(multiUpdateRequest).forEach { repository.saveKV(it, LocalDateTime.now()) }
    }

    @Transactional(readOnly = true)
    fun getLatestKV(phoneNumber: String): UserResponseWithKV {
        val userResponse = repository.getUser(phoneNumber) ?: throw NoSuchUserExistsException(phoneNumber)
        val sqlRecordList = repository.getLatestKV(phoneNumber)
        return mapToUserResponseWithKV(userResponse, sqlRecordList)
    }

    @Transactional(readOnly = true)
    fun getKVOnTime(phoneNumber: String, time: String): UserResponseWithKV {
        val userResponse = repository.getUser(phoneNumber) ?: throw NoSuchUserExistsException(phoneNumber)
        val sqlRecordList = repository.getKVOnTime(
            phoneNumber,
            LocalDateTime.parse(time, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
        )
        return mapToUserResponseWithKV(userResponse, sqlRecordList)
    }

    @Transactional(readOnly = true)
    fun getHistoryKV(phoneNumber: String, key: String, page: Int, perPage: Int): UserResponseWithKV {
        val userResponse = repository.getUser(phoneNumber) ?: throw NoSuchUserExistsException(phoneNumber)
        val sqlRecordList = repository.getHistoryKV(
            phoneNumber, key, page, perPage
        )
        return mapToUserResponseWithKV(userResponse, sqlRecordList)
    }

    fun deleteUser(phoneNumber: String) {
        if (!repository.deleteUser(phoneNumber)) throw NoSuchUserExistsException(phoneNumber)
    }

    fun deleteKV(phoneNumber: String, key: String) {
        repository.getUser(phoneNumber) ?: throw NoSuchUserExistsException(phoneNumber)
        if (!repository.deleteKV(phoneNumber, key)) throw NoSuchKeyExistsException(key)
    }

}
