package com.krizaldis.parceldelivery.application

import com.krizaldis.parceldelivery.api.CreateParcelRequest
import com.krizaldis.parceldelivery.domain.Parcel
import com.krizaldis.parceldelivery.domain.ParcelRepository
import com.krizaldis.parceldelivery.domain.TrackingNumberGenerator
import com.krizaldis.parceldelivery.exceptions.ParcelNotFoundException
import java.time.Clock
import java.util.UUID

class ParcelApplicationService(
    private val parcelRepository: ParcelRepository,
    private val trackingNumberGenerator: TrackingNumberGenerator,
    private val clock: Clock,
) {
    fun create(request: CreateParcelRequest): Parcel {
        val parcel =
            Parcel.create(
                trackingNumber = trackingNumberGenerator.generate(),
                sender = request.sender.toDomain(),
                recipient = request.recipient.toDomain(),
                weight = request.weight,
                clock = clock,
            )

        parcelRepository.save(parcel)

        return parcel
    }

    fun get(id: UUID): Parcel =
        parcelRepository.findById(id)
            ?: throw ParcelNotFoundException(id)

    fun getByTrackingNumber(trackingNumber: String): Parcel =
        parcelRepository.findByTrackingNumber(trackingNumber)
            ?: throw ParcelNotFoundException(trackingNumber)

    fun pickUp(id: UUID): Parcel =
        transition(id) {
            pickUp()
        }

    fun arriveAtSortingCenter(id: UUID): Parcel =
        transition(id) {
            arriveAtSortingCenter()
        }

    fun dispatch(id: UUID): Parcel =
        transition(id) {
            dispatch()
        }

    fun outForDelivery(id: UUID): Parcel =
        transition(id) {
            outForDelivery()
        }

    fun deliver(id: UUID): Parcel =
        transition(id) {
            deliver()
        }

    fun deliveryFailed(id: UUID): Parcel =
        transition(id) {
            deliveryFailed()
        }

    fun retryDelivery(id: UUID): Parcel =
        transition(id) {
            retryDelivery()
        }

    fun returnToSender(id: UUID): Parcel =
        transition(id) {
            returnToSender()
        }

    private fun transition(
        id: UUID,
        operation: Parcel.() -> Unit,
    ): Parcel {
        val parcel = get(id)
        parcel.operation()
        parcelRepository.save(parcel)

        return parcel
    }
}
