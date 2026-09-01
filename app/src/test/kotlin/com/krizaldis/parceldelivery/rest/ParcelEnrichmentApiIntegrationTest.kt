package com.krizaldis.parceldelivery.rest

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.containers.PostgreSQLContainer
import java.util.UUID

@Disabled
@SpringBootTest
@AutoConfigureMockMvc
class ParcelEnrichmentApiIntegrationTest {
    companion object {
        private val postgres = PostgreSQLContainer("postgres:17")

        @JvmStatic
        @org.junit.jupiter.api.BeforeAll
        fun start() {
            postgres.start()
        }

        @JvmStatic
        @org.junit.jupiter.api.AfterAll
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

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    @Disabled
    fun `enrichment endpoint returns concurrent enrichment results`(): Unit =
        runBlocking {
            val createResponse =
                mockMvc
                    .perform(
                        post("/parcels")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(
                                """
                                {
                                  "sender": {"name":"Alice","street":"1 Main Street","city":"Paris","postal_code":"75001","country":"FR"},
                                  "recipient": {"name":"Bob","street":"2 Oak Street","city":"Lyon","postal_code":"69001","country":"FR"},
                                  "weight": 2.5
                                }
                                """.trimIndent(),
                            ),
                    ).andExpect(status().isCreated)
                    .andReturn()

            val id =
                UUID.fromString(
                    Regex("""\"id\":\"([^\"]+)\"""")
                        .find(createResponse.response.contentAsString)!!
                        .groupValues[1],
                )

            mockMvc
                .perform(get("/parcels/$id/enrichment"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.parcelId").value(id.toString()))
                .andExpect(jsonPath("$.addressVerified").value(true))
                .andExpect(jsonPath("$.route.distanceKm").value(465.2))
                .andExpect(jsonPath("$.route.estimatedMinutes").value(285))
                .andExpect(jsonPath("$.deliveryEstimate.date").exists())
        }
}
