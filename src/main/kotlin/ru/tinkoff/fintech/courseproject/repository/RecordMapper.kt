package ru.tinkoff.fintech.courseproject.repository

import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Component
import ru.tinkoff.fintech.courseproject.dto.RecordSqlRow
import java.sql.ResultSet

@Component
class RecordMapper : RowMapper<RecordSqlRow> {
    override fun mapRow(rs: ResultSet, rowNum: Int): RecordSqlRow =
        RecordSqlRow(
            name = rs.getString("name"),
            phoneNumber = rs.getString("phone_number"),
            email = rs.getString("email"),
            key = rs.getString("key"),
            value = rs.getString("value"),
            revision = rs.getInt("revision"),
            updateTime = rs.getTimestamp("update_time").toLocalDateTime()
        )
}
