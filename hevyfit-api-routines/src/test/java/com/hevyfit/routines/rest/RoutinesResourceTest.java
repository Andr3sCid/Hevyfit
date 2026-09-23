package com.hevyfit.routines.rest;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class RoutinesResourceTest {
    @Test
    void testHelloEndpoint() {
        given()
          .when().get("/api/routines")
          .then()
             .statusCode(200)
             .body(is("Hello from Quarkus REST"));
    }

}