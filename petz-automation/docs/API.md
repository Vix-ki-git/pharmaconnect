# PETZ Animal Welfare Platform — API Documentation

**Base URL (Local):** `http://localhost:8081/api`  
**Base URL (Production):** `https://petz-production.up.railway.app/api`

All responses follow the format:
```json
{ "success": true, "data": {}, "message": "Success" }
```

Authentication is via **Bearer token** in the `Authorization` header:
```
Authorization: Bearer <token>
```

---

## Table of Contents
- [Auth](#-auth)
- [User](#-user)
- [Pets](#-pets)
- [Hospitals — Public](#-hospitals--public)
- [Hospitals — Hospital Role](#-hospitals--hospital-role)
- [Appointments](#-appointments)
- [Rescue](#-rescue)
- [Adoption](#-adoption)
- [NGO](#-ngo)
- [Notifications](#-notifications)
- [Admin](#-admin)

---

## 🔐 Auth

### POST `/auth/register`
Register a new account. NGO and HOSPITAL roles require admin approval before they can log in.

**Request Body:**
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123",
  "phone": "9876543210",
  "role": "USER",
  "address": "123 Main Street",
  "city": "Mumbai"
}
```
> `role` — `USER` | `NGO` | `HOSPITAL`

**Response:**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGci...",
    "userId": 1,
    "email": "john@example.com",
    "name": "John Doe",
    "role": "USER",
    "isApproved": true
  },
  "message": "Success"
}
```

---

### POST `/auth/login`
Login and receive a JWT token.

**Request Body:**
```json
{
  "email": "john@example.com",
  "password": "password123"
}
```

**Response:** Same as register.

---

## 👤 User

> All endpoints require authentication.

### GET `/users/me`
Get the authenticated user's profile.

---

### PUT `/users/me`
Update profile details.

**Request Body:**
```json
{
  "name": "John Doe",
  "phone": "9876543210",
  "address": "456 New Street",
  "city": "Delhi"
}
```

---

### POST `/users/me/photo`
Upload a profile photo.

**Request:** `multipart/form-data`
| Field | Type | Description |
|-------|------|-------------|
| `file` | File | Image file (jpg/png) |

---

## 🐾 Pets

> All endpoints require authentication.

### POST `/pets/`
Add a new pet.

**Request Body:**
```json
{
  "name": "Buddy",
  "species": "Dog",
  "breed": "Labrador",
  "ageYears": 3,
  "gender": "MALE",
  "weightKg": 28.5,
  "notes": "Allergic to chicken"
}
```

---

### GET `/pets/my`
Get all pets belonging to the logged-in user.

---

### GET `/pets/{id}`
Get a single pet by ID.

---

### PUT `/pets/{id}`
Update a pet's details. Same body as POST `/pets/`.

---

### POST `/pets/{id}/photo`
Upload a photo for a pet.

**Request:** `multipart/form-data`
| Field | Type | Description |
|-------|------|-------------|
| `file` | File | Image file (jpg/png) |

---

### DELETE `/pets/{id}`
Delete a pet.

---

## 🏥 Hospitals — Public

> No authentication required.

### GET `/hospitals/public`
List all registered hospitals. Filter by city using a query param.

**Query Params:**
| Param | Type | Required | Description |
|-------|------|----------|-------------|
| `city` | String | No | Filter by city name |

---

### GET `/hospitals/public/{id}`
Get a single hospital by ID.

---

### GET `/hospitals/public/{hospitalId}/doctors`
Get all doctors at a hospital.

---

### GET `/hospitals/public/{hospitalId}/doctors/{doctorId}/slots`
Get available time slots for a doctor on a specific date.

**Query Params:**
| Param | Type | Required | Description |
|-------|------|----------|-------------|
| `date` | LocalDate | Yes | Date in `YYYY-MM-DD` format |

---

## 🏥 Hospitals — Hospital Role

> All endpoints require `HOSPITAL` role.

### GET `/hospitals/profile`
Get the logged-in hospital's profile.

---

### POST `/hospitals/profile`
Create or update the hospital profile.

**Request Body:**
```json
{
  "name": "PetCare Veterinary Clinic",
  "city": "Mumbai",
  "address": "12 Linking Road, Bandra",
  "phone": "022-12345678",
  "email": "info@petcare.com",
  "description": "24x7 veterinary care for all animals"
}
```

---

### POST `/hospitals/profile/logo`
Upload the hospital logo.

**Request:** `multipart/form-data`
| Field | Type | Description |
|-------|------|-------------|
| `file` | File | Image file (jpg/png) |

---

### POST `/hospitals/profile/doctors`
Add a doctor to the hospital.

**Request Body:**
```json
{
  "name": "Dr. Anjali Mehta",
  "specialization": "Small Animal Surgery",
  "phone": "9876543210",
  "email": "anjali@petcare.com",
  "scheduleStart": "09:00",
  "scheduleEnd": "17:00",
  "slotDuration": 30
}
```
> `slotDuration` — appointment slot length in minutes

---

### PUT `/hospitals/profile/doctors/{doctorId}`
Update a doctor's details. Same body as POST above.

---

### DELETE `/hospitals/profile/doctors/{doctorId}`
Remove a doctor from the hospital.

---

## 📅 Appointments

### POST `/appointments/`
Book an appointment. Requires `USER` role.

**Request Body:**
```json
{
  "hospitalId": 1,
  "doctorId": 1,
  "petId": 1,
  "apptDate": "2025-06-15",
  "apptTime": "10:00",
  "reason": "Annual vaccination checkup"
}
```
> `apptDate` — `YYYY-MM-DD` | `apptTime` — `HH:mm`

---

### GET `/appointments/my`
Get all appointments for the logged-in user.

---

### DELETE `/appointments/{id}`
Cancel an appointment.

---

### GET `/appointments/slots`
Get available slots for a doctor on a date.

**Query Params:**
| Param | Type | Required | Description |
|-------|------|----------|-------------|
| `doctorId` | Long | Yes | Doctor ID |
| `date` | LocalDate | Yes | Date in `YYYY-MM-DD` format |

---

### GET `/appointments/hospital`
Get all appointments for the logged-in hospital. Requires `HOSPITAL` role.

---

### PATCH `/appointments/{id}/status`
Update appointment status. Requires `HOSPITAL` or `ADMIN` role.

**Request Body:**
```json
{
  "status": "CONFIRMED"
}
```
> `status` — `CONFIRMED` | `CANCELLED` | `COMPLETED`

---

## 🆘 Rescue

### POST `/rescue/`
Report a rescue. Supports an optional photo upload.

**Request:** `multipart/form-data`
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `data` | JSON string | Yes | Rescue details (see below) |
| `photo` | File | No | Photo of the animal |

**`data` JSON:**
```json
{
  "animalType": "Dog",
  "description": "Injured stray dog, bleeding from leg",
  "latitude": 19.0760,
  "longitude": 72.8777,
  "address": "Near Dadar Station, Mumbai",
  "criticality": "HIGH",
  "reporterPhone": "9876543210"
}
```
> `criticality` — `LOW` | `MEDIUM` | `HIGH`

---

### GET `/rescue/my`
Get all rescue reports filed by the logged-in user.

---

### GET `/rescue/{id}`
Get a single rescue report by ID.

---

### GET `/rescue/ngo`
Get all incoming rescue reports. Requires `NGO` role.

---

### POST `/rescue/{id}/respond`
Respond to a rescue report. Requires `NGO` role.

**Request Body:**
```json
{
  "response": "Our team is on the way. ETA 30 minutes."
}
```

---

### POST `/rescue/{id}/complete`
Mark a rescue as completed. Requires `NGO` role.

**Request Body:**
```json
{
  "notes": "Animal rescued and brought to shelter. Receiving medical care."
}
```

---

## 🐶 Adoption

### GET `/adoption/animals`
Browse all animals available for adoption. No authentication required.

**Query Params:**
| Param | Type | Required | Description |
|-------|------|----------|-------------|
| `species` | String | No | e.g. `Dog`, `Cat` |
| `city` | String | No | Filter by city |

---

### GET `/adoption/animals/{id}`
Get a single adoptable animal by ID. No authentication required.

---

### POST `/adoption/apply`
Apply to adopt an animal. Requires `USER` role.

**Request Body:**
```json
{
  "animalId": 1,
  "reason": "I want to give this animal a forever home",
  "experience": "I owned a golden retriever for 8 years",
  "housingType": "APARTMENT",
  "hasOtherPets": false
}
```
> `housingType` — `APARTMENT` | `HOUSE` | `FARM`

---

### GET `/adoption/my-applications`
Get all adoption applications submitted by the logged-in user.

---

### GET `/adoption/ngo/animals`
Get all animals listed by the logged-in NGO. Requires `NGO` role.

---

### POST `/adoption/ngo/animals`
List a new animal for adoption. Requires `NGO` role.

**Request Body:**
```json
{
  "name": "Bruno",
  "species": "Dog",
  "breed": "Indie Mix",
  "ageMonths": 18,
  "gender": "MALE",
  "description": "Friendly, playful, good with children",
  "city": "Mumbai",
  "isVaccinated": true,
  "isNeutered": false,
  "status": "AVAILABLE"
}
```
> `status` — `AVAILABLE` | `ADOPTED` | `UNDER_REVIEW`

---

### PUT `/adoption/ngo/animals/{id}`
Update an animal's details. Requires `NGO` role. Same body as POST above.

---

### POST `/adoption/ngo/animals/{id}/photo`
Upload a photo for an animal. Requires `NGO` role.

**Request:** `multipart/form-data`
| Field | Type | Description |
|-------|------|-------------|
| `file` | File | Image file (jpg/png) |

---

### DELETE `/adoption/ngo/animals/{id}`
Remove an animal listing. Requires `NGO` role.

---

### GET `/adoption/ngo/applications`
Get all adoption applications for the NGO's animals. Requires `NGO` role.

---

### PATCH `/adoption/ngo/applications/{id}/review`
Approve or reject an adoption application. Requires `NGO` role.

**Request Body:**
```json
{
  "status": "APPROVED",
  "notes": "Applicant meets all requirements."
}
```
> `status` — `APPROVED` | `REJECTED` | `PENDING`

---

## 🏢 NGO

### GET `/ngo/public`
List all NGOs. No authentication required.

---

### GET `/ngo/public/{id}`
Get a single NGO by ID. No authentication required.

---

### GET `/ngo/profile`
Get the logged-in NGO's profile. Requires `NGO` role.

---

### POST `/ngo/profile`
Create or update the NGO profile. Requires `NGO` role.

**Request Body:**
```json
{
  "name": "Paws & Care NGO",
  "city": "Pune",
  "address": "Plot 45, Koregaon Park",
  "phone": "020-98765432",
  "email": "contact@pawscare.org",
  "description": "Rescuing and rehoming stray animals since 2015"
}
```

---

### POST `/ngo/profile/logo`
Upload the NGO logo. Requires `NGO` role.

**Request:** `multipart/form-data`
| Field | Type | Description |
|-------|------|-------------|
| `file` | File | Image file (jpg/png) |

---

## 🔔 Notifications

> All endpoints require authentication.

### GET `/notifications/`
Get all notifications for the logged-in user.

---

### GET `/notifications/unread`
Get only unread notifications.

---

### GET `/notifications/unread/count`
Get the count of unread notifications.

**Response:**
```json
{ "success": true, "data": 5, "message": "Success" }
```

---

### PATCH `/notifications/{id}/read`
Mark a single notification as read.

---

### PATCH `/notifications/read-all`
Mark all notifications as read.

---

## ⚙️ Admin

> All endpoints require `ADMIN` role.

### GET `/admin/users`
Get all registered users.

---

### GET `/admin/pending-approvals`
Get NGO and HOSPITAL accounts awaiting approval.

---

### PATCH `/admin/users/{id}/approve`
Approve or reject a user account.

**Request Body:**
```json
{ "approved": true }
```

---

### PATCH `/admin/users/{id}/toggle`
Enable or disable a user account.

**Request Body:**
```json
{ "active": false }
```

---

### GET `/admin/ngos`
Get all NGOs on the platform.

---

### GET `/admin/ngos/unverified`
Get NGOs that have not been verified yet.

---

### PATCH `/admin/ngos/{id}/verify`
Verify or unverify an NGO.

**Request Body:**
```json
{ "verified": true }
```

---

### PATCH `/admin/ngos/{id}/toggle`
Enable or disable an NGO.

**Request Body:**
```json
{ "active": true }
```

---

### GET `/admin/hospitals`
Get all hospitals on the platform.

---

### PATCH `/admin/hospitals/{id}/toggle`
Enable or disable a hospital.

**Request Body:**
```json
{ "active": true }
```

---

### GET `/admin/rescues`
Get all rescue reports. Optionally filter by status.

**Query Params:**
| Param | Type | Required | Description |
|-------|------|----------|-------------|
| `status` | String | No | `PENDING` \| `RESPONDED` \| `COMPLETED` |

---

### GET `/admin/adoptions`
Get all adoption applications across the platform.
