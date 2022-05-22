package ru.tinkoff.fintech.courseproject.repository

import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Component
import ru.tinkoff.fintech.courseproject.dto.UserResponse
import java.sql.ResultSet

@Component
class UserMapper : RowMapper<UserResponse> {
    override fun mapRow(rs: ResultSet, rowNum: Int): UserResponse = UserResponse(
        name = rs.getString("name"),
        email = rs.getString("email"),
        phoneNumber = rs.getString("phone_number")
    )
}
