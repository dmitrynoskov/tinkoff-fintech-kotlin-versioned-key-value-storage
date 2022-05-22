package ru.tinkoff.fintech.courseproject.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class UserResponseWithKV(
    val user: UserResponse,
    @JsonProperty("key_value") val records: Array<KeyValuePairWithVersion>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserResponseWithKV

        if (user != other.user) return false
        if (!records.contentEquals(other.records)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = user.hashCode()
        result = 31 * result + records.contentHashCode()
        return result
    }
}
