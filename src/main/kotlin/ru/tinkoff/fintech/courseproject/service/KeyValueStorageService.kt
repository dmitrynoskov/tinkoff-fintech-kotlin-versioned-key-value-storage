package ru.tinkoff.fintech.courseproject.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.tinkoff.fintech.courseproject.dto.MultiUpdateRequest
import ru.tinkoff.fintech.courseproject.dto.SingleUpdateRequest
import ru.tinkoff.fintech.courseproject.dto.UserResponseWithKV
import ru.tinkoff.fintech.courseproject.exception.DuplicateKeysException
import ru.tinkoff.fintech.courseproject.exception.NoSuchKeyExistsException
import ru.tinkoff.fintech.courseproject.exception.NoSuchUserExistsException
import ru.tinkoff.fintech.courseproject.repository.KVRepository
import ru.tinkoff.fintech.courseproject.repository.UserRepository
import ru.tinkoff.fintech.courseproject.util.findDuplicateKeys
import ru.tinkoff.fintech.courseproject.util.mapToUserResponseWithKV
import ru.tinkoff.fintech.courseproject.util.toListSingleRequest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class KeyValueStorageService(
    private val kvRepository: KVRepository,
    private val userRepository: UserRepository,
    @Value("\${jackson-datetime-format}") private val dateTimePattern: String,
    @Value("\${key-history-limit:}") private val keyHistoryLimit: Int?
) {

    @Transactional
    fun addSingleKV(singleUpdateRequest: SingleUpdateRequest) {
        userRepository.getUser(singleUpdateRequest.phoneNumber)
            ?: throw NoSuchUserExistsException(singleUpdateRequest.phoneNumber)
        kvRepository.saveKV(singleUpdateRequest, LocalDateTime.now())
        keyHistoryLimit?.let {
            kvRepository.trimKeyHistory(
                singleUpdateRequest.phoneNumber,
                singleUpdateRequest.key,
                keyHistoryLimit
            )
        }
    }

    @Transactional
    fun addMultiKV(multiUpdateRequest: MultiUpdateRequest) {
        userRepository.getUser(multiUpdateRequest.phoneNumber)
            ?: throw NoSuchUserExistsException(multiUpdateRequest.phoneNumber)
        if (multiUpdateRequest.findDuplicateKeys().isNotEmpty()) {
            throw DuplicateKeysException(multiUpdateRequest.findDuplicateKeys())
        }
        multiUpdateRequest.toListSingleRequest().forEach { kvRepository.saveKV(it, LocalDateTime.now()) }
    }

    @Transactional(readOnly = true)
    fun getLatestKV(phoneNumber: String): UserResponseWithKV {
        val userResponse = userRepository.getUser(phoneNumber) ?: throw NoSuchUserExistsException(phoneNumber)
        val sqlRecordList = kvRepository.getLatestKV(phoneNumber)
        return mapToUserResponseWithKV(userResponse, sqlRecordList)
    }

    @Transactional(readOnly = true)
    fun getKVOnTime(phoneNumber: String, time: String): UserResponseWithKV {
        val userResponse = userRepository.getUser(phoneNumber) ?: throw NoSuchUserExistsException(phoneNumber)
        val sqlRecordList = kvRepository.getKVOnTime(
            phoneNumber,
            LocalDateTime.parse(time, DateTimeFormatter.ofPattern(dateTimePattern))
        )
        return mapToUserResponseWithKV(userResponse, sqlRecordList)
    }

    @Transactional(readOnly = true)
    fun getHistoryKV(phoneNumber: String, key: String, page: Int, perPage: Int): UserResponseWithKV {
        val userResponse = userRepository.getUser(phoneNumber) ?: throw NoSuchUserExistsException(phoneNumber)
        val sqlRecordList = kvRepository.getHistoryKV(
            phoneNumber, key, page, perPage
        )
        return mapToUserResponseWithKV(userResponse, sqlRecordList)
    }

    @Transactional
    fun deleteKV(phoneNumber: String, key: String) {
        userRepository.getUser(phoneNumber) ?: throw NoSuchUserExistsException(phoneNumber)
        if (!kvRepository.deleteKV(phoneNumber, key)) throw NoSuchKeyExistsException(key)
    }

}
