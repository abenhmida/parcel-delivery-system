package com.krizaldis.parceldelivery.rest

import com.krizaldis.parceldelivery.api.AddressRequest
import com.krizaldis.parceldelivery.api.CreateParcelRequest
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.math.BigDecimal
import java.util.UUID

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ParcelControllerTest {
    @LocalServerPort
    private var port: Int = 0

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

    @BeforeEach
    fun setUp() {
        RestAssured.port = port
    }

    @Test
    fun `should create parcel successfully`() {
        val request =
            CreateParcelRequest(
                sender =
                    AddressRequest(
                        name = "Alice Sender",
                        street = "Sender Street 1",
                        city = "Berlin",
                        postalCode = "10115",
                        country = "DE",
                    ),
                recipient =
                    AddressRequest(
                        name = "Bob Receiver",
                        street = "Receiver Street 2",
                        city = "Munich",
                        postalCode = "80331",
                        country = "DE",
                    ),
                weight = BigDecimal("2.500"),
            )

        RestAssured
            .given()
            .contentType(ContentType.JSON)
            .body(request)
            .`when`()
            .post("/parcels")
            .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("tracking_number", notNullValue())
            .body("sender.name", equalTo("Alice Sender"))
            .body("sender.street", equalTo("Sender Street 1"))
            .body("sender.city", equalTo("Berlin"))
            .body("sender.postal_code", equalTo("10115"))
            .body("sender.country", equalTo("DE"))
            .body("recipient.name", equalTo("Bob Receiver"))
            .body("recipient.street", equalTo("Receiver Street 2"))
            .body("recipient.city", equalTo("Munich"))
            .body("recipient.postal_code", equalTo("80331"))
            .body("recipient.country", equalTo("DE"))
            .body("weight", equalTo(2.500f))
            .body("status", equalTo("CREATED"))
            .body("created_at", notNullValue())
        // .body("events", hasSize<Any>(1))
        // .body("events[0].status", equalTo("CREATED"))
        // .body("events[0].occurred_at", notNullValue())
    }

    @Test
    fun `should get parcel by id successfully`() {
        val request =
            CreateParcelRequest(
                sender =
                    AddressRequest(
                        name = "Charlie Sender",
                        street = "Main St 10",
                        city = "Hamburg",
                        postalCode = "20095",
                        country = "DE",
                    ),
                recipient =
                    AddressRequest(
                        name = "David Receiver",
                        street = "North St 20",
                        city = "Frankfurt",
                        postalCode = "60311",
                        country = "DE",
                    ),
                weight = BigDecimal("1.750"),
            )

        val createdParcelId: String =
            RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(request)
                .`when`()
                .post("/parcels")
                .then()
                .statusCode(201)
                .extract()
                .path("id")

        RestAssured
            .given()
            .`when`()
            .get("/parcels/{id}", createdParcelId)
            .then()
            .statusCode(200)
            .body("id", equalTo(createdParcelId))
            .body("sender.name", equalTo("Charlie Sender"))
            .body("recipient.name", equalTo("David Receiver"))
            .body("weight", equalTo(1.750f))
            .body("status", equalTo("CREATED"))
        // .body("events", hasSize<Any>(1))
        // .body("events[0].parcel_id", equalTo(createdParcelId))
        // .body("events[0].status", equalTo("CREATED"))
    }

    @Test
    fun `should get parcel by tracking number successfully`() {
        val request =
            CreateParcelRequest(
                sender =
                    AddressRequest(
                        name = "Eve Sender",
                        street = "East St 5",
                        city = "Cologne",
                        postalCode = "50667",
                        country = "DE",
                    ),
                recipient =
                    AddressRequest(
                        name = "Frank Receiver",
                        street = "West St 6",
                        city = "Dusseldorf",
                        postalCode = "40213",
                        country = "DE",
                    ),
                weight = BigDecimal("3.000"),
            )

        val trackingNumber: String =
            RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(request)
                .`when`()
                .post("/parcels")
                .then()
                .statusCode(201)
                .extract()
                .path("tracking_number")

        RestAssured
            .given()
            .`when`()
            .get("/parcels/tracking/{trackingNumber}", trackingNumber)
            .then()
            .statusCode(200)
            .body("tracking_number", equalTo(trackingNumber))
            .body("sender.name", equalTo("Eve Sender"))
            .body("recipient.name", equalTo("Frank Receiver"))
            .body("status", equalTo("CREATED"))
    }

    @Test
    fun `should return 404 when parcel not found by tracking number`() {
        RestAssured
            .given()
            .`when`()
            .get("/parcels/tracking/{trackingNumber}", "NON-EXISTENT-TRK")
            .then()
            .statusCode(404)
            .body("code", equalTo("PARCEL_NOT_FOUND"))
            .body("message", equalTo("Parcel with id NON-EXISTENT-TRK not found"))
    }

    @Test
    fun `should transition parcel status through lifecycle`() {
        val request =
            CreateParcelRequest(
                sender =
                    AddressRequest(
                        name = "Grace Sender",
                        street = "Grace St 1",
                        city = "Stuttgart",
                        postalCode = "70173",
                        country = "DE",
                    ),
                recipient =
                    AddressRequest(
                        name = "Heidi Receiver",
                        street = "Heidi St 2",
                        city = "Nuremberg",
                        postalCode = "90403",
                        country = "DE",
                    ),
                weight = BigDecimal("4.200"),
            )

        val parcelId: String =
            RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(request)
                .`when`()
                .post("/parcels")
                .then()
                .statusCode(201)
                .extract()
                .path("id")

        RestAssured
            .given()
            .contentType(ContentType.JSON)
            .`when`()
            .post("/parcels/{id}/pickup", parcelId)
            .then()
            .statusCode(200)
            .body("status", equalTo("PICKED_UP"))

        RestAssured
            .given()
            .contentType(ContentType.JSON)
            .`when`()
            .post("/parcels/{id}/sorting", parcelId)
            .then()
            .statusCode(200)
            .body("status", equalTo("AT_SORTING_CENTER"))

        RestAssured
            .given()
            .contentType(ContentType.JSON)
            .`when`()
            .post("/parcels/{id}/dispatch", parcelId)
            .then()
            .statusCode(200)
            .body("status", equalTo("IN_TRANSIT"))

        RestAssured
            .given()
            .contentType(ContentType.JSON)
            .`when`()
            .post("/parcels/{id}/out-for-delivery", parcelId)
            .then()
            .statusCode(200)
            .body("status", equalTo("OUT_FOR_DELIVERY"))

        RestAssured
            .given()
            .contentType(ContentType.JSON)
            .`when`()
            .post("/parcels/{id}/deliver", parcelId)
            .then()
            .statusCode(200)
            .body("status", equalTo("DELIVERED"))
    }

    @Test
    fun `should return 404 when transition non existent parcel`() {
        val nonExistentId = UUID.randomUUID()

        RestAssured
            .given()
            .contentType(ContentType.JSON)
            .`when`()
            .post("/parcels/{id}/pickup", nonExistentId)
            .then()
            .statusCode(404)
            .body("code", equalTo("PARCEL_NOT_FOUND"))
    }

    @Test
    fun `should return 404 when parcel not found`() {
        val nonExistentId = UUID.randomUUID()

        RestAssured
            .given()
            .`when`()
            .get("/parcels/{id}", nonExistentId)
            .then()
            .statusCode(404)
            .body("code", equalTo("PARCEL_NOT_FOUND"))
            .body("message", equalTo("Parcel with id $nonExistentId not found"))
    }

    @Test
    fun `should return 400 when parcel id is invalid UUID`() {
        RestAssured
            .given()
            .`when`()
            .get("/parcels/{id}", "invalid-uuid-string")
            .then()
            .statusCode(400)
    }
}
