package ru.tinkoff.fintech.courseproject.exception

class UserAlreadyRegisteredException(phoneNumber: String) :
    IllegalArgumentException("user with phone number $phoneNumber is already registered!") {
}
