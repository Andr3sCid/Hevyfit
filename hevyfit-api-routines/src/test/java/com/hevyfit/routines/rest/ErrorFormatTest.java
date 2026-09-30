package com.hevyfit.routines.rest;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class ErrorFormatTest {

    @Test
    void validationErrorFollowsCommonFormat() {
        given()
          .when().get("/test/error-format/validation")
          .then()
             .statusCode(400)
             .body("code", is("VALIDATION_ERROR"))
             .body("details[0].field", is("name"))
             .body("details[0].code", is("FIELD_REQUIRED"));
    }

    @Test
    void notFoundErrorFollowsCommonFormat() {
        given()
          .when().get("/test/error-format/not-found")
          .then()
             .statusCode(404)
             .body("code", is("NOT_FOUND"));
    }
}
