package ru.tinkoff.fintech.courseproject.repository

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.PreparedStatementSetter
import org.springframework.stereotype.Repository
import ru.tinkoff.fintech.courseproject.dto.RecordSqlRow
import ru.tinkoff.fintech.courseproject.dto.SingleUpdateRequest
import ru.tinkoff.fintech.courseproject.dto.UserRequest
import ru.tinkoff.fintech.courseproject.dto.UserResponse
import java.sql.Timestamp
import java.time.LocalDateTime

@Repository
class JdbcRepository(
    private val jdbcTemplate: JdbcTemplate,
    private val recordMapper: RecordMapper,
    private val userMapper: UserMapper
) {

    fun getUserList(page: Int, perPage: Int): List<UserResponse> =
        jdbcTemplate.queryForStream(SELECT_ALL_USERS_QUERY,
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


    fun saveKV(singleUpdateRequest: SingleUpdateRequest, updateTime: LocalDateTime): Boolean =
        jdbcTemplate.update(
            INSERT_KV_QUERY,
            singleUpdateRequest.phoneNumber,
            singleUpdateRequest.key,
            singleUpdateRequest.value,
            singleUpdateRequest.phoneNumber,
            singleUpdateRequest.key,
            updateTime
        ) > 0


    fun getLatestKV(phoneNumber: String): List<RecordSqlRow> =
        jdbcTemplate.queryForStream(
            SELECT_LATEST_KV_QUERY,
            PreparedStatementSetter {
                it.setString(1, phoneNumber)
                it.setString(2, phoneNumber)
            },
            recordMapper
        ).toList()


    fun getKVOnTime(phoneNumber: String, time: LocalDateTime): List<RecordSqlRow> =
        jdbcTemplate.queryForStream(
            SELECT_KV_ON_TIME_QUERY,
            PreparedStatementSetter {
                it.setString(1, phoneNumber)
                it.setTimestamp(2, Timestamp.valueOf(time))
                it.setString(3, phoneNumber)
            },
            recordMapper
        ).toList()


    fun getHistoryKV(phoneNumber: String, key: String, page: Int, perPage: Int): List<RecordSqlRow> =
        jdbcTemplate.queryForStream(
            SELECT_KV_HISTORY_QUERY,
            PreparedStatementSetter {
                it.setString(1, phoneNumber)
                it.setString(2, key)
                it.setInt(3, perPage)
                it.setInt(4, page * perPage)
            },
            recordMapper
        ).toList()


    fun deleteUser(phoneNumber: String): Boolean =
        jdbcTemplate.update(DELETE_USER_QUERY, phoneNumber) > 0

    fun deleteKV(phoneNumber: String, key: String): Boolean =
        jdbcTemplate.update(DELETE_KV_QUERY, phoneNumber, key) > 0

    fun trimKeyHistory(phoneNumber: String, key: String, maxSize: Int) =
        jdbcTemplate.update(TRIM_KV_QUERY, phoneNumber, key, maxSize)

    private companion object {
        private const val SELECT_ALL_USERS_QUERY = """ SELECT name, email, phone_number FROM user_info
                    ORDER BY name
                    LIMIT ?
                    OFFSET ?"""
        private const val SELECT_USER_QUERY = "SELECT name, email, phone_number FROM user_info WHERE phone_number = ?"
        private const val INSERT_USER_QUERY =
            "INSERT INTO user_info(phone_number, name, email) VALUES (?, ?, ?) ON CONFLICT DO NOTHING"
        private const val INSERT_KV_QUERY = """WITH id_table AS (SELECT user_id FROM user_info WHERE phone_number = ?)
                    INSERT INTO record (user_id, key, value, revision, update_time)
                    SELECT  user_id,
                            ?,
                            ?,
                            (SELECT COALESCE(
                                    (SELECT MAX(revision)
                                    FROM record JOIN user_info USING (user_id)
                                    WHERE phone_number = ?
                                     AND key = ?) + 1, 0)
                            ),
                            ?
                    FROM id_table
                    WHERE id_table.user_id NOTNULL"""
        private const val SELECT_LATEST_KV_QUERY =
            """SELECT record.key AS key, value, revision, update_time, phone_number, name, email
                    FROM record JOIN user_info USING (user_id)
                                JOIN (SELECT key, MAX(revision) AS max_revision
                                        FROM record JOIN user_info USING (user_id)
                                        WHERE user_info.phone_number = ?
                                        GROUP BY key) AS max_table 
                                        ON (record.key = max_table.key AND revision = max_table.max_revision)
                    WHERE phone_number = ?"""
        private const val SELECT_KV_ON_TIME_QUERY =
            """SELECT record.key AS key, value, revision, update_time, phone_number, name, email
                    FROM record JOIN user_info USING (user_id)
                                JOIN (SELECT key, MAX(revision) AS max_revision
                                        FROM record JOIN user_info USING (user_id)
                                        WHERE user_info.phone_number = ?
                                            AND update_time <= ?
                                        GROUP BY key) AS max_table 
                                        ON (record.key = max_table.key AND revision = max_table.max_revision)
                    WHERE phone_number = ?"""
        private const val SELECT_KV_HISTORY_QUERY =
            """SELECT key, value, revision, update_time, phone_number, name, email
                    FROM record JOIN user_info USING (user_id)           
                    WHERE phone_number = ? AND key = ?
                    ORDER BY revision
                    LIMIT ?
                    OFFSET ? """
        private const val DELETE_USER_QUERY = "DELETE FROM user_info WHERE phone_number = ?"
        private const val DELETE_KV_QUERY = """DELETE FROM record USING user_info
                    WHERE record.user_id = user_info.user_id AND phone_number = ? AND key = ?"""
        private const val TRIM_KV_QUERY = """DELETE FROM record
                    WHERE record_id IN 
                        (
                            SELECT record_id 
                            FROM record JOIN user_info ui ON record.user_id = ui.user_id 
                            WHERE phone_number = ? AND key = ? 
                            ORDER BY revision DESC 
                            OFFSET ?
                        )"""
    }
}
