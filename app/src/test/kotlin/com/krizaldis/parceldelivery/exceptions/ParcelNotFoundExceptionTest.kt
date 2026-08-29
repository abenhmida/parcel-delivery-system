package com.krizaldis.parceldelivery.exceptions

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

class ParcelNotFoundExceptionTest {
    @Test
    fun `should create exception with custom message`() {
        val exception = ParcelNotFoundException("Custom parcel not found message")

        assertThat(exception.message).isEqualTo("Custom parcel not found message")
    }

    @Test
    fun `should create exception with uuid`() {
        val id = UUID.randomUUID()
        val exception = ParcelNotFoundException(id)

        assertThat(exception.message).isEqualTo("Parcel with id $id not found")
    }
}
