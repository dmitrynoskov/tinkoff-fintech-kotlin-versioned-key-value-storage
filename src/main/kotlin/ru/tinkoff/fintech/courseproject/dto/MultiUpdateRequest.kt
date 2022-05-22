package ru.tinkoff.fintech.courseproject.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class MultiUpdateRequest(
    @JsonProperty("phone_number") val phoneNumber: String,
    @JsonProperty("key_value") val records: Array<KeyValuePair>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MultiUpdateRequest

        if (phoneNumber != other.phoneNumber) return false
        if (!records.contentEquals(other.records)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = phoneNumber.hashCode()
        result = 31 * result + records.contentHashCode()
        return result
    }
}
