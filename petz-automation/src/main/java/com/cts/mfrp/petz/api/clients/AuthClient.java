package com.cts.mfrp.petz.api.clients;

import com.cts.mfrp.petz.api.specs.ApiSpecs;
import com.cts.mfrp.petz.models.auth.LoginRequest;
import com.cts.mfrp.petz.models.auth.RegisterRequest;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import static com.cts.mfrp.petz.api.endpoints.ApiEndpoints.AUTH_LOGIN;
import static com.cts.mfrp.petz.api.endpoints.ApiEndpoints.AUTH_REGISTER;

public class AuthClient {

    public Response login(LoginRequest body) {
        return RestAssured
                .given(ApiSpecs.baseRequestSpec())
                .body(body)
                .when()
                .post(AUTH_LOGIN);
    }

    public Response login(String email, String password) {
        return login(new LoginRequest(email, password));
    }

    public Response loginRaw(String rawBody) {
        return RestAssured
                .given(ApiSpecs.baseRequestSpec())
                .body(rawBody)
                .when()
                .post(AUTH_LOGIN);
    }

    public Response register(RegisterRequest body) {
        return RestAssured
                .given(ApiSpecs.baseRequestSpec())
                .body(body)
                .when()
                .post(AUTH_REGISTER);
    }
}
