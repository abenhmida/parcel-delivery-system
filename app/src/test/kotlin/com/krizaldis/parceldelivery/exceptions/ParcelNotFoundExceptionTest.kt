package com.krizaldis.parceldelivery.exceptions

import com.example.parceldelivery.application.ParcelNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

class ParcelNotFoundExceptionTest {
    @Test
    fun `should create exception with uuid`() {
        val id = UUID.randomUUID()
        val exception = ParcelNotFoundException(id.toString())

        assertThat(exception.message).isEqualTo("Parcel with id $id not found")
    }
}
