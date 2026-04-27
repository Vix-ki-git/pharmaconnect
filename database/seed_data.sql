-- ============================================================
-- PHARMACONNECT — SEED DATA FOR TESTING
-- Run this AFTER starting the Spring Boot app once (so tables are created)
-- ============================================================

USE pharmaconnect;

-- ── STEP 1: Add medicines to the catalog ──────────────────
-- Run these first so sellers can add stock

INSERT INTO medicine (id, name, generic_name, category, manufacturer, dosage_form, strength) VALUES
  (UUID(), 'Paracetamol', 'Acetaminophen', 'Analgesic', 'Sun Pharma', 'Tablet', '500mg'),
  (UUID(), 'Ibuprofen', 'Ibuprofen', 'NSAID', 'Cipla', 'Tablet', '400mg'),
  (UUID(), 'Amoxicillin', 'Amoxicillin', 'Antibiotic', 'Dr. Reddys', 'Capsule', '500mg'),
  (UUID(), 'Metformin', 'Metformin HCl', 'Antidiabetic', 'Lupin', 'Tablet', '500mg'),
  (UUID(), 'Omeprazole', 'Omeprazole', 'Antacid', 'Zydus', 'Capsule', '20mg'),
  (UUID(), 'Cetirizine', 'Cetirizine HCl', 'Antihistamine', 'Mankind', 'Tablet', '10mg'),
  (UUID(), 'Azithromycin', 'Azithromycin', 'Antibiotic', 'Sun Pharma', 'Tablet', '500mg'),
  (UUID(), 'Atorvastatin', 'Atorvastatin Calcium', 'Statin', 'Ranbaxy', 'Tablet', '10mg'),
  (UUID(), 'Losartan', 'Losartan Potassium', 'Antihypertensive', 'Cipla', 'Tablet', '50mg'),
  (UUID(), 'Dolo 650', 'Acetaminophen', 'Analgesic', 'Micro Labs', 'Tablet', '650mg');

-- ── STEP 2: Promote a registered user to ADMIN ────────────
-- First register a user at /register with the email below, then run this:
-- UPDATE users SET role = 'ADMIN' WHERE email = 'YOUR_EMAIL_HERE';
-- Example:
-- UPDATE users SET role = 'ADMIN' WHERE email = 'admin@pharmaconnect.com';

-- ── STEP 3: Verify a pharmacy (after admin approves via UI) ──
-- After admin logs in and verifies via the Sellers page, search will work.
-- OR run this to manually verify a pharmacy:
-- UPDATE pharmacy SET is_verified = 1 WHERE id = 'YOUR_PHARMACY_ID';

-- ── HOW TO GET SEARCH WORKING (End-to-End Flow) ────────────
-- 1. Start Spring Boot backend (port 8082)
-- 2. Run this SQL file to add medicines
-- 3. Register a user at /register → then run: UPDATE users SET role='ADMIN' WHERE email='your@email.com'
-- 4. Register a pharmacy at /register-pharmacy (different email/account)
-- 5. Log in as admin → go to /admin/sellers → click "Verify & Approve" on the pharmacy
-- 6. Log in as seller → go to /seller/inventory → add medicines with prices
-- 7. Log in as patient (or just open /search) → search for a medicine → results appear!

-- ── OPTIONAL: Quick test setup (manual pharmacy + stock) ──
-- After running step 2, use the UI for all remaining steps.
-- The system requires verified pharmacies to appear in search.
