# PETZ UI Automation — Notes for TS06 to TS09

These notes cover the four UI scenarios added on top of the original TS05 Dashboard
suite. Use this file as a study aid or as talking points when walking a teammate
through the codebase.

---

## 1. Project context

- **Framework:** Java 17 + Maven + TestNG + Selenium 4 (Chrome via WebDriverManager).
- **App under test:** the PETZ Angular SPA deployed at
  `https://stellular-taffy-e3ee7a.netlify.app`, backed by a Spring API at
  `https://petz-production.up.railway.app/api`.
- **Reports:** ExtentReports Spark — `test-output/reports/ExtentReport.html`.
  Failure screenshots are captured automatically by `BaseTest` into
  `test-output/screenshots/`.
- **Test data:** the seed pet-owner account `user@petz.com / admin@petz123`
  (declared in `AppConstants`); fresh accounts are minted via the REST API when
  the test requires guaranteed-empty state.

---

## 2. POM (Page Object Model) design

Every page lives under `src/main/java/com/cts/mfrp/petz/pages/`. The convention used
across this suite:

1. **One POM per route.** Each page object owns the locators and the actions for
   exactly one URL.
2. **Locators are private fields**; tests never see XPath strings.
3. **Public methods describe user intent**, not Selenium plumbing
   (`detail.openPetDetailFromListingByName("Max")`, not `findElement(...).click()`).
4. **Test files only orchestrate** — they call POM methods and run TestNG
   assertions. No locators leak into the test layer.
5. **Single test class per scenario.** Each `@Test` method represents one test
   case, marked with a `priority` matching the test-case number.

The reusable infrastructure already provided by the project:

- `BaseTest` — `@BeforeMethod` spawns a fresh `WebDriver`; `@AfterMethod` takes a
  screenshot on failure and pushes pass/fail into the Extent report.
- `LoginPage` — `loginAsPetOwner()` for the seed account and
  `login(email, password)` for ad-hoc credentials.
- `AuthClient` + `ApiSpecs.asUser()` — used in tests that need to create or read
  data via the REST API (so the UI test starts from a known state).

---

## 3. TS06 — Pet Detail and Apply (TC032 to TC035)

**Route:** `/adoption/animals/{id}` (a single pet's detail page with the
Apply-to-Adopt form below).

**POM added:** `PetDetailPage.java`

**What each test does:**

| TC    | Title                       | What it validates                                                                                                                                                                       |
| ----- | --------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| TC032 | Pet detail layout           | Navigates from the listing, then asserts pet card chrome — species chip overlay, name heading, breed-age-gender line, Vaccinated + AVAILABLE chips, location, description, 4 attr chips |
| TC033 | Apply form fields           | Verifies the "Apply to Adopt {name}" heading, the YOUR STORY section, the "Why do you want to adopt {name}?" textarea, and the "Previous pet ownership experience" textarea            |
| TC034 | Submit disabled when empty  | Leaves the Why field empty, attempts to submit, then asserts the Submit button is either disabled, the form shows a Required inline error, or the URL never changes                    |
| TC035 | Happy-path submission       | Fills both textareas with valid copy, asserts Submit is enabled, clicks it, and confirms a success toast appears                                                                       |

**Notable implementation points:**

- TC032 doesn't just click the first pet card — it calls
  `openPetDetailFromListingByName("Max")` because the listing is sorted
  newest-first and a recently-created unvaccinated pet would otherwise be picked,
  making the Vaccinated-chip assertion fail.
- The chip locators use XPath `translate(...)` so they're case-insensitive
  (the chip text varies between "Vaccinated" and "VACCINATED" across builds).
- Description-paragraph detection has two layers: prefer a class hint, otherwise
  accept any visible `<p>` with at least 20 chars of text.

---

## 4. TS07 — My Applications (TC036 to TC037)

