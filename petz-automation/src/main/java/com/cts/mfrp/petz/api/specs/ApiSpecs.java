package com.cts.mfrp.petz.api.specs;

import com.cts.mfrp.petz.api.auth.Role;
import com.cts.mfrp.petz.api.auth.TokenManager;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import static com.cts.mfrp.petz.constants.AppConstants.API_BASE_URL;

public final class ApiSpecs {

    private ApiSpecs() {}

    /**
     * Apply once per JVM: tell REST Assured to use a Jackson mapper that drops null fields,
     * so partial-update POJOs don't serialise unset properties as `"foo":null`.
     */
    public static void configureJacksonOnce() {
        if (configured) return;
        ObjectMapper mapper = new ObjectMapper()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(
                new ObjectMapperConfig().jackson2ObjectMapperFactory((type, charset) -> mapper));
        configured = true;
    }
    private static boolean configured = false;

    public static RequestSpecification baseRequestSpec() {
        return new RequestSpecBuilder()
                .setBaseUri(API_BASE_URL)
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .log(LogDetail.URI)
                .log(LogDetail.METHOD)
                .build();
    }

    public static RequestSpecification authedRequestSpec(String bearerToken) {
        return new RequestSpecBuilder()
                .addRequestSpecification(baseRequestSpec())
                .addHeader("Authorization", "Bearer " + bearerToken)
                .build();
    }

    public static RequestSpecification asRole(Role role) {
        return authedRequestSpec(TokenManager.tokenFor(role));
    }

    public static RequestSpecification asAdmin()    { return asRole(Role.ADMIN); }
    public static RequestSpecification asNgo()      { return asRole(Role.NGO); }
    public static RequestSpecification asHospital() { return asRole(Role.HOSPITAL); }
    public static RequestSpecification asUser()     { return asRole(Role.USER); }

    public static ResponseSpecification expectStatus(int statusCode) {
        return new ResponseSpecBuilder()
                .expectStatusCode(statusCode)
                .build();
    }
}
