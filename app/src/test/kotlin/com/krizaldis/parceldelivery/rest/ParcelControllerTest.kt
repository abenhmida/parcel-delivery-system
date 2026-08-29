package com.krizaldis.parceldelivery.rest

import com.krizaldis.parceldelivery.domain.Address
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import java.math.BigDecimal
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ParcelControllerTest {
    @LocalServerPort
    private var port: Int = 0

    @BeforeEach
    fun setUp() {
        RestAssured.port = port
    }

    @Test
    fun `should create parcel successfully`() {
        val request =
            CreateParcelRequest(
                sender =
                    AddressDTO(
                        name = "Alice Sender",
                        street = "Sender Street 1",
                        city = "Berlin",
                        postalCode = "10115",
                        country = "DE",
                    ),
                recipient =
                    AddressDTO(
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
            .statusCode(200)
            .body("id", notNullValue())
            .body("trackingNumber", notNullValue())
            .body("sender.name", equalTo("Alice Sender"))
            .body("sender.street", equalTo("Sender Street 1"))
            .body("sender.city", equalTo("Berlin"))
            .body("sender.postalCode", equalTo("10115"))
            .body("sender.country", equalTo("DE"))
            .body("recipient.name", equalTo("Bob Receiver"))
            .body("recipient.street", equalTo("Receiver Street 2"))
            .body("recipient.city", equalTo("Munich"))
            .body("recipient.postalCode", equalTo("80331"))
            .body("recipient.country", equalTo("DE"))
            .body("weight", equalTo(2.500f))
            .body("status", equalTo("CREATED"))
            .body("createdAt", notNullValue())
            .body("trackingEvents", hasSize<Any>(1))
            .body("trackingEvents[0].status", equalTo("CREATED"))
            .body("trackingEvents[0].occurredAt", notNullValue())
    }

    @Test
    fun `should get parcel by id successfully`() {
        val request =
            CreateParcelRequest(
                sender =
                    AddressDTO(
                        name = "Charlie Sender",
                        street = "Main St 10",
                        city = "Hamburg",
                        postalCode = "20095",
                        country = "DE",
                    ),
                recipient =
                    AddressDTO(
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
                .statusCode(200)
                .extract()
                .path("id")

        RestAssured
            .given()
            .contentType(ContentType.JSON)
            .`when`()
            .get("/parcels/{id}", createdParcelId)
            .then()
            .statusCode(200)
            .body("id", equalTo(createdParcelId))
            .body("sender.name", equalTo("Charlie Sender"))
            .body("recipient.name", equalTo("David Receiver"))
            .body("weight", equalTo(1.750f))
            .body("status", equalTo("CREATED"))
            .body("trackingEvents", hasSize<Any>(1))
            .body("trackingEvents[0].parcelId", equalTo(createdParcelId))
            .body("trackingEvents[0].status", equalTo("CREATED"))
    }

    @Test
    fun `should return 404 when parcel not found`() {
        val nonExistentId = UUID.randomUUID()

        RestAssured
            .given()
            .contentType(ContentType.JSON)
            .`when`()
            .get("/parcels/{id}", nonExistentId)
            .then()
            .statusCode(404)
    }

    @Test
    fun `should return 400 when parcel id is invalid UUID`() {
        RestAssured
            .given()
            .contentType(ContentType.JSON)
            .`when`()
            .get("/parcels/{id}", "invalid-uuid-string")
            .then()
            .statusCode(400)
    }
}
