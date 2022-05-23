package ru.tinkoff.fintech.courseproject.exception

class BadNumberException(phoneNumber: String) : IllegalArgumentException("phone number $phoneNumber is not valid!") {
}
