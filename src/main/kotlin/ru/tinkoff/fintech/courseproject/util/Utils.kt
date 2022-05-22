package ru.tinkoff.fintech.courseproject.util

import ru.tinkoff.fintech.courseproject.dto.*

fun mapMultiRequestToListSingleRequest(multiUpdateRequest: MultiUpdateRequest): List<SingleUpdateRequest> =
    multiUpdateRequest.records.map { SingleUpdateRequest(multiUpdateRequest.phoneNumber, it.key, it.value) }

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

