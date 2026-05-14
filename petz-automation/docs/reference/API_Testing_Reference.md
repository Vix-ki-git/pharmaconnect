
# PETZ API Testing — Complete Reference

> One-stop reference for the REST Assured API test suite in `petz-automation/`.
> Read this end-to-end to answer any question about **what the code does**, **what was built and why**, and **how the underlying tools (REST Assured + TestNG) work**.

---

## Table of contents

1. [What we built — at a glance](#1-what-we-built--at-a-glance)
2. [The deployed backend we test against](#2-the-deployed-backend-we-test-against)
3. [Project layout (file by file)](#3-project-layout-file-by-file)
4. [Framework architecture](#4-framework-architecture)
5. [Test classes (TC101 – TC1101)](#5-test-classes-tc101--tc1101)
6. [Test patterns used (with code)](#6-test-patterns-used-with-code)
7. [Coverage matrix — 49 of 66 endpoints](#7-coverage-matrix--49-of-66-endpoints)
8. [Deployed-backend quirks the suite captures](#8-deployed-backend-quirks-the-suite-captures)
9. [How to run the suite](#9-how-to-run-the-suite)
10. [Dependencies (why each is here)](#10-dependencies-why-each-is-here)
11. [REST Assured concepts cheat sheet](#11-rest-assured-concepts-cheat-sheet)
12. [TestNG concepts cheat sheet](#12-testng-concepts-cheat-sheet)
13. [Interview Q & A](#13-interview-q--a)

---

## 1. What we built — at a glance

A **REST Assured + TestNG** API test suite that lives alongside the existing Selenium UI suite in the same Maven project.

- **62 tests across 14 test classes**, all green against the deployed Railway backend.
- Covers **49 of 66 documented endpoints** (~74 %) across all 11 API modules.
- Two extra dependencies on top of the existing UI stack: `rest-assured` and `jackson-databind`.
- Runs in ~110 s against the live URL.

The suite is intentionally **GET-heavy** with mutating endpoints exercised through `create → assert → cleanup` lifecycle tests. Anything that would clobber the shared seed accounts (admin approve/toggle, NGO/Hospital profile overwrites) is left for a follow-up so the suite can be re-run safely.

---

## 2. The deployed backend we test against

- **Base URL:** `https://petz-production.up.railway.app/api`
- **Context path** is `/api` (set in the backend's `application.properties`), so REST paths in code are written as `/auth/login`, `/users/me`, `/pets`, etc.
- **Auth model:** JWT bearer tokens issued by `POST /auth/login`. Every protected endpoint requires `Authorization: Bearer <token>`.
- **Roles:** `USER`, `NGO`, `HOSPITAL`, `ADMIN`. Authorization is enforced at the controller level.
- **Response wrapper:**
  ```json
  { "success": true, "data": { ... }, "message": "Success" }
  ```
- **Validation errors (400):**
  ```json
  { "success": false, "data": { "email": "must not be blank" }, "message": "Validation failed." }
  ```
- **Authorization errors (403, wrong role):**
  ```json
  { "success": false, "data": null, "message": "Access denied." }
  ```

**Seed accounts** (provided by the project owner, all share the same password `admin@petz123`):

| Role     | Email             |
|----------|-------------------|
| ADMIN    | admin@petz.com    |
| NGO      | ngo@petz.com      |
| HOSPITAL | hospital@petz.com |
| USER     | user@petz.com     |

---

## 3. Project layout (file by file)

```
petz-automation/
├── pom.xml                                 ← Maven build, deps, surefire wired to testng.xml
├── testng.xml                              ← TestNG suite definition (UI + API blocks)
├── docs/
│   ├── API.md                              ← Source of truth for the deployed API
│   └── reference/API_Testing_Reference.md  ← (this document)
└── src/
    ├── main/java/com/cts/mfrp/petz/
    │   ├── constants/
    │   │   └── AppConstants.java           ← API_BASE_URL (override with -Dapi.base.url=…)
    │   ├── base/
    │   │   ├── BaseTest.java               ← UI base (Selenium)
    │   │   └── BaseApiTest.java            ← API base (@BeforeSuite config, Extent hook)
    │   ├── api/
    │   │   ├── auth/
    │   │   │   ├── Role.java               ← enum { ADMIN, NGO, HOSPITAL, USER }
    │   │   │   └── TokenManager.java       ← caches one JWT per role for the suite
    │   │   ├── clients/
    │   │   │   └── AuthClient.java         ← REST Assured wrapper for /auth endpoints
    │   │   ├── endpoints/
    │   │   │   └── ApiEndpoints.java       ← URL path constants
    │   │   └── specs/
    │   │       └── ApiSpecs.java           ← RequestSpec builders + asAdmin/asNgo/...
    │   ├── models/
    │   │   ├── ApiResponse.java            ← generic { success, data, message } wrapper
    │   │   ├── ApiError.java               ← error wrapper
    │   │   └── auth/
    │   │       ├── LoginRequest.java       ← { email, password }
    │   │       ├── RegisterRequest.java    ← { name, email, password, phone, role, address, city }
    │   │       └── AuthResponse.java       ← { token, userId, email, name, role, isApproved }
    │   └── utils/
    │       ├── DriverFactory.java          ← (UI suite, untouched)
    │       ├── ScreenshotUtil.java         ← (UI suite, untouched)
    │       └── ExtentReportManager.java    ← shared HTML report (UI + API)
    └── test/java/com/cts/mfrp/petz/tests/api/
        ├── TC101_AuthLoginTest.java
        ├── TC102_AuthRegisterTest.java
        ├── TC201_UserProfileTest.java
        ├── TC301_PetsTest.java
        ├── TC401_HospitalsPublicTest.java
        ├── TC402_HospitalsRoleTest.java
        ├── TC501_AppointmentsTest.java
        ├── TC601_RescueTest.java
        ├── TC701_AdoptionPublicTest.java
        ├── TC702_AdoptionNgoTest.java
        ├── TC801_NgoTest.java
        ├── TC901_NotificationsTest.java
        ├── TC1001_AdminTest.java
        └── TC1101_AuthBoundariesTest.java
```

---

## 4. Framework architecture

### 4.1 `AppConstants` — single place for URL config

```java
public static final String API_BASE_URL = System.getProperty(
        "api.base.url", "https://petz-production.up.railway.app/api");
```

The `System.getProperty(…, default)` trick lets you override the URL from the command line without code changes:

```powershell
mvn test "-Dapi.base.url=http://localhost:8081/api"
```

### 4.2 `ApiEndpoints` — path constants

Avoids "magic strings" scattered through tests. If a path ever changes, you fix one constant.

```java
public static final String AUTH_LOGIN     = "/auth/login";
public static final String USERS_ME       = "/users/me";
public static final String PETS_BY_ID     = "/pets/{id}";
public static final String ADOPTION_ANIMALS_BY_ID = "/adoption/animals/{id}";
```

Curly braces are REST Assured **path-parameter placeholders**, filled later via `.pathParam("id", value)`.

### 4.3 `ApiSpecs` — reusable request/response specs

The heart of the framework. Three things happen here:

1. **`baseRequestSpec()`** — every request gets `baseURI`, `Content-Type: application/json`, `Accept: application/json`, and URI/method logging.
2. **`authedRequestSpec(token)` / `asUser()` / `asAdmin()` / `asNgo()` / `asHospital()`** — adds the `Authorization: Bearer …` header by pulling a cached token from `TokenManager`.
3. **`configureJacksonOnce()`** — installs a Jackson `ObjectMapper` with `JsonInclude.NON_NULL` so request POJOs don't serialize unset fields as `"foo": null`. Without this, `new LoginRequest("e", null)` would send `{"email":"e","password":null}` and the backend's `@NotBlank` would treat it as blank — but the explicit null could trigger different error paths.

```java
public static RequestSpecification asUser() {
    return authedRequestSpec(TokenManager.tokenFor(Role.USER));
}
```

### 4.4 `TokenManager` — log in once per suite

A `HashMap<Role, String>` cache. The first time a test asks for the USER token, we call `POST /auth/login`, store the JWT, and return it. Every subsequent request for any role reuses the cached token.

```java
public static synchronized String tokenFor(Role role) {
    return TOKENS.computeIfAbsent(role, TokenManager::login);
}
```

The `synchronized` keyword is there because TestNG can run tests in parallel; without it two threads could simultaneously call `/auth/login` for the same role.

If login fails, we **fail fast** by throwing `IllegalStateException` — better than every dependent test exploding with a useless 403.

### 4.5 `AuthClient` — Page Object Model for APIs

`AuthClient` is the API analog of a Page Object: it hides REST Assured details behind business-named methods.

```java
public Response login(String email, String password) {
    return RestAssured
            .given(ApiSpecs.baseRequestSpec())
            .body(new LoginRequest(email, password))
            .when()
            .post(AUTH_LOGIN);
}
```

Tests stay readable: `auth.login("user@petz.com", "pwd")` — no REST Assured boilerplate in the test body.

> We chose to keep only `AuthClient` because every other module follows the same shape; in a larger suite you'd add `PetsClient`, `NotificationsClient`, etc. Tests outside the auth module call REST Assured directly via `ApiSpecs.asUser()` to keep the file count down for an interview-sized project.

### 4.6 `BaseApiTest` — TestNG lifecycle hook

```java
@BeforeSuite(alwaysRun = true)
public void initApiSuite() {
    RestAssured.baseURI = API_BASE_URL;
    ApiSpecs.configureJacksonOnce();
    ExtentReportManager.initReport();
}
```

- **`@BeforeSuite`** runs once before any test class in the suite.
- Every API test class extends `BaseApiTest`, so they all inherit this setup.
- Extent reports (the HTML test report) are shared with the UI suite — one report file for both.

### 4.7 POJOs (request/response models)

Plain Java classes with getters/setters. REST Assured + Jackson serialize them to JSON automatically when you do `.body(loginRequest)`, and deserialize with `.as(LoginResponse.class)`.

```java
public class LoginRequest {
    private String email;
    private String password;
    // ctor + getters/setters
}
```

**Why POJOs over `Map.of(...)`?** POJOs are typed (compile error if you misspell a field) and document the request shape. They earn their keep for auth where the contract is stable. For ad-hoc payloads (creating a one-off pet/doctor/animal in a test), `Map.of(...)` is fine and avoids creating a class file per resource.

---

## 5. Test classes (TC101 – TC1101)

| ID | File | Tests | What it covers |
|----|------|-------|----------------|
| TC101 | `TC101_AuthLoginTest` | 4 | Login validation: empty body, bad email, unknown user (404), malformed JSON (500 — documented bug) |
| TC102 | `TC102_AuthRegisterTest` | 5 | Register happy path, empty body, invalid email, short password, duplicate email |
| TC201 | `TC201_UserProfileTest` | 3 | GET/PUT `/users/me`, no-token 403 |
| TC301 | `TC301_PetsTest` | 3 | Pet CRUD lifecycle (POST → GET → PUT → DELETE), no-token 403, trailing-slash 500 quirk |
| TC401 | `TC401_HospitalsPublicTest` | 5 | List hospitals, by-id, doctors, doctor slots, no-token 403 |
| TC402 | `TC402_HospitalsRoleTest` | 3 | HOSPITAL profile happy, USER→profile 403, doctor CRUD lifecycle |
| TC501 | `TC501_AppointmentsTest` | 5 | `/appointments/my`, `/slots`, `/hospital`, USER→hospital 403, book+cancel lifecycle |
| TC601 | `TC601_RescueTest` | 4 | `/rescue/my`, `/rescue/ngo`, role mismatch 403, GET by id |
| TC701 | `TC701_AdoptionPublicTest` | 4 | List animals, by-id, my-applications, duplicate apply rejection |
| TC702 | `TC702_AdoptionNgoTest` | 4 | NGO animals list, applications list, role mismatch, animal CRUD lifecycle |
| TC801 | `TC801_NgoTest` | 4 | Public list/by-id, NGO profile, role mismatch |
| TC901 | `TC901_NotificationsTest` | 6 | List, unread, count, no-token, mark one read, mark all read |
| TC1001 | `TC1001_AdminTest` | 9 | DataProvider sweep across 7 admin listings + USER→admin 403 + no-token 403 |
| TC1101 | `TC1101_AuthBoundariesTest` | 3 | No Bearer 403, garbage Bearer 403, role mismatch 403 |

**62 tests total.**

---

## 6. Test patterns used (with code)

### 6.1 The classic `given().when().then()` style

REST Assured's DSL maps to **BDD** thinking:

```
given()   — what's the input (auth, body, headers, path params)?
when()    — what action do we take (GET/POST/PUT/PATCH/DELETE)?
then()    — what do we assert?
```

In this suite we usually split `then()` into explicit `Assert.assertEquals(...)` calls because they produce clearer failure messages than chained REST Assured matchers — easier to read for someone new to the library.

```java
Response r = RestAssured
        .given(ApiSpecs.asUser())          // given
        .when().get("/users/me");          // when

Assert.assertEquals(r.statusCode(), 200);  // then
Assert.assertEquals(r.jsonPath().getString("data.email"), "user@petz.com");
```

### 6.2 Lifecycle test with `try/finally` cleanup

The pattern used in TC301, TC402, TC501, TC702 for safe mutating tests:

```java
// 1. Create — capture the new id
Response create = RestAssured.given(spec).body(createBody).when().post(...);
Integer id = create.jsonPath().getInt("data.id");

try {
    // 2. Update / read assertions
    Response update = RestAssured.given(spec).pathParam("id", id).body(updateBody)
            .when().put(...);
    Assert.assertEquals(update.statusCode(), 200);
    // ...
} finally {
    // 3. Delete — runs even if an assertion above fails
    RestAssured.given(spec).pathParam("id", id).when().delete(...);
}
```

**Why `try/finally`?** If an assertion fails mid-test, the JVM throws an `AssertionError`. Without `finally`, the cleanup `delete()` would be skipped, leaving an orphan record. With `finally`, cleanup always runs, so the next test run starts from the same state.

### 6.3 Role-keyed authentication

Every authenticated request goes through one of four helpers:

```java
ApiSpecs.asUser()      // pulls cached USER token
ApiSpecs.asAdmin()
ApiSpecs.asNgo()
ApiSpecs.asHospital()
```

Tests never construct `Authorization` headers by hand. That keeps the test files focused on **what they're testing**, not **how to authenticate**.

### 6.4 `@DataProvider` — table-driven tests

TC1001 sweeps seven admin listings with one method:

```java
@DataProvider(name = "adminListings")
public Object[][] adminListings() {
    return new Object[][] {
            { "/admin/users" },
            { "/admin/pending-approvals" },
            { "/admin/ngos" },
            // ...
    };
}

@Test(dataProvider = "adminListings")
public void adminListing_returns200(String path) {
    Response r = RestAssured.given(ApiSpecs.asAdmin()).when().get(path);
    Assert.assertEquals(r.statusCode(), 200);
}
```

TestNG runs the `@Test` method once per row. Each row becomes a separate test result, so a failure on `/admin/ngos` is reported distinctly from a failure on `/admin/users`.

### 6.5 `dependsOnMethods` — ordering within a class

TC102 uses this to ensure register-happy-path runs before duplicate-email check:

```java
@Test(dependsOnMethods = "registerUser_happyPath")
public void registerDuplicateEmail_returnsError() { ... }
```

If `registerUser_happyPath` fails, the duplicate check is **skipped** (not failed) — which is the right signal: you can't meaningfully test "duplicate" if "create" itself broke.

### 6.6 `jsonPath()` — assert without POJO mapping

For 90 % of assertions, REST Assured's `JsonPath` is faster than mapping to a POJO:

```java
r.jsonPath().getString("data.email")       // "user@petz.com"
r.jsonPath().getInt("data.id")             // 6
r.jsonPath().getBoolean("success")         // true
r.jsonPath().getList("data.id")            // [1, 2, 3]
r.jsonPath().getList("data.id").contains(petId)
r.jsonPath().getInt("data[0].id")          // first element of an array
```

Dotted path syntax mirrors the JSON structure. Brackets index arrays.

### 6.7 Pulling state from one request to use in another

TC601 fetches a rescue id from `/rescue/my` then uses it in `/rescue/{id}`:

```java
Response mine = RestAssured.given(ApiSpecs.asUser()).when().get("/rescue/my");
Integer rescueId = mine.jsonPath().getInt("data[0].id");

Response one = RestAssured.given(ApiSpecs.asUser())
        .pathParam("id", rescueId).when().get("/rescue/{id}");
```

This makes the test **robust** against changes in seed data — we don't hard-code `id = 1`.

---

## 7. Coverage matrix — 49 of 66 endpoints

### Covered

| Module | Coverage | Endpoints |
|--------|----------|-----------|
| Auth                  | 2 / 2  | login, register |
| User                  | 2 / 3  | GET, PUT `/users/me` |
| Pets                  | 5 / 6  | POST, GET `/my`, GET `/{id}`, PUT `/{id}`, DELETE `/{id}` |
| Hospitals — public    | 4 / 4  | list, by-id, doctors, doctor slots |
| Hospitals — role      | 4 / 6  | GET `/profile`, POST/PUT/DELETE `/profile/doctors` |
| Appointments          | 5 / 6  | POST, GET `/my`, GET `/slots`, GET `/hospital`, DELETE |
| Rescue                | 3 / 6  | GET `/my`, `/ngo`, `/{id}` |
| Adoption              | 6 / 11 | GET `/animals`, `/animals/{id}`, POST `/apply`, GET `/my-applications`, GET `/ngo/animals`, GET `/ngo/applications` + animal CRUD lifecycle (POST/PUT/DELETE) |
| NGO                   | 3 / 5  | public list, public by-id, `/profile` |
| Notifications         | 5 / 5  | list, unread, count, mark-read, read-all |
| Admin                 | 7 / 12 | All 7 listing endpoints |

### Not covered — and why

| Category | Endpoints | Reason |
|----------|-----------|--------|
| **Multipart photo uploads** | 6 (`/users/me/photo`, `/pets/{id}/photo`, `/hospitals/profile/logo`, `/ngo/profile/logo`, `/adoption/ngo/animals/{id}/photo`, `POST /rescue`) | Need binary image fixtures and `multipart/form-data` setup — deliberately skipped |
| **Admin mutating PATCHes** | 5 (`/admin/users/{id}/approve|toggle`, `/admin/ngos/{id}/verify|toggle`, `/admin/hospitals/{id}/toggle`) | Could lock out or disable a seed account; suite must be safely re-runnable |
| **HOSPITAL/NGO profile writes** | 2 (`POST /hospitals/profile`, `POST /ngo/profile`) | Overwrite the seeded VetCare / Chennai Animal Rescue profiles |
| **Cross-role flows** | 1 (`PATCH /appointments/{id}/status`) | Requires USER + HOSPITAL coordination — possible, kept out for simplicity |
| **Rescue mutating ops** | 2 (`POST /rescue/{id}/respond`, `POST /rescue/{id}/complete`) | Mutate rescue state without a clean rollback path |
| **NGO review** | 1 (`PATCH /adoption/ngo/applications/{id}/review`) | Mutates existing application state |

---

## 8. Deployed-backend quirks the suite captures

The deployed backend differs from `API.md` in five places. We assert the **deployed** behaviour, not the docs, so the suite stays green. Each is flagged in the test comment so a future engineer can flip the expectation when the backend is fixed.

1. **Trailing-slash 500** — `POST /pets/` (with `/`) returns `500 "No static resource pets."`. Use `/pets` (no slash). *Spring 6 default `useTrailingSlashMatch = false`.* Captured by **TC301.3**.

2. **"Public" endpoints need auth** — `/hospitals/public`, `/ngo/public`, `/adoption/animals` all 403 without a token despite `API.md` saying public. Captured by **TC401.3**.

3. **Auth failure = 403, not 401** — Missing or invalid Bearer token returns 403 with an empty body (Spring Security's default `AccessDeniedHandler`). Captured by **TC1101.1**, **TC1101.2**.

4. **Unknown user on login = 404** — `POST /auth/login` with a non-existent email returns `404 "User not found."` (and incidentally leaks user existence). Captured by **TC101.3**.

5. **Malformed JSON = 500** — Backend has no `HttpMessageNotReadableException` handler, so JSON parse errors propagate as 500 with the parser message. Captured by **TC101.4**.

---

## 9. How to run the suite

```powershell
# Full TestNG suite (UI + API) per testng.xml
mvn test

# API tests only (wildcard match)
mvn test "-Dtest=TC10*Test,TC11*Test,TC2*Test,TC3*Test,TC4*Test,TC5*Test,TC6*Test,TC7*Test,TC8*Test,TC9*Test"

# Single class
mvn test "-Dtest=TC101_AuthLoginTest"

# Single method
mvn test "-Dtest=TC101_AuthLoginTest#loginEmptyBody_returns400"

# Target a different backend (e.g. a local instance)
mvn test "-Dapi.base.url=http://localhost:8081/api"
```

**Where the reports go:**
- TestNG HTML: `target/surefire-reports/index.html`
- Per-class text: `target/surefire-reports/com.cts.mfrp.petz.tests.api.<Class>.txt`
- ExtentReports HTML: `test-output/reports/ExtentReport.html`

---

## 10. Dependencies (why each is here)

| Dependency | Why we need it |
|------------|----------------|
| `io.rest-assured:rest-assured` | The HTTP client + DSL we test with |
| `com.fasterxml.jackson.core:jackson-databind` | Lets REST Assured serialize POJOs in `.body(pojo)` and deserialize in `.as(Class)` |
| `org.testng:testng` | Test runner (`@Test`, `@BeforeSuite`, `@DataProvider`, Assert) |
| `com.aventstack:extentreports` | Shared HTML report with the UI suite |
| `org.seleniumhq.selenium:selenium-java`, `webdrivermanager` | UI suite — not used by API tests but in the same Maven module |
| `commons-io` | File helpers (used by the UI screenshot util) |

For an interview: **"We added only two API-specific dependencies — `rest-assured` and `jackson-databind`. REST Assured uses Jackson internally to (de)serialize POJOs."**

---

## 11. REST Assured concepts cheat sheet

### 11.1 The three-part DSL

```java
RestAssured.given(spec)         // configure the request
           .body(payload)        // optional body
           .pathParam("id", 5)   // optional path param
           .queryParam("k","v")  // optional query param
           .when().get(path)     // fire it
           .then()               // optional fluent assertions
           .statusCode(200)
           .body("data.id", equalTo(5));
```

In this project we capture the `Response` and use TestNG's `Assert` for clearer failure messages.

### 11.2 `RequestSpecification` and `ResponseSpecification`

A **spec** is a reusable bundle of request settings (base URI, content type, headers, auth) or response expectations (status code, schema).

```java
RequestSpecification base = new RequestSpecBuilder()
        .setBaseUri("https://...")
        .setContentType(ContentType.JSON)
        .setAccept(ContentType.JSON)
        .log(LogDetail.URI)
        .build();
```

Build it once, reuse everywhere. Tests stay DRY.

### 11.3 Content type & headers

- `.contentType(ContentType.JSON)` — sets `Content-Type: application/json`.
- `.accept(ContentType.JSON)` — sets `Accept: application/json`.
- `.header("Authorization", "Bearer " + token)` — arbitrary header.
- `.headers(map)` — bulk add.

### 11.4 Body forms

```java
.body(pojo)                              // Jackson-serialized
.body(Map.of("k","v"))                   // map-based JSON
.body("{ \"raw\": \"json\" }")           // raw string
.multiPart("file", new File("img.jpg"))  // multipart upload
```

### 11.5 Path & query parameters

```java
.pathParam("id", 42)       // replaces {id} in the URL template
.queryParam("date", "2026-06-15")   // appends ?date=2026-06-15
.queryParams(map)
```

### 11.6 HTTP methods

```java
.when().get(path)
       .post(path)
       .put(path)
       .patch(path)
       .delete(path)
       .head(path)
       .options(path)
```

### 11.7 Response inspection

```java
Response r = ...;
r.statusCode();                     // int
r.asString();                       // full body as String
r.getBody().asString();             // same
r.getHeader("X-Trace-Id");
r.getCookie("JSESSIONID");
r.getTime();                        // round-trip ms
r.as(MyPojo.class);                 // Jackson-deserialize
r.jsonPath().getString("data.email");
r.jsonPath().getList("data.id");
r.jsonPath().getInt("data[0].id");
```

### 11.8 `JsonPath` syntax (Groovy-style GPath)

| Expression | Meaning |
|------------|---------|
| `data.email` | nested object access |
| `data[0]` | array index |
| `data.size()` | array length |
| `data.findAll { it.role == 'NGO' }` | filter |
| `data.collect { it.id }` | map to a list of ids |

### 11.9 Logging requests/responses

```java
.log().all()         // everything
.log().uri()         // just the URL
.log().ifValidationFails()  // only when an assertion fails
```

In `ApiSpecs.baseRequestSpec()` we log URI and method on every call — enough breadcrumbs to debug a failure without drowning logs.

### 11.10 Authentication patterns

```java
// Bearer token (what we use)
.header("Authorization", "Bearer " + jwt)

// Basic
.auth().preemptive().basic("user", "pwd")

// OAuth2
.auth().oauth2(token)
```

### 11.11 Schema validation (not used, but worth knowing)

Add `io.rest-assured:json-schema-validator` and:
```java
.then().body(matchesJsonSchemaInClasspath("schemas/login.json"));
```

We dropped this dependency to keep the suite minimal.

---

## 12. TestNG concepts cheat sheet

### 12.1 Core annotations

| Annotation | Lifecycle |
|------------|-----------|
| `@BeforeSuite` | once before any test class in the suite |
| `@BeforeTest` | once per `<test>` block in testng.xml |
| `@BeforeClass` | once per test class |
| `@BeforeMethod` | before every `@Test` method |
| `@Test` | the test |
| `@AfterMethod` / `@AfterClass` / `@AfterTest` / `@AfterSuite` | mirror images |

`alwaysRun = true` ensures the method runs even if previous methods failed (useful for cleanup hooks).

### 12.2 `@Test` parameters

```java
@Test(
    description    = "Human-readable",
    dependsOnMethods = "otherTest",
    enabled        = false,            // skip without deleting
    priority       = 1,                // lower = earlier
    timeOut        = 5000,             // ms
    expectedExceptions = ApiException.class,
    dataProvider   = "rows",
    groups         = { "smoke" }
)
```

### 12.3 `@DataProvider`

Returns `Object[][]` — TestNG runs the consuming `@Test` once per row.

### 12.4 Assertions

```java
Assert.assertEquals(actual, expected, message);
Assert.assertTrue(condition, message);
Assert.assertNotNull(obj);
Assert.fail("custom failure");
```

Use **soft assertions** (`SoftAssert`) when you want a test to keep running after a failed assertion and report multiple problems at once — not used in this suite, since each test asserts one thing.

### 12.5 `testng.xml` — the suite definition

```xml
<suite name="PETZ Automation Suite" parallel="false">
  <test name="TC101+ - API Tests (REST Assured)">
    <classes>
      <class name="com.cts.mfrp.petz.tests.api.TC101_AuthLoginTest"/>
      ...
    </classes>
  </test>
</suite>
```

Surefire is told about this file via `pom.xml`:
```xml
<plugin>
  <artifactId>maven-surefire-plugin</artifactId>
  <configuration>
    <suiteXmlFiles><suiteXmlFile>testng.xml</suiteXmlFile></suiteXmlFiles>
  </configuration>
</plugin>
```

### 12.6 Parallelism

```xml
<suite name="..." parallel="methods" thread-count="5">
```

Set to `false` here because tests share seed accounts; running them in parallel could create race conditions on the same hospital/NGO data. Worth flagging in an interview as a deliberate trade-off.

---

## 13. Interview Q & A

### 13.1 *What does this project do?*

It is an API test suite for the PETZ Animal Welfare Platform's REST API, deployed at `https://petz-production.up.railway.app/api`. The suite is written in Java with REST Assured for HTTP, TestNG for the runner, and lives in the same Maven module as an existing Selenium UI suite — so a single `mvn test` run covers both layers.

### 13.2 *Why REST Assured over plain HttpClient?*

Three reasons:
1. **Built-in JSON support** — `.body(pojo)` and `.jsonPath().getString(...)` remove ~20 lines of boilerplate per test.
2. **Fluent `given/when/then` DSL** — reads like the test scenario itself; non-programmers can follow it.
3. **Reusable specs** — `RequestSpecBuilder` / `ResponseSpecBuilder` keep tests DRY without writing helper classes.

### 13.3 *How is authentication handled?*

`TokenManager` calls `POST /auth/login` once per role (USER, NGO, HOSPITAL, ADMIN) at the start of the suite and caches the JWT in a `HashMap`. Every test calls one of `ApiSpecs.asUser()`, `asAdmin()`, `asNgo()`, `asHospital()` to get a `RequestSpec` already containing the right `Authorization: Bearer …` header. The cache means we don't burn one login per test.

### 13.4 *What's the difference between POJOs and `Map.of(...)` for request bodies?*

POJOs (e.g. `LoginRequest`) are typed and self-documenting — a misspelled field is a compile error. We use them for stable contracts like auth. For one-off ad-hoc bodies in a single test (creating a pet, a doctor, an animal), `Map.of(...)` is fine and avoids inflating the codebase with a new class per resource.

### 13.5 *How do you test a `POST` without leaving junk data behind?*

The `try/finally` lifecycle pattern. Create the resource, capture its id, do the test assertions inside `try`, and delete the resource in `finally`. Cleanup runs even when an assertion fails, so the next run starts from the same baseline.

### 13.6 *How do you handle the `JWT` expiring mid-suite?*

Currently the suite runs in ~110 s and JWTs are valid for 24 hours, so this never happens. If it did, you'd add a check in `TokenManager.tokenFor` — peek at the JWT's `exp` claim and re-login if it's within (say) 60 s of expiry. The current code is intentionally simple because the problem doesn't exist yet.

### 13.7 *What does `jsonPath().getString("data.email")` actually do?*

REST Assured uses Groovy's GPath under the hood. Given the response body
```json
{ "success": true, "data": { "email": "u@e.com" } }
```
`data.email` walks the JSON tree like a JavaScript object access and pulls out `"u@e.com"`. Brackets index arrays: `data[0].id` → first element's id.

### 13.8 *What's a `RequestSpecification`?*

A reusable bundle of request settings — base URI, content type, headers, auth, logging config. Built once with `RequestSpecBuilder`, used in many tests via `RestAssured.given(spec)`. Keeps duplication out of test bodies.

### 13.9 *Why is the suite mostly GET-heavy?*

Two reasons: (a) GETs are pure and easy to assert deterministically, (b) mutating endpoints on shared seed data are risky — toggling an admin user off, or overwriting the seeded hospital profile, would break later runs. Where we do mutate (pets, doctors, animals, appointments, notifications), every test cleans up after itself with `try/finally`.

### 13.10 *Why 403 instead of 401 on the unauthenticated endpoints?*

The backend uses Spring Security's default `AccessDeniedHandler`, which returns 403 for both unauthenticated and unauthorized requests. To get the standard 401 vs 403 distinction, the backend would need to wire an `AuthenticationEntryPoint`. The suite asserts the deployed behaviour (403) and documents the expectation in test comments.

### 13.11 *How would you parallelize this?*

Two changes:
1. In `testng.xml`, set `parallel="methods"` and `thread-count="N"`.
2. Stop sharing the seed accounts — each test thread should provision its own throwaway USER via `POST /auth/register` and use that account's token, so race conditions on shared records can't happen.

### 13.12 *What's the difference between `dependsOnMethods` and `priority`?*

- `priority` is a soft hint to TestNG about ordering when there are no other constraints.
- `dependsOnMethods` is a hard contract: if dependency fails, the dependent test is **skipped**, not failed. That's the right behaviour for "duplicate-email check needs the user to exist first."

### 13.13 *Where does the `Bearer` token live, and is it secure?*

In memory only — the static `HashMap<Role, String>` in `TokenManager`. Never written to disk or logged. The seed credentials (email + password) are hard-coded in `TokenManager` because they are well-known test data; for a real project they'd come from environment variables or a secrets manager.

### 13.14 *What's the relationship between TestNG's `Assert` and REST Assured's `body()` matchers?*

They overlap. REST Assured's `.then().body("data.id", equalTo(5))` does the same thing as `Assert.assertEquals(r.jsonPath().getInt("data.id"), 5)`. We chose `TestNG Assert` for clearer failure messages — REST Assured's matcher errors are sometimes terse Hamcrest output, while TestNG prints "expected X but found Y" verbatim.

### 13.15 *If I asked you to add coverage for the multipart photo upload endpoints, how would you?*

```java
RestAssured.given(ApiSpecs.asUser())
           .multiPart("file", new File("src/test/resources/sample.jpg"))
           .when().post("/users/me/photo");
```

Drop a small `sample.jpg` into `src/test/resources/`, then assert `statusCode == 200` and that `data.profilePhotoUrl` is non-null. The whole pattern is one extra REST Assured method (`.multiPart`); the rest of the framework needs no changes.

### 13.16 *Why is `petzbackend` in the Projects folder different from what's deployed?*

The repo at `C:\Users\…\PETZ` actually contains **two** Spring Boot projects: a legacy `petzbackend` (package `com.cts.mfrp.petzbackend`) and the current `petz-backend` (package `com.petz`). Railway deploys the second one — that's why the URLs in the original `PETZ_API_Documentation.md` didn't match reality. The current `docs/API.md` is the canonical contract for what's running.

---

## 14. Glossary

| Term | Meaning |
|------|---------|
| **POJO** | Plain Old Java Object — a class with fields, getters, setters and no framework dependencies. |
| **JWT** | JSON Web Token — a signed string carrying claims (user id, role, expiry). Server hands it out on login; client sends it back on every request. |
| **Page Object Model** | UI testing pattern: every screen has a class that hides selectors behind business methods. `AuthClient` is the API equivalent. |
| **Spec (REST Assured)** | Reusable request or response configuration object. |
| **GPath / JsonPath** | Groovy-style dot-and-bracket expression language for navigating JSON. |
| **DataProvider** | TestNG mechanism to feed a single test method multiple input rows. |
| **Idempotent test** | One that produces the same outcome when run repeatedly — our lifecycle tests achieve this via `try/finally` cleanup. |
| **Negative test** | One that verifies the system rejects bad input (4xx) or misuse — TC101.1-4, TC102.2-5, TC1101 are all negative. |
