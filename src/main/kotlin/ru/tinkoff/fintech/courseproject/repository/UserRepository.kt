package ru.tinkoff.fintech.courseproject.repository

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.PreparedStatementSetter
import org.springframework.stereotype.Repository
import ru.tinkoff.fintech.courseproject.dto.UserRequest
import ru.tinkoff.fintech.courseproject.dto.UserResponse
import ru.tinkoff.fintech.courseproject.util.UserMapper

@Repository
class UserRepository(
    private val jdbcTemplate: JdbcTemplate,
    private val userMapper: UserMapper
) {

    fun getAllUsers(page: Int, perPage: Int): List<UserResponse> =
        jdbcTemplate.queryForStream(
            SELECT_ALL_USERS_QUERY,
            PreparedStatementSetter {
                it.setInt(1, perPage)
                it.setInt(2, page * perPage)
            }, userMapper
        ).toList()

    fun getUser(phoneNumber: String): UserResponse? =
        jdbcTemplate.queryForStream(
            SELECT_USER_QUERY,
            PreparedStatementSetter {
                it.setString(1, phoneNumber)
            },
            userMapper
        ).toList().firstOrNull();


    fun saveUser(userRequest: UserRequest): Boolean =
        jdbcTemplate.update(
            INSERT_USER_QUERY,
            userRequest.phoneNumber,
            userRequest.name,
            userRequest.email
        ) > 0

    fun deleteUser(phoneNumber: String): Boolean =
        jdbcTemplate.update(DELETE_USER_QUERY, phoneNumber) > 0

    fun getUserKeys(phoneNumber: String): List<String> =
        jdbcTemplate.queryForList(
            SELECT_USER_KEYS_QUERY,
            String::class.java,
            phoneNumber
        )

    private companion object {
        private const val SELECT_ALL_USERS_QUERY = """ SELECT name, email, phone_number FROM user_info
                    ORDER BY name
                    LIMIT ?
                    OFFSET ?"""
        private const val SELECT_USER_QUERY = "SELECT name, email, phone_number FROM user_info WHERE phone_number = ?"
        private const val INSERT_USER_QUERY =
            "INSERT INTO user_info(phone_number, name, email) VALUES (?, ?, ?) ON CONFLICT DO NOTHING"
        private const val DELETE_USER_QUERY = "DELETE FROM user_info WHERE phone_number = ?"
        private const val SELECT_USER_KEYS_QUERY = """SELECT key FROM record
            JOIN user_info ui ON ui.user_id = record.user_id
            WHERE phone_number = ?
            GROUP BY key
            ORDER BY key"""
    }
}
