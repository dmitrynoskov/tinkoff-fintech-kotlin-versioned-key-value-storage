package ru.tinkoff.fintech.courseproject.controller

import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.tinkoff.fintech.courseproject.dto.MultiUpdateRequest
import ru.tinkoff.fintech.courseproject.dto.SingleUpdateRequest
import ru.tinkoff.fintech.courseproject.dto.UserRequest
import ru.tinkoff.fintech.courseproject.dto.UserResponseWithKV
import ru.tinkoff.fintech.courseproject.service.KeyValueStorageService

@RestController
class UserController(private val service: KeyValueStorageService) {

    @PostMapping("/user")
    fun addUser(@RequestBody userRequest: UserRequest) =
        service.addUser(userRequest)

    @PostMapping("/single")
    fun addSingleKV(@RequestBody singleUpdateRequest: SingleUpdateRequest) =
        service.addSingleKV(singleUpdateRequest);

    @PostMapping("/multi")
    fun addMultiKV(@RequestBody multiUpdateRequest: MultiUpdateRequest) =
        service.addMultiKV(multiUpdateRequest);

    @GetMapping("/user/{phoneNumber}")
    fun getKVOnTime(@PathVariable phoneNumber: String, @RequestParam("time") time: String?): UserResponseWithKV =
        if (time == null) {
            service.getLatestKV(phoneNumber)
        } else {
            service.getKVOnTime(phoneNumber, time)
        }

    @GetMapping("/user/history/{phoneNumber}")
    fun getHistoryKV(
        @PathVariable phoneNumber: String,
        @RequestParam("key") key: String,
        @RequestParam("page") page: Int?,
        @RequestParam("per_page") perPage: Int?
    ): UserResponseWithKV {
        val actualPage = page ?: 0
        val actualPerPage = perPage ?: 20
        return service.getHistoryKV(phoneNumber, key, actualPage, actualPerPage)
    }

    @DeleteMapping("/user/{phoneNumber}")
    fun deleteUser(@PathVariable phoneNumber: String) =
        service.deleteUser(phoneNumber)

    @DeleteMapping("/key/{phoneNumber}")
    fun deleteKV(@PathVariable phoneNumber: String, @RequestParam("key") key: String) =
        service.deleteKV(phoneNumber, key)

}
