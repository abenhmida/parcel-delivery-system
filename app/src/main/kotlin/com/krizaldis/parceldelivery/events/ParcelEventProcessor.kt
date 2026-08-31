package com.krizaldis.parceldelivery.events

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class ParcelEventProcessor(
    private val handler: ParcelEventHandler,
    @Value("\${parcel.consumer.max-concurrency:8}")
    maxConcurrency: Int,
) {
    private val scope =
        CoroutineScope(
            Dispatchers.Default + SupervisorJob(),
        )

    private val semaphore = Semaphore(maxConcurrency)

    suspend fun process(events: List<ParcelEvent>) {
        events
            .map { event ->
                scope.async {
                    semaphore.withPermit {
                        handler.handle(event)
                    }
                }
            }.awaitAll()
    }

    fun shutdown() {
        scope.cancel()
    }
}
