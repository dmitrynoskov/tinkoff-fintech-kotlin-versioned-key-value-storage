package ru.tinkoff.fintech.courseproject.controller

import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.tinkoff.fintech.courseproject.dto.MultiUpdateRequest
import ru.tinkoff.fintech.courseproject.dto.SingleUpdateRequest
import ru.tinkoff.fintech.courseproject.dto.UserResponseWithKV
import ru.tinkoff.fintech.courseproject.service.KeyValueStorageService

@RestController
@RequestMapping("/kv")
class KVController(
    private val keyValueService: KeyValueStorageService,
    @Value("\${default-page-size}") private val defaultPageSize: Int
) {

    @PostMapping("/single")
    fun addSingleKV(@RequestBody singleUpdateRequest: SingleUpdateRequest) =
        keyValueService.addSingleKV(singleUpdateRequest);

    @PostMapping("/multi")
    fun addMultiKV(@RequestBody multiUpdateRequest: MultiUpdateRequest) =
        keyValueService.addMultiKV(multiUpdateRequest);

    @GetMapping("/{phoneNumber}")
    fun getKVOnTime(@PathVariable phoneNumber: String, @RequestParam("time") time: String?): UserResponseWithKV =
        if (time == null) {
            keyValueService.getLatestKV(phoneNumber)
        } else {
            keyValueService.getKVOnTime(phoneNumber, time)
        }

    @GetMapping("/history/{phoneNumber}")
    fun getHistoryKV(
        @PathVariable phoneNumber: String,
        @RequestParam("key") key: String,
        @RequestParam("page") page: Int?,
        @RequestParam("per_page") perPage: Int?
    ): UserResponseWithKV {
        val actualPage = page ?: 0
        val actualPerPage = perPage ?: defaultPageSize
        return keyValueService.getHistoryKV(phoneNumber, key, actualPage, actualPerPage)
    }

    @DeleteMapping("/{phoneNumber}")
    fun deleteKV(@PathVariable phoneNumber: String, @RequestParam("key") key: String) =
        keyValueService.deleteKV(phoneNumber, key)

}
