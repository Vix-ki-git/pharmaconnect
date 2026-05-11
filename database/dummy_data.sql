-- ============================================================
-- PHARMACONNECT — DUMMY DATA FOR DEMO / TESTING
-- ============================================================
-- Prerequisites:
--   1. The Spring Boot app must have been started at least once
--      (so Hibernate has created all the tables).
--   2. MySQL must be running and the `pharmaconnect` database exists.
--
-- Run order: db_setup.sql  →  start Spring Boot once  →  this file.
--
-- All passwords for dummy users are: "password"  (BCrypt cost 10)
-- The bcrypt hash below is the canonical hasc-h for the string
-- "password"; if you ever rotate it, regenerate via Spring's
-- BCryptPasswordEncoder.
--
-- Coordinates are Bangalore-area so distances/Haversine demos
-- look realistic.
-- ============================================================

USE pharmaconnect;

-- ─────────────────────────────────────────────────────────────
-- OPTIONAL: wipe existing data before re-seeding.
-- Uncomment the block below if you want a clean slate.
-- ─────────────────────────────────────────────────────────────
-- SET FOREIGN_KEY_CHECKS = 0;
-- TRUNCATE TABLE predictive_alert;
-- TRUNCATE TABLE demand_analytics;
-- TRUNCATE TABLE search_log;
-- TRUNCATE TABLE reservation;
-- TRUNCATE TABLE medicine_alternative;
-- TRUNCATE TABLE pharmacy_documents;
-- TRUNCATE TABLE pharmacy_stock;
-- TRUNCATE TABLE pharmacy;
-- TRUNCATE TABLE medicine;
-- TRUNCATE TABLE users;
-- SET FOREIGN_KEY_CHECKS = 1;

SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 1. USERS
-- ============================================================
-- 1 admin, 4 sellers (one per pharmacy), 5 patients
-- All passwords = "password"
INSERT INTO users (id, name, email, phone, password_hash, role, lat, lng, created_at) VALUES
  -- Admin
  ('00000000-0000-0000-0000-000000000001', 'Admin User',         'admin@pharmaconnect.com', '+919000000001',
   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN',  12.9716, 77.5946, NOW() - INTERVAL 60 DAY),

  -- Sellers (each owns one pharmacy below)
  ('00000000-0000-0000-0000-000000000010', 'Rajesh Kumar',       'rajesh@medplus.com',      '+919000000010',
   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'SELLER', 12.9352, 77.6245, NOW() - INTERVAL 45 DAY),
  ('00000000-0000-0000-0000-000000000011', 'Priya Sharma',       'priya@apollopharm.com',   '+919000000011',
   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'SELLER', 12.9784, 77.6408, NOW() - INTERVAL 40 DAY),
  ('00000000-0000-0000-0000-000000000012', 'Arjun Reddy',        'arjun@247pharma.com',     '+919000000012',
   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'SELLER', 12.9698, 77.7500, NOW() - INTERVAL 35 DAY),
  ('00000000-0000-0000-0000-000000000013', 'Meera Iyer',         'meera@wellness.com',      '+919000000013',
   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'SELLER', 12.9116, 77.6473, NOW() - INTERVAL 5 DAY),

  -- Patients
  ('00000000-0000-0000-0000-000000000020', 'Anil Verma',         'anil@example.com',        '+919000000020',
   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'BUYER',  12.9716, 77.5946, NOW() - INTERVAL 30 DAY),
  ('00000000-0000-0000-0000-000000000021', 'Sneha Pillai',       'sneha@example.com',       '+919000000021',
   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'BUYER',  12.9550, 77.6100, NOW() - INTERVAL 25 DAY),
  ('00000000-0000-0000-0000-000000000022', 'Vikram Singh',       'vikram@example.com',      '+919000000022',
   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'BUYER',  12.9800, 77.6300, NOW() - INTERVAL 20 DAY),
  ('00000000-0000-0000-0000-000000000023', 'Ananya Nair',        'ananya@example.com',      '+919000000023',
   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'BUYER',  12.9300, 77.6800, NOW() - INTERVAL 15 DAY),
  ('00000000-0000-0000-0000-000000000024', 'Karthik Menon',      'karthik@example.com',     '+919000000024',
   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'BUYER',  12.9000, 77.6000, NOW() - INTERVAL 7 DAY);

-- ============================================================
-- 2. PHARMACIES
-- ============================================================
-- 3 verified+active, 1 pending verification
INSERT INTO pharmacy (id, owner_id, name, address, lat, lng, phone, is_24_7, is_verified, is_active, created_at) VALUES
  ('00000000-0000-0000-0000-000000000100', '00000000-0000-0000-0000-000000000010',
   'MedPlus HSR Layout',
   '14th Main Road, Sector 4, HSR Layout, Bangalore 560102',
   12.9352, 77.6245, '+918012345001', 1, 1, 1, NOW() - INTERVAL 44 DAY),

  ('00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000011',
   'Apollo Pharmacy Indiranagar',
   '100 Feet Road, Indiranagar, Bangalore 560038',
   12.9784, 77.6408, '+918012345002', 0, 1, 1, NOW() - INTERVAL 39 DAY),

  ('00000000-0000-0000-0000-000000000102', '00000000-0000-0000-0000-000000000012',
   '24/7 Pharma Whitefield',
   'ITPL Main Road, Whitefield, Bangalore 560066',
   12.9698, 77.7500, '+918012345003', 1, 1, 1, NOW() - INTERVAL 34 DAY),

  ('00000000-0000-0000-0000-000000000103', '00000000-0000-0000-0000-000000000013',
   'Wellness Forever BTM',
   '16th Main, BTM 2nd Stage, Bangalore 560076',
   12.9116, 77.6473, '+918012345004', 0, 0, 1, NOW() - INTERVAL 4 DAY);

-- ============================================================
-- 3. MEDICINE CATALOG
-- ============================================================
-- 15 medicines. Some share a generic_name to seed alternative pairs.
INSERT INTO medicine (id, name, generic_name, category, manufacturer, dosage_form, strength) VALUES
  ('00000000-0000-0000-0000-000000000200', 'Paracetamol',   'Acetaminophen',         'Analgesic',        'Sun Pharma',  'Tablet',  '500mg'),
  ('00000000-0000-0000-0000-000000000201', 'Dolo 650',      'Acetaminophen',         'Analgesic',        'Micro Labs',  'Tablet',  '650mg'),
  ('00000000-0000-0000-0000-000000000202', 'Crocin 500',    'Acetaminophen',         'Analgesic',        'GSK',         'Tablet',  '500mg'),
  ('00000000-0000-0000-0000-000000000203', 'Ibuprofen',     'Ibuprofen',             'NSAID',            'Cipla',       'Tablet',  '400mg'),
  ('00000000-0000-0000-0000-000000000204', 'Brufen',        'Ibuprofen',             'NSAID',            'Abbott',      'Tablet',  '400mg'),
  ('00000000-0000-0000-0000-000000000205', 'Amoxicillin',   'Amoxicillin',           'Antibiotic',       'Dr. Reddys',  'Capsule', '500mg'),
  ('00000000-0000-0000-0000-000000000206', 'Mox',           'Amoxicillin',           'Antibiotic',       'Sun Pharma',  'Capsule', '500mg'),
  ('00000000-0000-0000-0000-000000000207', 'Metformin',     'Metformin HCl',         'Antidiabetic',     'Lupin',       'Tablet',  '500mg'),
  ('00000000-0000-0000-0000-000000000208', 'Glycomet',      'Metformin HCl',         'Antidiabetic',     'USV',         'Tablet',  '500mg'),
  ('00000000-0000-0000-0000-000000000209', 'Omeprazole',    'Omeprazole',            'Antacid',          'Zydus',       'Capsule', '20mg'),
  ('00000000-0000-0000-0000-000000000210', 'Cetirizine',    'Cetirizine HCl',        'Antihistamine',    'Mankind',     'Tablet',  '10mg'),
  ('00000000-0000-0000-0000-000000000211', 'Azithromycin',  'Azithromycin',          'Antibiotic',       'Sun Pharma',  'Tablet',  '500mg'),
  ('00000000-0000-0000-0000-000000000212', 'Atorvastatin',  'Atorvastatin Calcium',  'Statin',           'Ranbaxy',     'Tablet',  '10mg'),
  ('00000000-0000-0000-0000-000000000213', 'Losartan',      'Losartan Potassium',    'Antihypertensive', 'Cipla',       'Tablet',  '50mg'),
  ('00000000-0000-0000-0000-000000000214', 'Pantoprazole',  'Pantoprazole',          'Antacid',          'Alkem',       'Tablet',  '40mg');

-- ============================================================
-- 4. PHARMACY STOCK
-- ============================================================
-- Stock distributed across the 4 pharmacies.
-- Includes low-stock and out-of-stock items so seller dashboard demos shine.
INSERT INTO pharmacy_stock (id, pharmacy_id, medicine_id, quantity, price, manufacturing_date, expiry_date, last_updated) VALUES
  -- ── MedPlus HSR Layout (00000000-0000-0000-0000-000000000100) ──
  ('00000000-0000-0000-0000-000000000300', '00000000-0000-0000-0000-000000000100', '00000000-0000-0000-0000-000000000200', 250,  18.50, '2025-11-01', '2027-11-01', NOW() - INTERVAL 1 DAY),
  ('00000000-0000-0000-0000-000000000301', '00000000-0000-0000-0000-000000000100', '00000000-0000-0000-0000-000000000201', 180,  35.00, '2025-10-15', '2027-10-15', NOW() - INTERVAL 1 DAY),
  ('00000000-0000-0000-0000-000000000302', '00000000-0000-0000-0000-000000000100', '00000000-0000-0000-0000-000000000203',   8,  42.00, '2025-08-01', '2027-08-01', NOW() - INTERVAL 2 DAY), -- low stock
  ('00000000-0000-0000-0000-000000000303', '00000000-0000-0000-0000-000000000100', '00000000-0000-0000-0000-000000000205', 120,  85.00, '2025-09-10', '2027-09-10', NOW() - INTERVAL 1 DAY),
  ('00000000-0000-0000-0000-000000000304', '00000000-0000-0000-0000-000000000100', '00000000-0000-0000-0000-000000000209',  60, 110.00, '2025-12-01', '2027-12-01', NOW() - INTERVAL 1 DAY),
  ('00000000-0000-0000-0000-000000000305', '00000000-0000-0000-0000-000000000100', '00000000-0000-0000-0000-000000000210',   0,  28.00, '2025-07-01', '2027-07-01', NOW() - INTERVAL 3 DAY), -- out of stock
  ('00000000-0000-0000-0000-000000000306', '00000000-0000-0000-0000-000000000100', '00000000-0000-0000-0000-000000000212',  45, 125.00, '2025-11-15', '2027-11-15', NOW() - INTERVAL 1 DAY),

  -- ── Apollo Indiranagar (00000000-0000-0000-0000-000000000101) ──
  ('00000000-0000-0000-0000-000000000310', '00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000200', 300,  20.00, '2025-10-01', '2027-10-01', NOW() - INTERVAL 1 DAY),
  ('00000000-0000-0000-0000-000000000311', '00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000202', 200,  22.00, '2025-11-20', '2027-11-20', NOW() - INTERVAL 1 DAY),
  ('00000000-0000-0000-0000-000000000312', '00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000204',  90,  44.50, '2025-09-05', '2027-09-05', NOW() - INTERVAL 2 DAY),
  ('00000000-0000-0000-0000-000000000313', '00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000207', 150,  18.00, '2025-08-20', '2027-08-20', NOW() - INTERVAL 1 DAY),
  ('00000000-0000-0000-0000-000000000314', '00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000211',   5, 165.00, '2025-09-01', '2027-09-01', NOW() - INTERVAL 1 DAY), -- low stock
  ('00000000-0000-0000-0000-000000000315', '00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000213', 100,  78.00, '2025-12-10', '2027-12-10', NOW() - INTERVAL 1 DAY),
  ('00000000-0000-0000-0000-000000000316', '00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000214',  75,  95.00, '2025-10-25', '2027-10-25', NOW() - INTERVAL 1 DAY),

  -- ── 24/7 Pharma Whitefield (00000000-0000-0000-0000-000000000102) ──
  ('00000000-0000-0000-0000-000000000320', '00000000-0000-0000-0000-000000000102', '00000000-0000-0000-0000-000000000200', 500,  17.00, '2025-11-15', '2027-11-15', NOW() - INTERVAL 1 DAY),
  ('00000000-0000-0000-0000-000000000321', '00000000-0000-0000-0000-000000000102', '00000000-0000-0000-0000-000000000201', 400,  33.00, '2025-12-01', '2027-12-01', NOW() - INTERVAL 1 DAY),
  ('00000000-0000-0000-0000-000000000322', '00000000-0000-0000-0000-000000000102', '00000000-0000-0000-0000-000000000203', 220,  40.00, '2025-10-10', '2027-10-10', NOW() - INTERVAL 1 DAY),
  ('00000000-0000-0000-0000-000000000323', '00000000-0000-0000-0000-000000000102', '00000000-0000-0000-0000-000000000205', 180,  82.00, '2025-09-20', '2027-09-20', NOW() - INTERVAL 1 DAY),
  ('00000000-0000-0000-0000-000000000324', '00000000-0000-0000-0000-000000000102', '00000000-0000-0000-0000-000000000206', 130,  78.00, '2025-09-25', '2027-09-25', NOW() - INTERVAL 1 DAY),
  ('00000000-0000-0000-0000-000000000325', '00000000-0000-0000-0000-000000000102', '00000000-0000-0000-0000-000000000208',  95,  21.00, '2025-08-15', '2027-08-15', NOW() - INTERVAL 1 DAY),
  ('00000000-0000-0000-0000-000000000326', '00000000-0000-0000-0000-000000000102', '00000000-0000-0000-0000-000000000209',  80, 108.00, '2025-11-05', '2027-11-05', NOW() - INTERVAL 1 DAY),
  ('00000000-0000-0000-0000-000000000327', '00000000-0000-0000-0000-000000000102', '00000000-0000-0000-0000-000000000210', 160,  26.00, '2025-10-30', '2027-10-30', NOW() - INTERVAL 1 DAY),
  ('00000000-0000-0000-0000-000000000328', '00000000-0000-0000-0000-000000000102', '00000000-0000-0000-0000-000000000211',  70, 160.00, '2025-09-15', '2027-09-15', NOW() - INTERVAL 1 DAY),

  -- ── Wellness Forever BTM (00000000-0000-0000-0000-000000000103) — pending verification ──
  -- These rows exist but won't appear in patient search until admin verifies.
  ('00000000-0000-0000-0000-000000000340', '00000000-0000-0000-0000-000000000103', '00000000-0000-0000-0000-000000000200', 100,  19.00, '2025-12-01', '2027-12-01', NOW() - INTERVAL 1 DAY),
  ('00000000-0000-0000-0000-000000000341', '00000000-0000-0000-0000-000000000103', '00000000-0000-0000-0000-000000000209',  50, 112.00, '2025-12-05', '2027-12-05', NOW() - INTERVAL 1 DAY);

-- ============================================================
-- 5. PHARMACY DOCUMENTS (licenses)
-- ============================================================
INSERT INTO pharmacy_documents (id, pharmacy_id, document_type, document_url, status, uploaded_at) VALUES
  ('00000000-0000-0000-0000-000000000400', '00000000-0000-0000-0000-000000000100', 'DRUG_LICENSE',
   'uploads/licenses/dummy-medplus-license.pdf', 'APPROVED', NOW() - INTERVAL 43 DAY),
  ('00000000-0000-0000-0000-000000000401', '00000000-0000-0000-0000-000000000101', 'DRUG_LICENSE',
   'uploads/licenses/dummy-apollo-license.pdf',  'APPROVED', NOW() - INTERVAL 38 DAY),
  ('00000000-0000-0000-0000-000000000402', '00000000-0000-0000-0000-000000000102', 'DRUG_LICENSE',
   'uploads/licenses/dummy-247-license.pdf',     'APPROVED', NOW() - INTERVAL 33 DAY),
  ('00000000-0000-0000-0000-000000000403', '00000000-0000-0000-0000-000000000103', 'DRUG_LICENSE',
   'uploads/licenses/dummy-wellness-license.pdf', 'PENDING',  NOW() - INTERVAL 3 DAY);

-- ============================================================
-- 6. MEDICINE ALTERNATIVES (bidirectional, mirrors backend behavior)
-- ============================================================
INSERT INTO medicine_alternative (id, medicine_id, alternative_id, equivalence_note) VALUES
  -- Paracetamol ↔ Dolo 650
  ('00000000-0000-0000-0000-000000000500', '00000000-0000-0000-0000-000000000200', '00000000-0000-0000-0000-000000000201', 'Same active ingredient, higher strength'),
  ('00000000-0000-0000-0000-000000000501', '00000000-0000-0000-0000-000000000201', '00000000-0000-0000-0000-000000000200', 'Same active ingredient, lower strength'),

  -- Paracetamol ↔ Crocin 500
  ('00000000-0000-0000-0000-000000000502', '00000000-0000-0000-0000-000000000200', '00000000-0000-0000-0000-000000000202', 'Identical formulation, different brand'),
  ('00000000-0000-0000-0000-000000000503', '00000000-0000-0000-0000-000000000202', '00000000-0000-0000-0000-000000000200', 'Identical formulation, different brand'),

  -- Ibuprofen ↔ Brufen
  ('00000000-0000-0000-0000-000000000504', '00000000-0000-0000-0000-000000000203', '00000000-0000-0000-0000-000000000204', 'Identical formulation, different brand'),
  ('00000000-0000-0000-0000-000000000505', '00000000-0000-0000-0000-000000000204', '00000000-0000-0000-0000-000000000203', 'Identical formulation, different brand'),

  -- Amoxicillin ↔ Mox
  ('00000000-0000-0000-0000-000000000506', '00000000-0000-0000-0000-000000000205', '00000000-0000-0000-0000-000000000206', 'Generic equivalent'),
  ('00000000-0000-0000-0000-000000000507', '00000000-0000-0000-0000-000000000206', '00000000-0000-0000-0000-000000000205', 'Generic equivalent'),

  -- Metformin ↔ Glycomet
  ('00000000-0000-0000-0000-000000000508', '00000000-0000-0000-0000-000000000207', '00000000-0000-0000-0000-000000000208', 'Generic equivalent'),
  ('00000000-0000-0000-0000-000000000509', '00000000-0000-0000-0000-000000000208', '00000000-0000-0000-0000-000000000207', 'Generic equivalent'),

  -- Omeprazole ↔ Pantoprazole (same class, different drug)
  ('00000000-0000-0000-0000-000000000510', '00000000-0000-0000-0000-000000000209', '00000000-0000-0000-0000-000000000214', 'Same therapeutic class (PPI)'),
  ('00000000-0000-0000-0000-000000000511', '00000000-0000-0000-0000-000000000214', '00000000-0000-0000-0000-000000000209', 'Same therapeutic class (PPI)');

-- ============================================================
-- 7. RESERVATIONS
-- ============================================================
-- Mix of statuses to demonstrate every code path.
-- NOTE: For accurate stock numbers, the quantity in pharmacy_stock above
-- already reflects "post-reservation-deduction" balances for PENDING rows.
INSERT INTO reservation (id, user_id, pharmacy_id, medicine_id, quantity, status, hold_at, expires_at, created_at, updated_at) VALUES
  -- Active hold — Anil reserved Paracetamol at MedPlus, expires in ~25 min
  ('00000000-0000-0000-0000-000000000600',
   '00000000-0000-0000-0000-000000000020',
   '00000000-0000-0000-0000-000000000100',
   '00000000-0000-0000-0000-000000000200',
   10, 'PENDING',
   NOW() - INTERVAL 5 MINUTE,  NOW() + INTERVAL 25 MINUTE,
   NOW() - INTERVAL 5 MINUTE,  NOW() - INTERVAL 5 MINUTE),

  -- Active hold — Sneha reserved Amoxicillin at Apollo
  ('00000000-0000-0000-0000-000000000601',
   '00000000-0000-0000-0000-000000000021',
   '00000000-0000-0000-0000-000000000101',
   '00000000-0000-0000-0000-000000000205',
   2,  'PENDING',
   NOW() - INTERVAL 10 MINUTE, NOW() + INTERVAL 20 MINUTE,
   NOW() - INTERVAL 10 MINUTE, NOW() - INTERVAL 10 MINUTE),

  -- Vikram claimed his Ibuprofen reservation yesterday
  ('00000000-0000-0000-0000-000000000602',
   '00000000-0000-0000-0000-000000000022',
   '00000000-0000-0000-0000-000000000102',
   '00000000-0000-0000-0000-000000000203',
   5,  'CLAIMED',
   NOW() - INTERVAL 1 DAY,     NOW() - INTERVAL 1 DAY + INTERVAL 30 MINUTE,
   NOW() - INTERVAL 1 DAY,     NOW() - INTERVAL 23 HOUR),

  -- Ananya cancelled an Omeprazole reservation
  ('00000000-0000-0000-0000-000000000603',
   '00000000-0000-0000-0000-000000000023',
   '00000000-0000-0000-0000-000000000102',
   '00000000-0000-0000-0000-000000000209',
   3,  'CANCELLED',
   NOW() - INTERVAL 2 DAY,     NOW() - INTERVAL 2 DAY + INTERVAL 30 MINUTE,
   NOW() - INTERVAL 2 DAY,     NOW() - INTERVAL 2 DAY + INTERVAL 5 MINUTE),

  -- Karthik's reservation expired — cron already swept it
  ('00000000-0000-0000-0000-000000000604',
   '00000000-0000-0000-0000-000000000024',
   '00000000-0000-0000-0000-000000000100',
   '00000000-0000-0000-0000-000000000212',
   1,  'EXPIRED',
   NOW() - INTERVAL 3 DAY,     NOW() - INTERVAL 3 DAY + INTERVAL 30 MINUTE,
   NOW() - INTERVAL 3 DAY,     NOW() - INTERVAL 3 DAY + INTERVAL 35 MINUTE),

  -- Anil claimed Atorvastatin earlier this week
  ('00000000-0000-0000-0000-000000000605',
   '00000000-0000-0000-0000-000000000020',
   '00000000-0000-0000-0000-000000000100',
   '00000000-0000-0000-0000-000000000212',
   2,  'CLAIMED',
   NOW() - INTERVAL 4 DAY,     NOW() - INTERVAL 4 DAY + INTERVAL 30 MINUTE,
   NOW() - INTERVAL 4 DAY,     NOW() - INTERVAL 4 DAY + INTERVAL 15 MINUTE),

  -- Sneha emergency-reserved Azithromycin at 24/7 pharma
  ('00000000-0000-0000-0000-000000000606',
   '00000000-0000-0000-0000-000000000021',
   '00000000-0000-0000-0000-000000000102',
   '00000000-0000-0000-0000-000000000211',
   1,  'CLAIMED',
   NOW() - INTERVAL 6 DAY,     NOW() - INTERVAL 6 DAY + INTERVAL 30 MINUTE,
   NOW() - INTERVAL 6 DAY,     NOW() - INTERVAL 6 DAY + INTERVAL 12 MINUTE),

  -- Vikram has another active hold on Cetirizine at 24/7
  ('00000000-0000-0000-0000-000000000607',
   '00000000-0000-0000-0000-000000000022',
   '00000000-0000-0000-0000-000000000102',
   '00000000-0000-0000-0000-000000000210',
   4,  'PENDING',
   NOW() - INTERVAL 2 MINUTE,  NOW() + INTERVAL 28 MINUTE,
   NOW() - INTERVAL 2 MINUTE,  NOW() - INTERVAL 2 MINUTE);

-- ============================================================
-- 8. SEARCH LOG
-- ============================================================
INSERT INTO search_log (id, user_id, query, emergency_mode, user_lat, user_lng, searched_at) VALUES
  ('00000000-0000-0000-0000-000000000700', '00000000-0000-0000-0000-000000000020', 'paracetamol',  0, 12.9716, 77.5946, NOW() - INTERVAL 1 HOUR),
  ('00000000-0000-0000-0000-000000000701', '00000000-0000-0000-0000-000000000020', 'amoxicillin',  0, 12.9716, 77.5946, NOW() - INTERVAL 2 HOUR),
  ('00000000-0000-0000-0000-000000000702', '00000000-0000-0000-0000-000000000021', 'azithromycin', 1, 12.9550, 77.6100, NOW() - INTERVAL 6 DAY),
  ('00000000-0000-0000-0000-000000000703', '00000000-0000-0000-0000-000000000022', 'ibuprofen',    0, 12.9800, 77.6300, NOW() - INTERVAL 1 DAY),
  ('00000000-0000-0000-0000-000000000704', '00000000-0000-0000-0000-000000000022', 'cetirizine',   1, 12.9800, 77.6300, NOW() - INTERVAL 30 MINUTE),
  ('00000000-0000-0000-0000-000000000705', '00000000-0000-0000-0000-000000000023', 'omeprazole',   0, 12.9300, 77.6800, NOW() - INTERVAL 2 DAY),
  ('00000000-0000-0000-0000-000000000706', '00000000-0000-0000-0000-000000000024', 'atorvastatin', 0, 12.9000, 77.6000, NOW() - INTERVAL 3 DAY),
  ('00000000-0000-0000-0000-000000000707', NULL,                                    'metformin',    0, 12.9716, 77.5946, NOW() - INTERVAL 4 HOUR),
  ('00000000-0000-0000-0000-000000000708', '00000000-0000-0000-0000-000000000020', 'paracetamol',  0, 12.9716, 77.5946, NOW() - INTERVAL 5 HOUR),
  ('00000000-0000-0000-0000-000000000709', '00000000-0000-0000-0000-000000000021', 'paracetamol',  0, 12.9550, 77.6100, NOW() - INTERVAL 8 HOUR);

-- ============================================================
-- 9. DEMAND ANALYTICS  (per pharmacy + medicine + day)
-- ============================================================
-- Today's row plus a few prior days so analytics charts have shape.
INSERT INTO demand_analytics (id, pharmacy_id, medicine_id, search_count, reservation_count, period_date) VALUES
  -- TODAY
  ('00000000-0000-0000-0000-000000000800', '00000000-0000-0000-0000-000000000100', '00000000-0000-0000-0000-000000000200', 12, 2, CURDATE()),
  ('00000000-0000-0000-0000-000000000801', '00000000-0000-0000-0000-000000000100', '00000000-0000-0000-0000-000000000205',  8, 1, CURDATE()),
  ('00000000-0000-0000-0000-000000000802', '00000000-0000-0000-0000-000000000100', '00000000-0000-0000-0000-000000000212',  3, 0, CURDATE()),
  ('00000000-0000-0000-0000-000000000803', '00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000200', 15, 0, CURDATE()),
  ('00000000-0000-0000-0000-000000000804', '00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000205',  9, 1, CURDATE()),
  ('00000000-0000-0000-0000-000000000805', '00000000-0000-0000-0000-000000000102', '00000000-0000-0000-0000-000000000200', 22, 0, CURDATE()),
  ('00000000-0000-0000-0000-000000000806', '00000000-0000-0000-0000-000000000102', '00000000-0000-0000-0000-000000000210',  7, 1, CURDATE()),
  ('00000000-0000-0000-0000-000000000807', '00000000-0000-0000-0000-000000000102', '00000000-0000-0000-0000-000000000211',  5, 0, CURDATE()),

  -- YESTERDAY
  ('00000000-0000-0000-0000-000000000810', '00000000-0000-0000-0000-000000000100', '00000000-0000-0000-0000-000000000200',  9, 1, CURDATE() - INTERVAL 1 DAY),
  ('00000000-0000-0000-0000-000000000811', '00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000205',  6, 0, CURDATE() - INTERVAL 1 DAY),
  ('00000000-0000-0000-0000-000000000812', '00000000-0000-0000-0000-000000000102', '00000000-0000-0000-0000-000000000203', 10, 1, CURDATE() - INTERVAL 1 DAY),

  -- 2 DAYS AGO
  ('00000000-0000-0000-0000-000000000820', '00000000-0000-0000-0000-000000000100', '00000000-0000-0000-0000-000000000200',  7, 0, CURDATE() - INTERVAL 2 DAY),
  ('00000000-0000-0000-0000-000000000821', '00000000-0000-0000-0000-000000000102', '00000000-0000-0000-0000-000000000209',  4, 1, CURDATE() - INTERVAL 2 DAY),

  -- 6 DAYS AGO  (matches Sneha's emergency reservation event)
  ('00000000-0000-0000-0000-000000000830', '00000000-0000-0000-0000-000000000102', '00000000-0000-0000-0000-000000000211', 11, 1, CURDATE() - INTERVAL 6 DAY);

-- ============================================================
-- 10. PREDICTIVE ALERTS  (US-12/13/14 — schema only; no service yet)
-- ============================================================
INSERT INTO predictive_alert (id, pharmacy_id, medicine_id, alert_type, message, is_read, created_at) VALUES
  ('00000000-0000-0000-0000-000000000900', '00000000-0000-0000-0000-000000000100', '00000000-0000-0000-0000-000000000203',
   'LOW_STOCK', 'Ibuprofen stock is low (8 units). Reorder recommended.', 0, NOW() - INTERVAL 6 HOUR),

  ('00000000-0000-0000-0000-000000000901', '00000000-0000-0000-0000-000000000100', '00000000-0000-0000-0000-000000000210',
   'OUT_OF_STOCK', 'Cetirizine is out of stock and was searched 3 times today.', 0, NOW() - INTERVAL 4 HOUR),

  ('00000000-0000-0000-0000-000000000902', '00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000211',
   'LOW_STOCK', 'Azithromycin running low (5 units).', 1, NOW() - INTERVAL 1 DAY),

  ('00000000-0000-0000-0000-000000000903', '00000000-0000-0000-0000-000000000102', '00000000-0000-0000-0000-000000000200',
   'HIGH_DEMAND', 'Paracetamol search volume up 40% over the last week.', 0, NOW() - INTERVAL 12 HOUR);

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- VERIFICATION
-- ============================================================
SELECT 'users'                AS table_name, COUNT(*) AS `rows` FROM users
UNION ALL SELECT 'pharmacy',             COUNT(*) FROM pharmacy
UNION ALL SELECT 'medicine',             COUNT(*) FROM medicine
UNION ALL SELECT 'pharmacy_stock',       COUNT(*) FROM pharmacy_stock
UNION ALL SELECT 'pharmacy_documents',   COUNT(*) FROM pharmacy_documents
UNION ALL SELECT 'medicine_alternative', COUNT(*) FROM medicine_alternative
UNION ALL SELECT 'reservation',          COUNT(*) FROM reservation
UNION ALL SELECT 'search_log',           COUNT(*) FROM search_log
UNION ALL SELECT 'demand_analytics',     COUNT(*) FROM demand_analytics
UNION ALL SELECT 'predictive_alert',     COUNT(*) FROM predictive_alert;

-- ============================================================
-- TEST ACCOUNTS QUICK REFERENCE
-- ============================================================
-- All passwords are: password
--
-- ADMIN:
--   admin@pharmaconnect.com
--
-- SELLERS (each owns one pharmacy):
--   rajesh@medplus.com      → MedPlus HSR Layout      (verified)
--   priya@apollopharm.com   → Apollo Indiranagar       (verified)
--   arjun@247pharma.com     → 24/7 Pharma Whitefield   (verified, 24/7)
--   meera@wellness.com      → Wellness Forever BTM     (PENDING — admin must verify)
--
-- PATIENTS:
--   anil@example.com    (has 1 active hold + 1 claimed reservation)
--   sneha@example.com   (has 1 active hold + 1 emergency claim)
--   vikram@example.com  (has 1 active hold + 1 claimed reservation)
--   ananya@example.com  (has 1 cancelled reservation)
--   karthik@example.com (has 1 expired reservation)
-- ============================================================
