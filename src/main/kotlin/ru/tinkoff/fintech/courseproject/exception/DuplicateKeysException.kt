package ru.tinkoff.fintech.courseproject.exception

class DuplicateKeysException(keys: List<String>) : IllegalArgumentException("duplicate keys: ${keys.joinToString()}") {
}
