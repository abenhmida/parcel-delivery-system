package com.krizaldis.parceldelivery

import org.assertj.core.api.Assertions.assertThat
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
@SpringBootTest
class ParcelDeliveryApplicationTest {
    @Autowired(required = false)
    private var flyway: Flyway? = null

    companion object {
        @Container
        private val postgres = PostgreSQLContainer("postgres:17")

        @JvmStatic
        @DynamicPropertySource
        fun registerDynamicProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }

    @Test
    fun `application context starts and flyway executes migrations`() {
        assertThat(flyway).isNotNull
        val appliedMigrations = flyway?.info()?.applied()
        assertThat(appliedMigrations).isNotEmpty
        assertThat(appliedMigrations?.map { it.script }).contains("V1__create_parcel_tables.sql")
    }
}
