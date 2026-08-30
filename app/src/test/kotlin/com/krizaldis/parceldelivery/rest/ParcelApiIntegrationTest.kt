package com.krizaldis.parceldelivery.rest

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.containers.PostgreSQLContainer

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ParcelApiIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `complete parcel lifecycle is exposed through HTTP`() {
        val createResult =
            mockMvc
                .perform(
                    post("/parcels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            """
                            {
                              "sender": {
                                "name": "Alice",
                                "street": "1 Main Street",
                                "city": "Paris",
                                "postalCode": "75001",
                                "country": "FR"
                              },
                              "recipient": {
                                "name": "Bob",
                                "street": "2 Oak Street",
                                "city": "Lyon",
                                "postalCode": "69001",
                                "country": "FR"
                              },
                              "weight": 2.5
                            }
                            """.trimIndent(),
                        ),
                ).andExpect(status().isCreated)
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andReturn()

        val json = createResult.response.contentAsString
        val id = Regex(""""id":"([^"]+)"""").find(json)!!.groupValues[1]

        listOf(
            "pickup",
            "sorting",
            "dispatch",
            "out-for-delivery",
            "deliver",
        ).forEach { operation ->
            mockMvc
                .perform(
                    post("/parcels/$id/$operation"),
                ).andExpect(status().isOk)
        }

        mockMvc
            .perform(get("/parcels/$id/tracking"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.events.length()").value(6))
            .andExpect(jsonPath("$.events[0].status").value("CREATED"))
            .andExpect(jsonPath("$.events[5].status").value("DELIVERED"))
    }

    @Test
    fun `invalid transition returns conflict`() {
        val createResult =
            mockMvc
                .perform(
                    post("/parcels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            """
                            {
                              "sender": {
                                "name": "Alice",
                                "street": "1 Main Street",
                                "city": "Paris",
                                "postalCode": "75001",
                                "country": "FR"
                              },
                              "recipient": {
                                "name": "Bob",
                                "street": "2 Oak Street",
                                "city": "Lyon",
                                "postalCode": "69001",
                                "country": "FR"
                              },
                              "weight": 2.5
                            }
                            """.trimIndent(),
                        ),
                ).andReturn()

        val id =
            Regex(""""id":"([^"]+)"""")
                .find(createResult.response.contentAsString)!!
                .groupValues[1]

        mockMvc
            .perform(post("/parcels/$id/deliver"))
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.code").value("INVALID_STATE_TRANSITION"))
    }

    companion object {
        private val postgres = PostgreSQLContainer("postgres:17")

        @JvmStatic
        @BeforeAll
        fun start() {
            postgres.start()
        }

        @JvmStatic
        @AfterAll
        fun stop() {
            postgres.stop()
        }

        @JvmStatic
        @DynamicPropertySource
        fun databaseProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { postgres.jdbcUrl }
            registry.add("spring.datasource.username") { postgres.username }
            registry.add("spring.datasource.password") { postgres.password }
        }
    }
}
