package com.krizaldis.parceldelivery.infrastructure.database

import com.krizaldis.parceldelivery.domain.Parcel
import com.krizaldis.parceldelivery.domain.ParcelRepository
import org.jooq.DSLContext
import java.util.UUID

class JooqParcelRepository(
    private val dsl: DSLContext,
) : ParcelRepository {
    override fun save(parcel: Parcel) {
        TODO("Not yet implemented")
    }

    override fun findById(id: UUID): Parcel? {
        TODO("Not yet implemented")
    }

    override fun findByTrackingNumber(trackingNumber: String): Parcel? {
        TODO("Not yet implemented")
    }
}