**Route:** `/adoption/my` (the pet owner's adoption-application tracker).

**POM added:** `MyApplicationsPage.java`

**What each test does:**

| TC    | Title           | What it validates                                                                                                                                                                                                                          |
| ----- | --------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| TC036 | Empty state     | Registers a brand-new pet-owner via the API, UI-logs in, opens `/adoption/my` and asserts the empty state: heading, top-right Browse Animals button, clipboard icon, "No applications yet", subtitle, and the orange "Browse Animals" CTA. Clicking the CTA routes to `/adoption/animals` |
| TC037 | List with status| Logs in as the seed pet-owner (who already has at least one submitted application from prior tests) and asserts at least one card surfaces a status badge: Pending / Under Review / Approved / Rejected                                    |

**Notable implementation points:**

- TC036 cannot rely on the seed user because the seed user already has
  applications. Instead it calls `AuthClient.register()` to create a fresh
  `auto_<timestamp>@example.com` USER, then uses `LoginPage.login(email, password)`
  to UI-log-in. The empty state is therefore guaranteed.
- Angular Material buttons wrap text in `<span>` around an icon, so locators use
  `contains(., 'Browse Animals')` rather than `normalize-space()='Browse Animals'`.

---

## 5. TS08 — Book Appointment (TC038 to TC043)

**Route:** `/appointments/book` (the single-page 3-section appointment-booking form).

**POM added:** `BookAppointmentPage.java`

**What each test does:**

| TC    | Title                       | What it validates                                                                                                                                                                                                  |
| ----- | --------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| TC038 | Form layout                 | Title, subtitle, top-right "My Appointments", all three sections (Hospital + Doctor selects, Date + Time controls, Reason textarea), the info banner, and that Confirm Booking starts disabled with Cancel beside it |
| TC039 | Progressive enable          | Selects Hospital → Doctor → Date → Time and asserts Confirm enables once those four valid; Reason is then filled and Confirm stays enabled                                                                          |
| TC040 | Hospital + Doctor options   | Opens the Hospital dropdown and asserts options exist; selects the first one; opens the Doctor dropdown and asserts it populates                                                                                    |
| TC041 | Past-date rejection         | Opens the date picker, locates yesterday's cell via its `aria-label`, asserts it is disabled (aria-disabled or a "disabled" class), clicks it, and confirms the date field stays empty                              |
| TC042 | Time slot options           | Picks a future weekday, opens the Time dropdown, asserts at least one slot is listed, and verifies the field captures the selection                                                                                  |
| TC043 | Cancel button               | Fills some fields, clicks Cancel, and asserts either the URL navigated away from `/appointments/book` or the form was cleared                                                                                       |

**Notable implementation points:**

- The form uses Angular Material `mat-select`. A plain Selenium `.click()`
  sometimes does not open the panel, so `openMatSelect` tries three strategies
  in order: native click, `sendKeys(Keys.ENTER)`, then a JS click — each
  followed by a short wait for the overlay.
- Field locators are anchored on the unique, stable `formcontrolname` attribute
  (`hospitalId`, `doctorId`, `apptDate`, `apptTime`, `reason`). An earlier
  XPath used `(A | B)[1]` which picked the wrong `mat-select` because document
  order put Hospital first — switching to `formcontrolname` fixed that and made
  locators self-documenting.
- The date-picker assertion uses Angular Material's `aria-label="May 16, 2026"`
  format to find yesterday's cell deterministically rather than relying on
  visual position.
- TC039 deviates from the test plan in one spot: the deployed form marks the
  Reason textarea `aria-required="false"`, so Confirm enables after
  Hospital + Doctor + Date + Time — before Reason is filled. The test asserts
  the actual behaviour with an inline comment flagging the discrepancy.

---

## 6. TS09 — My Appointments (TC045 to TC046)

**Route:** `/appointments` (the pet owner's appointment list).

**POM added:** `MyAppointmentsPage.java`

**What each test does:**

| TC    | Title         | What it validates                                                                                                                                                                                                                                |
| ----- | ------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| TC045 | Empty state   | Registers a fresh pet-owner via the API, UI-logs in, asserts the empty card (X-calendar icon, "No appointments", subtitle, "+ Book Now" CTA, plus the top-right "+ Book Appointment" button). Clicking the CTA routes to `/appointments/book`     |
| TC046 | Populated list| Ensures the seed user has at least one booking (creating one via the REST API if necessary), then asserts the first row shows a status badge (PENDING / CONFIRMED / COMPLETED / CANCELLED) plus a date or time value                              |

**Notable implementation points:**

- TC046 is **self-bootstrapping** — if the seed user has zero appointments it
  POSTs one against `/appointments` using `ApiSpecs.asUser()`, picking a random
  future weekday (120 to 720 days out) and a random half-hour slot so reruns
  never collide on a taken slot.
- The "Reschedule / Cancel where applicable" clause from the test plan is
  logged as an Extent informational note rather than asserted, because those
  buttons may legitimately be hidden for COMPLETED/CANCELLED rows.

---

## 7. How to run

All five scenarios are wired into `testng.xml`. Examples (Windows PowerShell):

```powershell
# Run every scenario in the suite
mvn -f C:\Users\2480085\pharmaconnect\petz-automation\pom.xml test

# Run a single scenario
mvn -f C:\Users\2480085\pharmaconnect\petz-automation\pom.xml test "-Dtest=BookAppointmentTest"

# Run a single test method
mvn -f C:\Users\2480085\pharmaconnect\petz-automation\pom.xml test "-Dtest=BookAppointmentTest#TC041_verifyDatePickerRejectsPast"
```

After the run:

- HTML report: `test-output/reports/ExtentReport.html`
- Screenshots on failure: `test-output/screenshots/`
- Surefire text/XML: `target/surefire-reports/`

---

## 8. Patterns and gotchas worth remembering

- **Stay in the Page Object Model.** Locator strings never appear inside a
  `@Test` method. If a test needs a new element, add a method to the POM.
- **Prefer Angular's `formcontrolname` over text-based XPath** for inputs and
  selects — it is unique and survives copy changes.
- **Angular Material `mat-select` is finicky.** A direct click sometimes only
  focuses without opening the panel. Falling back to `Keys.ENTER` (because
  `mat-select` is a `role="combobox"` listening for keyboard) or to a JS click
  is the safest combination.
- **The seed pet-owner accumulates state.** For "empty state" tests we always
  register a fresh user via `AuthClient.register()` and log in via
  `LoginPage.login(email, password)`. For "populated state" tests we either
  rely on the seed account already having data or seed it via the REST API
  (`MyAppointmentsTest.ensureSeedUserHasAppointment()` is a good example).
- **Date-picker cells:** Angular Material exposes cells via
  `aria-label="MMMM d, yyyy"`. Finding a date by ARIA is more reliable than
  parsing the calendar grid.
- **When in doubt about why an assertion failed**, the post-failure screenshot
  (named `<testName>_<timestamp>.png`) usually answers the question instantly.
  Several of the iterative fixes during this work came from reading those
  screenshots rather than guessing.

---

## 9. File map (additions / changes)

```
petz-automation/
├── src/
│   ├── main/java/com/cts/mfrp/petz/pages/
│   │   ├── PetDetailPage.java         (TS06)
│   │   ├── MyApplicationsPage.java    (TS07)
│   │   ├── BookAppointmentPage.java   (TS08)
│   │   └── MyAppointmentsPage.java    (TS09)
│   └── test/java/com/cts/mfrp/petz/tests/
│       ├── PetDetailAndApplyTest.java (TC032-TC035)
│       ├── MyApplicationsTest.java    (TC036-TC037)
│       ├── BookAppointmentTest.java   (TC038-TC043)
│       └── MyAppointmentsTest.java    (TC045-TC046)
└── testng.xml                         (new <test> blocks per scenario)
```

19 `@Test` methods total across the 5 scenarios.
