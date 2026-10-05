-- WarehouseSystem — skema Supabase (PostgreSQL, terma shqip)
-- Ekzekuto një herë në Supabase SQL Editor

DROP TABLE IF EXISTS dergesat CASCADE;
DROP TABLE IF EXISTS artikujt_porosise CASCADE;
DROP TABLE IF EXISTS porosite CASCADE;
DROP TABLE IF EXISTS produktet CASCADE;
DROP TABLE IF EXISTS perdoruesit CASCADE;

-- Përdoruesit (login i aplikacionit, jo Supabase Auth)
CREATE TABLE perdoruesit (
    id               SERIAL PRIMARY KEY,
    emri_perdoruesit VARCHAR(50)  NOT NULL UNIQUE,
    fjalekalimi      VARCHAR(100) NOT NULL,
    emri_plote       VARCHAR(100) NOT NULL,
    roli             VARCHAR(20)  NOT NULL
        CHECK (roli IN ('ADMIN', 'MENAXHER', 'OPERATOR'))
);

-- Produktet
CREATE TABLE produktet (
    id              SERIAL PRIMARY KEY,
    emri            VARCHAR(150) NOT NULL,
    pershkrimi      TEXT,
    cmimi           DOUBLE PRECISION NOT NULL DEFAULT 0,
    sasia           INTEGER NOT NULL DEFAULT 0 CHECK (sasia >= 0),
    kategoria       VARCHAR(30) NOT NULL
        CHECK (kategoria IN ('ELEKTRONIKE', 'ELEKTROSHTEPIAK', 'USHQIM', 'VESHJE', 'TJETER')),
    njesia          VARCHAR(20) NOT NULL DEFAULT 'cope',
    stoku_minimal   INTEGER NOT NULL DEFAULT 0 CHECK (stoku_minimal >= 0)
);

-- Porositë
CREATE TABLE porosite (
    id                      SERIAL PRIMARY KEY,
    numri_porosise          VARCHAR(20) NOT NULL UNIQUE,
    data_porosise           DATE NOT NULL,
    data_dorezimit          DATE,
    statusi                 VARCHAR(20) NOT NULL
        CHECK (statusi IN ('NE_PRITJE', 'KONFIRMUAR', 'NE_PROCES', 'DERGUAAR', 'DOREZUAR', 'ANULUAR')),
    lloji                   VARCHAR(20) NOT NULL
        CHECK (lloji IN ('HYRJE', 'DALJE')),
    furnizuesi_ose_klienti  VARCHAR(150) NOT NULL,
    shenime                 TEXT,
    krijuar_nga_id          INTEGER NOT NULL REFERENCES perdoruesit(id),
    stoku_aplikuar          BOOLEAN NOT NULL DEFAULT FALSE
);

-- Artikujt e porosisë
CREATE TABLE artikujt_porosise (
    id              SERIAL PRIMARY KEY,
    porosia_id      INTEGER NOT NULL REFERENCES porosite(id) ON DELETE CASCADE,
    produkti_id     INTEGER NOT NULL REFERENCES produktet(id),
    sasia           INTEGER NOT NULL CHECK (sasia > 0),
    cmimi_njesi     DOUBLE PRECISION NOT NULL CHECK (cmimi_njesi >= 0),
    UNIQUE (porosia_id, produkti_id)
);

-- Dërgesat
CREATE TABLE dergesat (
    id                      SERIAL PRIMARY KEY,
    numri_gjurmimit         VARCHAR(50) NOT NULL UNIQUE,
    porosia_id              INTEGER NOT NULL REFERENCES porosite(id),
    data_dergeses           DATE NOT NULL,
    data_vleresuar_mbrritje DATE,
    statusi                 VARCHAR(20) NOT NULL
        CHECK (statusi IN ('NE_PERGATITJE', 'DERGUAAR', 'NE_TRANSIT', 'DOREZUAR', 'KTHYER')),
    transportuesi           VARCHAR(100) NOT NULL,
    destinacioni            VARCHAR(150) NOT NULL,
    pesha                   DOUBLE PRECISION NOT NULL DEFAULT 0,
    shenime                 TEXT
);

-- Indekset
CREATE INDEX idx_produktet_emri ON produktet (emri);
CREATE INDEX idx_porosite_statusi ON porosite (statusi);
CREATE INDEX idx_porosite_data ON porosite (data_porosise DESC);
CREATE INDEX idx_artikujt_porosia ON artikujt_porosise (porosia_id);
CREATE INDEX idx_dergesat_porosia ON dergesat (porosia_id);

