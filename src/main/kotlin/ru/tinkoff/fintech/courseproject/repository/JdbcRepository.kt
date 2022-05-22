package ru.tinkoff.fintech.courseproject.repository

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.PreparedStatementSetter
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import ru.tinkoff.fintech.courseproject.dto.RecordSqlRow
import ru.tinkoff.fintech.courseproject.dto.SingleUpdateRequest
import ru.tinkoff.fintech.courseproject.dto.UserRequest
import ru.tinkoff.fintech.courseproject.dto.UserResponse
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.LocalDateTime

@Repository
class JdbcRepository(
    private val jdbcTemplate: JdbcTemplate
) {

    fun getUser(phoneNumber: String): UserResponse? {
        return jdbcTemplate.queryForStream(
            "SELECT name, email, phone_number FROM user_info WHERE phone_number = ?",
            PreparedStatementSetter {
                it.setString(1, phoneNumber)
            },
            RowMapper { rs, _ ->
                UserResponse(
                    name = rs.getString("name"),
                    email = rs.getString("email"),
                    phoneNumber = rs.getString("phone_number")
                )
            }
        ).toList().firstOrNull();
    }

    fun ifUserExists(phoneNumber: String): Boolean {
        return jdbcTemplate.queryForObject(
            "SELECT EXISTS (SELECT 1 FROM user_info WHERE phone_number = ?)",
            Boolean::class.java,
            phoneNumber
        )
    }

    fun saveUser(userRequest: UserRequest): Boolean {
        return jdbcTemplate.update(
            "INSERT INTO user_info(phone_number, name, email) VALUES (?, ?, ?) ON CONFLICT DO NOTHING",
            userRequest.phoneNumber,
            userRequest.name,
            userRequest.email
        ) > 0
    }

    fun saveKV(singleUpdateRequest: SingleUpdateRequest, updateTime: LocalDateTime): Boolean {
        return jdbcTemplate.update(
            """WITH id_table AS (SELECT user_id FROM user_info WHERE phone_number = ?)
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
                    WHERE id_table.user_id NOTNULL""",
            singleUpdateRequest.phoneNumber,
            singleUpdateRequest.key,
            singleUpdateRequest.value,
            singleUpdateRequest.phoneNumber,
            singleUpdateRequest.key,
            updateTime
        ) > 0
    }

    fun getLatestKV(phoneNumber: String): List<RecordSqlRow> {
        return jdbcTemplate.queryForStream(
            """SELECT record.key AS key, value, revision, update_time, phone_number, name, email
                    FROM record JOIN user_info USING (user_id)
                                JOIN (SELECT key, MAX(revision) AS max_revision
                                        FROM record JOIN user_info USING (user_id)
                                        WHERE user_info.phone_number = ?
                                        GROUP BY key) AS max_table 
                                        ON (record.key = max_table.key AND revision = max_table.max_revision)
                    WHERE phone_number = ?""",
            PreparedStatementSetter {
                it.setString(1, phoneNumber)
                it.setString(2, phoneNumber)
            },
            RecordMapper
        ).toList()
    }

    fun getKVOnTime(phoneNumber: String, time: LocalDateTime): List<RecordSqlRow> {
        return jdbcTemplate.queryForStream(
            """SELECT record.key AS key, value, revision, update_time, phone_number, name, email
                    FROM record JOIN user_info USING (user_id)
                                JOIN (SELECT key, MAX(revision) AS max_revision
                                        FROM record JOIN user_info USING (user_id)
                                        WHERE user_info.phone_number = ?
                                            AND update_time <= ?
                                        GROUP BY key) AS max_table 
                                        ON (record.key = max_table.key AND revision = max_table.max_revision)
                    WHERE phone_number = ?""",
            PreparedStatementSetter {
                it.setString(1, phoneNumber)
                it.setTimestamp(2, Timestamp.valueOf(time))
                it.setString(3, phoneNumber)
            },
            RecordMapper
        ).toList()
    }

    fun getHistoryKV(phoneNumber: String, key: String, page: Int, perPage: Int): List<RecordSqlRow> {
        return jdbcTemplate.queryForStream(
            """SELECT key, value, revision, update_time, phone_number, name, email
                    FROM record JOIN user_info USING (user_id)           
                    WHERE phone_number = ? AND key = ?
                    ORDER BY revision
                    LIMIT ?
                    OFFSET ? """,
            PreparedStatementSetter {
                it.setString(1, phoneNumber)
                it.setString(2, key)
                it.setInt(3, perPage)
                it.setInt(4, page * perPage)
            },
            RecordMapper
        ).toList()
    }

    fun deleteUser(phoneNumber: String): Boolean {
        println("inside deleteUser")
        println("phoneNumber is $phoneNumber")
        return jdbcTemplate.update(
            "DELETE FROM user_info WHERE phone_number = ?",
            phoneNumber
        ) > 0
    }

    fun deleteKV(phoneNumber: String, key: String): Boolean {
        return jdbcTemplate.update(
            """DELETE FROM record USING user_info
                    WHERE record.user_id = user_info.user_id AND phone_number = ? AND key = ?""",
            phoneNumber,
            key
        ) > 0
    }

    private companion object RecordMapper : RowMapper<RecordSqlRow> {
        override fun mapRow(rs: ResultSet, rowNum: Int): RecordSqlRow {
            return RecordSqlRow(
                name = rs.getString("name"),
                phoneNumber = rs.getString("phone_number"),
                email = rs.getString("email"),
                key = rs.getString("key"),
                value = rs.getString("value"),
                revision = rs.getInt("revision"),
                updateTime = rs.getTimestamp("update_time").toLocalDateTime()
            )
        }
    }

}
