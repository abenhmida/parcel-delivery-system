package com.krizaldis.parceldelivery.infrastructure.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import com.krizaldis.parceldelivery.events.ParcelEvent
import com.krizaldis.parceldelivery.events.ParcelEventSerializer

class JacksonParcelEventSerializer(
    private val objectMapper: ObjectMapper,
) : ParcelEventSerializer {
    override fun serialize(event: ParcelEvent): String = objectMapper.writeValueAsString(event)

    override fun deserialize(payload: String): ParcelEvent = objectMapper.readValue(payload, ParcelEvent::class.java)
}
