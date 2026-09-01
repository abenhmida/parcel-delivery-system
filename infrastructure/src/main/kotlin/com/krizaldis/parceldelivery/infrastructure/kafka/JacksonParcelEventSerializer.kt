package com.krizaldis.parceldelivery.infrastructure.kafka

import com.krizaldis.parceldelivery.events.ParcelEvent
import com.krizaldis.parceldelivery.events.ParcelEventSerializer
import tools.jackson.databind.json.JsonMapper

class JacksonParcelEventSerializer(
    private val objectMapper: JsonMapper,
) : ParcelEventSerializer {
    override fun serialize(event: ParcelEvent): String = objectMapper.writeValueAsString(event)

    override fun deserialize(payload: String): ParcelEvent = objectMapper.readValue(payload, ParcelEvent::class.java)
}
