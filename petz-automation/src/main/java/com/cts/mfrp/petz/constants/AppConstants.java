package com.cts.mfrp.petz.constants;

public class AppConstants {

    public static final String BASE_URL     = "https://stellular-taffy-e3ee7a.netlify.app";
    public static final String HOME_URL     = BASE_URL + "/";
    public static final String LOGIN_URL    = BASE_URL + "/auth/login";
    public static final String REGISTER_URL = BASE_URL + "/auth/register";
    public static final String DASHBOARD_URL       = BASE_URL + "/dashboard";
    public static final String RESCUE_REPORT_URL   = BASE_URL + "/rescue/report";
    public static final String RESCUE_URL          = BASE_URL + "/rescue";
    public static final String ADOPTION_ANIMALS_URL= BASE_URL + "/adoption/animals";
    public static final String ADOPTION_MY_URL     = BASE_URL + "/adoption/my";
    public static final String APPOINTMENTS_URL    = BASE_URL + "/appointments";
    public static final String APPOINTMENTS_BOOK_URL = BASE_URL + "/appointments/book";
    public static final String NOTIFICATIONS_URL     = BASE_URL + "/notifications";

    public static final String PAGE_TITLE   = "Petz \u2013 Animal Welfare Platform";

    public static final int IMPLICIT_WAIT  = 10;
    public static final int EXPLICIT_WAIT  = 15;
    public static final int PAGE_LOAD_WAIT = 30;

    // Pet Owner test credentials \u2014 override via -DpetOwnerEmail / -DpetOwnerPassword
    public static final String PET_OWNER_EMAIL    =
            System.getProperty("petOwnerEmail",    "user@petz.com");
    public static final String PET_OWNER_PASSWORD =
            System.getProperty("petOwnerPassword", "admin@petz123");
    public static final String PET_OWNER_NAME     =
            System.getProperty("petOwnerName",     "Test");

    public static final String REPORT_PATH     = "test-output/reports/ExtentReport.html";
    public static final String SCREENSHOT_PATH = "test-output/screenshots/";

    // \u2500\u2500 API testing \u2500\u2500
    // Deployed backend mounts everything under the /api context path.
    // Override at runtime with -Dapi.base.url=http://localhost:8081/api
    public static final String API_BASE_URL = System.getProperty(
            "api.base.url", "https://petz-production.up.railway.app/api");

    public static final int API_CONNECT_TIMEOUT_MS = 10_000;
    public static final int API_READ_TIMEOUT_MS    = 20_000;

    private AppConstants() {}
}