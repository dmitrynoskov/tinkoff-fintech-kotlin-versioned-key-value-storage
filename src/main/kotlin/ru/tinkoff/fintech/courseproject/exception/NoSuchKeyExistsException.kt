package ru.tinkoff.fintech.courseproject.exception

class NoSuchKeyExistsException(key: String) : IllegalArgumentException("no such key $key exists") {
}
