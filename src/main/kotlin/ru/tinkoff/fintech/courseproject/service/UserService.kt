package ru.tinkoff.fintech.courseproject.service

import org.springframework.stereotype.Service
import ru.tinkoff.fintech.courseproject.client.PhoneValidationClient
import ru.tinkoff.fintech.courseproject.dto.UserRequest
import ru.tinkoff.fintech.courseproject.exception.BadNumberException
import ru.tinkoff.fintech.courseproject.exception.NoSuchUserExistsException
import ru.tinkoff.fintech.courseproject.exception.UserAlreadyRegisteredException
import ru.tinkoff.fintech.courseproject.repository.UserRepository

@Service
class UserService(
    private val userRepository: UserRepository,
    private val client: PhoneValidationClient
) {

    fun addUser(userRequest: UserRequest) {
        if (userRequest.phoneNumber.isBlank() || !client.validate(userRequest.phoneNumber).valid) {
            throw BadNumberException(userRequest.phoneNumber)
        }
        if (!userRepository.saveUser(userRequest)) throw UserAlreadyRegisteredException(userRequest.phoneNumber)
    }

    fun deleteUser(phoneNumber: String) {
        if (!userRepository.deleteUser(phoneNumber)) throw NoSuchUserExistsException(phoneNumber)
    }

    fun getAllUsers(page: Int, perPage: Int) = userRepository.getAllUsers(page, perPage)

}
