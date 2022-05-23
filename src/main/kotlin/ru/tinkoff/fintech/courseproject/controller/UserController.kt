package ru.tinkoff.fintech.courseproject.controller

import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.tinkoff.fintech.courseproject.dto.UserRequest
import ru.tinkoff.fintech.courseproject.service.UserService

@RestController
@RequestMapping("/user")
class UserController(private val userService: UserService) {

    @PostMapping
    fun addUser(@RequestBody userRequest: UserRequest) =
        userService.addUser(userRequest)

    @DeleteMapping("/{phoneNumber}")
    fun deleteUser(@PathVariable phoneNumber: String) =
        userService.deleteUser(phoneNumber)

}
