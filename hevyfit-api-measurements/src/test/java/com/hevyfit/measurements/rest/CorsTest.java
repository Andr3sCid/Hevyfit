package com.hevyfit.measurements.rest;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class CorsTest {

    @Test
    void allowsConfiguredWebOrigin() {
        given()
          .header("Origin", "http://localhost:5173")
          .header("Access-Control-Request-Method", "GET")
          .when().options("/api/measurements")
          .then()
             .statusCode(200)
             .header("Access-Control-Allow-Origin", is("http://localhost:5173"));
    }
}
