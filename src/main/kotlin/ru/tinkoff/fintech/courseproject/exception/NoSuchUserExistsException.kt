package ru.tinkoff.fintech.courseproject.exception

class NoSuchUserExistsException(phoneNumber: String) :
    IllegalArgumentException("user with phone number $phoneNumber is not registered!") {
}
