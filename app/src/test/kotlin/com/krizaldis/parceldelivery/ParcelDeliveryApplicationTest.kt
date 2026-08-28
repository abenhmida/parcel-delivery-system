package com.krizaldis.parceldelivery

import org.assertj.core.api.Assertions.assertThat
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ParcelDeliveryApplicationTest {
    @Autowired(required = false)
    private var flyway: Flyway? = null

    @Test
    fun `application context starts and flyway executes migrations`() {
        assertThat(flyway).isNotNull
        val appliedMigrations = flyway?.info()?.applied()
        assertThat(appliedMigrations).isNotEmpty
        assertThat(appliedMigrations?.map { it.script }).contains("V1__create_parcel_tables.sql")
    }
}
