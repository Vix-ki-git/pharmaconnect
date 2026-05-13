package com.cts.mfrp.petz.tests.api;

import com.cts.mfrp.petz.api.specs.ApiSpecs;
import com.cts.mfrp.petz.base.BaseApiTest;
import com.cts.mfrp.petz.utils.ExtentReportManager;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

import static com.cts.mfrp.petz.api.endpoints.ApiEndpoints.USERS_ME;

public class TC201_UserProfileTest extends BaseApiTest {

    @Test(description = "TC201.1 — GET /users/me returns the USER's profile")
    public void getMyProfile_returnsProfile() {
        ExtentReportManager.createTest("TC201.1 GET /users/me",
                "Authenticated USER fetches own profile");

        Response r = RestAssured.given(ApiSpecs.asUser()).when().get(USERS_ME);

        Assert.assertEquals(r.statusCode(), 200, "Body: " + r.asString());
        Assert.assertTrue(r.jsonPath().getBoolean("success"));
        Assert.assertEquals(r.jsonPath().getString("data.email"), "user@petz.com");
        Assert.assertEquals(r.jsonPath().getString("data.role"),  "USER");
    }

    @Test(description = "TC201.2 — PUT /users/me updates name and persists")
    public void updateProfile_persists() {
        ExtentReportManager.createTest("TC201.2 PUT /users/me",
                "Update profile; subsequent GET returns updated value");

        String newName = "Test User " + System.currentTimeMillis();
        Map<String, Object> body = Map.of("name", newName);

        Response put = RestAssured.given(ApiSpecs.asUser()).body(body).when().put(USERS_ME);
        Assert.assertEquals(put.statusCode(), 200, "Body: " + put.asString());
        Assert.assertEquals(put.jsonPath().getString("data.name"), newName);

        Response get = RestAssured.given(ApiSpecs.asUser()).when().get(USERS_ME);
        Assert.assertEquals(get.jsonPath().getString("data.name"), newName);
    }

    @Test(description = "TC201.3 — GET /users/me without token returns 403")
    public void getMyProfile_noToken_returns403() {
        ExtentReportManager.createTest("TC201.3 /users/me no token",
                "Spring Security default returns 403 with empty body for missing Bearer");

        Response r = RestAssured.given(ApiSpecs.baseRequestSpec()).when().get(USERS_ME);

        Assert.assertEquals(r.statusCode(), 403, "Body: " + r.asString());
    }
}
