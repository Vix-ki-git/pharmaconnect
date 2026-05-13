package com.cts.mfrp.petz.tests.api;

import com.cts.mfrp.petz.api.specs.ApiSpecs;
import com.cts.mfrp.petz.base.BaseApiTest;
import com.cts.mfrp.petz.utils.ExtentReportManager;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import static com.cts.mfrp.petz.api.endpoints.ApiEndpoints.HOSPITALS_PUBLIC;
import static com.cts.mfrp.petz.api.endpoints.ApiEndpoints.HOSPITALS_PUBLIC_BY_ID;

public class TC401_HospitalsPublicTest extends BaseApiTest {

    private static final int SEEDED_HOSPITAL_ID = 1;

    @Test(description = "TC401.1 — GET /hospitals/public returns list with seeded VetCare hospital")
    public void listPublicHospitals_returnsSeeded() {
        ExtentReportManager.createTest("TC401.1 GET /hospitals/public",
                "Returns the seeded hospitals (auth required despite docs)");

        Response r = RestAssured.given(ApiSpecs.asUser()).when().get(HOSPITALS_PUBLIC);

        Assert.assertEquals(r.statusCode(), 200, "Body: " + r.asString());
        Assert.assertTrue(r.jsonPath().getList("data.id").contains(SEEDED_HOSPITAL_ID),
                "Seeded hospital id=" + SEEDED_HOSPITAL_ID + " should be present");
    }

    @Test(description = "TC401.2 — GET /hospitals/public/{id} returns single hospital")
    public void getPublicHospitalById_returnsOne() {
        ExtentReportManager.createTest("TC401.2 GET /hospitals/public/{id}",
                "Fetch the seeded VetCare hospital by id");

        Response r = RestAssured.given(ApiSpecs.asUser())
                .pathParam("id", SEEDED_HOSPITAL_ID)
                .when().get(HOSPITALS_PUBLIC_BY_ID);

        Assert.assertEquals(r.statusCode(), 200, "Body: " + r.asString());
        Assert.assertEquals(r.jsonPath().getInt("data.id"), SEEDED_HOSPITAL_ID);
        Assert.assertNotNull(r.jsonPath().getString("data.name"));
    }

    @Test(description = "TC401.3 — /hospitals/public needs auth despite being doc'd public")
    public void hospitalsPublic_noToken_returns403() {
        ExtentReportManager.createTest("TC401.3 /hospitals/public no token",
                "API.md says public but deployment requires auth — capture today's behaviour");

        Response r = RestAssured.given(ApiSpecs.baseRequestSpec()).when().get(HOSPITALS_PUBLIC);
        Assert.assertEquals(r.statusCode(), 403, "Body: " + r.asString());
    }
}
