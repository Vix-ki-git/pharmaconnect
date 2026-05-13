package com.cts.mfrp.petz.tests.api;

import com.cts.mfrp.petz.api.specs.ApiSpecs;
import com.cts.mfrp.petz.base.BaseApiTest;
import com.cts.mfrp.petz.utils.ExtentReportManager;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import static com.cts.mfrp.petz.api.endpoints.ApiEndpoints.ADOPTION_ANIMALS;
import static com.cts.mfrp.petz.api.endpoints.ApiEndpoints.ADOPTION_ANIMALS_BY_ID;

public class TC701_AdoptionPublicTest extends BaseApiTest {

    private static final int SEEDED_ANIMAL_ID = 1;

    @Test(description = "TC701.1 — GET /adoption/animals returns list including seeded Max")
    public void listAnimals_includesSeeded() {
        ExtentReportManager.createTest("TC701.1 GET /adoption/animals",
                "Authenticated USER lists adoptable animals (seeded data)");

        Response r = RestAssured.given(ApiSpecs.asUser()).when().get(ADOPTION_ANIMALS);
        Assert.assertEquals(r.statusCode(), 200, "Body: " + r.asString());
        Assert.assertTrue(r.jsonPath().getList("data.id").contains(SEEDED_ANIMAL_ID),
                "Seeded animal id=" + SEEDED_ANIMAL_ID + " should be present");
    }

    @Test(description = "TC701.2 — GET /adoption/animals/{id} returns single animal")
    public void getAnimalById_returnsOne() {
        ExtentReportManager.createTest("TC701.2 GET /adoption/animals/{id}",
                "Fetch the seeded Max by id");

        Response r = RestAssured.given(ApiSpecs.asUser())
                .pathParam("id", SEEDED_ANIMAL_ID)
                .when().get(ADOPTION_ANIMALS_BY_ID);

        Assert.assertEquals(r.statusCode(), 200, "Body: " + r.asString());
        Assert.assertEquals(r.jsonPath().getInt("data.id"), SEEDED_ANIMAL_ID);
        Assert.assertEquals(r.jsonPath().getString("data.species"), "Dog");
    }

    @Test(description = "TC701.3 — GET /adoption/my-applications returns USER's applications")
    public void myApplications_returnsList() {
        ExtentReportManager.createTest("TC701.3 GET /adoption/my-applications",
                "Authenticated USER lists their submitted applications");

        Response r = RestAssured.given(ApiSpecs.asUser()).when().get("/adoption/my-applications");
        Assert.assertEquals(r.statusCode(), 200, "Body: " + r.asString());
        Assert.assertTrue(r.jsonPath().getBoolean("success"));
    }
}
