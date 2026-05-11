# PETZ Platform — Complete API Documentation
> **Base URL:** `http://localhost:8081`  
> **Server Port:** `8081`  
> **Content-Type:** `application/json` (unless noted as multipart)  
> **Last Updated:** April 2026

---

## 📋 Table of Contents

1. [How to Use This Document (Postman Setup)](#postman-setup)
2. [Authentication Explained](#authentication-explained)
3. [Standard Response Format](#standard-response-format)
4. [Error Codes Reference](#error-codes-reference)
5. [Module 1 — Authentication](#module-1--authentication)
6. [Module 2 — User Profile](#module-2--user-profile)
7. [Module 3 — Notifications](#module-3--notifications)
8. [Module 4 — SOS Reports](#module-4--sos-reports)
9. [Module 5 — NGO (SOS Flow)](#module-5--ngo-sos-flow)
10. [Module 6 — NGO Profile & Dashboard](#module-6--ngo-profile--dashboard)
11. [Module 7 — Rescue Missions](#module-7--rescue-missions)
12. [Module 8 — On-Site Rescue Operations](#module-8--on-site-rescue-operations)
13. [Module 9 — Rescue History & Analytics](#module-9--rescue-history--analytics)
14. [Module 10 — Hospital Discovery](#module-10--hospital-discovery)
15. [Module 11 — Hospital Profile Management](#module-11--hospital-profile-management)
16. [Module 12 — Hospital Services & Doctors](#module-12--hospital-services--doctors)
17. [Module 13 — Slot Management](#module-13--slot-management)
18. [Module 14 — Appointment Booking](#module-14--appointment-booking)
19. [Module 15 — Appointment Lifecycle](#module-15--appointment-lifecycle)
20. [Module 16 — Appointment History](#module-16--appointment-history)
21. [Module 17 — Pet Management (Hospital)](#module-17--pet-management-hospital)
22. [Module 18 — Hospital Admin](#module-18--hospital-admin)
23. [Module 19 — Adoptable Pets (Public)](#module-19--adoptable-pets-public)
24. [Module 20 — NGO Pet Listings](#module-20--ngo-pet-listings)
25. [Module 21 — Adoption Applications (Adopter)](#module-21--adoption-applications-adopter)
26. [Module 22 — Adoption Review (NGO)](#module-22--adoption-review-ngo)
27. [Module 23 — Adoption Completion & Follow-ups](#module-23--adoption-completion--follow-ups)
28. [Module 24 — Adoption Disputes](#module-24--adoption-disputes)
29. [Module 25 — Adoption Admin](#module-25--adoption-admin)
30. [Coverage Verification Checklist](#coverage-verification-checklist)

---

## Postman Setup

### Step 1 — Import Base URL as a Variable
1. Open Postman → click **Environments** → **New Environment** → name it `PETZ Local`
2. Add a variable:
   - Variable: `base_url`
   - Initial Value: `http://localhost:8081`
   - Current Value: `http://localhost:8081`
3. Save, then select `PETZ Local` from the environment dropdown (top-right)
4. In every request URL, use `{{base_url}}/api/v1/...`

### Step 2 — Get a JWT Token (do this first!)
1. Call `POST {{base_url}}/api/v1/auth/login` with credentials
2. Copy the `accessToken` from the response
3. Add another environment variable:
   - Variable: `jwt_token`
   - Current Value: (paste the token here)
4. In each request that needs auth, go to **Authorization** tab → select **Bearer Token** → value: `{{jwt_token}}`

### Step 3 — Dev-Mode Shortcut (No JWT needed)
If you don't want to deal with JWT during local testing:
- Add header `X-User-Id: <your-user-uuid>` to any request
- The server will use this as your identity
- ⚠️ **Only works locally. Never use in production.**

### Step 4 — Headers to always set
| Header | Value | When |
|--------|-------|------|
| `Content-Type` | `application/json` | All JSON body requests |
| `Authorization` | `Bearer {{jwt_token}}` | All protected endpoints |
| `X-User-Id` | `<uuid>` | Dev-mode shortcut (replaces JWT) |

---

## Authentication Explained

The PETZ platform has **two authentication methods**:

### Method A — OTP (Quick, for SOS emergencies)
1. `POST /api/v1/auth/send-otp` → enter your phone number
2. `POST /api/v1/auth/verify-otp` → enter the 6-digit code you received → get JWT
3. This gives a **temporary session** (reporter role). You can convert it later to a full account.

### Method B — Password (Full account)
1. `POST /api/v1/auth/register` → create full account
2. `POST /api/v1/auth/login` → get JWT token

### Using the JWT Token
- All tokens start with `Bearer `
- Tokens expire after 24 hours (`expiresIn` field tells you the seconds)
- When a token expires, log in again to get a new one

---

## Standard Response Format

Every endpoint returns this wrapper:
```json
{
  "success": true,
  "message": "Human-readable message",
  "data": { ... }
}
```

For errors:
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Specific error reason",
  "path": "/api/v1/..."
}
```

---

## Error Codes Reference

| HTTP Code | Meaning | Common Cause |
|-----------|---------|--------------|
| 400 | Bad Request | Missing required field, invalid format |
| 401 | Unauthorized | No/invalid JWT token |
| 403 | Forbidden | Your role doesn't have permission |
| 404 | Not Found | Resource with that ID doesn't exist |
| 409 | Conflict | Duplicate action (e.g. already applied, already archived) |
| 423 | Locked | Account locked due to too many failed login attempts |
| 500 | Server Error | Unexpected bug — check application logs |

---

---

# Module 1 — Authentication

> **Base path:** `/api/v1/auth`  
> **Auth required:** ❌ None (all public)

---

### 1.1 Send OTP
**POST** `/api/v1/auth/send-otp`

Sends a 6-digit OTP to the given phone number via SMS. Use this when a user wants to report a SOS quickly without creating a full account.

**Request Body:**
```json
{
  "phone": "+919876543210"
}
```

| Field | Type | Required | Rules |
|-------|------|----------|-------|
| phone | String | ✅ | Must be valid international format: `+91XXXXXXXXXX` |

**Success Response (200):**
```json
{
  "message": "OTP sent successfully. Valid for 5 minutes.",
  "expiresInSeconds": 300,
  "authMethod": "OTP"
}
```

**Error Responses:**
- `400` — Invalid phone number format
- `429` — Too many OTP requests (max 5 per hour)

> ⚠️ **Caution:** OTP expires in 5 minutes. If the user doesn't call `/verify-otp` in time, they must request a new OTP.

---

### 1.2 Verify OTP
**POST** `/api/v1/auth/verify-otp`

Verifies the OTP and returns a JWT token. If the phone number is new, a temporary account is auto-created with role `REPORTER`.

**Request Body:**
```json
{
  "phone": "+919876543210",
  "otp": "123456"
}
```

| Field | Type | Required | Rules |
|-------|------|----------|-------|
| phone | String | ✅ | Same phone used in send-otp |
| otp | String | ✅ | Exactly 6 digits |

**Success Response (200):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "userId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "role": "REPORTER",
  "isTemporarySession": true,
  "message": "Emergency session created. You can track your rescue status."
}
```

**Error Responses:**
- `400` — Wrong OTP or OTP expired
- `400` — OTP format invalid (must be 6 digits)

> ⚠️ **Caution:** `isTemporarySession: true` means the account is temporary. The user should later call `/api/v1/auth/convert-session` to create a full account.

---

### 1.3 Initiate Missed Call
**POST** `/api/v1/auth/missed-call/initiate`

Alternative to OTP — gives the user a number to call. When they hang up (missed call), the system detects it and authenticates them. Used when SMS doesn't work.

**Request Body:**
```json
{
  "phone": "+919876543210"
}
```

**Success Response (200):**
```json
{
  "message": "Please give a missed call to verify your number.",
  "callbackNumber": "+911800XXXXXX",
  "timeoutSeconds": 120,
  "authMethod": "MISSED_CALL"
}
```

---

### 1.4 Verify Missed Call
**POST** `/api/v1/auth/missed-call/verify`

After the missed call is received, call this to get a JWT. Can also be called by the IVR webhook automatically.

**Request Body:**
```json
{
  "phone": "+919876543210",
  "verificationToken": "mc_token_abc123"
}
```

**Success Response (200):** Same as Verify OTP response.

---

### 1.5 Missed Call Webhook (Server-to-Server)
**POST** `/api/v1/auth/webhook/missed-call-verified`

Called by the IVR provider automatically when a missed call is detected. Not meant to be called by the frontend.

> ⚠️ **Caution:** In production, this should validate the `X-Webhook-Signature` header. Currently the signature check is a TODO.

---

### 1.6 Register Full Account
**POST** `/api/v1/auth/register`

Creates a full PETZ account with email, phone, password. An OTP is sent to verify the phone.

**Request Body:**
```json
{
  "fullName": "Rahul Sharma",
  "email": "rahul@example.com",
  "phone": "+919876543210",
  "password": "MyPass@123",
  "role": "ADOPTER"
}
```

| Field | Type | Required | Rules |
|-------|------|----------|-------|
| fullName | String | ✅ | Max 120 characters |
| email | String | ✅ | Valid email format, must be unique |
| phone | String | ✅ | Valid international format, must be unique |
| password | String | ✅ | 8–72 characters |
| role | String | ❌ | One of: `REPORTER`, `VOLUNTEER`, `NGO_REP`, `VET`, `ADOPTER`. Defaults to `ADOPTER`. **Cannot set ADMIN.** |

**Success Response (201):**
```json
{
  "success": true,
  "message": "Account created.",
  "data": {
    "userId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "role": "ADOPTER",
    "phoneVerified": false,
    "emailVerified": false,
    "message": "Account created. Please verify your phone via the OTP sent.",
    "otpExpiresInSeconds": 300
  }
}
```

**Error Responses:**
- `400` — Validation failure (missing fields, bad format)
- `409` — Email or phone already registered

> ⚠️ **Caution:** Account is created but `phoneVerified=false`. Call `POST /verify-otp` with the OTP sent to the phone to activate the account.

---

### 1.7 Login with Password
**POST** `/api/v1/auth/login`

Password-based login. The identifier can be either email or phone number.

**Request Body:**
```json
{
  "identifier": "rahul@example.com",
  "password": "MyPass@123"
}
```

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| identifier | String | ✅ | Email OR phone number |
| password | String | ✅ | The account password |

**Success Response (200):**
```json
{
  "success": true,
  "message": "Login successful.",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400000,
    "userId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "role": "ADOPTER",
    "message": "Login successful."
  }
}
```

**Error Responses:**
- `401` — Wrong password
- `423` — Account locked after 5 failed attempts (auto-unlocks after 15 minutes)
- `403` — Account disabled by admin

> ⚠️ **Caution (Lockout):** After **5 wrong password attempts**, the account is locked for **15 minutes**. You will see HTTP 423. Wait 15 minutes before trying again. There is no unlock endpoint — it's automatic.

---

### 1.8 Convert Temporary Session
**POST** `/api/v1/auth/convert-session`

Converts a temporary SOS reporter session into a full account with password + email. Call this after getting a JWT from the OTP flow.

**Query Params:**
| Param | Type | Required |
|-------|------|----------|
| userId | UUID | ✅ |

**Request Body:**
```json
{
  "fullName": "Rahul Sharma",
  "email": "rahul@example.com",
  "password": "MyPass@123"
}
```

**Success Response (200):**
```json
{
  "userId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "phone": "+919876543210",
  "fullName": "Rahul Sharma",
  "email": "rahul@example.com",
  "message": "Account upgraded successfully."
}
```

---

---

# Module 2 — User Profile

> **Base path:** `/api/v1/users/me`  
> **Auth required:** ✅ JWT or `X-User-Id` header

---

### 2.1 Get My Profile
**GET** `/api/v1/users/me`

Returns the full profile of the currently logged-in user.

**Headers:**
```
Authorization: Bearer {{jwt_token}}
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "Profile fetched.",
  "data": {
    "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "role": "ADOPTER",
    "fullName": "Rahul Sharma",
    "phone": "+919876543210",
    "email": "rahul@example.com",
    "ngoId": null,
    "profilePhotoUrl": null,
    "phoneVerified": true,
    "emailVerified": false,
    "active": true,
    "temporary": false,
    "lastLoginAt": "2026-04-22T10:00:00",
    "createdAt": "2026-04-01T09:00:00"
  }
}
```

---

### 2.2 Update My Profile
**PATCH** `/api/v1/users/me`

Updates name, email, or phone. All fields are optional — only send what you want to change.

**Request Body (all optional):**
```json
{
  "fullName": "Rahul Kumar Sharma",
  "email": "newemail@example.com",
  "phone": "+919999999999"
}
```

> ⚠️ **Caution:** If you change `email` or `phone`, the `verified` flag for that field is reset to `false`. You'll need to re-verify it via OTP.

**Success Response (200):** Returns updated `ProfileResponse` (same shape as Get Profile).

---

### 2.3 Change Password
**POST** `/api/v1/users/me/password`

Changes the account password. Requires the current password as proof.

**Request Body:**
```json
{
  "currentPassword": "OldPass@123",
  "newPassword": "NewPass@456"
}
```

| Field | Required | Rules |
|-------|----------|-------|
| currentPassword | ✅ | Must match existing password |
| newPassword | ✅ | 8–72 characters |

**Success Response (200):**
```json
{
  "success": true,
  "message": "Password updated.",
  "data": null
}
```

**Error Responses:**
- `400` — Current password is wrong
- `400` — New password too short

---

### 2.4 Upload Profile Photo
**POST** `/api/v1/users/me/photo`

Uploads a profile photo. Must use `multipart/form-data`.

**Headers:**
```
Content-Type: multipart/form-data
Authorization: Bearer {{jwt_token}}
```

**Form Data:**
| Key | Type | Required | Notes |
|-----|------|----------|-------|
| file | File | ✅ | JPEG or PNG, max 20MB |

**In Postman:** Body → form-data → Key: `file`, Type: `File`, choose your image.

**Success Response (200):** Returns updated `ProfileResponse` with `profilePhotoUrl` filled in.

---

---

# Module 3 — Notifications

> **Base path:** `/api/v1/users/me/notifications`  
> **Auth required:** ✅ JWT or `X-User-Id` header  
> **Note:** Notifications are auto-created by the server whenever something important happens (adoption approved, SOS dispatched, etc.). You don't create them manually.

---

### 3.1 List My Notifications
**GET** `/api/v1/users/me/notifications`

Returns a paginated list of your notifications, newest first.

**Query Params (all optional):**
| Param | Type | Default | Notes |
|-------|------|---------|-------|
| unreadOnly | Boolean | false | Set `true` to see only unread |
| page | Integer | 0 | Page number (0-based) |
| size | Integer | 20 | Items per page |

**Sample Request:**
```
GET {{base_url}}/api/v1/users/me/notifications?unreadOnly=true&page=0&size=10
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "Notifications fetched.",
  "data": {
    "content": [
      {
        "id": "uuid",
        "type": "ADOPTION_DECISION",
        "title": "Application Approved!",
        "body": "Your adoption application has been approved by the NGO.",
        "referenceId": "uuid-of-application",
        "referenceType": "APPLICATION",
        "isRead": false,
        "createdAt": "2026-04-22T09:00:00",
        "readAt": null
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 5,
    "totalPages": 1
  }
}
```

---

### 3.2 Get Unread Count (Badge Count)
**GET** `/api/v1/users/me/notifications/unread-count`

Returns the number of unread notifications. Use this to show a badge on the notification icon.

**Success Response (200):**
```json
{
  "success": true,
  "message": "Unread count fetched.",
  "data": {
    "count": 3
  }
}
```

---

### 3.3 Mark One Notification as Read
**PATCH** `/api/v1/users/me/notifications/{id}/read`

Marks a specific notification as read.

**Path Param:** `id` — UUID of the notification

**Success Response (200):** Returns the updated notification object with `isRead: true` and `readAt` filled.

**Error Responses:**
- `403` — Notification belongs to a different user
- `404` — Notification not found

---

### 3.4 Mark All Notifications as Read
**PATCH** `/api/v1/users/me/notifications/read-all`

Marks every unread notification as read in one call.

**Success Response (200):**
```json
{
  "success": true,
  "message": "5 notification(s) marked as read.",
  "data": null
}
```

---

### 3.5 Delete a Notification
**DELETE** `/api/v1/users/me/notifications/{id}`

Permanently deletes a notification from your inbox.

**Path Param:** `id` — UUID of the notification

**Success Response (200):**
```json
{
  "success": true,
  "message": "Notification dismissed.",
  "data": null
}
```

---

---

# Module 4 — SOS Reports

> **Base path:** `/api/v1/sos-reports`  
> **Auth required:** ❌ None (public endpoint — anyone can report)

---

### 4.1 Create SOS Report
**POST** `/api/v1/sos-reports`

Creates a new SOS report for an injured or distressed animal. This is the starting point of the entire rescue flow.

**Request Body:**
```json
{
  "reporterId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "latitude": 13.0827,
  "longitude": 80.2707,
  "urgencyLevel": "CRITICAL",
  "description": "Dog hit by a car, bleeding, near Anna Nagar bus stand"
}
```

| Field | Type | Required | Rules |
|-------|------|----------|-------|
| reporterId | UUID | ✅ | Your user ID (from OTP login or registration) |
| latitude | Decimal | ✅ | -90.0 to 90.0 |
| longitude | Decimal | ✅ | -180.0 to 180.0 |
| urgencyLevel | String | ✅ | `CRITICAL`, `MODERATE`, or `LOW` |
| description | String | ❌ | Max 500 characters |

**Success Response (201):**
```json
{
  "success": true,
  "message": "SOS Report created successfully",
  "data": {
    "id": "report-uuid",
    "reporterId": "user-uuid",
    "latitude": 13.0827,
    "longitude": 80.2707,
    "urgencyLevel": "CRITICAL",
    "currentStatus": "REPORTED",
    "description": "Dog hit by a car...",
    "reportedAt": "2026-04-22T10:30:00"
  }
}
```

> ⚠️ **Caution:** Rate limit is **3 SOS reports per hour** per account. You'll get HTTP 429 if exceeded.

---

### 4.2 Upload Media Evidence
**POST** `/api/v1/sos-reports/{reportId}/media`

Attaches photos/videos to an existing SOS report. Must use `multipart/form-data`.

**Path Param:** `reportId` — UUID of the SOS report

**Headers:**
```
Content-Type: multipart/form-data
```

**Form Data:**
| Key | Type | Notes |
|-----|------|-------|
| files | File (multiple) | JPEG, PNG, or MP4. Max 3 photos or 1 video |

**In Postman:** Body → form-data → Key: `files`, Type: `File`. Add multiple files by clicking "Add row" and using the same key `files`.

**Success Response (200):** Returns the updated SOS report with media URLs.

> ⚠️ **Caution:** Max file upload size is 20MB per file, 50MB total per request.

---

### 4.3 Get SOS Report by ID
**GET** `/api/v1/sos-reports/{reportId}`

Fetches full details of a single SOS report.

**Path Param:** `reportId` — UUID of the report

**Success Response (200):** Returns full `SosReportResponse`.

---

### 4.4 Get All SOS Reports
**GET** `/api/v1/sos-reports`

Returns all SOS reports in the system. Typically used by admins or NGOs.

**Success Response (200):** Returns list of `SosReportResponse`.

---

### 4.5 Get My SOS Reports
**GET** `/api/v1/sos-reports/my-reports?reporterId={uuid}`

Returns all reports submitted by a specific reporter.

**Query Params:**
| Param | Required | Notes |
|-------|----------|-------|
| reporterId | ✅ | UUID of the reporter |

**Sample Request:**
```
GET {{base_url}}/api/v1/sos-reports/my-reports?reporterId=3fa85f64-5717-4562-b3fc-2c963f66afa6
```

---

---

# Module 5 — NGO (SOS Flow)

> **Base path:** `/api/v1/ngo`  
> **Auth required:** ❌ (legacy endpoints from early Epic 1)

---

### 5.1 Assign Nearest NGO
**POST** `/api/v1/ngo/assign`

Auto-assigns the nearest available NGO to a rescue based on coordinates and severity.

**Query Params:**
| Param | Type | Required |
|-------|------|----------|
| sosLat | Double | ✅ |
| sosLon | Double | ✅ |
| severity | Integer | ✅ |

**Sample Request:**
```
POST {{base_url}}/api/v1/ngo/assign?sosLat=13.0827&sosLon=80.2707&severity=3
```

**Success Response (200):** Returns list of assigned `NgoResponseDTO`.

---

### 5.2 Accept Mission
**POST** `/api/v1/ngo/missions/{missionId}/accept`

NGO rep accepts a rescue mission assignment.

**Path Param:** `missionId` — UUID  
**Query Param:** `ngoUserId` — UUID of the NGO user

```
POST {{base_url}}/api/v1/ngo/missions/{missionId}/accept?ngoUserId={uuid}
```

**Success Response (200):** `"Mission accepted"`

---

### 5.3 Decline Mission
**POST** `/api/v1/ngo/missions/{missionId}/decline`

NGO rep declines a mission (another NGO will be tried).

**Query Param:** `ngoUserId` — UUID

**Success Response (200):** `"Mission declined"`

---

### 5.4 Get Navigation Details
**GET** `/api/v1/ngo/missions/{missionId}/navigation`

Returns navigation/GPS directions from the NGO to the rescue location.

**Success Response (200):** Returns `NavigationDTO` with route info.

---

---

# Module 6 — NGO Profile & Dashboard

> **Base path:** `/api/v1/ngo`  
> **Auth required:** ✅ Role: `NGO_REP` or `ADMIN` (or `X-User-Id` in dev mode)

---

### 6.1 Register a New NGO
**POST** `/api/v1/ngo/register`

Registers a new NGO on the platform. The logged-in user becomes the NGO owner. The NGO starts as **unverified** — an admin must approve it.

**Request Body:**
```json
{
  "name": "Paws & Care Foundation",
  "latitude": 13.0827,
  "longitude": 80.2707,
  "contactEmail": "paws@carefoundation.org",
  "contactPhone": "+919876543210",
  "address": "42 Anna Nagar, Chennai 600040",
  "registrationNumber": "TN/NGO/2024/00123",
  "description": "Rescuing and rehoming stray animals in Chennai since 2018."
}
```

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| name | String | ✅ | NGO name |
| latitude | Double | ✅ | NGO base location |
| longitude | Double | ✅ | NGO base location |
| contactEmail | String | ❌ | Contact email |
| contactPhone | String | ❌ | Contact phone |
| address | String | ❌ | Full address |
| registrationNumber | String | ❌ | Govt/Trust registration number |
| description | String | ❌ | About the NGO |

**Success Response (201):**
```json
{
  "success": true,
  "message": "NGO registered successfully. Pending admin verification.",
  "data": {
    "id": "ngo-uuid",
    "name": "Paws & Care Foundation",
    "latitude": 13.0827,
    "longitude": 80.2707,
    "active": true,
    "isVerified": false,
    "ownerUserId": "user-uuid",
    "contactEmail": "paws@carefoundation.org",
    "contactPhone": "+919876543210",
    "address": "42 Anna Nagar, Chennai 600040",
    "registrationNumber": "TN/NGO/2024/00123",
    "description": "Rescuing and rehoming..."
  }
}
```

**Error Responses:**
- `409` — User already has an NGO registered
- `400` — Missing required fields

> ⚠️ **Caution:** One user can only register **one NGO**. The NGO is not visible on the platform until an admin verifies it (`isVerified=true`).

---

### 6.2 Get My NGO Profile
**GET** `/api/v1/ngo/profile`

Returns the NGO profile associated with the logged-in user.

**Success Response (200):**
```json
{
  "success": true,
  "message": "NGO profile fetched.",
  "data": {
    "id": "ngo-uuid",
    "name": "Paws & Care Foundation",
    "isVerified": false,
    "active": true,
    "ownerUserId": "user-uuid",
    ...
  }
}
```

**Error Responses:**
- `404` — Logged-in user has no NGO registered

---

### 6.3 Update My NGO Profile
**PATCH** `/api/v1/ngo/profile`

Updates the NGO's profile. All fields are optional — only send what you want to change.

**Request Body (all optional):**
```json
{
  "name": "Paws & Care Foundation Chennai",
  "latitude": 13.09,
  "longitude": 80.28,
  "contactEmail": "new@email.org",
  "contactPhone": "+918888888888",
  "address": "Updated Address",
  "description": "Updated description."
}
```

**Success Response (200):** Returns updated `NgoProfileResponse`.

---

### 6.4 Get NGO Dashboard
**GET** `/api/v1/ngo/dashboard`

Returns aggregated statistics for the NGO — pet listings, applications, adoptions.

**Success Response (200):**
```json
{
  "success": true,
  "message": "NGO dashboard fetched.",
  "data": {
    "id": "ngo-uuid",
    "name": "Paws & Care Foundation",
    "isVerified": true,
    "totalListings": 15,
    "activeListings": 8,
    "archivedListings": 4,
    "adoptedPets": 3,
    "pendingApplications": 5,
    "approvedApplications": 3,
    "rejectedApplications": 7,
    "completedAdoptions": 3
  }
}
```

---

---

# Module 7 — Rescue Missions

> **Base path:** `/api/v1/rescue-missions`  
> **Auth required:** ❌ (open for rescue team use)

---

### 7.1 Create Rescue Mission
**POST** `/api/v1/rescue-missions?sosReportId={id}`

Creates a rescue mission linked to an SOS report. Called internally when an NGO accepts an assignment.

**Query Param:** `sosReportId` — UUID of the SOS report

**Success Response (201):**
```json
{
  "success": true,
  "message": "Rescue mission created",
  "data": {
    "id": "mission-uuid",
    "sosReportId": "report-uuid",
    "status": "DISPATCHED",
    "createdAt": "2026-04-22T10:30:00"
  }
}
```

---

### 7.2 List Rescue Missions
**GET** `/api/v1/rescue-missions?status={status}`

Lists all rescue missions. Optionally filter by status.

**Query Params:**
| Param | Required | Values |
|-------|----------|--------|
| status | ❌ | `REPORTED`, `DISPATCHED`, `ON_SITE`, `TRANSPORTING`, `COMPLETED` |

**Sample Request:**
```
GET {{base_url}}/api/v1/rescue-missions?status=DISPATCHED
```

---

### 7.3 Get Mission by ID
**GET** `/api/v1/rescue-missions/{missionId}`

Returns full details of a single rescue mission.

---

### 7.4 Get Mission by SOS Report
**GET** `/api/v1/rescue-missions/by-report/{sosReportId}`

Returns the mission linked to a specific SOS report. Reporters use this to track their case.

---

### 7.5 Update Mission Status
**PUT** `/api/v1/rescue-missions/{missionId}/status`

Updates the status of a rescue mission.

**Request Body:**
```json
{
  "status": "ON_SITE",
  "notes": "Arrived at the location. Animal is conscious."
}
```

| Field | Type | Required | Values |
|-------|------|----------|--------|
| status | String | ✅ | `DISPATCHED`, `ON_SITE`, `TRANSPORTING`, `COMPLETED` |
| notes | String | ❌ | Optional update notes |

---

### 7.6 Submit Mission Summary
**POST** `/api/v1/rescue-missions/{missionId}/summary`

NGO submits a final summary when the mission is complete.

**Request Body:**
```json
{
  "outcome": "Animal safely transported to City Animal Hospital",
  "animalsRescued": 1,
  "volunteerNotes": "Dog was dehydrated but stable. Handed over to vet team.",
  "completedAt": "2026-04-22T13:00:00"
}
```

**Success Response (201):** Returns `MissionSummaryResponse`.

---

### 7.7 Get Mission Summary
**GET** `/api/v1/rescue-missions/{missionId}/summary`

Retrieves the summary submitted for a mission.

---

### 7.8 Verify and Close Case (Admin)
**POST** `/api/v1/rescue-missions/{missionId}/verify`

Admin verifies and officially closes a rescue case.

**Request Body:**
```json
{
  "verified": true,
  "adminNotes": "Case verified. Animal now in hospital care.",
  "closedAt": "2026-04-22T14:00:00"
}
```

**Success Response (201):** Returns `CaseVerificationResponse`.

---

### 7.9 Get Case Verification
**GET** `/api/v1/rescue-missions/{missionId}/verification`

Retrieves the verification record for a mission.

---

---

# Module 8 — On-Site Rescue Operations

> **Base path:** `/api/v1/rescue/{sosReportId}`  
> **Auth required:** ✅ Role: `VOLUNTEER` or `NGO_REP` (or `VET`/`ADMIN` for handover)  
> **Note:** All endpoints in this module use the `sosReportId` as a path variable, NOT the mission ID.

---

### 8.1 Mark Arrival at Scene
**PATCH** `/api/v1/rescue/{sosReportId}/arrival`

Volunteer marks that they've arrived at the rescue location. Changes mission status to `ON_SITE`.

**Request Body:**
```json
{
  "arrivedAt": "2026-04-22T11:00:00",
  "volunteerId": "volunteer-user-uuid",
  "notes": "Arrived. Animal is under a vehicle."
}
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "Arrival marked. Status: ON_SITE.",
  "data": null
}
```

---

### 8.2 Submit On-Site Assessment
**POST** `/api/v1/rescue/{sosReportId}/assessment`

Records the volunteer's assessment of the animal's condition on-site.

**Request Body:**
```json
{
  "animalCondition": "CRITICAL",
  "injuries": "Broken front leg, bleeding from head",
  "animalType": "DOG",
  "estimatedAge": "YOUNG",
  "requiresImmediateHospital": true,
  "assessedBy": "volunteer-uuid",
  "assessedAt": "2026-04-22T11:10:00"
}
```

**Success Response (200):** Returns `OnSiteAssessmentResponse`.

---

### 8.3 Get Nearby Emergency Hospitals
**GET** `/api/v1/rescue/{sosReportId}/hospitals/nearby`

Returns hospitals near the rescue location that are marked as `emergencyReady=true`. Use this to decide where to take the animal.

**Success Response (200):**
```json
{
  "success": true,
  "message": "Nearby hospitals fetched.",
  "data": [
    {
      "hospitalId": "hosp-uuid",
      "name": "City Animal Hospital",
      "distanceKm": 1.8,
      "address": "12 Mount Road, Chennai",
      "phone": "+9144XXXXXXXX",
      "emergencyReady": true
    }
  ]
}
```

---

### 8.4 Send Incoming Rescue Alert to Hospital
**POST** `/api/v1/rescue/{sosReportId}/hospitals/{hospitalId}/alert`

Notifies the hospital that an animal is on its way. Hospital can prepare.

**Path Params:**
- `sosReportId` — UUID of the SOS report
- `hospitalId` — UUID of the target hospital

**Success Response (200):**
```json
{
  "success": true,
  "message": "Incoming rescue alert sent.",
  "data": null
}
```

---

### 8.5 Book Emergency Hospital Slot
**POST** `/api/v1/rescue/{sosReportId}/booking`

Books an emergency slot at a hospital for the incoming animal.

**Request Body:**
```json
{
  "hospitalId": "hosp-uuid",
  "serviceType": "EMERGENCY",
  "estimatedArrivalMinutes": 15
}
```

**Success Response (200):** Returns `EmergencyBookingResponse` with booking ID and slot time.

---

### 8.6 Record Hospital Handover
**POST** `/api/v1/rescue/{sosReportId}/handover`

Records that the animal has been handed over to the hospital. Requires `VET` or `ADMIN` role.

**Request Body:**
```json
{
  "hospitalId": "hosp-uuid",
  "handedOverTo": "Dr. Priya (Vet staff)",
  "handedOverAt": "2026-04-22T11:45:00",
  "notes": "Animal stable, vet team taking over."
}
```

**Success Response (200):** Returns `HandoverResponse`. Status changes to `HANDED_OVER`.

---

### 8.7 Confirm Release with Photo
**POST** `/api/v1/rescue/{sosReportId}/release`

Volunteer confirms the animal has been released/treated and provides a confirmation photo URL.

**Request Body:**
```json
{
  "releasePhotoUrl": "https://uploads.petz.com/release-photo.jpg",
  "confirmedAt": "2026-04-22T16:00:00",
  "notes": "Animal treated and released back to owner."
}
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "Release confirmed. Case pending closure.",
  "data": null
}
```

---

---

# Module 9 — Rescue History & Analytics

---

### 9.1 Get User's Rescue History
**GET** `/api/v1/users/{userId}/rescue-history`

Returns all SOS reports submitted by a specific user.

**Path Param:** `userId` — UUID of the user

**Success Response (200):**
```json
[
  {
    "sosId": "report-uuid",
    "reportedAt": "2026-04-10T09:00:00",
    "latitude": 13.0827,
    "longitude": 80.2707,
    "urgencyLevel": "CRITICAL",
    "status": "COMPLETED",
    "description": "Injured dog on highway",
    "outcome": "Animal rescued and handed to hospital"
  }
]
```

---

### 9.2 Admin KPI Dashboard
**GET** `/api/v1/admin/kpis`

Returns rescue performance KPIs for the admin dashboard.

**Query Params (all optional):**
| Param | Type | Notes |
|-------|------|-------|
| from | DateTime | ISO-8601: `2026-01-01T00:00:00` |
| to | DateTime | ISO-8601: `2026-04-30T23:59:59` |
| ngoId | UUID | Filter by NGO |
| city | String | Filter by city (future use) |

**Sample Request:**
```
GET {{base_url}}/api/v1/admin/kpis?from=2026-01-01T00:00:00&to=2026-04-30T23:59:59
```

**Success Response (200):**
```json
{
  "avgMinutesToAcceptance": 8.5,
  "completionRatePercent": 87.2,
  "hospitalHandoverRatePercent": 62.0,
  "volunteerResponseRatePercent": 91.0,
  "totalSos": 120,
  "completedSos": 104,
  "totalDispatched": 115,
  "acceptedDispatched": 105
}
```

---

### 9.3 Admin Live Rescue Map
**GET** `/api/v1/admin/rescues/live`

Returns all active (non-completed) SOS reports for an admin map view.

**Query Params (all optional):**
| Param | Type | Values |
|-------|------|--------|
| status | String | `REPORTED`, `DISPATCHED`, `ON_SITE`, `TRANSPORTING` |
| severity | String | `CRITICAL`, `MODERATE`, `LOW` |
| ngoId | UUID | Filter by specific NGO |

---

### 9.4 Admin Reassign Rescue
**PATCH** `/api/v1/admin/rescues/{sosReportId}/reassign`

Reassigns a stalled rescue to a different NGO or volunteer.

**Request Body:**
```json
{
  "newNgoId": "ngo-uuid",
  "newVolunteerId": "volunteer-uuid",
  "reason": "Previous volunteer unreachable for 30 minutes"
}
```

**Success Response (200):** Returns `ReassignResponse`.

---

---

# Module 10 — Hospital Discovery

> **Base path:** `/api/v1/hospitals`  
> **Auth required:** ❌ (public, no auth needed)

---

### 10.1 Browse All Hospitals
**GET** `/api/v1/hospitals`

Returns all verified hospitals. Optionally sort by distance by providing coordinates.

**Query Params (all optional):**
| Param | Type | Notes |
|-------|------|-------|
| lat | Double | User's latitude (for distance sort) |
| lon | Double | User's longitude (for distance sort) |

**Sample Requests:**
```
GET {{base_url}}/api/v1/hospitals
GET {{base_url}}/api/v1/hospitals?lat=13.0827&lon=80.2707
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "Hospitals fetched.",
  "data": [
    {
      "id": "hosp-uuid",
      "name": "City Animal Hospital",
      "city": "Chennai",
      "distanceKm": 2.3,
      "emergencyReady": true,
      "isVerified": true,
      "operatingHours": "9:00 AM - 9:00 PM",
      "services": ["Consultation", "Surgery", "Vaccination"]
    }
  ]
}
```

---

### 10.2 Search & Filter Hospitals
**GET** `/api/v1/hospitals/search`

Advanced search with multiple filters.

**Query Params (all optional):**
| Param | Type | Notes |
|-------|------|-------|
| name | String | Partial match on hospital name |
| city | String | City name |
| serviceType | String | e.g., `Surgery`, `Vaccination` |
| emergencyReady | Boolean | `true` to show only emergency-ready |
| openNow | Boolean | `true` to show only currently open |
| lat | Double | For distance sorting |
| lon | Double | For distance sorting |

**Sample Request:**
```
GET {{base_url}}/api/v1/hospitals/search?city=Chennai&emergencyReady=true&openNow=true
```

---

### 10.3 Get Hospital Profile
**GET** `/api/v1/hospitals/{hospitalId}`

Returns the full profile of a specific hospital including services, doctors, and operating hours.

**Path Param:** `hospitalId` — UUID

**Query Params (optional):**
| Param | Notes |
|-------|-------|
| lat | For showing distance |
| lon | For showing distance |

**Success Response (200):**
```json
{
  "success": true,
  "message": "Hospital profile fetched.",
  "data": {
    "id": "hosp-uuid",
    "name": "City Animal Hospital",
    "emergencyReady": true,
    "isVerified": true,
    "operatingHours": {...},
    "services": [...],
    "doctors": [...]
  }
}
```

---

---

# Module 11 — Hospital Profile Management

> **Base path:** `/api/v1/hospitals`  
> **Auth required:** ✅ Hospital admin or VET role

---

### 11.1 Register Hospital
**POST** `/api/v1/hospitals/register`

Registers a new hospital on the platform. Hospital starts as **PENDING** — admin must verify it.

**Request Body:**
```json
{
  "name": "City Animal Hospital",
  "ownerId": "vet-user-uuid",
  "locationId": "location-uuid",
  "emergencyReady": true,
  "operatingHours": "9:00 AM - 9:00 PM"
}
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "Hospital submitted for verification. Status: PENDING.",
  "data": { ... }
}
```

> ⚠️ **Caution:** Hospital will NOT appear in discovery search until admin verifies it.

---

### 11.2 Update Operating Hours
**PATCH** `/api/v1/hospitals/{hospitalId}/operating-hours`

Updates the hospital's day-wise operating hours.

**Request Body:**
```json
{
  "monday": "9:00-21:00",
  "tuesday": "9:00-21:00",
  "wednesday": "9:00-21:00",
  "thursday": "9:00-21:00",
  "friday": "9:00-21:00",
  "saturday": "10:00-18:00",
  "sunday": "CLOSED"
}
```

---

### 11.3 Update Emergency Status
**PATCH** `/api/v1/hospitals/{hospitalId}/emergency-status`

Toggles whether the hospital accepts emergency rescues.

**Request Body:**
```json
{
  "emergencyReady": true
}
```

> ⚠️ **Caution:** Setting `emergencyReady=false` immediately removes the hospital from emergency rescue search results.

---

---

# Module 12 — Hospital Services & Doctors

---

### 12.1 List Hospital Services
**GET** `/api/v1/hospitals/{hospitalId}/services`

Returns all services offered by a hospital.

**Success Response (200):**
```json
{
  "success": true,
  "message": "Services fetched.",
  "data": [
    {
      "id": "service-uuid",
      "serviceName": "General Consultation",
      "price": 500.00
    }
  ]
}
```

---

### 12.2 Add Service
**POST** `/api/v1/hospitals/{hospitalId}/services`

**Request Body:**
```json
{
  "serviceName": "Surgery",
  "price": 5000.00
}
```

---

### 12.3 Update Service
**PUT** `/api/v1/hospitals/{hospitalId}/services/{serviceId}`

**Request Body:**
```json
{
  "serviceName": "Dental Surgery",
  "price": 3500.00
}
```

---

### 12.4 Delete Service
**DELETE** `/api/v1/hospitals/{hospitalId}/services/{serviceId}`

Removes a service from the hospital catalog.

---

### 12.5 List Doctors
**GET** `/api/v1/hospitals/{hospitalId}/doctors`

**Query Params:**
| Param | Default | Notes |
|-------|---------|-------|
| activeOnly | false | Set `true` to exclude deactivated doctors |

---

### 12.6 Add Doctor
**POST** `/api/v1/hospitals/{hospitalId}/doctors`

**Request Body:**
```json
{
  "name": "Dr. Priya Menon",
  "specialization": "Small Animals",
  "phone": "+919876543210",
  "email": "priya@cityanimal.com"
}
```

---

### 12.7 Update Doctor
**PUT** `/api/v1/hospitals/{hospitalId}/doctors/{doctorId}`

Same fields as Add Doctor. All fields are optional.

---

### 12.8 Deactivate Doctor
**DELETE** `/api/v1/hospitals/{hospitalId}/doctors/{doctorId}`

Soft-deletes the doctor (marks `isActive=false`). The doctor won't be visible to patients but the record is preserved.

---

### 12.9 Link Doctor to Services
**PUT** `/api/v1/hospitals/{hospitalId}/doctors/{doctorId}/services`

Replaces the doctor's linked services.

**Request Body:**
```json
{
  "serviceIds": ["service-uuid-1", "service-uuid-2"]
}
```

---

---

# Module 13 — Slot Management

> **Base path:** `/api/v1/hospitals`

---

### 13.1 Create Appointment Slots
**POST** `/api/v1/hospitals/slots`

Creates one or multiple appointment slots for a hospital.

**Request Body:**
```json
{
  "hospitalId": "hosp-uuid",
  "doctorId": "doctor-uuid",
  "serviceId": "service-uuid",
  "slotDate": "2026-05-10",
  "startTime": "09:00:00",
  "durationMinutes": 30,
  "bookingType": "ROUTINE",
  "recurring": false
}
```

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| hospitalId | UUID | ✅ | Target hospital |
| doctorId | UUID | ❌ | Assign to specific doctor |
| serviceId | UUID | ❌ | Link to a service |
| slotDate | Date (YYYY-MM-DD) | ✅ | Date of the slot |
| startTime | Time (HH:mm:ss) | ✅ | Start time — must include seconds e.g. `"09:00:00"` |
| durationMinutes | Integer | ✅ | Slot length in minutes (e.g. 30) |
| bookingType | String | ❌ | `ROUTINE` or `EMERGENCY` |
| recurring | Boolean | ❌ | `true` to create recurring slots |
| recurringEnd | Date | ❌ | End date for recurring slots |
| recurringDays | String | ❌ | Days for recurring e.g. `"MON,WED,FRI"` |

> ⚠️ **Bug Fix Note:** `endTime` is NOT a field — use `durationMinutes` instead. `startTime` must be in `HH:mm:ss` format (with seconds).

**Success Response (200):** Returns list of created `SlotResponse` objects.

> ⚠️ **Caution:** If `recurrence=DAILY`, slots are created for 30 days. Use carefully to avoid flooding the calendar.

---

### 13.2 Get Slots for a Date
**GET** `/api/v1/hospitals/{hospitalId}/slots?date=YYYY-MM-DD`

Returns all slots for a specific hospital on a given date.

**Query Params:**
| Param | Type | Required |
|-------|------|----------|
| date | Date (YYYY-MM-DD) | ✅ |

**Sample Request:**
```
GET {{base_url}}/api/v1/hospitals/hosp-uuid/slots?date=2026-04-25
```

**Success Response (200):** Returns list of slots with status (`AVAILABLE`, `LOCKED`, `BOOKED`).

---

### 13.3 Add Blackout Date
**POST** `/api/v1/hospitals/blackout`

Blocks all slots on a specific date (e.g., hospital closed for a holiday).

**Request Body:**
```json
{
  "hospitalId": "hosp-uuid",
  "date": "2026-04-14",
  "reason": "Tamil New Year holiday"
}
```

> ⚠️ **Caution:** All existing `AVAILABLE` slots on that date are blocked. Existing confirmed bookings are **not** cancelled automatically — handle them separately.

---

### 13.4 Remove Blackout Date
**DELETE** `/api/v1/hospitals/{hospitalId}/blackout?date=YYYY-MM-DD`

Restores slots on a previously blocked date.

---

### 13.5 Get Blackout Dates
**GET** `/api/v1/hospitals/{hospitalId}/blackout`

Returns all blackout dates for a hospital.

---

### 13.6 Get Slot Utilization
**GET** `/api/v1/hospitals/{hospitalId}/utilization`

Returns slot utilization stats for a date range.

**Query Params:**
| Param | Required | Notes |
|-------|----------|-------|
| from | ✅ | YYYY-MM-DD |
| to | ✅ | YYYY-MM-DD |
| doctorId | ❌ | Filter by doctor |

**Success Response (200):** Returns list of `SlotUtilizationResponse` per day.

---

---

# Module 14 — Appointment Booking

> **Base path:** `/api/v1/appointments`

---

### 14.1 Lock a Slot (Step 1 of 2)
**POST** `/api/v1/appointments/lock`

Temporarily locks a slot for 2 minutes while the user fills the booking form. Prevents double-booking.

**Request Body:**
```json
{
  "slotId": "slot-uuid",
  "userId": "user-uuid"
}
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "Slot locked for 2 minutes.",
  "data": {
    "slotId": "slot-uuid",
    "lockedUntil": "2026-04-22T11:02:00",
    "lockToken": "lock-token-abc"
  }
}
```

> ⚠️ **Caution:** You have exactly **2 minutes** to call `/confirm` before the lock expires and the slot becomes available again.

---

### 14.2 Confirm Booking (Step 2 of 2)
**POST** `/api/v1/appointments/confirm`

Finalizes the appointment. Creates a confirmed booking record.

**Request Body:**
```json
{
  "slotId": "slot-uuid",
  "userId": "user-uuid",
  "petId": "pet-uuid",
  "hospitalId": "hosp-uuid",
  "serviceId": "service-uuid",
  "bookingType": "ROUTINE",
  "notes": "Annual vaccination checkup"
}
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "Appointment confirmed.",
  "data": {
    "appointmentId": "appt-uuid",
    "status": "CONFIRMED",
    "slotTime": "2026-04-25T09:00:00",
    "hospitalName": "City Animal Hospital",
    "petName": "Bruno",
    "serviceName": "Vaccination"
  }
}
```

---

### 14.3 Release Slot Lock (Cancel Checkout)
**DELETE** `/api/v1/appointments/lock/{slotId}?userId={uuid}`

Explicitly releases a locked slot before the 2-minute window expires. Call this when the user clicks "Cancel" on the booking page.

---

---

# Module 15 — Appointment Lifecycle

> **Base path:** `/api/v1/appointments`

---

### 15.1 Get Appointment by ID
**GET** `/api/v1/appointments/{appointmentId}`

Fetches full appointment details.

---

### 15.2 Cancel Appointment
**PATCH** `/api/v1/appointments/{appointmentId}/cancel`

**Request Body:**
```json
{
  "reason": "Pet recovered, no longer needed",
  "cancelledBy": "user-uuid"
}
```

**Success Response (200):** Status changes to `CANCELLED`.

> ⚠️ **Caution:** Once cancelled, the appointment **cannot** be reinstated. Create a new booking instead.

---

### 15.3 Reschedule Appointment
**PATCH** `/api/v1/appointments/{appointmentId}/reschedule`

**Request Body:**
```json
{
  "newSlotId": "new-slot-uuid",
  "reason": "Conflict with work schedule"
}
```

**Success Response (200):** Status stays `CONFIRMED` with updated slot time.

---

### 15.4 Mark as Attended
**PATCH** `/api/v1/appointments/{appointmentId}/attended`

Hospital staff marks the pet as arrived and attended. No request body needed.

**Success Response (200):** Status changes to `ATTENDED`.

---

### 15.5 Complete Appointment (Add Clinical Notes)
**PATCH** `/api/v1/appointments/{appointmentId}/complete`

Doctor completes the appointment and adds clinical notes. These are saved to the pet's medical record.

**Request Body:**
```json
{
  "clinicalNotes": "Dog vaccinated for Rabies + Distemper. Next dose in 1 year.",
  "diagnosis": "Healthy",
  "prescriptions": "None",
  "followUpDate": "2027-04-25"
}
```

**Success Response (200):** Status changes to `COMPLETED`. Notes saved to pet's medical history.

---

### 15.6 Mark as No-Show
**PATCH** `/api/v1/appointments/{appointmentId}/no-show`

Hospital marks appointment as no-show when the pet owner doesn't arrive. No request body.

**Success Response (200):** Status changes to `NO_SHOW`.

---

---

# Module 16 — Appointment History

---

### 16.1 Get User's Appointment History
**GET** `/api/v1/users/{userId}/appointments`

Returns appointment history for a user (pet owner view).

**Query Params (all optional):**
| Param | Type | Notes |
|-------|------|-------|
| petId | UUID | Filter by specific pet |
| hospitalId | UUID | Filter by hospital |
| from | Date (YYYY-MM-DD) | Start date |
| to | Date (YYYY-MM-DD) | End date |

**Sample Request:**
```
GET {{base_url}}/api/v1/users/{userId}/appointments?from=2026-01-01&to=2026-04-30
```

---

### 16.2 Hospital Dashboard
**GET** `/api/v1/hospitals/{hospitalId}/dashboard`

Returns the hospital's appointment dashboard — today's schedule, upcoming, stats.

**Query Params:**
| Param | Required | Notes |
|-------|----------|-------|
| doctorId | ❌ | Filter by doctor |

**Success Response (200):**
```json
{
  "success": true,
  "message": "Dashboard fetched",
  "data": {
    "todayAppointments": 12,
    "upcomingThisWeek": 45,
    "noShowRate": 8.3,
    "completedToday": 9,
    "appointments": [...]
  }
}
```

---

---

# Module 17 — Pet Management (Hospital)

> **Base path:** `/api/v1/users/{userId}/pets`

---

### 17.1 List User's Pets
**GET** `/api/v1/users/{userId}/pets`

Returns all pets registered under a user's account.

**Success Response (200):**
```json
{
  "success": true,
  "message": "Pets fetched.",
  "data": [
    {
      "id": "pet-uuid",
      "name": "Bruno",
      "species": "DOG",
      "breed": "Labrador",
      "gender": "MALE",
      "ageMonths": 24,
      "dateOfBirth": "2024-04-01"
    }
  ]
}
```

---

### 17.2 Register a Pet
**POST** `/api/v1/users/{userId}/pets`

Adds a new pet to the user's account.

**Request Body:**
```json
{
  "name": "Bruno",
  "species": "DOG",
  "breed": "Labrador",
  "gender": "MALE",
  "dateOfBirth": "2024-04-01",
  "medicalHistory": "Vaccinated in 2025. No known allergies.",
  "temperament": "Friendly, playful"
}
```

| Field | Required | Notes |
|-------|----------|-------|
| name | ✅ | Pet's name |
| species | ✅ | `DOG`, `CAT`, etc. |
| breed | ❌ | |
| gender | ❌ | `MALE`, `FEMALE` |
| dateOfBirth | ❌ | YYYY-MM-DD |

> ℹ️ **Info:** Pets adopted through the adoption module are automatically added here when the adoption is finalized.

---

---

# Module 18 — Hospital Admin

> **Base path:** `/api/v1/admin/hospitals`  
> **Auth required:** ✅ Role: `ADMIN`

---

### 18.1 Get Pending Hospital Registrations
**GET** `/api/v1/admin/hospitals/pending`

Returns all hospitals waiting for verification.

---

### 18.2 Verify (Approve/Reject) Hospital
**POST** `/api/v1/admin/hospitals/{hospitalId}/verify`

**Request Body:**
```json
{
  "action": "APPROVE",
  "reason": "All documents verified successfully."
}
```

| Field | Values |
|-------|--------|
| action | `APPROVE` or `REJECT` |
| reason | Optional note |

**Success Response (200):** Returns updated hospital record.

---

### 18.3 Get Hospital Metrics
**GET** `/api/v1/admin/hospitals/metrics`

Returns appointment metrics across hospitals.

**Query Params (all optional):**
| Param | Notes |
|-------|-------|
| hospitalId | Filter by specific hospital |
| city | Filter by city |
| from | YYYY-MM-DD |
| to | YYYY-MM-DD |

---

### 18.4 Disable Hospital
**POST** `/api/v1/admin/hospitals/{hospitalId}/disable`

Disables a hospital. All future appointment slots are blocked. Active confirmed appointments are cancelled.

**Request Body:**
```json
{
  "reason": "License expired. Hospital suspended pending renewal."
}
```

> ⚠️ **Caution:** This is a destructive action. All active appointments get cancelled. Owners are notified via in-app notification.

---

---

# Module 19 — Adoptable Pets (Public)

> **Base path:** `/api/v1/adoptable-pets`  
> **Auth required:** ❌ (fully public — no login needed)

---

### 19.1 Browse Adoptable Pets
**GET** `/api/v1/adoptable-pets`

Returns a paginated list of all pets available for adoption. Real-time — archived pets are excluded automatically.

**Query Params (all optional):**
| Param | Type | Default | Notes |
|-------|------|---------|-------|
| sort | String | newest | `newest`, `nearest`, `ready` |
| lat | Double | — | Required if sort=nearest |
| lon | Double | — | Required if sort=nearest |
| page | Integer | 0 | Page number (0-based) |
| size | Integer | 20 | Items per page, max 100 |

**Sample Requests:**
```
GET {{base_url}}/api/v1/adoptable-pets
GET {{base_url}}/api/v1/adoptable-pets?sort=nearest&lat=13.0827&lon=80.2707
GET {{base_url}}/api/v1/adoptable-pets?sort=ready&page=1&size=10
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "Adoptable pets fetched.",
  "data": {
    "content": [
      {
        "id": "pet-uuid",
        "name": "Luna",
        "species": "CAT",
        "breed": "Persian",
        "gender": "FEMALE",
        "ageMonths": 18,
        "locationCity": "Chennai",
        "isAdoptionReady": true,
        "status": "LISTED",
        "primaryImageUrl": "https://uploads.petz.com/luna.jpg",
        "ngoName": "Paws & Care Foundation"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 47,
    "totalPages": 3
  }
}
```

---

### 19.2 Search with Filters
**GET** `/api/v1/adoptable-pets/search`

Multi-filter search. All params are optional — combine as needed.

**Query Params:**
| Param | Type | Notes |
|-------|------|-------|
| species | String | e.g., `DOG`, `CAT`, `BIRD` |
| breed | String | Partial match |
| gender | String | `MALE`, `FEMALE` |
| minAgeMonths | Integer | Minimum age in months |
| maxAgeMonths | Integer | Maximum age in months |
| city | String | City name |
| vaccinated | Boolean | `true` for vaccinated only |
| specialNeeds | Boolean | `true` to include special-needs pets |
| adoptionReady | Boolean | `true` for ready-to-adopt only |
| sort | String | `newest`, `nearest`, `ready` |
| lat | Double | For distance sort |
| lon | Double | For distance sort |
| page | Integer | 0 |
| size | Integer | 20 |

**Sample Request:**
```
GET {{base_url}}/api/v1/adoptable-pets/search?species=DOG&city=Chennai&vaccinated=true&adoptionReady=true
```

---

### 19.3 Get Pet Full Profile
**GET** `/api/v1/adoptable-pets/{petId}`

Returns the complete profile of an adoptable pet including media gallery, NGO contact info, medical summary.

**Success Response (200):**
```json
{
  "success": true,
  "message": "Adoptable pet profile fetched.",
  "data": {
    "id": "pet-uuid",
    "name": "Luna",
    "species": "CAT",
    "breed": "Persian",
    "gender": "FEMALE",
    "ageMonths": 18,
    "color": "White",
    "description": "Luna is a gentle, affectionate cat...",
    "temperament": "Calm, loves cuddles",
    "locationCity": "Chennai",
    "latitude": 13.0827,
    "longitude": 80.2707,
    "medicalSummary": "Vaccinated, spayed, healthy",
    "vaccinationStatus": true,
    "specialNeeds": false,
    "specialNeedsText": null,
    "isAdoptionReady": true,
    "status": "LISTED",
    "media": [
      {
        "id": "media-uuid",
        "fileUrl": "https://uploads.petz.com/luna1.jpg",
        "mediaType": "IMAGE",
        "isPrimary": true,
        "displayOrder": 1
      }
    ],
    "ngoName": "Paws & Care Foundation",
    "ngoContact": "paws@care.org",
    "createdAt": "2026-03-01T09:00:00"
  }
}
```

---

---

# Module 20 — NGO Pet Listings

> **Base path:** `/api/v1/ngo/adoptable-pets`  
> **Auth required:** ✅ Role: `NGO_REP` or `ADMIN` (or `X-User-Id` in dev mode)  
> **Important:** The NGO is automatically resolved from the logged-in user. You don't need to pass ngoId — the server reads it from your account.

---

### 20.1 Create Pet Listing
**POST** `/api/v1/ngo/adoptable-pets`

Creates a new adoptable pet listing for the NGO.

**Request Body:**
```json
{
  "name": "Bruno",
  "species": "DOG",
  "breed": "Labrador Mix",
  "gender": "MALE",
  "ageMonths": 14,
  "sizeCategory": "LARGE",
  "color": "Golden",
  "description": "Bruno is a playful and energetic dog...",
  "temperament": "Energetic, friendly with kids",
  "locationCity": "Chennai",
  "latitude": 13.0827,
  "longitude": 80.2707,
  "medicalSummary": "Vaccinated for Rabies, Distemper. Neutered.",
  "vaccinationStatus": true,
  "specialNeeds": false,
  "isAdoptionReady": true
}
```

| Field | Required | Notes |
|-------|----------|-------|
| name | ✅ | Pet's name |
| species | ✅ | `DOG`, `CAT`, `BIRD`, `RABBIT`, etc. |
| breed | ✅ | Breed or mix |
| ageMonths | ✅ | Age in months |
| gender | ✅ | `MALE`, `FEMALE` |
| locationCity | ✅ | City |
| latitude | ✅ | Decimal coordinates |
| longitude | ✅ | Decimal coordinates |
| isAdoptionReady | ❌ | Default: `false`. Set `true` when ready for applications. |

**Success Response (200):** Returns full `Detail` with `status: "LISTED"`.

---

### 20.2 List My NGO's Pets
**GET** `/api/v1/ngo/adoptable-pets`

Returns all pet listings for the NGO (including archived).

**Query Params:**
| Param | Notes |
|-------|-------|
| status | Filter: `LISTED`, `ON_HOLD`, `ADOPTED`, `ARCHIVED` |
| sort | `newest` (default), `ready` |
| page | 0 |
| size | 20 |

---

### 20.3 Update Pet Listing
**PATCH** `/api/v1/ngo/adoptable-pets/{petId}`

Partially updates a pet listing. Only include fields you want to change.

**Request Body (all optional):**
```json
{
  "description": "Updated description after health check.",
  "isAdoptionReady": true,
  "medicalSummary": "Now fully vaccinated and neutered."
}
```

> ⚠️ **Caution:** Every update writes an audit log entry. Don't spam updates.

---

### 20.4 Archive Pet Listing
**POST** `/api/v1/ngo/adoptable-pets/{petId}/archive`

Removes the pet from the public catalog. All pending applications on this pet are flagged.

**Request Body:**
```json
{
  "reason": "Pet has been adopted privately."
}
```

> ⚠️ **Caution:** Archiving is reversible only by un-archiving via update (PATCH). But once archived, the pet won't appear in public search.

---

### 20.5 List Pet Media
**GET** `/api/v1/ngo/adoptable-pets/{petId}/media`

Returns all photos/videos for a pet.

---

### 20.6 Upload Pet Media
**POST** `/api/v1/ngo/adoptable-pets/{petId}/media`

Uploads a photo or video for a pet.

**Headers:**
```
Content-Type: multipart/form-data
```

**Form Data:**
| Key | Type | Notes |
|-----|------|-------|
| file | File | JPEG, PNG, or MP4. Max 20MB |

**In Postman:** Body → form-data → Key: `file`, Type: `File`.

---

### 20.7 Reorder Media
**PATCH** `/api/v1/ngo/adoptable-pets/{petId}/media/order`

Changes the display order of media items.

**Request Body:**
```json
{
  "order": ["media-uuid-3", "media-uuid-1", "media-uuid-2"]
}
```

> The array lists media IDs in the desired order (index 0 = display position 1).

---

### 20.8 Set Primary Image
**PATCH** `/api/v1/ngo/adoptable-pets/{petId}/media/{mediaId}/primary`

Sets a specific media item as the primary (cover) photo. Automatically unsets the previous primary.

---

### 20.9 Delete Media
**DELETE** `/api/v1/ngo/adoptable-pets/{petId}/media/{mediaId}`

Permanently deletes a media item.

---

---

# Module 21 — Adoption Applications (Adopter)

> **Base path:** `/api/v1/adoption-applications`  
> **Auth required:** ✅ (JWT or X-User-Id)  
> **Flow:** Start → Fill Personal → Fill Lifestyle → Fill Experience → Fill Consent → Upload KYC Docs → Submit

---

### 21.1 Start Application (Step 1)
**POST** `/api/v1/adoption-applications`

Creates a draft application for a specific pet. This is step 1 of the multi-step form.

**Request Body:**
```json
{
  "adoptablePetId": "pet-uuid"
}
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "Adoption application started.",
  "data": {
    "id": "application-uuid",
    "status": "DRAFT",
    "adoptablePetId": "pet-uuid",
    "adopterId": "user-uuid",
    "currentStep": "PERSONAL",
    "createdAt": "2026-04-22T10:00:00"
  }
}
```

**Error Responses:**
- `409` — You already have an active application for this pet

> ⚠️ **Caution:** Only one active application per pet per adopter. If you previously withdrew an application, you can start a new one.

---

### 21.2 Fill Personal Details (Step 2)
**PATCH** `/api/v1/adoption-applications/{id}/personal`

Auto-saves the personal section. Can be called multiple times.

**Request Body:**
```json
{
  "fullName": "Rahul Sharma",
  "phone": "+919876543210",
  "email": "rahul@example.com",
  "addressLine": "42 Anna Nagar",
  "city": "Chennai",
  "pincode": "600040"
}
```

---

### 21.3 Fill Lifestyle Details (Step 3)
**PATCH** `/api/v1/adoption-applications/{id}/lifestyle`

**Request Body:**
```json
{
  "housingType": "APARTMENT",
  "hasYard": false,
  "otherPetsCount": 1,
  "workScheduleHours": 8
}
```

---

### 21.4 Fill Pet Experience (Step 4)
**PATCH** `/api/v1/adoption-applications/{id}/experience`

**Request Body:**
```json
{
  "prevPetOwnership": true,
  "prevPetOwnershipText": "Owned a Labrador for 5 years until 2023.",
  "vetSupport": "Dr. Priya at City Animal Hospital, +91-44-XXXX"
}
```

---

### 21.5 Fill Consent (Step 5)
**PATCH** `/api/v1/adoption-applications/{id}/consent`

**Request Body:**
```json
{
  "consentHomeVisit": true,
  "consentFollowUp": true,
  "consentBackgroundCheck": true
}
```

> ⚠️ **Caution:** All three consent fields must be `true` to submit the application. If any is `false`, submission will fail with 400.

---

### 21.6 Upload KYC Document
**POST** `/api/v1/adoption-applications/{id}/documents`

Upload identity/address proof documents.

**Query Params:**
| Param | Required | Values |
|-------|----------|--------|
| docType | ✅ | `ID_PROOF`, `ADDRESS_PROOF` |

**Form Data:**
| Key | Type | Notes |
|-----|------|-------|
| file | File | PDF, JPEG, or PNG. Max 5MB |

**In Postman:**
1. Body → form-data
2. Key: `docType`, Value: `ID_PROOF` (Type: Text)
3. Key: `file`, Value: (select file) (Type: File)

**Success Response (200):**
```json
{
  "data": {
    "id": "doc-uuid",
    "docType": "ID_PROOF",
    "fileName": "aadhar.pdf",
    "verificationStatus": "PENDING",
    "uploadedAt": "2026-04-22T10:05:00"
  }
}
```

> ⚠️ **Caution:** KYC file size cap is **5MB**. Larger files will be rejected with 400.

---

### 21.7 List My KYC Documents
**GET** `/api/v1/adoption-applications/{id}/documents`

Returns all uploaded documents for an application with their verification status.

---

### 21.8 Submit Application (Final Step)
**POST** `/api/v1/adoption-applications/{id}/submit`

Finalizes and submits the application to the NGO for review. Status changes from `DRAFT` to `SUBMITTED`.

**No request body needed.**

**Pre-conditions before calling this:**
- Personal section must be filled
- Lifestyle section must be filled
- Experience section must be filled
- All consent fields must be `true`

**Success Response (200):** Status changes to `SUBMITTED`.

**Error Responses:**
- `400` — Required sections not filled
- `400` — Consent not given
- `409` — Application already submitted

---

### 21.9 Withdraw Application
**POST** `/api/v1/adoption-applications/{id}/withdraw`

Withdraws the application. Allowed for: `DRAFT`, `SUBMITTED`, `UNDER_REVIEW`, `CLARIFICATION_REQUESTED`.

**Request Body (optional):**
```json
{
  "reason": "Changed my mind, not ready for a pet yet."
}
```

**Error Responses:**
- `409` — Application already APPROVED, REJECTED, or WITHDRAWN

---

### 21.10 Get Application Detail
**GET** `/api/v1/adoption-applications/{id}`

Returns full application detail including audit history and NGO comments.

---

### 21.11 List My Applications
**GET** `/api/v1/adoption-applications/mine`

Returns all your adoption applications.

---

---

# Module 22 — Adoption Review (NGO)

> **Base path:** `/api/v1/ngo/adoption-applications`  
> **Auth required:** ✅ Role: `NGO_REP` or `ADMIN`  
> **Note:** Only applications for pets belonging to the logged-in user's NGO are returned.

---

### 22.1 List Applications (NGO Queue)
**GET** `/api/v1/ngo/adoption-applications`

Returns adoption applications for the NGO's pets.

**Query Params (all optional):**
| Param | Type | Notes |
|-------|------|-------|
| status | String | `DRAFT`, `SUBMITTED`, `UNDER_REVIEW`, `APPROVED`, `REJECTED`, `WITHDRAWN` |
| petId | UUID | Filter by specific pet |
| from | Date (YYYY-MM-DD) | Application date from |
| to | Date (YYYY-MM-DD) | Application date to |
| unreviewed | Boolean | `true` to see only `SUBMITTED` (new applications) |
| sort | String | `date` (default) or `priority` |
| page | Integer | 0 |
| size | Integer | 20 |

**Sample Request:**
```
GET {{base_url}}/api/v1/ngo/adoption-applications?unreviewed=true
```

---

### 22.2 Get Application Detail (NGO View)
**GET** `/api/v1/ngo/adoption-applications/{id}`

Returns full detail including KYC document URLs, all sections, audit trail.

---

### 22.3 Start Review
**POST** `/api/v1/ngo/adoption-applications/{id}/review-start`

Marks the application as under review. Status changes to `UNDER_REVIEW`. Idempotent.

**No request body needed.**

---

### 22.4 Approve Application
**POST** `/api/v1/ngo/adoption-applications/{id}/approve`

Approves the adoption application. Sends notification to the adopter.

**Request Body:**
```json
{
  "confirm": true
}
```

> ⚠️ **Caution:** `confirm: true` is mandatory as a safety check. Sending `confirm: false` returns 400. This prevents accidental approvals.

**Success Response (200):** Status changes to `APPROVED`.

---

### 22.5 Reject Application
**POST** `/api/v1/ngo/adoption-applications/{id}/reject`

Rejects the application with a mandatory reason.

**Request Body:**
```json
{
  "reason": "Applicant's housing type is not suitable for a large dog."
}
```

| Field | Required |
|-------|----------|
| reason | ✅ — Must provide a reason |

**Success Response (200):** Status changes to `REJECTED`. Adopter notified with reason.

---

### 22.6 Request Clarification
**POST** `/api/v1/ngo/adoption-applications/{id}/clarify`

Sends questions back to the adopter. Status changes to `CLARIFICATION_REQUESTED`.

**Request Body:**
```json
{
  "questions": [
    "Can you provide more details about your previous pet?",
    "Is your apartment building pet-friendly?"
  ]
}
```

> ℹ️ When the adopter updates any section after this, status auto-changes back to `UNDER_REVIEW`.

---

### 22.7 Verify KYC Document
**POST** `/api/v1/ngo/adoption-applications/{id}/documents/{docId}/verify`

Marks a KYC document as verified or rejected.

**Request Body:**
```json
{
  "status": "VERIFIED",
  "reason": null
}
```

Or to reject:
```json
{
  "status": "REJECTED",
  "reason": "Document is blurry and unreadable."
}
```

| status | Meaning |
|--------|---------|
| `VERIFIED` | Document accepted |
| `REJECTED` | Document failed review (reason required) |

---

---

# Module 23 — Adoption Completion & Follow-ups

---

## NGO-Facing Endpoints

### 23.1 Schedule Handover
**POST** `/api/v1/ngo/adoptions/schedule`

Schedules the handover date and location. Application must be in `APPROVED` status.

**Request Body:**
```json
{
  "applicationId": "application-uuid",
  "handoverDate": "2026-05-10",
  "handoverLocation": "NGO Office, 42 Anna Nagar, Chennai"
}
```

**Success Response (200):**
```json
{
  "data": {
    "id": "adoption-uuid",
    "status": "HANDOVER_SCHEDULED",
    "handoverDate": "2026-05-10",
    "handoverLocation": "NGO Office, 42 Anna Nagar, Chennai"
  }
}
```

**Error Responses:**
- `400` — Application is not in APPROVED status

---

### 23.2 Confirm Handover (Finalize Adoption)
**POST** `/api/v1/ngo/adoptions/{id}/confirm-handover`

Finalizes the adoption. This triggers:
1. Adoption status → `COMPLETED`
2. Pet status → `ADOPTED` (removed from public catalog)
3. Pet is automatically added to the adopter's hospital module account
4. 3 follow-up records created (Day 7, Day 30, Day 90)
5. Adopter notified

**No request body needed.**

> ⚠️ **Caution:** This is **irreversible**. Once COMPLETED, the adoption record cannot be modified. The pet is permanently linked to the adopter in the hospital module.

---

### 23.3 List NGO's Adoptions
**GET** `/api/v1/ngo/adoptions`

Returns all adoption records for the NGO.

---

### 23.4 Record Follow-Up
**PATCH** `/api/v1/ngo/adoptions/{id}/follow-ups/{followUpId}`

Records the result of a welfare follow-up (Day 7, 30, or 90 check-in).

**Request Body:**
```json
{
  "status": "COMPLETED",
  "notes": "Pet is healthy and well-adjusted. Owner is happy.",
  "concernFlag": false
}
```

| Field | Values |
|-------|--------|
| status | `COMPLETED`, `FLAGGED` |
| concernFlag | `true` if welfare concern detected |

> ⚠️ **Caution:** Set `concernFlag: true` and `status: "FLAGGED"` if the animal shows signs of neglect or abuse. This flags the adoption for admin review.

---

## Adopter-Facing Endpoints

### 23.5 List My Adoptions
**GET** `/api/v1/adoptions/mine`

Returns all completed adoption records for the logged-in adopter.

---

### 23.6 Get Adoption by ID
**GET** `/api/v1/adoptions/{id}`

Returns full adoption details.

---

### 23.7 List Follow-ups for Adoption
**GET** `/api/v1/adoptions/{id}/follow-ups`

Returns the 3 follow-up records (Day 7, 30, 90) and their current status.

**Success Response (200):**
```json
{
  "data": [
    {
      "id": "followup-uuid",
      "followUpType": "DAY_7",
      "dueDate": "2026-05-17",
      "status": "COMPLETED",
      "notes": "Doing great!",
      "concernFlag": false,
      "completedAt": "2026-05-16T10:00:00"
    },
    {
      "id": "followup-uuid-2",
      "followUpType": "DAY_30",
      "dueDate": "2026-06-09",
      "status": "SCHEDULED",
      "notes": null,
      "concernFlag": false,
      "completedAt": null
    }
  ]
}
```

---

---

# Module 24 — Adoption Disputes

---

### 24.1 Raise a Dispute
**POST** `/api/v1/adoptions/disputes`

Any authenticated user (adopter or NGO) can raise a dispute about an adoption.

**Request Body:**
```json
{
  "adoptionId": "adoption-uuid",
  "description": "The adopted animal was returned after 3 days. Owner claims they were given false information about the pet's health."
}
```

**Success Response (200):**
```json
{
  "data": {
    "id": "dispute-uuid",
    "adoptionId": "adoption-uuid",
    "status": "OPEN",
    "raisedByUserId": "user-uuid",
    "createdAt": "2026-04-22T12:00:00"
  }
}
```

---

### 24.2 List Disputes (Admin)
**GET** `/api/v1/admin/adoptions/disputes`

**Auth:** ADMIN only

**Query Params:**
| Param | Notes |
|-------|-------|
| status | `OPEN` or `RESOLVED`. Leave blank for all. |
| page | 0 |
| size | 20 |

---

### 24.3 Get Dispute Detail (Admin)
**GET** `/api/v1/admin/adoptions/disputes/{id}`

Returns full dispute details with audit history.

---

### 24.4 Resolve Dispute (Admin)
**POST** `/api/v1/admin/adoptions/disputes/{id}/resolve`

**Auth:** ADMIN only

**Request Body:**
```json
{
  "action": "WARN",
  "resolution": "NGO issued a formal warning. Adoption records updated."
}
```

| action | Effect |
|--------|--------|
| `OVERRIDE` | Admin overrides — adoption record updated |
| `WARN` | Formal warning to the party at fault |
| `SUSPEND` | Suspends NGO or user account (`active=false`) |

> ⚠️ **Caution:** `SUSPEND` immediately disables the NGO or user account. They will be unable to log in. Use only for confirmed violations.

---

---

# Module 25 — Adoption Admin

> **Base path:** `/api/v1/admin/adoptions`  
> **Auth required:** ✅ Role: `ADMIN` (or dev fallback)

---

### 25.1 Get Adoption Metrics (KPI Dashboard)
**GET** `/api/v1/admin/adoptions/metrics`

Returns platform-wide adoption KPIs.

**Query Params (all optional):**
| Param | Type | Notes |
|-------|------|-------|
| from | Date (YYYY-MM-DD) | Start date filter |
| to | Date (YYYY-MM-DD) | End date filter |
| ngoId | UUID | Filter by NGO |
| city | String | Filter by city |

**Success Response (200):**
```json
{
  "data": {
    "totalApplications": 250,
    "approvedCount": 80,
    "rejectedCount": 120,
    "withdrawnCount": 30,
    "completedAdoptions": 65,
    "conversionRatePercent": 32.0,
    "completionRatePercent": 81.25,
    "avgReviewTimeHours": 47.5,
    "followUpCompliancePercent": 88.0,
    "totalFollowUps": 195,
    "completedFollowUps": 172,
    "flaggedFollowUps": 8
  }
}
```

---

### 25.2 List NGOs (for Admin Verification)
**GET** `/api/v1/admin/adoptions/ngos`

Returns NGOs filtered by verification status.

**Query Params:**
| Param | Notes |
|-------|-------|
| verified | `true` = verified NGOs, `false` = pending NGOs, omit = all |

---

### 25.3 Verify / Reject / Suspend NGO
**POST** `/api/v1/admin/adoptions/ngos/{ngoId}/verify`

Admin verifies, rejects, or suspends an NGO.

**Path Param:** `ngoId` — UUID of the NGO

**Request Body:**
```json
{
  "action": "APPROVE",
  "reason": "All registration documents verified.",
  "ownerUserId": "user-uuid-optional"
}
```

| action | Effect |
|--------|--------|
| `APPROVE` | Sets `isVerified=true`. NGO is now active on platform. |
| `REJECT` | NGO registration rejected. |
| `SUSPEND` | NGO suspended (sets `active=false`). |

**Success Response (200):** Returns updated NGO record.

---

### 25.4 Get Unified Audit Log
**GET** `/api/v1/admin/adoptions/audit-logs`

Returns a paginated, filterable audit log of all adoption-related actions across the platform.

**Query Params (all optional):**
| Param | Type | Notes |
|-------|------|-------|
| targetType | String | `PET_LISTING`, `APPLICATION`, `ADOPTION`, `NGO`, `DISPUTE` |
| targetId | UUID | UUID of the specific entity |
| actorId | UUID | UUID of the user who performed the action |
| from | Date (YYYY-MM-DD) | Start date |
| to | Date (YYYY-MM-DD) | End date |
| page | Integer | 0 |
| size | Integer | 50 (default) |

**Sample Requests:**
```
GET {{base_url}}/api/v1/admin/adoptions/audit-logs?targetType=APPLICATION&from=2026-04-01&to=2026-04-30
GET {{base_url}}/api/v1/admin/adoptions/audit-logs?targetType=NGO&targetId=ngo-uuid
```

**Success Response (200):**
```json
{
  "data": {
    "content": [
      {
        "id": "log-uuid",
        "targetType": "APPLICATION",
        "targetId": "application-uuid",
        "actorId": "ngo-user-uuid",
        "action": "APPROVED",
        "reason": "All conditions met.",
        "metadata": null,
        "performedAt": "2026-04-22T11:00:00"
      }
    ],
    "page": 0,
    "size": 50,
    "totalElements": 342,
    "totalPages": 7
  }
}
```

---

---

# Live Test Results Summary

> All tests run against `http://localhost:8081` on **22 April 2026**.

## End-to-End Flow Tested ✅

| Step | Action | Result |
|------|--------|--------|
| 1 | Register new ADOPTER account | ✅ 201 — userId returned |
| 2 | Login with password | ✅ 200 — JWT token returned |
| 3 | GET profile with JWT | ✅ 200 — full profile |
| 4 | PATCH profile (update name) | ✅ 200 — name updated |
| 5 | POST change password | ✅ 200 — password changed |
| 6 | GET notifications (empty inbox) | ✅ 200 — empty list |
| 7 | GET unread count | ✅ 200 — count: 0 |
| 8 | POST SOS report | ✅ 201 — report created |
| 9 | GET SOS by ID | ✅ 200 — report fetched |
| 10 | GET my SOS reports | ✅ 200 — list returned |
| 11 | GET hospitals (browse) | ✅ 200 — 3 hospitals returned |
| 12 | GET hospitals/search | ✅ 200 — filtered correctly |
| 13 | GET hospital profile | ✅ 200 — with doctors |
| 14 | GET hospital services | ✅ 200 |
| 15 | GET hospital doctors | ✅ 200 |
| 16 | Register NGO_REP user | ✅ 201 |
| 17 | Login NGO_REP | ✅ 200 — JWT returned |
| 18 | POST /api/v1/ngo/register | ✅ 201 — NGO created (unverified) |
| 19 | GET /api/v1/ngo/profile | ✅ 200 — profile fetched |
| 20 | GET /api/v1/ngo/dashboard | ✅ 200 — all counts correct |
| 21 | POST NGO pet listing | ✅ 200 — pet created (LISTED) |
| 22 | GET public browse (sees new pet) | ✅ 200 — pet appears |
| 23 | GET public pet detail | ✅ 200 |
| 24 | POST start adoption application | ✅ 200 — DRAFT created |
| 25 | PATCH personal/lifestyle/experience/consent | ✅ 200 — all 4 sections saved |
| 26 | POST submit application | ✅ 200 — status → SUBMITTED |
| 27 | GET NGO unreviewed queue | ✅ 200 — application visible |
| 28 | POST review-start | ✅ 200 — status → UNDER_REVIEW |
| 29 | POST approve (confirm:true) | ✅ 200 — status → APPROVED |
| 30 | POST schedule handover | ✅ 200 — HANDOVER_SCHEDULED |
| 31 | POST confirm-handover | ✅ 200 — COMPLETED + 3 follow-ups auto-created |
| 32 | Hospital bridge — pet in /api/v1/users/{id}/pets | ✅ 200 — E2E Test Dog appears |
| 33 | Pet status in catalog → ADOPTED | ✅ 200 — hidden from public browse |
| 34 | GET adopter's adoptions | ✅ 200 |
| 35 | GET follow-ups (DAY_7, 30, 90) | ✅ 200 — all 3 present |
| 36 | PATCH record DAY_7 follow-up | ✅ 200 — COMPLETED |
| 37 | POST create hospital slot | ✅ 200 (with corrected fields) |
| 38 | POST lock slot | ✅ 200 — locked for 120s |
| 39 | POST confirm booking | ✅ 200 — CONFIRMED |
| 40 | GET appointment by ID | ✅ 200 |
| 41 | PATCH mark attended | ✅ 200 — ATTENDED |
| 42 | PATCH complete appointment | ✅ 200 — COMPLETED, notes saved |
| 43 | GET user appointment history | ✅ 200 |
| 44 | GET hospital dashboard | ✅ 200 |
| 45 | GET admin adoption metrics | ✅ 200 — counts correct |
| 46 | GET admin audit logs | ✅ 200 — action history visible |
| 47 | GET admin NGO list | ✅ 200 |
| 48 | POST admin verify NGO | ✅ 200 — verified:true |
| 49 | POST raise dispute | ✅ 200 — OPEN |
| 50 | GET admin disputes list | ✅ 200 |
| 51 | POST resolve dispute | ✅ 200 — RESOLVED |
| 52 | GET admin live rescue map | ✅ 200 |
| 53 | GET user rescue history | ✅ 200 |
| 54 | GET admin KPIs (rescue) | ✅ 200 |
| 55 | OTP send | ✅ 200 |
| 56 | Lockout after 5 wrong passwords | ✅ 423 on 6th attempt |

## ❌ Bugs Found & Fixed

| # | Endpoint | Bug | Fix Applied |
|---|----------|-----|-------------|
| 1 | POST /api/v1/hospitals/slots | `endTime` field doesn't exist — must use `durationMinutes` (Integer, required). `startTime` must be `HH:mm:ss` format | ✅ Documentation corrected |

## ⚠️ Minor Observations (Not Bugs)

| Observation | Detail |
|-------------|--------|
| `isVerified` vs `verified` | Response field name is `"verified"` (not `"isVerified"` as in some docs) |
| NGO dashboard wraps profile | Response has `"profile": {...}` nested, not flat |
| Search: E2E Dog not in search | `GET /adoptable-pets/search?species=DOG` returned Luna but not E2E Dog — because our NGO was unverified at test time. After verification it appears. ✅ Expected behavior |
| appointmentId in history | History uses `appointmentId` (not `id`) as the key |

---

# Coverage Verification Checklist

## ✅ Epic 1 — SOS & Rescue

| User Story | Description | Status | Endpoint |
|------------|-------------|--------|----------|
| US-1.1.2 | OTP Authentication | ✅ | POST /auth/send-otp, POST /auth/verify-otp |
| US-1.1.3 | Missed Call Auth | ✅ | POST /auth/missed-call/initiate, /verify |
| US-1.1.4 | JWT Session Token | ✅ | Returned on verify |
| US-1.2.1 | Create SOS Report | ✅ | POST /sos-reports |
| US-1.2.2 | Upload Photo/Video | ✅ | POST /sos-reports/{id}/media |
| US-1.2.3 | GPS Auto-capture | ✅ | lat/lon in request |
| US-1.3 | NGO Assignment | ✅ | POST /api/v1/ngo/assign |
| US-1.3 | Accept/Decline Mission | ✅ | POST /api/v1/ngo/missions/{id}/accept|decline |
| US-1.4.1 | Mark Arrival | ✅ | PATCH /rescue/{id}/arrival |
| US-1.4.2 | On-Site Assessment | ✅ | POST /rescue/{id}/assessment |
| US-1.5.1 | Nearby Hospitals | ✅ | GET /rescue/{id}/hospitals/nearby |
| US-1.5.2 | Hospital Alert | ✅ | POST /rescue/{id}/hospitals/{hId}/alert |
| US-1.5.3 | Emergency Booking | ✅ | POST /rescue/{id}/booking |
| US-1.5.4 | Hospital Handover | ✅ | POST /rescue/{id}/handover |
| US-1.5.5 | Release Confirmation | ✅ | POST /rescue/{id}/release |
| US-1.6 | Mission Summary | ✅ | POST /rescue-missions/{id}/summary |
| US-1.6 | Case Verification | ✅ | POST /rescue-missions/{id}/verify |
| US-1.7 | Rescue History | ✅ | GET /api/v1/users/{id}/rescue-history |
| US-1.8 | Rescue KPIs | ✅ | GET /api/v1/admin/kpis |
| US-1.8 | Admin Live Map | ✅ | GET /api/v1/admin/rescues/live |
| US-1.8 | Reassign Rescue | ✅ | PATCH /api/v1/admin/rescues/{id}/reassign |
| US-1.9 | Session Conversion | ✅ | POST /api/v1/auth/convert-session |

## ✅ Epic 2 — Pet Adoption

| User Story | Description | Status | Endpoint |
|------------|-------------|--------|----------|
| US-2.1.1 | Browse Adoptable Pets | ✅ | GET /adoptable-pets |
| US-2.1.2 | Filter/Search Pets | ✅ | GET /adoptable-pets/search |
| US-2.1.3 | Sort (newest/nearest/ready) | ✅ | sort query param |
| US-2.1.4 | Full Pet Profile | ✅ | GET /adoptable-pets/{id} |
| US-2.2.1 | Create Pet Listing | ✅ | POST /api/v1/ngo/adoptable-pets |
| US-2.2.2 | Update Listing + Audit | ✅ | PATCH /api/v1/ngo/adoptable-pets/{id} |
| US-2.2.3 | Media Gallery | ✅ | POST/GET/PATCH/DELETE /api/v1/ngo/adoptable-pets/{id}/media |
| US-2.2.4 | Archive Listing | ✅ | POST /api/v1/ngo/adoptable-pets/{id}/archive |
| US-2.3.1 | Start Application | ✅ | POST /adoption-applications |
| US-2.3.2 | Multi-Step Form | ✅ | PATCH /{id}/personal, /lifestyle, /experience, /consent |
| US-2.3.3 | Submit Application | ✅ | POST /adoption-applications/{id}/submit |
| US-2.3.4 | KYC Upload | ✅ | POST /adoption-applications/{id}/documents |
| US-2.3.5 | View Application Status | ✅ | GET /adoption-applications/{id} |
| US-2.3.6 | Withdraw Application | ✅ | POST /adoption-applications/{id}/withdraw |
| US-2.4.1 | NGO Application Queue | ✅ | GET /api/v1/ngo/adoption-applications |
| US-2.4.2 | Review Detail | ✅ | GET /api/v1/ngo/adoption-applications/{id} |
| US-2.4.3 | Approve Application | ✅ | POST /api/v1/ngo/adoption-applications/{id}/approve |
| US-2.4.4 | Reject Application | ✅ | POST /api/v1/ngo/adoption-applications/{id}/reject |
| US-2.4.5 | Request Clarification | ✅ | POST /api/v1/ngo/adoption-applications/{id}/clarify |
| US-2.4.6 | Verify KYC Documents | ✅ | POST /api/v1/ngo/adoption-applications/{id}/documents/{docId}/verify |
| US-2.5.1 | Schedule Handover | ✅ | POST /api/v1/ngo/adoptions/schedule |
| US-2.5.2 | Confirm Handover | ✅ | POST /api/v1/ngo/adoptions/{id}/confirm-handover |
| US-2.5.3 | Auto Follow-ups | ✅ | Created on confirm-handover |
| US-2.5.4 | Record Follow-up | ✅ | PATCH /api/v1/ngo/adoptions/{id}/follow-ups/{fId} |
| US-2.5.5 | Hospital Module Bridge | ✅ | Pet auto-added on confirm-handover |
| US-2.6.1 | Admin KPI Metrics | ✅ | GET /api/v1/admin/adoptions/metrics |
| US-2.6.2 | NGO Verification | ✅ | GET + POST /api/v1/admin/adoptions/ngos/{id}/verify |
| US-2.6.3 | Disputes | ✅ | POST + GET /api/v1/admin/adoptions/disputes |

## ✅ Epic 3 — Hospital Module

| User Story | Description | Status | Endpoint |
|------------|-------------|--------|----------|
| US-3.1.1 | Browse Hospitals | ✅ | GET /hospitals |
| US-3.1.2 | Filter Hospitals | ✅ | GET /hospitals/search |
| US-3.1.3 | Hospital Profile | ✅ | GET /hospitals/{id} |
| US-3.2.1 | Register Hospital | ✅ | POST /hospitals/register |
| US-3.2.2 | Manage Services | ✅ | CRUD /hospitals/{id}/services |
| US-3.2.3 | Manage Doctors | ✅ | CRUD /hospitals/{id}/doctors |
| US-3.2.4 | Operating Hours + Emergency | ✅ | PATCH /hospitals/{id}/operating-hours, /emergency-status |
| US-3.3.1 | Create Slots | ✅ | POST /hospitals/slots |
| US-3.3.2 | Blackout Dates | ✅ | POST/DELETE/GET /hospitals/{id}/blackout |
| US-3.3.3 | Slot Utilization | ✅ | GET /hospitals/{id}/utilization |
| US-3.4.1 | Book Appointment | ✅ | POST /appointments/lock + /confirm |
| US-3.4.2 | Slot Locking | ✅ | POST /appointments/lock |
| US-3.4.3–5 | Confirm with Pet/Service | ✅ | POST /appointments/confirm |
| US-3.5.1 | Cancel Appointment | ✅ | PATCH /appointments/{id}/cancel |
| US-3.5.2 | Reschedule | ✅ | PATCH /appointments/{id}/reschedule |
| US-3.5.3 | Mark Attended | ✅ | PATCH /appointments/{id}/attended |
| US-3.5.4 | Complete + Notes | ✅ | PATCH /appointments/{id}/complete |
| US-3.5.5 | Mark No-Show | ✅ | PATCH /appointments/{id}/no-show |
| US-3.6.1 | Appointment History | ✅ | GET /api/v1/users/{id}/appointments |
| US-3.6.2 | Hospital Dashboard | ✅ | GET /api/v1/hospitals/{id}/dashboard |
| US-3.7.1 | Verify Hospital | ✅ | GET /api/v1/admin/hospitals/pending + POST verify |
| US-3.7.2 | Hospital Metrics | ✅ | GET /api/v1/admin/hospitals/metrics |
| US-3.7.3 | Disable Hospital | ✅ | POST /api/v1/admin/hospitals/{id}/disable |

## ✅ Epic 4 — Platform Identity & Notifications

| User Story | Description | Status | Endpoint |
|------------|-------------|--------|----------|
| US-4.1.1 | Full Account Registration | ✅ | POST /auth/register |
| US-4.1.2 | Password Login + Lockout | ✅ | POST /auth/login |
| US-4.1.3 | RBAC Guards | ✅ | @PreAuthorize on controllers |
| US-4.1.4 | Profile View/Edit/Photo | ✅ | GET/PATCH /users/me, POST /users/me/photo |
| US-4.2.1 | In-App Notifications | ✅ | GET/PATCH/DELETE /users/me/notifications |
| US-4.2.2 | SMS Delivery Log | ✅ | Persisted via SmsServiceStub |
| US-4.3.1 | NGO Self-Registration | ✅ | POST /api/v1/ngo/register |
| US-4.3.2 | NGO Profile Management | ✅ | GET/PATCH /api/v1/ngo/profile |
| US-4.3.3 | NGO Dashboard | ✅ | GET /api/v1/ngo/dashboard |
| US-4.3.4 | Unified Audit Log | ✅ | GET /api/v1/admin/adoptions/audit-logs |

---

## ⚠️ Known Notes & Gaps

| Area | Note |
|------|------|
| Webhook Signature | `POST /auth/webhook/missed-call-verified` — signature validation is a TODO. In production, validate `X-Webhook-Signature`. |
| OTP in Dev | OTP is logged to console (SmsServiceStub). Check application logs for the OTP value in local testing. |
| Pet Status Filter (NGO listing) | The `status` filter on `GET /api/v1/ngo/adoptable-pets` is partially implemented — uses sort-hint approach. Full enum filtering deferred. |
| Rate Limiting | SOS reports: 3/hour. OTP: 5/hour. Exceeding returns HTTP 429. |
| File Storage | Files are stored locally under `uploads/` folder. In production this should be S3/GCS. |
| Admin Account | ADMIN accounts cannot be created via API. They must be inserted directly in the database. |
| JWT Expiry | Tokens expire in 24 hours (86400000ms). There is no refresh token endpoint — log in again. |
| Follow-up Reminders | Scheduled job runs daily at 9AM to send reminders. For testing, back-date `dueDate` in DB. |

---

*Document generated from live codebase — April 2026*  
*Server: `http://localhost:8081` | Database: MySQL `petz_db` | Port: 8081*
