package com.hevyfit.measurements.rest;

import io.quarkus.test.junit.QuarkusTest;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class HealthCheckTest {

    @Test
    void reportsUpWhenDatabaseIsAvailable() {
        int managementPort = ConfigProvider.getConfig().getValue("quarkus.management.port", Integer.class);

        given()
          .port(managementPort)
          .when().get("/q/health")
          .then()
             .statusCode(200)
             .body("status", is("UP"));
    }
}
