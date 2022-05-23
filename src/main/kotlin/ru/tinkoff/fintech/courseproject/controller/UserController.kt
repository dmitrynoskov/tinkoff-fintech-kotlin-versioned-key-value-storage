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
import ru.tinkoff.fintech.courseproject.dto.UserRequest
import ru.tinkoff.fintech.courseproject.dto.UserResponse
import ru.tinkoff.fintech.courseproject.service.UserService

@RestController
@RequestMapping("/user")
class UserController(
    private val userService: UserService,
    @Value("\${default-page-size}") private val defaultPageSize: Int
) {

    @PostMapping
    fun addUser(@RequestBody userRequest: UserRequest) =
        userService.addUser(userRequest)

    @DeleteMapping("/{phoneNumber}")
    fun deleteUser(@PathVariable phoneNumber: String) =
        userService.deleteUser(phoneNumber)

    @GetMapping("/all")
    fun getAllUsers(
        @RequestParam("page") page: Int?,
        @RequestParam("per_page") perPage: Int?
    ): List<UserResponse> {
        val actualPage = page ?: 0
        val actualPerPage = perPage ?: defaultPageSize
        return userService.getAllUsers(actualPage, actualPerPage)
    }

}