-- Të dhëna fillestare — përdoruesit
INSERT INTO perdoruesit (emri_perdoruesit, fjalekalimi, emri_plote, roli) VALUES
    ('admin',    'admin123', 'Artan Hoxha',  'ADMIN'),
    ('manager',  'man123',   'Blerina Koci', 'MENAXHER'),
    ('operator', 'op123',    'Genti Mema',   'OPERATOR');

-- Të dhëna fillestare — produktet
INSERT INTO produktet (emri, pershkrimi, cmimi, sasia, kategoria, njesia, stoku_minimal) VALUES
    ('Laptop Dell XPS',        'Laptop 15 inch',       85000, 15, 'ELEKTRONIKE',     'cope', 5),
    ('Monitor Samsung 24"',    'Monitor Full HD',      22000,  8, 'ELEKTRONIKE',     'cope', 3),
    ('Tastaturen Logitech',    'Tastature wireless',    4500, 30, 'ELEKTRONIKE',     'cope', 10),
    ('Karrike Zyre',           'Karrike ergonomike',   12000, 12, 'TJETER',          'cope', 4),
    ('Kafe Lavazza 1kg',       'Kafe e bluar',          1200, 50, 'USHQIM',          'kg',   20),
    ('Frigorifer Bosch',       'Frigorifer 350L',      48000,  6, 'ELEKTROSHTEPIAK', 'cope', 2),
    ('Pallto Dimri',           'Pallto L/XL',           5500,  3, 'VESHJE',          'cope', 5),
    ('Printer HP LaserJet',    'Printer lazer A4',     18000, 13, 'ELEKTRONIKE',     'cope', 3);

-- Të dhëna fillestare — porositë (admin id=1, menaxher id=2)
INSERT INTO porosite (numri_porosise, data_porosise, data_dorezimit, statusi, lloji, furnizuesi_ose_klienti, shenime, krijuar_nga_id, stoku_aplikuar)
VALUES
    ('ORD-001', CURRENT_DATE - 5,  CURRENT_DATE + 2,  'KONFIRMUAR', 'HYRJE', 'TechSupply SH.P.K', 'Porosi urgjente',   1, FALSE),
    ('ORD-002', CURRENT_DATE - 2,  CURRENT_DATE + 5,  'NE_PROCES',  'DALJE', 'Klenti ABC',        'Dorëzim ne Tirane', 2, FALSE),
    ('ORD-003', CURRENT_DATE - 10, CURRENT_DATE - 1,  'DOREZUAR',   'HYRJE', 'ElectroShop',       'Pa shenime',        1, TRUE);

INSERT INTO artikujt_porosise (porosia_id, produkti_id, sasia, cmimi_njesi) VALUES
    (1, 1, 3, 83000),
    (1, 2, 2, 21000),
    (2, 3, 10, 4500),
    (2, 5, 5, 1200),
    (3, 8, 4, 17500);

INSERT INTO dergesat (numri_gjurmimit, porosia_id, data_dergeses, data_vleresuar_mbrritje, statusi, transportuesi, destinacioni, pesha, shenime)
VALUES
    ('TRK-20240501', 2, CURRENT_DATE - 1, CURRENT_DATE + 3, 'NE_TRANSIT', 'DHL Albania', 'Tirane, Shqiperi', 12.5, 'Fragile'),
    ('TRK-20240498', 3, CURRENT_DATE - 8, CURRENT_DATE - 1, 'DOREZUAR',   'GLS Express', 'Durres, Shqiperi',  8.2, '');

-- Rregullo sekuencat pas seed-it
SELECT setval(pg_get_serial_sequence('perdoruesit', 'id'),        (SELECT MAX(id) FROM perdoruesit));
SELECT setval(pg_get_serial_sequence('produktet', 'id'),         (SELECT MAX(id) FROM produktet));
SELECT setval(pg_get_serial_sequence('porosite', 'id'),          (SELECT MAX(id) FROM porosite));
SELECT setval(pg_get_serial_sequence('artikujt_porosise', 'id'), (SELECT MAX(id) FROM artikujt_porosise));
SELECT setval(pg_get_serial_sequence('dergesat', 'id'),         (SELECT MAX(id) FROM dergesat));
