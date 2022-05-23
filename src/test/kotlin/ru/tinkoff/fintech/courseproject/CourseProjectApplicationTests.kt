package ru.tinkoff.fintech.courseproject

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.extensions.Extension
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.core.test.TestCase
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import io.mockk.every
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActionsDsl
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import ru.tinkoff.fintech.courseproject.client.PhoneValidationClient
import ru.tinkoff.fintech.courseproject.dto.ClientResponse
import ru.tinkoff.fintech.courseproject.dto.KeyValuePair
import ru.tinkoff.fintech.courseproject.dto.MultiUpdateRequest
import ru.tinkoff.fintech.courseproject.dto.SingleUpdateRequest
import ru.tinkoff.fintech.courseproject.dto.UserRequest
import ru.tinkoff.fintech.courseproject.dto.UserResponse
import ru.tinkoff.fintech.courseproject.dto.UserResponseWithKV
import java.lang.Thread.sleep
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.text.Charsets.UTF_8

@SpringBootTest
@AutoConfigureMockMvc
class CourseProjectApplicationTests(private val mockMvc: MockMvc, private val objectMapper: ObjectMapper) :
    FeatureSpec() {

    @MockkBean
    private lateinit var phoneValidationClient: PhoneValidationClient

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    override fun extensions(): List<Extension> = listOf(SpringExtension)

    override suspend fun beforeTest(testCase: TestCase) {
        every { phoneValidationClient.validate(WRONG_PHONE_NUMBER) } returns ClientResponse(false)
        every { phoneValidationClient.validate(not(WRONG_PHONE_NUMBER)) } returns ClientResponse(true)
    }

    override suspend fun beforeEach(testCase: TestCase) {
        jdbcTemplate.update(DELETE_QUERY)
    }

    init {
        feature("add person") {
            scenario("success") {
                addPersonResponseStatus(userIvan) shouldBe HttpStatus.OK.value()
            }
            scenario("failure: invalid phone number") {
                addPersonResponseStatus(userWithWrongNumber) shouldBe HttpStatus.BAD_REQUEST.value()
                addPersonResponseStatus(userWithEmptyNumber) shouldBe HttpStatus.BAD_REQUEST.value()
            }
            scenario("failure: person with this phone number is already registered") {
                addPersonResponseStatus(userIvan) shouldBe HttpStatus.OK.value()
                addPersonResponseStatus(userIvan) shouldBe HttpStatus.BAD_REQUEST.value()
            }
        }
        feature("save and receive key-value") {
            scenario("success: save and receive single value") {
                addPersonResponseStatus(userIvan) shouldBe HttpStatus.OK.value()
                addSingleKVResponseStatus(
                    SingleUpdateRequest(
                        userIvan.phoneNumber,
                        KEY_1,
                        VALUE_1
                    )
                ) shouldBe HttpStatus.OK.value()
                val userResponseWithKV = getKVOnTime(userIvan.phoneNumber)
                userResponseWithKV.user shouldBe userIvanResponse
                userResponseWithKV.records.first().key shouldBe KEY_1
                userResponseWithKV.records.first().value shouldBe VALUE_1
                userResponseWithKV.records.first().revision shouldBe 0
            }
            scenario("success: save and receive multiple values") {
                addPersonResponseStatus(userIvan) shouldBe HttpStatus.OK.value()
                addMultiKVResponseStatus(
                    MultiUpdateRequest(
                        userIvan.phoneNumber, arrayOf(
                            KeyValuePair(KEY_1, VALUE_1),
                            KeyValuePair(KEY_2, VALUE_2)
                        )
                    )
                ) shouldBe HttpStatus.OK.value()
                val userResponseWithKV = getKVOnTime(userIvan.phoneNumber)
                userResponseWithKV.user shouldBe userIvanResponse
                userResponseWithKV.records.first().key shouldBe KEY_1
                userResponseWithKV.records.first().value shouldBe VALUE_1
                userResponseWithKV.records.first().revision shouldBe 0
                userResponseWithKV.records.last().key shouldBe KEY_2
                userResponseWithKV.records.last().value shouldBe VALUE_2
                userResponseWithKV.records.last().revision shouldBe 0
            }
            scenario("success: receive values history") {
                addPersonResponseStatus(userIvan) shouldBe HttpStatus.OK.value()
                addSingleKVResponseStatus(
                    SingleUpdateRequest(
                        userIvan.phoneNumber,
                        KEY_1,
                        VALUE_1
                    )
                ) shouldBe HttpStatus.OK.value()
                addSingleKVResponseStatus(
                    SingleUpdateRequest(
                        userIvan.phoneNumber,
                        KEY_1,
                        VALUE_2
                    )
                ) shouldBe HttpStatus.OK.value()
                addSingleKVResponseStatus(
                    SingleUpdateRequest(
                        userIvan.phoneNumber,
                        KEY_1,
                        VALUE_3
                    )
                ) shouldBe HttpStatus.OK.value()
                val userResponseWithKV = getHistoryKV(userIvan.phoneNumber, KEY_1, 1, 2)
                userResponseWithKV.user shouldBe userIvanResponse
                userResponseWithKV.records.first().key shouldBe KEY_1
                userResponseWithKV.records.first().value shouldBe VALUE_3
                userResponseWithKV.records.first().revision shouldBe 2
            }
            scenario("success: receive values actual in particular time") {
                addPersonResponseStatus(userIvan) shouldBe HttpStatus.OK.value()
                addSingleKVResponseStatus(
                    SingleUpdateRequest(
                        userIvan.phoneNumber,
                        KEY_1,
                        VALUE_1
                    )
                ) shouldBe HttpStatus.OK.value()
                sleep(1000)
                val timeOfZeroRevision = LocalDateTime.now()
                sleep(5000)
                addSingleKVResponseStatus(
                    SingleUpdateRequest(
                        userIvan.phoneNumber,
                        KEY_1,
                        VALUE_2
                    )
                ) shouldBe HttpStatus.OK.value()
                val userResponseWithKV = getKVOnTime(userIvan.phoneNumber, timeOfZeroRevision.format())
                userResponseWithKV.user shouldBe userIvanResponse
                userResponseWithKV.records.first().key shouldBe KEY_1
                userResponseWithKV.records.first().value shouldBe VALUE_1
                userResponseWithKV.records.first().revision shouldBe 0
            }
            scenario("failure: no such user exists") {
                addSingleKVResponseStatus(
                    SingleUpdateRequest(
                        userIvan.phoneNumber,
                        KEY_1,
                        VALUE_1
                    )
                ) shouldBe HttpStatus.BAD_REQUEST.value()
            }
            scenario("failure: invalid time format") {
                addPersonResponseStatus(userIvan) shouldBe HttpStatus.OK.value()
                getKVOnTimeResponseStatus(
                    userIvan.phoneNumber,
                    "2022-05-22 19:10:28"
                ) shouldBe HttpStatus.BAD_REQUEST.value()
            }
            scenario("failure: duplicate keys in request") {
                addPersonResponseStatus(userIvan) shouldBe HttpStatus.OK.value()
                addMultiKVResponseStatus(
                    MultiUpdateRequest(
                        userIvan.phoneNumber, arrayOf(
                            KeyValuePair(KEY_1, VALUE_1),
                            KeyValuePair(KEY_1, VALUE_2)
                        )
                    )
                ) shouldBe HttpStatus.BAD_REQUEST.value()
            }
        }
        feature("delete") {
            scenario("success: delete user") {
                addPersonResponseStatus(userIvan) shouldBe HttpStatus.OK.value()
                deletePersonResponseStatus(userIvan.phoneNumber) shouldBe HttpStatus.OK.value()
            }
            scenario("success: delete values on key") {
                addPersonResponseStatus(userIvan) shouldBe HttpStatus.OK.value()
                addSingleKVResponseStatus(
                    SingleUpdateRequest(
                        userIvan.phoneNumber,
                        KEY_1,
                        VALUE_1
                    )
                ) shouldBe HttpStatus.OK.value()
                deleteKeyResponseStatus(userIvan.phoneNumber, KEY_1) shouldBe HttpStatus.OK.value()
            }
            scenario("failure: no such user exists") {
                deletePersonResponseStatus(userIvan.phoneNumber) shouldBe HttpStatus.BAD_REQUEST.value()
            }
            scenario("failure: no such key exists") {
                addPersonResponseStatus(userIvan) shouldBe HttpStatus.OK.value()
                addSingleKVResponseStatus(
                    SingleUpdateRequest(
                        userIvan.phoneNumber,
                        KEY_1,
                        VALUE_1
                    )
                ) shouldBe HttpStatus.OK.value()
                deleteKeyResponseStatus(userIvan.phoneNumber, KEY_2) shouldBe HttpStatus.BAD_REQUEST.value()
            }
        }
    }

    private fun addPersonResponseStatus(userRequest: UserRequest) =
        mockMvc.post("/user") {
            contentType = MediaType.APPLICATION_JSON; content = objectMapper.writeValueAsString(userRequest)
        }
            .andReturn().response.status

    private fun addSingleKVResponseStatus(singleUpdateRequest: SingleUpdateRequest) =
        mockMvc.post("/single") {
            contentType = MediaType.APPLICATION_JSON; content = objectMapper.writeValueAsString(singleUpdateRequest)
        }
            .andReturn().response.status

    private fun addMultiKVResponseStatus(multiUpdateRequest: MultiUpdateRequest) =
        mockMvc.post("/multi") {
            contentType = MediaType.APPLICATION_JSON; content = objectMapper.writeValueAsString(multiUpdateRequest)
        }
            .andReturn().response.status

    private fun getKVOnTime(phoneNumber: String, time: String? = null): UserResponseWithKV =
        if (time == null) {
            mockMvc.get("/user/{phoneNumber}", phoneNumber).readResponse()
        } else {
            mockMvc.get("/user/{phoneNumber}?time={time}", phoneNumber, time).readResponse()
        }

    private fun getKVOnTimeResponseStatus(phoneNumber: String, time: String) =
        mockMvc.get("/user/{phoneNumber}?time={time}", phoneNumber, time).andReturn().response.status

    private fun getHistoryKV(phoneNumber: String, key: String, page: Int, perPage: Int): UserResponseWithKV =
        mockMvc.get(
            "/user/history/{phoneNumber}?key={key}&page={page}&per_page={perPage}",
            phoneNumber,
            key,
            page,
            perPage
        ).readResponse()

    private fun deletePersonResponseStatus(phoneNumber: String) =
        mockMvc.delete("/user/{phoneNumber}", phoneNumber).andReturn().response.status

    private fun deleteKeyResponseStatus(phoneNumber: String, key: String) =
        mockMvc.delete("/key/{phoneNumber}?key={key}", phoneNumber, key).andReturn().response.status

    private inline fun <reified T> ResultActionsDsl.readResponse(expectedStatus: HttpStatus = HttpStatus.OK): T =
        this.andExpect { status { isEqualTo(expectedStatus.value()) } }.andReturn().response.getContentAsString(UTF_8)
            .let { if (T::class == String::class) it as T else objectMapper.readValue(it) }

    private fun LocalDateTime.format() = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss").format(this)

    private companion object {
        private const val WRONG_PHONE_NUMBER = "123"
        private const val PHONE_NUMBER = "+74950000000"
        private const val KEY_1 = "key 1"
        private const val KEY_2 = "key 2"
        private const val VALUE_1 = "value 1"
        private const val VALUE_2 = "value 2"
        private const val VALUE_3 = "value 3"
        private val userIvan = UserRequest("Ivan", "Ivan@mail.ru", PHONE_NUMBER)
        private val userWithWrongNumber = UserRequest("Ivan", "Ivan@mail.ru", WRONG_PHONE_NUMBER)
        private val userWithEmptyNumber = UserRequest("Ivan", "Ivan@mail.ru", "")
        private val userIvanResponse = UserResponse(userIvan.name, userIvan.email, userIvan.phoneNumber)
        private const val DELETE_QUERY = "DELETE FROM user_info"
    }

}
