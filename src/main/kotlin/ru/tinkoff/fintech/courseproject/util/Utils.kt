package ru.tinkoff.fintech.courseproject.util

import ru.tinkoff.fintech.courseproject.dto.*

fun MultiUpdateRequest.toListSingleRequest() =
    this.records.map { SingleUpdateRequest(this.phoneNumber, it.key, it.value) }

fun MultiUpdateRequest.findDuplicateKeys() =
    this.records.groupingBy { it.key }.eachCount().filter { it.value > 1 }.keys.toList()

fun mapToUserResponseWithKV(userResponse: UserResponse, sqlRecordList: List<RecordSqlRow>): UserResponseWithKV =
    UserResponseWithKV(
        user = userResponse,
        records = sqlRecordList.map {
            KeyValuePairWithVersion(
                key = it.key,
                value = it.value,
                revision = it.revision,
                updateTime = it.updateTime
            )
        }.toTypedArray()
    )
