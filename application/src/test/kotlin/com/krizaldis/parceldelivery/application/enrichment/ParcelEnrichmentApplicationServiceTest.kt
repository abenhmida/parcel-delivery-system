package com.krizaldis.parceldelivery.application.enrichment

import com.krizaldis.parceldelivery.domain.Address
import com.krizaldis.parceldelivery.domain.Parcel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Collections
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.system.measureTimeMillis
import kotlin.time.Duration.Companion.milliseconds

class ParcelEnrichmentApplicationServiceTest {
    @Test
    fun `sequential enrichment runs operations one after another`(): Unit =
        runBlocking {
            val order = Collections.synchronizedList(mutableListOf<String>())
            val services = testServices(order)
            val service = SequentialParcelEnrichmentService(services.address, services.route, services.estimate)

            service.enrich(parcel())

            assertThat(order).containsExactly(
                "address-start",
                "address-end",
                "route-start",
                "route-end",
                "estimate-start",
                "estimate-end",
            )
        }

    @Test
    fun `concurrent enrichment starts all independent operations before they finish`(): Unit =
        runBlocking {
            val order = Collections.synchronizedList(mutableListOf<String>())
            val services = testServices(order)
            val service = ConcurrentParcelEnrichmentService(services.address, services.route, services.estimate)

            service.enrich(parcel())

            assertThat(order.indexOf("route-start")).isLessThan(order.indexOf("address-end"))
            assertThat(order.indexOf("estimate-start")).isLessThan(order.indexOf("route-end"))
        }

    @Test
    fun `concurrent enrichment returns all three results`(): Unit =
        runBlocking {
            val service =
                ConcurrentParcelEnrichmentService(
                    addressVerifier = { AddressVerification(true, "normalized") },
                    routeCalculator = { Route(10.0, 20) },
                    deliveryEstimator = { DeliveryEstimate(LocalDate.of(2026, 9, 2)) },
                )

            val parcel = parcel()
            val result = service.enrich(parcel)

            assertThat(result.parcelId).isEqualTo(parcel.id)
            assertThat(result.addressVerification.valid).isTrue()
            assertThat(result.route.distanceKm).isEqualTo(10.0)
            assertThat(result.route.estimatedMinutes).isEqualTo(20)
            assertThat(result.deliveryEstimate.deliveryDate).isEqualTo(LocalDate.of(2026, 9, 2))
        }

    @Test
    fun `failure of one child cancels sibling work under structured concurrency`(): Unit =
        runBlocking {
            val siblingCancelled = AtomicBoolean(false)
            val service =
                ConcurrentParcelEnrichmentService(
                    addressVerifier = {
                        try {
                            delay(10_000.milliseconds)
                            AddressVerification(true, "never")
                        } catch (e: CancellationException) {
                            siblingCancelled.set(true)
                            throw e
                        }
                    },
                    routeCalculator = {
                        delay(20.milliseconds)
                        error("route unavailable")
                    },
                    deliveryEstimator = {
                        delay(10_000.milliseconds)
                        DeliveryEstimate(LocalDate.of(2026, 9, 2))
                    },
                )

            assertThatThrownBy {
                runBlockingAndAssert {
                    service.enrich(parcel())
                }
            }.isInstanceOf(IllegalStateException::class.java)

            assertThat(siblingCancelled).isTrue()
        }

    fun <T> runBlockingAndAssert(block: suspend () -> T): T = runBlocking { block() }

    @Test
    fun `suspend simulated services run concurrently in wall clock time`(): Unit =
        runBlocking {
            val address = SimulatedAddressVerifier(Duration.ofMillis(150))
            val route = SimulatedRouteCalculator(Duration.ofMillis(250))
            val estimate =
                SimulatedDeliveryEstimator(
                    Duration.ofMillis(100),
                    Clock.fixed(Instant.parse("2026-08-30T10:00:00Z"), ZoneOffset.UTC),
                )
            val parcel = parcel()

            val sequential = SequentialParcelEnrichmentService(address, route, estimate)
            val concurrent = ConcurrentParcelEnrichmentService(address, route, estimate)

            val sequentialMs = measureTimeMillis { sequential.enrich(parcel) }
            val concurrentMs = measureTimeMillis { concurrent.enrich(parcel) }

            assertThat(sequentialMs).isGreaterThanOrEqualTo(450)
            assertThat(concurrentMs).isLessThan(400)
        }

    private fun testServices(order: MutableList<String>): TestServices = TestServices(order)

    private fun parcel(): Parcel =
        Parcel.create(
            trackingNumber = "PD-ASYNC-001",
            sender = Address("Alice", "1 Main Street", "Paris", "75001", "FR"),
            recipient = Address("Bob", "2 Oak Street", "Lyon", "69001", "FR"),
            weight = BigDecimal("2.5"),
            clock = Clock.fixed(Instant.parse("2026-08-30T10:00:00Z"), ZoneOffset.UTC),
        )
}

private class TestServices(
    order: MutableList<String>,
) {
    val address =
        AddressVerifier {
            order += "address-start"
            delay(50.milliseconds)
            order += "address-end"
            AddressVerification(true, "address")
        }
    val route =
        RouteCalculator {
            order += "route-start"
            delay(50.milliseconds)
            order += "route-end"
            Route(1.0, 1)
        }
    val estimate =
        DeliveryEstimator {
            order += "estimate-start"
            delay(50.milliseconds)
            order += "estimate-end"
            DeliveryEstimate(LocalDate.of(2026, 9, 2))
        }
}
