package com.cts.mfrp.petz.tests.api;

import com.cts.mfrp.petz.api.specs.ApiSpecs;
import com.cts.mfrp.petz.base.BaseApiTest;
import com.cts.mfrp.petz.utils.ExtentReportManager;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC901_NotificationsTest extends BaseApiTest {

    @Test(description = "TC901.1 — GET /notifications returns list")
    public void notifications_returnsList() {
        ExtentReportManager.createTest("TC901.1 GET /notifications",
                "Authenticated USER lists notifications");

        Response r = RestAssured.given(ApiSpecs.asUser()).when().get("/notifications");
        Assert.assertEquals(r.statusCode(), 200, "Body: " + r.asString());
        Assert.assertTrue(r.jsonPath().getBoolean("success"));
    }

    @Test(description = "TC901.2 — GET /notifications/unread returns unread list")
    public void notificationsUnread_returnsList() {
        ExtentReportManager.createTest("TC901.2 GET /notifications/unread",
                "Returns unread notifications only");

        Response r = RestAssured.given(ApiSpecs.asUser()).when().get("/notifications/unread");
        Assert.assertEquals(r.statusCode(), 200, "Body: " + r.asString());
    }

    @Test(description = "TC901.3 — GET /notifications/unread/count returns numeric count")
    public void unreadCount_returnsNumber() {
        ExtentReportManager.createTest("TC901.3 GET /notifications/unread/count",
                "Returns a count for the bell badge");

        Response r = RestAssured.given(ApiSpecs.asUser()).when().get("/notifications/unread/count");
        Assert.assertEquals(r.statusCode(), 200, "Body: " + r.asString());
        Assert.assertNotNull(r.jsonPath().get("data"), "data should hold the count");
    }

    @Test(description = "TC901.4 — /notifications without token returns 403")
    public void notifications_noToken_returns403() {
        ExtentReportManager.createTest("TC901.4 /notifications no token",
                "Authenticated endpoint requires Bearer");

        Response r = RestAssured.given(ApiSpecs.baseRequestSpec()).when().get("/notifications");
        Assert.assertEquals(r.statusCode(), 403, "Body: " + r.asString());
    }

    @Test(description = "TC901.5 — PATCH /notifications/{id}/read marks a notification as read")
    public void markOneAsRead_returns200() {
        ExtentReportManager.createTest("TC901.5 PATCH /notifications/{id}/read",
                "Fetch the first notification id, mark it read (idempotent)");

        Response list = RestAssured.given(ApiSpecs.asUser()).when().get("/notifications");
        Assert.assertEquals(list.statusCode(), 200);
        Integer notificationId = list.jsonPath().getInt("data[0].id");
        Assert.assertNotNull(notificationId,
                "Seed user is expected to have at least one notification");

        Response patch = RestAssured.given(ApiSpecs.asUser())
                .pathParam("id", notificationId)
                .when().patch("/notifications/{id}/read");

        Assert.assertEquals(patch.statusCode(), 200, "Body: " + patch.asString());
    }

    @Test(description = "TC901.6 — PATCH /notifications/read-all marks all as read (idempotent)")
    public void markAllAsRead_returns200() {
        ExtentReportManager.createTest("TC901.6 PATCH /notifications/read-all",
                "Bulk-mark every unread notification as read");

        Response r = RestAssured.given(ApiSpecs.asUser()).when().patch("/notifications/read-all");
        Assert.assertEquals(r.statusCode(), 200, "Body: " + r.asString());

        // After a read-all, unread count must be zero
        Response count = RestAssured.given(ApiSpecs.asUser()).when().get("/notifications/unread/count");
        Assert.assertEquals(count.jsonPath().getInt("data"), 0,
                "Unread count should be 0 right after read-all");
    }
}
